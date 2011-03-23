/*
 * File   : $Source: /alkacon/cvs/opencms/src-modules/org/opencms/editors/fckeditor/CmsFCKEditor.java,v $
 * Date   : $Date: 2011/03/23 14:53:09 $
 * Version: $Revision: 1.12 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (c) 2002 - 2011 Alkacon Software GmbH (http://www.alkacon.com)
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
 * For further information about Alkacon Software GmbH, please see the
 * company website: http://www.alkacon.com
 *
 * For further information about OpenCms, please see the
 * project website: http://www.opencms.org
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package org.opencms.editors.fckeditor;

import org.opencms.jsp.CmsJspActionElement;
import org.opencms.util.CmsHtmlConverter;
import org.opencms.util.CmsStringUtil;
import org.opencms.workplace.CmsWorkplaceSettings;
import org.opencms.workplace.editors.CmsSimplePageEditor;

import javax.servlet.http.HttpServletRequest;

/**
 * Creates the output for editing a page with the open source FCKeditor editor.<p>
 * 
 * This editor supports user defined styles. To show these styles, a plain text file containing the style definition
 * XML code has to be placed in the same folder where the template CSS style sheet is located.<br>
 * The file name has to be exactly like the file name of the CSS with the suffix <code>_style.xml</code> added. 
 * E.g. for the CSS file <code>style.css</code> the style definition file 
 * has to be named <code>style.css_style.xml</code>.<p>
 * 
 * An example for a style XML can be found 
 * in the VFS file <code>/system/workplace/resources/editors/fckeditor/fckstyles.xml</code>.<p>
 * 
 * The following editor uses this class:
 * <ul>
 * <li>/editors/fckeditor/editor.jsp
 * </ul>
 * <p>
 *
 * @author  Andreas Zahner 
 * 
 * @version $Revision: 1.12 $ 
 * 
 * @since 6.1.7
 */
public class CmsFCKEditor extends CmsSimplePageEditor {

    /** Suffix for the style XML file that is added to the used template CSS style sheet file name. */
    public static final String SUFFIX_STYLESXML = "_style.xml";

    /** Constant for the editor type, must be the same as the editors sub folder name in the VFS. */
    private static final String EDITOR_TYPE = "fckeditor";

    /**
     * Public constructor.<p>
     * 
     * @param jsp an initialized JSP action element
     */
    public CmsFCKEditor(CmsJspActionElement jsp) {

        super(jsp);
    }

    /**
     * @see org.opencms.workplace.editors.CmsEditor#getEditorResourceUri()
     */
    @Override
    public String getEditorResourceUri() {

        return getSkinUri() + "editors/" + EDITOR_TYPE + "/";
    }

    /**
     * @see org.opencms.workplace.CmsWorkplace#initWorkplaceRequestValues(org.opencms.workplace.CmsWorkplaceSettings, javax.servlet.http.HttpServletRequest)
     */
    @Override
    protected void initWorkplaceRequestValues(CmsWorkplaceSettings settings, HttpServletRequest request) {

        if (CmsStringUtil.isNotEmpty(request.getParameter(PARAM_RESOURCE))) {
            super.initWorkplaceRequestValues(settings, request);
        }
    }

    /**
     * @see org.opencms.workplace.editors.CmsSimplePageEditor#prepareContent(boolean)
     */
    @Override
    protected String prepareContent(boolean save) {

        if (save) {
            String conversionSetting = CmsHtmlConverter.getConversionSettings(getCms(), m_file);
            if (CmsStringUtil.isEmptyOrWhitespaceOnly(conversionSetting)) {
                // by default we want to pretty-print and Xhtml format when saving the content in FCKeditor
                String content = getParamContent();
                CmsHtmlConverter converter = new CmsHtmlConverter(getEncoding(), CmsHtmlConverter.PARAM_XHTML);
                content = converter.convertToStringSilent(content);
                setParamContent(content);
            }
        }
        // do further processing with super class
        return super.prepareContent(true);
    }
}