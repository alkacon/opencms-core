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
import org.opencms.gwt.client.ui.input.CmsSelectBox;

import com.google.gwt.core.shared.GWT;
import com.google.gwt.dom.client.Element;
import com.google.gwt.event.logical.shared.HasValueChangeHandlers;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.TextBox;

/**
 * The yearly pattern panel.<p>
 * */
public class CmsPatternPanelYearly extends Composite implements HasValueChangeHandlers<String> {

    /** The UI binder interface. */
    interface I_CmsPatternPanelYearlyUiBinder extends UiBinder<HTMLPanel, CmsPatternPanelYearly> {
        // nothing to do
    }

    /** The UI binder instance. */
    private static I_CmsPatternPanelYearlyUiBinder uiBinder = GWT.create(I_CmsPatternPanelYearlyUiBinder.class);

    /** The select box for the day selection. */
    @UiField
    CmsSelectBox m_atDay;

    /** The select box for the month selection. */
    @UiField
    CmsSelectBox m_atMonth;
    /** The select box for the nummeric selection. */
    @UiField
    CmsSelectBox m_atNumber;

    /** The at radio button. */
    @UiField(provided = true)
    CmsRadioButton m_atRadioButton;
    /** The text box for the date input. */
    @UiField
    TextBox m_everyDay;
    /** The select box for the month selection. */
    @UiField
    CmsSelectBox m_everyMonth;

    /** The every radio butoon. */
    @UiField(provided = true)
    CmsRadioButton m_everyRadioButton;

    /** The in label. */
    @UiField
    Element m_labelIn;

    /** Group off all radio buttons. */
    private CmsRadioButtonGroup m_group;

    /** The value change handler. */
    private ValueChangeHandler<String> m_handler;

    /**
     * Default constructor to create the panel.<p>
     */
    public CmsPatternPanelYearly() {

        m_group = new CmsRadioButtonGroup();
        m_everyRadioButton = new CmsRadioButton("sel1", Messages.get().key(Messages.GUI_SERIALDATE_YEARLY_EVERY_0));
        m_everyRadioButton.setGroup(m_group);
        m_everyRadioButton.setChecked(true);
        m_atRadioButton = new CmsRadioButton("sel2", Messages.get().key(Messages.GUI_SERIALDATE_YEARLY_AT_0));
        m_atRadioButton.setGroup(m_group);
        m_group.addValueChangeHandler(new ValueChangeHandler<String>() {

            public void onValueChange(ValueChangeEvent<String> event) {

                fireValueChange();
            }
        });
        initWidget(uiBinder.createAndBindUi(this));
        m_everyDay.setText("1");
        m_labelIn.setInnerText(Messages.get().key(Messages.GUI_SERIALDATE_YEARLY_IN_0));
        initSelectBoxes();
    }

    /**
     * @see com.google.gwt.event.logical.shared.HasValueChangeHandlers#addValueChangeHandler(com.google.gwt.event.logical.shared.ValueChangeHandler)
     */
    public HandlerRegistration addValueChangeHandler(ValueChangeHandler<String> handler) {

        m_handler = handler;
        m_atNumber.addValueChangeHandler(m_handler);
        m_atDay.addValueChangeHandler(m_handler);
        m_atMonth.addValueChangeHandler(m_handler);
        m_everyDay.addValueChangeHandler(m_handler);
        m_everyMonth.addValueChangeHandler(m_handler);
        return addHandler(handler, ValueChangeEvent.getType());
    }

    /**
     * Represents a value change event.<p>
     */
    public void fireValueChange() {

        ValueChangeEvent.fire(this, getWeekDays());
    }

    /**
     * Returns the day of month.<p>
     * @return the day of month
     * */
    public String getDayOfMonth() {

        if (m_group.getSelectedButton().equals(m_everyRadioButton)) {
            return m_everyDay.getText();
        } else {
            return m_atNumber.getFormValueAsString();
        }
    }

    /**
     * Returns the month.<p>
     * @return the month
     * */
    public String getMonth() {

        if (m_group.getSelectedButton().equals(m_everyRadioButton)) {
            return m_everyMonth.getFormValueAsString();
        } else {
            return m_atMonth.getFormValueAsString();
        }

    }

    /**
     * Returns the week day.<p>
     * @return the week day
     * */
    public String getWeekDays() {

        if (m_group.getSelectedButton().equals(m_everyRadioButton)) {
            return "-1";
        } else {
            return m_atDay.getFormValueAsString();
        }

    }

    /**
     * Sets the interval.<p>
     *
     * @param dayOfMonth the interval
     * */
    public void setDayOfMonth(int dayOfMonth) {

        if (m_group.getSelectedButton().equals(m_everyRadioButton)) {
            m_everyDay.setText(dayOfMonth + "");
        } else {
            m_atNumber.selectValue(dayOfMonth + "");
        }

    }

    /**
     * Sets the month.<p>
     * @param month the month
     * */
    public void setMonth(int month) {

        if (m_group.getSelectedButton().equals(m_everyRadioButton)) {
            m_everyMonth.selectValue(month + "");
        } else {
            m_atMonth.selectValue(month + "");
        }

    }

    /**
     * Sets the week day.<p>
     *
     *  @param weekDay the week day
     * */
    public void setWeekDay(int weekDay) {

        if (weekDay == -1) {
            m_group.selectButton(m_everyRadioButton);
        } else {
            m_group.selectButton(m_atRadioButton);
            m_atDay.selectValue(weekDay + "");
        }

    }

    /**
     * Creates the 'at' selection view.<p>
     * */
    private void initSelectBoxes() {

        m_atNumber.getOpener().setStyleName(
            org.opencms.acacia.client.css.I_CmsWidgetsLayoutBundle.INSTANCE.widgetCss().selectBoxSelected());
        m_atNumber.getSelectorPopup().addStyleName(I_CmsLayoutBundle.INSTANCE.globalWidgetCss().selectBoxPopup());
        m_atNumber.setWidth("80px");
        m_atNumber.addOption("1", Messages.get().key(Messages.GUI_SERIALDATE_WEEKDAYNUMBER_1_0));
        m_atNumber.addOption("2", Messages.get().key(Messages.GUI_SERIALDATE_WEEKDAYNUMBER_2_0));
        m_atNumber.addOption("3", Messages.get().key(Messages.GUI_SERIALDATE_WEEKDAYNUMBER_3_0));
        m_atNumber.addOption("4", Messages.get().key(Messages.GUI_SERIALDATE_WEEKDAYNUMBER_4_0));
        m_atNumber.addOption("5", Messages.get().key(Messages.GUI_SERIALDATE_WEEKDAYNUMBER_5_0));
        m_atDay.getOpener().setStyleName(
            org.opencms.acacia.client.css.I_CmsWidgetsLayoutBundle.INSTANCE.widgetCss().selectBoxSelected());
        m_atDay.getSelectorPopup().addStyleName(I_CmsLayoutBundle.INSTANCE.globalWidgetCss().selectBoxPopup());
        m_atDay.setWidth("100px");
        m_atDay.addOption("1", Messages.get().key(Messages.GUI_SERIALDATE_DAY_SUNDAY_0));
        m_atDay.addOption("2", Messages.get().key(Messages.GUI_SERIALDATE_DAY_MONDAY_0));
        m_atDay.addOption("3", Messages.get().key(Messages.GUI_SERIALDATE_DAY_TUESDAY_0));
        m_atDay.addOption("4", Messages.get().key(Messages.GUI_SERIALDATE_DAY_WEDNESDAY_0));
        m_atDay.addOption("5", Messages.get().key(Messages.GUI_SERIALDATE_DAY_THURSDAY_0));
        m_atDay.addOption("6", Messages.get().key(Messages.GUI_SERIALDATE_DAY_FRIDAY_0));
        m_atDay.addOption("7", Messages.get().key(Messages.GUI_SERIALDATE_DAY_SATURDAY_0));

        m_atMonth.getOpener().setStyleName(
            org.opencms.acacia.client.css.I_CmsWidgetsLayoutBundle.INSTANCE.widgetCss().selectBoxSelected());
        m_everyMonth.getSelectorPopup().addStyleName(I_CmsLayoutBundle.INSTANCE.globalWidgetCss().selectBoxPopup());
        m_atMonth.setWidth("100px");
        m_atMonth.addOption("0", Messages.get().key(Messages.GUI_SERIALDATE_YEARLY_JAN_0));
        m_atMonth.addOption("1", Messages.get().key(Messages.GUI_SERIALDATE_YEARLY_FEB_0));
        m_atMonth.addOption("2", Messages.get().key(Messages.GUI_SERIALDATE_YEARLY_MAR_0));
        m_atMonth.addOption("3", Messages.get().key(Messages.GUI_SERIALDATE_YEARLY_APR_0));
        m_atMonth.addOption("4", Messages.get().key(Messages.GUI_SERIALDATE_YEARLY_MAY_0));
        m_atMonth.addOption("5", Messages.get().key(Messages.GUI_SERIALDATE_YEARLY_JUN_0));
        m_atMonth.addOption("6", Messages.get().key(Messages.GUI_SERIALDATE_YEARLY_JUL_0));
        m_atMonth.addOption("7", Messages.get().key(Messages.GUI_SERIALDATE_YEARLY_AUG_0));
        m_atMonth.addOption("8", Messages.get().key(Messages.GUI_SERIALDATE_YEARLY_SEP_0));
        m_atMonth.addOption("9", Messages.get().key(Messages.GUI_SERIALDATE_YEARLY_OCT_0));
        m_atMonth.addOption("10", Messages.get().key(Messages.GUI_SERIALDATE_YEARLY_NOV_0));
        m_atMonth.addOption("11", Messages.get().key(Messages.GUI_SERIALDATE_YEARLY_DEC_0));

        m_everyMonth.getOpener().setStyleName(
            org.opencms.acacia.client.css.I_CmsWidgetsLayoutBundle.INSTANCE.widgetCss().selectBoxSelected());
        m_everyMonth.getSelectorPopup().addStyleName(I_CmsLayoutBundle.INSTANCE.globalWidgetCss().selectBoxPopup());
        m_everyMonth.setWidth("100px");
        m_everyMonth.addOption("0", Messages.get().key(Messages.GUI_SERIALDATE_YEARLY_JAN_0));
        m_everyMonth.addOption("1", Messages.get().key(Messages.GUI_SERIALDATE_YEARLY_FEB_0));
        m_everyMonth.addOption("2", Messages.get().key(Messages.GUI_SERIALDATE_YEARLY_MAR_0));
        m_everyMonth.addOption("3", Messages.get().key(Messages.GUI_SERIALDATE_YEARLY_APR_0));
        m_everyMonth.addOption("4", Messages.get().key(Messages.GUI_SERIALDATE_YEARLY_MAY_0));
        m_everyMonth.addOption("5", Messages.get().key(Messages.GUI_SERIALDATE_YEARLY_JUN_0));
        m_everyMonth.addOption("6", Messages.get().key(Messages.GUI_SERIALDATE_YEARLY_JUL_0));
        m_everyMonth.addOption("7", Messages.get().key(Messages.GUI_SERIALDATE_YEARLY_AUG_0));
        m_everyMonth.addOption("8", Messages.get().key(Messages.GUI_SERIALDATE_YEARLY_SEP_0));
        m_everyMonth.addOption("9", Messages.get().key(Messages.GUI_SERIALDATE_YEARLY_OCT_0));
        m_everyMonth.addOption("10", Messages.get().key(Messages.GUI_SERIALDATE_YEARLY_NOV_0));
        m_everyMonth.addOption("11", Messages.get().key(Messages.GUI_SERIALDATE_YEARLY_DEC_0));

    }

}
