/*
 * File   : $Source: /alkacon/cvs/opencms/src-modules/org/opencms/workplace/tools/workplace/rfsfile/Attic/CmsRfsFileDownloadServlet.java,v $
 * Date   : $Date: 2005/06/23 10:47:10 $
 * Version: $Revision: 1.5 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Mananagement System
 *
 * Copyright (c) 2005 Alkacon Software GmbH (http://www.alkacon.com)
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

package org.opencms.workplace.tools.workplace.rfsfile;

import org.opencms.flex.CmsFlexController;
import org.opencms.util.CmsStringUtil;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Helper <code>Servlet</code> that reads the 
 * <b>request parameter <i>fileName</i></b> for a valid RFS file name and 
 * pushes this file to the client browser where it triggers a download 
 * (popup-box with save dialog). <p>
 * 
 * @author  Achim Westermann 
 * 
 * @version $Revision: 1.5 $ 
 * 
 * @since 6.0.0 
 */
public final class CmsRfsFileDownloadServlet extends HttpServlet {

    /**
     * Default constructor for this stateless class. 
     */
    public CmsRfsFileDownloadServlet() {

        // nop
    }

    /**
     * Forwards the call to <code>{@link #doPost(HttpServletRequest, HttpServletResponse)}</code>.<p>
     * 
     * @param request Provided by the servlet container if this servlet is directly used from the container's servlet-mappings or 
     *                by the implicit jsp variable "request"
     * @param response Provided by the servlet container if this servlet is directly used from the container's servlet-mappings or 
     *                 by the implicit jsp variable "response"
     * 
     * @see javax.servlet.http.HttpServlet#doGet(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
     * @throws ServletException if the needed parameter 'filePath' cannot be found
     * @throws IOException if work related to the download process fails
     */
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {

        doPost(request, response);
    }

    /**
     * Pushes a file that has been specified by request parameter 
     * <em>"filePath"</em> to the client browser.<p> 
     * 
     * The browser will open a popup menu that offers a donwload even if the 
     * file type is known to the client's OS.<p>
     * 
     * Note that the <b>parameter "filePath"</b> is read from the 
     * given <code>{@link HttpServletRequest}</code> for the file name to serve.<p>
     * 
     * @param req Provided by the servlet container if this servlet is directly used from the container's servlet-mappings or 
     *                by the implicit jsp variable "request"
     * 
     * @param res Provided by the servlet container if this servlet is directly used from the container's servlet-mappings or 
     *                 by the implicit jsp variable "response"
     * 
     * @see javax.servlet.http.HttpServlet#doPost(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
     * 
     * @throws ServletException if the needed parameter 'filePath' cannot be found
     * @throws IOException if work related to the download process fails
     */
    public void doPost(HttpServletRequest req, HttpServletResponse res) throws IOException, ServletException {

        // find the file: 
        String fileToFind = req.getParameter("filePath");
        if (CmsStringUtil.isEmpty(fileToFind)) {
            throw new ServletException(Messages.get().key(Messages.ERR_DOWNLOAD_SERVLET_FILE_ARG_0));
        } else {

            File downloadFile = new File(fileToFind);
            res.setHeader("Content-Disposition", new StringBuffer("attachment; filename=\"").append(
                downloadFile.getName()).append("\"").toString());
            res.setContentLength((int)downloadFile.length());

            CmsFlexController controller = CmsFlexController.getController(req);
            controller.getTopResponse().setContentType("application/download");

            InputStream in = null;
            ServletOutputStream outStream = null;

            // push the file:
            outStream = res.getOutputStream();
            in = new BufferedInputStream(new FileInputStream(downloadFile));

            try {
                // don't write the last '-1'
                int bit = in.read();
                while ((bit) >= 0) {
                    outStream.write(bit);
                    bit = in.read();
                }
            } catch (IOException ioe) {
                ioe.printStackTrace(System.out);
            }
            outStream.flush();
            outStream.close();
            in.close();
        }
    }

    /**
     * Dispatches to the single implemented method that deals with get and post requests.<p>
     *
     * @param request Provided by the servlet container if this servlet is directly used from the container's servlet-mappings or 
     *                by the implicit jsp variable "request"
     * 
     * @param response Provided by the servlet container if this servlet is directly used from the container's servlet-mappings or 
     *                 by the implicit jsp variable "response"
     * 
     * @see javax.servlet.http.HttpServlet#service(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
     * @throws ServletException if the needed parameter 'filePath' cannot be found
     * @throws IOException if work related to the download process fails
     */
    protected void service(HttpServletRequest request, HttpServletResponse response)
    throws ServletException, IOException {

        doPost(request, response);
    }
}
