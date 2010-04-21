/*
 * File   : $Source: /alkacon/cvs/opencms/src-modules/org/opencms/ade/sitemap/client/Attic/CmsSitemapToolbarHandler.java,v $
 * Date   : $Date: 2010/04/21 07:40:21 $
 * Version: $Revision: 1.2 $
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
import org.opencms.gwt.client.util.CmsDomUtil;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.CloseEvent;
import com.google.gwt.event.logical.shared.CloseHandler;
import com.google.gwt.user.client.ui.PopupPanel;

/**
 * Sitemap toolbar handler.<p>
 * 
 * @author Michael Moossen
 * 
 * @version $Revision: 1.2 $ 
 * 
 * @since 8.0.0
 * 
 * @see org.opencms.ade.sitemap.client.CmsSitemapToolbar
 */
public class CmsSitemapToolbarHandler implements ClickHandler {

    /** The controller. */
    protected CmsSitemapController m_controller;

    /** The toolbar itself. */
    private CmsSitemapToolbar m_toolbar;

    /**
     * Constructor.<p>
     * 
     * @param controller the controller
     */
    public CmsSitemapToolbarHandler(CmsSitemapController controller) {

        super();
        m_controller = controller;
    }

    /**
     * Returns the toolbar.<p>
     *
     * @return the toolbar
     */
    public CmsSitemapToolbar getToolbar() {

        return m_toolbar;
    }

    /**
     * @see com.google.gwt.event.dom.client.ClickHandler#onClick(com.google.gwt.event.dom.client.ClickEvent)
     */
    public void onClick(ClickEvent event) {

        if (event.getSource().equals(m_toolbar.getSaveButton())) {
            m_controller.commit();
        } else if (event.getSource().equals(m_toolbar.getAddButton())) {
            // TODO: add
        } else if (event.getSource().equals(m_toolbar.getClipboardButton())) {
            // TODO: clipboard
        } else if (event.getSource().equals(m_toolbar.getPublishButton())) {
            // triggering a mouse-out event, as it won't be fired once the dialog has opened (the dialog will capture all events)
            CmsDomUtil.ensureMouseOut(m_toolbar.getPublishButton().getElement());
            CmsPublishDialog.showPublishDialog(new CloseHandler<PopupPanel>() {

                /**
                 * @see com.google.gwt.event.logical.shared.CloseHandler#onClose(com.google.gwt.event.logical.shared.CloseEvent)
                 */
                public void onClose(CloseEvent<PopupPanel> event2) {

                    getToolbar().getPublishButton().setDown(false);
                }
            });
        } else if (event.getSource().equals(m_toolbar.getUndoButton())) {
            m_controller.undo();
        } else if (event.getSource().equals(m_toolbar.getRedoButton())) {
            m_controller.redo();
        } else if (event.getSource().equals(m_toolbar.getResetButton())) {
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
    }

    /**
     * Sets the toolbar.<p>
     *
     * @param toolbar the toolbar to set
     */
    public void setToolbar(CmsSitemapToolbar toolbar) {

        m_toolbar = toolbar;
    }
}
