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

package org.opencms.gwt.client.ui.input;

import com.alkacon.geranium.client.ui.I_Truncable;

import org.opencms.gwt.client.ui.CmsPushButton;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Style.Float;
import com.google.gwt.dom.client.Style.Overflow;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;

/***/
public class CmsDataValue extends Composite implements I_Truncable {

    /***/
    interface MyUiBinder extends UiBinder<Widget, CmsDataValue> {
        //TODO
    }

    /***/
    private static MyUiBinder uiBinder = GWT.create(MyUiBinder.class);

    /***/
    @UiField
    Label m_label;

    /***/
    @UiField
    FlexTable m_table;

    /***/
    private int m_width;
    /***/
    private int m_part;
    /***/
    private String[] m_parameters;

    /**
     * 
     *
     * @param width 
     * @param part 
     * @param parameters 
     */
    public CmsDataValue(int width, int part, String... parameters) {

        m_width = width;
        m_part = part;
        m_parameters = parameters;
        generateDataValue();
    }

    /**
     * Adds a single button to the view.
     * 
     * @param button the button that should be added
     * */
    public void addButton(CmsPushButton button) {

        m_table.setWidget(0, m_table.getCellCount(0), button);
    }

    /**
     * Returns the label of this widget.<p>
     * @return the label of this widget
     */
    public String getLabel() {

        return m_parameters[0];
    }

    /**
     * Returns the requested parameter.<p>
     * @param i the index of the parameter
     * @return the requested parameter
     */
    public String getParameter(int i) {

        return m_parameters[i];
    }

    /**
     * Sets the color.<p>
     * 
     * @param color the color that should be set
     * */
    public void setColor(String color) {

        m_label.getElement().getStyle().setColor(color);
    }

    /**
     * @see com.alkacon.geranium.client.ui.I_Truncable#truncate(java.lang.String, int)
     */
    public void truncate(String textMetricsKey, int clientWidth) {

        m_width = clientWidth;
        generateDataValue();

    }

    /**
     * 
     */
    private void generateDataValue() {

        int width_label = (m_width / m_part);
        int width_tabel = (m_width - 30) - width_label;
        int cell_width = width_tabel / (m_parameters.length - 1);
        initWidget(uiBinder.createAndBindUi(this));

        m_table.getElement().getStyle().setFloat(Float.RIGHT);
        m_table.getElement().getStyle().setWidth(width_tabel, Unit.PX);
        m_table.getElement().getStyle().setMarginTop(-2, Unit.PX);

        m_label.getElement().setAttribute("style", "text-overflow:ellipsis; white-space: nowrap;");
        m_label.getElement().getStyle().setPaddingLeft(2, Unit.PX);
        m_label.getElement().getStyle().setPaddingTop(2, Unit.PX);
        m_label.getElement().getStyle().setPaddingRight(10, Unit.PX);
        m_label.getElement().getStyle().setPaddingBottom(2, Unit.PX);
        m_label.getElement().getStyle().setOverflow(Overflow.HIDDEN);

        m_table.insertRow(0);
        int i = 0;
        for (String parameter : m_parameters) {

            if (i > 0) {
                Label lable = new Label(parameter);

                lable.getElement().setAttribute("style", "text-overflow:ellipsis; white-space: nowrap;");
                lable.getElement().getStyle().setOverflow(Overflow.HIDDEN);
                lable.getElement().getStyle().setWidth(cell_width, Unit.PX);
                lable.setTitle(parameter);

                //m_table.insertCell(0, i);
                m_table.setWidget(0, i - 1, lable);
            } else {
                m_label.setText(parameter);
            }
            i++;

        }
    }
}