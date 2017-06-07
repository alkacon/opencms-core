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

import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.RepeatingCommand;
import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.Element;
import com.vaadin.client.ui.VTextField;

/**
 * Abstract superclass for the user name and password fields of the login dialog.<p>
 *
 * Since these fields may be pre-filled by the browser password manager and not by the server side
 * component, they require some special handling.<p>
 */
public class A_CmsLoginField extends VTextField {

    /** True if the intial call to updateFieldContent has already occured. */
    private boolean m_initialUpdateCalled;

    /**
     * Creates a new instance based on an existing dom input element.<p>
     *
     * @param element the input element to use for the widget
     */
    protected A_CmsLoginField(Element element) {

        super(element);
        Document.get().createTextInputElement();
    }

    /**
     * @see com.vaadin.client.ui.VTextField#updateFieldContent(java.lang.String)
     *
     * We have to override this method to prevent its value being overwritten by Vaadin and to make
     * sure that the real value is sent to the server.
     */
    @Override
    public void updateFieldContent(String text) {

        if (!m_initialUpdateCalled) {
            m_initialUpdateCalled = true;
            if ("".equals(text)) {
                valueChange(false);
                Scheduler.get().scheduleFixedDelay(new RepeatingCommand() {

                    public boolean execute() {

                        if (isAttached()) {
                            valueChange(false);
                            return true;
                        } else {
                            return false;
                        }
                    }
                }, 100);
                return;
            }

        }
        super.updateFieldContent(text);
    }

}
