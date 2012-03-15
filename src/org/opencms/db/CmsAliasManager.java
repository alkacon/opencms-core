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

package org.opencms.db;

import org.opencms.ade.sitemap.shared.CmsAliasMode;
import org.opencms.main.CmsException;
import org.opencms.util.CmsUUID;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

public class CmsAliasManager {

    public static final String LUCKY_PATH = "/lucky";

    public static final CmsUUID LUCKY_UUID = new CmsUUID("b7e482d3-77ef-11e0-be13-000c2972a6a4");
    public static final CmsAlias X_DUMMY_ALIAS = new CmsAlias(
        LUCKY_UUID,
        "/sites/default",
        LUCKY_PATH,
        CmsAliasMode.page);

    protected CmsSecurityManager m_securityManager;
    private List<CmsAlias> m_aliases = new ArrayList<CmsAlias>();

    public CmsAliasManager(CmsSecurityManager securityManager) {

        m_securityManager = securityManager;
        m_aliases.add(X_DUMMY_ALIAS);
    }

    public void clearAliases(CmsUUID structureId) throws CmsException {

        Iterator<CmsAlias> it = m_aliases.iterator();
        while (it.hasNext()) {
            CmsAlias alias = it.next();
            if (alias.getStructureId().equals(structureId)) {
                it.remove();
            }
        }
    }

    public List<CmsAlias> getAliasesForPath(String siteRoot, String aliasPath) throws CmsException {

        for (CmsAlias alias : m_aliases) {
            if (siteRoot.equals(alias.getSiteRoot()) && aliasPath.equals(alias.getAliasPath())) {
                return Collections.singletonList(alias);
            }
        }
        return new ArrayList<CmsAlias>();
    }

    /**
     * @param structureId 
     * @return
     */
    public List<CmsAlias> getAliasesForStructureId(CmsUUID structureId) throws CmsException {

        List<CmsAlias> result = new ArrayList<CmsAlias>();
        for (CmsAlias alias : m_aliases) {
            if (alias.getStructureId().equals(structureId)) {
                result.add(alias);
            }
        }
        return result;
    }

    public void saveAliases(CmsUUID structureId, List<CmsAlias> aliases) throws CmsException {

        clearAliases(structureId);
        m_aliases.addAll(aliases);
    }

}
