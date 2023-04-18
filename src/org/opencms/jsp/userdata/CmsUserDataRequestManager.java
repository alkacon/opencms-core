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

package org.opencms.jsp.userdata;

import org.opencms.ade.detailpage.CmsDetailPageInfo;
import org.opencms.configuration.CmsParameterConfiguration;
import org.opencms.configuration.I_CmsXmlConfiguration;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsResource;
import org.opencms.file.CmsUser;
import org.opencms.file.CmsUserSearchParameters;
import org.opencms.file.CmsUserSearchParameters.SortKey;
import org.opencms.jsp.util.CmsJspStandardContextBean;
import org.opencms.mail.CmsHtmlMail;
import org.opencms.main.CmsException;
import org.opencms.main.CmsLog;
import org.opencms.main.OpenCms;
import org.opencms.report.I_CmsReport;
import org.opencms.security.CmsRole;
import org.opencms.util.CmsStringUtil;
import org.opencms.xml.content.CmsXmlContent;
import org.opencms.xml.content.CmsXmlContentFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.ServiceLoader;
import java.util.stream.Collectors;

import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;

import org.apache.commons.digester3.Digester;
import org.apache.commons.digester3.Rule;
import org.apache.commons.logging.Log;
import org.apache.commons.mail.EmailException;

import org.dom4j.Element;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.xml.sax.Attributes;

/**
 * Manager class for user data requests.<p>
 *
 * Users can request their data either via username/password, in which case the user data will be collected for that user,
 * or by email, in which case the data for all users with the given email address is collected.
 * <p>
 * User data is formatted as HTML by one or more configurable 'user data domain' classes, which implement the
 * I_CmsUserDataDomain interface.
 *
 * <p>After the user requests their data, they are sent an email with a link (which is only valid for a certain time). When
 * opening that link, they have to confirm the credentials they requested the data with, and if they successfully do so,
 * are shown a page with their user data.
 */
public class CmsUserDataRequestManager {

    /** The name that must be used for the function detail pages for user data requests. */
    public static final String FUNCTION_NAME = "userdatarequest";

    /** Tag name for the user data request manager. */
    public static final String N_USERDATA = "userdata";

    /** Attribute to enable/disable autoloading of plugins via service loader.*/
    public static final String A_AUTOLOAD = "autoload";

    /** Tag name for the user data domain. */
    public static final String N_USERDATA_DOMAIN = "userdata-domain";

    /** Logger for this class. */
    private static final Log LOG = CmsLog.getLog(CmsUserDataRequestManager.class);

    /** A CmsObject with admin privileges. */
    private CmsObject m_adminCms;

    /** The configured user data domains. */
    private List<I_CmsUserDataDomain> m_configuredDomains = new ArrayList<>();

    /** The store used to load/save user data requests. */
    private CmsUserDataRequestStore m_requestStore = new CmsUserDataRequestStore();

    /** If true, additional plugins should be loaded via service loader. */
    private boolean m_autoload;

    /** List of all domains, both the ones from the configuration and the ones loaded via service loader. */
    private List<I_CmsUserDataDomain> m_allDomains = new ArrayList<>();

    /**
     * Adds digester rules for configuration.
     *
     * @param digester the digester
     * @param basePath the base xpath for the configuration of user data requests
     */
    public static void addDigesterRules(Digester digester, String basePath) {

        digester.addObjectCreate(basePath, CmsUserDataRequestManager.class);
        digester.addRule(basePath, new Rule() {

            private boolean m_autoload;

            @Override
            public void begin(String namespace, String name, Attributes attributes) throws Exception {

                m_autoload = Boolean.parseBoolean(attributes.getValue(A_AUTOLOAD));
            }

            @Override
            public void end(String namespace, String name) throws Exception {

                CmsUserDataRequestManager manager = digester.peek();
                manager.setAutoload(m_autoload);
            }

        });
        String domainPath = basePath + "/" + N_USERDATA_DOMAIN;
        digester.addObjectCreate(domainPath, null, I_CmsXmlConfiguration.A_CLASS);
        digester.addSetNext(domainPath, "addUserDataDomain");
        // use pre-existing rules for params in system configuration to set the parameters for the user domain
    }

    /**
     * Gets all users with a given email address (maximum of 999).
     *
     * @param cms the CMS context
     * @param email the email address
     * @return the list of users
     * @throws CmsException if something goes wrong
     */
    public static List<CmsUser> getUsersByEmail(CmsObject cms, String email) throws CmsException {

        if (CmsStringUtil.isEmptyOrWhitespaceOnly(email)) {
            // we don't want to find the users with no email address!
            return new ArrayList<>();
        }
        CmsUserSearchParameters params = new CmsUserSearchParameters();
        params.setPaging(9999, 1);
        params.setFilterEmail(email);
        params.setSorting(SortKey.email, true);
        List<CmsUser> users = OpenCms.getOrgUnitManager().searchUsers(cms, params);
        return users;
    }

    /**
     * Adds a user data domain during configuration phase.
     *
     * @param domain the user data domain to add
     */
    public void addUserDataDomain(I_CmsUserDataDomain domain) {

        checkNotInitialized();
        m_configuredDomains.add(domain);
    }

    /**
     * Writes the configuration back to XML.
     *
     * @param element the parent element
     */
    public void appendToXml(Element element) {

        Element root = element.addElement(N_USERDATA);
        if (isAutoload()) {
            root.addAttribute(A_AUTOLOAD, "true");
        }
        for (I_CmsUserDataDomain domain : m_configuredDomains) {
            String clsName = domain.getClass().getName();
            Element domainElem = root.addElement(N_USERDATA_DOMAIN);
            domainElem.addAttribute(I_CmsXmlConfiguration.A_CLASS, clsName);
            CmsParameterConfiguration config = domain.getConfiguration();
            config.appendToXml(domainElem);
        }
    }

    /**
     * Checks that the manager has not already been initialized, and throws an exception otherwise.
     */
    public void checkNotInitialized() {

        if (m_adminCms != null) {
            throw new IllegalStateException("CmsUserDataRequestManager already initialized.");
        }
    }

    /**
     * Gets the list of all user data domains, both those from the configuration and those loaded via service loader.
     *
     * @return the list of all user data domains
     */
    public List<I_CmsUserDataDomain> getAllDomains() {

        return m_allDomains;
    }

    /**
     * Gets the user data for an email address.
     *
     * <p>Only callable by root admin users.
     *
     * @param cms the CMS context
     * @param mode the mode
     * @param email the email address (may be null)
     * @param searchStrings a list of additional search strings entered by the user
     * @param root the root element to which the report should be added
     * @param report the report to write to
     * @return true if the HTML document was changed as a result of executing this method
     *
     * @throws CmsException if something goes wrong
     */
    public boolean getInfoForEmail(
        CmsObject cms,
        I_CmsUserDataDomain.Mode mode,
        String email,
        List<String> searchStrings,
        org.jsoup.nodes.Element root,
        I_CmsReport report)
    throws CmsException {

        OpenCms.getRoleManager().checkRole(cms, CmsRole.ROOT_ADMIN);
        return internalGetInfoForEmail(cms, mode, email, searchStrings, root, report);
    }

    /**
     * Gets the user data for a specific OpenCms user.
     *
     * <p>Only callable by root admin users.
     *
     * @param cms the CMS context
     * @param mode the mode
     * @param user the OpenCms user
     * @param root the root element to which the report should be added
     * @param report the report to write to
     * @return true if the HTML document was changed as a result of executing this method
     * @throws CmsException if something goes wrong
     *
     */
    public boolean getInfoForUser(
        CmsObject cms,
        I_CmsUserDataDomain.Mode mode,
        CmsUser user,
        org.jsoup.nodes.Element root,
        I_CmsReport report)
    throws CmsException {

        OpenCms.getRoleManager().checkRole(cms, CmsRole.ROOT_ADMIN);
        return internalGetInfoForUser(cms, mode, user, root, report);
    }

    /**
     * Gets the user data request store.
     *
     * @return the user data request store
     */
    public CmsUserDataRequestStore getRequestStore() {

        return m_requestStore;
    }

    /**
     * Initializes the manager.
     *
     * @param cms a CMS context with admin privileges
     */
    public void initialize(CmsObject cms) {

        checkNotInitialized();
        m_adminCms = cms;
        m_allDomains.addAll(m_configuredDomains);
        if (m_autoload) {
            m_allDomains.addAll(loadUserDataDomainsFromClasses());
        }
        for (I_CmsUserDataDomain domain : m_allDomains) {
            domain.initialize(cms);
        }
        m_requestStore.initialize(cms);
    }

    /**
     * Loads the user data request configuration from the given file.
     *
     * <p>Returns null if no configuration is found.
     *
     * @param cms the CMS context
     * @param path the site path of the configuration
     * @return the configuration for the given path
     */
    public Optional<CmsUserDataRequestConfig> loadConfig(CmsObject cms, String path) {

        LOG.debug("loading user data request config for path " + path);
        if (path == null) {
            LOG.info("path is null");
            return Optional.empty();
        }
        try {
            CmsResource resource = cms.readResource(path);
            CmsXmlContent content = CmsXmlContentFactory.unmarshal(cms, cms.readFile(resource));
            CmsUserDataRequestConfig result = new CmsUserDataRequestConfig(
                cms,
                content,
                cms.getRequestContext().getLocale());
            return Optional.of(result);
        } catch (CmsException e) {
            LOG.error(e.getLocalizedMessage(), e);
            return Optional.empty();
        }
    }

    /**
     * Starts a user data request for the single user case (with user name and password).<p>
     *
     * @param cms the CMS context
     * @param config the configuration
     * @param user the user for which the data was requested
     *
     * @throws AddressException if parsing the email address fails
     * @throws EmailException if sending the email fails
     */
    public void startUserDataRequest(CmsObject cms, CmsUserDataRequestConfig config, CmsUser user)
    throws AddressException, EmailException {

        Document doc = Jsoup.parseBodyFragment("");
        org.jsoup.nodes.Element root = doc.body().appendElement("div");
        CmsUserDataRequestInfo info = new CmsUserDataRequestInfo();
        info.setUser(user.getName());
        info.setType(CmsUserDataRequestType.singleUser);
        info.setEmail(user.getEmail());
        info.setExpiration(System.currentTimeMillis() + config.getRequestLifetime());
        for (I_CmsUserDataDomain userDomain : getAllDomains()) {
            if (userDomain.matchesUser(cms, CmsUserDataRequestType.singleUser, user)) {
                userDomain.appendInfoHtml(
                    cms,
                    CmsUserDataRequestType.singleUser,
                    Collections.singletonList(user),
                    root);
            }
        }
        info.setInfoHtml(root.toString());
        m_requestStore.save(info);
        sendMail(cms, config, user.getEmail(), info.getId());
    }

    /**
     * Starts a user data request for the email case.
     *
     * @param cms the CMS context
     * @param config the user data request configuration
     * @param email the email address
     * @throws CmsUserDataRequestException if something goes wrong
     * @throws EmailException if sending the email fails
     * @throws AddressException if parsing the email address fails
     */
    public void startUserDataRequest(CmsObject cms, CmsUserDataRequestConfig config, String email)
    throws CmsUserDataRequestException, EmailException, AddressException {

        if (CmsStringUtil.isEmptyOrWhitespaceOnly(email)) {
            throw new IllegalArgumentException("Can not use empty email address for user data request by email.");
        }
        try {
            List<CmsUser> users = getUsersByEmail(m_adminCms, email);
            Document doc = Jsoup.parseBodyFragment("");
            org.jsoup.nodes.Element root = doc.body().appendElement("div");

            boolean foundDomain = false;
            for (I_CmsUserDataDomain userDomain : getAllDomains()) {
                List<CmsUser> usersForDomain = new ArrayList<>();
                for (CmsUser user : users) {
                    if (userDomain.matchesUser(cms, CmsUserDataRequestType.email, user)) {
                        usersForDomain.add(user);
                        foundDomain = true;
                    }
                }
                if (!usersForDomain.isEmpty()) {
                    userDomain.appendInfoHtml(cms, CmsUserDataRequestType.email, usersForDomain, root);
                }
            }
            if (!foundDomain) {
                throw new CmsUserDataRequestException("no users found with the given email address.");
            }
            CmsUserDataRequestInfo info = new CmsUserDataRequestInfo();
            info.setType(CmsUserDataRequestType.email);
            info.setEmail(email);
            info.setInfoHtml(root.toString());
            info.setExpiration(System.currentTimeMillis() + config.getRequestLifetime());
            m_requestStore.save(info);
            sendMail(cms, config, email, info.getId());
        } catch (CmsException e) {
            throw new CmsUserDataRequestException(e);
        }

    }

    /**
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {

        String collectorsStr = getAllDomains().stream().map(domain -> domain.toString()).collect(
            Collectors.joining(", "));
        return getClass().getName() + "[" + collectorsStr + "]";
    }

    /**
     * Sets the autoload flag, which enables automatic loading of plugins via service loader.
     *
     * @param autoload the value of the autoload flag
     */
    protected void setAutoload(boolean autoload) {

        checkNotInitialized();
        m_autoload = autoload;
    }

    /**
     * Internal helper method for getting the user data for an email address.
     *
     * @param cms the CMS context
     * @param mode the mode
     * @param email the email address
     * @param searchStrings an additional list of search strings entered by the user
     * @param root the root element to which the report should be added
     * @param report the report to write to
     * @return true if the HTML document was changed as a result of executing this method
     * @throws CmsException if something goes wrong
     */
    private boolean internalGetInfoForEmail(
        CmsObject cms,
        I_CmsUserDataDomain.Mode mode,
        String email,
        List<String> searchStrings,
        org.jsoup.nodes.Element root,
        I_CmsReport report)
    throws CmsException {

        Document doc = root.ownerDocument();
        String oldHtml = doc.toString();

        List<CmsUser> users = getUsersByEmail(m_adminCms, email);
        boolean foundDomain = false;
        int i = 0;
        for (I_CmsUserDataDomain userDomain : getAllDomains()) {
            i += 1;
            report.print(
                Messages.get().container(Messages.RPT_USERDATADOMAIN_COUNT_2, "" + i, "" + getAllDomains().size()));
            report.print(
                org.opencms.report.Messages.get().container(org.opencms.report.Messages.RPT_DOTS_0),
                I_CmsReport.FORMAT_DEFAULT);
            if (!userDomain.isAvailableForMode(mode)) {
                report.println(
                    org.opencms.report.Messages.get().container(org.opencms.report.Messages.RPT_SKIPPED_0),
                    I_CmsReport.FORMAT_DEFAULT);
                continue;
            }
            List<CmsUser> usersForDomain = new ArrayList<>();
            for (CmsUser user : users) {
                if (userDomain.matchesUser(cms, CmsUserDataRequestType.email, user)) {
                    usersForDomain.add(user);
                    foundDomain = true;
                }
            }
            if (!usersForDomain.isEmpty()) {
                userDomain.appendInfoHtml(cms, CmsUserDataRequestType.email, usersForDomain, root);
            }
            userDomain.appendlInfoForEmail(cms, email, searchStrings, root);
            report.println(
                org.opencms.report.Messages.get().container(org.opencms.report.Messages.RPT_OK_0),
                I_CmsReport.FORMAT_OK);
        }
        String newHtml = doc.toString();
        boolean changed = !(newHtml.equals(oldHtml));
        return changed;
    }

    /**
     * Internal helper method for getting the user data for a specific OpenCms user.
     *
     * @param cms the CMS context
     * @param mode the mode
     * @param user the OpenCms user
     * @param root the root element to which the report should be added
     * @param report the report
     * @return true if the HTML document was changed as a result of executing this method
     *
     */
    private boolean internalGetInfoForUser(
        CmsObject cms,
        I_CmsUserDataDomain.Mode mode,
        CmsUser user,
        org.jsoup.nodes.Element root,
        I_CmsReport report) {

        Document doc = root.ownerDocument();
        String oldHtml = doc.toString();
        int i = 0;
        for (I_CmsUserDataDomain userDomain : getAllDomains()) {
            i += 1;
            report.print(
                Messages.get().container(Messages.RPT_USERDATADOMAIN_COUNT_2, "" + i, "" + getAllDomains().size()));
            report.print(
                org.opencms.report.Messages.get().container(org.opencms.report.Messages.RPT_DOTS_0),
                I_CmsReport.FORMAT_DEFAULT);
            if (!userDomain.isAvailableForMode(mode)) {
                report.println(
                    org.opencms.report.Messages.get().container(org.opencms.report.Messages.RPT_SKIPPED_0),
                    I_CmsReport.FORMAT_DEFAULT);
                continue;
            }
            List<CmsUser> usersForDomain = new ArrayList<>();
            if (userDomain.matchesUser(cms, CmsUserDataRequestType.email, user)) {
                usersForDomain.add(user);
            }
            if (!usersForDomain.isEmpty()) {
                userDomain.appendInfoHtml(cms, CmsUserDataRequestType.email, usersForDomain, root);
            }
            report.println(
                org.opencms.report.Messages.get().container(org.opencms.report.Messages.RPT_OK_0),
                I_CmsReport.FORMAT_OK);
        }
        String newHtml = doc.toString();
        boolean changed = !(newHtml.equals(oldHtml));
        return changed;
    }

    /**
     * Returns true if the autoload flag is enabled.
     *
     * @return true if the autoload flag is enabled
     */
    private boolean isAutoload() {

        return m_autoload;
    }

    /**
     * Loads additional plugins via service loader.
     *
     * @return the additional plugins loaded
     */
    private List<I_CmsUserDataDomain> loadUserDataDomainsFromClasses() {

        List<I_CmsUserDataDomain> result = new ArrayList<>();
        ServiceLoader<I_CmsUserDataDomainProvider> loader = ServiceLoader.load(I_CmsUserDataDomainProvider.class);
        for (I_CmsUserDataDomainProvider provider : loader) {
            for (I_CmsUserDataDomain domain : provider.getUserDataDomains()) {
                result.add(domain);
            }
        }
        return result;
    }

    /**
     * Sends the user data request mail to the given email address.
     *
     * @param cms the CMS context
     * @param config the configuration
     * @param email the email address
     * @param id the user data request id
     * @throws EmailException if sending the email fails
     * @throws AddressException if parsing the address fails
     */
    private void sendMail(CmsObject cms, CmsUserDataRequestConfig config, String email, String id)
    throws EmailException, AddressException {

        CmsHtmlMail mail = new CmsHtmlMail();
        mail.setCharset("UTF-8");

        mail.setSubject(config.getMailSubject());
        mail.setTo(Arrays.asList(InternetAddress.parse(email)));
        String link = CmsJspStandardContextBean.getFunctionDetailLink(
            cms,
            CmsDetailPageInfo.FUNCTION_PREFIX,
            FUNCTION_NAME,
            true)
            + "?"
            + CmsJspUserDataRequestBean.PARAM_UDRID
            + "="
            + id
            + "&"
            + CmsJspUserDataRequestBean.PARAM_ACTION
            + "="
            + CmsJspUserDataRequestBean.ACTION_VIEW;
        mail.setHtmlMsg(config.getMailText() + "<a href=\"" + link + "\">" + link + "</a>");
        mail.send();

    }
}
