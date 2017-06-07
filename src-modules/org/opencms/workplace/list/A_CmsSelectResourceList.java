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

package org.opencms.workplace.list;

import org.opencms.db.CmsUserSettings;
import org.opencms.i18n.CmsMessageContainer;
import org.opencms.jsp.CmsJspActionElement;
import org.opencms.util.CmsStringUtil;
import org.opencms.util.CmsUUID;
import org.opencms.workplace.CmsWorkplace;
import org.opencms.workplace.CmsWorkplaceSettings;
import org.opencms.workplace.explorer.CmsNewResourceXmlContent;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.jsp.JspException;

/**
 * A base list dialog to select a resource.<p>
 *
 * This dialog can be used as part of a wizard based dialog by forwarding to it and after the selection switching back
 * to the wizard. Necessary request parameters have to be passed through.<p>
 *
 * @since 6.5.6
 */
public abstract class A_CmsSelectResourceList extends A_CmsListExplorerDialog {

    /** Constant for the "Finish" button in the build button methods. */
    public static final int BUTTON_FINISH = 30;

    /** Constant for the "Next" button in the build button methods. */
    public static final int BUTTON_NEXT = 20;

    /** List column id constant. */
    public static final String LIST_COLUMN_SELECT = "cs";

    /** List independent action id constant. */
    public static final String LIST_RACTION_SEL = "rs";

    /**
     * Creates a new select resource list ordered and searchable by name.<p>
     *
     * @param jsp an initialized JSP action element
     * @param listId the id of the displayed list
     * @param listName the name of the list
     */
    protected A_CmsSelectResourceList(CmsJspActionElement jsp, String listId, CmsMessageContainer listName) {

        super(jsp, listId, listName);
        // hide print button
        getList().getMetadata().getIndependentAction(CmsListPrintIAction.LIST_ACTION_ID).setVisible(false);
        // suppress the box around the list
        getList().setBoxed(false);
    }

    /**
     * Default constructor.<p>
     *
     * @param jsp an initialized JSP action element
     * @param listId the id of the displayed list
     * @param listName the name of the list
     * @param sortedColId the a priory sorted column
     * @param sortOrder the order of the sorted column
     * @param searchableColId the column to search into
     */
    protected A_CmsSelectResourceList(
        CmsJspActionElement jsp,
        String listId,
        CmsMessageContainer listName,
        String sortedColId,
        CmsListOrderEnum sortOrder,
        String searchableColId) {

        super(jsp, listId, listName, sortedColId, sortOrder, searchableColId);
        // hide print button
        getList().getMetadata().getIndependentAction(CmsListPrintIAction.LIST_ACTION_ID).setVisible(false);
        // suppress the box around the list
        getList().setBoxed(false);
    }

    /**
     * Override this to set additional parameters before forwarding or to change the forward target.<p>
     *
     * Usually you have to set the "action" parameter to another value before forwarding.<p>
     *
     * @see org.opencms.workplace.list.A_CmsListDialog#actionDialog()
     */
    @Override
    public void actionDialog() throws JspException, ServletException, IOException {

        if (getAction() == ACTION_CONTINUE) {
            sendForward(nextUrl(), paramsAsParameterMap());
            return;
        }
        super.actionDialog();
    }

    /**
     * Builds a default button row with a continue and cancel button.<p>
     *
     * Override this to have special buttons for your dialog.<p>
     *
     * @return the button row
     */
    public String dialogButtons() {

        return dialogButtons(new int[] {BUTTON_CONTINUE, BUTTON_CANCEL}, new String[] {"", ""});
    }

    /**
     * @see org.opencms.workplace.list.A_CmsListDialog#executeListMultiActions()
     */
    @Override
    public void executeListMultiActions() {

        // no multi actions present
    }

    /**
     * @see org.opencms.workplace.list.A_CmsListDialog#executeListSingleActions()
     */
    @Override
    public void executeListSingleActions() {

        // no single actions present
    }

    /**
     * Returns the resource name of the selected resource.<p>
     *
     * @return the resource name of the selected resource or null if no resource was selected
     */
    public String getSelectedResourceName() {

        String resParam = getJsp().getRequest().getParameter(getListId() + LIST_RACTION_SEL);
        if (CmsStringUtil.isNotEmpty(resParam)) {
            CmsListItem item = getList().getItem(resParam);
            return (String)item.get(LIST_COLUMN_NAME);
        }
        return null;
    }

    /**
     * @see org.opencms.workplace.list.A_CmsListDialog#displayDialog()
     */
    @Override
    public void displayDialog() throws JspException, IOException, ServletException {

        getList().setShowTitle(false);
        super.displayDialog();
    }

    /**
     * Returns the title of the list to display.<p>
     *
     * @return the title of the list to display
     */
    public abstract String getListTitle();

    /**
     * Returns the url to forward the parameters after selection.<p>
     *
     * @return the url to forward the parameters after selection
     */
    public abstract String nextUrl();

    /**
     * @see org.opencms.workplace.list.A_CmsListDialog#customHtmlEnd()
     */
    @Override
    protected String customHtmlEnd() {

        StringBuffer result = new StringBuffer(256);

        result.append(dialogWhiteBoxEnd());
        result.append(dialogBlockEnd());
        result.append(dialogSpacer());
        result.append(dialogButtons());
        result.append(super.customHtmlEnd());
        return result.toString();
    }

    /**
     * @see org.opencms.workplace.list.A_CmsListDialog#customHtmlStart()
     */
    @Override
    protected String customHtmlStart() {

        StringBuffer result = new StringBuffer(256);
        result.append("<script type='text/javascript' src='");
        result.append(CmsWorkplace.getSkinUri());
        result.append("admin/javascript/general.js'></script>\n");
        result.append("<script type='text/javascript' src='");
        result.append(CmsWorkplace.getSkinUri());
        result.append("editors/xmlcontent/help.js'></script>\n");
        result.append(dialogBlockStart(getListTitle()));
        result.append(dialogWhiteBoxStart());
        return result.toString();
    }

    /**
     * @see org.opencms.workplace.CmsDialog#dialogButtonsHtml(java.lang.StringBuffer, int, java.lang.String)
     */
    @Override
    protected void dialogButtonsHtml(StringBuffer result, int button, String attribute) {

        attribute = appendDelimiter(attribute);

        switch (button) {
            case BUTTON_FINISH:
                result.append("<input name='" + DIALOG_CONTINUE + "' type=\"button\" value=\"");
                result.append(key(org.opencms.workplace.explorer.Messages.GUI_BUTTON_ENDWIZARD_0) + "\"");
                if (attribute.toLowerCase().indexOf("onclick") == -1) {
                    result.append(" onclick=\"submitAction('" + DIALOG_CONTINUE + "', form, '");
                    result.append(getListId());
                    result.append("-form');\"");
                }
                result.append("\" class=\"dialogbutton\"");
                result.append(attribute);
                result.append(">\n");
                break;
            case BUTTON_NEXT:
                result.append("<input name='" + DIALOG_CONTINUE + "' type=\"button\" value=\"");
                result.append(key(org.opencms.workplace.explorer.Messages.GUI_BUTTON_NEXTSCREEN_0) + "\"");
                if (attribute.toLowerCase().indexOf("onclick") == -1) {
                    result.append(" onclick=\"submitAction('" + DIALOG_CONTINUE + "', form, '");
                    result.append(getListId());
                    result.append("-form');\"");
                }
                result.append("\" class=\"dialogbutton\"");
                result.append(attribute);
                result.append(">\n");
                break;
            case BUTTON_CANCEL:
                result.append("<input name='" + DIALOG_CANCEL + "' type=\"button\" value=\"");
                result.append(key(org.opencms.workplace.Messages.GUI_DIALOG_BUTTON_CANCEL_0) + "\"");
                if (attribute.toLowerCase().indexOf("onclick") == -1) {
                    result.append(" onclick=\"submitAction('" + DIALOG_CANCEL + "', form, '");
                    result.append(getListId());
                    result.append("-form');\"");
                }
                result.append(" class=\"dialogbutton\"");
                result.append(attribute);
                result.append(">\n");
                break;
            default:
                super.dialogButtonsHtml(result, button, attribute);
        }
    }

    /**
     * @see org.opencms.workplace.list.A_CmsListDialog#fillDetails(java.lang.String)
     */
    @Override
    protected void fillDetails(String detailId) {

        // no details
    }

    /**
     * @see org.opencms.workplace.list.A_CmsListExplorerDialog#initWorkplaceRequestValues(org.opencms.workplace.CmsWorkplaceSettings, javax.servlet.http.HttpServletRequest)
     */
    @Override
    protected void initWorkplaceRequestValues(CmsWorkplaceSettings settings, HttpServletRequest request) {

        super.initWorkplaceRequestValues(settings, request);
        if (DIALOG_CONTINUE.equals(getParamAction())) {
            setAction(ACTION_CONTINUE);
        }
    }

    /**
     * The following columns are visible by default: type icon, resource name, title and last modification date.<p>
     *
     * Override this to set different column visibilities.<p>
     *
     * @see org.opencms.workplace.list.A_CmsListExplorerDialog#isColumnVisible(int)
     */
    @Override
    protected boolean isColumnVisible(int colFlag) {

        if (colFlag == LIST_COLUMN_TYPEICON.hashCode()) {
            return true;
        }
        if (colFlag == LIST_COLUMN_NAME.hashCode()) {
            return true;
        }
        if (colFlag == CmsUserSettings.FILELIST_TITLE) {
            return true;
        }
        if (colFlag == CmsUserSettings.FILELIST_DATE_LASTMODIFIED) {
            return true;
        }
        return false;
    }

    /**
     * @see org.opencms.workplace.list.A_CmsListExplorerDialog#setColumns(org.opencms.workplace.list.CmsListMetadata)
     */
    @Override
    protected void setColumns(CmsListMetadata metadata) {

        // create column for radio button
        CmsListColumnDefinition radioSelCol = new CmsListColumnDefinition(LIST_COLUMN_SELECT);
        radioSelCol.setName(Messages.get().container(Messages.GUI_EXPLORER_LIST_COLS_SELECT_0));
        radioSelCol.setWidth("20");
        radioSelCol.setAlign(CmsListColumnAlignEnum.ALIGN_CENTER);
        radioSelCol.setSorteable(false);

        // add item selection action to column
        CmsListItemSelectionAction selAction = new CmsListItemSelectionAction(LIST_RACTION_SEL, null);
        selAction.setName(Messages.get().container(Messages.GUI_EXPLORER_LIST_COLS_SELECT_HELP_0));
        selAction.setEnabled(true);
        selAction.setSelectedItemId(CmsUUID.getConstantUUID(CmsNewResourceXmlContent.VALUE_NONE + "s").toString());
        radioSelCol.addDirectAction(selAction);

        // add the column at first position
        metadata.addColumn(radioSelCol);

        // add the other columns
        super.setColumns(metadata);
    }

    /**
     * @see org.opencms.workplace.list.A_CmsListDialog#setMultiActions(org.opencms.workplace.list.CmsListMetadata)
     */
    @Override
    protected void setMultiActions(CmsListMetadata metadata) {

        // no multi action present
    }
}
