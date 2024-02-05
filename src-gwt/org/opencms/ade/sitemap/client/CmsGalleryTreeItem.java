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

package org.opencms.ade.sitemap.client;

import org.opencms.ade.galleries.client.ui.CmsGalleryPopup;
import org.opencms.ade.galleries.shared.CmsGalleryConfiguration;
import org.opencms.ade.galleries.shared.CmsGalleryTabConfiguration;
import org.opencms.ade.galleries.shared.I_CmsGalleryProviderConstants.GalleryMode;
import org.opencms.ade.sitemap.client.control.CmsSitemapController;
import org.opencms.ade.sitemap.client.ui.css.I_CmsSitemapLayoutBundle;
import org.opencms.ade.sitemap.shared.CmsClientSitemapEntry;
import org.opencms.ade.sitemap.shared.CmsGalleryFolderEntry;
import org.opencms.ade.sitemap.shared.CmsGalleryType;
import org.opencms.file.CmsResource;
import org.opencms.gwt.client.property.CmsReloadMode;
import org.opencms.gwt.client.ui.CmsAlertDialog;
import org.opencms.gwt.client.ui.CmsListItemWidget;
import org.opencms.gwt.client.ui.CmsListItemWidget.I_CmsTitleEditHandler;
import org.opencms.gwt.client.ui.input.CmsLabel;
import org.opencms.gwt.client.ui.tree.CmsTreeItem;
import org.opencms.gwt.shared.CmsListInfoBean;
import org.opencms.gwt.shared.property.CmsClientProperty;
import org.opencms.gwt.shared.property.CmsPropertyModification;
import org.opencms.util.CmsStringUtil;
import org.opencms.util.CmsUUID;

import java.util.ArrayList;
import java.util.List;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;

/**
 * Tree item for the gallery view.<p>
 */
public class CmsGalleryTreeItem extends CmsTreeItem {

    /**
     * List item widget that displays additional infos dynamically.<p>
     */
    protected class CmsGalleryListItemWidget extends CmsListItemWidget {

        /**
         * Constructor.<p>
         *
         * @param infoBean the data to display
         */
        public CmsGalleryListItemWidget(CmsListInfoBean infoBean) {

            super(infoBean);
            ensureOpenCloseAdditionalInfo();
        }

        /**
         * @see org.opencms.gwt.client.ui.CmsListItemWidget#setAdditionalInfoVisible(boolean)
         */
        @Override
        public void setAdditionalInfoVisible(boolean visible) {

            if (visible && !m_openClose.isDown()) {
                CmsClientSitemapEntry entry = CmsSitemapView.getInstance().getController().getEntryById(getEntryId());
                if (entry != null) {
                    initAdditionalInfo(CmsSitemapTreeItem.getInfoBean(entry, false));
                } else {
                    return;
                }
            }
            super.setAdditionalInfoVisible(visible);
        }

    }

    /** The folder entry id. */
    private CmsUUID m_entryId;

    /**
     * Constructor.<p>
     *
     * @param galleryFolder the gallery folder
     */
    public CmsGalleryTreeItem(CmsGalleryFolderEntry galleryFolder) {

        super(true);
        initContent(createListWidget(galleryFolder));
        m_entryId = galleryFolder.getStructureId();
    }

    /**
     * Constructor.<p>
     *
     * @param galleryType the gallery type
     */
    public CmsGalleryTreeItem(CmsGalleryType galleryType) {

        super(true);
        CmsListItemWidget itemWidget = createListWidget(galleryType);
        initContent(itemWidget);
        itemWidget.addIconClickHandler(new ClickHandler() {

            public void onClick(ClickEvent event) {

                setOpen(!isOpen());
            }
        });
    }

    /**
     * Creates the list item widget for the given type.<p>
     *
     * @param galleryType the gallery type
     *
     * @return the list item widget
     */
    public static CmsListItemWidget createListWidget(CmsGalleryType galleryType) {

        CmsListItemWidget result = new CmsListItemWidget(galleryType);
        return result;
    }

    /**
     * Returns the folder entry id.<p>
     *
     * @return the folder entry id
     */
    public CmsUUID getEntryId() {

        return m_entryId;
    }

    /**
     * Returns the site path.<p>
     *
     * @return the site path
     */
    public String getSitePath() {

        // the site path is displayed as the sub title
        return getListItemWidget().getSubtitleLabel();
    }

    /**
     * Updates the site path info.<p>
     *
     * @param sitePath the new site path
     */
    public void updateSitePath(String sitePath) {

        String oldPath = getSitePath();
        getListItemWidget().setSubtitleLabel(sitePath);
        for (Widget child : getChildren()) {
            ((CmsGalleryTreeItem)child).updateParentPath(sitePath, oldPath);
        }
    }

    /**
     * Shows the gallery dialog for the given entry.<p>
     *
     * @param entry the gallery folder entry
     */
    protected void showGallery(CmsClientSitemapEntry entry) {

        CmsGalleryConfiguration configuration = new CmsGalleryConfiguration();
        List<String> typeNames = CmsSitemapView.getInstance().getController().getGalleryType(
            Integer.valueOf(entry.getResourceTypeId())).getContentTypeNames();
        configuration.setSearchTypes(typeNames);
        configuration.setResourceTypes(typeNames);
        configuration.setGalleryMode(GalleryMode.adeView);
        configuration.setGalleryStoragePrefix("" + GalleryMode.adeView);
        configuration.setTabConfiguration(CmsGalleryTabConfiguration.resolve("selectDoc"));
        configuration.setReferencePath(getSitePath());
        configuration.setGalleryPath(getSitePath());
        CmsGalleryPopup dialog = new CmsGalleryPopup(null, configuration);
        dialog.center();
    }

    /**
     * Handles direct editing of the gallery title.<p>
     *
     * @param editEntry the edit entry
     * @param newTitle the new title
     */
    void handleEdit(CmsClientSitemapEntry editEntry, final String newTitle) {

        if (CmsStringUtil.isEmpty(newTitle)) {
            String dialogTitle = Messages.get().key(Messages.GUI_EDIT_TITLE_ERROR_DIALOG_TITLE_0);
            String dialogText = Messages.get().key(Messages.GUI_TITLE_CANT_BE_EMPTY_0);
            CmsAlertDialog alert = new CmsAlertDialog(dialogTitle, dialogText);
            alert.center();
            return;
        }
        String oldTitle = editEntry.getPropertyValue(CmsClientProperty.PROPERTY_TITLE);
        if (!oldTitle.equals(newTitle)) {
            CmsPropertyModification propMod = new CmsPropertyModification(
                getEntryId(),
                CmsClientProperty.PROPERTY_TITLE,
                newTitle,
                true);
            final List<CmsPropertyModification> propChanges = new ArrayList<CmsPropertyModification>();
            propChanges.add(propMod);
            CmsSitemapController controller = CmsSitemapView.getInstance().getController();
            controller.edit(editEntry, propChanges, CmsReloadMode.reloadEntry);
        }
    }

    /**
     * Creates the list item widget for the given folder.<p>
     *
     * @param galleryFolder the gallery folder
     *
     * @return the list item widget
     */
    private CmsListItemWidget createListWidget(CmsGalleryFolderEntry galleryFolder) {

        String title;
        if (galleryFolder.getOwnProperties().containsKey(CmsClientProperty.PROPERTY_TITLE)) {
            title = galleryFolder.getOwnProperties().get(CmsClientProperty.PROPERTY_TITLE).getStructureValue();
        } else {
            title = CmsResource.getName(galleryFolder.getSitePath());
            if (title.endsWith("/")) {
                title = title.substring(0, title.length() - 1);
            }
        }
        CmsListInfoBean infoBean = new CmsListInfoBean(title, galleryFolder.getSitePath(), null);
        CmsListItemWidget result = new CmsGalleryListItemWidget(infoBean);
        result.setIcon(galleryFolder.getIconClasses());
        result.addIconClickHandler(new ClickHandler() {

            public void onClick(ClickEvent event) {

                CmsSitemapView.getInstance().getController().loadPath(
                    getSitePath(),
                    new AsyncCallback<CmsClientSitemapEntry>() {

                        public void onFailure(Throwable caught) {

                            // nothing to do

                        }

                        public void onSuccess(CmsClientSitemapEntry entry) {

                            showGallery(entry);
                        }
                    });
            }
        });
        if ((CmsSitemapView.getInstance().getController().getEntryById(galleryFolder.getStructureId()) == null)
            || CmsSitemapView.getInstance().getController().getEntryById(galleryFolder.getStructureId()).isEditable()) {
            result.addTitleStyleName(I_CmsSitemapLayoutBundle.INSTANCE.sitemapItemCss().itemTitle());
            result.setTitleEditable(true);
            result.setTitleEditHandler(new I_CmsTitleEditHandler() {

                /**
                 * @see org.opencms.gwt.client.ui.CmsListItemWidget.I_CmsTitleEditHandler#handleEdit(org.opencms.gwt.client.ui.input.CmsLabel, com.google.gwt.user.client.ui.TextBox)
                 */
                public void handleEdit(final CmsLabel titleLabel, final TextBox box) {

                    CmsSitemapView.getInstance().getController().loadPath(
                        getSitePath(),
                        new AsyncCallback<CmsClientSitemapEntry>() {

                            public void onFailure(Throwable caught) {

                                // nothing to do
                            }

                            public void onSuccess(CmsClientSitemapEntry editEntry) {

                                CmsGalleryTreeItem.this.handleEdit(editEntry, box.getText());
                                box.removeFromParent();
                                titleLabel.setVisible(true);
                            }
                        });
                }
            });
        }
        return result;
    }

    /**
     * Updates the site path info.<p>
     *
     * @param newParentPath the new parent path
     * @param oldParentPath the previous parent path
     */
    private void updateParentPath(String newParentPath, String oldParentPath) {

        String oldPath = getSitePath();
        String newPath = oldPath.replaceFirst(oldParentPath, newParentPath);
        getListItemWidget().setSubtitleLabel(newPath);
        for (Widget child : getChildren()) {
            ((CmsGalleryTreeItem)child).updateParentPath(newPath, oldPath);
        }
    }
}
