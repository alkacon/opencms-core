/*
 * File   : $Source: /alkacon/cvs/opencms/src-modules/org/opencms/workplace/tools/searchindex/CmsSearchIndexSourceList.java,v $
 * Date   : $Date: 2005/09/20 15:39:06 $
 * Version: $Revision: 1.1.2.1 $
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

package org.opencms.workplace.tools.searchindex;

import org.opencms.configuration.CmsSearchConfiguration;
import org.opencms.i18n.CmsMessageContainer;
import org.opencms.jsp.CmsJspActionElement;
import org.opencms.main.CmsIllegalStateException;
import org.opencms.main.OpenCms;
import org.opencms.search.CmsSearchDocumentType;
import org.opencms.search.CmsSearchIndex;
import org.opencms.search.CmsSearchIndexSource;
import org.opencms.search.CmsSearchManager;
import org.opencms.workplace.list.CmsListColumnAlignEnum;
import org.opencms.workplace.list.CmsListColumnDefinition;
import org.opencms.workplace.list.CmsListDefaultAction;
import org.opencms.workplace.list.CmsListDirectAction;
import org.opencms.workplace.list.CmsListItem;
import org.opencms.workplace.list.CmsListItemDetails;
import org.opencms.workplace.list.CmsListItemDetailsFormatter;
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
 * A list that displays information about the <code>{@link org.opencms.search.CmsSearchIndexSource}</code> 
 * that are members of the <code>{@link org.opencms.search.CmsSearchIndex}</code> 
 * in the current request scope (param "searchindex").<p> 
 * 
 * This list is no stand-alone page but has to be embedded in another dialog 
 * (see <code> {@link org.opencms.workplace.tools.searchindex.A_CmsEmbeddedListDialog}</code>. <p>
 * 
 * @author Achim Westermann 
 * 
 * @version $Revision: 1.1.2.1 $
 * 
 * @since 6.0.0
 */
public class CmsSearchIndexSourceList extends A_CmsEmbeddedListDialog {

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
    public static final String LIST_ID = "lssis";

    /** Stores the value of the request parameter for the search index Name. */
    private String m_paramIndexName;

    /**
     * Public constructor.<p>
     * 
     * @param jsp an initialized JSP action element
     */
    public CmsSearchIndexSourceList(CmsJspActionElement jsp) {

        this(jsp, LIST_ID, Messages.get().container(Messages.GUI_LIST_INDEXSOURCES_NAME_0));
    }

    /**
     * Public constructor.<p>
     * 
     * @param jsp an initialized JSP action element
     * @param listId the id of the list
     * @param listName the list name
     */
    public CmsSearchIndexSourceList(CmsJspActionElement jsp, String listId, CmsMessageContainer listName) {

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
    public CmsSearchIndexSourceList(
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
    public CmsSearchIndexSourceList(PageContext context, HttpServletRequest req, HttpServletResponse res) {

        this(new CmsJspActionElement(context, req, res));
    }

    /**
     * @see org.opencms.workplace.list.A_CmsListDialog#executeListMultiActions()
     */
    public void executeListMultiActions() {

        // view only 
    }

    /**
     * @throws ServletException if something goes wrong
     * 
     * @throws IOException if something goes wrong
     * 
     * @see org.opencms.workplace.list.A_CmsListDialog#executeListSingleActions()
     */
    public void executeListSingleActions() throws IOException, ServletException {

        String indexSource = getSelectedItem().getId();
        Map params = new HashMap();
        String action = getParamListAction();
        if (action.equals(CmsSearchIndexSourceControlList.LIST_ACTION_OVERVIEW_INDEXSOURCE)
            || action.equals(CmsSearchIndexSourceControlList.LIST_ACTION_OVERVIEW_INDEXSOURCE2)) {
            // forward to the index overview screen   
            params.put(A_CmsEditIndexSourceDialog.PARAM_INDEXSOURCE, indexSource);
            params.put(PARAM_ACTION, DIALOG_INITIAL);
            params.put(PARAM_STYLE, "new");
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
    protected void fillDetails(String detailId) {

        // get content
        List items = getList().getAllContent();
        Iterator itItems = items.iterator();
        CmsListItem item;
        if (detailId.equals(LIST_DETAIL_DOCTYPES)) {
            while (itItems.hasNext()) {
                item = (CmsListItem)itItems.next();
                fillDetailDocTypes(item, detailId);

            }
        }
        if (detailId.equals(LIST_DETAIL_RESOURCES)) {
            while (itItems.hasNext()) {
                item = (CmsListItem)itItems.next();
                fillDetailResources(item, detailId);

            }
        }

    }

    /**
     * @see org.opencms.workplace.list.A_CmsListDialog#getListItems()
     */
    protected List getListItems() {

        List result = new ArrayList();
        // get content
        List sources = searchIndexSources();
        Iterator itSources = sources.iterator();
        CmsSearchIndexSource source;
        while (itSources.hasNext()) {
            source = (CmsSearchIndexSource)itSources.next();
            CmsListItem item = getList().newItem(source.getName());
            // use "null" String to avoid exception if name is empty
            item.set(LIST_COLUMN_NAME, String.valueOf(source.getName()));
            item.set(LIST_COLUMN_INDEXER, source.getIndexer().getClass());
            result.add(item);
        }
        return result;
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
     * @see org.opencms.workplace.list.A_CmsListDialog#setColumns(org.opencms.workplace.list.CmsListMetadata)
     */
    protected void setColumns(CmsListMetadata metadata) {

        // create column for edit
        CmsListColumnDefinition editCol = new CmsListColumnDefinition(LIST_COLUMN_ICON);
        editCol.setName(Messages.get().container(Messages.GUI_LIST_INDEXSOURCE_COL_ICON_NAME_0));
        editCol.setHelpText(Messages.get().container(Messages.GUI_LIST_INDEXSOURCE_COL_ICON_NAME_HELP_0));
        editCol.setWidth("5");
        editCol.setAlign(CmsListColumnAlignEnum.ALIGN_LEFT);
        editCol.setSorteable(false);
        // add dummy icon
        CmsListDirectAction editAction = new CmsListDirectAction(
            CmsSearchIndexSourceControlList.LIST_ACTION_OVERVIEW_INDEXSOURCE);
        editAction.setName(Messages.get().container(Messages.GUI_LIST_INDEXSOURCE_COL_ICON_NAME_0));
        editAction.setHelpText(Messages.get().container(Messages.GUI_LIST_INDEXSOURCE_COL_ICON_NAME_HELP_0));
        editAction.setIconPath(CmsSearchIndexList.LIST_ICON_INDEXSOURCE);
        editCol.addDirectAction(editAction);
        // add it to the list definition
        metadata.addColumn(editCol);

        // add column for name
        CmsListColumnDefinition nameCol = new CmsListColumnDefinition(LIST_COLUMN_NAME);
        nameCol.setAlign(CmsListColumnAlignEnum.ALIGN_LEFT);
        nameCol.setName(Messages.get().container(Messages.GUI_LIST_SEARCHINDEX_COL_NAME_0));
        nameCol.setWidth("55%");
        // add overview action
        CmsListDefaultAction defEditAction = new CmsListDefaultAction(
            CmsSearchIndexSourceControlList.LIST_ACTION_OVERVIEW_INDEXSOURCE2);
        defEditAction.setName(Messages.get().container(Messages.GUI_LIST_INDEXSOURCE_COL_OVERVIEW_NAME_0));
        defEditAction.setHelpText(Messages.get().container(Messages.GUI_LIST_INDEXSOURCE_COL_OVERVIEW_NAME_HELP_0));
        nameCol.addDefaultAction(defEditAction);
        metadata.addColumn(nameCol);

        // add column for analyzer
        CmsListColumnDefinition analyzerCol = new CmsListColumnDefinition(LIST_COLUMN_INDEXER);
        analyzerCol.setAlign(CmsListColumnAlignEnum.ALIGN_LEFT);
        analyzerCol.setName(Messages.get().container(Messages.GUI_LIST_INDEXSOURCE_COL_INDEXER_0));
        analyzerCol.setWidth("45%");
        metadata.addColumn(analyzerCol);

    }

    /**
     * @see org.opencms.workplace.list.A_CmsListDialog#setIndependentActions(org.opencms.workplace.list.CmsListMetadata)
     */
    protected void setIndependentActions(CmsListMetadata metadata) {

        // add document types of index source detail help
        CmsListItemDetails doctypeDetails = new CmsListItemDetails(LIST_DETAIL_DOCTYPES);
        doctypeDetails.setAtColumn(LIST_COLUMN_NAME);
        doctypeDetails.setVisible(false);
        doctypeDetails.setShowActionName(Messages.get().container(Messages.GUI_LIST_INDEXSOURCE_DETAIL_DOCTYPE_SHOW_0));
        doctypeDetails.setShowActionHelpText(Messages.get().container(
            Messages.GUI_LIST_INDEXSOURCE_DETAIL_DOCTYPE_SHOW_HELP_0));
        doctypeDetails.setHideActionName(Messages.get().container(Messages.GUI_LIST_INDEXSOURCE_DETAIL_DOCTYPE_HIDE_0));
        doctypeDetails.setHideActionHelpText(Messages.get().container(
            Messages.GUI_LIST_INDEXSOURCE_DETAIL_DOCTYPE_HIDE_HELP_0));
        doctypeDetails.setName(Messages.get().container(Messages.GUI_LIST_INDEXSOURCE_DETAIL_DOCTYPE_NAME_0));
        doctypeDetails.setFormatter(new CmsListItemDetailsFormatter(Messages.get().container(
            Messages.GUI_LIST_INDEXSOURCE_DETAIL_DOCTYPE_NAME_0)));
        metadata.addItemDetails(doctypeDetails);

        // add resources of index source detail help
        CmsListItemDetails resourceDetails = new CmsListItemDetails(LIST_DETAIL_RESOURCES);
        resourceDetails.setAtColumn(LIST_COLUMN_NAME);
        resourceDetails.setVisible(false);
        resourceDetails.setShowActionName(Messages.get().container(Messages.GUI_LIST_INDEXSOURCE_DETAIL_RESOURCE_SHOW_0));
        resourceDetails.setShowActionHelpText(Messages.get().container(
            Messages.GUI_LIST_INDEXSOURCE_DETAIL_RESOURCE_SHOW_HELP_0));
        resourceDetails.setHideActionName(Messages.get().container(Messages.GUI_LIST_INDEXSOURCE_DETAIL_RESOURCE_HIDE_0));
        resourceDetails.setHideActionHelpText(Messages.get().container(
            Messages.GUI_LIST_INDEXSOURCE_DETAIL_RESOURCE_HIDE_HELP_0));
        resourceDetails.setName(Messages.get().container(Messages.GUI_LIST_INDEXSOURCE_DETAIL_RESOURCE_NAME_0));
        resourceDetails.setFormatter(new CmsListItemDetailsFormatter(Messages.get().container(
            Messages.GUI_LIST_INDEXSOURCE_DETAIL_RESOURCE_NAME_0)));
        metadata.addItemDetails(resourceDetails);

    }

    /**
     * @see org.opencms.workplace.list.A_CmsListDialog#setMultiActions(org.opencms.workplace.list.CmsListMetadata)
     */
    protected void setMultiActions(CmsListMetadata metadata) {

        // view only
    }

    /**
     * @see org.opencms.workplace.list.A_CmsListDialog#validateParamaters()
     */
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
        List docTypes = idxSource.getDocumentTypes();
        // output of found index sources
        Iterator itDocTypes = docTypes.iterator();
        CmsSearchDocumentType docType;
        html.append("<ul>\n");
        while (itDocTypes.hasNext()) {
            // get the instance (instead of plain name) for more detail in future... 
            docType = searchManager.getDocumentTypeConfig(itDocTypes.next().toString());
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
        List resources = idxSource.getResourcesNames();
        // output of found index sources
        Iterator itResources = resources.iterator();
        html.append("<ul>\n");
        while (itResources.hasNext()) {

            html.append("  <li>\n").append("  ").append(itResources.next().toString()).append("\n");
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
    private List searchIndexSources() {

        CmsSearchManager manager = OpenCms.getSearchManager();
        CmsSearchIndex index = manager.getIndex(getParamIndexName());
        List sources = index.getSources();
        return sources;
    }

}
