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

package org.opencms.ui.apps.scheduler;

import org.opencms.file.CmsResourceFilter;
import org.opencms.main.CmsIllegalArgumentException;
import org.opencms.main.CmsLog;
import org.opencms.main.CmsRuntimeException;
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
import org.opencms.ui.Messages;
import org.opencms.ui.components.OpenCmsTheme;
import org.opencms.ui.components.fileselect.CmsPathSelectField;
import org.opencms.ui.util.CmsComboNullToEmptyConverter;
import org.opencms.ui.util.CmsNullToEmptyConverter;
import org.opencms.util.CmsStringUtil;

import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

import org.apache.commons.logging.Log;

import com.vaadin.data.Validator;
import com.vaadin.data.fieldgroup.BeanFieldGroup;
import com.vaadin.server.FontAwesome;
import com.vaadin.shared.ui.combobox.FilteringMode;
import com.vaadin.ui.AbstractField;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.CheckBox;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.Component;
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
            // Job name may be needed in exception
            try {
                info.setJobName(m_fieldJobName.getValue());
            } catch (CmsRuntimeException e) {
                throw new InvalidValueException(e.getLocalizedMessage(A_CmsUI.get().getLocale()));
            }
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

            // Job name may be needed in exception
            try {
                info.setJobName(m_fieldJobName.getValue());
            } catch (CmsRuntimeException e) {
                throw new InvalidValueException(e.getLocalizedMessage(A_CmsUI.get().getLocale()));
            }
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
            deleteButton.setDescription(CmsVaadinUtils.getMessageText(Messages.GUI_SCHEDULER_REMOVE_PARAMETER_0));
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

    /** Logger instance for this class. */
    private static final Log LOG = CmsLog.getLog(CmsJobEditView.class);

    /** Serial version id. */
    private static final long serialVersionUID = 1L;

    /** Field for the job name. */
    TextField m_fieldJobName;

    /** Form containing the job parameters. */
    FormLayout m_paramContainer;

    /** Button to add a new parameter. */
    private Button m_buttonAddParam;

    /** The button bar. */
    private HorizontalLayout m_buttonBar;

    /** Field to activate / deactivate the job. */
    private CheckBox m_fieldActive;

    /** Field for the cron expression. */
    private ComboBox m_fieldCron;

    /** Field for the encoding. */
    private TextField m_fieldEncoding;

    /** Field for the class name. */
    private ComboBox m_fieldJobClass;

    /** Field for the locale. */
    private TextField m_fieldLocale;

    /** Field for the project. */
    private TextField m_fieldProject;

    /** Field for the remote address. */
    private TextField m_fieldRemoteAddress;

    /** 'Reuse instance' check box. */
    private CheckBox m_fieldReuseInstance;

    /** Field for the site root. */
    private CmsPathSelectField m_fieldSiteRoot;

    /** Field for the URI. */
    private CmsPathSelectField m_fieldUri;

    /** Field for the user name. */
    private TextField m_fieldUser;

    /** Field group. */
    private BeanFieldGroup<CmsScheduledJobInfo> m_group;

    /** Edited job. */
    private CmsScheduledJobInfo m_job = new CmsScheduledJobInfo();

    /**
     * Creates a new instance.<p>
     *
     * @param job the job to be edited
     */
    public CmsJobEditView(CmsScheduledJobInfo job) {
        m_job = job;
        CmsVaadinUtils.readAndLocalizeDesign(this, CmsVaadinUtils.getWpMessagesForCurrentLocale(), null);

        BeanFieldGroup<CmsScheduledJobInfo> group = new BeanFieldGroup<CmsScheduledJobInfo>(CmsScheduledJobInfo.class);
        group.setItemDataSource(m_job);
        m_group = group;

        bindField(m_fieldJobName, "jobName");
        bindField(m_fieldJobClass, "className");
        bindField(m_fieldCron, "cronExpression");
        bindField(m_fieldReuseInstance, "reuseInstance");
        bindField(m_fieldActive, "active");
        bindField(m_fieldUser, "contextInfo.userName");
        bindField(m_fieldProject, "contextInfo.projectName");
        bindField(m_fieldSiteRoot, "contextInfo.siteRoot");
        bindField(m_fieldUri, "contextInfo.requestedUri");
        bindField(m_fieldLocale, "contextInfo.localeName");
        bindField(m_fieldEncoding, "contextInfo.encoding");
        bindField(m_fieldRemoteAddress, "contextInfo.remoteAddr");

        m_fieldJobName.setConverter(new CmsNullToEmptyConverter());
        m_fieldJobClass.setConverter(new CmsComboNullToEmptyConverter());
        m_fieldCron.setConverter(new CmsComboNullToEmptyConverter());

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

        if (!CmsStringUtil.isEmptyOrWhitespaceOnly(m_job.getClassName())) {
            m_fieldJobClass.addItem(m_job.getClassName());
        }

        if (!CmsStringUtil.isEmptyOrWhitespaceOnly(m_job.getCronExpression())) {
            m_fieldCron.addItem(m_job.getCronExpression());
        }

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

        // all other fields already populated by field group, we still need to handle the parameters

        for (Map.Entry<String, String> entry : info.getParameters().entrySet()) {
            addParamLine(entry.getKey(), entry.getValue());
        }
    }

    /**
     * Sets the buttons for the edit view.<p>
     *
     * @param buttons the buttons
     */
    public void setButtons(Component... buttons) {

        m_buttonBar.removeAllComponents();
        for (Component button : buttons) {
            m_buttonBar.addComponent(button);
        }
    }

    /**
     * Try to save the form values to the edited bean.<p>
     *
     * @return true if setting the information was successful
     */
    public boolean trySaveToBean() {

        try {
            m_group.commit();
        } catch (Exception e) {
            LOG.info(e.getLocalizedMessage(), e);
            return false;
        }
        m_job.setParameters(readParams());
        return true;
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
     * Binds the given component to the given bean property.<p>
     *
     * @param field the component
     * @param property the bean property
     */
    void bindField(AbstractField<?> field, String property) {

        m_group.bind(field, property);

        field.setCaption(CmsVaadinUtils.getMessageText("label." + property));
        field.setDescription(CmsVaadinUtils.getMessageText("label." + property + ".help"));
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
}
