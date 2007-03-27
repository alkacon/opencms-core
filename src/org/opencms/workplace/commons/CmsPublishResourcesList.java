/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/workplace/commons/CmsPublishResourcesList.java,v $
 * Date   : $Date: 2007/03/27 14:16:25 $
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

import org.opencms.db.CmsPublishList;
import org.opencms.db.CmsUserSettings;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsResource;
import org.opencms.i18n.CmsMessageContainer;
import org.opencms.jsp.CmsJspActionElement;
import org.opencms.main.CmsException;
import org.opencms.main.CmsLog;
import org.opencms.main.OpenCms;
import org.opencms.site.CmsSiteManager;
import org.opencms.util.CmsStringUtil;
import org.opencms.workplace.CmsWorkplace;
import org.opencms.workplace.explorer.CmsResourceUtil;
import org.opencms.workplace.list.A_CmsListDialog;
import org.opencms.workplace.list.A_CmsListExplorerDialog;
import org.opencms.workplace.list.A_CmsListIndependentJsAction;
import org.opencms.workplace.list.A_CmsListResourceCollector;
import org.opencms.workplace.list.CmsListColumnDefinition;
import org.opencms.workplace.list.CmsListDirectAction;
import org.opencms.workplace.list.CmsListExplorerColumn;
import org.opencms.workplace.list.CmsListIndependentAction;
import org.opencms.workplace.list.CmsListItem;
import org.opencms.workplace.list.CmsListItemDetails;
import org.opencms.workplace.list.CmsListItemDetailsFormatter;
import org.opencms.workplace.list.CmsListMetadata;
import org.opencms.workplace.list.CmsListResourceProjStateAction;
import org.opencms.workplace.list.I_CmsListAction;
import org.opencms.workplace.list.I_CmsListResourceCollector;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;

/**
 * List for resources that can be published.<p>
 * 
 * @author Michael Moossen  
 * 
 * @version $Revision: 1.1.2.4 $ 
 * 
 * @since 6.5.5 
 */
public class CmsPublishResourcesList extends A_CmsListExplorerDialog {

    /** list action id constant. */
    public static final String LIST_DETAIL_RELATIONS = "dr";

    /** list action id constant. */
    public static final String LIST_DETAIL_RELATIONS_HIDE = "drh";

    /** list action id constant. */
    public static final String LIST_DETAIL_RELATIONS_SHOW = "drs";

    /** list id constant. */
    public static final String LIST_ID = "pr";

    /** List column id constant. */
    protected static final String LIST_COLUMN_IS_RELATED = "ecir";

    /** The log object for this class. */
    protected static final Log LOG = CmsLog.getLog(CmsPublishResourcesList.class);

    /** This constant is just a hack to mark related resources in the list. */
    private static final int FLAG_RELATED_RESOURCE = 8192;

    /** The internal collector instance. */
    private I_CmsListResourceCollector m_collector;

    /** The count of all related resources. */
    private int m_relatedResources;

    /**
     * Public constructor.<p>
     * 
     * @param jsp an initialized JSP action element
     * @param relativeTo the 'relative to' path, this only affects the generation of the path for the resource
     */
    public CmsPublishResourcesList(CmsJspActionElement jsp, String relativeTo) {

        super(jsp, LIST_ID, Messages.get().container(Messages.GUI_PUBLISH_RESOURCES_LIST_NAME_0));

        // prevent paging
        getList().setMaxItemsPerPage(Integer.MAX_VALUE);

        // set the right resource util parameters
        CmsResourceUtil resUtil = getResourceUtil();
        resUtil.setAbbrevLength(50);
        resUtil.setRelativeTo(relativeTo);
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
     * @see org.opencms.workplace.list.A_CmsListExplorerDialog#getCollector()
     */
    public I_CmsListResourceCollector getCollector() {

        if (m_collector == null) {
            m_collector = new A_CmsListResourceCollector(this) {

                /** Parameter of the default collector name. */
                private static final String COLLECTOR_NAME = "publishResources";

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
                public List getResources(CmsObject cms, Map params) {

                    List allResources = new ArrayList();
                    allResources.addAll(getSettings().getPublishList().getFileList());
                    allResources.addAll(getSettings().getPublishList().getDeletedFolderList());
                    allResources.addAll(getSettings().getPublishList().getFolderList());
                    if (getSettings().getPublishList().isPublishRelatedResources()) {
                        try {
                            CmsPublishList pubList = OpenCms.getPublishManager().getRelatedResourcesToPublish(
                                cms,
                                getSettings().getPublishList(),
                                null);
                            List relatedResources = new ArrayList(pubList.getFileList());
                            relatedResources.addAll(pubList.getDeletedFolderList());
                            relatedResources.addAll(pubList.getFolderList());
                            Iterator it = relatedResources.iterator();
                            while (it.hasNext()) {
                                CmsResource resource = (CmsResource)it.next();
                                CmsResource modRes = new CmsResource(
                                    resource.getStructureId(),
                                    resource.getResourceId(),
                                    resource.getRootPath(),
                                    resource.getTypeId(),
                                    resource.isFolder(),
                                    resource.getFlags() | FLAG_RELATED_RESOURCE,
                                    resource.getProjectLastModified(),
                                    resource.getState(),
                                    resource.getDateCreated(),
                                    resource.getUserCreated(),
                                    resource.getDateLastModified(),
                                    resource.getUserLastModified(),
                                    resource.getDateReleased(),
                                    resource.getDateExpired(),
                                    resource.getSiblingCount(),
                                    resource.getLength());
                                allResources.add(modRes);
                            }
                        } catch (CmsException e) {
                            if (LOG.isErrorEnabled()) {
                                LOG.error(e.getLocalizedMessage(getLocale()), e);
                            }
                        }
                    }
                    return allResources;
                }

                /**
                 * @see org.opencms.workplace.list.A_CmsListResourceCollector#setAdditionalColumns(org.opencms.workplace.list.CmsListItem, org.opencms.workplace.explorer.CmsResourceUtil)
                 */
                protected void setAdditionalColumns(CmsListItem item, CmsResourceUtil resUtil) {

                    item.set(LIST_COLUMN_IS_RELATED, new Boolean(
                        (resUtil.getResource().getFlags() & FLAG_RELATED_RESOURCE) == FLAG_RELATED_RESOURCE));
                }
            };
        }
        return m_collector;
    }

    /**
     * Returns the count of all related resources.<p>
     *
     * @return the count of all related resources
     */
    public int getRelatedResources() {

        return m_relatedResources;
    }

    /**
     * @see org.opencms.workplace.list.A_CmsListDialog#fillDetails(java.lang.String)
     */
    protected void fillDetails(String detailId) {

        // get content
        List resourceNames = getList().getAllContent();
        Iterator itResourceNames = resourceNames.iterator();
        while (itResourceNames.hasNext()) {
            CmsListItem item = (CmsListItem)itResourceNames.next();
            StringBuffer html = new StringBuffer(128);
            try {
                if (detailId.equals(LIST_DETAIL_RELATIONS)) {
                    CmsResource resource = getResourceUtil(item).getResource();
                    // relations
                    Iterator itRelations = OpenCms.getPublishManager().getRelatedResourcesToPublish(
                        getCms(),
                        getSettings().getPublishList(),
                        resource).getFileList().iterator();

                    // show all unpublished related resources
                    while (itRelations.hasNext()) {
                        CmsResource relatedRes = (CmsResource)itRelations.next();
                        String relationName = relatedRes.getRootPath();
                        if (relationName.startsWith(getCms().getRequestContext().getSiteRoot())) {
                            // same site
                            relationName = getCms().getSitePath(relatedRes);
                        } else {
                            // other site
                            String site = CmsSiteManager.getSiteRoot(relationName);
                            String siteName = site;
                            if (site != null) {
                                relationName = relationName.substring(site.length());
                                siteName = CmsSiteManager.getSite(site).getTitle();
                            } else {
                                siteName = "/";
                            }
                            relationName = key(Messages.GUI_DELETE_SITE_RELATION_2, new Object[] {
                                siteName,
                                relationName});
                        }
                        relationName = CmsStringUtil.formatResourceName(relationName, 50);
                        m_relatedResources++;
                        html.append(relationName);
                        if (itRelations.hasNext()) {
                            html.append("<br>");
                        }
                        html.append("\n");
                    }
                } else {
                    continue;
                }
            } catch (CmsException e) {
                // should never happen, log exception
                item.set(detailId, CmsException.getFormattedErrorstack(e));
            }
            item.set(detailId, html.toString());
        }
    }

    /**
     * @see org.opencms.workplace.list.A_CmsListExplorerDialog#isColumnVisible(int)
     */
    protected boolean isColumnVisible(int colFlag) {

        boolean isVisible = (colFlag == CmsUserSettings.FILELIST_TITLE);
        isVisible = isVisible || (colFlag == LIST_COLUMN_TYPEICON.hashCode());
        isVisible = isVisible || (colFlag == LIST_COLUMN_LOCKICON.hashCode());
        isVisible = isVisible || (colFlag == LIST_COLUMN_PROJSTATEICON.hashCode());
        isVisible = isVisible || (colFlag == LIST_COLUMN_NAME.hashCode());
        isVisible = isVisible || (colFlag == LIST_COLUMN_SITE.hashCode());
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
     * @see org.opencms.workplace.list.A_CmsListDialog#setIndependentActions(org.opencms.workplace.list.CmsListMetadata)
     */
    protected void setIndependentActions(CmsListMetadata metadata) {

        /**
         * Class to render a javascript driven detail action button.<p>
         */
        abstract class DetailsJsAction extends A_CmsListIndependentJsAction {

            /**
             * Default constructor.<p>
             * 
             * @param id the action id
             */
            public DetailsJsAction(String id) {

                super(id);
            }

            /**
             * @see org.opencms.workplace.list.CmsListIndependentAction#buttonHtml(org.opencms.workplace.CmsWorkplace)
             */
            public String buttonHtml(CmsWorkplace wp) {

                StringBuffer html = new StringBuffer(1024);
                html.append("\t<span id='");
                html.append(getId());
                html.append("' class=\"link");
                html.append("\"");
                html.append(" onClick=\"");
                html.append(resolveOnClic(wp));
                html.append("\"");
                html.append(">");
                html.append("<img src='");
                html.append(CmsWorkplace.getSkinUri());
                html.append(getIconPath());
                html.append("'");
                html.append(" alt='");
                html.append(getName().key(wp.getLocale()));
                html.append("'");
                html.append(" title='");
                html.append(getName().key(wp.getLocale()));
                html.append("'");
                html.append(">");
                html.append("&nbsp;");
                html.append("<a href='#'>");
                html.append(getName().key(wp.getLocale()));
                html.append("</a>");
                html.append("</span>");
                return html.toString();
            }
        }

        I_CmsListAction hideAction = new DetailsJsAction(LIST_DETAIL_RELATIONS_HIDE) {

            /**
             * @see org.opencms.workplace.list.A_CmsListIndependentJsAction#jsCode(CmsWorkplace)
             */
            public String jsCode(CmsWorkplace wp) {

                return "javascript:showRelatedResources(false);";
            }
        };
        hideAction.setIconPath(A_CmsListDialog.ICON_DETAILS_HIDE);
        hideAction.setName(Messages.get().container(Messages.GUI_PUBLISH_RELATED_RESOURCES_DETAIL_HIDE_NAME_0));
        hideAction.setHelpText(Messages.get().container(Messages.GUI_PUBLISH_RELATED_RESOURCES_DETAIL_HIDE_HELP_0));
        metadata.addIndependentAction(hideAction);

        I_CmsListAction showAction = new DetailsJsAction(LIST_DETAIL_RELATIONS_SHOW) {

            /**
             * @see org.opencms.workplace.list.A_CmsListIndependentJsAction#jsCode(CmsWorkplace)
             */
            public String jsCode(CmsWorkplace wp) {

                return "javascript:showRelatedResources(true);";
            }
        };
        showAction.setIconPath(A_CmsListDialog.ICON_DETAILS_SHOW);
        showAction.setName(Messages.get().container(Messages.GUI_PUBLISH_RELATED_RESOURCES_DETAIL_SHOW_NAME_0));
        showAction.setHelpText(Messages.get().container(Messages.GUI_PUBLISH_RELATED_RESOURCES_DETAIL_SHOW_HELP_0));
        metadata.addIndependentAction(showAction);

        // create list item detail
        CmsListItemDetails relationsDetails = new CmsListItemDetails(LIST_DETAIL_RELATIONS) {

            /**
             * @see org.opencms.workplace.list.CmsListItemDetails#getAction()
             */
            public I_CmsListAction getAction() {

                return new CmsListIndependentAction("hide") {

                    /**
                     * @see org.opencms.workplace.list.CmsListIndependentAction#buttonHtml(org.opencms.workplace.CmsWorkplace)
                     */
                    public String buttonHtml(CmsWorkplace wp) {

                        return "";
                    }
                };
            }
        };
        relationsDetails.setAtColumn(LIST_COLUMN_NAME);
        relationsDetails.setVisible(true);
        relationsDetails.setFormatter(new CmsListItemDetailsFormatter(Messages.get().container(
            Messages.GUI_PUBLISH_RELATED_RESOURCES_LABEL_0)));
        relationsDetails.setShowActionName(Messages.get().container(
            Messages.GUI_PUBLISH_RELATED_RESOURCES_DETAIL_SHOW_NAME_0));
        relationsDetails.setShowActionHelpText(Messages.get().container(
            Messages.GUI_PUBLISH_RELATED_RESOURCES_DETAIL_SHOW_HELP_0));
        relationsDetails.setHideActionName(Messages.get().container(
            Messages.GUI_PUBLISH_RELATED_RESOURCES_DETAIL_HIDE_NAME_0));
        relationsDetails.setHideActionHelpText(Messages.get().container(
            Messages.GUI_PUBLISH_RELATED_RESOURCES_DETAIL_HIDE_HELP_0));

        // add resources info item detail to meta data
        metadata.addItemDetails(relationsDetails);
    }

    /**
     * @see org.opencms.workplace.list.A_CmsListDialog#setMultiActions(org.opencms.workplace.list.CmsListMetadata)
     */
    protected void setMultiActions(CmsListMetadata metadata) {

        // no LMAs, and remove default search action
        metadata.setSearchAction(null);
    }
}
