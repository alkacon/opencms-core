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

package org.opencms.acacia.client;

import org.opencms.acacia.client.css.I_CmsLayoutBundle;
import org.opencms.acacia.client.ui.CmsAttributeValueView;

import com.google.gwt.event.dom.client.MouseDownEvent;
import com.google.gwt.event.dom.client.MouseDownHandler;
import com.google.gwt.event.dom.client.MouseOutEvent;
import com.google.gwt.event.dom.client.MouseOutHandler;
import com.google.gwt.event.dom.client.MouseOverEvent;
import com.google.gwt.event.dom.client.MouseOverHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.Widget;

/**
 * The attribute value view highlighting handler.<p>
 */
public final class CmsValueFocusHandler implements MouseOverHandler, MouseOutHandler, MouseDownHandler {

    /** The handler instance. */
    private static CmsValueFocusHandler INSTANCE;

    /** The on button hover highlighted element. */
    private CmsAttributeValueView m_currentButtonFocus;

    /** The currently selected value view. */
    private CmsAttributeValueView m_currentFocus;

    /** The handler registration. */
    private HandlerRegistration m_handlerRegistration;

    /**
     * Constructor.<p>
     */
    private CmsValueFocusHandler() {

        m_handlerRegistration = RootPanel.get().addDomHandler(this, MouseDownEvent.getType());
    }

    /**
     * Returns the highlighting handler instance.<p>
     * 
     * @return the highlighting handler instance
     */
    public static CmsValueFocusHandler getInstance() {

        if (INSTANCE == null) {
            INSTANCE = new CmsValueFocusHandler();
        }
        return INSTANCE;
    }

    /**
     * Clears the static instance reference.<p>
     */
    private static void clearInstance() {

        INSTANCE = null;
    }

    /**
     * Removes all focus highlighting.<p>
     */
    public void clearFocus() {

        if ((m_currentFocus != null)) {
            m_currentFocus.toggleFocus(false);
            m_currentFocus = null;
        }
    }

    /**
     * Destroys the current handler instance.<p>
     */
    public void destroy() {

        m_currentFocus = null;
        if (m_handlerRegistration != null) {
            m_handlerRegistration.removeHandler();
        }
        m_handlerRegistration = null;
        clearInstance();
    }

    /**
     * Hides all help bubbles.<p>
     * 
     * @param formPanel the form panel
     * @param hide <code>true</code> to hide the help bubbles
     */
    public void hideHelpBubbles(Widget formPanel, boolean hide) {

        if (hide) {
            formPanel.addStyleName(I_CmsLayoutBundle.INSTANCE.form().hideHelpBubbles());
        } else {
            formPanel.removeStyleName(I_CmsLayoutBundle.INSTANCE.form().hideHelpBubbles());
        }
    }

    /**
     * @see com.google.gwt.event.dom.client.MouseDownHandler#onMouseDown(com.google.gwt.event.dom.client.MouseDownEvent)
     */
    public void onMouseDown(MouseDownEvent event) {

        event.stopPropagation();
        if (RootPanel.get().equals(event.getSource())) {
            com.google.gwt.dom.client.Element element = com.google.gwt.dom.client.Element.as(event.getNativeEvent().getEventTarget());
            if ((m_currentFocus != null) && !m_currentFocus.owns(element)) {
                m_currentFocus.toggleFocus(false);
                m_currentFocus = null;
            }
        } else {
            if (event.getSource().equals(m_currentFocus)) {
                return;
            }
            if ((m_currentFocus != null)) {
                m_currentFocus.toggleFocus(false);
            }
            m_currentFocus = (CmsAttributeValueView)event.getSource();
            m_currentFocus.toggleFocus(true);
        }
    }

    /**
     * @see com.google.gwt.event.dom.client.MouseOutHandler#onMouseOut(com.google.gwt.event.dom.client.MouseOutEvent)
     */
    public void onMouseOut(MouseOutEvent event) {

        if (!(event.getSource() instanceof CmsAttributeValueView)) {
            if ((m_currentButtonFocus != null)
                && m_currentButtonFocus.getElement().isOrHasChild(((Widget)event.getSource()).getElement())) {
                if (!m_currentButtonFocus.equals(m_currentFocus)) {
                    m_currentButtonFocus.toggleFocus(false);
                }
                m_currentButtonFocus = null;
            }
            return;
        }
    }

    /**
     * @see com.google.gwt.event.dom.client.MouseOverHandler#onMouseOver(com.google.gwt.event.dom.client.MouseOverEvent)
     */
    public void onMouseOver(MouseOverEvent event) {

        if (!(event.getSource() instanceof CmsAttributeValueView)) {
            CmsAttributeValueView parentView = null;
            Widget source = ((Widget)event.getSource()).getParent();
            while (parentView == null) {
                if (source instanceof CmsAttributeValueView) {
                    parentView = (CmsAttributeValueView)source;
                } else {
                    source = source.getParent();
                }
            }
            if (m_currentButtonFocus != null) {
                if (m_currentButtonFocus.equals(parentView)) {
                    return;
                } else if (!m_currentButtonFocus.equals(m_currentFocus)) {
                    m_currentButtonFocus.toggleFocus(false);
                }
            }
            m_currentButtonFocus = parentView;
            m_currentButtonFocus.toggleFocus(true);
            return;
        }
    }

    /**
     * Sets the given attribute value view focused.<p>
     * 
     * @param target the target attribute value view
     */
    public void setFocus(CmsAttributeValueView target) {

        if (m_currentFocus == target) {
            return;
        }

        if ((m_currentFocus != null)) {
            m_currentFocus.toggleFocus(false);
        }
        m_currentFocus = target;
        m_currentFocus.toggleFocus(true);
    }
}
