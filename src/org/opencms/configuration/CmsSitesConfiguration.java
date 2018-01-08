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

import org.opencms.main.CmsLog;
import org.opencms.main.OpenCms;
import org.opencms.site.CmsSite;
import org.opencms.site.CmsSiteManagerImpl;
import org.opencms.site.CmsSiteMatcher;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;

import org.apache.commons.digester.Digester;

import org.dom4j.Element;

/**
 * Class to read and write the OpenCms site configuration.<p>
 */
public class CmsSitesConfiguration extends A_CmsXmlConfiguration {

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

    /** The "server" attribute. */
    public static final String A_SERVER = "server";

    /** The "title" attribute. */
    public static final String A_TITLE = "title";

    /** The ssl mode attribute.*/
    public static final String A_SSL = "sslmode";

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

    /** The node name for the parameters. */
    public static final String N_PARAMETERS = "parameters";

    /** The node name for the secure site. */
    public static final String N_SECURE = "secure";

    /** Shared folder node name. */
    public static final String N_SHARED_FOLDER = "shared-folder";

    /** New secure modes node. */
    public static final String N_OLD_STYLE_SECURE_SERVER = "oldStyleSecureServer";

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

    /** The configured site manager. */
    private CmsSiteManagerImpl m_siteManager;

    /**
     * @see org.opencms.configuration.I_CmsXmlConfiguration#addXmlDigesterRules(org.apache.commons.digester.Digester)
     */
    public void addXmlDigesterRules(Digester digester) {

        // add site configuration rule
        digester.addObjectCreate("*/" + N_SITES, CmsSiteManagerImpl.class);
        digester.addCallMethod("*/" + N_SITES + "/" + N_WORKPLACE_SERVER, "addWorkplaceServer", 0);
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

        digester.addCallMethod(siteXpath, "addSite", 11);
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
        digester.addCallMethod(siteXpath + "/" + N_PARAMETERS + "/" + N_PARAM, "addParamToConfigSite", 2);
        digester.addCallParam(siteXpath + "/" + N_PARAMETERS + "/" + N_PARAM, 0, A_NAME);
        digester.addCallParam(siteXpath + "/" + N_PARAMETERS + "/" + N_PARAM, 1);
        // add an alias to the currently configured site
        digester.addCallMethod("*/" + N_SITES + "/" + N_SITE + "/" + N_ALIAS, "addAliasToConfigSite", 2);
        digester.addCallParam("*/" + N_SITES + "/" + N_SITE + "/" + N_ALIAS, 0, A_SERVER);
        digester.addCallParam("*/" + N_SITES + "/" + N_SITE + "/" + N_ALIAS, 1, A_OFFSET);

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
        for (String server : m_siteManager.getWorkplaceServers()) {
            sitesElement.addElement(N_WORKPLACE_SERVER).addText(server);
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
                if (matcher.getTimeOffset() != 0) {
                    aliasElement.addAttribute(A_OFFSET, "" + matcher.getTimeOffset());
                }
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
