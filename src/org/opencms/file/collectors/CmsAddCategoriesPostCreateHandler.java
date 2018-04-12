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

package org.opencms.file.collectors;

import org.opencms.file.CmsObject;
import org.opencms.file.CmsResource;
import org.opencms.lock.CmsLockUtil;
import org.opencms.main.CmsException;
import org.opencms.relations.CmsCategory;
import org.opencms.relations.CmsCategoryService;

import java.util.Arrays;
import java.util.List;

/**
 * A post create handler that adds categories to newly created resources (that are not a copy of an existing resource).<p>
 *
 * To configure it for adding the categories with category paths "cat1/subcat1/", "cat1/subcat2/" and "cat2/"
 * add the attribute
 *  <code>postCreateHandler="org.opencms.file.collectors.CmsAddCategoriesPostCreateHandler|cat1/subcat1/,cat1/subcat2/,cat2/"</code>
 * to the tag that provides the create option (i.e., <cms:contentload>, <cms:edit> or <cms:display>.<p>
 *
 * Instead of providing category paths, one can also use site or root paths of the folders representing the categories.
 * If a category path starts with "/" it is assumed to be site or root path. Otherwise it is treated as category path, i.e.,
 * the path without the category repositories base path.<p>
 *
 * @see I_CmsCollectorPostCreateHandler for more information on where post create handlers can be configured.
 */
public class CmsAddCategoriesPostCreateHandler implements I_CmsCollectorPostCreateHandler {

    /**
     * @see org.opencms.file.collectors.I_CmsCollectorPostCreateHandler#onCreate(org.opencms.file.CmsObject, org.opencms.file.CmsResource, boolean)
     */
    public void onCreate(CmsObject cms, CmsResource createdResource, boolean copyMode) {

        // there are no categories configured that should be added. Just return without modifying the created resource.
        return;

    }

    /**
     * Adds the categories specified via <code>config</code> to the newly created resource iff not in copy mode.
     *
     * @param cms the current user's CMS context
     * @param createdResource the resource which has been created
     * @param copyMode <code>true</code> if the user chose one of the elements in the collector list as a model
     * @param config a comma separted list of category, site or root paths that specify the categories to be added to the created resource
     *        if <code>copyMode</code> is <code>false</code>.
     *
     * @see org.opencms.file.collectors.I_CmsCollectorPostCreateHandler#onCreate(org.opencms.file.CmsObject, org.opencms.file.CmsResource, boolean, java.lang.String)
     */
    public void onCreate(CmsObject cms, CmsResource createdResource, boolean copyMode, String config) {

        if ((null != config) && !copyMode) {
            List<String> cats = Arrays.asList(config.split(","));
            CmsCategoryService catService = CmsCategoryService.getInstance();
            try {
                try (AutoCloseable c = CmsLockUtil.withLockedResources(cms, createdResource)) {
                    String sitePath = cms.getRequestContext().getSitePath(createdResource);
                    for (String catPath : cats) {
                        if (!catPath.isEmpty()) {
                            try {
                                CmsCategory cat;
                                if (catPath.startsWith("/")) { // assume we have a site or rootpath
                                    cat = catService.getCategory(cms, catPath);
                                } else { // assume we have the category path
                                    cat = catService.readCategory(cms, catPath, sitePath);
                                }
                                if (null != cat) {
                                    catService.addResourceToCategory(cms, sitePath, cat);
                                }
                            } catch (CmsException e) {
                                // TODO Auto-generated catch block
                                e.printStackTrace();
                            }
                        }
                    }
                }
            } catch (Exception e1) {
                // TODO Auto-generated catch block
                e1.printStackTrace();
            }
        }
    }

}
