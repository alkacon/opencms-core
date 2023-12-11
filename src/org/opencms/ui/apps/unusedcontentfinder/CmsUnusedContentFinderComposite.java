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

package org.opencms.ui.apps.unusedcontentfinder;

import org.opencms.file.CmsFile;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsProject;
import org.opencms.file.CmsProperty;
import org.opencms.file.CmsPropertyDefinition;
import org.opencms.file.CmsResource;
import org.opencms.file.CmsResourceFilter;
import org.opencms.file.types.CmsResourceTypeXmlAdeConfiguration;
import org.opencms.file.types.CmsResourceTypeXmlContainerPage;
import org.opencms.file.types.CmsResourceTypeXmlContent;
import org.opencms.file.types.I_CmsResourceType;
import org.opencms.main.CmsException;
import org.opencms.main.CmsLog;
import org.opencms.main.OpenCms;
import org.opencms.relations.CmsRelation;
import org.opencms.relations.CmsRelationFilter;
import org.opencms.ui.A_CmsUI;
import org.opencms.ui.CmsVaadinUtils;
import org.opencms.ui.I_CmsDialogContext;
import org.opencms.ui.I_CmsDialogContext.ContextType;
import org.opencms.ui.apps.CmsAppWorkplaceUi;
import org.opencms.ui.apps.I_CmsContextProvider;
import org.opencms.ui.apps.Messages;
import org.opencms.ui.components.CmsComponentState;
import org.opencms.ui.components.CmsErrorDialog;
import org.opencms.ui.components.CmsFileTable;
import org.opencms.ui.components.CmsFileTableDialogContext;
import org.opencms.ui.components.CmsFolderSelector;
import org.opencms.ui.components.CmsResourceTableProperty;
import org.opencms.ui.components.CmsResultFilterComponent;
import org.opencms.ui.components.CmsSiteSelector;
import org.opencms.ui.components.CmsTypeSelector;
import org.opencms.ui.components.OpenCmsTheme;
import org.opencms.ui.contextmenu.CmsResourceContextMenuBuilder;
import org.opencms.ui.dialogs.CmsDeleteDialog;
import org.opencms.util.CmsStringUtil;
import org.opencms.workplace.explorer.CmsExplorerTypeSettings;
import org.opencms.xml.CmsXmlContentDefinition;
import org.opencms.xml.content.I_CmsXmlContentHandler;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.apache.commons.logging.Log;

import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Component;
import com.vaadin.ui.FormLayout;
import com.vaadin.v7.data.Property.ValueChangeEvent;
import com.vaadin.v7.data.Property.ValueChangeListener;
import com.vaadin.v7.data.util.filter.Or;
import com.vaadin.v7.data.util.filter.SimpleStringFilter;
import com.vaadin.v7.event.FieldEvents.TextChangeEvent;
import com.vaadin.v7.event.FieldEvents.TextChangeListener;
import com.vaadin.v7.ui.HorizontalLayout;
import com.vaadin.v7.ui.VerticalLayout;

/**
 * Component that realizes a unused content finder.
 */
@SuppressWarnings("deprecation")
public class CmsUnusedContentFinderComposite {

    /**
     * The delete button of the unused content finder.
     */
    private class DeleteButtonComponent extends Button {

        /** Serial version id. */
        private static final long serialVersionUID = 1L;

        /**
         * Creates a new delete button.
         */
        DeleteButtonComponent() {

            String caption = CmsVaadinUtils.getMessageText(Messages.GUI_UNUSED_CONTENT_FINDER_DELETE_ALL_0);
            addStyleName(OpenCmsTheme.BUTTON_RED);
            setCaption(caption);
            setVisible(false);
            addClickListener(new ClickListener() {

                private static final long serialVersionUID = 1L;

                public void buttonClick(ClickEvent event) {

                    m_resultComponent.getFileTable().selectAll();
                    I_CmsDialogContext context = m_resultComponent.getFileTable().getContextProvider().getDialogContext();
                    context.start(caption, new CmsDeleteDialog(context));
                }
            });
        }
    }

    /**
     * The form component of the unused content finder.
     */
    private class FormComponent extends VerticalLayout {

        /** Serial version id. */
        private static final long serialVersionUID = 1L;

        /** The form layout. */
        private FormLayout m_formLayout;

        /** The site selector. */
        private CmsSiteSelector m_siteSelector;

        /** The folder selector. */
        private CmsFolderSelector m_folderSelector;

        /** The type selector. */
        private CmsTypeSelector m_typeSelector;

        /** The search button. */
        private Button m_searchButton;

        /**
         * Creates a new form component.
         */
        FormComponent() {

            setMargin(true);
            setSpacing(true);
            m_formLayout = new FormLayout();
            m_formLayout.setMargin(true);
            m_formLayout.setSpacing(true);
            m_formLayout.addStyleName("o-formlayout-narrow");
            initSiteSelector();
            initFolderSelector();
            initTypeSelector();
            m_formLayout.addComponent(new VerticalLayout()); // fix layout bug
            addComponent(m_formLayout);
            initSearchButton();
            initDeleteButton();
        }

        /**
         * Returns the selected folder value.
         * @return the selected folder value
         */
        String getFolderValue() {

            return m_folderSelector.getValue();
        }

        /**
         * Returns the selected site value.
         * @return the selected site value
         */
        String getSiteValue() {

            return (String)m_siteSelector.getValue();
        }

        /**
         * Returns the selected type value.
         * @return the selected type value
         */
        I_CmsResourceType getTypeValue() {

            return (I_CmsResourceType)m_typeSelector.getValue();
        }

        /**
         * Sets the folder selector value.
         * @param folder the folder to set
         */
        void setFolderValue(String folder) {

            m_folderSelector.setValue(folder);
        }

        /**
         * Sets the site selector value.
         * @param site the site to set
         */
        void setSiteValue(String site) {

            m_siteSelector.setValue(site);
        }

        /**
         * Sets the type selector value.
         * @param type the type to set
         */
        void setTypeValue(I_CmsResourceType type) {

            m_typeSelector.setValue(type);
        }

        /**
         * Updates the search root.
         * @throws CmsException if initializing the CMS object fails
         */
        void updateSearchRoot() throws CmsException {

            CmsObject newCms = OpenCms.initCmsObject(A_CmsUI.getCmsObject());
            newCms.getRequestContext().setSiteRoot((String)m_siteSelector.getValue());
            m_folderSelector.setCmsObject(newCms);
            m_folderSelector.setValue("/");
        }

        /**
         * Initializes the delete button.
         */
        private void initDeleteButton() {

            HorizontalLayout layout = new HorizontalLayout();
            layout.setMargin(true);
            layout.setSpacing(true);
            layout.setWidth("100%");
            layout.setStyleName("o-dialog-button-bar");
            layout.addComponent(m_deleteButtonComponent);
            layout.setComponentAlignment(m_deleteButtonComponent, Alignment.TOP_RIGHT);
            addComponent(layout);
        }

        /**
         * Initializes the folder selector.
         */
        private void initFolderSelector() {

            m_folderSelector = new CmsFolderSelector();
            m_formLayout.addComponent(m_folderSelector);
        }

        /**
         * Initializes the search button.
         */
        private void initSearchButton() {

            HorizontalLayout layout = new HorizontalLayout();
            layout.setMargin(true);
            layout.setSpacing(true);
            layout.setWidth("100%");
            layout.setStyleName("o-dialog-button-bar");
            m_searchButton = new Button(
                CmsVaadinUtils.getMessageText(Messages.GUI_SOURCESEARCH_SEARCH_0),
                new Button.ClickListener() {

                    private static final long serialVersionUID = 1L;

                    public void buttonClick(ClickEvent event) {

                        search(true);

                    }
                });
            m_searchButton.addStyleName(OpenCmsTheme.BUTTON_BLUE);
            layout.addComponent(m_searchButton);
            layout.setComponentAlignment(m_searchButton, Alignment.TOP_RIGHT);
            addComponent(layout);
        }

        /**
         * Initializes the site selector.
         */
        @SuppressWarnings("serial")
        private void initSiteSelector() {

            m_siteSelector = new CmsSiteSelector();
            m_siteSelector.setWidthFull();
            m_siteSelector.addValueChangeListener(new ValueChangeListener() {

                public void valueChange(ValueChangeEvent event) {

                    try {
                        updateSearchRoot();
                    } catch (CmsException e) {
                        LOG.error("Unable to initialize CmsObject", e);
                    }
                }

            });
            m_formLayout.addComponent(m_siteSelector);
        }

        /**
         * Initializes the type selector.
         */
        private void initTypeSelector() {

            m_typeSelector = new CmsTypeSelector();
            m_typeSelector.updateTypes(getAvailableTypes());
            m_formLayout.addComponent(m_typeSelector);
        }
    }

    /**
     * The result component of the unused content finder.
     */
    private class ResultComponent extends VerticalLayout {

        /** Serial version id. */
        private static final long serialVersionUID = 1L;

        /** The file table. */
        CmsFileTable m_fileTable;

        /** Layout showing empty result message. */
        private VerticalLayout m_infoEmptyResult;

        /** Layout showing introduction message. */
        private VerticalLayout m_infoIntroLayout;

        /** Layout showing too many results message. */
        private VerticalLayout m_infoTooManyResults;

        /** Layout showing too invalid folder message. */
        private VerticalLayout m_infoInvalidFolder;

        /**
         * Creates a new result component.
         */
        ResultComponent() {

            setSizeFull();
            initInfoIntroLayout();
            initInfoEmptyResult();
            initInfoTooManyResults();
            initInfoInvalidFolder();
            initFileTable();
        }

        /**
         * Returns the file table.
         * @return the file table
         */
        CmsFileTable getFileTable() {

            return m_fileTable;
        }

        /**
         * Updates the search result.
         */
        void updateResult() {

            if (!isValidFolder()) {
                showInfoInvalidFolder();
                return;
            }
            CmsObject cms = getOfflineCms();
            I_CmsResourceType type = m_formComponent.getTypeValue();
            String folderName = m_formComponent.getFolderValue();
            List<CmsResource> resourcesToShow = new ArrayList<CmsResource>();
            boolean tooManyResults = false;
            if (cms.existsResource(folderName)) {
                try {
                    List<I_CmsResourceType> resourceTypes = new ArrayList<>();
                    if (type == null) {
                        resourceTypes = getAvailableTypes();
                    } else {
                        resourceTypes.add(type);
                    }
                    for (I_CmsResourceType resourceType : resourceTypes) {
                        CmsResourceFilter filter = CmsResourceFilter.ONLY_VISIBLE.addRequireType(resourceType);
                        List<CmsResource> resources = cms.readResources(folderName, filter);
                        for (CmsResource resource : resources) {
                            if (isExcludedByProperties(resource, false)
                                || isUsedByOtherContents(resource, false)
                                || isUsedByOtherContents(resource, true)) {
                                // resource is excluded by properties in the offline project
                                // resource is used by other contents in the offline project
                                // resource is used by other contents in the online project
                            } else {
                                resourcesToShow.add(resource);
                            }
                            if (resourcesToShow.size() > MAX_RESULTS) {
                                tooManyResults = true;
                                break;
                            }
                        }
                    }
                    m_fileTable.fillTable(cms, resourcesToShow);
                } catch (CmsException e) {
                    CmsErrorDialog.showErrorDialog(e);
                }
            }
            m_infoIntroLayout.setVisible(false);
            if (resourcesToShow.isEmpty()) {
                showInfoEmptyResult();
                m_deleteButtonComponent.setVisible(false);
            } else if (tooManyResults) {
                showInfoTooManyResults();
                m_deleteButtonComponent.setVisible(false);
            } else {
                showFileTable();
                m_deleteButtonComponent.setVisible(true);
            }
        }

        /**
         * Initializes the file table.
         */
        private void initFileTable() {

            m_fileTable = new CmsFileTable(null) {

                private static final long serialVersionUID = 1L;

                /**
                 * Path, title, and type columns shall be visible and not collapsed.
                 * @see org.opencms.ui.components.CmsFileTable#applyWorkplaceAppSettings()
                 */
                @Override
                public void applyWorkplaceAppSettings() {

                    super.applyWorkplaceAppSettings();
                    m_fileTable.setColumnCollapsed(CmsResourceTableProperty.PROPERTY_SIZE, true);
                    m_fileTable.setColumnCollapsed(CmsResourceTableProperty.PROPERTY_INTERNAL_RESOURCE_TYPE, false);
                }

                /**
                 * Filter by path, title, and type names.
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
                                new SimpleStringFilter(CmsResourceTableProperty.PROPERTY_TITLE, search, true, false),
                                new SimpleStringFilter(
                                    CmsResourceTableProperty.PROPERTY_RESOURCE_TYPE,
                                    search,
                                    true,
                                    false),
                                new SimpleStringFilter(
                                    CmsResourceTableProperty.PROPERTY_INTERNAL_RESOURCE_TYPE,
                                    search,
                                    true,
                                    false)));
                    }
                    if ((m_fileTable.getValue() != null) & !((Set<?>)m_fileTable.getValue()).isEmpty()) {
                        m_fileTable.setCurrentPageFirstItemId(((Set<?>)m_fileTable.getValue()).iterator().next());
                    }
                }
            };
            m_fileTable.applyWorkplaceAppSettings();
            m_fileTable.setContextProvider(new I_CmsContextProvider() {

                /**
                 * @see org.opencms.ui.apps.I_CmsContextProvider#getDialogContext()
                 */
                public I_CmsDialogContext getDialogContext() {

                    CmsFileTableDialogContext context = new CmsFileTableDialogContext(
                        CmsUnusedContentFinderConfiguration.APP_ID,
                        ContextType.fileTable,
                        m_fileTable,
                        m_fileTable.getSelectedResources());
                    return context;
                }
            });
            m_fileTable.setSizeFull();
            m_fileTable.setMenuBuilder(new CmsResourceContextMenuBuilder());
            m_fileTable.setVisible(false);
            addComponent(m_fileTable);
            setExpandRatio(m_fileTable, 2);
        }

        /**
         * Initializes the empty result info box.
         */
        private void initInfoEmptyResult() {

            m_infoEmptyResult = CmsVaadinUtils.getInfoLayout(Messages.GUI_SOURCESEARCH_EMPTY_0);
            m_infoEmptyResult.setVisible(false);
            addComponent(m_infoEmptyResult);
        }

        /**
         * Initializes the intro info box.
         */
        private void initInfoIntroLayout() {

            m_infoIntroLayout = CmsVaadinUtils.getInfoLayout(Messages.GUI_SOURCESEARCH_INTRO_0);
            m_infoIntroLayout.setVisible(true);
            addComponent(m_infoIntroLayout);
        }

        /**
         * Initializes the invalid folder info box.
         */
        private void initInfoInvalidFolder() {

            m_infoInvalidFolder = CmsVaadinUtils.getInfoLayout(Messages.GUI_UNUSED_CONTENT_FINDER_INVALID_FOLDER_0);
            m_infoInvalidFolder.setVisible(false);
            addComponent(m_infoInvalidFolder);
        }

        /**
         * Initializes the too many results info box.
         */
        private void initInfoTooManyResults() {

            m_infoTooManyResults = CmsVaadinUtils.getInfoLayout(Messages.GUI_UNUSED_CONTENT_FINDER_TOO_MANY_RESULTS_0);
            m_infoTooManyResults.setVisible(false);
            addComponent(m_infoTooManyResults);
        }

        /**
         * Shows the file table.
         */
        private void showFileTable() {

            m_infoIntroLayout.setVisible(false);
            m_infoTooManyResults.setVisible(false);
            m_infoInvalidFolder.setVisible(false);
            m_infoEmptyResult.setVisible(false);
            m_fileTable.setVisible(true);
        }

        /**
         * Shows the empty result info box.
         */
        private void showInfoEmptyResult() {

            m_fileTable.setVisible(false);
            m_infoIntroLayout.setVisible(false);
            m_infoTooManyResults.setVisible(false);
            m_infoInvalidFolder.setVisible(false);
            m_infoEmptyResult.setVisible(true);
        }

        /**
         * Shows the invalid folder info box.
         */
        private void showInfoInvalidFolder() {

            m_fileTable.setVisible(false);
            m_infoIntroLayout.setVisible(false);
            m_infoTooManyResults.setVisible(false);
            m_infoEmptyResult.setVisible(false);
            m_infoInvalidFolder.setVisible(true);
        }

        /**
         * Shows the empty result info box.
         */
        private void showInfoTooManyResults() {

            m_fileTable.setVisible(false);
            m_infoIntroLayout.setVisible(false);
            m_infoEmptyResult.setVisible(false);
            m_infoInvalidFolder.setVisible(false);
            m_infoTooManyResults.setVisible(true);
        }
    }

    /**
     * Component to filter the result table.
     */
    @SuppressWarnings("serial")
    private class ResultFilterComponent extends CmsResultFilterComponent {

        /**
         * Creates a new result filter.
         */
        ResultFilterComponent() {

            super();
            addTextChangeListener(new TextChangeListener() {

                public void textChange(TextChangeEvent event) {

                    m_resultComponent.getFileTable().filterTable(event.getText());

                }
            });
        }
    }

    /** The log object for this class. */
    static final Log LOG = CmsLog.getLog(CmsUnusedContentFinderComposite.class);

    /** The maximum number of results. */
    static final int MAX_RESULTS = 5000;

    /** Schema parameter name. */
    static final String SCHEMA_PARAM_NAME = "unusedcontentfinder";

    /** Schema parameter value. */
    static final String SCHEMA_PARAM_VALUE = "include";

    /** The delete button of this unused content finder. */
    DeleteButtonComponent m_deleteButtonComponent;

    /** The form component of this unused content finder. */
    FormComponent m_formComponent;

    /** The result component of this unused content finder. */
    ResultComponent m_resultComponent;

    /** The result filter component of this unused content finder. */
    ResultFilterComponent m_resultFilterComponent;

    /**
     * Creates a new component.
     */
    public CmsUnusedContentFinderComposite() {

        m_deleteButtonComponent = new DeleteButtonComponent();
        m_formComponent = new FormComponent();
        m_resultComponent = new ResultComponent();
        m_resultFilterComponent = new ResultFilterComponent();
    }

    /**
     * Returns the delete button component of this unused content finder.
     * @return the delete button component of this unused content finder
     */
    public Component getDeleteButtonComponent() {

        return m_deleteButtonComponent;
    }

    /**
     * Returns the form component of this unused content finder.
     * @return the form component of this unused content finder
     */
    public Component getFormComponent() {

        return m_formComponent;
    }

    /**
     * Returns the result component of this unused content finder.
     * @return the result component of this unused content finder
     */
    public Component getResultComponent() {

        return m_resultComponent;
    }

    /**
     * Returns the result filter component of this unused content finder.
     * @return the result filter component of this unused content finder
     */
    public Component getResultFilterComponent() {

        return m_resultFilterComponent;
    }

    /**
     * Search for unused contents.
     * @param updateState whether to also update the state when searching
     */
    public void search(boolean updateState) {

        if (updateState) {
            CmsComponentState componentState = new CmsComponentState();
            componentState.setSite(m_formComponent.getSiteValue());
            componentState.setFolder(m_formComponent.getFolderValue());
            componentState.setResourceType(m_formComponent.getTypeValue());
            CmsAppWorkplaceUi.get().changeCurrentAppState(componentState.generateStateString());
        }
        m_resultComponent.updateResult();
    }

    /**
     * Initializes this component with data from a given state bean.
     * @param componentState the state bean
     */
    public void setState(CmsComponentState componentState) {

        m_formComponent.setSiteValue(componentState.getSite());
        m_formComponent.setFolderValue(componentState.getFolder());
        m_formComponent.setTypeValue(componentState.getResourceType());
    }

    /**
     * Returns the list of all relevant XML content types. A content type is relevant
     * if either there is a parameter "unusedcontentfinder" defined in the XML
     * content schema and the value of this parameter is set to "include"; or, the
     * "unusedcontentfinder" parameter is not defined at all but the containerPageOnly
     * attribute of the searchsettings element is set to "true".
     * @return the list of all relevant XML content types
     */
    List<I_CmsResourceType> getAvailableTypes() {

        List<I_CmsResourceType> result = new ArrayList<>();
        CmsObject cms = A_CmsUI.getCmsObject();
        List<I_CmsResourceType> types = OpenCms.getResourceManager().getResourceTypes();
        for (I_CmsResourceType type : types) {
            CmsExplorerTypeSettings typeSetting = OpenCms.getWorkplaceManager().getExplorerTypeSetting(
                type.getTypeName());
            if (typeSetting == null) {
                continue;
            }
            if ((type instanceof CmsResourceTypeXmlContent)
                && !((type instanceof CmsResourceTypeXmlContainerPage)
                    || (type instanceof CmsResourceTypeXmlAdeConfiguration))) {
                CmsResourceTypeXmlContent xmlType = (CmsResourceTypeXmlContent)type;
                try {
                    CmsFile schemaFile = cms.readFile(xmlType.getSchema());
                    CmsXmlContentDefinition definition = CmsXmlContentDefinition.unmarshal(
                        cms,
                        schemaFile.getRootPath());
                    I_CmsXmlContentHandler contentHandler = definition.getContentHandler();
                    String parameter = contentHandler.getParameter(SCHEMA_PARAM_NAME);
                    if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(parameter)) {
                        if (parameter.equalsIgnoreCase(SCHEMA_PARAM_VALUE)) {
                            result.add(type);
                        }
                    } else if (contentHandler.isContainerPageOnly()) {
                        result.add(type);
                    }
                } catch (Throwable t) {
                    // may happen for internal types
                }
            }
        }
        return result;
    }

    /**
     * Returns the offline CMS.
     * @return the offline CMS
     */
    CmsObject getOfflineCms() {

        CmsObject offlineCms = null;
        try {
            offlineCms = OpenCms.initCmsObject(A_CmsUI.getCmsObject());
            offlineCms.getRequestContext().setSiteRoot(m_formComponent.getSiteValue());
            offlineCms.getRequestContext().setCurrentProject(offlineCms.readProject("Offline"));
        } catch (CmsException e) {
            LOG.error(e.getLocalizedMessage(), e);
        }
        return offlineCms;
    }

    /**
     * Return the online CMS.
     * @return the online CMS
     */
    CmsObject getOnlineCms() {

        CmsObject onlineCms = null;
        try {
            onlineCms = OpenCms.initCmsObject(A_CmsUI.getCmsObject());
            onlineCms.getRequestContext().setSiteRoot(m_formComponent.getSiteValue());
            onlineCms.getRequestContext().setCurrentProject(onlineCms.readProject(CmsProject.ONLINE_PROJECT_ID));
        } catch (CmsException e) {
            LOG.error(e.getLocalizedMessage(), e);
        }
        return onlineCms;
    }

    /**
     * Returns whether a resource is excluded by a property. This is the case for properties:
     * <li>element.model=true
     * <li>NavInfo=keep
     * @param resource the resource to check
     * @param online whether to check in the online project
     * @return whether the resource in not excluded by a property
     * @throws CmsException if reading the properties fails
     */
    boolean isExcludedByProperties(CmsResource resource, boolean online) throws CmsException {

        CmsObject cms = online ? getOnlineCms() : getOfflineCms();
        CmsProperty navInfo = cms.readPropertyObject(resource, CmsPropertyDefinition.PROPERTY_NAVINFO, true);
        if (!navInfo.isNullProperty() && navInfo.getValue().equals("keep")) {
            return true;
        }
        CmsProperty elementModel = cms.readPropertyObject(resource, CmsPropertyDefinition.PROPERTY_ELEMENT_MODEL, true);
        if (!elementModel.isNullProperty() && elementModel.getValue().equals("true")) {
            return true;
        }
        return false;
    }

    /**
     * Returns whether a resource is used by other contents.
     * @param resource the resource to check
     * @param online whether the check should be performed online
     * @return whether the resource is used by other contents
     * @throws CmsException if reading related resources fails
     */
    boolean isUsedByOtherContents(CmsResource resource, boolean online) throws CmsException {

        if (online && resource.getState().isNew()) {
            return false;
        }
        CmsRelationFilter filter = CmsRelationFilter.SOURCES;
        CmsObject cms = online ? getOnlineCms() : getOfflineCms();
        List<CmsRelation> relations = cms.getRelationsForResource(resource, filter);
        return !relations.isEmpty();
    }

    /**
     * Returns whether the current site and folder selection is valid.
     * Only path levels greater or equal than 2 are allowed.
     * @return whether the current site and folder selection is valid
     */
    boolean isValidFolder() {

        String rootPath = CmsStringUtil.joinPaths(m_formComponent.getSiteValue(), m_formComponent.getFolderValue());
        if (CmsResource.getPathLevel(rootPath) < 2) {
            return false;
        }
        return true;
    }
}
