/*
* File   : $Source: /alkacon/cvs/opencms/src/com/opencms/file/Attic/CmsStaticExport.java,v $
* Date   : $Date: 2001/11/15 15:56:45 $
* Version: $Revision: 1.1 $
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
package com.opencms.file;

import com.opencms.core.*;
import com.opencms.launcher.*;
import java.util.*;
import java.io.*;
import org.apache.oro.text.perl.*;

/**
 * This class holds the functionaility to export resources from the cms
 * to the filesystem.
 *
 * @author Hanjo Riege
 * @version $Revision: 1.1 $ $Date: 2001/11/15 15:56:45 $
 */
public class CmsStaticExport implements I_CmsConstants{

    /**
     * The cms-object to do the operations.
     */
    private CmsObject m_cms;

    /**
     * The resources to export.
     */
    private Vector m_startpoints;

    private Perl5Util m_perlUtil = null;

    /**
     * The export path.
     */
    private String m_exportPath;

    /**
     * This constructs a new CmsStaticExport-object which generates static html pages in the filesystem.
     *
     * @param cms the cms-object to work with.
     * @param startpoints. The resources to export (Vector of Strings)
     *
     * @exception CmsException the CmsException is thrown if something goes wrong.
     */
    public CmsStaticExport(CmsObject cms, Vector startpoints) throws CmsException{

        m_cms = cms;
        m_startpoints = startpoints;
        m_exportPath = cms.getStaticExportPath();
        m_perlUtil = new Perl5Util();

        if(I_CmsLogChannels.C_PREPROCESSOR_IS_LOGGING && A_OpenCms.isLogging()) {
            A_OpenCms.log(I_CmsLogChannels.C_OPENCMS_STATICEXPORT, "[CmsStaticExport] starting the static export.");
        }
        checkExportPath();
        Vector exportLinks = getStartLinks();
        for(int i=0; i < exportLinks.size(); i++){
            exportLink((String)exportLinks.elementAt(i), exportLinks);
        }
    }

    /**
     * Checks if the export path exists. If not it is created.
     */
    private void checkExportPath()throws CmsException{

        if(m_exportPath == null){
            throw new CmsException("[" + this.getClass().getName() + "] " + "no export folder given (null)", CmsException.C_BAD_NAME);
        }
        // we don't need the last slash
        if(m_exportPath.endsWith("/")){
            m_exportPath.substring(0, m_exportPath.length()-1);
        }
        File discFolder = new File(m_exportPath + "/");
        if (!discFolder.exists()){
            throw new CmsException("[" + this.getClass().getName() + "] " + "the export folder does not exist", CmsException.C_BAD_NAME);
        }
    }

    /**
     * exports one single link and adds sublinks to the allLinks vector.
     *
     * @param link The link to export may have html parameters
     * @param allLinks The vector with all links that have to be exported.
     */
    private void exportLink(String link, Vector allLinks) throws CmsException{

        if(I_CmsLogChannels.C_PREPROCESSOR_IS_LOGGING && A_OpenCms.isLogging()) {
            A_OpenCms.log(I_CmsLogChannels.C_OPENCMS_STATICEXPORT, "[CmsStaticExport] exporting "+link);
        }
        try{
            // first lets create our request and response objects for the export
            CmsExportRequest dReq = new CmsExportRequest();
            // test if there are parameters for the request
            int paraStart = link.indexOf("?");

            // get the name for the filesystem
            String externLink = getExternLinkName(link);

            if(paraStart >= 0){
                Hashtable parameter = javax.servlet.http.HttpUtils.parseQueryString(
                                        link.substring(paraStart +1));
                link = link.substring(0, paraStart);
                dReq.setParameters(parameter);
            }
            CmsExportResponse dRes = new CmsExportResponse();

            // now create the necesary folders
            String folder = externLink.substring(0, externLink.lastIndexOf('/'));
            File discFolder = new File(m_exportPath + folder);
            if(!discFolder.exists()){
                if(!discFolder.mkdirs()){
                    throw new CmsException("["+this.getClass().getName() + "] " +
                            "could't create all folders for "+folder+".");
                }
            }
            // all folders exist now create the file
            File discFile = new File(m_exportPath + externLink);
            try{
                // link the File to the request
                OutputStream outStream = new FileOutputStream(discFile);
                dRes.putOutputStream(outStream);
            }catch(Exception e){
                throw new CmsException("["+this.getClass().getName() + "] " + "could't open file "
                            + m_exportPath + externLink + ": " + e.getMessage());
            }

            // everthing is prepared now start the template mechanism
            CmsObject cmsForStaticExport = m_cms.getCmsObjectForStaticExport(dReq, dRes);
            cmsForStaticExport.setMode(C_MODUS_EXPORT);
            CmsFile file = m_cms.readFile(link);
            int launcherId = file.getLauncherType();
            String startTemplateClass = file.getLauncherClassname();
            I_CmsLauncher launcher = cmsForStaticExport.getLauncherManager().getLauncher(launcherId);
            if(launcher == null){
                throw new CmsException("Could not launch file " + link + ". Launcher for requested launcher ID "
                            + launcherId + " could not be found.");
            }
            ((CmsExportRequest)cmsForStaticExport.getRequestContext().getRequest()).setRequestedResource(link);
            launcher.initlaunch(cmsForStaticExport, file, startTemplateClass, null);

            // at last we need the links on the page for the further export
            Vector linksToAdd = cmsForStaticExport.getRequestContext().getLinkVector();
            for(int i=0; i<linksToAdd.size(); i++){
                if(!allLinks.contains(linksToAdd.elementAt(i))){
                    allLinks.add(linksToAdd.elementAt(i));
                }
            }

        }catch(CmsException exc){
            if(I_CmsLogChannels.C_PREPROCESSOR_IS_LOGGING && A_OpenCms.isLogging()) {
                A_OpenCms.log(I_CmsLogChannels.C_OPENCMS_STATICEXPORT, "[CmsStaticExport]  export "+link+" failed: "+exc.toString());
            }
        }catch(Exception e){
            if(I_CmsLogChannels.C_PREPROCESSOR_IS_LOGGING && A_OpenCms.isLogging()) {
                A_OpenCms.log(I_CmsLogChannels.C_OPENCMS_STATICEXPORT, "[CmsStaticExport]  export "+link+" failed: "+e.toString());
            }
        }
    }

    /**
     *
     */
    private String getExternLinkName(String link){

        String[] rules = m_cms.getLinkRules(C_MODUS_EXTERN);
        String startRule = OpenCms.getLinkRuleStart();
        if(startRule != null && !"".equals(startRule)){
            try{
                link = m_perlUtil.substitute(startRule, link);
            }catch(Exception e){
                if(I_CmsLogChannels.C_PREPROCESSOR_IS_LOGGING && A_OpenCms.isLogging() ) {
                    A_OpenCms.log(I_CmsLogChannels.C_OPENCMS_CRITICAL, "["+this.getClass().getName()+"] problems with startrule:\""+startRule+"\" (" + e + "). ");
                }
            }
        }
        if(rules == null || rules.length == 0){
            return link;
        }
        String retValue = link;
        for(int i=0; i<rules.length; i++){
            try{
                retValue = m_perlUtil.substitute(rules[i], link);
                if(!link.equals(retValue)){
                    // found the match
                    return retValue;
                }
            }catch(Exception e){
                if(I_CmsLogChannels.C_PREPROCESSOR_IS_LOGGING && A_OpenCms.isLogging() ) {
                    A_OpenCms.log(I_CmsLogChannels.C_OPENCMS_CRITICAL, "["+this.getClass().getName()+"] problems with rule:\""+rules[i]+"\" (" + e + "). ");
                }
            }
        }
        return retValue;
    }

    /**
     * returns a vector with all links in the startpoints.
     *
     * @return the links (vector of strings).
     */
    private Vector getStartLinks(){

        Vector exportLinks = new Vector();
        for(int i=0; i<m_startpoints.size(); i++){
            String cur = (String)m_startpoints.elementAt(i);
            if(cur.endsWith("/")){
                // a folder, get all files in it
                try{
                    addSubFiles(exportLinks, cur);
                }catch(CmsException e){
                    if(I_CmsLogChannels.C_PREPROCESSOR_IS_LOGGING && A_OpenCms.isLogging()) {
                        A_OpenCms.log(I_CmsLogChannels.C_OPENCMS_STATICEXPORT,
                            "[CmsStaticExport] error couldn't get all subfolder from startpoint "
                            + cur + ": " + e.toString());
                    }
                }
            }else{
                exportLinks.add(cur);
            }
        }
        return exportLinks;
    }

    /**
     * Adds all subfiles of a folder to the vector
     *
     * @param links The vector to add the filenames to.
     * @param folder The folder where the files are in.
     */
    private void addSubFiles(Vector links, String folder)throws CmsException{

        // the firstlevel files
        Vector files = m_cms.getFilesInFolder(folder);
        for(int i=0; i<files.size(); i++){
            links.add(files.elementAt(i));
        }
        Vector subFolders = m_cms.getSubFolders(folder);
        for(int i=0; i<subFolders.size(); i++){
            addSubFiles(links, (String)subFolders.elementAt(i));
        }
    }









}