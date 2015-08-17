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

package org.opencms.cmis;

import java.util.HashSet;
import java.util.Set;

/**
 * This class encapsulates the filter logic for CMIS rendition filters.<p>
 */
public class CmsCmisRenditionFilter {

    /** Flag which indicates that all renditions should be returned. */
    private boolean m_includeAll;

    /** The kinds which will be accepted. */
    private Set<String> m_kinds = new HashSet<String>();

    /** The MIME supertypes which will be accepted. */
    private Set<String> m_supertypes = new HashSet<String>();

    /** The MIME types which will be accepted. */
    private Set<String> m_types = new HashSet<String>();

    /**
     * Creates a new filter from the given filter string.<p>
     *
     * @param filterStr the CMIS rendition filter string
     */
    public CmsCmisRenditionFilter(String filterStr) {

        if ((filterStr == null) || filterStr.equals("cmis:none")) {
            return;
        } else if ("*".equals(filterStr)) {
            m_includeAll = true;
        } else {
            String[] tokens = filterStr.split(",");
            for (String token : tokens) {
                int slashPos = token.indexOf("/");
                if (slashPos > -1) {
                    String supertype = token.substring(0, slashPos);
                    String subtype = token.substring(slashPos + 1);
                    if ("*".equals(subtype)) {
                        m_supertypes.add(supertype);
                    } else {
                        m_types.add(token);
                    }
                } else {
                    m_kinds.add(token);
                }
            }
        }
    }

    /**
     * Checks whether this filter accepts a given kind/mimetype combination.<p>
     *
     * @param kind the kind
     * @param mimetype the mime type
     *
     * @return true if the filter accepts the combination
     */
    public boolean accept(String kind, String mimetype) {

        if (m_includeAll) {
            return true;
        }
        int slashpos = mimetype.indexOf("/");
        String supertype = mimetype.substring(0, slashpos);
        return m_kinds.contains(kind) || m_types.contains(mimetype) || m_supertypes.contains(supertype);
    }

}
