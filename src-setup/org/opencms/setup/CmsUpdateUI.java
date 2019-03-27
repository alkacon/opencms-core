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

package org.opencms.setup;

import org.opencms.main.OpenCmsServlet;
import org.opencms.setup.ui.CmsSetupErrorDialog;
import org.opencms.setup.updater.dialogs.A_CmsUpdateDialog;
import org.opencms.setup.updater.dialogs.CmsUpdateStep01LicenseDialog;
import org.opencms.ui.A_CmsUI;
import org.opencms.ui.components.CmsBasicDialog;
import org.opencms.ui.components.CmsBasicDialog.DialogWidth;

import com.vaadin.annotations.Theme;
import com.vaadin.server.VaadinRequest;
import com.vaadin.ui.Window;

@Theme("opencms")
public class CmsUpdateUI extends A_CmsUI {

    private Window m_window;

    private CmsUpdateBean m_updateBean;

    public void displayDialog(A_CmsUpdateDialog dialog) {

        if (dialog.init(this)) {
            m_window.setContent(dialog);
            m_window.setCaption(dialog.getCaption());
            m_window.center();
        }

    }

    public CmsUpdateBean getUpdateBean() {

        return m_updateBean;
    }

    @Override
    protected void init(VaadinRequest request) {

        this.addStyleName("opencms");
        m_updateBean = new CmsUpdateBean();
        if (!m_updateBean.checkOceeVersion(org.opencms.main.OpenCms.getSystemInfo().getVersionNumber())) {

        } else {
            m_updateBean.init(
                CmsUpdateServlet.instance.getServletConfig().getServletContext().getRealPath("/"),
                CmsUpdateServlet.instance.getServletContext().getInitParameter(
                    OpenCmsServlet.SERVLET_PARAM_OPEN_CMS_SERVLET),
                CmsUpdateServlet.instance.getServletContext().getInitParameter(
                    OpenCmsServlet.SERVLET_PARAM_DEFAULT_WEB_APPLICATION));
            // check wizards accessability
            boolean wizardEnabled = m_updateBean.getWizardEnabled();
            Window window = createWindow();
            addWindow(window);
            if (!wizardEnabled) {
                window.setContent(new CmsSetupErrorDialog("Wizard not enabled", null, new Runnable() {

                    public void run() {

                        //

                    }
                }, window));
            } else {
                displayDialog(new CmsUpdateStep01LicenseDialog());
            }

        }

    }

    private Window createWindow() {

        if (m_window != null) {
            m_window.close();
        }
        Window window = CmsBasicDialog.prepareWindow(DialogWidth.wide);
        window.setDraggable(false);
        window.setResizable(false);
        window.setClosable(false);
        m_window = window;
        return m_window;
    }

}
