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

import org.opencms.ade.sitemap.client.control.CmsSitemapController;
import org.opencms.gwt.client.ui.CmsPushButton;
import org.opencms.gwt.client.ui.CmsToggleButton;
import org.opencms.gwt.client.ui.CmsToolbar;
import org.opencms.gwt.client.ui.CmsToolbarContextButton;
import org.opencms.gwt.client.ui.I_CmsToolbarButton;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Widget;

/**
 * Sitemap toolbar.<p>
 * 
 * @since 8.0.0
 */
public class CmsSitemapToolbar extends CmsToolbar {

    /** The context menu button. */
    private CmsToolbarContextButton m_contextMenuButton;

    /** The new menu button. */
    private CmsToolbarNewButton m_newMenuButton;

    /**
     * Constructor.<p>
     * 
     * @param controller the sitemap controller 
     */
    public CmsSitemapToolbar(CmsSitemapController controller) {

        addLeft(new CmsToolbarPublishButton(this, controller));
        m_newMenuButton = new CmsToolbarNewButton(this, controller);
        if (controller.isEditable() && (controller.getData().getDefaultNewElementInfo() != null)) {
            addLeft(new CmsToolbarClipboardButton(this, controller));
            addLeft(m_newMenuButton);
        }
        addLeft(new CmsToolbarShowNonNavigationButton());
        ClickHandler clickHandler = new ClickHandler() {

            /**
             * @see com.google.gwt.event.dom.client.ClickHandler#onClick(com.google.gwt.event.dom.client.ClickEvent)
             */
            public void onClick(ClickEvent event) {

                I_CmsToolbarButton source = (I_CmsToolbarButton)event.getSource();
                source.onToolbarClick();
                if (source instanceof CmsPushButton) {
                    ((CmsPushButton)source).clearHoverState();
                }
            }
        };
        m_contextMenuButton = new CmsToolbarContextButton(new CmsSitemapToolbarHandler(
            controller.getData().getContextMenuEntries()));
        m_contextMenuButton.addClickHandler(clickHandler);
        addRight(m_contextMenuButton);
        addRight(new CmsToolbarGoBackButton(this, controller));
    }

    /**
     * Deactivates all toolbar buttons.<p>
     */
    public void deactivateAll() {

        for (Widget button : getAll()) {
            if (button instanceof I_CmsToolbarActivatable) {
                ((I_CmsToolbarActivatable)button).setEnabled(false);
            } else if (button instanceof CmsToggleButton) {
                ((CmsToggleButton)button).setEnabled(false);
            }
        }
    }

    /**
     * Gets the context menu button.<p>
     * 
     * @return the context menu button 
     */
    public CmsToolbarContextButton getContextMenuButton() {

        return m_contextMenuButton;
    }

    /**
     * Should be executed by every widget when starting an action.<p>
     * 
     * @param widget the widget that got activated
     */
    public void onButtonActivation(Widget widget) {

        for (Widget w : getAll()) {
            if (!(w instanceof I_CmsToolbarActivatable)) {
                continue;
            }
            ((I_CmsToolbarActivatable)w).onActivation(widget);
        }
    }

    /**
     * Enables/disables the new menu button.<p>
     * 
     * @param enabled <code>true</code> to enable the button
     * @param disabledReason the reason, why the button is disabled
     */
    public void setNewEnabled(boolean enabled, String disabledReason) {

        if (enabled) {
            m_newMenuButton.enable();
        } else {
            m_newMenuButton.disable(disabledReason);
        }
    }
}
