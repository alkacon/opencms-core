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

package org.opencms.ui.apps.projects;

import org.opencms.file.CmsObject;
import org.opencms.file.CmsProject;
import org.opencms.file.CmsResource;
import org.opencms.main.CmsException;
import org.opencms.main.CmsLog;
import org.opencms.main.OpenCms;
import org.opencms.security.CmsOrganizationalUnit;
import org.opencms.security.CmsRole;
import org.opencms.ui.A_CmsUI;
import org.opencms.ui.CmsCssIcon;
import org.opencms.ui.CmsVaadinUtils;
import org.opencms.ui.apps.Messages;
import org.opencms.ui.components.CmsBasicDialog;
import org.opencms.ui.components.CmsErrorDialog;
import org.opencms.ui.components.CmsRemovableFormRow;
import org.opencms.ui.components.CmsResourceInfo;
import org.opencms.ui.components.OpenCmsTheme;
import org.opencms.ui.components.fileselect.CmsPathSelectField;
import org.opencms.ui.dialogs.permissions.CmsPrincipalSelect;
import org.opencms.ui.dialogs.permissions.CmsPrincipalSelect.WidgetType;
import org.opencms.util.CmsStringUtil;
import org.opencms.util.CmsUUID;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.logging.Log;

import com.vaadin.server.UserError;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.Component;
import com.vaadin.ui.FormLayout;
import com.vaadin.ui.UI;
import com.vaadin.ui.Window;
import com.vaadin.v7.data.Property.ValueChangeEvent;
import com.vaadin.v7.data.Property.ValueChangeListener;
import com.vaadin.v7.data.Validator;
import com.vaadin.v7.data.Validator.InvalidValueException;
import com.vaadin.v7.ui.CheckBox;
import com.vaadin.v7.ui.Field;
import com.vaadin.v7.ui.TextField;

/**
 * The edit project form component.<p>
 */
public class CmsEditProjectForm extends CmsBasicDialog {

    /**
     * The OU validator.<p>
     */
    protected class OUValidator implements Validator {

        /** The serial version id. */
        private static final long serialVersionUID = 1L;

        /**
         * @see com.vaadin.data.Validator#validate(java.lang.Object)
         */
        public void validate(Object value) throws InvalidValueException {

            if (m_fieldOU.isEnabled() && CmsStringUtil.isNotEmptyOrWhitespaceOnly((String)value)) {
                try {
                    OpenCms.getOrgUnitManager().readOrganizationalUnit(A_CmsUI.getCmsObject(), (String)value);
                } catch (CmsException e) {
                    throw new InvalidValueException(e.getLocalizedMessage(UI.getCurrent().getLocale()));
                }
            }
        }
    }

    /**
     * The resource field validator. Checks whether the resource is part of the project OU.<p>
     */
    protected class ResourceValidator implements Validator {

        /** The serial version id. */
        private static final long serialVersionUID = 1L;

        /**
         * @see com.vaadin.data.Validator#validate(java.lang.Object)
         */
        public void validate(Object value) throws InvalidValueException {

            if (CmsStringUtil.isNotEmptyOrWhitespaceOnly((String)value)
                && CmsStringUtil.isNotEmptyOrWhitespaceOnly(m_fieldOU.getValue())) {
                try {
                    List<CmsResource> ouRes = OpenCms.getOrgUnitManager().getResourcesForOrganizationalUnit(
                        A_CmsUI.getCmsObject(),
                        m_fieldOU.getValue());

                    String resPath = (String)value;
                    for (CmsResource res : ouRes) {
                        if (resPath.startsWith(res.getRootPath())) {
                            return;
                        }
                    }
                    throw new InvalidValueException(
                        "The resource path "
                            + value
                            + " is not part of the project OU '"
                            + m_fieldOU.getValue()
                            + "'.");
                } catch (CmsException e) {
                    // ignore
                }
            }
        }
    }

    /** The logger for this class. */
    private static Log LOG = CmsLog.getLog(CmsEditProjectForm.class.getName());

    /** The serial version id. */
    private static final long serialVersionUID = 2345799706922671537L;

    /** The OU field. */
    TextField m_fieldOU;

    /** The add resources button. */
    private Button m_addResource;

    /** The cancel button. */
    private Button m_cancel;

    /** The delete after publish check box. */
    private CheckBox m_fieldDeleteAfterPublish;

    /** The project description field. */
    private TextField m_fieldDescription;

    /** The manager group field. */
    private CmsPrincipalSelect m_fieldManager;

    /** The project name field. */
    private TextField m_fieldName;

    /** The form fileds. */
    private Field<?>[] m_fields;

    /** The user group field. */
    private CmsPrincipalSelect m_fieldUser;

    /** The projects table instance. */
    private CmsProjectsTable m_table;

    /** The OK button. */
    private Button m_ok;

    /** The edited project. */
    private CmsProject m_project;

    /** The resources form layout. */
    private FormLayout m_resources;

    /** The resource field validator. */
    private ResourceValidator m_resourceValidator;

    /** The window this form is displayed in. */
    private Window m_window;

    /**
     * Constructor.<p>
     * Used to edit existing projects.<p>
     *
     * @param table the projects table
     * @param projectId the project to edit
     * @param window the window this form is displayed in
     */
    public CmsEditProjectForm(CmsProjectsTable table, CmsUUID projectId, Window window) {

        this(table, window);
        CmsObject cms = A_CmsUI.getCmsObject();
        try {
            m_project = cms.readProject(projectId);
            displayResourceInfoDirectly(
                Collections.singletonList(
                    new CmsResourceInfo(
                        m_project.getName(),
                        m_project.getDescription(),
                        new CmsCssIcon(OpenCmsTheme.ICON_PROJECT))));
            m_fieldName.setValue(m_project.getName());
            m_fieldName.setEnabled(false);
            m_fieldDescription.setValue(m_project.getDescription());
            m_fieldUser.setValue(cms.readGroup(m_project.getGroupId()).getName());
            m_fieldManager.setValue(cms.readGroup(m_project.getManagerGroupId()).getName());
            try {
                CmsOrganizationalUnit ou;
                ou = OpenCms.getOrgUnitManager().readOrganizationalUnit(cms, m_project.getOuFqn());
                m_fieldOU.setValue(ou.getDisplayName(UI.getCurrent().getLocale()));
            } catch (CmsException e) {
                LOG.error(e.getLocalizedMessage(), e);
                m_fieldOU.setValue(null);
            }
            m_fieldOU.setEnabled(false);
            for (String resName : cms.readProjectResources(m_project)) {
                addResourceField(resName);
            }
        } catch (CmsException e) {
            CmsErrorDialog.showErrorDialog(e);
        }

    }

    /**
     * Constructor.<p>
     * Use this to create a new project.<p>
     *
     * @param table the projects table
     * @param window the window this form is displayed in
     */
    public CmsEditProjectForm(CmsProjectsTable table, Window window) {

        m_window = window;
        m_table = table;
        CmsVaadinUtils.readAndLocalizeDesign(this, CmsVaadinUtils.getWpMessagesForCurrentLocale(), null);
        m_resourceValidator = new ResourceValidator();
        m_fieldManager.setWidgetType(WidgetType.groupwidget);
        m_fieldManager.setRealPrincipalsOnly(true);
        m_fieldUser.setWidgetType(WidgetType.groupwidget);
        m_fieldUser.setRealPrincipalsOnly(true);

        try {
            CmsOrganizationalUnit ou = OpenCms.getRoleManager().getOrgUnitsForRole(
                A_CmsUI.getCmsObject(),
                CmsRole.PROJECT_MANAGER,
                true).get(0);
            m_fieldOU.setValue(ou.getName());
        } catch (CmsException e) {
            LOG.error(e.getLocalizedMessage(), e);
            m_fieldOU.setValue(null);
        }
        m_fieldOU.setImmediate(true);
        m_fieldOU.addValidator(new OUValidator());
        if (m_project == null) {
            m_fieldName.addValidator(new Validator() {

                public void validate(Object value) throws InvalidValueException {
                    m_fieldName.setComponentError(null);
                    CmsObject cms = A_CmsUI.getCmsObject();
                    String projectName = CmsStringUtil.joinPaths(m_fieldOU.getValue(), m_fieldName.getValue());
                    try {
                        cms.readProject(projectName);
                        throw new InvalidValueException(CmsVaadinUtils.getMessageText(Messages.GUI_PROJECTS_PROJECT_WITH_SAME_NAME_EXISTS_0));

                    } catch (CmsException e) {
                        // ignore
                    }
                }
            });
        }
        m_fieldOU.addValueChangeListener(new ValueChangeListener() {

            private static final long serialVersionUID = 1L;

            public void valueChange(ValueChangeEvent event) {

                if (m_project == null) {
                    // the 'project exists' validation is attached to m_fieldName, but is also dependent on the value of the OU field,
                    // so we trigger it here
                    try {
                        m_fieldName.setComponentError(null);
                        m_fieldName.validate();
                    } catch (InvalidValueException e) {
                        m_fieldName.setComponentError(new UserError(e.getMessage()));
                    }
                }
                validateResourceFields();
            }
        });

        m_addResource.addClickListener(new ClickListener() {

            private static final long serialVersionUID = 1L;

            public void buttonClick(ClickEvent event) {

                addResourceField(null);
            }
        });
        m_ok.addClickListener(new ClickListener() {

            private static final long serialVersionUID = 1L;

            public void buttonClick(ClickEvent event) {

                submit();
            }
        });
        m_cancel.addClickListener(new ClickListener() {

            private static final long serialVersionUID = 1L;

            public void buttonClick(ClickEvent event) {

                cancel();
            }
        });
        m_fields = new Field<?>[] {m_fieldName, m_fieldDescription, m_fieldManager, m_fieldUser, m_fieldOU};
    }

    /**
     * Adds a new resource field.<p>
     *
     * @param value the value to set
     */
    void addResourceField(String value) {

        CmsPathSelectField field = new CmsPathSelectField();
        field.setUseRootPaths(true);
        if (value != null) {
            field.setValue(value);
        }
        field.addValidator(m_resourceValidator);
        CmsRemovableFormRow<CmsPathSelectField> row = new CmsRemovableFormRow<CmsPathSelectField>(
            field,
            CmsVaadinUtils.getMessageText(Messages.GUI_PROJECTS_REMOVE_RESOURCE_0));
        row.setCaption(CmsVaadinUtils.getMessageText(Messages.GUI_PROJECTS_RESOURCE_0));
        row.setDescription(CmsVaadinUtils.getMessageText(Messages.GUI_PROJECTS_RESOURCE_HELP_0));
        m_resources.addComponent(row);
    }

    /**
     * Cancels project edit.<p>
     */
    void cancel() {

        m_window.close();
    }

    /**
     * Submits the form.<p>
     */
    void submit() {

        if (isValid()) {
            if (m_project == null) {
                createProject();
            } else {
                saveProject();
            }
            m_table.loadProjects();
            m_window.close();
        }
    }

    /**
     * Validates the resource fields.<p>
     */
    @SuppressWarnings("unchecked")
    void validateResourceFields() {

        for (Component c : m_resources) {
            if (c instanceof CmsRemovableFormRow<?>) {
                ((CmsRemovableFormRow<CmsPathSelectField>)c).getInput().validate();
            }
        }
    }

    /**
     * Creates a new project.<p>
     */
    private void createProject() {

        CmsObject cms = A_CmsUI.getCmsObject();
        try {
            String name = "/";
            if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(m_fieldOU.getValue())) {
                name = CmsStringUtil.joinPaths(name, m_fieldOU.getValue());
            }
            if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(m_fieldName.getValue())) {
                name = CmsStringUtil.joinPaths(name, m_fieldName.getValue());
            } else {
                name = CmsStringUtil.joinPaths(name, "/");
            }

            m_project = cms.createProject(
                name,
                m_fieldDescription.getValue(),
                m_fieldUser.getValue(),
                m_fieldManager.getValue(),
                m_fieldDeleteAfterPublish.getValue().booleanValue()
                ? CmsProject.PROJECT_TYPE_TEMPORARY
                : CmsProject.PROJECT_TYPE_NORMAL);
            updateProjectResources();

        } catch (Throwable t) {
            CmsErrorDialog.showErrorDialog(t);
        }
    }

    /**
     * Returns the selected resource paths.<p>
     *
     * @return the resource paths
     */
    @SuppressWarnings("unchecked")
    private Set<String> getResourcePaths() {

        Set<String> resources = new HashSet<String>();
        for (Component c : m_resources) {
            if (c instanceof CmsRemovableFormRow<?>) {
                String value = ((CmsRemovableFormRow<CmsPathSelectField>)c).getInput().getValue();
                if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(value)) {
                    resources.add(value);
                }
            }
        }
        return resources;
    }

    /**
     * Validates the form fields.<p>
     *
     * @return <code>true</code> in case all fields are valid
     */
    @SuppressWarnings("unchecked")
    private boolean isValid() {

        for (Field<?> field : m_fields) {
            if (!field.isValid()) {
                field.focus();

                return false;
            }
        }
        for (Component c : m_resources) {
            if (c instanceof CmsRemovableFormRow<?>) {
                if (!((CmsRemovableFormRow<CmsPathSelectField>)c).getInput().isValid()) {
                    ((CmsRemovableFormRow<CmsPathSelectField>)c).getInput().focus();
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * Saves an existing project.<p>
     */
    private void saveProject() {

        CmsObject cms = A_CmsUI.getCmsObject();
        try {
            m_project.setDescription(m_fieldDescription.getValue());
            m_project.setGroupId(cms.readGroup(m_fieldUser.getValue()).getId());
            m_project.setManagerGroupId(cms.readGroup(m_fieldManager.getValue()).getId());
            m_project.setDeleteAfterPublishing(m_fieldDeleteAfterPublish.getValue().booleanValue());
            cms.writeProject(m_project);
            updateProjectResources();
        } catch (Throwable t) {
            CmsErrorDialog.showErrorDialog(t);
        }
    }

    /**
     * Updates the project resources.<p>
     *
     * @throws CmsException in case writing the project fails
     */
    private void updateProjectResources() throws CmsException {

        CmsObject cms = A_CmsUI.getCmsObject();
        Set<String> resourceRootPaths = getResourcePaths();
        // write the edited project resources
        CmsProject currentProject = cms.getRequestContext().getCurrentProject();
        // change the current project
        cms.getRequestContext().setCurrentProject(m_project);
        // store the current site root
        String currentSite = cms.getRequestContext().getSiteRoot();
        // copy the resources to the current project
        try {
            // switch to the root site
            cms.getRequestContext().setSiteRoot("");
            // remove deleted resources
            for (String resName : cms.readProjectResources(m_project)) {
                if (!resourceRootPaths.contains(resName)) {
                    cms.removeResourceFromProject(resName);
                }
            }
            // read project resources again!
            List<String> currentResNames = cms.readProjectResources(m_project);
            // copy missing resources
            for (String resName : resourceRootPaths) {
                if (!currentResNames.contains(resName)) {
                    cms.copyResourceToProject(resName);
                }
            }
        } finally {
            // switch back to current site and project
            cms.getRequestContext().setSiteRoot(currentSite);
            cms.getRequestContext().setCurrentProject(currentProject);
        }
    }
}
