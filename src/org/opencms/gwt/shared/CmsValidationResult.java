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
 * The result of a single field validation.<p>
 *
 * @since 8.0.0
 */
public class CmsValidationResult implements IsSerializable {

    /** Convenience constant which contains a validation result for successful validations. */
    public static final CmsValidationResult VALIDATION_OK = new CmsValidationResult(null);

    /** The error message to display, or null. */
    private String m_errorMessage;

    /** The flag indicating whether the field value should be replaced. */
    private boolean m_hasNewValue;

    /** The replacement for the field value. */
    private String m_newValue;

    /**
     * Creates a new validation result which doesn't replace the current field value.<p>
     *
     * @param errorMessage the error message to display, or null if there
     */
    public CmsValidationResult(String errorMessage) {

        m_errorMessage = errorMessage;
        m_hasNewValue = false;
    }

    /**
     * Creates a new validation result which also replaces the current field value.<p>
     *
     * @param errorMessage the error message to display, or null if there was no error
     * @param newValue the replacement for the field value
     */
    public CmsValidationResult(String errorMessage, String newValue) {

        m_errorMessage = errorMessage;
        m_newValue = newValue;
        m_hasNewValue = true;
    }

    /**
     * Hidden default constructor.<p>
     */
    protected CmsValidationResult() {

        // do nothing
    }

    /**
     * Returns the error message, or null if the validation has succeeded.<p>
     *
     * @return an error message or null the
     */
    public String getErrorMessage() {

        return m_errorMessage;
    }

    /**
     * Returns the replacement for the field value.<p>
     *
     * @return the replacement for the field value
     */
    public String getNewValue() {

        return m_newValue;
    }

    /**
     * Returns true if the field value should be replaced.<p>
     *
     * @return true if the field value should be replaced
     */
    public boolean hasNewValue() {

        return m_hasNewValue;
    }

    /**
     * Returns true if the validation has succeeded.<p>
     *
     * @return true if the validation has succeeded
     */
    public boolean isOk() {

        return m_errorMessage == null;
    }

}
