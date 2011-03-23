/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/widgets/A_CmsGalleryWidget.java,v $
 * Date   : $Date: 2011/03/23 14:50:15 $
 * Version: $Revision: 1.23 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (c) 2002 - 2011 Alkacon Software GmbH (http://www.alkacon.com)
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
import org.opencms.json.JSONArray;
import org.opencms.util.CmsStringUtil;
import org.opencms.workplace.CmsWorkplace;
import org.opencms.workplace.galleries.A_CmsAjaxGallery;

/**
 * Base class for all gallery widget implementations.<p>
 *
 * @author Alexander Kandzior 
 * @author Andreas Zahner 
 * 
 * @version $Revision: 1.23 $ 
 * 
 * @since 6.0.0 
 */
public abstract class A_CmsGalleryWidget extends A_CmsWidget {

    /**
     * Creates a new gallery widget.<p>
     */
    protected A_CmsGalleryWidget() {

        // empty constructor is required for class registration
        this("");
    }

    /**
     * Creates a new gallery widget with the given configuration.<p>
     * 
     * @param configuration the configuration to use
     */
    protected A_CmsGalleryWidget(String configuration) {

        super(configuration);
    }

    /**
     * @see org.opencms.widgets.I_CmsWidget#getDialogIncludes(org.opencms.file.CmsObject, org.opencms.widgets.I_CmsWidgetDialog)
     */
    @Override
    public String getDialogIncludes(CmsObject cms, I_CmsWidgetDialog widgetDialog) {

        StringBuffer result = new StringBuffer(256);
        // import the JavaScript for the gallery widget
        result.append(getJSIncludeFile(CmsWorkplace.getSkinUri()
            + "components/widgets/"
            + getNameLower()
            + "gallery.js"));
        return result.toString();
    }

    /**
     * @see org.opencms.widgets.I_CmsWidget#getDialogInitCall(org.opencms.file.CmsObject, org.opencms.widgets.I_CmsWidgetDialog)
     */
    @Override
    public String getDialogInitCall(CmsObject cms, I_CmsWidgetDialog widgetDialog) {

        return "\tinit" + getNameUpper() + "Gallery();\n";
    }

    /**
     * @see org.opencms.widgets.I_CmsWidget#getDialogInitMethod(org.opencms.file.CmsObject, org.opencms.widgets.I_CmsWidgetDialog)
     */
    @Override
    public String getDialogInitMethod(CmsObject cms, I_CmsWidgetDialog widgetDialog) {

        StringBuffer result = new StringBuffer(16);
        result.append("function init");
        result.append(getNameUpper());
        result.append("Gallery() {\n");
        result.append("\t");
        result.append(getNameLower());
        result.append("GalleryPath = '");
        // path to download/image/link/html/table gallery
        result.append(A_CmsAjaxGallery.PATH_GALLERIES);
        result.append(getNameLower());
        result.append("gallery/index.jsp?");
        result.append("';\n");
        result.append("}\n");
        return result.toString();
    }

    /**
     * @see org.opencms.widgets.I_CmsWidget#getDialogWidget(org.opencms.file.CmsObject, org.opencms.widgets.I_CmsWidgetDialog, org.opencms.widgets.I_CmsWidgetParameter)
     */
    public String getDialogWidget(CmsObject cms, I_CmsWidgetDialog widgetDialog, I_CmsWidgetParameter param) {

        String id = param.getId();
        long idHash = id.hashCode();
        if (idHash < 0) {
            // negative hash codes will not work as JS variable names, so convert them
            idHash = -idHash;
            // add 2^32 to the value to ensure that it is unique
            idHash += 4294967296L;
        }
        StringBuffer result = new StringBuffer(128);
        result.append("<td class=\"xmlTd\">");
        result.append("<table border=\"0\" cellpadding=\"0\" cellspacing=\"0\"><tr><td>");
        result.append("<input class=\"xmlInputMedium");
        if (param.hasError()) {
            result.append(" xmlInputError");
        }
        result.append("\" value=\"");
        String value = param.getStringValue(cms);
        result.append(value);
        result.append("\" name=\"");
        result.append(id);
        result.append("\" id=\"");
        result.append(id);
        result.append("\" onkeyup=\"checkPreview('");
        result.append(id);
        result.append("');\"></td>");
        result.append(widgetDialog.dialogHorizontalSpacer(10));
        result.append("<td><table class=\"editorbuttonbackground\" border=\"0\" cellpadding=\"0\" cellspacing=\"0\"><tr>");

        result.append(widgetDialog.button(
            "javascript:open"
                + getNameUpper()
                + "Gallery('"
                + A_CmsAjaxGallery.MODE_WIDGET
                + "',  '"
                + id
                + "',  '"
                + idHash
                + "');return false;",
            null,
            getNameLower() + "gallery",
            Messages.getButtonName(getNameLower()),
            widgetDialog.getButtonStyle()));
        // create preview button
        String previewClass = "hide";
        if (showPreview(value)) {
            // show button if preview is enabled
            previewClass = "show";
        }
        result.append("<td class=\"");
        result.append(previewClass);
        result.append("\" id=\"preview");
        result.append(id);
        result.append("\">");
        result.append("<table border=\"0\" cellpadding=\"0\" cellspacing=\"0\"><tr>");
        result.append(widgetDialog.button(
            "javascript:preview" + getNameUpper() + "('" + id + "');return false;",
            null,
            "preview.png",
            Messages.GUI_BUTTON_PREVIEW_0,
            widgetDialog.getButtonStyle()));
        result.append("</tr></table>");

        result.append("</td></tr></table>");

        result.append("</td>");
        result.append("</tr></table>");

        result.append("</td>");

        if (getNameLower().equals("image")) {
            // reads the configuration String for this widget
            CmsVfsImageWidgetConfiguration configuration = new CmsVfsImageWidgetConfiguration(
                cms,
                widgetDialog,
                param,
                getConfiguration());

            if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(configuration.getStartup())) {
                result.append("\n<script type=\"text/javascript\">");
                result.append("\nvar startupFolder").append(idHash).append(" = \"").append(configuration.getStartup()).append(
                    "\";");
                result.append("\nvar startupType").append(idHash).append(" = \"").append(configuration.getType()).append(
                    "\";");
                result.append("\n</script>");
            } else {
                result.append("\n<script type=\"text/javascript\">");
                result.append("\nvar startupFolder").append(idHash).append(" = null;");
                result.append("\nvar startupType").append(idHash).append(" = null;");
                result.append("\n</script>");
            }

            //This part is not used in javascript for now
            if (configuration.isShowFormat()) {
                // create hidden field to store the matching image format value
                result.append("\n<script type=\"text/javascript\">");
                JSONArray formatsJson = new JSONArray(configuration.getFormatValues());
                result.append("\nvar imgFmts").append(idHash).append(" = ").append(formatsJson).append(";");
                result.append("\nvar imgFmtNames").append(idHash).append(" = \"").append(
                    CmsEncoder.escape(configuration.getSelectFormatString(), CmsEncoder.ENCODING_UTF_8)).append("\";");
                result.append("\nvar useFmts").append(idHash).append(" = true;");
                result.append("\n</script>");
            } else {
                result.append("\n<script type=\"text/javascript\">");
                result.append("\nvar useFmts").append(idHash).append(" = false;");
                result.append("\nvar imgFmts").append(idHash).append(" = null;");
                result.append("\nvar imgFmtNames").append(idHash).append(" = null;");
                result.append("\n</script>");
            }
        } else { // for download, link, html or table galleries
            // reads the configuration String for this widget
            CmsGalleryWidgetConfiguration configuration = new CmsGalleryWidgetConfiguration(
                cms,
                widgetDialog,
                param,
                getConfiguration());

            if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(configuration.getStartup())) {
                result.append("\n<script type=\"text/javascript\">");
                result.append("\nvar startupFolder").append(idHash).append(" = \"").append(configuration.getStartup()).append(
                    "\";");
                result.append("\nvar startupType").append(idHash).append(" = \"").append(configuration.getType()).append(
                    "\";");
                result.append("\n</script>");
            } else {
                result.append("\n<script type=\"text/javascript\">");
                result.append("\nvar startupFolder").append(idHash).append(" = null;");
                result.append("\nvar startupType").append(idHash).append(" = null;");
                result.append("\n</script>");
            }
        }

        return result.toString();
    }

    /**
     * Returns the lower case name of the gallery, for example <code>"html"</code>.<p>
     * 
     * @return the lower case name of the gallery
     */
    public abstract String getNameLower();

    /**
     * Returns the upper case name of the gallery, for example <code>"Html"</code>.<p>
     * 
     * @return the upper case name of the gallery
     */
    public abstract String getNameUpper();

    /**
     * Returns <code>true</code> if the preview button should be shown.<p>
     * 
     * @param value the current widget value
     * @return <code>true</code> if the preview button should be shown
     */
    public abstract boolean showPreview(String value);
}