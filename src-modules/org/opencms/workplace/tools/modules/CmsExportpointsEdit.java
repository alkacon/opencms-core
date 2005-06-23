/*
 * File   : $Source: /alkacon/cvs/opencms/src-modules/org/opencms/workplace/tools/modules/CmsExportpointsEdit.java,v $
 * Date   : $Date: 2005/06/23 10:11:48 $
 * Version: $Revision: 1.7 $
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

package org.opencms.workplace.tools.modules;

import org.opencms.configuration.CmsConfigurationException;
import org.opencms.db.CmsExportPoint;
import org.opencms.jsp.CmsJspActionElement;
import org.opencms.main.OpenCms;
import org.opencms.module.CmsModule;
import org.opencms.security.CmsSecurityException;
import org.opencms.util.CmsStringUtil;
import org.opencms.widgets.CmsComboWidget;
import org.opencms.widgets.CmsDisplayWidget;
import org.opencms.widgets.CmsSelectWidgetOption;
import org.opencms.widgets.CmsVfsFileWidget;
import org.opencms.workplace.CmsDialog;
import org.opencms.workplace.CmsWidgetDialog;
import org.opencms.workplace.CmsWidgetDialogParameter;
import org.opencms.workplace.CmsWorkplaceSettings;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.jsp.PageContext;

/**
 * Class to edit a module dependencies.<p>
 * 
 * @author Michael Emmerich 
 * 
 * @version $Revision: 1.7 $ 
 * 
 * @since 6.0.0 
 */
public class CmsExportpointsEdit extends CmsWidgetDialog {

    /** The dialog type. */
    public static final String DIALOG_TYPE = "ExportpointsOverview";

    /** Defines which pages are valid for this dialog. */
    public static final String[] PAGES = {"page1"};

    /** The module exportpoints object that is shown on this dialog. */
    private CmsExportPoint m_exportpoint;

    /** Exportpoint name. */
    private String m_paramExportpoint;

    /** Modulename. */
    private String m_paramModule;

    /**
     * Public constructor with JSP action element.<p>
     * 
     * @param jsp an initialized JSP action element
     */
    public CmsExportpointsEdit(CmsJspActionElement jsp) {

        super(jsp);
    }

    /**
     * Public constructor with JSP variables.<p>
     * 
     * @param context the JSP page context
     * @param req the JSP request
     * @param res the JSP response
     */
    public CmsExportpointsEdit(PageContext context, HttpServletRequest req, HttpServletResponse res) {

        this(new CmsJspActionElement(context, req, res));
    }

    /** 
     * Commits the edited module.<p>
     */
    public void actionCommit() {

        List errors = new ArrayList();

        try {
            // get the correct module
            String moduleName = getParamModule();
            CmsModule module = (CmsModule)OpenCms.getModuleManager().getModule(moduleName).clone();
            // get the current exportpoints from the module
            List oldExportpoints = module.getExportPoints();
            // now loop through the exportpoints and create the new list of exportpoints
            List newExportpoints = new ArrayList();
            Iterator i = oldExportpoints.iterator();
            while (i.hasNext()) {
                CmsExportPoint exp = (CmsExportPoint)i.next();
                if (!exp.getUri().equals(m_exportpoint.getUri())) {
                    newExportpoints.add(exp);
                }
            }
            // update the exportpoints
            newExportpoints.add(m_exportpoint);
            module.setExportPoints(newExportpoints);
            // update the module
            OpenCms.getModuleManager().updateModule(getCms(), module);
            // refresh the list
            Map objects = (Map)getSettings().getListObject();
            if (objects != null) {
                objects.remove(CmsModulesList.class.getName());
                objects.remove(CmsExportpointsList.class.getName());
            }
        } catch (CmsConfigurationException ce) {
            errors.add(ce);
        } catch (CmsSecurityException se) {
            errors.add(se);
        }

        // set the list of errors to display when saving failed
        setCommitErrors(errors);
    }

    /**
     * Builds the HTML for the dialog form.<p>
     * 
     * @return the HTML for the dialog form
     */
    public String buildDialogForm() {

        StringBuffer result = new StringBuffer(1024);

        try {

            // create the dialog HTML
            result.append(createDialogHtml(getParamPage()));

        } catch (Throwable t) {
            // TODO: Error handling
        }
        return result.toString();
    }

    /**
     * @see org.opencms.workplace.CmsDialog#getCancelAction()
     */
    public String getCancelAction() {

        // set the default action
        setParamPage((String)getPages().get(0));

        return DIALOG_SET;
    }

    /**
     * Gets the module exportpoint parameter.<p>
     * 
     * @return the module exportpoint parameter
     */
    public String getParamExportpoint() {

        return m_paramExportpoint;
    }

    /**
     * Gets the module parameter.<p>
     * 
     * @return the module parameter
     */
    public String getParamModule() {

        return m_paramModule;
    }

    /** 
     * Sets the module exportpoint parameter.<p>
     * @param  paramExportpoint the module exportpoint parameter
     */
    public void setParamExportpoint(String paramExportpoint) {

        m_paramExportpoint = paramExportpoint;
    }

    /** 
     * Sets the module parameter.<p>
     * @param paramModule the module parameter
     */
    public void setParamModule(String paramModule) {

        m_paramModule = paramModule;
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
            result.append(dialogBlockStart(key("label.exportpointinformation")));
            result.append(createWidgetTableStart());
            result.append(createDialogRowsHtml(0, 2));
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

        initModule();

        List destinations = getDestinations();

        addWidget(new CmsWidgetDialogParameter(m_exportpoint, "uri", PAGES[0], new CmsVfsFileWidget()));
        addWidget(new CmsWidgetDialogParameter(m_exportpoint, "configuredDestination", PAGES[0], new CmsComboWidget(
            destinations)));
        addWidget(new CmsWidgetDialogParameter(m_exportpoint, "destinationPath", PAGES[0], new CmsDisplayWidget()));

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
     * Initializes the module  to work with depending on the dialog state and request parameters.<p>
     */
    protected void initModule() {

        Object o;
        CmsModule module;

        if (CmsStringUtil.isEmpty(getParamAction()) || CmsDialog.DIALOG_INITIAL.equals(getParamAction())) {
            // this is the initial dialog call
            if (CmsStringUtil.isNotEmpty(m_paramModule)) {
                // edit an existing module, get it from manager
                o = OpenCms.getModuleManager().getModule(m_paramModule);
            } else {
                // create a new module
                o = null;
            }
        } else {
            // this is not the initial call, get module from session
            o = getDialogObject();
        }

        if (!(o instanceof CmsModule)) {
            // create a new module
            module = new CmsModule();

        } else {
            // reuse module stored in session
            module = (CmsModule)((CmsModule)o).clone();
        }

        List exportpoints = module.getExportPoints();
        m_exportpoint = new CmsExportPoint();
        if (exportpoints != null && exportpoints.size() > 0) {
            Iterator i = exportpoints.iterator();
            while (i.hasNext()) {
                CmsExportPoint exportpoint = (CmsExportPoint)i.next();
                if (exportpoint.getUri().equals(m_paramExportpoint)) {
                    m_exportpoint = exportpoint;
                }
            }
        }

    }

    /**
     * @see org.opencms.workplace.CmsWorkplace#initWorkplaceRequestValues(org.opencms.workplace.CmsWorkplaceSettings, javax.servlet.http.HttpServletRequest)
     */
    protected void initWorkplaceRequestValues(CmsWorkplaceSettings settings, HttpServletRequest request) {

        // set the dialog type
        setParamDialogtype(DIALOG_TYPE);

        super.initWorkplaceRequestValues(settings, request);

        // save the current state of the module (may be changed because of the widget values)
        setDialogObject(m_exportpoint);

    }

    /**
     * @see org.opencms.workplace.CmsWidgetDialog#validateParamaters()
     */
    protected void validateParamaters() throws Exception {

        String moduleName = getParamModule();
        // check module
        CmsModule module = OpenCms.getModuleManager().getModule(moduleName);
        if (module == null) {
            throw new Exception();
        }
        // check export point
        if (!isNewExportPoint()) {
            Iterator it = module.getExportPoints().iterator();
            while (it.hasNext()) {
                CmsExportPoint ep = (CmsExportPoint)it.next();
                if (ep.getUri().equals(getParamExportpoint())) {
                    // export point found
                    return;
                }
            }
            throw new Exception();
        }
    }

    /**
     * Returns the list of default destinations for export points.<p>
     * 
     * The result list elements are of type <code>{@link org.opencms.widgets.CmsSelectWidgetOption}</code>.<p> 
     * 
     * @return the list of default destinations for export points
     */
    private List getDestinations() {

        List result = new ArrayList();
        result.add(new CmsSelectWidgetOption("WEB-INF/classes/"));
        result.add(new CmsSelectWidgetOption("WEB-INF/lib/"));
        return result;
    }

    /**
     * Checks if the new export point dialog has to be displayed.<p>
     * 
     * @return <code>true</code> if the new export point dialog has to be displayed
     */
    private boolean isNewExportPoint() {

        return getCurrentToolPath().equals("/modules/edit/exportpoints/new");
    }
}