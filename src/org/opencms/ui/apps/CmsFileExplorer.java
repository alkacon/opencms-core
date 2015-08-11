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
import org.opencms.file.types.I_CmsResourceType;
import org.opencms.lock.CmsLockActionRecord;
import org.opencms.lock.CmsLockActionRecord.LockChange;
import org.opencms.lock.CmsLockUtil;
import org.opencms.main.CmsException;
import org.opencms.main.CmsLog;
import org.opencms.main.OpenCms;
import org.opencms.ui.A_CmsUI;
import org.opencms.ui.I_CmsContextMenuBuilder;
import org.opencms.ui.I_CmsDialogContext;
import org.opencms.ui.components.CmsFileTable;
import org.opencms.ui.components.CmsToolBar;
import org.opencms.ui.components.I_CmsFilePropertyEditHandler;
import org.opencms.ui.components.I_CmsWindowCloseListener;
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
import java.util.List;
import java.util.Locale;

import org.apache.commons.logging.Log;

import org.vaadin.peter.contextmenu.ContextMenu;
import org.vaadin.peter.contextmenu.ContextMenu.ContextMenuItem;
import org.vaadin.peter.contextmenu.ContextMenu.ContextMenuItemClickEvent;
import org.vaadin.peter.contextmenu.ContextMenu.ContextMenuItemClickListener;

import com.vaadin.data.Item;
import com.vaadin.data.Validator.InvalidValueException;
import com.vaadin.data.util.HierarchicalContainer;
import com.vaadin.event.FieldEvents.TextChangeEvent;
import com.vaadin.event.FieldEvents.TextChangeListener;
import com.vaadin.event.ItemClickEvent;
import com.vaadin.event.ItemClickEvent.ItemClickListener;
import com.vaadin.navigator.ViewChangeListener;
import com.vaadin.server.ExternalResource;
import com.vaadin.server.FontAwesome;
import com.vaadin.server.Resource;
import com.vaadin.server.Sizeable.Unit;
import com.vaadin.shared.MouseEventDetails.MouseButton;
import com.vaadin.ui.AbsoluteLayout;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.CssLayout;
import com.vaadin.ui.HorizontalSplitPanel;
import com.vaadin.ui.Label;
import com.vaadin.ui.Notification;
import com.vaadin.ui.TextField;
import com.vaadin.ui.Tree;
import com.vaadin.ui.Tree.CollapseEvent;
import com.vaadin.ui.Tree.CollapseListener;
import com.vaadin.ui.Tree.ExpandEvent;
import com.vaadin.ui.Tree.ExpandListener;
import com.vaadin.ui.Tree.ItemStyleGenerator;
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
        private String m_editProperty;

        /** The lock action record. */
        private CmsLockActionRecord m_lockActionRecord;

        /**
         * Constructor.<p>
         *
         * @param editId the content structure id
         * @param editProperty the property to edit
         */
        public ContextMenuEditHandler(CmsUUID editId, String editProperty) {
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
                    //TODO: show error dialog
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
                //TODO: show error dialog
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
                    if (CmsFileTable.PROPERTY_TITLE.equals(m_editProperty)
                        || CmsFileTable.PROPERTY_NAVIGATION_TEXT.equals(m_editProperty)) {

                        CmsProperty prop = new CmsProperty(m_editProperty, value, null);
                        cms.writePropertyObject(cms.getSitePath(res), prop);
                    } else if (CmsFileTable.PROPERTY_RESOURCE_NAME.equals(m_editProperty)) {
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
                //TODO: show error dialog
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
                editTitle.addItemClickListener(new ContextMenuEditHandler(editId, CmsFileTable.PROPERTY_TITLE));
                ContextMenuItem editNavText = menu.addItem("Edit navigation text");
                editNavText.addItemClickListener(
                    new ContextMenuEditHandler(editId, CmsFileTable.PROPERTY_NAVIGATION_TEXT));
                ContextMenuItem editResourceName = menu.addItem("Rename");
                editResourceName.addItemClickListener(
                    new ContextMenuEditHandler(editId, CmsFileTable.PROPERTY_RESOURCE_NAME));
            }
            CmsContextMenuTreeBuilder treeBuilder = new CmsContextMenuTreeBuilder(
                A_CmsUI.getCmsObject(),
                m_fileTable.getSelectedResources());
            m_treeBuilder = treeBuilder;
            CmsTreeNode<I_CmsContextMenuItem> tree = treeBuilder.buildTree(m_menuItemProvider.getMenuItems());
            for (CmsTreeNode<I_CmsContextMenuItem> node : tree.getChildren()) {
                createItem(menu, node);
            }

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
            Locale locale = A_CmsUI.get().getLocale();
            if (parent instanceof ContextMenu) {
                guiMenuItem = ((ContextMenu)parent).addItem(data.getTitle(locale));
            } else {
                guiMenuItem = ((ContextMenuItem)parent).addItem(data.getTitle(locale));
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

                        data.executeAction(createDialogContext());
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
    private static final Log LOG = CmsLog.getLog(CmsFileExplorer.class);

    /** The serial version id. */
    private static final long serialVersionUID = 1L;

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

    /** The info component. */
    private AbsoluteLayout m_info;

    /** The info path. */
    private Label m_infoPath;

    /** The info title. */
    private Label m_infoTitle;

    /** The search field. */
    private TextField m_searchField;

    /** The folder tree data container. */
    private HierarchicalContainer m_treeContainer;

    /** The move up button. */
    private Button m_upButton;

    /**
     * Constructor.<p>
     */
    public CmsFileExplorer() {
        m_fileTable = new CmsFileTable();
        m_fileTable.setSizeFull();
        m_fileTable.setMenuBuilder(new MenuBuilder());
        m_fileTable.addItemClickListener(new ItemClickListener() {

            private static final long serialVersionUID = 1L;

            public void itemClick(ItemClickEvent event) {

                if (event.getButton().equals(MouseButton.LEFT)
                    && !CmsFileTable.PROPERTY_TYPE_ICON.equals(event.getPropertyId())
                    && (event.getPropertyId() != null) // event.getPropertyId() is actually null when clicking on the icon. Not sure if this is a bug in the current Vaadin version or not.
                    && !m_fileTable.isEditProperty((String)event.getPropertyId())) {
                    Boolean isFolder = (Boolean)event.getItem().getItemProperty(
                        CmsFileTable.PROPERTY_IS_FOLDER).getValue();
                    if ((isFolder != null) && isFolder.booleanValue()) {
                        expandCurrentFolder();
                        readFolder((CmsUUID)event.getItemId());
                    }
                }

            }
        });
        m_treeContainer = new HierarchicalContainer();
        m_treeContainer.addContainerProperty(CmsFileTable.PROPERTY_RESOURCE_NAME, String.class, null);
        m_treeContainer.addContainerProperty(CmsFileTable.PROPERTY_STATE, CmsResourceState.class, null);
        m_treeContainer.addContainerProperty(CmsFileTable.PROPERTY_TYPE_ICON, Resource.class, null);
        m_fileTree = new Tree();
        m_fileTree.setWidth("100%");
        m_fileTree.setContainerDataSource(m_treeContainer);
        m_fileTree.setItemIconPropertyId(CmsFileTable.PROPERTY_TYPE_ICON);
        m_fileTree.setItemCaptionPropertyId(CmsFileTable.PROPERTY_RESOURCE_NAME);
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

        m_info = new AbsoluteLayout();
        m_info.setSizeFull();
        m_infoTitle = new Label();
        m_infoTitle.addStyleName(ValoTheme.LABEL_H4);
        m_info.addComponent(m_infoTitle, "top:3px; left:0px;");
        m_infoPath = new Label();
        m_infoPath.addStyleName(ValoTheme.LABEL_TINY);
        m_info.addComponent(m_infoPath, "bottom:3px; left:0px;");
        m_searchField = new TextField();
        m_searchField.setIcon(FontAwesome.SEARCH);
        m_searchField.setInputPrompt("Search");
        m_searchField.addStyleName(ValoTheme.TEXTFIELD_SMALL);
        m_searchField.addStyleName(ValoTheme.TEXTFIELD_INLINE_ICON);
        m_searchField.addTextChangeListener(new TextChangeListener() {

            private static final long serialVersionUID = 1L;

            public void textChange(TextChangeEvent event) {

                filterTable(event.getText());

            }
        });
        // requires a wrapper to work around issue with the inline icon positioning
        CssLayout wrapper = new CssLayout();
        wrapper.addComponent(m_searchField);
        m_info.addComponent(wrapper, "top:6px; right:15px;");

        // toolbar buttons
        m_upButton = CmsToolBar.createButton(FontAwesome.ARROW_UP);
        m_upButton.addClickListener(new ClickListener() {

            private static final long serialVersionUID = 1L;

            public void buttonClick(ClickEvent event) {

                showParentFolder();
            }
        });
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
        context.setAppInfo(m_info);
        context.addToolbarButton(CmsToolBar.createButton(FontAwesome.MAGIC));
        context.addToolbarButton(m_upButton);
        context.addToolbarButton(CmsToolBar.createButton(FontAwesome.UPLOAD));

        populateFolderTree();
    }

    /**
     * @see org.opencms.ui.apps.I_CmsWorkplaceApp#onStateChange(java.lang.String)
     */
    public void onStateChange(String state) {

        if ((m_currentState == null) || !m_currentState.equals(state)) {
            m_currentState = state;
            openPath(state);
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
            Notification.show(e.getMessage());
        }
    }

    /**
     * Populates the folder tree.<p>
     */
    public void populateFolderTree() {

        CmsObject cms = A_CmsUI.getCmsObject();
        try {
            CmsResource siteRoot = cms.readResource("/", FOLDERS);
            addTreeItem(siteRoot, null, m_treeContainer);
            List<CmsResource> folderResources = cms.readResources("/", FOLDERS, false);
            for (CmsResource resource : folderResources) {
                addTreeItem(resource, siteRoot.getStructureId(), m_treeContainer);
            }
            m_fileTree.expandItem(siteRoot.getStructureId());
        } catch (CmsException e) {
            LOG.error("Error while populating file explorer tree", e);
        }

    }

    /**
     * Clears the given tree level.<p>
     *
     * @param parentId the parent id
     */
    protected void clearTreeLevel(CmsUUID parentId) {

        HierarchicalContainer container = (HierarchicalContainer)m_fileTree.getContainerDataSource();
        // create a new list to avoid concurrent modifications
        Collection<?> children = container.getChildren(parentId);
        // may be null when monkey clicking
        if (children != null) {
            List<Object> childIds = new ArrayList<Object>(container.getChildren(parentId));
            for (Object childId : childIds) {
                container.removeItemRecursively(childId);
            }
        }
    }

    /**
     * Creates the dialog context for dialogs opened from the context menu.<p>
     *
     * @return the dialog context
     */
    protected I_CmsDialogContext createDialogContext() {

        return new CmsExplorerDialogContext(m_appContext, m_fileTable.getSelectedResources());
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
            CmsProperty titleProp = cms.readPropertyObject(folder, CmsPropertyDefinition.PROPERTY_TITLE, false);
            String title = titleProp.isNullProperty() ? "" : titleProp.getValue();
            m_currentFolder = folderId;
            m_infoTitle.setValue(title);
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
            ((HierarchicalContainer)m_fileTree.getContainerDataSource()).setChildrenAllowed(folderId, hasFolderChild);
            updateUpButtonStatus();
            String sitePath = folder.getRootPath().equals(cms.getRequestContext().getSiteRoot() + "/")
            ? ""
            : cms.getSitePath(folder);

            if (!sitePath.equals(m_currentState)) {
                m_currentState = sitePath;
                CmsAppWorkplaceUi.get().changeCurrentAppState(m_currentState);
            }
        } catch (CmsException e) {
            Notification.show(e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Reads the given tree level.<p>
     *
     * @param parentId the parent id
     */
    protected void readTreeLevel(CmsUUID parentId) {

        CmsObject cms = A_CmsUI.getCmsObject();
        HierarchicalContainer container = (HierarchicalContainer)m_fileTree.getContainerDataSource();
        try {
            CmsResource parent = cms.readResource(parentId, FOLDERS);
            List<CmsResource> folderResources = cms.readResources(cms.getSitePath(parent), FOLDERS, false);

            // sets the parent to leaf mode, in case no child folders are present
            container.setChildrenAllowed(parentId, !folderResources.isEmpty());

            for (CmsResource resource : folderResources) {
                addTreeItem(resource, parentId, container);
            }
            m_fileTree.markAsDirtyRecursive();
        } catch (CmsException e) {
            LOG.error("Failed to read eplorer settings", e);
        }
    }

    /**
     * Expands the currently viewed folder in the tree.<p>
     */
    void expandCurrentFolder() {

        if (m_currentFolder != null) {
            ((HierarchicalContainer)m_fileTree.getContainerDataSource()).setChildrenAllowed(m_currentFolder, true);
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
     * Shows the parent folder, if available.<p>
     */
    void showParentFolder() {

        CmsUUID parentId = (CmsUUID)((HierarchicalContainer)m_fileTree.getContainerDataSource()).getParent(
            m_currentFolder);
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
        resourceItem.getItemProperty(CmsFileTable.PROPERTY_RESOURCE_NAME).setValue(
            parentId == null ? resource.getRootPath() : resource.getName());
        resourceItem.getItemProperty(CmsFileTable.PROPERTY_STATE).setValue(resource.getState());
        I_CmsResourceType type = OpenCms.getResourceManager().getResourceType(resource);
        CmsExplorerTypeSettings settings = OpenCms.getWorkplaceManager().getExplorerTypeSetting(type.getTypeName());
        resourceItem.getItemProperty(CmsFileTable.PROPERTY_TYPE_ICON).setValue(
            new ExternalResource(CmsWorkplace.getResourceUri(CmsWorkplace.RES_PATH_FILETYPES + settings.getBigIcon())));
        if (parentId != null) {
            container.setParent(resource.getStructureId(), parentId);
        }
    }

    /**
     * Opens the given site path.<p>
     *
     * @param path the path
     */
    private void openPath(String path) {

        String[] pathItems = path.split("/");
        HierarchicalContainer container = (HierarchicalContainer)m_fileTree.getContainerDataSource();
        Collection<?> rootItems = container.rootItemIds();
        if (rootItems.size() != 1) {
            throw new RuntimeException("Illeagal state, folder tree has " + rootItems.size() + " children");
        }
        CmsUUID folderId = (CmsUUID)rootItems.iterator().next();
        Collection<?> children = container.getChildren(folderId);
        for (int i = 0; i < pathItems.length; i++) {
            if (CmsStringUtil.isEmptyOrWhitespaceOnly(pathItems[i])) {
                continue;
            }
            CmsUUID level = null;
            for (Object id : children) {
                if (container.getItem(id).getItemProperty(CmsFileTable.PROPERTY_RESOURCE_NAME).getValue().equals(
                    pathItems[i])) {
                    level = (CmsUUID)id;
                    m_fileTree.expandItem(level);
                    break;
                }
            }
            if ((level == null) || level.equals(folderId)) {
                break;
            }
            folderId = level;
            children = container.getChildren(folderId);
        }
        readFolder(folderId);
    }

    /**
     * Updates the up button status.<p>
     */
    private void updateUpButtonStatus() {

        m_upButton.setEnabled(
            ((HierarchicalContainer)m_fileTree.getContainerDataSource()).getParent(m_currentFolder) != null);
    }

}
