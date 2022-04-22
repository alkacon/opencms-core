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

package org.opencms.configuration;

import org.opencms.file.CmsObject;
import org.opencms.letsencrypt.CmsLetsEncryptConfiguration;
import org.opencms.letsencrypt.CmsLetsEncryptConfiguration.Trigger;
import org.opencms.letsencrypt.CmsSiteConfigToLetsEncryptConfigConverter;
import org.opencms.main.CmsLog;
import org.opencms.main.OpenCms;
import org.opencms.report.CmsLogReport;
import org.opencms.site.CmsAlternativeSiteRootMapping;
import org.opencms.site.CmsSSLMode;
import org.opencms.site.CmsSite;
import org.opencms.site.CmsSiteManagerImpl;
import org.opencms.site.CmsSiteMatcher;
import org.opencms.site.CmsSiteMatcher.RedirectMode;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.digester3.CallMethodRule;
import org.apache.commons.digester3.Digester;
import org.apache.commons.digester3.NodeCreateRule;
import org.apache.commons.digester3.ObjectCreateRule;
import org.apache.commons.digester3.Rule;

import org.dom4j.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.Attributes;

/**
 * Class to read and write the OpenCms site configuration.<p>
 */
public class CmsSitesConfiguration extends A_CmsXmlConfiguration implements I_CmsXmlConfigurationWithUpdateHandler {

    /** The "error" attribute. */
    public static final String A_ERROR = "error";

    /** The "errorPage" attribute. */
    public static final String A_ERROR_PAGE = "errorPage";

    /** The "exclusive" attribute. */
    public static final String A_EXCLUSIVE = "exclusive";

    /** The attribute name for the alias offset. */
    public static final String A_OFFSET = "offset";

    /** The "position" attribute. */
    public static final String A_POSITION = "position";

    /** The "redirect" attribute. */
    public static final String A_REDIRECT = "redirect";

    /** The "server" attribute. */
    public static final String A_SERVER = "server";

    /** The ssl mode attribute.*/
    public static final String A_SSL = "sslmode";

    /** Attribute name for the subsiteSelection option. */
    public static final String A_SUBSITE_SELECTION = "subsiteSelection";

    /** The "title" attribute. */
    public static final String A_TITLE = "title";

    /** The "usePermanentRedirects" attribute. */
    public static final String A_USE_PERMANENT_REDIRECTS = "usePermanentRedirects";

    /** The "webserver" attribute. */
    public static final String A_WEBSERVER = "webserver";

    /** The name of the DTD for this configuration. */
    public static final String CONFIGURATION_DTD_NAME = "opencms-sites.dtd";

    /** The name of the default XML file for this configuration. */
    public static final String DEFAULT_XML_FILE_NAME = "opencms-sites.xml";

    /** The node name for the alias node. */
    public static final String N_ALIAS = "alias";

    /** The node name for the default-uri node. */
    public static final String N_DEFAULT_URI = "default-uri";

    /** New secure modes node. */
    public static final String N_OLD_STYLE_SECURE_SERVER = "oldStyleSecureServer";

    /** The node name for the parameters. */
    public static final String N_PARAMETERS = "parameters";

    /** The node name for the secure site. */
    public static final String N_SECURE = "secure";

    /** Shared folder node name. */
    public static final String N_SHARED_FOLDER = "shared-folder";

    /** The node name for the sites node. */
    public static final String N_SITES = "sites";

    /** The node name which indicates if apache should be configurable in sitemanager. */
    public static final String N_WEBSERVERSCRIPTING = "webserver-scripting";

    /** Configuration node name. */
    public static final String N_WEBSERVERSCRIPTING_CONFIGTEMPLATE = "configtemplate";

    /** Configuration node name. */
    public static final String N_WEBSERVERSCRIPTING_FILENAMEPREFIX = "filenameprefix";

    /** Configuration node name. */
    public static final String N_WEBSERVERSCRIPTING_LOGGINGDIR = "loggingdir";

    /** Configuration node name. */
    public static final String N_WEBSERVERSCRIPTING_SECURETEMPLATE = "securetemplate";

    /** Configuration node name. */
    public static final String N_WEBSERVERSCRIPTING_TARGETPATH = "targetpath";

    /** Configuration node name. */
    public static final String N_WEBSERVERSCRIPTING_WEBSERVERSCRIPT = "webserverscript";

    /** The node name for the workplace-server node. */
    public static final String N_WORKPLACE_SERVER = "workplace-server";

    /** The CmsObject with admin privileges. */
    private CmsObject m_adminCms;

    /** The configured site manager. */
    private CmsSiteManagerImpl m_siteManager;

    /** Future for the LetsEncrypt async update. */
    private ScheduledFuture<?> m_updateFuture;

    /**
     * @see org.opencms.configuration.I_CmsXmlConfiguration#addXmlDigesterRules(org.apache.commons.digester3.Digester)
     */
    public void addXmlDigesterRules(Digester digester) {

        // add site configuration rule
        digester.addObjectCreate("*/" + N_SITES, CmsSiteManagerImpl.class);
        digester.addCallMethod("*/" + N_SITES + "/" + N_WORKPLACE_SERVER, "addWorkplaceServer", 2);
        digester.addCallParam("*/" + N_SITES + "/" + N_WORKPLACE_SERVER, 0);
        digester.addCallParam("*/" + N_SITES + "/" + N_WORKPLACE_SERVER, 1, A_SSL);
        digester.addCallMethod("*/" + N_SITES + "/" + N_DEFAULT_URI, "setDefaultUri", 0);
        digester.addCallMethod("*/" + N_SITES + "/" + N_OLD_STYLE_SECURE_SERVER, "setOldStyleSecureServerAllowed", 0);

        String configApachePath = "*/" + N_SITES + "/" + N_WEBSERVERSCRIPTING;
        digester.addCallMethod(configApachePath, "setWebServerScripting", 6);
        digester.addCallParam(configApachePath + "/" + N_WEBSERVERSCRIPTING_WEBSERVERSCRIPT, 0);
        digester.addCallParam(configApachePath + "/" + N_WEBSERVERSCRIPTING_TARGETPATH, 1);
        digester.addCallParam(configApachePath + "/" + N_WEBSERVERSCRIPTING_CONFIGTEMPLATE, 2);
        digester.addCallParam(configApachePath + "/" + N_WEBSERVERSCRIPTING_SECURETEMPLATE, 3);
        digester.addCallParam(configApachePath + "/" + N_WEBSERVERSCRIPTING_FILENAMEPREFIX, 4);
        digester.addCallParam(configApachePath + "/" + N_WEBSERVERSCRIPTING_LOGGINGDIR, 5);

        digester.addSetNext("*/" + N_SITES, "setSiteManager");

        // add site configuration rule
        String siteXpath = "*/" + N_SITES + "/" + N_SITE;
        digester.addRule(
            siteXpath,
            new CallMethodRule(
                "addSiteInternally",
                15,
                new Class[] {
                    String.class,
                    String.class,
                    String.class,
                    String.class,
                    String.class,
                    String.class,
                    String.class,
                    String.class,
                    String.class,
                    String.class,
                    String.class,
                    String.class,
                    SortedMap.class,
                    List.class,
                    Optional.class}) {

                @Override
                public void begin(String namespace, String name, Attributes attributes) throws Exception {

                    super.begin(namespace, name, attributes);
                    getDigester().peekParams()[12] = new TreeMap();
                    getDigester().peekParams()[13] = new ArrayList();
                    getDigester().peekParams()[14] = Optional.empty(); // non-string parameters must be initialized to a non-null value, so we have to use Optional
                }
            });
        digester.addCallParam(siteXpath, 0, A_SERVER);
        digester.addCallParam(siteXpath, 1, A_URI);
        digester.addCallParam(siteXpath, 2, A_TITLE);
        digester.addCallParam(siteXpath, 3, A_POSITION);
        digester.addCallParam(siteXpath, 4, A_ERROR_PAGE);
        digester.addCallParam(siteXpath, 5, A_WEBSERVER);
        digester.addCallParam(siteXpath, 6, A_SSL);
        digester.addCallParam("*/" + N_SITES + "/" + N_SITE + "/" + N_SECURE, 7, A_SERVER);
        digester.addCallParam("*/" + N_SITES + "/" + N_SITE + "/" + N_SECURE, 8, A_EXCLUSIVE);
        digester.addCallParam("*/" + N_SITES + "/" + N_SITE + "/" + N_SECURE, 9, A_ERROR);
        digester.addCallParam("*/" + N_SITES + "/" + N_SITE + "/" + N_SECURE, 10, A_USE_PERMANENT_REDIRECTS);
        digester.addCallParam(siteXpath, 11, A_SUBSITE_SELECTION);

        digester.addRule(siteXpath + "/" + N_PARAMETERS, new ObjectCreateRule(TreeMap.class) {

            @Override
            public void end(String namespace, String name) throws Exception {

                getDigester().peekParams()[12] = getDigester().peek();
                super.end(namespace, name);
            }
        });
        digester.addCallMethod(siteXpath + "/" + N_PARAMETERS + "/" + N_PARAM, "put", 2);
        digester.addCallParam(siteXpath + "/" + N_PARAMETERS + "/" + N_PARAM, 0, A_NAME);
        digester.addCallParam(siteXpath + "/" + N_PARAMETERS + "/" + N_PARAM, 1);

        digester.addRule("*/" + N_SITES + "/" + N_SITE + "/" + N_ALIAS, new Rule() {

            @Override
            public void begin(String namespace, String name, Attributes attributes) throws Exception {

                String server = attributes.getValue(A_SERVER);
                String redirect = attributes.getValue(A_REDIRECT);
                String offset = attributes.getValue(A_OFFSET);
                CmsSiteMatcher matcher = CmsSiteManagerImpl.createAliasSiteMatcher(server, redirect, offset);
                Object[] params = getDigester().peekParams();
                ((ArrayList)params[13]).add(matcher);

            }
        });

        try {
            digester.addRule(
                "*/" + N_SITES + "/" + N_SITE + "/" + CmsAlternativeSiteRootMapping.N_ALTERNATIVE_SITE_ROOT_MAPPING,
                new NodeCreateRule() {

                    @Override
                    public void end(String namespace, String name) throws Exception {

                        org.w3c.dom.Element elem = (org.w3c.dom.Element)digester.peek();
                        String uri = elem.getAttribute(I_CmsXmlConfiguration.A_URI);
                        String titlePrefix = elem.getAttribute(CmsAlternativeSiteRootMapping.A_TITLE_SUFFIX);
                        NodeList nodes = elem.getElementsByTagName(CmsAlternativeSiteRootMapping.N_PATH);
                        List<String> paths = new ArrayList<>();
                        for (int i = 0; i < nodes.getLength(); i++) {
                            org.w3c.dom.Element pathElem = (org.w3c.dom.Element)nodes.item(i);
                            String path = pathElem.getTextContent().trim();
                            paths.add(path);
                        }
                        CmsAlternativeSiteRootMapping mapping = new CmsAlternativeSiteRootMapping(
                            uri,
                            paths,
                            titlePrefix);
                        getDigester().peekParams()[14] = Optional.of(mapping);
                        super.end(namespace, name);
                    }

                });
        } catch (ParserConfigurationException e) {
            throw new RuntimeException(e);
        }
        digester.addCallMethod("*/" + N_SITES + "/" + N_SHARED_FOLDER, "setSharedFolder", 0);

    }

    /**
     * @see org.opencms.configuration.I_CmsXmlConfiguration#generateXml(org.dom4j.Element)
     */
    public Element generateXml(Element parent) {

        // create <sites> node
        Element sitesElement = parent.addElement(N_SITES);
        if (OpenCms.getRunLevel() >= OpenCms.RUNLEVEL_3_SHELL_ACCESS) {
            m_siteManager = OpenCms.getSiteManager();
        }
        Map<String, CmsSSLMode> workplaceMap = m_siteManager.getWorkplaceServersMap();
        for (String server : workplaceMap.keySet()) {
            Element workplaceElement = sitesElement.addElement(N_WORKPLACE_SERVER).addText(server);
            workplaceElement.addAttribute(A_SSL, workplaceMap.get(server).getXMLValue());
        }
        sitesElement.addElement(N_DEFAULT_URI).addText(m_siteManager.getDefaultUri());
        String sharedFolder = m_siteManager.getSharedFolder();
        if (sharedFolder != null) {
            sitesElement.addElement(N_SHARED_FOLDER).addText(sharedFolder);
        }
        String oldStyleSecureAllowed = String.valueOf(m_siteManager.isOldStyleSecureServerAllowed());
        sitesElement.addElement(N_OLD_STYLE_SECURE_SERVER).addText(oldStyleSecureAllowed);
        if (m_siteManager.isConfigurableWebServer()) {
            Element configServer = sitesElement.addElement(N_WEBSERVERSCRIPTING);
            Map<String, String> configServerMap = m_siteManager.getWebServerConfig();
            configServer.addElement(N_WEBSERVERSCRIPTING_WEBSERVERSCRIPT).addText(
                configServerMap.get(CmsSiteManagerImpl.WEB_SERVER_CONFIG_WEBSERVERSCRIPT));
            configServer.addElement(N_WEBSERVERSCRIPTING_TARGETPATH).addText(
                configServerMap.get(CmsSiteManagerImpl.WEB_SERVER_CONFIG_TARGETPATH));
            configServer.addElement(N_WEBSERVERSCRIPTING_CONFIGTEMPLATE).addText(
                configServerMap.get(CmsSiteManagerImpl.WEB_SERVER_CONFIG_CONFIGTEMPLATE));
            configServer.addElement(N_WEBSERVERSCRIPTING_SECURETEMPLATE).addText(
                configServerMap.get(CmsSiteManagerImpl.WEB_SERVER_CONFIG_SECURETEMPLATE));
            configServer.addElement(N_WEBSERVERSCRIPTING_FILENAMEPREFIX).addText(
                configServerMap.get(CmsSiteManagerImpl.WEB_SERVER_CONFIG_FILENAMEPREFIX));
            configServer.addElement(N_WEBSERVERSCRIPTING_LOGGINGDIR).addText(
                configServerMap.get(CmsSiteManagerImpl.WEB_SERVER_CONFIG_LOGGINGDIR));
        }
        Iterator<CmsSite> siteIterator = new HashSet<CmsSite>(m_siteManager.getSites().values()).iterator();
        while (siteIterator.hasNext()) {
            CmsSite site = siteIterator.next();
            // create <site server="" uri=""/> subnode(s)
            Element siteElement = sitesElement.addElement(N_SITE);

            siteElement.addAttribute(A_SERVER, site.getSiteMatcher().toString());
            siteElement.addAttribute(A_URI, site.getSiteRoot().concat("/"));
            siteElement.addAttribute(A_TITLE, site.getTitle());
            siteElement.addAttribute(A_POSITION, Float.toString(site.getPosition()));
            siteElement.addAttribute(A_ERROR_PAGE, site.getErrorPage());
            siteElement.addAttribute(A_WEBSERVER, String.valueOf(site.isWebserver()));
            siteElement.addAttribute(A_SSL, site.getSSLMode().getXMLValue());
            siteElement.addAttribute(A_SUBSITE_SELECTION, "" + site.isSubsiteSelectionEnabled());

            // create <secure server=""/> subnode
            if (site.hasSecureServer()) {
                Element secureElem = siteElement.addElement(N_SECURE);
                secureElem.addAttribute(A_SERVER, site.getSecureUrl());

                secureElem.addAttribute(A_EXCLUSIVE, String.valueOf(site.isExclusiveUrl()));
                secureElem.addAttribute(A_ERROR, String.valueOf(site.isExclusiveError()));
                if (site.usesPermanentRedirects()) {
                    secureElem.addAttribute(A_USE_PERMANENT_REDIRECTS, Boolean.TRUE.toString());
                }
            }

            if ((site.getParameters() != null) && !site.getParameters().isEmpty()) {
                Element parametersElem = siteElement.addElement(N_PARAMETERS);
                for (Map.Entry<String, String> entry : site.getParameters().entrySet()) {
                    Element paramElem = parametersElem.addElement(N_PARAM);
                    paramElem.addAttribute(A_NAME, entry.getKey());
                    paramElem.addText(entry.getValue());
                }
            }

            // create <alias server=""/> subnode(s)
            Iterator<CmsSiteMatcher> aliasIterator = site.getAliases().iterator();
            while (aliasIterator.hasNext()) {
                CmsSiteMatcher matcher = aliasIterator.next();
                Element aliasElement = siteElement.addElement(N_ALIAS);
                aliasElement.addAttribute(A_SERVER, matcher.getUrl());

                RedirectMode redirectMode = matcher.getRedirectMode();

                String redirectModeStr = null;
                switch (redirectMode) {

                    case permanent:
                        redirectModeStr = "permanent";
                        break;
                    case temporary:

                        redirectModeStr = "true";
                        break;
                    case none:
                    default:
                        redirectModeStr = "false";
                        break;
                }
                aliasElement.addAttribute(A_REDIRECT, redirectModeStr);

                if (matcher.getTimeOffset() != 0) {
                    aliasElement.addAttribute(A_OFFSET, "" + (matcher.getTimeOffset() / 1000));
                }
            }
            java.util.Optional<CmsAlternativeSiteRootMapping> altSiteRoot = site.getAlternativeSiteRootMapping();
            if (altSiteRoot.isPresent()) {
                altSiteRoot.get().appendXml(siteElement);
            }
        }
        return sitesElement;

    }

    /**
     * @see org.opencms.configuration.I_CmsXmlConfiguration#getDtdFilename()
     */
    public String getDtdFilename() {

        return CONFIGURATION_DTD_NAME;
    }

    /**
     * Returns the site manager.<p>
     *
     * @return the site manager
     */
    public CmsSiteManagerImpl getSiteManager() {

        return m_siteManager;
    }

    /**
     * @see org.opencms.configuration.I_CmsXmlConfigurationWithUpdateHandler#handleUpdate()
     */
    public synchronized void handleUpdate() throws Exception {

        CmsLetsEncryptConfiguration config = OpenCms.getLetsEncryptConfig();
        if ((config != null) && config.isValidAndEnabled() && (config.getTrigger() == Trigger.siteConfig)) {

            // the configuration may be written several times in quick succession. We want to update when this
            // happens for the last time, not the first, so we use a scheduled task.

            if (m_updateFuture != null) {
                m_updateFuture.cancel(false);
                m_updateFuture = null;
            }
            m_updateFuture = OpenCms.getExecutor().schedule(new Runnable() {

                @SuppressWarnings("synthetic-access")
                public void run() {

                    m_updateFuture = null;
                    CmsLogReport report = new CmsLogReport(
                        Locale.ENGLISH,
                        org.opencms.letsencrypt.CmsSiteConfigToLetsEncryptConfigConverter.class);
                    CmsSiteConfigToLetsEncryptConfigConverter converter = new CmsSiteConfigToLetsEncryptConfigConverter(
                        config);
                    converter.run(report, OpenCms.getSiteManager());

                    // TODO Auto-generated method stub

                }
            }, 5, TimeUnit.SECONDS);
        }

    }

    /**
     * @see org.opencms.configuration.I_CmsXmlConfigurationWithUpdateHandler#setCmsObject(org.opencms.file.CmsObject)
     */
    public void setCmsObject(CmsObject cms) {

        m_adminCms = cms;
    }

    /**
     * Sets the site manager.<p>
     *
     * @param siteManager the site manager to set
     */
    public void setSiteManager(CmsSiteManagerImpl siteManager) {

        m_siteManager = siteManager;
        if (CmsLog.INIT.isInfoEnabled()) {
            CmsLog.INIT.info(Messages.get().getBundle().key(Messages.INIT_SITE_CONFIG_FINISHED_0));
        }
    }

    /**
     * @see org.opencms.configuration.A_CmsXmlConfiguration#initMembers()
     */
    @Override
    protected void initMembers() {

        setXmlFileName(DEFAULT_XML_FILE_NAME);
    }
}
