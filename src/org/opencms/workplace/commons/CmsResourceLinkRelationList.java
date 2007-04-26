/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/workplace/commons/CmsResourceLinkRelationList.java,v $
 * Date   : $Date: 2007/04/26 14:11:37 $
 * Version: $Revision: 1.1.2.1 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Mananagement System
 *
 * Copyright (C) 2005 Alkacon Software GmbH (http://www.alkacon.com)
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

package org.opencms.workplace.commons;

import org.opencms.db.CmsUserSettings;
import org.opencms.file.CmsResource;
import org.opencms.i18n.CmsMessageContainer;
import org.opencms.jsp.CmsJspActionElement;
import org.opencms.main.CmsException;
import org.opencms.main.CmsLog;
import org.opencms.relations.CmsRelationType;
import org.opencms.workplace.explorer.CmsResourceUtil;
import org.opencms.workplace.list.A_CmsListExplorerDialog;
import org.opencms.workplace.list.CmsListColumnAlignEnum;
import org.opencms.workplace.list.CmsListColumnDefinition;
import org.opencms.workplace.list.CmsListDateMacroFormatter;
import org.opencms.workplace.list.CmsListDefaultAction;
import org.opencms.workplace.list.CmsListDirectAction;
import org.opencms.workplace.list.CmsListEditResourceAction;
import org.opencms.workplace.list.CmsListItem;
import org.opencms.workplace.list.CmsListItemActionIconComparator;
import org.opencms.workplace.list.CmsListMetadata;
import org.opencms.workplace.list.CmsListOpenResourceAction;
import org.opencms.workplace.list.CmsListResourceLockAction;
import org.opencms.workplace.list.CmsListResourceProjStateAction;
import org.opencms.workplace.list.CmsListResourceTypeIconAction;
import org.opencms.workplace.list.I_CmsListResourceCollector;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;

/**
 * List for resources with relations to a given resource.<p>
 * 
 * @author Raphael Schnuck
 * 
 * @version $Revision: 1.1.2.1 $ 
 * 
 * @since 6.9.1 
 */
public class CmsResourceLinkRelationList extends A_CmsListExplorerDialog {

    /** List column id constant. */
    protected static final String LIST_COLUMN_RELATION_TYPE = "crt";

    /** The log object for this class. */
    protected static final Log LOG = CmsLog.getLog(CmsResourceLinkRelationList.class);

    /** The list id for this class. */
    private static final String LIST_ID = "lrlr";

    /** The list holds the broken links list item ids. */
    private List m_brokenLinks;

    /** The current JSP action element. */
    private CmsJspActionElement m_cms;

    /** The resource collector for this class. */
    private I_CmsListResourceCollector m_collector;

    /** Indicates if the current request shows the source resources for the relations are shown. */
    private boolean m_isSource;

    /** The map to map resources to relation types. */
    private Map m_relationTypes;

    /**
     * Default constructor.<p>
     * 
     * @param jsp an initialized JSP action element
     * @param isSource indicates if the source resources of the relations are shown in the list
     */
    public CmsResourceLinkRelationList(CmsJspActionElement jsp, boolean isSource) {

        super(jsp, LIST_ID, Messages.get().container(Messages.GUI_LINK_RELATION_LIST_NAME_0));
        m_isSource = isSource;
        m_cms = jsp;

        // set the right resource util parameters
        CmsResourceUtil resUtil = getResourceUtil();
        resUtil.setAbbrevLength(50);
        resUtil.setRelativeTo(jsp.getRequestContext().getFolderUri());
        resUtil.setSiteMode(CmsResourceUtil.SITE_MODE_MATCHING);
    }

    /**
     * @see org.opencms.workplace.list.A_CmsListDialog#executeListMultiActions()
     */
    public void executeListMultiActions() {

        throwListUnsupportedActionException();
    }

    /**
     * @see org.opencms.workplace.list.A_CmsListDialog#executeListSingleActions()
     */
    public void executeListSingleActions() {

        throwListUnsupportedActionException();
    }

    /**
     * Returns the list to identify the resources with broken links.<p>
     * 
     * @return the list to identify the resources with broken links
     */
    public List getBrokenLinks() {

        return m_brokenLinks;
    }

    /**
     * @see org.opencms.workplace.list.A_CmsListExplorerDialog#getCollector()
     */
    public I_CmsListResourceCollector getCollector() {

        if (m_collector == null) {
            m_collector = new CmsListResourceLinkRelationCollector(this, getParamResource(), isSource());
        }
        return m_collector;
    }

    /**
     * Returns the relationTypes.<p>
     *
     * @return the relationTypes
     */
    public Map getRelationTypes() {

        return m_relationTypes;
    }

    /**
     * Returns the isSource.<p>
     *
     * @return the isSource
     */
    public boolean isSource() {

        return m_isSource;
    }

    /**
     * Sets the list to identify the resources with broken links.<p>
     * 
     * @param brokenLinks the list to identify the resources with broken links
     */
    public void setBrokenLinks(List brokenLinks) {

        m_brokenLinks = brokenLinks;
    }

    /**
     * Sets the relationTypes.<p>
     *
     * @param relationTypes the relationTypes to set
     */
    public void setRelationTypes(Map relationTypes) {

        m_relationTypes = relationTypes;
    }

    /**
     * Sets the isSource.<p>
     *
     * @param isSource the isSource to set
     */
    public void setSource(boolean isSource) {

        m_isSource = isSource;
    }

    /**
     * @see org.opencms.workplace.list.A_CmsListDialog#fillDetails(java.lang.String)
     */
    protected void fillDetails(String detailId) {

        // noop
    }

    /**
     * @see org.opencms.workplace.list.A_CmsListExplorerDialog#getListItems()
     */
    protected List getListItems() throws CmsException {

        List newItems = new ArrayList();
        List items = super.getListItems();
        Iterator itItems = items.iterator();
        while (itItems.hasNext()) {
            CmsListItem item = (CmsListItem)itItems.next();
            CmsResource resource = getResourceUtil(item).getResource();

            CmsRelationType relationType = (CmsRelationType)((List)getRelationTypes().get(resource)).remove(0);
            String localizedRelationType = relationType.getLocalizedName(m_cms.getRequestContext().getLocale());

            Map itemValues = item.getValues();
            CmsListItem newItem = getList().newItem(localizedRelationType + "_" + resource.getStructureId().toString());

            Iterator itItemValuesKeys = itemValues.keySet().iterator();
            while (itItemValuesKeys.hasNext()) {
                String currentKey = (String)itItemValuesKeys.next();
                newItem.set(currentKey, itemValues.get(currentKey));
            }
            newItem.set(LIST_COLUMN_RELATION_TYPE, localizedRelationType);
            newItems.add(newItem);
        }
        return newItems;
    }

    /**
     * @see org.opencms.workplace.list.A_CmsListExplorerDialog#setColumns(org.opencms.workplace.list.CmsListMetadata)
     */
    protected void setColumns(CmsListMetadata metadata) {

        setColumnVisibilities();

        // position 0: icon
        CmsListColumnDefinition typeIconCol = new CmsListColumnDefinition(LIST_COLUMN_TYPEICON);
        typeIconCol.setName(org.opencms.workplace.list.Messages.get().container(
            org.opencms.workplace.list.Messages.GUI_EXPLORER_LIST_COLS_ICON_0));
        typeIconCol.setHelpText(org.opencms.workplace.list.Messages.get().container(
            org.opencms.workplace.list.Messages.GUI_EXPLORER_LIST_COLS_ICON_HELP_0));
        typeIconCol.setWidth("20");
        typeIconCol.setAlign(CmsListColumnAlignEnum.ALIGN_CENTER);
        typeIconCol.setListItemComparator(new CmsListItemActionIconComparator());

        // add resource icon action
        CmsListDirectAction resourceTypeIconAction = new CmsListResourceTypeIconAction(LIST_ACTION_TYPEICON);
        resourceTypeIconAction.setEnabled(false);
        typeIconCol.addDirectAction(resourceTypeIconAction);
        metadata.addColumn(typeIconCol);

        // position 1: edit button
        CmsListColumnDefinition editIconCol = new CmsListColumnDefinition(LIST_COLUMN_EDIT);
        editIconCol.setName(org.opencms.workplace.list.Messages.get().container(
            org.opencms.workplace.list.Messages.GUI_EXPLORER_LIST_COLS_EDIT_0));
        editIconCol.setHelpText(org.opencms.workplace.list.Messages.get().container(
            org.opencms.workplace.list.Messages.GUI_EXPLORER_LIST_COLS_EDIT_HELP_0));
        editIconCol.setWidth("20");
        editIconCol.setAlign(CmsListColumnAlignEnum.ALIGN_CENTER);

        // add enabled edit action
        CmsListDirectAction editAction = new CmsListEditResourceAction(LIST_ACTION_EDIT, LIST_COLUMN_NAME);
        editAction.setEnabled(true);
        editIconCol.addDirectAction(editAction);
        // add disabled edit action
        CmsListDirectAction noEditAction = new CmsListEditResourceAction(LIST_ACTION_EDIT + "d", LIST_COLUMN_NAME);
        noEditAction.setEnabled(false);
        editIconCol.addDirectAction(noEditAction);
        metadata.addColumn(editIconCol);

        // position 2: lock icon
        CmsListColumnDefinition lockIconCol = new CmsListColumnDefinition(LIST_COLUMN_LOCKICON);
        lockIconCol.setName(org.opencms.workplace.list.Messages.get().container(
            org.opencms.workplace.list.Messages.GUI_EXPLORER_LIST_COLS_LOCK_0));
        lockIconCol.setWidth("20");
        lockIconCol.setAlign(CmsListColumnAlignEnum.ALIGN_CENTER);
        lockIconCol.setListItemComparator(new CmsListItemActionIconComparator());

        // add lock icon action
        CmsListDirectAction resourceLockIconAction = new CmsListResourceLockAction(LIST_ACTION_LOCKICON);
        resourceLockIconAction.setEnabled(false);
        lockIconCol.addDirectAction(resourceLockIconAction);
        metadata.addColumn(lockIconCol);

        // position 3: project state icon, resource is inside or outside current project        
        CmsListColumnDefinition projStateIconCol = new CmsListColumnDefinition(LIST_COLUMN_PROJSTATEICON);
        projStateIconCol.setName(org.opencms.workplace.list.Messages.get().container(
            org.opencms.workplace.list.Messages.GUI_EXPLORER_LIST_COLS_PROJSTATE_0));
        projStateIconCol.setWidth("20");

        // add resource icon action
        CmsListDirectAction resourceProjStateAction = new CmsListResourceProjStateAction(LIST_ACTION_PROJSTATEICON) {

            /**
             * @see org.opencms.workplace.list.CmsListResourceProjStateAction#getIconPath()
             */
            public String getIconPath() {

                if (((CmsResourceLinkRelationList)getWp()).getBrokenLinks() != null) {
                    if (((CmsResourceLinkRelationList)getWp()).getBrokenLinks().contains(getItem().getId())) {
                        return "buttons/deletecontent.png";
                    }
                }
                return super.getIconPath();
            }

            /**
             * @see org.opencms.workplace.tools.A_CmsHtmlIconButton#getName()
             */
            public CmsMessageContainer getName() {

                if (((CmsResourceLinkRelationList)getWp()).getBrokenLinks() != null) {
                    if (((CmsResourceLinkRelationList)getWp()).getBrokenLinks().contains(getItem().getId())) {
                        return Messages.get().container(Messages.GUI_RELATION_LIST_BROKEN_HELP_0);
                    }
                }
                return super.getName();
            }
        };
        resourceProjStateAction.setEnabled(false);
        projStateIconCol.addDirectAction(resourceProjStateAction);
        metadata.addColumn(projStateIconCol);

        CmsListColumnDefinition relationTypeCol = new CmsListResourceLinkRelationExplorerColumn(
            LIST_COLUMN_RELATION_TYPE);
        relationTypeCol.setName(Messages.get().container(Messages.GUI_RELATION_LIST_TYPE_NAME_0));
        metadata.addColumn(relationTypeCol);

        // position 4: name
        CmsListColumnDefinition nameCol = new CmsListResourceLinkRelationExplorerColumn(LIST_COLUMN_NAME);
        nameCol.setName(org.opencms.workplace.explorer.Messages.get().container(
            org.opencms.workplace.explorer.Messages.GUI_INPUT_NAME_0));

        // add resource open action
        CmsListDefaultAction resourceOpenDefAction = new CmsListOpenResourceAction(
            LIST_DEFACTION_OPEN,
            LIST_COLUMN_NAME);
        resourceOpenDefAction.setEnabled(true);
        nameCol.addDefaultAction(resourceOpenDefAction);
        metadata.addColumn(nameCol);
        nameCol.setPrintable(false);

        // position 4: root path for printing
        CmsListColumnDefinition rootPathCol = new CmsListResourceLinkRelationExplorerColumn(LIST_COLUMN_ROOT_PATH);
        rootPathCol.setName(org.opencms.workplace.explorer.Messages.get().container(
            org.opencms.workplace.explorer.Messages.GUI_INPUT_NAME_0));
        rootPathCol.setVisible(false);
        rootPathCol.setPrintable(true);
        metadata.addColumn(rootPathCol);

        // position 5: title
        CmsListColumnDefinition titleCol = new CmsListResourceLinkRelationExplorerColumn(LIST_COLUMN_TITLE);
        titleCol.setName(org.opencms.workplace.explorer.Messages.get().container(
            org.opencms.workplace.explorer.Messages.GUI_INPUT_TITLE_0));
        metadata.addColumn(titleCol);

        // position 6: resource type
        CmsListColumnDefinition typeCol = new CmsListResourceLinkRelationExplorerColumn(LIST_COLUMN_TYPE);
        typeCol.setName(org.opencms.workplace.explorer.Messages.get().container(
            org.opencms.workplace.explorer.Messages.GUI_INPUT_TYPE_0));
        metadata.addColumn(typeCol);

        // position 7: size
        CmsListColumnDefinition sizeCol = new CmsListResourceLinkRelationExplorerColumn(LIST_COLUMN_SIZE);
        sizeCol.setName(org.opencms.workplace.explorer.Messages.get().container(
            org.opencms.workplace.explorer.Messages.GUI_INPUT_SIZE_0));
        metadata.addColumn(sizeCol);

        // position 8: permissions
        CmsListColumnDefinition permissionsCol = new CmsListResourceLinkRelationExplorerColumn(LIST_COLUMN_PERMISSIONS);
        permissionsCol.setName(org.opencms.workplace.explorer.Messages.get().container(
            org.opencms.workplace.explorer.Messages.GUI_INPUT_PERMISSIONS_0));
        metadata.addColumn(permissionsCol);

        // position 9: date of last modification
        CmsListColumnDefinition dateLastModCol = new CmsListResourceLinkRelationExplorerColumn(LIST_COLUMN_DATELASTMOD);
        dateLastModCol.setName(org.opencms.workplace.explorer.Messages.get().container(
            org.opencms.workplace.explorer.Messages.GUI_INPUT_DATELASTMODIFIED_0));
        dateLastModCol.setFormatter(CmsListDateMacroFormatter.getDefaultDateFormatter());
        metadata.addColumn(dateLastModCol);

        // position 10: user who last modified the resource
        CmsListColumnDefinition userLastModCol = new CmsListResourceLinkRelationExplorerColumn(LIST_COLUMN_USERLASTMOD);
        userLastModCol.setName(org.opencms.workplace.explorer.Messages.get().container(
            org.opencms.workplace.explorer.Messages.GUI_INPUT_USERLASTMODIFIED_0));
        metadata.addColumn(userLastModCol);

        // position 11: date of creation
        CmsListColumnDefinition dateCreateCol = new CmsListResourceLinkRelationExplorerColumn(LIST_COLUMN_DATECREATE);
        dateCreateCol.setName(org.opencms.workplace.explorer.Messages.get().container(
            org.opencms.workplace.explorer.Messages.GUI_INPUT_DATECREATED_0));
        dateCreateCol.setFormatter(CmsListDateMacroFormatter.getDefaultDateFormatter());
        metadata.addColumn(dateCreateCol);

        // position 12: user who created the resource
        CmsListColumnDefinition userCreateCol = new CmsListResourceLinkRelationExplorerColumn(LIST_COLUMN_USERCREATE);
        userCreateCol.setName(org.opencms.workplace.explorer.Messages.get().container(
            org.opencms.workplace.explorer.Messages.GUI_INPUT_USERCREATED_0));
        metadata.addColumn(userCreateCol);

        // position 13: date of release
        CmsListColumnDefinition dateReleaseCol = new CmsListResourceLinkRelationExplorerColumn(LIST_COLUMN_DATEREL);
        dateReleaseCol.setName(org.opencms.workplace.explorer.Messages.get().container(
            org.opencms.workplace.explorer.Messages.GUI_INPUT_DATERELEASED_0));
        dateReleaseCol.setFormatter(new CmsListDateMacroFormatter(org.opencms.workplace.list.Messages.get().container(
            org.opencms.workplace.list.Messages.GUI_LIST_DATE_FORMAT_1), new CmsMessageContainer(
            null,
            CmsTouch.DEFAULT_DATE_STRING), CmsResource.DATE_RELEASED_DEFAULT));
        metadata.addColumn(dateReleaseCol);

        // position 14: date of expiration
        CmsListColumnDefinition dateExpirationCol = new CmsListResourceLinkRelationExplorerColumn(LIST_COLUMN_DATEEXP);
        dateExpirationCol.setName(org.opencms.workplace.explorer.Messages.get().container(
            org.opencms.workplace.explorer.Messages.GUI_INPUT_DATEEXPIRED_0));
        dateExpirationCol.setFormatter(new CmsListDateMacroFormatter(
            org.opencms.workplace.list.Messages.get().container(
                org.opencms.workplace.list.Messages.GUI_LIST_DATE_FORMAT_1),
            new CmsMessageContainer(null, CmsTouch.DEFAULT_DATE_STRING),
            CmsResource.DATE_EXPIRED_DEFAULT));
        metadata.addColumn(dateExpirationCol);

        // position 15: state (changed, unchanged, new, deleted)
        CmsListColumnDefinition stateCol = new CmsListResourceLinkRelationExplorerColumn(LIST_COLUMN_STATE);
        stateCol.setName(org.opencms.workplace.explorer.Messages.get().container(
            org.opencms.workplace.explorer.Messages.GUI_INPUT_STATE_0));
        metadata.addColumn(stateCol);

        // position 16: locked by
        CmsListColumnDefinition lockedByCol = new CmsListResourceLinkRelationExplorerColumn(LIST_COLUMN_LOCKEDBY);
        lockedByCol.setName(org.opencms.workplace.explorer.Messages.get().container(
            org.opencms.workplace.explorer.Messages.GUI_INPUT_LOCKEDBY_0));
        metadata.addColumn(lockedByCol);

        // position 17: site
        CmsListColumnDefinition siteCol = new CmsListResourceLinkRelationExplorerColumn(LIST_COLUMN_SITE);
        siteCol.setName(org.opencms.workplace.explorer.Messages.get().container(
            org.opencms.workplace.explorer.Messages.GUI_LABEL_SITE_0));
        metadata.addColumn(siteCol);
    }

    /**
     * @see org.opencms.workplace.list.A_CmsListExplorerDialog#setColumnVisibilities()
     */
    protected void setColumnVisibilities() {

        Map colVisibilities = new HashMap(16);
        // set explorer configurable column visibilities
        colVisibilities.put(new Integer(CmsUserSettings.FILELIST_TITLE), Boolean.TRUE);
        colVisibilities.put(new Integer(CmsUserSettings.FILELIST_TYPE), Boolean.FALSE);
        colVisibilities.put(new Integer(CmsUserSettings.FILELIST_SIZE), Boolean.FALSE);
        colVisibilities.put(new Integer(CmsUserSettings.FILELIST_PERMISSIONS), Boolean.FALSE);
        colVisibilities.put(new Integer(CmsUserSettings.FILELIST_DATE_LASTMODIFIED), Boolean.FALSE);
        colVisibilities.put(new Integer(CmsUserSettings.FILELIST_USER_LASTMODIFIED), Boolean.FALSE);
        colVisibilities.put(new Integer(CmsUserSettings.FILELIST_DATE_CREATED), Boolean.FALSE);
        colVisibilities.put(new Integer(CmsUserSettings.FILELIST_USER_CREATED), Boolean.FALSE);
        colVisibilities.put(new Integer(CmsUserSettings.FILELIST_DATE_RELEASED), Boolean.FALSE);
        colVisibilities.put(new Integer(CmsUserSettings.FILELIST_DATE_EXPIRED), Boolean.FALSE);
        colVisibilities.put(new Integer(CmsUserSettings.FILELIST_STATE), Boolean.FALSE);
        colVisibilities.put(new Integer(CmsUserSettings.FILELIST_LOCKEDBY), Boolean.FALSE);
        // set explorer no configurable column visibilities
        colVisibilities.put(new Integer(LIST_COLUMN_TYPEICON.hashCode()), Boolean.TRUE);
        colVisibilities.put(new Integer(LIST_COLUMN_LOCKICON.hashCode()), Boolean.TRUE);
        colVisibilities.put(new Integer(LIST_COLUMN_PROJSTATEICON.hashCode()), Boolean.TRUE);
        colVisibilities.put(new Integer(LIST_COLUMN_NAME.hashCode()), Boolean.TRUE);
        colVisibilities.put(new Integer(LIST_COLUMN_EDIT.hashCode()), Boolean.FALSE);
        colVisibilities.put(new Integer(LIST_COLUMN_SITE.hashCode()), Boolean.FALSE);

        setColVisibilities(colVisibilities);
    }

    /**
     * @see org.opencms.workplace.list.A_CmsListDialog#setMultiActions(org.opencms.workplace.list.CmsListMetadata)
     */
    protected void setMultiActions(CmsListMetadata metadata) {

        // noop
    }
}
