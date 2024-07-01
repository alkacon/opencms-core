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

import java.util.List;
import java.util.Locale;

import org.apache.commons.logging.Log;

/**
 * Provides a standard HTML form textarea widget, for use on a widget dialog.<p>
 *
 * Displays a textarea with 4 rows to enter String values conveniently.<p>
 *
 * @since 6.0.0
 */
public class CmsTextareaWidget extends A_CmsWidget implements I_CmsADEWidget {

    /** Logger instance for this class. */
    private static final Log LOG = CmsLog.getLog(CmsTextareaWidget.class);

    /** Default number of rows to display. */
    private static final int DEFAULT_ROWS_NUMBER = 4;

    /**
     * Creates a new textarea widget.<p>
     */
    public CmsTextareaWidget() {

        // default configuration is to display 4 rows
        this(DEFAULT_ROWS_NUMBER);
    }

    /**
     * Creates a new textarea widget with the given number of rows.<p>
     *
     * @param rows the number of rows to display
     */
    public CmsTextareaWidget(int rows) {

        super("" + rows);
    }

    /**
     * Creates a new textarea widget with the given configuration.<p>
     *
     * @param configuration the configuration to use
     */
    public CmsTextareaWidget(String configuration) {

        super(configuration);
    }

    /**
     * Converts locale to the necessary format for the Typograf library.
     * @param contentLocale the locale
     * @return the locale for Typograf
     */
    public static  String getTypografLocale(Locale contentLocale) {

        String localeStr = contentLocale.toString();
        if (contentLocale.getLanguage().equals("en")) {
            localeStr = "en-US";
        } else {
            localeStr = contentLocale.getLanguage();
        }
        return localeStr;
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

        JSONObject json = new JSONObject();
        try {
            json.put(CmsGwtConstants.JSON_TEXTAREA_CONFIG, getConfiguration());
            String localeStr = getTypografLocale(contentLocale);
            json.put(CmsGwtConstants.JSON_TEXTAREA_LOCALE, localeStr);
        } catch (Exception e) {
            LOG.error(e.getLocalizedMessage(), e);
        }
        String result = json.toString();
        return result;
    }

    /**
     * @see org.opencms.widgets.I_CmsADEWidget#getCssResourceLinks(org.opencms.file.CmsObject)
     */
    public List<String> getCssResourceLinks(CmsObject cms) {

        return null;
    }

    /**
     * @see org.opencms.widgets.I_CmsADEWidget#getDefaultDisplayType()
     */
    public DisplayType getDefaultDisplayType() {

        return DisplayType.wide;
    }

    /**
     * @see org.opencms.widgets.I_CmsWidget#getDialogWidget(org.opencms.file.CmsObject, org.opencms.widgets.I_CmsWidgetDialog, org.opencms.widgets.I_CmsWidgetParameter)
     */
    public String getDialogWidget(CmsObject cms, I_CmsWidgetDialog widgetDialog, I_CmsWidgetParameter param) {

        String id = param.getId();
        StringBuffer result = new StringBuffer(16);
        int rows = DEFAULT_ROWS_NUMBER;
        try {
            rows = Integer.valueOf(getConfiguration()).intValue();
        } catch (Exception e) {
            // ignore
        }

        result.append("<td class=\"xmlTd\">");
        result.append("<textarea class=\"xmlInput maxwidth");
        if (param.hasError()) {
            result.append(" xmlInputError");
        }
        result.append("\" name=\"");
        result.append(id);
        result.append("\" rows=\"");
        result.append(rows);
        result.append("\" cols=\"60\" style=\"overflow:auto;\">");
        result.append(CmsEncoder.escapeXml(param.getStringValue(cms)));
        result.append("</textarea>");
        result.append("</td>");

        return result.toString();
    }

    /**
     * @see org.opencms.widgets.I_CmsADEWidget#getInitCall()
     */
    public String getInitCall() {

        return null;
    }

    /**
     * @see org.opencms.widgets.I_CmsADEWidget#getJavaScriptResourceLinks(org.opencms.file.CmsObject)
     */
    public List<String> getJavaScriptResourceLinks(CmsObject cms) {

        return null;
    }

    /**
     * @see org.opencms.widgets.I_CmsADEWidget#getWidgetName()
     */
    public String getWidgetName() {

        return CmsTextareaWidget.class.getName();
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

        return new CmsTextareaWidget(getConfiguration());
    }

}