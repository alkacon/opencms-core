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
import org.opencms.ade.sitemap.shared.CmsClientSitemapEntry;
import org.opencms.ade.sitemap.shared.CmsSitemapData;
import org.opencms.gwt.client.ui.A_CmsListItemSelectDialog;
import org.opencms.gwt.client.ui.CmsConfirmDialog;
import org.opencms.gwt.client.ui.CmsListItemWidget;
import org.opencms.gwt.client.ui.I_CmsConfirmDialogHandler;
import org.opencms.gwt.shared.CmsListInfoBean;
import org.opencms.util.CmsStringUtil;

import java.util.List;

/**
 * Sitemap context menu create sub sitemap entry.<p>
 *
 * @since 8.0.0
 */
public class CmsSubSitemapMenuEntry extends A_CmsSitemapMenuEntry {

    /**
     * Selection dialog for sitemap folder types.<p>
     */
    protected class SitemapTypeDialog extends A_CmsListItemSelectDialog<CmsListInfoBean> {

        /**
         * Creates a new instance.<p>
         *
         * @param itemInfos the list info beans for the different sitemap types
         */
        public SitemapTypeDialog(List<CmsListInfoBean> itemInfos) {

            super(itemInfos, Messages.get().key(Messages.GUI_SITEMAP_TYPE_CHOICE_TITLE_0), Messages.get().key(
                Messages.GUI_SITEMAP_TYPE_CHOICE_TEXT_0));

        }

        /**
         * @see org.opencms.gwt.client.ui.A_CmsListItemSelectDialog#handleSelection(org.opencms.gwt.shared.CmsListInfoBean)
         */
        @Override
        protected void handleSelection(CmsListInfoBean info) {

            CmsSitemapController controller = CmsSitemapView.getInstance().getController();
            String typeName = info.getResourceType();
            final CmsClientSitemapEntry entry = getHoverbar().getEntry();
            controller.createSitemapSubEntry(entry, typeName);
        }

    }

    /**
     * Constructor.<p>
     *
     * @param hoverbar the hoverbar
     */
    public CmsSubSitemapMenuEntry(CmsSitemapHoverbar hoverbar) {

        super(hoverbar);
        setLabel(Messages.get().key(Messages.GUI_HOVERBAR_SUBSITEMAP_0));
        setActive(true);
    }

    /**
     * @see org.opencms.gwt.client.ui.contextmenu.I_CmsContextMenuEntry#execute()
     */
    public void execute() {

        if (isCreateFolderMode()) {
            executeFolderMode();
        } else {
            executeConvertMode();
        }
    }

    /**
     * @see org.opencms.ade.sitemap.client.hoverbar.A_CmsSitemapMenuEntry#onShow()
     */
    @Override
    public void onShow() {

        CmsSitemapController controller = getHoverbar().getController();
        CmsClientSitemapEntry entry = getHoverbar().getEntry();
        boolean show = controller.isEditable()
            && CmsSitemapView.getInstance().isNavigationMode()
            && entry.isInNavigation()
            && entry.isFolderType()
            && !controller.isRoot(entry.getSitePath());
        if (isCreateFolderMode()) {
            setVisible(show);
            setActive(show);
            setDisabledReason(null);
        } else {
            setVisible(show);
            if (show && !entry.isEditable()) {
                setActive(false);
                setDisabledReason(controller.getNoEditReason(entry));
            } else {
                setActive(true);
                setDisabledReason(null);
            }
        }
    }

    /**
     * @see org.opencms.gwt.client.ui.contextmenu.I_CmsContextMenuEntry#execute()
     */
    private void executeConvertMode() {

        final CmsSitemapController controller = getHoverbar().getController();
        String confirmTitle = Messages.get().key(Messages.GUI_SUBSITEMAP_CONFIRM_TITLE_0);
        String confirmMessage = Messages.get().key(Messages.GUI_SUBSITEMAP_CONFIRM_TEXT_0);
        CmsConfirmDialog confirmDialog = new CmsConfirmDialog(confirmTitle, confirmMessage);

        final CmsClientSitemapEntry entry = getHoverbar().getEntry();

        CmsListInfoBean infoBean = new CmsListInfoBean();
        infoBean.setTitle(entry.getTitle());
        infoBean.setSubTitle(entry.getSitePath());
        infoBean.addAdditionalInfo(Messages.get().key(Messages.GUI_NAME_0), entry.getName());
        String shownPath = entry.getVfsPath();
        if (CmsStringUtil.isEmptyOrWhitespaceOnly(shownPath)) {
            shownPath = "-";
        }
        infoBean.addAdditionalInfo(Messages.get().key(Messages.GUI_VFS_PATH_0), shownPath);
        // showing the resource type icon of the default file in navigation mode
        infoBean.setResourceType(CmsStringUtil.isNotEmptyOrWhitespaceOnly(entry.getDefaultFileType())
        ? entry.getDefaultFileType()
        : entry.getResourceTypeName());
        confirmDialog.addTopWidget(new CmsListItemWidget(infoBean));
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

                controller.createSubSitemap(entry.getId());
            }
        });
        confirmDialog.center();
    }

    /**
     * @see org.opencms.gwt.client.ui.contextmenu.I_CmsContextMenuEntry#execute()
     */
    private void executeFolderMode() {

        final CmsSitemapController controller = getHoverbar().getController();
        SitemapTypeDialog dialog = new SitemapTypeDialog(controller.getData().getSubsitemapFolderTypeInfos());
        dialog.center();
    }

    /**
     * Checks if new folders should be created for subsitemaps.<p>
     *
     * @return true if new folders should be created for subsitemaps
     */
    private boolean isCreateFolderMode() {

        CmsSitemapController controller = getHoverbar().getController();
        CmsSitemapData data = controller.getData();
        return data.isCreateNewFoldersForSubsitemaps()
            && (data.getSubsitemapFolderTypeInfos() != null)
            && !data.getSubsitemapFolderTypeInfos().isEmpty();
    }

}
