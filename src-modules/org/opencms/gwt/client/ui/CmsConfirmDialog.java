/*
 * File   : $Source: /alkacon/cvs/opencms/src-modules/org/opencms/gwt/client/ui/Attic/CmsConfirmDialog.java,v $
 * Date   : $Date: 2010/03/03 15:32:37 $
 * Version: $Revision: 1.1 $
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

import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Label;

/**
 * Provides a pop up dialog.<p>
 * 
 * @author Tobias Herrmann
 * 
 * @version $Revision: 1.1 $
 * 
 * @since 8.0.0
 */
public class CmsConfirmDialog extends CmsPopupDialog {

    /** The 'Ok' button. */
    CmsTextButton m_okButton;

    /** The 'Cancel' button. */
    CmsTextButton m_cancelButton;

    /** 
     * Constructor.<p>
     */
    public CmsConfirmDialog() {

        super();
        m_okButton = new CmsTextButton("Ok");
        addButton(m_okButton);
        m_cancelButton = new CmsTextButton("Cancel");
        addButton(m_cancelButton);
    }

    /**
     * The constructor.<p>
     * 
     * @param title the title and heading of the dialog
     * @param content the content text
     */
    public CmsConfirmDialog(String title, String content) {

        this();
        setTitle(title);
        setContent(new Label(content));
    }

    /**
     * Adds an 'ok' click handler.
     * 
     * @param handler the click handler
     */
    public void addOkClickHandler(ClickHandler handler) {

        m_okButton.addClickHandler(handler);
    }

    /**
     * Adds a 'cancel' click handler.
     * 
     * @param handler the click handler
     */
    public void addCancelClickHandler(ClickHandler handler) {

        m_cancelButton.addClickHandler(handler);
    }
}
