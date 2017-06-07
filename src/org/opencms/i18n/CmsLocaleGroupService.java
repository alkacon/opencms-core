/*
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (c) Alkacon Software GmbH & Co. KG (http://www.alkacon.com)
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

package org.opencms.i18n;

import org.opencms.ade.configuration.CmsADEManager;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsProperty;
import org.opencms.file.CmsPropertyDefinition;
import org.opencms.file.CmsResource;
import org.opencms.file.CmsResourceFilter;
import org.opencms.lock.CmsLock;
import org.opencms.lock.CmsLockActionRecord;
import org.opencms.lock.CmsLockActionRecord.LockChange;
import org.opencms.lock.CmsLockUtil;
import org.opencms.main.CmsException;
import org.opencms.main.CmsLog;
import org.opencms.main.OpenCms;
import org.opencms.relations.CmsRelation;
import org.opencms.relations.CmsRelationFilter;
import org.opencms.relations.CmsRelationType;
import org.opencms.security.CmsPermissionSet;
import org.opencms.security.CmsSecurityException;
import org.opencms.site.CmsSite;
import org.opencms.util.CmsFileUtil;
import org.opencms.util.CmsStringUtil;
import org.opencms.util.CmsUUID;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import org.apache.commons.logging.Log;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

/**
 * Helper class for manipulating locale groups.<p>
 *
 * A locale group is a construct used to group pages which are translations of each other.
 *  *
 * A locale group consists of a set of resources connected by relations in the following way:<p>
 * <ul>
 * <li> There is a primary resource and a set of secondary resources.
 * <li> Each secondary resource has a relation to the primary resource of type LOCALE_VARIANT.
 * <li> Ideally, each resource has a different locale.
 * </ul>
 *
 * The point of the primary resource is to act as a 'master' resource which translators then use to translate to different locales.
 */
public class CmsLocaleGroupService {

    /**
     * Enum representing whether two resources can be linked together in a locale group.<p>
     */
    public enum Status {
        /** Resource already linked. */
        alreadyLinked,

        /** Resource linkable to locale group.*/
        linkable,

        /** Resource to link has a locale which is marked as 'do not translate' on the locale group. */
        notranslation,

        /** Other reason that resource can't be linked to locale group. */
        other
    }

    /** The logger instance for this class. */
    private static final Log LOG = CmsLog.getLog(CmsLocaleGroupService.class);

    /** CMS context to use for VFS operations. */
    private CmsObject m_cms;

    /**
     * Creates a new instance.<p>
     *
     * @param cms the CMS context to use
     */
    public CmsLocaleGroupService(CmsObject cms) {
        m_cms = cms;
    }

    /**
     * Helper method for getting the possible locales for a resource.<p>
     *
     * @param cms the CMS context
     * @param currentResource the resource
     *
     * @return the possible locales for a resource
     */
    public static List<Locale> getPossibleLocales(CmsObject cms, CmsResource currentResource) {

        CmsSite site = OpenCms.getSiteManager().getSiteForRootPath(currentResource.getRootPath());
        List<Locale> secondaryLocales = Lists.newArrayList();
        Locale mainLocale = null;
        if (site != null) {
            List<Locale> siteLocales = site.getSecondaryTranslationLocales();
            mainLocale = site.getMainTranslationLocale(null);
            if ((siteLocales == null) || siteLocales.isEmpty()) {
                siteLocales = OpenCms.getLocaleManager().getAvailableLocales();
                if (mainLocale == null) {
                    mainLocale = siteLocales.get(0);
                }
            }
            secondaryLocales.addAll(siteLocales);
        }

        try {
            CmsProperty secondaryLocaleProp = cms.readPropertyObject(
                currentResource,
                CmsPropertyDefinition.PROPERTY_SECONDARY_LOCALES,
                true);
            String propValue = secondaryLocaleProp.getValue();
            if (!CmsStringUtil.isEmptyOrWhitespaceOnly(propValue)) {
                List<Locale> restrictionLocales = Lists.newArrayList();
                String[] tokens = propValue.trim().split(" *, *"); //$NON-NLS-1$
                for (String token : tokens) {
                    OpenCms.getLocaleManager();
                    Locale localeForToken = CmsLocaleManager.getLocale(token);
                    restrictionLocales.add(localeForToken);
                }
                if (!restrictionLocales.isEmpty()) {
                    secondaryLocales.retainAll(restrictionLocales);
                }
            }
        } catch (CmsException e) {
            LOG.error(e.getLocalizedMessage(), e);
        }
        List<Locale> result = new ArrayList<Locale>();
        result.add(mainLocale);
        for (Locale secondaryLocale : secondaryLocales) {
            if (!result.contains(secondaryLocale)) {
                result.add(secondaryLocale);
            }
        }
        return result;
    }

    /**
     * Adds a resource to a locale group.<p>
     *
     * Note: This is a low level method that is hard to use correctly. Please use attachLocaleGroupIndirect if at all possible.
     *
     * @param secondaryPage the page to add
     * @param primaryPage the primary resource of the locale group which the resource should be added to
     * @throws CmsException if something goes wrong
     */
    public void attachLocaleGroup(CmsResource secondaryPage, CmsResource primaryPage) throws CmsException {

        if (secondaryPage.getStructureId().equals(primaryPage.getStructureId())) {
            throw new IllegalArgumentException(
                "A page can not be linked with itself as a locale variant: " + secondaryPage.getRootPath());
        }
        CmsLocaleGroup group = readLocaleGroup(secondaryPage);
        if (group.isRealGroup()) {
            throw new IllegalArgumentException(
                "The page " + secondaryPage.getRootPath() + " is already part of a group. ");
        }

        // TODO: Check for redundant locales

        CmsLocaleGroup targetGroup = readLocaleGroup(primaryPage);
        CmsLockActionRecord record = CmsLockUtil.ensureLock(m_cms, secondaryPage);
        try {
            m_cms.deleteRelationsFromResource(
                secondaryPage,
                CmsRelationFilter.ALL.filterType(CmsRelationType.LOCALE_VARIANT));
            m_cms.addRelationToResource(
                secondaryPage,
                targetGroup.getPrimaryResource(),
                CmsRelationType.LOCALE_VARIANT.getName());
        } finally {
            if (record.getChange() == LockChange.locked) {
                m_cms.unlockResource(secondaryPage);
            }
        }
    }

    /**
     * Smarter method to connect a resource to a locale group.<p>
     *
     * Exactly one of the resources given as an argument must represent a locale group, while the other should
     * be the locale that you wish to attach to the locale group.<p>
     *
     * @param first a resource
     * @param second a resource
     * @throws CmsException if something goes wrong
     */
    public void attachLocaleGroupIndirect(CmsResource first, CmsResource second) throws CmsException {

        CmsResource firstResourceCorrected = getDefaultFileOrSelf(first);
        CmsResource secondResourceCorrected = getDefaultFileOrSelf(second);
        if ((firstResourceCorrected == null) || (secondResourceCorrected == null)) {
            throw new IllegalArgumentException("no default file");
        }

        CmsLocaleGroup group1 = readLocaleGroup(firstResourceCorrected);
        CmsLocaleGroup group2 = readLocaleGroup(secondResourceCorrected);
        int numberOfRealGroups = (group1.isRealGroupOrPotentialGroupHead() ? 1 : 0)
            + (group2.isRealGroupOrPotentialGroupHead() ? 1 : 0);
        if (numberOfRealGroups != 1) {
            throw new IllegalArgumentException("more than one real groups");
        }
        CmsResource main = null;
        CmsResource secondary = null;
        if (group1.isRealGroupOrPotentialGroupHead()) {
            main = group1.getPrimaryResource();
            secondary = group2.getPrimaryResource();
        } else if (group2.isRealGroupOrPotentialGroupHead()) {
            main = group2.getPrimaryResource();
            secondary = group1.getPrimaryResource();
        }
        attachLocaleGroup(secondary, main);
    }

    /**
     * Checks if the two resources are linkable as locale variants and returns an appropriate status<p>
     *
     *  This is the case if exactly one of the resources represents a locale group, the locale of the other resource
     *  is not already present in the locale group, and if some other permission / validity checks are passed.
     *
     * @param firstResource a resource
     * @param secondResource a resource
     *
     * @return the result of the linkability check
     */
    public Status checkLinkable(CmsResource firstResource, CmsResource secondResource) {

        String debugPrefix = "checkLinkable [" + Thread.currentThread().getName() + "]: ";
        LOG.debug(
            debugPrefix
                + (firstResource != null ? firstResource.getRootPath() : null)
                + " -- "
                + (secondResource != null ? secondResource.getRootPath() : null));
        try {
            CmsResource firstResourceCorrected = getDefaultFileOrSelf(firstResource);
            CmsResource secondResourceCorrected = getDefaultFileOrSelf(secondResource);
            if ((firstResourceCorrected == null) || (secondResourceCorrected == null)) {
                LOG.debug(debugPrefix + " rejected - no resource");
                return Status.other;
            }
            Locale locale1 = OpenCms.getLocaleManager().getDefaultLocale(m_cms, firstResourceCorrected);
            Locale locale2 = OpenCms.getLocaleManager().getDefaultLocale(m_cms, secondResourceCorrected);
            if (locale1.equals(locale2)) {
                LOG.debug(debugPrefix + "  rejected - same locale " + locale1);
                return Status.other;
            }

            Locale mainLocale1 = getMainLocale(firstResourceCorrected.getRootPath());
            Locale mainLocale2 = getMainLocale(secondResourceCorrected.getRootPath());
            if ((mainLocale1 == null) || !(mainLocale1.equals(mainLocale2))) {
                LOG.debug(debugPrefix + " rejected - incompatible main locale " + mainLocale1 + "/" + mainLocale2);
                return Status.other;
            }

            CmsLocaleGroup group1 = readLocaleGroup(firstResourceCorrected);
            Set<Locale> locales1 = group1.getLocales();
            CmsLocaleGroup group2 = readLocaleGroup(secondResourceCorrected);
            Set<Locale> locales2 = group2.getLocales();
            if (!(Sets.intersection(locales1, locales2).isEmpty())) {
                LOG.debug(debugPrefix + "  rejected - already linked (case 1)");
                return Status.alreadyLinked;
            }

            if (group1.isMarkedNoTranslation(group2.getLocales())
                || group2.isMarkedNoTranslation(group1.getLocales())) {
                LOG.debug(debugPrefix + "  rejected - marked 'no translation'");
                return Status.notranslation;
            }

            if (group1.isRealGroupOrPotentialGroupHead() == group2.isRealGroupOrPotentialGroupHead()) {
                LOG.debug(debugPrefix + "  rejected - incompatible locale group states");
                return Status.other;
            }

            CmsResource permCheckResource = null;
            if (group1.isRealGroupOrPotentialGroupHead()) {
                permCheckResource = group2.getPrimaryResource();
            } else {
                permCheckResource = group1.getPrimaryResource();
            }
            if (!m_cms.hasPermissions(
                permCheckResource,
                CmsPermissionSet.ACCESS_WRITE,
                false,
                CmsResourceFilter.IGNORE_EXPIRATION)) {
                LOG.debug(debugPrefix + " no write permissions: " + permCheckResource.getRootPath());
                return Status.other;
            }

            if (!checkLock(permCheckResource)) {
                LOG.debug(debugPrefix + " lock state: " + permCheckResource.getRootPath());
                return Status.other;
            }

            if (group2.getPrimaryResource().getStructureId().equals(group1.getPrimaryResource().getStructureId())) {
                LOG.debug(debugPrefix + "  rejected - already linked (case 2)");
                return Status.alreadyLinked;
            }
        } catch (Exception e) {
            LOG.error(debugPrefix + e.getLocalizedMessage(), e);
            LOG.debug(debugPrefix + "  rejected - exception (see previous)");
            return Status.other;
        }
        LOG.debug(debugPrefix + " OK");
        return Status.linkable;
    }

    /**
     * Removes a locale group relation between two resources.<p>
     *
     * @param firstPage the first resource
     * @param secondPage the second resource
     * @throws CmsException if something goes wrong
     */
    public void detachLocaleGroup(CmsResource firstPage, CmsResource secondPage) throws CmsException {

        CmsRelationFilter typeFilter = CmsRelationFilter.ALL.filterType(CmsRelationType.LOCALE_VARIANT);
        firstPage = getDefaultFileOrSelf(firstPage);
        secondPage = getDefaultFileOrSelf(secondPage);
        if ((firstPage == null) || (secondPage == null)) {
            return;
        }

        List<CmsRelation> relations = m_cms.readRelations(typeFilter.filterStructureId(secondPage.getStructureId()));
        CmsUUID firstId = firstPage.getStructureId();
        CmsUUID secondId = secondPage.getStructureId();
        for (CmsRelation relation : relations) {
            CmsUUID sourceId = relation.getSourceId();
            CmsUUID targetId = relation.getTargetId();
            CmsResource resourceToModify = null;
            if (sourceId.equals(firstId) && targetId.equals(secondId)) {
                resourceToModify = firstPage;
            } else if (sourceId.equals(secondId) && targetId.equals(firstId)) {
                resourceToModify = secondPage;
            }
            if (resourceToModify != null) {
                CmsLockActionRecord record = CmsLockUtil.ensureLock(m_cms, resourceToModify);
                try {
                    m_cms.deleteRelationsFromResource(resourceToModify, typeFilter);
                } finally {
                    if (record.getChange() == LockChange.locked) {
                        m_cms.unlockResource(resourceToModify);
                    }
                }
                break;
            }
        }
    }

    /**
     * Tries to find the 'best' localized subsitemap parent folder for a resource.<p>
     *
     * This is used when we use locale group dialogs outside the sitemap editor, so we
     * don't have a clearly defined 'root resource' - this method is used to find a replacement
     * for the root resource which we would have in the sitemap editor.
     *
     * @param resource the resource for which to find the localization root
     * @return the localization root
     *
     * @throws CmsException if something goes wrong
     */
    public CmsResource findLocalizationRoot(CmsResource resource) throws CmsException {

        String rootPath = resource.getRootPath();
        LOG.debug("Trying to find localization root for " + rootPath);
        if (resource.isFile()) {
            rootPath = CmsResource.getParentFolder(rootPath);
        }
        CmsObject cms = OpenCms.initCmsObject(m_cms);
        cms.getRequestContext().setSiteRoot("");
        String currentPath = rootPath;
        CmsSite site = OpenCms.getSiteManager().getSiteForRootPath(rootPath);
        if (site == null) {
            return null;
        }
        String siteroot = site.getSiteRoot();
        List<String> ancestors = Lists.newArrayList();
        while ((currentPath != null) && CmsStringUtil.isPrefixPath(siteroot, currentPath)) {
            ancestors.add(currentPath);
            currentPath = CmsResource.getParentFolder(currentPath);
        }
        Iterator<String> iter = ancestors.iterator();
        while (iter.hasNext()) {
            String path = iter.next();
            if (CmsFileUtil.removeTrailingSeparator(path).equals(CmsFileUtil.removeTrailingSeparator(siteroot))) {
                LOG.debug("keeping path because it is the site root: " + path);
            } else if (cms.existsResource(
                CmsStringUtil.joinPaths(path, CmsADEManager.CONFIG_SUFFIX),
                CmsResourceFilter.IGNORE_EXPIRATION)) {
                LOG.debug("keeping path because it is a sub-sitemap: " + path);
            } else {
                LOG.debug("removing path " + path);
                iter.remove();
            }
        }
        for (String ancestor : ancestors) {
            try {
                CmsResource ancestorRes = cms.readResource(ancestor, CmsResourceFilter.IGNORE_EXPIRATION);
                CmsLocaleGroup group = readLocaleGroup(ancestorRes);
                if (group.isRealGroup()) {
                    return ancestorRes;
                }
            } catch (CmsException e) {
                LOG.error(e.getLocalizedMessage(), e);
            }
        }
        // there is at least one ancestor: the site root
        String result = ancestors.get(0);
        LOG.debug("result = " + result);
        return cms.readResource(result, CmsResourceFilter.IGNORE_EXPIRATION);

    }

    /**
     * Gets the main translation locale configured for the given root path.<p>
     *
     * @param rootPath a root path
     *
     * @return the main translation locale configured for the given path, or null if none was found
     */
    public Locale getMainLocale(String rootPath) {

        CmsSite site = OpenCms.getSiteManager().getSiteForRootPath(rootPath);
        if (site == null) {
            return null;
        }
        return site.getMainTranslationLocale(null);
    }

    /**
     * Reads the locale group of a default file.<p>
     *
     * @param resource a resource which might be a folder or a file
     * @return the locale group corresponding to the default file
     *
     * @throws CmsException if something goes wrong
     */
    public CmsLocaleGroup readDefaultFileLocaleGroup(CmsResource resource) throws CmsException {

        if (resource.isFolder()) {
            CmsResource defaultFile = m_cms.readDefaultFile(resource, CmsResourceFilter.IGNORE_EXPIRATION);
            if (defaultFile != null) {
                return readLocaleGroup(defaultFile);
            } else {
                LOG.warn("default file not found, reading locale group of folder.");
            }
        }
        return readLocaleGroup(resource);

    }

    /**
     * Reads a locale group from the VFS.<p>
     *
     * @param resource the resource for which to read the locale group
     *
     * @return the locale group for the resource
     * @throws CmsException if something goes wrong
     */
    public CmsLocaleGroup readLocaleGroup(CmsResource resource) throws CmsException {

        if (resource.isFolder()) {
            CmsResource defaultFile = m_cms.readDefaultFile(resource, CmsResourceFilter.IGNORE_EXPIRATION);
            if (defaultFile != null) {
                resource = defaultFile;
            }
        }

        List<CmsRelation> relations = m_cms.readRelations(
            CmsRelationFilter.ALL.filterType(CmsRelationType.LOCALE_VARIANT).filterStructureId(
                resource.getStructureId()));
        List<CmsRelation> out = Lists.newArrayList();
        List<CmsRelation> in = Lists.newArrayList();
        for (CmsRelation rel : relations) {
            if (rel.getSourceId().equals(resource.getStructureId())) {
                out.add(rel);
            } else {
                in.add(rel);
            }
        }
        CmsResource primaryResource = null;
        List<CmsResource> secondaryResources = Lists.newArrayList();
        if ((out.size() == 0) && (in.size() == 0)) {
            primaryResource = resource;
        } else if ((out.size() == 0) && (in.size() > 0)) {
            primaryResource = resource;
            // resource is the primary variant
            for (CmsRelation relation : in) {
                CmsResource source = relation.getSource(m_cms, CmsResourceFilter.ALL);
                secondaryResources.add(source);
            }
        } else if ((out.size() == 1) && (in.size() == 0)) {

            CmsResource target = out.get(0).getTarget(m_cms, CmsResourceFilter.ALL);
            primaryResource = target;
            CmsRelationFilter filter = CmsRelationFilter.TARGETS.filterType(
                CmsRelationType.LOCALE_VARIANT).filterStructureId(target.getStructureId());
            List<CmsRelation> relationsToTarget = m_cms.readRelations(filter);
            for (CmsRelation targetRelation : relationsToTarget) {
                CmsResource secondaryResource = targetRelation.getSource(m_cms, CmsResourceFilter.ALL);
                secondaryResources.add(secondaryResource);
            }
        } else {
            throw new IllegalStateException(
                "illegal locale variant relations for resource with id="
                    + resource.getStructureId()
                    + ", path="
                    + resource.getRootPath());
        }
        return new CmsLocaleGroup(m_cms, primaryResource, secondaryResources);
    }

    /**
     * Helper method for reading the default file of a folder.<p>
     *
     * If the resource given already is a file, it will be returned, otherwise
     * the default file (or null, if none exists) of the folder will be returned.
     *
     * @param res the resource whose default file to read
     * @return the default file
     */
    protected CmsResource getDefaultFileOrSelf(CmsResource res) {

        CmsResource defaultfile = null;
        if (res.isFolder()) {
            try {
                defaultfile = m_cms.readDefaultFile("" + res.getStructureId());
            } catch (CmsSecurityException e) {
                LOG.error(e.getLocalizedMessage(), e);
                return null;
            } catch (CmsException e) {
                LOG.error(e.getLocalizedMessage(), e);
                return null;
            }
            return defaultfile;
        }
        return res;

    }

    /**
     * Checks that the resource is not locked by another user.<p>
     *
     * @param resource the resource
     * @return true if the resource is not locked by another user
     *
     * @throws CmsException if something goes wrong
     */
    private boolean checkLock(CmsResource resource) throws CmsException {

        CmsLock lock = m_cms.getLock(resource);
        return lock.isUnlocked() || lock.getUserId().equals(m_cms.getRequestContext().getCurrentUser().getId());
    }

}
