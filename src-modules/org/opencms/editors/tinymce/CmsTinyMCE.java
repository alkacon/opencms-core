
package org.opencms.editors.tinymce;

import org.opencms.jsp.CmsJspActionElement;
import org.opencms.util.CmsHtmlConverter;
import org.opencms.util.CmsStringUtil;
import org.opencms.workplace.CmsWorkplaceSettings;
import org.opencms.workplace.editors.CmsSimplePageEditor;

import javax.servlet.http.HttpServletRequest;

/**
 * The tinyMCE based editor.<p>
 */
public class CmsTinyMCE extends CmsSimplePageEditor {

    /** String constant separator for button groups. */
    public static final String GROUP_SEPARATOR = "|";

    /** Suffix for the style file that is added to the used template  styles file name. */
    public static final String SUFFIX_STYLE = "_style";

    /** Constant for the editor type, must be the same as the editors sub folder name in the VFS. */
    private static final String EDITOR_TYPE = "tinymce";

    /**
     * Public constructor.<p>
     * 
     * @param jsp an initialized JSP action element
     */
    public CmsTinyMCE(CmsJspActionElement jsp) {

        super(jsp);
    }

    /**
     * Builds toolbar javascript file for TinyMCE.<p>
     * 
     * @param buttonString button names and block separators delimited by comma
     * 
     * @return returns the toolbar buttons
     */
    public static String buildToolbar(String buttonString) {

        StringBuilder toolbar = new StringBuilder();
        String[] buttons = buttonString.split("\\,");

        String button;
        for (int i = 0; i < buttons.length; i++) {
            button = buttons[i];
            toolbar.append(button + " ");
        }
        return toolbar.toString();
    }

    /**
     * @see org.opencms.workplace.editors.CmsEditor#getEditorResourceUri()
     */
    @Override
    public String getEditorResourceUri() {

        return getSkinUri() + "editors/" + EDITOR_TYPE + "/jscripts/tinymce/";
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
                // by default we want to pretty-print and XHTML format when saving the content in TinyMCE
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
