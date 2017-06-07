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

package org.opencms.file.types;

import org.opencms.configuration.CmsConfigurationException;
import org.opencms.db.CmsSecurityManager;
import org.opencms.file.CmsFile;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsProperty;
import org.opencms.file.CmsResource;
import org.opencms.file.CmsResource.CmsResourceDeleteMode;
import org.opencms.file.CmsResource.CmsResourceUndoMode;
import org.opencms.loader.CmsDumpLoader;
import org.opencms.loader.CmsJspLoader;
import org.opencms.main.CmsException;
import org.opencms.main.CmsIllegalArgumentException;
import org.opencms.main.OpenCms;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Resource type descriptor for the type "plain".<p>
 *
 * @since 6.0.0
 */
public class CmsResourceTypePlain extends A_CmsResourceType {

    /** Static type id. */
    private static int m_staticTypeId;

    /** The type id of this resource type. */
    @SuppressWarnings("unused")
    private static final int RESOURCE_TYPE_ID = 1;

    /** The name of this resource type. */
    private static final String RESOURCE_TYPE_NAME = "plain";

    /** JSP Loader instance. */
    protected CmsJspLoader m_jspLoader;

    /**
     * Default constructor, used to initialize member variables.<p>
     */
    public CmsResourceTypePlain() {

        super();
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

        Set<String> references = getReferencingStrongLinks(cms, resource);
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

        Set<String> references = getReferencingStrongLinks(cms, resource);
        super.deleteResource(cms, securityManager, resource, siblingMode);
        removeReferencingFromCache(references);
    }

    /**
     * A plain resource might appear as a sub-element in a JSP,
     * therefore it needs cache properties.<p>
     *
     * @see org.opencms.file.types.I_CmsResourceType#getCachePropertyDefault()
     */
    @Override
    public String getCachePropertyDefault() {

        return "always;";
    }

    /**
     * @see org.opencms.file.types.I_CmsResourceType#getLoaderId()
     */
    @Override
    public int getLoaderId() {

        return CmsDumpLoader.RESOURCE_LOADER_ID;
    }

    /**
     * @see org.opencms.file.types.A_CmsResourceType#initConfiguration(java.lang.String, java.lang.String, String)
     */
    @Override
    public void initConfiguration(String name, String id, String className) throws CmsConfigurationException {

        super.initConfiguration(name, id, className);
        if (name.equals(RESOURCE_TYPE_NAME)) {
            m_staticTypeId = m_typeId;
        }
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
    public void moveResource(
        CmsObject cms,
        CmsSecurityManager securityManager,
        CmsResource resource,
        String destination) throws CmsException, CmsIllegalArgumentException {

        Set<String> references = getReferencingStrongLinks(cms, resource);
        super.moveResource(cms, securityManager, resource, destination);
        removeReferencingFromCache(references);
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
        List<CmsProperty> properties) throws CmsException {

        Set<String> references = getReferencingStrongLinks(cms, resource);
        super.replaceResource(cms, securityManager, resource, type, content, properties);
        removeReferencingFromCache(references);
    }

    /**
     * @see org.opencms.file.types.A_CmsResourceType#restoreResource(org.opencms.file.CmsObject, org.opencms.db.CmsSecurityManager, org.opencms.file.CmsResource, int)
     */
    @Override
    public void restoreResource(CmsObject cms, CmsSecurityManager securityManager, CmsResource resource, int version)
    throws CmsException {

        Set<String> references = getReferencingStrongLinks(cms, resource);
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

        Set<String> references = getReferencingStrongLinks(cms, resource);
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

        Set<String> references = getReferencingStrongLinks(cms, resource);
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

        Set<String> references = getReferencingStrongLinks(cms, resource);
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

        Set<String> references = getReferencingStrongLinks(cms, resource);
        super.undoChanges(cms, securityManager, resource, mode);
        removeReferencingFromCache(references);
    }

    /**
     * @see org.opencms.file.types.A_CmsResourceType#writeFile(org.opencms.file.CmsObject, org.opencms.db.CmsSecurityManager, org.opencms.file.CmsFile)
     */
    @Override
    public CmsFile writeFile(CmsObject cms, CmsSecurityManager securityManager, CmsFile resource) throws CmsException {

        Set<String> references = getReferencingStrongLinks(cms, resource);
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
    protected Set<String> getReferencingStrongLinks(CmsObject cms, CmsResource resource) throws CmsException {

        Set<String> references = new HashSet<String>();
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
    protected void removeReferencingFromCache(Set<String> references) {

        if (m_jspLoader != null) {
            m_jspLoader.removeFromCache(references, false);
        }
    }
}