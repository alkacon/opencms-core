/*
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (c) Alkacon Software GmbH & Co. KG (https://www.alkacon.com)
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
 * company website: https://www.alkacon.com
 *
 * For further information about OpenCms, please see the
 * project website: https://www.opencms.org
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package org.opencms.ade.sitemap.client.hoverbar;

import org.opencms.ade.sitemap.client.CmsSitemapView;
import org.opencms.ade.sitemap.client.Messages;
import org.opencms.ade.sitemap.client.control.CmsSitemapController;
import org.opencms.ade.sitemap.client.ui.CmsCreateGalleryDialog;
import org.opencms.ade.sitemap.shared.CmsClientSitemapEntry;

/**
 * Sitemap context menu create gallery entry.<p>
 *
 * @since 8.0.0
 */
public class CmsCreateGalleryMenuEntry extends A_CmsSitemapMenuEntry {

    /**
     * Constructor.<p>
     *
     * @param hoverbar the hoverbar
     */
    public CmsCreateGalleryMenuEntry(CmsSitemapHoverbar hoverbar) {

        super(hoverbar);
        setLabel(Messages.get().key(Messages.GUI_GALLERIES_CREATE_0));
        setActive(true);
    }

    /**
     * @see org.opencms.gwt.client.ui.contextmenu.I_CmsContextMenuEntry#execute()
     */
    public void execute() {

        final CmsSitemapController controller = getHoverbar().getController();
        final CmsClientSitemapEntry entry = getHoverbar().getEntry();
        CmsCreateGalleryDialog dialog = new CmsCreateGalleryDialog(controller, entry.getResourceTypeId(), entry.getId());
        dialog.center();
    }

    /**
     * @see org.opencms.ade.sitemap.client.hoverbar.A_CmsSitemapMenuEntry#onShow()
     */
    @Override
    public void onShow() {

        // only gallery managers are allowed to create galleries
        if (getHoverbar().getController().isEditable()
            && CmsSitemapView.getInstance().isGalleryMode()
            && getHoverbar().getController().getData().isGalleryManager()) {
            setVisible(true);
        } else {
            setVisible(false);
        }
    }
}
