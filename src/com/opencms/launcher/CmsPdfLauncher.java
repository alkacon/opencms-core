/*
* File   : $Source: /alkacon/cvs/opencms/src/com/opencms/launcher/Attic/CmsPdfLauncher.java,v $
* Date   : $Date: 2002/03/07 13:42:40 $
* Version: $Revision: 1.10 $
*
* This library is part of OpenCms -
* the Open Source Content Mananagement System
*
* Copyright (C) 2001  The OpenCms Group
*
* This library is free software; you can redistribute it and/or
* modify it under the terms of the GNU Lesser General Public
* License as published by the Free Software Foundation; either
* version 2.1 of the License, or (at your option) any later version.
*
* This library is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
* Lesser General Public License for more details.
*
* For further information about OpenCms, please see the
* OpenCms Website: http://www.opencms.org
*
* You should have received a copy of the GNU Lesser General Public
* License along with this library; if not, write to the Free Software
* Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
*/

package com.opencms.launcher;

import com.opencms.template.*;
import com.opencms.file.*;
import com.opencms.core.*;
import org.apache.fop.apps.Driver;
import org.apache.fop.apps.Version;
import org.w3c.dom.*;
import org.xml.sax.*;
import org.apache.xerces.parsers.SAXParser;
import java.io.*;

/**
 * OpenCms launcher class for XML templates.
 * This can be used generating PDF output for XML body files using XML template and
 * subtemplate technology. The template data has to be XSL:FO coded.
 * <P><B>Remark:</B><BR>
 * The PDF rendering is based on the Formatting Objects Printer (FOP) from the Apache
 * XML project. For supported features see http://xml.apache.org/fop/.
 * <P>
 * The selected body should define a start template class using <BR> <CODE>
 * &lt;PAGE&gt;<BR>
 * &nbsp;&nbsp;&lt;CLASS&gt;...&lt;/CLASS&gt;<BR>
 * &lt;/PAGE&gt;</CODE><P>
 *
 * If no start template is defined, the class given by the parameters
 * will be used.
 * <P>
 * If even this is not defined, CmsXmlTemplate will
 * be used to create output.
 *
 * @author Matthias Schreiber
 * @version $Revision: 1.10 $ $Date: 2002/03/07 13:42:40 $
 */
public class CmsPdfLauncher extends CmsXmlLauncher {

    /**
     * Creates a SAX parser, using the value of org.xml.sax.parser
     * defaulting to org.apache.xerces.parsers.SAXParser.
     *
     * @return the created SAX parser
     */
    private static Parser createParser() {
        String parserClassName = System.getProperty("org.xml.sax.parser");
        if(parserClassName == null) {
            parserClassName = "org.apache.xerces.parsers.SAXParser";
        }
        if(I_CmsLogChannels.C_PREPROCESSOR_IS_LOGGING && A_OpenCms.isLogging() ) {
            A_OpenCms.log(C_OPENCMS_INFO, "[CmsPdfLauncher] Using SAX parser: " + parserClassName);
        }
        try {
            return (Parser)Class.forName(parserClassName).newInstance();
        }
        catch(ClassNotFoundException e) {
            if(I_CmsLogChannels.C_PREPROCESSOR_IS_LOGGING && A_OpenCms.isLogging() ) {
                A_OpenCms.log(C_OPENCMS_CRITICAL, "[CmsPdfLauncher] Could not find " + parserClassName);
            }
        }
        catch(InstantiationException e) {
            if(I_CmsLogChannels.C_PREPROCESSOR_IS_LOGGING && A_OpenCms.isLogging() ) {
                A_OpenCms.log(C_OPENCMS_CRITICAL, "[CmsPdfLauncher] Could not instantiate " + parserClassName);
            }
        }
        catch(IllegalAccessException e) {
            if(I_CmsLogChannels.C_PREPROCESSOR_IS_LOGGING && A_OpenCms.isLogging() ) {
                A_OpenCms.log(C_OPENCMS_CRITICAL, "[CmsPdfLauncher] Could not access " + parserClassName);
            }
        }
        catch(ClassCastException e) {
            if(I_CmsLogChannels.C_PREPROCESSOR_IS_LOGGING && A_OpenCms.isLogging() ) {
                A_OpenCms.log(C_OPENCMS_CRITICAL, "[CmsPdfLauncher] " + parserClassName + " is not a SAX driver");
            }
        }
        return null;
    }

    /**
     * Gets the ID that indicates the type of the launcher.
     * @return launcher ID
     */
    public int getLauncherId() {
        return C_TYPE_PDF;
    }

    /**
     * Launches a PDF file by writing a byte array to the HttpServletResponse output stream.
     * Calls the canonical root with the appropriate template class.
     *
     * @param cms A_CmsObject Object for accessing system resources
     * @param file CmsFile Object with the selected resource to be shown
     * @param startTemplateClass Name of the template class to start with.
     * @exception CmsException
     */
    protected void launch(CmsObject cms, CmsFile file, String startTemplateClass, A_OpenCms openCms) throws CmsException {

        // get the CmsRequest
        I_CmsRequest req = cms.getRequestContext().getRequest();
        byte[] templatedata = null;
        byte[] result = null;

        // get the complete data from all templates
        templatedata = generateOutput(cms, file, startTemplateClass, req, openCms);

        // Set PDF Version and initialize SAX Parser
        String version = Version.getVersion();
        Parser parser = createParser();
        InputStream in = new ByteArrayInputStream(templatedata);
        InputSource source = new InputSource(in);
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        // Start the FOP PDF Renderer and try to generate PDF file.
        try {
            Driver driver = new Driver();
            driver.setRenderer("org.apache.fop.render.pdf.PDFRenderer", version);
            driver.addElementMapping("org.apache.fop.fo.StandardElementMapping");
            driver.addElementMapping("org.apache.fop.svg.SVGElementMapping");
            driver.setWriter(new PrintWriter(out));
            driver.buildFOTree(parser, source);
            driver.format();
            driver.render();
        }
        catch(Exception e) {
            if(I_CmsLogChannels.C_PREPROCESSOR_IS_LOGGING && A_OpenCms.isLogging() ) {
                A_OpenCms.log(C_OPENCMS_CRITICAL, "[CmsPdfLauncher] PDF-Renderer Error: " + e.getMessage());
            }
        }
        result = out.toByteArray();

        // Write out PDF file if available.
        if(result != null) {
            writeBytesToResponse(cms, result);
        }
    }
}
