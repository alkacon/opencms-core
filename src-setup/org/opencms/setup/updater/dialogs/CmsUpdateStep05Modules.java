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

package org.opencms.setup.updater.dialogs;

import org.opencms.module.CmsModule;
import org.opencms.setup.CmsUpdateUI;
import org.opencms.setup.CmsVaadinUpdateThread;
import org.opencms.ui.CmsVaadinUtils;
import org.opencms.ui.report.CmsStreamReportWidget;
import org.opencms.util.CmsStringUtil;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.vaadin.server.FontAwesome;
import com.vaadin.ui.CheckBox;
import com.vaadin.ui.FormLayout;
import com.vaadin.ui.Panel;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.v7.shared.ui.label.ContentMode;
import com.vaadin.v7.ui.HorizontalLayout;
import com.vaadin.v7.ui.Label;

/**
 * The Module update dialog.<p>
 */
public class CmsUpdateStep05Modules extends A_CmsUpdateDialog {

    /**vaadin serial id. */
    private static final long serialVersionUID = 1L;

    /**vaadin component. */
    private FormLayout m_components;

    /**vaadin component. */
    private VerticalLayout m_mainLayout;

    /**vaadin component. */
    private VerticalLayout m_reportLayout;

    /**vaadin component. */
    private List<CheckBox> m_componentCheckboxes = new ArrayList<>();

    /**Module map. */
    private Map<String, CmsModule> m_componentMap = new HashMap<>();

    /**vaadin component. */
    Panel m_reportPanel;

    /**vaadin component. */
    Label m_icon;

    /**vaadin component. */
    Label m_iconFin;

    /**vaadin component. */
    HorizontalLayout m_running;

    /**vaadin component. */
    HorizontalLayout m_finished;

    /**flag if XML changes are done. */
    protected boolean m_isDone;

    /**
     * @see org.opencms.setup.updater.dialogs.A_CmsUpdateDialog#init(org.opencms.setup.CmsUpdateUI)
     */
    @Override
    public boolean init(CmsUpdateUI ui) {

        CmsVaadinUtils.readAndLocalizeDesign(this, null, null);
        super.init(ui, true, true);
        setCaption("OpenCms Update-Wizard - Update Modules");
        Map<String, CmsModule> modules = ui.getUpdateBean().getAvailableModules();
        initComponents(modules);
        m_icon.setContentMode(ContentMode.HTML);
        m_icon.setValue(FontAwesome.CLOCK_O.getHtml());
        m_iconFin.setContentMode(ContentMode.HTML);
        m_iconFin.setValue(FontAwesome.CHECK_CIRCLE_O.getHtml());
        return true;
    }

    /**
     * @see org.opencms.setup.updater.dialogs.A_CmsUpdateDialog#submitDialog()
     */
    @Override
    protected boolean submitDialog() {

        if (!m_isDone) {
            fetchModulesFromDialog();
            m_ui.getUpdateBean().prepareUpdateStep5();
            m_mainLayout.setVisible(false);
            m_reportLayout.setVisible(true);
            m_finished.setVisible(false);
            m_reportPanel.setContent(getReportContent());
        }
        return m_isDone;
    }

    /**
     * @see org.opencms.setup.updater.dialogs.A_CmsUpdateDialog#getNextDialog()
     */
    @Override
    A_CmsUpdateDialog getNextDialog() {

        return new CmsUpdateStep06FinishDialog();
    }

    /**
     * @see org.opencms.setup.updater.dialogs.A_CmsUpdateDialog#getPreviousDialog()
     */
    @Override
    A_CmsUpdateDialog getPreviousDialog() {

        return new CmsUpdateStep04SettingsDialog();
    }

    /**
     *
     */
    private void fetchModulesFromDialog() {

        Set<String> modules = new HashSet<>();

        for (CheckBox checkbox : m_componentCheckboxes) {
            CmsModule component = (CmsModule)(checkbox.getData());
            if (checkbox.getValue().booleanValue()) {
                modules.add(component.getName());
            }
        }

        List<String> moduleList = new ArrayList<>(modules);
        m_ui.getUpdateBean().setInstallModules(CmsStringUtil.listAsString(moduleList, "|"));
    }

    /**
     * Get content.<p>
     *
     * @return VerticalLayout
     */
    private VerticalLayout getReportContent() {

        VerticalLayout layout = new VerticalLayout();
        layout.setHeight("100%");
        final CmsStreamReportWidget report = new CmsStreamReportWidget();
        report.setWidth("100%");
        report.setHeight("100%");
        enableOK(false);
        Thread thread = new CmsVaadinUpdateThread(m_ui.getUpdateBean(), report);
        thread.start();
        try {
            OutputStream logStream = new FileOutputStream(m_ui.getUpdateBean().getLogName());
            report.setDelegateStream(logStream);
            report.addReportFinishedHandler(() -> {

                try {
                    logStream.close();
                    report.getStream().close();
                    enableOK(true);
                    m_finished.setVisible(true);
                    m_running.setVisible(false);
                    m_isDone = true;
                } catch (IOException e) {
                    //
                }
            });

        } catch (FileNotFoundException e1) {
            //
        }
        layout.addComponent(report);
        return layout;
    }

    /**
     * Init the module list.<p>
     *
     * @param modules to be displayed
     */
    private void initComponents(Map<String, CmsModule> modules) {

        for (String moduleName : modules.keySet()) {
            CmsModule module = modules.get(moduleName);
            CheckBox checkbox = new CheckBox();
            checkbox.setCaption(module.getNiceName() + " (" + module.getName() + " " + module.getVersionStr() + ")");
            checkbox.setDescription(module.getDescription());
            checkbox.setData(module);
            checkbox.setValue(Boolean.TRUE);
            m_components.addComponent(checkbox);
            m_componentCheckboxes.add(checkbox);
            m_componentMap.put(module.getName(), module);

        }
    }

}
