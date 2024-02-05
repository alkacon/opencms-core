/*
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (c) Alkacon Software GmbH & Co. KG (http://www.alkacon.com)
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
 * For further information about Alkacon Software GmbH & Co. KG, please see the
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

import org.opencms.configuration.CmsSitesConfiguration;
import org.opencms.configuration.CmsVariablesConfiguration;
import org.opencms.db.CmsDbEntryNotFoundException;
import org.opencms.db.CmsLoginManager;
import org.opencms.db.CmsLoginMessage;
import org.opencms.db.CmsUserSettings;
import org.opencms.file.CmsFile;
import org.opencms.file.CmsGroup;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsProject;
import org.opencms.file.CmsProperty;
import org.opencms.file.CmsPropertyDefinition;
import org.opencms.file.CmsResource;
import org.opencms.file.CmsResourceFilter;
import org.opencms.file.CmsUser;
import org.opencms.file.types.CmsResourceTypeFolder;
import org.opencms.file.types.CmsResourceTypeXmlContainerPage;
import org.opencms.file.types.I_CmsResourceType;
import org.opencms.i18n.CmsLocaleManager;
import org.opencms.i18n.CmsMessages;
import org.opencms.importexport.CmsExportParameters;
import org.opencms.importexport.CmsImportParameters;
import org.opencms.importexport.CmsVfsImportExportHandler;
import org.opencms.lock.CmsLockActionRecord;
import org.opencms.lock.CmsLockActionRecord.LockChange;
import org.opencms.lock.CmsLockUtil;
import org.opencms.main.CmsBroadcast.ContentMode;
import org.opencms.module.CmsModule;
import org.opencms.module.CmsModule.ExportMode;
import org.opencms.module.CmsModuleImportExportHandler;
import org.opencms.module.CmsModuleManager;
import org.opencms.report.CmsLogReport;
import org.opencms.report.CmsMultiplexReport;
import org.opencms.report.CmsPrintStreamReport;
import org.opencms.report.CmsShellLogReport;
import org.opencms.report.CmsShellReport;
import org.opencms.report.I_CmsReport;
import org.opencms.search.CmsSearchManager;
import org.opencms.search.I_CmsSearchIndex;
import org.opencms.security.CmsAccessControlEntry;
import org.opencms.security.CmsAccessControlList;
import org.opencms.security.CmsOrgUnitManager;
import org.opencms.security.CmsOrganizationalUnit;
import org.opencms.security.CmsRole;
import org.opencms.security.CmsRoleViolationException;
import org.opencms.security.I_CmsPrincipal;
import org.opencms.site.CmsSite;
import org.opencms.staticexport.CmsLinkManager;
import org.opencms.ui.apps.search.CmsSearchReplaceSettings;
import org.opencms.ui.apps.search.CmsSearchReplaceThread;
import org.opencms.ui.apps.search.CmsSourceSearchForm.SearchType;
import org.opencms.ui.favorites.CmsFavoriteDAO;
import org.opencms.ui.favorites.CmsFavoriteEntry;
import org.opencms.ui.favorites.CmsFavoriteEntry.Type;
import org.opencms.util.CmsFileUtil;
import org.opencms.util.CmsUUID;
import org.opencms.workplace.CmsWorkplace;
import org.opencms.xml.CmsXmlFileTransformer;
import org.opencms.xml.content.CmsXmlContent;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.StringTokenizer;

import org.apache.commons.logging.Log;

/**
 * Provides additional commands for the CmsShell.<p>
 *
 * Such additional commands can access OpenCms functions not available on "regular" OpenCms classes.
 * Also, wrapping methods to access some important functions in the CmsObject that
 * require complex data type parameters are provided.<p>
 *
 * @since 6.0.0
 */
class CmsShellCommands implements I_CmsShellCommands {

    /** The log object for this class. */
    private static final Log LOG = CmsLog.getLog(CmsShellCommands.class);

    /** The OpenCms context object. */
    private CmsObject m_cms;
    /** The Cms shell object. */
    private CmsShell m_shell;

    /**
     * Generate a new instance of the command processor.<p>
     *
     * To initialize the command processor, you must call {@link #initShellCmsObject(CmsObject, CmsShell)}.
     *
     * @see #initShellCmsObject(CmsObject, CmsShell)
     */
    protected CmsShellCommands() {

        // noop
    }

    /**
     * Adds bookmark for the givne user/site root/path/project combination
     *
     * @param user the user for whom to set the bookmark
     * @param siteRoot the site root
     * @param sitePath the site path of the resource
     * @param project the name of the project
     *
     * @throws Exception if something goes wrong
     */
    public void addBookmark(String user, String siteRoot, String sitePath, String project) throws Exception {

        CmsObject cms = OpenCms.initCmsObject(m_cms);
        if (project != null) {
            cms.getRequestContext().setCurrentProject(cms.readProject(project));
        }
        cms.getRequestContext().setSiteRoot(siteRoot);
        CmsFavoriteDAO favDao = new CmsFavoriteDAO(cms, user);
        List<CmsFavoriteEntry> entries = favDao.loadFavorites();
        CmsResource res = cms.readResource(sitePath);
        CmsFavoriteEntry entry = new CmsFavoriteEntry();
        CmsProject currProject = cms.getRequestContext().getCurrentProject();
        if (res.isFolder()) {
            entry.setType(Type.explorerFolder);
            entry.setStructureId(res.getStructureId());
            entry.setProjectId(currProject.getId());
            entry.setSiteRoot(siteRoot);
        } else {
            if (currProject.isOnlineProject()) {
                throw new IllegalArgumentException("Can not set bookmark for page in Online project.");
            }
            entry.setType(Type.page);
            entry.setStructureId(res.getStructureId());
            entry.setProjectId(currProject.getId());
            entry.setSiteRoot(siteRoot);
        }
        entries.add(entry);
        favDao.saveFavorites(entries);
    }

    /**
     * Adds a resource to an organizational unit.
     *
     * @param ouFqn the full path of the OU
     * @param resource the resource path to add
     *
     * @throws Exception if something goes wrong
     */
    public void addResourceToOU(String ouFqn, String resource) throws Exception {

        CmsObject cms = m_cms;
        CmsOrgUnitManager ouManager = OpenCms.getOrgUnitManager();
        ouManager.addResourceToOrgUnit(cms, ouFqn, resource);

    }

    /**
     * Add the user to the given role.<p>
     *
     * @param user name of the user
     * @param role name of the role, for example 'EDITOR'
     *
     * @throws CmsException if something goes wrong
     */
    public void addUserToRole(String user, String role) throws CmsException {

        OpenCms.getRoleManager().addUserToRole(m_cms, CmsRole.valueOfRoleName(role), user);
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
            throw new CmsIllegalArgumentException(
                Messages.get().container(Messages.ERR_NOT_A_FOLDER_1, resolvedTarget));
        }
        m_cms.getRequestContext().setUri(resolvedTarget);
        m_shell.getOut().println(
            '\n' + getMessages().key(Messages.GUI_SHELL_CURRENT_FOLDER_1, new Object[] {resolvedTarget}));
        m_shell.getOut().println();
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
        if (I_CmsPrincipal.PRINCIPAL_GROUP.equalsIgnoreCase(principalType.trim())) {
            principalName = OpenCms.getImportExportManager().translateGroup(principalName);
        } else {
            principalName = OpenCms.getImportExportManager().translateUser(principalName);
        }
        m_cms.chacc(resourceName, principalType, principalName, permissionString);
    }

    /**
     * Change the user settings concerned with the place where a user is taken on login.<p>
     *
     * @param username the name of the user for which the data should be changed
     * @param startProject the start project
     * @param startSite the start site
     * @param startFolder the start folder (relative to the site root)
     * @param startView the start view
     *                  - Direct edit (/system/workplace/views/explorer/directEdit.jsp)
     *                  - Explorer (/system/workplace/views/explorer/explorer_fs.jsp)
     *                  - Administration (/system/workplace/views/admin/admin-fs.jsp)
     * @throws CmsException thrown if user can't be read or settings can't be saved.
     */
    public void changeUserSettingsStartParameters(
        String username,
        String startProject,
        String startSite,
        String startFolder,
        String startView)
    throws CmsException {

        CmsUser user = m_cms.readUser(username);
        CmsUserSettings settings = new CmsUserSettings(user);
        settings.setStartProject(startProject);
        settings.setStartSite(startSite);
        settings.setStartFolder(startFolder);
        settings.setStartView(startView);
        settings.save(m_cms);
    }

    /**
     * Clears all OpenCms internal caches.<p>
     *
     * @throws Exception if something goes wrong
     */
    public void clearCaches() throws Exception {

        OpenCms.fireCmsEvent(new CmsEvent(I_CmsEventListener.EVENT_CLEAR_CACHES, new HashMap<String, Object>()));
    }

    /**
     * Clears the login message.<p>
     *
     * @throws CmsRoleViolationException if this is not called with the correct privileges
     */
    public void clearLoginMessage() throws CmsRoleViolationException {

        OpenCms.getLoginManager().setLoginMessage(m_cms, null);
        OpenCms.writeConfiguration(CmsVariablesConfiguration.class);
    }

    /**
     * Prints the OpenCms copyright information.<p>
     */
    public void copyright() {

        String[] copy = Messages.COPYRIGHT_BY_ALKACON;
        for (int i = 0; i < copy.length; i++) {
            m_shell.getOut().println(copy[i]);
        }
    }

    /**
     * Creates a default project.<p>
     *
     * This created project has the following properties:<ul>
     * <li>The users group is the default user group
     * <li>The users group is also the default project manager group
     * <li>All resources are contained in the project
     * <li>The project will remain after publishing</ul>
     *
     * @param name the name of the project to create
     * @param description the description for the new project
     * @throws Exception if something goes wrong
     */
    public void createDefaultProject(String name, String description) throws Exception {

        String storedSiteRoot = m_cms.getRequestContext().getSiteRoot();
        try {
            m_cms.getRequestContext().setSiteRoot("/");
            CmsProject project = m_cms.createProject(
                name,
                description,
                OpenCms.getDefaultUsers().getGroupUsers(),
                OpenCms.getDefaultUsers().getGroupUsers(),
                CmsProject.PROJECT_TYPE_NORMAL);
            m_cms.getRequestContext().setCurrentProject(project);
            m_cms.copyResourceToProject("/");
        } finally {
            m_cms.getRequestContext().setSiteRoot(storedSiteRoot);
        }
        if (OpenCms.getRoleManager().hasRole(m_cms, CmsRole.WORKPLACE_MANAGER)) {
            // re-initialize the search indexes after default project generation
            OpenCms.getSearchManager().initialize(m_cms);
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
    @SuppressWarnings("deprecation")
    public CmsResource createFolder(String targetFolder, String folderName) throws Exception {

        if (m_cms.existsResource(targetFolder + folderName)) {
            m_shell.getOut().println(
                getMessages().key(Messages.GUI_SHELL_FOLDER_ALREADY_EXISTS_1, targetFolder + folderName));
            return null;
        }
        return m_cms.createResource(targetFolder + folderName, CmsResourceTypeFolder.getStaticTypeId());
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

        return m_cms.createGroup(name, description, I_CmsPrincipal.FLAG_ENABLED, null);
    }

    /**
     * Create an OU
     * @param ouFqn the fully qualified name of the OU
     * @param description the description of the OU
     * @param hideLogin flag, indicating if the OU should be hidden from the login form.
     * @param resource the initial OU resource
     *
     * @return the created OU, or <code>null</code> if creation fails.
     * @throws Exception if something goes wrong
     */
    public CmsOrganizationalUnit createOU(String ouFqn, String description, boolean hideLogin, String resource)
    throws Exception {

        return OpenCms.getOrgUnitManager().createOrganizationalUnit(
            m_cms,
            ouFqn,
            description,
            (hideLogin ? CmsOrganizationalUnit.FLAG_HIDE_LOGIN : 0),
            resource);

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
     * @see CmsObject#createUser(String, String, String, java.util.Map)
     * @return the created user
     */
    public CmsUser createUser(String name, String password, String description) throws Exception {

        if (existsUser(name)) {
            m_shell.getOut().println(getMessages().key(Messages.GUI_SHELL_USER_ALREADY_EXISTS_1, name));
            return null;
        }
        return m_cms.createUser(name, password, description, new Hashtable<String, Object>());
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
     *
     * @see CmsObject#createUser(String, String, String, java.util.Map)
     */
    public CmsUser createUser(
        String name,
        String password,
        String description,
        String firstname,
        String lastname,
        String email)
    throws Exception {

        if (existsUser(name)) {
            m_shell.getOut().println(getMessages().key(Messages.GUI_SHELL_USER_ALREADY_EXISTS_1, name));
            return null;
        }
        CmsUser user = m_cms.createUser(name, password, description, new Hashtable<String, Object>());
        user.setEmail(email);
        user.setFirstname(firstname);
        user.setLastname(lastname);
        m_cms.writeUser(user);
        return user;
    }

    /**
     * Create a web OU
     * @param ouFqn the fully qualified name of the OU
     * @param description the description of the OU
     * @param hideLogin flag, indicating if the OU should be hidden from the login form.
     *
     * @return the created OU, or <code>null</code> if creation fails.
     */
    public CmsOrganizationalUnit createWebOU(String ouFqn, String description, boolean hideLogin) {

        try {
            return OpenCms.getOrgUnitManager().createOrganizationalUnit(
                m_cms,
                ouFqn,
                description,
                (hideLogin ? CmsOrganizationalUnit.FLAG_HIDE_LOGIN : 0) | CmsOrganizationalUnit.FLAG_WEBUSERS,
                null);
        } catch (CmsException e) {
            // TODO Auto-generated catch block
            m_shell.getOut().println(
                getMessages().key(Messages.GUI_SHELL_WEB_OU_CREATION_FAILED_2, ouFqn, description));
            return null;
        }
    }

    /**
     * Deletes the versions from the history tables that are older then the given number of versions.<p>
     *
     * @param versionsToKeep number of versions to keep, is ignored if negative
     * @param versionsDeleted number of versions to keep for deleted resources, is ignored if negative
     * @param timeDeleted deleted resources older than this will also be deleted, is ignored if negative
     *
     * @throws Exception if something goes wrong
     *
     * @see CmsObject#deleteHistoricalVersions( int, int, long, I_CmsReport)
     */
    public void deleteHistoricalVersions(int versionsToKeep, int versionsDeleted, long timeDeleted) throws Exception {

        m_cms.deleteHistoricalVersions(
            versionsToKeep,
            versionsDeleted,
            timeDeleted,
            new CmsShellReport(m_cms.getRequestContext().getLocale()));
    }

    /**
     * Deletes a module.<p>
     *
     * @param moduleName the name of the module
     * @throws Exception if something goes wrong
     */
    public void deleteModule(String moduleName) throws Exception {

        OpenCms.getModuleManager().deleteModule(
            m_cms,
            moduleName,
            false,
            new CmsShellReport(m_cms.getRequestContext().getLocale()));
    }

    /**
     * Deletes a project by name.<p>
     *
     * @param name the name of the project to delete

     * @throws Exception if something goes wrong
     *
     * @see CmsObject#deleteProject(CmsUUID)
     */
    public void deleteProject(String name) throws Exception {

        m_cms.deleteProject(m_cms.readProject(name).getUuid());
    }

    /**
     * Delete a property definition for a resource.<p>
     *
     * @param name the name of the property definition to delete
     *
     * @throws Exception if something goes wrong
     *
     * @see CmsObject#deletePropertyDefinition(String)
     */
    public void deletepropertydefinition(String name) throws Exception {

        m_cms.deletePropertyDefinition(name);
    }

    /**
     * Deletes a resource.<p>
     *
     * @param name the name of the resource to delete
     *
     * @throws Exception if something goes wrong
     */
    public void deleteResource(String name) throws Exception {

        m_cms.lockResource(name);
        m_cms.deleteResource(name, CmsResource.DELETE_PRESERVE_SIBLINGS);
    }

    /**
     * Deletes a resource together with its siblings.<p>
     *
     * @param name the name of the resource to delete
     *
     * @throws Exception if something goes wrong
     */
    public void deleteResourceWithSiblings(String name) throws Exception {

        m_cms.lockResource(name);
        m_cms.deleteResource(name, CmsResource.DELETE_REMOVE_SIBLINGS);
    }

    /**
     * Disable incremental module updates.
     */
    public void disableModuleUpdates() {

        OpenCms.getModuleManager().setModuleUpdateEnabled(false);
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
            m_shell.getOut().println(getMessages().key(Messages.GUI_SHELL_ECHO_ON_0));
        } else {
            m_shell.getOut().println(getMessages().key(Messages.GUI_SHELL_ECHO_OFF_0));
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

        exportAllResources(exportFile, false);
    }

    /**
     * Exports all resources from the current site root to a ZIP file.<p>
     *
     * @param exportFile the name (absolute path) of the ZIP file to export to
     * @param isReducedExportMode flag, indicating if the reduced export mode should be used
     * @throws Exception if something goes wrong
     */
    public void exportAllResources(String exportFile, boolean isReducedExportMode) throws Exception {

        List<String> exportPaths = new ArrayList<String>(1);
        exportPaths.add("/");

        CmsVfsImportExportHandler vfsExportHandler = new CmsVfsImportExportHandler();
        CmsExportParameters params = new CmsExportParameters(
            exportFile,
            null,
            true,
            false,
            false,
            exportPaths,
            true,
            true,
            0,
            true,
            false,
            isReducedExportMode ? ExportMode.REDUCED : ExportMode.DEFAULT);
        vfsExportHandler.setExportParams(params);

        OpenCms.getImportExportManager().exportData(
            m_cms,
            vfsExportHandler,
            new CmsShellReport(m_cms.getRequestContext().getLocale()));
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
                + CmsSystemInfo.FOLDER_MODULES
                + moduleName
                + "_"
                + OpenCms.getModuleManager().getModule(moduleName).getVersion().toString());

        List<String> moduleResources = CmsModule.calculateModuleResourceNames(m_cms, module);
        String[] resources = new String[moduleResources.size()];
        System.arraycopy(moduleResources.toArray(), 0, resources, 0, resources.length);

        // generate a module export handler
        CmsModuleImportExportHandler moduleExportHandler = new CmsModuleImportExportHandler();
        moduleExportHandler.setFileName(filename);
        moduleExportHandler.setAdditionalResources(resources);
        moduleExportHandler.setModuleName(module.getName().replace('\\', '/'));
        moduleExportHandler.setDescription(
            getMessages().key(
                Messages.GUI_SHELL_IMPORTEXPORT_MODULE_HANDLER_NAME_1,
                new Object[] {moduleExportHandler.getModuleName()}));

        // export the module
        OpenCms.getImportExportManager().exportData(
            m_cms,
            moduleExportHandler,
            new CmsShellReport(m_cms.getRequestContext().getLocale()));
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

        exportResources(exportFile, pathList, false);
    }

    /**
     * Exports a list of resources from the current site root to a ZIP file.<p>
     *
     * The resource names in the list must be separated with a ";".<p>
     *
     * @param exportFile the name (absolute path) of the ZIP file to export to
     * @param pathList the list of resource to export, separated with a ";"
     * @param isReducedExportMode flag, indicating if the reduced export mode should be used
     * @throws Exception if something goes wrong
     */
    public void exportResources(String exportFile, String pathList, boolean isReducedExportMode) throws Exception {

        exportResources(exportFile, pathList, isReducedExportMode, false);
    }

    /**
     * Exports a list of resources from the current site root to a ZIP file.<p>
     *
     * The resource names in the list must be separated with a ";".<p>
     *
     * @param exportFile the name (absolute path) of the ZIP file to export to
     * @param pathList the list of resource to export, separated with a ";"
     * @param isReducedExportMode flag, indicating if the reduced export mode should be used
     * @param skipParentFolders true if parent folders should not be written to the export
     * @throws Exception if something goes wrong
     */
    public void exportResources(
        String exportFile,
        String pathList,
        boolean isReducedExportMode,
        boolean skipParentFolders)
    throws Exception {

        StringTokenizer tok = new StringTokenizer(pathList, ";");
        List<String> exportPaths = new ArrayList<String>();
        while (tok.hasMoreTokens()) {
            exportPaths.add(tok.nextToken());
        }
        boolean includeSystem = false;
        if (pathList.startsWith(CmsWorkplace.VFS_PATH_SYSTEM)
            || (pathList.indexOf(";" + CmsWorkplace.VFS_PATH_SYSTEM) > -1)) {
            includeSystem = true;
        }

        CmsVfsImportExportHandler vfsExportHandler = new CmsVfsImportExportHandler();
        CmsExportParameters params = new CmsExportParameters(
            exportFile,
            null,
            true,
            false,
            false,
            exportPaths,
            includeSystem,
            true,
            0,
            true,
            false,
            isReducedExportMode ? ExportMode.REDUCED : ExportMode.DEFAULT);
        params.setSkipParentFolders(skipParentFolders);
        vfsExportHandler.setExportParams(params);

        OpenCms.getImportExportManager().exportData(
            m_cms,
            vfsExportHandler,
            new CmsShellReport(m_cms.getRequestContext().getLocale()));
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

        exportResourcesAndUserdata(exportFile, pathList, false);
    }

    /**
     * Exports a list of resources from the current site root and the user data to a ZIP file.<p>
     *
     * The resource names in the list must be separated with a ";".<p>
     *
     * @param exportFile the name (absolute path) of the ZIP file to export to
     * @param pathList the list of resource to export, separated with a ";"
     * @param isReducedExportMode flag, indicating if the reduced export mode should be used
     * @throws Exception if something goes wrong
     */
    public void exportResourcesAndUserdata(String exportFile, String pathList, boolean isReducedExportMode)
    throws Exception {

        exportResourcesAndUserdata(exportFile, pathList, isReducedExportMode, false);
    }

    /**
     * Exports a list of resources from the current site root and the user data to a ZIP file.<p>
     *
     * The resource names in the list must be separated with a ";".<p>
     *
     * @param exportFile the name (absolute path) of the ZIP file to export to
     * @param pathList the list of resource to export, separated with a ";"
     * @param isReducedExportMode flag, indicating if the reduced export mode should be used
     * @param skipParentFolders true if parent folders should not be written to the export
     * @throws Exception if something goes wrong
     */
    public void exportResourcesAndUserdata(
        String exportFile,
        String pathList,
        boolean isReducedExportMode,
        boolean skipParentFolders)
    throws Exception {

        StringTokenizer tok = new StringTokenizer(pathList, ";");
        List<String> exportPaths = new ArrayList<String>();
        while (tok.hasMoreTokens()) {
            exportPaths.add(tok.nextToken());
        }
        boolean includeSystem = false;
        if (pathList.startsWith(CmsWorkplace.VFS_PATH_SYSTEM)
            || (pathList.indexOf(";" + CmsWorkplace.VFS_PATH_SYSTEM) > -1)) {
            includeSystem = true;
        }

        CmsVfsImportExportHandler vfsExportHandler = new CmsVfsImportExportHandler();
        CmsExportParameters params = new CmsExportParameters(
            exportFile,
            null,
            true,
            true,
            false,
            exportPaths,
            includeSystem,
            true,
            0,
            true,
            false,
            isReducedExportMode ? ExportMode.REDUCED : ExportMode.DEFAULT);
        params.setSkipParentFolders(skipParentFolders);
        vfsExportHandler.setExportParams(params);

        OpenCms.getImportExportManager().exportData(
            m_cms,
            vfsExportHandler,
            new CmsShellReport(m_cms.getRequestContext().getLocale()));
    }

    /**
     * Deletes a module. Will ignore any modules depending on the module to delete.<p>
     *
     * @param moduleName the name of the module
     * @throws Exception if something goes wrong
     */
    public void forceDeleteModule(String moduleName) throws Exception {

        OpenCms.getModuleManager().deleteModule(
            m_cms,
            moduleName,
            true,
            new CmsShellReport(m_cms.getRequestContext().getLocale()));
    }

    /**
     * Forces password reset state for (non-managed, non-default) users.
     */
    public void forcePasswordResetForUsers() throws Exception {

        CmsObject cms = m_cms;
        OpenCms.getRoleManager().checkRole(cms, CmsRole.ACCOUNT_MANAGER);
        CmsLoginManager loginManager = OpenCms.getLoginManager();
        String ou = "";
        List<CmsUser> users = OpenCms.getOrgUnitManager().getUsers(cms, ou, true);
        for (CmsUser user : users) {
            if (loginManager.isExcludedFromPasswordReset(cms, user)) {
                LOG.info("Excluded user " + user.getName() + " from password reset.");
                continue;
            }
            try {
                if (user.getAdditionalInfo(CmsUserSettings.ADDITIONAL_INFO_PASSWORD_RESET) == null) {
                    LOG.info("Marking user " + user.getName() + " for password reset.");
                    user.setAdditionalInfo(CmsUserSettings.ADDITIONAL_INFO_PASSWORD_RESET, "true");
                    cms.writeUser(user);
                }
            } catch (CmsException e) {
                LOG.error(e.getLocalizedMessage(), e);
            }
        }
    }

    /**
     * Displays the access control list of a given resource.<p>
     *
     * @param resourceName the name of the resource
     *
     * @throws Exception if something goes wrong
     *
     * @see CmsObject#getAccessControlList(String)
     */
    public void getAcl(String resourceName) throws Exception {

        CmsAccessControlList acList = m_cms.getAccessControlList(resourceName);
        Iterator<CmsUUID> principals = acList.getPrincipals().iterator();
        while (principals.hasNext()) {
            I_CmsPrincipal p = m_cms.lookupPrincipal(principals.next());
            m_shell.getOut().println(p.getName() + ": " + acList.getPermissions(p.getId()).getPermissionString());
        }
    }

    /**
     * Returns the Locales available on the system ready to use on Method
     * {@link #setLocale(String)} from the <code>{@link CmsShell}</code>. <p>
     *
     * Note that the full name containing language, country and optional variant seperated
     * by underscores is returned always but the latter two parts may be left out. <p>
     */
    public void getLocales() {

        m_shell.getOut().println(getMessages().key(Messages.GUI_SHELL_LOCALES_AVAILABLE_0));
        Locale[] locales = Locale.getAvailableLocales();
        for (int i = locales.length - 1; i >= 0; i--) {
            m_shell.getOut().println("  \"" + locales[i].toString() + "\"");
        }
    }

    /**
     * Provides help information for the CmsShell.<p>
     */
    public void help() {

        m_shell.getOut().println();
        m_shell.getOut().println(getMessages().key(Messages.GUI_SHELL_HELP1_0));
        m_shell.getOut().println(getMessages().key(Messages.GUI_SHELL_HELP2_0));
        m_shell.getOut().println(getMessages().key(Messages.GUI_SHELL_HELP3_0));
        m_shell.getOut().println(getMessages().key(Messages.GUI_SHELL_HELP4_0));
        m_shell.getOut().println();
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
     *
     * @throws Exception if something goes wrong
     *
     * @see org.opencms.importexport.CmsImportExportManager#importData(CmsObject, I_CmsReport, CmsImportParameters)
     */
    public void importModule(String importFile) throws Exception {

        CmsImportParameters params = new CmsImportParameters(importFile, "/", true);

        OpenCms.getImportExportManager().importData(
            m_cms,
            new CmsShellReport(m_cms.getRequestContext().getLocale()),
            params);
    }

    /**
     * Imports a module (zipfile) from the default module directory,
     * creating a temporary project for this.<p>
     *
     * @param importFile the name of the import module located in the default module directory
     *
     * @throws Exception if something goes wrong
     *
     * @see org.opencms.importexport.CmsImportExportManager#importData(CmsObject, I_CmsReport, CmsImportParameters)
     */
    public void importModuleFromDefault(String importFile) throws Exception {

        String exportPath = OpenCms.getSystemInfo().getPackagesRfsPath();
        String fileName = OpenCms.getSystemInfo().getAbsoluteRfsPathRelativeToWebInf(
            exportPath + CmsSystemInfo.FOLDER_MODULES + importFile);

        CmsImportParameters params = new CmsImportParameters(fileName, "/", true);

        OpenCms.getImportExportManager().importData(
            m_cms,
            new CmsShellReport(m_cms.getRequestContext().getLocale()),
            params);
    }

    /**
     * Exists so that the setup script can run without the wizard, does nothing.<p>
     */
    public void importModulesFromSetupBean() {

        // noop, exists so that the setup script can run without the wizard
    }

    /**
     * Imports a resource into the Cms.<p>
     *
     * @param importFile the name (absolute Path) of the import resource (zip or folder)
     * @param importPath the name (absolute Path) of folder in which should be imported
     *
     * @throws Exception if something goes wrong
     */
    public void importResources(String importFile, String importPath) throws Exception {

        CmsImportParameters params = new CmsImportParameters(
            OpenCms.getSystemInfo().getAbsoluteRfsPathRelativeToWebInf(importFile),
            importPath,
            true);

        OpenCms.getImportExportManager().importData(
            m_cms,
            new CmsShellReport(m_cms.getRequestContext().getLocale()),
            params);
    }

    /**
     * Imports a resource into the Cms.<p>
     *
     * @param importFile the name (absolute Path) of the import resource (zip or folder)
     * @param importPath the name (absolute Path) of folder in which should be imported
     * @param keepPermissions if set, the permissions set on existing resources will not be modified
     *
     * @throws Exception if something goes wrong
     */
    public void importResources(String importFile, String importPath, boolean keepPermissions) throws Exception {

        CmsImportParameters params = new CmsImportParameters(
            OpenCms.getSystemInfo().getAbsoluteRfsPathRelativeToWebInf(importFile),
            importPath,
            keepPermissions);

        OpenCms.getImportExportManager().importData(
            m_cms,
            new CmsShellReport(m_cms.getRequestContext().getLocale()),
            params);
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
            getMessages().key(Messages.GUI_SHELL_IMPORT_TEMP_PROJECT_NAME_0),
            OpenCms.getDefaultUsers().getGroupAdministrators(),
            OpenCms.getDefaultUsers().getGroupAdministrators(),
            CmsProject.PROJECT_TYPE_TEMPORARY);
        CmsUUID id = project.getUuid();
        m_cms.getRequestContext().setCurrentProject(project);
        m_cms.copyResourceToProject("/");

        CmsImportParameters params = new CmsImportParameters(importFile, "/", true);

        OpenCms.getImportExportManager().importData(
            m_cms,
            new CmsShellReport(m_cms.getRequestContext().getLocale()),
            params);

        m_cms.unlockProject(id);
        OpenCms.getPublishManager().publishProject(m_cms);
        OpenCms.getPublishManager().waitWhileRunning();
    }

    /**
     * Initializes the ADE manager.
     */
    public void initAdeManager() {

        OpenCms.getADEManager();
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

        Set<String> modules = OpenCms.getModuleManager().getModuleNames();
        m_shell.getOut().println(
            "\n" + getMessages().key(Messages.GUI_SHELL_LIST_MODULES_1, Integer.valueOf(modules.size())));
        Iterator<String> i = modules.iterator();
        while (i.hasNext()) {
            String moduleName = i.next();
            m_shell.getOut().println(moduleName);
        }
        m_shell.getOut().println();
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
            m_shell.getOut().println(getMessages().key(Messages.GUI_SHELL_LOGIN_1, whoami().getName()));
            // output the login message if required
            CmsLoginMessage message = OpenCms.getLoginManager().getLoginMessage();
            if ((message != null) && (message.isActive())) {
                m_shell.getOut().println(message.getMessage());
            }
        } catch (Exception exc) {
            m_shell.getOut().println(getMessages().key(Messages.GUI_SHELL_LOGIN_FAILED_0));
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
        List<CmsResource> resources = m_cms.getResourcesInFolder(folder, CmsResourceFilter.IGNORE_EXPIRATION);
        m_shell.getOut().println(
            "\n" + getMessages().key(Messages.GUI_SHELL_LS_2, folder, Integer.valueOf(resources.size())));
        Iterator<CmsResource> i = resources.iterator();
        while (i.hasNext()) {
            CmsResource r = i.next();
            m_shell.getOut().println(m_cms.getSitePath(r));
        }
        m_shell.getOut().println();
    }

    /**
     * Lists the access control entries of a given resource.<p>
     *
     * @param resourceName the name of the resource
     * @throws Exception if something goes wrong
     */
    public void lsacc(String resourceName) throws Exception {

        List<CmsAccessControlEntry> acList = m_cms.getAccessControlEntries(resourceName);
        for (int i = 0; i < acList.size(); i++) {
            CmsAccessControlEntry ace = acList.get(i);
            I_CmsPrincipal acePrincipal = m_cms.lookupPrincipal(ace.getPrincipal());
            String pName = (acePrincipal != null) ? acePrincipal.getName() : ace.getPrincipal().toString();
            m_shell.getOut().println(pName + ": " + ace.getPermissions().getPermissionString() + " " + ace);
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
        List<CmsAccessControlEntry> acList = m_cms.getAccessControlEntries(resourceName);
        for (int i = 0; i < acList.size(); i++) {
            CmsAccessControlEntry ace = acList.get(i);
            I_CmsPrincipal acePrincipal = m_cms.lookupPrincipal(ace.getPrincipal());
            if (principal.equals(acePrincipal)) {
                String pName = (acePrincipal != null) ? acePrincipal.getName() : ace.getPrincipal().toString();
                m_shell.getOut().println(pName + ": " + ace.getPermissions().getPermissionString() + " " + ace);
            }
        }
    }

    /**
     * Sets the error code which should be returned as exit code in case an exception is thrown
     * by a shell command.<p>
     *
     * A value of -1 means the shell should not exit on exceptions thrown by shell commands
     *
     * @param errorCode the error code (the string representation of an integer)
     */
    public void onError(String errorCode) {

        m_shell.m_errorCode = Integer.parseInt(errorCode);
    }

    /**
     * Does performance measurements of the OpenCms core.<p>
     *
     * @throws Exception if something goes wrong
     */
    public void perf() throws Exception {

        int maxTests = 50000;
        String storedSiteRoot = m_cms.getRequestContext().getSiteRoot();
        try {
            m_cms.getRequestContext().setSiteRoot("/");
            Random random = new Random();
            // create a resource filter to get the resources with
            List<CmsResource> testResources = m_cms.readResources("/", CmsResourceFilter.ALL);
            int resourceCount = testResources.size();
            m_shell.getOut().println("#Resources:\t" + resourceCount);
            long start, time;
            long totalTime = 0;
            long minTime = Long.MAX_VALUE;
            long maxTime = Long.MIN_VALUE;
            m_shell.getOut().print("readFileHeader:\t");
            for (int i = maxTests; i > 0; --i) {
                int index = random.nextInt(resourceCount);
                CmsResource resource = testResources.get(index);
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
                    m_shell.getOut().print('.');
                }
            }
            m_shell.getOut().println(
                "\nreadFileHeader:\t" + minTime + "\t" + maxTime + "\t" + (((float)totalTime) / maxTests) + " ms");

        } finally {
            m_cms.getRequestContext().setSiteRoot(storedSiteRoot);
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
     * Publishes the current project and waits until it finishes.<p>
     *
     * @throws Exception if something goes wrong
     */
    public void publishProjectAndWait() throws Exception {

        OpenCms.getPublishManager().publishProject(m_cms);
        OpenCms.getPublishManager().waitWhileRunning();
    }

    /**
     * Purges the jsp repository.<p>
     *
     * @throws Exception if something goes wrong
     *
     * @see org.opencms.flex.CmsFlexCache#cmsEvent(org.opencms.main.CmsEvent)
     */
    public void purgeJspRepository() throws Exception {

        OpenCms.fireCmsEvent(
            new CmsEvent(I_CmsEventListener.EVENT_FLEX_PURGE_JSP_REPOSITORY, new HashMap<String, Object>()));
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
    public CmsGroup readGroupOfProject(CmsUUID project) throws Exception {

        return m_cms.readGroup(m_cms.readProject(project));
    }

    /**
     * Returns the manager group of a project.<p>
     *
     * @param project the id of the project to return the manager group for
     * @return the manager group of the project
     * @throws Exception if something goes wrong
     */
    public CmsGroup readManagerGroup(CmsUUID project) throws Exception {

        return m_cms.readManagerGroup(m_cms.readProject(project));
    }

    /**
     * Returns the owner of a project.<p>
     *
     * @param project the id of the project
     * @return the owner of the project
     * @throws Exception if something goes wrong
     */
    public CmsUser readOwnerOfProject(CmsUUID project) throws Exception {

        return m_cms.readOwner(m_cms.readProject(project));
    }

    /**
     * Rebuilds (if required creates) all configured search indexes.<p>
     *
     * @throws Exception if something goes wrong
     *
     * @see org.opencms.search.CmsSearchManager#rebuildAllIndexes(org.opencms.report.I_CmsReport)
     */
    public void rebuildAllIndexes() throws Exception {

        I_CmsReport report = new CmsShellReport(m_cms.getRequestContext().getLocale());
        OpenCms.getSearchManager().rebuildAllIndexes(report);
    }

    /**
     * Rebuilds (if required creates) the given search index.<p>
     *
     * @param index name of the index to update
     * @throws Exception if something goes wrong
     * @see org.opencms.search.CmsSearchManager#rebuildIndex(String, org.opencms.report.I_CmsReport)
     */
    public void rebuildIndex(String index) throws Exception {

        OpenCms.getSearchManager().rebuildIndex(index, new CmsShellReport(m_cms.getRequestContext().getLocale()));
    }

    /**
     * Reindexes either a single file or all files under a folder in the given project.
     *
     * @param path the path of either a file to reindex, or a folder under which resources should be indexed
     * @param reindexRelated true if related resources should be reindexed
     * @throws CmsException if something goes wrong
     */
    public void reindexResources(String path, boolean reindexRelated) throws CmsException {

        CmsObject cms = OpenCms.initCmsObject(m_cms);
        Map<String, Object> eventData = new HashMap<>(3);
        boolean online = cms.getRequestContext().getCurrentProject().isOnlineProject();
        if (!online) {
            eventData.put(I_CmsEventListener.KEY_PROJECTID, cms.getRequestContext().getCurrentProject().getId());
        }
        CmsResource resource = cms.readResource(path, CmsResourceFilter.IGNORE_EXPIRATION);
        eventData.put(I_CmsEventListener.KEY_RESOURCES, Collections.singletonList(resource));
        eventData.put(
            I_CmsEventListener.KEY_REPORT,
            new CmsLogReport(CmsLocaleManager.getDefaultLocale(), CmsSearchManager.class));
        eventData.put(I_CmsEventListener.KEY_REINDEX_RELATED, Boolean.valueOf(reindexRelated));
        CmsEvent reindexEvent = new CmsEvent(
            online ? I_CmsEventListener.EVENT_REINDEX_ONLINE : I_CmsEventListener.EVENT_REINDEX_OFFLINE,
            eventData);
        OpenCms.fireCmsEvent(reindexEvent);
    }

    /**
     * Replaces elements on container pages.<p>
     *
     * @param elementPath to be found in container pages
     * @param oldName to replace
     * @param newName new name
     * @param project project to replace in
     * @param basePath basePath to find resources
     * @param ignoreSubSites ignore subsites?
     */
    public void renameNestedContainers(
        String elementPath,
        String oldName,
        String newName,
        String project,
        String basePath,
        boolean ignoreSubSites) {

        try {
            CmsObject cms = OpenCms.initCmsObject(m_cms);
            cms.getRequestContext().setCurrentProject(cms.readProject(project));
            cms.getRequestContext().setSiteRoot("");
            CmsResource resource = cms.readResource(elementPath);
            CmsSearchReplaceSettings settings = new CmsSearchReplaceSettings();
            settings.setType(SearchType.renameContainer);
            settings.setSearchpattern(CmsSearchReplaceSettings.searchElementInPagePattern(resource));
            settings.setElementResource(resource);
            settings.setReplacepattern(oldName + ";" + newName);
            String siteRoot = OpenCms.getSiteManager().getSiteRoot(basePath);
            if (siteRoot != null) {
                settings.setSiteRoot(siteRoot);
                settings.setPaths(Collections.singletonList(basePath.substring(siteRoot.length())));
            } else {
                settings.setSiteRoot("");
                settings.setPaths(Collections.singletonList(basePath));
            }
            settings.setIgnoreSubSites(ignoreSubSites);
            settings.setProject(project);
            settings.setTypes(
                CmsResourceTypeXmlContainerPage.RESOURCE_TYPE_NAME
                    + ","
                    + CmsResourceTypeXmlContainerPage.MODEL_GROUP_TYPE_NAME);

            CmsSearchReplaceThread thread = new CmsSearchReplaceThread(
                null,
                cms,
                settings,
                new CmsShellLogReport(m_cms.getRequestContext().getLocale()));
            thread.run();
        } catch (CmsException e) {
            //
        }
    }

    /**
     * Replaces elements on container pages.<p>
     *
     * @param elementPath to be found in container pages
     * @param targetElementPath new elements in pages
     * @param project project to replace in
     * @param basePath basePath to find resources
     * @param ignoreSubSites ignore subsites?
     */
    public void replaceElementsInPages(
        String elementPath,
        String targetElementPath,
        String project,
        String basePath,
        boolean ignoreSubSites) {

        try {
            CmsObject cms = OpenCms.initCmsObject(m_cms);
            cms.getRequestContext().setCurrentProject(cms.readProject(project));
            cms.getRequestContext().setSiteRoot("");
            CmsResource resource = cms.readResource(elementPath);
            CmsResource targetResource = cms.readResource(targetElementPath);
            CmsSearchReplaceSettings settings = new CmsSearchReplaceSettings();
            settings.setType(SearchType.resourcetype);
            settings.setSearchpattern(CmsSearchReplaceSettings.searchElementInPagePattern(resource));
            settings.setReplacepattern(CmsSearchReplaceSettings.replaceElementInPagePattern(targetResource));
            String siteRoot = OpenCms.getSiteManager().getSiteRoot(basePath);
            if (siteRoot != null) {
                settings.setSiteRoot(siteRoot);
                settings.setPaths(Collections.singletonList(basePath.substring(siteRoot.length())));
            } else {
                settings.setSiteRoot("");
                settings.setPaths(Collections.singletonList(basePath));
            }

            settings.setIgnoreSubSites(ignoreSubSites);
            settings.setProject(project);
            settings.setTypes(
                CmsResourceTypeXmlContainerPage.RESOURCE_TYPE_NAME
                    + ","
                    + CmsResourceTypeXmlContainerPage.MODEL_GROUP_TYPE_NAME);

            CmsSearchReplaceThread thread = new CmsSearchReplaceThread(
                null,
                cms,
                settings,
                new CmsShellLogReport(m_cms.getRequestContext().getLocale()));
            thread.run();
        } catch (CmsException e) {
            LOG.error("test", e);
        }
    }

    /**
     * Replaces a module with another revision.<p>
     *
     * @param importFile the name of the import file
     *
     * @throws Exception if something goes wrong
     */
    public void replaceModule(String importFile) throws Exception {

        OpenCms.getModuleManager().replaceModule(
            m_cms,
            importFile,
            new CmsShellReport(m_cms.getRequestContext().getLocale()));

    }

    /**
     * Replaces a module with another revision.<p>
     *
     * @param moduleName the name of the module to delete
     * @param importFile the name of the import file
     *
     * @throws Exception if something goes wrong
     */
    public void replaceModule(String moduleName, String importFile) throws Exception {

        CmsModule module = CmsModuleImportExportHandler.readModuleFromImport(importFile);
        if (moduleName.equals(module.getName())) {
            replaceModule(importFile);
        } else {
            if (OpenCms.getModuleManager().getModule(moduleName) != null) {
                OpenCms.getModuleManager().deleteModule(
                    m_cms,
                    moduleName,
                    true,
                    new CmsShellReport(m_cms.getRequestContext().getLocale()));
            }
            importModule(importFile);
        }
    }

    /**
     * Replaces a module with another revision.<p>
     *
     * @param importFile the name of the import file
     * @throws Exception if something goes wrong
     */
    public void replaceModuleFromDefault(String importFile) throws Exception {
        String exportPath = OpenCms.getSystemInfo().getPackagesRfsPath();
        String fileName = OpenCms.getSystemInfo().getAbsoluteRfsPathRelativeToWebInf(
            exportPath + CmsSystemInfo.FOLDER_MODULES + importFile);

        CmsModule module = CmsModuleImportExportHandler.readModuleFromImport(fileName);
        String moduleName = module.getName();
        replaceModuleFromDefault(moduleName, importFile);
    }

    /**
     * Replaces a module with another revision.<p>
     *
     * @param moduleName the name of the module to delete
     * @param importFile the name of the import file
     * @throws Exception if something goes wrong
     */
    public void replaceModuleFromDefault(String moduleName, String importFile) throws Exception {

        if (OpenCms.getModuleManager().getModule(moduleName) != null) {
            OpenCms.getModuleManager().deleteModule(
                m_cms,
                moduleName,
                true,
                new CmsShellReport(m_cms.getRequestContext().getLocale()));
        }

        importModuleFromDefault(importFile);
    }

    /**
     * Sends a broadcast message.<p>
     *
     * This is only useful when the CmsShell is used via RMI.<p>
     *
     * @param message the broadcast message
     */
    public void sendBroadcast(String message) {

        OpenCms.getSessionManager().sendBroadcast(m_cms, message, ContentMode.plain);
    }

    /**
     * Sets the current project to the provided project id.<p>
     *
     * @param id the project id to set
     * @return the project set
     * @throws Exception if something goes wrong
     */
    public CmsProject setCurrentProject(CmsUUID id) throws Exception {

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
     * Sets the rebuild mode for the requested index. Allowing to disable indexing during module import.<p>
     * This setting will not be written to the XML configuration file and will only take effect within the current shell instance.<p>
     *
     * @param searchIndex the search index
     * @param mode the rebuild mode to set
     */
    public void setIndexRebuildMode(String searchIndex, String mode) {

        I_CmsSearchIndex index = OpenCms.getSearchManager().getIndex(searchIndex);
        if (index != null) {
            index.setRebuildMode(mode);
            // required for this setting to take effect
            OpenCms.getSearchManager().initOfflineIndexes();
        }
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
        m_shell.getOut().println(
            getMessages().key(
                Messages.GUI_SHELL_SETLOCALE_2,
                locale,
                m_cms.getRequestContext().getCurrentUser().getName()));

        m_shell.setLocale(locale);
        m_shell.getOut().println(getMessages().key(Messages.GUI_SHELL_SETLOCALE_POST_1, locale));
    }

    /**
     * Sets the login message.<p>
     *
     * @param messageText the message text
     * @param loginDisabled true if login should be disabled
     *
     * @throws CmsRoleViolationException when this is not called with the correct privileges
     */
    public void setLoginMessage(String messageText, boolean loginDisabled) throws CmsRoleViolationException {

        CmsLoginMessage message = new CmsLoginMessage(0, 0, messageText, loginDisabled);
        OpenCms.getLoginManager().setLoginMessage(m_cms, message);
        OpenCms.writeConfiguration(CmsVariablesConfiguration.class);
    }

    /**
     * Sets a site parameter and writes back the updated system configuration.<p>
     *
     * @param siteRoot the root path used to identify the site
     *
     * @param key the parameter key
     * @param value the parameter value
     */
    public void setSiteParam(String siteRoot, String key, String value) {

        CmsSite site = OpenCms.getSiteManager().getSiteForRootPath(siteRoot);
        if (site == null) {
            throw new IllegalArgumentException("No site found for path: " + siteRoot);
        } else {
            site.getParameters().put(key, value);
            OpenCms.writeConfiguration(CmsSitesConfiguration.class);
        }
    }

    /**
     * Sets a string-valued additional info entry on the user.
     *
     * @param username the name of the user
     * @param infoName the additional info key
     * @param value the additional info value
     *
     * @throws CmsException if something goes wrong
     */
    public void setUserInfo(String username, String infoName, String value) throws CmsException {

        CmsUser user = m_cms.readUser(username);
        user.setAdditionalInfo(infoName, value);
        m_cms.writeUser(user);
    }

    /**
     * @see org.opencms.main.I_CmsShellCommands#shellExit()
     */
    public void shellExit() {

        m_shell.getOut().println();
        m_shell.getOut().println(getMessages().key(Messages.GUI_SHELL_GOODBYE_0));
    }

    /**
     * @see org.opencms.main.I_CmsShellCommands#shellStart()
     */
    public void shellStart() {

        m_shell.getOut().println();
        m_shell.getOut().println(getMessages().key(Messages.GUI_SHELL_WELCOME_0));
        m_shell.getOut().println();

        // print the version information
        version();
        // print the copyright message
        copyright();
        if (m_shell.isInteractive()) {
            // print the help information for interactive terminals
            help();
        }
    }

    /**
     * Sleeps for a duration given in milliseconds.<p>
     *
     * @param sleepMillis a string containing the number of milliseconds to wait
     *
     * @throws NumberFormatException if the sleepMillis parameter is not a valid number
     */
    public void sleep(String sleepMillis) throws NumberFormatException {

        try {
            Thread.sleep(Long.parseLong(sleepMillis));
        } catch (InterruptedException e) {
            // ignore
        }
    }

    /**
     * Starts a benchmark timer.
     *
     * @param name the timer name
     */
    public void startBenchmark(String name) {

        m_shell.getBenchmarkTable().start(name);
    }

    /**
     * Stops a benchmark timer.
     *
     * @param name the timer name
     */
    public void stopBenchmark(String name) {

        m_shell.getBenchmarkTable().stop(name);

    }

    /**
     * Sets the checkpoint time of all installed modules to the current time.<p>
     */
    public void touchAllModules() {

        CmsModuleManager manager = OpenCms.getModuleManager();
        long time = System.currentTimeMillis();

        for (CmsModule module : manager.getAllInstalledModules()) {
            m_shell.getOut().println("Touching module: " + module.getName());
            module.setCheckpointTime(time);
        }
        m_shell.getOut().println("Writing module configuration.");
        manager.updateModuleConfiguration();

    }

    /**
     * Sets the checkpoint time of a given module to the current time.<p>
     *
     * If the module does not exist, this just prints an error message but does not throw an exception.
     *
     * @param moduleName the name of the module whose checkpoint time to update
     */
    public void touchModule(String moduleName) {

        CmsModule module = OpenCms.getModuleManager().getModule(moduleName);
        if (module == null) {
            m_shell.getOut().println("touchModule: could not find module " + moduleName);
        } else {
            m_shell.getOut().println("Touching module: " + moduleName);
            module.setCheckpointTime(System.currentTimeMillis());
            m_shell.getOut().println("Writing module configuration.");
            OpenCms.getModuleManager().updateModuleConfiguration();
        }
    }

    /**
     * Touches a resource and all its children.<p>
     *
     * This method also rewrites the content for all files in the subtree.
     *
     * @param resourcePath the site path of the resource
     *
     * @throws Exception if something goes wrong
     */
    public void touchResource(String resourcePath) throws Exception {

        CmsResource resource = m_cms.readResource(resourcePath);
        CmsLockActionRecord action = CmsLockUtil.ensureLock(m_cms, resource);
        try {
            OpenCms.getWorkplaceManager().flushMessageCache();
            // One important reason for touching resources via the shell is to write mapped values containing
            // localization macros to properties, so we flush the workplace messages immediately before the touch operation
            // in case an older version of the workplace messages (not containing the keys we need) has already been cached
            touchSingleResource(m_cms, resourcePath, System.currentTimeMillis(), true, true, true);
        } finally {
            if (action.getChange() == LockChange.locked) {
                m_cms.unlockResource(resource);
            }
        }
    }

    /**
     * Transforms configuration resources in the VFS using an XSL transformation.
     *
     * @param path the path for which to transform the XML contents
     * @param typeName the name of the type to transform
     * @param transform the RFS path to the XSL transformation
     * @throws Exception if something goes wrong
     */
    public void transformXmlContents(String path, String typeName, String transform) throws Exception {

        String timestamp = new SimpleDateFormat("yyyyMMddHHmmss").format(new Date(System.currentTimeMillis()));
        String additionalLogName = "xml_transform_" + timestamp + ".log";
        String additionalLogPath = OpenCms.getSystemInfo().getAbsoluteRfsPathRelativeToWebInf(
            "logs/" + additionalLogName);
        transform = OpenCms.getSystemInfo().getAbsoluteRfsPathRelativeToWebInf(transform);
        try (FileOutputStream additionalLog = new FileOutputStream(additionalLogPath)) {
            CmsMultiplexReport report = new CmsMultiplexReport();
            report.addReport(new CmsPrintStreamReport(m_shell.getOut(), Locale.ENGLISH, false));
            report.addReport(new CmsPrintStreamReport(new PrintStream(additionalLog), Locale.ENGLISH, false));
            try (FileInputStream stream = new FileInputStream(transform)) {
                CmsXmlFileTransformer fileTransformer = new CmsXmlFileTransformer(
                    m_cms,
                    path,
                    typeName,
                    transform,
                    stream,
                    report);
                fileTransformer.run();
            }
        }
    }

    /**
     * Unlocks the current project, required before publishing.<p>
     * @throws Exception if something goes wrong
     */
    public void unlockCurrentProject() throws Exception {

        m_cms.unlockProject(m_cms.getRequestContext().getCurrentProject().getUuid());
    }

    /**
     * Updates a module with another revision in case their module versions differ.<p>
     *
     * @param importFile the name of the import file
     *
     * @throws Exception if something goes wrong
     */
    public void updateModule(String importFile) throws Exception {

        CmsModule module = CmsModuleImportExportHandler.readModuleFromImport(importFile);
        String moduleName = module.getName();
        String moduleVersion = module.getVersionStr();
        m_shell.getOut().println("Updating module " + moduleName + " version " + moduleVersion);
        if (OpenCms.getModuleManager().getModule(moduleName) != null) {

            String oldVersion = OpenCms.getModuleManager().getModule(moduleName).getVersionStr();
            m_shell.getOut().println("Previous version '" + oldVersion + "' detected");
            if (!oldVersion.equals(moduleVersion)) {
                // the import version does not equal the present module version, replace the module
                OpenCms.getModuleManager().replaceModule(
                    m_cms,
                    importFile,
                    new CmsShellReport(m_cms.getRequestContext().getLocale()));
            }
        } else {
            importModule(importFile);
        }
    }

    /**
     * Updates a module with another revision in case their module versions differ.<p>
     *
     * @param importFile the name of the import file
     *
     * @throws Exception if something goes wrong
     */
    public void updateModuleFromDefault(String importFile) throws Exception {

        String exportPath = OpenCms.getSystemInfo().getPackagesRfsPath();
        String fileName = OpenCms.getSystemInfo().getAbsoluteRfsPathRelativeToWebInf(
            exportPath + CmsSystemInfo.FOLDER_MODULES + importFile);
        updateModule(fileName);
    }

    /**
     * Tries to trigger offline index update and wait until it's finished if possible.
     */
    public void updateOfflineIndexes() {

        OpenCms.getSearchManager().updateOfflineIndexes();
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

        I_CmsResourceType t = OpenCms.getResourceManager().getResourceType(type);
        return m_cms.createResource(folder + filename, t, CmsFileUtil.readFile(new File(localfile)), null);
    }

    /**
     * Returns the current users name.<p>
     *
     * @return the current users name
     */
    public String userName() {

        return m_cms.getRequestContext().getCurrentUser().getName();
    }

    /**
     * Returns the version information for this OpenCms instance.<p>
     */
    public void version() {

        m_shell.getOut().println();
        m_shell.getOut().println(
            getMessages().key(Messages.GUI_SHELL_VERSION_1, OpenCms.getSystemInfo().getVersionNumber()));
    }

    /**
     * Returns the current user.<p>
     *
     * @return the current user
     */
    public CmsUser whoami() {

        return m_cms.getRequestContext().getCurrentUser();
    }

    /**
     * Writes the given property to the resource as structure value.<p>
     *
     * @param resourceName the resource to write the value to
     * @param propertyName the property to write the value to
     * @param value the value to write
     *
     * @throws CmsException if something goes wrong
     */
    public void writeProperty(String resourceName, String propertyName, String value) throws CmsException {

        m_cms.lockResource(resourceName);
        m_cms.writePropertyObject(resourceName, new CmsProperty(propertyName, value, null));
    }

    /**
     * Returns the localized messages object for the current user.<p>
     *
     * @return the localized messages object for the current user
     */
    protected CmsMessages getMessages() {

        return m_shell.getMessages();
    }

    /**
     * Checks whether the given user already exists.<p>
     *
     * @param name the user name
     *
     * @return <code>true</code> if the given user already exists
     */
    private boolean existsUser(String name) {

        CmsUser user = null;
        try {
            user = m_cms.readUser(name);
        } catch (CmsException e) {
            // this will happen, if the user does not exist
        }
        return user != null;
    }

    /**
     * Rewrites the content of the given file.<p>
     *
     * @param cms the CmsObject
     * @param resource the resource to rewrite the content for
     *
     * @throws CmsException if something goes wrong
     */
    private void hardTouch(CmsObject cms, CmsResource resource) throws CmsException {

        CmsFile file = cms.readFile(resource);
        cms = OpenCms.initCmsObject(cms);
        cms.getRequestContext().setAttribute(CmsXmlContent.AUTO_CORRECTION_ATTRIBUTE, Boolean.TRUE);
        file.setContents(file.getContents());
        cms.writeFile(file);
    }

    /**
     * Performs a touch operation for a single resource.<p>
     *
     * @param cms the CMS context
     * @param resourceName the resource name of the resource to touch
     * @param timeStamp the new time stamp
     * @param recursive the flag if the touch operation is recursive
     * @param correctDate the flag if the new time stamp is a correct date
     * @param touchContent if the content has to be rewritten
     *
     * @throws CmsException if touching the resource fails
     */
    private void touchSingleResource(
        CmsObject cms,
        String resourceName,
        long timeStamp,
        boolean recursive,
        boolean correctDate,
        boolean touchContent)
    throws CmsException {

        CmsResource sourceRes = cms.readResource(resourceName, CmsResourceFilter.ALL);
        if (!correctDate) {
            // no date value entered, use current resource modification date
            timeStamp = sourceRes.getDateLastModified();
        }
        cms.setDateLastModified(resourceName, timeStamp, recursive);

        if (touchContent) {
            if (sourceRes.isFile()) {
                hardTouch(cms, sourceRes);
            } else if (recursive) {
                Iterator<CmsResource> it = cms.readResources(resourceName, CmsResourceFilter.ALL, true).iterator();
                while (it.hasNext()) {
                    CmsResource subRes = it.next();
                    if (subRes.isFile()) {
                        hardTouch(cms, subRes);
                    }
                }
            }
        }
    }
}