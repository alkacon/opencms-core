/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/jsp/CmsJspTagContentShow.java,v $
 * Date   : $Date: 2005/07/03 09:41:52 $
 * Version: $Revision: 1.22 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Mananagement System
 *
 * Copyright (c) 2005 Alkacon Software GmbH (http://www.alkacon.com)
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

package org.opencms.jsp;

import org.opencms.file.CmsObject;
import org.opencms.flex.CmsFlexController;
import org.opencms.i18n.CmsMessageContainer;
import org.opencms.i18n.CmsMessages;
import org.opencms.main.CmsLog;
import org.opencms.util.CmsMacroResolver;
import org.opencms.util.CmsStringUtil;
import org.opencms.xml.A_CmsXmlDocument;
import org.opencms.xml.CmsXmlUtils;

import java.io.IOException;
import java.util.Locale;

import javax.servlet.ServletRequest;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspTagException;
import javax.servlet.jsp.tagext.Tag;
import javax.servlet.jsp.tagext.TagSupport;

import org.apache.commons.logging.Log;

/**
 * Used to access and display XML content item information from the VFS.<p>
 * 
 * @author  Alexander Kandzior 
 * 
 * @version $Revision: 1.22 $ 
 * 
 * @since 6.0.0 
 */
public class CmsJspTagContentShow extends TagSupport {

    /** Serial version UID required for safe serialization. */
    private static final long serialVersionUID = -6776067180965738432L;

    /** The log object for this class. */
    private static final Log LOG = CmsLog.getLog(TagSupport.class);

    /** Name of the content node element to show. */
    private String m_element;

    /**
     * Internal action method to show an element from a XML content document.<p>
     * 
     * @param content the XML content to show the element from
     * @param element the node name of the element to show
     * @param locale the locale of the element to show
     * @param req the current request 
     * 
     * @return the value of the selected content element
     */
    public static String contentShowTagAction(
        A_CmsXmlDocument content,
        String element,
        Locale locale,
        ServletRequest req) {

        if (content == null) {
            // no content was loaded
            return null;
        }

        if (content.hasValue(element, locale)) {
            // selected element is available in content
            CmsObject cms = CmsFlexController.getCmsObject(req);
            try {
                // read the element from the content
                return content.getStringValue(cms, element, locale);
            } catch (Exception e) {
                LOG.error(Messages.get().key(Messages.LOG_ERR_CONTENT_SHOW_1, element), e);
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

        // initialize a string mapper to resolve EL like strings in tag attributes
        CmsObject cms = CmsFlexController.getCmsObject(pageContext.getRequest());

        // get a reference to the parent "content container" class
        Tag ancestor = findAncestorWithClass(this, I_CmsJspTagContentContainer.class);
        if (ancestor == null) {
            CmsMessageContainer errMsgContainer = Messages.get().container(Messages.ERR_PARENTLESS_TAG_1, "contentshow");
            String msg = Messages.getLocalizedMessage(errMsgContainer, pageContext);
            throw new JspTagException(msg);
        }
        I_CmsJspTagContentContainer contentContainer = (I_CmsJspTagContentContainer)ancestor;

        // get loaded content from parent <contentload> tag
        A_CmsXmlDocument xmlContent = contentContainer.getXmlDocument();
        Locale locale = contentContainer.getXmlDocumentLocale();

        String element = getElement();

        if (CmsStringUtil.isEmpty(element)) {
            element = contentContainer.getXmlDocumentElement();
        } else {
            element = CmsXmlUtils.concatXpath(contentContainer.getXmlDocumentElement(), element);
        }

        String content;
        if (CmsMacroResolver.isMacro(element)) {
            // this is a macro, initialize a macro resolver
            String resourcename = CmsJspTagContentLoad.getResourceName(cms, contentContainer);
            CmsMacroResolver resolver = CmsMacroResolver.newInstance().setCmsObject(cms).setJspPageContext(pageContext).setResourceName(
                resourcename).setKeepEmptyMacros(true);
            // resolve the macro
            content = resolver.resolveMacros(element);
        } else if (xmlContent == null) {
            // no XML content- no output
            content = null;
        } else {

            // now get the content element value to display
            content = contentShowTagAction(xmlContent, element, locale, pageContext.getRequest());

            // make sure that no null String is returned
            if (content == null) {
                content = CmsMessages.formatUnknownKey(element);
            }
        }

        try {
            if (content != null) {
                pageContext.getOut().print(content);
            }
        } catch (IOException e) {
            if (LOG.isErrorEnabled()) {
                LOG.error(Messages.get().key(Messages.LOG_ERR_JSP_BEAN_0), e);
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
     * @see javax.servlet.jsp.tagext.Tag#release()
     */
    public void release() {

        super.release();
        m_element = null;
    }

    /**
     * Sets the name of the content node element to show.<p>
     * 
     * @param element the name of the content node element to show
     */
    public void setElement(String element) {

        m_element = element;
    }
}