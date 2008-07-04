/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/file/types/CmsResourceTypeJsp.java,v $
 * Date   : $Date: 2008/07/04 15:21:25 $
 * Version: $Revision: 1.31 $
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
import org.opencms.file.CmsResource;
import org.opencms.file.CmsResourceFilter;
import org.opencms.file.CmsResource.CmsResourceDeleteMode;
import org.opencms.file.CmsResource.CmsResourceUndoMode;
import org.opencms.i18n.CmsEncoder;
import org.opencms.i18n.CmsLocaleManager;
import org.opencms.jsp.util.CmsJspLinkMacroResolver;
import org.opencms.loader.CmsJspLoader;
import org.opencms.main.CmsException;
import org.opencms.main.CmsIllegalArgumentException;
import org.opencms.main.CmsLog;
import org.opencms.main.OpenCms;
import org.opencms.relations.CmsRelation;
import org.opencms.relations.CmsRelationFilter;
import org.opencms.relations.CmsRelationType;
import org.opencms.util.CmsFileUtil;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.commons.logging.Log;

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
 * @version $Revision: 1.31 $ 
 * 
 * @since 6.0.0 
 */
public class CmsResourceTypeJsp extends A_CmsResourceTypeLinkParseable {

    /** The log object for this class. */
    private static final Log LOG = CmsLog.getLog(CmsResourceTypeJsp.class);

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
     * Deletes all given jsp files from RFS.<p>
     * 
     * @param rootPaths the root paths of the jsp files to delete
     * @param online if online or offline
     * 
     * @throws CmsException if something goes wrong
     */
    public static void deleteReferencingStrongLinks(Set rootPaths, boolean online) throws CmsException {

        Iterator it = rootPaths.iterator();
        while (it.hasNext()) {
            String rootPath = (String)it.next();
            String extension = CmsJspLoader.JSP_EXTENSION;
            if (rootPath.endsWith(extension)) {
                extension = "";
            }
            String jspPath = CmsFileUtil.getRepositoryName(
                CmsJspLoader.getJspRepository(),
                rootPath + extension,
                online);
            File f = new File(jspPath);
            if (f.exists()) {
                if (!f.delete()) {
                    if (LOG.isWarnEnabled()) {
                        LOG.warn(Messages.get().getBundle().key(
                            Messages.ERR_TYPEJSP_CANNOT_DELETE_1,
                            f.getAbsolutePath()));
                    }
                }
            }
        }
    }

    /**
     * Returns a set of root paths of jsp files that are including the given resource using the 'link.strong' macro.<p>
     * 
     * @param cms the current cms context
     * @param resource the current updated jsp file
     * 
     * @return the set of referencing paths
     * 
     * @throws CmsException if something goes wrong
     */
    public static Set getReferencingStrongLinks(CmsObject cms, CmsResource resource) throws CmsException {

        Set ret = new HashSet();
        getReferencingStrongLinks(cms, resource, ret);
        return ret;
    }

    /**
     * Returns a set of root paths of jsp files that are including the given resource using the 'link.strong' macro.<p>
     * 
     * @param cms the current cms context
     * @param resource the current updated jsp file
     * @param referencingPaths the set of already referencing paths, return parameter
     * 
     * @throws CmsException if something goes wrong
     */
    public static void getReferencingStrongLinks(CmsObject cms, CmsResource resource, Set referencingPaths)
    throws CmsException {

        CmsRelationFilter filter = CmsRelationFilter.SOURCES.filterType(CmsRelationType.JSP_STRONG);
        Iterator it = cms.getRelationsForResource(resource, filter).iterator();
        while (it.hasNext()) {
            CmsRelation relation = (CmsRelation)it.next();
            CmsResource source = relation.getSource(cms, CmsResourceFilter.DEFAULT);
            // check if page was already updated
            if ((source.getTypeId() != CmsResourceTypeJsp.getStaticTypeId())
                || referencingPaths.contains(source.getRootPath())) {
                // no need to delete the including file from the real FS more than once
                continue;
            }
            referencingPaths.add(source.getRootPath());
            getReferencingStrongLinks(cms, source, referencingPaths);
        }
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
    public void chtype(CmsObject cms, CmsSecurityManager securityManager, CmsResource resource, int type)
    throws CmsException {

        Set includingFiles = getReferencingStrongLinks(cms, resource);
        super.chtype(cms, securityManager, resource, type);
        deleteReferencingStrongLinks(includingFiles, false);
    }

    /**
     * @see org.opencms.file.types.A_CmsResourceType#deleteResource(org.opencms.file.CmsObject, org.opencms.db.CmsSecurityManager, org.opencms.file.CmsResource, org.opencms.file.CmsResource.CmsResourceDeleteMode)
     */
    public void deleteResource(
        CmsObject cms,
        CmsSecurityManager securityManager,
        CmsResource resource,
        CmsResourceDeleteMode siblingMode) throws CmsException {

        Set includingFiles = getReferencingStrongLinks(cms, resource);
        super.deleteResource(cms, securityManager, resource, siblingMode);
        deleteReferencingStrongLinks(includingFiles, false);
    }

    /**
     * @see org.opencms.file.types.I_CmsResourceType#getLoaderId()
     */
    public int getLoaderId() {

        return CmsJspLoader.RESOURCE_LOADER_ID;
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

        Set includingFiles = getReferencingStrongLinks(cms, resource);
        super.moveResource(cms, securityManager, resource, destination);
        deleteReferencingStrongLinks(includingFiles, false);
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

        Set includingFiles = getReferencingStrongLinks(cms, resource);
        super.replaceResource(cms, securityManager, resource, type, content, properties);
        deleteReferencingStrongLinks(includingFiles, false);
    }

    /**
     * @see org.opencms.file.types.A_CmsResourceType#restoreResource(org.opencms.file.CmsObject, org.opencms.db.CmsSecurityManager, org.opencms.file.CmsResource, int)
     */
    public void restoreResource(CmsObject cms, CmsSecurityManager securityManager, CmsResource resource, int version)
    throws CmsException {

        Set includingFiles = getReferencingStrongLinks(cms, resource);
        super.restoreResource(cms, securityManager, resource, version);
        deleteReferencingStrongLinks(includingFiles, false);
    }

    /**
     * @see org.opencms.file.types.A_CmsResourceType#undoChanges(org.opencms.file.CmsObject, org.opencms.db.CmsSecurityManager, org.opencms.file.CmsResource, org.opencms.file.CmsResource.CmsResourceUndoMode)
     */
    public void undoChanges(
        CmsObject cms,
        CmsSecurityManager securityManager,
        CmsResource resource,
        CmsResourceUndoMode mode) throws CmsException {

        Set includingFiles = getReferencingStrongLinks(cms, resource);
        super.undoChanges(cms, securityManager, resource, mode);
        deleteReferencingStrongLinks(includingFiles, false);
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
        Set includingFiles = getReferencingStrongLinks(cms, resource);
        CmsFile file = super.writeFile(cms, securityManager, resource);
        deleteReferencingStrongLinks(includingFiles, false);
        return file;
    }
}
