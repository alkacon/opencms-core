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

import org.opencms.ade.configuration.CmsADEManager;
import org.opencms.ade.galleries.CmsSiteSelectorOptionBuilder;
import org.opencms.ade.galleries.shared.CmsSiteSelectorOption;
import org.opencms.db.CmsExportPoint;
import org.opencms.db.CmsUserSettings;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsResource;
import org.opencms.file.types.CmsResourceTypeFolder;
import org.opencms.file.types.I_CmsResourceType;
import org.opencms.i18n.CmsLocaleManager;
import org.opencms.i18n.CmsVfsBundleManager;
import org.opencms.jsp.util.CmsJspElFunctions;
import org.opencms.lock.CmsLockException;
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
import org.opencms.ui.components.editablegroup.CmsEditableGroup;
import org.opencms.ui.components.editablegroup.I_CmsEditableGroupRow;
import org.opencms.ui.util.CmsComponentField;
import org.opencms.ui.util.CmsNullToEmptyConverter;
import org.opencms.util.CmsFileUtil;
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
import com.google.common.base.Supplier;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.vaadin.ui.AbstractComponentContainer;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.Component;
import com.vaadin.ui.FormLayout;
import com.vaadin.ui.TabSheet;
import com.vaadin.v7.data.Item;
import com.vaadin.v7.data.Property.ValueChangeEvent;
import com.vaadin.v7.data.Property.ValueChangeListener;
import com.vaadin.v7.data.Validator;
import com.vaadin.v7.data.fieldgroup.BeanFieldGroup;
import com.vaadin.v7.data.fieldgroup.FieldGroup;
import com.vaadin.v7.data.fieldgroup.FieldGroup.CommitException;
import com.vaadin.v7.data.util.IndexedContainer;
import com.vaadin.v7.ui.AbstractField;
import com.vaadin.v7.ui.CheckBox;
import com.vaadin.v7.ui.Field;
import com.vaadin.v7.ui.TextArea;
import com.vaadin.v7.ui.TextField;
import com.vaadin.v7.ui.VerticalLayout;

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

    /** Message bundle file name suffix. */
    private static final String SUFFIX_BUNDLE_FILE = ".messages";

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

    /**I18n path. */
    private static final String PATH_i18n = "i18n/";

    public static final String CONFIG_FILE = ".config";

    /** Text box for the action class. */
    private TextField m_actionClass;

    /** The text box for the author email address. */
    private TextField m_authorEmail;

    /** Text box for the author name. */
    private TextField m_authorName;

    /** Check box to enable / disable version autoincrement mode. */
    private CheckBox m_autoIncrement;

    /** The cancel button. */
    private Button m_cancel;

    /** Layout containing the module dependency widgets. */
    private FormLayout m_dependencies;

    /** Group for editing lists of dependencies. */
    private CmsEditableGroup m_dependencyGroup;

    /** Text box for the description. */
    private TextArea m_description;

    /** Parent layout for the excluded resources. */
    private FormLayout m_excludedResources;

    /** The group for the excluded module resource fields. */
    private CmsEditableGroup m_excludedResourcesGroup;

    /** Group for editing list of export points. */
    private CmsEditableGroup m_exportPointGroup;

    /** Parent layout for export point widgets. */
    private VerticalLayout m_exportPoints;

    /** The field group. */
    private BeanFieldGroup<CmsModule> m_fieldGroup = new BeanFieldGroup<CmsModule>(CmsModule.class);

    /** Check box for creating the classes folder. */
    private CheckBox m_folderClasses;

    /** Check box for creating the elmments folder. */
    private CheckBox m_folderI18N;

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

    /** The group for the module resource fields. */
    private CmsEditableGroup m_moduleResourcesGroup;

    /** Text box for the module name. */
    private TextField m_name;

    /** True if this dialog instance was opened for a new module (rather than an existing module). */
    private boolean m_new;

    /** Text box for the nice module name. */
    private TextField m_niceName;

    /** The OK button. */
    private Button m_ok;

    /** The original module instance passed into the constructor. */
    private CmsModule m_oldModuleInstance;

    /** Group for editing lists of parameters. */
    private CmsEditableGroup m_parameterGroup;

    /** Parent layout for module parameter widgets. */
    private FormLayout m_parameters;

    /** Check box for the 'reduced metadata' export mode. */
    private CheckBox m_reducedMetadata;

    /** The tab layout. */
    private TabSheet m_tabs;

    /** The callback to call after editing the module. */
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

        m_oldModuleInstance = module;
        m_module = (module.clone());
        String site = m_module.getSite();
        if (!CmsStringUtil.isEmptyOrWhitespaceOnly(site)) {
            site = site.trim();
            if (!site.equals("/")) {
                site = CmsFileUtil.removeTrailingSeparator(site);
                m_module.setSite(site);
            }
        }
        m_new = newModule;
        m_updateCallback = updateCallback;
        CmsVaadinUtils.readAndLocalizeDesign(this, CmsVaadinUtils.getWpMessagesForCurrentLocale(), null);
        IndexedContainer importSitesModel = getModuleSiteContainer(
            A_CmsUI.getCmsObject(),
            PROPERTY_SITE_NAME,
            m_module.getSite());
        m_importSite.setContainerDataSource(importSitesModel);
        m_importSite.setNullSelectionItemId(ID_EMPTY_SITE);
        m_importSite.setItemCaptionPropertyId(PROPERTY_SITE_NAME);
        m_importSite.setNewValueHandler(new CmsSiteSelectorNewValueHandler(PROPERTY_SITE_NAME));
        if (m_new) {
            m_module.setCreateModuleFolder(true);
            m_module.setCreateI18NFolder(true);
        }
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
        m_fieldGroup.bind(m_folderI18N, "createI18NFolder");
        m_fieldGroup.bind(m_folderFormatters, "createFormattersFolder");
        m_fieldGroup.bind(m_folderLib, "createLibFolder");
        m_fieldGroup.bind(m_folderResources, "createResourcesFolder");
        m_fieldGroup.bind(m_folderSchemas, "createSchemasFolder");
        m_fieldGroup.bind(m_autoIncrement, "autoIncrement");
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
                m_folderI18N,
                m_folderFormatters,
                m_folderLib,
                m_folderResources,
                m_folderSchemas,
                m_folderTemplates}) {
                field.setVisible(false);
            }
            m_name.setEnabled(false);
        }

        Supplier<Component> moduleResourceFieldFactory = new Supplier<Component>() {

            public Component get() {

                return createModuleResourceField(null);
            }
        };
        String addResourceButtonText = CmsVaadinUtils.getMessageText(Messages.GUI_MODULES_ADD_RESOURCE_0);
        m_moduleResourcesGroup = new CmsEditableGroup(
            m_moduleResources,
            moduleResourceFieldFactory,
            addResourceButtonText);
        m_excludedResourcesGroup = new CmsEditableGroup(
            m_excludedResources,
            moduleResourceFieldFactory,
            addResourceButtonText);
        m_parameterGroup = new CmsEditableGroup(m_parameters, new Supplier<Component>() {

            public Component get() {

                TextField result = new TextField();
                return result;
            }
        }, CmsVaadinUtils.getMessageText(Messages.GUI_MODULES_ADD_PARAMETER_0));
        m_exportPointGroup = new CmsEditableGroup(m_exportPoints, new Supplier<Component>() {

            public Component get() {

                return new CmsExportPointWidget("", "");
            }
        }, CmsVaadinUtils.getMessageText(Messages.GUI_MODULES_ADD_EXPORT_POINT_0));

        m_dependencyGroup = new CmsEditableGroup(m_dependencies, new Supplier<Component>() {

            public Component get() {

                CmsModuleDependencyWidget component = CmsModuleDependencyWidget.create(null);
                return component;
            }
        }, CmsVaadinUtils.getMessageText(Messages.GUI_MODULES_ADD_DEPENDENCY_0));

        m_moduleResourcesGroup.init();
        m_excludedResourcesGroup.init();
        m_parameterGroup.init();
        m_exportPointGroup.init();
        m_dependencyGroup.init();
        String resourceListError = CmsVaadinUtils.getMessageText(Messages.GUI_MODULES_RESOURCE_LIST_ERROR_0);
        m_moduleResourcesGroup.setErrorMessage(resourceListError);
        m_excludedResourcesGroup.setErrorMessage(resourceListError);

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

        m_info.set(new CmsResourceInfo("", "", ""));
        m_info.get().getResourceIcon().initContent(null, CmsModuleApp.Icons.RESINFO_ICON, null, false, false);
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
        m_dependencyGroup.addRow(w);
    }

    /**
     * Adds another entry to the list of export points in the export point tab.<p>
     *
     * @param src the export point source
     * @param target the export point target
     */
    public void addExportPointRow(String src, String target) {

        CmsExportPointWidget exportPointWidget = new CmsExportPointWidget(src, target);
        m_exportPointGroup.addRow(exportPointWidget);
        //        row.addStyleName(COMPLEX_ROW);
        //        m_exportPoints.addComponent(row);
    }

    /**
     * Writes the form data back to the module.<p>
     */
    public void updateModule() {

        try {
            m_fieldGroup.commit();
            // validate 'dynamic' tabs here
            TreeMap<String, String> params = Maps.newTreeMap();
            for (I_CmsEditableGroupRow row : m_parameterGroup.getRows()) {
                TextField paramField = (TextField)(row.getComponent());
                String paramStr = paramField.getValue();
                int eqPos = paramStr.indexOf("=");
                if (eqPos >= 0) {
                    String key = paramStr.substring(0, eqPos);
                    key = key.trim();
                    String value = paramStr.substring(eqPos + 1);
                    value = value.trim();
                    if (!CmsStringUtil.isEmpty(key)) {
                        params.put(key, value);
                    }
                }
            }
            m_module.setParameters(params);

            List<CmsExportPoint> exportPoints = Lists.newArrayList();
            for (I_CmsEditableGroupRow row : m_exportPointGroup.getRows()) {
                CmsExportPointWidget widget = (CmsExportPointWidget)(row.getComponent());
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
            for (I_CmsEditableGroupRow row : m_moduleResourcesGroup.getRows()) {
                CmsModuleResourceSelectField field = (CmsModuleResourceSelectField)(row.getComponent());
                String moduleResource = field.getValue().trim();
                if (!moduleResource.isEmpty()) {
                    moduleResources.add(moduleResource);
                }
            }
            m_module.setResources(moduleResources);

            List<String> excludedResources = Lists.newArrayList();
            for (I_CmsEditableGroupRow row : m_excludedResourcesGroup.getRows()) {
                CmsModuleResourceSelectField field = (CmsModuleResourceSelectField)(row.getComponent());
                String moduleResource = field.getValue().trim();
                if (!moduleResource.isEmpty()) {
                    excludedResources.add(moduleResource);
                }
            }
            m_module.setExcludeResources(excludedResources);

            if (!m_oldModuleInstance.isAutoIncrement() && m_module.isAutoIncrement()) {
                m_module.setCheckpointTime(System.currentTimeMillis());
            }

            CmsObject cms = A_CmsUI.getCmsObject();
            if (m_new) {
                createModuleFolders(cms, m_module);
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

        CmsModuleResourceSelectField resField = createModuleResourceField(moduleResource);
        if (resField != null) {
            m_excludedResourcesGroup.addRow(resField);
        }
    }

    /**
     * Adds a new module resource row.<p>
     *
     * @param moduleResource the initial value for the module resource
     */
    void addModuleResource(String moduleResource) {

        CmsModuleResourceSelectField resField = createModuleResourceField(moduleResource);
        if (resField != null) {
            m_moduleResourcesGroup.addRow(resField);
        }
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
        m_parameterGroup.addRow(textField);
    }

    /**
     * Creates a module resource selection field.<p>
     *
     * @param moduleResource the initial content for the field
     *
     * @return the module resource selection field
     */
    CmsModuleResourceSelectField createModuleResourceField(String moduleResource) {

        CmsModuleResourceSelectField resField = new CmsModuleResourceSelectField();
        CmsObject moduleCms = null;
        try {
            moduleCms = OpenCms.initCmsObject(A_CmsUI.getCmsObject());
            if (getSelectedSite() != null) {
                moduleCms.getRequestContext().setSiteRoot(getSelectedSite());
            }
            resField.setCmsObject(moduleCms);
            if (moduleResource != null) {
                resField.setValue(moduleResource);
            }
            return resField;
        } catch (CmsException e) {
            LOG.error(e.getLocalizedMessage(), e);
            return null;
        }
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
    private CmsModule createModuleFolders(CmsObject cms, CmsModule module) throws CmsException {

        String modulePath = CmsWorkplace.VFS_PATH_MODULES + module.getName() + "/";
        List<CmsExportPoint> exportPoints = module.getExportPoints();
        List<String> resources = module.getResources();

        // set the createModuleFolder flag if any other flag is set
        if (module.isCreateClassesFolder()
            || module.isCreateElementsFolder()
            || module.isCreateI18NFolder()
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
        I_CmsResourceType configType = OpenCms.getResourceManager().getResourceType(CmsADEManager.MODULE_CONFIG_TYPE);

        if (module.isCreateModuleFolder()) {
            CmsResource resource = cms.createResource(modulePath, folderType);
            CmsResource configResource = cms.createResource(modulePath + CONFIG_FILE, configType);
            try {
                cms.unlockResource(resource);
                cms.unlockResource(configResource);
            } catch (CmsLockException locke) {
                LOG.warn("Unbale to unlock resource", locke);
            }
            // add the module folder to the resource list
            resources.add(modulePath);
            module.setResources(resources);
        }

        // check if we have to create the template folder
        if (module.isCreateTemplateFolder()) {
            String path = modulePath + PATH_TEMPLATES;
            CmsResource resource = cms.createResource(path, folderType);
            try {
                cms.unlockResource(resource);
            } catch (CmsLockException locke) {
                LOG.warn("Unbale to unlock resource", locke);
            }
        }

        if (module.isCreateI18NFolder()) {
            String path = modulePath + PATH_i18n;
            CmsResource resource = cms.createResource(path, folderType);
            CmsResource bundleResource = cms.createResource(
                path + module.getName() + SUFFIX_BUNDLE_FILE + "_" + CmsLocaleManager.getDefaultLocale(),
                OpenCms.getResourceManager().getResourceType(CmsVfsBundleManager.TYPE_PROPERTIES_BUNDLE),
                null,
                null);
            cms.writeResource(bundleResource);
            try {
                cms.unlockResource(resource);
                cms.unlockResource(bundleResource);
            } catch (CmsLockException locke) {
                LOG.warn("Unbale to unlock resource", locke);
            }
        }

        // check if we have to create the elements folder
        if (module.isCreateElementsFolder()) {
            String path = modulePath + PATH_ELEMENTS;
            CmsResource resource = cms.createResource(path, folderType);
            try {
                cms.unlockResource(resource);
            } catch (CmsLockException locke) {
                LOG.warn("Unbale to unlock resource", locke);
            }
        }

        if (module.isCreateFormattersFolder()) {
            String path = modulePath + PATH_FORMATTERS;
            CmsResource resource = cms.createResource(path, folderType);
            try {
                cms.unlockResource(resource);
            } catch (CmsLockException locke) {
                LOG.warn("Unbale to unlock resource", locke);
            }
        }

        // check if we have to create the schemas folder
        if (module.isCreateSchemasFolder()) {
            String path = modulePath + PATH_SCHEMAS;
            CmsResource resource = cms.createResource(path, folderType);
            try {
                cms.unlockResource(resource);
            } catch (CmsLockException locke) {
                LOG.warn("Unbale to unlock resource", locke);
            }
        }

        // check if we have to create the resources folder
        if (module.isCreateResourcesFolder()) {
            String path = modulePath + PATH_RESOURCES;
            CmsResource resource = cms.createResource(path, folderType);
            try {
                cms.unlockResource(resource);
            } catch (CmsLockException locke) {
                LOG.warn("Unbale to unlock resource", locke);
            }
        }

        // check if we have to create the lib folder
        if (module.isCreateLibFolder()) {
            String path = modulePath + PATH_LIB;
            CmsResource resource = cms.createResource(path, folderType);
            try {
                cms.unlockResource(resource);
            } catch (CmsLockException locke) {
                LOG.warn("Unbale to unlock resource", locke);
            }
            if (!exportPointPaths.contains(path)) {
                CmsExportPoint exp = new CmsExportPoint(path, "WEB-INF/lib/");
                exportPoints.add(exp);
            }
            module.setExportPoints(exportPoints);
        }

        // check if we have to create the classes folder
        if (module.isCreateClassesFolder()) {
            String path = modulePath + PATH_CLASSES;
            CmsResource resource = cms.createResource(path, folderType);
            try {
                cms.unlockResource(resource);
            } catch (CmsLockException locke) {
                LOG.warn("Unbale to unlock resource", locke);
            }
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
                CmsResource resource2 = cms.createResource(path, folderType);
                try {
                    cms.unlockResource(resource2);
                } catch (CmsLockException locke) {
                    LOG.warn("Unbale to unlock resource", locke);
                }
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

        if (m_new) {
            top = CmsVaadinUtils.getMessageText(Messages.GUI_MODULES_RESINFO_NEW_MODULE_0);
        } else {
            top = m_module.getName();
        }

        if (siteRoot == null) {
            bottom = CmsVaadinUtils.getMessageText(Messages.GUI_MODULES_MODULE_SITE_NOT_SET_0);
        } else {
            CmsSiteManagerImpl siteManager = OpenCms.getSiteManager();
            CmsSite site = siteManager.getSiteForSiteRoot(siteRoot);
            if (site != null) {
                bottom = CmsVaadinUtils.getMessageText(
                    Messages.GUI_MODULES_MODULE_SITE_1,
                    site.getTitle() + " (" + siteRoot + ")");
            } else if (siteRoot.equals("") || siteRoot.equals("/")) {
                bottom = CmsVaadinUtils.getMessageText(Messages.GUI_MODULES_MODULE_SITE_ROOT_FOLDER_0);
            } else {
                bottom = CmsVaadinUtils.getMessageText(Messages.GUI_MODULES_MODULE_SITE_1, siteRoot);
            }
        }
        m_info.get().getTopLine().setValue(top);
        m_info.get().getBottomLine().setValue(CmsJspElFunctions.stripHtml(bottom));

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
