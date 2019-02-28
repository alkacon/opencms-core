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
import org.opencms.util.CmsStringUtil;

/**
 * Element setting access wrapper.<p>
 */
public class CmsJspElementSettingValueWrapper extends A_CmsJspValueWrapper {

    /** The instance of the enclosing context bean. */
    private final CmsJspStandardContextBean m_contextBean;

    /** Flag indicating the setting has been configured. */
    private boolean m_exists;

    /** Calculated hash code. */
    private int m_hashCode;

    /** The wrapped setting value, which always is a String. */
    private String m_value;

    /**
     * Constructor.<p>
     *
     * @param contextBean the standard context bean this setting was wrapped from
     * @param value the wrapped value
     * @param exists flag indicating the setting has been configured
     */
    CmsJspElementSettingValueWrapper(CmsJspStandardContextBean contextBean, String value, boolean exists) {

        m_contextBean = contextBean;
        m_value = value;
        m_exists = exists;
    }

    /**
     * @see org.opencms.jsp.util.A_CmsJspValueWrapper#getCmsObject()
     */
    @Override
    public CmsObject getCmsObject() {

        return m_contextBean.m_cms;
    }

    /**
     * Returns if the setting has been configured.<p>
     *
     * @return <code>true</code> if the setting has been configured
     */
    @Override
    public boolean getExists() {

        return m_exists;
    }

    /**
     * Returns if the setting value is null or empty.<p>
     *
     * @return <code>true</code> if the setting value is null or empty
     */
    @Override
    public boolean getIsEmpty() {

        return !m_exists || CmsStringUtil.isEmpty(m_value);
    }

    /**
     * Returns if the setting value is null or white space only.<p>
     *
     * @return <code>true</code> if the setting value is null or white space only
     */
    @Override
    public boolean getIsEmptyOrWhitespaceOnly() {

        return !m_exists || CmsStringUtil.isEmptyOrWhitespaceOnly(m_value);
    }

    /**
     * @see org.opencms.jsp.util.A_CmsJspValueWrapper#getObjectValue()
     */
    @Override
    public Object getObjectValue() {

        return m_value;
    }

    /**
     * Returns the raw value.<p>
     *
     * This may return <code>null</code>.
     *
     * @return the value
     *
     * @deprecated use {@link #getObjectValue()} instead
     */
    @Deprecated
    public String getValue() {

        return m_value;
    }

    /**
     * The hash code is based in the String value of the element setting.<p>
     *
     * @see org.opencms.jsp.util.A_CmsJspValueWrapper#hashCode()
     */
    @Override
    public int hashCode() {

        if (m_value == null) {
            return 0;
        }
        if (m_hashCode == 0) {
            m_hashCode = m_value.hashCode();
        }
        return m_hashCode;
    }

    /**
     * Returns the string value.<p>
     *
     * This will always be at least an empty String <code>""</code>, never <code>null</code>.
     *
     * @return the string value
     */
    @Override
    public String toString() {

        return m_value != null ? m_value : "";
    }
}