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
 * For further information about Alkacon Software GmbH & Co. KG, please see the
 * company website: http://www.alkacon.com
 *
 * For further information about OpenCms, please see the
 * project website: http://www.opencms.org
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package org.opencms.setup.comptest;

import com.alkacon.simapi.RenderSettings;
import com.alkacon.simapi.Simapi;

import org.opencms.loader.CmsImageScaler;
import org.opencms.setup.CmsSetupBean;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.Iterator;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.ImageWriter;

/**
 * Tests the image processing capabilities.<p>
 *
 * @since 6.1.8
 */
public class CmsSetupTestSimapi implements I_CmsSetupTest {

    /** The test name. */
    public static final String TEST_NAME = "Image Processing";

    /**
     * @see org.opencms.setup.comptest.I_CmsSetupTest#execute(org.opencms.setup.CmsSetupBean)
     */
    public CmsSetupTestResult execute(CmsSetupBean setupBean) {

        CmsSetupTestResult testResult = new CmsSetupTestResult(this);
        boolean ok = true;
        Throwable ex = null;
        try {
            RenderSettings settings = new RenderSettings(Simapi.RENDER_QUALITY);
            settings.setCompressionQuality(0.85f);
            Simapi simapi = new Simapi(settings);

            ImageIO.scanForPlugins();
            Iterator<ImageReader> pngReaders = ImageIO.getImageReadersByFormatName(Simapi.TYPE_PNG);
            if (!pngReaders.hasNext()) {
                throw (new Exception("No Java ImageIO readers for the PNG format are available."));
            }
            Iterator<ImageWriter> pngWriters = ImageIO.getImageWritersByFormatName(Simapi.TYPE_PNG);
            if (!pngWriters.hasNext()) {
                throw (new Exception("No Java ImageIO writers for the PNG format are available."));
            }

            String basePath = setupBean.getWebAppRfsPath();
            if (!basePath.endsWith(File.separator)) {
                basePath += File.separator;
            }
            basePath += "setup" + File.separator + "resources" + File.separator;

            CmsImageScaler scaler = new CmsImageScaler();
            byte[] scaled;
            BufferedImage result;

            BufferedImage source = Simapi.read(basePath + "test1.png");
            String targetName = basePath + "test3.png";
            scaler.parseParameters("w:50,h:18");
            scaled = scaler.scaleImage(simapi.getBytes(source, Simapi.TYPE_PNG), targetName);
            writeFile(targetName, scaled);
            result = Simapi.read(targetName);

            BufferedImage expected = Simapi.read(basePath + "test2.png");

            ok = Arrays.equals(simapi.getBytes(expected, Simapi.TYPE_PNG), simapi.getBytes(result, Simapi.TYPE_PNG));
        } catch (Throwable e) {
            ok = false;
            ex = e;
        }

        if (ok) {
            testResult.setResult(RESULT_PASSED);
            testResult.setGreen();
        } else {
            testResult.setYellow();
            if (ex != null) {
                testResult.setResult(RESULT_FAILED);
                testResult.setHelp(ex.toString());
                testResult.setInfo(
                    "<p><code>-Djava.awt.headless=true</code> JVM parameter or X-Server may be missing.<br>"
                        + "<b>You can continue the setup, but image processing will be disabled.</b></p>");
            } else {
                testResult.setResult(RESULT_WARNING);
                testResult.setHelp("Image processing works but result does not exactly match.");
                StringBuffer info = new StringBuffer();
                info.append("<p>Please check the following images for visible differences:</p>");
                info.append("<table width='100%'>");
                info.append("<tr><th>Expected</th><th>Result</th></tr>");
                info.append("<tr><td align='center' width='50%'><img src='resources/test2.png'></td>");
                info.append("<td align='center' width='50%'><img src='resources/test3.png'></td></table>");
                info.append(
                    "<p><b>You can continue the setup, but image processing may not always work as expected.</b></p>");
                testResult.setInfo(info.toString());
            }
        }
        return testResult;
    }

    /**
     * @see org.opencms.setup.comptest.I_CmsSetupTest#getName()
     */
    public String getName() {

        return TEST_NAME;
    }

    private byte[] readFile(File file) throws IOException {

        // create input and output stream
        FileInputStream in = new FileInputStream(file);
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        // read the file content
        int c;
        while ((c = in.read()) != -1) {
            out.write(c);
        }

        in.close();
        out.close();

        return out.toByteArray();
    }

    private File writeFile(String rfsName, byte[] content) throws IOException {

        File f = new File(rfsName);
        File p = f.getParentFile();
        if (!p.exists()) {
            // create parent folders
            p.mkdirs();
        }
        // write file contents
        FileOutputStream fs = new FileOutputStream(f);
        fs.write(content);
        fs.close();
        return f;
    }
}
