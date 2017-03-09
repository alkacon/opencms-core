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

import org.opencms.file.CmsObject;
import org.opencms.file.CmsResource;
import org.opencms.file.CmsResourceFilter;
import org.opencms.main.CmsLog;
import org.opencms.main.OpenCms;
import org.opencms.search.solr.CmsSolrResultList;
import org.opencms.ui.A_CmsUI;
import org.opencms.ui.CmsVaadinUtils;
import org.opencms.ui.FontOpenCms;
import org.opencms.ui.I_CmsDialogContext;
import org.opencms.ui.I_CmsDialogContext.ContextType;
import org.opencms.ui.I_CmsUpdateListener;
import org.opencms.ui.apps.A_CmsWorkplaceApp;
import org.opencms.ui.apps.CmsAppWorkplaceUi;
import org.opencms.ui.apps.CmsEditor;
import org.opencms.ui.apps.CmsFileExplorer;
import org.opencms.ui.apps.I_CmsAppUIContext;
import org.opencms.ui.apps.I_CmsContextProvider;
import org.opencms.ui.apps.Messages;
import org.opencms.ui.apps.projects.CmsProjectManagerConfiguration;
import org.opencms.ui.components.CmsErrorDialog;
import org.opencms.ui.components.CmsFileTable;
import org.opencms.ui.components.CmsFileTableDialogContext;
import org.opencms.ui.components.CmsToolBar;
import org.opencms.ui.components.I_CmsWindowCloseListener;
import org.opencms.ui.components.OpenCmsTheme;
import org.opencms.ui.components.extensions.CmsGwtDialogExtension;
import org.opencms.util.CmsStringUtil;
import org.opencms.util.CmsUUID;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

import org.apache.commons.logging.Log;

import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.Property.ValueChangeListener;
import com.vaadin.event.FieldEvents.TextChangeEvent;
import com.vaadin.event.FieldEvents.TextChangeListener;
import com.vaadin.navigator.ViewChangeListener;
import com.vaadin.server.Sizeable.Unit;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.Component;
import com.vaadin.ui.HorizontalSplitPanel;
import com.vaadin.ui.TextField;
import com.vaadin.ui.UI;
import com.vaadin.ui.themes.ValoTheme;

/**
 * Manager for list configuration files.<p>
 */
public class CmsListManager extends A_CmsWorkplaceApp
implements I_CmsContextProvider, ViewChangeListener, I_CmsWindowCloseListener {

    /** The serial version id. */
    private static final long serialVersionUID = -25954374225590319L;

    /** The view content list path name. */
    public static final String PATH_NAME_VIEW = "view";

    /** The list configuration resource type name. */
    public static final String RES_TYPE_LIST_CONFIG = "listconfig";

    /** The logger for this class. */
    private static Log LOG = CmsLog.getLog(CmsListManager.class.getName());

    /** The mail layout. */
    private HorizontalSplitPanel m_mainLayout;

    /** The toggle overview button. */
    private Button m_overviewButton;

    /** The publish button. */
    private Button m_publishButton;

    /** The result table. */
    private CmsFileTable m_resultTable;

    /** The table filter input. */
    private TextField m_resultTableFilter;

    /** The search form. */
    private CmsListConfigurationForm m_searchForm;

    /** The text search input. */
    private TextField m_textSearch;

    /**
     * @see com.vaadin.navigator.ViewChangeListener#afterViewChange(com.vaadin.navigator.ViewChangeListener.ViewChangeEvent)
     */
    public void afterViewChange(ViewChangeEvent event) {

        // nothing to do
    }

    /**
     * @see com.vaadin.navigator.ViewChangeListener#beforeViewChange(com.vaadin.navigator.ViewChangeListener.ViewChangeEvent)
     */
    public boolean beforeViewChange(ViewChangeEvent event) {

        m_searchForm.tryUnlockCurrent();
        return true;
    }

    /**
     * @see org.opencms.ui.apps.I_CmsContextProvider#getDialogContext()
     */
    public I_CmsDialogContext getDialogContext() {

        CmsFileTableDialogContext context = new CmsFileTableDialogContext(
            CmsProjectManagerConfiguration.APP_ID,
            ContextType.fileTable,
            m_resultTable,
            m_resultTable.getSelectedResources());
        context.setEditableProperties(CmsFileExplorer.INLINE_EDIT_PROPERTIES);
        return context;
    }

    /**
     * @see org.opencms.ui.apps.A_CmsWorkplaceApp#initUI(org.opencms.ui.apps.I_CmsAppUIContext)
     */
    @Override
    public void initUI(I_CmsAppUIContext context) {

        super.initUI(context);

        m_overviewButton = CmsToolBar.createButton(
            FontOpenCms.CLIPBOARD,
            CmsVaadinUtils.getMessageText(Messages.GUI_LISTMANAGER_OVERVIEW_0));
        m_overviewButton.addClickListener(new ClickListener() {

            /** Serial version id. */
            private static final long serialVersionUID = 1L;

            public void buttonClick(ClickEvent event) {

                toggleOverview();
            }
        });
        context.addToolbarButton(m_overviewButton);

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

            public void buttonClick(ClickEvent event) {

                publish();
            }
        });

        context.addToolbarButton(m_publishButton);

        m_rootLayout.setMainHeightFull(true);
        m_mainLayout = new HorizontalSplitPanel();
        m_mainLayout.setSizeFull();
        m_searchForm = new CmsListConfigurationForm(this);
        m_mainLayout.setFirstComponent(m_searchForm);
        m_infoLayout.addComponent(m_searchForm.getResultSorter());
        m_resultTable = new CmsFileTable(null);
        m_resultTable.applyWorkplaceAppSettings();
        m_resultTable.setContextProvider(this);
        m_resultTable.setSizeFull();
        m_resultTableFilter = new TextField();
        m_resultTableFilter.setIcon(FontOpenCms.FILTER);
        m_resultTableFilter.setInputPrompt(
            Messages.get().getBundle(UI.getCurrent().getLocale()).key(Messages.GUI_EXPLORER_FILTER_0));
        m_resultTableFilter.addStyleName(ValoTheme.TEXTFIELD_INLINE_ICON);
        m_resultTableFilter.setWidth("200px");
        m_resultTableFilter.addTextChangeListener(new TextChangeListener() {

            private static final long serialVersionUID = 1L;

            public void textChange(TextChangeEvent event) {

                filterTable(event.getText());

            }
        });
        m_infoLayout.addComponent(m_resultTableFilter);

        m_textSearch = new TextField();
        m_textSearch.setIcon(FontOpenCms.SEARCH);
        m_textSearch.setInputPrompt("Search");
        m_textSearch.addStyleName(ValoTheme.TEXTFIELD_INLINE_ICON);
        m_textSearch.setWidth("200px");
        m_textSearch.addValueChangeListener(new ValueChangeListener() {

            /** Serial version id. */
            private static final long serialVersionUID = 1L;

            public void valueChange(ValueChangeEvent event) {

                search((String)event.getProperty().getValue());
            }
        });

        m_infoLayout.addComponent(m_textSearch);

        m_mainLayout.setSecondComponent(m_resultTable);
        m_mainLayout.setSplitPosition(CmsFileExplorer.LAYOUT_SPLIT_POSITION, Unit.PIXELS);
    }

    /**
     * @see org.opencms.ui.components.I_CmsWindowCloseListener#onWindowClose()
     */
    public void onWindowClose() {

        m_searchForm.tryUnlockCurrent();
    }

    /**
     * @see org.opencms.ui.apps.A_CmsWorkplaceApp#getBreadCrumbForState(java.lang.String)
     */
    @Override
    protected LinkedHashMap<String, String> getBreadCrumbForState(String state) {

        LinkedHashMap<String, String> crumbs = new LinkedHashMap<String, String>();
        if (CmsStringUtil.isEmptyOrWhitespaceOnly(state)) {
            crumbs.put("", CmsVaadinUtils.getMessageText(Messages.GUI_LISTMANAGER_TITLE_0));
        } else if (state.equals(PATH_NAME_VIEW)) {
            crumbs.put(
                CmsListManagerConfiguration.APP_ID,
                CmsVaadinUtils.getMessageText(Messages.GUI_LISTMANAGER_TITLE_0));
            crumbs.put("", "View");
        }
        return crumbs;
    }

    /**
     * @see org.opencms.ui.apps.A_CmsWorkplaceApp#getComponentForState(java.lang.String)
     */
    @Override
    protected Component getComponentForState(String state) {

        CmsObject cms = A_CmsUI.getCmsObject();
        if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(
            A_CmsWorkplaceApp.getParamFromState(state, CmsEditor.RESOURCE_ID_PREFIX))) {
            try {
                CmsUUID id = new CmsUUID(A_CmsWorkplaceApp.getParamFromState(state, CmsEditor.RESOURCE_ID_PREFIX));
                CmsResource res = cms.readResource(id, CmsResourceFilter.ONLY_VISIBLE_NO_DELETED);
                m_searchForm.initFormValues(res);
                enableOverviewMode(false);
            } catch (Exception e) {
                CmsErrorDialog.showErrorDialog(e);
                LOG.error(e.getLocalizedMessage(), e);
            }
        } else {
            displayListConfigs();
            m_searchForm.resetFormValues();
            enableOverviewMode(true);
        }
        return m_mainLayout;
    }

    /**
     * @see org.opencms.ui.apps.A_CmsWorkplaceApp#getSubNavEntries(java.lang.String)
     */
    @Override
    protected List<NavEntry> getSubNavEntries(String state) {

        return null;
    }

    /**
     * Displays the search result in the result table.<p>
     *
     * @param resultList the search result
     * @param clearFilter <code>true</code> to clear the result filter
     */
    void displayResult(CmsSolrResultList resultList, boolean clearFilter) {

        List<CmsResource> resources = new ArrayList<CmsResource>(resultList);
        if (clearFilter) {
            m_resultTableFilter.clear();
        }
        m_resultTable.fillTable(A_CmsUI.getCmsObject(), resources, clearFilter, false);
        enableOverviewMode(false);
    }

    /**
     * Enables the overview mode.<p>
     *
     * @param enabled <code>true</code> to enable the mode
     */
    void enableOverviewMode(boolean enabled) {

        m_publishButton.setVisible(!enabled);
        m_resultTableFilter.setVisible(enabled);
        m_textSearch.setVisible(!enabled);
        if (enabled) {
            m_overviewButton.removeStyleName(OpenCmsTheme.BUTTON_PRESSED);
        } else {
            m_overviewButton.addStyleName(OpenCmsTheme.BUTTON_PRESSED);
        }
    }

    /**
     * Filters the result table.<p>
     *
     * @param filter the filter string
     */
    void filterTable(String filter) {

        m_resultTable.filterTable(filter);
    }

    /**
     * Opens the publish dialog to publish all resources related to the current search configuration.<p>
     */
    void publish() {

        I_CmsUpdateListener<String> updateListener = new I_CmsUpdateListener<String>() {

            public void onUpdate(List<String> updatedItems) {

                updateItems(updatedItems);
            }
        };
        CmsAppWorkplaceUi.get().disableGlobalShortcuts();
        CmsGwtDialogExtension extension = new CmsGwtDialogExtension(A_CmsUI.get(), updateListener);
        extension.openPublishDialog(m_searchForm.getPublishResources());
    }

    /**
     * Searches within the current list.<p>
     *
     * @param query the query string
     */
    void search(String query) {

        m_searchForm.search(true, true, query);
    }

    /**
     * Toggles the overview mode.<p>
     */
    void toggleOverview() {

        boolean isOverview = !m_overviewButton.getStyleName().contains(OpenCmsTheme.BUTTON_PRESSED);
        if (!isOverview) {
            m_searchForm.resetFormValues();
            enableOverviewMode(!isOverview);
            displayListConfigs();
        }
    }

    /**
     * Updates the given items in the result table.<p>
     *
     * @param updatedItems the items to update
     */
    void updateItems(List<String> updatedItems) {

        for (String id : updatedItems) {
            m_resultTable.update(new CmsUUID(id), false);
        }
    }

    /**
     * Displays the list config resources.<p>
     */
    private void displayListConfigs() {

        CmsObject cms = A_CmsUI.getCmsObject();
        try {
            // display the search configuration overview
            List<CmsResource> resources = cms.readResources(
                "/",
                CmsResourceFilter.ONLY_VISIBLE_NO_DELETED.addRequireType(
                    OpenCms.getResourceManager().getResourceType(RES_TYPE_LIST_CONFIG)));
            m_resultTable.fillTable(cms, resources);
        } catch (Exception e) {
            CmsErrorDialog.showErrorDialog(e);
            LOG.error(e.getLocalizedMessage(), e);
        }
    }
}
