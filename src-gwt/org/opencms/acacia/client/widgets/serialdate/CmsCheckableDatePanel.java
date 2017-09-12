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

package org.opencms.acacia.client.widgets.serialdate;

import org.opencms.gwt.client.ui.input.CmsCheckBox;
import org.opencms.util.CmsPair;

import java.util.Collection;
import java.util.Comparator;
import java.util.Date;
import java.util.SortedSet;
import java.util.TreeSet;

import com.google.gwt.dom.client.Style.Display;
import com.google.gwt.event.logical.shared.HasValueChangeHandlers;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.Widget;

/** Special list for checkboxes with dates. */
public class CmsCheckableDatePanel extends Composite implements HasValueChangeHandlers<SortedSet<Date>> {

    /** The various style options for the checkable date panel. */
    public static enum Style {
        /** One column. */
        ONE_COLUMN,
        /** Two columns. */
        TWO_COLUMNS,
        /** Three columns. */
        THREE_COLUMNS;

        /**
         * Get the width of elements dependent on the style.
         * @return the element width, e.g., "50%"
         */
        public String getWidth() {

            switch (this) {
                case ONE_COLUMN:
                    return "100%";
                case TWO_COLUMNS:
                    return "50%";
                case THREE_COLUMNS:
                    return "33%";
                default:
                    return "100%";
            }
        }
    }

    /** Default date format to use if no other format is specified in the message bundle. */
    private static final String DEFAULT_DATE_FORMAT = "E, MMMM d, yyyy";

    /** The map from the checkboxes in the list to the dates of the boxes. */
    SortedSet<CmsCheckBox> m_checkBoxes;

    /** The dates in the widget. */
    SortedSet<Date> m_dates;
    /** The date format. */
    DateTimeFormat m_dateFormat;

    /** The panel where checkboxes with the dates are places. */
    Panel m_panel;

    /** Flag, indicating if only labels should be shown. */
    boolean m_onlyLabels;

    /** The style of the panel. */
    Style m_style;

    /** The element width determined by the style. */
    String m_width;

    /**
     * Constructor for creating a one column list with check boxes.
     * @param dateFormat The date format to use.
     */
    public CmsCheckableDatePanel(String dateFormat) {
        this(dateFormat, Style.ONE_COLUMN, false);
    }

    /**
     * Constructor for creating a list with check boxes.
     * @param dateFormat The date format to use.
     * @param style the style to use for displaying the dates.
     */
    public CmsCheckableDatePanel(String dateFormat, Style style) {
        this(dateFormat, style, false);
    }

    /**
     * Constructor where all options can be set.
     * @param dateFormat The date format to use.
     * @param style the style to use for displaying the dates.
     * @param onlyLabels flag, indicating if only labels should be shown.
     */
    public CmsCheckableDatePanel(String dateFormat, Style style, boolean onlyLabels) {
        m_panel = new FlowPanel();
        m_style = null == style ? Style.ONE_COLUMN : style;
        m_width = m_style.getWidth();
        m_onlyLabels = onlyLabels;
        initWidget(m_panel);
        m_checkBoxes = new TreeSet<CmsCheckBox>(new Comparator<CmsCheckBox>() {

            public int compare(CmsCheckBox o1, CmsCheckBox o2) {

                Date date1 = (Date)o1.getElement().getPropertyObject("date");
                Date date2 = (Date)o2.getElement().getPropertyObject("date");
                if ((null == date1) || (null == date2)) {
                    return 0;
                } else {
                    return date1.compareTo(date2);
                }
            }
        });
        try {
            m_dateFormat = DateTimeFormat.getFormat(dateFormat);
        } catch (@SuppressWarnings("unused") Exception e) {
            m_dateFormat = DateTimeFormat.getFormat(DEFAULT_DATE_FORMAT);
        }
        m_dates = new TreeSet<>();

    }

    /**
     * Adds a date to the list (unchecked).
     * @param date the date to add.
     */
    public void addDate(Date date) {

        addDateWithCheckState(date, false);
    }

    /**
     * Adds a date that is already checked.
     * @param date the date to add.
     */
    public void addDateChecked(Date date) {

        addDateWithCheckState(date, true);
    }

    /**
     * @see com.google.gwt.event.logical.shared.HasValueChangeHandlers#addValueChangeHandler(com.google.gwt.event.logical.shared.ValueChangeHandler)
     */
    public HandlerRegistration addValueChangeHandler(ValueChangeHandler<SortedSet<Date>> handler) {

        return addHandler(handler, ValueChangeEvent.getType());
    }

    /**
     * Returns all checked dates.
     * @return all checked dates.
     */
    public SortedSet<Date> getCheckedDates() {

        return getDates(Boolean.TRUE);
    }

    /**
     * Returns all dates in the list.
     * @return all dates in the list.
     */
    public SortedSet<Date> getDates() {

        return new TreeSet<Date>(m_dates);
    }

    /**
     * Returns all dates with the specified check state, if the check state is <code>null</code>, all dates are returned.
     * @param checkState the check state, the returned dates should have.
     * @return all dates with the specified check state, if the check state is <code>null</code>, all dates are returned.
     */
    public SortedSet<Date> getDates(Boolean checkState) {

        TreeSet<Date> result = new TreeSet<Date>();
        for (CmsCheckBox cb : m_checkBoxes) {
            if ((checkState == null) || (cb.isChecked() == checkState.booleanValue())) {
                Date date = (Date)cb.getElement().getPropertyObject("date");
                result.add(date);
            }
        }
        return result;
    }

    /**
     * Returns all dates that are not checked.
     * @return all dates that are not checked.
     */
    public SortedSet<Date> getUncheckedDates() {

        return getDates(Boolean.FALSE);
    }

    /**
     * Sets all dates in the list (unchecked).
     * @param dates the dates to set.
     */
    public void setDates(SortedSet<Date> dates) {

        setDates(dates, false);
    }

    /**
     * Sets all dates in the list.
     * @param dates the dates to set
     * @param checked flag, indicating if all should be checked or unchecked.
     */
    public void setDates(SortedSet<Date> dates, boolean checked) {

        m_checkBoxes.clear();
        for (Date date : dates) {
            CmsCheckBox cb = generateCheckBox(date, checked);
            m_checkBoxes.add(cb);
        }
        reInitLayoutElements();
        setDatesInternal(dates);
    }

    /**
     * Set dates with the provided check states.
     * @param datesWithCheckInfo the dates to set, accompanied with the check state to set.
     */
    public void setDatesWithCheckState(Collection<CmsPair<Date, Boolean>> datesWithCheckInfo) {

        SortedSet<Date> dates = new TreeSet<>();
        m_checkBoxes.clear();
        for (CmsPair<Date, Boolean> p : datesWithCheckInfo) {
            addCheckBox(p.getFirst(), p.getSecond().booleanValue());
            dates.add(p.getFirst());
        }
        reInitLayoutElements();
        setDatesInternal(dates);
    }

    /**
     * Add a new check box.
     * @param date the date for the check box
     * @param checkState the initial check state.
     */
    private void addCheckBox(Date date, boolean checkState) {

        CmsCheckBox cb = generateCheckBox(date, checkState);
        m_checkBoxes.add(cb);
        reInitLayoutElements();

    }

    /**
     * Add a date with a certain check state.
     * @param date the date to add.
     * @param checkState the check state.
     */
    private void addDateWithCheckState(Date date, boolean checkState) {

        addCheckBox(date, checkState);
        if (!m_dates.contains(date)) {
            m_dates.add(date);
            fireValueChange();
        }
    }

    /**
     * Fire a value change event.
     */
    private void fireValueChange() {

        ValueChangeEvent.fire(this, m_dates);
    }

    /**
     * Generate a new check box with the provided date and check state.
     * @param date date for the check box.
     * @param checkState the initial check state.
     * @return the created check box
     */
    private CmsCheckBox generateCheckBox(Date date, boolean checkState) {

        CmsCheckBox cb = new CmsCheckBox();
        cb.setText(m_dateFormat.format(date));
        cb.setChecked(checkState);
        cb.getElement().setPropertyObject("date", date);
        return cb;

    }

    /**
     * Refresh the layout element.
     */
    private void reInitLayoutElements() {

        m_panel.clear();
        for (CmsCheckBox cb : m_checkBoxes) {
            m_panel.add(setStyle(m_onlyLabels ? new Label(cb.getText()) : cb));
        }
    }

    /**
     * Updates the internal list of dates and fires a value change if necessary.
     *
     * @param dates the dates to set.
     */
    private void setDatesInternal(SortedSet<Date> dates) {

        if (!m_dates.equals(dates)) {
            m_dates = new TreeSet<>(dates);
            fireValueChange();
        }
    }

    /**
     * Set the style for the widgets in the panel according to the chosen style option.
     * @param widget the widget that should be styled.
     * @return the styled widget.
     */
    private Widget setStyle(Widget widget) {

        widget.setWidth(m_width);
        widget.getElement().getStyle().setDisplay(Display.INLINE_BLOCK);
        return widget;
    }

}
