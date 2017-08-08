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

package org.opencms.ui.client;

import org.opencms.ui.components.extensions.CmsMaxHeightExtension;
import org.opencms.ui.shared.components.CmsMaxHeightState;
import org.opencms.ui.shared.rpc.I_CmsMaxHeightServerRpc;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.user.client.ui.Widget;
import com.vaadin.client.ComponentConnector;
import com.vaadin.client.ServerConnector;
import com.vaadin.client.communication.StateChangeEvent;
import com.vaadin.client.communication.StateChangeEvent.StateChangeHandler;
import com.vaadin.client.extensions.AbstractExtensionConnector;
import com.vaadin.shared.ui.Connect;

/**
 * This connector will manipulate the CSS classes of the extended widget depending on the scroll position.<p>
 */
@Connect(CmsMaxHeightExtension.class)
public class CmsMaxHeightConnector extends AbstractExtensionConnector {

    /** The serial version id. */
    private static final long serialVersionUID = -3661096843568550285L;

    /** The currently set height. */
    private int m_currentHeight;

    /** Flag indicating the required height is currently being evaluated. */
    private boolean m_evaluating;

    /** The native mutation observer. */
    private JavaScriptObject m_mutationObserver;

    /** The RPC proxy. */
    private I_CmsMaxHeightServerRpc m_rpc;

    /** Flag which is set when this connector is unregistered. */
    private boolean m_unregistered;

    /** The widget to enhance. */
    private Widget m_widget;

    /**
     * Constructor.<p>
     */
    public CmsMaxHeightConnector() {
        m_currentHeight = -1;
        m_rpc = getRpcProxy(I_CmsMaxHeightServerRpc.class);
    }

    /**
     * @see com.vaadin.client.ui.AbstractConnector#getState()
     */
    @Override
    public CmsMaxHeightState getState() {

        return (CmsMaxHeightState)super.getState();
    }

    /**
     * @see com.vaadin.client.ui.AbstractConnector#onUnregister()
     */
    @Override
    public void onUnregister() {

        super.onUnregister();
        m_unregistered = true;
        removeObserver();
    }

    /**
     * @see com.vaadin.client.extensions.AbstractExtensionConnector#extend(com.vaadin.client.ServerConnector)
     */
    @Override
    protected void extend(ServerConnector target) {

        // Get the extended widget
        m_widget = ((ComponentConnector)target).getWidget();
        addMutationObserver(m_widget.getElement());
        addStateChangeHandler(new StateChangeHandler() {

            /** Serial version id. */
            private static final long serialVersionUID = 1L;

            @SuppressWarnings("synthetic-access")
            public void onStateChanged(StateChangeEvent stateChangeEvent) {

                m_currentHeight = -1;
                handleMutation();
            }
        });
    }

    /**
     * Handles the widget mutation.<p>
     */
    protected void handleMutation() {

        if (m_unregistered) {
            removeObserver();
            return;
        }

        int maxHeight = getState().getMaxHeight();
        if (m_currentHeight > 0) {
            removeObserver(); // prevent 'recursive' call of handleMutation (it's not actually recursive since it's async, but would still lead to an infinite number of calls)
            JavaScriptObject scrollPositionData = saveScrollPositions(m_widget.getElement());
            String classToAdd = "o-measuring-height";
            m_widget.addStyleName(classToAdd);
            m_widget.getElement().getStyle().clearHeight();
            int computedHeight = m_widget.getOffsetHeight();
            m_widget.removeStyleName(classToAdd);
            if ((computedHeight + 10) < m_currentHeight) {
                m_currentHeight = -1;
                m_rpc.fixHeight(m_currentHeight);
            } else {
                m_widget.getElement().getStyle().setHeight(m_currentHeight, Unit.PX);
            }
            restoreScrollPositions(scrollPositionData);
            addMutationObserver(m_widget.getElement());
        } else if ((maxHeight > 0) && (m_widget.getOffsetHeight() > maxHeight)) {
            m_currentHeight = maxHeight;
            m_rpc.fixHeight(m_currentHeight);
        }
    }

    /**
     * Adds a native mutation observer to the widget element.<p>
     *
     * @param element the element
     */
    private native void addMutationObserver(Element element)/*-{
        var self = this;
        var observer = new MutationObserver(
                function(mutations) {
                    self.@org.opencms.ui.client.CmsMaxHeightConnector::handleMutation()();
                });
        this.@org.opencms.ui.client.CmsMaxHeightConnector::m_mutationObserver = observer;

        // configuration of the observer:
        var config = {
            attributes : true,
            childList : true,
            characterData : true,
            subtree : true
        };

        // pass in the target node, as well as the observer options
        observer.observe(element, config);
    }-*/;

    /**
     * Removes the mutation observer.<p>
     */
    private native void removeObserver()/*-{
        if (this.@org.opencms.ui.client.CmsMaxHeightConnector::m_mutationObserver != null) {
            this.@org.opencms.ui.client.CmsMaxHeightConnector::m_mutationObserver
                    .disconnect();
            this.@org.opencms.ui.client.CmsMaxHeightConnector::m_mutationObserver = null;
        }
    }-*/;

    /**
     * Restores saved scroll positions of scrollable containers.<p>
     *
     * @param capturedScrollPositions the saved scroll positions
     */
    private native void restoreScrollPositions(JavaScriptObject capturedScrollPositions) /*-{
        for (var i = 0; i < capturedScrollPositions.length; i++) {
            var entry = capturedScrollPositions[i];
            try {
                var element = entry[0];
                var st = entry[1];
                element.scrollTop = st;
            } catch (e) {
                // ignore
            }
        }
    }-*/;

    /**
     * Creates a list of pairs of v-scrollable elements and their respective scrollTop attribute beneath the given DOM element.<p>
     *
     * @param element the root element
     * @return a list of pairs [[element1, scrolltop1], [element2, scrolltop2], ...]
     */
    private native JavaScriptObject saveScrollPositions(Element element) /*-{
        var result = [];
        try {
            var elems = element.querySelectorAll(".v-scrollable");
            for (var i = 0; i < elems.length; i++) {
                var elem = elems[i];
                if (elem.scrollHeight > elem.clientHeight) {
                    result.push([ elem, elem.scrollTop ]);
                }
            }
        } catch (e) {
        }
        return result;
    }-*/;

}
