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

package org.opencms.ade.containerpage.client.ui;

import org.opencms.gwt.client.CmsCoreProvider;
import org.opencms.gwt.client.CmsPageEditorTouchHandler;
import org.opencms.gwt.client.I_CmsElementToolbarContext;
import org.opencms.gwt.client.dnd.CmsDNDHandler;
import org.opencms.gwt.client.ui.A_CmsHoverHandler;
import org.opencms.gwt.client.ui.css.I_CmsLayoutBundle;
import org.opencms.gwt.client.util.I_CmsUniqueActiveItem;
import org.opencms.gwt.shared.CmsGwtConstants;

import java.util.Iterator;

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
public class CmsElementOptionBar extends Composite
implements HasMouseOverHandlers, HasMouseOutHandlers, I_CmsUniqueActiveItem, I_CmsElementToolbarContext {

    /**
     * Hover handler for option bar.<p>
     */
    protected class HoverHandler extends A_CmsHoverHandler {

        /**
         * @see org.opencms.gwt.client.ui.A_CmsHoverHandler#onHoverIn(com.google.gwt.event.dom.client.MouseOverEvent)
         */
        @Override
        protected void onHoverIn(MouseOverEvent event) {

            if (!CmsPageEditorTouchHandler.get().ignoreHover()) {
                timer = null;
                CmsCoreProvider.get().getFlyoutMenuContainer().setActiveItem(CmsElementOptionBar.this);
                addHighlighting();
            }
        }

        /**
         * @see org.opencms.gwt.client.ui.A_CmsHoverHandler#onHoverOut(com.google.gwt.event.dom.client.MouseOutEvent)
         */
        @Override
        protected void onHoverOut(MouseOutEvent event) {

            if (!CmsPageEditorTouchHandler.get().ignoreHover()) {

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
    }

    /** The timer used for hiding the option bar. */
    /*default */static Timer timer;

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

        m_panel = new FlowPanel(CmsGwtConstants.TAG_OC_EDITPOINT);
        m_containerElement = containerElement;
        initWidget(m_panel);
        HoverHandler handler = new HoverHandler();
        addMouseOverHandler(handler);
        addMouseOutHandler(handler);
        setStyleName(I_CmsElementToolbarContext.ELEMENT_OPTION_BAR_CSS_CLASS);
        addStyleName(I_CmsLayoutBundle.INSTANCE.generalCss().opencms());
        if (containerElement.isReused()) {
            addStyleName(
                org.opencms.ade.containerpage.client.ui.css.I_CmsLayoutBundle.INSTANCE.containerpageCss().reusedElement());
        } else {
            removeStyleName(
                org.opencms.ade.containerpage.client.ui.css.I_CmsLayoutBundle.INSTANCE.containerpageCss().reusedElement());
        }
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
        return optionBar;
    }

    /**
     * @see org.opencms.gwt.client.I_CmsElementToolbarContext#activateToolbarContext()
     */
    public void activateToolbarContext() {

        addHighlighting();
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
     * @see org.opencms.gwt.client.I_CmsElementToolbarContext#deactivateToolbarContext()
     */
    public void deactivateToolbarContext() {

        try {
            internalRemoveHighlighting();
        } catch (Exception e) {
            // ignore
        }
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
     * Returns an iterator for the child widgets.<p>
     *
     * @return the iterator
     */
    public Iterator<Widget> iterator() {

        return m_panel.iterator();
    }

    /**
     * @see org.opencms.gwt.client.util.I_CmsUniqueActiveItem#onDeactivate()
     */
    public void onDeactivate() {

        try {
            internalRemoveHighlighting();
        } catch (Exception e) {
            // ignore

        }
    }

    /**
     * Removes the highlighting and option bar.<p>
     */
    public void removeHighlighting() {

        timer = null;
        CmsCoreProvider.get().getFlyoutMenuContainer().clearIfMatches(this);
        internalRemoveHighlighting();
    }

    /**
     * Adds the highlighting and option bar.<p>
     */
    protected void addHighlighting() {

        getElement().addClassName(I_CmsLayoutBundle.INSTANCE.stateCss().cmsHovering());
        getContainerElement().highlightElement();

    }

    /**
     * Returns the parent container element.<p>
     *
     * @return the parent container element
     */
    protected CmsContainerPageElementPanel getContainerElement() {

        return m_containerElement;
    }

    /**
     * Removes the highlighting.<p>
     */
    protected void internalRemoveHighlighting() {

        getElement().removeClassName(I_CmsLayoutBundle.INSTANCE.stateCss().cmsHovering());
        getContainerElement().removeHighlighting();

    }
}
