/*
 * File   : $Source: /alkacon/cvs/opencms/src-modules/org/opencms/gwt/client/util/Attic/CmsDomUtil.java,v $
 * Date   : $Date: 2010/03/31 11:25:55 $
 * Version: $Revision: 1.5 $
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
 * @version $Revision: 1.5 $
 * 
 * @since 8.0.0
 */
public final class CmsDomUtil {

    /** Browser dependent implementation. */
    private static DocumentStyleImpl styleImpl;

    /**
     * Hidden constructor.<p>
     */
    private CmsDomUtil() {

        // doing nothing
    }

    /**
     * Returns the computed style of the given element.<p>
     * 
     * @param element the element
     * @param name the name of the CSS property 
     * 
     * @return the currently computed style
     */
    public static String getCurrentStyle(Element element, String name) {

        if (styleImpl == null) {
            styleImpl = GWT.create(DocumentStyleImpl.class);
        }
        return styleImpl.getCurrentStyle(element, name);
    }

    /**
     * Returns the computed style of the given element as number.<p>
     * 
     * @param element the element
     * @param name the name of the CSS property 
     * 
     * @return the currently computed style
     */
    public static int getCurrentStyleInt(Element element, String name) {

        String currentStyle = getCurrentStyle(element, name);
        return CmsStringUtil.parseInt(currentStyle);
    }

    /**
     * Returns all elements from the DOM with the given CSS class and tag name.<p>
     * 
     * @param className the class name to look for
     * @param tagName the tag name
     * 
     * @return the matching elements
     */
    public static List<Element> getElementByClass(String className, String tagName) {

        return getElementsByClass(className, tagName, Document.get().getBody());
    }

    /**
     * Returns all elements from the DOM with the given CSS class.<p>
     * 
     * @param className the class name to look for
     * 
     * @return the matching elements
     */
    public static List<Element> getElementsByClass(String className) {

        return getElementsByClass(className, "*", Document.get().getBody());
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

        return getElementsByClass(className, "*", rootElement);

    }

    /**
     * Returns all elements with the given CSS class and tag name including the root element.<p>
     * 
     * @param className the class name to look for
     * @param tagName the tag name
     * @param rootElement the root element of the search
     * 
     * @return the matching elements
     */
    public static List<Element> getElementsByClass(String className, String tagName, Element rootElement) {

        if ((rootElement == null)
            || (className == null)
            || (className.trim().length() == 0)
            || (tagName == null)
            || (tagName.trim().length() == 0)) {
            return null;
        }
        className = className.trim();
        List<Element> result = new ArrayList<Element>();
        if (intHasClass(className, rootElement)) {
            result.add(rootElement);
        }
        NodeList<Element> elements = rootElement.getElementsByTagName(tagName.trim());
        for (int i = 0; i < elements.getLength(); i++) {
            if (intHasClass(className, elements.getItem(i))) {
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

        String backgroundColor = CmsDomUtil.getCurrentStyle(element, "background-color");
        String backgroundImage = CmsDomUtil.getCurrentStyle(element, "background-image");
        if ((backgroundColor.equals("transparent"))
            && ((backgroundImage == null) || (backgroundImage.trim().length() == 0) || backgroundImage.equals("none"))) {
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

        String borderStyle = CmsDomUtil.getCurrentStyle(element, "border-style");
        if ((borderStyle == null) || borderStyle.equals("none") || (borderStyle.length() == 0)) {
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

        return intHasClass(className.trim(), element);
    }

    /**
     * Internal method to indicate if the given element has a CSS class.<p>
     * 
     * @param className the class name to look for
     * @param element the element
     * 
     * @return <code>true</code> if the element has the given CSS class
     */
    private static boolean intHasClass(String className, Element element) {

        String elementClass = element.getClassName().trim();
        if (elementClass.contains(className)
            && (elementClass.equals(className)
                || elementClass.startsWith(className + " ")
                || elementClass.endsWith(" " + className) || elementClass.contains(" " + className + " "))) {
            return true;
        }
        return false;
    }
}
