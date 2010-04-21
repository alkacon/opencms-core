/*
 * File   : $Source: /alkacon/cvs/opencms/src-modules/org/opencms/ade/sitemap/client/Attic/CmsSitemapHoverbarHandler.java,v $
 * Date   : $Date: 2010/04/21 07:40:21 $
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

import org.opencms.ade.sitemap.shared.CmsClientSitemapEntry;
import org.opencms.gwt.client.CmsCoreProvider;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.Window;

/**
 * Sitemap hover-bar handler.<p>
 * 
 * @author Michael Moossen
 * 
 * @version $Revision: 1.1 $ 
 * 
 * @since 8.0.0
 * 
 * @see org.opencms.ade.sitemap.client.CmsSitemapToolbar
 */
public class CmsSitemapHoverbarHandler implements ClickHandler {

    /** The controller. */
    protected CmsSitemapController m_controller;

    /** The sitemap entry. */
    private CmsClientSitemapEntry m_entry;

    /** The hover-bar itself. */
    private CmsSitemapHoverbar m_hoverbar;

    /**
     * Constructor.<p>
     * 
     * @param entry the sitemap entry
     * @param controller the controller
     */
    public CmsSitemapHoverbarHandler(CmsClientSitemapEntry entry, CmsSitemapController controller) {

        m_entry = entry;
        m_controller = controller;
    }

    /**
     * Returns the sitemap entry.<p>
     *
     * @return the sitemap entry
     */
    public CmsClientSitemapEntry getEntry() {

        return m_entry;
    }

    /**
     * Returns the hover-bar.<p>
     *
     * @return the hover-bar
     */
    public CmsSitemapHoverbar getHoverbar() {

        return m_hoverbar;
    }

    /**
     * @see com.google.gwt.event.dom.client.ClickHandler#onClick(com.google.gwt.event.dom.client.ClickEvent)
     */
    public void onClick(ClickEvent event) {

        if (event.getSource().equals(m_hoverbar.getMoveButton())) {
            // TODO: move
        } else if (event.getSource().equals(m_hoverbar.getNewButton())) {
            // TODO: new
        } else if (event.getSource().equals(m_hoverbar.getDeleteButton())) {
            // TODO: delete
        } else if (event.getSource().equals(m_hoverbar.getEditButton())) {
            // TODO: edit
        } else if (event.getSource().equals(m_hoverbar.getGotoButton())) {
            Window.Location.replace(CmsCoreProvider.get().link(m_entry.getSitePath()));
        }
    }

    /**
     * Sets the sitemap entry.<p>
     *
     * @param entry the sitemap entry to set
     */
    public void setEntry(CmsClientSitemapEntry entry) {

        m_entry = entry;
    }

    /**
     * Sets the hover-bar.<p>
     *
     * @param hoverbar the hover-bar to set
     */
    public void setHoverbar(CmsSitemapHoverbar hoverbar) {

        m_hoverbar = hoverbar;
    }
}
