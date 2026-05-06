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
import org.opencms.file.types.CmsResourceTypePlain;
import org.opencms.main.OpenCms;
import org.opencms.test.OpenCmsJupiterTestCase;
import org.opencms.util.CmsFileUtil;

import java.io.File;
import java.io.FileInputStream;

import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestMethodOrder;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @since 6.0.0
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class TestExportFile extends OpenCmsJupiterTestCase {

    /**
     * @see org.opencms.test.OpenCmsJupiterTestCase#getPublish()
     */
    @Override
    protected boolean getPublish() {

        return true;
    }

    /**
     * @see org.opencms.test.OpenCmsJupiterTestCase#getImportFolder()
     */
    @Override
    protected String getImportFolder() {

        return null;
    }

    /**
     * @see org.opencms.test.OpenCmsJupiterTestCase#getTargetFolder()
     */
    @Override
    protected String getTargetFolder() {

        return null;
    }

    /**
     * Tests the file export.<p>
     *
     * @throws Throwable if something goes wrong
     */
    @Test
    @Order(1)
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
}
