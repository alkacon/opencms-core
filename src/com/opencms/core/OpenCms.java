/*
 * File   : $Source: /alkacon/cvs/opencms/src/com/opencms/core/Attic/OpenCms.java,v $
 * Date   : $Date: 2003/07/22 05:50:35 $
 * Version: $Revision: 1.147 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Mananagement System
 *
 * Copyright (C) 2002 - 2003 Alkacon Software (http://www.alkacon.com)
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

package com.opencms.core;

import org.opencms.db.CmsDriverManager;
import org.opencms.loader.CmsJspLoader;
import org.opencms.loader.CmsLoaderManager;
import org.opencms.loader.I_CmsResourceLoader;
import org.opencms.lock.CmsLockDispatcher;
import org.opencms.site.CmsSiteManager;

import com.opencms.boot.CmsBase;
import com.opencms.boot.I_CmsLogChannels;
import com.opencms.core.exceptions.CmsCheckResourceException;
import com.opencms.file.CmsFile;
import com.opencms.file.CmsFolder;
import com.opencms.file.CmsObject;
import com.opencms.file.CmsResource;
import com.opencms.file.CmsStaticExport;
import com.opencms.flex.util.CmsResourceTranslator;
import com.opencms.flex.util.CmsStringSubstitution;
import com.opencms.flex.util.CmsUUID;
import com.opencms.util.Utils;
import com.opencms.workplace.I_CmsWpConstants;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import source.org.apache.java.util.Configurations;

/**
 * This class is the main class of the OpenCms system,
 * think of it as the "operating system" of OpenCms.<p>
 *  
 * Any request to an OpenCms resource will be processed by this class first.
 * The class will try to map the request to a VFS (Virtual File System) resource,
 * i.e. an URI. If the resource is found, it will be read anf forwarded to
 * to a launcher, which is performs the output of the requested resource.<p>
 *
 * The OpenCms class is independent of access module to the OpenCms 
 * (e.g. Servlet, Command Shell), therefore this class is <b>not</b> responsible 
 * for user authentification. This is done by the access module to the OpenCms.<p>
 *
 * There will be only one instance of the OpenCms object created for
 * any accessing class. This means that in the default configuration, where 
 * OpenCms is accessed through a servlet, there will be only one instance of 
 * this class running at a time.
 * 
 * @see com.opencms.core.A_OpenCms
 * @see com.opencms.core.OpenCmsHttpServlet
 * @see com.opencms.file.CmsObject
 * 
 * @author Alexander Kandzior (a.kandzior@alkacon.com)
 * @author Michael Emmerich (m.emmerich@alkacon.com)
 * 
 * @version $Revision: 1.147 $
 */
public final class OpenCms extends A_OpenCms implements I_CmsConstants, I_CmsLogChannels {

    /** The default mimetype */
    private static final String C_DEFAULT_MIMETYPE = "text/html";

    /** The object to store the properties from the opencms.property file for the static export */
    private static CmsStaticExportProperties c_exportProperties = new CmsStaticExportProperties();

    /** The name of the class used to validate a new password */
    private static String c_passwordValidatingClass = "";

    /** List of default file names (for directories, e.g, "index.html") */
    private static String[] m_defaultFilenames = null;

    /** Directory translator, used to translate all access to resources */
    private static CmsResourceTranslator m_directoryTranslator = null;

    /** Filename translator, used only for the creation of new files */
    private static CmsResourceTranslator m_fileTranslator = null;

    /** Member variable to store instances to modify resources */
    private List m_checkFile = new ArrayList();

    /** Flag to indicate if the startup classes have already been initialized */
    private boolean m_startupClassesInitialized = false;

    /** Hashtable with all available Mimetypes */
    private Hashtable m_mt = new Hashtable();

    /**  The cron scheduler to schedule the cronjobs */
    private CmsCronScheduler m_scheduler;

    /** The cron table to use with the scheduler */
    private CmsCronTable m_table;

    /**
     * Constructor to create a new OpenCms object.<p>
     * 
     * It reads the configurations from the <code>opencms.properties</code>
     * file in the <code>config/</code> subdirectory. With the information 
     * from this file is inits a ResourceBroker (Database access module),
     * various caching systems and other options.<p>
     * 
     * This will only be done once per accessing class.
     *
     * @param conf The configurations from the <code>opencms.properties</code> file.
     * @throws Exception in case of problems initializing OpenCms, this is usually fatal 
     */
    public OpenCms(Configurations conf) throws Exception {
        // Save the configuration
        setConfiguration(conf);

        // this will initialize the encoding with some default from the A_OpenCms
        String defaultEncoding = getDefaultEncoding();
        // check the opencms.properties for a different setting
        defaultEncoding = conf.getString("defaultContentEncoding", defaultEncoding);
        if (C_LOGGING && isLogging(C_OPENCMS_INIT))
            log(C_OPENCMS_INIT, ". OpenCms encoding     : " + defaultEncoding);
        String systemEncoding = null;
        try {
            systemEncoding = System.getProperty("file.encoding");
        } catch (SecurityException se) {
            // security manager is active, but we will try other options before giving up
        }
        if (C_LOGGING && isLogging(C_OPENCMS_INIT))
            log(C_OPENCMS_INIT, ". System file.encoding : " + systemEncoding);
        if (!defaultEncoding.equals(systemEncoding)) {
            String msg = "OpenCms startup failure: System file.encoding '" + systemEncoding + "' not equal to OpenCms encoding '" + defaultEncoding + "'";
            if (C_LOGGING && isLogging(C_OPENCMS_CRITICAL))
                log(C_OPENCMS_CRITICAL, ". Critical init error/1: " + msg);
            throw new Exception(msg);
        }
        try {
            // check if the found encoding is supported 
            // this will work with Java 1.4+ only
            if (!java.nio.charset.Charset.isSupported(defaultEncoding)) {
                defaultEncoding = getDefaultEncoding();
            }
        } catch (Throwable t) {
            // will be thrown in Java < 1.4 (NoSuchMethodException etc.)
            // in Java < 1.4 there is no easy way to check if encoding is supported,
            // so you must make sure your setting in "opencms.properties" is correct.             
        }
        setDefaultEncoding(defaultEncoding);
        if (C_LOGGING && isLogging(C_OPENCMS_INIT))
            log(C_OPENCMS_INIT, ". Encoding set to      : " + defaultEncoding);

        // Read server ethernet address (MAC) and init UUID generator
        String ethernetAddress = conf.getString("server.ethernet.address", CmsUUID.getDummyEthernetAddress());
        if (C_LOGGING && isLogging(C_OPENCMS_INIT))
            log(C_OPENCMS_INIT, ". Ethernet address used: " + ethernetAddress);
        CmsUUID.init(ethernetAddress);

        // invoke the ResourceBroker via the initalizer
        try {
            if (C_LOGGING && isLogging(C_OPENCMS_INIT)) {
                String jdkinfo = System.getProperty("java.vm.name") + " ";
                jdkinfo += System.getProperty("java.vm.version") + " ";
                jdkinfo += System.getProperty("java.vm.info") + " ";
                jdkinfo += System.getProperty("java.vm.vendor") + " ";
                log(C_OPENCMS_INIT, ". Java VM in use       : " + jdkinfo);
                String osinfo = System.getProperty("os.name") + " ";
                osinfo += System.getProperty("os.version") + " ";
                osinfo += System.getProperty("os.arch") + " ";
                log(C_OPENCMS_INIT, ". Operating sytem      : " + osinfo);
            }
        } catch (Exception e) {
            if (C_LOGGING && isLogging(C_OPENCMS_CRITICAL))
                log(C_OPENCMS_CRITICAL, ". Critical init error/2: " + e.getMessage());
            // any exception here is fatal and will cause a stop in processing
            throw e;
        }

        try {
            // init the rb via the manager with the configuration
            // and init the cms-object with the rb.
            m_driverManager = CmsDriverManager.newInstance(conf);            
            initLockDispatcher();
        } catch (Exception e) {
            if (C_LOGGING && isLogging(C_OPENCMS_CRITICAL))
                log(C_OPENCMS_CRITICAL, ". Critical init error/3: " + e.getMessage());
            // any exception here is fatal and will cause a stop in processing
            throw new CmsException("Database init failed", CmsException.C_RB_INIT_ERROR, e);
        }

        try {
            // initalize the Hashtable with all available mimetypes
            m_mt = m_driverManager.readMimeTypes();
            if (C_LOGGING && isLogging(C_OPENCMS_INIT))
                log(C_OPENCMS_INIT, ". Found mime types     : " + m_mt.size() + " entrys");

            // if the System property opencms.disableScheduler is set to true, don't start scheduling
            if (!new Boolean(System.getProperty("opencms.disableScheduler")).booleanValue()) {
                // now initialise the OpenCms scheduler to launch cronjobs
                m_table = new CmsCronTable(m_driverManager.readCronTable());
                m_scheduler = new CmsCronScheduler(this, m_table);
                if (C_LOGGING && isLogging(C_OPENCMS_INIT))
                    log(C_OPENCMS_INIT, ". OpenCms scheduler    : enabled");
            } else {
                if (C_LOGGING && isLogging(C_OPENCMS_INIT))
                    log(C_OPENCMS_INIT, ". OpenCms scheduler    : disabled");
            }
        } catch (Exception e) {
            if (C_LOGGING && isLogging(C_OPENCMS_CRITICAL))
                log(C_OPENCMS_CRITICAL, ". Critical init error/5: " + e.getMessage());
            // any exception here is fatal and will cause a stop in processing
            throw e;
        }

        // read flex jsp export url property and save in runtime configuration
        String flexExportUrl = conf.getString(CmsJspLoader.C_LOADER_JSPEXPORTURL, null);
        if (null != flexExportUrl) {
            // if JSP export URL is null it will be set in initStartupClasses()
            if (flexExportUrl.endsWith(C_FOLDER_SEPARATOR)) {
                flexExportUrl = flexExportUrl.substring(0, flexExportUrl.length() - 1);
            }
            setRuntimeProperty(CmsJspLoader.C_LOADER_JSPEXPORTURL, flexExportUrl);
            if (C_LOGGING && isLogging(C_OPENCMS_INIT))
                log(C_OPENCMS_INIT, ". JSP export URL       : using value from opencms.properties - " + flexExportUrl);
        }

        // read flex jsp error page commit property and save in runtime configuration
        Boolean flexErrorPageCommit = conf.getBoolean(CmsJspLoader.C_LOADER_ERRORPAGECOMMIT, new Boolean(true));
        setRuntimeProperty(CmsJspLoader.C_LOADER_ERRORPAGECOMMIT, flexErrorPageCommit);
        if (C_LOGGING && isLogging(C_OPENCMS_INIT))
            log(C_OPENCMS_INIT, ". JSP errorPage commit : " + (flexErrorPageCommit.booleanValue() ? "enabled" : "disabled"));

        // try to initialize the flex cache
        try {
            if (C_LOGGING && isLogging(C_OPENCMS_INIT))
                log(C_OPENCMS_INIT, ". Flex cache init      : starting");
            // the flexCache has static members that must be initialized with "this" object
            new com.opencms.flex.cache.CmsFlexCache(this);
            if (C_LOGGING && isLogging(C_OPENCMS_INIT))
                log(C_OPENCMS_INIT, ". Flex cache init      : finished");
        } catch (Exception e) {
            if (C_LOGGING && isLogging(C_OPENCMS_INIT))
                log(C_OPENCMS_INIT, ". Flex cache init      : non-critical error " + e.toString());
        }
        
        // initialize the loaders
        try {
            if (C_LOGGING && isLogging(C_OPENCMS_INIT))
                log(C_OPENCMS_INIT, ". ResourceLoader init  : starting");
            setLoaderManager(new CmsLoaderManager(this, conf));
            if (C_LOGGING && isLogging(C_OPENCMS_INIT))
                log(C_OPENCMS_INIT, ". ResourceLoader init  : finished");
        } catch (Exception e) {
            if (C_LOGGING && isLogging(C_OPENCMS_INIT))
                log(C_OPENCMS_INIT, ". ResourceLoader init  : non-critical error " + e.toString());
        }

        // try to initialize directory translations
        try {
            boolean translationEnabled = conf.getBoolean("directory.translation.enabled", false);
            if (C_LOGGING && isLogging(C_OPENCMS_INIT))
                log(C_OPENCMS_INIT, ". Directory translation: " + (translationEnabled ? "enabled" : "disabled"));
            if (translationEnabled) {
                String[] translations = conf.getStringArray("directory.translation.rules");
                // Directory translation stops after fist match, hence the "false" parameter
                m_directoryTranslator = new CmsResourceTranslator(translations, false);
            }
        } catch (Exception e) {
            if (C_LOGGING && isLogging(C_OPENCMS_INIT))
                log(C_OPENCMS_INIT, ". Directory translation: non-critical error " + e.toString());
        }
        // make sure we always have at least an empty array      
        if (m_directoryTranslator == null)
            m_directoryTranslator = new CmsResourceTranslator(new String[0], false);

        // try to initialize filename translations
        try {
            boolean translationEnabled = conf.getBoolean("filename.translation.enabled", false);
            if (C_LOGGING && isLogging(C_OPENCMS_INIT))
                log(C_OPENCMS_INIT, ". Filename translation : " + (translationEnabled ? "enabled" : "disabled"));
            if (translationEnabled) {
                String[] translations = conf.getStringArray("filename.translation.rules");
                // Filename translations applies all rules, hence the true patameters
                m_fileTranslator = new CmsResourceTranslator(translations, true);
            }
        } catch (Exception e) {
            if (C_LOGGING && isLogging(C_OPENCMS_INIT))
                log(C_OPENCMS_INIT, ". Filename translation : non-critical error " + e.toString());
        }
        // make sure we always have at last an emtpy array      
        if (m_fileTranslator == null)
            m_fileTranslator = new CmsResourceTranslator(new String[0], false);

        // try to initialize default directory file names (e.g. index.html)
        try {
            m_defaultFilenames = conf.getStringArray("directory.default.files");
            for (int i = 0; i < m_defaultFilenames.length; i++) {
                // remove possible white space
                m_defaultFilenames[i] = m_defaultFilenames[i].trim();
                if (C_LOGGING && isLogging(C_OPENCMS_INIT))
                    log(C_OPENCMS_INIT, ". Default file         : " + (i + 1) + " - " + m_defaultFilenames[i]);
            }
        } catch (Exception e) {
            if (C_LOGGING && isLogging(C_OPENCMS_INIT))
                log(C_OPENCMS_INIT, ". Default file         : non-critical error " + e.toString());
        }
        // make sure we always have at last an emtpy array      
        if (m_defaultFilenames == null)
            m_defaultFilenames = new String[0];
            
        // read the immutable import resources
        String[] immuResources = conf.getStringArray("import.immutable.resources");
        if (immuResources == null)
            immuResources = new String[0];
        List immutableResourcesOri = java.util.Arrays.asList(immuResources);
        ArrayList immutableResources = new ArrayList();
        for (int i = 0; i < immutableResourcesOri.size(); i++) {
            // remove possible white space
            String path = ((String)immutableResourcesOri.get(i)).trim();
            if (path != null && !"".equals(path)) {
                immutableResources.add(path);
                if (C_LOGGING && isLogging(C_OPENCMS_INIT))
                    log(C_OPENCMS_INIT, ". Immutable resource   : " + (i + 1) + " - " + path);
            }
        }
        if (C_LOGGING && isLogging(C_OPENCMS_INIT))
            log(C_OPENCMS_INIT, ". Immutable resources  : " + ((immutableResources.size() > 0) ? "enabled" : "disabled"));
        setRuntimeProperty("import.immutable.resources", immutableResources);
        
        // read the default user settings
        try {
            String userDefaultLanguage = conf.getString("workplace.user.default.language", I_CmsWpConstants.C_DEFAULT_LANGUAGE);
            setUserDefaultLanguage(userDefaultLanguage);
            if (C_LOGGING && isLogging(C_OPENCMS_INIT))
                log(C_OPENCMS_INIT, ". User data init       : Default language is '" + userDefaultLanguage + "'");
        } catch (Exception e) {
            if (C_LOGGING && isLogging(C_OPENCMS_INIT))
                log(C_OPENCMS_INIT, ". User data init       : non-critical error " + e.toString());
        }
                
        // read the password validating class
        c_passwordValidatingClass = conf.getString("passwordvalidatingclass", "com.opencms.util.PasswordValidtation");
        if (C_LOGGING && isLogging(C_OPENCMS_INIT))
            log(C_OPENCMS_INIT, ". Password validation  : " + c_passwordValidatingClass);
        
        // read the maximum file upload size limit
        Integer fileMaxUploadSize = new Integer(conf.getInteger("workplace.file.maxuploadsize", -1));
        setRuntimeProperty("workplace.file.maxuploadsize", fileMaxUploadSize);
        if (C_LOGGING && isLogging(C_OPENCMS_INIT))
            log(C_OPENCMS_INIT, ". File max. upload size: " + (fileMaxUploadSize.intValue() > 0 ? (fileMaxUploadSize + " KB") : "unlimited"));
        
        // initialize "checkresource" registry classes
        try {
            Hashtable checkresourceNode = getRegistry().getSystemValues("checkresource");
            if (checkresourceNode != null) {
                for (int i = 1; i <= checkresourceNode.size(); i++) {
                    String currentClass = (String)checkresourceNode.get("class" + i);
                    try {
                        m_checkFile.add(Class.forName(currentClass).newInstance());
                        if (C_LOGGING && isLogging(C_OPENCMS_INIT))
                            log(C_OPENCMS_INIT, ". Checkfile class init : " + currentClass + " instanciated");
                    } catch (Exception e1) {
                        if (C_LOGGING && isLogging(C_OPENCMS_INIT))
                            log(C_OPENCMS_INIT, ". Checkfile class init : non-critical error " + e1.toString());
                    }
                }
            }
        } catch (Exception e2) {
            if (C_LOGGING && isLogging(C_OPENCMS_INIT))
                log(C_OPENCMS_INIT, ". Checkfile class init : non-critical error " + e2.toString());
        }        
        
        // initialize the site manager
        setSiteManager(CmsSiteManager.initialize(conf));        
        
        // read old (proprietary XML-style) locale backward compatibily support flag
        Boolean supportOldLocales = conf.getBoolean("compatibility.support.oldlocales", new Boolean(false));
        setRuntimeProperty("compatibility.support.oldlocales", supportOldLocales);
        if (C_LOGGING && isLogging(C_OPENCMS_INIT))
            log(C_OPENCMS_INIT, ". Old locale support   : " + (supportOldLocales.booleanValue() ? "enabled" : "disabled"));

        // convert import files from 4.x versions old webapp URL
        String webappUrl = conf.getString("compatibility.support.import.old.webappurl", null);
        if (webappUrl != null) {
            setRuntimeProperty("compatibility.support.import.old.webappurl", webappUrl);
        }
        if (C_LOGGING && isLogging(C_OPENCMS_INIT))
            log(C_OPENCMS_INIT, ". Old webapp URL       : " + ((webappUrl == null) ? "not set!" : webappUrl));

        // unwanted resource properties which are deleted during import
        String[] propNames = conf.getStringArray("compatibility.support.import.remove.propertytags");
        if (propNames == null)
            propNames = new String[0];
        List propertyNamesOri = java.util.Arrays.asList(propNames);
        ArrayList propertyNames = new ArrayList();
        for (int i = 0; i < propertyNamesOri.size(); i++) {
            // remove possible white space
            String name = ((String)propertyNamesOri.get(i)).trim();
            if (name != null && !"".equals(name)) {
                propertyNames.add(name);
                if (C_LOGGING && isLogging(C_OPENCMS_INIT))
                    log(C_OPENCMS_INIT, ". Clear import property: " + (i + 1) + " - " + name);
            }
        }
        if (C_LOGGING && isLogging(C_OPENCMS_INIT))
            log(C_OPENCMS_INIT, ". Remove properties    : " + ((propertyNames.size() > 0) ? "enabled" : "disabled"));
        setRuntimeProperty("compatibility.support.import.remove.propertytags", propertyNames);

        // old web application names (for editor macro replacement) 
        String[] appNames = conf.getStringArray("compatibility.support.webAppNames");
        if (appNames == null)
            appNames = new String[0];
        List webAppNamesOri = java.util.Arrays.asList(appNames);
        ArrayList webAppNames = new ArrayList();
        for (int i = 0; i < webAppNamesOri.size(); i++) {
            // remove possible white space
            String name = ((String)webAppNamesOri.get(i)).trim();
            if (name != null && !"".equals(name)) {
                webAppNames.add(name);
                if (C_LOGGING && isLogging(C_OPENCMS_INIT))
                    log(C_OPENCMS_INIT, ". Old context path     : " + (i + 1) + " - " + name);
            }
        }
        if (C_LOGGING && isLogging(C_OPENCMS_INIT))
            log(C_OPENCMS_INIT, ". Old context support  : " + ((webAppNames.size() > 0) ? "enabled" : "disabled"));
        setRuntimeProperty("compatibility.support.webAppNames", webAppNames);

        // now for the link replacement rules there are up to three rulesets for export online and offline
        try {
            if (C_LOGGING && isLogging(C_OPENCMS_INIT))
                log(C_OPENCMS_INIT, ". Link rules init      : starting");

            String[] staticUrlPrefix = new String[4];
            staticUrlPrefix[0] = CmsStringSubstitution.substitute(conf.getString(C_URL_PREFIX_EXPORT, ""), C_WEB_APP_REPLACE_KEY, CmsBase.getWebAppName());
            staticUrlPrefix[1] = CmsStringSubstitution.substitute(conf.getString(C_URL_PREFIX_HTTP, ""), C_WEB_APP_REPLACE_KEY, CmsBase.getWebAppName());
            staticUrlPrefix[2] = CmsStringSubstitution.substitute(conf.getString(C_URL_PREFIX_HTTPS, ""), C_WEB_APP_REPLACE_KEY, CmsBase.getWebAppName());
            staticUrlPrefix[3] = CmsStringSubstitution.substitute(conf.getString(C_URL_PREFIX_SERVERNAME, ""), C_WEB_APP_REPLACE_KEY, CmsBase.getWebAppName());
            c_exportProperties.setUrlPrefixArray(staticUrlPrefix);
            // to get the right rulesets we need the default value for the export property
            String exportDefault = conf.getString("staticexport.default.export", "true");
            c_exportProperties.setExportDefaultValue(exportDefault);
            String export = conf.getString("linkrules." + exportDefault + ".export");
            String[] linkRulesExport;
            if (export != null && !"".equals(export)) {
                linkRulesExport = conf.getStringArray("ruleset." + export);
                // now replace ${WEB_APP_NAME} with the correct name of the webapplication and replace the other variables
                for (int i = 0; i < linkRulesExport.length; i++) {
                    linkRulesExport[i] = CmsStringSubstitution.substitute(linkRulesExport[i], C_WEB_APP_REPLACE_KEY, CmsBase.getWebAppName());
                    linkRulesExport[i] = CmsStringSubstitution.substitute(linkRulesExport[i], "${" + C_URL_PREFIX_EXPORT + "}", staticUrlPrefix[0]);
                    linkRulesExport[i] = CmsStringSubstitution.substitute(linkRulesExport[i], "${" + C_URL_PREFIX_HTTP + "}", staticUrlPrefix[1]);
                    linkRulesExport[i] = CmsStringSubstitution.substitute(linkRulesExport[i], "${" + C_URL_PREFIX_HTTPS + "}", staticUrlPrefix[2]);
                    linkRulesExport[i] = CmsStringSubstitution.substitute(linkRulesExport[i], "${" + C_URL_PREFIX_SERVERNAME + "}", staticUrlPrefix[3]);
                }
                c_exportProperties.setLinkRulesExport(linkRulesExport);
            }
            String online = conf.getString("linkrules." + exportDefault + ".online");
            String[] linkRulesOnline;
            if (online != null && !"".equals(online)) {
                linkRulesOnline = conf.getStringArray("ruleset." + online);
                // now replace ${WEB_APP_NAME} with the correct name of the webapplication and replace the other variables
                for (int i = 0; i < linkRulesOnline.length; i++) {
                    linkRulesOnline[i] = CmsStringSubstitution.substitute(linkRulesOnline[i], C_WEB_APP_REPLACE_KEY, CmsBase.getWebAppName());
                    linkRulesOnline[i] = CmsStringSubstitution.substitute(linkRulesOnline[i], "${" + C_URL_PREFIX_EXPORT + "}", staticUrlPrefix[0]);
                    linkRulesOnline[i] = CmsStringSubstitution.substitute(linkRulesOnline[i], "${" + C_URL_PREFIX_HTTP + "}", staticUrlPrefix[1]);
                    linkRulesOnline[i] = CmsStringSubstitution.substitute(linkRulesOnline[i], "${" + C_URL_PREFIX_HTTPS + "}", staticUrlPrefix[2]);
                    linkRulesOnline[i] = CmsStringSubstitution.substitute(linkRulesOnline[i], "${" + C_URL_PREFIX_SERVERNAME + "}", staticUrlPrefix[3]);
                }
                c_exportProperties.setLinkRulesOnline(linkRulesOnline);
            }
            String offline = conf.getString("linkrules." + exportDefault + ".offline");
            String[] linkRulesOffline;
            if (offline != null && !"".equals(offline)) {
                linkRulesOffline = conf.getStringArray("ruleset." + offline);
                // now replace ${WEB_APP_NAME} with the correct name of the webapplication and replace the other variables
                for (int i = 0; i < linkRulesOffline.length; i++) {
                    linkRulesOffline[i] = CmsStringSubstitution.substitute(linkRulesOffline[i], C_WEB_APP_REPLACE_KEY, CmsBase.getWebAppName());
                    linkRulesOffline[i] = CmsStringSubstitution.substitute(linkRulesOffline[i], "${" + C_URL_PREFIX_EXPORT + "}", staticUrlPrefix[0]);
                    linkRulesOffline[i] = CmsStringSubstitution.substitute(linkRulesOffline[i], "${" + C_URL_PREFIX_HTTP + "}", staticUrlPrefix[1]);
                    linkRulesOffline[i] = CmsStringSubstitution.substitute(linkRulesOffline[i], "${" + C_URL_PREFIX_HTTPS + "}", staticUrlPrefix[2]);
                    linkRulesOffline[i] = CmsStringSubstitution.substitute(linkRulesOffline[i], "${" + C_URL_PREFIX_SERVERNAME + "}", staticUrlPrefix[3]);
                }
                c_exportProperties.setLinkRulesOffline(linkRulesOffline);
            }
            String extern = conf.getString("linkrules." + exportDefault + ".extern");
            String[] linkRulesExtern;
            if (extern != null && !"".equals(extern)) {
                linkRulesExtern = conf.getStringArray("ruleset." + extern);
                // now replace ${WEB_APP_NAME} with the correct name of the webapplication and replace the other variables
                for (int i = 0; i < linkRulesExtern.length; i++) {
                    linkRulesExtern[i] = CmsStringSubstitution.substitute(linkRulesExtern[i], C_WEB_APP_REPLACE_KEY, CmsBase.getWebAppName());
                    linkRulesExtern[i] = CmsStringSubstitution.substitute(linkRulesExtern[i], "${" + C_URL_PREFIX_EXPORT + "}", staticUrlPrefix[0]);
                    linkRulesExtern[i] = CmsStringSubstitution.substitute(linkRulesExtern[i], "${" + C_URL_PREFIX_HTTP + "}", staticUrlPrefix[1]);
                    linkRulesExtern[i] = CmsStringSubstitution.substitute(linkRulesExtern[i], "${" + C_URL_PREFIX_HTTPS + "}", staticUrlPrefix[2]);
                    linkRulesExtern[i] = CmsStringSubstitution.substitute(linkRulesExtern[i], "${" + C_URL_PREFIX_SERVERNAME + "}", staticUrlPrefix[3]);
                }
                c_exportProperties.setLinkRulesExtern(linkRulesExtern);
            }
            c_exportProperties.setStartRule(null); // temporary out of order: conf.getString("exportfirstrule");

            Vector staticExportStart = new Vector();
            staticExportStart.add("/");
            c_exportProperties.setStartPoints(staticExportStart);

            // at last the target for the export
            c_exportProperties.setExportPath(com.opencms.boot.CmsBase.getAbsoluteWebPath(CmsBase.getAbsoluteWebPath(conf.getString(C_STATICEXPORT_PATH))));

            // should the links in static export be relative?
            c_exportProperties.setExportRelativeLinks(conf.getBoolean("relativelinks_in_export", false));
            // is the static export enabled?
            String activCheck = conf.getString("staticexport.enabled", "false");
            c_exportProperties.setStaticExportEnabledValue(activCheck);
            if ("true".equalsIgnoreCase(activCheck)) {
                c_exportProperties.setStaticExportEnabled(true);
            } else {
                c_exportProperties.setStaticExportEnabled(false);
            }
            if (c_exportProperties.isStaticExportEnabled()) {
                // we have to generate the dynamic rulessets
                createDynamicLinkRules();
            } else {
                if ("false_ssl".equalsIgnoreCase(activCheck)) {
                    // no static esport, but we need the dynamic rules for setting the protokoll to https
                    c_exportProperties.setLinkRulesOffline(new String[] {"s#^#" + staticUrlPrefix[1] + "#"});
                    c_exportProperties.setLinkRulesOnline(new String[] {"*dynamicRules*", "s#^#" + staticUrlPrefix[1] + "#"});
                    // and we have to change the standart export prefix to stay in opencms
                    c_exportProperties.getUrlPrefixArray()[0] = staticUrlPrefix[1];
                    // if we need them we should create them
                    createDynamicLinkRules();
                } else {
                    // no static export. We need online and offline rules to stay in OpenCms.
                    // we generate them with the url_prefix_http so the user can still configure
                    // the servletpath.
                    c_exportProperties.setLinkRulesOffline(new String[] {"s#^#" + staticUrlPrefix[1] + "#"});
                    c_exportProperties.setLinkRulesOnline(new String[] {"s#^#" + staticUrlPrefix[1] + "#"});
                }
            }
            if (C_LOGGING && isLogging(C_OPENCMS_INIT))
                log(C_OPENCMS_INIT, ". Link rules init      : finished");
        } catch (Exception e) {
            if (C_LOGGING && isLogging(C_OPENCMS_INIT))
                log(C_OPENCMS_INIT, ". Link rules init      : non-critical error " + e.toString());
        }
    }

    /**
     * Returns the Class that is used for the password validation.<p>
     * 
     * @return the Class that is used for the password validation
     */
    public static String getPasswordValidatingClass() {
        return c_passwordValidatingClass;
    }

    /**
     * Returns the properties for the static export.<p>
     * 
     * @return the properties for the static export
     */
    public static CmsStaticExportProperties getStaticExportProperties() {
        return c_exportProperties;
    }

    /**
     * Creates the dynamic linkrules.<p>
     */
    private void createDynamicLinkRules() {
        //create a valid cms-object
        CmsObject cms = new CmsObject();
        try {
            initUser(cms, null, null, C_USER_ADMIN, C_GROUP_ADMIN, C_VFS_DEFAULT, C_PROJECT_ONLINE_ID, null);
            new CmsStaticExport(cms, null, false, null, null, null, null);
        } catch (Exception e) {
            if (C_LOGGING && isLogging(C_OPENCMS_INIT))
                log(C_OPENCMS_INIT, ". Dynamic link rules   : non-critical error " + e.toString());
        }
    }

    /**
     * Destructor, called when the the servlet is shut down.<p>
     * 
     * @throws Throwable if something goes wrong during shutdown
     */
    public void destroy() throws Throwable {
        m_scheduler.shutDown();
        CmsObject cms = new CmsObject();
        cms.init(m_driverManager);
        cms.destroy();
    }

    /**
     * Returns the file name translator this OpenCms has read from the opencms.properties.<p>
     * 
     * @return The file name translator this OpenCms has read from the opencms.properties
     */
    public CmsResourceTranslator getFileTranslator() {
        return m_fileTranslator;
    }
    
    /**
     * Initializers the lock dispatcher.<p>
     * 
     * To do so, all locked resources are read from the database.
     * 
     * @param cms
     * @throws CmsException
     */
    private void initLockDispatcher() {
        try {
            // create a valid dummy CmsObject instance
            CmsObject cms = new CmsObject();
                        
            initUser(cms, null, null, C_USER_ADMIN, C_GROUP_ADMIN, C_VFS_DEFAULT, Integer.MAX_VALUE, null);
            int count = CmsLockDispatcher.getInstance().init(cms);
            if (C_LOGGING && isLogging(C_OPENCMS_INIT)) {
                log(C_OPENCMS_INIT, ". LockDispatcher init  : ok, found " + count + " directly locked resources");
            }
        } catch (Exception e) {
            if (C_LOGGING && isLogging(C_OPENCMS_INIT)) {
                log(C_OPENCMS_INIT, ". LockDispatcher init  : " + e.toString());
            }
        }
    }    

    /**
     * This method reads the requested document from the OpenCms request context
     * and returns it to the calling module, which will usually be 
     * the running OpenCmsHttpServlet.<p>
     * 
     * In case a directory name is requested, the default files of the 
     * directory will be looked up and the first match is returned.
     *
     * @param cms the current CmsObject
     * @return CmsFile the requested file read from the VFS
     * 
     * @throws CmsException in case the file does not exist or the user has insufficient access permissions 
     */
    CmsFile initResource(CmsObject cms) throws CmsException {

        CmsFile file = null;
        // Get the requested filename from the request context
        String resourceName = cms.getRequestContext().getUri();
        CmsException tmpException = null;

        try {
            // Try to read the requested file
            file = cms.readFile(resourceName);
        } catch (CmsException e) {
            if (e.getType() == CmsException.C_NOT_FOUND) {
                // The requested file was not found
                // Check if a folder name was requested, and if so, try
                // to read the default pages in that folder

                try {
                    // Try to read the requested resource name as a folder
                    CmsFolder folder = cms.readFolder(resourceName);
                    // If above call did not throw an exception the folder
                    // was sucessfully read, so lets go on check for default 
                    // pages in the folder now

                    // Check if C_PROPERTY_DEFAULT_FILE is set on folder
                    String defaultFileName = cms.readProperty(CmsResource.getPath(cms.readAbsolutePath(folder)), I_CmsConstants.C_PROPERTY_DEFAULT_FILE);
                    if (defaultFileName != null) {
                        // Property was set, so look up this file first
                        String tmpResourceName = CmsResource.getPath(cms.readAbsolutePath(folder)) + defaultFileName;

                        try {
                            file = cms.readFile(tmpResourceName);
                            // No exception? So we have found the default file                         
                            cms.getRequestContext().getRequest().setRequestedResource(tmpResourceName);
                        } catch (CmsException exc) {
                            if (exc.getType() == CmsException.C_NO_ACCESS) {
                                // Maybe no access to default file?
                                throw exc;
                            }
                        }
                    }
                    if (file == null) {
                        // No luck with the property, so check default files specified in opencms.properties (if required)         
                        for (int i = 0; i < m_defaultFilenames.length; i++) {
                            String tmpResourceName = CmsResource.getPath(cms.readAbsolutePath(folder)) + m_defaultFilenames[i];
                            try {
                                file = cms.readFile(tmpResourceName);
                                // No exception? So we have found the default file                         
                                cms.getRequestContext().getRequest().setRequestedResource(tmpResourceName);
                                // Stop looking for default files   
                                break;
                            } catch (CmsException exc) {
                                if (exc.getType() == CmsException.C_NO_ACCESS) {
                                    // Maybe no access to default file?
                                    throw exc;
                                }
                                // Otherwise just continue looking for files
                            }
                        }
                    }
                    if (file == null) {
                        // No default file was found, throw original exception
                        throw e;
                    }
                } catch (CmsException ex) {
                    // Exception trying to read the folder (or it's properties)
                    if (ex.getType() == CmsException.C_NOT_FOUND) {
                        // Folder with the name does not exist, store original exception
                        tmpException = e;
                        // throw e;
                    } else {
                        // If the folder was found there might have been a permission problem
                        throw ex;
                    }
                }

            } else {
                // Throw the CmsException (possible cause e.g. no access permissions)
                throw e;
            }
        }

        if (file != null) {
            // test if this file is only available for internal access operations
            if ((file.getAccessFlags() & C_ACCESS_INTERNAL_READ) > 0) {
                throw new CmsException(CmsException.C_EXTXT[CmsException.C_INTERNAL_FILE] + cms.getRequestContext().getUri(), CmsException.C_INTERNAL_FILE);
            }
        }

        // test if this file has to be checked or modified
        Iterator i = m_checkFile.iterator();
        while (i.hasNext()) {
            try {
                file = ((I_CmsCheckResource)i.next()).checkResource(file, cms);
                // the loop has to be interrupted when the exception is thrown!
            } catch (CmsCheckResourceException e) {
                break;
            }
        }

        // file is still null and not found exception was thrown, so throw original exception
        if (file == null && tmpException != null) {
            throw tmpException;
        }

        // Return the file read from the VFS
        return file;
    }

    /**
     * Initialize the startup classes of this OpenCms object.<p>
     * 
     * A startup class has to be configured in the <code>registry.xml</code> 
     * file of OpenCms. Startup classes are a way to create plug-in 
     * functions that required to be initialized once at OpenCms load time 
     * without the need to add initializing code to the constructor of this 
     * class.<p>
     * 
     * This must be done only once per running OpenCms object instance.
     * Usually this will be done by the OpenCms servlet.
     * 
     * @param req the current request
     * @param res the current response 
     * @throws CmsException in case something goes wrong
     */
    void initStartupClasses(HttpServletRequest req, HttpServletResponse res) throws CmsException {
        if (m_startupClassesInitialized)
            return;

        synchronized (this) {
            // Set the initialized flag to true
            m_startupClassesInitialized = true;

            if (res == null) {
                // currently no init action depends on res, this might change in the future
            }

            if (C_LOGGING && isLogging(C_OPENCMS_INIT))
                log(C_OPENCMS_INIT, ". Startup class init   : starting");

            // set context once and for all
            String context = req.getContextPath() + req.getServletPath();
            if (context.endsWith("/"))
                context = context.substring(0, context.lastIndexOf('/'));
            A_OpenCms.setOpenCmsContext(context);
            if (C_LOGGING && isLogging(C_OPENCMS_INIT))
                log(C_OPENCMS_INIT, ". OpenCms context      : " + context);

            // check for old webapp names and extend with context
            ArrayList webAppNames = (ArrayList)A_OpenCms.getRuntimeProperty("compatibility.support.webAppNames");
            if (webAppNames == null) {
                webAppNames = new ArrayList();
            }
            if (!webAppNames.contains(context)) {
                webAppNames.add(context);
                setRuntimeProperty("compatibility.support.webAppNames", webAppNames);
            }

            // check for the JSP export URL runtime property
            String jspExportUrl = (String)getRuntimeProperty(CmsJspLoader.C_LOADER_JSPEXPORTURL);
            if (jspExportUrl == null) {
                // not initialized yet, so we use the value from the first request
                StringBuffer url = new StringBuffer(256);
                url.append(req.getScheme());
                url.append("://");
                url.append(req.getServerName());
                url.append(":");
                url.append(req.getServerPort());
                url.append(context);
                String flexExportUrl = new String(url);
                // check if the URL ends with a "/", this is not allowed
                if (flexExportUrl.endsWith(C_FOLDER_SEPARATOR)) {
                    flexExportUrl = flexExportUrl.substring(0, flexExportUrl.length() - 1);
                }
                setRuntimeProperty(CmsJspLoader.C_LOADER_JSPEXPORTURL, flexExportUrl);
                CmsJspLoader.setJspExportUrl(flexExportUrl);
                if (C_LOGGING && isLogging(C_OPENCMS_INIT))
                    log(C_OPENCMS_INIT, ". JSP export URL       : using value from first request - " + flexExportUrl);
            }

            // initialize 1 instance per class listed in the startup node          
            try {
                Hashtable startupNode = getRegistry().getSystemValues("startup");
                if (startupNode != null) {
                    for (int i = 1; i <= startupNode.size(); i++) {
                        String currentClass = (String)startupNode.get("class" + i);
                        try {
                            Class.forName(currentClass).newInstance();
                            if (C_LOGGING && isLogging(C_OPENCMS_INIT))
                                log(C_OPENCMS_INIT, ". Startup class init   : " + currentClass + " instanciated");
                        } catch (Exception e1) {
                            if (C_LOGGING && isLogging(C_OPENCMS_INIT))
                                log(C_OPENCMS_INIT, ". Startup class init   : non-critical error " + e1.toString());
                        }
                    }
                }
            } catch (Exception e2) {
                if (C_LOGGING && isLogging(C_OPENCMS_INIT))
                    log(C_OPENCMS_INIT, ". Startup class init   : non-critical error " + e2.toString());
            }

            if (C_LOGGING && A_OpenCms.isLogging(C_OPENCMS_INIT)) {
                A_OpenCms.log(C_OPENCMS_INIT, ". Startup class init   : finished");
                A_OpenCms.log(C_OPENCMS_INIT, ".                      ...............................................................");
                A_OpenCms.log(C_OPENCMS_INIT, ".");
            }
        }
    }

    /**
     * Inits a user and updates the given CmsObject withs this users information.<p>
     * 
     * @param cms the CmsObject to update
     * @param cmsReq the current I_CmsRequest (usually initialized form the HttpServletRequest)
     * @param cmsRes the current I_CmsResponse (usually initialized form the HttpServletResponse)
     * @param user the name of the user to init
     * @param group the name of the current group
     * @param currentSite the users current site 
     * @param project the id of the current project
     * @param sessionStorage the session storage for this OpenCms instance
     * @throws CmsException in case something goes wrong
     */
    public void initUser(
        CmsObject cms, 
        I_CmsRequest cmsReq,
        I_CmsResponse cmsRes, 
        String user, 
        String group, 
        String currentSite, 
        int project, 
        CmsCoreSession sessionStorage
    ) throws CmsException {
        cms.init(m_driverManager, cmsReq, cmsRes, user, group, project, currentSite, sessionStorage, m_directoryTranslator, m_fileTranslator);
    }

    /**
     * Sets the mimetype of the response.<p>
     * 
     * The mimetype is selected by the file extension of the requested document.
     * If no available mimetype is found, it is set to the default
     * "application/octet-stream".
     *
     * @param cms The current initialized CmsObject
     * @param file The requested document
     */
    void setResponse(CmsObject cms, CmsFile file) {
        String mimetype = null;
        int lastDot = file.getResourceName().lastIndexOf(".");
        // check if there was a file extension
        if ((lastDot > 0) && (lastDot < (file.getResourceName().length() - 1))) {
            String ext = file.getResourceName().substring(lastDot + 1);
            mimetype = (String)m_mt.get(ext);
            // was there a mimetype fo this extension?
            if (mimetype == null) {
                mimetype = C_DEFAULT_MIMETYPE;
            }
        } else {
            mimetype = C_DEFAULT_MIMETYPE;
        }
        mimetype = mimetype.toLowerCase();
        if (mimetype.startsWith("text") && (mimetype.indexOf("charset") == -1)) {
            mimetype += "; charset=" + cms.getRequestContext().getEncoding();
        }
        cms.getRequestContext().getResponse().setContentType(mimetype);
    }

    /**    
     * Delivers (i.e. shows) the requested resource to the user.<p>
     * 
     * @param req the current http request
     * @param res the current http response
     * @param cms the curren cms context
     * @param file the requested file
     * @throws CmsException if something goes wrong
     * @throws ServletException if some other things goes wrong
     * @throws IOException if io things go wrong
     */
    public void showResource(
        HttpServletRequest req, 
        HttpServletResponse res, 
        CmsObject cms, 
        CmsFile file
    ) throws CmsException, ServletException, IOException {
        I_CmsResourceLoader loader = getLoaderManager().getLoader(file.getLoaderId());
        loader.load(cms, file, req, res);
    }

    /**
     * Starts a scheduled job with a correct instantiated CmsObject.<p>
     * 
     * @param entry the CmsCronEntry to start.
     */
    void startScheduleJob(CmsCronEntry entry) {
        // create a valid cms-object
        CmsObject cms = new CmsObject();
        try {
            initUser(cms, null, null, entry.getUserName(), entry.getGroupName(), C_VFS_DEFAULT, C_PROJECT_ONLINE_ID, null);
            // create a new ScheduleJob and start it
            CmsCronScheduleJob job = new CmsCronScheduleJob(cms, entry);
            job.start();
        } catch (Exception exc) {
            if (C_LOGGING && isLogging(C_OPENCMS_CRONSCHEDULER)) {
                log(C_OPENCMS_CRONSCHEDULER, "Error initialising job for " + entry + " Error: " + Utils.getStackTrace(exc));
            }
        }
    }

    /**
     * Reads the current cron entries from the database and updates the Crontable.<p>
     */
    void updateCronTable() {
        try {
            m_table.update(m_driverManager.readCronTable());
        } catch (Exception exc) {
            if (C_LOGGING && isLogging(C_OPENCMS_CRITICAL)) {
                log(C_OPENCMS_CRITICAL, "[OpenCms] crontable corrupt. Scheduler is now disabled!");
            }
        }
    }
}