/*
 * File   : $Source: /alkacon/cvs/opencms/src-modules/org/opencms/workplace/tools/modules/CmsModulesEdit.java,v $
 * Date   : $Date: 2005/06/07 16:25:39 $
 * Version: $Revision: 1.5 $
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
import org.opencms.jsp.CmsJspActionElement;
import org.opencms.main.OpenCms;
import org.opencms.module.CmsModule;
import org.opencms.security.CmsRoleViolationException;
import org.opencms.security.CmsSecurityException;
import org.opencms.util.CmsStringUtil;
import org.opencms.widgets.CmsDisplayWidget;
import org.opencms.widgets.CmsInputWidget;
import org.opencms.widgets.CmsTextareaWidget;
import org.opencms.widgets.CmsVfsFileWidget;
import org.opencms.workplace.CmsDialog;
import org.opencms.workplace.CmsWidgetDialog;
import org.opencms.workplace.CmsWidgetDialogParameter;
import org.opencms.workplace.CmsWorkplaceSettings;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.jsp.PageContext;

/**
 * Edit class to edit an exiting module.<p>
 * 
 * @author Michael Emmerich (m.emmerich@alkacon.com)
 * 
 * @version $Revision: 1.5 $
 * @since 5.9.1
 */
public class CmsModulesEdit extends CmsWidgetDialog {

    /** The dialog type. */
    public static final String DIALOG_TYPE = "ModulesEdit";

    /** Defines which pages are valid for this dialog. */
    public static final String[] PAGES = {"page1", "page2", "page3"};

    /** The module object that is edited on this dialog. */
    private CmsModule m_module;

    /** Modulename. */
    private String m_paramModule;

    /**
     * Public constructor with JSP action element.<p>
     * 
     * @param jsp an initialized JSP action element
     */
    public CmsModulesEdit(CmsJspActionElement jsp) {

        super(jsp);
    }

    /**
     * Public constructor with JSP variables.<p>
     * 
     * @param context the JSP page context
     * @param req the JSP request
     * @param res the JSP response
     */
    public CmsModulesEdit(PageContext context, HttpServletRequest req, HttpServletResponse res) {

        this(new CmsJspActionElement(context, req, res));
    }

    /** 
     * Commits the edited module.<p>
     */
    public void actionCommit() {

        List errors = new ArrayList();
        // refresh the list
        Map objects = (Map)getSettings().getListObject();
        if (objects != null) {
            objects.remove(CmsModulesList.class.getName());
        }
        // freeze the module
        m_module.initialize(getCms());
        
        //check if we have to update an existing module or to create a new one
        Set moduleNames = OpenCms.getModuleManager().getModuleNames();
        if (moduleNames.contains(m_module.getName())) {
        
            // update the module information
            try {
               OpenCms.getModuleManager().updateModule(getCms(), m_module);
            } catch (CmsConfigurationException ce) {
                errors.add(ce);
            } catch (CmsRoleViolationException re) {
                errors.add(re);
            }
        } else {
            try {
                OpenCms.getModuleManager().addModule(getCms(), m_module);
            } catch (CmsConfigurationException ce) {
                errors.add(ce);
            } catch (CmsSecurityException se) {
                errors.add(se);
            }
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
     * Gets the module parameter.<p>
     * 
     * @return the module parameter
     */
    public String getParamModule() {

        return m_paramModule;
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
            result.append(dialogBlockStart(key("label.moduleinformation")));
            result.append(createWidgetTableStart());
            result.append(createDialogRowsHtml(0, 5));
            result.append(createWidgetTableEnd());
            result.append(dialogBlockStart(key("label.modulecreator")));
            result.append(createWidgetTableStart());
            result.append(createDialogRowsHtml(6, 7));
            result.append(createWidgetTableEnd());
            result.append(dialogBlockEnd());
        } else if (dialog.equals(PAGES[1])) {
            result.append(dialogBlockStart(key("label.parameter")));
            result.append(createWidgetTableStart());
            result.append(createDialogRowsHtml(8, 8));
            result.append(createWidgetTableEnd());
            result.append(dialogBlockEnd());
        } else if (dialog.equals(PAGES[2])) {
            result.append(dialogBlockStart(key("label.resource")));
            result.append(createWidgetTableStart());
            result.append(createDialogRowsHtml(9, 9));
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

        if (CmsStringUtil.isEmpty(m_module.getName())) {
            addWidget(new CmsWidgetDialogParameter(m_module, "name", PAGES[0], new CmsInputWidget()));
        } else {
            addWidget(new CmsWidgetDialogParameter(m_module, "name", PAGES[0], new CmsDisplayWidget()));
        }
        addWidget(new CmsWidgetDialogParameter(m_module, "niceName", PAGES[0], new CmsInputWidget()));
        addWidget(new CmsWidgetDialogParameter(m_module, "description", PAGES[0], new CmsTextareaWidget()));
        addWidget(new CmsWidgetDialogParameter(m_module, "version.version", PAGES[0], new CmsInputWidget()));
        addWidget(new CmsWidgetDialogParameter(m_module, "group", PAGES[0], new CmsInputWidget()));
        addWidget(new CmsWidgetDialogParameter(m_module, "actionClass", PAGES[0], new CmsInputWidget()));
        addWidget(new CmsWidgetDialogParameter(m_module, "authorName", PAGES[0], new CmsInputWidget()));
        addWidget(new CmsWidgetDialogParameter(m_module, "authorEmail", PAGES[0], new CmsInputWidget()));

        addWidget(new CmsWidgetDialogParameter(m_module, "parameters", PAGES[1], new CmsInputWidget()));

        addWidget(new CmsWidgetDialogParameter(m_module, "resources", PAGES[2], new CmsVfsFileWidget()));

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
            m_module = new CmsModule();

        } else {
            // reuse module stored in session
            m_module = (CmsModule)((CmsModule)o).clone();
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
        setDialogObject(m_module);
    }
}
