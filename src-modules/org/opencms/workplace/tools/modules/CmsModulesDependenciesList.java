/*
 * File   : $Source: /alkacon/cvs/opencms/src-modules/org/opencms/workplace/tools/modules/CmsModulesDependenciesList.java,v $
 * Date   : $Date: 2005/09/16 13:11:14 $
 * Version: $Revision: 1.13.2.1 $
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

package org.opencms.workplace.tools.modules;

import org.opencms.configuration.CmsConfigurationException;
import org.opencms.jsp.CmsJspActionElement;
import org.opencms.main.CmsRuntimeException;
import org.opencms.main.OpenCms;
import org.opencms.module.CmsModule;
import org.opencms.module.CmsModuleDependency;
import org.opencms.security.CmsRoleViolationException;
import org.opencms.workplace.list.A_CmsListDialog;
import org.opencms.workplace.list.CmsListColumnAlignEnum;
import org.opencms.workplace.list.CmsListColumnDefinition;
import org.opencms.workplace.list.CmsListDefaultAction;
import org.opencms.workplace.list.CmsListDirectAction;
import org.opencms.workplace.list.CmsListItem;
import org.opencms.workplace.list.CmsListMetadata;
import org.opencms.workplace.list.CmsListMultiAction;
import org.opencms.workplace.list.CmsListOrderEnum;

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
 * Module dependencies view.<p>
 * 
 * @author Michael Emmerich  
 * 
 * @version $Revision: 1.13.2.1 $ 
 * 
 * @since 6.0.0 
 */
public class CmsModulesDependenciesList extends A_CmsListDialog {

    /** List action delete. */
    public static final String LIST_ACTION_DELETE = "ad";

    /** list action id constant. */
    public static final String LIST_ACTION_EDIT = "ae";

    /** List column delete. */
    public static final String LIST_COLUMN_DELETE = "cd";

    /** List column edit. */
    public static final String LIST_COLUMN_EDIT = "ce";

    /** List column name. */
    public static final String LIST_COLUMN_NAME = "cn";

    /** List column version. */
    public static final String LIST_COLUMN_VERSION = "cv";

    /** list action id constant. */
    public static final String LIST_DEFACTION_EDIT = "de";

    /** list id constant. */
    public static final String LIST_ID = "lmd";

    /** List action multi delete. */
    public static final String LIST_MACTION_DELETE = "md";

    /** Dependency parameter. */
    public static final String PARAM_DEPENDENCY = "dependency";

    /** Module parameter. */
    public static final String PARAM_MODULE = "module";

    /** Path to the list buttons. */
    public static final String PATH_BUTTONS = "tools/modules/buttons/";

    /** Modulename. */
    private String m_paramModule;

    /**
     * Public constructor.<p>
     * 
     * @param jsp an initialized JSP action element
     */
    public CmsModulesDependenciesList(CmsJspActionElement jsp) {

        super(
            jsp,
            LIST_ID,
            Messages.get().container(Messages.GUI_DEPENDENCIES_LIST_NAME_0),
            LIST_COLUMN_NAME,
            CmsListOrderEnum.ORDER_ASCENDING,
            null);
    }

    /**
     * Public constructor with JSP variables.<p>
     * 
     * @param context the JSP page context
     * @param req the JSP request
     * @param res the JSP response
     */
    public CmsModulesDependenciesList(PageContext context, HttpServletRequest req, HttpServletResponse res) {

        this(new CmsJspActionElement(context, req, res));
    }

    /**
     * This method should handle every defined list multi action,
     * by comparing <code>{@link #getParamListAction()}</code> with the id 
     * of the action to execute.<p> 
     */
    public void executeListMultiActions() {

        if (getParamListAction().equals(LIST_MACTION_DELETE)) {
            String moduleName = getParamModule();
            // execute the delete multiaction
            Iterator itItems = getSelectedItems().iterator();

            while (itItems.hasNext()) {
                CmsModule module = (CmsModule)OpenCms.getModuleManager().getModule(moduleName).clone();
                CmsListItem listItem = (CmsListItem)itItems.next();
                String dependencyName = listItem.getId();
                deleteDependency(module, dependencyName);
            }
        }
        // refresh the list
        Map objects = (Map)getSettings().getListObject();
        if (objects != null) {
            objects.remove(CmsModulesList.class.getName());
        }
        listSave();

    }

    /**
     * @see org.opencms.workplace.list.A_CmsListDialog#executeListSingleActions()
     */
    public void executeListSingleActions() throws IOException, ServletException {

        String moduleName = getParamModule();
        String dependencyName = getSelectedItem().getId();

        Map params = new HashMap();
        params.put(PARAM_MODULE, moduleName);
        params.put(PARAM_DEPENDENCY, dependencyName);

        if (getParamListAction().equals(LIST_ACTION_DELETE)) {
            // delete a dependency
            CmsModule module = (CmsModule)OpenCms.getModuleManager().getModule(moduleName).clone();
            deleteDependency(module, dependencyName);
        } else if (getParamListAction().equals(LIST_ACTION_EDIT) || getParamListAction().equals(LIST_DEFACTION_EDIT)) {
            // edit a dependency from the list
            params.put(PARAM_ACTION, DIALOG_INITIAL);
            getToolManager().jspForwardTool(this, "/modules/edit/dependencies/edit", params);
        }
        // refresh the list
        Map objects = (Map)getSettings().getListObject();
        if (objects != null) {
            objects.remove(CmsModulesList.class.getName());
        }
        listSave();
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
     * @see org.opencms.workplace.list.A_CmsListDialog#fillDetails(java.lang.String)
     */
    protected void fillDetails(String detailId) {

        // noop
    }

    /**
     * @see org.opencms.workplace.list.A_CmsListDialog#getListItems()
     */
    protected List getListItems() {

        List ret = new ArrayList();

        String moduleName = getParamModule();
        CmsModule module = OpenCms.getModuleManager().getModule(moduleName);
        // get dependencies
        List dependencies = module.getDependencies();
        Iterator i = dependencies.iterator();
        while (i.hasNext()) {
            CmsModuleDependency dependency = (CmsModuleDependency)i.next();
            CmsListItem item = getList().newItem(dependency.getName());
            // name
            item.set(LIST_COLUMN_NAME, dependency.getName());
            // version
            item.set(LIST_COLUMN_VERSION, dependency.getVersion());

            ret.add(item);
        }
        return ret;
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
     * @see org.opencms.workplace.list.A_CmsListDialog#setColumns(org.opencms.workplace.list.CmsListMetadata)
     */
    protected void setColumns(CmsListMetadata metadata) {

        //add column for edit action
        CmsListColumnDefinition editCol = new CmsListColumnDefinition(LIST_COLUMN_EDIT);
        editCol.setName(Messages.get().container(Messages.GUI_MODULES_LIST_COLS_EDIT_0));
        editCol.setWidth("20");
        editCol.setSorteable(false);
        editCol.setAlign(CmsListColumnAlignEnum.ALIGN_CENTER);
        // add the edit action
        CmsListDirectAction editColAction = new CmsListDirectAction(LIST_ACTION_EDIT);
        editColAction.setName(Messages.get().container(Messages.GUI_DEPENDENCIES_LIST_ACTION_EDIT_NAME_0));
        editColAction.setHelpText(Messages.get().container(Messages.GUI_DEPENDENCIES_LIST_ACTION_EDIT_HELP_0));
        editColAction.setIconPath(PATH_BUTTONS + "module_dependencies.png");
        editColAction.setEnabled(true);
        editColAction.setConfirmationMessage(null);
        editCol.addDirectAction(editColAction);
        metadata.addColumn(editCol);

        // add column for delete action
        CmsListColumnDefinition delCol = new CmsListColumnDefinition(LIST_COLUMN_DELETE);
        delCol.setName(Messages.get().container(Messages.GUI_MODULES_LIST_COLS_DELETE_0));
        delCol.setWidth("20");
        delCol.setSorteable(false);
        delCol.setAlign(CmsListColumnAlignEnum.ALIGN_CENTER);
        // direct action: delete module
        CmsListDirectAction delDependency = new CmsListDirectAction(LIST_ACTION_DELETE);
        delDependency.setName(Messages.get().container(Messages.GUI_DEPENDENCIES_LIST_ACTION_DELETE_NAME_0));
        delDependency.setConfirmationMessage(Messages.get().container(
            Messages.GUI_DEPENDENCIES_LIST_ACTION_DELETE_CONF_0));
        delDependency.setIconPath(ICON_DELETE);
        delDependency.setEnabled(true);
        delDependency.setHelpText(Messages.get().container(Messages.GUI_DEPENDENCIES_LIST_ACTION_DELETE_HELP_0));
        delCol.addDirectAction(delDependency);
        metadata.addColumn(delCol);

        // add column for name
        CmsListColumnDefinition nameCol = new CmsListColumnDefinition(LIST_COLUMN_NAME);
        nameCol.setName(Messages.get().container(Messages.GUI_DEPENDENCIES_LIST_COLS_NAME_0));
        nameCol.setWidth("80%");
        nameCol.setAlign(CmsListColumnAlignEnum.ALIGN_LEFT);
        // create default edit action for name column: edit dependency
        CmsListDefaultAction nameColAction = new CmsListDefaultAction(LIST_DEFACTION_EDIT);
        nameColAction.setName(Messages.get().container(Messages.GUI_DEPENDENCIES_LIST_ACTION_OVERVIEW_NAME_0));
        nameColAction.setIconPath(null);
        nameColAction.setHelpText(Messages.get().container(Messages.GUI_DEPENDENCIES_LIST_ACTION_OVERVIEW_HELP_0));
        nameColAction.setEnabled(true);
        nameColAction.setConfirmationMessage(null);
        // set action for the name column
        nameCol.addDefaultAction(nameColAction);
        metadata.addColumn(nameCol);

        // add column for version
        CmsListColumnDefinition versionCol = new CmsListColumnDefinition(LIST_COLUMN_VERSION);
        versionCol.setName(Messages.get().container(Messages.GUI_DEPENDENCIES_LIST_COLS_VERSION_0));
        versionCol.setWidth("20%");
        versionCol.setAlign(CmsListColumnAlignEnum.ALIGN_LEFT);
        metadata.addColumn(versionCol);
    }

    /**
     * @see org.opencms.workplace.list.A_CmsListDialog#setIndependentActions(org.opencms.workplace.list.CmsListMetadata)
     */
    protected void setIndependentActions(CmsListMetadata metadata) {

        // noop
    }

    /**
     * @see org.opencms.workplace.list.A_CmsListDialog#setMultiActions(org.opencms.workplace.list.CmsListMetadata)
     */
    protected void setMultiActions(CmsListMetadata metadata) {

        // add the delete dependencies multi action
        CmsListMultiAction deleteDependencies = new CmsListMultiAction(LIST_MACTION_DELETE);
        deleteDependencies.setName(Messages.get().container(Messages.GUI_DEPENDENCIES_LIST_ACTION_MDELETE_NAME_0));
        deleteDependencies.setConfirmationMessage(Messages.get().container(
            Messages.GUI_DEPENDENCIES_LIST_ACTION_MDELETE_CONF_0));
        deleteDependencies.setIconPath(ICON_MULTI_DELETE);
        deleteDependencies.setEnabled(true);
        deleteDependencies.setHelpText(Messages.get().container(Messages.GUI_DEPENDENCIES_LIST_ACTION_MDELETE_HELP_0));
        metadata.addMultiAction(deleteDependencies);
    }

    /**
     * @see org.opencms.workplace.list.A_CmsListDialog#validateParamaters()
     */
    protected void validateParamaters() throws Exception {

        if (OpenCms.getModuleManager().getModule(getParamModule()) == null) {
            // just throw a dummy exception here since getModule does not produce an exception when a 
            // module is not found
            throw new Exception();
        }
    }

    /**
     * Deletes a module dependency from a module.<p>
     * 
     * @param module the module to delete the dependency from
     * @param dependencyName the name of the dependcency to delete
     */
    private void deleteDependency(CmsModule module, String dependencyName) {

        List oldDependencies = module.getDependencies();
        List newDependencies = new ArrayList();
        Iterator i = oldDependencies.iterator();
        while (i.hasNext()) {
            CmsModuleDependency dep = (CmsModuleDependency)i.next();
            if (!dep.getName().equals(dependencyName)) {
                newDependencies.add(dep);
            }
        }
        module.setDependencies(newDependencies);
        // update the module information
        try {
            OpenCms.getModuleManager().updateModule(getCms(), module);
        } catch (CmsConfigurationException ce) {
            // should never happen
            throw new CmsRuntimeException(Messages.get().container(
                Messages.ERR_ACTION_DEPENDENCIES_DELETE_2,
                dependencyName,
                module.getName()), ce);

        } catch (CmsRoleViolationException re) {
            throw new CmsRuntimeException(Messages.get().container(
                Messages.ERR_ACTION_DEPENDENCIES_DELETE_2,
                dependencyName,
                module.getName()), re);
        }
        getList().removeItem(dependencyName, getLocale());
    }
}