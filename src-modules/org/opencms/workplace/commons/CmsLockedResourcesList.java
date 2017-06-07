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
import org.opencms.i18n.CmsEncoder;
import org.opencms.i18n.CmsMessageContainer;
import org.opencms.jsp.CmsJspActionElement;
import org.opencms.workplace.CmsDialog;
import org.opencms.workplace.CmsMultiDialog;
import org.opencms.workplace.CmsWorkplace;
import org.opencms.workplace.explorer.CmsResourceUtil;
import org.opencms.workplace.list.A_CmsListDialog;
import org.opencms.workplace.list.A_CmsListExplorerDialog;
import org.opencms.workplace.list.A_CmsListIndependentJsAction;
import org.opencms.workplace.list.CmsListColumnDefinition;
import org.opencms.workplace.list.CmsListDirectAction;
import org.opencms.workplace.list.CmsListExplorerColumn;
import org.opencms.workplace.list.CmsListItemDetails;
import org.opencms.workplace.list.CmsListMetadata;
import org.opencms.workplace.list.CmsListResourceProjStateAction;
import org.opencms.workplace.list.I_CmsListAction;
import org.opencms.workplace.list.I_CmsListResourceCollector;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Explorer dialog for the project files view.<p>
 *
 * @since 6.0.0
 */
public class CmsLockedResourcesList extends A_CmsListExplorerDialog {

    /** list action id constant. */
    public static final String LIST_DETAIL_OWN_LOCKS = "dol";

    /** list action id constant. */
    public static final String LIST_DETAIL_OWN_LOCKS_HIDE = "dolh";

    /** list action id constant. */
    public static final String LIST_DETAIL_OWN_LOCKS_SHOW = "dols";

    /** list id constant. */
    public static final String LIST_ID = "llr";

    /** List column id constant. */
    protected static final String LIST_COLUMN_IS_RELATED = "ecir";

    /** The internal collector instance. */
    private I_CmsListResourceCollector m_collector;

    /** The parameter map for creating the ajax request in the independent action.  */
    private Map<String, String> m_lockParams;

    /**
     * Public constructor with JSP action element.<p>
     *
     * @param jsp an initialized JSP action element
     * @param lockedResources the list of locked resources (as root paths)
     * @param relativeTo the current folder
     * @param lockParams the parameter map for creating the ajax request in the independent action
     */
    public CmsLockedResourcesList(
        CmsJspActionElement jsp,
        List<String> lockedResources,
        String relativeTo,
        Map<String, String> lockParams) {

        super(jsp, LIST_ID, Messages.get().container(Messages.GUI_LOCKED_FILES_LIST_NAME_0));
        m_collector = new CmsLockedResourcesCollector(this, lockedResources);

        // prevent paging
        getList().setMaxItemsPerPage(Integer.MAX_VALUE);

        // set the right resource util parameters
        CmsResourceUtil resUtil = getResourceUtil();
        resUtil.setAbbrevLength(50);
        resUtil.setRelativeTo(getCms().getRequestContext().addSiteRoot(relativeTo));

        m_lockParams = lockParams;
        getList().getMetadata().getItemDetailDefinition(LIST_DETAIL_OWN_LOCKS).setVisible(
            Boolean.valueOf(getLockParams().get(CmsLock.PARAM_SHOWOWNLOCKS)).booleanValue());
    }

    /**
     * @see org.opencms.workplace.list.A_CmsListDialog#executeListMultiActions()
     */
    @Override
    public void executeListMultiActions() {

        throwListUnsupportedActionException();
    }

    /**
     * @see org.opencms.workplace.list.A_CmsListDialog#executeListSingleActions()
     */
    @Override
    public void executeListSingleActions() {

        throwListUnsupportedActionException();
    }

    /**
     * @see org.opencms.workplace.list.A_CmsListExplorerDialog#getCollector()
     */
    @Override
    public I_CmsListResourceCollector getCollector() {

        return m_collector;
    }

    /**
     * Returns the parameter map for creating the ajax request in the independent action.<p>
     *
     * @return the parameter map for creating the ajax request in the independent action
     */
    public Map<String, String> getLockParams() {

        return m_lockParams;
    }

    /**
     * @see org.opencms.workplace.list.A_CmsListDialog#fillDetails(java.lang.String)
     */
    @Override
    protected void fillDetails(String detailId) {

        // no-details
    }

    /**
     * @see org.opencms.workplace.CmsWorkplace#initMessages()
     */
    @Override
    protected void initMessages() {

        // add specific dialog resource bundle
        addMessages(Messages.get().getBundleName());
        // add default resource bundles
        super.initMessages();
    }

    /**
     * @see org.opencms.workplace.list.A_CmsListExplorerDialog#isColumnVisible(int)
     */
    @Override
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
    @Override
    protected void setColumns(CmsListMetadata metadata) {

        super.setColumns(metadata);

        Iterator<CmsListColumnDefinition> it = metadata.getColumnDefinitions().iterator();
        while (it.hasNext()) {
            CmsListColumnDefinition colDefinition = it.next();
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
                    @Override
                    public String getIconPath() {

                        if (((Boolean)getItem().get(LIST_COLUMN_IS_RELATED)).booleanValue()) {
                            return "explorer/related_resource.png";
                        }
                        return super.getIconPath();
                    }

                    /**
                     * @see org.opencms.workplace.list.CmsListResourceProjStateAction#getName()
                     */
                    @Override
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
        relatedCol.setName(
            org.opencms.workplace.explorer.Messages.get().container(
                org.opencms.workplace.explorer.Messages.GUI_INPUT_NAME_0));
        relatedCol.setVisible(false);
        relatedCol.setPrintable(false);
        metadata.addColumn(relatedCol);
    }

    /**
     * @see org.opencms.workplace.list.A_CmsListExplorerDialog#setIndependentActions(org.opencms.workplace.list.CmsListMetadata)
     */
    @Override
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
            @Override
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
                html.append(super.getIconPath());
                html.append("'");
                html.append(" alt='");
                html.append(super.getName().key(wp.getLocale()));
                html.append("'");
                html.append(" title='");
                html.append(super.getName().key(wp.getLocale()));
                html.append("'");
                html.append(">");
                html.append("&nbsp;");
                html.append("<a href='#'>");
                html.append(super.getName().key(wp.getLocale()));
                html.append("</a>");
                html.append("</span>");
                return html.toString();
            }

            /**
             * Returns an ajax request call code.<p>
             *
             * @param wp the workplace context
             * @param showOwnLocks if to show or hide the own locked resources
             *
             * @return html code
             */
            protected String getRequestLink(CmsWorkplace wp, boolean showOwnLocks) {

                Map<String, String> params = ((CmsLockedResourcesList)wp).getLockParams();
                StringBuffer html = new StringBuffer(128);
                html.append(
                    "javascript:{ajaxReportContent = ''; document.getElementById('ajaxreport').innerHTML = ajaxWaitMessage; makeRequest('");
                html.append(wp.getJsp().link("/system/workplace/commons/report-locks.jsp"));
                html.append("', '");
                boolean needsAmpersand = false;
                if (params.get(CmsMultiDialog.PARAM_RESOURCELIST) != null) {
                    html.append(CmsMultiDialog.PARAM_RESOURCELIST);
                    html.append("=");
                    html.append(CmsEncoder.escapeXml(params.get(CmsMultiDialog.PARAM_RESOURCELIST)));
                    needsAmpersand = true;
                }
                if (params.get(CmsDialog.PARAM_RESOURCE) != null) {
                    if (needsAmpersand) {
                        html.append("&");
                    }
                    html.append(CmsDialog.PARAM_RESOURCE);
                    html.append("=");
                    html.append(CmsEncoder.escapeXml(params.get(CmsDialog.PARAM_RESOURCE)));
                    needsAmpersand = true;
                }
                if (params.get(CmsLock.PARAM_INCLUDERELATED) != null) {
                    if (needsAmpersand) {
                        html.append("&");
                    }
                    html.append(CmsLock.PARAM_INCLUDERELATED);
                    html.append("=");
                    html.append(CmsEncoder.escapeXml(params.get(CmsLock.PARAM_INCLUDERELATED)));
                }
                if (needsAmpersand) {
                    html.append("&");
                }
                html.append(CmsLock.PARAM_SHOWOWNLOCKS);
                html.append("=").append(showOwnLocks).append("', 'doReportUpdate');}");
                return html.toString();
            }
        }

        I_CmsListAction hideAction = new DetailsJsAction(LIST_DETAIL_OWN_LOCKS_HIDE) {

            /**
             * @see org.opencms.workplace.list.A_CmsListIndependentJsAction#jsCode(CmsWorkplace)
             */
            @Override
            public String jsCode(CmsWorkplace wp) {

                return getRequestLink(wp, false);
            }
        };
        hideAction.setIconPath(A_CmsListDialog.ICON_DETAILS_HIDE);
        hideAction.setName(Messages.get().container(Messages.GUI_LOCK_DETAIL_HIDE_OWN_LOCKS_NAME_0));
        hideAction.setHelpText(Messages.get().container(Messages.GUI_LOCK_DETAIL_HIDE_OWN_LOCKS_HELP_0));

        I_CmsListAction showAction = new DetailsJsAction(LIST_DETAIL_OWN_LOCKS_SHOW) {

            /**
             * @see org.opencms.workplace.list.A_CmsListIndependentJsAction#jsCode(CmsWorkplace)
             */
            @Override
            public String jsCode(CmsWorkplace wp) {

                return getRequestLink(wp, true);
            }
        };
        showAction.setIconPath(A_CmsListDialog.ICON_DETAILS_SHOW);
        showAction.setName(Messages.get().container(Messages.GUI_LOCK_DETAIL_SHOW_OWN_LOCKS_NAME_0));
        showAction.setHelpText(Messages.get().container(Messages.GUI_LOCK_DETAIL_SHOW_OWN_LOCKS_HELP_0));

        // create list item detail
        CmsListItemDetails relationsDetails = new CmsListItemDetails(LIST_DETAIL_OWN_LOCKS);
        relationsDetails.setAtColumn(LIST_COLUMN_NAME);
        relationsDetails.setVisible(false);
        relationsDetails.setFormatter(new CmsPublishBrokenRelationFormatter());
        relationsDetails.setHideAction(hideAction);
        relationsDetails.setShowAction(showAction);

        // add resources info item detail to meta data
        metadata.addItemDetails(relationsDetails);
    }

    /**
     * @see org.opencms.workplace.list.A_CmsListDialog#setMultiActions(org.opencms.workplace.list.CmsListMetadata)
     */
    @Override
    protected void setMultiActions(CmsListMetadata metadata) {

        // no LMAs, and remove default search action
        metadata.setSearchAction(null);
    }
}
