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
import org.opencms.gwt.client.ui.input.CmsRadioButton;
import org.opencms.gwt.client.ui.input.CmsRadioButtonGroup;

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
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.TextBox;

/**
 * The daily pattern panel.<p>
 * */
public class CmsPatternPanelDaily extends Composite implements HasValueChangeHandlers<String> {

    /** The UI binder interface. */
    interface I_CmsPatternPanelDailyUiBinder extends UiBinder<HTMLPanel, CmsPatternPanelDaily> {
        // nothing to do
    }

    /** The UI binder instance. */
    private static I_CmsPatternPanelDailyUiBinder uiBinder = GWT.create(I_CmsPatternPanelDailyUiBinder.class);

    /** The text box for the date input. */
    @UiField
    TextBox m_everyDay;

    /** The every day radio button. */
    @UiField(provided = true)
    CmsRadioButton m_everyRadioButton;

    /** The days label. */
    @UiField
    Element m_labelDays;

    /** The every working day radio button. */
    @UiField(provided = true)
    CmsRadioButton m_workingRadioButton;

    /** Group off all radio buttons. */
    private CmsRadioButtonGroup m_group;

    /** Value change handler. */
    private ValueChangeHandler<String> m_handler;

    /** Activation status. */
    private boolean m_active = true;

    /**
     * Default constructor to create the panel.<p>
     */
    public CmsPatternPanelDaily() {

        // init radio buttons
        m_group = new CmsRadioButtonGroup();
        m_everyRadioButton = new CmsRadioButton("sel1", Messages.get().key(Messages.GUI_SERIALDATE_DAILY_EVERY_0));

        m_everyRadioButton.setGroup(m_group);
        m_everyRadioButton.setChecked(true);
        m_workingRadioButton = new CmsRadioButton(
            "sel2",
            Messages.get().key(Messages.GUI_SERIALDATE_DAILY_EVERYWORKINGDAY_0));
        m_workingRadioButton.setGroup(m_group);
        m_group.addValueChangeHandler(new ValueChangeHandler<String>() {

            public void onValueChange(ValueChangeEvent<String> event) {

                fireValueChange();
            }
        });
        initWidget(uiBinder.createAndBindUi(this));
        m_labelDays.setInnerText(Messages.get().key(Messages.GUI_SERIALDATE_DAILY_DAYS_0));
        m_everyDay.setValue("1");

    }

    /**
     * @see com.google.gwt.event.logical.shared.HasValueChangeHandlers#addValueChangeHandler(com.google.gwt.event.logical.shared.ValueChangeHandler)
     */
    public HandlerRegistration addValueChangeHandler(ValueChangeHandler<String> handler) {

        m_handler = handler;
        m_everyDay.addValueChangeHandler(m_handler);
        return addHandler(handler, ValueChangeEvent.getType());
    }

    /**
     * Represents a value change event.<p>
     */
    public void fireValueChange() {

        if (m_active) {
            ValueChangeEvent.fire(this, getWorkingDay());
        }
    }

    /**
     * Returns the interval.<p>
     * @return the interval
     * */
    public String getInterval() {

        return m_everyDay.getText();
    }

    /**
     * Returns the selection.<p>
     * @return the selection
     * */
    public String getWorkingDay() {

        boolean result;
        result = m_group.getSelectedButton().getName().equals("sel2");
        return result + "";
    }

    /**
     * Handles the days key press event.<p>
     *
     * @param event the event
     */
    @UiHandler("m_everyDay")
    public void onDaysKeyPress(KeyPressEvent event) {

        fireValueChange();
    }

    /**
     * Sets the panel active.<p>
     *
     * @param active if active
     */
    public void setActive(boolean active) {

        if (active != m_active) {
            m_active = active;
            m_everyRadioButton.setEnabled(active);
            m_workingRadioButton.setEnabled(active);
            m_everyDay.setEnabled(active);
            m_everyRadioButton.setChecked(true);
            m_everyDay.setText("1");
        }
    }

    /**
     * Sets the interval.<p>
     * @param interval the interval that should be set
     * */
    public void setInterval(String interval) {

        m_everyDay.setText(interval);
    }

    /**
     * Sets the right selection.<p>
     * @param selection the selection that should be selected
     * */
    public void setWorkingDaySelection(boolean selection) {

        m_group.selectButton(selection ? m_workingRadioButton : m_everyRadioButton);
    }
}
