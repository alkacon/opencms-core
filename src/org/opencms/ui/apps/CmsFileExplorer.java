/*
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (c) Alkacon Software GmbH (http://www.alkacon.com)
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

package org.opencms.ui.apps;

import org.opencms.db.CmsResourceState;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsProperty;
import org.opencms.file.CmsPropertyDefinition;
import org.opencms.file.CmsResource;
import org.opencms.file.CmsResourceFilter;
import org.opencms.file.CmsVfsResourceNotFoundException;
import org.opencms.file.types.I_CmsResourceType;
import org.opencms.lock.CmsLockActionRecord;
import org.opencms.lock.CmsLockActionRecord.LockChange;
import org.opencms.lock.CmsLockUtil;
import org.opencms.main.CmsException;
import org.opencms.main.CmsLog;
import org.opencms.main.OpenCms;
import org.opencms.site.CmsSite;
import org.opencms.ui.A_CmsUI;
import org.opencms.ui.CmsVaadinUtils;
import org.opencms.ui.FontOpenCms;
import org.opencms.ui.I_CmsContextMenuBuilder;
import org.opencms.ui.I_CmsDialogContext;
import org.opencms.ui.I_CmsUpdateListener;
import org.opencms.ui.components.A_CmsFocusShortcutListener;
import org.opencms.ui.components.CmsErrorDialog;
import org.opencms.ui.components.CmsFileTable;
import org.opencms.ui.components.CmsResourceTableProperty;
import org.opencms.ui.components.CmsToolBar;
import org.opencms.ui.components.I_CmsFilePropertyEditHandler;
import org.opencms.ui.components.I_CmsWindowCloseListener;
import org.opencms.ui.components.extensions.CmsGwtDialogExtension;
import org.opencms.ui.contextmenu.CmsContextMenuTreeBuilder;
import org.opencms.ui.contextmenu.I_CmsContextMenuItem;
import org.opencms.ui.contextmenu.I_CmsContextMenuItemProvider;
import org.opencms.util.CmsStringUtil;
import org.opencms.util.CmsTreeNode;
import org.opencms.util.CmsUUID;
import org.opencms.workplace.CmsWorkplace;
import org.opencms.workplace.explorer.CmsExplorerTypeSettings;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.apache.commons.logging.Log;

import org.vaadin.peter.contextmenu.ContextMenu;
import org.vaadin.peter.contextmenu.ContextMenu.ContextMenuItem;
import org.vaadin.peter.contextmenu.ContextMenu.ContextMenuItemClickEvent;
import org.vaadin.peter.contextmenu.ContextMenu.ContextMenuItemClickListener;

import com.vaadin.data.Item;
import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.Property.ValueChangeListener;
import com.vaadin.data.Validator.InvalidValueException;
import com.vaadin.data.util.HierarchicalContainer;
import com.vaadin.data.util.IndexedContainer;
import com.vaadin.event.FieldEvents.TextChangeEvent;
import com.vaadin.event.FieldEvents.TextChangeListener;
import com.vaadin.event.ItemClickEvent;
import com.vaadin.event.ItemClickEvent.ItemClickListener;
import com.vaadin.event.ShortcutAction.KeyCode;
import com.vaadin.navigator.ViewChangeListener;
import com.vaadin.server.ExternalResource;
import com.vaadin.server.FontAwesome;
import com.vaadin.server.Resource;
import com.vaadin.server.Sizeable.Unit;
import com.vaadin.shared.MouseEventDetails.MouseButton;
import com.vaadin.shared.ui.combobox.FilteringMode;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.HorizontalSplitPanel;
import com.vaadin.ui.TextField;
import com.vaadin.ui.Tree;
import com.vaadin.ui.Tree.CollapseEvent;
import com.vaadin.ui.Tree.CollapseListener;
import com.vaadin.ui.Tree.ExpandEvent;
import com.vaadin.ui.Tree.ExpandListener;
import com.vaadin.ui.Tree.ItemStyleGenerator;
import com.vaadin.ui.UI;
import com.vaadin.ui.themes.ValoTheme;

/**
 * The file explorer app.<p>
 */
public class CmsFileExplorer implements I_CmsWorkplaceApp, ViewChangeListener, I_CmsWindowCloseListener {

    /**
     * Handles inline editing within the file table.<p>
     */
    public class ContextMenuEditHandler implements ContextMenuItemClickListener, I_CmsFilePropertyEditHandler {

        /** The edited content structure id. */
        private CmsUUID m_editId;

        /** The edited property. */
        private CmsResourceTableProperty m_editProperty;

        /** The lock action record. */
        private CmsLockActionRecord m_lockActionRecord;

        /**
         * Constructor.<p>
         *
         * @param editId the content structure id
         * @param editProperty the property to edit
         */
        public ContextMenuEditHandler(CmsUUID editId, CmsResourceTableProperty editProperty) {
            m_editId = editId;
            m_editProperty = editProperty;
        }

        /**
         * Cancels the edit process. Unlocks the resource if required.<p>
         *
         * @see org.opencms.ui.components.I_CmsFilePropertyEditHandler#cancel()
         */
        public void cancel() {

            if (m_lockActionRecord.getChange() == LockChange.locked) {
                CmsObject cms = A_CmsUI.getCmsObject();
                try {
                    CmsResource res = cms.readResource(m_editId);
                    cms.unlockResource(res);
                } catch (CmsException e) {
                    LOG.warn("Failed to unlock resource " + m_editId.toString(), e);
                }
            }
        }

        /**
         * @see org.vaadin.peter.contextmenu.ContextMenu.ContextMenuItemClickListener#contextMenuItemClicked(org.vaadin.peter.contextmenu.ContextMenu.ContextMenuItemClickEvent)
         */
        public void contextMenuItemClicked(ContextMenuItemClickEvent event) {

            CmsObject cms = A_CmsUI.getCmsObject();
            try {
                CmsResource res = cms.readResource(m_editId);
                m_lockActionRecord = CmsLockUtil.ensureLock(cms, res);
                m_fileTable.startEdit(m_editId, m_editProperty, this);
            } catch (CmsException e) {
                CmsErrorDialog.showErrorDialog(e);
                LOG.debug(e.getLocalizedMessage(), e);
            }
        }

        /**
         * @see org.opencms.ui.components.I_CmsFilePropertyEditHandler#save(java.lang.String)
         */
        public void save(String value) {

            try {
                CmsObject cms = A_CmsUI.getCmsObject();
                CmsResource res = cms.readResource(m_editId);
                try {
                    if (CmsResourceTableProperty.PROPERTY_NAVIGATION_TEXT.equals(m_editProperty)
                        || CmsResourceTableProperty.PROPERTY_TITLE.equals(m_editProperty)) {

                        CmsProperty prop = new CmsProperty(
                            m_editProperty == CmsResourceTableProperty.PROPERTY_NAVIGATION_TEXT
                            ? CmsPropertyDefinition.PROPERTY_NAVTEXT
                            : CmsPropertyDefinition.PROPERTY_TITLE,
                            value,
                            null);
                        cms.writePropertyObject(cms.getSitePath(res), prop);
                    } else if (CmsResourceTableProperty.PROPERTY_RESOURCE_NAME.equals(m_editProperty)) {
                        String sourcePath = cms.getSitePath(res);
                        cms.renameResource(
                            sourcePath,
                            CmsStringUtil.joinPaths(CmsResource.getParentFolder(sourcePath), value));
                    }
                } finally {
                    if (m_lockActionRecord.getChange() == LockChange.locked) {
                        cms.unlockResource(res);
                    }

                }
            } catch (CmsException e) {
                LOG.error("Exception while saving changed " + m_editProperty + " to resource " + m_editId, e);
                CmsErrorDialog.showErrorDialog(e);
            }

        }

        /**
         * @see org.opencms.ui.components.I_CmsFilePropertyEditHandler#validate(java.lang.String)
         */
        public void validate(String value) throws InvalidValueException {

            // TODO validate file name

        }
    }

    /**
     * Context menu builder for explorer.<p>
     */
    protected class MenuBuilder implements I_CmsContextMenuBuilder {

        /** Tree builder used to build the tree of menu items. */
        private CmsContextMenuTreeBuilder m_treeBuilder;

        /**
         * @see org.opencms.ui.I_CmsContextMenuBuilder#buildContextMenu(java.util.List, org.vaadin.peter.contextmenu.ContextMenu)
         */
        public void buildContextMenu(List<CmsResource> resources, ContextMenu menu) {

            if (resources.size() == 1) {
                CmsUUID editId = resources.iterator().next().getStructureId();
                ContextMenuItem editTitle = menu.addItem("Edit title");
                editTitle.addItemClickListener(
                    new ContextMenuEditHandler(editId, CmsResourceTableProperty.PROPERTY_TITLE));
                ContextMenuItem editNavText = menu.addItem("Edit navigation text");
                editNavText.addItemClickListener(
                    new ContextMenuEditHandler(editId, CmsResourceTableProperty.PROPERTY_NAVIGATION_TEXT));
                ContextMenuItem editResourceName = menu.addItem("Rename");
                editResourceName.addItemClickListener(
                    new ContextMenuEditHandler(editId, CmsResourceTableProperty.PROPERTY_RESOURCE_NAME));
            }
            CmsContextMenuTreeBuilder treeBuilder = new CmsContextMenuTreeBuilder(
                A_CmsUI.getCmsObject(),
                m_fileTable.getSelectedResources());
            m_treeBuilder = treeBuilder;
            CmsTreeNode<I_CmsContextMenuItem> tree = treeBuilder.buildAll(m_menuItemProvider.getMenuItems());
            for (CmsTreeNode<I_CmsContextMenuItem> node : tree.getChildren()) {
                createItem(menu, node);
            }
        }

        /**
         * Gets the localized title for the context menu item by resolving any message key macros in the raw title using the current locale.<p>
         *
         * @param item the unlocalized title
         * @return the localized title
         */
        String getTitle(I_CmsContextMenuItem item) {

            return CmsVaadinUtils.localizeString(item.getTitle());
        }

        /**
         * Creates a context menu item.<p>
         *
         * @param parent the parent (either the context menu itself, or a parent item)
         * @param node the node which should be added as a context menu item
         *
         * @return the created item
         */
        private ContextMenuItem createItem(Object parent, CmsTreeNode<I_CmsContextMenuItem> node) {

            final I_CmsContextMenuItem data = node.getData();
            ContextMenuItem guiMenuItem = null;
            if (parent instanceof ContextMenu) {
                guiMenuItem = ((ContextMenu)parent).addItem(getTitle(data));
            } else {
                guiMenuItem = ((ContextMenuItem)parent).addItem(getTitle(data));
            }
            if (m_treeBuilder.getVisibility(data).isInActive()) {
                guiMenuItem.setEnabled(false);
            }
            if (node.getChildren().size() > 0) {
                for (CmsTreeNode<I_CmsContextMenuItem> childNode : node.getChildren()) {
                    createItem(guiMenuItem, childNode);
                }
            } else {
                guiMenuItem.addItemClickListener(new ContextMenuItemClickListener() {

                    public void contextMenuItemClicked(ContextMenuItemClickEvent event) {

                        data.executeAction(createDialogContext(data));
                    }
                });

            }
            return guiMenuItem;
        }
    }

    /** The files and folder resource filter. */
    private static final CmsResourceFilter FILES_N_FOLDERS = CmsResourceFilter.ONLY_VISIBLE;

    /** The folders resource filter. */
    private static final CmsResourceFilter FOLDERS = CmsResourceFilter.ONLY_VISIBLE_NO_DELETED.addRequireFolder();

    /** Logger instance for this class. */
    static final Log LOG = CmsLog.getLog(CmsFileExplorer.class);

    /** The serial version id. */
    private static final long serialVersionUID = 1L;

    /** Site selector caption property. */
    public static final String SITE_CAPTION = "site_caption";

    /** Site selector site root property. */
    public static final String SITE_ROOT = "site_root";

    /** The opened paths session attribute name. */
    public static final String OPENED_PATHS = "explorer-opened-paths";

    /** The state separator string. */
    public static final String STATE_SEPARATOR = "!!";

    /** The opened paths by site. */
    private Map<String, String> m_openedPaths;

    /** The UI context. */
    protected I_CmsAppUIContext m_appContext;

    /** The menu item provider. */
    protected I_CmsContextMenuItemProvider m_menuItemProvider = CmsAppWorkplaceUi.get().getMenuItemProvider();

    /** Saved explorer state used by dialogs after they have finished. */
    protected String m_savedExplorerState = "";

    /** The table containing the contents of the current folder. */
    CmsFileTable m_fileTable;

    /** The currently viewed folder. */
    private CmsUUID m_currentFolder;

    /** The current app state. */
    private String m_currentState;

    /** The folder tree. */
    private Tree m_fileTree;

    /** The info path. */
    TextField m_infoPath;

    /** The search field. */
    private TextField m_searchField;

    /** The folder tree data container. */
    private HierarchicalContainer m_treeContainer;

    /** The move up button. */
    private Button m_upButton;

    /** The site selector. */
    private ComboBox m_siteSelector;

    /**
     * Constructor.<p>
     */
    @SuppressWarnings("unchecked")
    public CmsFileExplorer() {
        m_fileTable = new CmsFileTable();
        m_fileTable.setSizeFull();
        m_fileTable.setMenuBuilder(new MenuBuilder());
        m_fileTable.addItemClickListener(new ItemClickListener() {

            private static final long serialVersionUID = 1L;

            public void itemClick(ItemClickEvent event) {

                handleFileItemClick(event);
            }
        });
        m_treeContainer = new HierarchicalContainer();
        m_treeContainer.addContainerProperty(CmsResourceTableProperty.PROPERTY_RESOURCE_NAME, String.class, null);
        m_treeContainer.addContainerProperty(CmsResourceTableProperty.PROPERTY_STATE, CmsResourceState.class, null);
        m_treeContainer.addContainerProperty(CmsResourceTableProperty.PROPERTY_TYPE_ICON, Resource.class, null);
        m_fileTree = new Tree();
        m_fileTree.setWidth("100%");
        m_fileTree.setContainerDataSource(m_treeContainer);
        m_fileTree.setItemIconPropertyId(CmsResourceTableProperty.PROPERTY_TYPE_ICON);
        m_fileTree.setItemCaptionPropertyId(CmsResourceTableProperty.PROPERTY_RESOURCE_NAME);
        m_fileTree.addExpandListener(new ExpandListener() {

            private static final long serialVersionUID = 1L;

            public void nodeExpand(ExpandEvent event) {

                readTreeLevel((CmsUUID)event.getItemId());
            }
        });
        m_fileTree.addCollapseListener(new CollapseListener() {

            private static final long serialVersionUID = 1L;

            public void nodeCollapse(CollapseEvent event) {

                clearTreeLevel((CmsUUID)event.getItemId());
            }
        });

        m_fileTree.addItemClickListener(new ItemClickListener() {

            private static final long serialVersionUID = 1L;

            public void itemClick(ItemClickEvent event) {

                readFolder((CmsUUID)event.getItemId());
            }
        });

        m_fileTree.setItemStyleGenerator(new ItemStyleGenerator() {

            private static final long serialVersionUID = 1L;

            public String getStyle(Tree source, Object itemId) {

                return CmsFileTable.getStateStyle(source.getContainerDataSource().getItem(itemId));
            }
        });

        m_siteSelector = createSiteSelect(A_CmsUI.getCmsObject());
        m_infoPath = new TextField();
        A_CmsFocusShortcutListener shortcutListener = new A_CmsFocusShortcutListener("Open path", KeyCode.ENTER, null) {

            private static final long serialVersionUID = 1L;

            @Override
            public void handleAction(Object sender, Object target) {

                openPath(m_infoPath.getValue());
            }
        };
        shortcutListener.installOn(m_infoPath);
        m_searchField = new TextField();
        m_searchField.setIcon(FontAwesome.SEARCH);
        m_searchField.setInputPrompt("Search");
        m_searchField.addStyleName(ValoTheme.TEXTFIELD_INLINE_ICON);
        m_searchField.addTextChangeListener(new TextChangeListener() {

            private static final long serialVersionUID = 1L;

            public void textChange(TextChangeEvent event) {

                filterTable(event.getText());

            }
        });

        // toolbar buttons
        m_upButton = CmsToolBar.createButton(FontAwesome.ARROW_UP);
        m_upButton.addClickListener(new ClickListener() {

            private static final long serialVersionUID = 1L;

            public void buttonClick(ClickEvent event) {

                showParentFolder();
            }
        });

        m_openedPaths = (Map<String, String>)UI.getCurrent().getSession().getAttribute(OPENED_PATHS);
        if (m_openedPaths == null) {
            m_openedPaths = new HashMap<String, String>();
        }
    }

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

        OpenCms.getWorkplaceAppManager().storeAppSettings(
            A_CmsUI.getCmsObject(),
            CmsFileExplorerSettings.class,
            m_fileTable.getTableSettings());
        UI.getCurrent().getSession().setAttribute(OPENED_PATHS, m_openedPaths);
        return true;
    }

    /**
     * @see org.opencms.ui.apps.I_CmsWorkplaceApp#initUI(org.opencms.ui.apps.I_CmsAppUIContext)
     */
    public void initUI(I_CmsAppUIContext context) {

        m_appContext = context;
        HorizontalSplitPanel sp = new HorizontalSplitPanel();
        sp.setSizeFull();
        sp.setFirstComponent(m_fileTree);
        CmsFileExplorerSettings settings;
        try {
            settings = OpenCms.getWorkplaceAppManager().getAppSettings(
                A_CmsUI.getCmsObject(),
                CmsFileExplorerSettings.class);

            m_fileTable.setTableState(settings);
        } catch (Exception e) {
            LOG.error("Error while reading file explorer settings from user.", e);
        }
        sp.setSecondComponent(m_fileTable);
        sp.setSplitPosition(400 - 1, Unit.PIXELS);
        context.setAppContent(sp);
        context.showInfoArea(true);
        HorizontalLayout inf = new HorizontalLayout();
        inf.setSizeFull();
        inf.setSpacing(true);
        inf.setMargin(true);
        m_siteSelector.setWidth("379px");
        inf.addComponent(m_siteSelector);

        m_infoPath.setWidth("100%");
        inf.addComponent(m_infoPath);
        inf.setExpandRatio(m_infoPath, 1);

        m_searchField.setWidth("200px");
        inf.addComponent(m_searchField);
        context.setAppInfo(inf);

        context.addToolbarButton(CmsToolBar.createButton(FontAwesome.MAGIC));
        context.addToolbarButton(m_upButton);
        context.addToolbarButton(CmsToolBar.createButton(FontAwesome.UPLOAD));
        Button publishButton = CmsToolBar.createButton(FontOpenCms.PUBLISH);
        publishButton.addClickListener(new ClickListener() {

            /** Serial version id. */
            private static final long serialVersionUID = 1L;

            public void buttonClick(ClickEvent event) {

                onClickPublish();
            }
        });

        context.addToolbarButton(publishButton);

        populateFolderTree();
    }

    /**
     * Triggered when the user clicks the 'publsh' button.<p>
     */
    public void onClickPublish() {

        CmsGwtDialogExtension extension = new CmsGwtDialogExtension(A_CmsUI.get(), new I_CmsUpdateListener<String>() {

            public void onUpdate(List<String> updatedItems) {

                updateAll();
            }

        });
        extension.openPublishDialog(A_CmsUI.getCmsObject().getRequestContext().getCurrentProject());
    }

    /**
     * @see org.opencms.ui.apps.I_CmsWorkplaceApp#onStateChange(java.lang.String)
     */
    public void onStateChange(String state) {

        state = normalizeState(state);
        if ((m_currentState == null) || !m_currentState.equals(state)) {
            m_currentState = state;
            CmsObject cms = A_CmsUI.getCmsObject();
            String siteRoot = getSiteRootFromState();
            String path = getPathFromState();
            if ((siteRoot != null) && !siteRoot.equals(cms.getRequestContext().getSiteRoot())) {
                changeSite(siteRoot, path);
                m_siteSelector.select(siteRoot);
            }
            openPath(path);
        }
    }

    /**
     * @see org.opencms.ui.components.I_CmsWindowCloseListener#onWindowClose()
     */
    public void onWindowClose() {

        OpenCms.getWorkplaceAppManager().storeAppSettings(
            A_CmsUI.getCmsObject(),
            CmsFileExplorerSettings.class,
            m_fileTable.getTableSettings());
        UI.getCurrent().getSession().setAttribute(OPENED_PATHS, m_openedPaths);
    }

    /**
     * Fills the file table with the resources from the given path.<p>
     *
     * @param sitePath a folder site path
     */
    public void populateFileTable(String sitePath) {

        CmsObject cms = A_CmsUI.getCmsObject();
        m_searchField.clear();
        try {
            List<CmsResource> folderResources = cms.readResources(sitePath, FILES_N_FOLDERS, false);
            m_fileTable.fillTable(cms, folderResources);
        } catch (CmsException e) {
            CmsErrorDialog.showErrorDialog(e);
            LOG.error(e.getLocalizedMessage(), e);
        }
    }

    /**
     * Populates the folder tree.<p>
     */
    public void populateFolderTree() {

        CmsObject cms = A_CmsUI.getCmsObject();
        m_treeContainer.removeAllItems();
        try {
            CmsResource siteRoot = cms.readResource("/", FOLDERS);
            addTreeItem(siteRoot, null, m_treeContainer);
            List<CmsResource> folderResources = cms.readResources("/", FOLDERS, false);
            for (CmsResource resource : folderResources) {
                addTreeItem(resource, siteRoot.getStructureId(), m_treeContainer);
            }
            m_fileTree.expandItem(siteRoot.getStructureId());
        } catch (CmsException e) {
            CmsErrorDialog.showErrorDialog(e);
            LOG.error(e.getLocalizedMessage(), e);
        }

    }

    /**
     * Updates the table entries with the given ids.<p>
     *
     * @param ids the ids of the table entries to update
     */
    public void update(List<CmsUUID> ids) {

        m_fileTable.update(ids);
        updateTree(ids);
    }

    /**
     * Updates display for all contents of the current folder.<p>
     */
    public void updateAll() {

        readFolder(m_currentFolder);
    }

    /**
     * Updates the tree items with the given ids.<p>
     *
     * @param ids the ids for which the tree should be updated
     */
    public void updateTree(List<CmsUUID> ids) {

        CmsObject cms = A_CmsUI.getCmsObject();

        for (CmsUUID id : ids) {
            try {
                CmsResource resource = cms.readResource(id, CmsResourceFilter.IGNORE_EXPIRATION);

                CmsResource parent = cms.readParentFolder(id);
                CmsUUID parentId = parent.getStructureId();
                Item resourceItem = m_treeContainer.getItem(id);
                if (resourceItem != null) {
                    // use the root path as name in case of the root item
                    resourceItem.getItemProperty(CmsResourceTableProperty.PROPERTY_RESOURCE_NAME).setValue(
                        parentId == null ? resource.getRootPath() : resource.getName());
                    resourceItem.getItemProperty(CmsResourceTableProperty.PROPERTY_STATE).setValue(resource.getState());
                    if (parentId != null) {
                        m_treeContainer.setParent(resource.getStructureId(), parentId);
                    }
                } else {
                    addTreeItem(resource, parentId, m_treeContainer);
                }
            } catch (CmsVfsResourceNotFoundException e) {
                m_treeContainer.removeItem(id);
                LOG.debug(e.getLocalizedMessage(), e);
            } catch (CmsException e) {
                CmsErrorDialog.showErrorDialog(e);
                LOG.error(e.getLocalizedMessage(), e);
            }
        }

    }

    /**
     * Clears the given tree level.<p>
     *
     * @param parentId the parent id
     */
    protected void clearTreeLevel(CmsUUID parentId) {

        // create a new list to avoid concurrent modifications
        Collection<?> children = m_treeContainer.getChildren(parentId);
        // may be null when monkey clicking
        if (children != null) {
            List<Object> childIds = new ArrayList<Object>(m_treeContainer.getChildren(parentId));
            for (Object childId : childIds) {
                m_treeContainer.removeItemRecursively(childId);
            }
        }
    }

    /**
     * Creates the dialog context for dialogs opened from the context menu.<p>
     *
     * @param item the context menu item
     *
     * @return the dialog context
     */
    protected I_CmsDialogContext createDialogContext(I_CmsContextMenuItem item) {

        return new CmsExplorerDialogContext(m_appContext, this, m_fileTable.getSelectedResources(), item);
    }

    /**
     * Reads the given folder.<p>
     *
     * @param folderId the folder id
     */
    protected void readFolder(CmsUUID folderId) {

        CmsObject cms = A_CmsUI.getCmsObject();
        m_searchField.clear();
        try {
            CmsResource folder = cms.readResource(folderId, FOLDERS);
            m_currentFolder = folderId;
            m_infoPath.setValue(cms.getSitePath(folder));
            List<CmsResource> childResources = cms.readResources(cms.getSitePath(folder), FILES_N_FOLDERS, false);
            m_fileTable.fillTable(cms, childResources);
            boolean hasFolderChild = false;
            for (CmsResource child : childResources) {
                if (child.isFolder()) {
                    hasFolderChild = true;
                    break;
                }
            }
            m_treeContainer.setChildrenAllowed(folderId, hasFolderChild);
            updateUpButtonStatus();
            String sitePath = folder.getRootPath().equals(cms.getRequestContext().getSiteRoot() + "/")
            ? ""
            : cms.getSitePath(folder);

            String state = normalizeState(cms.getRequestContext().getSiteRoot() + STATE_SEPARATOR + sitePath);

            if (!(state).equals(m_currentState)) {
                m_currentState = state;
                CmsAppWorkplaceUi.get().changeCurrentAppState(m_currentState);
            }
            m_openedPaths.put(cms.getRequestContext().getSiteRoot(), sitePath);
        } catch (CmsException e) {
            CmsErrorDialog.showErrorDialog(e);
            LOG.error(e.getLocalizedMessage(), e);
        }
    }

    /**
     * Reads the given tree level.<p>
     *
     * @param parentId the parent id
     */
    protected void readTreeLevel(CmsUUID parentId) {

        CmsObject cms = A_CmsUI.getCmsObject();
        try {
            CmsResource parent = cms.readResource(parentId, FOLDERS);
            List<CmsResource> folderResources = cms.readResources(cms.getSitePath(parent), FOLDERS, false);

            // sets the parent to leaf mode, in case no child folders are present
            m_treeContainer.setChildrenAllowed(parentId, !folderResources.isEmpty());

            for (CmsResource resource : folderResources) {
                addTreeItem(resource, parentId, m_treeContainer);
            }
            m_fileTree.markAsDirtyRecursive();
        } catch (CmsException e) {
            CmsErrorDialog.showErrorDialog(e);
            LOG.error(e.getLocalizedMessage(), e);
        }
    }

    /**
     * Switches to the requested site.<p>
     *
     * @param siteRoot the site root
     * @param path the folder path to open
     */
    void changeSite(String siteRoot, String path) {

        CmsObject cms = A_CmsUI.getCmsObject();
        if (!cms.getRequestContext().getSiteRoot().equals(siteRoot)) {
            cms.getRequestContext().setSiteRoot(siteRoot);
            populateFolderTree();
            openPath(path);
        }
    }

    /**
     * Expands the currently viewed folder in the tree.<p>
     */
    void expandCurrentFolder() {

        if (m_currentFolder != null) {
            m_treeContainer.setChildrenAllowed(m_currentFolder, true);
            m_fileTree.expandItem(m_currentFolder);
        }
    }

    /**
     * Filters the file table.<p>
     *
     * @param search the search term
     */
    void filterTable(String search) {

        m_fileTable.filterTable(search);
    }

    /**
     * Handles the file table item click.<p>
     *
     * @param event the click event
     */
    void handleFileItemClick(ItemClickEvent event) {

        if (m_fileTable.isEditing()) {
            m_fileTable.stopEdit();

        } else if (!event.isCtrlKey() && !event.isShiftKey()) {
            // don't interfere with multi-selection using control key
            if (event.getButton().equals(MouseButton.RIGHT)) {
                m_fileTable.handleSelection((CmsUUID)event.getItemId());
                m_fileTable.openContextMenu(event);
            } else if ((event.getPropertyId() == null)
                || CmsResourceTableProperty.PROPERTY_TYPE_ICON.equals(event.getPropertyId())) {
                m_fileTable.openContextMenu(event);
            } else {
                Boolean isFolder = (Boolean)event.getItem().getItemProperty(
                    CmsResourceTableProperty.PROPERTY_IS_FOLDER).getValue();
                if ((isFolder != null) && isFolder.booleanValue()) {
                    expandCurrentFolder();
                    if (m_fileTree.getItem(event.getItemId()) != null) {
                        m_fileTree.select(event.getItemId());
                    }
                    readFolder((CmsUUID)event.getItemId());

                }
            }
        }
    }

    /**
     * Opens the given site path.<p>
     *
     * @param path the path
     */
    void openPath(String path) {

        if (path == null) {
            path = m_openedPaths.get(A_CmsUI.getCmsObject().getRequestContext().getSiteRoot());
            if (path == null) {
                path = "";
            }
        }
        String[] pathItems = path.split("/");
        Collection<?> rootItems = m_treeContainer.rootItemIds();
        if (rootItems.size() != 1) {
            throw new RuntimeException("Illeagal state, folder tree has " + rootItems.size() + " children");
        }
        CmsUUID folderId = (CmsUUID)rootItems.iterator().next();
        Collection<?> children = m_treeContainer.getChildren(folderId);
        for (int i = 0; i < pathItems.length; i++) {
            if (CmsStringUtil.isEmptyOrWhitespaceOnly(pathItems[i])) {
                continue;
            }
            CmsUUID level = null;
            for (Object id : children) {
                if (m_treeContainer.getItem(id).getItemProperty(
                    CmsResourceTableProperty.PROPERTY_RESOURCE_NAME).getValue().equals(pathItems[i])) {
                    level = (CmsUUID)id;
                    m_fileTree.expandItem(level);
                    break;
                }
            }
            if ((level == null) || level.equals(folderId)) {
                break;
            }
            folderId = level;
            children = m_treeContainer.getChildren(folderId);
        }
        readFolder(folderId);
    }

    /**
     * Shows the parent folder, if available.<p>
     */
    void showParentFolder() {

        CmsUUID parentId = (CmsUUID)m_treeContainer.getParent(m_currentFolder);
        if (parentId != null) {
            readFolder(parentId);
            m_fileTree.select(parentId);
        }

    }

    /**
     * Adds an item to the folder tree.<p>
     *
     * @param resource the folder resource
     * @param parentId the parent folder id
     * @param container the data container
     */
    private void addTreeItem(CmsResource resource, CmsUUID parentId, HierarchicalContainer container) {

        Item resourceItem = container.getItem(resource.getStructureId());
        if (resourceItem == null) {
            resourceItem = container.addItem(resource.getStructureId());
        }
        // use the root path as name in case of the root item
        resourceItem.getItemProperty(CmsResourceTableProperty.PROPERTY_RESOURCE_NAME).setValue(
            parentId == null ? resource.getRootPath() : resource.getName());
        resourceItem.getItemProperty(CmsResourceTableProperty.PROPERTY_STATE).setValue(resource.getState());
        I_CmsResourceType type = OpenCms.getResourceManager().getResourceType(resource);
        CmsExplorerTypeSettings settings = OpenCms.getWorkplaceManager().getExplorerTypeSetting(type.getTypeName());
        resourceItem.getItemProperty(CmsResourceTableProperty.PROPERTY_TYPE_ICON).setValue(
            new ExternalResource(CmsWorkplace.getResourceUri(CmsWorkplace.RES_PATH_FILETYPES + settings.getBigIcon())));
        if (parentId != null) {
            container.setParent(resource.getStructureId(), parentId);
        }
    }

    /**
     * Creates the site selector combo box.<p>
     *
     * @param cms the current cms context
     *
     * @return the combo box
     */
    private ComboBox createSiteSelect(CmsObject cms) {

        List<CmsSite> sites = OpenCms.getSiteManager().getAvailableSites(
            cms,
            true,
            true,
            cms.getRequestContext().getOuFqn());
        final IndexedContainer availableSites = new IndexedContainer();
        availableSites.addContainerProperty(SITE_CAPTION, String.class, null);
        Locale locale = A_CmsUI.get().getLocale();
        for (CmsSite site : sites) {
            Item siteItem = availableSites.addItem(site.getSiteRoot());
            String title = CmsWorkplace.substituteSiteTitleStatic(site.getTitle(), locale);
            if (CmsStringUtil.isEmptyOrWhitespaceOnly(title)) {
                title = site.getSiteRoot();
            }
            siteItem.getItemProperty(SITE_CAPTION).setValue(title);
        }
        ComboBox combo = new ComboBox(null, availableSites);
        combo.setInputPrompt("You can click here");
        combo.setTextInputAllowed(true);
        combo.setNullSelectionAllowed(false);
        combo.setWidth("200px");
        combo.setItemCaptionPropertyId(SITE_CAPTION);
        combo.select(cms.getRequestContext().getSiteRoot());
        combo.setFilteringMode(FilteringMode.CONTAINS);
        combo.addValueChangeListener(new ValueChangeListener() {

            private static final long serialVersionUID = 1L;

            public void valueChange(ValueChangeEvent event) {

                String value = (String)event.getProperty().getValue();
                if (availableSites.containsId(value)) {
                    changeSite(value, null);
                    availableSites.removeAllContainerFilters();
                }
            }
        });
        return combo;
    }

    /**
     * Returns the site path from the current state.<p>
     *
     * @return the site path
     */
    private String getPathFromState() {

        String path = null;
        if (m_currentState.contains(STATE_SEPARATOR)) {
            path = m_currentState.substring(m_currentState.indexOf(STATE_SEPARATOR) + STATE_SEPARATOR.length());
        }
        return path;
    }

    /**
     * Returns the site root from the current state.<p>
     *
     * @return the site root
     */
    private String getSiteRootFromState() {

        String siteRoot = null;
        if (m_currentState.contains(STATE_SEPARATOR)) {
            siteRoot = m_currentState.substring(0, m_currentState.indexOf(STATE_SEPARATOR));
        }
        return siteRoot;
    }

    /**
     * Normalizes the state string. Ensuring it starts with the right number of slashes resolving an issue with the vaadin state handling.<p>
     *
     * @param state the state string
     *
     * @return the normalized state string
     */
    private String normalizeState(String state) {

        String result = "";
        if (state.contains(STATE_SEPARATOR)) {
            if (!state.startsWith(STATE_SEPARATOR) && !state.startsWith("/")) {
                // in case the site root part is not empty, it should start with a slash
                result = "/" + state;
            } else {
                result = state;
                // make sure to remove excessive slashes, may get introduced through vaadin state handling
                while (result.startsWith("//")) {
                    result = result.substring(1);
                }
            }
        }
        return result;
    }

    /**
     * Updates the up button status.<p>
     */
    private void updateUpButtonStatus() {

        m_upButton.setEnabled(m_treeContainer.getParent(m_currentFolder) != null);
    }

}
