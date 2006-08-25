/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/file/types/A_CmsResourceTypeLinkParseable.java,v $
 * Date   : $Date: 2006/08/25 12:22:55 $
 * Version: $Revision: 1.1.2.1 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Mananagement System
 *
 * Copyright (c) 2005 Alkacon Software GmbH (http://www.alkacon.com)
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

import org.opencms.db.CmsSecurityManager;
import org.opencms.file.CmsFile;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsResource;
import org.opencms.file.CmsVfsException;
import org.opencms.main.CmsException;
import org.opencms.main.CmsIllegalArgumentException;
import org.opencms.main.CmsLog;
import org.opencms.relations.CmsRelationFilter;
import org.opencms.relations.I_CmsLinkParseable;
import org.opencms.security.CmsSecurityException;

import java.util.List;

import org.apache.commons.logging.Log;

/**
 * Base implementation for resource types implementing the {@link I_CmsLinkParseable} interface.<p>
 * 
 * @author Michael Moossen
 * 
 * @version $Revision: 1.1.2.1 $ 
 * 
 * @since 6.5.0 
 */
public abstract class A_CmsResourceTypeLinkParseable extends A_CmsResourceType implements I_CmsLinkParseable {

    /**
     * Default constructor.<p>
     */
    public A_CmsResourceTypeLinkParseable() {

        super();
    }

    /**
     * @see org.opencms.file.types.I_CmsResourceType#copyResource(org.opencms.file.CmsObject, CmsSecurityManager, CmsResource, java.lang.String, int)
     */
    public void copyResource(
        CmsObject cms,
        CmsSecurityManager securityManager,
        CmsResource source,
        String destination,
        int siblingMode) throws CmsException {

        super.copyResource(cms, securityManager, source, destination, siblingMode);
        createRelations(cms, securityManager, cms.getRequestContext().addSiteRoot(destination));
    }

    /**
     * @see org.opencms.file.types.I_CmsResourceType#createResource(org.opencms.file.CmsObject, CmsSecurityManager, java.lang.String, byte[], List)
     */
    public CmsResource createResource(
        CmsObject cms,
        CmsSecurityManager securityManager,
        String resourcename,
        byte[] content,
        List properties) throws CmsException {

        CmsResource resource = super.createResource(cms, securityManager, resourcename, content, properties);
        createRelations(cms, securityManager, cms.getRequestContext().addSiteRoot(resourcename));
        return resource;
    }

    /**
     * @see org.opencms.file.types.I_CmsResourceType#createSibling(org.opencms.file.CmsObject, org.opencms.db.CmsSecurityManager, CmsResource, java.lang.String, java.util.List)
     */
    public void createSibling(
        CmsObject cms,
        CmsSecurityManager securityManager,
        CmsResource source,
        String destination,
        List properties) throws CmsException {

        super.createSibling(cms, securityManager, source, destination, properties);
        createRelations(cms, securityManager, cms.getRequestContext().addSiteRoot(destination));
    }

    /**
     * @see org.opencms.file.types.I_CmsResourceType#deleteResource(org.opencms.file.CmsObject, CmsSecurityManager, CmsResource, int)
     */
    public void deleteResource(CmsObject cms, CmsSecurityManager securityManager, CmsResource resource, int siblingMode)
    throws CmsException {

        super.deleteResource(cms, securityManager, resource, siblingMode);
        securityManager.deleteRelationsForResource(cms.getRequestContext(), resource, CmsRelationFilter.TARGETS);
    }

    /**
     * @see org.opencms.file.types.I_CmsResourceType#importResource(org.opencms.file.CmsObject, CmsSecurityManager, java.lang.String, org.opencms.file.CmsResource, byte[], List)
     */
    public CmsResource importResource(
        CmsObject cms,
        CmsSecurityManager securityManager,
        String resourcename,
        CmsResource resource,
        byte[] content,
        List properties) throws CmsException {

        CmsResource importedResource = super.importResource(
            cms,
            securityManager,
            resourcename,
            resource,
            content,
            properties);
        try {
            // this may fail if the xsd is not jet imported
            createRelations(cms, securityManager, importedResource.getRootPath());
        } catch (Exception e) {
            if (LOG.isDebugEnabled()) {
                LOG.debug(Messages.get().getBundle().key(
                    Messages.LOG_RELATION_CREATION_FAILED_1,
                    importedResource.getRootPath()));
            }
            // ignore, the import handler has to take care of this
            // see org.opencms.importexport.CmsImportVersion4/5#rewriteParseables
        }
        return importedResource;
    }

    /** The log object for this class. */
    private static final Log LOG = CmsLog.getLog(A_CmsResourceType.class);

    /**
     * @see org.opencms.file.types.I_CmsResourceType#isDirectEditable()
     */
    public boolean isDirectEditable() {

        return true;
    }

    /**
     * @see org.opencms.file.types.I_CmsResourceType#moveResource(org.opencms.file.CmsObject, org.opencms.db.CmsSecurityManager, org.opencms.file.CmsResource, java.lang.String)
     */
    public void moveResource(CmsObject cms, CmsSecurityManager securityManager, CmsResource resource, String destination)
    throws CmsException, CmsIllegalArgumentException {

        super.moveResource(cms, securityManager, resource, destination);
        updateRelations(cms, securityManager, resource, cms.getRequestContext().addSiteRoot(destination));
    }

    /**
     * @see org.opencms.file.types.I_CmsResourceType#writeFile(org.opencms.file.CmsObject, CmsSecurityManager, CmsFile)
     */
    public CmsFile writeFile(CmsObject cms, CmsSecurityManager securityManager, CmsFile resource)
    throws CmsException, CmsVfsException, CmsSecurityException {

        CmsFile file = super.writeFile(cms, securityManager, resource);
        // update the relations after creating!!
        securityManager.updateRelationsForResource(cms.getRequestContext(), file, parseLinks(cms, file));

        return file;
    }
}