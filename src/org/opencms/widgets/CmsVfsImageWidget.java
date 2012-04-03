/*
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (c) Alkacon Software GmbH (http://www.alkacon.com)
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
import org.opencms.json.JSONException;
import org.opencms.json.JSONObject;
import org.opencms.loader.CmsImageScaler;
import org.opencms.main.CmsException;
import org.opencms.main.CmsLog;
import org.opencms.util.CmsMacroResolver;
import org.opencms.util.CmsStringUtil;
import org.opencms.workplace.CmsWorkplace;
import org.opencms.xml.types.CmsXmlVfsImageValue;

import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;

/**
 * Provides a widget for an extended image selection using the advanced gallery dialog.<p>
 * 
 * @since 7.5.0 
 */
public class CmsVfsImageWidget extends CmsAdeImageGalleryWidget {

    /** The static log object for this class. */
    private static final Log LOG = CmsLog.getLog(CmsVfsImageWidget.class);

    /** Input field prefix for the description field. */
    private static final String PREFIX_DESCRIPTION = "desc.";

    /** Input field prefix for the format field. */
    private static final String PREFIX_FORMAT = "format.";

    /** Input field prefix for the hidden format value field. */
    private static final String PREFIX_FORMATVALUE = "fmtval.";

    /** Input field prefix for the image field. */
    private static final String PREFIX_IMAGE = "img.";

    /** Input field prefix for the image ratio field. */
    private static final String PREFIX_IMAGERATIO = "imgrat.";

    /** Input field prefix for the hidden scale field. */
    private static final String PREFIX_SCALE = "scale.";

    /**
     * Creates a new image widget.<p>
     */
    public CmsVfsImageWidget() {

        // empty constructor is required for class registration
        super();
    }

    /**
     * Creates an image widget with the specified configuration options.<p>
     * 
     * @param configuration the configuration (possible options) for the image widget
     */
    public CmsVfsImageWidget(String configuration) {

        super(configuration);
    }

    /**
     * @see org.opencms.widgets.I_CmsWidget#getDialogIncludes(org.opencms.file.CmsObject,org.opencms.widgets.I_CmsWidgetDialog)
     */
    @Override
    public String getDialogIncludes(CmsObject cms, I_CmsWidgetDialog widgetDialog) {

        StringBuffer result = new StringBuffer(256);
        // import the JavaScript for the image widget
        result.append(getJSIncludeFile(CmsWorkplace.getSkinUri() + "components/widgets/vfsimage.js"));
        return result.toString();
    }

    /**
     * @see org.opencms.widgets.I_CmsWidget#getDialogWidget(org.opencms.file.CmsObject, org.opencms.widgets.I_CmsWidgetDialog, org.opencms.widgets.I_CmsWidgetParameter)
     */
    @Override
    public String getDialogWidget(CmsObject cms, I_CmsWidgetDialog widgetDialog, I_CmsWidgetParameter param) {

        String id = param.getId();
        long idHash = id.hashCode();
        if (idHash < 0) {
            // negative hash codes will not work as JS variable names, so convert them
            idHash = -idHash;
            // add 2^32 to the value to ensure that it is unique
            idHash += 4294967296L;
        }
        // cast parameter to xml value to access the specific methods
        CmsXmlVfsImageValue value = (CmsXmlVfsImageValue)param;
        String imageLink = value.getRequestLink(cms);
        if (imageLink == null) {
            imageLink = "";
        }

        StringBuffer result = new StringBuffer(4096);

        result.append("<td class=\"xmlTd\" style=\"height: 25px;\">");

        result.append("<table class=\"xmlTableNested\">");
        result.append("<tr>");
        result.append("<td class=\"xmlLabel\">");
        result.append(widgetDialog.getMessages().key(Messages.GUI_EDITOR_LABEL_IMAGE_PATH_0));
        result.append(" </td>");
        result.append("<td>");
        result.append("<input class=\"xmlInputMedium\" value=\"").append(imageLink).append("\" name=\"");
        result.append(PREFIX_IMAGE).append(id).append("\" id=\"");
        result.append(PREFIX_IMAGE).append(id);
        result.append("\" onkeyup=\"checkVfsImagePreview('");
        result.append(id);
        result.append("');\" />");
        result.append("</td>");

        result.append(widgetDialog.dialogHorizontalSpacer(10));
        result.append("<td><table class=\"editorbuttonbackground\" border=\"0\" cellpadding=\"0\" cellspacing=\"0\"><tr>");

        result.append(widgetDialog.button(getOpenGalleryCall(cms, widgetDialog, param, idHash), null, getGalleryName()
            + "gallery", Messages.getButtonName(getGalleryName()), widgetDialog.getButtonStyle()));

        // create preview button
        String previewClass = "hide";
        if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(imageLink)) {
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
            getOpenPreviewCall(widgetDialog, PREFIX_IMAGE + param.getId()),
            null,
            "preview.png",
            Messages.GUI_BUTTON_PREVIEW_0,
            widgetDialog.getButtonStyle()));
        result.append("</tr></table></td>");

        result.append("</tr></table></td>");
        result.append("</tr>");

        JSONObject additional = null;
        try {
            additional = getAdditionalGalleryInfo(cms, widgetDialog, param);
        } catch (JSONException e) {
            LOG.error("Error parsing widget configuration", e);
        }
        if (additional != null) {
            result.append("\n<script type=\"text/javascript\">\n");
            result.append("var cms_additional_").append(idHash).append("=");
            result.append(additional.toString()).append(";\n");
            result.append("</script>");
        }
        CmsVfsImageWidgetConfiguration configuration = getWidgetConfiguration(cms, widgetDialog, param);
        String format = value.getFormat(cms);
        if (configuration.isShowFormat()) {
            // show the format select box, also create hidden format value field
            result.append("<tr>");
            result.append("<td class=\"xmlLabel\">");
            result.append(widgetDialog.getMessages().key(Messages.GUI_EDITOR_LABEL_IMAGE_FORMAT_0));
            result.append(" </td>");
            result.append("<td class=\"xmlTd\">");
            result.append("<select class=\"xmlInput");
            if (param.hasError()) {
                result.append(" xmlInputError");
            }
            result.append("\" name=\"");
            result.append(PREFIX_FORMAT).append(id);
            result.append("\" id=\"");
            result.append(PREFIX_FORMAT).append(id);
            result.append("\"");
            result.append(" onchange=\"setImageFormat(\'");
            result.append(id);
            result.append("\', \'imgFmts");
            result.append(idHash);
            result.append("\');\"");
            result.append(">");

            // get select box options from default value String
            List<CmsSelectWidgetOption> options = configuration.getSelectFormat();
            String selected = getSelectedValue(cms, options, format);
            int selectedIndex = 0;
            for (int i = 0; i < options.size(); i++) {
                CmsSelectWidgetOption option = options.get(i);
                // create the option
                result.append("<option value=\"");
                result.append(option.getValue());
                result.append("\"");
                if ((selected != null) && selected.equals(option.getValue())) {
                    result.append(" selected=\"selected\"");
                    selectedIndex = i;
                }
                result.append(">");
                result.append(option.getOption());
                result.append("</option>");
            }
            result.append("</select>");
            result.append("</td>");
            result.append("</tr>");
            List<String> formatValues = configuration.getFormatValues();
            String selectedFormat = "";
            try {
                selectedFormat = formatValues.get(selectedIndex);
            } catch (Exception e) {
                // ignore, just didn't find a matching format value
            }
            // create hidden field to store the matching image format value
            result.append("<input type=\"hidden\" value=\"").append(selectedFormat).append("\" name=\"");
            result.append(PREFIX_FORMATVALUE).append(id).append("\" id=\"");
            result.append(PREFIX_FORMATVALUE).append(id).append("\" />");
            // create hidden field to store image ratio
            String ratio = "";
            if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(imageLink)) {
                // an image is specified, calculate ratio
                try {
                    CmsImageScaler scaler = new CmsImageScaler(cms, cms.readResource(imageLink));
                    float r = scaler.getWidth() / (float)scaler.getHeight();
                    ratio = String.valueOf(r);
                } catch (CmsException e) {
                    // ignore, image not found in VFS
                }
            }
            result.append("<input type=\"hidden\" value=\"").append(ratio).append("\" name=\"");
            result.append(PREFIX_IMAGERATIO).append(id).append("\" id=\"");
            result.append(PREFIX_IMAGERATIO).append(id).append("\" />");
            // add possible format names and values as JS variables to access them from image gallery window
            result.append("\n<script type=\"text/javascript\">");
            JSONArray formatsJson = new JSONArray(configuration.getFormatValues());
            result.append("\nvar imgFmts").append(idHash).append(" = ").append(formatsJson).append(";");
            result.append("\nvar imgFmtNames").append(idHash).append(" = \"").append(
                CmsEncoder.escape(configuration.getSelectFormatString(), CmsEncoder.ENCODING_UTF_8)).append("\";");
            result.append("\nvar useFmts").append(idHash).append(" = true;");
            result.append("\n</script>");
        } else {
            result.append("<input type=\"hidden\" value=\"\" name=\"");
            result.append(PREFIX_IMAGERATIO).append(id).append("\" id=\"");
            result.append(PREFIX_IMAGERATIO).append(id).append("\" />");
            result.append("<input type=\"hidden\" value=\"").append(format).append("\" name=\"");
            result.append(PREFIX_FORMAT).append(id).append("\" id=\"");
            result.append(PREFIX_FORMAT).append(id).append("\" />");
            result.append("\n<script type=\"text/javascript\">");
            result.append("\nvar useFmts").append(idHash).append(" = false;");
            result.append("\n</script>");
        }

        String description = value.getDescription(cms);
        if (description == null) {
            description = "";
        }

        if (configuration.isShowDescription()) {
            result.append("<tr>");
            result.append("<td class=\"xmlLabel\">");
            result.append(widgetDialog.getMessages().key(Messages.GUI_EDITOR_LABEL_IMAGE_DESC_0));
            result.append("</td>");
            result.append("<td class=\"xmlTd\">");
            result.append("<textarea class=\"xmlInput maxwidth");
            if (param.hasError()) {
                result.append(" xmlInputError");
            }
            result.append("\" name=\"");
            result.append(PREFIX_DESCRIPTION).append(id).append("\" id=\"");
            result.append(PREFIX_DESCRIPTION).append(id);
            result.append("\" rows=\"");
            result.append(2);
            result.append("\" cols=\"60\" style=\"height: 3em; overflow:auto;\">");
            result.append(CmsEncoder.escapeXml(description));
            result.append("</textarea>");
            result.append("</td>");
            result.append("</tr>");
        } else {
            result.append("<input type=\"hidden\" value=\"").append(CmsEncoder.escapeXml(description)).append(
                "\" name=\"");
            result.append(PREFIX_DESCRIPTION).append(id).append("\" id=\"");
            result.append(PREFIX_DESCRIPTION).append(id).append("\" />");

        }
        result.append("</table>");

        String scale = value.getScaleOptions(cms);
        if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(configuration.getScaleParams())
            && (scale.indexOf(configuration.getScaleParams()) == -1)) {
            if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(scale)) {
                scale += ",";
            }
            scale += configuration.getScaleParams();

        }
        result.append("<input type=\"hidden\" value=\"").append(scale).append("\" name=\"");
        result.append(PREFIX_SCALE).append(id).append("\" id=\"");
        result.append(PREFIX_SCALE).append(id).append("\" />");

        result.append("</td>");

        return result.toString();
    }

    /**
     * @see org.opencms.widgets.A_CmsWidget#getWidgetStringValue(org.opencms.file.CmsObject, org.opencms.widgets.I_CmsWidgetDialog, org.opencms.widgets.I_CmsWidgetParameter)
     */
    @Override
    public String getWidgetStringValue(CmsObject cms, I_CmsWidgetDialog widgetDialog, I_CmsWidgetParameter param) {

        String result = super.getWidgetStringValue(cms, widgetDialog, param);
        String configuration = CmsMacroResolver.resolveMacros(getConfiguration(), cms, widgetDialog.getMessages());
        if (configuration == null) {
            configuration = param.getDefault(cms);
        }
        List<CmsSelectWidgetOption> options = CmsSelectWidgetOption.parseOptions(configuration);
        for (int m = 0; m < options.size(); m++) {
            CmsSelectWidgetOption option = options.get(m);
            if (result.equals(option.getValue())) {
                result = option.getOption();
                break;
            }
        }
        return result;
    }

    /**
     * @see org.opencms.widgets.I_CmsWidget#newInstance()
     */
    @Override
    public I_CmsWidget newInstance() {

        return new CmsVfsImageWidget(getConfiguration());
    }

    /**
     * @see org.opencms.widgets.I_CmsWidget#setEditorValue(org.opencms.file.CmsObject, java.util.Map, org.opencms.widgets.I_CmsWidgetDialog, org.opencms.widgets.I_CmsWidgetParameter)
     */
    @Override
    public void setEditorValue(
        CmsObject cms,
        Map<String, String[]> formParameters,
        I_CmsWidgetDialog widgetDialog,
        I_CmsWidgetParameter param) {

        String[] imgValues = formParameters.get(PREFIX_IMAGE + param.getId());
        if ((imgValues != null) && (imgValues.length > 0)) {
            param.setStringValue(cms, imgValues[0]);
        }

        CmsXmlVfsImageValue value = (CmsXmlVfsImageValue)param;

        String[] descValues = formParameters.get(PREFIX_DESCRIPTION + param.getId());
        value.setDescription(cms, descValues[0]);

        String[] formatValues = formParameters.get(PREFIX_FORMAT + param.getId());
        value.setFormat(cms, formatValues[0]);

        String[] scaleValues = formParameters.get(PREFIX_SCALE + param.getId());
        value.setScaleOptions(cms, scaleValues[0]);
    }

    /**
     * @see org.opencms.widgets.CmsAdeImageGalleryWidget#getAdditionalGalleryInfo(org.opencms.file.CmsObject, org.opencms.widgets.I_CmsWidgetDialog, org.opencms.widgets.I_CmsWidgetParameter)
     */
    @Override
    protected JSONObject getAdditionalGalleryInfo(
        CmsObject cms,
        I_CmsWidgetDialog widgetDialog,
        I_CmsWidgetParameter param) throws JSONException {

        JSONObject result = super.getAdditionalGalleryInfo(cms, widgetDialog, param);
        result.put("isAdvancedWidget", true);
        return result;
    }

    /**
     * @see org.opencms.widgets.A_CmsAdeGalleryWidget#getGalleryOpenParams(org.opencms.file.CmsObject, org.opencms.widgets.I_CmsWidgetDialog, org.opencms.widgets.I_CmsWidgetParameter, long)
     */
    @Override
    protected Map<String, String> getGalleryOpenParams(
        CmsObject cms,
        I_CmsWidgetDialog widgetDialog,
        I_CmsWidgetParameter param,
        long hashId) {

        Map<String, String> result = super.getGalleryOpenParams(cms, widgetDialog, param, hashId);
        // the current element value will be read by java-script including the image input field and the scale input field
        StringBuffer currentElement = new StringBuffer("'+document.getElementById('");
        currentElement.append(PREFIX_IMAGE).append(param.getId());
        currentElement.append("').getAttribute('value')+'");
        // only try reading scale and format info if formats are used
        if (getWidgetConfiguration(cms, widgetDialog, param).isShowFormat()) {
            currentElement.append("%3F__scale%3D'+document.getElementById('");
            currentElement.append(PREFIX_SCALE).append(param.getId()).append("').getAttribute('value')+'");
            currentElement.append("%26__formatName%3D'+escape(document.getElementById('").append(PREFIX_FORMAT).append(
                param.getId()).append("')[document.getElementById('").append(PREFIX_FORMAT).append(param.getId()).append(
                "').selectedIndex].value)+'");
        }
        result.put(GALLERY_PARAM.currentelement.name(), currentElement.toString());
        return result;
    }

    /**
     * Returns the currently selected value of the select widget.<p>
     * 
     * If a value is found in the given parameter, this is used. Otherwise 
     * the default value of the select options are used. If there is neither a parameter value
     * nor a default value, <code>null</code> is returned.<p> 
     * 
     * @param cms the current users OpenCms context
     * @param selectOptions the available select options
     * @param currentValue the current value that is selected
     * 
     * @return the currently selected value of the select widget
     */
    protected String getSelectedValue(CmsObject cms, List<CmsSelectWidgetOption> selectOptions, String currentValue) {

        String paramValue = currentValue;
        if (CmsStringUtil.isEmpty(paramValue)) {
            CmsSelectWidgetOption option = CmsSelectWidgetOption.getDefaultOption(selectOptions);
            if (option != null) {
                paramValue = option.getValue();
            }
        }
        return paramValue;
    }
}