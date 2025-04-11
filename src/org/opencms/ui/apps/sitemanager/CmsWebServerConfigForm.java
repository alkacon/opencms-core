/*
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (c) Alkacon Software GmbH & Co. KG (https://www.alkacon.com)
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
 * company website: https://www.alkacon.com
 *
 * For further information about OpenCms, please see the
 * project website: https://www.opencms.org
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package org.opencms.ui.apps.sitemanager;

import org.opencms.main.CmsLog;
import org.opencms.main.OpenCms;
import org.opencms.site.CmsSiteManagerImpl;
import org.opencms.ui.A_CmsUI;
import org.opencms.ui.CmsVaadinUtils;
import org.opencms.ui.components.CmsBasicDialog;
import org.opencms.ui.report.CmsReportWidget;

import java.util.Map;

import org.apache.commons.logging.Log;

import com.google.common.collect.Maps;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.FormLayout;
import com.vaadin.ui.Panel;
import com.vaadin.v7.ui.TextField;

/**
 * Class for the Web server configuration form and execution of script.
 */
public class CmsWebServerConfigForm extends CmsBasicDialog {

    /**vaadin serial id. */
    private static final long serialVersionUID = 6872090597762705805L;

    /** The logger for this class. */
    static Log LOG = CmsLog.getLog(CmsWebServerConfigForm.class.getName());

    /**Vaadin component. */
    private Button m_cancel;

    /**Vaadin component. */
    private Button m_ok;

    /**Vaadin component. */
    private TextField m_fieldConfigTemplate;

    /**Vaadin component. */
    private TextField m_fieldLogging;

    /**Vaadin component. */
    private TextField m_fieldPrefix;

    /**Vaadin component. */
    private TextField m_fieldScript;

    /**Vaadin component. */
    private TextField m_fieldSecureTemplate;

    /**Vaadin component. */
    private TextField m_fieldTargetPath;

    /**Vaadin component. */
    private Panel m_form;

    /**Site Manager instance. */
    private CmsSiteManager m_manager;

    /**Vaadin component. */
    private Panel m_report;

    /**Vaadin component. */
    private FormLayout m_threadReport;

    /**
     * Public Constructor.<p>
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

        m_ok.addClickListener(new ClickListener() {

            private static final long serialVersionUID = 7862341970072428829L;

            public void buttonClick(ClickEvent event) {

                runScript();
            }
        });

        Map<String, String> webconfig = OpenCms.getSiteManager().getWebServerConfig();
        if (webconfig == null) {
            webconfig = Maps.newHashMap();

        }
        m_fieldConfigTemplate.setValue(nullToEmpty(webconfig.get(CmsSiteManagerImpl.WEB_SERVER_CONFIG_CONFIGTEMPLATE)));
        m_fieldSecureTemplate.setValue(nullToEmpty(webconfig.get(CmsSiteManagerImpl.WEB_SERVER_CONFIG_SECURETEMPLATE)));
        m_fieldTargetPath.setValue(nullToEmpty(webconfig.get(CmsSiteManagerImpl.WEB_SERVER_CONFIG_TARGETPATH)));
        m_fieldScript.setValue(nullToEmpty(webconfig.get(CmsSiteManagerImpl.WEB_SERVER_CONFIG_WEBSERVERSCRIPT)));
        m_fieldLogging.setValue(nullToEmpty(webconfig.get(CmsSiteManagerImpl.WEB_SERVER_CONFIG_LOGGINGDIR)));
        m_fieldPrefix.setValue(nullToEmpty(webconfig.get(CmsSiteManagerImpl.WEB_SERVER_CONFIG_FILENAMEPREFIX)));
    }

    /**
     * Cancels site edit.<p>
     */
    void cancel() {

        m_manager.closeDialogWindow(false);
    }

    /**
     * Converts null to an empty string and returns all other strings unchanged.<p>
     *
     * @param s the input string
     * @return the output string
     */
    String nullToEmpty(String s) {

        if (s == null) {
            return "";
        }
        return s;

    }

    /**
     * Executes script.<p>
     */
    void runScript() {

        //Show report field and hide form fields
        m_report.setVisible(true);
        m_form.setVisible(false);
        m_ok.setEnabled(false);

        Map<String, String> webconfig = OpenCms.getSiteManager().getWebServerConfig();
        if (webconfig == null) {
            webconfig = Maps.newHashMap();
        }
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
}