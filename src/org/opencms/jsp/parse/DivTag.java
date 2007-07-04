/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/jsp/parse/Attic/DivTag.java,v $
 * Date   : $Date: 2007/07/04 16:57:49 $
 * Version: $Revision: 1.3 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Mananagement System
 *
 * Copyright (C) 2005 Alkacon Software GmbH (http://www.alkacon.com)
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * For further information about Alkacon Software GmbH, please see the
 * company website: http://www.alkacon.com
 *
 * For further information about OpenCms, please see the
 * project website: http://www.opencms.org
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package org.opencms.jsp.parse;

import org.htmlparser.nodes.TagNode;
import org.htmlparser.tags.Div;

/**
 * A <code>{@link Div} </code> for flat parsing (vs. nested) which is misued for avoiding the creation of 
 * the corresponding end tag in case the html to parse is not balanced.<p>
 * 
 * @author Achim Westermann
 * 
 * @version $Revision: 1.3 $
 * 
 * @since 6.2.2
 *
 */
public class DivTag extends TagNode {

    /** 
     * Mimick the same behviour (except nesting of tags) as the tag this one replaces. Caution this field has to be 
     * static or NPE will happen (getIds is called earlier). 
     */
    private static Div m_mimicked = new Div();

    /** Generated serial version UID. */
    private static final long serialVersionUID = -6409422683628200225L;

    /**
     * @see org.htmlparser.nodes.TagNode#getEnders()
     */
    public String[] getEnders() {

        return m_mimicked.getEnders();
    }

    /**
     * @see org.htmlparser.nodes.TagNode#getIds()
     */
    public String[] getIds() {

        return m_mimicked.getIds();
    }

}
