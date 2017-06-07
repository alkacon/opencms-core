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

package org.opencms.gwt.shared;

import com.google.gwt.user.client.rpc.IsSerializable;

/**
 * A simple bean class which represents a validation request for a single form field.<p>
 *
 * @since 8.0.0
 */
public class CmsValidationQuery implements IsSerializable {

    /** The configuration string for the server-side validator. */
    private String m_config;

    /** The class name of the server-side validator. */
    private String m_validatorId;

    /** The value which should be validated. */
    private String m_value;

    /**
     * Constructor.<p>
     *
     * @param validator the server-side validator class name
     * @param value the value to validate
     * @param config the configuration string for the server-side validator
     */
    public CmsValidationQuery(String validator, String value, String config) {

        m_validatorId = validator;
        m_value = value;
        m_config = config;
    }

    /**
     * Hidden default constructor.<p>
     */
    protected CmsValidationQuery() {

        // do nothing
    }

    /**
     * Gets the configuration string for the server-side validator.<p>
     *
     * @return a configuration string
     */
    public String getConfig() {

        return m_config;
    }

    /**
     * Gets the class name of the server-side validator.<p>
     *
     * @return  the class name of the server-side validator
     */
    public String getValidatorId() {

        return m_validatorId;
    }

    /**
     * Returns the value to validate.<p>
     *
     * @return the value which should be validated
     */
    public String getValue() {

        return m_value;
    }
}
