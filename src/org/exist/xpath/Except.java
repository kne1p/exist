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
package org.exist.xpath;

import org.exist.dom.DocumentSet;
import org.exist.dom.NodeSet;
import org.exist.xpath.value.Item;
import org.exist.xpath.value.Sequence;
import org.exist.xpath.value.Type;

/**
 * @author Wolfgang Meier (wolfgang@exist-db.org)
 */
public class Except extends CombiningExpression {
	
	/**
	 * 
	 */
	public Except(StaticContext context, PathExpr left, PathExpr right) {
		super(context, left, right);
	}

	/* (non-Javadoc)
	 * @see org.exist.xpath.CombiningExpression#eval(org.exist.dom.DocumentSet, org.exist.xpath.value.Sequence, org.exist.xpath.value.Item)
	 */
	public Sequence eval(DocumentSet docs, Sequence contextSequence, Item contextItem)
		throws XPathException {
		Sequence lval = left.eval(docs, contextSequence, contextItem);
		Sequence rval = right.eval(docs, contextSequence, contextItem);
		if(lval.getItemType() != Type.NODE || rval.getItemType() != Type.NODE)
			throw new XPathException("intersect operand is not a node sequence");
		NodeSet result = ((NodeSet)lval).intersection((NodeSet)rval);
		return result;
	}
	
	public String pprint() {
		StringBuffer buf = new StringBuffer();
		buf.append(left.pprint());
		buf.append(" union ");
		buf.append(right.pprint());
		return buf.toString();
	}
}
