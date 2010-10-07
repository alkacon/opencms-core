/*
 * File   : $Source: /alkacon/cvs/opencms/src-modules/org/opencms/gwt/client/ui/input/form/Attic/CmsFormRow.java,v $
 * Date   : $Date: 2010/10/07 07:56:34 $
 * Version: $Revision: 1.4 $
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

package org.opencms.gwt.client.ui.input.form;

import org.opencms.gwt.client.ui.CmsToggleButton;
import org.opencms.gwt.client.ui.css.I_CmsInputCss;
import org.opencms.gwt.client.ui.css.I_CmsInputLayoutBundle;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Style.Visibility;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.Widget;

/**
 * A row in a properties form.<p>
 * 
 * This widget contains both a label and a panel into which an input widget for the form field can be placed. 
 * These widgets are next to each other horizontally.
 * 
 * @author Georg Westenberger 
 * 
 * @version $Revision: 1.4 $
 * 
 * @since 8.0.0
 */
public class CmsFormRow extends Composite {

    /** The ui binder interface for this widget. */
    protected interface I_CmsFormRowUiBinder extends UiBinder<Widget, CmsFormRow> {
        // uibinder
    }

    /** The width of the label. */
    public static final int LABEL_WIDTH = 160;

    /** The width of the opener. */
    public static final int OPENER_WIDTH = 16;

    /** The CSS bundle used for this widget. */
    protected static I_CmsInputCss CSS = I_CmsInputLayoutBundle.INSTANCE.inputCss();

    /** The ui binder instance for this form row. */
    private static I_CmsFormRowUiBinder uiBinder = GWT.create(I_CmsFormRowUiBinder.class);
    /** The label for the form row. */
    @UiField
    protected Label m_label;

    /** The opener of the form row. */
    @UiField
    protected CmsToggleButton m_opener;

    /** The widget container for the form row. */
    @UiField
    protected Panel m_widgetContainer;

    /**
     * The default constructor. 
     */
    public CmsFormRow() {

        initWidget(uiBinder.createAndBindUi(this));
        m_opener.setDown(false);

        m_opener.setUpFace("", CSS.formRowOpenerClosed());
        m_opener.setDownFace("", CSS.formRowOpenerOpen());
    }

    /** 
     * Returns the width of the label as a string.<p>
     * 
     * @return the width of the label as a string 
     */
    public static String getLabelWidth() {

        return LABEL_WIDTH + "px";
    }

    /**
     * Returns the width of the opener as a string.<p>
     * 
     * @return the width of the opener as a string 
     */
    public static String getOpenerWidth() {

        return OPENER_WIDTH + "px";
    }

    /**
     * Returns the left margin of the widget container as a string.<p>
     * 
     * @return the left margin of the widget container as a string 
     */
    public static String getWidgetContainerLeftMargin() {

        return OPENER_WIDTH + LABEL_WIDTH + "px";
    }

    /**
     * Returns the label for the form row.<p>
     * 
     * @return the label for the form row 
     */
    public Label getLabel() {

        return m_label;
    }

    /**
     * Returns the opener of the form row.<p>
     * 
     * @return the opener of the form row 
     */
    public CmsToggleButton getOpener() {

        return m_opener;
    }

    /**
     * Returns the widget container for the form row.<p>
     * 
     * @return the widget container for the form row
     */
    public Panel getWidgetContainer() {

        return m_widgetContainer;
    }

    /**
     * Opens or closes the opener.<p>
     * 
     * @param open if true opens the opener, else closes it.<p>
     */
    public void setOpenerOpen(boolean open) {

        m_opener.setDown(open);
    }

    /**
     * Shows or hides the opener.<p>
     * 
     * @param openerVisible if true, shows the opener, else hides it.
     */
    public void setOpenerVisible(boolean openerVisible) {

        m_opener.getElement().getStyle().setVisibility(openerVisible ? Visibility.VISIBLE : Visibility.HIDDEN);
    }

}
