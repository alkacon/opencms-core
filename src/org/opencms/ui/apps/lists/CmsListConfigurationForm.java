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
import org.opencms.ade.configuration.CmsResourceTypeConfig;
import org.opencms.file.CmsFile;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsResource;
import org.opencms.file.CmsResourceFilter;
import org.opencms.i18n.CmsLocaleManager;
import org.opencms.jsp.search.config.CmsSearchConfiguration;
import org.opencms.jsp.search.config.CmsSearchConfigurationFacetField;
import org.opencms.jsp.search.config.CmsSearchConfigurationFacetRange;
import org.opencms.jsp.search.config.CmsSearchConfigurationPagination;
import org.opencms.jsp.search.config.CmsSearchConfigurationSortOption;
import org.opencms.jsp.search.config.CmsSearchConfigurationSorting;
import org.opencms.jsp.search.config.I_CmsSearchConfigurationCommon;
import org.opencms.jsp.search.config.I_CmsSearchConfigurationDidYouMean;
import org.opencms.jsp.search.config.I_CmsSearchConfigurationFacet.SortOrder;
import org.opencms.jsp.search.config.I_CmsSearchConfigurationFacetField;
import org.opencms.jsp.search.config.I_CmsSearchConfigurationFacetQuery;
import org.opencms.jsp.search.config.I_CmsSearchConfigurationFacetRange;
import org.opencms.jsp.search.config.I_CmsSearchConfigurationHighlighting;
import org.opencms.jsp.search.config.I_CmsSearchConfigurationPagination;
import org.opencms.jsp.search.config.I_CmsSearchConfigurationSortOption;
import org.opencms.jsp.search.config.I_CmsSearchConfigurationSorting;
import org.opencms.jsp.search.config.parser.I_CmsSearchConfigurationParser;
import org.opencms.jsp.search.controller.CmsSearchController;
import org.opencms.jsp.search.controller.I_CmsSearchControllerFacetField;
import org.opencms.jsp.search.controller.I_CmsSearchControllerFacetRange;
import org.opencms.jsp.search.result.CmsSearchResultWrapper;
import org.opencms.jsp.search.state.I_CmsSearchStateFacet;
import org.opencms.lock.CmsLockActionRecord;
import org.opencms.lock.CmsLockUtil;
import org.opencms.main.CmsException;
import org.opencms.main.CmsLog;
import org.opencms.main.OpenCms;
import org.opencms.relations.CmsCategory;
import org.opencms.relations.CmsCategoryService;
import org.opencms.search.CmsSearchException;
import org.opencms.search.solr.CmsSolrIndex;
import org.opencms.search.solr.CmsSolrQuery;
import org.opencms.search.solr.CmsSolrResultList;
import org.opencms.ui.A_CmsUI;
import org.opencms.ui.CmsVaadinUtils;
import org.opencms.ui.apps.Messages;
import org.opencms.ui.apps.projects.CmsEditProjectForm;
import org.opencms.ui.components.CmsErrorDialog;
import org.opencms.ui.components.CmsRemovableFormRow;
import org.opencms.ui.components.OpenCmsTheme;
import org.opencms.ui.components.fileselect.CmsPathSelectField;
import org.opencms.util.CmsStringUtil;
import org.opencms.workplace.CmsWorkplace;
import org.opencms.workplace.CmsWorkplaceMessages;
import org.opencms.workplace.explorer.CmsExplorerTypeSettings;
import org.opencms.workplace.explorer.CmsResourceUtil;
import org.opencms.xml.containerpage.I_CmsFormatterBean;
import org.opencms.xml.content.CmsXmlContent;
import org.opencms.xml.content.CmsXmlContentFactory;
import org.opencms.xml.types.CmsXmlDisplayFormatterValue;
import org.opencms.xml.types.I_CmsXmlContentValue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.logging.Log;
import org.apache.solr.client.solrj.response.FacetField;
import org.apache.solr.client.solrj.response.FacetField.Count;
import org.apache.solr.client.solrj.response.RangeFacet;

import com.vaadin.data.Item;
import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.Property.ValueChangeListener;
import com.vaadin.data.util.IndexedContainer;
import com.vaadin.server.ExternalResource;
import com.vaadin.server.Resource;
import com.vaadin.ui.AbstractComponent;
import com.vaadin.ui.Accordion;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.CheckBox;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.Component;
import com.vaadin.ui.Field;
import com.vaadin.ui.FormLayout;
import com.vaadin.ui.GridLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Panel;
import com.vaadin.ui.TextField;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.ValoTheme;

/**
 * The list configuration edit form.<p>
 */
public class CmsListConfigurationForm extends Accordion {

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

        /**
         * Constructor.<p>
         *
         * @param key the field key
         * @param captionKey the caption message key
         * @param descriptionKey the description message key
         * @param fieldType the field component class
         */
        public ParameterField(String key, String captionKey, String descriptionKey, Class<?> fieldType) {
            m_key = key;
            m_captionKey = captionKey;
            m_decriptionKey = descriptionKey;
            m_fieldType = fieldType;
        }
    }

    /**
     * Reads the search configuration from the current list configuration form.<p>
     */
    class SearchConfigParser implements I_CmsSearchConfigurationParser {

        /**
         * @see org.opencms.jsp.search.config.parser.I_CmsSearchConfigurationParser#parseCommon()
         */
        public I_CmsSearchConfigurationCommon parseCommon() {

            return new I_CmsSearchConfigurationCommon() {

                public Map<String, String> getAdditionalParameters() {

                    return null;
                }

                public boolean getEscapeQueryChars() {

                    return false;
                }

                public String getExtraSolrParams() {

                    return getFolderFilter() + getResourceTypeFilter() + getCategoryFilter() + getFilterQuery();
                }

                public boolean getIgnoreExpirationDate() {

                    return true;
                }

                public boolean getIgnoreQueryParam() {

                    return false;
                }

                public boolean getIgnoreReleaseDate() {

                    return true;
                }

                public String getLastQueryParam() {

                    return null;
                }

                public String getModifiedQuery(String queryString) {

                    return "{!type=edismax qf=\"content_${cms.locale} Title_prop spell\"}" + queryString;
                }

                public String getQueryParam() {

                    return null;
                }

                public String getReloadedParam() {

                    return null;
                }

                public boolean getSearchForEmptyQueryParam() {

                    return true;
                }

                public String getSolrCore() {

                    return null;
                }

                public String getSolrIndex() {

                    return null;
                }
            };
        }

        /**
         * @see org.opencms.jsp.search.config.parser.I_CmsSearchConfigurationParser#parseDidYouMean()
         */
        public I_CmsSearchConfigurationDidYouMean parseDidYouMean() {

            return null;
        }

        /**
         * @see org.opencms.jsp.search.config.parser.I_CmsSearchConfigurationParser#parseFieldFacets()
         */
        public Map<String, I_CmsSearchConfigurationFacetField> parseFieldFacets() {

            Map<String, I_CmsSearchConfigurationFacetField> result = new HashMap<String, I_CmsSearchConfigurationFacetField>();
            result.put(
                FIELD_CATEGORIES,
                new CmsSearchConfigurationFacetField(
                    FIELD_CATEGORIES,
                    null,
                    Integer.valueOf(1),
                    Integer.valueOf(200),
                    null,
                    "Category",
                    SortOrder.index,
                    null,
                    Boolean.FALSE,
                    null,
                    Boolean.TRUE));
            result.put(
                FIELD_PARENT_FOLDERS,
                new CmsSearchConfigurationFacetField(
                    FIELD_PARENT_FOLDERS,
                    null,
                    Integer.valueOf(1),
                    Integer.valueOf(200),
                    null,
                    "Folders",
                    SortOrder.index,
                    null,
                    Boolean.FALSE,
                    null,
                    Boolean.TRUE));
            return result;
        }

        /**
         * @see org.opencms.jsp.search.config.parser.I_CmsSearchConfigurationParser#parseHighlighter()
         */
        public I_CmsSearchConfigurationHighlighting parseHighlighter() {

            return null;
        }

        /**
         * @see org.opencms.jsp.search.config.parser.I_CmsSearchConfigurationParser#parsePagination()
         */
        public I_CmsSearchConfigurationPagination parsePagination() {

            return new CmsSearchConfigurationPagination(null, Integer.valueOf(10000), Integer.valueOf(1));
        }

        /**
         * @see org.opencms.jsp.search.config.parser.I_CmsSearchConfigurationParser#parseQueryFacet()
         */
        public I_CmsSearchConfigurationFacetQuery parseQueryFacet() {

            return null;
        }

        /**
         * @see org.opencms.jsp.search.config.parser.I_CmsSearchConfigurationParser#parseRangeFacets()
         */
        public Map<String, I_CmsSearchConfigurationFacetRange> parseRangeFacets() {

            Map<String, I_CmsSearchConfigurationFacetRange> result = new HashMap<String, I_CmsSearchConfigurationFacetRange>();
            I_CmsSearchConfigurationFacetRange rangeFacet = new CmsSearchConfigurationFacetRange(
                String.format(FIELD_DATE, getContentLocale().toString()),
                "NOW/YEAR-20YEARS",
                "NOW/MONTH+2YEARS",
                "+1MONTHS",
                null,
                Boolean.FALSE,
                "newsdate",
                Integer.valueOf(1),
                "Date",
                Boolean.FALSE,
                null,
                Boolean.TRUE);

            result.put(rangeFacet.getName(), rangeFacet);

            return result;
        }

        /**
         * @see org.opencms.jsp.search.config.parser.I_CmsSearchConfigurationParser#parseSorting()
         */
        public I_CmsSearchConfigurationSorting parseSorting() {

            return getSortOptions();
        }
    }

    /** SOLR field name. */
    public static final String FIELD_CATEGORIES = "category_exact";

    /** SOLR field name. */
    public static final String FIELD_DATE = "newsdate_%s_dt";

    /** SOLR field name. */
    public static final String FIELD_DATE_FACET_NAME = "newsdate";

    /** SOLR field name. */
    public static final String FIELD_PARENT_FOLDERS = "parent-folders";

    /** Container item property key. */
    private static final String FORMATTER_PROP = "formatter";

    /** Container item property key. */
    private static final String ICON_PROP = "icon";

    /** The logger for this class. */
    private static Log LOG = CmsLog.getLog(CmsEditProjectForm.class.getName());

    /** The month name abbreviations. */
    private static final String[] MONTHS = new String[] {
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

    /** List configuration node name and field key. */
    private static final String N_CATEGORY = "Category";

    /** List configuration node name and field key. */
    private static final String N_CATEGORY_FILTERS = "CategoryFilters";

    /** List configuration node name and field key. */
    private static final String N_CATEGORY_FULL_PATH = "CategoryFullPath";

    /** List configuration node name and field key. */
    private static final String N_CATEGORY_ONLY_LEAFS = "CategoryOnlyLeafs";

    /** List configuration node name and field key. */
    private static final String N_DISPLAY_OPTIONS = "DisplayOptions";

    /** List configuration node name and field key. */
    private static final String N_DISPLAY_TYPE = "TypesToCollect";

    /** List configuration node name and field key. */
    private static final String N_FILTER_QUERY = "FilterQuery";

    /** List configuration node name and field key. */
    private static final String N_PREOPEN_ARCHIVE = "PreopenArchive";

    /** List configuration node name and field key. */
    private static final String N_PREOPEN_CATEGORIES = "PreopenCategories";

    /** List configuration node name and field key. */
    private static final String N_SEARCH_FOLDER = "SearchFolder";

    /** List configuration node name and field key. */
    private static final String N_SHOW_DATE = "ShowDate";

    /** List configuration node name and field key. */
    private static final String N_SHOW_EXPIRED = "ShowExpired";

    /** List configuration node name and field key. */
    private static final String N_SORT_ORDER = "SortOrder";

    /** List configuration node name and field key. */
    private static final String N_TITLE = "Title";

    /** The parameter fields. */
    private static final ParameterField[] PARAMETER_FIELDS = new ParameterField[] {
        new ParameterField(
            N_CATEGORY,
            Messages.GUI_LISTMANAGER_PARAM_CATEGORY_0,
            Messages.GUI_LISTMANAGER_PARAM_CATEGORY_HELP_0,
            TextField.class),
        new ParameterField(
            N_FILTER_QUERY,
            Messages.GUI_LISTMANAGER_PARAM_FILTER_QUERY_0,
            Messages.GUI_LISTMANAGER_PARAM_FILTER_QUERY_HELP_0,
            TextField.class),
        new ParameterField(
            N_SORT_ORDER,
            Messages.GUI_LISTMANAGER_PARAM_SORT_ORDER_0,
            Messages.GUI_LISTMANAGER_PARAM_SORT_ORDER_HELP_0,
            ComboBox.class),
        new ParameterField(
            N_SHOW_DATE,
            Messages.GUI_LISTMANAGER_PARAM_SHOW_DATE_0,
            Messages.GUI_LISTMANAGER_PARAM_SHOW_DATE_HELP_0,
            CheckBox.class),
        new ParameterField(
            N_SHOW_EXPIRED,
            Messages.GUI_LISTMANAGER_PARAM_SHOW_EXPIRED_0,
            Messages.GUI_LISTMANAGER_PARAM_SHOW_EXPIRED_HELP_0,
            CheckBox.class),
        new ParameterField(
            N_DISPLAY_OPTIONS,
            Messages.GUI_LISTMANAGER_PARAM_DISPLAY_OPTIONS_0,
            Messages.GUI_LISTMANAGER_PARAM_DISPLAY_OPTIONS_HELP_0,
            TextField.class),
        new ParameterField(
            N_CATEGORY_FILTERS,
            Messages.GUI_LISTMANAGER_PARAM_CATEGORY_FILTERS_0,
            Messages.GUI_LISTMANAGER_PARAM_CATEGORY_FILTERS_HELP_0,
            TextField.class),
        new ParameterField(
            N_CATEGORY_FULL_PATH,
            Messages.GUI_LISTMANAGER_PARAM_FULL_CATEGORY_PATHS_0,
            Messages.GUI_LISTMANAGER_PARAM_FULL_CATEGORY_PATHS_HELP_0,
            CheckBox.class),
        new ParameterField(
            N_CATEGORY_ONLY_LEAFS,
            Messages.GUI_LISTMANAGER_PARAM_LEAFS_ONLY_0,
            Messages.GUI_LISTMANAGER_PARAM_LEAFS_ONLY_HELP_0,
            CheckBox.class),
        new ParameterField(
            N_PREOPEN_CATEGORIES,
            Messages.GUI_LISTMANAGER_PARAM_PREOPEN_CATEGORIES_0,
            Messages.GUI_LISTMANAGER_PARAM_PREOPEN_CATEGORIES_HELP_0,
            CheckBox.class),
        new ParameterField(
            N_PREOPEN_ARCHIVE,
            Messages.GUI_LISTMANAGER_PARAM_PREOPEN_ARCHIVE_0,
            Messages.GUI_LISTMANAGER_PARAM_PREOPEN_ARCHIVE_HELP_0,
            CheckBox.class)};

    /** Container item property key. */
    private static final String RANK_PROP = "rank";

    /** The serial version id. */
    private static final long serialVersionUID = 2345799706922671537L;

    /** The available sort options. */
    private static final String[][] SORT_OPTIONS = new String[][] {
        {
            "newsdate_%s_dt asc",
            "newsdate_%s_dt desc",
            "disptitle_%s_s asc",
            "disptitle_%s_s desc",
            "newsorder_%s_i asc",
            "newsorder_%s_i desc"},
        {
            "Date ascending",
            "Date descending",
            "Title ascending",
            "Title descending",
            "Order ascending",
            "Order descending"}};

    /** Container item property key. */
    private static final String TITLE_PROP = "title";

    /** Container item property key. */
    private static final String TYPE_PROP = "type";

    /** The add folder button. */
    private Button m_addFolder;

    /** The add type button. */
    private Button m_addType;

    /** The currently edited configuration resource. */
    private CmsResource m_currentResource;

    /** The facets layout. */
    private VerticalLayout m_facetsLayout;

    /** The configuration form felds. */
    private Map<String, Field<?>> m_fields;

    /** The resources form layout. */
    private VerticalLayout m_folders;

    /** The form layout. */
    private FormLayout m_formLayout;

    /** The current lock action. */
    private CmsLockActionRecord m_lockAction;

    /** The list manager instance. */
    private CmsListManager m_manager;

    /** The sort select. */
    private ComboBox m_resultSorter;

    /** The save configuration button. */
    private Button m_save;

    /** The save configuration as new content button. */
    private Button m_saveNew;

    /** The search button. */
    private Button m_search;

    /** The selected field facets. */
    private Map<String, List<String>> m_selectedFieldFacets;

    /** The selected range facets. */
    private Map<String, List<String>> m_selectedRangeFacets;

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
        m_resultSorter = new ComboBox();
        m_resultSorter.setNullSelectionAllowed(false);
        m_resultSorter.setWidth("200px");
        m_resultSorter.addValueChangeListener(new ValueChangeListener() {

            private static final long serialVersionUID = 1L;

            public void valueChange(ValueChangeEvent event) {

                search(false, false);
            }
        });

        CmsVaadinUtils.readAndLocalizeDesign(this, CmsVaadinUtils.getWpMessagesForCurrentLocale(), null);
        m_fields = new HashMap<String, Field<?>>();
        m_fields.put(N_TITLE, m_title);
        initParamFields();
        m_selectedFieldFacets = new HashMap<String, List<String>>();
        m_selectedRangeFacets = new HashMap<String, List<String>>();
        m_manager = manager;
        m_search.addClickListener(new ClickListener() {

            private static final long serialVersionUID = 1L;

            public void buttonClick(ClickEvent event) {

                updateSortOptions();
                search(true, true);
            }
        });
        m_saveNew.addClickListener(new ClickListener() {

            private static final long serialVersionUID = 1L;

            public void buttonClick(ClickEvent event) {

                saveContent(true);
            }
        });
        m_save.addClickListener(new ClickListener() {

            private static final long serialVersionUID = 1L;

            public void buttonClick(ClickEvent event) {

                saveContent(false);
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
        prepareSortOrder();
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
        try {
            m_lockAction = CmsLockUtil.ensureLock(cms, m_currentResource);
            CmsFile configFile = cms.readFile(m_currentResource);
            CmsXmlContent content = CmsXmlContentFactory.unmarshal(cms, configFile);
            Locale locale = CmsLocaleManager.getLocale("en");

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
            search(true, true);
        } catch (CmsException e) {
            e.printStackTrace();
        }
        updateSaveButtons();
    }

    /**
     * Returns the sort select component.<p>
     *
     * @return the sort select component
     */
    protected ComboBox getResultSorter() {

        return m_resultSorter;
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
     * Returns the category filter query part.<p>
     *
     * @return the category filter query part
     */
    String getCategoryFilter() {

        String result = "";
        @SuppressWarnings("unchecked")
        Field<String> categoryField = (Field<String>)m_fields.get(N_CATEGORY);
        if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(categoryField.getValue())) {
            result = "&fq=category_exact:(";
            for (String path : categoryField.getValue().split(",")) {
                result += path + "&";
            }
            result = result.substring(0, result.length() - 1);
            result += ")";
        }
        return result;
    }

    /**
     * Returns the content locale.<p>
     *
     * @return the content locale
     */
    Locale getContentLocale() {

        // TODO: get locale from UI
        return UI.getCurrent().getLocale();
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
                            new ExternalResource(
                                CmsWorkplace.getResourceUri(CmsWorkplace.RES_PATH_FILETYPES + typeSetting.getIcon())));
                    }
                }
            }
        }
        container.sort(new Object[] {TYPE_PROP, RANK_PROP}, new boolean[] {true, false});
        return container;
    }

    /**
     * Returns the additional filter query part.<p>
     *
     * @return the additional filter query part
     */
    String getFilterQuery() {

        @SuppressWarnings("unchecked")
        Field<String> filterField = (Field<String>)m_fields.get(N_FILTER_QUERY);
        String result = filterField.getValue();
        if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(result) && !result.startsWith("&")) {
            result = "&" + result;
        }
        return result;
    }

    /**
     * Returns the folder filter query part.<p>
     *
     * @return the folder filter query part
     */
    String getFolderFilter() {

        String result = "";
        boolean first = true;
        for (String value : getSelectedFolders()) {
            if (!first) {
                result += " OR ";
            }
            result += "\"" + value + "\"";
            first = false;
        }
        if (CmsStringUtil.isEmptyOrWhitespaceOnly(result)) {
            result = "fq=parent-folders:(\"/\")";
        } else {
            result = "fq=parent-folders:(" + result + ")";
        }
        return result;
    }

    /**
     * Returns the direct publish resources to the current search configuration.<p>
     *
     * @return the publish resources
     */
    List<CmsResource> getPublishResources() {

        List<CmsResource> result = new ArrayList<CmsResource>();
        if (m_currentResource != null) {
            result.add(m_currentResource);
        }
        CmsObject cms = A_CmsUI.getCmsObject();
        CmsSolrQuery query = getInitialQuery();
        CmsSearchController controller = new CmsSearchController(new CmsSearchConfiguration(new SearchConfigParser()));
        controller.getPagination().getState().setCurrentPage(1);
        controller.addQueryParts(query);

        CmsSolrIndex index = OpenCms.getSearchManager().getIndexSolr(
            cms.getRequestContext().getCurrentProject().isOnlineProject()
            ? CmsSolrIndex.DEFAULT_INDEX_NAME_ONLINE
            : CmsSolrIndex.DEFAULT_INDEX_NAME_OFFLINE);
        try {
            CmsSolrResultList solrResultList = index.search(cms, query, true, CmsResourceFilter.IGNORE_EXPIRATION);
            result.addAll(solrResultList);
        } catch (CmsSearchException e) {
            LOG.error("Error reading resources for publish.", e);
        }
        return result;
    }

    /**
     * Returns the resource type filter query part.<p>
     *
     * @return the resource type filter query part
     */
    String getResourceTypeFilter() {

        String result = "";
        boolean first = true;
        for (String value : getSelectedTypes()) {
            if (!first) {
                result += " OR ";
            }
            result += "\"" + value + "\"";
            first = false;
        }
        if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(result)) {
            result = "&fq=type:(" + result + ")";
        }

        return result;
    }

    /**
     * Returns the sort options.<p>
     *
     * @return the sort options
     */
    CmsSearchConfigurationSorting getSortOptions() {

        List<I_CmsSearchConfigurationSortOption> result = null;
        I_CmsSearchConfigurationSortOption defaultOption = null;
        if (CmsStringUtil.isNotEmptyOrWhitespaceOnly((String)m_resultSorter.getValue())) {
            defaultOption = new CmsSearchConfigurationSortOption(
                "",
                "",
                String.format((String)m_resultSorter.getValue(), getContentLocale().toString()));
            result = Collections.<I_CmsSearchConfigurationSortOption> singletonList(defaultOption);
        } else {
            result = Collections.<I_CmsSearchConfigurationSortOption> emptyList();
        }

        return new CmsSearchConfigurationSorting(null, result, defaultOption);
    }

    /**
     * Prepares the sort order select component.<p>
     */
    void prepareSortOrder() {

        ComboBox sortOrder = (ComboBox)m_fields.get(N_SORT_ORDER);
        sortOrder.setNullSelectionAllowed(false);
        for (int i = 0; i < SORT_OPTIONS[0].length; i++) {
            sortOrder.addItem(SORT_OPTIONS[0][i]);
            sortOrder.setItemCaption(SORT_OPTIONS[0][i], SORT_OPTIONS[1][i]);
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
        m_save.setEnabled(false);
        for (Entry<String, Field<?>> fieldEntry : m_fields.entrySet()) {
            Object value = fieldEntry.getValue().getValue();
            if (value instanceof Boolean) {
                ((Field<Boolean>)fieldEntry.getValue()).setValue(Boolean.FALSE);
            } else if (fieldEntry.getValue() instanceof ComboBox) {
                ComboBox field = (ComboBox)fieldEntry.getValue();
                field.setValue(field.getItemIds().iterator().next());
            } else if (value instanceof String) {
                ((Field<String>)fieldEntry.getValue()).setValue("");
            } else {
                fieldEntry.setValue(null);
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

        CmsObject cms = A_CmsUI.getCmsObject();
        if ((m_currentResource == null) || asNew) {
            String contextPath;
            List<String> folders = getSelectedFolders();
            if (m_currentResource != null) {
                contextPath = m_currentResource.getRootPath();
                tryUnlockCurrent();
            } else if (folders.isEmpty()) {
                contextPath = cms.getRequestContext().getSiteRoot();
            } else {
                contextPath = folders.get(0);
            }
            CmsADEConfigData configData = OpenCms.getADEManager().lookupConfiguration(cms, contextPath);
            CmsResourceTypeConfig typeConfig = configData.getResourceType(CmsListManager.RES_TYPE_LIST_CONFIG);
            if (typeConfig != null) {
                try {
                    m_currentResource = typeConfig.createNewElement(cms, contextPath);
                    m_lockAction = CmsLockUtil.ensureLock(cms, m_currentResource);
                } catch (CmsException e) {
                    CmsErrorDialog.showErrorDialog(e);
                    return;
                }
            }
        }
        if (m_currentResource != null) {
            try {
                CmsFile configFile = cms.readFile(m_currentResource);
                CmsXmlContent content = CmsXmlContentFactory.unmarshal(cms, configFile);
                Locale locale = CmsLocaleManager.getLocale("en");
                content.removeLocale(locale);
                content.addLocale(cms, locale);
                for (Entry<String, Field<?>> fieldEntry : m_fields.entrySet()) {
                    Object value = fieldEntry.getValue().getValue();
                    if (((value instanceof Boolean) && ((Boolean)value).booleanValue())
                        || ((value instanceof String) && CmsStringUtil.isNotEmptyOrWhitespaceOnly((String)value))) {
                        I_CmsXmlContentValue contentVal = content.getValue(fieldEntry.getKey(), locale);
                        if (contentVal == null) {
                            contentVal = content.addValue(cms, fieldEntry.getKey(), locale, 0);
                        }
                        contentVal.setStringValue(cms, String.valueOf(value));
                    }

                }
                int count = 0;
                for (String type : getSelectedDisplayTypes()) {
                    I_CmsXmlContentValue contentVal = content.getValue(N_DISPLAY_TYPE, locale, count);
                    if (contentVal == null) {
                        contentVal = content.addValue(cms, N_DISPLAY_TYPE, locale, count);
                    }
                    contentVal.setStringValue(cms, type);
                    count++;
                }
                count = 0;
                for (String folder : getSelectedFolders()) {
                    I_CmsXmlContentValue contentVal = content.getValue(N_SEARCH_FOLDER, locale, count);
                    if (contentVal == null) {
                        contentVal = content.addValue(cms, N_SEARCH_FOLDER, locale, count);
                    }
                    contentVal.setStringValue(cms, folder);
                    count++;
                }
                configFile.setContents(content.marshal());
                cms.writeFile(configFile);
            } catch (CmsException e) {
                e.printStackTrace();
            }
        }
        updateSaveButtons();
    }

    /**
     * Executes the search for the current configuration and facets.<p>
     *
     * @param resetFacets <code>true</code> to reset the selected facets
     * @param clearFilter <code>true</code> to clear the result filter
     */
    void search(boolean resetFacets, boolean clearFilter) {

        search(resetFacets, clearFilter, null);
    }

    /**
     * Executes the search for the current configuration and facets.<p>
     *
     * @param resetFacets <code>true</code> to reset the selected facets
     * @param clearFilter <code>true</code> to clear the result filter
     * @param additionalQuery the additional query string
     */
    void search(boolean resetFacets, boolean clearFilter, String additionalQuery) {

        if (resetFacets) {
            m_selectedFieldFacets.clear();
            m_selectedRangeFacets.clear();
        }
        CmsObject cms = A_CmsUI.getCmsObject();
        CmsSolrQuery query = getInitialQuery();
        CmsSearchController controller = new CmsSearchController(new CmsSearchConfiguration(new SearchConfigParser()));
        controller.getPagination().getState().setCurrentPage(1);
        Map<String, I_CmsSearchControllerFacetField> fieldFacetControllers = controller.getFieldFacets().getFieldFacetController();
        for (Entry<String, List<String>> facetEntry : m_selectedFieldFacets.entrySet()) {
            I_CmsSearchStateFacet state = fieldFacetControllers.get(facetEntry.getKey()).getState();
            state.clearChecked();
            for (String check : facetEntry.getValue()) {
                state.addChecked(check);
            }
        }
        Map<String, I_CmsSearchControllerFacetRange> rangeFacetControllers = controller.getRangeFacets().getRangeFacetController();
        for (Entry<String, List<String>> facetEntry : m_selectedRangeFacets.entrySet()) {
            I_CmsSearchStateFacet state = rangeFacetControllers.get(facetEntry.getKey()).getState();
            state.clearChecked();
            for (String check : facetEntry.getValue()) {
                state.addChecked(check);
            }
        }
        if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(additionalQuery)) {
            controller.getCommon().getState().setQuery(additionalQuery);
        }

        controller.addQueryParts(query);

        CmsSolrIndex index = OpenCms.getSearchManager().getIndexSolr(
            cms.getRequestContext().getCurrentProject().isOnlineProject()
            ? CmsSolrIndex.DEFAULT_INDEX_NAME_ONLINE
            : CmsSolrIndex.DEFAULT_INDEX_NAME_OFFLINE);
        try {
            CmsSolrResultList solrResultList = index.search(cms, query, true, CmsResourceFilter.IGNORE_EXPIRATION);
            m_manager.displayResult(solrResultList, clearFilter);
            displayFacetResult(
                solrResultList,
                new CmsSearchResultWrapper(controller, solrResultList, query, cms, null));
        } catch (CmsSearchException e) {
            CmsErrorDialog.showErrorDialog(e);

            LOG.error("Error executing search.", e);
        }
    }

    /**
     * Selects the given field facet.<p>
     *
     * @param field the field name
     * @param value the value
     */
    void selectFieldFacet(String field, String value) {

        m_selectedFieldFacets.clear();
        m_selectedRangeFacets.clear();
        m_selectedFieldFacets.put(field, Collections.singletonList(value));
        search(false, false);
    }

    /**
     * Selects the given range facet.<p>
     *
     * @param field the field name
     * @param value the value
     */
    void selectRangeFacet(String field, String value) {

        m_selectedFieldFacets.clear();
        m_selectedRangeFacets.clear();
        m_selectedRangeFacets.put(field, Collections.singletonList(value));
        search(false, false);
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
     * Updates the sort select.<p>
     */
    void updateSortOptions() {

        m_resultSorter.removeAllItems();
        for (int i = 0; i < SORT_OPTIONS[0].length; i++) {
            m_resultSorter.addItem(SORT_OPTIONS[0][i]);
            m_resultSorter.setItemCaption(SORT_OPTIONS[0][i], SORT_OPTIONS[1][i]);
        }
        m_resultSorter.setValue(m_fields.get(N_SORT_ORDER).getValue());
    }

    /**
     * Displays the available facets for the given search result.<p>
     *
     * @param solrResultList the result list
     * @param resultWrapper the result wrapper
     */
    private void displayFacetResult(CmsSolrResultList solrResultList, CmsSearchResultWrapper resultWrapper) {

        m_facetsLayout.removeAllComponents();
        Component categories = prepareCategoryFacets(solrResultList, resultWrapper);
        if (categories != null) {
            m_facetsLayout.addComponent(categories);
        }
        Component folders = prepareFolderFacets(solrResultList, resultWrapper);
        if (folders != null) {
            m_facetsLayout.addComponent(folders);
        }
        Component dates = prepareDateFacets(solrResultList, resultWrapper);
        if (dates != null) {
            m_facetsLayout.addComponent(dates);
        }
    }

    /**
     * Filters the available folder facets.<p>
     *
     * @param folderFacets the folder facets
     *
     * @return the filtered facets
     */
    private Collection<Count> filterFolderFacets(Collection<Count> folderFacets) {

        String siteRoot = A_CmsUI.getCmsObject().getRequestContext().getSiteRoot();
        if (!siteRoot.endsWith("/")) {
            siteRoot += "/";
        }
        Collection<Count> result = new ArrayList<Count>();
        List<String> selectedFolders = getSelectedFolders();
        for (Count value : folderFacets) {
            if (value.getName().startsWith(siteRoot) && (value.getName().length() > siteRoot.length())) {
                if (selectedFolders.isEmpty()) {
                    result.add(value);
                } else {
                    for (String folder : selectedFolders) {
                        if (value.getName().startsWith(folder)) {
                            result.add(value);
                            break;
                        }
                    }
                }
            }
        }
        return result;
    }

    /**
     * Returns the label for the given category.<p>
     *
     * @param categoryPath the category
     *
     * @return the label
     */
    private String getCategoryLabel(String categoryPath) {

        CmsObject cms = A_CmsUI.getCmsObject();
        String result = "";
        if (CmsStringUtil.isEmptyOrWhitespaceOnly(categoryPath)) {
            return result;
        }
        Locale locale = UI.getCurrent().getLocale();
        CmsCategoryService catService = CmsCategoryService.getInstance();

        try {
            @SuppressWarnings("unchecked")
            Field<Boolean> catFullPath = (Field<Boolean>)m_fields.get(N_CATEGORY_FULL_PATH);
            if (catFullPath.getValue().booleanValue()) {
                //cut last slash
                categoryPath = categoryPath.substring(0, categoryPath.length() - 1);

                List<String> pathParts = Arrays.asList(categoryPath.split("/"));

                String currentPath = "";
                boolean isFirst = true;
                for (String part : pathParts) {
                    currentPath += part + "/";
                    CmsCategory cat = CmsCategoryService.getInstance().localizeCategory(
                        cms,
                        catService.readCategory(cms, currentPath, "/"),
                        locale);
                    if (!isFirst) {
                        result += "  /  ";
                    } else {
                        isFirst = false;
                    }
                    result += cat.getTitle();
                }

            } else {

                CmsCategory cat = catService.localizeCategory(
                    cms,
                    catService.readCategory(cms, categoryPath, "/"),
                    locale);
                result = cat.getTitle();
            }
        } catch (Exception e) {
            LOG.error("Error reading category " + categoryPath + ".", e);
        }
        return result;
    }

    /**
     * Returns the label for the given folder.<p>
     *
     * @param path The folder path
     *
     * @return the label
     */
    private String getFolderLabel(String path) {

        CmsObject cms = A_CmsUI.getCmsObject();
        return cms.getRequestContext().removeSiteRoot(path);
    }

    /**
     * Returns the initial SOLR query object.<p>
     *
     * @return the initial SOLR query object
     */
    private CmsSolrQuery getInitialQuery() {

        Map<String, String[]> queryParams = new HashMap<String, String[]>();
        @SuppressWarnings("unchecked")
        Field<Boolean> showExpired = (Field<Boolean>)m_fields.get(N_SHOW_EXPIRED);
        if (!A_CmsUI.getCmsObject().getRequestContext().getCurrentProject().isOnlineProject()
            && showExpired.getValue().booleanValue()) {
            queryParams.put("fq", new String[] {"released:[* TO *]", "expired:[* TO *]"});
        }
        return new CmsSolrQuery(null, queryParams);
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
     * Returns the selected resource type names.<p>
     *
     * @return the selected resource type names
     */
    private List<String> getSelectedTypes() {

        List<String> types = new ArrayList<String>();
        for (Component c : m_types) {
            if (c instanceof CmsRemovableFormRow<?>) {
                @SuppressWarnings("unchecked")
                ComboBox field = ((CmsRemovableFormRow<ComboBox>)c).getInput();
                Item selected = field.getItem(field.getValue());
                if (selected != null) {
                    types.add((String)selected.getItemProperty(TYPE_PROP).getValue());
                }
            }
        }
        return types;
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
                comp.setWidth("100%");
                if (comp instanceof AbstractComponent) {
                    ((AbstractComponent)comp).setDescription(CmsVaadinUtils.getMessageText(field.m_decriptionKey));
                }
                m_formLayout.addComponent(comp);
                m_fields.put(field.m_key, (Field<?>)comp);
            } catch (Exception e) {
                LOG.error(e.getLocalizedMessage(), e);
            }
        }
    }

    /**
     * Prepares the category facets for the given search result.<p>
     *
     * @param solrResultList the search result list
     * @param resultWrapper the result wrapper
     *
     * @return the category facets component
     */
    private Component prepareCategoryFacets(CmsSolrResultList solrResultList, CmsSearchResultWrapper resultWrapper) {

        FacetField categoryFacets = solrResultList.getFacetField(FIELD_CATEGORIES);
        I_CmsSearchControllerFacetField facetController = resultWrapper.getController().getFieldFacets().getFieldFacetController().get(
            FIELD_CATEGORIES);
        if ((categoryFacets != null) && (categoryFacets.getValueCount() > 0)) {
            VerticalLayout catLayout = new VerticalLayout();
            for (final Count value : categoryFacets.getValues()) {
                Button cat = new Button(getCategoryLabel(value.getName()) + " (" + value.getCount() + ")");
                cat.addStyleName(ValoTheme.BUTTON_TINY);
                cat.addStyleName(ValoTheme.BUTTON_BORDERLESS);
                Boolean selected = facetController.getState().getIsChecked().get(value.getName());
                if ((selected != null) && selected.booleanValue()) {
                    cat.addStyleName(ValoTheme.LABEL_BOLD);
                }
                cat.addClickListener(new ClickListener() {

                    private static final long serialVersionUID = 1L;

                    public void buttonClick(ClickEvent event) {

                        selectFieldFacet(FIELD_CATEGORIES, value.getName());
                    }
                });
                catLayout.addComponent(cat);
            }
            Panel catPanel = new Panel("Categories");
            catPanel.setContent(catLayout);
            return catPanel;
        } else {
            return null;
        }
    }

    /**
     * Prepares the date facets for the given search result.<p>
     *
     * @param solrResultList the search result list
     * @param resultWrapper the result wrapper
     *
     * @return the date facets component
     */
    private Component prepareDateFacets(CmsSolrResultList solrResultList, CmsSearchResultWrapper resultWrapper) {

        RangeFacet<?, ?> dateFacets = resultWrapper.getRangeFacet().get(FIELD_DATE_FACET_NAME);
        I_CmsSearchControllerFacetRange facetController = resultWrapper.getController().getRangeFacets().getRangeFacetController().get(
            FIELD_DATE_FACET_NAME);
        if ((dateFacets != null) && (dateFacets.getCounts().size() > 0)) {
            GridLayout dateLayout = new GridLayout();
            dateLayout.setWidth("100%");
            dateLayout.setColumns(6);
            String currentYear = null;
            int row = -2;
            for (final RangeFacet.Count value : dateFacets.getCounts()) {
                String[] dateParts = value.getValue().split("-");
                if (!dateParts[0].equals(currentYear)) {
                    row += 2;
                    dateLayout.setRows(row + 2);
                    currentYear = dateParts[0];
                    Label year = new Label(currentYear);
                    year.addStyleName(OpenCmsTheme.PADDING_HORIZONTAL);
                    dateLayout.addComponent(year, 0, row, 5, row);
                    row++;
                }
                int month = Integer.parseInt(dateParts[1]) - 1;

                Button date = new Button(MONTHS[month] + " (" + value.getCount() + ")");
                date.addStyleName(ValoTheme.BUTTON_TINY);
                date.addStyleName(ValoTheme.BUTTON_BORDERLESS);
                Boolean selected = facetController.getState().getIsChecked().get(value.getValue());
                if ((selected != null) && selected.booleanValue()) {
                    date.addStyleName(ValoTheme.LABEL_BOLD);
                }
                date.addClickListener(new ClickListener() {

                    private static final long serialVersionUID = 1L;

                    public void buttonClick(ClickEvent event) {

                        selectRangeFacet(FIELD_DATE_FACET_NAME, value.getValue());
                    }
                });
                int targetColumn;
                int targetRow;
                if (month < 6) {
                    targetColumn = month;
                    targetRow = row;
                } else {
                    targetColumn = month - 6;
                    targetRow = row + 1;
                    dateLayout.setRows(row + 2);
                }
                dateLayout.addComponent(date, targetColumn, targetRow);
            }
            Panel datePanel = new Panel("Date");
            datePanel.setContent(dateLayout);
            return datePanel;
        } else {
            return null;
        }
    }

    /**
     * Prepares the folder facets for the given search result.<p>
     *
     * @param solrResultList the search result list
     * @param resultWrapper the result wrapper
     *
     * @return the folder facets component
     */
    private Component prepareFolderFacets(CmsSolrResultList solrResultList, CmsSearchResultWrapper resultWrapper) {

        FacetField folderFacets = solrResultList.getFacetField(FIELD_PARENT_FOLDERS);
        I_CmsSearchControllerFacetField facetController = resultWrapper.getController().getFieldFacets().getFieldFacetController().get(
            FIELD_PARENT_FOLDERS);
        if ((folderFacets != null) && (folderFacets.getValueCount() > 0)) {
            VerticalLayout folderLayout = new VerticalLayout();
            for (final Count value : filterFolderFacets(folderFacets.getValues())) {
                Button folder = new Button(getFolderLabel(value.getName()) + " (" + value.getCount() + ")");
                folder.addStyleName(ValoTheme.BUTTON_TINY);
                folder.addStyleName(ValoTheme.BUTTON_BORDERLESS);
                Boolean selected = facetController.getState().getIsChecked().get(value.getName());
                if ((selected != null) && selected.booleanValue()) {
                    folder.addStyleName(ValoTheme.LABEL_BOLD);
                }
                folder.addClickListener(new ClickListener() {

                    private static final long serialVersionUID = 1L;

                    public void buttonClick(ClickEvent event) {

                        selectFieldFacet(FIELD_PARENT_FOLDERS, value.getName());
                    }
                });
                folderLayout.addComponent(folder);
            }
            Panel folderPanel = new Panel("Folders");
            folderPanel.setContent(folderLayout);
            return folderPanel;
        } else {
            return null;
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
        m_save.setEnabled(saveEnabled);
        m_saveNew.setEnabled(true);
    }
}
