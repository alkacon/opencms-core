/*
 * File   : $Source: /alkacon/cvs/opencms/src-modules/org/opencms/editors/fckeditor/CmsFCKEditorWidget.java,v $
 * Date   : $Date: 2006/12/08 16:25:47 $
 * Version: $Revision: 1.2.4.3 $
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
import org.opencms.workplace.galleries.A_CmsGallery;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Provides a widget that creates a rich input field using the "FCKeditor" component, for use on a widget dialog.<p>
 * 
 * For configuration options, have a look at the documentation of {@link CmsHtmlWidgetOption}.<p>
 *
 * @author Andreas Zahner
 * 
 * @version $Revision: 1.2.4.3 $ 
 * 
 * @since 6.1.7
 */
public class CmsFCKEditorWidget extends A_CmsHtmlWidget {

    /** Request parameter name for the toolbar configuration parameter. */
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
     * Builds the toolbar button configuration String for the OpenCms specific buttons of the editor widget.<p>
     * 
     * @param toolbar the toolbar configuration defining the buttons to show
     * @param widgetOptionsString the options String containing the button names to show
     * @return true if at least one button was added to the toolbar, otherwise false
     */
    public static boolean buildOpenCmsButtonRow(StringBuffer toolbar, String widgetOptionsString) {

        boolean buttonRendered = false;

        if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(widgetOptionsString)) {
            // configuration String found, build buttons to show
            CmsHtmlWidgetOption option = new CmsHtmlWidgetOption(widgetOptionsString);
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

            // build the gallery button row
            Map galleryMap = OpenCms.getWorkplaceManager().getGalleries();
            List galleries = new ArrayList(galleryMap.size());
            Map typeMap = new HashMap(galleryMap.size());

            Iterator i = galleryMap.entrySet().iterator();
            while (i.hasNext()) {
                Map.Entry entry = (Map.Entry)i.next();
                String key = (String)entry.getKey();
                A_CmsGallery currGallery = (A_CmsGallery)entry.getValue();
                galleries.add(currGallery);
                // put the type name to the type Map
                typeMap.put(currGallery, key);
            }

            // sort the found galleries by their order
            Collections.sort(galleries);

            StringBuffer galleryResult = new StringBuffer(8);
            boolean showGallery = false;
            for (int k = 0; k < galleries.size(); k++) {
                A_CmsGallery currGallery = (A_CmsGallery)galleries.get(k);
                String galleryType = (String)typeMap.get(currGallery);
                if (option.getDisplayGalleries().contains(galleryType)) {
                    // gallery is shown, build row configuration String
                    if (galleryResult.length() > 0) {
                        galleryResult.append(", ");
                    }
                    galleryResult.append("\"oc-");
                    galleryResult.append(galleryType);
                    galleryResult.append("\"");
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

            // show image button if configured
            if (option.showImageDialog()) {
                if (buttonRendered) {
                    custom.append(",\"-\",");
                }
                custom.append("\"OcmsImage\"");
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
                // append custom buttons to toolbar
                toolbar.append(custom);
            }

        }

        return buttonRendered;
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
        // set time out for IE to avoid toolbar error message on direct publish button click
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
        if (getHtmlWidgetOption().useCss()) {
            result.append("editor.Config[\"EditorAreaCSS\"] = \"");
            result.append(OpenCms.getLinkManager().substituteLink(cms, getHtmlWidgetOption().getCssPath()));
            result.append("\";\n");
            // set the css path to null (the created config String passed to JS will not include this path then)
            getHtmlWidgetOption().setCssPath(null);
        }

        // set styles XML for current editor widget if configured
        if (getHtmlWidgetOption().showStylesXml()) {
            result.append("editor.Config[\"StylesXmlPath\"] = \"");
            result.append(OpenCms.getLinkManager().substituteLink(cms, getHtmlWidgetOption().getStylesXmlPath()));
            result.append("\";\n");
            // set the styles XML path to a value that the JS will create the selector
            getHtmlWidgetOption().setStylesXmlPath("true");
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