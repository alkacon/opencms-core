/*
 * File   : $Source: /alkacon/cvs/opencms/src/com/opencms/template/Attic/CmsImportVersion1.java,v $
 * Date   : $Date: 2004/02/11 14:23:48 $
 * Version: $Revision: 1.1 $
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

import org.opencms.importexport.CmsCompatibleCheck;
import org.opencms.importexport.CmsImportVersion2;
import org.opencms.main.OpenCms;
import org.opencms.report.I_CmsReport;
import org.opencms.util.CmsStringSubstitution;
import org.opencms.util.CmsXmlTemplateLinkConverter;

import com.opencms.core.CmsException;
import com.opencms.file.CmsResourceTypeCompatiblePlain;
import com.opencms.workplace.I_CmsWpConstants;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.util.Enumeration;
import java.util.Hashtable;

import org.w3c.dom.CDATASection;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.NodeList;

/**
 * Implementation of the OpenCms Import Interface ({@link org.opencms.importexport.I_CmsImport}) for 
 * the import version 1.</p>
 * 
 * This import format was used in OpenCms 4.3.23 - 5.0.0.</p>
 * 
 * This import class has similar funktions to CmsImportVersion2, but because of the need for a
 * single import class for each import version, a new, inherited class must be used, returning 
 * the correct import version.</p>
 *
 * @author Michael Emmerich (m.emmerich@alkacon.com)
 * @author Thomas Weckert (t.weckert@alkacon.com)
 * 
 * @see org.opencms.importexport.A_CmsImport
 */
public class CmsImportVersion1 extends CmsImportVersion2 {

    /** The path to the bodies in OpenCms 4.x */
    private static final String C_VFS_PATH_OLD_BODIES = "/content/bodys/";

    /**
     * Creates a new CmsImportVerion1 object.<p>
     */
    public CmsImportVersion1() {
        m_importVersion = 1;
    }

    /**
     * Returns the import version of the import implementation.<p>
     * 
     * @return import version
     */
    public int getVersion() {
        return 1;
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
                    System.err.println("[" + this.getClass().getName() + ".convertFile()]: Encoding not supported, using default encoding.");
                }
            }
        } else {
            // encoding not found, set encoding of xml files to default
            if (DEBUG > 0) {
                System.err.println("[" + this.getClass().getName() + ".convertFile()]: Encoding not set, using default encoding and setting it in <?xml...?>.");
            }
            encoding = OpenCms.getDefaultEncoding();
            fileContent = setEncoding(fileContent, encoding);
        }
        // check the frametemplates
        if (filename.indexOf("frametemplates") != -1) {
            fileContent = scanFrameTemplate(fileContent);
        }
        // scan content/bodys
        if (filename.indexOf(C_VFS_PATH_OLD_BODIES) != -1 || filename.indexOf(I_CmsWpConstants.C_VFS_PATH_BODIES) != -1) {
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
        // variables needed for the creation of <template> elements
        boolean createTemplateTags = false;
        Hashtable templateElements = new Hashtable();
        // first check if any contextpaths are in the content String
        boolean found = false;
        for (int i = 0; i < m_webAppNames.size(); i++) {
            if (content.indexOf((String) m_webAppNames.get(i)) != -1) {
                found = true;
            }
        }
        // check if edittemplates are in the content string
        if (content.indexOf("<edittemplate>") != -1) {
            found = true;
        }
        // only build document when some paths were found or <edittemplate> is missing!
        if (found) {
            InputStream in = new ByteArrayInputStream(content.getBytes());
            String editString, templateString;
            try {
                // create DOM document
                Document contentXml = A_CmsXmlContent.getXmlParser().parse(in);
                // get all <edittemplate> nodes to check their content
                NodeList editNodes = contentXml.getElementsByTagName("edittemplate");
                // no <edittemplate> tags present, create them!
                if (editNodes.getLength() < 1) {
                    if (DEBUG > 0) {
                        System.err.println("[" + this.getClass().getName() + ".convertPageBody()]: No <edittemplate> found, creating it.");
                    }
                    createTemplateTags = true;
                    NodeList templateNodes = contentXml.getElementsByTagName("TEMPLATE");
                    // create an <edittemplate> tag for each <template> tag
                    for (int i = 0; i < templateNodes.getLength(); i++) {
                        // get the CDATA content of the <template> tags
                        editString = templateNodes.item(i).getFirstChild().getNodeValue();
                        templateString = editString;
                        // substitute the links in the <template> tag String
                        try {
                            templateString = CmsXmlTemplateLinkConverter.convertFromImport(templateString, m_webappUrl, fileName);
                        } catch (CmsException e) {
                            throw new CmsException("[" + this.getClass().getName() + ".convertPageBody()] can't parse the content: ", e);
                        }
                        // look for the "name" attribute of the <template> tag
                        NamedNodeMap attrs = templateNodes.item(i).getAttributes();
                        String templateName = "";
                        if (attrs.getLength() > 0) {
                            templateName = attrs.item(0).getNodeValue();
                        }
                        // create the new <edittemplate> node                       
                        Element newNode = contentXml.createElement("edittemplate");
                        CDATASection newText = contentXml.createCDATASection(editString);
                        newNode.appendChild(newText);
                        // set the "name" attribute, if necessary
                        attrs = newNode.getAttributes();
                        if (!templateName.equals("")) {
                            newNode.setAttribute("name", templateName);
                        }
                        // append the new edittemplate node to the document
                        contentXml.getElementsByTagName("XMLTEMPLATE").item(0).appendChild(newNode);
                        // store modified <template> node Strings in Hashtable
                        if (templateName.equals("")) {
                            templateName = "noNameKey";
                        }
                        templateElements.put(templateName, templateString);
                    }
                    // finally, delete old <TEMPLATE> tags from document
                    while (templateNodes.getLength() > 0) {
                        contentXml.getElementsByTagName("XMLTEMPLATE").item(0).removeChild(templateNodes.item(0));
                    }
                }
                // check the content of the <edittemplate> nodes
                for (int i = 0; i < editNodes.getLength(); i++) {
                    editString = editNodes.item(i).getFirstChild().getNodeValue();
                    for (int k = 0; k < m_webAppNames.size(); k++) {
                        editString = CmsStringSubstitution.substitute(editString, (String) m_webAppNames.get(k), I_CmsWpConstants.C_MACRO_OPENCMS_CONTEXT + "/");
                    }
                    editNodes.item(i).getFirstChild().setNodeValue(editString);
                }
                // convert XML document back to String
                CmsXmlXercesParser parser = new CmsXmlXercesParser();
                Writer out = new StringWriter();
                parser.getXmlText(contentXml, out);
                content = out.toString();
                // rebuild the template tags in the document!
                if (createTemplateTags) {
                    content = content.substring(0, content.lastIndexOf("</XMLTEMPLATE>"));
                    // get the keys
                    Enumeration enum = templateElements.keys();
                    while (enum.hasMoreElements()) {
                        String key = (String) enum.nextElement();
                        String value = (String) templateElements.get(key);
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
            content = CmsStringSubstitution.substitute(content, "</head>", "<meta http-equiv=\"content-type\" content=\"text/html; charset=]]><method name=\"getEncoding\"/><![CDATA[\">\n</head>");
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
        if (m_importVersion < 2) {
            // convert content from pre 5.x must be activated
            if ("page".equals(resType) || ("plain".equals(resType)) || ("XMLTemplate".equals(resType))) {
                if (DEBUG > 0) {
                    System.err.println("#########################");
                    System.err.println("[" + this.getClass().getName() + ".convertContent()]: starting conversion of \"" + resType + "\" resource " + source + ".");
                }
                // change the filecontent for encoding if necessary
                content = convertFile(source, content);
            }
            // only check the file type if the version of the export is 0
            if (m_importVersion == 0) {
                // ok, a (very) old system exported this, check if the file is ok
                if (!(new CmsCompatibleCheck()).isTemplateCompatible(m_importPath + destination, content, resType)) {
                    resType = CmsResourceTypeCompatiblePlain.C_RESOURCE_TYPE_NAME;
                    m_report.print(m_report.key("report.must_set_to") + resType + " ", I_CmsReport.C_FORMAT_WARNING);
                }
            }
        } 
        
        // drag the content also through the conversion method in the super class
        return super.convertContent(source, destination, content, resType);
    }

}
