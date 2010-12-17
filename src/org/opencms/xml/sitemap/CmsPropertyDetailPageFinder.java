/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/xml/sitemap/Attic/CmsPropertyDetailPageFinder.java,v $
 * Date   : $Date: 2010/12/17 08:45:29 $
 * Version: $Revision: 1.1 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (C) 2002 - 2009 Alkacon Software (http://www.alkacon.com)
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

package org.opencms.xml.sitemap;

import org.opencms.file.CmsObject;
import org.opencms.file.CmsProperty;
import org.opencms.file.CmsPropertyDefinition;
import org.opencms.file.CmsResource;
import org.opencms.file.types.CmsResourceTypeXmlContent;
import org.opencms.main.CmsException;
import org.opencms.util.CmsStringUtil;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * A detail page finder which uses the ade.detailview property on a resource to determine the detail page.<p>
 * 
 * @author Georg Westenberger
 * 
 * @version $Revision: 1.1 $
 * 
 * @since 8.0.0
 */
public class CmsPropertyDetailPageFinder implements I_CmsDetailPageFinder {

    /**
     * @see org.opencms.xml.sitemap.I_CmsDetailPageFinder#getAllDetailPages(org.opencms.file.CmsObject, org.opencms.file.CmsResource)
     */
    public Collection<String> getAllDetailPages(CmsObject cms, CmsResource res) throws CmsException {

        String detailPage = getDetailPage(cms, res, null);
        List<String> result = new ArrayList<String>();
        if (detailPage != null) {
            result.add(detailPage);
        }
        return result;
    }

    /**
     * @see org.opencms.xml.sitemap.I_CmsDetailPageFinder#getDetailPage(org.opencms.file.CmsObject, org.opencms.file.CmsResource, java.lang.String)
     */
    public String getDetailPage(CmsObject cms, CmsResource res, String linkSource) throws CmsException {

        if (!CmsResourceTypeXmlContent.isXmlContent(res)) {
            // only xml contents can have detail pages 
            return null;
        }
        String result = null;
        CmsProperty detailViewProp = cms.readPropertyObject(
            res,
            CmsPropertyDefinition.PROPERTY_ADE_SITEMAP_DETAILVIEW,
            true);
        if (!detailViewProp.isNullProperty() && !CmsStringUtil.isEmptyOrWhitespaceOnly(detailViewProp.getValue())) {
            String detailView = detailViewProp.getValue();
            result = cms.getRequestContext().addSiteRoot(detailView);
        }
        return result;
    }

}
