/*
 * File   : $Source: /alkacon/cvs/opencms/src/com/opencms/util/Attic/LinkSubstitution.java,v $
 * Date   : $Date: 2003/08/18 10:50:48 $
 * Version: $Revision: 1.37 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Mananagement System
 *
 * Copyright (C) 2002 - 2003 Alkacon Software (http://www.alkacon.com)
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

package com.opencms.util;

import org.opencms.main.OpenCms;

import com.opencms.core.CmsException;
import com.opencms.file.CmsObject;
import com.opencms.htmlconverter.CmsHtmlConverter;

import java.net.URL;

import javax.servlet.http.HttpServletRequest;

/**
 * Does the dynamic link replacement for the link tags.<p> 
 * 
 * @author Hanjo Riege
 * @version $Revision: 1.37 $
 */
public final class LinkSubstitution {

    /**
     * String to configure the CmsHtmlConverter
     */
    private static String m_converterConfiguration =
        "<?xml version=\"1.0\"?>"
            + "<converterconfig>"
            + "   <defaults>"
            + "       <xhtmloutput value=\"false\"/>"
            + "       <globalprefix value=\"\"/>"
            + "       <globalsuffix value=\"\"/>"
            + "       <globaladdeveryline value=\"false\"/>"
            + "       <usebrackets value=\"true\" openbracket=\"#(\" closebracket=\")#\"/>"
            + "       <encodequotationmarks value=\"false\"/>"
            + "   </defaults>"
            + "   <replacecontent>"
            + "       <string content=\"&amp;mdash;\" replace=\"$mdash$\"/>"
            + "       <string content=\"&amp;ndash;\" replace=\"$ndash$\"/>"
            + "       <string content=\"&amp;bull;\" replace=\"$bull$\"/>"
            + "       <string content=\"&#\" replace=\"$Sonder$\"/>"
            + "   </replacecontent>"
            + "   <replacestrings usedefaults=\"true\">"
            + "       <string content=\"$Sonder$\" replace=\"&#\"/>"
            + "       <string content=\"$mdash$\" replace=\"&amp;mdash;\"/>"
            + "       <string content=\"$ndash$\" replace=\"&amp;ndash;\"/>"
            + "       <string content=\"$bull$\" replace=\"&amp;bull;\"/>"
            + "   </replacestrings>"
            + "   <inlinetags>"
            + "       <tag name=\"img\"/>"
            + "       <tag name=\"br\"/>"
            + "       <tag name=\"hr\"/>"
            + "       <tag name=\"input\"/>"
            + "       <tag name=\"frame\"/>"
            + "       <tag name=\"meta\"/>"
            + "   </inlinetags>"
            + "   <replacetags usedefaults=\"true\">"
            + "       <tag name=\"img\" attrib=\"src\" replacestarttag=\"]]&gt;&lt;LINK&gt;&lt;![CDATA[$parameter$]]&gt;&lt;/LINK&gt;&lt;![CDATA[\" parameter=\"src\" replaceparamattr=\"true\"/>"
            + "       <tag name=\"a\" attrib=\"href\" replacestarttag=\"]]&gt;&lt;LINK&gt;&lt;![CDATA[$parameter$]]&gt;&lt;/LINK&gt;&lt;![CDATA[\" replaceendtag=\"&lt;/a&gt;\" parameter=\"href\" replaceparamattr=\"true\"/>"
            + "   </replacetags>"
            + "</converterconfig>";

    /**
     * Hide constructor since this class contains only static methods.<p>
     */
    private LinkSubstitution() { }

    /**
    * Parses the html content for the editor. It replaces the links in <a href=""
    * and in &lt;image src="". They will be replaced with ]]&gt;&lt;LINK&gt; path in opencms &lt;LINK&gt;&lt;![CDATA[.<p>
    * 
    * This method is used for database imports of OpenCms versions &lt; 5.0<p>
    * 
    * @param body the html content fragment
    * @param webappUrl the old web app URL, e.g. http://localhost:8080/opencms/opencms/
    * @param fileName the path and name of current file
    * @return String the converted content
    * @throws CmsException if something goes wrong
    */
    public static String substituteContentBody(String body, String webappUrl, String fileName) throws CmsException {

        // prepare the content for the JTidy
        body = "<html><head></head><body>" + body + "</body></html>";
        CmsHtmlConverter converter = new CmsHtmlConverter();
        try {
            // check errors...
            if (converter.hasErrors(body)) {
                String errors = converter.showErrors(body);
                throw new CmsException(errors);
            }
            // configure the converter
            converter.setConverterConfString(m_converterConfiguration);
            URL url = new URL(webappUrl + fileName);
            converter.setServletPrefix(OpenCms.getOpenCmsContext(), null);
            converter.setOriginalUrl(url);
            // convert html code
            body = converter.convertHTML(body);
        } catch (Exception e) {
            e.printStackTrace(System.err);
            throw new CmsException("[LinkSubstitution] can't convert the editor content:" + e.toString());
        }
        // remove the preparetags
        int startIndex = body.indexOf("<body");
        startIndex = body.indexOf(">", startIndex + 1) + 1;
        int endIndex = body.lastIndexOf("</body>");
        if (startIndex > 0) {
            body = body.substring(startIndex, endIndex);
        }
        return body;
    }

    /**
     * Parses the html content for the editor.
     * 
     * @param cms the CmsObject
     * @param content the html content fragment
     * @return the substituted content
     * @throws CmsException if something goes wrong
     */
    public static String substituteEditorContent(CmsObject cms, String content) throws CmsException {
        return substituteEditorContent(cms, content, null, null);
    }

    /**
     * Parses the html content from the editor and replaces the links in 'a href=..'
     * and in 'image src=..' with XMLTemplate mechanism &lt;LINK&gt; tags.<p>
     * 
     * @param cms the cms context
     * @param content the content of the editor
     * @param path the path of the file
     * @param relativeRoot the relative root
     * @return the parsed content
     * @throws CmsException if something goes wrong
     */
    public static String substituteEditorContent(CmsObject cms, String content, String path, String relativeRoot) throws CmsException {
        CmsHtmlConverter converter = new CmsHtmlConverter();
        String retValue = null;
        if (path == null || "".equals(path)) {
            path = "/";
        }
        try {
            if (converter.hasErrors(content)) {
                String errors = converter.showErrors(content);
                throw new CmsException(errors);
            }
            converter.setConverterConfString(m_converterConfiguration);
            // get parameter to create the url object of the edited file
            String servletPrefix = cms.getRequestContext().getRequest().getServletUrl();
            String prot = ((HttpServletRequest)cms.getRequestContext().getRequest().getOriginalRequest()).getScheme();
            String host = ((HttpServletRequest)cms.getRequestContext().getRequest().getOriginalRequest()).getServerName();
            int port = ((HttpServletRequest)cms.getRequestContext().getRequest().getOriginalRequest()).getServerPort();
            URL urltool = new URL(prot, host, port, servletPrefix + path);
            converter.setServletPrefix(servletPrefix, relativeRoot);
            converter.setOriginalUrl(urltool);
            retValue = converter.convertHTML(content);
        } catch (Exception e) {
            throw new CmsException("[LinkSubstitution] can't convert the editor content:" + e.toString());
        }
        return retValue;
    }

    /**
     * Parses the content of the body tag of a html page. It is the same as
     * substituteEditorContent except that it expects only the part of the
     * html page between the body tags.<p>
     * 
     * @param cms the cms object
     * @param body body content
     * @return the substituted content
     * @throws CmsException if something goes wrong
     */
    public static String substituteEditorContentBody(CmsObject cms, String body) throws CmsException {

        // we have to prepare the content for the tidy
        body = "<html><head></head><body>" + body + "</body></html>";
        // start the tidy
        String result = substituteEditorContent(cms, body);
        // remove the preparetags
        int startIndex = result.indexOf("<body");
        startIndex = result.indexOf(">", startIndex + 1) + 1;
        int endIndex = result.lastIndexOf("</body>");
        if (startIndex > 0) {
            result = result.substring(startIndex, endIndex);
        }
        return result;
    }
}