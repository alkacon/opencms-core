/*
 * File   : $Source: /alkacon/cvs/opencms/src-modules/org/opencms/gwt/client/ui/input/Attic/CmsCheckBox.java,v $
 * Date   : $Date: 2010/03/09 14:19:24 $
 * Version: $Revision: 1.3 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (C) 2002 - 2009 Alkacon Software (http://www.alkacon.com)
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

import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.HasClickHandlers;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ToggleButton;

/**
 * This class represents a labelled checkbox which is not represented as an INPUT element in 
 * the DOM, but is displayed as an image. 
 * 
 * It can be checked/unchecked and enabled/disabled, which means 4 combinations in total.
 * So you need to supply 4 images, one for each of the combinations.
 * 
 * @author Georg Westenberger
 * 
 * @version $Revision: 1.3 $ 
 * 
 * @since 8.0.0
 */
public class CmsCheckBox extends Composite implements HasClickHandlers, I_CmsFormWidget {

    /** CSS bundle for this widget. */
    private static final I_CmsInputCss CSS = I_CmsLayoutBundle.INSTANCE.inputCss();

    /** Image bundle for this widget. */
    private static final I_CmsImageBundle IMAGES = I_CmsImageBundle.INSTANCE;

    /** Toggle button which actually displays the checkbox. */
    private final ToggleButton m_button = new ToggleButton();

    /** The error display for this widget. */
    private final CmsErrorWidget m_error = new CmsErrorWidget();

    /** Internal root widget to which all other components of this widget are attached. */
    private final FlowPanel m_root = new FlowPanel();

    /** A row containing the checkbox and a label (only used if the checkbox has a label). */
    private final HorizontalPanel m_row = new HorizontalPanel();

    /**
     * Default constructor which creates a checkbox without a label.<p>
     */
    public CmsCheckBox() {

        this(null);
    }

    /**
     * Public constructor for a checkbox.<p>
     * 
     * The label text passed will be displayed to the right of the checkbox. If
     * it is null, no label is displayed.
     * 
     * @param labelText the label text
     */
    public CmsCheckBox(String labelText) {

        m_button.getDownFace().setImage(new Image(IMAGES.checkboxChecked()));
        m_button.getUpFace().setImage(new Image(IMAGES.checkboxUnchecked()));
        m_button.getDownDisabledFace().setImage(new Image(IMAGES.checkboxCheckedDisabled()));
        m_button.getUpDisabledFace().setImage(new Image(IMAGES.checkboxUncheckedDisabled()));

        initWidget(m_root);
        if (labelText != null) {
            m_row.add(m_button);
            Label label = new Label(labelText);
            label.setText(labelText);
            label.addStyleName(CSS.checkBoxLabel());
            m_row.add(label);
            m_root.add(m_row);
        } else {
            m_root.add(m_button);
        }
        m_root.add(m_error);

        addStyleName(CSS.checkBox());

    }

    static {
        CSS.ensureInjected();
    }

    /**
     * Adds a click handler to the checkbox.<p>
     * 
     * @see com.google.gwt.event.dom.client.HasClickHandlers#addClickHandler(com.google.gwt.event.dom.client.ClickHandler)
     */
    public HandlerRegistration addClickHandler(ClickHandler handler) {

        return m_button.addClickHandler(handler);
    }

    /**
     * @see org.opencms.gwt.client.ui.input.I_CmsFormWidget#getFieldType()
     */
    public FieldType getFieldType() {

        return I_CmsFormWidget.FieldType.BOOLEAN;
    }

    /**
     * @see org.opencms.gwt.client.ui.input.I_CmsFormWidget#getFormValue()
     */
    public Boolean getFormValue() {

        return this.isChecked() ? Boolean.TRUE : Boolean.FALSE;
    }

    /**
     * Returns true if the checkbox is checked.<p>
     * 
     * @return true if the checkbox is checked
     */
    public boolean isChecked() {

        return m_button.isDown();
    }

    /**
     * Returns true if the checkbox is enabled.<p>
     * 
     * @return true if the checkbox is enabled
     */
    public boolean isEnabled() {

        return m_button.isEnabled();
    }

    /**
     * @see org.opencms.gwt.client.ui.input.I_CmsFormWidget#reset()
     */
    public void reset() {

        this.setChecked(false);
    }

    /**
     * Checks or unchecks the checkbox.<p>
     * 
     * @param checked if true, check the checkbox else uncheck it
     */
    public void setChecked(boolean checked) {

        m_button.setDown(checked);
    }

    /**
     * Enables or disables the checkbox.<p>
     * 
     * @param enabled if true, enable the checkbox, else disable it
     */
    public void setEnabled(boolean enabled) {

        m_button.setEnabled(enabled);
    }

    /**
     * @see org.opencms.gwt.client.ui.input.I_CmsFormWidget#setErrorMessage(java.lang.String)
     */
    public void setErrorMessage(String errorMessage) {

        m_error.setText(errorMessage);
    }

    /**
     * @see org.opencms.gwt.client.ui.input.I_CmsFormWidget#setFormValue(java.lang.Object)
     */
    public void setFormValue(Object value) {

        if (value instanceof Boolean) {
            Boolean boolValue = (Boolean)value;
            this.setChecked(boolValue.booleanValue());
        }
    }

}
