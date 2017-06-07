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

package org.opencms.workplace.comparison;

import org.opencms.file.CmsFile;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsProject;
import org.opencms.file.CmsResource;
import org.opencms.file.CmsResourceFilter;
import org.opencms.file.history.CmsHistoryFile;
import org.opencms.file.history.CmsHistoryResourceHandler;
import org.opencms.file.types.CmsResourceTypePlain;
import org.opencms.jsp.CmsJspActionElement;
import org.opencms.loader.CmsLoaderException;
import org.opencms.main.CmsException;
import org.opencms.main.OpenCms;
import org.opencms.util.CmsStringUtil;
import org.opencms.util.CmsUUID;
import org.opencms.workplace.CmsWorkplace;
import org.opencms.workplace.commons.CmsHistoryList;
import org.opencms.workplace.list.CmsListIndependentAction;
import org.opencms.workplace.list.CmsListItem;
import org.opencms.workplace.list.CmsListItemDetails;
import org.opencms.workplace.list.CmsListMetadata;
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
 * List for property comparison including columns for property name and the values. <p>
 *
 * @since 6.0.0
 */
public class CmsAttributeComparisonList extends CmsPropertyComparisonList {

    /** List id constant. */
    public static final String AC_LIST_ID = "hiacl";

    /**
     * Public constructor.<p>
     *
     * @param jsp an initialized JSP action element
     */
    public CmsAttributeComparisonList(CmsJspActionElement jsp) {

        this(AC_LIST_ID, jsp);
    }

    /**
     * Public constructor with JSP variables.<p>
     *
     * @param context the JSP page context
     * @param req the JSP request
     * @param res the JSP response
     */
    public CmsAttributeComparisonList(PageContext context, HttpServletRequest req, HttpServletResponse res) {

        this(new CmsJspActionElement(context, req, res));
    }

    /**
     * Protected constructor.<p>
     *
     * @param listId the id of the specialized list
     * @param jsp an initialized JSP action element
     */
    protected CmsAttributeComparisonList(String listId, CmsJspActionElement jsp) {

        super(
            jsp,
            listId,
            Messages.get().container(Messages.GUI_COMPARE_ATTRIBUTES_0),
            LIST_COLUMN_PROPERTY_NAME,
            CmsListOrderEnum.ORDER_ASCENDING,
            null);
    }

    /**
     * Returns either the historical file or the offline file, depending on the version number.<p>
     *
     * @param cms the CmsObject to use
     * @param structureId the structure id of the file
     * @param version the historical version number
     *
     * @return either the historical file or the offline file, depending on the version number
     *
     * @throws CmsException if something goes wrong
     */
    protected static CmsFile readFile(CmsObject cms, CmsUUID structureId, String version) throws CmsException {

        if (Integer.parseInt(version) == CmsHistoryResourceHandler.PROJECT_OFFLINE_VERSION) {
            // offline
            CmsResource resource = cms.readResource(structureId, CmsResourceFilter.IGNORE_EXPIRATION);
            return cms.readFile(resource);
        } else {
            int ver = Integer.parseInt(version);
            if (ver < 0) {
                // online
                CmsProject project = cms.getRequestContext().getCurrentProject();
                try {
                    cms.getRequestContext().setCurrentProject(cms.readProject(CmsProject.ONLINE_PROJECT_ID));
                    CmsResource resource = cms.readResource(structureId, CmsResourceFilter.IGNORE_EXPIRATION);
                    return cms.readFile(resource);
                } finally {
                    cms.getRequestContext().setCurrentProject(project);
                }
            }
            // backup
            return cms.readFile((CmsHistoryFile)cms.readResource(structureId, ver));
        }
    }

    /**
     * @see org.opencms.workplace.list.A_CmsListDialog#executeListSingleActions()
     */
    @Override
    public void executeListSingleActions() throws IOException, ServletException {

        // forward to the edit module screen
        Map<String, String[]> params = new HashMap<String, String[]>();
        params.put(CmsHistoryList.PARAM_VERSION_1, new String[] {getParamVersion1()});
        params.put(CmsHistoryList.PARAM_VERSION_2, new String[] {getParamVersion2()});
        params.put(CmsHistoryList.PARAM_ID_1, new String[] {getParamId1()});
        params.put(CmsHistoryList.PARAM_ID_2, new String[] {getParamId2()});
        params.put(PARAM_COMPARE, new String[] {CmsResourceComparisonDialog.COMPARE_ATTRIBUTES});
        params.put(PARAM_RESOURCE, new String[] {getParamResource()});
        // forward to the difference screen
        getToolManager().jspForwardTool(this, "/history/comparison/difference", params);
    }

    /**
     * @see org.opencms.workplace.list.A_CmsListDialog#getListItems()
     */
    @Override
    protected List<CmsListItem> getListItems() {

        List<CmsListItem> ret = new ArrayList<CmsListItem>();
        Iterator<?> diffs = CmsResourceComparison.compareAttributes(
            getCms(),
            getResource1(),
            getResource2()).iterator();
        while (diffs.hasNext()) {
            CmsAttributeComparison comparison = (CmsAttributeComparison)diffs.next();
            CmsListItem item = getList().newItem(comparison.getName());
            item.set(LIST_COLUMN_PROPERTY_NAME, key(comparison.getName()));
            item.set(LIST_COLUMN_VERSION_1, CmsStringUtil.trimToSize(comparison.getVersion1(), TRIM_AT_LENGTH));
            item.set(LIST_COLUMN_VERSION_2, CmsStringUtil.trimToSize(comparison.getVersion2(), TRIM_AT_LENGTH));
            if (CmsResourceComparison.TYPE_ADDED.equals(comparison.getStatus())) {
                item.set(LIST_COLUMN_TYPE, key(Messages.GUI_COMPARE_ADDED_0));
            } else if (CmsResourceComparison.TYPE_REMOVED.equals(comparison.getStatus())) {
                item.set(LIST_COLUMN_TYPE, key(Messages.GUI_COMPARE_REMOVED_0));
            } else if (CmsResourceComparison.TYPE_CHANGED.equals(comparison.getStatus())) {
                item.set(LIST_COLUMN_TYPE, key(Messages.GUI_COMPARE_CHANGED_0));
            } else {
                if (!getList().getMetadata().getItemDetailDefinition(LIST_IACTION_SHOW).isVisible()) {
                    // do not display entry
                    continue;
                } else {
                    item.set(LIST_COLUMN_TYPE, key(Messages.GUI_COMPARE_UNCHANGED_0));
                }
            }
            ret.add(item);

            if (!diffs.hasNext()) {
                getList().getMetadata().getIndependentAction(LIST_ACTION_VIEW1).setEnabled(getResource1().isFile());
                getList().getMetadata().getIndependentAction(LIST_ACTION_VIEW2).setEnabled(getResource2().isFile());
            }
        }
        getList().getMetadata().getColumnDefinition(LIST_COLUMN_VERSION_1).setName(
            Messages.get().container(
                Messages.GUI_COMPARE_VERSION_1,
                CmsHistoryListUtil.getDisplayVersion(getParamVersion1(), getLocale())));
        getList().getMetadata().getColumnDefinition(LIST_COLUMN_VERSION_2).setName(Messages.get().container(
            Messages.GUI_COMPARE_VERSION_1,
            CmsHistoryListUtil.getDisplayVersion(getParamVersion2(), getLocale())));

        return ret;
    }

    /**
     * Returns the html code to display a file version.<p>
     *
     * @param structureId the structure id of the file to be displayed
     * @param version the version of the file to be displayed
     *
     * @return the html code to display a file version
     */
    protected String getViewVersionButtonHtml(CmsUUID structureId, String version) {

        // set flag to activate the preview button of the comparison dialog
        boolean active = true;
        try {
            // only show preview, if the resource is a file
            // or the version has content
            if (OpenCms.getResourceManager().getResourceType(getResource1().getTypeId()).isFolder()) {
                active = false;
            } else {
                byte[] content = readFile(getCms(), structureId, version).getContents();
                if (content.length < 1) {
                    active = false;
                }
            }
        } catch (CmsLoaderException e) {
            // ignore, buttons will be shown

        } catch (CmsException e) {
            // do not show the preview button
            active = false;
        }

        // return button only if file resource
        if (active) {
            String label = Messages.get().container(
                Messages.GUI_COMPARE_VIEW_VERSION_1,
                CmsHistoryListUtil.getDisplayVersion(version, getLocale())).key(getLocale());
            String iconPath = null;
            try {
                String typeName = OpenCms.getResourceManager().getResourceType(
                    getResource1().getTypeId()).getTypeName();
                iconPath = CmsWorkplace.RES_PATH_FILETYPES
                    + OpenCms.getWorkplaceManager().getExplorerTypeSetting(typeName).getIcon();
            } catch (CmsException e) {
                iconPath = CmsWorkplace.RES_PATH_FILETYPES
                    + OpenCms.getWorkplaceManager().getExplorerTypeSetting(
                        CmsResourceTypePlain.getStaticTypeName()).getIcon();
            }
            StringBuffer result = new StringBuffer(1024);
            result.append("<span class='link' onClick=\"");
            result.append("window.open('");
            result.append(getJsp().link(CmsHistoryListUtil.getHistoryLink(getCms(), structureId, version)));
            result.append("','version','scrollbars=yes', 'resizable=yes', 'width=800', 'height=600')\">");
            result.append("<img style='width: 16px; height: 16px;' src='");
            result.append(CmsWorkplace.getSkinUri());
            result.append(iconPath);
            result.append("' alt='");
            result.append(label);
            result.append("' title='");
            result.append(label);
            result.append("'>&nbsp;<a href='#'>");
            result.append(label);
            result.append("</a></span>");

            return result.toString();
        }
        return "";
    }

    /**
     * @see org.opencms.workplace.list.A_CmsListDialog#setIndependentActions(org.opencms.workplace.list.CmsListMetadata)
     */
    @Override
    protected void setIndependentActions(CmsListMetadata metadata) {

        // add the view version action
        CmsListIndependentAction viewVersion1 = new CmsListIndependentAction(LIST_ACTION_VIEW1) {

            /**
             * @see org.opencms.workplace.tools.I_CmsHtmlIconButton#buttonHtml(org.opencms.workplace.CmsWorkplace)
             */
            @Override
            public String buttonHtml(CmsWorkplace wp) {

                return ((CmsAttributeComparisonList)wp).getViewVersionButtonHtml(
                    new CmsUUID(((CmsAttributeComparisonList)wp).getParamId1()),
                    ((CmsAttributeComparisonList)wp).getParamVersion1());
            }
        };
        metadata.addIndependentAction(viewVersion1);
        // add the view version action
        CmsListIndependentAction viewVersion2 = new CmsListIndependentAction(LIST_ACTION_VIEW2) {

            /**
             * @see org.opencms.workplace.tools.I_CmsHtmlIconButton#buttonHtml(org.opencms.workplace.CmsWorkplace)
             */
            @Override
            public String buttonHtml(CmsWorkplace wp) {

                return ((CmsAttributeComparisonList)wp).getViewVersionButtonHtml(
                    new CmsUUID(((CmsAttributeComparisonList)wp).getParamId2()),
                    ((CmsAttributeComparisonList)wp).getParamVersion2());
            }
        };
        metadata.addIndependentAction(viewVersion2);

        // add event details
        CmsListItemDetails eventDetails = new CmsListItemDetails(LIST_IACTION_SHOW);
        eventDetails.setVisible(false);
        eventDetails.setShowActionName(Messages.get().container(Messages.GUI_COMPARE_SHOW_ALL_ATTRIBUTES_0));
        eventDetails.setHideActionName(Messages.get().container(Messages.GUI_COMPARE_HIDE_IDENTICAL_ATTRIBUTES_0));
        metadata.addItemDetails(eventDetails);
    }
}
