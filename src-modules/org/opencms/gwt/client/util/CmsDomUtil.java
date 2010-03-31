/*
 * File   : $Source: /alkacon/cvs/opencms/src-modules/org/opencms/gwt/client/util/Attic/CmsDomUtil.java,v $
 * Date   : $Date: 2010/03/31 13:35:36 $
 * Version: $Revision: 1.6 $
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

package org.opencms.gwt.client.util;

import org.opencms.gwt.client.util.impl.DocumentStyleImpl;

import java.util.ArrayList;
import java.util.List;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.NodeList;

/**
 * Utility class to access the HTML DOM.<p>
 * 
 * @author Tobias Herrmann
 * 
 * @version $Revision: 1.6 $
 * 
 * @since 8.0.0
 */
public final class CmsDomUtil {

    /**
     * CSS Colors.<p>
     */
    public static enum Color {

        /** CSS Color. */
        red,
    }

    /**
     * CSS Properties.<p>
     */
    public static enum Style {

        /** CSS Property. */
        backgroundColor,

        /** CSS Property. */
        backgroundImage,

        /** CSS Property. */
        borderStyle,

        /** CSS Property. */
        floatCss {

            /**
             * @see java.lang.Enum#toString()
             */
            @Override
            public String toString() {

                return "float";
            }
        },

        /** CSS Property. */
        fontFamily,

        /** CSS Property. */
        fontSize,

        /** CSS Property. */
        fontSizeAdjust,

        /** CSS Property. */
        fontStretch,

        /** CSS Property. */
        fontStyle,

        /** CSS Property. */
        fontVariant,

        /** CSS Property. */
        fontWeight,

        /** CSS Property. */
        height,

        /** CSS Property. */
        left,

        /** CSS Property. */
        letterSpacing,

        /** CSS Property. */
        lineHeight,

        /** CSS Property. */
        marginBottom,

        /** CSS Property. */
        position,

        /** CSS Property. */
        textAlign,

        /** CSS Property. */
        textDecoration,

        /** CSS Property. */
        textIndent,

        /** CSS Property. */
        textShadow,

        /** CSS Property. */
        textTransform,

        /** CSS Property. */
        top,

        /** CSS Property. */
        visibility,

        /** CSS Property. */
        whiteSpace,

        /** CSS Property. */
        width,

        /** CSS Property. */
        wordSpacing,

        /** CSS Property. */
        wordWrap
    }

    /**
     * CSS Property values.<p>
     */
    public static enum StyleValue {

        /** CSS Property value. */
        absolute,

        /** CSS Property value. */
        hidden,

        /** CSS Property value. */
        none,

        /** CSS Property value. */
        transparent;
    }

    /**
     * HTML Tags.<p>
     */
    public static enum Tag {

        /** HTML Tag. */
        ALL {

            /**
             * @see java.lang.Enum#toString()
             */
            @Override
            public String toString() {

                return "*";
            }
        },

        /** HTML Tag. */
        b,

        /** HTML Tag. */
        div,

        /** HTML Tag. */
        li,

        /** HTML Tag. */
        ul;
    }

    /** Browser dependent implementation. */
    private static DocumentStyleImpl styleImpl;

    /**
     * Hidden constructor.<p>
     */
    private CmsDomUtil() {

        // doing nothing
    }

    /**
     * Generates a closing tag.<p>
     * 
     * @param tag the tag to use
     * 
     * @return HTML code
     */
    public static String close(Tag tag) {

        return "</" + tag.name() + ">";
    }

    /**
     * Encloses the given text with the given tag.<p>
     * 
     * @param tag the tag to use
     * @param text the text to enclose
     * 
     * @return HTML code
     */
    public static String enclose(Tag tag, String text) {

        return open(tag) + text + close(tag);
    }

    /**
     * Returns the computed style of the given element.<p>
     * 
     * @param element the element
     * @param style the CSS property 
     * 
     * @return the currently computed style
     */
    public static String getCurrentStyle(Element element, Style style) {

        if (styleImpl == null) {
            styleImpl = GWT.create(DocumentStyleImpl.class);
        }
        return styleImpl.getCurrentStyle(element, style.toString());
    }

    /**
     * Returns the computed style of the given element as number.<p>
     * 
     * @param element the element
     * @param style the CSS property 
     * 
     * @return the currently computed style
     */
    public static int getCurrentStyleInt(Element element, Style style) {

        String currentStyle = getCurrentStyle(element, style);
        return CmsStringUtil.parseInt(currentStyle);
    }

    /**
     * Returns all elements from the DOM with the given CSS class and tag name.<p>
     * 
     * @param className the class name to look for
     * @param tag the tag
     * 
     * @return the matching elements
     */
    public static List<Element> getElementByClass(String className, Tag tag) {

        return getElementsByClass(className, tag, Document.get().getBody());
    }

    /**
     * Returns all elements from the DOM with the given CSS class.<p>
     * 
     * @param className the class name to look for
     * 
     * @return the matching elements
     */
    public static List<Element> getElementsByClass(String className) {

        return getElementsByClass(className, Tag.ALL, Document.get().getBody());
    }

    /**
     * Returns all elements with the given CSS class including the root element.<p>
     * 
     * @param className the class name to look for
     * @param rootElement the root element of the search
     * 
     * @return the matching elements
     */
    public static List<Element> getElementsByClass(String className, Element rootElement) {

        return getElementsByClass(className, Tag.ALL, rootElement);

    }

    /**
     * Returns all elements with the given CSS class and tag name including the root element.<p>
     * 
     * @param className the class name to look for
     * @param tag the tag
     * @param rootElement the root element of the search
     * 
     * @return the matching elements
     */
    public static List<Element> getElementsByClass(String className, Tag tag, Element rootElement) {

        if ((rootElement == null) || (className == null) || (className.trim().length() == 0) || (tag == null)) {
            return null;
        }
        className = className.trim();
        List<Element> result = new ArrayList<Element>();
        if (internalHasClass(className, rootElement)) {
            result.add(rootElement);
        }
        NodeList<Element> elements = rootElement.getElementsByTagName(tag.toString());
        for (int i = 0; i < elements.getLength(); i++) {
            if (internalHasClass(className, elements.getItem(i))) {
                result.add(elements.getItem(i));
            }
        }
        return result;
    }

    /**
     * Utility method to determine if the given element has a set background.<p>
     * 
     * @param element the element
     * 
     * @return <code>true</code> if the element has a background set
     */
    public static boolean hasBackground(Element element) {

        String backgroundColor = CmsDomUtil.getCurrentStyle(element, Style.backgroundColor);
        String backgroundImage = CmsDomUtil.getCurrentStyle(element, Style.backgroundImage);
        if ((backgroundColor.equals(StyleValue.transparent.toString()))
            && ((backgroundImage == null) || (backgroundImage.trim().length() == 0) || backgroundImage.equals(StyleValue.none.toString()))) {
            return false;
        }
        return true;
    }

    /**
     * Utility method to determine if the given element has a set border.<p>
     * 
     * @param element the element
     * 
     * @return <code>true</code> if the element has a border
     */
    public static boolean hasBorder(Element element) {

        String borderStyle = CmsDomUtil.getCurrentStyle(element, Style.borderStyle);
        if ((borderStyle == null) || borderStyle.equals(StyleValue.none.toString()) || (borderStyle.length() == 0)) {
            return false;
        }
        return true;

    }

    /**
     * Indicates if the given element has a CSS class.<p>
     * 
     * @param className the class name to look for
     * @param element the element
     * 
     * @return <code>true</code> if the element has the given CSS class
     */
    public static boolean hasClass(String className, Element element) {

        return internalHasClass(className.trim(), element);
    }

    /**
     * Generates an opening tag.<p>
     * 
     * @param tag the tag to use
     * 
     * @return HTML code
     */
    public static String open(Tag tag) {

        return "<" + tag.name() + ">";
    }

    /**
     * Internal method to indicate if the given element has a CSS class.<p>
     * 
     * @param className the class name to look for
     * @param element the element
     * 
     * @return <code>true</code> if the element has the given CSS class
     */
    private static boolean internalHasClass(String className, Element element) {

        String elementClass = element.getClassName().trim();
        boolean hasClass = elementClass.contains(" " + className + " ");
        hasClass |= elementClass.startsWith(className + " ");
        hasClass |= elementClass.endsWith(" " + className);
        return hasClass;
    }
}
