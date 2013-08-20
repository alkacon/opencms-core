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

package org.opencms.ade.containerpage.client.ui;

import org.opencms.gwt.client.dnd.CmsDNDHandler;
import org.opencms.gwt.client.ui.A_CmsHoverHandler;
import org.opencms.gwt.client.ui.css.I_CmsLayoutBundle;

import java.util.Iterator;

import com.google.gwt.dom.client.Style.Display;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.HasMouseOutHandlers;
import com.google.gwt.event.dom.client.HasMouseOverHandlers;
import com.google.gwt.event.dom.client.MouseOutEvent;
import com.google.gwt.event.dom.client.MouseOutHandler;
import com.google.gwt.event.dom.client.MouseOverEvent;
import com.google.gwt.event.dom.client.MouseOverHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Widget;

/**
 * A panel to be displayed inside a container element to provide optional functions like edit, move, remove... <p> 
 * 
 * @since 8.0.0
 */
public class CmsElementOptionBar extends Composite implements HasMouseOverHandlers, HasMouseOutHandlers {

    /**
     * Hover handler for option bar.<p>
     */
    protected class HoverHandler extends A_CmsHoverHandler {

        /**
         * @see org.opencms.gwt.client.ui.A_CmsHoverHandler#onHoverIn(com.google.gwt.event.dom.client.MouseOverEvent)
         */
        @Override
        protected void onHoverIn(MouseOverEvent event) {

            if (activeBar != null) {

                try {
                    activeBar.removeHighlighting();

                } catch (Throwable t) {
                    // ignore
                }
                activeBar = null;
            }
            addHighlighting();
        }

        /**
         * @see org.opencms.gwt.client.ui.A_CmsHoverHandler#onHoverOut(com.google.gwt.event.dom.client.MouseOutEvent)
         */
        @Override
        protected void onHoverOut(MouseOutEvent event) {

            timer = new Timer() {

                @Override
                public void run() {

                    if (timer == this) {
                        removeHighlighting();
                    }
                }
            };
            timer.schedule(750);
        }
    }

    /** The currently active option bar. */
    /*default */static CmsElementOptionBar activeBar;

    /** The timer used for hiding the option bar. */
    /*default */static Timer timer;

    /** The CSS class to be assigned to each option-bar. */
    private static String CSS_CLASS = org.opencms.ade.containerpage.client.ui.css.I_CmsLayoutBundle.INSTANCE.containerpageCss().optionBar();

    /** The calculated panel width. */
    private int m_calculatedWidth;

    /** The parent container element. */
    private CmsContainerPageElementPanel m_containerElement;

    /** The panel. */
    private FlowPanel m_panel;

    /**
     * Constructor.<p>
     * 
     * @param containerElement the parent container element
     */
    public CmsElementOptionBar(CmsContainerPageElementPanel containerElement) {

        m_panel = new FlowPanel();
        m_containerElement = containerElement;
        initWidget(m_panel);
        HoverHandler handler = new HoverHandler();
        addMouseOverHandler(handler);
        addMouseOutHandler(handler);
        setStyleName(CSS_CLASS);
    }

    /**
     * Creates an option-bar for the given drag element.<p>
     * 
     * @param element the element to create the option-bar for
     * @param dndHandler the drag and drop handler
     * @param buttons the list of buttons to display
     * 
     * @return the created option-bar
     */
    public static CmsElementOptionBar createOptionBarForElement(
        CmsContainerPageElementPanel element,
        CmsDNDHandler dndHandler,
        A_CmsToolbarOptionButton... buttons) {

        CmsElementOptionBar optionBar = new CmsElementOptionBar(element);
        if (buttons != null) {
            // add buttons, last as first
            for (int i = buttons.length - 1; i >= 0; i--) {
                CmsElementOptionButton option = buttons[i].createOptionForElement(element);
                if (option == null) {
                    continue;
                }
                optionBar.add(option);
                if (buttons[i] instanceof CmsToolbarMoveButton) {
                    option.addMouseDownHandler(dndHandler);
                }
            }
        }
        optionBar.initWidth();
        return optionBar;
    }

    /**
     * Adds another option button.<p>
     * 
     * @param w the button to add
     */
    public void add(Widget w) {

        m_panel.add(w);
    }

    /**
     * @see com.google.gwt.event.dom.client.HasMouseOutHandlers#addMouseOutHandler(com.google.gwt.event.dom.client.MouseOutHandler)
     */
    public HandlerRegistration addMouseOutHandler(MouseOutHandler handler) {

        return addDomHandler(handler, MouseOutEvent.getType());

    }

    /**
     * @see com.google.gwt.event.dom.client.HasMouseOverHandlers#addMouseOverHandler(com.google.gwt.event.dom.client.MouseOverHandler)
     */
    public HandlerRegistration addMouseOverHandler(MouseOverHandler handler) {

        return addDomHandler(handler, MouseOverEvent.getType());
    }

    /**
    * Clears the bar.<p>
    */
    public void clear() {

        m_panel.clear();
        m_calculatedWidth = 0;
    }

    /**
     * Returns the calculated width of the widget.<p>
     * 
     * @return the calculated width
     */
    public int getCalculatedWidth() {

        return m_calculatedWidth;
    }

    /**
     * Calculates and sets the width of the option bar.<p>
     */
    public void initWidth() {

        m_calculatedWidth = 0;
        for (Widget w : m_panel) {
            if (!Display.NONE.getCssName().equalsIgnoreCase(w.getElement().getStyle().getDisplay())) {
                m_calculatedWidth += 20;
            }
        }
        getElement().getStyle().setWidth(m_calculatedWidth, Unit.PX);
    }

    /**
     * Returns an iterator for the child widgets.<p>
     * 
     * @return the iterator
     */
    public Iterator<Widget> iterator() {

        return m_panel.iterator();
    }

    /**
     * Removes the highlighting and option bar.<p>
     */
    public void removeHighlighting() {

        timer = null;
        if (activeBar == this) {
            activeBar = null;
        }
        getElement().removeClassName(I_CmsLayoutBundle.INSTANCE.stateCss().cmsHovering());
        getContainerElement().removeHighlighting();
    }

    /** 
     * Adds the highlighting and option bar.<p>
     */
    protected void addHighlighting() {

        timer = null;
        getElement().addClassName(I_CmsLayoutBundle.INSTANCE.stateCss().cmsHovering());
        getContainerElement().highlightElement();
        activeBar = this;

    }

    /**
     * Returns the parent container element.<p>
     * 
     * @return the parent container element
     */
    protected CmsContainerPageElementPanel getContainerElement() {

        return m_containerElement;
    }
}
