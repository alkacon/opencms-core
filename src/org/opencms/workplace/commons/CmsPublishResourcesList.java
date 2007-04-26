/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/workplace/commons/CmsPublishResourcesList.java,v $
 * Date   : $Date: 2007/04/26 14:31:14 $
 * Version: $Revision: 1.1.2.8 $
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
import org.opencms.file.CmsResourceFilter;
import org.opencms.i18n.CmsMessageContainer;
import org.opencms.jsp.CmsJspActionElement;
import org.opencms.main.CmsException;
import org.opencms.main.CmsLog;
import org.opencms.main.OpenCms;
import org.opencms.relations.CmsRelation;
import org.opencms.relations.CmsRelationFilter;
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
import org.opencms.workplace.list.CmsListMetadata;
import org.opencms.workplace.list.CmsListResourceProjStateAction;
import org.opencms.workplace.list.I_CmsListAction;
import org.opencms.workplace.list.I_CmsListFormatter;
import org.opencms.workplace.list.I_CmsListResourceCollector;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.zip.DataFormatException;

import org.apache.commons.logging.Log;

/**
 * List for resources that can be published.<p>
 * 
 * @author Michael Moossen  
 * 
 * @version $Revision: 1.1.2.8 $ 
 * 
 * @since 6.5.5 
 */
public class CmsPublishResourcesList extends A_CmsListExplorerDialog {

    /** This constant is just a hack to mark related resources in the list. */
    public static final int FLAG_RELATED_RESOURCE = 8192;

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

    /** Indicates if the related resources should be included. */
    protected boolean m_publishRelated;

    /** The internal collector instance. */
    private I_CmsListResourceCollector m_collector;

    /**
     * Public constructor.<p>
     * 
     * @param jsp an initialized JSP action element
     * @param relativeTo the 'relative to' path, this only affects the generation of the path for the resource
     * @param publishRelated indicates if the related resources should be included
     */
    public CmsPublishResourcesList(CmsJspActionElement jsp, String relativeTo, boolean publishRelated) {

        super(jsp, LIST_ID, Messages.get().container(Messages.GUI_PUBLISH_RESOURCES_LIST_NAME_0));

        // prevent paging
        getList().setMaxItemsPerPage(Integer.MAX_VALUE);

        // set the right resource util parameters
        CmsResourceUtil resUtil = getResourceUtil();
        resUtil.setAbbrevLength(50);
        resUtil.setRelativeTo(relativeTo);
        resUtil.setSiteMode(CmsResourceUtil.SITE_MODE_MATCHING);

        m_publishRelated = publishRelated;
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
                    if (m_publishRelated) {
                        try {
                            CmsPublishList pubList = OpenCms.getPublishManager().getRelatedResourcesToPublish(
                                cms,
                                getSettings().getPublishList());
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
                                    resource.getLength(),
                                    resource.getDateContent(),
                                    resource.getVersion());
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
     * @see org.opencms.workplace.list.A_CmsListDialog#fillDetails(java.lang.String)
     */
    protected void fillDetails(String detailId) {

        List publishResources = new ArrayList(getSettings().getPublishList().getDeletedFolderList());
        publishResources.addAll(getSettings().getPublishList().getFileList());
        publishResources.addAll(getSettings().getPublishList().getFolderList());

        // get content
        List resourceNames = getList().getAllContent();
        Iterator itResourceNames = resourceNames.iterator();
        while (itResourceNames.hasNext()) {
            CmsListItem item = (CmsListItem)itResourceNames.next();
            try {
                if (detailId.equals(LIST_DETAIL_RELATIONS)) {
                    List relatedResources = new ArrayList();
                    CmsResource resource = getResourceUtil(item).getResource();

                    // get and iterate over all related resources
                    Iterator itRelations = getCms().getRelationsForResource(
                        getCms().getSitePath(resource),
                        CmsRelationFilter.TARGETS.filterStrong()).iterator();
                    while (itRelations.hasNext()) {
                        CmsRelation relation = (CmsRelation)itRelations.next();
                        CmsResource target = relation.getTarget(getCms(), CmsResourceFilter.ALL);
                        // just add resources that may come in question
                        if (!publishResources.contains(target) && !target.getState().isUnchanged()) {
                            String relationName = target.getRootPath();
                            if (relationName.startsWith(getCms().getRequestContext().getSiteRoot())) {
                                // same site
                                relationName = getCms().getSitePath(target);
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
                            if (!getCms().getLock(target).isLockableBy(getCms().getRequestContext().currentUser())) {
                                // mark not lockable resources
                                relationName = relationName + "*";
                            } else if (m_publishRelated) {
                                // mark related resources to be published
                                relationName = relationName + "!";
                            }
                            if (!resourceNames.contains(relationName)) {
                                relatedResources.add(relationName);
                            }
                        }
                    }
                    if ((resource.getFlags() & FLAG_RELATED_RESOURCE) > 0) {
                        // mark the reverse references
                        itRelations = getCms().getRelationsForResource(
                            getCms().getSitePath(resource),
                            CmsRelationFilter.SOURCES.filterStrong()).iterator();
                        while (itRelations.hasNext()) {
                            CmsRelation relation = (CmsRelation)itRelations.next();
                            CmsResource source = relation.getSource(getCms(), CmsResourceFilter.ALL);
                            // just add resources that may come in question
                            if (publishResources.contains(source)) {
                                String relationName = source.getRootPath();
                                if (relationName.startsWith(getCms().getRequestContext().getSiteRoot())) {
                                    // same site
                                    relationName = getCms().getSitePath(source);
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
                                // mark as reverse reference
                                relationName = relationName + "$";
                                if (!resourceNames.contains(relationName)) {
                                    relatedResources.add(relationName);
                                }
                            }
                        }
                    }
                    if (relatedResources.isEmpty()) {
                        relatedResources = null; // prevent empty row
                    }
                    item.set(detailId, relatedResources);
                } else {
                    continue;
                }
            } catch (CmsException e) {
                // should never happen, log exception
                item.set(detailId, CmsException.getFormattedErrorstack(e));
            }
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
        relationsDetails.setFormatter(new I_CmsListFormatter() {

            public String format(Object data, Locale locale) {

                if (!(data instanceof List)) {
                    return new DataFormatException().getLocalizedMessage();
                }
                StringBuffer html = new StringBuffer(512);
                Iterator itResourceNames = ((List)data).iterator();
                if (itResourceNames.hasNext()) {
                    html.append("<table border='0' cellspacing='0' cellpadding='0'>\n");
                }
                while (itResourceNames.hasNext()) {
                    String resName = (String)itResourceNames.next();
                    html.append("\t<tr>\n");
                    html.append("\t\t<td width='150' align='right' class='listdetailhead'>\n");
                    html.append("\t\t\t");
                    if (resName.endsWith("*")) {
                        // resource is not lockable, and will not be published
                        resName = resName.substring(0, resName.length() - 1);
                        html.append("<font color='red' />");
                        html.append(Messages.get().getBundle(locale).key(
                            Messages.GUI_PUBLISH_DETAIL_RELATED_LOCKED_RESOURCE_0));
                        html.append("</font/>");
                    } else if (resName.endsWith("!")) {
                        // resource will be published
                        resName = resName.substring(0, resName.length() - 1);
                        html.append(Messages.get().getBundle(locale).key(Messages.GUI_PUBLISH_DETAIL_RELATED_RESOURCE_0));
                    } else if (resName.endsWith("$")) {
                        // reverse reference
                        resName = resName.substring(0, resName.length() - 1);
                        html.append(Messages.get().getBundle(locale).key(
                            Messages.GUI_PUBLISH_DETAIL_REVERSE_REFERENCE_0));
                    } else {
                        // resource will not be published
                        html.append("<font color='red' />");
                        html.append(Messages.get().getBundle(locale).key(
                            Messages.GUI_PUBLISH_DETAIL_RELATED_RESOURCE_NO_0));
                        html.append("</font/>");
                    }
                    html.append("&nbsp;:&nbsp;\n");
                    html.append("\t\t</td>\n");
                    html.append("\t\t<td class='listdetailitem' style='white-space:normal;'>\n");
                    html.append("\t\t\t");
                    html.append(resName);
                    html.append("\n");
                    html.append("\t\t</td>\n");
                    html.append("\t</tr>\n");
                }
                if (html.length() > 0) {
                    html.append("</table>\n");
                }
                return html.toString();
            }

        });
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
