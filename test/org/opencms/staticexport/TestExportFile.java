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
 * For further information about Alkacon Software GmbH & Co. KG, please see the
 * company website: https://www.alkacon.com
 *
 * For further information about OpenCms, please see the
 * project website: https://www.opencms.org
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package org.opencms.staticexport;

import org.opencms.file.CmsObject;
import org.opencms.file.CmsProperty;
import org.opencms.file.CmsPropertyDefinition;
import org.opencms.file.CmsResource;
import org.opencms.file.types.CmsResourceTypeJsp;
import org.opencms.file.types.CmsResourceTypePlain;
import org.opencms.main.OpenCms;
import org.opencms.test.OpenCmsTestRunner;
import org.opencms.util.CmsFileUtil;

import java.io.File;
import java.io.FileInputStream;
import java.util.Collections;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;

import jakarta.servlet.http.HttpServletResponse;

/**
 * @since 6.0.0
 */
public class TestExportFile extends OpenCmsTestRunner {

    @Override
    @BeforeAll
    public void $openCmsSetUp(TestInfo testInfo) {

        setupOpenCms(testInfo);
    }

    /**
     * Tests the file export.<p>
     *
     * @throws Throwable if something goes wrong
     */
    @Test
    public void testStaticexportFile() throws Throwable {

        CmsObject cms = getCmsObject();
        echo("Testing file export");

        OpenCms.getStaticExportManager().setHandler("org.opencms.staticexport.CmsAfterPublishStaticExportHandler");

        String resourcename = "/file1.txt";
        String content = "this is a test content";

        cms.createResource(resourcename, CmsResourceTypePlain.getStaticTypeId(), content.getBytes(), null);
        cms.unlockResource(resourcename);

        assertContent(cms, resourcename, content.getBytes());

        OpenCms.getPublishManager().publishProject(cms);
        OpenCms.getPublishManager().waitWhileRunning();

        String rootPath = cms.getRequestContext().addSiteRoot(resourcename);
        String exportPath = CmsFileUtil.normalizePath(
            OpenCms.getStaticExportManager().getExportPath(rootPath) + rootPath);
        File f = new File(exportPath);
        assertTrue(f.exists());

        byte[] exportContent = new byte[(int)f.length()];
        FileInputStream fileStream = new FileInputStream(f);
        fileStream.read(exportContent);
        fileStream.close();

        assertContent(cms, resourcename, exportContent);
    }

    /**
     * Tests that trailing dots cannot bypass the JSP filename check during static export.<p>
     *
     * @throws Throwable if something goes wrong
     */
    @Test
    public void testStaticExportRejectsJspNamesWithTrailingDots() throws Throwable {

        checkExportFileNames(
            CmsResourceTypePlain.getStaticTypeId(),
            false,
            "/blocked.jsp",
            "/blocked.jsp.",
            "/blocked.jsp..",
            "/blocked.JsP...",
            "/blocked.jspx...");
        checkExportFileNames(
            OpenCms.getResourceManager().getResourceType(CmsResourceTypeJsp.getStaticTypeName()).getTypeId(),
            false,
            "/blocked-jsp-type.jsp.",
            "/blocked-jsp-type.jspx...");
        checkExportFileNames(
            CmsResourceTypePlain.getStaticTypeId(),
            true,
            "/allowed.txt",
            "/allowed.jsp.txt",
            "/allowed.jsp...txt");
    }

    /**
     * Checks whether files with the given names are written by static export.<p>
     *
     * @param resourceType the resource type
     * @param exportable whether the files should be exported
     * @param names the file names
     * @throws Throwable if something goes wrong
     */
    private void checkExportFileNames(int resourceType, boolean exportable, String... names) throws Throwable {

        CmsObject cms = getCmsObject();
        byte[] content = "static export filename test".getBytes();
        for (String name : names) {
            cms.createResource(
                name,
                resourceType,
                content,
                Collections.singletonList(new CmsProperty(CmsPropertyDefinition.PROPERTY_EXPORT, "true", null)));
            cms.unlockResource(name);
        }
        OpenCms.getPublishManager().publishProject(cms);
        OpenCms.getPublishManager().waitWhileRunning();

        CmsObject exportCms = OpenCms.initCmsObject(OpenCms.getDefaultUsers().getUserExport());
        exportCms.getRequestContext().setCurrentProject(exportCms.readProject("Online"));
        CmsStaticExportManager manager = OpenCms.getStaticExportManager();
        for (String name : names) {
            String rootPath = cms.getRequestContext().addSiteRoot(name);
            CmsResource resource = exportCms.readResource(rootPath);
            CmsStaticExportData data = new CmsStaticExportData(rootPath, rootPath, resource, null);
            int status = manager.export(null, null, exportCms, data);
            File exportFile = new File(CmsFileUtil.normalizePath(manager.getExportPath(rootPath) + rootPath));
            assertEquals(exportable, exportFile.exists(), "Unexpected export file for " + name);
            assertEquals(
                exportable ? HttpServletResponse.SC_OK : HttpServletResponse.SC_SEE_OTHER,
                status,
                "Unexpected export status for " + name);
        }
    }
}
