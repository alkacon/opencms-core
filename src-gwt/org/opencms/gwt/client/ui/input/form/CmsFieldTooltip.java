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

package org.opencms.gwt.client.ui.input.form;

import org.opencms.gwt.client.util.CmsJsUtil;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.EventTarget;
import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.dom.client.Style;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.dom.client.StyleInjector;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.Event.NativePreviewEvent;
import com.google.gwt.user.client.Event.NativePreviewHandler;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.Widget;

/**
 * Tooltip widget for element setting help texts.<p>
 */
public class CmsFieldTooltip extends Composite {

    /**
     * Data needed to create a tooltip.<p>
     */
    public static class Data {

        /** The tooltip content. */
        private String m_info;

        /** True if the content is HTML, false if it is plain text. */
        private boolean m_isHtml;

        /** The icon for which the tooltip should be shown. */
        private Panel m_reference;

        /**
         * Creates a new instance.<p>
         * @param reference the icon for which the tooltip should be shown
         * @param info the tooltip content
         * @param isHtml true if the content is HTML
         */
        public Data(Panel reference, String info, boolean isHtml) {

            m_info = info;
            m_isHtml = isHtml;
            m_reference = reference;
        }

        /**
         * Gets the tooltip content.<p>
         *
         * @return the tooltip content
         */
        public String getInfo() {

            return m_info;
        }

        /**
         * Gets the icon for which the tooltip is intended.<p>
         *
         * @return the icon for which to display a tooltip
         */
        public Panel getReference() {

            return m_reference;
        }

        /**
         * Returns true if the tooltip content is HTML.<p>
         *
         * @return true if the tooltip content is HTML
         */
        public boolean isHtml() {

            return m_isHtml;
        }

        /**
         * @see java.lang.Object#toString()
         */
        @Override
        public String toString() {

            return m_info;
        }
    }

    /**
     * Event handler for managing tooltip visibility.<p>
     */
    public static class Handler implements NativePreviewHandler {

        /** The currently active tooltip. */
        private CmsFieldTooltip m_tooltip;

        /**
         * Creats a new instance.<p>
         */
        public Handler() {

            Event.addNativePreviewHandler(this);
        }

        /**
         * Called when the user clicks on the icon.<p>
         *
         * @param data the tooltip data
         */
        public void buttonClick(Data data) {

            if (m_tooltip != null) {
                m_tooltip.makePersistent();
            }
        }

        /**
         * Called if the mouse moves over the button with a tooltip.<p>
         *
         * @param data the tooltip data for a button
         */
        public void buttonHover(Data data) {

            if (data != getData()) {
                closeTooltip();
                CmsFieldTooltip tooltip = new CmsFieldTooltip(data);
                if (data.isHtml()) {
                    tooltip.getLabel().setHTML(data.getInfo());
                } else {
                    tooltip.getLabel().setText(data.getInfo());
                }
                m_tooltip = tooltip;
                RootPanel.get().add(tooltip);
                position(tooltip.getElement(), data.getReference().getElement());
            }
        }

        /**
         * Called if the mouse leaves a button with a tooltip.<p>
         *
         * @param data the tooltip data for the button
         */
        public void buttonOut(Data data) {

            closeTooltip(false);
        }

        /**
         * Closes the tooltip.<p>
         */
        public void closeTooltip() {

            closeTooltip(true);
        }

        /**
         * Closes the active tooltip.<p>
         *
         * @param closePersistent true if a persistent tooltip should also be closed
         */
        public void closeTooltip(boolean closePersistent) {

            if (m_tooltip != null) {

                if (m_tooltip.isPersistent() && !closePersistent) {
                    return;
                }
                if (CmsJsUtil.getAttributeString(CmsJsUtil.getWindow(), "cmsDisableCloseTooltip") == null) {
                    m_tooltip.removeFromParent();
                }
                m_tooltip = null;
            }
        }

        /**
         * Gets the target element for a native event, or null if there is no target element.<p>
         *
         * @param nativeEvent the native event
         * @return the target element, or null if there is no target element
         */
        public Element getTargetElement(NativeEvent nativeEvent) {

            EventTarget target = nativeEvent.getEventTarget();
            Element targetElement = null;
            if (Element.is(target)) {
                targetElement = Element.as(target);
            }
            return targetElement;
        }

        /**
         * @see com.google.gwt.user.client.Event.NativePreviewHandler#onPreviewNativeEvent(com.google.gwt.user.client.Event.NativePreviewEvent)
         */
        public void onPreviewNativeEvent(NativePreviewEvent event) {

            int eventType = event.getTypeInt();
            NativeEvent nativeEvent = event.getNativeEvent();
            switch (eventType) {
                case Event.ONMOUSEWHEEL:
                    closeTooltip();
                    break;
                case Event.ONMOUSEDOWN:
                    if (tooltipContains(getTargetElement(nativeEvent))) {
                        event.consume();
                        return;
                    } else {
                        closeTooltip();
                    }
                    break;
                case Event.ONCLICK:
                    if (tooltipContains(getTargetElement(nativeEvent))) {
                        event.consume();
                        return;
                    }
                    break;
                default: // do nothing
                    break;

            }
        }

        /**
         * Checks if the tooltip contains a given element.<p>
         *
         * @param targetElement the element to check
         * @return true if the tooltip contains an element
         */
        public boolean tooltipContains(Element targetElement) {

            if ((targetElement != null) && (m_tooltip != null)) {
                return m_tooltip.getElement().isOrHasChild(targetElement)
                    || m_tooltip.getData().getReference().getElement().isOrHasChild(targetElement);
            }
            return false;
        }

        /**
         * Gets the tooltip data for the active tooltip, or null if no tooltip is active.<p>
         *
         * @return the tooltip data
         */
        Data getData() {

            if (m_tooltip == null) {
                return null;
            }
            return m_tooltip.getData();
        }
    }

    /** The ui binder interface for this widget. */
    protected interface I_UiBinder extends UiBinder<Widget, CmsFieldTooltip> {
        //UIBinder interface

    }

    /** The handler instance. */
    private static Handler m_handler = new Handler();

    /** The label with the help text. */
    @UiField
    protected HTML m_label;

    /** The tooltip data. */
    private Data m_data;

    /** Flag indicating whether tooltip is persistent (i.e. not closed when the mouse cursor leaves the icon). */
    private boolean m_persistent;

    /**
     * Creates a new instance.<p>
     *
     * @param data the tooltip data
     */
    public CmsFieldTooltip(Data data) {

        I_UiBinder uiBinder = GWT.create(I_UiBinder.class);
        initWidget(uiBinder.createAndBindUi(this));
        // force synchronous injection of styles
        StyleInjector.flush();
        m_data = data;
    }

    /**
     * Gets the handler instance.<p>
     *
     * @return the handler instance
     */
    public static Handler getHandler() {

        return m_handler;
    }

    /**
     * Positions the tooltip.<p>
     *
     *
     * @param elem the tooltip element
     * @param referenceElement the tooltip icon element
     */
    public static void position(Element elem, Element referenceElement) {

        int dy = 25;
        Style style = elem.getStyle();
        style.setLeft(0, Unit.PX);
        style.setTop(0, Unit.PX);
        int myX = elem.getAbsoluteLeft();
        int myY = elem.getAbsoluteTop();
        int refX = referenceElement.getAbsoluteLeft();
        int refY = referenceElement.getAbsoluteTop();
        int refWidth = referenceElement.getOffsetWidth();
        int newX = (refX - myX - ((2 * elem.getOffsetWidth()) / 3)) + (refWidth / 2);
        int newY = (refY - myY) + dy;
        style.setLeft(newX, Unit.PX);
        style.setTop(newY, Unit.PX);
    }

    /**
     * Gets the tooltip data.<p>
     *
     * @return the tooltip data
     */
    public Data getData() {

        return m_data;
    }

    /**
     * Gets the label for the help text.<p>
     *
     * @return the label for the help text
     */
    public HTML getLabel() {

        return m_label;
    }

    /**
     * Checks if the tooltip is persistent,  i.e. it can no longer be closed by leaving the button with the mouse cursor,
     * but needs to be closed by clicking somewhere else.<p>
     *
     * @return true if the tooltip is persistent
     */
    public boolean isPersistent() {

        return m_persistent;
    }

    /**
     * Makes the tooltip persistent, i.e. it can no longer be closed by leaving the button with the mouse cursor,
     * but needs to be closed by clicking somewhere else.<p>
     */
    public void makePersistent() {

        m_persistent = true;
    }

}
