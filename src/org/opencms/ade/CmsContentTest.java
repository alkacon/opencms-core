/*
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (c) Alkacon Software GmbH & Co. KG (https://www.alkacon.com)
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
 * company website: https://www.alkacon.com
 *
 * For further information about OpenCms, please see the
 * project website: https://www.opencms.org
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package org.opencms.ade;

import org.opencms.db.CmsModificationContext;
import org.opencms.file.CmsObject;
import org.opencms.file.types.I_CmsResourceType;
import org.opencms.jsp.CmsJspActionElement;
import org.opencms.main.CmsLog;
import org.opencms.main.OpenCms;
import org.opencms.relations.CmsCategoryService;
import org.opencms.util.CmsStringUtil;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.jsp.PageContext;

import org.apache.commons.logging.Log;

public class CmsContentTest extends CmsJspActionElement {

    private static final String BASE_FOLDER = "/shared/online/test";
    private static final Log LOG = CmsLog.getLog(CmsContentTest.class);

    public CmsContentTest(PageContext pageContext, HttpServletRequest request, HttpServletResponse response) {

        super(pageContext, request, response);
    }

    public void run(String name) throws Exception {

        int count = 500;
        long start = System.currentTimeMillis();
        try {

            CmsObject cms = getCmsObject();
            cms.lockResourceTemporary(BASE_FOLDER);
            String folder = CmsStringUtil.joinPaths(BASE_FOLDER, name);
            try {

                I_CmsResourceType type = OpenCms.getResourceManager().getResourceType("m-section");
                String contentTemplate = "";
                try (InputStream stream = getClass().getResourceAsStream("example.xml")) {
                    contentTemplate = new String(contentTemplate.getBytes(), StandardCharsets.UTF_8);
                }
                final String finalTemplate = contentTemplate;
                cms.createResource(folder, OpenCms.getResourceManager().getResourceType("folder"));
                for (int i = 0; i < count; i++) {
                    final int finalI = i;
                    CmsModificationContext.doWithModificationContext(cms.getRequestContext(), () -> {
                        byte[] content = finalTemplate.replace("NUMBER", "" + finalI).getBytes(StandardCharsets.UTF_8);
                        String path = CmsStringUtil.joinPaths(folder, "content_" + finalI + ".xml");
                        cms.createResource(path, type, content, new ArrayList<>());
                        CmsCategoryService.getInstance().addResourceToCategory(cms, path, "foo");
                        CmsCategoryService.getInstance().addResourceToCategory(cms, path, "bar");
                        return null;
                    });
                }
                OpenCms.getPublishManager().publishProject(cms);
                OpenCms.getPublishManager().waitWhileRunning();
            } finally {
                cms.unlockResource(BASE_FOLDER);
            }

        } catch (Exception e) {
            LOG.error(e.getLocalizedMessage(), e);
        } finally {
            long end = System.currentTimeMillis();
            System.out.println("DURATION: " + (end - start));
        }

    }

}
