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
import org.opencms.file.CmsResource;
import org.opencms.gwt.shared.CmsGwtConstants;
import org.opencms.i18n.CmsEncoder;
import org.opencms.i18n.CmsMessages;
import org.opencms.json.JSONObject;
import org.opencms.main.CmsLog;
import org.opencms.xml.content.I_CmsXmlContentHandler.DisplayType;
import org.opencms.xml.types.A_CmsXmlContentValue;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import org.apache.commons.logging.Log;

/**
 * Provides a standard HTML form input widget, for use on a widget dialog.<p>
 *
 * @since 6.0.0
 */
public class CmsInputWidget extends A_CmsWidget implements I_CmsADEWidget {

    /** Config value. */
    public static final String CONF_AUTO_TYPOGRAPHY = "auto-typography";

    /** Config value. */
    public static final String CONF_TYPOGRAPHY = "typography";

    /** Logger instance for this class. */
    private static final Log LOG = CmsLog.getLog(CmsInputWidget.class);

    /**
     * Creates a new input widget.<p>
     */
    public CmsInputWidget() {

        // empty constructor is required for class registration
        this("");
    }

    /**
     * Creates a new input widget with the given configuration.<p>
     *
     * @param configuration the configuration to use
     */
    public CmsInputWidget(String configuration) {

        super(configuration);
    }

    /**
     * @see org.opencms.widgets.I_CmsADEWidget#getConfiguration(org.opencms.file.CmsObject, org.opencms.xml.types.A_CmsXmlContentValue, org.opencms.i18n.CmsMessages, org.opencms.file.CmsResource, java.util.Locale)
     */
    public String getConfiguration(
        CmsObject cms,
        A_CmsXmlContentValue schemaType,
        CmsMessages messages,
        CmsResource resource,
        Locale contentLocale) {

        String configStr = getConfiguration();
        if (configStr == null) {
            configStr = "";
        }
        String[] tokens = configStr.split("\\|");
        JSONObject json = new JSONObject();
        String typografLocale = CmsTextareaWidget.getTypografLocale(contentLocale);
        for (String token : tokens) {
            if (Arrays.asList(CONF_TYPOGRAPHY, CONF_AUTO_TYPOGRAPHY).contains(token)) {
                try {
                    json.put(CmsGwtConstants.JSON_INPUT_TYPOGRAF, CONF_AUTO_TYPOGRAPHY.equals(token) ? "auto" : "true");
                    json.put(CmsGwtConstants.JSON_INPUT_LOCALE, typografLocale);
                } catch (Exception e) {
                    LOG.debug(e.getMessage(), e);

                }
            }
        }
        return json.toString();
    }

    /**
     * @see org.opencms.widgets.I_CmsADEWidget#getCssResourceLinks(org.opencms.file.CmsObject)
     */
    public List<String> getCssResourceLinks(CmsObject cms) {

        // not needed for internal widget
        return null;
    }

    /**
     * @see org.opencms.widgets.I_CmsADEWidget#getDefaultDisplayType()
     */
    public DisplayType getDefaultDisplayType() {

        return DisplayType.singleline;
    }

    /**
     * @see org.opencms.widgets.I_CmsWidget#getDialogWidget(org.opencms.file.CmsObject, org.opencms.widgets.I_CmsWidgetDialog, org.opencms.widgets.I_CmsWidgetParameter)
     */
    public String getDialogWidget(CmsObject cms, I_CmsWidgetDialog widgetDialog, I_CmsWidgetParameter param) {

        String id = param.getId();

        StringBuffer result = new StringBuffer(16);

        result.append("<td class=\"xmlTd\">");
        result.append("<input class=\"xmlInput textInput");
        if (param.hasError()) {
            result.append(" xmlInputError");
        }
        result.append("\"");
        result.append(" name=\"");
        result.append(id);
        result.append("\" id=\"");
        result.append(id);
        result.append("\" value=\"");
        result.append(CmsEncoder.escapeXml(param.getStringValue(cms)));
        result.append("\">");
        result.append("</td>");

        return result.toString();
    }

    /**
     * @see org.opencms.widgets.I_CmsADEWidget#getInitCall()
     */
    public String getInitCall() {

        // not needed for internal widget
        return null;
    }

    /**
     * @see org.opencms.widgets.I_CmsADEWidget#getJavaScriptResourceLinks(org.opencms.file.CmsObject)
     */
    public List<String> getJavaScriptResourceLinks(CmsObject cms) {

        // not needed for internal widget
        return null;
    }

    /**
     * @see org.opencms.widgets.I_CmsADEWidget#getWidgetName()
     */
    public String getWidgetName() {

        return CmsInputWidget.class.getName();
    }

    /**
     * @see org.opencms.widgets.I_CmsADEWidget#isInternal()
     */
    public boolean isInternal() {

        return true;
    }

    /**
     * @see org.opencms.widgets.I_CmsWidget#newInstance()
     */
    public I_CmsWidget newInstance() {

        return new CmsInputWidget(getConfiguration());
    }
}