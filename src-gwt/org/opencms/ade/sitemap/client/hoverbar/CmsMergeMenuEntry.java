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

package org.opencms.ade.sitemap.client.hoverbar;

import org.opencms.ade.sitemap.client.CmsSitemapView;
import org.opencms.ade.sitemap.client.Messages;
import org.opencms.ade.sitemap.client.control.CmsSitemapController;
import org.opencms.ade.sitemap.shared.CmsClientSitemapEntry;
import org.opencms.gwt.client.ui.CmsConfirmDialog;
import org.opencms.gwt.client.ui.I_CmsConfirmDialogHandler;
import org.opencms.gwt.client.ui.css.I_CmsImageBundle;

/**
 * Sitemap context menu merge entry.<p>
 * 
 * @since 8.0.0
 */
public class CmsMergeMenuEntry extends A_CmsSitemapMenuEntry {

    /**
     * Constructor.<p>
     * 
     * @param hoverbar the hoverbar 
     */
    public CmsMergeMenuEntry(CmsSitemapHoverbar hoverbar) {

        super(hoverbar);
        setImageClass(I_CmsImageBundle.INSTANCE.contextMenuIcons().mergeSitemap());
        setLabel(Messages.get().key(Messages.GUI_HOVERBAR_MERGE_SUB_0));
        setActive(true);
    }

    /**
     * @see org.opencms.gwt.client.ui.contextmenu.I_CmsContextMenuEntry#execute()
     */
    public void execute() {

        final CmsSitemapController controller = getHoverbar().getController();
        final CmsClientSitemapEntry entry = getHoverbar().getEntry();
        String confirmTitle = Messages.get().key(Messages.GUI_MERGE_SITEMAP_CONFIRM_TITLE_0);
        String confirmMessage = Messages.get().key(Messages.GUI_MERGE_SITEMAP_CONFIRM_TEXT_0);
        CmsConfirmDialog confirmDialog = new CmsConfirmDialog(confirmTitle, confirmMessage);
        confirmDialog.setHandler(new I_CmsConfirmDialogHandler() {

            /**
             * @see org.opencms.gwt.client.ui.I_CmsCloseDialogHandler#onClose()
             */
            public void onClose() {

                // do nothing
            }

            /**
             * @see org.opencms.gwt.client.ui.I_CmsConfirmDialogHandler#onOk()
             */
            public void onOk() {

                controller.mergeSubSitemap(entry.getId());
            }
        });
        confirmDialog.center();
    }

    /**
     * @see org.opencms.ade.sitemap.client.hoverbar.A_CmsSitemapMenuEntry#onShow(org.opencms.ade.sitemap.client.hoverbar.CmsHoverbarShowEvent)
     */
    @Override
    public void onShow(CmsHoverbarShowEvent event) {

        if (CmsSitemapView.getInstance().isNavigationMode()) {
            CmsSitemapController controller = getHoverbar().getController();
            CmsClientSitemapEntry entry = getHoverbar().getEntry();
            boolean show = (entry != null) && entry.isSubSitemapType() && !controller.isRoot(entry.getSitePath());
            setVisible(show);
        } else {
            setVisible(false);
        }
    }
}
