/*
 * File   : $Source: /alkacon/cvs/opencms/src-modules/org/opencms/ade/sitemap/client/Attic/CmsSitemapHoverbarHandler.java,v $
 * Date   : $Date: 2010/04/21 14:29:20 $
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

import org.opencms.ade.sitemap.shared.CmsClientSitemapEntry;
import org.opencms.gwt.client.CmsCoreProvider;
import org.opencms.gwt.client.ui.CmsConfirmDialog;
import org.opencms.gwt.client.ui.I_CmsConfirmDialogHandler;
import org.opencms.gwt.client.ui.css.I_CmsLayoutBundle;
import org.opencms.gwt.client.util.CmsDomUtil;

import com.google.gwt.dom.client.Element;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.RootPanel;

/**
 * Sitemap hover-bar handler.<p>
 * 
 * @author Michael Moossen
 * 
 * @version $Revision: 1.2 $ 
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
            cancelHover(m_hoverbar.getDeleteButton().getElement());
            CmsDomUtil.ensureMouseOut(m_hoverbar.getDeleteButton().getElement());
            // TODO: check if the current entry has children and show the dialog only if so
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

                    m_controller.delete(getEntry());
                }
            });
            dialog.center();
        } else if (event.getSource().equals(m_hoverbar.getEditButton())) {
            // TODO: edit
        } else if (event.getSource().equals(m_hoverbar.getSubsitemapButton())) {
            // TODO: create subsitemap
        } else if (event.getSource().equals(m_hoverbar.getGotoButton())) {
            Window.Location.replace(CmsCoreProvider.get().link(m_entry.getSitePath()));
        }
    }

    /**
     * Sets the hover-bar.<p>
     *
     * @param hoverbar the hover-bar to set
     */
    public void setHoverbar(CmsSitemapHoverbar hoverbar) {

        m_hoverbar = hoverbar;
    }

    /**
     * Updates the sitemap entry.<p>
     *
     * @param entry the sitemap entry to update
     */
    public void updateEntry(CmsClientSitemapEntry entry) {

        m_entry.setName(entry.getName());
        m_entry.setSitePath(entry.getSitePath());
        m_entry.setTitle(entry.getTitle());
        m_entry.setVfsPath(entry.getVfsPath());
        m_entry.setProperties(entry.getProperties());
        m_entry.setPosition(entry.getPosition());
    }

    /**
     * Cancels the hover effect from the given element.<p>
     * 
     * @param element the element to cancel the hover effect for
     */
    private void cancelHover(Element element) {

        while (element.equals(RootPanel.getBodyElement())
            || !CmsDomUtil.hasClass(I_CmsLayoutBundle.INSTANCE.stateCss().cmsHovering(), element)) {
            element = element.getParentElement();
        }
        if (!element.equals(RootPanel.getBodyElement())) {
            element.removeClassName(I_CmsLayoutBundle.INSTANCE.stateCss().cmsHovering());
        }
    }
}
