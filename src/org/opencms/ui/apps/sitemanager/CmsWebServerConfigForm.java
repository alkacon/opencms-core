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

package org.opencms.ui.apps.sitemanager;

import org.opencms.main.OpenCms;
import org.opencms.site.CmsSiteManagerImpl;
import org.opencms.ui.A_CmsUI;
import org.opencms.ui.CmsVaadinUtils;
import org.opencms.ui.report.CmsReportWidget;

import java.util.Map;

import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.FormLayout;
import com.vaadin.ui.Panel;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;

/**
 * Class for the Web server configuration form and execution of script.
 *
 */

public class CmsWebServerConfigForm extends VerticalLayout {

    /**generated id. */
    private static final long serialVersionUID = 6872090597762705805L;

    /**Vaadin component. */
    Button m_cancel;

    /**Vaadin component. */
    Button m_exec;

    /**Vaadin component. */
    TextField m_fieldConfigTemplate;

    /**Vaadin component. */
    TextField m_fieldSecureTemplate;

    /**Vaadin component. */
    TextField m_fieldTargetPath;

    /**Vaadin component. */
    TextField m_fieldScript;

    /**Vaadin component. */
    TextField m_fieldLogging;

    /**Vaadin component. */
    TextField m_fieldPrefix;

    /**Vaadin component. */
    FormLayout m_threadReport;

    /**Vaadin component. */
    Panel m_report;

    /**Vaadin component. */
    Panel m_form;

    /**Site Manager instance. */
    private CmsSiteManager m_manager;

    /**
     * public Constructor.
     *
     * @param manager sitemanager instance
     */
    public CmsWebServerConfigForm(CmsSiteManager manager) {
        m_manager = manager;
        CmsVaadinUtils.readAndLocalizeDesign(this, CmsVaadinUtils.getWpMessagesForCurrentLocale(), null);
        m_report.setVisible(false);
        m_cancel.addClickListener(new ClickListener() {

            private static final long serialVersionUID = 5105904639509364528L;

            public void buttonClick(ClickEvent event) {

                cancel();

            }

        });

        m_exec.addClickListener(new ClickListener() {

            private static final long serialVersionUID = 7862341970072428829L;

            public void buttonClick(ClickEvent event) {

                m_report.setVisible(true);
                m_form.setVisible(false);

                m_exec.setEnabled(false);

                Map<String, String> webconfig = OpenCms.getSiteManager().getWebServerConfig();
                CmsSitesWebserverThread thread = new CmsSitesWebserverThread(
                    A_CmsUI.getCmsObject(),
                    webconfig.get(CmsSiteManagerImpl.WEB_SERVER_CONFIG_TARGETPATH),
                    webconfig.get(CmsSiteManagerImpl.WEB_SERVER_CONFIG_CONFIGTEMPLATE),
                    webconfig.get(CmsSiteManagerImpl.WEB_SERVER_CONFIG_WEBSERVERSCRIPT),
                    webconfig.get(CmsSiteManagerImpl.WEB_SERVER_CONFIG_FILENAMEPREFIX),
                    webconfig.get(CmsSiteManagerImpl.WEB_SERVER_CONFIG_LOGGINGDIR),
                    webconfig.get(CmsSiteManagerImpl.WEB_SERVER_CONFIG_SECURETEMPLATE));
                thread.start();
                CmsReportWidget report = new CmsReportWidget(thread);
                m_threadReport.addComponent(report);

            }

        });

        Map<String, String> webconfig = OpenCms.getSiteManager().getWebServerConfig();

        m_fieldConfigTemplate.setValue(webconfig.get(CmsSiteManagerImpl.WEB_SERVER_CONFIG_CONFIGTEMPLATE));
        m_fieldSecureTemplate.setValue(webconfig.get(CmsSiteManagerImpl.WEB_SERVER_CONFIG_SECURETEMPLATE));
        m_fieldTargetPath.setValue(webconfig.get(CmsSiteManagerImpl.WEB_SERVER_CONFIG_TARGETPATH));
        m_fieldScript.setValue(webconfig.get(CmsSiteManagerImpl.WEB_SERVER_CONFIG_WEBSERVERSCRIPT));
        m_fieldLogging.setValue(webconfig.get(CmsSiteManagerImpl.WEB_SERVER_CONFIG_LOGGINGDIR));
        m_fieldPrefix.setValue(webconfig.get(CmsSiteManagerImpl.WEB_SERVER_CONFIG_FILENAMEPREFIX));

    }

    /**
     * Cancels site edit.<p>
     */
    void cancel() {

        m_manager.openSubView("", true);
    }

}
