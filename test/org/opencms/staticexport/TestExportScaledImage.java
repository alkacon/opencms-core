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

import org.opencms.file.CmsFile;
import org.opencms.file.CmsObject;
import org.opencms.loader.CmsImageScaler;
import org.opencms.main.OpenCms;
import org.opencms.test.OpenCmsTestRunner;
import org.opencms.util.CmsFileUtil;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Arrays;
import java.util.Objects;

import javax.servlet.ServletOutputStream;
import javax.servlet.WriteListener;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;

/**
 * @since 6.0.0
 */
public class TestExportScaledImage extends OpenCmsTestRunner {

    /**
     * @see org.opencms.test.OpenCmsTestRunner#$openCmsSetUp(org.junit.jupiter.api.TestInfo)
     */
    @Override
    @BeforeAll
    public void $openCmsSetUp(TestInfo testInfo) {

        setupOpenCms(testInfo, "simpletest", "/", "../org/opencms/staticexport/");
    }

    /**
     * Tests the file export.<p>
     *
     * @throws Throwable if something goes wrong
     */
    @Test
    public void testExportScaledImage() throws Throwable {

        CmsObject cms = getCmsObject();
        echo("Testing export of scaled image");

        // set the export mode to export immediately after publishing resources
        // OpenCms.getStaticExportManager().setHandler("org.opencms.staticexport.CmsAfterPublishStaticExportHandler");
        String resourcename = "/folder1/image1.gif";
        String scaleParams = "cx:5,cy:5,ch:10,cw:10,t:0,h:40,w:40,transparent";

        CmsFile imageFile = cms.readFile(resourcename);
        // now read the exported file in the file system and check its content
        String rootPath = cms.getRequestContext().addSiteRoot(resourcename);
        String exportPath = CmsFileUtil.normalizePath(
            OpenCms.getStaticExportManager().getExportPath(rootPath) + rootPath);

        CmsStaticExportData data = new CmsStaticExportData(
            rootPath,
            rootPath,
            imageFile,
            CmsImageScaler.PARAM_SCALE + "=" + scaleParams);
        CmsObject exportCms = OpenCms.initCmsObject(OpenCms.getDefaultUsers().getUserExport());
        // Request and response are provided only with information needed to get scaling running at all.
        HttpServletRequest testRequest = (HttpServletRequest)Proxy.newProxyInstance(
            getClass().getClassLoader(),
            new Class[] {HttpServletRequest.class},
            new InvocationHandler() {

                @Override
                public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {

                    if (method.getName().equals("getParameter")) {
                        if (Objects.equals(CmsImageScaler.PARAM_SCALE, args[0])) {
                            return scaleParams;
                        }
                        return null;
                    } else {
                        if (method.getReturnType() == int.class) {
                            return Integer.valueOf(0);
                        } else if (method.getReturnType() == long.class) {
                            return Long.valueOf(0);
                        } else if (method.getReturnType() == boolean.class) {
                            return Boolean.FALSE;
                        }
                        return null;
                    }
                }
            });
        HttpServletResponse testResponse = (HttpServletResponse)Proxy.newProxyInstance(
            getClass().getClassLoader(),
            new Class[] {HttpServletResponse.class},
            new InvocationHandler() {

                @Override
                public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {

                    if (method.getName().equals("getOutputStream")) {
                        // Returning dummy output stream, since we do not want to actually write somewhere in the output stream
                        return new ServletOutputStream() {

                            @Override
                            public boolean isReady() {

                                // Returning default, since the method is never called in the test case.
                                return false;
                            }

                            @Override
                            public void setWriteListener(WriteListener writeListener) {

                                // Returning default, since the method is never called in the test case.

                            }

                            @Override
                            public void write(int b) throws IOException {

                                // Returning default, since the method is never called in the test case.

                            }
                        };
                    } else {
                        if (method.getReturnType() == int.class) {
                            return Integer.valueOf(0);
                        } else if (method.getReturnType() == long.class) {
                            return Long.valueOf(0);
                        } else if (method.getReturnType() == boolean.class) {
                            return Boolean.FALSE;
                        }
                        return null;
                    }
                }
            });

        OpenCms.getStaticExportManager().export(testRequest, testResponse, exportCms, data);

        File f = new File(exportPath);
        assertTrue(f.exists());

        byte[] expectedContent = (new CmsImageScaler(scaleParams)).scaleImage(cms.readFile(resourcename));
        // check the exported content
        byte[] exportContent = new byte[(int)f.length()];
        FileInputStream fileStream = new FileInputStream(f);
        fileStream.read(exportContent);
        fileStream.close();
        assertEquals(Arrays.toString(expectedContent), Arrays.toString(exportContent));
    }
}
