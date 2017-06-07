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
import org.opencms.ade.sitemap.client.control.CmsSitemapController;
import org.opencms.ade.sitemap.client.ui.CmsCopyModelPageDialog;
import org.opencms.ade.sitemap.shared.CmsClientSitemapEntry;
import org.opencms.ade.sitemap.shared.CmsNewResourceInfo;
import org.opencms.gwt.client.CmsCoreProvider;
import org.opencms.gwt.client.ui.CmsConfirmDialog;
import org.opencms.gwt.client.ui.I_CmsConfirmDialogHandler;
import org.opencms.gwt.shared.CmsListInfoBean;
import org.opencms.util.CmsUUID;

import com.google.gwt.user.client.rpc.AsyncCallback;

/**
 * Context menu entry for copying a model page.<p>
 */
public class CmsCopyModelPageMenuEntry extends A_CmsSitemapMenuEntry {

    /** The instance of the dialog for copying a model page. */
    CmsCopyModelPageDialog m_dialog;

    /**
     * Creates a new model page menu entry.<p>
     *
     * @param hoverbar the hoverbar
     */
    public CmsCopyModelPageMenuEntry(CmsSitemapHoverbar hoverbar) {

        super(hoverbar);
        setLabel(Messages.get().key(Messages.GUI_COPY_MODEL_PAGE_0));
        setActive(true);
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
        final CmsUUID id = entry.getId();
        CmsListInfoBean listInfo = CmsSitemapView.getInstance().getModelPageEntry(id).getListInfoBean();
        m_dialog = new CmsCopyModelPageDialog(listInfo, false, new AsyncCallback<String>() {

            public void onFailure(Throwable caught) {

                // do nothing

            }

            public void onSuccess(String title) {

                if (title != null) {
                    CmsSitemapView.getInstance().getController().createNewModelPage(
                        title,
                        m_dialog.getDescription(),
                        id,
                        false);
                }
            }
        });
        m_dialog.center();
    }

    /**
     * @see org.opencms.ade.sitemap.client.hoverbar.A_CmsSitemapMenuEntry#onShow()
     */
    @Override
    public void onShow() {

        CmsClientSitemapEntry entry = getHoverbar().getEntry();
        final CmsUUID id = entry.getId();
        boolean show = getHoverbar().getController().isEditable()
            && CmsSitemapView.getInstance().isModelPageMode()
            && (CmsSitemapView.getInstance().isModelPageEntry(id)
                || CmsSitemapView.getInstance().isParentModelPageEntry(id));
        setVisible(show);
    }
}
