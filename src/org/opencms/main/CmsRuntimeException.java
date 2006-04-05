/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/main/CmsRuntimeException.java,v $
 * Date   : $Date: 2005/07/03 09:41:52 $
 * Version: $Revision: 1.8 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Mananagement System
 *
 * Copyright (c) 2005 Alkacon Software GmbH (http://www.alkacon.com)
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
 * For further information about Alkacon Software GmbH, please see the
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

import java.util.Locale;

/**
 * A replacement for <code>{@link java.lang.RuntimeException}</code> to obtain fully
 * localized exception messages for OpenCms.<p>
 * 
 * @author Alexander Kandzior 
 * 
 * @version $Revision: 1.8 $ 
 * 
 * @since 6.0.0 
 */
public class CmsRuntimeException extends RuntimeException implements I_CmsThrowable {

    /** Serial version UID required for safe serialization. */
    private static final long serialVersionUID = -7855345575622173787L;

    /** The container for the localized message.  */
    protected CmsMessageContainer m_message;

    /**
     * Creates a new localized Exception.<p>
     * 
     * @param message the localized message container to use
     */
    public CmsRuntimeException(CmsMessageContainer message) {

        super(message.getKey());
        m_message = message;
    }

    /**
     * Creates a new localized Exception that also containes a root cause.<p>
     * 
     * @param message the localized message container to use
     * @param cause the Exception root cause
     */
    public CmsRuntimeException(CmsMessageContainer message, Throwable cause) {

        super(message.getKey(), cause);
        m_message = message;
    }

    /**
     * Creates a copied instance of this localized exception.<p>
     * 
     * @param container the message container
     * @param cause the root cause
     * 
     * @return a copied instance of this localized exception
     */
    public CmsRuntimeException createException(CmsMessageContainer container, Throwable cause) {

        return new CmsRuntimeException(container, cause);
    }

    /**
     * @see org.opencms.main.I_CmsThrowable#getLocalizedMessage()
     */
    public String getLocalizedMessage() {

        return m_message.key();
    }

    /**
     * @see org.opencms.main.I_CmsThrowable#getLocalizedMessage(Locale)
     */
    public String getLocalizedMessage(Locale locale) {

        return m_message.key(locale);
    }

    /**
     * @see java.lang.Throwable#getMessage()
     */
    public String getMessage() {

        return getLocalizedMessage();
    }

    /**
     * @see org.opencms.main.I_CmsThrowable#getMessageContainer()
     */
    public CmsMessageContainer getMessageContainer() {

        return m_message;
    }
}
