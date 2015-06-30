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

package org.opencms.ade.containerpage.client.ui;

import org.opencms.gwt.client.ui.CmsToggleButton;
import org.opencms.gwt.client.ui.CmsToolbarPopup;
import org.opencms.gwt.client.ui.css.I_CmsLayoutBundle;

import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.Style;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.MouseOutEvent;
import com.google.gwt.event.dom.client.MouseOutHandler;
import com.google.gwt.event.dom.client.MouseOverEvent;
import com.google.gwt.event.dom.client.MouseOverHandler;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Widget;

/**
 * A popup menu which is displayed below the selection button.<p>
 */
public class CmsSelectionButtonMenu extends CmsToolbarPopup {

    /** The delay for showing the popup. */
    public static final int SHOW_DELAY = 150;

    /** The delay for hiding the popup. */
    public static final int HIDE_DELAY = 750;

    /** The resize interval for the popup. */
    public static final int RESIZE_INTERVAL = 250;

    /** Flag which indicates whether the mouse is over the button. */
    protected boolean m_mouseOverButton;

    /** Flag which indicates whether the mouse is over the popup. */
    protected boolean m_mouseOverPopup;

    /** The timer used to schedule show/hide operations. */
    protected Timer m_timer;

    /** The timer used for scheduling resize operations. */
    protected Timer m_sizingTimer;

    /** The selection button. */
    protected CmsToggleButton m_toggleButton;

    /** The panel in the popup which actually contains the buttons. */
    protected HorizontalPanel m_buttonPanel = new HorizontalPanel();

    /**
     * Creates a new selection menu.<p>
     *
     * @param button the selection button
     * @param isToolbarMode true if this is the toolbar mode
     * @param baseElement the base button element
     */
    public CmsSelectionButtonMenu(CmsToggleButton button, boolean isToolbarMode, Element baseElement) {

        super(button, isToolbarMode, baseElement);
        m_toggleButton = button;
        Style buttonPanelStyle = m_buttonPanel.getElement().getStyle();
        buttonPanelStyle.setPadding(4, Unit.PX);
        buttonPanelStyle.setBackgroundColor("white");
        String cornerAll = I_CmsLayoutBundle.INSTANCE.generalCss().cornerAll();
        m_buttonPanel.addStyleName(cornerAll);
        setGlassEnabled(false);
        add(m_buttonPanel);
    }

    /**
     * Installs the event handlers for displaying the selection button menu.<p>
     */
    public void activate() {

        if (m_sizingTimer != null) {
            return; // already activated
        }

        m_buttonPanel.addDomHandler(new MouseOverHandler() {

            public void onMouseOver(MouseOverEvent event) {

                m_mouseOverPopup = true;
                update();

            }
        }, MouseOverEvent.getType());

        m_buttonPanel.addDomHandler(new MouseOutHandler() {

            public void onMouseOut(MouseOutEvent event) {

                m_mouseOverPopup = false;
                update();
            }

        }, MouseOutEvent.getType());
        m_toggleButton.addMouseOverHandler(new MouseOverHandler() {

            public void onMouseOver(MouseOverEvent e) {

                m_mouseOverButton = true;
                update();

            }
        });
        m_toggleButton.addMouseOutHandler(new MouseOutHandler() {

            public void onMouseOut(MouseOutEvent e) {

                m_mouseOverButton = false;
                update();

            }

        });
        m_sizingTimer = new Timer() {

            /**
             * @see com.google.gwt.user.client.Timer#run()
             */
            @Override
            public void run() {

                adaptSize();
            }
        };
    }

    /**
     * Adds a widget to the button panel.<p>
     *
     * @param widget the widget to add
     */
    public void addToPanel(Widget widget) {

        m_buttonPanel.add(widget);
    }

    /**
     * @see org.opencms.gwt.client.ui.CmsPopup#hide()
     */
    @Override
    public void hide() {

        super.hide();
        m_sizingTimer.cancel();
    }

    /**
     * @see org.opencms.gwt.client.ui.CmsPopup#show()
     */
    @Override
    public void show() {

        super.show();
        m_sizingTimer.scheduleRepeating(RESIZE_INTERVAL);

    }

    /**
     * Adapts the size of the popup to the size of its contents.<p>
     */
    protected void adaptSize() {

        setWidth(m_buttonPanel.getOffsetWidth());
        setHeight(m_buttonPanel.getOffsetHeight());
    }

    /**
     * Handles state transitions of the popup caused by mouse events.<p>
     */
    protected void update() {

        // cancel timer on state change
        if (m_timer != null) {
            m_timer.cancel();
            m_timer = null;
        }

        // timed transition show -> hide
        if (isAttached() && !m_mouseOverButton && !m_mouseOverPopup) {
            m_timer = new Timer() {

                @Override
                public void run() {

                    hide();

                }
            };
            m_timer.schedule(HIDE_DELAY);
        }

        // timed transition hide -> show, only when selection button is active
        if (!isAttached() && m_mouseOverButton && m_toggleButton.isDown()) {
            m_timer = new Timer() {

                @Override
                public void run() {

                    show();
                    position();
                }
            };
            m_timer.schedule(SHOW_DELAY);
        }
    }
}
