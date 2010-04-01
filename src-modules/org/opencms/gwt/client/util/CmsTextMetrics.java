/*
 * File   : $Source: /alkacon/cvs/opencms/src-modules/org/opencms/gwt/client/util/Attic/CmsTextMetrics.java,v $
 * Date   : $Date: 2010/04/01 13:45:57 $
 * Version: $Revision: 1.4 $
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
 * @version $Revision: 1.4 $ 
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

        // empty
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
    public void bind(Element element, CmsDomUtil.Style... attributes) {

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

        m_elem.removeFromParent();
        m_elem = null;
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

        m_elem.getStyle().setWidth(width, Style.Unit.PX);
    }
}