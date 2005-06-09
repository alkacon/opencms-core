/*
 * File   : $Source: /alkacon/cvs/opencms/src-modules/org/opencms/workplace/tools/database/CmsDatabaseImportFromHttp.java,v $
 * Date   : $Date: 2005/06/09 07:59:25 $
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
 
package org.opencms.workplace.tools.database;

import org.opencms.file.CmsResource;
import org.opencms.file.CmsRfsException;
import org.opencms.jsp.CmsJspActionElement;
import org.opencms.main.CmsException;
import org.opencms.main.CmsIllegalArgumentException;
import org.opencms.main.CmsRuntimeException;
import org.opencms.main.OpenCms;
import org.opencms.util.CmsStringUtil;
import org.opencms.workplace.CmsDialog;
import org.opencms.workplace.CmsWorkplaceSettings;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspWriter;
import javax.servlet.jsp.PageContext;

import org.apache.commons.fileupload.FileItem;

/**
 * Class to upload a zip file containing VFS resources with HTTP upload.<p>
 * 
 * @author Andreas Zahner (a.zahner@alkacon.com)
 * 
 * @version $Revision: 1.1 $
 * @since 6.0
 */
public class CmsDatabaseImportFromHttp extends CmsDialog {
    
    /** The dialog type. */
    public static final String DIALOG_TYPE = "DatabaseImportHttp";
    
    /** The dialog URI. */
    public static final String DIALOG_URI = C_PATH_WORKPLACE + "admin/database/importhttp.html";
    
    /** Import file request parameter. */
    public static final String PARAM_IMPORTFILE = "importfile";
    
    /** The exception thrown if an error occurs. */
    private CmsException m_exception;
    
    /** The import file name that is uploaded. */
    private String m_paramImportfile;
    
    /**
     * Public constructor with JSP action element.<p>
     * 
     * @param jsp an initialized JSP action element
     */
    public CmsDatabaseImportFromHttp(CmsJspActionElement jsp) {

        super(jsp);
    }

    /**
     * Public constructor with JSP variables.<p>
     * 
     * @param context the JSP page context
     * @param req the JSP request
     * @param res the JSP response
     */
    public CmsDatabaseImportFromHttp(PageContext context, HttpServletRequest req, HttpServletResponse res) {

        this(new CmsJspActionElement(context, req, res));
    }
    
    /**
     * Imports the selected file from the server.<p>
     */
    public void actionCommit() {
        
        try {
            copyFileToServer();
        } catch (CmsException e) {
            // error copying the file to the OpenCms server
            setException(e);
            return;
        }
        try {
            Map params = new HashMap();
            params.put(PARAM_FILE, getParamImportfile());
            // set style to display report in correct layout
            params.put(PARAM_STYLE, "new");
            // set close link to get back to overview after finishing the import
            params.put(PARAM_CLOSELINK, getToolManager().linkForPath(getJsp(), "/database", null));
            // redirect to the report output JSP
            getToolManager().jspRedirectPage(this, CmsDatabaseImportFromServer.IMPORT_ACTION_REPORT, params);
        } catch (IOException e) {
            throw new CmsRuntimeException(Messages.get().container(
                Messages.ERR_ACTION_FILE_UPLOAD_1,
                getParamImportfile()), e);
        }

    }
    
    /**
     * Performs the dialog actions depending on the initialized action and displays the dialog form.<p>
     * 
     * @throws IOException if writing to the JSP out fails
     * @throws JspException if dialog actions fail
     */
    public void displayDialog() throws IOException, JspException {

        switch (getAction()) {

            case ACTION_CANCEL:
                // ACTION: cancel button pressed
                actionCloseDialog();
                break;

            case ACTION_OK:
                // ACTION: ok button pressed
                setParamAction(DIALOG_OK);
                actionCommit();
                if (getException() == null) {
                    // file successfully copied to server
                    break;
                }

            case ACTION_DEFAULT:
            default:
                // ACTION: show dialog (default)
                setParamAction(DIALOG_OK);
                JspWriter out = getJsp().getJspContext().getOut();
                out.print(defaultActionHtml());
        }
    }

    /**
     * Returns the import file name that is uploaded.<p>
     *
     * @return the import file name that is uploaded
     */
    public String getParamImportfile() {

        return m_paramImportfile;
    }
    
    /**
     * Sets the import file name that is uploaded.<p>
     *
     * @param importfile the import file name that is uploaded
     */
    public void setParamImportfile(String importfile) {

        m_paramImportfile = importfile;
    }
    
    /**
     * Creates the HTML for the error message if validation errors were found.<p>
     * 
     * @return the HTML for the error message if validation errors were found
     */
    protected String createDialogErrorMessage() {

        if (getException() != null) {
            StringBuffer result = new StringBuffer(8);
            result.append(dialogBlockStart(""));
            result.append("<table border=\"0\">\n");         
            result.append("<tr><td><img src=\"");
            result.append(getSkinUri()).append("commons/");
            result.append("error.gif");
            result.append("\" border=\"0\" alt=\"\"></td><td class=\"xmlTdError maxwidth\">");
            Throwable t = getException();
            while (t != null) {
                result.append(t.getLocalizedMessage());
                t = t.getCause();
                if (t != null) {
                    result.append("<br>");
                }
            }
            result.append("</table>\n");
            result.append(dialogBlockEnd());
            return result.toString();
        }
        return "";
    }
    
    /**
     * Returns the HTML to build the input form of the upload dialog.<p>
     * 
     * @return the HTML to build the input form of the upload dialog
     */
    protected String defaultActionHtml() {
        
        StringBuffer result = new StringBuffer(32);
        
        result.append(htmlStart());
        result.append(bodyStart(null));
        result.append(dialogStart());
        result.append(dialogContentStart(""));
        result.append("<form name=\"main\" class=\"nomargin\" action=\"");
        result.append(getJsp().link(DIALOG_URI));
        result.append("\" method=\"post\" onsubmit=\"submitAction('");
        result.append(DIALOG_OK);
        result.append("', null, 'main');\" enctype=\"multipart/form-data\">\n");
        result.append(paramsAsHidden());
        if (getParamFramename() == null) {
            result.append("<input type=\"hidden\" name=\"");
            result.append(PARAM_FRAMENAME);
            result.append("\" value=\"\">");
        }
        result.append(createDialogErrorMessage());
        result.append(dialogBlockStart(key(Messages.GUI_DATABASE_IMPORT_BLOCK_0)));
        result.append("<table border=\"0\" width=\"100%\">\n");
        result.append("<tr>\n\t<td style=\"white-space: nowrap;\" unselectable=\"on\">");
        result.append(key(Messages.GUI_DATABASE_IMPORT_FILE_0));
        result.append(":</td>\n");
        result.append("\t<td class=\"maxwidth\">");
        result.append("<input type=\"file\" name=\"");
        result.append(PARAM_IMPORTFILE);
        result.append("\" class=\"maxwidth\" accept=\"application/zip\">");
        result.append("</td>\n</tr>");
        result.append("</table>\n");
        result.append(dialogBlockEnd());
        
        result.append(dialogContentEnd());
        result.append(dialogButtonsOkCancel());
        result.append("</form>\n");
        result.append(dialogEnd());
        result.append(bodyEnd());
        result.append(htmlEnd());              
        return result.toString();
    }
    /**
     * Returns the dialog exception.<p>
     *
     * @return the dialog exception
     */
    protected CmsException getException() {

        return m_exception;
    }
    
    /**
     * @see org.opencms.workplace.CmsWorkplace#initMessages()
     */
    protected void initMessages() {

        // add specific dialog resource bundle
        addMessages(Messages.get().getBundleName());
        // add default resource bundles
        addMessages(org.opencms.workplace.Messages.get().getBundleName());
        addMessages(org.opencms.workplace.tools.Messages.get().getBundleName());
    }

    /**
     * @see org.opencms.workplace.CmsWorkplace#initWorkplaceRequestValues(org.opencms.workplace.CmsWorkplaceSettings, javax.servlet.http.HttpServletRequest)
     */
    protected void initWorkplaceRequestValues(CmsWorkplaceSettings settings, HttpServletRequest request) {

        // set the dialog type
        setParamDialogtype(getClass().getName());

        // fill the parameter values in the get/set methods
        fillParamValues(request);

        // set the action for the JSP switch 
        if (DIALOG_OK.equals(getParamAction())) {
            // ok button pressed
            setAction(ACTION_OK);
        } else if (DIALOG_CANCEL.equals(getParamAction())) {
            // cancel button pressed
            setAction(ACTION_CANCEL);
        } else {
            // first dialog call, set the default action               
            setAction(ACTION_DEFAULT);
        }
    }
    /**
     * Sets the dialog exception.<p>
     *
     * @param exception the dialog exception
     */
    protected void setException(CmsException exception) {

        m_exception = exception;
    }
    
    /**
     * Gets a database importfile from the client and copys it to the server.<p>
     *
     * @return the name of the file or null if something went wrong when importing the file.
     * 
     * @throws CmsIllegalArgumentException if the specified file name is invalid
     * @throws CmsRfsException if generating folders or files on the server fails
     */
    private String copyFileToServer() throws CmsIllegalArgumentException, CmsRfsException {
        
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
        
        String fileName = null;
        
        if (fi != null && CmsStringUtil.isNotEmptyOrWhitespaceOnly(fi.getName())) {
            // file name has been specified, upload the file
            fileName = fi.getName();
            byte[] content = fi.get();
            fi.delete();
            // get the file name without folder information
            fileName = CmsResource.getName(fileName.replace('\\', '/'));
            // first create the folder if it does not exist
            File discFolder = new File(OpenCms.getSystemInfo().getAbsoluteRfsPathRelativeToWebInf(OpenCms.getSystemInfo().getPackagesRfsPath() + File.separator));
            if (! discFolder.exists()) {
                if (! discFolder.mkdir()) {
                   throw new CmsRfsException(Messages.get().container(Messages.ERR_FOLDER_NOT_CREATED_0));
                }
            }
            // write the file into the packages folder of the OpenCms server
            File discFile = new File(OpenCms.getSystemInfo().getAbsoluteRfsPathRelativeToWebInf(OpenCms.getSystemInfo().getPackagesRfsPath() + File.separator + fileName));
            try {
                // write the new file to disk
                OutputStream s = new FileOutputStream(discFile);
                s.write(content);
                s.close();
            } catch (FileNotFoundException e) {
                throw new CmsRfsException(Messages.get().container(Messages.ERR_FILE_NOT_FOUND_1, fileName, e));
            } catch (IOException e) {
                throw new CmsRfsException(Messages.get().container(Messages.ERR_FILE_NOT_WRITTEN_0, e));
            }
        } else {
            // no file name has been specified, throw exception
            throw new CmsIllegalArgumentException(Messages.get().container(Messages.ERR_FILE_NOT_SPECIFIED_0));
        }
        // set the request parameter to the name of the import file
        setParamImportfile(fileName);
        return fileName;
    }
}