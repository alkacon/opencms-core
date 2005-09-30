/*
 * File   : $Source: /alkacon/cvs/opencms/src-modules/org/opencms/editors/htmlarea/Attic/CmsHtmlAreaWidget.java,v $
 * Date   : $Date: 2005/09/30 15:09:30 $
 * Version: $Revision: 1.1.2.2 $
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

package org.opencms.editors.htmlarea;

import org.opencms.file.CmsObject;
import org.opencms.i18n.CmsEncoder;
import org.opencms.main.OpenCms;
import org.opencms.widgets.A_CmsHtmlWidget;
import org.opencms.widgets.CmsHtmlWidgetOption;
import org.opencms.widgets.I_CmsWidget;
import org.opencms.widgets.I_CmsWidgetDialog;
import org.opencms.widgets.I_CmsWidgetParameter;
import org.opencms.workplace.CmsWorkplace;
import org.opencms.workplace.galleries.A_CmsGallery;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Provides a widget that creates a rich input field using the "HtmlArea" component, for use on a widget dialog.<p>
 *
 * @author Alexander Kandzior 
 * @author Andreas Zahner
 * 
 * @version $Revision: 1.1.2.2 $ 
 * 
 * @since 6.0.0 
 */
public class CmsHtmlAreaWidget extends A_CmsHtmlWidget {
    
    /** VFS path to the available HtmlArea Locales. */
    public static final String HTMLAREA_LOCALES_VFS = CmsWorkplace.VFS_PATH_WORKPLACE + "resources/editors/htmlarea/lang/";

    /**
     * Creates a new html area editor widget.<p>
     */
    public CmsHtmlAreaWidget() {

        // empty constructor is required for class registration
        this("");
    }

    /**
     * Creates a new html area editor widget with the given configuration.<p>
     * 
     * @param configuration the configuration to use
     */
    public CmsHtmlAreaWidget(String configuration) {

        super(configuration);
    }
    
    /**
     * Creates a new html area editor widget with the given configuration.<p>
     * 
     * @param configuration the configuration to use
     */
    public CmsHtmlAreaWidget(CmsHtmlWidgetOption configuration) {

        super(configuration);
    }
    
    /**
     * Returns the Locale for which HtmlArea has localization data available.<p>
     * 
     * @param cms the initialized cmsobject
     * @param wantedLocale the preferred Locale
     * @return the Locale for which HtmlArea has localization data available
     */
    public static Locale getHtmlAreaLocale(CmsObject cms, Locale wantedLocale) {
                
        // check the Locale to use for editor localization
        if (!cms.existsResource(HTMLAREA_LOCALES_VFS + wantedLocale.toString() + ".js")) {
            boolean foundLocale = false;
            Iterator i = OpenCms.getLocaleManager().getDefaultLocales().iterator();
            // check the available default Locales
            while (i.hasNext()) {
                wantedLocale = (Locale)i.next();
                if (cms.existsResource(HTMLAREA_LOCALES_VFS + wantedLocale.toString() + ".js")) {
                    // found a valid Locale
                    foundLocale = true;
                    break;
                }
            }
            if (! foundLocale) {
                // did not find any matching Locale, fall back to english
                wantedLocale = Locale.ENGLISH;
            }
        }
        return wantedLocale;
    }

    /**
     * @see org.opencms.widgets.I_CmsWidget#getDialogIncludes(org.opencms.file.CmsObject, org.opencms.widgets.I_CmsWidgetDialog)
     */
    public String getDialogIncludes(CmsObject cms, I_CmsWidgetDialog widgetDialog) {
        
        StringBuffer result = new StringBuffer(16);
        result.append("<script type=\"text/javascript\">\n<!--\n");
        result.append("\tvar _editor_url = \"");
        result.append(CmsWorkplace.getSkinUri());
        result.append("editors/htmlarea/\";\n");
        result.append("\tvar _editor_lang = \"");
        result.append(getHtmlAreaLocale(cms, widgetDialog.getLocale()));
        result.append("\";\n");
        result.append("//-->\n</script>\n");
        // general HtmlArea JS
        result.append(getJSIncludeFile(CmsWorkplace.getSkinUri() + "editors/htmlarea/htmlarea.js"));
        result.append("\n");
        // special functions required for the OpenCms dialogs
        result.append(getJSIncludeFile(CmsWorkplace.getSkinUri() + "editors/htmlarea/htmlarea-ocms.js"));
        result.append("\n");
        // special HtmlArea widget functions
        result.append(getJSIncludeFile(CmsWorkplace.getSkinUri() + "components/widgets/htmlarea.js"));
        return result.toString();
    }

    /**
     * @see org.opencms.widgets.I_CmsWidget#getDialogInitCall(org.opencms.file.CmsObject, org.opencms.widgets.I_CmsWidgetDialog)
     */
    public String getDialogInitCall(CmsObject cms, I_CmsWidgetDialog widgetDialog) {

        // the timeout setting prevents IE from jumping to the last html area widget
        return "\tinitHtmlArea();\n";
    }

    /**
     * @see org.opencms.widgets.I_CmsWidget#getDialogInitMethod(org.opencms.file.CmsObject, org.opencms.widgets.I_CmsWidgetDialog)
     */
    public String getDialogInitMethod(CmsObject cms, I_CmsWidgetDialog widgetDialog) {

        StringBuffer result = new StringBuffer(8);
        result.append("function initHtmlArea() {\n");        
        result.append("\tinitHtmlAreas();\n");
        result.append(buildOpenCmsButtons(widgetDialog));
        result.append(buildOpenCmsButtonRow());
        result.append("\tgenerateHtmlAreas();\n");
        result.append("}\n");
        return result.toString();
    }

    /**
     * @see org.opencms.widgets.I_CmsWidget#getDialogWidget(org.opencms.file.CmsObject, org.opencms.widgets.I_CmsWidgetDialog, org.opencms.widgets.I_CmsWidgetParameter)
     */
    public String getDialogWidget(CmsObject cms, I_CmsWidgetDialog widgetDialog, I_CmsWidgetParameter param) {

        String id = param.getId();
        StringBuffer result = new StringBuffer(128);
      
        result.append("<td class=\"xmlTd\">");
        
        result.append("<textarea class=\"xmlInput maxwidth\" name=\"");
        result.append(id);
        result.append("\" id=\"");
        result.append(id);
        result.append("\" style=\"height: ");
        result.append(getHtmlWidgetOption().getEditorHeight());
        result.append(";\" wrap=\"virtual\">");
        result.append(CmsEncoder.escapeXml(param.getStringValue(cms)));
        result.append("</textarea>");
        
        result.append("</td>");

        return result.toString();
    }

    /**
     * @see org.opencms.widgets.I_CmsWidget#newInstance()
     */
    public I_CmsWidget newInstance() {

        return new CmsHtmlAreaWidget(getHtmlWidgetOption());
    }

    /**
     * @see org.opencms.widgets.I_CmsWidget#setEditorValue(org.opencms.file.CmsObject, java.util.Map, org.opencms.widgets.I_CmsWidgetDialog, org.opencms.widgets.I_CmsWidgetParameter)
     */
    public void setEditorValue(
        CmsObject cms,
        Map formParameters,
        I_CmsWidgetDialog widgetDialog,
        I_CmsWidgetParameter param) {

        String[] values = (String[])formParameters.get(param.getId());
        if ((values != null) && (values.length > 0)) {
            String val = CmsEncoder.decode(values[0], CmsEncoder.ENCODING_UTF_8);
            param.setStringValue(cms, val);
        }
    }
    
    /**
     * Returns the JavaScript to configure the OpenCms buttons for HtmlArea.<p>
     * 
     * @param cms the current users OpenCms context
     * @param widgetDialog the dialog where the widget is used on
     * @return the JavaScript to configure the OpenCms buttons for HtmlArea
     */
    private String buildOpenCmsButtons(I_CmsWidgetDialog widgetDialog) {

        StringBuffer result = new StringBuffer();

        // build the link button configuration
        result.append("\tconfig.registerButton(\"");
        result.append("oc-link");
        result.append("\", \"");
        result.append(widgetDialog.getMessages().key("button.linkto"));
        result.append("\", _editor_url + \"../../buttons/link.png");
        result.append("\", false, function(e) { setActiveEditor(e); openLinkDialog(\'");
        result.append(widgetDialog.getMessages().key("editor.message.noselection"));
        result.append("\'); });\n");

        // build the anchor button configuration
        result.append("\tconfig.registerButton(\"");
        result.append("oc-anchor");
        result.append("\", \"");
        result.append(widgetDialog.getMessages().key("button.anchor"));
        result.append("\", _editor_url + \"../../buttons/anchor.png");
        result.append("\", false, function(e) { setActiveEditor(e); openAnchorDialog(\'");
        result.append(widgetDialog.getMessages().key("editor.message.noselection"));
        result.append("\'); });\n");

        // build the gallery button configurations
        Iterator i = OpenCms.getWorkplaceManager().getGalleries().keySet().iterator();
        while (i.hasNext()) {
            String galleryType = (String)i.next();
            String galleryName = galleryType.replaceFirst("gallery", "");
            // create gallery button code
            result.append("\tconfig.registerButton(\"");
            result.append(galleryType);
            result.append("\", \"");
            result.append(widgetDialog.getMessages().key("button." + galleryName + "list"));
            result.append("\", _editor_url + \"/images/opencms/");
            result.append(galleryType);
            result.append(".gif\", false, function(e) { setActiveEditor(e); openGallery(\'");
            result.append(galleryType);
            result.append("\'); });\n");
        }

        return result.toString();
    }
    
    /**
     * Returns the configuration String for the gallery button row in HtmlArea.<p>
     * 
     * @return the html String for the gallery buttons
     */
    private String buildOpenCmsButtonRow() {

        StringBuffer result = new StringBuffer(16);
        
        result.append("\tconfig.toolbar = [\n");
        result.append("\t\t[\n");
        result.append("\t\t\t\"copy\", \"cut\", \"paste\", \"separator\",\n");
        result.append("\t\t\t");
        // show format block if configured
        if (getHtmlWidgetOption().showFormatSelect()) {
            result.append("\"formatblock\", \"space\", ");
        }
        result.append("\"bold\", \"italic\", \"underline\", \"separator\",\n");
        result.append("\t\t\t\"strikethrough\", \"subscript\", \"superscript\", \"separator\",\n");
        result.append("\t\t\t\"justifyleft\", \"justifycenter\", \"justifyright\", \"justifyfull\", \"separator\",\n");
        result.append("\t\t\t\"insertorderedlist\", \"insertunorderedlist\", \"outdent\", \"indent\"");
        
        // build the link buttons
        boolean showLink = false;
        StringBuffer custom = new StringBuffer(8);
        if (getHtmlWidgetOption().showLinkDialog()) {
            custom.append("\"oc-link\"");
            showLink = true;
        }
        if (getHtmlWidgetOption().showAnchorDialog()) {
            if (showLink) {
                custom.append(", ");
            }
            custom.append("\"oc-anchor\"");
            showLink = true;
        }
        if (showLink) {
            result.append(", \"separator\",\n\t\t\t");
            result.append(custom);
        }
        
        // build the gallery button row
        Map galleryMap = OpenCms.getWorkplaceManager().getGalleries();
        List galleries = new ArrayList(galleryMap.size());
        Map typeMap = new HashMap(galleryMap.size());
        
        Iterator i = galleryMap.keySet().iterator();
        while (i.hasNext()) {
            String key = (String)i.next();
            A_CmsGallery currGallery = (A_CmsGallery)galleryMap.get(key);
            galleries.add(currGallery);
            // put the type name to the type Map
            typeMap.put(currGallery, key);
        }
        
        // sort the found galleries by their order
        Collections.sort(galleries);
        
        StringBuffer galleryResult = new StringBuffer(8);
        boolean showGallery = false;
        for (int k=0; k<galleries.size(); k++) {
            A_CmsGallery currGallery = (A_CmsGallery)galleries.get(k);
            String galleryType = (String)typeMap.get(currGallery);
            if (getHtmlWidgetOption().showGalleryDialog(galleryType)) {
                // gallery is shown, build row configuration String
                if (galleryResult.length() > 0) {
                    galleryResult.append(", ");
                }
                galleryResult.append("\"" + galleryType + "\"");
                showGallery = true;
            }
        }
        
        if (showGallery) {
            result.append(", \"separator\",\n\t\t\t");
            result.append(galleryResult);
        }
        
        // show source button
        if (getHtmlWidgetOption().showSourceEditor()) {
            result.append(", \"separator\",\n\t\t\t\"htmlmode\"\n");
        }
        result.append("\t\t]\n");
        result.append("\t];");
        return result.toString();
    }
}