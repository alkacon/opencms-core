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

package org.opencms.workplace.tools.searchindex;

import org.opencms.configuration.CmsSearchConfiguration;
import org.opencms.i18n.CmsMessageContainer;
import org.opencms.jsp.CmsJspActionElement;
import org.opencms.main.CmsIllegalStateException;
import org.opencms.main.CmsLog;
import org.opencms.main.OpenCms;
import org.opencms.search.CmsSearchDocumentType;
import org.opencms.search.CmsSearchException;
import org.opencms.search.CmsSearchIndex;
import org.opencms.search.CmsSearchIndexSource;
import org.opencms.search.CmsSearchManager;
import org.opencms.workplace.CmsWorkplaceSettings;
import org.opencms.workplace.list.CmsListColumnAlignEnum;
import org.opencms.workplace.list.CmsListColumnDefinition;
import org.opencms.workplace.list.CmsListDefaultAction;
import org.opencms.workplace.list.CmsListDirectAction;
import org.opencms.workplace.list.CmsListItem;
import org.opencms.workplace.list.CmsListItemDetails;
import org.opencms.workplace.list.CmsListItemDetailsFormatter;
import org.opencms.workplace.list.CmsListMetadata;
import org.opencms.workplace.list.CmsListMultiAction;
import org.opencms.workplace.list.CmsListOrderEnum;
import org.opencms.workplace.tools.CmsToolDialog;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.jsp.PageContext;

import org.apache.commons.logging.Log;

/**
 * A list that displays the <code>{@link org.opencms.search.CmsSearchIndexSource}</code>
 * instances that are not members of the <code>{@link org.opencms.search.CmsSearchIndex}</code>
 * in the current request scope (param "searchindex" and allows to add them to the index.<p>
 *
 * This list is no stand-alone page but has to be embedded in another dialog
 * (see <code> {@link org.opencms.workplace.tools.searchindex.A_CmsEmbeddedListDialog}</code>. <p>
 *
 * @since 6.0.0
 */
public class CmsSearchIndexSourceAddList extends A_CmsEmbeddedListDialog {

    /** list action dummy id constant. */
    public static final String LIST_ACTION_ADDSOURCE = "a";

    /** list action dummy id constant. */
    public static final String LIST_ACTION_NONE = "n";

    /** list column id constant. */
    public static final String LIST_COLUMN_ADDSOURCE = "cas";

    /** list column id constant. */
    public static final String LIST_COLUMN_ADDSOURCE2 = LIST_COLUMN_ADDSOURCE + "2";

    /** list column id constant. */
    public static final String LIST_COLUMN_ICON = "ci";

    /** list column id constant. */
    public static final String LIST_COLUMN_INDEXER = "ca";

    /** list column id constant. */
    public static final String LIST_COLUMN_NAME = "cn";

    /** list item detail id constant. */
    public static final String LIST_DETAIL_DOCTYPES = "dd";

    /** list item detail id constant. */
    public static final String LIST_DETAIL_RESOURCES = "dr";

    /** list id constant. */
    public static final String LIST_ID = "lssisa";

    /** list action id constant. */
    public static final String LIST_MACTION_ADDSOURCE = "maa";

    /** The log object for this class. */
    private static final Log LOG = CmsLog.getLog(CmsSearchIndexSourceAddList.class);

    /** Stores the value of the request parameter for the search index Name. */
    private String m_paramIndexName;

    /**
     * Public constructor.<p>
     *
     * @param jsp an initialized JSP action element
     */
    public CmsSearchIndexSourceAddList(CmsJspActionElement jsp) {

        this(jsp, LIST_ID, Messages.get().container(Messages.GUI_LIST_INDEXSOURCES_AVAIL_NAME_0));
    }

    /**
     * Public constructor.<p>
     *
     * @param jsp an initialized JSP action element
     * @param listId the id of the list
     * @param listName the list name
     */
    public CmsSearchIndexSourceAddList(CmsJspActionElement jsp, String listId, CmsMessageContainer listName) {

        this(jsp, listId, listName, LIST_COLUMN_NAME, CmsListOrderEnum.ORDER_ASCENDING, null);
    }

    /**
     * Public constructor.<p>
     *
     * @param jsp an initialized JSP action element
     * @param listId the id of the displayed list
     * @param listName the name of the list
     * @param sortedColId the a priory sorted column
     * @param sortOrder the order of the sorted column
     * @param searchableColId the column to search into
     */
    public CmsSearchIndexSourceAddList(
        CmsJspActionElement jsp,
        String listId,
        CmsMessageContainer listName,
        String sortedColId,
        CmsListOrderEnum sortOrder,
        String searchableColId) {

        super(jsp, listId, listName, sortedColId, sortOrder, searchableColId);

    }

    /**
     * Public constructor.<p>
     *
     * Public constructor with JSP variables.<p>
     *
     * @param context the JSP page context
     * @param req the JSP request
     * @param res the JSP response
     */
    public CmsSearchIndexSourceAddList(PageContext context, HttpServletRequest req, HttpServletResponse res) {

        this(new CmsJspActionElement(context, req, res));
    }

    /**
     * @see org.opencms.workplace.list.A_CmsListDialog#executeListMultiActions()
     */
    @Override
    public void executeListMultiActions() {

        CmsSearchManager searchManager = OpenCms.getSearchManager();
        CmsListItem item = getSelectedItem();
        if (getParamListAction().equals(LIST_MACTION_ADDSOURCE)) {
            // execute the delete multiaction
            Iterator<CmsListItem> itItems = getSelectedItems().iterator();
            String indexSource;
            CmsSearchIndex idx = searchManager.getIndex(getParamIndexName());
            while (itItems.hasNext()) {
                item = itItems.next();
                indexSource = (String)item.get(LIST_COLUMN_NAME);
                idx.addSourceName(indexSource);
            }
            try {
                idx.initialize();
            } catch (CmsSearchException e) {
                if (LOG.isErrorEnabled()) {
                    LOG.error(e.getLocalizedMessage(), e);
                }
            }
            writeConfiguration(false);
            refreshList();
        }

        listSave();
    }

    /**
     * @see org.opencms.workplace.list.A_CmsListDialog#executeListSingleActions()
     */
    @Override
    public void executeListSingleActions() throws IOException, ServletException {

        CmsSearchManager searchManager = OpenCms.getSearchManager();
        CmsListItem item = getSelectedItem();
        String indexsourceName = (String)item.get(LIST_COLUMN_NAME);
        String action = getParamListAction();
        if (action.equals(LIST_ACTION_ADDSOURCE) || action.equals(LIST_COLUMN_ADDSOURCE2)) {

            CmsSearchIndex idx = searchManager.getIndex(getParamIndexName());
            idx.addSourceName(indexsourceName);
            try {
                idx.initialize();
            } catch (CmsSearchException e) {
                if (LOG.isErrorEnabled()) {
                    LOG.error(e.getLocalizedMessage(), e);
                }
            }
            refreshList();
            writeConfiguration(false);
        } else if (action.equals(CmsSearchIndexSourceControlList.LIST_ACTION_OVERVIEW_INDEXSOURCE)) {

            // action currently unused (not triggered by a column any more)
            Map<String, String[]> params = new HashMap<String, String[]>();
            // forward to the edit indexsource screen
            params.put(A_CmsEditIndexSourceDialog.PARAM_INDEXSOURCE, new String[] {indexsourceName});
            params.put(PARAM_STYLE, new String[] {CmsToolDialog.STYLE_NEW});
            getToolManager().jspForwardTool(this, "/searchindex/indexsources/indexsource", params);

        }
    }

    /**
     * Returns the request parameter mapped to member <code>m_searchindex</code>
     * or null if no one was received. <p>
     *
     * @return the request parameter mapped to member <code>m_searchindex</code>
     *          or null if no one was received
     */
    public String getParamIndexName() {

        return m_paramIndexName;
    }

    /**
     * Maps the request parameter to member <code>m_searchindex</code>. <p>
     *
     * @param paramSearchIndex the request parameter <code>searchindex</code>
     *        that is filled using this method.
     */
    public void setParamIndexName(String paramSearchIndex) {

        m_paramIndexName = paramSearchIndex;
    }

    /**
     * @see org.opencms.workplace.list.A_CmsListDialog#fillDetails(java.lang.String)
     */
    @Override
    protected void fillDetails(String detailId) {

        // get content
        List<CmsListItem> items = getList().getAllContent();
        if (detailId.equals(LIST_DETAIL_DOCTYPES)) {
            for (CmsListItem item : items) {
                fillDetailDocTypes(item, detailId);

            }
        } else if (detailId.equals(LIST_DETAIL_RESOURCES)) {
            for (CmsListItem item : items) {
                fillDetailResources(item, detailId);

            }
        }
    }

    /**
     * @see org.opencms.workplace.list.A_CmsListDialog#getListItems()
     */
    @Override
    protected List<CmsListItem> getListItems() {

        List<CmsListItem> result = new ArrayList<CmsListItem>();
        // get content
        // cannot use the returned map: unmodifyable
        List<CmsSearchIndexSource> allSources = new LinkedList<CmsSearchIndexSource>(
            OpenCms.getSearchManager().getSearchIndexSources().values());
        allSources.removeAll(searchIndexSources());
        Iterator<CmsSearchIndexSource> itSources = allSources.iterator();
        CmsSearchIndexSource source;
        while (itSources.hasNext()) {
            source = itSources.next();
            CmsListItem item = getList().newItem(source.getName());
            item.set(LIST_COLUMN_NAME, source.getName());
            item.set(LIST_COLUMN_INDEXER, source.getIndexer().getClass().getName());
            result.add(item);
        }
        return result;
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
     * @see org.opencms.workplace.list.A_CmsListDialog#initWorkplaceRequestValues(org.opencms.workplace.CmsWorkplaceSettings, javax.servlet.http.HttpServletRequest)
     */
    @Override
    protected void initWorkplaceRequestValues(CmsWorkplaceSettings settings, HttpServletRequest request) {

        super.initWorkplaceRequestValues(settings, request);
    }

    /**
     * @see org.opencms.workplace.list.A_CmsListDialog#setColumns(org.opencms.workplace.list.CmsListMetadata)
     */
    @Override
    protected void setColumns(CmsListMetadata metadata) {

        // create dummy column for icon
        CmsListColumnDefinition editCol = new CmsListColumnDefinition(LIST_COLUMN_ICON);
        editCol.setName(Messages.get().container(Messages.GUI_LIST_INDEXSOURCE_COL_ICON_NAME_0));
        editCol.setHelpText(Messages.get().container(Messages.GUI_LIST_INDEXSOURCE_COL_ICON_NAME_HELP_0));
        editCol.setWidth("5");
        editCol.setAlign(CmsListColumnAlignEnum.ALIGN_LEFT);
        editCol.setSorteable(false);
        // add dummy icon
        CmsListDirectAction editAction = new CmsListDirectAction(LIST_ACTION_NONE);
        editAction.setName(Messages.get().container(Messages.GUI_LIST_INDEXSOURCE_COL_ICON_NAME_0));
        editAction.setHelpText(Messages.get().container(Messages.GUI_LIST_INDEXSOURCE_COL_ICON_NAME_HELP_0));
        editAction.setIconPath(CmsSearchIndexList.LIST_ICON_INDEXSOURCE);
        // disable!
        editAction.setEnabled(false);
        editCol.addDirectAction(editAction);
        // add it to the list definition
        metadata.addColumn(editCol);

        // add column for add action
        CmsListColumnDefinition addCol = new CmsListColumnDefinition(LIST_COLUMN_ADDSOURCE);
        addCol.setName(Messages.get().container(Messages.GUI_LIST_INDEXSOURCE_COL_ADDSOURCE_NAME_0));
        addCol.setHelpText(Messages.get().container(Messages.GUI_LIST_INDEXSOURCE_COL_ADDSOURCE_NAME_HELP_0));
        addCol.setWidth("5");
        addCol.setAlign(CmsListColumnAlignEnum.ALIGN_LEFT);
        addCol.setSorteable(false);
        // add add action
        CmsListDirectAction addAction = new CmsListDirectAction(LIST_ACTION_ADDSOURCE);
        addAction.setName(Messages.get().container(Messages.GUI_LIST_INDEXSOURCE_COL_ADDSOURCE_NAME_0));
        addAction.setHelpText(Messages.get().container(Messages.GUI_LIST_SEARCHINDEX_ACTION_ADDSOURCE_NAME_0));
        addAction.setIconPath(ICON_ADD);
        addCol.addDirectAction(addAction);
        // add it to the list definition
        metadata.addColumn(addCol);

        // add column for name
        CmsListColumnDefinition nameCol = new CmsListColumnDefinition(LIST_COLUMN_NAME);
        nameCol.setAlign(CmsListColumnAlignEnum.ALIGN_LEFT);
        nameCol.setName(Messages.get().container(Messages.GUI_LIST_SEARCHINDEX_COL_NAME_0));
        nameCol.setWidth("55%");
        // add second add action, see how action id has to be different....
        CmsListDefaultAction addAction2 = new CmsListDefaultAction(LIST_COLUMN_ADDSOURCE2);
        addAction2.setName(Messages.get().container(Messages.GUI_LIST_INDEXSOURCE_COL_ADDSOURCE_NAME_0));
        addAction2.setHelpText(Messages.get().container(Messages.GUI_LIST_SEARCHINDEX_ACTION_ADDSOURCE_NAME_0));
        nameCol.addDefaultAction(addAction2);
        metadata.addColumn(nameCol);

        // add column for analyzer
        CmsListColumnDefinition analyzerCol = new CmsListColumnDefinition(LIST_COLUMN_INDEXER);
        analyzerCol.setAlign(CmsListColumnAlignEnum.ALIGN_LEFT);
        analyzerCol.setName(Messages.get().container(Messages.GUI_LIST_INDEXSOURCE_COL_INDEXER_0));
        analyzerCol.setWidth("30%");
        metadata.addColumn(analyzerCol);

    }

    /**
     * @see org.opencms.workplace.list.A_CmsListDialog#setIndependentActions(org.opencms.workplace.list.CmsListMetadata)
     */
    @Override
    protected void setIndependentActions(CmsListMetadata metadata) {

        // add document types of index source detail help
        CmsListItemDetails doctypeDetails = new CmsListItemDetails(LIST_DETAIL_DOCTYPES);
        doctypeDetails.setAtColumn(LIST_COLUMN_NAME);
        doctypeDetails.setVisible(false);
        doctypeDetails.setShowActionName(Messages.get().container(Messages.GUI_LIST_INDEXSOURCE_DETAIL_DOCTYPE_NAME_0));
        doctypeDetails.setShowActionHelpText(
            Messages.get().container(Messages.GUI_LIST_INDEXSOURCE_DETAIL_DOCTYPE_SHOW_HELP_0));
        doctypeDetails.setHideActionName(Messages.get().container(Messages.GUI_LIST_INDEXSOURCE_DETAIL_DOCTYPE_NAME_0));
        doctypeDetails.setHideActionHelpText(
            Messages.get().container(Messages.GUI_LIST_INDEXSOURCE_DETAIL_DOCTYPE_HIDE_HELP_0));
        doctypeDetails.setName(Messages.get().container(Messages.GUI_LIST_INDEXSOURCE_DETAIL_DOCTYPE_NAME_0));
        doctypeDetails.setFormatter(
            new CmsListItemDetailsFormatter(
                Messages.get().container(Messages.GUI_LIST_INDEXSOURCE_DETAIL_DOCTYPE_NAME_0)));
        metadata.addItemDetails(doctypeDetails);

        // add resources of index source detail help
        CmsListItemDetails resourceDetails = new CmsListItemDetails(LIST_DETAIL_RESOURCES);
        resourceDetails.setAtColumn(LIST_COLUMN_NAME);
        resourceDetails.setVisible(false);
        resourceDetails.setShowActionName(
            Messages.get().container(Messages.GUI_LIST_INDEXSOURCE_DETAIL_RESOURCE_NAME_0));
        resourceDetails.setShowActionHelpText(
            Messages.get().container(Messages.GUI_LIST_INDEXSOURCE_DETAIL_RESOURCE_SHOW_HELP_0));
        resourceDetails.setHideActionName(
            Messages.get().container(Messages.GUI_LIST_INDEXSOURCE_DETAIL_RESOURCE_NAME_0));
        resourceDetails.setHideActionHelpText(
            Messages.get().container(Messages.GUI_LIST_INDEXSOURCE_DETAIL_RESOURCE_HIDE_HELP_0));
        resourceDetails.setName(Messages.get().container(Messages.GUI_LIST_INDEXSOURCE_DETAIL_RESOURCE_NAME_0));
        resourceDetails.setFormatter(
            new CmsListItemDetailsFormatter(
                Messages.get().container(Messages.GUI_LIST_INDEXSOURCE_DETAIL_RESOURCE_NAME_0)));
        metadata.addItemDetails(resourceDetails);

    }

    /**
     * @see org.opencms.workplace.list.A_CmsListDialog#setMultiActions(org.opencms.workplace.list.CmsListMetadata)
     */
    @Override
    protected void setMultiActions(CmsListMetadata metadata) {

        // add add multi action
        CmsListMultiAction addMultiAction = new CmsListMultiAction(LIST_MACTION_ADDSOURCE);
        addMultiAction.setName(Messages.get().container(Messages.GUI_LIST_SEARCHINDEX_MACTION_ADDSOURCE_NAME_0));
        addMultiAction.setHelpText(
            Messages.get().container(Messages.GUI_LIST_SEARCHINDEX_MACTION_ADDSOURCE_NAME_HELP_0));
        addMultiAction.setIconPath(ICON_MULTI_ADD);
        metadata.addMultiAction(addMultiAction);
    }

    /**
     * @see org.opencms.workplace.list.A_CmsListDialog#validateParamaters()
     */
    @Override
    protected void validateParamaters() throws Exception {

        // test the needed parameters
        if (getParamIndexName() == null) {
            throw new CmsIllegalStateException(Messages.get().container(
                Messages.ERR_SEARCHINDEX_EDIT_MISSING_PARAM_1,
                A_CmsEditSearchIndexDialog.PARAM_INDEXNAME));
        }
    }

    /**
     * Writes the updated search configuration back to the XML
     * configuration file and refreshes the complete list.<p>
     *
     * @param refresh if true, the list items are refreshed
     */
    protected void writeConfiguration(boolean refresh) {

        // update the XML configuration
        OpenCms.writeConfiguration(CmsSearchConfiguration.class);
        if (refresh) {
            refreshList();
        }
    }

    /**
     * Fills details about document types of the index source into the given item. <p>
     *
     * @param item the list item to fill
     * @param detailId the id for the detail to fill
     *
     */
    private void fillDetailDocTypes(CmsListItem item, String detailId) {

        CmsSearchManager searchManager = OpenCms.getSearchManager();
        StringBuffer html = new StringBuffer();

        // search for the corresponding CmsSearchIndexSource:
        String idxSourceName = (String)item.get(LIST_COLUMN_NAME);
        CmsSearchIndexSource idxSource = searchManager.getIndexSource(idxSourceName);

        // get the index sources doc types
        List<String> docTypes = idxSource.getDocumentTypes();
        // output of found index sources
        Iterator<String> itDocTypes = docTypes.iterator();
        CmsSearchDocumentType docType;
        html.append("<ul>\n");
        while (itDocTypes.hasNext()) {
            // get the instance (instead of plain name) for more detail in future...
            docType = searchManager.getDocumentTypeConfig(itDocTypes.next());
            // harden against unconfigured doctypes that are refferred to by indexsource nodes
            if (docType != null) {

                html.append("  <li>\n").append("  ").append(docType.getName()).append("\n");
                html.append("  </li>");
            }
        }

        html.append("</ul>\n");
        item.set(detailId, html.toString());
    }

    /**
     * Fills details about resource paths of the index source into the given item. <p>
     *
     * @param item the list item to fill
     * @param detailId the id for the detail to fill
     *
     */
    private void fillDetailResources(CmsListItem item, String detailId) {

        CmsSearchManager searchManager = OpenCms.getSearchManager();
        StringBuffer html = new StringBuffer();

        // search for the corresponding CmsSearchIndexSource:
        String idxSourceName = (String)item.get(LIST_COLUMN_NAME);
        CmsSearchIndexSource idxSource = searchManager.getIndexSource(idxSourceName);

        // get the index sources resource strings
        List<String> resources = idxSource.getResourcesNames();
        // output of found index sources
        Iterator<String> itResources = resources.iterator();
        html.append("<ul>\n");
        while (itResources.hasNext()) {

            html.append("  <li>\n").append("  ").append(itResources.next()).append("\n");
            html.append("  </li>");
        }

        html.append("</ul>\n");

        item.set(detailId, html.toString());
    }

    /**
     * Returns the available search indexes of this installation.
     *
     * @return the available search indexes of this installation
     */
    private List<CmsSearchIndexSource> searchIndexSources() {

        CmsSearchManager manager = OpenCms.getSearchManager();
        CmsSearchIndex index = manager.getIndex(getParamIndexName());
        List<CmsSearchIndexSource> sources = index.getSources();
        return sources;
    }

}
