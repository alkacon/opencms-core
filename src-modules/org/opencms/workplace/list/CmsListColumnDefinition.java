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

import org.opencms.i18n.CmsMessageContainer;
import org.opencms.i18n.CmsMessages;
import org.opencms.main.CmsIllegalArgumentException;
import org.opencms.util.CmsStringUtil;
import org.opencms.workplace.CmsWorkplace;
import org.opencms.workplace.tools.A_CmsHtmlIconButton;
import org.opencms.workplace.tools.CmsHtmlIconButtonStyleEnum;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

/**
 * Html list column definition.<p>
 *
 * @since 6.0.0
 */
public class CmsListColumnDefinition {

    /** Standard list button location. */
    public static final String ICON_DOWN = "list/arrow_down.png";

    /** Standard list button location. */
    public static final String ICON_UP = "list/arrow_up.png";

    /** Column alignment. */
    private CmsListColumnAlignEnum m_align = CmsListColumnAlignEnum.ALIGN_LEFT;

    /** Comparator for sorting. */
    private I_CmsListItemComparator m_comparator = new CmsListItemDefaultComparator();

    /** Default action. */
    private List<CmsListDefaultAction> m_defaultActions = new ArrayList<CmsListDefaultAction>();

    /** List of actions. */
    private List<I_CmsListDirectAction> m_directActions = new ArrayList<I_CmsListDirectAction>();

    /** Data formatter. */
    private I_CmsListFormatter m_formatter;

    /** Customized help text. */
    private CmsMessageContainer m_helpText;

    /** Unique id. */
    private final String m_id;

    /** List id. */
    private String m_listId;

    /** Display name. */
    private CmsMessageContainer m_name;

    /** Is printable flag. */
    private boolean m_printable = true;

    /** Flag for text wrapping. */
    private boolean m_textWrapping;

    /** Visible Flag. */
    private boolean m_visible = true;

    /** Column width. */
    private String m_width;

    /** The related workplace dialog object. */
    private transient A_CmsListDialog m_wp;

    /**
     * Default Constructor.<p>
     *
     * @param id the unique id
     */
    public CmsListColumnDefinition(String id) {

        if (CmsStringUtil.isEmptyOrWhitespaceOnly(id)) {
            throw new CmsIllegalArgumentException(Messages.get().container(Messages.ERR_LIST_INVALID_NULL_ARG_1, "id"));
        }
        m_id = id;
    }

    /**
     * Adds a default Action.<p>
     *
     * A column could have more than one default action if the visibilities are complementary.<p>
     *
     * @param defaultAction the default Action to add
     */
    public void addDefaultAction(CmsListDefaultAction defaultAction) {

        if (m_listId != null) {
            // set the list id
            defaultAction.setListId(m_listId);
        }
        // set the column id
        defaultAction.setColumnForLink(this);

        m_defaultActions.add(defaultAction);
    }

    /**
     * Adds a new action to the column.<p>
     *
     * @param listAction the action to add
     */
    public void addDirectAction(I_CmsListDirectAction listAction) {

        if (m_listId != null) {
            listAction.setListId(m_listId);
        }
        m_directActions.add(listAction);
    }

    /**
     * returns the csv output for a cell.<p>
     *
     * @param item the item to render the cell for
     *
     * @return csv output
     */
    public String csvCell(CmsListItem item) {

        if (!isVisible()) {
            return "";
        }
        StringBuffer csv = new StringBuffer(512);
        if (m_formatter == null) {
            // unformatted output
            if (item.get(m_id) != null) {
                // null values are not showed by default
                csv.append(item.get(m_id).toString());
            } else {
                Iterator<I_CmsListDirectAction> itActions = m_directActions.iterator();
                while (itActions.hasNext()) {
                    I_CmsListDirectAction action = itActions.next();
                    if (action.isVisible()) {
                        action.setItem(item);
                        csv.append(action.getName().key(getWp().getLocale()));
                    }
                }
            }
        } else {
            // formatted output
            csv.append(m_formatter.format(item.get(m_id), getWp().getLocale()));
        }
        return csv.toString();
    }

    /**
     * Returns the csv output for a column header.<p>
     *
     * @return csv header
     */
    public String csvHeader() {

        if (!isVisible()) {
            return "";
        }
        return getName().key(getWp().getLocale());
    }

    /**
     * Returns the align.<p>
     *
     * @return the align
     */
    public CmsListColumnAlignEnum getAlign() {

        return m_align;
    }

    /**
     * Returns a default action by id.<p>
     *
     * @param actionId the id of the action
     *
     * @return the action if found or null
     */
    public CmsListDefaultAction getDefaultAction(String actionId) {

        Iterator<CmsListDefaultAction> it = m_defaultActions.iterator();
        while (it.hasNext()) {
            CmsListDefaultAction action = it.next();
            if (action.getId().equals(actionId)) {
                return action;
            }
        }
        return null;
    }

    /**
     * Returns the default Action Ids list.<p>
     *
     * @return the default Action Ids list
     */
    public List<String> getDefaultActionIds() {

        List<String> ids = new ArrayList<String>();
        Iterator<CmsListDefaultAction> itDefActions = m_defaultActions.iterator();
        while (itDefActions.hasNext()) {
            I_CmsListDirectAction action = itDefActions.next();
            ids.add(action.getId());
        }
        return Collections.unmodifiableList(ids);
    }

    /**
     * Returns the default Actions list.<p>
     *
     * @return a list of {@link CmsListDefaultAction} objects
     */
    public List<CmsListDefaultAction> getDefaultActions() {

        return Collections.unmodifiableList(m_defaultActions);
    }

    /**
     * Returns a direct action by id.<p>
     *
     * @param actionId the id of the action
     *
     * @return the action if found or null
     */
    public I_CmsListDirectAction getDirectAction(String actionId) {

        Iterator<I_CmsListDirectAction> it = m_directActions.iterator();
        while (it.hasNext()) {
            I_CmsListDirectAction action = it.next();
            if (action.getId().equals(actionId)) {
                return action;
            }
        }
        return null;
    }

    /**
     * Returns the direct Action Ids list.<p>
     *
     * @return the direct Action Ids list
     */
    public List<String> getDirectActionIds() {

        List<String> ids = new ArrayList<String>();
        Iterator<I_CmsListDirectAction> itDirActions = m_directActions.iterator();
        while (itDirActions.hasNext()) {
            I_CmsListDirectAction action = itDirActions.next();
            ids.add(action.getId());
        }
        return Collections.unmodifiableList(ids);
    }

    /**
     * Returns all direct actions.<p>
     *
     * @return a list of <code>{@link I_CmsListDirectAction}</code>s.
     */
    public List<I_CmsListDirectAction> getDirectActions() {

        return Collections.unmodifiableList(m_directActions);
    }

    /**
     * Returns the data formatter.<p>
     *
     * @return the data formatter
     */
    public I_CmsListFormatter getFormatter() {

        return m_formatter;
    }

    /**
     * Returns the customized help Text.<p>
     *
     * if <code>null</code> a default help text indicating the sort actions is used.<p>
     *
     * @return the customized help Text
     */
    public CmsMessageContainer getHelpText() {

        return m_helpText;
    }

    /**
     * Returns the id.<p>
     *
     * @return the id
     */
    public String getId() {

        return m_id;
    }

    /**
     * Returns the comparator, used for sorting.<p>
     *
     * if no comparator was set, the default list item comparator is used.<p>
     *
     * @return the comparator
     *
     * @see CmsListItemDefaultComparator
     */
    public I_CmsListItemComparator getListItemComparator() {

        return m_comparator;
    }

    /**
     * Returns the name.<p>
     *
     * @return the name
     */
    public CmsMessageContainer getName() {

        return m_name;
    }

    /**
     * Returns the width.<p>
     *
     * @return the width
     */
    public String getWidth() {

        return m_width;
    }

    /**
     * Returns the workplace dialog object.<p>
     *
     * @return the workplace dialog object
     */
    public A_CmsListDialog getWp() {

        return m_wp;
    }

    /**
     * returns the html for a cell.<p>
     *
     * @param item the item to render the cell for
     * @param isPrintable if the list is to be printed
     *
     * @return html code
     */
    public String htmlCell(CmsListItem item, boolean isPrintable) {

        StringBuffer html = new StringBuffer(512);
        Iterator<I_CmsListDirectAction> itActions = m_directActions.iterator();
        while (itActions.hasNext()) {
            I_CmsListDirectAction action = itActions.next();
            action.setItem(item);
            boolean enabled = action.isEnabled();
            if (isPrintable) {
                action.setEnabled(false);
            }
            html.append(action.buttonHtml());
            if (isPrintable) {
                action.setEnabled(enabled);
            }
        }
        if (!m_defaultActions.isEmpty()) {
            Iterator<CmsListDefaultAction> itDefaultActions = m_defaultActions.iterator();
            while (itDefaultActions.hasNext()) {
                CmsListDefaultAction defAction = itDefaultActions.next();
                defAction.setItem(item);
                boolean enabled = defAction.isEnabled();
                if (isPrintable) {
                    defAction.setEnabled(false);
                }
                html.append(defAction.buttonHtml());
                if (isPrintable) {
                    defAction.setEnabled(enabled);
                }
            }
        } else {
            if (m_formatter == null) {
                // unformatted output
                if (item.get(m_id) != null) {
                    // null values are not showed by default
                    html.append(item.get(m_id).toString());
                }
            } else {
                // formatted output
                html.append(m_formatter.format(item.get(m_id), getWp().getLocale()));
            }
        }
        html.append("\n");
        return html.toString();
    }

    /**
     * Returns the html code for a column header.<p>
     *
     * @param list the list to generate the header code for
     *
     * @return html code
     */
    public String htmlHeader(CmsHtmlList list) {

        String listId = list.getId();
        String sortedCol = list.getSortedColumn();
        CmsListOrderEnum order = list.getCurrentSortOrder();

        StringBuffer html = new StringBuffer(512);
        Locale locale = getWp().getLocale();
        CmsMessages messages = Messages.get().getBundle(locale);
        html.append("<th");
        if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(getWidth())) {
            html.append(" width='");
            html.append(getWidth());
            html.append("'");
        }
        if (!isTextWrapping()) {
            html.append(" style='white-space: nowrap;'");
        }
        html.append(">\n");

        boolean isSorted = getId().equals(sortedCol);
        CmsListOrderEnum nextOrder = CmsListOrderEnum.ORDER_ASCENDING;
        if (isSorted && (order == CmsListOrderEnum.ORDER_ASCENDING)) {
            nextOrder = CmsListOrderEnum.ORDER_DESCENDING;
        }
        // button
        String id = listId + getId() + "Sort";
        String onClic = "listSort('" + listId + "', '" + getId() + "');";
        String helpText = null;
        if (m_helpText != null) {
            helpText = new MessageFormat(m_helpText.key(locale), locale).format(new Object[] {getName().key(locale)});
        } else {
            if (isSorteable()) {
                if (nextOrder.equals(CmsListOrderEnum.ORDER_ASCENDING)) {
                    helpText = messages.key(Messages.GUI_LIST_COLUMN_ASC_SORT_1, new Object[] {getName().key(locale)});
                } else {
                    helpText = messages.key(Messages.GUI_LIST_COLUMN_DESC_SORT_1, new Object[] {getName().key(locale)});
                }
            } else {
                helpText = messages.key(Messages.GUI_LIST_COLUMN_NO_SORT_1, new Object[] {getName().key(locale)});
            }
        }
        if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(getWidth()) && (getWidth().indexOf('%') < 0)) {
            html.append("\t<div style='display:block; width: ");
            html.append(getWidth());
            html.append("px;'>\n");
        }
        String sortArrow = "";
        // sort order marker
        if (isSorted) {
            if (nextOrder == CmsListOrderEnum.ORDER_ASCENDING) {
                sortArrow = "<img src='" + CmsWorkplace.getSkinUri() + ICON_DOWN + "' alt=''>&nbsp;";
            } else {
                sortArrow = "<img src='" + CmsWorkplace.getSkinUri() + ICON_UP + "' alt=''>&nbsp;";
            }
        }
        html.append(
            A_CmsHtmlIconButton.defaultButtonHtml(
                CmsHtmlIconButtonStyleEnum.SMALL_ICON_TEXT,
                id,
                id,
                getName().key(locale),
                helpText,
                list.isPrintable() ? false : isSorteable(),
                null,
                null,
                onClic,
                false,
                sortArrow));
        if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(getWidth()) && (getWidth().indexOf('%') < 0)) {
            html.append("\t</div>\n");
        }
        html.append("</th>\n");
        return html.toString();
    }

    /**
     * Returns the printable  .<p>
     *
     * @return the printable flag
     */
    public boolean isPrintable() {

        return m_printable;
    }

    /**
     * Returns the sorteable.<p>
     *
     * @return the sorteable
     */
    public boolean isSorteable() {

        return getListItemComparator() != null;
    }

    /**
     * Returns the text Wrapping flag.<p>
     *
     * @return the text Wrapping flag
     */
    public boolean isTextWrapping() {

        return m_textWrapping;
    }

    /**
     * Returns the visible.<p>
     *
     * @return the visible
     */
    public boolean isVisible() {

        return m_visible;
    }

    /**
     * Removes the default action from this column by id.<p>
     *
     * @param actionId the id of the action to remove
     *
     * @return the action if found or <code>null</code>
     */
    public CmsListDefaultAction removeDefaultAction(String actionId) {

        Iterator<CmsListDefaultAction> it = m_defaultActions.iterator();
        while (it.hasNext()) {
            CmsListDefaultAction action = it.next();
            if (action.getId().equals(actionId)) {
                it.remove();
                return action;
            }
        }
        return null;
    }

    /**
     * Removes a direct action from this column by id.<p>
     *
     * @param actionId the id of the action to remove
     *
     * @return the action if found or <code>null</code>
     */
    public I_CmsListDirectAction removeDirectAction(String actionId) {

        Iterator<I_CmsListDirectAction> it = m_directActions.iterator();
        while (it.hasNext()) {
            I_CmsListDirectAction action = it.next();
            if (action.getId().equals(actionId)) {
                it.remove();
                return action;
            }
        }
        return null;
    }

    /**
     * Sets the align.<p>
     *
     * @param align the align to set
     */
    public void setAlign(CmsListColumnAlignEnum align) {

        m_align = align;
    }

    /**
     * Sets the data formatter.<p>
     *
     * @param formatter the data formatter to set
     */
    public void setFormatter(I_CmsListFormatter formatter) {

        m_formatter = formatter;
        // set the formatter for all default actions
        Iterator<CmsListDefaultAction> it = m_defaultActions.iterator();
        while (it.hasNext()) {
            CmsListDefaultAction action = it.next();
            action.setColumnForLink(this);
        }
    }

    /**
     * Sets the customized help Text.<p>
     *
     * if <code>null</code> a default help text indicating the sort actions is used.<p>
     *
     * @param helpText the customized help Text to set
     */
    public void setHelpText(CmsMessageContainer helpText) {

        m_helpText = helpText;
    }

    /**
     * Sets the comparator, used for sorting.<p>
     *
     * @param comparator the comparator to set
     */
    public void setListItemComparator(I_CmsListItemComparator comparator) {

        m_comparator = comparator;
    }

    /**
     * Sets the name.<p>
     *
     * @param name the name to set
     */
    public void setName(CmsMessageContainer name) {

        m_name = name;
    }

    /**
     * Sets the printable flag.<p>
     *
     * @param printable the printable flag to set
     */
    public void setPrintable(boolean printable) {

        m_printable = printable;
    }

    /**
     * Indicates if the current column is sorteable or not.<p>
     *
     * if <code>true</code> a default list item comparator is used.<p>
     *
     * if <code>false</code> any previously set list item comparator is removed.<p>
     *
     * @param sorteable the sorteable flag
     */
    public void setSorteable(boolean sorteable) {

        if (sorteable) {
            setListItemComparator(new CmsListItemDefaultComparator());
        } else {
            setListItemComparator(null);
        }
    }

    /**
     * Sets the text Wrapping flag.<p>
     *
     * @param textWrapping the text Wrapping flag to set
     */
    public void setTextWrapping(boolean textWrapping) {

        m_textWrapping = textWrapping;
    }

    /**
     * Sets the visible.<p>
     *
     * This will set also the printable flag to <code>false</code>.<p>
     *
     * @param visible the visible to set
     */
    public void setVisible(boolean visible) {

        m_visible = visible;
        if (!m_visible) {
            setPrintable(false);
        }
    }

    /**
     * Sets the width.<p>
     *
     * @param width the width to set
     */
    public void setWidth(String width) {

        m_width = width;
    }

    /**
     * Sets the workplace dialog object.<p>
     *
     * @param wp the workplace dialog object to set
     */
    public void setWp(A_CmsListDialog wp) {

        m_wp = wp;
        Iterator<I_CmsListDirectAction> itActs = getDirectActions().iterator();
        while (itActs.hasNext()) {
            I_CmsListDirectAction action = itActs.next();
            action.setWp(wp);
        }
        Iterator<CmsListDefaultAction> itDefActs = getDefaultActions().iterator();
        while (itDefActs.hasNext()) {
            CmsListDefaultAction action = itDefActs.next();
            action.setWp(wp);
        }
    }

    /**
     * Sets the id of the list.<p>
     *
     * @param listId the id of the list
     */
    /*package*/void setListId(String listId) {

        m_listId = listId;
    }
}