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

package org.opencms.xml.types;

import org.opencms.file.CmsObject;
import org.opencms.xml.I_CmsXmlDocument;

import java.util.Locale;

import org.dom4j.Element;

/**
 * Provides access to the value of a specific XML content node.<p>
 *
 * @since 6.0.0
 */
public interface I_CmsXmlContentValue extends I_CmsXmlSchemaType {

    /**
     * Returns the XML content instance this value belongs to.<p>
     *
     * @return the XML content instance this value belongs to
     */
    I_CmsXmlDocument getDocument();

    /**
     * Returns the original XML element of this XML content value.<p>
     *
     * @return the original XML element of this XML content value
     */
    Element getElement();

    /**
     * Returns the node index of this XML content value in the source XML document,
     * starting with 0, with special handling of elements in choice groups.<p>
     *
     * This is useful in case there are more than one elements
     * with the same XML node name in the source XML document.<p>
     *
     * Elements in XML choice groups will share the same number space, so a choice
     * sequence will be numbered like this:
     * <code>Title[1], Text[2], Title[3], Image[4]</code><p>
     *
     * @return the index of this XML content node in the source document with special handling of elements in choice groups
     *
     * @see #getXmlIndex()
     */
    int getIndex();

    /**
     * Returns the locale of this XML content value was generated for.<p>
     *
     * @return the locale of this XML content value was generated for
     */
    Locale getLocale();

    /**
     * Returns the total number of XML elements of this type that currently exist in the source document.<p>
     *
     * @return the total number of XML elements of this type that currently exist in the source document
     */
    int getMaxIndex();

    /**
     * Returns the path of this XML content value in the source document.<p>
     *
     * @return the path of this XML content value in the source document
     */
    String getPath();

    /**
     * Returns the value of this XML content node as a plain text String.<p>
     *
     * Plain text in this context means a pure textual representation
     * of the content (i.e. without html tags).
     * The plain text may be <code>null</code>, too, if there is no sound or useful
     * textual representation (i.e. color values).<p>
     *
     * @param cms an initialized instance of a CmsObject
     *
     * @return the value of this XML content node as a plain text String
     */
    String getPlainText(CmsObject cms);

    /**
     * Returns the value of this XML content node as a String.<p>
     *
     * @param cms an initialized instance of a CmsObject
     *
     * @return the value of this XML content node as a String
     */
    String getStringValue(CmsObject cms);

    /**
     * Returns the node index of this XML content value in the source XML document,
     * starting with 0, based on the XML ordering.<p>
     *
     * Elements in choice groups will be numbered like this:
     * <code>Title[1], Text[1], Title[2], Image[1]</code><p>
     *
     * @return the index of this XML content node in the source document with special handling of elements in choice groups
     *
     * @see #getIndex()
     */
    int getXmlIndex();

    /**
     * Returns <code>true</code> in case this value is searchable by default with
     * the integrated full text search.<p>
     *
     * @return <code>true</code> in case this value is searchable by default
     */
    boolean isSearchable();

    /**
     * Moves this XML content value one position down in the source document, if possible.<p>
     *
     * If the XML content value is already the first in it's sequence, it is not moved.<p>
     */
    void moveDown();

    /**
     * Moves this XML content value one position up in the source document, if possible.<p>
     *
     * If the XML content value is already the last in it's sequence, it is not moved.<p>
     */
    void moveUp();

    /**
     * Sets the provided String as value of this XML content node.<p>
     *
     * This method does provide processing of the content based on the
     * users current OpenCms context. This can be used e.g. for link
     * extraction and replacement in the content.<p>
     *
     * @param cms an initialized instance of a CmsObject
     * @param value the value to set
     *
     */
    void setStringValue(CmsObject cms, String value);
}