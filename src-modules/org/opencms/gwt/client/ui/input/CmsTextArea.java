/*
 * File   : $Source: /alkacon/cvs/opencms/src-modules/org/opencms/gwt/client/ui/input/Attic/CmsTextArea.java,v $
 * Date   : $Date: 2010/04/13 09:17:19 $
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

import org.opencms.gwt.client.ui.css.I_CmsInputCss;
import org.opencms.gwt.client.ui.css.I_CmsInputLayoutBundle;

import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.TextArea;

/**
 * Basic text area widget for forms.<p>
 * 
 * @author Georg Westenberger
 * 
 * @version $Revision: 1.5 $ 
 * 
 * @since 8.0.0
 * 
 */
public class CmsTextArea extends Composite implements I_CmsFormWidget {

    /** The CSS bundle for this widget. */
    private static I_CmsInputCss CSS = I_CmsInputLayoutBundle.INSTANCE.inputCss();

    /** Default padding for text areas. */
    private static final int DEFAULT_PADDING = 4;

    /** The error display for this widget. */
    private CmsErrorWidget m_error = new CmsErrorWidget();

    /** The root panel containing the other components of this widget. */
    private Panel m_panel = new FlowPanel();

    /** The internal text area widet used by this widget. */
    private TextArea m_textArea = new TextArea();

    private CmsPaddedPanel m_textAreaContainer = new CmsPaddedPanel(DEFAULT_PADDING);

    /**
     * Text area widgets for ADE forms.<p>
     */
    public CmsTextArea() {

        super();
        initWidget(m_panel);
        m_panel.add(m_textAreaContainer);
        m_textAreaContainer.add(m_textArea);
        m_panel.add(m_error);
        m_textArea.addStyleName(CSS.textArea());
        m_textAreaContainer.addStyleName(CSS.textAreaContainer());
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
    public Object getFormValue() {

        return m_textArea.getText();
    }

    /**
     * Returns the text contained in the text area.<p>
     * 
     * @return the text in the text area
     */
    public String getText() {

        return m_textArea.getText();
    }

    /**
     * @see org.opencms.gwt.client.ui.input.I_CmsFormWidget#reset()
     */
    public void reset() {

        m_textArea.setText("");
    }

    /**
     * @see org.opencms.gwt.client.ui.input.I_CmsFormWidget#setEnabled(boolean)
     */
    public void setEnabled(boolean enabled) {

        m_textArea.setEnabled(enabled);
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

        if (value instanceof String) {
            String strValue = (String)value;
            m_textArea.setText(strValue);
        }
    }

    /**
     * Sets the text in the text area.<p>
     * 
     * @param text the new text
     */
    public void setText(String text) {

        m_textArea.setText(text);
    }
}
