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

package org.opencms.xml;

import org.opencms.file.CmsFile;
import org.opencms.file.CmsObject;
import org.opencms.staticexport.CmsLinkProcessor;
import org.opencms.staticexport.CmsLinkTable;
import org.opencms.xml.content.CmsXmlContentErrorHandler;
import org.opencms.xml.content.I_CmsXmlContentHandler;
import org.opencms.xml.types.I_CmsXmlContentValue;

import java.util.List;
import java.util.Locale;

/**
 * Describes the API to access the values of a XML content document.<p>
 *
 * @since 6.0.0
 */
public interface I_CmsXmlDocument {

    /**
     * Adds the given locale to this XML document.<p>
     *
     * @param cms the current users OpenCms context
     * @param locale the locale to add
     *
     * @throws CmsXmlException in case the locale already existed, or if something else goes wrong
     */
    void addLocale(CmsObject cms, Locale locale) throws CmsXmlException;

    /**
     * Copies the content from the first matching source locale that exists in this XML document
     * to the given destination locale in this XML document.<p>
     *
     * The list of possible sources, has to be sorted in order of preference.
     * The first match that exists in this XML document is used as source for the destination locale.
     * No "locale simplification" ("en_EN" to "en" etc.) is performed for the match.<p>
     *
     * @param possibleSources the possible source locales in order of preference,
     *      must contain objects of type {@link Locale}
     * @param destination the destination locale
     *
     * @throws CmsXmlException in case non of the source locales did not exist,
     *      or the destination locale already exists in the document, or if something else goes wrong
     */
    void copyLocale(List<Locale> possibleSources, Locale destination) throws CmsXmlException;

    /**
     * Copies the content of the given source locale to the given destination locale in this XML document.<p>
     *
     * @param source the source locale
     * @param destination the destination locale
     *
     * @throws CmsXmlException in case either the source locale did not exist,
     *      or the destination locale already exists in the document, or if something else goes wrong
     */
    void copyLocale(Locale source, Locale destination) throws CmsXmlException;

    /**
     * Returns the first matching locale (eventually simplified) from the available locales.<p>
     *
     * In case no match is found, code <code>null</code> is returned.<p>
     *
     * @param locale the requested locale
     *
     * @return the matching locale available within the document
     */
    Locale getBestMatchingLocale(Locale locale);

    /**
     * Returns the content definition object used for this XML document.<p>
     *
     * @return the content definition object used for this XML document
     */
    CmsXmlContentDefinition getContentDefinition();

    /**
     * Returns the content conversion parameter used for this XML document.<p>
     *
     * @return the content conversion parameter used for this XML document
     */
    String getConversion();

    /**
     * Returns the encoding used for this XML document.<p>
     *
     * @return the encoding used for this XML document
     */
    String getEncoding();

    /**
     * Returns the file this document was generated from, may be <code>null</code> if the file not available.<p>
     *
     * The file may not be available if the document was generated from a String or a pure XML document.<p>
     *
     * @return the file this document was generated from
     */
    CmsFile getFile();

    /**
     * Returns the content handler associated with the content definition of this XML document.<p>
     *
     * This is a shortcut for <code>getContentDefinition().getContentHandler()</code>.<p>
     *
     * @return the content handler associated with the content definition of this XML document
     */
    I_CmsXmlContentHandler getHandler();

    /**
     * Returns the index count of existing values for the given path,
     * or <code>-1</code> if no such path exists.<p>
     *
     * @param path the path to get the index count for
     * @param locale the locale to get the index count for
     *
     * @return the index count for the given key name
     */
    int getIndexCount(String path, Locale locale);

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
     * Returns a List of all locales that have at last one value in this XML document.<p>
     *
     * @return a List of all locales that have at last one value in this XML document
     */
    List<Locale> getLocales();

    /**
     * Returns a List of all locales that have at least one element with the given path in this XML document.<p>
     *
     * If no locale for the given element name is available, an empty list is returned.<p>
     *
     * @param path the path to look up the locale List for
     * @return a List of all locales that have at least one element with the given path in this XML document
     */
    List<Locale> getLocales(String path);

    /**
     * Returns a List of all available elements paths (Strings) used in this document for the given locale.<p>
     *
     * If no element for the given locale is available, an empty list is returned.<p>
     *
     * @param locale the locale to look up the elements paths for
     * @return a List of all available elements paths (Strings) used in this document for the given locale
     *
     * @see #getValues(Locale)
     */
    List<String> getNames(Locale locale);

    /**
     * Returns the first content value for the given path as a String,
     * or <code>null</code> if no such value exists.<p>
     *
     * @param cms the current OpenCms user context
     * @param path the path to get the content value for
     * @param locale the locale to get the content value for
     *
     * @return the first content value for the given path as a String
     *
     * @throws CmsXmlException if something goes wrong
     */
    String getStringValue(CmsObject cms, String path, Locale locale) throws CmsXmlException;

    /**
     * Returns the content value for the given path and the selected index as a String,
     * or <code>null</code> if no such value exists.<p>
     *
     * @param cms the current OpenCms user context
     * @param path the path to get the content value for
     * @param locale the locale to get the content value for
     * @param index the index position to get the value from
     *
     * @return the content value for the given path and the selected index as a String
     *
     * @throws CmsXmlException if something goes wrong
     */
    String getStringValue(CmsObject cms, String path, Locale locale, int index) throws CmsXmlException;

    /**
     * Returns all content values (of type {@link I_CmsXmlContentValue}) directly below the given path
     * available in this document for the given locale.<p>
     *
     * If no content value for the given path is available with this locale, an empty list is returned.<p>
     *
     * @param path the path to get the sub content values for
     * @param locale the locale to get the sub content values for
     *
     * @return all content values (of type {@link I_CmsXmlContentValue}) directly below the given path
     *      available in this document for the given locale
     */
    List<I_CmsXmlContentValue> getSubValues(String path, Locale locale);

    /**
     * Returns the content value Object for the given path,
     * or <code>null</code> if no such value exists.<p>
     *
     * You can provide an index by appending a number in square brackets
     * to the path parameter like this "Title[1]".
     * If no index is provided, 1 is used for the index position.<p>
     *
     * @param path the path to get the content value for
     * @param locale the locale to get the content value for
     *
     * @return the content value Object for the given path
     */
    I_CmsXmlContentValue getValue(String path, Locale locale);

    /**
     * Returns the content value Object for the given path and the selected index,
     * or <code>null</code> if no such value exists.<p>
     *
     * @param path the path to get the content value for
     * @param locale the locale to get the content value for
     * @param index the index position to get the value from
     *
     * @return the content value Object for the given path and the selected index
     */
    I_CmsXmlContentValue getValue(String path, Locale locale, int index);

    /**
     * Returns all available content values (of type {@link I_CmsXmlContentValue})
     * in this document for the given locale.<p>
     *
     * If no content value for the given locale is available, an empty list is returned.<p>
     *
     * @param locale the locale to get the content values for
     * @return all available content values (of type {@link I_CmsXmlContentValue}) in this document for the given locale
     *
     * @see #getNames(Locale)
     */
    List<I_CmsXmlContentValue> getValues(Locale locale);

    /**
     * Returns all content values (of type {@link I_CmsXmlContentValue}) with the given path
     * available in this document for the given locale.<p>
     *
     * If no content value for the given path is available with this locale, an empty list is returned.<p>
     *
     * @param path the path to get the content values for
     * @param locale the locale to get the content values for
     *
     * @return all content values (of type {@link I_CmsXmlContentValue}) with the given path
     *      available in this document for the given locale
     */
    List<I_CmsXmlContentValue> getValues(String path, Locale locale);

    /**
     * Returns <code>true</code> if the given locale exists in this XML document.<p>
     *
     * @param locale the locale to check
     *
     * @return <code>true</code> if the given locale exists in this XML document, <code>false</code> otherwise
     */
    boolean hasLocale(Locale locale);

    /**
     * Returns <code>true</code> if a value with the given path exists for the selected locale in this XML document, or
     * <code>false</code> otherwise.<p>
     *
     * You can provide an index by appending a number in square brackets
     * to the path parameter like this "Title[1]".
     * If no index is provided, 1 is used for the index position.<p>
     *
     * @param path the path to check
     * @param locale the locale to check
     *
     * @return <code>true</code> if a value with the given path exists for the selected locale in this XML document
     */
    boolean hasValue(String path, Locale locale);

    /**
     * Returns <code>true</code> if a value with the given path and the provided index exists for the selected locale
     * in this XML document, or <code>false</code> otherwise.<p>
     *
     * @param path the path to check
     * @param locale the locale to check
     * @param index the index position to check
     *
     * @return <code>true</code> if a value with the given path and the provided index exists for the selected locale
     *      in this XML document
     */
    boolean hasValue(String path, Locale locale, int index);

    /**
     * Initializes this XML document, required after structural changes to the internal XML.<p>
     *
     * If nodes in the XML are added, removed or moved, the document needs to be initialized in
     * order to update the internal data structures.<p>
     */
    void initDocument();

    /**
     * Returns <code>true</code> if a value with the given path exists for the selected locale in this XML document,
     * and that value is enabled,
     * or <code>false</code> otherwise.<p>
     *
     * This is only used with implementations that support enabling and disabling individual values,
     * such as {@link org.opencms.xml.page.CmsXmlPage}. If enabling / disabling values is not supported,
     * this is identical to {@link #hasValue(String, Locale)}.<p>
     *
     * You can provide an index by appending a number in square brackets
     * to the path parameter like this "Title[1]".
     * If no index is provided, 1 is used for the index position.<p>
     *
     * @param path the path to check
     * @param locale the locale to check
     *
     * @return <code>true</code> if a value with the given path exists for the selected locale in this XML document,
     *      and that value is enabled
     */
    boolean isEnabled(String path, Locale locale);

    /**
     * Returns <code>true</code> if a value with the given path and the provided index exists for the selected locale
     * in this XML document, and that value is enabled, or <code>false</code> otherwise.<p>
     *
     * This is only used with implementations that support enabling and disabling individual values,
     * such as {@link org.opencms.xml.page.CmsXmlPage}. If enabling / disabling values is not supported,
     * this is identical to {@link #hasValue(String, Locale, int)}.<p>
     *
     * @param path the path to check
     * @param locale the locale to check
     * @param index the index position to check
     *
     * @return <code>true</code> if a value with the given path and the provided index exists for the selected locale
     *      in this XML document, and that value is enabled
     */
    boolean isEnabled(String path, Locale locale, int index);

    /**
     * Moves the content of the given source locale to the given destination locale in this XML document.<p>
     *
     * @param source the source locale
     * @param destination the destination locale
     *
     * @throws CmsXmlException in case either the source locale does not exist,
     *      or the destination locale already exists in the document, or if something else goes wrong
     */
    void moveLocale(Locale source, Locale destination) throws CmsXmlException;

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
     * To check for errors in a single document locale only, use
     * {@link CmsXmlContentErrorHandler#hasErrors(Locale)} in the result object.<p>
     *
     * @param cms the current OpenCms user context
     *
     * @return an error handler instance that provides information about the errors or warnings that have been found
     */
    CmsXmlContentErrorHandler validate(CmsObject cms);
}