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

import org.opencms.ade.sitemap.client.CmsSitemapView;
import org.opencms.ade.sitemap.client.control.CmsSitemapController;
import org.opencms.ade.sitemap.shared.CmsGalleryType;
import org.opencms.ade.sitemap.shared.CmsSitemapData.EditorMode;
import org.opencms.gwt.client.CmsCoreProvider;
import org.opencms.gwt.client.ui.CmsPushButton;
import org.opencms.gwt.client.ui.CmsQuickLauncher.A_QuickLaunchHandler;
import org.opencms.gwt.client.ui.CmsToggleButton;
import org.opencms.gwt.client.ui.CmsToolbar;
import org.opencms.gwt.client.ui.CmsToolbarContextButton;
import org.opencms.gwt.client.ui.I_CmsToolbarButton;
import org.opencms.gwt.shared.CmsGwtConstants.QuickLaunch;
import org.opencms.gwt.shared.CmsQuickLaunchParams;

import java.util.Collection;

import com.google.gwt.dom.client.Style.Display;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Widget;

/**
 * Sitemap toolbar.<p>
 *
 * @since 8.0.0
 */
public class CmsSitemapToolbar extends CmsToolbar {

    /**
     * Quick launch handler for the sitemap.<p>
     */
    public static class SitemapQuickLaunchHandler extends A_QuickLaunchHandler {

        /**
         * @see org.opencms.gwt.client.ui.CmsQuickLauncher.I_QuickLaunchHandler#getParameters()
         */
        public CmsQuickLaunchParams getParameters() {

            return new CmsQuickLaunchParams(
                QuickLaunch.CONTEXT_SITEMAP,
                null,
                null,
                CmsSitemapView.getInstance().getController().getData().getReturnCode(),
                CmsCoreProvider.get().getUri());
        }

    }

    /** The sitemap clipboard button. */
    private CmsToolbarClipboardButton m_clipboardButton;

    /** The context menu button. */
    private CmsToolbarContextButton m_contextMenuButton;

    /** The new galleries menu button. */
    private CmsToolbarNewGalleryButton m_newGalleryMenuButton;

    /** The new menu button. */
    private CmsToolbarNewButton m_newMenuButton;

    /** The sitemap toolbar handler. */
    private CmsSitemapToolbarHandler m_toolbarHandler;

    /**
     * Constructor.<p>
     *
     * @param controller the sitemap controller
     */
    public CmsSitemapToolbar(CmsSitemapController controller) {

        m_toolbarHandler = new CmsSitemapToolbarHandler(controller.getData().getContextMenuEntries());
        addLeft(new CmsToolbarPublishButton(this, controller));
        m_newMenuButton = new CmsToolbarNewButton(this, controller);
        if (controller.isEditable() && (controller.getData().getDefaultNewElementInfo() != null)) {
            m_clipboardButton = new CmsToolbarClipboardButton(this, controller);
            addLeft(m_clipboardButton);
            addLeft(m_newMenuButton);
        }

        m_newGalleryMenuButton = new CmsToolbarNewGalleryButton(this, controller);
        if (controller.isEditable()) {
            addLeft(m_newGalleryMenuButton);
        }

        addLeft(new CmsToolbarChooseEditorModeButton(CmsCoreProvider.get().getUserInfo().isDeveloper()));
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

        m_contextMenuButton = new CmsToolbarContextButton(m_toolbarHandler);
        m_contextMenuButton.addClickHandler(clickHandler);
        insertRight(m_contextMenuButton, 0);
        setMode(EditorMode.navigation);
        setQuickLaunchHandler(new SitemapQuickLaunchHandler());
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
     * Returns the toolbar handler.<p>
     *
     * @return the toolbar handler
     */
    public CmsSitemapToolbarHandler getToolbarHandler() {

        return m_toolbarHandler;
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
     * Enables/disables the new clipboard button.<p>
     *
     * @param enabled <code>true</code> to enable the button
     * @param disabledReason the reason, why the button is disabled
     */
    public void setClipboardEnabled(boolean enabled, String disabledReason) {

        if (m_clipboardButton != null) {
            if (enabled) {
                m_clipboardButton.enable();
            } else {
                m_clipboardButton.disable(disabledReason);
            }
        }
    }

    /**
     * Sets the available gallery types.<p>
     *
     * @param galleryTypes the gallery types
     */
    public void setGalleryTypes(Collection<CmsGalleryType> galleryTypes) {

        m_newGalleryMenuButton.setGalleryTypes(galleryTypes);
    }

    /**
     * Sets the galleries mode.<p>
     *
     * @param mode the editor mode
     */
    public void setMode(EditorMode mode) {

        switch (mode) {
            case galleries:
                m_newGalleryMenuButton.getElement().getStyle().clearDisplay();
                m_newMenuButton.getElement().getStyle().setDisplay(Display.NONE);
                break;
            case modelpages:
            case categories:
                m_newGalleryMenuButton.getElement().getStyle().setDisplay(Display.NONE);
                m_newMenuButton.getElement().getStyle().clearDisplay();
                break;
            default:
                m_newMenuButton.getElement().getStyle().clearDisplay();
                m_newGalleryMenuButton.getElement().getStyle().setDisplay(Display.NONE);
                break;
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

    /**
     * Enables/disables the new menu button.<p>
     *
     * @param enabled <code>true</code> to enable the button
     * @param disabledReason the reason, why the button is disabled
     */
    public void setNewGalleryEnabled(boolean enabled, String disabledReason) {

        if (enabled) {
            m_newGalleryMenuButton.enable();
        } else {
            m_newGalleryMenuButton.disable(disabledReason);
        }
    }
}
