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

import org.opencms.file.CmsObject;
import org.opencms.main.CmsException;
import org.opencms.util.CmsUUID;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * The alias manager provides access to the aliases stored in the database.<p>
 */
public class CmsAliasManager {

    /** The security manager for accessing the database. */
    protected CmsSecurityManager m_securityManager;

    /**
     * Creates a new alias manager instance.<p>
     *
     * @param securityManager the security manager
     */
    public CmsAliasManager(CmsSecurityManager securityManager) {

        m_securityManager = securityManager;
    }

    /**
     * Gets the list of aliases for a path in a given site.<p>
     *
     * This should only return either an empty list or a list with a single element.
     *
     *
     * @param cms the current CMS context
     * @param siteRoot the site root for which we want the aliases
     * @param aliasPath the alias path
     *
     * @return the aliases for the given site root and path
     *
     * @throws CmsException if something goes wrong 
     */
    public List<CmsAlias> getAliasesForPath(CmsObject cms, String siteRoot, String aliasPath) throws CmsException {

        CmsAlias alias = m_securityManager.readAliasByPath(cms.getRequestContext(), siteRoot, aliasPath);
        if (alias == null) {
            return Collections.emptyList();
        } else {
            return Collections.singletonList(alias);
        }
    }

    /**
     * Gets the list of aliases for a given site root.<p>
     * 
     * @param cms the current CMS context  
     * @param siteRoot the site root 
     * @return the list of aliases for the given site 
     * 
     * @throws CmsException if something goes wrong 
     */
    public List<CmsAlias> getAliasesForSite(CmsObject cms, String siteRoot) throws CmsException {

        return m_securityManager.getAliasesForSite(cms.getRequestContext(), siteRoot);
    }

    /**
     * Gets the aliases for a given structure id.<p>
     *
     * @param cms the current CMS context
     * @param structureId the structure id of a resource
     *
     * @return the aliases which point to the resource with the given structure id
     *
     * @throws CmsException if something goes wrong
     */
    public List<CmsAlias> getAliasesForStructureId(CmsObject cms, CmsUUID structureId) throws CmsException {

        List<CmsAlias> aliases = m_securityManager.readAliasesById(cms.getRequestContext(), structureId);
        Collections.sort(aliases, new Comparator<CmsAlias>() {

            public int compare(CmsAlias first, CmsAlias second) {

                return first.getAliasPath().compareTo(second.getAliasPath());
            }
        });
        return aliases;
    }

    /**
     * Saves the aliases for a given structure id, <b>completely replacing</b> any existing aliases for the same structure id.<p>
     *
     * @param cms the current CMS context
     * @param structureId the structure id of a resource
     * @param aliases the list of aliases which should be written
     *
     * @throws CmsException if something goes wrong
     */
    public void saveAliases(CmsObject cms, CmsUUID structureId, List<CmsAlias> aliases) throws CmsException {

        m_securityManager.saveAliases(cms.getRequestContext(), cms.readResource(structureId), aliases);
    }

}
