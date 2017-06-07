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
 * For further information about Alkacon Software, please see the
 * company website: http://www.alkacon.com
 *
 * For further information about OpenCms, please see the
 * project website: http://www.opencms.org
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package org.opencms.gwt.client.ui.input;

import org.opencms.gwt.client.I_CmsHasInit;
import org.opencms.gwt.client.Messages;
import org.opencms.gwt.client.ui.I_CmsAutoHider;
import org.opencms.gwt.client.ui.input.form.CmsWidgetFactoryRegistry;
import org.opencms.gwt.client.ui.input.form.I_CmsFormWidgetFactory;
import org.opencms.gwt.client.util.CmsMessages;
import org.opencms.util.CmsStringUtil;

import java.util.HashMap;
import java.util.Map;

import com.google.gwt.user.client.ui.FocusPanel;

/**
 * Widget for selecting one of multiple items from a drop-down list which opens
 * after the user clicks on the widget.<p>
 *
 * @since 8.0.0
 *
 */
public class CmsSelectBox extends A_CmsSelectBox<CmsLabelSelectCell>implements I_CmsHasInit, I_CmsHasGhostValue {

    /** The key for the text which should be displayed in the opener if no option is available. */
    public static final String NO_SELECTION_OPENER_TEXT = "%NO_SELECTION_OPENER_TEXT%";

    /** The key for the text which should be displayed if no option is available. */
    public static final String NO_SELECTION_TEXT = "%NO_SELECTION_TEXT%";

    /** Text metrics key. */
    private static final String TM_OPENER_LABEL = "OpenerLabel";

    /** The widget type identifier. */
    public static final String WIDGET_TYPE = "select";

    /** Widget type identifier (no null value). */
    public static final String WIDGET_TYPE_NOTNULL = "select_notnull";

    /** The ghost value. */
    protected String m_ghostValue;

    /** The widget displayed in the opener. */
    protected CmsLabel m_openerWidget;

    /** A map from select options to their label texts. */
    private Map<String, String> m_items;

    /** The text which should be displayed in the opener if there is no selection. */
    private String m_noSelectionOpenerText;

    /** The text which should be displayed if there is no selection. */
    private String m_noSelectionText;

    /** A map of titles for the select options which should  be displayed on mouseover. */
    private Map<String, String> m_titles = new HashMap<String, String>();

    /**
     * Default constructor.<p>
     */
    public CmsSelectBox() {

        super();

    }

    /**
     * Constructs a new select box from a map.<p>
     *
     * The keys of the map are the values of the select options, and the values of the map are the labels to be displayed
     * for each option.
     *
     * @param items the map of select options
     */
    public CmsSelectBox(Map<String, String> items) {

        super();
        setItems(items);
    }

    /**
     * Creates a new select box, with the option of adding a "not selected" choice.<p>
     *
     * @param items the map of select options
     * @param addNullOption if true, a "not selected" option will be added to the select box
     */
    public CmsSelectBox(Map<String, String> items, boolean addNullOption) {

        super();
        if (items.containsKey(NO_SELECTION_TEXT)) {
            m_noSelectionText = items.get(NO_SELECTION_TEXT);
            m_noSelectionOpenerText = items.get(NO_SELECTION_OPENER_TEXT);
            if (m_noSelectionOpenerText == null) {
                m_noSelectionOpenerText = m_noSelectionText;
            }
            items.remove(NO_SELECTION_TEXT);
            items.remove(NO_SELECTION_OPENER_TEXT);
        }
        if (addNullOption) {
            String text = Messages.get().key(Messages.GUI_SELECTBOX_EMPTY_SELECTION_0);
            items.put("", text);
        }
        setItems(items);
        if (addNullOption) {
            selectValue("");
        }
    }

    /**
     * Initializes this class.<p>
     */
    public static void initClass() {

        // registers a factory for creating new instances of this widget
        CmsWidgetFactoryRegistry.instance().registerFactory(WIDGET_TYPE, new I_CmsFormWidgetFactory() {

            /**
             * @see org.opencms.gwt.client.ui.input.form.I_CmsFormWidgetFactory#createWidget(java.util.Map)
             */
            public I_CmsFormWidget createWidget(Map<String, String> widgetParams) {

                return new CmsSelectBox(widgetParams, true);
            }
        });

        CmsWidgetFactoryRegistry.instance().registerFactory(WIDGET_TYPE_NOTNULL, new I_CmsFormWidgetFactory() {

            /**
             * @see org.opencms.gwt.client.ui.input.form.I_CmsFormWidgetFactory#createWidget(java.util.Map)
             */
            public I_CmsFormWidget createWidget(Map<String, String> widgetParams) {

                return new CmsSelectBox(widgetParams, false);
            }
        });

    }

    /**
     * Adds a new selection cell.<p>
     *
     * @param value the value of the select option
     * @param text the text to be displayed for the select option
     */
    public void addOption(String value, String text) {

        String title = getTitle(value, text);
        CmsLabelSelectCell cell = new CmsLabelSelectCell(value, text, title);
        addOption(cell);
    }

    /**
     * @see org.opencms.gwt.client.ui.input.I_CmsFormWidget#getApparentValue()
     */
    public String getApparentValue() {

        String val = getFormValueAsString();
        if (val == null) {
            val = m_ghostValue;
        }
        return val;

    }

    /**
     * Returns the items as a map for option values to label text.<p>
     *
     * @return the items as a map for option values to label text
     */
    public Map<String, String> getItems() {

        return m_items;
    }

    /**
     * Returns the opener of this widget.<p>
     *
     * @return the opener of this widget
     */
    public FocusPanel getOpener() {

        return m_opener;
    }

    /**
     * @see org.opencms.gwt.client.ui.input.A_CmsSelectBox#selectValue(java.lang.String)
     */
    @Override
    public void selectValue(String value) {

        super.selectValue(value);
        updateStyle();
    }

    /**
     * @see org.opencms.gwt.client.ui.input.I_CmsFormWidget#setAutoHideParent(org.opencms.gwt.client.ui.I_CmsAutoHider)
     */
    public void setAutoHideParent(I_CmsAutoHider autoHideParent) {

        // nothing to do

    }

    /**
     * @see org.opencms.gwt.client.ui.input.I_CmsHasGhostValue#setGhostMode(boolean)
     */
    public void setGhostMode(boolean ghostMode) {

        // do nothing for now

    }

    /**
     * @see org.opencms.gwt.client.ui.input.I_CmsHasGhostValue#setGhostValue(java.lang.String, boolean)
     */
    public void setGhostValue(String value, boolean ghostMode) {

        if (value == null) {
            value = "";
        }
        String otherOptionText = m_items.get(value);
        String message = m_noSelectionText != null
        ? m_noSelectionText
        : Messages.get().key(Messages.GUI_SELECTBOX_EMPTY_SELECTION_1);
        message = CmsMessages.formatMessage(message, otherOptionText);
        //setTextForNullSelection(message);
        m_ghostValue = value;
        updateCells();
        if (ghostMode) {
            selectValue("");
        }
    }

    /**
     * Sets the items using a map from option values to label texts.<p>
     *
     * @param items the map containing the select options
     */
    public void setItems(Map<String, String> items) {

        clearItems();
        m_items = items;
        for (Map.Entry<String, String> entry : items.entrySet()) {
            addOption(entry.getKey(), entry.getValue());
        }
    }

    /**
     * Sets the text that is used for the "not selected" option.<p>
     *
     * @param text the text which should be used for the "not selected" option
     */
    public void setTextForNullSelection(String text) {

        // do nothing if there's no null option
        CmsLabelSelectCell cell = m_selectCells.get("");
        if (cell == null) {
            return;
        }
        cell.setText(text);
        // if the null option is selected, we still need to update the opener
        if (CmsStringUtil.isEmptyOrWhitespaceOnly(m_selectedValue)) {
            selectValue("");
        }
    }

    /**
     * Sets the title for a select option.<p>
     *
     * Note: This will only affect select options added *after* calling this method!
     *
     * @param option the select option value
     * @param title the new title for the option
     */
    public void setTitle(String option, String title) {

        m_titles.put(option, title);
    }

    /**
     * @see org.opencms.gwt.client.ui.input.A_CmsSelectBox#truncateOpener(java.lang.String, int)
     */
    @Override
    public void truncateOpener(String prefix, int width) {

        m_openerWidget.truncate(prefix + '_' + TM_OPENER_LABEL, width);
    }

    /**
     * Updates a single select cell.<p>
     *
     * @param cell the select cell to update
     */
    public void updateCell(CmsLabelSelectCell cell) {

        // do nothing
    }

    /**
     * Updates the select cells.<p>
     */
    public void updateCells() {

        for (CmsLabelSelectCell cell : m_selectCells.values()) {
            updateCell(cell);
        }
    }

    /**
     * @see org.opencms.gwt.client.ui.input.A_CmsSelectBox#createUnknownOption(java.lang.String)
     */
    @Override
    protected CmsLabelSelectCell createUnknownOption(String value) {

        CmsLabelSelectCell cell = new CmsLabelSelectCell(value, value);
        return cell;

    }

    /**
     * Helper method to get the title for a given select option.<p>
     *
     * @param option the select option value
     * @param defaultValue the value to return when no title for the value was found
     *
     * @return the title for the select option
     */
    protected String getTitle(String option, String defaultValue) {

        if ((option != null) && m_titles.containsKey(option)) {
            return m_titles.get(option);
        }
        return defaultValue;
    }

    /**
     * @see org.opencms.gwt.client.ui.input.A_CmsSelectBox#initOpener()
     */
    @Override
    protected void initOpener() {

        m_openerWidget = new CmsLabel();
        m_openerWidget.addStyleName(CSS.selectBoxOpener());
        m_opener.add(m_openerWidget);
    }

    /**
     * @see com.google.gwt.user.client.ui.Widget#onLoad()
     */
    @Override
    protected void onLoad() {

        super.onLoad();
        updateStyle();
    }

    /**
     * @see org.opencms.gwt.client.ui.input.A_CmsSelectBox#updateOpener(java.lang.String)
     */
    @Override
    protected void updateOpener(String newValue) {

        CmsLabel label = m_openerWidget;
        CmsLabelSelectCell cell = m_selectCells.get(newValue);
        String openerText = cell.getOpenerText();
        label.setText(openerText);
        label.setTitle(getTitle(cell.getValue(), openerText));
    }

    /**
     * This method should be used to make changes to the CSS style of the select box when the value changes.<p>
     */
    protected void updateStyle() {

        // do nothing
    }
}
