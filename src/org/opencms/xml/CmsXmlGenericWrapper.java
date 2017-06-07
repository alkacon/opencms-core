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

package org.opencms.xml;

import java.util.Iterator;
import java.util.List;

import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.Node;
import org.dom4j.QName;

/**
 * Provides generic wrappers for XML library methods that do not support Java 5 generic types.<p>
 *
 * @since 7.5.1
 */
public final class CmsXmlGenericWrapper {

    /**
     * Prevents instances of this class from being generated.<p>
     */
    private CmsXmlGenericWrapper() {

        // NOOP
    }

    /**
     * Provides a type safe / generic wrapper for {@link Element#content()}.<p>
     *
     * @param element the element to get the content for
     *
     * @return type safe access to {@link Element#content()}.<p>
     */
    @SuppressWarnings("unchecked")
    public static List<Node> content(Element element) {

        return element.content();
    }

    /**
     * Returns an element iterator.<p>
     *
     * @param element the element
     * @param name the name
     *
     * @return the iterator
     */
    public static Iterable<Element> elementIterable(final Element element, final String name) {

        return new Iterable<Element>() {

            public Iterator<Element> iterator() {

                return elementIterator(element, name);
            }
        };
    }

    /**
     * Provides a type safe / generic wrapper for {@link Element#elementIterator(org.dom4j.QName)}.<p>
     *
     * @param element the element to iterate
     *
     * @return type safe access to {@link Element#elementIterator(org.dom4j.QName)}.<p>
     */
    @SuppressWarnings("unchecked")
    public static Iterator<Element> elementIterator(Element element) {

        return element.elementIterator();
    }

    /**
     * Provides a type safe / generic wrapper for {@link Element#elementIterator(String)}.<p>
     *
     * @param element the element to iterate
     * @param name the element name to match
     *
     * @return type safe access to {@link Element#elementIterator(String)}.<p>
     */
    @SuppressWarnings("unchecked")
    public static Iterator<Element> elementIterator(Element element, String name) {

        return element.elementIterator(name);
    }

    /**
     * Provides a type safe / generic wrapper for {@link Element#elements()}.<p>
     *
     * @param element the element to iterate
     *
     * @return type safe access to {@link Element#elements()}.<p>
     */
    @SuppressWarnings("unchecked")
    public static List<Element> elements(Element element) {

        return element.elements();
    }

    /**
     * Provides a type safe / generic wrapper for {@link Element#elements(org.dom4j.QName)}.<p>
     *
     * @param element the element to iterate
     * @param name the element name to match
     *
     * @return type safe access to {@link Element#elements(org.dom4j.QName)}.<p>
     */
    @SuppressWarnings("unchecked")
    public static List<Element> elements(Element element, QName name) {

        return element.elements(name);
    }

    /**
     * Provides a type safe / generic wrapper for {@link Element#elements(String)}.<p>
     *
     * @param element the element to iterate
     * @param name the element name to match
     *
     * @return type safe access to {@link Element#elements(String)}.<p>
     */
    @SuppressWarnings("unchecked")
    public static List<Element> elements(Element element, String name) {

        return element.elements(name);
    }

    /**
     * Provides a type safe / generic wrapper for {@link Document#selectNodes(String)}.<p>
     *
     * @param doc the document to select the nodes from
     * @param xpathExpression the XPATH expression to select
     *
     * @return type safe access to {@link Document#selectNodes(String)}
     */
    @SuppressWarnings("unchecked")
    public static List<Node> selectNodes(Document doc, String xpathExpression) {

        return doc.selectNodes(xpathExpression);
    }
}