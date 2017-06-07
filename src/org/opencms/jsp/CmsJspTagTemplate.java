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

import org.opencms.flex.CmsFlexController;
import org.opencms.loader.I_CmsResourceLoader;
import org.opencms.main.CmsException;
import org.opencms.main.CmsLog;
import org.opencms.main.OpenCms;
import org.opencms.util.CmsStringUtil;
import org.opencms.xml.I_CmsXmlDocument;
import org.opencms.xml.page.CmsXmlPageFactory;

import java.util.List;
import java.util.Locale;

import javax.servlet.ServletRequest;
import javax.servlet.jsp.tagext.BodyTagSupport;

import org.apache.commons.logging.Log;

/**
 * Used to select various template elements form a JSP template that
 * is included in another file.<p>
 *
 * @since 6.0.0
 */
public class CmsJspTagTemplate extends BodyTagSupport {

    /** Serial version UID required for safe serialization. */
    private static final long serialVersionUID = -3773247710025810438L;

    /** The log object for this class. */
    private static final Log LOG = CmsLog.getLog(CmsJspTagTemplate.class);

    /** Condition for element check. */
    private boolean m_checkall;

    /** Condition for negative element check. */
    private boolean m_checknone;

    /** Name of element. */
    private String m_element;

    /** List of elements for element check. */
    private String m_elementlist;

    /**
     * Internal action method.<p>
     *
     * @param element the selected element
     * @param elementlist list the list of elements to check
     * @param checkall flag to indicate that all elements should be checked
     * @param checknone flag to indicate that the check is done for nonexisting elements
     * @param req the current request
     * @return boolean <code>true</code> if this element should be inclued, <code>false</code>
     * otherwise
     */
    public static boolean templateTagAction(
        String element,
        String elementlist,
        boolean checkall,
        boolean checknone,
        ServletRequest req) {

        if (elementlist != null) {

            CmsFlexController controller = CmsFlexController.getController(req);
            String filename = controller.getCmsObject().getRequestContext().getUri();

            I_CmsXmlDocument content = null;
            try {
                content = CmsXmlPageFactory.unmarshal(controller.getCmsObject(), filename, req);
            } catch (CmsException e) {
                LOG.error(Messages.get().getBundle().key(Messages.ERR_XML_DOCUMENT_UNMARSHAL_1, filename), e);
            }

            if (content != null) {
                String absolutePath = controller.getCmsObject().getSitePath(content.getFile());
                // check the elements in the elementlist, if the check fails don't render the element
                String[] elements = CmsStringUtil.splitAsArray(elementlist, ',');
                boolean found = false;
                for (int i = 0; i < elements.length; i++) {
                    String el = elements[i].trim();
                    List<Locale> locales = content.getLocales(el);
                    Locale locale = null;
                    if ((locales != null) && (locales.size() != 0)) {
                        locale = OpenCms.getLocaleManager().getBestMatchingLocale(
                            controller.getCmsObject().getRequestContext().getLocale(),
                            OpenCms.getLocaleManager().getDefaultLocales(controller.getCmsObject(), absolutePath),
                            locales);
                    }
                    if ((locale != null) && content.hasValue(el, locale) && content.isEnabled(el, locale)) {

                        found = true;
                        if (!checkall) {
                            // found at least an element that is available
                            break;
                        }
                    } else {
                        if (checkall) {
                            // found at least an element that is not available
                            return false;
                        }
                    }
                }

                if (!found && !checknone) {
                    // no element found while checking for existing elements
                    return false;
                } else if (found && checknone) {
                    // element found while checking for nonexisting elements
                    return false;
                }
            }
        }

        // otherwise, check if an element was defined and if its equal to the desired element
        String param = req.getParameter(I_CmsResourceLoader.PARAMETER_ELEMENT);
        return ((element == null) || (param == null) || (param.equals(element)));
    }

    /**
     * @see javax.servlet.jsp.tagext.Tag#doStartTag()
     */
    @Override
    public int doStartTag() {

        if (templateTagAction(m_element, m_elementlist, m_checkall, m_checknone, pageContext.getRequest())) {
            return EVAL_BODY_INCLUDE;
        } else {
            return SKIP_BODY;
        }
    }

    /**
     * Returns the selected element.<p>
     *
     * @return the selected element
     */
    public String getElement() {

        return m_element != null ? m_element : "";
    }

    /**
     * Returns the list of elements to check.<p>
     *
     * @return the list of elements
     */
    public String getIfexists() {

        return m_elementlist != null ? m_elementlist : "";
    }

    /**
     * Returns the list of elements to check.<p>
     *
     * @return the list of elements
     */
    public String getIfexistsall() {

        return m_elementlist != null ? m_elementlist : "";
    }

    /**
     * Returns the list of elements to check.<p>
     *
     * @return the list of elements
     */
    public String getIfexistsnone() {

        return m_elementlist != null ? m_elementlist : "";
    }

    /**
     * Returns the list of elements to check.<p>
     *
     * @return the list of elements
     */
    public String getIfexistsone() {

        return m_elementlist != null ? m_elementlist : "";
    }

    /**
     * @see javax.servlet.jsp.tagext.Tag#release()
     */
    @Override
    public void release() {

        super.release();
        m_element = null;
    }

    /**
     * Sets the element target.<p>
     *
     * @param element the target to set
     */
    public void setElement(String element) {

        if (element != null) {
            m_element = element.toLowerCase();
        }
    }

    /**
     * Sets the list of elements to check.<p>
     *
     * @param elements the list of elements
     */
    public void setIfexists(String elements) {

        if (elements != null) {
            m_elementlist = elements;
            m_checkall = false;
            m_checknone = false;
        }
    }

    /**
     * Sets the list of elements to check.<p>
     *
     * @param elements the list of elements
     */
    public void setIfexistsall(String elements) {

        if (elements != null) {
            m_elementlist = elements;
            m_checkall = true;
            m_checknone = false;
        }
    }

    /**
     * Sets the list of elements to check.<p>
     *
     * @param elements the list of elements
     */
    public void setIfexistsnone(String elements) {

        if (elements != null) {
            m_elementlist = elements;
            m_checkall = false;
            m_checknone = true;
        }
    }

    /**
     * Sets the list of elements to check.<p>
     *
     * @param elements the list of elements
     */
    public void setIfexistsone(String elements) {

        if (elements != null) {
            m_elementlist = elements;
            m_checkall = false;
            m_checknone = false;
        }
    }
}