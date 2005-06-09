/*
 * File   : $Source: /alkacon/cvs/opencms/src-modules/org/opencms/workplace/tools/database/CmsDatabaseExportDialog.java,v $
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

import org.opencms.importexport.CmsVfsImportExportHandler;
import org.opencms.jsp.CmsJspActionElement;
import org.opencms.main.CmsRuntimeException;
import org.opencms.main.OpenCms;
import org.opencms.util.CmsStringUtil;
import org.opencms.widgets.CmsCalendarWidget;
import org.opencms.widgets.CmsCheckboxWidget;
import org.opencms.widgets.CmsComboWidget;
import org.opencms.widgets.CmsVfsFileWidget;
import org.opencms.workplace.CmsWidgetDialog;
import org.opencms.workplace.CmsWidgetDialogParameter;
import org.opencms.workplace.CmsWorkplaceSettings;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.jsp.PageContext;

/**
 * Widget dialog that sets the export options to export VFS resources to the OpenCms server.<p>
 * 
 * @author  Andreas Zahner (a.zahner@alkacon.com)
 * @version $Revision: 1.1 $
 * @since 6.0
 */
public class CmsDatabaseExportDialog extends CmsWidgetDialog {

    /** The import JSP report workplace URI. */
    protected static final String EXPORT_ACTION_REPORT =  C_PATH_WORKPLACE + "admin/database/reports/export.html";
    
    /** Defines which pages are valid for this dialog. */
    public static final String[] PAGES = {"page1"};
    
    /** The export handler object that is edited on this dialog. */
    private CmsVfsImportExportHandler m_exportHandler;
    
    /**
     * Public constructor with JSP action element.<p>
     * 
     * @param jsp an initialized JSP action element
     */
    public CmsDatabaseExportDialog(CmsJspActionElement jsp) {

        super(jsp);
    }

    /**
     * Public constructor with JSP variables.<p>
     * 
     * @param context the JSP page context
     * @param req the JSP request
     * @param res the JSP response
     */
    public CmsDatabaseExportDialog(PageContext context, HttpServletRequest req, HttpServletResponse res) {

        this(new CmsJspActionElement(context, req, res));
    }

    /**
     * @see org.opencms.workplace.CmsWidgetDialog#actionCommit()
     */
    public void actionCommit() {

        try {
            // create absolute RFS path and store it in dialog object
            String exportFileName = OpenCms.getSystemInfo().getAbsoluteRfsPathRelativeToWebInf(OpenCms.getSystemInfo().getPackagesRfsPath() + File.separator + m_exportHandler.getFileName());
            m_exportHandler.setFileName(exportFileName);
            setDialogObject(m_exportHandler);
            Map params = new HashMap();
            // set the name of this class to get dialog object in report
            params.put(CmsDatabaseExportReport.PARAM_CLASSNAME, this.getClass().getName());
            // set style to display report in correct layout
            params.put(PARAM_STYLE, "new");
            // set close link to get back to overview after finishing the import
            params.put(PARAM_CLOSELINK, getToolManager().linkForPath(getJsp(), "/database", null));
            // redirect to the report output JSP
            getToolManager().jspRedirectPage(this, EXPORT_ACTION_REPORT, params);
        } catch (IOException e) {
            throw new CmsRuntimeException(Messages.get().container(Messages.ERR_ACTION_FILE_EXPORT_0), e);
        }
    }
    
    /**
     * @see org.opencms.workplace.CmsWidgetDialog#createDialogHtml(java.lang.String)
     */
    protected String createDialogHtml(String dialog) {

        StringBuffer result = new StringBuffer(1024);

        // create table
        result.append(createWidgetTableStart());

        // show error header once if there were validation errors
        result.append(createWidgetErrorHeader());
        
        // create export settings block
        result.append(dialogBlockStart(key(Messages.GUI_EDITOR_LABEL_EXPORTSETTINGS_BLOCK_0)));
        result.append(createWidgetTableStart());
        result.append(createDialogRowsHtml(0, 4));
        result.append(createWidgetTableEnd());
        result.append(dialogBlockEnd());
        
        // create export file name block
        result.append(dialogBlockStart(key(Messages.GUI_EDITOR_LABEL_EXPORTFILE_BLOCK_0)));
        result.append(createWidgetTableStart());
        result.append(createDialogRowsHtml(5, 5));
        result.append(createWidgetTableEnd());
        result.append(dialogBlockEnd());
        
        // close table
        result.append(createWidgetTableEnd());

        return result.toString();
    }

    /**
     * @see org.opencms.workplace.CmsWidgetDialog#defineWidgets()
     */
    protected void defineWidgets() {

        // initialize the sexport object to use for the dialog
        initDatabaseExportObject();
        
        addWidget(new CmsWidgetDialogParameter(m_exportHandler, "exportPaths", "/", PAGES[0], new CmsVfsFileWidget(), 1, CmsWidgetDialogParameter.MAX_OCCURENCES));
        addWidget(new CmsWidgetDialogParameter(m_exportHandler, "excludeSystem", PAGES[0], new CmsCheckboxWidget()));
        addWidget(new CmsWidgetDialogParameter(m_exportHandler, "excludeUnchanged", PAGES[0], new CmsCheckboxWidget()));
        addWidget(new CmsWidgetDialogParameter(m_exportHandler, "exportUserdata", PAGES[0], new CmsCheckboxWidget()));
        addWidget(new CmsWidgetDialogParameter(m_exportHandler, "contentAge", "0", PAGES[0], new CmsCalendarWidget(), 0, 1));
        addWidget(new CmsWidgetDialogParameter(m_exportHandler, "fileName", PAGES[0], new CmsComboWidget(CmsDatabaseImportFromServer.getFilesFromServer())));
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
     * Initializes the scheduled job object to work with depending on the dialog state and request parameters.<p>
     * 
     * Three initializations of the scheduled job object on first dialog call are possible:
     * <ul>
     * <li>edit an existing scheduled job</li>
     * <li>create a new scheduled job</li>
     * <li>copy an existing scheduled job and edit it</li>
     * </ul>
     */
    protected void initDatabaseExportObject() {

        Object o;

        if (CmsStringUtil.isEmpty(getParamAction())) {
            o = new CmsVfsImportExportHandler();
        } else {
            // this is not the initial call, get the job object from session
            o = getDialogObject();
        }
        
        if (!(o instanceof CmsVfsImportExportHandler)) {
          // create a new export handler object
          m_exportHandler = new CmsVfsImportExportHandler();
      } else {
          // reuse export handler object stored in session
          m_exportHandler = (CmsVfsImportExportHandler)o;
      }
    }

    /**
     * @see org.opencms.workplace.CmsWorkplace#initWorkplaceRequestValues(org.opencms.workplace.CmsWorkplaceSettings, javax.servlet.http.HttpServletRequest)
     */
    protected void initWorkplaceRequestValues(CmsWorkplaceSettings settings, HttpServletRequest request) {

        // initialize parameters and dialog actions in super implementation
        super.initWorkplaceRequestValues(settings, request);

        // save the current state of the export handler (may be changed because of the widget values)
        setDialogObject(m_exportHandler);
    }

}