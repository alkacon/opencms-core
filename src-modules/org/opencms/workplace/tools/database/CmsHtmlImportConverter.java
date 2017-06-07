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
 * For further information about Alkacon Software GmbH & Co. KG, please see the
 * company website: http://www.alkacon.com
 *
 * For further information about OpenCms, please see the
 * project website: http://www.opencms.org
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package org.opencms.workplace.tools.database;

import org.opencms.file.CmsPropertyDefinition;
import org.opencms.i18n.CmsEncoder;
import org.opencms.main.CmsLog;
import org.opencms.util.CmsStringUtil;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.tidy.Tidy;

/**
 * This class implements Html-converting routines based on tidy to modify the
 * Html code of the imported Html pages.<p>
 *
 * @since 6.0.0
 */
public class CmsHtmlImportConverter {

    /** defintition of the alt attribute. */
    private static final String ATTRIB_ALT = "alt";

    /** defintition of the content attribute. */
    private static final String ATTRIB_CONTENT = "content";

    /** defintition of the href attribute.  */
    private static final String ATTRIB_HREF = "href";

    /** defintition of the name attribute. */
    private static final String ATTRIB_NAME = "name";

    /** defintition of the src attribute. */
    private static final String ATTRIB_SRC = "src";

    /** defintition of the &lt;BODY&gt;&lt;/BODY&gt; node. */
    private static final String NODE_BODY = "body";

    /** defintition of the &lt;HEAD&gt;&lt;/HEAD&gt; node. */
    private static final String NODE_HEAD = "head";

    /** defintition of the &lt;A&gt;&lt;/A&gt; node. */
    private static final String NODE_HREF = "a";

    /** defintition of the &lt;HTML&gt;&lt;/HTML&gt; node. */
    private static final String NODE_HTML = "html";

    /** defintition of the &lt;IMG&gt;&lt;/IMG&gt; node. */
    private static final String NODE_IMG = "img";

    /** defintition of the &lt;META&gt;&lt;/META&gt; node. */
    private static final String NODE_META = "meta";

    /** defintition of the &lt;TITLE&gt;&lt;/TITLE&gt;  node. */
    private static final String NODE_TITLE = "title";

    /**
     * HashMap stores tag names, after the end-tag, a "\n" is added to the output.<p>
     */
    private HashSet m_enterTags = new HashSet();

    /**
     * the absolute path in the real filesystem of the file to convert.
     */
    private String m_filename;

    /**
     * reference to the HtmlImport object, required to access the link translation.
     */
    private CmsHtmlImport m_htmlImport;

    /**
     * temporary buffer used in transformation method.
     */
    private StringBuffer m_tempString;

    /** instance of JTidy. */
    private Tidy m_tidy = new Tidy();

    /** flag to write the output. */
    private boolean m_write;

    /**
     * Default constructor, creates a new HtmlConverter.<p>
     *
     * @param htmlImport reference to the htmlimport
     * @param xmlMode switch for setting the import to HTML or XML mode
     */
    public CmsHtmlImportConverter(CmsHtmlImport htmlImport, boolean xmlMode) {

        m_tidy.setTidyMark(false);
        m_tidy.setShowWarnings(false);
        m_tidy.setQuiet(true);
        m_tidy.setForceOutput(true);

        if (xmlMode) {
            m_tidy.setXmlTags(xmlMode);
            m_tidy.setXmlSpace(true);
        }

        initialiseTags();
        m_htmlImport = htmlImport;
    }

    /**
     * Extracts the content of a HTML page.<p>
     *
     * This method should be pretty robust and work even if the input HTML does not contains
     * the specified matchers.<p>
     *
     * @param content the content to extract the body from
     * @param startpoint the point where matching starts
     * @param endpoint the point where matching ends
     * @return the extracted body tag content
     */
    public static String extractHtml(String content, String startpoint, String endpoint) {

        /** Regex that matches a start body tag. */
        Pattern startPattern = Pattern.compile(startpoint, Pattern.CASE_INSENSITIVE);

        /** Regex that matches an end body tag. */
        Pattern endPattern = Pattern.compile(endpoint, Pattern.CASE_INSENSITIVE);

        Matcher startMatcher = startPattern.matcher(content);
        Matcher endMatcher = endPattern.matcher(content);

        int start = 0;
        int end = content.length();

        if (startMatcher.find()) {
            start = startMatcher.end();
        }

        if (endMatcher.find(start)) {
            end = endMatcher.start();
        }

        return content.substring(start, end);
    }

    /**
     * Transforms HTML code into user defined output.<p>
     *
     * @param input Reader with HTML code
     * @param output Writer with transformed code
     * @param startPattern the start pattern definition for content extracting
     * @param endPattern the end pattern definition for content extracting
     * @param properties the file properties
     */
    public void convertHTML(Reader input, Writer output, String startPattern, String endPattern, Hashtable properties) {

        /* local variables */
        StringBuffer htmlString = new StringBuffer();
        Node node;
        String outString = "";

        try {
            /* write InputStream input in StringBuffer htmlString */
            int c;
            while ((c = input.read()) != -1) {
                htmlString.append((char)c);
            }
        } catch (IOException e) {
            if (CmsLog.INIT.isWarnEnabled()) {
                CmsLog.INIT.warn(
                    Messages.get().getBundle().key(
                        Messages.LOG_HTMLIMPORT_CONVERSION_ERROR_0,
                        e.getLocalizedMessage()));
            }
            return;
        }
        outString = htmlString.toString();
        // extract from html if even both patterns are defined
        if (CmsStringUtil.isNotEmpty(startPattern) && CmsStringUtil.isNotEmpty(endPattern)) {
            String extractMain = extractHtml(outString, startPattern, endPattern);
            if (extractMain.length() != outString.length()) {
                String extractHead = extractHtml(outString, "<html>", CmsStringUtil.BODY_START_REGEX);
                //String extractHead = extractHtml(extractMain, "<html>", CmsStringUtil.C_BODY_START_REGEX);
                StringBuffer buffer = new StringBuffer(extractHead.length() + extractMain.length() + 255);
                buffer.append("<html>");
                buffer.append(extractHead);
                buffer.append("<body>");
                buffer.append(extractMain);
                buffer.append("</body></html>");
                outString = buffer.toString();
            }
        }

        /* convert htmlString in InputStream for parseDOM */
        InputStream in;
        try {
            in = new ByteArrayInputStream(outString.getBytes(CmsEncoder.ENCODING_UTF_8));
        } catch (UnsupportedEncodingException e) {
            // this should never happen since UTF-8 is always supported
            in = new ByteArrayInputStream(outString.getBytes());
        }
        m_tidy.setInputEncoding(CmsEncoder.ENCODING_UTF_8);
        m_tidy.setOutputEncoding(CmsEncoder.ENCODING_UTF_8);

        // hold tidy error information into a new PrintWriter Object
        PrintWriter errorLog = new PrintWriter(new ByteArrayOutputStream(), true);
        m_tidy.setErrout(errorLog);

        node = m_tidy.parseDOM(in, null);
        /* check if html code has errors */
        if (m_tidy.getParseErrors() != 0) {
            if (CmsLog.INIT.isWarnEnabled()) {
                CmsLog.INIT.warn(Messages.get().getBundle().key(Messages.LOG_HTMLIMPORT_CONVERSION_ERROR_0));
            }
        }
        /* second step: create transformed output with printDocument from DOM */
        printDocument(node, properties);

        try {
            String content = m_tempString.toString();
            content = CmsStringUtil.substitute(content, "<br></br>", "<br>");
            content = CmsStringUtil.substitutePerl(content, "</a>(\\w+)", "</a> $1", "g");
            output.write(content);
            output.close();

        } catch (IOException e) {
            if (CmsLog.INIT.isWarnEnabled()) {
                CmsLog.INIT.warn(
                    Messages.get().getBundle().key(
                        Messages.LOG_HTMLIMPORT_CONVERSION_ERROR_1,
                        e.getLocalizedMessage()));
            }
            return;
        }
    }

    /**
     * Transforms HTML code into user defined output.<p>
     *
     * @param filename the absolute path in the real filesystem of the file to convert
     * @param inString String with HTML code
     * @param startPattern the start pattern definition for content extracting
     * @param endPattern the end pattern definition for content extracting
     * @param properties the file properties
     * @return String with transformed code
     */
    public String convertHTML(
        String filename,
        String inString,
        String startPattern,
        String endPattern,
        Hashtable properties) {

        m_tempString = new StringBuffer();
        m_write = true;
        m_filename = filename.replace('\\', '/');
        Reader in = new StringReader(inString);
        Writer out = new StringWriter();
        convertHTML(in, out, startPattern, endPattern, properties);
        return out.toString();
    }

    /**
     * Initialises Vector m_enterTags with tag names.<p>
     */
    private void initialiseTags() {

        StringTokenizer T = new StringTokenizer(
            "p,table,tr,td,body,head,script,pre,title,style,h1,h2,h3,h4,h5,h6,ul,ol,li",
            ",");
        while (T.hasMoreTokens()) {
            m_enterTags.add(T.nextToken());
        }
    }

    /**
     * Private method to parse DOM and create user defined output.<p>
     *
     * @param node Node of DOM from HTML code
     * @param properties the file properties
     */
    private void printDocument(Node node, Hashtable properties) {

        // if node is empty do nothing... (Recursion)
        if (node == null) {
            return;
        }
        // initialise local variables
        int type = node.getNodeType();
        String name = node.getNodeName();

        // detect node type
        switch (type) {
            case Node.DOCUMENT_NODE:

                printDocument(((Document)node).getDocumentElement(), properties);
                break;
            case Node.ELEMENT_NODE:

                // check if its the <head> node. Nothing inside the <head> node
                // must be
                // part of the output, but we must scan the content of this
                // node to get all
                // <meta> tags
                if (name.equals(NODE_HEAD)) {
                    m_write = false;
                }
                // scan element node; if a block has to be removed or replaced,
                // break and discard child nodes
                transformStartElement(node, properties);

                // test if node has children
                NodeList children = node.getChildNodes();
                if (children != null) {
                    int len = children.getLength();
                    for (int i = 0; i < len; i++) {
                        // recursively call printDocument with all child nodes
                        printDocument(children.item(i), properties);
                    }
                }
                break;
            case Node.TEXT_NODE:

                // replace subStrings in text nodes
                transformTextNode(node);
                break;
            default:

                break;
        }
        // end of recursion, add eventual endtags and suffixes
        switch (type) {
            case Node.ELEMENT_NODE:
                // analyse endtags and add them to output
                transformEndElement(node);
                if (node.getNodeName().equals(NODE_HEAD)) {
                    m_write = true;
                }
                break;
            case Node.DOCUMENT_NODE:
                break;
            default:
                break;
        }
    }

    /**
     * Transform element nodes and create end tags in output.<p>
     *
     * @param node actual element node
     */
    private void transformEndElement(Node node) {

        // check hat kind of node we have
        String nodeName = node.getNodeName();

        // the <HTML> and <BODY> node must be skipped
        if (nodeName.equals(NODE_HTML) || nodeName.equals(NODE_BODY)) {
            // do nothing here
        } else {
            // only do some output if we are in writing mode
            if (m_write) {
                m_tempString.append("</");
                m_tempString.append(nodeName);
                m_tempString.append(">");

                // append a "\n" to output String if possible
                if (m_enterTags.contains(node.getNodeName())) {
                    m_tempString.append("\n");
                }
            }
        }
    }

    /**
     * Transforms element nodes and create start tags in output. <p>
     *
     * @param node actual element node
     * @param properties the file properties
     */
    private void transformStartElement(Node node, Hashtable properties) {

        // check hat kind of node we have
        String nodeName = node.getNodeName();

        // the <HTML> and <BODY> node must be skipped
        if (nodeName.equals(NODE_HTML) || nodeName.equals(NODE_BODY)) {
            // the <TITLE> node must be read and its value set as properties to
            // the imported file

        } else if (nodeName.equals(NODE_TITLE)) {

            writeTitleProperty(node, properties);

        } else if (nodeName.equals(NODE_META)) {

            writeMetaTagProperty(node, properties);

        } else if (nodeName.equals(NODE_HREF)) {

            // only do some output if we are in writing mode
            if (m_write) {
                m_tempString.append("<");
                m_tempString.append(nodeName);
                NamedNodeMap attrs = node.getAttributes();
                // look through all attribs to find the reference
                for (int i = attrs.getLength() - 1; i >= 0; i--) {
                    String name = attrs.item(i).getNodeName();
                    String value = attrs.item(i).getNodeValue();

                    if (name.equals(ATTRIB_HREF)) {

                        // check if this is an external link
                        if (value.indexOf("://") > 0) {
                            // store it for later creation of an entry in the
                            // link gallery
                            String externalLinkFile = m_htmlImport.storeExternalLink(value);
                            if (externalLinkFile != null) {
                                value = m_htmlImport.getLinkGallery() + externalLinkFile;
                            }
                        } else if (!value.startsWith("mailto:") && !value.startsWith("javascript:")) {

                            // save an existing anchor link for later use
                            //                            if (value.indexOf("#") > 0) {
                            //                                String anchor = value.substring(value.indexOf("#"), value.length());
                            //                            }
                            // get the new link into the VFS
                            String internalUri = m_htmlImport.getAbsoluteUri(
                                value,
                                m_filename.substring(0, m_filename.lastIndexOf("/") + 1));

                            value = m_htmlImport.translateLink(internalUri);
                        }
                    }

                    m_tempString.append(" ");
                    m_tempString.append(name);
                    m_tempString.append("=\"");
                    m_tempString.append(value);
                    m_tempString.append("\"");
                }
                m_tempString.append(">");
            }

            // this is a imasge, its reference must be converted
        } else if (nodeName.equals(NODE_IMG)) {

            // only do some output if we are in writing mode
            if (m_write) {
                m_tempString.append("<");
                m_tempString.append(nodeName);
                NamedNodeMap attrs = node.getAttributes();
                // look through all attribs to find the src and alt attributes
                String imagename = "";
                String altText = "";
                for (int i = attrs.getLength() - 1; i >= 0; i--) {
                    String name = attrs.item(i).getNodeName();
                    String value = attrs.item(i).getNodeValue();
                    if (name.equals(ATTRIB_SRC)) {
                        // we found the src. now check if it refers to an
                        // external image.
                        // if not, we must get the correct location in the VFS
                        if (value.indexOf("://") <= 0) {
                            imagename = m_htmlImport.getAbsoluteUri(
                                value,
                                m_filename.substring(0, m_filename.lastIndexOf("/") + 1));
                            value = m_htmlImport.translateLink(imagename);
                        }
                    } else if (name.equals(ATTRIB_ALT)) {
                        altText = value;
                    }

                    m_tempString.append(" ");
                    m_tempString.append(name);
                    m_tempString.append("=\"");
                    m_tempString.append(value);
                    m_tempString.append("\"");
                }

                //store the alt tag of this image for later use
                m_htmlImport.storeImageInfo(imagename, altText);

                m_tempString.append(">");
            }
        } else {

            // only do some output if we are in writing mode
            if (m_write) {

                m_tempString.append("<");
                m_tempString.append(nodeName);
                NamedNodeMap attrs = node.getAttributes();
                for (int i = attrs.getLength() - 1; i >= 0; i--) {
                    m_tempString.append(" " + attrs.item(i).getNodeName() + "=" + "\"");
                    /* scan attribute values and replace subStrings */
                    m_tempString.append(attrs.item(i).getNodeValue() + "\"");
                }
                m_tempString.append(">");
            }
        }
    }

    /**
     * Private method to transform text nodes.<p>
     *
     * @param node actual text node
     */
    private void transformTextNode(Node node) {

        // only do some output if we are in writing mode
        if (m_write) {
            String helpString = node.getNodeValue();
            m_tempString.append(helpString);
        }
    }

    /**
     * Writes meta tags as cms properties by analyzing the meta tags nodes.<p>
     *
     * @param node the meta tag node in html document
     * @param properties the properties hashtable
     */
    private void writeMetaTagProperty(Node node, Hashtable properties) {

        NamedNodeMap attrs = node.getAttributes();
        String metaName = "";
        String metaContent = "";
        // look through all attribs to find the name and content attributes
        for (int i = attrs.getLength() - 1; i >= 0; i--) {
            String name = attrs.item(i).getNodeName();
            String value = attrs.item(i).getNodeValue();
            if (name.equals(ATTRIB_NAME)) {
                metaName = value;
            } else if (name.equals(ATTRIB_CONTENT)) {
                metaContent = value;
            }
        }
        // check if we have valid entries for this <META> node, store them
        // in the properties
        if ((metaName.length() > 0) && (metaContent.length() > 0)) {
            properties.put(metaName, CmsStringUtil.substitute(metaContent, "{subst}", "&#"));
        }
    }

    /**
     * Sets the Property title by analyzing the title node.<p>
     *
     * @param node the title node in html document
     * @param properties the properties hashtable
     */
    private void writeTitleProperty(Node node, Hashtable properties) {

        String title = "";
        // the title string is stored in the first child node
        NodeList children = node.getChildNodes();
        if (children != null) {
            Node titleNode = children.item(0);
            if (titleNode != null) {
                title = titleNode.getNodeValue();
            }
        }
        // add the title property if we have one
        if ((title != null) && (title.length() > 0)) {

            properties.put(CmsPropertyDefinition.PROPERTY_TITLE, CmsStringUtil.substitute(title, "{subst}", "&#"));
            // the title will be used as navtext if no other navtext is
            // given
            if (properties.get(CmsPropertyDefinition.PROPERTY_NAVTEXT) == null) {
                properties.put(
                    CmsPropertyDefinition.PROPERTY_NAVTEXT,
                    CmsStringUtil.substitute(title, "{subst}", "&#"));
            }
        }

    }

}
