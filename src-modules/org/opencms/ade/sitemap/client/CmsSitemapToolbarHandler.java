/*
 * File   : $Source: /alkacon/cvs/opencms/src-modules/org/opencms/ade/sitemap/client/Attic/CmsSitemapToolbarHandler.java,v $
 * Date   : $Date: 2010/04/22 09:23:34 $
 * Version: $Revision: 1.4 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (C) 2002 - 2009 Alkacon Software (http://www.alkacon.com)
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

package org.opencms.ade.sitemap.client;

import org.opencms.ade.publish.client.CmsPublishDialog;
import org.opencms.gwt.client.ui.CmsConfirmDialog;
import org.opencms.gwt.client.ui.I_CmsConfirmDialogHandler;

/**
 * Sitemap toolbar handler.<p>
 * 
 * @author Michael Moossen
 * 
 * @version $Revision: 1.4 $ 
 * 
 * @since 8.0.0
 * 
 * @see org.opencms.ade.sitemap.client.CmsSitemapToolbar
 */
public class CmsSitemapToolbarHandler {

    /** The controller. */
    protected CmsSitemapController m_controller;

    /**
     * Constructor.<p>
     * 
     * @param controller the controller
     */
    public CmsSitemapToolbarHandler(CmsSitemapController controller) {

        m_controller = controller;
    }

    /**
     * Will be triggered when the user click on the add button.<p>
     */
    public void onAdd() {

        // TODO: add
    }

    /**
     * Will be triggered when the user click on the clipboard button.<p>
     */
    public void onClipboard() {

        // TODO: clipboard
    }

    /**
     * Will be triggered when the user click on the publish button.<p>
     */
    public void onPublish() {

        CmsPublishDialog.showPublishDialog();
    }

    /**
     * Will be triggered when the user click on the redo button.<p>
     */
    public void onRedo() {

        m_controller.redo();
    }

    /**
     * Will be triggered when the user click on the reset button.<p>
     */
    public void onReset() {

        CmsConfirmDialog dialog = new CmsConfirmDialog(org.opencms.gwt.client.Messages.get().key(
            org.opencms.gwt.client.Messages.GUI_DIALOG_RESET_TITLE_0), org.opencms.gwt.client.Messages.get().key(
            org.opencms.gwt.client.Messages.GUI_DIALOG_RESET_TEXT_0));
        dialog.setHandler(new I_CmsConfirmDialogHandler() {

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

                m_controller.reset();
            }
        });
        dialog.center();
    }

    /**
     * Will be triggered when the user click on the save button.<p>
     */
    public void onSave() {

        m_controller.commit();
    }

    /**
     * Will be triggered when the user click on the undo button.<p>
     */
    public void onUndo() {

        m_controller.undo();
    }
}
