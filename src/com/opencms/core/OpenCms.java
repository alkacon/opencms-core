
package com.opencms.core;

import java.io.*;
import java.util.*;
import java.lang.reflect.*;

import javax.servlet.*;
import javax.servlet.http.*;

import source.org.apache.java.io.*;
import source.org.apache.java.util.*;

import com.opencms.file.*;
import com.opencms.launcher.*;


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
* @version $Revision: 1.12 $ $Date: 2000/01/21 14:51:02 $  
* 
*/

class OpenCms extends A_OpenCms implements I_CmsConstants, I_CmsLogChannels 
{

    /**
     * Definition of the index page
     */
    private static String C_INDEX ="index.html";
    
    /**
     * The default mimetype
     */
     private static String C_DEFAULT_MIMETYPE="application/octet-stream";
     
     /**
      * The session storage for all active users.
      */
     private CmsSession m_sessionStorage;
 
     /**
      * The reference to the resource broker
      */
     private I_CmsResourceBroker m_rb;

     /**
      * Reference to the OpenCms launcer manager
      */
     private CmsLauncherManager m_launcherManager;

     
     /**
      * Hashtable with all available Mimetypes.
      */
     private Hashtable m_mt=new Hashtable();
     
     /**
      * Constructor, creates a new OpenCms object.
      * It connects to the poerty database to read all requred data to set up the
      * OpenCms system and creates an initalizer object which initiates all requires
      * access modules and resource brokers.
      * 
      * @param driver The database driver for the property database.
      * @param connect The connect string to the property database.
      * @param classname The name of the initalizer class. 
      */
     OpenCms(String driver, String connect, String classname) {
        // invoke the ResourceBroker via the initalizer
        try {
  		    m_rb = ((A_CmsInit) Class.forName(classname).newInstance() ).init(driver, connect);
            CmsObject cms=new CmsObject();
            cms.init(m_rb);
        } catch (Exception e) {
            System.err.println(e.getMessage());    
        }
        
        // try to initialize the launchers.
        try {
            m_launcherManager = new CmsLauncherManager();
        } catch (Exception e) {
            System.err.println(e.getMessage());    
        }            
        
        // initalize the Hashtable with all available mimetypes
        initMimetypes();
     }
     
     /**
     * This method gets the requested document from the OpenCms and returns it to the 
     * calling module.
     * 
     * @param cms The CmsObject containing all information about the requested document
     * and the requesting user.
     * @return CmsFile object.
     */
     CmsFile initResource(CmsObject cms) 
        throws CmsException, IOException {
          
        CmsFile file=null;
        try {
            //read the requested file
            file =cms.readFile(cms.getRequestContext().currentUser(),
                               cms.getRequestContext().currentProject(),
                               cms.getRequestContext().getUri());
        } catch (CmsException e ) {
            if (e.getType() == CmsException.C_NOT_FOUND) {
                // there was no file found with this name. 
                // it is possible that the requested resource was a folder, so try to access an
                // index.html there
                String resourceName=cms.getRequestContext().getUri();
                // test if the requested file is already the index.html
                if (!resourceName.endsWith(C_INDEX)) {
                    // check if the requested file ends with an "/"
                    if (!resourceName.endsWith("/")) {
                       resourceName+="/";
                     }
                     //redirect the request to the index.html
                    resourceName+=C_INDEX;
                    cms.getRequestContext().getResponse().sendCmsRedirect(resourceName);
                } else {
                    // throw the CmsException.
                    throw e;
                }
           } else {
               // throw the CmsException.
              throw e;
          }
        }
        if (file != null) {
            // test if this file is only available for internal access operations
            if ((file.getAccessFlags() & C_ACCESS_INTERNAL_READ) >0) {
            throw new CmsException (CmsException.C_EXTXT[CmsException.C_INTERNAL_FILE]+cms.getRequestContext().getUri(),
                                    CmsException.C_INTERNAL_FILE);
            }
        }
            
        return file;
    }

     
    /**
     * Selects the appropriate launcher for a given file by analyzing the 
     * file's launcher id and calls the initlaunch() method to initiate the 
     * generating of the output.
     * 
     * @param cms A_CmsObject containing all document and user information
     * @param file CmsFile object representing the selected file.
     * @exception CmsException
     */
    void showResource(A_CmsObject cms, CmsFile file) throws CmsException { 
        int launcherId = file.getLauncherType();
        String startTemplateClass = file.getLauncherClassname();
        I_CmsLauncher launcher = m_launcherManager.getLauncher(launcherId);
        if(launcher == null) {
            String errorMessage = "Could not launch file " + file.getName() 
                + ". Launcher for requested launcher ID " + launcherId + " could not be found.";
            if(A_OpenCms.isLogging()) {
                    A_OpenCms.log(C_OPENCMS_INFO, "[OpenCms] " + errorMessage);
            }
            throw new CmsException(errorMessage, CmsException.C_UNKNOWN_EXCEPTION);
        }
        launcher.initlaunch(cms, file, startTemplateClass);
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
    void setResponse(A_CmsObject cms, CmsFile file){
        String ext=null;
        String mimetype=null;
        int lastDot=file.getName().lastIndexOf(".");
        // check if there was a file extension
        if ((lastDot>0) && (!file.getName().endsWith("."))){
         ext=file.getName().substring(lastDot+1,file.getName().length());   
         mimetype=(String)m_mt.get(ext);
         // was there a mimetype fo this extension?
         if (mimetype != null) {
             cms.getRequestContext().getResponse().setContentType(mimetype);
         } else {
             cms.getRequestContext().getResponse().setContentType(C_DEFAULT_MIMETYPE);
         }
        } else {
             cms.getRequestContext().getResponse().setContentType(C_DEFAULT_MIMETYPE);
        }
    }
    
    /**
	 * Inits all mimetypes.
	 * The mimetype-data should be stored in the database. But now this data
	 * is putted directly here.
	 */
	private void initMimetypes() {
		// HACK: read this from the database!
		m_mt.put( "ez", "application/andrew-inset" );
		m_mt.put( "hqx", "application/mac-binhex40" );
		m_mt.put( "cpt", "application/mac-compactpro" );
		m_mt.put( "doc", "application/msword" );
		m_mt.put( "bin", "application/octet-stream" );
		m_mt.put( "dms", "application/octet-stream" );
		m_mt.put( "lha", "application/octet-stream" );
		m_mt.put( "lzh", "application/octet-stream" );
		m_mt.put( "exe", "application/octet-stream" );
		m_mt.put( "class", "application/octet-stream" );
		m_mt.put( "oda", "application/oda" );
		m_mt.put( "pdf", "application/pdf" );
		m_mt.put( "ai", "application/postscript" );
		m_mt.put( "eps", "application/postscript" );
		m_mt.put( "ps", "application/postscript" );
		m_mt.put( "rtf", "application/rtf" );
		m_mt.put( "smi", "application/smil" );
		m_mt.put( "smil", "application/smil" );
		m_mt.put( "mif", "application/vnd.mif" );
		m_mt.put( "xls", "application/vnd.ms-excel" );
		m_mt.put( "ppt", "application/vnd.ms-powerpoint" );
		m_mt.put( "bcpio", "application/x-bcpio" );
		m_mt.put( "vcd", "application/x-cdlink" );
		m_mt.put( "pgn", "application/x-chess-pgn" );
		m_mt.put( "cpio", "application/x-cpio" );
		m_mt.put( "csh", "application/x-csh" );
		m_mt.put( "dcr", "application/x-director" );
		m_mt.put( "dir", "application/x-director" );
		m_mt.put( "dxr", "application/x-director" );
		m_mt.put( "dvi", "application/x-dvi" );
		m_mt.put( "spl", "application/x-futuresplash" );
		m_mt.put( "gtar", "application/x-gtar" );
		m_mt.put( "hdf", "application/x-hdf" );
		m_mt.put( "js", "application/x-javascript" );
		m_mt.put( "skp", "application/x-koan" );
		m_mt.put( "skd", "application/x-koan" );
		m_mt.put( "skt", "application/x-koan" );
		m_mt.put( "skm", "application/x-koan" );
		m_mt.put( "latex", "application/x-latex" );
		m_mt.put( "nc", "application/x-netcdf" );
		m_mt.put( "cdf", "application/x-netcdf" );
		m_mt.put( "sh", "application/x-sh" );
		m_mt.put( "shar", "application/x-shar" );
		m_mt.put( "swf", "application/x-shockwave-flash" );
		m_mt.put( "sit", "application/x-stuffit" );
		m_mt.put( "sv4cpio", "application/x-sv4cpio" );
		m_mt.put( "sv4crc", "application/x-sv4crc" );
		m_mt.put( "tar", "application/x-tar" );
		m_mt.put( "tcl", "application/x-tcl" );
		m_mt.put( "tex", "application/x-tex" );
		m_mt.put( "texinfo", "application/x-texinfo" );
		m_mt.put( "texi", "application/x-texinfo" );
		m_mt.put( "t", "application/x-troff" );
		m_mt.put( "tr", "application/x-troff" );
		m_mt.put( "roff", "application/x-troff" );
		m_mt.put( "man", "application/x-troff-man" );
		m_mt.put( "me", "application/x-troff-me" );
		m_mt.put( "ms", "application/x-troff-ms" );
		m_mt.put( "ustar", "application/x-ustar" );
		m_mt.put( "src", "application/x-wais-source" );
		m_mt.put( "zip", "application/zip" );
		m_mt.put( "au", "audio/basic" );
		m_mt.put( "snd", "audio/basic" );
		m_mt.put( "mid", "audio/midi" );
		m_mt.put( "midi", "audio/midi" );
		m_mt.put( "kar", "audio/midi" );
		m_mt.put( "mpga", "audio/mpeg" );
		m_mt.put( "mp2", "audio/mpeg" );
		m_mt.put( "mp3", "audio/mpeg" );
		m_mt.put( "aif", "audio/x-aiff" );
		m_mt.put( "aiff", "audio/x-aiff" );
		m_mt.put( "aifc", "audio/x-aiff" );
		m_mt.put( "ram", "audio/x-pn-realaudio" );
		m_mt.put( "rm", "audio/x-pn-realaudio" );
		m_mt.put( "rpm", "audio/x-pn-realaudio-plugin" );
		m_mt.put( "ra", "audio/x-realaudio" );
		m_mt.put( "wav", "audio/x-wav" );
		m_mt.put( "pdb", "chemical/x-pdb" );
		m_mt.put( "xyz", "chemical/x-pdb" );
		m_mt.put( "bmp", "image/bmp" );
		m_mt.put( "gif", "image/gif" );
		m_mt.put( "ief", "image/ief" );
		m_mt.put( "jpeg", "image/jpeg" );
		m_mt.put( "jpg", "image/jpeg" );
		m_mt.put( "jpe", "image/jpeg" );
		m_mt.put( "png", "image/png" );
		m_mt.put( "tiff", "image/tiff" );
		m_mt.put( "tif", "image/tiff" );
		m_mt.put( "ras", "image/x-cmu-raster" );
		m_mt.put( "pnm", "image/x-portable-anymap" );
		m_mt.put( "pbm", "image/x-portable-bitmap" );
		m_mt.put( "pgm", "image/x-portable-graymap" );
		m_mt.put( "ppm", "image/x-portable-pixmap" );
		m_mt.put( "rgb", "image/x-rgb" );
		m_mt.put( "xbm", "image/x-xbitmap" );
		m_mt.put( "xpm", "image/x-xpixmap" );
		m_mt.put( "xwd", "image/x-xwindowdump" );
		m_mt.put( "igs", "model/iges" );
		m_mt.put( "iges", "model/iges" );
		m_mt.put( "msh", "model/mesh" );
		m_mt.put( "mesh", "model/mesh" );
		m_mt.put( "silo", "model/mesh" );
		m_mt.put( "wrl", "model/vrml" );
		m_mt.put( "vrml", "model/vrml" );
		m_mt.put( "css", "text/css" );
		m_mt.put( "html", "text/html" );
		m_mt.put( "htm", "text/html" );
		m_mt.put( "asc", "text/plain" );
		m_mt.put( "txt", "text/plain" );
		m_mt.put( "rtx", "text/richtext" );
		m_mt.put( "rtf", "text/rtf" );
		m_mt.put( "sgml", "text/sgml" );
		m_mt.put( "sgm", "text/sgml" );
		m_mt.put( "tsv", "text/tab-separated-values" );
		m_mt.put( "etx", "text/x-setext" );
		m_mt.put( "xml", "text/xml" );
		m_mt.put( "mpeg", "video/mpeg" );
		m_mt.put( "mpg", "video/mpeg" );
		m_mt.put( "mpe", "video/mpeg" );
		m_mt.put( "qt", "video/quicktime" );
		m_mt.put( "mov", "video/quicktime" );
		m_mt.put( "avi", "video/x-msvideo" );
		m_mt.put( "movie", "video/x-sgi-movie" );
		m_mt.put( "ice", "x-conference/x-cooltalk" );
    }
}