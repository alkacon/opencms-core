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

import org.opencms.file.CmsObject;
import org.opencms.file.CmsResource;
import org.opencms.file.CmsResourceFilter;
import org.opencms.file.CmsVfsResourceNotFoundException;
import org.opencms.importexport.CmsExportParameters;
import org.opencms.importexport.CmsVfsImportExportHandler;
import org.opencms.main.CmsException;
import org.opencms.main.CmsLog;
import org.opencms.main.OpenCms;
import org.opencms.module.CmsModule.ExportMode;
import org.opencms.report.A_CmsReportThread;
import org.opencms.ui.A_CmsUI;
import org.opencms.ui.CmsVaadinUtils;
import org.opencms.ui.apps.Messages;
import org.opencms.ui.components.CmsBasicDialog;
import org.opencms.ui.components.CmsBasicDialog.DialogWidth;
import org.opencms.ui.components.CmsDateField;
import org.opencms.ui.components.editablegroup.CmsEditableGroup;
import org.opencms.ui.components.editablegroup.I_CmsEditableGroupRow;
import org.opencms.ui.components.fileselect.CmsPathSelectField;
import org.opencms.util.CmsStringUtil;
import org.opencms.util.CmsUUID;
import org.opencms.workplace.threads.CmsExportThread;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.logging.Log;

import com.google.common.base.Supplier;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.Component;
import com.vaadin.ui.FormLayout;
import com.vaadin.ui.Window;
import com.vaadin.v7.data.Property.ValueChangeEvent;
import com.vaadin.v7.data.Property.ValueChangeListener;
import com.vaadin.v7.data.Validator;
import com.vaadin.v7.data.util.IndexedContainer;
import com.vaadin.v7.shared.ui.combobox.FilteringMode;
import com.vaadin.v7.ui.AbstractSelect.ItemCaptionMode;
import com.vaadin.v7.ui.CheckBox;
import com.vaadin.v7.ui.ComboBox;
import com.vaadin.v7.ui.VerticalLayout;

/**
 * Class for the Export dialog.<p>
 */
public class CmsDbExportView extends VerticalLayout {

    /**
     * Validator for entered resources.<p>
     */
    class ResourceValidator implements Validator {

        /**vaadin serial id.*/
        private static final long serialVersionUID = -4341247963641286345L;

        /**
         * @see com.vaadin.data.Validator#validate(java.lang.Object)
         */
        public void validate(Object value) throws InvalidValueException {

            String resourcePath = (String)value;
            if ((value == null)) {
                throw new InvalidValueException(
                    CmsVaadinUtils.getMessageText(Messages.GUI_DATABASEAPP_EXPORT_INVALID_RESOURCE_EMPTY_0));
            }

            if (resourcePath.isEmpty()) {
                throw new InvalidValueException(
                    CmsVaadinUtils.getMessageText(Messages.GUI_DATABASEAPP_EXPORT_INVALID_RESOURCE_EMPTY_0));
            }

            if (!m_cms.existsResource(resourcePath, CmsResourceFilter.IGNORE_EXPIRATION)) {
                throw new InvalidValueException(
                    CmsVaadinUtils.getMessageText(Messages.GUI_DATABASEAPP_EXPORT_INVALID_RESOURCE_NOTFOUND_0));
            }
        }
    }

    /**
     * Validator for the target field.<p>
     */
    class TargetValidator implements Validator {

        /**vaadin serial id.*/
        private static final long serialVersionUID = 7530400504930612299L;

        /**
         * @see com.vaadin.data.Validator#validate(java.lang.Object)
         */
        public void validate(Object value) throws InvalidValueException {

            if (value == null) {
                throw new InvalidValueException(
                    CmsVaadinUtils.getMessageText(Messages.GUI_DATABASEAPP_EXPORT_INVALID_TARGET_0));
            }
        }
    }

    /** The logger for this class. */
    private static final Log LOG = CmsLog.getLog(CmsDbExportView.class.getName());

    /**vaadin serial id.*/
    private static final long serialVersionUID = -2571459807662862053L;

    /**Copy of current CmsObject.*/
    protected CmsObject m_cms;

    /** Button to add a list of resources through a text area. */
    private Button m_addResources;

    /**vaadin component.*/
    private CheckBox m_asFiles;

    /**vaadin component.*/
    private CmsDateField m_changedSince;

    /** The export parameters object that is edited on this dialog. */
    private CmsExportParameters m_exportParams;

    /**vaadin component.*/
    private CheckBox m_includeAccount;

    /**vaadin component.*/
    private CheckBox m_includeProject;

    /**vaadin component.*/
    private CheckBox m_includeResource;

    /**vaadin component.*/
    private CheckBox m_includeSystem;

    /**vaadin component.*/
    private CheckBox m_includeUnchanged;

    /**vaadin component.*/
    private CheckBox m_modified;

    /**vaadin component.*/
    private Button m_ok;

    private ComboBox m_project;

    /**vaadin component.*/
    private CheckBox m_recursive;

    /**Vaadin component. */
    private CheckBox m_reducedMetadata;

    /**vaadin component.*/
    private VerticalLayout m_resources;

    private CmsEditableGroup m_resourcesGroup;

    /**vaadin component.*/
    private ComboBox m_site;

    /**vaadin component.*/
    private CheckBox m_skipParentFolders;

    /**vaadin component.*/
    private ComboBox m_target;

    /**
     * public constructor.<p>
     */
    public CmsDbExportView() {

        CmsVaadinUtils.readAndLocalizeDesign(this, CmsVaadinUtils.getWpMessagesForCurrentLocale(), null);
        setHeightUndefined();
        try {
            m_cms = OpenCms.initCmsObject(A_CmsUI.getCmsObject());
        } catch (CmsException e) {
            LOG.error("Failed to clone CmsObject", e);
        }

        m_resourcesGroup = new CmsEditableGroup(m_resources, new Supplier<Component>() {

            public Component get() {

                return getResourceRow("");

            }

        }, CmsVaadinUtils.getMessageText(Messages.GUI_DATABASEAPP_EXPORT_ADD_RESOURCE_0));
        m_resourcesGroup.init();
        m_resourcesGroup.addRow(getResourceRow(""));
        m_exportParams = new CmsExportParameters();

        setupCheckBoxes();
        setupComboBoxFile();
        setupComboBoxSite();

        m_ok.addClickListener(new ClickListener() {

            private static final long serialVersionUID = -4224924796312615674L;

            public void buttonClick(ClickEvent event) {

                addResourceIfEmpty();
                addValidators();
                if (isFormValid()) {
                    startThread();
                }
            }
        });
        m_addResources.addClickListener(e -> openAddResourcesDialog());
    }

    protected void addResourceIfEmpty() {

        if (m_resourcesGroup.getRows().size() == 0) {
            m_resourcesGroup.addRow(getResourceRow(""));
        }
    }

    /**
     * Adds all validators to the formular.<p>
     */
    protected void addValidators() {

        //Target file ComboBox
        m_target.removeAllValidators();
        m_target.addValidator(new TargetValidator());

        for (I_CmsEditableGroupRow row : m_resourcesGroup.getRows()) {
            FormLayout layout = (FormLayout)(row.getComponent());
            CmsPathSelectField field = (CmsPathSelectField)layout.getComponent(0);
            field.removeAllValidators();
            field.addValidator(new ResourceValidator());
        }
    }

    /**
     * Changes the site of the cms object.<p>
     */
    protected void changeSite() {

        m_cms.getRequestContext().setSiteRoot((String)m_site.getValue());
    }

    protected Component getResourceRow(String path) {

        FormLayout res = new FormLayout();
        CmsPathSelectField field = new CmsPathSelectField();
        if (!CmsStringUtil.isEmptyOrWhitespaceOnly(path)) {
            field.setValue(path);
        }
        field.setCaption(CmsVaadinUtils.getMessageText(Messages.GUI_DATABASEAPP_EXPORT_RESOURCES_0));
        field.setDescription(CmsVaadinUtils.getMessageText(Messages.GUI_DATABASEAPP_EXPORT_RESOURCES_HELP_0));
        field.setCmsObject(m_cms);
        res.addComponent(field);
        return res;
    }

    /**
     * Checks if form is valid.<p>
     *
     * @return true if all fields are valid
     */
    protected boolean isFormValid() {

        return m_target.isValid() & allResourcesValid();
    }

    /**
     * Checks if resources exist in site. if not the row gets removed.<p>
     */
    protected void removeUnvalidPathFields() {

        int counter = 0;
        List<I_CmsEditableGroupRow> rowsToRemove = new ArrayList<I_CmsEditableGroupRow>();
        for (I_CmsEditableGroupRow row : m_resourcesGroup.getRows()) {
            FormLayout layout = (FormLayout)(row.getComponent());
            CmsPathSelectField field = (CmsPathSelectField)layout.getComponent(0);
            if (!m_cms.existsResource(field.getValue(), CmsResourceFilter.IGNORE_EXPIRATION)) {
                rowsToRemove.add(row);
            }
        }

        for (I_CmsEditableGroupRow row : rowsToRemove) {
            m_resourcesGroup.remove(row);
        }
    }

    /**
     * Starts the export thread and displays it's report.<p>
     */
    protected void startThread() {

        try {
            m_cms.getRequestContext().setCurrentProject(m_cms.readProject((CmsUUID)m_project.getValue()));
        } catch (CmsException e) {
            LOG.error("Unable to set project", e);
        }
        updateExportParams();

        CmsVfsImportExportHandler handler = new CmsVfsImportExportHandler();
        handler.setExportParams(m_exportParams);
        A_CmsReportThread exportThread = new CmsExportThread(m_cms, handler, false);

        Window window = CmsBasicDialog.prepareWindow(DialogWidth.max);
        window.setContent(new CmsExportThreadDialog(handler, exportThread, window));
        A_CmsUI.get().addWindow(window);
        exportThread.start();
    }

    /**
     * Checks if all resources are valid.<p>
     *
     * @return true if resources are valid
     */
    private boolean allResourcesValid() {

        boolean valid = true;

        for (I_CmsEditableGroupRow row : m_resourcesGroup.getRows()) {
            FormLayout layout = (FormLayout)(row.getComponent());
            CmsPathSelectField field = (CmsPathSelectField)layout.getComponent(0);
            if (!field.isValid()) {
                valid = false;
            }
        }

        return valid;
    }

    /**
     * Reads out resources from form.<p>
     *
     * @return List with site-relative paths of resources
     */
    private List<String> getResources() {

        List<String> res = new ArrayList<String>();

        for (I_CmsEditableGroupRow row : m_resourcesGroup.getRows()) {
            FormLayout layout = (FormLayout)(row.getComponent());
            CmsPathSelectField field = (CmsPathSelectField)layout.getComponent(0);
            String value = field.getValue();
            if (!value.isEmpty()) {
                if (!value.endsWith("/")) {
                    try {
                        CmsResource resource = m_cms.readResource(value, CmsResourceFilter.IGNORE_EXPIRATION);
                        if (resource.isFolder()) {
                            value = value + "/";
                        }
                    } catch (CmsException e) {
                        if (!(e instanceof CmsVfsResourceNotFoundException)) {
                            LOG.error(e.getLocalizedMessage());
                        }
                    }
                }
                if (!res.contains(value)) {
                    res.add(value);
                }
            }
        }

        return res;
    }

    /**
     * Opens the dialog for adding new resources via a text area.
     */
    private void openAddResourcesDialog() {

        Window window = CmsBasicDialog.prepareWindow(DialogWidth.wide);
        window.setCaption(CmsVaadinUtils.getMessageText(Messages.GUI_DATABASEAPP_OPEN_ADD_RESOURCES_DIALOG_0));
        window.setContent(new CmsAddExportResourcesDialog(result -> updateExportResources(result)));
        A_CmsUI.get().addWindow(window);
    }

    /**
     * Sets the init values for check boxes.<p>
     */
    private void setupCheckBoxes() {

        m_includeResource.setValue(Boolean.valueOf(true));
        m_includeUnchanged.setValue(Boolean.valueOf(true));
        m_includeSystem.setValue(Boolean.valueOf(true));
        m_recursive.setValue(Boolean.valueOf(true));
    }

    /**
     * Sets up the combo box for the target file.<p>
     */
    private void setupComboBoxFile() {

        m_target.setInputPrompt(CmsVaadinUtils.getMessageText(Messages.GUI_DATABASEAPP_EXPORT_FILE_NAME_EMPTY_0));
        m_target.setNewItemsAllowed(true);
        List<String> files = CmsDbManager.getFileListFromServer(true);
        for (String file : files) {
            m_target.addItem(file);
        }
    }

    /**
     * Sets up the combo box for the site choice.<p>
     */
    private void setupComboBoxSite() {

        IndexedContainer container = CmsVaadinUtils.getAvailableSitesContainer(A_CmsUI.getCmsObject(), "title");
        m_site.setContainerDataSource(container);
        m_site.setItemCaptionMode(ItemCaptionMode.PROPERTY);
        m_site.setItemCaptionPropertyId("title");
        m_site.setFilteringMode(FilteringMode.CONTAINS);
        m_site.setNullSelectionAllowed(false);
        m_site.setValue(A_CmsUI.getCmsObject().getRequestContext().getSiteRoot());
        m_site.addValueChangeListener(new ValueChangeListener() {

            private static final long serialVersionUID = -1019243885633462477L;

            public void valueChange(ValueChangeEvent event) {

                changeSite();
                removeUnvalidPathFields();
            }
        });

        m_project.setContainerDataSource(CmsVaadinUtils.getProjectsContainer(A_CmsUI.getCmsObject(), "caption"));
        m_project.setItemCaptionPropertyId("caption");
        m_project.select(A_CmsUI.getCmsObject().getRequestContext().getCurrentProject().getUuid());
        m_project.setNewItemsAllowed(false);
        m_project.setNullSelectionAllowed(false);
        m_project.setTextInputAllowed(false);
    }

    /**
     * Updates the Export parameter based on user input.<p>
     */
    private void updateExportParams() {

        m_exportParams.setExportAccountData(m_includeAccount.getValue().booleanValue());
        m_exportParams.setExportAsFiles(m_asFiles.getValue().booleanValue());
        m_exportParams.setExportProjectData(m_includeProject.getValue().booleanValue());
        m_exportParams.setExportResourceData(m_includeResource.getValue().booleanValue());
        m_exportParams.setInProject(m_modified.getValue().booleanValue());
        m_exportParams.setIncludeSystemFolder(m_includeSystem.getValue().booleanValue());
        m_exportParams.setIncludeUnchangedResources(m_includeUnchanged.getValue().booleanValue());
        m_exportParams.setSkipParentFolders(m_skipParentFolders.getValue().booleanValue());
        String exportFileName = OpenCms.getSystemInfo().getAbsoluteRfsPathRelativeToWebInf(
            OpenCms.getSystemInfo().getPackagesRfsPath() + File.separator + (String)m_target.getValue());
        m_exportParams.setPath(exportFileName);
        m_exportParams.setRecursive(m_recursive.getValue().booleanValue());
        m_exportParams.setResources(getResources());
        ExportMode exportMode = m_reducedMetadata.getValue().booleanValue() ? ExportMode.REDUCED : ExportMode.DEFAULT;
        m_exportParams.setExportMode(exportMode);
        if (m_changedSince.getValue() != null) {
            m_exportParams.setContentAge(m_changedSince.getDate().getTime());
        } else {
            m_exportParams.setContentAge(0);
        }
    }

    /**
     * Updates the export resource fields from a newline-separated list of paths.
     *
     * @param resourceListing a newline-separated list of paths
     */
    private void updateExportResources(String resourceListing) {

        List<String> exResources = getResources();
        List<String> lines = Arrays.stream(resourceListing.trim().split("\n")).filter(
            r -> !CmsStringUtil.isEmptyOrWhitespaceOnly(r)).map(r -> r.trim()).collect(Collectors.toList());

        if ((exResources.size() == 0) && (lines.size() > 0)) {
            // We have paths to add from the resource listing, but only empty fields in the form,
            // so remove the existing form fields first, because they aren't needed anymore and would
            // cause validation errors when clicking OK because they're empty.

            m_resourcesGroup.init();
        }

        for (String line : lines) {
            if (!exResources.contains(line)) {
                // folders may have been entered without trailing slashes,
                // but to correct that, we have to read the resources
                try {
                    CmsResource res = m_cms.readResource(line, CmsResourceFilter.IGNORE_EXPIRATION);
                    line = m_cms.getSitePath(res);
                } catch (CmsException e) {
                    LOG.debug(e.getLocalizedMessage(), e);
                }
                m_resourcesGroup.addRow(getResourceRow(line));
            }
        }

    }

}
