/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/workplace/commons/CmsShowSiblingsList.java,v $
 * Date   : $Date: 2007/05/14 08:07:38 $
 * Version: $Revision: 1.1.2.2 $
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
import org.opencms.file.CmsObject;
import org.opencms.file.CmsResourceFilter;
import org.opencms.jsp.CmsJspActionElement;
import org.opencms.main.CmsException;
import org.opencms.main.CmsRuntimeException;
import org.opencms.util.CmsStringUtil;
import org.opencms.workplace.explorer.CmsResourceUtil;
import org.opencms.workplace.list.A_CmsListExplorerDialog;
import org.opencms.workplace.list.A_CmsListResourceCollector;
import org.opencms.workplace.list.CmsListItem;
import org.opencms.workplace.list.CmsListMetadata;
import org.opencms.workplace.list.I_CmsListResourceCollector;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * List to show siblings of a given resource.<p>
 * 
 * @author Raphael Schnuck
 * 
 * @version $Revision: 1.1.2.2 $ 
 * 
 * @since 6.9.2 
 */
public class CmsShowSiblingsList extends A_CmsListExplorerDialog {

    /** The list id for this class. */
    private static final String LIST_ID = "lshsib";

    /** The resource collector for this class. */
    private I_CmsListResourceCollector m_collector;

    /**
     * Default constructor.<p>
     * 
     * @param jsp an initialized JSP action element
     */
    public CmsShowSiblingsList(CmsJspActionElement jsp) {

        super(jsp, LIST_ID, Messages.get().container(Messages.GUI_SHOW_SIBLINGS_LIST_NAME_0));

        // set the right resource util parameters
        CmsResourceUtil resUtil = getResourceUtil();
        resUtil.setAbbrevLength(50);
        resUtil.setRelativeTo(jsp.getRequestContext().getFolderUri());
        resUtil.setSiteMode(CmsResourceUtil.SITE_MODE_MATCHING);
    }

    /**
     * @see org.opencms.workplace.list.A_CmsListDialog#executeListMultiActions()
     */
    public void executeListMultiActions() throws CmsRuntimeException {

        throwListUnsupportedActionException();
    }

    /**
     * @see org.opencms.workplace.list.A_CmsListDialog#executeListSingleActions()
     */
    public void executeListSingleActions() throws CmsRuntimeException {

        throwListUnsupportedActionException();
    }

    /**
     * @see org.opencms.workplace.list.A_CmsListExplorerDialog#getCollector()
     */
    public I_CmsListResourceCollector getCollector() {

        if (m_collector == null) {
            m_collector = new A_CmsListResourceCollector(this) {

                /** Parameter of the default collector name. */
                private static final String COLLECTOR_NAME = "showSiblings";

                /**
                 * @see org.opencms.file.collectors.I_CmsResourceCollector#getCollectorNames()
                 */
                public List getCollectorNames() {

                    List names = new ArrayList();
                    names.add(COLLECTOR_NAME);
                    return names;
                }

                /**
                 * @see org.opencms.workplace.list.A_CmsListResourceCollector#getResources(org.opencms.file.CmsObject, java.util.Map)
                 */
                public List getResources(CmsObject cms, Map params) throws CmsException {

                    return getCms().readSiblings(
                        ((CmsShowSiblingsList)getWp()).getParamResource(),
                        CmsResourceFilter.ALL);
                }

                /**
                 * @see org.opencms.workplace.list.A_CmsListResourceCollector#setAdditionalColumns(org.opencms.workplace.list.CmsListItem, org.opencms.workplace.explorer.CmsResourceUtil)
                 */
                protected void setAdditionalColumns(CmsListItem item, CmsResourceUtil resUtil) {

                    // noop
                }
            };
        }
        return m_collector;
    }

    /**
     * @see org.opencms.workplace.list.A_CmsListDialog#fillDetails(java.lang.String)
     */
    protected void fillDetails(String detailId) {

        // noop
    }

    /**
     * @see org.opencms.workplace.list.A_CmsListExplorerDialog#setColumns(org.opencms.workplace.list.CmsListMetadata)
     */
    protected void setColumns(CmsListMetadata metadata) {

        super.setColumns(metadata);
        metadata.getColumnDefinition(A_CmsListExplorerDialog.LIST_COLUMN_NAME).setName(
            org.opencms.workplace.explorer.Messages.get().container(
                org.opencms.workplace.explorer.Messages.GUI_INPUT_PATH_0));
    }

    /**
     * @see org.opencms.workplace.list.A_CmsListExplorerDialog#setColumnVisibilities()
     */
    protected void setColumnVisibilities() {

        Map colVisibilities = new HashMap(16);
        // set explorer configurable column visibilities
        colVisibilities.put(new Integer(CmsUserSettings.FILELIST_TITLE), Boolean.TRUE);
        colVisibilities.put(new Integer(CmsUserSettings.FILELIST_TYPE), Boolean.TRUE);
        colVisibilities.put(new Integer(CmsUserSettings.FILELIST_SIZE), Boolean.TRUE);
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

    /**
     * @see org.opencms.workplace.list.A_CmsListDialog#validateParamaters()
     */
    protected void validateParamaters() throws Exception {

        if (CmsStringUtil.isEmptyOrWhitespaceOnly(getParamResource())) {
            throw new Exception();
        }
    }
}
