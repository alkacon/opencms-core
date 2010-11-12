/*
 * File   : $Source: /alkacon/cvs/opencms/src-modules/org/opencms/ade/containerpage/client/ui/Attic/CmsElementOptionBar.java,v $
 * Date   : $Date: 2010/11/12 07:42:48 $
 * Version: $Revision: 1.6 $
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

package org.opencms.ade.containerpage.client.ui;

import org.opencms.gwt.client.dnd.CmsDNDHandler;
import org.opencms.gwt.client.ui.A_CmsHoverHandler;
import org.opencms.gwt.client.ui.css.I_CmsLayoutBundle;

import com.google.gwt.event.dom.client.HasMouseOutHandlers;
import com.google.gwt.event.dom.client.HasMouseOverHandlers;
import com.google.gwt.event.dom.client.MouseOutEvent;
import com.google.gwt.event.dom.client.MouseOutHandler;
import com.google.gwt.event.dom.client.MouseOverEvent;
import com.google.gwt.event.dom.client.MouseOverHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;

/**
 * A panel to be displayed inside a container element to provide optional functions like edit, move, remove... <p> 
 * 
 * @author Tobias Herrmann
 * 
 * @version $Revision: 1.6 $
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

            getElement().addClassName(I_CmsLayoutBundle.INSTANCE.stateCss().cmsHovering());
            getContainerElement().highlightElement();
        }

        /**
         * @see org.opencms.gwt.client.ui.A_CmsHoverHandler#onHoverOut(com.google.gwt.event.dom.client.MouseOutEvent)
         */
        @Override
        protected void onHoverOut(MouseOutEvent event) {

            getElement().removeClassName(I_CmsLayoutBundle.INSTANCE.stateCss().cmsHovering());
            getContainerElement().removeHighlighting();
        }

    }

    /** The CSS class to be assigned to each option-bar. */
    private static String CSS_CLASS = org.opencms.ade.containerpage.client.ui.css.I_CmsLayoutBundle.INSTANCE.containerpageCss().optionBar();

    /** The parent container element. */
    private CmsContainerPageElement m_containerElement;

    /** The panel. */
    private FlowPanel m_panel;

    /**
     * Constructor.<p>
     * 
     * @param containerElement the parent container element
     */
    public CmsElementOptionBar(CmsContainerPageElement containerElement) {

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
        CmsContainerPageElement element,
        CmsDNDHandler dndHandler,
        A_CmsToolbarOptionButton... buttons) {

        CmsElementOptionBar optionBar = new CmsElementOptionBar(element);
        if (buttons != null) {
            for (int i = 0; i < buttons.length; i++) {
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
        return optionBar;
    }

    /**
     * Adds another option button.<p>
     * 
     * @param w the button to add
     */
    public void add(CmsElementOptionButton w) {

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
    }

    /**
     * Returns the parent container element.<p>
     * 
     * @return the parent container element
     */
    protected CmsContainerPageElement getContainerElement() {

        return m_containerElement;
    }
}
