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

package org.opencms.main;

import org.opencms.i18n.CmsMessageContainer;

/**
 * This exeption is thrown by a class which implements org.opencms.main.I_CmsResourceInit.
 * When this exeption is thrown,
 * all other implementations of I_CmsResourceInit will not be executed.<p>
 *
 * @since 6.0.0
 */
public class CmsResourceInitException extends CmsException {

    /** Serial version UID required for safe serialization. */
    private static final long serialVersionUID = 4896514314866157082L;

    /** The 'clear errors' flag. */
    private boolean m_clearErrors;

    /**
     * Creates a resource init exception for a given resource init handler class.<p>
     *
     * @param cls the resource init handler class
     */
    public CmsResourceInitException(Class<? extends I_CmsResourceInit> cls) {

        this(Messages.get().container(Messages.ERR_RESOURCE_INIT_ABORTED_1, cls.getName()));
    }

    /**
     * Creates a new localized Exception.<p>
     *
     * @param container the localized message container to use
     */
    public CmsResourceInitException(CmsMessageContainer container) {

        super(container);
    }

    /**
     * Creates a new localized Exception that also containes a root cause.<p>
     *
     * @param container the localized message container to use
     * @param cause the Exception root cause
     */
    public CmsResourceInitException(CmsMessageContainer container, Throwable cause) {

        super(container, cause);
    }

    /**
     * @see org.opencms.main.CmsException#createException(org.opencms.i18n.CmsMessageContainer, java.lang.Throwable)
     */
    @Override
    public CmsException createException(CmsMessageContainer container, Throwable cause) {

        return new CmsResourceInitException(container, cause);
    }

    /**
     * If this method returns true, the {@link OpenCmsCore#initResource(org.opencms.file.CmsObject, String, javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)}
     * method should just return null instead of throwing an exception.<p>
     *
     * @return the 'clear errors' flag
     */
    public boolean isClearErrors() {

        return m_clearErrors;
    }

    /**
     * Sets the 'clear errors' flag, which causes the resource init method catching this exception to return null of throwing an exception.
     * This can be useful if you want to redirect inside a resource init handler.<p>
     *
     * @param clearErrors the new value of the 'clear errors' flag
     */
    public void setClearErrors(boolean clearErrors) {

        m_clearErrors = clearErrors;
    }

}
