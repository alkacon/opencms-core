/*
 * File   : $Source: /alkacon/cvs/opencms/src-modules/org/opencms/ade/galleries/client/preview/ui/Attic/CmsPropertyForm.java,v $
 * Date   : $Date: 2010/05/27 09:42:23 $
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

package org.opencms.ade.galleries.client.preview.ui;

import org.opencms.ade.galleries.client.ui.css.I_CmsLayoutBundle;
import org.opencms.gwt.client.ui.input.CmsLabel;
import org.opencms.gwt.client.ui.input.CmsTextBox;

import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;

/**
 * The widget to display a simple form with a label and an text box.<p>
 *  
 * @author Polina Smagina
 * 
 * @version $Revision: 1.3 $
 * 
 * @since 8.0.
 */
public class CmsPropertyForm extends Composite implements Runnable {

    /** The id of the property. */
    private String m_id;

    /** The text box panel. */
    private FlowPanel m_inputPanel;

    /** The flag to indicate if the text box value is changed. */
    private boolean m_isChanged;

    /** The label. */
    private CmsLabel m_label;

    /** The papanel. */
    private FlowPanel m_parent;

    /** The width of the parent panel. */
    private int m_parentWidth;

    /** The text box. */
    private CmsTextBox m_textBox;

    /**
     * The constructor.<p>
     * 
     * @param id the id of the property from
     * @param width the property from width
     * @param label the property label
     * @param value the property value
     * @param textMetricsKey the key identifying the text metrics to use 
     */
    public CmsPropertyForm(String id, int width, String label, String value, String textMetricsKey) {

        m_id = id;
        m_isChanged = false;
        m_parentWidth = width;
        m_parent = new FlowPanel();
        m_parent.getElement().getStyle().setWidth(m_parentWidth, Unit.PX);
        // set form label
        m_label = new CmsLabel(label);
        m_label.addStyleName(I_CmsLayoutBundle.INSTANCE.previewDialogCss().labelField());
        m_label.getElement().getStyle().setWidth(getLabelWidth(), Unit.PX);
        m_label.truncate(textMetricsKey, getLabelWidth());
        m_parent.add(m_label);

        // set form text box
        m_inputPanel = new FlowPanel();
        m_inputPanel.getElement().getStyle().setWidth(getInputWidth(), Unit.PX);
        m_inputPanel.addStyleName(I_CmsLayoutBundle.INSTANCE.previewDialogCss().inputField());
        m_textBox = new CmsTextBox();
        m_textBox.setText(value);
        m_textBox.setChangeHandler(this);
        m_inputPanel.add(m_textBox);
        m_parent.add(m_inputPanel);

        initWidget(m_parent);
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
     * Returns the isChanged.<p>
     *
     * @return the isChanged
     */
    public boolean isChanged() {

        return m_isChanged;
    }

    /**
     * Will be triggered, if the value of the text box is changed.<p>
     * 
     * @see java.lang.Runnable#run()
     */
    public void run() {

        m_isChanged = true;
        m_textBox.setChangedStyle();
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