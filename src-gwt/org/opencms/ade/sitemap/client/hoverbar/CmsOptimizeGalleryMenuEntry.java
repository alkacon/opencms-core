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

import org.opencms.ade.sitemap.client.CmsSitemapView;
import org.opencms.ade.sitemap.client.Messages;
import org.opencms.ade.sitemap.shared.CmsClientSitemapEntry;
import org.opencms.gwt.client.ui.contextmenu.I_CmsActionHandler;
import org.opencms.gwt.client.util.CmsEmbeddedDialogHandler;
import org.opencms.gwt.shared.CmsGwtConstants;
import org.opencms.util.CmsUUID;

import java.util.Collections;

/**
 * Sitemap context menu optimize gallery entry.<p>
 */
public class CmsOptimizeGalleryMenuEntry extends A_CmsSitemapMenuEntry {

    /**
     * Constructor.<p>
     *
     * @param hoverbar the hoverbar
     */
    public CmsOptimizeGalleryMenuEntry(CmsSitemapHoverbar hoverbar) {

        super(hoverbar);
        setLabel(Messages.get().key(Messages.GUI_HOVERBAR_OPTIMIZE_GALLERY_0));
        setActive(true);
    }

    /**
     * @see org.opencms.gwt.client.ui.contextmenu.I_CmsContextMenuEntry#execute()
     */
    public void execute() {

        final CmsClientSitemapEntry entry = getHoverbar().getEntry();
        CmsEmbeddedDialogHandler dialogHandler = new CmsEmbeddedDialogHandler(new I_CmsActionHandler() {

            public void leavePage(String targetUri) {

                // not supported
            }

            public void onSiteOrProjectChange(String sitePath, String serverLink) {

                // not supported
            }

            public void refreshResource(CmsUUID structureId) {

            }
        });

        dialogHandler.openDialog(
            "org.opencms.ui.actions.CmsGalleryOptimizeDialogAction",
            CmsGwtConstants.CONTEXT_TYPE_FILE_TABLE,
            Collections.singletonList(entry.getId()));
    }

    /**
     * @see org.opencms.ade.sitemap.client.hoverbar.A_CmsSitemapMenuEntry#onShow()
     */
    @Override
    public void onShow() {

        if (CmsSitemapView.getInstance().isGalleryMode() && getHoverbar().getEntry().isEditable()) {
            setVisible(true);
        } else {
            setVisible(false);
        }
    }
}
