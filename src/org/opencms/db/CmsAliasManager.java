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
import org.opencms.file.CmsResource;
import org.opencms.gwt.shared.alias.CmsAliasImportStatus;
import org.opencms.gwt.shared.alias.CmsAliasMode;
import org.opencms.main.CmsException;
import org.opencms.util.CmsUUID;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;

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
     * Imports a single alias.<p>
     * 
     * @param cms the current CMS context 
     * @param siteRoot the site root 
     * @param aliasPath the alias path  
     * @param vfsPath the VFS path 
     * @param mode the alias mode
     *  
     * @return the result of the import
     *  
     * @throws CmsException if something goes wrong 
     */
    public CmsAliasImportResult importAlias(
        CmsObject cms,
        String siteRoot,
        String aliasPath,
        String vfsPath,
        CmsAliasMode mode) throws CmsException {

        CmsResource resource;
        String originalSiteRoot = cms.getRequestContext().getSiteRoot();
        try {
            cms.getRequestContext().setSiteRoot(siteRoot);
            resource = cms.readResource(vfsPath);
        } catch (CmsException e) {
            return new CmsAliasImportResult(CmsAliasImportStatus.aliasError, "Could not read resource: " + vfsPath);
        } finally {
            cms.getRequestContext().setSiteRoot(originalSiteRoot);
        }
        if (!CmsAlias.ALIAS_PATTERN.matcher(aliasPath).matches()) {
            return new CmsAliasImportResult(CmsAliasImportStatus.aliasError, "Invalid alias path: " + aliasPath);
        }
        List<CmsAlias> maybeAlias = getAliasesForPath(cms, siteRoot, aliasPath);
        if (maybeAlias.isEmpty()) {
            CmsAlias newAlias = new CmsAlias(resource.getStructureId(), siteRoot, aliasPath, mode);
            m_securityManager.addAlias(cms.getRequestContext(), newAlias);
            return new CmsAliasImportResult(CmsAliasImportStatus.aliasNew, "OK");
        } else {
            CmsAlias existingAlias = maybeAlias.get(0);
            CmsAliasFilter deleteFilter = new CmsAliasFilter(
                siteRoot,
                existingAlias.getAliasPath(),
                existingAlias.getStructureId());
            m_securityManager.deleteAliases(cms.getRequestContext(), deleteFilter);
            CmsAlias newAlias = new CmsAlias(resource.getStructureId(), siteRoot, aliasPath, mode);
            m_securityManager.addAlias(cms.getRequestContext(), newAlias);
            return new CmsAliasImportResult(CmsAliasImportStatus.aliasChanged, "OK");
        }

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

    /**
     * Updates the aliases in the database.<p>
     * 
     * @param cms the current CMS context 
     * @param toDelete the collection of aliases to delete 
     * @param toAdd the collection of aliases to add
     * @throws CmsException if something goes wrong 
     */
    public void updateAliases(CmsObject cms, Collection<CmsAlias> toDelete, Collection<CmsAlias> toAdd)
    throws CmsException {

        Set<CmsUUID> allKeys = new HashSet<CmsUUID>();
        Multimap<CmsUUID, CmsAlias> toDeleteMap = ArrayListMultimap.create();

        // first, group the aliases by structure id

        for (CmsAlias alias : toDelete) {
            toDeleteMap.put(alias.getStructureId(), alias);
            allKeys.add(alias.getStructureId());
        }

        Multimap<CmsUUID, CmsAlias> toAddMap = ArrayListMultimap.create();
        for (CmsAlias alias : toAdd) {
            toAddMap.put(alias.getStructureId(), alias);
            allKeys.add(alias.getStructureId());
        }

        // Now update the aliases for each structure id...

        for (CmsUUID structureId : allKeys) {
            // Instead of deleting/adding the aliases individually, we load the current list of aliases, delete/add the entries from 
            // it, and then save the list again   

            Set<CmsAlias> aliasesToSave = new HashSet<CmsAlias>(getAliasesForStructureId(cms, structureId));
            Collection<CmsAlias> toDeleteForId = toDeleteMap.get(structureId);
            if ((toDeleteForId != null) && !toDeleteForId.isEmpty()) {
                aliasesToSave.removeAll(toDeleteForId);
            }
            Collection<CmsAlias> toAddForId = toAddMap.get(structureId);
            if ((toAddForId != null) && !toAddForId.isEmpty()) {
                aliasesToSave.addAll(toAddForId);
            }
            saveAliases(cms, structureId, new ArrayList<CmsAlias>(aliasesToSave));
        }
    }

}
