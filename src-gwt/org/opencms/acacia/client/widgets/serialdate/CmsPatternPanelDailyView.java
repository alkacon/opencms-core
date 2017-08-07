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

import org.opencms.acacia.shared.I_CmsSerialDateValue.PatternType;
import org.opencms.ade.contenteditor.client.Messages;
import org.opencms.gwt.client.ui.input.CmsRadioButton;
import org.opencms.gwt.client.ui.input.CmsRadioButtonGroup;

import com.google.gwt.core.shared.GWT;
import com.google.gwt.dom.client.Element;
import com.google.gwt.event.dom.client.FocusEvent;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HTMLPanel;

/**
 * The daily pattern panel.<p>
 * */
public class CmsPatternPanelDailyView extends Composite implements I_CmsSerialDatePatternView {

    /** The UI binder interface. */
    interface I_CmsPatternPanelDailyUiBinder extends UiBinder<HTMLPanel, CmsPatternPanelDailyView> {
        // nothing to do
    }

    /** Name of the "every day" radio button. */
    private static final String EVERYDAY_RADIOBUTTON = "everyday";

    /** Name of the "working day" radio button. */
    private static final String WORKINGDAY_RADIOBUTTON = "workingday";

    /** The UI binder instance. */
    private static I_CmsPatternPanelDailyUiBinder uiBinder = GWT.create(I_CmsPatternPanelDailyUiBinder.class);

    /* UI elements for "every day". */

    /** The every day radio button. */
    @UiField(provided = true)
    CmsRadioButton m_everyRadioButton;

    /** The text box for the date input. */
    @UiField
    CmsFocusAwareTextBox m_everyDay;

    /** The days label. */
    @UiField
    Element m_labelDays;

    /* UI elements for "every working day" */

    /** The every working day radio button. */
    @UiField(provided = true)
    CmsRadioButton m_workingRadioButton;

    /** Group off all radio buttons. */
    CmsRadioButtonGroup m_group;

    /** The model to read the data from. */
    private final I_CmsObservableSerialDateValue m_model;

    /** The controller to handle changes. */
    final CmsPatternPanelDailyController m_controller;

    /** Flag, indicating if change actions should not be triggered. */
    private boolean m_triggerChangeActions = true;

    /**
     * Constructor to create the daily pattern panel.
     * @param controller the controller that handles value changes.
     * @param model the model that provides the values.
     */
    public CmsPatternPanelDailyView(CmsPatternPanelDailyController controller, I_CmsObservableSerialDateValue model) {
        m_controller = controller;
        m_model = model;
        m_model.registerValueChangeObserver(this);

        // init radio buttons
        m_group = new CmsRadioButtonGroup();
        m_everyRadioButton = new CmsRadioButton(
            EVERYDAY_RADIOBUTTON,
            Messages.get().key(Messages.GUI_SERIALDATE_DAILY_EVERY_0));

        m_everyRadioButton.setGroup(m_group);
        m_everyRadioButton.setChecked(true);
        m_workingRadioButton = new CmsRadioButton(
            WORKINGDAY_RADIOBUTTON,
            Messages.get().key(Messages.GUI_SERIALDATE_DAILY_EVERYWORKINGDAY_0));
        m_workingRadioButton.setGroup(m_group);
        m_group.addValueChangeHandler(new ValueChangeHandler<String>() {

            public void onValueChange(ValueChangeEvent<String> event) {

                if (handleChange()) {
                    m_controller.setEveryWorkingDay(m_workingRadioButton.isChecked());
                }
            }
        });
        initWidget(uiBinder.createAndBindUi(this));
        m_labelDays.setInnerText(Messages.get().key(Messages.GUI_SERIALDATE_DAILY_DAYS_0));
        m_everyDay.setFormValueAsString(m_model.getInterval() < 1 ? "" : "" + m_model.getInterval());
        m_everyDay.setTriggerChangeOnKeyPress(true);
    }

    /**
     * @see org.opencms.acacia.client.widgets.serialdate.I_CmsSerialDateValueChangeObserver#onValueChange()
     */
    public void onValueChange() {

        if (m_model.getPatternType().equals(PatternType.DAILY)) {
            m_triggerChangeActions = false;
            if (m_model.isEveryWorkingDay()) {
                m_group.selectButton(m_workingRadioButton);
                m_everyDay.setFormValueAsString("");
            } else {
                m_group.selectButton(m_everyRadioButton);
                if (!m_everyDay.isFocused()) {
                    m_everyDay.setFormValueAsString(String.valueOf(m_model.getInterval()));
                }
            }
            m_triggerChangeActions = true;
        }

    }

    /**
     * Returns a flag, indicating if change actions should be triggered.
     * @return a flag, indicating if change actions should be triggered.
     */
    boolean handleChange() {

        return m_triggerChangeActions;
    }

    /**
     * Handle interval change.
     * @param event the change event.
     */
    @UiHandler("m_everyDay")
    void onEveryDayChange(ValueChangeEvent<String> event) {

        if (handleChange()) {
            m_controller.setInterval(m_everyDay.getFormValueAsString());
        }

    }

    /**
     * Handle interval field focus.
     * @param event the focus event.
     */
    @UiHandler("m_everyDay")
    void onEveryDayFocus(FocusEvent event) {

        if (handleChange()) {
            m_group.selectButton(m_everyRadioButton);
        }

    }
}
