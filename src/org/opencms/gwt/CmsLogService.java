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

package org.opencms.gwt;

import org.opencms.gwt.shared.rpc.I_CmsLogService;
import org.opencms.main.CmsLog;

import org.apache.commons.logging.Log;

/**
 * Handles client side logging.<p>
 *
 * @since 8.0.0
 *
 * @see org.opencms.gwt.CmsLogService
 * @see org.opencms.gwt.shared.rpc.I_CmsLogService
 * @see org.opencms.gwt.shared.rpc.I_CmsLogServiceAsync
 */
public class CmsLogService extends CmsGwtService implements I_CmsLogService {

    /** Serialization uid. */
    private static final long serialVersionUID = -7136544324371767330L;

    /** The log object for this class. */
    private static final Log LOG = CmsLog.getLog(CmsLogService.class);

    /**
     * @see org.opencms.gwt.shared.rpc.I_CmsLogService#log(java.lang.String, java.lang.String)
     */
    public void log(String ticket, String message) {

        String[] args = new String[3 + (ticket == null ? 0 : 1)];
        args[0] = getRequest().getRemoteHost();
        args[1] = getRequest().getRemoteAddr();
        args[2] = message;
        if (ticket != null) {
            args[3] = ticket;
        }
        String key = (ticket == null ? Messages.LOG_CLIENT_WITHOUT_TICKET_3 : Messages.LOG_CLIENT_WITH_TICKET_4);
        String logMsg = Messages.get().getBundle().key(key, args);
        LOG.error(logMsg);
    }
}
