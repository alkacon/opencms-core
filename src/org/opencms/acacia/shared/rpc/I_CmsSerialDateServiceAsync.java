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

package org.opencms.acacia.shared.rpc;

import org.opencms.util.CmsPair;

import java.util.Collection;
import java.util.Date;

import com.google.gwt.user.client.rpc.AsyncCallback;

/** Asynchronous interface for the serial date service. */
public interface I_CmsSerialDateServiceAsync {

    /**
     * Get the dates of the specified series.
     * @param config series specification (widget's string value)
     * @param callback the callback function that takes the dates of the specified series, each with a flag, indicating if it is really taking place (or excluded as an exception).
     */
    void getDates(String config, AsyncCallback<Collection<CmsPair<Date, Boolean>>> callback);

    /**
     * Get information on the series specified by the current value.
     * @param config series specification (widget's string value)
     * @param callback the callback function that takes the status information.
     */
    void getStatus(String config, AsyncCallback<CmsPair<Boolean, String>> callback);

}
