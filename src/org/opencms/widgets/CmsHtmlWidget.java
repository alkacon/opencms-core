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

import org.opencms.ade.configuration.CmsADEConfigData;
import org.opencms.ade.galleries.shared.I_CmsGalleryProviderConstants;
import org.opencms.file.CmsFile;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsRequestContext;
import org.opencms.file.CmsResource;
import org.opencms.file.CmsResourceFilter;
import org.opencms.i18n.CmsEncoder;
import org.opencms.i18n.CmsLocaleManager;
import org.opencms.i18n.CmsMessages;
import org.opencms.json.JSONException;
import org.opencms.json.JSONObject;
import org.opencms.main.CmsException;
import org.opencms.main.CmsLog;
import org.opencms.main.OpenCms;
import org.opencms.main.OpenCmsSpellcheckHandler;
import org.opencms.util.CmsJsonUtil;
import org.opencms.util.CmsMacroResolver;
import org.opencms.util.CmsStringUtil;
import org.opencms.workplace.editors.CmsEditorDisplayOptions;
import org.opencms.workplace.editors.I_CmsEditorCssHandler;
import org.opencms.xml.content.I_CmsXmlContentHandler.DisplayType;
import org.opencms.xml.types.A_CmsXmlContentValue;

import java.io.UnsupportedEncodingException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.apache.commons.logging.Log;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

/**
 * Provides a widget that creates a rich input field using the matching component, for use on a widget dialog.<p>
 *
 * The matching component is determined by checking the installed editors for the best matching component to use.<p>
 *
 * @since 6.0.1
 */
public class CmsHtmlWidget extends A_CmsHtmlWidget implements I_CmsADEWidget {

    /** Sitemap attribute key for configuring the TinyMCE JSON configuration. */
    public static final String ATTR_TEMPLATE_EDITOR_CONFIGFILE = "template.editor.configfile";

    /** Labels for the default block format options. */
    public static final Map<String, String> TINYMCE_DEFAULT_BLOCK_FORMAT_LABELS = Collections.unmodifiableMap(
        CmsStringUtil.splitAsMap(
            "p:Paragraph|address:Address|pre:Pre|h1:Header 1|h2:Header 2|h3:Header 3|h4:Header 4|h5:Header 5|h6:Header 6",
            "|",
            ":"));

    /** The log object for this class. */
    private static final Log LOG = CmsLog.getLog(CmsHtmlWidget.class);

    /** The editor widget to use depending on the current users settings, current browser and installed editors. */
    private I_CmsWidget m_editorWidget;

    /**
     * Creates a new html editing widget.<p>
     */
    public CmsHtmlWidget() {

        // empty constructor is required for class registration
        this("");
    }

    /**
     * Creates a new html editing widget with the given configuration.<p>
     *
     * @param configuration the configuration to use
     */
    public CmsHtmlWidget(String configuration) {

        super(configuration);
    }

    /**
     * Returns the WYSIWYG editor configuration as a JSON object.<p>
     *
     * @param widgetOptions the options for the wysiwyg widget
     * @param cms the OpenCms context
     * @param resource the edited resource
     * @param contentLocale the edited content locale
     *
     * @return the configuration
     */
    public static JSONObject getJSONConfiguration(
        CmsHtmlWidgetOption widgetOptions,
        CmsObject cms,
        CmsResource resource,
        Locale contentLocale) {

        JSONObject result = new JSONObject();

        CmsEditorDisplayOptions options = OpenCms.getWorkplaceManager().getEditorDisplayOptions();
        Properties displayOptions = options.getDisplayOptions(cms);
        try {
            if (options.showElement("gallery.enhancedoptions", displayOptions)) {
                result.put("cmsGalleryEnhancedOptions", true);
            }
            if (options.showElement("gallery.usethickbox", displayOptions)) {
                result.put("cmsGalleryUseThickbox", true);
            }
            if (widgetOptions.isAllowScripts()) {
                result.put("allowscripts", Boolean.TRUE);
            }
            result.put("fullpage", widgetOptions.isFullPage());
            List<String> toolbarItems = widgetOptions.getButtonBarShownItems();
            result.put("toolbar_items", toolbarItems);
            Locale workplaceLocale = OpenCms.getWorkplaceManager().getWorkplaceLocale(cms);
            String editorHeight = widgetOptions.getEditorHeight();
            if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(editorHeight)) {
                editorHeight = editorHeight.replaceAll("px", "");
                result.put("height", editorHeight);
            }
            // set CSS style sheet for current editor widget if configured
            boolean cssConfigured = false;
            String cssPath = "";
            if (widgetOptions.useCss()) {
                cssPath = widgetOptions.getCssPath();
                // set the CSS path to null (the created configuration String passed to JS will not include this path then)
                widgetOptions.setCssPath(null);
                cssConfigured = true;
            } else if (OpenCms.getWorkplaceManager().getEditorCssHandlers().size() > 0) {
                Iterator<I_CmsEditorCssHandler> i = OpenCms.getWorkplaceManager().getEditorCssHandlers().iterator();
                try {
                    String editedResourceSitePath = resource == null ? null : cms.getSitePath(resource);
                    while (i.hasNext()) {
                        I_CmsEditorCssHandler handler = i.next();
                        if (handler.matches(cms, editedResourceSitePath)) {
                            cssPath = handler.getUriStyleSheet(cms, editedResourceSitePath);
                            if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(cssPath)) {
                                cssConfigured = true;
                            }
                            break;
                        }
                    }
                } catch (Exception e) {
                    // ignore, CSS could not be set
                }
            }
            if (cssConfigured) {
                result.put("content_css", OpenCms.getLinkManager().substituteLink(cms, cssPath));
            }

            if (widgetOptions.showStylesFormat()) {
                try {
                    CmsFile file = cms.readFile(widgetOptions.getStylesFormatPath());
                    String characterEncoding = OpenCms.getSystemInfo().getDefaultEncoding();
                    result.put("style_formats", new String(file.getContents(), characterEncoding));
                } catch (CmsException cmsException) {
                    LOG.error("Can not open file:" + widgetOptions.getStylesFormatPath(), cmsException);
                } catch (UnsupportedEncodingException ex) {
                    LOG.error(ex);
                }
            }
            if (widgetOptions.isImportCss()) {
                result.put("importCss", true);
            }
            String formatSelectOptions = widgetOptions.getFormatSelectOptions();
            if (!CmsStringUtil.isEmpty(formatSelectOptions)
                && !widgetOptions.isButtonHidden(CmsHtmlWidgetOption.OPTION_FORMATSELECT)) {
                result.put("block_formats", getTinyMceBlockFormats(formatSelectOptions));
            }
            Boolean pasteText = Boolean.valueOf(
                OpenCms.getWorkplaceManager().getWorkplaceEditorManager().getEditorParameter(
                    cms,
                    "tinymce",
                    "paste_text"));
            JSONObject pasteOptions = new JSONObject();
            pasteOptions.put("paste_text_sticky_default", pasteText);
            pasteOptions.put("paste_text_sticky", pasteText);
            result.put("pasteOptions", pasteOptions);
            // if spell checking is enabled, add the spell handler URL
            if (OpenCmsSpellcheckHandler.isSpellcheckingEnabled()) {
                result.put(
                    "spellcheck_url",
                    OpenCms.getLinkManager().substituteLinkForUnknownTarget(
                        cms,
                        OpenCmsSpellcheckHandler.getSpellcheckHandlerPath()));

                result.put("spellcheck_language", contentLocale.getLanguage());
            }
            String typografLocale = CmsTextareaWidget.getTypografLocale(contentLocale);
            result.put("typograf_locale", typografLocale);
            String linkDefaultProtocol = widgetOptions.getLinkDefaultProtocol();
            if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(linkDefaultProtocol)) {
                result.put("link_default_protocol", linkDefaultProtocol);
            }

            String editorOptions = widgetOptions.getEditorConfigPath();
            editorOptions = getEditorConfigPath(cms, resource, widgetOptions);
            if (editorOptions != null) {
                try {
                    CmsResource editorOptionsRes = cms.readResource(editorOptions, CmsResourceFilter.IGNORE_EXPIRATION);
                    CmsFile editorOptionsFile = cms.readFile(editorOptionsRes);
                    String encoding = CmsLocaleManager.getResourceEncoding(cms, editorOptionsRes);
                    String contentAsString = new String(editorOptionsFile.getContents(), encoding);
                    JSONObject directOptions = new JSONObject(contentAsString);
                    // JSON may contain user-readable strings, which we may want to localize,
                    // but we also don't want to accidentally produce invalid JSON, so we recursively
                    // replace macros in string values occuring in the JSON
                    CmsMacroResolver resolver = new CmsMacroResolver();
                    resolver.setCmsObject(cms);
                    resolver.setMessages(OpenCms.getWorkplaceManager().getMessages(workplaceLocale));
                    JSONObject replacedOptions = CmsJsonUtil.mapJsonObject(directOptions, val -> {
                        if (val instanceof String) {
                            return resolver.resolveMacros((String)val);
                        } else {
                            return val;
                        }
                    });
                    result.put("directOptions", replacedOptions);
                } catch (Exception e) {
                    LOG.error(
                        "Error processing editor options from " + editorOptions + ": " + e.getLocalizedMessage(),
                        e);
                }
            }

        } catch (JSONException e) {
            LOG.error(e.getLocalizedMessage(), e);
        }
        return result;
    }

    /**
     * Gets the block format configuration string for TinyMCE from the configured format select options.<p>
     *
     * @param formatSelectOptions the format select options
     *
     * @return the block_formats configuration
     */
    public static String getTinyMceBlockFormats(String formatSelectOptions) {

        String[] options = formatSelectOptions.split(";");
        List<String> resultParts = Lists.newArrayList();
        for (String option : options) {
            String label = TINYMCE_DEFAULT_BLOCK_FORMAT_LABELS.get(option);
            if (label == null) {
                label = option;
            }
            resultParts.add(label + "=" + option);
        }
        String result = CmsStringUtil.listAsString(resultParts, ";");
        return result;
    }

    /**
     * Determines the TinyMCE configuration JSON path for the given widget configuration and edited resource.
     *
     * @param cms the CMS context
     * @param resource the edited resource
     * @param widgetOptions the widget configuration
     * @return
     */
    private static String getEditorConfigPath(CmsObject cms, CmsResource resource, CmsHtmlWidgetOption widgetOptions) {

        if (!CmsStringUtil.isEmptyOrWhitespaceOnly(widgetOptions.getEditorConfigPath())) {
            return widgetOptions.getEditorConfigPath();
        }
        String adeContextPath = (String)cms.getRequestContext().getAttribute(
            CmsRequestContext.ATTRIBUTE_ADE_CONTEXT_PATH);
        String pathToCheck = null;
        if (adeContextPath != null) {
            pathToCheck = adeContextPath;
        } else if (resource != null) {
            pathToCheck = resource.getRootPath();
        } else {
            return null;
        }
        CmsADEConfigData config = OpenCms.getADEManager().lookupConfigurationWithCache(cms, pathToCheck);
        String valueFromSitemapConfig = config.getAttribute(ATTR_TEMPLATE_EDITOR_CONFIGFILE, null);
        if (valueFromSitemapConfig != null) {
            valueFromSitemapConfig = valueFromSitemapConfig.trim();
            if ("none".equals(valueFromSitemapConfig)) {
                return null;
            }
            return valueFromSitemapConfig;
        } else {
            return null;
        }
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

        JSONObject result = getJSONConfiguration(cms, resource, contentLocale);
        try {
            addEmbeddedGalleryOptions(result, cms, schemaType, messages, resource, contentLocale);
        } catch (JSONException e) {
            LOG.error(e.getLocalizedMessage(), e);
        }
        return result.toString();
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

        return DisplayType.wide;
    }

    /**
     * @see org.opencms.widgets.I_CmsWidget#getDialogIncludes(org.opencms.file.CmsObject, org.opencms.widgets.I_CmsWidgetDialog)
     */
    @Override
    public String getDialogIncludes(CmsObject cms, I_CmsWidgetDialog widgetDialog) {

        return getEditorWidget(cms, widgetDialog).getDialogIncludes(cms, widgetDialog);
    }

    /**
     * @see org.opencms.widgets.I_CmsWidget#getDialogInitCall(org.opencms.file.CmsObject, org.opencms.widgets.I_CmsWidgetDialog)
     */
    @Override
    public String getDialogInitCall(CmsObject cms, I_CmsWidgetDialog widgetDialog) {

        return getEditorWidget(cms, widgetDialog).getDialogInitCall(cms, widgetDialog);
    }

    /**
     * @see org.opencms.widgets.I_CmsWidget#getDialogInitMethod(org.opencms.file.CmsObject, org.opencms.widgets.I_CmsWidgetDialog)
     */
    @Override
    public String getDialogInitMethod(CmsObject cms, I_CmsWidgetDialog widgetDialog) {

        return getEditorWidget(cms, widgetDialog).getDialogInitMethod(cms, widgetDialog);
    }

    /**
     * @see org.opencms.widgets.I_CmsWidget#getDialogWidget(org.opencms.file.CmsObject, org.opencms.widgets.I_CmsWidgetDialog, org.opencms.widgets.I_CmsWidgetParameter)
     */
    public String getDialogWidget(CmsObject cms, I_CmsWidgetDialog widgetDialog, I_CmsWidgetParameter param) {

        return getEditorWidget(cms, widgetDialog).getDialogWidget(cms, widgetDialog, param);
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

        return CmsHtmlWidget.class.getName();
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

        return new CmsHtmlWidget(getConfiguration());
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

        String[] values = formParameters.get(param.getId());
        if ((values != null) && (values.length > 0)) {
            String val = CmsEncoder.decode(values[0], CmsEncoder.ENCODING_UTF_8);
            param.setStringValue(cms, val);
        }
    }

    /**
     * Adds the configuration for embedded gallery widgets the the JSON object.<p>
     *
     * @param result the  JSON object to modify
     * @param cms the OpenCms context
     * @param schemaType the schema type
     * @param messages the messages
     * @param resource the edited resource
     * @param contentLocale the content locale
     *
     * @throws JSONException in case JSON manipulation fails
     */
    protected void addEmbeddedGalleryOptions(
        JSONObject result,
        CmsObject cms,
        A_CmsXmlContentValue schemaType,
        CmsMessages messages,
        CmsResource resource,
        Locale contentLocale)
    throws JSONException {

        CmsHtmlWidgetOption widgetOption = parseWidgetOptions(cms);
        String embeddedImageGalleryOptions = widgetOption.getEmbeddedConfigurations().get("imagegallery");
        String embeddedDownloadGalleryOptions = widgetOption.getEmbeddedConfigurations().get("downloadgallery");

        if (embeddedDownloadGalleryOptions != null) {
            CmsAdeDownloadGalleryWidget widget = new CmsAdeDownloadGalleryWidget();
            widget.setConfiguration(embeddedDownloadGalleryOptions);
            String downloadJsonString = widget.getConfiguration(
                cms,
                schemaType/*?*/,
                messages,
                resource,
                contentLocale);

            JSONObject downloadJsonObj = new JSONObject(downloadJsonString);
            filterEmbeddedGalleryOptions(downloadJsonObj);
            result.put("downloadGalleryConfig", downloadJsonObj);
        }

        if (embeddedImageGalleryOptions != null) {
            CmsAdeImageGalleryWidget widget = new CmsAdeImageGalleryWidget();
            widget.setConfiguration(embeddedImageGalleryOptions);
            String imageJsonString = widget.getConfiguration(cms, schemaType/*?*/, messages, resource, contentLocale);
            JSONObject imageJsonObj = new JSONObject(imageJsonString);
            filterEmbeddedGalleryOptions(imageJsonObj);
            result.put("imageGalleryConfig", imageJsonObj);
        }
    }

    /**
     * Returns the WYSIWYG editor configuration as a JSON object.<p>
     *
     * @param cms the OpenCms context
     * @param resource the edited resource
     * @param contentLocale the edited content locale
     *
     * @return the configuration
     */
    protected JSONObject getJSONConfiguration(CmsObject cms, CmsResource resource, Locale contentLocale) {

        return getJSONConfiguration(parseWidgetOptions(cms), cms, resource, contentLocale);
    }

    /**
     * Removes all keys from the given JSON object which do not directly result from the embedded gallery configuration strings.<p>
     *
     * @param json the JSON object to modify
     */
    private void filterEmbeddedGalleryOptions(JSONObject json) {

        Set<String> validKeys = Sets.newHashSet(
            Arrays.asList(
                I_CmsGalleryProviderConstants.CONFIG_GALLERY_TYPES,
                I_CmsGalleryProviderConstants.CONFIG_GALLERY_PATH,
                I_CmsGalleryProviderConstants.CONFIG_USE_FORMATS,
                I_CmsGalleryProviderConstants.CONFIG_IMAGE_FORMAT_NAMES,
                I_CmsGalleryProviderConstants.CONFIG_IMAGE_FORMATS));

        // delete all keys not listed above
        Set<String> toDelete = new HashSet<String>(Sets.difference(json.keySet(), validKeys));
        for (String toDeleteKey : toDelete) {
            json.remove(toDeleteKey);
        }
    }

    /**
     * Returns the editor widget to use depending on the current users settings, current browser and installed editors.<p>
     *
     * @param cms the current CmsObject
     * @param widgetDialog the dialog where the widget is used on
     * @return the editor widget to use depending on the current users settings, current browser and installed editors
     */
    private I_CmsWidget getEditorWidget(CmsObject cms, I_CmsWidgetDialog widgetDialog) {

        if (m_editorWidget == null) {
            // get HTML widget to use from editor manager
            String widgetClassName = OpenCms.getWorkplaceManager().getWorkplaceEditorManager().getWidgetEditor(
                cms.getRequestContext(),
                widgetDialog.getUserAgent());
            boolean foundWidget = true;
            if (CmsStringUtil.isEmpty(widgetClassName)) {
                // no installed widget found, use default text area to edit HTML value
                widgetClassName = CmsTextareaWidget.class.getName();
                foundWidget = false;
            }
            try {
                if (foundWidget) {
                    // get widget instance and set the widget configuration
                    Class<?> widgetClass = Class.forName(widgetClassName);
                    A_CmsHtmlWidget editorWidget = (A_CmsHtmlWidget)widgetClass.newInstance();
                    editorWidget.setConfiguration(getConfiguration());
                    m_editorWidget = editorWidget;
                } else {
                    // set the text area to display 15 rows for editing
                    Class<?> widgetClass = Class.forName(widgetClassName);
                    I_CmsWidget editorWidget = (I_CmsWidget)widgetClass.newInstance();
                    editorWidget.setConfiguration("15");
                    m_editorWidget = editorWidget;
                }
            } catch (Exception e) {
                // failed to create widget instance
                LOG.error(
                    Messages.get().container(Messages.LOG_CREATE_HTMLWIDGET_INSTANCE_FAILED_1, widgetClassName).key());
            }

        }
        return m_editorWidget;
    }
}
