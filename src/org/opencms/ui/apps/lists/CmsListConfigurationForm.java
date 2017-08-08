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

package org.opencms.ui.apps.lists;

import org.opencms.ade.configuration.CmsADEConfigData;
import org.opencms.file.CmsFile;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsResource;
import org.opencms.file.CmsResourceFilter;
import org.opencms.i18n.CmsLocaleManager;
import org.opencms.lock.CmsLockActionRecord;
import org.opencms.lock.CmsLockUtil;
import org.opencms.main.CmsException;
import org.opencms.main.CmsLog;
import org.opencms.main.OpenCms;
import org.opencms.relations.CmsLink;
import org.opencms.ui.A_CmsUI;
import org.opencms.ui.CmsVaadinUtils;
import org.opencms.ui.apps.Messages;
import org.opencms.ui.apps.lists.CmsListManager.ListConfigurationBean;
import org.opencms.ui.apps.projects.CmsEditProjectForm;
import org.opencms.ui.components.CmsBasicDialog;
import org.opencms.ui.components.CmsRemovableFormRow;
import org.opencms.ui.components.categoryselect.CmsCategorySelectField;
import org.opencms.ui.components.fileselect.CmsPathSelectField;
import org.opencms.util.CmsStringUtil;
import org.opencms.util.CmsUUID;
import org.opencms.workplace.CmsWorkplaceMessages;
import org.opencms.workplace.explorer.CmsExplorerTypeSettings;
import org.opencms.workplace.explorer.CmsResourceUtil;
import org.opencms.xml.containerpage.I_CmsFormatterBean;
import org.opencms.xml.content.CmsXmlContent;
import org.opencms.xml.content.CmsXmlContentFactory;
import org.opencms.xml.types.CmsXmlDisplayFormatterValue;
import org.opencms.xml.types.CmsXmlVfsFileValue;
import org.opencms.xml.types.I_CmsXmlContentValue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.logging.Log;

import com.vaadin.data.Item;
import com.vaadin.data.util.IndexedContainer;
import com.vaadin.server.Resource;
import com.vaadin.ui.AbstractComponent;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.CheckBox;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.Component;
import com.vaadin.ui.Field;
import com.vaadin.ui.FormLayout;
import com.vaadin.ui.TextField;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;

/**
 * The list configuration edit form.<p>
 */
public class CmsListConfigurationForm extends CmsBasicDialog {

    /**
     * Parameter field data.<p>
     */
    public static class ParameterField {

        /** The caption message key. */
        String m_captionKey;

        /** The description message key. */
        String m_decriptionKey;

        /** The field component class. */
        Class<?> m_fieldType;

        /** The field key. */
        String m_key;

        /** The advanced tab flag. */
        boolean m_useAdvancedTab;

        /**
         * Constructor.<p>
         *
         * @param key the field key
         * @param captionKey the caption message key
         * @param descriptionKey the description message key
         * @param fieldType the field component class
         * @param useAdvancedTab <code>true</code> to show on advanced fields tab
         */
        public ParameterField(
            String key,
            String captionKey,
            String descriptionKey,
            Class<?> fieldType,
            boolean useAdvancedTab) {
            m_key = key;
            m_captionKey = captionKey;
            m_decriptionKey = descriptionKey;
            m_fieldType = fieldType;
            m_useAdvancedTab = useAdvancedTab;
        }
    }

    /** SOLR field name. */
    public static final String FIELD_CATEGORIES = "category_exact";

    /** SOLR field name. */
    public static final String FIELD_DATE = "instancedate_%s_dt";

    /** SOLR field name. */
    public static final String FIELD_DATE_FACET_NAME = "instancedate";

    /** SOLR field name. */
    public static final String FIELD_PARENT_FOLDERS = "parent-folders";

    /** List configuration node name and field key. */
    public static final String N_BLACKLIST = "Blacklist";

    /** List configuration node name and field key. */
    public static final String N_CATEGORY = "Category";

    /** List configuration node name and field key. */
    public static final String N_CATEGORY_FILTERS = "CategoryFilters";

    /** List configuration node name and field key. */
    public static final String N_CATEGORY_FULL_PATH = "CategoryFullPath";

    /** List configuration node name and field key. */
    public static final String N_CATEGORY_ONLY_LEAFS = "CategoryOnlyLeafs";

    /** List configuration node name and field key. */
    public static final String N_DISPLAY_OPTIONS = "DisplayOptions";

    /** List configuration node name and field key. */
    public static final String N_DISPLAY_TYPE = "TypesToCollect";

    /** List configuration node name and field key. */
    public static final String N_FILTER_QUERY = "FilterQuery";

    /** List configuration node name and field key. */
    public static final String N_PREOPEN_ARCHIVE = "PreopenArchive";

    /** List configuration node name and field key. */
    public static final String N_PREOPEN_CATEGORIES = "PreopenCategories";

    /** List configuration node name and field key. */
    public static final String N_SEARCH_FOLDER = "SearchFolder";

    /** List configuration node name and field key. */
    public static final String N_SHOW_DATE = "ShowDate";

    /** List configuration node name and field key. */
    public static final String N_SHOW_EXPIRED = "ShowExpired";

    /** List configuration node name and field key. */
    public static final String N_SORT_ORDER = "SortOrder";

    /** List configuration node name and field key. */
    public static final String N_TITLE = "Title";

    /** Container item property key. */
    private static final String FORMATTER_PROP = "formatter";

    /** Container item property key. */
    private static final String ICON_PROP = "icon";

    /** The logger for this class. */
    private static Log LOG = CmsLog.getLog(CmsEditProjectForm.class.getName());

    /** The month name abbreviations. */
    static final String[] MONTHS = new String[] {
        "JAN",
        "FEB",
        "MAR",
        "APR",
        "MAY",
        "JUN",
        "JUL",
        "AUG",
        "SEP",
        "OCT",
        "NOV",
        "DEC"};

    /** The parameter fields. */
    public static final ParameterField[] PARAMETER_FIELDS = new ParameterField[] {
        new ParameterField(
            N_CATEGORY,
            Messages.GUI_LISTMANAGER_PARAM_CATEGORY_0,
            Messages.GUI_LISTMANAGER_PARAM_CATEGORY_HELP_0,
            CmsCategorySelectField.class,
            false),
        new ParameterField(
            N_FILTER_QUERY,
            Messages.GUI_LISTMANAGER_PARAM_FILTER_QUERY_0,
            Messages.GUI_LISTMANAGER_PARAM_FILTER_QUERY_HELP_0,
            TextField.class,
            true),
        new ParameterField(
            N_SORT_ORDER,
            Messages.GUI_LISTMANAGER_PARAM_SORT_ORDER_0,
            Messages.GUI_LISTMANAGER_PARAM_SORT_ORDER_HELP_0,
            ComboBox.class,
            true),
        new ParameterField(
            N_SHOW_DATE,
            Messages.GUI_LISTMANAGER_PARAM_SHOW_DATE_0,
            Messages.GUI_LISTMANAGER_PARAM_SHOW_DATE_HELP_0,
            CheckBox.class,
            true),
        new ParameterField(
            N_SHOW_EXPIRED,
            Messages.GUI_LISTMANAGER_PARAM_SHOW_EXPIRED_0,
            Messages.GUI_LISTMANAGER_PARAM_SHOW_EXPIRED_HELP_0,
            CheckBox.class,
            true),
        new ParameterField(
            N_DISPLAY_OPTIONS,
            Messages.GUI_LISTMANAGER_PARAM_DISPLAY_OPTIONS_0,
            Messages.GUI_LISTMANAGER_PARAM_DISPLAY_OPTIONS_HELP_0,
            TextField.class,
            true),
        new ParameterField(
            N_CATEGORY_FILTERS,
            Messages.GUI_LISTMANAGER_PARAM_CATEGORY_FILTERS_0,
            Messages.GUI_LISTMANAGER_PARAM_CATEGORY_FILTERS_HELP_0,
            TextField.class,
            true),
        new ParameterField(
            N_CATEGORY_FULL_PATH,
            Messages.GUI_LISTMANAGER_PARAM_FULL_CATEGORY_PATHS_0,
            Messages.GUI_LISTMANAGER_PARAM_FULL_CATEGORY_PATHS_HELP_0,
            CheckBox.class,
            true),
        new ParameterField(
            N_CATEGORY_ONLY_LEAFS,
            Messages.GUI_LISTMANAGER_PARAM_LEAFS_ONLY_0,
            Messages.GUI_LISTMANAGER_PARAM_LEAFS_ONLY_HELP_0,
            CheckBox.class,
            true),
        new ParameterField(
            N_PREOPEN_CATEGORIES,
            Messages.GUI_LISTMANAGER_PARAM_PREOPEN_CATEGORIES_0,
            Messages.GUI_LISTMANAGER_PARAM_PREOPEN_CATEGORIES_HELP_0,
            CheckBox.class,
            true),
        new ParameterField(
            N_PREOPEN_ARCHIVE,
            Messages.GUI_LISTMANAGER_PARAM_PREOPEN_ARCHIVE_0,
            Messages.GUI_LISTMANAGER_PARAM_PREOPEN_ARCHIVE_HELP_0,
            CheckBox.class,
            true)};

    /** Container item property key. */
    private static final String RANK_PROP = "rank";

    /** The serial version id. */
    private static final long serialVersionUID = 2345799706922671537L;

    /** The available sort options. */
    protected static final String[][] SORT_OPTIONS = new String[][] {
        {
            FIELD_DATE + " asc",
            FIELD_DATE + " desc",
            "disptitle_%s_s asc",
            "disptitle_%s_s desc",
            "newsorder_%s_i asc",
            "newsorder_%s_i desc"},
        {
            Messages.GUI_LISTMANAGER_SORT_DATE_ASC_0,
            Messages.GUI_LISTMANAGER_SORT_DATE_DESC_0,
            Messages.GUI_LISTMANAGER_SORT_TITLE_ASC_0,
            Messages.GUI_LISTMANAGER_SORT_TITLE_DESC_0,
            Messages.GUI_LISTMANAGER_SORT_ORDER_ASC_0,
            Messages.GUI_LISTMANAGER_SORT_ORDER_DESC_0}};

    /** Container item property key. */
    private static final String TITLE_PROP = "title";

    /** Container item property key. */
    private static final String TYPE_PROP = "type";

    /** The add folder button. */
    private Button m_addFolder;

    /** The add type button. */
    private Button m_addType;

    /** The resource blacklist. */
    private List<CmsUUID> m_blacklist;

    /** The clear blacklisted resources button. */
    private Button m_clearBlacklist;

    /** The currently edited configuration resource. */
    private CmsResource m_currentResource;

    /** The configuration form fields. */
    private Map<String, Field<?>> m_fields;

    /** The resources form layout. */
    private VerticalLayout m_folders;

    /** The form layout. */
    private FormLayout m_formLayout;

    /** The form layout for the advanced tab. */
    private FormLayout m_formLayoutAdvanced;

    /** The current lock action. */
    private CmsLockActionRecord m_lockAction;

    /** The list manager instance. */
    CmsListManager m_manager;

    /** The save configuration button. */
    private Button m_ok;

    /** The cancel edit button. */
    private Button m_cancel;

    /** The title field. */
    private TextField m_title;

    /** The types layout. */
    private VerticalLayout m_types;

    /**
     * Constructor.<p>
     *
     * @param manager the list manager instance
     */
    public CmsListConfigurationForm(CmsListManager manager) {
        m_blacklist = new ArrayList<CmsUUID>();

        CmsVaadinUtils.readAndLocalizeDesign(this, CmsVaadinUtils.getWpMessagesForCurrentLocale(), null);

        m_fields = new HashMap<String, Field<?>>();
        m_fields.put(N_TITLE, m_title);
        initParamFields();
        m_manager = manager;
        m_ok.addClickListener(new ClickListener() {

            private static final long serialVersionUID = 1L;

            public void buttonClick(ClickEvent event) {

                saveContent(false);
            }
        });
        m_cancel.addClickListener(new ClickListener() {

            private static final long serialVersionUID = 1L;

            public void buttonClick(ClickEvent event) {

                m_manager.closeEditDialog();
            }
        });
        m_addType.addClickListener(new ClickListener() {

            private static final long serialVersionUID = 1L;

            public void buttonClick(ClickEvent event) {

                addTypeField(null);
            }
        });
        addTypeField(null);
        m_addFolder.addClickListener(new ClickListener() {

            private static final long serialVersionUID = 1L;

            public void buttonClick(ClickEvent event) {

                addFolderField(null);
            }
        });
        m_clearBlacklist.addClickListener(new ClickListener() {

            private static final long serialVersionUID = 1L;

            public void buttonClick(ClickEvent event) {

                clearBlacklist();
            }
        });
        prepareSortOrder();
    }

    /**
     * Adds a resource id to the blacklist.<p>
     *
     * @param resourceId the resource id to add
     */
    public void addToBlacklist(CmsUUID resourceId) {

        m_blacklist.add(resourceId);
    }

    /**
     * Initializes the form fields with the given resource.<p>
     *
     * @param res the list configuration resource
     */
    @SuppressWarnings("unchecked")
    public void initFormValues(CmsResource res) {

        CmsObject cms = A_CmsUI.getCmsObject();
        m_currentResource = res;
        this.displayResourceInfo(Collections.singletonList(m_currentResource));
        try {
            m_lockAction = CmsLockUtil.ensureLock(cms, m_currentResource);
            CmsFile configFile = cms.readFile(m_currentResource);
            CmsXmlContent content = CmsXmlContentFactory.unmarshal(cms, configFile);
            Locale locale = CmsLocaleManager.MASTER_LOCALE;

            if (!content.hasLocale(locale)) {
                locale = content.getLocales().get(0);
            }
            for (Entry<String, Field<?>> fieldEntry : m_fields.entrySet()) {
                String val = content.getStringValue(cms, fieldEntry.getKey(), locale);
                if (fieldEntry.getValue().getValue() instanceof Boolean) {
                    ((Field<Boolean>)fieldEntry.getValue()).setValue(Boolean.valueOf(val));
                } else {
                    if (val == null) {
                        val = "";
                    }
                    ((Field<String>)fieldEntry.getValue()).setValue(val);
                }
            }
            m_types.removeAllComponents();
            List<I_CmsXmlContentValue> typeValues = content.getValues(N_DISPLAY_TYPE, locale);
            if (!typeValues.isEmpty()) {
                for (I_CmsXmlContentValue value : typeValues) {
                    String val = value.getStringValue(cms);
                    addTypeField(val);
                }
            } else {
                addTypeField(null);
            }

            m_folders.removeAllComponents();
            List<I_CmsXmlContentValue> folderValues = content.getValues(N_SEARCH_FOLDER, locale);
            if (!folderValues.isEmpty()) {
                for (I_CmsXmlContentValue value : folderValues) {
                    String val = value.getStringValue(cms);
                    // we are using root paths
                    addFolderField(cms.getRequestContext().addSiteRoot(val));
                }
            } else {
                addFolderField(null);
            }
            m_blacklist.clear();
            List<I_CmsXmlContentValue> blacklistValues = content.getValues(N_BLACKLIST, locale);
            if (!blacklistValues.isEmpty()) {
                for (I_CmsXmlContentValue value : blacklistValues) {
                    CmsLink link = ((CmsXmlVfsFileValue)value).getLink(cms);
                    if (link != null) {
                        m_blacklist.add(link.getStructureId());
                    }
                }
            }
        } catch (CmsException e) {
            e.printStackTrace();
        }
        updateSaveButtons();
    }

    /**
     * Adds the given structure id to the blacklist.<p>
     *
     * @param structureId the structure id to add
     */
    protected void blacklistResource(CmsUUID structureId) {

        m_blacklist.add(structureId);
    }

    /**
     * Checks whether the given ID is blacklisted.<p>
     *
     * @param structureId the id to check
     *
     * @return <code>true</code> in case the id is blacklisted
     */
    protected boolean isBlacklisted(CmsUUID structureId) {

        return m_blacklist.contains(structureId);
    }

    /**
     * Removes the given id from the blacklist.<p>
     *
     * @param structureId the id to remove
     */
    protected void removeFromBlacklist(CmsUUID structureId) {

        m_blacklist.remove(structureId);
    }

    /**
     * Adds a new resource field.<p>
     *
     * @param value the value to set
     */
    void addFolderField(String value) {

        m_folders.setVisible(true);
        CmsPathSelectField field = new CmsPathSelectField();
        field.setUseRootPaths(true);
        field.setResourceFilter(CmsResourceFilter.ONLY_VISIBLE_NO_DELETED.addRequireFolder());
        if (value != null) {
            field.setValue(value);
        }
        CmsRemovableFormRow<CmsPathSelectField> row = new CmsRemovableFormRow<CmsPathSelectField>(
            field,
            CmsVaadinUtils.getMessageText(Messages.GUI_PROJECTS_REMOVE_RESOURCE_0));
        row.setCaption(CmsVaadinUtils.getMessageText(Messages.GUI_LISTMANAGER_FOLDER_0));
        row.setDescription(CmsVaadinUtils.getMessageText(Messages.GUI_LISTMANAGER_FOLDER_HELP_0));
        m_folders.addComponent(row);
    }

    /**
     * Adds a new resource field.<p>
     *
     * @param value the value to set
     */
    void addTypeField(String value) {

        ComboBox field = new ComboBox();
        field.setContainerDataSource(getDisplayTypeContainer());
        field.setItemCaptionPropertyId(TITLE_PROP);
        field.setItemIconPropertyId(ICON_PROP);
        field.setNullSelectionAllowed(false);
        CmsRemovableFormRow<ComboBox> row = new CmsRemovableFormRow<ComboBox>(
            field,
            CmsVaadinUtils.getMessageText(Messages.GUI_LISTMANAGER_REMOVE_TYPE_0));
        row.setCaption(CmsVaadinUtils.getMessageText(Messages.GUI_LISTMANAGER_TYPE_0));
        row.setDescription(CmsVaadinUtils.getMessageText(Messages.GUI_LISTMANAGER_TYPE_HELP_0));
        if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(value) && (field.getItem(value) != null)) {
            field.setValue(value);
        }
        m_types.addComponent(row);
    }

    /**
     * Clears the resource blacklist.<p>
     */
    void clearBlacklist() {

        m_blacklist.clear();
    }

    /**
     * Returns the available display types container.<p>
     *
     * @return the available display types container
     */
    IndexedContainer getDisplayTypeContainer() {

        CmsObject cms = A_CmsUI.getCmsObject();
        Locale wpLocale = UI.getCurrent().getLocale();
        CmsADEConfigData config = OpenCms.getADEManager().lookupConfiguration(cms, "/");
        IndexedContainer container = new IndexedContainer();
        container.addContainerProperty(RANK_PROP, Integer.class, null);
        container.addContainerProperty(TITLE_PROP, String.class, null);
        container.addContainerProperty(TYPE_PROP, String.class, null);
        container.addContainerProperty(FORMATTER_PROP, String.class, null);
        container.addContainerProperty(ICON_PROP, Resource.class, null);
        if (config != null) {
            for (I_CmsFormatterBean formatter : config.getDisplayFormatters(cms)) {
                for (String typeName : formatter.getResourceTypeNames()) {
                    String id = typeName + CmsXmlDisplayFormatterValue.SEPARATOR + formatter.getId();
                    Item item = container.addItem(id);
                    item.getItemProperty(FORMATTER_PROP).setValue(formatter.getId());
                    item.getItemProperty(RANK_PROP).setValue(Integer.valueOf(formatter.getRank()));
                    item.getItemProperty(TITLE_PROP).setValue(
                        formatter.getNiceName(wpLocale)
                            + " ("
                            + CmsWorkplaceMessages.getResourceTypeName(wpLocale, typeName)
                            + ")");
                    item.getItemProperty(TYPE_PROP).setValue(typeName);
                    CmsExplorerTypeSettings typeSetting = OpenCms.getWorkplaceManager().getExplorerTypeSetting(
                        typeName);
                    if (typeSetting != null) {
                        item.getItemProperty(ICON_PROP).setValue(
                            CmsResourceUtil.getSmallIconResource(typeSetting, null));
                    }
                }
            }
        }
        container.sort(new Object[] {TYPE_PROP, RANK_PROP}, new boolean[] {true, false});
        return container;
    }

    /**
     * Prepares the sort order select component.<p>
     */
    void prepareSortOrder() {

        ComboBox sortOrder = (ComboBox)m_fields.get(N_SORT_ORDER);
        sortOrder.setNullSelectionAllowed(false);
        for (int i = 0; i < SORT_OPTIONS[0].length; i++) {
            sortOrder.addItem(SORT_OPTIONS[0][i]);
            sortOrder.setItemCaption(SORT_OPTIONS[0][i], CmsVaadinUtils.getMessageText(SORT_OPTIONS[1][i]));
        }
        sortOrder.setValue(SORT_OPTIONS[0][0]);
    }

    /**
     * Resets the form fields.<p>
     */
    @SuppressWarnings("unchecked")
    void resetFormValues() {

        tryUnlockCurrent();
        m_currentResource = null;
        m_lockAction = null;
        updateSaveButtons();
        m_ok.setEnabled(false);
        for (Entry<String, Field<?>> fieldEntry : m_fields.entrySet()) {
            Object value = fieldEntry.getValue().getValue();
            if (value != null) {
                if (value instanceof Boolean) {
                    ((Field<Boolean>)fieldEntry.getValue()).setValue(Boolean.FALSE);
                } else if (fieldEntry.getValue() instanceof ComboBox) {
                    ComboBox field = (ComboBox)fieldEntry.getValue();
                    field.setValue(field.getItemIds().iterator().next());
                } else if (value instanceof String) {
                    ((Field<String>)fieldEntry.getValue()).setValue("");
                } else {
                    fieldEntry.getValue().setValue(null);
                }
            }
        }
        m_folders.removeAllComponents();
        m_types.removeAllComponents();
        addTypeField(null);
    }

    /**
     * Saves the current list configuration.<p>
     *
     * @param asNew to create a new resource
     */
    void saveContent(boolean asNew) {

        m_manager.saveContent(getConfigBean(), asNew);
    }

    /**
     * Tries to unlocks the current resource if available.<p>
     */
    void tryUnlockCurrent() {

        if ((m_lockAction != null) && m_lockAction.getChange().equals(CmsLockActionRecord.LockChange.locked)) {
            try {
                A_CmsUI.getCmsObject().unlockResource(m_currentResource);
            } catch (CmsException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Returns the configuration data bean according to the form settings.<p>
     *
     * @return the configuration data
     */
    private ListConfigurationBean getConfigBean() {

        ListConfigurationBean result = new ListConfigurationBean();
        for (Entry<String, Field<?>> fieldEntry : m_fields.entrySet()) {
            Object value = fieldEntry.getValue().getValue();
            if (N_CATEGORY.equals(fieldEntry.getKey())) {
                if (CmsStringUtil.isNotEmptyOrWhitespaceOnly((String)value)) {
                    result.setCategories(Arrays.asList(((String)value).split(",")));
                } else {
                    result.setCategories(Collections.<String> emptyList());
                }
            } else {
                if (((value instanceof Boolean) && ((Boolean)value).booleanValue())
                    || ((value instanceof String) && CmsStringUtil.isNotEmptyOrWhitespaceOnly((String)value))) {
                    result.setParameterValue(fieldEntry.getKey(), String.valueOf(value));
                }
            }
        }
        result.setDisplayTypes(getSelectedDisplayTypes());
        result.setFolders(getSelectedFolders());
        result.setBlacklist(m_blacklist);
        return result;
    }

    /**
     * Returns the selected display types.<p>
     *
     * @return the selected display type names
     */
    private List<String> getSelectedDisplayTypes() {

        List<String> displayTypes = new ArrayList<String>();
        for (Component c : m_types) {
            if (c instanceof CmsRemovableFormRow<?>) {
                @SuppressWarnings("unchecked")
                ComboBox field = ((CmsRemovableFormRow<ComboBox>)c).getInput();
                String value = (String)field.getValue();
                if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(value)) {
                    displayTypes.add(value);
                }
            }
        }
        return displayTypes;
    }

    /**
     * Returns the selected folder root paths.<p>
     *
     * @return the selected folder root paths
     */
    private List<String> getSelectedFolders() {

        List<String> folders = new ArrayList<String>();
        for (Component c : m_folders) {
            if (c instanceof CmsRemovableFormRow<?>) {
                @SuppressWarnings("unchecked")
                String value = ((CmsRemovableFormRow<CmsPathSelectField>)c).getInput().getValue();
                if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(value)) {
                    folders.add(value);
                }
            }
        }
        return folders;
    }

    /**
     * Initializes the parameter form fields.<p>
     */
    private void initParamFields() {

        for (ParameterField field : PARAMETER_FIELDS) {
            try {
                Component comp = (Component)field.m_fieldType.newInstance();
                if (!(comp instanceof Field)) {
                    throw new RuntimeException(
                        "Invalid field type. '"
                            + field.m_fieldType.getName()
                            + "' does not implement '"
                            + Field.class.getName()
                            + "'.");
                }
                comp.setCaption(CmsVaadinUtils.getMessageText(field.m_captionKey));
                comp.setWidth("715px");
                if (comp instanceof AbstractComponent) {
                    ((AbstractComponent)comp).setDescription(CmsVaadinUtils.getMessageText(field.m_decriptionKey));
                }
                if (field.m_useAdvancedTab) {
                    m_formLayoutAdvanced.addComponent(comp);
                } else {
                    m_formLayout.addComponent(comp);
                }
                m_fields.put(field.m_key, (Field<?>)comp);
            } catch (Exception e) {
                LOG.error(e.getLocalizedMessage(), e);
            }
        }
    }

    /**
     * Updates the save button status.<p>
     */
    private void updateSaveButtons() {

        boolean saveEnabled = true;
        if (m_currentResource == null) {
            saveEnabled = false;
        } else {
            CmsResourceUtil resUtil = new CmsResourceUtil(A_CmsUI.getCmsObject(), m_currentResource);
            saveEnabled = resUtil.isEditable();
        }
        m_ok.setEnabled(saveEnabled);
    }
}
