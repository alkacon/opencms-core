/*
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (C) Alkacon Software (http://www.alkacon.com)
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

package org.opencms.gwt.shared.alias;

import com.google.gwt.user.client.rpc.IsSerializable;

/**
 * The values of this enum describe what should happen when a request to an aliased resource
 * comes in.<p>
 */
public enum CmsAliasMode implements IsSerializable {

    /** The request will be forwarded internally to the given resource. */
    page(0), /** Pass the new path along to the next resource handler. */
    passthrough(3), /** A 'moved permanently' status with a link to the aliased resource will be sent to the browser. */
    permanentRedirect(
    2), /** A 'moved temporarily' status with a link to the aliased resource will be sent to the browser. */
    redirect(1);

    /** The id used for storing alias modes in the database. */
    private final int m_id;

    /**
     * Creates a new enum constant.<p>
     *
     * @param id the id used in the database
     */
    CmsAliasMode(int id) {

        m_id = id;
    }

    /**
     * Gets the enum constant for a given integer id.<p>
     *
     * @param id the integer id
     * @return the enum constant matching that id
     */
    public static CmsAliasMode fromInt(int id) {

        switch (id) {
            case 1:
                return redirect;
            case 2:
                return permanentRedirect;
            case 3:
                return passthrough;
            case 0:
            default:
                return page;
        }
    }

    /**
     * Checks whether this is a mode that requires a redirect.<p>
     *
     * @return true if this mode requires a redirect
     */
    public boolean isRedirect() {

        return (this == redirect) || (this == permanentRedirect);
    }

    /**
     * Converts an enum constant to the id used for storing alias modes in the database.<p>
     *
     * @return the enum constant id
     */
    public int toInt() {

        return m_id;
    }

}
