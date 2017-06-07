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

import org.opencms.file.CmsObject;
import org.opencms.flex.CmsFlexController;
import org.opencms.i18n.CmsEncoder;
import org.opencms.main.CmsException;
import org.opencms.main.CmsLog;
import org.opencms.main.OpenCms;
import org.opencms.xml.containerpage.CmsContainerElementBean;

import java.util.Collections;
import java.util.Map;

import javax.servlet.ServletRequest;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.TagSupport;

import org.apache.commons.logging.Log;

/**
 * Provides access to the settings of an ADE container element.<p>
 *
 * @since 8.0
 */
public class CmsJspTagElementSetting extends TagSupport {

    /** The log object for this class. */
    private static final Log LOG = CmsLog.getLog(CmsJspTagElementSetting.class);

    /** Serial version UID required for safe serialization. */
    private static final long serialVersionUID = -7847101480288189549L;

    /** The default value. */
    private String m_defaultValue;

    /** The name of the element setting to read. */
    private String m_elementSetting;

    /** Indicates if HTML should be escaped. */
    private boolean m_escapeHtml;

    /**
     * Internal action method.<p>
     *
     * @param req the current request
     *
     * @return a map that contains the element settings
     */
    public static Map<String, String> elementSettingTagAction(ServletRequest req) {

        CmsFlexController controller = CmsFlexController.getController(req);

        CmsObject cms = controller.getCmsObject();
        // try to find element setting on the container element
        try {
            CmsContainerElementBean currentElement = OpenCms.getADEManager().getCurrentElement(req);
            currentElement.initResource(cms);
            return currentElement.getSettings();
        } catch (CmsException e) {
            // most likely we are not in a container page
            LOG.debug(e.getLocalizedMessage(), e);
            return Collections.emptyMap();
        }
    }

    /**
     * Internal action method.<p>
     *
     * @param setting the setting to look up
     * @param defaultValue the default value
     * @param escape if the result String should be HTML escaped or not
     * @param req the current request
     *
     * @return the value of the element setting or <code>null</code> if not found
     */
    public static String elementSettingTagAction(
        String setting,
        String defaultValue,
        boolean escape,
        ServletRequest req) {

        String value = elementSettingTagAction(req).get(setting);
        if (value == null) {
            value = defaultValue;
        }
        if (escape) {
            // HTML escape the value
            value = CmsEncoder.escapeHtml(value);
        }
        return value;
    }

    /**
     * @return SKIP_BODY
     * @see javax.servlet.jsp.tagext.Tag#doStartTag()
     */
    @Override
    public int doStartTag() throws JspException {

        ServletRequest req = pageContext.getRequest();

        // This will always be true if the page is called through OpenCms
        if (CmsFlexController.isCmsRequest(req)) {

            try {
                String setting = elementSettingTagAction(getName(), m_defaultValue, m_escapeHtml, req);
                // Make sure that no null String is returned
                if (setting == null) {
                    setting = "";
                }
                pageContext.getOut().print(setting);

            } catch (Exception ex) {
                if (LOG.isErrorEnabled()) {
                    LOG.error(Messages.get().getBundle().key(Messages.ERR_PROCESS_TAG_1, "elementSetting"), ex);
                }
                throw new javax.servlet.jsp.JspException(ex);
            }
        }
        return SKIP_BODY;
    }

    /**
     * Returns the default value.<p>
     *
     * @return the default value
     */
    public String getDefault() {

        return m_defaultValue != null ? m_defaultValue : "";
    }

    /**
     * The value of the escape HTML flag.<p>
     *
     * @return the value of the escape HTML flag
     */
    public String getEscapeHtml() {

        return String.valueOf(m_escapeHtml);
    }

    /**
     * Returns the selected element setting name.<p>
     *
     * @return the selected element setting name
     */
    public String getName() {

        return m_elementSetting != null ? m_elementSetting : "";
    }

    /**
     * @see javax.servlet.jsp.tagext.Tag#release()
     */
    @Override
    public void release() {

        super.release();
        m_elementSetting = null;
        m_defaultValue = null;
        m_escapeHtml = false;
    }

    /**
     * Sets the default value.<p>
     *
     * This is used if a selected element setting is not found.<p>
     *
     * @param def the default value
     */
    public void setDefault(String def) {

        if (def != null) {
            m_defaultValue = def;
        }
    }

    /**
     * Set the escape HTML flag.<p>
     *
     * @param value must be <code>"true"</code> or <code>"false"</code> (all values other then <code>"true"</code> are
     * considered to be false)
     */
    public void setEscapeHtml(String value) {

        if (value != null) {
            m_escapeHtml = Boolean.valueOf(value.trim()).booleanValue();
        } else {
            m_escapeHtml = false;
        }
    }

    /**
     * Sets the element setting name.<p>
     *
     * @param name the element setting name to set
     */
    public void setName(String name) {

        if (name != null) {
            m_elementSetting = name;
        }
    }
}
