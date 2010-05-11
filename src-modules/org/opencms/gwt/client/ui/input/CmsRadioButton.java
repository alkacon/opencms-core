/*
 * File   : $Source: /alkacon/cvs/opencms/src-modules/org/opencms/gwt/client/ui/input/Attic/CmsRadioButton.java,v $
 * Date   : $Date: 2010/05/11 15:49:06 $
 * Version: $Revision: 1.5 $
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

import org.opencms.gwt.client.ui.CmsToggleButton;
import org.opencms.gwt.client.ui.css.I_CmsInputCss;
import org.opencms.gwt.client.ui.css.I_CmsInputLayoutBundle;

import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.HasClickHandlers;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;

/**
 * Class representing a single radio button.<p>
 * 
 * This class is a helper class for the CmsRadioButtonGroup class, and is not very useful by itself.
 * 
 * @author Georg Westenberger
 * 
 * @version $Revision: 1.5 $ 
 * 
 * @since 8.0.0
 * 
 */
public class CmsRadioButton extends Composite implements HasHorizontalAlignment, HasClickHandlers {

    /** The CSS bundle instance used for this widget. */
    private static final I_CmsInputCss CSS = I_CmsInputLayoutBundle.INSTANCE.inputCss();

    /** The current horizontal alignment. */
    private HorizontalAlignmentConstant m_align;

    /** The wigdet used to implement the actual radio button. */
    private CmsToggleButton m_button = new CmsToggleButton();

    /** The value associated with this radio button. */
    private String m_name;

    /**
     * Creates a new radio button.<p>
     * 
     * @param name the value associated with this radio button
     * @param labelText the label text of the radio button
     */
    public CmsRadioButton(String name, String labelText) {

        m_name = name;
        m_button.setUseMinWidth(false);
        m_button.setShowBorder(false);
        m_button.setImageClass(CSS.radioButtonImage());
        if (labelText != null) {
            m_button.setText(labelText);
        }
        setHorizontalAlignment(ALIGN_RIGHT);

        initWidget(m_button);
        addStyleName(CSS.radioButton());
        addStyleName(CSS.inlineBlock());

    }

    /**
     * Adds a click handler to the radio button.<p>
     * 
     * @see com.google.gwt.event.dom.client.HasClickHandlers#addClickHandler(com.google.gwt.event.dom.client.ClickHandler)
     */
    public HandlerRegistration addClickHandler(ClickHandler handler) {

        return m_button.addClickHandler(handler);
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
     * Returns true if the radio button is checked.<p>
     * 
     * @return true if the  radio button is checked 
     */
    public boolean isChecked() {

        return m_button.isDown();
    }

    /**
     * Sets the 'checked' status of the radio button.<p>
     * 
     * @param checked if true, check the radio button, else uncheck it 
     */
    public void setChecked(boolean checked) {

        m_button.setDown(checked);
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

}
