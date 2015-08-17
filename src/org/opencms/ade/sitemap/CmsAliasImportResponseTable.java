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

package org.opencms.ade.sitemap;

import org.opencms.gwt.shared.alias.CmsAliasImportResult;
import org.opencms.util.CmsUUID;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A class used for storing alias import results by key.
 */
public class CmsAliasImportResponseTable {

    /** A map of alias import results. */
    private Map<String, List<CmsAliasImportResult>> m_entries = new HashMap<String, List<CmsAliasImportResult>>();

    /**
     * Adds a list of alias import results, and returns the key under which they were stored.<p>
     *
     * @param importResults the alias import results
     *
     * @return the key under which the alias import results were stored
     */
    public synchronized String addImportResult(List<CmsAliasImportResult> importResults) {

        String key = (new CmsUUID()).toString();
        m_entries.put(key, importResults);
        return key;
    }

    /**
     * Removes the list of alias import results for the given key and returns it.<p>
     *
     * @param key the alias import result key
     *
     * @return the list of alias import results
     */
    public synchronized List<CmsAliasImportResult> getAndRemove(String key) {

        List<CmsAliasImportResult> result = m_entries.get(key);
        if (result != null) {
            m_entries.remove(key);
        }
        return result;
    }
}
