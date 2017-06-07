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
 * For further information about Alkacon Software GmbH & Co. KG, please see the
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

import java.util.HashMap;
import java.util.Map;

import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.Style;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.ui.RootPanel;

/**
 * Provides precise pixel measurements for blocks of text so that you can
 * determine exactly how high and wide, in pixels, a given block of text will
 * be.<p>
 *
 * Normal usage would be:
 * <pre>
 * for(Element e: elements) {
 *   CmsTextMetrics tm = CmsTextMetrics.get(e, "TextMetricsKey");
 *
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
 * @since 8.0.0
 */
public final class CmsTextMetrics {

    /** Default attributes to bind. */
    private static final CmsDomUtil.Style[] ATTRIBUTES = new CmsDomUtil.Style[] {
        CmsDomUtil.Style.fontSize,
        CmsDomUtil.Style.fontSizeAdjust,
        CmsDomUtil.Style.fontFamily,
        CmsDomUtil.Style.fontStretch,
        CmsDomUtil.Style.fontStyle,
        CmsDomUtil.Style.fontVariant,
        CmsDomUtil.Style.fontWeight,
        CmsDomUtil.Style.letterSpacing,
        CmsDomUtil.Style.textAlign,
        CmsDomUtil.Style.textDecoration,
        CmsDomUtil.Style.textIndent,
        CmsDomUtil.Style.textShadow,
        CmsDomUtil.Style.textTransform,
        CmsDomUtil.Style.lineHeight,
        CmsDomUtil.Style.whiteSpace,
        CmsDomUtil.Style.wordSpacing,
        CmsDomUtil.Style.wordWrap,
        CmsDomUtil.Style.padding};

    /** The map containing the instances. */
    private static Map<String, CmsTextMetrics> m_instances = new HashMap<String, CmsTextMetrics>();

    /** The playground. */
    private Element m_elem;

    /** The text metrics key. */
    private String m_key;

    /**
     * Internal constructor for creating a text metrics object with a given key.<p>
     *
     * @param key the key identifying the text metrics.
     */
    private CmsTextMetrics(String key) {

        m_key = key;
    }

    /**
     * Gets the text metrics object for a given DOM element and key.<p>
     *
     * If the key is null, or the method has been never called with the same key
     * before, a new text metrics object will be created, with its style taken from
     * the element parameter. Otherwise, the text metrics object for the given key will
     * be returned, and the element parameter will be ignored.
     *
     * @param element the element from which to take the style
     * @param key the text metrics key
     *
     * @return a text metrics object
     */
    public static CmsTextMetrics get(Element element, String key) {

        CmsTextMetrics instance = null;
        if (key == null) {
            instance = new CmsTextMetrics(key);
            instance.bind(element);
        } else {
            instance = m_instances.get(key);
            if (instance == null) {
                instance = new CmsTextMetrics(key);
                instance.bind(element);
                m_instances.put(key, instance);
            }
        }
        return instance;
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
        return CmsDomUtil.getCurrentStyleInt(m_elem, CmsDomUtil.Style.height);
    }

    /**
     * Returns the measured width of the specified text.<p>
     *
     * @param text the text to measure
     * @return the width in pixels
     */
    public int getWidth(String text) {

        m_elem.setInnerText(text);
        return CmsDomUtil.getCurrentStyleInt(m_elem, CmsDomUtil.Style.width);
    }

    /**
     * Should be called, when finished measuring, to release the playground.<p>
     */
    public void release() {

        if (m_key == null) {
            m_elem.removeFromParent();
            m_elem = null;
        }
        // if we have a key, we do nothing so that the instance can be reused later

    }

    /**
     * Sets a fixed width on the internal measurement element. If the text will be
     * multiline, you have to set a fixed width in order to accurately measure the
     * text height.<p>
     *
     * @param width the width to set on the element
     */
    public void setFixedWidth(int width) {

        m_elem.getStyle().setWidth(width, Style.Unit.PX);
    }

    /**
     * Binds this text metrics instance to an element from which to copy existing
     * CSS styles that can affect the size of the rendered text.<p>
     *
     * @param element the element
     */
    protected void bind(Element element) {

        bind(element, ATTRIBUTES);
    }

    /**
     * Binds this text metrics instance to an element from which to copy existing
     * CSS styles that can affect the size of the rendered text.<p>
     *
     * @param element the element
     * @param attributes the attributes to bind
     */
    protected void bind(Element element, CmsDomUtil.Style... attributes) {

        if (m_elem == null) {
            // create playground
            m_elem = DOM.createDiv();
            Style style = m_elem.getStyle();
            style.setVisibility(Style.Visibility.HIDDEN);
            style.setPosition(Style.Position.ABSOLUTE);
            style.setLeft(-5000, Style.Unit.PX);
            style.setTop(-5000, Style.Unit.PX);
        }
        // copy all relevant CSS properties
        Style style = m_elem.getStyle();
        for (CmsDomUtil.Style attr : attributes) {
            String attrName = attr.toString();
            style.setProperty(attrName, CmsDomUtil.getCurrentStyle(element, attr));
        }
        // append playground
        RootPanel.getBodyElement().appendChild(m_elem);
    }
}