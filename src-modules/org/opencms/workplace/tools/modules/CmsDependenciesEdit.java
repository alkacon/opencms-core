/*
 * File   : $Source: /alkacon/cvs/opencms/src-modules/org/opencms/workplace/tools/modules/CmsDependenciesEdit.java,v $
 * Date   : $Date: 2005/06/16 10:55:02 $
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
import org.opencms.module.CmsModuleDependency;
import org.opencms.security.CmsSecurityException;
import org.opencms.util.CmsStringUtil;
import org.opencms.widgets.CmsInputWidget;
import org.opencms.widgets.CmsSelectWidget;
import org.opencms.widgets.CmsSelectWidgetOption;
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
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.PageContext;

/**
 * Class to edit a module dependencies.<p>
 * 
 * @author Michael Emmerich (m.emmerich@alkacon.com)
 * 
 * @version $Revision: 1.5 $
 * @since 5.9.1
 */
public class CmsDependenciesEdit extends CmsWidgetDialog {

    /** The dialog type. */
    public static final String DIALOG_TYPE = "DependenciesEdit";

    /** Defines which pages are valid for this dialog. */
    public static final String[] PAGES = {"page1"};

    /** The module dependency object that is shown on this dialog. */
    private CmsModuleDependency m_dependency;

    /** Dependency name. */
    private String m_paramDependency;

    /** Modulename. */
    private String m_paramModule;

    /**
     * Public constructor with JSP action element.<p>
     * 
     * @param jsp an initialized JSP action element
     */
    public CmsDependenciesEdit(CmsJspActionElement jsp) {

        super(jsp);
    }

    /**
     * Public constructor with JSP variables.<p>
     * 
     * @param context the JSP page context
     * @param req the JSP request
     * @param res the JSP response
     */
    public CmsDependenciesEdit(PageContext context, HttpServletRequest req, HttpServletResponse res) {

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
            // get the current dependencies from the module
            List oldDependencies = module.getDependencies();
            // now loop through the dependencies and create the new list of dependencies
            List newDependencies = new ArrayList();
            Iterator i = oldDependencies.iterator();
            while (i.hasNext()) {
                CmsModuleDependency dep = (CmsModuleDependency)i.next();
                if (!dep.getName().equals(m_dependency.getName())) {
                    newDependencies.add(dep);
                }
            }
            // update the dependencies
            newDependencies.add(m_dependency);
            module.setDependencies(newDependencies);
            // update the module
            OpenCms.getModuleManager().updateModule(getCms(), module);
            // refresh the list
            Map objects = (Map)getSettings().getListObject();
            if (objects != null) {
                objects.remove(CmsModulesList.class.getName());
                objects.remove(CmsModulesDependenciesList.class.getName());
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
     * Gets the module dependency parameter.<p>
     * 
     * @return the module dependency parameter
     */
    public String getParamDependency() {

        return m_paramDependency;
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
     * Sets the module dependency parameter.<p>
     * @param paramDependency the module dependency parameter
     */
    public void setParamDependency(String paramDependency) {

        m_paramDependency = paramDependency;
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
            result.append(dialogBlockStart(key("label.dependencyinformation")));
            result.append(createWidgetTableStart());
            result.append(createDialogRowsHtml(0, 1));
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

        addWidget(new CmsWidgetDialogParameter(m_dependency, "name", PAGES[0], new CmsSelectWidget(getModules())));
        addWidget(new CmsWidgetDialogParameter(m_dependency, "version.version", PAGES[0], new CmsInputWidget()));

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

        // first get the correct module
        if (CmsStringUtil.isNotEmpty(m_paramModule)) {
            module = (CmsModule)OpenCms.getModuleManager().getModule(m_paramModule).clone();
        } else {
            // create a new module
            module = new CmsModule();
        }

        // now try to get the dependency
        if (CmsStringUtil.isEmpty(getParamAction()) || CmsDialog.DIALOG_INITIAL.equals(getParamAction())) {
            o = null;
        } else {
            // this is not the initial call, get module dependency from session
            o = getDialogObject();
        }

        if (!(o instanceof CmsModuleDependency)) {
            if (m_paramDependency == null) {
                // there was no parameter given, so create a new, empty dependency
                m_dependency = new CmsModuleDependency();
            } else {
                // create a new module dependency by reading it from the module
                List dependencies = module.getDependencies();
                m_dependency = new CmsModuleDependency();
                if (dependencies != null && dependencies.size() > 0) {
                    Iterator i = dependencies.iterator();
                    while (i.hasNext()) {
                        CmsModuleDependency dependency = (CmsModuleDependency)i.next();
                        if (dependency.getName().equals(m_paramDependency)) {
                            m_dependency = dependency;
                        }
                    }
                }
            }

        } else {
            // reuse module dependency stored in session
            m_dependency = (CmsModuleDependency)o;
        }

    }

    /**
     * @see org.opencms.workplace.CmsWorkplace#initWorkplaceRequestValues(org.opencms.workplace.CmsWorkplaceSettings, javax.servlet.http.HttpServletRequest)
     */
    protected void initWorkplaceRequestValues(CmsWorkplaceSettings settings, HttpServletRequest request) {

        // set the dialog type
        setParamDialogtype(DIALOG_TYPE);

        super.initWorkplaceRequestValues(settings, request);

        String moduleName = getParamModule();
        CmsModule module = OpenCms.getModuleManager().getModule(moduleName);

        if (module == null) {
            setAction(ACTION_CANCEL);
            try {
                actionCloseDialog();
            } catch (JspException e) {
                // noop
            }
        }

        // save the current state of the module (may be changed because of the widget values)
        setDialogObject(m_dependency);
    }

    /**
     * Get the list of all modules available.<p>
     * 
     * @return list of module names
     */
    private List getModules() {

        List retVal = new ArrayList();
        // get all modules
        Iterator i = OpenCms.getModuleManager().getModuleNames().iterator();
        // add them to the list of modules
        while (i.hasNext()) {
            String moduleName = (String)i.next();
            if (moduleName.equals(getParamDependency())) {
                // check for the preselection
                retVal.add(new CmsSelectWidgetOption(moduleName, true));
            } else {
                retVal.add(new CmsSelectWidgetOption(moduleName, false));
            }
        }
        return retVal;
    }
}
