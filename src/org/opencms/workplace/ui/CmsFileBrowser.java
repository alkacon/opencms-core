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

package org.opencms.workplace.ui;

import org.opencms.file.CmsResource;
import org.opencms.file.CmsResourceFilter;
import org.opencms.main.CmsException;
import org.opencms.main.OpenCms;
import org.opencms.ui.A_CmsCustomComponent;
import org.opencms.util.CmsUUID;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;

import com.vaadin.data.Item;
import com.vaadin.data.util.HierarchicalContainer;
import com.vaadin.event.ItemClickEvent;
import com.vaadin.event.ItemClickEvent.ItemClickListener;
import com.vaadin.ui.HorizontalSplitPanel;
import com.vaadin.ui.Notification;
import com.vaadin.ui.Tree;
import com.vaadin.ui.Tree.CollapseEvent;
import com.vaadin.ui.Tree.CollapseListener;
import com.vaadin.ui.Tree.ExpandEvent;
import com.vaadin.ui.Tree.ExpandListener;

/**
 * File browser component containing both a VFS folder tree and a list of files in a table.<p>
 */
public class CmsFileBrowser extends A_CmsCustomComponent {

    private static final CmsResourceFilter FILES_N_FOLDERS = CmsResourceFilter.ONLY_VISIBLE;

    private static final CmsResourceFilter FOLDERS = CmsResourceFilter.ONLY_VISIBLE_NO_DELETED.addRequireFolder();

    /** The table containing the contents of the current folder. */
    private CmsFileTable m_fileTable = new CmsFileTable();

    /** The folder tree. */
    private Tree m_fileTree;

    public CmsFileBrowser() {

        HorizontalSplitPanel main = new HorizontalSplitPanel();

        m_fileTree = new Tree();
        m_fileTree.setItemCaptionPropertyId("resourceName");
        main.setFirstComponent(m_fileTree);
        m_fileTree.setHeight(100, Unit.PERCENTAGE);
        m_fileTree.setWidth(100, Unit.PERCENTAGE);
        populateFolderTree();
        m_fileTree.addExpandListener(new ExpandListener() {

            public void nodeExpand(ExpandEvent event) {

                readTreeLevel((CmsUUID)event.getItemId());
            }
        });
        m_fileTree.addCollapseListener(new CollapseListener() {

            public void nodeCollapse(CollapseEvent event) {

                clearTreeLevel((CmsUUID)event.getItemId());
            }
        });

        m_fileTree.addItemClickListener(new ItemClickListener() {

            public void itemClick(ItemClickEvent event) {

                readFolder((CmsUUID)event.getItemId());
            }
        });

        main.setSecondComponent(m_fileTable);
        m_fileTable.setHeight(100, Unit.PERCENTAGE);
        m_fileTable.setWidth(100, Unit.PERCENTAGE);

        populateFileTable("/");
        main.setSplitPosition(25, Unit.PERCENTAGE);
        setCompositionRoot(main);
        setHeight(100, Unit.PERCENTAGE);
        setWidth(100, Unit.PERCENTAGE);
    }

    /**
     * Filles the file table with the resources from the given path.<p>
     *
     * @param sitePath a folder site path
     */
    public void populateFileTable(String sitePath) {

        try {
            List<CmsResource> folderResources = getCmsObject().readResources(sitePath, FILES_N_FOLDERS, false);
            m_fileTable.fillTable(getCmsObject(), folderResources);
        } catch (CmsException e) {
            Notification.show(e.getMessage());
        }
    }

    /**
     * Popuplates the folder tree.<p>
     */
    public void populateFolderTree() {

        HierarchicalContainer container = new HierarchicalContainer();
        container.addContainerProperty("resourceName", String.class, null);

        try {
            CmsResource siteRoot = getCmsObject().readResource("/", FOLDERS);
            Item rootItem = container.addItem(siteRoot.getStructureId());
            // use the root path as name for site root folder
            rootItem.getItemProperty("resourceName").setValue(siteRoot.getRootPath());
            List<CmsResource> folderResources = getCmsObject().readResources("/", FOLDERS, false);
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

    protected void clearTreeLevel(CmsUUID parentIt) {

        HierarchicalContainer container = (HierarchicalContainer)m_fileTree.getContainerDataSource();
        // create a new list to avoid concurrent modifications
        Collection<?> children = container.getChildren(parentIt);
        // may be null when monkey clicking
        if (children != null) {
            List<Object> childIds = new ArrayList<Object>(container.getChildren(parentIt));
            for (Object childId : childIds) {
                container.removeItemRecursively(childId);
            }
        }
    }

    protected void readFolder(CmsUUID folderId) {

        try {
            CmsResource folder = getCmsObject().readResource(folderId, FOLDERS);
            List<CmsResource> folderResources = getCmsObject().readResources(
                getCmsObject().getSitePath(folder),
                FILES_N_FOLDERS,
                false);
            m_fileTable.getContainer().removeAllItems();
            m_fileTable.fillTable(getCmsObject(), folderResources);
        } catch (CmsException e) {
            Notification.show(e.getMessage());
            e.printStackTrace();
        }
    }

    protected void readTreeLevel(CmsUUID parentId) {

        HierarchicalContainer container = (HierarchicalContainer)m_fileTree.getContainerDataSource();
        try {
            CmsResource parent = getCmsObject().readResource(parentId, FOLDERS);
            List<CmsResource> folderResources = getCmsObject().readResources(
                getCmsObject().getSitePath(parent),
                FOLDERS,
                false);
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

    private Locale getWorkplaceLocale() {

        return OpenCms.getWorkplaceManager().getWorkplaceLocale(getCmsObject());
    }

}
