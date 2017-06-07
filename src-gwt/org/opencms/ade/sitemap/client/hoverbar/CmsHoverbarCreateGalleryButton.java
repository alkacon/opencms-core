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

package org.opencms.ade.sitemap.client.hoverbar;

import org.opencms.ade.sitemap.client.Messages;
import org.opencms.ade.sitemap.client.ui.CmsCreateGalleryDialog;
import org.opencms.gwt.client.ui.CmsPushButton;
import org.opencms.gwt.client.ui.I_CmsButton;
import org.opencms.gwt.client.ui.I_CmsButton.ButtonStyle;
import org.opencms.util.CmsUUID;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;

/**
 * Sitemap hoverbar "go to page" button.<p>
 *
 * @since 8.0.0
 */
public class CmsHoverbarCreateGalleryButton extends CmsPushButton {

    /** The hover bar. */
    CmsSitemapHoverbar m_hoverbar;

    /** The parent folder id. */
    private CmsUUID m_parentId;

    /** The resource type id. */
    private int m_resourceTypeId;

    /**
     * Constructor.<p>
     *
     * @param resourceTypeId the resource type id
     * @param parentId the parent folder id
     */
    public CmsHoverbarCreateGalleryButton(int resourceTypeId, CmsUUID parentId) {

        m_resourceTypeId = resourceTypeId;
        m_parentId = parentId;
        setTitle(Messages.get().key(Messages.GUI_GALLERIES_CREATE_0));
        setImageClass(I_CmsButton.ADD_SMALL);
        setButtonStyle(ButtonStyle.FONT_ICON, null);
        addClickHandler(new ClickHandler() {

            /**
             * @see com.google.gwt.event.dom.client.ClickHandler#onClick(com.google.gwt.event.dom.client.ClickEvent)
             */
            public void onClick(ClickEvent event) {

                if (m_hoverbar != null) {
                    m_hoverbar.hide();
                    openDialog();
                }
            }
        });
    }

    /**
     * Sets the hover bar instance.<p>
     *
     * @param hoverbar the hover bar
     */
    public void setHoverbar(CmsSitemapHoverbar hoverbar) {

        m_hoverbar = hoverbar;
    }

    /**
     * Opens the new gallery dialog.<p>
     */
    protected void openDialog() {

        CmsCreateGalleryDialog dialog = new CmsCreateGalleryDialog(
            m_hoverbar.getController(),
            m_resourceTypeId,
            m_parentId);
        dialog.center();
    }
}
