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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.jsp.PageContext;

import org.apache.commons.logging.Log;

/**
 * A list that displays the document types of the system that are assigned to
 * a request parameter given
 * <code>{@link org.opencms.search.CmsSearchIndexSource}</code> ("indexsource") and
 * offers single- and multi-actions that remove those document types to the current
 * indexsource.<p>
 *
 * This list is no stand-alone page but has to be embedded in another dialog
 * (see <code> {@link org.opencms.workplace.tools.searchindex.A_CmsEmbeddedListDialog}</code>. <p>
 *
 * @since 6.0.0
 */
public class CmsDocumentTypeRemoveList extends A_CmsEmbeddedListDialog {

    /** list action dummy id constant. */
    public static final String LIST_ACTION_NONE = "an";

    /** list action id constant. */
    public static final String LIST_ACTION_REMOVE_DOCTYPE = "ard";

    /** list action id constant. */
    public static final String LIST_ACTION_REMOVE_DOCTYPE2 = LIST_ACTION_REMOVE_DOCTYPE + "2";

    /** list column id constant. */
    public static final String LIST_COLUMN_DOCCLASS = "cdc";

    /** list column id constant. */
    public static final String LIST_COLUMN_ICON = "ci";

    /** list column id constant. */
    public static final String LIST_COLUMN_NAME = "cn";

    /** list column id constant. */
    public static final String LIST_COLUMN_REMOVE_DOCTYPE = "crd";

    /** list item detail id constant. */
    public static final String LIST_DETAIL_MIMETYPES = "dmt";

    /** list item detail id constant. */
    public static final String LIST_DETAIL_RESOURCETYPES = "drt";

    /** list id constant. */
    public static final String LIST_ID = "lssisdtr";

    /** list action id constant. */
    public static final String LIST_MACTION_REMOVE_DOCTYPE = "mard";

    /** The log object for this class. */
    private static final Log LOG = CmsLog.getLog(CmsDocumentTypeRemoveList.class);

    /** Stores the value of the request parameter for the search index source name. */
    private String m_paramIndexsource;

    /**
     * Public constructor.<p>
     *
     * @param jsp an initialized JSP action element
     */
    public CmsDocumentTypeRemoveList(CmsJspActionElement jsp) {

        this(jsp, LIST_ID, Messages.get().container(Messages.GUI_LIST_DOCUMENTTYPES_NAME_0));
    }

    /**
     * Public constructor.<p>
     *
     * @param jsp an initialized JSP action element
     * @param listId the id of the list
     * @param listName the list name
     */
    public CmsDocumentTypeRemoveList(CmsJspActionElement jsp, String listId, CmsMessageContainer listName) {

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
    public CmsDocumentTypeRemoveList(
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
    public CmsDocumentTypeRemoveList(PageContext context, HttpServletRequest req, HttpServletResponse res) {

        this(new CmsJspActionElement(context, req, res));
    }

    /**
     * @see org.opencms.workplace.list.A_CmsListDialog#executeListMultiActions()
     */
    @Override
    public void executeListMultiActions() {

        CmsSearchManager searchManager = OpenCms.getSearchManager();
        if (getParamListAction().equals(LIST_MACTION_REMOVE_DOCTYPE)) {
            // execute the delete multiaction
            Iterator<CmsListItem> itItems = getSelectedItems().iterator();
            CmsListItem listItem;
            String doctype;
            CmsSearchIndexSource idxsrc = searchManager.getIndexSource(getParamIndexsource());
            while (itItems.hasNext()) {
                listItem = itItems.next();
                doctype = (String)listItem.get(LIST_COLUMN_NAME);
                idxsrc.removeDocumentType(doctype);
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
    public void executeListSingleActions() {

        CmsSearchManager searchManager = OpenCms.getSearchManager();
        String action = getParamListAction();
        if (action.equals(LIST_ACTION_REMOVE_DOCTYPE) || action.equals(LIST_ACTION_REMOVE_DOCTYPE2)) {
            CmsSearchIndexSource idxsrc = searchManager.getIndexSource(getParamIndexsource());
            CmsListItem item = getSelectedItem();
            String doctypeName = (String)item.get(LIST_COLUMN_NAME);
            idxsrc.removeDocumentType(doctypeName);
            refreshList();
            writeConfiguration(false);
        }
    }

    /**
     * Returns the request parameter "indexsource".<p>
     *
     * @return the request parameter "indexsource"
     */
    public String getParamIndexsource() {

        return m_paramIndexsource;
    }

    /**
     * Sets the request parameter "indexsource". <p>
     *
     * Method intended for workplace-properietary automatic filling of
     * request parameter values to dialogs, not for manual invocation. <p>
     *
     * @param indexsource the request parameter "indexsource" to set
     */
    public void setParamIndexsource(String indexsource) {

        m_paramIndexsource = indexsource;
    }

    /**
     * @see org.opencms.workplace.list.A_CmsListDialog#fillDetails(java.lang.String)
     */
    @Override
    protected void fillDetails(String detailId) {

        // get content
        List<CmsListItem> items = getList().getAllContent();
        Iterator<CmsListItem> itItems = items.iterator();
        CmsListItem item;
        if (detailId.equals(LIST_DETAIL_MIMETYPES)) {
            while (itItems.hasNext()) {
                item = itItems.next();
                fillDetailMimetypes(item, detailId);

            }
        }
        if (detailId.equals(LIST_DETAIL_RESOURCETYPES)) {
            while (itItems.hasNext()) {
                item = itItems.next();
                fillDetailResourceTypes(item, detailId);

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
        List<CmsSearchDocumentType> doctypes = documentTypes();
        Iterator<CmsSearchDocumentType> itDoctypes = doctypes.iterator();
        CmsSearchDocumentType doctype;
        while (itDoctypes.hasNext()) {
            doctype = itDoctypes.next();
            CmsListItem item = getList().newItem(doctype.getName());
            item.set(LIST_COLUMN_NAME, doctype.getName());
            item.set(LIST_COLUMN_DOCCLASS, doctype.getClassName());
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

        // create dummy column for corporate design reasons
        CmsListColumnDefinition dummyCol = new CmsListColumnDefinition(LIST_COLUMN_ICON);
        dummyCol.setName(Messages.get().container(Messages.GUI_LIST_DOCUMENTTYPE_NAME_0));
        dummyCol.setHelpText(Messages.get().container(Messages.GUI_LIST_DOCUMENTTYPE_NAME_HELP_0));
        dummyCol.setWidth("20");
        dummyCol.setAlign(CmsListColumnAlignEnum.ALIGN_LEFT);
        dummyCol.setSorteable(false);
        // add dummy icon
        CmsListDirectAction dummyAction = new CmsListDirectAction(LIST_ACTION_NONE);
        dummyAction.setName(Messages.get().container(Messages.GUI_LIST_INDEXSOURCE_COL_ICON_NAME_0));
        dummyAction.setHelpText(Messages.get().container(Messages.GUI_LIST_INDEXSOURCE_COL_ICON_NAME_HELP_0));
        dummyAction.setIconPath(CmsDocumentTypeList.ICON_DOCTYPE);
        // disable!
        dummyAction.setEnabled(false);
        dummyCol.addDirectAction(dummyAction);
        // add it to the list definition
        metadata.addColumn(dummyCol);

        // add column for add single-action
        CmsListColumnDefinition remCol = new CmsListColumnDefinition(LIST_COLUMN_REMOVE_DOCTYPE);
        remCol.setName(Messages.get().container(Messages.GUI_LIST_INDEXSOURCE_COL_REMOVE_DOCTYPE_NAME_0));
        remCol.setHelpText(Messages.get().container(Messages.GUI_LIST_INDEXSOURCE_COL_REMOVE_DOCTYPE_NAME_HELP_0));
        remCol.setWidth("20");
        remCol.setAlign(CmsListColumnAlignEnum.ALIGN_LEFT);
        remCol.setSorteable(false);
        // add add action
        CmsListDirectAction remAction = new CmsListDirectAction(LIST_ACTION_REMOVE_DOCTYPE);
        remAction.setName(Messages.get().container(Messages.GUI_LIST_SEARCHINDEX_ACTION_REMOVE_DOCTYPE_NAME_0));
        remAction.setHelpText(Messages.get().container(Messages.GUI_LIST_INDEXSOURCE_COL_REMOVE_DOCTYPE_NAME_HELP_0));
        remAction.setIconPath(ICON_MINUS);
        remCol.addDirectAction(remAction);
        metadata.addColumn(remCol);

        // add column for name
        CmsListColumnDefinition nameCol = new CmsListColumnDefinition(LIST_COLUMN_NAME);
        nameCol.setAlign(CmsListColumnAlignEnum.ALIGN_LEFT);
        nameCol.setName(Messages.get().container(Messages.GUI_LIST_SEARCHINDEX_COL_NAME_0));
        nameCol.setWidth("50%");

        // add a duplicate action
        CmsListDefaultAction remAction2 = new CmsListDefaultAction(LIST_ACTION_REMOVE_DOCTYPE2);
        remAction2.setName(Messages.get().container(Messages.GUI_LIST_SEARCHINDEX_ACTION_REMOVE_DOCTYPE_NAME_0));
        remAction2.setHelpText(Messages.get().container(Messages.GUI_LIST_INDEXSOURCE_COL_REMOVE_DOCTYPE_NAME_HELP_0));
        nameCol.addDefaultAction(remAction2);
        metadata.addColumn(nameCol);

        // add column for document implementation class
        CmsListColumnDefinition docclassCol = new CmsListColumnDefinition(LIST_COLUMN_DOCCLASS);
        docclassCol.setAlign(CmsListColumnAlignEnum.ALIGN_LEFT);
        docclassCol.setName(Messages.get().container(Messages.GUI_LIST_DOCTYPE_COL_DOCCLASS_0));
        docclassCol.setWidth("45%");
        metadata.addColumn(docclassCol);

    }

    /**
     * @see org.opencms.workplace.list.A_CmsListDialog#setIndependentActions(org.opencms.workplace.list.CmsListMetadata)
     */
    @Override
    protected void setIndependentActions(CmsListMetadata metadata) {

        // add document types of index source detail help
        CmsListItemDetails mimetypeDetails = new CmsListItemDetails(LIST_DETAIL_MIMETYPES);
        mimetypeDetails.setAtColumn(LIST_COLUMN_NAME);
        mimetypeDetails.setVisible(false);
        mimetypeDetails.setShowActionName(Messages.get().container(Messages.GUI_LIST_DOCTYPE_DETAIL_MIMETYPES_SHOW_0));
        mimetypeDetails.setShowActionHelpText(
            Messages.get().container(Messages.GUI_LIST_DOCTYPE_DETAIL_MIMETYPES_SHOW_HELP_0));
        mimetypeDetails.setHideActionName(Messages.get().container(Messages.GUI_LIST_DOCTYPE_DETAIL_MIMETYPES_HIDE_0));
        mimetypeDetails.setHideActionHelpText(
            Messages.get().container(Messages.GUI_LIST_DOCTYPE_DETAIL_MIMETYPES_HIDE_HELP_0));
        mimetypeDetails.setName(Messages.get().container(Messages.GUI_LIST_DOCTYPE_DETAIL_MIMETYPES_NAME_0));
        mimetypeDetails.setFormatter(
            new CmsListItemDetailsFormatter(
                Messages.get().container(Messages.GUI_LIST_DOCTYPE_DETAIL_MIMETYPES_NAME_0)));
        metadata.addItemDetails(mimetypeDetails);

        // add resources of index source detail help
        CmsListItemDetails resourceDetails = new CmsListItemDetails(LIST_DETAIL_RESOURCETYPES);
        resourceDetails.setAtColumn(LIST_COLUMN_NAME);
        resourceDetails.setVisible(false);
        resourceDetails.setShowActionName(
            Messages.get().container(Messages.GUI_LIST_DOCTYPE_DETAIL_RESOURCETYPES_SHOW_0));
        resourceDetails.setShowActionHelpText(
            Messages.get().container(Messages.GUI_LIST_DOCTYPE_DETAIL_RESOURCETYPES_SHOW_HELP_0));
        resourceDetails.setHideActionName(
            Messages.get().container(Messages.GUI_LIST_DOCTYPE_DETAIL_RESOURCETYPES_HIDE_0));
        resourceDetails.setHideActionHelpText(
            Messages.get().container(Messages.GUI_LIST_DOCTYPE_DETAIL_RESOURCETYPES_HIDE_HELP_0));
        resourceDetails.setName(Messages.get().container(Messages.GUI_LIST_DOCTYPE_DETAIL_RESOURCETYPES_NAME_0));
        resourceDetails.setFormatter(
            new CmsListItemDetailsFormatter(
                Messages.get().container(Messages.GUI_LIST_DOCTYPE_DETAIL_RESOURCETYPES_NAME_0)));
        metadata.addItemDetails(resourceDetails);

    }

    /**
     * @see org.opencms.workplace.list.A_CmsListDialog#setMultiActions(org.opencms.workplace.list.CmsListMetadata)
     */
    @Override
    protected void setMultiActions(CmsListMetadata metadata) {

        // add add multi action
        CmsListMultiAction addMultiAction = new CmsListMultiAction(LIST_MACTION_REMOVE_DOCTYPE);
        addMultiAction.setName(Messages.get().container(Messages.GUI_LIST_SEARCHINDEX_MACTION_REMOVE_DOCTYPE_NAME_0));
        addMultiAction.setHelpText(
            Messages.get().container(Messages.GUI_LIST_SEARCHINDEX_MACTION_REMOVE_DOCTYPE_NAME_HELP_0));
        addMultiAction.setIconPath(ICON_MULTI_MINUS);
        metadata.addMultiAction(addMultiAction);
    }

    /**
     * @see org.opencms.workplace.list.A_CmsListDialog#validateParamaters()
     */
    @Override
    protected void validateParamaters() throws Exception {

        // test the needed parameters
        if (getParamIndexsource() == null) {
            throw new CmsIllegalStateException(Messages.get().container(
                Messages.ERR_SEARCHINDEX_EDIT_MISSING_PARAM_1,
                A_CmsEditIndexSourceDialog.PARAM_INDEXSOURCE));
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
     * Returns the systems configured document types that are assigned
     * to the current indexsource (those that may be removed).<p>
     *
     * @return the systems configured document types that are assigned
     *         to the current indexsource (those that may be removed)
     */
    private List<CmsSearchDocumentType> documentTypes() {

        CmsSearchManager manager = OpenCms.getSearchManager();
        CmsSearchIndexSource indexsource = manager.getIndexSource(getParamIndexsource());
        List<CmsSearchDocumentType> result;
        if (indexsource != null) {
            List<String> doctypeNames = indexsource.getDocumentTypes();

            // transform these mere names to real document types...
            result = new ArrayList<CmsSearchDocumentType>(doctypeNames.size());
            Iterator<String> it = doctypeNames.iterator();
            String doctypename = "";
            CmsSearchDocumentType doctype;
            while (it.hasNext()) {
                doctypename = it.next();
                if (doctypename != null) {
                    doctype = manager.getDocumentTypeConfig(doctypename);
                    if (doctype != null) {
                        result.add(doctype);
                    }
                }
            }
        } else {
            result = Collections.emptyList();
            if (LOG.isErrorEnabled()) {
                LOG.error(Messages.get().getBundle().key(Messages.ERR_SEARCHINDEX_EDIT_MISSING_PARAM_1, "indexsource"));
            }

        }
        return result;
    }

    /**
     * Fills details about configured mime types of the document type into the given item. <p>
     *
     * @param item the list item to fill
     * @param detailId the id for the detail to fill
     *
     */
    private void fillDetailMimetypes(CmsListItem item, String detailId) {

        CmsSearchManager searchManager = OpenCms.getSearchManager();
        StringBuffer html = new StringBuffer();

        String doctypeName = (String)item.get(LIST_COLUMN_NAME);
        CmsSearchDocumentType docType = searchManager.getDocumentTypeConfig(doctypeName);

        // output of mime types
        Iterator<String> itMimetypes = docType.getMimeTypes().iterator();
        html.append("<ul>\n");
        while (itMimetypes.hasNext()) {
            html.append("  <li>\n").append("  ").append(itMimetypes.next()).append("\n");
            html.append("  </li>");
        }

        html.append("</ul>\n");
        item.set(detailId, html.toString());
    }

    /**
     * Fills details about resource types of the document type into the given item. <p>
     *
     * @param item the list item to fill
     * @param detailId the id for the detail to fill
     *
     */
    private void fillDetailResourceTypes(CmsListItem item, String detailId) {

        CmsSearchManager searchManager = OpenCms.getSearchManager();
        StringBuffer html = new StringBuffer();

        String doctypeName = (String)item.get(LIST_COLUMN_NAME);
        CmsSearchDocumentType docType = searchManager.getDocumentTypeConfig(doctypeName);

        // output of resource types
        Iterator<String> itResourcetypes = docType.getResourceTypes().iterator();
        html.append("<ul>\n");
        while (itResourcetypes.hasNext()) {
            html.append("  <li>\n").append("  ").append(itResourcetypes.next()).append("\n");
            html.append("  </li>");
        }

        html.append("</ul>\n");
        item.set(detailId, html.toString());
    }
}
