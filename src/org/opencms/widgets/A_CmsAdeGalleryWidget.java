/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/widgets/A_CmsAdeGalleryWidget.java,v $
 * Date   : $Date: 2010/07/06 14:54:45 $
 * Version: $Revision: 1.2 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (C) 2002 - 2009 Alkacon Software (http://www.alkacon.com)
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

package org.opencms.widgets;

import org.opencms.file.CmsObject;
import org.opencms.json.JSONObject;
import org.opencms.main.OpenCms;
import org.opencms.util.CmsStringUtil;
import org.opencms.workplace.CmsDialog;
import org.opencms.workplace.galleries.A_CmsAjaxGallery;

/**
 * Base class for all ADE gallery widget implementations.<p>
 *
 * @author Tobias Herrmann 
 * 
 * @version $Revision: 1.2 $ 
 * 
 * @since 8.0.0 
 */
public abstract class A_CmsAdeGalleryWidget extends A_CmsWidget {

    /** The gallery JSP path. */
    protected static final String PATH_GALLERY_JSP = "/system/modules/org.opencms.ade.galleries/gallery.jsp";

    /**
     * Constructor.<p>
     */
    public A_CmsAdeGalleryWidget() {

        this("");
    }

    /**
     * Creates a new gallery widget with the given configuration.<p>
     * 
     * @param configuration the configuration to use
     */
    protected A_CmsAdeGalleryWidget(String configuration) {

        super(configuration);
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
        StringBuffer result = new StringBuffer(512);
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
        result.append(widgetDialog.button(getOpenGalleryCall(cms, widgetDialog, param, idHash), null, getGalleryName()
            + "gallery", Messages.getButtonName(getGalleryName()), widgetDialog.getButtonStyle()));
        // create preview button
        String previewClass = "hide";
        if (CmsStringUtil.isNotEmpty(value) && value.startsWith("/")) {
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
            getOpenPreviewCall(param.getId()),
            null,
            "preview.png",
            Messages.GUI_BUTTON_PREVIEW_0,
            widgetDialog.getButtonStyle()));
        result.append("</tr></table>");

        result.append("</td></tr></table>");

        result.append("</td>");
        result.append("</tr></table>");

        result.append("</td>");

        JSONObject additional = getAdditionalGalleryInfo(cms, widgetDialog, param);
        if (additional != null) {
            result.append("\n<script type=\"text/javascript\">\n");
            result.append("var cms_additional_").append(idHash).append("=");
            result.append(additional.toString()).append(";\n");
            result.append("</script>");
        }

        return result.toString();
    }

    /**
     * Returns the lower case name of the gallery, for example <code>"html"</code>.<p>
     * 
     * @return the lower case name of the gallery
     */
    public abstract String getGalleryName();

    /**
     * Returns additional widget information encapsulated in a JSON object.<p>
     * May be <code>null</code>.<p>
     * 
     * @param cms an initialized instance of a CmsObject
     * @param widgetDialog the dialog where the widget is used on
     * @param param the widget parameter to generate the widget for
     * 
     * @return additional widget information
     */
    protected abstract JSONObject getAdditionalGalleryInfo(
        CmsObject cms,
        I_CmsWidgetDialog widgetDialog,
        I_CmsWidgetParameter param);

    /**
     * Returns the resource type names available within this gallery widget.<p>
     * 
     * @return the resource type names
     */
    protected abstract String getGalleryTypes();

    /**
     * Returns the javascript call to open the gallery widget dialog.<p>
     * 
     * @param cms an initialized instance of a CmsObject
     * @param widgetDialog the dialog where the widget is used on
     * @param param the widget parameter to generate the widget for
     * @param hashId the field id hash
     * 
     * @return the javascript call to open the gallery widget dialog
     */
    protected String getOpenGalleryCall(
        CmsObject cms,
        I_CmsWidgetDialog widgetDialog,
        I_CmsWidgetParameter param,
        long hashId) {

        // the edited resource
        String paramResource = "&resource=";
        if (widgetDialog instanceof CmsDialog) {
            paramResource += ((CmsDialog)widgetDialog).getParamResource();
        }

        // the start up gallery path
        String paramGalleryPath = "&gallerypath=";
        CmsGalleryWidgetConfiguration configuration = new CmsGalleryWidgetConfiguration(
            cms,
            widgetDialog,
            param,
            getConfiguration());
        if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(configuration.getStartup())) {
            paramGalleryPath += configuration.getStartup();
        }

        StringBuffer sb = new StringBuffer(128);
        sb.append("javascript:cmsOpenDialog('");

        // the gallery title
        sb.append(Messages.getButtonName(getGalleryName())).append("', '");

        // the gallery path
        sb.append(OpenCms.getSystemInfo().getOpenCmsContext()).append(PATH_GALLERY_JSP);

        // the gallery parameters
        sb.append("?dialogmode=").append(A_CmsAjaxGallery.MODE_WIDGET).append("&types=").append(getGalleryTypes());

        // the current field value
        sb.append("&currentelement='+document.getElementById('").append(param.getId());
        sb.append("').getAttribute('value')+'");
        sb.append(paramResource).append(paramGalleryPath);
        sb.append("&fieldid=").append(param.getId()).append("&hashid=").append(hashId);
        sb.append("', '").append(param.getId()).append("', 490, 650); return false;");
        return sb.toString();
    }

    /**
     * Returns the javascript call to open the preview dialog.<p>
     * 
     * @param id the field id
     * 
     * @return the javascript call to open the preview dialog
     */
    protected String getOpenPreviewCall(String id) {

        StringBuffer sb = new StringBuffer(64);
        sb.append("javascript:cmsOpenPreview('").append(Messages.GUI_BUTTON_PREVIEW_0);
        sb.append("', '").append(OpenCms.getSystemInfo().getOpenCmsContext());
        sb.append("', '").append(id);
        sb.append("'); return false;");
        return sb.toString();
    }

}
