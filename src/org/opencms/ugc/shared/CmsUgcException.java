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

package org.opencms.ugc.shared;

import org.opencms.gwt.CmsRpcException;
import org.opencms.ugc.shared.CmsUgcConstants.ErrorCode;

/**
 * Exception class for use in the org.opencms.editors.usergenerated module.<p>
 *
 */
public class CmsUgcException extends CmsRpcException {

    /** Serial version id. */
    private static final long serialVersionUID = -8081364940852864867L;

    /** Contains the  error type. */
    private CmsUgcConstants.ErrorCode m_errorCode;

    /** The human-readable error message. */
    private String m_message;

    /**
     * Creates a new instance.<p>
     *
     * @param errorCode the error type
     * @param message the error message
     */
    public CmsUgcException(CmsUgcConstants.ErrorCode errorCode, String message) {

        setErrorCode(errorCode);
        m_message = message;
    }

    /**
     * Creates a new instance.<p>
     *
     * @param t the wrapped exception
     */
    public CmsUgcException(Throwable t) {

        super(t);
        setErrorCode(ErrorCode.errMisc);
        m_message = t.getLocalizedMessage();
    }

    /**
     * Creates a new instance.<p>
     *
     * @param t the original exception
     * @param errorCode the error type
     * @param message the error message
     */
    public CmsUgcException(Throwable t, CmsUgcConstants.ErrorCode errorCode, String message) {

        super(t);
        setErrorCode(errorCode);
        m_message = message;
    }

    /**
     * Default constructor for serialization.<p>
     */
    protected CmsUgcException() {

        // do nothing
    }

    /**
     * Returns the errorCode.<p>
     *
     * @return the errorCode
     */
    public CmsUgcConstants.ErrorCode getErrorCode() {

        return m_errorCode;
    }

    /**
     * Gets the human-readable message.<p>
     *
     * @return the human-readable message
     */
    public String getUserMessage() {

        return m_message;
    }

    /**
     * Sets the errorCode.<p>
     *
     * @param errorCode the errorCode to set
     */
    public void setErrorCode(CmsUgcConstants.ErrorCode errorCode) {

        m_errorCode = errorCode;
    }

}
