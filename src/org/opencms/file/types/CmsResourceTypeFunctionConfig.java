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

package org.opencms.file.types;

import org.opencms.ade.contenteditor.CmsContentService;
import org.opencms.db.CmsSecurityManager;
import org.opencms.file.CmsFile;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsResource;
import org.opencms.main.CmsException;
import org.opencms.main.OpenCms;
import org.opencms.util.CmsWaitHandle;

/**
 * Resource type class for the second version of dynamic functions.<p>
 */
public class CmsResourceTypeFunctionConfig extends CmsResourceTypeXmlAdeConfiguration {

    /** The path of the JSP used for rendering v2 functions. */
    public static final String FORMATTER_PATH = "/system/modules/org.opencms.base/formatters/function.jsp";

    /** The type name. */
    public static final String TYPE_NAME = "function_config";

    /** The serial version id. */
    private static final long serialVersionUID = -2378978201570511075L;

    /**
     * Returns the static type name of this (default) resource type.<p>
     *
     * @return the static type name of this (default) resource type
     */
    public static String getStaticTypeName() {

        return TYPE_NAME;
    }

    /**
     * Checks if a resource has this type.<p>
     *
     * @param res the resource to check
     * @return true if the resource is a V2 dynamic function
     */
    public static boolean isFunction(CmsResource res) {

        return OpenCms.getResourceManager().matchResourceType(TYPE_NAME, res.getTypeId());
    }

    /**
     * @see org.opencms.file.types.CmsResourceTypeXmlContent#getCachePropertyDefault()
     */
    @Override
    public String getCachePropertyDefault() {

        return null;
    }

    /**
     * @see org.opencms.file.types.CmsResourceTypeXmlContent#writeFile(org.opencms.file.CmsObject, org.opencms.db.CmsSecurityManager, org.opencms.file.CmsFile)
     *
     * After writing the file, this method waits until the formatter configuration is update the next time.
     */
    @Override
    public CmsFile writeFile(CmsObject cms, CmsSecurityManager securityManager, CmsFile resource) throws CmsException {

        String savingStr = (String)cms.getRequestContext().getAttribute(CmsContentService.ATTR_EDITOR_SAVING);
        CmsFile file = super.writeFile(cms, securityManager, resource);
        // Formatter configuration cache updates are asynchronous, but to be able to reload a container page
        // element in the page editor directly after editing it and having it reflect the changes made by the user
        // requires that we wait on a wait handle for the formatter cache.

        if (Boolean.valueOf(savingStr).booleanValue()) {
            CmsWaitHandle waitHandle = OpenCms.getADEManager().addFormatterCacheWaitHandle(false);
            waitHandle.enter(10000);
        }
        return file;
    }

}
