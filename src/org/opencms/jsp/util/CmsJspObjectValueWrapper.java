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
import org.opencms.jsp.CmsJspResourceWrapper;
import org.opencms.main.CmsException;
import org.opencms.main.CmsLog;
import org.opencms.util.CmsStringUtil;
import org.opencms.xml.types.I_CmsXmlContentValue;

import java.util.Collection;
import java.util.Date;

import org.apache.commons.logging.Log;

/**
 * Provides access to common object types through wrappers.<p>
 *
 * @since 11.0
 */
public final class CmsJspObjectValueWrapper extends A_CmsJspValueWrapper {

    /** The log object for this class. */
    private static final Log LOG = CmsLog.getLog(CmsJspObjectValueWrapper.class);

    /** Constant for the null (non existing) value. */
    protected static final CmsJspObjectValueWrapper NULL_VALUE_WRAPPER = new CmsJspObjectValueWrapper();

    /** Calculated hash code. */
    private int m_hashCode;

    /** The wrapped XML content value. */
    private Object m_object;

    /**
     * Private constructor, used for creation of NULL constant value, use factory method to create instances.<p>
     *
     * @see #createWrapper(CmsObject, Object)
     */
    private CmsJspObjectValueWrapper() {

        // cast needed to avoid compiler confusion with constructors
        this((CmsObject)null, (I_CmsXmlContentValue)null);
    }

    /**
     * Private constructor, use factory method to create instances.<p>
     *
     * @param cms the current users OpenCms context
     * @param value the object to warp
     */
    private CmsJspObjectValueWrapper(CmsObject cms, Object value) {

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
    public static CmsJspObjectValueWrapper createWrapper(CmsObject cms, Object value) {

        if ((value != null) && (cms != null)) {
            return new CmsJspObjectValueWrapper(cms, value);
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
        } else if (m_object instanceof Collection) {
            // if a collection has any entries it is not emtpy
            return !((Collection)m_object).isEmpty();
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
     * Returns the wrapped object value.<p>
     *
     * @return the wrapped object value
     */
    @Override
    public Object getObjectValue() {

        return m_object;
    }

    /**
     * Converts the wrapped value to a date. Keeps it as date, if it already is one.<p>
     *
     * @return the date
     *
     * @see A_CmsJspValueWrapper#getToDate()
     */
    @Override
    public Date getToDate() {

        if (m_object instanceof Date) {
            return (Date)m_object;
        }
        return super.getToDate();
    }

    /**
     * @see org.opencms.jsp.util.A_CmsJspValueWrapper#getToResource()
     */
    @Override
    public CmsJspResourceWrapper getToResource() {

        try {
            return CmsJspElFunctions.convertResource(m_cms, m_object);
        } catch (CmsException e) {
            if (LOG.isInfoEnabled()) {
                LOG.info("Failed to convert object \"" + getToString() + "\" to a resource.", e);
            }
            return null;
        }
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