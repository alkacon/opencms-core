/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/jsp/CmsJspTagContentShow.java,v $
 * Date   : $Date: 2004/10/18 13:57:54 $
 * Version: $Revision: 1.2 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Mananagement System
 *
 * Copyright (C) 2002 - 2003 Alkacon Software (http://www.alkacon.com)
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

package org.opencms.jsp;

import org.opencms.file.CmsObject;
import org.opencms.flex.CmsFlexController;
import org.opencms.i18n.CmsMessages;
import org.opencms.main.OpenCms;
import org.opencms.util.CmsStringUtil;
import org.opencms.xml.A_CmsXmlDocument;
import org.opencms.xml.CmsXmlException;

import java.io.IOException;
import java.util.Locale;

import javax.servlet.ServletRequest;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspTagException;
import javax.servlet.jsp.tagext.Tag;
import javax.servlet.jsp.tagext.TagSupport;

/**
 * Used to access and display XML content item information from the VFS.<p>
 * 
 * @author  Alexander Kandzior (a.kandzior@alkacon.com)
 * 
 * @version $Revision: 1.2 $
 * @since 5.5.0
 */
public class CmsJspTagContentShow extends TagSupport {

    /** Name of the content node element to show. */
    private String m_element;

    /** Index of the content node element to show. */
    private int m_index = -1;

    /**
     * Internal action method to show an element from a XML content document.<p>
     * @param content the XML content to show the element from
     * @param element the node name of the element to show
     * @param locale the locale of the element to show
     * @param index the node index of the element to show
     * @param req the current request 
     * 
     * @return the value of the selected content element
     */
    public static String contentShowTagAction(
        A_CmsXmlDocument content,
        String element,
        Locale locale,
        int index,
        ServletRequest req) {

        if (content == null) {
            // no content was loaded
            return null;
        }

        if (content.hasValue(element, locale, index)) {

            // selected element is available in content
            CmsFlexController controller = (CmsFlexController)req.getAttribute(CmsFlexController.ATTRIBUTE_NAME);
            CmsObject cms = controller.getCmsObject();

            try {
                // read the element from the content
                return content.getStringValue(cms, element, locale, index);
            } catch (CmsXmlException e) {
                OpenCms.getLog(CmsJspTagContentShow.class).error(
                    "Error processing content element '" + element + '[' + index + "]'",
                    e);
                return null;
            }
        } else {
            return null;
        }
    }

    /**
     * @see javax.servlet.jsp.tagext.Tag#doStartTag()
     */
    public int doStartTag() throws JspException {

        // get a reference to the parent "content container" class
        Tag ancestor = findAncestorWithClass(this, I_CmsJspTagContentContainer.class);
        if (ancestor == null) {
            throw new JspTagException("Tag <contentshow> without required parent tag <contentload> found!");
        }
        I_CmsJspTagContentContainer contentContainer = (I_CmsJspTagContentContainer)ancestor;

        // get loaded content from parent <contentload> tag
        A_CmsXmlDocument xmlContent = contentContainer.getXmlDocument();
        Locale locale = contentContainer.getXmlDocumentLocale();

        String element = getElement();

        if (CmsStringUtil.isEmpty(element)) {
            element = contentContainer.getXmlDocumentElement();
        }

        String content;
        if (element.startsWith(I_CmsJspTagContentContainer.C_MAGIC_PREFIX)) {

            // this is a "magic" element name, resolve it
            content = contentContainer.resolveMagicName(element);
        } else {

            // resolve "normal" content reference
            int index = getIndex();
            if (getIndex() < 0) {
                index = contentContainer.getXmlDocumentIndex();
            }

            // now get the content element value to display
            content = contentShowTagAction(xmlContent, element, locale, index, pageContext.getRequest());

            // make sure that no null String is returned
            if (content == null) {
                content = CmsMessages.formatUnknownKey(element + '[' + index + ']');
            }
        }

        try {
            pageContext.getOut().print(content);
        } catch (IOException e) {
            if (OpenCms.getLog(this).isErrorEnabled()) {
                OpenCms.getLog(this).error("Error in Jsp <contentshow> tag processing", e);
            }
            throw new JspException(e);
        }

        return SKIP_BODY;
    }

    /**
     * Returns the name of the content node element to show.<p>
     * 
     * @return the name of the content node element to show
     */
    public String getElement() {

        return (m_element != null) ? m_element : "";
    }

    /**
     * Returns the index of the content node element to show.<p>
     *
     * @return the index of the content node element to show
     */
    public int getIndex() {

        return m_index;
    }

    /**
     * @see javax.servlet.jsp.tagext.Tag#release()
     */
    public void release() {

        super.release();
        m_element = null;
        m_index = -1;
    }

    /**
     * Sets the name of the content node element to show.<p>
     * 
     * @param element the name of the content node element to show
     */
    public void setElement(String element) {

        m_element = element;
    }

    /**
     * Sets the index of the content node element to show.<p>
     *
     * @param index the index of the content node element to show
     */
    public void setIndex(int index) {

        m_index = index;
    }
}