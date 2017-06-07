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

package org.opencms.ade.containerpage.shared;

import org.opencms.util.CmsStringUtil;
import org.opencms.xml.content.CmsXmlContentProperty;

import java.util.Map;
import java.util.Set;

import com.google.gwt.user.client.rpc.IsSerializable;

/**
 * Formatter configuration data.<p>
 */
public class CmsFormatterConfig implements IsSerializable {

    /** Key for the formatter configuration id setting. Append the container name to the key, to store container depending values. */
    public static final String FORMATTER_SETTINGS_KEY = "formatterSettings#";

    /** Id used for schema based formatters. */
    public static final String SCHEMA_FORMATTER_ID = "schema_formatter";

    /** The required css resources. */
    private Set<String> m_cssResources;

    /** The formatter configuration id. */
    private String m_id;

    /** The inline CSS. */
    private String m_inlineCss;

    /** The formatter root path. */
    private String m_jspRootPath;

    /** The formatter label. */
    private String m_label;

    /** The setting for this container element. */
    private Map<String, CmsXmlContentProperty> m_settingConfig;

    /**
     * Constructor.<p>
     *
     * @param id the formatter id
     */
    public CmsFormatterConfig(String id) {

        m_id = id;
    }

    /**
     * Constructor for serialization only.<p>
     */
    protected CmsFormatterConfig() {

        // nothing to to
    }

    /**
     * Returns the formatter configuration settings key for the given container name.<p>
     *
     * @param containerName the container name
     *
     * @return the settings key
     */
    public static String getSettingsKeyForContainer(String containerName) {

        return FORMATTER_SETTINGS_KEY + containerName;
    }

    /**
     * Returns the required CSS resources.<p>
     *
     * @return the CSS resources
     */
    public Set<String> getCssResources() {

        return m_cssResources;
    }

    /**
     * Returns the formatter configuration id.<p>
     *
     * @return the configuration id
     */
    public String getId() {

        return m_id;
    }

    /**
     * Returns the inline CSS.<p>
     *
     * @return the inline CSS
     */
    public String getInlineCss() {

        return m_inlineCss;
    }

    /**
     * Returns the formatter root path.<p>
     *
     * @return the formatter root path
     */
    public String getJspRootPath() {

        return m_jspRootPath;
    }

    /**
     * Returns the formatter label.<p>
     *
     * @return the label
     */
    public String getLabel() {

        return m_label;
    }

    /**
     * Returns the settings configuration.<p>
     *
     * @return the settings configuration
     */
    public Map<String, CmsXmlContentProperty> getSettingConfig() {

        return m_settingConfig;
    }

    /**
     * Returns if the formatter has inline CSS.<p>
     *
     * @return <code>true</code> if the formatter has inline CSS
     */
    public boolean hasInlineCss() {

        return CmsStringUtil.isNotEmptyOrWhitespaceOnly(m_inlineCss);
    }

    /**
     * Sets the required CSS resources.<p>
     *
     * @param cssResources the CSS resources
     */
    public void setCssResources(Set<String> cssResources) {

        m_cssResources = cssResources;
    }

    /**
     * Sets the inline CSS.<p>
     *
     * @param inlineCss the inline CSS
     */
    public void setInlineCss(String inlineCss) {

        m_inlineCss = inlineCss;
    }

    /**
     * Sets the formatter root path.<p>
     *
     * @param jspRootPath the formatter root path
     */
    public void setJspRootPath(String jspRootPath) {

        m_jspRootPath = jspRootPath;
    }

    /**
     * Sets the formatter label.<p>
     *
     * @param label the label
     */
    public void setLabel(String label) {

        m_label = label;
    }

    /**
     * Sets the settings configuration.<p>
     *
     * @param settingConfig the settings configuration
     */
    public void setSettingConfig(Map<String, CmsXmlContentProperty> settingConfig) {

        m_settingConfig = settingConfig;
    }
}
