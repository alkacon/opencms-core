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

package org.opencms.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.google.common.io.Files;

/**
 * Helper class for statically changing color constants which were previously defined as system colors.<p>
 **/
public final class CmsColorReplaceHelper {

    /** The regular expression. (This constant only starts with A so it comes before PATTERN alphabetically). */
    public static final String A_PATTERN_STR = "(/\\*begin-color ([a-zA-Z]+)\\*/)(.+?)(/\\*end-color\\*/)";

    /** The base directory. */
    public static final String BASEDIR = "/home/user/workspace/opencms-core";

    /** The list of files to process. */
    public static final String[] FILE_LIST = {
        "modules/org.opencms.editors.codemirror/static/editors/codemirror/codemirror-ocms.css",
        "modules/org.opencms.editors/resources/system/workplace/editors/dialogs/property.jsp",
        "modules/org.opencms.editors/resources/system/workplace/editors/dialogs/table_new.jsp",
        "modules/org.opencms.editors/resources/system/workplace/editors/direct_edit_include.txt",
        "modules/org.opencms.editors/resources/system/workplace/editors/direct_edit.jsp",
        "modules/org.opencms.editors.tinymce/resources/system/workplace/editors/tinymce/editor.jsp",
        "modules/org.opencms.jquery/resources/system/modules/org.opencms.jquery/resources/css/ui-ocms/jquery.ui.ocms.css",
        "modules/org.opencms.workplace.explorer/resources/system/workplace/commons/property_custom.jsp",
        "modules/org.opencms.workplace.explorer/resources/system/workplace/resources/commons/explorer.css",
        "modules/org.opencms.workplace.explorer/resources/system/workplace/resources/commons/tree.js",
        "modules/org.opencms.workplace/resources/system/workplace/commons/includes/report.jsp",
        "modules/org.opencms.workplace/resources/system/workplace/commons/style/menu.css",
        "modules/org.opencms.workplace/resources/system/workplace/commons/style/report.css",
        "modules/org.opencms.workplace/resources/system/workplace/galleries/downloadgallery/css/dialog.css",
        "modules/org.opencms.workplace/resources/system/workplace/galleries/imagegallery/css/crop.css",
        "modules/org.opencms.workplace/resources/system/workplace/galleries/imagegallery/css/editor.css",
        "modules/org.opencms.workplace/resources/system/workplace/resources/components/js_calendar/calendar-opencms.css",
        "modules/org.opencms.workplace/resources/system/workplace/resources/components/js_calendar/calendar-system.css",
        "modules/org.opencms.workplace/resources/system/workplace/resources/components/js_colorpicker/index.html",
        "src/org/opencms/jsp/util/errorpage.properties",
        "src/org/opencms/main/CmsException.java",
        "src/org/opencms/workplace/CmsReport.java",
        "src/org/opencms/workplace/CmsWorkplaceCustomFoot.java"};

    /** The compiled pattern. */
    public static Pattern PATTERN = Pattern.compile(A_PATTERN_STR);

    /** The color substitution rules. */
    public static String[][] SUBSTITUTION_LIST = {

        {
            "ActiveBorder|ButtonFace|InactiveBorder|Menu|ThreeDFace|ThreeDLightShadow|ScrollBar|InactiveCaptionText",
            "#f0f0f0"},
        {"AppWorkspace|ButtonShadow|GrayText|InactiveCaption|ThreeDShadow", "#999999"},
        {"InfoBackground", "#f9f9f9"},
        {"TextHover", "#b31b34"},
        {"ThreeDDarkShadow", "#606161"},
        {"ActiveCaption|Highlight", "#1f232a"},
        {"ThreeDHighlight|ButtonHighlight|CaptionText|HighlightText|Window|InfoText", "#ffffff"},
        {"Background|WindowFrame|WindowText|MenuText|ButtonText", "#000000"}

    };

    /** The map of substitutions. */
    public static Map<String, String> SUBSTITUTION_MAP = mapFromArray(SUBSTITUTION_LIST);

    /**
     * Private constructor.<p>
     */
    private CmsColorReplaceHelper() {

    }

    /**
     * Looks up a replacement color by name.<p>
     *
     * @param name the name of the color
     * @return the replacement color
     */
    public static String lookupColor(String name) {

        return SUBSTITUTION_MAP.get(name.trim().toLowerCase());
    }

    /**
     * Main method which processes the files.<p>
     *
     * @param args arguments are ignored
     * @throws Exception
     */
    public static void main(String[] args) throws Exception {

        for (String filePath : FILE_LIST) {
            File file = new File(BASEDIR + "/" + filePath);
            StringBuffer output = new StringBuffer();
            BufferedReader br = null;
            try {
                br = new BufferedReader(new FileReader(file));
                String line = null;

                while ((line = br.readLine()) != null) {
                    String processed = processLine(line);
                    output.append(processed);
                    output.append("\n");
                }
            } finally {
                if (br != null) {
                    br.close();
                }
            }
            byte[] data = output.toString().getBytes("UTF-8");
            Files.write(data, file);
        }

    }

    /**
     * Constructs a map from an array of string pairs.<p>
     *
     * @param stringData an array of string pairs with  the first component being a |-separated list of map keys, and the second component the map value
     * @return the map for the given data
     */
    public static Map<String, String> mapFromArray(String[][] stringData) {

        Map<String, String> result = new HashMap<String, String>();
        for (String[] entry : stringData) {
            for (String key : CmsStringUtil.splitAsList(entry[0], "|")) {
                result.put(key.toLowerCase(), entry[1]);
            }
        }
        return result;
    }

    /**
     * Performs color substitutions on a single line of text.<p>
     *
     * @param line the original line
     * @return the processed line
     */
    public static String processLine(String line) {

        String result = CmsStringUtil.substitute(PATTERN, line, new I_CmsRegexSubstitution() {

            public String substituteMatch(String string, Matcher matcher) {

                // group 1: begin-color marker
                // group 2: color name after begin-color token
                // group 3: text between markers
                // group 4: end-color marker

                String maybeQuote = matcher.group(3).contains("\"") ? "\"" : "";
                return matcher.group(1) + maybeQuote + lookupColor(matcher.group(2)) + maybeQuote + matcher.group(4);
            }

        });

        return result;
    }

}
