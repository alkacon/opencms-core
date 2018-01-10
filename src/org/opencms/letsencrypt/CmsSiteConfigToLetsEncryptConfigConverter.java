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
import org.opencms.main.CmsLog;
import org.opencms.report.I_CmsReport;
import org.opencms.site.CmsSite;
import org.opencms.site.CmsSiteMatcher;
import org.opencms.util.CmsStringUtil;

import java.net.InetAddress;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.UnknownHostException;
import java.security.MessageDigest;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.apache.commons.codec.binary.Hex;
import org.apache.commons.logging.Log;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;
import com.google.common.collect.Sets;
import com.google.common.net.InternetDomainName;

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

    /** The logger used for this class. */
    static final Log LOG = CmsLog.getLog(CmsSiteConfigToLetsEncryptConfigConverter.class);

    /** The object to which the configuration is sent after it is generated. */
    private I_CmsLetsEncryptUpdater m_configUpdater;

    /**
     * Creates a new instance.<p>
     *
     * @param config the LetsEncrypt configuration
     */
    public CmsSiteConfigToLetsEncryptConfigConverter(CmsLetsEncryptConfiguration config) {
        m_configUpdater = new CmsLetsEncryptUpdater(config);
    }

    /**
     * Computes the domain grouping for a set of sites and workplace URLs.<p>
     *
     * @param sites the sites
     * @param workplaceUrls the workplace URLS
     * @return the domain grouping
     */
    private static DomainGrouping computeDomainGrouping(Collection<CmsSite> sites, Collection<String> workplaceUrls) {

        DomainGrouping result = new DomainGrouping();
        if (LOG.isInfoEnabled()) {
            LOG.info("Computing domain grouping for sites...");
            List<String> servers = Lists.newArrayList();
            for (CmsSite site : sites) {
                servers.add(site.getUrl());
            }
            LOG.info("SITES = " + CmsStringUtil.listAsString(servers, ", "));
        }
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

        Multimap<String, SiteDomainInfo> infosByRootDomain = ArrayListMultimap.create();

        List<SiteDomainInfo> ungroupedSites = Lists.newArrayList();
        for (CmsSite site : sites) {
            SiteDomainInfo info = getDomainInfo(site);
            if (info.hasInvalidPort()) {
                LOG.warn("Invalid port occuring in site definition: " + site);
                continue;
            }
            String root = info.getCommonRootDomain();
            if (root == null) {
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

        return result;
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
        InternetDomainName name = InternetDomainName.from(host);
        if (name.isUnderPublicSuffix()) {
            return name.topPrivateDomain().toString();
        } else {
            // localhost, ip address or something else that is not a 'normal' internet domain
            return null;
        }
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
     * Runs the certificate configuration update for a given set of sites and workplace URLS.<p>
     *
     * @param report the report to write to
     * @param sites the sites
     * @param workplaceUrls the workplace URLS
     *
     * @return true if the Letsencrypt update was successful
     */
    public boolean run(I_CmsReport report, Collection<CmsSite> sites, Collection<String> workplaceUrls) {

        try {
            DomainGrouping domainGrouping = CmsSiteConfigToLetsEncryptConfigConverter.computeDomainGrouping(
                sites,
                workplaceUrls);
            if (domainGrouping.isEmpty()) {
                report.println(
                    org.opencms.ui.apps.Messages.get().container(
                        org.opencms.ui.apps.Messages.RPT_LETSENCRYPT_NO_DOMAINS_0));
                return false;
            }
            Set<String> unresolvableDomains = domainGrouping.getUnresolvableDomains();
            if (unresolvableDomains.size() > 0) {
                LOG.warn(
                    "Found unresolvable domains while trying to generate LetsEncrypt config: " + unresolvableDomains);
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
