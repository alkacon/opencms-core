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

import org.opencms.jsp.CmsJspActionElement;
import org.opencms.main.CmsException;
import org.opencms.main.CmsRuntimeException;
import org.opencms.relations.CmsCategory;
import org.opencms.workplace.list.CmsListColumnAlignEnum;
import org.opencms.workplace.list.CmsListColumnDefinition;
import org.opencms.workplace.list.CmsListDirectAction;
import org.opencms.workplace.list.CmsListItem;
import org.opencms.workplace.list.CmsListMetadata;

import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.jsp.PageContext;

/**
 * Resource categories view.<p>
 *
 * @since 6.9.2
 */
public class CmsResourceCategoriesList extends A_CmsResourceCategoriesList {

    /** list action id constant. */
    public static final String LIST_ACTION_REMOVE1 = "ar1";

    /** list action id constant. */
    public static final String LIST_ACTION_REMOVE2 = "ar2";

    /** list id constant. */
    public static final String LIST_ID = "lrc";

    /** list action id constant. */
    public static final String LIST_MACTION_REMOVE = "mr";

    /**
     * Public constructor.<p>
     *
     * @param jsp an initialized JSP action element
     */
    public CmsResourceCategoriesList(CmsJspActionElement jsp) {

        this(jsp, LIST_ID);
    }

    /**
     * Public constructor with JSP variables.<p>
     *
     * @param context the JSP page context
     * @param req the JSP request
     * @param res the JSP response
     */
    public CmsResourceCategoriesList(PageContext context, HttpServletRequest req, HttpServletResponse res) {

        this(new CmsJspActionElement(context, req, res));
    }

    /**
     * Protected constructor.<p>
     * @param jsp an initialized JSP action element
     * @param listId the id of the specialized list
     */
    protected CmsResourceCategoriesList(CmsJspActionElement jsp, String listId) {

        super(jsp, listId, Messages.get().container(Messages.GUI_RESOURCECATEGORIES_LIST_NAME_0), true);
    }

    /**
     * @see org.opencms.workplace.list.A_CmsListDialog#executeListSingleActions()
     */
    @Override
    public void executeListSingleActions() throws CmsRuntimeException {

        if (getParamListAction().equals(LIST_ACTION_REMOVE1) || getParamListAction().equals(LIST_ACTION_REMOVE2)) {
            try {
                // lock resource if autolock is enabled
                checkLock(getParamResource());

                CmsListItem listItem = getSelectedItem();
                String categoryPath = listItem.getId();
                getCategoryService().removeResourceFromCategory(getCms(), getParamResource(), categoryPath);
                getCategoryService().repairRelations(getCms(), getParamResource());
            } catch (CmsException e) {
                throw new CmsRuntimeException(e.getMessageContainer(), e);
            }
        } else {
            throwListUnsupportedActionException();
        }
        listSave();
    }

    /**
     * @see org.opencms.workplace.list.A_CmsListDialog#defaultActionHtmlStart()
     */
    @Override
    protected String defaultActionHtmlStart() {

        return getList().listJs() + dialogContentStart(getParamTitle());
    }

    /**
     * @see org.opencms.workplace.commons.A_CmsResourceCategoriesList#getCategories()
     */
    @Override
    protected List<CmsCategory> getCategories() throws CmsException {

        return getResourceCategories();
    }

    /**
     * @see org.opencms.workplace.commons.A_CmsResourceCategoriesList#setStateActionCol(org.opencms.workplace.list.CmsListMetadata)
     */
    @Override
    protected void setStateActionCol(CmsListMetadata metadata) {

        // create column for state change
        CmsListColumnDefinition stateCol = new CmsListColumnDefinition(LIST_COLUMN_STATE);
        stateCol.setName(Messages.get().container(Messages.GUI_CATEGORIES_LIST_COLS_STATE_0));
        stateCol.setHelpText(Messages.get().container(Messages.GUI_CATEGORIES_LIST_COLS_STATE_HELP_0));
        stateCol.setWidth("20");
        stateCol.setAlign(CmsListColumnAlignEnum.ALIGN_CENTER);
        stateCol.setSorteable(false);
        // add remove action with confirmation message
        CmsListDirectAction stateAction1 = new CmsListDirectAction(LIST_ACTION_REMOVE1) {

            /**
             * @see org.opencms.workplace.tools.A_CmsHtmlIconButton#isVisible()
             */
            @Override
            public boolean isVisible() {

                if ((getItem() == null) || ((Boolean)getItem().get(LIST_COLUMN_LEAFS)).booleanValue()) {
                    return false;
                }
                return true;
            }
        };
        stateAction1.setName(Messages.get().container(Messages.GUI_CATEGORIES_LIST_DEFACTION_REMOVE_NAME_0));
        stateAction1.setHelpText(Messages.get().container(Messages.GUI_CATEGORIES_LIST_DEFACTION_REMOVE_HELP_0));
        stateAction1.setIconPath(ICON_MINUS);
        stateAction1.setConfirmationMessage(
            Messages.get().container(Messages.GUI_CATEGORIES_LIST_DEFACTION_REMOVE_CONF_MORE_0));
        stateCol.addDirectAction(stateAction1);
        // add remove action without confirmation message
        CmsListDirectAction stateAction2 = new CmsListDirectAction(LIST_ACTION_REMOVE2) {

            /**
             * @see org.opencms.workplace.tools.A_CmsHtmlIconButton#isVisible()
             */
            @Override
            public boolean isVisible() {

                if ((getItem() == null) || ((Boolean)getItem().get(LIST_COLUMN_LEAFS)).booleanValue()) {
                    return true;
                }
                return false;
            }
        };
        stateAction2.setName(Messages.get().container(Messages.GUI_CATEGORIES_LIST_DEFACTION_REMOVE_NAME_0));
        stateAction2.setHelpText(Messages.get().container(Messages.GUI_CATEGORIES_LIST_DEFACTION_REMOVE_HELP_0));
        stateAction2.setIconPath(ICON_MINUS);
        stateCol.addDirectAction(stateAction2);
        // add it to the list definition
        metadata.addColumn(stateCol);
    }
}
