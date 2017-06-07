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

package org.opencms.gwt.client.ui.input;

import org.opencms.gwt.client.ui.CmsToggleButton;
import org.opencms.gwt.client.ui.I_CmsButton.ButtonStyle;
import org.opencms.gwt.client.ui.css.I_CmsInputCss;
import org.opencms.gwt.client.ui.css.I_CmsInputLayoutBundle;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.HasClickHandlers;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;

/**
 * Class representing a single radio button.<p>
 *
 * In most cases, you will need to set the group of a radio button, which is a Java object,
 * not just a string as in HTML radio buttons. Clicking on a radio button in a group will result
 * in the radio button being selected, and none of the other buttons in the group being selected.<p>
 *
 * @since 8.0.0
 *
 */
public class CmsRadioButton extends Composite implements HasHorizontalAlignment, HasClickHandlers {

    /** The CSS bundle instance used for this widget. */
    private static final I_CmsInputCss CSS = I_CmsInputLayoutBundle.INSTANCE.inputCss();

    /** The wigdet used to implement the actual radio button. */
    protected CmsToggleButton m_button = new CmsToggleButton();

    /** The radio button group. */
    protected CmsRadioButtonGroup m_group;

    /** The current horizontal alignment. */
    private HorizontalAlignmentConstant m_align;

    /** The value associated with this radio button. */
    private String m_name;

    /**
     * Creates a new radio button without setting the name and label text.<p>
     */
    public CmsRadioButton() {

        m_button.setUseMinWidth(false);
        m_button.setButtonStyle(ButtonStyle.TRANSPARENT, null);
        m_button.setImageClass(CSS.radioButtonImage());
        setHorizontalAlignment(ALIGN_RIGHT);

        initWidget(m_button);
        addStyleName(CSS.radioButton());
        addStyleName(CSS.inlineBlock());
        m_button.addClickHandler(new ClickHandler() {

            /**
             * @see com.google.gwt.event.dom.client.ClickHandler#onClick(com.google.gwt.event.dom.client.ClickEvent)
             */
            public void onClick(ClickEvent event) {

                m_button.setDown(true);
                if (m_group != null) {
                    m_group.selectButton(CmsRadioButton.this);
                }
            }
        });

    }

    /**
     * Creates a new radio button.<p>
     *
     * @param name the value associated with this radio button
     * @param labelText the label text of the radio button
     */
    public CmsRadioButton(String name, String labelText) {

        this();
        m_name = name;
        if (labelText != null) {
            m_button.setText(labelText);
        }
    }

    /**
     * Adds a click handler to the radio button.<p>
     *
     * @see com.google.gwt.event.dom.client.HasClickHandlers#addClickHandler(com.google.gwt.event.dom.client.ClickHandler)
     */
    public HandlerRegistration addClickHandler(ClickHandler handler) {

        return addDomHandler(handler, ClickEvent.getType());
    }

    /**
     * This is the alignment of the text in reference to the checkbox, possible values are left or right.<p>
     *
     * @see com.google.gwt.user.client.ui.HasHorizontalAlignment#getHorizontalAlignment()
     */
    public HorizontalAlignmentConstant getHorizontalAlignment() {

        return m_align;
    }

    /**
     * Returns the value associated with this radio button.<p>
     *
     * @return the value associated with this radio button
     */
    public String getName() {

        return m_name;
    }

    /**
     * Returns the radio button.<p>
     *
     * @return the radio button
     */
    public CmsToggleButton getRadioButton() {

        return m_button;
    }

    /**
     * Returns true if the radio button is checked.<p>
     *
     * @return true if the  radio button is checked
     */
    public boolean isChecked() {

        return m_button.isDown();
    }

    /**
     * Returns <code>true</code> if this widget is enabled.<p>
     *
     * @return <code>true</code> if this widget is enabled
     */
    public boolean isEnabled() {

        return m_button.isEnabled();
    }

    /**
     * Sets the 'checked' status of the radio button.<p>
     *
     * @param checked if true, check the radio button, else uncheck it
     */
    public void setChecked(boolean checked) {

        m_button.setDown(checked);
        if (checked) {
            m_group.selectButton(this);
        } else {
            m_group.deselectButton();
        }
    }

    /**
     * Enables or disables the radio button.<p>
     *
     * @param enabled if true, the radio button is enabled, else disabled
     */
    public void setEnabled(boolean enabled) {

        m_button.setEnabled(enabled);
    }

    /**
     * Sets the group for this radio button.<p>
     *
     * @param group
     */
    public void setGroup(CmsRadioButtonGroup group) {

        m_group = group;
    }

    /**
     * This is the alignment of the text in reference to the checkbox, possible values are left or right.<p>
     *
     * @see com.google.gwt.user.client.ui.HasHorizontalAlignment#setHorizontalAlignment(com.google.gwt.user.client.ui.HasHorizontalAlignment.HorizontalAlignmentConstant)
     */
    public void setHorizontalAlignment(HorizontalAlignmentConstant align) {

        if (align.equals(HasHorizontalAlignment.ALIGN_CENTER)) {
            // ignore center alignment
            return;
        }
        m_button.setHorizontalAlignment(align);
        m_align = align;
    }

    /**
     * Sets the name of this radio button.<p>
     *
     * @param name the new name
     */
    public void setName(String name) {

        m_name = name;
    }

    /**
     * Sets the text which is displayed next to the radio button.<p>
     *
     * @param text the new text
     */
    public void setText(String text) {

        m_button.setText(text);
    }

}
