/*
* File   : $Source: /alkacon/cvs/opencms/src/com/opencms/flex/Attic/CmsJspLoader.java,v $
* Date   : $Date: 2002/11/17 13:58:41 $
* Version: $Revision: 1.10 $
*
* This library is part of OpenCms -
* the Open Source Content Mananagement System
*
* Copyright (C) 2002  The OpenCms Group
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


package com.opencms.flex;

import java.util.*;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.*;

import com.opencms.boot.I_CmsLogChannels;
import com.opencms.file.*;
import com.opencms.core.*;
import com.opencms.launcher.*;
import com.opencms.util.Encoder;
import com.opencms.util.Utils;
import com.opencms.flex.cache.*;
import com.opencms.flex.jsp.*;

/**
 * The JSP loader which enables the execution of JSP in OpenCms.<p>
 *
 * It does NOT extend {@link com.opencms.launcher.A_CmsLauncher}, since JSP are not related
 * to the OpenCms Template mechanism. However, it implements the
 * launcher interface so that JSP can be sub-elements in XMLTemplace pages.
 *
 * @author  Alexander Kandzior (a.kandzior@alkacon.com)
 *
 * @version $Revision: 1.10 $
 * @since FLEX alpha 1
 * 
 * @see I_CmsResourceLoader
 * @see com.opencms.launcher.I_CmsLauncher
 */
public class CmsJspLoader implements I_CmsLauncher, I_CmsResourceLoader {

    /** The directory to store the generated JSP pages in (absolute path) */
    private static String m_jspRepository = null;
    
    /** The directory to store the generated JSP pages in (relative path in web application */
    private static String m_jspWebAppRepository = null;
    
    /** The CmsFlexCache used to store generated cache entries in */
    private static CmsFlexCache m_cache;
    
    /** Export URL for JSP pages */
    private String m_exportUrl;

    /** Special JSP directive tag start (<code>&lt;%@</code>)*/
    public static final String C_DIRECTIVE_START = "<%@";

    /** Special JSP directive tag start (<code>%&gt;</code>)*/
    public static final String C_DIRECTIVE_END ="%>";
    
    /** Encoding to write JSP files to disk (<code>ISO-8859-1</code>) */
    public static final String C_DEFAULT_JSP_ENCODING = "ISO-8859-1";
    
    /** Extension for JSP managed by OpenCms (<code>.jsp</code>) */
    public static final String C_JSP_EXTENSION = ".jsp";      
    
    /** Flag for debugging output. Set to 9 for maximum verbosity. */ 
    private static final int DEBUG = 0;
        
    /**
     * The constructor of the class is empty, the initial instance will be 
     * created by the launcher manager upon startup of OpenCms.<p>
     * 
     * To initilize the fields in this class, the <code>setOpenCms()</code>
     * method will be called by the launcher.
     * 
     * @see com.opencms.launcher.CmsLauncherManager
     * @see #setOpenCms(A_OpenCms openCms)
     */
    public CmsJspLoader() {
        // NOOP
    }
    
    // ---------------------------- Implementation of interface com.opencms.launcher.I_CmsLauncher          
    
    /**
     * This is part of the I_CmsLauncher interface, but for JSP so far this 
     * is a NOOP.
     */
    public void clearCache() {
        // NOOP
    }
    
    /**
     * This is part of the I_CmsLauncher interface, 
     * used here to call the init() method.
     * 
     * @see #init(A_OpenCms openCms)
     */
    public void setOpenCms(A_OpenCms openCms) {
        init(openCms);
    }    
    
    /** 
     * Returns the ID that indicates the type of the launcher.
     * 
     * The IDs for all launchers of the core distributions are constants 
     * in the I_CmsLauncher interface.
     * The value returned is <code>com.opencms.launcher.I_CmsLauncher.C_TYPE_JSP</code>.
     *
     * @return launcher ID
     * 
     * @see com.opencms.launcher.I_CmsLauncher
     */
    public int getLauncherId() {
        return com.opencms.launcher.I_CmsLauncher.C_TYPE_JSP;
    }
    
    /** 
     * Start launch method called by the OpenCms system to show a resource,
     * this basically processes the resource and returns the output.<p>
     * 
     * This is part of the Launcher interface.
     * All requests will be forwarded to the <code>load()</code> method of this 
     * class. That forms the link between the Launcher and Loader interfaces.<p>
     * 
     * Exceptions thrown in the <code>load()</code> method of this loader
     * will be handled here, usually by wrapping them in a CmsException
     * that will then be shown in the OpenCms error dialog.
     *
     * @param cms CmsObject Object for accessing system resources.
     * @param file CmsFile Object with the selected resource to be shown.
     * @param startTemplateClass Name of the template class to start with.
     * @param openCms a instance of A_OpenCms for redirect-needs
     * @throws CmsException all exeptions in the load process of a JSP will be caught here and wrapped to a CmsException
     * 
     * @see com.opencms.launcher.I_CmsLauncher
     * @see #load(CmsObject cms, CmsFile file, HttpServletRequest req, HttpServletResponse res) 
     */
    public void initlaunch(CmsObject cms, CmsFile file, String startTemplateClass, A_OpenCms openCms) throws CmsException {
        HttpServletRequest req;
        HttpServletResponse res;

        CmsRequestContext context = cms.getRequestContext();
        if (context.getRequest() instanceof com.opencms.core.CmsExportRequest) {
            if (DEBUG > 1) System.err.println("FlexJspLoader: Export requested for " + file.getAbsolutePath());
                com.opencms.launcher.I_CmsLauncher dumpLauncher = cms.getLauncherManager().getLauncher(com.opencms.launcher.I_CmsLauncher.C_TYPE_DUMP);
                dumpLauncher.initlaunch(cms, file, startTemplateClass, openCms);
            /*            
            // This works :-)
            try {
                // TODO: static export of JSP pages
                // 1:    Build the exportUrl automatically from the server settings
                // [ok]  Maybe have some setting in opencms.properties
                //       Default settings could be used in servlet to calculate server / webapp / context
                // 2:    Check possibility to not make http request in case element is
                // [ok]  already cached in FlexCache
                //       However, FlexCache shoule be empty anyway since publish 
                //       had occured, so element will never be found in cache.
                // 3:    Add parameters to the exportUrl (don't forget to encode)
                // [ok]

                // 4:    Must make a check / setting so that the http request is
                //       recognized as export request, prop. "_flex=export" 
                //       Set "mode" of CmsObject to "export" using cms.setMode(int)
                //       Put the collected links from the vector 
                //       cms.getRequestContext().getLinkVector() back through the request,
                //       probably as a header in the http response (?)
                
                // 5:    Add handling of included JSP sub-elements 
                //       Maybe have some new parameter _flex=export
                //       It is important to ensure the URI/pathInfo is in sync for exported/not exported elements
                //       In export with request like below, URI will be URI of sub-element
                //       In normal request URI would be of top-level page

                
                String exportUrl = m_exportUrl + file.getAbsolutePath();

                // Add parameters to export call
                Enumeration params = context.getRequest().getParameterNames();
                if (params.hasMoreElements()) exportUrl+="?";
                while (params.hasMoreElements()) {
                    String key = (String)params.nextElement();
                    String values[] = (String[])context.getRequest().getParameterValues(key);
                    for (int i=0; i<values.length; i++) {
                        exportUrl += key + "=";
                        exportUrl += Encoder.encode(values[i], "UTF-8", true);
                        if ((i+1)<values.length) exportUrl+="&";
                    }
                    if (params.hasMoreElements()) exportUrl+="&";                       
                }
                if (DEBUG > 2) System.err.println("[CmsJspLoader] JSP export URL: " + exportUrl);
                
                URL export = new URL(exportUrl); 
                HttpURLConnection urlcon = (HttpURLConnection) export.openConnection(); 
                // Set request type to GET
                urlcon.setRequestMethod("GET");
                urlcon.setFollowRedirects(false);
                // Input and output stream            
                DataInputStream input = new DataInputStream(urlcon.getInputStream());            
                OutputStream output = context.getResponse().getOutputStream();
                int b;
                while ((b = input.read()) > 0) {
                    output.write(b);
                }
            } catch (Exception e) {
                System.err.println("" + e + "\n" + Utils.getStackTrace(e));
            }                                     
            */
            return;               
        } else {
            req = (HttpServletRequest)context.getRequest().getOriginalRequest();
            res = (HttpServletResponse)context.getResponse().getOriginalResponse();
        }
        
        try {
            // Load the resource
            load(cms, file, req, res);
        } catch (Exception e) {
            // All Exceptions are caught here and get translated to a CmsException for display in the OpenCms error dialog
            if (DEBUG > 1) System.err.println("Error in Flex loader: " + e + Utils.getStackTrace(e));
            throw new CmsException("Error in Flex loader", CmsException.C_FLEX_LOADER, e, true);
        }            
    } 

    // ---------------------------- Implementation of interface com.opencms.flex.I_CmsResourceLoader    
    
    /** Destroy this ResourceLoder, this is a NOOP so far.  */
    public void destroy() {
        // NOOP
    }
    
    /**
     * Return a String describing the ResourceLoader,
     * which is <code>"The OpenCms default resource loader for JSP"</code>
     * 
     * @return a describing String for the ResourceLoader 
     */
    public String getResourceLoaderInfo() {
        return "The OpenCms default resource loader for JSP";
    }
    
    /** 
     * Initialize the ResourceLoader,
     * here the configuration for the JSP repository (directories used) is set.
     *
     * @param openCms An OpenCms object to use for initalizing.
     */
    public void init(A_OpenCms openCms) {
        m_jspRepository = com.opencms.boot.CmsBase.getBasePath();
        if (m_jspRepository.indexOf("WEB-INF") >= 0) {
            // Should always be true, just make sure we don't generate an exception in untested environments
            m_jspRepository = m_jspRepository.substring(0, m_jspRepository.indexOf("WEB-INF")-1);
        }
        source.org.apache.java.util.Configurations c = openCms.getConfiguration();
        m_jspWebAppRepository = c.getString("flex.jsp.repository", "/WEB-INF/jsp");
        m_jspRepository += m_jspWebAppRepository.replace('/', File.separatorChar);
        if (! m_jspRepository.endsWith(File.separator)) m_jspRepository += File.separator;
        if (DEBUG > 0) System.err.println("JspLoader: Setting jsp repository to " + m_jspRepository);
        // Get the cache from the runtime properties
        m_cache = (CmsFlexCache)openCms.getRuntimeProperty(this.C_LOADER_CACHENAME);
        // Get the export URL from the runtime properties
        m_exportUrl = (String)openCms.getRuntimeProperty("flex.jsp.exporturl");
        if (I_CmsLogChannels.C_LOGGING && A_OpenCms.isLogging(I_CmsLogChannels.C_FLEX_LOADER)) {
            A_OpenCms.log(I_CmsLogChannels.C_FLEX_LOADER, "Initialized!");        
            A_OpenCms.log(I_CmsLogChannels.C_FLEX_LOADER, "JSP repository (absolute path): " + m_jspRepository);        
            A_OpenCms.log(I_CmsLogChannels.C_FLEX_LOADER, "JSP repository (web application path): " + m_jspWebAppRepository);              
        }
    }
    
    /**
     * Basic top-page processing method for this I_CmsResourceLoader,
     * this method is called by <code>initlaunch()</code> if a JSP is requested if
     * the original request was from the launcher manager.
     *
     * @param cms The initialized CmsObject which provides user permissions
     * @param file The requested OpenCms VFS resource
     * @param req The original servlet request
     * @param res The original servlet response
     * 
     * @throws ServletException might be thrown in the process of including the JSP 
     * @throws IOException might be thrown in the process of including the JSP 
     * 
     * @see I_CmsResourceLoader
     * @see #initlaunch(CmsObject cms, CmsFile file, String startTemplateClass, A_OpenCms openCms)
     */
    public void load(CmsObject cms, CmsFile file, HttpServletRequest req, HttpServletResponse res) 
    throws ServletException, IOException {       

        long timer1 = 0;
        if (DEBUG > 0) {
            timer1 = System.currentTimeMillis();        
            System.err.println("========== JspLoader loading: " + file.getAbsolutePath());
        }

        boolean streaming = false;            
        boolean bypass = false;
        
        try {
            // Read caching property from requested VFS resource                                     
            String stream = cms.readProperty(file.getAbsolutePath(), I_CmsResourceLoader.C_LOADER_STREAMPROPERTY);                    
            if (stream != null) {
                if ("yes".equalsIgnoreCase(stream) || "true".equalsIgnoreCase(stream)) {
                    streaming = true;                
                } else if ("bypass".equalsIgnoreCase(stream) || "bypasscache".equalsIgnoreCase(stream)) {
                    bypass = true;
                }
            }
        } catch (CmsException e) {
            throw new ServletException("FlexJspLoader: Error while loading stream properties for " + file.getAbsolutePath() + "\n" + e, e);
        } 
        
        if (DEBUG > 1) System.err.println("========== JspLoader stream=" + streaming + " bypass=" + bypass);
        
        CmsFlexRequest w_req; 
        CmsFlexResponse w_res;
        if (req instanceof CmsFlexRequest) {
            w_req = (CmsFlexRequest)req; 
        } else {
            w_req = new CmsFlexRequest(req, file, m_cache, cms); 
        }        
        if (res instanceof CmsFlexResponse) {
            w_res = (CmsFlexResponse)res;              
        } else {
            w_res = new CmsFlexResponse(res, streaming);
        }
        
        if (bypass) {
            // Bypass Flex cache for this page (this solves some compatibility issues in BEA Weblogic)        
            if (DEBUG > 1) System.err.println("JspLoader.load() bypassing cache for file " + file.getAbsolutePath());
            // Update the JSP first if neccessary            
            String target = updateJsp(cms, file, w_req, new HashSet(11));
            // Dispatch to external JSP
            req.getRequestDispatcher(target).forward(w_req, res);              
            if (DEBUG > 1) System.err.println("JspLoader.load() cache was bypassed!");
        } else {
            // Flex cache not bypassed            
            try {
                w_req.getCmsRequestDispatcher(file.getAbsolutePath()).include(w_req, w_res);
            } catch (java.net.SocketException e) {        
                // Uncritical, might happen if client (browser) does not wait until end of page delivery
                if (DEBUG > 1) System.err.println("JspLoader.load() ignoring SocketException " + e);
            }            
            if (! streaming && ! w_res.isSuspended()) {
                try {      
                    if (! res.isCommitted()) {
                        // If a JSP errorpage was triggered the response will be already committed here
                        byte[] result = w_res.getWriterBytes();
                        
                        // Encoding project:  
                        // The byte array will internally be encoded in the OpenCms 
                        // default encoding. In case another encoding is set 
                        // in the 'content-encoding' property of the file, 
                        // we need to re-encode the output here. 
                        String dnc = A_OpenCms.getDefaultEncoding().trim().toLowerCase();  
                        String enc = cms.getRequestContext().getEncoding().trim().toLowerCase();
                        if (! dnc.equals(enc)) {
                            if (DEBUG > 1) System.err.println("CmsJspLoader.load(): Encoding result from " + dnc + " to " + enc);
                            result = (new String(result, dnc)).getBytes(enc);                            
                        }
                                                        
                        res.setContentLength(result.length);
                        w_res.processHeaders(w_res.getHeaders(), res);
                        res.getOutputStream().write(result);
                        res.getOutputStream().flush();
                    }
                } catch (IllegalStateException e) {
                    // Uncritical, might happen if JSP error page was used
                    if (DEBUG > 1) System.err.println("JspLoader.load() ignoring IllegalStateException " + e);
                } catch (java.net.SocketException e) {        
                    // Uncritical, might happen if client (browser) does not wait until end of page delivery
                    if (DEBUG > 1) System.err.println("JspLoader.load() ignoring SocketException " + e);
                }       
            }
        }
        
        if (DEBUG > 0) {
            long timer2 = System.currentTimeMillis() - timer1;
            System.err.println("========== JspLoader time delivering JSP for " + file.getAbsolutePath() + ": " + timer2 + "ms");
        }        
    }
    
    /**
     * Method to enable JSPs to be used as sub-elements in XMLTemplates.
     *
     * @param cms The initialized CmsObject which provides user permissions
     * @param file The requested OpenCms VFS resource
     * 
     * @throws CmsException In case the Loader can not process the requested resource
     * 
     * @see CmsJspTemplate
     */
    public byte[] loadTemplate(CmsObject cms, CmsFile file) 
    throws CmsException {

        byte[] result = null;
        
        long timer1 = 0;
        if (DEBUG > 0) {
            timer1 = System.currentTimeMillis();        
            System.err.println("========== JspLoader (Template) loading: " + file.getAbsolutePath());
        }       

        HttpServletRequest req = (HttpServletRequest)cms.getRequestContext().getRequest().getOriginalRequest();
        HttpServletResponse res = (HttpServletResponse)cms.getRequestContext().getResponse().getOriginalResponse();             
        
        CmsFlexRequest w_req; 
        CmsFlexResponse w_res;
        if (req instanceof CmsFlexRequest) {
            w_req = (CmsFlexRequest)req; 
        } else {
            w_req = new CmsFlexRequest(req, file, m_cache, cms); 
        }        
        if (res instanceof CmsFlexResponse) {
            w_res = (CmsFlexResponse)res;              
        } else {
            w_res = new CmsFlexResponse(res, false);
        }
        
        try {
            w_req.getCmsRequestDispatcher(file.getAbsolutePath()).include(w_req, w_res);
        } catch (java.net.SocketException e) {        
            // Uncritical, might happen if client (browser) does not wait until end of page delivery
            if (DEBUG > 1) System.err.println("JspLoader.loadTemplate() ignoring SocketException " + e);
        } catch (Exception e) {            
            System.err.println("Error in CmsJspLoader.loadTemplate() while loading: " + e.toString());
            if (DEBUG > 0) System.err.println(com.opencms.util.Utils.getStackTrace(e));
            throw new CmsException("Error in CmsJspLoader.loadTemplate() while loading " + file.getAbsolutePath() + "\n" + e, CmsException.C_LAUNCH_ERROR, e);
        } 

        if (! w_res.isSuspended()) {
            try {      
                if ((res == null) || (! res.isCommitted())) {
                    // If a JSP errorpage was triggered the response will be already committed here
                    result = w_res.getWriterBytes();
                }
            } catch (IllegalStateException e) {
                // Uncritical, might happen if JSP error page was used
                if (DEBUG > 1) System.err.println("JspLoader.loadTemplate() ignoring IllegalStateException " + e);
            } catch (Exception e) {
                System.err.println("Error in CmsJspLoader.loadTemplate() while writing buffer to final stream: " + e.toString());
                if (DEBUG > 0) System.err.println(com.opencms.util.Utils.getStackTrace(e));
                throw new CmsException("Error in CmsJspLoader.loadTemplate() while writing buffer to final stream for " + file.getAbsolutePath() + "\n" + e, CmsException.C_LAUNCH_ERROR, e);
            }        
        }
        
        if (DEBUG > 0) {
            long timer2 = System.currentTimeMillis() - timer1;
            System.err.println("========== JspLoader (Template) time delivering JSP for " + file.getAbsolutePath() + ": " + timer2 + "ms");
        }        
        
        return result;
    }
    
    /**
     * Translates the JSP file name for a OpenCms VFS resourcn 
     * to the name used in the "real" file system.<p>
     * 
     * The name given must be a absolute URI in the OpenCms VFS,
     * e.g. CmsFile.getAbsolutePath()
     *
     * @param name The file to calculate the JSP name for
     * @return The JSP name for the file
     */    
    public static String getJspName(String name) {
        return name.replace('\\', 'T').replace('/', 'T') + '.' + name.hashCode() + C_JSP_EXTENSION;
    }
    
    /**
     * Returns the uri for a given JSP in the "real" file system, 
     * i.e. the path in the file
     * system relative to the web application directory.
     *
     * @param name The name of the JSP file 
     * @param online Flag to check if this is request is online or not
     * @return The full uri to the JSP
     */
    public static String getJspUri(String name, boolean online) {
        return m_jspWebAppRepository + (online?"/online/":"/offline/") + getJspName(name);  
    }
    
    /**
     * Returns the absolute path in the "real" file system for a given JSP.
     *
     * @param name The name of the JSP file 
     * @param online Flag to check if this is request is online or not
     * @return The full path to the JSP
     */
    public static String getJspPath(String name, boolean online) {
        return m_jspRepository + (online?"online":"offline") + File.separator + name;
    }

    /**
     * Returns the absolute path in the "real" file system for the JSP repository
     * toplevel directory.
     *
     * @return The full path to the JSP repository
     */
    public static String getJspRepository() {        
        return m_jspRepository;
    }    
    
    /**
     * Updates a JSP page in the "real" file system in case the VFS resource has changed.<p>
     * 
     * Also processes the <code>&lt;%@ cms %&gt;</code> tags before the JSP is written to the real FS.
     * Also recursivly updates all files that are referenced by a <code>&lt;%@ cms %&gt;</code> tag 
     * on this page to make sure the file actually exists in the real FS. 
     * All <code>&lt;%@ include %&gt;</code> tags are parsed and the name in the tag is translated
     * from the OpenCms VFS path to the path in the real FS. 
     * The same is done for filenames in <code>&lt;%@ page errorPage=... %&gt;</code> tags.
     * 
     * @param cms Used to access the OpenCms VFS
     * @param file The reqested JSP file resource in the VFS
     * @param req The current request
     * @param res The current response
     * @param updates A Set containing all JSP pages that have been already updated
     * 
     * @return The file name of the updated JSP in the "real" FS
     * 
     * @throws ServletException might be thrown in the process of including the JSP 
     * @throws IOException might be thrown in the process of including the JSP 
     */
    private synchronized String updateJsp(CmsObject cms, CmsResource file, CmsFlexRequest req, Set updates) 
    throws IOException, ServletException {
        
        String jspTargetName = getJspName(file.getAbsolutePath());
        String jspPath = getJspPath(jspTargetName, req.isOnline());
        
        File d = new File(jspPath).getParentFile();
        if (! (d != null) && (d.exists() && d.isDirectory() && d.canRead())) {
            if (I_CmsLogChannels.C_LOGGING && A_OpenCms.isLogging(I_CmsLogChannels.C_OPENCMS_CRITICAL)) 
                A_OpenCms.log(I_CmsLogChannels.C_OPENCMS_CRITICAL, "Could not access directory for " + jspPath);
            throw new ServletException("JspLoader: Could not access directory for " + jspPath);
        }    
        
        if (updates.contains(jspTargetName)) return null;
        updates.add(jspTargetName);
        
        boolean mustUpdate = false;
        
        File f = new File(jspPath);        
        if (!f.exists()) {
            // File does not exist in FS
            mustUpdate = true;
        } else if (f.lastModified() <= file.getDateLastModified()) {
            // File in FS is older then file in VFS
            mustUpdate = true;
        } else if (req.isDoRecompile()) {
            // Recompile is forced with parameter
            mustUpdate = true;
        }

        String jspfilename = getJspUri(file.getAbsolutePath(), req.isOnline());               
        
        if (mustUpdate) {
            if (DEBUG > 2) System.err.println("JspLoader writing new file: " + jspfilename);         
            byte[] contents = null;
            String jspEncoding = null;
            try {
                contents = req.getCmsObject().readFile(file.getAbsolutePath()).getContents();
                // Encoding project:
                // Check the JSP "content-encoding" property
                jspEncoding = cms.readProperty(file.getAbsolutePath(), I_CmsConstants.C_PROPERTY_CONTENT_ENCODING, false);
                if (jspEncoding == null) jspEncoding = C_DEFAULT_JSP_ENCODING;
                jspEncoding = jspEncoding.trim().toLowerCase();
            } catch (CmsException e) {
                throw new ServletException("JspLoader: Could not read contents for file '" + file.getAbsolutePath() + "'", e);
            }
            
            try {
                FileOutputStream fs = new FileOutputStream(f);                
                // Encoding project:
                // We need to use some encoding to convert bytes to String
                // corectly. Internally a JSP will always be stored in the 
                // system default encoding since they are just a variation of
                // the "plain" resource type.
                String page = new String(contents, A_OpenCms.getDefaultEncoding());
                StringBuffer buf = new StringBuffer(contents.length);

                int p0 = 0, i2 = 0, slen = C_DIRECTIVE_START.length(), elen = C_DIRECTIVE_END.length();
                // Check if any jsp name references occur in the file
                int i1 = page.indexOf(C_DIRECTIVE_START);
                while (i1 >= 0) {
                    // Parse the file and replace jsp name references 
                    i2 = page.indexOf(C_DIRECTIVE_END, i1 + slen);
                    if (i2 > i1) {
                        String directive = page.substring(i1 + slen, i2);
                        if (DEBUG > 2) System.err.println("JspLoader: Detected " + C_DIRECTIVE_START + directive + C_DIRECTIVE_END);

                        int t1=0, t2=0, t3=0, t4=0, t5=0, t6=slen, t7=0;
                        while (directive.charAt(t1) == ' ') t1++;
                        String filename = null;                        
                        if (directive.startsWith("include", t1)) {            
                            if (DEBUG > 2) System.err.println("JspLoader: Detected 'include' directive!");                            
                            t2 = directive.indexOf("file", t1 + 7);
                            t5 = 6;
                        } else if (directive.startsWith("page", t1)) {
                            if (DEBUG > 2) System.err.println("JspLoader: Detected 'page' directive!");                            
                            t2 = directive.indexOf("errorPage", t1 + 4);
                            t5 = 11;
                        } else if (directive.startsWith("cms", t1)) {
                            if (DEBUG > 2) System.err.println("JspLoader: Detected 'cms' directive!");                            
                            t2 = directive.indexOf("file", t1 + 3);
                            t5 = 4; t6 = 0; t7 = elen; 
                        }
                        
                        if (t2 > 0) {
                            String sub = directive.substring(t2 + t5); 
                            char c1 = sub.charAt(t3);
                            while ((c1 == ' ') || (c1 == '=') || (c1 == '"')) c1 = sub.charAt(++t3);
                            t4 = t3;
                            while (c1 != '"') c1 = sub.charAt(++t4);
                            if (t4 > t3) filename=sub.substring(t3,t4);
                            if (DEBUG > 2) System.err.println("JspLoader: File given in directive is: " + filename);                            
                        }
                        
                        if (filename != null) {
                            // a file was found, changes have to be made
                            String pre = ((t7 == 0)?directive.substring(0,t2+t3+t5):"");                            ;
                            String suf = ((t7 == 0)?directive.substring(t2+t3+t5+filename.length()):"");
                            // Now try to update the referenced file 
                            String absolute = req.toAbsolute(filename);
                            if (DEBUG > 2) System.err.println("JspLoader: Absolute location=" + absolute);
                            String jspname = null;
                            try {
                                // Make sure the jsp referenced file is generated
                                CmsResource jsp = cms.readFileHeader(absolute);
                                updateJsp(cms, jsp, req, updates);
                                jspname = getJspUri(jsp.getAbsolutePath(), req.isOnline());
                            } catch (Exception e) {
                                jspname = null;
                                if (DEBUG > 2) System.err.println("JspLoader: Error while creating jsp file " + absolute + "\n" + e);
                            }
                            if (jspname != null) {
                                // Only change something in case no error had occured
                                if (DEBUG > 2) System.err.println("JspLoader: Name of jsp file is " + jspname);
                                directive = pre + jspname + suf;
                                if (DEBUG > 2) System.err.println("JspLoader: Changed directive to " + C_DIRECTIVE_START + directive + C_DIRECTIVE_END);                                                     
                            }
                        }
                        
                        buf.append(page.substring(p0, i1 + t6));
                        buf.append(directive);
                        p0 = i2 + t7;
                        i1 = page.indexOf(C_DIRECTIVE_START, p0);
                    }
                }
                if (i2 > 0) {
                    buf.append(page.substring(p0, page.length()));
                    // Encoding project:
                    // Now we are ready to store String data in file system.
                    // To convert String to bytes we also need to provide
                    // some encoding. The default (by the JSP standard) encoding 
                    // for JSP is ISO-8859-1.
                    contents = buf.toString().getBytes(jspEncoding);
                } else {
                    // Encoding project:
                    // Contents of original file where not modified,
                    // just translate to the required JSP encoding (if necessary)
                    String defaultEncoding = A_OpenCms.getDefaultEncoding().trim().toLowerCase();  
                    if (! jspEncoding.equals(defaultEncoding)) {
                        contents = (new String(contents, defaultEncoding)).getBytes(jspEncoding); 
                    }                    
                }                                         
                fs.write(contents);                
                fs.close();
                
                if (I_CmsLogChannels.C_LOGGING && A_OpenCms.isLogging(I_CmsLogChannels.C_OPENCMS_INFO)) 
                    A_OpenCms.log(I_CmsLogChannels.C_OPENCMS_INFO, "Updated JSP file \"" + jspfilename + "\" for resource \"" + file.getAbsolutePath() + "\"") ;
            } catch (FileNotFoundException e) {
                throw new ServletException("JspLauncher: Could not write to file '" + f.getName() + "'\n" + e, e);
            }
        }                      
        return jspfilename;
    }    
        
    /**
	 * Does the job of including the JSP, 
	 * this method should usually be called from a <code>CmsFlexRequestDispatcher</code> only.<p>
     * 
     * This method is called directly if the element is 
     * called as a sub-element from another I_CmsResourceLoader.<p>
	 * 
	 * One of the tricky issues is the correct cascading of the Exceptions, 
	 * so that you are able to identify the true origin of the problem.
	 * This ia achived by imprinting a String C_EXCEPTION_PREFIX to the 
	 * exception message.
	 * 
	 * @param cms Used to access the OpenCms VFS
	 * @param file The reqested JSP file resource in the VFS
	 * @param req The current request
	 * @param res The current response
     * 
     * @throws ServletException might be thrown in the process of including the JSP 
     * @throws IOException might be thrown in the process of including the JSP 
     * 
     * @see com.opencms.flex.cache.CmsFlexRequestDispatcher
	 */
	public void service(CmsObject cms, CmsResource file, CmsFlexRequest req, CmsFlexResponse res)
	throws ServletException, IOException {              
	    try {	
	        // Get JSP target name on "real" file system
	        String target = updateJsp(cms, file, req, new HashSet(11));               
	        // Important: Indicate that all output must be buffered
	        res.setOnlyBuffering(true);   
	        // Dispatch to external file
	        req.getCmsRequestDispatcher(file.getAbsolutePath(), target).include(req, res);  	        
	    } catch (ServletException e) {          
	        // Check if this Exception has already been marked
	        String msg = e.getMessage();
	        if (DEBUG > 1) System.err.println("JspLauncher: Caught ServletException " + e );
	        if (msg.startsWith(C_LOADER_EXCEPTION_PREFIX)) throw e;
	        // Not marked, imprint current JSP file and stack trace
	        throw new ServletException(C_LOADER_EXCEPTION_PREFIX + " '" + file.getAbsolutePath() + "'\n\nRoot cause:\n" + Utils.getStackTrace(e) + "\n--------------- End of root cause.\n", e);           
	    } catch (Exception e) {
	        // Imprint current JSP file and stack trace
	        throw new ServletException(C_LOADER_EXCEPTION_PREFIX + " '" + file.getAbsolutePath() + "'\n\nRoot cause:\n" + Utils.getStackTrace(e) + "\n--------------- End of root cause.\n", e);          
	    }
	} 
}
