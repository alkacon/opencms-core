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

package org.opencms.mx;

/**
 * Bean interface for special diagnostic information retrievable via JMX.
 */
public interface I_CmsDiagnosticsMXBean {

    /**
     * Contains a textual representation of the current requests running in the OpenCmsServlet.
     * <p>
     * For each request, a line of text containing the thread id of the thread handling the request, the request URL, and the current runtime is produced. They look like this:
     *  <p>
     *  (#67) http://www.adfasdfasdf.com 33333
     *
     * @return a textual representation of the current requests in the OpenCmsServlet
     */
    public String listActiveRequests();

}
