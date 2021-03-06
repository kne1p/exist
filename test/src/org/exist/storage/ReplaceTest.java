/*
 *  eXist Open Source Native XML Database
 *  Copyright (C) 2001-04 The eXist Project
 *  http://exist-db.org
 *  
 *  This program is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU Lesser General Public License
 *  as published by the Free Software Foundation; either version 2
 *  of the License, or (at your option) any later version.
 *  
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU Lesser General Public License for more details.
 *  
 *  You should have received a copy of the GNU Lesser General Public License
 *  along with this program; if not, write to the Free Software
 *  Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
 *  
 *  $Id$
 */
package org.exist.storage;

import org.exist.collections.IndexInfo;
import org.exist.dom.persistent.DefaultDocumentSet;
import org.exist.dom.persistent.MutableDocumentSet;
import org.exist.security.xacml.AccessContext;
import org.exist.storage.dom.DOMFile;
import org.exist.storage.txn.TransactionManager;
import org.exist.storage.txn.Txn;
import org.exist.xupdate.Modification;
import org.exist.xupdate.XUpdateProcessor;
import org.xml.sax.InputSource;
import org.junit.Test;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import java.io.StringReader;

public class ReplaceTest extends AbstractUpdateTest {

    @Test
    public void update() {
        BrokerPool.FORCE_CORRUPTION = true;
        BrokerPool pool = null;        
        DBBroker broker = null;
        try {
        	pool = startDB();
        	assertNotNull(pool);
            broker = pool.get(pool.getSecurityManager().getSystemSubject());
            assertNotNull(broker);
            TransactionManager mgr = pool.getTransactionManager();
            assertNotNull(mgr);
            
            IndexInfo info = init(broker, mgr);
            assertNotNull(info);
            MutableDocumentSet docs = new DefaultDocumentSet();
            docs.add(info.getDocument());
            XUpdateProcessor proc = new XUpdateProcessor(broker, docs, AccessContext.TEST);
            assertNotNull(proc);
            
            Txn transaction = mgr.beginTransaction();
            assertNotNull(transaction);
            System.out.println("Transaction started ...");
            
            String xupdate;
            Modification modifications[];
            
            xupdate =
                "<xu:modifications version=\"1.0\" xmlns:xu=\"http://www.xmldb.org/xupdate\">" +
                "   <xu:append select=\"/products\">" +
                "       <product id=\"1\">" +
                "           <description>Product 1</description>" +
                "           <price>24.30</price>" +
                "           <stock>10</stock>" +
                "       </product>" +
                "   </xu:append>" +
                "</xu:modifications>";
            proc.setBroker(broker);
            proc.setDocumentSet(docs);
            modifications = proc.parse(new InputSource(new StringReader(xupdate)));
            assertNotNull(modifications);
            modifications[0].process(transaction);
            proc.reset();
            
            // append some new element to records
            for (int i = 1; i <= 200; i++) {
                xupdate =
                    "<xu:modifications version=\"1.0\" xmlns:xu=\"http://www.xmldb.org/xupdate\">" +
                    "   <xu:insert-before select=\"/products/product[1]\">" +
                    "       <product>" +
                    "           <xu:attribute name=\"id\"><xu:value-of select=\"count(/products/product) + 1\"/></xu:attribute>" +
                    "           <description>Product " + i + "</description>" +
                    "           <price>" + (i * 2.5) + "</price>" +
                    "           <stock>" + (i * 10) + "</stock>" +
                    "       </product>" +
                    "   </xu:insert-before>" +
                    "</xu:modifications>";
                proc.setBroker(broker);
                proc.setDocumentSet(docs);
                modifications = proc.parse(new InputSource(new StringReader(xupdate)));
                assertNotNull(modifications);
                modifications[0].process(transaction);
                proc.reset();
            }
            
            DOMFile domDb = ((NativeBroker) broker).getDOMFile();
            assertNotNull(domDb);
            System.out.println(domDb.debugPages(info.getDocument(), false));
            
            mgr.commit(transaction);
            System.out.println("Transaction commited ...");            
            
            // the following transaction will not be committed and thus undone during recovery
            transaction = mgr.beginTransaction();
            System.out.println("Transaction started...");   
            
            // replace elements
            for (int i = 1; i <= 100; i++) {
                xupdate =
                    "<xu:modifications version=\"1.0\" xmlns:xu=\"http://www.xmldb.org/xupdate\">" +
                    "   <xu:replace select=\"/products/product[" + i + "]\">" +
                    "     <product id=\"" + i + "\">" +
                    "         <description>Replaced product</description>" +
                    "         <price>" + (i * 0.75) + "</price>" +
                    "     </product>" +
                    " </xu:replace>" +
                    "</xu:modifications>";
                proc.setBroker(broker);
                proc.setDocumentSet(docs);
                modifications = proc.parse(new InputSource(new StringReader(xupdate)));
                modifications[0].process(transaction);
                proc.reset();
            }
//            mgr.commit(transaction);
            
//          Don't commit...              
            pool.getTransactionManager().getJournal().flushToLog(true);
            System.out.println("Transaction interrupted ...");
	    } catch (Exception e) {            
	        fail(e.getMessage());             
        } finally {
            pool.release(broker);
        }
    }
}
