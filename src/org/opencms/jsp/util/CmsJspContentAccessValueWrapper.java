/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/jsp/util/CmsJspContentAccessValueWrapper.java,v $
 * Date   : $Date: 2007/08/13 16:30:11 $
 * Version: $Revision: 1.2 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (c) 2002 - 2007 Alkacon Software GmbH (http://www.alkacon.com)
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
import org.opencms.xml.CmsXmlUtils;
import org.opencms.xml.types.I_CmsXmlContentValue;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.Transformer;
import org.apache.commons.collections.map.LazyMap;

/**
 * Allows access to XML content values, with possible iteration of sub-nodes.<p>
 * 
 * The implementation is optimized for performance and uses lazy initializing of the 
 * requested values as much as possible.<p>
 * 
 * @author Alexander Kandzior
 * 
 * @version $Revision: 1.2 $ 
 * 
 * @since 7.0.2
 * 
 * @see org.opencms.jsp.CmsJspTagContentAccess
 */
public final class CmsJspContentAccessValueWrapper {

    /**
     * Provides a Map with Booleans that indicate if a specified path exists in the XML content in the selected Locale,  
     * the input is assumed to be a String that represents an xpath in the XML content.<p>
     */
    public class CmsHasValueTransformer implements Transformer {

        /**
         * @see org.apache.commons.collections.Transformer#transform(java.lang.Object)
         */
        public Object transform(Object input) {

            return Boolean.valueOf(m_contentValue.getDocument().hasValue(getPath(input), m_contentValue.getLocale()));
        }
    }

    /**
     * Provides a Map which lets the user access value Lists in an XML content, 
     * the input is assumed to be a String that represents an xpath in the XML content.<p>
     */
    public class CmsValueListTransformer implements Transformer {

        /**
         * @see org.apache.commons.collections.Transformer#transform(java.lang.Object)
         */
        public Object transform(Object input) {

            List values = m_contentValue.getDocument().getValues(getPath(input), m_contentValue.getLocale());
            List result = new ArrayList();
            Iterator i = values.iterator();
            while (i.hasNext()) {
                // XML content API offers List of values only as Objects, must iterate them and create Strings 
                I_CmsXmlContentValue value = (I_CmsXmlContentValue)i.next();
                result.add(createWrapper(m_cms, value));
            }
            return result;
        }
    }

    /**
     * Provides a Map which lets the user a value in an XML content, 
     * the input is assumed to be a String that represents an xpath in the XML content.<p>
     */
    public class CmsValueTransformer implements Transformer {

        /**
         * @see org.apache.commons.collections.Transformer#transform(java.lang.Object)
         */
        public Object transform(Object input) {

            I_CmsXmlContentValue value = m_contentValue.getDocument().getValue(
                getPath(input),
                m_contentValue.getLocale());
            return createWrapper(m_cms, value);
        }
    }

    /** The wrapped OpenCms user context. */
    protected CmsObject m_cms;

    /** The wrapped XML content value. */
    protected I_CmsXmlContentValue m_contentValue;

    /** The lazy initialized Map that checks if a value is available. */
    private Map m_hasValue;

    /** The lazy initialized value Map. */
    private Map m_value;

    /** The lazy initialized value list Map. */
    private Map m_valueList;

    /**
     * Private constructor, use factory method to create instances.<p>
     * 
     * @param cms the current users OpenCms context
     * @param value the value to warp
     * 
     * @see #createWrapper(CmsObject, I_CmsXmlContentValue)
     */
    private CmsJspContentAccessValueWrapper(CmsObject cms, I_CmsXmlContentValue value) {

        m_cms = cms;
        m_contentValue = value;

        if (m_contentValue.isSimpleType()) {
            // simple types don't have any sub-elements
            m_hasValue = Collections.EMPTY_MAP;
            m_value = Collections.EMPTY_MAP;
            m_valueList = Collections.EMPTY_MAP;
        }
    }

    /**
     * Factory method to create a new XML content value wrapper.<p>
     * 
     * In case either parameter is <code>null</code>, <code>null</code> is returned.<p>
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
        // invalid, arguments
        return null;
    }

    /**
     * Returns a lazy initialized Map that provides Booleans that 
     * indicate if a value (xpath) is available in the XML content in the current locale.<p>
     * 
     * The provided Map key is assumed to be a String that represents the xpath to the value.<p>
     * 
     * Usage example on a JSP with the JSTL:<pre>
     * &lt;cms:contentloop ... &gt;
     *     &lt;cms:contentaccess var="content" /&gt;
     *     &lt;c:if test="${content.hasValue['Title']}" &gt;
     *         The content has a "Title" value in the current locale! 
     *     &lt;/c:if&gt;
     * &lt;/cms:contentloop&gt;</pre>
     *  
     * @return a lazy initialized Map that provides Booleans that 
     *      indicate if a value (xpath) is available in the XML content in the current locale
     */
    public Map getHasValue() {

        if (m_hasValue == null) {
            m_hasValue = LazyMap.decorate(new HashMap(), new CmsHasValueTransformer());
        }
        return m_hasValue;
    }

    /**
     * Returns the path to the XML content based on the current element path.<p>
     * 
     * @param input the additional path that is appended to the current path
     * 
     * @return the path to the XML content based on the current element path
     */
    public String getPath(Object input) {

        return CmsXmlUtils.concatXpath(m_contentValue.getPath(), String.valueOf(input));
    }

    /**
     * Returns a lazy initialized Map that provides values from the XML content in the current locale.<p>
     * 
     * The provided Map key is assumed to be a String that represents the xpath to the value.<p>
     * 
     * Usage example on a JSP with the JSTL:<pre>
     * &lt;cms:contentloop ... &gt;
     *     &lt;cms:contentaccess var="content" /&gt;
     *     The Title: &lt;c:out value="${content.value['Title']}" &gt;
     * &lt;/cms:contentloop&gt;</pre>
     *  
     * @return a lazy initialized Map that provides values from the XML content in the current locale
     */
    public Map getValue() {

        if (m_value == null) {
            m_value = LazyMap.decorate(new HashMap(), new CmsValueTransformer());
        }
        return m_value;
    }

    /**
     * Returns a lazy initialized Map that provides Lists of values from the XML content in the current locale.<p>
     * 
     * The provided Map key is assumed to be a String that represents the xpath to the value.
     * Use this method in case you want to iterate over a List of values form the XML content.<p>
     * 
     * Usage example on a JSP with the JSTL:<pre>
     * &lt;cms:contentloop ... &gt;
     *     &lt;cms:contentaccess var="content" /&gt;
     *     &lt;c:forEach var="teaser" items="${content.valueList['Teaser']}"&gt;
     *         &lt;c:out value="${teaser}" /&gt;
     *     &lt;/c:forEach&gt;
     * &lt;/cms:contentloop&gt;</pre>
     *  
     * @return a lazy initialized Map that provides Lists of values from the XML content in the current locale
     */
    public Map getValueList() {

        if (m_valueList == null) {
            m_valueList = LazyMap.decorate(new HashMap(), new CmsValueListTransformer());
        }
        return m_valueList;
    }

    /**
     * @see java.lang.Object#toString()
     */
    public String toString() {

        if (m_contentValue.isSimpleType()) {
            // return values for simple type
            return m_contentValue.getStringValue(m_cms);
        } else {
            // do something else
            return null;
        }
    }
}