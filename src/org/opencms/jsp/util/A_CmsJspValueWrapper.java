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
import org.opencms.main.CmsException;
import org.opencms.main.CmsLog;
import org.opencms.main.OpenCms;
import org.opencms.staticexport.CmsLinkManager;
import org.opencms.util.CmsCollectionsGenericWrapper;
import org.opencms.util.CmsConstantMap;
import org.opencms.util.CmsStringUtil;

import java.util.Date;
import java.util.Map;

import org.apache.commons.collections.Transformer;
import org.apache.commons.logging.Log;

/**
 * Common value wrapper class that provides generic functions.<p>
 *
 * Wrappers that extend this are usually used for the values in lazy initialized transformer maps.<p>
 */
abstract class A_CmsJspValueWrapper {

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

    /** The log object for this class. */
    private static final Log LOG = CmsLog.getLog(A_CmsJspValueWrapper.class);

    /**
     * Returns the substituted link to the given target.<p>
     *
     * @param cms the cms context
     * @param target the link target
     *
     * @return the substituted link
     */
    protected static String substituteLink(CmsObject cms, String target) {

        if (cms != null) {
            return OpenCms.getLinkManager().substituteLinkForUnknownTarget(
                cms,
                CmsLinkManager.getAbsoluteUri(String.valueOf(target), cms.getRequestContext().getUri()));
        } else {
            return "";
        }
    }

    /** The wrapped OpenCms user context. */
    protected CmsObject m_cms;

    /** Image bean instance created from the wrapped value. */
    private CmsJspImageBean m_imageBean;

    /** String representation of the wrapped value with HTML stripped off. */
    private String m_stripHtml;

    /** Boolean representation of the wrapped value. */
    private Boolean m_boolean;

    /** Date created from the wrapped value. */
    private Date m_date;

    /** Double created from the wrapped value. */
    private Double m_double;

    /** Long created from the wrapped value. */
    private Long m_long;

    /** Link created from the wrapped value. */
    private String m_link;

    /** String representation of the wrapped value. */
    private String m_string;

    /** The lazy initialized Map that checks if a Object is equal. */
    private Map<Object, Boolean> m_isEqual;

    /** The lazy initialized trim to size map. */
    private Map<Object, String> m_trimToSize;

    /**
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj) {

        if (obj == this) {
            return true;
        }
        if ((obj != null) && (obj.getClass() == getClass())) {
            // rely on hash code implementation for equals method
            return hashCode() == ((A_CmsJspValueWrapper)obj).hashCode();
        }
        return false;
    }

    /**
     * Returns the current cms context.<p>
     *
     * @return the cms context
     */
    public CmsObject getCmsObject() {

        return m_cms;
    }

    /**
     * Returns <code>true</code> if the wrapped value has been somehow initialized.<p>
     *
     * @return <code>true</code> if the wrapped value has been somehow initialized
     */
    public abstract boolean getExists();

    /**
     * Returns <code>true</code> in case the wrapped value is empty, that is either <code>null</code> or an empty String.<p>
     *
     * @return <code>true</code> in case the wrapped value is empty
     */
    public abstract boolean getIsEmpty();

    /**
     * Returns <code>true</code> in case the wrapped value is empty or whitespace only,
     * that is either <code>null</code> or String that contains only whitespace chars.<p>
     *
     * @return <code>true</code> in case the wrapped value is empty or whitespace only
     */
    public abstract boolean getIsEmptyOrWhitespaceOnly();

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
     * Returns <code>true</code> in case the wrapped value exists and is not empty.<p>
     *
     * @return <code>true</code> in case the wrapped value exists and is not empty
     */
    public abstract boolean getIsSet();

    /**
     * Calculates the next largest integer from the wrapped value.<p>
     *
     * Note that the result is an Object of type {@link java.lang.Long},
     * so in case the wrapped value can not be converted to a number, <code>null</code> is returned.
     * This means you can check for an <code>empty</code> result in the EL.<p>
     *
     * @return the next largest integer for the wrapped value
     */
    public Long getMathCeil() {

        return CmsJspElFunctions.mathCeil(getToDouble());
    }

    /**
     * Calculates the next smallest integer from the wrapped value.<p>
     *
     * Note that the result is an Object of type {@link java.lang.Long},
     * so in case the wrapped value can not be converted to a number, <code>null</code> is returned.
     * This means you can check for an <code>empty</code> result in the EL.<p>
     *
     * @return the next smallest integer for the wrapped value
     */
    public Long getMathFloor() {

        return CmsJspElFunctions.mathFloor(getToDouble());
    }

    /**
     * Calculates the next integer from the wrapped value by rounding.<p>
     *
     * Note that the result is an Object of type {@link java.lang.Long},
     * so in case the wrapped value can not be converted to a number, <code>null</code> is returned.
     * This means you can check for an <code>empty</code> result in the EL.<p>
     *
     * @return the next integer for the wrapped value calculated by rounding
     */
    public Long getMathRound() {

        return CmsJspElFunctions.mathRound(getToDouble());
    }

    /**
     * Returns the raw instance of the wrapped value.<p>
     *
     * @return the raw instance of the wrapped value
     */
    public abstract Object getObjectValue();

    /**
     * Returns the String value for the wrapped content value.<p>
     *
     * This will return the empty String <code>""</code> when {@link #getExists()} returns <code>false</code><p>.
     *
     * @return the String value of the wrapped content value
     *
     * @deprecated use {@link #getToString()} instead
     */
    @Deprecated
    public String getStringValue() {

        return toString();
    }

    /**
     * Assumes the wrapped value is a String and strips all HTML markup from this String.<p>
     *
     * @return the wrapped value with all HTML stripped.
     */
    public String getStripHtml() {

        if (m_stripHtml == null) {
            m_stripHtml = CmsJspElFunctions.stripHtml(this);
        }
        return m_stripHtml;
    }

    /**
     * Converts the wrapped value to a boolean.<p>
     *
     * @return the boolean value
     */
    public boolean getToBoolean() {

        if (m_boolean == null) {
            m_boolean = Boolean.valueOf(Boolean.parseBoolean(getToString()));
        }
        return m_boolean.booleanValue();
    }

    /**
     * Converts the wrapped value to a date.<p>
     *
     * @return the date
     *
     * @see CmsJspElFunctions#convertDate(Object)
     */
    public Date getToDate() {

        if (m_date == null) {
            m_date = CmsJspElFunctions.convertDate(getToString());
        }
        return m_date;
    }

    /**
     * Parses the wrapped value to a Double precision float.<p>
     *
     * Note that the result is an Object of type {@link java.lang.Double},
     * so in case the wrapped value can not be converted to a number, <code>null</code> is returned.
     * This means you can check for an <code>empty</code> result in the EL.<p>
     *
     * @return the Double precision float value
     */
    public Double getToDouble() {

        if (m_double == null) {
            try {
                m_double = new Double(Double.parseDouble(getToString()));
            } catch (NumberFormatException e) {
                LOG.info(e.getLocalizedMessage());
            }
        }
        return m_double;
    }

    /**
     * Parses the wrapped value to a Double precision float.<p>
     *
     * Note that the result is an Object of type {@link java.lang.Double},
     * so in case the wrapped value can not be converted to a number, <code>null</code> is returned.
     * This means you can check for an <code>empty</code> result in the EL.<p>
     *
     * @return the Double precision float value
     */
    public Double getToFloat() {

        return getToDouble();
    }

    /**
     * Returns a scaled image bean from the wrapped value.<p>
     *
     * In case the value does not point to an image resource, <code>null</code> is returned.
     *
     * @return the scaled image bean
     */
    public CmsJspImageBean getToImage() {

        if (m_imageBean == null) {
            try {
                m_imageBean = new CmsJspImageBean(getCmsObject(), getToString());
            } catch (CmsException e) {
                // this should only happen if the image path is not valid, in which case we will return null
                LOG.info(e.getLocalizedMessage(), e);
            }
        }
        return m_imageBean;
    }

    /**
     * Parses the wrapped value to a Long integer.<p>
     *
     * Note that the result is an Object of type {@link java.lang.Long},
     * so in case the wrapped value can not be converted to a number, <code>null</code> is returned.
     * This means you can check for an <code>empty</code> result in the EL.<p>
     *
     * @return the Long integer value
     *
     * @see #getToLong()
     */
    public Long getToInteger() {

        return getToLong();
    }

    /**
     * Returns the substituted link to the wrapped value.<p>
     *
     * In case no link can be substituted from the wrapped value, an empty String <code>""</code> is returned.
     *
     * @return the substituted link
     */
    public String getToLink() {

        if (m_link == null) {
            String target = toString();
            if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(target)) {
                m_link = substituteLink(getCmsObject(), target);
            } else {
                m_link = "";
            }
        }
        return m_link;
    }

    /**
     * Parses the wrapped value to a Long integer.<p>
     *
     * Note that the result is an Object of type {@link java.lang.Long},
     * so in case the wrapped value can not be converted to a number, <code>null</code> is returned.
     * This means you can check for an <code>empty</code> result in the EL.<p>
     *
     * @return the Long integer value
     *
     * @see #getToInteger()
     */
    public Long getToLong() {

        if (m_long == null) {
            try {
                m_long = new Long(Long.parseLong(getToString()));
            } catch (NumberFormatException e) {
                LOG.info(e.getLocalizedMessage());
            }
        }
        return m_long;
    }

    /**
     * Returns the wrapped value as a String.<p>
     *
     * This will always be at least an empty String <code>""</code>, never <code>null</code>.
     *
     * @return the wrapped value as a String
     */
    public String getToString() {

        if (m_string == null) {
            m_string = toString();
        }
        return m_string;
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
    public abstract int hashCode();

    /**
     * Parses the wrapped value to a Double precision float, returning the default in case the number can not be parsed.<p>
     *
     * @param def the default in case the wrapped value can not be converted to a number
     * @return a Double precision float value
     *
     * @see #toFloat(Double)
     */
    public Double toDouble(Double def) {

        if (getToDouble() == null) {
            return def;
        }
        return m_double;
    }

    /**
     * Parses the wrapped value to a Double precision float, returning the default in case the number can not be parsed.<p>
     *
     * @param def the default in case the wrapped value can not be converted to a number
     * @return a Double precision float value
     *
     * @see #toDouble(Double)
     */
    public Double toFloat(Double def) {

        return toDouble(def);
    }

    /**
     * Parses the wrapped value to a Long integer, returning the default in case the number can not be parsed.<p>
     *
     * @param def the default in case the wrapped value can not be converted to a number
     * @return a Long integer value
     *
     * @see #toLong(Long)
     */
    public Long toInteger(Long def) {

        return toLong(def);
    }

    /**
     * Parses the wrapped value to a Long integer, returning the default in case the number can not be parsed.<p>
     *
     * @param def the default in case the wrapped value can not be converted to a number
     * @return a Long integer value
     *
     * @see #toInteger(Long)
     */
    public Long toLong(Long def) {

        if (getToLong() == null) {
            return def;
        }
        return m_long;
    }

}
