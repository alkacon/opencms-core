/*
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (c) Alkacon Software GmbH & Co. KG (https://www.alkacon.com)
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
 * For further information about Alkacon Software GmbH & Co. KG, please see the
 * company website: https://www.alkacon.com
 *
 * For further information about OpenCms, please see the
 * project website: https://www.opencms.org
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package org.opencms.file;

import org.opencms.i18n.CmsMessageContainer;
import org.opencms.main.CmsException;

/**
 * Signals that an attempt to call a method has failed since it is not implemented.
 * This exception may be thrown by various driver implementation classes. <p>
 *
 * @since 6.0.0
 */
public class CmsDataNotImplementedException extends CmsDataAccessException {

    /** Serial version UID required for safe serialization. */
    private static final long serialVersionUID = -2896104363126492116L;

    /**
     * @see org.opencms.main.CmsException#CmsException(CmsMessageContainer)
     */
    public CmsDataNotImplementedException(CmsMessageContainer container) {

        super(container);
    }

    /**
     * @see org.opencms.main.CmsException#CmsException(CmsMessageContainer, Throwable)
     */
    public CmsDataNotImplementedException(CmsMessageContainer container, Throwable cause) {

        super(container, cause);
    }

    /**
     * @see org.opencms.main.CmsException#createException(org.opencms.i18n.CmsMessageContainer, java.lang.Throwable)
     */
    @Override
    public CmsException createException(CmsMessageContainer container, Throwable cause) {

        return new CmsDataNotImplementedException(container, cause);
    }
}