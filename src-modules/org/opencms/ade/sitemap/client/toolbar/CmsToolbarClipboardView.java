/*
 * File   : $Source: /alkacon/cvs/opencms/src-modules/org/opencms/ade/sitemap/client/toolbar/Attic/CmsToolbarClipboardView.java,v $
 * Date   : $Date: 2010/11/29 08:25:32 $
 * Version: $Revision: 1.6 $
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

package org.opencms.ade.sitemap.client.toolbar;

import org.opencms.ade.sitemap.client.CmsSitemapView;
import org.opencms.ade.sitemap.client.Messages;
import org.opencms.ade.sitemap.client.control.CmsSitemapChangeEvent;
import org.opencms.ade.sitemap.client.control.CmsSitemapController;
import org.opencms.ade.sitemap.client.control.I_CmsSitemapChangeHandler;
import org.opencms.ade.sitemap.client.ui.css.I_CmsImageBundle;
import org.opencms.ade.sitemap.shared.CmsClientSitemapEntry;
import org.opencms.gwt.client.ui.CmsList;
import org.opencms.gwt.client.ui.CmsListItem;
import org.opencms.gwt.client.ui.CmsListItemWidget;
import org.opencms.gwt.client.ui.CmsPushButton;
import org.opencms.gwt.client.util.CmsDomUtil;
import org.opencms.gwt.shared.CmsListInfoBean;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;

/**
 * Sitemap toolbar clipboard view.<p>
 * 
 * @author Michael Moossen
 * 
 * @version $Revision: 1.6 $
 * 
 * @since 8.0.0
 */
public class CmsToolbarClipboardView {

    /**
     * The deleted item widget.<p>
     */
    public final class CmsClipboardDeletedItem extends CmsListItem {

        /** The sitemap entry. */
        private final CmsClientSitemapEntry m_entry;

        /**
         * Constructor.<p>
         * 
         * @param widget the widget
         * @param entry the entry
         */
        public CmsClipboardDeletedItem(CmsListItemWidget widget, CmsClientSitemapEntry entry) {

            super(widget);
            m_entry = entry;
            setId(m_entry.getName());
        }

        /**
         * Returns the underlying sitemap entry.<p>
         * 
         * @return the sitemap entry
         */
        public CmsClientSitemapEntry getEntry() {

            return m_entry;
        }
    }

    /** The clipboard toolbar button. */
    protected CmsToolbarClipboardButton m_clipboardButton;

    /** The deleted list. */
    private CmsList<CmsListItem> m_deleted;

    /** The modified list. */
    private CmsList<CmsListItem> m_modified;

    /**
     * Constructor.<p>
     * 
     * @param clipboardButton the clipboard button
     * @param controller the sitemap controller 
     */
    public CmsToolbarClipboardView(CmsToolbarClipboardButton clipboardButton, final CmsSitemapController controller) {

        m_modified = new CmsList<CmsListItem>();
        for (CmsClientSitemapEntry entry : controller.getData().getClipboardData().getModifications()) {
            m_modified.addItem(createModifiedItem(entry));
        }

        m_deleted = new CmsList<CmsListItem>();
        // allow dragging to the sitemap tree
        m_deleted.setDNDHandler(CmsSitemapView.getInstance().getTree().getDnDHandler());
        // prevent dragging to the deleted list
        m_deleted.setDropEnabled(false);

        for (CmsClientSitemapEntry entry : controller.getData().getClipboardData().getDeletions()) {
            m_deleted.addItem(createDeletedItem(entry));
        }

        m_clipboardButton = clipboardButton;

        controller.addChangeHandler(new I_CmsSitemapChangeHandler() {

            /**
             * @see org.opencms.ade.sitemap.client.control.I_CmsSitemapChangeHandler#onChange(org.opencms.ade.sitemap.client.control.CmsSitemapChangeEvent)
             */
            public void onChange(CmsSitemapChangeEvent changeEvent) {

                changeEvent.getChange().applyToClipboardView(CmsToolbarClipboardView.this);
            }
        });
    }

    /**
     * Adds an deleted entry.<p>
     * 
     * @param entry the deleted entry
     */
    public void addDeleted(CmsClientSitemapEntry entry) {

        removeDeleted(entry.getId().toString());
        removeModified(entry.getId().toString());
        getDeleted().insertItem(createDeletedItem(entry), 0);
    }

    /**
     * Adds a modified entry.<p>
     * 
     * @param entry the entry
     * @param previousPath the previous path
     */
    public void addModified(CmsClientSitemapEntry entry, String previousPath) {

        removeDeleted(entry.getId().toString());
        removeModified(entry.getId().toString());
        getModified().insertItem(createModifiedItem(entry), 0);
    }

    /**
     * Creates a new deleted list item.<p>
     * 
     * @param entry the sitemap entry
     * 
     * @return the new created (still orphan) list item 
     */
    public CmsListItem createDeletedItem(final CmsClientSitemapEntry entry) {

        CmsListInfoBean infoBean = new CmsListInfoBean();
        infoBean.setTitle(entry.getTitle());
        infoBean.setSubTitle(entry.getSitePath());
        infoBean.addAdditionalInfo(Messages.get().key(Messages.GUI_NAME_0), entry.getName());
        infoBean.addAdditionalInfo(Messages.get().key(Messages.GUI_VFS_PATH_0), entry.getVfsPath());
        final CmsListItemWidget itemWidget = new CmsListItemWidget(infoBean);
        CmsListItem listItem = new CmsClipboardDeletedItem(itemWidget, entry);
        listItem.initMoveHandle(CmsSitemapView.getInstance().getTree().getDnDHandler());
        listItem.setId(entry.getId().toString());
        return listItem;
    }

    /**
     * Creates a new modified list item.<p>
     * 
     * @param entry the sitemap entry
     * 
     * @return the new created (still orphan) list item 
     */
    public CmsListItem createModifiedItem(final CmsClientSitemapEntry entry) {

        CmsListInfoBean infoBean = new CmsListInfoBean();
        infoBean.setTitle(entry.getTitle());
        infoBean.setSubTitle(entry.getSitePath());
        infoBean.addAdditionalInfo(Messages.get().key(Messages.GUI_NAME_0), entry.getName());
        infoBean.addAdditionalInfo(Messages.get().key(Messages.GUI_VFS_PATH_0), entry.getVfsPath());
        CmsListItemWidget itemWidget = new CmsListItemWidget(infoBean);

        final CmsListItem listItem = new CmsListItem(itemWidget);

        CmsPushButton button = new CmsPushButton();
        button.setImageClass(I_CmsImageBundle.INSTANCE.buttonCss().hoverbarGoto());
        button.setTitle(Messages.get().key(Messages.GUI_HOVERBAR_GOTO_0));
        button.setShowBorder(false);
        button.addClickHandler(new ClickHandler() {

            /**
             * @see com.google.gwt.event.dom.client.ClickHandler#onClick(com.google.gwt.event.dom.client.ClickEvent)
             */
            public void onClick(ClickEvent event) {

                // TODO: hide menu
                // TODO: check if entry is intern or extern
                boolean intern = true;
                if (intern) {
                    //                    final CmsSitemapTreeItem treeItem = CmsSitemapView.getInstance().getTreeItem(entry.getSitePath());
                    //                    // TODO: it could be that the item is not yet loaded, so ensure that it is load
                    //                    CmsSitemapView.getInstance().ensureVisible(treeItem);
                    CmsDomUtil.ensureMouseOut(listItem.getElement());
                    m_clipboardButton.closeMenu();
                    CmsSitemapView.getInstance().highlightPath(entry.getSitePath());
                } else {
                    // TODO: get the file to open
                    // TODO: jump to the right file with the right parameter
                }
            }
        });
        itemWidget.addButton(button);
        listItem.setId(entry.getId().toString());
        return listItem;
    }

    /**
     * Returns the deleted list.<p>
     *
     * @return the deleted list
     */
    public CmsList<CmsListItem> getDeleted() {

        return m_deleted;
    }

    /**
     * Returns the modified list.<p>
     *
     * @return the modified list
     */
    public CmsList<CmsListItem> getModified() {

        return m_modified;
    }

    /**
     * Removes an entry from the deleted list.<p>
     * 
     * @param sitePath the former sitemap path of the entry
     */
    private void removeDeleted(String sitePath) {

        CmsListItem item = getDeleted().getItem(sitePath);
        if (item != null) {
            // remove
            getDeleted().removeItem(item);
        }
    }

    /**
     * Removes an entry from the modified list.<p>
     * 
     * @param sitePath the former sitemap path of the entry
     */
    private void removeModified(String sitePath) {

        CmsListItem item = getModified().getItem(sitePath);
        if (item != null) {
            // remove
            getModified().removeItem(item);
        }
    }
}
