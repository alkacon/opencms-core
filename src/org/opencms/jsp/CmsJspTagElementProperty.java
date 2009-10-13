/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/jsp/Attic/CmsJspTagElementProperty.java,v $
 * Date   : $Date: 2009/10/13 11:59:45 $
 * Version: $Revision: 1.1.2.2 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (C) 2002 - 2009 Alkacon Software (http://www.alkacon.com)
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

import org.opencms.file.CmsProperty;
import org.opencms.flex.CmsFlexController;
import org.opencms.main.CmsException;
import org.opencms.main.CmsLog;
import org.opencms.util.CmsStringUtil;
import org.opencms.xml.containerpage.CmsADEManager;
import org.opencms.xml.containerpage.I_CmsContainerElementBean;

import java.io.IOException;

import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.PageContext;
import javax.servlet.jsp.tagext.TagSupport;

import org.apache.commons.logging.Log;

/**
 * Provides access to the element properties in a container-page.<p>
 * 
 * @author Tobias Herrmann
 * 
 * @version $Revision: 1.1.2.2 $
 * 
 * @since 7.6
 */
public class CmsJspTagElementProperty extends TagSupport {

    private static final long serialVersionUID = 1L;

    /** The log object for this class. */
    private static final Log LOG = CmsLog.getLog(CmsJspTagElementProperty.class);

    /** The property attribute value. */
    private String m_property;

    /** The default attribute value. */
    private String m_default;

    /**
     * Internal action method.<p>
     * 
     * @param pageContext the current JSP page context
     * @param propertyName the name of the property
     * @param defaultValue the default value of the property
     * @param req the current request
     * @param res the current response
     * 
     * @throws IOException 
     */
    public static void elementPropertyTagAction(
        PageContext pageContext,
        String propertyName,
        String defaultValue,
        ServletRequest req,
        ServletResponse res) throws IOException {

        String propValue = "";
        // get the element
        I_CmsContainerElementBean element = null;
        try {
            element = CmsADEManager.getCurrentElement(req);
        } catch (CmsException e) {
            LOG.warn(e);
        }
        if (!CmsStringUtil.isEmptyOrWhitespaceOnly(defaultValue)) {
            propValue = defaultValue;
        }
        if ((element != null) && element.getProperties().containsKey(propertyName)) {
            CmsProperty property = element.getProperties().get(propertyName);
            if (property != null) {
                propValue = property.getValue(defaultValue);
            }
        }
        pageContext.getOut().print(propValue);
    }

    /**
     * @return SKIP_BODY
     * @throws JspException in case something goes wrong
     * @see javax.servlet.jsp.tagext.Tag#doStartTag()
     */
    @Override
    public int doStartTag() throws JspException {

        ServletRequest req = pageContext.getRequest();
        ServletResponse res = pageContext.getResponse();

        // This will always be true if the page is called through OpenCms 
        if (CmsFlexController.isCmsRequest(req)) {

            try {
                elementPropertyTagAction(pageContext, getProperty(), getDefault(), req, res);
            } catch (Exception ex) {
                if (LOG.isErrorEnabled()) {
                    LOG.error(Messages.get().getBundle().key(Messages.ERR_PROCESS_TAG_1, "elementProperty"), ex);
                }
                throw new javax.servlet.jsp.JspException(ex);
            }
        }
        return SKIP_BODY;
    }

    /**
     * Returns the property.<p>
     *
     * @return the property
     */
    public String getProperty() {

        return m_property;
    }

    /**
     * Sets the property.<p>
     *
     * @param property the property to set
     */
    public void setProperty(String property) {

        m_property = property;
    }

    /**
     * Returns the default.<p>
     *
     * @return the default
     */
    public String getDefault() {

        return m_default;
    }

    /**
     * Sets the default.<p>
     *
     * @param default1 the default to set
     */
    public void setDefault(String default1) {

        m_default = default1;
    }

}
