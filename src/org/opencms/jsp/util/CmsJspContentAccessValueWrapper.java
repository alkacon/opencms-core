/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/jsp/util/CmsJspContentAccessValueWrapper.java,v $
 * Date   : $Date: 2011/03/23 14:53:20 $
 * Version: $Revision: 1.15 $
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

package org.opencms.jsp.util;

import org.opencms.file.CmsObject;
import org.opencms.i18n.CmsLocaleManager;
import org.opencms.util.CmsConstantMap;
import org.opencms.util.CmsMacroResolver;
import org.opencms.util.CmsStringUtil;
import org.opencms.xml.CmsXmlUtils;
import org.opencms.xml.types.I_CmsXmlContentValue;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.apache.commons.collections.Transformer;
import org.apache.commons.collections.map.LazyMap;

import org.dom4j.Node;

/**
 * Allows direct access to XML content values, with possible iteration of sub-nodes.<p>
 * 
 * The implementation is optimized for performance and uses lazy initializing of the 
 * requested values as much as possible.<p>
 * 
 * @author Alexander Kandzior
 * 
 * @version $Revision: 1.15 $ 
 * 
 * @since 7.0.2
 * 
 * @see CmsJspContentAccessBean
 * @see org.opencms.jsp.CmsJspTagContentAccess
 */
public final class CmsJspContentAccessValueWrapper {

    /**
     * Provides a Map with Booleans that 
     * indicate if a nested sub value (xpath) for the current value is available in the XML content.<p>
     */
    public class CmsHasValueTransformer implements Transformer {

        /**
         * @see org.apache.commons.collections.Transformer#transform(java.lang.Object)
         */
        public Object transform(Object input) {

            return Boolean.valueOf(obtainContentValue().getDocument().hasValue(
                createPath(input),
                obtainContentValue().getLocale()));
        }
    }

    /**
     * Provides a Map which lets the user access nested sub value Lists from the current value, 
     * the input is assumed to be a String that represents an xpath in the XML content.<p>
     */
    public class CmsValueListTransformer implements Transformer {

        /**
         * @see org.apache.commons.collections.Transformer#transform(java.lang.Object)
         */
        public Object transform(Object input) {

            List values = obtainContentValue().getDocument().getValues(
                createPath(input),
                obtainContentValue().getLocale());
            List result = new ArrayList();
            Iterator i = values.iterator();
            while (i.hasNext()) {
                // must iterate values from XML content and create wrapper for each 
                I_CmsXmlContentValue value = (I_CmsXmlContentValue)i.next();
                result.add(createWrapper(obtainCmsObject(), value));
            }
            return result;
        }
    }

    /**
     * Provides a Map which lets the user a nested sub value from the current value, 
     * the input is assumed to be a String that represents an xpath in the XML content.<p>
     */
    public class CmsValueTransformer implements Transformer {

        /**
         * @see org.apache.commons.collections.Transformer#transform(java.lang.Object)
         */
        public Object transform(Object input) {

            I_CmsXmlContentValue value = obtainContentValue().getDocument().getValue(
                createPath(input),
                obtainContentValue().getLocale());
            return createWrapper(obtainCmsObject(), value);
        }
    }

    /**
     * Provides a Map which lets the user directly access sub-nodes of the XML represented by the current value,
     * the input is assumed to be a String that represents an xpath in the XML content.<p>
     */
    public class CmsXmlValueTransformer implements Transformer {

        /**
         * @see org.apache.commons.collections.Transformer#transform(java.lang.Object)
         */
        public Object transform(Object input) {

            Node node = obtainContentValue().getElement().selectSingleNode(input.toString());
            if (node != null) {
                return node.getStringValue();
            }
            return "";
        }
    }

    /** Constant for the null (non existing) value. */
    protected static final CmsJspContentAccessValueWrapper NULL_VALUE_WRAPPER = new CmsJspContentAccessValueWrapper();

    /** The wrapped OpenCms user context. */
    private CmsObject m_cms;

    /** The wrapped XML content value. */
    private I_CmsXmlContentValue m_contentValue;

    /** Calculated hash code. */
    private int m_hashCode;

    /** The lazy initialized Map that checks if a value is available. */
    private Map m_hasValue;

    /** The macro resolver used to resolve macros for this value. */
    private CmsMacroResolver m_macroResolver;

    /** The lazy initialized value Map. */
    private Map m_value;

    /** The lazy initialized value list Map. */
    private Map m_valueList;

    /** The lazy initialized XML element Map. */
    private Map m_xml;

    /**
     * Private constructor, used for creation of NULL constant value, use factory method to create instances.<p>
     * 
     * @see #createWrapper(CmsObject, I_CmsXmlContentValue)
     */
    private CmsJspContentAccessValueWrapper() {

        // cast needed to avoid compiler confusion with constructors
        this((CmsObject)null, (I_CmsXmlContentValue)null);
    }

    /**
     * Private constructor, use factory method to create instances.<p>
     * 
     * Used to create a copy with macro resolving enabled.<p>
     * 
     * @param base the wrapper base
     * @param macroResolver the macro resolver to use 
     * 
     * @see #createWrapper(CmsObject, I_CmsXmlContentValue)
     */
    private CmsJspContentAccessValueWrapper(CmsJspContentAccessValueWrapper base, CmsMacroResolver macroResolver) {

        m_cms = base.m_cms;
        m_contentValue = base.m_contentValue;
        m_hashCode = base.m_hashCode;
        m_hasValue = base.m_hasValue;
        m_macroResolver = macroResolver;
        m_value = base.m_value;
        m_valueList = base.m_valueList;
    }

    /**
     * Private constructor, use factory method to create instances.<p>
     * 
     * @param cms the current users OpenCms context
     * @param value the value to warp
     * 
     * @see #createWrapper(CmsObject, I_CmsXmlContentValue)
     */
    private CmsJspContentAccessValueWrapper(CmsObject cms, I_CmsXmlContentValue value) {

        // a null value is used for constant generation
        m_cms = cms;
        m_contentValue = value;

        if ((m_contentValue == null) || m_contentValue.isSimpleType()) {
            // maps must all be static
            m_hasValue = CmsConstantMap.CONSTANT_BOOLEAN_FALSE_MAP;
            m_value = CmsJspContentAccessBean.CONSTANT_NULL_VALUE_WRAPPER_MAP;
            m_valueList = CmsConstantMap.CONSTANT_EMPTY_LIST_MAP;
        }
    }

    /**
     * Factory method to create a new XML content value wrapper.<p>
     * 
     * In case either parameter is <code>null</code>, the {@link #NULL_VALUE_WRAPPER} is returned.<p>
     * 
     * @param cms the current users OpenCms context
     * @param value the value to warp
     * 
     * @return a new content value wrapper instance, or <code>null</code> if any parameter is <code>null</code>
     */
    public static CmsJspContentAccessValueWrapper createWrapper(CmsObject cms, I_CmsXmlContentValue value) {

        if ((value != null) && (cms != null)) {
            return new CmsJspContentAccessValueWrapper(cms, value);
        }
        // if no value is available, 
        return NULL_VALUE_WRAPPER;
    }

    /**
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj) {

        if (obj == this) {
            return true;
        }
        if (obj instanceof CmsJspContentAccessValueWrapper) {
            // rely on hash code implementation for equals method
            return hashCode() == ((CmsJspContentAccessValueWrapper)obj).hashCode();
        }
        return false;
    }

    /**
     * Returns <code>true</code> in case this value actually exists in the XML content it was requested from.<p>
     * 
     * Usage example on a JSP with the JSTL:<pre>
     * &lt;cms:contentload ... &gt;
     *     &lt;cms:contentaccess var="content" /&gt;
     *     &lt;c:if test="${content.value['Link'].exists}" &gt;
     *         The content has a "Link" value! 
     *     &lt;/c:if&gt;
     * &lt;/cms:contentload&gt;</pre>
     * 
     * @return <code>true</code> in case this value actually exists in the XML content it was requested from
     */
    public boolean getExists() {

        return m_contentValue != null;
    }

    /**
     * Returns a lazy initialized Map that provides Booleans that 
     * indicate if a nested sub value (xpath) for the current value is available in the XML content.<p>
     * 
     * The provided Map key is assumed to be a String that represents the relative xpath to the value.<p>
     * 
     * In case the current value is not a nested XML content value, or the XML content value does not exist,
     * the {@link CmsConstantMap#CONSTANT_BOOLEAN_FALSE_MAP} is returned.<p>
     * 
     * Usage example on a JSP with the JSTL:<pre>
     * &lt;cms:contentload ... &gt;
     *     &lt;cms:contentaccess var="content" /&gt;
     *     &lt;c:if test="${content.value['Link'].hasValue['Description']}" &gt;
     *         The content has a "Description" value as sub element to the "Link" value! 
     *     &lt;/c:if&gt;
     * &lt;/cms:contentload&gt;</pre>
     * 
     * Please note that you can also test if a sub-value exists like this:<pre>
     * &lt;c:if test="${content.value['Link'].value['Description'].exists}" &gt; ... &lt;/c:if&gt;</pre>
     *  
     * @return a lazy initialized Map that provides Booleans that 
     *      indicate if a sub value (xpath) for the current value is available in the XML content
     */
    public Map getHasValue() {

        if (m_hasValue == null) {
            m_hasValue = LazyMap.decorate(new HashMap(), new CmsHasValueTransformer());
        }
        return m_hasValue;
    }

    /**
     * Returns the node index of the XML content value in the source XML document, 
     * starting with 0.<p>
     * 
     * In case the XML content value does not exist, <code>-1</code> is returned.<p> 
     * 
     * Usage example on a JSP with the JSTL:<pre>
     * &lt;cms:contentload ... &gt;
     *     &lt;cms:contentaccess var="content" /&gt;
     *     The locale of the Link node: ${content.value['Link'].locale}
     * &lt;/cms:contentload&gt;</pre>
     * 
     * @return the locale of the current XML content value
     */
    public int getIndex() {

        if (m_contentValue == null) {
            return -1;
        }
        return m_contentValue.getIndex();
    }

    /**
     * Returns <code>true</code> in case the value is empty, that is either <code>null</code> or an empty String.<p> 
     * 
     * In case the XML content value does not exist, <code>true</code> is returned.<p>
     * 
     * Usage example on a JSP with the JSTL:<pre>
     * &lt;cms:contentload ... &gt;
     *     &lt;cms:contentaccess var="content" /&gt;
     *     &lt;c:if test="${content.value['Link'].isEmpty}" &gt;
     *         The content of the "Link" value is empty. 
     *     &lt;/c:if&gt;
     * &lt;/cms:contentload&gt;</pre>
     * 
     * @return <code>true</code> in case the value is empty
     */
    public boolean getIsEmpty() {

        if (m_contentValue == null) {
            // this is the case for non existing values
            return true;
        }
        if (m_contentValue.isSimpleType()) {
            // return values for simple type
            return CmsStringUtil.isEmpty(m_contentValue.getStringValue(m_cms));
        } else {
            // nested types are not empty if they have any children in the XML
            return m_contentValue.getElement().elements().size() > 0;
        }
    }

    /**
     * Returns <code>true</code> in case the value is empty or whitespace only, 
     * that is either <code>null</code> or String that contains only whitespace chars.<p> 
     * 
     * In case the XML content value does not exist, <code>true</code> is returned.<p>
     * 
     * Usage example on a JSP with the JSTL:<pre>
     * &lt;cms:contentload ... &gt;
     *     &lt;cms:contentaccess var="content" /&gt;
     *     &lt;c:if test="${content.value['Link'].isEmptyOrWhitespaceOnly}" &gt;
     *         The content of the "Link" value is empty or contains only whitespace chars. 
     *     &lt;/c:if&gt;
     * &lt;/cms:contentload&gt;</pre>
     * 
     * @return <code>true</code> in case the value is empty or whitespace only
     */
    public boolean getIsEmptyOrWhitespaceOnly() {

        if (m_contentValue == null) {
            // this is the case for non existing values
            return true;
        }
        if (m_contentValue.isSimpleType()) {
            // return values for simple type
            return CmsStringUtil.isEmptyOrWhitespaceOnly(m_contentValue.getStringValue(m_cms));
        } else {
            // nested types are not empty if they have any children in the XML
            return m_contentValue.getElement().elements().size() > 0;
        }
    }

    /**
     * Returns the Locale of the current XML content value.<p>
     * 
     * In case the XML content value does not exist, the OpenCms system default Locale is returned.<p> 
     * 
     * Usage example on a JSP with the JSTL:<pre>
     * &lt;cms:contentload ... &gt;
     *     &lt;cms:contentaccess var="content" /&gt;
     *     The locale of the Link node: ${content.value['Link'].locale}
     * &lt;/cms:contentload&gt;</pre>
     * 
     * @return the locale of the current XML content value
     */
    public Locale getLocale() {

        if (m_contentValue == null) {
            return CmsLocaleManager.getDefaultLocale();
        }
        return m_contentValue.getLocale();
    }

    /**
     * Returns the path to the current XML content value.<p>
     * 
     * In case the XML content value does not exist, an empty String <code>""</code> is returned.<p> 
     * 
     * Usage example on a JSP with the JSTL:<pre>
     * &lt;cms:contentload ... &gt;
     *     &lt;cms:contentaccess var="content" /&gt;
     *     The path to the Link node in the XML: ${content.value['Link'].path}
     * &lt;/cms:contentload&gt;</pre>
     * 
     * @return the path to the current XML content value
     */
    public String getPath() {

        if (m_contentValue == null) {
            return "";
        }
        return m_contentValue.getPath();
    }

    /**
     * Short form of {@link #getResolveMacros()}.<p>
     * 
     * @return a value wrapper with macro resolving turned on
     * 
     * @see #getResolveMacros()
     */
    public CmsJspContentAccessValueWrapper getResolve() {

        return getResolveMacros();
    }

    /**
     * Turn on macro resolving for the wrapped value.<p> 
     * 
     * Macro resolving is turned off by default. 
     * When turned on, a macro resolver is initialized with 
     * the current OpenCms user context and the URI of the current resource. 
     * This means known macros contained in the wrapped value will be resolved when the output String is generated. 
     * For example, a <code>%(property.Title)</code> in the value would be replaced with the 
     * value of the title property. Macros that can not be resolved will be kept.<p>
     * 
     * Usage example on a JSP with the JSTL:<pre>
     * &lt;cms:contentload ... &gt;
     *     &lt;cms:contentaccess var="content" /&gt;
     *     The text with macros resolved: ${content.value['Text'].resolveMacros}
     * &lt;/cms:contentload&gt;</pre>
     * 
     * @return a value wrapper with macro resolving turned on
     * 
     * @see CmsMacroResolver
     */
    public CmsJspContentAccessValueWrapper getResolveMacros() {

        if (m_macroResolver == null) {
            CmsMacroResolver macroResolver = CmsMacroResolver.newInstance();
            macroResolver.setCmsObject(m_cms);
            macroResolver.setKeepEmptyMacros(true);
            return new CmsJspContentAccessValueWrapper(this, macroResolver);
        }
        // macro resolving is already turned on
        return this;
    }

    /**
     * Returns the String value of the wrapped content value.<p>
     * 
     * Note that this will return the empty String <code>""</code> when {@link #getExists()} returns <code>false</code><p>.
     * 
     * @return the String value of the wrapped content value
     * 
     * @see #toString()
     */
    public String getStringValue() {

        return toString();
    }

    /**
     * Returns a lazy initialized Map that provides the nested sub values 
     * for the current value from the XML content.<p>
     * 
     * The provided Map key is assumed to be a String that represents the relative xpath to the value.<p>
     * 
     * In case the current value is not a nested XML content value, or the XML content value does not exist,
     * the {@link CmsJspContentAccessBean#CONSTANT_NULL_VALUE_WRAPPER_MAP} is returned.<p>
     * 
     * Usage example on a JSP with the JSTL:<pre>
     * &lt;cms:contentload ... &gt;
     *     &lt;cms:contentaccess var="content" /&gt;
     *     The Link Description: ${content.value['Link'].value['Description']}
     * &lt;/cms:contentload&gt;</pre>
     * 
     * Please note that this example will only work if the 'Link' element is mandatory in the schema definition 
     * of the XML content.<p>
     *  
     * @return a lazy initialized Map that provides a sub value for the current value from the XML content
     */
    public Map getValue() {

        if (m_value == null) {
            m_value = LazyMap.decorate(new HashMap(), new CmsValueTransformer());
        }
        return m_value;
    }

    /**
     * Returns a lazy initialized Map that provides the Lists of nested sub values 
     * for the current value from the XML content.<p>
     * 
     * The provided Map key is assumed to be a String that represents the relative xpath to the value.
     * Use this method in case you want to iterate over a List of values form the XML content.<p>
     * 
     * In case the current value is not a nested XML content value, or the XML content value does not exist,
     * the {@link CmsConstantMap#CONSTANT_EMPTY_LIST_MAP} is returned.<p>
     * 
     * Usage example on a JSP with the JSTL:<pre>
     * &lt;cms:contentload ... &gt;
     *     &lt;cms:contentaccess var="content" /&gt;
     *     &lt;c:forEach var="desc" items="${content.value['Link'].valueList['Description']}"&gt;
     *         ${desc}
     *     &lt;/c:forEach&gt;
     * &lt;/cms:contentload&gt;</pre>
     *  
     * @return a lazy initialized Map that provides a Lists of sub values for the current value from the XML content
     */
    public Map getValueList() {

        if (m_valueList == null) {
            m_valueList = LazyMap.decorate(new HashMap(), new CmsValueListTransformer());
        }
        return m_valueList;
    }

    /**
     * Returns a lazy initialized Map that provides direct access to the XML element 
     * for the current value from the XML content.<p>
     * 
     * @return a lazy initialized Map that provides direct access to the XML element for the current value from the XML content
     */
    public Map getXmlText() {

        if (m_xml == null) {
            m_xml = LazyMap.decorate(new HashMap(), new CmsXmlValueTransformer());
        }
        return m_xml;
    }

    /**
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {

        if (m_contentValue == null) {
            return 0;
        }
        if (m_hashCode == 0) {
            StringBuffer result = new StringBuffer(64);
            result.append(m_contentValue.getDocument().getFile().getStructureId().toString());
            result.append('/');
            result.append(m_contentValue.getLocale());
            result.append('/');
            result.append(m_contentValue.getPath());
            m_hashCode = result.toString().hashCode();
        }
        return m_hashCode;
    }

    /**
     * Returns the wrapped OpenCms user context.<p>
     * 
     * Note that this will return <code>null</code> when {@link #getExists()} returns <code>false</code>. 
     * 
     * @return the wrapped OpenCms user context
     */
    public CmsObject obtainCmsObject() {

        return m_cms;
    }

    /**
     * Returns the wrapped content value.<p>
     * 
     * Note that this will return <code>null</code> when {@link #getExists()} returns <code>false</code><p>.
     * 
     * Method name does not start with "get" to prevent using it in the expression language.<p>
     * 
     * @return the wrapped content value
     */
    public I_CmsXmlContentValue obtainContentValue() {

        return m_contentValue;
    }

    /**
     * @see java.lang.Object#toString()
     * @see #getStringValue()
     */
    @Override
    public String toString() {

        if (m_contentValue == null) {
            // this is the case for non existing values
            return "";
        }
        if (m_contentValue.isSimpleType()) {
            // return values for simple type
            String value = m_contentValue.getStringValue(m_cms);
            if (m_macroResolver == null) {
                // no macro resolving
                return value;
            } else {
                // resolve macros first
                return m_macroResolver.resolveMacros(value);
            }
        } else {
            // nested types should not be called this way by the user
            return "";
        }
    }

    /**
     * Returns the path to the XML content based on the current element path.<p>
     * 
     * This is used to create xpath information for sub-elements in the transformers.<p>
     * 
     * @param input the additional path that is appended to the current path
     * 
     * @return the path to the XML content based on the current element path
     */
    protected String createPath(Object input) {

        return CmsXmlUtils.concatXpath(m_contentValue.getPath(), String.valueOf(input));
    }
}