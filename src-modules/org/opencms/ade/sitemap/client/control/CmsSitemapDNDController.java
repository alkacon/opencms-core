/*
 * File   : $Source: /alkacon/cvs/opencms/src-modules/org/opencms/ade/sitemap/client/control/Attic/CmsSitemapDNDController.java,v $
 * Date   : $Date: 2010/09/23 08:18:33 $
 * Version: $Revision: 1.3 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (C) 2002 - 2009 Alkacon Software (http://www.alkacon.com)
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

package org.opencms.ade.sitemap.client.control;

import org.opencms.ade.galleries.client.ui.CmsResultListItem;
import org.opencms.ade.sitemap.client.CmsSitemapTreeItem;
import org.opencms.ade.sitemap.client.toolbar.CmsSitemapToolbar;
import org.opencms.ade.sitemap.client.toolbar.CmsToolbarClipboardView.CmsClipboardDeletedItem;
import org.opencms.ade.sitemap.shared.CmsClientSitemapEntry;
import org.opencms.gwt.client.dnd.CmsDNDHandler;
import org.opencms.gwt.client.dnd.I_CmsDNDController;
import org.opencms.gwt.client.dnd.I_CmsDraggable;
import org.opencms.gwt.client.dnd.I_CmsDropTarget;
import org.opencms.gwt.client.ui.tree.CmsTree;
import org.opencms.gwt.client.util.CmsDebugLog;
import org.opencms.gwt.client.util.CmsDomUtil;

import com.google.gwt.dom.client.Style.Display;

/**
 * The sitemap drag and drop controller.<p>
 * 
 * @author Tobias Herrmann
 * 
 * @version $Revision: 1.3 $
 * 
 * @since 8.0.0
 */
public class CmsSitemapDNDController implements I_CmsDNDController {

    /** The sitemap controller instance. */
    private CmsSitemapController m_controller;

    /** The insert position of the draggable. */
    private int m_insertIndex;

    /** The insert path of the draggable. */
    private String m_insertPath;

    /** The original position of the draggable. */
    private int m_originalIndex;

    /** The original path of the draggable. */
    private String m_originalPath;

    /** The sitemap toolbar. */
    private CmsSitemapToolbar m_toolbar;

    /**
     * Constructor.<p>
     * 
     * @param controller the sitemap controller
     * @param toolbar the sitemap toolbar
     */
    public CmsSitemapDNDController(CmsSitemapController controller, CmsSitemapToolbar toolbar) {

        m_controller = controller;
        m_toolbar = toolbar;
    }

    /**
     * @see org.opencms.gwt.client.dnd.I_CmsDNDController#onBeforeDrop(org.opencms.gwt.client.dnd.I_CmsDraggable, org.opencms.gwt.client.dnd.I_CmsDropTarget, org.opencms.gwt.client.dnd.CmsDNDHandler)
     */
    public boolean onBeforeDrop(I_CmsDraggable draggable, I_CmsDropTarget target, CmsDNDHandler handler) {

        if (!(target instanceof CmsTree<?>)) {
            // only dropping onto the tree allowed in sitemap editor
            return false;
        }
        CmsTree<?> tree = (CmsTree<?>)target;
        m_insertIndex = tree.getPlaceholderIndex();
        if (m_insertIndex == -1) {
            return false;
        }
        m_insertPath = tree.getPlaceholderPath();
        return true;
    }

    /**
     * @see org.opencms.gwt.client.dnd.I_CmsDNDController#onDragCancel(org.opencms.gwt.client.dnd.I_CmsDraggable, org.opencms.gwt.client.dnd.I_CmsDropTarget, org.opencms.gwt.client.dnd.CmsDNDHandler)
     */
    public void onDragCancel(I_CmsDraggable draggable, I_CmsDropTarget target, CmsDNDHandler handler) {

        CmsDomUtil.removeDisablingOverlay(draggable.getElement());
    }

    /**
     * @see org.opencms.gwt.client.dnd.I_CmsDNDController#onDragStart(org.opencms.gwt.client.dnd.I_CmsDraggable, org.opencms.gwt.client.dnd.I_CmsDropTarget, org.opencms.gwt.client.dnd.CmsDNDHandler)
     */
    public boolean onDragStart(I_CmsDraggable draggable, I_CmsDropTarget target, CmsDNDHandler handler) {

        // TODO: check lock and modification state
        //        CmsSitemapController controller = CmsSitemapView.getInstance().getController();
        //        boolean cancel = !controller.isDirty();
        //        cancel &= !CmsCoreProvider.get().lockAndCheckModification(
        //            CmsCoreProvider.get().getUri(),
        //            controller.getData().getTimestamp());

        m_insertIndex = -1;
        m_insertPath = null;
        m_originalIndex = -1;
        m_originalPath = null;
        if ((draggable instanceof CmsClipboardDeletedItem) || (draggable instanceof CmsResultListItem)) {
            m_toolbar.onButtonActivation(null);
        } else if (draggable instanceof CmsSitemapTreeItem) {
            CmsSitemapTreeItem treeItem = (CmsSitemapTreeItem)draggable;
            m_originalPath = treeItem.getParentItem().getPath();
            if (treeItem.getParentItem() != null) {
                m_originalIndex = treeItem.getParentItem().getItemPosition(treeItem);
            }
            CmsDomUtil.addDisablingOverlay(draggable.getElement());
        }
        CmsDebugLog.getInstance().printLine("Starting path: " + m_originalPath + ", Index: " + m_originalIndex);
        return true;
    }

    /**
     * @see org.opencms.gwt.client.dnd.I_CmsDNDController#onDrop(org.opencms.gwt.client.dnd.I_CmsDraggable, org.opencms.gwt.client.dnd.I_CmsDropTarget, org.opencms.gwt.client.dnd.CmsDNDHandler)
     */
    public void onDrop(I_CmsDraggable draggable, I_CmsDropTarget target, CmsDNDHandler handler) {

        if (!(target instanceof CmsTree<?>)) {
            // only dropping onto the tree allowed in sitemap
            handler.cancel();
            return;
        }
        if (draggable instanceof CmsClipboardDeletedItem) {
            // reinserting a deleted item
            CmsClientSitemapEntry entry = ((CmsClipboardDeletedItem)draggable).getEntry();
            entry.updateSitePath(m_insertPath + entry.getName() + "/");
            entry.setPosition(m_insertIndex);
            m_controller.create(entry);
        }
        if (draggable instanceof CmsSitemapTreeItem) {
            CmsDomUtil.removeDisablingOverlay(draggable.getElement());
            if (isChangedPosition(draggable, target)) {
                // moving a tree entry around
                CmsClientSitemapEntry entry = m_controller.getEntry(((CmsSitemapTreeItem)draggable).getSitePath());
                CmsDebugLog.getInstance().printLine(
                    "inserting at " + m_insertPath + entry.getName() + "/ and index " + m_insertIndex);
                m_controller.move(entry, m_insertPath + entry.getName() + "/", m_insertIndex);
            } else {
                CmsDebugLog.getInstance().printLine("Nothing changed");
            }
        }
        if (draggable instanceof CmsResultListItem) {
            CmsResultListItem galleryItem = (CmsResultListItem)draggable;

            CmsClientSitemapEntry entry = new CmsClientSitemapEntry();
            entry.setName(galleryItem.getId());
            entry.setSitePath(m_insertPath + galleryItem.getId() + "/");
            entry.setTitle(galleryItem.getListItemWidget().getTitleLabel());
            entry.setVfsPath(galleryItem.getVfsPath());
            entry.setPosition(m_insertIndex);
            m_controller.create(entry);
        }

    }

    /**
     * @see org.opencms.gwt.client.dnd.I_CmsDNDController#onPositionedPlaceholder(org.opencms.gwt.client.dnd.I_CmsDraggable, org.opencms.gwt.client.dnd.I_CmsDropTarget, org.opencms.gwt.client.dnd.CmsDNDHandler)
     */
    public void onPositionedPlaceholder(I_CmsDraggable draggable, I_CmsDropTarget target, CmsDNDHandler handler) {

        if (draggable instanceof CmsSitemapTreeItem) {
            if (!isChangedPosition(draggable, target)) {
                draggable.getElement().getStyle().setDisplay(Display.NONE);
            } else {
                draggable.getElement().getStyle().setDisplay(Display.BLOCK);
            }
        }
    }

    /**
     * @see org.opencms.gwt.client.dnd.I_CmsDNDController#onTargetEnter(org.opencms.gwt.client.dnd.I_CmsDraggable, org.opencms.gwt.client.dnd.I_CmsDropTarget, org.opencms.gwt.client.dnd.CmsDNDHandler)
     */
    public boolean onTargetEnter(I_CmsDraggable draggable, I_CmsDropTarget target, CmsDNDHandler handler) {

        return true;
    }

    /**
     * @see org.opencms.gwt.client.dnd.I_CmsDNDController#onTargetLeave(org.opencms.gwt.client.dnd.I_CmsDraggable, org.opencms.gwt.client.dnd.I_CmsDropTarget, org.opencms.gwt.client.dnd.CmsDNDHandler)
     */
    public void onTargetLeave(I_CmsDraggable draggable, I_CmsDropTarget target, CmsDNDHandler handler) {

        if (target instanceof CmsTree<?>) {
            ((CmsTree<?>)target).cancelOpenTimer();
            if (draggable instanceof CmsSitemapTreeItem) {
                draggable.getElement().getStyle().setDisplay(Display.BLOCK);
            }
        }
    }

    /**
     * Checks whether the current placeholder position represents a change to the original draggable position within the tree.<p>
     * 
     * @param draggable the draggable
     * @param target the current drop target
     * 
     * @return <code>true</code> if the position changed
     */
    private boolean isChangedPosition(I_CmsDraggable draggable, I_CmsDropTarget target) {

        // if draggable is not a sitemap item, any valid position is a changed position
        if (!((draggable instanceof CmsSitemapTreeItem) && (target instanceof CmsTree<?>))) {
            return true;
        }
        // if the the path differs, the position has changed
        String placeholderPath = ((CmsTree<?>)target).getPlaceholderPath();
        if (!((placeholderPath != null) && placeholderPath.equals(m_originalPath))) {
            return true;
        }
        // if the new index is not next to the old one, the position has changed
        if (!((target.getPlaceholderIndex() == m_originalIndex + 1) || (target.getPlaceholderIndex() == m_originalIndex))) {
            return true;
        }
        return false;
    }

}
