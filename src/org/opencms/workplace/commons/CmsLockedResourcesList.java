/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/workplace/commons/CmsLockedResourcesList.java,v $
 * Date   : $Date: 2007/03/28 15:39:28 $
 * Version: $Revision: 1.1.2.4 $
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

package org.opencms.workplace.commons;

import org.opencms.db.CmsUserSettings;
import org.opencms.i18n.CmsMessageContainer;
import org.opencms.jsp.CmsJspActionElement;
import org.opencms.workplace.explorer.CmsResourceUtil;
import org.opencms.workplace.list.A_CmsListExplorerDialog;
import org.opencms.workplace.list.CmsListColumnDefinition;
import org.opencms.workplace.list.CmsListDirectAction;
import org.opencms.workplace.list.CmsListExplorerColumn;
import org.opencms.workplace.list.CmsListMetadata;
import org.opencms.workplace.list.CmsListResourceProjStateAction;
import org.opencms.workplace.list.I_CmsListResourceCollector;

import java.util.Iterator;
import java.util.List;

/**
 * Explorer dialog for the project files view.<p>
 * 
 * @author Michael Moossen 
 * 
 * @version $Revision: 1.1.2.4 $ 
 * 
 * @since 6.0.0 
 */
public class CmsLockedResourcesList extends A_CmsListExplorerDialog {

    /** list id constant. */
    public static final String LIST_ID = "llr";

    /** List column id constant. */
    protected static final String LIST_COLUMN_IS_RELATED = "ecir";

    /** The internal collector instance. */
    private I_CmsListResourceCollector m_collector;

    /**
     * Public constructor with JSP action element.<p>
     * 
     * @param jsp an initialized JSP action element
     * @param lockedResources the list of locked resources (as root paths)
     * @param relativeTo the current folder
     */
    public CmsLockedResourcesList(CmsJspActionElement jsp, List lockedResources, String relativeTo) {

        super(jsp, LIST_ID, Messages.get().container(Messages.GUI_LOCKED_FILES_LIST_NAME_0));
        m_collector = new CmsLockedResourcesCollector(this, lockedResources);

        // prevent paging
        getList().setMaxItemsPerPage(Integer.MAX_VALUE);

        // set the right resource util parameters
        CmsResourceUtil resUtil = getResourceUtil();
        resUtil.setAbbrevLength(50);
        resUtil.setRelativeTo(relativeTo);
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
     * @see org.opencms.workplace.list.A_CmsListExplorerDialog#getCollector()
     */
    public I_CmsListResourceCollector getCollector() {

        return m_collector;
    }

    /**
     * @see org.opencms.workplace.list.A_CmsListDialog#fillDetails(java.lang.String)
     */
    protected void fillDetails(String detailId) {

        // no-details
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
     * @see org.opencms.workplace.list.A_CmsListExplorerDialog#isColumnVisible(int)
     */
    protected boolean isColumnVisible(int colFlag) {

        boolean isVisible = (colFlag == CmsUserSettings.FILELIST_TITLE);
        isVisible = isVisible || (colFlag == CmsUserSettings.FILELIST_LOCKEDBY);
        isVisible = isVisible || (colFlag == LIST_COLUMN_TYPEICON.hashCode());
        isVisible = isVisible || (colFlag == LIST_COLUMN_LOCKICON.hashCode());
        isVisible = isVisible || (colFlag == LIST_COLUMN_PROJSTATEICON.hashCode());
        isVisible = isVisible || (colFlag == LIST_COLUMN_NAME.hashCode());
        return isVisible;
    }

    /**
     * @see org.opencms.workplace.list.A_CmsListDialog#setColumns(org.opencms.workplace.list.CmsListMetadata)
     */
    protected void setColumns(CmsListMetadata metadata) {

        super.setColumns(metadata);

        Iterator it = metadata.getColumnDefinitions().iterator();
        while (it.hasNext()) {
            CmsListColumnDefinition colDefinition = (CmsListColumnDefinition)it.next();
            colDefinition.setSorteable(false);
            if (colDefinition.getId().equals(LIST_COLUMN_NAME)) {
                colDefinition.removeDefaultAction(LIST_DEFACTION_OPEN);
                colDefinition.setWidth("60%");
            } else if (colDefinition.getId().equals(LIST_COLUMN_PROJSTATEICON)) {
                colDefinition.removeDirectAction(LIST_ACTION_PROJSTATEICON);
                // add resource state icon action
                CmsListDirectAction resourceProjStateAction = new CmsListResourceProjStateAction(
                    LIST_ACTION_PROJSTATEICON) {

                    /**
                     * @see org.opencms.workplace.list.CmsListResourceProjStateAction#getIconPath()
                     */
                    public String getIconPath() {

                        if (((Boolean)getItem().get(LIST_COLUMN_IS_RELATED)).booleanValue()) {
                            return "explorer/related_resource.png";
                        }
                        return super.getIconPath();
                    }

                    /**
                     * @see org.opencms.workplace.list.CmsListResourceProjStateAction#getName()
                     */
                    public CmsMessageContainer getName() {

                        if (((Boolean)getItem().get(LIST_COLUMN_IS_RELATED)).booleanValue()) {
                            return Messages.get().container(Messages.GUI_PUBLISH_RELATED_RESOURCE_0);
                        }
                        return super.getName();
                    }
                };
                resourceProjStateAction.setEnabled(false);
                colDefinition.addDirectAction(resourceProjStateAction);
            }
        }

        CmsListColumnDefinition relatedCol = new CmsListExplorerColumn(LIST_COLUMN_IS_RELATED);
        relatedCol.setName(org.opencms.workplace.explorer.Messages.get().container(
            org.opencms.workplace.explorer.Messages.GUI_INPUT_NAME_0));
        relatedCol.setVisible(false);
        relatedCol.setPrintable(false);
        metadata.addColumn(relatedCol);
    }

    /**
     * @see org.opencms.workplace.list.A_CmsListExplorerDialog#setIndependentActions(org.opencms.workplace.list.CmsListMetadata)
     */
    protected void setIndependentActions(CmsListMetadata metadata) {

        // no LIA
    }

    /**
     * @see org.opencms.workplace.list.A_CmsListDialog#setMultiActions(org.opencms.workplace.list.CmsListMetadata)
     */
    protected void setMultiActions(CmsListMetadata metadata) {

        // no LMAs, and remove default search action
        metadata.setSearchAction(null);
    }
}
