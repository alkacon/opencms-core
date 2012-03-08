/*
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (c) Alkacon Software GmbH (http://www.alkacon.com)
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

package org.opencms.ade.upload;

import org.opencms.ade.upload.shared.CmsUploadData;
import org.opencms.ade.upload.shared.I_CmsUploadConstants;
import org.opencms.ade.upload.shared.rpc.I_CmsUploadService;
import org.opencms.gwt.CmsGwtActionElement;
import org.opencms.jsp.CmsJspActionElement;
import org.opencms.util.CmsStringUtil;
import org.opencms.workplace.CmsDialog;
import org.opencms.workplace.CmsWorkplace;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.jsp.PageContext;

/**
 * Upload action element, used to generate the upload dialog.<p>
 * 
 * @since 8.0.0
 */
public class CmsUploadActionElement extends CmsGwtActionElement {

    /**
     * A CMS dialog that makes the {@link Dialog#computeCurrentFolder} method visible.<p>
     */
    private class Dialog extends CmsDialog {

        /**
         * Constructor.<p>
         * 
         * @param jsp the JSP Action Element
         */
        public Dialog(CmsJspActionElement jsp) {

            super(jsp);

        }

        /**
         * @see org.opencms.workplace.CmsDialog#computeCurrentFolder()
         */
        @Override
        public String computeCurrentFolder() {

            return super.computeCurrentFolder();
        }
    }

    /** The module name. */
    public static final String MODULE_NAME = "upload";

    /**
     * Constructor.<p>
     * 
     * @param context the JSP page context object
     * @param req the JSP request 
     * @param res the JSP response 
     */
    public CmsUploadActionElement(PageContext context, HttpServletRequest req, HttpServletResponse res) {

        super(context, req, res);
    }

    /**
     * @see org.opencms.gwt.CmsGwtActionElement#export()
     */
    @Override
    public String export() throws Exception {

        StringBuffer sb = new StringBuffer();
        String prefetchedData = serialize(I_CmsUploadService.class.getMethod("prefetch"), getUploadData());
        sb.append(CmsUploadData.DICT_NAME).append("='").append(prefetchedData).append("';");
        sb.append(ClientMessages.get().export(getRequest()));
        wrapScript(sb);
        return sb.toString();
    }

    /**
     * @see org.opencms.gwt.CmsGwtActionElement#exportAll()
     */
    @Override
    public String exportAll() throws Exception {

        StringBuffer sb = new StringBuffer();
        sb.append(super.export());
        sb.append(export());
        sb.append(exportTargetFolder());
        sb.append(exportCloseLink());
        sb.append(createNoCacheScript(MODULE_NAME));
        return sb.toString();
    }

    /**
     * Special export for the button mode.<p>
     * 
     * @return the data
     * 
     * @throws Exception if something goes wrong
     */
    public String exportButton() throws Exception {

        StringBuffer sb = new StringBuffer();
        sb.append(exportAll());
        sb.append(exportDialogMode());
        return sb.toString();
    }

    /**
     * Returns the upload dialog title.<p>
     * 
     * @return the upload dialog title
     */
    public String getTitle() {

        return Messages.get().getBundle(getWorkplaceLocale()).key(Messages.GUI_UPLOAD_TITLE_0);
    }

    /**
     * Returns the needed server data for client-side usage.<p> 
     *
     * @return the needed server data for client-side usage
     */
    public CmsUploadData getUploadData() {

        return CmsUploadService.newInstance(getRequest()).prefetch();
    }

    /**
     * Returns a javascript tag that contains a variable deceleration that has the close link as value.<p>
     * 
     * @return a javascript tag that contains a variable deceleration that has the close link as value
     */
    private String exportCloseLink() {

        String closeLink = null;
        if (getRequest().getAttribute(I_CmsUploadConstants.ATTR_CLOSE_LINK) != null) {
            closeLink = (String)getRequest().getAttribute(I_CmsUploadConstants.ATTR_CLOSE_LINK);
        }
        if (CmsStringUtil.isEmptyOrWhitespaceOnly(closeLink)) {
            closeLink = CmsWorkplace.FILE_EXPLORER_FILELIST;
        }

        StringBuffer sb = new StringBuffer();
        // var closeLink = '/system/workplace/views/explorer/explorer_files.jsp';
        sb.append("var ").append(I_CmsUploadConstants.ATTR_CLOSE_LINK).append(" = \'").append(closeLink).append("\';");
        wrapScript(sb);
        return sb.toString();
    }

    /**
     * Returns a javascript tag that contains a variable deceleration that has the close link as value.<p>
     * 
     * @return a javascript tag that contains a variable deceleration that has the close link as value
     */
    private String exportDialogMode() {

        // var dialogMode = 'button';
        StringBuffer sb = new StringBuffer("var " + I_CmsUploadConstants.ATTR_DIALOG_MODE + " = 'button';");
        wrapScript(sb);
        return sb.toString();
    }

    /**
     * Returns a javascript tag that contains a variable deceleration that has the target folder as value.<p>
     * 
     * @return a javascript tag that contains a variable deceleration that has the target folder as value
     */
    private String exportTargetFolder() {

        String targetFolder = null;
        if (getRequest().getAttribute(I_CmsUploadConstants.ATTR_CURRENT_FOLDER) != null) {
            targetFolder = (String)getRequest().getAttribute(I_CmsUploadConstants.ATTR_CURRENT_FOLDER);
        }
        if (CmsStringUtil.isEmptyOrWhitespaceOnly(targetFolder)) {
            targetFolder = new Dialog(this).computeCurrentFolder();
        }
        StringBuffer sb = new StringBuffer();
        // var targetFolder = '/demo_t3/';
        sb.append("var ").append(I_CmsUploadConstants.VAR_TARGET_FOLDER).append(" = \'").append(targetFolder).append(
            "\';");
        wrapScript(sb);
        return sb.toString();
    }
}
