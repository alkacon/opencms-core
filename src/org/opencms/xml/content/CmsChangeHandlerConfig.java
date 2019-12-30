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

package org.opencms.xml.content;

import org.opencms.main.CmsLog;

import org.apache.commons.logging.Log;

/**
 * A configuration bean representing a &lt;ChangeHandler&gt; element configured in an XSD's field settings.
 */
public class CmsChangeHandlerConfig {

    /** Logger instance for this class. */
    private static final Log LOG = CmsLog.getLog(CmsChangeHandlerConfig.class);

    /** Name of the field for which this is configured. */
    private String m_field;

    /** The class name for the change handler. */
    private String m_className;

    /** The configuration string for the change handler. */
    private String m_config;

    /**
     * Creates a new instance.
     *
     * @param field the field name
     * @param className the handler class name
     * @param config the configuration string
     */
    public CmsChangeHandlerConfig(String field, String className, String config) {

        m_field = field;
        m_className = className.trim();
        m_config = config;
    }

    /**
     * Gets the class name for the handler.
     *
     * @return the class name
     */
    public String getClassName() {

        return m_className;
    }

    /**
     * Gets the configuration string for the handler.
     *
     * @return the configuration string
     */
    public String getConfig() {

        return m_config;
    }

    /**
     * Gets the field name for which the handler is configured.
     *
     * @return the field name for which the handler is configured
     */
    public String getField() {

        return m_field;
    }

    /**
     * Creates a new handler instance using this configuration and the given scope.
     *
     * @param scope the scope
     * @return the new handler instance
     */
    public java.util.Optional<I_CmsXmlContentEditorChangeHandler> newHandler(String scope) {

        try {
            Class<?> cls = Class.forName(m_className, false, getClass().getClassLoader());
            if (I_CmsXmlContentEditorChangeHandler.class.isAssignableFrom(cls)) {
                I_CmsXmlContentEditorChangeHandler handler = (I_CmsXmlContentEditorChangeHandler)(cls.newInstance());
                handler.setConfiguration(m_config);
                handler.setScope(scope);
                return java.util.Optional.of(handler);
            } else {
                throw new Exception("Incompatible class for editor change handler: " + m_className);
            }
        } catch (Exception e) {
            LOG.error("Could not create editor change handler: " + e.getLocalizedMessage(), e);
            return java.util.Optional.empty();
        }
    }
}