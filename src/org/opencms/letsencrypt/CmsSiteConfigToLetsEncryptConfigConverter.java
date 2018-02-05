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

package org.opencms.letsencrypt;

import org.opencms.json.JSONArray;
import org.opencms.json.JSONObject;
import org.opencms.letsencrypt.CmsLetsEncryptConfiguration.Mode;
import org.opencms.main.CmsLog;
import org.opencms.report.I_CmsReport;
import org.opencms.site.CmsSSLMode;
import org.opencms.site.CmsSite;
import org.opencms.site.CmsSiteManagerImpl;
import org.opencms.site.CmsSiteMatcher;
import org.opencms.util.CmsStringUtil;

import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.UnknownHostException;
import java.security.MessageDigest;
import java.util.Collection;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Set;

import org.apache.commons.codec.binary.Hex;
import org.apache.commons.logging.Log;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;
import com.google.common.collect.Sets;

import de.malkusch.whoisServerList.publicSuffixList.PublicSuffixList;
import de.malkusch.whoisServerList.publicSuffixList.PublicSuffixListFactory;

/**
 * Class which converts the OpenCms site configuration to a certificate configuration for the LetsEncrypt docker instance.
 */
public class CmsSiteConfigToLetsEncryptConfigConverter {

    /**
     * Represents a grouping of domains into certificates.
     */
    public static class DomainGrouping {

        /** The list of domain sets. */
        private List<Set<String>> m_domainGroups = Lists.newArrayList();

        /**
         * Adds a domain group.<p>
         *
         * @param domains the domain group
         */
        public void addDomainSet(Set<String> domains) {

            if (!domains.isEmpty()) {
                m_domainGroups.add(domains);
            }
        }

        /**
         * Generates the JSON configuration corresponding to the domain grouping.<p>
         *
         * @return the JSON configuration corresponding to the domain grouping
         */
        public String generateCertJson() {

            try {
                JSONObject result = new JSONObject();
                for (Set<String> domainGroup : m_domainGroups) {
                    String key = computeName(domainGroup);
                    if (key != null) {
                        result.put(key, new JSONArray(domainGroup));
                    }
                }
                return result.toString();
            } catch (Exception e) {
                LOG.error(e.getLocalizedMessage(), e);
                return null;
            }
        }

        /**
         * Checks all domains for resolvability and return the unresolvable ones.
         *
         * @return the set of unresolvable domains
         */
        public Set<String> getUnresolvableDomains() {

            Set<String> result = Sets.newHashSet();
            for (Set<String> domainGroup : m_domainGroups) {
                for (String domain : domainGroup) {
                    try {
                        InetAddress.getByName(domain);
                    } catch (UnknownHostException e) {
                        result.add(domain);
                    } catch (SecurityException e) {
                        LOG.error(e.getLocalizedMessage(), e);
                    }
                }
            }
            return result;
        }

        /**
         * Checks if the domain grouping does not contain any domain groups.
         *
         * @return true if there are no domain groups
         */
        public boolean isEmpty() {

            return m_domainGroups.isEmpty();
        }

        /**
         * Deterministically generates a certificate name for a set of domains.<p>
         *
         * @param domains the domains
         * @return the certificate name
         */
        private String computeName(Set<String> domains) {

            try {
                List<String> domainList = Lists.newArrayList(domains);
                Collections.sort(domainList);
                String prefix = domainList.get(0);
                MessageDigest md5 = MessageDigest.getInstance("MD5");
                for (String domain : domainList) {
                    md5.update(domain.getBytes("UTF-8"));
                    md5.update((byte)10);
                }

                return prefix + "-" + new String(Hex.encodeHex(md5.digest()));
            } catch (Exception e) {
                LOG.error(e.getLocalizedMessage(), e);
                return null;
            }

        }
    }

    /**
     * Represents the domain information for a single site.<p>
     */
    public static class SiteDomainInfo {

        /** The common root domain, or null if there is no common root domain. */
        private String m_commonRootDomain;

        /** The set of domains for the site. */
        private Set<String> m_domains = Sets.newHashSet();

        /** True if an invalid port was used. */
        private boolean m_invalidPort;

        /**
         * Creates a new instance.<p>
         *
         * @param domains the set of domains
         * @param commonRootDomain the common root domain
         * @param invalidPort true if an invalid port was used
         */
        public SiteDomainInfo(Set<String> domains, String commonRootDomain, boolean invalidPort) {
            super();
            m_domains = domains;
            m_commonRootDomain = commonRootDomain;
            m_invalidPort = invalidPort;
        }

        /**
         * Gets the common root domain.<p>
         *
         * @return the common root domain
         */
        public String getCommonRootDomain() {

            return m_commonRootDomain;
        }

        /**
         * Gets the set of domains.<p>
         *
         * @return the set of domains
         */
        public Set<String> getDomains() {

            return m_domains;
        }

        /**
         * True if an invalid port was used.<p>
         *
         * @return true if an invalid port was used
         */
        public boolean hasInvalidPort() {

            return m_invalidPort;
        }
    }

    /**
     * Timed cache for the public suffix list.<p>
     */
    static class SuffixListCache {

        /** The public suffix list. */
        private PublicSuffixList m_suffixList;

        /** The time the list was last cached. */
        private long m_timestamp = -1;

        /**
         * Gets the public suffix list, loading it if hasn't been loaded before or the time since it was loaded was too long ago.<p>
         *
         * @return the public suffix list
         */
        public synchronized PublicSuffixList getPublicSuffixList() {

            long now = System.currentTimeMillis();
            if ((m_suffixList == null) || ((now - m_timestamp) > (1000 * 3600))) {
                PublicSuffixListFactory factory = new PublicSuffixListFactory();
                try (InputStream stream = CmsSiteConfigToLetsEncryptConfigConverter.class.getResourceAsStream(
                    "public_suffix_list.dat")) {
                    m_suffixList = factory.build(stream);
                    m_timestamp = now;
                } catch (IOException e) {
                    LOG.error(e.getLocalizedMessage(), e);
                }
            }
            return m_suffixList;

        }
    }

    /** The logger used for this class. */
    static final Log LOG = CmsLog.getLog(CmsSiteConfigToLetsEncryptConfigConverter.class);

    /** Disables grouping. */
    public static final boolean GROUPING_DISABLED = true;

    /** Lock to prevent two converters from running simultaneously. */
    private static Object LOCK = new Object();

    /** The cache for the public suffix list. */
    private static SuffixListCache SUFFIX_LIST_CACHE = new SuffixListCache();

    /** The configuration. */
    private CmsLetsEncryptConfiguration m_config;

    /** The object to which the configuration is sent after it is generated. */
    private I_CmsLetsEncryptUpdater m_configUpdater;

    /**
     * Creates a new instance.<p>
     *
     * @param config the LetsEncrypt configuration
     */
    public CmsSiteConfigToLetsEncryptConfigConverter(CmsLetsEncryptConfiguration config) {
        m_config = config;
        m_configUpdater = new CmsLetsEncryptUpdater(config);
    }

    /**
     * Computes the domain information for a single site.<p>
     *
     * @param site the site
     * @return the domain information for a site
     */
    private static SiteDomainInfo getDomainInfo(CmsSite site) {

        List<String> urls = Lists.newArrayList();
        for (CmsSiteMatcher matcher : site.getAllMatchers()) {
            urls.add(matcher.getUrl());

        }
        return getDomainInfo(urls);
    }

    /**
     * Computes the SiteDomainInfo bean for a collection of URIs.<p>
     *
     * @param uris a collection of URIs
     * @return the SiteDomainInfo bean for the URIs
     */
    private static SiteDomainInfo getDomainInfo(Collection<String> uris) {

        Set<String> rootDomains = Sets.newHashSet();
        Set<String> domains = Sets.newHashSet();
        boolean invalidPort = false;

        for (String uriStr : uris) {

            try {
                URI uri = new URI(uriStr);
                int port = uri.getPort();
                if (!((port == 80) || (port == 443) || (port == -1))) {
                    invalidPort = true;
                }
                String rootDomain = getDomainRoot(uri);
                if (rootDomain == null) {
                    LOG.warn("Host is not under public suffix, skipping it: " + uri);
                    continue;
                }
                domains.add(uri.getHost());
                rootDomains.add(rootDomain);
            } catch (URISyntaxException e) {
                LOG.warn("getDomainInfo: invalid URI " + uriStr, e);
                continue;
            }
        }
        String rootDomain = (rootDomains.size() == 1 ? rootDomains.iterator().next() : null);
        return new SiteDomainInfo(domains, rootDomain, invalidPort);
    }

    /**
     * Calculates the domain root for a given uri.<p>
     *
     * @param uri an URI
     * @return the domain root for the uri
     */
    private static String getDomainRoot(URI uri) {

        String host = uri.getHost();
        return SUFFIX_LIST_CACHE.getPublicSuffixList().getRegistrableDomain(host);
    }

    /**
     * Gets the domains for a collection of SiteDomainInfo beans.<p>
     *
     * @param infos a collection of SiteDomainInfo beans
     * @return the domains for the beans
     */
    private static Set<String> getDomains(Collection<SiteDomainInfo> infos) {

        Set<String> domains = Sets.newHashSet();
        for (SiteDomainInfo info : infos) {
            for (String domain : info.getDomains()) {
                domains.add(domain);
            }
        }
        return domains;
    }

    /**
     * Runs the certificate configuration update for the sites configured in a site manager.<p>
     *
     * @param report the report to write to
     * @param siteManager the site manager instance
     *
     * @return true if the Letsencrypt update was successful
     */
    public boolean run(I_CmsReport report, CmsSiteManagerImpl siteManager) {

        synchronized (LOCK) {
            // *not* using getAvailable sites here, because the result does not include sites with unpublished site folders if called with a CmsObject in the Online project
            // Instead we use getSites() and avoid duplicates using an IdentityHashMap
            IdentityHashMap<CmsSite, CmsSite> siteIdMap = new IdentityHashMap<CmsSite, CmsSite>();
            for (CmsSite site : siteManager.getSites().values()) {
                if (site.getSSLMode() == CmsSSLMode.LETS_ENCRYPT) {
                    siteIdMap.put(site, site);
                }
            }
            List<CmsSite> sites = Lists.newArrayList(siteIdMap.values());
            List<String> workplaceServers = siteManager.getWorkplaceServers(CmsSSLMode.LETS_ENCRYPT);
            return run(report, sites, workplaceServers);
        }
    }

    /**
     * Computes the domain grouping for a set of sites and workplace URLs.<p>
     *
     * @param sites the sites
     * @param workplaceUrls the workplace URLS
     * @return the domain grouping
     */
    private DomainGrouping computeDomainGrouping(Collection<CmsSite> sites, Collection<String> workplaceUrls) {

        DomainGrouping result = new DomainGrouping();
        if (LOG.isInfoEnabled()) {
            LOG.info("Computing domain grouping for sites...");
            List<String> servers = Lists.newArrayList();
            for (CmsSite site : sites) {
                servers.add(site.getUrl());
            }
            LOG.info("SITES = " + CmsStringUtil.listAsString(servers, ", "));
        }

        Mode mode = m_config.getMode();
        boolean addWp = false;
        boolean addSites = false;
        if ((mode == Mode.all) || (mode == Mode.sites)) {
            addSites = true;
        }
        if ((mode == Mode.all) || (mode == Mode.workplace)) {
            addWp = true;
        }

        if (addWp) {
            Set<String> workplaceDomains = Sets.newHashSet();
            for (String wpServer : workplaceUrls) {
                try {
                    URI uri = new URI(wpServer);
                    workplaceDomains.add(uri.getHost());
                } catch (Exception e) {
                    LOG.error(e.getLocalizedMessage(), e);
                }
            }
            result.addDomainSet(workplaceDomains);
        }

        if (addSites) {
            Multimap<String, SiteDomainInfo> infosByRootDomain = ArrayListMultimap.create();

            List<SiteDomainInfo> ungroupedSites = Lists.newArrayList();
            for (CmsSite site : sites) {
                SiteDomainInfo info = getDomainInfo(site);
                if (info.hasInvalidPort()) {
                    LOG.warn("Invalid port occuring in site definition: " + site);
                    continue;
                }
                String root = info.getCommonRootDomain();
                if ((root == null) || GROUPING_DISABLED) {
                    ungroupedSites.add(info);
                } else {
                    infosByRootDomain.put(root, info);
                }
            }
            List<String> keysToRemove = Lists.newArrayList();

            for (String key : infosByRootDomain.keySet()) {
                Collection<SiteDomainInfo> siteInfos = infosByRootDomain.get(key);
                Set<String> domains = getDomains(siteInfos);
                if (domains.size() > 100) {
                    LOG.info("Too many domains for root domain " + key + ", splitting them up by site instead.");
                    keysToRemove.add(key);
                    for (SiteDomainInfo info : siteInfos) {
                        ungroupedSites.add(info);
                    }
                }
            }
            for (String key : keysToRemove) {
                infosByRootDomain.removeAll(key);
            }
            for (SiteDomainInfo ungroupedSite : ungroupedSites) {
                Set<String> domains = getDomains(Collections.singletonList(ungroupedSite));
                result.addDomainSet(domains);
                LOG.info("DOMAINS (site config): " + domains);
            }
            for (String key : infosByRootDomain.keySet()) {
                Set<String> domains = getDomains(infosByRootDomain.get(key));
                result.addDomainSet(domains);
                LOG.info("DOMAINS (" + key + ")" + domains);
            }
        }
        return result;
    }

    /**
     * Runs the certificate configuration update for a given set of sites and workplace URLS.<p>
     *
     * @param report the report to write to
     * @param sites the sites
     * @param workplaceUrls the workplace URLS
     *
     * @return true if the Letsencrypt update was successful
     */
    private boolean run(I_CmsReport report, Collection<CmsSite> sites, Collection<String> workplaceUrls) {

        try {
            DomainGrouping domainGrouping = computeDomainGrouping(sites, workplaceUrls);
            if (domainGrouping.isEmpty()) {
                report.println(
                    org.opencms.ui.apps.Messages.get().container(
                        org.opencms.ui.apps.Messages.RPT_LETSENCRYPT_NO_DOMAINS_0));
                return false;
            }
            String certConfig = domainGrouping.generateCertJson();
            if (!m_configUpdater.update(certConfig)) {
                report.println(
                    org.opencms.ui.apps.Messages.get().container(
                        org.opencms.ui.apps.Messages.RPT_LETSENCRYPT_UPDATE_FAILED_0),
                    I_CmsReport.FORMAT_WARNING);
                return false;
            }
            return true;
        } catch (Exception e) {
            report.println(e);
            return false;
        }
    }

}
