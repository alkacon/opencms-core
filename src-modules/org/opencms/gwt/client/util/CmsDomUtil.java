/*
 * File   : $Source: /alkacon/cvs/opencms/src-modules/org/opencms/gwt/client/util/Attic/CmsDomUtil.java,v $
 * Date   : $Date: 2010/03/05 15:50:43 $
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

package org.opencms.gwt.client.util;

import java.util.ArrayList;
import java.util.List;

import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.NodeList;

/**
 * Utility class to access the HTML DOM.<p>
 * 
 * @author Tobias Herrmann
 * 
 * @version $Revision: 1.1 $
 * 
 * @since 8.0.0
 */
public class CmsDomUtil {

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
     * Indicates if the given element has a CSS class.<p>
     * 
     * @param className the class name to look for
     * @param element the element
     * 
     * @return true if the element has the given CSS class
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
     * @return true if the element has the given CSS class
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
