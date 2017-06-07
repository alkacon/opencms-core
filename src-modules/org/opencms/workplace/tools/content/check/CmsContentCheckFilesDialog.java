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

package org.opencms.workplace.tools.content.check;

import org.opencms.file.CmsResource;
import org.opencms.jsp.CmsJspActionElement;
import org.opencms.workplace.list.A_CmsListExplorerDialog;
import org.opencms.workplace.list.CmsListItem;
import org.opencms.workplace.list.CmsListItemDetails;
import org.opencms.workplace.list.CmsListItemDetailsFormatter;
import org.opencms.workplace.list.CmsListMetadata;
import org.opencms.workplace.list.I_CmsListResourceCollector;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.jsp.PageContext;

/**
 * Result List Dialog.<p>
 *
 * @since 6.1.2
 */
public class CmsContentCheckFilesDialog extends A_CmsListExplorerDialog {

    /** List detail error. */
    public static final String LIST_DETAIL_ERROR = "de";

    /** List detail warning. */
    public static final String LIST_DETAIL_WARNING = "dw";

    /** list id constant. */
    public static final String LIST_ID = "checkcontent";

    /** The results of the content check. */
    CmsContentCheckResult m_results;

    /** The internal collector instance. */
    private I_CmsListResourceCollector m_collector;

    /**
     * Public constructor.<p>
     *
     * @param jsp an initialized JSP action element
     */
    public CmsContentCheckFilesDialog(CmsJspActionElement jsp) {

        super(jsp, LIST_ID, Messages.get().container(Messages.GUI_CHECKCONTENT_LIST_NAME_0));
    }

    /**
     * Public constructor with JSP variables.<p>
     *
     * @param context the JSP page context
     * @param req the JSP request
     * @param res the JSP response
     */
    public CmsContentCheckFilesDialog(PageContext context, HttpServletRequest req, HttpServletResponse res) {

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
    public void executeListSingleActions() {

        throwListUnsupportedActionException();
    }

    /**
     * @see org.opencms.workplace.list.A_CmsListExplorerDialog#getCollector()
     */
    @Override
    public I_CmsListResourceCollector getCollector() {

        if (m_collector == null) {
            // get the content check result object
            Map objects = (Map)getSettings().getDialogObject();
            Object o = objects.get(CmsContentCheckDialog.class.getName());
            if ((o != null) && (o instanceof CmsContentCheck)) {
                m_results = ((CmsContentCheck)o).getResults();
            } else {
                m_results = new CmsContentCheckResult();
            }
            m_collector = new CmsContentCheckCollector(this, m_results);
        }
        return m_collector;
    }

    /**
     * @see org.opencms.workplace.list.A_CmsListDialog#fillDetails(java.lang.String)
     */
    @Override
    protected void fillDetails(String detailId) {

        // get content
        List resourceNames = getList().getAllContent();
        Iterator i = resourceNames.iterator();
        while (i.hasNext()) {
            CmsListItem item = (CmsListItem)i.next();
            CmsResource res = getCollector().getResource(getCms(), item);
            // check if errors are enabled
            StringBuffer html = new StringBuffer();
            // error detail is enabled
            if (detailId.equals(LIST_DETAIL_ERROR)) {
                // get all errors for this resource and show them
                List errors = m_results.getErrors(res.getRootPath());
                if (errors != null) {
                    Iterator j = errors.iterator();
                    while (j.hasNext()) {
                        String errorMessage = (String)j.next();
                        html.append(errorMessage);
                        html.append("<br>");
                    }
                    item.set(detailId, html.toString());
                }
            }
            // warning detail is enabled
            if (detailId.equals(LIST_DETAIL_WARNING)) {
                // get all warnings for this resource and show them
                List warnings = m_results.getWarnings(res.getRootPath());
                if (warnings != null) {
                    Iterator j = warnings.iterator();
                    while (j.hasNext()) {
                        String warningsMessage = (String)j.next();
                        html.append(warningsMessage);
                        html.append("<br>");
                    }
                    item.set(detailId, html.toString());
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
        addMessages(org.opencms.workplace.tools.content.Messages.get().getBundleName());
        addMessages(Messages.get().getBundleName());
        super.initMessages();
    }

    /**
     * @see org.opencms.workplace.list.A_CmsListDialog#setIndependentActions(org.opencms.workplace.list.CmsListMetadata)
     */
    @Override
    protected void setIndependentActions(CmsListMetadata metadata) {

        // create list item detail for errors
        CmsListItemDetails errorDetails = new CmsListItemDetails(LIST_DETAIL_ERROR);
        errorDetails.setAtColumn(LIST_COLUMN_NAME);
        //errorDetails.setVisible(false);
        errorDetails.setShowActionName(
            Messages.get().container(Messages.GUI_CHECKCONTENT_DETAIL_SHOW_ERRORINFO_NAME_0));
        errorDetails.setShowActionHelpText(
            Messages.get().container(Messages.GUI_CHECKCONTENT_DETAIL_SHOW_ERRORINFO_HELP_0));
        errorDetails.setHideActionName(
            Messages.get().container(Messages.GUI_CHECKCONTENT_DETAIL_HIDE_ERRORINFO_NAME_0));
        errorDetails.setHideActionHelpText(
            Messages.get().container(Messages.GUI_CHECKCONTENT_DETAIL_HIDE_ERRORINFO_HELP_0));
        errorDetails.setFormatter(
            new CmsListItemDetailsFormatter(Messages.get().container(Messages.GUI_CHECKCONTENT_LABEL_ERROR_0)));

        // add error info item detail to meta data
        metadata.addItemDetails(errorDetails);

        // create list item detail for warnings
        CmsListItemDetails warningDetails = new CmsListItemDetails(LIST_DETAIL_WARNING);
        warningDetails.setAtColumn(LIST_COLUMN_NAME);
        //warningDetails.setVisible(false);
        warningDetails.setShowActionName(
            Messages.get().container(Messages.GUI_CHECKCONTENT_DETAIL_SHOW_WARNINGINFO_NAME_0));
        warningDetails.setShowActionHelpText(
            Messages.get().container(Messages.GUI_CHECKCONTENT_DETAIL_SHOW_WARNINGINFO_NAME_0));
        warningDetails.setHideActionName(
            Messages.get().container(Messages.GUI_CHECKCONTENT_DETAIL_SHOW_WARNINGINFO_NAME_0));
        warningDetails.setHideActionHelpText(
            Messages.get().container(Messages.GUI_CHECKCONTENT_DETAIL_HIDE_WARNINGINFO_HELP_0));
        warningDetails.setFormatter(
            new CmsListItemDetailsFormatter(Messages.get().container(Messages.GUI_CHECKCONTENT_LABEL_WARNING_0)));

        // add warning info item detail to meta data
        metadata.addItemDetails(warningDetails);

        super.setIndependentActions(metadata);
    }

    /**
     * @see org.opencms.workplace.list.A_CmsListDialog#setMultiActions(org.opencms.workplace.list.CmsListMetadata)
     */
    @Override
    protected void setMultiActions(CmsListMetadata metadata) {

        // no LMA
    }

}