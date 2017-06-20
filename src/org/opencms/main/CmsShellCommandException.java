/*
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (c) Alkacon Software GmbH (http://www.alkacon.com)
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

/**
 * Wrapper exception used to notify the shell that an error has occurred during execution of a shell command.<p>
 */
public class CmsShellCommandException extends RuntimeException {

    /** Serial version id. */
    private static final long serialVersionUID = 5501584058087027184L;

    /** Indicates whether this exception was created because of a report error. */
    private boolean m_fromReport;

    /**
     * Creates a new instance.<p>
     *
     * @param fromReport true if this exception is being created because of a report error
     */
    public CmsShellCommandException(boolean fromReport) {
        super("Shell command exception caused by report error");
        m_fromReport = fromReport;
    }

    /**
     * Creates a new instance.<p>
     *
     * @param t the cause of this exception
     */
    public CmsShellCommandException(Throwable t) {
        super("Shell command exception caused by different exception", t);
    }

    /**
     * Returns true if this exception was created because of a report error.<p>
     *
     * @return true if this exception was created because of a report error
     */
    public boolean isFromReport() {

        return m_fromReport;
    }

}
