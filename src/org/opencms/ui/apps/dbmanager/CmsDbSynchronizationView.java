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

package org.opencms.ui.apps.dbmanager;

import org.opencms.db.CmsUserSettings;
import org.opencms.file.CmsObject;
import org.opencms.main.CmsException;
import org.opencms.main.CmsLog;
import org.opencms.main.OpenCms;
import org.opencms.synchronize.CmsSynchronizeSettings;
import org.opencms.ui.A_CmsUI;
import org.opencms.ui.CmsVaadinUtils;
import org.opencms.ui.apps.Messages;
import org.opencms.ui.components.CmsRemovableFormRow;
import org.opencms.ui.components.fileselect.CmsPathSelectField;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.logging.Log;

import com.vaadin.v7.data.Property.ValueChangeEvent;
import com.vaadin.v7.data.Property.ValueChangeListener;
import com.vaadin.v7.data.Validator;
import com.vaadin.v7.ui.AbstractField;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.v7.ui.CheckBox;
import com.vaadin.ui.Component;
import com.vaadin.ui.FormLayout;
import com.vaadin.v7.ui.TextField;
import com.vaadin.v7.ui.VerticalLayout;

/**
 * Synchronization layout class.<p>
 */
public class CmsDbSynchronizationView extends VerticalLayout {

    /**
     * Validator for the resource fields.<p>
     */
    class ResourceValidator implements Validator {

        /**vaadin serial id.*/
        private static final long serialVersionUID = -1513193444185009615L;

        /**
         * @see com.vaadin.data.Validator#validate(java.lang.Object)
         */
        public void validate(Object value) throws InvalidValueException {

            String resourceName = (String)value;
            if (synchroEnabled() & (resourceName == null)) {
                throw new InvalidValueException(
                    CmsVaadinUtils.getMessageText(Messages.GUI_DATABASEAPP_SYNC_VALIDATION_EMPTY_RESOURCES_0));
            }

            try {
                m_cms.readResource(resourceName);
            } catch (CmsException e) {
                throw new InvalidValueException(
                    CmsVaadinUtils.getMessageText(
                        Messages.GUI_DATABASEAPP_SYNC_VALIDATION_RESOURCE_NOT_FOUND_1,
                        resourceName));
            }
        }
    }

    /**
     * Validator for the target.<p>
     */
    class TargetValidator implements Validator {

        /**vaadin serial id.*/
        private static final long serialVersionUID = 4404089964412111711L;

        /**
         * @see com.vaadin.data.Validator#validate(java.lang.Object)
         */
        public void validate(Object value) throws InvalidValueException {

            String val = (String)value;
            if (synchroEnabled() & val.isEmpty()) {
                throw new InvalidValueException(
                    CmsVaadinUtils.getMessageText(Messages.GUI_DATABASEAPP_SYNC_VALIDATION_EMPTY_TARGET_0));
            }
            if (synchroEnabled()) {
                try {
                    m_synchronizeSettings.setDestinationPathInRfs(val);
                } catch (Throwable t) {
                    throw new InvalidValueException(
                        CmsVaadinUtils.getMessageText(Messages.GUI_DATABASEAPP_SYNC_VALIDATION_EMPTY_TARGET_0));
                }
            }
        }

    }

    /**vaadin serial id.*/
    private static final long serialVersionUID = -794938118093564562L;

    /** The logger for this class. */
    static Log LOG = CmsLog.getLog(CmsDbSynchronizationView.class.getName());

    /** The synchronize settings which are edited on this dialog. */
    CmsSynchronizeSettings m_synchronizeSettings;

    /**Vaadin component.*/
    private Button m_addResource;

    /**Vaadin component.*/
    private Button m_ok;

    /**Vaadin component.*/
    private FormLayout m_resources;

    /**Root CmsObject. */
    protected CmsObject m_cms;

    /**Vaadin component.*/
    private TextField m_target;

    /**Vaadin component.*/
    private CheckBox m_enabled;

    /**Instance of calling app.*/
    private CmsDbSynchronizationApp m_app;

    /**Components which have to be validated.*/
    private Set<AbstractField<String>> m_componentsToValidate = new HashSet<AbstractField<String>>();

    /**
     * Public constructor.<p>
     * @param app instance of calling app
     */
    public CmsDbSynchronizationView(CmsDbSynchronizationApp app) {
        CmsVaadinUtils.readAndLocalizeDesign(this, CmsVaadinUtils.getWpMessagesForCurrentLocale(), null);
        m_app = app;
        m_componentsToValidate.add(m_target);
        m_cms = initCms(A_CmsUI.getCmsObject());
        CmsUserSettings userSettings = new CmsUserSettings(m_cms);

        m_synchronizeSettings = userSettings.getSynchronizeSettings();
        if (m_synchronizeSettings == null) {
            m_synchronizeSettings = new CmsSynchronizeSettings();
            m_synchronizeSettings.setEnabled(false);
        }

        m_app.setRefreshButton(m_synchronizeSettings.isEnabled());

        initUI();

        m_target.addValueChangeListener(new ValueChangeListener() {

            private static final long serialVersionUID = 5508413459469395463L;

            public void valueChange(ValueChangeEvent event) {

                setOkButtonEnabled(true);

            }
        });

        m_enabled.addValueChangeListener(new ValueChangeListener() {

            private static final long serialVersionUID = -5921001488632416603L;

            public void valueChange(ValueChangeEvent event) {

                setOkButtonEnabled(true);

            }
        });

        m_addResource.addClickListener(new Button.ClickListener() {

            private static final long serialVersionUID = 2169629308804189534L;

            public void buttonClick(ClickEvent event) {

                addResource("");
            }
        });

        m_ok.addClickListener(new Button.ClickListener() {

            private static final long serialVersionUID = -5083450266085163340L;

            public void buttonClick(ClickEvent event) {

                removeEmptyResourceFields();
                addValidators();
                if (isValid()) {
                    submit();
                    setOkButtonEnabled(false);
                }
            }
        });
    }

    /**
     * Adds a resource to form.<p>
     *
     * @param path of resource to add
     */
    protected void addResource(String path) {

        CmsPathSelectField field = new CmsPathSelectField();
        field.setCmsObject(m_cms);
        field.setUseRootPaths(true);
        field.setValue(path);
        field.addValueChangeListener(new ValueChangeListener() {

            private static final long serialVersionUID = 8646438704383992571L;

            public void valueChange(ValueChangeEvent event) {

                setOkButtonEnabled(true);

            }
        });
        m_componentsToValidate.add(field);
        CmsRemovableFormRow<CmsPathSelectField> row = new CmsRemovableFormRow<CmsPathSelectField>(
            field,
            CmsVaadinUtils.getMessageText(Messages.GUI_DATABASEAPP_SYNC_REMOVE_RESOURCE_0));
        row.setCaption(CmsVaadinUtils.getMessageText(Messages.GUI_DATABASEAPP_SYNC_RESOURCE_0));
        row.setRemoveRunnable(new Runnable() {

            public void run() {

                setOkButtonEnabled(true);
            }
        });
        m_resources.addComponent(row);
    }

    /**
     * Adds validators to the input fields.<p>
     */
    protected void addValidators() {

        m_target.removeAllValidators();
        m_target.addValidator(new TargetValidator());

        for (Component c : m_componentsToValidate) {
            if (c instanceof CmsPathSelectField) {
                ((CmsPathSelectField)c).removeAllValidators();
                ((CmsPathSelectField)c).addValidator(new ResourceValidator());
            }
        }
    }

    /**
     * Checks if synchronization is enabled.<p>
     *
     * @return true if synchronization is enabled, false otherwise
     */
    protected boolean isEnabledSych() {

        return m_synchronizeSettings.isEnabled();
    }

    /**
     * Checks if all fields are valid.<p>
     *
     * @return true if fields are valid
     */
    protected boolean isValid() {

        for (AbstractField<String> component : m_componentsToValidate) {
            if (!component.isValid()) {
                return false;
            }
        }
        return true;
    }

    /**
     * Removes empty resource fields.<p>
     */
    protected void removeEmptyResourceFields() {

        Set<Component> componentsToRemove = new HashSet<Component>();
        int count = 0;
        Component lastElement = null;

        for (Component c : m_resources) {
            if (c instanceof CmsRemovableFormRow<?>) {
                count++;
                AbstractField<?> field = (AbstractField<?>)((CmsRemovableFormRow)c).getInput();
                String value = (String)(field.getValue());
                if (value.isEmpty()) {
                    componentsToRemove.add(c);
                    lastElement = c;
                }
            }
        }

        //if all fields are empty -> keep one of them to attach validator to
        if (componentsToRemove.size() == count) {
            componentsToRemove.remove(lastElement);
        }

        if (count == 0) {
            addResource("");
        }

        for (Component c : componentsToRemove) {
            m_resources.removeComponent(c);
            m_componentsToValidate.remove(c);
        }
    }

    /**
     * En/ Disables the ok button.<p>
     *
     * @param enable boolean indicates if button should be enabled
     */
    protected void setOkButtonEnabled(boolean enable) {

        m_ok.setEnabled(enable);
    }

    /**
     * Submits the form.<p>
     */
    protected void submit() {

        m_synchronizeSettings.setEnabled(m_enabled.getValue().booleanValue());
        m_synchronizeSettings.setSourceListInVfs(getResourcePaths());
        try {
            // set the synchronize settings
            CmsUserSettings userSettings = new CmsUserSettings(A_CmsUI.getCmsObject());
            userSettings.setSynchronizeSettings(m_synchronizeSettings);
            userSettings.save(A_CmsUI.getCmsObject());
        } catch (CmsException e) {
            LOG.error("Unable to set synchro settings", e);
        }
        CmsUserSettings userSettings = new CmsUserSettings(A_CmsUI.getCmsObject());
        m_synchronizeSettings = userSettings.getSynchronizeSettings();
        m_app.setRefreshButton(m_synchronizeSettings.isEnabled());
    }

    /**
     * Is the enabled button clicked.<p>
     *
     * @return true, if synchro should be enabled
     */
    protected boolean synchroEnabled() {

        return m_enabled.getValue().booleanValue();
    }

    /**
     * Reads entered resources from form.<p>
     *
     * @return List of resource paths
     */
    private List<String> getResourcePaths() {

        List<String> res = new ArrayList<String>();
        for (Component c : m_resources) {
            if (c instanceof CmsRemovableFormRow<?>) {
                AbstractField<?> field = (AbstractField<?>)(((CmsRemovableFormRow)c).getInput());
                res.add((String)(field.getValue()));
            }
        }
        return res;
    }

    /**
     * Clones given CmsObject and switches to root site.<p>
     *
     * @param cms given CmsObject
     * @return cloned CmsObject
     */
    private CmsObject initCms(CmsObject cms) {

        try {
            cms = OpenCms.initCmsObject(cms);
        } catch (CmsException e) {
            LOG.error("Unable to init CmsObject");
        }
        cms.getRequestContext().setSiteRoot("");
        return cms;
    }

    /**
     * Initializes UI.<p>
     */
    private void initUI() {

        m_enabled.setValue(Boolean.valueOf(m_synchronizeSettings.isEnabled()));
        if (m_synchronizeSettings.getDestinationPathInRfs() != null) {
            m_target.setValue(m_synchronizeSettings.getDestinationPathInRfs());
        }
        List<String> resources = m_synchronizeSettings.getSourceListInVfs();
        for (String resource : resources) {
            addResource(resource);
        }
        setOkButtonEnabled(false);
    }
}
