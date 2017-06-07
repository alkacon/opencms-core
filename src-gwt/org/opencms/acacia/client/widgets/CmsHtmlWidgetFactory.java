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

package org.opencms.acacia.client.widgets;

import org.opencms.acacia.client.I_CmsWidgetFactory;
import org.opencms.ade.contenteditor.client.Messages;
import org.opencms.ade.contenteditor.widgetregistry.client.WidgetRegistry;
import org.opencms.gwt.client.CmsCoreProvider;
import org.opencms.gwt.client.I_CmsHasInit;
import org.opencms.gwt.client.util.CmsMessages;
import org.opencms.util.CmsStringUtil;
import org.opencms.workplace.editors.CmsTinyMceToolbarHelper;

import java.util.ArrayList;
import java.util.List;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.JsArray;
import com.google.gwt.dom.client.Element;

/**
 * Factory to generate basic input widget.<p>
 */
public class CmsHtmlWidgetFactory implements I_CmsWidgetFactory, I_CmsHasInit {

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
     * Creates the TinyMCE toolbar config string from a Javascript config object.<p>
     *
     * @param jso a Javascript array of toolbar items
     *
     * @return the TinyMCE toolbar config string
     */
    protected static String createContextMenu(JavaScriptObject jso) {

        JsArray<?> jsItemArray = jso.<JsArray<?>> cast();
        List<String> jsItemList = new ArrayList<String>();
        for (int i = 0; i < jsItemArray.length(); i++) {
            jsItemList.add(jsItemArray.get(i).toString());
        }
        return CmsTinyMceToolbarHelper.getContextMenuEntries(jsItemList);
    }

    /**
     * Creates the TinyMCE toolbar config string from a Javascript config object.<p>
     *
     * @param jso a Javascript array of toolbar items
     *
     * @return the TinyMCE toolbar config string
     */
    protected static String createToolbar(JavaScriptObject jso) {

        JsArray<?> jsItemArray = jso.<JsArray<?>> cast();
        List<String> jsItemList = new ArrayList<String>();
        for (int i = 0; i < jsItemArray.length(); i++) {
            jsItemList.add(jsItemArray.get(i).toString());
        }
        return CmsTinyMceToolbarHelper.createTinyMceToolbarStringFromGenericToolbarItems(jsItemList);
    }

    /**
     * @see org.opencms.acacia.client.I_CmsWidgetFactory#createFormWidget(java.lang.String)
     */
    public I_CmsFormEditWidget createFormWidget(String configuration) {

        JavaScriptObject options = null;
        if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(configuration)) {
            options = generateOptionsForTiny(configuration);
        }
        return new CmsFormWidgetWrapper(new CmsTinyMCEWidget(options));
    }

    /**
     * @see org.opencms.acacia.client.I_CmsWidgetFactory#createInlineWidget(java.lang.String, com.google.gwt.dom.client.Element)
     */
    public I_CmsEditWidget createInlineWidget(String configuration, Element element) {

        JavaScriptObject options = null;
        if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(configuration)) {
            options = generateOptionsForTiny(configuration);
        }
        return new CmsTinyMCEWidget(element, options);
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
            var config = @org.opencms.gwt.client.util.CmsDomUtil::parseJSON(Ljava/lang/String;)(configuration);
            options = {
                entity_encoding : 'named',
                entities : '160,nbsp',
                // the browser call back function is defined in /system/workplace/editors/tinymce/opencms_plugin.js
                file_browser_callback : $wnd.cmsTinyMceFileBrowser
            };
            if (config.downloadGalleryConfig) {
                options.downloadGalleryConfig = config.downloadGalleryConfig;
            }

            if (config.imageGalleryConfig) {
                options.imageGalleryConfig = config.imageGalleryConfig;
            }

            if (config.language) {
                options.language = config.language;
            }
            if (config.content_css) {
                options.content_css = config.content_css;
            }
            options.importcss_append = true;
            if (config.importCss) {
                options.importcss_selector_filter = ""; // always matches
            } else {
                options.importcss_selector_filter = new $wnd.RegExp("$.^"); // never matches
            }
            if (config.height) {
                options.editorHeight = config.height;
            }
            if (config.block_formats) {
                options.block_formats = config.block_formats;
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
            options.plugins = "anchor,charmap,codemirror,importcss,textcolor,autolink,lists,pagebreak,layer,table,save,hr,image,link,emoticons,insertdatetime,preview,media,searchreplace,print,paste,directionality,fullscreen,noneditable,visualchars,nonbreaking,template,wordcount,advlist,spellchecker,-opencms";
            if (config.fullpage) {
                options.plugins += ",fullpage";
            }
            // add codemirror source view plugin configuration
            options.codemirror = {
                indentOnInit : true, // whether or not to indent code on init.
                path : this.@org.opencms.acacia.client.widgets.CmsHtmlWidgetFactory::getCodeMirrorPath()(), // path to CodeMirror distribution
                config : { // CodeMirror config object
                    lineNumbers : true
                }
            };
            if (config.allowscripts) {
                options.valid_elements = "*[*]";
                options.allow_script_urls = true;
            }

            if (config.toolbar_items) {
                toolbarGroup = @org.opencms.acacia.client.widgets.CmsHtmlWidgetFactory::createToolbar(Lcom/google/gwt/core/client/JavaScriptObject;)(config.toolbar_items);
                toolbarGroup += ",spellchecker";
                options.toolbar1 = toolbarGroup;
                var contextmenu = @org.opencms.acacia.client.widgets.CmsHtmlWidgetFactory::createContextMenu(Lcom/google/gwt/core/client/JavaScriptObject;)(config.toolbar_items);
                if (contextmenu != "") {
                    options.plugins += ",contextmenu";
                    options.contextmenu = contextmenu;
                }
                if (config.tinyMceOptions) {
                    options.paste_as_text = config.tinyMceOptions.paste_text_sticky_default ? true
                            : false;
                }
                if (config.spellcheck_url) {
                    options.spellchecker_language = config.spellcheck_language;
                    options.spellchecker_languages = config.spellcheck_language;
                    options.spellchecker_rpc_url = config.spellcheck_url;
                    options.spellchecker_callback = function(method, text,
                            success, failure) {
                        $wnd.tinymce.util.JSONRequest.sendRPC({
                            url : config.spellcheck_url,
                            method : "spellcheck",
                            params : {
                                lang : this.getLanguage(),
                                words : text.match(this.getWordCharPattern())
                            },
                            success : function(result) {
                                success(result);
                            },
                            error : function(error, xhr) {
                                failure("Spellcheck error:" + xhr.status);
                            }
                        });
                    };
                }
            }

        } catch (e) {
            // nothing to do
        }

        return options;
    }-*/;

    /**
     * Returns the code mirror path.<p>
     *
     * @return the code mirror resource path
     */
    private String getCodeMirrorPath() {

        return CmsStringUtil.joinPaths(CmsCoreProvider.get().getWorkplaceResourcesPrefix(), "editors/codemirror/dist/");
    }
}
