/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/workplace/explorer/Attic/CmsNewCsvFileUpload.java,v $
 * Date   : $Date: 2005/04/15 09:08:31 $
 * Version: $Revision: 1.1 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Mananagement System
 *
 * Copyright (C) 2002 - 2005 Alkacon Software (http://www.alkacon.com)
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

package org.opencms.workplace.explorer;

import org.opencms.file.CmsProperty;
import org.opencms.file.CmsResource;
import org.opencms.file.CmsResourceFilter;
import org.opencms.jsp.CmsJspActionElement;
import org.opencms.main.CmsException;
import org.opencms.main.I_CmsConstants;
import org.opencms.main.OpenCms;
import org.opencms.util.CmsStringUtil;
import org.opencms.workplace.I_CmsWpConstants;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.PageContext;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.apache.commons.fileupload.FileItem;

import org.w3c.dom.Document;

/**
 * The new resource upload dialog handles the upload of cvs files. They are converted in a first step to xml
 * and in a second step transformed via a xsl stylesheet.<p>
 * 
 * The following files use this class:
 * <ul>
 * <li>/commons/newcvsfile_upload.jsp
 * </ul>
 * 
 * @author Jan Baudisch (j.baudisch@alkacon.com)
 * @version $Revision: 1.1 $
 * 
 * @since 6.0
 */
public class CmsNewCsvFileUpload extends CmsNewResourceUpload {

    /** The delimiter, the fields of the CVS are separated with. */
    public static String FIELD_DELIMITER = ";";

    /** The XSLT File to transform the table with. */
    private String m_paramXsltFile;

    /** The delimiter to separate the CVS values. */
    private String m_paramDelimiter;
    
    /** The delimiter to separate the text. */
    private static final char C_TEXT_DELIMITER = '"';

    /**
     * Public constructor with JSP action element.<p>
     * 
     * @param jsp an initialized JSP action element
     */
    public CmsNewCsvFileUpload(CmsJspActionElement jsp) {

        super(jsp);
    }

    /**
     * Public constructor with JSP variables.<p>
     * 
     * @param context the JSP page context
     * @param req the JSP request
     * @param res the JSP response
     */
    public CmsNewCsvFileUpload(PageContext context, HttpServletRequest req, HttpServletResponse res) {

        this(new CmsJspActionElement(context, req, res));
    }

    /**
     * Returns the xslt file to transform the xml with.<p>
     * 
     * @return the path to the xslt file to transform the xml with or null if it is not set
     */
    public String getParamXsltFile() {

        return m_paramXsltFile;
    }

    /**
     * Returns the delimiter to separate the CSV values.<p>
     * 
     * @return the delimiter to separate the CSV values
     */
    public String getParamDelimiter() {

        return m_paramDelimiter;
    }

    /**
     * Sets the path to the xslt file.<p>
     * 
     * @param xsltFile the file to transform the xml with.
     */
    public void setParamXsltFile(String xsltFile) {

        m_paramXsltFile = xsltFile;
    }

    /**
     * Sets the delimiter to separate the CSV values.<p>
     * 
     * @param delimiter the delimiter to separate the CSV values.
     */
    public void setParamDelimiter(String delimiter) {

        m_paramDelimiter = delimiter;
    }

    /**
     * Uploads the specified file and unzips it, if selected.<p>
     * 
     * @throws JspException if inclusion of error dialog fails
     */
    public void actionUpload() throws JspException {

        String errorMsgSuffix = "";

        try {
            // get the file item from the multipart request
            Iterator i = getMultiPartFileItems().iterator();
            FileItem fi = null;
            while (i.hasNext()) {
                fi = (FileItem)i.next();
                if (fi.getName() != null) {
                    // found the file object, leave iteration
                    break;
                } else {
                    // this is no file object, check next item
                    continue;
                }
            }

            if (fi != null) {
                String fileName = fi.getName();
                long size = fi.getSize();
                long maxFileSizeBytes = OpenCms.getWorkplaceManager().getFileBytesMaxUploadSize(getCms());
                // check file size
                if (maxFileSizeBytes > 0 && size > maxFileSizeBytes) {
                    // file size is larger than maximum allowed file size, throw an error
                    errorMsgSuffix = "size";
                    throw new CmsException("File size larger than maximum allowed upload size, currently set to "
                        + (maxFileSizeBytes / 1024)
                        + " kb");
                }
                byte[] content = fi.get();
                fi.delete();

                // single file upload
                String newResname = getCms().getRequestContext().getFileTranslator().translateResource(
                    CmsResource.getName(fileName.replace('\\', '/')));
                newResname = CmsStringUtil.changeFileNameSuffixTo(newResname, "html");
                setParamNewResourceName(newResname);
                setParamResource(newResname);
                setParamResource(computeFullResourceName());
                //int resTypeId = CmsResourceTypePlain.getStaticTypeId();
                int resTypeId = OpenCms.getResourceManager().getDefaultTypeForName(newResname).getTypeId();

                String xmlContent = "";
                CmsProperty styleProp = CmsProperty.getNullProperty();
                try {
                    xmlContent = convertCsvToXml(new InputStreamReader(new ByteArrayInputStream(content), "ISO-8859-1"));
                } catch (UnsupportedEncodingException e) {
                    // 
                }
                if (CmsStringUtil.isNotEmpty(getParamXsltFile())) {
                    
                    String m_xsltFilesPath = I_CmsWpConstants.C_VFS_PATH_MODULES + "de.bvi.internet.frontend/xslt/";
                    xmlContent = applyXslTransformation(m_xsltFilesPath + getParamXsltFile(), xmlContent);
                    styleProp = getCms().readPropertyObject(
                        m_xsltFilesPath + getParamXsltFile(),
                        I_CmsConstants.C_PROPERTY_STYLESHEET,
                        true);
                }
                content = xmlContent.getBytes();

                try {
                    // create the resource
                    getCms().createResource(getParamResource(), resTypeId, content, Collections.EMPTY_LIST);
                } catch (CmsException e) {
                    // resource was present, overwrite it
                    getCms().lockResource(getParamResource());
                    getCms().replaceResource(getParamResource(), resTypeId, content, null);
                }
                // copy xslt stylesheet-property to the new resource
                getCms().writePropertyObject(getParamResource(), styleProp);
            } else {
                throw new CmsException("Upload file not found");
            }
        } catch (CmsException e) {
            // error uploading file, show error dialog
            setAction(ACTION_SHOWERROR);
            getJsp().getRequest().setAttribute(C_SESSION_WORKPLACE_CLASS, this);
            setParamErrorstack(e.getStackTraceAsString());
            setParamMessage(key("error.message.upload"));
            setParamReasonSuggestion(key("error.reason.upload" + errorMsgSuffix)
                + "<br>\n"
                + key("error.suggestion.upload" + errorMsgSuffix)
                + "\n");
            getJsp().include(C_FILE_DIALOG_SCREEN_ERROR);
        }
    }

    /**
     * Converts CSV data to xml.<p>
     * 
     * @return a XML representation of the csv data
     * @param r the csv data to convert
     */
    public String convertCsvToXml(Reader r) {

        StringBuffer xml = new StringBuffer("<table>\n");
        String line;
        BufferedReader br = new BufferedReader(r);
        try {
            while ((line = br.readLine()) != null) {
                xml.append("<tr>");
                String[] words = CmsStringUtil.splitAsArray(line, getParamDelimiter());
                for (int i = 0; i < words.length; i++) {
                    xml.append("<td>").append(removeStringDelimiters(words[i])).append("</td>");
                }
                xml.append("</tr>\n");
            }
        } catch (IOException e) {
            if (OpenCms.getLog(this).isErrorEnabled()) {
                OpenCms.getLog(this).error(e);
            }
        }
        return xml.append("</table>").toString();
    }

    /**
     * applies a XSLT Transformation to the xmlContent.
     * 
     * @param xsltFile the XSLT transformation file
     * @param xmlContent the XML content to transform
     * @return the transformed xml
     */
    public String applyXslTransformation(String xsltFile, String xmlContent) {

        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        Document document;
        StringWriter result = new StringWriter();
        try {
            InputStream stylesheet = new ByteArrayInputStream(getCms().readFile(xsltFile).getContents());

            DocumentBuilder builder = factory.newDocumentBuilder();
            document = builder.parse(new ByteArrayInputStream(xmlContent.getBytes()));

            TransformerFactory tFactory = TransformerFactory.newInstance();
            StreamSource stylesource = new StreamSource(stylesheet);
            Transformer transformer = tFactory.newTransformer(stylesource);

            DOMSource source = new DOMSource(document);
            StreamResult streamResult = new StreamResult(result);
            transformer.transform(source, streamResult);
        } catch (Exception e) {
            if (OpenCms.getLog(this).isErrorEnabled()) {
                OpenCms.getLog(this).error(e);
            }
        }
        return result.toString();
    }

    /**
     * Builds a html select for the XSLT files.
     * 
     * @return html select code with the possible available xslt files
     */
    public String buildXsltSelect() {

        StringBuffer result = new StringBuffer("<select name=\"xsltfile\">\n<option value=\"\"></option>");
        Iterator i = getXsltFiles().iterator();
        String fileName;
        CmsResource resource;
        CmsProperty titleProp = null;
        while (i.hasNext()) {
            resource = (CmsResource)i.next();
            fileName = resource.getName();

            try {
                titleProp = getCms().readPropertyObject(resource.getRootPath(), I_CmsConstants.C_PROPERTY_TITLE, false);
            } catch (CmsException e) {
                if (OpenCms.getLog(this).isErrorEnabled()) {
                    OpenCms.getLog(this).error(e);
                }
            }
            result.append("<option value=\"").append(fileName).append("\">");
            if (titleProp.isNullProperty()) {
                result.append('[').append(fileName).append(']');
            } else {
                result.append(titleProp.getValue());
            }
            result.append("</option>\n");
        }
        return result.append("</select>").toString();
    }

    /**
     * Returns a list of the available xslt files.<p>
     * 
     * @return a list of the available xslt files
     */
    public List getXsltFiles() {

        // collect list of all
        List xsltFiles = new ArrayList();
        Iterator moduleNames = OpenCms.getModuleManager().getModuleNames().iterator();
        while (moduleNames.hasNext()) {
            String moduleName = (String)moduleNames.next();
            String xsltDirPath = I_CmsWpConstants.C_VFS_PATH_MODULES + moduleName + "/xslt/";
            if (getCms().existsResource(xsltDirPath)) {
                try {                
                    xsltFiles.addAll(getCms().readResources(xsltDirPath, CmsResourceFilter.DEFAULT_FILES, true));
                } catch (CmsException e) {
                    // error reading resources
                    if (OpenCms.getLog(this).isErrorEnabled()) {
                        OpenCms.getLog(this).error(e);
                    }
                } catch (NullPointerException e) {
                    // ignore this exception    
                }                    
            }        
        }       

        return xsltFiles;
    }

    /**                                                                                                                              
     * Removes the string delimiters from a key (as well as any white space                                                          
     * outside the delimiters).                                                                                                      
     *                                                                                                                               
     * @param key  the key (including delimiters).                                                                                   
     *                                                                                                                               
     * @return The key without delimiters.                                                                                           
     */
    private String removeStringDelimiters(String key) {

        String k = key.trim();
        if (k.charAt(0) == C_TEXT_DELIMITER) {
            k = k.substring(1);
        }
        if (k.charAt(k.length() - 1) == C_TEXT_DELIMITER) {
            k = k.substring(0, k.length() - 1);
        }
        return k;
    }
}