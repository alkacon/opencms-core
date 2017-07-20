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

package org.opencms.jsp.util;

import org.opencms.file.CmsObject;
import org.opencms.gwt.shared.CmsGwtConstants;
import org.opencms.util.CmsCollectionsGenericWrapper;
import org.opencms.util.CmsConstantMap;
import org.opencms.util.CmsStringUtil;
import org.opencms.xml.types.I_CmsXmlContentValue;

import java.util.List;
import java.util.Map;

import org.apache.commons.collections.Transformer;

/**
 * Allows direct access to XML content values, with possible iteration of sub-nodes.<p>
 *
 * The implementation is optimized for performance and uses lazy initializing of the
 * requested values as much as possible.<p>
 *
 * @since 7.0.2
 *
 * @see CmsJspContentAccessBean
 * @see org.opencms.jsp.CmsJspTagContentAccess
 */
public final class CmsJspObjectAccessWrapper extends A_CmsJspValueWrapper {

    /**
     * Provides a Map with Booleans that
     * indicate if a given Object is equal to the wrapped object.<p>
     */
    public class CmsIsEqualTransformer implements Transformer {

        /**
         * @see org.apache.commons.collections.Transformer#transform(java.lang.Object)
         */
        @Override
        public Object transform(Object input) {

            Object o = getObjectValue();
            if ((o instanceof A_CmsJspValueWrapper) && (input instanceof String)) {
                return Boolean.valueOf(((A_CmsJspValueWrapper)o).getToString().equals(input));
            }
            if (o == null) {
                return Boolean.valueOf(input == null);
            }
            return Boolean.valueOf(o.equals(input));
        }
    }

    /**
     * Provides trimmed to size string values.<p>
     */
    public class CmsTrimToSizeTransformer implements Transformer {

        /**
         * @see org.apache.commons.collections.Transformer#transform(java.lang.Object)
         */
        @Override
        public Object transform(Object input) {

            try {
                int lenght = Integer.parseInt(String.valueOf(input));
                return CmsJspElFunctions.trimToSize(getToString(), lenght);

            } catch (Exception e) {
                return getToString();
            }
        }
    }

    /** Constant for the null (non existing) value. */
    protected static final CmsJspObjectAccessWrapper NULL_VALUE_WRAPPER = new CmsJspObjectAccessWrapper();

    /** The wrapped OpenCms user context. */
    private CmsObject m_cms;

    /** Calculated hash code. */
    private int m_hashCode;

    /** The lazy initialized Map that checks if a Object is equal. */
    private Map<Object, Boolean> m_isEqual;

    /** The wrapped XML content value. */
    private Object m_object;

    /** The lazy initialized trim to size map. */
    private Map<Object, String> m_trimToSize;

    /**
     * Private constructor, used for creation of NULL constant value, use factory method to create instances.<p>
     *
     * @see #createWrapper(CmsObject, Object)
     */
    private CmsJspObjectAccessWrapper() {

        // cast needed to avoid compiler confusion with constructors
        this((CmsObject)null, (I_CmsXmlContentValue)null);
    }

    /**
     * Private constructor, use factory method to create instances.<p>
     *
     * @param cms the current users OpenCms context
     * @param value the object to warp
     */
    private CmsJspObjectAccessWrapper(CmsObject cms, Object value) {

        // a null value is used for constant generation
        m_cms = cms;
        m_object = value;
    }

    /**
     * Factory method to create a new XML content value wrapper.<p>
     *
     * In case either parameter is <code>null</code>, the {@link #NULL_VALUE_WRAPPER} is returned.<p>
     *
     * @param cms the current users OpenCms context
     * @param value the object to warp
     *
     * @return a new content value wrapper instance, or <code>null</code> if any parameter is <code>null</code>
     */
    public static CmsJspObjectAccessWrapper createWrapper(CmsObject cms, Object value) {

        if ((value != null) && (cms != null)) {
            return new CmsJspObjectAccessWrapper(cms, value);
        }
        // if no value is available,
        return NULL_VALUE_WRAPPER;
    }

    /**
     * Returns if direct edit is enabled.<p>
     *
     * @param cms the current cms context
     *
     * @return <code>true</code> if direct edit is enabled
     */
    static boolean isDirectEditEnabled(CmsObject cms) {

        return !cms.getRequestContext().getCurrentProject().isOnlineProject()
            && (cms.getRequestContext().getAttribute(CmsGwtConstants.PARAM_DISABLE_DIRECT_EDIT) == null);
    }

    /**
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj) {

        if (obj == this) {
            return true;
        }
        if (obj instanceof CmsJspObjectAccessWrapper) {
            // rely on hash code implementation for equals method
            return hashCode() == ((CmsJspObjectAccessWrapper)obj).hashCode();
        }
        return false;
    }

    /**
     * @see org.opencms.jsp.util.A_CmsJspValueWrapper#getCmsObject()
     */
    @Override
    public CmsObject getCmsObject() {

        return m_cms;
    }

    /**
     * Returns <code>true</code> in case there was an object wrapped.<p>
     *
     * @return <code>true</code> in case there was an object wrapped
     */
    @Override
    public boolean getExists() {

        return m_object != null;
    }

    /**
     * Returns <code>true</code> in case the object is empty, that is either <code>null</code> or an empty String.<p>
     *
     * In case the object does not exist, <code>true</code> is returned.<p>
     *
     * @return <code>true</code> in case the object is empty
     */
    @SuppressWarnings("rawtypes")
    @Override
    public boolean getIsEmpty() {

        if (m_object == null) {
            // this is the case for non existing values
            return true;
        } else if (m_object instanceof String) {
            // return values for String
            return CmsStringUtil.isEmpty((String)m_object);
        } else if (m_object instanceof Map) {
            // if map has any entries it is not emtpy
            return !((Map)m_object).isEmpty();
        } else if (m_object instanceof List) {
            // if map has any entries it is not emtpy
            return !((List)m_object).isEmpty();
        } else {
            // assume all other non-null objects are not empty
            return false;
        }
    }

    /**
     * Returns <code>true</code> in case the object is empty or whitespace only, that is either <code>null</code> or an empty String.<p>
     *
     * In case the object does not exist, <code>true</code> is returned.<p>
     *
     * @return <code>true</code> in case the object is empty
     */
    @Override
    public boolean getIsEmptyOrWhitespaceOnly() {

        if (m_object instanceof String) {
            // return values for simple type
            return CmsStringUtil.isEmptyOrWhitespaceOnly((String)m_object);
        } else {
            // use isEmpty() for all other object types
            return getIsEmpty();
        }
    }

    /**
     * Returns a lazy initialized Map that provides Booleans which
     * indicate if an Object is equal to the wrapped object.<p>
     *
     * In case the current,
     * the {@link CmsConstantMap#CONSTANT_BOOLEAN_FALSE_MAP} is returned.<p>
     *
     * @return a lazy initialized Map that provides Booleans which
     *    indicate if an Object is equal to the wrapped object
     */
    public Map<Object, Boolean> getIsEqual() {

        if (m_isEqual == null) {
            m_isEqual = CmsCollectionsGenericWrapper.createLazyMap(new CmsIsEqualTransformer());
        }
        return m_isEqual;
    }

    /**
     * Returns <code>true</code> in case
     * the object exists and is not empty or whitespace only.<p>
     *
     * @return <code>true</code> in case the object exists and is not empty or whitespace only
     */
    @Override
    public boolean getIsSet() {

        return !getIsEmptyOrWhitespaceOnly();
    }

    /**
     * Returns the wrapped object value.<p>
     *
     * @return the wrapped object value
     */
    public Object getObjectValue() {

        return m_object;
    }

    /**
     * Returns a lazy initialized map that provides trimmed to size strings of the wrapped object string value.
     * The size being the integer value of the key object.<p>
     *
     * @return a map that provides trimmed to size strings of the wrapped object string value
     */
    public Map<Object, String> getTrimToSize() {

        if (m_trimToSize == null) {
            m_trimToSize = CmsCollectionsGenericWrapper.createLazyMap(new CmsTrimToSizeTransformer());
        }
        return m_trimToSize;
    }

    /**
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {

        if (m_object == null) {
            return 0;
        }
        if (m_hashCode == 0) {
            m_hashCode = m_object.toString().hashCode();
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
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {

        if (m_object == null) {
            // this is the case for non existing values
            return "";
        } else {
            return m_object.toString();
        }
    }
}