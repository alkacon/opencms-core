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

package org.opencms.ui.actions;

import org.opencms.file.CmsObject;
import org.opencms.file.CmsResource;
import org.opencms.main.CmsException;
import org.opencms.main.CmsLog;
import org.opencms.main.OpenCms;
import org.opencms.ui.I_CmsDialogContext;
import org.opencms.ui.contextmenu.CmsMenuItemVisibilityMode;
import org.opencms.ui.contextmenu.CmsStandardVisibilityCheck;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.logging.Log;

/**
 * Class representing an abstract gallery dialog action.<p>
 */
public abstract class A_CmsGalleryDialogAction extends A_CmsWorkplaceAction {

    /** Logger instance for this class. */
    static final Log LOG = CmsLog.getLog(A_CmsGalleryDialogAction.class);

    /**
     * @see org.opencms.ui.contextmenu.I_CmsHasMenuItemVisibility#getVisibility(org.opencms.file.CmsObject, java.util.List)
     */
    public CmsMenuItemVisibilityMode getVisibility(CmsObject cms, List<CmsResource> resources) {

        if (resources.size() == 1) {
            CmsResource resource = resources.get(0);
            if (resource.isFolder()) {
                String type = OpenCms.getResourceManager().getResourceType(resource).getTypeName();
                if (Arrays.asList(getSupportedGalleryTypes()).contains(type)) {
                    return CmsStandardVisibilityCheck.VISIBLE.getVisibility(cms, resources);
                }
            } else {
                CmsResource parentGallery = getParentGallery(cms, resource);
                if (parentGallery != null) {
                    List<CmsResource> parentList = new ArrayList<CmsResource>();
                    parentList.add(parentGallery);
                    return getVisibility(cms, parentList);
                }
            }
        }
        return CmsMenuItemVisibilityMode.VISIBILITY_INVISIBLE;
    }

    /**
     * Returns the gallery.<p>
     *
     * @param context the dialog context
     * @return the gallery
     */
    protected CmsResource getGallery(I_CmsDialogContext context) {

        CmsResource resource = context.getResources().get(0);
        return resource.isFolder() ? resource : getParentGallery(context.getCms(), resource);
    }

    /**
     * Returns the gallery types supported by this dialog action.
     *
     * @return the gallery types
     */
    abstract protected String[] getSupportedGalleryTypes();

    /**
     * If the context of this gallery dialog action is a gallery item, returns the parent gallery.<p>
     *
     * @param cms the CMS object
     * @param resource the resource that has opened the context menu
     * @return the parent gallery
     */
    private CmsResource getParentGallery(CmsObject cms, CmsResource resource) {

        String parentFolder = CmsResource.getParentFolder(resource.getRootPath());
        parentFolder = cms.getRequestContext().removeSiteRoot(parentFolder);
        CmsResource parentGallery = null;
        try {
            parentGallery = cms.readResource(parentFolder);
        } catch (CmsException e) {
            LOG.error(e.getLocalizedMessage(), e);
        }
        return parentGallery;
    }
}
