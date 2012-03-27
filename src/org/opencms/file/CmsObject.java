/*
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (c) Alkacon Software GmbH (http://www.alkacon.com)
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
 * For further information about Alkacon Software GmbH, please see the
 * company website: http://www.alkacon.com
 *
 * For further information about OpenCms, please see the
 * project website: http://www.opencms.org
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package org.opencms.file;

import org.opencms.db.CmsDbEntryNotFoundException;
import org.opencms.db.CmsPublishedResource;
import org.opencms.db.CmsResourceState;
import org.opencms.db.CmsSecurityManager;
import org.opencms.db.log.CmsLogEntry;
import org.opencms.db.log.CmsLogFilter;
import org.opencms.file.history.CmsHistoryPrincipal;
import org.opencms.file.history.CmsHistoryProject;
import org.opencms.file.history.I_CmsHistoryResource;
import org.opencms.file.types.I_CmsResourceType;
import org.opencms.lock.CmsLock;
import org.opencms.lock.CmsLockFilter;
import org.opencms.lock.CmsLockType;
import org.opencms.main.CmsException;
import org.opencms.main.CmsIllegalArgumentException;
import org.opencms.main.I_CmsEventListener;
import org.opencms.main.OpenCms;
import org.opencms.relations.CmsRelation;
import org.opencms.relations.CmsRelationFilter;
import org.opencms.relations.CmsRelationType;
import org.opencms.report.I_CmsReport;
import org.opencms.security.CmsAccessControlEntry;
import org.opencms.security.CmsAccessControlList;
import org.opencms.security.CmsOrganizationalUnit;
import org.opencms.security.CmsPermissionSet;
import org.opencms.security.CmsPrincipal;
import org.opencms.security.CmsRole;
import org.opencms.security.CmsSecurityException;
import org.opencms.security.I_CmsPermissionHandler;
import org.opencms.security.I_CmsPrincipal;
import org.opencms.util.CmsPair;
import org.opencms.util.CmsStringUtil;
import org.opencms.util.CmsUUID;
import org.opencms.workplace.CmsWorkplace;
import org.opencms.xml.content.CmsNumberSuffixNameSequence;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/**
 * This pivotal class provides all authorized access to the OpenCms VFS resources.<p>
 * 
 * It encapsulates user identification and permissions.
 * Think of it as an initialized "shell" to access the OpenCms VFS.
 * Every call to a method here will be checked for user permissions
 * according to the <code>{@link org.opencms.file.CmsRequestContext}</code> this CmsObject instance was created with.<p>
 * 
 * From a JSP page running in OpenCms, use <code>{@link org.opencms.jsp.CmsJspBean#getCmsObject()}</code> to gain 
 * access to the current users CmsObject. Usually this is done with a <code>{@link org.opencms.jsp.CmsJspActionElement}</code>.<p>
 * 
 * To generate a new instance of this class in your application, use 
 * <code>{@link org.opencms.main.OpenCms#initCmsObject(String)}</code>. The argument String should be 
 * the name of the guest user, usually "Guest" and more formally obtained by <code>{@link org.opencms.db.CmsDefaultUsers#getUserGuest()}</code>.
 * This will give you an initialized context with guest user permissions.
 * Then use <code>{@link CmsObject#loginUser(String, String)}</code> to log in the user you want.
 * Obviously you need the password for the new user.
 * You should never try to create an instance of this class using the constructor, 
 * this is reserved for internal operation only.<p> 
 * 
 * @since 6.0.0 
 */
public final class CmsObject {

    /** The request context. */
    protected CmsRequestContext m_context;

    /** The security manager to access the cms. */
    protected CmsSecurityManager m_securityManager;

    /**
     * Connects an OpenCms user context to a running database.<p>
     * 
     * <b>Please note:</b> This constructor is internal to OpenCms and not for public use.
     * If you want to create a new instance of a <code>{@link CmsObject}</code> in your application,
     * use <code>{@link org.opencms.main.OpenCms#initCmsObject(String)}</code>.<p>
     * 
     * @param securityManager the security manager
     * @param context the request context that contains the user authentication
     */
    public CmsObject(CmsSecurityManager securityManager, CmsRequestContext context) {

        init(securityManager, context);
    }

    /**
     * Adds a new relation to the given resource.<p>
     * 
     * @param resourceName the name of the source resource
     * @param targetPath the path of the target resource
     * @param type the type of the relation
     * 
     * @throws CmsException if something goes wrong
     */
    public void addRelationToResource(String resourceName, String targetPath, String type) throws CmsException {

        createRelation(resourceName, targetPath, type, false);
    }

    /**
     * Convenience method to add the site root from the current user's 
     * request context to the given resource name.<p>
     *
     * @param resourcename the resource name
     * 
     * @return the resource name with the site root added
     * 
     * @see CmsRequestContext#addSiteRoot(String)
     */
    public String addSiteRoot(String resourcename) {

        return m_context.addSiteRoot(resourcename);
    }

    /**
     * Adds a user to a group.<p>
     * 
     * @param username the name of the user that is to be added to the group
     * @param groupname the name of the group
     * 
     * @throws CmsException if something goes wrong
     */
    public void addUserToGroup(String username, String groupname) throws CmsException {

        m_securityManager.addUserToGroup(m_context, username, groupname, false);
    }

    /**
     * This method works just like {@link CmsObject#adjustLinks(String, String)}, but you can specify multiple source
     * files, and the target folder is interpreted as the folder into which the source files have been copied.<p>
     * 
     * @param sourceFiles the list of source files 
     * @param targetParentFolder the folder into which the source files have been copied
     *  
     * @throws CmsException if something goes wrong 
     */
    public void adjustLinks(List<String> sourceFiles, String targetParentFolder) throws CmsException {

        CmsObject cms = OpenCms.initCmsObject(this);
        cms.getRequestContext().setSiteRoot("");
        List<String> rootSourceFiles = new ArrayList<String>();
        for (String sourceFile : sourceFiles) {
            rootSourceFiles.add(addSiteRoot(sourceFile));
        }
        String rootTargetParentFolder = addSiteRoot(targetParentFolder);

        CmsLinkRewriter rewriter = new CmsLinkRewriter(cms, rootSourceFiles, rootTargetParentFolder);
        rewriter.rewriteLinks();
    }

    /**
     * This method works just like {@link CmsObject#adjustLinks(String, String)}, but instead of specifying 
     * a single source and target folder, you can specify multiple sources and the corresponding targets in 
     * a map of strings.
     * 
     * @param sourceTargetMap a map with the source files as keys and the corresponding targets as values
     * @param targetParentFolder the folder into which the source files have been copied
     *  
     * @throws CmsException if something goes wrong 
     */
    public void adjustLinks(Map<String, String> sourceTargetMap, String targetParentFolder) throws CmsException {

        CmsObject cms = OpenCms.initCmsObject(this);
        cms.getRequestContext().setSiteRoot("");
        List<CmsPair<String, String>> sourcesAndTargets = new ArrayList<CmsPair<String, String>>();
        for (Map.Entry<String, String> entry : sourceTargetMap.entrySet()) {
            String rootSource = addSiteRoot(entry.getKey());
            String rootTarget = addSiteRoot(entry.getValue());
            sourcesAndTargets.add(CmsPair.create(rootSource, rootTarget));
        }
        String rootTargetParentFolder = addSiteRoot(targetParentFolder);
        CmsLinkRewriter rewriter = new CmsLinkRewriter(cms, rootTargetParentFolder, sourcesAndTargets);
        rewriter.rewriteLinks();
    }

    /**
     * Adjusts all links in the target folder that point to the source folder 
     * so that they are kept "relative" in the target folder where possible.
     * 
     * If a link is found from the target folder to the source folder, 
     * then the target folder is checked if a target of the same name 
     * is found also "relative" inside the target Folder, and if so,
     * the link is changed to that "relative" target. This is mainly used to keep 
     * relative links inside a copied folder structure intact. 
     * 
     * Example: Image we have folder /folderA/ that contains files 
     * /folderA/x1 and /folderA/y1. x1 has a link to y1 and y1 to x1. 
     * Now someone copies /folderA/ to /folderB/. So we end up with 
     * /folderB/x2 and /folderB/y2. Because of the link mechanism in OpenCms, 
     * x2 will have a link to y1 and y2 to x1. By using this method, 
     * the links from x2 to y1 will be replaced by a link x2 to y2, 
     * and y2 to x1 with y2 to x2.
     * 
     * Link replacement works for links in XML files as well as relation only
     * type links.
     * 
     * @param sourceFolder the source folder
     * @param targetFolder the target folder
     * 
     * @throws CmsException if something goes wrong 
     */
    public void adjustLinks(String sourceFolder, String targetFolder) throws CmsException {

        String rootSourceFolder = addSiteRoot(sourceFolder);
        String rootTargetFolder = addSiteRoot(targetFolder);
        String siteRoot = getRequestContext().getSiteRoot();
        getRequestContext().setSiteRoot("");
        try {
            CmsLinkRewriter linkRewriter = new CmsLinkRewriter(this, rootSourceFolder, rootTargetFolder);
            linkRewriter.rewriteLinks();
        } finally {
            getRequestContext().setSiteRoot(siteRoot);
        }
    }

    /**
     * Changes the access control for a given resource and a given principal(user/group).<p>
     * 
     * @param resourceName name of the resource
     * @param principalType the type of the principal (currently group or user):
     *      <ul>
     *          <li><code>{@link I_CmsPrincipal#PRINCIPAL_USER}</code></li>
     *          <li><code>{@link I_CmsPrincipal#PRINCIPAL_GROUP}</code></li>
     *      </ul>
     * @param principalName name of the principal
     * @param allowedPermissions bit set of allowed permissions
     * @param deniedPermissions bit set of denied permissions
     * @param flags additional flags of the access control entry
     * 
     * @throws CmsException if something goes wrong
     */
    public void chacc(
        String resourceName,
        String principalType,
        String principalName,
        int allowedPermissions,
        int deniedPermissions,
        int flags) throws CmsException {

        CmsResource res = readResource(resourceName, CmsResourceFilter.ALL);

        CmsAccessControlEntry acEntry = null;
        try {
            I_CmsPrincipal principal = CmsPrincipal.readPrincipal(this, principalType, principalName);
            acEntry = new CmsAccessControlEntry(
                res.getResourceId(),
                principal.getId(),
                allowedPermissions,
                deniedPermissions,
                flags);
            acEntry.setFlagsForPrincipal(principal);
        } catch (CmsDbEntryNotFoundException e) {
            // check for special ids
            if (principalName.equalsIgnoreCase(CmsAccessControlEntry.PRINCIPAL_ALL_OTHERS_NAME)) {
                acEntry = new CmsAccessControlEntry(
                    res.getResourceId(),
                    CmsAccessControlEntry.PRINCIPAL_ALL_OTHERS_ID,
                    allowedPermissions,
                    deniedPermissions,
                    flags);
                acEntry.setFlags(CmsAccessControlEntry.ACCESS_FLAGS_ALLOTHERS);
            } else if (principalName.equalsIgnoreCase(CmsAccessControlEntry.PRINCIPAL_OVERWRITE_ALL_NAME)) {
                acEntry = new CmsAccessControlEntry(
                    res.getResourceId(),
                    CmsAccessControlEntry.PRINCIPAL_OVERWRITE_ALL_ID,
                    allowedPermissions,
                    deniedPermissions,
                    flags);
                acEntry.setFlags(CmsAccessControlEntry.ACCESS_FLAGS_OVERWRITE_ALL);
            } else if (principalType.equalsIgnoreCase(CmsRole.PRINCIPAL_ROLE)) {
                // only vfs managers can set role based permissions
                m_securityManager.checkRoleForResource(m_context, CmsRole.VFS_MANAGER, res);
                // check for role
                CmsRole role = CmsRole.valueOfRoleName(principalName);
                // role based permissions can only be set in the system folder
                if ((role == null) || (!res.getRootPath().startsWith(CmsWorkplace.VFS_PATH_SYSTEM))) {
                    throw e;
                }
                acEntry = new CmsAccessControlEntry(
                    res.getResourceId(),
                    role.getId(),
                    allowedPermissions,
                    deniedPermissions,
                    flags);
                acEntry.setFlags(CmsAccessControlEntry.ACCESS_FLAGS_ROLE);
            } else {
                throw e;
            }
        }

        m_securityManager.writeAccessControlEntry(m_context, res, acEntry);
    }

    /**
     * Changes the access control for a given resource and a given principal(user/group).<p>
     * 
     * @param resourceName name of the resource
     * @param principalType the type of the principal (group or user):
     *      <ul>
     *          <li><code>{@link I_CmsPrincipal#PRINCIPAL_USER}</code></li>
     *          <li><code>{@link I_CmsPrincipal#PRINCIPAL_GROUP}</code></li>
     *      </ul>
     * @param principalName name of the principal
     * @param permissionString the permissions in the format ((+|-)(r|w|v|c|i|o))*
     * 
     * @throws CmsException if something goes wrong
     */
    public void chacc(String resourceName, String principalType, String principalName, String permissionString)
    throws CmsException {

        CmsResource res = readResource(resourceName, CmsResourceFilter.ALL);

        CmsAccessControlEntry acEntry = null;
        try {
            I_CmsPrincipal principal = CmsPrincipal.readPrincipal(this, principalType, principalName);
            acEntry = new CmsAccessControlEntry(res.getResourceId(), principal.getId(), permissionString);
            acEntry.setFlagsForPrincipal(principal);
        } catch (CmsDbEntryNotFoundException e) {
            // check for special ids
            if (principalName.equalsIgnoreCase(CmsAccessControlEntry.PRINCIPAL_ALL_OTHERS_NAME)) {
                acEntry = new CmsAccessControlEntry(
                    res.getResourceId(),
                    CmsAccessControlEntry.PRINCIPAL_ALL_OTHERS_ID,
                    permissionString);
                acEntry.setFlags(CmsAccessControlEntry.ACCESS_FLAGS_ALLOTHERS);
            } else if (principalName.equalsIgnoreCase(CmsAccessControlEntry.PRINCIPAL_OVERWRITE_ALL_NAME)) {
                acEntry = new CmsAccessControlEntry(
                    res.getResourceId(),
                    CmsAccessControlEntry.PRINCIPAL_OVERWRITE_ALL_ID,
                    permissionString);
                acEntry.setFlags(CmsAccessControlEntry.ACCESS_FLAGS_OVERWRITE_ALL);
            } else if (principalType.equalsIgnoreCase(CmsRole.PRINCIPAL_ROLE)) {
                // only vfs managers can set role based permissions
                m_securityManager.checkRoleForResource(m_context, CmsRole.VFS_MANAGER, res);
                // check for role
                CmsRole role = CmsRole.valueOfRoleName(principalName);
                // role based permissions can only be set in the system folder
                if ((role == null)
                    || (!res.getRootPath().startsWith(CmsWorkplace.VFS_PATH_SYSTEM) && !res.getRootPath().equals("/") && !res.getRootPath().equals(
                        "/system"))) {
                    throw e;
                }
                acEntry = new CmsAccessControlEntry(res.getResourceId(), role.getId(), permissionString);
                acEntry.setFlags(CmsAccessControlEntry.ACCESS_FLAGS_ROLE);
            } else {
                throw e;
            }
        }

        m_securityManager.writeAccessControlEntry(m_context, res, acEntry);
    }

    /**
     * Changes the lock of a resource to the current user,
     * that is "steals" the lock from another user.<p>
     * 
     * This is the "steal lock" operation.<p>
     * 
     * @param resource the resource to change the lock
     * 
     * @throws CmsException if something goes wrong
     */
    public void changeLock(CmsResource resource) throws CmsException {

        getResourceType(resource).changeLock(this, m_securityManager, resource);
    }

    /**
     * Changes the lock of a resource to the current user,
     * that is "steals" the lock from another user.<p>
     * 
     * This is the "steal lock" operation.<p>
     * 
     * @param resourcename the name of the resource to change the lock with complete path
     * 
     * @throws CmsException if something goes wrong
     */
    public void changeLock(String resourcename) throws CmsException {

        CmsResource resource = readResource(resourcename, CmsResourceFilter.ALL);
        changeLock(resource);
    }

    /**
     * Returns a list with all sub resources of a given folder that have set the given property, 
     * matching the current property's value with the given old value and replacing it by a given new value.<p>
     *
     * @param resourcename the name of the resource to change the property value
     * @param property the name of the property to change the value
     * @param oldValue the old value of the property, can be a regular expression
     * @param newValue the new value of the property
     * @param recursive if true, change recursively all property values on sub-resources (only for folders)
     *
     * @return a list with the <code>{@link CmsResource}</code>'s where the property value has been changed
     *
     * @throws CmsException if operation was not successful
     */
    public List<CmsResource> changeResourcesInFolderWithProperty(
        String resourcename,
        String property,
        String oldValue,
        String newValue,
        boolean recursive) throws CmsException {

        CmsResource resource = readResource(resourcename, CmsResourceFilter.IGNORE_EXPIRATION);
        return m_securityManager.changeResourcesInFolderWithProperty(
            m_context,
            resource,
            property,
            oldValue,
            newValue,
            recursive);
    }

    /**
     * Changes the resource flags of a resource.<p>
     * 
     * The resource flags are used to indicate various "special" conditions
     * for a resource. Most notably, the "internal only" setting which signals 
     * that a resource can not be directly requested with it's URL.<p>
     *
     * @param resourcename the name of the resource to change the flags for (full current site relative path)
     * @param flags the new flags for this resource
     *
     * @throws CmsException if something goes wrong
     */
    public void chflags(String resourcename, int flags) throws CmsException {

        CmsResource resource = readResource(resourcename, CmsResourceFilter.IGNORE_EXPIRATION);
        getResourceType(resource).chflags(this, m_securityManager, resource, flags);
    }

    /**
     * Changes the resource type of a resource.<p>
     * 
     * OpenCms handles resources according to the resource type,
     * not the file suffix. This is e.g. why a JSP in OpenCms can have the 
     * suffix ".html" instead of ".jsp" only. Changing the resource type
     * makes sense e.g. if you want to make a plain text file a JSP resource,
     * or a binary file an image, etc.<p> 
     *
     * @param resourcename the name of the resource to change the type for (full current site relative path)
     * @param type the new resource type for this resource
     *
     * @throws CmsException if something goes wrong
     */
    public void chtype(String resourcename, int type) throws CmsException {

        CmsResource resource = readResource(resourcename, CmsResourceFilter.IGNORE_EXPIRATION);
        getResourceType(resource).chtype(this, m_securityManager, resource, type);
    }

    /**
     * Copies a resource.<p>
     * 
     * The copied resource will always be locked to the current user
     * after the copy operation.<p>
     * 
     * Siblings will be treated according to the
     * <code>{@link org.opencms.file.CmsResource#COPY_PRESERVE_SIBLING}</code> mode.<p>
     * 
     * @param source the name of the resource to copy (full current site relative path)
     * @param destination the name of the copy destination (full current site relative path)
     * 
     * @throws CmsException if something goes wrong
     * @throws CmsIllegalArgumentException if the <code>destination</code> argument is null or of length 0
     * 
     * @see #copyResource(String, String, CmsResource.CmsResourceCopyMode)
     */
    public void copyResource(String source, String destination) throws CmsException, CmsIllegalArgumentException {

        copyResource(source, destination, CmsResource.COPY_PRESERVE_SIBLING);
    }

    /**
     * Copies a resource.<p>
     * 
     * The copied resource will always be locked to the current user
     * after the copy operation.<p>
     * 
     * The <code>siblingMode</code> parameter controls how to handle siblings 
     * during the copy operation.<br>
     * Possible values for this parameter are: <br>
     * <ul>
     * <li><code>{@link CmsResource#COPY_AS_NEW}</code></li>
     * <li><code>{@link CmsResource#COPY_AS_SIBLING}</code></li>
     * <li><code>{@link CmsResource#COPY_PRESERVE_SIBLING}</code></li>
     * </ul><p>
     * 
     * @param source the name of the resource to copy (full current site relative path)
     * @param destination the name of the copy destination (full current site relative path)
     * @param siblingMode indicates how to handle siblings during copy
     * 
     * @throws CmsException if something goes wrong
     * @throws CmsIllegalArgumentException if the <code>destination</code> argument is null or of length 0
     */
    public void copyResource(String source, String destination, CmsResource.CmsResourceCopyMode siblingMode)
    throws CmsException, CmsIllegalArgumentException {

        CmsResource resource = readResource(source, CmsResourceFilter.IGNORE_EXPIRATION);
        getResourceType(resource).copyResource(this, m_securityManager, resource, destination, siblingMode);
    }

    /**
     * Copies a resource to the current project of the user.<p>
     * 
     * This is used to extend the current users project with the
     * specified resource, in case that the resource is not yet part of the project.
     * The resource is not really copied like in a regular copy operation, 
     * it is in fact only "enabled" in the current users project.<p>   
     * 
     * @param resource the resource to copy to the current project
     * 
     * @throws CmsException if something goes wrong
     */
    public void copyResourceToProject(CmsResource resource) throws CmsException {

        getResourceType(resource).copyResourceToProject(this, m_securityManager, resource);
    }

    /**
     * Copies a resource to the current project of the user.<p>
     * 
     * This is used to extend the current users project with the
     * specified resource, in case that the resource is not yet part of the project.
     * The resource is not really copied like in a regular copy operation, 
     * it is in fact only "enabled" in the current users project.<p>   
     * 
     * @param resourcename the name of the resource to copy to the current project (full current site relative path)
     * 
     * @throws CmsException if something goes wrong
     */
    public void copyResourceToProject(String resourcename) throws CmsException {

        CmsResource resource = readResource(resourcename, CmsResourceFilter.ALL);
        copyResourceToProject(resource);
    }

    /**
     * Counts the locked resources in a project.<p>
     *
     * @param id the id of the project
     * 
     * @return the number of locked resources in this project
     *
     * @throws CmsException if operation was not successful
     */
    public int countLockedResources(CmsUUID id) throws CmsException {

        return m_securityManager.countLockedResources(m_context, id);
    }

    /**
     * Copies access control entries of a given resource to another resource.<p>
     * 
     * Already existing access control entries of the destination resource are removed.<p>
     * 
     * @param sourceName the name of the resource of which the access control entries are copied
     * @param destName the name of the resource to which the access control entries are applied
     * 
     * @throws CmsException if something goes wrong
     */
    public void cpacc(String sourceName, String destName) throws CmsException {

        CmsResource source = readResource(sourceName);
        CmsResource dest = readResource(destName);
        m_securityManager.copyAccessControlEntries(m_context, source, dest);
    }

    /**
     * Creates a new user group.<p>
     * 
     * @param groupFqn the name of the new group
     * @param description the description of the new group
     * @param flags the flags for the new group
     * @param parent the parent group (or <code>null</code>)
     *
     * @return a <code>{@link CmsGroup}</code> object representing the newly created group
     *
     * @throws CmsException if operation was not successful
     */
    public CmsGroup createGroup(String groupFqn, String description, int flags, String parent) throws CmsException {

        return m_securityManager.createGroup(m_context, groupFqn, description, flags, parent);
    }

    /**
     * Creates a new project.<p>
     *
     * @param name the name of the project to create
     * @param description the description for the new project
     * @param groupname the name of the project user group
     * @param managergroupname the name of the project manager group
     * 
     * @return the created project
     * 
     * @throws CmsException if something goes wrong
     */
    public CmsProject createProject(String name, String description, String groupname, String managergroupname)
    throws CmsException {

        return m_securityManager.createProject(
            m_context,
            name,
            description,
            groupname,
            managergroupname,
            CmsProject.PROJECT_TYPE_NORMAL);
    }

    /**
     * Creates a new project.<p>
     *
     * @param name the name of the project to create
     * @param description the description for the new project
     * @param groupname the name of the project user group
     * @param managergroupname the name of the project manager group
     * @param projecttype the type of the project (normal or temporary)
     * 
     * @return the created project
     * 
     * @throws CmsException if operation was not successful
     */
    public CmsProject createProject(
        String name,
        String description,
        String groupname,
        String managergroupname,
        CmsProject.CmsProjectType projecttype) throws CmsException {

        return m_securityManager.createProject(m_context, name, description, groupname, managergroupname, projecttype);
    }

    /**
     * Creates a property definition.<p>
     *
     * Property definitions are valid for all resource types.<p>
     * 
     * @param name the name of the property definition to create
     * 
     * @return the created property definition
     * 
     * @throws CmsException if something goes wrong
     */
    public CmsPropertyDefinition createPropertyDefinition(String name) throws CmsException {

        return (m_securityManager.createPropertyDefinition(m_context, name));
    }

    /**
     * Creates a resource with the given properties and content.
     * Will throw an exception, if a resource with the given name already exists.<p>
     *
     * @param sitePath the site path for the resource
     * @param resource the resource object to be imported
     * @param content the content of the resource
     * @param properties the properties of the resource
     * 
     * @return the imported resource
     * 
     * @throws CmsException if something goes wrong
     */
    public CmsResource createResource(
        String sitePath,
        CmsResource resource,
        byte[] content,
        List<CmsProperty> properties) throws CmsException {

        resource.setUserLastModified(getRequestContext().getCurrentUser().getId());
        resource.setDateLastModified(System.currentTimeMillis());
        // ensure resource record is updated
        resource.setState(CmsResource.STATE_NEW);
        return m_securityManager.createResource(
            m_context,
            m_context.addSiteRoot(sitePath),
            resource,
            content,
            properties);
    }

    /**
     * Creates a new resource of the given resource type with 
     * empty content and no properties.<p>
     * 
     * @param resourcename the name of the resource to create (full current site relative path)
     * @param type the type of the resource to create
     * 
     * @return the created resource
     * 
     * @throws CmsException if something goes wrong
     * @throws CmsIllegalArgumentException if the given <code>resourcename</code> is null or of length 0
     * 
     * @see #createResource(String, int, byte[], List)
     */
    public CmsResource createResource(String resourcename, int type) throws CmsException, CmsIllegalArgumentException {

        return createResource(resourcename, type, new byte[0], new ArrayList<CmsProperty>(0));
    }

    /**
     * Creates a new resource of the given resource type
     * with the provided content and properties.<p>
     * 
     * @param resourcename the name of the resource to create (full current site relative path)
     * @param type the type of the resource to create
     * @param content the contents for the new resource
     * @param properties the properties for the new resource
     * 
     * @return the created resource
     * 
     * @throws CmsException if something goes wrong
     * @throws CmsIllegalArgumentException if the <code>resourcename</code> argument is null or of length 0
     */
    public CmsResource createResource(String resourcename, int type, byte[] content, List<CmsProperty> properties)
    throws CmsException, CmsIllegalArgumentException {

        return getResourceType(type).createResource(this, m_securityManager, resourcename, content, properties);
    }

    /**
     * Creates a new sibling of the source resource.<p>
     * 
     * @param source the name of the resource to create a sibling for with complete path
     * @param destination the name of the sibling to create with complete path
     * @param properties the individual properties for the new sibling
     * 
     * @return the new created sibling
     * 
     * @throws CmsException if something goes wrong
     */
    public CmsResource createSibling(String source, String destination, List<CmsProperty> properties)
    throws CmsException {

        CmsResource resource = readResource(source, CmsResourceFilter.IGNORE_EXPIRATION);
        return getResourceType(resource).createSibling(this, m_securityManager, resource, destination, properties);
    }

    /**
     * Creates the project for the temporary workplace files.<p>
     *
     * @return the created project for the temporary workplace files
     * 
     * @throws CmsException if something goes wrong
     */
    public CmsProject createTempfileProject() throws CmsException {

        return m_securityManager.createTempfileProject(m_context);
    }

    /**
     * Creates a new user.<p>
     * 
     * @param userFqn the name for the new user
     * @param password the password for the new user
     * @param description the description for the new user
     * @param additionalInfos the additional infos for the user
     *
     * @return the created user
     * 
     * @throws CmsException if something goes wrong
     */
    public CmsUser createUser(String userFqn, String password, String description, Map<String, Object> additionalInfos)
    throws CmsException {

        return m_securityManager.createUser(m_context, userFqn, password, description, additionalInfos);
    }

    /**
     * Deletes all published resource entries.<p>
     * 
     * @param linkType the type of resource deleted (0= non-parameter, 1=parameter)
     * 
     * @throws CmsException if something goes wrong
     */
    public void deleteAllStaticExportPublishedResources(int linkType) throws CmsException {

        m_securityManager.deleteAllStaticExportPublishedResources(m_context, linkType);
    }

    /**
     * Deletes a group, where all permissions, users and children of the group
     * are transfered to a replacement group.<p>
     * 
     * @param groupId the id of the group to be deleted
     * @param replacementId the id of the group to be transfered, can be <code>null</code>
     *
     * @throws CmsException if operation was not successful
     */
    public void deleteGroup(CmsUUID groupId, CmsUUID replacementId) throws CmsException {

        m_securityManager.deleteGroup(m_context, groupId, replacementId);
    }

    /**
     * Deletes a user group.<p>
     *
     * Only groups that contain no subgroups can be deleted.<p>
     * 
     * @param group the name of the group
     * 
     * @throws CmsException if operation was not successful
     */
    public void deleteGroup(String group) throws CmsException {

        m_securityManager.deleteGroup(m_context, group);
    }

    /**
     * Deletes the versions from the history tables, keeping the given number of versions per resource.<p>
     * 
     * @param versionsToKeep number of versions to keep, is ignored if negative 
     * @param versionsDeleted number of versions to keep for deleted resources, is ignored if negative
     * @param timeDeleted deleted resources older than this will also be deleted, is ignored if negative
     * @param report the report for output logging
     * 
     * @throws CmsException if operation was not successful
     */
    public void deleteHistoricalVersions(int versionsToKeep, int versionsDeleted, long timeDeleted, I_CmsReport report)
    throws CmsException {

        m_securityManager.deleteHistoricalVersions(m_context, versionsToKeep, versionsDeleted, timeDeleted, report);
    }

    /**
     * Deletes the log entries matching the given filter.<p>
     *
     * @param filter the filter to use for deleting the log entries
     *
     * @throws CmsException if something goes wrong
     * 
     * @see CmsSecurityManager#deleteLogEntries(CmsRequestContext, CmsLogFilter)
     * @see #getLogEntries(CmsLogFilter)
     */
    public void deleteLogEntries(CmsLogFilter filter) throws CmsException {

        m_securityManager.deleteLogEntries(m_context, filter);
    }

    /**
     * Deletes a project.<p>
     *
     * All resources inside the project have to be be reset to their online state.<p>
     * 
     * @param id the id of the project to delete
     *
     * @throws CmsException if operation was not successful
     */
    public void deleteProject(CmsUUID id) throws CmsException {

        m_securityManager.deleteProject(m_context, id);
    }

    /**
     * Deletes a property definition.<p>
     *
     * @param name the name of the property definition to delete
     *
     * @throws CmsException if something goes wrong
     */
    public void deletePropertyDefinition(String name) throws CmsException {

        m_securityManager.deletePropertyDefinition(m_context, name);
    }

    /**
     * Deletes the relations to a given resource.<p>
     *
     * @param resourceName the resource to delete the relations from
     * @param filter the filter to use for deleting the relations
     *
     * @throws CmsException if something goes wrong
     */
    public void deleteRelationsFromResource(String resourceName, CmsRelationFilter filter) throws CmsException {

        CmsResource resource = readResource(resourceName, CmsResourceFilter.ALL);
        m_securityManager.deleteRelationsForResource(m_context, resource, filter);
    }

    /**
     * Deletes a resource given its name.<p>
     * 
     * The <code>siblingMode</code> parameter controls how to handle siblings 
     * during the delete operation.<br>
     * Possible values for this parameter are: <br>
     * <ul>
     * <li><code>{@link CmsResource#DELETE_REMOVE_SIBLINGS}</code></li>
     * <li><code>{@link CmsResource#DELETE_PRESERVE_SIBLINGS}</code></li>
     * </ul><p>
     * 
     * @param resourcename the name of the resource to delete (full current site relative path)
     * @param siblingMode indicates how to handle siblings of the deleted resource
     *
     * @throws CmsException if something goes wrong
     */
    public void deleteResource(String resourcename, CmsResource.CmsResourceDeleteMode siblingMode) throws CmsException {

        // throw the exception if resource name is an empty string
        if (CmsStringUtil.isEmptyOrWhitespaceOnly(resourcename)) {
            throw new CmsVfsResourceNotFoundException(Messages.get().container(
                Messages.ERR_DELETE_RESOURCE_1,
                resourcename));
        }

        CmsResource resource = readResource(resourcename, CmsResourceFilter.IGNORE_EXPIRATION);
        getResourceType(resource).deleteResource(this, m_securityManager, resource, siblingMode);
    }

    /**
     * Deletes a published resource entry.<p>
     * 
     * @param resourceName The name of the resource to be deleted in the static export
     * @param linkType the type of resource deleted (0= non-parameter, 1=parameter)
     * @param linkParameter the parameters of the resource
     * 
     * @throws CmsException if something goes wrong
     */
    public void deleteStaticExportPublishedResource(String resourceName, int linkType, String linkParameter)
    throws CmsException {

        m_securityManager.deleteStaticExportPublishedResource(m_context, resourceName, linkType, linkParameter);
    }

    /**
     * Deletes a user.<p>
     *
     * @param userId the id of the user to be deleted
     *
     * @throws CmsException if operation was not successful
     */
    public void deleteUser(CmsUUID userId) throws CmsException {

        m_securityManager.deleteUser(m_context, userId);
    }

    /**
     * Deletes a user, where all permissions and resources attributes of the user
     * were transfered to a replacement user.<p>
     *
     * @param userId the id of the user to be deleted
     * @param replacementId the id of the user to be transfered, can be <code>null</code>
     *
     * @throws CmsException if operation was not successful
     */
    public void deleteUser(CmsUUID userId, CmsUUID replacementId) throws CmsException {

        m_securityManager.deleteUser(m_context, userId, replacementId);
    }

    /**
     * Deletes a user.<p>
     * 
     * @param username the name of the user to be deleted
     *
     * @throws CmsException if operation was not successful
     */
    public void deleteUser(String username) throws CmsException {

        m_securityManager.deleteUser(m_context, username);
    }

    /**
     * Checks the availability of a resource in the VFS,
     * using the <code>{@link CmsResourceFilter#DEFAULT}</code> filter.<p> 
     *
     * A resource may be of type <code>{@link CmsFile}</code> or 
     * <code>{@link CmsFolder}</code>.<p>
     * 
     * This method also takes into account the user permissions, so if 
     * the given resource exists, but the current user has not the required 
     * permissions, then this method will return <code>false</code>.<p>
     *
     * @param structureId the structure id of the resource to check
     *
     * @return <code>true</code> if the resource is available
     *
     * @see #readResource(CmsUUID)
     * @see #existsResource(CmsUUID, CmsResourceFilter)
     */
    public boolean existsResource(CmsUUID structureId) {

        return existsResource(structureId, CmsResourceFilter.DEFAULT);
    }

    /**
     * Checks the availability of a resource in the VFS,
     * using the <code>{@link CmsResourceFilter#DEFAULT}</code> filter.<p> 
     *
     * A resource may be of type <code>{@link CmsFile}</code> or 
     * <code>{@link CmsFolder}</code>.<p>  
     *
     * The specified filter controls what kind of resources should be "found" 
     * during the read operation. This will depend on the application. For example, 
     * using <code>{@link CmsResourceFilter#DEFAULT}</code> will only return currently
     * "valid" resources, while using <code>{@link CmsResourceFilter#IGNORE_EXPIRATION}</code>
     * will ignore the date release / date expired information of the resource.<p>
     * 
     * This method also takes into account the user permissions, so if 
     * the given resource exists, but the current user has not the required 
     * permissions, then this method will return <code>false</code>.<p>
     *
     * @param structureId the structure id of the resource to check
     * @param filter the resource filter to use while checking
     *
     * @return <code>true</code> if the resource is available
     * 
     * @see #readResource(CmsUUID)
     * @see #readResource(CmsUUID, CmsResourceFilter)
     */
    public boolean existsResource(CmsUUID structureId, CmsResourceFilter filter) {

        return m_securityManager.existsResource(m_context, structureId, filter);
    }

    /**
     * Checks the availability of a resource in the VFS,
     * using the <code>{@link CmsResourceFilter#DEFAULT}</code> filter.<p> 
     *
     * A resource may be of type <code>{@link CmsFile}</code> or 
     * <code>{@link CmsFolder}</code>.<p>
     * 
     * This method also takes into account the user permissions, so if 
     * the given resource exists, but the current user has not the required 
     * permissions, then this method will return <code>false</code>.<p>
     *
     * @param resourcename the name of the resource to check (full current site relative path)
     *
     * @return <code>true</code> if the resource is available
     *
     * @see #readResource(String)
     * @see #existsResource(String, CmsResourceFilter)
     */
    public boolean existsResource(String resourcename) {

        return existsResource(resourcename, CmsResourceFilter.DEFAULT);
    }

    /**
     * Checks the availability of a resource in the VFS,
     * using the provided filter.<p> 
     *
     * A resource may be of type <code>{@link CmsFile}</code> or 
     * <code>{@link CmsFolder}</code>.<p>  
     *
     * The specified filter controls what kind of resources should be "found" 
     * during the read operation. This will depend on the application. For example, 
     * using <code>{@link CmsResourceFilter#DEFAULT}</code> will only return currently
     * "valid" resources, while using <code>{@link CmsResourceFilter#IGNORE_EXPIRATION}</code>
     * will ignore the date release / date expired information of the resource.<p>
     * 
     * This method also takes into account the user permissions, so if 
     * the given resource exists, but the current user has not the required 
     * permissions, then this method will return <code>false</code>.<p>
     *
     * @param resourcename the name of the resource to check (full current site relative path)
     * @param filter the resource filter to use while checking
     *
     * @return <code>true</code> if the resource is available
     * 
     * @see #readResource(String)
     * @see #readResource(String, CmsResourceFilter)
     */
    public boolean existsResource(String resourcename, CmsResourceFilter filter) {

        return m_securityManager.existsResource(m_context, addSiteRoot(resourcename), filter);
    }

    /**
     * Returns the list of access control entries of a resource given its name.<p>
     * 
     * @param resourceName the name of the resource
     * 
     * @return a list of <code>{@link CmsAccessControlEntry}</code> objects
     * 
     * @throws CmsException if something goes wrong
     */
    public List<CmsAccessControlEntry> getAccessControlEntries(String resourceName) throws CmsException {

        return getAccessControlEntries(resourceName, true);
    }

    /**
     * Returns the list of access control entries of a resource given its name.<p>
     * 
     * @param resourceName the name of the resource
     * @param getInherited <code>true</code>, if inherited access control entries should be returned, too
     * 
     * @return a list of <code>{@link CmsAccessControlEntry}</code> objects defining all permissions for the given resource
     * 
     * @throws CmsException if something goes wrong
     */
    public List<CmsAccessControlEntry> getAccessControlEntries(String resourceName, boolean getInherited)
    throws CmsException {

        CmsResource res = readResource(resourceName, CmsResourceFilter.ALL);
        return m_securityManager.getAccessControlEntries(m_context, res, getInherited);
    }

    /**
     * Returns the access control list (summarized access control entries) of a given resource.<p>
     * 
     * @param resourceName the name of the resource
     * 
     * @return the access control list of the resource
     * 
     * @throws CmsException if something goes wrong
     */
    public CmsAccessControlList getAccessControlList(String resourceName) throws CmsException {

        return getAccessControlList(resourceName, false);
    }

    /**
     * Returns the access control list (summarized access control entries) of a given resource.<p>
     * 
     * If <code>inheritedOnly</code> is set, only inherited access control entries are returned.<p>
     * 
     * @param resourceName the name of the resource
     * @param inheritedOnly if set, the non-inherited entries are skipped
     * 
     * @return the access control list of the resource
     * 
     * @throws CmsException if something goes wrong
     */
    public CmsAccessControlList getAccessControlList(String resourceName, boolean inheritedOnly) throws CmsException {

        CmsResource res = readResource(resourceName, CmsResourceFilter.ALL);
        return m_securityManager.getAccessControlList(m_context, res, inheritedOnly);
    }

    /**
     * Returns a list with all projects from history.<p>
     *
     * @return list of <code>{@link CmsHistoryProject}</code> objects 
     *           with all projects from history
     *
     * @throws CmsException  if operation was not successful
     */
    public List<CmsHistoryProject> getAllHistoricalProjects() throws CmsException {

        return m_securityManager.getAllHistoricalProjects(m_context);
    }

    /**
     * Gets all URL names for a given structure id.<p>
     * 
     * @param id the structure id 
     * @return the list of all URL names for that structure id 
     * 
     * @throws CmsException if something goes wrong 
     */
    public List<String> getAllUrlNames(CmsUUID id) throws CmsException {

        return m_securityManager.readAllUrlNameMappingEntries(m_context, id);
    }

    /**
     * Returns a list of child resources to the given resource that can not be locked by the current user.<p>
     * 
     * @param resource the resource
     * 
     * @return a list of child resources to the given resource that can not be locked by the current user
     * 
     * @throws CmsException if something goes wrong reading the resources
     */
    public List<CmsResource> getBlockingLockedResources(CmsResource resource) throws CmsException {

        if (resource.isFolder()) {
            CmsLockFilter blockingFilter = CmsLockFilter.FILTER_ALL;
            blockingFilter = blockingFilter.filterNotLockableByUser(getRequestContext().getCurrentUser());
            return getLockedResources(resource, blockingFilter);
        }
        return Collections.<CmsResource> emptyList();
    }

    /**
     * Returns a list of child resources to the given resource that can not be locked by the current user.<p>
     * 
     * @param resourceName the resource site path
     * 
     * @return a list of child resources to the given resource that can not be locked by the current user
     * 
     * @throws CmsException if something goes wrong reading the resources
     */
    public List<CmsResource> getBlockingLockedResources(String resourceName) throws CmsException {

        CmsResource resource = readResource(resourceName);
        return getBlockingLockedResources(resource);
    }

    /**
     * Returns all child groups of a group.<p>
     * 
     * @param groupname the name of the group
     * @param includeSubChildren if set also returns all sub-child groups of the given group
     *
     * @return a list of all child <code>{@link CmsGroup}</code> objects or <code>null</code>
     * 
     * @throws CmsException if operation was not successful
     */
    public List<CmsGroup> getChildren(String groupname, boolean includeSubChildren) throws CmsException {

        return m_securityManager.getChildren(m_context, groupname, includeSubChildren);
    }

    /**
     * Returns the detail name of a resource.<p>
     * 
     * The detail view URI of a content element consists of its detail page URI and the detail name returned by this 
     * method.<p>
     * 
     * @param res the resource for which the detail name should be retrieved
     * @param locale the locale for the detail name 
     * @param defaultLocales the default locales for the detail name 
     *   
     * @return the detail name 
     * @throws CmsException if something goes wrong 
     */
    public String getDetailName(CmsResource res, Locale locale, List<Locale> defaultLocales) throws CmsException {

        String urlName = readBestUrlName(res.getStructureId(), locale, defaultLocales);
        if (urlName == null) {
            urlName = res.getStructureId().toString();
        }
        return urlName;
    }

    /**
     * Returns all file resources contained in a folder.<p>
     * 
     * The result is filtered according to the rules of 
     * the <code>{@link CmsResourceFilter#DEFAULT}</code> filter.<p>
     * 
     * @param resourcename the full current site relative path of the resource to return the child resources for
     * 
     * @return a list of all child files as <code>{@link CmsResource}</code> objects
     * 
     * @throws CmsException if something goes wrong
     * 
     * @see #getFilesInFolder(String, CmsResourceFilter)
     */
    public List<CmsResource> getFilesInFolder(String resourcename) throws CmsException {

        return getFilesInFolder(resourcename, CmsResourceFilter.DEFAULT);
    }

    /**
     * Returns all file resources contained in a folder.<p>
     * 
     * With the <code>{@link CmsResourceFilter}</code> provided as parameter
     * you can control if you want to include deleted, invisible or 
     * time-invalid resources in the result.<p>
     * 
     * @param resourcename the full path of the resource to return the child resources for
     * @param filter the resource filter to use
     * 
     * @return a list of all child file as <code>{@link CmsResource}</code> objects
     * 
     * @throws CmsException if something goes wrong
     */
    public List<CmsResource> getFilesInFolder(String resourcename, CmsResourceFilter filter) throws CmsException {

        CmsResource resource = readResource(resourcename, CmsResourceFilter.ALL);
        return m_securityManager.readChildResources(m_context, resource, filter, false, true);
    }

    /**
     * Returns all the groups the given user belongs to.<p>
     *
     * @param username the name of the user
     * @param directGroupsOnly if set only the direct assigned groups will be returned, if not also indirect roles
     * 
     * @return a list of <code>{@link CmsGroup}</code> objects
     * 
     * @throws CmsException if operation was not successful
     */
    public List<CmsGroup> getGroupsOfUser(String username, boolean directGroupsOnly) throws CmsException {

        return getGroupsOfUser(username, directGroupsOnly, true);
    }

    /**
     * Returns all the groups the given user belongs to.<p>
     *
     * @param username the name of the user
     * @param directGroupsOnly if set only the direct assigned groups will be returned, if not also indirect roles
     * @param includeOtherOus if to include groups of other organizational units
     * 
     * @return a list of <code>{@link CmsGroup}</code> objects
     * 
     * @throws CmsException if operation was not successful
     */
    public List<CmsGroup> getGroupsOfUser(String username, boolean directGroupsOnly, boolean includeOtherOus)
    throws CmsException {

        return getGroupsOfUser(username, directGroupsOnly, includeOtherOus, m_context.getRemoteAddress());
    }

    /**
     * Returns the groups of a user filtered by the specified IP address.<p>
     *
     * @param username the name of the user
     * @param directGroupsOnly if set only the direct assigned groups will be returned, if not also indirect roles
     * @param remoteAddress the IP address to filter the groups in the result list
     * @param includeOtherOus if to include groups of other organizational units
     * 
     * @return a list of <code>{@link CmsGroup}</code> objects filtered by the specified IP address
     * 
     * @throws CmsException if operation was not successful
     */
    public List<CmsGroup> getGroupsOfUser(
        String username,
        boolean directGroupsOnly,
        boolean includeOtherOus,
        String remoteAddress) throws CmsException {

        return m_securityManager.getGroupsOfUser(
            m_context,
            username,
            (includeOtherOus ? "" : CmsOrganizationalUnit.getParentFqn(username)),
            includeOtherOus,
            false,
            directGroupsOnly,
            remoteAddress);
    }

    /**
     * Returns the edition lock state for a specified resource.<p>
     * 
     * If the resource is waiting to be publish you might get a lock of type {@link CmsLockType#PUBLISH}.<p>
     * 
     * @param resource the resource to return the edition lock state for
     * 
     * @return the edition lock state for the specified resource
     * 
     * @throws CmsException if something goes wrong
     */
    public CmsLock getLock(CmsResource resource) throws CmsException {

        return m_securityManager.getLock(m_context, resource);
    }

    /**
     * Returns the lock state for a specified resource name.<p>
     * 
     * If the resource is waiting to be publish you might get a lock of type {@link CmsLockType#PUBLISH}.<p>
     * 
     * @param resourcename the name if the resource to get the lock state for (full current site relative path)
     * 
     * @return the lock state for the specified resource
     * 
     * @throws CmsException if something goes wrong
     */
    public CmsLock getLock(String resourcename) throws CmsException {

        CmsResource resource = readResource(resourcename, CmsResourceFilter.ALL);
        return getLock(resource);
    }

    /**
     * Returns all locked resources within a folder or matches the lock of the given resource.<p>
     *
     * @param resource the resource to check
     * @param filter the lock filter
     * 
     * @return a list of locked resources
     *
     * @throws CmsException if operation was not successful
     */
    public List<CmsResource> getLockedResources(CmsResource resource, CmsLockFilter filter) throws CmsException {

        return m_securityManager.getLockedResourcesObjects(m_context, resource, filter);
    }

    /**
     * Returns all locked resources within a folder or matches the lock of the given resource.<p>
     *
     * @param resourceName the name of the resource to check
     * @param filter the lock filter
     * 
     * @return a list of locked resource paths (relative to current site)
     *
     * @throws CmsException if operation was not successful
     */
    public List<String> getLockedResources(String resourceName, CmsLockFilter filter) throws CmsException {

        CmsResource resource = readResource(resourceName, CmsResourceFilter.ALL);
        return m_securityManager.getLockedResources(m_context, resource, filter);
    }

    /**
     * Returns all locked resources within a folder or matches the lock of the given resource, but uses a cache for resource lookup.<p>
     *
     * @param resource the resource to check
     * @param filter the lock filter
     * @param cache the cache to use for resource lookups 
     * 
     * @return a list of locked resources
     *
     * @throws CmsException if operation was not successful
     */
    public List<CmsResource> getLockedResourcesWithCache(
        CmsResource resource,
        CmsLockFilter filter,
        Map<String, CmsResource> cache) throws CmsException {

        return m_securityManager.getLockedResourcesObjectsWithCache(m_context, resource, filter, cache);
    }

    /**
     * Returns all log entries matching the given filter.<p> 
     * 
     * @param filter the filter to match the relation 
     * 
     * @return a list containing all log entries matching the given filter
     * 
     * @throws CmsException if something goes wrong
     * 
     * @see CmsSecurityManager#getLogEntries(CmsRequestContext, CmsLogFilter)
     * @see #deleteLogEntries(CmsLogFilter)
     */
    public List<CmsLogEntry> getLogEntries(CmsLogFilter filter) throws CmsException {

        return m_securityManager.getLogEntries(m_context, filter);
    }

    /**
     * Returns the name a resource would have if it were moved to the
     * "lost and found" folder. <p>
     * 
     * In general, it is the same name as the given resource has, the only exception is
     * if a resource in the "lost and found" folder with the same name already exists. 
     * In such case, a counter is added to the resource name.<p>
     * 
     * @param resourcename the name of the resource to get the "lost and found" name for (full current site relative path)
     *
     * @return the tentative name of the resource inside the "lost and found" folder
     * 
     * @throws CmsException if something goes wrong
     * 
     * @see #moveToLostAndFound(String)
     */
    public String getLostAndFoundName(String resourcename) throws CmsException {

        CmsResource resource = readResource(resourcename, CmsResourceFilter.ALL);
        return m_securityManager.moveToLostAndFound(m_context, resource, true);
    }

    /**
     * Returns the parent group of a group.<p>
     *
     * @param groupname the name of the group
     * 
     * @return group the parent group or <code>null</code>
     * 
     * @throws CmsException if operation was not successful
     */
    public CmsGroup getParent(String groupname) throws CmsException {

        return m_securityManager.getParent(m_context, groupname);
    }

    /**
     * Returns the set of permissions of the current user for a given resource.<p>
     * 
     * @param resourceName the name of the resource
     * 
     * @return the bit set of the permissions of the current user
     * 
     * @throws CmsException if something goes wrong
     */
    public CmsPermissionSet getPermissions(String resourceName) throws CmsException {

        return getPermissions(resourceName, m_context.getCurrentUser().getName());
    }

    /**
     * Returns the set of permissions of a given user for a given resource.<p>
     * 
     * @param resourceName the name of the resource
     * @param userName the name of the user
     * 
     * @return the current permissions on this resource
     * 
     * @throws CmsException if something goes wrong
     */
    public CmsPermissionSet getPermissions(String resourceName, String userName) throws CmsException {

        // reading permissions is allowed even if the resource is marked as deleted
        CmsResource resource = readResource(resourceName, CmsResourceFilter.ALL);
        CmsUser user = readUser(userName);
        return m_securityManager.getPermissions(m_context, resource, user);
    }

    /**
     * Returns all relations for the given resource matching the given filter.<p> 
     * 
     * You should have view/read permissions on the given resource.<p>
     * 
     * You may become source and/or target paths to resource you do not have view/read permissions on.<p> 
     * 
     * @param resource the resource to retrieve the relations for
     * @param filter the filter to match the relation 
     * 
     * @return a List containing all {@link org.opencms.relations.CmsRelation} 
     *          objects for the given resource matching the given filter
     * 
     * @throws CmsException if something goes wrong
     * 
     * @see CmsSecurityManager#getRelationsForResource(CmsRequestContext, CmsResource, CmsRelationFilter)
     */
    public List<CmsRelation> getRelationsForResource(CmsResource resource, CmsRelationFilter filter)
    throws CmsException {

        return m_securityManager.getRelationsForResource(m_context, resource, filter);
    }

    /**
     * Returns all relations for the given resource matching the given filter.<p> 
     * 
     * You should have view/read permissions on the given resource.<p>
     * 
     * You may become source and/or target paths to resource you do not have view/read permissions on.<p> 
     * 
     * @param resourceName the name of the resource to retrieve the relations for
     * @param filter the filter to match the relation 
     * 
     * @return a List containing all {@link org.opencms.relations.CmsRelation} 
     *          objects for the given resource matching the given filter
     * 
     * @throws CmsException if something goes wrong
     * 
     * @see CmsSecurityManager#getRelationsForResource(CmsRequestContext, CmsResource, CmsRelationFilter)
     */
    public List<CmsRelation> getRelationsForResource(String resourceName, CmsRelationFilter filter) throws CmsException {

        return getRelationsForResource(readResource(resourceName, CmsResourceFilter.ALL), filter);
    }

    /**
     * Returns the current users request context.<p>
     *
     * This request context is used to authenticate the user for all 
     * OpenCms operations. It also contains the request runtime settings, e.g.
     * about the current site this request was made on.<p>
     *
     * @return the current users request context
     */
    public CmsRequestContext getRequestContext() {

        return m_context;
    }

    /**
     * Returns all resources associated to a given principal via an ACE with the given permissions.<p> 
     * 
     * If the <code>includeAttr</code> flag is set it returns also all resources associated to 
     * a given principal through some of following attributes.<p> 
     * 
     * <ul>
     *    <li>User Created</li>
     *    <li>User Last Modified</li>
     * </ul><p>
     * 
     * @param principalId the id of the principal
     * @param permissions a set of permissions to match, can be <code>null</code> for all ACEs
     * @param includeAttr a flag to include resources associated by attributes
     * 
     * @return a set of <code>{@link CmsResource}</code> objects
     * 
     * @throws CmsException if something goes wrong
     */
    public Set<CmsResource> getResourcesForPrincipal(
        CmsUUID principalId,
        CmsPermissionSet permissions,
        boolean includeAttr) throws CmsException {

        return m_securityManager.getResourcesForPrincipal(getRequestContext(), principalId, permissions, includeAttr);
    }

    /**
     * Returns all child resources of a resource, that is the resources
     * contained in a folder.<p>
     * 
     * With the <code>{@link CmsResourceFilter}</code> provided as parameter
     * you can control if you want to include deleted, invisible or 
     * time-invalid resources in the result.<p>
     * 
     * This method is mainly used by the workplace explorer.<p>
     * 
     * @param resourcename the full current site relative path of the resource to return the child resources for
     * @param filter the resource filter to use
     * 
     * @return a list of all child <code>{@link CmsResource}</code>s
     * 
     * @throws CmsException if something goes wrong
     */
    public List<CmsResource> getResourcesInFolder(String resourcename, CmsResourceFilter filter) throws CmsException {

        CmsResource resource = readResource(resourcename, CmsResourceFilter.ALL);
        return m_securityManager.readChildResources(m_context, resource, filter, true, true);
    }

    /**
     * Adjusts the absolute resource root path for the current site.<p> 
     * 
     * The full root path of a resource is always available using
     * <code>{@link CmsResource#getRootPath()}</code>. From this name this method cuts 
     * of the current site root using 
     * <code>{@link CmsRequestContext#removeSiteRoot(String)}</code>.<p>
     * 
     * If the resource root path does not start with the current site root,
     * it is left untouched.<p>
     * 
     * @param resource the resource to get the adjusted site root path for
     * 
     * @return the absolute resource path adjusted for the current site
     * 
     * @see CmsRequestContext#removeSiteRoot(String)
     * @see CmsRequestContext#getSitePath(CmsResource)
     * @see CmsResource#getRootPath()
     */
    public String getSitePath(CmsResource resource) {

        return m_context.getSitePath(resource);
    }

    /**
     * Returns all folder resources contained in a folder.<p>
     * 
     * The result is filtered according to the rules of 
     * the <code>{@link CmsResourceFilter#DEFAULT}</code> filter.<p>
     * 
     * @param resourcename the full current site relative path of the resource to return the child resources for. 
     * 
     * @return a list of all child file as <code>{@link CmsResource}</code> objects
     * 
     * @throws CmsException if something goes wrong
     * 
     * @see #getSubFolders(String, CmsResourceFilter)
     */
    public List<CmsResource> getSubFolders(String resourcename) throws CmsException {

        return getSubFolders(resourcename, CmsResourceFilter.DEFAULT);
    }

    /**
     * Returns all folder resources contained in a folder.<p>
     * 
     * With the <code>{@link CmsResourceFilter}</code> provided as parameter
     * you can control if you want to include deleted, invisible or 
     * time-invalid resources in the result.<p>
     * 
     * @param resourcename the full current site relative path of the resource to return the child resources for. 
     * 
     * @return a list of all child folder <code>{@link CmsResource}</code>s
     * @param filter the resource filter to use
     * 
     * @throws CmsException if something goes wrong
     */
    public List<CmsResource> getSubFolders(String resourcename, CmsResourceFilter filter) throws CmsException {

        CmsResource resource = readResource(resourcename, CmsResourceFilter.ALL);
        return m_securityManager.readChildResources(m_context, resource, filter, true, false);
    }

    /**
     * Returns the newest URL names for the given structure id for each locale.<p>
     * 
     * @param id the structure id 
     * @return the list of URL names for each locale 
     * 
     * @throws CmsException if something goes wrong 
     */
    public List<String> getUrlNamesForAllLocales(CmsUUID id) throws CmsException {

        return m_securityManager.readUrlNamesForAllLocales(m_context, id);
    }

    /**
     * Returns all direct users of a given group.<p>
     *
     * Users that are "indirectly" in the group are not returned in the result.<p>
     *
     * @param groupname the name of the group to get all users for
     * 
     * @return all <code>{@link CmsUser}</code> objects in the group
     *
     * @throws CmsException if operation was not successful
     */
    public List<CmsUser> getUsersOfGroup(String groupname) throws CmsException {

        return getUsersOfGroup(groupname, true);
    }

    /**
     * Returns all direct users of a given group.<p>
     *
     * Users that are "indirectly" in the group are not returned in the result.<p>
     *
     * @param groupname the name of the group to get all users for
     * @param includeOtherOus if the result should include users of other ous
     * 
     * @return all <code>{@link CmsUser}</code> objects in the group
     *
     * @throws CmsException if operation was not successful
     */
    public List<CmsUser> getUsersOfGroup(String groupname, boolean includeOtherOus) throws CmsException {

        return m_securityManager.getUsersOfGroup(m_context, groupname, includeOtherOus, true, false);
    }

    /**
     * Checks if the current user has required permissions to access a given resource.<p>
     * 
     * @param resource the resource to check the permissions for
     * @param requiredPermissions the set of permissions to check for
     * 
     * @return <code>true</code> if the required permissions are satisfied
     * 
     * @throws CmsException if something goes wrong
     */
    public boolean hasPermissions(CmsResource resource, CmsPermissionSet requiredPermissions) throws CmsException {

        return m_securityManager.hasPermissions(m_context, resource, requiredPermissions, true, CmsResourceFilter.ALL).isAllowed();
    }

    /**
     * Checks if the current user has required permissions to access a given resource.<p>
     * 
     * @param resource the resource to check the permissions for
     * @param requiredPermissions the set of permissions to check for
     * @param checkLock if <code>true</code>, a lock for the current user is required for 
     *      all write operations, if <code>false</code> it's ok to write as long as the resource
     *      is not locked by another user.
     * @param filter the resource filter to use
     * 
     * @return <code>true</code> if the required permissions are satisfied
     * 
     * @throws CmsException if something goes wrong
     */
    public boolean hasPermissions(
        CmsResource resource,
        CmsPermissionSet requiredPermissions,
        boolean checkLock,
        CmsResourceFilter filter) throws CmsException {

        return I_CmsPermissionHandler.PERM_ALLOWED == m_securityManager.hasPermissions(
            m_context,
            resource,
            requiredPermissions,
            checkLock,
            filter);
    }

    /**
     * Writes a list of access control entries as new access control entries of a given resource.<p>
     * 
     * Already existing access control entries of this resource are removed before.<p>
     * 
     * @param resource the resource to attach the control entries to
     * @param acEntries a list of <code>{@link CmsAccessControlEntry}</code> objects
     * 
     * @throws CmsException if something goes wrong
     */
    public void importAccessControlEntries(CmsResource resource, List<CmsAccessControlEntry> acEntries)
    throws CmsException {

        m_securityManager.importAccessControlEntries(m_context, resource, acEntries);
    }

    /**
     * Imports a new relation to the given resource.<p>
     * 
     * @param resourceName the name of the source resource
     * @param targetPath the path of the target resource
     * @param relationType the type of the relation
     * 
     * @throws CmsException if something goes wrong
     */
    public void importRelation(String resourceName, String targetPath, String relationType) throws CmsException {

        createRelation(resourceName, targetPath, relationType, true);
    }

    /**
     * Imports a resource to the OpenCms VFS.<p>
     * 
     * If a resource already exists in the VFS (i.e. has the same name and 
     * same id) it is replaced by the imported resource.<p>
     * 
     * If a resource with the same name but a different id exists, 
     * the imported resource is (usually) moved to the "lost and found" folder.<p> 
     *
     * @param resourcename the name for the resource after import (full current site relative path)
     * @param resource the resource object to be imported
     * @param content the content of the resource
     * @param properties the properties of the resource
     * 
     * @return the imported resource
     * 
     * @throws CmsException if something goes wrong
     * 
     * @see CmsObject#moveToLostAndFound(String)
     */
    public CmsResource importResource(
        String resourcename,
        CmsResource resource,
        byte[] content,
        List<CmsProperty> properties) throws CmsException {

        return getResourceType(resource).importResource(
            this,
            m_securityManager,
            resourcename,
            resource,
            content,
            properties);
    }

    /**
     * Creates a new user by import.<p>
     * 
     * @param id the id of the user
     * @param name the new name for the user
     * @param password the new password for the user
     * @param firstname the first name of the user
     * @param lastname the last name of the user
     * @param email the email of the user
     * @param flags the flags for a user (for example <code>{@link I_CmsPrincipal#FLAG_ENABLED}</code>)
     * @param dateCreated the creation date
     * @param additionalInfos the additional user infos
     * 
     * @return the imported user
     *
     * @throws CmsException if something goes wrong
     */
    public CmsUser importUser(
        String id,
        String name,
        String password,
        String firstname,
        String lastname,
        String email,
        int flags,
        long dateCreated,
        Map<String, Object> additionalInfos) throws CmsException {

        return m_securityManager.importUser(
            m_context,
            id,
            name,
            password,
            firstname,
            lastname,
            email,
            flags,
            dateCreated,
            additionalInfos);
    }

    /**
     * Increments a counter and returns its old value.<p>
     * 
     * @param name the name of the counter 
     *  
     * @return the value of the counter before incrementing 
     *    
     * @throws CmsException if something goes wrong 
     */
    public int incrementCounter(String name) throws CmsException {

        return m_securityManager.incrementCounter(m_context, name);
    }

    /**
     * Checks if the specified resource is inside the current project.<p>
     * 
     * The project "view" is determined by a set of path prefixes. 
     * If the resource starts with any one of this prefixes, it is considered to 
     * be "inside" the project.<p>
     * 
     * @param resourcename the specified resource name (full current site relative path)
     * 
     * @return <code>true</code>, if the specified resource is inside the current project
     */
    public boolean isInsideCurrentProject(String resourcename) {

        return m_securityManager.isInsideCurrentProject(m_context, addSiteRoot(resourcename));
    }

    /**
     * Checks if the current user has management access to the current project.<p>
     *
     * @return <code>true</code>, if the user has management access to the current project
     */

    public boolean isManagerOfProject() {

        return m_securityManager.isManagerOfProject(m_context);
    }

    /**
     * Locks a resource.<p>
     *
     * This will be an exclusive, persistent lock that is removed only if the user unlocks it.<p>
     *
     * @param resource the resource to lock
     * 
     * @throws CmsException if something goes wrong
     */
    public void lockResource(CmsResource resource) throws CmsException {

        getResourceType(resource).lockResource(this, m_securityManager, resource, CmsLockType.EXCLUSIVE);
    }

    /**
     * Locks a resource.<p>
     *
     * This will be an exclusive, persistent lock that is removed only if the user unlocks it.<p>
     *
     * @param resourcename the name of the resource to lock (full current site relative path)
     * 
     * @throws CmsException if something goes wrong
     */
    public void lockResource(String resourcename) throws CmsException {

        lockResource(resourcename, CmsLockType.EXCLUSIVE);
    }

    /**
     * Locks a resource temporary.<p>
     *
     * This will be an exclusive, temporary lock valid only for the current users session.
     * Usually this should not be used directly, this method is intended for the OpenCms workplace only.<p>
     *
     * @param resource the resource to lock
     * 
     * @throws CmsException if something goes wrong
     * 
     * @see CmsObject#lockResource(String)
     */
    public void lockResourceTemporary(CmsResource resource) throws CmsException {

        getResourceType(resource).lockResource(this, m_securityManager, resource, CmsLockType.TEMPORARY);
    }

    /**
     * Locks a resource temporary.<p>
     *
     * This will be an exclusive, temporary lock valid only for the current users session.
     * Usually this should not be used directly, this method is intended for the OpenCms workplace only.<p>
     *
     * @param resourcename the name of the resource to lock (full current site relative path)
     * 
     * @throws CmsException if something goes wrong
     * 
     * @see CmsObject#lockResource(String)
     */
    public void lockResourceTemporary(String resourcename) throws CmsException {

        lockResource(resourcename, CmsLockType.TEMPORARY);
    }

    /**
     * Logs a user into the Cms, if the password is correct.<p>
     *
     * @param username the name of the user
     * @param password the password of the user
     * 
     * @return the name of the logged in user
     *
     * @throws CmsException if the login was not successful
     */
    public String loginUser(String username, String password) throws CmsException {

        return loginUser(username, password, m_context.getRemoteAddress());
    }

    /**
     * Logs a user with a given ip address into the Cms, if the password is correct.<p>
     *
     * @param username the name of the user
     * @param password the password of the user
     * @param remoteAddress the ip address
     * 
     * @return the name of the logged in user
     *
     * @throws CmsException if the login was not successful
     */
    public String loginUser(String username, String password, String remoteAddress) throws CmsException {

        // login the user
        CmsUser newUser = m_securityManager.loginUser(m_context, username, password, remoteAddress);
        // set the project back to the "Online" project
        CmsProject newProject = m_securityManager.readProject(CmsProject.ONLINE_PROJECT_ID);
        // switch the cms context to the new user and project
        m_context.switchUser(newUser, newProject, newUser.getOuFqn());
        // init this CmsObject with the new user
        init(m_securityManager, m_context);
        // fire a login event
        fireEvent(I_CmsEventListener.EVENT_LOGIN_USER, newUser);
        // return the users login name
        return newUser.getName();
    }

    /**
     * Lookups and reads the user or group with the given UUID.<p>
     *   
     * @param principalId the uuid of a user or group
     * 
     * @return the user or group with the given UUID
     */
    public I_CmsPrincipal lookupPrincipal(CmsUUID principalId) {

        return m_securityManager.lookupPrincipal(m_context, principalId);
    }

    /**
     * Lookups and reads the user or group with the given name.<p>
     * 
     * @param principalName the name of the user or group
     * 
     * @return the user or group with the given name
     */
    public I_CmsPrincipal lookupPrincipal(String principalName) {

        return m_securityManager.lookupPrincipal(m_context, principalName);
    }

    /**
     * Moves a resource to the given destination.<p>
     * 
     * A move operation in OpenCms is always a copy (as sibling) followed by a delete,
     * this is a result of the online/offline structure of the 
     * OpenCms VFS. This way you can see the deleted files/folders in the offline
     * project, and you will be unable to undelete them.<p>
     * 
     * @param source the name of the resource to move (full current site relative path)
     * @param destination the destination resource name (full current site relative path)
     *
     * @throws CmsException if something goes wrong
     * 
     * @see #renameResource(String, String)
     */
    public void moveResource(String source, String destination) throws CmsException {

        CmsResource resource = readResource(source, CmsResourceFilter.IGNORE_EXPIRATION);
        getResourceType(resource).moveResource(this, m_securityManager, resource, destination);
    }

    /**
     * Moves a resource to the "lost and found" folder.<p>
     * 
     * The "lost and found" folder is a special system folder. 
     * 
     * This operation is used e.g. during import of resources
     * when a resource with the same name but a different resource ID
     * already exists in the VFS. In this case, the imported resource is 
     * moved to the "lost and found" folder.<p>
     * 
     * @param resourcename the name of the resource to move to "lost and found" (full current site relative path)
     *
     * @return the name of the resource inside the "lost and found" folder
     * 
     * @throws CmsException if something goes wrong
     * 
     * @see #getLostAndFoundName(String)
     */
    public String moveToLostAndFound(String resourcename) throws CmsException {

        CmsResource resource = readResource(resourcename, CmsResourceFilter.ALL);
        return m_securityManager.moveToLostAndFound(m_context, resource, false);
    }

    /**
     * Reads all historical versions of a resource.<br>
     * 
     * The reading excludes the file content, if the resource is a file.<p>
     *
     * @param resourceName the name of the resource to be read
     *
     * @return a list of historical resources, as <code>{@link I_CmsHistoryResource}</code> objects
     *
     * @throws CmsException if operation was not successful
     */
    public List<I_CmsHistoryResource> readAllAvailableVersions(String resourceName) throws CmsException {

        CmsResource resource = readResource(resourceName, CmsResourceFilter.ALL);
        return m_securityManager.readAllAvailableVersions(m_context, resource);
    }

    /**
     * Reads all property definitions.<p>
     *
     * @return a list with the <code>{@link CmsPropertyDefinition}</code> objects (may be empty)
     *
     * @throws CmsException if something goes wrong
     */
    public List<CmsPropertyDefinition> readAllPropertyDefinitions() throws CmsException {

        return m_securityManager.readAllPropertyDefinitions(m_context);
    }

    /**
     * Returns the first ancestor folder matching the filter criteria.<p>
     * 
     * If no folder matching the filter criteria is found, null is returned.<p>
     * 
     * @param resourcename the name of the resource to start (full current site relative path)
     * @param filter the resource filter to match while reading the ancestors
     * 
     * @return the first ancestor folder matching the filter criteria or null if no folder was found
     * 
     * @throws CmsException if something goes wrong
     */
    public CmsFolder readAncestor(String resourcename, CmsResourceFilter filter) throws CmsException {

        CmsResource resource = readResource(resourcename, CmsResourceFilter.ALL);
        return m_securityManager.readAncestor(m_context, resource, filter);
    }

    /**
     * Returns the first ancestor folder matching the resource type.<p>
     * 
     * If no folder with the requested resource type is found, null is returned.<p>
     * 
     * @param resourcename the name of the resource to start (full current site relative path)
     * @param type the resource type of the folder to match
     * 
     * @return the first ancestor folder matching the filter criteria or null if no folder was found
     * 
     * @throws CmsException if something goes wrong
     */
    public CmsFolder readAncestor(String resourcename, int type) throws CmsException {

        return readAncestor(resourcename, CmsResourceFilter.requireType(type));
    }

    /**
     * Reads the newest URL name which is mapped to the given structure id.<p>
     * 
     * If the structure id is not mapped to any name, null will be returned.<p>
     * 
     * @param id the structure id for which the newest mapped name should be returned
     * @param locale the locale for which the URL name should be selected if possible 
     * @param defaultLocales the default locales which should be used if the locale is not available
     * @return an URL name or null 
     * @throws CmsException if something goes wrong 
     */
    public String readBestUrlName(CmsUUID id, Locale locale, List<Locale> defaultLocales) throws CmsException {

        return m_securityManager.readBestUrlName(m_context, id, locale, defaultLocales);
    }

    /**
     * Returns the default resource for the given folder.<p>
     * <ol>
     *   <li>the {@link CmsPropertyDefinition#PROPERTY_DEFAULT_FILE} is checked, and
     *   <li>if still no file could be found, the configured default files in the 
     *       <code>opencms-vfs.xml</code> configuration are iterated until a match is 
     *       found, and
     *   <li>if still no file could be found, <code>null</code> is returned
     * </ol>
     * 
     * @param folderResource the folder
     * @param resourceFilter the resource filter
     * 
     * @return the default file for the given folder
     * 
     * @throws CmsSecurityException  if the user has no permissions to read the resulting file
     */
    public CmsResource readDefaultFile(CmsResource folderResource, CmsResourceFilter resourceFilter)
    throws CmsSecurityException {

        return m_securityManager.readDefaultFile(m_context, folderResource, resourceFilter);
    }

    /**
     * Returns the default resource for the given folder.<p>
     * 
     * If the given resource name or id identifies a file, then this file is returned.<p>
     * 
     * Otherwise, in case of a folder:<br> 
     * <ol>
     *   <li>the {@link CmsPropertyDefinition#PROPERTY_DEFAULT_FILE} is checked, and
     *   <li>if still no file could be found, the configured default files in the 
     *       <code>opencms-vfs.xml</code> configuration are iterated until a match is 
     *       found, and
     *   <li>if still no file could be found, <code>null</code> is returned
     * </ol>
     * 
     * @param resourceNameOrID the name or id of the folder to read the default file for
     * 
     * @return the default file for the given folder
     * 
     * @throws CmsException if something goes wrong
     * @throws CmsSecurityException if the user has no permissions to read the resulting file
     */
    public CmsResource readDefaultFile(String resourceNameOrID) throws CmsException, CmsSecurityException {

        CmsResource resource;
        try {
            resource = readResource(new CmsUUID(resourceNameOrID));
        } catch (NumberFormatException e) {
            resource = readResource(resourceNameOrID);
        }
        return m_securityManager.readDefaultFile(m_context, resource, CmsResourceFilter.DEFAULT);
    }

    /**
     * Reads all deleted (historical) resources below the given path, 
     * including the full tree below the path, if required.<p>
     * 
     * The result list may include resources with the same name of  
     * resources (with different id's).<p>
     * 
     * Use in conjunction with the {@link #restoreDeletedResource(CmsUUID)} 
     * method.<p>
     * 
     * @param resourcename the parent path to read the resources from
     * @param readTree <code>true</code> to read all sub resources
     * 
     * @return a list of <code>{@link I_CmsHistoryResource}</code> objects
     * 
     * @throws CmsException if something goes wrong
     * 
     * @see #readResource(CmsUUID, int)
     * @see #readResources(String, CmsResourceFilter, boolean)
     */
    public List<I_CmsHistoryResource> readDeletedResources(String resourcename, boolean readTree) throws CmsException {

        CmsResource resource = readResource(resourcename, CmsResourceFilter.ALL);
        return m_securityManager.readDeletedResources(m_context, resource, readTree);
    }

    /**
     * Reads a file resource (including it's binary content) from the VFS,
     * for the given resource (this may also be an historical version of the resource).<p>
     * 
     * In case the input {@link CmsResource} object already is a {@link CmsFile} with contents
     * available, it is casted to a file and returned unchanged. Otherwise the file is read 
     * from the VFS.<p>
     * 
     * In case you do not need the file content, 
     * use <code>{@link #readResource(String)}</code> or
     * <code>{@link #readResource(String, CmsResourceFilter)}</code> instead.<p>
     * 
     * No resource filter is applied when reading the resource, since we already have
     * a full resource instance and assume we just want the content for that instance. 
     * In case you need to apply a filter, use {@link #readFile(String, CmsResourceFilter)} instead.<p>
     * 
     * @param resource the resource to read
     *
     * @return the file resource that was read
     *
     * @throws CmsException if the file resource could not be read for any reason
     * 
     * @see #readFile(String)
     * @see #readFile(String, CmsResourceFilter)
     */
    public CmsFile readFile(CmsResource resource) throws CmsException {

        // test if we already have a file
        if (resource instanceof CmsFile) {
            // resource is already a file
            CmsFile file = (CmsFile)resource;
            if ((file.getContents() != null) && (file.getContents().length > 0)) {
                // file has the contents already available
                return file;
            }
        }

        return m_securityManager.readFile(m_context, resource);
    }

    /**
     * Reads a file resource (including it's binary content) from the VFS,
     * using the <code>{@link CmsResourceFilter#DEFAULT}</code> filter.<p>
     *  
     * In case you do not need the file content, 
     * use <code>{@link #readResource(String)}</code> instead.<p>
     *
     * @param resourcename the name of the resource to read (full current site relative path)
     *
     * @return the file resource that was read
     *
     * @throws CmsException if the file resource could not be read for any reason
     * 
     * @see #readFile(String, CmsResourceFilter)
     * @see #readFile(CmsResource)
     * @see #readResource(String)
     */
    public CmsFile readFile(String resourcename) throws CmsException {

        return readFile(resourcename, CmsResourceFilter.DEFAULT);
    }

    /**
     * Reads a file resource (including it's binary content) from the VFS,
     * using the specified resource filter.<p>
     * 
     * In case you do not need the file content, 
     * use <code>{@link #readResource(String, CmsResourceFilter)}</code> instead.<p>
     * 
     * The specified filter controls what kind of resources should be "found" 
     * during the read operation. This will depend on the application. For example, 
     * using <code>{@link CmsResourceFilter#DEFAULT}</code> will only return currently
     * "valid" resources, while using <code>{@link CmsResourceFilter#IGNORE_EXPIRATION}</code>
     * will ignore the date release / date expired information of the resource.<p>
     *
     * @param resourcename the name of the resource to read (full current site relative path)
     * @param filter the resource filter to use while reading
     *
     * @return the file resource that was read
     *
     * @throws CmsException if the file resource could not be read for any reason
     *
     * @see #readFile(String)
     * @see #readFile(CmsResource)
     * @see #readResource(String, CmsResourceFilter)
     */
    public CmsFile readFile(String resourcename, CmsResourceFilter filter) throws CmsException {

        CmsResource resource = readResource(resourcename, filter);
        return readFile(resource);
    }

    /**
     * Reads a folder resource from the VFS,
     * using the <code>{@link CmsResourceFilter#DEFAULT}</code> filter.<p> 
     *
     * @param resourcename the name of the folder resource to read (full current site relative path)
     *
     * @return the folder resource that was read
     *
     * @throws CmsException if the resource could not be read for any reason
     *
     * @see #readResource(String, CmsResourceFilter)
     * @see #readFolder(String, CmsResourceFilter)
     */
    public CmsFolder readFolder(String resourcename) throws CmsException {

        return readFolder(resourcename, CmsResourceFilter.DEFAULT);
    }

    /**
     * Reads a folder resource from the VFS,
     * using the specified resource filter.<p>
     *
     * The specified filter controls what kind of resources should be "found" 
     * during the read operation. This will depend on the application. For example, 
     * using <code>{@link CmsResourceFilter#DEFAULT}</code> will only return currently
     * "valid" resources, while using <code>{@link CmsResourceFilter#IGNORE_EXPIRATION}</code>
     * will ignore the date release / date expired information of the resource.<p>
     * 
     * @param resourcename the name of the folder resource to read (full current site relative path)
     * @param filter the resource filter to use while reading
     *
     * @return the folder resource that was read
     *
     * @throws CmsException if the resource could not be read for any reason
     * 
     * @see #readResource(String, CmsResourceFilter)
     */
    public CmsFolder readFolder(String resourcename, CmsResourceFilter filter) throws CmsException {

        return m_securityManager.readFolder(m_context, addSiteRoot(resourcename), filter);
    }

    /**
     * Reads the group of a project.<p>
     *
     * @param project the project to read the group from
     * 
     * @return the group of the given project
     */
    public CmsGroup readGroup(CmsProject project) {

        return m_securityManager.readGroup(m_context, project);
    }

    /**
     * Reads a group based on its id.<p>
     *
     * @param groupId the id of the group to be read
     * 
     * @return the group that has the provided id
     * 
     * @throws CmsException if something goes wrong
     * 
     * @see #readHistoryPrincipal(CmsUUID) for retrieving deleted groups
     */
    public CmsGroup readGroup(CmsUUID groupId) throws CmsException {

        return m_securityManager.readGroup(m_context, groupId);
    }

    /**
     * Reads a group based on its name.<p>
     * 
     * @param groupName the name of the group to be read
     * 
     * @return the group that has the provided name
     * 
     * @throws CmsException if something goes wrong
     */
    public CmsGroup readGroup(String groupName) throws CmsException {

        return m_securityManager.readGroup(m_context, groupName);
    }

    /**
     * Reads a principal (an user or group) from the historical archive based on its ID.<p>
     * 
     * @param principalId the id of the principal to read
     * 
     * @return the historical principal entry with the given id
     * 
     * @throws CmsException if something goes wrong, ie. {@link org.opencms.db.CmsDbEntryNotFoundException}
     * 
     * @see #readUser(CmsUUID)
     * @see #readGroup(CmsUUID)
     */
    public CmsHistoryPrincipal readHistoryPrincipal(CmsUUID principalId) throws CmsException {

        return m_securityManager.readHistoricalPrincipal(m_context, principalId);
    }

    /**
     * Returns the latest historical project entry with the given id.<p>
     *
     * @param projectId the project id
     * 
     * @return the requested historical project entry
     * 
     * @throws CmsException if operation was not successful
     */
    public CmsHistoryProject readHistoryProject(CmsUUID projectId) throws CmsException {

        return (m_securityManager.readHistoryProject(m_context, projectId));
    }

    /**
     * Returns a historical project entry.<p>
     *
     * @param publishTag publish tag of the project
     * 
     * @return the requested historical project entry
     * 
     * @throws CmsException if operation was not successful
     */
    public CmsHistoryProject readHistoryProject(int publishTag) throws CmsException {

        return (m_securityManager.readHistoryProject(m_context, publishTag));
    }

    /**
     * Reads the list of all <code>{@link CmsProperty}</code> objects that belong to the given historical resource version.<p>
     * 
     * @param resource the historical resource version to read the properties for
     * 
     * @return the list of <code>{@link CmsProperty}</code> objects
     * 
     * @throws CmsException if something goes wrong
     */
    public List<CmsProperty> readHistoryPropertyObjects(I_CmsHistoryResource resource) throws CmsException {

        return m_securityManager.readHistoryPropertyObjects(m_context, resource);
    }

    /**
     * This method retrieves the structure id which is mapped to a given URL name.<p>
     * 
     * If there is no structure id mapped to the given name, null will be returned.<p>
     * 
     * However if the parameter is a string which represents a valid uuid, it will be directly returned as a {@link CmsUUID} instance.<p> 
     * 
     * @param name the url name  
     * @return the id which is mapped to the URL name 
     * 
     * @throws CmsException if something goes wrong 
     */
    public CmsUUID readIdForUrlName(String name) throws CmsException {

        if (CmsUUID.isValidUUID(name)) {
            return new CmsUUID(name);
        }
        return m_securityManager.readIdForUrlName(m_context, name);
    }

    /**
     * Returns the project manager group of a project.<p>
     *
     * @param project the project
     * 
     * @return the manager group of the project
     */
    public CmsGroup readManagerGroup(CmsProject project) {

        return m_securityManager.readManagerGroup(m_context, project);
    }

    /**
     * Reads the owner of a project.<p>
     *
     * @param project the project to read the owner from
     * 
     * @return the owner of the project
     * 
     * @throws CmsException if something goes wrong
     */
    public CmsUser readOwner(CmsProject project) throws CmsException {

        return m_securityManager.readOwner(m_context, project);
    }

    /**
     * Returns the parent folder to the given structure id.<p>
     * 
     * @param structureId the child structure id
     * 
     * @return the parent folder <code>{@link CmsResource}</code>
     * 
     * @throws CmsException if something goes wrong
     */
    public CmsResource readParentFolder(CmsUUID structureId) throws CmsException {

        return m_securityManager.readParentFolder(m_context, structureId);
    }

    /**
     * Builds a list of resources for a given path.<p>
     * 
     * @param path the requested path
     * @param filter a filter object (only "includeDeleted" information is used!)
     * 
     * @return list of <code>{@link CmsResource}</code>s
     * 
     * @throws CmsException if something goes wrong
     */
    public List<CmsResource> readPath(String path, CmsResourceFilter filter) throws CmsException {

        return m_securityManager.readPath(m_context, addSiteRoot(path), filter);
    }

    /**
     * Reads the project with the given id.<p>
     *
     * @param id the id of the project
     * 
     * @return the project with the given id
     *
     * @throws CmsException if operation was not successful
     */
    public CmsProject readProject(CmsUUID id) throws CmsException {

        return m_securityManager.readProject(id);
    }

    /**
     * Reads the project with the given name.<p>
     *
     * @param name the name of the project
     * 
     * @return the project with the given name
     * 
     * @throws CmsException if operation was not successful
     */
    public CmsProject readProject(String name) throws CmsException {

        return m_securityManager.readProject(name);
    }

    /**
     * Returns the list of all resource names that define the "view" of the given project.<p>
     * 
     * @param project the project to get the project resources for
     * 
     * @return the list of all resource names (root paths), as <code>{@link String}</code> 
     *              objects that define the "view" of the given project
     * 
     * @throws CmsException if something goes wrong
     */
    public List<String> readProjectResources(CmsProject project) throws CmsException {

        return m_securityManager.readProjectResources(m_context, project);
    }

    /**
     * Reads all resources of a project that match a given state from the VFS.<p>
     * 
     * Possible values for the <code>state</code> parameter are:<br>
     * <ul>
     * <li><code>{@link CmsResource#STATE_CHANGED}</code>: Read all "changed" resources in the project</li>
     * <li><code>{@link CmsResource#STATE_NEW}</code>: Read all "new" resources in the project</li>
     * <li><code>{@link CmsResource#STATE_DELETED}</code>: Read all "deleted" resources in the project</li>
     * <li><code>{@link CmsResource#STATE_KEEP}</code>: Read all resources either "changed", "new" or "deleted" in the project</li>
     * </ul><p>
     * 
     * @param projectId the id of the project to read the file resources for
     * @param state the resource state to match
     *
     * @return all <code>{@link CmsResource}</code>s of a project that match a given criteria from the VFS
     * 
     * @throws CmsException if something goes wrong
     */
    public List<CmsResource> readProjectView(CmsUUID projectId, CmsResourceState state) throws CmsException {

        return m_securityManager.readProjectView(m_context, projectId, state);
    }

    /**
     * Reads a property definition.<p>
     *
     * If no property definition with the given name is found, 
     * <code>null</code> is returned.<p>
     * 
     * @param name the name of the property definition to read
     * 
     * @return the property definition that was read
     *
     * @throws CmsException a CmsDbEntryNotFoundException is thrown if the property definition does not exist
     */
    public CmsPropertyDefinition readPropertyDefinition(String name) throws CmsException {

        return (m_securityManager.readPropertyDefinition(m_context, name));
    }

    /**
     * Reads a property object from a resource specified by a property name.<p>
     * 
     * Returns <code>{@link CmsProperty#getNullProperty()}</code> if the property is not found.<p>
     * 
     * This method is more efficient then using <code>{@link CmsObject#readPropertyObject(String, String, boolean)}</code>
     * if you already have an instance of the resource to look up the property from.<p>
     * 
     * @param resource the resource where the property is attached to
     * @param property the property name
     * @param search if true, the property is searched on all parent folders of the resource, 
     *      if it's not found attached directly to the resource
     * 
     * @return the required property, or <code>{@link CmsProperty#getNullProperty()}</code> if the property was not found
     * 
     * @throws CmsException if something goes wrong
     */
    public CmsProperty readPropertyObject(CmsResource resource, String property, boolean search) throws CmsException {

        return m_securityManager.readPropertyObject(m_context, resource, property, search);
    }

    /**
     * Reads a property object from a resource specified by a property name.<p>
     * 
     * Returns <code>{@link CmsProperty#getNullProperty()}</code> if the property is not found.<p>
     * 
     * @param resourcePath the name of resource where the property is attached to
     * @param property the property name
     * @param search if true, the property is searched on all parent folders of the resource, 
     *      if it's not found attached directly to the resource
     * 
     * @return the required property, or <code>{@link CmsProperty#getNullProperty()}</code> if the property was not found
     * 
     * @throws CmsException if something goes wrong
     */
    public CmsProperty readPropertyObject(String resourcePath, String property, boolean search) throws CmsException {

        CmsResource resource = readResource(resourcePath, CmsResourceFilter.ALL);
        return m_securityManager.readPropertyObject(m_context, resource, property, search);
    }

    /**
     * Reads all property objects from a resource.<p>
     * 
     * Returns an empty list if no properties are found.<p>
     * 
     * This method is more efficient then using <code>{@link CmsObject#readPropertyObjects(String, boolean)}</code>
     * if you already have an instance of the resource to look up the property from.<p>
     * 
     * If the <code>search</code> parameter is <code>true</code>, the properties of all 
     * parent folders of the resource are also read. The results are merged with the 
     * properties directly attached to the resource. While merging, a property
     * on a parent folder that has already been found will be ignored.
     * So e.g. if a resource has a property "Title" attached, and it's parent folder 
     * has the same property attached but with a different value, the result list will
     * contain only the property with the value from the resource, not form the parent folder(s).<p>
     * 
     * @param resource the resource where the property is mapped to
     * @param search if <code>true</code>, the properties of all parent folders of the resource 
     *      are merged with the resource properties.
     * 
     * @return a list of <code>{@link CmsProperty}</code> objects
     * 
     * @throws CmsException if something goes wrong
     */
    public List<CmsProperty> readPropertyObjects(CmsResource resource, boolean search) throws CmsException {

        return m_securityManager.readPropertyObjects(m_context, resource, search);
    }

    /**
     * Reads all property objects from a resource.<p>
     * 
     * Returns an empty list if no properties are found.<p>
     * 
     * All properties in the result List will be in frozen (read only) state, so you can't change the values.<p>
     * 
     * If the <code>search</code> parameter is <code>true</code>, the properties of all 
     * parent folders of the resource are also read. The results are merged with the 
     * properties directly attached to the resource. While merging, a property
     * on a parent folder that has already been found will be ignored.
     * So e.g. if a resource has a property "Title" attached, and it's parent folder 
     * has the same property attached but with a different value, the result list will
     * contain only the property with the value from the resource, not form the parent folder(s).<p>
     * 
     * @param resourcePath the name of resource where the property is mapped to
     * @param search if <code>true</code>, the properties of all parent folders of the resource 
     *      are merged with the resource properties.
     * 
     * @return a list of <code>{@link CmsProperty}</code> objects
     * 
     * @throws CmsException if something goes wrong
     */
    public List<CmsProperty> readPropertyObjects(String resourcePath, boolean search) throws CmsException {

        CmsResource resource = readResource(resourcePath, CmsResourceFilter.ALL);
        return m_securityManager.readPropertyObjects(m_context, resource, search);
    }

    /**
     * Reads the resources that were published in a publish task for a given publish history ID.<p>
     * 
     * @param publishHistoryId unique ID to identify each publish task in the publish history
     * 
     * @return a list of <code>{@link org.opencms.db.CmsPublishedResource}</code> objects
     * 
     * @throws CmsException if something goes wrong
     */
    public List<CmsPublishedResource> readPublishedResources(CmsUUID publishHistoryId) throws CmsException {

        return m_securityManager.readPublishedResources(m_context, publishHistoryId);
    }

    /**
     * Returns all relations matching the given filter.<p> 
     * 
     * @param filter the filter to match the relation 
     * 
     * @return all relations matching the given filter
     * 
     * @throws CmsException if something goes wrong
     * 
     * @see CmsSecurityManager#getRelationsForResource(CmsRequestContext, CmsResource, CmsRelationFilter)
     * @see #getRelationsForResource(CmsResource, CmsRelationFilter)
     */
    public List<CmsRelation> readRelations(CmsRelationFilter filter) throws CmsException {

        return m_securityManager.getRelationsForResource(m_context, null, filter);
    }

    /**
     * Reads a resource from the VFS,
     * using the <code>{@link CmsResourceFilter#DEFAULT}</code> filter.<p> 
     *
     * A resource may be of type <code>{@link CmsFile}</code> or 
     * <code>{@link CmsFolder}</code>. In case of
     * a file, the resource will not contain the binary file content. Since reading 
     * the binary content is a cost-expensive database operation, it's recommended 
     * to work with resources if possible, and only read the file content when absolutely
     * required. To "upgrade" a resource to a file, 
     * use <code>{@link #readFile(CmsResource)}</code>.<p> 
     *
     * @param structureID the structure ID of the resource to read
     *
     * @return the resource that was read
     *
     * @throws CmsException if the resource could not be read for any reason
     *
     * @see #readFile(String) 
     * @see #readResource(CmsUUID, CmsResourceFilter)
     */
    public CmsResource readResource(CmsUUID structureID) throws CmsException {

        return readResource(structureID, CmsResourceFilter.DEFAULT);
    }

    /**
     * Reads a resource from the VFS,
     * using the specified resource filter.<p>
     *
     * A resource may be of type <code>{@link CmsFile}</code> or 
     * <code>{@link CmsFolder}</code>. In case of
     * a file, the resource will not contain the binary file content. Since reading 
     * the binary content is a cost-expensive database operation, it's recommended 
     * to work with resources if possible, and only read the file content when absolutely
     * required. To "upgrade" a resource to a file, 
     * use <code>{@link #readFile(CmsResource)}</code>.<p> 
     *
     * The specified filter controls what kind of resources should be "found" 
     * during the read operation. This will depend on the application. For example, 
     * using <code>{@link CmsResourceFilter#DEFAULT}</code> will only return currently
     * "valid" resources, while using <code>{@link CmsResourceFilter#IGNORE_EXPIRATION}</code>
     * will ignore the date release / date expired information of the resource.<p>
     * 
     * @param structureID the structure ID of the resource to read
     * @param filter the resource filter to use while reading
     *
     * @return the resource that was read
     *
     * @throws CmsException if the resource could not be read for any reason
     * 
     * @see #readFile(String, CmsResourceFilter)
     * @see #readFolder(String, CmsResourceFilter)
     */
    public CmsResource readResource(CmsUUID structureID, CmsResourceFilter filter) throws CmsException {

        return m_securityManager.readResource(m_context, structureID, filter);
    }

    /**
     * Reads the historical resource with the given version for the resource given 
     * the given structure id.<p>
     *
     * A resource may be of type <code>{@link CmsFile}</code> or 
     * <code>{@link CmsFolder}</code>. In case of a file, the resource will not 
     * contain the binary file content. Since reading the binary content is a 
     * cost-expensive database operation, it's recommended to work with resources 
     * if possible, and only read the file content when absolutely required. To 
     * "upgrade" a resource to a file, use 
     * <code>{@link #readFile(CmsResource)}</code>.<p> 
     *
     * Please note that historical versions are just generated during publishing, 
     * so the first version with version number 1 is generated during publishing 
     * of a new resource (exception is a new sibling, that may also contain some 
     * relevant versions of already published siblings) and the last version 
     * available is the version of the current online resource.<p>
     * 
     * @param structureID the structure ID of the resource to read
     * @param version the version number you want to retrieve
     *
     * @return the resource that was read
     *
     * @throws CmsException if the resource could not be read for any reason
     * @throws CmsVfsResourceNotFoundException if the version does not exists
     * 
     * @see #restoreResourceVersion(CmsUUID, int)
     */
    public I_CmsHistoryResource readResource(CmsUUID structureID, int version)
    throws CmsException, CmsVfsResourceNotFoundException {

        CmsResource resource = readResource(structureID, CmsResourceFilter.ALL);
        return m_securityManager.readResource(m_context, resource, version);
    }

    /**
     * Reads a resource from the VFS,
     * using the <code>{@link CmsResourceFilter#DEFAULT}</code> filter.<p> 
     *
     * A resource may be of type <code>{@link CmsFile}</code> or 
     * <code>{@link CmsFolder}</code>. In case of
     * a file, the resource will not contain the binary file content. Since reading 
     * the binary content is a cost-expensive database operation, it's recommended 
     * to work with resources if possible, and only read the file content when absolutely
     * required. To "upgrade" a resource to a file, 
     * use <code>{@link #readFile(CmsResource)}</code>.<p> 
     *
     * @param resourcename the name of the resource to read (full current site relative path)
     *
     * @return the resource that was read
     *
     * @throws CmsException if the resource could not be read for any reason
     *
     * @see #readFile(String) 
     * @see #readResource(String, CmsResourceFilter)
     */
    public CmsResource readResource(String resourcename) throws CmsException {

        return readResource(resourcename, CmsResourceFilter.DEFAULT);
    }

    /**
     * Reads a resource from the VFS,
     * using the specified resource filter.<p>
     *
     * A resource may be of type <code>{@link CmsFile}</code> or 
     * <code>{@link CmsFolder}</code>. In case of
     * a file, the resource will not contain the binary file content. Since reading 
     * the binary content is a cost-expensive database operation, it's recommended 
     * to work with resources if possible, and only read the file content when absolutely
     * required. To "upgrade" a resource to a file, 
     * use <code>{@link #readFile(CmsResource)}</code>.<p> 
     *
     * The specified filter controls what kind of resources should be "found" 
     * during the read operation. This will depend on the application. For example, 
     * using <code>{@link CmsResourceFilter#DEFAULT}</code> will only return currently
     * "valid" resources, while using <code>{@link CmsResourceFilter#IGNORE_EXPIRATION}</code>
     * will ignore the date release / date expired information of the resource.<p>
     * 
     * @param resourcename the name of the resource to read (full current site relative path)
     * @param filter the resource filter to use while reading
     *
     * @return the resource that was read
     *
     * @throws CmsException if the resource could not be read for any reason
     * 
     * @see #readFile(String, CmsResourceFilter)
     * @see #readFolder(String, CmsResourceFilter)
     */
    public CmsResource readResource(String resourcename, CmsResourceFilter filter) throws CmsException {

        return m_securityManager.readResource(m_context, addSiteRoot(resourcename), filter);
    }

    /**
     * Reads all resources below the given path matching the filter criteria, 
     * including the full tree below the path.<p>
     * 
     * @param resourcename the parent path to read the resources from
     * @param filter the filter
     * 
     * @return a list of <code>{@link CmsResource}</code> objects matching the filter criteria
     * 
     * @throws CmsException if something goes wrong
     * 
     * @see #readResources(String, CmsResourceFilter, boolean)
     */
    public List<CmsResource> readResources(String resourcename, CmsResourceFilter filter) throws CmsException {

        return readResources(resourcename, filter, true);
    }

    /**
     * Reads all resources below the given path matching the filter criteria,
     * including the full tree below the path only in case the <code>readTree</code> 
     * parameter is <code>true</code>.<p>
     * 
     * @param resourcename the parent path to read the resources from
     * @param filter the filter
     * @param readTree <code>true</code> to read all sub resources
     * 
     * @return a list of <code>{@link CmsResource}</code> objects matching the filter criteria
     * 
     * @throws CmsException if something goes wrong
     */
    public List<CmsResource> readResources(String resourcename, CmsResourceFilter filter, boolean readTree)
    throws CmsException {

        CmsResource resource = readResource(resourcename, CmsResourceFilter.ALL);
        return m_securityManager.readResources(m_context, resource, filter, readTree);
    }

    /**
     * Reads all resources that have a value set for the specified property.<p>
     * 
     * Both individual and shared properties of a resource are checked.<p>
     * 
     * Will use the {@link CmsResourceFilter#ALL} resource filter.<p>
     *
     * @param propertyDefinition the name of the property to check for
     * 
     * @return a list of all <code>{@link CmsResource}</code> objects 
     *          that have a value set for the specified property.
     * 
     * @throws CmsException if something goes wrong
     */
    public List<CmsResource> readResourcesWithProperty(String propertyDefinition) throws CmsException {

        return readResourcesWithProperty("/", propertyDefinition);
    }

    /**
     * Reads all resources that have a value set for the specified property in the given path.<p>
     * 
     * Both individual and shared properties of a resource are checked.<p>
     * 
     * Will use the {@link CmsResourceFilter#ALL} resource filter.<p>
     *
     * @param path the folder to get the resources with the property from
     * @param propertyDefinition the name of the property to check for
     * 
     * @return all <code>{@link CmsResource}</code> objects 
     *          that have a value set for the specified property in the given path.
     * 
     * @throws CmsException if something goes wrong
     */
    public List<CmsResource> readResourcesWithProperty(String path, String propertyDefinition) throws CmsException {

        return readResourcesWithProperty(path, propertyDefinition, null);
    }

    /**
     * Reads all resources that have a value (containing the specified value) set 
     * for the specified property in the given path.<p>
     * 
     * Both individual and shared properties of a resource are checked.<p>
     *
     * If the <code>value</code> parameter is <code>null</code>, all resources having the
     * given property set are returned.<p>
     * 
     * Will use the {@link CmsResourceFilter#ALL} resource filter.<p>
     * 
     * @param path the folder to get the resources with the property from
     * @param propertyDefinition the name of the property to check for
     * @param value the string to search in the value of the property
     * 
     * @return all <code>{@link CmsResource}</code> objects 
     *          that have a value set for the specified property in the given path.
     * 
     * @throws CmsException if something goes wrong
     */
    public List<CmsResource> readResourcesWithProperty(String path, String propertyDefinition, String value)
    throws CmsException {

        CmsResource resource = readResource(path, CmsResourceFilter.IGNORE_EXPIRATION);
        return m_securityManager.readResourcesWithProperty(
            m_context,
            resource,
            propertyDefinition,
            value,
            CmsResourceFilter.ALL);
    }

    /**
     * Reads all resources that have a value (containing the specified value) set 
     * for the specified property in the given path.<p>
     * 
     * Both individual and shared properties of a resource are checked.<p>
     *
     * If the <code>value</code> parameter is <code>null</code>, all resources having the
     * given property set are returned.<p>
     * 
     * Will use the given resource filter.<p>
     * 
     * @param path the folder to get the resources with the property from
     * @param propertyDefinition the name of the property to check for
     * @param value the string to search in the value of the property
     * @param filter the resource filter to apply to the result set
     * 
     * @return all <code>{@link CmsResource}</code> objects 
     *          that have a value set for the specified property in the given path.
     * 
     * @throws CmsException if something goes wrong
     */
    public List<CmsResource> readResourcesWithProperty(
        String path,
        String propertyDefinition,
        String value,
        CmsResourceFilter filter) throws CmsException {

        CmsResource resource = readResource(path, CmsResourceFilter.IGNORE_EXPIRATION);
        return m_securityManager.readResourcesWithProperty(m_context, resource, propertyDefinition, value, filter);
    }

    /**
     * Returns a set of principals that are responsible for a specific resource.<p>
     * 
     * @param resource the resource to get the responsible principals from
     * 
     * @return the set of principals that are responsible for a specific resource
     * 
     * @throws CmsException if something goes wrong
     */
    public Set<I_CmsPrincipal> readResponsiblePrincipals(CmsResource resource) throws CmsException {

        return m_securityManager.readResponsiblePrincipals(m_context, resource);
    }

    /**
     * Returns a set of users that are responsible for a specific resource.<p>
     * 
     * @param resource the resource to get the responsible users from
     * 
     * @return the set of users that are responsible for a specific resource
     * 
     * @throws CmsException if something goes wrong
     */
    public Set<CmsUser> readResponsibleUsers(CmsResource resource) throws CmsException {

        return m_securityManager.readResponsibleUsers(m_context, resource);
    }

    /**
     * Returns a list of all siblings of the specified resource,
     * the specified resource being always part of the result set.<p>
     * 
     * @param resource the resource
     * @param filter a resource filter
     * 
     * @return a list of <code>{@link CmsResource}</code>s that 
     *          are siblings to the specified resource, 
     *          including the specified resource itself.
     * 
     * @throws CmsException if something goes wrong
     */
    public List<CmsResource> readSiblings(CmsResource resource, CmsResourceFilter filter) throws CmsException {

        return m_securityManager.readSiblings(m_context, resource, filter);
    }

    /**
     * Returns a list of all siblings of the specified resource,
     * the specified resource being always part of the result set.<p>
     * 
     * @param resourcename the name of the specified resource
     * @param filter a resource filter
     * 
     * @return a list of <code>{@link CmsResource}</code>s that 
     *          are siblings to the specified resource, 
     *          including the specified resource itself.
     * 
     * @throws CmsException if something goes wrong
     */
    public List<CmsResource> readSiblings(String resourcename, CmsResourceFilter filter) throws CmsException {

        CmsResource resource = readResource(resourcename, filter);
        return readSiblings(resource, filter);
    }

    /**
     * Returns the parameters of a resource in the list of all published template resources.<p>
     * 
     * @param rfsName the rfs name of the resource
     * 
     * @return the parameter string of the requested resource
     * 
     * @throws CmsException if something goes wrong
     */
    public String readStaticExportPublishedResourceParameters(String rfsName) throws CmsException {

        return m_securityManager.readStaticExportPublishedResourceParameters(m_context, rfsName);
    }

    /**
     * Returns a list of all template resources which must be processed during a static export.<p>
     * 
     * @param parameterResources flag for reading resources with parameters (1) or without (0)
     * 
     * @param timestamp a time stamp for reading the data from the db
     * 
     * @return a list of template resources as <code>{@link String}</code> objects
     * 
     * @throws CmsException if something goes wrong
     */
    public List<String> readStaticExportResources(int parameterResources, long timestamp) throws CmsException {

        return m_securityManager.readStaticExportResources(m_context, parameterResources, timestamp);
    }

    /**
     * Reads the URL names for all locales.<p> 
     * 
     * @param structureId the id of resource for which the URL names should be read 
     * @return returns the URL names for the resource 
     * 
     * @throws CmsException if something goes wrong 
     */
    public List<String> readUrlNamesForAllLocales(CmsUUID structureId) throws CmsException {

        List<String> detailNames = m_securityManager.readUrlNamesForAllLocales(m_context, structureId);
        if (detailNames.isEmpty()) {
            List<String> result = new ArrayList<String>();
            result.add(structureId.toString());
            return result;
        }
        return detailNames;
    }

    /**
     * Reads a user based on its id.<p>
     *
     * @param userId the id of the user to be read
     * 
     * @return the user with the given id
     * 
     * @throws CmsException if something goes wrong
     * 
     * @see #readHistoryPrincipal(CmsUUID) for retrieving data of deleted users
     */
    public CmsUser readUser(CmsUUID userId) throws CmsException {

        return m_securityManager.readUser(m_context, userId);
    }

    /**
     * Reads a user based on its name.<p>
     *
     * @param username the name of the user to be read
     * 
     * @return the user with the given name
     * 
     * @throws CmsException if something goes wrong
     */
    public CmsUser readUser(String username) throws CmsException {

        return m_securityManager.readUser(m_context, username);
    }

    /**
     * Returns a user, if the password is correct.<p>
     * 
     * If the user/pwd pair is not valid a <code>{@link CmsException}</code> is thrown.<p>
     *
     * @param username the name of the user to be returned
     * @param password the password of the user to be returned
     * 
     * @return the validated user
     *
     * @throws CmsException if operation was not successful
     */
    public CmsUser readUser(String username, String password) throws CmsException {

        return m_securityManager.readUser(m_context, username, password);
    }

    /**
     * Removes a resource from the current project of the user.<p>
     * 
     * This is used to reduce the current users project with the
     * specified resource, in case that the resource is already part of the project.
     * The resource is not really removed like in a regular copy operation, 
     * it is in fact only "disabled" in the current users project.<p>   
     * 
     * @param resourcename the name of the resource to remove to the current project (full current site relative path)
     * 
     * @throws CmsException if something goes wrong
     */
    public void removeResourceFromProject(String resourcename) throws CmsException {

        // TODO: this should be also possible if the resource has been deleted
        CmsResource resource = readResource(resourcename, CmsResourceFilter.ALL);
        getResourceType(resource).removeResourceFromProject(this, m_securityManager, resource);
    }

    /**
     * Removes a user from a group.<p>
     *
     * @param username the name of the user that is to be removed from the group
     * @param groupname the name of the group
     * 
     * @throws CmsException if operation was not successful
     */
    public void removeUserFromGroup(String username, String groupname) throws CmsException {

        m_securityManager.removeUserFromGroup(m_context, username, groupname, false);
    }

    /**
     * Renames a resource to the given destination name,
     * this is identical to a <code>move</code> operation.<p>
     *
     * @param source the name of the resource to rename (full current site relative path)
     * @param destination the new resource name (full path)
     * 
     * @throws CmsException if something goes wrong
     * 
     * @see #moveResource(String, String)
     */
    public void renameResource(String source, String destination) throws CmsException {

        moveResource(source, destination);
    }

    /**
     * Replaces the content, type and properties of a resource.<p>
     * 
     * @param resourcename the name of the resource to replace (full current site relative path)
     * @param type the new type of the resource
     * @param content the new content of the resource
     * @param properties the new properties of the resource
     * 
     * @throws CmsException if something goes wrong
     */
    public void replaceResource(String resourcename, int type, byte[] content, List<CmsProperty> properties)
    throws CmsException {

        CmsResource resource = readResource(resourcename, CmsResourceFilter.IGNORE_EXPIRATION);
        getResourceType(resource).replaceResource(this, m_securityManager, resource, type, content, properties);
    }

    /**
     * Restores a deleted resource identified by its structure id from the historical archive.<p>
     * 
     * These ids can be obtained from the {@link #readDeletedResources(String, boolean)} method.<p>
     * 
     * @param structureId the structure id of the resource to restore
     * 
     * @throws CmsException if something goes wrong
     */
    public void restoreDeletedResource(CmsUUID structureId) throws CmsException {

        m_securityManager.restoreDeletedResource(m_context, structureId);
    }

    /**
     * Restores a resource in the current project with a version from the historical archive.<p>
     * 
     * @param structureId the structure id of the resource to restore from the archive
     * @param version the desired version of the resource to be restored
     *
     * @throws CmsException if something goes wrong
     * 
     * @see #readResource(CmsUUID, int)
     */
    public void restoreResourceVersion(CmsUUID structureId, int version) throws CmsException {

        CmsResource resource = readResource(structureId, CmsResourceFilter.IGNORE_EXPIRATION);
        getResourceType(resource).restoreResource(this, m_securityManager, resource, version);
    }

    /**
     * Removes an access control entry of a given principal from a given resource.<p>
     * 
     * @param resourceName name of the resource
     * @param principalType the type of the principal (currently group or user)
     * @param principalName the name of the principal
     * 
     * @throws CmsException if something goes wrong
     */
    public void rmacc(String resourceName, String principalType, String principalName) throws CmsException {

        CmsResource res = readResource(resourceName, CmsResourceFilter.ALL);

        if (CmsUUID.isValidUUID(principalName)) {
            // principal name is in fact a UUID, probably the user was already deleted
            m_securityManager.removeAccessControlEntry(m_context, res, new CmsUUID(principalName));
        } else {
            try {
                // principal name not a UUID, assume this is a normal group or user name
                I_CmsPrincipal principal = CmsPrincipal.readPrincipal(this, principalType, principalName);
                m_securityManager.removeAccessControlEntry(m_context, res, principal.getId());
            } catch (CmsDbEntryNotFoundException e) {
                // role case
                CmsRole role = CmsRole.valueOfRoleName(principalName);
                if (role == null) {
                    throw e;
                }
                m_securityManager.removeAccessControlEntry(m_context, res, role.getId());
            }
        }
    }

    /**
     * Changes the "expire" date of a resource.<p>
     * 
     * @param resource the resource to change
     * @param dateExpired the new expire date of the changed resource
     * @param recursive if this operation is to be applied recursively to all resources in a folder
     * 
     * @throws CmsException if something goes wrong
     */
    public void setDateExpired(CmsResource resource, long dateExpired, boolean recursive) throws CmsException {

        getResourceType(resource).setDateExpired(this, m_securityManager, resource, dateExpired, recursive);
    }

    /**
     * Changes the "expire" date of a resource.<p>
     * 
     * @param resourcename the name of the resource to change (full current site relative path)
     * @param dateExpired the new expire date of the changed resource
     * @param recursive if this operation is to be applied recursively to all resources in a folder
     * 
     * @throws CmsException if something goes wrong
     */
    public void setDateExpired(String resourcename, long dateExpired, boolean recursive) throws CmsException {

        CmsResource resource = readResource(resourcename, CmsResourceFilter.IGNORE_EXPIRATION);
        setDateExpired(resource, dateExpired, recursive);
    }

    /**
     * Changes the "last modified" time stamp of a resource.<p>
     * 
     * @param resourcename the name of the resource to change (full current site relative path)
     * @param dateLastModified time stamp the new time stamp of the changed resource
     * @param recursive if this operation is to be applied recursively to all resources in a folder
     * 
     * @throws CmsException if something goes wrong
     */
    public void setDateLastModified(String resourcename, long dateLastModified, boolean recursive) throws CmsException {

        CmsResource resource = readResource(resourcename, CmsResourceFilter.IGNORE_EXPIRATION);
        getResourceType(resource).setDateLastModified(this, m_securityManager, resource, dateLastModified, recursive);
    }

    /**
     * Changes the "release" date of a resource.<p>
     * 
     * @param resource the resource to change
     * @param dateReleased the new release date of the changed resource
     * @param recursive if this operation is to be applied recursively to all resources in a folder
     * 
     * @throws CmsException if something goes wrong
     */
    public void setDateReleased(CmsResource resource, long dateReleased, boolean recursive) throws CmsException {

        getResourceType(resource).setDateReleased(this, m_securityManager, resource, dateReleased, recursive);
    }

    /**
     * Changes the "release" date of a resource.<p>
     * 
     * @param resourcename the name of the resource to change (full current site relative path)
     * @param dateReleased the new release date of the changed resource
     * @param recursive if this operation is to be applied recursively to all resources in a folder
     * 
     * @throws CmsException if something goes wrong
     */
    public void setDateReleased(String resourcename, long dateReleased, boolean recursive) throws CmsException {

        CmsResource resource = readResource(resourcename, CmsResourceFilter.IGNORE_EXPIRATION);
        setDateReleased(resource, dateReleased, recursive);
    }

    /**
     * Sets a new parent-group for an already existing group.<p>
     *
     * @param groupName the name of the group that should be updated
     * @param parentGroupName the name of the parent group to set, 
     *                      or <code>null</code> if the parent
     *                      group should be deleted.
     * 
     * @throws CmsException  if operation was not successful
     */
    public void setParentGroup(String groupName, String parentGroupName) throws CmsException {

        m_securityManager.setParentGroup(m_context, groupName, parentGroupName);
    }

    /**
     * Sets the password for a user.<p>
     *
     * @param username the name of the user
     * @param newPassword the new password
     *
     * @throws CmsException if operation was not successful
     */
    public void setPassword(String username, String newPassword) throws CmsException {

        m_securityManager.setPassword(m_context, username, newPassword);
    }

    /**
     * Sets the password for a specified user.<p>
     *
     * @param username the name of the user
     * @param oldPassword the old password
     * @param newPassword the new password
     * 
     * @throws CmsException if the user data could not be read from the database
     */
    public void setPassword(String username, String oldPassword, String newPassword) throws CmsException {

        m_securityManager.resetPassword(m_context, username, oldPassword, newPassword);
    }

    /**
     * Undeletes a resource.<p>
     * 
     * Only resources that have already been published once can be undeleted,
     * if a "new" resource is deleted it can not be undeleted.<p>
     * 
     * @param resourcename the name of the resource to undelete
     * @param recursive if this operation is to be applied recursively to all resources in a folder
     *
     * @throws CmsException if something goes wrong
     * 
     * @see CmsObject#undoChanges(String, CmsResource.CmsResourceUndoMode)
     */
    public void undeleteResource(String resourcename, boolean recursive) throws CmsException {

        CmsResource resource = readResource(resourcename, CmsResourceFilter.ALL);
        getResourceType(resource).undelete(this, m_securityManager, resource, recursive);
    }

    /**
     * Undoes all changes to a resource by restoring the version from the 
     * online project to the current offline project.<p>
     * 
     * @param resourcename the name of the resource to undo the changes for
     * @param mode the undo mode, one of the <code>{@link CmsResource.CmsResourceUndoMode}#UNDO_XXX</code> constants
     *
     * @throws CmsException if something goes wrong
     * 
     * @see CmsResource#UNDO_CONTENT
     * @see CmsResource#UNDO_CONTENT_RECURSIVE
     * @see CmsResource#UNDO_MOVE_CONTENT
     * @see CmsResource#UNDO_MOVE_CONTENT_RECURSIVE
     */
    public void undoChanges(String resourcename, CmsResource.CmsResourceUndoMode mode) throws CmsException {

        CmsResource resource = readResource(resourcename, CmsResourceFilter.ALL);
        getResourceType(resource).undoChanges(this, m_securityManager, resource, mode);
    }

    /**
     * Unlocks all resources of a project.
     *
     * @param id the id of the project to be unlocked
     *
     * @throws CmsException if operation was not successful
     */
    public void unlockProject(CmsUUID id) throws CmsException {

        m_securityManager.unlockProject(m_context, id);
    }

    /**
     * Unlocks a resource.<p>
     * 
     * @param resource the resource to unlock
     * 
     * @throws CmsException if something goes wrong
     */
    public void unlockResource(CmsResource resource) throws CmsException {

        getResourceType(resource).unlockResource(this, m_securityManager, resource);
    }

    /**
     * Unlocks a resource.<p>
     * 
     * @param resourcename the name of the resource to unlock (full current site relative path)
     * 
     * @throws CmsException if something goes wrong
     */
    public void unlockResource(String resourcename) throws CmsException {

        CmsResource resource = readResource(resourcename, CmsResourceFilter.ALL);
        getResourceType(resource).unlockResource(this, m_securityManager, resource);
    }

    /**
     * Tests if a user is member of the given group.<p>
     *
     * @param username the name of the user to test
     * @param groupname the name of the group to test
     * 
     * @return <code>true</code>, if the user is in the group; or <code>false</code> otherwise
     *
     * @throws CmsException if operation was not successful
     */
    public boolean userInGroup(String username, String groupname) throws CmsException {

        return (m_securityManager.userInGroup(m_context, username, groupname));
    }

    /**
     * This method checks if a new password follows the rules for
     * new passwords, which are defined by a Class implementing the 
     * <code>{@link org.opencms.security.I_CmsPasswordHandler}</code> 
     * interface and configured in the opencms.properties file.<p>
     * 
     * If this method throws no exception the password is valid.<p>
     *
     * @param password the new password that has to be checked
     *
     * @throws CmsSecurityException if the password is not valid
     */
    public void validatePassword(String password) throws CmsSecurityException {

        m_securityManager.validatePassword(password);
    }

    /**
     * Writes a resource to the OpenCms VFS, including it's content.<p>
     * 
     * Applies only to resources of type <code>{@link CmsFile}</code>
     * i.e. resources that have a binary content attached.<p>
     * 
     * Certain resource types might apply content validation or transformation rules 
     * before the resource is actually written to the VFS. The returned result
     * might therefore be a modified version from the provided original.<p>
     *
     * @param resource the resource to write
     *
     * @return the written resource (may have been modified)
     *
     * @throws CmsException if something goes wrong
     */
    public CmsFile writeFile(CmsFile resource) throws CmsException {

        return getResourceType(resource).writeFile(this, m_securityManager, resource);
    }

    /**
     * Writes an already existing group.<p>
     *
     * The group has to be a valid OpenCms group.<br>
     * 
     * The group will be completely overridden by the given data.<p>
     *
     * @param group the group that should be written
     * 
     * @throws CmsException if operation was not successful
     */
    public void writeGroup(CmsGroup group) throws CmsException {

        m_securityManager.writeGroup(m_context, group);
    }

    /**
     * Creates a historical entry of the current project.<p>
     * 
     * @param publishTag the correlative publish tag
     * @param publishDate the date of publishing

     * @throws CmsException if operation was not successful
     */
    public void writeHistoryProject(int publishTag, long publishDate) throws CmsException {

        m_securityManager.writeHistoryProject(m_context, publishTag, publishDate);
    }

    /**
     * Writes an already existing project.<p>
     *
     * The project id has to be a valid OpenCms project id.<br>
     * 
     * The project with the given id will be completely overridden
     * by the given data.<p>
     *
     * @param project the project that should be written
     * 
     * @throws CmsException if operation was not successful
     */
    public void writeProject(CmsProject project) throws CmsException {

        m_securityManager.writeProject(m_context, project);
    }

    /**
     * Writes a property for a specified resource.<p>
     * 
     * @param resourcename the name of resource with complete path
     * @param property the property to write
     * 
     * @throws CmsException if something goes wrong
     */
    public void writePropertyObject(String resourcename, CmsProperty property) throws CmsException {

        CmsResource resource = readResource(resourcename, CmsResourceFilter.IGNORE_EXPIRATION);
        getResourceType(resource).writePropertyObject(this, m_securityManager, resource, property);
    }

    /**
     * Writes a list of properties for a specified resource.<p>
     * 
     * Code calling this method has to ensure that the no properties 
     * <code>a, b</code> are contained in the specified list so that <code>a.equals(b)</code>, 
     * otherwise an exception is thrown.<p>
     * 
     * @param res the resource
     * @param properties the list of properties to write
     * 
     * @throws CmsException if something goes wrong
     */
    public void writePropertyObjects(CmsResource res, List<CmsProperty> properties) throws CmsException {

        getResourceType(res).writePropertyObjects(this, m_securityManager, res, properties);
    }

    /**
     * Writes a list of properties for a specified resource.<p>
     * 
     * Code calling this method has to ensure that the no properties 
     * <code>a, b</code> are contained in the specified list so that <code>a.equals(b)</code>, 
     * otherwise an exception is thrown.<p>
     * 
     * @param resourcename the name of resource with complete path
     * @param properties the list of properties to write
     * 
     * @throws CmsException if something goes wrong
     */
    public void writePropertyObjects(String resourcename, List<CmsProperty> properties) throws CmsException {

        CmsResource resource = readResource(resourcename, CmsResourceFilter.IGNORE_EXPIRATION);
        getResourceType(resource).writePropertyObjects(this, m_securityManager, resource, properties);
    }

    /**
     * Writes a resource.<p>
     *
     * @param resource the file to write
     *
     * @throws CmsException if resource type is set to folder, or
     *                      if the user has not the rights to write the file header.
     */
    public void writeResource(CmsResource resource) throws CmsException {

        m_securityManager.writeResource(m_context, resource);
    }

    /**
     * Writes a published resource entry.<p>
     * 
     * This is done during static export.<p>
     * 
     * @param resourceName The name of the resource to be added to the static export
     * @param linkType the type of resource exported (0= non-parameter, 1=parameter)
     * @param linkParameter the parameters added to the resource
     * @param timestamp a time stamp for writing the data into the db
     * 
     * @throws CmsException if something goes wrong
     */
    public void writeStaticExportPublishedResource(
        String resourceName,
        int linkType,
        String linkParameter,
        long timestamp) throws CmsException {

        m_securityManager.writeStaticExportPublishedResource(
            m_context,
            resourceName,
            linkType,
            linkParameter,
            timestamp);
    }

    /**
     * Writes a new URL name mapping for a given resource.<p>
     * 
     * The first name from the given name sequence which is not already mapped to another resource will be used
     * for the URL name mapping.<p>
     * 
     * @param nameSeq an iterator for generating names for the mapping   
     * @param structureId the structure id to which the name should be mapped
     * @param locale the locale of the mapping 
     *   
     * @return the name which was actually mapped to the structure id
     *  
     * @throws CmsException if something goes wrong 
     */
    public String writeUrlNameMapping(Iterator<String> nameSeq, CmsUUID structureId, String locale) throws CmsException {

        return m_securityManager.writeUrlNameMapping(m_context, nameSeq, structureId, locale);
    }

    /**
     * Writes a new URL name mapping for a given resource.<p>
     * 
     * This method uses {@link CmsNumberSuffixNameSequence} to generate a sequence of name candidates 
     * from the given base name.<p>
     * 
     * @param name the base name for the mapping 
     * @param structureId the structure id to which the name should be mapped
     * @param locale the locale of the mapping 
     *  
     * @return the URL name that was actually used for the mapping
     * 
     * @throws CmsException if something goes wrong 
     */
    public String writeUrlNameMapping(String name, CmsUUID structureId, String locale) throws CmsException {

        return writeUrlNameMapping(new CmsNumberSuffixNameSequence(name), structureId, locale);
    }

    /**
     * Updates the user information. <p>
     * 
     * The user id has to be a valid OpenCms user id.<br>
     * 
     * The user with the given id will be completely overriden
     * by the given data.<p>
     *
     * @param user the user to be written
     *
     * @throws CmsException if operation was not successful
     */
    public void writeUser(CmsUser user) throws CmsException {

        m_securityManager.writeUser(m_context, user);
    }

    /**
     * Adds a new relation to the given resource.<p>
     * 
     * @param resourceName the name of the source resource
     * @param targetPath the path of the target resource
     * @param relationType the type of the relation
     * @param importCase if importing relations
     * 
     * @throws CmsException if something goes wrong
     */
    private void createRelation(String resourceName, String targetPath, String relationType, boolean importCase)
    throws CmsException {

        CmsResource resource = readResource(resourceName, CmsResourceFilter.IGNORE_EXPIRATION);
        CmsResource target = readResource(targetPath, CmsResourceFilter.IGNORE_EXPIRATION);
        CmsRelationType type = CmsRelationType.valueOf(relationType);
        m_securityManager.addRelationToResource(m_context, resource, target, type, importCase);
    }

    /**
     * Notify all event listeners that a particular event has occurred.<p>
     * 
     * The event will be given to all registered <code>{@link I_CmsEventListener}</code>s.<p>
     * 
     * @param type the type of the event
     * @param data a data object that contains data used by the event listeners
     * 
     * @see OpenCms#addCmsEventListener(I_CmsEventListener)
     * @see OpenCms#addCmsEventListener(I_CmsEventListener, int[])
     */
    private void fireEvent(int type, Object data) {

        OpenCms.fireCmsEvent(type, Collections.singletonMap("data", data));
    }

    /**
     * Convenience method to get the initialized resource type instance for the given resource, 
     * with a fall back to special "unknown" resource types in case the resource type is not configured.<p>
     * 
     * @param resource the resource to get the type for
     * 
     * @return the initialized resource type instance for the given resource
     * 
     * @see org.opencms.loader.CmsResourceManager#getResourceType(int)
     */
    private I_CmsResourceType getResourceType(CmsResource resource) {

        return OpenCms.getResourceManager().getResourceType(resource);
    }

    /**
     * Convenience method to return the initialized resource type 
     * instance for the given id.<p>
     * 
     * @param resourceType the id of the resource type to get
     * 
     * @return the initialized resource type instance for the given id
     * 
     * @throws CmsException if something goes wrong
     * 
     * @see org.opencms.loader.CmsResourceManager#getResourceType(int)
     */
    private I_CmsResourceType getResourceType(int resourceType) throws CmsException {

        return OpenCms.getResourceManager().getResourceType(resourceType);
    }

    /**
     * Initializes this <code>{@link CmsObject}</code> with the provided user context and database connection.<p>
     * 
     * @param securityManager the security manager
     * @param context the request context that contains the user authentication
     */
    private void init(CmsSecurityManager securityManager, CmsRequestContext context) {

        m_securityManager = securityManager;
        m_context = context;
    }

    /**
     * Locks a resource.<p>
     *
     * The <code>type</code> parameter controls what kind of lock is used.<br>
     * Possible values for this parameter are: <br>
     * <ul>
     * <li><code>{@link org.opencms.lock.CmsLockType#EXCLUSIVE}</code></li>
     * <li><code>{@link org.opencms.lock.CmsLockType#TEMPORARY}</code></li>
     * </ul><p>
     * 
     * @param resourcename the name of the resource to lock (full current site relative path)
     * @param type type of the lock
     * 
     * @throws CmsException if something goes wrong
     */
    private void lockResource(String resourcename, CmsLockType type) throws CmsException {

        // throw the exception if resource name is an empty string
        if (CmsStringUtil.isEmptyOrWhitespaceOnly(resourcename)) {
            throw new CmsVfsResourceNotFoundException(Messages.get().container(
                Messages.ERR_LOCK_RESOURCE_1,
                resourcename));
        }
        CmsResource resource = readResource(resourcename, CmsResourceFilter.ALL);
        getResourceType(resource).lockResource(this, m_securityManager, resource, type);
    }

}
