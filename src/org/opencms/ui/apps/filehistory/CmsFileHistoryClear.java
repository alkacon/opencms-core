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

package org.opencms.ui.apps.filehistory;

import org.opencms.main.OpenCms;
import org.opencms.report.A_CmsReportThread;
import org.opencms.ui.A_CmsUI;
import org.opencms.ui.CmsVaadinUtils;
import org.opencms.ui.apps.Messages;
import org.opencms.ui.components.CmsDateField;
import org.opencms.ui.report.CmsReportWidget;
import org.opencms.util.CmsStringUtil;

import java.util.ArrayList;
import java.util.List;

import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.ui.AbstractSelect.NewItemHandler;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.Label;
import com.vaadin.ui.OptionGroup;
import com.vaadin.ui.Panel;
import com.vaadin.ui.VerticalLayout;

/**
 * Class for the clear file history dialog and execution.<p>
 */
public class CmsFileHistoryClear extends VerticalLayout {

    /**vaadin serial id.*/
    private static final long serialVersionUID = 1484327372474823882L;

    /**Option for maximal count of versions.*/
    private static final int VERSIONS_MAX = 50;

    /**Vaadin component.*/
    ComboBox m_numberVersionsToKeep;

    /**Vaadin component.*/
    private Button m_cancel;

    /**Vaadin component.*/
    private CmsDateField m_dateField;

    /**Vaadin component.*/
    private OptionGroup m_mode;

    /**Vaadin component.*/
    private Button m_ok;

    /**Vaadin component.*/
    private Panel m_optionPanel;

    /**Vaadin component.*/
    private Panel m_reportPanel;

    /**Vaadin component.*/
    private Label m_settedVersions;

    /**
     * public constructor.<p>
     *
     * @param app instance of calling app
     */
    public CmsFileHistoryClear(final CmsFileHistoryApp app) {
        CmsVaadinUtils.readAndLocalizeDesign(this, CmsVaadinUtils.getWpMessagesForCurrentLocale(), null);

        //Setup ui components
        setupVersionSettingsLabel();
        setupVersionsToKeepComboBox();
        setupModeOptions();

        m_ok.addClickListener(new ClickListener() {

            private static final long serialVersionUID = -6314367378702836242L;

            public void buttonClick(ClickEvent event) {

                startCleanAndShowReport();
            }
        });

        m_cancel.addClickListener(new ClickListener() {

            private static final long serialVersionUID = 9147968498465234528L;

            public void buttonClick(ClickEvent event) {

                app.openSubView("", true);
            }
        });
    }

    /**
     * Starts the clean thread and shows the report.<p>
     * Hides other formular elements, except for cancel button.<p>
     */
    void startCleanAndShowReport() {

        //Start clean process in thread
        A_CmsReportThread thread = startThread();

        //Update UI
        m_optionPanel.setVisible(false);
        m_reportPanel.setVisible(true);

        CmsReportWidget report = new CmsReportWidget(thread);
        report.setWidth("100%");
        report.setHeight("700px");

        m_reportPanel.setContent(report);

        m_ok.setEnabled(false);
    }

    /**
     * Sets up the OptionGroup for the mode for deleted resources.<p>
     */
    private void setupModeOptions() {

        int versionsDeleted = OpenCms.getSystemInfo().getHistoryVersionsAfterDeletion();

        String mode;

        if (versionsDeleted == 0) {
            mode = CmsFileHistoryApp.MODE_DISABLED;
        } else if (versionsDeleted == 1) {
            mode = CmsFileHistoryApp.MODE_WITHOUTVERSIONS;
        } else if ((versionsDeleted > 1) || (versionsDeleted == -1)) {
            mode = CmsFileHistoryApp.MODE_WITHVERSIONS;
        } else {
            mode = CmsFileHistoryApp.MODE_DISABLED;
        }
        m_mode.setValue(mode);
    }

    /**
     * Sets the label showing maximal version number from settings.<p>
     */
    private void setupVersionSettingsLabel() {

        int numberHistoryVersions = OpenCms.getSystemInfo().getHistoryVersions();

        //Convert int number to readable Text
        String numberString = String.valueOf(numberHistoryVersions);
        if (numberHistoryVersions == CmsFileHistoryApp.NUMBER_VERSIONS_DISABLED) {
            numberString = CmsVaadinUtils.getMessageText(Messages.GUI_FILEHISTORY_SETTINGS_VERSIONS_DISABLED_0);
        }
        if (numberHistoryVersions == CmsFileHistoryApp.NUMBER_VERSIONS_UNLIMITED) {
            numberString = CmsVaadinUtils.getMessageText(Messages.GUI_FILEHISTORY_SETTINGS_VERSIONS_UNLIMITED_0);
        }
        m_settedVersions.setContentMode(ContentMode.HTML);
        m_settedVersions.setValue(
            CmsVaadinUtils.getMessageText(Messages.GUI_FILEHISTORY_DELETE_VERSIONINFO_1, numberString));
    }

    /**
     * Sets up the combo box for the amount of versions to keep.<p>
     */
    private void setupVersionsToKeepComboBox() {

        final List<Integer> items = new ArrayList<Integer>();

        //Add default values to ComboBox
        for (int i = 0; i < 10; i++) {
            m_numberVersionsToKeep.addItem(new Integer(i));
            items.add(new Integer(i));
        }
        for (int i = 10; i <= VERSIONS_MAX; i += 5) {
            m_numberVersionsToKeep.addItem(new Integer(i));
            items.add(new Integer(i));
        }
        m_numberVersionsToKeep.setPageLength(m_numberVersionsToKeep.size());
        m_numberVersionsToKeep.setNullSelectionAllowed(false);
        m_numberVersionsToKeep.setTextInputAllowed(true);

        //Alow user to enter other values
        m_numberVersionsToKeep.setNewItemsAllowed(true);
        m_numberVersionsToKeep.setNewItemHandler(new NewItemHandler() {

            private static final long serialVersionUID = -1962380117946789444L;

            public void addNewItem(String newItemCaption) {

                int num = CmsStringUtil.getIntValue(newItemCaption, -1, "user entered version number is not a number");
                if ((num > 1) && !items.contains(new Integer(num))) {
                    m_numberVersionsToKeep.addItem(new Integer(num));
                    m_numberVersionsToKeep.select(new Integer(num));
                }
            }
        });

        //Select the value which correspons to current history setting.
        int numberHistoryVersions = OpenCms.getSystemInfo().getHistoryVersions();
        if (numberHistoryVersions == CmsFileHistoryApp.NUMBER_VERSIONS_DISABLED) {
            numberHistoryVersions = 0;
        }
        if (numberHistoryVersions == CmsFileHistoryApp.NUMBER_VERSIONS_UNLIMITED) {
            numberHistoryVersions = VERSIONS_MAX;
        }
        m_numberVersionsToKeep.select(new Integer(numberHistoryVersions));
    }

    /**
     * Starts the thread for deleting versions<p>
     *
     * @return started thread
     */
    private A_CmsReportThread startThread() {

        //Maximal count of versions for current resources.
        int versions = ((Integer)m_numberVersionsToKeep.getValue()).intValue();

        //Maximal count of versions for deleted resources.
        int versionsDeleted = versions;

        if (m_mode.getValue().equals(CmsFileHistoryApp.MODE_DISABLED)) {
            versionsDeleted = 0;
        }
        if (m_mode.getValue().equals(CmsFileHistoryApp.MODE_WITHOUTVERSIONS)) {
            versionsDeleted = 1;
        }

        long date = m_dateField.getValue() != null ? m_dateField.getValue().getTime() : 0;

        CmsHistoryClearThread thread = new CmsHistoryClearThread(
            A_CmsUI.getCmsObject(),
            versions,
            versionsDeleted,
            date);
        thread.start();
        return thread;
    }
}
