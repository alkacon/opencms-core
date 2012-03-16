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

package org.opencms.ade.sitemap.client.toolbar;

import org.opencms.ade.sitemap.client.CmsSitemapView;
import org.opencms.ade.sitemap.client.Messages;
import org.opencms.ade.sitemap.client.control.CmsSitemapChangeEvent;
import org.opencms.ade.sitemap.client.control.CmsSitemapController;
import org.opencms.ade.sitemap.client.control.I_CmsSitemapChangeHandler;
import org.opencms.ade.sitemap.client.ui.css.I_CmsImageBundle;
import org.opencms.ade.sitemap.shared.CmsClientSitemapEntry;
import org.opencms.ade.sitemap.shared.CmsSitemapClipboardData;
import org.opencms.gwt.client.ui.CmsList;
import org.opencms.gwt.client.ui.CmsListItem;
import org.opencms.gwt.client.ui.CmsListItemWidget;
import org.opencms.gwt.client.ui.CmsPushButton;
import org.opencms.gwt.client.ui.I_CmsButton.ButtonStyle;
import org.opencms.gwt.client.util.CmsDomUtil;
import org.opencms.gwt.shared.CmsListInfoBean;
import org.opencms.gwt.shared.CmsListInfoBean.StateIcon;
import org.opencms.util.CmsStringUtil;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;

/**
 * Sitemap toolbar clipboard view.<p>
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

        m_clipboardButton = clipboardButton;
        m_modified = new CmsList<CmsListItem>();
        m_deleted = new CmsList<CmsListItem>();
        updateContent(controller.getData().getClipboardData());
        controller.addChangeHandler(new I_CmsSitemapChangeHandler() {

            /**
             * @see org.opencms.ade.sitemap.client.control.I_CmsSitemapChangeHandler#onChange(org.opencms.ade.sitemap.client.control.CmsSitemapChangeEvent)
             */
            public void onChange(CmsSitemapChangeEvent changeEvent) {

                updateContent(changeEvent.getChange().getClipBoardData());
            }
        });
    }

    /**
     * Adds an deleted entry.<p>
     * 
     * @param entry the deleted entry
     */
    public void addDeleted(CmsClientSitemapEntry entry) {

        if (!(entry.isNew() && CmsStringUtil.isEmptyOrWhitespaceOnly(entry.getVfsPath()))) {
            removeDeleted(entry.getId().toString());
            getDeleted().insertItem(createDeletedItem(entry), 0);
        }
        removeModified(entry.getId().toString());
        m_clipboardButton.enableClearDeleted(true);
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
        m_clipboardButton.enableClearModified(true);
    }

    /**
     * Clears the deleted list.<p>
     */
    public void clearDeleted() {

        m_deleted.clearList();
        m_clipboardButton.enableClearDeleted(false);
    }

    /**
     * Clears the modified list.<p>
     */
    public void clearModified() {

        m_modified.clearList();
        m_clipboardButton.enableClearModified(false);
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
        CmsPushButton button = new CmsPushButton();
        button.setImageClass(I_CmsImageBundle.INSTANCE.buttonCss().toolbarUndo());
        button.setTitle(Messages.get().key(Messages.GUI_HOVERBAR_UNDELETE_0));
        button.setButtonStyle(ButtonStyle.TRANSPARENT, null);
        button.addClickHandler(new ClickHandler() {

            public void onClick(ClickEvent event) {

                CmsDomUtil.ensureMouseOut(itemWidget.getElement());
                m_clipboardButton.closeMenu();
                CmsSitemapView.getInstance().getController().undelete(entry.getId(), entry.getSitePath());
            }

        });
        itemWidget.addButton(button);
        itemWidget.setStateIcon(StateIcon.standard);
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
        button.setButtonStyle(ButtonStyle.TRANSPARENT, null);
        button.addClickHandler(new ClickHandler() {

            /**
             * @see com.google.gwt.event.dom.client.ClickHandler#onClick(com.google.gwt.event.dom.client.ClickEvent)
             */
            public void onClick(ClickEvent event) {

                // TODO: check if entry is intern or extern
                boolean intern = true;
                if (intern) {
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
        itemWidget.setStateIcon(StateIcon.standard);
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
     * @param entryId the entry id
     */
    public void removeDeleted(String entryId) {

        CmsListItem item = getDeleted().getItem(entryId);
        if (item != null) {
            // remove
            getDeleted().removeItem(item);
        }
        if (getDeleted().getWidgetCount() == 0) {
            m_clipboardButton.enableClearDeleted(false);
        }
    }

    /**
     * Removes an entry from the modified list.<p>
     * 
     * @param entryId the entry id
     */
    public void removeModified(String entryId) {

        CmsListItem item = getModified().getItem(entryId);
        if (item != null) {
            // remove
            getModified().removeItem(item);
        }
        if (getModified().getWidgetCount() == 0) {
            m_clipboardButton.enableClearModified(false);
        }
    }

    /**
     * Updates the clip board content.<p>
     * 
     * @param data the clip board data
     */
    protected void updateContent(CmsSitemapClipboardData data) {

        if (data == null) {
            return;
        }
        m_modified.clearList();
        boolean hasElements = false;
        for (CmsClientSitemapEntry entry : data.getModifications().values()) {
            m_modified.insertItem(createModifiedItem(entry), 0);
            hasElements = true;
        }
        m_clipboardButton.enableClearModified(hasElements);
        m_deleted.clearList();
        hasElements = false;
        for (CmsClientSitemapEntry entry : data.getDeletions().values()) {
            m_deleted.insertItem(createDeletedItem(entry), 0);
            hasElements = true;
        }
        m_clipboardButton.enableClearDeleted(hasElements);
    }
}
