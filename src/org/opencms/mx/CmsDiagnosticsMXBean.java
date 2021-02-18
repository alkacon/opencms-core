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

import org.opencms.main.OpenCmsServlet;
import org.opencms.main.OpenCmsServlet.RequestInfo;

import java.lang.management.ManagementFactory;
import java.util.ArrayList;
import java.util.List;

import javax.management.MBeanServer;
import javax.management.ObjectName;

/**
 * Bean for special diagnostic information retrievable via JMX.
 */
public class CmsDiagnosticsMXBean implements I_CmsDiagnosticsMXBean {

    /** The instance. */
    public static final CmsDiagnosticsMXBean INSTANCE = new CmsDiagnosticsMXBean();

    /**
     * Registers an MBean of this class.
     *
     * @throws Exception if registration fails
     */
    public static void register() throws Exception {

        MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();
        ObjectName mxbeanName = new ObjectName("org.opencms.mx:type=CmsDiagnosticsMXBean");
        mbs.registerMBean(INSTANCE, mxbeanName);
    }

    /**
     * @see org.opencms.mx.I_CmsDiagnosticsMXBean#listActiveRequests()
     */
    public String listActiveRequests() {

        List<RequestInfo> infos = new ArrayList<>(OpenCmsServlet.activeRequests.values());
        long now = System.currentTimeMillis();
        infos.sort((a, b) -> Long.compare(a.getStartTime(), b.getStartTime()));
        StringBuilder result = new StringBuilder();
        for (RequestInfo info : infos) {
            String line = "(#" + info.getThreadId() + ") " + info.getUri() + " " + (now - info.getStartTime());
            result.append(line);
            result.append("\n");
        }
        return result.toString();
    }

}
