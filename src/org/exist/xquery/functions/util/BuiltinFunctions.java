/*
 *  eXist Open Source Native XML Database
 *  Copyright (C) 2001-03 Wolfgang M. Meier
 *  wolfgang@exist-db.org
 *  http://exist.sourceforge.net
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
package org.exist.xquery.functions.util;

import java.util.Iterator;
import java.util.Set;
import java.util.TreeSet;

import org.exist.dom.QName;
import org.exist.xquery.Cardinality;
import org.exist.xquery.Function;
import org.exist.xquery.FunctionSignature;
import org.exist.xquery.Module;
import org.exist.xquery.XPathException;
import org.exist.xquery.XQueryContext;
import org.exist.xquery.value.Item;
import org.exist.xquery.value.QNameValue;
import org.exist.xquery.value.Sequence;
import org.exist.xquery.value.SequenceType;
import org.exist.xquery.value.Type;
import org.exist.xquery.value.ValueSequence;

/**
 * Returns a sequence containing the QNames of all built-in functions
 * currently registered in the query engine.
 * 
 * @author wolf
 */
public class BuiltinFunctions extends Function {

	public final static FunctionSignature signature =
		new FunctionSignature(
			new QName("builtin-functions", UtilModule.NAMESPACE_URI, UtilModule.PREFIX),
			"Returns a sequence containing the QNames of all built-in functions " +
			"currently known to the system.",
			null,
			new SequenceType(Type.STRING, Cardinality.ONE_OR_MORE));

	public BuiltinFunctions(XQueryContext context) {
		super(context, signature);
	}

	/* (non-Javadoc)
	 * @see org.exist.xquery.Expression#eval(org.exist.dom.DocumentSet, org.exist.xquery.value.Sequence, org.exist.xquery.value.Item)
	 */
	public Sequence eval(
		Sequence contextSequence,
		Item contextItem)
		throws XPathException {
		Set set = new TreeSet();
		ValueSequence resultSeq = new ValueSequence();
		for(Iterator i = context.getModules(); i.hasNext(); ) {
			Module module = (Module)i.next();
			FunctionSignature signatures[] = module.listFunctions();
			// add to set to remove duplicate QName's
			for(int j = 0; j < signatures.length; j++) {
				QName qname = signatures[j].getName();
				set.add(qname);
			}
			for(Iterator it = set.iterator(); it.hasNext(); ) {
				QName qname = (QName)it.next();
				resultSeq.add(new QNameValue(context, qname));
			}
			set.clear();
		}
		return resultSeq;
	}

}
