/*
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (c) Alkacon Software GmbH & Co. KG (http://www.alkacon.com)
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

import org.opencms.gwt.client.ui.I_CmsTruncable;
import org.opencms.gwt.client.ui.css.I_CmsLayoutBundle;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.HasClickHandlers;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;

/**
 * Widget to generate an single row of values.<p>
 *
 * */
public class CmsDataValue extends Composite implements I_CmsTruncable, HasClickHandlers {

    /**
     * @see com.google.gwt.uibinder.client.UiBinder
     */
    interface I_CmsDataValueUiBinder extends UiBinder<Widget, CmsDataValue> {
        // GWT interface, nothing to do here
    }

    /** Internal CSS style interface. */
    interface I_Style extends CssResource {

        /**
         * Returns the CSS style name.<p>
         *
         * @return the CSS style name
         */
        String buttonPanel();

        /**
         * Returns the CSS style name.<p>
         *
         * @return the CSS style name
         */
        String icon();

        /**
         * Returns the CSS style name.<p>
         *
         * @return the CSS style name
         */
        String label();

        /**
         * Returns the CSS style name.<p>
         *
         * @return the CSS style name
         */
        String parameter();

        /**
         * Returns the CSS style name.<p>
         *
         * @return the CSS style name
         */
        String searchMatch();

        /**
         * Returns the CSS style name.<p>
         *
         * @return the CSS style name
         */
        String table();
    }

    /** The ui-binder instance for this class. */
    private static I_CmsDataValueUiBinder uiBinder = GWT.create(I_CmsDataValueUiBinder.class);

    /** The button panel. */
    @UiField
    FlowPanel m_buttonPanel;

    /**The image panel. */
    @UiField
    SimplePanel m_imagePanel;

    /** The label field. */
    @UiField
    Label m_label;

    /** The CSS bundle instance. */
    @UiField
    I_Style m_style;

    /** The table. */
    @UiField
    FlexTable m_table;

    /** The css string for the image that is shown in front of the label. */
    private String m_image;

    /** The values that should be shown in this widget. The first value is used for the label*/
    private String[] m_parameters;

    /** The part of the width that should be used for the label. */
    private int m_part;

    /** The width of this widget. */
    private int m_width;

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
        m_part = part;
        m_parameters = parameters;
        m_image = image;
        generateDataValue();
        setWidth(width);
    }

    /**
     * Adds buttons to the view.
     *
     * @param buttons the buttons that should be added
     * */
    public void addButton(Widget... buttons) {

        for (Widget button : buttons) {
            m_buttonPanel.add(button);
        }
    }

    /**
     * @see com.google.gwt.event.dom.client.HasClickHandlers#addClickHandler(com.google.gwt.event.dom.client.ClickHandler)
     */
    public HandlerRegistration addClickHandler(ClickHandler clickHandler) {

        return addDomHandler(clickHandler, ClickEvent.getType());
    }

    /**
     * Returns the label of this widget.<p>
     * @return the label of this widget
     */
    public String getLabel() {

        return m_parameters[0];
    }

    /**
     * Gets the label widget.<p>
     *
     * @return the label widget
     */
    public Label getLabelWidget() {

        return m_label;
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
     * Sets the style if the data value should be inactive.<p>
     * */
    public void setInactive() {

        getElement().getStyle().setBorderColor(I_CmsLayoutBundle.INSTANCE.constants().css().borderColor());
        getElement().getStyle().setColor(I_CmsLayoutBundle.INSTANCE.constants().css().textColorDisabled());
    }

    /**
     * Enables / disables the 'search match' style for this widget.<p>
     *
     * @param isSearchMatch true if 'search match' style should be enabled
     */
    public void setSearchMatch(boolean isSearchMatch) {

        if (isSearchMatch) {
            addStyleName(m_style.searchMatch());
        } else {
            removeStyleName(m_style.searchMatch());
        }
    }

    /**
     * Makes the content of the list info box unselectable.<p>
     */
    public void setUnselectable() {

        getWidget().addStyleName(I_CmsLayoutBundle.INSTANCE.listItemWidgetCss().unselectable());
    }

    /**
     * @see org.opencms.gwt.client.ui.I_CmsTruncable#truncate(java.lang.String, int)
     */
    public void truncate(String textMetricsKey, int clientWidth) {

        setWidth(clientWidth);
    }

    /**
     * Generates the widget an adds all parameter to the right place.<p>
     */
    private void generateDataValue() {

        if (m_image == null) {
            m_imagePanel.removeFromParent();
        } else {
            m_imagePanel.addStyleName(m_image);
        }
        m_table.insertRow(0);
        int i = 0;
        for (String parameter : m_parameters) {

            if (i > 0) {
                if (parameter.contains("hide:")) {
                    m_parameters[i] = parameter.replace("hide:", "");
                } else {

                    Label label = new Label(parameter);
                    label.setStyleName(m_style.parameter());
                    label.setTitle(parameter);
                    m_table.setWidget(0, i - 1, label);
                }
            } else {
                m_label.setText(parameter);
                m_label.setTitle(parameter);
            }
            i++;
        }
    }

    /**
     * Sets the widget width.<p>
     *
     * @param width the widget width
     */
    private void setWidth(int width) {

        m_width = width;
        int width_label = (m_width / m_part);
        int width_table = (m_width - 30) - width_label;
        m_table.getElement().getStyle().setWidth(width_table, Unit.PX);
    }

}