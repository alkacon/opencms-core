/*
 * File   : $Source: /alkacon/cvs/opencms/src-modules/org/opencms/gwt/client/ui/input/Attic/CmsTextBox.java,v $
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

import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.TextBox;

/**
 * A basic text box class. <p>
 * 
 * @author Georg Westenberger
 * 
 * @version $Revision: 1.3 $ 
 * 
 * @since 8.0.0
 * 
 */
public class CmsTextBox extends Composite implements I_CmsFormWidget {

    /** The CSS bundle used by this class. */
    private static final I_CmsInputCss CSS = I_CmsLayoutBundle.INSTANCE.inputCss();

    /** The error display for this widget. */
    private CmsErrorWidget m_error = new CmsErrorWidget();

    /** The text box widget used internally by this widget. */
    private TextBox m_textBox = new TextBox();

    /**
     * Default constructor.
     */
    public CmsTextBox() {

        super();
        m_textBox.addStyleName(CSS.textBox());
        FlowPanel panel = new FlowPanel();
        panel.addStyleName(CSS.textBoxWidget());
        panel.add(m_textBox);
        panel.add(m_error);
        initWidget(panel);
    }

    static {
        CSS.ensureInjected();
    }

    /**
     * @see org.opencms.gwt.client.ui.input.I_CmsFormWidget#getFieldType()
     */
    public FieldType getFieldType() {

        return I_CmsFormWidget.FieldType.STRING;
    }

    /**
     * @see org.opencms.gwt.client.ui.input.I_CmsFormWidget#getFormValue()
     */
    public String getFormValue() {

        return m_textBox.getText();
    }

    /**
     * Returns the text box used internally by this class.<p>
     * @return a text box 
     */
    public TextBox getInnerTextBox() {

        return m_textBox;
    }

    /**
     * @see org.opencms.gwt.client.ui.input.I_CmsFormWidget#reset()
     */
    public void reset() {

        m_textBox.setText("");
    }

    /**
     * @see org.opencms.gwt.client.ui.input.I_CmsFormWidget#setEnabled(boolean)
     */
    public void setEnabled(boolean enabled) {

        m_textBox.setEnabled(enabled);
    }

    /**
     * @see org.opencms.gwt.client.ui.input.I_CmsFormWidget#setErrorMessage(java.lang.String)
     */
    public void setErrorMessage(String message) {

        m_error.setText(message);
    }

    /**
     * @see org.opencms.gwt.client.ui.input.I_CmsFormWidget#setFormValue(java.lang.Object)
     */
    public void setFormValue(Object value) {

        if (value instanceof String) {
            m_textBox.setText((String)value);
        }
    }
}
