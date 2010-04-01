/*
 * File   : $Source: /alkacon/cvs/opencms/src-modules/org/opencms/gwt/client/ui/Attic/CmsLabel.java,v $
 * Date   : $Date: 2010/04/01 13:46:26 $
 * Version: $Revision: 1.1 $
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

package org.opencms.gwt.client.ui;

import org.opencms.gwt.client.util.CmsDebugLog;
import org.opencms.gwt.client.util.CmsDomUtil;
import org.opencms.gwt.client.util.CmsStringUtil;
import org.opencms.gwt.client.util.CmsTextMetrics;

import java.util.ArrayList;
import java.util.List;

import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.Element;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.HasAllMouseHandlers;
import com.google.gwt.event.dom.client.HasClickHandlers;
import com.google.gwt.event.dom.client.MouseDownEvent;
import com.google.gwt.event.dom.client.MouseDownHandler;
import com.google.gwt.event.dom.client.MouseMoveEvent;
import com.google.gwt.event.dom.client.MouseMoveHandler;
import com.google.gwt.event.dom.client.MouseOutEvent;
import com.google.gwt.event.dom.client.MouseOutHandler;
import com.google.gwt.event.dom.client.MouseOverEvent;
import com.google.gwt.event.dom.client.MouseOverHandler;
import com.google.gwt.event.dom.client.MouseUpEvent;
import com.google.gwt.event.dom.client.MouseUpHandler;
import com.google.gwt.event.dom.client.MouseWheelEvent;
import com.google.gwt.event.dom.client.MouseWheelHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HasText;
import com.google.gwt.user.client.ui.HasWordWrap;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.Widget;

/**
 * Label with smart text truncating and tool tip.<p>
 * 
 * @author Michael Moossen
 * 
 * @version $Revision: 1.1 $
 * 
 * @since 8.0.0
 */
public class CmsLabel extends Widget
implements HasHorizontalAlignment, HasText, HasWordWrap, HasClickHandlers, HasAllMouseHandlers {

    /** List of elements to measure. */
    protected static ArrayList<Element> m_elements;

    /** Timer to defer width measures. */
    protected static Timer m_timer;

    /** Attribute name constant. */
    private static final String ATTR_WIDTH_CHECKED = "__widthChecked";

    /** Debug log displayed within the client window. */
    private static CmsDebugLog m_debug;

    /** Current horizontal alignment. */
    private HorizontalAlignmentConstant m_horzAlign;

    /** Indicates if text truncation is desired, used mainly for performance reasons. */
    private boolean m_truncate;

    /**
     * Creates an empty label.<p>
     */
    public CmsLabel() {

        this(Document.get().createDivElement());
    }

    /**
     * Creates an empty label using the given element.<p>
     * 
     * @param element the element to use 
     */
    public CmsLabel(Element element) {

        m_truncate = true;
        setElement(element);
    }

    /**
     * Creates a label with the specified text.<p>
     * 
     * @param text the new label's text
     */
    public CmsLabel(String text) {

        this();
        setText(text);
    }

    /**
     * Creates a label with the specified text.<p>
     * 
     * @param text the new label's text
     * @param wordWrap <code>false</code> to disable word wrapping
     */
    public CmsLabel(String text, boolean wordWrap) {

        this(text);
        setWordWrap(wordWrap);
    }

    /**
     * Truncates long text and sets the original text to the title attribute.<p> 
     * @param element 
     */
    protected static void fixElement(Element element) {

        if (getWidthChecked(element)) {
            return;
        }
        setWidthChecked(element, true);

        // measure the actual text width
        CmsTextMetrics tm = CmsTextMetrics.get();
        tm.bind(element);
        String text = element.getInnerText();
        int textWidth = tm.getWidth(text);
        tm.release();

        // the current element width
        int elementWidth = CmsDomUtil.getCurrentStyleInt(element, CmsDomUtil.Style.width);

        getDebug().printLine("fixElement: ");
        getDebug().printLine("text: " + text);
        getDebug().printLine("elemWidth: " + elementWidth);
        getDebug().printLine("textWidth: " + textWidth);

        if (elementWidth == 0) {
            // HACK: from time to time elementWidth seems to be zero :(
            setWidthChecked(element, false);
            return;
        }
        if (elementWidth >= textWidth) {
            return;
        }
        // if the text does not have enough space, fix it
        int maxChars = (int)((float)elementWidth / (float)textWidth * text.length());
        if (maxChars < 1) {
            maxChars = 1;
        }
        String newText = text.substring(0, maxChars - 1);
        if (text.startsWith("/")) {
            // file name?
            newText = CmsStringUtil.formatResourceName(text, maxChars);
        } else if (maxChars > 2) {
            // enough space for ellipsis?
            newText += "&hellip;";
        }
        if (newText.isEmpty()) {
            // if empty, it will break the layout
            newText = "&nbsp;";
        }
        // use html instead of text because of the entities
        element.setInnerHTML(newText);
        // add tooltip with the original text
        element.setAttribute("title", text);
    }

    /**
     * Returns the debug log.<p>
     * 
     * @return the debug log
     */
    private static CmsDebugLog getDebug() {

        if (m_debug == null) {
            m_debug = new CmsDebugLog();
            RootPanel.get().add(m_debug);
        }
        return m_debug;
    }

    /**
     * Gets the width checked value.<p>
     * 
     * @param element the element to get it for
     */
    private static boolean getWidthChecked(Element element) {

        return Boolean.parseBoolean(DOM.getElementAttribute(
            element.<com.google.gwt.user.client.Element> cast(),
            ATTR_WIDTH_CHECKED));
    }

    /**
     * Schedule the width measure of the given element.<p>
     * 
     * @param element the element to measure
     */
    private static void schedule(Element element) {

        if (getWidthChecked(element)) {
            return;
        }
        if (m_timer == null) {
            m_elements = new ArrayList<Element>();
            m_elements.add(element);
            /* defer until children have been (hopefully) layouted. */
            m_timer = new Timer() {

                /**
                 * @see com.google.gwt.user.client.Timer#run()
                 */
                @Override
                public void run() {

                    m_timer = null;
                    List<Element> elements = new ArrayList<Element>();
                    elements.addAll(m_elements);
                    m_elements = null;
                    for (Element elem : elements) {
                        fixElement(elem);
                    }
                }
            };
            // HACK: clientWidth seems to be from time to time zero :(
            // specially if waiting less than 200ms, see #fixElement
            m_timer.schedule(200);
        } else {
            m_elements.add(element);
        }
    }

    /**
     * Sets the width checked value.<p>
     * 
     * @param element the element to set it for
     * @param value the value to set
     */
    private static void setWidthChecked(Element element, boolean value) {

        DOM.setElementAttribute(
            element.<com.google.gwt.user.client.Element> cast(),
            ATTR_WIDTH_CHECKED,
            String.valueOf(value));
    }

    /**
     * @see com.google.gwt.event.dom.client.HasClickHandlers#addClickHandler(com.google.gwt.event.dom.client.ClickHandler)
     */
    public HandlerRegistration addClickHandler(ClickHandler handler) {

        return addDomHandler(handler, ClickEvent.getType());
    }

    /**
     * @see com.google.gwt.event.dom.client.HasMouseDownHandlers#addMouseDownHandler(com.google.gwt.event.dom.client.MouseDownHandler)
     */
    public HandlerRegistration addMouseDownHandler(MouseDownHandler handler) {

        return addDomHandler(handler, MouseDownEvent.getType());
    }

    /**
     * @see com.google.gwt.event.dom.client.HasMouseMoveHandlers#addMouseMoveHandler(com.google.gwt.event.dom.client.MouseMoveHandler)
     */
    public HandlerRegistration addMouseMoveHandler(MouseMoveHandler handler) {

        return addDomHandler(handler, MouseMoveEvent.getType());
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
     * @see com.google.gwt.event.dom.client.HasMouseUpHandlers#addMouseUpHandler(com.google.gwt.event.dom.client.MouseUpHandler)
     */
    public HandlerRegistration addMouseUpHandler(MouseUpHandler handler) {

        return addDomHandler(handler, MouseUpEvent.getType());
    }

    /**
     * @see com.google.gwt.event.dom.client.HasMouseWheelHandlers#addMouseWheelHandler(com.google.gwt.event.dom.client.MouseWheelHandler)
     */
    public HandlerRegistration addMouseWheelHandler(MouseWheelHandler handler) {

        return addDomHandler(handler, MouseWheelEvent.getType());
    }

    /**
     * @see com.google.gwt.user.client.ui.HasHorizontalAlignment#getHorizontalAlignment()
     */
    public HorizontalAlignmentConstant getHorizontalAlignment() {

        return m_horzAlign;
    }

    /**
     * @see com.google.gwt.user.client.ui.HasText#getText()
     */
    public String getText() {

        return getElement().getInnerText();
    }

    /**
     * @see com.google.gwt.user.client.ui.HasWordWrap#getWordWrap()
     */
    public boolean getWordWrap() {

        return !CmsDomUtil.getCurrentStyle(getElement(), CmsDomUtil.Style.whiteSpace).equals(
            CmsDomUtil.StyleValue.nowrap.name());
    }

    /**
     * Checks if text truncation is desired.<p>
     *
     * @return the truncate
     */
    public boolean isTruncate() {

        return m_truncate;
    }

    /**
     * @see com.google.gwt.user.client.ui.HasHorizontalAlignment#setHorizontalAlignment(com.google.gwt.user.client.ui.HasHorizontalAlignment.HorizontalAlignmentConstant)
     */
    public void setHorizontalAlignment(HorizontalAlignmentConstant align) {

        m_horzAlign = align;
        getElement().getStyle().setProperty(CmsDomUtil.Style.textAlign.name(), align.getTextAlignString());
    }

    /**
     * @see com.google.gwt.user.client.ui.HasText#setText(java.lang.String)
     */
    public void setText(String text) {

        getElement().setInnerText(text);
        boolean value = false;
        setWidthChecked(getElement(), value);
        widthCheck();
    }

    /**
     * Sets the truncation flag.<p>
     *
     * @param truncate the truncation flag to set
     */
    public void setTruncate(boolean truncate) {

        m_truncate = truncate;
    }

    /**
     * @see com.google.gwt.user.client.ui.HasWordWrap#setWordWrap(boolean)
     */
    public void setWordWrap(boolean wrap) {

        getElement().getStyle().setProperty(
            CmsDomUtil.Style.whiteSpace.name(),
            wrap ? CmsDomUtil.StyleValue.normal.name() : CmsDomUtil.StyleValue.nowrap.name());
    }

    /**
     * Will check the width and truncate the text if necessary.<p>
     */
    public void widthCheck() {

        if (!m_truncate || !isAttached()) {
            return;
        }
        schedule(this.getElement());
    }

    /**
     * @see com.google.gwt.user.client.ui.Widget#onLoad()
     */
    @Override
    protected void onLoad() {

        super.onLoad();
        widthCheck();
    }
}
