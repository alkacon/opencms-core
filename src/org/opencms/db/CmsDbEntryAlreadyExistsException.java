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
 * For further information about Alkacon Software GmbH & Co. KG, please see the
 * company website: http://www.alkacon.com
 *
 * For further information about OpenCms, please see the
 * project website: http://www.opencms.org
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package org.opencms.db;

import org.opencms.i18n.CmsMessageContainer;
import org.opencms.main.CmsException;

/**
 * Signals that an attempt to create an object was not successfull because it already exists.<p>
 *
 * @since 6.0.0
 */
public class CmsDbEntryAlreadyExistsException extends CmsDbException {

    /** Serial version UID required for safe serialization. */
    private static final long serialVersionUID = -3605466011434350322L;

    /**
     * @see org.opencms.main.CmsException#CmsException(CmsMessageContainer)
     */
    public CmsDbEntryAlreadyExistsException(CmsMessageContainer container) {

        super(container);
    }

    /**
     * @see org.opencms.main.CmsException#CmsException(CmsMessageContainer, Throwable)
     */
    public CmsDbEntryAlreadyExistsException(CmsMessageContainer container, Throwable cause) {

        super(container, cause);
    }

    /**
     * @see org.opencms.main.CmsException#createException(org.opencms.i18n.CmsMessageContainer, java.lang.Throwable)
     */
    @Override
    public CmsException createException(CmsMessageContainer container, Throwable cause) {

        return new CmsDbEntryAlreadyExistsException(container, cause);
    }
}
