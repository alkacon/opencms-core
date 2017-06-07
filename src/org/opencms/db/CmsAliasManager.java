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
import org.opencms.gwt.shared.alias.CmsAliasImportResult;
import org.opencms.gwt.shared.alias.CmsAliasImportStatus;
import org.opencms.gwt.shared.alias.CmsAliasMode;
import org.opencms.i18n.CmsEncoder;
import org.opencms.lock.CmsLock;
import org.opencms.main.CmsException;
import org.opencms.main.CmsLog;
import org.opencms.main.OpenCms;
import org.opencms.security.CmsRole;
import org.opencms.util.CmsStringUtil;
import org.opencms.util.CmsUUID;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
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

import au.com.bytecode.opencsv.CSVParser;

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
     * Reads the rewrite aliases for a given site root.<p>
     *
     * @param cms the current CMS context
     * @param siteRoot the site root for which the rewrite aliases should be retrieved
     * @return the list of rewrite aliases for the given site root
     *
     * @throws CmsException if something goes wrong
     */
    public List<CmsRewriteAlias> getRewriteAliases(CmsObject cms, String siteRoot) throws CmsException {

        CmsRewriteAliasFilter filter = new CmsRewriteAliasFilter().setSiteRoot(siteRoot);
        List<CmsRewriteAlias> result = m_securityManager.getRewriteAliases(cms.getRequestContext(), filter);
        return result;
    }

    /**
     * Gets the rewrite alias matcher for the given site.<p>
     *
     * @param cms the CMS context to use
     * @param siteRoot the site root
     *
     * @return the alias matcher for the site with the given site root
     *
     * @throws CmsException if something goes wrong
     */
    public CmsRewriteAliasMatcher getRewriteAliasMatcher(CmsObject cms, String siteRoot) throws CmsException {

        List<CmsRewriteAlias> aliases = getRewriteAliases(cms, siteRoot);
        return new CmsRewriteAliasMatcher(aliases);
    }

    /**
     * Checks whether the current user has permissions for mass editing the alias table.<p>
     *
     * @param cms the current CMS context
     * @param siteRoot the site root to check
     * @return true if the user from the CMS context is allowed to mass edit the alias table
     */
    public boolean hasPermissionsForMassEdit(CmsObject cms, String siteRoot) {

        String originalSiteRoot = cms.getRequestContext().getSiteRoot();
        try {
            cms.getRequestContext().setSiteRoot(siteRoot);
            return OpenCms.getRoleManager().hasRoleForResource(cms, CmsRole.ADMINISTRATOR, "/");
        } finally {
            cms.getRequestContext().setSiteRoot(originalSiteRoot);
        }

    }

    /**
     * Imports alias CSV data.<p>
     *
     * @param cms the current CMS context
     * @param aliasData the alias data
     * @param siteRoot the root of the site into which the alias data should be imported
     * @param separator the field separator which is used by the imported data
     * @return the list of import results
     *
     * @throws Exception if something goes wrong
     */
    public synchronized List<CmsAliasImportResult> importAliases(
        CmsObject cms,
        byte[] aliasData,
        String siteRoot,
        String separator)
    throws Exception {

        checkPermissionsForMassEdit(cms);
        BufferedReader reader = new BufferedReader(
            new InputStreamReader(new ByteArrayInputStream(aliasData), CmsEncoder.ENCODING_UTF_8));
        String line = reader.readLine();
        List<CmsAliasImportResult> totalResult = new ArrayList<CmsAliasImportResult>();
        CmsAliasImportResult result;
        while (line != null) {
            result = processAliasLine(cms, siteRoot, line, separator);
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
     * Saves the rewrite alias for a given site root.<p>
     *
     * @param cms the current CMS context
     * @param siteRoot the site root for which the rewrite aliases should be saved
     * @param newAliases the list of aliases to save
     *
     * @throws CmsException if something goes wrong
     */
    public void saveRewriteAliases(CmsObject cms, String siteRoot, List<CmsRewriteAlias> newAliases)
    throws CmsException {

        checkPermissionsForMassEdit(cms, siteRoot);
        m_securityManager.saveRewriteAliases(cms.getRequestContext(), siteRoot, newAliases);
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

        OpenCms.getRoleManager().checkRoleForResource(cms, CmsRole.ADMINISTRATOR, "/");
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
    protected synchronized CmsAliasImportResult importAlias(
        CmsObject cms,
        String siteRoot,
        String aliasPath,
        String vfsPath,
        CmsAliasMode mode)
    throws CmsException {

        CmsResource resource;
        Locale locale = OpenCms.getWorkplaceManager().getWorkplaceLocale(cms);
        String originalSiteRoot = cms.getRequestContext().getSiteRoot();
        try {
            cms.getRequestContext().setSiteRoot(siteRoot);
            resource = cms.readResource(vfsPath);
        } catch (CmsException e) {
            return new CmsAliasImportResult(
                CmsAliasImportStatus.aliasImportError,
                messageImportCantReadResource(locale, vfsPath),
                aliasPath,
                vfsPath,
                mode);
        } finally {
            cms.getRequestContext().setSiteRoot(originalSiteRoot);
        }
        if (!CmsAlias.ALIAS_PATTERN.matcher(aliasPath).matches()) {
            return new CmsAliasImportResult(
                CmsAliasImportStatus.aliasImportError,
                messageImportInvalidAliasPath(locale, aliasPath),
                aliasPath,
                vfsPath,
                mode);
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
     * @param separator the field separator
     *
     * @return the import result
     */
    protected CmsAliasImportResult processAliasLine(CmsObject cms, String siteRoot, String line, String separator) {

        Locale locale = OpenCms.getWorkplaceManager().getWorkplaceLocale(cms);
        line = line.trim();
        // ignore empty lines or comments starting with #
        if (CmsStringUtil.isEmptyOrWhitespaceOnly(line) || line.startsWith("#")) {
            return null;
        }
        CSVParser parser = new CSVParser(separator.charAt(0));
        String[] tokens = null;
        try {
            tokens = parser.parseLine(line);
            for (int i = 0; i < tokens.length; i++) {
                tokens[i] = tokens[i].trim();
            }
        } catch (IOException e) {
            return new CmsAliasImportResult(
                line,
                CmsAliasImportStatus.aliasParseError,
                messageImportInvalidFormat(locale));
        }
        int numTokens = tokens.length;
        String alias = null;
        String vfsPath = null;
        if (numTokens >= 2) {
            alias = tokens[0];
            vfsPath = tokens[1];
        }
        CmsAliasMode mode = CmsAliasMode.permanentRedirect;
        if (numTokens >= 3) {
            try {
                mode = CmsAliasMode.valueOf(tokens[2].trim());
            } catch (Exception e) {
                return new CmsAliasImportResult(
                    line,
                    CmsAliasImportStatus.aliasParseError,
                    messageImportInvalidFormat(locale));
            }
        }
        boolean isRewrite = false;
        if (numTokens == 4) {
            if (!tokens[3].equals("rewrite")) {
                return new CmsAliasImportResult(
                    line,
                    CmsAliasImportStatus.aliasParseError,
                    messageImportInvalidFormat(locale));
            } else {
                isRewrite = true;
            }
        }
        if ((numTokens < 2) || (numTokens > 4)) {
            return new CmsAliasImportResult(
                line,
                CmsAliasImportStatus.aliasParseError,
                messageImportInvalidFormat(locale));
        }
        CmsAliasImportResult returnValue = null;
        if (isRewrite) {
            returnValue = processRewriteImport(cms, siteRoot, alias, vfsPath, mode);
        } else {
            returnValue = processAliasImport(cms, siteRoot, alias, vfsPath, mode);
        }
        returnValue.setLine(line);
        return returnValue;
    }

    /**
     * Checks that the user has permissions for a mass edit operation in a given site.<p>
     *
     * @param cms the current CMS context
     * @param siteRoot the site for which the permissions should be checked
     *
     * @throws CmsException if something goes wrong
     */
    private void checkPermissionsForMassEdit(CmsObject cms, String siteRoot) throws CmsException {

        String originalSiteRoot = cms.getRequestContext().getSiteRoot();
        try {
            cms.getRequestContext().setSiteRoot(siteRoot);
            checkPermissionsForMassEdit(cms);
        } finally {
            cms.getRequestContext().setSiteRoot(originalSiteRoot);
        }
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
     * Handles the import of a rewrite alias.<p>
     *
     * @param cms the current CMS context
     * @param siteRoot the site root
     * @param source the rewrite pattern
     * @param target the rewrite replacement
     * @param mode the alias mode
     *
     * @return the import result
     */
    private CmsAliasImportResult processRewriteImport(
        CmsObject cms,
        String siteRoot,
        String source,
        String target,
        CmsAliasMode mode) {

        try {
            return m_securityManager.importRewriteAlias(cms.getRequestContext(), siteRoot, source, target, mode);
        } catch (CmsException e) {
            return new CmsAliasImportResult(
                CmsAliasImportStatus.aliasImportError,
                e.getLocalizedMessage(),
                source,
                target,
                mode);
        }

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
