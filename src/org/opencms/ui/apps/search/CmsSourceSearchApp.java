/*
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (c) Alkacon Software GmbH & Co. KG (https://www.alkacon.com)
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
 * company website: https://www.alkacon.com
 *
 * For further information about OpenCms, please see the
 * project website: https://www.opencms.org
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package org.opencms.ui.apps.search;

import org.opencms.file.CmsObject;
import org.opencms.file.CmsResource;
import org.opencms.main.CmsException;
import org.opencms.main.OpenCms;
import org.opencms.ui.A_CmsUI;
import org.opencms.ui.CmsVaadinUtils;
import org.opencms.ui.FontOpenCms;
import org.opencms.ui.I_CmsDialogContext;
import org.opencms.ui.I_CmsDialogContext.ContextType;
import org.opencms.ui.apps.A_CmsWorkplaceApp;
import org.opencms.ui.apps.CmsAppWorkplaceUi;
import org.opencms.ui.apps.CmsFileExplorer;
import org.opencms.ui.apps.I_CmsAppUIContext;
import org.opencms.ui.apps.I_CmsCachableApp;
import org.opencms.ui.apps.I_CmsContextProvider;
import org.opencms.ui.apps.Messages;
import org.opencms.ui.apps.projects.CmsProjectManagerConfiguration;
import org.opencms.ui.apps.search.CmsSourceSearchForm.SearchType;
import org.opencms.ui.components.CmsErrorDialog;
import org.opencms.ui.components.CmsFileTable;
import org.opencms.ui.components.CmsFileTableDialogContext;
import org.opencms.ui.report.CmsReportOverlay;
import org.opencms.util.CmsStringUtil;
import org.opencms.util.CmsUUID;

import java.io.ByteArrayInputStream;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Set;

import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.net.URLCodec;

import com.vaadin.server.Sizeable.Unit;
import com.vaadin.server.StreamResource;
import com.vaadin.ui.Component;
import com.vaadin.ui.HorizontalSplitPanel;
import com.vaadin.ui.UI;
import com.vaadin.ui.themes.ValoTheme;
import com.vaadin.v7.event.FieldEvents.TextChangeEvent;
import com.vaadin.v7.event.FieldEvents.TextChangeListener;
import com.vaadin.v7.ui.TextField;
import com.vaadin.v7.ui.VerticalLayout;

/**
 * The source search app.<p>
 */
public class CmsSourceSearchApp extends A_CmsWorkplaceApp implements I_CmsCachableApp {

    /** The folder key. */
    public static final String FOLDER = "f";

    /** The ignore subsites key. */
    public static final String IGNORE_SUBSITES = "igss";

    /** The index key. */
    public static final String INDEX = "i";

    /** The locale key. */
    public static final String LOCALE = "l";

    /** The project ley. */
    public static final String PROJECT = "p";

    /** The property key. */
    public static final String PROPERTY = "pr";

    /** The query key. */
    public static final String QUERY = "q";

    /** The replace pattern key. */
    public static final String REPLACE_PATTERN = "rp";

    /** The resource type key. */
    public static final String RESOURCE_TYPE = "rt";

    /** The search pattern key. */
    public static final String SEARCH_PATTERN = "sp";

    /** The type key. */
    public static final String SEARCH_TYPE = "t";

    /** The site root key. */
    public static final String SITE_ROOT = "s";

    /** The XPath key. */
    public static final String XPATH = "x";

    /** The serial version id. */
    private static final long serialVersionUID = 4675966043824229258L;

    /** The results file table. */
    CmsFileTable m_resultTable;

    /** The currently selected result list resources. */
    private List<CmsResource> m_currentResources;

    /** The current state string. */
    private String m_currentState;

    /**Layout showing empty result message.*/
    private VerticalLayout m_infoEmptyResult;

    /**Layout showing introduction message.*/
    private VerticalLayout m_infoIntroLayout;

    /** The current search report. */
    private CmsReportOverlay m_report;

    /** The result table filter input. */
    private TextField m_resultTableFilter;

    /** The search form. */
    private CmsSourceSearchForm m_searchForm;

    /** The search and replace thread. */
    private CmsSearchReplaceThread m_thread;

    /**
     * Generates the state string for the given search settings.<p>
     *
     * @param settings the search settings
     *
     * @return the state string
     */
    public static String generateState(CmsSearchReplaceSettings settings) {

        String state = "";
        state = A_CmsWorkplaceApp.addParamToState(state, SITE_ROOT, settings.getSiteRoot());
        state = A_CmsWorkplaceApp.addParamToState(state, SEARCH_TYPE, settings.getType().name());
        state = A_CmsWorkplaceApp.addParamToState(state, SEARCH_PATTERN, settings.getSearchpattern());
        if (!settings.getPaths().isEmpty()) {
            state = A_CmsWorkplaceApp.addParamToState(state, FOLDER, settings.getPaths().get(0));
        }
        state = A_CmsWorkplaceApp.addParamToState(state, RESOURCE_TYPE, settings.getTypes());
        state = A_CmsWorkplaceApp.addParamToState(state, LOCALE, settings.getLocale());
        state = A_CmsWorkplaceApp.addParamToState(state, QUERY, settings.getQuery());
        state = A_CmsWorkplaceApp.addParamToState(state, INDEX, settings.getSource());
        state = A_CmsWorkplaceApp.addParamToState(state, XPATH, settings.getXpath());
        state = A_CmsWorkplaceApp.addParamToState(state, IGNORE_SUBSITES, String.valueOf(settings.ignoreSubSites()));
        state = A_CmsWorkplaceApp.addParamToState(state, PROPERTY, settings.getProperty().getName());

        return state;
    }

    /**
     * Returns the settings for the given state.<p>
     *
     * @param state the state
     *
     * @return the search settings
     */
    static CmsSearchReplaceSettings getSettingsFromState(String state) {

        try {
            state = new URLCodec().decode(state);
        } catch (DecoderException e1) {
            //
        }
        CmsSearchReplaceSettings settings = null;
        String typeString = A_CmsWorkplaceApp.getParamFromState(state, SEARCH_TYPE);
        if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(typeString)) {
            SearchType type = SearchType.valueOf(typeString);
            settings = new CmsSearchReplaceSettings();
            settings.setType(type);
            settings.setIgnoreSubSites(
                Boolean.parseBoolean(A_CmsWorkplaceApp.getParamFromState(state, IGNORE_SUBSITES)));
            settings.setSiteRoot(A_CmsWorkplaceApp.getParamFromState(state, SITE_ROOT).replace("%2F", "/"));
            settings.setPaths(
                Collections.singletonList(A_CmsWorkplaceApp.getParamFromState(state, FOLDER).replace("%2F", "/")));
            String resType = A_CmsWorkplaceApp.getParamFromState(state, RESOURCE_TYPE);
            if (resType != null) {
                settings.setTypes(resType);
            }
            String project = A_CmsWorkplaceApp.getParamFromState(state, PROJECT);
            if (project != null) {
                settings.setProject(project);
            }
            settings.setSearchpattern(A_CmsWorkplaceApp.getParamFromState(state, SEARCH_PATTERN).replace("%2F", "/"));
            if (type.isContentValuesOnly()) {
                settings.setOnlyContentValues(true);
                String locale = A_CmsWorkplaceApp.getParamFromState(state, LOCALE);
                if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(locale)) {
                    settings.setLocale(locale);
                }
                settings.setXpath(A_CmsWorkplaceApp.getParamFromState(state, XPATH).replace("%2F", "/"));
            }
            if (type.isSolrSearch()) {
                settings.setQuery(A_CmsWorkplaceApp.getParamFromState(state, QUERY).replace("%2F", "/"));
                settings.setSource(A_CmsWorkplaceApp.getParamFromState(state, INDEX));
            }
            if (type.isPropertySearch()) {
                try {
                    settings.setProperty(
                        A_CmsUI.getCmsObject().readPropertyDefinition(
                            A_CmsWorkplaceApp.getParamFromState(state, PROPERTY)));
                } catch (CmsException e) {
                    //
                }
            }
        }
        return settings;
    }

    /**
     * @see org.opencms.ui.apps.A_CmsWorkplaceApp#initUI(org.opencms.ui.apps.I_CmsAppUIContext)
     */
    @Override
    public void initUI(I_CmsAppUIContext context) {

        context.addPublishButton(changed -> {/* do nothing */});
        super.initUI(context);
    }

    /**
     * @see org.opencms.ui.apps.I_CmsCachableApp#isCachable()
     */
    public boolean isCachable() {

        return true;
    }

    /**
     * @see org.opencms.ui.apps.I_CmsCachableApp#onRestoreFromCache()
     */
    public void onRestoreFromCache() {

        if (m_resultTable.getItemCount() < CmsFileExplorer.UPDATE_FOLDER_THRESHOLD) {
            m_resultTable.update(m_resultTable.getAllIds(), false);
        } else {
            if (m_currentResources != null) {
                Set<CmsUUID> ids = new HashSet<CmsUUID>();
                for (CmsResource res : m_currentResources) {
                    ids.add(res.getStructureId());
                }
                m_resultTable.update(ids, false);
            }
        }
    }

    /**
     * @see org.opencms.ui.apps.A_CmsWorkplaceApp#onStateChange(java.lang.String)
     */
    @Override
    public void onStateChange(String state) {

        if ((m_currentState == null) || !m_currentState.equals(state)) {
            super.onStateChange(state);
        }
    }

    /**
     * Displays the search result.<p>
     */
    protected void displayResult() {

        if (m_thread.getMatchedResources().isEmpty()) {
            m_resultTable.setVisible(false);
            m_infoIntroLayout.setVisible(false);
            m_infoEmptyResult.setVisible(true);
            m_resultTable.fillTable(A_CmsUI.getCmsObject(), m_thread.getMatchedResources());
            m_searchForm.setDownload(null);
        } else {
            m_resultTable.setVisible(true);
            m_infoIntroLayout.setVisible(false);
            m_infoEmptyResult.setVisible(false);
            m_resultTable.fillTable(A_CmsUI.getCmsObject(), m_thread.getMatchedResources());
            SimpleDateFormat fmt = new SimpleDateFormat("hhmmss");
            String timeStr = fmt.format(new Date());
            String filename = "opencms_sourcesearch_" + timeStr + ".csv";
            StreamResource downloadResource = new StreamResource(
                () -> new ByteArrayInputStream(m_resultTable.generateCsv()),
                filename);
            m_searchForm.setDownload(downloadResource);
        }
        m_searchForm.removeComponent(m_report);
        m_report = null;
    }

    /**
     * @see org.opencms.ui.apps.A_CmsWorkplaceApp#getBreadCrumbForState(java.lang.String)
     */
    @Override
    protected LinkedHashMap<String, String> getBreadCrumbForState(String state) {

        return null;
    }

    /**
     * @see org.opencms.ui.apps.A_CmsWorkplaceApp#getComponentForState(java.lang.String)
     */
    @Override
    protected Component getComponentForState(String state) {

        m_rootLayout.setMainHeightFull(true);
        HorizontalSplitPanel sp = new HorizontalSplitPanel();
        sp.setSizeFull();
        m_searchForm = new CmsSourceSearchForm(this);
        sp.setFirstComponent(m_searchForm);
        VerticalLayout result = new VerticalLayout();
        result.setSizeFull();
        m_infoIntroLayout = CmsVaadinUtils.getInfoLayout(Messages.GUI_SOURCESEARCH_INTRO_0);
        m_infoEmptyResult = CmsVaadinUtils.getInfoLayout(Messages.GUI_SOURCESEARCH_EMPTY_0);
        m_resultTable = new CmsFileTable(null);

        result.addComponent(m_resultTable);
        result.addComponent(m_infoEmptyResult);
        result.addComponent(m_infoIntroLayout);

        m_resultTable.setVisible(false);
        m_infoEmptyResult.setVisible(false);
        m_infoIntroLayout.setVisible(true);

        m_resultTable.applyWorkplaceAppSettings();
        m_resultTable.setContextProvider(new I_CmsContextProvider() {

            /**
             * @see org.opencms.ui.apps.I_CmsContextProvider#getDialogContext()
             */
            public I_CmsDialogContext getDialogContext() {

                CmsFileTableDialogContext context = new CmsFileTableDialogContext(
                    CmsProjectManagerConfiguration.APP_ID,
                    ContextType.fileTable,
                    m_resultTable,
                    m_resultTable.getSelectedResources());
                storeCurrentFileSelection(m_resultTable.getSelectedResources());
                context.setEditableProperties(CmsFileExplorer.INLINE_EDIT_PROPERTIES);
                return context;
            }
        });
        m_resultTable.setSizeFull();
        if (m_resultTableFilter == null) {
            m_resultTableFilter = new TextField();
            m_resultTableFilter.setIcon(FontOpenCms.FILTER);
            m_resultTableFilter.setInputPrompt(
                Messages.get().getBundle(UI.getCurrent().getLocale()).key(Messages.GUI_EXPLORER_FILTER_0));
            m_resultTableFilter.addStyleName(ValoTheme.TEXTFIELD_INLINE_ICON);
            m_resultTableFilter.setWidth("200px");
            m_resultTableFilter.addTextChangeListener(new TextChangeListener() {

                private static final long serialVersionUID = 1L;

                public void textChange(TextChangeEvent event) {

                    m_resultTable.filterTable(event.getText());

                }
            });
            m_infoLayout.addComponent(m_resultTableFilter);
        }

        sp.setSecondComponent(result);
        sp.setSplitPosition(CmsFileExplorer.LAYOUT_SPLIT_POSITION, Unit.PIXELS);

        if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(state)) {
            CmsSearchReplaceSettings settings = getSettingsFromState(state);
            if (settings != null) {
                m_currentState = state;
                m_searchForm.initFormValues(settings);
                search(settings, false);
            }
        }
        return sp;
    }

    /**
     * @see org.opencms.ui.apps.A_CmsWorkplaceApp#getSubNavEntries(java.lang.String)
     */
    @Override
    protected List<NavEntry> getSubNavEntries(String state) {

        return null;
    }

    /**
     * Executes the search.<p>
     *
     * @param settings the search settings
     * @param updateState <code>true</code> to create a new history entry
     */
    protected void search(CmsSearchReplaceSettings settings, boolean updateState) {

        if (updateState) {
            String state = generateState(settings);
            CmsAppWorkplaceUi.get().changeCurrentAppState(state);
            m_currentState = state;
        }

        CmsObject cms;
        try {
            cms = OpenCms.initCmsObject(A_CmsUI.getCmsObject());
            if (settings.getSiteRoot() != null) {
                cms.getRequestContext().setSiteRoot(settings.getSiteRoot());
            }

            m_thread = new CmsSearchReplaceThread(A_CmsUI.get().getHttpSession(), cms, settings);
            if (m_report != null) {
                m_searchForm.removeComponent(m_report);
            }
            m_report = new CmsReportOverlay(m_thread);
            m_report.addReportFinishedHandler(new Runnable() {

                public void run() {

                    displayResult();
                }
            });
            m_searchForm.addComponent(m_report);
            m_report.setTitle(CmsVaadinUtils.getMessageText(Messages.GUI_SOURCESEARCH_REPORT_TITLE_0));
            m_thread.start();
            m_resultTableFilter.clear();
        } catch (CmsException e) {
            CmsErrorDialog.showErrorDialog(e);
        }
    }

    /**
     * Stores the currently selected resources list.<p>
     *
     * @param resources the currently selected resources
     */
    void storeCurrentFileSelection(List<CmsResource> resources) {

        m_currentResources = resources;
    }
}
