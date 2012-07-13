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

package org.opencms.ade.contenteditor.client.widgets;

import com.alkacon.acacia.client.I_WidgetFactory;
import com.alkacon.acacia.client.widgets.FormWidgetWrapper;
import com.alkacon.acacia.client.widgets.HalloWidget;
import com.alkacon.acacia.client.widgets.I_EditWidget;
import com.alkacon.acacia.client.widgets.I_FormEditWidget;
import com.alkacon.acacia.client.widgets.TinyMCEWidget;

import org.opencms.ade.contenteditor.widgetregistry.client.WidgetRegistry;
import org.opencms.gwt.client.I_CmsHasInit;
import org.opencms.util.CmsStringUtil;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.user.client.Element;

/**
 * Factory to generate basic input widget.<p>
 */
public class CmsHtmlWidgetFactory implements I_WidgetFactory, I_CmsHasInit {

    /** The widget name. */
    private static final String WIDGET_NAME = "org.opencms.widgets.CmsHtmlWidget";

    /**
     * Initializes this class.<p>
     */
    public static void initClass() {

        WidgetRegistry.getInstance().registerWidgetFactory(WIDGET_NAME, new CmsHtmlWidgetFactory());
    }

    /**
     * @see com.alkacon.acacia.client.I_WidgetFactory#createFormWidget(java.lang.String)
     */
    public I_FormEditWidget createFormWidget(String configuration) {

        JavaScriptObject options = null;
        if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(configuration)) {
            options = generateOptionsForTiny(configuration);
        }
        return new FormWidgetWrapper(new TinyMCEWidget(options));
    }

    /**
     * @see com.alkacon.acacia.client.I_WidgetFactory#createInlineWidget(java.lang.String, com.google.gwt.user.client.Element)
     */
    public I_EditWidget createInlineWidget(String configuration, Element element) {

        return new HalloWidget(element);
    }

    /**
     * Generates the tinyMCE editor options according to the configuration.<p>
     * 
     * @param configuration the widget configuration
     * 
     * @return the tinyMCE options
     */
    private native JavaScriptObject generateOptionsForTiny(String configuration)/*-{

        var options = null;
        try {
            var config = $wnd.JSON && $wnd.JSON.parse(configuration)
                    || eval('(' + configuration + ')');
            options = {
                skin_variant : 'contenteditor'
            };
            if (config.language) {
                options.language = config.language;
            }
            if (config.content_css) {
                options.content_css = config.content_css;
            }
            if (config.block_formats) {
                options.theme_advanced_blockformats = config.block_format;
            }
            if (config.style_formats) {
                options.style_formats = config.styleFormats;
            }
            if (config.cmsGalleryEnhancedOptions) {
                options.cmsGalleryEnhancedOptions = config.cmsGalleryEnhancedOptions;
            }
            if (config.cmsGalleryUseThickbox) {
                options.cmsGalleryUseThickbox = config.cmsGalleryUseThickbox;
            }
            options.plugins = "autolink,lists,pagebreak,style,layer,table,save,advhr,advimage,advlink,emotions,iespell,inlinepopups,insertdatetime,preview,media,searchreplace,print,contextmenu,paste,directionality,fullscreen,noneditable,visualchars,nonbreaking,xhtmlxtras,template,wordcount,advlist,-opencms";
            if (config.fullpage) {
                options.plugins += ",fullpage";
            }

            if (config.toolbar_items) {
                // assemble the toolbar

                // translation map
                var BUTTON_TRANSLATION = {
                    alignleft : "justifyleft",
                    aligncenter : "justifycenter",
                    alignright : "justifyright",
                    justify : "justifyfull",
                    style : "styleselect",
                    paste : "paste,pastetext,pasteword",
                    find : "search",
                    unorderedlist : "bullist",
                    orderedlist : "numlist",
                    editorlink : "link",
                    source : "code",
                    subscript : "sub",
                    superscript : "sup",
                    specialchar : "charmap",
                    spellcheck : "iespell",
                    fitwindow : "fullscreen",
                    imagegallery : "OcmsImageGallery",
                    downloadgallery : "OcmsDownloadGallery",
                    linkgallery : "OcmsLinkGallery",
                    htmlgallery : "OcmsHtmlGallery",
                    tablegallery : "OcmsTableGallery",
                    link : "oc-link"
                };

                var toolbarGroup = "";
                var groupCount = 1;

                // iterate over all toolbar items and generate toobar groups
                for ( var i = 0; i < config.toolbar_items.length; i++) {
                    var item = config.toolbar_items[i];
                    // ignore duplicate items
                    if (item != config.toolbar_items[i - 1]) {
                        // check for an item translation
                        if (BUTTON_TRANSLATION[item]) {
                            item = BUTTON_TRANSLATION[item];
                        }
                        // |,[,],- are group separators indicating to add the group and start a new one
                        if (item == "|" || item == "[" || item == "]"
                                || item == "-"
                                || i == config.toolbar_items.length - 1) {
                            // don't add empty groups
                            if (toolbarGroup != "") {
                                options["theme_advanced_buttons" + groupCount] = toolbarGroup;
                                groupCount++;
                                toolbarGroup = "";
                            }
                        } else {
                            // add item to the group 
                            if (toolbarGroup != "") {
                                toolbarGroup += ",";
                            }
                            toolbarGroup += item;
                        }
                    }
                }

                // in case there are less than 4 groups, override the default ones
                for ( var i = groupCount; i < 5; i++) {
                    options["theme_advanced_buttons" + i] = "";
                }

            }

        } catch (e) {
            // nothing to do
        }

        return options;
    }-*/;
}
