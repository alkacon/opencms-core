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

package org.opencms.ade.sitemap.client.toolbar;

import org.opencms.ade.sitemap.client.CmsGalleryTreeItem;
import org.opencms.ade.sitemap.client.Messages;
import org.opencms.ade.sitemap.client.control.CmsSitemapController;
import org.opencms.ade.sitemap.client.ui.CmsCreateGalleryDialog;
import org.opencms.ade.sitemap.shared.CmsGalleryType;
import org.opencms.gwt.client.ui.CmsList;
import org.opencms.gwt.client.ui.CmsListItem;
import org.opencms.gwt.client.ui.CmsPushButton;
import org.opencms.gwt.client.ui.I_CmsButton;
import org.opencms.gwt.client.ui.I_CmsButton.ButtonStyle;
import org.opencms.gwt.client.ui.I_CmsListItem;

import java.util.Collection;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;

/**
 * The create new gallery menu.<p>
 */
public class CmsToolbarNewGalleryButton extends A_CmsToolbarListMenuButton {

    /** The gallery types list. */
    private CmsList<I_CmsListItem> m_galleriesList;

    /**
     * Constructor.<p>
     *
     * @param toolbar the tool bar instance
     * @param controller the controller
     */
    public CmsToolbarNewGalleryButton(CmsSitemapToolbar toolbar, CmsSitemapController controller) {

        super(
            Messages.get().key(Messages.GUI_TOOLBAR_NEW_GALLERY_TITLE_0),
            I_CmsButton.ButtonData.WAND_BUTTON.getIconClass(),
            toolbar,
            controller);
        m_galleriesList = new CmsList<I_CmsListItem>();
        addTab(createTab(m_galleriesList), Messages.get().key(Messages.GUI_GALLERIES_TYPES_TAB_0));
    }

    /**
     * @see org.opencms.ade.sitemap.client.toolbar.A_CmsToolbarListMenuButton#initContent()
     */
    @Override
    protected boolean initContent() {

        return true;
    }

    /**
     * Sets the available gallery types.<p>
     *
     * @param galleryTypes the gallery types
     */
    protected void setGalleryTypes(Collection<CmsGalleryType> galleryTypes) {

        m_galleriesList.clear();
        for (CmsGalleryType galleryType : galleryTypes) {
            m_galleriesList.addItem(makeGalleryTypeItem(galleryType));
        }
        m_galleriesList.truncate(TM_LITST_MENU, DIALOG_WIDTH);
    }

    /**
     * Creates a gallery type item.<p>
     *
     * @param galleryType the gallery type
     *
     * @return the type item
     */
    private I_CmsListItem makeGalleryTypeItem(final CmsGalleryType galleryType) {

        CmsListItem item = new CmsListItem(CmsGalleryTreeItem.createListWidget(galleryType));
        CmsPushButton button = new CmsPushButton();
        button.setImageClass(I_CmsButton.ADD_SMALL);
        button.setButtonStyle(ButtonStyle.FONT_ICON, null);
        button.setTitle(Messages.get().key(Messages.GUI_GALLERIES_CREATE_0));
        button.addClickHandler(new ClickHandler() {

            public void onClick(ClickEvent event) {

                closeMenu();
                CmsCreateGalleryDialog dialog = new CmsCreateGalleryDialog(
                    getController(),
                    galleryType.getTypeId(),
                    null);
                dialog.center();
            }
        });
        item.getListItemWidget().addButton(button);
        return item;
    }
}
