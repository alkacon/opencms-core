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

package org.opencms.ade.contenteditor;

import org.opencms.main.OpenCms;
import org.opencms.widgets.I_CmsComplexWidget;
import org.opencms.widgets.I_CmsWidget;
import org.opencms.xml.CmsXmlContentDefinition;
import org.opencms.xml.CmsXmlUtils;
import org.opencms.xml.content.I_CmsXmlContentHandler;
import org.opencms.xml.content.I_CmsXmlContentHandler.DisplayType;
import org.opencms.xml.types.I_CmsXmlContentValue;
import org.opencms.xml.types.I_CmsXmlSchemaType;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.collections4.CollectionUtils;

/**
 * Utility methods for getting widget informations out of content definitions.<p>
 */
public final class CmsWidgetUtil {

    /**
     * Bean representing widget information.<p>
     */
    public static class WidgetInfo {

        /** The display type. */
        private DisplayType m_displayType;

        /** A widget instance. */
        private I_CmsWidget m_widget;

        /** The complex widget. */
        private I_CmsComplexWidget m_complexWidget;

        /**
         * Gets the complex widget.<p>
         *
         * @return the complex widget
         */
        public I_CmsComplexWidget getComplexWidget() {

            return m_complexWidget;
        }

        /**
         * Gets the display type.<p>
         *
         * @return the display type
         */
        public DisplayType getDisplayType() {

            return m_displayType;
        }

        /**
         * Gets the widget instance.<p>
         *
         * @return the widget instance
         */
        public I_CmsWidget getWidget() {

            return m_widget;
        }

        /**
         * Sets the complex widget.<p>
         *
         * @param complexWidget the complex widget to set
         */
        public void setComplexWidget(I_CmsComplexWidget complexWidget) {

            m_complexWidget = complexWidget;

        }

        /**
         * Sets the display type.<p>
         *
         * @param displayType the display type
         */
        public void setDisplayType(DisplayType displayType) {

            m_displayType = displayType;
        }

        /**
         * Sets the widget.<p>
         *
         * @param widget the widget
         */
        public void setWidget(I_CmsWidget widget) {

            m_widget = widget;
        }
    }

    /**
     * Hidden default constructor.
     */
    private CmsWidgetUtil() {
        // hidden default constructor
    }

    /**
     * Collects widget information for a given content definition and content value path.<p>
     *
     * @param rootContentDefinition the content definition
     * @param path the path relative to the given content definition
     *
     * @return the widget information for the given path
     */
    public static WidgetInfo collectWidgetInfo(CmsXmlContentDefinition rootContentDefinition, String path) {

        String widgetConfig = null;
        DisplayType configuredType = DisplayType.none;
        I_CmsXmlSchemaType schemaType = rootContentDefinition.getSchemaType(path);

        I_CmsWidget widget = null;
        I_CmsComplexWidget complexWidget = null;
        I_CmsXmlContentHandler contentHandler = schemaType.getContentDefinition().getContentHandler();
        final List<I_CmsWidget> configuredWidgets = new ArrayList<>();
        final List<String> configuredWidgetConfigs = new ArrayList<>();
        final List<DisplayType> configuredDisplayTypes = new ArrayList<>();
        final List<I_CmsComplexWidget> configuredComplexWidgets = new ArrayList<>();

        // Use lists to store found widget configurations, and then use the first elements of each list.
        // Because we iterate from the top level schema down to the nested schema, configurations in higher level schemas
        // will have precedence over those in lower level schemas for the same element.

        rootContentDefinition.findSchemaTypesForPath(path, (nestedType, remainingPath) -> {
            remainingPath = CmsXmlUtils.concatXpath(nestedType.getName(), remainingPath);
            I_CmsXmlContentHandler handler = nestedType.getContentDefinition().getContentHandler();
            CollectionUtils.addIgnoreNull(configuredWidgets, handler.getUnconfiguredWidget(remainingPath));
            CollectionUtils.addIgnoreNull(configuredWidgetConfigs, handler.getConfiguration(remainingPath));
            CollectionUtils.addIgnoreNull(
                configuredDisplayTypes,
                handler.getConfiguredDisplayType(remainingPath, null));
            CollectionUtils.addIgnoreNull(
                configuredComplexWidgets,
                handler.getUnconfiguredComplexWidget(remainingPath));

        });
        if (!configuredWidgets.isEmpty()) {
            widget = configuredWidgets.get(0).newInstance();
        } else {
            widget = OpenCms.getXmlContentTypeManager().getWidgetDefault(schemaType.getTypeName());
        }
        if (!configuredDisplayTypes.isEmpty()) {
            configuredType = configuredDisplayTypes.get(0);
        }
        if (!configuredWidgetConfigs.isEmpty()) {
            widgetConfig = configuredWidgetConfigs.get(0);
        } else if (widget != null) {
            widgetConfig = OpenCms.getXmlContentTypeManager().getWidgetDefaultConfiguration(widget);
        }
        if (widget != null) {
            widget.setConfiguration(widgetConfig);
        }
        // default complex widget and default c. widget config have lower priorities than those directly defined, so put them at the end of the list
        CollectionUtils.addIgnoreNull(configuredComplexWidgets, contentHandler.getDefaultComplexWidget());
        List<String> complexWidgetConfigs = new ArrayList<>(configuredWidgetConfigs);
        CollectionUtils.addIgnoreNull(complexWidgetConfigs, contentHandler.getDefaultComplexWidgetConfiguration());
        if (!configuredComplexWidgets.isEmpty()) {
            String config = "";
            if (!complexWidgetConfigs.isEmpty()) {
                config = complexWidgetConfigs.get(0);
            }
            complexWidget = configuredComplexWidgets.get(0).configure(config);
        }
        WidgetInfo result = new WidgetInfo();
        result.setComplexWidget(complexWidget);
        result.setDisplayType(configuredType);
        result.setWidget(widget);
        return result;
    }

    /**
     * Collects widget information for a given content value.<p>
     *
     * @param value a content value
     *
     * @return the widget information for the given value
     */

    public static WidgetInfo collectWidgetInfo(I_CmsXmlContentValue value) {

        CmsXmlContentDefinition contentDef = value.getDocument().getContentDefinition();
        String path = value.getPath();
        return collectWidgetInfo(contentDef, path);
    }

}
