/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/staticexport/Attic/CmsStaticExport.java,v $
 * Date   : $Date: 2003/08/11 11:00:11 $
 * Version: $Revision: 1.3 $
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

package org.opencms.staticexport;

import org.opencms.loader.I_CmsResourceLoader;

import com.opencms.boot.I_CmsLogChannels;
import com.opencms.core.A_OpenCms;
import com.opencms.core.CmsException;
import com.opencms.core.CmsExportRequest;
import com.opencms.core.CmsExportResponse;
import com.opencms.core.I_CmsConstants;
import com.opencms.file.CmsFile;
import com.opencms.file.CmsFolder;
import com.opencms.file.CmsObject;
import com.opencms.file.CmsPublishedResources;
import com.opencms.file.CmsResource;
import com.opencms.report.I_CmsReport;
import com.opencms.util.Utils;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Hashtable;
import java.util.List;
import java.util.StringTokenizer;
import java.util.Vector;

import org.apache.oro.text.perl.Perl5Util;

/**
 * Provides the functionaility to export resources from the cms
 * to the filesystem.<p>
 *
 * @author Alexander Kandzior (a.kandzior@alkacon.com)
 * @version $Revision: 1.3 $
 */
public class CmsStaticExport {

    /** Internal marker for resources with property "export=false" */
    private static final String C_EXPORT_FALSE = "export-value-is-false";

    /**
     * Additional rules for the export generated dynamical.
     * Used to replace ugly long foldernames with short nice
     * ones set as a property to the folder.
     * one extra version for extern
     */    
    public static Vector m_dynamicExportNameRules;
    
    /**
     * ?
     */
    public static Vector m_dynamicExportNameRulesExtern;

    /**
     * Additional rules for the export generated dynamical.
     * Used for linking back to OpenCms for dynamic content and
     * linking out to the static stuff. Used for extern. It only
     * returns "" so the staticExport dont write something to disk.
     */
    public static Vector m_dynamicExportRulesExtern;

    /**
     * Rules used for linking back to OpenCms for dynamic content and
     * linking out to the static content, used for online and for export.<p>
     */
    public static Vector m_dynamicExportRulesOnline ;

    /** Provides regular expression functionality */
    private static Perl5Util m_perlUtil;

    /** Indicates that the export is called after publish project */
    private boolean m_afterPublish;

    /** Stores the links that have changed after publishing */
    private Vector m_changedLinks;

    /** The cms context object to use for exporting */
    private CmsObject m_cms;

    /** The export path */
    private String m_exportPath;

    /** The object to report the log-messages */
    private I_CmsReport m_report;
    
    /** The Servlet url */
    private String m_servletUrl = "";

    /** The resources to export */
    private Vector m_startpoints;

    /** The url of the web application */
    private String m_webAppUrl = "";

    /**
     * Generates static html pages in the filesystem.<p>
     *
     * @param cms the cms context object to work with
     * @param startpoints the resources to export (Vector of Strings)
     * @param doTheExport must be set to true to export something, otherwise only the link rules are generated
     * @param changedLinks return Vector with the changed links for the search module (worked with the getlinksubstitution() method)
     * @param changedResources return Vector with the changed resources belonging to the project, if started after publishProject()
     * @param report to handle the log messages
     *
     * @throws CmsException if something goes wrong
     */
    public CmsStaticExport(CmsObject cms, Vector startpoints, boolean doTheExport, Vector changedLinks, CmsPublishedResources changedResources, I_CmsReport report) throws CmsException {

        /* Notes:
         * 
         * The Vector "changedLinks" is a return Vector.
         * It is needed only to pass it to the "publish" methods in the modules.
         * What the modules do with it has still to be determined.
         * 
         * The method "checkExportPath()" in this class should be moved
         * to the class CmsStaticExportProperties. It seems unneccessary
         * to check the path every time. The check should be performed when setting
         * path in the properties class.
         * 
         * There are dependencies saved in the database.
         * These are read in the method "getChangedLinks()" in this class.
         * What exctly is stored there and when it is stored has still to be determined.
         * 
         * The dependency Vector is used to determine what resources must be
         * exported. This is related to the "startpoints". The startpoints are
         * used only if this is called from the Admin screen. Otherwise
         * the "changedResources" class is used to determine the startpoints,
         * and the dependencys are checked for this resources (see above).
         * 
         * The export itself is done in a simple for loop that iterates
         * a Vector "exportLinks" that in fact is the list of startpoints (see above).
         * However, the Vector is "grown" i.e. new links are appended to the Vector while 
         * the export process is in progress. 
         * 
         * TODO:
         * - Move "ceckExportPath()" to class CmsStaticExportProperties
         * - Make the constructor of this class a simple constructor and add a "export()" method
         *   that is called to start the actucal aexport
         * - Have the "export()" method return the Values as a return value and not though
         *   parameters
         * - Apparently there is to much program logic in the DB methods that are called 
         *   to determine the link dependencies, move this logic to method "getChangedLinks()"
         * - Resolve the strange "for" loop that is grown dynamically and have some kind
         *   clear way to add the results of an export  
         * 
         * 
         * Database methods dealing with the static export: 
         * 
         * CmsProjectDriver.getDependingExportLinks()
         * CmsProjectDirver.readExportLinkHeader()
         * CmsProjectDriver.writeExportLink()
         */
         
        boolean dummy = true;
        if (dummy) return;

        m_cms = cms;
        m_startpoints = startpoints;
        m_changedLinks = changedLinks;
        m_exportPath = A_OpenCms.getStaticExportProperties().getExportPath();
        m_perlUtil = new Perl5Util();
        m_report = report;        
        m_afterPublish = (changedResources != null);
            
        if (!doTheExport) {
            // this is just to generate the dynamic rulesets
            if (I_CmsLogChannels.C_LOGGING && A_OpenCms.isLogging(I_CmsLogChannels.C_OPENCMS_INIT)) {
                A_OpenCms.log(I_CmsLogChannels.C_OPENCMS_INIT, ". Generating rulesets  : ok");
            }
            createDynamicRules();
            return;
        }
        
        if (cms.getRequestContext().getRequest() == null) {
            // this will be true if OpenCms is running from the CmsShell, where we can't use the export 
            return;
        }
        
        m_servletUrl = cms.getRequestContext().getRequest().getServletUrl();
        m_webAppUrl = cms.getRequestContext().getRequest().getWebAppUrl();
                
        try {
            
            if (I_CmsLogChannels.C_LOGGING && A_OpenCms.isLogging(I_CmsLogChannels.C_OPENCMS_STATICEXPORT)) {
                A_OpenCms.log(I_CmsLogChannels.C_OPENCMS_STATICEXPORT, "[CmsStaticExport] Starting the static export.");
            }
            
            // generate the Perl rules for name replacement
            createDynamicRules();

            // check the export path (this should be moved into CmsStaticExportProperties) 
            checkExportPath();
            
            Vector exportLinks = null;
            if (m_afterPublish) {
                exportLinks = getChangedLinks(changedResources);
            } else {
                exportLinks = getStartLinks();
            }

            if (I_CmsLogChannels.C_LOGGING && A_OpenCms.isLogging(I_CmsLogChannels.C_OPENCMS_STATICEXPORT)) {
                A_OpenCms.log(I_CmsLogChannels.C_OPENCMS_STATICEXPORT, "[CmsStaticExport] got " + exportLinks.size() + " links to start with.");
            }

            boolean doExport = (exportLinks.size() > 0);
            
            if (doExport) {
                m_report.print(m_report.key("report.static_export_begin"), I_CmsReport.C_FORMAT_HEADLINE);
                m_report.println(" " + exportLinks.size(), I_CmsReport.C_FORMAT_HEADLINE);
                
                // TODO: This Vector "grows" dynamically during the process
                // TODO: Should be calculated completly from the start OR handled differently 
                for (int i = 0; i < exportLinks.size(); i++) {
                    String aktLink = (String)exportLinks.elementAt(i);
                    exportLink(aktLink, exportLinks, true);                
                }
                
            }
            
            setChangedLinkVector(exportLinks);           
            
            if (doExport) {
                m_report.println(m_report.key("report.static_export_end"), I_CmsReport.C_FORMAT_HEADLINE);
            } else {
                m_report.println(m_report.key("report.static_export_none"), I_CmsReport.C_FORMAT_HEADLINE);
            }
            
            if (I_CmsLogChannels.C_LOGGING && A_OpenCms.isLogging(I_CmsLogChannels.C_OPENCMS_STATICEXPORT)) {
                A_OpenCms.log(I_CmsLogChannels.C_OPENCMS_STATICEXPORT, "[CmsStaticExport] all done.");
            }
            
        } catch (NullPointerException e) {
            m_report.println(e);
            if (I_CmsLogChannels.C_LOGGING && A_OpenCms.isLogging(I_CmsLogChannels.C_OPENCMS_STATICEXPORT)) {
                A_OpenCms.log(I_CmsLogChannels.C_OPENCMS_STATICEXPORT, "[CmsStaticExport] Nothing found to export.");
            }
        }
    }

    /**
     * Handles the dynamic rules created by opencms
     * in using the properties of resources.<p>
     *
     * @param cms used for the parameter replace
     * @param link the link that has to be replaced
     * @param modus the modus OpenCms runs in
     * @param paramterOnly is set to true if only the parameters are replaced and no other rules
     * @return a String
     */
    public static String handleDynamicRules(CmsObject cms, String link, int modus, Vector paramterOnly) {
        // first get the ruleset
        Vector dynRules = null;
        Vector nameRules = null;
        if (modus == I_CmsConstants.C_MODUS_EXTERN) {
            dynRules = m_dynamicExportRulesExtern;
            nameRules = m_dynamicExportNameRulesExtern;
        } else {
            dynRules = m_dynamicExportRulesOnline;
            nameRules = m_dynamicExportNameRules;
        }
        String retValue = link;
        int count;
        StringBuffer result = null;
        if (dynRules != null) {
            for (int i = 0; i < dynRules.size(); i++) {
                result = new StringBuffer();
                count = m_perlUtil.substitute(result, (String)dynRules.elementAt(i), link);
                if (count != 0) {
                    paramterOnly.add(new Boolean(false));
                    retValue = result.toString();
                    if (A_OpenCms.getStaticExportProperties().isExportDefault()) {
                        return retValue;
                    } else {
                        // default is dynamic
                        if (modus == I_CmsConstants.C_MODUS_EXTERN) {
                            if (!link.equals(retValue)) {
                                return retValue;
                            }
                        } else {
                            if (!retValue.startsWith(A_OpenCms.getStaticExportProperties().getExportPrefix())) {
                                return retValue;
                            }
                            link = retValue;
                        }
                    }
                }
            }
        }
        // here we can start the parameters if it will be exported
        if (A_OpenCms.getStaticExportProperties().isExportDefault() || modus == I_CmsConstants.C_MODUS_EXTERN) {
            link = substituteLinkParameter(cms, link);
            retValue = link;
        } else if (link.startsWith(A_OpenCms.getStaticExportProperties().getExportPrefix())) {
            link = link.substring(A_OpenCms.getStaticExportProperties().getExportPrefix().length());
            link = substituteLinkParameter(cms, link);
            link = A_OpenCms.getStaticExportProperties().getExportPrefix() + link;
            retValue = link;
        }

        // now for the name replacement rules
        if (nameRules != null) {
            for (int i = 0; i < nameRules.size(); i++) {
                retValue = m_perlUtil.substitute((String)nameRules.elementAt(i), link);
                if (!retValue.equals(link)) {
                    paramterOnly.add(new Boolean(false));
                    return retValue;
                }
            }
        }
        // nothing changed
        paramterOnly.add(new Boolean(true));
        return retValue;
    }

    /**
     * Parses a name in the query string.<p>
     * 
     * @param s the String to parse 
     * @param sb StringBuffer
     * @return the parsed result
     */
    private static String parseName(String s, StringBuffer sb) {
        sb.setLength(0);
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            switch (c) {
                case '+' :
                    sb.append(' ');
                    break;
                case '%' :
                    try {
                        sb.append((char)Integer.parseInt(s.substring(i + 1, i + 3), 16));
                        i += 2;
                    } catch (NumberFormatException e) {
                        // XXX
                        // need to be more specific about illegal arg
                        throw new IllegalArgumentException();
                    } catch (StringIndexOutOfBoundsException e) {
                        String rest = s.substring(i);
                        sb.append(rest);
                        if (rest.length() == 2)
                            i++;
                    }

                    break;
                default :
                    sb.append(c);
                    break;
            }
        }
        return sb.toString();
    }

    /**
     * Parses a query string passed from the client to the
     * server and builds a <code>HashTable</code> object
     * with key-value pairs.<p>
     *  
     * The query string should be in the form of a string
     * packaged by the GET or POST method, that is, it
     * should have key-value pairs in the form <i>key=value</i>,
     * with each pair separated from the next by a & character.<p>
     *
     * A key can appear more than once in the query string
     * with different values. However, the key appears only once in 
     * the hashtable, with its value being
     * an array of strings containing the multiple values sent
     * by the query string.<p>
     * 
     * The keys and values in the hashtable are stored in their
     * decoded form, so
     * any + characters are converted to spaces, and characters
     * sent in hexadecimal notation (like <i>%xx</i>) are
     * converted to ASCII characters.
     *
     * @param s a string containing the query to be parsed
     * @return a <code>HashTable</code> object from the parsed key-value pairs
     * @throws IllegalArgumentException if the query string is invalid
     */
    private static Hashtable parseQueryString(String s) {
        String valArray[] = null;
        if (s == null) {
            throw new IllegalArgumentException();
        }
        Hashtable ht = new Hashtable();
        StringBuffer sb = new StringBuffer();
        StringTokenizer st = new StringTokenizer(s, "&");
        while (st.hasMoreTokens()) {
            String pair = st.nextToken();
            int pos = pair.indexOf('=');
            if (pos == -1) {
                // XXX
                // should give more detail about the illegal argument
                throw new IllegalArgumentException();
            }
            String key = parseName(pair.substring(0, pos), sb);
            String val = parseName(pair.substring(pos + 1, pair.length()), sb);
            if (ht.containsKey(key)) {
                String oldVals[] = (String[])ht.get(key);
                valArray = new String[oldVals.length + 1];
                for (int i = 0; i < oldVals.length; i++)
                    valArray[i] = oldVals[i];
                valArray[oldVals.length] = val;
            } else {
                valArray = new String[1];
                valArray[0] = val;
            }
            ht.put(key, valArray);
        }
        return ht;
    }

    /**
     * If a link has html parameters this methode removes them and adds an index
     * to the link.<p>
     * 
     * This index is the linkId in the database where all exported
     * links are saved. i.e. /test/index.html?para1=1&para2=2&para3=3 to
     * /test/index_32.html (when 32 is the databaseId of this link).
     * If the link is not in the database this methode saves it to create the id.<p>
     *
     * @param cms the cms context
     * @param link the Link to process
     * @return the processed link
     */
    private static String substituteLinkParameter(CmsObject cms, String link) {
        // has it parameters?
        int paraStartPos = link.indexOf('?');
        if (paraStartPos < 0) {
            // no parameter - no service
            return link;
        }
        // is the export activ?
        if (!A_OpenCms.getStaticExportProperties().isStaticExportEnabled()) {
            return link;
        }
        // cut the parameters
        String returnValue = link.substring(0, paraStartPos);
        try {
            // get the id from the database
            CmsStaticExportLink linkObject = cms.readExportLinkHeader(link);
            if (linkObject == null) {
                // it was not written before, so we have to do it here.
                linkObject = new CmsStaticExportLink(link, 0, null);
                cms.writeExportLink(linkObject);
            }
            int id = linkObject.getId();
            // now we put this id at the right place in the link
            int pointId = returnValue.lastIndexOf('.');
            if (pointId < 0) {
                return returnValue + "_" + id;
            }
            returnValue = returnValue.substring(0, pointId) + "_" + id + returnValue.substring(pointId);

        } catch (CmsException e) {
            if (I_CmsLogChannels.C_PREPROCESSOR_IS_LOGGING && A_OpenCms.isLogging()) {
                A_OpenCms.log(I_CmsLogChannels.C_OPENCMS_STATICEXPORT, "[CmsStaticExport] cant substitute the htmlparameter for link: " + link + "  " + e.toString());
            }
            return link;
        }
        return returnValue;
    }

    /**
     * Adds all subfiles of a folder to the Vector.<p>
     *
     * @param links The vector to add the filenames to
     * @param folder The folder where the files are in
     * @throws CmsException if something goes wrong
     */
    private void addSubFiles(Vector links, String folder) throws CmsException {

        // the firstlevel files
        List files = m_cms.getFilesInFolder(folder);
        for (int i = 0; i < files.size(); i++) {
            links.add(m_cms.readAbsolutePath((CmsFile)files.get(i)));
        }
        List subFolders = m_cms.getSubFolders(folder);
        for (int i = 0; i < subFolders.size(); i++) {
            addSubFiles(links, m_cms.readAbsolutePath((CmsFolder)subFolders.get(i)));
        }
    }

    /**
     * Checks if the export path exists, if not it is created.<p>
     * 
     * @throws CmsException if something goes wrong
     */
    private void checkExportPath() throws CmsException {

        // TODO: This should be in the CmsStaticExportProperties and not be tested every time during the export

        if (m_exportPath == null) {
            if (I_CmsLogChannels.C_PREPROCESSOR_IS_LOGGING && A_OpenCms.isLogging()) {
                A_OpenCms.log(I_CmsLogChannels.C_OPENCMS_STATICEXPORT, "[CmsStaticExport] no export folder given (null).");
            }
            throw new CmsException("[" + this.getClass().getName() + "] " + "no export folder given (null)", CmsException.C_BAD_NAME);
        }
        // we don't need the last slash
        if (m_exportPath.endsWith("/")) {
            m_exportPath = m_exportPath.substring(0, m_exportPath.length() - 1);
        }
        File discFolder = new File(m_exportPath + "/");
        if (!discFolder.exists()) {
            if (I_CmsLogChannels.C_PREPROCESSOR_IS_LOGGING && A_OpenCms.isLogging()) {
                A_OpenCms.log(I_CmsLogChannels.C_OPENCMS_STATICEXPORT, "[CmsStaticExport] the export folder does not exist.");
            }
            throw new CmsException("[" + this.getClass().getName() + "] " + "the export folder does not exist", CmsException.C_BAD_NAME);
        }
    }

    /**
     * Generates the dynamic rules for the export.<p> 
     * 
     * It looks for all folders that have the property "exportname" set. 
     * For each folder found it generates a rule that
     * replaces the folder name (incl. path) with the exportname.
     * In addition it looks for the property "dynamic" and creates rules for linking
     * between static and dynamic pages.
     * These rules are used for export and for extern links.<p>
     */
    private void createDynamicRules() {
        // first the rules for namereplacing
        try {
            // get the resources with the property exportname
            Vector resWithProp = null;

            resWithProp = m_cms.getResourcesWithPropertyDefinition(I_CmsConstants.C_PROPERTY_EXPORTNAME);

            // generate the dynamic rules for the nice exportnames
            if (resWithProp != null && resWithProp.size() != 0) {
                m_dynamicExportNameRules = new Vector();
                m_dynamicExportNameRulesExtern = new Vector();

                for (int i = 0; i < resWithProp.size(); i++) {
                    CmsResource resource = (CmsResource)resWithProp.elementAt(i);
                    String resName = m_cms.readAbsolutePath(resource);
                    String newName = m_cms.readProperty(resName, I_CmsConstants.C_PROPERTY_EXPORTNAME);

                    if (newName != null) {
                        resName = m_cms.getRequestContext().addSiteRoot(resName);
                        newName = newName.trim();

                        if (A_OpenCms.getStaticExportProperties().isExportDefault()) {
                            m_dynamicExportNameRules.addElement("s#^" + resName + "#" + A_OpenCms.getStaticExportProperties().getExportPrefix() + newName + "#");
                        } else {
                            m_dynamicExportNameRules.addElement("s#^(" + A_OpenCms.getStaticExportProperties().getExportPrefix() + ")" + resName + "#$1" + newName + "#");
                        }
                        m_dynamicExportNameRulesExtern.addElement("s#^" + resName + "#" + newName + "#");
                    }
                }

            }
        } catch (CmsException e) {
            if (I_CmsLogChannels.C_PREPROCESSOR_IS_LOGGING && A_OpenCms.isLogging()) {
                A_OpenCms.log(I_CmsLogChannels.C_OPENCMS_STATICEXPORT, "[CmsStaticExport] " + "couldnt create dynamic export rules for the nice names. Will use the original names instead." + e.toString());
            }
        }
        // now the rules for linking between static and dynamic pages
        try {
            // get the resources with the property "export"
            Vector resWithProp = m_cms.getResourcesWithPropertyDefinition(I_CmsConstants.C_PROPERTY_EXPORT);
            // generate the rules
            if ((resWithProp != null) && (resWithProp.size() != 0)) {

                m_dynamicExportRulesExtern = new Vector();
                m_dynamicExportRulesOnline = new Vector();

                for (int i = 0; i < resWithProp.size(); i++) {
                    CmsResource resource = (CmsResource)resWithProp.elementAt(i);
                    String resName = m_cms.readAbsolutePath(resource);
                    String propertyValue = m_cms.readProperty(resName, I_CmsConstants.C_PROPERTY_EXPORT);
                    resName = m_cms.getRequestContext().addSiteRoot(resName);

                    if (propertyValue != null) {
                        propertyValue = propertyValue.trim();

                        if (propertyValue.equalsIgnoreCase("true")) {
                            m_dynamicExportRulesExtern.addElement("s#^(" + resName + ")#$1#");
                            m_dynamicExportRulesOnline.addElement("s#^(" + resName + ")#" + A_OpenCms.getStaticExportProperties().getExportPrefix() + "$1#");

                        } else if (propertyValue.equalsIgnoreCase("false")) {
                            m_dynamicExportRulesExtern.addElement("s#^" + resName + ".*#" + C_EXPORT_FALSE + "#");
                            m_dynamicExportRulesOnline.addElement("s#^(" + resName + ")#" + A_OpenCms.getStaticExportProperties().getInternPrefix() + "$1#");

                        } else if (propertyValue.equalsIgnoreCase("dynamic")) {
                            m_dynamicExportRulesExtern.addElement("s#^" + resName + ".*##");
                            m_dynamicExportRulesOnline.addElement("s#^(" + resName + ")#" + A_OpenCms.getStaticExportProperties().getInternPrefix() + "$1#");

                        }
                    }

                }
            }
        } catch (CmsException e) {
            if (I_CmsLogChannels.C_PREPROCESSOR_IS_LOGGING && A_OpenCms.isLogging()) {
                A_OpenCms.log(I_CmsLogChannels.C_OPENCMS_STATICEXPORT, "[CmsStaticExport] " + "couldnt create dynamic export rules. " + e.toString());
            }
        }
    }

    /**
     * Exports one single link and adds sublinks to the allLinks vector.<p>
     *
     * @param link the link to export may have html parameters
     * @param allLinks the vector with all links that have to be exported
     * @param writeAccess conrols if the DB should be updated, needed for the clustering mode
     *      where only the files must be written but the database was updated by the master system
     * @throws CmsException if something goes wrong
     */
    private void exportLink(String link, Vector allLinks, boolean writeAccess) throws CmsException {

        String deleteFileOnError = null;
        OutputStream outStream = null;
        CmsStaticExportLink dbLink = new CmsStaticExportLink(link, System.currentTimeMillis(), null);
        // remember the original link for later
        String remLink = link;

        try {
            // first lets create our request and response objects for the export
            CmsExportRequest dReq = new CmsExportRequest(m_webAppUrl, m_servletUrl);
            // test if there are parameters for the request
            int paraStart = link.indexOf("?");

            // get the name for the filesystem
            String externLink = getExternLinkName(link);
            boolean writeFile = true;
            if (externLink == null || externLink.equals("")) {
                writeFile = false;
            }
            if (C_EXPORT_FALSE.equals(externLink)) {
                // this resource must not be exported and has no links on it that must be exported
                m_report.print(m_report.key("report.skipping"), I_CmsReport.C_FORMAT_NOTE);
                m_report.println(link);
                return;
            }
            if (paraStart >= 0) {
                Hashtable parameter = parseQueryString(link.substring(paraStart + 1));
                link = link.substring(0, paraStart);
                dReq.setParameters(parameter);
            }
            CmsExportResponse dRes = new CmsExportResponse();

            if (writeFile) {
                // update the report
                m_report.print(m_report.key("report.exporting"), I_CmsReport.C_FORMAT_NOTE);
                m_report.println(link);
                // now create the necesary folders
                String folder = "";
                int folderIndex = externLink.lastIndexOf('/');
                if (folderIndex != -1) {
                    folder = externLink.substring(0, externLink.lastIndexOf('/'));
                }
                String correctur = "";
                if (!externLink.startsWith("/")) {
                    correctur = "/";
                    // this is only for old versions where the editor may have added linktags containing
                    // extern links. Such a link can not be exported and we dont want to create strange
                    // folders like '"http:'
                    boolean linkIsExtern = true;
                    try {
                        new URL(externLink);
                    } catch (MalformedURLException e) {
                        linkIsExtern = false;
                    }
                    if (linkIsExtern) {
                        throw new CmsException(" This is an external link.");
                    }
                }
                File discFolder = new File(m_exportPath + correctur + folder);
                if (!discFolder.exists()) {
                    if (!discFolder.mkdirs()) {
                        throw new CmsException("[" + this.getClass().getName() + "] " + "could't create all folders for " + folder + ".");
                    }
                }
                // all folders exist now create the file
                File discFile = new File(m_exportPath + correctur + externLink);
                if (!discFile.isDirectory()) {
                    deleteFileOnError = m_exportPath + correctur + externLink;
                    try {
                        // link the File to the request
                        outStream = new FileOutputStream(discFile);
                        dRes.putOutputStream(outStream);
                    } catch (Exception e) {
                        throw new CmsException("[" + this.getClass().getName() + "] " + "couldn't open file " + m_exportPath + correctur + externLink + ": " + e.getMessage());
                    }
                }
            } else {
                // update the report
                m_report.print(m_report.key("report.following_links_on"), I_CmsReport.C_FORMAT_NOTE);
                m_report.println(link);
                // we dont want to write this file but we have to generate it to get links in it.
                dRes.putOutputStream(new ByteArrayOutputStream());
            }

            // everthing is prepared now start the template mechanism
            CmsObject cmsForStaticExport = m_cms.getCmsObjectForStaticExport(dReq, dRes);
            cmsForStaticExport.setMode(I_CmsConstants.C_MODUS_EXPORT);

            CmsFile file = m_cms.readFile(link);

            int loaderId = file.getLoaderId();
            I_CmsResourceLoader loader = A_OpenCms.getLoaderManager().getLoader(loaderId);
            if (loader == null) {
                throw new CmsException("Could not export file " + link + ". Loader for requested loader ID " + loaderId + " could not be found.");
            }
            ((CmsExportRequest)cmsForStaticExport.getRequestContext().getRequest()).setRequestedResource(link);
            cmsForStaticExport.getRequestContext().addDependency(file.getResourceName());
            // Encoding project:
            // make new detection of current encoding because we have changed the requested resource
            cmsForStaticExport.getRequestContext().initEncoding();
            loader.export(cmsForStaticExport, file);

            // we need the links on the page for the further export
            Vector linksToAdd = cmsForStaticExport.getRequestContext().getLinkVector();
            for (int i = 0; i < linksToAdd.size(); i++) {
                if (!allLinks.contains(linksToAdd.elementAt(i))) {
                    CmsStaticExportLink lookup = m_cms.readExportLinkHeader((String)linksToAdd.elementAt(i));
                    if (!m_afterPublish || (lookup == null) || (lookup.getLastExportDate() == 0)) {
                        // after publish we only add this link if it is was
                        // not exported befor.
                        allLinks.add(linksToAdd.elementAt(i));
                    }
                }
            }
            // now get the dependencies and write the link to the database
            if (writeAccess) {
                Vector depsToAdd = cmsForStaticExport.getRequestContext().getDependencies();
                for (int i = 0; i < depsToAdd.size(); i++) {
                    dbLink.addDependency((String)depsToAdd.elementAt(i));
                }
                try {
                    dbLink.setProcessedState(true);
                    m_cms.writeExportLink(dbLink);
                } catch (CmsException e) {
                    m_report.println(e);
                    if (I_CmsLogChannels.C_PREPROCESSOR_IS_LOGGING && A_OpenCms.isLogging()) {
                        A_OpenCms.log(I_CmsLogChannels.C_OPENCMS_STATICEXPORT, "[CmsStaticExport] write ExportLink " + dbLink.getLink() + " failed: " + e.toString());
                    }
                }
            }

            // at last close the outputstream
            if (outStream != null) {
                outStream.close();
            }
        } catch (CmsException exc) {
            if (exc.getType() == CmsException.C_NOT_FOUND) {
                // resource was not found in VFS, probably the link does not exist, do 
                // not display an exception but a warning message
                m_report.println(m_report.key("report.file_does_not_exist_skipping_export"), I_CmsReport.C_FORMAT_NOTE);
            } else {
                m_report.println(exc);
                if (I_CmsLogChannels.C_LOGGING && A_OpenCms.isLogging(I_CmsLogChannels.C_OPENCMS_STATICEXPORT)) {
                    A_OpenCms.log(I_CmsLogChannels.C_OPENCMS_STATICEXPORT, "[CmsStaticExport] " + deleteFileOnError + " export " + link + " failed: " + exc.toString());
                }
            }
            if (deleteFileOnError != null) {
                try {
                    File deleteMe = new File(deleteFileOnError);
                    if (deleteMe.exists()) {
                        if (outStream != null) {
                            outStream.close();
                        }
                        deleteMe.delete();
                        m_cms.deleteExportLink(remLink);
                    }
                } catch (Exception e) { }
            }
        } catch (Exception e) {
            m_report.println(e);
            if (I_CmsLogChannels.C_LOGGING && A_OpenCms.isLogging(I_CmsLogChannels.C_OPENCMS_STATICEXPORT)) {
                A_OpenCms.log(I_CmsLogChannels.C_OPENCMS_STATICEXPORT, "[CmsStaticExport]  export " + link + " failed : " + Utils.getStackTrace(e));
            }
        }
    }

    /**
     * Returns a Vector with all links that must be exported.<p>
     *
     * @param changedResources changed resources 
     * @return the links (vector of strings)
     * @throws CmsException if something goes wrong
     */
    private Vector getChangedLinks(CmsPublishedResources changedResources) throws CmsException {
        String currentResourceName = null;

        if (changedResources == null) {
            return new Vector();
        }

        Vector resToCheck = new Vector();

        Vector addVector = changedResources.getChangedModuleMasters();
        if (addVector != null) {
            resToCheck.addAll(addVector);
        }

        addVector = changedResources.getChangedResources();
        if (addVector != null) {
            resToCheck.addAll(addVector);
        }

        // Reads all dependencies of the resources to publish from the database
        Vector returnValue = m_cms.getDependingExportLinks(resToCheck);

        // we also need all "new" files
        if (addVector != null) {
            for (int i = 0; i < addVector.size(); i++) {
                currentResourceName = (String)addVector.elementAt(i);
                if ((!currentResourceName.endsWith("/")) && (!returnValue.contains(currentResourceName))) {
                    returnValue.add(currentResourceName);
                }
            }
        }
        return returnValue;
    }

    /**
     * Returns the name used for the file that is exported.<p>
     * 
     * @param linkparam the link to get the file name for
     * @return the name used for the file that is exported
     */
    private String getExternLinkName(String linkparam) {

        String link = m_cms.getRequestContext().addSiteRoot(linkparam);

        String[] rules = A_OpenCms.getStaticExportProperties().getLinkRules(I_CmsConstants.C_MODUS_EXTERN);
        String startRule = A_OpenCms.getStaticExportProperties().getStartRule();
        if (startRule != null && !"".equals(startRule)) {
            try {
                link = m_perlUtil.substitute(startRule, link);
            } catch (Exception e) {
                if (I_CmsLogChannels.C_PREPROCESSOR_IS_LOGGING && A_OpenCms.isLogging()) {
                    A_OpenCms.log(I_CmsLogChannels.C_OPENCMS_CRITICAL, "[" + this.getClass().getName() + "] problems with startrule:\"" + startRule + "\" (" + e + "). ");
                }
            }
        }
        if ((rules == null) || (rules.length == 0)) {
            return link;
        }
        String retValue = link;
        for (int i = 0; i < rules.length; i++) {
            try {
                if ("*dynamicRules*".equals(rules[i])) {
                    // here we go trough our dynamic rules
                    Vector booleanReplace = new Vector();
                    retValue = handleDynamicRules(m_cms, link, I_CmsConstants.C_MODUS_EXTERN, booleanReplace);
                    Boolean goOn = (Boolean)booleanReplace.firstElement();
                    if (goOn.booleanValue()) {
                        link = retValue;
                    } else {
                        // found the match
                        return retValue;
                    }
                } else {
                    StringBuffer result = new StringBuffer();
                    int matches = m_perlUtil.substitute(result, rules[i], link);
                    if (matches != 0) {
                        // found the match
                        return result.toString();
                    }
                }
            } catch (Exception e) {
                if (I_CmsLogChannels.C_PREPROCESSOR_IS_LOGGING && A_OpenCms.isLogging()) {
                    A_OpenCms.log(I_CmsLogChannels.C_OPENCMS_CRITICAL, "[" + this.getClass().getName() + "] problems with rule:\"" + rules[i] + "\" (" + e + "). ");
                }
            }
        }
        return retValue;
    }

    /**
     * Returns a vector with all links in the startpoints.<p>
     *
     * @return the links (vector of strings)
     */
    private Vector getStartLinks() {

        Vector exportLinks = new Vector();
        for (int i = 0; i < m_startpoints.size(); i++) {
            String cur = (String)m_startpoints.elementAt(i);
            if (cur.endsWith("/")) {
                // a folder, get all files in it
                try {
                    addSubFiles(exportLinks, cur);
                } catch (CmsException e) {
                    if (I_CmsLogChannels.C_PREPROCESSOR_IS_LOGGING && A_OpenCms.isLogging()) {
                        A_OpenCms.log(I_CmsLogChannels.C_OPENCMS_STATICEXPORT, "[CmsStaticExport] error couldn't get all subfolder from startpoint " + cur + ": " + e.toString());
                    }
                }
            } else {
                exportLinks.add(cur);
            }
        }
        return exportLinks;
    }

    /**
     * Fills the return value vectors m_allExportedLinks and m_changedLinks.<p>
     * 
     * @param exportLinks the export links
     */
    private void setChangedLinkVector(Vector exportLinks) {
        if (m_changedLinks == null) {
            m_changedLinks = new Vector();
        }
        int oldMode = m_cms.getMode();
        m_cms.setMode(I_CmsConstants.C_MODUS_ONLINE);
        for (int i = 0; i < exportLinks.size(); i++) {
            // for the changedLinks we need the linkSubstitution
            m_changedLinks.add(A_OpenCms.getLinkManager().substituteLink(m_cms, (String)exportLinks.elementAt(i)));
            // for the exported links we just add the link
        }
        m_cms.setMode(oldMode);
    }
}