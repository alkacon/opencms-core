/*
* File   : $Source: /alkacon/cvs/opencms/src/com/opencms/file/Attic/CmsStaticExport.java,v $
* Date   : $Date: 2002/01/29 15:13:32 $
* Version: $Revision: 1.14 $
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
import com.opencms.util.Utils;
import java.util.*;
import java.io.*;
import java.net.*;
import org.apache.oro.text.perl.*;

/**
 * This class holds the functionaility to export resources from the cms
 * to the filesystem.
 *
 * @author Hanjo Riege
 * @version $Revision: 1.14 $ $Date: 2002/01/29 15:13:32 $
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

    /**
     * indicates that this is called after publish project
     */
    private boolean m_afterPublish = false;

    /**
     * If called after publish project this vector contains all resouces that
     * are part of the project. We only export links if they are in the project.
     */
//    private Vector m_projectResources = null;

    private static Perl5Util c_perlUtil = null;

    /**
     * The export path.
     */
    private String m_exportPath;

    /**
     *
     */
    private String m_webAppUrl="";
    private String m_servletUrl="";

    /**
     * Value for resources with property export = false
     */
    static final String C_EXPORT_FALSE = "export value was set to false";

    /**
     * Additional rules for the export generated dynamical.
     * Used to replace ugly long foldernames with short nice
     * ones set as a property to the folder.
     * one extra version for extern
     */
    public static Vector m_dynamicExportNameRules = null;
    public static Vector m_dynamicExportNameRulesExtern = null;

    /**
     * Additional rules for the export generated dynamical.
     * Used for linking back to OpenCms for dynamic content and
     * linking out to the static stuff. Used for online and for export.
     */
    public static Vector m_dynamicExportRulesOnline = null;

    /**
     * Additional rules for the export generated dynamical.
     * Used for linking back to OpenCms for dynamic content and
     * linking out to the static stuff. Used for extern. It only
     * returns "" so the staticExport dont write something to disk.
     */
    public static Vector m_dynamicExportRulesExtern = null;

    /**
     * This constructs a new CmsStaticExport-object which generates static html pages in the filesystem.
     *
     * @param cms the cms-object to work with.
     * @param startpoints. The resources to export (Vector of Strings)
     * @param doTheExport. must be set to true to export something, otherwise only the linkrules are generated.
     * @param changedResources. Contains the changed resources belonging to the project, if started after publishProject.
     *
     * @exception CmsException the CmsException is thrown if something goes wrong.
     */
    public CmsStaticExport(CmsObject cms, Vector startpoints, boolean doTheExport,
                     Vector projectResources, CmsPublishedResources changedResources)
                     throws CmsException{
        m_cms = cms;
        m_startpoints = startpoints;
        m_exportPath = cms.getStaticExportPath();
        c_perlUtil = new Perl5Util();
        if(changedResources != null){
            m_afterPublish = true;
        }

        if(!doTheExport){
            // this is just to generate the dynamic rulesets
            if(I_CmsLogChannels.C_PREPROCESSOR_IS_LOGGING && A_OpenCms.isLogging()) {
                A_OpenCms.log(I_CmsLogChannels.C_OPENCMS_STATICEXPORT,
                                "[CmsStaticExport] Generating the dynamic rulesets.");
            }
            createDynamicRules();
        }else{
            if(I_CmsLogChannels.C_PREPROCESSOR_IS_LOGGING && A_OpenCms.isLogging()) {
                A_OpenCms.log(I_CmsLogChannels.C_OPENCMS_STATICEXPORT,
                                    "[CmsStaticExport] Starting the static export.");
            }
            m_servletUrl = cms.getRequestContext().getRequest().getServletUrl();
            m_webAppUrl = cms.getRequestContext().getRequest().getWebAppUrl();
            createDynamicRules();
            checkExportPath();
            Vector exportLinks = null;
            if(m_afterPublish){
                exportLinks = getChangedLinks(changedResources);
            }else{
                exportLinks = getStartLinks();
            }
/*            // we only need the names of the projectResources
            if(projectResources != null){
                m_projectResources = new Vector(projectResources.size());
                for(int i=0; i<projectResources.size(); i++){
                    m_projectResources.addElement(
                        ((CmsResource)projectResources.elementAt(i)).getAbsolutePath());
                }
            }
*/            if(I_CmsLogChannels.C_PREPROCESSOR_IS_LOGGING && A_OpenCms.isLogging()) {
                A_OpenCms.log(I_CmsLogChannels.C_OPENCMS_STATICEXPORT,
                        "[CmsStaticExport] got "+exportLinks.size()+" links to start with.");
            }
            for(int i=0; i < exportLinks.size(); i++){
                String aktLink = (String)exportLinks.elementAt(i);
                exportLink(aktLink, exportLinks);
            }
        }
    }

    /**
     * Checks if the link is part of the project if the export was started after
     * publish project.
     *
     * @param link The link.
     * /
    private boolean linkIsInProject(String link){
        if(m_projectResources == null){
            // we want to export all
            return true;
        }
        for(int i=0; i<m_projectResources.size(); i++){
            if(link.startsWith((String)m_projectResources.elementAt(i))){
                return true;
            }
        }
        return false;
    }

    /**
     * This method checks if this link has to be exported.
     *
     * @param link The link to be checked.
     * @return true if the link should be exported.
     */
    private boolean linkHasChanged(String link){
        CmsExportLink linkObject = null;
        try{
            // first look if this link was exported before (then it is saved in the database)
            linkObject = m_cms.readExportLink(link);
            if(linkObject == null){
                return true;
            }else{
                // lets check the dates
                Vector deps = linkObject.getDependencies();
                try{
                    for(int i=0; i<deps.size(); i++){
                        CmsResource res = m_cms.readFileHeader((String)deps.elementAt(i));
                        if(linkObject.getLastExportDate() < res.getDateLastModified()){
                            // ok one of the deps has changed since the last export
                            m_cms.deleteExportLink(linkObject);
                            return true;
                        }
                    }
                }catch(CmsException e){
                    // one of the files was deleted, we have to export it again
                    m_cms.deleteExportLink(linkObject);
                    return true;
                }
            }
        }catch(CmsException e){
            // this should not happen. Better we delete the link in the database and export it.
            try{
                m_cms.deleteExportLink(link);
            }catch(CmsException exc){
            }
            return true;
        }
        //set the processed flag to true
        if(linkObject != null){
            linkObject.setProcessedState(true);
            // write the linkObject to the database. Only the state has changed so
            // we dont need to write the whole object.
            try{
                m_cms.writeExportLinkProcessedState(linkObject);
            }catch(CmsException exp){
                linkObject.setProcessedState(false);
                return true;
            }
        }
        return false;
    }

    /**
     * Checks if the export path exists. If not it is created.
     */
    private void checkExportPath()throws CmsException{

        if(m_exportPath == null){
            if(I_CmsLogChannels.C_PREPROCESSOR_IS_LOGGING && A_OpenCms.isLogging()) {
                A_OpenCms.log(I_CmsLogChannels.C_OPENCMS_STATICEXPORT, "[CmsStaticExport] no export folder given (null).");
            }
            throw new CmsException("[" + this.getClass().getName() + "] " + "no export folder given (null)", CmsException.C_BAD_NAME);
        }
        // we don't need the last slash
        if(m_exportPath.endsWith("/")){
            m_exportPath = m_exportPath.substring(0, m_exportPath.length()-1);
        }
        File discFolder = new File(m_exportPath + "/");
        if (!discFolder.exists()){
            if(I_CmsLogChannels.C_PREPROCESSOR_IS_LOGGING && A_OpenCms.isLogging()) {
                A_OpenCms.log(I_CmsLogChannels.C_OPENCMS_STATICEXPORT, "[CmsStaticExport] the export folder does not exist.");
            }
            throw new CmsException("[" + this.getClass().getName() + "] " + "the export folder does not exist", CmsException.C_BAD_NAME);
        }
    }

    /**
     * Generates the dynamic rules for the export. It looks for all folders that
     * have the property exportname. For each folder found it generates a rule that
     * replaces the folder name (incl. path) with the exportname.
     * In addition it looks for the property dynamic. and creates rules for linking
     * between static and dynamic pages.
     * These rules are used for export and for extern links.
     */
    private void createDynamicRules(){
        // first the rules for namereplacing
        try{
            // get the resources with the property exportname
            Vector resWithProp = m_cms.getResourcesWithProperty(C_PROPERTY_EXPORTNAME);
            // generate the dynamic rules for the nice exportnames
            if(resWithProp != null && resWithProp.size() != 0){
                m_dynamicExportNameRules = new Vector();
                m_dynamicExportNameRulesExtern = new Vector();
                for(int i=0; i < resWithProp.size(); i++){
                    CmsResource resource = (CmsResource)resWithProp.elementAt(i);
                    String oldName = resource .getAbsolutePath();
                    String newName = m_cms.readProperty(oldName, C_PROPERTY_EXPORTNAME);
                    m_dynamicExportNameRules.addElement("s#^"+oldName+"#"+m_cms.getUrlPrefixArray()[0]+newName+"#");
                    m_dynamicExportNameRulesExtern.addElement("s#^"+oldName+"#"+newName+"#");
                }
            }
        }catch(CmsException e){
            if(I_CmsLogChannels.C_PREPROCESSOR_IS_LOGGING && A_OpenCms.isLogging()) {
                A_OpenCms.log(I_CmsLogChannels.C_OPENCMS_STATICEXPORT, "[CmsStaticExport] "
                    +"couldnt create dynamic export rules for the nice names. Will use the original names instead."
                    + e.toString() );
            }
        }
        // now the rules for linking between static and dynamic pages
        try{
            // get the resources with the property "dynamic"
            Vector resWithProp = m_cms.getResourcesWithProperty(C_PROPERTY_EXPORT);
            // generate the rules
            if(resWithProp != null && resWithProp.size() != 0){
                m_dynamicExportRulesExtern = new Vector();
                m_dynamicExportRulesOnline = new Vector();
                for(int i=0; i < resWithProp.size(); i++){
                    CmsResource resource = (CmsResource)resWithProp.elementAt(i);
                    String resName = resource.getAbsolutePath();
                    String propertyValue = m_cms.readProperty(resName, C_PROPERTY_EXPORT);
                    if(propertyValue != null && (propertyValue.equalsIgnoreCase("dynamic")
                                  || propertyValue.equalsIgnoreCase("https")
                                  || propertyValue.equalsIgnoreCase("false"))){
                        // first we can create the dynamic rule for extern (it is the same for true and https)
                        if(propertyValue.equalsIgnoreCase("false")){
                            m_dynamicExportRulesExtern.addElement("s#^"+resName+".*#export value was set to false#");
                        }else{
                            m_dynamicExportRulesExtern.addElement("s#^"+resName+".*##");
                        }
                        if(propertyValue.equalsIgnoreCase("dynamic") || propertyValue.equalsIgnoreCase("false")){
                            // create the rules for dynamic pages
                            m_dynamicExportRulesOnline.addElement("s#^("+resName+")#"+ m_cms.getUrlPrefixArray()[1] +"$1#");
                        }else{
                            // create the rules for shtml dynamic pages
                            m_dynamicExportRulesOnline.addElement("s#^("+resName+")#"+ m_cms.getUrlPrefixArray()[2] +"$1#");
                        }
                    }
                }
            }
        }catch(CmsException e){
            if(I_CmsLogChannels.C_PREPROCESSOR_IS_LOGGING && A_OpenCms.isLogging()) {
                A_OpenCms.log(I_CmsLogChannels.C_OPENCMS_STATICEXPORT, "[CmsStaticExport] "
                    +"couldnt create dynamic export rules. " + e.toString() );
            }
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
        String deleteFileOnError = null;
        OutputStream outStream = null;
        CmsExportLink dbLink = new CmsExportLink(link, System.currentTimeMillis(), null);

        try{
            // first lets create our request and response objects for the export
            CmsExportRequest dReq = new CmsExportRequest(m_webAppUrl, m_servletUrl);
            // test if there are parameters for the request
            int paraStart = link.indexOf("?");

            // get the name for the filesystem
            String externLink = getExternLinkName(link);
            boolean writeFile = true;
            if(externLink == null || externLink.equals("")){
                writeFile = false;
            }
            if(C_EXPORT_FALSE.equals(externLink)){
                // this should not be exported and has no links on it that must be exported
                return;
            }
            if(paraStart >= 0){
                Hashtable parameter = javax.servlet.http.HttpUtils.parseQueryString(
                                        link.substring(paraStart +1));
                link = link.substring(0, paraStart);
                dReq.setParameters(parameter);
            }
            CmsExportResponse dRes = new CmsExportResponse();

            if(writeFile){
                // now create the necesary folders
                String folder = "";
                int folderIndex = externLink.lastIndexOf('/');
                if(folderIndex != -1){
                    folder = externLink.substring(0, externLink.lastIndexOf('/'));
                }
                String correctur = "";
                if(!externLink.startsWith("/")){
                    correctur = "/";
                    // this is only for old versions where the editor may have added linktags containing
                    // extern links. Such a link can not be exported and we dont want to create strange
                    // folders like '"http:'
                    boolean linkIsExtern = true;
                    try{
                        URL test = new URL(externLink);
                    }catch(MalformedURLException e){
                        linkIsExtern = false;
                    }
                    if(linkIsExtern){
                        throw new CmsException(" This is a extern link.");
                    }
                }
                File discFolder = new File(m_exportPath + correctur + folder);
                if(!discFolder.exists()){
                    if(!discFolder.mkdirs()){
                        throw new CmsException("["+this.getClass().getName() + "] " +
                                "could't create all folders for "+folder+".");
                    }
                }
                // all folders exist now create the file
                File discFile = new File(m_exportPath + correctur + externLink);
                deleteFileOnError = m_exportPath + correctur + externLink;
                try{
                    // link the File to the request
                    outStream = new FileOutputStream(discFile);
                    dRes.putOutputStream(outStream);
                }catch(Exception e){
                    throw new CmsException("["+this.getClass().getName() + "] " + "could't open file "
                                + m_exportPath + correctur + externLink + ": " + e.getMessage());
                }
            }else{
                // we dont want to write this file but we have to generate it to get links in it.
                dRes.putOutputStream(new ByteArrayOutputStream());
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
            cmsForStaticExport.getRequestContext().addDependency(file.getResourceName());
            launcher.initlaunch(cmsForStaticExport, file, startTemplateClass, null);

            // we need the links on the page for the further export
            Vector linksToAdd = cmsForStaticExport.getRequestContext().getLinkVector();
            for(int i=0; i<linksToAdd.size(); i++){
                if(!allLinks.contains(linksToAdd.elementAt(i))){
                    if(!m_afterPublish
                        || (m_cms.readExportLinkHeader((String)linksToAdd.elementAt(i)) == null)){
                            // after publish we only add this link if it is was
                            // not exported befor.
                            allLinks.add(linksToAdd.elementAt(i));
                    }
                }
            }
            // now get the dependencies and write the link to the database
            Vector depsToAdd = cmsForStaticExport.getRequestContext().getDependencies();
            for(int i=0; i<depsToAdd.size(); i++){
                dbLink.addDependency((String)depsToAdd.elementAt(i));
            }
            try{
                dbLink.setProcessedState(true);
                m_cms.writeExportLink(dbLink);
            }catch(CmsException e){
                if(I_CmsLogChannels.C_PREPROCESSOR_IS_LOGGING && A_OpenCms.isLogging()) {
                    A_OpenCms.log(I_CmsLogChannels.C_OPENCMS_STATICEXPORT, "[CmsStaticExport] write ExportLink "+dbLink.getLink()+" failed: "+e.toString());
                }
            }

            // at last close the outputstream
            if(outStream != null){
                outStream.close();
            }
        }catch(CmsException exc){
            if(I_CmsLogChannels.C_PREPROCESSOR_IS_LOGGING && A_OpenCms.isLogging()) {
                A_OpenCms.log(I_CmsLogChannels.C_OPENCMS_STATICEXPORT, "[CmsStaticExport] "+deleteFileOnError+" export "+link+" failed: "+exc.toString());
            }
            if(deleteFileOnError != null){
                try{
                    File deleteMe = new File(deleteFileOnError);
                    if(deleteMe.exists() ){//&& deleteMe.length() == 0){
                        if(outStream != null){
                            outStream.close();
                        }
                        deleteMe.delete();
                        m_cms.deleteExportLink(link);
                    }
                }catch(Exception e){
                }
            }
        }catch(Exception e){
            if(I_CmsLogChannels.C_PREPROCESSOR_IS_LOGGING && A_OpenCms.isLogging()) {
                A_OpenCms.log(I_CmsLogChannels.C_OPENCMS_STATICEXPORT, "[CmsStaticExport]  export "+link+" failed : "+Utils.getStackTrace(e));
            }
        }
    }

    /**
     * Returns the name used for the disc.
     */
    private String getExternLinkName(String link){

        String[] rules = m_cms.getLinkRules(C_MODUS_EXTERN);
        String startRule = OpenCms.getLinkRuleStart();
        if(startRule != null && !"".equals(startRule)){
            try{
                link = c_perlUtil.substitute(startRule, link);
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
                if("*dynamicRules*".equals(rules[i])){
                    // here we go trough our dynamic rules
                    Vector booleanReplace = new Vector();
                    retValue = handleDynamicRules(m_cms, link, C_MODUS_EXTERN, booleanReplace);
                    Boolean goOn =(Boolean)booleanReplace.firstElement();
                    if(goOn.booleanValue()){
                        link = retValue;
                    }else{
                        // found the match
                        return retValue;
                    }
                }else{
                    retValue = c_perlUtil.substitute(rules[i], link);
                    if(!link.equals(retValue)){
                        // found the match
                        return retValue;
                    }
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
     * returns a vector with all links that must be new exported .
     *
     * @return the links (vector of strings).
     */
    private Vector getChangedLinks(CmsPublishedResources changedResources) throws CmsException{

        Vector resToCheck = new Vector();
        if(changedResources == null){
            return new Vector();
        }
        Vector addVector = changedResources.getChangedModuleMasters();
        if(addVector != null){
            for(int i=0; i<addVector.size(); i++){
                resToCheck.add(addVector.elementAt(i));
            }
        }
        addVector = changedResources.getChangedResources();
        if(addVector != null){
            for(int i=0; i<addVector.size(); i++){
                resToCheck.add(addVector.elementAt(i));
            }
        }
        Vector returnValue = m_cms.getDependingExportLinks(resToCheck);
        // we also need all "new" files
        if(addVector != null){
            for(int i=0; i<addVector.size(); i++){
                String res = Utils.getAbsolutePathForResource((String)addVector.elementAt(i));
                if((!res.endsWith("/")) && (!returnValue.contains(res))){
                    returnValue.add(res);
                }
            }
        }
        return returnValue;
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
            links.add(((CmsFile)files.elementAt(i)).getAbsolutePath());
        }
        Vector subFolders = m_cms.getSubFolders(folder);
        for(int i=0; i<subFolders.size(); i++){
            addSubFiles(links, ((CmsFolder)subFolders.elementAt(i)).getAbsolutePath());
        }
    }

    /**
     * this method handles the dynamic rules created by opencms
     * in using the properties of resources.
     *
     * @param cms. The cms-object. used for the parameter replace.
     * @param link The link that has to be replaced.
     * @param modus The modus OpenCms runs in.
     * @param parameterOnly is set to true if only the parameters are replaced
     *          and no other rules
     */
    public static String handleDynamicRules(CmsObject cms, String link, int modus, Vector paramterOnly){

        // first get the ruleset
        Vector dynRules = null;
        Vector nameRules = null;
        if(modus == C_MODUS_EXTERN){
            dynRules = m_dynamicExportRulesExtern;
            nameRules = m_dynamicExportNameRulesExtern;
        }else{
            dynRules = m_dynamicExportRulesOnline;
            nameRules = m_dynamicExportNameRules;
        }
        String retValue = link;
        if(dynRules != null){
            for(int i=0; i<dynRules.size(); i++){
                retValue = c_perlUtil.substitute((String)dynRules.elementAt(i), link);
                if(!retValue.equals(link)) {
                    paramterOnly.add(new Boolean(false));
                    return retValue;
                }
            }
        }
        // here we can start the parameters
        link = substituteLinkParameter(cms, link);
        retValue = link;

        // now for the name replacement rules
        if(nameRules != null){
            for(int i=0; i<nameRules.size(); i++){
                retValue = c_perlUtil.substitute((String)nameRules.elementAt(i), link);
                if(!retValue.equals(link)){
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
     * If a link has htmlparameter this methode removes them and adds an index
     * to the link. This index is the linkId in the database where all exported
     * links are saved. i.e. /test/index.html?para1=1&para2=2&para3=3 to
     * /test/index_32.html (when 32 is the databaseId of this link).
     * If the link is not in the database this methode saves it to create the id.
     *
     * @param link The Link to process.
     *
     * @return The processed link.
     */
    private static String substituteLinkParameter(CmsObject cms, String link){
        // has it parameters?
        int paraStartPos = link.indexOf('?');
        if(paraStartPos < 0){
            // no parameter - no service
            return link;
        }
        // cut the parameters
        String returnValue = link.substring(0,paraStartPos);
        try{
            // get the id from the database
            CmsExportLink linkObject = cms.readExportLinkHeader(link);
            if(linkObject == null){
                // it was not written before, so we have to do it here.
                linkObject = new CmsExportLink(link, 0, null);
                cms.writeExportLink(linkObject);
            }
            int id = linkObject.getId();
            // now we put this id at the right place in the link
            int pointId = returnValue.lastIndexOf('.');
            if(pointId < 0){
                return returnValue + "_"+id;
            }
            returnValue = returnValue.substring(0, pointId)+"_"+id+returnValue.substring(pointId);

        }catch(CmsException e){
            if(I_CmsLogChannels.C_PREPROCESSOR_IS_LOGGING && A_OpenCms.isLogging()) {
                A_OpenCms.log(I_CmsLogChannels.C_OPENCMS_STATICEXPORT, "[CmsStaticExport] cant substitute the htmlparameter for link: "+link+"  "+e.toString());
            }
            return link;
        }
        return returnValue;
    }
}