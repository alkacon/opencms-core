/*
* File   : $Source: /alkacon/cvs/opencms/src/com/opencms/core/Attic/OpenCms.java,v $
* Date   : $Date: 2002/12/06 16:01:20 $
* Version: $Revision: 1.100 $
*
* This library is part of OpenCms -
* the Open Source Content Mananagement System
*
* Copyright (C) 2001  The OpenCms Group
*
* This library is free software; you can redistribute it and/or
* modify it under the terms of the GNU Lesser General Public
* License as published by the Free Software Foundation; either
* version 2.1 of the License, or (at your option) any later version.
*
* This library is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
* Lesser General Public License for more details.
*
* For further information about OpenCms, please see the
* OpenCms Website: http://www.opencms.org
*
* You should have received a copy of the GNU Lesser General Public
* License along with this library; if not, write to the Free Software
* Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
*/

package com.opencms.core;

import com.opencms.boot.CmsBase;
import com.opencms.boot.I_CmsLogChannels;
import com.opencms.file.CmsFile;
import com.opencms.file.CmsFolder;
import com.opencms.file.CmsObject;
import com.opencms.file.CmsRbManager;
import com.opencms.file.CmsStaticExport;
import com.opencms.file.I_CmsRegistry;
import com.opencms.file.I_CmsResourceBroker;
import com.opencms.flex.CmsJspLoader;
import com.opencms.flex.util.CmsResourceTranslator;
import com.opencms.launcher.CmsLauncherManager;
import com.opencms.launcher.I_CmsLauncher;
import com.opencms.template.cache.CmsElementCache;
import com.opencms.util.Utils;

import java.util.Hashtable;
import java.util.Vector;

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
 * @author Michael Emmerich
 * @author Alexander Lucas
 * @author Alexander Kandzior (a.kandzior@alkacon.com)
 * 
 * @version $Revision: 1.100 $ $Date: 2002/12/06 16:01:20 $
 */
public class OpenCms extends A_OpenCms implements I_CmsConstants,I_CmsLogChannels {

    /**
     * Define the default file.encoding that should be used by OpenCms
     */
    private static final String C_PREFERED_FILE_ENCODING = "ISO8859_1";

    /**
     * The default mimetype
     */
    private static final String C_DEFAULT_MIMETYPE = "text/html";
    
    /**
     * The resource-broker to access the database.
     */
    private static I_CmsResourceBroker c_rb;

    /**
     * The cron scheduler to schedule the cronjobs
     */
    private CmsCronScheduler m_scheduler;

    /**
     * The cron table to use with the scheduler
     */
    private CmsCronTable m_table;

    /**
     * Reference to the OpenCms launcer manager
     */
    private CmsLauncherManager m_launcherManager;

    /**
     * Hashtable with all available Mimetypes.
     */
    private Hashtable m_mt = new Hashtable();

    /**
     * Indicates, if the session-failover should be enabled or not
     */
    private boolean m_sessionFailover = false;

    /**
     * Indicates, if the streaming should be enabled by the configurations.
     */
    private boolean m_streaming = true;

    /**
     * The name of the class used to validate a new password
     */
    private static String c_passwordValidatingClass = "";

    /**
     * Indicates, if the element cache should be enabled by the configurations
     */
    private boolean m_enableElementCache = true;

    /**
     * Reference to the CmsElementCache object containing locators for all
     * URIs and elements in cache
     */
    private static CmsElementCache c_elementCache = null;

    /**
     * The object to store the  properties from the opencms.property file for the
     * static export
     */
    private static CmsStaticExportProperties c_exportProperties = new CmsStaticExportProperties();

    /**
     * In this hashtable the dependencies for all variants in the elementcache
     * are stored. The keys are Strings with resourceNames like "/siteX/cos/ContentClass/news4"
     * and the value is a Vector with strings (The elementvariants that depend on the keys)
     * like "ElementClass|ElementTemplate|VariantCacheKey"
     */
    private static Hashtable c_variantDeps = null;
    
    /**
     * Directory translator, used to translate all access to resources
     */
    private static CmsResourceTranslator m_directoryTranslator = null;

    /**
     * Filename translator, used only for the creation of new files
     */
    private static CmsResourceTranslator m_fileTranslator = null;
    
    /**
     * List of fefault file names (for directories, e.g, "index.html")
     */
    private static String[] m_defaultFilenames = null;

    /**
     * Flag to indicate if the startup classes have already been initialized
     */
    private boolean isInitialized = false;

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
     */
    public OpenCms(Configurations conf) throws Exception {

        CmsObject cms;
        
        // Save the configuration
        setConfiguration(conf);

        // invoke the ResourceBroker via the initalizer
        try {
            if(C_LOGGING && isLogging(C_OPENCMS_INIT)) {
                log(C_OPENCMS_INIT, "[OpenCmsServlet] logging started");
                String jdkinfo = System.getProperty("java.vm.name") + " ";
                jdkinfo += System.getProperty("java.vm.version") + " ";
                jdkinfo += System.getProperty("java.vm.info") + " ";
                jdkinfo += System.getProperty("java.vm.vendor") + " ";
                log(C_OPENCMS_INIT, "[OpenCmsServlet] JDK Info: " + jdkinfo);

                String osinfo = System.getProperty("os.name") + " ";
                osinfo += System.getProperty("os.version") + " ";
                osinfo += System.getProperty("os.arch") + " ";
                log(C_OPENCMS_INIT, "[OpenCmsServlet] OS Info: " + osinfo);

                log(C_OPENCMS_INIT, "[OpenCmsServlet] file.encoding: " + System.getProperty("file.encoding"));
                log(C_OPENCMS_INIT, "[OpenCms] creating first cms-object...");
            }
            cms = new CmsObject();
            m_sessionFailover = conf.getBoolean("sessionfailover.enabled", false);
        } catch(Exception e) {
            if(C_LOGGING && isLogging(C_OPENCMS_CRITICAL)) log(C_OPENCMS_CRITICAL, "[OpenCms] Startup init failed - " + e.getMessage());
            // any exception here is fatal and will cause a stop in processing
            throw e;
        }
        
        try {           
            // init the rb via the manager with the configuration
            // and init the cms-object with the rb.
            if(C_LOGGING && isLogging(C_OPENCMS_INIT)) log(C_OPENCMS_INIT, "[OpenCms] initializing the main resource-broker");
            c_rb = CmsRbManager.init(conf);
        } catch(Exception e) {
            if(C_LOGGING && isLogging(C_OPENCMS_CRITICAL)) log(C_OPENCMS_CRITICAL, "[OpenCms] Database init failed - " + e.getMessage());
            // any exception here is fatal and will cause a stop in processing
            throw new CmsException("Database init failed", CmsException.C_RB_INIT_ERROR, e);
        }
        
        try {       
            printCopyrightInformation(cms);

            // initalize the Hashtable with all available mimetypes
            if(C_LOGGING && isLogging(C_OPENCMS_INIT)) log(C_OPENCMS_INIT, "[OpenCms] read mime types");
            m_mt = c_rb.readMimeTypes(null, null);
            if(C_LOGGING && isLogging(C_OPENCMS_INIT)) log(C_OPENCMS_INIT, "[OpenCms] found " + m_mt.size() + " mime-type entrys");

            // Check, if the HTTP streaming should be enabled
            m_streaming = conf.getBoolean("httpstreaming.enabled", true);
            if(C_LOGGING && isLogging(C_OPENCMS_INIT)) log(C_OPENCMS_INIT, "[OpenCms] HTTP streaming " + (m_streaming?"en":"dis") + "abled. ");

            // if the System property opencms.disableScheduler is set to true, don't start scheduling
            if(!new Boolean(System.getProperty("opencms.disableScheduler")).booleanValue()) {
                // now initialise the OpenCms scheduler to launch cronjobs
                m_table = new CmsCronTable(c_rb.readCronTable(null, null));
                m_scheduler = new CmsCronScheduler(this, m_table);
                if(C_LOGGING && isLogging(C_OPENCMS_INIT)) log(C_OPENCMS_INIT, "[OpenCmsServlet] initializing CmsCronScheduler... DONE");
            } else {
                if(C_LOGGING && isLogging(C_OPENCMS_INIT)) log(C_OPENCMS_INIT, "[OpenCmsServlet] CmsCronScheduler is disabled!");
            }
        } catch(Exception e) {
            if(C_LOGGING && isLogging(C_OPENCMS_CRITICAL)) log(C_OPENCMS_CRITICAL, "[OpenCms] Type init falied - " + e.getMessage());
            // any exception here is fatal and will cause a stop in processing
            throw e;
        }
        
        // read flex jsp export url property and save in runtime configuration
        String flexExportUrl = (String)conf.getString(CmsJspLoader.C_LOADER_JSPEXPORTURL, null);
        if (null != flexExportUrl) {
            // if JSP export URL is null it will be set in initStartupClasses()
            if (flexExportUrl.endsWith(C_FOLDER_SEPERATOR)) {
                flexExportUrl = flexExportUrl.substring(0, flexExportUrl.length()-1);
            }
            setRuntimeProperty(CmsJspLoader.C_LOADER_JSPEXPORTURL, flexExportUrl);
            if(C_LOGGING && isLogging(C_OPENCMS_INIT))
                log(C_OPENCMS_INIT, "[OpenCms] setting JSP export URL to value from opencms.properties: " + flexExportUrl);
        }

        
        // try to initialize directory translations
        try {
            if(C_LOGGING && isLogging(C_OPENCMS_INIT)) log(C_OPENCMS_INIT, "[OpenCms] initializing directory translations...");
            boolean translationEnabled = conf.getBoolean("directory.translation.enabled", false);
            if (translationEnabled) {
                String[] translations = conf.getStringArray("directory.translation.rules");
                // Directory translation stops after fist match, hence the "false" parameter
                m_directoryTranslator = new CmsResourceTranslator(translations, false);        
            }
        } catch(Exception e) {
            if(C_LOGGING && isLogging(C_OPENCMS_INIT)) log(C_OPENCMS_INIT, "[OpenCms] " + e.toString());
        }   
        // make sure we always have at last an emtpy array      
        if (m_directoryTranslator == null) m_directoryTranslator = new CmsResourceTranslator(new String[0], false);
        
        // try to initialize filename translations
        try {
            if(C_LOGGING && isLogging(C_OPENCMS_INIT)) log(C_OPENCMS_INIT, "[OpenCms] initializing filename translations...");
            boolean translationEnabled = conf.getBoolean("filename.translation.enabled", false);
            if (translationEnabled) {
                String[] translations = conf.getStringArray("filename.translation.rules");
                // Filename translations applies all rules, hence the true patameters
                m_fileTranslator = new CmsResourceTranslator(translations, true);        
            }
        } catch(Exception e) {
            if(C_LOGGING && isLogging(C_OPENCMS_INIT)) log(C_OPENCMS_INIT, "[OpenCms] " + e.toString());
        }           
        // make sure we always have at last an emtpy array      
        if (m_fileTranslator == null) m_fileTranslator = new CmsResourceTranslator(new String[0], false);
                    
        // try to initialize default file names
        try {
            if(C_LOGGING && isLogging(C_OPENCMS_INIT)) log(C_OPENCMS_INIT, "[OpenCms] initializing directory default files...");
            m_defaultFilenames = conf.getStringArray("directory.default.files");
            for (int i=0; i<m_defaultFilenames.length; i++) {
                // remove possible white space
                m_defaultFilenames[i] = m_defaultFilenames[i].trim();
                if(C_LOGGING && isLogging(C_OPENCMS_INIT)) log(C_OPENCMS_INIT, "[OpenCms] directory default file " + i + " is: " + m_defaultFilenames[i] );               
            }
        } catch(Exception e) {
            if(C_LOGGING && isLogging(C_OPENCMS_INIT)) log(C_OPENCMS_INIT, "[OpenCms] " + e.toString());
        }    
        // make sure we always have at last an emtpy array      
        if (m_defaultFilenames == null) m_defaultFilenames = new String[0];                
                
        // try to initialize the flex cache
        try {
            if(C_LOGGING && isLogging(C_OPENCMS_INIT)) log(C_OPENCMS_INIT, "[OpenCms] initializing flex cache...");
            com.opencms.flex.cache.CmsFlexCache flexCache = new com.opencms.flex.cache.CmsFlexCache(this);
        } catch(Exception e) {
            if(C_LOGGING && isLogging(C_OPENCMS_INIT)) log(C_OPENCMS_INIT, "[OpenCms] " + e.toString());
        }        

        // try to initialize the launchers.
        try {
            if(C_LOGGING && isLogging(C_OPENCMS_INIT)) log(C_OPENCMS_INIT, "[OpenCms] initialize launchers...");
            m_launcherManager = new CmsLauncherManager(this);
        }
        catch(Exception e) {
            if(C_LOGGING && isLogging(C_OPENCMS_INIT)) log(C_OPENCMS_INIT, "[OpenCms] " + e.getMessage());
        }

        // get the password validating class
        c_passwordValidatingClass = conf.getString("passwordvalidatingclass", "com.opencms.util.PasswordValidtation");

        // Check, if the element cache should be enabled
        m_enableElementCache = conf.getBoolean("elementcache.enabled", false);
        if(C_LOGGING && isLogging(C_OPENCMS_INIT)) log(C_OPENCMS_INIT, "[OpenCms] element cache " + (m_enableElementCache?"en":"dis") + "abled. ");
        if(m_enableElementCache) {
            try {
                c_elementCache = new CmsElementCache(conf.getInteger("elementcache.uri", 10000),
                                                    conf.getInteger("elementcache.elements", 50000),
                                                    conf.getInteger("elementcache.variants", 100));
            }catch(Exception e) {
                if(C_LOGGING && isLogging(C_OPENCMS_INIT)) log(C_OPENCMS_INIT, "[OpenCms] " + e.getMessage());
            }
            c_variantDeps = new Hashtable();
            c_elementCache.getElementLocator().setExternDependencies(c_variantDeps);
        }
        // now for the link replacement rules there are up to three rulesets for export online and offline
        try{
            if(C_LOGGING && isLogging(C_OPENCMS_INIT)) log(C_OPENCMS_INIT, "[OpenCms] initializing link replace rules.");
            
            String[] staticUrlPrefix = new String[4];
            staticUrlPrefix[0]=Utils.replace(conf.getString(C_URL_PREFIX_EXPORT, ""), C_WEB_APP_REPLACE_KEY, CmsBase.getWebAppName());
            staticUrlPrefix[1]=Utils.replace(conf.getString(C_URL_PREFIX_HTTP, ""), C_WEB_APP_REPLACE_KEY, CmsBase.getWebAppName());
            staticUrlPrefix[2]=Utils.replace(conf.getString(C_URL_PREFIX_HTTPS, ""), C_WEB_APP_REPLACE_KEY, CmsBase.getWebAppName());
            staticUrlPrefix[3]=Utils.replace(conf.getString(C_URL_PREFIX_SERVERNAME, ""), C_WEB_APP_REPLACE_KEY, CmsBase.getWebAppName());
            c_exportProperties.setUrlPrefixArray(staticUrlPrefix);
            // to get the right rulesets we need the default value for the export property
            String exportDefault = conf.getString("staticexport.default.export", "true");
            c_exportProperties.setExportDefaultValue(exportDefault);
            String export = conf.getString("linkrules."+exportDefault+".export");
            String[] linkRulesExport;
            if(export != null && !"".equals(export)){
                linkRulesExport = conf.getStringArray("ruleset."+export);
                // now replace ${WEB_APP_NAME} with the correct name of the webapplication and replace the other variables
                for(int i = 0; i < linkRulesExport.length; i++) {
                    linkRulesExport[i] = Utils.replace(linkRulesExport[i], C_WEB_APP_REPLACE_KEY, CmsBase.getWebAppName());
                    linkRulesExport[i] = Utils.replace(linkRulesExport[i], "${"+C_URL_PREFIX_EXPORT+"}", staticUrlPrefix[0]);
                    linkRulesExport[i] = Utils.replace(linkRulesExport[i], "${"+C_URL_PREFIX_HTTP+"}", staticUrlPrefix[1]);
                    linkRulesExport[i] = Utils.replace(linkRulesExport[i], "${"+C_URL_PREFIX_HTTPS+"}", staticUrlPrefix[2]);
                    linkRulesExport[i] = Utils.replace(linkRulesExport[i], "${"+C_URL_PREFIX_SERVERNAME+"}", staticUrlPrefix[3]);
                }
                c_exportProperties.setLinkRulesExport(linkRulesExport);
            }
            String online = conf.getString("linkrules."+exportDefault+".online");
            String[] linkRulesOnline;
            if(online != null && !"".equals(online)){
                linkRulesOnline = conf.getStringArray("ruleset."+online);
                // now replace ${WEB_APP_NAME} with the correct name of the webapplication and replace the other variables
                for(int i = 0; i < linkRulesOnline.length; i++) {
                    linkRulesOnline[i] = Utils.replace(linkRulesOnline[i], C_WEB_APP_REPLACE_KEY, CmsBase.getWebAppName());
                    linkRulesOnline[i] = Utils.replace(linkRulesOnline[i], "${"+C_URL_PREFIX_EXPORT+"}", staticUrlPrefix[0]);
                    linkRulesOnline[i] = Utils.replace(linkRulesOnline[i], "${"+C_URL_PREFIX_HTTP+"}", staticUrlPrefix[1]);
                    linkRulesOnline[i] = Utils.replace(linkRulesOnline[i], "${"+C_URL_PREFIX_HTTPS+"}", staticUrlPrefix[2]);
                    linkRulesOnline[i] = Utils.replace(linkRulesOnline[i], "${"+C_URL_PREFIX_SERVERNAME+"}", staticUrlPrefix[3]);
                }
                c_exportProperties.setLinkRulesOnline(linkRulesOnline);
            }
            String offline = conf.getString("linkrules."+exportDefault+".offline");
            String[] linkRulesOffline;
            if(offline != null && !"".equals(offline)){
                linkRulesOffline = conf.getStringArray("ruleset."+offline);
                // now replace ${WEB_APP_NAME} with the correct name of the webapplication and replace the other variables
                for(int i = 0; i < linkRulesOffline.length; i++) {
                    linkRulesOffline[i] = Utils.replace(linkRulesOffline[i], C_WEB_APP_REPLACE_KEY, CmsBase.getWebAppName());
                    linkRulesOffline[i] = Utils.replace(linkRulesOffline[i], "${"+C_URL_PREFIX_EXPORT+"}", staticUrlPrefix[0]);
                    linkRulesOffline[i] = Utils.replace(linkRulesOffline[i], "${"+C_URL_PREFIX_HTTP+"}", staticUrlPrefix[1]);
                    linkRulesOffline[i] = Utils.replace(linkRulesOffline[i], "${"+C_URL_PREFIX_HTTPS+"}", staticUrlPrefix[2]);
                    linkRulesOffline[i] = Utils.replace(linkRulesOffline[i], "${"+C_URL_PREFIX_SERVERNAME+"}", staticUrlPrefix[3]);
                }
                c_exportProperties.setLinkRulesOffline(linkRulesOffline);
            }
            String extern = conf.getString("linkrules."+exportDefault+".extern");
            String[] linkRulesExtern;
            if(extern != null && !"".equals(extern)){
                linkRulesExtern = conf.getStringArray("ruleset."+extern);
                // now replace ${WEB_APP_NAME} with the correct name of the webapplication and replace the other variables
                for(int i = 0; i < linkRulesExtern.length; i++) {
                    linkRulesExtern[i] = Utils.replace(linkRulesExtern[i], C_WEB_APP_REPLACE_KEY, CmsBase.getWebAppName());
                    linkRulesExtern[i] = Utils.replace(linkRulesExtern[i], "${"+C_URL_PREFIX_EXPORT+"}", staticUrlPrefix[0]);
                    linkRulesExtern[i] = Utils.replace(linkRulesExtern[i], "${"+C_URL_PREFIX_HTTP+"}", staticUrlPrefix[1]);
                    linkRulesExtern[i] = Utils.replace(linkRulesExtern[i], "${"+C_URL_PREFIX_HTTPS+"}", staticUrlPrefix[2]);
                    linkRulesExtern[i] = Utils.replace(linkRulesExtern[i], "${"+C_URL_PREFIX_SERVERNAME+"}", staticUrlPrefix[3]);
                }
                c_exportProperties.setLinkRulesExtern(linkRulesExtern);
            }
            c_exportProperties.setStartRule(null); // temporary out of order: conf.getString("exportfirstrule");

            Vector staticExportStart=new Vector();
            staticExportStart.add("/");
            c_exportProperties.setStartPoints(staticExportStart);

            // at last the target for the export
            c_exportProperties.setExportPath( com.opencms.boot.CmsBase.getAbsoluteWebPath(CmsBase.getAbsoluteWebPath(conf.getString(C_STATICEXPORT_PATH))));

            // should the links in static export be relative?
            c_exportProperties.setExportRelativeLinks(conf.getBoolean("relativelinks_in_export", false));
            // is the static export enabled?
            String activCheck = conf.getString("staticexport.enabled", "false");
            c_exportProperties.setStaticExportEnabledValue(activCheck);
            if("true".equalsIgnoreCase(activCheck)){
                c_exportProperties.setStaticExportEnabled(true);
            }else{
                c_exportProperties.setStaticExportEnabled(false);
            }
            if(c_exportProperties.isStaticExportEnabled()){
                // we have to generate the dynamic rulessets
                createDynamicLinkRules();
            }else{
                if("false_ssl".equalsIgnoreCase(activCheck)){
                    // no static esport, but we need the dynamic rules for setting the protokoll to https
                    c_exportProperties.setLinkRulesOffline(new String[]{"s#^#" + staticUrlPrefix[1] + "#"});
                    c_exportProperties.setLinkRulesOnline(new String[]{"*dynamicRules*", "s#^#" + staticUrlPrefix[1] + "#"});
                    // and we have to change the standart export prefix to stay in opencms
                    c_exportProperties.getUrlPrefixArray()[0] = staticUrlPrefix[1];
                    // if we need them we should create them
                    createDynamicLinkRules();
                }else{
                    // no static export. We need online and offline rules to stay in OpenCms.
                    // we generate them with the url_prefix_http so the user can still configure
                    // the servletpath.
                    c_exportProperties.setLinkRulesOffline(new String[]{"s#^#" + staticUrlPrefix[1] + "#"});
                    c_exportProperties.setLinkRulesOnline(new String[]{"s#^#" + staticUrlPrefix[1] + "#"});
                }
            }
        }catch(Exception e){
            if(C_LOGGING && isLogging(C_OPENCMS_INIT)) log(C_OPENCMS_INIT, "[OpenCms] Exception initializing link rules: " + e.toString());
        }

        // Encoding project:
        String defaultEncoding = conf.getString("defaultContentEncoding", "UTF-8");        
        try {
            // This will work with Java 1.4+ only
            if (!java.nio.charset.Charset.isSupported(defaultEncoding)) {
                defaultEncoding = getDefaultEncoding();
            }
        } catch (Throwable t) {
            // Will be thrown in Java < 1.4 (NoSuchMethodException etc.)
            // In Java < 1.4 there is no easy way to check if encoding is supported,
            // so you must make sure your setting in "opencms.properties" is correct.             
        }        
        setDefaultEncoding(defaultEncoding);
        if(C_LOGGING && isLogging(C_OPENCMS_INIT)) log(C_OPENCMS_INIT, "[OpenCms] Default encoding set to: " + defaultEncoding);
        
    }

    /**
     * Creates the dynamic linkrules.
     * The CmsStaticExport class needs a CmsObject to create them.
     */
    private void createDynamicLinkRules(){
        //create a valid cms-object
        CmsObject cms = new CmsObject();
        try{
            initUser(cms, null, null, C_USER_ADMIN, C_GROUP_ADMIN, C_PROJECT_ONLINE_ID, null);
            new CmsStaticExport(cms, null, false, null, null, null, null);
        }catch(Exception e){
            if(C_LOGGING && isLogging(C_OPENCMS_INIT)) {
                log(C_OPENCMS_INIT, "Error initialising dynamic link rules. Error: " + Utils.getStackTrace(e));
            }
        }
    }
    
    /**
     * Initialize the startup classes of this OpenCms object.<p>
     * 
     * A startup class has to be configured in the <code>registry.xml</code> 
     * file of OpenCms. Startup classes are a way to create plug-in 
     * functions that required to be initialized one at OpenCms load time 
     * without the need to add initializing code to the constructor of this 
     * class.<p>
     * 
     * This must be done only once per running OpenCms object instance.
     * Usually this will be done by the OpenCms servlet. 
     */
    void initStartupClasses(HttpServletRequest req, HttpServletResponse res) throws CmsException {
        if (isInitialized) return;

        // Set the initialized flag to true
        isInitialized = true;

        if(C_LOGGING && isLogging(C_OPENCMS_INIT)) log(C_OPENCMS_INIT, "[OpenCms] OpenCms init() starting.");

        // check for the JSP export URL runtime property
        String jspExportUrl = (String)getRuntimeProperty(CmsJspLoader.C_LOADER_JSPEXPORTURL);
        if (jspExportUrl == null) {
            StringBuffer url = new StringBuffer(256);
            url.append(req.getScheme());
            url.append("://");
            url.append(req.getServerName());
            url.append(":");
            url.append(req.getServerPort());
            url.append(req.getContextPath());
            url.append(req.getServletPath());        
            String flexExportUrl = new String(url);    
            // check if the URL ends with a "/", this is not allowed
            if (flexExportUrl.endsWith(C_FOLDER_SEPERATOR)) {
                flexExportUrl = flexExportUrl.substring(0, flexExportUrl.length()-1);
            }
            setRuntimeProperty(CmsJspLoader.C_LOADER_JSPEXPORTURL, flexExportUrl);
            CmsJspLoader.setJspExportUrl(flexExportUrl);
            if(C_LOGGING && isLogging(C_OPENCMS_INIT)) 
                log(C_OPENCMS_INIT, "[OpenCms] Setting JSP export URL to value from first request: " + flexExportUrl);
        }
        

        // initialize 1 instance per class listed in the startup node
        try{
            Hashtable startupNode = getRegistry().getSystemValues( "startup" );
            if (startupNode!=null) {
                for (int i=1;i<=startupNode.size();i++) {
                    String currentClass = (String)startupNode.get( "class" + i );
                    try {
                        Class.forName(currentClass).newInstance();

                        if(C_LOGGING && isLogging(C_OPENCMS_INIT)) log(C_OPENCMS_INIT, "[OpenCms] created instance of class " + currentClass );
                    } catch (Exception e1){
                        if(C_LOGGING && isLogging(C_OPENCMS_INIT)) log(C_OPENCMS_INIT, "[OpenCms] Exception creating instance of startup class " +  currentClass + ": " + e1.toString());
                    }
                }
            }
        } catch (Exception e2){
            if(C_LOGGING && isLogging(C_OPENCMS_INIT)) log(C_OPENCMS_INIT, "[OpenCms] Exception creating startup classes: " + e2.toString());
        }

        if(C_LOGGING && isLogging(C_OPENCMS_INIT)) log(C_OPENCMS_INIT, "[OpenCms] OpenCms init() finished.");
    }

    /**
     * Destructor, called when the the servlet is shut down.
     * 
     * @throws CmsException If something goes wrong during shutdown.
     */
    public void destroy() throws CmsException {
        CmsObject cms = new CmsObject();
        cms.init(c_rb);
        cms.destroy();
    }

    /**
     * Returns the launcher manager used.
     * 
     * @return The launcher manager used.
     */
    public CmsLauncherManager getLauncherManager() {
        return m_launcherManager;
    }

    /**
     * Returns the ElementCache used for the online project.
     * 
     * @return The ElementCache used for the online project.
     */
    public static CmsElementCache getOnlineElementCache(){
        return c_elementCache;
    }

    /**
     * Returns the Class that is used for the password validation.
     * 
     * @return The Class that is used for the password validation.
     */
    public static String getPasswordValidatingClass(){
        return c_passwordValidatingClass;
    }

    /**
     * Returns the properties for the static export.
     * 
     * @return The properties for the static export.
     */
    public static CmsStaticExportProperties getStaticExportProperties(){
        return c_exportProperties;
    }

    /**
     * Returns the hashtable with the variant dependencies used for the elementcache.
     * 
     * @return The hashtable with the variant dependencies used for the elementcache.
     */
    public static Hashtable getVariantDependencies(){
        return c_variantDeps;
    }
    
    /**
     * Returns the file name translator this OpenCms has read from the opencms.properties.
     * 
     * @return The file name translator this OpenCms has read from the opencms.properties
     */
    public CmsResourceTranslator getFileTranslator() {
        return m_fileTranslator;
    } 
    
    /**
     * This method reads the requested document from the OpenCms request context
     * and returns it to the calling module, which will usually be 
     * the running OpenCmsHttpServlet.<p>
     * 
     * In case a directory name is requested, the default files of the 
     * directory will be looked up and the first match is returned.
     *
     * @param cms The current CmsObject
     * @return CmsFile The requested file read from the VFS
     * 
     * @throws CmsException In case the file does not exist or the user has insufficient access permissions 
     */
	CmsFile initResource(CmsObject cms) throws CmsException {

		CmsFile file = null;
		// Get the requested filename from the request context
		String resourceName = cms.getRequestContext().getUri();

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
                    String defaultFileName = cms.readProperty(folder.getPath(), I_CmsConstants.C_PROPERTY_DEFAULT_FILE);
                    if (defaultFileName != null) {
                        // Property was set, so look up this file first
                        String tmpResourceName = folder.getPath() + defaultFileName;
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
                        for (int i=0; i<m_defaultFilenames.length; i++) {
                            String tmpResourceName = folder.getPath() + m_defaultFilenames[i];
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
                        // Folder with the name does not exist, throw original exception
                        throw e;
                    }
                    // If the folder was found there might have been a permission problem
                    throw ex;
				}

			} else {
				// Throw the CmsException (possible cause e.g. no access permissions)
				throw e;
			}
		}

		if (file != null) {
			// Test if this file is only available for internal access operations
			if ((file.getAccessFlags() & C_ACCESS_INTERNAL_READ) > 0) {
				throw new CmsException(
					CmsException.C_EXTXT[CmsException.C_INTERNAL_FILE]
						+ cms.getRequestContext().getUri(),
					CmsException.C_INTERNAL_FILE);
			}
		}
        
        // Return the file read from the VFS
		return file;
	}

    /**
     * Inits a user and updates the given CmsObject withs this users information.<p>
     * 
     * @param cms The CmsObject to update
     * @param cmsReq The current I_CmsRequest (usually initialized form the HttpServletRequest)
     * @param cmsRes The current I_CmsResponse (usually initialized form the HttpServletResponse)
     * @param user The name of the user to init
     * @param group The name of the current group
     * @param project The id of the current project
     * @param sessionStorage The session storage for this OpenCms instance
     */
    public void initUser(CmsObject cms, I_CmsRequest cmsReq, I_CmsResponse cmsRes, String user,
            String group, int project, CmsCoreSession sessionStorage) throws CmsException {

        if((!m_enableElementCache) || (project == C_PROJECT_ONLINE_ID)){
            cms.init(c_rb, cmsReq, cmsRes, user, group, project, m_streaming, c_elementCache, sessionStorage, m_directoryTranslator, m_fileTranslator);
        }else{
            cms.init(c_rb, cmsReq, cmsRes, user, group, project, m_streaming, new CmsElementCache(10,200,10), sessionStorage, m_directoryTranslator, m_fileTranslator);
        }
    }

    /**
     * Prints the OpenCms copyright information to all log-files.<p>
     */
    private void printCopyrightInformation(CmsObject cms) {
        String copy[] = cms.copyright();

        // log to error-stream
        System.err.println("\n\nStarting OpenCms, version " + cms.version());
        for(int i = 0;i < copy.length;i++) {
            System.err.println(copy[i]);
        }

        // log with opencms-logger
        if(C_PREPROCESSOR_IS_LOGGING && isLogging(C_OPENCMS_INFO)) {
            log(C_OPENCMS_INFO, cms.version());
            for(int i = 0;i < copy.length;i++) {
                log(C_OPENCMS_INFO, copy[i]);
            }
        }
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
        int lastDot = file.getName().lastIndexOf(".");
        // check if there was a file extension
        if((lastDot > 0) && (lastDot < (file.getName().length()-1))) {
            String ext = file.getName().substring(lastDot + 1, file.getName().length());
            mimetype = (String)m_mt.get(ext);
            // was there a mimetype fo this extension?
            if(mimetype == null) {
                mimetype = C_DEFAULT_MIMETYPE;
            }
        } else {
            mimetype = C_DEFAULT_MIMETYPE;
        }
        mimetype = mimetype.toLowerCase();
        if (mimetype.startsWith("text")
            && (mimetype.indexOf("charset") == -1)) {
            mimetype += "; charset=" + cms.getRequestContext().getEncoding();
        }
        cms.getRequestContext().getResponse().setContentType(mimetype);
    }

    /**
     * Selects the appropriate launcher for a given file by analyzing the
     * file's launcher id and calls the initlaunch() method to initiate the
     * generating of the output.
     *
     * @param cms CmsObject containing all document and user information
     * @param file CmsFile object representing the selected file.
     * @throws CmsException In case of problems acessing the resource.
     */
    public void showResource(CmsObject cms, CmsFile file) throws CmsException {
        int launcherId = file.getLauncherType();
        String startTemplateClass = file.getLauncherClassname();
        I_CmsLauncher launcher = m_launcherManager.getLauncher(launcherId);
        if(launcher == null) {
            String errorMessage = "Could not launch file " + file.getName() + ". Launcher for requested launcher ID "
                    + launcherId + " could not be found.";
            if(C_LOGGING && isLogging(C_OPENCMS_INFO)) {
                log(C_OPENCMS_INFO, "[OpenCms] " + errorMessage);
            }
            throw new CmsException(errorMessage, CmsException.C_UNKNOWN_EXCEPTION);
        }
        cms.setLauncherManager(m_launcherManager);
        launcher.initlaunch(cms, file, startTemplateClass, this);
    }

    /**
     * This method loads old sessiondata from the database. It is used
     * for session failover.
     *
     * @param oldSessionId the id of the old session.
     * @return the old sessiondata.
     */
    Hashtable restoreSession(String oldSessionId) throws CmsException {
        // is session-failopver enabled?
        if(m_sessionFailover) {
            // yes
            return c_rb.restoreSession(oldSessionId);
        }
        else {
            // no - do nothing
            return null;
        }
    }
    
    /**
     * This method stores sessiondata into the database. It is used
     * for session failover.
     *
     * @param sessionId the id of the session.
     * @param isNew determines, if the session is new or not.
     * @return data the sessionData.
     */
    void storeSession(String sessionId, Hashtable sessionData) throws CmsException {
        // is session failover enabled?
        if(m_sessionFailover) {
            // yes
            c_rb.storeSession(sessionId, sessionData);
        }
    }

    /**
     * Returns the registry to read values from it. 
     * You don't have the permissions to write values. 
     * This is useful for modules to read module-parameters.
     *
     * @return The registry to READ values from it.
     * 
     * @exception CmsException, if the registry can not be returned.
     */
    public static I_CmsRegistry getRegistry() throws CmsException {
        return c_rb.getRegistry(null, null, null);
    }


    /**
     * Starts a schedule job with a correct instantiated CmsObject.
     * 
     * @param entry the CmsCronEntry to start.
     */
    void startScheduleJob(CmsCronEntry entry) {
        // create a valid cms-object
        CmsObject cms = new CmsObject();
        try {
            initUser(cms, null, null, entry.getUserName(), entry.getGroupName(), C_PROJECT_ONLINE_ID, null);
            // create a new ScheduleJob and start it
            CmsCronScheduleJob job = new CmsCronScheduleJob(cms, entry);
            job.start();
        } catch(Exception exc) {
            if(C_LOGGING && isLogging(C_OPENCMS_CRONSCHEDULER)) {
                log(C_OPENCMS_CRONSCHEDULER, "Error initialising job for " + entry + " Error: " + Utils.getStackTrace(exc));
            }
        }
    }

    /**
     * Reads the actual entries from the database and updates the Crontable
     */
    void updateCronTable() {
        try {
            m_table.update(c_rb.readCronTable(null, null));
        } catch(Exception exc) {
            if(C_LOGGING && isLogging(C_OPENCMS_CRITICAL)) {
                log(C_OPENCMS_CRITICAL, "[OpenCms] crontable corrupt. Scheduler is now disabled!");
            }
        }
    }
}
