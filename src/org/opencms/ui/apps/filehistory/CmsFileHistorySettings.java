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
import org.opencms.main.OpenCms;
import org.opencms.ui.CmsVaadinUtils;
import org.opencms.ui.apps.Messages;
import org.opencms.util.CmsStringUtil;
import org.opencms.util.CmsUUID;

import java.util.ArrayList;
import java.util.List;

import com.vaadin.data.util.BeanItemContainer;
import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.Label;
import com.vaadin.ui.OptionGroup;
import com.vaadin.ui.Panel;
import com.vaadin.ui.VerticalLayout;

/**
 * Class for the history settings dialog.<p>
 */
public class CmsFileHistorySettings extends VerticalLayout {

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

    /**Vaadin serial id.*/
    private static final long serialVersionUID = 5595583577732283758L;

    /**Vaadin component.*/
    private Button m_edit;

    /**Vaadin component.*/
    private OptionGroup m_mode;

    /**Vaadin component.*/
    private ComboBox m_numberVersions;

    /**Vaadin component.*/
    private Panel m_statusPanel;

    /**
     * Public constructor of class.<p>
     *
     * @param app instance of history app.
     * @param state state passed from app with information if settings were edited or are not valid
     */
    public CmsFileHistorySettings(final CmsFileHistoryApp app, String state) {
        CmsVaadinUtils.readAndLocalizeDesign(this, CmsVaadinUtils.getWpMessagesForCurrentLocale(), null);

        if (CmsStringUtil.isEmptyOrWhitespaceOnly(state) | state.equals(CmsFileHistoryApp.PATH_SETTINGS)) {
            m_statusPanel.setVisible(false);
        } else {
            Label status = new Label();
            status.setWidth("100%");
            status.setContentMode(ContentMode.HTML);
            if (state.startsWith(CmsFileHistoryApp.PATH_SETTINGS_INVALID)) {
                status.setValue(
                    "<p width='100%' align='center' style='color:#FF0000;'>"
                        + CmsVaadinUtils.getMessageText(Messages.GUI_FILEHISTORY_SETTINGS_INVALID_0)
                        + "</p>");
            } else {
                status.setValue("<p width='100%' align='center'>Settings saved</p>");
            }

            m_statusPanel.setContent(status);
        }

        setupVersionComboBox();
        setupModeOptions();

        m_edit.addClickListener(new ClickListener() {

            private static final long serialVersionUID = 161296255232053110L;

            public void buttonClick(ClickEvent event) {

                app.openSubView(saveOptions(), true);
            }
        });
    }

    /**
     * Saves the options.<p>
     *
     * @return Path to be called in app.
     */
    String saveOptions() {

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
            return CmsFileHistoryApp.PATH_SETTINGS_INVALID;
        }
        OpenCms.getSystemInfo().setVersionHistorySettings(enabled, versions, versionsDeleted);
        OpenCms.writeConfiguration(CmsSystemConfiguration.class);

        return CmsFileHistoryApp.PATH_SETTINGS_SAVE;
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
}
