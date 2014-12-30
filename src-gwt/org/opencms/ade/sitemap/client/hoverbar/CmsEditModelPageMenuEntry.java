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
import org.opencms.ade.sitemap.shared.CmsNewResourceInfo;
import org.opencms.gwt.client.CmsCoreProvider;
import org.opencms.gwt.client.ui.CmsConfirmDialog;
import org.opencms.gwt.client.ui.I_CmsConfirmDialogHandler;
import org.opencms.util.CmsUUID;

/**
 * Context menu entry for editing a model page.<p>
 */
public class CmsEditModelPageMenuEntry extends A_CmsSitemapMenuEntry {

    /**
     * Creates a new instance.<p>
     * 
     * @param hoverbar the hover bar 
     */
    public CmsEditModelPageMenuEntry(CmsSitemapHoverbar hoverbar) {

        super(hoverbar);
        setLabel(Messages.get().key(Messages.GUI_EDIT_MODELPAGE_CONTEXTMENU_0));
        setActive(true);
    }

    /** 
     * Chesks if the model page menu entry should be visible.<p>
     * 
     * @param id the id of the model page 
     * @return true if the entry should be visible 
     */
    public static boolean checkVisible(CmsUUID id) {

        CmsNewResourceInfo info = CmsSitemapView.getInstance().getController().getData().getNewResourceInfoById(id);
        boolean show = CmsSitemapView.getInstance().isModelPageMode() && (info != null) && info.isEditable();
        return show;
    }

    /**
     * Opens the editor for a model page menu entry.<p>
     * 
     * @param id the model page menu entry 
     */
    public static void editModelPage(CmsUUID id) {

        CmsNewResourceInfo info = CmsSitemapView.getInstance().getController().getData().getNewResourceInfoById(id);
        CmsEditModelPageMenuEntry.openEditConfirmDialog(info);
    }

    /**
     * Opens the confirmation dialog for editing a model page.<p>
     * 
     * @param resourceInfo the resource information bean which belongs to the model page to edit 
     */
    public static void openEditConfirmDialog(final CmsNewResourceInfo resourceInfo) {

        I_CmsConfirmDialogHandler handler = new I_CmsConfirmDialogHandler() {

            public void onClose() {

                // noop 
            }

            public void onOk() {

                String resourcePath = resourceInfo.getVfsPath();
                String siteRoot = CmsCoreProvider.get().getSiteRoot();
                if (resourcePath.startsWith(siteRoot)) {
                    resourcePath = resourcePath.substring(siteRoot.length());
                    // prepend slash if necessary
                    if (!resourcePath.startsWith("/")) {
                        resourcePath = "/" + resourcePath;
                    }
                }
                CmsSitemapController controller = CmsSitemapView.getInstance().getController();
                controller.leaveEditor(resourcePath);
            }
        };
        String dialogTitle = Messages.get().key(Messages.GUI_EDIT_MODELPAGE_CONFIRM_TITLE_0);
        String dialogContent = Messages.get().key(Messages.GUI_EDIT_MODELPAGE_CONFIRM_CONTENT_0);
        String buttonText = Messages.get().key(Messages.GUI_EDIT_MODELPAGE_OK_0);

        CmsConfirmDialog dialog = new CmsConfirmDialog(dialogTitle, dialogContent);
        dialog.getOkButton().setText(buttonText);
        dialog.setHandler(handler);
        dialog.center();
    }

    /**
     * @see org.opencms.gwt.client.ui.contextmenu.I_CmsContextMenuEntry#execute()
     */
    public void execute() {

        CmsClientSitemapEntry entry = getHoverbar().getEntry();
        CmsUUID id = entry.getId();
        editModelPage(id);
    }

    /**
     * @see org.opencms.ade.sitemap.client.hoverbar.A_CmsSitemapMenuEntry#onShow()
     */
    @Override
    public void onShow() {

        CmsUUID id = getHoverbar().getId();
        boolean show = checkVisible(id);
        setVisible(show);

    }

}
