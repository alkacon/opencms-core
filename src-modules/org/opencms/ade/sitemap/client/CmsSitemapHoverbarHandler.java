/*
 * File   : $Source: /alkacon/cvs/opencms/src-modules/org/opencms/ade/sitemap/client/Attic/CmsSitemapHoverbarHandler.java,v $
 * Date   : $Date: 2010/05/25 07:44:46 $
 * Version: $Revision: 1.12 $
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

import org.opencms.ade.sitemap.client.CmsSitemapEntryEditor.Mode;
import org.opencms.gwt.client.CmsCoreProvider;
import org.opencms.gwt.client.ui.CmsConfirmDialog;
import org.opencms.gwt.client.ui.I_CmsConfirmDialogHandler;

import com.google.gwt.user.client.Window;

/**
 * Sitemap hover-bar handler.<p>
 * 
 * @author Michael Moossen
 * 
 * @version $Revision: 1.12 $ 
 * 
 * @since 8.0.0
 * 
 * @see org.opencms.ade.sitemap.client.CmsSitemapToolbar
 */
public class CmsSitemapHoverbarHandler {

    /** The controller. */
    protected CmsSitemapController m_controller;

    /**
     * Constructor.<p>
     * 
     * @param controller the controller
     */
    public CmsSitemapHoverbarHandler(CmsSitemapController controller) {

        m_controller = controller;
    }

    /**
     * Triggered when the user clicks on the delete hover button.<p>
     * 
     * @param sitePath the current sitemap entry's site path
     */
    public void onDelete(final String sitePath) {

        if (m_controller.getEntry(sitePath).getSubEntries().isEmpty()) {
            m_controller.delete(sitePath);
            return;
        }
        // show the dialog only if the entry has children 
        CmsConfirmDialog dialog = new CmsConfirmDialog(
            Messages.get().key(Messages.GUI_DIALOG_DELETE_TITLE_0),
            Messages.get().key(Messages.GUI_DIALOG_DELETE_TEXT_0));
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

                m_controller.delete(sitePath);
            }
        });
        dialog.center();
    }

    /**
     * Triggered when the user clicks on the edit hover button.<p>
     * 
     * @param sitePath the current sitemap entry's site path
     */
    public void onEdit(String sitePath) {

        (new CmsSitemapEntryEditor(m_controller, sitePath, Mode.EDIT)).start();
    }

    /**
     * Triggered when the user clicks on the go-to hover button.<p>
     * 
     * @param sitePath the current sitemap entry's site path
     */
    public void onGoto(final String sitePath) {

        Window.Location.assign(CmsCoreProvider.get().link(sitePath));
    }

    /**
     * Triggered when the user clicks on the move hover button.<p>
     * 
     * @param sitePath the current sitemap entry's site path
     */
    public void onMove(String sitePath) {

        // TODO: move
    }

    /**
     * Triggered when the user clicks on the new hover button.<p>
     * 
     * @param sitePath the current sitemap entry's site path
     */
    public void onNew(String sitePath) {

        (new CmsSitemapEntryEditor(m_controller, sitePath, Mode.NEW)).start();
    }

    /**
     * Triggered when the user clicks on the parent sitemap hover button.<p>
     * 
     * @param sitePath the current sitemap entry's site path
     */
    public void onParent(final String sitePath) {

        Window.Location.assign(CmsCoreProvider.get().link(m_controller.getData().getParentSitemap()));
    }

    /**
     * Triggered when the user clicks on the sub-sitemap hover button.<p>
     * 
     * @param sitePath the current sitemap entry's site path
     */
    public void onSubsitemap(String sitePath) {

        // TODO: subsitemap
    }
}
