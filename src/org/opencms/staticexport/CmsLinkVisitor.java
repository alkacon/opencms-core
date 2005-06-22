/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/staticexport/Attic/CmsLinkVisitor.java,v $
 * Date   : $Date: 2005/06/22 10:38:16 $
 * Version: $Revision: 1.6 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Mananagement System
 *
 * Copyright (C) 2002 - 2005 Alkacon Software (http://www.alkacon.com)
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
 * For further information about Alkacon Software, please see the
 * company website: http://www.alkacon.com
 *
 * For further information about OpenCms, please see the
 * project website: http://www.opencms.org
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package org.opencms.staticexport;

import org.htmlparser.Node;
import org.htmlparser.Remark;
import org.htmlparser.Tag;
import org.htmlparser.Text;
import org.htmlparser.tags.CompositeTag;
import org.htmlparser.tags.ImageTag;
import org.htmlparser.tags.LinkTag;
import org.htmlparser.visitors.NodeVisitor;

/**
 * Implements the HTML parser node visitor pattern to
 * exchange all links on the page.<p>
 * 
 * @author Alexander Kandzior 
 * 
 * @version $Revision: 1.6 $
 * @since 5.3
 */
public class CmsLinkVisitor extends NodeVisitor {

    /** The link processor. */
    private CmsLinkProcessor m_linkProcessor;

    /** The processed content. */
    private StringBuffer m_result;

    /**
     * Public constructor.<p>
     * 
     * @param linkProcessor the link processor to use 
     */
    public CmsLinkVisitor(CmsLinkProcessor linkProcessor) {

        super(true, true);
        m_linkProcessor = linkProcessor;
        m_result = new StringBuffer(1024);
    }

    /**
     * Returns the generated HTML.<p>
     * 
     * @return the generated HTML
     */
    public String getHtml() {

        return m_result.toString();
    }

    /**
     * Visitor method to process a tag end.<p>
     * 
     * @param tag the tag to process
     */
    public void visitEndTag(Tag tag) {

        Node parent = tag.getParent();
        // process only those nodes not processed by a parent
        if (parent == null) {
            // an orphan end tag
            m_result.append(tag.toHtml());
        } else if (parent.getParent() == null) {
            // a top level tag with no parents
            m_result.append(parent.toHtml());
        }
    }

    /**
     * Visitor method to process an image tag.<p>
     * 
     * @param imageTag the tag to process
     */
    public void visitImageTag(ImageTag imageTag) {

        m_linkProcessor.processImageTag(imageTag);
    }

    /**
     * Visitor method to process a single link.<p>
     * 
     * @param linkTag the tag to process
     */
    public void visitLinkTag(LinkTag linkTag) {

        m_linkProcessor.processLinkTag(linkTag);
    }

    /**
     * Visitor method to process a remark.<p>
     * 
     * @param node the node to process
     */
    public void visitRemarkNode(Remark node) {

        if (null == node.getParent()) {
            m_result.append(node.toHtml());
        }
    }

    /**
     * Visitor method to process a string node.<p>
     * 
     * @param node the string node to process
     */
    public void visitStringNode(Text node) {

        if (null == node.getParent()) {
            m_result.append(node.toHtml());
        }
    }

    /**
     * Visitor method to process a tag (start).<p>
     * 
     * @param tag the tag to process
     */
    public void visitTag(Tag tag) {

        if (tag instanceof LinkTag) {
            visitLinkTag((LinkTag)tag);
        } else if (tag instanceof ImageTag) {
            visitImageTag((ImageTag)tag);
        } else {
            // process only those nodes that won't be processed by an end tag,
            // nodes without parents or parents without an end tag, since
            // the complete processing of all children should happen before
            // we turn this node back into html text        
            if ((tag.getParent() == null)
                && (!(tag instanceof CompositeTag) || (null == ((CompositeTag)tag).getEndTag()))) {
                m_result.append(tag.toHtml());
            }
        }
    }
}