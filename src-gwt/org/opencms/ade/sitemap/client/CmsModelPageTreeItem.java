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
import org.opencms.ade.sitemap.client.hoverbar.CmsEditModelPageMenuEntry;
import org.opencms.ade.sitemap.shared.CmsClientSitemapEntry;
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

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;

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
            Messages.get().key(Messages.GUI_MODE_PAGE_TREE_ROOT_SUBTITLE_0),
            null);
        CmsListItemWidget content = new CmsListItemWidget(infoBean);
        content.setIcon(CmsIconUtil.getResourceIconClasses("modelpage", false));
        initContent(content);
    }

    /**
     * Constructor.<p>
     * 
     * @param modelpage the model page
     */
    public CmsModelPageTreeItem(CmsModelPageEntry modelpage) {

        super(true);
        initContent(createListWidget(modelpage));
        m_entryId = modelpage.getStructureId();
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
     * @param modelPage the model page bean 
     * 
     * @return the list item widget
     */
    private CmsListItemWidget createListWidget(final CmsModelPageEntry modelPage) {

        String title;
        if (modelPage.getOwnProperties().containsKey(CmsClientProperty.PROPERTY_TITLE)) {
            title = modelPage.getOwnProperties().get(CmsClientProperty.PROPERTY_TITLE).getStructureValue();
        } else {
            title = CmsResource.getName(modelPage.getRootPath());
            if (title.endsWith("/")) {
                title = title.substring(0, title.length() - 1);
            }
        }
        CmsListInfoBean infoBean = modelPage.getListInfoBean();
        infoBean.setTitle(title);
        CmsListItemWidget result = new CmsModelPageListItemWidget(infoBean);
        result.setIcon(CmsIconUtil.getResourceIconClasses("modelpage", modelPage.getRootPath(), false));
        if (CmsEditModelPageMenuEntry.checkVisible(modelPage.getStructureId())) {
            result.addIconClickHandler(new ClickHandler() {

                public void onClick(ClickEvent event) {

                    CmsEditModelPageMenuEntry.editModelPage(modelPage.getStructureId());
                }
            });
        }

        return result;
    }
}
