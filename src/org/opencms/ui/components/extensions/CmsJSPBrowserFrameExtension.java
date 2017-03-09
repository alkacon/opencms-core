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

package org.opencms.ui.components.extensions;

import org.opencms.ui.I_CmsDialogContext;
import org.opencms.ui.apps.CmsAppWorkplaceUi;
import org.opencms.ui.shared.rpc.I_CmsJSPBrowserFrameRpc;
import org.opencms.util.CmsUUID;

import java.util.HashSet;
import java.util.Set;

import com.vaadin.server.AbstractExtension;
import com.vaadin.server.ExternalResource;
import com.vaadin.ui.BrowserFrame;
import com.vaadin.ui.Window;

/**
 * Vaadin extension class for a BrowserFrame to display a JSP.<p>
 *  From the client side, the extension allows to return an Array of UUIDs of changed resources over an Rpc-Interface.<p>
 */
public class CmsJSPBrowserFrameExtension extends AbstractExtension implements I_CmsJSPBrowserFrameRpc {

    /**vaadin serial id.*/
    private static final long serialVersionUID = -417339050505442749L;

    /**Dialog context.*/
    private I_CmsDialogContext m_context;

    /**Vaadin window to where the iframe with the jsp gets shown.*/
    private Window m_window;

    /**
     * Private constructor.<p>
     * Only called from static method in this class.<p>
     *
     * @param link of jsp
     * @param context Dialog Context
     */
    private CmsJSPBrowserFrameExtension(String link, I_CmsDialogContext context) {

        m_context = context;
        ExternalResource res = new ExternalResource(link);

        BrowserFrame browser = new BrowserFrame("Browser", res);
        browser.setSizeFull();
        m_window = new Window();
        m_window.setSizeFull();
        m_window.addStyleName("o-jspwindow");
        m_window.setContent(browser);
        m_window.setClosable(false);
        m_window.setDraggable(false);
        CmsAppWorkplaceUi.get().addWindow(m_window);

        super.extend(browser);
        registerRpc(this);
    }

    /**
     * Creates extended BrowserFrame displaying a JSP under given link.<p>
     *
     * @param jspLink of JSP file, absolute vfs-path
     * @param context of the dialog to finish operation in the end
     */
    @SuppressWarnings("unused")
    public static void showExtendedBrowserFrame(String jspLink, I_CmsDialogContext context) {

        new CmsJSPBrowserFrameExtension(jspLink, context);
    }

    /**
     * @see org.opencms.ui.shared.rpc.I_CmsJSPBrowserFrameRpc#cancelParentWindow(java.lang.String[])
     */
    public void cancelParentWindow(String[] uuids) {

        m_window.close();
        m_context.finish(getSetFromUUIDStrings(uuids));
    }

    /**
     * Creates Set of type CmsUUID from String array.<p>
     *
     * @param uuids in array as string
     * @return Set<CmsUUID> to use for finish method of I_CmsDialogContext
     */
    private Set<CmsUUID> getSetFromUUIDStrings(String[] uuids) {

        Set<CmsUUID> res = new HashSet<CmsUUID>();
        for (String uuid : uuids) {
            res.add(new CmsUUID(uuid));
        }
        return res;
    }
}
