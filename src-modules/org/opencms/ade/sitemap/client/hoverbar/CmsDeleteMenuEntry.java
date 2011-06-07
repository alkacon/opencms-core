/*
 * File   : $Source: /alkacon/cvs/opencms/src-modules/org/opencms/ade/sitemap/client/hoverbar/Attic/CmsDeleteMenuEntry.java,v $
 * Date   : $Date: 2011/06/07 14:02:16 $
 * Version: $Revision: 1.11 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (C) 2002 - 2011 Alkacon Software (http://www.alkacon.com)
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
import org.opencms.ade.sitemap.client.control.CmsSitemapController;
import org.opencms.ade.sitemap.shared.CmsClientSitemapEntry;
import org.opencms.gwt.client.ui.CmsDeleteWarningDialog;
import org.opencms.gwt.client.ui.I_CmsConfirmDialogHandler;
import org.opencms.gwt.client.ui.css.I_CmsImageBundle;

/**
 * Sitemap context menu delete entry.<p>
 * 
 * @author Tobias Herrmann
 * @author Ruediger Kurz
 * 
 * @version $Revision: 1.11 $
 * 
 * @since 8.0.0
 */
public class CmsDeleteMenuEntry extends A_CmsSitemapMenuEntry {

    /**
     * Constructor.<p>
     * 
     * @param hoverbar the hoverbar 
     */
    public CmsDeleteMenuEntry(CmsSitemapHoverbar hoverbar) {

        super(hoverbar);
        setImageClass(I_CmsImageBundle.INSTANCE.contextMenuIcons().delete());
        setLabel(Messages.get().key(Messages.GUI_HOVERBAR_DELETE_0));
        setActive(true);
    }

    /**
     * @see org.opencms.gwt.client.ui.contextmenu.I_CmsContextMenuEntry#execute()
     */
    public void execute() {

        I_CmsConfirmDialogHandler handler = new I_CmsConfirmDialogHandler() {

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

                getHoverbar().getController().delete(getHoverbar().getSitePath());
            }
        };
        CmsDeleteWarningDialog dialog = new CmsDeleteWarningDialog(getHoverbar().getSitePath());
        dialog.setHandler(handler);
        dialog.loadAndShow(null);
    }

    /**
     * @see org.opencms.ade.sitemap.client.hoverbar.A_CmsSitemapMenuEntry#onShow(org.opencms.ade.sitemap.client.hoverbar.CmsHoverbarShowEvent)
     */
    @Override
    public void onShow(CmsHoverbarShowEvent event) {

        String sitePath = getHoverbar().getSitePath();
        CmsSitemapController controller = getHoverbar().getController();
        CmsClientSitemapEntry entry = controller.getEntry(sitePath);
        boolean show = !controller.isRoot(sitePath) && (entry != null) && entry.isInNavigation();
        setVisible(show);
        if (show && (entry != null) && !entry.isEditable()) {
            setActive(false);
            setDisabledReason(controller.getNoEditReason(entry));
        } else {
            setActive(true);
            setDisabledReason(null);
        }
    }
}
