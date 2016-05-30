/*
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (C) Alkacon Software (http://www.alkacon.com)
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

package org.opencms.acacia.shared;

import com.google.gwt.user.client.rpc.IsSerializable;

/**
 * The attribute configuration. Stating the attribute label, help, widget name and widget configuration.<p>
 */
public class CmsAttributeConfiguration implements IsSerializable {

    /** The attribute default value. */
    private String m_defaultValue;

    /** The widget display type. */
    private String m_displayType;

    /** The attribute help information. */
    private String m_help;

    /** The attribute label. */
    private String m_label;

    /** States if the attribute should be synchronized across all locales. */
    private boolean m_localeSynchronized;

    /** States if the attribute is loaded dynamically. */
    private boolean m_dynamicallyLoaded;

    /** The visibility flag. */
    private boolean m_visible;

    /** The widget configuration. */
    private String m_widgetConfig;

    /** The widget name. */
    private String m_widgetName;

    /**
     * Constructor.<p>
     *
     * @param label the attribute label
     * @param help the attribute help information
     * @param widgetName the widget name
     * @param widgetConfig the widget configuration
     * @param defaultValue the attribute default value
     * @param displayType the display type
     * @param visible if the attribute should be visible in the editor
     * @param localSynchronized if the attribute should be synchronized across all locales
     * @param dynamicallyLoaded if the attribute should be loaded dynamically
     */
    public CmsAttributeConfiguration(
        String label,
        String help,
        String widgetName,
        String widgetConfig,
        String defaultValue,
        String displayType,
        boolean visible,
        boolean localSynchronized,
        boolean dynamicallyLoaded) {

        m_label = label;
        m_help = help;
        m_widgetName = widgetName;
        m_widgetConfig = widgetConfig;
        m_defaultValue = defaultValue;
        m_displayType = displayType;
        m_visible = visible;
        m_localeSynchronized = localSynchronized;
        m_dynamicallyLoaded = dynamicallyLoaded;
    }

    /**
     * Constructor. Used for serialization only.<p>
     */
    protected CmsAttributeConfiguration() {

        // nothing to do
    }

    /**
     * Returns the default value.<p>
     *
     * @return the default value
     */
    public String getDefaultValue() {

        return m_defaultValue;
    }

    /**
     * Returns the widget display type.<p>
     *
     * @return the widget display type
     */
    public String getDisplayType() {

        return m_displayType;
    }

    /**
     * Returns the attribute help information.<p>
     *
     * @return the attribute help information
     */
    public String getHelp() {

        return m_help;
    }

    /**
     * Returns the attribute label.<p>
     *
     * @return the attribute label
     */
    public String getLabel() {

        return m_label;
    }

    /**
     * Returns the widget configuration.<p>
     *
     * @return the widget configuration
     */
    public String getWidgetConfig() {

        return m_widgetConfig;
    }

    /**
     * Returns the widget name.<p>
     *
     * @return the widget name
     */
    public String getWidgetName() {

        return m_widgetName;
    }

    /**
     * Returns the if the widget should be displayed in compact view.<p>
     *
     * @return <code>true</code> if the widget should be displayed in compact view
     */
    public boolean isDisplayColumn() {

        return "column".equals(m_displayType);
    }

    /**
     * Returns <code>true</code> if the widget should be displayed in single line view.<p>
     *
     * @return <code>true</code> if the widget should be displayed in single line view
     */
    public boolean isDisplaySingleLine() {

        return "singleline".equals(m_displayType);
    }

    /**
     * Returns <code>true</code> if the attribute is set dynamically and not from the XML content.<p>
     *
     * @return <code>true</code> if the attribute is set dynamically and not from the XML content
     */
    public boolean isDynamicallyLoaded() {

        return m_dynamicallyLoaded;
    }

    /**
     * Returns if the attribute should be synchronized across all locales.<p>
     *
     * @return <code>true</code> if the attribute should be synchronized across all locales
     */
    public boolean isLocaleSynchronized() {

        return m_localeSynchronized;
    }

    /**
     * Returns if the given attribute should be visible in the editor.<p>
     *
     * @return <code>true</code> if the given attribute should be visible in the editor
     */
    public boolean isVisible() {

        return m_visible;
    }

    /**
     * Sets the widget display type.<p>
     *
     * @param displayType the widget display type
     */
    public void setDisplayType(String displayType) {

        m_displayType = displayType;
    }

}
