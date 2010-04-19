/*
 * File   : $Source: /alkacon/cvs/opencms/src-modules/org/opencms/ade/sitemap/client/Attic/CmsSitemapToolbarHandler.java,v $
 * Date   : $Date: 2010/04/19 11:48:12 $
 * Version: $Revision: 1.1 $
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
 * @version $Revision: 1.1 $ 
 * 
 * @since 8.0.0
 * 
 * @see org.opencms.ade.sitemap.client.CmsSitemapToolbar
 */
public class CmsSitemapToolbarHandler implements ClickHandler {

    /** The controller. */
    private CmsSitemapController m_controller;

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
        } else if (event.getSource().equals(m_toolbar.getSubsitemapButton())) {
            // TODO: create subsitemap
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
        } else if (event.getSource().equals(m_toolbar.getResetButton())) {
            // TODO: reset
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
