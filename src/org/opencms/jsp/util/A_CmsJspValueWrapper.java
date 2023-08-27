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
import org.opencms.jsp.CmsJspResourceWrapper;
import org.opencms.main.CmsException;
import org.opencms.main.CmsLog;
import org.opencms.main.OpenCms;
import org.opencms.staticexport.CmsLinkManager;
import org.opencms.util.CmsCollectionsGenericWrapper;
import org.opencms.util.CmsStringUtil;
import org.opencms.xml.containerpage.CmsXmlContainerPage;
import org.opencms.xml.containerpage.CmsXmlContainerPageFactory;

import java.util.AbstractCollection;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

import org.apache.commons.collections.Transformer;
import org.apache.commons.logging.Log;

/**
 * Common value wrapper class that provides generic functions.<p>
 *
 * Wrappers that extend this are usually used for the values in lazy initialized transformer maps.<p>
 */
abstract class A_CmsJspValueWrapper extends AbstractCollection<String> {

    /**
     * Provides a Map with Booleans that
     * indicate if a given String is contained in the wrapped objects String representation.<p>
     */
    public class CmsContainsTransformer implements Transformer {

        /**
         * @see org.apache.commons.collections.Transformer#transform(java.lang.Object)
         */
        @Override
        public Object transform(Object input) {

            Object o = getObjectValue();
            if ((o instanceof A_CmsJspValueWrapper) && (input != null)) {
                return Boolean.valueOf(((A_CmsJspValueWrapper)o).getToString().indexOf(input.toString()) > -1);
            }
            return Boolean.FALSE;
        }
    }

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

    /** The wrapped OpenCms user context. */
    protected CmsObject m_cms;

    /** Boolean representation of the wrapped value. */
    private Boolean m_boolean;

    /** Cached container page wrapper. */
    private CmsJspContainerPageWrapper m_containerPageWrapper;

    /** The lazy initialized Map that checks if the String representation of this wrapper contains specific words. */
    private Map<Object, Boolean> m_contains;

    /** Date created from the wrapped value. */
    private Date m_date;

    /** Double created from the wrapped value. */
    private Double m_double;

    /** Image bean instance created from the wrapped value. */
    private CmsJspImageBean m_imageBean;

    /** Date information as instance date bean. */
    private CmsJspInstanceDateBean m_instanceDate;

    /** The lazy initialized Map that checks if a Object is equal. */
    private Map<Object, Boolean> m_isEqual;

    /** Link created from the wrapped value. */
    private String m_link;

    /** Long created from the wrapped value. */
    private Long m_long;

    /** Resource created from the wrapped value. */
    private CmsJspResourceWrapper m_resource;

    /** String representation of the wrapped value. */
    private String m_string;

    /** String representation of the wrapped value with HTML stripped off. */
    private String m_stripHtml;

    /** The lazy initialized trim to size map. */
    private Map<Object, String> m_trimToSize;

    /** Cached link wrapper - use Optional to distinguish 'uncached' state from 'does not exist'. */
    protected Optional<CmsJspLinkWrapper> m_linkObj;

    /**
     * Returns the substituted link to the given target.<p>
     *
     * @param cms the cms context
     * @param target the link target
     *
     * @return the substituted link
     */
    public static String substituteLink(CmsObject cms, String target) {

        if (cms != null) {
            return OpenCms.getLinkManager().substituteLinkForUnknownTarget(
                cms,
                CmsLinkManager.getAbsoluteUri(String.valueOf(target), cms.getRequestContext().getUri()));
        } else {
            return "";
        }
    }

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
     * Returns a lazy initialized Map that provides Booleans which
     * indicate if if the wrapped values String representation contains a specific String.<p>
     *
     * The Object parameter is transformed to it's String representation to perform this check.
     *
     * @return  a lazy initialized Map that provides Booleans which
     * indicate if if the wrapped values String representation contains a specific String
     */
    public Map<Object, Boolean> getContains() {

        if (m_contains == null) {
            m_contains = CmsCollectionsGenericWrapper.createLazyMap(new CmsContainsTransformer());
        }
        return m_contains;
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
     * that is either <code>null</code> or a String that contains only whitespace chars.<p>
     *
     * @return <code>true</code> in case the wrapped value is empty or whitespace only
     */
    public abstract boolean getIsEmptyOrWhitespaceOnly();

    /**
     * Returns a lazy initialized Map that provides Booleans which
     * indicate if an Object is equal to the wrapped object.<p>
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
     * Returns <code>true</code> in case the wrapped value exists and is not empty or whitespace only.<p>
     *
     * @return <code>true</code> in case the wrapped value exists and is not empty or whitespace only
     */
    public boolean getIsSet() {

        return !getIsEmptyOrWhitespaceOnly();
    }

    /**
     * Returns <code>true</code> in case the wrapped value exists, is not empty or whitespace only
     * and is also not equal to the String <code>'none'</code>.<p>
     *
     * @return <code>true</code> in case the wrapped value exists, is not empty or whitespace only
     * and is also not equal to the String <code>'none'</code>
     */
    public boolean getIsSetNotNone() {

        return getIsEmptyOrWhitespaceOnly() ? false : !"none".equals(getToString());
    }

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

        return getToString();
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
     * Tries to create a container page wrapper from the wrapped value.
     *
     * @return the container page wrapper or null if none could be created
     */
    public CmsJspContainerPageWrapper getToContainerPage() {

        if (m_containerPageWrapper != null) {
            return m_containerPageWrapper;
        }
        CmsJspResourceWrapper res = getToResource();
        if (res == null) {
            return null;
        }
        try {
            CmsXmlContainerPage page = CmsXmlContainerPageFactory.unmarshal(
                m_cms,
                m_cms.readFile(res),
                true,
                /*nocache=*/true); // container page caching causes problems with the EL container rendering feature, don't use it here
            m_containerPageWrapper = new CmsJspContainerPageWrapper(page.getContainerPage(m_cms));
            return m_containerPageWrapper;
        } catch (Exception e) {
            LOG.debug(e.getLocalizedMessage(), e);
            return null;
        }
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
                m_double = Double.valueOf(Double.parseDouble(getToString()));
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
     * Converts a date to an instance date bean.
     * @return the instance date bean.
     */
    public CmsJspInstanceDateBean getToInstanceDate() {

        if (m_instanceDate == null) {
            m_instanceDate = new CmsJspInstanceDateBean(getToDate(), m_cms.getRequestContext().getLocale());
        }
        return m_instanceDate;
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
     * Converts the value to a link wrapper.
     *
     * @return the link wrapper
     */
    public CmsJspLinkWrapper getToLink() {

        if (m_linkObj == null) {
            String target = toString();
            if (target != null) {
                m_linkObj = Optional.of(new CmsJspLinkWrapper(getCmsObject(), target));
            } else {
                m_linkObj = Optional.empty();
            }
        }
        return m_linkObj.orElse(null);
    }

    /**
     * Returns the substituted link to the wrapped value.<p>
     *
     * In case no link can be substituted from the wrapped value, an empty String <code>""</code> is returned.
     *
     * @return the substituted link
     */
    public String getToLinkStr() {

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
     * Converts the wrapped value to a java.util.Locale instance using the locale manager.
     *
     * @return the locale instance for the wrapped value
     */
    public Locale getToLocale() {

        return CmsJspElFunctions.convertLocale(getObjectValue());
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
                m_long = Long.valueOf(Long.parseLong(getToString()));
            } catch (NumberFormatException e) {
                LOG.info(e.getLocalizedMessage());
            }
        }
        return m_long;
    }

    /**
     * Returns the resource this value if pointing to.<p>
     *
     * It is assumed the value holds a valid resource UIR in the OpenCms VFS.
     * In case the value cannot be converted to a resource, an error is logged but no exception is thrown.<p>
     *
     * @return the resource this value if pointing to
     */
    public CmsJspResourceWrapper getToResource() {

        if (m_resource == null) {
            try {
                m_resource = CmsJspElFunctions.convertResource(m_cms, getToString());
            } catch (CmsException e) {
                if (LOG.isInfoEnabled()) {
                    LOG.info("Failed to convert wrapper \"" + getToString() + "\" to a resource.", e);
                }
            }
        }
        return m_resource;
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
     * Supports the use of the <code>empty</code> operator in the JSP EL by implementing the Collection interface.<p>
     *
     * @return the value from {@link #getIsEmptyOrWhitespaceOnly()} which is the inverse of {@link #getIsSet()}.<p>
     *
     * @see java.util.AbstractCollection#isEmpty()
     * @see #getIsEmptyOrWhitespaceOnly()
     * @see #getIsSet()
     */
    @Override
    public boolean isEmpty() {

        return getIsEmptyOrWhitespaceOnly();
    }

    /**
     * Compares this value against a list of Strings and returns <code>true</code> in case the list contains the
     * toString representation of this value, or a <code>false</code> otherwise.<p>
     *
     * @param allowedValues the list of allowed String values
     *
     * @return returns <code>true</code> in case the list contains the
     *         toString representation of this value, or a <code>false</code> otherwise
     *
     * @see #validate(List)
     * @see #validate(List, Object)
     */
    public boolean isValid(List<String> allowedValues) {

        return (allowedValues != null) && !getIsEmptyOrWhitespaceOnly() && allowedValues.contains(toString());
    }

    /**
     * Supports the use of the <code>empty</code> operator in the JSP EL by implementing the Collection interface.<p>
     *
     * @return an empty Iterator in case {@link #isEmpty()} is <code>true</code>,
     * otherwise an Iterator that will return the String value of this wrapper exactly once.<p>
     *
     * @see java.util.AbstractCollection#size()
     */
    @Override
    public Iterator<String> iterator() {

        Iterator<String> it = new Iterator<String>() {

            private boolean isFirst = true;

            @Override
            public boolean hasNext() {

                return isFirst && !isEmpty();
            }

            @Override
            public String next() {

                isFirst = false;
                return getToString();
            }

            @Override
            public void remove() {

                throw new UnsupportedOperationException();
            }
        };
        return it;
    }

    /**
     * Supports the use of the <code>empty</code> operator in the JSP EL by implementing the Collection interface.<p>
     *
     * @return always returns 0.<p>
     *
     * @see java.util.AbstractCollection#size()
     */
    @Override
    public int size() {

        return isEmpty() ? 0 : 1;
    }

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

    /**
     * Returns a value wrapper for the provided default in case this value is empty.<p>
     *
     * If this value is empty, the object returned will be of type {@link CmsJspObjectValueWrapper}.
     * This means you can only use simple "get me the value as type X" operations on the result safely.<p>
     *
     * @param defaultValue the object to generate the default value from
     *
     * @return  a value wrapper for the provided default in case this value is empty.
     *
     * @see CmsJspObjectValueWrapper#createWrapper(CmsObject, Object)
     */
    public A_CmsJspValueWrapper useDefault(Object defaultValue) {

        if (getIsEmptyOrWhitespaceOnly()) {
            return CmsJspObjectValueWrapper.createWrapper(getCmsObject(), defaultValue);
        }
        return this;
    }

    /**
     * Compares this value against a list of Strings and returns this value in case the list contains the
     * toString representation of this value, or a {@link CmsJspObjectValueWrapper#NULL_VALUE_WRAPPER} otherwise.<p>
     *
     * @param allowedValues the list of allowed String values
     *
     * @return returns this value in case the list contains the toString representation of this value,
     *         or a {@link CmsJspObjectValueWrapper#NULL_VALUE_WRAPPER} otherwise
     *
     * @see #validate(List, Object)
     * @see #isValid(List)
     */
    public A_CmsJspValueWrapper validate(List<String> allowedValues) {

        return isValid(allowedValues) ? this : CmsJspObjectValueWrapper.NULL_VALUE_WRAPPER;
    }

    /**
     * Checks this value against a list of Strings and returns this value in case the list contains the
     * toString representation of this value, or a {@link CmsJspObjectValueWrapper} based on the given default object otherwise.<p>
     *
     * @param allowedValues the list of allowed String values
     * @param defaultValue the object to generate the default value from
     *
     * @return returns this value in case the list contains the toString representation of this value,
     *         or a {@link CmsJspObjectValueWrapper} based on the given default object otherwise
     *
     * @see #validate(List)
     * @see #isValid(List)
     * @see #useDefault(Object)
     */
    public A_CmsJspValueWrapper validate(List<String> allowedValues, Object defaultValue) {

        return isValid(allowedValues) ? this : CmsJspObjectValueWrapper.createWrapper(getCmsObject(), defaultValue);
    }
}
