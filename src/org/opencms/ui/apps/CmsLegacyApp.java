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

package org.opencms.ui.apps;

import org.opencms.main.OpenCms;
import org.opencms.ui.A_CmsUI;
import org.opencms.workplace.CmsWorkplace;
import org.opencms.workplace.CmsWorkplaceSettings;
import org.opencms.workplace.tools.I_CmsToolHandler;

import javax.servlet.http.HttpServletRequest;

import com.vaadin.server.ExternalResource;
import com.vaadin.ui.BrowserFrame;
import com.vaadin.ui.JavaScript;

/**
 * App for legacy admin tools. Renders the tool in an iframe.<p>
 */
public class CmsLegacyApp extends BrowserFrame implements I_CmsWorkplaceApp {

    /** Name of Javascript variable used to indicate whether we are currently showing a legacy app. */
    public static final String VAR_IS_LEGACY_APP = "cmsIsLegacyApp";

    /** The serial version id. */
    private static final long serialVersionUID = -2857100593142358027L;

    /** The tool handler. */
    private I_CmsToolHandler m_toolHandler;

    /**
     * Constructor.<p>
     *
     * @param toolHandler the tool handler
     */
    public CmsLegacyApp(I_CmsToolHandler toolHandler) {

        m_toolHandler = toolHandler;
        addAttachListener(new AttachListener() {

            private static final long serialVersionUID = 1L;

            public void attach(AttachEvent event) {

                JavaScript.eval(VAR_IS_LEGACY_APP + " = true;");

            }
        });
        addDetachListener(new DetachListener() {

            private static final long serialVersionUID = 1L;

            public void detach(DetachEvent event) {

                JavaScript.eval(VAR_IS_LEGACY_APP + " = false;");
            }
        });
        setSizeFull();
    }

    /**
     * @see org.opencms.ui.apps.I_CmsWorkplaceApp#initUI(org.opencms.ui.apps.I_CmsAppUIContext)
     */
    public void initUI(I_CmsAppUIContext context) {

        context.setAppContent(this);
        context.showInfoArea(false);
    }

    /**
     * @see org.opencms.ui.apps.I_CmsWorkplaceApp#onStateChange(java.lang.String)
     */
    public void onStateChange(String state) {

        // only act on initial state change
        if (getSource() == null) {
            CmsWorkplace wp = new CmsWorkplace(A_CmsUI.getCmsObject(), CmsAppWorkplaceUi.get().getHttpSession()) {

                @Override
                protected void initWorkplaceRequestValues(CmsWorkplaceSettings settings, HttpServletRequest request) {

                    // nothing to do
                }
            };

            OpenCms.getWorkplaceManager().getToolManager().setCurrentToolPath(wp, m_toolHandler.getPath());

            String url = OpenCms.getLinkManager().substituteLink(A_CmsUI.getCmsObject(), m_toolHandler.getLink());
            setSource(new ExternalResource(url));
            setSizeFull();
        }
    }
}
