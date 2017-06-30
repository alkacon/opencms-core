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
import org.opencms.file.CmsPropertyDefinition;
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
import org.opencms.lock.CmsLockActionRecord.LockChange;
import org.opencms.lock.CmsLockUtil;
import org.opencms.main.CmsException;
import org.opencms.main.CmsLog;
import org.opencms.main.OpenCms;
import org.opencms.relations.CmsLink;
import org.opencms.search.CmsSearchException;
import org.opencms.search.solr.CmsSolrIndex;
import org.opencms.search.solr.CmsSolrQuery;
import org.opencms.search.solr.CmsSolrResultList;
import org.opencms.ui.A_CmsUI;
import org.opencms.ui.CmsVaadinUtils;
import org.opencms.ui.FontOpenCms;
import org.opencms.ui.I_CmsDialogContext;
import org.opencms.ui.I_CmsDialogContext.ContextType;
import org.opencms.ui.I_CmsUpdateListener;
import org.opencms.ui.actions.A_CmsWorkplaceAction;
import org.opencms.ui.actions.CmsContextMenuActionItem;
import org.opencms.ui.actions.CmsEditDialogAction;
import org.opencms.ui.apps.A_CmsWorkplaceApp;
import org.opencms.ui.apps.CmsAppWorkplaceUi;
import org.opencms.ui.apps.CmsEditor;
import org.opencms.ui.apps.CmsFileExplorer;
import org.opencms.ui.apps.I_CmsAppUIContext;
import org.opencms.ui.apps.I_CmsContextProvider;
import org.opencms.ui.apps.Messages;
import org.opencms.ui.apps.lists.CmsListConfigurationForm.ParameterField;
import org.opencms.ui.apps.projects.CmsProjectManagerConfiguration;
import org.opencms.ui.components.CmsBasicDialog;
import org.opencms.ui.components.CmsBasicDialog.DialogWidth;
import org.opencms.ui.components.CmsErrorDialog;
import org.opencms.ui.components.CmsFileTable;
import org.opencms.ui.components.CmsFileTableDialogContext;
import org.opencms.ui.components.CmsResourceTable.I_ResourcePropertyProvider;
import org.opencms.ui.components.CmsResourceTableProperty;
import org.opencms.ui.components.CmsToolBar;
import org.opencms.ui.components.I_CmsWindowCloseListener;
import org.opencms.ui.components.OpenCmsTheme;
import org.opencms.ui.components.extensions.CmsGwtDialogExtension;
import org.opencms.ui.contextmenu.CmsResourceContextMenuBuilder;
import org.opencms.ui.contextmenu.I_CmsContextMenuItem;
import org.opencms.ui.contextmenu.I_CmsContextMenuItemProvider;
import org.opencms.util.CmsStringUtil;
import org.opencms.util.CmsUUID;
import org.opencms.workplace.explorer.menu.CmsMenuItemVisibilityMode;
import org.opencms.xml.content.CmsXmlContent;
import org.opencms.xml.content.CmsXmlContentFactory;
import org.opencms.xml.types.CmsXmlDisplayFormatterValue;
import org.opencms.xml.types.CmsXmlVfsFileValue;
import org.opencms.xml.types.I_CmsXmlContentValue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.logging.Log;

import com.vaadin.data.Item;
import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.Property.ValueChangeListener;
import com.vaadin.event.FieldEvents.TextChangeEvent;
import com.vaadin.event.FieldEvents.TextChangeListener;
import com.vaadin.navigator.ViewChangeListener;
import com.vaadin.server.Sizeable.Unit;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.Component;
import com.vaadin.ui.HorizontalSplitPanel;
import com.vaadin.ui.Table;
import com.vaadin.ui.Table.CellStyleGenerator;
import com.vaadin.ui.TextField;
import com.vaadin.ui.UI;
import com.vaadin.ui.Window;
import com.vaadin.ui.themes.ValoTheme;

/**
 * Manager for list configuration files.<p>
 */
public class CmsListManager extends A_CmsWorkplaceApp
implements I_ResourcePropertyProvider, I_CmsContextProvider, ViewChangeListener, I_CmsWindowCloseListener {

    /**
     * The list configuration data.<p>
     */
    public static class ListConfigurationBean {

        /** The resource blacklist. */
        private List<CmsUUID> m_blacklist;

        /** The categories. */
        private List<String> m_categories;

        /** The display types. */
        private List<String> m_dislayTypes;

        /** The folders. */
        private List<String> m_folders;

        /** Search parameters by configuration node name. */
        private Map<String, String> m_parameterFields;

        /**
         * Constructor.<p>
         */
        public ListConfigurationBean() {
            m_parameterFields = new HashMap<String, String>();
        }

        /**
         * Returns the black list.<p>
         *
         * @return the black list
         */
        public List<CmsUUID> getBlacklist() {

            return m_blacklist;
        }

        /**
         * Returns the categories.<p>
         *
         * @return the categories
         */
        public List<String> getCategories() {

            return m_categories;
        }

        /**
         * Returns the display types.<p>
         *
         * @return the display types
         */
        public List<String> getDisplayTypes() {

            return m_dislayTypes;
        }

        /**
         * Returns the folders.<p>
         *
         * @return the folders
         */
        public List<String> getFolders() {

            return m_folders;
        }

        /**
         * Returns the parameter map.<p>
         *
         * @return the parameters
         */
        public Map<String, String> getParameters() {

            return m_parameterFields;
        }

        /**
         * Returns the parameter by name.<p>
         *
         * @param key the parameter name
         *
         * @return the parameter value
         */
        public String getParameterValue(String key) {

            return m_parameterFields.get(key);
        }

        /**
         * Returns the search types.<p>
         *
         * @return the search types
         */
        public List<String> getTypes() {

            List<String> result = new ArrayList<String>();
            if (m_dislayTypes != null) {
                for (String displayType : m_dislayTypes) {
                    String type = displayType;
                    if (type.contains(CmsXmlDisplayFormatterValue.SEPARATOR)) {
                        type = type.substring(0, type.indexOf(CmsXmlDisplayFormatterValue.SEPARATOR));
                    }
                    if (!result.contains(type)) {
                        result.add(type);
                    }
                }
            }
            return result;
        }

        /**
         * Sets the blacklist.<p>
         *
         * @param blacklist the blacklist
         */
        public void setBlacklist(List<CmsUUID> blacklist) {

            m_blacklist = blacklist;
        }

        /**
         * Sets the categories.<p>
         *
         * @param categories the categories
         */
        public void setCategories(List<String> categories) {

            m_categories = categories;
        }

        /**
         * Sets the display types.<p>
         *
         * @param displayTypes the display types
         */
        public void setDisplayTypes(List<String> displayTypes) {

            m_dislayTypes = displayTypes;
        }

        /**
         * Sets the folders.<p>
         *
         * @param folders the folders
         */
        public void setFolders(List<String> folders) {

            m_folders = folders;
        }

        /**
         * Sets the parameter by name.<p>
         *
         * @param name the parameter name
         * @param value the parameter value
         */
        public void setParameterValue(String name, String value) {

            m_parameterFields.put(name, value);
        }

    }

    /**
     * Reads the search configuration from the current list configuration form.<p>
     */
    public static class SearchConfigParser implements I_CmsSearchConfigurationParser {

        /** The content locale. */
        private Locale m_contentLocale;

        /** The filter query. */
        private String m_filterQuery;

        /** The selected categories. */
        private List<String> m_selectedCategories;

        /** The selected folders. */
        private List<String> m_selectedFolders;

        /** The selected types. */
        private List<String> m_selectedTypes;

        /** The show expired flag. */
        private boolean m_showExpired;

        /** The sort option. */
        private String m_sortOption;

        /**
         * Constructor.<p>
         *
         * @param types the selected types
         * @param folders the selected folders
         * @param categories the selected categories
         * @param filterQuery the filter query
         * @param sortOption the sort option
         * @param showExpired the show expired flag
         * @param contentLocale the content locale
         */
        public SearchConfigParser(
            List<String> types,
            List<String> folders,
            List<String> categories,
            String filterQuery,
            String sortOption,
            boolean showExpired,
            Locale contentLocale) {
            m_selectedTypes = types;
            m_selectedFolders = folders;
            m_selectedCategories = categories;
            m_sortOption = sortOption;
            m_contentLocale = contentLocale;
            m_filterQuery = filterQuery;
        }

        /**
         * Constructor.<p>
         *
         * @param configBean the configuration data bean
         * @param locale the search content locale
         */
        public SearchConfigParser(ListConfigurationBean configBean, Locale locale) {
            this(
                configBean.getTypes(),
                configBean.getFolders(),
                configBean.getCategories(),
                configBean.getParameterValue(CmsListConfigurationForm.N_FILTER_QUERY),
                configBean.getParameterValue(CmsListConfigurationForm.N_SORT_ORDER),
                Boolean.parseBoolean(configBean.getParameterValue(CmsListConfigurationForm.N_SHOW_EXPIRED)),
                locale);
        }

        /**
         * Returns the initial SOLR query.<p>
         *
         * @return the SOLR query
         */
        public CmsSolrQuery getInitialQuery() {

            Map<String, String[]> queryParams = new HashMap<String, String[]>();
            if (!A_CmsUI.getCmsObject().getRequestContext().getCurrentProject().isOnlineProject() && m_showExpired) {
                queryParams.put("fq", new String[] {"released:[* TO *]", "expired:[* TO *]"});
            }
            return new CmsSolrQuery(null, queryParams);
        }

        /**
         * @see org.opencms.jsp.search.config.parser.I_CmsSearchConfigurationParser#parseCommon()
         */
        @Override
        public I_CmsSearchConfigurationCommon parseCommon() {

            return new I_CmsSearchConfigurationCommon() {

                @Override
                public Map<String, String> getAdditionalParameters() {

                    return null;
                }

                @Override
                public boolean getEscapeQueryChars() {

                    return false;
                }

                @Override
                public String getExtraSolrParams() {

                    return getFolderFilter() + getResourceTypeFilter() + getCategoryFilter() + getFilterQuery();
                }

                @Override
                public boolean getIgnoreExpirationDate() {

                    return true;
                }

                @Override
                public boolean getIgnoreQueryParam() {

                    return false;
                }

                @Override
                public boolean getIgnoreReleaseDate() {

                    return true;
                }

                @Override
                public String getLastQueryParam() {

                    return null;
                }

                @Override
                public String getModifiedQuery(String queryString) {

                    return "{!type=edismax qf=\"content_"
                        + getContentLocale().toString()
                        + " Title_prop spell\"}"
                        + queryString;
                }

                @Override
                public String getQueryParam() {

                    return null;
                }

                @Override
                public String getReloadedParam() {

                    return null;
                }

                @Override
                public boolean getSearchForEmptyQueryParam() {

                    return true;
                }

                @Override
                public String getSolrCore() {

                    return null;
                }

                @Override
                public String getSolrIndex() {

                    return null;
                }
            };
        }

        /**
         * @see org.opencms.jsp.search.config.parser.I_CmsSearchConfigurationParser#parseDidYouMean()
         */
        @Override
        public I_CmsSearchConfigurationDidYouMean parseDidYouMean() {

            return null;
        }

        /**
         * @see org.opencms.jsp.search.config.parser.I_CmsSearchConfigurationParser#parseFieldFacets()
         */
        @Override
        public Map<String, I_CmsSearchConfigurationFacetField> parseFieldFacets() {

            Map<String, I_CmsSearchConfigurationFacetField> result = new HashMap<String, I_CmsSearchConfigurationFacetField>();
            result.put(
                CmsListConfigurationForm.FIELD_CATEGORIES,
                new CmsSearchConfigurationFacetField(
                    CmsListConfigurationForm.FIELD_CATEGORIES,
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
                CmsListConfigurationForm.FIELD_PARENT_FOLDERS,
                new CmsSearchConfigurationFacetField(
                    CmsListConfigurationForm.FIELD_PARENT_FOLDERS,
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
        @Override
        public I_CmsSearchConfigurationHighlighting parseHighlighter() {

            return null;
        }

        /**
         * @see org.opencms.jsp.search.config.parser.I_CmsSearchConfigurationParser#parsePagination()
         */
        @Override
        public I_CmsSearchConfigurationPagination parsePagination() {

            return new CmsSearchConfigurationPagination(null, Integer.valueOf(10000), Integer.valueOf(1));
        }

        /**
         * @see org.opencms.jsp.search.config.parser.I_CmsSearchConfigurationParser#parseQueryFacet()
         */
        @Override
        public I_CmsSearchConfigurationFacetQuery parseQueryFacet() {

            return null;
        }

        /**
         * @see org.opencms.jsp.search.config.parser.I_CmsSearchConfigurationParser#parseRangeFacets()
         */
        @Override
        public Map<String, I_CmsSearchConfigurationFacetRange> parseRangeFacets() {

            Map<String, I_CmsSearchConfigurationFacetRange> result = new HashMap<String, I_CmsSearchConfigurationFacetRange>();
            I_CmsSearchConfigurationFacetRange rangeFacet = new CmsSearchConfigurationFacetRange(
                String.format(CmsListConfigurationForm.FIELD_DATE, m_contentLocale.toString()),
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
        @Override
        public I_CmsSearchConfigurationSorting parseSorting() {

            List<I_CmsSearchConfigurationSortOption> result = null;
            I_CmsSearchConfigurationSortOption defaultOption = null;
            if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(m_sortOption)) {
                defaultOption = new CmsSearchConfigurationSortOption(
                    "",
                    "",
                    String.format(m_sortOption, m_contentLocale.toString()));
                result = Collections.<I_CmsSearchConfigurationSortOption> singletonList(defaultOption);
            } else {
                result = Collections.<I_CmsSearchConfigurationSortOption> emptyList();
            }

            return new CmsSearchConfigurationSorting(null, result, defaultOption);
        }

        /**
         * Sets the search content locale.<p>
         *
         * @param locale the locale
         */
        public void setContentLocale(Locale locale) {

            m_contentLocale = locale;
        }

        /**
         * Sets the sort option.<p>
         *
         * @param sortOption the sort option
         */
        public void setSortOption(String sortOption) {

            m_sortOption = sortOption;
        }

        /**
         * Returns the category filter query part.<p>
         *
         * @return the category filter query part
         */
        String getCategoryFilter() {

            String result = "";
            if (!m_selectedCategories.isEmpty()) {
                result = "&fq=category_exact:(";
                for (String path : m_selectedCategories) {
                    result += path + " ";
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

            return m_contentLocale;
        }

        /**
         * Returns the additional filter query part.<p>
         *
         * @return the additional filter query part
         */
        String getFilterQuery() {

            if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(m_filterQuery)) {
                if (!m_filterQuery.startsWith("&")) {
                    return "&" + m_filterQuery;
                } else {
                    return m_filterQuery;
                }
            }
            return "";
        }

        /**
         * Returns the folder filter query part.<p>
         *
         * @return the folder filter query part
         */
        String getFolderFilter() {

            String result = "";
            boolean first = true;
            for (String value : m_selectedFolders) {
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
         * Returns the resource type filter query part.<p>
         *
         * @return the resource type filter query part
         */
        String getResourceTypeFilter() {

            String result = "";
            boolean first = true;
            for (String value : m_selectedTypes) {
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
    }

    /** The view content list path name. */
    public static final String PATH_NAME_VIEW = "view";

    /** The list configuration resource type name. */
    public static final String RES_TYPE_LIST_CONFIG = "listconfig";

    /** The blacklisted table column property id. */
    protected static final CmsResourceTableProperty BLACKLISTED_PROPERTY = new CmsResourceTableProperty(
        "BLACKLISTED",
        Boolean.class,
        Boolean.FALSE,
        Messages.GUI_LISTMANAGER_COLUMN_BLACKLISTED_0,
        true,
        1,
        40);

    /** The logger for this class. */
    private static Log LOG = CmsLog.getLog(CmsListManager.class.getName());

    /** The serial version id. */
    private static final long serialVersionUID = -25954374225590319L;

    /** The current list configuration data. */
    ListConfigurationBean m_currentConfig;

    /** The current list configuration resource. */
    CmsResource m_currentResource;

    /** The create new button. */
    private Button m_createNewButton;

    /** The current search parser. */
    private SearchConfigParser m_currentConfigParser;

    /** The current edit dialog window. */
    private Window m_dialogWindow;

    /** The edit current configuration button. */
    private Button m_editCurrentButton;

    /** Indicates if the overview list is shown. */
    private boolean m_isOverView;

    /** The current lock action. */
    private CmsLockActionRecord m_lockAction;

    /** The list configurations overview table. */
    private CmsFileTable m_overviewTable;

    /** The publish button. */
    private Button m_publishButton;

    /** The facet result display. */
    private CmsResultFacets m_resultFacets;

    /** The mail layout. */
    private HorizontalSplitPanel m_resultLayout;

    /** The result table. */
    private CmsFileTable m_resultTable;

    /** The table filter input. */
    private TextField m_tableFilter;

    /** The text search input. */
    private TextField m_textSearch;

    /** The sort select. */
    private ComboBox m_resultSorter;

    /** The resetting flag. */
    private boolean m_resetting;

    /** The locale select. */
    private ComboBox m_localeSelect;

    /**
     * @see org.opencms.ui.components.CmsResourceTable.I_ResourcePropertyProvider#addItemProperties(com.vaadin.data.Item, org.opencms.file.CmsObject, org.opencms.file.CmsResource, java.util.Locale)
     */
    @Override
    public void addItemProperties(Item resourceItem, CmsObject cms, CmsResource resource, Locale locale) {

        if ((m_currentConfig != null) && (resourceItem.getItemProperty(CmsListManager.BLACKLISTED_PROPERTY) != null)) {
            resourceItem.getItemProperty(CmsListManager.BLACKLISTED_PROPERTY).setValue(
                Boolean.valueOf(m_currentConfig.getBlacklist().contains(resource.getStructureId())));
        }
    }

    /**
     * @see com.vaadin.navigator.ViewChangeListener#afterViewChange(com.vaadin.navigator.ViewChangeListener.ViewChangeEvent)
     */
    @Override
    public void afterViewChange(ViewChangeEvent event) {

        // nothing to do
    }

    /**
     * @see com.vaadin.navigator.ViewChangeListener#beforeViewChange(com.vaadin.navigator.ViewChangeListener.ViewChangeEvent)
     */
    @Override
    public boolean beforeViewChange(ViewChangeEvent event) {

        unlockCurrent();
        return true;
    }

    /**
     * @see org.opencms.ui.apps.I_CmsContextProvider#getDialogContext()
     */
    @Override
    public I_CmsDialogContext getDialogContext() {

        CmsFileTable table = isOverView() ? m_overviewTable : m_resultTable;
        CmsFileTableDialogContext context = new CmsFileTableDialogContext(
            CmsProjectManagerConfiguration.APP_ID,
            ContextType.fileTable,
            table,
            table.getSelectedResources());
        context.setEditableProperties(CmsFileExplorer.INLINE_EDIT_PROPERTIES);
        return context;
    }

    /**
     * @see org.opencms.ui.apps.A_CmsWorkplaceApp#initUI(org.opencms.ui.apps.I_CmsAppUIContext)
     */
    @Override
    public void initUI(I_CmsAppUIContext uiContext) {

        super.initUI(uiContext);
        m_publishButton = CmsToolBar.createButton(
            FontOpenCms.PUBLISH,
            CmsVaadinUtils.getMessageText(Messages.GUI_PUBLISH_BUTTON_TITLE_0));
        if (CmsAppWorkplaceUi.isOnlineProject()) {
            // disable publishing in online project
            m_publishButton.setEnabled(false);
            m_publishButton.setDescription(CmsVaadinUtils.getMessageText(Messages.GUI_TOOLBAR_NOT_AVAILABLE_ONLINE_0));
        }

        m_publishButton.addClickListener(new ClickListener() {

            /** Serial version id. */
            private static final long serialVersionUID = 1L;

            @Override
            public void buttonClick(ClickEvent event) {

                publish();
            }
        });
        uiContext.addToolbarButton(m_publishButton);

        m_createNewButton = CmsToolBar.createButton(FontOpenCms.WAND, "Create new");
        m_createNewButton.addClickListener(new ClickListener() {

            /** Serial version id. */
            private static final long serialVersionUID = 1L;

            @Override
            public void buttonClick(ClickEvent event) {

                createNew();
            }
        });
        uiContext.addToolbarButton(m_createNewButton);

        m_editCurrentButton = CmsToolBar.createButton(FontOpenCms.SETTINGS, "EDit configuration");
        m_editCurrentButton.addClickListener(new ClickListener() {

            /** Serial version id. */
            private static final long serialVersionUID = 1L;

            @Override
            public void buttonClick(ClickEvent event) {

                editListConfiguration(m_currentResource);
            }
        });
        uiContext.addToolbarButton(m_editCurrentButton);

        m_rootLayout.setMainHeightFull(true);
        m_resultLayout = new HorizontalSplitPanel();
        m_resultLayout.setSizeFull();
        m_resultFacets = new CmsResultFacets(this);
        m_resultFacets.setWidth("100%");
        m_resultLayout.setFirstComponent(m_resultFacets);
        LinkedHashMap<CmsResourceTableProperty, Integer> tableColumns = new LinkedHashMap<CmsResourceTableProperty, Integer>();
        tableColumns.putAll(CmsFileTable.DEFAULT_TABLE_PROPERTIES);
        tableColumns.put(BLACKLISTED_PROPERTY, Integer.valueOf(0));
        m_resultTable = new CmsFileTable(null, tableColumns);
        m_resultTable.applyWorkplaceAppSettings();
        CmsResourceContextMenuBuilder menuBuilderOverView = new CmsResourceContextMenuBuilder();
        menuBuilderOverView.addMenuItemProvider(OpenCms.getWorkplaceAppManager().getMenuItemProvider());
        menuBuilderOverView.addMenuItemProvider(new I_CmsContextMenuItemProvider() {

            @Override
            public List<I_CmsContextMenuItem> getMenuItems() {

                return Arrays.<I_CmsContextMenuItem> asList(new CmsContextMenuActionItem(new CmsEditDialogAction() {

                    @Override
                    public void executeAction(I_CmsDialogContext context) {

                        editListConfiguration(context.getResources().get(0));
                    }
                }, null, 10, 1000));
            }
        });
        CmsResourceContextMenuBuilder menuBuilder = new CmsResourceContextMenuBuilder();
        menuBuilder.addMenuItemProvider(OpenCms.getWorkplaceAppManager().getMenuItemProvider());
        menuBuilder.addMenuItemProvider(new I_CmsContextMenuItemProvider() {

            @Override
            public List<I_CmsContextMenuItem> getMenuItems() {

                return Arrays.<I_CmsContextMenuItem> asList(new CmsContextMenuActionItem(new A_CmsWorkplaceAction() {

                    @Override
                    public void executeAction(I_CmsDialogContext context) {

                        CmsUUID structureId = context.getResources().get(0).getStructureId();
                        m_currentConfig.getBlacklist().add(structureId);
                        saveContent(m_currentConfig, false, false);
                        context.finish(Collections.singletonList(structureId));
                    }

                    @Override
                    public String getId() {

                        return "hideresource";
                    }

                    @Override
                    public String getTitle() {

                        return getWorkplaceMessage(Messages.GUI_LISTMANAGER_BLACKLIST_MENU_ENTRY_0);
                    }

                    @Override
                    public CmsMenuItemVisibilityMode getVisibility(CmsObject cms, List<CmsResource> resources) {

                        if ((m_currentConfig != null)
                            && (resources != null)
                            && (resources.size() == 1)
                            && !m_currentConfig.getBlacklist().contains(resources.get(0).getStructureId())) {
                            return CmsEditDialogAction.VISIBILITY.getVisibility(
                                cms,
                                Collections.singletonList(m_currentResource));
                        } else {
                            return CmsMenuItemVisibilityMode.VISIBILITY_INVISIBLE;
                        }
                    }

                }, null, 10, 0), new CmsContextMenuActionItem(new A_CmsWorkplaceAction() {

                    @Override
                    public void executeAction(I_CmsDialogContext context) {

                        CmsUUID structureId = context.getResources().get(0).getStructureId();
                        m_currentConfig.getBlacklist().remove(structureId);
                        saveContent(m_currentConfig, false, false);
                        context.finish(Collections.singletonList(structureId));
                    }

                    @Override
                    public String getId() {

                        return "showresource";
                    }

                    @Override
                    public String getTitle() {

                        return getWorkplaceMessage(Messages.GUI_LISTMANAGER_REMOVE_FROM_BLACKLIST_MENU_ENTRY_0);
                    }

                    @Override
                    public CmsMenuItemVisibilityMode getVisibility(CmsObject cms, List<CmsResource> resources) {

                        if ((m_currentConfig != null)
                            && (resources != null)
                            && (resources.size() == 1)
                            && m_currentConfig.getBlacklist().contains(resources.get(0).getStructureId())) {
                            return CmsEditDialogAction.VISIBILITY.getVisibility(
                                cms,
                                Collections.singletonList(m_currentResource));
                        } else {
                            return CmsMenuItemVisibilityMode.VISIBILITY_INVISIBLE;
                        }
                    }

                }, null, 10, 0));
            }
        });
        m_resultTable.setMenuBuilder(menuBuilder);
        m_resultTable.addAdditionalStyleGenerator(new CellStyleGenerator() {

            private static final long serialVersionUID = 1L;

            @Override
            public String getStyle(Table source, Object itemId, Object propertyId) {

                if ((m_currentConfig != null) && m_currentConfig.getBlacklist().contains(itemId)) {
                    return OpenCmsTheme.PROJECT_OTHER;
                }
                return null;
            }
        });
        m_resultTable.setContextProvider(this);
        m_resultTable.addPropertyProvider(this);
        m_resultTable.setSizeFull();
        m_resultLayout.setSecondComponent(m_resultTable);
        m_overviewTable = new CmsFileTable(this);
        m_overviewTable.applyWorkplaceAppSettings();
        m_overviewTable.setMenuBuilder(menuBuilderOverView);
        m_overviewTable.setSizeFull();

        m_tableFilter = new TextField();
        m_tableFilter.setIcon(FontOpenCms.FILTER);
        m_tableFilter.setInputPrompt(
            Messages.get().getBundle(UI.getCurrent().getLocale()).key(Messages.GUI_EXPLORER_FILTER_0));
        m_tableFilter.addStyleName(ValoTheme.TEXTFIELD_INLINE_ICON);
        m_tableFilter.setWidth("200px");
        m_tableFilter.addTextChangeListener(new TextChangeListener() {

            private static final long serialVersionUID = 1L;

            @Override
            public void textChange(TextChangeEvent event) {

                filterTable(event.getText());

            }
        });
        m_infoLayout.addComponent(m_tableFilter);

        m_localeSelect = new ComboBox();
        m_localeSelect.setNullSelectionAllowed(false);
        m_localeSelect.setWidth("100px");
        m_localeSelect.addValueChangeListener(new ValueChangeListener() {

            /** Serial version id. */
            private static final long serialVersionUID = 1L;

            @Override
            public void valueChange(ValueChangeEvent event) {

                changeContentLocale((Locale)event.getProperty().getValue());
            }
        });
        m_infoLayout.addComponent(m_localeSelect);

        m_resultSorter = new ComboBox();
        m_resultSorter.setNullSelectionAllowed(false);
        m_resultSorter.setWidth("200px");
        for (int i = 0; i < CmsListConfigurationForm.SORT_OPTIONS[0].length; i++) {
            m_resultSorter.addItem(CmsListConfigurationForm.SORT_OPTIONS[0][i]);
            m_resultSorter.setItemCaption(
                CmsListConfigurationForm.SORT_OPTIONS[0][i],
                CmsListConfigurationForm.SORT_OPTIONS[1][i]);
        }
        m_resultSorter.addValueChangeListener(new ValueChangeListener() {

            /** Serial version id. */
            private static final long serialVersionUID = 1L;

            @Override
            public void valueChange(ValueChangeEvent event) {

                sortResult();
            }
        });
        m_infoLayout.addComponent(m_resultSorter);

        m_textSearch = new TextField();
        m_textSearch.setIcon(FontOpenCms.SEARCH);
        m_textSearch.setInputPrompt("Search");
        m_textSearch.addStyleName(ValoTheme.TEXTFIELD_INLINE_ICON);
        m_textSearch.setWidth("200px");
        m_textSearch.addValueChangeListener(new ValueChangeListener() {

            /** Serial version id. */
            private static final long serialVersionUID = 1L;

            @Override
            public void valueChange(ValueChangeEvent event) {

                search((String)event.getProperty().getValue());
            }
        });
        m_infoLayout.addComponent(m_textSearch);
        m_resultLayout.setSecondComponent(m_resultTable);
        m_resultLayout.setSplitPosition(CmsFileExplorer.LAYOUT_SPLIT_POSITION, Unit.PIXELS);
    }

    /**
     * @see org.opencms.ui.components.I_CmsWindowCloseListener#onWindowClose()
     */
    @Override
    public void onWindowClose() {

        unlockCurrent();
    }

    /**
     * Parses the list configuration resource.<p>
     *
     * @param res the list configuration resource
     *
     * @return the configuration data bean
     */
    public ListConfigurationBean parseListConfiguration(CmsResource res) {

        CmsObject cms = A_CmsUI.getCmsObject();
        ListConfigurationBean result = new ListConfigurationBean();
        try {
            CmsFile configFile = cms.readFile(res);
            CmsXmlContent content = CmsXmlContentFactory.unmarshal(cms, configFile);
            Locale locale = CmsLocaleManager.getLocale("en");

            if (!content.hasLocale(locale)) {
                locale = content.getLocales().get(0);
            }
            for (ParameterField field : CmsListConfigurationForm.PARAMETER_FIELDS) {
                String val = content.getStringValue(cms, field.m_key, locale);
                if (CmsListConfigurationForm.N_CATEGORY.equals(field.m_key)) {
                    if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(val)) {
                        result.setCategories(Arrays.asList(val.split(",")));
                    } else {
                        result.setCategories(Collections.<String> emptyList());
                    }
                } else {
                    result.setParameterValue(field.m_key, val);
                }
            }
            List<String> displayTypes = new ArrayList<String>();
            List<I_CmsXmlContentValue> typeValues = content.getValues(CmsListConfigurationForm.N_DISPLAY_TYPE, locale);
            if (!typeValues.isEmpty()) {
                for (I_CmsXmlContentValue value : typeValues) {
                    displayTypes.add(value.getStringValue(cms));
                }
            }
            result.setDisplayTypes(displayTypes);
            List<String> folders = new ArrayList<String>();
            List<I_CmsXmlContentValue> folderValues = content.getValues(
                CmsListConfigurationForm.N_SEARCH_FOLDER,
                locale);
            if (!folderValues.isEmpty()) {
                for (I_CmsXmlContentValue value : folderValues) {
                    String val = value.getStringValue(cms);
                    // we are using root paths
                    folders.add(cms.getRequestContext().addSiteRoot(val));
                }
            }
            result.setFolders(folders);
            List<CmsUUID> blackList = new ArrayList<CmsUUID>();
            List<I_CmsXmlContentValue> blacklistValues = content.getValues(
                CmsListConfigurationForm.N_BLACKLIST,
                locale);
            if (!blacklistValues.isEmpty()) {
                for (I_CmsXmlContentValue value : blacklistValues) {
                    CmsLink link = ((CmsXmlVfsFileValue)value).getLink(cms);
                    if (link != null) {
                        blackList.add(link.getStructureId());
                    }
                }
            }
            result.setBlacklist(blackList);
        } catch (CmsException e) {
            e.printStackTrace();
        }
        return result;
    }

    /**
     * Saves the given list configuration data.<p>
     *
     * @param configBean the configuration data bean
     * @param asNew <code>true</code> to create a new resource
     */
    public void saveContent(ListConfigurationBean configBean, boolean asNew) {

        saveContent(configBean, asNew, true);
    }

    /**
     * Saves the given list configuration data.<p>
     *
     * @param configBean the configuration data bean
     * @param asNew <code>true</code> to create a new resource
     * @param updateResult <code>true</code> to update the result view
     */
    public void saveContent(ListConfigurationBean configBean, boolean asNew, boolean updateResult) {

        if (m_dialogWindow != null) {
            m_dialogWindow.close();
            m_dialogWindow = null;
        }
        CmsObject cms = A_CmsUI.getCmsObject();
        if ((m_currentResource == null) || asNew) {
            String contextPath;
            List<String> folders = configBean.getFolders();
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
        } else {
            try {
                m_lockAction = CmsLockUtil.ensureLock(cms, m_currentResource);
            } catch (CmsException e) {
                CmsErrorDialog.showErrorDialog(e);
                return;
            }
        }
        if (m_currentResource != null) {
            try {
                CmsFile configFile = cms.readFile(m_currentResource);
                CmsXmlContent content = CmsXmlContentFactory.unmarshal(cms, configFile);
                // list configurations are single locale contents
                Locale locale = CmsLocaleManager.MASTER_LOCALE;
                content.removeLocale(locale);
                content.addLocale(cms, locale);
                for (Entry<String, String> fieldEntry : configBean.getParameters().entrySet()) {
                    I_CmsXmlContentValue contentVal = content.getValue(fieldEntry.getKey(), locale);
                    if (contentVal == null) {
                        contentVal = content.addValue(cms, fieldEntry.getKey(), locale, 0);
                    }
                    contentVal.setStringValue(cms, fieldEntry.getValue());
                }
                int count = 0;
                for (String type : configBean.getDisplayTypes()) {
                    I_CmsXmlContentValue contentVal = content.getValue(
                        CmsListConfigurationForm.N_DISPLAY_TYPE,
                        locale,
                        count);
                    if (contentVal == null) {
                        contentVal = content.addValue(cms, CmsListConfigurationForm.N_DISPLAY_TYPE, locale, count);
                    }
                    contentVal.setStringValue(cms, type);
                    count++;
                }
                count = 0;
                for (String folder : configBean.getFolders()) {
                    I_CmsXmlContentValue contentVal = content.getValue(
                        CmsListConfigurationForm.N_SEARCH_FOLDER,
                        locale,
                        count);
                    if (contentVal == null) {
                        contentVal = content.addValue(cms, CmsListConfigurationForm.N_SEARCH_FOLDER, locale, count);
                    }
                    contentVal.setStringValue(cms, folder);
                    count++;
                }
                count = 0;
                for (CmsUUID hiddenId : configBean.getBlacklist()) {
                    CmsXmlVfsFileValue contentVal = (CmsXmlVfsFileValue)content.getValue(
                        CmsListConfigurationForm.N_BLACKLIST,
                        locale,
                        count);
                    if (contentVal == null) {
                        contentVal = (CmsXmlVfsFileValue)content.addValue(
                            cms,
                            CmsListConfigurationForm.N_BLACKLIST,
                            locale,
                            count);
                    }
                    contentVal.setIdValue(cms, hiddenId);
                    count++;
                }
                configFile.setContents(content.marshal());
                cms.writeFile(configFile);
                if (m_lockAction.getChange().equals(LockChange.locked)) {
                    CmsLockUtil.tryUnlock(cms, configFile);
                }
            } catch (CmsException e) {
                e.printStackTrace();
            }
            m_currentConfig = configBean;
            if (updateResult) {
                Locale contentLocale = getContentLocale(configBean);
                search(new SearchConfigParser(configBean, contentLocale), m_currentResource);
            }
        }

    }

    /**
     * Updates the search result.<p>
     *
     * @param fieldFacets the field facets
     * @param rangeFacets the range facets
     */
    public void search(Map<String, List<String>> fieldFacets, Map<String, List<String>> rangeFacets) {

        search(fieldFacets, rangeFacets, null);
    }

    /**
     * Updates the search result.<p>
     *
     * @param fieldFacets the field facets
     * @param rangeFacets the range facets
     * @param additionalQuery the additional query
     */
    public void search(
        Map<String, List<String>> fieldFacets,
        Map<String, List<String>> rangeFacets,
        String additionalQuery) {

        if (CmsStringUtil.isNotEmptyOrWhitespaceOnly((String)m_resultSorter.getValue())) {
            m_currentConfigParser.setSortOption((String)m_resultSorter.getValue());
        }

        CmsSolrQuery query = m_currentConfigParser.getInitialQuery();
        CmsSearchController controller = new CmsSearchController(new CmsSearchConfiguration(m_currentConfigParser));
        controller.getPagination().getState().setCurrentPage(1);
        if (fieldFacets != null) {
            Map<String, I_CmsSearchControllerFacetField> fieldFacetControllers = controller.getFieldFacets().getFieldFacetController();
            for (Entry<String, List<String>> facetEntry : fieldFacets.entrySet()) {
                I_CmsSearchStateFacet state = fieldFacetControllers.get(facetEntry.getKey()).getState();
                state.clearChecked();
                for (String check : facetEntry.getValue()) {
                    state.addChecked(check);
                }
            }
        }
        if (rangeFacets != null) {
            Map<String, I_CmsSearchControllerFacetRange> rangeFacetControllers = controller.getRangeFacets().getRangeFacetController();
            for (Entry<String, List<String>> facetEntry : rangeFacets.entrySet()) {
                I_CmsSearchStateFacet state = rangeFacetControllers.get(facetEntry.getKey()).getState();
                state.clearChecked();
                for (String check : facetEntry.getValue()) {
                    state.addChecked(check);
                }
            }
        }
        if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(additionalQuery)) {
            controller.getCommon().getState().setQuery(additionalQuery);
        } else {
            resetTextSearch();
        }

        controller.addQueryParts(query);
        executeSearch(controller, query);
    }

    /**
     * Execute a search with the given search configuration parser.<p>
     *
     * @param configParser the search configuration parser
     * @param resource the current configuration resource
     */
    public void search(SearchConfigParser configParser, CmsResource resource) {

        m_currentResource = resource;
        m_currentConfigParser = configParser;
        resetContentLocale(configParser.getContentLocale());
        m_resetting = true;
        m_resultSorter.setValue(m_currentConfig.getParameterValue(CmsListConfigurationForm.N_SORT_ORDER));
        m_resetting = false;

        search(null, null, null);
    }

    /**
     * @see org.opencms.ui.apps.A_CmsWorkplaceApp#getBreadCrumbForState(java.lang.String)
     */
    @Override
    protected LinkedHashMap<String, String> getBreadCrumbForState(String state) {

        LinkedHashMap<String, String> crumbs = new LinkedHashMap<String, String>();
        if (CmsStringUtil.isEmptyOrWhitespaceOnly(state)) {
            crumbs.put("", CmsVaadinUtils.getMessageText(Messages.GUI_LISTMANAGER_TITLE_0));
        } else if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(
            A_CmsWorkplaceApp.getParamFromState(state, CmsEditor.RESOURCE_ID_PREFIX))) {
            crumbs.put(
                CmsListManagerConfiguration.APP_ID,
                CmsVaadinUtils.getMessageText(Messages.GUI_LISTMANAGER_TITLE_0));
            String title = "";
            try {
                title = A_CmsUI.getCmsObject().readPropertyObject(
                    m_currentResource,
                    CmsPropertyDefinition.PROPERTY_TITLE,
                    false).getValue();
            } catch (Exception e) {
                // ignore
            }
            if ((m_currentResource != null) && CmsStringUtil.isEmptyOrWhitespaceOnly(title)) {
                title = m_currentResource.getName();
            }
            crumbs.put("", "View: " + title);
        }
        return crumbs;
    }

    /**
     * @see org.opencms.ui.apps.A_CmsWorkplaceApp#getComponentForState(java.lang.String)
     */
    @Override
    protected Component getComponentForState(String state) {

        CmsObject cms = A_CmsUI.getCmsObject();
        boolean showOverview = true;
        if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(
            A_CmsWorkplaceApp.getParamFromState(state, CmsEditor.RESOURCE_ID_PREFIX))) {
            try {
                CmsUUID id = new CmsUUID(A_CmsWorkplaceApp.getParamFromState(state, CmsEditor.RESOURCE_ID_PREFIX));
                CmsResource res = cms.readResource(id, CmsResourceFilter.ONLY_VISIBLE_NO_DELETED);
                m_currentConfig = parseListConfiguration(res);
                search(new SearchConfigParser(m_currentConfig, getContentLocale(m_currentConfig)), res);
                showOverview = false;

            } catch (Exception e) {
                CmsErrorDialog.showErrorDialog(e);
                LOG.error(e.getLocalizedMessage(), e);
            }
        }
        if (showOverview) {
            unlockCurrent();
            m_lockAction = null;
            displayListConfigs();
        }
        //        m_searchForm.resetFormValues();
        enableOverviewMode(showOverview);

        return showOverview ? m_overviewTable : m_resultLayout;
    }

    /**
     * @see org.opencms.ui.apps.A_CmsWorkplaceApp#getSubNavEntries(java.lang.String)
     */
    @Override
    protected List<NavEntry> getSubNavEntries(String state) {

        return null;
    }

    /**
     * Changes the search content locale, reissuing a search.<p>
     *
     * @param contentLocale the content locale to set
     */
    void changeContentLocale(Locale contentLocale) {

        if (!m_resetting) {
            m_currentConfigParser.setContentLocale(contentLocale);
        }
        search(null, null, null);
    }

    /**
     * Closes the edit dialog window.<p>
     */
    void closeEditDialog() {

        if (m_dialogWindow != null) {
            m_dialogWindow.close();
            m_dialogWindow = null;
        }
        if (m_isOverView) {
            unlockCurrent();
            m_currentResource = null;
        }
    }

    /**
     * Opens the create new dialog.<p>
     */
    void createNew() {

        if (m_isOverView) {
            editListConfiguration(null);
        }
    }

    /**
     * Displays the search result in the result table.<p>
     *
     * @param resultList the search result
     */
    void displayResult(CmsSolrResultList resultList) {

        List<CmsResource> resources = new ArrayList<CmsResource>(resultList);
        m_resultTable.fillTable(A_CmsUI.getCmsObject(), resources, true, false);
        if (m_isOverView) {
            enableOverviewMode(false);
            String state = A_CmsWorkplaceApp.addParamToState(
                "",
                CmsEditor.RESOURCE_ID_PREFIX,
                m_currentResource.getStructureId().toString());
            CmsAppWorkplaceUi.get().changeCurrentAppState(state);

            updateBreadCrumb(getBreadCrumbForState(state));
        }
    }

    /**
     * Edits the given list configuration resource.<p>
     *
     * @param resource the cofiguration resource
     */
    void editListConfiguration(CmsResource resource) {

        if (m_dialogWindow != null) {
            m_dialogWindow.close();
        }
        m_dialogWindow = CmsBasicDialog.prepareWindow(DialogWidth.wide);
        CmsListConfigurationForm formDialog = new CmsListConfigurationForm(this);
        if (resource != null) {
            formDialog.initFormValues(resource);
        }
        m_currentResource = resource;
        m_dialogWindow.setContent(formDialog);
        m_dialogWindow.setCaption("Edit configuration");
        CmsAppWorkplaceUi.get().addWindow(m_dialogWindow);
        m_dialogWindow.center();
    }

    /**
     * Enables the overview mode.<p>
     *
     * @param enabled <code>true</code> to enable the mode
     */
    void enableOverviewMode(boolean enabled) {

        boolean isOffline = !A_CmsUI.getCmsObject().getRequestContext().getCurrentProject().isOnlineProject();
        m_publishButton.setVisible(!enabled);
        m_publishButton.setEnabled(isOffline);
        m_tableFilter.setVisible(enabled);
        m_textSearch.setVisible(!enabled);
        m_editCurrentButton.setVisible(!enabled);
        m_editCurrentButton.setEnabled(isOffline);
        m_createNewButton.setVisible(enabled);
        m_createNewButton.setEnabled(isOffline);
        m_resultSorter.setVisible(!enabled);
        m_localeSelect.setVisible(!enabled);
        m_isOverView = enabled;
        m_rootLayout.setMainContent(enabled ? m_overviewTable : m_resultLayout);
    }

    /**
     * Filters the result table.<p>
     *
     * @param filter the filter string
     */
    void filterTable(String filter) {

        if (!m_resetting) {
            m_overviewTable.filterTable(filter);
        }
    }

    /**
     * Returns the resources to publish for the current list.<p>
     *
     * @return the publish resources
     */
    List<CmsResource> getPublishResources() {

        List<CmsResource> result = new ArrayList<CmsResource>();
        if (m_currentResource != null) {
            result.add(m_currentResource);
            CmsObject cms = A_CmsUI.getCmsObject();
            CmsSolrQuery query = m_currentConfigParser.getInitialQuery();
            CmsSearchController controller = new CmsSearchController(new CmsSearchConfiguration(m_currentConfigParser));
            controller.getPagination().getState().setCurrentPage(1);
            controller.addQueryParts(query);

            CmsSolrIndex index = OpenCms.getSearchManager().getIndexSolr(CmsSolrIndex.DEFAULT_INDEX_NAME_OFFLINE);
            try {
                CmsSolrResultList solrResultList = index.search(cms, query, true, CmsResourceFilter.IGNORE_EXPIRATION);
                result.addAll(solrResultList);
            } catch (CmsSearchException e) {
                LOG.error("Error reading resources for publish.", e);
            }
        }
        return result;
    }

    /**
     * Returns whether the overview mode is active.<p>
     *
     * @return <code>true</code> in case the overview mode is active
     */
    boolean isOverView() {

        return m_isOverView;
    }

    /**
     * Opens the publish dialog to publish all resources related to the current search configuration.<p>
     */
    void publish() {

        I_CmsUpdateListener<String> updateListener = new I_CmsUpdateListener<String>() {

            @Override
            public void onUpdate(List<String> updatedItems) {

                updateItems(updatedItems);
            }
        };
        CmsAppWorkplaceUi.get().disableGlobalShortcuts();
        CmsGwtDialogExtension extension = new CmsGwtDialogExtension(A_CmsUI.get(), updateListener);
        extension.openPublishDialog(getPublishResources());
    }

    /**
     * Searches within the current list.<p>
     *
     * @param query the query string
     */
    void search(String query) {

        if (!m_resetting) {
            search(null, null, query);
        }
    }

    /**
     * Sorts the search result.<p>
     */
    void sortResult() {

        if (!m_resetting) {
            search(
                m_resultFacets.getSelectedFieldFacets(),
                m_resultFacets.getSelectedRangeFactes(),
                m_textSearch.getValue());
        }
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
     * Unlocks the current resource in case it has been locked by previous actions.<p>
     */
    void unlockCurrent() {

        if (m_currentResource != null) {
            if ((m_lockAction != null) && m_lockAction.getChange().equals(LockChange.locked)) {
                CmsLockUtil.tryUnlock(A_CmsUI.getCmsObject(), m_currentResource);
            }
        }
        m_lockAction = null;
    }

    /**
     * Updates the given items in the result table.<p>
     *
     * @param updatedItems the items to update
     */
    void updateItems(List<String> updatedItems) {

        Set<CmsUUID> ids = new HashSet<CmsUUID>();
        for (String id : updatedItems) {
            ids.add(new CmsUUID(id));
        }
        m_resultTable.update(ids, false);
    }

    /**
     * Displays the list config resources.<p>
     */
    private void displayListConfigs() {

        CmsObject cms = A_CmsUI.getCmsObject();
        resetTableFilter();
        try {
            // display the search configuration overview
            List<CmsResource> resources = cms.readResources(
                "/",
                CmsResourceFilter.ONLY_VISIBLE_NO_DELETED.addRequireType(
                    OpenCms.getResourceManager().getResourceType(RES_TYPE_LIST_CONFIG)));
            m_overviewTable.fillTable(cms, resources);
        } catch (Exception e) {
            CmsErrorDialog.showErrorDialog(e);
            LOG.error(e.getLocalizedMessage(), e);
        }
    }

    /**
     * Executes a search.<p>
     *
     * @param controller the search controller
     * @param query the SOLR query
     */
    private void executeSearch(CmsSearchController controller, CmsSolrQuery query) {

        CmsObject cms = A_CmsUI.getCmsObject();
        CmsSolrIndex index = OpenCms.getSearchManager().getIndexSolr(
            cms.getRequestContext().getCurrentProject().isOnlineProject()
            ? CmsSolrIndex.DEFAULT_INDEX_NAME_ONLINE
            : CmsSolrIndex.DEFAULT_INDEX_NAME_OFFLINE);
        try {
            CmsSolrResultList solrResultList = index.search(cms, query, true, CmsResourceFilter.IGNORE_EXPIRATION);
            displayResult(solrResultList);
            m_resultFacets.displayFacetResult(
                solrResultList,
                new CmsSearchResultWrapper(controller, solrResultList, query, cms, null));
        } catch (CmsSearchException e) {
            CmsErrorDialog.showErrorDialog(e);

            LOG.error("Error executing search.", e);
        }
    }

    /**
     * Returns the content locale configured for the first search root folder of the search configuration.<p>
     *
     * @param bean the search configuration data
     *
     * @return the locale
     */
    private Locale getContentLocale(ListConfigurationBean bean) {

        if (bean.getFolders().isEmpty()) {
            return CmsLocaleManager.getDefaultLocale();
        } else {
            return OpenCms.getLocaleManager().getDefaultLocale(
                A_CmsUI.getCmsObject(),
                m_currentConfig.getFolders().get(0));
        }
    }

    /**
     * Resets the locale select according to the current configuration data.<p>
     *
     * @param defaultLocale the default content locale
     */
    private void resetContentLocale(Locale defaultLocale) {

        m_resetting = true;
        m_localeSelect.removeAllItems();
        if (m_currentConfig.getFolders().isEmpty()) {
            m_localeSelect.addItem(defaultLocale);
            m_localeSelect.setItemCaption(defaultLocale, defaultLocale.getDisplayLanguage(UI.getCurrent().getLocale()));
        } else {
            for (String folder : m_currentConfig.getFolders()) {
                for (Locale locale : OpenCms.getLocaleManager().getAvailableLocales(A_CmsUI.getCmsObject(), folder)) {
                    if (!m_localeSelect.containsId(locale)) {
                        m_localeSelect.addItem(locale);
                        m_localeSelect.setItemCaption(locale, locale.getDisplayLanguage(UI.getCurrent().getLocale()));
                    }
                }
            }
        }
        m_localeSelect.setValue(defaultLocale);
        m_localeSelect.setEnabled(m_localeSelect.getItemIds().size() > 1);
        m_resetting = false;
    }

    /**
     * Resets the table filter.<p>
     */
    private void resetTableFilter() {

        m_resetting = true;
        m_tableFilter.clear();
        m_resetting = false;
    }

    /**
     * Resets the text search input.<p>
     */
    private void resetTextSearch() {

        m_resetting = true;
        m_textSearch.clear();
        m_resetting = false;
    }
}
