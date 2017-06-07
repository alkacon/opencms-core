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

package org.opencms.gwt.client.ui.input.form;

import org.opencms.gwt.client.ui.input.CmsTextBox;
import org.opencms.gwt.client.ui.input.I_CmsFormWidget;

import java.util.HashMap;
import java.util.Map;

/**
 * This singleton class is used for registering all widget factories.<p>
 *
 *  @since 8.0.0
 *
 */
public final class CmsWidgetFactoryRegistry implements I_CmsFormWidgetMultiFactory {

    /** The singleton instance. */
    private static CmsWidgetFactoryRegistry instance;

    /** The widget factories for each widget type string. */
    private Map<String, I_CmsFormWidgetFactory> m_widgetFactories = new HashMap<String, I_CmsFormWidgetFactory>();

    /**
     * The hidden default constructor.<p>
     */
    private CmsWidgetFactoryRegistry() {

        // do nothing
    }

    /**
     * Returns the singleton instance of this class.<p>
     *
     * @return the singleton instance of this class
     */
    public static CmsWidgetFactoryRegistry instance() {

        if (instance == null) {
            instance = new CmsWidgetFactoryRegistry();
        }
        return instance;
    }

    /**
     * Creates a new widget based on a type string and widget parameters.<p>
     *
     * @param key the type string
     * @param widgetParams the widget configuration parameters
     *
     * @return the newly created widget
     */
    public I_CmsFormWidget createFormWidget(String key, Map<String, String> widgetParams) {

        if (key == null) {
            key = "";
        }
        I_CmsFormWidgetFactory factory = m_widgetFactories.get(key);
        if (factory == null) {
            // if the given widget type is not found, fall back to text box widget
            factory = m_widgetFactories.get(CmsTextBox.WIDGET_TYPE);
        }
        return factory.createWidget(widgetParams);

    }

    /**
     * Registers a new widget factory for a given widget type key.<p>
     *
     * @param key the widget type key
     *
     * @param factory the new factory for the key
     */
    public void registerFactory(String key, I_CmsFormWidgetFactory factory) {

        m_widgetFactories.put(key, factory);
    }

}
