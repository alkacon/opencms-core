/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/widgets/CmsDisplayWidget.java,v $
 * Date   : $Date: 2005/07/28 15:20:49 $
 * Version: $Revision: 1.10 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Mananagement System
 *
 * Copyright (c) 2005 Alkacon Software GmbH (http://www.alkacon.com)
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
 * For further information about Alkacon Software GmbH, please see the
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
import org.opencms.i18n.CmsEncoder;
import org.opencms.main.OpenCms;
import org.opencms.util.CmsStringUtil;

/**
 * Provides a display only widget, for use on a widget dialog.<p>
 *
 * @author Michael Moossen 
 * 
 * @version $Revision: 1.10 $ 
 * 
 * @since 6.0.0 
 */
public class CmsDisplayWidget extends A_CmsWidget {

    /** Key post fix, so you can display different help text if used a "normal" widget, and a display widget. */
    private static final String DISABLED_POSTFIX = ".disabled";

    /**
     * Creates a new input widget.<p>
     */
    public CmsDisplayWidget() {

        // empty constructor is required for class registration
        this("");
    }

    /**
     * Creates a new input widget with the given configuration.<p>
     * 
     * @param configuration the configuration to use
     */
    public CmsDisplayWidget(String configuration) {

        super(configuration);
    }

    /**
     * @see org.opencms.widgets.I_CmsWidget#getDialogWidget(org.opencms.file.CmsObject, org.opencms.widgets.I_CmsWidgetDialog, org.opencms.widgets.I_CmsWidgetParameter)
     */
    public String getDialogWidget(CmsObject cms, I_CmsWidgetDialog widgetDialog, I_CmsWidgetParameter param) {

        String id = param.getId();

        StringBuffer result = new StringBuffer(16);

        result.append("<td class=\"xmlTd\">");
        result.append("<span class=\"xmlInput textInput\" style=\"border: 0px solid black;\">");
        if (CmsStringUtil.isNotEmpty(getConfiguration())) {
            result.append(getConfiguration());
        } else {
            result.append(param.getStringValue(cms));
        }
        result.append("</span>");
        result.append("<input type=\"hidden\"");
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
     * Returns the localized help key for the provided widget parameter.<p>
     * 
     * @param param the widget parameter to return the localized help key for
     * 
     * @return the localized help key for the provided widget parameter
     */
    private String getDisabledHelpKey(I_CmsWidgetParameter param) {
        
        StringBuffer result = new StringBuffer(64);
        result.append(LABEL_PREFIX);
        result.append(param.getKey());
        result.append(HELP_POSTFIX);
        result.append(DISABLED_POSTFIX);
        return result.toString();
    }
    /**
     * @see org.opencms.widgets.A_CmsWidget#getHelpBubble(org.opencms.file.CmsObject, org.opencms.widgets.I_CmsWidgetDialog, org.opencms.widgets.I_CmsWidgetParameter)
     */
    public String getHelpBubble(CmsObject cms, I_CmsWidgetDialog widgetDialog, I_CmsWidgetParameter param) {

        StringBuffer result = new StringBuffer(128);
        String locKey = getDisabledHelpKey(param);
        String locValue = widgetDialog.getMessages().key(locKey, true);
        if (locValue == null) {
            // there was no help message found for this key, so return a spacer cell
            return widgetDialog.dialogHorizontalSpacer(16);
        } else {
            result.append("<td>");
            result.append("<img name=\"img");
            result.append(locKey);
            result.append("\" id=\"img");
            result.append(locKey);
            result.append("\" src=\"");
            result.append(OpenCms.getLinkManager().substituteLink(cms, "/system/workplace/resources/commons/help.png"));
            result.append("\" alt=\"\" border=\"0\"");
            result.append(getJsHelpMouseHandler(widgetDialog, locKey));
            result.append("></td>");
            return result.toString();
        }
    }

    /**
     * @see org.opencms.widgets.A_CmsWidget#getHelpText(org.opencms.widgets.I_CmsWidgetDialog, org.opencms.widgets.I_CmsWidgetParameter)
     */
    public String getHelpText(I_CmsWidgetDialog widgetDialog, I_CmsWidgetParameter param) {

        StringBuffer result = new StringBuffer(128);
        // calculate the key
        String locKey = getDisabledHelpKey(param);
        String locValue = widgetDialog.getMessages().key(locKey, true);
        if (locValue == null) {
            // there was no help message found for this key, so return an empty string
            return "";
        } else {
            result.append("<div class=\"help\" id=\"help");
            result.append(locKey);
            result.append("\"");
            result.append(getJsHelpMouseHandler(widgetDialog, locKey));
            result.append(">");
            result.append(locValue);
            result.append("</div>");
            return result.toString();
        }
    }

    /**
     * @see org.opencms.widgets.I_CmsWidget#newInstance()
     */
    public I_CmsWidget newInstance() {

        return new CmsDisplayWidget(getConfiguration());
    }
}