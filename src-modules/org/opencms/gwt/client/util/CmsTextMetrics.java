/*
 * File   : $Source: /alkacon/cvs/opencms/src-modules/org/opencms/gwt/client/util/Attic/CmsTextMetrics.java,v $
 * Date   : $Date: 2010/03/31 12:19:29 $
 * Version: $Revision: 1.1 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (c) 2002 - 2009 Alkacon Software GmbH (http://www.alkacon.com)
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
 * For further information about Alkacon Software GmbH, please see the
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

import java.util.ArrayList;
import java.util.List;

import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.ui.RootPanel;

/**
 * Provides precise pixel measurements for blocks of text so that you can
 * determine exactly how high and wide, in pixels, a given block of text will
 * be.<p>
 * 
 * Normal usage would be:
 * <pre>
 * for(Element e: elements) {
 *   CmsTextMetrics tm = CmsTextMetrics.get();
 *   // bind to match font size, etc.
 *   tm.bind(e);
 *   // measure text 
 *   if (r.getWidth(text) > 500) {
 *      // do something
 *   }
 *   // release
 *   tm.release();
 * }
 * </pre>
 * 
 * Based on <a href="http://code.google.com/p/my-gwt/source/browse/trunk/user/src/net/mygwt/ui/client/util/TextMetrics.java">my-gwt TextMetrics</a>.<p> 
 * 
 * @author Michael Moossen 
 * 
 * @version $Revision: 1.1 $ 
 * 
 * @since 8.0.0
 */
public final class CmsTextMetrics {

    /** Default attributes to bind. */
    private static final String[] ATTRIBUTES = new String[] {
        "fontSize",
        "fontSizeAdjust",
        "fontFamily",
        "fontStretch",
        "fontStyle",
        "fontVariant",
        "fontWeight",
        "letterSpacing",
        "textAlign",
        "textDecoration",
        "textIndent",
        "textShadow",
        "textTransform",
        "lineHeight",
        "whiteSpace",
        "wordSpacing",
        "wordWrap"};

    /** The count of used instances. */
    private static int m_count;

    /** The instance pool. */
    private static List<CmsTextMetrics> m_instances = new ArrayList<CmsTextMetrics>();

    /** The playground. */
    private Element m_elem;

    /**
     * Prevent instantiation.<p> 
     */
    private CmsTextMetrics() {

        m_elem = DOM.createDiv();
        DOM.appendChild(RootPanel.getBodyElement(), m_elem);
        DOM.setStyleAttribute(m_elem, "position", "absolute");
        DOM.setStyleAttribute(m_elem, "left", "-5000px");
        DOM.setStyleAttribute(m_elem, "top", "-5000px");
        DOM.setStyleAttribute(m_elem, "visibility", "hidden");
    }

    /**
     * Returns the singleton instance.<p>
     * 
     * @return the text metrics instance
     */
    public static CmsTextMetrics get() {

        m_count++;
        if (m_instances.size() < m_count) {
            m_instances.add(new CmsTextMetrics());
        }
        return m_instances.get(m_count - 1);
    }

    /**
     * Binds this text metrics instance to an element from which to copy existing
     * CSS styles that can affect the size of the rendered text.<p>
     * 
     * @param element the element
     */
    public void bind(Element element) {

        bind(element, ATTRIBUTES);
    }

    /**
     * Binds this text metrics instance to an element from which to copy existing
     * CSS styles that can affect the size of the rendered text.<p>
     * 
     * @param element the element
     * @param attributes the attributes to bind
     */
    public void bind(Element element, String... attributes) {

        for (String attr : attributes) {
            DOM.setStyleAttribute(m_elem, attr, DOM.getStyleAttribute(element, attr));
        }
    }

    /**
     * Returns the measured height of the specified text. For multiline text, be
     * sure to call {@link #setFixedWidth} if necessary.<p>
     * 
     * @param text the text to be measured
     * @return the height in pixels
     */
    public int getHeight(String text) {

        m_elem.setInnerText(text);
        return CmsDomUtil.getCurrentStyleInt(m_elem, "height");
    }

    /**
     * Returns the measured width of the specified text.<p>
     * 
     * @param text the text to measure
     * @return the width in pixels
     */
    public int getWidth(String text) {

        DOM.setStyleAttribute(m_elem, "width", "auto");
        m_elem.setInnerText(text);
        return CmsDomUtil.getCurrentStyleInt(m_elem, "width");
    }

    /**
     * Should be called, when finished measuring, to release the playground.<p>
     */
    public void release() {

        m_count--;
    }

    /**
     * Sets a fixed width on the internal measurement element. If the text will be
     * multiline, you have to set a fixed width in order to accurately measure the
     * text height.<p>
     * 
     * @param width the width to set on the element
     */
    public void setFixedWidth(int width) {

        DOM.setIntStyleAttribute(m_elem, "width", width);
    }
}