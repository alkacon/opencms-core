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

package org.opencms.gwt;

import org.opencms.db.CmsAlias;
import org.opencms.db.CmsAliasManager;
import org.opencms.db.CmsRewriteAlias;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsResource;
import org.opencms.file.CmsResourceFilter;
import org.opencms.file.CmsVfsResourceNotFoundException;
import org.opencms.gwt.shared.alias.CmsAliasBean;
import org.opencms.main.CmsException;
import org.opencms.main.CmsLog;
import org.opencms.main.OpenCms;
import org.opencms.util.CmsUUID;

import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;

import au.com.bytecode.opencsv.CSVWriter;

/**
 * This class contains the real implementations of service methods related to aliases.<p>
 */
public class CmsAliasHelper {

    /** The logger for this class. */
    private static final Log LOG = CmsLog.getLog(CmsAliasHelper.class);

    /** The internal CMS object. */
    private CmsObject m_cms;

    /**
     * Returns the comma separated alias data.<p>
     *
     * @param cms the OpenCms context
     *
     * @return the comma separated alias data
     *
     * @throws CmsException if something goes wrong reading the alias data
     */
    public String exportAliasesAsCsv(CmsObject cms) throws CmsException {

        String siteRoot = cms.getRequestContext().getSiteRoot();
        List<CmsAlias> aliases = OpenCms.getAliasManager().getAliasesForSite(cms, siteRoot);
        StringWriter writer = new StringWriter();
        CSVWriter csvWriter = new CSVWriter(writer);
        StringBuffer resultBuffer = writer.getBuffer();
        for (CmsAlias alias : aliases) {
            try {
                CmsResource resource = cms.readResource(alias.getStructureId());
                csvWriter.writeNext(
                    new String[] {alias.getAliasPath(), cms.getSitePath(resource), alias.getMode().toString()});
            } catch (CmsException e) {
                LOG.warn("Could not read alias resource", e);
                continue;
            }
        }

        List<CmsRewriteAlias> rewriteAliases = OpenCms.getAliasManager().getRewriteAliases(cms, siteRoot);
        for (CmsRewriteAlias rewrite : rewriteAliases) {
            csvWriter.writeNext(
                new String[] {
                    rewrite.getPatternString(),
                    rewrite.getReplacementString(),
                    rewrite.getMode().toString(),
                    "rewrite"});
        }
        try {
            csvWriter.flush();
            csvWriter.close();
        } catch (IOException e) {
            // can't happen
        }
        return resultBuffer.toString();
    }

    /**
     * Saves aliases.<p>
     *
     * @param structureId the structure id
     * @param aliasBeans the alias beans
     * @throws CmsException if something goes wrong
     */
    public void saveAliases(CmsUUID structureId, List<CmsAliasBean> aliasBeans) throws CmsException {

        CmsAliasManager aliasManager = OpenCms.getAliasManager();
        CmsObject cms = m_cms;
        List<CmsAlias> aliases = new ArrayList<CmsAlias>();
        for (CmsAliasBean aliasBean : aliasBeans) {
            CmsAlias alias = new CmsAlias(
                structureId,
                cms.getRequestContext().getSiteRoot(),
                aliasBean.getSitePath(),
                aliasBean.getMode());
            aliases.add(alias);
        }
        aliasManager.saveAliases(cms, structureId, aliases);
    }

    /**
     * Sets the CMS object.<p>
     *
     * @param cms the CMS object
     */
    public void setCms(CmsObject cms) {

        m_cms = cms;
    }

    /**
     * Checks whether a given string is a valid alias path.<p>
     *
     * @param path the path to check
     * @param locale the locale to use for validation messages
     *
     * @return null if the string is a valid alias path, else an error message
     */
    protected String checkValidAliasPath(String path, Locale locale) {

        if (org.opencms.db.CmsAlias.ALIAS_PATTERN.matcher(path).matches()) {
            return null;
        } else {
            return Messages.get().getBundle(locale).key(Messages.ERR_ALIAS_INVALID_PATH_0);
        }
    }

    /**
     * Converts a server-side alias object to an alias bean.<p>
     *
     * @param alias the server-side alias object
     *
     * @return the client-side alias bean
     */
    protected CmsAliasBean convertAliasToBean(CmsAlias alias) {

        return new CmsAliasBean(alias.getAliasPath(), alias.getMode());
    }

    /**
     * Implementation of the getAliasesForPage method.<p>
     *
     * @param uuid the structure id of the page
     * @return the aliases for the given page
     *
     * @throws CmsException if something goes wrong
     */
    protected List<CmsAliasBean> getAliasesForPage(CmsUUID uuid) throws CmsException {

        CmsAliasManager aliasManager = OpenCms.getAliasManager();
        List<CmsAlias> aliases = aliasManager.getAliasesForStructureId(m_cms, uuid);
        List<CmsAliasBean> result = new ArrayList<CmsAliasBean>();
        for (CmsAlias alias : aliases) {
            CmsAliasBean bean = convertAliasToBean(alias);
            result.add(bean);
        }
        return result;
    }

    /**
     * The internal method used for validating aliases.<p>
     *
     * @param uuid the structure id of the resource whose aliases are being validated
     * @param aliasPaths a map from (arbitrary) ids to alias paths
     *
     * @return a map from the same ids to validation error messages
     *
     * @throws CmsException if something goes wrong
     */
    protected Map<String, String> validateAliases(CmsUUID uuid, Map<String, String> aliasPaths) throws CmsException {

        Locale locale = OpenCms.getWorkplaceManager().getWorkplaceLocale(m_cms);
        Set<String> seenPaths = new HashSet<String>();
        Set<String> duplicatePaths = new HashSet<String>();
        for (String path : aliasPaths.values()) {
            if (seenPaths.contains(path)) {
                duplicatePaths.add(path);
            }
            seenPaths.add(path);
        }
        Map<String, String> errorMessagesByPath = new HashMap<String, String>();
        for (String path : duplicatePaths) {
            errorMessagesByPath.put(path, Messages.get().getBundle(locale).key(Messages.ERR_ALIAS_DUPLICATE_PATH_0));
        }
        seenPaths.removeAll(duplicatePaths);

        for (String path : seenPaths) {
            String pathError = checkValidAliasPath(path, locale);
            if (pathError != null) {
                errorMessagesByPath.put(path, pathError);
            } else {
                errorMessagesByPath.put(path, null);
                if (m_cms.existsResource(path, CmsResourceFilter.ALL)) {
                    errorMessagesByPath.put(path, Messages.get().getBundle(locale).key(Messages.ERR_ALIAS_IS_VFS_0));
                } else {
                    List<CmsAlias> aliases = OpenCms.getAliasManager().getAliasesForPath(
                        m_cms,
                        m_cms.getRequestContext().getSiteRoot(),
                        path);
                    for (CmsAlias alias : aliases) {
                        CmsUUID otherStructureId = alias.getStructureId();
                        if (!otherStructureId.equals(uuid)) {
                            try {
                                CmsResource resource = m_cms.readResource(otherStructureId, CmsResourceFilter.ALL);
                                errorMessagesByPath.put(
                                    path,
                                    Messages.get().getBundle(locale).key(
                                        Messages.ERR_ALIAS_ALREADY_USED_1,
                                        resource.getRootPath()));
                                break;
                            } catch (CmsVfsResourceNotFoundException e) {
                                // this may happen if there are outdated entries in the database table
                                errorMessagesByPath.put(
                                    path,
                                    Messages.get().getBundle(locale).key(Messages.ERR_ALIAS_ALREADY_USED_UNKNOWN_0));
                                break;
                            }
                        }
                    }
                }

            }
        }
        Map<String, String> errorMessagesById = new HashMap<String, String>();
        for (String key : aliasPaths.keySet()) {
            String path = aliasPaths.get(key);
            if (errorMessagesByPath.containsKey(path)) {
                String errorMessage = errorMessagesByPath.get(path);
                errorMessagesById.put(key, errorMessage);
            }
        }
        return errorMessagesById;
    }
}
