/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/file/types/CmsResourceTypeJsp.java,v $
 * Date   : $Date: 2008/03/17 14:51:49 $
 * Version: $Revision: 1.29 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (c) 2002 - 2008 Alkacon Software GmbH (http://www.alkacon.com)
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

package org.opencms.file.types;

import org.opencms.configuration.CmsConfigurationException;
import org.opencms.db.CmsSecurityManager;
import org.opencms.file.CmsFile;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsProperty;
import org.opencms.file.CmsResource;
import org.opencms.file.CmsResourceFilter;
import org.opencms.file.CmsResource.CmsResourceCopyMode;
import org.opencms.file.CmsResource.CmsResourceDeleteMode;
import org.opencms.file.CmsResource.CmsResourceUndoMode;
import org.opencms.i18n.CmsEncoder;
import org.opencms.i18n.CmsLocaleManager;
import org.opencms.jsp.util.CmsJspLinkMacroResolver;
import org.opencms.loader.CmsJspLoader;
import org.opencms.main.CmsException;
import org.opencms.main.CmsIllegalArgumentException;
import org.opencms.main.OpenCms;
import org.opencms.security.CmsRole;

import java.io.UnsupportedEncodingException;
import java.util.List;

/**
 * Resource type descriptor for the type "jsp".<p>
 * 
 * Ensures that some required file properties are attached to new JSPs.<p>
 * 
 * The value for the encoding properties of a new JSP usually is the
 * system default encoding, but this can be overwritten by 
 * a configuration parameters set in <code>opencms-vfs.xml</code>.<p>
 *
 * @author Alexander Kandzior 
 * 
 * @version $Revision: 1.29 $ 
 * 
 * @since 6.0.0 
 */
public class CmsResourceTypeJsp extends A_CmsResourceTypeLinkParseable {

    /** Indicates that the static configuration of the resource type has been frozen. */
    private static boolean m_staticFrozen;

    /** The static type id of this resource type. */
    private static int m_staticTypeId;

    /** The type id of this resource type. */
    private static final int RESOURCE_TYPE_ID = 4;

    /** The name of this resource type. */
    private static final String RESOURCE_TYPE_NAME = "jsp";

    /**
     * Default constructor, used to initialize member variables.<p>
     */
    public CmsResourceTypeJsp() {

        super();
        m_typeId = RESOURCE_TYPE_ID;
        m_typeName = RESOURCE_TYPE_NAME;
    }

    /**
     * Returns the static type id of this (default) resource type.<p>
     * 
     * @return the static type id of this (default) resource type
     */
    public static int getStaticTypeId() {

        return m_staticTypeId;
    }

    /**
     * Returns the static type name of this (default) resource type.<p>
     * 
     * @return the static type name of this (default) resource type
     */
    public static String getStaticTypeName() {

        return RESOURCE_TYPE_NAME;
    }

    /**
     * @see org.opencms.file.types.A_CmsResourceType#chflags(org.opencms.file.CmsObject, org.opencms.db.CmsSecurityManager, org.opencms.file.CmsResource, int)
     */
    public void chflags(CmsObject cms, CmsSecurityManager securityManager, CmsResource resource, int flags)
    throws CmsException {

        // security check
        securityManager.checkRoleForResource(cms.getRequestContext(), CmsRole.DEVELOPER, resource);
        // change flags
        super.chflags(cms, securityManager, resource, flags);
    }

    /**
     * @see org.opencms.file.types.A_CmsResourceType#chtype(org.opencms.file.CmsObject, org.opencms.db.CmsSecurityManager, org.opencms.file.CmsResource, int)
     */
    public void chtype(CmsObject cms, CmsSecurityManager securityManager, CmsResource resource, int type)
    throws CmsException {

        // security check
        securityManager.checkRoleForResource(cms.getRequestContext(), CmsRole.DEVELOPER, resource);
        // change type
        super.chtype(cms, securityManager, resource, type);
    }

    /**
     * @see org.opencms.file.types.A_CmsResourceType#copyResource(org.opencms.file.CmsObject, org.opencms.db.CmsSecurityManager, org.opencms.file.CmsResource, java.lang.String, org.opencms.file.CmsResource.CmsResourceCopyMode)
     */
    public void copyResource(
        CmsObject cms,
        CmsSecurityManager securityManager,
        CmsResource source,
        String destination,
        CmsResourceCopyMode siblingMode) throws CmsException {

        // need the parent folder for security check
        String parentFolderName = CmsResource.getParentFolder(cms.getRequestContext().addSiteRoot(destination));
        CmsResource parentFolder = securityManager.readFolder(
            cms.getRequestContext(),
            parentFolderName,
            CmsResourceFilter.ALL);
        // security check
        securityManager.checkRoleForResource(cms.getRequestContext(), CmsRole.DEVELOPER, parentFolder);
        // copy resource
        super.copyResource(cms, securityManager, source, destination, siblingMode);
    }

    /**
     * @see org.opencms.file.types.A_CmsResourceType#createResource(org.opencms.file.CmsObject, org.opencms.db.CmsSecurityManager, java.lang.String, byte[], java.util.List)
     */
    public CmsResource createResource(
        CmsObject cms,
        CmsSecurityManager securityManager,
        String resourcename,
        byte[] content,
        List properties) throws CmsException {

        // need the parent folder for security check
        String parentFolderName = CmsResource.getParentFolder(cms.getRequestContext().addSiteRoot(resourcename));
        CmsResource parentFolder = securityManager.readFolder(
            cms.getRequestContext(),
            parentFolderName,
            CmsResourceFilter.ALL);
        // security check
        securityManager.checkRoleForResource(cms.getRequestContext(), CmsRole.DEVELOPER, parentFolder);
        // create resource
        return super.createResource(cms, securityManager, resourcename, content, properties);
    }

    /**
     * @see org.opencms.file.types.A_CmsResourceType#createSibling(org.opencms.file.CmsObject, org.opencms.db.CmsSecurityManager, org.opencms.file.CmsResource, java.lang.String, java.util.List)
     */
    public CmsResource createSibling(
        CmsObject cms,
        CmsSecurityManager securityManager,
        CmsResource source,
        String destination,
        List properties) throws CmsException {

        // need the parent folder for security check
        String parentFolderName = CmsResource.getParentFolder(cms.getRequestContext().addSiteRoot(destination));
        CmsResource parentFolder = securityManager.readFolder(
            cms.getRequestContext(),
            parentFolderName,
            CmsResourceFilter.ALL);
        // security check
        securityManager.checkRoleForResource(cms.getRequestContext(), CmsRole.DEVELOPER, parentFolder);
        // create sibling
        return super.createSibling(cms, securityManager, source, destination, properties);
    }

    /**
     * @see org.opencms.file.types.A_CmsResourceType#deleteResource(org.opencms.file.CmsObject, org.opencms.db.CmsSecurityManager, org.opencms.file.CmsResource, org.opencms.file.CmsResource.CmsResourceDeleteMode)
     */
    public void deleteResource(
        CmsObject cms,
        CmsSecurityManager securityManager,
        CmsResource resource,
        CmsResourceDeleteMode siblingMode) throws CmsException {

        // security check
        securityManager.checkRoleForResource(cms.getRequestContext(), CmsRole.DEVELOPER, resource);
        // delete resource
        super.deleteResource(cms, securityManager, resource, siblingMode);
    }

    /**
     * @see org.opencms.file.types.I_CmsResourceType#getLoaderId()
     */
    public int getLoaderId() {

        return CmsJspLoader.RESOURCE_LOADER_ID;
    }

    /**
     * @see org.opencms.file.types.A_CmsResourceType#importResource(org.opencms.file.CmsObject, org.opencms.db.CmsSecurityManager, java.lang.String, org.opencms.file.CmsResource, byte[], java.util.List)
     */
    public CmsResource importResource(
        CmsObject cms,
        CmsSecurityManager securityManager,
        String resourcename,
        CmsResource resource,
        byte[] content,
        List properties) throws CmsException {

        // need the parent folder for security check
        String parentFolderName = CmsResource.getParentFolder(cms.getRequestContext().addSiteRoot(resourcename));
        CmsResource parentFolder = securityManager.readFolder(
            cms.getRequestContext(),
            parentFolderName,
            CmsResourceFilter.ALL);
        // security check
        securityManager.checkRoleForResource(cms.getRequestContext(), CmsRole.DEVELOPER, parentFolder);
        // import resource
        return super.importResource(cms, securityManager, resourcename, resource, content, properties);
    }

    /**
     * @see org.opencms.file.types.A_CmsResourceType#initConfiguration(java.lang.String, java.lang.String, String)
     */
    public void initConfiguration(String name, String id, String className) throws CmsConfigurationException {

        if ((OpenCms.getRunLevel() > OpenCms.RUNLEVEL_2_INITIALIZING) && m_staticFrozen) {
            // configuration already frozen
            throw new CmsConfigurationException(Messages.get().container(
                Messages.ERR_CONFIG_FROZEN_3,
                this.getClass().getName(),
                getStaticTypeName(),
                new Integer(getStaticTypeId())));
        }

        if (!RESOURCE_TYPE_NAME.equals(name)) {
            // default resource type MUST have default name
            throw new CmsConfigurationException(Messages.get().container(
                Messages.ERR_INVALID_RESTYPE_CONFIG_NAME_3,
                this.getClass().getName(),
                RESOURCE_TYPE_NAME,
                name));
        }

        // freeze the configuration
        m_staticFrozen = true;

        super.initConfiguration(RESOURCE_TYPE_NAME, id, className);
        // set static members with values from the configuration        
        m_staticTypeId = m_typeId;
    }

    /**
     * @see org.opencms.file.types.A_CmsResourceType#moveResource(org.opencms.file.CmsObject, org.opencms.db.CmsSecurityManager, org.opencms.file.CmsResource, java.lang.String)
     */
    public void moveResource(CmsObject cms, CmsSecurityManager securityManager, CmsResource resource, String destination)
    throws CmsException, CmsIllegalArgumentException {

        // need the parent folder for security check
        String parentFolderName = CmsResource.getParentFolder(cms.getRequestContext().addSiteRoot(destination));
        CmsResource parentFolder = securityManager.readFolder(
            cms.getRequestContext(),
            parentFolderName,
            CmsResourceFilter.ALL);
        // security check
        securityManager.checkRoleForResource(cms.getRequestContext(), CmsRole.DEVELOPER, parentFolder);
        // move resource
        super.moveResource(cms, securityManager, resource, destination);
    }

    /**
     * @see org.opencms.relations.I_CmsLinkParseable#parseLinks(org.opencms.file.CmsObject, org.opencms.file.CmsFile)
     */
    public List parseLinks(CmsObject cms, CmsFile file) {

        CmsJspLinkMacroResolver macroResolver = new CmsJspLinkMacroResolver(cms, file.getRootPath(), false);
        String encoding = CmsLocaleManager.getResourceEncoding(cms, file);
        String content = CmsEncoder.createString(file.getContents(), encoding);
        macroResolver.resolveMacros(content); // ignore return value
        return macroResolver.getLinks();
    }

    /**
     * @see org.opencms.file.types.A_CmsResourceType#replaceResource(org.opencms.file.CmsObject, org.opencms.db.CmsSecurityManager, org.opencms.file.CmsResource, int, byte[], java.util.List)
     */
    public void replaceResource(
        CmsObject cms,
        CmsSecurityManager securityManager,
        CmsResource resource,
        int type,
        byte[] content,
        List properties) throws CmsException {

        // security check
        securityManager.checkRoleForResource(cms.getRequestContext(), CmsRole.DEVELOPER, resource);
        // replace resource
        super.replaceResource(cms, securityManager, resource, type, content, properties);
    }

    /**
     * @see org.opencms.file.types.A_CmsResourceType#restoreResource(org.opencms.file.CmsObject, org.opencms.db.CmsSecurityManager, org.opencms.file.CmsResource, int)
     */
    public void restoreResource(CmsObject cms, CmsSecurityManager securityManager, CmsResource resource, int version)
    throws CmsException {

        // security check
        securityManager.checkRoleForResource(cms.getRequestContext(), CmsRole.DEVELOPER, resource);
        // restore resource
        super.restoreResource(cms, securityManager, resource, version);
    }

    /**
     * @see org.opencms.file.types.A_CmsResourceType#setDateExpired(org.opencms.file.CmsObject, org.opencms.db.CmsSecurityManager, org.opencms.file.CmsResource, long, boolean)
     */
    public void setDateExpired(
        CmsObject cms,
        CmsSecurityManager securityManager,
        CmsResource resource,
        long dateExpired,
        boolean recursive) throws CmsException {

        // security check
        securityManager.checkRoleForResource(cms.getRequestContext(), CmsRole.DEVELOPER, resource);
        // set date expired
        super.setDateExpired(cms, securityManager, resource, dateExpired, recursive);
    }

    /**
     * @see org.opencms.file.types.A_CmsResourceType#setDateLastModified(org.opencms.file.CmsObject, org.opencms.db.CmsSecurityManager, org.opencms.file.CmsResource, long, boolean)
     */
    public void setDateLastModified(
        CmsObject cms,
        CmsSecurityManager securityManager,
        CmsResource resource,
        long dateLastModified,
        boolean recursive) throws CmsException {

        // security check
        securityManager.checkRoleForResource(cms.getRequestContext(), CmsRole.DEVELOPER, resource);
        // set date modified
        super.setDateLastModified(cms, securityManager, resource, dateLastModified, recursive);
    }

    /**
     * @see org.opencms.file.types.A_CmsResourceType#setDateReleased(org.opencms.file.CmsObject, org.opencms.db.CmsSecurityManager, org.opencms.file.CmsResource, long, boolean)
     */
    public void setDateReleased(
        CmsObject cms,
        CmsSecurityManager securityManager,
        CmsResource resource,
        long dateReleased,
        boolean recursive) throws CmsException {

        // security check
        securityManager.checkRoleForResource(cms.getRequestContext(), CmsRole.DEVELOPER, resource);
        // set date released
        super.setDateReleased(cms, securityManager, resource, dateReleased, recursive);
    }

    /**
     * @see org.opencms.file.types.A_CmsResourceType#undelete(org.opencms.file.CmsObject, org.opencms.db.CmsSecurityManager, org.opencms.file.CmsResource, boolean)
     */
    public void undelete(CmsObject cms, CmsSecurityManager securityManager, CmsResource resource, boolean recursive)
    throws CmsException {

        // security check
        securityManager.checkRoleForResource(cms.getRequestContext(), CmsRole.DEVELOPER, resource);
        // undelete resource
        super.undelete(cms, securityManager, resource, recursive);
    }

    /**
     * @see org.opencms.file.types.A_CmsResourceType#undoChanges(org.opencms.file.CmsObject, org.opencms.db.CmsSecurityManager, org.opencms.file.CmsResource, org.opencms.file.CmsResource.CmsResourceUndoMode)
     */
    public void undoChanges(
        CmsObject cms,
        CmsSecurityManager securityManager,
        CmsResource resource,
        CmsResourceUndoMode mode) throws CmsException {

        // security check
        securityManager.checkRoleForResource(cms.getRequestContext(), CmsRole.DEVELOPER, resource);
        // undo resource
        super.undoChanges(cms, securityManager, resource, mode);
    }

    /**
     * @see org.opencms.file.types.A_CmsResourceType#writeFile(org.opencms.file.CmsObject, org.opencms.db.CmsSecurityManager, org.opencms.file.CmsFile)
     */
    public CmsFile writeFile(CmsObject cms, CmsSecurityManager securityManager, CmsFile resource) throws CmsException {

        // actualize the link paths and/or ids
        CmsJspLinkMacroResolver macroResolver = new CmsJspLinkMacroResolver(cms, resource.getRootPath(), false);
        String encoding = CmsLocaleManager.getResourceEncoding(cms, resource);
        String content = CmsEncoder.createString(resource.getContents(), encoding);
        content = macroResolver.resolveMacros(content);
        try {
            resource.setContents(content.getBytes(encoding));
        } catch (UnsupportedEncodingException e) {
            // this should usually never happen since the encoding is already used before
            resource.setContents(content.getBytes());
        }
        // write the content with the 'right' links
        return super.writeFile(cms, securityManager, resource);
    }

    /**
     * @see org.opencms.file.types.A_CmsResourceType#writePropertyObject(org.opencms.file.CmsObject, org.opencms.db.CmsSecurityManager, org.opencms.file.CmsResource, org.opencms.file.CmsProperty)
     */
    public void writePropertyObject(
        CmsObject cms,
        CmsSecurityManager securityManager,
        CmsResource resource,
        CmsProperty property) throws CmsException {

        // security check
        securityManager.checkRoleForResource(cms.getRequestContext(), CmsRole.DEVELOPER, resource);
        // write properties
        super.writePropertyObject(cms, securityManager, resource, property);
    }

    /**
     * @see org.opencms.file.types.A_CmsResourceType#writePropertyObjects(org.opencms.file.CmsObject, org.opencms.db.CmsSecurityManager, org.opencms.file.CmsResource, java.util.List)
     */
    public void writePropertyObjects(
        CmsObject cms,
        CmsSecurityManager securityManager,
        CmsResource resource,
        List properties) throws CmsException {

        // security check
        securityManager.checkRoleForResource(cms.getRequestContext(), CmsRole.DEVELOPER, resource);
        // write properties
        super.writePropertyObjects(cms, securityManager, resource, properties);
    }
}