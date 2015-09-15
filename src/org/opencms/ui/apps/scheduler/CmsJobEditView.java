/*
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (c) Alkacon Software GmbH (http://www.alkacon.com)
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

package org.opencms.ui.apps.scheduler;

import org.opencms.file.CmsResourceFilter;
import org.opencms.main.CmsContextInfo;
import org.opencms.main.CmsIllegalArgumentException;
import org.opencms.monitor.CmsMemoryMonitor;
import org.opencms.notification.CmsContentNotificationJob;
import org.opencms.relations.CmsExternalLinksValidator;
import org.opencms.relations.CmsInternalRelationsValidationJob;
import org.opencms.scheduler.CmsScheduledJobInfo;
import org.opencms.scheduler.jobs.CmsCreateImageSizeJob;
import org.opencms.scheduler.jobs.CmsDeleteExpiredResourcesJob;
import org.opencms.scheduler.jobs.CmsHistoryClearJob;
import org.opencms.scheduler.jobs.CmsImageCacheCleanupJob;
import org.opencms.scheduler.jobs.CmsPublishJob;
import org.opencms.scheduler.jobs.CmsStaticExportJob;
import org.opencms.scheduler.jobs.CmsUnsubscribeDeletedResourcesJob;
import org.opencms.search.CmsSearchManager;
import org.opencms.ui.A_CmsUI;
import org.opencms.ui.CmsVaadinUtils;
import org.opencms.ui.components.OpenCmsTheme;
import org.opencms.ui.components.fileselect.CmsResourceSelectField;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

import com.google.common.base.Strings;
import com.vaadin.data.Validator;
import com.vaadin.data.Validator.InvalidValueException;
import com.vaadin.server.FontAwesome;
import com.vaadin.shared.ui.combobox.FilteringMode;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.CheckBox;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.Component;
import com.vaadin.ui.Field;
import com.vaadin.ui.FormLayout;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.ValoTheme;

/**
 * Form used to edit a scheduled job.<p>
 */
public class CmsJobEditView extends VerticalLayout {

    /**
     * Validator for the cron expression field.<p>
     */
    public class CronExpressionValidator implements Validator {

        /** Serial version id. */
        private static final long serialVersionUID = 1L;

        /**
         * @see com.vaadin.data.Validator#validate(java.lang.Object)
         */
        public void validate(Object value) throws InvalidValueException {

            CmsScheduledJobInfo info = new CmsScheduledJobInfo();
            String stringValue = (String)value;
            try {
                info.setCronExpression(stringValue);
            } catch (CmsIllegalArgumentException e) {
                throw new InvalidValueException(e.getLocalizedMessage(A_CmsUI.get().getLocale()));
            }
        }

    }

    /**
     * Validator for the Java class name field.<p>
     */
    public class JobClassValidator implements Validator {

        /** Serial version id. */
        private static final long serialVersionUID = 1L;

        /**
         * @see com.vaadin.data.Validator#validate(java.lang.Object)
         */
        public void validate(Object value) throws InvalidValueException {

            CmsScheduledJobInfo info = new CmsScheduledJobInfo();
            String stringValue = (String)value;
            try {
                info.setClassName(stringValue);
            } catch (CmsIllegalArgumentException e) {
                throw new InvalidValueException(e.getLocalizedMessage(A_CmsUI.get().getLocale()));
            }
        }
    }

    /**
     * Validator for the job name.<p>
     */
    public class JobNameValidator implements Validator {

        /** Serial version id. */
        private static final long serialVersionUID = 1L;

        /**
         * @see com.vaadin.data.Validator#validate(java.lang.Object)
         */
        public void validate(Object value) throws InvalidValueException {

            CmsScheduledJobInfo info = new CmsScheduledJobInfo();
            String name = (String)value;
            try {
                info.setJobName(name);
            } catch (CmsIllegalArgumentException e) {
                throw new InvalidValueException(e.getLocalizedMessage(A_CmsUI.get().getLocale()));

            }

        }
    }

    /**
     * Widget used to display a line of text and also a remove button to remove the widget.<p>
     */
    class ParamLine extends HorizontalLayout {

        /** Serial version id. */
        private static final long serialVersionUID = 1L;

        /** The text input field. */
        private TextField m_input;

        /**
         * Creates a new instance.<p>
         *
         * @param content the initial content of the text field
         */
        public ParamLine(String content) {
            setWidth("100%");
            TextField input = new TextField();
            m_input = input;
            setCaption("Parameter");
            setSpacing(true);
            input.setValue(content);
            input.setWidth("100%");
            addComponent(input);
            setExpandRatio(input, 1f);
            Button deleteButton = new Button("");
            deleteButton.addStyleName(ValoTheme.BUTTON_LINK);
            deleteButton.setIcon(FontAwesome.TIMES_CIRCLE);
            deleteButton.addStyleName(OpenCmsTheme.BUTTON_UNPADDED);
            deleteButton.setDescription("Remove parameter");
            deleteButton.addClickListener(new ClickListener() {

                private static final long serialVersionUID = 1L;

                public void buttonClick(ClickEvent event) {

                    m_paramContainer.removeComponent(ParamLine.this);

                }
            });
            addComponent(deleteButton);

        }

        /**
         * Gets the value of the text field.<p>
         *
         * @return the value of the text field
         */
        public String getValue() {

            return m_input.getValue();
        }

    }

    /** Serial version id. */
    private static final long serialVersionUID = 1L;

    /** Button to add a new parameter. */
    private Button m_buttonAddParam;

    /** Field to activate / deactivate the job. */
    private CheckBox m_fieldActive;

    /** Field for the cron expression. */
    private ComboBox m_fieldCron;

    /** Field for the encoding. */
    private TextField m_fieldEncoding;

    /** Field for the class name. */
    private ComboBox m_fieldJobClass;

    /** Field for the job name. */
    private TextField m_fieldJobName;

    /** Field for the locale. */
    private TextField m_fieldLocale;

    /** Field for the project. */
    private TextField m_fieldProject;

    /** Field for the remote address. */
    private TextField m_fieldRemoteAddress;

    /** 'Reuse instance' check box. */
    private CheckBox m_fieldReuseInstance;

    /** Field for the site root. */
    private CmsResourceSelectField m_fieldSiteRoot;

    /** Field for the URI. */
    private CmsResourceSelectField m_fieldUri;

    /** Field for the user name. */
    private TextField m_fieldUser;

    /** Form containing the job parameters. */
    FormLayout m_paramContainer;

    /**
     * Creates a new instance.<p>
     */
    public CmsJobEditView() {

        CmsVaadinUtils.readAndLocalizeDesign(this, CmsVaadinUtils.getWpMessagesForCurrentLocale(), null);
        m_buttonAddParam.addClickListener(new ClickListener() {

            /** Serial version id. */
            private static final long serialVersionUID = 1L;

            public void buttonClick(ClickEvent event) {

                addParamLine("");
            }
        });

        m_fieldJobName.addValidator(new JobNameValidator());
        m_fieldJobClass.setFilteringMode(FilteringMode.OFF);
        m_fieldCron.setFilteringMode(FilteringMode.OFF);
        m_fieldCron.setNewItemsAllowed(true);
        m_fieldJobClass.setNewItemsAllowed(true);
        m_fieldJobClass.setPageLength(20);
        m_fieldJobClass.addValidator(new JobClassValidator());
        m_fieldCron.addValidator(new CronExpressionValidator());
        m_fieldJobClass.addItem(

            CmsInternalRelationsValidationJob.class.getName());
        m_fieldJobClass.addItem(CmsPublishJob.class.getName());
        m_fieldJobClass.addItem(CmsStaticExportJob.class.getName());
        m_fieldJobClass.addItem(CmsExternalLinksValidator.class.getName());
        m_fieldJobClass.addItem(CmsMemoryMonitor.class.getName());
        m_fieldJobClass.addItem(CmsSearchManager.class.getName());
        m_fieldJobClass.addItem(CmsContentNotificationJob.class.getName());
        m_fieldJobClass.addItem(CmsCreateImageSizeJob.class.getName());
        m_fieldJobClass.addItem(CmsImageCacheCleanupJob.class.getName());
        m_fieldJobClass.addItem(CmsHistoryClearJob.class.getName());
        m_fieldJobClass.addItem(CmsDeleteExpiredResourcesJob.class.getName());
        m_fieldJobClass.addItem(CmsUnsubscribeDeletedResourcesJob.class.getName());

        for (String item : new String[] {
            "0 0 3 * * ?",
            "0 0/30 * * * ?",
            "0 30 8 ? * 4",
            "0 15 18 20 * ?",
            "0 45 15 ? * 1 2007-2009"}) {
            m_fieldCron.addItem(item);
        }
        m_fieldSiteRoot.setResourceFilter(CmsResourceFilter.DEFAULT_FOLDERS);
    }

    /**
     * Initializes the form fields with values from the given job bean.<P>
     *
     * @param info the job bean with which to fill the form
     */
    public void loadFromBean(CmsScheduledJobInfo info) {

        m_fieldJobName.setValue(Strings.nullToEmpty(info.getJobName()));
        setComboBoxValue(m_fieldJobClass, Strings.nullToEmpty(info.getClassName()));
        setComboBoxValue(m_fieldCron, Strings.nullToEmpty(info.getCronExpression()));
        m_fieldReuseInstance.setValue(Boolean.valueOf(info.isReuseInstance()));
        m_fieldActive.setValue(Boolean.valueOf(info.isActive()));

        CmsContextInfo context = info.getContextInfo();
        m_fieldUser.setValue(context.getUserName());
        m_fieldProject.setValue(context.getProjectName());
        m_fieldSiteRoot.setValue(context.getSiteRoot());
        m_fieldUri.setValue(context.getRequestedUri());
        m_fieldLocale.setValue(context.getLocaleName());
        m_fieldEncoding.setValue(context.getEncoding());
        m_fieldRemoteAddress.setValue(context.getRemoteAddr());
        for (Map.Entry<String, String> entry : info.getParameters().entrySet()) {
            addParamLine(entry.getKey(), entry.getValue());
        }
    }

    /**
     * Try to save the form values to the given job bean.<p>
     *
     * @param info the bean in which the information should be saved
     *
     * @return true if setting the information was successful
     */
    public boolean trySaveToBean(CmsScheduledJobInfo info) {

        if (!validate()) {
            return false;
        }
        info.setJobName(m_fieldJobName.getValue());
        info.setClassName((String)(m_fieldJobClass.getValue()));
        info.setCronExpression((String)m_fieldCron.getValue());
        info.setReuseInstance(m_fieldReuseInstance.getValue().booleanValue());
        info.setActive(m_fieldActive.getValue().booleanValue());
        info.setParameters(readParams());
        CmsContextInfo context = info.getContextInfo();
        context.setUserName(m_fieldUser.getValue());
        context.setProjectName(m_fieldProject.getValue());
        context.setSiteRoot(m_fieldSiteRoot.getValue());
        context.setRequestedUri(m_fieldUri.getValue());
        context.setLocaleName(m_fieldLocale.getValue());
        context.setEncoding(m_fieldEncoding.getValue());
        context.setRemoteAddr(m_fieldRemoteAddress.getValue());
        return true;
    }

    /**
     * Validates the form fields.<p>
     *
     * @return true if there were any validation erors
     */
    public boolean validate() {

        List<Field<?>> fields = Arrays.<Field<?>> asList(m_fieldJobName, m_fieldJobClass, m_fieldCron);
        boolean result = true;
        for (Field<?> field : fields) {
            try {
                field.validate();
            } catch (InvalidValueException e) {
                result = false;
            }
        }
        return result;
    }

    /**
     * Adds a new parameter input field with the given content.<p>
     *
     * @param content the content
     */
    void addParamLine(String content) {

        m_paramContainer.addComponent(new ParamLine(content));
    }

    /**
     * Reads the job parameters from the parameter input fields.<p>
     *
     * @return the job parameters
     */
    SortedMap<String, String> readParams() {

        SortedMap<String, String> result = new TreeMap<String, String>();

        for (Component component : m_paramContainer) {
            if (component instanceof ParamLine) {
                ParamLine paramLine = (ParamLine)component;
                String keyAndValue = paramLine.getValue();
                int eqPos = keyAndValue.indexOf("=");
                if (eqPos >= 0) {
                    String key = keyAndValue.substring(0, eqPos);
                    String value = keyAndValue.substring(eqPos + 1);
                    result.put(key, value);
                }
            }

        }
        return result;
    }

    /**
     * Adds a new widget for entering a job parameter.<p>
     *
     * @param key the preselected key
     * @param value the preselected value
     */
    private void addParamLine(String key, String value) {

        addParamLine(key + "=" + value);

    }

    /**
     * Sets the value of a combo box, adding it as an option if it isn't already.<p>
     *
     * @param box the combo box
     * @param value the value to set
     */
    private void setComboBoxValue(ComboBox box, String value) {

        if (!box.containsId(value)) {
            box.addItem(value);
        }
        box.select(value);
    }

}
