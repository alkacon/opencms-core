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

package org.opencms.ui.apps.shell;

import org.opencms.file.CmsObject;
import org.opencms.main.CmsException;
import org.opencms.main.CmsLog;
import org.opencms.main.OpenCms;
import org.opencms.ui.A_CmsUI;
import org.opencms.ui.CmsVaadinUtils;
import org.opencms.ui.apps.Messages;
import org.opencms.ui.components.CmsBasicDialog;
import org.opencms.ui.components.CmsBasicDialog.DialogWidth;
import org.opencms.util.CmsUUID;

import org.apache.commons.logging.Log;

import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Window;
import com.vaadin.v7.ui.ComboBox;
import com.vaadin.v7.ui.TextArea;
import com.vaadin.v7.ui.VerticalLayout;

/**
 * Layout for shell script settings and input dialog.<p>
 */
public class CmsShellScriptLayout extends VerticalLayout {

    /** The log instance for this class. */
    static final Log LOG = CmsLog.getLog(CmsShellScriptLayout.class);

    /**vaadin serial id. */
    private static final long serialVersionUID = -7284574557422737112L;

    /**Vaadin component. */
    private ComboBox m_site;

    /**Vaadin component. */
    private ComboBox m_project;

    /**Vaadin component. */
    private TextArea m_script;

    /**Vaadin component. */
    private Button m_ok;

    /**
     * public constructor.<p>
     */
    public CmsShellScriptLayout() {

        CmsVaadinUtils.readAndLocalizeDesign(this, CmsVaadinUtils.getWpMessagesForCurrentLocale(), null);
        m_site.setContainerDataSource(CmsVaadinUtils.getAvailableSitesContainer(A_CmsUI.getCmsObject(), "caption"));
        m_site.setItemCaptionPropertyId("caption");
        m_site.select(A_CmsUI.getCmsObject().getRequestContext().getSiteRoot());
        m_project.setContainerDataSource(CmsVaadinUtils.getProjectsContainer(A_CmsUI.getCmsObject(), "caption"));
        m_project.setItemCaptionPropertyId("caption");
        m_project.select(A_CmsUI.getCmsObject().getRequestContext().getCurrentProject().getUuid());
        m_site.setNewItemsAllowed(false);
        m_site.setNullSelectionAllowed(false);
        m_project.setNewItemsAllowed(false);
        m_project.setNullSelectionAllowed(false);

        m_script.setValue(CmsVaadinUtils.getMessageText(Messages.GUI_SHELL_SCRIPT_APP_INI_COMMENT_0));

        m_ok.addClickListener(new Button.ClickListener() {

            private static final long serialVersionUID = 6836938102455627631L;

            public void buttonClick(ClickEvent event) {

                try {
                    Window window = CmsBasicDialog.prepareWindow(DialogWidth.wide);

                    CmsObject cms = OpenCms.initCmsObject(A_CmsUI.getCmsObject());
                    setupCms(cms);
                    CmsShellScriptThread thread = new CmsShellScriptThread(cms, getScript());
                    CmsShellScriptReportDialog dialog = new CmsShellScriptReportDialog(thread, window);
                    window.setContent(dialog);

                    A_CmsUI.get().addWindow(window);
                    thread.start();
                } catch (CmsException e) {
                    LOG.error("Unable to initialize CmsObject", e);
                }
            }
        });
    }

    /**
     * Gets the currently entered script.<p>
     *
     * @return String
     */
    protected String getScript() {

        return m_script.getValue();
    }

    /**
     * Sets up given CmsObject with currently set Project and Site.<p>
     *
     * @param cms CmsObject to gets adjusted to input fields
     */
    protected void setupCms(CmsObject cms) {

        cms.getRequestContext().setUri("/");
        cms.getRequestContext().setSiteRoot((String)m_site.getValue());
        try {
            cms.getRequestContext().setCurrentProject(cms.readProject((CmsUUID)m_project.getValue()));
        } catch (CmsException e) {
            LOG.error("Unable to read Project", e);
        }
    }

}
