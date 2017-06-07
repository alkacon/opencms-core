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

package org.opencms.workplace.search;

import org.opencms.jsp.CmsJspActionElement;
import org.opencms.search.CmsSearchResult;
import org.opencms.util.CmsUUID;
import org.opencms.workplace.CmsDialog;
import org.opencms.workplace.CmsWorkplace;
import org.opencms.workplace.editors.CmsEditor;
import org.opencms.workplace.explorer.CmsExplorer;
import org.opencms.workplace.explorer.CmsResourceUtil;
import org.opencms.workplace.list.A_CmsListExplorerDialog;
import org.opencms.workplace.list.CmsListColumnAlignEnum;
import org.opencms.workplace.list.CmsListColumnDefinition;
import org.opencms.workplace.list.CmsListExplorerColumn;
import org.opencms.workplace.list.CmsListItem;
import org.opencms.workplace.list.CmsListItemDetails;
import org.opencms.workplace.list.CmsListMetadata;
import org.opencms.workplace.list.CmsListOrderEnum;
import org.opencms.workplace.list.I_CmsListFormatter;
import org.opencms.workplace.list.I_CmsListResourceCollector;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.jsp.PageContext;

/**
 * Explorer list for the search results.<p>
 *
 * @since 6.0.0
 */
public class CmsSearchResultsList extends A_CmsListExplorerDialog {

    /** List column id constant. */
    public static final String LIST_COLUMN_SCORE = "cs";

    /** list item detail id constant. */
    public static final String LIST_DETAIL_EXCERPT = "de";

    /** list id constant. */
    public static final String LIST_ID = "lsr";

    /** Path to the list buttons. */
    public static final String PATH_BUTTONS = "tools/ex_search/buttons/";

    /** The internal collector instance. */
    private I_CmsListResourceCollector m_collector;

    /** Search parameters. */
    private CmsSearchWorkplaceBean m_searchParams;

    /**
     * Public constructor with JSP action element.<p>
     *
     * @param jsp an initialized JSP action element
     */
    public CmsSearchResultsList(CmsJspActionElement jsp) {

        super(
            jsp,
            LIST_ID,
            Messages.get().container(Messages.GUI_SEARCH_LIST_NAME_0),
            A_CmsListExplorerDialog.LIST_COLUMN_NAME,
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
    public CmsSearchResultsList(PageContext context, HttpServletRequest req, HttpServletResponse res) {

        this(new CmsJspActionElement(context, req, res));
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
    public void executeListSingleActions() throws IOException, ServletException {

        if (getParamListAction().equals(LIST_ACTION_EDIT)) {
            // forward to the editor
            Map<String, String[]> params = new HashMap<String, String[]>();
            params.put(CmsDialog.PARAM_ACTION, new String[] {CmsDialog.DIALOG_INITIAL});
            params.put(CmsDialog.PARAM_CLOSELINK, new String[] {CmsWorkplace.VFS_PATH_VIEWS + "workplace.jsp"});
            params.put(CmsEditor.PARAM_BACKLINK, new String[] {CmsWorkplace.VFS_PATH_VIEWS + "workplace.jsp"});
            params.put(CmsDialog.PARAM_RESOURCE, new String[] {(String)getSelectedItem().get(LIST_COLUMN_NAME)});
            getToolManager().jspForwardPage(this, "/system/workplace/explorer/search/edit.jsp", params);
        } else {
            throwListUnsupportedActionException();
        }
    }

    /**
     * @see org.opencms.workplace.list.A_CmsListExplorerDialog#getCollector()
     */
    @Override
    public I_CmsListResourceCollector getCollector() {

        if (m_collector == null) {
            m_collector = new CmsSearchResourcesCollector(
                this,
                getSearchParams().getQuery(),
                getSearchParams().getSortOrder(),
                getSearchParams().getFields(),
                Collections.singletonList(getSearchParams().getSearchPath()),
                getSearchParams().getMinDateCreated(),
                getSearchParams().getMaxDateCreated(),
                getSearchParams().getMinDateLastModified(),
                getSearchParams().getMaxDateLastModified(),
                getSearchParams().getIndexName());

            // set the right resource util parameters
            CmsResourceUtil resUtil = getResourceUtil();
            resUtil.setAbbrevLength(50);
            resUtil.setSiteMode(CmsResourceUtil.SITE_MODE_MATCHING);
        }
        return m_collector;
    }

    /**
     * Generates the dialog starting html code.<p>
     *
     * @return html code
     */
    @Override
    protected String defaultActionHtmlStart() {

        StringBuffer result = new StringBuffer(2048);
        result.append(htmlStart(null));
        result.append(getList().listJs());
        result.append(CmsListExplorerColumn.getExplorerStyleDef());
        result.append("<script language='JavaScript'>\n");
        result.append(new CmsExplorer(getJsp()).getInitializationHeader());
        result.append("\ntop.updateWindowStore();\n");
        result.append("top.displayHead(top.win.head, 0, 1);\n}\n");
        result.append("</script>");
        result.append(bodyStart("dialog", "onload='initialize();'"));
        result.append(dialogStart());
        result.append(dialogContentStart(getParamTitle()));
        return result.toString();
    }

    /**
     * @see org.opencms.workplace.list.A_CmsListDialog#fillDetails(java.lang.String)
     */
    @Override
    protected void fillDetails(String detailId) {

        // excerpt detail is enabled
        if (detailId.equals(LIST_DETAIL_EXCERPT)) {
            CmsSearchResourcesCollector collector = (CmsSearchResourcesCollector)getCollector();
            Iterator<CmsListItem> itResources = getList().getAllContent().iterator();
            while (itResources.hasNext()) {
                CmsListItem item = itResources.next();
                // get excerpt for item
                if (!item.getId().equals(CmsUUID.getNullUUID().toString())) {
                    CmsSearchResult result = collector.getSearchResult(item.getId());
                    if (result != null) {
                        item.set(detailId, result.getExcerpt());
                    }
                }
            }
        }
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
     * @see org.opencms.workplace.list.A_CmsListDialog#setColumns(org.opencms.workplace.list.CmsListMetadata)
     */
    @Override
    protected void setColumns(CmsListMetadata metadata) {

        super.setColumns(metadata);

        // last position: score
        CmsListColumnDefinition scoreCol = new CmsListExplorerColumn(LIST_COLUMN_SCORE);
        scoreCol.setName(Messages.get().container(Messages.GUI_SEARCH_LIST_COLS_SCORE_0));
        scoreCol.setHelpText(Messages.get().container(Messages.GUI_SEARCH_LIST_COLS_SCORE_HELP_0));
        scoreCol.setAlign(CmsListColumnAlignEnum.ALIGN_RIGHT);
        metadata.addColumn(scoreCol);

        Iterator<CmsListColumnDefinition> it = metadata.getColumnDefinitions().iterator();
        while (it.hasNext()) {
            CmsListColumnDefinition column = it.next();
            column.setSorteable(false);
        }
    }

    /**
     * @see org.opencms.workplace.list.A_CmsListExplorerDialog#setColumnVisibilities()
     */
    @Override
    protected void setColumnVisibilities() {

        super.setColumnVisibilities();
        setColumnVisibility(LIST_COLUMN_EDIT.hashCode(), LIST_COLUMN_EDIT.hashCode());
    }

    /**
     * @see org.opencms.workplace.list.A_CmsListDialog#setIndependentActions(org.opencms.workplace.list.CmsListMetadata)
     */
    @Override
    protected void setIndependentActions(CmsListMetadata metadata) {

        super.setIndependentActions(metadata);
        // add excerpt details
        CmsListItemDetails excerptDetails = new CmsListItemDetails(LIST_DETAIL_EXCERPT);
        excerptDetails.setAtColumn(LIST_COLUMN_NAME);
        excerptDetails.setVisible(true);
        excerptDetails.setShowActionName(Messages.get().container(Messages.GUI_SEARCH_DETAIL_SHOW_EXCERPT_NAME_0));
        excerptDetails.setShowActionHelpText(Messages.get().container(Messages.GUI_SEARCH_DETAIL_SHOW_EXCERPT_HELP_0));
        excerptDetails.setHideActionName(Messages.get().container(Messages.GUI_SEARCH_DETAIL_HIDE_EXCERPT_NAME_0));
        excerptDetails.setHideActionHelpText(Messages.get().container(Messages.GUI_SEARCH_DETAIL_HIDE_EXCERPT_HELP_0));
        excerptDetails.setName(Messages.get().container(Messages.GUI_SEARCH_DETAIL_EXCERPT_NAME_0));
        excerptDetails.setFormatter(new I_CmsListFormatter() {

            /**
             * @see org.opencms.workplace.list.I_CmsListFormatter#format(java.lang.Object, java.util.Locale)
             */
            @Override
            public String format(Object data, Locale locale) {

                return (String)data;
            }
        });
        metadata.addItemDetails(excerptDetails);
    }

    /**
     * @see org.opencms.workplace.list.A_CmsListDialog#setMultiActions(org.opencms.workplace.list.CmsListMetadata)
     */
    @Override
    protected void setMultiActions(CmsListMetadata metadata) {

        // no LMAs
    }

    /**
     * @see org.opencms.workplace.list.A_CmsListDialog#validateParamaters()
     */
    @Override
    protected void validateParamaters() throws Exception {

        if (getSearchParams() == null) {
            throw new Exception();
        }
    }

    /**
     * Returns the search parameter bean.<p>
     *
     * @return the search parameter bean
     */
    private CmsSearchWorkplaceBean getSearchParams() {

        if ((m_searchParams == null) && (getSettings().getDialogObject() instanceof Map<?, ?>)) {
            Map<?, ?> dialogObject = (Map<?, ?>)getSettings().getDialogObject();
            if (dialogObject.get(CmsSearchDialog.class.getName()) instanceof CmsSearchWorkplaceBean) {
                m_searchParams = (CmsSearchWorkplaceBean)dialogObject.get(CmsSearchDialog.class.getName());
            }
        }
        return m_searchParams;
    }
}
