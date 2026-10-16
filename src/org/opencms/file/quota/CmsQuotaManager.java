/*
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (c) Alkacon Software GmbH & Co. KG (https://www.alkacon.com)
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
 * company website: https://www.alkacon.com
 *
 * For further information about OpenCms, please see the
 * project website: https://www.opencms.org
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package org.opencms.file.quota;

import org.opencms.ade.configuration.CmsADEConfigDataInternal.AttributeValue;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsResource;
import org.opencms.gwt.shared.CmsQuotaStatus;
import org.opencms.gwt.shared.CmsQuotaStatus.State;
import org.opencms.main.CmsException;
import org.opencms.main.CmsLog;
import org.opencms.main.OpenCms;
import org.opencms.util.CmsFileUtil;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import org.apache.commons.logging.Log;

/**
 * Evaluates Offline storage quotas for budget scopes, with an optional system-wide default.<p>
 *
 * Website roots and subsites marked with {@link CmsQuotaConfig#ATTR_UPLOAD_QUOTA_SCOPE} form budget
 * scopes. Ordinary subsites contribute to their enclosing scope's usage. Subordinate budget scopes
 * have separate budgets and are excluded from the enclosing scope's usage.<p>
 *
 * This internal service does not perform authorization; UI/RPC callers must check access before
 * exposing its snapshots. Quotas are advisory; this class does not enforce upload restrictions.<p>
 */
public class CmsQuotaManager {

    /** Logger instance for this class. */
    private static final Log LOG = CmsLog.getLog(CmsQuotaManager.class);

    /** Shared manager instance. */
    private static final CmsQuotaManager INSTANCE = new CmsQuotaManager();

    /** Administrative Offline context for the central quota configuration. */
    private volatile CmsObject m_cms;

    /**
     * Hidden singleton constructor.<p>
     */
    private CmsQuotaManager() {}

    /**
     * Calculates a snapshot using one folder report and all known scope boundaries.<p>
     *
     * @param definitions all scope definitions
     * @param report the offline folder report, requested for all scope paths
     *
     * @return snapshots indexed by scope root path
     */
    public static Map<String, CmsQuotaStatus> calculate(
        Collection<CmsQuotaDefinition> definitions,
        Map<String, CmsFolderReportEntry> report) {

        return calculate(definitions, report, null);
    }

    /**
     * Calculates a snapshot with a configurable fallback for scopes without an applicable default.<p>
     *
     * @param definitions all scope definitions
     * @param report the offline folder report, with exclusive sizes calculated against all budget scope paths
     * @param systemDefaultQuota system-wide default in bytes, null if not configured
     *
     * @return snapshots indexed by scope root path
     */
    public static Map<String, CmsQuotaStatus> calculate(
        Collection<CmsQuotaDefinition> definitions,
        Map<String, CmsFolderReportEntry> report,
        Long systemDefaultQuota) {

        Map<String, CmsQuotaStatus> result = new LinkedHashMap<>();
        for (CmsQuotaDefinition definition : definitions) {
            Long quota = resolveQuota(definition, definitions, () -> systemDefaultQuota);
            CmsFolderReportEntry sizes = report.get(definition.getRootPath());
            long used = sizes == null ? -1 : sizes.getTreeSizeExclusive();
            long total = sizes == null ? -1 : sizes.getTreeSize();
            State state;
            if ((used < 0) || ((quota != null) && (quota.longValue() < 0))) {
                state = State.UNAVAILABLE;
            } else if (quota == null) {
                state = State.UNCONFIGURED;
            } else if (quota.longValue() == 0) {
                state = State.UNLIMITED;
            } else {
                state = CmsQuotaStatus.getUsageState(used, quota.longValue());
            }
            result.put(
                definition.getRootPath(),
                new CmsQuotaStatus(
                    definition.getRootPath(),
                    used,
                    total,
                    quota == null ? -1 : quota.longValue(),
                    state,
                    definition.getIndividualQuota() != null));
        }
        return result;
    }

    /**
     * Gets the shared storage quota manager.<p>
     *
     * @return the shared manager
     */
    public static CmsQuotaManager get() {

        return INSTANCE;
    }

    /**
     * Creates budget definitions for websites and explicitly marked subsites.<p>
     *
     * Individual budgets and scope markers are not inherited. Defaults from ordinary subsites
     * remain applicable to budget scopes below them without creating additional scope boundaries.
     * Each definition contains the nearest applicable default, including defaults from enclosing
     * websites and subsites. A normal subsite's individual budget is ignored.<p>
     *
     * @param attributes local attributes, including masters and connectors, indexed by root path
     * @param siteRoots the website root paths, which always form budget scopes
     *
     * @return definitions for the budget scopes
     */
    public static Collection<CmsQuotaDefinition> getDefinitions(
        Map<String, Map<String, AttributeValue>> attributes,
        Collection<String> siteRoots) {

        Map<String, Map<String, AttributeValue>> normalized = new HashMap<>();
        attributes.forEach((path, values) -> normalized.put(CmsFileUtil.addTrailingSeparator(path), values));
        Set<String> websites = siteRoots.stream().map(CmsFileUtil::addTrailingSeparator).collect(Collectors.toSet());
        Collection<CmsQuotaDefinition> result = new ArrayList<>();
        for (Map.Entry<String, Map<String, AttributeValue>> entry : normalized.entrySet()) {
            String rootPath = entry.getKey();
            AttributeValue scope = entry.getValue().get(CmsQuotaConfig.ATTR_UPLOAD_QUOTA_SCOPE);
            if (!websites.contains(rootPath) && ((scope == null) || !Boolean.parseBoolean(scope.getValue().trim()))) {
                continue;
            }
            Long defaultQuota = null;
            String ancestor = rootPath;
            while ((ancestor != null) && (defaultQuota == null)) {
                AttributeValue value = normalized.getOrDefault(ancestor, Collections.emptyMap()).get(
                    CmsQuotaConfig.ATTR_UPLOAD_QUOTA_DEFAULT);
                defaultQuota = CmsQuotaConfig.parse(value == null ? null : value.getValue());
                ancestor = CmsResource.getParentFolder(ancestor);
            }
            AttributeValue individualQuota = entry.getValue().get(CmsQuotaConfig.ATTR_UPLOAD_QUOTA);
            result.add(
                new CmsQuotaDefinition(
                    rootPath,
                    defaultQuota,
                    CmsQuotaConfig.parse(individualQuota == null ? null : individualQuota.getValue())));
        }
        return result;
    }

    /**
     * Resolves a single finite budget before requesting its consumption.<p>
     *
     * @param rootPath the resource's root path
     * @param definitions the budget scopes
     * @param systemDefault supplies the system default only when needed
     * @param skipScope checks whether the scope needs a warning
     * @param sizes supplies the inclusive and exclusive sizes for the selected scope
     * @return the status, or null if no consumption is needed
     */
    static CmsQuotaStatus getLimitedStatus(
        String rootPath,
        Collection<CmsQuotaDefinition> definitions,
        Supplier<Long> systemDefault,
        Predicate<String> skipScope,
        Function<String, CmsFolderReportEntry> sizes) {

        String path = CmsFileUtil.addTrailingSeparator(rootPath);
        CmsQuotaDefinition selected = null;
        for (CmsQuotaDefinition definition : definitions) {
            if (path.startsWith(definition.getRootPath())
                && ((selected == null) || (definition.getRootPath().length() > selected.getRootPath().length()))) {
                selected = definition;
            }
        }
        if ((selected == null) || skipScope.test(selected.getRootPath())) {
            return null;
        }
        Long quota = resolveQuota(selected, definitions, systemDefault);
        if ((quota == null) || (quota.longValue() <= 0)) {
            return null;
        }
        CmsFolderReportEntry report = sizes.apply(selected.getRootPath());
        long used = report == null ? -1 : report.getTreeSizeExclusive();
        return new CmsQuotaStatus(
            selected.getRootPath(),
            used,
            report == null ? -1 : report.getTreeSize(),
            quota.longValue(),
            used < 0 ? State.UNAVAILABLE : CmsQuotaStatus.getUsageState(used, quota.longValue()),
            selected.getIndividualQuota() != null);
    }

    /**
     * Resolves an individual budget, inherited default or system default.<p>
     *
     * @param definition the selected scope
     * @param definitions all scope definitions
     * @param systemDefault supplies the system default only if no other setting applies
     * @return the applicable budget, or null if none is configured
     */
    private static Long resolveQuota(
        CmsQuotaDefinition definition,
        Collection<CmsQuotaDefinition> definitions,
        Supplier<Long> systemDefault) {

        Long quota = definition.getIndividualQuota();
        if (quota == null) {
            int nearest = -1;
            for (CmsQuotaDefinition ancestor : definitions) {
                if (definition.getRootPath().startsWith(ancestor.getRootPath())
                    && (ancestor.getDefaultQuota() != null)
                    && (ancestor.getRootPath().length() > nearest)) {
                    quota = ancestor.getDefaultQuota();
                    nearest = ancestor.getRootPath().length();
                }
            }
        }
        return quota == null ? systemDefault.get() : quota;
    }

    /**
     * Gets a finite budget's status only if the caller still needs it.<p>
     *
     * Scope resolution precedes the caller's check. Configuration and usage changes are therefore
     * not hidden by a cached decision for a different or newly introduced budget scope.<p>
     *
     * @param rootPath the resource's VFS root path
     * @param skipScope whether the caller already acknowledged the scope's warning
     * @return the status, or null if no usage check is needed or configuration is unavailable
     */
    public CmsQuotaStatus getLimitedStatus(String rootPath, Predicate<String> skipScope) {

        try {
            Collection<CmsQuotaDefinition> definitions = getDefinitions(
                OpenCms.getADEManager().getLocalSitemapAttributes(false),
                OpenCms.getSiteManager().getSiteRoots());
            return getLimitedStatus(
                rootPath,
                definitions,
                () -> m_cms == null ? CmsQuotaDefinition.UNAVAILABLE : CmsQuotaConfig.readSystemDefault(m_cms),
                skipScope,
                scope -> OpenCms.getFolderSizeTracker(false).getFolderReport(
                    definitions.stream().map(CmsQuotaDefinition::getRootPath).filter(
                        path -> path.startsWith(scope)).collect(Collectors.toList())).get(scope));
        } catch (RuntimeException e) {
            LOG.error("Could not determine storage quota", e);
            return null;
        }
    }

    /**
     * Finds the nearest enclosing budget scope for a resource.<p>
     *
     * Resources in ordinary subsites belong to the enclosing budget scope.<p>
     *
     * @param rootPath the resource's absolute VFS root path
     *
     * @return its snapshot, or an unavailable snapshot if it has no known scope
     */
    public CmsQuotaStatus getStatus(String rootPath) {

        String path = CmsFileUtil.addTrailingSeparator(rootPath);
        CmsQuotaStatus result = null;
        for (CmsQuotaStatus status : getStatuses().values()) {
            if (path.startsWith(status.getRootPath())
                && ((result == null) || (status.getRootPath().length() > result.getRootPath().length()))) {
                result = status;
            }
        }
        return result != null ? result : new CmsQuotaStatus(path, -1, -1, -1, State.UNAVAILABLE);
    }

    /**
     * Reads cached sitemap attributes and evaluates all scopes against the existing offline size cache.<p>
     *
     * @return the snapshots; empty if the configuration could not be read
     */
    public Map<String, CmsQuotaStatus> getStatuses() {

        try {
            Collection<CmsQuotaDefinition> definitions = getDefinitions(
                OpenCms.getADEManager().getLocalSitemapAttributes(false),
                OpenCms.getSiteManager().getSiteRoots());
            return calculate(
                definitions,
                OpenCms.getFolderSizeTracker(false).getFolderReport(
                    definitions.stream().map(CmsQuotaDefinition::getRootPath).collect(Collectors.toList())),
                m_cms == null ? CmsQuotaDefinition.UNAVAILABLE : CmsQuotaConfig.readSystemDefault(m_cms));
        } catch (RuntimeException e) {
            LOG.error("Could not determine storage quotas", e);
            return Collections.emptyMap();
        }
    }

    /**
     * Initializes the administrative context used to read the central quota configuration.<p>
     *
     * @param adminCms the administrative CMS context supplied during system startup
     *
     * @throws CmsException if the Offline context cannot be initialized
     */
    public void initialize(CmsObject adminCms) throws CmsException {

        CmsObject cms = OpenCms.initCmsObject(adminCms);
        cms.getRequestContext().setSiteRoot("");
        cms.getRequestContext().setCurrentProject(cms.readProject("Offline"));
        m_cms = cms;
    }
}
