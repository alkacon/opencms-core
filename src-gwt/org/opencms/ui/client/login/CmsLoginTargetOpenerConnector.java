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

package org.opencms.ui.client.login;

import org.opencms.gwt.client.util.CmsDebugLog;
import org.opencms.gwt.shared.CmsGwtConstants;
import org.opencms.ui.shared.login.I_CmsLoginTargetRpc;

import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.FormElement;
import com.google.gwt.dom.client.InputElement;
import com.google.gwt.user.client.Window;
import com.vaadin.client.ServerConnector;
import com.vaadin.client.extensions.AbstractExtensionConnector;
import com.vaadin.shared.ui.Connect;

/**
 * Connector for the login target opener widget.<p>
 */
@Connect(org.opencms.ui.login.CmsLoginTargetOpener.class)
public class CmsLoginTargetOpenerConnector extends AbstractExtensionConnector {

    /** Default version id. */
    private static final long serialVersionUID = 1L;

    /**
     * Creates a new instance.<p>
     */
    public CmsLoginTargetOpenerConnector() {

    }

    /**
     * @see com.vaadin.client.extensions.AbstractExtensionConnector#extend(com.vaadin.client.ServerConnector)
     */
    @Override
    protected void extend(ServerConnector extendedComponent) {

        registerRpc(I_CmsLoginTargetRpc.class, new I_CmsLoginTargetRpc() {

            private static final long serialVersionUID = 1L;

            public void openTarget(String target, boolean isPublicPC) {

                if (isPublicPC) {
                    // in this case we do not want to trigger the browsers password manager, just call the login target
                    Window.Location.assign(target);
                } else {
                    // Post a hidden form with user name and password fields,
                    // to hopefully trigger the browser's password manager
                    Document doc = Document.get();
                    FormElement formEl = (FormElement)doc.getElementById("opencms-login-form");
                    CmsDebugLog.consoleLog("form target = " + formEl.getTarget());

                    // make sure user name and password are children of the form
                    Element user = doc.getElementById("hidden-username");
                    Element password = doc.getElementById("hidden-password");

                    if ((user != null) && !formEl.isOrHasChild(user)) {
                        formEl.appendChild(user);
                    }
                    if ((password != null) && !formEl.isOrHasChild(password)) {
                        formEl.appendChild(password);
                    }

                    InputElement requestedResourceField = doc.createTextInputElement();
                    requestedResourceField.setName(CmsGwtConstants.PARAM_LOGIN_REDIRECT);
                    requestedResourceField.setValue(target);

                    formEl.appendChild(requestedResourceField);
                    formEl.submit();
                }
            }
        });
    }

}
