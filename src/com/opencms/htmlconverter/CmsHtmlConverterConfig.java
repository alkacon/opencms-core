/*
* File   : $Source: /alkacon/cvs/opencms/src/com/opencms/htmlconverter/Attic/CmsHtmlConverterConfig.java,v $
* Date   : $Date: 2005/02/18 15:18:52 $
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

package com.opencms.htmlconverter;

import java.util.*;
import org.w3c.tidy.Tidy;
import org.w3c.dom.*;
import java.io.*;

/**
 * Configuration class for CmsHtmlConverter, user defined configuration
 * is parsed and used by CmsHtmlConverter.
 * @author Andreas Zahner
 * @version 0.6
 * 
 * @deprecated Will not be supported past the OpenCms 6 release.
 */
final class CmsHtmlConverterConfig {

    /** Comment node in xml-configuration. */
    private static final int C_COMMENT_NODE = 0;
    /** "ConverterConfig" node in xml-configuration. */
    private static final int C_CONFIG_NODE = 1;
    /** "defaults" node in xml-configuration. */
    private static final int C_DEFAULT_NODE = 10;
    /** "replacecontent" node in xml-configuration. */
    private static final int C_REPLACECONTENT_NODE = 11;
    /** "inlinetags" node in xml-configuration. */
    private static final int C_INLINETAG_NODE = 12;
    /** "removetags" node in xml-configuration. */
    private static final int C_REMOVETAG_NODE = 13;
    /** "removeblocks" node in xml-configuration. */
    private static final int C_REMOVEBLOCK_NODE = 14;
    /** "replacetags" node in xml-configuration. */
    private static final int C_REPLACETAG_NODE = 15;
    /** "replaceblocks" node in xml-configuration. */
    private static final int C_REPLACEBLOCK_NODE = 16;
    /** "replacestrings" node in xml-configuration. */
    private static final int C_REPLACESTRING_NODE = 17;

    /** "xhtmloutput" node in defaults: xml-configuration. */
    private static final int C_XHTMLOUTPUT_NODE = 20;
    /** "globalprefix" node in defaults: xml-configuration. */
    private static final int C_GLOBALPREFIX_NODE = 21;
    /** "globalsuffix" node in defaults: xml-configuration. */
    private static final int C_GLOBALSUFFIX_NODE = 22;
    /** "globaladdeveryline" node in defaults: xml-configuration. */
    private static final int C_GLOBALADDEVERYLINE_NODE = 23;
    /** "usebrackets" node in defaults: xml-configuration. */
    private static final int C_USEBRACKETS_NODE = 24;
    /** "encodequotationmarks" node in defaults: xml-configuration. */
    private static final int C_ENCODEQUOTATIONMARKS_NODE = 25;

    /** "addeveryline" node in replacenodes: xml-configuration. */
    private static final int C_ADDEVERYLINE_NODE = 30;
    /** "prefix" node in replacenodes: xml-configuration. */
    private static final int C_PREFIX_NODE = 31;
    /** "suffix" node in replacenodes: xml-configuration. */
    private static final int C_SUFFIX_NODE = 32;
    /** "tag" node in replacenodes: xml-configuration. */
    private static final int C_TAG_NODE = 33;
    /** "string" node in replacenodes: xml-configuration. */
    private static final int C_STRING_NODE = 34;

    /** check if output has to be XHTML. */
    private boolean m_xhtmlOutput;
    /** stores global prefix. */
    private String m_globalPrefix = "";
    /** stores global suffix. */
    private String m_globalSuffix = "";
    /** check if prefix and suffix have to be added to every new line. */
    private boolean m_globalAddEveryLine;
    /** code for html bracket "<". */
    private String m_openBracket = "";
    /** code for html bracket ">". */
    private String m_closeBracket = "";
    /** check if brackets are used. */
    private boolean m_useBrackets;
    /** check if quotationmarks must be encoded. */
    private boolean m_encodeQuotationmarks;
    /** stores code for quotationmarks. */
    private String m_quotationmark = "";
    /** stores prefix for replacetags. */
    private String m_replaceTagsPrefix = "";
    /** stores suffix for replacetags. */
    private String m_replaceTagsSuffix = "";
    /** check if replaced content needs new lines. */
    private boolean m_replaceTagsAddEveryLine;
    /** check if default values are used. */
    private boolean m_replaceTagsUseDefaults;

    /** stores prefix for replaceblocks. */
    private String m_replaceBlocksPrefix = "";
    /** stores suffix for replaceblocks. */
    private String m_replaceBlocksSuffix = "";
    /** check if replaced content needs new lines. */
    private boolean m_replaceBlocksAddEveryLine;
    /** check if default values are used. */
    private boolean m_replaceBlocksUseDefaults;

    /** stores prefix for replacestrings. */
    private String m_replaceStringsPrefix = "";
    /** stores suffix for replacestrings. */
    private String m_replaceStringsSuffix = "";
    /** check if replaced content needs new lines. */
    private boolean m_replaceStringsAddEveryLine;
    /** check if default values are used. */
    private boolean m_replaceStringsUseDefaults;

    /** list with objects which will be replaced in run #1. */
    private ArrayList m_replaceContent = new ArrayList();
    /** list with tags to be removed. */
    private HashSet m_removeTags = new HashSet();
    /** list with blocks to be removed. */
    private HashSet m_removeBlocks = new HashSet();
    /** list with all inline tags. */
    private HashSet m_inlineTags = new HashSet();
    /** list with extended chars which are replaced in text nodes. */
    private ArrayList m_replaceExtendedChars = new ArrayList();
    /** list with Strings whihch are replaced. */
    private ArrayList m_replaceStrings = new ArrayList();
    /** list with tags which are replaced. */
    private ArrayList m_replaceTags = new ArrayList();
    /** list with blocks which are replaced. */
    private ArrayList m_replaceBlocks = new ArrayList();

    /** temporary list of replacecontent. */
    private ArrayList m_tempReplaceContent = new ArrayList();
    /** temporary list of replacestrings. */
    private ArrayList m_tempReplaceStrings = new ArrayList();
    /** temporary list of replacetags. */
    private ArrayList m_tempReplaceTags = new ArrayList();
    /** temporary list of replaceblocks. */
    private ArrayList m_tempReplaceBlocks = new ArrayList();

    /** instance of CmsHtmlConverterTools. */
    private CmsHtmlConverterTools m_tools = new CmsHtmlConverterTools();

    /** to check if instance has already been configured. */
    private boolean m_isConfigured;

    /**
     * Default constructor.<p>
     */
    protected CmsHtmlConverterConfig() {
        // empty
    }

    /**
     * Creates configuration from String.<p>
     * 
     * @param confString String with XML configuration
     */
    protected CmsHtmlConverterConfig(String confString) {
        init(confString);
    }

    /**
     * Creates configuration from InputStream.<p>
     * 
     * @param in InputStream with XML configuration
     */
    protected CmsHtmlConverterConfig(InputStream in) {
        init(in);
    }

    /**
     * Returns all tags which are removed from html input.<p>
     * 
     * @return HashSet with tags
     */
    protected HashSet getRemoveTags() {
        return m_removeTags;
    }

    /**
     * Returns all blocks which are removed from html input.<p>
     * 
     * @return HashSet with blocks
     */
    protected HashSet getRemoveBlocks() {
        return m_removeBlocks;
    }

    /**
     * Returns all inline tags defined in configuration.<p>
     * 
     * @return HashSet with inline tags
     */
    protected HashSet getInlineTags() {
        return m_inlineTags;
    }

    /**
     * Returns all Strings which are replaced in run #1.<p>
     * 
     * @return ArrayList with CmsHtmlConverterObjectReplaceContent
     */
    protected ArrayList getReplaceContent() {
        return m_replaceContent;
    }

    /**
     * Returns extended characters which are replaced in text nodes.<p>
     * 
     * @return ArrayList with CmsHtmlConverterObjectReplaceExtendedChars
     */
    protected ArrayList getReplaceExtendedChars() {
        return m_replaceExtendedChars;
    }

    /**
     * Returns all Strings which are replaced in run #2.<p>
     * 
     * @return ArrayList with CmsHtmlConverterObjectReplaceStrings
     */
    protected ArrayList getReplaceStrings() {
        return m_replaceStrings;
    }

    /**
     * Returns all tags which are replaced in html input.<p>
     * 
     * @return ArrayList with CmsHtmlConverterObjectReplaceTags
     */
    protected ArrayList getReplaceTags() {
        return m_replaceTags;
    }

    /**
     * Returns all Blocks which are replaced in html input.<p>
     * 
     * @return ArrayList with CmsHtmlConverterObjectReplaceBlocks
     */
    protected ArrayList getReplaceBlocks() {
        return m_replaceBlocks;
    }

    /**
     * Returns boolean if output is in XHTML format.<p>
     * 
     * @return true, if output has to be XHTML, otherwise false
     */
    protected boolean getXhtmlOutput() {
        return m_xhtmlOutput;
    }

    /**
     * Returns global prefix defined in configuration.<p>
     * 
     * @return String with global prefix
     */
    protected String getGlobalPrefix() {
        return m_globalPrefix;
    }

    /**
     * Returns global suffix defined in configuration.<p>
     * 
     * @return String with global suffix
     */
    protected String getGlobalSuffix() {
        return m_globalSuffix;
    }

    /**
     * Returns boolean if global prefix and suffix have to be added to every new line.<p>
     * 
     * @return true, if prefix and suffix are added in every new line, otherwise false
     */
    protected boolean getGlobalAddEveryLine() {
        return m_globalAddEveryLine;
    }

    /**
     * Returns boolean if encoded brackets are used.<p>
     * 
     * @return true, when encoded brackets are defined, otherwise false
     */
    protected boolean getUseBrackets() {
        return m_useBrackets;
    }

    /**
     * Returns boolean if quotationmarks must be encoded.<p>
     * 
     * @return true, when quotationmarks must be encoded
     */
    protected boolean getEncodeQuotationmarks() {
        return m_encodeQuotationmarks;
    }

    /**
     * Returns the String for quotationmarks.<p>
     * 
     * @return String with value for quotationmarks
     */
    protected String getQuotationmark() {
        return m_quotationmark;
    }

    /**
     * Adds new object to ArrayList m_replaceTags.<p>
     * 
     * @param prefix prefix String
     * @param tagName tagname String
     * @param attributeName attributeName String
     * @param attributeValue attributeNalue String
     * @param replaceStartTag replaceStartTag String
     * @param replaceEndTag replaceEndTag String
     * @param suffix suffix String
     * @param getReplaceFromAttrs getReplaceFromAttrs boolean
     * @param startAttribute startAttribute String
     * @param endAttribute endAttribute String
     * @param parameter parameter String
     * @param replaceParamAttr flag to indicate the parameter replacement
     * @return true if object was successfully added, otherwise false
     */
    protected boolean addObjectReplaceTag(String prefix, String tagName,
            String attributeName, String attributeValue, String replaceStartTag,
            String replaceEndTag, String suffix, boolean getReplaceFromAttrs,
            String startAttribute, String endAttribute, String parameter, boolean replaceParamAttr) {

        return m_replaceTags.add(
            new CmsHtmlConverterObjectReplaceTags(
                prefix,
                tagName,
                attributeName,
                attributeValue,
                replaceStartTag,
                replaceEndTag,
                suffix,
                getReplaceFromAttrs,
                startAttribute,
                endAttribute,
                parameter,
                replaceParamAttr));
    }

    /**
     * Removes object from position index from ArrayList m_replaceTags.<p>
     * 
     * @param index index of removed object
     */
    protected void removeObjectReplaceTag(int index) {
        m_replaceTags.remove(index);
    }

    /**
     * Adds new object to ArrayList m_replaceBlocks.<p>
     * 
     * @param prefix prefix String
     * @param tagName tagname String
     * @param attributeName attributeName String
     * @param attributeValue attributeNalue String
     * @param replaceString replaceString String
     * @param suffix suffix String
     * @param getReplaceFromAttrs getReplaceFromAttrs boolean
     * @param replaceAttribute replaceAttribute String
     * @param parameter parameter String
     * @return true if object was successfully added, otherwise false
     */
    protected boolean addObjectReplaceBlock(String prefix, String tagName,
            String attributeName, String attributeValue, String replaceString,
            String suffix, boolean getReplaceFromAttrs, String replaceAttribute, String parameter) {

        return m_replaceBlocks.add(
            new CmsHtmlConverterObjectReplaceBlocks(
                prefix,
                tagName,
                attributeName,
                attributeValue,
                replaceString,
                suffix,
                getReplaceFromAttrs,
                replaceAttribute,
                parameter));
    }

    /**
     * Removes object from position index from ArrayList m_replaceBlocks.<p>
     * 
     * @param index index of removed object
     */
    protected void removeObjectReplaceBlock(int index) {
        m_replaceBlocks.remove(index);
    }

    /**
     * Scans a String for coded bracket subStrings and replaces them.<p>
     * 
     * @param content String to scan
     * @return String with real "<" and ">"
     */
    protected String scanBrackets(String content) {
        content = m_tools.replaceString(content, m_openBracket, "<");
        content = m_tools.replaceString(content, m_closeBracket, ">");
        return content;
    }

    /**
     * Initialises configuration from String.<p>
     * 
     * @param confString String with XML configuration
     */
    protected void init(String confString) {
        InputStream in = new ByteArrayInputStream(confString.getBytes());
        init(in);
    }

    /**
     * Initialises configuration from InputStream.<p>
     * 
     * @param in InputStream with XML configuration
     */
    protected void init(InputStream in) {
        Node node;
        Tidy tidy = new Tidy();
        /* initialise Tidy */
        tidy.setXmlTags(true);
        tidy.setQuiet(true);
        tidy.setOnlyErrors(true);
        tidy.setShowWarnings(false);
        /* if converter was configured, clear previous configuration */
        if (m_isConfigured) {
            clearConfiguration();
        }
        node = tidy.parseDOM(in, null);
        parseConfig(node);
        buildObjects();
        m_isConfigured = true;
    }

    /**
     * Clears all variables and objects if the configuration is redefined.<p>
     */
    private void clearConfiguration() {
        m_globalAddEveryLine = false;
        m_useBrackets = false;
        m_globalPrefix = "";
        m_globalSuffix = "";
        m_openBracket = "";
        m_closeBracket = "";
        m_encodeQuotationmarks = false;
        m_quotationmark = "";
        m_replaceTagsPrefix = "";
        m_replaceTagsSuffix = "";
        m_replaceTagsAddEveryLine = false;
        m_replaceTagsUseDefaults = false;
        m_replaceBlocksPrefix = "";
        m_replaceBlocksSuffix = "";
        m_replaceBlocksAddEveryLine = false;
        m_replaceBlocksUseDefaults = false;
        m_replaceStringsPrefix = "";
        m_replaceStringsSuffix = "";
        m_replaceStringsAddEveryLine = false;
        m_replaceStringsUseDefaults = false;
        m_tempReplaceContent.clear();
        m_tempReplaceStrings.clear();
        m_tempReplaceTags.clear();
        m_tempReplaceBlocks.clear();
        m_replaceContent.clear();
        m_removeTags.clear();
        m_removeBlocks.clear();
        m_inlineTags.clear();
        m_replaceExtendedChars.clear();
        m_replaceStrings.clear();
        m_replaceTags.clear();
        m_replaceBlocks.clear();
        m_isConfigured = false;
    }

    /**
     * Builds all objects after XML configuration was parsed.<p>
     */
    private void buildObjects() {
        boolean added = true;
        /* scan global prefix and suffix for brackets */
        if (m_useBrackets) {
            m_globalPrefix = scanBrackets(m_globalPrefix);
            m_globalSuffix = scanBrackets(m_globalSuffix);
        }
        /* set normal quotationmarks, if no encoding is required */
        if (!m_encodeQuotationmarks) {
            m_quotationmark = "\"";
        }
        /* builds ArrayList m_replaceContent */
        buildObjectReplaceContent();
        /* builds ArrayList m_replaceTags */
        buildObjectReplaceTags();
        /* builds ArrayList m_replaceBlocks */
        buildObjectReplaceBlocks();
        /* builds ArrayList m_replaceStrings */
        buildObjectReplaceStrings();
        /* build ArrayList m_extendedChars */
        // Encoding project:
        // String Chars = "ä,&auml;,Ä,&Auml;,ö,&ouml;,Ö,&Ouml;,ü,&uuml;,Ü,&Uuml;,ß,&szlig;,©,&copy;,\",&quot;,<,&lt;,>,&gt;,&lt;!--,<!--,--&gt;,-->,€,&euro;";
        String Chars = "\",&quot;,<,&lt;,>,&gt;";
        StringTokenizer T = new StringTokenizer(Chars, ",");
        while (T.hasMoreTokens()) {
            added = m_replaceExtendedChars.add(new CmsHtmlConverterObjectReplaceExtendedChars(T.nextToken(), T.nextToken()));
            if (!added) {
                System.err.println("Configuration error: failed adding object to ArrayList m_replaceExtendedChars.");
            }
        }
        /* clear temporary ArrayLists */
        m_tempReplaceContent.clear();
        m_tempReplaceStrings.clear();
        m_tempReplaceTags.clear();
        m_tempReplaceBlocks.clear();
    }

    /**
     * Creates ArrayList of objects with CmsHtmlConverterObjectReplaceContent.<p>
     */
    private void buildObjectReplaceContent() {
        boolean added = true;
        int len = m_tempReplaceContent.size();
        String content, replace;
        CmsHtmlConverterObjectReplaceContent objectReplaceContent = new CmsHtmlConverterObjectReplaceContent();
        /* get temporary objects and scan replace for encoded brackets */
        for (int i=0; i<len; i++) {
            objectReplaceContent = (CmsHtmlConverterObjectReplaceContent) (m_tempReplaceContent.get(i));
            content = objectReplaceContent.getSearchString();
            replace = objectReplaceContent.getReplaceItem();
            if (m_useBrackets) {
                replace = scanBrackets(replace);
            }
            added = m_replaceContent.add(new CmsHtmlConverterObjectReplaceContent(content, replace));
            if (!added) {
                System.err.println("Configuration error: failed adding object to ArrayList m_replaceContent.");
            }
        }
        added = m_replaceContent.add(new CmsHtmlConverterObjectReplaceContent("&nbsp;", "$nbsp$"));
        if (!added) {
                System.err.println("Configuration error: failed adding object to ArrayList m_replaceContent.");
        }
    }

    /**
     * Creates ArrayList of objects with CmsHtmlConverterObjectReplaceTags.<p>
     */
    private void buildObjectReplaceTags() {
        boolean added = true;
        int len = m_tempReplaceTags.size();
        String name, attrib, value, replaceStartTag, replaceEndTag, startAttribute, endAttribute, parameter;
        boolean getReplaceFromAttrs, replaceParamAttr;
        CmsHtmlConverterObjectReplaceTags objectReplaceTags = new CmsHtmlConverterObjectReplaceTags();
        /* create prefix, suffix for replaceTags  */
        if (m_useBrackets) {
                m_replaceTagsPrefix = scanBrackets(m_replaceTagsPrefix);
                m_replaceTagsSuffix = scanBrackets(m_replaceTagsSuffix);
        }
        if (m_replaceTagsUseDefaults) {
                m_replaceTagsPrefix = m_globalSuffix;
                m_replaceTagsSuffix = m_globalPrefix;
                m_replaceTagsAddEveryLine = m_globalAddEveryLine;
        }
        if (m_replaceTagsAddEveryLine) {
                m_replaceTagsPrefix += "\n";
                m_replaceTagsSuffix = "\n" + m_replaceTagsSuffix;
        }
        /* get temporary objects and add them to ArrayList m_replaceTags */
        for (int i=0; i<len; i++) {
            objectReplaceTags = (CmsHtmlConverterObjectReplaceTags) (m_tempReplaceTags.get(i));
            name = objectReplaceTags.getTagName();
            attrib = objectReplaceTags.getTagAttrib();
            value = objectReplaceTags.getTagAttribValue();
            replaceStartTag = objectReplaceTags.getReplaceStartTag();
            replaceEndTag = objectReplaceTags.getReplaceEndTag();
            getReplaceFromAttrs = objectReplaceTags.getReplaceFromAttrs();
            startAttribute = objectReplaceTags.getStartAttribute();
            endAttribute = objectReplaceTags.getEndAttribute();
            parameter = objectReplaceTags.getParameter();
            replaceParamAttr = objectReplaceTags.getReplaceParamAttr();
            if (m_useBrackets) {
                replaceStartTag = scanBrackets(replaceStartTag);
                replaceEndTag = scanBrackets(replaceEndTag);
            }
            added =
                m_replaceTags.add(
                    new CmsHtmlConverterObjectReplaceTags(
                        m_replaceTagsPrefix,
                        name,
                        attrib,
                        value,
                        replaceStartTag,
                        replaceEndTag,
                        m_replaceTagsSuffix,
                        getReplaceFromAttrs,
                        startAttribute,
                        endAttribute,
                        parameter,
                        replaceParamAttr));
            if (!added) {
                System.err.println("Configuration error: failed adding object to ArrayList m_replaceTags.");
            }
        }
    }

    /**
     * Creates ArrayList of objects with CmsHtmlConverterObjectReplaceBlocks.<p>
     */
    private void buildObjectReplaceBlocks() {
        boolean added = true;
        int len = m_tempReplaceBlocks.size();
        String name, attrib, value, replaceString, replaceAttribute, parameter;
        boolean getReplaceFromAttrs;
        CmsHtmlConverterObjectReplaceBlocks objectReplaceBlocks = new CmsHtmlConverterObjectReplaceBlocks();
        /* create prefix, suffix for replaceBlocks  */
        if (m_useBrackets) {
                m_replaceBlocksPrefix = scanBrackets(m_replaceBlocksPrefix);
                m_replaceBlocksSuffix = scanBrackets(m_replaceBlocksSuffix);
        }
        if (m_replaceBlocksUseDefaults) {
                m_replaceBlocksPrefix = m_globalSuffix;
                m_replaceBlocksSuffix = m_globalPrefix;
                m_replaceBlocksAddEveryLine = m_globalAddEveryLine;
        }
        if (m_replaceBlocksAddEveryLine) {
                m_replaceBlocksPrefix += "\n";
                m_replaceBlocksSuffix = "\n" + m_replaceBlocksSuffix;
        }
        /* get temporary objects and add them to ArrayList m_replaceBlocks */
        for (int i=0; i<len; i++) {
            objectReplaceBlocks = (CmsHtmlConverterObjectReplaceBlocks) (m_tempReplaceBlocks.get(i));
            name = objectReplaceBlocks.getTagName();
            attrib = objectReplaceBlocks.getTagAttrib();
            value = objectReplaceBlocks.getTagAttribValue();
            replaceString = objectReplaceBlocks.getReplaceString();
            getReplaceFromAttrs = objectReplaceBlocks.getReplaceFromAttrs();
            replaceAttribute = objectReplaceBlocks.getReplaceAttribute();
            parameter = objectReplaceBlocks.getParameter();
            if (m_useBrackets) {
                replaceString = scanBrackets(replaceString);
            }
            added =
                m_replaceBlocks.add(
                    new CmsHtmlConverterObjectReplaceBlocks(
                        m_replaceBlocksPrefix,
                        name,
                        attrib,
                        value,
                        replaceString,
                        m_replaceBlocksSuffix,
                        getReplaceFromAttrs,
                        replaceAttribute,
                        parameter));
            if (!added) {
                System.err.println("Configuration error: failed adding object to ArrayList m_replaceBlocks.");
            }
        }
    }

    /**
     * Creates ArrayList of objects with CmsHtmlConverterObjectReplaceStrings.<p>
     */
    private void buildObjectReplaceStrings() {
        boolean added = true;
        int len = m_tempReplaceStrings.size();
        String content, replace;
        CmsHtmlConverterObjectReplaceStrings objectReplaceStrings = new CmsHtmlConverterObjectReplaceStrings();
        /* create prefix, suffix for replaceStrings  */
        if (m_useBrackets) {
                m_replaceStringsPrefix = scanBrackets(m_replaceStringsPrefix);
                m_replaceStringsSuffix = scanBrackets(m_replaceStringsSuffix);
        }
        if (m_replaceStringsUseDefaults) {
                m_replaceStringsPrefix = m_globalSuffix;
                m_replaceStringsSuffix = m_globalPrefix;
                m_replaceStringsAddEveryLine = m_globalAddEveryLine;
        }
        if (m_replaceStringsAddEveryLine) {
                m_replaceStringsPrefix += "\n";
                m_replaceStringsSuffix = "\n" + m_replaceStringsSuffix;
        }
        /* get temporary objects and add them to ArrayList m_replaceStrings */
        for (int i=0; i<len; i++) {
            objectReplaceStrings = (CmsHtmlConverterObjectReplaceStrings) (m_tempReplaceStrings.get(i));
            content = objectReplaceStrings.getSearchString();
            replace = objectReplaceStrings.getReplaceItem();
            if (m_useBrackets) {
                replace = scanBrackets(replace);
            }
            added =
                m_replaceStrings.add(
                    new CmsHtmlConverterObjectReplaceStrings(
                        content,
                        m_replaceStringsPrefix,
                        replace,
                        m_replaceStringsSuffix));
            if (!added) {
                System.err.println("Configuration error: failed adding object to ArrayList m_replaceStrings.");
            }
        }
        added =
            m_replaceStrings.add(
                new CmsHtmlConverterObjectReplaceStrings(
                    "$nbsp$",
                    "",
                    "&nbsp;",
                    ""));
        if (!added) {
                System.err.println("Configuration error: failed adding object to ArrayList m_replaceStrings.");
        }
    }

    /**
     * Parses XML configuration input.<p>
     * 
     * @param node actual node of XML configuration
     */
    private void parseConfig(Node node) {
        int type = node.getNodeType();
        int len = 0;
        boolean added = true;
        String valueString = "";
        switch (type) {
        case Node.DOCUMENT_NODE:
            parseConfig(((Document)node).getDocumentElement());
            break;
        case Node.ELEMENT_NODE:
            int nodeName = getConfigNodeName(node.getNodeName());
            NodeList children = node.getChildNodes();
            if (children != null) {
                len = children.getLength();
            }
            switch (nodeName) {
            case C_COMMENT_NODE:
                break;
            case C_CONFIG_NODE:
                for (int i = 0; i < len; i++) {
                    /* recursively call parseConfig with all child nodes */
                    parseConfig(children.item(i));
                }
                break;
            case C_DEFAULT_NODE:
                 for (int i = 0; i < len; i++) {
                    /* set defaults in configuration */
                    parseDefaults(children.item(i));
                 }
                 break;
            case C_REPLACECONTENT_NODE:
                for (int i = 0; i < len; i++) {
                    /* add replacecontent to configuration */
                    parseReplaceContent(children.item(i));
                }
                break;
            case C_INLINETAG_NODE:
                for (int i = 0; i < len; i++) {
                    /* add inlinetags to configuration */
                    valueString = parseTag(children.item(i));
                    if (!valueString.equals("")) {
                        added = m_inlineTags.add(new String(valueString));
                    }
                    if (!added) {
                        System.err.println("Configuration error: failed adding object to ArrayList m_inlineTags.");
                    }
                }
                break;
            case C_REMOVETAG_NODE:
                for (int i = 0; i < len; i++) {
                    /* add removetags to configuration */
                    valueString = parseTag(children.item(i));
                    if (!valueString.equals("")) {
                        added = m_removeTags.add(new String(valueString));
                    }
                    if (!added) {
                        System.err.println("Configuration error: failed adding object to ArrayList m_removeTags.");
                    }
                }
                break;
            case C_REMOVEBLOCK_NODE:
                for (int i = 0; i < len; i++) {
                    /* add removeblocks to configuration */
                    valueString = parseTag(children.item(i));
                    if (!valueString.equals("")) {
                        added = m_removeBlocks.add(new String(valueString));
                    }
                    if (!added) {
                        System.err.println("Configuration error: failed adding object to ArrayList m_removeBlocks.");
                    }
                }
                break;
            case C_REPLACETAG_NODE:
                m_replaceTagsUseDefaults = checkBoolean(node, 0);
                for (int i = 0; i < len; i++) {
                    parseReplaceTags(children.item(i));
                }
                break;
            case C_REPLACEBLOCK_NODE:
                m_replaceBlocksUseDefaults = checkBoolean(node, 0);
                for (int i = 0; i < len; i++) {
                    parseReplaceBlocks(children.item(i));
                }
                break;
            case C_REPLACESTRING_NODE:
                m_replaceStringsUseDefaults = checkBoolean(node, 0);
                for (int i = 0; i < len; i++) {
                    parseReplaceStrings(children.item(i));
                }
                break;
            default:
                System.err.println("Configuration error: Tag <"+node.getNodeName()+"> not recognized.");
                break;
            }
        default:
            break;
        }
    }

    /**
     * Reads default values from XML configuration.<p>
     * 
     * @param node analysed node from default block
     */
    private void parseDefaults(Node node) {
        NamedNodeMap attrs = node.getAttributes();
        int nodeName = getConfigNodeName(node.getNodeName());
        String attrName;
        switch (nodeName) {
        case C_COMMENT_NODE:
            break;
        case C_XHTMLOUTPUT_NODE:
            m_xhtmlOutput = checkBoolean(node, 0);
            break;
        case C_GLOBALPREFIX_NODE:
            m_globalPrefix = attrs.item(0).getNodeValue();
            break;
        case C_GLOBALSUFFIX_NODE:
            m_globalSuffix = attrs.item(0).getNodeValue();
            break;
        case C_GLOBALADDEVERYLINE_NODE:
            m_globalAddEveryLine = checkBoolean(node, 0);
            break;
        case C_USEBRACKETS_NODE:
            for (int i=0; i<attrs.getLength(); i++) {
                attrName = attrs.item(i).getNodeName();
                if (attrName.equalsIgnoreCase("value")) {
                    m_useBrackets = checkBoolean(node, i);
                }
                if (attrName.equalsIgnoreCase("openbracket")) {
                    m_openBracket = attrs.item(i).getNodeValue();
                }
                if (attrName.equalsIgnoreCase("closebracket")) {
                    m_closeBracket = attrs.item(i).getNodeValue();
                }
            }
            if ((m_useBrackets) && ((m_openBracket.equals("")) || (m_closeBracket.equals("")))) {
                System.err.println("Fatal configuration error: Wrong definition of <usebrackets> in block <defaults>.");
                System.exit(1);
            }
            break;
        case C_ENCODEQUOTATIONMARKS_NODE:
            for (int i=0; i<attrs.getLength(); i++) {
                attrName = attrs.item(i).getNodeName();
                if (attrName.equalsIgnoreCase("value")) {
                    m_encodeQuotationmarks = checkBoolean(node, i);
                }
                if (attrName.equalsIgnoreCase("code")) {
                    m_quotationmark = attrs.item(i).getNodeValue();
                }
            }
            if ((m_encodeQuotationmarks) && (m_quotationmark.equals(""))) {
                System.err.println("Fatal configuration error: Wrong definition of <encodequotationsmarks> in block <defaults>.");
                System.exit(1);
            }
            break;
        default:
            System.err.println("Fatal configuration error: Tag <"+node.getNodeName()+"> not recognized in block <defaults>.");
            System.exit(1);
        }
    }

    /**
     * Reads replacecontent definitions from XML configuration.<p>
     * 
     * @param node analysed node from replacecontent block
     */
    private void parseReplaceContent(Node node) {
        if (node.getNodeName().equals("#comment")) {
            return;
        }
        NamedNodeMap attrs = node.getAttributes();
        String attrName;
        String content = "";
        String replace = "";
        boolean added = true;
        for (int i=0; i<attrs.getLength(); i++) {
            attrName = attrs.item(i).getNodeName();
            if (attrName.equalsIgnoreCase("content")) {
                content = attrs.item(i).getNodeValue();
            }
            if (attrName.equalsIgnoreCase("replace")) {
                replace = attrs.item(i).getNodeValue();
            }
        }
        if (content.equals("")) {
            System.err.println("Fatal configuration error: Empty content in block <replacecontent>.");
            System.exit(1);
        }
        added = m_tempReplaceContent.add(new CmsHtmlConverterObjectReplaceContent(content, replace));
        if (!added) {
            System.err.println("Configuration error: Failed adding object to ArrayList m_tempReplaceContent.");
        }
    }

    /**
     * Reads replacetags definitions from XML configuration.<p>
     * 
     * @param node analysed node from replacetags block
     */
    private void parseReplaceTags(Node node) {
        boolean added = true;
        NamedNodeMap attrs = node.getAttributes();
        int nodeName = getConfigNodeName(node.getNodeName());
        String attrName;
        String attrNameValue = "";
        String attrAttribValue = "";
        String attrValueValue = "";
        String attrStartValue = "";
        String attrEndValue = "";
        boolean attrGetReplace = false;
        String attrStartAttribute = "";
        String attrEndAttribute = "";
        String attrParameter = "";
        boolean attrReplaceParamAttr = false;
        switch (nodeName) {
        case C_COMMENT_NODE:
            break;
        case C_PREFIX_NODE:
            //m_replaceTagsPrefix = attrs.item(0).getNodeValue();
            m_replaceTagsPrefix = attrs.getNamedItem("value").getNodeValue();
            break;
        case C_SUFFIX_NODE:
            m_replaceTagsSuffix = attrs.item(0).getNodeValue();
            break;
        case C_ADDEVERYLINE_NODE:
            m_replaceTagsAddEveryLine = checkBoolean(node, 0);
            break;
        case C_TAG_NODE:
            for (int i=0; i<attrs.getLength(); i++) {
                attrName = attrs.item(i).getNodeName();
                if (attrName.equalsIgnoreCase("name")) {
                    attrNameValue = attrs.item(i).getNodeValue();
                }
                if (attrName.equalsIgnoreCase("attrib")) {
                    attrAttribValue = attrs.item(i).getNodeValue();
                }
                if (attrName.equalsIgnoreCase("value")) {
                    attrValueValue = attrs.item(i).getNodeValue();
                }
                if (attrName.equalsIgnoreCase("replacestarttag")) {
                    attrStartValue = attrs.item(i).getNodeValue();
                }
                if (attrName.equalsIgnoreCase("replaceendtag")) {
                    attrEndValue = attrs.item(i).getNodeValue();
                }
                if (attrName.equalsIgnoreCase("getreplacefromattrs")) {
                    attrGetReplace = checkBoolean(node, i);
                }
                if (attrName.equalsIgnoreCase("startattribute")) {
                    attrStartAttribute = attrs.item(i).getNodeValue();
                }
                if (attrName.equalsIgnoreCase("endattribute")) {
                    attrEndAttribute = attrs.item(i).getNodeValue();
                }
                if (attrName.equalsIgnoreCase("parameter")) {
                    attrParameter = attrs.item(i).getNodeValue();
                }
                if (attrName.equalsIgnoreCase("replaceparamattr")) {
                    attrReplaceParamAttr = checkBoolean(node, i);
                }
            }
            if (attrNameValue.equals("")) {
                System.err.println("Fatal configuration error: Tagname not specified in block <replacetags>.");
                System.exit(1);
            }
            added =
                m_tempReplaceTags.add(
                    new CmsHtmlConverterObjectReplaceTags(
                        "",
                        attrNameValue,
                        attrAttribValue,
                        attrValueValue,
                        attrStartValue,
                        attrEndValue,
                        "",
                        attrGetReplace,
                        attrStartAttribute,
                        attrEndAttribute,
                        attrParameter,
                        attrReplaceParamAttr));
            if (!added) {
                System.err.println("Configuration error: Failed adding object to ArrayList m_tempReplaceTags.");
            }
            break;
        default:
            System.err.println("Fatal configuration error: Tag <"+node.getNodeName()+"> not recognized in block <replacetags>.");
            System.exit(1);
        }
    }

    /**
     * Reads replaceblocks definitions from XML configuration.<p>
     * 
     * @param node analysed node from replaceblocks block
     */
    private void parseReplaceBlocks(Node node) {
        boolean added = false;
        NamedNodeMap attrs = node.getAttributes();
        int nodeName = getConfigNodeName(node.getNodeName());
        String attrName;
        String attrNameValue = "";
        String attrAttribValue = "";
        String attrValueValue = "";
        String attrStringValue = "";
        boolean attrGetReplace = false;
        String attrReplaceAttribute = "";
        String attrParameter = "";
        switch (nodeName) {
        case C_COMMENT_NODE:
            break;
        case C_PREFIX_NODE:
            m_replaceBlocksPrefix = attrs.item(0).getNodeValue();
            break;
        case C_SUFFIX_NODE:
            m_replaceBlocksSuffix = attrs.item(0).getNodeValue();
            break;
        case C_ADDEVERYLINE_NODE:
            m_replaceBlocksAddEveryLine = checkBoolean(node, 0);
            break;
        case C_TAG_NODE:
            for (int i=0; i<attrs.getLength(); i++) {
                attrName = attrs.item(i).getNodeName();
                if (attrName.equalsIgnoreCase("name")) {
                    attrNameValue = attrs.item(i).getNodeValue();
                }
                if (attrName.equalsIgnoreCase("attrib")) {
                    attrAttribValue = attrs.item(i).getNodeValue();
                }
                if (attrName.equalsIgnoreCase("value")) {
                    attrValueValue = attrs.item(i).getNodeValue();
                }
                if (attrName.equalsIgnoreCase("replacestring")) {
                    attrStringValue = attrs.item(i).getNodeValue();
                }
                if (attrName.equalsIgnoreCase("getreplacefromattrs")) {
                    attrGetReplace = checkBoolean(node, i);
                }
                if (attrName.equalsIgnoreCase("replaceattribute")) {
                    attrReplaceAttribute = attrs.item(i).getNodeValue();
                }
                if (attrName.equalsIgnoreCase("parameter")) {
                    attrParameter = attrs.item(i).getNodeValue();
                }
            }
            added =
                m_tempReplaceBlocks.add(
                    new CmsHtmlConverterObjectReplaceBlocks(
                        "",
                        attrNameValue,
                        attrAttribValue,
                        attrValueValue,
                        attrStringValue,
                        "",
                        attrGetReplace,
                        attrReplaceAttribute,
                        attrParameter));
            if (!added) {
                System.err.println("Configuration error: Failed adding object to ArrayList m_tempReplaceBlocks.");
            }
            if (attrNameValue.equals("")) {
                System.err.println("Fatal configuration error: Tagname not specified in block <replaceblocks>.");
                System.exit(1);
            }

            break;
        default:
            System.err.println("Fatal configuration error: Tag <"+node.getNodeName()+"> not recognized in block <replaceblocks>.");
            System.exit(1);
        }
    }

    /**
     * Reads replacestrings definitions from XML configuration.<p>
     * 
     * @param node analysed node from replacestrings block
     */
    private void parseReplaceStrings(Node node) {
        boolean added = false;
        NamedNodeMap attrs = node.getAttributes();
        int nodeName = getConfigNodeName(node.getNodeName());
        String attrName;
        String attrContentValue = "";
        String attrReplaceValue = "";
        switch (nodeName) {
        case C_COMMENT_NODE:
            break;
        case C_PREFIX_NODE:
            m_replaceStringsPrefix = attrs.item(0).getNodeValue();
            break;
        case C_SUFFIX_NODE:
            m_replaceStringsSuffix = attrs.item(0).getNodeValue();
            break;
        case C_ADDEVERYLINE_NODE:
            m_replaceStringsAddEveryLine = checkBoolean(node, 0);
            break;
        case C_STRING_NODE:
            for (int i=0; i<attrs.getLength(); i++) {
                attrName = attrs.item(i).getNodeName();
                if (attrName.equalsIgnoreCase("content")) {
                    attrContentValue = attrs.item(i).getNodeValue();
                }
                if (attrName.equalsIgnoreCase("replace")) {
                    attrReplaceValue = attrs.item(i).getNodeValue();
                }
            }
            if (attrContentValue.equals("")) {
                System.err.println("Fatal configuration error: Empty content in block <replacestrings>.");
                System.exit(1);
            }
            added =
                m_tempReplaceStrings.add(
                    new CmsHtmlConverterObjectReplaceStrings(
                        attrContentValue,
                        "",
                        attrReplaceValue,
                        ""));
            if (!added) {
                System.err.println("Configuration error: Failed adding object to ArrayList m_tempReplaceStrings.");
            }
            break;
        default:
            System.err.println("Fatal configuration error: Tag <"+node.getNodeName()
                    +"> not recognized in block <replacestrings>.");
            System.exit(1);
        }
    }

    /**
     * Reads tag definitions (removetags, removeblocks, inlinetags) from XML configuration.<p>
     * 
     * @param node analysed node from blocks removetags, removeblocks, inlinetags
     * @return value of attribute "name" of analysed node
     */
    private String parseTag(Node node) {
        if (node.getNodeName().equals("#comment")) {
            return "";
        }
        NamedNodeMap attrs = node.getAttributes();
        if (attrs.getLength()<1) {
            System.err.println("Fatal configuration error: Tag <"+node.getNodeName()
                    +"> has no attributes.");
            System.exit(1);
        }
        String name=attrs.item(0).getNodeName();
        if (name.equalsIgnoreCase("name")) {
            return attrs.item(0).getNodeValue();
        }
        System.err.println("Fatal configuration error: Attribute \""+name
                +"\" not recognized in tag <"+node.getNodeName()+">.");
        System.exit(1);
        return "";
    }

    /**
     * Analyses attribute values and returns.<p>
     * 
     * @param node analysed node
     * @param number analysed attribute number of node
     * @return true, if attribute value is "true"; false, if value is "false"
     */
    private boolean checkBoolean(Node node, int number) {
        NamedNodeMap attrs = node.getAttributes();
        if (attrs.item(number).getNodeValue().equalsIgnoreCase("true")) {
            return true;
        } else if (attrs.item(number).getNodeValue().equalsIgnoreCase("false")) {
            return false;
        }
        System.err.println("Fatal configuration error: Attribute value \""
                +attrs.item(number).getNodeValue()+"\" not recognized in tag <"+node.getNodeName()+">.");
        System.exit(1);
        return false;
    }

    /**
     * Tests if a node name is allowed in XML configuration.<p>
     * 
     * @param name String which is tested
     * @return value of node name, -1 for commentnode, exit if name was not recognized
     */
    private int getConfigNodeName(String name) {
        if (name.equalsIgnoreCase("tag")) {
            return C_TAG_NODE;
        }
        if (name.equalsIgnoreCase("string")) {
            return C_STRING_NODE;
        }
        if (name.equalsIgnoreCase("converterconfig")) {
            return C_CONFIG_NODE;
        }
        if (name.equalsIgnoreCase("replacecontent")) {
            return C_REPLACECONTENT_NODE;
        }
        if (name.equalsIgnoreCase("defaults")) {
            return C_DEFAULT_NODE;
        }
        if (name.equalsIgnoreCase("inlinetags")) {
            return C_INLINETAG_NODE;
        }
        if (name.equalsIgnoreCase("removetags")) {
            return C_REMOVETAG_NODE;
        }
        if (name.equalsIgnoreCase("removeblocks")) {
            return C_REMOVEBLOCK_NODE;
        }
        if (name.equalsIgnoreCase("replacetags")) {
            return C_REPLACETAG_NODE;
        }
        if (name.equalsIgnoreCase("replaceblocks")) {
            return C_REPLACEBLOCK_NODE;
        }
        if (name.equalsIgnoreCase("replacestrings")) {
            return C_REPLACESTRING_NODE;
        }
        if (name.equalsIgnoreCase("xhtmloutput")) {
            return C_XHTMLOUTPUT_NODE;
        }
        if (name.equalsIgnoreCase("globalprefix")) {
            return C_GLOBALPREFIX_NODE;
        }
        if (name.equalsIgnoreCase("globalsuffix")) {
            return C_GLOBALSUFFIX_NODE;
        }
        if (name.equalsIgnoreCase("globaladdeveryline")) {
            return C_GLOBALADDEVERYLINE_NODE;
        }
        if (name.equalsIgnoreCase("usebrackets")) {
            return C_USEBRACKETS_NODE;
        }
        if (name.equalsIgnoreCase("encodequotationmarks")) {
            return C_ENCODEQUOTATIONMARKS_NODE;
        }
        if (name.equalsIgnoreCase("addeveryline")) {
            return C_ADDEVERYLINE_NODE;
        }
        if (name.equalsIgnoreCase("prefix")) {
            return C_PREFIX_NODE;
        }
        if (name.equalsIgnoreCase("suffix")) {
            return C_SUFFIX_NODE;
        }
        if (name.equalsIgnoreCase("#comment")) {
            return C_COMMENT_NODE;
        }
        /* if tagname is not recognized, return -1 */
        return -1;
    }

}