/*
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (C) Alkacon Software (http://www.alkacon.com)
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

package org.opencms.ade.sitemap.client.alias;

import org.opencms.gwt.client.ui.CmsPopup;
import org.opencms.gwt.client.ui.CmsPushButton;

/**
 * The alias editor.<p>
 */
public class CmsAliasEditor {

    /** The controller. */
    private CmsAliasTableController m_controller;

    /** The view. */
    private CmsAliasView m_view;

    /**
     * Creates a new instance.<p>
     */
    public CmsAliasEditor() {

        m_controller = new CmsAliasTableController();
        m_view = new CmsAliasView(m_controller);
        m_controller.setView(m_view);
    }

    /**
     * Opens the alias editor.<p>
     */
    public void show() {

        final CmsPopup popup = new CmsPopup(CmsAliasMessages.messageTitleAliasEditor());
        popup.setMainContent(m_view);
        for (CmsPushButton button : m_view.getButtonBar()) {
            popup.addButton(button);
        }
        m_view.setPopup(popup);
        popup.setWidth(1250);
        popup.addDialogClose(null);
        m_controller.load(new Runnable() {

            public void run() {

                popup.centerHorizontally(100);
            }
        });

    }
}
