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
import org.opencms.jsp.search.config.CmsSearchConfigurationPagination;
import org.opencms.jsp.search.config.I_CmsSearchConfigurationCommon;
import org.opencms.jsp.search.config.I_CmsSearchConfigurationPagination;
import org.opencms.jsp.search.config.parser.CmsSimpleSearchConfigurationParser;
import org.opencms.jsp.search.config.parser.CmsSimpleSearchConfigurationParser.SortOption;
import org.opencms.jsp.search.config.parser.simplesearch.CmsConfigParserUtils;
import org.opencms.jsp.search.config.parser.simplesearch.CmsConfigurationBean;
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
import org.opencms.ui.apps.CmsAppView;
import org.opencms.ui.apps.CmsAppView.CacheStatus;
import org.opencms.ui.apps.CmsAppWorkplaceUi;
import org.opencms.ui.apps.CmsEditor;
import org.opencms.ui.apps.CmsEditorConfiguration;
import org.opencms.ui.apps.CmsFileExplorer;
import org.opencms.ui.apps.CmsFileExplorerSettings;
import org.opencms.ui.apps.I_CmsAppUIContext;
import org.opencms.ui.apps.I_CmsCachableApp;
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
import org.opencms.ui.components.CmsResultFacets;
import org.opencms.ui.components.CmsToolBar;
import org.opencms.ui.components.I_CmsResultFacetsManager;
import org.opencms.ui.components.I_CmsWindowCloseListener;
import org.opencms.ui.components.OpenCmsTheme;
import org.opencms.ui.components.extensions.CmsGwtDialogExtension;
import org.opencms.ui.contextmenu.CmsMenuItemVisibilityMode;
import org.opencms.ui.contextmenu.CmsResourceContextMenuBuilder;
import org.opencms.ui.contextmenu.I_CmsContextMenuItem;
import org.opencms.ui.contextmenu.I_CmsContextMenuItemProvider;
import org.opencms.util.CmsStringUtil;
import org.opencms.util.CmsUUID;
import org.opencms.workplace.editors.directedit.CmsDateSeriesEditHandler;
import org.opencms.workplace.editors.directedit.I_CmsEditHandler;
import org.opencms.workplace.explorer.CmsResourceUtil;
import org.opencms.xml.containerpage.CmsContainerElementBean;
import org.opencms.xml.content.CmsXmlContent;
import org.opencms.xml.content.CmsXmlContentFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;

import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener;
import com.vaadin.server.Sizeable.Unit;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.Component;
import com.vaadin.ui.HorizontalSplitPanel;
import com.vaadin.ui.UI;
import com.vaadin.ui.Window;
import com.vaadin.ui.themes.ValoTheme;
import com.vaadin.v7.data.Item;
import com.vaadin.v7.data.Property.ValueChangeEvent;
import com.vaadin.v7.data.Property.ValueChangeListener;
import com.vaadin.v7.event.FieldEvents.TextChangeEvent;
import com.vaadin.v7.event.FieldEvents.TextChangeListener;
import com.vaadin.v7.ui.AbstractSelect.ItemDescriptionGenerator;
import com.vaadin.v7.ui.ComboBox;
import com.vaadin.v7.ui.Table;
import com.vaadin.v7.ui.Table.CellStyleGenerator;
import com.vaadin.v7.ui.TextField;

/**
 * Manager for list configuration files.<p>
 */
@SuppressWarnings("deprecation")
public class CmsListManager extends A_CmsWorkplaceApp
implements I_ResourcePropertyProvider, I_CmsContextProvider, ViewChangeListener, I_CmsWindowCloseListener,
I_CmsCachableApp, I_CmsResultFacetsManager {

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
                closeWindow();
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
     * Overrides the standard edit action to enable use of edit handlers.<p>
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

    /** List configuration node name and field key. */
    public static final String PARAM_LOCALE = "locale";

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

    /** The date series info table column property id. */
    protected static final CmsResourceTableProperty INFO_PROPERTY = new CmsResourceTableProperty(
        "INFO_PROPERTY",
        String.class,
        null,
        null,
        true,
        1,
        0);

    /** The date series info label table column property id. */
    protected static final CmsResourceTableProperty INFO_PROPERTY_LABEL = new CmsResourceTableProperty(
        "INFO_PROPERTY_LABEL",
        String.class,
        null,
        Messages.GUI_LISTMANAGER_COLUMN_INFO_0,
        true,
        0,
        110);

    /** The blacklisted table column property id. */
    public static final CmsResourceTableProperty INSTANCEDATE_PROPERTY = new CmsResourceTableProperty(
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
            SortOption.DATE_ASC.toString(),
            SortOption.DATE_DESC.toString(),
            SortOption.TITLE_ASC.toString(),
            SortOption.TITLE_DESC.toString(),
            SortOption.ORDER_ASC.toString(),
            SortOption.ORDER_DESC.toString()},
        {
            Messages.GUI_LISTMANAGER_SORT_DATE_ASC_0,
            Messages.GUI_LISTMANAGER_SORT_DATE_DESC_0,
            Messages.GUI_LISTMANAGER_SORT_TITLE_ASC_0,
            Messages.GUI_LISTMANAGER_SORT_TITLE_DESC_0,
            Messages.GUI_LISTMANAGER_SORT_ORDER_ASC_0,
            Messages.GUI_LISTMANAGER_SORT_ORDER_DESC_0}};

    /** The month name abbreviations. */
    public static final String[] MONTHS = new String[] {
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
    static final Log LOG = CmsLog.getLog(CmsListManager.class.getName());

    /** Default backend pagination. */
    private static final I_CmsSearchConfigurationPagination PAGINATION = new CmsSearchConfigurationPagination(
        null,
        Integer.valueOf(10000),
        Integer.valueOf(1));

    /** The serial version id. */
    private static final long serialVersionUID = -25954374225590319L;

    /** The current list configuration data. */
    CmsConfigurationBean m_currentConfig;

    /** The current list configuration resource. */
    CmsResource m_currentResource;

    /** The result table. */
    CmsResultTable m_resultTable;

    /** The create new button. */
    private Button m_createNewButton;

    /** The current search parser. */
    private CmsSimpleSearchConfigurationParser m_currentConfigParser;

    /** The current app state. */
    private String m_currentState;

    /** The current edit dialog window. */
    private Window m_dialogWindow;

    /** The edit current configuration button. */
    private Button m_editCurrentButton;

    /** Indicates multiple instances of a series are present in the current search result. */
    private boolean m_hasSeriesInstances;

    /** Indicates series types are present in the current search result. */
    private boolean m_hasSeriesType;

    /** Flag indicating individual instances of a date series should be hidden. */
    private boolean m_hideSeriesInstances;

    /** The info button. */
    private Button m_infoButton;

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

    /** The resetting flag. */
    private boolean m_resetting;

    /** The facet result display. */
    private CmsResultFacets m_resultFacets;

    /** The mail layout. */
    private HorizontalSplitPanel m_resultLayout;

    /** The sort select. */
    private ComboBox m_resultSorter;

    /** The table filter input. */
    private TextField m_tableFilter;

    /** The text search input. */
    private TextField m_textSearch;

    /** The toggle date series display. */
    private Button m_toggleSeriesButton;

    /**
     * @see org.opencms.ui.components.CmsResourceTable.I_ResourcePropertyProvider#addItemProperties(com.vaadin.v7.data.Item, org.opencms.file.CmsObject, org.opencms.file.CmsResource, java.util.Locale)
     */
    @SuppressWarnings("javadoc")
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
        List<CmsResource> resources = table.getSelectedResources();
        DialogContext context = new DialogContext(
            CmsProjectManagerConfiguration.APP_ID,
            ContextType.fileTable,
            table,
            resources);
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

        m_toggleSeriesButton = CmsToolBar.createButton(
            FontOpenCms.LIST,
            CmsVaadinUtils.getMessageText(Messages.GUI_LISTMANAGER_TOGGLE_SERIES_BUTTON_0));
        m_toggleSeriesButton.addClickListener(new ClickListener() {

            /** Serial version id. */
            private static final long serialVersionUID = 1L;

            @Override
            public void buttonClick(ClickEvent event) {

                toggleDateSeries();
            }
        });
        uiContext.addToolbarButton(m_toggleSeriesButton);

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

        m_rootLayout.setMainHeightFull(true);
        m_resultLayout = new HorizontalSplitPanel();
        m_resultLayout.setSizeFull();
        m_resultFacets = new CmsResultFacets(this);
        m_resultFacets.setWidth("100%");
        m_resultLayout.setFirstComponent(m_resultFacets);
        LinkedHashMap<CmsResourceTableProperty, Integer> tableColumns = new LinkedHashMap<CmsResourceTableProperty, Integer>();
        // insert columns a specific positions
        for (Map.Entry<CmsResourceTableProperty, Integer> columnsEntry : CmsFileTable.DEFAULT_TABLE_PROPERTIES.entrySet()) {
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

                return Arrays.<I_CmsContextMenuItem> asList(new CmsContextMenuActionItem(new A_CmsWorkplaceAction() {

                    @Override
                    public void executeAction(I_CmsDialogContext context) {

                        CmsUUID structureId = context.getResources().get(0).getStructureId();
                        m_currentConfig.getBlacklist().add(structureId);
                        saveBlacklist(m_currentConfig);
                        context.finish(Collections.singletonList(structureId));
                    }

                    @Override
                    public String getId() {

                        return "hideresource";
                    }

                    @Override
                    public String getTitleKey() {

                        return Messages.GUI_LISTMANAGER_BLACKLIST_MENU_ENTRY_0;
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
                        saveBlacklist(m_currentConfig);
                        context.finish(Collections.singletonList(structureId));
                    }

                    @Override
                    public String getId() {

                        return "showresource";
                    }

                    @Override
                    public String getTitleKey() {

                        return Messages.GUI_LISTMANAGER_REMOVE_FROM_BLACKLIST_MENU_ENTRY_0;
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
        final ItemDescriptionGenerator defaultDescriptionGenerator = new CmsResourceTable.DefaultItemDescriptionGenerator();
        m_resultTable.setsetItemDescriptionGenerator(new ItemDescriptionGenerator() {

            private static final long serialVersionUID = 1L;

            public String generateDescription(Component source, Object itemId, Object propertyId) {

                Item item = ((Table)source).getItem(itemId);
                if (INFO_PROPERTY_LABEL.equals(propertyId)
                    && ((Boolean)item.getItemProperty(BLACKLISTED_PROPERTY).getValue()).booleanValue()) {
                    return CmsVaadinUtils.getMessageText(Messages.GUI_LISTMANAGER_COLUMN_BLACKLISTED_0);
                }
                return defaultDescriptionGenerator.generateDescription(source, itemId, propertyId);
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
     * @see org.opencms.ui.apps.I_CmsCachableApp#isCachable()
     */
    public boolean isCachable() {

        return true;
    }

    /**
     * @see org.opencms.ui.apps.I_CmsCachableApp#onRestoreFromCache()
     */
    public void onRestoreFromCache() {

        if (isOverView()) {
            CmsFileExplorerSettings state = m_overviewTable.getTableSettings();
            displayListConfigs();
            m_overviewTable.setTableState(state);
            m_overviewTable.updateSorting();
        } else {
            refreshResult();
        }
    }

    /**
     * @see org.opencms.ui.apps.A_CmsWorkplaceApp#onStateChange(java.lang.String)
     */
    @Override
    public void onStateChange(String state) {

        if ((m_currentState == null) || !m_currentState.equals(state)) {
            m_currentState = state;
            super.onStateChange(state);
        }
    }

    /**
     * @see org.opencms.ui.components.I_CmsWindowCloseListener#onWindowClose()
     */
    @Override
    public void onWindowClose() {

        unlockCurrent();
    }

    /**
     * Saves the blacklist from the bean in the current list configuration.<p>
     *
     * @param configBean the bean whose blacklist should be saved
     */
    public void saveBlacklist(CmsConfigurationBean configBean) {

        if (m_dialogWindow != null) {
            m_dialogWindow.close();
            m_dialogWindow = null;
        }
        CmsObject cms = A_CmsUI.getCmsObject();

        try {
            m_lockAction = CmsLockUtil.ensureLock(cms, m_currentResource);
        } catch (CmsException e) {
            CmsErrorDialog.showErrorDialog(e);
            return;
        }

        try {
            CmsFile configFile = cms.readFile(m_currentResource);
            CmsXmlContent content = CmsXmlContentFactory.unmarshal(cms, configFile);
            content = CmsConfigParserUtils.updateBlackList(cms, content, configBean);
            configFile.setContents(content.marshal());
            cms.writeFile(configFile);
            if (m_lockAction.getChange().equals(LockChange.locked)) {
                CmsLockUtil.tryUnlock(cms, configFile);
            }
        } catch (CmsException e) {
            e.printStackTrace();
        }
        m_currentConfig = configBean;
    }

    /**
     * Execute a search with the given search configuration parser.<p>
     *
     * @param configParser the search configuration parser
     * @param resource the current configuration resource
     */
    public void search(CmsSimpleSearchConfigurationParser configParser, CmsResource resource) {

        m_currentResource = resource;
        m_currentConfigParser = configParser;
        resetContentLocale(configParser.getSearchLocale());
        m_resetting = true;
        m_resultSorter.setValue(m_currentConfig.getParameterValue(CmsConfigurationBean.PARAM_SORT_ORDER));
        m_resetting = false;

        search(null, null, null);
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
        CmsSearchController controller = new CmsSearchController(
            new CmsSearchConfiguration(m_currentConfigParser, A_CmsUI.getCmsObject()));
        controller.getPagination().getState().setCurrentPage(1);
        if (fieldFacets != null) {
            Map<String, I_CmsSearchControllerFacetField> fieldFacetControllers = controller.getFieldFacets().getFieldFacetController();
            for (Map.Entry<String, List<String>> facetEntry : fieldFacets.entrySet()) {
                I_CmsSearchStateFacet state = fieldFacetControllers.get(facetEntry.getKey()).getState();
                state.clearChecked();
                for (String check : facetEntry.getValue()) {
                    state.addChecked(check);
                }
            }
        }
        if (rangeFacets != null) {
            Map<String, I_CmsSearchControllerFacetRange> rangeFacetControllers = controller.getRangeFacets().getRangeFacetController();
            for (Map.Entry<String, List<String>> facetEntry : rangeFacets.entrySet()) {
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

        controller.addQueryParts(query, A_CmsUI.getCmsObject());
        executeSearch(controller, query);
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
                m_currentConfig = CmsConfigParserUtils.parseListConfiguration(A_CmsUI.getCmsObject(), res);
                String localeString = A_CmsWorkplaceApp.getParamFromState(state, PARAM_LOCALE);
                Locale locale;
                if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(localeString)) {
                    locale = CmsLocaleManager.getLocale(localeString);
                } else {
                    locale = getContentLocale(m_currentConfig);
                }
                //SearchConfigParser configParser = new SearchConfigParser(m_currentConfig, m_collapseItemSeries, locale);
                CmsSimpleSearchConfigurationParser configParser = new CmsSimpleSearchConfigurationParser(
                    cms,
                    m_currentConfig,
                    null);
                setBackendSpecificOptions(configParser, locale);
                search(configParser, res);
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
            m_currentConfigParser.setSearchLocale(contentLocale);
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

        List<CmsResource> resources;
        evalSeries(resultList);
        if (m_hideSeriesInstances) {
            Set<CmsUUID> instanceIds = new HashSet<CmsUUID>();
            resources = new ArrayList<CmsResource>();
            for (CmsSearchResource res : resultList) {
                if (!instanceIds.contains(res.getStructureId())) {
                    instanceIds.add(res.getStructureId());
                    resources.add(res);
                }
            }
        } else {
            resources = new ArrayList<CmsResource>(resultList);
        }
        m_resultTable.fillTable(A_CmsUI.getCmsObject(), resources, true, false);
        String state = A_CmsWorkplaceApp.addParamToState(
            "",
            CmsEditor.RESOURCE_ID_PREFIX,
            m_currentResource.getStructureId().toString());
        state = A_CmsWorkplaceApp.addParamToState(
            state,
            PARAM_LOCALE,
            m_currentConfigParser.getSearchLocale().toString());
        m_currentState = state;
        CmsAppWorkplaceUi.get().changeCurrentAppState(state);
        if (m_isOverView) {
            enableOverviewMode(false);
            updateBreadCrumb(getBreadCrumbForState(state));
        }
    }

    /**
     * Edits the given list configuration resource.<p>
     *
     * @param resource the configuration resource
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
            View view = CmsAppWorkplaceUi.get().getCurrentView();
            if (view instanceof CmsAppView) {
                ((CmsAppView)view).setCacheStatus(CacheStatus.cacheOnce);
            }
            CmsAppWorkplaceUi.get().showApp(
                OpenCms.getWorkplaceAppManager().getAppConfiguration(CmsEditorConfiguration.APP_ID),
                editState);

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

        m_toggleSeriesButton.setVisible(m_hasSeriesType && !enabled);
        m_resultSorter.setVisible(!enabled);
        m_localeSelect.setVisible(!enabled);
        m_isOverView = enabled;
        m_rootLayout.setMainContent(enabled ? m_overviewTable : m_resultLayout);
        m_createNewButton.setVisible(enabled);
        if (enabled) {
            if (!isOffline) {
                m_createNewButton.setEnabled(false);
                m_createNewButton.setDescription(
                    CmsVaadinUtils.getMessageText(Messages.GUI_LISTMANAGER_NOT_CREATABLE_ONLINE_0));
            } else {
                CmsObject cms = A_CmsUI.getCmsObject();
                CmsADEConfigData data = OpenCms.getADEManager().lookupConfiguration(
                    cms,
                    cms.getRequestContext().getSiteRoot());
                CmsResourceTypeConfig typeConfig = data.getResourceType(RES_TYPE_LIST_CONFIG);
                try {
                    if ((typeConfig == null)
                        || !typeConfig.checkCreatable(cms, cms.getRequestContext().getSiteRoot())) {
                        m_createNewButton.setEnabled(false);
                        m_createNewButton.setDescription(
                            CmsVaadinUtils.getMessageText(Messages.GUI_LISTMANAGER_NOT_CREATABLE_TYPE_0));
                    } else {
                        m_createNewButton.setEnabled(true);
                        m_createNewButton.setDescription(
                            CmsVaadinUtils.getMessageText(Messages.GUI_LISTMANAGER_CREATE_NEW_0));
                    }
                } catch (CmsException e) {
                    m_createNewButton.setEnabled(false);
                    m_createNewButton.setDescription(
                        CmsVaadinUtils.getMessageText(Messages.GUI_LISTMANAGER_NOT_CREATABLE_TYPE_0));
                }
            }
        }
    }

    /**
     * Evaluates if date series types are present and if more than one instance of a series is in the search result.<p>
     *
     * @param resultList the search result list
     */
    void evalSeries(CmsSolrResultList resultList) {

        m_hasSeriesType = false;
        m_hasSeriesInstances = false;
        Set<CmsUUID> instanceIds = new HashSet<CmsUUID>();
        for (CmsSearchResource res : resultList) {
            String seriesType = res.getField(CmsSearchField.FIELD_SERIESDATES_TYPE);
            m_hasSeriesType = m_hasSeriesType || CmsStringUtil.isNotEmptyOrWhitespaceOnly(seriesType);
            if (m_hasSeriesType && I_CmsSerialDateValue.DateType.SERIES.name().equals(seriesType)) {
                if (instanceIds.contains(res.getStructureId())) {
                    m_hasSeriesInstances = true;
                    break;
                } else {
                    instanceIds.add(res.getStructureId());
                }
            }
        }
        if (!m_hasSeriesInstances) {
            setsDateSeriesHiddenFlag(false);
        }
        m_toggleSeriesButton.setEnabled(m_hasSeriesInstances);
        m_toggleSeriesButton.setVisible(m_hasSeriesType);

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
            CmsSearchController controller = new CmsSearchController(
                new CmsSearchConfiguration(m_currentConfigParser, A_CmsUI.getCmsObject()));
            controller.getPagination().getState().setCurrentPage(1);
            controller.addQueryParts(query, A_CmsUI.getCmsObject());
            I_CmsSearchConfigurationCommon commonConfig = controller.getCommon().getConfig();
            CmsSolrIndex index = OpenCms.getSearchManager().getIndexSolr(commonConfig.getSolrIndex());
            try {
                CmsSolrResultList solrResultList = index.search(
                    cms,
                    query,
                    true,
                    null,
                    false,
                    CmsResourceFilter.IGNORE_EXPIRATION,
                    commonConfig.getMaxReturnedResults());
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
        CmsFileExplorerSettings state = m_resultTable.getTableSettings();
        search(
            m_resultFacets.getSelectedFieldFacets(),
            m_resultFacets.getSelectedRangeFactes(),
            m_textSearch.getValue());
        m_resultTable.setTableState(state);
        m_resultTable.updateSorting();
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
     * Sets the date series hidden flag.<p>
     *
     * @param hide the date series hidden flag
     */
    void setsDateSeriesHiddenFlag(boolean hide) {

        m_hideSeriesInstances = hide;
        if (m_hideSeriesInstances) {
            m_toggleSeriesButton.addStyleName(OpenCmsTheme.BUTTON_PRESSED);
        } else {
            m_toggleSeriesButton.removeStyleName(OpenCmsTheme.BUTTON_PRESSED);
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
     * Toggles the date series filter.<p>
     */
    void toggleDateSeries() {

        setsDateSeriesHiddenFlag(!m_hideSeriesInstances);
        refreshResult();
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
        I_CmsSearchConfigurationCommon commonConfig = controller.getCommon().getConfig();
        CmsSolrIndex index = OpenCms.getSearchManager().getIndexSolr(commonConfig.getSolrIndex());
        try {
            CmsSolrResultList solrResultList = index.search(
                cms,
                query,
                true,
                null,
                false,
                CmsResourceFilter.IGNORE_EXPIRATION,
                commonConfig.getMaxReturnedResults());
            displayResult(solrResultList);
            CmsSearchResultWrapper searchResultWrapper = new CmsSearchResultWrapper(
                controller,
                solrResultList,
                query,
                cms,
                null);
            I_CmsSearchControllerFacetField categoriesFacetController = searchResultWrapper.getController().getFieldFacets().getFieldFacetController().get(
                CmsSimpleSearchConfigurationParser.FIELD_CATEGORIES);
            I_CmsSearchControllerFacetRange dateFacetController = searchResultWrapper.getController().getRangeFacets().getRangeFacetController().get(
                CmsSimpleSearchConfigurationParser.FIELD_DATE_FACET_NAME);
            I_CmsSearchControllerFacetField folderFacetController = searchResultWrapper.getController().getFieldFacets().getFieldFacetController().get(
                CmsSimpleSearchConfigurationParser.FIELD_PARENT_FOLDERS);
            m_resultFacets.displayFacetResult(
                solrResultList,
                categoriesFacetController.getState().getIsChecked(),
                dateFacetController.getState().getIsChecked(),
                folderFacetController.getState().getIsChecked(),
                A_CmsUI.getCmsObject());
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
    private Locale getContentLocale(CmsConfigurationBean bean) {

        CmsObject cms = A_CmsUI.getCmsObject();
        if (bean.getFolders().isEmpty()) {
            return OpenCms.getLocaleManager().getDefaultLocale(cms, "/");
        } else {
            return OpenCms.getLocaleManager().getDefaultLocale(
                cms,
                cms.getRequestContext().removeSiteRoot(m_currentConfig.getFolders().get(0)));
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

    /**
     * Sets options which are specific to the backend list manager on the cnofiguration parser.<p>
     *
     * @param configParser the configuration parser
     * @param locale the search locale
     */
    private void setBackendSpecificOptions(CmsSimpleSearchConfigurationParser configParser, Locale locale) {

        configParser.setSearchLocale(locale);
        configParser.setIgnoreBlacklist(true);
        configParser.setPagination(PAGINATION);
    }
}
