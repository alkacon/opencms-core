/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/xml/I_CmsXmlDocument.java,v $
 * Date   : $Date: 2004/12/03 18:40:22 $
 * Version: $Revision: 1.7 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Mananagement System
 *
 * Copyright (C) 2002 - 2004 Alkacon Software (http://www.alkacon.com)
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

import org.opencms.file.CmsObject;
import org.opencms.staticexport.CmsLinkProcessor;
import org.opencms.staticexport.CmsLinkTable;
import org.opencms.xml.content.CmsXmlContentErrorHandler;
import org.opencms.xml.types.I_CmsXmlContentValue;

import java.util.List;
import java.util.Locale;

/**
 * Describes the API to access the values of a XML content document.<p>
 *
 * @author Alexander Kandzior (a.kandzior@alkacon.com)
 * 
 * @version $Revision: 1.7 $
 * @since 5.5.0
 */
public interface I_CmsXmlDocument {

    /**
     * Adds the given locale to this XML document.
     * 
     * @param locale the locale to add
     * 
     * @throws CmsXmlException in case the locale already existed, or if something else goes wrong
     */
    void addLocale(Locale locale) throws CmsXmlException;

    /**
     * Returns the content definition object for this xml content object.<p>
     * 
     * @return the content definition object for this xml content object
     */
    CmsXmlContentDefinition getContentDefinition();

    /**
     * Returns the content converison parameter used for this XML document.<p>
     * 
     * @return the content converison parameter used for this XML document
     */
    String getConversion();

    /**
     * Returns the encoding used for this XML document.<p>
     * 
     * @return the encoding used for this XML document
     */
    String getEncoding();

    /**
     * Returns the index count of existing values for the given key name,
     * or <code>-1</code> if no such value exists.<p>
     * 
     * @param name the key to get the index count for
     * @param locale the locale to get the index count for
     * 
     * @return the index count for the given key name
     */
    int getIndexCount(String name, Locale locale);

    /**
     * Returns a link processor for the values of this XML document.<p>
     * 
     * @param cms the current OpenCms user context that provides access to the link processor
     * @param linkTable the table with the links to process
     * 
     * @return a link processor for the values of this XML document
     */
    CmsLinkProcessor getLinkProcessor(CmsObject cms, CmsLinkTable linkTable);

    /**
     * Returns a List of all locales that have at last one element in 
     * this XML document.<p>
     * 
     * @return a List of all locales that have at last one element in this XML document
     */
    List getLocales();

    /**
     * Returns a List of all locales that have the given element set in this XML document.<p>
     * 
     * If no locale for the given element name is available, an empty list is returned.<p>
     * 
     * @param element the element to look up the locale List for
     * @return a List of all Locales that have the given element set in this XML document
     */
    List getLocales(String element);

    /**
     * Returns all available elements names used in this document for the given locale.<p>
     * 
     * If no element for the given locale is available, an empty list is returned.<p>
     * 
     * @param locale the locale
     * @return list of available element names (Strings)
     * 
     * @see #getValues(Locale)
     */
    List getNames(Locale locale);

    /**
     * Returns the first content value for the given key name as a String,
     * or <code>null</code> if no such value exists.<p>.<p>
     * 
     * @param cms the current OpenCms user context
     * @param name the key to get the content value for
     * @param locale the locale to get the content value for
     * 
     * @return the content value for the given key name
     * 
     * @throws CmsXmlException if something goes wrong
     */
    String getStringValue(CmsObject cms, String name, Locale locale) throws CmsXmlException;

    /**
     * Returns the content value for the given key name from the selected index as a String,
     * or <code>null</code> if no such value exists.<p>
     * 
     * @param cms the current OpenCms user context
     * @param name the key to get the content value for
     * @param locale the locale to get the content value for
     * @param index the index position to get the value from
     * 
     * @return the content value for the given key name
     * 
     * @throws CmsXmlException if something goes wrong
     */
    String getStringValue(CmsObject cms, String name, Locale locale, int index) throws CmsXmlException;

    /**
     * Returns the content value Object for the given key name,
     * or <code>null</code> if no such value exists.<p>.<p>
     * 
     * You can provide an index for the value by appending a numer in aquare brackets 
     * to the name parameter like this "Title[1]". 
     * If no index is provided, 0 is used for the index position.<p>
     * 
     * @param name the key to get the content value for
     * @param locale the locale to get the content value for
     * 
     * @return the content value for the given key name
     */
    I_CmsXmlContentValue getValue(String name, Locale locale);

    /**
     * Returns the content value Object for the given key name from 
     * the selected index, or <code>null</code> if no such value exists.<p>
     * 
     * @param name the key to get the content value for
     * @param locale the locale to get the content value for
     * @param index the index position to get the value from
     * 
     * @return the content value for the given key name
     */
    I_CmsXmlContentValue getValue(String name, Locale locale, int index);

    /**
     * Returns all available elements values used in this document for the given locale.<p>
     * 
     * If no element for the given locale is available, an empty list is returned.<p>
     * 
     * @param locale the locale
     * @return list of available element valies (type {@link I_CmsXmlContentValue})
     * 
     * @see #getNames(Locale)
     */
    List getValues(Locale locale);

    /**
     * Returns all content value Objects for the given key name in a List,
     * or <code>null</code> if no such value exists.<p>
     * 
     * @param name the key to get the content values for
     * @param locale the locale to get the content values for
     * 
     * @return the content value for the given key name
     */
    List getValues(String name, Locale locale);

    /**
     * Checks if the given locale exists in this XML document.<p>
     * 
     * @param locale the locale to check
     * 
     * @return true if the given locale exists in this XML document, false otherwise
     */
    boolean hasLocale(Locale locale);

    /**
     * Returns <code>true</code> if a value exists with the given key name, 
     * <code>false</code> otherwise.<p> 
     * 
     * You can provide an index for the value by appending a numer in aquare brackets 
     * to the name parameter like this "Title[1]". 
     * If no index is provided, 0 is used for the index position.<p>
     * 
     * @param name the key to check
     * @param locale the locale to check
     * 
     * @return true if a value exists with the given key name, false otherwise
     */
    boolean hasValue(String name, Locale locale);

    /**
     * Returns <code>true</code> if a value exists with the given key name at the selected index, 
     * <code>false</code> otherwise.<p> 
     * 
     * @param name the key to check
     * @param locale the locale to check
     * @param index the index position to check
     * 
     * @return true if a value exists with the given key name at the selected index, 
     *      false otherwise
     */
    boolean hasValue(String name, Locale locale, int index);

    /**
     * Returns <code>true</code> if a value exists with the given key name,
     * and that value is enabled, 
     * <code>false</code> otherwise.<p> 
     * 
     * You can provide an index for the value by appending a numer in aquare brackets 
     * to the name parameter like this "Title[1]". 
     * If no index is provided, 0 is used for the index position.<p>
     * 
     * @param name the key to check
     * @param locale the locale to check
     * 
     * @return true if a value exists with the given key name, and that value is enabled, 
     *      false otherwise
     */
    boolean isEnabled(String name, Locale locale);

    /**
     * Returns <code>true</code> if a value exists with the given key name at the selected index,
     * and that value is enabled, 
     * <code>false</code> otherwise.<p> 
     * 
     * @param name the key to check
     * @param locale the locale to check
     * @param index the index position to check
     * 
     * @return true if a value exists with the given key name at the selected index, 
     *      and that value is enabled, false otherwise
     */
    boolean isEnabled(String name, Locale locale, int index);

    /**
     * Removes the given locale from this XML document.
     * 
     * @param locale the locale to remove
     * 
     * @throws CmsXmlException in case the locale did not exist in the document, or if something else goes wrong
     */
    void removeLocale(Locale locale) throws CmsXmlException;

    /**
     * Validates the content of this XML document.<p>
     * 
     * @param cms the current OpenCms user context
     * 
     * @return an error handler instance that provides information about the errors or warnings that have been found
     */
    CmsXmlContentErrorHandler validate(CmsObject cms);

}