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

import org.opencms.ade.contenteditor.client.Messages;
import org.opencms.gwt.client.ui.input.CmsCheckBox;

import java.util.ArrayList;
import java.util.List;

import com.google.gwt.core.shared.GWT;
import com.google.gwt.dom.client.Element;
import com.google.gwt.event.dom.client.KeyPressEvent;
import com.google.gwt.event.logical.shared.HasValueChangeHandlers;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.TextBox;

/**
 * The weekly pattern panel.<p>
 * */
public class CmsPatternPanelWeekly extends Composite implements HasValueChangeHandlers<String> {

    /** The UI binder interface. */
    interface I_CmsPatternPanelWeekly extends UiBinder<HTMLPanel, CmsPatternPanelWeekly> {
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
    TextBox m_everyDay = new TextBox();

    /** The every label. */
    @UiField
    Element m_labelEvery;

    /** The weeks label. */
    @UiField
    Element m_labelWeeks;

    /** The handler. */
    private ValueChangeHandler<String> m_handler;

    /**
     * Default constructor to create the panel.<p>
     */
    public CmsPatternPanelWeekly() {

        initWidget(uiBinder.createAndBindUi(this));
        m_labelEvery.setInnerText(Messages.get().key(Messages.GUI_SERIALDATE_WEEKLY_EVERY_0));
        m_labelWeeks.setInnerText(Messages.get().key(Messages.GUI_SERIALDATE_WEEKLY_WEEK_AT_0));
        createDayPanel();
    }

    /**
     * @see com.google.gwt.event.logical.shared.HasValueChangeHandlers#addValueChangeHandler(com.google.gwt.event.logical.shared.ValueChangeHandler)
     */
    public HandlerRegistration addValueChangeHandler(ValueChangeHandler<String> handler) {

        m_handler = handler;
        m_everyDay.addValueChangeHandler(m_handler);
        for (CmsCheckBox box : m_checkboxes) {
            box.addValueChangeHandler(new ValueChangeHandler<Boolean>() {

                public void onValueChange(ValueChangeEvent<Boolean> event) {

                    fireValueChange();

                }

            });
        }
        return addHandler(handler, ValueChangeEvent.getType());
    }

    /**
     * Represents a value change event.<p>
     */
    public void fireValueChange() {

        ValueChangeEvent.fire(this, getWeekDays());
    }

    /**
     * Returns the interval.<p>
     * @return the interval
     * */
    public String getInterval() {

        return m_everyDay.getText();
    }

    /**
     * Returns all selected days.<p>
     * @return all selected days
     * */
    public String getWeekDays() {

        String result = "";
        int i = 0;
        for (CmsCheckBox box : m_checkboxes) {
            if (box.isChecked()) {
                if (i > 0) {
                    result += ",";
                }
                result += box.getInternalValue();
                i++;
            }
        }

        return result;
    }

    /**
     * Handles the days key press event.<p>
     *
     * @param event the key press event
     */
    @UiHandler("m_everyDay")
    public void onDaysKeyPress(KeyPressEvent event) {

        fireValueChange();
    }

    /**
     * Sets the interval.<p>
     * @param intervalStr the interval
     * */
    public void setInterval(String intervalStr) {

        m_everyDay.setText(intervalStr);

    }

    /**
     * Selects all days.<p>
     * @param weekDaysStrList List of selected days
     * */
    public void setWeekDays(List<String> weekDaysStrList) {

        List<CmsCheckBox> checked = new ArrayList<CmsCheckBox>();

        for (String day : weekDaysStrList) {
            for (CmsCheckBox box : m_checkboxes) {
                if (box.getInternalValue().equals(day)) {
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

    /**
     * Creates the day selection view.<p>
     * */
    private void createDayPanel() {

        CmsCheckBox box = new CmsCheckBox(Messages.get().key(Messages.GUI_SERIALDATE_DAY_MONDAY_0));
        box.setInternalValue("2");
        m_checkboxes.add(box);
        m_dayPanel.add(box);
        box = new CmsCheckBox(Messages.get().key(Messages.GUI_SERIALDATE_DAY_TUESDAY_0));
        box.setInternalValue("3");
        m_checkboxes.add(box);
        m_dayPanel.add(box);
        box = new CmsCheckBox(Messages.get().key(Messages.GUI_SERIALDATE_DAY_WEDNESDAY_0));
        box.setInternalValue("4");
        m_checkboxes.add(box);
        m_dayPanel.add(box);
        box = new CmsCheckBox(Messages.get().key(Messages.GUI_SERIALDATE_DAY_THURSDAY_0));
        box.setInternalValue("5");
        m_checkboxes.add(box);
        m_dayPanel.add(box);
        box = new CmsCheckBox(Messages.get().key(Messages.GUI_SERIALDATE_DAY_FRIDAY_0));
        box.setInternalValue("6");
        m_checkboxes.add(box);
        m_dayPanel.add(box);
        box = new CmsCheckBox(Messages.get().key(Messages.GUI_SERIALDATE_DAY_SATURDAY_0));
        box.setInternalValue("7");
        m_checkboxes.add(box);
        m_dayPanel.add(box);
        box = new CmsCheckBox(Messages.get().key(Messages.GUI_SERIALDATE_DAY_SUNDAY_0));
        box.setInternalValue("1");
        m_checkboxes.add(box);
        m_dayPanel.add(box);
    }
}
