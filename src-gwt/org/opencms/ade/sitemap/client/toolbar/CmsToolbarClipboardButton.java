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

package org.opencms.ade.sitemap.client.toolbar;

import org.opencms.ade.sitemap.client.Messages;
import org.opencms.ade.sitemap.client.control.CmsSitemapController;
import org.opencms.gwt.client.ui.I_CmsButton;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;

/**
 * Sitemap toolbar clipboard button.<p>
 *
 * @since 8.0.0
 */
public class CmsToolbarClipboardButton extends A_CmsToolbarListMenuButton {

    /** The clear deleted list button enabled flag. */
    private boolean m_clearDeleted;

    /** The clear modified list button enabled flag. */
    private boolean m_clearModified;

    /** The modified list tab. */
    private CmsListTab m_modifiedTab;

    /** The deleted list tab. */
    private CmsListTab m_deletedTab;

    /**
     * Constructor.<p>
     *
     * @param toolbar the toolbar instance
     * @param controller the sitemap controller
     */
    public CmsToolbarClipboardButton(final CmsSitemapToolbar toolbar, final CmsSitemapController controller) {

        super(
            I_CmsButton.ButtonData.CLIPBOARD_BUTTON.getTitle(),
            I_CmsButton.ButtonData.CLIPBOARD_BUTTON.getIconClass(),
            toolbar,
            controller);
    }

    /**
     * Enables/disables the clear deleted list button.<p>
     *
     * @param enable <code>true</code> to enable
     */
    public void enableClearDeleted(boolean enable) {

        m_clearDeleted = enable;
        if (m_deletedTab != null) {
            m_deletedTab.setClearButtonEnabled(enable);
        }
    }

    /**
     * Enables/disables the clear modified list button.<p>
     *
     * @param enable <code>true</code> to enable
     */
    public void enableClearModified(boolean enable) {

        m_clearModified = enable;
        if (m_modifiedTab != null) {
            m_modifiedTab.setClearButtonEnabled(enable);
        }
    }

    /**
     * @see org.opencms.ade.sitemap.client.toolbar.A_CmsToolbarListMenuButton#initContent()
     */
    @Override
    protected boolean initContent() {

        CmsToolbarClipboardView view = new CmsToolbarClipboardView(this, getController());
        m_modifiedTab = createTab(view.getModified());
        m_modifiedTab.addClearListButton(new ClickHandler() {

            /**
             * @see com.google.gwt.event.dom.client.ClickHandler#onClick(com.google.gwt.event.dom.client.ClickEvent)
             */
            public void onClick(ClickEvent event) {

                getController().clearModifiedList();
            }
        });
        m_modifiedTab.setClearButtonEnabled(m_clearModified);
        addTab(m_modifiedTab, Messages.get().key(Messages.GUI_CLIPBOARD_MODIFIED_TITLE_0));
        m_deletedTab = createTab(view.getDeleted());

        m_deletedTab.addClearListButton(new ClickHandler() {

            /**
             * @see com.google.gwt.event.dom.client.ClickHandler#onClick(com.google.gwt.event.dom.client.ClickEvent)
             */
            public void onClick(ClickEvent event) {

                getController().clearDeletedList();
            }
        });
        m_deletedTab.setClearButtonEnabled(m_clearDeleted);
        addTab(m_deletedTab, Messages.get().key(Messages.GUI_CLIPBOARD_DELETED_TITLE_0));
        return true;
    }
}
