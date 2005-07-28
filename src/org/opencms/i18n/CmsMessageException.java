/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/i18n/CmsMessageException.java,v $
 * Date   : $Date: 2005/07/28 15:18:32 $
 * Version: $Revision: 1.1 $
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

package org.opencms.i18n;

import org.opencms.main.CmsException;

/**
 * Used to signal issues with the localization support, for example during localized messages access.<p> 
 * 
 * @author Michael Moossen
 * 
 * @version $Revision: 1.1 $ 
 * 
 * @since 6.0.0 
 */
public class CmsMessageException extends CmsException {

    /** Serial version UID required for safe serialization. */
    private static final long serialVersionUID = -7012912310293845492L;

    /**
     * Creates a new localized Exception.<p>
     * 
     * @param container the localized message container to use
     */
    public CmsMessageException(CmsMessageContainer container) {

        super(container);
    }

    /**
     * Creates a new localized Exception that also containes a root cause.<p>
     * 
     * @param container the localized message container to use
     * @param cause the Exception root cause
     */
    public CmsMessageException(CmsMessageContainer container, Throwable cause) {

        super(container, cause);
    }

    /**
     * @see org.opencms.main.CmsException#createException(org.opencms.i18n.CmsMessageContainer, java.lang.Throwable)
     */
    public CmsException createException(CmsMessageContainer container, Throwable cause) {

        return new CmsMessageException(container, cause);
    }
}