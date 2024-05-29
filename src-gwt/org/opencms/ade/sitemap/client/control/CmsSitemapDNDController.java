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

package org.opencms.ade.sitemap.client.control;

import org.opencms.ade.detailpage.CmsDetailPageInfo;
import org.opencms.ade.sitemap.client.CmsSitemapTreeItem;
import org.opencms.ade.sitemap.client.CmsSitemapView;
import org.opencms.ade.sitemap.client.toolbar.CmsSitemapToolbar;
import org.opencms.ade.sitemap.client.ui.CmsCreatableListItem;
import org.opencms.ade.sitemap.client.ui.css.I_CmsSitemapLayoutBundle;
import org.opencms.ade.sitemap.shared.CmsClientSitemapEntry;
import org.opencms.ade.sitemap.shared.CmsClientSitemapEntry.EntryType;
import org.opencms.ade.sitemap.shared.CmsNewResourceInfo;
import org.opencms.gwt.client.dnd.CmsDNDHandler;
import org.opencms.gwt.client.dnd.CmsDNDHandler.Orientation;
import org.opencms.gwt.client.dnd.I_CmsDNDController;
import org.opencms.gwt.client.dnd.I_CmsDraggable;
import org.opencms.gwt.client.dnd.I_CmsDropTarget;
import org.opencms.gwt.client.property.CmsReloadMode;
import org.opencms.gwt.client.ui.tree.CmsTree;
import org.opencms.gwt.client.util.CmsDebugLog;
import org.opencms.gwt.client.util.CmsDomUtil;
import org.opencms.gwt.client.util.CmsDomUtil.Tag;
import org.opencms.gwt.client.util.I_CmsSimpleCallback;
import org.opencms.gwt.shared.property.CmsClientProperty;
import org.opencms.gwt.shared.property.CmsPropertyModification;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import com.google.common.collect.Maps;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.ScheduledCommand;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.Style.Unit;

/**
 * The sitemap drag and drop controller.<p>
 *
 * @since 8.0.0
 */
public class CmsSitemapDNDController implements I_CmsDNDController {

    /** The sitemap controller instance. */
    CmsSitemapController m_controller;

    /** The insert position of the draggable. */
    int m_insertIndex;

    /** The insert path of the draggable. */
    String m_insertPath;

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
     * @see org.opencms.gwt.client.dnd.I_CmsDNDController#onAnimationStart(org.opencms.gwt.client.dnd.I_CmsDraggable, org.opencms.gwt.client.dnd.I_CmsDropTarget, org.opencms.gwt.client.dnd.CmsDNDHandler)
     */
    public void onAnimationStart(I_CmsDraggable draggable, I_CmsDropTarget target, CmsDNDHandler handler) {

        // nothing to do
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
        m_insertPath = tree.getPlaceholderPath();
        m_insertIndex = tree.getPlaceholderIndex();
        if (m_insertPath.equals(m_originalPath) && (m_insertIndex > m_originalIndex)) {
            // new position has the same path and is below the original position, adjust insert index
            m_insertIndex -= 1;
            if (m_insertIndex == m_originalIndex) {
                return false;
            }
        }
        if (m_insertIndex == -1) {
            return false;
        }
        return true;
    }

    /**
     * @see org.opencms.gwt.client.dnd.I_CmsDNDController#onDragCancel(org.opencms.gwt.client.dnd.I_CmsDraggable, org.opencms.gwt.client.dnd.I_CmsDropTarget, org.opencms.gwt.client.dnd.CmsDNDHandler)
     */
    public void onDragCancel(I_CmsDraggable draggable, I_CmsDropTarget target, CmsDNDHandler handler) {

        if (draggable instanceof CmsSitemapTreeItem) {
            ((CmsSitemapTreeItem)draggable).resetEntry();
        }
        Scheduler.get().scheduleDeferred(new ScheduledCommand() {

            /**
             * @see com.google.gwt.user.client.Command#execute()
             */
            public void execute() {

                CmsSitemapView.getInstance().getTree().closeAllEmpty();
            }
        });
    }

    /**
     * @see org.opencms.gwt.client.dnd.I_CmsDNDController#onDragStart(org.opencms.gwt.client.dnd.I_CmsDraggable, org.opencms.gwt.client.dnd.I_CmsDropTarget, org.opencms.gwt.client.dnd.CmsDNDHandler)
     */
    public boolean onDragStart(I_CmsDraggable draggable, I_CmsDropTarget target, CmsDNDHandler handler) {

        handler.setOrientation(Orientation.VERTICAL);
        hideItemContent(handler.getPlaceholder());
        handler.getDragHelper().getStyle().setOpacity(0.6);
        m_insertIndex = -1;
        m_insertPath = null;
        m_originalIndex = -1;
        m_originalPath = null;
        if (draggable instanceof CmsCreatableListItem) {
            m_toolbar.onButtonActivation(null);

            // fixing placeholder indent not being present in non tree items
            List<Element> elements = CmsDomUtil.getElementsByClass(
                org.opencms.gwt.client.ui.css.I_CmsLayoutBundle.INSTANCE.floatDecoratedPanelCss().primary(),
                Tag.div,
                handler.getPlaceholder());
            if ((elements != null) && (elements.size() > 0)) {
                elements.get(0).getStyle().setMarginLeft(16, Unit.PX);
            }
        } else if (draggable instanceof CmsSitemapTreeItem) {
            CmsSitemapTreeItem treeItem = (CmsSitemapTreeItem)draggable;
            m_originalPath = treeItem.getParentItem().getPath();
            if (treeItem.getParentItem() != null) {
                m_originalIndex = treeItem.getParentItem().getItemPosition(treeItem);
            }
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
        CmsClientSitemapEntry parent = CmsSitemapView.getInstance().getController().getEntry(m_insertPath);
        if (draggable instanceof CmsSitemapTreeItem) {
            handleDropSitemapEntry((CmsSitemapTreeItem)draggable, target, parent);
        }
        if (draggable instanceof CmsCreatableListItem) {
            handleDropNewEntry((CmsCreatableListItem)draggable, parent);
        }
        Scheduler.get().scheduleDeferred(new ScheduledCommand() {

            /**
             * @see com.google.gwt.user.client.Command#execute()
             */
            public void execute() {

                CmsSitemapView.getInstance().getTree().closeAllEmpty();
            }
        });
    }

    /**
     * @see org.opencms.gwt.client.dnd.I_CmsDNDController#onPositionedPlaceholder(org.opencms.gwt.client.dnd.I_CmsDraggable, org.opencms.gwt.client.dnd.I_CmsDropTarget, org.opencms.gwt.client.dnd.CmsDNDHandler)
     */
    public void onPositionedPlaceholder(I_CmsDraggable draggable, I_CmsDropTarget target, CmsDNDHandler handler) {

        if (draggable instanceof CmsSitemapTreeItem) {
            adjustOriginalPositionIndicator((CmsSitemapTreeItem)draggable, target, handler);
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
        }
    }

    /**
     * Checks whether the current placeholder position represents a change to the original draggable position within the tree.<p>
     *
     * @param draggable the draggable
     * @param target the current drop target
     * @param strict if <code>false</code> only the parent path is considered, the index position will be ignored
     *
     * @return <code>true</code> if the position changed
     */
    boolean isChangedPosition(I_CmsDraggable draggable, I_CmsDropTarget target, boolean strict) {

        // if draggable is not a sitemap item, any valid position is a changed position
        if (!((draggable instanceof CmsSitemapTreeItem) && (target instanceof CmsTree<?>))) {
            return true;
        }

        String placeholderPath = ((CmsTree<?>)target).getPlaceholderPath();
        if ((placeholderPath == null) && !strict) {
            // first positioning, path has not changed yet
            return false;
        }
        // if the the path differs, the position has changed
        if ((m_originalPath == null) || !m_originalPath.equals(placeholderPath)) {
            return true;
        }
        // if the new index is not next to the old one, the position has changed
        if (!((target.getPlaceholderIndex() == (m_originalIndex + 1))
            || (target.getPlaceholderIndex() == m_originalIndex)) && strict) {
            return true;
        }
        return false;
    }

    /**
     * Adjust the original position indicator by styling the draggable element for this purpose.<p>
     *
     * @param draggable the draggable
     * @param target the current drop target
     * @param handler the drag and drop handler
     */
    private void adjustOriginalPositionIndicator(
        CmsSitemapTreeItem draggable,
        I_CmsDropTarget target,
        CmsDNDHandler handler) {

        if (!isChangedPosition(draggable, target, true)) {
            draggable.getElement().addClassName(I_CmsSitemapLayoutBundle.INSTANCE.sitemapItemCss().markUnchanged());
            List<Element> itemWidget = CmsDomUtil.getElementsByClass(
                org.opencms.gwt.client.ui.css.I_CmsLayoutBundle.INSTANCE.listItemWidgetCss().itemContainer(),
                handler.getPlaceholder());
            if ((itemWidget != null) && (itemWidget.size() > 0)) {
                CmsDomUtil.addDisablingOverlay(itemWidget.get(0));
            }
        } else {
            draggable.getElement().removeClassName(I_CmsSitemapLayoutBundle.INSTANCE.sitemapItemCss().markUnchanged());
            CmsDomUtil.removeDisablingOverlay(handler.getPlaceholder());
        }
    }

    /**
     * Handles a dropped detail page.<p>
     *
     * @param createItem the detail page which was dropped into the sitemap
     * @param parent the parent sitemap entry
     */
    private void handleDropNewEntry(CmsCreatableListItem createItem, final CmsClientSitemapEntry parent) {

        final CmsNewResourceInfo typeInfo = createItem.getResourceTypeInfo();
        final CmsClientSitemapEntry entry = new CmsClientSitemapEntry();
        entry.setNew(true);
        entry.setVfsPath(null);
        entry.setPosition(m_insertIndex);
        entry.setInNavigation(true);
        Map<String, CmsClientProperty> defaultFileProps = Maps.newHashMap();
        entry.setDefaultFileProperties(defaultFileProps);
        String name;
        final boolean appendSlash;
        switch (createItem.getNewEntryType()) {
            case detailpage:
                name = CmsDetailPageInfo.removeFunctionPrefix(typeInfo.getResourceType());
                appendSlash = true;
                entry.setDetailpageTypeName(typeInfo.getResourceType());
                entry.setVfsModeIcon(typeInfo.getBigIconClasses());
                if (typeInfo.isFunction()) {

                    CmsClientProperty titleProp = new CmsClientProperty(
                        CmsClientProperty.PROPERTY_TITLE,
                        typeInfo.getTitle(),
                        null);
                    CmsClientProperty navtextProp = new CmsClientProperty(
                        CmsClientProperty.PROPERTY_NAVTEXT,
                        typeInfo.getTitle(),
                        null);
                    entry.getOwnProperties().put(titleProp.getName(), titleProp);
                    entry.getDefaultFileProperties().put(titleProp.getName(), titleProp);
                    entry.getOwnProperties().put(navtextProp.getName(), navtextProp);
                }
                entry.setResourceTypeName("folder");
                break;
            case redirect:
                name = typeInfo.getResourceType();
                entry.setEntryType(EntryType.redirect);
                entry.setVfsModeIcon(typeInfo.getBigIconClasses());
                entry.setResourceTypeName(typeInfo.getResourceType());
                appendSlash = false;
                break;
            default:
                name = CmsSitemapController.NEW_ENTRY_NAME;
                appendSlash = true;
                entry.setResourceTypeName("folder");
                entry.setVfsModeIcon(typeInfo.getBigIconClasses());
        }

        m_controller.ensureUniqueName(parent, name, new I_CmsSimpleCallback<String>() {

            public void execute(String uniqueName) {

                entry.setName(uniqueName);
                String sitepath = m_insertPath + uniqueName;
                if (appendSlash) {
                    sitepath += "/";
                }
                entry.setSitePath(sitepath);
                m_controller.create(
                    entry,
                    parent.getId(),
                    typeInfo.getId(),
                    typeInfo.getCopyResourceId(),
                    typeInfo.getCreateParameter(),
                    false);
            }
        });

    }

    /**
     * Handles the drop for a sitemap item which was dragged to a different position.<p>
     *
     * @param sitemapEntry the dropped item
     * @param target the drop target
     * @param parent the parent sitemap entry
     */
    private void handleDropSitemapEntry(
        final CmsSitemapTreeItem sitemapEntry,
        final I_CmsDropTarget target,
        CmsClientSitemapEntry parent) {

        if (isChangedPosition(sitemapEntry, target, true)) {
            // moving a tree entry around
            final CmsClientSitemapEntry entry = sitemapEntry.getSitemapEntry();
            m_controller.ensureUniqueName(parent, entry.getName(), new I_CmsSimpleCallback<String>() {

                public void execute(String uniqueName) {

                    String suffix = "";
                    if ((sitemapEntry.getSitePath() != null) && sitemapEntry.getSitePath().endsWith("/")) {
                        suffix = "/";
                    }
                    if (!uniqueName.equals(entry.getName()) && isChangedPosition(sitemapEntry, target, false)) {
                        m_controller.editAndChangeName(
                            entry,
                            uniqueName,
                            Collections.<CmsPropertyModification> emptyList(),
                            entry.isNew(),
                            CmsReloadMode.none);
                        m_controller.move(entry, m_insertPath + uniqueName + suffix, m_insertIndex);
                    } else {
                        m_controller.move(entry, m_insertPath + entry.getName() + suffix, m_insertIndex);
                    }
                }
            });
        } else {
            sitemapEntry.resetEntry();
        }
    }

    /**
     * Hides the content of list items by setting a specific css class.<p>
     *
     * @param element the list item element
     */
    private void hideItemContent(Element element) {

        List<Element> itemWidget = CmsDomUtil.getElementsByClass(
            org.opencms.gwt.client.ui.css.I_CmsLayoutBundle.INSTANCE.listItemWidgetCss().itemContainer(),
            element);
        if ((itemWidget != null) && (itemWidget.size() > 0)) {
            itemWidget.get(0).addClassName(I_CmsSitemapLayoutBundle.INSTANCE.sitemapItemCss().contentHide());
        }
    }
}
