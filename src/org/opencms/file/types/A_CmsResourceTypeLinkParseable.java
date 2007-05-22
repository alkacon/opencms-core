/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/file/types/A_CmsResourceTypeLinkParseable.java,v $
 * Date   : $Date: 2007/05/22 16:07:08 $
 * Version: $Revision: 1.1.2.8 $
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
import org.opencms.main.CmsException;
import org.opencms.main.CmsIllegalArgumentException;
import org.opencms.relations.CmsRelationFilter;
import org.opencms.relations.I_CmsLinkParseable;

import java.util.List;

/**
 * Base implementation for resource types implementing the {@link I_CmsLinkParseable} interface.<p>
 * 
 * @author Michael Moossen
 * 
 * @version $Revision: 1.1.2.8 $ 
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
     * @see org.opencms.file.types.I_CmsResourceType#copyResource(org.opencms.file.CmsObject, CmsSecurityManager, CmsResource, java.lang.String, CmsResource.CmsResourceCopyMode)
     */
    public void copyResource(
        CmsObject cms,
        CmsSecurityManager securityManager,
        CmsResource source,
        String destination,
        CmsResource.CmsResourceCopyMode siblingMode) throws CmsException {

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
    public CmsResource createSibling(
        CmsObject cms,
        CmsSecurityManager securityManager,
        CmsResource source,
        String destination,
        List properties) throws CmsException {

        CmsResource sibling = super.createSibling(cms, securityManager, source, destination, properties);
        createRelations(cms, securityManager, cms.getRequestContext().addSiteRoot(destination));
        return sibling;
    }

    /**
     * @see org.opencms.file.types.I_CmsResourceType#deleteResource(org.opencms.file.CmsObject, CmsSecurityManager, CmsResource, CmsResource.CmsResourceDeleteMode)
     */
    public void deleteResource(CmsObject cms, CmsSecurityManager securityManager, CmsResource resource, CmsResource.CmsResourceDeleteMode siblingMode)
    throws CmsException {

        // delete relation of sibling too if needed
        if (siblingMode == CmsResource.DELETE_PRESERVE_SIBLINGS) {
            securityManager.deleteRelationsForResource(cms.getRequestContext(), resource, CmsRelationFilter.TARGETS);
        } else {
            deleteRelationsWithSiblings(cms, securityManager, resource);
        }
        super.deleteResource(cms, securityManager, resource, siblingMode);
    }

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
    public CmsFile writeFile(CmsObject cms, CmsSecurityManager securityManager, CmsFile resource) throws CmsException {

        CmsFile file = super.writeFile(cms, securityManager, resource);
        // update the relations after writing!!
        securityManager.updateRelationsForResource(cms.getRequestContext(), file, parseLinks(cms, file));

        return file;
    }
}