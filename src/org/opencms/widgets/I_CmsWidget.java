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

package org.opencms.widgets;

import org.opencms.file.CmsObject;

import java.util.Map;

/**
 * Describes an editor widget for use in the OpenCms workplace.<p>
 *
 * @since 6.0.0
 */
public interface I_CmsWidget {

    /**
     * Returns the configuration string.<p>
     *
     * This can be used to enable / disable certain widget features that should not always be available,
     * or to pass specific initialization information to the widget.
     * It depends on the widget implementation on how this information is used.<p>
     *
     * @return the configuration string
     */
    String getConfiguration();

    /**
     * Generates the html to include at the end of the dialog for this widget.<p>
     *
     * The "help bubble" text div's are added using this method.<p>
     *
     * @param cms the current users OpenCms context
     * @param widgetDialog the dialog where the widget is used on
     * @param param the widget parameter to generate the widget for
     *
     * @return the html to include at the end of the dialog for this widget
     */
    String getDialogHtmlEnd(CmsObject cms, I_CmsWidgetDialog widgetDialog, I_CmsWidgetParameter param);

    /**
     * Generates the necessary JavaScript inclusion code for this widget.<p>
     *
     * @param cms the current users OpenCms context
     * @param widgetDialog the dialog where the widget is used on
     *
     * @return the JavaScript inclusion code
     */
    String getDialogIncludes(CmsObject cms, I_CmsWidgetDialog widgetDialog);

    /**
     * Generates the initialisation method JavaScript code for this widget.<p>
     *
     * @param cms the current users OpenCms context
     * @param widgetDialog the dialog where the widget is used on
     *
     * @return the initialisation method JavaScript code
     */
    String getDialogInitCall(CmsObject cms, I_CmsWidgetDialog widgetDialog);

    /**
     * Generates the initialization method JavaScript code for this widget.<p>
     *
     * @param cms an initialized instance of a CmsObject
     * @param widgetDialog the dialog where the widget is used on
     *
     * @return the initialization method JavaScript code
     */
    String getDialogInitMethod(CmsObject cms, I_CmsWidgetDialog widgetDialog);

    /**
     * Generates the widget HTML for the provided widget parameter.<p>
     *
     * @param cms an initialized instance of a CmsObject
     * @param widgetDialog the dialog where the widget is used on
     * @param param the widget parameter to generate the widget for
     *
     * @return the widget HTML for the provided widget parameter
     */
    String getDialogWidget(CmsObject cms, I_CmsWidgetDialog widgetDialog, I_CmsWidgetParameter param);

    /**
     * Creates the HTML code for the help bubble for this widget.<p>
     *
     * @param cms the current users OpenCms context
     * @param widgetDialog the dialog where the widget is used on
     * @param param the widget parameter to generate the widget for
     *
     * @return the HTML code for the help bubble for this widget
     */
    String getHelpBubble(CmsObject cms, I_CmsWidgetDialog widgetDialog, I_CmsWidgetParameter param);

    /**
     * Creates a HTML &lt;div&gt; containing the help text for this widget.<p>
     *
     * @param widgetDialog the dialog where the widget is used on
     * @param value the value to create the help bubble for
     *
     * @return a HTML &lt;div&gt; containing the help text for this widget
     */
    String getHelpText(I_CmsWidgetDialog widgetDialog, I_CmsWidgetParameter value);

    /**
     * Returns the <code>{@link I_CmsWidgetParameter#getStringValue(CmsObject)}</code>
     * processed according to the output rules of this widget and the given widget dialog.<p>
     *
     * @param cms the current users OpenCms context
     * @param widgetDialog the dialog where the widget is used on
     * @param param the widget parameter to generate the widget for
     *
     * @return the <code>{@link I_CmsWidgetParameter#getStringValue(CmsObject)}</code>
     *      processed according to the output rules of this widget
     *
     * @see I_CmsWidgetParameter#getStringValue(CmsObject)
     * @see org.opencms.xml.types.I_CmsXmlContentValue#getStringValue(CmsObject)
     */
    String getWidgetStringValue(CmsObject cms, I_CmsWidgetDialog widgetDialog, I_CmsWidgetParameter param);

    /**
     * Returns if the widget is compact view enabled.<p>
     *
     * @return <code>true</code> if the widget is compact view enabled
     */
    boolean isCompactViewEnabled();

    /**
     * Creates a duplicate of this widget instance.<p>
     *
     * @return a duplicate of this widget instance
     */
    I_CmsWidget newInstance();

    /**
     * Sets the configuration of this widget.<p>
     *
     * This can be used to enable / disable certain widget features that should not always be available,
     * or to pass specific initialization information to the widget.
     * It depends on the widget implementation on how this information is used.<p>
     *
     * @param configuration the configuration to set
     */
    void setConfiguration(String configuration);

    /**
     * Sets the value of in the given widget parameter by reading the "right"
     * value from the offered map of parameters.<p>
     *
     * @param cms the current users OpenCms context
     * @param formParameters the map of parameters to get the value from
     * @param widgetDialog the dialog where the widget is used on
     * @param param the widget parameter to generate the widget for
     */
    void setEditorValue(
        CmsObject cms,
        Map<String, String[]> formParameters,
        I_CmsWidgetDialog widgetDialog,
        I_CmsWidgetParameter param);
}