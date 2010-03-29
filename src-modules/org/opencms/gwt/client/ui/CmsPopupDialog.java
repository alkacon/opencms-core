/*
 * File   : $Source: /alkacon/cvs/opencms/src-modules/org/opencms/gwt/client/ui/Attic/CmsPopupDialog.java,v $
 * Date   : $Date: 2010/03/29 06:39:40 $
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

package org.opencms.gwt.client.ui;

import org.opencms.gwt.client.ui.css.I_CmsLayoutBundle;

import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Widget;

/**
 * Provides a pop up dialog, including convenience methods to add and remove buttons.<p>
 * 
 * @author Tobias Herrmann
 * 
 * @version $Revision: 1.4 $
 * 
 * @since 8.0.0
 * 
 */
public class CmsPopupDialog extends CmsPopup {

    /** The panel holding the dialog's buttons. */
    private FlowPanel m_buttonPanel;

    /** The content widget. */
    private Widget m_content;

    /**
     * The Constructor.<p>
     */
    public CmsPopupDialog() {

        super();
    }

    /**
     * The constructor.<p>
     * 
     * @param title the title and heading of the dialog
     * @param content the content widget
     */
    public CmsPopupDialog(String title, Widget content) {

        super(title);
        this.setContent(content);
    }

    /**
     * Adds a button widget to the button panel.<p>
     * 
     * @param button the button widget
     */
    public void addButton(Widget button) {

        if (m_buttonPanel == null) {
            m_buttonPanel = new FlowPanel();
            getDialog().add(m_buttonPanel);
            m_buttonPanel.setStyleName(I_CmsLayoutBundle.INSTANCE.dialogCss().popupButtonPanel());
            getDialog().getWidget().setStyleName(I_CmsLayoutBundle.INSTANCE.dialogCss().popupMainContent());

        }
        //        button.addStyleName(I_CmsLayoutBundle.INSTANCE.buttonCss().cmsMinWidth());
        m_buttonPanel.add(button);
    }

    /**
     * Returns the content widget.<p>
     * 
     * @return the content widget
     */
    public Widget getContent() {

        return m_content;
    }

    /**
     * Removes all buttons.<p>
     */
    public void removeAllButtons() {

        if (m_buttonPanel == null) {
            return;
        }
        m_buttonPanel.clear();
    }

    /**
     * Removes the given button widget from the button panel.<p>
     * 
     * @param button the button widget to remove
     */
    public void removeButton(Widget button) {

        if (m_buttonPanel == null) {
            return;
        }
        m_buttonPanel.remove(button);
        if (m_buttonPanel.getWidgetCount() == 0) {
            getDialog().remove(m_buttonPanel);
            m_buttonPanel = null;
        }
    }

    /**
     * Sets the content for this dialog replacing any former content.<p>
     * 
     * @param widget the content widget
     */
    public void setContent(Widget widget) {

        if (m_content != null) {
            getDialog().remove(m_content);
        }
        getDialog().insert(widget, 0);
        m_content = widget;
    }

}
