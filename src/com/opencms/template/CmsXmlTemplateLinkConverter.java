/*
 * File   : $Source: /alkacon/cvs/opencms/src/com/opencms/template/Attic/CmsXmlTemplateLinkConverter.java,v $
 * Date   : $Date: 2004/07/08 15:21:13 $
 * Version: $Revision: 1.3 $
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

package com.opencms.template;

import org.opencms.file.CmsObject;
import org.opencms.main.CmsException;
import org.opencms.main.OpenCms;

import com.opencms.htmlconverter.CmsHtmlConverter;
import com.opencms.legacy.CmsXmlTemplateLoader;

import java.net.URL;

import javax.servlet.http.HttpServletRequest;

/**
 * Provides utility methods for handling content of deprecated XmlTemplate methods.<p>
 * 
 * This class is mostly required for database imports of OpenCms versions &lt; 5.0.<p>
 * 
 * @author Alexander Kandzior (a.kandzior@alkacon.com)
 * @version $Revision: 1.3 $ 
 * @since 5.3.2
 * 
 * @deprecated Will not be supported past the OpenCms 6 release.
 */
public final class CmsXmlTemplateLinkConverter {

    /**  The JTidy configuration for the HTML conversion */
    private static final String m_converterConfiguration =
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
    private CmsXmlTemplateLinkConverter() {
        // empty
    }

    /**
     * Parses the html content of an imported XmlTemplate. <p>
     * 
     * It replaces the links in &lt;a href="" and in &lt;image src="". 
     * They will be replaced with ]]&gt;&lt;LINK&gt;
     * {path in OpenCms} &lt;LINK&gt;&lt;![CDATA[. <p>
     * 
     * This method is used for database imports of OpenCms versions &lt; 5.0 <p>
     * 
     * @param body the html content fragment
     * @param webappUrl the old web app URL, e.g. http://localhost:8080/opencms/opencms/
     * @param fileName the path and name of current file
     * @return String the converted content
     * @throws CmsException if something goes wrong
     */
    public static String convertFromImport(String body, String webappUrl, String fileName) throws CmsException {
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
            converter.setServletPrefix(OpenCms.getSystemInfo().getOpenCmsContext(), null);
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
     * Parses the html content of an edited XmlTemplate. <p>
     * 
     * It replaces the links in &lt;a href="" and in &lt;image src="". 
     * They will be replaced with ]]&gt;&lt;LINK&gt;
     * {path in OpenCms} &lt;LINK&gt;&lt;![CDATA[. <p>
     * 
     * This method is used for by the old XmlTemplate page editor.<p>
     * 
     * @param cms the cms context
     * @param content the content of the editor
     * @param path the path of the file
     * @param relativeRoot the relative root
     * @return the parsed content
     * @throws CmsException if something goes wrong
     */
    public static String convertFromEditor(CmsObject cms, String content, String path, String relativeRoot) throws CmsException {
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
            String servletPrefix = CmsXmlTemplateLoader.getRequest(cms.getRequestContext()).getServletUrl();
            String prot = ((HttpServletRequest)CmsXmlTemplateLoader.getRequest(cms.getRequestContext()).getOriginalRequest()).getScheme();
            String host = ((HttpServletRequest)CmsXmlTemplateLoader.getRequest(cms.getRequestContext()).getOriginalRequest()).getServerName();
            int port = ((HttpServletRequest)CmsXmlTemplateLoader.getRequest(cms.getRequestContext()).getOriginalRequest()).getServerPort();
            URL urltool = new URL(prot, host, port, servletPrefix + path);
            converter.setServletPrefix(servletPrefix, relativeRoot);
            converter.setOriginalUrl(urltool);
            retValue = converter.convertHTML(content);
        } catch (Exception e) {
            throw new CmsException("[LinkSubstitution] can't convert the editor content:" + e.toString());
        }
        return retValue;
    }
}