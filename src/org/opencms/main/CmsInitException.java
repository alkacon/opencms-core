/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/main/CmsInitException.java,v $
 * Date   : $Date: 2005/04/22 08:46:44 $
 * Version: $Revision: 1.9 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Mananagement System
 *
 * Copyright (C) 2002 - 2005 Alkacon Software (http://www.alkacon.com)
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

package org.opencms.main;

import org.opencms.i18n.CmsMessageContainer;

/**
 * Describes errors that occur in the context of OpenCms the initialization, this is fatal
 * and prevents OpenCms from starting.<p>
 * 
 * If an Exception of this class is thrown, OpenCms is set to an error state and 
 * the system won't try to start up again.<p>
 * 
 * @author Alexander Kandzior (a.kandzior@alkacon.com)
 * @since 5.7.3
 */
public class CmsInitException extends CmsRuntimeException implements I_CmsThrowable {

    /** Indicates that this exception describes a new error. */
    private boolean m_newError;

    /**
     * Creates a new localized Exception.<p>
     * 
     * @param container the localized message container to use
     */
    public CmsInitException(CmsMessageContainer container) {

        this(container, true);
    }

    /**
     * Creates a new localized Exception.<p>
     * 
     * @param container the localized message container to use
     * @param newError indicates that the error is new, and OpenCms should be stopped
     */
    public CmsInitException(CmsMessageContainer container, boolean newError) {

        super(container);
        m_newError = newError;
        if (m_newError) {
            setErrorCondition();
        }
    }

    /**
     * Creates a new localized Exception that also containes a root cause.<p>
     * 
     * @param container the localized message container to use
     * @param cause the Exception root cause
     */
    public CmsInitException(CmsMessageContainer container, Throwable cause) {

        super(container, cause);
        m_newError = true;
        setErrorCondition();
    }

    /**
     * Indicates that this exception describes a new error that was not already logged.<p>
     * 
     * @return <code>true</code> if this exception describes a new error that was not already logged
     */
    public boolean isNewError() {

        return m_newError;
    }

    /**
     * Prints an error message to the System.err stream, indicating that OpenCms
     * is unable to start up.<p>
     */
    private void setErrorCondition() {

        CmsMessageContainer errorCondition = getMessageContainer();
        OpenCmsCore.setErrorCondition(errorCondition);
    }
}
