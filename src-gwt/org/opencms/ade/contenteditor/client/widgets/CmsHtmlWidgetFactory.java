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

import org.opencms.ade.contenteditor.client.Messages;
import org.opencms.ade.contenteditor.widgetregistry.client.WidgetRegistry;
import org.opencms.gwt.client.I_CmsHasInit;
import org.opencms.gwt.client.util.CmsMessages;
import org.opencms.util.CmsStringUtil;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.user.client.Element;

/**
 * Factory to generate basic input widget.<p>
 */
public class CmsHtmlWidgetFactory implements I_WidgetFactory, I_CmsHasInit {

    /** The message bundle. */
    private static final CmsMessages MESSAGES = Messages.get();

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

        return new HalloWidget(element, generateOptionsForHallo(configuration));
    }

    /**
     * Generates the hallo editor options according to the configuration.<p>
     * 
     * @param configuration the widget configuration
     * 
     * @return the hallo options
     */
    private native JavaScriptObject generateOptionsForHallo(String configuration)/*-{
        var options = null;
        var messages = @org.opencms.ade.contenteditor.client.widgets.CmsHtmlWidgetFactory::MESSAGES;
        try {
            var config = @org.opencms.gwt.client.util.CmsDomUtil::parseJSON(Ljava/lang/String;)(configuration);
            options = {};
            if (config.toolbar_items) {
                for ( var i = 0; i < config.toolbar_items.length; i++) {
                    var item = config.toolbar_items[i];
                    if (item == 'undo') {
                        options.reundo = {
                            labelUndo : messages.@org.opencms.gwt.client.util.CmsMessages::key(Ljava/lang/String;)(@org.opencms.ade.contenteditor.client.Messages::GUI_HALLO_UNDO_0),
                            labelRedo : messages.@org.opencms.gwt.client.util.CmsMessages::key(Ljava/lang/String;)(@org.opencms.ade.contenteditor.client.Messages::GUI_HALLO_REDO_0)
                        }
                    }
                    if (item == 'alignleft') {
                        if (!options.justify) {
                            options.justify = {
                                formats : []
                            };
                        }
                        options.justify.formats
                                .push({
                                    command : "Left",
                                    label : messages.@org.opencms.gwt.client.util.CmsMessages::key(Ljava/lang/String;)(@org.opencms.ade.contenteditor.client.Messages::GUI_HALLO_LEFT_0)
                                });
                    }
                    if (item == 'aligncenter') {
                        if (!options.justify) {
                            options.justify = {
                                formats : []
                            };
                        }
                        options.justify.formats
                                .push({
                                    command : "Center",
                                    label : messages.@org.opencms.gwt.client.util.CmsMessages::key(Ljava/lang/String;)(@org.opencms.ade.contenteditor.client.Messages::GUI_HALLO_CENTER_0)
                                });
                    }
                    if (item == 'alignright') {
                        if (!options.justify) {
                            options.justify = {
                                formats : []
                            };
                        }
                        options.justify.formats
                                .push({
                                    command : "Right",
                                    label : messages.@org.opencms.gwt.client.util.CmsMessages::key(Ljava/lang/String;)(@org.opencms.ade.contenteditor.client.Messages::GUI_HALLO_RIGHT_0)
                                });
                    }
                    if (item == 'justify') {
                        if (!options.justify) {
                            options.justify = {
                                formats : []
                            };
                        }
                        options.justify.formats
                                .push({
                                    command : "Full",
                                    label : messages.@org.opencms.gwt.client.util.CmsMessages::key(Ljava/lang/String;)(@org.opencms.ade.contenteditor.client.Messages::GUI_HALLO_JUSTIFY_0),
                                    icon : "justify"
                                });
                    }
                    if (item == 'unorderedlist') {
                        if (!options.lists) {
                            options.lists = {
                                formats : []
                            };
                        }
                        options.lists.formats
                                .push({
                                    command : "Unordered",
                                    label : messages.@org.opencms.gwt.client.util.CmsMessages::key(Ljava/lang/String;)(@org.opencms.ade.contenteditor.client.Messages::GUI_HALLO_UNORDERED_LIST_0),
                                    icon : "ul"
                                });
                    }
                    if (item == 'orderedlist') {
                        if (!options.lists) {
                            options.lists = {
                                formats : []
                            };
                        }
                        options.lists.formats
                                .push({
                                    command : "Ordered",
                                    label : messages.@org.opencms.gwt.client.util.CmsMessages::key(Ljava/lang/String;)(@org.opencms.ade.contenteditor.client.Messages::GUI_HALLO_ORDERED_LIST_0),
                                    icon : "ol"
                                });
                    }
                    if (item == 'bold') {
                        if (!options.format) {
                            options.format = {
                                formats : []
                            };
                        }
                        options.format.formats
                                .push({
                                    command : "bold",
                                    label : messages.@org.opencms.gwt.client.util.CmsMessages::key(Ljava/lang/String;)(@org.opencms.ade.contenteditor.client.Messages::GUI_HALLO_BOLD_0)
                                });
                    }
                    if (item == 'italic') {
                        if (!options.format) {
                            options.format = {
                                formats : []
                            };
                        }
                        options.format.formats
                                .push({
                                    command : "italic",
                                    label : messages.@org.opencms.gwt.client.util.CmsMessages::key(Ljava/lang/String;)(@org.opencms.ade.contenteditor.client.Messages::GUI_HALLO_ITALIC_0)
                                });
                    }
                    if (item == 'underline') {
                        if (!options.format) {
                            options.format = {
                                formats : []
                            };
                        }
                        options.format.formats
                                .push({
                                    command : "underline",
                                    label : messages.@org.opencms.gwt.client.util.CmsMessages::key(Ljava/lang/String;)(@org.opencms.ade.contenteditor.client.Messages::GUI_HALLO_UNDERLINE_0)
                                });
                    }
                    if (item == 'strikethrough') {
                        if (!options.format) {
                            options.format = {
                                formats : []
                            };
                        }
                        options.format.formats
                                .push({
                                    command : "strikeThrough",
                                    label : messages.@org.opencms.gwt.client.util.CmsMessages::key(Ljava/lang/String;)(@org.opencms.ade.contenteditor.client.Messages::GUI_HALLO_STRIKE_THROUGH_0)
                                });
                    }
                    if (item == 'formatselect' && !config.block_formats) {
                        options.block = {
                            elements : [ 'h1', 'h2', 'h3', 'h4', 'h5', 'h6',
                                    'p', 'pre', 'address' ],
                            label : messages.@org.opencms.gwt.client.util.CmsMessages::key(Ljava/lang/String;)(@org.opencms.ade.contenteditor.client.Messages::GUI_HALLO_BLOCK_FORMATS_0)
                        };
                    }
                }
            }
            if (config.block_formats) {
                options.block = {
                    elements : config.block_formats.split(","),
                    label : messages.@org.opencms.gwt.client.util.CmsMessages::key(Ljava/lang/String;)(@org.opencms.ade.contenteditor.client.Messages::GUI_HALLO_BLOCK_FORMATS_0)
                };
            }
        } catch (e) {
            // nothing to do
        }
        return options;
    }-*/;

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
            var config = @org.opencms.gwt.client.util.CmsDomUtil::parseJSON(Ljava/lang/String;)(configuration);
            options = {
                skin_variant : 'contenteditor'
            };
            if (config.language) {
                options.language = config.language;
            }
            if (config.content_css) {
                options.content_css = config.content_css;
            }
            if (config.height) {
                options.editorHeight = config.height;
            }
            if (config.block_formats) {
                options.theme_advanced_blockformats = config.block_formats;
            }
            if (config.style_formats) {
                var temp = null;
                try {
                    temp = eval('(' + config.style_formats + ')');
                } catch (error) {
                    $wnd.alert("Could not parse WYSIWYG editor options: "
                            + error);
                }
                if (typeof temp != 'undefined' && temp != null) {
                    options.style_formats = temp;
                }
            }
            if (config.cmsGalleryEnhancedOptions) {
                options.cmsGalleryEnhancedOptions = config.cmsGalleryEnhancedOptions;
            }
            if (config.cmsGalleryUseThickbox) {
                options.cmsGalleryUseThickbox = config.cmsGalleryUseThickbox;
            }
            options.plugins = "autoresize,autolink,lists,pagebreak,style,layer,table,save,advhr,advimage,advlink,emotions,iespell,inlinepopups,insertdatetime,preview,media,searchreplace,print,contextmenu,paste,directionality,fullscreen,noneditable,visualchars,nonbreaking,xhtmlxtras,template,wordcount,advlist,-opencms";
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

                if (config.tinyMceOptions) {
                    for ( var tinyMceOptionKey in config.tinyMceOptions) {
                        options[tinyMceOptionKey] = config.tinyMceOptions[tinyMceOptionKey];
                    }
                }
            }

        } catch (e) {
            // nothing to do
        }

        return options;
    }-*/;
}
