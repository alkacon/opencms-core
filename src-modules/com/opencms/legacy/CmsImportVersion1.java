/*
 * File   : $Source: /alkacon/cvs/opencms/src-modules/com/opencms/legacy/Attic/CmsImportVersion1.java,v $
 * Date   : $Date: 2005/06/27 23:27:46 $
 * Version: $Revision: 1.8 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Mananagement System
 *
 * Copyright (C) 2002  Alkacon Software (http://www.alkacon.com)
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

package com.opencms.legacy;

import org.opencms.importexport.CmsCompatibleCheck;
import org.opencms.importexport.CmsImportVersion2;
import org.opencms.main.CmsException;
import org.opencms.main.OpenCms;
import org.opencms.report.I_CmsReport;
import org.opencms.util.CmsStringUtil;
import org.opencms.xml.CmsXmlException;
import org.opencms.xml.CmsXmlUtils;

import com.opencms.template.*;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.List;

import org.dom4j.Attribute;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.xml.sax.InputSource;

/**
 * Implementation of the OpenCms Import Interface ({@link org.opencms.importexport.I_CmsImport}) for 
 * the import version 1.<p>
 * 
 * This import format was used in OpenCms 4.3.23 - 5.0.0.<p>
 * 
 * This import class has similar funktions to CmsImportVersion2, but because of the need for a
 * single import class for each import version, a new, inherited class must be used, returning 
 * the correct import version.<p>
 *
 * @author Michael Emmerich (m.emmerich@alkacon.com)
 * @author Thomas Weckert (t.weckert@alkacon.com)
 * 
 * @see org.opencms.importexport.A_CmsImport
 * 
 * @deprecated Will not be supported past the OpenCms 6 release.
 */
public class CmsImportVersion1 extends CmsImportVersion2 {

    /** The version number of this import implementation. */
    private static final int C_IMPORT_VERSION = 1;

    /** The path to the bodies in OpenCms 4.x. */
    private static final String C_VFS_PATH_OLD_BODIES = "/content/bodys/";

    /**
     * @see org.opencms.importexport.I_CmsImport#getVersion()
     */
    public int getVersion() {

        return CmsImportVersion1.C_IMPORT_VERSION;
    }

    /**
     * Converts the content of a file from OpenCms 4.x versions.<p>
     * 
     * @param filename the name of the file to convert
     * @param byteContent the content of the file
     * @return the converted filecontent
     */
    private byte[] convertFile(String filename, byte[] byteContent) {

        byte[] returnValue = byteContent;
        if (!filename.startsWith("/")) {
            filename = "/" + filename;
        }

        String fileContent = new String(byteContent);
        String encoding = getEncoding(fileContent);
        if (!"".equals(encoding)) {
            // encoding found, ensure that the String is correct
            try {
                // get content of the file and store it in String with the correct encoding
                fileContent = new String(byteContent, encoding);
            } catch (UnsupportedEncodingException e) {
                // encoding not supported, we use the default and hope we are lucky
                if (DEBUG > 0) {
                    System.err.println("["
                        + this.getClass().getName()
                        + ".convertFile()]: Encoding not supported, using default encoding.");
                }
            }
        } else {
            // encoding not found, set encoding of xml files to default
            if (DEBUG > 0) {
                System.err.println("["
                    + this.getClass().getName()
                    + ".convertFile()]: Encoding not set, using default encoding and setting it in <?xml...?>.");
            }
            encoding = OpenCms.getSystemInfo().getDefaultEncoding();
            fileContent = setEncoding(fileContent, encoding);
        }
        // check the frametemplates
        if (filename.indexOf("frametemplates") != -1) {
            fileContent = scanFrameTemplate(fileContent);
        }
        // scan content/bodys
        if (filename.indexOf(C_VFS_PATH_OLD_BODIES) != -1 || filename.indexOf(CmsCompatibleCheck.VFS_PATH_BODIES) != -1) {
            if (DEBUG > 0) {
                System.err.println("[" + this.getClass().getName() + ".convertFile()]: Starting scan of body page.");
            }
            fileContent = convertPageBody(fileContent, filename);
        }
        // translate OpenCms 4.x paths to the new directory structure 
        fileContent = setDirectories(fileContent, m_cms.getRequestContext().getDirectoryTranslator().getTranslations());

        // create output ByteArray
        try {
            returnValue = fileContent.getBytes(encoding);
        } catch (UnsupportedEncodingException e) {
            // encoding not supported, we use the default and hope we are lucky
            returnValue = fileContent.getBytes();
        }
        return returnValue;
    }

    /**
     * Searches for the webapps String and replaces it with a macro which is needed for the WYSIWYG editor,
     * also creates missing &lt;edittemplate&gt; tags for exports of older OpenCms 4.x versions.<p>
     * 
     * @param content the filecontent 
     * @param fileName the name of the file 
     * @return String the modified filecontent
     */

    private String convertPageBody(String content, String fileName) {

        String nodeName = null;

        // variables needed for the creation of <template> elements
        boolean createTemplateTags = false;
        Hashtable templateElements = new Hashtable();

        // first check if any contextpaths are in the content String
        boolean found = false;
        for (int i = 0; i < m_webAppNames.size(); i++) {
            if (content.indexOf((String)m_webAppNames.get(i)) != -1) {
                found = true;
            }
        }
        // check if edittemplates are in the content string
        if (content.indexOf("<edittemplate>") != -1 || content.indexOf("<EDITTEMPLATE>") != -1) {
            found = true;
        }

        // only build document when some paths were found or <edittemplate> is missing!
        if (found) {
            InputStream in = new ByteArrayInputStream(content.getBytes());
            String editString, templateString;
            try {
                // create DOM document
                InputSource source = new InputSource(in);
                Document doc = CmsXmlUtils.unmarshalHelper(source, null);

                // get all <edittemplate> nodes to check their content
                nodeName = "edittemplate";
                Element root = doc.getRootElement();
                List editNodes = root.elements(nodeName.toLowerCase());
                editNodes.addAll(root.elements(nodeName.toUpperCase()));
                // no <edittemplate> tags present, create them!
                if (editNodes.size() < 1) {
                    if (DEBUG > 0) {
                        System.err.println("["
                            + this.getClass().getName()
                            + ".convertPageBody()]: No <edittemplate> found, creating it.");
                    }

                    createTemplateTags = true;

                    nodeName = "TEMPLATE";
                    List templateNodes = root.elements(nodeName.toLowerCase());
                    List attributes;
                    templateNodes.addAll(root.elements(nodeName.toUpperCase()));

                    // create an <edittemplate> tag for each <template> tag
                    Element templateTag;
                    for (int i = 0; i < templateNodes.size(); i++) {

                        // get the CDATA content of the <template> tags
                        templateTag = (Element)templateNodes.get(i);
                        editString = templateTag.getText();
                        templateString = editString;

                        // substitute the links in the <template> tag String
                        try {
                            templateString = CmsXmlTemplateLinkConverter.convertFromImport(
                                templateString,
                                m_webappUrl,
                                fileName);
                        } catch (CmsException e) {
                            throw new CmsLegacyException("["
                                + this.getClass().getName()
                                + ".convertPageBody()] can't parse the content: ", e);
                        }

                        // look for the "name" attribute of the <template> tag
                        attributes = ((Element)templateNodes.get(i)).attributes();

                        String templateName = "";

                        if (attributes.size() > 0) {
                            templateName = ((Attribute)attributes.get(0)).getName();
                        }

                        // create the new <edittemplate> node
                        nodeName = "edittemplate";
                        Element newNode = DocumentHelper.createElement(nodeName.toLowerCase());
                        if (newNode == null) {
                            newNode = root.addElement(nodeName.toUpperCase());
                        }
                        newNode.addCDATA(editString);
                        // set the "name" attribute, if necessary
                        attributes = newNode.attributes();
                        if (!templateName.equals("")) {
                            newNode.addAttribute("name", templateName);
                        }

                        // append the new edittemplate node to the document
                        ((Element)root.elements("XMLTEMPLATE").get(0)).add(newNode);
                        // store modified <template> node Strings in Hashtable
                        if (templateName.equals("")) {
                            templateName = "noNameKey";
                        }
                        templateElements.put(templateName, templateString);
                    }
                    // finally, delete old <TEMPLATE> tags from document
                    while (templateNodes.size() > 0) {
                        ((Element)root.elements("XMLTEMPLATE").get(0)).remove((Element)templateNodes.get(0));
                    }

                }
                // check the content of the <edittemplate> nodes
                Element editTemplate;
                for (int i = 0; i < editNodes.size(); i++) {
                    // editString = editNodes.item(i).getFirstChild().getNodeValue();
                    editTemplate = (Element)editNodes.get(i);
                    editString = editTemplate.getText();
                    for (int k = 0; k < m_webAppNames.size(); k++) {
                        editString = CmsStringUtil.substitute(
                            editString,
                            (String)m_webAppNames.get(k),
                            CmsStringUtil.MACRO_OPENCMS_CONTEXT + "/");
                    }

                    // There is a setText(String) but no corresponding setCDATA in dom4j.
                    editTemplate.clearContent();
                    editTemplate.addCDATA(editString);

                }
                // convert XML document back to String

                content = CmsXmlUtils.marshal(doc, OpenCms.getSystemInfo().getDefaultEncoding());
                // rebuild the template tags in the document!
                if (createTemplateTags) {
                    content = content.substring(0, content.lastIndexOf("</XMLTEMPLATE>"));
                    // get the keys
                    Enumeration en = templateElements.keys();
                    while (en.hasMoreElements()) {
                        String key = (String)en.nextElement();
                        String value = (String)templateElements.get(key);
                        // create the default template
                        if (key.equals("noNameKey")) {
                            content += "\n<TEMPLATE><![CDATA[" + value;
                        } else {
                            // create template with "name" attribute
                            content += "\n<TEMPLATE name=\"" + key + "\"><![CDATA[" + value;
                        }
                        content += "]]></TEMPLATE>\n";
                    }
                    content += "\n</XMLTEMPLATE>";
                }

            } catch (Exception exc) {
                // ignore
            }
        }
        return content;
    }

    /** 
     * Scans the given content of a frametemplate and returns the result.<p>
     *
     * @param content the filecontent
     * @return modified content
     */
    private String scanFrameTemplate(String content) {

        // no Meta-Tag present, insert it!
        if (content.toLowerCase().indexOf("http-equiv=\"content-type\"") == -1) {
            content = CmsStringUtil
                .substitute(
                    content,
                    "</head>",
                    "<meta http-equiv=\"content-type\" content=\"text/html; charset=]]><method name=\"getEncoding\"/><![CDATA[\">\n</head>");
        } else {
            // Meta-Tag present
            if (content.toLowerCase().indexOf("charset=]]><method name=\"getencoding\"/>") == -1) {
                String fileStart = content.substring(0, content.toLowerCase().indexOf("charset=") + 8);
                String editContent = content.substring(content.toLowerCase().indexOf("charset="));
                editContent = editContent.substring(editContent.indexOf("\""));
                String newEncoding = "]]><method name=\"getEncoding\"/><![CDATA[";
                content = fileStart + newEncoding + editContent;
            }
        }
        return content;
    }

    /**
     * Performs all required pre-import steps.<p>
     * 
     * The content *IS* changed in the implementation of this class (edittemplate/displaytemplate,
     * links, paths including the webapps name etc.).<p>
     * 
     * @see org.opencms.importexport.CmsImportVersion2#convertContent(java.lang.String, java.lang.String, byte[], java.lang.String)
     */
    protected byte[] convertContent(String source, String destination, byte[] content, String resType) {

        // check and convert old import files    
        if (getVersion() < 2) {
            // convert content from pre 5.x must be activated
            if ("page".equals(resType) || ("plain".equals(resType)) || ("XMLTemplate".equals(resType))) {
                if (DEBUG > 0) {
                    System.err.println("#########################");
                    System.err.println("["
                        + this.getClass().getName()
                        + ".convertContent()]: starting conversion of \""
                        + resType
                        + "\" resource "
                        + source
                        + ".");
                }
                // change the filecontent for encoding if necessary
                content = convertFile(source, content);
            }
            // only check the file type if the version of the export is 0
            if (getVersion() == 0) {
                // ok, a (very) old system exported this, check if the file is ok
                if (!(new CmsCompatibleCheck()).isTemplateCompatible(m_importPath + destination, content, resType)) {
                    resType = CmsResourceTypeCompatiblePlain.getStaticTypeName();
                    m_report.print(Messages.get().container(Messages.RPT_MUST_SET_TO_1, resType), I_CmsReport.FORMAT_WARNING);
                }
            }
        }

        // drag the content also through the conversion method in the super class
        return super.convertContent(source, destination, content, resType);
    }

    /**
     * Creates a dom4j document out of a specified stream.<p>
     * 
     * @param stream the stream
     * @return a dom4j document
     * @throws CmsXmlException if something goes wrong
     */
    public static Document getXmlDocument(InputStream stream) throws CmsXmlException {

        return CmsXmlUtils.unmarshalHelper(new InputSource(stream), null);
    }

}
