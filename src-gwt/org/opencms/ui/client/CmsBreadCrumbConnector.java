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

import org.opencms.gwt.client.util.CmsDomUtil;
import org.opencms.ui.shared.components.CmsBreadCrumbState;
import org.opencms.util.CmsStringUtil;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.ScheduledCommand;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.NodeList;
import com.google.gwt.dom.client.Style;
import com.google.gwt.event.logical.shared.ResizeEvent;
import com.google.gwt.event.logical.shared.ResizeHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.Widget;
import com.vaadin.client.communication.StateChangeEvent;
import com.vaadin.client.ui.AbstractComponentConnector;
import com.vaadin.shared.ui.Connect;
import com.vaadin.shared.ui.Connect.LoadStyle;

/**
 * Bread crumb component connector.<p>
 */
@Connect(value = org.opencms.ui.components.CmsBreadCrumb.class, loadStyle = LoadStyle.EAGER)
public class CmsBreadCrumbConnector extends AbstractComponentConnector implements ResizeHandler {

    /** The dynamic style element id. */
    private static final String DYNAMIC_STYLE_ID = "breadcrumbstyle";

    /** The style rule. */
    private static JavaScriptObject m_maxWidthRule;

    /** The serial version id. */
    private static final long serialVersionUID = -8483069041782419156L;

    /** The widget style name. */
    private static final String STYLE_NAME = "o-tools-breadcrumb";

    /**
     * @see com.vaadin.client.ui.AbstractComponentConnector#getState()
     */
    @Override
    public CmsBreadCrumbState getState() {

        return (CmsBreadCrumbState)super.getState();
    }

    /**
     * @see com.vaadin.client.ui.AbstractComponentConnector#getWidget()
     */
    @Override
    public HTML getWidget() {

        return (HTML)super.getWidget();
    }

    /**
     * @see com.google.gwt.event.logical.shared.ResizeHandler#onResize(com.google.gwt.event.logical.shared.ResizeEvent)
     */
    public void onResize(ResizeEvent event) {

        updateMaxWidth();
    }

    /**
     * @see com.vaadin.client.ui.AbstractComponentConnector#onStateChanged(com.vaadin.client.communication.StateChangeEvent)
     */
    @Override
    public void onStateChanged(StateChangeEvent stateChangeEvent) {

        super.onStateChanged(stateChangeEvent);

        getWidget().setHTML(getBreadCrumbHtml(getState().getEntries()));
        if (RootPanel.getBodyElement().isOrHasChild(getWidget().getElement())) {
            // only if attached
            updateMaxWidth();
        }
    }

    /**
     * @see com.vaadin.client.ui.AbstractComponentConnector#createWidget()
     */
    @Override
    protected Widget createWidget() {

        HTML widget = new HTML() {

            private HandlerRegistration m_handlerReg;

            @Override
            protected void onAttach() {

                super.onAttach();
                m_handlerReg = Window.addResizeHandler(CmsBreadCrumbConnector.this);
                Scheduler.get().scheduleDeferred(new ScheduledCommand() {

                    public void execute() {

                        updateMaxWidth();
                    }
                });
            }

            @Override
            protected void onDetach() {

                super.onDetach();
                m_handlerReg.removeHandler();
            }
        };
        widget.setStyleName(STYLE_NAME);
        return widget;
    }

    /**
     * Generates the bread crumb HTML for the given entries.<p>
     *
     * @param breadCrumbEntries the bread crub entries
     *
     * @return the generated HTML
     */
    protected String getBreadCrumbHtml(String[][] breadCrumbEntries) {

        StringBuffer buffer = new StringBuffer();
        buffer.append("<div>");
        if ((breadCrumbEntries != null)) {
            for (String[] entry : breadCrumbEntries) {
                appendBreadCrumbEntry(buffer, entry[0], entry[1]);
            }
        }
        buffer.append("</div>");
        return buffer.toString();
    }

    /**
     * Updates the entry max-width according to the available space.<p>
     */
    void updateMaxWidth() {

        Element base = getWidget().getElement();
        int availableWidth = base.getOffsetWidth();
        int requiredWidth = 0;
        NodeList<Element> children = CmsDomUtil.querySelectorAll("div > a, div > span", base);
        for (int i = 0; i < children.getLength(); i++) {
            Element child = children.getItem(i);
            Style style = child.getFirstChildElement().getStyle();
            style.setProperty("maxWidth", "none");
            requiredWidth += child.getOffsetWidth();
            style.clearProperty("maxWidth");
        }
        if (requiredWidth > availableWidth) {
            int padding = 30 + ((children.getLength() - 1) * 35);
            int maxWidth = (availableWidth - padding) / children.getLength();
            setMaxWidth(maxWidth + "px");
        } else {
            setMaxWidth("none");
        }
    }

    /**
     * Appends a bread crumb entry.<p>
     *
     * @param buffer the string buffer to append to
     * @param target the target state
     * @param label the entry label
     */
    private void appendBreadCrumbEntry(StringBuffer buffer, String target, String label) {

        if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(target)) {
            buffer.append("<a href=\"#!").append(target).append(
                "\" title=\"" + CmsDomUtil.escapeXml(label) + "\"><span>").append(label).append("</span></a>");
        } else {
            buffer.append(
                "<span class=\"o-tools-breadcrumb-active\" title=\""
                    + CmsDomUtil.escapeXml(label)
                    + "\"><span>").append(label).append("</span></span>");
        }
    }

    /**
     * Sets the max-width style property.<p>
     *
     * @param maxWidth the max-width value
     */
    private native void setMaxWidth(String maxWidth)/*-{
        if (@org.opencms.ui.client.CmsBreadCrumbConnector::m_maxWidthRule == null) {
            var style = $wnd.document
                    .getElementById(@org.opencms.ui.client.CmsBreadCrumbConnector::DYNAMIC_STYLE_ID);
            if (style == null) {
                style = $wnd.document.createElement("style");
                style
                        .setAttribute("id",
                                      @org.opencms.ui.client.CmsBreadCrumbConnector::DYNAMIC_STYLE_ID)
                style.appendChild(window.document.createTextNode(""));
                $wnd.document.head.appendChild(style);
            }
            style.sheet
                    .insertRule(
                                ".opencms .o-tools-breadcrumb  > div > a span, .opencms .o-tools-breadcrumb  > div > span span {}",
                                0);
            var rules = style.sheet.cssRules ? style.sheet.cssRules
                    : style.sheet.rules;
            @org.opencms.ui.client.CmsBreadCrumbConnector::m_maxWidthRule = rules[0];
        }
        @org.opencms.ui.client.CmsBreadCrumbConnector::m_maxWidthRule.style.maxWidth = maxWidth;
    }-*/;
}
