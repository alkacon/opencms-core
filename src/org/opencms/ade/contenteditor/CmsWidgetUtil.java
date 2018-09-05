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
import org.opencms.widgets.I_CmsWidget;
import org.opencms.xml.CmsXmlContentDefinition;
import org.opencms.xml.CmsXmlUtils;
import org.opencms.xml.content.I_CmsXmlContentHandler;
import org.opencms.xml.content.I_CmsXmlContentHandler.DisplayType;
import org.opencms.xml.types.I_CmsXmlContentValue;
import org.opencms.xml.types.I_CmsXmlSchemaType;

import java.util.ArrayList;
import java.util.List;

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

        String widgetConfig;
        DisplayType configuredType = DisplayType.none;

        I_CmsWidget widget = null;
        final List<I_CmsWidget> configuredWidgets = new ArrayList<>();
        final List<String> configuredWidgetConfigs = new ArrayList<>();
        final List<DisplayType> configuredDisplayTypes = new ArrayList<>();
        final List<I_CmsXmlSchemaType> schemaTypes = new ArrayList<>();
        rootContentDefinition.findSchemaTypesForPath(path, (nestedType, remainingPath) -> {
            schemaTypes.add(nestedType);
            remainingPath = CmsXmlUtils.concatXpath(nestedType.getName(), remainingPath);
            I_CmsXmlContentHandler handler = nestedType.getContentDefinition().getContentHandler();
            I_CmsWidget confWidget = handler.getUnconfiguredWidget(remainingPath);
            String config = handler.getConfiguration(remainingPath);

            DisplayType type = handler.getConfiguredDisplayType(remainingPath, null);
            if (confWidget != null) {
                configuredWidgets.add(confWidget);
            }
            if (config != null) {
                configuredWidgetConfigs.add(config);
            }
            if (type != null) {
                configuredDisplayTypes.add(type);
            }
        });
        if (!configuredWidgets.isEmpty()) {
            widget = configuredWidgets.get(0).newInstance();
        } else {
            widget = OpenCms.getXmlContentTypeManager().getWidgetDefault(
                schemaTypes.get(schemaTypes.size() - 1).getTypeName());
        }
        if (!configuredDisplayTypes.isEmpty()) {
            configuredType = configuredDisplayTypes.get(0);
        }
        if (!configuredWidgetConfigs.isEmpty()) {
            widgetConfig = configuredWidgetConfigs.get(0);
        } else {
            widgetConfig = OpenCms.getXmlContentTypeManager().getWidgetDefaultConfiguration(widget);
        }
        widget.setConfiguration(widgetConfig);
        WidgetInfo result = new WidgetInfo();
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
