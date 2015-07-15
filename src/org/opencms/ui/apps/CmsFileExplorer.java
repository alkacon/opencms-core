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
import org.opencms.main.CmsException;
import org.opencms.ui.A_CmsUI;
import org.opencms.ui.I_CmsContextMenuBuilder;
import org.opencms.ui.I_CmsDialogContext;
import org.opencms.ui.components.CmsErrorDialog;
import org.opencms.ui.components.CmsFileTable;
import org.opencms.ui.components.CmsToolBar;
import org.opencms.ui.dialogs.availability.CmsAvailabilityDialog;
import org.opencms.util.CmsStringUtil;
import org.opencms.util.CmsUUID;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import org.vaadin.peter.contextmenu.ContextMenu;
import org.vaadin.peter.contextmenu.ContextMenu.ContextMenuItem;
import org.vaadin.peter.contextmenu.ContextMenu.ContextMenuItemClickEvent;
import org.vaadin.peter.contextmenu.ContextMenu.ContextMenuItemClickListener;

import com.google.common.collect.Lists;
import com.vaadin.data.Item;
import com.vaadin.data.util.HierarchicalContainer;
import com.vaadin.event.FieldEvents.TextChangeEvent;
import com.vaadin.event.FieldEvents.TextChangeListener;
import com.vaadin.event.ItemClickEvent;
import com.vaadin.event.ItemClickEvent.ItemClickListener;
import com.vaadin.server.FontAwesome;
import com.vaadin.server.Sizeable.Unit;
import com.vaadin.shared.MouseEventDetails.MouseButton;
import com.vaadin.ui.AbsoluteLayout;
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

/**
 * The file explorer app.<p>
 */
public class CmsFileExplorer implements I_CmsWorkplaceApp {

    /**
     * Context menu builder for explorer.<p>
     */
    protected class MenuBuilder implements I_CmsContextMenuBuilder {

        public void buildContextMenu(Set<CmsUUID> structureIds, ContextMenu menu) {

            ContextMenuItem availability = menu.addItem("Availability");
            availability.addItemClickListener(new ContextMenuItemClickListener() {

                public void contextMenuItemClicked(ContextMenuItemClickEvent event) {

                    I_CmsDialogContext context = createDialogContext();
                    CmsAvailabilityDialog availability = new CmsAvailabilityDialog(context);
                    m_savedExplorerState = CmsAppWorkplaceUi.get().getNavigator().getState();
                    CmsAppWorkplaceUi.get().changeCurrentAppState(
                        CmsAppWorkplaceUi.get().getAppState() + "#availability");
                    m_appContext.setAppContent(availability);
                }
            });

        }
    }

    /** The files and folder resource filter. */
    private static final CmsResourceFilter FILES_N_FOLDERS = CmsResourceFilter.ONLY_VISIBLE;

    /** The folders resource filter. */
    private static final CmsResourceFilter FOLDERS = CmsResourceFilter.ONLY_VISIBLE_NO_DELETED.addRequireFolder();

    /** Saved explorer state used by dialogs after they have finished. */
    protected String m_savedExplorerState = "";

    /** The UI context. */
    private I_CmsAppUIContext m_appContext;

    /** The current app state. */
    private String m_currentState;

    /** The table containing the contents of the current folder. */
    private CmsFileTable m_fileTable;

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

    /** The currently viewed folder. */
    private CmsUUID m_currentFolder;

    /**
     * Constructor.<p>
     */
    public CmsFileExplorer() {
        m_fileTable = new CmsFileTable();
        m_fileTable.setSizeFull();
        m_fileTable.setMenuBuilder(new MenuBuilder() /**/);
        m_fileTable.addItemClickListener(new ItemClickListener() {

            private static final long serialVersionUID = 1L;

            public void itemClick(ItemClickEvent event) {

                if (event.getButton().equals(MouseButton.LEFT)
                    && !CmsFileTable.PROPERTY_TYPE_ICON.equals(event.getPropertyId())) {
                    Boolean isFolder = (Boolean)event.getItem().getItemProperty(
                        CmsFileTable.PROPERTY_IS_FOLDER).getValue();
                    if ((isFolder != null) && isFolder.booleanValue()) {
                        expandCurrentFolder();
                        readFolder((CmsUUID)event.getItemId());
                    }
                }

            }
        });
        m_fileTree = new Tree();
        m_fileTree.setWidth("100%");
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
        m_infoTitle.addStyleName("h4");
        m_info.addComponent(m_infoTitle, "top:3px; left:0px;");
        m_infoPath = new Label();
        m_infoPath.addStyleName("tiny");
        m_info.addComponent(m_infoPath, "bottom:3px; left:0px;");
        m_searchField = new TextField();
        m_searchField.setIcon(FontAwesome.SEARCH);
        m_searchField.setInputPrompt("Search");
        m_searchField.addStyleName("small");
        m_searchField.addStyleName("inline-icon");
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
    }

    /**
     * @see org.opencms.ui.apps.I_CmsWorkplaceApp#initUI(org.opencms.ui.apps.I_CmsAppUIContext)
     */
    public void initUI(I_CmsAppUIContext context) {

        m_appContext = context;
        HorizontalSplitPanel sp = new HorizontalSplitPanel();
        sp.setSizeFull();
        sp.setFirstComponent(m_fileTree);
        sp.setSecondComponent(m_fileTable);
        sp.setSplitPosition(400 - 1, Unit.PIXELS);
        context.setAppContent(sp);
        context.setAppInfo(m_info);
        context.addToolbarButton(CmsToolBar.createButton(FontAwesome.MAGIC));
        context.addToolbarButton(CmsToolBar.createButton(FontAwesome.ARROW_UP));
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

        HierarchicalContainer container = new HierarchicalContainer();
        container.addContainerProperty(CmsFileTable.PROPERTY_RESOURCE_NAME, String.class, null);
        container.addContainerProperty(CmsFileTable.PROPERTY_STATE, CmsResourceState.class, null);
        CmsObject cms = A_CmsUI.getCmsObject();
        try {
            CmsResource siteRoot = cms.readResource("/", FOLDERS);
            addTreeItem(siteRoot, null, container);
            List<CmsResource> folderResources = cms.readResources("/", FOLDERS, false);
            for (CmsResource resource : folderResources) {
                addTreeItem(resource, siteRoot.getStructureId(), container);
            }
            m_fileTree.setContainerDataSource(container);
            m_fileTree.expandItem(siteRoot.getStructureId());
        } catch (CmsException e) {
            // TODO: Auto-generated catch block
            e.printStackTrace();
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

        Set<CmsUUID> selected = m_fileTable.getValue();
        final List<CmsResource> resources = Lists.newArrayList();
        for (CmsUUID id : selected) {
            try {
                CmsResource res = A_CmsUI.getCmsObject().readResource(id, CmsResourceFilter.IGNORE_EXPIRATION);
                resources.add(res);
            } catch (CmsException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

        }

        return new I_CmsDialogContext() {

            public CmsObject getCms() {

                return A_CmsUI.getCmsObject();
            }

            public List<CmsResource> getResources() {

                return resources;
            }

            public void onError(Throwable error) {

                CmsErrorDialog err = new CmsErrorDialog(error, this);
                m_appContext.setAppContent(err);
            }

            public void onFinish(Object result) {

                CmsAppWorkplaceUi.get().getNavigator().navigateTo(m_savedExplorerState);
            }

        };

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
            // TODO: Auto-generated catch block
            e.printStackTrace();
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

}
