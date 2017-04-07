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

package org.opencms.ui.apps.modules.edit;

import org.opencms.ade.galleries.CmsSiteSelectorOptionBuilder;
import org.opencms.ade.galleries.shared.CmsSiteSelectorOption;
import org.opencms.db.CmsExportPoint;
import org.opencms.db.CmsUserSettings;
import org.opencms.file.CmsObject;
import org.opencms.file.types.CmsResourceTypeFolder;
import org.opencms.file.types.I_CmsResourceType;
import org.opencms.main.CmsException;
import org.opencms.main.CmsLog;
import org.opencms.main.OpenCms;
import org.opencms.module.CmsModule;
import org.opencms.module.CmsModuleDependency;
import org.opencms.module.CmsModuleVersion;
import org.opencms.site.CmsSite;
import org.opencms.site.CmsSiteManagerImpl;
import org.opencms.ui.A_CmsUI;
import org.opencms.ui.CmsVaadinUtils;
import org.opencms.ui.apps.Messages;
import org.opencms.ui.apps.modules.CmsModuleApp;
import org.opencms.ui.components.CmsAutoItemCreatingComboBox;
import org.opencms.ui.components.CmsBasicDialog;
import org.opencms.ui.components.CmsErrorDialog;
import org.opencms.ui.components.CmsRemovableFormRow;
import org.opencms.ui.components.CmsResourceInfo;
import org.opencms.ui.util.CmsComponentField;
import org.opencms.ui.util.CmsNullToEmptyConverter;
import org.opencms.util.CmsStringUtil;
import org.opencms.workplace.CmsWorkplace;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.TreeMap;

import org.apache.commons.logging.Log;

import com.google.common.base.Predicate;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.vaadin.data.Item;
import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.Property.ValueChangeListener;
import com.vaadin.data.Validator;
import com.vaadin.data.fieldgroup.BeanFieldGroup;
import com.vaadin.data.fieldgroup.FieldGroup;
import com.vaadin.data.fieldgroup.FieldGroup.CommitException;
import com.vaadin.data.util.IndexedContainer;
import com.vaadin.ui.AbstractComponentContainer;
import com.vaadin.ui.AbstractField;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.CheckBox;
import com.vaadin.ui.Component;
import com.vaadin.ui.Field;
import com.vaadin.ui.FormLayout;
import com.vaadin.ui.TabSheet;
import com.vaadin.ui.TextArea;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;

/**
 * Form for editing a module.<p>
 */
public class CmsEditModuleForm extends CmsBasicDialog {

    /** CSS class. */
    public static final String COMPLEX_ROW = "o-module-complex-row";

    /** Dummy site root used to identify the 'none' select option in the module site selector. */
    public static final String ID_EMPTY_SITE = "!empty";

    /** Classes folder within the module. */
    public static final String PATH_CLASSES = "classes/";

    /** Elements folder within the module. */
    public static final String PATH_ELEMENTS = "elements/";

    /** The formatters folder within the module. */
    public static final String PATH_FORMATTERS = "formatters/";

    /** Lib folder within the module. */
    public static final String PATH_LIB = "lib/";

    /** Resources folder within the module. */
    public static final String PATH_RESOURCES = "resources/";

    /** Schemas folder within the module. */
    public static final String PATH_SCHEMAS = "schemas/";

    /** Template folder within the module. */
    public static final String PATH_TEMPLATES = "templates/";

    /** Logger instance for this class. */
    private static final Log LOG = CmsLog.getLog(CmsEditModuleForm.class);

    /** The name of the caption property for the module site selector. */
    private static final String PROPERTY_SITE_NAME = "name";

    /** Serial version id. */
    private static final long serialVersionUID = 1L;

    /** Text box for the action class. */
    private TextField m_actionClass;

    /** The button to add a new dependency. */
    private Button m_addDependency;

    /** Button to add a new excluded resource. */
    private Button m_addExcludedResource;

    /** Button to add a new export point. */
    private Button m_addExportPoint;

    /** Button to add a new module resource. */
    private Button m_addModuleResource;

    /** The button to add a new module parameter. */
    private Button m_addParameter;

    /** The instance of the module app from which this dialog was opened. */
    private CmsModuleApp m_app;

    /** The text box for the author email address. */
    private TextField m_authorEmail;

    /** Text box for the author name. */
    private TextField m_authorName;

    /** The cancel button. */
    private Button m_cancel;

    /** Layout containing the module dependency widgets. */
    private FormLayout m_dependencies;

    /** Text box for the description. */
    private TextArea m_description;

    /** Parent layout for the excluded resources. */
    private FormLayout m_excludedResources;

    /** Parent layout for export point widgets. */
    private VerticalLayout m_exportPoints;

    /** The field group. */
    private BeanFieldGroup<CmsModule> m_fieldGroup = new BeanFieldGroup<CmsModule>(CmsModule.class);

    /** Check box for creating the classes folder. */
    private CheckBox m_folderClasses;

    /** Check box for creating the elmments folder. */
    private CheckBox m_folderElements;

    /** Check box for creating the formatters folder. */
    private CheckBox m_folderFormatters;

    /** Check box for creating the lib folder. */
    private CheckBox m_folderLib;

    /** Check box for crreating the module folder. */
    private CheckBox m_folderModule;

    /** Check box for creating the resources folder. */
    private CheckBox m_folderResources;

    /** Check box for creating the schemas folder. */
    private CheckBox m_folderSchemas;

    /** Check box for creating the templates folder. */
    private CheckBox m_folderTemplates;

    /** Text box for the group. */
    private TextField m_group;

    /** Check box to enable / disable fixed import site. */
    private CheckBox m_hasImportSite;

    /** Text area for the import script. */
    private TextArea m_importScript;

    /** Select box for the module site. */
    private CmsAutoItemCreatingComboBox m_importSite;

    /** Contains the widget used to display the module site information. */
    private CmsComponentField<CmsResourceInfo> m_info = new CmsComponentField<CmsResourceInfo>();

    /** The module being edited. */
    private CmsModule m_module;

    /** The layout containing the module resources. */
    private FormLayout m_moduleResources;

    /** Text box for the module name. */
    private TextField m_name;

    /** True if this dialog instance was opened for a new module (rather than an existing module). */
    private boolean m_new;

    /** Text box for the nice module name. */
    private TextField m_niceName;

    /** The OK button. */
    private Button m_ok;

    /** Parent layout for module parameter widgets. */
    private FormLayout m_parameters;

    /** Check box for the 'reduced metadata' export mode. */
    private CheckBox m_reducedMetadata;

    /** The tab layout. */
    private TabSheet m_tabs;

    private Runnable m_updateCallback;

    /** Text box for the version. */
    private TextField m_version;

    /**
     * Creates a new instance.<p>
     *
     * @param module the module to edit
     * @param newModule true if the module is a new one, false for editing an existing module
     * @param updateCallback the update callback
     */
    @SuppressWarnings("unchecked")
    public CmsEditModuleForm(CmsModule module, boolean newModule, Runnable updateCallback) {
        m_module = (CmsModule)(module.clone());
        m_new = newModule;
        m_updateCallback = updateCallback;
        CmsVaadinUtils.readAndLocalizeDesign(this, CmsVaadinUtils.getWpMessagesForCurrentLocale(), null);
        IndexedContainer importSitesModel = getModuleSiteContainer(
            A_CmsUI.getCmsObject(),
            PROPERTY_SITE_NAME,
            module.getSite());
        m_importSite.setContainerDataSource(importSitesModel);
        m_importSite.setNullSelectionItemId(ID_EMPTY_SITE);
        m_importSite.setItemCaptionPropertyId(PROPERTY_SITE_NAME);
        m_importSite.setNewValueHandler(new CmsSiteSelectorNewValueHandler(PROPERTY_SITE_NAME));
        m_fieldGroup.setItemDataSource(m_module);
        m_fieldGroup.bind(m_name, "name");
        m_fieldGroup.bind(m_niceName, "niceName");
        m_fieldGroup.bind(m_description, "description");
        m_fieldGroup.bind(m_version, "versionStr");
        m_fieldGroup.bind(m_group, "group");
        m_fieldGroup.bind(m_actionClass, "actionClass");
        m_fieldGroup.bind(m_importScript, "importScript");
        m_fieldGroup.bind(m_importSite, "site");
        m_fieldGroup.bind(m_hasImportSite, "hasImportSite");
        m_fieldGroup.bind(m_authorName, "authorName");
        m_fieldGroup.bind(m_authorEmail, "authorEmail");
        m_fieldGroup.bind(m_reducedMetadata, "reducedExportMode");
        m_fieldGroup.bind(m_folderModule, "createModuleFolder");
        m_fieldGroup.bind(m_folderClasses, "createClassesFolder");
        m_fieldGroup.bind(m_folderElements, "createElementsFolder");
        m_fieldGroup.bind(m_folderFormatters, "createFormattersFolder");
        m_fieldGroup.bind(m_folderLib, "createLibFolder");
        m_fieldGroup.bind(m_folderResources, "createResourcesFolder");
        m_fieldGroup.bind(m_folderSchemas, "createSchemasFolder");
        if (m_new) {
            m_reducedMetadata.setValue(Boolean.TRUE);
            m_name.addValidator(new Validator() {

                private static final long serialVersionUID = 1L;

                public void validate(Object value) throws InvalidValueException {

                    if (OpenCms.getModuleManager().hasModule((String)value)) {
                        throw new InvalidValueException(
                            CmsVaadinUtils.getMessageText(Messages.GUI_MODULES_MODULE_ALREADY_EXISTS_0));
                    }
                    if (!CmsStringUtil.isValidJavaClassName((String)value)) {
                        throw new InvalidValueException(
                            CmsVaadinUtils.getMessageText(Messages.GUI_MODULES_INVALID_MODULE_NAME_0));
                    }
                }

            });

        }
        m_version.addValidator(new Validator() {

            private static final long serialVersionUID = 1L;

            public void validate(Object value) throws InvalidValueException {

                try {
                    @SuppressWarnings("unused")
                    CmsModuleVersion ver = new CmsModuleVersion("" + value);
                } catch (Exception e) {
                    throw new InvalidValueException(
                        CmsVaadinUtils.getMessageText(Messages.GUI_MODULES_INVALID_MODULE_VERSION_0));
                }
            }
        });
        m_fieldGroup.bind(m_folderTemplates, "createTemplateFolder");
        for (AbstractField<String> field : new AbstractField[] {
            m_name,
            m_niceName,
            m_group,
            m_importScript,
            m_actionClass}) {
            field.setConverter(new CmsNullToEmptyConverter());
        }

        if (!newModule) {
            for (AbstractField<?> field : new AbstractField[] {
                m_folderModule,
                m_folderClasses,
                m_folderElements,
                m_folderFormatters,
                m_folderLib,
                m_folderResources,
                m_folderSchemas,
                m_folderTemplates}) {
                field.setVisible(false);
            }
            m_name.setEnabled(false);
        }

        Map<String, String> params = module.getParameters();
        for (Map.Entry<String, String> entry : params.entrySet()) {
            addParameter(entry.getKey() + "=" + entry.getValue());
        }
        for (CmsModuleDependency dependency : module.getDependencies()) {
            addDependencyRow(dependency);
        }

        for (CmsExportPoint exportPoint : module.getExportPoints()) {
            addExportPointRow(exportPoint.getUri(), exportPoint.getConfiguredDestination());
        }
        for (String moduleResource : module.getResources()) {
            addModuleResource(moduleResource);
        }

        for (String excludedResource : module.getExcludeResources()) {
            addExcludedResource(excludedResource);
        }

        m_addParameter.addClickListener(new ClickListener() {

            private static final long serialVersionUID = 1L;

            public void buttonClick(ClickEvent event) {

                addParameter("");
            }

        });
        m_addDependency.addClickListener(new ClickListener() {

            private static final long serialVersionUID = 1L;

            public void buttonClick(ClickEvent event) {

                addDependencyRow(null);
            }
        });
        m_addExportPoint.addClickListener(new ClickListener() {

            private static final long serialVersionUID = 1L;

            public void buttonClick(ClickEvent event) {

                addExportPointRow("", "");
            }
        });
        m_addModuleResource.addClickListener(new ClickListener() {

            private static final long serialVersionUID = 1L;

            public void buttonClick(ClickEvent event) {

                addModuleResource(null);
            }
        });

        m_addExcludedResource.addClickListener(new ClickListener() {

            private static final long serialVersionUID = 1L;

            public void buttonClick(ClickEvent event) {

                addExcludedResource(null);
            }
        });

        m_cancel.addClickListener(new ClickListener() {

            private static final long serialVersionUID = 1L;

            public void buttonClick(ClickEvent event) {

                CmsVaadinUtils.getWindow(CmsEditModuleForm.this).close();
            }
        });
        m_ok.addClickListener(new ClickListener() {

            private static final long serialVersionUID = 1L;

            public void buttonClick(ClickEvent event) {

                updateModule();
            }
        });
        m_importSite.addValueChangeListener(new ValueChangeListener() {

            private static final long serialVersionUID = 1L;

            @SuppressWarnings("synthetic-access")
            public void valueChange(ValueChangeEvent event) {

                String siteRoot = (String)(event.getProperty().getValue());
                updateSiteInfo(siteRoot);

            }
        });
        m_info.set(new CmsResourceInfo("", "", CmsWorkplace.getResourceUri("filetypes/folder_big.png")));
        updateSiteInfo(module.getSite());
        displayResourceInfoDirectly(Arrays.asList(m_info.get()));
    }

    /**
     * Builds the container used for the module site selector.<p>
     *
     * @param cms the CMS context
     * @param captionPropertyName the name of the property used to store captions
     * @param prevValue the value previously set in the module
     *
     * @return the container with the available sites
     */
    public static IndexedContainer getModuleSiteContainer(CmsObject cms, String captionPropertyName, String prevValue) {

        CmsSiteSelectorOptionBuilder optBuilder = new CmsSiteSelectorOptionBuilder(cms);
        optBuilder.addNormalSites(true, (new CmsUserSettings(cms)).getStartFolder());
        IndexedContainer availableSites = new IndexedContainer();
        availableSites.addContainerProperty(captionPropertyName, String.class, null);
        for (CmsSiteSelectorOption option : optBuilder.getOptions()) {
            String siteRoot = option.getSiteRoot();
            if (siteRoot.equals("")) {
                siteRoot = "/";
            }
            Item siteItem = availableSites.addItem(siteRoot);
            siteItem.getItemProperty(captionPropertyName).setValue(option.getMessage());
        }
        if (!availableSites.containsId(prevValue)) {
            String caption = prevValue;
            String siteId = prevValue;

            if (CmsStringUtil.isEmptyOrWhitespaceOnly(prevValue)) {
                caption = CmsVaadinUtils.getMessageText(Messages.GUI_MODULES_MODULE_SITE_NONE_0);
                siteId = ID_EMPTY_SITE;
            }
            availableSites.addItem(siteId).getItemProperty(captionPropertyName).setValue(caption);
        }
        return availableSites;
    }

    /**
     * Adds another entry to the list of module dependencies in the dependencies tab.<p>
     *
     * @param dep the module dependency for which a new row should be added
     */
    public void addDependencyRow(CmsModuleDependency dep) {

        CmsModuleDependencyWidget w = CmsModuleDependencyWidget.create(dep);
        CmsRemovableFormRow<CmsModuleDependencyWidget> row = new CmsRemovableFormRow<CmsModuleDependencyWidget>(w, "");
        row.addStyleName(COMPLEX_ROW);
        w.setWidth("100%");
        m_dependencies.addComponent(row);

    }

    /**
     * Adds another entry to the list of export points in the export point tab.<p>
     *
     * @param src the export point source
     * @param target the export point target
     */
    public void addExportPointRow(String src, String target) {

        CmsExportPointWidget exportPointWidget = new CmsExportPointWidget(src, target);
        CmsRemovableFormRow<CmsExportPointWidget> row = new CmsRemovableFormRow<CmsExportPointWidget>(
            exportPointWidget,
            "");
        row.addStyleName(COMPLEX_ROW);
        m_exportPoints.addComponent(row);
    }

    /**
     * Writes the form data back to the module.<p>
     */
    public void updateModule() {

        try {
            m_fieldGroup.commit();
            // validate 'dynamic' tabs here
            TreeMap<String, String> params = Maps.newTreeMap();
            for (TextField paramField : getFormRowChildren(m_parameters, TextField.class)) {
                String paramStr = paramField.getValue();
                int eqPos = paramStr.indexOf("=");
                if (eqPos >= 0) {
                    String key = paramStr.substring(0, eqPos);
                    String value = paramStr.substring(eqPos + 1);
                    if (!CmsStringUtil.isEmpty(key)) {
                        params.put(key, value);
                    }
                }
            }
            m_module.setParameters(params);

            List<CmsExportPoint> exportPoints = Lists.newArrayList();
            for (CmsExportPointWidget widget : getFormRowChildren(m_exportPoints, CmsExportPointWidget.class)) {
                String source = widget.getUri().trim();
                String target = widget.getDestination().trim();
                if (CmsStringUtil.isEmpty(source) || CmsStringUtil.isEmpty(target)) {
                    continue;
                }
                CmsExportPoint point = new CmsExportPoint(source, target);
                exportPoints.add(point);
            }
            m_module.setExportPoints(exportPoints);

            List<CmsModuleDependency> dependencies = Lists.newArrayList();
            for (CmsModuleDependencyWidget widget : getFormRowChildren(
                m_dependencies,
                CmsModuleDependencyWidget.class)) {
                String moduleName = widget.getModuleName();
                String moduleVersion = widget.getModuleVersion();
                try {
                    CmsModuleDependency dep = new CmsModuleDependency(moduleName, new CmsModuleVersion(moduleVersion));
                    dependencies.add(dep);
                } catch (Exception e) {
                    LOG.debug(e.getLocalizedMessage(), e);
                }
            }
            m_module.setDependencies(dependencies);

            List<String> moduleResources = Lists.newArrayList();
            for (CmsModuleResourceSelectField moduleResField : getFormRowChildren(
                m_moduleResources,
                CmsModuleResourceSelectField.class)) {
                String moduleResource = moduleResField.getValue().trim();
                if (!moduleResource.isEmpty()) {
                    moduleResources.add(moduleResource);
                }
            }
            m_module.setResources(moduleResources);

            List<String> excludedResources = Lists.newArrayList();
            for (CmsModuleResourceSelectField moduleResField : getFormRowChildren(
                m_excludedResources,
                CmsModuleResourceSelectField.class)) {
                String moduleResource = moduleResField.getValue().trim();
                if (!moduleResource.isEmpty()) {
                    excludedResources.add(moduleResource);
                }
            }
            m_module.setExcludeResources(excludedResources);

            CmsObject cms = A_CmsUI.getCmsObject();
            if (m_new) {
                createModuleFolders(m_module);
                OpenCms.getModuleManager().addModule(cms, m_module);
            } else {
                OpenCms.getModuleManager().updateModule(cms, m_module);
            }
            CmsVaadinUtils.getWindow(this).close();
            m_updateCallback.run();
        } catch (CommitException e) {
            if (e.getCause() instanceof FieldGroup.FieldGroupInvalidValueException) {
                int minTabIdx = 999;
                for (Field<?> field : e.getInvalidFields().keySet()) {
                    int tabIdx = getTabIndex(field);
                    if (tabIdx != -1) {
                        minTabIdx = Math.min(tabIdx, minTabIdx);
                    }
                }
                m_tabs.setSelectedTab(minTabIdx);
            } else {
                CmsErrorDialog.showErrorDialog(e);
            }
            return;
        } catch (Exception e) {
            CmsErrorDialog.showErrorDialog(e);
        }

    }

    /**
     * Adds a new module dependency widget.<p>
     *
     * @param moduleName the module name
     * @param version the module version
     */
    void addDependency(String moduleName, String version) {

        try {
            m_dependencies.addComponent(
                new CmsRemovableFormRow<CmsModuleDependencyWidget>(
                    CmsModuleDependencyWidget.create(
                        new CmsModuleDependency(moduleName, new CmsModuleVersion(version))),
                    ""));
        } catch (Exception e) {
            CmsErrorDialog.showErrorDialog(e);
        }
    }

    /**
     * Adds a new resource selection widget to the list of module resources.<p>
     *
     * @param moduleResource the initial value for the new widget
     */
    void addExcludedResource(String moduleResource) {

        CmsModuleResourceSelectField resField = new CmsModuleResourceSelectField();
        CmsObject moduleCms = null;
        try {
            moduleCms = OpenCms.initCmsObject(A_CmsUI.getCmsObject());
        } catch (CmsException e) {
            LOG.error(e.getLocalizedMessage(), e);
        }
        if (getSelectedSite() != null) {
            moduleCms.getRequestContext().setSiteRoot(getSelectedSite());
        }
        resField.setCmsObject(moduleCms);
        if (moduleResource != null) {
            resField.setValue(moduleResource);
        }
        CmsRemovableFormRow<CmsModuleResourceSelectField> row = new CmsRemovableFormRow<CmsModuleResourceSelectField>(
            resField,
            "");
        row.setCaption(CmsVaadinUtils.getMessageText(Messages.GUI_MODULES_LABEL_EXCLUDED_RESOURCE_0));
        m_excludedResources.addComponent(row);
    }

    /**
     * Adds a new module resource row.<p>
     *
     * @param moduleResource the initial value for the module resource
     */
    void addModuleResource(String moduleResource) {

        CmsModuleResourceSelectField resField = new CmsModuleResourceSelectField();
        CmsObject moduleCms = null;
        try {
            moduleCms = OpenCms.initCmsObject(A_CmsUI.getCmsObject());
        } catch (CmsException e) {
            LOG.error(e.getLocalizedMessage(), e);
        }
        if (getSelectedSite() != null) {
            moduleCms.getRequestContext().setSiteRoot(getSelectedSite());
        }
        resField.setCmsObject(moduleCms);
        if (moduleResource != null) {
            resField.setValue(moduleResource);
        }
        CmsRemovableFormRow<Field<String>> row = new CmsRemovableFormRow<Field<String>>(resField, "");
        row.setCaption(CmsVaadinUtils.getMessageText(Messages.GUI_MODULES_LABEL_MODULE_RESOURCE_0));
        m_moduleResources.addComponent(row);
    }

    /**
     * Add a given parameter to the form layout.<p>
     *
     * @param parameter parameter to add to form
     */
    void addParameter(String parameter) {

        TextField textField = new TextField();
        if (parameter != null) {
            textField.setValue(parameter);
        }
        CmsRemovableFormRow<TextField> row = new CmsRemovableFormRow<TextField>(textField, "");
        row.setCaption(CmsVaadinUtils.getMessageText(Messages.GUI_MODULES_LABEL_PARAMETER_0));
        row.setDescription(CmsVaadinUtils.getMessageText(Messages.GUI_MODULES_PARAMETER_DESC_0));
        m_parameters.addComponent(row);
    }

    /**
     * Helper method to get the descendants of a container with a specific widget type.<p>
     *
     * @param container the container
     * @param cls the class
     *
     * @return the list of results
     */
    <T extends Component> List<T> getFormRowChildren(AbstractComponentContainer container, final Class<T> cls) {

        final List<T> result = Lists.newArrayList();
        CmsVaadinUtils.visitDescendants(container, new Predicate<Component>() {

            public boolean apply(Component comp) {

                if (cls.isAssignableFrom(comp.getClass())) {
                    result.add(cls.cast(comp));
                }
                return true;
            }
        });
        return result;
    }

    /**
     * Gets the site root currently selected in the module site combo box.<p>
     *
     * @return the currently selected module site
     */
    String getSelectedSite() {

        return (String)(m_importSite.getValue());
    }

    /**
     * Gets the tab index for the given component.<p>
     *
     * @param component a component
     *
     * @return the tab index
     */
    int getTabIndex(Component component) {

        List<Component> tabs = Lists.newArrayList(m_tabs.iterator());
        while (component != null) {
            int pos = tabs.indexOf(component);
            if (pos >= 0) {
                return pos;
            }
            component = component.getParent();
        }
        return -1;
    }

    /**
     * Creates all module folders that are selected in the input form.<p>
     *
     * @param module the module
     *
     * @return the updated module
     *
     * @throws CmsException if somehting goes wrong
     */
    private CmsModule createModuleFolders(CmsModule module) throws CmsException {

        String modulePath = CmsWorkplace.VFS_PATH_MODULES + module.getName() + "/";
        CmsObject cms = A_CmsUI.getCmsObject();
        List<CmsExportPoint> exportPoints = module.getExportPoints();
        List<String> resources = module.getResources();

        // set the createModuleFolder flag if any other flag is set
        if (module.isCreateClassesFolder()
            || module.isCreateElementsFolder()
            || module.isCreateLibFolder()
            || module.isCreateResourcesFolder()
            || module.isCreateSchemasFolder()
            || module.isCreateTemplateFolder()
            || module.isCreateFormattersFolder()) {
            module.setCreateModuleFolder(true);
        }

        Set<String> exportPointPaths = new HashSet<String>();
        for (CmsExportPoint exportPoint : exportPoints) {
            exportPointPaths.add(exportPoint.getUri());
        }

        // check if we have to create the module folder

        I_CmsResourceType folderType = OpenCms.getResourceManager().getResourceType(
            CmsResourceTypeFolder.getStaticTypeName());
        if (module.isCreateModuleFolder()) {
            cms.createResource(modulePath, folderType);
            // add the module folder to the resource list
            resources.add(modulePath);
            module.setResources(resources);
        }

        // check if we have to create the template folder
        if (module.isCreateTemplateFolder()) {
            String path = modulePath + PATH_TEMPLATES;
            cms.createResource(path, folderType);
        }

        // check if we have to create the elements folder
        if (module.isCreateElementsFolder()) {
            String path = modulePath + PATH_ELEMENTS;
            cms.createResource(path, folderType);
        }

        if (module.isCreateFormattersFolder()) {
            String path = modulePath + PATH_FORMATTERS;
            cms.createResource(path, folderType);
        }

        // check if we have to create the schemas folder
        if (module.isCreateSchemasFolder()) {
            String path = modulePath + PATH_SCHEMAS;
            cms.createResource(path, folderType);
        }

        // check if we have to create the resources folder
        if (module.isCreateResourcesFolder()) {
            String path = modulePath + PATH_RESOURCES;
            cms.createResource(path, folderType);
        }

        // check if we have to create the lib folder
        if (module.isCreateLibFolder()) {
            String path = modulePath + PATH_LIB;
            cms.createResource(path, folderType);
            if (!exportPointPaths.contains(path)) {
                CmsExportPoint exp = new CmsExportPoint(path, "WEB-INF/lib/");
                exportPoints.add(exp);
            }
            module.setExportPoints(exportPoints);
        }

        // check if we have to create the classes folder
        if (module.isCreateClassesFolder()) {
            String path = modulePath + PATH_CLASSES;
            cms.createResource(path, folderType);
            if (!exportPointPaths.contains(path)) {
                CmsExportPoint exp = new CmsExportPoint(path, "WEB-INF/classes/");
                exportPoints.add(exp);
                module.setExportPoints(exportPoints);
            }

            // now create all subfolders for the package structure
            StringTokenizer tok = new StringTokenizer(m_module.getName(), ".");
            while (tok.hasMoreTokens()) {
                String folder = tok.nextToken();
                path += folder + "/";
                cms.createResource(path, folderType);
            }
        }
        return module;
    }

    /**
     * Updates the module site info display after the module site is changed.<p>
     *
     * @param siteRoot the new module site root
     */
    private void updateSiteInfo(final String siteRoot) {

        String top = "";
        String bottom = "";

        if (siteRoot == null) {
            top = CmsVaadinUtils.getMessageText(Messages.GUI_MODULES_MODULE_SITE_NOT_SET_0);
            bottom = "";
        } else {
            CmsSiteManagerImpl siteManager = OpenCms.getSiteManager();
            CmsSite site = siteManager.getSiteForSiteRoot(siteRoot);
            if (site != null) {
                top = CmsVaadinUtils.getMessageText(Messages.GUI_MODULES_MODULE_SITE_1, site.getTitle());
                bottom = site.getSiteRoot();
            } else if (siteRoot.equals("") || siteRoot.equals("/")) {
                top = CmsVaadinUtils.getMessageText(Messages.GUI_MODULES_MODULE_SITE_ROOT_FOLDER_0);
                bottom = "/";
            } else {
                top = CmsVaadinUtils.getMessageText(Messages.GUI_MODULES_MODULE_SITE_1) + siteRoot;
                bottom = siteRoot;
            }
        }
        m_info.get().getTopLine().setValue(top);
        m_info.get().getBottomLine().setValue(bottom);

        for (Component c : Arrays.asList(m_moduleResources, m_excludedResources)) {
            CmsVaadinUtils.visitDescendants(c, new Predicate<Component>() {

                public boolean apply(Component comp) {

                    if (comp instanceof CmsModuleResourceSelectField) {
                        ((CmsModuleResourceSelectField)comp).updateSite(siteRoot);
                    }
                    return true;
                }
            });
        }

    }

}
