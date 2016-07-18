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

package org.opencms.ui.components.fileselect;

import org.opencms.file.CmsObject;
import org.opencms.file.CmsResource;
import org.opencms.file.CmsResourceFilter;
import org.opencms.file.types.I_CmsResourceType;
import org.opencms.gwt.CmsIconUtil;
import org.opencms.jsp.CmsJspNavBuilder;
import org.opencms.jsp.CmsJspNavElement;
import org.opencms.main.CmsException;
import org.opencms.main.CmsLog;
import org.opencms.main.OpenCms;
import org.opencms.util.CmsUUID;
import org.opencms.workplace.CmsWorkplace;
import org.opencms.workplace.explorer.CmsExplorerTypeSettings;

import java.util.List;

import org.apache.commons.logging.Log;

import com.google.common.collect.Lists;
import com.vaadin.data.Item;

/**
 * The data container for the sitmeap folder selection tree.<p>
 */
public class CmsSitemapTreeContainer extends CmsResourceTreeContainer {

    /** The logger instance for this class. */
    private static final Log LOG = CmsLog.getLog(CmsSitemapTreeContainer.class);

    /** Serial version id. */
    private static final long serialVersionUID = 1L;

    /**
     * Default constructor.<p>
     */
    public CmsSitemapTreeContainer() {
        super();
    }

    /**
     * @see org.opencms.ui.components.fileselect.CmsResourceTreeContainer#getIcon(org.opencms.file.CmsObject, org.opencms.file.CmsResource)
     */
    @Override
    public String getIcon(CmsObject cms, CmsResource resource) {

        CmsResource defaultFile = null;
        List<CmsResource> resourcesForType = Lists.newArrayList();
        resourcesForType.add(resource);

        try {

            defaultFile = cms.readDefaultFile(resource, CmsResourceFilter.IGNORE_EXPIRATION);
            if (defaultFile != null) {
                resourcesForType.add(0, defaultFile);
            }
        } catch (Exception e) {
            // Shouldn't normally happen - readDefaultFile returns null instead of throwing an exception when it doesn't find a default file
            LOG.error(e.getLocalizedMessage(), e);
        }
        if (CmsJspNavBuilder.isNavLevelFolder(cms, resource)) {
            return CmsWorkplace.getResourceUri(CmsWorkplace.RES_PATH_FILETYPES + CmsIconUtil.ICON_NAV_LEVEL_BIG);
        }
        String result = null;
        for (CmsResource res : resourcesForType) {
            I_CmsResourceType type = OpenCms.getResourceManager().getResourceType(res);
            CmsExplorerTypeSettings settings = OpenCms.getWorkplaceManager().getExplorerTypeSetting(type.getTypeName());
            if (settings != null) {
                result = CmsWorkplace.RES_PATH_FILETYPES + settings.getBigIconIfAvailable();
                break;
            }
        }
        return CmsWorkplace.getResourceUri(result);
    }

    /**
     * @see org.opencms.ui.components.fileselect.CmsResourceTreeContainer#readTreeLevel(org.opencms.file.CmsObject, org.opencms.util.CmsUUID, org.opencms.file.CmsResourceFilter)
     */
    @Override
    public void readTreeLevel(CmsObject cms, CmsUUID parentId, CmsResourceFilter filter) {

        // TODO Auto-generated method stub
        super.readTreeLevel(cms, parentId, filter);
        sort(new Object[] {"HAS_NAV", "NAV_POS"}, new boolean[] {false, true});
    }

    /**
     * @see org.opencms.ui.components.fileselect.CmsResourceTreeContainer#defineProperties()
     */
    @Override
    protected void defineProperties() {

        super.defineProperties();
        addContainerProperty("HAS_NAV", Boolean.class, Boolean.FALSE);
        addContainerProperty("NAV_POS", Float.class, Integer.valueOf(Integer.MAX_VALUE));
        sort(new Object[] {"HAS_NAV", "NAV_POS"}, new boolean[] {false, true});
    }

    /**
     * @see org.opencms.ui.components.fileselect.CmsResourceTreeContainer#fillProperties(org.opencms.file.CmsObject, com.vaadin.data.Item, org.opencms.file.CmsResource, org.opencms.util.CmsUUID)
     */
    @Override
    protected void fillProperties(CmsObject cms, Item resourceItem, CmsResource resource, CmsUUID parentId) {

        // TODO Auto-generated method stub
        super.fillProperties(cms, resourceItem, resource, parentId);
        try {
            CmsObject rootCms = OpenCms.initCmsObject(cms);
            rootCms.getRequestContext().setSiteRoot("");
            CmsJspNavBuilder builder = new CmsJspNavBuilder(rootCms);
            CmsJspNavElement nav = builder.getNavigationForResource(resource.getRootPath());

            resourceItem.getItemProperty("HAS_NAV").setValue(Boolean.valueOf(nav.isInNavigation()));
            if (nav.isInNavigation()) {
                resourceItem.getItemProperty("NAV_POS").setValue(Float.valueOf(nav.getNavPosition()));
            }

        } catch (CmsException e) {
            LOG.error(e.getLocalizedMessage(), e);
        }

    }

    /**
     * @see org.opencms.ui.components.fileselect.CmsResourceTreeContainer#getName(org.opencms.file.CmsObject, org.opencms.file.CmsResource, org.opencms.util.CmsUUID)
     */
    @Override
    protected String getName(CmsObject cms, CmsResource resource, CmsUUID parentId) {

        String defaultResult = super.getName(cms, resource, parentId);
        try {
            CmsObject rootCms = OpenCms.initCmsObject(cms);
            rootCms.getRequestContext().setSiteRoot("");
            CmsJspNavBuilder builder = new CmsJspNavBuilder(rootCms);
            CmsJspNavElement nav = builder.getNavigationForResource(resource.getRootPath());
            String result = null;
            if (nav.isInNavigation() && (nav.getNavText() != null) && !nav.getNavText().contains("???")) {
                result = nav.getNavText() + " [" + defaultResult + "]";
            } else {
                result = defaultResult;
            }
            return result;
        } catch (CmsException e) {
            LOG.error(e.getLocalizedMessage(), e);
            return defaultResult;
        }
    }
}
