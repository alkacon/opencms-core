/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/main/CmsShellCommands.java,v $
 * Date   : $Date: 2005/06/09 07:58:45 $
 * Version: $Revision: 1.71 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Mananagement System
 *
 * Copyright (C) 2002 - 2005 Alkacon Software (http://www.alkacon.com)
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

import org.opencms.db.CmsDbEntryNotFoundException;
import org.opencms.db.CmsUserSettings;
import org.opencms.file.CmsFile;
import org.opencms.file.CmsGroup;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsProject;
import org.opencms.file.CmsPropertyDefinition;
import org.opencms.file.CmsResource;
import org.opencms.file.CmsResourceFilter;
import org.opencms.file.CmsUser;
import org.opencms.file.types.CmsResourceTypeFolder;
import org.opencms.i18n.CmsLocaleManager;
import org.opencms.importexport.CmsVfsImportExportHandler;
import org.opencms.module.CmsModule;
import org.opencms.module.CmsModuleImportExportHandler;
import org.opencms.report.CmsShellReport;
import org.opencms.security.CmsAccessControlEntry;
import org.opencms.security.CmsAccessControlList;
import org.opencms.security.I_CmsPrincipal;
import org.opencms.staticexport.CmsLinkManager;
import org.opencms.util.CmsFileUtil;
import org.opencms.util.CmsUUID;
import org.opencms.workplace.I_CmsWpConstants;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Random;
import java.util.Set;
import java.util.StringTokenizer;

/**
 * Provides additional commands for the CmsShell.<p>
 * 
 * Such additional commands can access OpenCms functions not available on "regular" OpenCms classes.
 * Also, wrapping methods to access some important functions in the CmsObject that
 * require complex data type parameters are provided.<p>
 * 
 * @author Alexander Kandzior (a.kandzior@alkacon.com)
 * @version $Revision: 1.71 $
 */
class CmsShellCommands implements I_CmsShellCommands {

    /** The OpenCms context object. */
    private CmsObject m_cms;
    
    /** The Cms shell object. */
    private CmsShell m_shell;

    /**
     * Generate a new instance of the command processor.<p>
     * 
     * To initilize the command processor, you must call {@link #initShellCmsObject(CmsObject, CmsShell)}.
     * 
     * @see #initShellCmsObject(CmsObject, CmsShell)
     */
    protected CmsShellCommands() {

        // noop
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

        String folder = CmsResource.getFolderPath(m_cms.getRequestContext().getUri());
        if (!target.endsWith("/")) {
            target += "/";
        }
        String resolvedTarget = CmsLinkManager.getAbsoluteUri(target, folder);
        CmsResource res = m_cms.readResource(resolvedTarget);
        if (!res.isFolder()) {
            throw new CmsIllegalArgumentException(Messages.get().container(Messages.ERR_NOT_A_FOLDER_1, resolvedTarget));
        }
        m_cms.getRequestContext().setUri(resolvedTarget);
        System.out.println('\n' + Messages.get().key(
            m_shell.getLocale(),
            Messages.GUI_SHELL_CURRENT_FOLDER_1,
            new Object[] {resolvedTarget}));
        System.out.println();
    }

    /**
     * Changes the access control for a given resource and a given principal(user/group).
     * 
     * @param resourceName name of the resource
     * @param principalType the type of the principal (group or user)
     * @param principalName name of the principal
     * @param permissionString the permissions in the format ((+|-)(r|w|v|c|i))*
     * @throws CmsException if something goes wrong
     * @see CmsObject#chacc(String, String, String, String)
     */
    public void chacc(String resourceName, String principalType, String principalName, String permissionString)
    throws CmsException {

        m_cms.lockResource(resourceName);
        if (I_CmsPrincipal.C_PRINCIPAL_GROUP.equalsIgnoreCase(principalType.trim())) {
            principalName = OpenCms.getImportExportManager().translateGroup(principalName);
        } else {
            principalName = OpenCms.getImportExportManager().translateUser(principalName);
        }
        m_cms.chacc(resourceName, principalType, principalName, permissionString);
    }

    /**
     * Prints the OpenCms copyright information.<p>
     */
    public void copyright() {

        String[] copy = Messages.COPYRIGHT_BY_ALKACON;
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
                I_CmsConstants.C_PROJECT_TYPE_NORMAL);
            m_cms.getRequestContext().setCurrentProject(project);
            m_cms.copyResourceToProject("/");
        } finally {
            m_cms.getRequestContext().restoreSiteRoot();
        }
    }

    /**
     * Creates a new folder in the given target folder.<p>
     * 
     * @param targetFolder the target folder
     * @param folderName the new folder to create in the target folder
     * @return the created folder
     * @throws Exception if somthing goes wrong
     */
    public CmsResource createFolder(String targetFolder, String folderName) throws Exception {

        return m_cms.createResource(targetFolder + folderName, CmsResourceTypeFolder.C_RESOURCE_TYPE_ID);
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
     * @return the created property definition
     * @throws Exception if something goes wrong
     * @see CmsObject#createPropertyDefinition(String)
     */
    public CmsPropertyDefinition createPropertydefinition(String name) throws Exception {

        return m_cms.createPropertyDefinition(name);
    }

    /**
     * Creates a new user.<p>
     * 
     * @param name the name for the new user
     * @param password the password for the new user
     * @param description the description for the new user
     * 
     * @throws Exception if something goes wrong
     * @see CmsObject#createUser(String, String, String, Hashtable)
     * @return the created user
     */
    public CmsUser createUser(String name, String password, String description) throws Exception {

        return m_cms.createUser(name, password, description, new Hashtable());
    }

    /**
     * Creates a user with some additional information.<p>
     *
     * @param name the name of the new user
     * @param password the password 
     * @param description the description
     * @param firstname the users first name 
     * @param lastname the users he last name
     * @param email the users email address
     * @return the created user
     * 
     * @throws Exception if something goes wrong
     * @see CmsObject#createUser(String, String, String, Hashtable)
     */
    public CmsUser createUser(
        String name,
        String password,
        String description,
        String firstname,
        String lastname,
        String email) throws Exception {

        CmsUser user = m_cms.createUser(name, password, description, new Hashtable());
        user.setEmail(email);
        user.setFirstname(firstname);
        user.setLastname(lastname);
        m_cms.writeUser(user);
        return user;
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
     * Delete a property definition for a resource.<p>
     *
     * @param name the name of the property definition to delete
     * @throws Exception if something goes wrong
     * @see CmsObject#deletePropertyDefinition(String)
     */
    public void deletepropertydefinition(String name) throws Exception {

        m_cms.deletePropertyDefinition(name);
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
        if (b) {
            System.out.println(Messages.get().key(Messages.GUI_SHELL_ECHO_ON_0));
        } else {
            System.out.println(Messages.get().key(Messages.GUI_SHELL_ECHO_OFF_0));
        }
    }

    /**
     * Exits the shell.<p>
     */
    public void exit() {

        m_shell.exit();
    }

    /**
     * Exports all resources from the current site root to a ZIP file.<p>
     *
     * @param exportFile the name (absolute path) of the ZIP file to export to 
     * @throws Exception if something goes wrong
     */
    public void exportAllResources(String exportFile) throws Exception {

        List exportPaths = new ArrayList(1);
        exportPaths.add(I_CmsConstants.C_ROOT);

        CmsVfsImportExportHandler vfsExportHandler = new CmsVfsImportExportHandler();
        vfsExportHandler.setFileName(exportFile);
        vfsExportHandler.setExportPaths(exportPaths);
        vfsExportHandler.setExcludeSystem(false);
        vfsExportHandler.setExcludeUnchanged(false);
        vfsExportHandler.setExportUserdata(false);

        OpenCms.getImportExportManager().exportData(m_cms, vfsExportHandler, new CmsShellReport());
    }

    /**
     * Exports the module with the given name to the default location.<p>
     * 
     * @param moduleName the name of the module to export
     * 
     * @throws Exception if something goes wrong
     */
    public void exportModule(String moduleName) throws Exception {

        CmsModule module = OpenCms.getModuleManager().getModule(moduleName);

        if (module == null) {
            throw new CmsDbEntryNotFoundException(Messages.get().container(Messages.ERR_UNKNOWN_MODULE_1, moduleName));
        }

        String filename = OpenCms.getSystemInfo().getAbsoluteRfsPathRelativeToWebInf(
            OpenCms.getSystemInfo().getPackagesRfsPath()
                + I_CmsConstants.C_MODULE_PATH
                + moduleName
                + "_"
                + OpenCms.getModuleManager().getModule(moduleName).getVersion().toString());

        String[] resources = new String[module.getResources().size()];
        System.arraycopy(module.getResources().toArray(), 0, resources, 0, resources.length);

        // generate a module export handler
        CmsModuleImportExportHandler moduleExportHandler = new CmsModuleImportExportHandler();
        moduleExportHandler.setFileName(filename);
        moduleExportHandler.setAdditionalResources(resources);
        moduleExportHandler.setModuleName(module.getName().replace('\\', '/'));
        moduleExportHandler.setDescription(Messages.get().key(
            m_shell.getLocale(),
            Messages.GUI_SHELL_IMPORTEXPORT_MODULE_HANDLER_NAME_1,
            new Object[] {moduleExportHandler.getModuleName()}));

        // export the module
        OpenCms.getImportExportManager().exportData(m_cms, moduleExportHandler, new CmsShellReport());
    }

    /**
     * Exports a list of resources from the current site root to a ZIP file.<p>
     * 
     * The resource names in the list must be separated with a ";".<p>
     *
     * @param exportFile the name (absolute path) of the ZIP file to export to 
     * @param pathList the list of resource to export, separated with a ";"
     * @throws Exception if something goes wrong
     */
    public void exportResources(String exportFile, String pathList) throws Exception {

        StringTokenizer tok = new StringTokenizer(pathList, ";");
        List exportPaths = new ArrayList();
        while (tok.hasMoreTokens()) {
            exportPaths.add(tok.nextToken());
        }
        boolean excludeSystem = true;
        if (pathList.startsWith(I_CmsWpConstants.C_VFS_PATH_SYSTEM)
            || (pathList.indexOf(";" + I_CmsWpConstants.C_VFS_PATH_SYSTEM) > -1)) {
            excludeSystem = false;
        }

        CmsVfsImportExportHandler vfsExportHandler = new CmsVfsImportExportHandler();
        vfsExportHandler.setFileName(exportFile);
        vfsExportHandler.setExportPaths(exportPaths);
        vfsExportHandler.setExcludeSystem(excludeSystem);
        vfsExportHandler.setExcludeUnchanged(false);
        vfsExportHandler.setExportUserdata(false);

        OpenCms.getImportExportManager().exportData(m_cms, vfsExportHandler, new CmsShellReport());
    }

    /**
     * Exports a list of resources from the current site root and the user data to a ZIP file.<p>
     * 
     * The resource names in the list must be separated with a ";".<p>
     *
     * @param exportFile the name (absolute path) of the ZIP file to export to 
     * @param pathList the list of resource to export, separated with a ";"
     * @throws Exception if something goes wrong
     */
    public void exportResourcesAndUserdata(String exportFile, String pathList) throws Exception {

        StringTokenizer tok = new StringTokenizer(pathList, ";");
        List exportPaths = new ArrayList();
        while (tok.hasMoreTokens()) {
            exportPaths.add(tok.nextToken());
        }
        boolean excludeSystem = true;
        if (pathList.startsWith(I_CmsWpConstants.C_VFS_PATH_SYSTEM)
            || (pathList.indexOf(";" + I_CmsWpConstants.C_VFS_PATH_SYSTEM) > -1)) {
            excludeSystem = false;
        }

        CmsVfsImportExportHandler vfsExportHandler = new CmsVfsImportExportHandler();
        vfsExportHandler.setFileName(exportFile);
        vfsExportHandler.setExportPaths(exportPaths);
        vfsExportHandler.setExcludeSystem(excludeSystem);
        vfsExportHandler.setExcludeUnchanged(false);
        vfsExportHandler.setExportUserdata(true);

        OpenCms.getImportExportManager().exportData(m_cms, vfsExportHandler, new CmsShellReport());
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
     * Returns the Locales available on the system ready to use on Method 
     * {@link #setLocale(String)} from the <code>CmsShell</code>. <p>
     * 
     * Note that the full name containing language, country and optional variant seperated 
     * by underscores is returned always but the latter two parts may be left out. <p>
     *
     */
    public void getLocales() {

        System.out.println(Messages.get().key(m_shell.getLocale(), Messages.GUI_SHELL_LOCALES_AVAILABLE_0, null));
        Locale[] locales = Locale.getAvailableLocales();
        for (int i = locales.length - 1; i >= 0; i--) {
            System.out.println("  \"" + locales[i].toString() + "\"");
        }
    }

    /**
     * Provides help information for the CmsShell.<p>
     */
    public void help() {

        Locale locale = m_shell.getLocale();
        System.out.println();
        System.out.println(Messages.get().key(locale, Messages.GUI_SHELL_HELP1_0, null));
        System.out.println(Messages.get().key(locale, Messages.GUI_SHELL_HELP2_0, null));
        System.out.println(Messages.get().key(locale, Messages.GUI_SHELL_HELP3_0, null));
        System.out.println(Messages.get().key(locale, Messages.GUI_SHELL_HELP4_0, null));
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
     * @see org.opencms.importexport.CmsImportExportManager#importData(CmsObject, String, String, org.opencms.report.I_CmsReport)
     */
    public void importModule(String importFile) throws Exception {

        OpenCms.getImportExportManager().importData(m_cms, importFile, null, new CmsShellReport());
    }

    /**
     * Imports a module (zipfile) from the default module directory, 
     * creating a temporary project for this.<p>
     *
     * @param importFile the name of the import module located in the default module directory
     * @throws Exception if something goes wrong
     * @see org.opencms.importexport.CmsImportExportManager#importData(CmsObject, String, String, org.opencms.report.I_CmsReport)
     */
    public void importModuleFromDefault(String importFile) throws Exception {

        String exportPath = OpenCms.getSystemInfo().getPackagesRfsPath();
        String fileName = OpenCms.getSystemInfo().getAbsoluteRfsPathRelativeToWebInf(
            exportPath + I_CmsConstants.C_MODULE_PATH + importFile);
        OpenCms.getImportExportManager().importData(m_cms, fileName, null, new CmsShellReport());
    }

    /**
     * Exists so that the script can run without the wizard, does nothing.<p>
     */
    public void importModulesFromSetupBean() {

        // noop, exists so that the script can run without the wizard
    }

    /**
     * Imports a resource into the Cms.<p>
     * 
     * @param importFile the name (absolute Path) of the import resource (zip or folder)
     * @param importPath the name (absolute Path) of folder in which should be imported
     * @throws Exception if something goes wrong
     */
    public void importResources(String importFile, String importPath) throws Exception {

        OpenCms.getImportExportManager().importData(
            m_cms,
            OpenCms.getSystemInfo().getAbsoluteRfsPathRelativeToWebInf(importFile),
            importPath,
            new CmsShellReport());
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
            Messages.get().key(m_shell.getLocale(), Messages.GUI_SHELL_IMPORT_TEMP_PROJECT_NAME_0, null),
            OpenCms.getDefaultUsers().getGroupAdministrators(),
            OpenCms.getDefaultUsers().getGroupAdministrators(),
            I_CmsConstants.C_PROJECT_TYPE_TEMPORARY);
        int id = project.getId();
        m_cms.getRequestContext().setCurrentProject(project);
        m_cms.copyResourceToProject(I_CmsConstants.C_ROOT);
        OpenCms.getImportExportManager().importData(m_cms, importFile, I_CmsConstants.C_ROOT, new CmsShellReport());
        m_cms.unlockProject(id);
        m_cms.publishProject();
    }

    /**
     * @see org.opencms.main.I_CmsShellCommands#initShellCmsObject(org.opencms.file.CmsObject, org.opencms.main.CmsShell)
     */
    public void initShellCmsObject(CmsObject cms, CmsShell shell) {

        m_cms = cms;
        m_shell = shell;
    }

    /**
     * Displays a list of all currently installed modules.<p>
     * 
     * @throws Exception if something goes wrong
     */
    public void listModules() throws Exception {

        Set modules = OpenCms.getModuleManager().getModuleNames();
        System.out.println("\n"
            + Messages.get().key(
                m_shell.getLocale(),
                Messages.GUI_SHELL_LIST_MODULES_1,
                new Object[] {new Integer(modules.size())}));
        Iterator i = modules.iterator();
        while (i.hasNext()) {
            String moduleName = (String)i.next();
            System.out.println(moduleName);
        }
        System.out.println();
    }

    /**
     * Log a user in to the the CmsSell.<p>
     *
     * @param username the name of the user to log in
     * @param password the password of the user
     */
    public void login(String username, String password) {

        username = OpenCms.getImportExportManager().translateUser(username);
        try {
            m_cms.loginUser(username, password);
            // reset the settings, this will switch the startup site root etc.
            m_shell.initSettings();
            System.out.println(Messages.get().key(
                m_shell.getLocale(),
                Messages.GUI_SHELL_LOGIN_1,
                new Object[] {whoami().getName()}));
        } catch (Exception exc) {
            System.out.println(Messages.get().key(m_shell.getLocale(), Messages.GUI_SHELL_LOGIN_FAILED_0, null));
        }
    }


    /**
     * Displays a list of all resources in the current folder.<p>
     * 
     * @throws Exception if something goes wrong
     * @see CmsObject#getResourcesInFolder(String, CmsResourceFilter)
     */
    public void ls() throws Exception {

        String folder = CmsResource.getFolderPath(m_cms.getRequestContext().getUri());
        List resources = m_cms.getResourcesInFolder(folder, CmsResourceFilter.IGNORE_EXPIRATION);
        System.out.println("\n"
            + Messages.get().key(m_shell.getLocale(), Messages.GUI_SHELL_LS_2, new Object[] {folder, resources}));
        Iterator i = resources.iterator();
        while (i.hasNext()) {
            CmsResource r = (CmsResource)i.next();
            System.out.println(m_cms.getSitePath(r));
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

        List acList = m_cms.getAccessControlEntries(resourceName);
        for (int i = 0; i < acList.size(); i++) {
            CmsAccessControlEntry ace = (CmsAccessControlEntry)acList.get(i);
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
        List acList = m_cms.getAccessControlEntries(resourceName);
        for (int i = 0; i < acList.size(); i++) {
            CmsAccessControlEntry ace = (CmsAccessControlEntry)acList.get(i);
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
                m_cms.readResource(m_cms.getSitePath(resource), CmsResourceFilter.ALL);
                time = System.currentTimeMillis() - start;
                totalTime += time;
                if (time < minTime) {
                    minTime = time;
                }
                if (time > maxTime) {
                    maxTime = time;
                }
                if ((i % 100) == 0) {
                    System.out.print('.');
                }
            }
            System.out.println("\nreadFileHeader:\t"
                + minTime
                + "\t"
                + maxTime
                + "\t"
                + (((float)totalTime) / maxTests)
                + " ms");

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
     * Returns the current folder set as URI in the request context.<p>
     * 
     * @return the current folder
     * @throws Exception if something goes wrong
     * @see org.opencms.file.CmsRequestContext#getUri()
     * @see CmsResource#getFolderPath(String)
     */
    public String pwd() throws Exception {

        return CmsResource.getFolderPath(m_cms.getRequestContext().getUri());
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
     * Returns the selected files contentsls as a String.<p>
     * 
     * @param filename the file to read the contents from
     * @throws CmsException if something goes wrong
     * @return the selected files contents
     */
    public String readFileContent(String filename) throws CmsException {

        filename = CmsLinkManager.getAbsoluteUri(
            filename,
            CmsResource.getFolderPath(m_cms.getRequestContext().getUri()));
        CmsFile file = m_cms.readFile(filename, CmsResourceFilter.IGNORE_EXPIRATION);
        return new String(file.getContents());
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
     * Sets the current project to the provided project id.<p>
     * 
     * @param id the project id to set
     * @return the project set
     * @throws Exception if something goes wrong
     */
    public CmsProject setCurrentProject(int id) throws Exception {

        return m_cms.getRequestContext().setCurrentProject(m_cms.readProject(id));
    }

    /**
     * Sets the current project to the provided project name.<p>
     * 
     * @param name the project name to set
     * @return the project set
     * @throws Exception if something goes wrong
     */
    public CmsProject setCurrentProject(String name) throws Exception {

        return m_cms.getRequestContext().setCurrentProject(m_cms.readProject(name));
    }
    
    /**
     * Set the locale of the current user logged in. <p> 
     * 
     * This method will always set a valid Locale for the current user!
     * If the provided locale name is not valid (i.e. leads to an Exception
     * when trying to create the Locale, then the configured default Locale is set.<p> 
     *
     * The full name must consist of language code, 
     * country code(optional), variant(optional) separated by "_".<p>
     * 
     * @see Locale#getLanguage() 
     * @see Locale#getCountry()
     * @see Locale#getVariant() 
     * @param localeName the full locale name
     *
     * @throws CmsException if something goes wrong
     * 
     */
    public void setLocale(String localeName) throws CmsException {
               
        Locale locale = CmsLocaleManager.getLocale(localeName);

        System.out.println(Messages.get().key(
            m_shell.getLocale(),
            Messages.GUI_SHELL_SETLOCALE_2,
            new Object[] {locale.toString(), m_cms.getRequestContext().currentUser().getName()}));
        
        CmsUserSettings settings = m_shell.getSettings();
        settings.setLocale(locale);
        settings.save(m_cms);        
        System.out.println(Messages.get().key(
            m_shell.getLocale(),
            Messages.GUI_SHELL_SETLOCALE_POST_1,
            new Object[] {locale.toString()}));
    }

    /**
     * @see org.opencms.main.I_CmsShellCommands#shellExit()
     */
    public void shellExit() {

        System.out.println();
        System.out.println(Messages.get().key(m_shell.getLocale(), Messages.GUI_SHELL_GOODBYE_0, null));
    }

    /**
     * @see org.opencms.main.I_CmsShellCommands#shellStart()
     */
    public void shellStart() {

        System.out.println();
        System.out.println(Messages.get().key(m_shell.getLocale(), Messages.GUI_SHELL_WELCOME_0, null));
        System.out.println();

        // print the version information
        version();
        // print the copyright message
        copyright();
        // print the help information
        help();
    }

    /**
     * Unlocks the current project, required before publishing.<p>
     * @throws Exception if something goes wrong
     */
    public void unlockCurrentProject() throws Exception {

        m_cms.unlockProject(m_cms.getRequestContext().currentProject().getId());
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
     * @param localfile the file upload
     * @param folder the folder in the VFS to place the file into
     * @param filename the name of the uploaded file in the VFS
     * @param type the type of the new file in the VFS
     * @return the createed file
     * @throws Exception if something goes wrong
     * @throws CmsIllegalArgumentException if the concatenation of String arguments 
     *         <code>folder</code> and <code>localfile</code> is of length 0
     * 
     */
    public CmsResource uploadFile(String localfile, String folder, String filename, String type)
    throws Exception, CmsIllegalArgumentException {

        int t = OpenCms.getResourceManager().getResourceType(type).getTypeId();
        return m_cms.createResource(folder + filename, t, CmsFileUtil.readFile(localfile), null);
    }

    /**
     * Returns the version information for this OpenCms instance.<p>
     */
    public void version() {

        System.out.println();
        System.out.println(Messages.get().key(
            m_shell.getLocale(),
            Messages.GUI_SHELL_VERSION_1,
            new Object[] {OpenCms.getSystemInfo().getVersionName()}));
    }

    /**
     * Returns the current user.<p>
     * 
     * @return the current user
     */
    public CmsUser whoami() {

        return m_cms.getRequestContext().currentUser();
    }
}