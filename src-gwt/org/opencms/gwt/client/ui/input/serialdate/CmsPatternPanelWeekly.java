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
import org.opencms.gwt.client.ui.input.CmsCheckBox;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;

/**
 * 
 * */
public class CmsPatternPanelWeekly extends FlowPanel {

    /** The panel for all values of 'every'. */
    private FlowPanel m_everyPanel = new FlowPanel();

    /** The panel for all values of the day selection. */
    private FlowPanel m_dayPanel = new FlowPanel();

    /** The text box for the date input. */
    private TextBox m_everyDay = new TextBox();

    /** The array of all checkboxes. */
    List<CmsCheckBox> m_checkboxes = new ArrayList<CmsCheckBox>();

    /**
     * Default constructor to create the panel.<p>
     */
    public CmsPatternPanelWeekly() {

        addStyleName(I_CmsLayoutBundle.INSTANCE.widgetCss().serialDateWeek());
        createEverPanel();
        this.add(m_everyPanel);
        createDayPanel();
        for (CmsCheckBox box : m_checkboxes) {
            box.addStyleName(I_CmsLayoutBundle.INSTANCE.widgetCss().serialDateCheckBox());
            m_dayPanel.add(box);
        }
        this.add(m_dayPanel);

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

        CmsCheckBox test = new CmsCheckBox("Monday");
        test.setInternalValue("2");
        m_checkboxes.add(test);
        test = new CmsCheckBox("Tuesday");
        test.setInternalValue("3");
        m_checkboxes.add(test);
        test = new CmsCheckBox("Wednesday");
        test.setInternalValue("4");
        m_checkboxes.add(test);
        test = new CmsCheckBox("Thursday");
        test.setInternalValue("5");
        m_checkboxes.add(test);
        test = new CmsCheckBox("Friday");
        test.setInternalValue("6");
        m_checkboxes.add(test);
        test = new CmsCheckBox("Saturday");
        test.setInternalValue("7");
        m_checkboxes.add(test);
        test = new CmsCheckBox("Sunday");
        test.setInternalValue("1");
        m_checkboxes.add(test);
    }

    /**
     * Creates the 'every' selection view.<p>
     * 
     * */
    private void createEverPanel() {

        m_everyPanel.add(new Label("Every"));
        m_everyPanel.add(m_everyDay);
        m_everyDay.setStyleName(I_CmsLayoutBundle.INSTANCE.widgetCss().textBoxSerialDate());
        m_everyPanel.add(new Label("week(s) at"));
    }

}
