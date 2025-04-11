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
 * For further information about Alkacon Software, please see the
 * company website: https://www.alkacon.com
 *
 * For further information about OpenCms, please see the
 * project website: https://www.opencms.org
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package org.opencms.xml.xml2json.handler;

public class CmsJsonHandlerException extends Exception {

    /** The serial version id. */
    private static final long serialVersionUID = 5155325235329055833L;

    /** The cause. */
    private Throwable m_cause;

    /**
     * Constructs a JSONException with an explanatory message.<p>
     *
     * @param message details about the reason for the exception
     */
    public CmsJsonHandlerException(String message) {

        super(message);
    }

    /**
     * Constructs a JSONException with an explanatory message.<p>
     *
     * @param t the throwable that was thrown
     */
    public CmsJsonHandlerException(Throwable t) {

        super(t.getMessage());
        m_cause = t;
    }

    /**
     * Returns the cause of the exception.<p>
     *
     *  @return the cause
     */
    @Override
    public Throwable getCause() {

        return m_cause;
    }
}
