/*
 * File   : $Source: /alkacon/cvs/opencms/src-modules/org/opencms/workplace/tools/modules/CmsModulesList.java,v $
 * Date   : $Date: 2005/06/03 15:21:23 $
 * Version: $Revision: 1.4 $
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

import org.opencms.file.types.I_CmsResourceType;
import org.opencms.jsp.CmsJspActionElement;
import org.opencms.main.CmsRuntimeException;
import org.opencms.main.OpenCms;
import org.opencms.module.CmsModule;
import org.opencms.module.CmsModuleDependency;
import org.opencms.workplace.explorer.CmsExplorerTypeSettings;
import org.opencms.workplace.list.A_CmsListDialog;
import org.opencms.workplace.list.CmsListColumnAlignEnum;
import org.opencms.workplace.list.CmsListColumnDefinition;
import org.opencms.workplace.list.CmsListDefaultAction;
import org.opencms.workplace.list.CmsListDirectAction;
import org.opencms.workplace.list.CmsListItem;
import org.opencms.workplace.list.CmsListItemDetails;
import org.opencms.workplace.list.CmsListItemDetailsFormatter;
import org.opencms.workplace.list.CmsListMetadata;
import org.opencms.workplace.list.CmsListMultiAction;
import org.opencms.workplace.list.CmsListSearchAction;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.jsp.PageContext;

/**
 * Main module management view.<p>
 * 
 * @author Michael Emmerich (m.emmerich@alkacon.com) 
 * @version $Revision: 1.4 $
 * @since 5.7.3
 */
public class CmsModulesList extends A_CmsListDialog {

    /** List action delete. */
    public static final String LIST_ACTION_DELETE = "delete";

    /** list action id constant. */
    public static final String LIST_ACTION_EDIT = "edit";

    /** List action export. */
    public static final String LIST_ACTION_EXPORT = "export";

    /** List action multi delete. */
    public static final String LIST_ACTION_MDELETE = "mdelete";

    /** List column delete. */
    public static final String LIST_COLUMN_DELETE = "delete";

    /** List column export. */
    public static final String LIST_COLUMN_EDIT = "edit";
    
    /** List column export. */
    public static final String LIST_COLUMN_EXPORT = "export";

    /** list column id constant. */
    public static final String LIST_COLUMN_GROUP = "group";

    /** list column id constant. */
    public static final String LIST_COLUMN_NAME = "name";

    /** list column id constant. */
    public static final String LIST_COLUMN_NICENAME = "nicename";

    /** list column id constant. */
    public static final String LIST_COLUMN_VERSION = "version";

    /** List detail author info. */
    public static final String LIST_DETAIL_AUTHORINFO = "authorinfo";

    /** List detail dependencies info. */
    public static final String LIST_DETAIL_DEPENDENCIES = "dependenciesinfo";

    /** List detail  resources info. */
    public static final String LIST_DETAIL_RESOURCES = "resourcestinfo";

    /** List detail restypes info. */
    public static final String LIST_DETAIL_RESTYPES = "restypesinfo";

    /** list id constant. */
    public static final String LIST_ID = "modules";

    /** Module parameter. */
    public static final String PARAM_MODULE = "module";
    
    /** Path to the list buttons. */
    public static final String PATH_BUTTONS = "tools/modules/buttons/";
    
    /** Path to the module reports. */
    public static final String PATH_REPORTS = "/system/workplace/admin/modules/reports/";

    /**
     * Public constructor.<p>
     * 
     * @param jsp an initialized JSP action element
     */
    public CmsModulesList(CmsJspActionElement jsp) {

        super(jsp, LIST_ID, Messages.get().container(Messages.GUI_MODULES_LIST_NAME_0), LIST_COLUMN_NAME, null);
    }

    /**
     * Public constructor with JSP variables.<p>
     * 
     * @param context the JSP page context
     * @param req the JSP request
     * @param res the JSP response
     */
    public CmsModulesList(PageContext context, HttpServletRequest req, HttpServletResponse res) {

        this(new CmsJspActionElement(context, req, res));
    }

    /**
     * This method should handle every defined list multi action,
     * by comparing <code>{@link #getParamListAction()}</code> with the id 
     * of the action to execute.<p> 
     */
    public void executeListMultiActions() {

        if (getParamListAction().equals(LIST_ACTION_MDELETE)) {
            String moduleList = "";
            try {
                // execute the delete multiaction
                Iterator itItems = getSelectedItems().iterator();
                StringBuffer modules = new StringBuffer(32);
                while (itItems.hasNext()) {
                    CmsListItem listItem = (CmsListItem)itItems.next();
                    modules.append(listItem.getId());
                    modules.append(",");
                }
                moduleList = new String(modules);
                moduleList = moduleList.substring(0, moduleList.length() - 1);
                Map params = new HashMap();
                params.put(PARAM_MODULE, moduleList);
                params.put(PARAM_ACTION, DIALOG_INITIAL);
                params.put(PARAM_STYLE, "new");
                getToolManager().jspRedirectPage(this, PATH_REPORTS + "delete.html", params);
            } catch (IOException e) {
                // should never happen
                throw new CmsRuntimeException(Messages.get().container(
                    Messages.ERR_ACTION_MODULES_DELETE_1,
                    moduleList), e);
            }
        }
        listSave();
    }

    /**
     * This method should handle every defined list single action,
     * by comparing <code>{@link #getParamListAction()}</code> with the id 
     * of the action to execute.<p> 
     */
    public void executeListSingleActions() {

        String module = getSelectedItem().getId();
        Map params = new HashMap();
        params.put(PARAM_MODULE, module);
        if (getParamListAction().equals(LIST_ACTION_EDIT)) {
            // edit a module from the list
            try {
                // forward to the edit module screen      
                params.put(PARAM_ACTION, DIALOG_INITIAL);
                getToolManager().jspRedirectTool(this, "/modules/edit", params);
            } catch (IOException e) {
                // should never happen
                throw new CmsRuntimeException(Messages.get().container(
                    Messages.ERR_ACTION_MODULE_EDIT_1,
                    module), e);
            }
        } else if (getParamListAction().equals(LIST_ACTION_DELETE)) {
            try {
                // forward to the delete module screen   
                params.put(PARAM_ACTION, DIALOG_INITIAL);
                params.put(PARAM_STYLE, "new");
                getToolManager().jspRedirectPage(this, PATH_REPORTS  + "delete.html", params);
            } catch (IOException e) {
                // should never happen
                throw new CmsRuntimeException(Messages.get().container(
                    Messages.ERR_ACTION_MODULE_DELETE_1,
                    module), e);
            }
        } else if (getParamListAction().equals(LIST_ACTION_EXPORT)) {
            try {
                // forward to the delete module screen   
                params.put(PARAM_ACTION, DIALOG_INITIAL);
                params.put(PARAM_STYLE, "new");
                getToolManager().jspRedirectPage(this, "/system/workplace/admin/modules/reports/export.html", params);
            } catch (IOException e) {
                // should never happen
                throw new CmsRuntimeException(Messages.get().container(
                    Messages.ERR_ACTION_MODULE_EXPORT_1,
                    module), e);
            }
        }
        listSave();
    }

    /**
     * @see org.opencms.workplace.list.A_CmsListDialog#getListItems()
     */
    protected List getListItems() {

        List ret = new ArrayList();
        // get content
        Set moduleNames = OpenCms.getModuleManager().getModuleNames();
        Iterator i = moduleNames.iterator();
        while (i.hasNext()) {
            String moduleName = (String)i.next();
            CmsModule module = OpenCms.getModuleManager().getModule(moduleName);
            CmsListItem item = getList().newItem(module.getName());
            // name
            item.set(LIST_COLUMN_NAME, moduleName);
            // nicename
            item.set(LIST_COLUMN_NICENAME, module.getNiceName());
            // author
            StringBuffer author = new StringBuffer(32);
            author.append(module.getAuthorName());
            author.append("&nbsp;(");
            author.append(module.getAuthorEmail());
            author.append(")");
            item.set(LIST_DETAIL_AUTHORINFO, author);
            //version
            item.set(LIST_COLUMN_VERSION, module.getVersion());
            //group           
            item.set(LIST_COLUMN_GROUP, module.getGroup());
            //resources
            StringBuffer resources = new StringBuffer(32);
            Iterator j = module.getResources().iterator();
            while (j.hasNext()) {
                String resource = (String)j.next();
                resources.append(resource);
                resources.append("<br>");
            }
            item.set(LIST_DETAIL_RESOURCES, resources);
            // dependencies
            StringBuffer dependencies = new StringBuffer(32);
            Iterator k = module.getDependencies().iterator();
            while (k.hasNext()) {
                CmsModuleDependency dep = (CmsModuleDependency)k.next();
                dependencies.append(dep.getName());
                dependencies.append("&nbsp;Version:");
                dependencies.append(dep.getVersion());
                dependencies.append("<br>");
            }
            item.set(LIST_DETAIL_DEPENDENCIES, dependencies);
            // resourcetypes
            StringBuffer restypes = new StringBuffer(32);
            Iterator l = module.getResourceTypes().iterator();
            boolean addRestypes = false;
            while (l.hasNext()) {
                addRestypes = true;
                I_CmsResourceType resourceType = (I_CmsResourceType)l.next();
                restypes.append(Messages.get().key(Messages.GUI_MODULES_LABEL_RESTYPES_DETAIL_0));
                restypes.append(":&nbsp;");
                restypes.append(resourceType.getTypeName());
                restypes.append("&nbsp;ID:");
                restypes.append(resourceType.getTypeId());
                restypes.append("<br>");
            }
            StringBuffer explorersettings = new StringBuffer(32);
            Iterator m = module.getExplorerTypes().iterator();
            boolean addExplorersettings = false;
            while (m.hasNext()) {
                addExplorersettings = true;
                CmsExplorerTypeSettings settings = (CmsExplorerTypeSettings)m.next();
                explorersettings.append(Messages.get().key(Messages.GUI_MODULES_LABEL_EXPLORERSETTINGSS_DETAIL_0));
                explorersettings.append(":&nbsp;");
                explorersettings.append(settings.getName());
                explorersettings.append("&nbsp;(");
                explorersettings.append(settings.getReference());
                explorersettings.append(")<br>");
            }

            StringBuffer resExpSettings = new StringBuffer(32);
            if (addRestypes) {
                resExpSettings.append(restypes);
            }
            if (addExplorersettings) {
                resExpSettings.append(explorersettings);
            }
            item.set(LIST_DETAIL_RESTYPES, resExpSettings);

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
        CmsListDirectAction editColAction = new CmsListDirectAction(LIST_ID, LIST_ACTION_EDIT);
        editColAction.setName(Messages.get().container(Messages.GUI_MODULES_LIST_ACTION_EDIT_NAME_0));
        editColAction.setHelpText(Messages.get().container(Messages.GUI_MODULES_LIST_ACTION_EDIT_HELP_0));
        editColAction.setIconPath(PATH_BUTTONS + "modules.png");
        editColAction.setEnabled(true);
        editColAction.setConfirmationMessage(null);
        editCol.addDirectAction(editColAction);
        metadata.addColumn(editCol);
        
        // add column for export action
        CmsListColumnDefinition expCol = new CmsListColumnDefinition(LIST_COLUMN_EXPORT);
        expCol.setName(Messages.get().container(Messages.GUI_MODULES_LIST_COLS_EXPORT_0));
        expCol.setWidth("20");
        expCol.setSorteable(false);
        expCol.setAlign(CmsListColumnAlignEnum.ALIGN_CENTER);
        // direct action: export module
        CmsListDirectAction expModule = new CmsListDirectAction(LIST_ID, LIST_ACTION_EXPORT);
        expModule.setColumn(LIST_COLUMN_NICENAME);
        expModule.setName(Messages.get().container(Messages.GUI_MODULES_LIST_ACTION_EXPORT_NAME_0));
        expModule.setConfirmationMessage(Messages.get().container(Messages.GUI_MODULES_LIST_ACTION_EXPORT_CONF_1));
        expModule.setIconPath(PATH_BUTTONS + "export.png");
        expModule.setEnabled(true);
        expModule.setHelpText(Messages.get().container(Messages.GUI_MODULES_LIST_ACTION_EXPORT_HELP_1));
        expCol.addDirectAction(expModule);
        metadata.addColumn(expCol);

        // add column for delete action
        CmsListColumnDefinition delCol = new CmsListColumnDefinition(LIST_COLUMN_DELETE);
        delCol.setName(Messages.get().container(Messages.GUI_MODULES_LIST_COLS_DELETE_0));
        delCol.setWidth("20");
        delCol.setSorteable(false);
        delCol.setAlign(CmsListColumnAlignEnum.ALIGN_CENTER);
        // direct action: delete module
        CmsListDirectAction delModule = new CmsListDirectAction(LIST_ID, LIST_ACTION_DELETE);
        delModule.setColumn(LIST_COLUMN_NICENAME);
        delModule.setName(Messages.get().container(Messages.GUI_MODULES_LIST_ACTION_DELETE_NAME_0));
        delModule.setConfirmationMessage(Messages.get().container(Messages.GUI_MODULES_LIST_ACTION_DELETE_CONF_1));
        delModule.setIconPath(ICON_DELETE);
        delModule.setEnabled(true);
        delModule.setHelpText(Messages.get().container(Messages.GUI_MODULES_LIST_ACTION_DELETE_HELP_1));
        delCol.addDirectAction(delModule);
        metadata.addColumn(delCol);

        // add column for name
        CmsListColumnDefinition nameCol = new CmsListColumnDefinition(LIST_COLUMN_NAME);
        nameCol.setName(Messages.get().container(Messages.GUI_MODULES_LIST_COLS_NAME_0));
        nameCol.setWidth("30%");
        nameCol.setAlign(CmsListColumnAlignEnum.ALIGN_LEFT);
        // create default edit action for name column: edit module
        CmsListDefaultAction nameColAction = new CmsListDefaultAction(LIST_ID, LIST_ACTION_EDIT);
        nameColAction.setName(Messages.get().container(Messages.GUI_MODULES_LIST_ACTION_EDIT_NAME_0));
        nameColAction.setIconPath(null);

        nameColAction.setHelpText(Messages.get().container(Messages.GUI_MODULES_LIST_ACTION_EDIT_HELP_0));
        nameColAction.setEnabled(true);
        nameColAction.setConfirmationMessage(null);
        // set action for the name column
        nameCol.setDefaultAction(nameColAction);
        metadata.addColumn(nameCol);

        // add column for nicename
        CmsListColumnDefinition nicenameCol = new CmsListColumnDefinition(LIST_COLUMN_NICENAME);
        nicenameCol.setName(Messages.get().container(Messages.GUI_MODULES_LIST_COLS_NICENAME_0));
        nicenameCol.setWidth("50%");
        nicenameCol.setAlign(CmsListColumnAlignEnum.ALIGN_LEFT);
        metadata.addColumn(nicenameCol);

        // add column for group
        CmsListColumnDefinition groupCol = new CmsListColumnDefinition(LIST_COLUMN_GROUP);
        groupCol.setName(Messages.get().container(Messages.GUI_MODULES_LIST_COLS_GROUP_0));
        groupCol.setWidth("10%");
        groupCol.setAlign(CmsListColumnAlignEnum.ALIGN_LEFT);
        CmsModulesListGroupFormatter groupFormatter = new CmsModulesListGroupFormatter();
        groupCol.setFormatter(groupFormatter);
        metadata.addColumn(groupCol);

        // add column for version
        CmsListColumnDefinition versionCol = new CmsListColumnDefinition(LIST_COLUMN_VERSION);
        versionCol.setName(Messages.get().container(Messages.GUI_MODULES_LIST_COLS_VERSION_0));
        versionCol.setWidth("10%");
        versionCol.setAlign(CmsListColumnAlignEnum.ALIGN_CENTER);
        metadata.addColumn(versionCol);

    }

    /**
     * @see org.opencms.workplace.list.A_CmsListDialog#setIndependentActions(org.opencms.workplace.list.CmsListMetadata)
     */
    protected void setIndependentActions(CmsListMetadata metadata) {

        // create list item detail
        CmsListItemDetails modulesAuthorInfoDetails = new CmsListItemDetails(LIST_ID, LIST_DETAIL_AUTHORINFO);
        modulesAuthorInfoDetails.setAtColumn(LIST_COLUMN_NAME);
        modulesAuthorInfoDetails.setVisible(false);
        modulesAuthorInfoDetails.setFormatter(new CmsListItemDetailsFormatter(Messages.get().container(
            Messages.GUI_MODULES_LABEL_AUTHOR_0)));
        modulesAuthorInfoDetails.setShowActionName(Messages.get().container(
            Messages.GUI_MODULES_DETAIL_SHOW_AUTHORINFO_NAME_0));
        modulesAuthorInfoDetails.setShowActionHelpText(Messages.get().container(
            Messages.GUI_MODULES_DETAIL_SHOW_AUTHORINFO_HELP_0));
        modulesAuthorInfoDetails.setHideActionName(Messages.get().container(
            Messages.GUI_MODULES_DETAIL_HIDE_AUTHORINFO_NAME_0));
        modulesAuthorInfoDetails.setHideActionHelpText(Messages.get().container(
            Messages.GUI_MODULES_DETAIL_HIDE_AUTHORINFO_HELP_0));

        // add author info item detail to meta data
        metadata.addItemDetails(modulesAuthorInfoDetails);

        // create list item detail
        CmsListItemDetails resourcesDetails = new CmsListItemDetails(LIST_ID, LIST_DETAIL_RESOURCES);
        resourcesDetails.setAtColumn(LIST_COLUMN_NAME);
        resourcesDetails.setVisible(false);
        resourcesDetails.setFormatter(new CmsListItemDetailsFormatter(Messages.get().container(
            Messages.GUI_MODULES_LABEL_RESOURCES_0)));
        resourcesDetails.setShowActionName(Messages.get().container(Messages.GUI_MODULES_DETAIL_SHOW_RESOURCES_NAME_0));
        resourcesDetails.setShowActionHelpText(Messages.get().container(
            Messages.GUI_MODULES_DETAIL_SHOW_RESOURCES_HELP_0));
        resourcesDetails.setHideActionName(Messages.get().container(Messages.GUI_MODULES_DETAIL_HIDE_RESOURCES_NAME_0));
        resourcesDetails.setHideActionHelpText(Messages.get().container(
            Messages.GUI_MODULES_DETAIL_HIDE_RESOURCES_HELP_0));

        // add resources info item detail to meta data
        metadata.addItemDetails(resourcesDetails);

        // create list item detail
        CmsListItemDetails dependenciesDetails = new CmsListItemDetails(LIST_ID, LIST_DETAIL_DEPENDENCIES);
        dependenciesDetails.setAtColumn(LIST_COLUMN_NAME);
        dependenciesDetails.setVisible(false);
        dependenciesDetails.setFormatter(new CmsListItemDetailsFormatter(Messages.get().container(
            Messages.GUI_MODULES_LABEL_DEPENDENCIES_0)));
        dependenciesDetails.setShowActionName(Messages.get().container(
            Messages.GUI_MODULES_DETAIL_SHOW_DEPENDENCIES_NAME_0));
        dependenciesDetails.setShowActionHelpText(Messages.get().container(
            Messages.GUI_MODULES_DETAIL_SHOW_DEPENDENCIES_HELP_0));
        dependenciesDetails.setHideActionName(Messages.get().container(
            Messages.GUI_MODULES_DETAIL_HIDE_DEPENDENCIES_NAME_0));
        dependenciesDetails.setHideActionHelpText(Messages.get().container(
            Messages.GUI_MODULES_DETAIL_HIDE_DEPENDENCIES_HELP_0));

        // add dependencies item detail to meta data
        metadata.addItemDetails(dependenciesDetails);

        // create list item detail
        CmsListItemDetails restypesDetails = new CmsListItemDetails(LIST_ID, LIST_DETAIL_RESTYPES);
        restypesDetails.setAtColumn(LIST_COLUMN_NAME);
        restypesDetails.setVisible(false);
        restypesDetails.setFormatter(new CmsListItemDetailsFormatter(Messages.get().container(
            Messages.GUI_MODULES_LABEL_RESTYPES_0)));
        restypesDetails.setShowActionName(Messages.get().container(Messages.GUI_MODULES_DETAIL_SHOW_RESTYPES_NAME_0));
        restypesDetails.setShowActionHelpText(Messages.get().container(Messages.GUI_MODULES_DETAIL_SHOW_RESTYPES_HELP_0));
        restypesDetails.setHideActionName(Messages.get().container(Messages.GUI_MODULES_DETAIL_HIDE_RESTYPES_NAME_0));
        restypesDetails.setHideActionHelpText(Messages.get().container(Messages.GUI_MODULES_DETAIL_HIDE_RESTYPES_HELP_0));

        // add restypes item detail to meta data
        metadata.addItemDetails(restypesDetails);

        // makes the list searchable
        CmsListSearchAction searchAction = new CmsListSearchAction(
            LIST_ID,
            metadata.getColumnDefinition(LIST_COLUMN_NAME));
        searchAction.addColumn(metadata.getColumnDefinition(LIST_COLUMN_GROUP));
        metadata.setSearchAction(searchAction);

    }

    /**
     * @see org.opencms.workplace.list.A_CmsListDialog#setMultiActions(org.opencms.workplace.list.CmsListMetadata)
     */
    protected void setMultiActions(CmsListMetadata metadata) {

        // add the delete module multi action
        CmsListMultiAction deleteModules = new CmsListMultiAction(LIST_ID, LIST_ACTION_MDELETE);
        deleteModules.setName(Messages.get().container(Messages.GUI_MODULES_LIST_ACTION_MDELETE_NAME_0));
        deleteModules.setConfirmationMessage(Messages.get().container(Messages.GUI_MODULES_LIST_ACTION_MDELETE_CONF_0));
        deleteModules.setIconPath(ICON_MULTI_DELETE);
        deleteModules.setEnabled(true);
        deleteModules.setHelpText(Messages.get().container(Messages.GUI_MODULES_LIST_ACTION_MDELETE_HELP_0));
        metadata.addMultiAction(deleteModules);
    }

}