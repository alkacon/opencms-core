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

package org.opencms.gwt.client.util;

import org.opencms.gwt.client.ui.css.I_CmsLayoutBundle;

import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.Style;
import com.google.gwt.dom.client.Style.BorderStyle;
import com.google.gwt.dom.client.Style.Overflow;
import com.google.gwt.dom.client.Style.Position;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.ui.RootPanel;

/**
 * A basic debug log, to print messages into the client window.<p>
 *
 * @since 8.0.0
 */
public final class CmsDebugLog {

    /** The debug log element id. */
    private static final String CMS_DEBUG_LOG_ID = "cms_debug_log";

    /** Global debugging flag. */
    private static final boolean DEBUG = false;

    /** Debug log displayed within the client window. */
    private static CmsDebugLog m_debug;

    /** The wrapped widget. */
    protected Element m_html;

    /**
     * Constructor.<p>
     */
    private CmsDebugLog() {

        if (!DEBUG) {
            return;
        }
        m_html = DOM.getElementById(CMS_DEBUG_LOG_ID);
        if (m_html == null) {
            m_html = DOM.createDiv();
            m_html.setAttribute("id", CMS_DEBUG_LOG_ID);
            Style style = m_html.getStyle();
            style.setWidth(400, Unit.PX);
            style.setHeight(700, Unit.PX);
            style.setPadding(10, Unit.PX);
            style.setOverflow(Overflow.AUTO);
            style.setBorderStyle(BorderStyle.SOLID);
            style.setBorderColor(I_CmsLayoutBundle.INSTANCE.constants().css().borderColor());
            style.setBorderWidth(1, Unit.PX);
            style.setPosition(Position.FIXED);
            style.setTop(50, Unit.PX);
            style.setRight(50, Unit.PX);
            style.setBackgroundColor(I_CmsLayoutBundle.INSTANCE.constants().css().backgroundColorDialog());
            style.setZIndex(I_CmsLayoutBundle.INSTANCE.constants().css().zIndexPopup());
            RootPanel.getBodyElement().appendChild(m_html);
        }
    }

    /**
     * Logs a message to the browser console if possible.<p>
     *
     * @param message the message to log
     */
    public static native void consoleLog(String message) /*-{
        if ($wnd.console && $wnd.console.log) {
            $wnd.console.log(message);
        }
    }-*/;

    /**
     * Returns the debug log.<p>
     *
     * @return the debug log
     */
    public static CmsDebugLog getInstance() {

        if (m_debug == null) {
            m_debug = new CmsDebugLog();
        }
        return m_debug;
    }

    /**
     * Clears the debug log.<p>
     */
    public void clear() {

        if (!DEBUG) {
            return;
        }
        m_html.setInnerHTML("");
    }

    /**
     * Prints a new line into the log window by adding a p-tag including given text as HTML.<p>
     *
     * @param text the text to print
     */
    public void printLine(String text) {

        if (!DEBUG) {
            return;
        }
        Element child = DOM.createElement("p");
        child.setInnerHTML(text);
        m_html.insertFirst(child);
    }
}
