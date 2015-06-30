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

import org.opencms.util.CmsUUID;

import java.util.HashMap;
import java.util.Map;

import com.google.gwt.user.client.rpc.IsSerializable;

/**
 * This class represents the result of a validation of rewrite aliases.<p>
 */
public class CmsRewriteAliasValidationReply implements IsSerializable {

    /** A map from the rewrite alias IDs to the corresponding error messages. */
    private Map<CmsUUID, String> m_errors = new HashMap<CmsUUID, String>();

    /**
     * Default constructor.<p>
     */
    public CmsRewriteAliasValidationReply() {

        // do nothing
    }

    /**
     * Adds a validation error to this object.<p>
     *
     * @param id the id of a rewrite alias for which the validation failed
     * @param error the validation error message
     */
    public void addError(CmsUUID id, String error) {

        m_errors.put(id, error);
    }

    /**
     * Gets the map of error messages by rewrite alias id.<p>
     *
     * @return the map of error messages
     */
    public Map<CmsUUID, String> getErrors() {

        return m_errors;
    }
}
