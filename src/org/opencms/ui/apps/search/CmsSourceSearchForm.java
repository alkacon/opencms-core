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
import org.opencms.main.CmsException;
import org.opencms.main.OpenCms;
import org.opencms.search.CmsSearchIndex;
import org.opencms.ui.A_CmsUI;
import org.opencms.ui.CmsVaadinUtils;
import org.opencms.ui.CmsVaadinUtils.PropertyId;
import org.opencms.ui.apps.Messages;
import org.opencms.ui.components.fileselect.CmsPathSelectField;
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
    enum SearchType {
        /** full text search. */
        fullText,
        /** Search using a solr index. */
        solr,
        /** XML content values only. */
        xmlContent
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
     * Handles search type changes.<p>
     */
    void changedSearchType() {

        SearchType type = (SearchType)m_searchType.getValue();
        boolean solrSearch = SearchType.solr == type;
        boolean xmlSearch = SearchType.xmlContent == type;

        m_searchIndex.setVisible(solrSearch);
        m_solrQuery.setVisible(solrSearch);
        m_searchPattern.setVisible(!solrSearch);
        m_replace.setVisible(!solrSearch);
        updateReplace();
        m_xPath.setVisible(xmlSearch);
        m_locale.setVisible(xmlSearch || solrSearch);

        IndexedContainer types = (IndexedContainer)m_resourceType.getContainerDataSource();
        types.removeAllContainerFilters();
        types.addContainerFilter(xmlSearch ? CmsVaadinUtils.FILTER_XML_CONTENTS : CmsVaadinUtils.FILTER_NO_FOLDERS);

    }

    /**
     * Calls the search for the given parameters.<p>
     */
    void search() {

        CmsSearchReplaceSettings settings = new CmsSearchReplaceSettings();
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
        }

        switch ((SearchType)m_searchType.getValue()) {
            default:
            case fullText:
                settings.setOnlyContentValues(false);

                settings.setSearchpattern(m_searchPattern.getValue());
                if (m_replace.getValue().booleanValue()) {
                    settings.setReplacepattern(m_replacePattern.getValue());

                }
                break;
            case xmlContent:
                settings.setOnlyContentValues(true);
                if (m_locale.getValue() != null) {
                    settings.setLocale(m_locale.getValue().toString());
                }
                settings.setXpath(m_xPath.getValue());
                settings.setSearchpattern(m_searchPattern.getValue());
                if (m_replace.getValue().booleanValue()) {
                    settings.setReplacepattern(m_replacePattern.getValue());
                }
                break;
            case solr:
                settings.setQuery(m_solrQuery.getValue());
                if (m_locale.getValue() != null) {
                    settings.setLocale(m_locale.getValue().toString());
                }
                settings.setSource((String)m_searchIndex.getValue());
        }
        m_app.search(settings);
    }

    /**
     * Toggles the replace option.<p>
     */
    void updateReplace() {

        boolean replace = m_replace.getValue().booleanValue() && (SearchType.solr != m_searchType.getValue());
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
        m_resourceType.setNullSelectionAllowed(false);
        m_searchType.addItem(SearchType.fullText);
        m_searchType.setItemCaption(
            SearchType.fullText,
            CmsVaadinUtils.getMessageText(Messages.GUI_SOURCESEARCH_SERACH_TYPE_FULLTEXT_0));
        m_searchType.addItem(SearchType.xmlContent);
        m_searchType.setItemCaption(
            SearchType.xmlContent,
            CmsVaadinUtils.getMessageText(Messages.GUI_SOURCESEARCH_SERACH_TYPE_XMLCONTENT_0));
        if (OpenCms.getSearchManager().getSolrServerConfiguration().isEnabled()) {
            m_searchType.addItem(SearchType.solr);
            m_searchType.setItemCaption(
                SearchType.solr,
                CmsVaadinUtils.getMessageText(Messages.GUI_SOURCESEARCH_SERACH_TYPE_SOLR_0));

            m_searchIndex.setFilteringMode(FilteringMode.OFF);
            m_searchIndex.setNullSelectionAllowed(false);

            for (CmsSearchIndex index : OpenCms.getSearchManager().getAllSolrIndexes()) {
                boolean offlineMode = CmsSearchIndex.REBUILD_MODE_OFFLINE.equals(index.getRebuildMode());
                // in case the current project is offline, show offline indexes, otherwise show online indexes
                if ((!online && offlineMode) || (online && !offlineMode)) {
                    m_searchIndex.addItem(index.getName());
                }
            }
        }
        m_searchType.setValue(SearchType.fullText);

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
