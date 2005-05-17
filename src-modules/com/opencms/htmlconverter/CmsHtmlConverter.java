/*
* File   : $Source: /alkacon/cvs/opencms/src-modules/com/opencms/htmlconverter/Attic/CmsHtmlConverter.java,v $
* Date   : $Date: 2005/05/17 13:47:32 $
* Version: $Revision: 1.1 $
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

import java.io.*;
import java.util.*;
import java.net.*;
import org.w3c.tidy.Tidy;
import org.w3c.dom.*;

/**
 * Implementation of interface I_CmsHtmlConverterInterface:
 * Tool to check and transform HTML input code
 * to user-defined output, for example JSP, JavaScript or other syntax.
 * @author Andreas Zahner
 * @version 1.0
 * 
 * @deprecated Will not be supported past the OpenCms 6 release.
 */
public final class CmsHtmlConverter implements I_CmsHtmlConverterInterface {

    /** Filename for JTidy configuration. */
    private String m_tidyConfFile = "";
    /** Checks if JTidy is already defined. */
    private boolean m_tidyConfFileDefined;
    /** Filename for CmsHtmlConverter configuration. */
    private String m_converterConfFile = "";
    /** Checks if CmsHtmlConverter is configured from file. */
    private boolean m_converterConfFileDefined;
    /** Checks if CmsHtmlConverter is configured. */
    private boolean m_converterConfigDefined;
    /** stores number of predefined replaceTags. */
    private int m_numberReplaceTags;
    /** stores number of predefined replaceTags. */
    private int m_numberReplaceBlocks;
    /** temporary buffer used in transformation method. */
    private StringBuffer m_tempString;
    /** instance of JTidy. */
    private Tidy m_tidy = new Tidy();
    /** instance of CmsHtmlConverterTools. */
    private CmsHtmlConverterTools m_tools = new CmsHtmlConverterTools();
    /** configuration object for CmsHtmlConverter. */
    private CmsHtmlConverterConfig m_configuration = new CmsHtmlConverterConfig();
    /** object stores data for replacing tags. */
    private CmsHtmlConverterObjectReplaceTags m_tagObject = new CmsHtmlConverterObjectReplaceTags();
    /** object stores data for replacing blocks. */
    private CmsHtmlConverterObjectReplaceBlocks m_blockObject = new CmsHtmlConverterObjectReplaceBlocks();
    // replacestring for the modifyParameter methode. Used for the html editor replacement
    private String m_servletPrefix;
    // replace String for relative uris 
    private String m_relativeRoot;
    //the url object for links that should not be replaced
    private URL m_url;
    /** Vector stores tag names, after the end-tag, a "\n" is added to the output. */
    private Vector m_enterTags = new Vector();

    /**
     * Default constructor.<p>
     */
    public CmsHtmlConverter() {
        m_tidy.setTidyMark(false);
        m_tidy.setShowWarnings(false);
        m_tidy.setQuiet(true);
        initialiseTags();
    }

    /**
     * Constructor with name of Tidy configuration file as parameter.<p>
     * 
     * @param tidyConfFileName String with Tidy configuration file name
     */
    public CmsHtmlConverter(String tidyConfFileName) {
        this.setTidyConfFile(tidyConfFileName);
        initialiseTags();
    }

    /**
     * Constructor with name of Tidy and Converter configuration files as parameters.<p>
     * 
     * @param tidyConfFileName String with Tidy configuration file name
     * @param confFile String with Converter configuration file name
     */
    public CmsHtmlConverter(String tidyConfFileName, String confFile) {
        this.setTidyConfFile(tidyConfFileName);
        this.setConverterConfFile(confFile);
        initialiseTags();
    }

    /** 
     * Initialises Vector m_enterTags with tag names.<p> 
     */
    private void initialiseTags() {
        StringTokenizer T = new StringTokenizer("p,table,tr,td,body,head,script,pre,title,style,h1,h2,h3,h4,h5,h6,ul,ol,li", ",");
        while (T.hasMoreTokens()) {
            m_enterTags.addElement(new String(T.nextToken()));
       }
    }

    /**
     * Sets the prefix and the relative root.<p>
     *
     * @param prefix the servletprefix
     * @param relativeRoot the relative root
     */
    public void setServletPrefix(String prefix, String relativeRoot) {
        m_servletPrefix = prefix;
        m_relativeRoot = relativeRoot;
    }

    /**
     * Sets the url.<p>
     *
     * @param orgUrl object
     */
    public void setOriginalUrl(URL orgUrl) {
        m_url = orgUrl;
    }

    /**
     * Configures JTidy from file.<p>
     * 
     * @param fileName filename of JTidy configuration file
     */
    public void setTidyConfFile(String fileName) {
        m_tidyConfFile = fileName;
        m_tidyConfFileDefined = true;
        m_tidy.setConfigurationFromFile(m_tidyConfFile);
    }

    /**
     * If defined, returns JTidy configuration filename.<p>
     * 
     * @return filename of JTidy configuration file
     */
    public String getTidyConfFile() {
        if (m_tidyConfFileDefined) {
            return m_tidyConfFile;
        } else {
            return "";
        }
    }

    /**
     * Checks whether JTidy is already configured or not.<p>
     * 
     * @return true if JTidy configuration file is set, otherwise false
     */
    public boolean tidyConfigured() {
        return m_tidyConfFileDefined;
    }

    /**
     * Configures CmsHtmlConverter from file.<p>
     * 
     * @param confFile filename of configuration file
     */
    public void setConverterConfFile(String confFile) {
        try {
            InputStream in = new FileInputStream(confFile);
            m_configuration.init(in);
        } catch (IOException e) {
            System.err.println("Configuration error: Configuration file no found!");
            return;
        }
        m_converterConfFileDefined = true;
        m_converterConfigDefined = true;
        m_numberReplaceTags = m_configuration.getReplaceTags().size();
        m_numberReplaceBlocks = m_configuration.getReplaceBlocks().size();
    }

    /**
     * Configures CmsHtmlConverter from string.<p>
     * 
     * @param configuration string with CmsHtmlConverter configuration
     */
    public void setConverterConfString(String configuration) {
        m_configuration.init(configuration);
        m_converterConfFileDefined = false;
        m_converterConfigDefined = true;
        m_numberReplaceTags = m_configuration.getReplaceTags().size();
        m_numberReplaceBlocks = m_configuration.getReplaceBlocks().size();
    }

    /**
     * If defined, returns filename of CmsHtmlConverter configuration file.<p>
     * 
     * @return filename of configuration file
     */
    public String getConverterConfFile() {
        if (m_converterConfFileDefined) {
            return m_converterConfFile;
        } else {
            return "";
        }
    }

    /**
     * Checks whether CmsHtmlConverter is already configured or not.<p>
     * 
     * @return true if CmsHtmlConverter configuration is set, otherwise false
     */
    public boolean converterConfigured() {
        return m_converterConfigDefined;
    }

    /**
     * Checks if HTML code has errors.<p>
     * 
     * @param inString String with HTML code
     * @return true if errors were detected, otherwise false
     */
    public boolean hasErrors (String inString) {
        InputStream in = new ByteArrayInputStream(inString.getBytes());
        return this.hasErrors(in);
    }

    /**
     * Checks if HTML code has errors.<p>
     * 
     * @param input InputStream with HTML code
     * @return true if errors were detected, otherwise false
     */
    public boolean hasErrors (InputStream input) {
        /* initialise JTidy */
        m_tidy.setOnlyErrors(true);
        m_tidy.setShowWarnings(false);
        m_tidy.setQuiet(true);
        m_tidy.setErrout(null);
        /* parse InputStream */
        m_tidy.parse(input, null);
        /* check number of errors */
        return m_tidy.getParseErrors() != 0;
    }

    /**
     * Returns number of found errors in last parsed html code.<p>
     * 
     * @return int with number of errors
     */
    public int getNumberErrors() {
        return m_tidy.getParseErrors();
    }

    /**
     * Checks if HTML code has errors and lists errors.<p>
     * 
     * @param inString String with HTML code
     * @return String with detected errors
     */
    public String showErrors (String inString) {
        InputStream in = new ByteArrayInputStream(inString.getBytes());
        OutputStream out = new ByteArrayOutputStream();
        this.showErrors(in, out);
        return out.toString();
    }

    /**
     * Checks if HTML code has errors and lists errors.<p>
     * 
     * @param input InputStream with HTML code
     * @param output OutputStream with detected errors
     */
    public void showErrors (InputStream input, OutputStream output) {
        /* initialise JTidy */
        m_tidy.setOnlyErrors(true);
        m_tidy.setQuiet(true);
        m_tidy.setShowWarnings(false);
        InputStream in = new BufferedInputStream(input);
        PrintWriter errorLog = new PrintWriter(output);
        m_tidy.setErrout(errorLog);
        m_tidy.parse(in, null);
        if (m_tidy.getParseErrors() == 0) {
            errorLog.println("HTML code ok!\nNo errors detected.");
        }
        errorLog.close();
    }

    /**
     * Transforms HTML code into user defined output.<p>
     * 
     * @param inString String with HTML code
     * @return String with transformed code
     */
    public String convertHTML (String inString) {
        Reader in = new StringReader(inString);
        Writer out = new StringWriter();
        this.convertHTML(in, out);
        return out.toString();
    }

    /**
     * Transforms HTML code into user defined output.<p>
     * 
     * @param input Reader with HTML code
     * @param output Writer with transformed code
     */
    public void convertHTML (Reader input, Writer output) {
        /* local variables */
        StringBuffer htmlString = new StringBuffer();
        Node node;
        String outString = "";
        /* initialise JTidy */
        m_tidy.setShowWarnings(false);
        m_tidy.setQuiet(true);
        if (!m_tidyConfFileDefined) {
            m_tidy.setOnlyErrors(false);
            m_tidy.setTidyMark(false);
        }
        /* print errorlog in ByteArray */
        PrintWriter errorLog = new PrintWriter(new ByteArrayOutputStream(), true);
        m_tidy.setErrout(errorLog);
        try {
            /* write InputStream input in StringBuffer htmlString */
            int c;
            while ((c = input.read()) != -1) {
                htmlString.append((char)c);
            }
        } catch (IOException e) {
            System.err.println("Conversion error: " + e.toString());
            return;
        }
        outString = htmlString.toString();
        /* first step: replace subStrings in htmlString run #1*/
        outString = m_tools.scanContent(outString, m_configuration.getReplaceContent());
        /* convert htmlString in InputStream for parseDOM */
        InputStream in;
        try {
            in = new ByteArrayInputStream(outString.getBytes("UTF-8"));
            //m_tidy.setCharEncoding(org.w3c.tidy.Configuration.UTF8);
            m_tidy.setOutputEncoding("UTF-8");
            m_tidy.setInputEncoding("UTF-8");
        } catch (UnsupportedEncodingException e) {
            in = new ByteArrayInputStream(outString.getBytes());
            //m_tidy.setCharEncoding(org.w3c.tidy.Configuration.LATIN1);
            m_tidy.setOutputEncoding("LATIN1");
            m_tidy.setInputEncoding("LATIN1");
        }
        node = m_tidy.parseDOM(in, null);
        /* check if html code has errors */
        if (m_tidy.getParseErrors() != 0) {
            System.err.println("Conversion error: HTML code has errors!");
        }
        /* second step: create transformed output with printDocument from DOM */
        this.printDocument(node);
        /* third step: replace Strings run #2 */
        outString = m_tools.scanString(m_tempString.toString(), m_configuration.getReplaceStrings());
        outString = this.cleanOutput(outString);
        try {
            output.write(outString);
            output.close();
        } catch (IOException e) {
            System.err.println("Conversion error: " + e.toString());
            return;
        }
    }

    /**
     * Private method to parse DOM and create user defined output.<p>
     * 
     * @param node Node of DOM from HTML code
     */
    private void printDocument(Node node) {
        // if node is empty do nothing... (Recursion) 
        if (node == null) {
            return;
        }
        // initialise local variables 
        int type = node.getNodeType();
        int replaceTag = -1;
        int replaceBlock = -1;
        // detect node type 
        switch (type) {
            case Node.DOCUMENT_NODE:
                // initialise m_tempString and add global prefix 
                m_tempString = new StringBuffer(m_configuration.getGlobalPrefix());
                this.printDocument(((Document)node).getDocumentElement());
                break;
            case Node.ELEMENT_NODE:
                // analyse element node and transform it 
                replaceBlock = this.indexReplaceBlock(node);
                replaceTag = this.indexReplaceTag(node);
                // scan element node; if a block has to be removed or replaced,
                // break and discard child nodes 
                if (this.transformStartElement(node, replaceBlock, replaceTag)) {
                    break;
                }
                // test if node has children 
                NodeList children = node.getChildNodes();
                if (children != null) {
                    int len = children.getLength();
                    for (int i = 0; i < len; i++) {
                        // recursively call printDocument with all child nodes 
                        this.printDocument(children.item(i));
                    }
                }
                break;
            case Node.TEXT_NODE:
                // replace subStrings in text nodes 
                this.transformTextNode(node);
                break;
            default:
                // TODO: check what to do if node type is unknown
                break;
        }
        // end of recursion, add eventual endtags and suffixes 
        switch (type) {
            case Node.ELEMENT_NODE:
                // analyse endtags and add them to output 
                this.transformEndElement(node, replaceBlock, replaceTag);
                break;
            case Node.DOCUMENT_NODE:
                // add suffix to end of output 
                this.transformEndDocument();
                break;
            default:
                // TODO: check what to do if node type is unknown
                break;
        }
    }

    /**
     * Private method to transform element nodes and create start tags in output.<p>
     * 
     * @param node actual element node
     * @param replaceBlock index of object m_replaceBlocks
     * @param replaceTag index of object m_replaceTags
     * @return true if recursion has to be interrupted, otherwise false
     */
    private boolean transformStartElement (Node node, int replaceBlock, int replaceTag) {
        String tempReplaceString, valueParam;
        /* remove complete block, interrupt recursion in printDocument */
        if (m_tools.checkTag(node.getNodeName(), m_configuration.getRemoveBlocks())) {
            return true;
        }
        /* if tag has to be removed return, otherwise test other cases */
        if (!m_tools.checkTag(node.getNodeName(), m_configuration.getRemoveTags())) {
            /* test if a block has to be replaced */
            if (replaceBlock != -1) {
                m_blockObject = (CmsHtmlConverterObjectReplaceBlocks)
                        m_configuration.getReplaceBlocks().get(replaceBlock);
                /* if a parameter is used, get it from node attribute value,
                   insert it into replaceString */
                tempReplaceString = m_blockObject.getReplaceString();
                if (!m_blockObject.getParameter().equals("")) {
                    valueParam = m_tools.scanNodeAttrs(node, m_blockObject.getParameter());
                    tempReplaceString = m_tools.replaceString(tempReplaceString, "$parameter$", valueParam);
                }
                m_tempString.append(tempReplaceString);
                /* remove temporary object from ArrayList replaceBlocks */
                if (replaceBlock > (m_numberReplaceBlocks-1)) {
                    m_configuration.removeObjectReplaceBlock(replaceBlock);
                }
                /* ignore child elements in block, interrupt recursion in printDocument */
                return true;
            } else {
                /* test if actual element (start tag) has to be replaced */
                if (replaceTag != -1) {
                    m_tagObject = (CmsHtmlConverterObjectReplaceTags)
                            m_configuration.getReplaceTags().get(replaceTag);
                    tempReplaceString = m_tagObject.getReplaceStartTag();
                    /* if a parameter is used, get it from node attribute value,
                       insert it into replaceString */
                    if (!m_tagObject.getParameter().equals("")) {
                        valueParam = m_tools.scanNodeAttrs(node, m_tagObject.getParameter());
                        // HACK: only replace attribute value of parameter attribute!
                        if (m_tagObject.getReplaceParamAttr()) {
                            if (!m_tools.shouldReplaceUrl(m_url, valueParam, m_servletPrefix)) {
                                tempReplaceString = "$parameter$";
                            } else {
                                valueParam = m_tools.modifyParameter(m_url, valueParam, m_servletPrefix, m_relativeRoot);
                            }
                            tempReplaceString = m_tools.reconstructTag(tempReplaceString, node, m_tagObject.getParameter(), m_configuration.getQuotationmark());
                        }
                        tempReplaceString = m_tools.replaceString(tempReplaceString, "$parameter$", valueParam);
                    }
                    m_tempString.append(tempReplaceString);
                } else {
                    /* no replacement needed: append original element to output */
                    m_tempString.append("<");
                    m_tempString.append(node.getNodeName());
                    NamedNodeMap attrs = node.getAttributes();
                    for (int i = attrs.getLength()-1; i >= 0; i--) {
                        m_tempString.append(" " + attrs.item(i).getNodeName()
                                + "=" + m_configuration.getQuotationmark());
                        /* scan attribute values and replace subStrings */
                        String helpString = attrs.item(i).getNodeValue();
                        helpString = m_tools.scanString(helpString, m_configuration.getReplaceStrings());
                        m_tempString.append(helpString + m_configuration.getQuotationmark());
                    }
                    if (m_configuration.getXhtmlOutput()
                            && m_tools.checkTag(node.getNodeName(), m_configuration.getInlineTags())) {
                        m_tempString.append("/");
                    }
                    m_tempString.append(">");
                }
            } /* OPTION: Here I can add a "\n" after every starttag */
        }
        return false;
    }

    /**
     * Private method to transform element nodes and create end tags in output.<p>
     * 
     * @param node actual element node
     * @param replaceBlock index of object m_replaceBlocks
     * @param replaceTag index of object m_replaceTags
     */
    private void transformEndElement(Node node, int replaceBlock, int replaceTag) {
        /* test if block has to be removed */
        if (!m_tools.checkTag(node.getNodeName(), m_configuration.getRemoveBlocks())) {
            /* test if tag has to be removed */
            if (!m_tools.checkTag(node.getNodeName(), m_configuration.getRemoveTags())) {
                /* continue, if block is not replaced */
                if (replaceBlock == -1) {
                    /* replace end tag and discard inline tags */
                    if (replaceTag != -1) {
                        m_tagObject = (CmsHtmlConverterObjectReplaceTags)
                                m_configuration.getReplaceTags().get(replaceTag);
                        if (!m_tagObject.getInline()) {
                            String tempReplaceString = m_tagObject.getReplaceEndTag();
                            /* if parameter is used, get it from node attribute value,
                               insert it into replaceString */
                            if (!m_tagObject.getParameter().equals("")) {
                                String valueParam = m_tools.scanNodeAttrs(node, m_tagObject.getParameter());
                                tempReplaceString = m_tools.replaceString(tempReplaceString, "$parameter$", valueParam);
                            }
                            m_tempString.append(tempReplaceString);
                        }
                        /* remove temporary object from ArrayList replaceTags */
                        if (replaceTag > (m_numberReplaceTags - 1)) {
                            m_configuration.removeObjectReplaceTag(replaceTag);
                        }
                    } else {
                        /* catch inline tags and discard them */
                        if (!m_tools.checkTag(node.getNodeName(), m_configuration.getInlineTags())) {
                            m_tempString.append("</");
                            m_tempString.append(node.getNodeName());
                            m_tempString.append(">");
                            // append a "\n" to output String
                            if (m_configuration.getGlobalAddEveryLine()) {
                                // check if a "\n" can be added to output
                                boolean added = false;
                                for (int i=0; i<m_enterTags.size(); i++) {
                                    if (!added && node.getNodeName().equalsIgnoreCase((String)m_enterTags.elementAt(i))) {
                                        m_tempString.append(m_configuration.getGlobalSuffix()
                                                + "\n" + m_configuration.getGlobalPrefix());
                                        added = true;
                                    }
                                    // if tag was found, return
                                    if (added) {
                                        return;
                                    }
                                }
                            } else {
                                // check if a "\n" can be added to output
                                boolean added = false;
                                for (int i=0; i<m_enterTags.size(); i++) {
                                    if (!added && node.getNodeName().equalsIgnoreCase((String)m_enterTags.elementAt(i))) {
                                        m_tempString.append("\n");
                                        added = true;
                                    }
                                    // if tag was found, return
                                    if (added) {
                                        return;
                                    }
                                }
                            }
                        }
                    }
                }
            } // end of removetag
        } // end of removeblock
    }

    /**
     * Private method to transform output at end of document.<p>
     */
    private void transformEndDocument () {
        m_tempString.append(m_configuration.getGlobalSuffix());
    }

    /**
     * Private method to transform text nodes.<p>
     * 
     * @param node actual text node
     */
    private void transformTextNode(Node node) {
        String helpString = node.getNodeValue();
        /* do not scan text nodes between <script> tags! */
        if (!node.getParentNode().getNodeName().equalsIgnoreCase("script")
                && !node.getParentNode().getNodeName().equalsIgnoreCase("style")) {
            helpString = m_tools.scanChar(helpString, m_configuration.getReplaceExtendedChars());
        }
        /* replace quotationsmarks if configured */
        if (m_configuration.getEncodeQuotationmarks()) {
            helpString = m_tools.replaceString(helpString, "\"", m_configuration.getQuotationmark());
        }
        /* test if prefix and suffix have to be added every new line */
        if (m_configuration.getGlobalAddEveryLine()) {
            helpString = m_tools.replaceString(helpString, "\n", (m_configuration.getGlobalSuffix() + "\n" + m_configuration.getGlobalPrefix()));
        }
        m_tempString.append(helpString);
    }

    /**
     * Private method to delete empty "prefix+suffix" lines in output String.<p>
     * 
     * @param cleanString string for cleaning up
     * @return the cleaned string
     */
    private String cleanOutput (String cleanString) {
        if (m_configuration.getGlobalAddEveryLine()) {
            cleanString += "\n";
            String cutString = m_configuration.getGlobalPrefix()
                    + m_configuration.getGlobalSuffix() + "\n";
            /* delete empty "prefix-suffix" lines if suffix and prefix are not empty */
            if (!m_configuration.getGlobalPrefix().equals("")
                    && !m_configuration.getGlobalSuffix().equals("")) {
                cleanString = m_tools.replaceString(cleanString, cutString, "");
            }
        }
        return cleanString;
    }

    /**
     * Tests if specified tag has to be replaced and, if it is found,
     * delivers index of hit in ArrayLis.<p>
     * 
     * @param node DOM Node which might be replaced
     * @return "-1" if tag is not found, otherwise index of list with hit
     */
    private int indexReplaceTag(Node node) {
        ArrayList replaceTags = m_configuration.getReplaceTags();
        NamedNodeMap attrs = node.getAttributes();
        CmsHtmlConverterObjectReplaceTags testObject = new CmsHtmlConverterObjectReplaceTags();
        for (int index = 0; index < replaceTags.size(); index++) {
            testObject = (CmsHtmlConverterObjectReplaceTags)(replaceTags.get(index));
            // cw 09.09.2003 added general qualifier *
            if (node.getNodeName().equals(testObject.getTagName()) || "*".equals(testObject.getTagName())) {
                /* if no identifier attribute is defined, replace all nodes */
                if (testObject.getTagAttrib().equals("")) {
                    /* test if replaceStrings have to be retrieved from attributes */
                    if (testObject.getReplaceFromAttrs()) {
                        return scanTagElementAttrs(node, testObject);
                    }
                    return index;
                }
                for (int i = attrs.getLength()-1; i >= 0; i--) {
                    if (attrs.item(i).getNodeName().equals(testObject.getTagAttrib())
                            && (attrs.item(i).getNodeValue().equals(testObject.getTagAttribValue())
                            || testObject.getTagAttribValue().equals(""))) {
                        /* test if replaceStrings have to be retrieved from attributes */
                        if (testObject.getReplaceFromAttrs()) {
                            return scanTagElementAttrs(node, testObject);
                        }
                        return index;
                    }
                }
            }
        }
        return -1;
    }

    /**
     * Scans node attributes and creates new CmsHtmlConverterObjectReplaceTags.<p>
     * 
     * @param node DOM node which is scanned
     * @param testObject parent replaceTag object
     * @return index of new object in ArrayList replaceTags
     */
    private int scanTagElementAttrs(Node node, CmsHtmlConverterObjectReplaceTags testObject) {
        NamedNodeMap attrs = node.getAttributes();
        String prefix = testObject.getPrefix();
        String suffix = testObject.getSuffix();
        String name = testObject.getTagName();
        String attrib = testObject.getTagAttrib();
        String attrValue = testObject.getTagAttribValue();
        String startAttribute = testObject.getStartAttribute();
        String endAttribute = testObject.getEndAttribute();
        String replaceStartTag = "";
        String replaceEndTag = "";
        String parameter = testObject.getParameter();
        String attrName = "";
        boolean replaceParamAttr = testObject.getReplaceParamAttr();
        /* scan attributes for replaceStrings */
        for (int i = 0; i < attrs.getLength(); i++) {
            attrName = attrs.item(i).getNodeName();
            if (attrName.equalsIgnoreCase(startAttribute)) {
                replaceStartTag = attrs.item(i).getNodeValue();
            }
            if (attrName.equalsIgnoreCase(endAttribute)) {
                replaceEndTag = attrs.item(i).getNodeValue();
            }
        }
        /* replace encoded brackets if defined */
        if (m_configuration.getUseBrackets()) {
            replaceStartTag = m_configuration.scanBrackets(replaceStartTag);
            replaceEndTag = m_configuration.scanBrackets(replaceEndTag);
        }
        /* add temporary object to ArrayList replaceTags */
        m_configuration.addObjectReplaceTag(prefix, name, attrib, attrValue, replaceStartTag, replaceEndTag, suffix, false, "", "", parameter, replaceParamAttr);
        return m_configuration.getReplaceTags().size()-1;
    }

    /**
     * Tests if specified block has to be replaced and, if it is found,
     * delivers index of hit in ArrayList.<p>
     * 
     * @param node DOM Node which might be replaced
     * @return "-1" if tag is not found, otherwise index of list with hit
     */
    private int indexReplaceBlock(Node node) {
        ArrayList replaceBlocks = m_configuration.getReplaceBlocks();
        NamedNodeMap attrs = node.getAttributes();
        CmsHtmlConverterObjectReplaceBlocks testObject = new CmsHtmlConverterObjectReplaceBlocks();
        for (int index = 0; index < replaceBlocks.size(); index++) {
            testObject=(CmsHtmlConverterObjectReplaceBlocks)(replaceBlocks.get(index));
            // cw 09.09.2003 added general qualifier *
            if (node.getNodeName().equals(testObject.getTagName()) || "*".equals(testObject.getTagName())) {
                if (testObject.getTagAttrib().equals("")) {
                    /* test if replaceStrings has to be retrieved from attributes */
                    if (testObject.getReplaceFromAttrs()) {
                        return scanBlockElementAttrs(node, testObject);
                    }
                    return index;
                }
                for (int i = (attrs.getLength() - 1); i >= 0; i--) {
                    if (attrs.item(i).getNodeName().equals(testObject.getTagAttrib())
                            && (attrs.item(i).getNodeValue().equals(testObject.getTagAttribValue())
                            || testObject.getTagAttribValue().equals(""))) {
                        /* test if replaceString has to be retrieved from attributes */
                        if (testObject.getReplaceFromAttrs()) {
                            return scanBlockElementAttrs(node, testObject);
                        }
                        return index;
                    }
                }
            }
        }
        return -1;
    }

    /**
     * Scans node attributes and creates new CmsHtmlConverterObjectReplaceBlocks.<p>
     * 
     * @param node DOM node which is scanned
     * @param testObject parent replaceBlock object
     * @return index of new object in ArrayList replaceBlocks
     */
    private int scanBlockElementAttrs(Node node, CmsHtmlConverterObjectReplaceBlocks testObject) {
        NamedNodeMap attrs = node.getAttributes();
        String prefix = testObject.getPrefix();
        String suffix = testObject.getSuffix();
        String name = testObject.getTagName();
        String attrib = testObject.getTagAttrib();
        String attrValue = testObject.getTagAttribValue();
        String replaceString = "";
        String replaceAttribute = testObject.getReplaceAttribute();
        String attrName = "";
        String parameter = testObject.getParameter();
        /* scan attributes for replaceString */
        for (int i=0; i<attrs.getLength(); i++) {
            attrName = attrs.item(i).getNodeName();
            if (attrName.equalsIgnoreCase(replaceAttribute)) {
                replaceString = attrs.item(i).getNodeValue();
            }
        }
        /* replace encoded brackets if defined */
        if (m_configuration.getUseBrackets()) {
            replaceString = m_configuration.scanBrackets(replaceString);
        }
        /* add temporary object to ArrayList replaceBlocks */
        m_configuration.addObjectReplaceBlock(prefix, name, attrib,
                attrValue, replaceString, suffix, false, "", parameter);
        return m_configuration.getReplaceBlocks().size()-1;
    }
}