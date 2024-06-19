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

import org.opencms.main.OpenCms;
import org.opencms.util.CmsPair;
import org.opencms.util.CmsStringUtil;
import org.opencms.util.I_CmsRegexSubstitution;
import org.opencms.workplace.galleries.CmsAjaxDownloadGallery;
import org.opencms.workplace.galleries.CmsAjaxImageGallery;
import org.opencms.workplace.galleries.CmsAjaxLinkGallery;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.google.common.collect.Maps;

/**
 * An option of a HTML type widget.<p>
 *
 * Options can be defined for each element of the type <code>OpenCmsHtml</code> using the widget <code>HtmlWidget</code>.
 * They have to be placed in the annotation section of a XSD describing an XML content. The <code>configuration</code> attribute
 * in the <code>layout</code> node for the element must contain the activated options as a comma separated String value:<p>
 *
 * <code><layout element="Text" widget="HtmlWidget" configuration="height:400px,link,anchor,imagegallery,downloadgallery,formatselect,source" /></code><p>
 *
 * Available options are:
 * <ul>
 * <li><code>anchor</code>: the anchor dialog button</li>
 * <li><code>buttonbar:${button bar items, separated by ';'}</code>: an individual button bar configuration,
 *     see {@link #BUTTONBAR_DEFAULT} for an example.</li>
 * <li><code>css:/vfs/path/to/cssfile.css</code>: the absolute path in the OpenCms VFS to the CSS style sheet
 *     to use to render the contents in the editor (availability depends on the integrated editor)</li>
 * <li><code>formatselect</code>: the format selector for selecting text format like paragraph or headings</li>
 * <li><code>formatselect.options:${list of options, separated by ';'}</code>: the options that should be available in the format selector,
 *     e.g. <code>formatselect.options:p;h1;h2</code></li>
 * <li><code>fullpage</code>: the editor creates an entire HTML page code</li>
 * <li><code>${gallerytype}</code>: Shows a gallery dialog button, e.g. <code>imagegallery</code> displays
 *     the image gallery button or <code>downloadgallery</code> displays the download gallery button</li>
 * <li><code>height:${editorheight}</code>: the editor height, where the height can be specified in px or %, e.g. <code>400px</code></li>
 * <li><code>hidebuttons:${list of buttons to hide, separated by ';'}</code>: the buttons to hide that usually appear in
 *     the default button bar, e.g. <code>hidebuttons:bold;italic;underline;strikethrough</code> hides some formatting buttons</li>
 * <li><code>image</code>: the image dialog button (availability depends on the integrated editor)</li>
 * <li><code>link</code>: the link dialog button</li>
 * <li><code>source</code>: shows the source code toggle button(s)</li>
 * <li><code>stylesxml:/vfs/path/to/stylefile.xml</code>: the absolute path in the OpenCms VFS to the user defined
 *     styles that should be displayed in the style selector (availability depends on the integrated editor)</li>
 * <li><code>stylesformat:/vfs/path/to/stylefile.xml</code>: the absolute path in the OpenCms VFS to the user defined
 *     styles format that should be displayed in the style selector (availability depends on the integrated editor)</li>
 * <li><code>table</code>: the table dialog button (availability depends on the integrated editor)</li>
 * </ul>
 * Some things like the button bar items should be defined in the global widget configuration of the file <code>opencms-vfs.xml</code>.<p>
 *
 * @since 6.0.1
 */
public class CmsHtmlWidgetOption {

    /** The button bar end block indicator. */
    public static final String BUTTONBAR_BLOCK_END = "]";

    /** The button bar start block indicator. */
    public static final String BUTTONBAR_BLOCK_START = "[";

    /** The default editor widget button bar configuration. */
    public static final String BUTTONBAR_DEFAULT = "[;undo;redo;];"
        + "[;find;replace;];"
        + "[;copy;paste;pastetext;];"
        + "[;visualchars;-;ltr;rtl;];"
        + "[;removeformat;-;formatselect;-;style;];"
        + "[;bold;italic;underline;strikethrough;];"
        + "[;subscript;superscript;];"
        + "[;orderedlist;unorderedlist;];"
        + "[;alignleft;aligncenter;alignright;justify;typography;];"
        + "[;outdent;indent;-;blockquote;];"
        + "[;link;unlink;-;anchor;];"
        + "[;fontselect;-;fontsizeselect;];"
        + "[;backcolor;forecolor;];"
        + "[;imagegallery;downloadgallery;linkgallery;-;media;];"
        + "[;specialchar;emotions;];"
        + "[;table;-;hr;-;nonbreaking;];"
        // the next line of options seem to be broken or useless:
        + "[;editorlink;abbr;absolute;acronym;advhr;attribs;cite;cleanup;del;ins;insertdate;insertlayer;inserttime;movebackward;moveforward;newdocument;pagebreak;styleprops;template;visualaid;];"
        + "[;print;-;spellcheck;-;fitwindow;-;source;];";

    /** The default button bar configuration as List. */
    public static final List<String> BUTTONBAR_DEFAULT_LIST = CmsStringUtil.splitAsList(BUTTONBAR_DEFAULT, ';');

    /** The button bar separator. */
    public static final String BUTTONBAR_SEPARATOR = "-";

    /** The delimiter to use in the configuration String. */
    public static final String DELIMITER_OPTION = ",";

    /** The delimiter to use for separation of option values. */
    public static final char DELIMITER_VALUE = ';';

    /** The editor widget default maximum height to use. */
    public static final String EDITOR_DEFAULMAXTHEIGHT = "400px";

    /** Option for the "abbreviation" button. */
    public static final String OPTION_ABBR = "abbr";

    /** Option for the "absolute" button. */
    public static final String OPTION_ABSOLUTE = "absolute";

    /** Option for the "acronym" button. */
    public static final String OPTION_ACRONYM = "acronym";

    /** Option for the "advanced hr" button. */
    public static final String OPTION_ADVHR = "advhr";

    /** Allow scripts in source code editor. */
    public static final String OPTION_ALLOWSCRIPTS = "allowscripts";

    /** Option for the "anchor" dialog. */
    public static final String OPTION_ANCHOR = "anchor";

    /** Option for the "insert/edit attributes" button. */
    public static final String OPTION_ATTRIBS = "attribs";

    /** Option for the "background color" button. */
    public static final String OPTION_BACKCOLOR = "backcolor";

    /** Option for the "block quote" button. */
    public static final String OPTION_BLOCKQUOTE = "blockquote";

    /** Option for the "buttonbar" configuration. */
    public static final String OPTION_BUTTONBAR = "buttonbar:";

    /** Option for the "citation" button. */
    public static final String OPTION_CITE = "cite";

    /** Option for the "clean up messy code" button. */
    public static final String OPTION_CLEANUP = "cleanup";

    /** Option for the css style sheet VFS path to use in the widget area. */
    public static final String OPTION_CSS = "css:";

    /** Option for the "mark text as deletion" button. */
    public static final String OPTION_DEL = "del";

    /** If this is set, the contents of the path following the ':' will be interpreted as JSON and passed to TinyMCE directly. */
    public static final String OPTION_EDITORCONFIG = "editorconfig:";

    /** Option for the "editor link" dialog (editor specific). */
    public static final String OPTION_EDITORLINK = "editorlink";

    /** Option for the "emotions" button. */
    public static final String OPTION_EMOTIONS = "emotions";

    /** Option for the "find" dialog. */
    public static final String OPTION_FIND = "find";

    /** Option for the "font select" button. */
    public static final String OPTION_FONTSELECT = "fontselect";

    /** Option for the "font size" button. */
    public static final String OPTION_FONTSIZESELECT = "fontsizeselect";

    /** Option for the "text color" button. */
    public static final String OPTION_FORECOLOR = "forecolor";

    /** Option for the "formatselect" selector. */
    public static final String OPTION_FORMATSELECT = "formatselect";

    /** Option for the "formatselect" options selector. */
    public static final String OPTION_FORMATSELECT_OPTIONS = "formatselect.options:";

    /** Option for the "fullpage" editor variant. */
    public static final String OPTION_FULLPAGE = "fullpage";

    /** Option for the "height" configuration. */
    public static final String OPTION_HEIGHT = "height:";

    /** Option for the "hidebuttons" configuration. */
    public static final String OPTION_HIDEBUTTONS = "hidebuttons:";

    /** Option for the "hr" button. */
    public static final String OPTION_HR = "hr";

    /** Option for the "image" dialog. */
    public static final String OPTION_IMAGE = "image";

    /** Option to import styles from stylesheet into the style selector. */
    public static final String OPTION_IMPORTCSS = "importcss";

    /** Option for the "mark text as insertion" button. */
    public static final String OPTION_INS = "ins";

    /** Option for the "insert date" button. */
    public static final String OPTION_INSERTDATE = "insertdate";

    /** Option for the "insert layer" button. */
    public static final String OPTION_INSERTLAYER = "insertlayer";

    /** Option for the "insert time" button. */
    public static final String OPTION_INSERTTIME = "inserttime";

    /** Option for the "link" dialog. */
    public static final String OPTION_LINK = "link";

    /** Option for the default protocol for links */
    public static final String OPTION_LINKDEFAULTPROTOCOL = "linkdefaultprotocol:";

    /** Option for the "left to right text" button. */
    public static final String OPTION_LTR = "ltr";

    /** Option for the "insert media (flash, video, audio)" button. */
    public static final String OPTION_MEDIA = "media";

    /** Option for the "move backward (layer context)" button. */
    public static final String OPTION_MOVEBACKWARD = "movebackward";

    /** Option for the "move forward (layer context)" button. */
    public static final String OPTION_MOVEFORWARD = "moveforward";

    /** Option for the "new document (remove existing content)" button. */
    public static final String OPTION_NEWDOCUMENT = "newdocument";

    /** Option for the "non breaking white space" button. */
    public static final String OPTION_NONBREAKING = "nonbreaking";

    /** Option for the "page break" button. */
    public static final String OPTION_PAGEBREAK = "pagebreak";

    /** Option for the "paste from word" button. */
    public static final String OPTION_PASTEWORD = "pasteword";

    /** Option for the "print" button. */
    public static final String OPTION_PRINT = "print";

    /** Option for the "replace" dialog. */
    public static final String OPTION_REPLACE = "replace";

    /** Option for the "right to left text" button. */
    public static final String OPTION_RTL = "rtl";

    /** Option for the "source" code mode. */
    public static final String OPTION_SOURCE = "source";

    /** Option for the "special char dialog" button. */
    public static final String OPTION_SPECIALCHAR = "specialchar";

    /** Option for the "spell check" dialog. */
    public static final String OPTION_SPELLCHECK = "spellcheck";

    /** Option for the style select box. */
    public static final String OPTION_STYLE = "style";

    /** Option for the "edit CSS style" button. */
    public static final String OPTION_STYLEPROPS = "styleprops";

    /** Option for the styles XML VFS path to use in the widget area. */
    public static final String OPTION_STYLES = "stylesxml:";

    /** Option for the styles format VFS path to use in the widget area. */
    public static final String OPTION_STYLES_FORMAT = "stylesformat:";

    /** Option for the "table" dialog. */
    public static final String OPTION_TABLE = "table";

    /** Option for the "insert predefined template content" button. */
    public static final String OPTION_TEMPLATE = "template";

    /** Typography button. */ 
    public static final String OPTION_TYPOGRAPHY = "typography";

    /** Option for the "unlink" button. */
    public static final String OPTION_UNLINK = "unlink";

    /** Option for the "show/hide guidelines/invisible elements" button. */
    public static final String OPTION_VISUALAID = "visualaid";

    /** Option for the "show/hide visual control characters" button. */
    public static final String OPTION_VISUALCHARS = "visualchars";

    /** The optional buttons that can be additionally added to the button bar. */
    public static final String[] OPTIONAL_BUTTONS = {
        OPTION_ANCHOR,
        OPTION_EDITORLINK,
        OPTION_FIND,
        OPTION_FORMATSELECT,
        OPTION_IMAGE,
        OPTION_LINK,
        OPTION_REPLACE,
        OPTION_SOURCE,
        OPTION_SPELLCHECK,
        OPTION_STYLE,
        OPTION_TABLE,
        OPTION_UNLINK,
        OPTION_HR,
        OPTION_ABBR,
        OPTION_ABSOLUTE,
        OPTION_ACRONYM,
        OPTION_ADVHR,
        OPTION_ATTRIBS,
        OPTION_BACKCOLOR,
        OPTION_BLOCKQUOTE,
        OPTION_CITE,
        OPTION_CLEANUP,
        OPTION_DEL,
        OPTION_EMOTIONS,
        OPTION_FONTSELECT,
        OPTION_FONTSIZESELECT,
        OPTION_FORECOLOR,
        OPTION_INS,
        OPTION_INSERTDATE,
        OPTION_INSERTLAYER,
        OPTION_INSERTTIME,
        OPTION_LTR,
        OPTION_MEDIA,
        OPTION_MOVEBACKWARD,
        OPTION_MOVEFORWARD,
        OPTION_NEWDOCUMENT,
        OPTION_NONBREAKING,
        OPTION_PAGEBREAK,
        OPTION_PASTEWORD,
        OPTION_PRINT,
        OPTION_RTL,
        OPTION_STYLEPROPS,
        OPTION_SPECIALCHAR,
        OPTION_TEMPLATE,
        OPTION_VISUALAID,
        CmsAjaxImageGallery.GALLERYTYPE_NAME,
        CmsAjaxDownloadGallery.GALLERYTYPE_NAME,
        CmsAjaxLinkGallery.GALLERYTYPE_NAME};

    /** The optional buttons that can be additionally added to the button bar as list. */
    public static final List<String> OPTIONAL_BUTTONS_LIST = Arrays.asList(OPTIONAL_BUTTONS);

    /** Pattern used for matching embedded gallery configurations. */
    public static final Pattern PATTERN_EMBEDDED_GALLERY_CONFIG = Pattern.compile(
        "(?<![a-zA-Z0-9_])(imagegallery|downloadgallery)(\\{.*?\\})");

    /** Holds the global button bar configuration options to increase performance. */
    private static List<String> m_globalButtonBarOption;

    /** The additional buttons list. */
    private List<String> m_additionalButtons;

    /** Flag which controls whether scripts are allowed in the source code editor. */
    private boolean m_allowScripts;

    /** The button bar items. */
    private List<String> m_buttonBar;

    /** The button bar configuration options. */
    private List<String> m_buttonBarOption;

    /** The button bar options. */
    private String m_buttonBarOptionString;

    /** The configuration. */
    private String m_configuration;

    /** The CSS style sheet path. */
    private String m_cssPath;

    /** Path to an external TinyMCE JSON config file. */
    private String m_editorConfigPath;

    /** The editor height. */
    private String m_editorHeight;

    /** The embedded configuration  strings for galleries, if available. */
    private Map<String, String> m_embeddedConfigurations = Maps.newHashMap();

    /** The format select options. */
    private String m_formatSelectOptions;

    /** The full page flag. */
    private boolean m_fullPage;

    /** The hidden buttons. */
    private List<String> m_hiddenButtons;

    /** True if styles from stylesheet should be imported into the style selector. */
    private boolean m_importCss;

    /** The link default protocol */
    private String m_linkDefaultProtocol;

    /**
    private boolean m_allowScripts;

    /** The path for custom styles. */
    private String m_stylesFormatPath;

    /** The style XML path. */
    private String m_stylesXmlPath;

    /**
     * Creates a new empty HTML widget object object.<p>
     */
    public CmsHtmlWidgetOption() {

        // initialize the options
        init(null);
    }

    /**
     * Creates a new HTML widget object object, configured by the given configuration String.<p>
     *
     * @param configuration configuration String to parse
     */
    public CmsHtmlWidgetOption(String configuration) {

        // initialize the options
        init(configuration);
    }

    /**
     * Returns a HTML widget configuration String created from the given HTML widget option.<p>
     *
     * @param option the HTML widget options to create the configuration String for
     *
     * @return a select widget configuration String created from the given HTML widget option object
     */
    public static String createConfigurationString(CmsHtmlWidgetOption option) {

        StringBuffer result = new StringBuffer(512);
        boolean added = false;
        if (!option.getEditorHeight().equals(EDITOR_DEFAULMAXTHEIGHT)) {
            // append the height configuration
            result.append(OPTION_HEIGHT);
            result.append(option.getEditorHeight());
            added = true;
        }
        if (option.useCss()) {
            // append the CSS VFS path
            if (added) {
                result.append(DELIMITER_OPTION);
            }
            result.append(OPTION_CSS);
            result.append(option.getCssPath());
            added = true;
        }
        if (option.showStylesXml()) {
            // append the styles XML VFS path
            if (added) {
                result.append(DELIMITER_OPTION);
            }
            result.append(OPTION_STYLES);
            result.append(option.getStylesXmlPath());
            added = true;
        }
        if (!option.getAdditionalButtons().isEmpty()) {
            // append the additional buttons to show
            if (added) {
                result.append(DELIMITER_OPTION);
            }
            result.append(
                CmsStringUtil.collectionAsString(option.getAdditionalButtons(), String.valueOf(DELIMITER_OPTION)));
            added = true;
        }
        if (!option.getHiddenButtons().isEmpty()) {
            // append the buttons to hide from tool bar
            if (added) {
                result.append(DELIMITER_OPTION);
            }
            result.append(OPTION_HIDEBUTTONS);
            result.append(CmsStringUtil.collectionAsString(option.getHiddenButtons(), String.valueOf(DELIMITER_VALUE)));
            added = true;
        }
        if (CmsStringUtil.isNotEmpty(option.getButtonBarOptionString())) {
            // append the button bar definition
            if (added) {
                result.append(DELIMITER_OPTION);
            }
            result.append(OPTION_BUTTONBAR);
            result.append(option.getButtonBarOptionString());
            added = true;
        }
        if (option.isImportCss()) {
            if (added) {
                result.append(DELIMITER_OPTION);
            }
            result.append(OPTION_IMPORTCSS);
            added = true;
        }
        if (CmsStringUtil.isNotEmpty(option.getFormatSelectOptions())) {
            // append the format select option String
            if (added) {
                result.append(DELIMITER_OPTION);
            }
            result.append(OPTION_FORMATSELECT_OPTIONS);
            result.append(option.getFormatSelectOptions());
            added = true;
        }

        if (null != option.getEditorConfigPath()) {
            if (added) {
                result.append(DELIMITER_OPTION);
            }
            result.append(OPTION_EDITORCONFIG);
            result.append(option.getEditorConfigPath());
            added = true;
        }

        if (CmsStringUtil.isNotEmpty(option.getLinkDefaultProtocol())) {
            result.append(OPTION_LINKDEFAULTPROTOCOL);
            result.append(option.getLinkDefaultProtocol());
        }

        return result.toString();
    }

    /**
     * Parses and removes embedded gallery configuration strings.
     *
     * @param configuration the configuration string to parse
     *
     * @return a map containing both the string resulting from removing the embedded configurations, and the embedded configurations as a a map
     */
    public static CmsPair<String, Map<String, String>> parseEmbeddedGalleryOptions(String configuration) {

        final Map<String, String> galleryOptions = Maps.newHashMap();
        String resultConfig = CmsStringUtil.substitute(
            PATTERN_EMBEDDED_GALLERY_CONFIG,
            configuration,
            new I_CmsRegexSubstitution() {

                public String substituteMatch(String string, Matcher matcher) {

                    String galleryName = string.substring(matcher.start(1), matcher.end(1));
                    String embeddedConfig = string.substring(matcher.start(2), matcher.end(2));
                    galleryOptions.put(galleryName, embeddedConfig);
                    return galleryName;
                }
            });
        return CmsPair.create(resultConfig, galleryOptions);
    }

    /**
     * Returns the buttons to show additionally as list with button names.<p>
     *
     * @return the buttons to show additionally as list with button names
     */
    public List<String> getAdditionalButtons() {

        return m_additionalButtons;
    }

    /**
     * Returns the specific editor button bar string generated from the configuration.<p>
     *
     * The lookup map can contain translations for the button names, the separator and the block names.
     * The button bar will be automatically surrounded by block start and end items if they are not explicitly defined.<p>
     *
     * It may be necessary to write your own method to generate the button bar string for a specific editor widget.
     * In this case, use the method {@link #getButtonBarShownItems()} to get the calculated list of shown button bar items.<p>
     *
     * @param buttonNamesLookUp the lookup map with translations for the button names, the separator and the block names or <code>null</code>
     * @param itemSeparator the separator for the tool bar items
     * @return the button bar string generated from the configuration
     */
    public String getButtonBar(Map<String, String> buttonNamesLookUp, String itemSeparator) {

        return getButtonBar(buttonNamesLookUp, itemSeparator, true);
    }

    /**
     * Returns the specific editor button bar string generated from the configuration.<p>
     *
     * The lookup map can contain translations for the button names, the separator and the block names.<p>
     *
     * It may be necessary to write your own method to generate the button bar string for a specific editor widget.
     * In this case, use the method {@link #getButtonBarShownItems()} to get the calculated list of shown button bar items.<p>
     *
     * @param buttonNamesLookUp the lookup map with translations for the button names, the separator and the block names or <code>null</code>
     * @param itemSeparator the separator for the tool bar items
     * @param addMissingBlock flag indicating if the button bar should be automatically surrounded by a block if not explicitly defined
     * @return the button bar string generated from the configuration
     */
    public String getButtonBar(Map<String, String> buttonNamesLookUp, String itemSeparator, boolean addMissingBlock) {

        // first get the calculated button bar items
        List<String> buttonBar = getButtonBarShownItems();
        if (addMissingBlock) {
            // the button bar has to be surrounded by block items, check it
            if (!buttonBar.isEmpty()) {
                if (!buttonBar.get(0).equals(BUTTONBAR_BLOCK_START)) {
                    // add missing start block item
                    buttonBar.add(0, BUTTONBAR_BLOCK_START);
                }
                if (!buttonBar.get(buttonBar.size() - 1).equals(BUTTONBAR_BLOCK_END)) {
                    // add missing end block items
                    buttonBar.add(BUTTONBAR_BLOCK_END);
                }
            }
        }
        StringBuffer result = new StringBuffer(512);
        boolean isFirst = true;
        for (Iterator<String> i = buttonBar.iterator(); i.hasNext();) {
            String barItem = i.next();
            if (BUTTONBAR_BLOCK_START.equals(barItem)) {
                // start a block
                if (!isFirst) {
                    result.append(itemSeparator);
                }
                result.append(getButtonName(barItem, buttonNamesLookUp));
                // starting a block means also: next item is the first (of the block)
                isFirst = true;
            } else if (BUTTONBAR_BLOCK_END.equals(barItem)) {
                // end a block (there is no item separator added before ending the block)
                result.append(getButtonName(barItem, buttonNamesLookUp));
                isFirst = false;
            } else {
                // button or separator
                if (!isFirst) {
                    result.append(itemSeparator);
                }
                result.append(getButtonName(barItem, buttonNamesLookUp));
                isFirst = false;
            }
        }
        return result.toString();
    }

    /**
     * Returns the individual button bar configuration option.<p>
     *
     * @return the individual button bar configuration option
     */
    public List<String> getButtonBarOption() {

        if (m_buttonBarOption == null) {
            // use lazy initializing for performance reasons
            if (CmsStringUtil.isEmpty(getButtonBarOptionString())) {
                // no individual configuration defined, create empty list
                m_buttonBarOption = Collections.emptyList();
            } else {
                // create list of button bar options from configuration string
                m_buttonBarOption = CmsStringUtil.splitAsList(getButtonBarOptionString(), DELIMITER_VALUE, true);
            }
        }
        return m_buttonBarOption;
    }

    /**
     * Returns the individual button bar configuration option string.<p>
     *
     * @return the individual button bar configuration option string
     */
    public String getButtonBarOptionString() {

        return m_buttonBarOptionString;
    }

    /**
     * Returns the calculated button bar items, including blocks and separators, considering the current widget configuration.<p>
     *
     * Use this method to get the calculated list of button bar items if {@link #getButtonBar(Map, String)} can not
     * be used for a specific editor widget.<p>
     *
     * @return the calculated button bar items
     */
    public List<String> getButtonBarShownItems() {

        if (m_buttonBar == null) {
            // first get individual button bar configuration
            List<String> buttonBar = getButtonBarOption();
            if (buttonBar.isEmpty()) {
                // no specific button bar defined, try to get global configuration first
                if (m_globalButtonBarOption == null) {
                    // global configuration not yet parsed, check it now
                    String defaultConf = OpenCms.getXmlContentTypeManager().getWidgetDefaultConfiguration(
                        CmsHtmlWidget.class.getName());
                    if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(defaultConf)
                        && defaultConf.contains(OPTION_BUTTONBAR)) {
                        // found a global configuration containing a button bar definition, parse it
                        CmsHtmlWidgetOption option = new CmsHtmlWidgetOption(defaultConf);
                        // set global configuration in static member
                        m_globalButtonBarOption = option.getButtonBarOption();
                    } else {
                        // no global configuration present, set static member to empty list
                        m_globalButtonBarOption = Collections.emptyList();
                    }
                }
                if (m_globalButtonBarOption.isEmpty()) {
                    // no global button bar configuration found, use default button bar
                    buttonBar = BUTTONBAR_DEFAULT_LIST;
                } else {
                    // found a global configuration containing a button bar definition, use it
                    buttonBar = m_globalButtonBarOption;
                }
            }

            List<String> result = new ArrayList<String>(buttonBar.size());
            int lastSep = -1;
            int lastBlock = -1;
            boolean buttonInBlockAdded = false;
            boolean buttonSinceSepAdded = false;
            for (Iterator<String> i = buttonBar.iterator(); i.hasNext();) {
                String barItem = i.next();
                if (BUTTONBAR_BLOCK_START.equals(barItem)) {
                    // start a block
                    if ((lastSep != -1) && (lastSep == (result.size() - 1))) {
                        // remove last separator before block start
                        result.remove(lastSep);
                    }
                    lastBlock = result.size();
                    lastSep = -1;
                    buttonInBlockAdded = false;
                    buttonSinceSepAdded = false;
                    result.add(BUTTONBAR_BLOCK_START);
                } else if (BUTTONBAR_BLOCK_END.equals(barItem)) {
                    // end a block
                    if (lastBlock != -1) {
                        // block has been started
                        if (lastSep == (result.size() - 1)) {
                            // remove last separator before block end
                            result.remove(lastSep);
                        }
                        //now check if there are items in it
                        if (buttonInBlockAdded) {
                            // block has items, add end
                            result.add(BUTTONBAR_BLOCK_END);
                        } else {
                            // block has no items, remove block start ite,
                            result.remove(lastBlock);
                        }
                        lastBlock = -1;
                        lastSep = -1;
                        buttonInBlockAdded = false;
                        buttonSinceSepAdded = false;
                    }
                } else if (BUTTONBAR_SEPARATOR.equals(barItem)) {
                    // insert a separator depending on preconditions
                    if (buttonSinceSepAdded) {
                        lastSep = result.size();
                        result.add(BUTTONBAR_SEPARATOR);
                        buttonSinceSepAdded = false;
                    }
                } else {
                    // insert a button depending on preconditions
                    if (getHiddenButtons().contains(barItem)) {
                        // skip hidden buttons
                        continue;
                    }
                    if (OPTIONAL_BUTTONS_LIST.contains(barItem)) {
                        // check optional buttons
                        if (CmsAjaxImageGallery.GALLERYTYPE_NAME.equals(barItem)) {
                            // special handling of image button to keep compatibility
                            if (!(getAdditionalButtons().contains(barItem)
                                || getAdditionalButtons().contains(OPTION_IMAGE))) {
                                // skip image gallery as it is not defined as additional button
                                continue;
                            }
                        } else if (OPTION_UNLINK.equals(barItem)) {
                            // special handling of unlink button to show only if anchor, editor link or link button are active
                            if (!(getAdditionalButtons().contains(OPTION_LINK)
                                || getAdditionalButtons().contains(OPTION_EDITORLINK)
                                || getAdditionalButtons().contains(OPTION_ANCHOR))) {
                                // skip unlink button because no link buttons are defined as additional buttons
                                continue;
                            }
                        } else if (OPTION_STYLE.equals(barItem)) {
                            boolean showStyles = getAdditionalButtons().contains(barItem)
                                || (getStylesFormatPath() != null)
                                || (getStylesXmlPath() != null);
                            if (!showStyles) {
                                continue;
                            }
                        } else if (!getAdditionalButtons().contains(barItem)) {
                            // skip all optional buttons that are not defined
                            continue;
                        }
                    }
                    result.add(barItem);
                    buttonSinceSepAdded = true;
                    if (lastBlock != -1) {
                        buttonInBlockAdded = true;
                    }
                }
            }
            m_buttonBar = result;
        }
        return m_buttonBar;
    }

    /**
     * Returns the original configuration String that was used to initialize the HTML widget options.<p>
     *
     * @return the original configuration String
     */
    public String getConfiguration() {

        return m_configuration;
    }

    /**
     * Returns the CSS style sheet VFS path to use in the widget area.<p>
     *
     * @return the CSS style sheet VFS path to use in the widget area
     */
    public String getCssPath() {

        return m_cssPath;
    }

    /**
     * Gets the path of a JSON file containing options to be passed directly into TinyMCE.
     *
     * @return the path of a JSON with direct TinyMCE options
     */
    public String getEditorConfigPath() {

        return m_editorConfigPath;
    }

    /**
     * Returns the widget editor height.<p>
     *
     * @return the widget editor height
     */
    public String getEditorHeight() {

        return m_editorHeight;
    }

    /**
     * Gets the embedded gallery configurations.<p>
     *
     * @return the embedded gallery configurations
     */
    public Map<String, String> getEmbeddedConfigurations() {

        return m_embeddedConfigurations;
    }

    /**
     * Returns the options for the format select box as String.<p>
     *
     * @return the options for the format select box as String
     */
    public String getFormatSelectOptions() {

        return m_formatSelectOptions;
    }

    /**
     * Returns the buttons to hide as list with button names.<p>
     *
     * @return the buttons to hide as list with button names
     */
    public List<String> getHiddenButtons() {

        return m_hiddenButtons;
    }

    /**
     * Returns the link default protocol to use when inserting/editing links via the link dialog.
     *
     * @return the link default protocol to use when inserting/editing links via the link dialog
     */
    public String getLinkDefaultProtocol() {

        return m_linkDefaultProtocol;
    }

    /**
     * Returns the styles format VFS path to use in the widget area.<p>
     *
     * @return the styles XML format path to use in the widget area
     */
    public String getStylesFormatPath() {

        return m_stylesFormatPath;
    }

    /**
     * Returns the styles XML VFS path to use in the widget area.<p>
     *
     * @return the styles XML VFS path to use in the widget area
     */
    public String getStylesXmlPath() {

        return m_stylesXmlPath;
    }

    /**
     * Initializes the widget options from the given configuration String.<p>
     *
     * @param configuration the configuration String
     */
    public void init(String configuration) {

        // initialize the members
        m_additionalButtons = new ArrayList<String>(OPTIONAL_BUTTONS_LIST.size());
        m_configuration = configuration;
        m_editorHeight = EDITOR_DEFAULMAXTHEIGHT;
        m_hiddenButtons = new ArrayList<String>();
        // initialize the widget options
        parseOptions(configuration);
    }

    /**
     * Returns true if scripts should be allowed in the source code editor.<p>
     *
     * @return true if scripts should be allowed in the source code editor
     */
    public boolean isAllowScripts() {

        return m_allowScripts;
    }

    /**
     * Returns if the button with the given name should be additionally shown.<p>
     *
     * @param buttonName the button name to check
     *
     * @return <code>true</code> if the button with the given name should be additionally shown, otherwise <code>false</code>
     */
    public boolean isButtonAdditional(String buttonName) {

        return getAdditionalButtons().contains(buttonName);
    }

    /**
     * Returns if the button with the given name should be hidden.<p>
     *
     * @param buttonName the button name to check
     *
     * @return <code>true</code> if the button with the given name should be hidden, otherwise <code>false</code>
     */
    public boolean isButtonHidden(String buttonName) {

        return getHiddenButtons().contains(buttonName);
    }

    /**
     * Returns if the button with the given name is optional.<p>
     *
     * @param buttonName the button name to check
     *
     * @return <code>true</code> if the button with the given name is optional, otherwise <code>false</code>
     */
    public boolean isButtonOptional(String buttonName) {

        return OPTIONAL_BUTTONS_LIST.contains(buttonName);
    }

    /**
     * Returns if the editor should be used in full page mode.<p>
     *
     * @return true if the editor should be used in full page mode, otherwise false
     */
    public boolean isFullPage() {

        return m_fullPage;
    }

    /**
     * Return true if the content stylesheet's styles should be imported into the style selector.<p>
     *
     * @return true if the content stylesheet's styles should be imported into the style selector
     */
    public boolean isImportCss() {

        return m_importCss;
    }

    /**
     * Sets the buttons to show additionally as list with button names.<p>
     *
     * @param buttons the buttons to show additionally as list with button names
     */
    public void setAdditionalButtons(List<String> buttons) {

        m_additionalButtons = buttons;
    }

    /**
     * Sets the individual button bar configuration option.<p>
     *
     * @param buttonBar the individual button bar configuration option
     */
    public void setButtonBarOption(List<String> buttonBar) {

        m_buttonBarOption = buttonBar;
    }

    /**
     * Sets the individual button bar configuration option string.<p>
     *
     * @param buttonBar the individual button bar configuration option string
     */
    public void setButtonBarOptionString(String buttonBar) {

        m_buttonBarOptionString = buttonBar;
    }

    /**
     * Sets the CSS style sheet VFS path to use in the widget area.<p>
     *
     * @param cssPath the CSS style sheet VFS path to use in the widget area
     */
    public void setCssPath(String cssPath) {

        m_cssPath = cssPath;
    }

    /**
     * Sets the path for a file containing JSON options to be passed directly into TinyMCE.
     *
     * @param optionJsonPath the path of a JSON file
     */
    public void setEditorConfigPath(String optionJsonPath) {

        m_editorConfigPath = optionJsonPath;
    }

    /**
     * Sets the widget editor height.<p>
     *
     * @param editorHeight the widget editor height
     */
    public void setEditorHeight(String editorHeight) {

        m_editorHeight = editorHeight;
    }

    /**
     * Sets the options for the format select box as String.<p>
     *
     * @param formatSelectOptions the options for the format select box as String
     */
    public void setFormatSelectOptions(String formatSelectOptions) {

        m_formatSelectOptions = formatSelectOptions;
    }

    /**
     * Sets if the editor should be used in full page mode.<p>
     *
     * @param fullPage true if the editor should be used in full page mode, otherwise false
     */
    public void setFullPage(boolean fullPage) {

        m_fullPage = fullPage;
    }

    /**
     * Sets the buttons to hide as list with button names.<p>
     *
     * @param buttons the buttons to hide as list with button names
     */
    public void setHiddenButtons(List<String> buttons) {

        m_hiddenButtons = buttons;
    }

    /**
     * Set the link default protocol to use when inserting/editing links via the link dialog
     *
     * @param linkDefaultProtocol
     *            the link default protocol to use when inserting/editing links via the link dialog
     */
    public void setLinkDefaultProtocol(String linkDefaultProtocol) {

        m_linkDefaultProtocol = linkDefaultProtocol;
    }

    /**
     * Sets the styles format VFS path to use in the widget area.<p>
     *
     * @param stylesFormatPath the styles XML VFS path to use in the widget area
     */
    public void setStylesFormatPath(String stylesFormatPath) {

        m_stylesFormatPath = stylesFormatPath;
    }

    /**
     * Sets the styles XML VFS path to use in the widget area.<p>
     *
     * @param stylesXmlPath the styles XML VFS path to use in the widget area
     */
    public void setStylesXmlPath(String stylesXmlPath) {

        m_stylesXmlPath = stylesXmlPath;
    }

    /**
     * Returns true if the anchor dialog button should be available.<p>
     *
     * @return if the anchor dialog button should be available
     */
    public boolean showAnchorDialog() {

        return getAdditionalButtons().contains(OPTION_ANCHOR);
    }

    /**
     * Returns true if the format selector should be available.<p>
     *
     * @return if the format selector should be available
     */
    public boolean showFormatSelect() {

        return getAdditionalButtons().contains(OPTION_FORMATSELECT);
    }

    /**
     * Returns true if the specified gallery type dialog button is shown.<p>
     *
     * @param galleryType the gallery type to check
     * @return true if the specified gallery type dialog button is shown, otherwise false
     */
    public boolean showGalleryDialog(String galleryType) {

        return getAdditionalButtons().contains(galleryType);
    }

    /**
     * Returns true if the image dialog button should be available.<p>
     *
     * @return if the image dialog button should be available
     */
    public boolean showImageDialog() {

        return getAdditionalButtons().contains(OPTION_IMAGE);
    }

    /**
     * Returns true if the link dialog button should be available.<p>
     *
     * @return if the link dialog button should be available
     */
    public boolean showLinkDialog() {

        return getAdditionalButtons().contains(OPTION_LINK);
    }

    /**
     * Returns true if the source code button should be available.<p>
     *
     * @return if the source code button should be available
     */
    public boolean showSourceEditor() {

        return getAdditionalButtons().contains(OPTION_SOURCE);
    }

    /**
     * Returns true if the styles format selector should be available.<p>
     *
     * @return if the styles format selector should be available
     */
    public boolean showStylesFormat() {

        return CmsStringUtil.isNotEmpty(getStylesFormatPath());
    }

    /**
     * Returns true if the styles selector should be available.<p>
     *
     * @return if the styles selector should be available
     */
    public boolean showStylesXml() {

        return CmsStringUtil.isNotEmpty(getStylesXmlPath());
    }

    /**
     * Returns true if the table dialog button should be available.<p>
     *
     * @return if the table dialog button should be available
     */
    public boolean showTableDialog() {

        return getAdditionalButtons().contains(OPTION_TABLE);
    }

    /**
     * Returns true if the widget editor should use a defined CSS style sheet.<p>
     *
     * @return if the widget editor should use a defined CSS style sheet
     */
    public boolean useCss() {

        return CmsStringUtil.isNotEmpty(getCssPath());
    }

    /**
     * Adds a button to the list of defined additional buttons.<p>
     *
     * @param buttonName the button name to add
     */
    protected void addAdditionalButton(String buttonName) {

        m_additionalButtons.add(buttonName);
    }

    /**
     * Returns the real button name matched with the look up map.<p>
     *
     * If no value is found in the look up map, the button name is returned unchanged.<p>
     *
     * @param barItem the button bar item name to look up
     * @param buttonNamesLookUp the look up map containing the button names and/or separator name to use
     * @return the translated button name
     */
    protected String getButtonName(String barItem, Map<String, String> buttonNamesLookUp) {

        String result = barItem;
        if (buttonNamesLookUp != null) {
            String translatedName = buttonNamesLookUp.get(barItem);
            if (CmsStringUtil.isNotEmpty(translatedName)) {
                result = translatedName;
            }
        }
        return result;
    }

    /**
     * Parses the given configuration String.<p>
     *
     * @param configuration the configuration String to parse
     */
    protected void parseOptions(String configuration) {

        if (CmsStringUtil.isNotEmpty(configuration)) {

            CmsPair<String, Map<String, String>> simplifiedStringAndGalleryOptions = parseEmbeddedGalleryOptions(
                configuration);
            configuration = simplifiedStringAndGalleryOptions.getFirst();
            m_embeddedConfigurations = simplifiedStringAndGalleryOptions.getSecond();

            List<String> options = CmsStringUtil.splitAsList(configuration, DELIMITER_OPTION, true);
            Iterator<String> i = options.iterator();
            while (i.hasNext()) {
                String option = i.next();
                // check which option is defined
                if (option.startsWith(OPTION_FORMATSELECT_OPTIONS)) {
                    // the format select options
                    option = option.substring(OPTION_FORMATSELECT_OPTIONS.length());
                    setFormatSelectOptions(option);
                } else if (option.startsWith(OPTION_HEIGHT)) {
                    // the editor height
                    option = option.substring(OPTION_HEIGHT.length());
                    if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(option)) {
                        setEditorHeight(option);
                    }
                } else if (option.startsWith(OPTION_HIDEBUTTONS)) {
                    // buttons to hide from the tool bar
                    option = option.substring(OPTION_HIDEBUTTONS.length());
                    setHiddenButtons(CmsStringUtil.splitAsList(option, DELIMITER_VALUE, true));
                } else if (option.startsWith(OPTION_CSS)) {
                    // the editor CSS
                    option = option.substring(OPTION_CSS.length());
                    setCssPath(option);
                } else if (option.startsWith(OPTION_STYLES)) {
                    // the editor styles XML path
                    option = option.substring(OPTION_STYLES.length());
                    setStylesXmlPath(option);
                } else if (option.startsWith(OPTION_STYLES_FORMAT)) {
                    // the editor styles format path
                    option = option.substring(OPTION_STYLES_FORMAT.length());
                    setStylesFormatPath(option);
                } else if (option.startsWith(OPTION_BUTTONBAR)) {
                    // the button bar definition string
                    option = option.substring(OPTION_BUTTONBAR.length());
                    setButtonBarOptionString(option);
                } else if (option.startsWith(OPTION_EDITORCONFIG)) {
                    option = option.substring(OPTION_EDITORCONFIG.length());
                    setEditorConfigPath(option);
                } else if (option.startsWith(OPTION_IMPORTCSS)) {
                    m_importCss = true;
                } else if (option.startsWith(OPTION_ALLOWSCRIPTS)) {
                    m_allowScripts = true;
                } else if (option.startsWith(OPTION_LINKDEFAULTPROTOCOL)) {
                    // the link default protocol
                    option = option.substring(OPTION_LINKDEFAULTPROTOCOL.length());
                    if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(option)) {
                        setLinkDefaultProtocol(option);
                    }
                } else {
                    // check if option describes an additional button
                    if (OPTIONAL_BUTTONS_LIST.contains(option)) {
                        addAdditionalButton(option);
                    }
                }
            }
        }
    }
}