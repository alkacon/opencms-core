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

package org.opencms.ui.apps.datesearch;

import org.opencms.file.CmsObject;
import org.opencms.file.CmsResource;
import org.opencms.file.CmsResourceFilter;
import org.opencms.file.types.I_CmsResourceType;
import org.opencms.jsp.search.config.parser.simplesearch.daterestrictions.CmsDateRangeRestriction;
import org.opencms.loader.CmsLoaderException;
import org.opencms.main.CmsException;
import org.opencms.main.CmsLog;
import org.opencms.main.OpenCms;
import org.opencms.search.CmsSearchException;
import org.opencms.search.CmsSearchResource;
import org.opencms.search.fields.CmsSearchField;
import org.opencms.search.solr.CmsSolrIndex;
import org.opencms.search.solr.CmsSolrQuery;
import org.opencms.search.solr.CmsSolrResultList;
import org.opencms.ui.A_CmsUI;
import org.opencms.ui.CmsVaadinUtils;
import org.opencms.ui.I_CmsDialogContext;
import org.opencms.ui.I_CmsDialogContext.ContextType;
import org.opencms.ui.apps.CmsAppWorkplaceUi;
import org.opencms.ui.apps.I_CmsContextProvider;
import org.opencms.ui.apps.Messages;
import org.opencms.ui.apps.lists.CmsListManager;
import org.opencms.ui.apps.lists.CmsResultTable;
import org.opencms.ui.components.CmsAvailabilitySelector;
import org.opencms.ui.components.CmsComponentState;
import org.opencms.ui.components.CmsDateField;
import org.opencms.ui.components.CmsErrorDialog;
import org.opencms.ui.components.CmsFileTable;
import org.opencms.ui.components.CmsFileTableDialogContext;
import org.opencms.ui.components.CmsFolderSelector;
import org.opencms.ui.components.CmsResourceTable.I_ResourcePropertyProvider;
import org.opencms.ui.components.CmsResourceTableProperty;
import org.opencms.ui.components.CmsResultFacets;
import org.opencms.ui.components.CmsResultFilterComponent;
import org.opencms.ui.components.CmsSiteSelector;
import org.opencms.ui.components.CmsTypeSelector;
import org.opencms.ui.components.I_CmsResultFacetsManager;
import org.opencms.ui.components.OpenCmsTheme;
import org.opencms.ui.contextmenu.CmsResourceContextMenuBuilder;
import org.opencms.util.CmsStringUtil;
import org.opencms.util.CmsUUID;
import org.opencms.workplace.explorer.CmsExplorerTypeSettings;
import org.opencms.workplace.explorer.CmsResourceUtil;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.solr.client.solrj.SolrQuery.ORDER;
import org.apache.solr.client.solrj.response.FacetField.Count;
import org.apache.solr.client.solrj.util.ClientUtils;

import com.vaadin.shared.ui.datefield.DateTimeResolution;
import com.vaadin.ui.Component;
import com.vaadin.ui.FormLayout;
import com.vaadin.ui.TextField;
import com.vaadin.v7.data.Item;
import com.vaadin.v7.data.Property.ValueChangeEvent;
import com.vaadin.v7.data.Property.ValueChangeListener;
import com.vaadin.v7.data.util.filter.Or;
import com.vaadin.v7.data.util.filter.SimpleStringFilter;
import com.vaadin.v7.event.FieldEvents.TextChangeEvent;
import com.vaadin.v7.event.FieldEvents.TextChangeListener;
import com.vaadin.v7.ui.Table;
import com.vaadin.v7.ui.Table.CellStyleGenerator;
import com.vaadin.v7.ui.VerticalLayout;

/**
 * Component that realizes a content finder.
 */
@SuppressWarnings("deprecation")
public class CmsDateSearchComposite implements I_ResourcePropertyProvider, I_CmsResultFacetsManager {

    /**
     * The filter component of the content finder.
     */
    @SuppressWarnings("serial")
    private class FilterComponent extends VerticalLayout {

        /** The site selector. */
        CmsSiteSelector m_siteSelector;

        /** The folder selector. */
        CmsFolderSelector m_folderSelector;

        /** The type selector. */
        CmsTypeSelector m_typeSelector;

        /** The date from selector. */
        CmsDateField m_dateFrom;

        /** The date to selector. */
        CmsDateField m_dateTo;

        /** The availability selector. */
        CmsAvailabilitySelector m_availabilitySelector;

        /** The result facets component. */
        CmsResultFacets m_resultFacets;

        /** The form layout. */
        FormLayout m_formLayout;

        /** The text search. */
        TextField m_textSearchField;

        /**
         * Creates a new filter component.
         */
        FilterComponent() {

            setMargin(true);
            setSpacing(true);
            m_formLayout = new FormLayout();
            m_formLayout.setMargin(true);
            m_formLayout.setSpacing(true);
            m_formLayout.addStyleName("o-formlayout-narrow");
            initSiteSelector();
            initFolderSelector();
            initTypeSelector();
            initDateFrom();
            initDateTo();
            initTextSearchField();
            initExpiredSelector();
            initResultFacets();
            m_formLayout.addComponent(new VerticalLayout()); // fix layout bug
            addComponent(m_formLayout);
        }

        /**
         * Returns the selected category.
         * @return the selected category
         */
        String getSelectedCategory() {

            List<String> categories = m_resultFacets.getSelectedFieldFacets().get(CmsSearchField.FIELD_CATEGORY_EXACT);
            if ((categories != null) && !categories.isEmpty()) {
                return categories.get(0);
            }
            return null;
        }

        /**
         * Updates the type selector.
         */
        void updateTypeSelector() {

            m_typeSelector.updateTypes(getAvailableTypes());
        }

        /**
         * Initializes the date from selector.
         */
        private void initDateFrom() {

            m_dateFrom = new CmsDateField();
            m_dateFrom.setWidthFull();
            String caption = CmsVaadinUtils.getMessageText(Messages.GUI_DATE_SEARCH_DATE_FROM_0);
            m_dateFrom.setCaption(caption);
            m_dateFrom.setResolution(DateTimeResolution.DAY);
            m_dateFrom.addValueChangeListener(new com.vaadin.data.HasValue.ValueChangeListener<LocalDateTime>() {

                public void valueChange(com.vaadin.data.HasValue.ValueChangeEvent<LocalDateTime> event) {

                    search(true, true);
                }
            });
            m_formLayout.addComponent(m_dateFrom);
        }

        /**
         * Initializes the date to selector.
         */
        private void initDateTo() {

            m_dateTo = new CmsDateField();
            m_dateTo.setWidthFull();
            String caption = CmsVaadinUtils.getMessageText(Messages.GUI_DATE_SEARCH_DATE_TO_0);
            m_dateTo.setCaption(caption);
            m_dateTo.setResolution(DateTimeResolution.DAY);
            m_dateTo.addValueChangeListener(new com.vaadin.data.HasValue.ValueChangeListener<LocalDateTime>() {

                public void valueChange(com.vaadin.data.HasValue.ValueChangeEvent<LocalDateTime> event) {

                    search(true, true);
                }
            });
            m_formLayout.addComponent(m_dateTo);
        }

        /**
         * Initializes the expired resources selector.
         */
        private void initExpiredSelector() {

            m_availabilitySelector = new CmsAvailabilitySelector();
            m_availabilitySelector.addValueChangeListener(event -> {
                search(true, true);
            });
            m_formLayout.addComponent(m_availabilitySelector);
        }

        /**
         * Initializes the folder selector.
         */
        private void initFolderSelector() {

            m_folderSelector = new CmsFolderSelector();
            m_folderSelector.addValueChangeListener(new ValueChangeListener() {

                public void valueChange(ValueChangeEvent event) {

                    search(true, true);
                }
            });
            m_formLayout.addComponent(m_folderSelector);
        }

        /**
         * Initializes the result facets component.
         */
        private void initResultFacets() {

            m_resultFacets = new CmsResultFacets(CmsDateSearchComposite.this);
            m_resultFacets.setWidthFull();
            m_resultFacets.setMargin(false);
            m_resultFacets.setSpacing(true);
            m_formLayout.addComponent(m_resultFacets);
        }

        /**
         * Initializes the site selector.
         */
        private void initSiteSelector() {

            m_siteSelector = new CmsSiteSelector();
            m_siteSelector.setWidthFull();
            m_siteSelector.addValueChangeListener(event -> {
                search(true, true);
            });
            m_formLayout.addComponent(m_siteSelector);
        }

        /**
         * Initializes the text search field.
         */
        private void initTextSearchField() {

            m_textSearchField = new TextField();
            m_textSearchField.setCaption(CmsVaadinUtils.getMessageText(Messages.GUI_LISTMANAGER_SEARCH_0));
            m_textSearchField.setWidthFull();
            m_textSearchField.addValueChangeListener(event -> {
                search(true, true);
            });
            m_formLayout.addComponent(m_textSearchField);
        }

        /**
         * Initializes the type selector.
         */
        private void initTypeSelector() {

            m_typeSelector = new CmsTypeSelector();
            m_typeSelector.addValueChangeListener(event -> {
                search(true, true);
            });
            m_formLayout.addComponent(m_typeSelector);
        }
    }

    /**
     * The result component of the content finder.
     */
    @SuppressWarnings("serial")
    private class ResultComponent extends VerticalLayout {

        /** Layout showing empty type message. */
        VerticalLayout m_infoEmptyType;

        /** Layout showing empty result message. */
        VerticalLayout m_infoEmptyResult;

        /** The file table. */
        CmsResultTable m_resultTable;

        /**
         * Creates a new result component.
         */
        ResultComponent() {

            setSizeFull();
            initInfoEmptyType();
            initInfoEmptyResult();
            initResultTable();
        }

        /**
         * Shows the empty result info layout.
         */
        void showInfoEmptyResult() {

            m_resultTable.setVisible(false);
            m_infoEmptyType.setVisible(false);
            m_infoEmptyResult.setVisible(true);
        }

        /**
         * Shows the empty type info layout.
         */
        void showInfoEmptyType() {

            m_infoEmptyResult.setVisible(false);
            m_resultTable.setVisible(false);
            m_infoEmptyType.setVisible(true);
        }

        /**
         * Updates the result table for given resources.
         * @param resources the resources
         */
        void updateResultTable(List<CmsResource> resources) {

            m_infoEmptyType.setVisible(false);
            m_infoEmptyResult.setVisible(false);
            m_resultTable.fillTable(getCmsObject(), resources, true, false, true);
            m_resultTable.setVisible(true);
        }

        /**
         * Initializes the empty result info layout.
         */
        private void initInfoEmptyResult() {

            m_infoEmptyResult = CmsVaadinUtils.getInfoLayout(Messages.GUI_DATE_SEARCH_EMPTY_RESULT_0);
            m_infoEmptyResult.setVisible(false);
            addComponent(m_infoEmptyResult);
        }

        /**
         * Initializes the empty type info layout.
         */
        private void initInfoEmptyType() {

            m_infoEmptyType = CmsVaadinUtils.getInfoLayout(Messages.GUI_DATE_SEARCH_EMPTY_TYPE_0);
            m_infoEmptyType.setVisible(false);
            addComponent(m_infoEmptyType);
        }

        /**
         * Initializes the result table.
         */
        private void initResultTable() {

            LinkedHashMap<CmsResourceTableProperty, Integer> tableColumns = new LinkedHashMap<CmsResourceTableProperty, Integer>();
            for (Map.Entry<CmsResourceTableProperty, Integer> columnsEntry : CmsFileTable.DEFAULT_TABLE_PROPERTIES.entrySet()) {
                if (columnsEntry.getKey().equals(CmsResourceTableProperty.PROPERTY_RESOURCE_TYPE)) {
                    tableColumns.put(CmsListManager.INSTANCEDATE_PROPERTY, Integer.valueOf(0));
                }
                tableColumns.put(columnsEntry.getKey(), columnsEntry.getValue());
            }
            m_resultTable = new CmsResultTable(null, tableColumns) {

                /**
                 * Path, title, and type columns shall be visible and not collapsed.
                 * @see org.opencms.ui.components.CmsFileTable#applyWorkplaceAppSettings()
                 */
                @Override
                public void applyWorkplaceAppSettings() {

                    super.applyWorkplaceAppSettings();
                    m_fileTable.setColumnCollapsed(CmsResourceTableProperty.PROPERTY_SIZE, true);
                    m_fileTable.setColumnCollapsed(CmsResourceTableProperty.PROPERTY_DATE_MODIFIED, true);
                    m_fileTable.setColumnCollapsed(CmsListManager.INSTANCEDATE_PROPERTY, false);
                }

                /**
                 * Filter by path, title.
                 * @see org.opencms.ui.components.CmsFileTable#filterTable(java.lang.String)
                 */
                @Override
                public void filterTable(String search) {

                    m_container.removeAllContainerFilters();
                    if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(search)) {
                        m_container.addContainerFilter(
                            new Or(
                                new SimpleStringFilter(
                                    CmsResourceTableProperty.PROPERTY_SITE_PATH,
                                    search,
                                    true,
                                    false),
                                new SimpleStringFilter(CmsResourceTableProperty.PROPERTY_TITLE, search, true, false)));
                    }
                    if ((m_fileTable.getValue() != null) & !((Set<?>)m_fileTable.getValue()).isEmpty()) {
                        m_fileTable.setCurrentPageFirstItemId(((Set<?>)m_fileTable.getValue()).iterator().next());
                    }
                }
            };
            m_resultTable.applyWorkplaceAppSettings();
            m_resultTable.addPropertyProvider(CmsDateSearchComposite.this);
            m_resultTable.setContextProvider(new I_CmsContextProvider() {

                /**
                 * @see org.opencms.ui.apps.I_CmsContextProvider#getDialogContext()
                 */
                public I_CmsDialogContext getDialogContext() {

                    CmsFileTableDialogContext context = new CmsFileTableDialogContext(
                        CmsDateSearchConfiguration.APP_ID,
                        ContextType.fileTable,
                        m_resultTable,
                        m_resultTable.getSelectedResources()) {

                        /**
                         * @see org.opencms.ui.components.CmsFileTableDialogContext#finish(java.util.Collection)
                         */
                        @Override
                        public void finish(Collection<CmsUUID> ids) {

                            if (m_resultTable.getSelectedItems() == null) {
                                super.finish(ids);
                            } else {
                                String itemId = m_resultTable.getCurrentPageFirstItemId();
                                search(false, false);
                                m_resultTable.setCurrentPageFirstItemId(itemId);
                                closeWindow();
                            }
                        }
                    };
                    return context;
                }
            });
            m_resultTable.addAdditionalStyleGenerator(new CellStyleGenerator() {

                public String getStyle(Table source, Object itemId, Object propertyId) {

                    String style = "";
                    Item item = source.getItem(itemId);
                    if (CmsResourceTableProperty.PROPERTY_TITLE.equals(propertyId)
                        && ((item.getItemProperty(CmsResourceTableProperty.PROPERTY_RELEASED_NOT_EXPIRED) == null)
                            || ((Boolean)item.getItemProperty(
                                CmsResourceTableProperty.PROPERTY_RELEASED_NOT_EXPIRED).getValue()).booleanValue())) {
                        style += OpenCmsTheme.IN_NAVIGATION + " ";
                    }
                    return style;
                }

            });
            m_resultTable.setSizeFull();
            m_resultTable.setMenuBuilder(new CmsResourceContextMenuBuilder());
            m_resultTable.setVisible(false);
            addComponent(m_resultTable);
        }
    }

    /**
     * The search component of the content finder.
     */
    @SuppressWarnings("serial")
    private class ResultFilterComponent extends CmsResultFilterComponent {

        /**
         * Creates a new search component.
         */
        ResultFilterComponent() {

            super();
            addTextChangeListener(new TextChangeListener() {

                public void textChange(TextChangeEvent event) {

                    m_resultComponent.m_resultTable.filterTable(event.getText());

                }
            });
        }
    }

    /** The log object for this class. */
    static final Log LOG = CmsLog.getLog(CmsDateSearchComposite.class);

    /** The maximum number of results. */
    static final int MAX_RESULTS = 5000;

    /** The filter component for this content finder. */
    FilterComponent m_filterComponent;

    /** The result component for this content finder. */
    ResultComponent m_resultComponent;

    /** The search component for this content finder. */
    CmsResultFilterComponent m_resultFilterComponent;

    /**
     * Creates a new content finder component.
     */
    public CmsDateSearchComposite() {

        m_filterComponent = new FilterComponent();
        m_filterComponent.updateTypeSelector();
        m_resultComponent = new ResultComponent();
        m_resultFilterComponent = new ResultFilterComponent();
        connectComponents();
        m_resultComponent.showInfoEmptyType();
    }

    /**
     * @see org.opencms.ui.components.CmsResourceTable.I_ResourcePropertyProvider#addItemProperties(com.vaadin.v7.data.Item, org.opencms.file.CmsObject, org.opencms.file.CmsResource, java.util.Locale)
     */
    @SuppressWarnings("javadoc")
    public void addItemProperties(Item resourceItem, CmsObject cms, CmsResource resource, Locale locale) {

        if (resourceItem.getItemProperty(CmsResourceTableProperty.PROPERTY_TITLE) != null) {
            CmsResourceUtil resUtil = new CmsResourceUtil(cms, resource);
            String title = resUtil.getGalleryTitle(locale);
            if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(title)) {
                resourceItem.getItemProperty(CmsResourceTableProperty.PROPERTY_TITLE).setValue(title);
            }
        }
        if ((resource instanceof CmsSearchResource)
            && (resourceItem.getItemProperty(CmsListManager.INSTANCEDATE_PROPERTY) != null)) {
            List<String> contentLocales = ((CmsSearchResource)resource).getDocument().getMultivaluedFieldAsStringList(
                CmsSearchField.FIELD_CONTENT_LOCALES);
            if (contentLocales != null) {
                for (String contentLocale : contentLocales) {
                    String dateFieldKey = CmsSearchField.FIELD_INSTANCEDATE + "_" + contentLocale + "_dt";
                    Date date = ((CmsSearchResource)resource).getDateField(dateFieldKey);
                    if (date != null) {
                        resourceItem.getItemProperty(CmsListManager.INSTANCEDATE_PROPERTY).setValue(date);
                    }
                }
            }
        }
    }

    /**
     * Returns the filter component of this content finder.
     * @return the filter component of this content finder
     */
    public Component getFilterComponent() {

        return m_filterComponent;
    }

    /**
     * Returns the result component of this content finder.
     * @return the result component of this content finder
     */
    public Component getResultComponent() {

        return m_resultComponent;
    }

    /**
     * Returns the result filter component of this content finder.
     * @return the result filter component of this content finder
     */
    public Component getResultFilterComponent() {

        return m_resultFilterComponent;
    }

    /**
     * Executes the search.
     * @param updateState whether to update the app state
     * @param resetSelectedFacets whether to reset the selected facets
     */
    public void search(boolean updateState, boolean resetSelectedFacets) {

        if (resetSelectedFacets) {
            m_filterComponent.m_resultFacets.resetFacets();
            m_filterComponent.m_resultFacets.setVisible(false);
        }
        if (updateState) {
            CmsComponentState componentState = new CmsComponentState();
            componentState.setSite((String)m_filterComponent.m_siteSelector.getValue());
            componentState.setFolder(m_filterComponent.m_folderSelector.getValue());
            componentState.setResourceType((I_CmsResourceType)m_filterComponent.m_typeSelector.getValue());
            componentState.setDateFrom(m_filterComponent.m_dateFrom.getDate());
            componentState.setDateTo(m_filterComponent.m_dateTo.getDate());
            componentState.setAvailability(m_filterComponent.m_availabilitySelector.getValue().getFirst());
            componentState.setQuery(m_filterComponent.m_textSearchField.getValue());
            componentState.setCategory(m_filterComponent.getSelectedCategory());
            CmsAppWorkplaceUi.get().changeCurrentAppState(componentState.generateStateString());
        }
        if (m_filterComponent.m_typeSelector.getValue() == null) {
            m_resultComponent.showInfoEmptyType();
        } else {
            CmsSolrResultList solrResultList = search(null);
            List<CmsResource> results = new ArrayList<CmsResource>(solrResultList);
            if (results.isEmpty()) {
                m_resultComponent.showInfoEmptyResult();
            } else {
                m_resultComponent.updateResultTable(results);
                Map<String, Boolean> checkedCategoryFacets = new HashMap<String, Boolean>();
                if (!m_filterComponent.m_resultFacets.getSelectedFieldFacets().isEmpty()) {
                    checkedCategoryFacets.put(
                        m_filterComponent.m_resultFacets.getSelectedFieldFacets().get(
                            CmsSearchField.FIELD_CATEGORY_EXACT).get(0),
                        Boolean.TRUE);
                }
                m_filterComponent.m_resultFacets.displayFacetResult(
                    solrResultList,
                    checkedCategoryFacets,
                    null,
                    null,
                    getCmsObject());
                m_filterComponent.m_resultFacets.setVisible(true);
            }
        }
    }

    /**
     * @see org.opencms.ui.components.I_CmsResultFacetsManager#search(java.util.Map, java.util.Map)
     */
    public void search(Map<String, List<String>> fieldFacets, Map<String, List<String>> rangeFacets) {

        search(true, false);
    }

    /**
     * Sets a component state.
     * @param componentState the component state
     */
    public void setState(CmsComponentState componentState) {

        m_filterComponent.m_siteSelector.setValue(componentState.getSite());
        m_filterComponent.m_folderSelector.setValue(componentState.getFolder());
        m_filterComponent.m_typeSelector.setValue(componentState.getResourceType());
        m_filterComponent.m_dateFrom.setDate(componentState.getDateFrom());
        m_filterComponent.m_dateTo.setDate(componentState.getDateTo());
        m_filterComponent.m_availabilitySelector.setValue(
            m_filterComponent.m_availabilitySelector.getOption(componentState.getAvailability()));
        if (componentState.getCategory() != null) {
            m_filterComponent.m_resultFacets.selectFieldFacet(
                CmsSearchField.FIELD_CATEGORY_EXACT,
                componentState.getCategory());
        }
        m_filterComponent.m_textSearchField.setValue(componentState.getQuery());
    }

    /**
     * Returns the list of XML content types relevant for the content finder.
     * @return the list of XML content types relevant for the content finder
     */
    List<I_CmsResourceType> getAvailableTypes() {

        CmsSolrResultList solrResultList = search(Integer.valueOf(0));
        List<I_CmsResourceType> result = new ArrayList<>();
        for (Count count : solrResultList.getFacetField(CmsSearchField.FIELD_TYPE).getValues()) {
            String typeName = count.getName();
            CmsExplorerTypeSettings typeSetting = OpenCms.getWorkplaceManager().getExplorerTypeSetting(typeName);
            if (typeSetting == null) {
                continue;
            }
            try {
                I_CmsResourceType resourceType = OpenCms.getResourceManager().getResourceType(typeName);
                result.add(resourceType);
            } catch (CmsLoaderException e) {
                LOG.error(e.getLocalizedMessage(), e);
            }
        }
        return result;
    }

    /**
     * Returns the current CMS context of this content finder.
     * @return the current CMS context of this content finder
     */
    CmsObject getCmsObject() {

        if ((m_filterComponent == null) || (m_filterComponent.m_folderSelector == null)) {
            return A_CmsUI.getCmsObject();
        }
        CmsObject folderSelectorCms = m_filterComponent.m_folderSelector.getCmsObject();
        return folderSelectorCms == null ? A_CmsUI.getCmsObject() : folderSelectorCms;
    }

    /**
     * Builds the Solr query.
     * @return the Solr query
     */
    private CmsSolrQuery buildQuery() {

        CmsSolrQuery query = new CmsSolrQuery(getCmsObject(), null);
        query.setSort(CmsSearchField.FIELD_PATH, ORDER.asc);
        query.addFacetField(CmsSearchField.FIELD_TYPE);
        query.addFacetField(CmsSearchField.FIELD_CONTENT_LOCALES);
        query.addFacetField("{!ex=ce}" + CmsSearchField.FIELD_CATEGORY_EXACT);
        query.setFacetMinCount(1);
        List<String> filterQueries = new ArrayList<String>();
        filterQueries.add(CmsSearchField.FIELD_CONTENT_LOCALES + ":*");
        String site = (String)m_filterComponent.m_siteSelector.getValue();
        String folder = m_filterComponent.m_folderSelector.getValue();
        if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(folder)) {
            String path = CmsStringUtil.joinPaths(site, folder);
            filterQueries.add(CmsSearchField.FIELD_PARENT_FOLDERS + ":\"" + path + "\"");
        } else {
            if (site != null) {
                site = site.endsWith("/") ? site : site + "/";
            }
            filterQueries.add(CmsSearchField.FIELD_PARENT_FOLDERS + ":\"" + site + "\"");
        }
        I_CmsResourceType type = (I_CmsResourceType)m_filterComponent.m_typeSelector.getValue();
        if (type != null) {
            filterQueries.add(CmsSearchField.FIELD_TYPE + ":" + type.getTypeName());
        }
        Date dateFrom = m_filterComponent.m_dateFrom.getDate();
        Date dateTo = m_filterComponent.m_dateTo.getDate();
        String dateRangeQuery = null;
        if ((dateFrom != null) || (dateTo != null)) {
            List<String> dateRangeQueries = new ArrayList<String>();
            List<Locale> availableLocales = OpenCms.getLocaleManager().getAvailableLocales(getCmsObject(), folder);
            for (Locale locale : availableLocales) {
                CmsDateRangeRestriction restriction = new CmsDateRangeRestriction(dateFrom, dateTo);
                dateRangeQueries.add(
                    CmsSearchField.FIELD_INSTANCEDATE_CURRENT_TILL
                        + "_"
                        + locale
                        + CmsSearchField.FIELD_POSTFIX_DATE
                        + ":"
                        + restriction.getRange());
            }
            dateRangeQuery = "(" + String.join(" OR ", dateRangeQueries) + ")";
            filterQueries.add(dateRangeQuery);
        }
        if (!filterQueries.isEmpty()) {
            if (m_filterComponent.m_availabilitySelector.isOptionWithout()) {
                query.setFilterQueries(String.join(" AND ", filterQueries));
            } else if (m_filterComponent.m_availabilitySelector.isOptionOnly()) {
                Date now = new Date();
                CmsDateRangeRestriction releasedRestriction = new CmsDateRangeRestriction(now, null);
                CmsDateRangeRestriction expiredRestriction = new CmsDateRangeRestriction(null, now);
                String releasedQuery = "released:" + releasedRestriction.getRange();
                String expiredQuery = "expired:" + expiredRestriction.getRange();
                String releasedOrExpiredQuery = releasedQuery + " OR " + expiredQuery;
                String expiredOrReleasedQuery = expiredQuery + " OR " + releasedQuery; // fix to avoid rewrite of query
                query.setFilterQueries(
                    String.join(" AND ", filterQueries),
                    releasedOrExpiredQuery,
                    expiredOrReleasedQuery);
            } else {
                String releasedQuery = "released:[* TO *]";
                String expiredQuery = "expired:[* TO *]";
                query.setFilterQueries(String.join(" AND ", filterQueries), releasedQuery, expiredQuery);
            }
        }
        List<String> checkedCategories = m_filterComponent.m_resultFacets.getSelectedFieldFacets().get(
            "category_exact");
        if ((checkedCategories != null) && !checkedCategories.isEmpty()) {
            query.addFilterQuery(
                "{!tag=ce}" + CmsSearchField.FIELD_CATEGORY_EXACT + ":\"" + checkedCategories.get(0) + "\"");
        }
        String textSearch = m_resultFilterComponent == null ? null : m_filterComponent.m_textSearchField.getValue();
        if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(textSearch)) {
            query.setQuery(ClientUtils.escapeQueryChars(textSearch));
        }
        return query;
    }

    /**
     * Connects all filter, search, and result components.
     */
    @SuppressWarnings("serial")
    private void connectComponents() {

        // update the folder selector and locale selector when the site selector changes
        m_filterComponent.m_siteSelector.addValueChangeListener(new ValueChangeListener() {

            public void valueChange(ValueChangeEvent arg0) {

                try {
                    CmsObject newCms = OpenCms.initCmsObject(A_CmsUI.getCmsObject());
                    newCms.getRequestContext().setSiteRoot((String)m_filterComponent.m_siteSelector.getValue());
                    m_filterComponent.m_folderSelector.setCmsObject(newCms);
                    m_filterComponent.m_folderSelector.setValue("/");
                    m_filterComponent.updateTypeSelector();
                    m_filterComponent.m_dateFrom.setValue(null);
                    m_filterComponent.m_dateTo.setValue(null);
                    m_filterComponent.m_availabilitySelector.reset();
                } catch (CmsException e) {
                    LOG.error(e.getLocalizedMessage(), e);
                }
            }
        });
        // update the locale selector when the folder selector changes
        m_filterComponent.m_folderSelector.addValueChangeListener(new ValueChangeListener() {

            public void valueChange(ValueChangeEvent event) {

                m_filterComponent.updateTypeSelector();
                m_filterComponent.m_dateFrom.setValue(null);
                m_filterComponent.m_dateTo.setValue(null);
                m_filterComponent.m_availabilitySelector.reset();
            }
        });
    }

    /**
     * Executes the search and returns the results.
     * @param rows the number of rows to return
     * @return the results
     */
    private CmsSolrResultList search(Integer rows) {

        CmsSolrQuery query = buildQuery();
        boolean online = getCmsObject().getRequestContext().getCurrentProject().isOnlineProject();
        String indexName = online ? CmsSolrIndex.DEFAULT_INDEX_NAME_ONLINE : CmsSolrIndex.DEFAULT_INDEX_NAME_OFFLINE;
        CmsSolrIndex index = OpenCms.getSearchManager().getIndexSolr(indexName);
        query.setRows(rows == null ? Integer.valueOf(MAX_RESULTS) : rows);
        try {
            return index.search(getCmsObject(), query, true, null, true, CmsResourceFilter.ALL, MAX_RESULTS);
        } catch (CmsSearchException e) {
            CmsErrorDialog.showErrorDialog(e);
            LOG.error(e.getLocalizedMessage(), e);
            return null;
        }
    }
}
