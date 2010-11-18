/*
 * File   : $Source: /alkacon/cvs/opencms/src-modules/org/opencms/ade/sitemap/client/hoverbar/Attic/CmsSitemapHoverbar.java,v $
 * Date   : $Date: 2010/11/18 15:32:41 $
 * Version: $Revision: 1.11 $
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

package org.opencms.ade.sitemap.client.hoverbar;

import org.opencms.ade.sitemap.client.CmsSitemapTreeItem;
import org.opencms.ade.sitemap.client.control.CmsSitemapController;
import org.opencms.ade.sitemap.client.ui.css.I_CmsImageBundle;
import org.opencms.gwt.client.ui.A_CmsHoverHandler;
import org.opencms.gwt.client.ui.CmsListItemWidget;
import org.opencms.gwt.client.ui.CmsPushButton;

import java.util.Iterator;

import com.google.gwt.event.dom.client.MouseOutEvent;
import com.google.gwt.event.dom.client.MouseOverEvent;
import com.google.gwt.event.shared.HandlerManager;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Widget;

/**
 * Sitemap tree item hover-bar.<p>
 * 
 * @author Michael Moossen
 * 
 * @version $Revision: 1.11 $ 
 * 
 * @since 8.0.0
 */
public final class CmsSitemapHoverbar extends FlowPanel {

    /** The handler manager. */
    private HandlerManager m_handlerManager;

    /** The sitemap controller. */
    private CmsSitemapController m_controller;

    /** Flag if hover bar buttons are enabled. */
    private boolean m_enabled;

    /** Flag to indicate the the hoverbar visibility is locked. */
    private boolean m_locked;

    /** Flag indicating if the hoverbar is currently hovered, the mouse cursor is over the bar. */
    private boolean m_hovered;

    /** The sitemap tree item. */
    private CmsSitemapTreeItem m_treeItem;

    /**
     * Constructor.<p>
     * 
     * @param controller the controller
     * @param treeItem the item to hover
     */
    private CmsSitemapHoverbar(CmsSitemapController controller, CmsSitemapTreeItem treeItem) {

        m_controller = controller;
        m_treeItem = treeItem;
        m_handlerManager = new HandlerManager(this);
        m_enabled = true;
        setStyleName(I_CmsImageBundle.INSTANCE.buttonCss().hoverbar());
        if (controller.isEditable()) {
            add(new CmsHoverbarContextMenuButton(this));
            add(new CmsHoverbarMoveButton(this));

            //            add(new CmsHoverbarGotoSubSitemapButton(this));
            //            add(new CmsHoverbarGotoButton(this));
            //            add(new CmsHoverbarSubsitemapButton(this));
            //            add(new CmsHoverbarDeleteButton(this));
            //            add(new CmsHoverbarEditButton(this));
            //            add(new CmsHoverbarNewButton(this));
            //            add(new CmsHoverbarMergeButton(this));
            //            add(new CmsHoverbarParentButton(this));
        } else {
            add(new CmsHoverbarGotoButton(this));
        }
    }

    /**
     * Adds a new attach event handler.<p>
     * 
     * @param handler the handler to add
     * 
     * @return the handler registration
     */
    public HandlerRegistration addShowHandler(I_CmsHoverbarShowHandler handler) {

        return m_handlerManager.addHandler(CmsHoverbarShowEvent.getType(), handler);
    }

    /**
     * Adds a new detach event handler.<p>
     * 
     * @param handler the handler to add
     * 
     * @return the handler registration
     */
    public HandlerRegistration addHideHandler(I_CmsHoverbarHideHandler handler) {

        return m_handlerManager.addHandler(CmsHoverbarHideEvent.getType(), handler);
    }

    /**
     * Detaches the hoverbar.<p>
     */
    public void hide() {

        m_locked = false;
        setVisible(false);
        m_handlerManager.fireEvent(new CmsHoverbarHideEvent());
        // CmsDebugLog.getInstance().printLine("detached");
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
     * Returns the current entry's site path.<p>
     *
     * @return the current entry's site path
     */
    public String getSitePath() {

        return m_treeItem.getSitePath();
    }

    /**
     * Installs this hoverbar for the given item widget.<p>
     * 
     * @param controller the controller 
     * @param treeItem the item to hover
     */
    public static void installOn(final CmsSitemapController controller, final CmsSitemapTreeItem treeItem) {

        final CmsListItemWidget widget = treeItem.getListItemWidget();
        final CmsSitemapHoverbar hoverbar = new CmsSitemapHoverbar(controller, treeItem);
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
     * Shows the hoverbar firing the appropriate event.<p>
     */
    protected void show() {

        setVisible(true);
        m_handlerManager.fireEvent(new CmsHoverbarShowEvent());
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
     * Returns if the bar is hovered.<p>
     * 
     * @return <code>true</code> if hovered
     */
    public boolean isHovered() {

        return m_hovered;
    }

}
