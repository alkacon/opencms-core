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
import org.opencms.i18n.CmsEncoder;
import org.opencms.main.CmsLog;
import org.opencms.main.CmsRuntimeException;
import org.opencms.relations.CmsRelationType;
import org.opencms.util.CmsFileUtil;
import org.opencms.util.CmsStringUtil;
import org.opencms.widgets.I_CmsWidgetParameter;
import org.opencms.xml.CmsXmlContentDefinition;
import org.opencms.xml.CmsXmlGenericWrapper;
import org.opencms.xml.CmsXmlUtils;
import org.opencms.xml.I_CmsXmlDocument;

import java.util.List;
import java.util.Locale;

import org.apache.commons.logging.Log;

import org.dom4j.Element;

/**
 * Base class for XML content value implementations.<p>
 *
 * @since 6.0.0
 */
public abstract class A_CmsXmlContentValue implements I_CmsXmlContentValue, I_CmsWidgetParameter {

    /** The log object for this class. */
    private static final Log LOG = CmsLog.getLog(A_CmsXmlContentValue.class);

    /** The default value for nodes of this value. */
    protected String m_defaultValue;

    /** The XML content instance this value belongs to. */
    protected I_CmsXmlDocument m_document;

    /** The XML element node that contains this value. */
    protected Element m_element;

    /** The locale this value was generated for. */
    protected Locale m_locale;

    /** The maximum occurrences of this value according to the parent schema. */
    protected int m_maxOccurs;

    /** The minimum occurrences of this value according to the parent schema. */
    protected int m_minOccurs;

    /** The configured XML node name of this value. */
    protected String m_name;

    /** The content definition this schema type belongs to. */
    private CmsXmlContentDefinition m_contentDefinition;

    /** The index position of this content value, with special handling for choice groups. */
    private int m_index;

    /** The maximum index of this type that currently exist in the source document. */
    private int m_maxIndex;

    /** Optional localized key prefix identifier. */
    private String m_prefix;

    /** The index position of this content value in the XML order. */
    private int m_xmlIndex;

    /**
     * Default constructor for a XML content type
     * that initializes some internal values.<p>
     */
    protected A_CmsXmlContentValue() {

        m_minOccurs = 0;
        m_maxOccurs = Integer.MAX_VALUE;
        m_index = -1;
        m_xmlIndex = -1;
        m_maxIndex = -1;
    }

    /**
     * Initializes the required members for this XML content value.<p>
     *
     * @param document the XML content instance this value belongs to
     * @param element the XML element that contains this value
     * @param locale the locale this value is created for
     * @param type the type instance to create the value for
     */
    protected A_CmsXmlContentValue(I_CmsXmlDocument document, Element element, Locale locale, I_CmsXmlSchemaType type) {

        m_element = element;
        m_name = element.getName();
        m_document = document;
        m_locale = locale;
        m_minOccurs = type.getMinOccurs();
        m_maxOccurs = type.getMaxOccurs();
        m_contentDefinition = type.getContentDefinition();
        m_index = -1;
        m_xmlIndex = -1;
        m_maxIndex = -1;
    }

    /**
     * Initializes the schema type descriptor values for this type descriptor.<p>
     *
     * @param name the name of the XML node containing the value according to the XML schema
     * @param minOccurs minimum number of occurrences of this type according to the XML schema
     * @param maxOccurs maximum number of occurrences of this type according to the XML schema
     */
    protected A_CmsXmlContentValue(String name, String minOccurs, String maxOccurs) {

        m_name = name;
        m_minOccurs = 1;
        if (CmsStringUtil.isNotEmpty(minOccurs)) {
            try {
                m_minOccurs = Integer.parseInt(minOccurs);
            } catch (NumberFormatException e) {
                // ignore
            }
        }
        m_maxOccurs = 1;
        if (CmsStringUtil.isNotEmpty(maxOccurs)) {
            if (CmsXmlContentDefinition.XSD_ATTRIBUTE_VALUE_UNBOUNDED.equals(maxOccurs)) {
                m_maxOccurs = Integer.MAX_VALUE;
            } else {
                try {
                    m_maxOccurs = Integer.parseInt(maxOccurs);
                } catch (NumberFormatException e) {
                    // ignore
                }
            }
        }
        m_index = -1;
        m_xmlIndex = -1;
        m_maxIndex = -1;
    }

    /**
     * Appends an element XML representation of this type to the given root node.<p>
     *
     * @param root the element to append the XML to
     */
    public void appendXmlSchema(Element root) {

        Element element = root.addElement(CmsXmlContentDefinition.XSD_NODE_ELEMENT);
        element.addAttribute(CmsXmlContentDefinition.XSD_ATTRIBUTE_NAME, getName());
        element.addAttribute(CmsXmlContentDefinition.XSD_ATTRIBUTE_TYPE, getTypeName());
        if ((getMinOccurs() > 1) || (getMinOccurs() == 0)) {
            element.addAttribute(CmsXmlContentDefinition.XSD_ATTRIBUTE_MIN_OCCURS, String.valueOf(getMinOccurs()));
        }
        if (getMaxOccurs() > 1) {
            if (getMaxOccurs() == Integer.MAX_VALUE) {
                element.addAttribute(
                    CmsXmlContentDefinition.XSD_ATTRIBUTE_MAX_OCCURS,
                    CmsXmlContentDefinition.XSD_ATTRIBUTE_VALUE_UNBOUNDED);
            } else {
                element.addAttribute(CmsXmlContentDefinition.XSD_ATTRIBUTE_MAX_OCCURS, String.valueOf(getMaxOccurs()));
            }
        }
    }

    /**
     * @see java.lang.Comparable#compareTo(java.lang.Object)
     */
    public int compareTo(I_CmsXmlSchemaType obj) {

        if (obj == this) {
            return 0;
        }
        return getTypeName().compareTo(obj.getTypeName());
    }

    /**
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj) {

        if (obj == this) {
            return true;
        }
        if (obj instanceof I_CmsXmlSchemaType) {
            I_CmsXmlSchemaType other = (I_CmsXmlSchemaType)obj;
            return (getName().equals(other.getName())
                && getTypeName().equals(other.getTypeName())
                && (getMinOccurs() == other.getMinOccurs())
                && (getMaxOccurs() == other.getMaxOccurs()));
        }
        return false;
    }

    /**
     * @see org.opencms.xml.types.I_CmsXmlSchemaType#generateXml(org.opencms.file.CmsObject, org.opencms.xml.I_CmsXmlDocument, org.dom4j.Element, java.util.Locale)
     */
    public Element generateXml(CmsObject cms, I_CmsXmlDocument document, Element root, Locale locale) {

        Element element = root.addElement(getName());
        // get the default value from the content handler
        String defaultValue = document.getHandler().getDefault(cms, this, locale);
        if (defaultValue != null) {
            try {
                I_CmsXmlContentValue value = createValue(document, element, locale);
                value.setStringValue(cms, defaultValue);
            } catch (CmsRuntimeException e) {
                // should not happen if default value is correct
                LOG.error(
                    Messages.get().getBundle().key(Messages.ERR_XMLCONTENT_INVALID_ELEM_DEFAULT_1, defaultValue),
                    e);
                element.clearContent();
            }
        }
        return element;
    }

    /**
     * @see org.opencms.xml.types.I_CmsXmlSchemaType#getChoiceMaxOccurs()
     */
    public int getChoiceMaxOccurs() {

        return 0;
    }

    /**
     * @see org.opencms.xml.types.I_CmsXmlSchemaType#getContentDefinition()
     */
    public CmsXmlContentDefinition getContentDefinition() {

        return m_contentDefinition;
    }

    /**
     * @see org.opencms.widgets.I_CmsWidgetParameter#getDefault(org.opencms.file.CmsObject)
     */
    public String getDefault(CmsObject cms) {

        return m_contentDefinition.getContentHandler().getDefault(cms, this, getLocale());
    }

    /**
     * @see org.opencms.xml.types.I_CmsXmlSchemaType#getDefault(java.util.Locale)
     */
    public String getDefault(Locale locale) {

        return m_defaultValue;
    }

    /**
     * @see org.opencms.xml.types.I_CmsXmlContentValue#getDocument()
     */
    public I_CmsXmlDocument getDocument() {

        return m_document;
    }

    /**
     * @see org.opencms.xml.types.I_CmsXmlContentValue#getElement()
     */
    public Element getElement() {

        return m_element;
    }

    /**
     * @see org.opencms.widgets.I_CmsWidgetParameter#getId()
     */
    public String getId() {

        if (m_element == null) {
            return null;
        }

        StringBuffer result = new StringBuffer(128);
        result.append(getTypeName());
        result.append('.');
        // the '[', ']' and '/' chars from the xpath are invalid for html id's
        result.append(getPath().replace('[', '_').replace(']', '_').replace('/', '.'));
        result.append('.');
        result.append(getIndex());
        return result.toString();
    }

    /**
     * @see org.opencms.xml.types.I_CmsXmlContentValue#getIndex()
     */
    public int getIndex() {

        if (m_index < 0) {
            if (isChoiceOption()) {
                m_index = m_element.getParent().elements().indexOf(m_element);
            } else {
                m_index = getXmlIndex();
            }
        }
        return m_index;
    }

    /**
     * @see org.opencms.widgets.I_CmsWidgetParameter#getKey()
     */
    public String getKey() {

        StringBuffer result = new StringBuffer(128);
        if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(m_prefix)) {
            result.append(m_prefix);
            result.append('.');
        }
        result.append(m_contentDefinition.getInnerName());
        result.append('.');
        result.append(getName());
        return result.toString();
    }

    /**
     * @see org.opencms.xml.types.I_CmsXmlContentValue#getLocale()
     */
    public Locale getLocale() {

        return m_locale;
    }

    /**
     * @see org.opencms.xml.types.I_CmsXmlContentValue#getMaxIndex()
     */
    public int getMaxIndex() {

        if (m_maxIndex < 0) {
            if (isChoiceOption()) {
                m_maxIndex = m_element.getParent().elements().size();
            } else {
                m_maxIndex = m_element.getParent().elements(m_element.getQName()).size();
            }
        }
        return m_maxIndex;
    }

    /**
     * Returns the maximum occurrences of this type.<p>
     *
     * @return the maximum occurrences of this type
     */
    public int getMaxOccurs() {

        return m_maxOccurs;
    }

    /**
     * Returns the minimum occurrences of this type.<p>
     *
     * @return the minimum occurrences of this type
     */
    public int getMinOccurs() {

        return m_minOccurs;
    }

    /**
     * Returns the name.<p>
     *
     * @return the name
     */
    public String getName() {

        return m_name;
    }

    /**
     * @see org.opencms.xml.types.I_CmsXmlContentValue#getPath()
     */
    public String getPath() {

        if (m_element == null) {
            return "";
        }
        String path = m_element.getUniquePath();
        // must remove the first 2 nodes because these are not required for XML content values
        int pos = path.indexOf('/', path.indexOf('/', 1) + 1) + 1;
        path = path.substring(pos);

        // ensure all path elements have an index, even though this may not be required
        return CmsXmlUtils.createXpath(path, 1);
    }

    /**
     * @see org.opencms.xml.types.I_CmsXmlContentValue#getPlainText(org.opencms.file.CmsObject)
     */
    public String getPlainText(CmsObject cms) {

        return null;
    }

    /**
     * @see org.opencms.xml.types.I_CmsXmlContentValue#getXmlIndex()
     */
    public int getXmlIndex() {

        if (m_xmlIndex < 0) {
            m_xmlIndex = m_element.getParent().elements(m_element.getQName()).indexOf(m_element);
        }
        return m_xmlIndex;
    }

    /**
     * @see org.opencms.widgets.I_CmsWidgetParameter#hasError()
     */
    public boolean hasError() {

        return false;
    }

    /**
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {

        return getTypeName().hashCode();
    }

    /**
     * @see org.opencms.xml.types.I_CmsXmlSchemaType#isChoiceOption()
     */
    public boolean isChoiceOption() {

        return m_contentDefinition.getChoiceMaxOccurs() > 0;
    }

    /**
     * @see org.opencms.xml.types.I_CmsXmlSchemaType#isChoiceType()
     */
    public boolean isChoiceType() {

        // this is overridden in the nested content definition
        return false;
    }

    /**
     * The default implementation always returns <code>true</code>.<p>
     *
     * @see org.opencms.xml.types.I_CmsXmlContentValue#isSearchable()
     */
    public boolean isSearchable() {

        return true;
    }

    /**
     * @see org.opencms.xml.types.I_CmsXmlSchemaType#isSimpleType()
     */
    public boolean isSimpleType() {

        // the abstract base type should be used for simple types only
        return true;
    }

    /**
     * @see org.opencms.xml.types.I_CmsXmlContentValue#moveDown()
     */
    public void moveDown() {

        if (getIndex() > 0) {
            // only move down if this element is not already at the first index position
            moveValue(-1);
            getDocument().initDocument();
        }
    }

    /**
     * @see org.opencms.xml.types.I_CmsXmlContentValue#moveUp()
     */
    public void moveUp() {

        if (getIndex() < (getMaxIndex() - 1)) {
            // only move up if this element is not already at the last index position
            moveValue(1);
            getDocument().initDocument();
        }
    }

    /**
     * @see org.opencms.xml.types.I_CmsXmlSchemaType#setContentDefinition(org.opencms.xml.CmsXmlContentDefinition)
     */
    public void setContentDefinition(CmsXmlContentDefinition contentDefinition) {

        m_contentDefinition = contentDefinition;
    }

    /**
     * Sets the default value for a node of this type.<p>
     *
     * @param defaultValue the default value to set
     */
    public void setDefault(String defaultValue) {

        m_defaultValue = defaultValue;
    }

    /**
     * @see org.opencms.widgets.I_CmsWidgetParameter#setKeyPrefix(java.lang.String)
     */
    public void setKeyPrefix(String prefix) {

        m_prefix = prefix;
    }

    /**
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {

        StringBuffer result = new StringBuffer(128);
        result.append(getClass().getName());
        result.append(": name=");
        result.append(getName());
        result.append(", type=");
        result.append(getTypeName());
        result.append(", path=");
        result.append(m_element == null ? null : getPath());
        String value;
        try {
            value = "'" + getStringValue(null) + "'";
        } catch (Exception e) {
            value = "(CmsObject required to generate)";
        }
        result.append(", value=");
        result.append(value);
        return result.toString();
    }

    /**
     * @see org.opencms.xml.types.I_CmsXmlSchemaType#validateValue(java.lang.String)
     */
    public boolean validateValue(String value) {

        return true;
    }

    /**
     * Returns the relation type for the given path.<p>
     *
     * @param path the element path
     *
     * @return the relation type
     */
    protected CmsRelationType getRelationType(String path) {

        CmsRelationType result = getContentDefinition().getContentHandler().getRelationType(path);
        I_CmsXmlDocument document = getDocument();
        if (document != null) {
            // the relations set in the main content definition override relations set in the nested definition
            result = document.getContentDefinition().getContentHandler().getRelationType(getPath(), result);
        } else {
            LOG.warn("Missing document while evaluating relation type for " + path);
        }
        return result;
    }

    /**
     * Moves this XML content element up or down in the XML document.<p>
     *
     * Please note: No check is performed if the move violates the XML document schema!<p>
     *
     * @param step move the element up or down according to the given step value
     */
    protected void moveValue(int step) {

        Element e = getElement();
        Element parent = e.getParent();
        List<Element> siblings = CmsXmlGenericWrapper.elements(parent);
        int idx = siblings.indexOf(e);
        int newIdx = idx + step;
        siblings.remove(idx);
        siblings.add(newIdx, e);
        m_index += step;
    }

    /**
     * Convenience method to loads the XML schema definition for this value type from an external file.<p>
     *
     * @param schemaUri the schema uri to load the XML schema file from
     *
     * @return the loaded XML schema
     *
     * @throws CmsRuntimeException if something goes wrong
     */
    protected String readSchemaDefinition(String schemaUri) throws CmsRuntimeException {

        // the schema definition is located in a separate file for easier editing
        String schemaDefinition;
        try {
            schemaDefinition = CmsFileUtil.readFile(schemaUri, CmsEncoder.ENCODING_UTF_8);
        } catch (Exception e) {
            throw new CmsRuntimeException(
                Messages.get().container(Messages.ERR_XMLCONTENT_LOAD_SCHEMA_1, schemaUri),
                e);
        }
        return schemaDefinition;
    }
}