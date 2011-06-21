/*
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (c) Alkacon Software GmbH (http://www.alkacon.com)
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
 * For further information about Alkacon Software GmbH, please see the
 * company website: http://www.alkacon.com
 *
 * For further information about OpenCms, please see the
 * project website: http://www.opencms.org
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package org.opencms.staticexport;

import org.opencms.relations.CmsLink;
import org.opencms.relations.CmsRelationType;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Maintains a table of links for an element of a CmsXmlPage.<p>
 * 
 * @since 6.0.0 
 */
public class CmsLinkTable {

    /** Prefix to identify a link in the content. */
    private static final String LINK_PREFIX = "link";

    /** The map to store the link table in. */
    private Map<String, CmsLink> m_linkTable;

    /**
     * Creates a new CmsLinkTable.<p>
     */
    public CmsLinkTable() {

        m_linkTable = new HashMap<String, CmsLink>();
    }

    /**
     * Adds a new link with a given internal name and internal flag to the link table.<p>
     * 
     * @param link the <code>CmsLink</code> to add
     * @return the new link entry
     */
    public CmsLink addLink(CmsLink link) {

        m_linkTable.put(link.getName(), link);
        return link;
    }

    /**
     * Adds a new link to the link table.<p>
     * 
     * @param type type of the link
     * @param targetUri link destination
     * @param internal flag to indicate if the link is a local link
     * 
     * @return the new link entry
     */
    public CmsLink addLink(CmsRelationType type, String targetUri, boolean internal) {

        CmsLink link = new CmsLink(LINK_PREFIX + m_linkTable.size(), type, targetUri, internal);
        m_linkTable.put(link.getName(), link);
        return link;
    }

    /**
     * Returns the CmsLink Entry for a given name.<p>
     * 
     * @param name the internal name of the link
     * @return the CmsLink entry
     */
    public CmsLink getLink(String name) {

        return m_linkTable.get(name);
    }

    /**
     * Returns if the link table is empty.<p>
     * 
     * @return true if the link table is empty, false otherwise
     */
    public boolean isEmpty() {

        return m_linkTable.isEmpty();
    }

    /**
     * Returns an iterator over the links in the table.<p>
     * 
     * The objects iterated are of type <code>{@link CmsLink}</code>.
     * 
     * @return a string iterator for internal link names
     */
    public Iterator<CmsLink> iterator() {

        return m_linkTable.values().iterator();
    }

    /**
     * Returns the size of this link table.<p>
     * 
     * @return the size of this link table
     */
    public int size() {

        return m_linkTable.size();
    }
}
