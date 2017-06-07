/*
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (c) Alkacon Software GmbH & Co. KG (http://www.alkacon.com)
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
 * For further information about Alkacon Software GmbH & Co. KG, please see the
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

import org.opencms.i18n.CmsLocaleManager;
import org.opencms.i18n.CmsMessageContainer;
import org.opencms.main.OpenCms;
import org.opencms.util.CmsStringUtil;
import org.opencms.xml.CmsXmlUtils;
import org.opencms.xml.I_CmsXmlDocument;

import java.util.Locale;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspTagException;
import javax.servlet.jsp.tagext.Tag;
import javax.servlet.jsp.tagext.TagSupport;

/**
 * Used to check the availablity of an XML content item for conditional display.<p>
 *
 * @since 6.0.0
 */
public class CmsJspTagContentCheck extends TagSupport {

    /** Serial version UID required for safe serialization. */
    private static final long serialVersionUID = -5996702196453747182L;

    /** Condition for element check. */
    private boolean m_checkall;

    /** Condition for negative element check. */
    private boolean m_checknone;

    /** The list of element to check. */
    private String m_elementList;

    /** The locale to check for. */
    private Locale m_locale;

    /**
     * Internal action method to check the elements from the provided XML content item.<p>
     *
     * @param elementList the list of elements to check for
     * @param prefix the Xpath prefix to append the elements to (in case of nested schemas)
     * @param checkall flag to indicate that all elements should be checked
     * @param checknone flag to indicate that the check is done for nonexisting elements
     * @param content the XML content document to check the elements from
     * @param locale the locale to check the element for
     *
     * @return true if the test succeeds, false if the test fails
     */
    public static boolean contentCheckTagAction(
        String elementList,
        String prefix,
        boolean checkall,
        boolean checknone,
        I_CmsXmlDocument content,
        Locale locale) {

        boolean found = false;
        String[] elements = CmsStringUtil.splitAsArray(elementList, ',');
        for (int i = (elements.length - 1); i >= 0; i--) {

            String element = CmsXmlUtils.concatXpath(prefix, elements[i].trim());
            found = found || content.hasValue(element, locale);

            if (found && checknone) {
                // found an item that must not exist
                return false;
            }
            if (found && !checkall && !checknone) {
                // we need to find only one item
                return true;
            }
        }

        if (!found && checknone) {
            // no item found as expected
            return true;
        }

        return found;
    }

    /**
     * @see javax.servlet.jsp.tagext.Tag#doEndTag()
     */
    @Override
    public int doEndTag() {

        if (OpenCms.getSystemInfo().getServletContainerSettings().isReleaseTagsAfterEnd()) {
            // need to release manually, JSP container may not call release as required (happens with Tomcat)
            release();
        }
        return EVAL_PAGE;
    }

    /**
     * @see javax.servlet.jsp.tagext.Tag#doStartTag()
     */
    @Override
    public int doStartTag() throws JspException {

        // get a reference to the parent "content load" class
        Tag ancestor = findAncestorWithClass(this, I_CmsXmlContentContainer.class);
        if (ancestor == null) {
            CmsMessageContainer errMsgContainer = Messages.get().container(
                Messages.ERR_TAG_CONTENTCHECK_WRONG_PARENT_0);
            String msg = Messages.getLocalizedMessage(errMsgContainer, pageContext);
            throw new JspTagException(msg);
        }
        I_CmsXmlContentContainer contentContainer = (I_CmsXmlContentContainer)ancestor;
        String prefix = contentContainer.getXmlDocumentElement();

        // get loaded content from parent <contentload> tag
        I_CmsXmlDocument content = contentContainer.getXmlDocument();

        if (m_locale == null) {
            m_locale = contentContainer.getXmlDocumentLocale();
        }

        // calculate the result
        boolean result = contentCheckTagAction(m_elementList, prefix, m_checkall, m_checknone, content, m_locale);

        if (result) {
            return EVAL_BODY_INCLUDE;
        } else {
            return SKIP_BODY;
        }
    }

    /**
     * Returns the list of elements to check.<p>
     *
     * @return the list of elements to check
     */
    public String getIfexists() {

        return m_elementList != null ? m_elementList : "";
    }

    /**
     * Returns the list of elements to check.<p>
     *
     * @return the list of elements to check
     */
    public String getIfexistsall() {

        return m_elementList != null ? m_elementList : "";
    }

    /**
     * Returns the list of elements to check.<p>
     *
     * @return the list of elements to check
     */
    public String getIfexistsnone() {

        return m_elementList != null ? m_elementList : "";
    }

    /**
     * Returns the list of elements to check.<p>
     *
     * @return the list of elements to check
     */
    public String getIfexistsone() {

        return m_elementList != null ? m_elementList : "";
    }

    /**
     * Returns the locale used for checking.<p>
     *
     * @return the locale used for checking
     */
    public String getLocale() {

        return (m_locale != null) ? m_locale.toString() : "";
    }

    /**
     * @see javax.servlet.jsp.tagext.Tag#release()
     */
    @Override
    public void release() {

        m_checkall = false;
        m_checknone = false;
        m_elementList = null;
        m_locale = null;
        super.release();
    }

    /**
     * Sets the list of elements to check for.<p>
     *
     * @param elementList the list of elements to check for
     */
    public void setIfexists(String elementList) {

        if (elementList != null) {
            m_elementList = elementList;
            m_checkall = false;
            m_checknone = false;
        }
    }

    /**
     * Sets the list of elements to check for.<p>
     *
     * @param elementList the list of elements to check for
     */
    public void setIfexistsall(String elementList) {

        if (elementList != null) {
            m_elementList = elementList;
            m_checkall = true;
            m_checknone = false;
        }
    }

    /**
     * Sets the list of elements to check for.<p>
     *
     * @param elementList the list of elements to check for
     */
    public void setIfexistsnone(String elementList) {

        if (elementList != null) {
            m_elementList = elementList;
            m_checkall = false;
            m_checknone = true;
        }
    }

    /**
     * Sets the list of elements to check for.<p>
     *
     * @param elementList the list of elements to check for
     */
    public void setIfexistsone(String elementList) {

        if (elementList != null) {
            m_elementList = elementList;
            m_checkall = false;
            m_checknone = false;
        }
    }

    /**
     * Sets the locale used for checking.<p>
     *
     * @param locale the locale to set
     */
    public void setLocale(String locale) {

        if (CmsStringUtil.isEmpty(locale)) {
            m_locale = null;
        } else {
            m_locale = CmsLocaleManager.getLocale(locale);
        }
    }
}