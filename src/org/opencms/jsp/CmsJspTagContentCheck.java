/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/jsp/CmsJspTagContentCheck.java,v $
 * Date   : $Date: 2004/10/15 12:22:00 $
 * Version: $Revision: 1.1 $
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

import org.opencms.main.OpenCms;
import org.opencms.util.CmsStringUtil;
import org.opencms.xml.A_CmsXmlDocument;

import java.util.Locale;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspTagException;
import javax.servlet.jsp.tagext.Tag;
import javax.servlet.jsp.tagext.TagSupport;

/**
 * Used to check the availablity of an XML content item for conditional display.<p>
 * 
 * @author  Alexander Kandzior (a.kandzior@alkacon.com)
 * 
 * @version $Revision: 1.1 $
 * @since 5.5.0
 */
public class CmsJspTagContentCheck extends TagSupport {

    /** Condition for element check. */
    private boolean m_checkall;

    /** Condition for negative element check. */
    private boolean m_checknone;

    /** The list of element to check. */
    private String m_elementList;

    /**
     * Internal action method to check the elements from the provided XML content item.<p>
     * 
     * @param elementList the list of elements to check for
     * @param checkall flag to indicate that all elements should be checked
     * @param checknone flag to indicate that the check is done for nonexisting elements
     * @param content the XML content document to check the elements from
     * @param locale the locale to check the element for
     * 
     * @return true if the test succeeds, false if the test fails
     */
    public static boolean contentCheckTagAction(
        String elementList,
        boolean checkall,
        boolean checknone,
        A_CmsXmlDocument content,
        Locale locale) {

        boolean result = false;
        String elements[] = CmsStringUtil.split(elementList, ",");
        for (int i = (elements.length - 1); i >= 0; i--) {
            String element = elements[i].trim();
            int index = 0;
            if (element.charAt(element.length() - 1) == ']') {
                // parse element names in the form: Teaser[1]
                int pos = element.lastIndexOf('[');
                if (pos > 0) {
                    String num = element.substring(pos + 1, element.length() - 1);
                    try {
                        index = Integer.valueOf(num).intValue();
                        element = element.substring(0, pos);
                    } catch (Exception e) {
                        // ignore, index will be 0, element name will stay
                        if (OpenCms.getLog(CmsJspTagContentCheck.class).isDebugEnabled()) {
                            OpenCms.getLog(CmsJspTagContentCheck.class).debug(
                                "Invalid content check element expression in list: " + elements[i], e);
                        }
                    }
                }

                result = content.hasValue(element, locale, index);
                if (result && checknone) {
                    // found an item that must not exist
                    return false;
                }
                if (result && !checkall && !checknone) {
                    // we need to find only one item
                    return true;
                }
            }
        }

        return result;
    }

    /**
     * @see javax.servlet.jsp.tagext.Tag#doStartTag()
     */
    public int doStartTag() throws JspException {

        // get a reference to the parent "content load" class
        Tag ancestor = findAncestorWithClass(this, I_CmsJspTagContentContainer.class);
        if (ancestor == null) {
            throw new JspTagException("Tag <contentcheck> without required parent tag <contentload> found!");
        }
        I_CmsJspTagContentContainer contentContainer = (I_CmsJspTagContentContainer)ancestor;

        // get loaded content from parent <contentload> tag
        A_CmsXmlDocument xmlContent = contentContainer.getXmlDocument();
        Locale locale = contentContainer.getXmlDocumentLocale();

        if (contentCheckTagAction(m_elementList, m_checkall, m_checknone, xmlContent, locale)) {
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
     * @see javax.servlet.jsp.tagext.Tag#release()
     */
    public void release() {

        super.release();
        m_checkall = false;
        m_checknone = false;
        m_elementList = null;
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
}