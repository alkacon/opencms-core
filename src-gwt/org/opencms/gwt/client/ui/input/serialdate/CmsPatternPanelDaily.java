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

import java.util.Iterator;

import com.google.gwt.dom.client.Style.Float;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;

/**
 * 
 * */
public class CmsPatternPanelDaily extends FlowPanel {

    /** Group off all radio buttons. */
    private CmsRadioButtonGroup m_group = new CmsRadioButtonGroup();

    /** The panel for all values of 'every'. */
    private FlowPanel m_everyPanel = new FlowPanel();

    /** The text box for the date input. */
    private TextBox m_everyDay = new TextBox();

    /** Array of all selections. */
    CmsRadioButton[] m_selection = new CmsRadioButton[2];

    /**
     * Default constructor to create the panel.<p>
     */
    public CmsPatternPanelDaily() {

        addStyleName(I_CmsLayoutBundle.INSTANCE.widgetCss().serialDateDay());
        CmsRadioButton sel1 = new CmsRadioButton("sel1", "Every");
        m_selection[0] = sel1;
        sel1.setGroup(m_group);
        sel1.setChecked(true);
        sel1.getElement().getStyle().setFloat(Float.LEFT);
        createEverPanel();
        CmsRadioButton sel2 = new CmsRadioButton("sel2", "Every working days");
        m_selection[1] = sel2;
        sel2.setGroup(m_group);
        this.add(sel1);
        this.add(m_everyPanel);

        this.add(sel2);

    }

    /**
     * Returns the interval.<p>
     * @return the interval
     * */
    public String getIterval() {

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
     * @param interval the interval that should be set
     * */
    public void setInterval(String interval) {

        m_everyDay.setText(interval);
    }

    /**
     * Sets the right selection.<p>
     * @param selection the selection that should be selected
     * */
    public void setSelection(int selection) {

        m_group.selectButton(m_selection[selection - 1]);

    }

    /**
     * Creates the 'every' selection view.<p>
     * 
     * */
    private void createEverPanel() {

        m_everyPanel.add(m_everyDay);
        m_everyDay.setStyleName(I_CmsLayoutBundle.INSTANCE.widgetCss().textBoxSerialDate());
        m_everyDay.getElement().getStyle().setWidth(25, Unit.PX);
        m_everyPanel.add(new Label("day(s)"));
    }

}
