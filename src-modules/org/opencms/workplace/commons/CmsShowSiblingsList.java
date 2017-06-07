/*
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (c) Alkacon Software GmbH & Co. KG (http://www.alkacon.com)
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
 * For further information about Alkacon Software GmbH & Co. KG, please see the
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
import org.opencms.file.CmsResource;
import org.opencms.file.CmsResourceFilter;
import org.opencms.jsp.CmsJspActionElement;
import org.opencms.main.CmsException;
import org.opencms.main.CmsRuntimeException;
import org.opencms.main.OpenCms;
import org.opencms.util.CmsStringUtil;
import org.opencms.workplace.explorer.CmsResourceUtil;
import org.opencms.workplace.list.A_CmsListExplorerDialog;
import org.opencms.workplace.list.A_CmsListResourceCollector;
import org.opencms.workplace.list.CmsListExplorerColumn;
import org.opencms.workplace.list.CmsListItem;
import org.opencms.workplace.list.CmsListMetadata;
import org.opencms.workplace.list.I_CmsListResourceCollector;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * List to show siblings of a given resource.<p>
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
        resUtil.setRelativeTo(getCms().getRequestContext().addSiteRoot(jsp.getRequestContext().getFolderUri()));
        resUtil.setSiteMode(CmsResourceUtil.SITE_MODE_MATCHING);
    }

    /**
     * @see org.opencms.workplace.list.A_CmsListDialog#executeListMultiActions()
     */
    @Override
    public void executeListMultiActions() throws CmsRuntimeException {

        throwListUnsupportedActionException();
    }

    /**
     * @see org.opencms.workplace.list.A_CmsListDialog#executeListSingleActions()
     */
    @Override
    public void executeListSingleActions() throws CmsRuntimeException {

        throwListUnsupportedActionException();
    }

    /**
     * @see org.opencms.workplace.list.A_CmsListExplorerDialog#getCollector()
     */
    @Override
    public I_CmsListResourceCollector getCollector() {

        if (m_collector == null) {
            m_collector = new A_CmsListResourceCollector(this) {

                /** Parameter of the default collector name. */
                private static final String COLLECTOR_NAME = "showSiblings";

                /**
                 * @see org.opencms.file.collectors.I_CmsResourceCollector#getCollectorNames()
                 */
                @Override
                public List<String> getCollectorNames() {

                    return Collections.unmodifiableList(Arrays.asList(new String[] {COLLECTOR_NAME}));
                }

                /**
                 * @see org.opencms.workplace.list.A_CmsListResourceCollector#getResources(org.opencms.file.CmsObject, java.util.Map)
                 */
                @Override
                public List<CmsResource> getResources(CmsObject cms, Map<String, String> params) throws CmsException {

                    return getCms().readSiblings(
                        ((CmsShowSiblingsList)getWp()).getParamResource(),
                        CmsResourceFilter.ALL);
                }

                /**
                 * @see org.opencms.workplace.list.A_CmsListResourceCollector#setAdditionalColumns(org.opencms.workplace.list.CmsListItem, org.opencms.workplace.explorer.CmsResourceUtil)
                 */
                @Override
                protected void setAdditionalColumns(CmsListItem item, CmsResourceUtil resUtil) {

                    // empty
                }
            };
        }
        return m_collector;
    }

    /**
     * @see org.opencms.workplace.list.A_CmsListDialog#defaultActionHtmlStart()
     */
    @Override
    protected String defaultActionHtmlStart() {

        return getList().listJs() + CmsListExplorerColumn.getExplorerStyleDef() + dialogContentStart(getParamTitle());
    }

    /**
     * @see org.opencms.workplace.list.A_CmsListDialog#fillDetails(java.lang.String)
     */
    @Override
    protected void fillDetails(String detailId) {

        // empty
    }

    /**
     * @see org.opencms.workplace.list.A_CmsListExplorerDialog#setColumns(org.opencms.workplace.list.CmsListMetadata)
     */
    @Override
    protected void setColumns(CmsListMetadata metadata) {

        super.setColumns(metadata);
        metadata.getColumnDefinition(A_CmsListExplorerDialog.LIST_COLUMN_NAME).setName(
            org.opencms.workplace.explorer.Messages.get().container(
                org.opencms.workplace.explorer.Messages.GUI_INPUT_PATH_0));
    }

    /**
     * @see org.opencms.workplace.list.A_CmsListExplorerDialog#isColumnVisible(int)
     */
    @Override
    protected boolean isColumnVisible(int colFlag) {

        boolean isVisible = (colFlag == CmsUserSettings.FILELIST_TITLE);
        isVisible = isVisible || (colFlag == LIST_COLUMN_TYPEICON.hashCode());
        isVisible = isVisible || (colFlag == LIST_COLUMN_LOCKICON.hashCode());
        isVisible = isVisible || (colFlag == LIST_COLUMN_PROJSTATEICON.hashCode());
        isVisible = isVisible || (colFlag == LIST_COLUMN_NAME.hashCode());
        isVisible = isVisible || (colFlag == CmsUserSettings.FILELIST_TYPE);
        isVisible = isVisible || (colFlag == CmsUserSettings.FILELIST_SIZE);
        isVisible = isVisible
            || ((colFlag == LIST_COLUMN_SITE.hashCode()) && (OpenCms.getSiteManager().getSites().size() > 1));
        return isVisible;
    }

    /**
     * @see org.opencms.workplace.list.A_CmsListDialog#setMultiActions(org.opencms.workplace.list.CmsListMetadata)
     */
    @Override
    protected void setMultiActions(CmsListMetadata metadata) {

        // empty
    }

    /**
     * @see org.opencms.workplace.list.A_CmsListDialog#validateParamaters()
     */
    @Override
    protected void validateParamaters() throws Exception {

        if (CmsStringUtil.isEmptyOrWhitespaceOnly(getParamResource())) {
            throw new Exception();
        }
    }
}
