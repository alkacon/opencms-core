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

package org.opencms.gwt.client.ui.input.category;

import org.opencms.gwt.client.ui.CmsPushButton;
import org.opencms.gwt.client.ui.css.I_CmsInputLayoutBundle;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Style.Float;
import com.google.gwt.dom.client.Style.Overflow;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;

/**
 * Widget to generate an single row of values.<p> 
 * 
 * */
public class CmsDataValue extends Composite {

    /**
     * @see com.google.gwt.uibinder.client.UiBinder
     */
    interface I_CmsDataValueUiBinder extends UiBinder<Widget, CmsDataValue> {
        // GWT interface, nothing to do here
    }

    /** The ui-binder instance for this class. */
    private static I_CmsDataValueUiBinder uiBinder = GWT.create(I_CmsDataValueUiBinder.class);

    /** The label field. */
    @UiField
    Label m_label;

    /**The table. */
    @UiField
    FlexTable m_table;

    /**The image panel. */
    @UiField
    SimplePanel m_imagePanel;

    /** The width of this widget. */
    private int m_width;

    /** The part of the width that should be used for the label. */
    private int m_part;

    /** The values that should be shown in this widget. The first value is used for the label*/
    private String[] m_parameters;

    /** The css string for the image that is shown in front of the label. */
    private String m_image;

    /**
     * Constructor to generate the DataValueWidget with image.<p>
     *
     * @param width the width of this widget.
     * @param part the part of the width that should be used for the label
     * @param parameters the values that should be shown in this widget. The first value is used for the label
     * @param image the css string for the image that is shown in front of the label 
     */
    public CmsDataValue(int width, int part, String image, String... parameters) {

        initWidget(uiBinder.createAndBindUi(this));
        m_width = width;
        m_part = part;
        m_parameters = parameters;
        m_image = image;
        generateDataValue();

        addStyleName(I_CmsInputLayoutBundle.INSTANCE.inputCss().dataValue());
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
     * Returns if the category matches the given filter.<p>
     * 
     * @param filter the filter to match
     * @param param the search value
     * @return <code>true</code> if the gallery matches the given filter.<p>
     */
    public boolean matchesFilter(String filter, int param) {

        filter = filter.toLowerCase();
        return m_parameters[param].toLowerCase().contains(filter);
    }

    /**
     * Returns if the category matches the given filter.<p>
     * 
     * @param filter the filter to match
     * @param priValue the first search value
     * @param secValue the second search value
     * 
     * @return <code>true</code> if the gallery matches the given filter.<p>
     */
    public boolean matchesFilter(String filter, int priValue, int secValue) {

        filter = filter.toLowerCase();
        return m_parameters[priValue].toLowerCase().contains(filter)
            || m_parameters[secValue].toLowerCase().contains(filter);
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
     * Generates the widget an adds all parameter to the right place.<p>
     */
    private void generateDataValue() {

        int width_label = (m_width / m_part);
        int width_tabel = (m_width - 30) - width_label;
        int cell_width;
        if (m_parameters.length > 1) {
            cell_width = width_tabel / (m_parameters.length - 1);
        } else {
            cell_width = width_tabel;
        }

        m_table.getElement().getStyle().setFloat(Float.RIGHT);
        m_table.getElement().getStyle().setWidth(width_tabel, Unit.PX);

        m_label.getElement().setAttribute("style", "text-overflow:ellipsis; white-space: nowrap;");
        m_label.getElement().getStyle().setPaddingLeft(2, Unit.PX);
        m_label.getElement().getStyle().setPaddingTop(2, Unit.PX);
        m_label.getElement().getStyle().setPaddingRight(10, Unit.PX);
        m_label.getElement().getStyle().setPaddingBottom(2, Unit.PX);
        m_label.getElement().getStyle().setOverflow(Overflow.HIDDEN);
        if (m_image == null) {
            m_imagePanel.removeFromParent();
        } else {
            m_imagePanel.setStyleName(m_image);
        }

        m_table.insertRow(0);
        int i = 0;
        for (String parameter : m_parameters) {

            if (i > 0) {
                if (parameter.contains("hide:")) {
                    m_parameters[i] = parameter.replace("hide:", "");
                } else {

                    Label lable = new Label(parameter);

                    lable.getElement().setAttribute("style", "text-overflow:ellipsis; white-space: nowrap;");
                    lable.getElement().getStyle().setOverflow(Overflow.HIDDEN);
                    lable.getElement().getStyle().setWidth(cell_width, Unit.PX);
                    lable.setTitle(parameter);

                    m_table.setWidget(0, i - 1, lable);
                }
            } else {
                m_label.setText(parameter);
            }
            i++;

        }
    }

}