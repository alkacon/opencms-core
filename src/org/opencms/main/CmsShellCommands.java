/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/main/CmsShellCommands.java,v $
 * Date   : $Date: 2004/02/20 15:56:44 $
 * Version: $Revision: 1.38 $
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

package org.opencms.main;

import org.opencms.db.I_CmsDriver;
import org.opencms.file.CmsGroup;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsProject;
import org.opencms.file.CmsPropertydefinition;
import org.opencms.file.CmsRegistry;
import org.opencms.file.CmsResource;
import org.opencms.file.CmsUser;
import org.opencms.report.CmsShellReport;
import org.opencms.security.CmsAccessControlEntry;
import org.opencms.security.CmsAccessControlList;
import org.opencms.security.I_CmsPrincipal;
import org.opencms.staticexport.CmsLinkManager;
import org.opencms.util.CmsUUID;
import org.opencms.workplace.I_CmsWpConstants;

import java.io.File;
import java.io.FileInputStream;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.StringTokenizer;
import java.util.Vector;

/**
 * Provides additional commands for the CmsShell.<p>
 * 
 * Such additional commands can access OpenCms functions not available on "regular" OpenCms classes.
 * Also, wrapping methods to access some important functions in the CmsObject that
 * require complex data type parameters are provided.<p>
 * 
 * @author Alexander Kandzior (a.kandzior@alkacon.com)
 * @version $Revision: 1.38 $
 */
class CmsShellCommands {

    /** The OpenCms context object */
    private CmsObject m_cms;
    
    /** The Cms shell object */
    private CmsShell m_shell;
    
    /**
     * Generate a new instance of the command processor.<p>
     * 
     * @param shell the CmsShell that for this command processor
     * @param cms an initilized context object
     * @throws Exception if something goes wrong
     */
    protected CmsShellCommands(CmsShell shell, CmsObject cms) throws Exception {
        m_shell = shell;
        m_cms = cms;
        // print the version information
        version();
        // print the copyright message
        copyright();
        // print the help information
        help();
    }

    /**
     * Adds a user.<p>
     *
     * @param name the name of the new user
     * @param password the password 
     * @param group the default group name
     * @param description the description
     * @return the created user
     * @throws Exception if something goes wrong
     * @see CmsObject#addUser(String, String, String, String, Hashtable)
     */
    public CmsUser addUser(String name, String password, String group, String description) throws Exception {
        return m_cms.addUser(name, password, group, description, new Hashtable());
    }

    /**
     * Adds a user with some additional information.<p>
     *
     * @param name the name of the new user
     * @param password the password 
     * @param group the default group name
     * @param description the description
     * @param firstname the users first name 
     * @param lastname the users he last name
     * @param email the users email address
     * @return the created user
     * @throws Exception if something goes wrong
     * @see CmsObject#addUser(String, String, String, String, Hashtable)
     */
    public CmsUser addUser(String name, String password, String group, String description, String firstname, String lastname, String email) throws Exception {
        CmsUser user = m_cms.addUser(name, password, group, description, new Hashtable());
        user.setEmail(email);
        user.setFirstname(firstname);
        user.setLastname(lastname);
        m_cms.writeUser(user);
        return user;
    }

    /**
     * Adds a web user.<p>
     *
     * @param name the name of the new web user
     * @param password the password 
     * @param group the default group name
     * @param description the description
     * @return the created user
     * @throws Exception if something goes wrong
     * @see CmsObject#addWebUser(String, String, String, String, Hashtable)
     */
    public CmsUser addWebUser(String name, String password, String group, String description) throws Exception {
        return m_cms.addWebUser(name, password, group, description, new Hashtable());
    }
    
    /**
     * Changes the current folder (i.e. the URI in the VFS).<p>
     * 
     * @param target the new URI
     * @throws Exception if something goes wrong
     * @see org.opencms.file.CmsRequestContext#setUri(String)
     */
    public void cd(String target) throws Exception {
        String folder = m_cms.readAbsolutePath(m_cms.getRequestContext().currentFolder());
        if (! target.endsWith("/")) {
            target += "/";
        }
        String resolvedTarget = CmsLinkManager.getAbsoluteUri(target, folder);
        CmsResource res = m_cms.readFileHeader(resolvedTarget);
        if (! res.isFolder()) {
            throw new Exception("Not a folder: " + resolvedTarget);
        }
        m_cms.getRequestContext().setUri(resolvedTarget);
        System.out.println("\nThe current folder is now '" + resolvedTarget + "'"); 
        System.out.println();        
    }

    /**
     * Prints the OpenCms copyright information.<p>
     */
    public void copyright() {
        String[] copy = I_CmsConstants.C_COPYRIGHT;
        for (int i = 0; i < copy.length; i++) {
            System.out.println(copy[i]);
        }
    }

    /**
     * Creates a default project.<p>
     * 
     * This created project has the following properties:<ul>
     * <li>The users groups is the default user group
     * <li>The project managers group is the default project manager group
     * <li>All resources are contained in the project
     * <li>The project will remain after publishing</ul>
     * 
     * @param name the name of the project to create
     * @param description the description for the new project
     * @throws Exception if something goes wrong
     */
    public void createDefaultProject(String name, String description) throws Exception {
        m_cms.getRequestContext().saveSiteRoot();
        m_cms.getRequestContext().setSiteRoot("/");
        try {
            CmsProject project = m_cms.createProject(
                name, 
                description, 
                OpenCms.getDefaultUsers().getGroupUsers(), 
                OpenCms.getDefaultUsers().getGroupProjectmanagers(), 
                I_CmsConstants.C_PROJECT_TYPE_NORMAL
            );
            m_cms.getRequestContext().setCurrentProject(project.getId());
            m_cms.copyResourceToProject("/");
        } finally {
            m_cms.getRequestContext().restoreSiteRoot();
        }
    }

    /**
     * Creates a group.<p>
     *
     * @param name the name of the new group
     * @param description the description of the new group
     * @return the created group
     * @throws Exception if something goes wrong
     * @see CmsObject#createGroup(String, String, int, String)
     */
    public CmsGroup createGroup(String name, String description) throws Exception {
        return m_cms.createGroup(name, description, I_CmsConstants.C_FLAG_ENABLED, null);
    }

    /**
     * Creates a property definition for the given  resource type.<p>
     *
     * @param name the name of the property definition to create
     * @param resourcetype the name of the resource type for the property definition
     * @return the created property definition
     * @throws Exception if something goes wrong
     * @see CmsObject#createPropertydefinition(String, int)
     */
    public CmsPropertydefinition createPropertydefinition(String resourcetype, String name) throws Exception {
        return m_cms.createPropertydefinition(name, m_cms.getResourceTypeId(resourcetype));
    }

    /**
     * Deletes the versions from the backup tables that are older then the given weeks.<p>
     * 
     * @param weeks a numer of weeks, all older backups are deleted
     * @throws Exception if something goes wrong
     * @see CmsObject#deleteBackups(long, int, org.opencms.report.I_CmsReport)
     */
    public void deleteBackups(int weeks) throws Exception {
        long oneWeek = 604800000;
        long maxDate = System.currentTimeMillis() - (weeks * oneWeek);
        m_cms.deleteBackups(maxDate, 100, new CmsShellReport());
    }
    
    /**
     * Delete a property definition for a resource type.<p>
     *
     * @param name the name of the property definition to delete
     * @param resourcetype the name of the resource type where the property definition should be deleted
     * @throws Exception if something goes wrong
     * @see CmsObject#deletePropertydefinition(String, int)
     */
    public void deletepropertydefinition(String name, String resourcetype) throws Exception {
        m_cms.deletePropertydefinition(name, m_cms.getResourceTypeId(resourcetype));
    }

    /**
     * Turns the echo status for the shell on or off.<p>
     *
     * @param echo if "on", echo is turned on, otherwise echo is turned off
     */
    public void echo(String echo) {
        if (echo == null) {
            return;
        }
        boolean b = "on".equalsIgnoreCase(echo.trim());
        m_shell.setEcho(b);
        System.out.println("Echo is now " + (b?"on":"off"));
    }

    /**
     * Exits the shell.<p>
     */
    public void exit() {    
        System.out.println();
        System.out.println("Goodbye!");
        m_shell.exit();
    } 
    
    /**
     * Exports all resources from the current site root to a ZIP file.<p>
     *
     * @param exportFile the name (absolute path) of the ZIP file to export to 
     * @throws Exception if something goes wrong
     * @see CmsObject#exportResources(String, String[], boolean, boolean)
     */
    public void exportAllResources(String exportFile) throws Exception {
        String[] exportPaths = {I_CmsConstants.C_ROOT};
        m_cms.exportResources(exportFile, exportPaths, false, false);
    }

    /**
     * Exports a list of resources from the current site root to a ZIP file.<p>
     * 
     * The resource names in the list must be separated with a ";".<p>
     *
     * @param exportFile the name (absolute path) of the ZIP file to export to 
     * @param pathList the list of resource to export, separated with a ";"
     * @throws Exception if something goes wrong
     * @see CmsObject#exportResources(String, String[], boolean, boolean)
     */
    public void exportResources(String exportFile, String pathList) throws Exception {
        StringTokenizer tok = new StringTokenizer(pathList, ";");
        Vector paths = new Vector();
        while (tok.hasMoreTokens()) {
            paths.addElement(tok.nextToken());
        }
        String exportPaths[] = new String[paths.size()];
        for (int i = 0; i < paths.size(); i++) {
            exportPaths[i] = (String)paths.elementAt(i);
        }
        boolean excludeSystem = true;
        if (pathList.startsWith(I_CmsWpConstants.C_VFS_PATH_SYSTEM) || (pathList.indexOf(";" + I_CmsWpConstants.C_VFS_PATH_SYSTEM) > -1)) {
            excludeSystem = false;
        }
        m_cms.exportResources(exportFile, exportPaths, excludeSystem, false);
    }

    /**
     * Exports a list of resources from the current site root and the user data to a ZIP file.<p>
     * 
     * The resource names in the list must be separated with a ";".<p>
     *
     * @param exportFile the name (absolute path) of the ZIP file to export to 
     * @param pathList the list of resource to export, separated with a ";"
     * @throws Exception if something goes wrong
     * @see CmsObject#exportResources(String, String[], boolean, boolean, boolean)
     */
    public void exportResourcesAndUserdata(String exportFile, String pathList) throws Exception {
        StringTokenizer tok = new StringTokenizer(pathList, ";");
        Vector paths = new Vector();
        while (tok.hasMoreTokens()) {
            paths.addElement(tok.nextToken());
        }
        String exportPaths[] = new String[paths.size()];
        for (int i = 0; i < paths.size(); i++) {
            exportPaths[i] = (String)paths.elementAt(i);
        }
        boolean excludeSystem = true;
        if (pathList.startsWith(I_CmsWpConstants.C_VFS_PATH_SYSTEM) || (pathList.indexOf(";" + I_CmsWpConstants.C_VFS_PATH_SYSTEM) > -1)) {
            excludeSystem = false;
        }
        m_cms.exportResources(exportFile, exportPaths, excludeSystem, false, true);
    }
    
    /**
     * Displays the access control list of a given resource.<p>
     * 
     * @param resourceName the name of the resource
     * @throws Exception if something goes wrong
     * @see CmsObject#getAccessControlList(String)
     */
    public void getAcl(String resourceName) throws Exception {
        CmsAccessControlList acList = m_cms.getAccessControlList(resourceName);
        Iterator principals = acList.getPrincipals().iterator();
        while (principals.hasNext()) {
            I_CmsPrincipal p = m_cms.lookupPrincipal((CmsUUID)principals.next());
            System.out.println(p.getName() + ": " + acList.getPermissions(p).getPermissionString());
        }
    }
    
    /**
     * Displays further information about a driver class.<p>
     * 
     * @param driverName the driver class name to display more information for
     */
    public void getDriverInfo(String driverName) {
        Map drivers = m_cms.getDrivers();
        System.out.println(((I_CmsDriver)drivers.get(driverName)).toString());
    }    

    /**
     * Provides help information for the CmsShell.<p>
     */
    public void help() {
        System.out.println();
        System.out.println("help              Shows this text");
        System.out.println("help *            Shows the signatures of all available methods");
        System.out.println("help {string}     Shows the signatures of all methods containing this string");
        System.out.println("exit or quit      Leaves this OpenCms Shell");
        System.out.println();
    }

    /**
     * Executes the given help command.<p>
     *
     * @param command the help command to execute
     */
    public void help(String command) {
        if ("*".equalsIgnoreCase(command)) {
            m_shell.help(null);
        } else if ("help".equalsIgnoreCase(command)) {
            help();
        } else {
            m_shell.help(command);
        }
    }

    /**
     * Imports a module.<p>
     *
     * @param importFile the absolute path of the import module file
     * @throws Exception if something goes wrong
     * @see CmsRegistry#importModule(String, Vector, org.opencms.report.I_CmsReport)
     */
    public void importModule(String importFile) throws Exception {
        CmsRegistry reg = m_cms.getRegistry();
        reg.importModule(importFile, new Vector(), new CmsShellReport());
    }

    /**
     * Imports a module (zipfile) from the default module directory, 
     * creating a temporary project for this.<p>
     *
     * @param importFile the name of the import module located in the default module directory
     * @throws Exception if something goes wrong
     * @see CmsRegistry#importModule(String, Vector, org.opencms.report.I_CmsReport)
     */
    public void importModuleFromDefault(String importFile) throws Exception {
        // build the complete filename
        String exportPath = null;
        exportPath = m_cms.readPackagePath();
        String fileName = OpenCms.getSystemInfo().getAbsolutePathRelativeToWebInf(exportPath + CmsRegistry.C_MODULE_PATH + importFile);
        // import the module
        System.out.println("Importing module: " + fileName);
        // create a temporary project for the import
        CmsProject project = m_cms.createProject(
            "ModuleImport", 
            "A temporary project to import the module " + importFile, 
            OpenCms.getDefaultUsers().getGroupAdministrators(), 
            OpenCms.getDefaultUsers().getGroupAdministrators(), 
            I_CmsConstants.C_PROJECT_TYPE_TEMPORARY
        );
        int id = project.getId();
        m_cms.getRequestContext().setCurrentProject(id);
        m_cms.getRequestContext().saveSiteRoot();
        m_cms.getRequestContext().setSiteRoot("/");
        m_cms.copyResourceToProject("/");
        m_cms.getRequestContext().restoreSiteRoot();
        // import the module
        CmsRegistry reg = m_cms.getRegistry();
        reg.importModule(fileName, new Vector(), new CmsShellReport());
        // finally publish the project
        m_cms.unlockProject(id);
        m_cms.publishProject();
    }

    /**
     * Imports a folder or a ZIP file to the root folder of the 
     * current site, creating a temporary project for this.<p>
     * 
     * @param importFile the absolute path of the import resource
     * @throws Exception if something goes wrong
     */
    public void importResourcesWithTempProject(String importFile) throws Exception {
        CmsProject project = m_cms.createProject(
            "SystemUpdate", 
            "A temporary project for a system update", 
            OpenCms.getDefaultUsers().getGroupAdministrators(), 
            OpenCms.getDefaultUsers().getGroupAdministrators(), 
            I_CmsConstants.C_PROJECT_TYPE_TEMPORARY
        );
        int id = project.getId();
        m_cms.getRequestContext().setCurrentProject(id);
        m_cms.copyResourceToProject(I_CmsConstants.C_ROOT);
        m_cms.importResources(importFile, I_CmsConstants.C_ROOT);
        m_cms.unlockProject(id);
        m_cms.publishProject();
    }

    /**
     * Checks if the current CmsSell user is a member of the project manager group.<p>
     * 
     * @return true if the user is a project manager
     * @throws Exception if something goes wrong
     */
    public boolean isProjectManager() throws Exception {
        return m_cms.getRequestContext().isProjectManager();
    }

    /**
     * Log a user in to the the CmsSell.<p>
     *
     * @param username the name of the user to log in
     * @param password the password of the user
     */
    public void login(String username, String password) {
        try {
            m_cms.loginUser(username, password);
            System.out.println("You are now logged in as user '" + whoami().getName() + "'.");
        } catch (Exception exc) {
            System.out.println("Login failed!");
        }
    }
    
    /**
     * Displays a list of all resources in the current folder.<p>
     * 
     * @throws Exception if something goes wrong
     * @see CmsObject#getResourcesInFolder(String)
     */
    public void ls() throws Exception {
        String folder = m_cms.readAbsolutePath(m_cms.getRequestContext().currentFolder());
        Vector v = m_cms.getResourcesInFolder(folder);
        System.out.println("\nThe current folder '" + folder + "' contains " + v.size() + " resources");
        Iterator i = v.iterator();
        while (i.hasNext()) {
            CmsResource r = (CmsResource)i.next();
            System.out.println(m_cms.readAbsolutePath(r));
        }
        System.out.println();
    }

    /**
     * Lists the access control entries of a given resource.<p>
     * 
     * @param resourceName the name of the resource
     * @throws Exception if something goes wrong
     */
    public void lsacc(String resourceName) throws Exception {
        Vector acList = m_cms.getAccessControlEntries(resourceName);
        for (int i = 0; i < acList.size(); i++) {
            CmsAccessControlEntry ace = (CmsAccessControlEntry)acList.elementAt(i);
            I_CmsPrincipal acePrincipal = m_cms.lookupPrincipal(ace.getPrincipal());
            if (true) {
                String pName = (acePrincipal != null) ? acePrincipal.getName() : ace.getPrincipal().toString();
                System.out.println(pName + ": " + ace.getPermissions().getPermissionString() + " " + ace);
            }
        }
    }

    /**
     * Lists the access control entries belonging to the given principal.<p>
     *  
     * @param resourceName the name of the resource
     * @param principalName the name of the principal
     * @throws Exception if something goes wrong
     */
    public void lsacc(String resourceName, String principalName) throws Exception {
        I_CmsPrincipal principal = m_cms.lookupPrincipal(principalName);
        Vector acList = m_cms.getAccessControlEntries(resourceName);
        for (int i = 0; i < acList.size(); i++) {
            CmsAccessControlEntry ace = (CmsAccessControlEntry)acList.elementAt(i);
            I_CmsPrincipal acePrincipal = m_cms.lookupPrincipal(ace.getPrincipal());
            if (acePrincipal.equals(principal)) {
                String pName = (acePrincipal != null) ? acePrincipal.getName() : ace.getPrincipal().toString();
                System.out.println(pName + ": " + ace.getPermissions().getPermissionString() + " " + ace);
            }
        }
    }

    /**
     * Does performance measurements of the OpenCms core.<p>
     * 
     * @throws Exception if something goes wrong
     */
    public void perf() throws Exception {        
        int maxTests = 50000;
        m_cms.getRequestContext().saveSiteRoot();
        m_cms.getRequestContext().setSiteRoot("/");
        try {       
            Random random = new Random();            
            List testResources = m_cms.getResourcesInTimeRange("/", 0, System.currentTimeMillis());
            int resourceCount = testResources.size();
            System.out.println("#Resources:\t" + resourceCount);
            long start, time;            
            long totalTime = 0;
            long minTime = Long.MAX_VALUE;
            long maxTime = Long.MIN_VALUE;
            System.out.print("readFileHeader:\t");
            for (int i = maxTests; i > 0; --i) {
                int index = random.nextInt(resourceCount);
                CmsResource resource = (CmsResource)testResources.get(index);
                start = System.currentTimeMillis();               
                m_cms.readFileHeader(m_cms.readAbsolutePath(resource), true);
                time = System.currentTimeMillis() - start;
                totalTime += time;
                if (time < minTime) {
                    minTime = time;
                }
                if (time > maxTime) {
                    maxTime = time;
                }
                if ((i % 100) == 0) {
                    System.out.print(".");
                }
            }
            System.out.println("\nreadFileHeader:\t" + minTime + "\t" + maxTime + "\t" + (((float)totalTime) / maxTests) + " ms");
            
        } finally {
            m_cms.getRequestContext().restoreSiteRoot();
        }
    }

    /**
     * Sets the current shell prompt.<p>
     *
     * @param prompt the prompt to set
     * @see CmsShell#setPrompt(String)
     */
    public void prompt(String prompt) {
        m_shell.setPrompt(prompt);
    }

    /**
     * Publishes a project, unlocks the project first.<p>
     *
     * @param id the id of the project to be published
     * @throws Exception if something goes wrong
     * @see CmsObject#publishProject()
     */
    public void publishProject(int id) throws Exception {
        m_cms.unlockProject(id);
        m_cms.publishProject();
    }
    
    /**
     * Returns the current folder set as URI in the request context.<p>
     * 
     * @return the current folder
     * @throws Exception if something goes wrong
     * @see org.opencms.file.CmsRequestContext#currentFolder()
     */
    public String pwd() throws Exception {
        return m_cms.readAbsolutePath(m_cms.getRequestContext().currentFolder());
    }

    /**
     * Exits the shell.<p>
     * 
     * @see #exit()
     */
    public void quit() {
        exit();
    }

    /**
     * Returns the users group of a project.<p>
     * 
     * @param project the id of the project to return the users group for
     * @return the users group of the project
     * @throws Exception if something goes wrong
     */
    public CmsGroup readGroupOfProject(int project) throws Exception {
        return m_cms.readGroup(m_cms.readProject(project));
    }

    /**
     * Returns the manager group of a project.<p>
     * 
     * @param project the id of the project to return the manager group for
     * @return the manager group of the project
     * @throws Exception if something goes wrong
     */
    public CmsGroup readManagerGroup(int project) throws Exception {
        return m_cms.readManagerGroup(m_cms.readProject(project));
    }

    /**
     * Returns the owner of a project.<p>
     * 
     * @param project the id of the project
     * @return the owner of the project
     * @throws Exception if something goes wrong
     */
    public CmsUser readOwnerOfProject(int project) throws Exception {
        return m_cms.readOwner(m_cms.readProject(project));
    }

    /**
     * Updates all configured search indexes.<p>
     * 
     * @throws Exception if something goes wrong
     * @see org.opencms.search.CmsSearchManager#updateIndex(org.opencms.report.I_CmsReport)
     */
    public void updateIndex() throws Exception {
        OpenCms.getSearchManager().updateIndex(new CmsShellReport(), true);
    }
    
    /**
     * Updates the given search index.<p>
     * 
     * @param index name of the index to update
     * @throws Exception if something goes wrong
     * @see org.opencms.search.CmsSearchManager#updateIndex(String, org.opencms.report.I_CmsReport)
     */
    public void updateIndex(String index) throws Exception {
        OpenCms.getSearchManager().updateIndex(index, new CmsShellReport());
    }
    
    /**
     * Loads a file from the "real" file system to the VFS.<p>
     *
     * @param lokalfile the file upload
     * @param folder the folder in the VFS to place the file into
     * @param filename the name of the uploaded file in the VFS
     * @param type the type of the new file in the VFS
     * @return the createed file
     * @throws Exception if something goes wrong
     */
    public CmsResource uploadFile(String lokalfile, String folder, String filename, String type) throws Exception {
        return m_cms.createResource(folder, filename, m_cms.getResourceTypeId(type), null, importFile(lokalfile));
    }
    
    /**
     * Returns the version information for this OpenCms instance.<p>
     */
    public void version() {
        System.out.println();
        System.out.println("This is OpenCms " + OpenCms.getSystemInfo().getVersionName());
    }

    /**
     * Returns the current user.<p>
     * 
     * @return the current user
     */
    public CmsUser whoami() {
        return m_cms.getRequestContext().currentUser();
    }

    /**
     * Reads a given file from the local harddisk and imports
     * it to the OpenCms system.<p>
     *
     * @param filename file to be uploaded
     * @return the file content
     * @throws CmsException if something goes wrong
     */
    private byte[] importFile(String filename) throws CmsException {
        File file = null;
        long len = 0;
        FileInputStream importInput = null;
        byte[] result;
        // try to load the file
        try {
            file = new File(filename);
        } catch (Exception e) {
            file = null;
        }
        if (file == null) {
            throw new CmsException("Could not load local file " + filename, CmsException.C_NOT_FOUND);
        }
        // now import the file
        try {
            len = file.length();
            result = new byte[(int)len];
            importInput = new FileInputStream(file);
            importInput.read(result);
            importInput.close();
        } catch (Exception e) {
            throw new CmsException(e.toString(), CmsException.C_UNKNOWN_EXCEPTION);
        }
        return result;
    }
}