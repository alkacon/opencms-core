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

package org.opencms.ade.galleries.client.preview.ui;

import org.opencms.ade.galleries.client.ui.css.I_CmsLayoutBundle;
import org.opencms.gwt.client.ui.input.CmsLabel;
import org.opencms.gwt.client.ui.input.CmsTextBox;
import org.opencms.util.CmsStringUtil;

import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.ScheduledCommand;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.KeyPressEvent;
import com.google.gwt.event.dom.client.KeyPressHandler;
import com.google.gwt.event.logical.shared.HasValueChangeHandlers;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;

/**
 * The widget to display a simple form with a label and an text box.<p>
 *
 * @since 8.0.
 */
public class CmsPropertyForm extends Composite implements HasValueChangeHandlers<String> {

    /** The flag to indicate if the text box value is changed. */
    protected boolean m_isChanged;

    /** The original value. */
    protected String m_originalValue;

    /** The text box. */
    protected CmsTextBox m_textBox;

    /** The id of the property. */
    private String m_id;

    /** The text box panel. */
    private FlowPanel m_inputPanel;

    /** The label. */
    private CmsLabel m_label;

    /** The parent panel. */
    private FlowPanel m_parent;

    /** The width of the parent panel. */
    private int m_parentWidth;

    /**
     * The constructor.<p>
     *
     * @param id the id of the property from
     * @param width the property from width
     * @param value the property value
     * @param noEditReason the reason why the properties are not editable
     * @param textMetricsKey the key identifying the text metrics to use
     */
    public CmsPropertyForm(String id, int width, String value, String noEditReason, String textMetricsKey) {

        m_id = id;
        m_originalValue = value;
        m_isChanged = false;
        m_parentWidth = width;
        m_parent = new FlowPanel();
        m_parent.getElement().getStyle().setWidth(m_parentWidth, Unit.PX);
        // set form label
        m_label = new CmsLabel(m_id);
        m_label.addStyleName(I_CmsLayoutBundle.INSTANCE.previewDialogCss().labelField());
        m_label.getElement().getStyle().setWidth(getLabelWidth(), Unit.PX);
        m_label.truncate(textMetricsKey, getLabelWidth());
        m_parent.add(m_label);

        // set form text box
        m_inputPanel = new FlowPanel();
        m_inputPanel.getElement().getStyle().setWidth(getInputWidth(), Unit.PX);
        m_inputPanel.addStyleName(I_CmsLayoutBundle.INSTANCE.previewDialogCss().inputField());
        m_textBox = new CmsTextBox();
        if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(noEditReason)) {
            m_textBox.setTitle(noEditReason);
            m_textBox.setReadOnly(true);
        }
        m_textBox.setFormValueAsString(m_originalValue);
        m_textBox.addValueChangeHandler(new ValueChangeHandler<String>() {

            /**
             * @see com.google.gwt.event.logical.shared.ValueChangeHandler#onValueChange(ValueChangeEvent event)
             */
            public void onValueChange(ValueChangeEvent<String> event) {

                m_isChanged = true;
                m_textBox.setChangedStyle();
            }
        });
        m_textBox.addKeyPressHandler(new KeyPressHandler() {

            public void onKeyPress(KeyPressEvent event) {

                // make sure the value change event is fired on the first change inside the text box
                if (!isChanged()) {
                    Scheduler.get().scheduleDeferred(new ScheduledCommand() {

                        public void execute() {

                            if (!isChanged()) {

                                if (((getValue() == null) && (m_originalValue != null))
                                    || (!getValue().equals(m_originalValue))) {
                                    ValueChangeEvent.fire(m_textBox, getValue());
                                }
                            }
                        }
                    });
                }
            }
        });
        m_inputPanel.add(m_textBox);
        m_parent.add(m_inputPanel);
        initWidget(m_parent);
    }

    /**
     * @see com.google.gwt.event.logical.shared.HasValueChangeHandlers#addValueChangeHandler(com.google.gwt.event.logical.shared.ValueChangeHandler)
     */
    public HandlerRegistration addValueChangeHandler(ValueChangeHandler<String> handler) {

        return m_textBox.addValueChangeHandler(handler);
    }

    /**
     * Returns the id of the property.<p>
     *
     * @return the id
     */
    public String getId() {

        return m_id;
    }

    /**
     * Returns the field value.<p>
     *
     * @return the field value
     */
    public String getValue() {

        return m_textBox.getFormValueAsString();
    }

    /**
     * Returns the isChanged.<p>
     *
     * @return the isChanged
     */
    public boolean isChanged() {

        return m_isChanged;
    }

    /**
     * Sets the style of the parent panel.<p>
     *
     * @param style the css class
     */
    public void setFormStyle(String style) {

        m_parent.addStyleName(style);
    }

    /**
     * The width of the text box.<p>
     *
     * @return the width
     */
    private int getInputWidth() {

        return (m_parentWidth / 3) * 2;
    }

    /**
     * The width of the label.<p>
     *
     * @return the label width
     */
    private int getLabelWidth() {

        // 2px: margin-left
        return (m_parentWidth / 3) - 2;
    }
}