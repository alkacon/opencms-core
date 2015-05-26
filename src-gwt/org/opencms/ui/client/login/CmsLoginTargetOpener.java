/*
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (c) Alkacon Software GmbH (http://www.alkacon.com)
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

package org.opencms.ui.client.login;

import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.InputElement;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.FormPanel;

/**
 * Client side class responsible for opening the login target page.<p>
 */
public class CmsLoginTargetOpener extends FlowPanel {

    /**
     * Opens the login target for the given user name and password.<p>
     * 
     * @param target the login target 
     * @param user the user 
     * @param password the password 
     */
    public void openTarget(final String target, final String user, final String password) {

        // Post a hidden form with user name and password fields, 
        // to hopefully trigger the browser's password manager 
        final FormPanel form = new FormPanel("_self");
        Document doc = Document.get();
        InputElement userField = doc.createTextInputElement();
        userField.setName("ocUname");
        InputElement passwordField = doc.createPasswordInputElement();
        passwordField.setName("ocPword");
        userField.setValue(user);
        passwordField.setValue(password);
        form.getElement().appendChild(userField);
        form.getElement().appendChild(passwordField);
        form.setMethod("post");
        form.setAction(target);
        form.setVisible(false);
        add(form);
        form.submit();
    }
}
