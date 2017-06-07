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
import org.opencms.ade.sitemap.shared.CmsSitemapData.EditorMode;
import org.opencms.gwt.client.ui.contextmenu.I_CmsActionHandler;
import org.opencms.gwt.client.util.CmsEmbeddedDialogHandler;
import org.opencms.gwt.shared.CmsGwtConstants;
import org.opencms.util.CmsUUID;

import java.util.Collections;

/**
 * Sitemap context menu availability entry.<p>
 *
 * @since 8.0.0
 */
public class CmsAvailabilityMenuEntry extends A_CmsSitemapMenuEntry {

    /**
     * Constructor.<p>
     *
     * @param hoverbar the hoverbar
     */
    public CmsAvailabilityMenuEntry(CmsSitemapHoverbar hoverbar) {

        super(hoverbar);
        setLabel(Messages.get().key(Messages.GUI_HOVERBAR_AVAILABILITY_0));
        setActive(true);
    }

    /**
     * @see org.opencms.gwt.client.ui.contextmenu.I_CmsContextMenuEntry#execute()
     */
    public void execute() {

        CmsClientSitemapEntry entry = getHoverbar().getEntry();
        CmsUUID editId = null;
        if ((CmsSitemapView.getInstance().getEditorMode() == EditorMode.navigation)
            && (entry.getDefaultFileId() != null)) {
            editId = entry.getDefaultFileId();
        } else {
            editId = entry.getId();
        }

        CmsEmbeddedDialogHandler dialogHandler = new CmsEmbeddedDialogHandler(new I_CmsActionHandler() {

            public void leavePage(String targetUri) {

                // not supported
            }

            public void onSiteOrProjectChange(String sitePath, String serverLink) {

                // not supported
            }

            public void refreshResource(CmsUUID structureId) {

                updateEntry();
            }
        });
        dialogHandler.openDialog(
            "org.opencms.ui.actions.CmsAvailabilityDialogAction",
            CmsGwtConstants.CONTEXT_TYPE_SITEMAP_TOOLBAR,
            Collections.singletonList(editId));
    }

    /**
     * @see org.opencms.ade.sitemap.client.hoverbar.A_CmsSitemapMenuEntry#onShow()
     */
    @Override
    public void onShow() {

        CmsSitemapController controller = getHoverbar().getController();
        CmsClientSitemapEntry entry = getHoverbar().getEntry();
        boolean show = controller.isEditable() && !CmsSitemapView.getInstance().isSpecialMode() && (entry != null);
        setVisible(show);
        if (show && (entry != null) && !entry.isEditable()) {
            setActive(false);
            setDisabledReason(controller.getNoEditReason(entry));
        } else {
            setActive(true);
            setDisabledReason(null);
        }

    }

    /**
     * Updates the sitemap entry.<p>
     */
    protected void updateEntry() {

        getHoverbar().getController().updateSingleEntry(getHoverbar().getEntry().getId());
    }
}
