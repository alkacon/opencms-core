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

package org.opencms.workplace.editors;

import org.opencms.util.CmsStringUtil;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * This class is used to help constructing the TinyMCE toolbar configuration, both for client-side and server-side code.<p>
 */
public final class CmsTinyMceToolbarHelper {

    /** The translation of the generic widget button names to TinyMCE specific button names. */
    public static final String BUTTON_TRANSLATION =
    /* Row 1*/
    "|newdocument:newdocument|bold:bold|italic:italic|underline:underline|strikethrough:strikethrough|alignleft:alignleft"
        + "|aligncenter:aligncenter|alignright:alignright|justify:alignjustify|style:styleselect|formatselect:formatselect"
        + "|fontselect:fontselect|fontsizeselect:fontsizeselect"
        /* Row 2*/
        + "|cut:cut|copy:copy|paste:paste|pastetext:pastetext|find:searchreplace|replace:searchreplace|unorderedlist:bullist"
        + "|orderedlist:numlist|outdent:outdent|indent:indent|blockquote:blockquote|undo:undo|redo:redo|editorlink:link|unlink:unlink"
        + "|anchor:anchor|image:image|cleanup:cleanup|source:code|insertdate:insertdate|inserttime:inserttime|forecolor:forecolor|backcolor:backcolor"
        /* Row 3*/
        + "|table:table|hr:hr|removeformat:removeformat|visualaid:visualaid|subscript:subscript|superscript:superscript|specialchar:charmap"
        + "|emotions:emoticons|spellcheck:spellchecker|media:media|print:print|ltr:ltr|rtl:rtl|fitwindow:fullscreen"
        /* Row 4*/
        + "|insertlayer:insertlayer|moveforward:moveforward|movebackward:movebackward|absolute:absolute|styleprops:styleprops|cite:cite"
        + "|abbr:abbr|acronym:acronym|del:del|ins:ins|attribs:attribs|visualchars:visualchars|nonbreaking:nonbreaking|template:template"
        + "|pagebreak:pagebreak|selectall:selectall|fullpage:fullpage|imagegallery:OcmsImageGallery|downloadgallery:OcmsDownloadGallery"
        + "|linkgallery:OcmsLinkGallery|link:link|typography:typograf";

    /** The map containing the translation of the generic widget button names to TinyMCE specific button names. */
    public static final Map<String, String> BUTTON_TRANSLATION_MAP = CmsStringUtil.splitAsMap(
        BUTTON_TRANSLATION,
        "|",
        ":");

    /**
     * Hidden constructor.<p>
     */
    private CmsTinyMceToolbarHelper() {

    }

    /**
     * Helper method to generate a TinyMCE-specific toolbar configuration string from a list of generic toolbar button names.<p>
     *
     * @param barItems the generic toolbar items
     *
     * @return the TinyMCE toolbar configuration string
     */
    public static String createTinyMceToolbarStringFromGenericToolbarItems(List<String> barItems) {

        List<List<String>> blocks = new ArrayList<List<String>>();
        blocks.add(new ArrayList<String>());
        String lastItem = null;
        List<String> processedItems = new ArrayList<String>();

        // translate buttons and eliminate adjacent separators
        for (String barItem : barItems) {
            String translated = CmsTinyMceToolbarHelper.translateButton(barItem);
            if (translated != null) {
                barItem = translated;
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
        Set<String> writtenItems = new HashSet<String>();

        // transform flat list into list of groups
        for (String processedItem : processedItems) {
            if (!writtenItems.contains(processedItem)) {
                blocks.get(blocks.size() - 1).add(processedItem);
            }
            if ("|".equals(processedItem)) {
                blocks.add(new ArrayList<String>());
            } else {
                writtenItems.add(processedItem);
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
        return toolbar;
    }

    /**
     * Returns the context menu entries according to the configured tool-bar items.<p>
     *
     * @param barItems the tool-bar items
     *
     * @return the context menu entries
     */
    public static String getContextMenuEntries(List<String> barItems) {

        String result = "";
        if (barItems.contains("link")) {
            result += translateButton("link");
        }
        if (barItems.contains("downloadgallery")) {
            result += " " + translateButton("downloadgallery");
        }
        if (barItems.contains("imagegallery")) {
            result += " " + translateButton("imagegallery");
        }
        if (barItems.contains("table")) {
            result += " inserttable | cell row column deletetable";
        }
        return result.trim();
    }

    /**
     * Translates a generic button name to a TinyMCE-specific button name (or a comma-separated list of button names).<p>
     *
     * @param cmsButtonName the generic button name
     * @return the TinyMCE button name(s)
     */
    public static String translateButton(String cmsButtonName) {

        return BUTTON_TRANSLATION_MAP.get(cmsButtonName);

    }

}
