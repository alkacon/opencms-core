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

package org.opencms.ui.apps.search;

import org.opencms.file.CmsObject;
import org.opencms.file.CmsProject;
import org.opencms.file.types.I_CmsResourceType;
import org.opencms.loader.CmsLoaderException;
import org.opencms.main.CmsException;
import org.opencms.main.OpenCms;
import org.opencms.search.CmsSearchIndex;
import org.opencms.ui.A_CmsUI;
import org.opencms.ui.CmsVaadinUtils;
import org.opencms.ui.CmsVaadinUtils.PropertyId;
import org.opencms.ui.apps.Messages;
import org.opencms.ui.components.fileselect.CmsPathSelectField;
import org.opencms.util.CmsStringUtil;
import org.opencms.util.CmsUUID;

import java.util.Collections;
import java.util.Locale;

import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.Property.ValueChangeListener;
import com.vaadin.data.util.IndexedContainer;
import com.vaadin.shared.ui.combobox.FilteringMode;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.CheckBox;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;

/**
 * The source search form.<p>
 */
public class CmsSourceSearchForm extends VerticalLayout {

    /** The available search types. */
    public static enum SearchType {
        /** XML content values only. */
        contentValues(false, true),
        /** Full text search. */
        fullText(false, false),
        /** Filter using a solr index, before searching for matches. */
        solr(true, false),
        /** Filter using a solr index, before searching for matches, XML content values only. */
        solrContentValues(true, true);

        /** The content values only flag. */
        private boolean m_contentValuesOnly;

        /** The is solr search flag. */
        private boolean m_solrSearch;

        /**
         * Constructor.<p>
         *
         * @param solrSearch the is solr search flag
         * @param contentValuesOnly the content values only flag
         */
        private SearchType(boolean solrSearch, boolean contentValuesOnly) {
            m_solrSearch = solrSearch;
            m_contentValuesOnly = contentValuesOnly;
        }

        /**
         * Returns whether this is a content values only search type.<p>
         *
         * @return <code>true</code> if this is a content values only search type
         */
        public boolean isContentValuesOnly() {

            return m_contentValuesOnly;
        }

        /**
         * Returns whether this is a SOLR search type.<p>
         *
         * @return <code>true</code> if this is a SOLR search type
         */
        public boolean isSolrSearch() {

            return m_solrSearch;
        }
    }

    /** The serial version id. */
    private static final long serialVersionUID = 1023130318064811880L;

    /** The source search app instance. */
    private CmsSourceSearchApp m_app;

    /** The search locale select. */
    private ComboBox m_locale;

    /** The replace check box. */
    private CheckBox m_replace;

    /** The replace pattern field. */
    private TextField m_replacePattern;

    /** The resource type select. */
    private ComboBox m_resourceType;

    /** The search button. */
    private Button m_search;

    /** The search index select. */
    private ComboBox m_searchIndex;
    /** The search pattern field. */
    private TextField m_searchPattern;
    /** The search root path select. */
    private CmsPathSelectField m_searchRoot;
    /** The search type select. */
    private ComboBox m_searchType;
    /** The SOLR query field. */
    private TextField m_solrQuery;
    /** The replace project. */
    private ComboBox m_workProject;
    /** The XPath field. */
    private TextField m_xPath;

    /**
     * Constructor.<p>
     *
     * @param app the source search app instance
     */
    public CmsSourceSearchForm(CmsSourceSearchApp app) {
        m_app = app;
        CmsVaadinUtils.readAndLocalizeDesign(this, CmsVaadinUtils.getWpMessagesForCurrentLocale(), null);
        initFields();
        m_replace.addValueChangeListener(new ValueChangeListener() {

            private static final long serialVersionUID = 1L;

            public void valueChange(ValueChangeEvent event) {

                updateReplace();
            }
        });
        m_searchType.addValueChangeListener(new ValueChangeListener() {

            private static final long serialVersionUID = 1L;

            public void valueChange(ValueChangeEvent event) {

                changedSearchType();
            }
        });
        m_search.addClickListener(new ClickListener() {

            private static final long serialVersionUID = 1L;

            public void buttonClick(ClickEvent event) {

                search();
            }
        });
        updateReplace();
        changedSearchType();
    }

    /**
     * Initializes the form with the given settings
     *
     * @param settings the settings
     */
    public void initFormValues(CmsSearchReplaceSettings settings) {

        m_searchType.setValue(settings.getType());
        if (!settings.getPaths().isEmpty()) {
            m_searchRoot.setValue(settings.getPaths().get(0));
        }
        if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(settings.getTypes())) {
            try {
                I_CmsResourceType type = OpenCms.getResourceManager().getResourceType(settings.getTypes());
                m_resourceType.setValue(type);
            } catch (CmsLoaderException e) {
                // nothing to do, skip setting the type
            }
        }
        m_searchPattern.setValue(settings.getSearchpattern());

        if (settings.getType().isContentValuesOnly()) {
            if (settings.getLocale() != null) {
                m_locale.setValue(settings.getLocale());
            }
            m_xPath.setValue(settings.getXpath());
        }
        if (settings.getType().isSolrSearch()) {
            m_solrQuery.setValue(settings.getQuery());
            m_searchIndex.setValue(settings.getSource());
        }
    }

    /**
     * Handles search type changes.<p>
     */
    void changedSearchType() {

        SearchType type = (SearchType)m_searchType.getValue();
        m_searchIndex.setVisible(type.isSolrSearch());
        m_solrQuery.setVisible(type.isSolrSearch());
        updateReplace();
        m_xPath.setVisible(type.isContentValuesOnly());
        m_locale.setVisible(type.isContentValuesOnly());

        IndexedContainer types = (IndexedContainer)m_resourceType.getContainerDataSource();
        types.removeAllContainerFilters();
        types.addContainerFilter(
            type.isContentValuesOnly() ? CmsVaadinUtils.FILTER_XML_CONTENTS : CmsVaadinUtils.FILTER_NO_FOLDERS);
    }

    /**
     * Calls the search for the given parameters.<p>
     */
    void search() {

        CmsSearchReplaceSettings settings = new CmsSearchReplaceSettings();
        settings.setType((SearchType)m_searchType.getValue());
        settings.setPaths(Collections.singletonList(m_searchRoot.getValue()));
        I_CmsResourceType type = (I_CmsResourceType)m_resourceType.getValue();
        if (type != null) {
            settings.setTypes(type.getTypeName());
        }

        if (m_replace.getValue().booleanValue()) {
            try {
                CmsProject workProject = A_CmsUI.getCmsObject().readProject((CmsUUID)m_workProject.getValue());
                settings.setProject(workProject.getName());
            } catch (CmsException e) {
                // ignore
            }
            settings.setReplacepattern(m_replacePattern.getValue());
        }
        settings.setSearchpattern(m_searchPattern.getValue());
        if (settings.getType().isContentValuesOnly()) {
            if (m_locale.getValue() != null) {
                settings.setLocale(m_locale.getValue().toString());
            }
            settings.setXpath(m_xPath.getValue());
        }
        if (settings.getType().isSolrSearch()) {
            settings.setQuery(m_solrQuery.getValue());
            settings.setSource((String)m_searchIndex.getValue());
        }

        m_app.search(settings, true);
    }

    /**
     * Toggles the replace option.<p>
     */
    void updateReplace() {

        boolean replace = m_replace.getValue().booleanValue();
        m_replacePattern.setVisible(replace);
        m_workProject.setVisible(replace);
    }

    /**
     * Initializes the form fields.<p>
     */
    private void initFields() {

        CmsObject cms = A_CmsUI.getCmsObject();
        boolean online = cms.getRequestContext().getCurrentProject().isOnlineProject();

        m_searchType.setFilteringMode(FilteringMode.OFF);
        m_searchType.setNullSelectionAllowed(false);
        m_searchType.addItem(SearchType.fullText);
        m_searchType.setItemCaption(
            SearchType.fullText,
            CmsVaadinUtils.getMessageText(Messages.GUI_SOURCESEARCH_SERACH_TYPE_FULLTEXT_0));
        m_searchType.addItem(SearchType.contentValues);
        m_searchType.setItemCaption(
            SearchType.contentValues,
            CmsVaadinUtils.getMessageText(Messages.GUI_SOURCESEARCH_SERACH_TYPE_XMLCONTENT_0));
        if (OpenCms.getSearchManager().getSolrServerConfiguration().isEnabled()) {

            m_searchIndex.setFilteringMode(FilteringMode.OFF);
            m_searchIndex.setNullSelectionAllowed(false);
            String selectIndex = null;
            for (CmsSearchIndex index : OpenCms.getSearchManager().getAllSolrIndexes()) {
                boolean offlineMode = CmsSearchIndex.REBUILD_MODE_OFFLINE.equals(index.getRebuildMode());
                // in case the current project is offline, show offline indexes, otherwise show online indexes
                if ((!online && offlineMode) || (online && !offlineMode)) {
                    m_searchIndex.addItem(index.getName());
                    if (selectIndex == null) {
                        selectIndex = index.getName();
                    }
                }
            }
            if (selectIndex != null) {
                m_searchIndex.setValue(selectIndex);

                // only add the solr search types if there is an index available
                m_searchType.addItem(SearchType.solr);
                m_searchType.setItemCaption(
                    SearchType.solr,
                    CmsVaadinUtils.getMessageText(Messages.GUI_SOURCESEARCH_SERACH_TYPE_SOLR_0));
                m_searchType.addItem(SearchType.solrContentValues);
                m_searchType.setItemCaption(
                    SearchType.solrContentValues,
                    CmsVaadinUtils.getMessageText(Messages.GUI_SOURCESEARCH_SERACH_TYPE_SOLR_CONTENT_VALUES_0));

            }
        }
        m_searchType.setValue(SearchType.fullText);

        m_searchRoot.setValue("/");

        m_locale.setFilteringMode(FilteringMode.OFF);
        for (Locale locale : OpenCms.getLocaleManager().getAvailableLocales()) {
            m_locale.addItem(locale);
        }

        m_resourceType.setNullSelectionAllowed(true);
        IndexedContainer resTypes = CmsVaadinUtils.getResourceTypesContainer();
        resTypes.addContainerFilter(CmsVaadinUtils.FILTER_NO_FOLDERS);

        m_resourceType.setContainerDataSource(resTypes);
        m_resourceType.setItemCaptionPropertyId(PropertyId.caption);
        m_resourceType.setItemIconPropertyId(PropertyId.icon);

        m_workProject.setNullSelectionAllowed(false);
        IndexedContainer projects = CmsVaadinUtils.getProjectsContainer(A_CmsUI.getCmsObject(), "caption");
        projects.removeItem(CmsProject.ONLINE_PROJECT_ID);
        m_workProject.setContainerDataSource(projects);
        m_workProject.setItemCaptionPropertyId("caption");

        if (online) {
            m_replace.setEnabled(false);
        } else {
            m_workProject.setValue(cms.getRequestContext().getCurrentProject().getUuid());
        }
    }
}
