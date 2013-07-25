
package org.opencms.editors.tinymce;

import org.opencms.file.CmsFile;
import org.opencms.file.CmsObject;
import org.opencms.i18n.CmsEncoder;
import org.opencms.json.JSONException;
import org.opencms.json.JSONObject;
import org.opencms.main.CmsException;
import org.opencms.main.CmsLog;
import org.opencms.main.OpenCms;
import org.opencms.util.CmsStringUtil;
import org.opencms.widgets.A_CmsHtmlWidget;
import org.opencms.widgets.CmsHtmlWidgetOption;
import org.opencms.widgets.I_CmsWidget;
import org.opencms.widgets.I_CmsWidgetDialog;
import org.opencms.widgets.I_CmsWidgetParameter;
import org.opencms.workplace.CmsWorkplace;
import org.opencms.workplace.editors.CmsEditorDisplayOptions;
import org.opencms.workplace.editors.CmsWorkplaceEditorConfiguration;
import org.opencms.workplace.editors.I_CmsEditorCssHandler;
import org.opencms.xml.types.I_CmsXmlContentValue;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;

/**
 * The TinyMCE implementation of the HTML widget.<p>
 */
public class CmsTinyMCEWidget extends A_CmsHtmlWidget {

    /** Path of the base content CSS. */
    public static final String BASE_CONTENT_CSS = "/system/workplace/editors/tinymce/base_content.css";

    /** The translation of the generic widget button names to TinyMCE specific button names. */
    public static final String BUTTON_TRANSLATION =
    /* Row 1*/
    "|newdocument:newdocument|bold:bold|italic:italic|underline:underline|strikethrough:strikethrough|alignleft:justifyleft"
        + "|aligncenter:justifycenter|alignright:justifyright|justify:justifyfull|style:styleselect|formatselect:formatselect"
        + "|fontselect:fontselect|fontsizeselect:fontsizeselect"
        /* Row 2*/
        + "|cut:cut|copy:copy|paste:paste,pastetext,pasteword|pastetext:pastetext|pasteword:pasteword|find:search|replace:replace|unorderedlist:bullist"
        + "|orderedlist:numlist|outdent:outdent|indent:indent|blockquote:blockquote|undo:undo|redo:redo|editorlink:link|unlink:unlink"
        + "|anchor:anchor|image:image|cleanup:cleanup|source:code|insertdate:insertdate|inserttime:inserttime|forecolor:forecolor|backcolor:backcolor"
        /* Row 3*/
        + "|table:table|hr:hr|removeformat:removeformat|visualaid:visualaid|subscript:sub|superscript:sup|specialchar:charmap"
        + "|emotions:emotions|spellcheck:iespell|media:media|advhr:advhr|print:print|ltr:ltr|rtl:rtl|fitwindow:fullscreen"
        /* Row 4*/
        + "|insertlayer:insertlayer|moveforward:moveforward|movebackward:movebackward|absolute:absolute|styleprops:styleprops|cite:cite"
        + "|abbr:abbr|acronym:acronym|del:del|ins:ins|attribs:attribs|visualchars:visualchars|nonbreaking:nonbreaking|template:template"
        + "|pagebreak:pagebreak|selectall:selectall|fullpage:fullpage|imagegallery:OcmsImageGallery|downloadgallery:OcmsDownloadGallery"
        + "|linkgallery:OcmsLinkGallery|htmlgallery:OcmsHtmlGallery|tablegallery:OcmsTableGallery|link:oc-link";

    /** The map containing the translation of the generic widget button names to TinyMCE specific button names. */
    public static final Map<String, String> BUTTON_TRANSLATION_MAP = CmsStringUtil.splitAsMap(
        BUTTON_TRANSLATION,
        "|",
        ":");

    /** Request parameter name for the tool bar configuration parameter. */
    public static final String PARAM_CONFIGURATION = "config";

    /** The log object for this class. */
    private static final Log LOG = CmsLog.getLog(org.opencms.editors.tinymce.CmsTinyMCEWidget.class);

    /**
     * Creates a new TinyMCE widget.<p>
     */
    public CmsTinyMCEWidget() {

        // empty constructor is required for class registration
        this("");
    }

    /**
     * Creates a new TinyMCE widget with the given configuration.<p>
     * 
     * @param configuration the configuration to use
     */
    public CmsTinyMCEWidget(CmsHtmlWidgetOption configuration) {

        super(configuration);
    }

    /**
     * Creates a new TinyMCE widget with the given configuration.<p>
     * 
     * @param configuration the configuration to use
     */
    public CmsTinyMCEWidget(String configuration) {

        super(configuration);
    }

    /**
     * @see org.opencms.widgets.I_CmsWidget#getDialogIncludes(org.opencms.file.CmsObject, org.opencms.widgets.I_CmsWidgetDialog)
     */
    @Override
    public String getDialogIncludes(CmsObject cms, I_CmsWidgetDialog widgetDialog) {

        StringBuilder result = new StringBuilder(128);
        // general TinyMCE JS
        result.append(getJSIncludeFile(CmsWorkplace.getSkinUri() + "editors/tinymce/jscripts/tinymce/tinymce.min.js"));
        result.append("\n");
        result.append(getJSIncludeFile(OpenCms.getLinkManager().substituteLinkForRootPath(
            cms,
            "/system/workplace/editors/tinymce/opencms_plugin.js")));
        result.append("\n");
        // special TinyMCE widget functions
        result.append(getJSIncludeFile(CmsWorkplace.getSkinUri() + "components/widgets/tinymce.js"));
        String cssUri = CmsWorkplace.getSkinUri() + "components/widgets/tinymce.css";
        result.append("<link type='text/css' rel='stylesheet' href='" + cssUri + "'>");
        return result.toString();
    }

    /**
     * @see org.opencms.widgets.I_CmsWidget#getDialogWidget(org.opencms.file.CmsObject, org.opencms.widgets.I_CmsWidgetDialog, org.opencms.widgets.I_CmsWidgetParameter)
     */
    public String getDialogWidget(CmsObject cms, I_CmsWidgetDialog widgetDialog, I_CmsWidgetParameter param) {

        String id = param.getId();
        String value = param.getStringValue(cms);
        StringBuilder result = new StringBuilder();

        result.append("<td class=\"cmsTinyMCE xmlTd\">");

        result.append("<textarea class=\"xmlInput maxwidth\" name=\"ta_");
        result.append(id);
        result.append("\" id=\"ta_");
        result.append(id);
        result.append("\" style=\"");
        result.append("\" rows=\"20\" cols=\"60\">");
        result.append(CmsEncoder.escapeXml(value));
        result.append("</textarea>");
        result.append("<input type=\"hidden\" name=\"");
        result.append(id);
        result.append("\" id=\"");
        result.append(id);
        result.append("\" value=\"");
        result.append(CmsEncoder.encode(value));
        result.append("\">");

        result.append("<script type=\"text/javascript\">\n");

        result.append("initTinyMCE(").append(getTinyMceConfiguration(cms, param)).append(");\n");
        result.append("contentFields[contentFields.length] = document.getElementById(\"").append(id).append("\");\n");
        result.append("</script>\n");
        result.append("</td>");

        return result.toString();
    }

    /**
     * @see org.opencms.widgets.I_CmsWidget#newInstance()
     */
    public I_CmsWidget newInstance() {

        return new CmsTinyMCEWidget(getHtmlWidgetOption());
    }

    /**
     * Returns the string representation of the tinyMCE options object.<p>
     * 
     * @param cms the OpenCms context
     * @param param the widget parameter
     * 
     * @return the string representation of the tinyMCE options object
     */
    private String getTinyMceConfiguration(CmsObject cms, I_CmsWidgetParameter param) {

        JSONObject result = new JSONObject();
        CmsEditorDisplayOptions options = OpenCms.getWorkplaceManager().getEditorDisplayOptions();
        Properties displayOptions = options.getDisplayOptions(cms);
        try {
            result.put("elements", "ta_" + param.getId());
            String editorHeight = getHtmlWidgetOption().getEditorHeight();
            if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(editorHeight)) {
                editorHeight = editorHeight.replaceAll("px", "");
                result.put("height", editorHeight);
            }
            if (options.showElement("gallery.enhancedoptions", displayOptions)) {
                result.put("cmsGalleryEnhancedOptions", true);
            }
            if (options.showElement("gallery.usethickbox", displayOptions)) {
                result.put("cmsGalleryUseThickbox", true);
            }
            CmsWorkplaceEditorConfiguration editorConfig = OpenCms.getWorkplaceManager().getWorkplaceEditorManager().getEditorConfiguration(
                "tinymce");
            Boolean pasteText = Boolean.valueOf(editorConfig.getParameters().get("paste_text"));
            result.put("paste_as_text", pasteText);

            result.put("fullpage", getHtmlWidgetOption().isFullPage());
            result.merge(getToolbarJson(), true, false);

            result.put("language", OpenCms.getWorkplaceManager().getWorkplaceLocale(cms).getLanguage());
            // set CSS style sheet for current editor widget if configured
            boolean cssConfigured = false;
            String cssPath = "";
            if (getHtmlWidgetOption().useCss()) {
                cssPath = getHtmlWidgetOption().getCssPath();
                // set the CSS path to null (the created configuration String passed to JS will not include this path then)
                getHtmlWidgetOption().setCssPath(null);
                cssConfigured = true;
            } else if (OpenCms.getWorkplaceManager().getEditorCssHandlers().size() > 0) {
                Iterator<I_CmsEditorCssHandler> i = OpenCms.getWorkplaceManager().getEditorCssHandlers().iterator();
                try {
                    // cast parameter to I_CmsXmlContentValue
                    I_CmsXmlContentValue contentValue = (I_CmsXmlContentValue)param;
                    // now extract the absolute path of the edited resource
                    CmsFile editedResource = contentValue.getDocument().getFile();
                    String editedResourceSitePath = editedResource == null ? null : cms.getSitePath(editedResource);
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
                    LOG.debug(e.getLocalizedMessage(), e);
                }
            }

            List<String> contentCssLinks = new ArrayList<String>();
            contentCssLinks.add(OpenCms.getLinkManager().substituteLink(cms, BASE_CONTENT_CSS));
            if (cssConfigured) {
                contentCssLinks.add(OpenCms.getLinkManager().substituteLink(cms, cssPath));
            }
            result.put("content_css", CmsStringUtil.listAsString(contentCssLinks, ","));
            if (getHtmlWidgetOption().showStylesFormat()) {
                try {
                    CmsFile file = cms.readFile(getHtmlWidgetOption().getStylesFormatPath());
                    String characterEncoding = OpenCms.getSystemInfo().getDefaultEncoding();
                    result.put("style_formats", new String(file.getContents(), characterEncoding));
                } catch (CmsException cmsException) {
                    LOG.error("Can not open file:" + getHtmlWidgetOption().getStylesFormatPath(), cmsException);
                } catch (UnsupportedEncodingException ex) {
                    LOG.error(ex);
                }
            }
            String formatSelectOptions = getHtmlWidgetOption().getFormatSelectOptions();
            if (!CmsStringUtil.isEmpty(formatSelectOptions)
                && !getHtmlWidgetOption().isButtonHidden(CmsHtmlWidgetOption.OPTION_FORMATSELECT)) {
                formatSelectOptions = StringUtils.replace(formatSelectOptions, ";", ",");
                result.put("block_formats", formatSelectOptions);
            }
            result.put("entity_encoding", "raw");
        } catch (JSONException e) {
            LOG.error(e.getLocalizedMessage(), e);
        }

        return result.toString();
    }

    /**
     * Builds the toolbar rows.<p>
     * 
     * @return the toolbar button rows configuration
     * 
     * @throws JSONException if something goes wrong manipulating the JSON object
     */
    private JSONObject getToolbarJson() throws JSONException {

        JSONObject result = new JSONObject();
        List<String> barItems = getHtmlWidgetOption().getButtonBarShownItems();
        List<List<String>> blocks = new ArrayList<List<String>>();
        blocks.add(new ArrayList<String>());
        String lastItem = null;
        List<String> processedItems = new ArrayList<String>();

        // translate buttons and eliminate adjacent separators 
        for (String barItem : barItems) {
            if (BUTTON_TRANSLATION_MAP.containsKey(barItem)) {
                barItem = BUTTON_TRANSLATION_MAP.get(barItem);
            }
            if (barItem.equals("[") || barItem.equals("]") || barItem.equals("-")) {
                barItem = "|";
                if ("|".equals(lastItem)) {
                    continue;
                }
            }
            if (barItem.indexOf(",") > -1) {
                for (String subItem : barItem.split(",")) {
                    processedItems.add(subItem);
                }
            } else {
                processedItems.add(barItem);
            }
            lastItem = barItem;
        }

        // remove leading or trailing '|' 
        if ((processedItems.size() > 0) && processedItems.get(0).equals("|")) {
            processedItems.remove(0);
        }

        if ((processedItems.size() > 0) && processedItems.get(processedItems.size() - 1).equals("|")) {
            processedItems.remove(processedItems.size() - 1);
        }

        // transform flat list into list of groups 
        for (String processedItem : processedItems) {
            blocks.get(blocks.size() - 1).add(processedItem);
            if ("|".equals(processedItem)) {
                blocks.add(new ArrayList<String>());
            }
        }

        // produce the TinyMCE toolbar options from the groups
        // we use TinyMCE's button rows as groups instead of rows and fix the layout using CSS.
        // This is because we want the button bars to wrap automatically when there is not enough space.
        // Using this method, the wraps can only occur between different blocks/rows. 
        String toolbar = "";
        for (List<String> block : blocks) {
            toolbar += CmsStringUtil.listAsString(block, " ") + " ";
        }
        result.put("toolbar", toolbar);

        return result;
    }
}
