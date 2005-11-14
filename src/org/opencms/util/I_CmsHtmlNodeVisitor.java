/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/util/I_CmsHtmlNodeVisitor.java,v $
 * Date   : $Date: 2005/11/14 15:04:05 $
 * Version: $Revision: 1.1.2.1 $
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

package org.opencms.util;

import org.htmlparser.Remark;
import org.htmlparser.Tag;
import org.htmlparser.Text;
import org.htmlparser.util.ParserException;

public interface I_CmsHtmlNodeVisitor {

    /**
     * Extracts the text from the given html content, assuming the given html encoding.<p>
     * 
     * @param html the content to extract the plain text from
     * @param encoding the encoding to use
     * 
     * @return the text extracted from the given html content
     * 
     * @throws ParserException if something goes wrong
     */
    String process(String html, String encoding) throws ParserException;

    /**
     * Returns the text extraction result.<p>
     * 
     * @return the text extraction result
     */
    String getResult();

    /**
     * @see org.htmlparser.visitors.NodeVisitor#visitEndTag(org.htmlparser.Tag)
     */
    void visitEndTag(Tag tag);

    /**
     * @see org.htmlparser.visitors.NodeVisitor#visitRemarkNode(org.htmlparser.Remark)
     */
    void visitRemarkNode(Remark remark);

    /**
     * @see org.htmlparser.visitors.NodeVisitor#visitStringNode(org.htmlparser.Text)
     */
    void visitStringNode(Text text);

    /**
     * @see org.htmlparser.visitors.NodeVisitor#visitTag(org.htmlparser.Tag)
     */
    void visitTag(Tag tag);
}