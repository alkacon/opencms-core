/*
* File   : $Source: /alkacon/cvs/opencms/src/com/opencms/util/Attic/LinkSubstitution.java,v $
* Date   : $Date: 2003/07/15 16:04:01 $
* Version: $Revision: 1.31 $
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

import com.opencms.boot.I_CmsLogChannels;
import com.opencms.core.A_OpenCms;
import com.opencms.core.CmsException;
import com.opencms.core.CmsStaticExportProperties;
import com.opencms.core.I_CmsConstants;
import com.opencms.core.OpenCms;
import com.opencms.file.CmsObject;
import com.opencms.file.CmsStaticExport;
import com.opencms.htmlconverter.CmsHtmlConverter;

import java.net.URL;
import java.util.Vector;

import javax.servlet.http.HttpServletRequest;

import org.apache.oro.text.perl.MalformedPerl5PatternException;
import org.apache.oro.text.perl.Perl5Util;

/**
 * 
 * 
 * @author Hanjo Riege
 * @version 1.0
 */

public final class LinkSubstitution {

    /**
     * Reference to the CmsElementCache object containing locators for all
     * URIs and elements in cache.
     */
    private static Perl5Util c_perlUtil = new Perl5Util();
    
    /**
     * Hide constructor since this class contains only static methods.<p>
     */
    private LinkSubstitution() {
    }
    
    /**
     * String to configure the CmsHtmlConverter
     */
    private static String m_converterConfiguration = 
            "<?xml version=\"1.0\"?>"
            +"<converterconfig>"
            +"   <defaults>"
            +"       <xhtmloutput value=\"false\"/>"
            +"       <globalprefix value=\"\"/>"
            +"       <globalsuffix value=\"\"/>"
            +"       <globaladdeveryline value=\"false\"/>"
            +"       <usebrackets value=\"true\" openbracket=\"#(\" closebracket=\")#\"/>"
            +"       <encodequotationmarks value=\"false\"/>"
            +"   </defaults>"
            +"   <replacecontent>"
            +"       <string content=\"&amp;mdash;\" replace=\"$mdash$\"/>"
            +"       <string content=\"&amp;ndash;\" replace=\"$ndash$\"/>"
            +"       <string content=\"&amp;bull;\" replace=\"$bull$\"/>"
            +"       <string content=\"&#\" replace=\"$Sonder$\"/>"
            +"   </replacecontent>"
            +"   <replacestrings usedefaults=\"true\">"
            +"       <string content=\"$Sonder$\" replace=\"&#\"/>"
            +"       <string content=\"$mdash$\" replace=\"&amp;mdash;\"/>"
            +"       <string content=\"$ndash$\" replace=\"&amp;ndash;\"/>"
            +"       <string content=\"$bull$\" replace=\"&amp;bull;\"/>"
            +"   </replacestrings>"
            +"   <inlinetags>"
            +"       <tag name=\"img\"/>"
            +"       <tag name=\"br\"/>"
            +"       <tag name=\"hr\"/>"
            +"       <tag name=\"input\"/>"
            +"       <tag name=\"frame\"/>"
            +"       <tag name=\"meta\"/>"
            +"   </inlinetags>"
            +"   <replacetags usedefaults=\"true\">"
            +"       <tag name=\"img\" attrib=\"src\" replacestarttag=\"]]&gt;&lt;LINK&gt;&lt;![CDATA[$parameter$]]&gt;&lt;/LINK&gt;&lt;![CDATA[\" parameter=\"src\" replaceparamattr=\"true\"/>"
            +"       <tag name=\"a\" attrib=\"href\" replacestarttag=\"]]&gt;&lt;LINK&gt;&lt;![CDATA[$parameter$]]&gt;&lt;/LINK&gt;&lt;![CDATA[\" replaceendtag=\"&lt;/a&gt;\" parameter=\"href\" replaceparamattr=\"true\"/>"
            +"   </replacetags>"
            +"</converterconfig>";
    
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
            converter.setServletPrefix(A_OpenCms.getOpenCmsContext(), null);
            converter.setOriginalUrl(url);
            // convert html code
            body = converter.convertHTML(body);
        } catch (Exception e) {
            e.printStackTrace(System.err);
            throw new CmsException(
                "[LinkSubstitution] can't convert the editor content:" + e.toString());
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
     * parses the html content from the editor. It replaces the links in <a href=""
     * and in <image src="". They will be replaced with ]]><LINK> path in opencms <LINK><![CDATA[
     */
    public static String substituteEditorContent(CmsObject cms, String content, String path, String relativeRoot)throws CmsException{
        CmsHtmlConverter converter = new CmsHtmlConverter();
        String retValue = null;
        if(path == null || "".equals(path)){
            path = "/";
        }
        try{
            if(converter.hasErrors(content)){
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
        }catch ( Exception e ){
            throw new CmsException("[LinkSubstitution] can't convert the editor content:"+e.toString());
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
    public static String getLinkSubstitution(CmsObject cms, String link){
        if(link == null || "".equals(link)){
            return "";
        }
        if(!link.startsWith("/")){
            // this is a relative link, lets make an absolute out of it
            link = Utils.mergeAbsolutePath(cms.getRequestContext().getRequest().getRequestedResource(), link);
        }
        // first ask if this is the export
        int modus = cms.getMode();
        if(modus == I_CmsConstants.C_MODUS_EXPORT){
            // we have to register this link to the request context
            cms.getRequestContext().addLink(link);
            // and we have to process the startrule
            String startRule = OpenCms.getStaticExportProperties().getStartRule();
            if(startRule != null && !"".equals(startRule)){
                try{
                    link = c_perlUtil.substitute(startRule, link);
                }catch(Exception e){
                    if(I_CmsLogChannels.C_PREPROCESSOR_IS_LOGGING && A_OpenCms.isLogging() ) {
                        A_OpenCms.log(I_CmsLogChannels.C_OPENCMS_CRITICAL, "[LinkSubstitution.getLinkSubstitution()/1] problems with startrule:\""+startRule+"\" (" + e + "). ");
                    }
                }
            }
        }
        // check if we are in a https page, then we have to set the http
        // protocol ahead the "not-https-links" in this page
        boolean httpsMode = false;
        if(modus == I_CmsConstants.C_MODUS_ONLINE){
            // https pages are always online
            try {
                // HACK: Original request might be unavailable here. 
                // If you start the export in a Thread (what is done most of the time now)
                // the original request might be gone here and a NullPointer Exception will raised.
                String scheme = ((HttpServletRequest)cms.getRequestContext().getRequest().getOriginalRequest()).getScheme();
                if("https".equalsIgnoreCase(scheme)){
                    httpsMode = true;
                }
            } catch (Exception e) {
                httpsMode = false;
            }
        }
        boolean needsScheme = true;
        if(httpsMode){
            needsScheme = CmsStaticExport.needsScheme(link);
        }
        String[] rules = CmsStaticExportProperties.getLinkRules(modus);
        if(rules == null || rules.length == 0){
            return link;
        }
        String retValue = link;
        for(int i=0; i<rules.length; i++){
            try{
                boolean nextRule = true;
                if("*dynamicRules*".equals(rules[i])){
                    // here we go trough our dynamic rules
                    Vector booleanReplace = new Vector();
                    retValue = CmsStaticExport.handleDynamicRules(cms, link, modus, booleanReplace);
                    Boolean goOn =(Boolean)booleanReplace.firstElement();
                    if(goOn.booleanValue()){
                        link = retValue;
                    }else{
                        nextRule = false;
                    }
                }else{
                    StringBuffer result = new StringBuffer();
                    int matches = c_perlUtil.substitute(result, rules[i], link);
                    if(matches != 0){
                        retValue = result.toString();
                        nextRule = false;
                    }
                }
                if(!nextRule){
                    // found the match
                    if(httpsMode && !retValue.startsWith("http")){
                        if(needsScheme){
                            retValue = CmsObject.getStaticExportProperties().getUrlPrefixArray()[3] + retValue;
                        }
                    }else{
                        if(CmsObject.getStaticExportProperties().relativLinksInExport() && modus == I_CmsConstants.C_MODUS_EXPORT
                                && (retValue != null) && retValue.startsWith(CmsObject.getStaticExportProperties().getUrlPrefixArray()[0])){
                            // we want the path relative
                            retValue = getRelativePath(cms.getRequestContext().getRequest().getRequestedResource(), retValue.substring((CmsObject.getStaticExportProperties().getUrlPrefixArray()[0]).length()));
                        }
                    }
                    return retValue;
                }
            }catch(MalformedPerl5PatternException e){
                if(I_CmsLogChannels.C_PREPROCESSOR_IS_LOGGING && A_OpenCms.isLogging() ) {
                    A_OpenCms.log(I_CmsLogChannels.C_OPENCMS_CRITICAL, "[LinkSubstitution.getLinkSubstitution()/2] problems with rule:\""+rules[i]+"\" (" + e + "). ");
                }
            }
        }
        if(httpsMode && !retValue.startsWith("http")){
            if(needsScheme){
                retValue = CmsObject.getStaticExportProperties().getUrlPrefixArray()[3] + retValue;
            }
        }else{
            if(CmsObject.getStaticExportProperties().relativLinksInExport() && modus == I_CmsConstants.C_MODUS_EXPORT && (retValue != null) && retValue.startsWith(CmsObject.getStaticExportProperties().getUrlPrefixArray()[0])){
                // we want the path relative
                retValue = getRelativePath(cms.getRequestContext().getRequest().getRequestedResource(), retValue.substring((CmsObject.getStaticExportProperties().getUrlPrefixArray()[0]).length()));
            }
        }
        return retValue;
    }

    /**
     * This methood calculates the relative path to a resource in OpenCms
     * depending on the page where it is used.
     * i.e.: baseFile  = "/folder1/folder2/index.html"
     *       linkTarget= "/folder1/pics/pic.gif"
     *       returns: "../pics/pic.gif"
     *
     * @param baseFile: the name(incl. path) of the page containing the link
     * @param linkTarget: the name(incl. path) of the resource to link to.
     *
     * @return the relative path to the target resource.
     */
    public static String getRelativePath(String baseFile, String linkTarget){

        // use tokenizer for better performance
        java.util.StringTokenizer cur = new java.util.StringTokenizer(baseFile, "/");
        java.util.StringTokenizer tar = new java.util.StringTokenizer(linkTarget,"/");

        // get the minimum of the number of tokens for both paths
        int maxAllowed = cur.countTokens();
        if(maxAllowed > tar.countTokens()){
            maxAllowed = tar.countTokens();
        }
        // serch for the part of the path they have in common.
        String currentToken = cur.nextToken();
        String targetToken = tar.nextToken();
        int counter = 1;
        while(currentToken.equals(targetToken) && counter < maxAllowed){
            currentToken = cur.nextToken();
            targetToken = tar.nextToken();
            counter++;
        }
        StringBuffer result = new StringBuffer();

        // link to the shared root path
        counter = cur .countTokens();
        for(int i=0; i < counter; i++){
            result.append("../");
        }

        // finaly add the link to the target from the shared root path
        result.append(targetToken);
        while(tar.hasMoreTokens()){
            result.append("/" +tar.nextToken());
        }
        return result.toString();
    }
}