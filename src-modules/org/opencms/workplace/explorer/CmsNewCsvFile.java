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

package org.opencms.workplace.explorer;

import org.opencms.file.CmsProperty;
import org.opencms.file.CmsPropertyDefinition;
import org.opencms.file.CmsResource;
import org.opencms.file.CmsResourceFilter;
import org.opencms.file.types.CmsResourceTypePlain;
import org.opencms.i18n.CmsEncoder;
import org.opencms.jsp.CmsJspActionElement;
import org.opencms.main.CmsException;
import org.opencms.main.CmsLog;
import org.opencms.main.OpenCms;
import org.opencms.util.CmsStringUtil;
import org.opencms.util.CmsXsltUtil;
import org.opencms.workplace.CmsWorkplace;
import org.opencms.workplace.CmsWorkplaceException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.PageContext;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.logging.Log;

/**
 * The new resource upload dialog handles the upload of CSV (Comma Separated Values) files.<p>
 *
 * CSV files are converted in a first step to xml
 * and in a second step transformed using a xsl stylesheet.<p>
 *
 * The following files use this class:
 * <ul>
 * <li>/commons/newcsvfile_upload.jsp
 * </ul>
 * <p>
 *
 * @since 6.0.0
 */
public class CmsNewCsvFile extends CmsNewResourceUpload {

    /** Constant for automatically selecting the best fitting delimiter. */
    public static final String BEST_DELIMITER = "best";

    /** Constant for the height of the dialog frame. */
    public static final String FRAMEHEIGHT = "450";

    /** Request parameter name for the CSV content. */
    public static final String PARAM_CSVCONTENT = "csvcontent";

    /** Request parameter name for the delimiter. */
    public static final String PARAM_DELIMITER = "delimiter";

    /** Request parameter name for the XSLT file. */
    public static final String PARAM_XSLTFILE = "xsltfile";

    /** Constant for the xslt file suffix for table transformations. */
    public static final String TABLE_XSLT_SUFFIX = ".table.xslt";

    /** Constant for the tab-value inside delimiter the select. */
    public static final String TABULATOR = "tab";

    /** The log object for this class. */
    private static final Log LOG = CmsLog.getLog(CmsNewCsvFile.class);

    /** The pasted CSV content. */
    private String m_paramCsvContent;

    /** The delimiter to separate the CSV values. */
    private String m_paramDelimiter;

    /** The XSLT File to transform the table with. */
    private String m_paramXsltFile;

    /**
     * Public constructor with JSP action element.<p>
     *
     * @param jsp an initialized JSP action element
     */
    public CmsNewCsvFile(CmsJspActionElement jsp) {

        super(jsp);
    }

    /**
     * Public constructor with JSP variables.<p>
     *
     * @param context the JSP page context
     * @param req the JSP request
     * @param res the JSP response
     */
    public CmsNewCsvFile(PageContext context, HttpServletRequest req, HttpServletResponse res) {

        this(new CmsJspActionElement(context, req, res));
    }

    /**
     * Embeds the given content as cdata if neccessary.<p>
     * Contents starting with "<" and ending with ">" are NOT embedded in order to allow content with tags.
     *
     * @param content the content
     * @return the embedded content
     */
    static String toXmlBody(String content) {

        StringBuffer xmlBody = new StringBuffer(1024);
        content = content.trim();

        if (content.startsWith("<") && content.endsWith(">")) {
            return content;
        } else {
            xmlBody.append("<![CDATA[");
            xmlBody.append(content);
            xmlBody.append("]]>");
        }

        return xmlBody.toString();
    }

    /**
     * Uploads the specified file and transforms it to HTML.<p>
     *
     * @throws JspException if inclusion of error dialog fails
     */
    @Override
    public void actionUpload() throws JspException {

        String newResname = "";

        try {
            if (CmsStringUtil.isNotEmpty(getParamCsvContent())) {
                // csv content is pasted in the textarea
                newResname = "csvcontent.html";
                setParamNewResourceName("");
            } else {
                setParamCsvContent(new String(getFileContentFromUpload(), CmsEncoder.ENCODING_ISO_8859_1));
                newResname = getCms().getRequestContext().getFileTranslator().translateResource(
                    CmsResource.getName(getParamResource().replace('\\', '/')));
                newResname = CmsStringUtil.changeFileNameSuffixTo(newResname, "html");
                setParamNewResourceName(newResname);
            }

            setParamResource(newResname);
            setParamResource(computeFullResourceName());
            int resTypeId = OpenCms.getResourceManager().getDefaultTypeForName(newResname).getTypeId();

            // transform csv to html
            String xmlContent = CmsXsltUtil.transformCsvContent(
                getCms(),
                getParamXsltFile(),
                getParamCsvContent(),
                (BEST_DELIMITER.equals(getParamDelimiter()) ? null : getParamDelimiter()));
            byte[] content = xmlContent.getBytes();

            // if xslt file parameter is set, transform the raw html and set the css stylesheet property
            // of the converted file to that of the stylesheet
            CmsProperty styleProp = CmsProperty.getNullProperty();
            if (CmsStringUtil.isNotEmpty(getParamXsltFile())) {
                styleProp = getCms().readPropertyObject(
                    getParamXsltFile(),
                    CmsPropertyDefinition.PROPERTY_STYLESHEET,
                    true);
            }

            try {
                // create the resource
                getCms().createResource(getParamResource(), resTypeId, content, Collections.<CmsProperty> emptyList());
            } catch (CmsException e) {
                // resource was present, overwrite it
                getCms().lockResource(getParamResource());
                getCms().replaceResource(getParamResource(), resTypeId, content, null);
            }
            // copy xslt stylesheet-property to the new resource
            if (!styleProp.isNullProperty()) {
                getCms().writePropertyObject(getParamResource(), styleProp);
            }
        } catch (Throwable e) {
            // error uploading file, show error dialog
            setParamMessage(Messages.get().getBundle(getLocale()).key(Messages.ERR_TABLE_IMPORT_FAILED_0));
            includeErrorpage(this, e);
        }
    }

    /**
     * Builds a html select for Delimiters.
     *
     * @return html select code with the possible available xslt files
     */
    public String buildDelimiterSelect() {

        String[] optionStrings = new String[] {
            key(Messages.GUI_NEWRESOURCE_CONVERSION_DELIM_BEST_0),
            key(Messages.GUI_NEWRESOURCE_CONVERSION_DELIM_SEMICOLON_0),
            key(Messages.GUI_NEWRESOURCE_CONVERSION_DELIM_COMMA_0),
            key(Messages.GUI_NEWRESOURCE_CONVERSION_DELIM_TAB_0)};
        List<String> options = new ArrayList<String>(Arrays.<String> asList(optionStrings));
        List<String> values = new ArrayList<String>(Arrays.<String> asList(new String[] {"best", ";", ",", "tab"}));
        String parameters = "name=\"" + PARAM_DELIMITER + "\" class=\"maxwidth\"";
        return buildSelect(parameters, options, values, 0);
    }

    /**
     * Builds a html select for the XSLT files.
     *
     * @return html select code with the possible available xslt files
     */
    public String buildXsltSelect() {

        // read all xslt files
        List<CmsResource> xsltFiles = getXsltFiles();
        if (xsltFiles.size() > 0) {
            List<String> options = new ArrayList<String>();
            List<String> values = new ArrayList<String>();

            options.add(key(Messages.GUI_NEWRESOURCE_CONVERSION_NOSTYLE_0));
            values.add("");

            CmsResource resource;
            CmsProperty titleProp = CmsProperty.getNullProperty();

            Iterator<CmsResource> i = xsltFiles.iterator();
            while (i.hasNext()) {

                resource = i.next();
                try {
                    titleProp = getCms().readPropertyObject(
                        resource.getRootPath(),
                        CmsPropertyDefinition.PROPERTY_TITLE,
                        false);
                } catch (CmsException e) {
                    if (LOG.isWarnEnabled()) {
                        LOG.warn(e);
                    }
                }
                values.add(resource.getRootPath());
                // display the title if set or otherwise the filename
                if (titleProp.isNullProperty()) {
                    options.add("[" + resource.getName() + "]");
                } else {
                    options.add(titleProp.getValue());
                }
            }

            StringBuffer result = new StringBuffer(512);
            // build a select box and a table row around
            result.append("<tr><td style=\"white-space: nowrap;\" unselectable=\"on\">");
            result.append(key(Messages.GUI_NEWRESOURCE_CONVERSION_XSLTFILE_0));
            result.append("</td><td class=\"maxwidth\">");
            String parameters = "class=\"maxwidth\" name=\"" + PARAM_XSLTFILE + "\"";
            result.append(buildSelect(parameters, options, values, 0));
            result.append("</td><tr>");
            return result.toString();
        } else {
            return "";
        }
    }

    /**
     * Returns the content of the file upload and sets the resource name.<p>
     *
     * @return the byte content of the uploaded file
     * @throws CmsWorkplaceException if the filesize if greater that maxFileSizeBytes or if the upload file cannot be found
     */
    public byte[] getFileContentFromUpload() throws CmsWorkplaceException {

        byte[] content;
        // get the file item from the multipart request
        Iterator<FileItem> i = getMultiPartFileItems().iterator();
        FileItem fi = null;
        while (i.hasNext()) {
            fi = i.next();
            if (fi.getName() != null) {
                // found the file object, leave iteration
                break;
            } else {
                // this is no file object, check next item
                continue;
            }
        }

        if (fi != null) {
            long size = fi.getSize();
            if (size == 0) {
                throw new CmsWorkplaceException(Messages.get().container(Messages.ERR_UPLOAD_FILE_NOT_FOUND_0));
            }
            long maxFileSizeBytes = OpenCms.getWorkplaceManager().getFileBytesMaxUploadSize(getCms());
            // check file size
            if ((maxFileSizeBytes > 0) && (size > maxFileSizeBytes)) {
                throw new CmsWorkplaceException(
                    Messages.get().container(
                        Messages.ERR_UPLOAD_FILE_SIZE_TOO_HIGH_1,
                        new Long(maxFileSizeBytes / 1024)));
            }
            content = fi.get();
            fi.delete();
            setParamResource(fi.getName());

        } else {
            throw new CmsWorkplaceException(Messages.get().container(Messages.ERR_UPLOAD_FILE_NOT_FOUND_0));
        }
        return content;
    }

    /**
     * Returns the height of the head frameset.<p>
     *
     * @return the height of the head frameset
     */
    public String getHeadFrameSetHeight() {

        return FRAMEHEIGHT;
    }

    /**
     * Returns the pasted csv content.<p>
     *
     * @return the csv content
     */
    public String getParamCsvContent() {

        return m_paramCsvContent;
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
     * Returns the xslt file to transform the xml with.<p>
     *
     * @return the path to the xslt file to transform the xml with or null if it is not set
     */
    public String getParamXsltFile() {

        return m_paramXsltFile;
    }

    /**
     * Returns a list of CmsResources with the xslt files in the modules folder.<p>
     *
     * @return a list of the available xslt files
     */
    public List<CmsResource> getXsltFiles() {

        List<CmsResource> result = new ArrayList<CmsResource>();
        try {
            // find all files of generic xmlcontent in the modules folder
            int plainId = OpenCms.getResourceManager().getResourceType(
                CmsResourceTypePlain.getStaticTypeName()).getTypeId();
            Iterator<CmsResource> xmlFiles = getCms().readResources(
                CmsWorkplace.VFS_PATH_MODULES,
                CmsResourceFilter.DEFAULT_FILES.addRequireType(plainId),
                true).iterator();
            while (xmlFiles.hasNext()) {
                CmsResource xmlFile = xmlFiles.next();
                // filter all files with the suffix .table.xml
                if (xmlFile.getName().endsWith(TABLE_XSLT_SUFFIX)) {
                    result.add(xmlFile);
                }
            }
        } catch (CmsException e) {
            LOG.error(e.getLocalizedMessage(), e);
        }
        return result;

    }

    /**
     * Sets the pasted csv content.<p>
     *
     * @param csvContent the csv content to set
     */
    public void setParamCsvContent(String csvContent) {

        m_paramCsvContent = csvContent;
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
     * Sets the path to the xslt file.<p>
     *
     * @param xsltFile the file to transform the xml with.
     */
    public void setParamXsltFile(String xsltFile) {

        m_paramXsltFile = xsltFile;
    }
}