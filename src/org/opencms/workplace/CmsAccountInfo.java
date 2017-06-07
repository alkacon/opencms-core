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

package org.opencms.workplace;

import org.opencms.file.CmsUser;
import org.opencms.main.CmsLog;

import java.lang.reflect.InvocationTargetException;

import org.apache.commons.beanutils.PropertyUtilsBean;
import org.apache.commons.logging.Log;

/**
 * Account info bean.<p>
 */
public class CmsAccountInfo {

    /** Account info fields. */
    public enum Field {
        /** An additional info field. */
        addinfo,

        /** The address field. */
        address,

        /** The city field. */
        city,

        /** The country field. */
        country,

        /** The email field. */
        email,

        /** The first name field. */
        firstname,

        /** The institution field. */
        institution,

        /** The last name field. */
        lastname,

        /** The zip code field. */
        zipcode
    }

    /** Logger instance for this class. */
    private static final Log LOG = CmsLog.getLog(CmsAccountInfo.class);

    /** The additional info key. */
    private String m_addInfoKey;

    /** The editable flag. */
    private boolean m_editable;

    /** The field. */
    private Field m_field;

    /**
     * Constructor.<p>
     *
     * @param field the field
     * @param addInfoKey the additional info key
     * @param editable the editable flag
     */
    public CmsAccountInfo(Field field, String addInfoKey, boolean editable) {
        m_field = field;
        m_addInfoKey = addInfoKey;
        m_editable = editable;
    }

    /**
     * Constructor.<p>
     *
     * @param field the field
     * @param addInfoKey the additional info key
     * @param editable the editable flag
     */
    public CmsAccountInfo(String field, String addInfoKey, String editable) {
        m_field = Field.valueOf(field);
        m_addInfoKey = addInfoKey;
        m_editable = Boolean.parseBoolean(editable);
    }

    /**
     * Returns the additional info key.<p>
     *
     * @return the additional info key
     */
    public String getAddInfoKey() {

        return m_addInfoKey;
    }

    /**
     * Returns the field.<p>
     *
     * @return the field
     */
    public Field getField() {

        return m_field;
    }

    /**
     * Returns the account info value for the given user.<p>
     *
     * @param user the user
     *
     * @return the value
     */
    public String getValue(CmsUser user) {

        String value = null;
        if (isAdditionalInfo()) {
            value = (String)user.getAdditionalInfo(getAddInfoKey());
        } else {
            try {
                PropertyUtilsBean propUtils = new PropertyUtilsBean();
                value = (String)propUtils.getProperty(user, getField().name());
            } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
                LOG.error("Error reading account info field.", e);
            }
        }
        return value;
    }

    /**
     * Returns whether this is an additional info field.<p>
     * @return <code>true</code> in case of an additional info field
     */
    public boolean isAdditionalInfo() {

        return Field.addinfo.equals(m_field);
    }

    /**
     * Returns if the field is editable.<p>
     *
     * @return if the field is editable
     */
    public boolean isEditable() {

        return m_editable;
    }

}
