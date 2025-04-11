/*
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (c) Alkacon Software GmbH & Co. KG (https://www.alkacon.com)
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
 * company website: https://www.alkacon.com
 *
 * For further information about OpenCms, please see the
 * project website: https://www.opencms.org
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package org.opencms.gwt;

import org.opencms.file.CmsObject;
import org.opencms.gwt.shared.CmsCoreData;
import org.opencms.gwt.shared.rpc.I_CmsCoreService;
import org.opencms.i18n.CmsEncoder;
import org.opencms.i18n.CmsLocaleManager;
import org.opencms.jsp.CmsJspActionElement;
import org.opencms.main.OpenCms;
import org.opencms.util.CmsStringUtil;
import org.opencms.workplace.CmsWorkplace;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collection;
import java.util.Locale;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.jsp.PageContext;

import com.google.gwt.user.client.rpc.SerializationException;
import com.google.gwt.user.server.rpc.RPC;

/**
 * Sitemap action used to generate the sitemap editor.<p>
 *
 * see jsp file <tt>/system/workplace/commons/sitemap.jsp</tt>.<p>
 *
 * @since 8.0.0
 */
public class CmsGwtActionElement extends CmsJspActionElement {

    /** The closing script tag. */
    protected static final String SCRIPT_TAG_CLOSE = "\n//-->\n</script>";

    /** The opening script tag. */
    protected static final String SCRIPT_TAG_OPEN = "<script>\n<!--\n";

    /** In page variable name for missing permutation message. */
    private static final String CMS_NO_PERMUTATION_MESSAGE = "CMS_NO_PERMUTATION_MESSAGE";

    /** The resource icon CSS URI. */
    private static final String ICON_CSS_URI = "/system/workplace/commons/resourceIcon.css";

    /** The current core data. */
    private CmsCoreData m_coreData;

    /**
     * Constructor.<p>
     *
     * @param context the JSP page context object
     * @param req the JSP request
     * @param res the JSP response
     */
    public CmsGwtActionElement(PageContext context, HttpServletRequest req, HttpServletResponse res) {

        super(context, req, res);
    }

    /**
     * Returns the script tag for the "*.nocache.js".<p>
     *
     * @param moduleName the module name to get the script tag for
     * @param moduleVersion the module version
     *
     * @return the <code>"&lt;script&gt;"</code> tag for the "*.nocache.js".<p>
     */
    public static String createNoCacheScript(String moduleName, String moduleVersion) {

        String result = "<script src=\""
            + CmsWorkplace.getResourceUri("ade/" + moduleName + "/" + moduleName + ".nocache.js");
        if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(moduleVersion)) {
            result += "?version=" + moduleVersion + "_" + OpenCms.getSystemInfo().getVersionNumber().hashCode();
        }
        result += "\"></script>";
        return result;
    }

    /**
     * Returns the serialized data for the core provider wrapped into a script tag.<p>
     *
     * @param cms the CMS context
     * @param coreData the core data to write into the page
     *
     * @return the data
     *
     * @throws Exception if something goes wrong
     */
    public static String exportCommon(CmsObject cms, CmsCoreData coreData) throws Exception {

        // determine the workplace locale
        String wpLocale = OpenCms.getWorkplaceManager().getWorkplaceLocale(cms).toString();
        if (CmsStringUtil.isEmptyOrWhitespaceOnly(wpLocale)) {
            // if no locale was found, take English as locale
            wpLocale = Locale.ENGLISH.toString();
        }
        StringBuffer sb = new StringBuffer();
        // append meta tag to set the IE to standard document mode
        sb.append("<meta http-equiv=\"X-UA-Compatible\" content=\"IE=edge\" />\n");

        sb.append(
            "<script src=\""
                + OpenCms.getStaticExportManager().getVfsPrefix()
                + "/"
                + CmsMessagesService.class.getName()
                + ".gwt?"
                + CmsLocaleManager.PARAMETER_LOCALE
                + "="
                + wpLocale
                + "\"></script>\n");

        // print out the missing permutation message to be used from the nocache.js generated by custom linker
        // see org.opencms.gwt.linker.CmsIFrameLinker
        sb.append(
            wrapScript(
                "var ",
                CMS_NO_PERMUTATION_MESSAGE,
                "='",
                Messages.get().getBundle(OpenCms.getWorkplaceManager().getWorkplaceLocale(cms)).key(
                    Messages.ERR_NO_PERMUTATION_AVAILABLE_0),
                "';\n"));

        String prefetchedData = exportDictionary(
            CmsCoreData.DICT_NAME,
            I_CmsCoreService.class.getMethod("prefetch"),
            coreData);
        sb.append(prefetchedData);
        //       sb.append(ClientMessages.get().export(wpLocale));
        sb.append("<style type=\"text/css\">\n @import url(\"").append(iconCssLink(cms)).append("\");\n </style>\n");

        // append additional style sheets
        Collection<String> stylesheets = OpenCms.getWorkplaceAppManager().getAdditionalStyleSheets();
        for (String stylesheet : stylesheets) {
            sb.append("<style type=\"text/css\">\n @import url(\"").append(stylesheet).append("\");\n </style>\n");
        }
        // append the workplace locale information
        sb.append("<meta name=\"gwt:property\" content=\"locale=").append(wpLocale).append("\" />\n");
        return sb.toString();
    }

    /**
     * Serializes the result of the given method for RPC-prefetching.<p>
     *
     * @param name the dictionary name
     * @param method the method
     * @param data the result to serialize
     *
     * @return the serialized data
     *
     * @throws SerializationException if something goes wrong
     */
    public static String exportDictionary(String name, Method method, Object data) throws SerializationException {

        return exportDictionary(name, serialize(method, data));
    }

    /**
     * Exports a dictionary by the given name as the content attribute of a meta tag.<p>
     *
     * @param name the dictionary name
     * @param data the data
     *
     * @return the meta tag
     */
    public static String exportDictionary(String name, String data) {

        StringBuffer sb = new StringBuffer();
        sb.append("<meta name=\"").append(name).append("\" content=\"").append(data).append("\" />");
        return sb.toString();
    }

    /**
     * Generates the HTML for a meta tag with given name and content.<p>
     *
     * @param name the name of the meta tag
     * @param data the content of the meta tag
     *
     * @return the HTML for the meta tag
     */
    public static String exportMeta(String name, String data) {

        String escName = CmsEncoder.escapeXml(name);
        String escData = CmsEncoder.escapeXml(data);
        return ("<meta name=\"" + escName + "\" content=\"" + escData + "\" />");
    }

    /**
     * Returns the OpenCms font icon CSS link.<p>
     *
     * @return the CSS link
     */
    public static String getFontIconCssLink() {

        return CmsStringUtil.joinPaths(
            OpenCms.getSystemInfo().getContextPath(),
            "VAADIN/themes/opencms/opencmsFonts.css");
    }

    /**
     * Serializes the result of the given method for RPC-prefetching.<p>
     *
     * @param method the method
     * @param data the result to serialize
     *
     * @return the serialized data
     *
     * @throws SerializationException if something goes wrong
     */
    public static String serialize(Method method, Object data) throws SerializationException {

        String result = RPC.encodeResponseForSuccess(method, data, CmsPrefetchSerializationPolicy.instance());
        result = CmsEncoder.escapeXml(result, true);
        return result;
    }

    /**
     * Concatenates the given strings and surrounds them with script tags.<p>
     *
     * @param s the array of strings to concatenate and wrap
     *
     * @return the concatenation of the input strings, wrapped in script tags
     */
    public static String wrapScript(String... s) {

        return SCRIPT_TAG_OPEN + CmsStringUtil.listAsString(Arrays.asList(s), "") + SCRIPT_TAG_CLOSE;
    }

    /**
     * Generates the link to the icon CSS JSP, and appends a "prefix" request parameter with the given value.<p>
     *
     * @param cms the CMS context
     *
     * @return the link to the icon CSS
     */
    private static String iconCssLink(CmsObject cms) {

        return OpenCms.getLinkManager().substituteLinkForUnknownTarget(cms, ICON_CSS_URI);
    }

    /**
     * Returns the script tag for the "*.nocache.js".<p>
     *
     * @param moduleName the module name to get the script tag for
     *
     * @return the <code>"&lt;script&gt;"</code> tag for the "*.nocache.js".<p>
     */
    @Deprecated
    public String createNoCacheScript(String moduleName) {

        return createNoCacheScript(moduleName, null);
    }

    /**
     * Returns the serialized data for the core provider wrapped in a script tag.<p>
     *
     * @return the data
     *
     * @throws Exception if something goes wrong
     */
    public String export() throws Exception {

        return export(true);
    }

    /**
     * Returns the serialized data for the core provider wrapped into a script tag.<p>
     *
     * @param includeFontCss <code>true</code> to include the OpenCms font CSS, not necessary in case VAADIN theme is loaded also
     *
     * @return the data
     *
     * @throws Exception if something goes wrong
     */
    public String export(boolean includeFontCss) throws Exception {

        StringBuffer buffer = new StringBuffer(exportCommon(getCmsObject(), getCoreData()));
        if (includeFontCss || !OpenCms.getWorkplaceAppManager().getWorkplaceCssUris().isEmpty()) {
            buffer.append("\n<style type=\"text/css\">\n");
            if (includeFontCss) {
                buffer.append("@import url(\"").append(getFontIconCssLink()).append("\");\n");
            }
            for (String cssURI : OpenCms.getWorkplaceAppManager().getWorkplaceCssUris()) {
                buffer.append("@import url(\"").append(CmsWorkplace.getResourceUri(cssURI)).append("\");\n");
            }
            buffer.append("</style>\n");
        }
        return buffer.toString();
    }

    /**
     * Returns the serialized data for the core provider.<p>
     *
     * @return the data
     *
     * @throws Exception if something goes wrong
     */
    public String exportAll() throws Exception {

        return export();
    }

    /**
     * Returns the needed server data for client-side usage.<p>
     *
     * @return the needed server data for client-side usage
     */
    public CmsCoreData getCoreData() {

        if (m_coreData == null) {
            m_coreData = CmsCoreService.prefetch(getRequest());
        }
        return m_coreData;
    }

    /**
     * Returns the workplace locale for the current user.<p>
     *
     * @return the workplace locale
     */
    public Locale getWorkplaceLocale() {

        return OpenCms.getWorkplaceManager().getWorkplaceLocale(getCmsObject());
    }

    /**
     * Exports script tag to the main OpenCms GWT no-cache js script tag.<p>
     *
     * @param moduleName the client module to start
     *
     * @return the HTML string
     */
    protected String exportModuleScriptTag(String moduleName) {

        String result = "<meta name=\""
            + CmsCoreData.META_PARAM_MODULE_KEY
            + "\" content=\""
            + moduleName
            + "\" >\n<script src=\""
            + CmsWorkplace.getStaticResourceUri("gwt/opencms/opencms.nocache.js");
        result += "\"></script>\n";
        return result;
    }
}
