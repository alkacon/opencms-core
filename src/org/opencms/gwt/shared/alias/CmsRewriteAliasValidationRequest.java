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

import java.util.List;

import com.google.gwt.user.client.rpc.IsSerializable;

/**
 * This method represents a query to validate a set of rewrite aliases on the  server.<p>
 */
public class CmsRewriteAliasValidationRequest implements IsSerializable {

    /** The rewrite aliases to validate. */
    private List<CmsRewriteAliasTableRow> m_editedRewrites;

    /**
     * Creates a new instance.<p>
     *
     * @param editedRewrites the list of rewrite aliases to validate
     */
    public CmsRewriteAliasValidationRequest(List<CmsRewriteAliasTableRow> editedRewrites) {

        m_editedRewrites = editedRewrites;
    }

    /**
     * Default constructor for serialization.<p>
     */
    protected CmsRewriteAliasValidationRequest() {

        // empty
    }

    /**
     * Gets the rewrite aliases which should be validated.<p>
     *
     * @return the list of rewrite aliases to validate
     */
    public List<CmsRewriteAliasTableRow> getEditedRewriteAliases() {

        return m_editedRewrites;
    }

}
