/*
 * File   : $Source: /alkacon/cvs/opencms/src-modules/org/opencms/editors/fckeditor/CmsFCKEditorWidget.java,v $
 * Date   : $Date: 2009/07/01 15:46:37 $
 * Version: $Revision: 1.12 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (c) 2002 - 2009 Alkacon Software GmbH (http://www.alkacon.com)
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

package org.opencms.editors.fckeditor;

import org.opencms.file.CmsObject;
import org.opencms.i18n.CmsEncoder;
import org.opencms.main.OpenCms;
import org.opencms.util.CmsStringUtil;
import org.opencms.widgets.A_CmsHtmlWidget;
import org.opencms.widgets.CmsHtmlWidgetOption;
import org.opencms.widgets.I_CmsWidget;
import org.opencms.widgets.I_CmsWidgetDialog;
import org.opencms.widgets.I_CmsWidgetParameter;
import org.opencms.workplace.CmsWorkplace;
import org.opencms.workplace.editors.CmsEditor;
import org.opencms.workplace.editors.I_CmsEditorCssHandler;
import org.opencms.workplace.galleries.CmsAjaxDownloadGallery;
import org.opencms.workplace.galleries.CmsAjaxHtmlGallery;
import org.opencms.workplace.galleries.CmsAjaxImageGallery;
import org.opencms.workplace.galleries.CmsAjaxLinkGallery;
import org.opencms.workplace.galleries.CmsAjaxTableGallery;
import org.opencms.xml.types.I_CmsXmlContentValue;

import java.util.Iterator;
import java.util.List;

/**
 * Provides a widget that creates a rich input field using the "FCKeditor" component, for use on a widget dialog.<p>
 * 
 * For configuration options, have a look at the documentation of {@link CmsHtmlWidgetOption}.<p>
 *
 * @author Andreas Zahner
 * 
 * @version $Revision: 1.12 $ 
 * 
 * @since 6.1.7
 */
public class CmsFCKEditorWidget extends A_CmsHtmlWidget {

    /** Request parameter name for the tool bar configuration parameter. */
    public static final String PARAM_CONFIGURATION = "config";

    /**
     * Creates a new FCKeditor widget.<p>
     */
    public CmsFCKEditorWidget() {

        // empty constructor is required for class registration
        this("");
    }

    /**
     * Creates a new FCKeditor widget with the given configuration.<p>
     * 
     * @param configuration the configuration to use
     */
    public CmsFCKEditorWidget(CmsHtmlWidgetOption configuration) {

        super(configuration);
    }

    /**
     * Creates a new FCKeditor widget with the given configuration.<p>
     * 
     * @param configuration the configuration to use
     */
    public CmsFCKEditorWidget(String configuration) {

        super(configuration);
    }

    /**
     * Builds the font buttons of the editor widget: bold, italic, underline, strikethrough, subscript, superscript.<p>
     * 
     * @param toolbar the tool bar configuration defining the buttons to show
     * @param option the configured HTML widget options
     * 
     * @return <code>true</code> if at least one button was added to the tool bar, otherwise <code>false</code>
     */
    public static boolean buildFontButtons(StringBuffer toolbar, CmsHtmlWidgetOption option) {

        boolean showStyleBt = false;
        StringBuffer styleBt = new StringBuffer(64);
        if (!option.isButtonHidden("bold")) {
            showStyleBt = true;
            styleBt.append(",'Bold'");
        }
        if (!option.isButtonHidden("italic")) {
            showStyleBt = true;
            styleBt.append(",'Italic'");
        }
        if (!option.isButtonHidden("underline")) {
            showStyleBt = true;
            styleBt.append(",'Underline'");
        }
        if (!option.isButtonHidden("strikethrough")) {
            showStyleBt = true;
            styleBt.append(",'StrikeThrough'");
        }
        if (showStyleBt) {
            // append configured font buttons with a leading separator
            toolbar.append(",'-'").append(styleBt);
        }

        boolean showScriptBt = false;
        styleBt = new StringBuffer(32);
        if (!option.isButtonHidden("sub")) {
            showScriptBt = true;
            styleBt.append(",'Subscript'");
        }
        if (!option.isButtonHidden("super")) {
            showScriptBt = true;
            styleBt.append(",'Superscript'");
        }
        if (showScriptBt) {
            // append configured buttons with a leading separator
            toolbar.append(",'-'").append(styleBt);
        }

        return showStyleBt || showScriptBt;
    }

    /**
     * Builds the format buttons of the editor widget: left, center, right, justify, ordered list, unordered list, indent, outdent.<p>
     * 
     * @param toolbar the tool bar configuration defining the buttons to show
     * @param option the configured HTML widget options
     * 
     * @return <code>true</code> if at least one button was added to the tool bar, otherwise <code>false</code>
     */
    public static boolean buildFormatButtons(StringBuffer toolbar, CmsHtmlWidgetOption option) {

        // first block: alignment buttons
        boolean showAlignBt = false;
        StringBuffer alignBt = new StringBuffer(64);
        if (!option.isButtonHidden("alignleft")) {
            showAlignBt = true;
            alignBt.append("'JustifyLeft'");
        }
        if (!option.isButtonHidden("aligncenter")) {
            if (showAlignBt) {
                alignBt.append(",");
            }
            showAlignBt = true;
            alignBt.append("'JustifyCenter'");
        }
        if (!option.isButtonHidden("alignright")) {
            if (showAlignBt) {
                alignBt.append(",");
            }
            showAlignBt = true;
            alignBt.append("'JustifyRight'");
        }
        if (!option.isButtonHidden("justify")) {
            if (showAlignBt) {
                alignBt.append(",");
            }
            showAlignBt = true;
            alignBt.append("'JustifyFull'");
        }

        // second block: list buttons
        boolean showListBt = false;
        StringBuffer listBt = new StringBuffer(32);
        if (!option.isButtonHidden("orderedlist")) {
            showListBt = true;
            listBt.append("'OrderedList'");
        }
        if (!option.isButtonHidden("unorderedlist")) {
            if (showListBt) {
                listBt.append(",");
            }
            showListBt = true;
            listBt.append("'UnorderedList'");
        }

        // third block: indentation buttons
        boolean showIndBt = false;
        StringBuffer indBt = new StringBuffer(32);
        if (!option.isButtonHidden("outdent")) {
            showIndBt = true;
            indBt.append("'Outdent'");
        }
        if (!option.isButtonHidden("indent")) {
            if (showIndBt) {
                indBt.append(",");
            }
            showIndBt = true;
            indBt.append("'Indent'");
        }

        if (showAlignBt || showListBt || showIndBt) {
            // at least one button group is shown, append buttons to tool bar
            toolbar.append(",[");
            toolbar.append(alignBt);
            if (showListBt) {
                if (showAlignBt) {
                    // add button separator between alignment and list buttons
                    toolbar.append(",'-',");
                }
                toolbar.append(listBt);
            }
            if (showIndBt) {
                if (showAlignBt || showListBt) {
                    // add button separator before indentation buttons
                    toolbar.append(",'-',");
                }
                toolbar.append(indBt);
            }
            toolbar.append("]");
            return true;
        }

        // all buttons are hidden
        return false;
    }

    /**
     * Builds the tool bar button configuration String for miscellaneous buttons of the editor widget.<p>
     * 
     * @param toolbar the tool bar configuration defining the buttons to show
     * @param option the configured HTML widget options
     * @return <code>true</code> if at least one button was added to the tool bar, otherwise <code>false</code>
     */
    public static boolean buildMiscButtons(StringBuffer toolbar, CmsHtmlWidgetOption option) {

        boolean buttonRendered = false;

        if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(option.getConfiguration())) {
            // configuration String found, build buttons to show
            StringBuffer custom = new StringBuffer(512);

            // show source button if configured
            if (option.showSourceEditor()) {
                custom.append("\"Source\"");
                buttonRendered = true;
            }

            // show format selector if configured
            boolean showFormatSelect = false;
            if (option.showFormatSelect()) {
                if (buttonRendered) {
                    custom.append(",\"-\",");
                }
                custom.append("\"FontFormat\"");
                buttonRendered = true;
                showFormatSelect = true;
            }

            // show style selector if configured
            if (option.showStylesXml()) {
                if (!showFormatSelect && buttonRendered) {
                    custom.append(",\"-\",");
                } else if (buttonRendered) {
                    custom.append(",");
                }
                custom.append("\"Style\"");
                buttonRendered = true;
            }

            // build the link and/or anchor buttons
            boolean showLink = false;
            if (option.showLinkDialog()) {
                if (buttonRendered) {
                    custom.append(",");
                }
                custom.append("\"oc-link\"");
                buttonRendered = true;
                showLink = true;
            }
            if (option.showAnchorDialog()) {
                if (buttonRendered) {
                    custom.append(",");
                }
                custom.append("\"Anchor\"");
                buttonRendered = true;
                showLink = true;
            }
            if (showLink) {
                // append the unlink button if at least one link button is configured
                custom.append(", \"Unlink\"");
            }

            //build the gallery buttons
            boolean showGallery = false;
            StringBuffer galleryResult = new StringBuffer(8);
            if ((option.getDisplayGalleries().size() > 0) || option.showImageDialog()) {
                // show image button if configured, compatible to old image dialog
                if (option.getDisplayGalleries().contains(CmsAjaxImageGallery.GALLERYTYPE_NAME)
                    || option.showImageDialog()) {
                    if (galleryResult.length() > 0) {
                        galleryResult.append(", ");
                    }
                    galleryResult.append("\"OcmsImageGallery\"");
                    showGallery = true;
                }

                // show download gallery button if configured
                if (option.getDisplayGalleries().contains(CmsAjaxDownloadGallery.GALLERYTYPE_NAME)) {
                    if (galleryResult.length() > 0) {
                        galleryResult.append(", ");
                    }
                    galleryResult.append("\"OcmsDownloadGallery\"");
                    showGallery = true;
                }

                // show link gallery button if configured
                if (option.getDisplayGalleries().contains(CmsAjaxLinkGallery.GALLERYTYPE_NAME)) {
                    if (galleryResult.length() > 0) {
                        galleryResult.append(", ");
                    }
                    galleryResult.append("\"OcmsLinkGallery\"");
                    showGallery = true;
                }

                // show HTML gallery button if configured
                if (option.getDisplayGalleries().contains(CmsAjaxHtmlGallery.GALLERYTYPE_NAME)) {
                    if (galleryResult.length() > 0) {
                        galleryResult.append(", ");
                    }
                    galleryResult.append("\"OcmsHtmlGallery\"");
                    showGallery = true;
                }

                // show table gallery button if configured
                if (option.getDisplayGalleries().contains(CmsAjaxTableGallery.GALLERYTYPE_NAME)) {
                    if (galleryResult.length() > 0) {
                        galleryResult.append(", ");
                    }
                    galleryResult.append("\"OcmsTableGallery\"");
                    showGallery = true;
                }
            }

            if (showGallery) {
                // show the galleries
                if (buttonRendered) {
                    custom.append("],[");
                }
                custom.append(galleryResult);
                buttonRendered = true;
            }

            // show table button if configured
            if (option.showTableDialog()) {
                if (buttonRendered) {
                    custom.append(",\"-\",");
                }
                custom.append("\"Table\"");
                buttonRendered = true;
            }

            if (buttonRendered) {
                // insert grouping bracket if at least one button was rendered
                custom.insert(0, ",[");
                // append custom buttons to tool bar
                toolbar.append(custom);
            }

        }

        return buttonRendered;
    }

    /**
     * Returns the individual options for the format select box, if they were configured, otherwise an empty String.<p>
     * 
     * @param option the configured HTML widget options
     * 
     * @return the individual options for the format select box
     */
    public static String getFormatSelectOptionsConfiguration(CmsHtmlWidgetOption option) {

        if (CmsStringUtil.isNotEmpty(option.getFormatSelectOptions())) {
            // individual options are used, create configuration output
            return "FCKConfig.FontFormats = \"" + option.getFormatSelectOptions() + "\";";
        }
        return "";
    }

    /**
     * Returns if the given button is configured to be shown.<p>
     * 
     * @param buttonName the name of the button to check
     * @param widgetOptions the options List containing the button names to show
     * @return true if the given button is configured to be shown
     */
    protected static boolean showButton(String buttonName, List widgetOptions) {

        return (widgetOptions.contains(buttonName));
    }

    /**
     * @see org.opencms.widgets.I_CmsWidget#getDialogIncludes(org.opencms.file.CmsObject, org.opencms.widgets.I_CmsWidgetDialog)
     */
    public String getDialogIncludes(CmsObject cms, I_CmsWidgetDialog widgetDialog) {

        StringBuffer result = new StringBuffer(128);
        // general FCKeditor JS
        result.append(getJSIncludeFile(CmsWorkplace.getSkinUri() + "editors/fckeditor/fckeditor.js"));
        result.append("\n");
        // special FCKeditor widget functions
        result.append(getJSIncludeFile(CmsWorkplace.getSkinUri() + "components/widgets/fckeditor.js"));
        return result.toString();
    }

    /**
     * @see org.opencms.widgets.I_CmsWidget#getDialogInitCall(org.opencms.file.CmsObject, org.opencms.widgets.I_CmsWidgetDialog)
     */
    public String getDialogInitCall(CmsObject cms, I_CmsWidgetDialog widgetDialog) {

        // creates the FCKeditor instances
        return "\tinitFCKeditor();\n";
    }

    /**
     * @see org.opencms.widgets.I_CmsWidget#getDialogInitMethod(org.opencms.file.CmsObject, org.opencms.widgets.I_CmsWidgetDialog)
     */
    public String getDialogInitMethod(CmsObject cms, I_CmsWidgetDialog widgetDialog) {

        StringBuffer result = new StringBuffer(64);
        result.append("function initFCKeditor() {\n");
        // set time out for IE to avoid tool bar error message on direct publish button click
        result.append("\tif (navigator.userAgent.toLowerCase().indexOf(\"msie\") != -1) {\n");
        result.append("\t\tsetTimeout(\"generateEditors();\", 50);\n");
        result.append("\t} else {");
        result.append("\t\tgenerateEditors();\n");
        result.append("\t}\n");
        result.append("}\n");
        return result.toString();
    }

    /**
     * @see org.opencms.widgets.I_CmsWidget#getDialogWidget(org.opencms.file.CmsObject, org.opencms.widgets.I_CmsWidgetDialog, org.opencms.widgets.I_CmsWidgetParameter)
     */
    public String getDialogWidget(CmsObject cms, I_CmsWidgetDialog widgetDialog, I_CmsWidgetParameter param) {

        String id = param.getId();
        String value = param.getStringValue(cms);
        StringBuffer result = new StringBuffer(4096);

        result.append("<td class=\"xmlTd\">");

        result.append("<textarea class=\"xmlInput maxwidth\" name=\"ta_");
        result.append(id);
        result.append("\" id=\"ta_");
        result.append(id);
        result.append("\" style=\"height: ");
        result.append(getHtmlWidgetOption().getEditorHeight());
        result.append(";\" rows=\"20\" cols=\"60\">");
        result.append(CmsEncoder.escapeXml(value));
        result.append("</textarea>");
        result.append("<input type=\"hidden\" name=\"");
        result.append(id);
        result.append("\" id=\"");
        result.append(id);
        result.append("\" value=\"");
        result.append(CmsEncoder.encode(value));
        result.append("\">");

        // generate the special configuration object for the current editor widget
        result.append("<script type=\"text/javascript\">\n");
        result.append("var editor = new FCKeditor(\"ta_").append(id).append("\");\n");
        result.append("editor.BasePath = \"").append(CmsWorkplace.getSkinUri()).append("editors/fckeditor/\";\n");

        // set CSS style sheet for current editor widget if configured
        boolean cssConfigured = false;
        String cssPath = "";
        if (getHtmlWidgetOption().useCss()) {
            cssPath = getHtmlWidgetOption().getCssPath();
            // set the CSS path to null (the created configuration String passed to JS will not include this path then)
            getHtmlWidgetOption().setCssPath(null);
            cssConfigured = true;
        } else if (OpenCms.getWorkplaceManager().getEditorCssHandlers().size() > 0) {
            Iterator i = OpenCms.getWorkplaceManager().getEditorCssHandlers().iterator();
            try {
                // cast param to I_CmsXmlContentValue
                I_CmsXmlContentValue contentValue = (I_CmsXmlContentValue)param;
                // now extract the absolute path of the edited resource
                String editedResource = cms.getSitePath(contentValue.getDocument().getFile());
                while (i.hasNext()) {
                    I_CmsEditorCssHandler handler = (I_CmsEditorCssHandler)i.next();
                    if (handler.matches(cms, editedResource)) {
                        cssPath = handler.getUriStyleSheet(cms, editedResource);
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
            result.append("editor.Config[\"EditorAreaCSS\"] = \"");
            result.append(OpenCms.getLinkManager().substituteLink(cms, cssPath));
            result.append("\";\n");
        }

        // set styles XML for current editor widget if configured
        if (getHtmlWidgetOption().showStylesXml()) {
            result.append("editor.Config[\"StylesXmlPath\"] = \"");
            result.append(OpenCms.getLinkManager().substituteLink(cms, getHtmlWidgetOption().getStylesXmlPath()));
            result.append("\";\n");
            // set the styles XML path to a value that the JS will create the selector
            getHtmlWidgetOption().setStylesXmlPath("true");
        }

        // set full page mode for current editor widget if configured
        if (getHtmlWidgetOption().isFullPage()) {
            result.append("editor.Config[\"FullPage\"] = true;\n");
        }

        result.append("editor.Width = \"100%\";\n");
        result.append("editor.Height = \"").append(getHtmlWidgetOption().getEditorHeight()).append("\";\n");
        result.append("editor.ToolbarSet = \"OpenCmsWidget\";\n");

        // generate the special configuration JS call for the current dialog widget
        StringBuffer configJs = new StringBuffer(128);
        configJs.append(CmsEditor.PATH_EDITORS);
        configJs.append("fckeditor/configwidget.js");
        configJs.append("?");
        configJs.append(PARAM_CONFIGURATION);
        configJs.append("=");
        configJs.append(CmsHtmlWidgetOption.createConfigurationString(getHtmlWidgetOption()));
        result.append("editor.Config[\"CustomConfigurationsPath\"] = \"");
        result.append(OpenCms.getLinkManager().substituteLink(cms, configJs.toString()));
        result.append("\";\n");
        result.append("editorInstances[editorInstances.length] = editor;\n");
        result.append("contentFields[contentFields.length] = document.getElementById(\"").append(id).append("\");\n");
        result.append("</script>\n");

        result.append("</td>");

        return result.toString();
    }

    /**
     * @see org.opencms.widgets.I_CmsWidget#newInstance()
     */
    public I_CmsWidget newInstance() {

        return new CmsFCKEditorWidget(getHtmlWidgetOption());
    }

}