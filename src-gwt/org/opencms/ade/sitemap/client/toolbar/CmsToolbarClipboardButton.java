/*
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (c) Alkacon Software GmbH (http://www.alkacon.com)
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
import org.opencms.ade.sitemap.client.ui.css.I_CmsSitemapLayoutBundle;
import org.opencms.gwt.client.ui.CmsPushButton;
import org.opencms.gwt.client.ui.I_CmsButton;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.FlowPanel;

/**
 * Sitemap toolbar clipboard button.<p>
 * 
 * @since 8.0.0
 */
public class CmsToolbarClipboardButton extends A_CmsToolbarListMenuButton {

    /** The clear deleted list button. */
    private CmsPushButton m_clearDeleted;

    /** The clear modified list button. */
    private CmsPushButton m_clearModified;

    /**
     * Constructor.<p>
     * 
     * @param toolbar the toolbar instance
     * @param controller the sitemap controller 
     */
    public CmsToolbarClipboardButton(final CmsSitemapToolbar toolbar, final CmsSitemapController controller) {

        super(
            I_CmsButton.ButtonData.CLIPBOARD.getTitle(),
            I_CmsButton.ButtonData.CLIPBOARD.getIconClass(),
            toolbar,
            controller);
        m_clearModified = createClearButton(new ClickHandler() {

            /**
             * @see com.google.gwt.event.dom.client.ClickHandler#onClick(com.google.gwt.event.dom.client.ClickEvent)
             */
            public void onClick(ClickEvent event) {

                getController().clearModifiedList();
            }
        });
        m_clearDeleted = createClearButton(new ClickHandler() {

            /**
             * @see com.google.gwt.event.dom.client.ClickHandler#onClick(com.google.gwt.event.dom.client.ClickEvent)
             */
            public void onClick(ClickEvent event) {

                getController().clearDeletedList();
            }
        });
    }

    /**
     * Enables/disables the clear deleted list button.<p>
     * 
     * @param enable <code>true</code> to enable
     */
    public void enableClearDeleted(boolean enable) {

        if (enable) {
            m_clearDeleted.enable();
        } else {
            m_clearDeleted.disable(Messages.get().key(Messages.GUI_DISABLE_CLEAR_LIST_0));
        }
    }

    /**
     * Enables/disables the clear modified list button.<p>
     * 
     * @param enable <code>true</code> to enable
     */
    public void enableClearModified(boolean enable) {

        if (enable) {
            m_clearModified.enable();
        } else {
            m_clearModified.disable(Messages.get().key(Messages.GUI_DISABLE_CLEAR_LIST_0));
        }
    }

    /**
     * @see org.opencms.ade.sitemap.client.toolbar.A_CmsToolbarListMenuButton#initContent()
     */
    @Override
    protected void initContent() {

        CmsToolbarClipboardView view = new CmsToolbarClipboardView(this, getController());
        FlowPanel modifiedTab = createTab(view.getModified());

        modifiedTab.add(m_clearModified);
        addTab(modifiedTab, Messages.get().key(Messages.GUI_CLIPBOARD_MODIFIED_TITLE_0));
        FlowPanel deletedTab = createTab(view.getDeleted());

        deletedTab.add(m_clearDeleted);
        addTab(deletedTab, Messages.get().key(Messages.GUI_CLIPBOARD_DELETED_TITLE_0));
    }

    /**
     * Creates a clear list button.<p>
     * 
     * @param handler the button click handler
     * 
     * @return the created button widget
     */
    private CmsPushButton createClearButton(ClickHandler handler) {

        CmsPushButton clearButton = new CmsPushButton();
        clearButton.setText(Messages.get().key(Messages.GUI_CLIPBOARD_CLEAR_LIST_0));
        clearButton.setTitle(Messages.get().key(Messages.GUI_CLIPBOARD_CLEAR_LIST_0));
        clearButton.addClickHandler(handler);
        clearButton.addStyleName(I_CmsSitemapLayoutBundle.INSTANCE.clipboardCss().listClearButton());
        return clearButton;
    }
}
