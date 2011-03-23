/*
 * File   : $Source: /alkacon/cvs/opencms/src-modules/org/opencms/frontend/templatetwo/CmsListBoxContentMapping.java,v $
 * Date   : $Date: 2011/03/23 14:52:16 $
 * Version: $Revision: 1.6 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (c) 2002 - 2011 Alkacon Software GmbH (http://www.alkacon.com)
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

package org.opencms.frontend.templatetwo;

import org.opencms.file.CmsFile;
import org.opencms.file.CmsObject;
import org.opencms.file.collectors.CmsDateResourceComparator;
import org.opencms.main.OpenCms;
import org.opencms.util.CmsStringUtil;
import org.opencms.xml.content.CmsXmlContent;
import org.opencms.xml.types.CmsXmlDateTimeValue;
import org.opencms.xml.types.CmsXmlHtmlValue;
import org.opencms.xml.types.I_CmsXmlContentValue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/**
 * Describes the mapping from an OpenCms XML content to a list box entry.<p>
 * 
 * @author Alexander Kandzior 
 * @author Peter Bonrad
 * 
 * @version $Revision: 1.6 $ 
 * 
 * @since 7.0.4
 */
public class CmsListBoxContentMapping {

    /**
     * Describes one individual mapping from an XML content field to a field of the list box entry.<p>
     */
    class CmsListBoxFieldMapping {

        /** The default value for the list box entry field in case no XML content match is found. */
        private String m_defaultValue;

        /** The field in the list box entry to map the XML content to. */
        private String m_listBoxField;

        /** The maximum length the field is allowed to have. */
        private int m_maxLenght;

        /** The fields in the XML content to map. */
        private List m_xmlFields;

        /**        
        * Creates a new list box field mapping to a list of XML fields with default value and max length.<p> 
        * 
        * @param xmlFields the List of field in the XML content to map (String with xpath)
        * @param listBoxField the field in the list box to map the XML content to
        * @param maxLength the maximum length the field is allowed to have
        * @param defaultValue the default value for the list box field in case no XML content match is found
        */
        public CmsListBoxFieldMapping(List xmlFields, String listBoxField, int maxLength, String defaultValue) {

            m_xmlFields = xmlFields;
            m_listBoxField = listBoxField;
            m_defaultValue = defaultValue;
            m_maxLenght = maxLength;
        }

        /**
         * Creates a new list box field mapping without default value.<p> 
         * 
         * @param xmlField the field in the XML content to map
         * @param listBoxField the field in the list box to map the XML content to
         */
        public CmsListBoxFieldMapping(String xmlField, String listBoxField) {

            this(xmlField, listBoxField, null);
        }

        /**
         * Creates a new list box field mapping with default value and max length.<p> 
         * 
         * @param xmlField the field in the XML content to map
         * @param listBoxField the field in the list box to map the XML content to
         * @param maxLength the maximum length the field is allowed to have
         * @param defaultValue the default value for the list box entry field in case no XML content match is found
         */
        public CmsListBoxFieldMapping(String xmlField, String listBoxField, int maxLength, String defaultValue) {

            this(new ArrayList(Collections.singletonList(xmlField)), listBoxField, maxLength, defaultValue);
        }

        /**
         * Creates a new list box field mapping with default value.<p> 
         * 
         * @param xmlField the field in the XML content to map
         * @param listBoxField the field in the list box to map the XML content to
         * @param defaultValue the default value for the list box entry field in case no XML content match is found
         */
        public CmsListBoxFieldMapping(String xmlField, String listBoxField, String defaultValue) {

            this(xmlField, listBoxField, 0, defaultValue);
        }

        /**
         * Adds another fields in the XML content to map.<p>
         *
         * @param xmlField the additional field in the XML content to map
         */
        public void addXmlField(String xmlField) {

            if (!m_xmlFields.contains(xmlField)) {
                m_xmlFields.add(xmlField);
            }
        }

        /**
         * This is a special implementation that also returns true if the object compared to 
         * is a String equal to {@link #getListBoxField()}.<p>
         * 
         * @see java.lang.Object#equals(java.lang.Object)
         */
        @Override
        public boolean equals(Object obj) {

            String listBoxField = null;
            if (obj instanceof CmsListBoxFieldMapping) {
                listBoxField = ((CmsListBoxFieldMapping)obj).m_listBoxField;
            }
            if (obj instanceof String) {
                listBoxField = (String)obj;
            }
            return m_listBoxField.equals(listBoxField);
        }

        /**
         * Returns the default value for the list box field in case no XML content match is found.<p>
         *
         * @return the default value for the list box field in case no XML content match is found
         */
        public String getDefaultValue() {

            return m_defaultValue;
        }

        /**
         * Returns the field in the list box to map the XML content to.<p>
         *
         * @return the field in the list box to map the XML content to
         */
        public String getListBoxField() {

            return m_listBoxField;
        }

        /**
         * Returns the maximum length the field is allowed to have.<p>
         *
         * A value of <code>0</code> or less indicates that the field length is unlimited.<p>
         *
         * @return the maximum length the field is allowed to have
         */
        public int getMaxLenght() {

            return m_maxLenght;
        }

        /**
         * Returns the fields in the XML content to map.<p>
         *
         * @return the fields in the XML content to map
         */
        public List getXmlFields() {

            return m_xmlFields;
        }

        /**
         * Returns <code>true</code> in case a default value for the list box field is available.<p>
         * 
         * @return <code>true</code> in case a default value for the list box field is available
         */
        public boolean hasDefaultValue() {

            return m_defaultValue != null;
        }

        /**
         * @see java.lang.Object#hashCode()
         */
        @Override
        public int hashCode() {

            return m_listBoxField.hashCode();
        }

        /**
         * Returns <code>true</code> in case the maximum length of the field has been set.<p>
         * 
         * @return <code>true</code> in case the maximum length of the field has been set
         */
        public boolean hasMaxLenghtLimit() {

            return m_maxLenght > 0;
        }
    }

    /** Constant to map to the list box entry author. */
    public static final String ENTRY_AUTHOR = "Author";

    /** Constant to map to the list box entry date. */
    public static final String ENTRY_DATE = "Date";

    /** Constant to map to the list box entry description (also called value). */
    public static final String ENTRY_TEXT = "Text";

    /** Constant to map to the list box link. */
    public static final String ENTRY_LINK = "Link";

    /** Constant to map to the list box entry title. */
    public static final String ENTRY_TITLE = "Title";

    /** Constant to map to the list box entry image. */
    public static final String ENTRY_IMAGE = "Image";

    /** Constant array with all possible list box mappings. */
    public static final String[] MAPPINGS = {ENTRY_TITLE, ENTRY_AUTHOR, ENTRY_TEXT, ENTRY_DATE, ENTRY_LINK, ENTRY_IMAGE};

    /** Constant list with all possible list box mappings. */
    public static final List MAPPINGS_LIST = Collections.unmodifiableList(Arrays.asList(MAPPINGS));

    /** The map of mappings from the XML content to the list box entry. */
    private Map m_mappings;

    /**
     * Creates a new list box content mapping.<p>
     */
    public CmsListBoxContentMapping() {

        m_mappings = new HashMap();
    }

    /**
     * Adds a new list box field mapping with default value and max length setting.<p> 
     * 
     * @param xmlFields the fields in the XML content to map
     * @param listBoxField the field in the list box to map the XML content to
     * @param maxLength the maximum length the field is allowed to have
     * @param defaultValue the default value for the list box entry field in case no XML content match is found
     */
    public void addListBoxFieldMapping(List xmlFields, String listBoxField, int maxLength, String defaultValue) {

        if (MAPPINGS_LIST.contains(listBoxField)) {
            CmsListBoxFieldMapping mapping = new CmsListBoxFieldMapping(
                xmlFields,
                listBoxField,
                maxLength,
                defaultValue);
            m_mappings.put(listBoxField, mapping);
        }
    }

    /**
     * Adds a new list box field mapping with default value and max length setting.<p> 
     * 
     * @param xmlFields the fields in the XML content to map
     * @param listBoxField the field in the list box to map the XML content to 
     * @param maxLength the maximum length the field is allowed to have (will be converted to an int)
     * @param defaultValue the default value for the list box entry field in case no XML content match is found
     */
    public void addListBoxFieldMapping(List xmlFields, String listBoxField, String maxLength, String defaultValue) {

        // store mappings as xpath to allow better control about what is mapped
        if (CmsStringUtil.isEmptyOrWhitespaceOnly(defaultValue)) {
            // we don't allow only whitespace defaults
            defaultValue = null;
        }
        int maxLengthInt = 0;
        if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(maxLength)) {
            try {
                maxLengthInt = Integer.parseInt(maxLength);
            } catch (NumberFormatException e) {
                // should not happen as the XSD already checks this
            }
        }
        addListBoxFieldMapping(xmlFields, listBoxField, maxLengthInt, defaultValue);
    }

    /**
     * Returns a list box entry created from the given XML content using the configured rules of this content mapping.<p>
     * 
     * @param cms the current users OpenCms context
     * @param content the XML content to create the list box entry from
     * @param locale the locale to use in the XML content
     * 
     * @return a list box entry created from the given XML content using the configured rules of this content mapping
     */
    public CmsListBoxEntry getEntryFromXmlContent(CmsObject cms, CmsXmlContent content, Locale locale) {

        if ((content == null) || (locale == null) || !content.hasLocale(locale)) {
            // no entry can be created if input is silly
            return null;
        }

        // get all configured mappings
        Set mappings = m_mappings.entrySet();
        // create the empty syndication entry
        CmsListBoxEntry result = new CmsListBoxEntry();
        boolean hasTitle = false;

        Iterator i = mappings.iterator();
        String link = null;
        Date date = null;
        while (i.hasNext()) {
            Map.Entry e = (Map.Entry)i.next();
            String listBoxField = (String)e.getKey();
            CmsListBoxFieldMapping mapping = (CmsListBoxFieldMapping)e.getValue();

            I_CmsXmlContentValue xmlContentValue = null;
            List xmlFields = mapping.getXmlFields();
            for (int j = 0, size = xmlFields.size(); j < size; j++) {
                String xmlField = (String)xmlFields.get(j);
                if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(xmlField)) {
                    xmlContentValue = content.getValue(xmlField, locale);
                    if (xmlContentValue != null) {
                        // found a matching XML content node
                        break;
                    }
                }
            }
            String value = null;
            if (xmlContentValue != null) {
                // value was found in the content
                value = xmlContentValue.getStringValue(cms);
            } else if (mapping.hasDefaultValue()) {
                // value not found in content, use default value
                value = mapping.getDefaultValue();
            }
            if (value != null) {
                if (mapping.hasMaxLenghtLimit()) {
                    // apply length restriction if required
                    value = applyLengthRestriction(cms, xmlContentValue, value, mapping.getMaxLenght());
                }
                // a value to map was found
                int pos = MAPPINGS_LIST.indexOf(listBoxField);
                switch (pos) {
                    case 0: // Title
                        result.setTitle(value);
                        hasTitle = true;
                        break;
                    case 1: // Author
                        result.setAuthor(value);
                        break;
                    case 2: // Description
                        result.setDescription(value);
                        break;
                    case 3: // Date
                        date = convertToDate(cms, content.getFile(), xmlContentValue, value);
                        if (date != null) {
                            result.setDate(date);
                        }
                        break;
                    case 4: // Link
                        // use link as provided in content
                        if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(value)) {
                            link = OpenCms.getLinkManager().getServerLink(cms, value);
                        }
                        break;
                    case 5: // Image
                        if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(value)) {
                            result.setImage(value);
                        }
                        break;
                    default: // unknown, this cannot happen as all mappings are checked when created            
                }
            }
        }

        if (hasTitle) {
            // we need at least an entry and an description
            if (link == null) {
                // calculate the link
                link = OpenCms.getLinkManager().getServerLink(cms, cms.getSitePath(content.getFile()));
            }
            if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(link)) {
                result.setLink(link);
            }
        } else {
            // required mappings are not available
            result = null;
        }
        return result;
    }

    /**
     * Returns the path of the first XML element that is mapped to the list box entry Author.<p>
     * 
     * If no XML element has been mapped to the list box entry Author, <code>null</code> is returned.<p>
     * 
     * @return the path of the first XML element that is mapped to the list box entry Author
     */
    public String getMappingForAuthor() {

        CmsListBoxFieldMapping mapping = (CmsListBoxFieldMapping)m_mappings.get(ENTRY_AUTHOR);
        return (mapping != null) ? (String)mapping.getXmlFields().get(0) : null;
    }

    /**
     * Returns the path of the first XML element that is mapped to the list box entry date.<p>
     * 
     * If no XML element has been mapped to the list box entry date, <code>null</code> is returned.<p>
     * 
     * @return the path of the first XML element that is mapped to the list box entry date
     */
    public String getMappingForDate() {

        CmsListBoxFieldMapping mapping = (CmsListBoxFieldMapping)m_mappings.get(ENTRY_DATE);
        return (mapping != null) ? (String)mapping.getXmlFields().get(0) : null;
    }

    /**
     * Returns the path of the first XML element that is mapped to the list box entry Description.<p>
     * 
     * If no XML element has been mapped to the list box entry Description, <code>null</code> is returned.<p>
     * 
     * @return the path of the first XML element that is mapped to the list box entry Description
     */
    public String getMappingForDescription() {

        CmsListBoxFieldMapping mapping = (CmsListBoxFieldMapping)m_mappings.get(ENTRY_TEXT);
        return (mapping != null) ? (String)mapping.getXmlFields().get(0) : null;
    }

    /**
     * Returns the path of the first XML element that is mapped to the list box entry Title.<p>
     * 
     * If no XML element has been mapped to the list box entry Title, <code>null</code> is returned.<p>
     * 
     * @return the path of the first XML element that is mapped to the list box entry Title
     */
    public String getMappingForTitle() {

        CmsListBoxFieldMapping mapping = (CmsListBoxFieldMapping)m_mappings.get(ENTRY_TITLE);
        return (mapping != null) ? (String)mapping.getXmlFields().get(0) : null;
    }

    /**
     * Applies the max length limitation of the current field to the given value.<p>
     * 
     * @param cms the current users OpenCms context
     * @param xmlContentValue the XML content value
     * @param value the String value generated form the XML content value
     * @param maxLength the max length setting of the current field
     * 
     * @return the input with the max length limitation of the current field applied
     */
    protected String applyLengthRestriction(
        CmsObject cms,
        I_CmsXmlContentValue xmlContentValue,
        String value,
        int maxLength) {

        if (value.length() <= maxLength) {
            return value;
        }
        String result;
        // value is to long, apply limitation
        if (xmlContentValue instanceof CmsXmlHtmlValue) {
            // the content is HTML
            result = xmlContentValue.getPlainText(cms);
        } else {
            // assume default "text/plain"
            result = value;
        }
        if (result.length() > maxLength) {
            result = CmsStringUtil.trimToSize(result, maxLength);
        }
        return result;
    }

    /**
     * Converts an XML content value to a Date.<p>
     * 
     * In case the XML content value itself can not be converted to a date, 
     * the given String value is used to access the given files attributes or properties
     * using {@link CmsDateResourceComparator#calculateDate(CmsObject, org.opencms.file.CmsResource, List, long)}.<p>
     * 
     * @param cms the current users OpenCms context
     * @param file the current file from which the list box entry is created
     * @param xmlContentValue the XML content value to convert
     * @param stringValue the String value of the XML content value
     * 
     * @return the converted Date, or <code>null</code> if no convertible data was found
     */
    private Date convertToDate(CmsObject cms, CmsFile file, I_CmsXmlContentValue xmlContentValue, String stringValue) {

        Date result = null;
        if (xmlContentValue instanceof CmsXmlDateTimeValue) {
            // dates should be using the right XML content value type
            result = new Date(((CmsXmlDateTimeValue)xmlContentValue).getDateTimeValue());
        } else {
            try {
                // try to parse the date as long
                result = new Date(Long.valueOf(stringValue).longValue());
            } catch (NumberFormatException e) {
                // no luck parsing, so we have no date - try using other options...
                if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(stringValue)) {
                    List items = CmsStringUtil.splitAsList(stringValue, '|', true);
                    long date = CmsDateResourceComparator.calculateDate(cms, file, items, -1);
                    if (date != -1) {
                        result = new Date(date);
                    }
                }
            }
        }
        return result;
    }
}