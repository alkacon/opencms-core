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

import org.opencms.configuration.CmsSystemConfiguration;
import org.opencms.file.CmsObject;
import org.opencms.main.CmsLog;
import org.opencms.main.OpenCms;
import org.opencms.report.A_CmsReportThread;
import org.opencms.ui.A_CmsUI;
import org.opencms.ui.CmsVaadinUtils;
import org.opencms.ui.apps.Messages;
import org.opencms.ui.components.CmsDateField;
import org.opencms.ui.components.CmsErrorDialog;
import org.opencms.ui.report.CmsReportDialog;
import org.opencms.util.CmsStringUtil;
import org.opencms.util.CmsUUID;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;

import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.Notification;
import com.vaadin.ui.Notification.Type;
import com.vaadin.ui.Panel;
import com.vaadin.v7.data.Property.ValueChangeEvent;
import com.vaadin.v7.data.Property.ValueChangeListener;
import com.vaadin.v7.data.util.BeanItemContainer;
import com.vaadin.v7.shared.ui.label.ContentMode;
import com.vaadin.v7.ui.AbstractSelect.NewItemHandler;
import com.vaadin.v7.ui.ComboBox;
import com.vaadin.v7.ui.Label;
import com.vaadin.v7.ui.OptionGroup;
import com.vaadin.v7.ui.VerticalLayout;

/**
 * Class for the clear file history dialog and execution.<p>
 */
public class CmsFileHistoryPanel extends VerticalLayout {

    /**
     * Bean for the elements of the combo box for number of version option.<p>
     */
    public class ComboBoxVersionsBean {

        /**Value of item.*/
        private int m_val;

        /**
         * public constructor.<p>
         *
         * @param value of item
         */
        public ComboBoxVersionsBean(int value) {

            m_val = value;
        }

        /**
         * @see java.lang.Object#equals(java.lang.Object)
         */
        @Override
        public boolean equals(Object o) {

            if (o instanceof ComboBoxVersionsBean) {
                return ((ComboBoxVersionsBean)o).getValue() == m_val;
            }
            return false;
        }

        /**
         * Getter for display String to be displayed in combo box.<p>
         *
         * @return String to be displayed
         */
        public String getDisplayValue() {

            if (m_val == CmsFileHistoryApp.NUMBER_VERSIONS_DISABLED) {
                return CmsVaadinUtils.getMessageText(Messages.GUI_FILEHISTORY_SETTINGS_VERSIONS_DISABLED_0);
            }
            if (m_val == CmsFileHistoryApp.NUMBER_VERSIONS_UNLIMITED) {
                return CmsVaadinUtils.getMessageText(Messages.GUI_FILEHISTORY_SETTINGS_VERSIONS_UNLIMITED_0);
            }
            return String.valueOf(m_val);
        }

        /**
         * Getter for (int) value of item.<p>
         *
         * @return int value
         */
        public int getValue() {

            return m_val;
        }

        /**
         * @see java.lang.Object#hashCode()
         */
        @Override
        public int hashCode() {

            return CmsUUID.getNullUUID().hashCode();
        }
    }

    /** Logger instance for this class. */
    private static final Log LOG = CmsLog.getLog(CmsFileHistoryPanel.class);

    /**vaadin serial id.*/
    private static final long serialVersionUID = 1484327372474823882L;

    /**Option for maximal count of versions.*/
    private static final int VERSIONS_MAX = 50;

    /** Button for cleaning up publish history. */
    protected Button m_cleanupPublishHistory;

    /**Vaadin component.*/
    ComboBox m_numberVersionsToKeep;

    /**Vaadin component.*/
    private CmsDateField m_dateField;

    /**Vaadin component.*/
    private OptionGroup m_deleteMode;

    /**Vaadin component.*/
    private Button m_ok;

    /**Vaadin component.*/
    private Panel m_optionPanel;

    /**Vaadin component.*/
    private Label m_settedVersions;

    /**Vaadin component.*/
    private Button m_edit;

    /**Vaadin component.*/
    private OptionGroup m_mode;

    /**Vaadin component.*/
    private ComboBox m_numberVersions;

    /**     *

     * public constructor.<p>
     *
     * @param app instance of calling app
     */
    public CmsFileHistoryPanel() {

        CmsVaadinUtils.readAndLocalizeDesign(this, CmsVaadinUtils.getWpMessagesForCurrentLocale(), null);

        //Setup ui components
        setupVersionSettingsLabel();
        setupVersionsToKeepComboBox();
        setupDeleteModeOptions();

        m_ok.addClickListener(new ClickListener() {

            private static final long serialVersionUID = -6314367378702836242L;

            public void buttonClick(ClickEvent event) {

                startCleanAndShowReport();
            }
        });
        m_cleanupPublishHistory.addClickListener(evt -> runPublishHistoryCleanup());
        m_edit.setEnabled(false);

        setupVersionComboBox();
        setupModeOptions();

        ValueChangeListener changeListener = new ValueChangeListener() {

            /**vaadin serial id.*/
            private static final long serialVersionUID = -6003215873244541851L;

            public void valueChange(ValueChangeEvent event) {

                setButtonEnabled(true);
            }
        };

        m_numberVersions.addValueChangeListener(changeListener);

        m_mode.addValueChangeListener(changeListener);

        m_edit.addClickListener(new ClickListener() {

            private static final long serialVersionUID = 161296255232053110L;

            public void buttonClick(ClickEvent event) {

                if (saveOptions()) {
                    setButtonEnabled(false);
                } else {
                    String message = CmsVaadinUtils.getMessageText(Messages.GUI_FILEHISTORY_SETTINGS_INVALID_0);
                    Notification.show(message, Type.ERROR_MESSAGE);
                }
            }
        });
    }

    /**
     * Sets the edit button enabled or disabled.<p>
     *
     * @param enable state of button
     */
    protected void setButtonEnabled(boolean enable) {

        m_edit.setEnabled(enable);
    }

    /**
     * Saves the options.<p>
     *
     * @return Path to be called in app.
     */
    boolean saveOptions() {

        //Enable history?
        boolean enabled = ((ComboBoxVersionsBean)m_numberVersions.getValue()).getValue() != CmsFileHistoryApp.NUMBER_VERSIONS_DISABLED;

        //Maximal count of versions for current resources.
        int versions = ((ComboBoxVersionsBean)m_numberVersions.getValue()).getValue();

        //Maximal count of versions for deleted resources.
        int versionsDeleted = versions;

        if (m_mode.getValue().equals(CmsFileHistoryApp.MODE_DISABLED)) {
            versionsDeleted = 0;
        }
        if (m_mode.getValue().equals(CmsFileHistoryApp.MODE_WITHOUTVERSIONS)) {
            versionsDeleted = 1;
        }

        if (m_mode.getValue().equals(CmsFileHistoryApp.MODE_WITHVERSIONS)
            && (versions == CmsFileHistoryApp.NUMBER_VERSIONS_DISABLED)) {
            return false;
        }
        OpenCms.getSystemInfo().setVersionHistorySettings(enabled, versions, versionsDeleted);
        OpenCms.writeConfiguration(CmsSystemConfiguration.class);

        return true;
    }

    /**
     * Starts the clean thread and shows the report.<p>
     */
    void startCleanAndShowReport() {

        //Start clean process in thread
        A_CmsReportThread thread = makeThread();
        String title = CmsVaadinUtils.getMessageText(Messages.GUI_FILEHISTORY_CLEAR_REPORT_TITLE_0);
        CmsReportDialog.showReportDialog(title, thread);
    }

    /**
     * Starts the thread for deleting versions.<p>
     *
     * @return started thread
     */
    private A_CmsReportThread makeThread() {

        //Maximal count of versions for current resources.
        int versions = ((Integer)m_numberVersionsToKeep.getValue()).intValue();

        //Maximal count of versions for deleted resources.
        int versionsDeleted = versions;

        if (m_deleteMode.getValue().equals(CmsFileHistoryApp.MODE_DISABLED)) {
            versionsDeleted = 0;
        }
        if (m_deleteMode.getValue().equals(CmsFileHistoryApp.MODE_WITHOUTVERSIONS)) {
            versionsDeleted = 1;
        }

        long date = m_dateField.getValue() != null ? m_dateField.getDate().getTime() : 0;

        CmsHistoryClearThread thread = new CmsHistoryClearThread(
            A_CmsUI.getCmsObject(),
            versions,
            versionsDeleted,
            date);
        return thread;
    }

    /**
     * Runs the publish history cleanup.
     */
    private void runPublishHistoryCleanup() {

        final CmsObject cms = A_CmsUI.getCmsObject();
        try {
            int numRemoved = OpenCms.getPublishManager().cleanupPublishHistory(cms);
            Notification notification = new Notification(
                CmsVaadinUtils.getMessageText(Messages.GUI_FILEHISTORY_CLEANED_PUBLISH_HISTORY_1, numRemoved),
                Notification.Type.HUMANIZED_MESSAGE);
            notification.setDelayMsec(Notification.DELAY_FOREVER);
            notification.show(A_CmsUI.get().getPage());
        } catch (Exception e) {
            LOG.error(e.getLocalizedMessage(), e);
            CmsErrorDialog.showErrorDialog(e);
        }

    }

    /**
     * Sets up the OptionGroup for the mode for deleted resources.<p>
     */
    private void setupDeleteModeOptions() {

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
        m_deleteMode.setValue(mode);
    }

    /**
     * Sets up the OptionGroup for the mode.<p>
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
     * Fills the combo box to choose the number of versions which shouls be stored.<p>
     */
    private void setupVersionComboBox() {

        //Setup beans
        List<ComboBoxVersionsBean> beans = new ArrayList<ComboBoxVersionsBean>();

        //Disabled
        beans.add(new ComboBoxVersionsBean(CmsFileHistoryApp.NUMBER_VERSIONS_DISABLED));

        //1-10
        for (int i = 1; i <= 10; i++) {
            beans.add(new ComboBoxVersionsBean(i));
        }

        //15-50
        for (int i = 15; i <= 50; i += 5) {
            beans.add(new ComboBoxVersionsBean(i));
        }

        //Unlimited
        beans.add(new ComboBoxVersionsBean(CmsFileHistoryApp.NUMBER_VERSIONS_UNLIMITED));

        BeanItemContainer<ComboBoxVersionsBean> objects = new BeanItemContainer<ComboBoxVersionsBean>(
            ComboBoxVersionsBean.class,
            beans);
        m_numberVersions.setContainerDataSource(objects);
        m_numberVersions.setItemCaptionPropertyId("displayValue");

        m_numberVersions.setNullSelectionAllowed(false);
        m_numberVersions.setTextInputAllowed(false);
        m_numberVersions.setPageLength(beans.size());

        int numberHistoryVersions = OpenCms.getSystemInfo().getHistoryVersions();

        m_numberVersions.setValue(beans.get(beans.indexOf(new ComboBoxVersionsBean(numberHistoryVersions))));
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
            m_numberVersionsToKeep.addItem(Integer.valueOf(i));
            items.add(Integer.valueOf(i));
        }
        for (int i = 10; i <= VERSIONS_MAX; i += 5) {
            m_numberVersionsToKeep.addItem(Integer.valueOf(i));
            items.add(Integer.valueOf(i));
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
                if ((num > 1) && !items.contains(Integer.valueOf(num))) {
                    m_numberVersionsToKeep.addItem(Integer.valueOf(num));
                    m_numberVersionsToKeep.select(Integer.valueOf(num));
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
        m_numberVersionsToKeep.select(Integer.valueOf(numberHistoryVersions));
    }

}
