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

package org.opencms.gwt.client.ui.input.serialdate;

import org.opencms.ade.contenteditor.client.css.I_CmsLayoutBundle;
import org.opencms.gwt.client.ui.input.CmsRadioButton;
import org.opencms.gwt.client.ui.input.CmsRadioButtonGroup;
import org.opencms.gwt.client.ui.input.CmsSelectBox;

import java.util.Iterator;

import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;

/**
 * 
 * */
public class CmsPatternPanelYearly extends FlowPanel {

    /** Group off all radio buttons. */
    private CmsRadioButtonGroup m_group = new CmsRadioButtonGroup();

    /** The panel for all values of 'every'. */
    private FlowPanel m_everyPanel = new FlowPanel();

    /** The panel for all values of 'at'. */
    private FlowPanel m_atPanel = new FlowPanel();

    /** The text box for the date input. */
    private TextBox m_everyDay = new TextBox();
    /** The select box for the month selection. */
    private CmsSelectBox m_everyMonth = new CmsSelectBox();

    /** The select box for the nummeric selection. */
    private CmsSelectBox m_atNummer = new CmsSelectBox();
    /** The select box for the day selection. */
    private CmsSelectBox m_atDay = new CmsSelectBox();
    /** The select box for the month selection. */
    private CmsSelectBox m_atMonth = new CmsSelectBox();

    /** The array of all radio button. */
    private CmsRadioButton[] m_radio = new CmsRadioButton[2];

    /**
     * Default constructor to create the panel.<p>
     */
    public CmsPatternPanelYearly() {

        addStyleName(I_CmsLayoutBundle.INSTANCE.widgetCss().serialDateYear());
        CmsRadioButton sel1 = new CmsRadioButton("sel1", "Every");
        m_radio[0] = sel1;
        sel1.setGroup(m_group);
        sel1.setChecked(true);
        sel1.addStyleName(I_CmsLayoutBundle.INSTANCE.widgetCss().serialDateYearSelection());
        createEverPanel();
        CmsRadioButton sel2 = new CmsRadioButton("sel2", "At");
        m_radio[1] = sel2;
        sel2.addStyleName(I_CmsLayoutBundle.INSTANCE.widgetCss().serialDateYearSelection());
        sel2.setGroup(m_group);
        createAtPanel();
        this.add(sel1);
        this.add(m_everyPanel);

        this.add(sel2);
        this.add(m_atPanel);

    }

    /**
     * Returns the day of month.<p>
     * @return the day of month
     * */
    public String getDayOfMonth() {

        if (m_group.getSelectedButton().equals(m_radio[0])) {
            return m_everyDay.getText();
        } else {
            return m_atNummer.getFormValueAsString();
        }
    }

    /**
     * Returns the month.<p>
     * @return the month
     * */
    public String getMonth() {

        if (m_group.getSelectedButton().equals(m_radio[0])) {
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

        if (m_group.getSelectedButton().equals(m_radio[0])) {
            return "-1";
        } else {
            return m_atDay.getFormValueAsString();
        }

    }

    /**
     * @see com.google.gwt.user.client.ui.HasWidgets#iterator()
     */
    @Override
    public Iterator<Widget> iterator() {

        Iterator<Widget> result = getChildren().iterator();
        return result;
    }

    /**
     * @see com.google.gwt.user.client.ui.Panel#remove(com.google.gwt.user.client.ui.Widget)
     */
    @Override
    public boolean remove(Widget child) {

        return remove(child);
    }

    /**
     * Sets the interval.<p>
     * 
     * @param dayOfMonth the interval
     * */
    public void setDayOfMonth(int dayOfMonth) {

        if (m_group.getSelectedButton().equals(m_radio[0])) {
            m_everyDay.setText(dayOfMonth + "");
        } else {
            m_atNummer.selectValue(dayOfMonth + "");
        }

    }

    /**
     * Sets the month.<p>
     * @param month the month
     * */
    public void setMonth(int month) {

        if (m_group.getSelectedButton().equals(m_radio[0])) {
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
            m_group.selectButton(m_radio[0]);
        } else {
            m_group.selectButton(m_radio[1]);
            m_atDay.selectValue(weekDay + "");
        }

    }

    /**
     * Creates the 'at' selection view.<p>
     * */
    private void createAtPanel() {

        m_atPanel.add(m_atNummer);
        m_atNummer.setWidth("80px");
        m_atNummer.addOption("1", "first");
        m_atNummer.addOption("2", "second");
        m_atNummer.addOption("3", "third");
        m_atNummer.addOption("4", "fourth");
        m_atNummer.addOption("5", "fifth");
        m_atPanel.add(m_atDay);
        m_atDay.setWidth("80px");
        m_atDay.addOption("1", "Sunday");
        m_atDay.addOption("2", "Monday");
        m_atDay.addOption("3", "Tuesday");
        m_atDay.addOption("4", "Wednesday");
        m_atDay.addOption("5", "Thurday");
        m_atDay.addOption("6", "Friday");
        m_atDay.addOption("7", "Saturday");

        m_atPanel.add(new Label("in"));
        m_atPanel.add(m_atMonth);
        m_atMonth.setWidth("80px");
        m_atMonth.addOption("0", "January");
        m_atMonth.addOption("1", "February");
        m_atMonth.addOption("2", "March");
        m_atMonth.addOption("3", "April");
        m_atMonth.addOption("4", "May");
        m_atMonth.addOption("5", "June");
        m_atMonth.addOption("6", "July");
        m_atMonth.addOption("7", "August");
        m_atMonth.addOption("8", "Septemer");
        m_atMonth.addOption("9", "October");
        m_atMonth.addOption("10", "November");
        m_atMonth.addOption("11", "December");

    }

    /**
     * Creates the 'every' selection view.<p>
     * 
     * */
    private void createEverPanel() {

        m_everyPanel.add(m_everyDay);
        m_everyDay.setStyleName(I_CmsLayoutBundle.INSTANCE.widgetCss().textBoxSerialDate());
        m_everyPanel.add(new Label("."));
        m_everyPanel.add(m_everyMonth);
        m_everyMonth.setWidth("80px");
        m_everyMonth.addOption("0", "January");
        m_everyMonth.addOption("1", "February");
        m_everyMonth.addOption("2", "March");
        m_everyMonth.addOption("3", "April");
        m_everyMonth.addOption("4", "May");
        m_everyMonth.addOption("5", "June");
        m_everyMonth.addOption("6", "July");
        m_everyMonth.addOption("7", "August");
        m_everyMonth.addOption("8", "Septemer");
        m_everyMonth.addOption("9", "October");
        m_everyMonth.addOption("10", "November");
        m_everyMonth.addOption("11", "December");

    }

}
