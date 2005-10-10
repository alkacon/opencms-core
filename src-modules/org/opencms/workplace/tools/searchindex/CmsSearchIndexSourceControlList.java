/*
 * File   : $Source: /alkacon/cvs/opencms/src-modules/org/opencms/workplace/tools/searchindex/CmsSearchIndexSourceControlList.java,v $
 * Date   : $Date: 2005/10/10 10:53:19 $
 * Version: $Revision: 1.1.2.2 $
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
import org.opencms.main.CmsLog;
import org.opencms.main.OpenCms;
import org.opencms.search.CmsSearchDocumentType;
import org.opencms.search.CmsSearchIndexSource;
import org.opencms.search.CmsSearchManager;
import org.opencms.workplace.list.A_CmsListDialog;
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
 * A list that displays information about the <code>{@link org.opencms.search.CmsSearchIndexSource}</code> 
 * that are members of the <code>{@link org.opencms.search.CmsSearchIndex}</code> 
 * in the current request scope (param "searchindex").<p> 
 * 
 * Unlike <code>{@link org.opencms.workplace.tools.searchindex.CmsSearchIndexSourceList}</code> 
 * this list is stand-alone displayable (not to embed in another dialog) and 
 * offers single actions within the rows related to the current selected indexsource 
 * which has to be found by the <b>request parameter <code></code></b>.
 * 
 * @author Achim Westermann 
 * 
 * @version $Revision: 1.1.2.2 $
 * 
 * @since 6.0.0
 */
public class CmsSearchIndexSourceControlList extends A_CmsListDialog {

    /** list action dummy id constant. */
    public static final String LIST_ACTION_DELETE = "ade";

    /** list action dummy id constant. */
    public static final String LIST_ACTION_DOCUMENTS = "ado";

    /** list action dummy id constant. */
    public static final String LIST_ACTION_EDIT = "ae";

    /** list action dummy id constant. */
    public static final String LIST_ACTION_OVERVIEW_INDEXSOURCE = "aois";

    /** 
     * List action dummy id constant. <p> 
     * 
     * This is meant to be used for the same action as 
     * <code>{@link #LIST_ACTION_OVERVIEW_INDEXSOURCE}</code> but has to be used if 
     * within one list two columns shall trigger the same action...<p>
     **/
    public static final String LIST_ACTION_OVERVIEW_INDEXSOURCE2 = "aois2";

    /** list action dummy id constant. */
    public static final String LIST_ACTION_RESOURCES = "ar";

    /** list column id constant. */
    public static final String LIST_COLUMN_DELETE = "cde";

    /** list item detail id constant. */
    public static final String LIST_COLUMN_DOCUMENTS = "cdo";

    /** list column id constant. */
    public static final String LIST_COLUMN_EDIT = "ce";

    /** list column id constant. */
    public static final String LIST_COLUMN_INDEXER = "ca";

    /** list column id constant. */
    public static final String LIST_COLUMN_NAME = "cn";

    /** list column id constant. */
    public static final String LIST_COLUMN_RESOURCES = "cr";

    /** list item detail id constant. */
    public static final String LIST_DETAIL_DOCTYPES = "dd";

    /** list item detail id constant. */
    public static final String LIST_DETAIL_RESOURCES = "dr";

    /** list id constant. */
    public static final String LIST_ID = "lssisc";

    /** list action id constant. */
    public static final String LIST_MACTION_DELETESOURCE = "mad";

    /** The list icon for a folder resource of a indexsource-documenttype. **/
    protected static final String ICON_FOLDER = "tools/searchindex/icons/small/indexsource-resources.png";

    /** The log object for this class. */
    private static final Log LOG = CmsLog.getLog(CmsSearchIndexSourceControlList.class);

    /**
     * Public constructor.<p>
     * 
     * @param jsp an initialized JSP action element
     */
    public CmsSearchIndexSourceControlList(CmsJspActionElement jsp) {

        this(jsp, LIST_ID, Messages.get().container(Messages.GUI_LIST_INDEXSOURCES_NAME_0));
    }

    /**
     * Public constructor.<p>
     * 
     * @param jsp an initialized JSP action element
     * @param listId the id of the list
     * @param listName the list name
     */
    public CmsSearchIndexSourceControlList(CmsJspActionElement jsp, String listId, CmsMessageContainer listName) {

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
    public CmsSearchIndexSourceControlList(
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
    public CmsSearchIndexSourceControlList(PageContext context, HttpServletRequest req, HttpServletResponse res) {

        this(new CmsJspActionElement(context, req, res));
    }

    /**
     * @see org.opencms.workplace.list.A_CmsListDialog#executeListMultiActions()
     */
    public void executeListMultiActions() {

        CmsSearchManager searchManager = OpenCms.getSearchManager();
        if (getParamListAction().equals(LIST_MACTION_DELETESOURCE)) {
            // execute the delete multiaction
            Iterator itItems = getSelectedItems().iterator();
            CmsListItem listItem;
            CmsSearchIndexSource idxsource;
            while (itItems.hasNext()) {
                listItem = (CmsListItem)itItems.next();
                idxsource = searchManager.getIndexSource((String)listItem.get(LIST_COLUMN_NAME));
                searchManager.removeSearchIndexSource(idxsource);
                getList().removeItem(listItem.getId(), getLocale());
            }
            refreshList();
            writeConfiguration(false);
        }
        listSave();
    }

    /**
     * @throws ServletException
     * @throws IOException
     * @see org.opencms.workplace.list.A_CmsListDialog#executeListSingleActions()
     */
    public void executeListSingleActions() throws IOException, ServletException {

        String indexSource = getSelectedItem().getId();
        Map params = new HashMap();
        String action = getParamListAction();
        if (action.equals(LIST_ACTION_EDIT)) {
            // forward to the edit indexsource screen   
            params.put(A_CmsEditIndexSourceDialog.PARAM_INDEXSOURCE, indexSource);
            params.put(PARAM_STYLE, CmsToolDialog.STYLE_NEW);
            getToolManager().jspForwardTool(this, "/searchindex/indexsources/indexsource/edit", params);
        } else if (action.equals(LIST_ACTION_DELETE)) {
            // forward to the delete indexsource screen   
            params.put(A_CmsEditIndexSourceDialog.PARAM_INDEXSOURCE, indexSource);
            params.put(PARAM_ACTION, DIALOG_INITIAL);
            params.put(PARAM_STYLE, CmsToolDialog.STYLE_NEW);
            getToolManager().jspForwardTool(this, "/searchindex/indexsources/indexsource/delete", params);
        } else if (action.equals(LIST_ACTION_RESOURCES)) {
            // forward to the assign resources to indexsource screen   
            params.put(A_CmsEditIndexSourceDialog.PARAM_INDEXSOURCE, indexSource);
            params.put(PARAM_ACTION, DIALOG_INITIAL);
            params.put(PARAM_STYLE, CmsToolDialog.STYLE_NEW);
            getToolManager().jspForwardTool(this, "/searchindex/indexsources/indexsource/resources", params);
        } else if (action.equals(LIST_ACTION_DOCUMENTS)) {
            // forward to the assign document types to indexsource screen   
            params.put(A_CmsEditIndexSourceDialog.PARAM_INDEXSOURCE, indexSource);
            params.put(PARAM_ACTION, DIALOG_INITIAL);
            params.put(PARAM_STYLE, CmsToolDialog.STYLE_NEW);
            getToolManager().jspForwardTool(this, "/searchindex/indexsources/indexsource/doctypes", params);
        } else if (action.equals(LIST_ACTION_OVERVIEW_INDEXSOURCE)) {
            // forward to the index overview screen   
            params.put(A_CmsEditIndexSourceDialog.PARAM_INDEXSOURCE, indexSource);
            params.put(PARAM_ACTION, DIALOG_INITIAL);
            params.put(PARAM_STYLE, CmsToolDialog.STYLE_NEW);
            getToolManager().jspForwardTool(this, "/searchindex/indexsources/indexsource", params);
        }
        listSave();
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
        String value;
        while (itSources.hasNext()) {
            try {
                source = (CmsSearchIndexSource)itSources.next();
                // use "null" String and avoid list exception in gui.
                CmsListItem item = getList().newItem(source.getName());
                item.set(LIST_COLUMN_NAME, source.getName());
                try {
                    value = source.getIndexer().getClass().getName();
                } catch (Throwable f) {
                    value = "null";
                }
                item.set(LIST_COLUMN_INDEXER, value);
                result.add(item);
            } catch (Throwable g) {
                CmsMessageContainer msg = Messages.get().container(
                    Messages.LOG_ERR_LIST_ITEM_SKIPPED_2,
                    getList().getName().key(getLocale()),
                    "Name");
                if (LOG.isWarnEnabled()) {
                    LOG.warn(msg.key(getLocale()));
                }
            }
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
        CmsListColumnDefinition editCol = new CmsListColumnDefinition(LIST_COLUMN_EDIT);
        editCol.setName(Messages.get().container(Messages.GUI_LIST_INDEXSOURCE_COL_EDIT_NAME_0));
        editCol.setHelpText(Messages.get().container(Messages.GUI_LIST_INDEXSOURCE_COL_EDIT_NAME_HELP_0));
        editCol.setWidth("5");
        editCol.setAlign(CmsListColumnAlignEnum.ALIGN_LEFT);
        editCol.setSorteable(false);
        // add dummy icon
        CmsListDirectAction editAction = new CmsListDirectAction(LIST_ACTION_EDIT);
        editAction.setName(Messages.get().container(Messages.GUI_LIST_INDEXSOURCE_ACTION_EDIT_NAME_0));
        editAction.setHelpText(Messages.get().container(Messages.GUI_LIST_INDEXSOURCE_COL_EDIT_NAME_HELP_0));
        editAction.setIconPath(CmsSearchIndexList.LIST_ICON_INDEXSOURCE);
        editCol.addDirectAction(editAction);
        // add it to the list definition
        metadata.addColumn(editCol);

        // create column for resource assignment
        CmsListColumnDefinition resCol = new CmsListColumnDefinition(LIST_COLUMN_RESOURCES);
        resCol.setName(Messages.get().container(Messages.GUI_LIST_INDEXSOURCE_COL_RESOURCES_NAME_0));
        resCol.setHelpText(Messages.get().container(Messages.GUI_LIST_INDEXSOURCE_COL_RESOURCES_NAME_HELP_0));
        resCol.setWidth("5");
        resCol.setAlign(CmsListColumnAlignEnum.ALIGN_LEFT);
        resCol.setSorteable(false);
        // add resource icon
        CmsListDirectAction resAction = new CmsListDirectAction(LIST_ACTION_RESOURCES);
        resAction.setName(Messages.get().container(Messages.GUI_LIST_SEARCHINDEX_ACTION_RESOURCES_NAME_0));
        resAction.setHelpText(Messages.get().container(Messages.GUI_LIST_INDEXSOURCE_COL_RESOURCES_NAME_HELP_0));
        resAction.setIconPath(ICON_FOLDER);
        resCol.addDirectAction(resAction);
        // add it to the list definition
        metadata.addColumn(resCol);

        // create column for document type assignment
        CmsListColumnDefinition docCol = new CmsListColumnDefinition(LIST_COLUMN_DOCUMENTS);
        docCol.setName(Messages.get().container(Messages.GUI_LIST_INDEXSOURCE_COL_DOCUMENTS_NAME_0));
        docCol.setHelpText(Messages.get().container(Messages.GUI_LIST_INDEXSOURCE_COL_DOCUMENTS_NAME_HELP_0));
        docCol.setWidth("5");
        docCol.setAlign(CmsListColumnAlignEnum.ALIGN_LEFT);
        docCol.setSorteable(false);
        // add document type icon
        CmsListDirectAction docAction = new CmsListDirectAction(LIST_ACTION_DOCUMENTS);
        docAction.setName(Messages.get().container(Messages.GUI_LIST_SEARCHINDEX_ACTION_DOCUMENTS_NAME_0));
        docAction.setHelpText(Messages.get().container(Messages.GUI_LIST_INDEXSOURCE_COL_DOCUMENTS_NAME_HELP_0));
        docAction.setIconPath(CmsDocumentTypeList.ICON_DOCTYPE);
        docCol.addDirectAction(docAction);
        // add it to the list definition
        metadata.addColumn(docCol);

        // create column for deletion
        CmsListColumnDefinition deleteCol = new CmsListColumnDefinition(LIST_COLUMN_DELETE);
        deleteCol.setName(Messages.get().container(Messages.GUI_LIST_SEARCHINDEX_COL_DELETE_NAME_0));
        deleteCol.setHelpText(Messages.get().container(Messages.GUI_LIST_SEARCHINDEX_COL_DELETE_HELP_0));
        deleteCol.setWidth("10");
        deleteCol.setAlign(CmsListColumnAlignEnum.ALIGN_LEFT);
        deleteCol.setSorteable(false);
        // add delete action
        CmsListDirectAction deleteAction = new CmsListDirectAction(LIST_ACTION_DELETE);
        deleteAction.setName(Messages.get().container(Messages.GUI_LIST_SEARCHINDEX_ACTION_DELETE_NAME_0));
        deleteAction.setHelpText(Messages.get().container(Messages.GUI_LIST_SEARCHINDEX_ACTION_DELETE_HELP_0));
        // skipped as the following page will have to ask for confirmation again (and additionally check a constraint) 
        //        deleteAction.setConfirmationMessage(Messages.get().container(Messages.GUI_LIST_INDEXSOURCE_ACTION_DELETE_CONF_0));
        deleteAction.setIconPath(ICON_DELETE);
        deleteCol.addDirectAction(deleteAction);
        // add it to the list definition
        metadata.addColumn(deleteCol);

        // add column for name
        CmsListColumnDefinition nameCol = new CmsListColumnDefinition(LIST_COLUMN_NAME);
        nameCol.setAlign(CmsListColumnAlignEnum.ALIGN_LEFT);
        nameCol.setName(Messages.get().container(Messages.GUI_LIST_SEARCHINDEX_COL_NAME_0));
        nameCol.setWidth("50%");
        // add overview action
        CmsListDefaultAction defEditAction = new CmsListDefaultAction(LIST_ACTION_OVERVIEW_INDEXSOURCE);
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

        // add add multi action
        CmsListMultiAction deleteMultiAction = new CmsListMultiAction(LIST_MACTION_DELETESOURCE);
        deleteMultiAction.setName(Messages.get().container(Messages.GUI_LIST_INDEXSOURCE_MACTION_DELETESOURCE_NAME_0));
        deleteMultiAction.setHelpText(Messages.get().container(
            Messages.GUI_LIST_INDEXSOURCE_MACTION_DELETESOURCE_NAME_HELP_0));
        deleteMultiAction.setConfirmationMessage(Messages.get().container(
            Messages.GUI_LIST_INDEXSOURCE_MACTION_DELETESOURCE_CONF_0));
        deleteMultiAction.setIconPath(ICON_MULTI_MINUS);
        metadata.addMultiAction(deleteMultiAction);

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
        return new LinkedList(manager.getSearchIndexSources().values());

    }
}
