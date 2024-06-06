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
 * For further information about Alkacon Software GmbH & Co. KG, please see the
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

import org.opencms.ade.detailpage.CmsDefaultDetailPageHandler;
import org.opencms.ade.detailpage.I_CmsDetailPageHandler;
import org.opencms.crypto.CmsAESTextEncryption;
import org.opencms.crypto.I_CmsTextEncryption;
import org.opencms.db.CmsCacheSettings;
import org.opencms.db.CmsDefaultUsers;
import org.opencms.db.CmsLoginManager;
import org.opencms.db.CmsSubscriptionManager;
import org.opencms.db.I_CmsDbContextFactory;
import org.opencms.flex.CmsFlexCacheConfiguration;
import org.opencms.i18n.CmsLocaleManager;
import org.opencms.jsp.userdata.CmsUserDataRequestManager;
import org.opencms.letsencrypt.CmsLetsEncryptConfiguration;
import org.opencms.mail.CmsMailHost;
import org.opencms.mail.CmsMailSettings;
import org.opencms.main.CmsDefaultSessionStorageProvider;
import org.opencms.main.CmsEventManager;
import org.opencms.main.CmsHttpAuthenticationSettings;
import org.opencms.main.CmsLog;
import org.opencms.main.CmsServletContainerSettings;
import org.opencms.main.I_CmsRequestHandler;
import org.opencms.main.I_CmsResourceInit;
import org.opencms.main.I_CmsSessionStorageProvider;
import org.opencms.main.OpenCms;
import org.opencms.monitor.CmsMemoryMonitorConfiguration;
import org.opencms.publish.CmsPublishManager;
import org.opencms.rmi.CmsRemoteShellConstants;
import org.opencms.security.CmsDefaultAuthorizationHandler;
import org.opencms.security.CmsDefaultCredentialsResolver;
import org.opencms.security.CmsDefaultValidationHandler;
import org.opencms.security.I_CmsAuthorizationHandler;
import org.opencms.security.I_CmsCredentialsResolver;
import org.opencms.security.I_CmsPasswordHandler;
import org.opencms.security.I_CmsValidationHandler;
import org.opencms.security.twofactor.CmsTwoFactorAuthenticationConfig;
import org.opencms.util.CmsStringUtil;
import org.opencms.workflow.CmsDefaultWorkflowManager;
import org.opencms.workflow.I_CmsWorkflowManager;
import org.opencms.xml.CmsXmlUtils;
import org.opencms.xml.containerpage.CmsADECacheSettings;
import org.opencms.xml.xml2json.I_CmsApiAuthorizationHandler;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.digester3.Digester;
import org.apache.commons.digester3.NodeCreateRule;
import org.apache.commons.digester3.Rule;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;

import org.dom4j.Element;
import org.w3c.dom.Document;
import org.xml.sax.Attributes;

/**
 * System master configuration class.<p>
 *
 * @since 6.0.0
 */
public class CmsSystemConfiguration extends A_CmsXmlConfiguration {

    /**
     * Data for creating API authorization handlers.
     */
    public static class ApiAuthorizationConfig {

        /** The class name. */
        private String m_class;

        /** The name. */
        private String m_name;

        /** The parameters. */
        private CmsParameterConfiguration m_params = new CmsParameterConfiguration();

        /**
         * Writes the data back to the given XML element.
         *
         * @param element an XML element
         */
        public void fillXml(Element element) {

            element.addElement("name").addText(getName());
            element.addElement("class").addText(getClassName());
            for (Map.Entry<String, String> entry : m_params.entrySet()) {
                Element paramElem = element.addElement("param");
                paramElem.addAttribute("name", entry.getKey());
                paramElem.addText(entry.getValue());
            }
        }

        /**
         * Gets the API authorization class name.
         *
         * @return the class name
         */
        public String getClassName() {

            return m_class;
        }

        /**
         * Gets the identifier for the authorization handler.
         *
         * @return the identifier for the handler
         */
        public String getName() {

            return m_name;
        }

        /**
         * Gets the parameters for the handler.
         *
         * @return the parameters for the handler
         */
        public CmsParameterConfiguration getParams() {

            return m_params;
        }

        /**
         * Sets the class name.
         *
         * @param class1 the class name
         */
        public void setClassName(String class1) {

            m_class = class1;
        }

        /**
         * Sets the name of the handler.
         *
         * @param name the handler name
         */
        public void setName(String name) {

            m_name = name;
        }

        /**
        * Sets one parameter.
        *
        * @param key the parameter name
        * @param value the parameter value
        */
        public void setParam(String key, String value) {

            m_params.put(key, value);
        }

    }

    /** Enum for the user session mode. */
    public enum UserSessionMode {
        /** Only a single session per user is allowed. */
        single,
        /** Any number of sessions for a user are allowed. */
        standard
    }

    /** The attribute name for the deleted node. */
    public static final String A_DELETED = "deleted";

    /** The "error" attribute. */
    public static final String A_ERROR = "error";

    /** The "errorPage" attribute. */
    public static final String A_ERROR_PAGE = "errorPage";

    /** The "id" attribute. */
    public static final String A_ID = "id";

    /** The "exclusive" attribute. */
    public static final String A_EXCLUSIVE = "exclusive";

    /** The "mailfrom" attribute. */
    public static final String A_MAILFROM = "mailfrom";

    /** The attribute name for the localization mode. */
    public static final String A_LOCALIZATION_MODE = "localizationMode";

    /** The "maxvisited" attribute. */
    public static final String A_MAXVISITED = "maxvisited";

    /** The "offline" attribute. */
    public static final String A_OFFLINE = "offline";

    /** The "online" attribute. */
    public static final String A_ONLINE = "online";
    /** The "poolname" attribute. */
    public static final String A_POOLNAME = "poolname";
    /** The "security" attribute. */
    public static final String A_SECURITY = "security";

    /** The name of the DTD for this configuration. */
    public static final String CONFIGURATION_DTD_NAME = "opencms-system.dtd";
    /** The default user session mode. */
    public static final UserSessionMode DEFAULT_USER_SESSION_MODE = UserSessionMode.standard;
    /** The name of the default XML file for this configuration. */
    public static final String DEFAULT_XML_FILE_NAME = "opencms-system.xml";

    /** The ade node name. */
    public static final String N_ADE = "ade";

    /** The ade-cache node name. */
    public static final String N_ADE_CACHE = "ade-cache";

    /** Node name for a single API authorization handler. */
    public static final String N_API_AUTHORIZATION = "api-authorization";

    /** Node name for the group of API authorization handlers. */
    public static final String N_API_AUTHORIZATIONS = "api-authorizations";

    /** The node name for the authorization handler. */
    public static final String N_AUTHORIZATIONHANDLER = "authorizationhandler";

    /** The node name for the avgcachebytes node. */
    public static final String N_AVGCACHEBYTES = "avgcachebytes";

    /** The node name for the browser-based node. */
    public static final String N_BROWSER_BASED = "browser-based";

    /** The node name for the cache-enabled node. */
    public static final String N_CACHE_ENABLED = "cache-enabled";

    /** The node name for the cache-offline node. */
    public static final String N_CACHE_OFFLINE = "cache-offline";

    /** The node name for a job class. */
    public static final String N_CLASS = "class";

    /** The configuration node name. */
    public static final String N_CONFIGURATION = "configuration";

    /** The containerpages node name. */
    public static final String N_CONTAINERPAGES = "containerpages";

    /** The duration after which responsible resource owners will be notified about out-dated content. */
    public static final String N_CONTENT_NOTIFICATION = "content-notification";

    /** The node name for the defaultcontentencoding node. */
    public static final String N_DEFAULT_CONTENT_ENCODING = "defaultcontentencoding";

    /** The node name for the defaultusers expression. */
    public static final String N_DEFAULTUSERS = "defaultusers";

    /** The node name for the detail page handler. */
    public static final String N_DETAIL_PAGE_HANDLER = "detail-page-handler";

    /** The node name for the device selector node. */
    public static final String N_DEVICESELECTOR = "device-selector";

    /** The node name for the digest type. */
    public static final String N_DIGESTTYPE = "digest-type";

    /** The node name for the login account lock minutes.  */
    public static final String N_DISABLEMINUTES = "disableMinutes";

    /** The node name for the sitemap cache for documents. */
    public static final String N_DOCUMENTS = "documents";

    /** The node name for the email-interval node. */
    public static final String N_EMAIL_INTERVAL = "email-interval";

    /** The node name for the email-receiver node. */
    public static final String N_EMAIL_RECEIVER = "email-receiver";

    /** The node name for the email-sender node. */
    public static final String N_EMAIL_SENDER = "email-sender";

    /** The node name for the login security option enabled flag. */
    public static final String N_ENABLESCURITY = "enableSecurity";

    /** Node name for the encryption section. */
    public static final String N_ENCRYPTION = "encryption";

    /** The node name for the request handler classes. */
    public static final String N_EVENTMANAGER = "eventmanager";

    /** The node name for the events node. */
    public static final String N_EVENTS = "events";

    /** The node name for the flexcache node. */
    public static final String N_FLEXCACHE = "flexcache";

    /** The node name for the form-based node. */
    public static final String N_FORM_BASED = "form-based";

    /** The node name for the group-administrators node. */
    public static final String N_GROUP_ADMINISTRATORS = "group-administrators";

    /** The node name for the group-guests node. */
    public static final String N_GROUP_GUESTS = "group-guests";

    /** The node name for the group-projectmanagers node. */
    public static final String N_GROUP_PROJECTMANAGERS = "group-projectmanagers";

    /** The node name for the group-users node. */
    public static final String N_GROUP_USERS = "group-users";

    /** The groupcontainers node name. */
    public static final String N_GROUPCONTAINERS = "groupcontainers";

    /** The node name for the publish "history-size" value. */
    public static final String N_HISTORYSIZE = "history-size";

    /** The node name for the http-authentication node. */
    public static final String N_HTTP_AUTHENTICATION = "http-authentication";

    /** The node name for the internationalization node. */
    public static final String N_I18N = "internationalization";

    /** The name of the class to generate cache keys. */
    public static final String N_KEYGENERATOR = "keygenerator";

    /** The node name for individual locales. */
    public static final String N_LOCALE = "locale";

    /** The node name for the locale handler. */
    public static final String N_LOCALEHANDLER = "localehandler";

    /** The node name for the configured locales. */
    public static final String N_LOCALESCONFIGURED = "localesconfigured";

    /** The node name for the default locale(s). */
    public static final String N_LOCALESDEFAULT = "localesdefault";

    /** The node name for the log-interval node. */
    public static final String N_LOG_INTERVAL = "log-interval";

    /** The node name for the login manager. */
    public static final String N_LOGINMANAGER = "loginmanager";

    /** Node name for the logout URI.*/
    public static final String N_LOGOUT_URI = "logoutUri";

    /** The node name for the mail configuration. */
    public static final String N_MAIL = "mail";

    /** The node name for the "mail from" node. */
    public static final String N_MAILFROM = "mailfrom";

    /** The node name for the "mail host" node. */
    public static final String N_MAILHOST = "mailhost";

    /** The node name for the login manager bad attempt count. */
    public static final String N_MAXBADATTEMPTS = "maxBadAttempts";

    /** The node name for the maxcachebytes node. */
    public static final String N_MAXCACHEBYTES = "maxcachebytes";

    /** The node name for the maxentrybytes node. */
    public static final String N_MAXENTRYBYTES = "maxentrybytes";

    /** The node name for the maxkeys node. */
    public static final String N_MAXKEYS = "maxkeys";

    /** The node name for the maxusagepercent node. */
    public static final String N_MAXUSAGE_PERCENT = "maxusagepercent";

    /** The node name for the memorymonitor node. */
    public static final String N_MEMORYMONITOR = "memorymonitor";

    /** The duration after which responsibles will be notified about out-dated content. */
    public static final String N_NOTIFICATION_PROJECT = "notification-project";

    /** The duration after which responsibles will be notified about out-dated content. */
    public static final String N_NOTIFICATION_TIME = "notification-time";

    /** The node name for the parameters. */
    public static final String N_PARAMETERS = "parameters";

    /** Node name for the password change interval. */
    public static final String N_PASSWORD_CHANGE_INTERVAL = "passwordChangeInterval";

    /** The node name for the password encoding. */
    public static final String N_PASSWORDENCODING = "encoding";

    /** The node name for the password handler. */
    public static final String N_PASSWORDHANDLER = "passwordhandler";

    /** The node name for the permission handler. */
    public static final String N_PERMISSIONHANDLER = "permissionhandler";

    /** The node name for the prevent-response-flush node. */
    public static final String N_PREVENTRESPONSEFLUSH = "prevent-response-flush";

    /** The node name for the publish list remove mode. */
    public static final String N_PUBLISH_LIST_REMOVE_MODE = "publish-list-remove-mode";

    /** The node name for the "publishhistory" section. */
    public static final String N_PUBLISHMANAGER = "publishmanager";

    /** The node name for the "publishhistory" section. */
    public static final String N_QUEUEPERSISTANCE = "queue-persistance";

    /** The node name for the "publishhistory" section. */
    public static final String N_QUEUESHUTDOWNTIME = "queue-shutdowntime";

    /** The node name for the memory email receiver. */
    public static final String N_RECEIVER = "receiver";

    /** The node name for the release-tags-after-end node. */
    public static final String N_RELEASETAGSAFTEREND = "release-tags-after-end";

    /** The node name for the request-error-page-attribute node. */
    public static final String N_REQUESTERRORPAGEATTRIBUTE = "request-error-page-attribute";

    /** The node name for the request handler classes. */
    public static final String N_REQUESTHANDLER = "requesthandler";

    /** The node name for the request handlers. */
    public static final String N_REQUESTHANDLERS = "requesthandlers";

    /** The node name for the resource init classes. */
    public static final String N_RESOURCEINIT = "resourceinit";

    /** The node name for the resource init classes. */
    public static final String N_RESOURCEINITHANDLER = "resourceinithandler";

    /** Node name for the restrict-detail-contents option. */
    public static final String N_RESTRICT_DETAIL_CONTENTS = "restrict-detail-contents";

    /** the result cache node. */
    public static final String N_RESULTCACHE = "resultcache";

    /** The node name for the runtime info. */
    public static final String N_RUNTIMECLASSES = "runtimeclasses";

    /** The node name for the runtime info factory. */
    public static final String N_RUNTIMEINFO = "runtimeinfo";

    /** The node name for the runtime properties node. */
    public static final String N_RUNTIMEPROPERTIES = "runtimeproperties";

    /** The node name for the sax-impl-system-properties node. */
    public static final String N_SAX_IMPL_SYSTEM_PROPERTIES = "sax-impl-system-properties";

    /** The node name for the servlet container settings. */
    public static final String N_SERVLETCONTAINERSETTINGS = "servletcontainer-settings";

    /** The node name for the session-storageprovider node. */
    public static final String N_SESSION_STORAGEPROVIDER = "session-storageprovider";

    /** The sitemap node name. */
    public static final String N_SITEMAP = "sitemap";

    /** The sitemap-cache node name. */
    public static final String N_SITEMAP_CACHE = "sitemap-cache";

    /** The size of the memory monitor's cache for ACLS. */
    public static final String N_SIZE_ACLS = "size-accesscontrollists";

    /** The size of the memory monitor's cache for offline container pages. */
    public static final String N_SIZE_CONTAINERPAGE_OFFLINE = "size-containerpage-offline";

    /** The size of the memory monitor's cache for online container pages. */
    public static final String N_SIZE_CONTAINERPAGE_ONLINE = "size-containerpage-online";

    /** The size of the memory monitor's cache for groups. */
    public static final String N_SIZE_GROUPS = "size-groups";

    /** The size of the memory monitor's cache for organizational units. */
    public static final String N_SIZE_ORGUNITS = "size-orgunits";

    /** The size of the memory monitor's cache for permission checks. */
    public static final String N_SIZE_PERMISSIONS = "size-permissions";

    /** The size of the memory monitor's cache for project resources. */
    public static final String N_SIZE_PROJECTRESOURCES = "size-projectresources";

    /** The size of the memory monitor's cache for projects. */
    public static final String N_SIZE_PROJECTS = "size-projects";

    /** The size of the memory monitor's cache for properties. */
    public static final String N_SIZE_PROPERTIES = "size-properties";

    /** The size of the memory monitor's cache for property lists. */
    public static final String N_SIZE_PROPERTYLISTS = "size-propertylists";

    /** The size of the memory monitor's cache for lists of resources. */
    public static final String N_SIZE_RESOURCELISTS = "size-resourcelists";

    /** The size of the memory monitor's cache for resources. */
    public static final String N_SIZE_RESOURCES = "size-resources";

    /** The size of the memory monitor's cache for roles. */
    public static final String N_SIZE_ROLES = "size-roles";

    /** The size of the memory monitor's cache for user/group relations. */
    public static final String N_SIZE_USERGROUPS = "size-usergroups";

    /** The size of the memory monitor's cache for users. */
    public static final String N_SIZE_USERS = "size-users";

    /** The subscriptionmanager node name. */
    public static final String N_SUBSCRIPTIONMANAGER = "subscriptionmanager";

    /** The main system configuration node name. */
    public static final String N_SYSTEM = "system";

    /** Node name for declaring a single text encryption. */
    public static final String N_TEXT_ENCRYPTION = "text-encryption";

    /** The node name for the time zone configuration. */
    public static final String N_TIMEZONE = "timezone";

    /** Node name for the authorization token lifetime. */
    public static final String N_TOKEN_LIFETIME = "tokenLifetime";

    /** The node name for the user-admin node. */
    public static final String N_USER_ADMIN = "user-admin";

    /** Node name for the user data check interval. */
    public static final String N_USER_DATA_CHECK_INTERVAL = "userDataCheckInterval";

    /** The node name for the user-deletedresource node. */
    public static final String N_USER_DELETEDRESOURCE = "user-deletedresource";

    /** The node name for the user-export node. */
    public static final String N_USER_EXPORT = "user-export";

    /** The node name for the user-guest node. */
    public static final String N_USER_GUEST = "user-guest";

    /** The node name for the validation handler. */
    public static final String N_VALIDATIONHANDLER = "validationhandler";

    /** The node name for the version history. */
    public static final String N_VERSIONHISTORY = "versionhistory";

    /** The node name for the warning-interval node. */
    public static final String N_WARNING_INTERVAL = "warning-interval";

    /** The node name which indicates if apache should be configurable in sitemanager. */
    public static final String N_WEBSERVERSCRIPTING = "webserver-scripting";

    public static final String N_WEBSERVERSCRIPTING_CONFIGTEMPLATE = "configtemplate";

    public static final String N_WEBSERVERSCRIPTING_FILENAMEPREFIX = "filenameprefix";

    public static final String N_WEBSERVERSCRIPTING_LOGGINGDIR = "loggingdir";

    public static final String N_WEBSERVERSCRIPTING_SECURETEMPLATE = "securetemplate";

    public static final String N_WEBSERVERSCRIPTING_TARGETPATH = "targetpath";

    public static final String N_WEBSERVERSCRIPTING_WEBSERVERSCRIPT = "webserverscript";

    /** The node name for the workflow configuration. */
    public static final String N_WORKFLOW = "workflow";

    /** The log object for this class. */
    private static final Log LOG = CmsLog.getLog(CmsSystemConfiguration.class);

    /** Node name for auto history cleanup setting. */
    private static final String N_AUTO_CLEANUP_HISTORY_ENTRIES = "auto-cleanup-history-entries";

    /** Node name for the credentials resolver setting. */
    private static final String N_CREDENTIALS_RESOLVER = "credentials-resolver";

    /** Node name for the user max inactive time. */
    private static final String N_MAX_INACTIVE_TIME = "maxInactiveTime";

    /** Node name for the 'require org unit' option. */
    private static final String N_REQUIRE_ORGUNIT = "requireOrgUnit";

    /** Node name for the element reuse mode. */
    private static final String N_REUSE_ELEMENTS = "reuse-elements";

    /** Node name for the shell server options. */
    private static final String N_SHELL_SERVER = "shell-server";

    /** Node name for two-factor auth configuration. */
    private static final String N_TWO_FACTOR_AUTHENTICATION = "two-factor-authentication";

    /** Node name for the user session mode. */
    private static final String N_USER_SESSION_MODE = "user-session-mode";

    /** The ADE cache settings. */
    private CmsADECacheSettings m_adeCacheSettings;

    /** The ADE configuration. */
    private String m_adeConfiguration;

    /** The ADE configuration parameters. */
    private Map<String, String> m_adeParameters = new LinkedHashMap<String, String>();

    private Map<String, I_CmsApiAuthorizationHandler> m_apiAuthorizationMap = new HashMap<>();

    private List<ApiAuthorizationConfig> m_apiAuthorizations = new ArrayList<>();

    /** Parameters for the authorization handler. */
    private Map<String, String> m_authHandlerParams = new HashMap<>();

    /** The authorization handler. */
    private String m_authorizationHandler;

    /** The settings of the memory monitor. */
    private CmsCacheSettings m_cacheSettings;

    /** The configured OpenCms default users and groups. */
    private CmsDefaultUsers m_cmsDefaultUsers;

    /** The flex cache configuration object. */
    private CmsFlexCacheConfiguration m_cmsFlexCacheConfiguration;

    /** The memory monitor configuration. */
    private CmsMemoryMonitorConfiguration m_cmsMemoryMonitorConfiguration;

    /** The credentials resolver instance. */
    private I_CmsCredentialsResolver m_credentialsResolver;

    /** The configured credentials resolver class name. */
    private String m_credentialsResolverClass;

    /** The default content encoding. */
    private String m_defaultContentEncoding;

    /** The detail page handler. */
    private I_CmsDetailPageHandler m_detailPageHandler = new CmsDefaultDetailPageHandler();

    /** The configured OpenCms event manager. */
    private CmsEventManager m_eventManager;

    /** Indicates if the version history is enabled. */
    private boolean m_historyEnabled;

    /** The maximum number of historical versions per resource. */
    private int m_historyVersions;

    /** The maximum number of historical versions for deleted resources. */
    private int m_historyVersionsAfterDeletion;

    /** The HTTP basic authentication settings. */
    private CmsHttpAuthenticationSettings m_httpAuthenticationSettings;

    /** The LetsEncrypt configuration. */
    private CmsLetsEncryptConfiguration m_letsEncryptConfig;

    /** The configured locale manager for multi language support. */
    private CmsLocaleManager m_localeManager;

    /** The configured login manager. */
    private CmsLoginManager m_loginManager;

    /** The mail settings. */
    private CmsMailSettings m_mailSettings;

    /** Notification project. */
    private String m_notificationProject;

    /** The duration after which responsibles will be notified about out-dated content (in days). */
    // It is an Integer object so that it can be distinguished if this optional element was set or not
    private Integer m_notificationTime;

    /** The password handler. */
    private I_CmsPasswordHandler m_passwordHandler;

    /** The permission handler. */
    private String m_permissionHandler;

    /** The configured publish list remove mode. */
    private String m_publishListRemoveMode;

    /** The configured publish manager. */
    private CmsPublishManager m_publishManager;

    /** A list of instantiated request handler classes. */
    private List<I_CmsRequestHandler> m_requestHandlers;

    /** A list of instantiated resource init handler classes. */
    private List<I_CmsResourceInit> m_resourceInitHandlers;

    /** Value of the restrict-detail-contents option. */
    private String m_restrictDetailContents;

    /** The runtime info factory. */
    private I_CmsDbContextFactory m_runtimeInfoFactory;

    /** The runtime properties. */
    private Map<String, String> m_runtimeProperties;

    /** Flag indicating if the SAX parser implementation classes should be stored in system properties
     *  to improve the unmarshalling performance. */
    private boolean m_saxImplProperties;

    /** The configured session storage provider class name. */
    private String m_sessionStorageProvider;

    /** The shell server options. */
    private CmsRemoteShellConfiguration m_shellServerOptions;

    /** The subscription manager. */
    private CmsSubscriptionManager m_subscriptionManager;

    /** The temporary file project id. */
    private int m_tempFileProjectId;

    private Map<String, I_CmsTextEncryption> m_textEncryptions = new LinkedHashMap<>();

    /** Two-factor authentication configuration. */
    private CmsTwoFactorAuthenticationConfig m_twoFactorConfig;

    private CmsUserDataRequestManager m_userDataRequestManager;

    /** The user session mode. */
    private UserSessionMode m_userSessionMode;

    /** The configured validation handler. */
    private String m_validationHandler;

    /** The configured workflow manager. */
    private I_CmsWorkflowManager m_workflowManager;

    /**
     * Adds an ADE configuration parameter.<p>
     *
     * @param name the parameter name
     * @param value the parameter value
     */
    public void addAdeParameter(String name, String value) {

        m_adeParameters.put(name, value);
    }

    /**
     * Adds a parameter for the authorization handler.
     *
     * @param name the parameter name
     * @param value the parameter value
     */
    public void addAuthorizationHandlerParam(String name, String value) {

        m_authHandlerParams.put(name, value);
    }

    /**
     * @see org.opencms.configuration.I_CmsConfigurationParameterHandler#addConfigurationParameter(java.lang.String, java.lang.String)
     */
    @Override
    public void addConfigurationParameter(String paramName, String paramValue) {

        m_runtimeProperties.put(paramName, paramValue);
    }

    /**
     * Adds the event manager class.<p>
     *
     * @param clazz the class name of event manager class  to instantiate and add
     */
    public void addEventManager(String clazz) {

        try {
            m_eventManager = (CmsEventManager)Class.forName(clazz).newInstance();
            if (CmsLog.INIT.isInfoEnabled()) {
                CmsLog.INIT.info(
                    Messages.get().getBundle().key(Messages.INIT_EVENTMANAGER_CLASS_SUCCESS_1, m_eventManager));
            }
        } catch (Throwable t) {
            LOG.error(Messages.get().getBundle().key(Messages.INIT_EVENTMANAGER_CLASS_INVALID_1, clazz), t);
            return;
        }
    }

    /**
     * Adds a new instance of a request handler class.<p>
     *
     * @param clazz the class name of the request handler to instantiate and add
     */
    public void addRequestHandler(String clazz, CmsParameterConfiguration params) {

        Object handler;
        try {
            handler = Class.forName(clazz).newInstance();
        } catch (Throwable t) {
            LOG.error(Messages.get().getBundle().key(Messages.LOG_INIT_REQUEST_HANDLER_FAILURE_1, clazz), t);
            return;
        }
        if (handler instanceof I_CmsRequestHandler) {
            ((I_CmsRequestHandler)handler).initParameters(params);
            m_requestHandlers.add((I_CmsRequestHandler)handler);
            if (CmsLog.INIT.isInfoEnabled()) {
                CmsLog.INIT.info(Messages.get().getBundle().key(Messages.INIT_REQUEST_HANDLER_SUCCESS_1, clazz));
            }
        } else {
            if (CmsLog.INIT.isErrorEnabled()) {
                CmsLog.INIT.error(Messages.get().getBundle().key(Messages.INIT_REQUEST_HANDLER_INVALID_1, clazz));
            }
        }
    }

    /**
     * Adds a new instance of a resource init handler class.<p>
     *
     * @param clazz the class name of the resource init handler to instantiate and add
     * @param params the parameters set for the resource init handler (parameters need to be copied out, the object will be modified after use)
     */
    public void addResourceInitHandler(String clazz, CmsParameterConfiguration params)
    throws CmsConfigurationException {

        Object initClass;
        try {
            initClass = Class.forName(clazz).newInstance();
        } catch (Throwable t) {
            LOG.error(Messages.get().getBundle().key(Messages.LOG_RESOURCE_INIT_CLASS_INVALID_1, clazz), t);
            return;
        }
        if (initClass instanceof I_CmsResourceInit) {
            ((I_CmsResourceInit)initClass).initParameters(params);
            m_resourceInitHandlers.add((I_CmsResourceInit)initClass);
            if (CmsLog.INIT.isInfoEnabled()) {
                CmsLog.INIT.info(Messages.get().getBundle().key(Messages.INIT_RESOURCE_INIT_SUCCESS_1, clazz));
            }
        } else {
            if (CmsLog.INIT.isErrorEnabled()) {
                CmsLog.INIT.error(Messages.get().getBundle().key(Messages.INIT_RESOURCE_INIT_INVALID_CLASS_1, clazz));
            }
        }
    }

    /**
     * @see org.opencms.configuration.I_CmsXmlConfiguration#addXmlDigesterRules(org.apache.commons.digester3.Digester)
     */
    public void addXmlDigesterRules(Digester digester) {

        // add finish rule
        digester.addCallMethod("*/" + N_SYSTEM, "initializeFinished");

        // add rule for internationalization
        digester.addObjectCreate("*/" + N_SYSTEM + "/" + N_I18N, CmsLocaleManager.class);
        digester.addSetNext("*/" + N_SYSTEM + "/" + N_I18N, "setLocaleManager");

        // add locale handler creation rule
        digester.addObjectCreate(
            "*/" + N_SYSTEM + "/" + N_I18N + "/" + N_LOCALEHANDLER,
            CmsConfigurationException.class.getName(),
            A_CLASS);
        digester.addSetNext("*/" + N_SYSTEM + "/" + N_I18N + "/" + N_LOCALEHANDLER, "setLocaleHandler");

        // add locale rules
        digester.addCallMethod(
            "*/" + N_SYSTEM + "/" + N_I18N + "/" + N_LOCALESCONFIGURED + "/" + N_LOCALE,
            "addAvailableLocale",
            0);
        digester.addCallMethod(
            "*/" + N_SYSTEM + "/" + N_I18N + "/" + N_LOCALESDEFAULT + "/" + N_LOCALE,
            "addDefaultLocale",
            0);
        // add time zone rule
        digester.addCallMethod("*/" + N_SYSTEM + "/" + N_I18N + "/" + N_TIMEZONE, "setTimeZone", 0);
        digester.addCallMethod("*/" + N_SYSTEM + "/" + N_I18N + "/" + N_REUSE_ELEMENTS, "setReuseElements", 0);

        // add version history rules
        digester.addCallMethod("*/" + N_SYSTEM + "/" + N_VERSIONHISTORY, "setHistorySettings", 3);
        digester.addCallParam("*/" + N_SYSTEM + "/" + N_VERSIONHISTORY, 0, A_ENABLED);
        digester.addCallParam("*/" + N_SYSTEM + "/" + N_VERSIONHISTORY, 1, A_COUNT);
        digester.addCallParam("*/" + N_SYSTEM + "/" + N_VERSIONHISTORY, 2, A_DELETED);

        // add mail configuration rule
        digester.addObjectCreate("*/" + N_SYSTEM + "/" + N_MAIL, CmsMailSettings.class);
        digester.addCallMethod("*/" + N_SYSTEM + "/" + N_MAIL + "/" + N_MAILFROM, "setMailFromDefault", 0);
        digester.addSetNext("*/" + N_SYSTEM + "/" + N_MAIL, "setMailSettings");

        // add mail host configuration rule
        digester.addCallMethod("*/" + N_SYSTEM + "/" + N_MAIL + "/" + N_MAILHOST, "addMailHost", 9);
        digester.addCallParam("*/" + N_SYSTEM + "/" + N_MAIL + "/" + N_MAILHOST, 0, A_NAME);
        digester.addCallParam("*/" + N_SYSTEM + "/" + N_MAIL + "/" + N_MAILHOST, 1, A_PORT);
        digester.addCallParam("*/" + N_SYSTEM + "/" + N_MAIL + "/" + N_MAILHOST, 2, A_ORDER);
        digester.addCallParam("*/" + N_SYSTEM + "/" + N_MAIL + "/" + N_MAILHOST, 3, A_PROTOCOL);
        digester.addCallParam("*/" + N_SYSTEM + "/" + N_MAIL + "/" + N_MAILHOST, 4, A_SECURITY);
        digester.addCallParam("*/" + N_SYSTEM + "/" + N_MAIL + "/" + N_MAILHOST, 5, A_USER);
        digester.addCallParam("*/" + N_SYSTEM + "/" + N_MAIL + "/" + N_MAILHOST, 6, A_PASSWORD);
        digester.addCallParam("*/" + N_SYSTEM + "/" + N_MAIL + "/" + N_MAILHOST, 7, A_ID);
        digester.addCallParam("*/" + N_SYSTEM + "/" + N_MAIL + "/" + N_MAILHOST, 8, A_MAILFROM);

        // add event classes
        digester.addCallMethod("*/" + N_SYSTEM + "/" + N_EVENTS + "/" + N_EVENTMANAGER, "addEventManager", 1);
        digester.addCallParam("*/" + N_SYSTEM + "/" + N_EVENTS + "/" + N_EVENTMANAGER, 0, A_CLASS);

        // use array so we can modify it in the inner class and give each resource init handler a fresh CmsParameterConfiguration instance
        CmsParameterConfiguration resourceHandlerParams[] = new CmsParameterConfiguration[] {null};

        digester.addRule("*/" + N_SYSTEM + "/" + N_RESOURCEINIT + "/" + N_RESOURCEINITHANDLER, new Rule() {

            private String m_class;

            @Override
            public void begin(String namespace, String name, Attributes attributes) throws Exception {

                m_class = attributes.getValue(A_CLASS);
                resourceHandlerParams[0] = new CmsParameterConfiguration();
            }

            @Override
            public void end(String namespace, String name) throws Exception {

                addResourceInitHandler(m_class, resourceHandlerParams[0]);

            }
        });

        digester.addRule(
            "*/" + N_SYSTEM + "/" + N_RESOURCEINIT + "/" + N_RESOURCEINITHANDLER + "/" + N_PARAM,
            new Rule() {

                private String m_name;
                private String m_value;

                @Override
                public void begin(String namespace, String name, Attributes attributes) throws Exception {

                    m_name = attributes.getValue(A_NAME);
                    m_value = null;
                }

                @Override
                public void body(String namespace, String name, String text) throws Exception {

                    m_value = text;
                }

                @Override
                public void end(String namespace, String name) throws Exception {

                    resourceHandlerParams[0].add(m_name, m_value);
                }
            });

        CmsParameterConfiguration[] requestHandlerParams = new CmsParameterConfiguration[] {null};
        digester.addRule(
            "*/" + N_SYSTEM + "/" + N_REQUESTHANDLERS + "/" + N_REQUESTHANDLER + "/" + N_PARAM,
            new Rule() {

                private String m_name;
                private String m_value;

                @Override
                public void begin(String namespace, String name, Attributes attributes) throws Exception {

                    m_name = attributes.getValue(A_NAME);
                    m_value = null;
                }

                @Override
                public void body(String namespace, String name, String text) throws Exception {

                    m_value = text;
                }

                @Override
                public void end(String namespace, String name) throws Exception {

                    requestHandlerParams[0].add(m_name, m_value);
                }
            });
        digester.addRule("*/" + N_SYSTEM + "/" + N_REQUESTHANDLERS + "/" + N_REQUESTHANDLER, new Rule() {

            private String m_class;

            @Override
            public void begin(String namespace, String name, Attributes attributes) throws Exception {

                m_class = attributes.getValue(A_CLASS);
                requestHandlerParams[0] = new CmsParameterConfiguration();
            }

            @Override
            public void end(String namespace, String name) throws Exception {

                addRequestHandler(m_class, requestHandlerParams[0]);
            }

        });

        // add password handler creation rule
        digester.addObjectCreate(
            "*/" + N_SYSTEM + "/" + N_PASSWORDHANDLER,
            CmsConfigurationException.class.getName(),
            A_CLASS);
        digester.addCallMethod(
            "*/" + N_SYSTEM + "/" + N_PASSWORDHANDLER,
            I_CmsConfigurationParameterHandler.INIT_CONFIGURATION_METHOD);
        digester.addBeanPropertySetter(
            "*/" + N_SYSTEM + "/" + N_PASSWORDHANDLER + "/" + N_PASSWORDENCODING,
            "inputEncoding");
        digester.addBeanPropertySetter("*/" + N_SYSTEM + "/" + N_PASSWORDHANDLER + "/" + N_DIGESTTYPE, "digestType");
        digester.addSetNext("*/" + N_SYSTEM + "/" + N_PASSWORDHANDLER, "setPasswordHandler");

        // add generic parameter rules for password handler
        digester.addCallMethod(
            "*/" + I_CmsXmlConfiguration.N_PARAM,
            I_CmsConfigurationParameterHandler.ADD_PARAMETER_METHOD,
            2);
        digester.addCallParam("*/" + I_CmsXmlConfiguration.N_PARAM, 0, I_CmsXmlConfiguration.A_NAME);
        digester.addCallParam("*/" + I_CmsXmlConfiguration.N_PARAM, 1);

        // add validation handler creation rules
        digester.addCallMethod("*/" + N_SYSTEM + "/" + N_VALIDATIONHANDLER, "setValidationHandler", 1);
        digester.addCallParam("*/" + N_SYSTEM + "/" + N_VALIDATIONHANDLER, 0, A_CLASS);

        // add login manager creation rules
        digester.addCallMethod("*/" + N_LOGINMANAGER, "setLoginManager", 9);
        digester.addCallParam("*/" + N_LOGINMANAGER + "/" + N_DISABLEMINUTES, 0);
        digester.addCallParam("*/" + N_LOGINMANAGER + "/" + N_MAXBADATTEMPTS, 1);
        digester.addCallParam("*/" + N_LOGINMANAGER + "/" + N_ENABLESCURITY, 2);
        digester.addCallParam("*/" + N_LOGINMANAGER + "/" + N_TOKEN_LIFETIME, 3);
        digester.addCallParam("*/" + N_LOGINMANAGER + "/" + N_MAX_INACTIVE_TIME, 4);
        digester.addCallParam("*/" + N_LOGINMANAGER + "/" + N_PASSWORD_CHANGE_INTERVAL, 5);
        digester.addCallParam("*/" + N_LOGINMANAGER + "/" + N_USER_DATA_CHECK_INTERVAL, 6);
        digester.addCallParam("*/" + N_LOGINMANAGER + "/" + N_REQUIRE_ORGUNIT, 7);
        digester.addCallParam("*/" + N_LOGINMANAGER + "/" + N_LOGOUT_URI, 8);

        try {
            digester.addRule("*/" + N_TWO_FACTOR_AUTHENTICATION, new NodeCreateRule() {

                @Override
                public void end(String namespace, String name) throws Exception {

                    org.w3c.dom.Element elem = (org.w3c.dom.Element)digester.pop();
                    DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
                    Document doc = dbf.newDocumentBuilder().newDocument();
                    doc.appendChild(doc.importNode(elem, true));
                    org.dom4j.Document dom4jDoc = CmsXmlUtils.convertDocumentFromW3CToDom4j(doc);
                    m_twoFactorConfig = new CmsTwoFactorAuthenticationConfig(dom4jDoc.getRootElement());
                }

            });
        } catch (ParserConfigurationException e) {
            LOG.error(e.getLocalizedMessage(), e);
        }

        digester.addCallMethod(
            "*/" + N_SYSTEM + "/" + N_SAX_IMPL_SYSTEM_PROPERTIES,
            "setUseSaxImplSystemProperties",
            1);
        digester.addCallParam("*/" + N_SYSTEM + "/" + N_SAX_IMPL_SYSTEM_PROPERTIES, 0);

        // add compatibility parameter rules
        digester.addCallMethod(
            "*/" + N_SYSTEM + "/" + N_RUNTIMEPROPERTIES + "/" + N_PARAM,
            I_CmsConfigurationParameterHandler.ADD_PARAMETER_METHOD,
            2);
        digester.addCallParam(
            "*/" + N_SYSTEM + "/" + N_RUNTIMEPROPERTIES + "/" + N_PARAM,
            0,
            I_CmsXmlConfiguration.A_NAME);
        digester.addCallParam("*/" + N_SYSTEM + "/" + N_RUNTIMEPROPERTIES + "/" + N_PARAM, 1);

        // add runtime classes configuration rules
        digester.addCallMethod(
            "*/" + N_SYSTEM + "/" + N_RUNTIMECLASSES + "/" + N_RUNTIMEINFO,
            "setRuntimeInfoFactory",
            1);
        digester.addCallParam("*/" + N_SYSTEM + "/" + N_RUNTIMECLASSES + "/" + N_RUNTIMEINFO, 0, A_CLASS);

        // add default users rule
        digester.addCallMethod("*/" + N_SYSTEM + "/" + N_DEFAULTUSERS, "setCmsDefaultUsers", 7);
        digester.addCallParam("*/" + N_SYSTEM + "/" + N_DEFAULTUSERS + "/" + N_USER_ADMIN, 0);
        digester.addCallParam("*/" + N_SYSTEM + "/" + N_DEFAULTUSERS + "/" + N_USER_GUEST, 1);
        digester.addCallParam("*/" + N_SYSTEM + "/" + N_DEFAULTUSERS + "/" + N_USER_EXPORT, 2);
        digester.addCallParam("*/" + N_SYSTEM + "/" + N_DEFAULTUSERS + "/" + N_USER_DELETEDRESOURCE, 3);
        digester.addCallParam("*/" + N_SYSTEM + "/" + N_DEFAULTUSERS + "/" + N_GROUP_ADMINISTRATORS, 4);
        digester.addCallParam("*/" + N_SYSTEM + "/" + N_DEFAULTUSERS + "/" + N_GROUP_USERS, 5);
        digester.addCallParam("*/" + N_SYSTEM + "/" + N_DEFAULTUSERS + "/" + N_GROUP_GUESTS, 6);

        // add defaultContentEncoding rule
        digester.addCallMethod("*/" + N_SYSTEM + "/" + N_DEFAULT_CONTENT_ENCODING, "setDefaultContentEncoding", 1);
        digester.addCallParam("*/" + N_SYSTEM + "/" + N_DEFAULT_CONTENT_ENCODING, 0);

        // add memorymonitor configuration rule
        digester.addObjectCreate("*/" + N_SYSTEM + "/" + N_MEMORYMONITOR, CmsMemoryMonitorConfiguration.class);
        digester.addCallMethod("*/" + N_SYSTEM + "/" + N_MEMORYMONITOR, "initialize", 5);
        digester.addCallParam("*/" + N_SYSTEM + "/" + N_MEMORYMONITOR, 0, A_CLASS);
        digester.addCallParam("*/" + N_SYSTEM + "/" + N_MEMORYMONITOR + "/" + N_MAXUSAGE_PERCENT, 1);
        digester.addCallParam("*/" + N_SYSTEM + "/" + N_MEMORYMONITOR + "/" + N_LOG_INTERVAL, 2);
        digester.addCallParam("*/" + N_SYSTEM + "/" + N_MEMORYMONITOR + "/" + N_EMAIL_INTERVAL, 3);
        digester.addCallParam("*/" + N_SYSTEM + "/" + N_MEMORYMONITOR + "/" + N_WARNING_INTERVAL, 4);
        digester.addCallMethod("*/" + N_SYSTEM + "/" + N_MEMORYMONITOR + "/" + N_EMAIL_SENDER, "setEmailSender", 0);
        digester.addCallMethod(
            "*/" + N_SYSTEM + "/" + N_MEMORYMONITOR + "/" + N_EMAIL_RECEIVER + "/" + N_RECEIVER,
            "addEmailReceiver",
            0);

        // set the MemoryMonitorConfiguration initialized once before
        digester.addSetNext("*/" + N_SYSTEM + "/" + N_MEMORYMONITOR, "setCmsMemoryMonitorConfiguration");

        // add flexcache configuration rule
        digester.addObjectCreate("*/" + N_SYSTEM + "/" + N_FLEXCACHE, CmsFlexCacheConfiguration.class);
        digester.addCallMethod("*/" + N_SYSTEM + "/" + N_FLEXCACHE, "initialize", 6);
        digester.addCallParam("*/" + N_SYSTEM + "/" + N_FLEXCACHE + "/" + N_CACHE_ENABLED, 0);
        digester.addCallParam("*/" + N_SYSTEM + "/" + N_FLEXCACHE + "/" + N_CACHE_OFFLINE, 1);
        digester.addCallParam("*/" + N_SYSTEM + "/" + N_FLEXCACHE + "/" + N_MAXCACHEBYTES, 2);
        digester.addCallParam("*/" + N_SYSTEM + "/" + N_FLEXCACHE + "/" + N_AVGCACHEBYTES, 3);
        digester.addCallParam("*/" + N_SYSTEM + "/" + N_FLEXCACHE + "/" + N_MAXENTRYBYTES, 4);
        digester.addCallParam("*/" + N_SYSTEM + "/" + N_FLEXCACHE + "/" + N_MAXKEYS, 5);
        // add flexcache device selector
        digester.addCallMethod(
            "*/" + N_SYSTEM + "/" + N_FLEXCACHE + "/" + N_DEVICESELECTOR,
            "setDeviceSelectorConfiguration",
            1);
        digester.addCallParam("*/" + N_SYSTEM + "/" + N_FLEXCACHE + "/" + N_DEVICESELECTOR, 0, A_CLASS);

        // set the FlexCacheConfiguration initialized once before
        digester.addSetNext("*/" + N_SYSTEM + "/" + N_FLEXCACHE, "setCmsFlexCacheConfiguration");

        // add http basic authentication rules
        digester.addObjectCreate("*/" + N_SYSTEM + "/" + N_HTTP_AUTHENTICATION, CmsHttpAuthenticationSettings.class);
        digester.addCallMethod(
            "*/" + N_SYSTEM + "/" + N_HTTP_AUTHENTICATION + "/" + N_BROWSER_BASED,
            "setUseBrowserBasedHttpAuthentication",
            0);
        digester.addCallMethod(
            "*/" + N_SYSTEM + "/" + N_HTTP_AUTHENTICATION + "/" + N_FORM_BASED,
            "setFormBasedHttpAuthenticationUri",
            0);
        digester.addSetNext("*/" + N_SYSTEM + "/" + N_HTTP_AUTHENTICATION, "setHttpAuthenticationSettings");

        // cache rules
        digester.addObjectCreate("*/" + N_SYSTEM + "/" + N_RESULTCACHE, CmsCacheSettings.class);
        digester.addCallMethod("*/" + N_SYSTEM + "/" + N_RESULTCACHE + "/" + N_KEYGENERATOR, "setCacheKeyGenerator", 0);
        digester.addCallMethod("*/" + N_SYSTEM + "/" + N_RESULTCACHE + "/" + N_SIZE_USERS, "setUserCacheSize", 0);
        digester.addCallMethod("*/" + N_SYSTEM + "/" + N_RESULTCACHE + "/" + N_SIZE_GROUPS, "setGroupCacheSize", 0);
        digester.addCallMethod("*/" + N_SYSTEM + "/" + N_RESULTCACHE + "/" + N_SIZE_ORGUNITS, "setOrgUnitCacheSize", 0);
        digester.addCallMethod(
            "*/" + N_SYSTEM + "/" + N_RESULTCACHE + "/" + N_SIZE_USERGROUPS,
            "setUserGroupsCacheSize",
            0);
        digester.addCallMethod("*/" + N_SYSTEM + "/" + N_RESULTCACHE + "/" + N_SIZE_PROJECTS, "setProjectCacheSize", 0);
        digester.addCallMethod(
            "*/" + N_SYSTEM + "/" + N_RESULTCACHE + "/" + N_SIZE_PROJECTRESOURCES,
            "setProjectResourcesCacheSize",
            0);
        digester.addCallMethod(
            "*/" + N_SYSTEM + "/" + N_RESULTCACHE + "/" + N_SIZE_RESOURCES,
            "setResourceCacheSize",
            0);
        digester.addCallMethod("*/" + N_SYSTEM + "/" + N_RESULTCACHE + "/" + N_SIZE_ROLES, "setRolesCacheSize", 0);
        digester.addCallMethod(
            "*/" + N_SYSTEM + "/" + N_RESULTCACHE + "/" + N_SIZE_RESOURCELISTS,
            "setResourcelistCacheSize",
            0);
        digester.addCallMethod(
            "*/" + N_SYSTEM + "/" + N_RESULTCACHE + "/" + N_SIZE_PROPERTIES,
            "setPropertyCacheSize",
            0);
        digester.addCallMethod(
            "*/" + N_SYSTEM + "/" + N_RESULTCACHE + "/" + N_SIZE_PROPERTYLISTS,
            "setPropertyListsCacheSize",
            0);
        digester.addCallMethod("*/" + N_SYSTEM + "/" + N_RESULTCACHE + "/" + N_SIZE_ACLS, "setAclCacheSize", 0);
        digester.addCallMethod(
            "*/" + N_SYSTEM + "/" + N_RESULTCACHE + "/" + N_SIZE_PERMISSIONS,
            "setPermissionCacheSize",
            0);
        digester.addCallMethod(
            "*/" + N_SYSTEM + "/" + N_RESULTCACHE + "/" + N_SIZE_CONTAINERPAGE_OFFLINE,
            "setContainerPageOfflineSize",
            0);
        digester.addCallMethod(
            "*/" + N_SYSTEM + "/" + N_RESULTCACHE + "/" + N_SIZE_CONTAINERPAGE_ONLINE,
            "setContainerPageOnlineSize",
            0);
        digester.addSetNext("*/" + N_SYSTEM + "/" + N_RESULTCACHE, "setCacheSettings");

        // set the notification time
        digester.addCallMethod(
            "*/" + N_SYSTEM + "/" + N_CONTENT_NOTIFICATION + "/" + N_NOTIFICATION_TIME,
            "setNotificationTime",
            1);
        digester.addCallParam("*/" + N_SYSTEM + "/" + N_CONTENT_NOTIFICATION + "/" + N_NOTIFICATION_TIME, 0);

        // set the notification project
        digester.addCallMethod(
            "*/" + N_SYSTEM + "/" + N_CONTENT_NOTIFICATION + "/" + N_NOTIFICATION_PROJECT,
            "setNotificationProject",
            1);
        digester.addCallParam("*/" + N_SYSTEM + "/" + N_CONTENT_NOTIFICATION + "/" + N_NOTIFICATION_PROJECT, 0);

        // add authorization handler creation rules
        digester.addCallMethod("*/" + N_SYSTEM + "/" + N_AUTHORIZATIONHANDLER, "setAuthorizationHandler", 1);
        digester.addCallParam("*/" + N_SYSTEM + "/" + N_AUTHORIZATIONHANDLER, 0, A_CLASS);

        digester.addCallMethod(
            "*/" + N_SYSTEM + "/" + N_AUTHORIZATIONHANDLER + "/parameters/param",
            "addAuthorizationHandlerParam",
            2);
        digester.addCallParam("*/" + N_SYSTEM + "/" + N_AUTHORIZATIONHANDLER + "/parameters/param", 0, "name");
        digester.addCallParam("*/" + N_SYSTEM + "/" + N_AUTHORIZATIONHANDLER + "/parameters/param", 1);

        String apiAuthPath = "*/system/" + N_API_AUTHORIZATIONS + "/" + N_API_AUTHORIZATION;
        digester.addRule(apiAuthPath, new Rule() {

            @Override
            public void begin(String namespace, String name, Attributes attributes) throws Exception {

                digester.push(new ApiAuthorizationConfig());
            }

            @Override
            public void end(String namespace, String name) throws Exception {

                ApiAuthorizationConfig config = (ApiAuthorizationConfig)digester.pop();
                addApiAuthorization(config);
            }
        });

        String namePath = apiAuthPath + "/name";
        digester.addCallMethod(namePath, "setName", 1);
        digester.addCallParam(namePath, 0);

        String classNamePath = apiAuthPath + "/class";
        digester.addCallMethod(classNamePath, "setClassName", 1);
        digester.addCallParam(classNamePath, 0);

        String paramPath = apiAuthPath + "/param";
        digester.addCallMethod(paramPath, "setParam", 2);
        digester.addCallParam(paramPath, 0, "name");
        digester.addCallParam(paramPath, 1);

        // add publish manager configuration rule
        digester.addObjectCreate("*/" + N_SYSTEM + "/" + N_PUBLISHMANAGER, CmsPublishManager.class);
        digester.addCallMethod(
            "*/" + N_SYSTEM + "/" + N_PUBLISHMANAGER + "/" + N_HISTORYSIZE,
            "setPublishHistorySize",
            0);
        digester.addCallMethod(
            "*/" + N_SYSTEM + "/" + N_PUBLISHMANAGER + "/" + N_QUEUEPERSISTANCE,
            "setPublishQueuePersistance",
            0);
        digester.addCallMethod(
            "*/" + N_SYSTEM + "/" + N_PUBLISHMANAGER + "/" + N_QUEUESHUTDOWNTIME,
            "setPublishQueueShutdowntime",
            0);
        digester.addCallMethod(
            "*/" + N_SYSTEM + "/" + N_PUBLISHMANAGER + "/" + N_AUTO_CLEANUP_HISTORY_ENTRIES,
            "setAutoCleanupHistoryEntries",
            0);
        digester.addSetNext("*/" + N_SYSTEM + "/" + N_PUBLISHMANAGER, "setPublishManager");

        // add rule for session storage provider
        digester.addCallMethod("*/" + N_SYSTEM + "/" + N_SESSION_STORAGEPROVIDER, "setSessionStorageProvider", 1);
        digester.addCallParam("*/" + N_SYSTEM + "/" + N_SESSION_STORAGEPROVIDER, 0, A_CLASS);

        // add rule for permission handler
        digester.addCallMethod("*/" + N_SYSTEM + "/" + N_PERMISSIONHANDLER, "setPermissionHandler", 1);
        digester.addCallParam("*/" + N_SYSTEM + "/" + N_PERMISSIONHANDLER, 0, A_CLASS);

        // add rules for servlet container settings
        digester.addCallMethod(
            "*/" + N_SYSTEM + "/" + N_SERVLETCONTAINERSETTINGS + "/" + N_PREVENTRESPONSEFLUSH,
            "setPreventResponseFlush",
            0);
        digester.addCallMethod(
            "*/" + N_SYSTEM + "/" + N_SERVLETCONTAINERSETTINGS + "/" + N_RELEASETAGSAFTEREND,
            "setReleaseTagsAfterEnd",
            0);
        digester.addCallMethod(
            "*/" + N_SYSTEM + "/" + N_SERVLETCONTAINERSETTINGS + "/" + N_REQUESTERRORPAGEATTRIBUTE,
            "setRequestErrorPageAttribute",
            0);
        digester.addCallMethod(
            "*/" + N_SYSTEM + "/" + N_SERVLETCONTAINERSETTINGS,
            "setServletContainerSettingsMode",
            1);
        digester.addCallParam("*/" + N_SYSTEM + "/" + N_SERVLETCONTAINERSETTINGS, 0, A_MODE);

        // add rule for ADE cache settings
        String adeCachePath = "*/" + N_SYSTEM + "/" + N_ADE + "/" + N_ADE_CACHE;
        digester.addObjectCreate(adeCachePath, CmsADECacheSettings.class);
        // container page cache
        digester.addCallMethod(adeCachePath + "/" + N_CONTAINERPAGES, "setContainerPageOfflineSize", 1);
        digester.addCallParam(adeCachePath + "/" + N_CONTAINERPAGES, 0, A_OFFLINE);
        digester.addCallMethod(adeCachePath + "/" + N_CONTAINERPAGES, "setContainerPageOnlineSize", 1);
        digester.addCallParam(adeCachePath + "/" + N_CONTAINERPAGES, 0, A_ONLINE);
        // groupcontainer cache
        digester.addCallMethod(adeCachePath + "/" + N_GROUPCONTAINERS, "setGroupContainerOfflineSize", 1);
        digester.addCallParam(adeCachePath + "/" + N_GROUPCONTAINERS, 0, A_OFFLINE);
        digester.addCallMethod(adeCachePath + "/" + N_GROUPCONTAINERS, "setGroupContainerOnlineSize", 1);
        digester.addCallParam(adeCachePath + "/" + N_GROUPCONTAINERS, 0, A_ONLINE);
        // set the settings
        digester.addSetNext(adeCachePath, "setAdeCacheSettings");

        String adeParamPath = "*/" + N_SYSTEM + "/" + N_ADE + "/" + N_PARAMETERS + "/" + N_PARAM;
        digester.addCallMethod(adeParamPath, "addAdeParameter", 2);
        digester.addCallParam(adeParamPath, 0, I_CmsXmlConfiguration.A_NAME);
        digester.addCallParam(adeParamPath, 1);

        // add rule for subscription manager settings
        digester.addObjectCreate("*/" + N_SYSTEM + "/" + N_SUBSCRIPTIONMANAGER, CmsSubscriptionManager.class);
        digester.addCallMethod("*/" + N_SYSTEM + "/" + N_SUBSCRIPTIONMANAGER, "setEnabled", 1);
        digester.addCallParam("*/" + N_SYSTEM + "/" + N_SUBSCRIPTIONMANAGER, 0, A_ENABLED);
        digester.addCallMethod("*/" + N_SYSTEM + "/" + N_SUBSCRIPTIONMANAGER, "setPoolName", 1);
        digester.addCallParam("*/" + N_SYSTEM + "/" + N_SUBSCRIPTIONMANAGER, 0, A_POOLNAME);
        digester.addCallMethod("*/" + N_SYSTEM + "/" + N_SUBSCRIPTIONMANAGER, "setMaxVisitedCount", 1);
        digester.addCallParam("*/" + N_SYSTEM + "/" + N_SUBSCRIPTIONMANAGER, 0, A_MAXVISITED);
        digester.addSetNext("*/" + N_SYSTEM + "/" + N_SUBSCRIPTIONMANAGER, "setSubscriptionManager");

        digester.addCallMethod("*/" + N_SYSTEM + "/" + N_PUBLISH_LIST_REMOVE_MODE, "setPublishListRemoveMode", 1);
        digester.addCallParam("*/" + N_SYSTEM + "/" + N_PUBLISH_LIST_REMOVE_MODE, 0, A_MODE);

        String workflowXpath = "*/" + N_SYSTEM + "/" + N_WORKFLOW;
        digester.addObjectCreate(workflowXpath, CmsDefaultWorkflowManager.class.getName(), A_CLASS);
        digester.addObjectCreate(workflowXpath + "/" + N_PARAMETERS, LinkedHashMap.class);
        digester.addCallMethod(workflowXpath + "/" + N_PARAMETERS + "/" + N_PARAM, "put", 2);
        digester.addCallParam(workflowXpath + "/" + N_PARAMETERS + "/" + N_PARAM, 0, A_NAME);
        digester.addCallParam(workflowXpath + "/" + N_PARAMETERS + "/" + N_PARAM, 1);
        digester.addSetNext(workflowXpath + "/" + N_PARAMETERS, "setParameters");
        digester.addSetNext(workflowXpath, "setWorkflowManager");

        CmsLetsEncryptConfiguration.CONFIG_HELPER.addRules(digester);
        digester.addSetNext(CmsLetsEncryptConfiguration.CONFIG_HELPER.getBasePath(), "setLetsEncryptConfig");

        digester.addRule("*/" + N_SYSTEM + "/" + N_ENCRYPTION + "/" + N_TEXT_ENCRYPTION, new Rule() {

            @Override
            public void begin(String namespace, String name, Attributes attributes) throws Exception {

                String className = attributes.getValue(A_CLASS);
                String instanceName = attributes.getValue(A_NAME);
                I_CmsTextEncryption encrypter = (I_CmsTextEncryption)Class.forName(className).newInstance();
                encrypter.setName(instanceName);
                digester.push(encrypter);
            }

            @Override
            public void end(String namespace, String name) throws Exception {

                I_CmsTextEncryption encrypter = (I_CmsTextEncryption)digester.pop();
                m_textEncryptions.put(encrypter.getName(), encrypter);
            }
        });

        // make sure that a 'default' text encryption exists, but attach the rule to the system element
        // because the <encryption> element doesn't necessarily exist,
        digester.addRule("*/" + N_SYSTEM, new Rule() {

            public void end(String namespace, String name) throws Exception {

                if (m_textEncryptions.get("default") == null) {
                    CmsAESTextEncryption defaultEncryption = new CmsAESTextEncryption();
                    defaultEncryption.setName("default");
                    defaultEncryption.addConfigurationParameter(
                        CmsAESTextEncryption.PARAM_SECRET,
                        RandomStringUtils.randomAlphanumeric(24));
                    m_textEncryptions.put("default", defaultEncryption);
                }
            };
        });

        String userSessionPath = "*/" + N_SYSTEM + "/" + N_USER_SESSION_MODE;
        digester.addCallMethod(userSessionPath, "setUserSessionMode", 0);

        String credentialsResolverPath = "*/" + N_SYSTEM + "/" + N_CREDENTIALS_RESOLVER;
        digester.addCallMethod(credentialsResolverPath, "setCredentialsResolver", 0);

        digester.addCallMethod("*/" + N_SYSTEM + "/" + N_RESTRICT_DETAIL_CONTENTS, "setRestrictDetailContents", 1);
        digester.addCallParam("*/" + N_SYSTEM + "/" + N_RESTRICT_DETAIL_CONTENTS, 0);

        String shellServerPath = "*/" + N_SYSTEM + "/" + N_SHELL_SERVER;
        digester.addCallMethod(shellServerPath, "setShellServerOptions", 2);
        digester.addCallParam(shellServerPath, 0, A_ENABLED);
        digester.addCallParam(shellServerPath, 1, A_PORT);

        String detailPageHandlerPath = "*/" + N_SYSTEM + "/" + N_DETAIL_PAGE_HANDLER;
        digester.addObjectCreate(detailPageHandlerPath, CmsDefaultDetailPageHandler.class.getName(), A_CLASS);
        digester.addSetNext(detailPageHandlerPath, "setDetailPageHandler");

        String userdataPath = "*/" + N_SYSTEM + "/" + CmsUserDataRequestManager.N_USERDATA;
        CmsUserDataRequestManager.addDigesterRules(digester, userdataPath);
        digester.addSetNext(userdataPath, "setUserDataRequestManager");
    }

    /**
     * @see org.opencms.configuration.I_CmsXmlConfiguration#generateXml(org.dom4j.Element)
     */
    public Element generateXml(Element parent) {

        // generate system node and sub notes
        Element systemElement = parent.addElement(N_SYSTEM);

        if (OpenCms.getRunLevel() >= OpenCms.RUNLEVEL_3_SHELL_ACCESS) {
            // initialized OpenCms instance is available, use latest values
            m_localeManager = OpenCms.getLocaleManager();
            m_mailSettings = OpenCms.getSystemInfo().getMailSettings();
            m_historyEnabled = OpenCms.getSystemInfo().isHistoryEnabled();
            m_historyVersions = OpenCms.getSystemInfo().getHistoryVersions();
            m_historyVersionsAfterDeletion = OpenCms.getSystemInfo().getHistoryVersionsAfterDeletion();
            // m_resourceInitHandlers instance must be the one from configuration
            // m_requestHandlers instance must be the one from configuration
            m_loginManager = OpenCms.getLoginManager();
        }

        // i18n nodes
        Element i18nElement = systemElement.addElement(N_I18N);
        i18nElement.addElement(N_LOCALEHANDLER).addAttribute(
            A_CLASS,
            m_localeManager.getLocaleHandler().getClass().getName());
        Iterator<Locale> loc;
        Element localesElement;
        localesElement = i18nElement.addElement(N_LOCALESCONFIGURED);
        loc = m_localeManager.getAvailableLocales().iterator();
        while (loc.hasNext()) {
            localesElement.addElement(N_LOCALE).addText(loc.next().toString());
        }
        localesElement = i18nElement.addElement(N_LOCALESDEFAULT);
        loc = m_localeManager.getDefaultLocales().iterator();
        while (loc.hasNext()) {
            localesElement.addElement(N_LOCALE).setText(loc.next().toString());
        }
        i18nElement.addElement(N_TIMEZONE).setText(m_localeManager.getTimeZone().getID());
        if (null != m_localeManager.getReuseElementsStr()) {
            i18nElement.addElement(N_REUSE_ELEMENTS).setText(m_localeManager.getReuseElementsStr());
        }

        // mail nodes
        Element mailElement = systemElement.addElement(N_MAIL);
        mailElement.addElement(N_MAILFROM).setText(m_mailSettings.getMailFromDefault());
        Iterator<CmsMailHost> hosts = m_mailSettings.getMailHosts().iterator();
        while (hosts.hasNext()) {
            CmsMailHost host = hosts.next();
            Element hostElement = mailElement.addElement(N_MAILHOST).addAttribute(A_ID, host.getId()).addAttribute(
                A_NAME,
                host.getHostname()).addAttribute(A_PORT, Integer.toString(host.getPort())).addAttribute(
                    A_ORDER,
                    host.getOrder().toString()).addAttribute(A_PROTOCOL, host.getProtocol()).addAttribute(
                        A_SECURITY,
                        host.getSecurity()).addAttribute(A_MAILFROM, host.getMailfrom());
            if (host.isAuthenticating()) {
                hostElement.addAttribute(A_USER, host.getUsername()).addAttribute(A_PASSWORD, host.getPassword());
            }
        }

        // scheduler node

        // <events> node
        Element eventsElement = systemElement.addElement(N_EVENTS);
        Element eventManagerElement = eventsElement.addElement(N_EVENTMANAGER);
        eventManagerElement.addAttribute(A_CLASS, m_eventManager.getClass().getName());

        // version history
        Element historyElement = systemElement.addElement(N_VERSIONHISTORY);
        historyElement.addAttribute(A_ENABLED, String.valueOf(m_historyEnabled));
        historyElement.addAttribute(A_COUNT, Integer.valueOf(m_historyVersions).toString());
        historyElement.addAttribute(A_DELETED, Integer.valueOf(m_historyVersionsAfterDeletion).toString());

        // resourceinit
        Element resourceinitElement = systemElement.addElement(N_RESOURCEINIT);
        Iterator<I_CmsResourceInit> resHandlers = m_resourceInitHandlers.iterator();
        while (resHandlers.hasNext()) {
            I_CmsResourceInit handler = resHandlers.next();
            Element handlerElement = resourceinitElement.addElement(N_RESOURCEINITHANDLER);
            handlerElement.addAttribute(A_CLASS, handler.getClass().getName());
            CmsParameterConfiguration config = handler.getConfiguration();
            if (config != null) {
                for (String key : config.keySet()) {
                    handlerElement.addElement(N_PARAM).addAttribute(A_NAME, key).addText(config.get(key));
                }
            }
        }

        // request handlers
        Element requesthandlersElement = systemElement.addElement(N_REQUESTHANDLERS);
        Iterator<I_CmsRequestHandler> reqHandlers = m_requestHandlers.iterator();
        while (reqHandlers.hasNext()) {
            I_CmsRequestHandler handler = reqHandlers.next();
            Element handlerElement = requesthandlersElement.addElement(N_REQUESTHANDLER);
            handlerElement.addAttribute(A_CLASS, handler.getClass().getName());
            CmsParameterConfiguration config = handler.getConfiguration();
            if (config != null) {
                for (String key : config.keySet()) {
                    handlerElement.addElement(N_PARAM).addAttribute(A_NAME, key).addText(config.get(key));
                }
            }
        }

        // password handler
        Element passwordhandlerElement = systemElement.addElement(N_PASSWORDHANDLER).addAttribute(
            A_CLASS,
            m_passwordHandler.getClass().getName());
        passwordhandlerElement.addElement(N_PASSWORDENCODING).addText(m_passwordHandler.getInputEncoding());
        passwordhandlerElement.addElement(N_DIGESTTYPE).addText(m_passwordHandler.getDigestType());
        CmsParameterConfiguration handlerParameters = m_passwordHandler.getConfiguration();
        if (handlerParameters != null) {
            handlerParameters.appendToXml(passwordhandlerElement);
        }

        // validation handler
        if (m_validationHandler != null) {
            Element valHandlerElem = systemElement.addElement(N_VALIDATIONHANDLER);
            valHandlerElem.addAttribute(A_CLASS, m_validationHandler);
        }

        // login manager
        if (m_loginManager != null) {
            Element managerElement = systemElement.addElement(N_LOGINMANAGER);
            managerElement.addElement(N_DISABLEMINUTES).addText(String.valueOf(m_loginManager.getDisableMinutes()));
            managerElement.addElement(N_MAXBADATTEMPTS).addText(String.valueOf(m_loginManager.getMaxBadAttempts()));
            managerElement.addElement(N_ENABLESCURITY).addText(String.valueOf(m_loginManager.isEnableSecurity()));
            String tokenLifetimeStr = m_loginManager.getTokenLifetimeStr();
            if (tokenLifetimeStr != null) {
                managerElement.addElement(N_TOKEN_LIFETIME).addText(tokenLifetimeStr);
            }
            if (m_loginManager.getMaxInactive() != null) {
                managerElement.addElement(N_MAX_INACTIVE_TIME).addText(m_loginManager.getMaxInactive());
            }

            if (m_loginManager.getPasswordChangeIntervalStr() != null) {
                managerElement.addElement(N_PASSWORD_CHANGE_INTERVAL).addText(
                    m_loginManager.getPasswordChangeIntervalStr());
            }

            if (m_loginManager.getUserDataCheckIntervalStr() != null) {
                managerElement.addElement(N_USER_DATA_CHECK_INTERVAL).addText(
                    m_loginManager.getUserDataCheckIntervalStr());
            }
            if (m_loginManager.isOrgUnitRequired()) {
                managerElement.addElement(N_REQUIRE_ORGUNIT).addText("true");
            }

            if (m_loginManager.getLogoutUri() != null) {
                managerElement.addElement(N_LOGOUT_URI).addText(m_loginManager.getLogoutUri());
            }
        }
        if (m_twoFactorConfig != null) {
            // the 2FA configuration is immutable, so we can just reuse the original element here
            systemElement.add(m_twoFactorConfig.getConfigElement());
        }

        Element saxImpl = systemElement.addElement(N_SAX_IMPL_SYSTEM_PROPERTIES);
        saxImpl.setText(String.valueOf(m_saxImplProperties));

        // create <runtimeproperties> node
        Element runtimepropertiesElement = systemElement.addElement(N_RUNTIMEPROPERTIES);
        if (m_runtimeProperties != null) {
            List<String> sortedRuntimeProperties = new ArrayList<String>(m_runtimeProperties.keySet());
            Collections.sort(sortedRuntimeProperties);
            Iterator<String> it = sortedRuntimeProperties.iterator();
            while (it.hasNext()) {
                String key = it.next();
                // create <param name="">value</param> subnodes
                runtimepropertiesElement.addElement(N_PARAM).addAttribute(A_NAME, key).addText(
                    m_runtimeProperties.get(key));
            }
        }

        // create <runtimeinfo> node
        Element runtimeinfoElement = systemElement.addElement(N_RUNTIMECLASSES);
        Element runtimeinfofactoryElement = runtimeinfoElement.addElement(N_RUNTIMEINFO);
        runtimeinfofactoryElement.addAttribute(A_CLASS, getRuntimeInfoFactory().getClass().getName());

        // create <defaultusers> node
        Element defaultusersElement = systemElement.addElement(N_DEFAULTUSERS);
        // create <user-admin> subnode
        defaultusersElement.addElement(N_USER_ADMIN).addText(m_cmsDefaultUsers.getUserAdmin());
        // create <user-guest> subnode
        defaultusersElement.addElement(N_USER_GUEST).addText(m_cmsDefaultUsers.getUserGuest());
        // create <user-export> subnode
        defaultusersElement.addElement(N_USER_EXPORT).addText(m_cmsDefaultUsers.getUserExport());
        if (!m_cmsDefaultUsers.getUserDeletedResource().equals(m_cmsDefaultUsers.getUserAdmin())) {
            // create <user-deletedresource> subnode
            defaultusersElement.addElement(N_USER_DELETEDRESOURCE).addText(m_cmsDefaultUsers.getUserDeletedResource());
        }
        // create <group-administrators> subnode
        defaultusersElement.addElement(N_GROUP_ADMINISTRATORS).addText(m_cmsDefaultUsers.getGroupAdministrators());
        // create <group-users> subnode
        defaultusersElement.addElement(N_GROUP_USERS).addText(m_cmsDefaultUsers.getGroupUsers());
        // create <group-guests> subnode
        defaultusersElement.addElement(N_GROUP_GUESTS).addText(m_cmsDefaultUsers.getGroupGuests());

        // create <defaultcontentencoding> node
        systemElement.addElement(N_DEFAULT_CONTENT_ENCODING).addText(getDefaultContentEncoding());

        // create <memorymonitor> node
        if (m_cmsMemoryMonitorConfiguration != null) {
            Element memorymonitorElement = systemElement.addElement(N_MEMORYMONITOR);
            if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(m_cmsMemoryMonitorConfiguration.getClassName())) {
                memorymonitorElement.addAttribute(A_CLASS, m_cmsMemoryMonitorConfiguration.getClassName());
            }

            memorymonitorElement.addElement(N_MAXUSAGE_PERCENT).addText(
                String.valueOf(m_cmsMemoryMonitorConfiguration.getMaxUsagePercent()));

            memorymonitorElement.addElement(N_LOG_INTERVAL).addText(
                String.valueOf(m_cmsMemoryMonitorConfiguration.getLogInterval()));

            if (m_cmsMemoryMonitorConfiguration.getEmailInterval() >= 0) {
                memorymonitorElement.addElement(N_EMAIL_INTERVAL).addText(
                    String.valueOf(m_cmsMemoryMonitorConfiguration.getEmailInterval()));
            }

            memorymonitorElement.addElement(N_WARNING_INTERVAL).addText(
                String.valueOf(m_cmsMemoryMonitorConfiguration.getWarningInterval()));

            if (m_cmsMemoryMonitorConfiguration.getEmailSender() != null) {
                memorymonitorElement.addElement(N_EMAIL_SENDER).addText(
                    m_cmsMemoryMonitorConfiguration.getEmailSender());
            }
            List<String> emailReceiver = m_cmsMemoryMonitorConfiguration.getEmailReceiver();
            if (!emailReceiver.isEmpty()) {
                Element emailreceiverElement = memorymonitorElement.addElement(N_EMAIL_RECEIVER);
                Iterator<String> iter = emailReceiver.iterator();
                while (iter.hasNext()) {
                    emailreceiverElement.addElement(N_RECEIVER).addText(iter.next());
                }
            }
        }

        // create <flexcache> node
        Element flexcacheElement = systemElement.addElement(N_FLEXCACHE);
        flexcacheElement.addElement(N_CACHE_ENABLED).addText(
            String.valueOf(m_cmsFlexCacheConfiguration.isCacheEnabled()));
        flexcacheElement.addElement(N_CACHE_OFFLINE).addText(
            String.valueOf(m_cmsFlexCacheConfiguration.isCacheOffline()));
        flexcacheElement.addElement(N_MAXCACHEBYTES).addText(
            String.valueOf(m_cmsFlexCacheConfiguration.getMaxCacheBytes()));
        flexcacheElement.addElement(N_AVGCACHEBYTES).addText(
            String.valueOf(m_cmsFlexCacheConfiguration.getAvgCacheBytes()));
        flexcacheElement.addElement(N_MAXENTRYBYTES).addText(
            String.valueOf(m_cmsFlexCacheConfiguration.getMaxEntryBytes()));
        flexcacheElement.addElement(N_MAXKEYS).addText(String.valueOf(m_cmsFlexCacheConfiguration.getMaxKeys()));
        if (m_cmsFlexCacheConfiguration.getDeviceSelectorConfiguration() != null) {
            Element flexcacheDeviceSelectorElement = flexcacheElement.addElement(N_DEVICESELECTOR);
            flexcacheDeviceSelectorElement.addAttribute(
                A_CLASS,
                m_cmsFlexCacheConfiguration.getDeviceSelectorConfiguration());
        }

        // create <http-authentication> node
        Element httpAuthenticationElement = systemElement.addElement(N_HTTP_AUTHENTICATION);
        httpAuthenticationElement.addElement(N_BROWSER_BASED).setText(
            m_httpAuthenticationSettings.getConfigBrowserBasedAuthentication());
        if (m_httpAuthenticationSettings.getFormBasedHttpAuthenticationUri() != null) {
            httpAuthenticationElement.addElement(N_FORM_BASED).setText(
                m_httpAuthenticationSettings.getFormBasedHttpAuthenticationUri());
        }

        // cache settings
        Element cacheElement = systemElement.addElement(N_RESULTCACHE);
        cacheElement.addElement(N_KEYGENERATOR).setText(m_cacheSettings.getCacheKeyGenerator());
        cacheElement.addElement(N_SIZE_USERS).setText(Integer.toString(m_cacheSettings.getUserCacheSize()));
        cacheElement.addElement(N_SIZE_GROUPS).setText(Integer.toString(m_cacheSettings.getGroupCacheSize()));
        if (m_cacheSettings.getConfiguredOrgUnitCacheSize() > -1) {
            cacheElement.addElement(N_SIZE_ORGUNITS).setText(
                Integer.toString(m_cacheSettings.getConfiguredOrgUnitCacheSize()));
        }
        cacheElement.addElement(N_SIZE_USERGROUPS).setText(Integer.toString(m_cacheSettings.getUserGroupsCacheSize()));
        cacheElement.addElement(N_SIZE_PROJECTS).setText(Integer.toString(m_cacheSettings.getProjectCacheSize()));
        if (m_cacheSettings.getConfiguredProjectResourcesCacheSize() > -1) {
            cacheElement.addElement(N_SIZE_PROJECTRESOURCES).setText(
                Integer.toString(m_cacheSettings.getConfiguredProjectResourcesCacheSize()));
        }
        cacheElement.addElement(N_SIZE_RESOURCES).setText(Integer.toString(m_cacheSettings.getResourceCacheSize()));
        if (m_cacheSettings.getConfiguredRolesCacheSize() > -1) {
            cacheElement.addElement(N_SIZE_ROLES).setText(
                Integer.toString(m_cacheSettings.getConfiguredRolesCacheSize()));
        }
        cacheElement.addElement(N_SIZE_RESOURCELISTS).setText(
            Integer.toString(m_cacheSettings.getResourcelistCacheSize()));
        cacheElement.addElement(N_SIZE_PROPERTIES).setText(Integer.toString(m_cacheSettings.getPropertyCacheSize()));
        if (m_cacheSettings.getConfiguredPropertyListsCacheSize() > -1) {
            cacheElement.addElement(N_SIZE_PROPERTYLISTS).setText(
                Integer.toString(m_cacheSettings.getConfiguredPropertyListsCacheSize()));
        }
        cacheElement.addElement(N_SIZE_ACLS).setText(Integer.toString(m_cacheSettings.getAclCacheSize()));
        cacheElement.addElement(N_SIZE_PERMISSIONS).setText(Integer.toString(m_cacheSettings.getPermissionCacheSize()));

        // content notification settings
        if ((m_notificationTime != null) || (m_notificationProject != null)) {
            Element notificationElement = systemElement.addElement(N_CONTENT_NOTIFICATION);
            if (m_notificationTime != null) {
                notificationElement.addElement(N_NOTIFICATION_TIME).setText(m_notificationTime.toString());
            }
            if (m_notificationProject != null) {
                notificationElement.addElement(N_NOTIFICATION_PROJECT).setText(m_notificationProject);
            }
        }

        // authorization handler
        if (m_authorizationHandler != null) {
            Element authorizationHandlerElem = systemElement.addElement(N_AUTHORIZATIONHANDLER);
            authorizationHandlerElem.addAttribute(A_CLASS, m_authorizationHandler);
        }

        if (m_apiAuthorizations.size() > 0) {
            Element authsElement = systemElement.addElement(N_API_AUTHORIZATIONS);
            for (ApiAuthorizationConfig apiAuth : m_apiAuthorizations) {
                apiAuth.fillXml(authsElement.addElement(N_API_AUTHORIZATION));
            }
        }

        Element encryptionElement = systemElement.addElement(N_ENCRYPTION);
        for (I_CmsTextEncryption encrypter : m_textEncryptions.values()) {
            Element textEncryption = encryptionElement.addElement(N_TEXT_ENCRYPTION);
            textEncryption.addAttribute(A_CLASS, encrypter.getClass().getName());
            textEncryption.addAttribute(A_NAME, encrypter.getName());
            CmsParameterConfiguration config = encrypter.getConfiguration();
            for (Map.Entry<String, String> entry : config.entrySet()) {
                textEncryption.addElement(N_PARAM).addAttribute(A_NAME, entry.getKey()).addText(entry.getValue());
            }
        }

        // optional publish manager nodes
        if (m_publishManager != null) {
            Element pubHistElement = systemElement.addElement(N_PUBLISHMANAGER);
            pubHistElement.addElement(N_HISTORYSIZE).setText(String.valueOf(m_publishManager.getPublishHistorySize()));
            // optional nodes for publish queue
            pubHistElement.addElement(N_QUEUEPERSISTANCE).setText(
                String.valueOf(m_publishManager.isPublishQueuePersistanceEnabled()));
            pubHistElement.addElement(N_QUEUESHUTDOWNTIME).setText(
                String.valueOf(m_publishManager.getPublishQueueShutdowntime()));
            pubHistElement.addElement(N_AUTO_CLEANUP_HISTORY_ENTRIES).setText(
                String.valueOf(m_publishManager.isAutoCleanupHistoryEntries()));
        }

        // session storage provider
        if (m_sessionStorageProvider != null) {
            Element sessionStorageProviderElem = systemElement.addElement(N_SESSION_STORAGEPROVIDER);
            sessionStorageProviderElem.addAttribute(A_CLASS, m_sessionStorageProvider);
        }

        // permission handler
        if (m_permissionHandler != null) {
            Element permissionHandlerElem = systemElement.addElement(N_PERMISSIONHANDLER);
            permissionHandlerElem.addAttribute(A_CLASS, m_permissionHandler);
        }

        // servlet container settings
        CmsServletContainerSettings servletContainerSettings = OpenCms.getSystemInfo().getServletContainerSettings();
        if (!servletContainerSettings.getMode().isNone()) {
            Element servletContainerSettingsElem = systemElement.addElement(N_SERVLETCONTAINERSETTINGS);
            servletContainerSettingsElem.addAttribute(A_MODE, servletContainerSettings.getMode().getMode());
            if (!servletContainerSettings.getMode().isAuto()) {
                servletContainerSettingsElem.addElement(N_PREVENTRESPONSEFLUSH).addText(
                    "" + servletContainerSettings.isPreventResponseFlush());
                servletContainerSettingsElem.addElement(N_RELEASETAGSAFTEREND).addText(
                    "" + servletContainerSettings.isReleaseTagsAfterEnd());
            }
            // always write back the error page attribute
            if (servletContainerSettings.getRequestErrorPageAttribute() != null) {
                servletContainerSettingsElem.addElement(N_REQUESTERRORPAGEATTRIBUTE).addText(
                    servletContainerSettings.getRequestErrorPageAttribute());
            }
        }

        // ADE settings
        if ((getAdeConfiguration() != null) || (getAdeCacheSettings() != null) || !m_adeParameters.isEmpty()) {
            Element adeElem = systemElement.addElement(N_ADE);
            if (getAdeConfiguration() != null) {
                adeElem.addElement(N_CONFIGURATION).addAttribute(A_CLASS, getAdeConfiguration());
            }
            if (!m_adeParameters.isEmpty()) {
                Element paramsElement = adeElem.addElement(N_PARAMETERS);
                for (Map.Entry<String, String> entry : m_adeParameters.entrySet()) {
                    String name = entry.getKey();
                    String value = entry.getValue();
                    Element paramElement = paramsElement.addElement(N_PARAM);
                    paramElement.addAttribute(N_NAME, name);
                    paramElement.setText(value);
                }
            }
            if (getAdeCacheSettings() != null) {
                Element cacheElem = adeElem.addElement(N_ADE_CACHE);
                // container page cache
                Element cntPageCacheElem = cacheElem.addElement(N_CONTAINERPAGES);
                cntPageCacheElem.addAttribute(A_OFFLINE, "" + getAdeCacheSettings().getContainerPageOfflineSize());
                cntPageCacheElem.addAttribute(A_ONLINE, "" + getAdeCacheSettings().getContainerPageOnlineSize());
                // group-container cache
                Element groupContainerCacheElem = cacheElem.addElement(N_GROUPCONTAINERS);
                groupContainerCacheElem.addAttribute(
                    A_OFFLINE,
                    "" + getAdeCacheSettings().getGroupContainerOfflineSize());
                groupContainerCacheElem.addAttribute(
                    A_ONLINE,
                    "" + getAdeCacheSettings().getGroupContainerOnlineSize());
            }
        }

        // subscription manager settings
        if (getSubscriptionManager() != null) {
            Element subscrManElem = systemElement.addElement(N_SUBSCRIPTIONMANAGER);
            subscrManElem.addAttribute(A_ENABLED, Boolean.toString(getSubscriptionManager().isEnabled()));
            subscrManElem.addAttribute(A_POOLNAME, getSubscriptionManager().getPoolName());
            subscrManElem.addAttribute(A_MAXVISITED, String.valueOf(getSubscriptionManager().getMaxVisitedCount()));
        }

        I_CmsWorkflowManager workflowMan = getWorkflowManager();
        if (workflowMan != null) {
            Element workflowElem = systemElement.addElement(N_WORKFLOW);
            workflowElem.addAttribute(A_CLASS, workflowMan.getClass().getName());
            Map<String, String> parameters = workflowMan.getParameters();
            Element parametersElem = workflowElem.addElement(N_PARAMETERS);
            for (Map.Entry<String, String> entry : parameters.entrySet()) {
                Element paramElem = parametersElem.addElement(N_PARAM);
                paramElem.addAttribute(A_NAME, entry.getKey());
                paramElem.addText(entry.getValue());
            }
        }

        if (m_userSessionMode != null) {
            Element userSessionElem = systemElement.addElement(N_USER_SESSION_MODE);
            userSessionElem.setText(m_userSessionMode.toString());
        }

        if (m_credentialsResolverClass != null) {
            systemElement.addElement(N_CREDENTIALS_RESOLVER).setText(m_credentialsResolverClass);
        }

        if (m_publishListRemoveMode != null) {
            systemElement.addElement(N_PUBLISH_LIST_REMOVE_MODE).addAttribute(A_MODE, m_publishListRemoveMode);
        }

        if (m_detailPageHandler != null) {
            Element handlerElement = systemElement.addElement(N_DETAIL_PAGE_HANDLER).addAttribute(
                A_CLASS,
                m_detailPageHandler.getClass().getName());
            CmsParameterConfiguration config = m_detailPageHandler.getConfiguration();
            if (config != null) {
                for (String key : config.keySet()) {
                    handlerElement.addElement(N_PARAM).addAttribute(A_NAME, key).addText(config.get(key));
                }
            }
        }

        if (m_restrictDetailContents != null) {
            Element restrictDetailContentsElem = systemElement.addElement(N_RESTRICT_DETAIL_CONTENTS);
            restrictDetailContentsElem.addText(m_restrictDetailContents);
        }

        if (m_shellServerOptions != null) {
            systemElement.addElement(N_SHELL_SERVER).addAttribute(
                A_ENABLED,
                "" + m_shellServerOptions.isEnabled()).addAttribute(A_PORT, "" + m_shellServerOptions.getPort());
        }
        CmsLetsEncryptConfiguration.CONFIG_HELPER.generateXml(systemElement, m_letsEncryptConfig);

        if (m_userDataRequestManager != null) {
            m_userDataRequestManager.appendToXml(systemElement);
        }

        // return the system node
        return systemElement;
    }

    /**
     * Returns the settings of the ADE cache.<p>
     *
     * @return the settings of the ADE cache
     */
    public CmsADECacheSettings getAdeCacheSettings() {

        return m_adeCacheSettings;
    }

    /**
     * Returns the ade configuration class name.<p>
     *
     * @return the ade configuration class name
     */
    public String getAdeConfiguration() {

        return m_adeConfiguration;
    }

    /**
     * Gets the ADE configuration parameters.<p>
     *
     * @return the ADE configuration parameters
     */
    public Map<String, String> getAdeParameters() {

        return m_adeParameters;
    }

    /**
     * Gets the map of API authorization handlers (with names as keys).
     *
     * @return the map of API authorization handlers
     */
    public Map<String, I_CmsApiAuthorizationHandler> getApiAuthorizations() {

        return m_apiAuthorizationMap;
    }

    /**
     * Returns an instance of the configured authorization handler.<p>
     *
     * @return an instance of the configured authorization handler
     */
    public I_CmsAuthorizationHandler getAuthorizationHandler() {

        if (CmsStringUtil.isEmptyOrWhitespaceOnly(m_authorizationHandler)) {
            return new CmsDefaultAuthorizationHandler();
        }
        try {
            I_CmsAuthorizationHandler authorizationHandler = (I_CmsAuthorizationHandler)Class.forName(
                m_authorizationHandler).newInstance();
            if (LOG.isInfoEnabled()) {
                LOG.info(
                    Messages.get().getBundle().key(
                        Messages.INIT_AUTHORIZATION_HANDLER_CLASS_SUCCESS_1,
                        m_authorizationHandler));
            }
            authorizationHandler.setParameters(new HashMap<>(m_authHandlerParams));
            return authorizationHandler;
        } catch (Throwable t) {
            LOG.error(
                Messages.get().getBundle().key(
                    Messages.INIT_AUTHORIZATION_HANDLER_CLASS_INVALID_1,
                    m_authorizationHandler),
                t);
            return new CmsDefaultAuthorizationHandler();
        }
    }

    /**
     * Returns the settings of the memory monitor.<p>
     *
     * @return the settings of the memory monitor
     */
    public CmsCacheSettings getCacheSettings() {

        return m_cacheSettings;
    }

    /**
     * Returns the default users.<p>
     *
     * @return the default users
     */
    public CmsDefaultUsers getCmsDefaultUsers() {

        return m_cmsDefaultUsers;
    }

    /**
     * Returns the flexCacheConfiguration.<p>
     *
     * @return the flexCacheConfiguration
     */
    public CmsFlexCacheConfiguration getCmsFlexCacheConfiguration() {

        return m_cmsFlexCacheConfiguration;
    }

    /**
     * Returns the memory monitor configuration.<p>
     *
     * @return the memory monitor configuration
     */
    public CmsMemoryMonitorConfiguration getCmsMemoryMonitorConfiguration() {

        return m_cmsMemoryMonitorConfiguration;
    }

    /**
     * Gets the credentials resolver.<p>
     *
     * @return the credentials resolver
     */
    public I_CmsCredentialsResolver getCredentialsResolver() {

        if (m_credentialsResolver == null) {
            m_credentialsResolver = new CmsDefaultCredentialsResolver();
        }
        return m_credentialsResolver;
    }

    /**
     * Gets the configured credentials resolver class name (null if no class is explicity configured).<p>
     *
     * @return the name of the configured credentials resolver class
     */
    public String getCredentialsResolverClass() {

        return m_credentialsResolverClass;
    }

    /**
     * Returns the defaultContentEncoding.<p>
     *
     * @return the defaultContentEncoding
     */
    public String getDefaultContentEncoding() {

        return m_defaultContentEncoding;
    }

    /**
     * Gets the detail page handler.
     *
     * @return the detail page handler
     */
    public I_CmsDetailPageHandler getDetailPageHandler() {

        return m_detailPageHandler;
    }

    /**
     * @see org.opencms.configuration.I_CmsXmlConfiguration#getDtdFilename()
     */
    public String getDtdFilename() {

        return CONFIGURATION_DTD_NAME;
    }

    /**
     * Returns the configured OpenCms event manager instance.<p>
     *
     * @return the configured OpenCms event manager instance
     */
    public CmsEventManager getEventManager() {

        return m_eventManager;
    }

    /**
     * Returns the maximum number of versions that are kept per resource in the VFS version history.<p>
     *
     * If the version history is disabled, this setting has no effect.<p>
     *
     * @return the maximum number of versions that are kept per resource
     *
     * @see #isHistoryEnabled()
     */
    public int getHistoryVersions() {

        return m_historyVersions;
    }

    /**
     * Returns the maximum number of versions that are kept in the VFS version history for deleted resources.<p>
     *
     * If the version history is disabled, this setting has no effect.<p>
     *
     * @return the maximum number of versions that are kept for deleted resources
     *
     * @see #isHistoryEnabled()
     */
    public int getHistoryVersionsAfterDeletion() {

        return m_historyVersionsAfterDeletion;
    }

    /**
     * Returns the HTTP authentication settings.<p>
     *
     * @return the HTTP authentication settings
     */
    public CmsHttpAuthenticationSettings getHttpAuthenticationSettings() {

        return m_httpAuthenticationSettings;
    }

    /**
     * Gets the LetsEncrypt configuration.<p>
     *
     * @return the LetsEncrypt configuration
     */
    public CmsLetsEncryptConfiguration getLetsEncryptConfig() {

        return m_letsEncryptConfig;
    }

    /**
     * Returns the configured locale manager for multi language support.<p>
     *
     * @return the configured locale manager for multi language support
     */
    public CmsLocaleManager getLocaleManager() {

        return m_localeManager;
    }

    /**
     * Returns the configured login manager.<p>
     *
     * @return the configured login manager
     */
    public CmsLoginManager getLoginManager() {

        if (m_loginManager == null) {
            // no login manager configured, create default
            m_loginManager = new CmsLoginManager(
                CmsLoginManager.DISABLE_MINUTES_DEFAULT,
                CmsLoginManager.MAX_BAD_ATTEMPTS_DEFAULT,
                CmsLoginManager.ENABLE_SECURITY_DEFAULT,
                null,
                null,
                null,
                null,
                false,
                null);
        }
        return m_loginManager;
    }

    /**
     * Returns the configured mail settings.<p>
     *
     * @return the configured mail settings
     */
    public CmsMailSettings getMailSettings() {

        return m_mailSettings;
    }

    /**
     * Returns the project in which timestamps for the content notification are read.<p>
     *
     * @return the project in which timestamps for the content notification are read
     */
    public String getNotificationProject() {

        return m_notificationProject;
    }

    /**
     * Returns the duration after which responsibles will be notified about out-dated content (in days).<p>
     *
     * @return the duration after which responsibles will be notified about out-dated content
     */
    public int getNotificationTime() {

        if (m_notificationTime != null) {
            return m_notificationTime.intValue();
        } else {
            return -1;
        }
    }

    /**
     * Returns the configured password handler.<p>
     *
     * @return the configured password handler
     */
    public I_CmsPasswordHandler getPasswordHandler() {

        return m_passwordHandler;
    }

    /**
     * Returns the permission Handler class name.<p>
     *
     * @return the permission Handler class name
     */
    public String getPermissionHandler() {

        return m_permissionHandler;
    }

    /**
     * Returns the configured publish list remove mode, or a default value if there is no configured value or an erroneous configured value.<p>
     *
     * @return the publish list remove mode
     */
    public CmsPublishManager.PublishListRemoveMode getPublishListRemoveMode() {

        try {
            // trim preserves null
            return CmsPublishManager.PublishListRemoveMode.valueOf(StringUtils.trim(m_publishListRemoveMode));
        } catch (Exception e) {
            return CmsPublishManager.PublishListRemoveMode.allUsers;
        }
    }

    /**
     * Returns the configured publish list remove mode as a string, or null if no publish list remove mode has been configured.<p>
     *
     * @return the publish list remove mode string from the configuration
     */
    public String getPublishListRemoveModeStr() {

        return m_publishListRemoveMode;
    }

    /**
     * Returns the configured publish manager.<p>
     *
     * @return the configured publish manager
     */
    public CmsPublishManager getPublishManager() {

        if (m_publishManager == null) {
            // no publish manager configured, create default
            m_publishManager = new CmsPublishManager(
                CmsPublishManager.DEFAULT_HISTORY_SIZE,
                CmsPublishManager.DEFAULT_QUEUE_PERSISTANCE,
                CmsPublishManager.DEFAULT_QUEUE_SHUTDOWNTIME);
        }
        return m_publishManager;
    }

    /**
     * Returns the list of instantiated request handler classes.<p>
     *
     * @return the list of instantiated request handler classes
     */
    public List<I_CmsRequestHandler> getRequestHandlers() {

        return m_requestHandlers;
    }

    /**
     * Returns the list of instantiated resource init handler classes.<p>
     *
     * @return the list of instantiated resource init handler classes
     */
    public List<I_CmsResourceInit> getResourceInitHandlers() {

        return m_resourceInitHandlers;
    }

    /**
     * Returns the runtime info factory instance.<p>
     *
     * @return the runtime info factory instance
     */
    public I_CmsDbContextFactory getRuntimeInfoFactory() {

        return m_runtimeInfoFactory;
    }

    /**
     * Returns the runtime Properties.<p>
     *
     * @return the runtime Properties
     */
    public Map<String, String> getRuntimeProperties() {

        return m_runtimeProperties;
    }

    /**
     * Returns an instance of the configured session storage provider.<p>
     *
     * @return an instance of the configured session storage provider
     */
    public I_CmsSessionStorageProvider getSessionStorageProvider() {

        if (CmsStringUtil.isEmptyOrWhitespaceOnly(m_sessionStorageProvider)) {
            return new CmsDefaultSessionStorageProvider();
        }
        try {
            I_CmsSessionStorageProvider sessionCacheProvider = (I_CmsSessionStorageProvider)Class.forName(
                m_sessionStorageProvider).newInstance();
            if (CmsLog.INIT.isInfoEnabled()) {
                CmsLog.INIT.info(
                    Messages.get().getBundle().key(
                        Messages.INIT_SESSION_STORAGEPROVIDER_SUCCESS_1,
                        m_sessionStorageProvider));
            }
            return sessionCacheProvider;
        } catch (Throwable t) {
            LOG.error(
                Messages.get().getBundle().key(
                    Messages.LOG_INIT_SESSION_STORAGEPROVIDER_FAILURE_1,
                    m_sessionStorageProvider),
                t);
            return new CmsDefaultSessionStorageProvider();
        }
    }

    /**
     * Returns the shell server options.<p>
     *
     * @return the shell server options
     */
    public CmsRemoteShellConfiguration getShellServerOptions() {

        return m_shellServerOptions;
    }

    /**
     * Returns the configured subscription manager.<p>
     *
     * @return the configured subscription manager
     */
    public CmsSubscriptionManager getSubscriptionManager() {

        if (m_subscriptionManager == null) {
            // no subscription manager configured, create default
            m_subscriptionManager = new CmsSubscriptionManager();
        }
        return m_subscriptionManager;
    }

    /**
     * Returns temporary file project id.<p>
     *
     * @return temporary file project id
     */
    public int getTempFileProjectId() {

        return m_tempFileProjectId;
    }

    /**
     * Gets the map of text encryptions.
     *
     * @return the map of text encryptions
     */
    public Map<String, I_CmsTextEncryption> getTextEncryptions() {

        return Collections.unmodifiableMap(m_textEncryptions);
    }

    /**
     * Gets the two-factor authentication configuration.
     *
     * @return the two-factor auth configuration
     */
    public CmsTwoFactorAuthenticationConfig getTwoFactorAuthenticationConfig() {

        return m_twoFactorConfig;
    }

    /**
     * Gets the user data request manager.
     *
     * @return the user data request manager
     */
    public CmsUserDataRequestManager getUserDataRequestManager() {

        return m_userDataRequestManager;
    }

    /**
     * Gets the user session mode.<p>
     *
     * @param useDefault if true, and no user session mode was configured, this will return the default value
     *
     * @return the user session mode
     */
    public UserSessionMode getUserSessionMode(boolean useDefault) {

        if (m_userSessionMode != null) {
            return m_userSessionMode;
        } else if (useDefault) {
            return DEFAULT_USER_SESSION_MODE;
        } else {
            return null;
        }
    }

    /**
     * Returns an instance of the configured validation handler.<p>
     *
     * @return an instance of the configured validation handler
     */
    public I_CmsValidationHandler getValidationHandler() {

        if (CmsStringUtil.isEmptyOrWhitespaceOnly(m_validationHandler)) {
            return new CmsDefaultValidationHandler();
        }
        try {
            I_CmsValidationHandler validationHandler = (I_CmsValidationHandler)Class.forName(
                m_validationHandler).newInstance();
            if (LOG.isInfoEnabled()) {
                LOG.info(
                    Messages.get().getBundle().key(
                        Messages.INIT_VALIDATION_HANDLER_CLASS_SUCCESS_1,
                        m_validationHandler));
            }
            return validationHandler;
        } catch (Throwable t) {
            LOG.error(
                Messages.get().getBundle().key(Messages.INIT_VALIDATION_HANDLER_CLASS_INVALID_1, m_validationHandler),
                t);
            return new CmsDefaultValidationHandler();
        }
    }

    /**
     * Gets the configured workflow manager instance.<p>
     *
     * @return the configured workflow manager instance.
     */
    public I_CmsWorkflowManager getWorkflowManager() {

        return m_workflowManager;
    }

    /**
     * Will be called when configuration of this object is finished.<p>
     */
    public void initializeFinished() {

        if (CmsLog.INIT.isInfoEnabled()) {
            CmsLog.INIT.info(Messages.get().getBundle().key(Messages.INIT_SYSTEM_CONFIG_FINISHED_0));
        }
    }

    /**
     * Returns if the VFS version history is enabled.<p>
     *
     * @return if the VFS version history is enabled
     */
    public boolean isHistoryEnabled() {

        return m_historyEnabled;
    }

    /**
     * Returns true if detail contents are restricted to detail pages from the same site.<p>
     *
     * @return true if detail contents are restricted to detail pages from the same site
     */
    public boolean isRestrictDetailContents() {

        return (m_restrictDetailContents == null) || Boolean.parseBoolean(m_restrictDetailContents.trim());

    }

    /**
     * Sets the cache settings for ADE.<p>
     *
     * @param settings the cache settings for ADE
     */
    public void setAdeCacheSettings(CmsADECacheSettings settings) {

        m_adeCacheSettings = settings;
    }

    /**
     * Sets the ADE configuration class name.<p>
     *
     * @param className the class name to set
     */
    public void setAdeConfiguration(String className) {

        m_adeConfiguration = className;
    }

    /**
     * Sets the authorization handler.<p>
     *
     * @param authorizationHandlerClass the authorization handler class to set.
     */
    public void setAuthorizationHandler(String authorizationHandlerClass) {

        m_authorizationHandler = authorizationHandlerClass;
    }

    /**
     * Sets the settings of the memory monitor.<p>
     *
     * @param settings the settings of the memory monitor
     */
    public void setCacheSettings(CmsCacheSettings settings) {

        m_cacheSettings = settings;
    }

    /**
     * Sets the CmsDefaultUsers.<p>
     *
     * @param userAdmin the name of the default admin user
     * @param userGuest the name of the guest user
     * @param userExport the name of the export user
     * @param userDeletedResource the name of the deleted resource user, can be <code>null</code>
     * @param groupAdministrators the name of the administrators group
     * @param groupUsers the name of the users group
     * @param groupGuests the name of the guests group
     */
    public void setCmsDefaultUsers(

        String userAdmin,
        String userGuest,
        String userExport,
        String userDeletedResource,
        String groupAdministrators,
        String groupUsers,
        String groupGuests) {

        if (CmsLog.INIT.isInfoEnabled()) {
            CmsLog.INIT.info(Messages.get().getBundle().key(Messages.INIT_CHECKING_DEFAULT_USER_NAMES_0));
        }
        m_cmsDefaultUsers = new CmsDefaultUsers(
            userAdmin,
            userGuest,
            userExport,
            userDeletedResource,
            groupAdministrators,
            groupUsers,
            groupGuests);

        if (CmsLog.INIT.isInfoEnabled()) {
            CmsLog.INIT.info(
                Messages.get().getBundle().key(Messages.INIT_ADMIN_USER_1, m_cmsDefaultUsers.getUserAdmin()));
            CmsLog.INIT.info(
                Messages.get().getBundle().key(Messages.INIT_GUEST_USER_1, m_cmsDefaultUsers.getUserGuest()));
            CmsLog.INIT.info(
                Messages.get().getBundle().key(Messages.INIT_EXPORT_USER_1, m_cmsDefaultUsers.getUserExport()));
            CmsLog.INIT.info(
                Messages.get().getBundle().key(
                    Messages.INIT_DELETED_RESOURCE_USER_1,
                    m_cmsDefaultUsers.getUserDeletedResource()));
            CmsLog.INIT.info(
                Messages.get().getBundle().key(
                    Messages.INIT_ADMIN_GROUP_1,
                    m_cmsDefaultUsers.getGroupAdministrators()));
            CmsLog.INIT.info(
                Messages.get().getBundle().key(Messages.INIT_USERS_GROUP_1, m_cmsDefaultUsers.getGroupUsers()));
            CmsLog.INIT.info(
                Messages.get().getBundle().key(Messages.INIT_GUESTS_GROUP_1, m_cmsDefaultUsers.getGroupGuests()));
            CmsLog.INIT.info(Messages.get().getBundle().key(Messages.INIT_DEFAULT_USER_NAMES_INITIALIZED_0));
        }
    }

    /**
     * Sets the flexCacheConfiguration.<p>
     *
     * @param flexCacheConfiguration the flexCacheConfiguration to set
     */
    public void setCmsFlexCacheConfiguration(CmsFlexCacheConfiguration flexCacheConfiguration) {

        m_cmsFlexCacheConfiguration = flexCacheConfiguration;
    }

    /**
     * Sets the cmsMemoryMonitorConfiguration.<p>
     *
     * @param cmsMemoryMonitorConfiguration the cmsMemoryMonitorConfiguration to set
     */
    public void setCmsMemoryMonitorConfiguration(CmsMemoryMonitorConfiguration cmsMemoryMonitorConfiguration) {

        m_cmsMemoryMonitorConfiguration = cmsMemoryMonitorConfiguration;
    }

    /**
     * Sets the credentials resolver class.<p>
     *
     * @param className the name of the credentials resolver class
     *
     * @throws Exception if something goes wrong
     */
    public void setCredentialsResolver(String className) throws Exception {

        String originalClassName = className;
        className = className.trim();
        Class<?> resolverClass = Class.forName(className);
        m_credentialsResolver = (I_CmsCredentialsResolver)(resolverClass.newInstance());
        m_credentialsResolverClass = originalClassName;
    }

    /**
     * Sets the defaultContentEncoding.<p>
     *
     * @param defaultContentEncoding the defaultContentEncoding to set
     */
    public void setDefaultContentEncoding(String defaultContentEncoding) {

        m_defaultContentEncoding = defaultContentEncoding;
    }

    /**
     * Sets the detail page handler.
     *
     * @param handler the detail page handler
     */
    public void setDetailPageHandler(I_CmsDetailPageHandler handler) {

        m_detailPageHandler = handler;

    }

    /**
     * VFS version history settings are set here.<p>
     *
     * @param historyEnabled if true the history is enabled
     * @param historyVersions the maximum number of versions that are kept per VFS resource
     * @param historyVersionsAfterDeletion the maximum number of versions for deleted resources
     */
    public void setHistorySettings(String historyEnabled, String historyVersions, String historyVersionsAfterDeletion) {

        m_historyEnabled = Boolean.valueOf(historyEnabled).booleanValue();
        m_historyVersions = Integer.valueOf(historyVersions).intValue();
        m_historyVersionsAfterDeletion = Integer.valueOf(historyVersionsAfterDeletion).intValue();
        if (CmsLog.INIT.isInfoEnabled()) {
            CmsLog.INIT.info(
                Messages.get().getBundle().key(
                    Messages.INIT_HISTORY_SETTINGS_3,
                    Boolean.valueOf(m_historyEnabled),
                    Integer.valueOf(m_historyVersions),
                    Integer.valueOf(m_historyVersionsAfterDeletion)));
        }
    }

    /**
     * Sets the HTTP authentication settings.<p>
     *
     * @param httpAuthenticationSettings the HTTP authentication settings to set
     */
    public void setHttpAuthenticationSettings(CmsHttpAuthenticationSettings httpAuthenticationSettings) {

        m_httpAuthenticationSettings = httpAuthenticationSettings;
    }

    /**
     * Sets the LetsEncrypt configuration.<p>
     *
     * @param letsEncryptConfig the LetsEncrypt configuration
     */
    public void setLetsEncryptConfig(CmsLetsEncryptConfiguration letsEncryptConfig) {

        m_letsEncryptConfig = letsEncryptConfig;
    }

    /**
     * Sets the locale manager for multi language support.<p>
     *
     * @param localeManager the locale manager to set
     */
    public void setLocaleManager(CmsLocaleManager localeManager) {

        m_localeManager = localeManager;
        if (CmsLog.INIT.isInfoEnabled()) {
            CmsLog.INIT.info(Messages.get().getBundle().key(Messages.INIT_CONFIG_I18N_FINISHED_0));
        }
    }

    /**
     * Sets the configured login manager.<p>
     *
     * @param maxBadAttemptsStr the number of allowed bad login attempts
     * @param disableMinutesStr the time an account gets locked if to many bad logins are attempted
     * @param enableSecurityStr flag to determine if the security option should be enabled on the login dialog
     * @param tokenLifetime the token lifetime
     * @param maxInactive maximum time since last login before CmsLockInactiveAccountsJob locks an account
     * @param passwordChangeInterval the password change interval
     * @param userDataCheckInterval the user data check interval
     * @param logoutUri the alternative logout handler URI (may be null)
     */
    public void setLoginManager(
        String disableMinutesStr,
        String maxBadAttemptsStr,
        String enableSecurityStr,
        String tokenLifetime,
        String maxInactive,
        String passwordChangeInterval,
        String userDataCheckInterval,
        String requireOrgUnitStr,
        String logoutUri) {

        int disableMinutes;
        try {
            disableMinutes = Integer.valueOf(disableMinutesStr).intValue();
        } catch (NumberFormatException e) {
            disableMinutes = CmsLoginManager.DISABLE_MINUTES_DEFAULT;
        }
        int maxBadAttempts;
        try {
            maxBadAttempts = Integer.valueOf(maxBadAttemptsStr).intValue();
        } catch (NumberFormatException e) {
            maxBadAttempts = CmsLoginManager.MAX_BAD_ATTEMPTS_DEFAULT;
        }
        boolean enableSecurity = Boolean.valueOf(enableSecurityStr).booleanValue();
        boolean requireOrgUnit = Boolean.valueOf(requireOrgUnitStr).booleanValue();
        m_loginManager = new CmsLoginManager(
            disableMinutes,
            maxBadAttempts,
            enableSecurity,
            tokenLifetime,
            maxInactive,
            passwordChangeInterval,
            userDataCheckInterval,
            requireOrgUnit,
            logoutUri);
        if (CmsLog.INIT.isInfoEnabled()) {
            CmsLog.INIT.info(
                Messages.get().getBundle().key(
                    Messages.INIT_LOGINMANAGER_3,
                    Integer.valueOf(disableMinutes),
                    Integer.valueOf(maxBadAttempts),
                    Boolean.valueOf(enableSecurity)));
        }
    }

    /**
     * Sets the mail settings.<p>
     *
     * @param mailSettings the mail settings to set.
     */
    public void setMailSettings(CmsMailSettings mailSettings) {

        m_mailSettings = mailSettings;
        if (LOG.isDebugEnabled()) {
            LOG.debug(Messages.get().getBundle().key(Messages.LOG_MAIL_SETTINGS_1, mailSettings));
        }
    }

    /**
     * Sets the project in which timestamps for the content notification are read.<p>
     *
     * @param notificationProject the project in which timestamps for the content notification are read
     */
    public void setNotificationProject(String notificationProject) {

        m_notificationProject = notificationProject;
        if (CmsLog.INIT.isInfoEnabled()) {
            CmsLog.INIT.info(
                Messages.get().getBundle().key(Messages.INIT_NOTIFICATION_PROJECT_1, m_notificationProject));
        }
    }

    /**
     * Sets the duration after which responsibles will be notified about out-dated content (in days).<p>
     *
     * @param notificationTime the duration after which responsibles will be notified about out-dated content
     */
    public void setNotificationTime(String notificationTime) {

        try {
            m_notificationTime = Integer.valueOf(notificationTime);
        } catch (Throwable t) {
            m_notificationTime = Integer.valueOf(-1);
        }
        if (CmsLog.INIT.isInfoEnabled()) {
            CmsLog.INIT.info(Messages.get().getBundle().key(Messages.INIT_NOTIFICATION_TIME_1, m_notificationTime));
        }
    }

    /**
     * Sets the password handler class.<p>
     *
     * @param passwordHandler the password handler to set
     */
    public void setPasswordHandler(I_CmsPasswordHandler passwordHandler) {

        m_passwordHandler = passwordHandler;
        if (CmsLog.INIT.isInfoEnabled()) {
            CmsLog.INIT.info(
                Messages.get().getBundle().key(
                    Messages.INIT_PWD_HANDLER_SUCCESS_1,
                    passwordHandler.getClass().getName()));
        }
    }

    /**
     * Sets the permission Handler class name.<p>
     *
     * @param permissionHandler the class name to set
     */
    public void setPermissionHandler(String permissionHandler) {

        m_permissionHandler = permissionHandler;
    }

    /**
     * Sets the servlet container specific setting.<p>
     *
     * @param configValue the configuration value
     */
    public void setPreventResponseFlush(String configValue) {

        OpenCms.getSystemInfo().getServletContainerSettings().setPreventResponseFlush(
            Boolean.valueOf(configValue).booleanValue());
    }

    /**
     * Sets the publish list remove mode.<p>
     *
     * @param removeMode the publish list remove mode
     */
    public void setPublishListRemoveMode(String removeMode) {

        m_publishListRemoveMode = removeMode;
    }

    /**
     * Sets the publish manager.<p>
     *
     * @param publishManager the publish manager
     */
    public void setPublishManager(CmsPublishManager publishManager) {

        m_publishManager = publishManager;
    }

    /**
     * Sets the servlet container specific setting.<p>
     *
     * @param configValue the configuration value
     */
    public void setReleaseTagsAfterEnd(String configValue) {

        OpenCms.getSystemInfo().getServletContainerSettings().setReleaseTagsAfterEnd(
            Boolean.valueOf(configValue).booleanValue());
    }

    /**
     * Sets the servlet container specific setting.<p>
     *
     * @param configValue the configuration value
     */
    public void setRequestErrorPageAttribute(String configValue) {

        OpenCms.getSystemInfo().getServletContainerSettings().setRequestErrorPageAttribute(configValue);
    }

    /**
     * Sets the 'restrict detail contents' option.<p>
     *
     * @param restrictDetailContents the value of the option
     */
    public void setRestrictDetailContents(String restrictDetailContents) {

        m_restrictDetailContents = restrictDetailContents;
    }

    /**
     * Sets the runtime info factory.<p>
     *
     * @param className the class name of the configured runtime info factory
     */
    public void setRuntimeInfoFactory(String className) {

        Object objectInstance;

        try {
            objectInstance = Class.forName(className).newInstance();
        } catch (Throwable t) {
            LOG.error(Messages.get().getBundle().key(Messages.LOG_CLASS_INIT_FAILURE_1, className), t);
            return;
        }

        if (objectInstance instanceof I_CmsDbContextFactory) {
            m_runtimeInfoFactory = (I_CmsDbContextFactory)objectInstance;
            if (CmsLog.INIT.isInfoEnabled()) {
                CmsLog.INIT.info(
                    Messages.get().getBundle().key(Messages.INIT_RUNTIME_INFO_FACTORY_SUCCESS_1, className));
            }
        } else {
            if (CmsLog.INIT.isFatalEnabled()) {
                CmsLog.INIT.fatal(
                    Messages.get().getBundle().key(Messages.INIT_RUNTIME_INFO_FACTORY_FAILURE_1, className));
            }
        }

    }

    /**
     * Sets the servlet container settings configuration mode.<p>
     *
     * @param configValue the value to set
     */
    public void setServletContainerSettingsMode(String configValue) {

        OpenCms.getSystemInfo().getServletContainerSettings().setMode(configValue);
    }

    /**
     * Sets the session storage provider.<p>
     *
     * @param sessionStorageProviderClass the session storage provider class to set.
     */
    public void setSessionStorageProvider(String sessionStorageProviderClass) {

        m_sessionStorageProvider = sessionStorageProviderClass;
    }

    /**
     * Sets the shell server options from the confriguration.<p>
     *
     * @param enabled the value of the 'enabled' attribute
     * @param portStr the value of the 'port' attribute
     */
    public void setShellServerOptions(String enabled, String portStr) {

        int port;
        try {
            port = Integer.parseInt(portStr);
        } catch (NumberFormatException e) {
            port = CmsRemoteShellConstants.DEFAULT_PORT;
        }
        m_shellServerOptions = new CmsRemoteShellConfiguration(Boolean.parseBoolean(enabled), port);

    }

    /**
     * Sets the subscription manager.<p>
     *
     * @param subscriptionManager the subscription manager
     */
    public void setSubscriptionManager(CmsSubscriptionManager subscriptionManager) {

        m_subscriptionManager = subscriptionManager;
    }

    /**
     * Sets the temporary file project id.<p>
     *
     * @param tempFileProjectId the temporary file project id to set
     */
    public void setTempFileProjectId(String tempFileProjectId) {

        try {
            m_tempFileProjectId = Integer.valueOf(tempFileProjectId).intValue();
        } catch (Throwable t) {
            m_tempFileProjectId = -1;
        }
        if (CmsLog.INIT.isInfoEnabled()) {
            CmsLog.INIT.info(
                Messages.get().getBundle().key(
                    Messages.INIT_TEMPFILE_PROJECT_ID_1,
                    Integer.valueOf(m_tempFileProjectId)));
        }
    }

    /**
     * Sets the user data request manager.
     *
     * @param manager the user data request manager
     */
    public void setUserDataRequestManager(CmsUserDataRequestManager manager) {

        m_userDataRequestManager = manager;

    }

    /**
     * Sets the user session mode.<p>
     *
     * @param userSessionMode the user session mode
     */
    public void setUserSessionMode(String userSessionMode) {

        if ((userSessionMode == null) || (m_userSessionMode != null)) {
            throw new IllegalStateException("Can't set user session mode to " + userSessionMode);
        }
        m_userSessionMode = UserSessionMode.valueOf(userSessionMode);
    }

    /**
     * Sets if the SAX parser implementation classes should be stored in system properties
     * to improve the unmarshalling performance.<p>
     *
     * @param enabled <code>true</code> to store SAX parser implementation classes in system properties
     */
    public void setUseSaxImplSystemProperties(String enabled) {

        m_saxImplProperties = Boolean.parseBoolean(enabled);
    }

    /**
     * Sets the validation handler.<p>
     *
     * @param validationHandlerClass the validation handler class to set.
     */
    public void setValidationHandler(String validationHandlerClass) {

        m_validationHandler = validationHandlerClass;
    }

    /**
     * Sets the configured workflow manager instance.<p>
     *
     * @param workflowManager the configured workflow manager
     */
    public void setWorkflowManager(I_CmsWorkflowManager workflowManager) {

        m_workflowManager = workflowManager;
    }

    /**
     * Returns whether the SAX parser implementation classes should be stored in system properties
     * to improve the unmarshalling performance.<p>
     *
     * @return <code>true</code> if the SAX parser implementation classes should be stored in system properties
     */
    public boolean useSaxImplSystemProperties() {

        return m_saxImplProperties;
    }

    /**
     * Adds a new authorization configuration.
     *
     * @param config the authorization configuration to add
     */
    protected void addApiAuthorization(ApiAuthorizationConfig config) {

        m_apiAuthorizations.add(config);
        try {
            Class<?> cls = Class.forName(config.getClassName(), false, getClass().getClassLoader());
            I_CmsApiAuthorizationHandler handler = (I_CmsApiAuthorizationHandler)(cls.getDeclaredConstructor().newInstance());
            handler.setParameters(config.getParams());
            m_apiAuthorizationMap.put(config.getName(), handler);
        } catch (Exception e) {
            LOG.error(e.getLocalizedMessage(), e);
        }
    }

    /**
     * @see org.opencms.configuration.A_CmsXmlConfiguration#initMembers()
     */
    @Override
    protected void initMembers() {

        setXmlFileName(DEFAULT_XML_FILE_NAME);
        m_historyEnabled = true;
        m_historyVersions = 10;
        m_historyVersionsAfterDeletion = -1; // use m_historyVersions instead
        m_resourceInitHandlers = new ArrayList<I_CmsResourceInit>();
        m_requestHandlers = new ArrayList<I_CmsRequestHandler>();
        m_runtimeProperties = new HashMap<String, String>();
        m_eventManager = new CmsEventManager();
        if (CmsLog.INIT.isInfoEnabled()) {
            CmsLog.INIT.info(Messages.get().getBundle().key(Messages.INIT_SYSTEM_CONFIG_INIT_0));
        }
    }

}
