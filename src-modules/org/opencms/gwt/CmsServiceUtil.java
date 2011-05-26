/*
 * File   : $Source: /alkacon/cvs/opencms/src-modules/org/opencms/gwt/Attic/CmsServiceUtil.java,v $
 * Date   : $Date: 2011/05/26 08:26:40 $
 * Version: $Revision: 1.2 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (C) 2002 - 2011 Alkacon Software (http://www.alkacon.com)
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

package org.opencms.gwt;

import org.opencms.file.CmsObject;
import org.opencms.file.CmsPropertyDefinition;
import org.opencms.file.CmsResource;
import org.opencms.gwt.shared.CmsListInfoBean;
import org.opencms.i18n.CmsMessages;
import org.opencms.loader.CmsLoaderException;
import org.opencms.main.CmsException;
import org.opencms.main.OpenCms;
import org.opencms.util.CmsStringUtil;

import java.util.Locale;

/**
 * A class which provides utility methods used in multiple services.<p>
 * 
 * @author Georg Westenberger
 * 
 * @version $Revision: 1.2 $
 * 
 * @since 8.0.0
 */
public final class CmsServiceUtil {

    /**
     * Private constructor to prevent instantiation.<p>
     */
    private CmsServiceUtil() {

        // do nothing
    }

    /**
     * Returns a bean to display the {@link org.opencms.gwt.client.ui.CmsListItemWidget}.<p>
     * 
     * @param cms the CMS context to use 
     * @param res the resource to get the page info for
     * 
     * @return a bean to display the {@link org.opencms.gwt.client.ui.CmsListItemWidget}.<p>
     * 
     * @throws CmsLoaderException if the resource type could not be found
     * @throws CmsException if something else goes wrong 
     */
    public static CmsListInfoBean getPageInfo(CmsObject cms, CmsResource res) throws CmsException, CmsLoaderException {

        CmsListInfoBean result = new CmsListInfoBean();

        result.setResourceState(res.getState());

        String resourceSitePath = cms.getRequestContext().removeSiteRoot(res.getRootPath());

        String title = cms.readPropertyObject(res, CmsPropertyDefinition.PROPERTY_TITLE, false).getValue();
        if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(title)) {
            result.setTitle(title);
        } else {
            result.setTitle(res.getName());
        }
        result.setSubTitle(resourceSitePath);
        String secure = cms.readPropertyObject(res, CmsPropertyDefinition.PROPERTY_SECURE, true).getValue();
        if (Boolean.parseBoolean(secure)) {
            result.setPageIcon(CmsListInfoBean.PageIcon.secure);
        } else {
            String export = cms.readPropertyObject(res, CmsPropertyDefinition.PROPERTY_EXPORT, true).getValue();
            if (Boolean.parseBoolean(export)) {
                result.setPageIcon(CmsListInfoBean.PageIcon.export);
            } else {
                result.setPageIcon(CmsListInfoBean.PageIcon.standard);
            }
        }
        String resTypeName = OpenCms.getResourceManager().getResourceType(res.getTypeId()).getTypeName();
        String key = OpenCms.getWorkplaceManager().getExplorerTypeSetting(resTypeName).getKey();
        Locale currentLocale = cms.getRequestContext().getLocale();
        CmsMessages messages = OpenCms.getWorkplaceManager().getMessages(currentLocale);
        String resTypeNiceName = messages.key(key);
        result.addAdditionalInfo(messages.key(org.opencms.workplace.commons.Messages.GUI_LABEL_TYPE_0), resTypeNiceName);
        result.setResourceType(resTypeName);
        return result;
    }

}
