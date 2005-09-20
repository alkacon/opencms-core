/*
 * File   : $Source: /alkacon/cvs/opencms/src-modules/org/opencms/workplace/tools/searchindex/CmsDocumentTypeList.java,v $
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
import org.opencms.main.CmsLog;
import org.opencms.main.OpenCms;
import org.opencms.search.CmsSearchDocumentType;
import org.opencms.search.CmsSearchIndexSource;
import org.opencms.search.CmsSearchManager;
import org.opencms.workplace.list.CmsListColumnAlignEnum;
import org.opencms.workplace.list.CmsListColumnDefinition;
import org.opencms.workplace.list.CmsListDirectAction;
import org.opencms.workplace.list.CmsListItem;
import org.opencms.workplace.list.CmsListItemDetails;
import org.opencms.workplace.list.CmsListItemDetailsFormatter;
import org.opencms.workplace.list.CmsListMetadata;
import org.opencms.workplace.list.CmsListOrderEnum;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.jsp.PageContext;

import org.apache.commons.logging.Log;

/**
 * A list that displays the document types of a request parameter given 
 * <code>{@link org.opencms.search.CmsSearchIndexSource}</code> ("indexsource"). 
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
public class CmsDocumentTypeList extends A_CmsEmbeddedListDialog {

    /** list action dummy id constant. */
    public static final String LIST_ACTION_NONE = "an";

    /** list column id constant. */
    public static final String LIST_COLUMN_DOCCLASS = "cdc";

    /** list column id constant. */
    public static final String LIST_COLUMN_ICON = "ci";

    /** list column id constant. */
    public static final String LIST_COLUMN_NAME = "cn";

    /** list item detail id constant. */
    public static final String LIST_DETAIL_MIMETYPES = "dmt";

    /** list item detail id constant. */
    public static final String LIST_DETAIL_RESOURCETYPES = "drt";

    /** list id constant. */
    public static final String LIST_ID = "lssisdt";

    /** The list icon for a folder resource. **/
    protected static final String ICON_DOCTYPE = "tools/searchindex/icons/small/indexsource-doctype.png";

    /** The log object for this class. */
    private static final Log LOG = CmsLog.getLog(CmsDocumentTypeList.class);

    /** Stores the value of the request parameter for the search index source name. */
    private String m_paramIndexsource;

    /**
     * Public constructor.<p>
     * 
     * @param jsp an initialized JSP action element
     */
    public CmsDocumentTypeList(CmsJspActionElement jsp) {

        this(jsp, LIST_ID, Messages.get().container(Messages.GUI_LIST_DOCUMENTTYPES_NAME_0));
    }

    /**
     * Public constructor.<p>
     * 
     * @param jsp an initialized JSP action element
     * @param listId the id of the list
     * @param listName the list name
     */
    public CmsDocumentTypeList(CmsJspActionElement jsp, String listId, CmsMessageContainer listName) {

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
    public CmsDocumentTypeList(
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
    public CmsDocumentTypeList(PageContext context, HttpServletRequest req, HttpServletResponse res) {

        this(new CmsJspActionElement(context, req, res));
    }

    /**
     * @see org.opencms.workplace.list.A_CmsListDialog#executeListMultiActions()
     */
    public void executeListMultiActions() {

        // view only 
    }

    /**
     * @see org.opencms.workplace.list.A_CmsListDialog#executeListSingleActions()
     */
    public void executeListSingleActions() {

        // view only
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
    protected void fillDetails(String detailId) {

        // get content
        List items = getList().getAllContent();
        Iterator itItems = items.iterator();
        CmsListItem item;
        if (detailId.equals(LIST_DETAIL_MIMETYPES)) {
            while (itItems.hasNext()) {
                item = (CmsListItem)itItems.next();
                fillDetailMimetypes(item, detailId);

            }
        }
        if (detailId.equals(LIST_DETAIL_RESOURCETYPES)) {
            while (itItems.hasNext()) {
                item = (CmsListItem)itItems.next();
                fillDetailResourceTypes(item, detailId);

            }

        }

    }

    /**
     * @see org.opencms.workplace.list.A_CmsListDialog#getListItems()
     */
    protected List getListItems() {

        List result = new ArrayList();
        // get content
        List doctypes = documentTypes();
        Iterator itDoctypes = doctypes.iterator();
        CmsSearchDocumentType doctype;
        while (itDoctypes.hasNext()) {
            doctype = (CmsSearchDocumentType)itDoctypes.next();
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

        // add column for name
        CmsListColumnDefinition nameCol = new CmsListColumnDefinition(LIST_COLUMN_NAME);
        nameCol.setAlign(CmsListColumnAlignEnum.ALIGN_LEFT);
        nameCol.setName(Messages.get().container(Messages.GUI_LIST_SEARCHINDEX_COL_NAME_0));
        nameCol.setWidth("50%");
        metadata.addColumn(nameCol);

        // add column for document implementation class 
        CmsListColumnDefinition docclassCol = new CmsListColumnDefinition(LIST_COLUMN_DOCCLASS);
        docclassCol.setAlign(CmsListColumnAlignEnum.ALIGN_LEFT);
        docclassCol.setName(Messages.get().container(Messages.GUI_LIST_DOCTYPE_COL_DOCCLASS_0));
        docclassCol.setWidth("50%");
        metadata.addColumn(docclassCol);

    }

    /**
     * @see org.opencms.workplace.list.A_CmsListDialog#setIndependentActions(org.opencms.workplace.list.CmsListMetadata)
     */
    protected void setIndependentActions(CmsListMetadata metadata) {

        // add document types of index source detail help
        CmsListItemDetails mimetypeDetails = new CmsListItemDetails(LIST_DETAIL_MIMETYPES);
        mimetypeDetails.setAtColumn(LIST_COLUMN_NAME);
        mimetypeDetails.setVisible(false);
        mimetypeDetails.setShowActionName(Messages.get().container(Messages.GUI_LIST_DOCTYPE_DETAIL_MIMETYPES_SHOW_0));
        mimetypeDetails.setShowActionHelpText(Messages.get().container(
            Messages.GUI_LIST_DOCTYPE_DETAIL_MIMETYPES_SHOW_HELP_0));
        mimetypeDetails.setHideActionName(Messages.get().container(Messages.GUI_LIST_DOCTYPE_DETAIL_MIMETYPES_HIDE_0));
        mimetypeDetails.setHideActionHelpText(Messages.get().container(
            Messages.GUI_LIST_DOCTYPE_DETAIL_MIMETYPES_HIDE_HELP_0));
        mimetypeDetails.setName(Messages.get().container(Messages.GUI_LIST_DOCTYPE_DETAIL_MIMETYPES_NAME_0));
        mimetypeDetails.setFormatter(new CmsListItemDetailsFormatter(Messages.get().container(
            Messages.GUI_LIST_DOCTYPE_DETAIL_MIMETYPES_NAME_0)));
        metadata.addItemDetails(mimetypeDetails);

        // add resources of index source detail help
        CmsListItemDetails resourceDetails = new CmsListItemDetails(LIST_DETAIL_RESOURCETYPES);
        resourceDetails.setAtColumn(LIST_COLUMN_NAME);
        resourceDetails.setVisible(false);
        resourceDetails.setShowActionName(Messages.get().container(
            Messages.GUI_LIST_DOCTYPE_DETAIL_RESOURCETYPES_SHOW_0));
        resourceDetails.setShowActionHelpText(Messages.get().container(
            Messages.GUI_LIST_DOCTYPE_DETAIL_RESOURCETYPES_SHOW_HELP_0));
        resourceDetails.setHideActionName(Messages.get().container(
            Messages.GUI_LIST_DOCTYPE_DETAIL_RESOURCETYPES_HIDE_0));
        resourceDetails.setHideActionHelpText(Messages.get().container(
            Messages.GUI_LIST_DOCTYPE_DETAIL_RESOURCETYPES_HIDE_HELP_0));
        resourceDetails.setName(Messages.get().container(Messages.GUI_LIST_DOCTYPE_DETAIL_RESOURCETYPES_NAME_0));
        resourceDetails.setFormatter(new CmsListItemDetailsFormatter(Messages.get().container(
            Messages.GUI_LIST_DOCTYPE_DETAIL_RESOURCETYPES_NAME_0)));
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
     * Returns the configured document types of the current indexsource. 
     * 
     * @return the configured document types of the current indexsource
     */
    private List documentTypes() {

        CmsSearchManager manager = OpenCms.getSearchManager();
        CmsSearchIndexSource indexsource = manager.getIndexSource(getParamIndexsource());
        List result;
        if (indexsource != null) {
            List doctypes = indexsource.getDocumentTypes();
            // transform these mere names to real document types... 
            result = new ArrayList(doctypes.size());
            Iterator it = doctypes.iterator();
            String doctypename = "";
            CmsSearchDocumentType doctype;
            while (it.hasNext()) {
                doctypename = (String)it.next();
                if (doctypename != null) {
                    doctype = manager.getDocumentTypeConfig(doctypename);
                    if (doctype != null) {
                        result.add(doctype);
                    }
                }
            }
        } else {
            result = new ArrayList(0);
            if (LOG.isErrorEnabled()) {
                LOG.error(Messages.get().key(
                    getLocale(),
                    Messages.ERR_SEARCHINDEX_EDIT_MISSING_PARAM_1,
                    new Object[] {A_CmsEditIndexSourceDialog.PARAM_INDEXSOURCE}));
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
        Iterator itMimetypes = docType.getMimeTypes().iterator();
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
        Iterator itResourcetypes = docType.getResourceTypes().iterator();
        html.append("<ul>\n");
        while (itResourcetypes.hasNext()) {
            html.append("  <li>\n").append("  ").append(itResourcetypes.next()).append("\n");
            html.append("  </li>");
        }

        html.append("</ul>\n");
        item.set(detailId, html.toString());
    }

}
