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

package org.opencms.editors.codemirror;

import org.opencms.file.CmsResource;
import org.opencms.file.types.CmsResourceTypeJsp;
import org.opencms.file.types.CmsResourceTypeXmlContent;
import org.opencms.file.types.CmsResourceTypeXmlPage;
import org.opencms.jsp.CmsJspActionElement;
import org.opencms.main.CmsException;
import org.opencms.main.OpenCms;
import org.opencms.util.CmsStringUtil;
import org.opencms.workplace.editors.CmsSimpleEditor;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;

/**
 * Provides helper methods for the usage of the CodeMirror editor that can be used
 * for syntax highlighting of text based files.<p>
 */
public class CmsCodeMirror extends CmsSimpleEditor {

    /** Syntax highlight type name: CSS. */
    public static final String HIGHLIGHT_TYPE_CSS = "css";

    /** Syntax highlight type name: HTML. */
    public static final String HIGHLIGHT_TYPE_HTML = "html";

    /** Syntax highlight type name: JavaScript. */
    public static final String HIGHLIGHT_TYPE_JAVASCRIPT = "js";

    /** Syntax highlight type name: JSP. */
    public static final String HIGHLIGHT_TYPE_JSP = "jsp";

    /** Syntax highlight type name: XML. */
    public static final String HIGHLIGHT_TYPE_XML = "xml";

    /** Request parameter name for the close function parameter. */
    public static final String PARAM_CLOSEFUNCTION = "closefunction";

    /** Possible type suffix names. */
    protected static final String[] HIGHLIGHT_TYPES = {
        HIGHLIGHT_TYPE_CSS,
        HIGHLIGHT_TYPE_HTML,
        HIGHLIGHT_TYPE_JAVASCRIPT,
        HIGHLIGHT_TYPE_JSP,
        HIGHLIGHT_TYPE_XML};

    /** Possible type suffix names as list. */
    protected static final List<String> HIGHLIGHT_TYPES_LIST = Arrays.asList(HIGHLIGHT_TYPES);

    /** Sub path fragment to the editor resources. */
    protected static final String SUBPATH_CODEMIRROR = "editors/codemirror/";

    /** Path to the editor resources. */
    protected static final String VFS_PATH_EDITOR = CmsStringUtil.joinPaths(
        OpenCms.getSystemInfo().getStaticResourceContext(),
        SUBPATH_CODEMIRROR);

    /** Path to the editor distribution resources. */
    protected static final String VFS_PATH_EDITOR_DIST = VFS_PATH_EDITOR + "dist/";

    /** The close function parameter. */
    private String m_paramCloseFunction;

    /**
     * Public constructor.<p>
     *
     * @param jsp an initialized JSP action element
     */
    public CmsCodeMirror(CmsJspActionElement jsp) {

        super(jsp);
    }

    /**
     * Returns the editor language to use according to the current users workplace settings.<p>
     *
     * @return the editor language to use
     */
    public String getEditorLanguage() {

        String testLocale = getLocale().toString();
        if (getCms().existsResource(VFS_PATH_EDITOR + "js/lang-" + testLocale + ".js")) {
            return testLocale;
        }
        return Locale.ENGLISH.toString();
    }

    /**
     * @see org.opencms.workplace.editors.CmsSimpleEditor#getEditorResourceUri()
     */
    @Override
    public String getEditorResourceUri() {

        return CmsStringUtil.joinPaths(OpenCms.getSystemInfo().getStaticResourceContext(), SUBPATH_CODEMIRROR);
    }

    /**
     * Returns the syntax highlighting type for the currently edited resource.<p>
     *
     * @return the syntax highlighting type
     */
    public String getHighlightMode() {

        // read edited resource
        CmsResource resource = null;
        try {
            resource = getCms().readResource(getParamResource());
        } catch (CmsException e) {
            // ignore
        }

        if (resource != null) {
            // determine resource type
            int type = resource.getTypeId();
            if (CmsResourceTypeJsp.isJspTypeId(type)) {
                // JSP file
                return HIGHLIGHT_TYPE_JSP;
            }
            if (CmsResourceTypeXmlContent.isXmlContent(resource) || CmsResourceTypeXmlPage.isXmlPage(resource)) {
                // XML content file or XML page file
                return HIGHLIGHT_TYPE_XML;
            }
            // all other files will be matched according to their suffix
            int dotIndex = getParamResource().lastIndexOf('.');
            if (dotIndex != -1) {
                String suffix = getParamResource().substring(dotIndex + 1);
                if (CmsStringUtil.isNotEmpty(suffix)) {
                    // there is a suffix, determine matching syntax highlighting
                    int typeIndex = HIGHLIGHT_TYPES_LIST.indexOf(suffix.toLowerCase());
                    if (typeIndex != -1) {
                        return HIGHLIGHT_TYPES_LIST.get(typeIndex);
                    }
                }
            }
        }
        // return HTML type as default
        return HIGHLIGHT_TYPE_HTML;
    }

    /**
     * Returns the close function parameter.<p>
     *
     * @return the close function parameter
     */
    public String getParamCloseFunction() {

        return m_paramCloseFunction;
    }

    /**
     * Sets the close function parameter.<p>
     *
     * @param closeFunction the close function parameter
     */
    public void setParamCloseFunction(String closeFunction) {

        m_paramCloseFunction = closeFunction;
    }

    /**
     * @see org.opencms.workplace.editors.CmsEditor#initMessages()
     */
    @Override
    protected void initMessages() {

        addMessages(Messages.get().getBundleName());
        // TODO: Auto-generated method stub
        super.initMessages();
    }
}
