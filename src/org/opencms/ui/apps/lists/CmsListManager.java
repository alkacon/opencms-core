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

import org.opencms.acacia.shared.I_CmsSerialDateValue;
import org.opencms.ade.configuration.CmsADEConfigData;
import org.opencms.ade.configuration.CmsResourceTypeConfig;
import org.opencms.ade.containerpage.shared.CmsDialogOptions;
import org.opencms.file.CmsFile;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsPropertyDefinition;
import org.opencms.file.CmsResource;
import org.opencms.file.CmsResourceFilter;
import org.opencms.file.types.CmsResourceTypeXmlContent;
import org.opencms.file.types.I_CmsResourceType;
import org.opencms.gwt.shared.CmsResourceStatusTabId;
import org.opencms.i18n.CmsLocaleManager;
import org.opencms.jsp.search.config.CmsSearchConfiguration;
import org.opencms.jsp.search.config.CmsSearchConfigurationFacetField;
import org.opencms.jsp.search.config.CmsSearchConfigurationFacetRange;
import org.opencms.jsp.search.config.CmsSearchConfigurationPagination;
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
import org.opencms.jsp.search.config.parser.CmsSimpleSearchConfigurationParser.SortOption;
import org.opencms.jsp.search.config.parser.I_CmsSearchConfigurationParser;
import org.opencms.jsp.search.controller.CmsSearchController;
import org.opencms.jsp.search.controller.I_CmsSearchControllerFacetField;
import org.opencms.jsp.search.controller.I_CmsSearchControllerFacetRange;
import org.opencms.jsp.search.result.CmsSearchResultWrapper;
import org.opencms.jsp.search.state.I_CmsSearchStateFacet;
import org.opencms.loader.CmsLoaderException;
import org.opencms.lock.CmsLockActionRecord;
import org.opencms.lock.CmsLockActionRecord.LockChange;
import org.opencms.lock.CmsLockUtil;
import org.opencms.main.CmsException;
import org.opencms.main.CmsLog;
import org.opencms.main.OpenCms;
import org.opencms.relations.CmsLink;
import org.opencms.search.CmsSearchException;
import org.opencms.search.CmsSearchResource;
import org.opencms.search.fields.CmsSearchField;
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
import org.opencms.ui.actions.CmsDeleteDialogAction;
import org.opencms.ui.actions.CmsEditDialogAction;
import org.opencms.ui.actions.CmsResourceInfoAction;
import org.opencms.ui.apps.A_CmsWorkplaceApp;
import org.opencms.ui.apps.CmsAppWorkplaceUi;
import org.opencms.ui.apps.CmsEditor;
import org.opencms.ui.apps.CmsFileExplorer;
import org.opencms.ui.apps.I_CmsAppUIContext;
import org.opencms.ui.apps.I_CmsContextProvider;
import org.opencms.ui.apps.Messages;
import org.opencms.ui.apps.lists.CmsOptionDialog.I_OptionHandler;
import org.opencms.ui.apps.projects.CmsProjectManagerConfiguration;
import org.opencms.ui.components.CmsBasicDialog;
import org.opencms.ui.components.CmsErrorDialog;
import org.opencms.ui.components.CmsFileTable;
import org.opencms.ui.components.CmsFileTableDialogContext;
import org.opencms.ui.components.CmsResourceTable;
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
import org.opencms.workplace.editors.directedit.CmsDateSeriesEditHandler;
import org.opencms.workplace.editors.directedit.I_CmsEditHandler;
import org.opencms.workplace.explorer.CmsResourceUtil;
import org.opencms.workplace.explorer.menu.CmsMenuItemVisibilityMode;
import org.opencms.xml.containerpage.CmsContainerElementBean;
import org.opencms.xml.content.CmsXmlContent;
import org.opencms.xml.content.CmsXmlContentFactory;
import org.opencms.xml.types.CmsXmlDisplayFormatterValue;
import org.opencms.xml.types.CmsXmlVfsFileValue;
import org.opencms.xml.types.I_CmsXmlContentValue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
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
import com.vaadin.ui.AbstractSelect.ItemDescriptionGenerator;
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

        /** The additional content parameters. */
        private Map<String, String> m_additionalParameters;

        /**
         * Constructor.<p>
         */
        public ListConfigurationBean() {
            m_parameterFields = new HashMap<String, String>();
        }

        /**
         * Returns the additional content parameters.<p>
         *
         * @return the additional content parameters
         */
        public Map<String, String> getAdditionalParameters() {

            return m_additionalParameters;
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
         * Sets the additional content parameters.<p>
         *
         * @param additionalParameters the additional content parameters to set
         */
        public void setAdditionalParameters(Map<String, String> additionalParameters) {

            m_additionalParameters = additionalParameters;
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
        private SortOption m_sortOption;

        /** The current items only flag. */
        boolean m_currentOnly;

        /** The category facet conjunction flag. */
        boolean m_categoryConjunction;

        /** The collapse item series flag. */
        boolean m_collapseItemSeries;

        /**
         * Constructor.<p>
         *
         * @param types the selected types
         * @param folders the selected folders
         * @param categories the selected categories
         * @param filterQuery the filter query
         * @param currentOnly the current items only flag
         * @param sortOption the sort option
         * @param showExpired the show expired flag
         * @param categoryConjunction the category facet conjunction flag
         * @param collapseItemSeries  the collapse item series flag
         * @param contentLocale the content locale
         */
        public SearchConfigParser(
            List<String> types,
            List<String> folders,
            List<String> categories,
            String filterQuery,
            boolean currentOnly,
            String sortOption,
            boolean showExpired,
            boolean categoryConjunction,
            boolean collapseItemSeries,
            Locale contentLocale) {
            m_selectedTypes = types;
            m_selectedFolders = folders;
            m_selectedCategories = categories;
            setSortOption(sortOption);
            m_contentLocale = contentLocale;
            m_filterQuery = filterQuery;
            m_currentOnly = currentOnly;
            m_showExpired = showExpired;
            m_categoryConjunction = categoryConjunction;
            m_collapseItemSeries = collapseItemSeries;
        }

        /**
         * Constructor.<p>
         *
         * @param configBean the configuration data bean
         * @param collapseItemSeries  the collapse item series flag
         * @param locale the search content locale
         */
        public SearchConfigParser(ListConfigurationBean configBean, boolean collapseItemSeries, Locale locale) {
            this(
                configBean.getTypes(),
                configBean.getFolders(),
                configBean.getCategories(),
                configBean.getParameterValue(N_FILTER_QUERY),
                Boolean.parseBoolean(configBean.getParameterValue(N_CURRENT_ONLY)),
                configBean.getParameterValue(N_SORT_ORDER),
                Boolean.parseBoolean(configBean.getParameterValue(N_SHOW_EXPIRED)),
                Boolean.parseBoolean(configBean.getParameterValue(N_CATEGORY_CONJUNCTION)),
                collapseItemSeries,
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

                    return getFolderFilter()
                        + getResourceTypeFilter()
                        + getCategoryFilter()
                        + getFilterQuery()
                        + getLocaleFilter();
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
                    Boolean.valueOf(m_categoryConjunction),
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
                String.format(FIELD_DATE, m_contentLocale.toString()),
                "NOW/YEAR-20YEARS",
                "NOW/MONTH+2YEARS",
                "+1MONTHS",
                null,
                Boolean.FALSE,
                FIELD_DATE_FACET_NAME,
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
            if (null != m_sortOption) {
                defaultOption = m_sortOption.getOption(m_contentLocale);
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

            if (null != sortOption) {
                try {
                    m_sortOption = SortOption.valueOf(sortOption);
                } catch (IllegalArgumentException e) {
                    m_sortOption = null;
                    LOG.warn(
                        "Setting illegal default sort option "
                            + sortOption
                            + " failed. Using Solr's default sort option.");
                }
            } else {
                m_sortOption = null;
            }
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

            String result = "";
            if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(m_filterQuery)) {
                if (!m_filterQuery.startsWith("&")) {
                    result += "&" + m_filterQuery;
                } else {
                    result += m_filterQuery;
                }
            }
            if (m_currentOnly) {
                result += "&fq=instancedatecurrenttill_" + m_contentLocale.toString() + "_dt:[NOW/DAY TO *]";
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
         * Returns the locale filter.<p>
         *
         * @return the locale filter
         */
        String getLocaleFilter() {

            return "&fq=con_locales:" + getContentLocale().toString();
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

    /**
     * Extended dialog context.<p>
     */
    protected class DialogContext extends CmsFileTableDialogContext {

        /** The selected table items. */
        private List<Item> m_selectedItems;

        /**
         * Constructor.<p>
         *
         * @param appId the app id
         * @param contextType the context type
         * @param fileTable the file table instance
         * @param resources the list of selected resources
         */
        public DialogContext(
            String appId,
            ContextType contextType,
            CmsFileTable fileTable,
            List<CmsResource> resources) {
            super(appId, contextType, fileTable, resources);

        }

        /**
         * @see org.opencms.ui.components.CmsFileTableDialogContext#finish(java.util.Collection)
         */
        @Override
        public void finish(Collection<CmsUUID> ids) {

            if (m_selectedItems == null) {
                super.finish(ids);
            } else {
                refreshResult();
            }
        }

        /**
         * Returns the selected table items.<p>
         *
         * @return the selected table items
         */
        public List<Item> getSelectedItems() {

            return m_selectedItems;
        }

        /**
         * Sets the table items.<p>
         *
         * @param items the table items
         */
        public void setSelectedItems(List<Item> items) {

            m_selectedItems = items;
        }
    }

    /**
     * Overrides the standard delete action to enable use of edit handlers.<p>
     */
    class DeleteAction extends CmsDeleteDialogAction {

        /**
         * @see org.opencms.ui.actions.CmsDeleteDialogAction#executeAction(org.opencms.ui.I_CmsDialogContext)
         */
        @Override
        public void executeAction(final I_CmsDialogContext context) {

            final CmsResource resource = context.getResources().get(0);
            I_CmsResourceType resourceType = OpenCms.getResourceManager().getResourceType(resource);
            if ((resourceType instanceof CmsResourceTypeXmlContent)
                && (((CmsResourceTypeXmlContent)resourceType).getEditHandler(context.getCms()) != null)) {
                final I_CmsEditHandler handler = ((CmsResourceTypeXmlContent)resourceType).getEditHandler(
                    context.getCms());
                final CmsContainerElementBean elementBean = getElementForEditHandler((DialogContext)context);
                CmsDialogOptions options = handler.getDeleteOptions(
                    context.getCms(),
                    elementBean,
                    null,
                    Collections.<String, String[]> emptyMap());
                if (options == null) {
                    super.executeAction(context);
                } else if (options.getOptions().size() == 1) {
                    deleteForOption(resource, elementBean, options.getOptions().get(0).getValue(), handler, context);
                } else {
                    Window window = CmsBasicDialog.prepareWindow();
                    window.setCaption(options.getTitle());
                    CmsOptionDialog dialog = new CmsOptionDialog(resource, options, new I_OptionHandler() {

                        public void handleOption(String option) {

                            deleteForOption(resource, elementBean, option, handler, context);
                        }
                    }, null, window);
                    window.setContent(dialog);
                    CmsAppWorkplaceUi.get().addWindow(window);
                    dialog.initActionHandler(window);

                }
            } else {
                super.executeAction(context);
            }
        }

        /**
         * Deletes with the given option.<p>
         *
         * @param resource the resource to delete
         * @param elementBean the element bean
         * @param deleteOption the delete option
         * @param editHandler edit handler
         * @param context the dialog context
         */
        void deleteForOption(
            CmsResource resource,
            CmsContainerElementBean elementBean,
            String deleteOption,
            I_CmsEditHandler editHandler,
            I_CmsDialogContext context) {

            try {
                editHandler.handleDelete(context.getCms(), elementBean, deleteOption, null, null);
            } catch (CmsException e) {
                CmsErrorDialog.showErrorDialog(e);
            }
        }
    }

    /**
     * Overrides the standard delete action to enable use of edit handlers.<p>
     */
    class EditAction extends CmsEditDialogAction {

        /**
         * @see org.opencms.ui.actions.CmsEditDialogAction#executeAction(org.opencms.ui.I_CmsDialogContext)
         */
        @Override
        public void executeAction(final I_CmsDialogContext context) {

            final CmsResource resource = context.getResources().get(0);
            I_CmsResourceType resourceType = OpenCms.getResourceManager().getResourceType(resource);
            if ((resourceType instanceof CmsResourceTypeXmlContent)
                && (((CmsResourceTypeXmlContent)resourceType).getEditHandler(context.getCms()) != null)) {
                final I_CmsEditHandler handler = ((CmsResourceTypeXmlContent)resourceType).getEditHandler(
                    context.getCms());
                final CmsContainerElementBean elementBean = getElementForEditHandler((DialogContext)context);
                CmsDialogOptions options = handler.getEditOptions(
                    context.getCms(),
                    elementBean,
                    null,
                    Collections.<String, String[]> emptyMap(),
                    true);
                if (options == null) {
                    super.executeAction(context);
                } else if (options.getOptions().size() == 1) {
                    editForOption(resource, elementBean, options.getOptions().get(0).getValue(), handler, context);
                } else {
                    Window window = CmsBasicDialog.prepareWindow();
                    window.setCaption(options.getTitle());
                    CmsOptionDialog dialog = new CmsOptionDialog(resource, options, new I_OptionHandler() {

                        public void handleOption(String option) {

                            editForOption(resource, elementBean, option, handler, context);
                        }
                    }, null, window);
                    window.setContent(dialog);
                    CmsAppWorkplaceUi.get().addWindow(window);
                    dialog.initActionHandler(window);

                }
            } else {
                super.executeAction(context);
            }
        }

        /**
         * Calls the editor with the given option.<p>
         *
         * @param resource the resource to delete
         * @param elementBean the element bean
         * @param editOption the edit option
         * @param editHandler edit handler
         * @param context the dialog context
         */
        void editForOption(
            CmsResource resource,
            CmsContainerElementBean elementBean,
            String editOption,
            I_CmsEditHandler editHandler,
            I_CmsDialogContext context) {

            try {
                CmsUUID id = editHandler.prepareForEdit(
                    context.getCms(),
                    elementBean,
                    editOption,
                    null,
                    Collections.<String, String[]> emptyMap());
                if (resource.getStructureId().equals(id)) {
                    super.executeAction(context);
                } else if (id != null) {
                    CmsFileTableDialogContext changedContext = new CmsFileTableDialogContext(
                        CmsProjectManagerConfiguration.APP_ID,
                        ContextType.fileTable,
                        m_resultTable,
                        Collections.singletonList(context.getCms().readResource(id)));
                    super.executeAction(changedContext);
                }
            } catch (CmsException e) {
                CmsErrorDialog.showErrorDialog(e);
            }
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
    public static final String N_PARAMETER = "Parameter";

    /** List configuration node name and field key. */
    public static final String N_KEY = "Key";

    /** List configuration node name and field key. */
    public static final String N_VALUE = "Value";

    /** List configuration node name and field key. */
    public static final String N_CATEGORY = "Category";

    /** List configuration node name and field key. */
    public static final String N_DISPLAY_TYPE = "TypesToCollect";

    /** List configuration node name and field key. */
    public static final String N_FILTER_QUERY = "FilterQuery";

    /** List configuration node name and field key. */
    public static final String N_SEARCH_FOLDER = "SearchFolder";

    /** List configuration node name and field key. */
    public static final String N_SHOW_EXPIRED = "ShowExpired";

    /** List configuration node name and field key. */
    public static final String N_SORT_ORDER = "SortOrder";

    /** List configuration node name and field key. */
    public static final String N_TITLE = "Title";

    /** List configuration node name and field key. */
    public static final String N_CURRENT_ONLY = "CurrentOnly";

    /** List configuration node name and field key. */
    public static final String N_CATEGORY_CONJUNCTION = "CategoryConjunction";

    /** List configuration node name and field key. */
    public static final String PARAM_LOCALE = "locale";

    /** The parameter fields. */
    public static final String[] PARAMETER_FIELDS = new String[] {
        N_TITLE,
        N_CATEGORY,
        N_FILTER_QUERY,
        N_SORT_ORDER,
        N_SHOW_EXPIRED,
        N_CATEGORY_CONJUNCTION,
        N_CURRENT_ONLY};

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
        0,
        110);

    /** The date series info label table column property id. */
    protected static final CmsResourceTableProperty INFO_PROPERTY_LABEL = new CmsResourceTableProperty(
        "INFO_PROPERTY_LABEL",
        String.class,
        null,
        Messages.GUI_LISTMANAGER_COLUMN_INFO_0,
        true,
        0,
        110);

    /** The date series info table column property id. */
    protected static final CmsResourceTableProperty INFO_PROPERTY = new CmsResourceTableProperty(
        "INFO_PROPERTY",
        String.class,
        null,
        null,
        true,
        1,
        0);

    /** The blacklisted table column property id. */
    protected static final CmsResourceTableProperty INSTANCEDATE_PROPERTY = new CmsResourceTableProperty(
        "INSTANCEDATE_PROPERTY",
        Date.class,
        null,
        Messages.GUI_LISTMANAGER_COLUMN_INSTANCEDATE_0,
        true,
        0,
        145);

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

    /** The logger for this class. */
    static Log LOG = CmsLog.getLog(CmsListManager.class.getName());

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

    /** The locale select. */
    private ComboBox m_localeSelect;

    /** The current lock action. */
    private CmsLockActionRecord m_lockAction;

    /** The list configurations overview table. */
    private CmsFileTable m_overviewTable;

    /** The publish button. */
    private Button m_publishButton;

    /** The info button. */
    private Button m_infoButton;

    /** The resetting flag. */
    private boolean m_resetting;

    /** The facet result display. */
    private CmsResultFacets m_resultFacets;

    /** The mail layout. */
    private HorizontalSplitPanel m_resultLayout;

    /** The sort select. */
    private ComboBox m_resultSorter;

    /** The result table. */
    CmsResultTable m_resultTable;

    /** The table filter input. */
    private TextField m_tableFilter;

    /** The text search input. */
    private TextField m_textSearch;

    /** The collapse item series flag. */
    private boolean m_collapseItemSeries;

    /**
     * @see org.opencms.ui.components.CmsResourceTable.I_ResourcePropertyProvider#addItemProperties(com.vaadin.data.Item, org.opencms.file.CmsObject, org.opencms.file.CmsResource, java.util.Locale)
     */
    @Override
    public void addItemProperties(Item resourceItem, CmsObject cms, CmsResource resource, Locale locale) {

        if ((m_currentConfig != null) && (resourceItem.getItemProperty(CmsListManager.BLACKLISTED_PROPERTY) != null)) {
            resourceItem.getItemProperty(CmsListManager.BLACKLISTED_PROPERTY).setValue(
                Boolean.valueOf(m_currentConfig.getBlacklist().contains(resource.getStructureId())));
        }
        if ((m_currentConfig != null)
            && (resourceItem.getItemProperty(CmsResourceTableProperty.PROPERTY_TITLE) != null)) {
            CmsResourceUtil resUtil = new CmsResourceUtil(cms, resource);
            String title = resUtil.getGalleryTitle((Locale)m_localeSelect.getValue());
            if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(title)) {
                resourceItem.getItemProperty(CmsResourceTableProperty.PROPERTY_TITLE).setValue(title);
            }
        }
        if ((resource instanceof CmsSearchResource)
            && (resourceItem.getItemProperty(CmsListManager.INFO_PROPERTY_LABEL) != null)) {
            String seriesType = ((CmsSearchResource)resource).getField(CmsSearchField.FIELD_SERIESDATES_TYPE);
            resourceItem.getItemProperty(CmsListManager.INFO_PROPERTY_LABEL).setValue(
                CmsVaadinUtils.getMessageText(
                    "GUI_LISTMANAGER_SERIES_TYPE_" + (seriesType == null ? "DEFAULT" : seriesType) + "_0"));
            resourceItem.getItemProperty(CmsListManager.INFO_PROPERTY).setValue(seriesType);

        }
        if ((resource instanceof CmsSearchResource)
            && (resourceItem.getItemProperty(CmsListManager.INSTANCEDATE_PROPERTY) != null)) {
            Date date = ((CmsSearchResource)resource).getDateField(m_resultTable.getDateFieldKey());
            resourceItem.getItemProperty(CmsListManager.INSTANCEDATE_PROPERTY).setValue(date);
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
        DialogContext context = new DialogContext(
            CmsProjectManagerConfiguration.APP_ID,
            ContextType.fileTable,
            table,
            table.getSelectedResources());
        if (!isOverView()) {
            context.setSelectedItems(m_resultTable.getSelectedItems());
        }
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

        m_infoButton = CmsToolBar.createButton(
            FontOpenCms.INFO,
            CmsVaadinUtils.getMessageText(org.opencms.ui.Messages.GUI_RESOURCE_INFO_0));
        m_infoButton.addClickListener(new ClickListener() {

            /** Serial version id. */
            private static final long serialVersionUID = 1L;

            @Override
            public void buttonClick(ClickEvent event) {

                DialogContext context = new DialogContext(
                    CmsProjectManagerConfiguration.APP_ID,
                    ContextType.fileTable,
                    m_resultTable,
                    Collections.singletonList(m_currentResource));
                CmsResourceInfoAction action = new CmsResourceInfoAction();
                action.openDialog(context, CmsResourceStatusTabId.tabRelationsTo.name());
            }
        });
        uiContext.addToolbarButton(m_infoButton);

        m_createNewButton = CmsToolBar.createButton(
            FontOpenCms.WAND,
            CmsVaadinUtils.getMessageText(Messages.GUI_LISTMANAGER_CREATE_NEW_0));
        m_createNewButton.addClickListener(new ClickListener() {

            /** Serial version id. */
            private static final long serialVersionUID = 1L;

            @Override
            public void buttonClick(ClickEvent event) {

                createNew();
            }
        });
        uiContext.addToolbarButton(m_createNewButton);

        m_editCurrentButton = CmsToolBar.createButton(
            FontOpenCms.PEN,
            CmsVaadinUtils.getMessageText(Messages.GUI_LISTMANAGER_EDIT_CONFIG_0));
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
        // insert columns a specific positions
        for (Entry<CmsResourceTableProperty, Integer> columnsEntry : CmsFileTable.DEFAULT_TABLE_PROPERTIES.entrySet()) {
            if (columnsEntry.getKey().equals(CmsResourceTableProperty.PROPERTY_RESOURCE_NAME)) {
                tableColumns.put(INFO_PROPERTY_LABEL, Integer.valueOf(0));
            } else if (columnsEntry.getKey().equals(CmsResourceTableProperty.PROPERTY_RESOURCE_TYPE)) {
                tableColumns.put(INSTANCEDATE_PROPERTY, Integer.valueOf(0));
            }
            tableColumns.put(columnsEntry.getKey(), columnsEntry.getValue());
        }
        tableColumns.put(BLACKLISTED_PROPERTY, Integer.valueOf(CmsResourceTable.INVISIBLE));
        tableColumns.put(INFO_PROPERTY, Integer.valueOf(CmsResourceTable.INVISIBLE));
        m_resultTable = new CmsResultTable(null, tableColumns);
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

                return Arrays.<I_CmsContextMenuItem> asList(
                    new CmsContextMenuActionItem(new A_CmsWorkplaceAction() {

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

                    }, null, 10, 0),
                    new CmsContextMenuActionItem(new A_CmsWorkplaceAction() {

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

                    }, null, 10, 0),
                    new CmsContextMenuActionItem(new EditAction(), null, 10, 1000),
                    new CmsContextMenuActionItem(new DeleteAction(), null, 10, 1000));
            }
        });
        m_resultTable.setMenuBuilder(menuBuilder);
        m_resultTable.addAdditionalStyleGenerator(new CellStyleGenerator() {

            private static final long serialVersionUID = 1L;

            @Override
            public String getStyle(Table source, Object itemId, Object propertyId) {

                String style = "";
                Item item = source.getItem(itemId);
                Boolean blacklisted = (Boolean)item.getItemProperty(BLACKLISTED_PROPERTY).getValue();
                if (blacklisted.booleanValue()) {
                    style += OpenCmsTheme.PROJECT_OTHER + " ";
                } else if (CmsResourceTableProperty.PROPERTY_TITLE.equals(propertyId)
                    && ((item.getItemProperty(CmsResourceTableProperty.PROPERTY_RELEASED_NOT_EXPIRED) == null)
                        || ((Boolean)item.getItemProperty(
                            CmsResourceTableProperty.PROPERTY_RELEASED_NOT_EXPIRED).getValue()).booleanValue())) {
                    style += OpenCmsTheme.IN_NAVIGATION + " ";
                }
                if (INFO_PROPERTY_LABEL.equals(propertyId)) {
                    if (blacklisted.booleanValue()) {
                        style += OpenCmsTheme.TABLE_COLUMN_BOX_BLACK;
                    } else {
                        Object value = item.getItemProperty(INFO_PROPERTY).getValue();
                        if (value == null) {
                            style += OpenCmsTheme.TABLE_COLUMN_BOX_GRAY;
                        } else {
                            I_CmsSerialDateValue.DateType type = I_CmsSerialDateValue.DateType.valueOf((String)value);
                            switch (type) {
                                case SERIES:
                                    style += OpenCmsTheme.TABLE_COLUMN_BOX_BLUE_LIGHT;
                                    break;
                                case SINGLE:
                                    style += OpenCmsTheme.TABLE_COLUMN_BOX_GRAY;
                                    break;
                                case EXTRACTED:
                                    style += OpenCmsTheme.TABLE_COLUMN_BOX_ORANGE;
                                    break;
                                default:
                                    break;
                            }
                        }
                    }
                }
                return style;
            }
        });
        m_resultTable.setsetItemDescriptionGenerator(new ItemDescriptionGenerator() {

            private static final long serialVersionUID = 1L;

            public String generateDescription(Component source, Object itemId, Object propertyId) {

                Item item = ((Table)source).getItem(itemId);
                if (INFO_PROPERTY_LABEL.equals(propertyId)
                    && ((Boolean)item.getItemProperty(BLACKLISTED_PROPERTY).getValue()).booleanValue()) {
                    return CmsVaadinUtils.getMessageText(Messages.GUI_LISTMANAGER_COLUMN_BLACKLISTED_0);
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
        for (int i = 0; i < SORT_OPTIONS[0].length; i++) {
            m_resultSorter.addItem(SORT_OPTIONS[0][i]);
            m_resultSorter.setItemCaption(SORT_OPTIONS[0][i], CmsVaadinUtils.getMessageText(SORT_OPTIONS[1][i]));
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
        m_textSearch.setInputPrompt(CmsVaadinUtils.getMessageText(Messages.GUI_LISTMANAGER_SEARCH_0));
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
            Locale locale = CmsLocaleManager.MASTER_LOCALE;

            if (!content.hasLocale(locale)) {
                locale = content.getLocales().get(0);
            }
            for (String field : PARAMETER_FIELDS) {
                String val = content.getStringValue(cms, field, locale);
                if (N_CATEGORY.equals(field)) {
                    if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(val)) {
                        result.setCategories(Arrays.asList(val.split(",")));
                    } else {
                        result.setCategories(Collections.<String> emptyList());
                    }
                } else {
                    result.setParameterValue(field, val);
                }
            }
            LinkedHashMap<String, String> parameters = new LinkedHashMap<String, String>();
            for (I_CmsXmlContentValue parameter : content.getValues(N_PARAMETER, locale)) {
                I_CmsXmlContentValue keyVal = content.getValue(parameter.getPath() + "/" + N_KEY, locale);
                I_CmsXmlContentValue valueVal = content.getValue(parameter.getPath() + "/" + N_VALUE, locale);
                if ((keyVal != null)
                    && CmsStringUtil.isNotEmptyOrWhitespaceOnly(keyVal.getStringValue(cms))
                    && (valueVal != null)) {
                    parameters.put(keyVal.getStringValue(cms), valueVal.getStringValue(cms));
                }
            }
            result.setAdditionalParameters(parameters);
            List<String> displayTypes = new ArrayList<String>();
            List<I_CmsXmlContentValue> typeValues = content.getValues(N_DISPLAY_TYPE, locale);
            if (!typeValues.isEmpty()) {
                for (I_CmsXmlContentValue value : typeValues) {
                    displayTypes.add(value.getStringValue(cms));
                }
            }
            result.setDisplayTypes(displayTypes);
            List<String> folders = new ArrayList<String>();
            List<I_CmsXmlContentValue> folderValues = content.getValues(N_SEARCH_FOLDER, locale);
            if (!folderValues.isEmpty()) {
                for (I_CmsXmlContentValue value : folderValues) {
                    String val = value.getStringValue(cms);
                    // we are using root paths
                    folders.add(cms.getRequestContext().addSiteRoot(val));
                }
            }
            result.setFolders(folders);
            List<CmsUUID> blackList = new ArrayList<CmsUUID>();
            List<I_CmsXmlContentValue> blacklistValues = content.getValues(N_BLACKLIST, locale);
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
                    I_CmsXmlContentValue contentVal = content.getValue(N_DISPLAY_TYPE, locale, count);
                    if (contentVal == null) {
                        contentVal = content.addValue(cms, N_DISPLAY_TYPE, locale, count);
                    }
                    contentVal.setStringValue(cms, type);
                    count++;
                }
                count = 0;
                for (String folder : configBean.getFolders()) {
                    I_CmsXmlContentValue contentVal = content.getValue(N_SEARCH_FOLDER, locale, count);
                    if (contentVal == null) {
                        contentVal = content.addValue(cms, N_SEARCH_FOLDER, locale, count);
                    }
                    contentVal.setStringValue(cms, folder);
                    count++;
                }
                count = 0;
                for (CmsUUID hiddenId : configBean.getBlacklist()) {
                    CmsXmlVfsFileValue contentVal = (CmsXmlVfsFileValue)content.getValue(N_BLACKLIST, locale, count);
                    if (contentVal == null) {
                        contentVal = (CmsXmlVfsFileValue)content.addValue(cms, N_BLACKLIST, locale, count);
                    }
                    contentVal.setIdValue(cms, hiddenId);
                    count++;
                }
                if (configBean.getAdditionalParameters() != null) {
                    count = 0;
                    for (Entry<String, String> paramEntry : configBean.getAdditionalParameters().entrySet()) {
                        I_CmsXmlContentValue paramValue = content.addValue(cms, N_PARAMETER, locale, count);

                        I_CmsXmlContentValue keyVal = content.getValue(paramValue.getPath() + "/" + N_KEY, locale);
                        if (keyVal == null) {
                            keyVal = content.addValue(cms, paramValue.getPath() + "/" + N_KEY, locale, 0);
                        }
                        keyVal.setStringValue(cms, paramEntry.getKey());
                        I_CmsXmlContentValue valueVal = content.getValue(paramValue.getPath() + "/" + N_VALUE, locale);
                        if (valueVal == null) {
                            valueVal = content.addValue(cms, paramValue.getPath() + "/" + N_VALUE, locale, 0);
                        }
                        valueVal.setStringValue(cms, paramEntry.getValue());
                        count++;
                    }
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
                search(new SearchConfigParser(configBean, m_collapseItemSeries, contentLocale), m_currentResource);
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
        m_resultSorter.setValue(m_currentConfig.getParameterValue(N_SORT_ORDER));
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
            crumbs.put("", CmsVaadinUtils.getMessageText(Messages.GUI_LISTMANAGER_VIEW_1, title));
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
                String localeString = A_CmsWorkplaceApp.getParamFromState(state, PARAM_LOCALE);
                Locale locale;
                if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(localeString)) {
                    locale = CmsLocaleManager.getLocale(localeString);
                } else {
                    locale = getContentLocale(m_currentConfig);
                }
                search(new SearchConfigParser(m_currentConfig, m_collapseItemSeries, locale), res);
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
        m_resultTable.setContentLocale(contentLocale);
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
        String state = A_CmsWorkplaceApp.addParamToState(
            "",
            CmsEditor.RESOURCE_ID_PREFIX,
            m_currentResource.getStructureId().toString());
        state = A_CmsWorkplaceApp.addParamToState(
            state,
            PARAM_LOCALE,
            m_currentConfigParser.getContentLocale().toString());
        CmsAppWorkplaceUi.get().changeCurrentAppState(state);
        if (m_isOverView) {
            enableOverviewMode(false);
            updateBreadCrumb(getBreadCrumbForState(state));
        }
    }

    /**
     * Edits the given list configuration resource.<p>
     *
     * @param resource the cofiguration resource
     */
    void editListConfiguration(CmsResource resource) {

        try {
            CmsObject cms = A_CmsUI.getCmsObject();
            String editState;
            if (resource == null) {
                editState = CmsEditor.getEditStateForNew(
                    cms,
                    OpenCms.getResourceManager().getResourceType(RES_TYPE_LIST_CONFIG),
                    "/",
                    null,
                    false,
                    UI.getCurrent().getPage().getLocation().toString());
            } else {
                editState = CmsEditor.getEditState(
                    resource.getStructureId(),
                    false,
                    UI.getCurrent().getPage().getLocation().toString());
            }
            CmsAppWorkplaceUi.get().showApp(OpenCms.getWorkplaceAppManager().getAppConfiguration("editor"), editState);

        } catch (CmsLoaderException e) {
            CmsErrorDialog.showErrorDialog(e);
        }
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
        m_infoButton.setVisible(!enabled);
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
     * Creates an element bean of the selected table item to be used with edit handlers.<p>
     *
     * @param context the dialog context
     *
     * @return the element bean
     */
    CmsContainerElementBean getElementForEditHandler(DialogContext context) {

        List<Item> selected = context.getSelectedItems();
        if (selected.size() == 1) {
            Item item = selected.get(0);
            Date instanceDate = (Date)item.getItemProperty(INSTANCEDATE_PROPERTY).getValue();
            CmsResource resource = context.getResources().get(0);
            return new CmsContainerElementBean(
                resource.getStructureId(),
                null,
                Collections.singletonMap(
                    CmsDateSeriesEditHandler.PARAM_INSTANCEDATE,
                    String.valueOf(instanceDate.getTime())),
                false);
        }
        return null;
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
     * Refreshes the search result maintaining the current scroll position.<p>
     */
    void refreshResult() {

        String itemId = m_resultTable.getCurrentPageFirstItemId();
        search(
            m_resultFacets.getSelectedFieldFacets(),
            m_resultFacets.getSelectedRangeFactes(),
            m_textSearch.getValue());
        m_resultTable.setCurrentPageFirstItemId(itemId);
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
