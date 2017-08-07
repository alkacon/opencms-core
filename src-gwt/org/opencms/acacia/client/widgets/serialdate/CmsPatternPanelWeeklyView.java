/*
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (c) Alkacon Software GmbH (http://www.alkacon.com)
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

import org.opencms.acacia.shared.I_CmsSerialDateValue.WeekDay;
import org.opencms.ade.contenteditor.client.Messages;
import org.opencms.gwt.client.ui.input.CmsCheckBox;

import java.util.ArrayList;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

import com.google.gwt.core.shared.GWT;
import com.google.gwt.dom.client.Element;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTMLPanel;

/**
 * The weekly pattern panel.<p>
 * */
public class CmsPatternPanelWeeklyView extends Composite implements I_CmsSerialDatePatternView {

    /** The UI binder interface. */
    interface I_CmsPatternPanelWeekly extends UiBinder<HTMLPanel, CmsPatternPanelWeeklyView> {
        // nothing to do
    }

    /** The UI binder instance. */
    private static I_CmsPatternPanelWeekly uiBinder = GWT.create(I_CmsPatternPanelWeekly.class);

    /** The array of all checkboxes. */
    List<CmsCheckBox> m_checkboxes = new ArrayList<CmsCheckBox>();

    /** The panel for all values of the day selection. */
    @UiField
    FlowPanel m_dayPanel = new FlowPanel();

    /** The text box for the date input. */
    @UiField
    CmsFocusAwareTextBox m_everyDay;

    /** The every label. */
    @UiField
    Element m_labelEvery;

    /** The weeks label. */
    @UiField
    Element m_labelWeeks;

    /** The handler for check box value changes. */
    private ValueChangeHandler<Boolean> m_checkBoxValueChangeHandler;

    /** The model to read the data from. */
    private final I_CmsObservableSerialDateValue m_model;

    /** The controller to handle changes. */
    final CmsPatternPanelWeeklyController m_controller;

    /** Flag, indicating if change actions should not be triggered. */
    private boolean m_triggerChangeActions = true;

    /**
     * Default constructor to create the panel.<p>
     * @param model the model to read data from.
     * @param controller the controller to communicate with.
     */
    public CmsPatternPanelWeeklyView(CmsPatternPanelWeeklyController controller, I_CmsObservableSerialDateValue model) {

        m_controller = controller;
        m_model = model;
        m_model.registerValueChangeObserver(this);
        m_checkBoxValueChangeHandler = new ValueChangeHandler<Boolean>() {

            public void onValueChange(ValueChangeEvent<Boolean> event) {

                if (handleChange()) {
                    m_controller.setWeekDays(getWeekDays());
                }
            }
        };

        initWidget(uiBinder.createAndBindUi(this));

        m_labelEvery.setInnerText(Messages.get().key(Messages.GUI_SERIALDATE_WEEKLY_EVERY_0));
        m_everyDay.setTriggerChangeOnKeyPress(true);
        m_labelWeeks.setInnerText(Messages.get().key(Messages.GUI_SERIALDATE_WEEKLY_WEEK_AT_0));
        createDayPanel();
    }

    /**
     * @see org.opencms.acacia.client.widgets.serialdate.I_CmsSerialDateValueChangeObserver#onValueChange()
     */
    public void onValueChange() {

        m_triggerChangeActions = false;
        if (!m_everyDay.isFocused()) {
            m_everyDay.setFormValueAsString("" + m_model.getInterval());
        }
        setWeekDays(m_model.getWeekDays());
        m_triggerChangeActions = true;

    }

    /**
     * Returns all selected days.<p>
     * @return all selected days
     * */
    protected SortedSet<WeekDay> getWeekDays() {

        SortedSet<WeekDay> result = new TreeSet<>();
        for (CmsCheckBox box : m_checkboxes) {
            if (box.isChecked()) {
                result.add(WeekDay.valueOf(box.getInternalValue()));
            }
        }
        return result;
    }

    /**
     * Returns a flag, indicating if change actions should be triggered.
     * @return a flag, indicating if change actions should be triggered.
     */
    boolean handleChange() {

        return m_triggerChangeActions;
    }

    /**
     * Handles the days key press event.<p>
     *
     * @param event the key press event
     */
    @UiHandler("m_everyDay")
    void onDaysValueChange(ValueChangeEvent<String> event) {

        if (handleChange()) {
            m_controller.setInterval(m_everyDay.getText());
        }
    }

    /**
     * Creates the day selection view.<p>
     * */
    private void createDayPanel() {

        CmsCheckBox box = new CmsCheckBox(Messages.get().key(Messages.GUI_SERIALDATE_DAY_MONDAY_0));
        box.setInternalValue(WeekDay.MONDAY.toString());
        box.addValueChangeHandler(m_checkBoxValueChangeHandler);
        m_checkboxes.add(box);
        m_dayPanel.add(box);
        box = new CmsCheckBox(Messages.get().key(Messages.GUI_SERIALDATE_DAY_TUESDAY_0));
        box.setInternalValue(WeekDay.TUESDAY.toString());
        box.addValueChangeHandler(m_checkBoxValueChangeHandler);
        m_checkboxes.add(box);
        m_dayPanel.add(box);
        box = new CmsCheckBox(Messages.get().key(Messages.GUI_SERIALDATE_DAY_WEDNESDAY_0));
        box.setInternalValue(WeekDay.WEDNESDAY.toString());
        box.addValueChangeHandler(m_checkBoxValueChangeHandler);
        m_checkboxes.add(box);
        m_dayPanel.add(box);
        box = new CmsCheckBox(Messages.get().key(Messages.GUI_SERIALDATE_DAY_THURSDAY_0));
        box.setInternalValue(WeekDay.THURSDAY.toString());
        box.addValueChangeHandler(m_checkBoxValueChangeHandler);
        m_checkboxes.add(box);
        m_dayPanel.add(box);
        box = new CmsCheckBox(Messages.get().key(Messages.GUI_SERIALDATE_DAY_FRIDAY_0));
        box.setInternalValue(WeekDay.FRIDAY.toString());
        box.addValueChangeHandler(m_checkBoxValueChangeHandler);
        m_checkboxes.add(box);
        m_dayPanel.add(box);
        box = new CmsCheckBox(Messages.get().key(Messages.GUI_SERIALDATE_DAY_SATURDAY_0));
        box.setInternalValue(WeekDay.SATURDAY.toString());
        box.addValueChangeHandler(m_checkBoxValueChangeHandler);
        m_checkboxes.add(box);
        m_dayPanel.add(box);
        box = new CmsCheckBox(Messages.get().key(Messages.GUI_SERIALDATE_DAY_SUNDAY_0));
        box.setInternalValue(WeekDay.SUNDAY.toString());
        box.addValueChangeHandler(m_checkBoxValueChangeHandler);
        box.addValueChangeHandler(m_checkBoxValueChangeHandler);
        m_checkboxes.add(box);
        m_dayPanel.add(box);
    }

    /**
     * Selects all days.<p>
     * @param weekDays List of selected days
     * */
    private void setWeekDays(SortedSet<WeekDay> weekDays) {

        List<CmsCheckBox> checked = new ArrayList<CmsCheckBox>();

        for (WeekDay day : weekDays) {
            for (CmsCheckBox box : m_checkboxes) {
                if (box.getInternalValue().equals(day.toString())) {
                    checked.add(box);
                }
            }
        }
        for (CmsCheckBox box : m_checkboxes) {
            if (checked.contains(box)) {
                box.setChecked(true);
            } else {
                box.setChecked(false);
            }
        }

    }
}
