/*
 * File   : $Source: /alkacon/cvs/opencms/src-modules/org/opencms/workplace/tools/database/CmsDatabaseImportFromServer.java,v $
 * Date   : $Date: 2005/06/28 18:38:09 $
 * Version: $Revision: 1.11 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Mananagement System
 *
 * Copyright (c) 2005 Alkacon Software GmbH (http://www.alkacon.com)
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
 * For further information about Alkacon Software GmbH, please see the
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

import org.opencms.jsp.CmsJspActionElement;
import org.opencms.main.OpenCms;
import org.opencms.widgets.CmsDisplayWidget;
import org.opencms.widgets.CmsSelectWidget;
import org.opencms.widgets.CmsSelectWidgetOption;
import org.opencms.workplace.CmsWidgetDialog;
import org.opencms.workplace.CmsWidgetDialogParameter;
import org.opencms.workplace.CmsWorkplaceSettings;
import org.opencms.workplace.tools.CmsToolManager;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.jsp.PageContext;

/**
 * Class to upload a zip file containing VFS resources from the server.<p>
 * 
 * @author Andreas Zahner 
 * 
 * @version $Revision: 1.11 $ 
 * 
 * @since 6.0.0 
 */
public class CmsDatabaseImportFromServer extends CmsWidgetDialog {

    /** The dialog type. */
    public static final String DIALOG_TYPE = "DatabaseImportServer";

    /** Defines which pages are valid for this dialog. */
    public static final String[] PAGES = {"page1"};

    /** Import file request parameter. */
    public static final String PARAM_IMPORTFILE = "importFile";

    /** The import JSP report workplace URI. */
    protected static final String IMPORT_ACTION_REPORT = PATH_WORKPLACE + "admin/database/reports/import.html";

    /** Name of the manifest file used in upload files. */
    private static final String FILE_MANIFEST = "manifest.xml";

    /** Name of the subfolder containing the OpenCms module packages. */
    private static final String FOLDER_MODULES = "modules";

    /** The import file name stored by the selectbox widget. */
    private String m_importFile;

    /**
     * Public constructor with JSP action element.<p>
     * 
     * @param jsp an initialized JSP action element
     */
    public CmsDatabaseImportFromServer(CmsJspActionElement jsp) {

        super(jsp);
    }

    /**
     * Public constructor with JSP variables.<p>
     * 
     * @param context the JSP page context
     * @param req the JSP request
     * @param res the JSP response
     */
    public CmsDatabaseImportFromServer(PageContext context, HttpServletRequest req, HttpServletResponse res) {

        this(new CmsJspActionElement(context, req, res));
    }

    /**
     * Returns the list of all uploadable zip files and uploadable folders available on the server.<p>
     * 
     * @param includeFolders if true, the uploadable folders are included in the list
     * @return the list of all uploadable zip files and uploadable folders available on the server
     */
    protected static List getFileListFromServer(boolean includeFolders) {

        List result = new ArrayList();

        // get the RFS package export path
        String exportpath = OpenCms.getSystemInfo().getPackagesRfsPath();
        File folder = new File(exportpath);

        // get a list of all files of the packages folder
        String[] files = folder.list();
        for (int i = 0; i < files.length; i++) {
            File diskFile = new File(exportpath, files[i]);
            // check this is a file and ends with zip -> this is a database upload file
            if (diskFile.isFile() && diskFile.getName().endsWith(".zip")) {
                result.add(diskFile.getName());
            } else if (diskFile.isDirectory()
                && includeFolders
                && (!diskFile.getName().equalsIgnoreCase(FOLDER_MODULES))
                && ((new File(diskFile + File.separator + FILE_MANIFEST)).exists())) {
                // this is an unpacked package, add it to uploadable files
                result.add(diskFile.getName());
            }
        }

        return result;
    }

    /**
     * @see org.opencms.workplace.CmsWidgetDialog#actionCommit()
     */
    public void actionCommit() throws IOException, ServletException {

        List errors = new ArrayList();

        Map params = new HashMap();
        params.put(PARAM_FILE, getImportFile());
        // set style to display report in correct layout
        params.put(PARAM_STYLE, "new");
        // set close link to get back to overview after finishing the import
        params.put(PARAM_CLOSELINK, CmsToolManager.linkForToolPath(getJsp(), "/database"));
        // redirect to the report output JSP
        getToolManager().jspForwardPage(this, IMPORT_ACTION_REPORT, params);
        // set the list of errors to display when saving failed
        setCommitErrors(errors);
    }

    /**
     * Returns the importFile parameter.<p>
     *
     * @return the importFile parameter
     */
    public String getImportFile() {

        return m_importFile;
    }

    /**
     * Sets the importFile parameter.<p>
     *
     * @param importFile the importFile parameter
     */
    public void setImportFile(String importFile) {

        m_importFile = importFile;
    }

    /**
     * Creates the dialog HTML for all defined widgets of the named dialog (page).<p>  
     * 
     * @param dialog the dialog (page) to get the HTML for
     * @return the dialog HTML for all defined widgets of the named dialog (page)
     */
    protected String createDialogHtml(String dialog) {

        StringBuffer result = new StringBuffer(1024);

        // create table
        result.append(createWidgetTableStart());

        // show error header once if there were validation errors
        result.append(createWidgetErrorHeader());

        if (dialog.equals(PAGES[0])) {
            result.append(dialogBlockStart(key("label.block.importFileFromServer")));
            result.append(createWidgetTableStart());
            result.append(createDialogRowsHtml(0, 0));
            result.append(createWidgetTableEnd());
            result.append(dialogBlockEnd());
        }

        // close table
        result.append(createWidgetTableEnd());

        return result.toString();
    }

    /**
     * Creates the list of widgets for this dialog.<p>
     */
    protected void defineWidgets() {

        // get available files from server
        List files = getFilesFromServer();
        
        if (files.isEmpty()) {
            // no import files available, display message
            addWidget(new CmsWidgetDialogParameter(this, PARAM_IMPORTFILE, PAGES[0], new CmsDisplayWidget(key(Messages.GUI_IMPORTSERVER_NO_DB_EXPORTS_0))));
        } else {
            // add the file select box widget
            addWidget(new CmsWidgetDialogParameter(this, PARAM_IMPORTFILE, PAGES[0], new CmsSelectWidget(files)));
        }
    }

    /**
     * Returns the list of all uploadable zip files and uploadable folders available on the server.<p>
     * 
     * The list is returned as a String separated by "|" to use as configuration parameter for selectbox widgets.<p>
     * 
     * @return pipe separated list of file names
     */
    protected List getFilesFromServer() {

        List retVal = new ArrayList();
        Iterator i = getFileListFromServer(true).iterator();
        while (i.hasNext()) {
            String fileName = (String)i.next();
            retVal.add(new CmsSelectWidgetOption(fileName));
        }
        return retVal;
    }

    /**
     * @see org.opencms.workplace.CmsWidgetDialog#getPageArray()
     */
    protected String[] getPageArray() {

        return PAGES;
    }

    /**
     * @see org.opencms.workplace.CmsWorkplace#initMessages()
     */
    protected void initMessages() {

        // add specific dialog resource bundle
        addMessages(Messages.get().getBundleName());
        // add default resource bundles
        super.initMessages();
    }

    /**
     * @see org.opencms.workplace.CmsWorkplace#initWorkplaceRequestValues(org.opencms.workplace.CmsWorkplaceSettings, javax.servlet.http.HttpServletRequest)
     */
    protected void initWorkplaceRequestValues(CmsWorkplaceSettings settings, HttpServletRequest request) {

        // set the dialog type
        setParamDialogtype(DIALOG_TYPE);

        super.initWorkplaceRequestValues(settings, request);
    }
}