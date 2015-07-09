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

import org.opencms.file.CmsObject;
import org.opencms.file.CmsProperty;
import org.opencms.file.CmsPropertyDefinition;
import org.opencms.file.CmsResource;
import org.opencms.file.CmsResourceFilter;
import org.opencms.main.CmsException;
import org.opencms.ui.A_CmsUI;
import org.opencms.ui.components.CmsFileTable;
import org.opencms.ui.components.CmsToolBar;
import org.opencms.util.CmsUUID;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.vaadin.data.Item;
import com.vaadin.data.util.HierarchicalContainer;
import com.vaadin.event.ItemClickEvent;
import com.vaadin.event.ItemClickEvent.ItemClickListener;
import com.vaadin.server.FontAwesome;
import com.vaadin.ui.Label;
import com.vaadin.ui.Notification;
import com.vaadin.ui.Tree;
import com.vaadin.ui.Tree.CollapseEvent;
import com.vaadin.ui.Tree.CollapseListener;
import com.vaadin.ui.Tree.ExpandEvent;
import com.vaadin.ui.Tree.ExpandListener;
import com.vaadin.ui.VerticalLayout;

/**
 * The file explorer app.<p>
 */
public class CmsFileExplorer implements I_CmsWorkplaceApp {

    /** The files and folder resource filter. */
    private static final CmsResourceFilter FILES_N_FOLDERS = CmsResourceFilter.ONLY_VISIBLE;

    /** The folders resource filter. */
    private static final CmsResourceFilter FOLDERS = CmsResourceFilter.ONLY_VISIBLE_NO_DELETED.addRequireFolder();

    /** The table containing the contents of the current folder. */
    private CmsFileTable m_fileTable;

    /** The folder tree. */
    private Tree m_fileTree;

    /** The info component. */
    private VerticalLayout m_info;

    /** The info title. */
    private Label m_infoTitle;

    /** The info path. */
    private Label m_infoPath;

    /**
     * Constructor.<p>
     */
    public CmsFileExplorer() {
        m_fileTable = new CmsFileTable();
        m_fileTable.setSizeFull();
        m_fileTree = new Tree();
        m_fileTree.setSizeFull();
        m_fileTree.setItemCaptionPropertyId("resourceName");
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

        m_info = new VerticalLayout();
        m_info.setMargin(false);
        m_info.setSizeFull();
        m_infoTitle = new Label();
        m_infoTitle.addStyleName("h4");
        m_info.addComponent(m_infoTitle);
        m_infoPath = new Label();
        m_infoPath.addStyleName("tiny");
        m_info.addComponent(m_infoPath);
    }

    /**
     * @see org.opencms.ui.apps.I_CmsWorkplaceApp#initUI(org.opencms.ui.apps.I_CmsAppUIContext)
     */
    public void initUI(I_CmsAppUIContext context) {

        context.setMenuContent(m_fileTree);
        context.setAppContent(m_fileTable);
        context.setAppInfo(m_info);
        context.addToolbarButton(CmsToolBar.createButton(FontAwesome.MAGIC));
        context.addToolbarButton(CmsToolBar.createButton(FontAwesome.ARROW_UP));
        context.addToolbarButton(CmsToolBar.createButton(FontAwesome.UPLOAD));
    }

    /**
     * @see org.opencms.ui.apps.I_CmsWorkplaceApp#onStateChange(java.lang.String)
     */
    public void onStateChange(String state) {

        // TODO: evaluate state
        populateFolderTree();
    }

    /**
     * Fills the file table with the resources from the given path.<p>
     *
     * @param sitePath a folder site path
     */
    public void populateFileTable(String sitePath) {

        CmsObject cms = A_CmsUI.getCmsObject();
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
        container.addContainerProperty("resourceName", String.class, null);
        CmsObject cms = A_CmsUI.getCmsObject();
        try {
            CmsResource siteRoot = cms.readResource("/", FOLDERS);
            Item rootItem = container.addItem(siteRoot.getStructureId());
            // use the root path as name for site root folder
            rootItem.getItemProperty("resourceName").setValue(siteRoot.getRootPath());
            List<CmsResource> folderResources = cms.readResources("/", FOLDERS, false);
            for (CmsResource resource : folderResources) {
                Item resourceItem = container.addItem(resource.getStructureId());
                resourceItem.getItemProperty("resourceName").setValue(resource.getName());
                container.setParent(resource.getStructureId(), siteRoot.getStructureId());
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
     * Reads the given folder.<p>
     *
     * @param folderId the folder id
     */
    protected void readFolder(CmsUUID folderId) {

        CmsObject cms = A_CmsUI.getCmsObject();
        try {
            CmsResource folder = cms.readResource(folderId, FOLDERS);
            CmsProperty titleProp = cms.readPropertyObject(folder, CmsPropertyDefinition.PROPERTY_TITLE, false);
            String title = titleProp.isNullProperty() ? "" : titleProp.getValue();
            m_infoTitle.setValue(title);
            m_infoPath.setValue(cms.getSitePath(folder));
            List<CmsResource> folderResources = cms.readResources(cms.getSitePath(folder), FILES_N_FOLDERS, false);
            m_fileTable.fillTable(cms, folderResources);
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
            for (CmsResource resource : folderResources) {
                Item resourceItem = container.getItem(resource.getStructureId());
                if (resourceItem == null) {
                    resourceItem = container.addItem(resource.getStructureId());
                }
                resourceItem.getItemProperty("resourceName").setValue(resource.getName());
                container.setParent(resource.getStructureId(), parentId);
            }
        } catch (CmsException e) {
            // TODO: Auto-generated catch block
            e.printStackTrace();
        }
    }

}
