/*
* File   : $Source: /alkacon/cvs/opencms/src/com/opencms/core/Attic/OpenCms.java,v $
* Date   : $Date: 2002/01/11 13:36:58 $
* Version: $Revision: 1.74 $
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

import java.io.*;
import java.util.*;
import source.org.apache.java.io.*;
import source.org.apache.java.util.*;
import com.opencms.file.*;
import com.opencms.boot.*;
import com.opencms.util.*;
import com.opencms.launcher.*;
import com.opencms.template.cache.*;

// if you need one of these test if the OpenCms Shell still runs!
//import javax.servlet.*;
//import javax.servlet.http.*;


/**
 * This class is the main class of the OpenCms system.
 * <p>
 * It is used to read a requested resource from the OpenCms System and forward it to
 * a launcher, which is performs the output of the requested resource. <br>
 *
 * The OpenCms class is independent of access module to the OpenCms (e.g. Servlet,
 * Command Shell), therefore this class is <b>not</b> responsible for user authentification.
 * This is done by the access module to the OpenCms.
 *
 * @author Michael Emmerich
 * @author Alexander Lucas
 * @version $Revision: 1.74 $ $Date: 2002/01/11 13:36:58 $
 *
 * */
public class OpenCms extends A_OpenCms implements I_CmsConstants,I_CmsLogChannels {

    /**
     * Define the default file.encoding that should be used by OpenCms
     */
    private static String C_PREFERED_FILE_ENCODING = "ISO8859_1";

    /**
     * Definition of the index page
     */
    private static String C_INDEX = "index.html";

    /**
     * The default mimetype
     */
    private static String C_DEFAULT_MIMETYPE = "text/html";

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
     * Indicates, if the session-failover should be enabled or not.
     */
    private boolean m_sessionFailover = false;

    /**
     * Indicates, if the streaming should be enabled by the configurations
     */
    private boolean m_streaming = true;

    /**
     * Indicates, if the element cache should be enabled by the configurations
     */
    private boolean m_enableElementCache = true;

    /**
     * Reference to the CmsElementCache object containing locators for all
     * URIs and elements in cache.
     */
    private static CmsElementCache c_elementCache = null;

    /**
     * In this hashtable the dependencies for all variants in the elementcache
     * are stored. The keys are Strings with resourceNames like "/siteX/cos/ContentClass/news4"
     * and the value is a Vector with strings (The elementvariants that depend on the keys)
     * like "ElementClass|ElementTemplate|VariantCacheKey"
     */
    private static Hashtable c_variantDeps = null;

    /**
     * the vectors to store the three different rulesets needed for the link replacement.
     * Each vector contains a ruleset. The elements are regular expressions (Strings) the
     * way perl5 uses them.
     */
    private static String[] c_linkRulesExport = null;
    private static String[] c_linkRulesOnline = null;
    private static String[] c_linkRulesOffline = null;
    private static String[] c_linkRulesExtern = null;

    /**
     * the start rule for the extern and the export rules
     */
    private static String c_linkRuleStart = null;

    /**
     * The startpoints for the static export.
     */
    private static Vector c_staticExportStart = null;

    /**
     * The path to where the export will go
     */
    private static String m_staticExportPath = null;

    /**
     * Is the static export enabled of diabled
     */
    private static boolean c_staticExportEnabled = false;

    /**
     * contains the four url prefixe for the lnikreplacement.
     * That are the prefix for export, http, https and servername. The last
     * two are used only wenn https is needed.
     */
    private static String[] c_staticUrlPrefix = new String[4];

    /**
     * Constructor, creates a new OpenCms object.
     *
     * It gets the configurations and inits a rb via the CmsRbManager.
     *
     * @param conf The configurations from the property-file.
     */
    OpenCms(Configurations conf) throws Exception {
        CmsObject cms = null;
        // invoke the ResourceBroker via the initalizer
        try {
            if(I_CmsLogChannels.C_PREPROCESSOR_IS_LOGGING && A_OpenCms.isLogging()) {
                A_OpenCms.log(I_CmsLogChannels.C_OPENCMS_INIT, "[OpenCmsServlet] logging started");
                String jdkinfo = System.getProperty("java.vm.name") + " ";
                jdkinfo += System.getProperty("java.vm.version") + " ";
                jdkinfo += System.getProperty("java.vm.info") + " ";
                jdkinfo += System.getProperty("java.vm.vendor") + " ";
                A_OpenCms.log(I_CmsLogChannels.C_OPENCMS_INIT, "[OpenCmsServlet] JDK Info: " + jdkinfo);

                String osinfo = System.getProperty("os.name") + " ";
                osinfo += System.getProperty("os.version") + " ";
                osinfo += System.getProperty("os.arch") + " ";
                A_OpenCms.log(I_CmsLogChannels.C_OPENCMS_INIT, "[OpenCmsServlet] OS Info: " + osinfo);
            }
            if(I_CmsLogChannels.C_PREPROCESSOR_IS_LOGGING && A_OpenCms.isLogging()) {
                A_OpenCms.log(I_CmsLogChannels.C_OPENCMS_INIT, "[OpenCms] creating first cms-object");
            }
            cms = new CmsObject();
            if(I_CmsLogChannels.C_PREPROCESSOR_IS_LOGGING && A_OpenCms.isLogging()) {
                A_OpenCms.log(I_CmsLogChannels.C_OPENCMS_INIT, "[OpenCms] initializing the main resource-broker");
            }
            m_sessionFailover = conf.getBoolean("sessionfailover.enabled", false);

            // init the rb via the manager with the configuration
            // and init the cms-object with the rb.
            c_rb = CmsRbManager.init(conf);
            printCopyrightInformation(cms);

            // initalize the Hashtable with all available mimetypes
            if(I_CmsLogChannels.C_PREPROCESSOR_IS_LOGGING && A_OpenCms.isLogging()) {
                A_OpenCms.log(I_CmsLogChannels.C_OPENCMS_INIT, "[OpenCms] read mime types");
            }
            m_mt = c_rb.readMimeTypes(null, null);
            if(I_CmsLogChannels.C_PREPROCESSOR_IS_LOGGING && A_OpenCms.isLogging()) {
                A_OpenCms.log(I_CmsLogChannels.C_OPENCMS_INIT, "[OpenCms] found "
                        + m_mt.size() + " mime-type entrys");
            }

            // Check, if the HTTP streaming should be enabled
            m_streaming = conf.getBoolean("httpstreaming.enabled", true);
            if(I_CmsLogChannels.C_PREPROCESSOR_IS_LOGGING && A_OpenCms.isLogging()) {
                A_OpenCms.log(I_CmsLogChannels.C_OPENCMS_INIT, "[OpenCms] HTTP streaming " + (m_streaming?"en":"dis") + "abled. ");
            }

            // if the System property opencms.disableScheduler is set to true, don't start scheduling
            if(!new Boolean(System.getProperty("opencms.disableScheduler")).booleanValue()) {
                // now initialise the OpenCms scheduler to launch cronjobs
                m_table = new CmsCronTable(c_rb.readCronTable(null, null));
                m_scheduler = new CmsCronScheduler(this, m_table);
                if(I_CmsLogChannels.C_PREPROCESSOR_IS_LOGGING && A_OpenCms.isLogging()) {
                    A_OpenCms.log(I_CmsLogChannels.C_OPENCMS_INIT, "[OpenCmsServlet] initializing CmsCronScheduler... DONE");
                }
            } else {
                if(I_CmsLogChannels.C_PREPROCESSOR_IS_LOGGING && A_OpenCms.isLogging()) {
                    A_OpenCms.log(I_CmsLogChannels.C_OPENCMS_INIT, "[OpenCmsServlet] CmsCronScheduler is disabled!");
                }
            }

            // Check the file.encoding
            checkFileEncoding();
        }
        catch(Exception e) {
            if(I_CmsLogChannels.C_PREPROCESSOR_IS_LOGGING && A_OpenCms.isLogging()) {
                A_OpenCms.log(I_CmsLogChannels.C_OPENCMS_INIT, "[OpenCms] " + e.getMessage());
            }
            throw e;
        }

        // try to initialize the launchers.
        try {
            if(I_CmsLogChannels.C_PREPROCESSOR_IS_LOGGING && A_OpenCms.isLogging()) {
                A_OpenCms.log(I_CmsLogChannels.C_OPENCMS_INIT, "[OpenCms] initialize launchers...");
            }
            m_launcherManager = new CmsLauncherManager(this);
        }
        catch(Exception e) {
            if(I_CmsLogChannels.C_PREPROCESSOR_IS_LOGGING && A_OpenCms.isLogging()) {
                A_OpenCms.log(I_CmsLogChannels.C_OPENCMS_INIT, "[OpenCms] " + e.getMessage());
            }
        }

        // Check, if the element cache should be enabled
        m_enableElementCache = conf.getBoolean("elementcache.enabled", false);
        if(I_CmsLogChannels.C_PREPROCESSOR_IS_LOGGING && A_OpenCms.isLogging()) {
            A_OpenCms.log(I_CmsLogChannels.C_OPENCMS_INIT, "[OpenCms] element cache " + (m_enableElementCache?"en":"dis") + "abled. ");
        }
        if(m_enableElementCache) {
            try {
                c_elementCache = new CmsElementCache(conf.getInteger("elementcache.uri", 10000),
                                                    conf.getInteger("elementcache.elements", 50000),
                                                    conf.getInteger("elementcache.variants", 100));
            }catch(Exception e) {
                if(I_CmsLogChannels.C_PREPROCESSOR_IS_LOGGING && A_OpenCms.isLogging()) {
                    A_OpenCms.log(I_CmsLogChannels.C_OPENCMS_INIT, "[OpenCms] " + e.getMessage());
                }
            }
            c_variantDeps = new Hashtable();
            c_elementCache.getElementLocator().setExternDependencies(c_variantDeps);
        }
        // now for the link replacement rules there are up to three rulesets for export online and offline
        try{
            if(I_CmsLogChannels.C_PREPROCESSOR_IS_LOGGING && A_OpenCms.isLogging()) {
                A_OpenCms.log(I_CmsLogChannels.C_OPENCMS_INIT, "[OpenCms] initializing link replace rules.");
            }
            c_staticUrlPrefix[0]=Utils.replace(conf.getString(C_URL_PREFIX_EXPORT, ""), C_WEB_APP_REPLACE_KEY, CmsBase.getWebAppName());
            c_staticUrlPrefix[1]=Utils.replace(conf.getString(C_URL_PREFIX_HTTP, ""), C_WEB_APP_REPLACE_KEY, CmsBase.getWebAppName());
            c_staticUrlPrefix[2]=Utils.replace(conf.getString(C_URL_PREFIX_HTTPS, ""), C_WEB_APP_REPLACE_KEY, CmsBase.getWebAppName());
            c_staticUrlPrefix[3]=Utils.replace(conf.getString(C_URL_PREFIX_SERVERNAME, ""), C_WEB_APP_REPLACE_KEY, CmsBase.getWebAppName());
            String export = conf.getString("linkrules.export");
            if(export != null && !"".equals(export)){
                c_linkRulesExport = conf.getStringArray("ruleset."+export);
                // now replace ${WEB_APP_NAME} with the correct name of the webapplication and replace the other variables
                for(int i = 0; i < c_linkRulesExport.length; i++) {
                    c_linkRulesExport[i] = Utils.replace(c_linkRulesExport[i], C_WEB_APP_REPLACE_KEY, CmsBase.getWebAppName());
                    c_linkRulesExport[i] = Utils.replace(c_linkRulesExport[i], "${"+C_URL_PREFIX_EXPORT+"}", c_staticUrlPrefix[0]);
                    c_linkRulesExport[i] = Utils.replace(c_linkRulesExport[i], "${"+C_URL_PREFIX_HTTP+"}", c_staticUrlPrefix[1]);
                    c_linkRulesExport[i] = Utils.replace(c_linkRulesExport[i], "${"+C_URL_PREFIX_HTTPS+"}", c_staticUrlPrefix[2]);
                    c_linkRulesExport[i] = Utils.replace(c_linkRulesExport[i], "${"+C_URL_PREFIX_SERVERNAME+"}", c_staticUrlPrefix[3]);
                }
            }
            String online = conf.getString("linkrules.online");
            if(online != null && !"".equals(online)){
                c_linkRulesOnline = conf.getStringArray("ruleset."+online);
                // now replace ${WEB_APP_NAME} with the correct name of the webapplication and replace the other variables
                for(int i = 0; i < c_linkRulesOnline.length; i++) {
                    c_linkRulesOnline[i] = Utils.replace(c_linkRulesOnline[i], C_WEB_APP_REPLACE_KEY, CmsBase.getWebAppName());
                    c_linkRulesOnline[i] = Utils.replace(c_linkRulesOnline[i], "${"+C_URL_PREFIX_EXPORT+"}", c_staticUrlPrefix[0]);
                    c_linkRulesOnline[i] = Utils.replace(c_linkRulesOnline[i], "${"+C_URL_PREFIX_HTTP+"}", c_staticUrlPrefix[1]);
                    c_linkRulesOnline[i] = Utils.replace(c_linkRulesOnline[i], "${"+C_URL_PREFIX_HTTPS+"}", c_staticUrlPrefix[2]);
                    c_linkRulesOnline[i] = Utils.replace(c_linkRulesOnline[i], "${"+C_URL_PREFIX_SERVERNAME+"}", c_staticUrlPrefix[3]);
                }
            }
            String offline = conf.getString("linkrules.offline");
            if(offline != null && !"".equals(offline)){
                c_linkRulesOffline = conf.getStringArray("ruleset."+offline);
                // now replace ${WEB_APP_NAME} with the correct name of the webapplication and replace the other variables
                for(int i = 0; i < c_linkRulesOffline.length; i++) {
                    c_linkRulesOffline[i] = Utils.replace(c_linkRulesOffline[i], C_WEB_APP_REPLACE_KEY, CmsBase.getWebAppName());
                    c_linkRulesOffline[i] = Utils.replace(c_linkRulesOffline[i], "${"+C_URL_PREFIX_EXPORT+"}", c_staticUrlPrefix[0]);
                    c_linkRulesOffline[i] = Utils.replace(c_linkRulesOffline[i], "${"+C_URL_PREFIX_HTTP+"}", c_staticUrlPrefix[1]);
                    c_linkRulesOffline[i] = Utils.replace(c_linkRulesOffline[i], "${"+C_URL_PREFIX_HTTPS+"}", c_staticUrlPrefix[2]);
                    c_linkRulesOffline[i] = Utils.replace(c_linkRulesOffline[i], "${"+C_URL_PREFIX_SERVERNAME+"}", c_staticUrlPrefix[3]);
                }
            }
            String extern = conf.getString("linkrules.extern");
            if(extern != null && !"".equals(extern)){
                c_linkRulesExtern = conf.getStringArray("ruleset."+extern);
                // now replace ${WEB_APP_NAME} with the correct name of the webapplication and replace the other variables
                for(int i = 0; i < c_linkRulesExtern.length; i++) {
                    c_linkRulesExtern[i] = Utils.replace(c_linkRulesExtern[i], C_WEB_APP_REPLACE_KEY, CmsBase.getWebAppName());
                    c_linkRulesExtern[i] = Utils.replace(c_linkRulesExtern[i], "${"+C_URL_PREFIX_EXPORT+"}", c_staticUrlPrefix[0]);
                    c_linkRulesExtern[i] = Utils.replace(c_linkRulesExtern[i], "${"+C_URL_PREFIX_HTTP+"}", c_staticUrlPrefix[1]);
                    c_linkRulesExtern[i] = Utils.replace(c_linkRulesExtern[i], "${"+C_URL_PREFIX_HTTPS+"}", c_staticUrlPrefix[2]);
                    c_linkRulesExtern[i] = Utils.replace(c_linkRulesExtern[i], "${"+C_URL_PREFIX_SERVERNAME+"}", c_staticUrlPrefix[3]);
                }
            }
            c_linkRuleStart = conf.getString("exportfirstrule");

            // now the startpoints for the static export
            String[] buffer = conf.getStringArray(C_STATICEXPORT_START);
            if(buffer != null){
                c_staticExportStart = new Vector();
                for(int i=0; i<buffer.length; i++){
                    c_staticExportStart.add(buffer[i]);
                }
            }
            // at last the target for the export
            m_staticExportPath = com.opencms.boot.CmsBase.getAbsoluteWebPath(CmsBase.getAbsoluteWebPath(conf.getString(C_STATICEXPORT_PATH)));

            // is the static export enabled?
            c_staticExportEnabled = conf.getBoolean("staticexport.enabled", false);
            if(c_staticExportEnabled){
                // we have to generate the dynamic rulessets
                createDynamicLinkRules();
            }else{
                // no static export. We need online and offline rules to stay in OpenCms.
                c_linkRulesOffline = new String[]{"s#^#/"+ CmsBase.getWebAppName() +"/opencms#"};
                c_linkRulesOnline = c_linkRulesOffline;
            }
        }catch(Exception e){
            if(I_CmsLogChannels.C_PREPROCESSOR_IS_LOGGING && A_OpenCms.isLogging()) {
                A_OpenCms.log(I_CmsLogChannels.C_OPENCMS_INIT, "[OpenCms] " + e.toString());
            }
        }
    }

    /**
     * Destructor, called when the the servlet is shut down.
     * @exception CmsException Throws CmsException if something goes wrong.
     */
    public void destroy() throws CmsException {
        CmsObject cms = new CmsObject();
        cms.init(c_rb);
        cms.destroy();
        checkFileEncoding();
    }

    /**
     * Insert the method's description here.
     * Creation date: (10/25/00 12:42:11)
     * @return com.opencms.launcher.CmsLauncherManager
     */
    public CmsLauncherManager getLauncherManager() {
        return m_launcherManager;
    }

    /**
     * Gets the ElementCache used for the online project.
     * @return CmsElementCache
     */
    public static CmsElementCache getOnlineElementCache(){
        return c_elementCache;
    }

    /**
     * Returns the ruleset for link replacement.
     * @param state. defines which set is needed.
     * @return String[] the ruleset.
     */
    public static String[] getLinkRules(int state){

        if(state == C_MODUS_ONLINE){
            return c_linkRulesOnline;
        }else if(state == C_MODUS_OFFLINE){
            return c_linkRulesOffline;
        }else if(state == C_MODUS_EXPORT){
            return c_linkRulesExport;
        }else if(state == C_MODUS_EXTERN){
            return c_linkRulesExtern;
        }
        return null;
    }
    /**
     * return the start rule used for export and extern mode.
     */
    public static String getLinkRuleStart(){
        return c_linkRuleStart;
    }

    /**
     * Returns a Vector (of Strings) with the names of the vfs resources (files
     * and folders) where the export should start.
     *
     * @return Vector with resources for the export.
     */
    public static Vector getStaticExportStartPoints(){
        return c_staticExportStart;
    }

    /**
     * Returns the exportpath for the static export.
     */
    public static String getStaticExportPath(){
        return m_staticExportPath;
    }

    /**
     * Gets the hashtable with the variant dependencies used for the elementcache.
     * @return Hashtable
     */
    public static Hashtable getVariantDependencies(){
        return c_variantDeps;
    }

    /**
     * Gets the prefix array for the linkreplacement
     * @return String[4]
     */
    public static String[] getUrlPrefixArray(){
        return c_staticUrlPrefix;
    }

    /**
     * Returns true if the static export is enabled
     */
    public static boolean isStaticExportEnabled(){
        return c_staticExportEnabled;
    }
    /**
     * This method gets the requested document from the OpenCms and returns it to the
     * calling module.
     *
     * @param cms The CmsObject containing all information about the requested document
     * and the requesting user.
     * @return CmsFile object.
     */
    CmsFile initResource(CmsObject cms) throws CmsException,IOException {
        CmsFile file = null;

        //check if the requested resource is a folder
        // if this is the case, redirect to the according index.html
        String resourceName = cms.getRequestContext().getUri();
        if(resourceName.endsWith("/")) {
            resourceName += C_INDEX;
            cms.getRequestContext().getResponse().sendCmsRedirect(resourceName);
            return null;
        }
        try {

            //read the requested file
            file = cms.readFile(cms.getRequestContext().getUri());
        }
        catch(CmsException e) {
            if(e.getType() == CmsException.C_NOT_FOUND) {

                // there was no file found with this name.
                // it is possible that the requested resource was a folder, so try to access an
                // index.html there
                resourceName = cms.getRequestContext().getUri();

                // test if the requested file is already the index.html
                if(!resourceName.endsWith(C_INDEX)) {

                    // check if the requested file ends with an "/"
                    if(!resourceName.endsWith("/")) {
                        resourceName += "/";
                    }

                    //redirect the request to the index.html
                    resourceName += C_INDEX;
                    cms.getRequestContext().getResponse().sendCmsRedirect(resourceName);
                }
                else {

                    // throw the CmsException.
                    throw e;
                }
            }
            else {

                // throw the CmsException.
                throw e;
            }
        }
        if(file != null) {

            // test if this file is only available for internal access operations
            if((file.getAccessFlags() & C_ACCESS_INTERNAL_READ) > 0) {
                throw new CmsException(CmsException.C_EXTXT[CmsException.C_INTERNAL_FILE]
                        + cms.getRequestContext().getUri(), CmsException.C_INTERNAL_FILE);
            }
        }
        return file;
    }

    /**
     * Inits a new user and sets it into the overgiven cms-object.
     *
     * @param cms the cms-object to use.
     * @param cmsReq the cms-request for this http-request.
     * @param cmsRes the cms-response for this http-request.
     * @param user The name of the user to init.
     * @param group The name of the current group.
     * @param project The id of the current project.
     */
    public void initUser(CmsObject cms, I_CmsRequest cmsReq, I_CmsResponse cmsRes, String user,
            String group, int project, CmsCoreSession sessionStorage) throws CmsException {
        if((!m_enableElementCache) || (project == C_PROJECT_ONLINE_ID)){
            cms.init(c_rb, cmsReq, cmsRes, user, group, project, m_streaming, c_elementCache, sessionStorage);
        }else{
            cms.init(c_rb, cmsReq, cmsRes, user, group, project, m_streaming, new CmsElementCache(10,200,10), sessionStorage);
        }
    }

    /**
     * Prints a copyright information to all log-files.
     */
    private void printCopyrightInformation(CmsObject cms) {
        String copy[] = cms.copyright();

        // log to error-stream
        System.err.println(cms.version());
        for(int i = 0;i < copy.length;i++) {
            System.err.println(copy[i]);
        }

        // log with opencms-logger
        if(I_CmsLogChannels.C_PREPROCESSOR_IS_LOGGING && isLogging()) {
            this.log(C_OPENCMS_INFO, cms.version());
            for(int i = 0;i < copy.length;i++) {
                this.log(C_OPENCMS_INFO, copy[i]);
            }
        }
    }

    /**
     * This method loads old sessiondata from the database. It is used
     * for sessionfailover.
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
     * Sets the mimetype of the response.<br>
     * The mimetype is selected by the file extension of the requested document.
     * If no available mimetype is found, it is set to the default
     * "application/octet-stream".
     *
     * @param cms The actual OpenCms object.
     * @param file The requested document.
     *
     */
    void setResponse(CmsObject cms, CmsFile file) {
        String ext = null;
        String mimetype = null;
        int lastDot = file.getName().lastIndexOf(".");

        // check if there was a file extension
        if((lastDot > 0) && (!file.getName().endsWith("."))) {
            ext = file.getName().substring(lastDot + 1, file.getName().length());
            mimetype = (String)m_mt.get(ext);

            // was there a mimetype fo this extension?
            if(mimetype != null) {
                cms.getRequestContext().getResponse().setContentType(mimetype);
            }
            else {
                cms.getRequestContext().getResponse().setContentType(C_DEFAULT_MIMETYPE);
            }
        }
        else {
            cms.getRequestContext().getResponse().setContentType(C_DEFAULT_MIMETYPE);
        }
    }

    /**
     * Selects the appropriate launcher for a given file by analyzing the
     * file's launcher id and calls the initlaunch() method to initiate the
     * generating of the output.
     *
     * @param cms CmsObject containing all document and user information
     * @param file CmsFile object representing the selected file.
     * @exception CmsException
     */
    public void showResource(CmsObject cms, CmsFile file) throws CmsException {
        int launcherId = file.getLauncherType();
        String startTemplateClass = file.getLauncherClassname();
        I_CmsLauncher launcher = m_launcherManager.getLauncher(launcherId);
        if(launcher == null) {
            String errorMessage = "Could not launch file " + file.getName() + ". Launcher for requested launcher ID "
                    + launcherId + " could not be found.";
            if(I_CmsLogChannels.C_PREPROCESSOR_IS_LOGGING && A_OpenCms.isLogging()) {
                A_OpenCms.log(C_OPENCMS_INFO, "[OpenCms] " + errorMessage);
            }
            throw new CmsException(errorMessage, CmsException.C_UNKNOWN_EXCEPTION);
        }
        cms.setLauncherManager(m_launcherManager);
        launcher.initlaunch(cms, file, startTemplateClass, this);
    }

    /**
     * This method stores sessiondata into the database. It is used
     * for sessionfailover.
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
     * Returns the registry to read values from it. You don't have the right to write
     * values. This is useful for modules, to read module-parameters.
     *
     * @return the registry to READ values from it.
     * @exception Throws CmsException, if the registry can not be returned.
     */
    public static I_CmsRegistry getRegistry() throws CmsException {
        return c_rb.getRegistry(null, null, null);
    }

    /**
     * Method that checks the system-property file.encoding.
     * If it is not the prefered encoding opencms logs a warning.
     */
    private void checkFileEncoding() {
       if(I_CmsLogChannels.C_PREPROCESSOR_IS_LOGGING && A_OpenCms.isLogging()) {
            if(!System.getProperty("file.encoding").equals(C_PREFERED_FILE_ENCODING)) {
                A_OpenCms.log(I_CmsLogChannels.C_OPENCMS_INIT, "[OpenCms] You are not using the prefered file.encoding. It should be " + C_PREFERED_FILE_ENCODING + " but it is " + System.getProperty("file.encoding"));
                A_OpenCms.log(I_CmsLogChannels.C_OPENCMS_INIT, "[OpenCms] You may get in trouble with user passwords if you change the encoding later on");
            }
        }
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
            new CmsStaticExport(cms, null, false);
        }catch(Exception e){
            if(I_CmsLogChannels.C_PREPROCESSOR_IS_LOGGING && CmsBase.isLogging()) {
                CmsBase.log(C_OPENCMS_INIT, "Error initialising dynamic link rules. Error: " + Utils.getStackTrace(e));
            }
        }
    }

    /**
     * Starts a schedule job with a correct instantiated CmsObject.
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
            if(I_CmsLogChannels.C_PREPROCESSOR_IS_LOGGING && CmsBase.isLogging()) {
                CmsBase.log(C_OPENCMS_CRONSCHEDULER, "Error initialising job for " + entry + " Error: " + Utils.getStackTrace(exc));
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
            if(I_CmsLogChannels.C_PREPROCESSOR_IS_LOGGING && A_OpenCms.isLogging()) {
                A_OpenCms.log(I_CmsLogChannels.C_OPENCMS_CRITICAL, "[OpenCms] crontable corrupt. Scheduler is now disabled!");
            }
        }
    }
}
