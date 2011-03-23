/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/file/types/CmsResourceTypeJsp.java,v $
 * Date   : $Date: 2011/03/23 14:52:35 $
 * Version: $Revision: 1.37 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (c) 2002 - 2011 Alkacon Software GmbH (http://www.alkacon.com)
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
import org.opencms.file.CmsResource;
import org.opencms.file.CmsResource.CmsResourceDeleteMode;
import org.opencms.file.CmsResource.CmsResourceUndoMode;
import org.opencms.i18n.CmsEncoder;
import org.opencms.i18n.CmsLocaleManager;
import org.opencms.jsp.util.CmsJspLinkMacroResolver;
import org.opencms.loader.CmsJspLoader;
import org.opencms.main.CmsException;
import org.opencms.main.CmsIllegalArgumentException;
import org.opencms.main.OpenCms;

import java.io.UnsupportedEncodingException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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
 * @version $Revision: 1.37 $ 
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

    /** JSP Loader instance. */
    protected CmsJspLoader m_jspLoader;

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
     * @see org.opencms.file.types.A_CmsResourceType#chtype(org.opencms.file.CmsObject, org.opencms.db.CmsSecurityManager, org.opencms.file.CmsResource, int)
     */
    @Override
    public void chtype(CmsObject cms, CmsSecurityManager securityManager, CmsResource resource, int type)
    throws CmsException {

        Set references = getReferencingStrongLinks(cms, resource);
        super.chtype(cms, securityManager, resource, type);
        removeReferencingFromCache(references);
    }

    /**
     * @see org.opencms.file.types.A_CmsResourceType#deleteResource(org.opencms.file.CmsObject, org.opencms.db.CmsSecurityManager, org.opencms.file.CmsResource, org.opencms.file.CmsResource.CmsResourceDeleteMode)
     */
    @Override
    public void deleteResource(
        CmsObject cms,
        CmsSecurityManager securityManager,
        CmsResource resource,
        CmsResourceDeleteMode siblingMode) throws CmsException {

        Set references = getReferencingStrongLinks(cms, resource);
        super.deleteResource(cms, securityManager, resource, siblingMode);
        removeReferencingFromCache(references);
    }

    /**
     * @see org.opencms.file.types.I_CmsResourceType#getLoaderId()
     */
    @Override
    public int getLoaderId() {

        return CmsJspLoader.RESOURCE_LOADER_ID;
    }

    /**
     * @see org.opencms.file.types.A_CmsResourceType#initConfiguration(java.lang.String, java.lang.String, String)
     */
    @Override
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
     * @see org.opencms.file.types.A_CmsResourceType#initialize(org.opencms.file.CmsObject)
     */
    @Override
    public void initialize(CmsObject cms) {

        super.initialize(cms);
        try {
            m_jspLoader = (CmsJspLoader)OpenCms.getResourceManager().getLoader(CmsJspLoader.RESOURCE_LOADER_ID);
        } catch (ArrayIndexOutOfBoundsException e) {
            // ignore, loader not configured
        }
    }

    /**
     * @see org.opencms.file.types.A_CmsResourceType#moveResource(org.opencms.file.CmsObject, org.opencms.db.CmsSecurityManager, org.opencms.file.CmsResource, java.lang.String)
     */
    @Override
    public void moveResource(CmsObject cms, CmsSecurityManager securityManager, CmsResource resource, String destination)
    throws CmsException, CmsIllegalArgumentException {

        Set references = getReferencingStrongLinks(cms, resource);
        super.moveResource(cms, securityManager, resource, destination);
        removeReferencingFromCache(references);
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
    @Override
    public void replaceResource(
        CmsObject cms,
        CmsSecurityManager securityManager,
        CmsResource resource,
        int type,
        byte[] content,
        List properties) throws CmsException {

        Set references = getReferencingStrongLinks(cms, resource);
        super.replaceResource(cms, securityManager, resource, type, content, properties);
        removeReferencingFromCache(references);
    }

    /**
     * @see org.opencms.file.types.A_CmsResourceType#restoreResource(org.opencms.file.CmsObject, org.opencms.db.CmsSecurityManager, org.opencms.file.CmsResource, int)
     */
    @Override
    public void restoreResource(CmsObject cms, CmsSecurityManager securityManager, CmsResource resource, int version)
    throws CmsException {

        Set references = getReferencingStrongLinks(cms, resource);
        super.restoreResource(cms, securityManager, resource, version);
        removeReferencingFromCache(references);
    }

    /**
     * @see org.opencms.file.types.A_CmsResourceType#setDateExpired(org.opencms.file.CmsObject, org.opencms.db.CmsSecurityManager, org.opencms.file.CmsResource, long, boolean)
     */
    @Override
    public void setDateExpired(
        CmsObject cms,
        CmsSecurityManager securityManager,
        CmsResource resource,
        long dateExpired,
        boolean recursive) throws CmsException {

        Set references = getReferencingStrongLinks(cms, resource);
        super.setDateExpired(cms, securityManager, resource, dateExpired, recursive);
        removeReferencingFromCache(references);
    }

    /**
     * @see org.opencms.file.types.A_CmsResourceType#setDateLastModified(org.opencms.file.CmsObject, org.opencms.db.CmsSecurityManager, org.opencms.file.CmsResource, long, boolean)
     */
    @Override
    public void setDateLastModified(
        CmsObject cms,
        CmsSecurityManager securityManager,
        CmsResource resource,
        long dateLastModified,
        boolean recursive) throws CmsException {

        Set references = getReferencingStrongLinks(cms, resource);
        super.setDateLastModified(cms, securityManager, resource, dateLastModified, recursive);
        removeReferencingFromCache(references);
    }

    /**
     * @see org.opencms.file.types.A_CmsResourceType#setDateReleased(org.opencms.file.CmsObject, org.opencms.db.CmsSecurityManager, org.opencms.file.CmsResource, long, boolean)
     */
    @Override
    public void setDateReleased(
        CmsObject cms,
        CmsSecurityManager securityManager,
        CmsResource resource,
        long dateReleased,
        boolean recursive) throws CmsException {

        Set references = getReferencingStrongLinks(cms, resource);
        super.setDateReleased(cms, securityManager, resource, dateReleased, recursive);
        removeReferencingFromCache(references);
    }

    /**
     * @see org.opencms.file.types.A_CmsResourceType#undoChanges(org.opencms.file.CmsObject, org.opencms.db.CmsSecurityManager, org.opencms.file.CmsResource, org.opencms.file.CmsResource.CmsResourceUndoMode)
     */
    @Override
    public void undoChanges(
        CmsObject cms,
        CmsSecurityManager securityManager,
        CmsResource resource,
        CmsResourceUndoMode mode) throws CmsException {

        Set references = getReferencingStrongLinks(cms, resource);
        super.undoChanges(cms, securityManager, resource, mode);
        removeReferencingFromCache(references);
    }

    /**
     * @see org.opencms.file.types.A_CmsResourceType#writeFile(org.opencms.file.CmsObject, org.opencms.db.CmsSecurityManager, org.opencms.file.CmsFile)
     */
    @Override
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
        Set references = getReferencingStrongLinks(cms, resource);
        CmsFile file = super.writeFile(cms, securityManager, resource);
        removeReferencingFromCache(references);
        return file;
    }

    /**
     * Returns a set of root paths of files that are including the given resource using the 'link.strong' macro.<p>
     * 
     * @param cms the current cms context
     * @param resource the resource to check
     * 
     * @return the set of referencing paths
     * 
     * @throws CmsException if something goes wrong
     */
    protected Set getReferencingStrongLinks(CmsObject cms, CmsResource resource) throws CmsException {

        Set references = new HashSet();
        if (m_jspLoader == null) {
            return references;
        }
        m_jspLoader.getReferencingStrongLinks(cms, resource, references);
        return references;
    }

    /**
     * Removes the referencing resources from the cache.<p>
     * 
     * @param references the references to remove
     */
    protected void removeReferencingFromCache(Set references) {

        if (m_jspLoader != null) {
            m_jspLoader.removeFromCache(references, false);
        }
    }
}
