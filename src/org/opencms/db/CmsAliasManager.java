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
import org.opencms.file.CmsResourceFilter;
import org.opencms.gwt.shared.alias.CmsAliasImportResult;
import org.opencms.gwt.shared.alias.CmsAliasImportStatus;
import org.opencms.gwt.shared.alias.CmsAliasMode;
import org.opencms.i18n.CmsEncoder;
import org.opencms.lock.CmsLock;
import org.opencms.main.CmsException;
import org.opencms.main.CmsLog;
import org.opencms.main.OpenCms;
import org.opencms.security.CmsPermissionSet;
import org.opencms.util.CmsStringUtil;
import org.opencms.util.CmsUUID;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import org.apache.commons.logging.Log;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;

/**
 * The alias manager provides access to the aliases stored in the database.<p>
 */
public class CmsAliasManager {

    /** The logger instance for this class. */
    private static final Log LOG = CmsLog.getLog(CmsAliasManager.class);

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
     * Gets the rewrite alias matcher for the given site.<p>
     * 
     * @param siteRoot the site root 
     * 
     * @return the alias matcher for the site with the given site root 
     */
    public CmsRewriteAliasMatcher getRewriteAliasMatcher(String siteRoot) {

        List<CmsRewriteAlias> aliases = new ArrayList<CmsRewriteAlias>();
        CmsRewriteAlias alias1 = new CmsRewriteAlias(new CmsUUID(), "/sites/default", "(.+?)foo.jsp", "$1bar.jsp", true);
        aliases.add(alias1);
        return new CmsRewriteAliasMatcher(aliases);
    }

    /**
     * Checks whether the current user has permissions for mass editing the alias table.<p> 
     * 
     * @param cms the current CMS context  
     * @param siteRoot the site root to check 
     * @return true if the user from the CMS context is allowed to mass edit the alias table 
     * 
     * @throws CmsException if something goes wrong 
     */
    public boolean hasPermissionsForMassEdit(CmsObject cms, String siteRoot) throws CmsException {

        String originalSiteRoot = cms.getRequestContext().getSiteRoot();
        try {
            cms.getRequestContext().setSiteRoot(siteRoot);
            CmsResource siteRootRes = cms.readResource("/");
            return cms.hasPermissions(siteRootRes, CmsPermissionSet.ACCESS_CONTROL, false, CmsResourceFilter.DEFAULT);
        } finally {
            cms.getRequestContext().setSiteRoot(originalSiteRoot);
        }

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
    public synchronized CmsAliasImportResult importAlias(
        CmsObject cms,
        String siteRoot,
        String aliasPath,
        String vfsPath,
        CmsAliasMode mode) throws CmsException {

        CmsResource resource;
        Locale locale = OpenCms.getWorkplaceManager().getWorkplaceLocale(cms);
        String originalSiteRoot = cms.getRequestContext().getSiteRoot();
        try {
            cms.getRequestContext().setSiteRoot(siteRoot);
            resource = cms.readResource(vfsPath);
        } catch (CmsException e) {
            return new CmsAliasImportResult(CmsAliasImportStatus.aliasImportError, messageImportCantReadResource(
                locale,
                vfsPath), aliasPath, vfsPath, mode);
        } finally {
            cms.getRequestContext().setSiteRoot(originalSiteRoot);
        }
        if (!CmsAlias.ALIAS_PATTERN.matcher(aliasPath).matches()) {
            return new CmsAliasImportResult(CmsAliasImportStatus.aliasImportError, messageImportInvalidAliasPath(
                locale,
                aliasPath), aliasPath, vfsPath, mode);
        }
        List<CmsAlias> maybeAlias = getAliasesForPath(cms, siteRoot, aliasPath);
        if (maybeAlias.isEmpty()) {
            CmsAlias newAlias = new CmsAlias(resource.getStructureId(), siteRoot, aliasPath, mode);
            m_securityManager.addAlias(cms.getRequestContext(), newAlias);
            touch(cms, resource);
            return new CmsAliasImportResult(
                CmsAliasImportStatus.aliasNew,
                messageImportOk(locale),
                aliasPath,
                vfsPath,
                mode);
        } else {
            CmsAlias existingAlias = maybeAlias.get(0);
            CmsAliasFilter deleteFilter = new CmsAliasFilter(
                siteRoot,
                existingAlias.getAliasPath(),
                existingAlias.getStructureId());
            m_securityManager.deleteAliases(cms.getRequestContext(), deleteFilter);
            CmsAlias newAlias = new CmsAlias(resource.getStructureId(), siteRoot, aliasPath, mode);
            m_securityManager.addAlias(cms.getRequestContext(), newAlias);
            touch(cms, resource);
            return new CmsAliasImportResult(
                CmsAliasImportStatus.aliasChanged,
                messageImportUpdate(locale),
                aliasPath,
                vfsPath,
                mode);
        }
    }

    /**
     * Imports alias CSV data.<p>
     * 
     * @param cms the current CMS context 
     * @param aliasData the alias data 
     * @param siteRoot the root of the site into which the alias data should be imported 
     * @return the list of import results 
     * 
     * @throws Exception if something goes wrong 
     */
    public synchronized List<CmsAliasImportResult> importAliases(CmsObject cms, byte[] aliasData, String siteRoot)
    throws Exception {

        checkPermissionsForMassEdit(cms);
        BufferedReader reader = new BufferedReader(new InputStreamReader(
            new ByteArrayInputStream(aliasData),
            CmsEncoder.ENCODING_UTF_8));
        String line = reader.readLine();
        List<CmsAliasImportResult> totalResult = new ArrayList<CmsAliasImportResult>();
        CmsAliasImportResult result;
        while (line != null) {
            result = processAliasLine(cms, siteRoot, line);
            if (result != null) {
                totalResult.add(result);
            }
            line = reader.readLine();
        }
        return totalResult;
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
    public synchronized void saveAliases(CmsObject cms, CmsUUID structureId, List<CmsAlias> aliases)
    throws CmsException {

        m_securityManager.saveAliases(cms.getRequestContext(), cms.readResource(structureId), aliases);
        touch(cms, cms.readResource(structureId));
    }

    /**
     * Updates the aliases in the database.<p>
     * 
     * @param cms the current CMS context 
     * @param toDelete the collection of aliases to delete 
     * @param toAdd the collection of aliases to add
     * @throws CmsException if something goes wrong 
     */
    public synchronized void updateAliases(CmsObject cms, Collection<CmsAlias> toDelete, Collection<CmsAlias> toAdd)
    throws CmsException {

        checkPermissionsForMassEdit(cms);
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

        // Do all the deletions first, so we don't run into duplicate key errors for the alias paths 
        for (CmsUUID structureId : allKeys) {
            Set<CmsAlias> aliasesToSave = new HashSet<CmsAlias>(getAliasesForStructureId(cms, structureId));
            Collection<CmsAlias> toDeleteForId = toDeleteMap.get(structureId);
            if ((toDeleteForId != null) && !toDeleteForId.isEmpty()) {
                aliasesToSave.removeAll(toDeleteForId);
            }
            saveAliases(cms, structureId, new ArrayList<CmsAlias>(aliasesToSave));
        }
        for (CmsUUID structureId : allKeys) {
            Set<CmsAlias> aliasesToSave = new HashSet<CmsAlias>(getAliasesForStructureId(cms, structureId));
            Collection<CmsAlias> toAddForId = toAddMap.get(structureId);
            if ((toAddForId != null) && !toAddForId.isEmpty()) {
                aliasesToSave.addAll(toAddForId);
            }
            saveAliases(cms, structureId, new ArrayList<CmsAlias>(aliasesToSave));
        }
    }

    /**
     * Checks whether the current user has the permissions to mass edit the alias table, and throws an 
     * exception otherwise.<p>
     * 
     * @param cms the current CMS context 
     * 
     * @throws CmsException
     */
    protected void checkPermissionsForMassEdit(CmsObject cms) throws CmsException {

        // read site root folder and check if we have control permissions on it  
        CmsResource siteRootRes = cms.readResource("/");
        m_securityManager.checkPermissions(
            cms.getRequestContext(),
            siteRootRes,
            CmsPermissionSet.ACCESS_CONTROL,
            false,
            CmsResourceFilter.DEFAULT);
    }

    /**
     * Processes a single alias import operation which has already been parsed into fields.<p>
     * 
     * @param cms the current CMS context 
     * @param siteRoot the site root 
     * @param aliasPath the alias path 
     * @param vfsPath the VFS resource path 
     * @param mode the alias mode 
     * 
     * @return the result of the import operation 
     */
    protected CmsAliasImportResult processAliasImport(
        CmsObject cms,
        String siteRoot,
        String aliasPath,
        String vfsPath,
        CmsAliasMode mode) {

        try {
            return importAlias(cms, siteRoot, aliasPath, vfsPath, mode);
        } catch (CmsException e) {
            return new CmsAliasImportResult(
                CmsAliasImportStatus.aliasImportError,
                e.getLocalizedMessage(),
                aliasPath,
                vfsPath,
                mode);
        }
    }

    /**
     * Processes a line from a CSV file containing the alias data to be imported.<p>
     * 
     * @param cms the current CMS context 
     * @param siteRoot the site root 
     * @param line the line with the data to import 
     * 
     * @return the import result 
     */
    protected CmsAliasImportResult processAliasLine(CmsObject cms, String siteRoot, String line) {

        Locale locale = OpenCms.getWorkplaceManager().getWorkplaceLocale(cms);
        line = line.trim();
        // ignore empty lines or comments starting with #
        if (CmsStringUtil.isEmptyOrWhitespaceOnly(line) || line.startsWith("#")) {
            return null;
        }
        boolean comma = line.contains(",");
        boolean semicolon = line.contains(";");
        String separatorRegex;
        // Depending on the country, Excel may use either commas or semicolons as separators  
        if (comma && !semicolon) {
            separatorRegex = ",";
        } else if (semicolon && !comma) {
            separatorRegex = ";";
        } else {
            return new CmsAliasImportResult(
                line,
                CmsAliasImportStatus.aliasParseError,
                messageImportBadSeparator(locale));
        }
        String[] tokens = line.split(separatorRegex);
        if ((tokens.length == 2) || (tokens.length == 3)) {
            String alias = tokens[0].trim();
            String vfsPath = tokens[1].trim();
            CmsAliasMode mode = CmsAliasMode.permanentRedirect;
            if (tokens.length == 3) {
                try {
                    mode = CmsAliasMode.valueOf(tokens[2].trim());
                } catch (Exception e) {
                    return new CmsAliasImportResult(
                        line,
                        CmsAliasImportStatus.aliasParseError,
                        messageImportInvalidFormat(locale));
                }
            }
            CmsAliasImportResult returnValue = processAliasImport(cms, siteRoot, alias, vfsPath, mode);
            returnValue.setLine(line);
            return returnValue;
        } else {
            return new CmsAliasImportResult(
                line,
                CmsAliasImportStatus.aliasParseError,
                messageImportInvalidFormat(locale));
        }
    }

    /**
     * Message accessor.<p>
     * 
     * @param locale the message locale 
     * 
     * @return the message string 
     */
    private String messageImportBadSeparator(Locale locale) {

        return Messages.get().getBundle(locale).key(Messages.ERR_ALIAS_BAD_SEPARATOR_0);
    }

    /**
     * Message accessor.<p>
     * 
     * @param locale the message locale 
     * @param path a path 
     * 
     * @return the message string 
     */
    private String messageImportCantReadResource(Locale locale, String path) {

        return Messages.get().getBundle(locale).key(Messages.ERR_ALIAS_IMPORT_COULD_NOT_READ_RESOURCE_0);

    }

    /**
     * Message accessor.<p>
     * 
     * @param locale the message locale 
     * @param path a path 
     * 
     * @return the message string 
     */
    private String messageImportInvalidAliasPath(Locale locale, String path) {

        return Messages.get().getBundle(locale).key(Messages.ERR_ALIAS_IMPORT_INVALID_ALIAS_PATH_0);

    }

    /**
     * Message accessor.<p>
     * 
     * @param locale the message locale 
     * 
     * @return the message string 
     */
    private String messageImportInvalidFormat(Locale locale) {

        return Messages.get().getBundle(locale).key(Messages.ERR_ALIAS_IMPORT_BAD_FORMAT_0);
    }

    /**
     * Message accessor.<p>
     * 
     * @param locale the message locale 
     * 
     * @return the message string 
     */
    private String messageImportOk(Locale locale) {

        return Messages.get().getBundle(locale).key(Messages.ERR_ALIAS_IMPORT_OK_0);
    }

    /**
     * Message accessor.<p>
     * 
     * @param locale the message locale 
     * 
     * @return the message string 
     */
    private String messageImportUpdate(Locale locale) {

        return Messages.get().getBundle(locale).key(Messages.ERR_ALIAS_IMPORT_UPDATED_0);
    }

    /**
     * Tries to to touch a resource by setting its last modification date, but only if its state is 'unchanged'.<p>
     * 
     * @param cms the current CMS context 
     * @param resource the resource which should be 'touched'. 
     */
    private void touch(CmsObject cms, CmsResource resource) {

        if (resource.getState().isUnchanged()) {
            try {
                CmsLock lock = cms.getLock(resource);
                if (lock.isUnlocked() || !lock.isOwnedBy(cms.getRequestContext().getCurrentUser())) {
                    cms.lockResourceTemporary(resource);
                    long now = System.currentTimeMillis();
                    resource.setDateLastModified(now);
                    cms.writeResource(resource);
                    if (lock.isUnlocked()) {
                        cms.unlockResource(resource);
                    }
                }
            } catch (CmsException e) {
                LOG.warn("Could not touch resource after alias modification: " + resource.getRootPath(), e);
            }
        }
    }

}
