/*
* File   : $Source: /alkacon/cvs/opencms/src/com/opencms/htmlconverter/Attic/CmsHtmlConverterTools.java,v $
* Date   : $Date: 2003/08/18 10:50:48 $
* Version: $Revision: 1.12 $
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

package com.opencms.htmlconverter;

import org.opencms.main.OpenCms;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;

import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

/**
 * Various methods used by CmsHtmlConverter to
 * check, remove or replace tags and Strings.
 * @author Andreas Zahner
 * @version 1.0
 */
final class CmsHtmlConverterTools {

    /**
     * Tests if a set of tags contains the specified tag.&nbsp;This method is used to remove tags, blocks and to check inlinetags.
     * @param NodeName String with tagname
     * @param tags set of tags
     * @return true if NodeName is found in tags, otherwise false.
     */
    protected boolean checkTag(String NodeName, HashSet tags) {
        if (tags.contains(new String(NodeName))) {
            return true;
        }
        return false;
    }

    /**
     * Scans a String for substrings specified in a list of Strings and replaces them, returns a String where all replacements are done;
     * this method is needed for replacement run #1.
     * @param testString is the String which has to be scanned
     * @param rStrings lists all subStrings which have to be replaced
     * @return String with replaced content
     */
    protected String scanContent(String testString, ArrayList rStrings) {
        CmsHtmlConverterObjectReplaceContent testObject = new CmsHtmlConverterObjectReplaceContent();
        String searchString,replaceItem;
        for (int i=0;i<rStrings.size();i++) {
            testObject=(CmsHtmlConverterObjectReplaceContent)(rStrings.get(i));
            searchString=testObject.getSearchString();
            replaceItem=testObject.getReplaceItem();
            testString=replaceString(testString,searchString,replaceItem);
        }
        return testString;
    }

    /**
     * Scans a String for substrings specified in a list of Strings and replaces them, returns a String where all replacements are done.
     * @param testString is the String which has to be scanned
     * @param rStrings lists all subStrings which have to be replaced
     * @return String with replaced content
     */
    protected String scanString(String testString, ArrayList rStrings) {
        CmsHtmlConverterObjectReplaceStrings testObject = new CmsHtmlConverterObjectReplaceStrings();
        String searchString,replaceItem;
        for (int i=0;i<rStrings.size();i++) {
            testObject=(CmsHtmlConverterObjectReplaceStrings)(rStrings.get(i));
            searchString=testObject.getSearchString();
            replaceItem=testObject.getReplaceItem();
            testString=replaceString(testString,searchString,replaceItem);
        }
        return testString;
    }

    /**
     * Scans a String and replaces umlauts and other special characters
     * @param testString is the String which has to be scanned
     * @param rStrings lists all subStrings wich have to be replaced
     * @return String with replaced special characters
     */
    protected String scanChar(String testString, ArrayList rStrings) {
        CmsHtmlConverterObjectReplaceExtendedChars testObject = new CmsHtmlConverterObjectReplaceExtendedChars();
        String searchString,replaceItem;
        for (int i=0;i<rStrings.size();i++) {
            testObject=(CmsHtmlConverterObjectReplaceExtendedChars)(rStrings.get(i));
            searchString=testObject.getSearchString();
            replaceItem=testObject.getReplaceItem();
            testString=replaceString(testString,searchString,replaceItem);
        }
        return testString;
    }

    /**
     * Method to replace a subString with replaceItem.
     * @param testString the original String
     * @param searchString the subString that has to be replaced
     * @param replaceItem the String that replaces searchString
     * @return String with replaced subStrings
     */
    protected String replaceString(String testString, String searchString, String replaceItem) {
        /* if searchString isn't in testString, return (better performance) */
        if (testString.indexOf(searchString) == -1) {
            return testString;
        }
        int tempIndex = 0;
        int searchLen = searchString.length();
        int searchIndex = testString.indexOf(searchString);
        StringBuffer returnString = new StringBuffer(testString.length());
        while (searchIndex != -1) {
            returnString.append(testString.substring(0,searchIndex));
            returnString.append(replaceItem);
            tempIndex = searchIndex+searchLen;
            testString = testString.substring(tempIndex);
            searchIndex = testString.indexOf(searchString);
        }
        returnString.append(testString);
        return returnString.toString();
    }

    /**
     * Method to scan attributes of a node and return the value.
     * @param node the node which is tested
     * @param attrName String with attribute name
     * @return String with attribute value
     */
    protected String scanNodeAttrs(Node node, String attrName) {
        NamedNodeMap attrs = node.getAttributes();
        for ( int i = attrs.getLength()-1; i >= 0 ; i-- ) {
            if (attrName.equalsIgnoreCase(attrs.item(i).getNodeName())) {
                return attrs.item(i).getNodeValue();
            }
        }
        return "";
    }

    protected String modifyParameter(URL orgUrl, String parameter, String prefix, String relativeRoot) {
        try {
            URL myURL = new URL(parameter);
            parameter = myURL.getFile();
            String reference = myURL.getRef();
            if(reference != null){
                parameter += "#"+reference;
            }
        }
        catch (MalformedURLException e) {
            if(!parameter.startsWith("/")){
                //this is a relative link
                try{
                    URL newUrl = new URL(orgUrl, parameter);
                    parameter = newUrl.getFile();
                    String reference = newUrl.getRef();
                    if(reference != null){
                        parameter += "#"+reference;
                    }
                }catch(MalformedURLException exc){
                }
            }
        }
        // remove the servletprefix
        if(prefix != null && !"".equals(prefix)){
            if(parameter.startsWith(prefix)){
                parameter = parameter.substring(prefix.length());
            }
        }
        // check if we need a relative path for this uri
        if ((relativeRoot != null) && parameter.startsWith(relativeRoot)) {
            // uri is located in the relative root folder
            String source = orgUrl.getFile();
            if (source.startsWith(prefix)) source = source.substring(prefix.length());
            parameter = OpenCms.getLinkManager().getRelativeUri(source, parameter);
        }
        return parameter;
    }

    protected boolean shouldReplaceUrl(URL orgUrl, String valueParam, String servletUri) {

        // HACK: if this link has already a link tag in it don't replace it
        // this is only for a special project and should be removed sometime...
        if (valueParam != null) {
            if (valueParam.indexOf("<link>") != -1 || valueParam.indexOf("<LINK>") != -1) {
                return false;
            }
            if (valueParam.startsWith("#")) {
                // its an anchor in the same page
                return false;
            }
            if (valueParam.toLowerCase().startsWith("javascript:")) {
                // it is a javascript (or Javasript or JavaScript or ...)
                return false;
            }
        }
        if (orgUrl == null) {
            return false;
        }

        if (valueParam == null) {
            return false;
        }

        URL paramUrl = null;
        String protocol = null;

        // replace protocol by "http://"
        String spec = valueParam.trim();

        // get protocol
        int index = spec.indexOf(":");
        if (index >= 0) {
            String tmpProtocol = spec.substring(0, index).trim().toLowerCase();
            if ((tmpProtocol.length() > 0) && Character.isLetter(tmpProtocol.charAt(0))) {
                char c;
                boolean isValid = true;
                for (int i = 1; i < tmpProtocol.length(); i++) {
                    c = tmpProtocol.charAt(i);
                    if (!Character.isLetterOrDigit(c) && c != '.' && c != '+' && c != '-') {
                        isValid = false;
                        break;
                    }
                }
                if (isValid) {
                    spec = "http" + spec.substring(index);
                    protocol = tmpProtocol;
                }
            }
        }

        try {
            // parse URL
            paramUrl = new URL(spec);
            if (protocol == null) {
                protocol = paramUrl.getProtocol();
            }
        } catch (MalformedURLException e) {
            return true;
        }

        if (orgUrl.getProtocol().equalsIgnoreCase(protocol)
            && orgUrl.getHost().equalsIgnoreCase(paramUrl.getHost())
            && paramUrl.getFile().startsWith(servletUri) 
            ) {
            if (paramUrl.getFile() == null || "".equals(paramUrl.getFile())) {
                return false;
            } else {
                return true;
            }
        }
        return false;
    }
        
    protected String reconstructTag(String replace, Node node, String param, String quotationMark) {
        StringBuffer tempString = new StringBuffer("");
        tempString.append("<");
        tempString.append(node.getNodeName());
        NamedNodeMap attrs = node.getAttributes();
        for ( int i = attrs.getLength()-1; i >= 0 ; i-- ) {
            tempString.append(" ");
            if (attrs.item(i).getNodeName().equals(param)) {
                tempString.append(param);
                tempString.append("=");
                tempString.append(quotationMark);
                tempString.append(replace);
            }
            else {
                tempString.append(attrs.item(i).getNodeName());
                tempString.append("=");
                tempString.append(quotationMark);
                tempString.append(attrs.item(i).getNodeValue());
            }
            tempString.append(quotationMark);
        }
        tempString.append(">");
        return tempString.toString();
    }
}