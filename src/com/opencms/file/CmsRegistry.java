/*
* File   : $Source: /alkacon/cvs/opencms/src/com/opencms/file/Attic/CmsRegistry.java,v $
* Date   : $Date: 2003/03/25 00:14:35 $
* Version: $Revision: 1.72 $
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

import com.opencms.core.A_OpenCms;
import com.opencms.core.CmsException;
import com.opencms.core.I_CmsConstants;
import com.opencms.report.CmsShellReport;
import com.opencms.report.I_CmsReport;
import com.opencms.template.A_CmsXmlContent;
import com.opencms.workplace.I_CmsWpConstants;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Method;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.Vector;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Implements the registry for OpenCms.<p>
 * 
 * The OpenCms registry contains information about the installed modules in the system,
 * as well as the launcher mappings, the mail server settings for the task management,
 * the workplace views and some other items.<p>
 *
 * @author Andreas Schouten
 * @author Thomas Weckert (t.weckert@alkacon.com)
 * @author Alexander Kandzior (a.kandzior@alkacon.com)
 * 
 * @version $Revision: 1.72 $ $Date: 2003/03/25 00:14:35 $
 */
public class CmsRegistry extends A_CmsXmlContent implements I_CmsRegistry, I_CmsConstants, I_CmsWpConstants {

    /**
     *  The xml-document representing the registry.
     */
    private Document m_xmlReg;

    /**
     *  The filename for the registry.
     */
    private String m_regFileName;

    /**
     *  A hashtable with shortcuts into the dom-structure for each module.
     */
    private Hashtable m_modules = new Hashtable();

    /**
     *  A hashtable with all exportpoints and paths.
     */
    private Hashtable m_exportpoints = new Hashtable();

    /**
     *  The cms-object to get access to the system with the context of the current user.
     */
    private CmsObject m_cms = null;

    /**
     *  The date-format to use.
     */
    private SimpleDateFormat m_dateFormat = new java.text.SimpleDateFormat("MM.dd.yyyy");

    /**
     *  A message digest to check the resource-codes
     */
    private MessageDigest m_digest;

    // Module event-method names.
    private static final String C_UPLOAD_EVENT_METHOD_NAME = "moduleWasUploaded";
    private static final String C_UPDATE_PARAMETER_EVENT_METHOD_NAME = "moduleParameterWasUpdated";
    private static final String C_DELETE_EVENT_METHOD_NAME = "moduleWasDeleted";

    /**
     * Declaration of an empty module in the registry.
     */
    private static final String[] C_EMPTY_MODULE = { 
        "<module><type>", 
        "</type><name>", 
        "</name><nicename>", 
        "</nicename><version>", 
        "</version><description><![CDATA[ ", 
        "]]></description><author>", 
        "</author><email/><creationdate>", 
        "</creationdate>",
        "<view/><publishclass/><documentation/><dependencies/><maintenance_class/><parameters/><repository/></module>" 
    };

    /** XML to create an export point */
    private static final String[] C_EXPORTPOINT = { "<exportpoint><source>", "</source><destination>", "</destination></exportpoint>"};
    
    /** Debug flag, set to 9 for maximum vebosity */
    private static final int DEBUG = 0;

    /**
     * Creates a new CmsRegistry for a user. The cms-object represents 
     * the current state of the current user.<p>
     *
     * @param CmsObject the cms-object to get access to the system
     */
    public CmsRegistry(CmsRegistry reg, CmsObject cms) {
        super();
        // there is no need of a real copy for this parameters
        m_modules = reg.m_modules;
        m_exportpoints = reg.m_exportpoints;
        m_regFileName = reg.m_regFileName;
        m_xmlReg = reg.m_xmlReg;
        // store the cms-object for this instance.
        m_cms = cms;
        try {
            m_digest = MessageDigest.getInstance(CmsImport.C_IMPORT_DIGEST);
        } catch (NoSuchAlgorithmException e) {
            m_digest = null;
        }
    }
    
    /**
     * Creates a new CmsRegistry. The regFileName is the path to the registry-file in
     * the server filesystem.
     *
     *  @param String regFileName the path to the registry-file in the server fs.
     */
    public CmsRegistry(String regFileName) throws CmsException {
        super();
        try {
            // store the filename
            m_regFileName = regFileName;

            // get the file
            File xmlFile = new File(m_regFileName);

            // parse the registry-xmlfile and store it.
            InputStream content = new FileInputStream(xmlFile);
            m_xmlReg = parse(content);
            init(true);
        } catch (Exception exc) {
            throw new CmsException("couldn't init registry", CmsException.C_REGISTRY_ERROR, exc);
        }
    }

    /**
     * Checks if the dependencies are fullfilled.<p>
     * 
     * @param module the dom-element describing the new module
     * @param replaceMode if <code>true</code> this is for module replacement, 
     * if <code>false</code> it is form module deletion
     * @return a Vector of conflict description Strings, if this is an empty vector, 
     * there are no conficts (i.e. the dependencies are fullfilled)
     */
    private Vector checkDependencies(Element module, boolean replaceMode) throws CmsException {
        
        float newVersion = -1;
        String versionString = module.getElementsByTagName("version").item(0).getFirstChild().getNodeValue();
        try {
            newVersion = Float.parseFloat(versionString);
        } catch (NumberFormatException e) {}

        Vector retValue = new Vector();
        
        if (replaceMode) { 
            // replace mode, just ensure new version number is larger then the old number
            // TODO: check dependencies of all other installed modules for "maxversion"
            String name = module.getElementsByTagName("name").item(0).getFirstChild().getNodeValue();
            // get the version of the module to replace
            float currentVersion = getModuleVersion(name);  
            if (currentVersion > newVersion) {
                retValue.addElement("For module replacement, the new version (" + newVersion + ") must be higher or equal to the current version " + currentVersion);
            }                     
        } else {
            // not replace mode, check if the listed dependencies are o.k.
            try {
                Element dependencies = (Element) (module.getElementsByTagName("dependencies").item(0));
                NodeList deps = dependencies.getElementsByTagName("dependency");
                for (int i = 0; i < deps.getLength(); i++) {
                    String name = ((Element)deps.item(i)).getElementsByTagName("name").item(0).getFirstChild().getNodeValue();
                    float minVersion =
                        Float.parseFloat(
                            ((Element)deps.item(i)).getElementsByTagName("minversion").item(0).getFirstChild().getNodeValue());
                    float maxVersion =
                        Float.parseFloat(
                            ((Element)deps.item(i)).getElementsByTagName("maxversion").item(0).getFirstChild().getNodeValue());
        
                    // get the version of the needed repository
                    float currentVersion = getModuleVersion(name);
        
                    if (currentVersion == -1) {
                        retValue.addElement("The required module " + name + " doesn't exist");
                    } else if (currentVersion < minVersion) {
                        retValue.addElement("Module " + name + " version " + minVersion + " is not high enough");
                    } else if ((maxVersion != C_ANY_VERSION) && (currentVersion > maxVersion)) {
                        retValue.addElement("Module " + name + " version " + maxVersion + " is to high");
                    }
                }
            } catch (Exception exc) {
                throw new CmsException("Could not check the dependencies", CmsException.C_REGISTRY_ERROR, exc);
            }
        }

        return retValue;
    }
    
    /**
     *  Checks if the type of the value is correct.
     *  @param type the type that the value should have..
     *  @param value the value to check.
     */
    private boolean checkType(String type, String value) {
        type = type.toLowerCase();
        try {
            if("string".equals(type) ) {
                if( value != null) {
                    return true;
                } else {
                    return false;
                }
            } else if("int".equals(type) || "integer".equals(type)) {
                Integer.parseInt(value);
                return true;
            } else if("float".equals(type)) {
                Float.valueOf(value);
                return true;
            } else if("boolean".equals(type)) {
                Boolean.valueOf(value);
                return true;
            } else if("long".equals(type)) {
                Long.valueOf(value);
                return true;
            } else if("double".equals(type)) {
                Double.valueOf(value);
                return true;
            } else if("byte".equals(type)) {
                Byte.valueOf(value);
                return true;
            } else {
                // the type dosen't exist
                return false;
            }
        } catch(Exception exc) {
            // the type of the value was wrong
            return false;
        }
    }
    
    /**
     * Clones the registry.<p>
     *
     * @param CmsObject the current cms-object to get access to the system
     * @return the cloned registry
     */
    public I_CmsRegistry clone(CmsObject cms) {
        return new CmsRegistry(this, cms);
    }

    /**
     * This method creates a new module in the repository.
     *
     * @param modulename the name of the module
     * @param niceModulename another name of the module
     * @param description the description of the module
     * @param author the name of the author
     * @param type the type of the module
     * @param exportPoints a map of all export points of the module 
     * @param createDate the creation date of the module
     * @param version the version number of the module
     * @throws CmsException if the user has no right to create a new module
     */
    public void createModule(String modulename, String niceModulename, String description, String author, String type, Map exportPoints, long createDate, float version) throws CmsException {
            createModule(modulename, niceModulename, description, author, type, exportPoints, m_dateFormat.format(new Date(createDate)), version);
    }
    
    /**
     * This method creates a new module in the repository.
     *
     * @param modulename the name of the module
     * @param niceModulename another name of the module
     * @param description the description of the module
     * @param author the name of the author
     * @param type the type of the module
     * @param exportPoints a map of all export points of the module 
     * @param createDate the creation date of the module in the format: mm.dd.yyyy
     * @param version the version number of the module
     * @throws CmsException if the user has no right to create a new module
     */
    public void createModule(
        String modulename,
        String niceModulename,
        String description,
        String author,
        String type,
        Map exportPoints,
        String createDate,
        float version)
        throws CmsException {

        // find out if the module exists already
        if (moduleExists(modulename)) {
            throw new CmsException("Module exists already " + modulename, CmsException.C_REGISTRY_ERROR);
        }

        // check if the user is allowed to perform this action
        if (!hasAccess()) {
            throw new CmsException("No access to perform the action 'createModule'", CmsException.C_REGISTRY_ERROR);
        };

        // create the new module in the registry
        StringBuffer moduleString = new StringBuffer();

        moduleString.append(C_EMPTY_MODULE[0] + type);
        moduleString.append(C_EMPTY_MODULE[1] + modulename);
        moduleString.append(C_EMPTY_MODULE[2] + niceModulename);
        moduleString.append(C_EMPTY_MODULE[3] + version);
        moduleString.append(C_EMPTY_MODULE[4] + description);
        moduleString.append(C_EMPTY_MODULE[5] + author);
        moduleString.append(C_EMPTY_MODULE[6] + createDate);
        moduleString.append(C_EMPTY_MODULE[7]);

        Iterator i = exportPoints.keySet().iterator();
        while (i.hasNext()) {
            String key = (String)i.next();
            String value = (String)exportPoints.get(key);
            moduleString.append(C_EXPORTPOINT[0]);
            moduleString.append(key);
            moduleString.append(C_EXPORTPOINT[1]);
            moduleString.append(value);
            moduleString.append(C_EXPORTPOINT[2]);
        }
        moduleString.append(C_EMPTY_MODULE[8]);

        // encoding project:
        Document doc;
        try {
            doc = parse(moduleString.toString().getBytes(A_OpenCms.getDefaultEncoding()));
        } catch (UnsupportedEncodingException uee) {
            // use default system encoding
            doc = parse(moduleString.toString().getBytes());
        }
        m_xmlReg.getElementsByTagName("modules").item(0).appendChild(
            getXmlParser().importNode(m_xmlReg, doc.getFirstChild()));
        saveRegistry();
    }

    /**
     * @see com.opencms.file.I_CmsRegistry#deleteCheckDependencies(String, boolean)
     */
    public Vector deleteCheckDependencies(String modulename, boolean replaceMode) throws CmsException {

        Vector result = new Vector();
        if (replaceMode) return result;
        
        Enumeration names = getModuleNames();
        Vector modules;
        Vector minVersions;
        Vector maxVersions;
        while (names.hasMoreElements()) {
            String name = (String)names.nextElement();
            modules = new Vector();
            minVersions = new Vector();
            maxVersions = new Vector();
            getModuleDependencies(name, modules, minVersions, maxVersions);
            // needs this module the module to test?
            if (modules.contains(modulename)) {
                // yes - store it in the result
                result.addElement(name);
            }
        }
        return result;
    }

    /**
     * This method checks for conflicting files before the deletion of a module.
     * It uses several Vectors to return the different conflicting files.
     *
     * @param modulename the name of the module that should be deleted.
     * @param filesWithProperty a return value. The files that are marked with the module-property for this module.
     * @param missingFiles a return value. The files that are missing.
     * @param wrongChecksum a return value. The files that should be deleted but have another checksum as at import-time.
     * @param filesInUse a return value. The files that should be deleted but are in use by other modules.
     * @param resourcesForProject a return value. The files that should be copied to a project to delete.
     */
    public void deleteGetConflictingFileNames(String modulename, Vector filesWithProperty, Vector missingFiles, Vector wrongChecksum, Vector filesInUse, Vector resourcesForProject) throws CmsException {

        // Module type SIMPLE? Just do nothing here, as SIMPLE modules do not support file conflicts
        if (this.getModuleType(modulename).equals(CmsRegistry.C_MODULE_TYPE_SIMPLE)) return;

        // the files and checksums for this module
        Vector moduleFiles = new Vector();
        Vector moduleChecksums = new Vector();
        // the files and checksums for all other modules
        Vector otherFiles = new Vector();
        Vector otherChecksums = new Vector();        
        
        getModuleFiles(modulename, moduleFiles, moduleChecksums);
    
        Enumeration modules = getModuleNames();
        while (modules.hasMoreElements()) {
            String module = (String) modules.nextElement();
            // get the files only for modules that are not for the current module.
            if (!module.equals(modulename)) {
                // get the files
                getModuleFiles(module, otherFiles, otherChecksums);
            }
        }
        for (int i = 0; i < moduleFiles.size(); i++) {
            // get the current file and checksum
            String currentFile = (String) moduleFiles.elementAt(i);
            String currentChecksum = (String) moduleChecksums.elementAt(i);
            CmsFile file = null;
    
            try {
                String resource = currentFile.substring(0, currentFile.indexOf("/",1) + 1);
                if(!resourcesForProject.contains(resource)) {
                    // add the resource, if it dosen't already exist
                    resourcesForProject.addElement(resource);
                }
            } catch(StringIndexOutOfBoundsException exc) {
                // this is a resource in root-folder: ignore the excpetion
            }
    
            // is it a file - then check all the possibilities
            if (!currentFile.endsWith("/")) {
                // exists the file in the cms?
                try {
                    file = m_cms.readFile(currentFile);
                } catch (CmsException exc) {
                    // the file dosen't exist - mark it as deleted
                    missingFiles.addElement(currentFile);
                }
    
                // is the file in use of another module?
                if (otherFiles.contains(currentFile)) {
                    // yes - mark it as in use
                    filesInUse.addElement(currentFile);
                }
    
                // was the file changed?
                if (file != null) {
                    // create the current digest-content for the file
                    // encoding project:
                    String digestContent;
                    try {
                        digestContent =
                            com.opencms.util.Encoder.escape(
                                new String(
                                    m_digest.digest(file.getContents()),
                                    m_cms.getRequestContext().getEncoding()),
                                m_cms.getRequestContext().getEncoding());
                    } catch (UnsupportedEncodingException e) {
                        digestContent =
                            com.opencms.util.Encoder.escape(
                                new String(
                                    m_digest.digest(file.getContents())),
                                m_cms.getRequestContext().getEncoding());
                    }
                    if (!currentChecksum.equals(digestContent)) {
                        // the file was changed, the checksums are different
                        wrongChecksum.addElement(currentFile);
                    }
                }
            }
        }
        
        Vector files = m_cms.getFilesWithProperty("module", modulename + "_" + getModuleVersion(modulename));        
        int fileCount = files.size();
        
        for(int i=0;i<fileCount;i++) {
            String currentFile = (String)files.elementAt(i);

            if(!moduleFiles.contains(currentFile)) {
                // is the file in use of another module?
                if (!otherFiles.contains(currentFile)) {
                filesWithProperty.addElement(currentFile);

                    try {
                        String resource = currentFile.substring(0, currentFile.indexOf("/",1) + 1);

                        if(!resourcesForProject.contains(resource)) {
                            // add the resource, if it dosen't already exist
                            resourcesForProject.addElement(resource);
                        }
                    } 
                    catch(StringIndexOutOfBoundsException exc) {
                        // this is a resource in root-folder: ignore the excpetion
                    }
                }
            }
        }           
    }
    
    /**
     * @see com.opencms.file.I_CmsRegistry#deleteModule(String, Vector, boolean, I_CmsReport)
     */
    public synchronized void deleteModule(String module, Vector exclusion, boolean replaceMode, I_CmsReport report) throws CmsException {
    
        if (DEBUG > 2) System.err.println("[" + this.getClass().getName() + ".deleteModule()] Starting to delete module " + module);
    
        // check if the module exists
        if (!moduleExists(module)) {
            throw new CmsException("Module '"+module+"' does not exist", CmsException.C_REGISTRY_ERROR);
        }
    
        // check if the user is allowed to perform this action
        if (!hasAccess()) {
            throw new CmsException("No access to perform the action 'deleteModule'", CmsException.C_REGISTRY_ERROR);
        }
    
        // check, if deletion is allowed
        Vector deps = deleteCheckDependencies(module, replaceMode);
        if (deps.size() != 0) {
            // there are dependencies - throw exception
            throw new CmsException(
                "There are dependencies for the module " + module + ": deletion is not allowed.",
                CmsException.C_REGISTRY_ERROR);
        }

        // try to invoke the event-method for delete on this calss.
        Class eventClass = getModuleMaintenanceEventClass(module);
    
        try {
            Class declaration[] = {CmsObject.class};
            Object arguments[] = {m_cms};
            Method eventMethod = eventClass.getMethod(C_DELETE_EVENT_METHOD_NAME, declaration);
            eventMethod.invoke(null, arguments);
        } catch(Exception exc) {
            // ignore the exception.
        }
    
        if (this.getModuleType(module).equals(CmsRegistry.C_MODULE_TYPE_SIMPLE)) {
            // SIMPLE module: Just delete all the folders of the module
                
            // check if additional resources outside the system/modules/{exportName} folder were 
            // specified as module resources by reading the module property {C_MODULE_PROPERTY_ADDITIONAL_RESOURCES}
            // just delete these resources plus the "standard" module paths under system/modules
                    
            String additionalResources = this.getModuleParameterString( module, I_CmsConstants.C_MODULE_PROPERTY_ADDITIONAL_RESOURCES );
            Vector resources = new Vector();
                                
            if (additionalResources!=null && !additionalResources.equals("")) {                            
                // add each additonal folder plus its content folder under "content/bodys"
                StringTokenizer additionalResourceTokens = null;
                additionalResourceTokens = new StringTokenizer( additionalResources, I_CmsConstants.C_MODULE_PROPERTY_ADDITIONAL_RESOURCES_SEPARATOR ); 
                                       
                while (additionalResourceTokens.hasMoreTokens()) {
                    String currentResource = additionalResourceTokens.nextToken().trim();
                    
                    if (DEBUG>0) {
                        System.err.println( "Adding resource: " + currentResource );
                    }
                                            
                    resources.add( currentResource );
                }                       
            }          
            
            resources.add(I_CmsWpConstants.C_VFS_PATH_MODULES + module + "/");
            // move through all resource-names and try to delete them
            for (int i = resources.size() - 1; i >= 0; i--) {
                String currentResource = null;
                try {
                    currentResource = (String)resources.elementAt(i);
                    if (DEBUG > 1) System.err.println("[" + this.getClass().getName() + ".deleteModule()] Deleting resource " + currentResource);                
                    // lock the resource
                    m_cms.lockResource(currentResource, true);
                     // delete the resource
                    m_cms.deleteResource(currentResource);
                    // update the report
                    report.print(report.key("report.deleting"), I_CmsReport.C_FORMAT_NOTE);
                    report.println(currentResource);
                } catch (CmsException exc) {
                    // ignore the exception and delete the next resource
                    if (DEBUG > 0) System.err.println("[" + this.getClass().getName() + ".deleteModule()] Exception " + exc + " deleting resource " + currentResource);
                    report.println(exc);
                }
            }              
     
        } else {
            // TRADITIONAL module: Check file dependencies
    
            // get the files, that are belonging to the module.
            Vector resourceNames = new Vector();
            Vector missingFiles = new Vector();
            Vector wrongChecksum = new Vector();
            Vector filesInUse = new Vector();
            Vector resourceCodes = new Vector();
        
            // get files by property

			deleteGetConflictingFileNames(module, resourceNames, missingFiles, wrongChecksum, filesInUse, new Vector());

			// get files by registry
			getModuleFiles(module, resourceNames, resourceCodes);

			// move through all resource-names and try to delete them
			for (int i = resourceNames.size() - 1; i >= 0; i--) {
				try {
					String currentResource = (String) resourceNames.elementAt(i);
					if ((!exclusion.contains(currentResource)) && (!filesInUse.contains(currentResource))) {
						m_cms.lockResource(currentResource, true);
						if (currentResource.endsWith("/")) {
							// this is a folder
							m_cms.deleteEmptyFolder(currentResource);
						} else {
							// this is a file
							m_cms.deleteResource(currentResource);
						}
						// update the report
						report.print(report.key("report.deleting"), I_CmsReport.C_FORMAT_NOTE);
						report.println(currentResource);
					}
				} catch (CmsException exc) {
					// ignore the exception and delete the next resource.
					report.println(exc);
				}
			}
        }
    
        // delete all entries for the module in the registry
        Element moduleElement = getModuleElement(module);
        moduleElement.getParentNode().removeChild(moduleElement);
        saveRegistry();
    
        try {
            init(false);
        } catch (Exception exc) {
            throw new CmsException("couldn't init registry", CmsException.C_REGISTRY_ERROR, exc);
        }
        
        if (DEBUG > 2) System.err.println("[" + this.getClass().getName() + ".deleteModule()] Finished for module " + module);    
    }
    
    /**
     * Deletes the view for a module.
     *
     * @param String the name of the module.
     */
    public void deleteModuleView(String modulename) throws CmsException {
        // check if the user is allowed to perform this action
        if (!hasAccess()) {
            throw new CmsException("No access to perform the action 'deleteModuleView'", CmsException.C_REGISTRY_ERROR);
        }
        try {
            Element module = getModuleElement(modulename);
            Element view = (Element) (module.getElementsByTagName("view").item(0));
    
            // delete all subnodes
            while(view.hasChildNodes()) {
                view.removeChild(view.getFirstChild());
            }
            saveRegistry();
        } catch (Exception exc) {
            // ignore the exception - reg is not welformed
        }
    }

    /**
     * This method exports a module to the filesystem.<p>
     *
     * @param moduleName the name of the module to be exported
     * @param String[] an array of resources to be exported
     * @param fileName the name of the file to write the export to
     * @param report a report for the output 
     * 
     * @throws CmsException in case of an error during export
     */
    public void exportModule(String moduleName, String[] resources, String fileName, I_CmsReport report) throws CmsException {
        // check if the user is allowed to import a module.
        if (!hasAccess()) {
            throw new CmsException("No access to perform the action 'exportModule'", CmsException.C_REGISTRY_ERROR);
        }
        // remove all "uploaddate" and "uploadby" nodes
        Element module = getModuleElement(moduleName);
        Element moduleCopy = (Element)module.cloneNode(true);
        NodeList list = moduleCopy.getChildNodes();
        for (int i=(list.getLength()-1); i>=0; i--) {
            Element e = (Element)list.item(i);
            if ("uploaddate".equals(e.getNodeName()) || "uploadedby".equals(e.getNodeName())) {
                moduleCopy.removeChild(e);
            }
        }                      
        // export the module using the standard export        
        new CmsExport(m_cms, fileName, resources, false, false, moduleCopy, false, 0, report);
    }
    
    /**
     * Gets a description of this content type.
     * For OpenCms internal use only.<p>
     * 
     * @return Content type description.
     */
    public String getContentDescription() {
        return "Registry";
    }
    
    /**
     * This method returns the author of the module.
     *
     * @param String the name of the module.
     * @return java.lang.String the author of the module.
     */
    public String getModuleAuthor(String modulename) {
        return getModuleData(modulename, "author");
    }

    /**
     * This method returns the email of author of the module.
     *
     * @param String the name of the module.
     * @return java.lang.String the email of author of the module.
     */
    public String getModuleAuthorEmail(String modulename) {
        return getModuleData(modulename, "email");
    }

    /**
     * Gets the create date of the module.
     *
     * @param String the name of the module.
     * @return long the create date of the module.
     */
    public long getModuleCreateDate(String modulname) {
        long retValue = -1;
        try {
            String value = getModuleData(modulname, "creationdate");
            retValue = m_dateFormat.parse(value).getTime();
        } catch (Exception exc) {
            // ignore the exception - reg is not welformed
        }
        return retValue;
    }

    /**
     *  Private method to return module data like author.
     *
     * @param String modulename the name of the module.
     * @param String dataName the name of the tag to get the data from.
     * @return String the value for the requested data.
     */
    private String getModuleData(String module, String dataName) {
        String retValue = null;
        try {
            Element moduleElement = getModuleElement(module);
            retValue = moduleElement.getElementsByTagName(dataName).item(0).getFirstChild().getNodeValue();
        } catch (Exception exc) {
            // ignore the exception - registry is not wellformed
        }
        return retValue;
    }

    /**
     * Returns the module dependencies for the module.
     *
     * @param module String the name of the module to check.
     * @param modules Vector in this parameter the names of the dependend modules will be returned.
     * @param minVersions Vector in this parameter the minimum versions of the dependend modules will be returned.
     * @param maxVersions Vector in this parameter the maximum versions of the dependend modules will be returned.
     * @return int the amount of dependencies for the module will be returned.
     */
    public int getModuleDependencies(String modulename, Vector modules, Vector minVersions, Vector maxVersions) {
        try {
            Element module = getModuleElement(modulename);
            Element dependencies = (Element) (module.getElementsByTagName("dependencies").item(0));
            NodeList deps = dependencies.getElementsByTagName("dependency");
            for (int i = 0; i < deps.getLength(); i++) {
                modules.addElement(((Element) deps.item(i)).getElementsByTagName("name").item(0).getFirstChild().getNodeValue());
                minVersions.addElement(((Element) deps.item(i)).getElementsByTagName("minversion").item(0).getFirstChild().getNodeValue());
                maxVersions.addElement(((Element) deps.item(i)).getElementsByTagName("maxversion").item(0).getFirstChild().getNodeValue());
            }
        } catch (Exception exc) {
            // ignore the exception - reg is not welformed
        }
        return modules.size();
    }

    /**
     * Returns the description of the module.
     *
     * @param String the name of the module.
     * @return java.lang.String the description of the module.
     */
    public String getModuleDescription(String module) {
        return getModuleData(module, "description");
    }
    
    /**
     * Gets the url to the documentation of the module.
     *
     * @param String the name of the module.
     * @return java.lang.String the url to the documentation of the module.
     */
    public String getModuleDocumentPath(String modulename) {
        return getModuleData(modulename, "documentation");
    }
    
    /**
     *  Private method to get the Element representing a module.
     *
     * @param String the name of the module.
     *
     */
    private Element getModuleElement(String name) {
        return (Element)m_modules.get(name);
    }

    /**
     * Reads the module-element from the manifest in the zip-file.
     * @param string the name of the zip-file to read from.
     * @return the module-element or null if it dosen't exist.
     */
    private Element getModuleElementFromImport(String filename) {
        try {
            // get the zip-file
            ZipFile importZip = new ZipFile(filename);
            // read the minifest
            ZipEntry entry = importZip.getEntry("manifest.xml");
            InputStream stream = importZip.getInputStream(entry);
            // parse the manifest
            Document manifest = parse(stream);
            importZip.close();
            // get the module-element
            return (Element) (manifest.getElementsByTagName("module").item(0));
        } catch (Exception exc) {
            return null;
        }
    }

    /**
     * Returns all filenames and hashcodes belonging to the module.
     *
     * @param String modulname the name of the module.
     * @param retNames the names of the resources belonging to the module.
     * @param retCodes the hashcodes of the resources belonging to the module.
     * @return the amount of entrys.
     */
    public int getModuleFiles(String modulename, Vector retNames, Vector retCodes) {
        try {
            Element module = getModuleElement(modulename);
            Element files = (Element) (module.getElementsByTagName("files").item(0));
            NodeList file = files.getElementsByTagName("file");
            for (int i = 0; i < file.getLength(); i++) {
                retNames.addElement(
                    ((Element)file.item(i)).getElementsByTagName("name").item(0).getFirstChild().getNodeValue());
                retCodes.addElement(
                    ((Element)file.item(i)).getElementsByTagName("checksum").item(0).getFirstChild().getNodeValue());
            }
        } catch (Exception exc) {
            // ignore the exception - reg is not welformed
        }
        return retNames.size();
    }

    /**
     * Returns the class, that receives all maintenance-events for the module.
     *
     * @param String the name of the module.
     * @return java.lang.Class that receives all maintenance-events for the module.
     */
    public Class getModuleMaintenanceEventClass(String modulname) {
        try {
    
            Vector repositories = new Vector();
            String[] reposNoVector = getRepositories();
            for (int i = 0; i < reposNoVector.length; i++) {
                repositories.addElement(reposNoVector[i]);
            }
            ClassLoader loader = this.getClass().getClassLoader();
    
            return loader.loadClass(getModuleData(modulname, "maintenance_class"));
    
        } catch (Exception exc) {
            return null;
        }
    }
    
    /**
     * Returns the name of the class, that receives all maintenance-events for the module.
     *
     * @param String the name of the module.
     * @return java.lang.Class that receives all maintenance-events for the module.
     */
    public String getModuleMaintenanceEventName(String modulname) {
        return getModuleData(modulname, "maintenance_class");
    }
    
    /**
     * Returns the names of all available modules.
     *
     * @return Enumeration the names of all available modules.
     */
    public Enumeration getModuleNames() {
        return m_modules.keys();
    }
    
    /**
     * Returns the nice name of the module.
     *
     * @param String the name of the module.
     * @return java.lang.String the description of the module.
     */
    public String getModuleNiceName(String module) {
        return getModuleData(module, "nicename");
    }
    
    /**
     * Gets a parameter for a module.
     *
     * @param modulename java.lang.String the name of the module.
     * @param parameter java.lang.String the name of the parameter to set.
     * @return value java.lang.String the value to set for the parameter.
     */
    public String getModuleParameter(String modulename, String parameter) {
        String retValue = null;
        try {
            Element param = getModuleParameterElement(modulename, parameter);
            retValue = param.getElementsByTagName("value").item(0).getFirstChild().getNodeValue();
        } catch (Exception exc) {
            // ignore the exception - parameter is not existent
        }
        return retValue;
    }

    /**
     * Gets a parameter for a module.
     *
     * @param modulename java.lang.String the name of the module.
     * @param parameter java.lang.String the name of the parameter to set.
     * @param defaultValue the default value.
     * @return value java.lang.String the value to set for the parameter.
     */
    public String getModuleParameter(String modulename, String parameter, String defaultValue) {
        String retValue = null;
        try {
            Element param = getModuleParameterElement(modulename, parameter);
            retValue = param.getElementsByTagName("value").item(0).getFirstChild().getNodeValue();
        } catch (Exception exc) {
            retValue = defaultValue;
            // ignore the exception - parameter is not existent
        }
        return retValue;
    }

    /**
     * Returns a parameter for a module.
     *
     * @param modulname String the name of the module.
     * @param parameter String the name of the parameter.
     * @return boolean the value for the parameter in the module.
     */
    public boolean getModuleParameterBoolean(String modulname, String parameter) {
        if ("true".equals(getModuleParameter(modulname, parameter).toLowerCase())) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * Returns a parameter for a module.
     *
     * @param modulname String the name of the module.
     * @param parameter String the name of the parameter.
     * @param default the default value.
     * @return boolean the value for the parameter in the module.
     */
    public Boolean getModuleParameterBoolean(String modulname, String parameter, Boolean defaultValue) {
        return new Boolean(getModuleParameterBoolean(modulname, parameter, defaultValue.booleanValue()));
    }
    
    /**
     * Returns a parameter for a module.
     *
     * @param modulname String the name of the module.
     * @param parameter String the name of the parameter.
     * @param default the default value.
     * @return boolean the value for the parameter in the module.
     */
    public boolean getModuleParameterBoolean(String modulname, String parameter, boolean defaultValue) {
        if (getModuleParameterBoolean(modulname, parameter)) {
            return true;
        } else {
            return defaultValue;
        }
    }
    
    /**
     * Returns a parameter for a module.
     *
     * @param modulname String the name of the module.
     * @param parameter String the name of the parameter.
     * @param default the default value.
     * @return boolean the value for the parameter in the module.
     */
    public byte getModuleParameterByte(String modulname, String parameter) {
        return Byte.parseByte(getModuleParameter(modulname, parameter));
    }
    
    /**
     * Returns a parameter for a module.
     *
     * @param modulname String the name of the module.
     * @param parameter String the name of the parameter.
     * @param default the default value.
     * @return boolean the value for the parameter in the module.
     */
    public byte getModuleParameterByte(String modulname, String parameter, byte defaultValue) {
        return Byte.parseByte(getModuleParameter(modulname, parameter, Byte.toString(defaultValue)));
    }
    
    /**
     * Returns a parameter for a module.
     *
     * @param modulname String the name of the module.
     * @param parameter String the name of the parameter.
     * @param default the default value.
     * @return boolean the value for the parameter in the module.
     */
    public Byte getModuleParameterByte(String modulname, String parameter, Byte defaultValue) {
        return new Byte(getModuleParameterByte(modulname, parameter, defaultValue.byteValue()));
    }
    
    /**
     * Returns a description for parameter in a module.
     *
     * @param modulname String the name of the module.
     * @param parameter String the name of the parameter.
     * @return String the description for the parameter in the module.
     */
    public String getModuleParameterDescription(String modulname, String parameter) {
        String retValue = null;
        try {
            Element param = getModuleParameterElement(modulname, parameter);
            retValue = param.getElementsByTagName("description").item(0).getFirstChild().getNodeValue();
        } catch (Exception exc) {
            // ignore the exception - parameter is not existent
        }
        return retValue;
    }
    
    /**
     * Returns a parameter for a module.
     *
     * @param modulname String the name of the module.
     * @param parameter String the name of the parameter.
     * @return boolean the value for the parameter in the module.
     */
    public double getModuleParameterDouble(String modulname, String parameter) {
        return Double.valueOf(getModuleParameter(modulname, parameter)).doubleValue();
    }
    
    /**
     * Returns a parameter for a module.
     *
     * @param modulname String the name of the module.
     * @param parameter String the name of the parameter.
     * @param default the default value.
     * @return boolean the value for the parameter in the module.
     */
    public double getModuleParameterDouble(String modulname, String parameter, double defaultValue) {
        return Double.valueOf(getModuleParameter(modulname, parameter, Double.toString(defaultValue))).doubleValue();
    }
    
    /**
     * Returns a parameter for a module.
     *
     * @param modulname String the name of the module.
     * @param parameter String the name of the parameter.
     * @param default the default value.
     * @return boolean the value for the parameter in the module.
     */
    public Double getModuleParameterDouble(String modulname, String parameter, Double defaultValue) {
        return new Double(getModuleParameterDouble(modulname, parameter, defaultValue.doubleValue()));
    }

    /**
     * Private method to get them XML-Element for a parameter in a module.
     *
     * @param modulename String the name of the module.
     * @param parameter String the name of the parameter.
     * @return Element the XML-Element corresponding to the parameter.
     */
    private Element getModuleParameterElement(String modulename, String parameter) {
        Element retValue = null;
        try {
            Element module = getModuleElement(modulename);
            Element parameters = (Element) (module.getElementsByTagName("parameters").item(0));
            NodeList para = parameters.getElementsByTagName("para");
            for (int i = 0; i < para.getLength(); i++) {
                if (((Element)para.item(i))
                    .getElementsByTagName("name")
                    .item(0)
                    .getFirstChild()
                    .getNodeValue()
                    .equals(parameter)) {
                    // this is the element for the parameter.
                    retValue = (Element)para.item(i);
                    // stop searching - parameter was found
                    break;
                }
            }
        } catch (Exception exc) {
            // ignore the exception - reg is not welformed
        }
        return retValue;
    }

    /**
     * Returns a parameter for a module.
     *
     * @param modulname String the name of the module.
     * @param parameter String the name of the parameter.
     * @param default the default value.
     * @return boolean the value for the parameter in the module.
     */
    public float getModuleParameterFloat(String modulname, String parameter) {
        return Float.valueOf(getModuleParameter(modulname, parameter)).floatValue();
    }
    
    /**
     * Returns a parameter for a module.
     *
     * @param modulname String the name of the module.
     * @param parameter String the name of the parameter.
     * @param default the default value.
     * @return boolean the value for the parameter in the module.
     */
    public float getModuleParameterFloat(String modulname, String parameter, float defaultValue) {
        return Float.valueOf(getModuleParameter(modulname, parameter, Float.toString(defaultValue))).floatValue();
    }
    
    /**
     * Returns a parameter for a module.
     *
     * @param modulname String the name of the module.
     * @param parameter String the name of the parameter.
     * @param default the default value.
     * @return boolean the value for the parameter in the module.
     */
    public Float getModuleParameterFloat(String modulname, String parameter, Float defaultValue) {
        return new Float(getModuleParameterFloat(modulname, parameter, defaultValue.floatValue()));
    }
    
    /**
     * Returns a parameter for a module.
     *
     * @param modulname String the name of the module.
     * @param parameter String the name of the parameter.
     * @return boolean the value for the parameter in the module.
     */
    public int getModuleParameterInteger(String modulname, String parameter) {
        return Integer.parseInt(getModuleParameter(modulname, parameter));
    }
    
    /**
     * Returns a parameter for a module.
     *
     * @param modulname String the name of the module.
     * @param parameter String the name of the parameter.
     * @param default the default value.
     * @return boolean the value for the parameter in the module.
     */
    public int getModuleParameterInteger(String modulname, String parameter, int defaultValue) {
        return Integer.parseInt(getModuleParameter(modulname, parameter, Integer.toString(defaultValue)));
    }
    /**
     * Returns a parameter for a module.
     *
     * @param modulname String the name of the module.
     * @param parameter String the name of the parameter.
     * @param default the default value.
     * @return boolean the value for the parameter in the module.
     */
    public Integer getModuleParameterInteger(String modulname, String parameter, Integer defaultValue) {
        return new Integer(getModuleParameterInteger(modulname, parameter, defaultValue.intValue()));
    }
    
    /**
     * Returns a parameter for a module.
     *
     * @param modulname String the name of the module.
     * @param parameter String the name of the parameter.
     * @param default the default value.
     * @return boolean the value for the parameter in the module.
     */
    public long getModuleParameterLong(String modulname, String parameter) {
        return Long.valueOf(getModuleParameter(modulname, parameter)).longValue();
    }
    
    /**
     * Returns a parameter for a module.
     *
     * @param modulname String the name of the module.
     * @param parameter String the name of the parameter.
     * @param default the default value.
     * @return boolean the value for the parameter in the module.
     */
    public long getModuleParameterLong(String modulname, String parameter, long defaultValue) {
        return Long.valueOf(getModuleParameter(modulname, parameter, Long.toString(defaultValue))).longValue();
    }
    
    /**
     * Returns a parameter for a module.
     *
     * @param modulname String the name of the module.
     * @param parameter String the name of the parameter.
     * @param default the default value.
     * @return boolean the value for the parameter in the module.
     */
    public Long getModuleParameterLong(String modulname, String parameter, Long defaultValue) {
        return new Long(getModuleParameterLong(modulname, parameter, defaultValue.longValue()));
    }
    
    /**
     * Gets all parameter-names for a module.
     *
     * @param modulename String the name of the module.
     * @return value String[] the names of the parameters for a module.
     */
    public String[] getModuleParameterNames(String modulename) {
        String[] retValue = null;
        try {
            Element module = getModuleElement(modulename);
            Element parameters = (Element) (module.getElementsByTagName("parameters").item(0));
            NodeList para = parameters.getElementsByTagName("para");
            retValue = new String[para.getLength()];
            for (int i = 0; i < para.getLength(); i++) {
                retValue[i] = ((Element)para.item(i)).getElementsByTagName("name").item(0).getFirstChild().getNodeValue();
            }
        } catch (Exception exc) {
            // ignore the exception - reg is not welformed
        }
        return retValue;
    }

    /**
     * Returns a parameter for a module.
     *
     * @param modulname String the name of the module.
     * @param parameter String the name of the parameter.
     * @return boolean the value for the parameter in the module.
     */
    public String getModuleParameterString(String modulname, String parameter) {
        return getModuleParameter(modulname, parameter);
    }
    
    /**
     * Returns a parameter for a module.
     *
     * @param modulname String the name of the module.
     * @param parameter String the name of the parameter.
     * @param default the default value.
     * @return boolean the value for the parameter in the module.
     */
    public String getModuleParameterString(String modulname, String parameter, String defaultValue) {
        return getModuleParameter(modulname, parameter, defaultValue);
    }
    
    /**
     * This method returns the type of a parameter in a module.
     *
     * @param modulename the name of the module.
     * @param parameter the name of the parameter.
     * @return the type of the parameter.
     */
    public String getModuleParameterType(String modulename, String parameter) {
        String retValue = null;
        try {
            Element param = getModuleParameterElement(modulename, parameter);
            retValue = param.getElementsByTagName("type").item(0).getFirstChild().getNodeValue();
        } catch (Exception exc) {
            // ignore the exception - parameter is not existent
        }
        return retValue;
    }
    
    /**
     * Returns all repositories for a module.
     *
     * @param String modulname the name of the module.
     * @return java.lang.String[] the reprositories of a module.
     */
    public java.lang.String[] getModuleRepositories(String modulename) {
        String[] retValue = null;
        try {
            Element module = getModuleElement(modulename);
            Element repository = (Element) (module.getElementsByTagName("repository").item(0));
            NodeList paths = repository.getElementsByTagName("path");
            retValue = new String[paths.getLength()];
            for (int i = 0; i < paths.getLength(); i++) {
                retValue[i] = paths.item(i).getFirstChild().getNodeValue();
            }
        } catch (Exception exc) {
            // ignore the exception - reg is not welformed
        }
        return retValue;
    }
    
    /**
     * Returns the upload-date for the module.
     *
     * @param String the name of the module.
     * @return java.lang.String the upload-date for the module.
     */
    public long getModuleUploadDate(String modulname) {
        long retValue = -1;
        try {
            //String value = getModuleData(modulname, "uploaddate");
            Element moduleElement = getModuleElement(modulname);
            NodeList allUploadDates = moduleElement.getElementsByTagName("uploaddate");
            String value = allUploadDates.item((allUploadDates.getLength() - 1)).getFirstChild().getNodeValue();
    
            retValue = m_dateFormat.parse(value).getTime();
        } catch (Exception exc) {
            // ignore the exception - reg is not welformed
        }
        return retValue;
    }
    
    /**
     * Returns the user-name of the user who had uploaded the module.
     *
     * @param String the name of the module.
     * @return java.lang.String the user-name of the user who had uploaded the module.
     */
    public String getModuleUploadedBy(String modulename) {
        String retValue = "";
        try {
            Element moduleElement = getModuleElement(modulename);
            NodeList allUploadDates = moduleElement.getElementsByTagName("uploadedby");
            retValue = allUploadDates.item((allUploadDates.getLength() - 1)).getFirstChild().getNodeValue();
        } catch (Exception e) {}
        return retValue;
    }

    /**
     * This method returns the version of the module.
     *
     * @param String the name of the module.
     * @return java.lang.String the version of the module.
     */
    public float getModuleVersion(String modulename) {
        float retValue = -1;
        try {
            retValue = Float.parseFloat(getModuleData(modulename, "version"));
        } catch (Exception exc) {
            // ignore the exception - reg is not welformed
        }
        return retValue;
    }
    
    /**
     * Returns the name of the view, that is implemented by the module.
     *
     * @param String the name of the module.
     * @return java.lang.String the name of the view, that is implemented by the module.
     */
    public String getModuleViewName(String modulename) {
        String retValue = null;
        try {
            Element module = getModuleElement(modulename);
            Element view = (Element) (module.getElementsByTagName("view").item(0));
            retValue = view.getElementsByTagName("name").item(0).getFirstChild().getNodeValue();
        } catch (Exception exc) {
            // ignore the exception - reg is not welformed
        }
        return retValue;
    }
    
    /**
     * Returns the url to the view-url for the module within the system.
     *
     * @param String the name of the module.
     * @return java.lang.String the view-url to the module.
     */
    public String getModuleViewUrl(String modulname) {
        String retValue = null;
        try {
            Element module = getModuleElement(modulname);
            Element view = (Element) (module.getElementsByTagName("view").item(0));
            retValue = view.getElementsByTagName("url").item(0).getFirstChild().getNodeValue();
        } catch (Exception exc) {
            // ignore the exception - reg is not welformed
        }
        return retValue;
    }

    /**
     * Returns all lifecycle classes for all modules.
     *
     * @param Vector classes in this parameter the classes will be returned.
     * @return int the amount of classes.
     */
    public int getModuleLifeCycle(Vector classes) {
        try {
            NodeList classList = m_xmlReg.getElementsByTagName("lifecycleclass");
            for (int x = 0; x < classList.getLength(); x++) {
                try {
                    String name =
                        ((Element)classList.item(x)).getElementsByTagName("name").item(0).getFirstChild().getNodeValue();
                    classes.addElement(name);
                } catch (Exception exc) {
                    // ignore the exception and try the next view-pair.
                }
            }
            return classes.size();
        } catch (Exception exc) {
            // no return-values
            return 0;
        }
    }

    /**
     * Returns the name of the class, that contains the publish method of the module.
     *
     * @param String the name of the module.
     * @return java.lang.Class that contains the publish method of the module.
     */
    public String getModulePublishClass(String modulname) {
        String retValue = null;
        try {
            Element module = getModuleElement(modulname);
            Element publishClass = (Element) (module.getElementsByTagName("publishclass").item(0));
            retValue = publishClass.getElementsByTagName("name").item(0).getFirstChild().getNodeValue();
        } catch (Exception exc) {
            // ignore the exception - reg is not welformed
        }
        return retValue;
    }

    /**
     * Returns all publishable classes for all modules.
     *
     * @param Vector classes in this parameter the classes will be returned.
     * @param String requiredMethod The value of the methodTag for the different
     *      methods useable after publish.
     *          null means the standard publish method
     *          "linkpublish" means the method that needs the changed links as parameter (i.e. search)
     * @return int the amount of classes.
     */
    public int getModulePublishables(Vector classes, String requiredMethod) {
        if (requiredMethod == null) {
            requiredMethod = "";
        }
        try {
            NodeList classList = m_xmlReg.getElementsByTagName("publishclass");
            for (int x = 0; x < classList.getLength(); x++) {
                try {
                    String methodValue = ((Element)classList.item(x)).getAttribute("method");
                    if (methodValue == null) {
                        methodValue = "";
                    }
                    if (methodValue.equals(requiredMethod)) {
                        String name =
                            ((Element)classList.item(x))
                                .getElementsByTagName("name")
                                .item(0)
                                .getFirstChild()
                                .getNodeValue();
                        if ((name != null) && (!"".equals(name)))
                            classes.addElement(name);
                    }
                } catch (Exception exc) {
                    // ignore the exception and try the next view-pair.
                }
            }
            return classes.size();
        } catch (Exception exc) {
            // no return-values
            return 0;
        }
    }

    /**
     * Returns all exportable classes for all modules.
     *
     * @param Hashtable classes in this parameter the classes will be returned.
     * @return int the amount of classes.
     */
    public int getModuleExportables(Hashtable classes) {
        try {
            Enumeration allModules = m_modules.keys();
            while (allModules.hasMoreElements()) {
                String nicename = (String)allModules.nextElement();
                NodeList classList = ((Element)m_modules.get(nicename)).getElementsByTagName("publishclass");
                if (classList.getLength() > 0) {
                    try {
                        String classname =
                            ((Element)classList.item(0))
                                .getElementsByTagName("name")
                                .item(0)
                                .getFirstChild()
                                .getNodeValue();
                        if (classname != null && !"".equalsIgnoreCase(classname)) {
                            classes.put(nicename, classname);
                        }
                    } catch (Exception exc) {
                        // ignore the exception and try the next view-pair.
                    }
                }
            }
            return classes.size();
        } catch (Exception exc) {
            // no return-values
            return 0;
        }
    }

    /**
     * Returns all exportpoints and paths.
     *
     *
     * @return Hashtable The exportpoints and the paths.
     */
    public Hashtable getExportpoints() {
        if((m_exportpoints == null) || (m_exportpoints.size() == 0)){
            m_exportpoints = new Hashtable();
            try {
                NodeList exportpointsList = m_xmlReg.getElementsByTagName("exportpoint");
                for (int x = 0; x < exportpointsList.getLength(); x++) {
                    try {
                        String curExportpoint = ((Element) exportpointsList.item(x)).getElementsByTagName("source").item(0).getFirstChild().getNodeValue();
                        String curPath = ((Element) exportpointsList.item(x)).getElementsByTagName("destination").item(0).getFirstChild().getNodeValue();
                        m_exportpoints.put(curExportpoint, com.opencms.boot.CmsBase.getAbsoluteWebPath(curPath));
                    } catch(Exception exc) {
                        exc.printStackTrace();
                        // ignore the exception and try the next view-pair.
                    }
                }
            } catch (Exception exc) {
                exc.printStackTrace();
                // no return-values
            }
        }
    
        return m_exportpoints;
    }

    /**
     * Returns all repositories for all modules.
     *
     * @return java.lang.String[] the reprositories of all modules.
     */
    public java.lang.String[] getRepositories() {
        NodeList repositories = m_xmlReg.getElementsByTagName("repository");
        Vector retValue = new Vector();
        String[] retValueArray = new String[0];
        for (int x = 0; x < repositories.getLength(); x++) {
            NodeList paths = ((Element)repositories.item(x)).getElementsByTagName("path");
            for (int y = 0; y < paths.getLength(); y++) {
                retValue.addElement(paths.item(y).getFirstChild().getNodeValue());
            }
        }
        retValueArray = new String[retValue.size()];
        retValue.copyInto(retValueArray);
        return retValueArray;
    }

    /**
     * Returns all Resourcetypes and korresponding parameter for System and all modules.
     *
     * @param Vector names in this parameter the names of the Resourcetypes will be returned.
     * @param Vector launcherTypes in this parameters the launcherType will be returned(int).
     * @param Vector launcherClass in this parameters the launcherClass will be returned.
     * @param Vector resourceClass in this parameters the resourceClass will be returned.
     * @return int the amount of resourcetypes.
     */
    public int getResourceTypes(Vector names, Vector launcherTypes, Vector launcherClass, Vector resourceClass){
        try{
            NodeList resTypes = m_xmlReg.getElementsByTagName("restype");
            for (int x = 0; x < resTypes.getLength(); x++){
                try{
                    String name = ((Element)resTypes.item(x)).getElementsByTagName("name").item(0).getFirstChild().getNodeValue();
                    String lType = ((Element)resTypes.item(x)).getElementsByTagName("launcherType").item(0).getFirstChild().getNodeValue();
                    String lClass = "";
                    try{
                        lClass = ((Element)resTypes.item(x)).getElementsByTagName("launcherClass").item(0).getFirstChild().getNodeValue();
                    }catch(Exception ex){
                    }
                    String resClass = ((Element)resTypes.item(x)).getElementsByTagName("resourceClass").item(0).getFirstChild().getNodeValue();
                    names.addElement(name);
                    launcherTypes.addElement(lType);
                    launcherClass.addElement(lClass);
                    resourceClass.addElement(resClass);
                }catch(Exception exc){
                    // ignore the exeption
                }
            }
            return names.size();
        }catch(Exception e){
            // no returnvalues
            return 0;
        }
    }

    /**
     * Returns a value for a system-key.
     * E.g. <code>&lt;system&gt;&lt;mailserver&gt;mail.server.com&lt;/mailserver&gt;&lt;/system&gt;</code>
     * can be requested via <code>getSystemValue("mailserver");</code> and returns "mail.server.com".
     *
     * @param String the key of the system-value.
     * @return the value for that system-key.
     */
    public String getSystemValue(String key) {
        String retValue = null;
        try {
            Element systemElement = (Element)m_xmlReg.getElementsByTagName("system").item(0);
            retValue = systemElement.getElementsByTagName(key).item(0).getFirstChild().getNodeValue();
        } catch (Exception exc) {
            // ignore the exception - registry is not wellformed
        }
        return retValue;
    }

    /**
     * Returns a vector of value for a system-key.
     *
     * @param String the key of the system-value.
     * @return the values for that system-key.
     */
    public Hashtable getSystemValues(String key) {
        Hashtable retValue = new Hashtable();
        try {
            Element systemElement = (Element)m_xmlReg.getElementsByTagName("system").item(0);
            NodeList list = systemElement.getElementsByTagName(key).item(0).getChildNodes();
            for (int i = 0; i < list.getLength(); i++) {
                retValue.put(list.item(i).getNodeName(), list.item(i).getFirstChild().getNodeValue());
            }
        } catch (Exception exc) {
            // ignore the exception - registry is not wellformed
        }
        return retValue;
    }

    /**
     * Return the XML "system" node Element from the registry for further
     * processing in another class.
     * @return the system node.
     */
    public Element getSystemElement() {
        return (Element)m_xmlReg.getElementsByTagName("system").item(0);
    }

    /**
     * Returns all views and korresponding urls for all modules.
     *
     * @param String[] views in this parameter the views will be returned.
     * @param String[] urls in this parameters the urls vor the views will be returned.
     * @return int the amount of views.
     */
    public int getViews(Vector views, Vector urls) {
        try {
            NodeList viewList = m_xmlReg.getElementsByTagName("view");
            for (int x = 0; x < viewList.getLength(); x++) {
                try {
                    String name =
                        ((Element)viewList.item(x)).getElementsByTagName("name").item(0).getFirstChild().getNodeValue();
                    String url =
                        ((Element)viewList.item(x)).getElementsByTagName("url").item(0).getFirstChild().getNodeValue();
                    views.addElement(name);
                    urls.addElement(url);
                } catch (Exception exc) {
                    // ignore the exception and try the next view-pair.
                }
            }
            return views.size();
        } catch (Exception exc) {
            // no return-vaules
            return 0;
        }
    }

    /**
     * Gets the expected tagname for the XML documents of this content type
     * @return Expected XML tagname.
     */
    public String getXmlDocumentTagName() {
        return "registry";
    }
    
    /**
     * Returns true if the user has write-access to the registry. Otherwise false.
     * @return true if access is granted, else false.
     */
    private boolean hasAccess() {
        // check the access - only the admin has write access.
        boolean retValue = false;
        try {
            retValue = m_cms.isAdmin();
        } catch (CmsException exc) {
            // ignore the exception - no access granted
        }
        return retValue;
    }

    /**
     * @see com.opencms.file.I_CmsRegistry#importCheckDependencies(String, boolean)
     */
    public Vector importCheckDependencies(String moduleZip, boolean replaceMode) throws CmsException {
        Element newModule = getModuleElementFromImport(moduleZip);
        return checkDependencies(newModule, replaceMode);
    }
    
    /**
     *  Checks for files that already exist in the system but should be replaced by the module.
     *
     *  @param moduleZip The name of the zip-file to import.
     *  @return The complete paths to the resources that have conflicts.
     */
    public Vector importGetConflictingFileNames(String moduleZip) throws CmsException {
        if (!hasAccess()) {
            throw new CmsException(
                "No access to perform the action 'getConflictingFileNames'",
                CmsException.C_REGISTRY_ERROR);
        }
    
        CmsImport cmsImport = new CmsImport(moduleZip, "/", m_cms, new CmsShellReport());
        return cmsImport.getConflictingFilenames();
    }

    /**
     * Returns a map of information about the module to be imported.<p>
     *
     * The map contains the following values:
     * <ul>
     * <li><code>"name"</code>: the package name of the module (e.g. "org.opencms.default")
     * <li><code>"type"</code>: the type of the module ("simple" or "traditional")
     * </ul>
     *
     * @param moduleZip the name of the zip file to import
     * @return a map of information about the module to be imported
     */
    public Map importGetModuleInfo(String moduleZip) {
        // read module node from import
        Element newModule = getModuleElementFromImport(moduleZip);
        
        // get module name
        String moduleName = newModule.getElementsByTagName("name").item(0).getFirstChild().getNodeValue();
        
        // get module type
        String moduleType = null; 
        NodeList list = newModule.getChildNodes();
        for (int i=0; i<list.getLength(); i++) {
            Element e = (Element)list.item(i);
            if ("type".equals(e.getNodeName())) {
                moduleType = e.getFirstChild().getNodeValue();
                break;
            }
        }  
        if (moduleType == null) moduleType = C_MODULE_TYPE_TRADITIONAL;

        // fill return value map 
        HashMap map = new HashMap();
        map.put("name", moduleName);
        map.put("type", moduleType);

        return map;
    }
    
    /**
     *  Returns all files that are needed to create a project for the module-import.
     *
     *  @param moduleZip The name of the zip-file to import.
     *  @return The complete paths for resources that should be in the import-project.
     */
    public Vector importGetResourcesForProject(String moduleZip) throws CmsException {
        if (!hasAccess()) {
            throw new CmsException(
                "No access to perform the action 'importGetResourcesForProject'",
                CmsException.C_REGISTRY_ERROR);
        }
    
        CmsImport cmsImport = new CmsImport(moduleZip, "/", m_cms, new CmsShellReport());
        return cmsImport.getResourcesForProject();
    }

    /**
     *  Imports a module. This method is synchronized, so only one module can be imported at on time.
     *
     *  @param moduleZip the name of the zip-file to import from.
     *  @param exclusion a Vector with resource-names that should be excluded from this import.
     */
    public synchronized void importModule(String moduleZip, Vector exclusion, I_CmsReport report) throws CmsException {
        // check if the user is allowed to import a module.
    
        if (!hasAccess()) {
            throw new CmsException("No access to perform the action 'importModule'", CmsException.C_REGISTRY_ERROR);
        }
        Element newModule = getModuleElementFromImport(moduleZip);
        String newModuleName = newModule.getElementsByTagName("name").item(0).getFirstChild().getNodeValue();
        String newModuleVersion = newModule.getElementsByTagName("version").item(0).getFirstChild().getNodeValue();
    
        // does the module already exist?
        if (moduleExists(newModuleName)) {
            throw new CmsException("The module " + newModuleName + " exists already", CmsException.C_REGISTRY_ERROR);
        }
        Vector dependencies = checkDependencies(newModule, false);
    
        // are there any dependencies not fulfilled?
        if (dependencies.size() != 0) {
            throw new CmsException("the dependencies for the module are not fulfilled.", CmsException.C_REGISTRY_ERROR);
        }
    
        Vector resourceNames = new Vector();
        Vector resourceCodes = new Vector();
    
        String propertyName = null;
        String propertyValue = null;
    
        // check for module type SIMPLE or TRADITIONAL
        boolean isSimpleModule = false;
        try {
            isSimpleModule =
                CmsRegistry.C_MODULE_TYPE_SIMPLE.equals(
                    newModule.getElementsByTagName("type").item(0).getFirstChild().getNodeValue());
        } catch (Exception e) {
            // value of "isSimpleModule" will be false, so traditional module is the default         
        }
        
        if (isSimpleModule) {
            // add all 5.0 default directories to the exclusion list
            // otherwise all of these folders would be locked during import, which is usually not 
            // required and slows down the import considerably (esp. for the "/system/" fodler)            
            exclusion.add(I_CmsWpConstants.C_VFS_PATH_SYSTEM);
            exclusion.add(I_CmsWpConstants.C_VFS_PATH_MODULES);
            exclusion.add(I_CmsWpConstants.C_VFS_PATH_BODIES);
            exclusion.add(I_CmsWpConstants.C_VFS_PATH_WORKPLACE);
            exclusion.add(I_CmsWpConstants.C_VFS_PATH_MODULEDEMOS);
            exclusion.add(I_CmsWpConstants.C_VFS_PATH_GALLERIES);
            exclusion.add(I_CmsWpConstants.C_VFS_PATH_HELP);
            exclusion.add(I_CmsWpConstants.C_VFS_PATH_LOCALES);
            exclusion.add(I_CmsWpConstants.C_VFS_PATH_SCRIPTS);
            exclusion.add(I_CmsWpConstants.C_VFS_PATH_SYSTEMPICS);
            exclusion.add(I_CmsWpConstants.C_VFS_GALLERY_PICS);
            exclusion.add(I_CmsWpConstants.C_VFS_GALLERY_HTML);
            exclusion.add(I_CmsWpConstants.C_VFS_GALLERY_DOWNLOAD);
            exclusion.add(I_CmsWpConstants.C_VFS_GALLERY_EXTERNALLINKS);
        } else {    
            // traditional module requires weird "module" property             
            // get list of unwanted properties
            List deleteProperties = (List) A_OpenCms.getRuntimeProperty("compatibility.support.import.remove.propertytags");
            if ((deleteProperties != null) && (deleteProperties.contains("module")))  {
                propertyName = propertyValue = null;
            } else {
                propertyName = "module";
                propertyValue = newModuleName + "_" + newModuleVersion;
            }
        }
            
        CmsImport cmsImport = new CmsImport(moduleZip, "/", m_cms, report);
        cmsImport.importResources(exclusion, resourceNames, resourceCodes, propertyName, propertyValue);
    
        // import the module data into the registry
        Element regModules = (Element) (m_xmlReg.getElementsByTagName("modules").item(0));
        // set the import-date
        Node uploadDate = newModule.getOwnerDocument().createElement("uploaddate");
    
        uploadDate.appendChild(newModule.getOwnerDocument().createTextNode(m_dateFormat.format(new java.util.Date())));
        newModule.appendChild(uploadDate);
    
        // set the import-user
        Node uploadBy = newModule.getOwnerDocument().createElement("uploadedby");
        uploadBy.appendChild(
            newModule.getOwnerDocument().createTextNode(m_cms.getRequestContext().currentUser().getName()));
        newModule.appendChild(uploadBy);
    
        if (!isSimpleModule) {
            // set the files
            Node files = newModule.getOwnerDocument().createElement("files");
    
            // store the resources-names that are depending to the module
            for (int i = 0; i < resourceNames.size(); i++) {
                Node file = newModule.getOwnerDocument().createElement("file");
                files.appendChild(file);
                Node name = newModule.getOwnerDocument().createElement("name");
                file.appendChild(name);
                Node checksum = newModule.getOwnerDocument().createElement("checksum");
                file.appendChild(checksum);
                name.appendChild(newModule.getOwnerDocument().createTextNode((String)resourceNames.elementAt(i)));
                // Encoding project:
                checksum.appendChild(
                    newModule.getOwnerDocument().createTextNode(
                        com.opencms.util.Encoder.escape(
                            (String)resourceCodes.elementAt(i),
                            m_cms.getRequestContext().getEncoding())));
            }
    
            // append the files to the module-entry
            newModule.appendChild(files);
        }
    
        // append the module data to the registry
        Node newNode = getXmlParser().importNode(m_xmlReg, newModule);
        regModules.appendChild(newNode);
        saveRegistry();
    
        try {
            init(false);
        } catch (Exception exc) {
            throw new CmsException("couldn't init registry", CmsException.C_REGISTRY_ERROR, exc);
        }
    
        // try to invoke the event-method for upload on this calss.
        Class eventClass = getModuleMaintenanceEventClass(newModuleName);
    
        try {
            Class declaration[] = { CmsObject.class };
            Object arguments[] = { m_cms };
            Method eventMethod = eventClass.getMethod(C_UPLOAD_EVENT_METHOD_NAME, declaration);
            eventMethod.invoke(null, arguments);
        } catch (Exception exc) {
            // ignore the exception.
        }
    }

    /**
     *  Inits all member-variables for the instance.
     */
    private void init(boolean booting) throws Exception {
        // clear and refill the hashtable for the exportpoints
        m_exportpoints.clear();
        getExportpoints();
        // get the entry-points for the modules
        NodeList modules = m_xmlReg.getElementsByTagName("module");
        // create the hashtable for the shortcuts
        m_modules.clear();
        // walk throug all modules
        for (int i = 0; i < modules.getLength(); i++) {
            Element module = (Element)modules.item(i);
            String moduleName = module.getElementsByTagName("name").item(0).getFirstChild().getNodeValue();
            if (C_LOGGING && booting && A_OpenCms.isLogging(C_OPENCMS_INIT))
                A_OpenCms.log(C_OPENCMS_INIT, ". Loading module       : " + moduleName);
            // store the shortcuts to the modules
            m_modules.put(moduleName, module);
        }
    }
    
    /**
     * Checks if the module exists already in the repository.
     *
     * @param String the name of the module.
     * @return true if the module exists, else false.
     */
    public boolean moduleExists(String modulename) {
        return m_modules.containsKey(modulename);
    }
    
    /**
     *  Saves the registry and stores it to the registry-file.
     */
    private void saveRegistry() throws CmsException {
        try {
            // get the file
            File xmlFile = new File(m_regFileName);
            BufferedOutputStream os = new BufferedOutputStream(new FileOutputStream(xmlFile));
            A_CmsXmlContent.getXmlParser().getXmlText(m_xmlReg, os, A_OpenCms.getDefaultEncoding());
            // reinit the modules-hashtable
            init(false);
            if (C_LOGGING && A_OpenCms.isLogging(C_OPENCMS_INFO))
                A_OpenCms.log(C_OPENCMS_INFO, "[CmsRegistry] Saved the registry");
                            
        } catch (Exception exc) {
            throw new CmsException("couldn't save registry", CmsException.C_REGISTRY_ERROR, exc);
        }
    }

    /**
     * This method sets the author of the module.
     *
     * @param String the name of the module.
     * @param String the name of the author.
     */
    public void setModuleAuthor(String modulename, String author) throws CmsException {
        setModuleData(modulename, "author", author);
    }
    
    /**
     * This method sets the email of author of the module.
     *
     * @param String the name of the module.
     * @param String the email of author of the module.
     */
    public void setModuleAuthorEmail(String modulename, String email) throws CmsException {
        setModuleData(modulename, "email", email);
    }
    
    /**
     * Sets the create date of the module.
     *
     * @param String the name of the module.
     * @param long the create date of the module.
     */
    public void setModuleCreateDate(String modulname, long createdate) throws CmsException {
        setModuleData(modulname, "creationdate", m_dateFormat.format(new Date(createdate)));
    }
    
    /**
     * Sets the create date of the module.
     *
     * @param String the name of the module.
     * @param String the create date of the module. Format: mm.dd.yyyy
     */
    public void setModuleCreateDate(String modulname, String createdate) throws CmsException {
        setModuleData(modulname, "creationdate", createdate);
    }
    
    /**
     * Private method to set module data like author.
     *
     * @param String modulename the name of the module.
     * @param String dataName the name of the tag to set the data for.
     * @param String the value to be set.
     */
    private void setModuleData(String module, String dataName, String value) throws CmsException {
        if (!hasAccess()) {
            throw new CmsException("No access to perform the action 'setModuleData'", CmsException.C_REGISTRY_ERROR);
        }
        try {
            Element moduleElement = getModuleElement(module);
            Node tag = moduleElement.getElementsByTagName(dataName).item(0);
            setTagValue(tag, value);
            // save the registry
            saveRegistry();
        } catch (Exception exc) {
            // ignore the exception - registry is not wellformed
        }
    }
    
    /**
     * Sets the module dependencies for the module.
     *
     * @param module String the name of the module to check.
     * @param modules Vector in this parameter the names of the dependend modules will be returned.
     * @param minVersions Vector in this parameter the minimum versions of the dependend modules will be returned.
     * @param maxVersions Vector in this parameter the maximum versions of the dependend modules will be returned.
     */
    public void setModuleDependencies(String modulename, Vector modules, Vector minVersions, Vector maxVersions)
        throws CmsException {
        if (!hasAccess()) {
            throw new CmsException(
                "No access to perform the action 'setModuleDependencies'",
                CmsException.C_REGISTRY_ERROR);
        }
        try {
            Element module = getModuleElement(modulename);
            Element dependencies = (Element) (module.getElementsByTagName("dependencies").item(0));
    
            // delete all subnodes
            while (dependencies.hasChildNodes()) {
                dependencies.removeChild(dependencies.getFirstChild());
            }
    
            // create the new dependencies
            for (int i = 0; i < modules.size(); i++) {
                Element dependency = m_xmlReg.createElement("dependency");
                dependencies.appendChild(dependency);
                Element name = m_xmlReg.createElement("name");
                Element min = m_xmlReg.createElement("minversion");
                Element max = m_xmlReg.createElement("maxversion");
                name.appendChild(m_xmlReg.createTextNode(modules.elementAt(i).toString()));
                min.appendChild(m_xmlReg.createTextNode(minVersions.elementAt(i).toString()));
                max.appendChild(m_xmlReg.createTextNode(maxVersions.elementAt(i).toString()));
                dependency.appendChild(name);
                dependency.appendChild(min);
                dependency.appendChild(max);
            }
    
            // save the registry
            saveRegistry();
        } catch (Exception exc) {
            // ignore the exception - reg is not welformed
        }
    }

    /**
     * Sets the description of the module.
     *
     * @param String the name of the module.
     * @param String the description of the module.
     */
    public void setModuleDescription(String module, String description) throws CmsException {
        setModuleData(module, "description", description);
    }
    
    /**
     * Sets the url to the documentation of the module.
     *
     * @param String the name of the module.
     * @param java.lang.String the url to the documentation of the module.
     */
    public void setModuleDocumentPath(String modulename, String url) throws CmsException {
        setModuleData(modulename, "documentation", url);
    }
    
    /**
     * Sets the classname, that receives all maintenance-events for the module.
     *
     * @param String the name of the module.
     * @param String the name of the class that receives all maintenance-events for the module.
     */
    public void setModuleMaintenanceEventClass(String modulname, String classname) throws CmsException {
        setModuleData(modulname, "maintenance_class", classname);
    }

    /**
     * Sets the classname, that contains the publish method of the module.
     *
     * @param String the name of the module.
     * @param String the name of the class that contains the publish method of the module.
     */
    public void setModulePublishClass(String modulname, String classname) throws CmsException {
        try {
            Element module = getModuleElement(modulname);
            Element pubClass = (Element) (module.getElementsByTagName("publishclass").item(0));

            // delete all subnodes
            while (pubClass.hasChildNodes()) {
                pubClass.removeChild(pubClass.getFirstChild());
            }

            // create the new repository
            Element path = m_xmlReg.createElement("name");
            path.appendChild(m_xmlReg.createTextNode(classname));
            pubClass.appendChild(path);

            // save the registry
            saveRegistry();
        } catch (Exception exc) {
            // ignore the exception - reg is not welformed
        }
    }

    /**
     * Sets the description of the module.
     *
     * @param String the name of the module.
     * @param String the nice name of the module.
     */
    public void setModuleNiceName(String module, String nicename) throws CmsException {
        setModuleData(module, "nicename", nicename);
    }
    
    /**
     * Sets a parameter for a module.
     *
     * @param modulename java.lang.String the name of the module.
     * @param parameter java.lang.String the name of the parameter to set.
     * @param the value to set for the parameter.
     */
    public void setModuleParameter(String modulename, String parameter, byte value) throws CmsException {
        setModuleParameter(modulename, parameter, value + "");
    }
    
    /**
     * Sets a parameter for a module.
     *
     * @param modulename java.lang.String the name of the module.
     * @param parameter java.lang.String the name of the parameter to set.
     * @param the value to set for the parameter.
     */
    public void setModuleParameter(String modulename, String parameter, double value) throws CmsException {
        setModuleParameter(modulename, parameter, value + "");
    }
    
    /**
     * Sets a parameter for a module.
     *
     * @param modulename java.lang.String the name of the module.
     * @param parameter java.lang.String the name of the parameter to set.
     * @param the value to set for the parameter.
     */
    public void setModuleParameter(String modulename, String parameter, float value) throws CmsException {
        setModuleParameter(modulename, parameter, value + "");
    }
    
    /**
     * Sets a parameter for a module.
     *
     * @param modulename java.lang.String the name of the module.
     * @param parameter java.lang.String the name of the parameter to set.
     * @param the value to set for the parameter.
     */
    public void setModuleParameter(String modulename, String parameter, int value) throws CmsException {
        setModuleParameter(modulename, parameter, value + "");
    }
    
    /**
     * Sets a parameter for a module.
     *
     * @param modulename java.lang.String the name of the module.
     * @param parameter java.lang.String the name of the parameter to set.
     * @param the value to set for the parameter.
     */
    public void setModuleParameter(String modulename, String parameter, long value) throws CmsException {
        setModuleParameter(modulename, parameter, value + "");
    }
    
    /**
     * Sets a parameter for a module.
     *
     * @param modulename java.lang.String the name of the module.
     * @param parameter java.lang.String the name of the parameter to set.
     * @param the value to set for the parameter.
     */
    public void setModuleParameter(String modulename, String parameter, Boolean value) throws CmsException {
        setModuleParameter(modulename, parameter, value.toString());
    }
    
    /**
     * Sets a parameter for a module.
     *
     * @param modulename java.lang.String the name of the module.
     * @param parameter java.lang.String the name of the parameter to set.
     * @param the value to set for the parameter.
     */
    public void setModuleParameter(String modulename, String parameter, Byte value) throws CmsException {
        setModuleParameter(modulename, parameter, value.toString());
    }
    
    /**
     * Sets a parameter for a module.
     *
     * @param modulename java.lang.String the name of the module.
     * @param parameter java.lang.String the name of the parameter to set.
     * @param the value to set for the parameter.
     */
    public void setModuleParameter(String modulename, String parameter, Double value) throws CmsException {
        setModuleParameter(modulename, parameter, value.toString());
    }
    
    /**
     * Sets a parameter for a module.
     *
     * @param modulename java.lang.String the name of the module.
     * @param parameter java.lang.String the name of the parameter to set.
     * @param the value to set for the parameter.
     */
    public void setModuleParameter(String modulename, String parameter, Float value) throws CmsException {
        setModuleParameter(modulename, parameter, value.toString());
    }
    
    /**
     * Sets a parameter for a module.
     *
     * @param modulename java.lang.String the name of the module.
     * @param parameter java.lang.String the name of the parameter to set.
     * @param the value to set for the parameter.
     */
    public void setModuleParameter(String modulename, String parameter, Integer value) throws CmsException {
        setModuleParameter(modulename, parameter, value.toString());
    }
    
    /**
     * Sets a parameter for a module.
     *
     * @param modulename java.lang.String the name of the module.
     * @param parameter java.lang.String the name of the parameter to set.
     * @param the value to set for the parameter.
     */
    public void setModuleParameter(String modulename, String parameter, Long value) throws CmsException {
        setModuleParameter(modulename, parameter, value.toString());
    }
    
    /**
     * Sets a parameter for a module.
     *
     * @param modulename java.lang.String the name of the module.
     * @param parameter java.lang.String the name of the parameter to set.
     * @param value java.lang.String the value to set for the parameter.
     */
    public void setModuleParameter(String modulename, String parameter, String value) throws CmsException {
        // check if the user is allowed to set parameters
    
        if (!hasAccess()) {
            throw new CmsException("No access to perform the action 'setModuleParameter'", CmsException.C_REGISTRY_ERROR);
        }
        try {
            Element param = getModuleParameterElement(modulename, parameter);
            if (!checkType(getModuleParameterType(modulename, parameter), value)) {
                throw new CmsException(
                    "wrong number format for " + parameter + " -> " + value,
                    CmsException.C_REGISTRY_ERROR);
            }
            param.getElementsByTagName("value").item(0).getFirstChild().setNodeValue(value);
            saveRegistry();
    
            // try to invoke the event-method for setting parameters on this class.
            Class eventClass = getModuleMaintenanceEventClass(modulename);
            try {
                Class declaration[] = { CmsObject.class };
                Object arguments[] = { m_cms };
                Method eventMethod = eventClass.getMethod(C_UPDATE_PARAMETER_EVENT_METHOD_NAME, declaration);
                eventMethod.invoke(null, arguments);
            } catch (Exception exc) {
                // ignore the exception.
            }
    
        } catch (CmsException exc) {
            throw exc;
        } catch (Exception exc) {
            throw new CmsException(
                "couldn't set parameter " + parameter + " for module " + modulename + " to vale " + value,
                CmsException.C_REGISTRY_ERROR,
                exc);
        }
    }
    
    /**
     * Sets a parameter for a module.
     *
     * @param modulename java.lang.String the name of the module.
     * @param parameter java.lang.String the name of the parameter to set.
     * @param the value to set for the parameter.
     */
    public void setModuleParameter(String modulename, String parameter, boolean value) throws CmsException {
        setModuleParameter(modulename, parameter, value + "");
    }
    
    /**
     * Sets the module dependencies for the module.
     *
     * @param module String the name of the module to check.
     * @param names Vector with parameternames
     * @param descriptions Vector with parameterdescriptions
     * @param types Vector with parametertypes (string, float,...)
     * @param values Vector with defaultvalues for parameters
     */
    public void setModuleParameterdef(String modulename, Vector names, Vector descriptions, Vector types, Vector values)
        throws CmsException {
        if (!hasAccess()) {
            throw new CmsException(
                "No access to perform the action 'setModuleParameterdef'",
                CmsException.C_REGISTRY_ERROR);
        }
        try {
            Element module = getModuleElement(modulename);
            Element params = (Element) (module.getElementsByTagName("parameters").item(0));
            // delete all subnodes
            while (params.hasChildNodes()) {
                params.removeChild(params.getFirstChild());
            }
    
            // create the new parameters
            for (int i = 0; i < names.size(); i++) {
                Element para = m_xmlReg.createElement("para");
                params.appendChild(para);
                Element name = m_xmlReg.createElement("name");
                Element desc = m_xmlReg.createElement("description");
                Element type = m_xmlReg.createElement("type");
                Element value = m_xmlReg.createElement("value");
                name.appendChild(m_xmlReg.createTextNode(names.elementAt(i).toString()));
                desc.appendChild(m_xmlReg.createCDATASection(descriptions.elementAt(i).toString()));
                type.appendChild(m_xmlReg.createTextNode(types.elementAt(i).toString()));
                value.appendChild(m_xmlReg.createTextNode(values.elementAt(i).toString()));
                para.appendChild(name);
                para.appendChild(desc);
                para.appendChild(type);
                para.appendChild(value);
            }
    
            // save the registry
            saveRegistry();
    
            // try to invoke the event-method for setting parameters on this class.
            Class eventClass = getModuleMaintenanceEventClass(modulename);
            try {
                Class declaration[] = { CmsObject.class };
                Object arguments[] = { m_cms };
                Method eventMethod = eventClass.getMethod(C_UPDATE_PARAMETER_EVENT_METHOD_NAME, declaration);
                eventMethod.invoke(null, arguments);
            } catch (Exception exc) {
                // ignore the exception.
            }
        } catch (Exception exc) {
            // ignore the exception - reg is not welformed
        }
    }

    /**
     * Sets all repositories for a module.
     *
     * @param String modulname the name of the module.
     * @param String[] the reprositories of a module.
     */
    public void setModuleRepositories(String modulename, String[] repositories) throws CmsException {
        if (!hasAccess()) {
            throw new CmsException(
                "No access to perform the action 'setModuleRepositories'",
                CmsException.C_REGISTRY_ERROR);
        }
        try {
            Element module = getModuleElement(modulename);
            Element repository = (Element) (module.getElementsByTagName("repository").item(0));
    
            // delete all subnodes
            while (repository.hasChildNodes()) {
                repository.removeChild(repository.getFirstChild());
            }
    
            // create the new repository
            for (int i = 0; i < repositories.length; i++) {
                Element path = m_xmlReg.createElement("path");
                path.appendChild(m_xmlReg.createTextNode(repositories[i]));
                repository.appendChild(path);
            }
    
            // save the registry
            saveRegistry();
        } catch (Exception exc) {
            // ignore the exception - reg is not welformed
        }
    }

    /**
     * This method sets the version of the module.
     *
     * @param modulename the name of the module.
     * @param version the version of the module.
     */
    public void setModuleVersion(String modulename, String version) throws CmsException {
        if (version == null)
            version = "0";
        try {
            Float.parseFloat(version);
        } catch (NumberFormatException nf) {
            throw new CmsException("Module version '" + version + "' not a number", CmsException.C_UNKNOWN_EXCEPTION, nf);
        }
        version = version.trim();
        setModuleData(modulename, "version", version);
    }

    /**
     * Sets a view for a module
     *
     * @param String the name of the module.
     * @param String the name of the view, that is implemented by the module.
     * @param String the url of the view, that is implemented by the module.
     */
    public void setModuleView(String modulename, String viewname, String viewurl) throws CmsException {
        if (!hasAccess()) {
            throw new CmsException("No access to perform the action 'setModuleView'", CmsException.C_REGISTRY_ERROR);
        }
        try {
            Element module = getModuleElement(modulename);
            Element view = (Element) (module.getElementsByTagName("view").item(0));
            if (!view.hasChildNodes()) {
                view.appendChild(m_xmlReg.createElement("name"));
                view.appendChild(m_xmlReg.createElement("url"));
            }
            setTagValue(view.getElementsByTagName("name").item(0), viewname);
            setTagValue(view.getElementsByTagName("url").item(0), viewurl);
            saveRegistry();
        } catch (Exception exc) {
            // ignore the exception - reg is not welformed
        }
    }

    /**
     * Public method to set system values.
     *
     * @param String dataName the name of the tag to set the data for.
     * @param String the value to be set.
     */
    public void setSystemValue(String dataName, String value) throws CmsException {
        if (!hasAccess()) {
            throw new CmsException("No access to perform the action 'setSystemValue'", CmsException.C_REGISTRY_ERROR);
        }
        try {
            Element systemElement = (Element)m_xmlReg.getElementsByTagName("system").item(0);
            // remove the old tag if it exists and create a new one
            try {
                Node oldTag = systemElement.getElementsByTagName(dataName).item(0);
                oldTag.getParentNode().removeChild(oldTag);
            } catch (Exception exc) {}
            Element newTag = m_xmlReg.createElement(dataName);
            systemElement.appendChild(newTag);
            Node tag = systemElement.getElementsByTagName(dataName).item(0);
            setTagValue(tag, value);
            // save the registry
            saveRegistry();
        } catch (Exception exc) {
            // ignore the exception - registry is not wellformed
        }
    }

    /**
     * Public method to set system values with hashtable.
     *
     * @param String dataName the name of the tag to set the data for.
     * @param Hashtable the value to be set.
     */
    public void setSystemValues(String dataName, Hashtable values) throws CmsException {
        if (!hasAccess()) {
            throw new CmsException("No access to perform the action 'setSystemValues'", CmsException.C_REGISTRY_ERROR);
        }
        try {
            Element systemElement = (Element)m_xmlReg.getElementsByTagName("system").item(0);
            // remove the old tag if it exists and create a new one
            try {
                Node oldTag = systemElement.getElementsByTagName(dataName).item(0);
                oldTag.getParentNode().removeChild(oldTag);
            } catch (Exception exc) {}
            Element newTag = m_xmlReg.createElement(dataName);
            systemElement.appendChild(newTag);
            Node parentTag = systemElement.getElementsByTagName(dataName).item(0);
            Enumeration keys = values.keys();
            while (keys.hasMoreElements()) {
                String key = (String)keys.nextElement();
                String value = (String)values.get(key);
                Element keyTag = m_xmlReg.createElement(key);
                parentTag.appendChild(keyTag);
                keyTag.appendChild(m_xmlReg.createTextNode(value));
            }
            // save the registry
            saveRegistry();
        } catch (Exception exc) {
            // ignore the exception - registry is not wellformed
        }
    }
    
    /**
     * Creates or replaces a textvalue for a parent node.
     * @param Node to set the textvalue.
     * @param String the value to be set.
     */
    private void setTagValue(Node node, String value) {
        if (node.hasChildNodes()) {
            node.getFirstChild().setNodeValue(value);
        } else {
            if ("description".equals(value)) {
                node.appendChild(m_xmlReg.createCDATASection(value));
            } else {
                node.appendChild(m_xmlReg.createTextNode(value));
            }
        }
    }

    /**
     * Returns the value of the "type" node of a module subtree in the registry.
     * @return the value of the "type" node of a module
     */
    public String getModuleType( String theModulename ) {
        String moduleType = null;
        
        try {           
            if ((moduleType=this.getModuleData(theModulename,"type"))==null) {
                // the default type is "traditional"
                moduleType = CmsRegistry.C_MODULE_TYPE_TRADITIONAL;                
            }
        }
        catch (Exception e) {
            // the default type is "traditional"
            moduleType = CmsRegistry.C_MODULE_TYPE_TRADITIONAL;
        }
        
        return moduleType;
    }
    
    /**
     * Sets the type for a given module.<p>
     * 
     * @param theModuleName the name of the module
     * @param theModuleType the new type of the module
     */
    public void setModuleType( String theModulename, String theModuleType ) {
        if (theModuleType==null || theModuleType.equals("")) {
            theModuleType = CmsRegistry.C_MODULE_TYPE_TRADITIONAL;
        }        
        try {
            // for backward compatibility issues: check if the module has already
            // a type node or not, add a type node in this case...            
            Element moduleElement = getModuleElement( theModulename );
            NodeList list = moduleElement.getChildNodes();            
            Node typeNode = null;
            for (int i=0; i<list.getLength(); i++) {
                Element e = (Element) list.item(i);
                if ("type".equals(e.getNodeName())) {
                    typeNode = (Node) e;
                    i = list.getLength();
                }
            }  
                        
            if (typeNode==null) {
                Element newTypeNode = m_xmlReg.createElement( "type" );
                Node firstNode = moduleElement.getFirstChild();
                moduleElement.insertBefore(newTypeNode, firstNode);         
            }      
            
            // now it is save to set the value of the module type node                    
            this.setModuleData( theModulename, "type", theModuleType );
        }
        catch (CmsException e) {
            // we don't have valid permissions for this operation
        }
    }    
}
