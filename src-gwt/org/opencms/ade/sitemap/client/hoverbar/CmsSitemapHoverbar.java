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

package org.opencms.ade.sitemap.client.hoverbar;

import org.opencms.ade.sitemap.client.CmsSitemapTreeItem;
import org.opencms.ade.sitemap.client.control.CmsSitemapController;
import org.opencms.ade.sitemap.client.ui.css.I_CmsImageBundle;
import org.opencms.ade.sitemap.shared.CmsClientSitemapEntry;
import org.opencms.gwt.client.ui.A_CmsHoverHandler;
import org.opencms.gwt.client.ui.CmsListItemWidget;
import org.opencms.gwt.client.ui.CmsPushButton;
import org.opencms.gwt.client.ui.tree.CmsTreeItem;
import org.opencms.util.CmsUUID;

import java.util.Collection;
import java.util.Iterator;

import com.google.gwt.event.dom.client.MouseOutEvent;
import com.google.gwt.event.dom.client.MouseOverEvent;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.event.shared.SimpleEventBus;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Widget;

/**
 * Sitemap tree item hover-bar.<p>
 * 
 * @since 8.0.0
 */
public final class CmsSitemapHoverbar extends FlowPanel {

    /** The sitemap controller. */
    private CmsSitemapController m_controller;

    /** Flag if hover bar buttons are enabled. */
    private boolean m_enabled;

    /** The sitemap entry id. */
    private CmsUUID m_entryId;

    /** The event bus. */
    private SimpleEventBus m_eventBus;

    /** Flag indicating if the hoverbar is currently hovered, the mouse cursor is over the bar. */
    private boolean m_hovered;

    /** Flag to indicate the the hoverbar visibility is locked. */
    private boolean m_locked;

    /**
     * Constructor.<p>
     * 
     * @param controller the controller
     * @param entryId sitemap entry id
     * @param isGallery <code>true</code> if in galleries view
     */
    private CmsSitemapHoverbar(CmsSitemapController controller, CmsUUID entryId, boolean isGallery) {

        m_controller = controller;
        m_entryId = entryId;
        m_eventBus = new SimpleEventBus();
        m_enabled = true;
        setStyleName(I_CmsImageBundle.INSTANCE.buttonCss().hoverbar());
        if (controller.isEditable()) {
            add(new CmsHoverbarContextMenuButton(this));
            if (!isGallery) {
                add(new CmsHoverbarMoveButton(this));
            }
        } else {
            add(new CmsHoverbarGotoParentButton(this));
            add(new CmsHoverbarGotoSubSitemapButton(this));
            add(new CmsHoverbarGotoButton(this));
        }
    }

    /**
     * Constructor.<p>
     * 
     * @param controller the controller
     * @param buttons the buttons to add to the hover bar
     */
    private CmsSitemapHoverbar(CmsSitemapController controller, Collection<Widget> buttons) {

        m_controller = controller;
        m_eventBus = new SimpleEventBus();
        m_enabled = true;
        setStyleName(I_CmsImageBundle.INSTANCE.buttonCss().hoverbar());
        for (Widget button : buttons) {
            add(button);
        }
    }

    /**
     * Installs a hover bar for the given item widget.<p>
     * 
     * @param controller the controller 
     * @param treeItem the item to hover
     * @param entryId the entry id
     */
    public static void installOn(CmsSitemapController controller, CmsTreeItem treeItem, CmsUUID entryId) {

        CmsSitemapHoverbar hoverbar = new CmsSitemapHoverbar(
            controller,
            entryId,
            !(treeItem instanceof CmsSitemapTreeItem));
        installHoverbar(hoverbar, treeItem.getListItemWidget());
    }

    /**
     * Installs a hover bar for the given item widget.<p>
     * 
     * @param controller the controller 
     * @param treeItem the item to hover
     * @param buttons the buttons
     * 
     * @return the hover bar instance 
     */
    public static CmsSitemapHoverbar installOn(
        CmsSitemapController controller,
        CmsTreeItem treeItem,
        Collection<Widget> buttons) {

        CmsSitemapHoverbar hoverbar = new CmsSitemapHoverbar(controller, buttons);
        installHoverbar(hoverbar, treeItem.getListItemWidget());
        return hoverbar;
    }

    /**
     * Installs the given hover bar.<p>
     * 
     * @param hoverbar the hover bar
     * @param widget the list item widget
     */
    private static void installHoverbar(final CmsSitemapHoverbar hoverbar, CmsListItemWidget widget) {

        hoverbar.setVisible(false);
        widget.getContentPanel().add(hoverbar);
        A_CmsHoverHandler handler = new A_CmsHoverHandler() {

            /**
             * @see org.opencms.gwt.client.ui.A_CmsHoverHandler#onHoverIn(com.google.gwt.event.dom.client.MouseOverEvent)
             */
            @Override
            protected void onHoverIn(MouseOverEvent event) {

                hoverbar.setHovered(true);
                if (hoverbar.isVisible()) {
                    // prevent show when not needed
                    return;
                }
                hoverbar.show();
            }

            /**
             * @see org.opencms.gwt.client.ui.A_CmsHoverHandler#onHoverOut(com.google.gwt.event.dom.client.MouseOutEvent)
             */
            @Override
            protected void onHoverOut(MouseOutEvent event) {

                hoverbar.setHovered(false);
                if (!hoverbar.isLocked()) {
                    hoverbar.hide();
                }
            }
        };
        widget.addMouseOutHandler(handler);
        widget.addMouseOverHandler(handler);
    }

    /**
     * Adds a new detach event handler.<p>
     * 
     * @param handler the handler to add
     * 
     * @return the handler registration
     */
    public HandlerRegistration addHideHandler(I_CmsHoverbarHideHandler handler) {

        return m_eventBus.addHandlerToSource(CmsHoverbarHideEvent.getType(), this, handler);
    }

    /**
     * Adds a new attach event handler.<p>
     * 
     * @param handler the handler to add
     * 
     * @return the handler registration
     */
    public HandlerRegistration addShowHandler(I_CmsHoverbarShowHandler handler) {

        return m_eventBus.addHandlerToSource(CmsHoverbarShowEvent.getType(), this, handler);
    }

    /**
     * Returns the controller.<p>
     *
     * @return the controller
     */
    public CmsSitemapController getController() {

        return m_controller;
    }

    /**
     * Returns the sitemap entry.<p>
     * 
     * @return the sitemap entry
     */
    public CmsClientSitemapEntry getEntry() {

        return m_controller.getEntryById(m_entryId);
    }

    /**
     * Detaches the hover bar.<p>
     */
    public void hide() {

        m_locked = false;
        setVisible(false);
        m_eventBus.fireEventFromSource(new CmsHoverbarHideEvent(), this);
        // CmsDebugLog.getInstance().printLine("detached");
    }

    /**
     * Returns if the bar is hovered.<p>
     * 
     * @return <code>true</code> if hovered
     */
    public boolean isHovered() {

        return m_hovered;
    }

    /**
     * Sets the buttons of the hoverbar enabled.<p>
     * 
     * @param enable if <code>true</code> the buttons will be enabled
     * @param disableMessage message for disabling buttons
     */
    public void setEnabled(boolean enable, String disableMessage) {

        if (m_enabled && !enable) {
            Iterator<Widget> it = iterator();
            while (it.hasNext()) {
                Widget w = it.next();
                if (w instanceof CmsPushButton) {
                    ((CmsPushButton)w).disable(disableMessage);
                }
            }
        } else if (!m_enabled && enable) {
            Iterator<Widget> it = iterator();
            while (it.hasNext()) {
                Widget w = it.next();
                if (w instanceof CmsPushButton) {
                    ((CmsPushButton)w).enable();
                }
            }
        }
    }

    /**
     * Locks the hoverbar visibility.<p>
     * 
     * @param locked <code>true</code> to lock the hoverbar visibility
     */
    public void setLocked(boolean locked) {

        m_locked = locked;
    }

    /**
     * Returns if the hoverbar visibility is locked.<p>
     * 
     * @return <code>true</code> if the hoverbar visibility is locked
     */
    protected boolean isLocked() {

        return m_locked;
    }

    /**
     * Sets the hovered state.<p>
     * 
     * @param hovered <code>true</code> if hovered
     */
    protected void setHovered(boolean hovered) {

        m_hovered = hovered;
    }

    /**
     * Shows the hoverbar firing the appropriate event.<p>
     */
    protected void show() {

        setVisible(true);
        m_eventBus.fireEventFromSource(new CmsHoverbarShowEvent(), this);
    }

}
