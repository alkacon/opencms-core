/*
* File   : $Source: /alkacon/cvs/opencms/src/com/opencms/util/Attic/LinkSubstitution.java,v $
* Date   : $Date: 2002/02/21 16:13:53 $
* Version: $Revision: 1.15 $
*
* This library is part of OpenCms -
* the Open Source Content Mananagement System
*
* Copyright (C) 2001  The OpenCms Group
*
* This library is free software; you can redistribute it and/or
* modify it under the terms of the GNU Lesser General Public
* License as published by the Free Software Foundation; either
* version 2.1 of the License, or (at your option) any later version.
*
* This library is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
* Lesser General Public License for more details.
*
* For further information about OpenCms, please see the
* OpenCms Website: http://www.opencms.org
*
* You should have received a copy of the GNU Lesser General Public
* License along with this library; if not, write to the Free Software
* Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
*/
package com.opencms.util;

import com.opencms.file.CmsObject;
import com.opencms.core.*;
import com.opencms.file.CmsStaticExport;
import com.opencms.htmlconverter.*;
import org.apache.oro.text.perl.*;
import javax.servlet.http.*;
import java.net.*;
import java.io.*;
import java.util.*;

/**
 * Title:        OpenCms
 * Description:
 * @author Hanjo Riege
 * @version 1.0
 */

public class LinkSubstitution {

    /**
     * Reference to the CmsElementCache object containing locators for all
     * URIs and elements in cache.
     */
    private static Perl5Util c_perlUtil = null;

    public LinkSubstitution() {
        c_perlUtil = new Perl5Util();
    }

    /**
     * Parses the content of the body tag of a html page. It is the same as
     * substituteEditorContent except that it expects only the part of the
     * html page between the body tags.
     */
    public String substituteEditorContentBody(CmsObject cms, String body) throws CmsException{

        // we have to prepare the content for the tidy
        body = "<html><head></head><body>" + body + "</body></html>";
        // start the tidy
        String result = substituteEditorContent(cms, body);
        // remove the preparetags
        int startIndex = result.indexOf("<body");
        startIndex = result.indexOf(">", startIndex + 1) + 1;
        int endIndex = result.lastIndexOf("</body>");
        if(startIndex > 0) {
            result = result.substring(startIndex, endIndex);
        }
        return result;
    }

     /**
     * parses the html content from the editor. It replaces the links in <a href=""
     * and in <image src="". They will be replaced with ]]><LINK> path in opencms <LINK><![CDATA[
     */
    public String substituteEditorContent(CmsObject cms, String content)throws CmsException{
        return substituteEditorContent(cms, content, null);
    }

   /**
     * parses the html content from the editor. It replaces the links in <a href=""
     * and in <image src="". They will be replaced with ]]><LINK> path in opencms <LINK><![CDATA[
     */
    public String substituteEditorContent(CmsObject cms, String content, String path)throws CmsException{
        CmsHtmlConverter converter = new CmsHtmlConverter();
        String retValue = null;
        if(path == null || "".equals(path)){
            path = "/";
        }
        try{
            //converter.setTidyConfFile("/andzah/projekte/ebk/config.txt");
            if(converter.hasErrors(content)){
                String errors = converter.showErrors(content);
                throw new CmsException(errors);
            }
            converter.setConverterConfString("<?xml version=\"1.0\"?>"+
                        "<converterconfig>"+
                        "	<defaults>"+
                        "		<xhtmloutput value=\"false\"/>"+
                        "		<globalprefix value=\"\"/>"+
                        "		<globalsuffix value=\"\"/>"+
                        "		<globaladdeveryline value=\"false\"/>"+
                        "		<usebrackets value=\"true\" openbracket=\"#(\" closebracket=\")#\"/>"+
                        "		<encodequotationmarks value=\"false\"/>"+
                        "	</defaults>"+
                        "   <replacecontent>"+
                        "       <string content=\"&amp;mdash;\" replace=\"$mdash$\"/>"+
                        "       <string content=\"&amp;ndash;\" replace=\"$ndash$\"/>"+
                        "       <string content=\"&amp;bull;\" replace=\"$bull$\"/>"+
                        "       <string content=\"&#\" replace=\"$Sonder$\"/>"+
                        "   </replacecontent>"+
                        "   <replacestrings usedefaults=\"true\">"+
                        "       <string content=\"$Sonder$\" replace=\"&#\"/>"+
                        "       <string content=\"$mdash$\" replace=\"&amp;mdash;\"/>"+
                        "       <string content=\"$ndash$\" replace=\"&amp;ndash;\"/>"+
                        "       <string content=\"$bull$\" replace=\"&amp;bull;\"/>"+
                        "   </replacestrings>"+
                        "	<inlinetags>"+
                        "		<tag name=\"img\"/>"+
                        "		<tag name=\"br\"/>"+
                        "		<tag name=\"hr\"/>"+
                        "		<tag name=\"input\"/>"+
                        "		<tag name=\"frame\"/>"+
                        "		<tag name=\"meta\"/>"+
                        "	</inlinetags>"+
                        "	<replacetags usedefaults=\"true\">"+
                        "		<tag name=\"img\" attrib=\"src\" replacestarttag=\"]]&gt;&lt;LINK&gt;&lt;![CDATA[$parameter$]]&gt;&lt;/LINK&gt;&lt;![CDATA[\" parameter=\"src\" replaceparamattr=\"true\"/>"+
                        "		<tag name=\"a\" attrib=\"href\" replacestarttag=\"]]&gt;&lt;LINK&gt;&lt;![CDATA[$parameter$]]&gt;&lt;/LINK&gt;&lt;![CDATA[\" replaceendtag=\"&lt;/a&gt;\" parameter=\"href\" replaceparamattr=\"true\"/>"+
                        "	</replacetags>"+
                        "</converterconfig>");
            // get parameter to create the url object of the edited file
            String servletPrefix = cms.getRequestContext().getRequest().getServletUrl();
            String prot = ((HttpServletRequest)cms.getRequestContext().getRequest().getOriginalRequest()).getScheme();
            String host = ((HttpServletRequest)cms.getRequestContext().getRequest().getOriginalRequest()).getServerName();
            int port = ((HttpServletRequest)cms.getRequestContext().getRequest().getOriginalRequest()).getServerPort();
            URL urltool = new URL(prot, host, port, servletPrefix + path);
            converter.setServletPrefix(servletPrefix);
            converter.setOriginalUrl(urltool);
            retValue = converter.convertHTML(content);
        }catch ( Exception e ){
            throw new CmsException("["+this.getClass().getName()+"] cant convert the editor content:"+e.toString());
        }
        return retValue;
    }
    /**
     * Replaces the link according to the rules and registers it to the
     * requestcontex if we are in export modus.
     * @param cms. The cms object.
     * @param link. The link to process.
     * @return String The substituded link.
     */
    public String getLinkSubstitution(CmsObject cms, String link){
        if(link == null || "".equals(link)){
            return "";
        }
        if(!link.startsWith("/")){
            // this is a relative link. Lets make an absolute out of it
            link = Utils.mergeAbsolutePath(cms.getRequestContext().getRequest().getRequestedResource(), link);
        }
        // first ask if this is the export
        int modus = cms.getMode();
        if(modus == cms.C_MODUS_EXPORT){
            // we have to register this link to the requestcontex
            cms.getRequestContext().addLink(link);
            // and we have to process the startrule
            String startRule = OpenCms.getLinkRuleStart();
            if(startRule != null && !"".equals(startRule)){
                try{
                    link = c_perlUtil.substitute(startRule, link);
                }catch(Exception e){
                    if(I_CmsLogChannels.C_PREPROCESSOR_IS_LOGGING && A_OpenCms.isLogging() ) {
                        A_OpenCms.log(I_CmsLogChannels.C_OPENCMS_CRITICAL, "["+this.getClass().getName()+"] problems with startrule:\""+startRule+"\" (" + e + "). ");
                    }
                }
            }
        }
        // check if we are in a https page (then we have to set the http
        // protocol ahead the "not-https-links" in this page
        boolean httpsMode = false;
        if(modus == cms.C_MODUS_ONLINE){
            // https pages are always online
            String scheme = ((HttpServletRequest)cms.getRequestContext().getRequest().getOriginalRequest()).getScheme();
            if("https".equalsIgnoreCase(scheme)){
                httpsMode = true;
            }
        }
        String[] rules = cms.getLinkRules(modus);
        if(rules == null || rules.length == 0){
            return link;
        }
        String retValue = link;
        for(int i=0; i<rules.length; i++){
            try{
                if("*dynamicRules*".equals(rules[i])){
                    // here we go trough our dynamic rules
                    Vector booleanReplace = new Vector();
                    retValue = CmsStaticExport.handleDynamicRules(cms, link, modus, booleanReplace);
                    Boolean goOn =(Boolean)booleanReplace.firstElement();
                    if(goOn.booleanValue()){
                        link = retValue;
                    }
                }else{
                    retValue = c_perlUtil.substitute(rules[i], link);
                }
                if(!link.equals(retValue)){
                    // found the match
                    if(httpsMode && !retValue.startsWith("http")){
                        retValue = cms.getUrlPrefixArray()[3] + retValue;
                    }
                    return retValue;
                }
            }catch(MalformedPerl5PatternException e){
                if(I_CmsLogChannels.C_PREPROCESSOR_IS_LOGGING && A_OpenCms.isLogging() ) {
                    A_OpenCms.log(I_CmsLogChannels.C_OPENCMS_CRITICAL, "["+this.getClass().getName()+"] problems with rule:\""+rules[i]+"\" (" + e + "). ");
                }
            }
        }
        if(httpsMode && !retValue.startsWith("http")){
            retValue = cms.getUrlPrefixArray()[3] + retValue;
        }
        return retValue;
    }
}