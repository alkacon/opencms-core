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

package org.opencms.acacia.shared.rpc;

import org.opencms.util.CmsPair;

import java.util.Collection;
import java.util.Date;

import com.google.gwt.user.client.rpc.RemoteService;

/** Synchronous interface for the serial date service. */
public interface I_CmsSerialDateService extends RemoteService {

    /**
     * Get the dates of the specified series
     * @param config series specification (widget's string value)
     * @return the dates of the specified series, each with a flag, indicating if it is really taking place (or excluded as an exception).
     */
    Collection<CmsPair<Date, Boolean>> getDates(String config);

    /**
     * Get information on the series specified by the current value.
     * @param config series specification (widget's string value)
     * @return a flag, indicating if the value is valid, accompanied with a suitable status message.
     */
    CmsPair<Boolean, String> getStatus(String config);
}
