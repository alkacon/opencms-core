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

package org.opencms.ade.sitemap.client;

import org.opencms.ade.sitemap.client.control.CmsSitemapController;
import org.opencms.ade.sitemap.shared.CmsClientSitemapEntry;
import org.opencms.ade.sitemap.shared.CmsGalleryType;
import org.opencms.ade.sitemap.shared.CmsModelPageEntry;
import org.opencms.file.CmsResource;
import org.opencms.gwt.client.property.CmsReloadMode;
import org.opencms.gwt.client.ui.CmsAlertDialog;
import org.opencms.gwt.client.ui.CmsListItemWidget;
import org.opencms.gwt.client.ui.tree.CmsTreeItem;
import org.opencms.gwt.shared.CmsIconUtil;
import org.opencms.gwt.shared.CmsListInfoBean;
import org.opencms.gwt.shared.property.CmsClientProperty;
import org.opencms.gwt.shared.property.CmsPropertyModification;
import org.opencms.util.CmsStringUtil;
import org.opencms.util.CmsUUID;

import java.util.ArrayList;
import java.util.List;

/**
 * Tree item for the model page editor mode.<p>
 */
public class CmsModelPageTreeItem extends CmsTreeItem {

    /**
     * List item widget that displays additional infos dynamically.<p>
     */
    protected class CmsModelPageListItemWidget extends CmsListItemWidget {

        /**
         * Constructor.<p>
         * 
         * @param infoBean the data to display
         */
        public CmsModelPageListItemWidget(CmsListInfoBean infoBean) {

            super(infoBean);
            ensureOpenCloseAdditionalInfo();
        }

    }

    /** The folder entry id. */
    private CmsUUID m_entryId;

    /**
     * Creates the fake model page tree item used as a root for the tree view.<p>
     */
    public CmsModelPageTreeItem() {

        super(true);
        CmsListInfoBean infoBean = new CmsListInfoBean(
            Messages.get().key(Messages.GUI_MODEL_PAGE_TREE_ROOT_TITLE_0),
            "",
            null);
        CmsListItemWidget content = new CmsListItemWidget(infoBean);
        content.setIcon(CmsIconUtil.getResourceIconClasses("containerpage", false));
        initContent(content);
    }

    /**
     * Constructor.<p>
     * 
     * @param galleryFolder the gallery folder
     */
    public CmsModelPageTreeItem(CmsModelPageEntry galleryFolder) {

        super(true);
        initContent(createListWidget(galleryFolder));
        m_entryId = galleryFolder.getStructureId();
    }

    /**
     * Creates the list item widget for the given type.<p>
     * 
     * @param galleryType the gallery type
     * 
     * @return the list item widget
     */
    public static CmsListItemWidget createListWidget(CmsGalleryType galleryType) {

        CmsListInfoBean infoBean = new CmsListInfoBean(galleryType.getNiceName(), galleryType.getDescription(), null);
        CmsListItemWidget result = new CmsListItemWidget(infoBean);
        result.setIcon(CmsIconUtil.getResourceIconClasses(galleryType.getTypeName(), null, false));
        return result;
    }

    /**
     * Creates the fake model page tree item used as a root for the tree view.<p>
     * 
     * @return the root tree item 
     */
    public static CmsModelPageTreeItem createRootItem() {

        return new CmsModelPageTreeItem();
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

        getListItemWidget().setSubtitleLabel(sitePath);
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
    private CmsListItemWidget createListWidget(CmsModelPageEntry galleryFolder) {

        String title;
        if (galleryFolder.getOwnProperties().containsKey(CmsClientProperty.PROPERTY_TITLE)) {
            title = galleryFolder.getOwnProperties().get(CmsClientProperty.PROPERTY_TITLE).getStructureValue();
        } else {
            title = CmsResource.getName(galleryFolder.getRootPath());
            if (title.endsWith("/")) {
                title = title.substring(0, title.length() - 1);
            }
        }
        CmsListInfoBean infoBean = galleryFolder.getListInfoBean();
        infoBean.setTitle(title);
        CmsListItemWidget result = new CmsModelPageListItemWidget(infoBean);
        result.setIcon(CmsIconUtil.getResourceIconClasses(
            galleryFolder.getResourceType(),
            galleryFolder.getRootPath(),
            false));

        return result;
    }

}
