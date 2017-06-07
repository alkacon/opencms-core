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

package org.opencms.ui.client;

import org.opencms.gwt.client.util.CmsDomUtil;
import org.opencms.ui.components.CmsCopyToClipboardButton;
import org.opencms.ui.shared.components.CmsCopyToClipboardState;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.vaadin.client.ui.ConnectorFocusAndBlurHandler;
import com.vaadin.client.ui.VButton;
import com.vaadin.client.ui.button.ButtonConnector;
import com.vaadin.shared.ui.Connect;

/**
 * The copy to clip-board/select text button client connector.<p>
 */
@Connect(CmsCopyToClipboardButton.class)
public class CmsCopyToClipboardButtonConnector extends ButtonConnector {

    /** The serial version id. */
    private static final long serialVersionUID = -5124036048815760156L;

    /**
     * @see com.vaadin.client.ui.AbstractConnector#getState()
     */
    @Override
    public CmsCopyToClipboardState getState() {

        return (CmsCopyToClipboardState)super.getState();
    }

    /**
     * @see com.vaadin.client.ui.button.ButtonConnector#init()
     */
    @Override
    public void init() {

        VButton button = getWidget();
        button.client = getConnection();
        ConnectorFocusAndBlurHandler.addHandlers(this);

        if (CmsDomUtil.isCopyToClipboardSupported()) {
            button.addClickHandler(new ClickHandler() {

                public void onClick(ClickEvent event) {

                    event.preventDefault();
                    event.stopPropagation();
                    CmsDomUtil.copyToClipboard(getState().getSelector());
                }
            });
        } else {
            button.setVisible(false);
        }
    }
}
