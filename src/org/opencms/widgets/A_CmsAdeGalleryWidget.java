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

package org.opencms.widgets;

import org.opencms.ade.galleries.shared.I_CmsGalleryProviderConstants;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsResource;
import org.opencms.i18n.CmsMessages;
import org.opencms.json.JSONException;
import org.opencms.json.JSONObject;
import org.opencms.main.CmsLog;
import org.opencms.main.OpenCms;
import org.opencms.util.CmsStringUtil;
import org.opencms.workplace.CmsDialog;
import org.opencms.workplace.galleries.A_CmsAjaxGallery;
import org.opencms.xml.content.I_CmsXmlContentHandler.DisplayType;
import org.opencms.xml.types.A_CmsXmlContentValue;
import org.opencms.xml.types.I_CmsXmlContentValue;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.logging.Log;

/**
 * Base class for all ADE gallery widget implementations.<p>
 *
 * @since 8.0.0
 */
public abstract class A_CmsAdeGalleryWidget extends A_CmsWidget implements I_CmsADEWidget {

    /** The gallery JSP path. */
    protected static final String PATH_GALLERY_JSP = "/system/workplace/commons/gallery.jsp";

    /** The static log object for this class. */
    private static final Log LOG = CmsLog.getLog(A_CmsAdeGalleryWidget.class);

    /** The widget configuration. */
    private CmsGalleryWidgetConfiguration m_widgetConfiguration;

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
     * @see org.opencms.widgets.I_CmsADEWidget#getConfiguration(org.opencms.file.CmsObject, org.opencms.xml.types.A_CmsXmlContentValue, org.opencms.i18n.CmsMessages, org.opencms.file.CmsResource, java.util.Locale)
     */
    public String getConfiguration(
        CmsObject cms,
        A_CmsXmlContentValue schemaType,
        CmsMessages messages,
        CmsResource resource,
        Locale contentLocale) {

        return getJSONConfig(cms, schemaType, messages, resource, contentLocale).toString();
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
        long idHash = id.hashCode();
        if (idHash < 0) {
            // negative hash codes will not work as JS variable names, so convert them
            idHash = -idHash;
            // add 2^32 to the value to ensure that it is unique
            idHash += 4294967296L;
        }
        StringBuffer result = new StringBuffer(512);
        result.append("<td class=\"xmlTd\">");
        result.append(
            "<table border=\"0\" cellpadding=\"0\" cellspacing=\"0\" class=\"maxwidth\"><tr><td class=\"xmlTd\">");
        result.append("<input class=\"xmlInput textInput");
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
        result.append(
            "<td><table class=\"editorbuttonbackground\" border=\"0\" cellpadding=\"0\" cellspacing=\"0\"><tr>");
        result.append(
            widgetDialog.button(
                getOpenGalleryCall(cms, widgetDialog, param, idHash),
                null,
                getGalleryName() + "gallery",
                Messages.getButtonName(getGalleryName()),
                widgetDialog.getButtonStyle()));
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
        result.append(
            widgetDialog.button(
                getOpenPreviewCall(widgetDialog, param.getId()),
                null,
                "preview.png",
                Messages.GUI_BUTTON_PREVIEW_0,
                widgetDialog.getButtonStyle()));

        result.append("</tr></table>");

        result.append("</td></tr></table>");

        result.append("</td>");
        result.append("</tr></table>");

        result.append("</td>");

        JSONObject additional = null;
        try {
            additional = getAdditionalGalleryInfo(
                cms,
                widgetDialog instanceof CmsDialog ? ((CmsDialog)widgetDialog).getParamResource() : null,
                widgetDialog.getMessages(),
                param);
        } catch (JSONException e) {
            LOG.error("Error parsing widget configuration", e);
        }
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

        return A_CmsAdeGalleryWidget.class.getName();
    }

    /**
     * @see org.opencms.widgets.A_CmsWidget#isCompactViewEnabled()
     */
    @Override
    public boolean isCompactViewEnabled() {

        return false;
    }

    /**
     * @see org.opencms.widgets.I_CmsADEWidget#isInternal()
     */
    public boolean isInternal() {

        return true;
    }

    /**
     * Returns additional widget information encapsulated in a JSON object.<p>
     * May be <code>null</code>.<p>
     *
     * @param cms an initialized instance of a CmsObject
     * @param resource the edited resource
     * @param messages the dialog messages
     * @param param the widget parameter to generate the widget for
     *
     * @return additional widget information
     *
     * @throws JSONException if something goes wrong generating the JSON object
     */
    protected abstract JSONObject getAdditionalGalleryInfo(
        CmsObject cms,
        String resource,
        CmsMessages messages,
        I_CmsWidgetParameter param) throws JSONException;

    /**
     * Returns the required gallery open parameters.
     *
     * @param cms an initialized instance of a CmsObject
     * @param messages the dialog messages
     * @param param the widget parameter to generate the widget for
     * @param resource the resource being edited
     * @param hashId the field id hash
     *
     * @return the gallery open parameters
     */
    protected Map<String, String> getGalleryOpenParams(
        CmsObject cms,
        CmsMessages messages,
        I_CmsWidgetParameter param,
        String resource,
        long hashId) {

        Map<String, String> result = new HashMap<String, String>();
        result.put(I_CmsGalleryProviderConstants.CONFIG_GALLERY_MODE, A_CmsAjaxGallery.MODE_WIDGET);
        result.put(I_CmsGalleryProviderConstants.CONFIG_GALLERY_STORAGE_PREFIX, getGalleryStoragePrefix());
        result.put(I_CmsGalleryProviderConstants.CONFIG_RESOURCE_TYPES, getGalleryTypes());
        if (param.getId() != null) {
            result.put(I_CmsGalleryProviderConstants.KEY_FIELD_ID, param.getId());
            // use javascript to read the current field value
            result.put(
                I_CmsGalleryProviderConstants.CONFIG_CURRENT_ELEMENT,
                "'+document.getElementById('" + param.getId() + "').getAttribute('value')+'");
        }
        result.put(I_CmsGalleryProviderConstants.KEY_HASH_ID, "" + hashId);
        // the edited resource
        if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(resource)) {
            result.put(I_CmsGalleryProviderConstants.CONFIG_REFERENCE_PATH, resource);
        }
        // the start up gallery path
        CmsGalleryWidgetConfiguration configuration = getWidgetConfiguration(cms, messages, param);
        if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(configuration.getStartup())) {
            result.put(I_CmsGalleryProviderConstants.CONFIG_GALLERY_PATH, configuration.getStartup());
        }
        // set gallery types if available
        if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(configuration.getGalleryTypes())) {
            result.put(I_CmsGalleryProviderConstants.CONFIG_GALLERY_TYPES, configuration.getGalleryTypes());
        }
        result.put(I_CmsGalleryProviderConstants.CONFIG_GALLERY_NAME, getGalleryName());
        return result;
    }

    /**
     * Gets the prefix for the key used to store the last selected gallery.<p>
     *
     * @return the key prefix
     */
    protected String getGalleryStoragePrefix() {

        return "";
    }

    /**
     * Returns the resource type names available within this gallery widget.<p>
     *
     * @return the resource type names
     */
    protected abstract String getGalleryTypes();

    /**
     * Returns the gallery widget configuration as a JSON object.<p>
     *
     * @param cms the cms context
     * @param schemaType the schema type
     * @param messages the messages
     * @param resource the edited resource
     * @param contentLocale the content locale
     *
     * @return  the gallery widget configuration
     */
    protected JSONObject getJSONConfig(
        CmsObject cms,
        A_CmsXmlContentValue schemaType,
        CmsMessages messages,
        CmsResource resource,
        Locale contentLocale) {

        JSONObject result = new JSONObject();
        try {
            for (Entry<String, String> paramEntry : getGalleryOpenParams(
                cms,
                messages,
                schemaType,
                cms.getSitePath(resource),
                0).entrySet()) {
                result.put(paramEntry.getKey(), paramEntry.getValue());
            }
            JSONObject additional = getAdditionalGalleryInfo(cms, cms.getSitePath(resource), messages, null);
            if (additional != null) {
                result.merge(additional, true, true);
            }
            result.put(I_CmsGalleryProviderConstants.CONFIG_LOCALE, contentLocale.toString());
            result.remove(I_CmsGalleryProviderConstants.CONFIG_CURRENT_ELEMENT);
        } catch (JSONException e) {
            LOG.error(e.getMessage(), e);
        }
        return result;
    }

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

        StringBuffer sb = new StringBuffer(128);
        sb.append("javascript:cmsOpenDialog('");

        // the gallery title
        sb.append(widgetDialog.getMessages().key(Messages.getButtonName(getGalleryName()))).append("', '");

        // the gallery path
        sb.append(OpenCms.getSystemInfo().getOpenCmsContext()).append(PATH_GALLERY_JSP);

        // set the content locale
        Locale contentLocale = widgetDialog.getLocale();
        try {
            I_CmsXmlContentValue value = (I_CmsXmlContentValue)param;
            contentLocale = value.getLocale();
        } catch (Exception e) {
            // may fail if widget is not opened from xml content editor, ignore
        }
        sb.append("?__locale=").append(contentLocale.toString());
        // add other open parameters
        for (Entry<String, String> paramEntry : getGalleryOpenParams(
            cms,
            widgetDialog.getMessages(),
            param,
            widgetDialog instanceof CmsDialog ? ((CmsDialog)widgetDialog).getParamResource() : null,
            hashId).entrySet()) {
            sb.append("&").append(paramEntry.getKey()).append("=").append(paramEntry.getValue());
        }
        sb.append("', '").append(param.getId()).append("', 488, 650); return false;");
        return sb.toString();
    }

    /**
     * Returns the javascript call to open the preview dialog.<p>
     *
     * @param widgetDialog the dialog where the widget is used on
     * @param id the field id
     *
     * @return the javascript call to open the preview dialog
     */
    protected String getOpenPreviewCall(I_CmsWidgetDialog widgetDialog, String id) {

        StringBuffer sb = new StringBuffer(64);
        sb.append("javascript:cmsOpenPreview('").append(widgetDialog.getMessages().key(Messages.GUI_BUTTON_PREVIEW_0));
        sb.append("', '").append(OpenCms.getSystemInfo().getOpenCmsContext());
        sb.append("', '").append(id);
        sb.append("'); return false;");
        return sb.toString();
    }

    /**
     * Returns the widget configuration.<p>
     *
     * @param cms an initialized instance of a CmsObject
     * @param messages the dialog where the widget is used on
     * @param param the widget parameter to generate the widget for
     *
     * @return the widget configuration
     */
    protected CmsGalleryWidgetConfiguration getWidgetConfiguration(
        CmsObject cms,
        CmsMessages messages,
        I_CmsWidgetParameter param) {

        if (m_widgetConfiguration == null) {
            m_widgetConfiguration = new CmsGalleryWidgetConfiguration(cms, messages, param, getConfiguration());
        }
        return m_widgetConfiguration;
    }

}
