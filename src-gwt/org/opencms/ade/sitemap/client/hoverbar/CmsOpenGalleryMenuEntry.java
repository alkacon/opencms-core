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

import org.opencms.ade.galleries.client.ui.CmsGalleryPopup;
import org.opencms.ade.galleries.shared.CmsGalleryConfiguration;
import org.opencms.ade.galleries.shared.CmsGalleryTabConfiguration;
import org.opencms.ade.galleries.shared.I_CmsGalleryProviderConstants.GalleryMode;
import org.opencms.ade.sitemap.client.CmsSitemapView;
import org.opencms.ade.sitemap.client.Messages;
import org.opencms.ade.sitemap.client.control.CmsSitemapController;
import org.opencms.ade.sitemap.shared.CmsClientSitemapEntry;

import java.util.List;

/**
 * Sitemap context menu create gallery entry.<p>
 *
 * @since 8.0.0
 */
public class CmsOpenGalleryMenuEntry extends A_CmsSitemapMenuEntry {

    /**
     * Constructor.<p>
     *
     * @param hoverbar the hoverbar
     */
    public CmsOpenGalleryMenuEntry(CmsSitemapHoverbar hoverbar) {

        super(hoverbar);
        setLabel(Messages.get().key(Messages.GUI_HOVERBAR_OPEN_GALLERY_0));
        setActive(true);
    }

    /**
     * @see org.opencms.gwt.client.ui.contextmenu.I_CmsContextMenuEntry#execute()
     */
    public void execute() {

        CmsSitemapController controller = getHoverbar().getController();
        CmsClientSitemapEntry entry = getHoverbar().getEntry();

        CmsGalleryConfiguration configuration = new CmsGalleryConfiguration();
        List<String> typeNames = controller.getGalleryType(
            Integer.valueOf(entry.getResourceTypeId())).getContentTypeNames();
        configuration.setSearchTypes(typeNames);
        configuration.setResourceTypes(typeNames);
        configuration.setGalleryMode(GalleryMode.adeView);
        configuration.setGalleryStoragePrefix("" + GalleryMode.adeView);
        configuration.setTabConfiguration(CmsGalleryTabConfiguration.resolve("selectDoc"));
        configuration.setReferencePath(entry.getSitePath());
        configuration.setGalleryPath(entry.getSitePath());
        CmsGalleryPopup dialog = new CmsGalleryPopup(null, configuration);
        dialog.center();
    }

    /**
     * @see org.opencms.ade.sitemap.client.hoverbar.A_CmsSitemapMenuEntry#onShow()
     */
    @Override
    public void onShow() {

        if (CmsSitemapView.getInstance().isGalleryMode()) {
            setVisible(true);
        } else {
            setVisible(false);
        }
    }
}
