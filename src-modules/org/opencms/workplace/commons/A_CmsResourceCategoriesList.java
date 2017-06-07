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

import org.opencms.file.CmsResource;
import org.opencms.i18n.CmsMessageContainer;
import org.opencms.jsp.CmsJspActionElement;
import org.opencms.main.CmsException;
import org.opencms.main.CmsRuntimeException;
import org.opencms.relations.CmsCategory;
import org.opencms.relations.CmsCategoryService;
import org.opencms.util.CmsStringUtil;
import org.opencms.workplace.CmsWorkplace;
import org.opencms.workplace.list.A_CmsListDialog;
import org.opencms.workplace.list.CmsListColumnDefinition;
import org.opencms.workplace.list.CmsListItem;
import org.opencms.workplace.list.CmsListItemDetails;
import org.opencms.workplace.list.CmsListMetadata;
import org.opencms.workplace.list.CmsListOrderEnum;
import org.opencms.workplace.list.I_CmsListFormatter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Generalized resource categories view.<p>
 *
 * @since 6.9.2
 */
public abstract class A_CmsResourceCategoriesList extends A_CmsListDialog {

    /** list action id constant. */
    public static final String LIST_ACTION_ICON = "ai";

    /** list column id constant. */
    public static final String LIST_COLUMN_ICON = "ci";

    /** list column id constant. */
    public static final String LIST_COLUMN_LEAFS = "cl";

    /** list column id constant. */
    public static final String LIST_COLUMN_NAME = "cn";

    /** list column id constant. */
    public static final String LIST_COLUMN_PATH = "cp";

    /** list column id constant. */
    public static final String LIST_COLUMN_STATE = "cs";

    /** list item detail id constant. */
    public static final String LIST_DETAIL_DESCRIPTION = "doo";

    /** list item detail id constant. */
    public static final String LIST_DETAIL_PATH = "dp";

    /** The current category service. */
    private CmsCategoryService m_categoryService;

    /** The current resource categories. */
    private List<CmsCategory> m_resCats;

    /**
     * Public constructor.<p>
     *
     * @param jsp an initialized JSP action element
     * @param listId the id of the list
     * @param listName the name of the list
     * @param searchable searchable flag
     */
    protected A_CmsResourceCategoriesList(
        CmsJspActionElement jsp,
        String listId,
        CmsMessageContainer listName,
        boolean searchable) {

        super(
            jsp,
            listId,
            listName,
            LIST_COLUMN_PATH,
            CmsListOrderEnum.ORDER_ASCENDING,
            searchable ? LIST_COLUMN_NAME : null);
        m_categoryService = CmsCategoryService.getInstance();
    }

    /**
     * @see org.opencms.workplace.list.A_CmsListDialog#executeListMultiActions()
     */
    @Override
    public void executeListMultiActions() throws CmsRuntimeException {

        throwListUnsupportedActionException();
    }

    /**
     * @see org.opencms.workplace.list.A_CmsListDialog#fillDetails(java.lang.String)
     */
    @Override
    protected void fillDetails(String detailId) {

        // get content
        List<CmsListItem> items = getList().getAllContent();
        Iterator<CmsListItem> itCategories = items.iterator();
        while (itCategories.hasNext()) {
            CmsListItem item = itCategories.next();
            String categoryPath = item.getId();
            StringBuffer html = new StringBuffer(512);
            try {
                CmsCategory category = m_categoryService.readCategory(getCms(), categoryPath, getParamResource());
                if (detailId.equals(LIST_DETAIL_PATH)) {
                    html.append(category.getRootPath());
                } else if (detailId.equals(LIST_DETAIL_DESCRIPTION)) {
                    // Append the description if one is given
                    if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(category.getDescription())) {
                        html.append(category.getDescription());
                    }
                }
            } catch (Exception e) {
                // noop
            }
            item.set(detailId, html.toString());
        }
    }

    /**
     * Returns a list of categories to display.<p>
     *
     * @return a list of categories
     *
     * @throws CmsException if something goes wrong
     */
    protected abstract List<CmsCategory> getCategories() throws CmsException;

    /**
     * Returns the categoryService.<p>
     *
     * @return the categoryService
     */
    protected CmsCategoryService getCategoryService() {

        return m_categoryService;
    }

    /**
     * @see org.opencms.workplace.list.A_CmsListDialog#getListItems()
     */
    @Override
    protected List<CmsListItem> getListItems() throws CmsException {

        Map<String, CmsListItem> ret = new HashMap<String, CmsListItem>();
        Map<String, Boolean> isLeaf = new HashMap<String, Boolean>();
        List<CmsCategory> cats = getCategories();
        Iterator<CmsCategory> itCategories = cats.iterator();
        while (itCategories.hasNext()) {
            CmsCategory category = itCategories.next();
            isLeaf.put(category.getPath(), Boolean.TRUE);
        }
        itCategories = cats.iterator();
        while (itCategories.hasNext()) {
            CmsCategory category = itCategories.next();
            String parentPath = CmsResource.getParentFolder(category.getPath());
            if (parentPath == null) {
                continue;
            }
            if (isLeaf.get(parentPath) != null) {
                isLeaf.put(parentPath, Boolean.FALSE);
            }
        }
        itCategories = cats.iterator();
        while (itCategories.hasNext()) {
            CmsCategory category = itCategories.next();
            String categoryPath = category.getPath();
            if (ret.get(categoryPath) != null) {
                continue;
            }
            CmsListItem item = getList().newItem(categoryPath);
            StringBuffer itemHtml = new StringBuffer(192);
            int pathLevel = CmsStringUtil.splitAsList(categoryPath, '/').size();
            if (pathLevel > 1) {
                // append image for indentation
                itemHtml.append("<img src=\"");
                itemHtml.append(CmsWorkplace.getResourceUri("tree/empty.gif"));
                itemHtml.append("\" width=\"");
                itemHtml.append((pathLevel - 1) * 20);
                itemHtml.append("px\" height=\"11px\"/>");
            }
            String name = category.getTitle();
            if (CmsStringUtil.isEmptyOrWhitespaceOnly(name)) {
                name = category.getName();
            }
            itemHtml.append(name);
            item.set(LIST_COLUMN_NAME, itemHtml.toString());
            item.set(LIST_COLUMN_PATH, categoryPath);
            item.set(LIST_COLUMN_LEAFS, isLeaf.get(categoryPath));
            ret.put(item.getId(), item);
        }

        return new ArrayList<CmsListItem>(ret.values());
    }

    /**
     * Returns a list of a categories related to the current request resource.<p>
     *
     * @return a list of a categories related to the current request resource
     *
     * @throws CmsException if something goes wrong
     */
    protected List<CmsCategory> getResourceCategories() throws CmsException {

        if (m_resCats == null) {
            m_resCats = m_categoryService.readResourceCategories(getJsp().getCmsObject(), getParamResource());
        }
        return m_resCats;
    }

    /**
     * @see org.opencms.workplace.list.A_CmsListDialog#setColumns(org.opencms.workplace.list.CmsListMetadata)
     */
    @Override
    protected void setColumns(CmsListMetadata metadata) {

        setStateActionCol(metadata);

        // create column for name
        CmsListColumnDefinition nameCol = new CmsListColumnDefinition(LIST_COLUMN_NAME);
        nameCol.setName(Messages.get().container(Messages.GUI_CATEGORIES_LIST_COLS_NAME_0));
        nameCol.setWidth("100%");
        nameCol.setSorteable(false);
        // add it to the list definition
        metadata.addColumn(nameCol);

        // create column for path
        CmsListColumnDefinition pathCol = new CmsListColumnDefinition(LIST_COLUMN_PATH);
        pathCol.setName(Messages.get().container(Messages.GUI_CATEGORIES_LIST_COLS_PATH_0));
        pathCol.setVisible(false);
        // add it to the list definition
        metadata.addColumn(pathCol);

        // create column for leaf
        CmsListColumnDefinition leafCol = new CmsListColumnDefinition(LIST_COLUMN_LEAFS);
        leafCol.setName(Messages.get().container(Messages.GUI_CATEGORIES_LIST_COLS_PATH_0));
        leafCol.setVisible(false);
        // add it to the list definition
        metadata.addColumn(leafCol);
    }

    /**
     * @see org.opencms.workplace.list.A_CmsListDialog#setIndependentActions(org.opencms.workplace.list.CmsListMetadata)
     */
    @Override
    protected void setIndependentActions(CmsListMetadata metadata) {

        // show path
        CmsListItemDetails pathDetails = new CmsListItemDetails(LIST_DETAIL_PATH);
        pathDetails.setAtColumn(LIST_COLUMN_NAME);
        pathDetails.setVisible(false);
        pathDetails.setShowActionName(Messages.get().container(Messages.GUI_CATEGORIES_DETAIL_SHOW_PATH_NAME_0));
        pathDetails.setShowActionHelpText(Messages.get().container(Messages.GUI_CATEGORIES_DETAIL_SHOW_PATH_HELP_0));
        pathDetails.setHideActionName(Messages.get().container(Messages.GUI_CATEGORIES_DETAIL_HIDE_PATH_NAME_0));
        pathDetails.setHideActionHelpText(Messages.get().container(Messages.GUI_CATEGORIES_DETAIL_HIDE_PATH_HELP_0));
        pathDetails.setName(Messages.get().container(Messages.GUI_CATEGORIES_DETAIL_PATH_NAME_0));
        pathDetails.setFormatter(new I_CmsListFormatter() {

            /**
             * @see org.opencms.workplace.list.I_CmsListFormatter#format(java.lang.Object, java.util.Locale)
             */
            @Override
            public String format(Object data, Locale locale) {

                StringBuffer html = new StringBuffer(512);
                html.append("<table border='0' cellspacing='0' cellpadding='0'>\n");
                html.append("\t<tr>\n");
                html.append("\t\t<td style='white-space:normal;' >\n");
                html.append("\t\t\t");
                html.append(data == null ? "" : data);
                html.append("\n");
                html.append("\t\t</td>\n");
                html.append("\t</tr>\n");
                html.append("</table>\n");
                return html.toString();
            }
        });
        metadata.addItemDetails(pathDetails);

        // show description
        CmsListItemDetails descriptionDetails = new CmsListItemDetails(LIST_DETAIL_DESCRIPTION);
        descriptionDetails.setAtColumn(LIST_COLUMN_NAME);
        descriptionDetails.setVisible(false);
        descriptionDetails.setShowActionName(
            Messages.get().container(Messages.GUI_CATEGORIES_DETAIL_SHOW_DESCRIPTION_NAME_0));
        descriptionDetails.setShowActionHelpText(
            Messages.get().container(Messages.GUI_CATEGORIES_DETAIL_SHOW_DESCRIPTION_HELP_0));
        descriptionDetails.setHideActionName(
            Messages.get().container(Messages.GUI_CATEGORIES_DETAIL_HIDE_DESCRIPTION_NAME_0));
        descriptionDetails.setHideActionHelpText(
            Messages.get().container(Messages.GUI_CATEGORIES_DETAIL_HIDE_DESCRIPTION_HELP_0));
        descriptionDetails.setName(Messages.get().container(Messages.GUI_CATEGORIES_DETAIL_DESCRIPTION_NAME_0));
        descriptionDetails.setFormatter(new I_CmsListFormatter() {

            /**
             * @see org.opencms.workplace.list.I_CmsListFormatter#format(java.lang.Object, java.util.Locale)
             */
            @Override
            public String format(Object data, Locale locale) {

                StringBuffer html = new StringBuffer(512);
                html.append("<table border='0' cellspacing='0' cellpadding='0'>\n");
                html.append("\t<tr>\n");
                html.append("\t\t<td style='white-space:normal;' >\n");
                html.append("\t\t\t");
                html.append(data == null ? "" : data);
                html.append("\n");
                html.append("\t\t</td>\n");
                html.append("\t</tr>\n");
                html.append("</table>\n");
                return html.toString();
            }
        });
        metadata.addItemDetails(descriptionDetails);
    }

    /**
     * @see org.opencms.workplace.list.A_CmsListDialog#setMultiActions(org.opencms.workplace.list.CmsListMetadata)
     */
    @Override
    protected void setMultiActions(CmsListMetadata metadata) {

        // noop
    }

    /**
     * Sets the optional state change action column.<p>
     *
     * @param metadata the list metadata object
     */
    protected abstract void setStateActionCol(CmsListMetadata metadata);
}
