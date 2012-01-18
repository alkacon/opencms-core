package org.opencms.editors.tinymce;

import org.opencms.jsp.CmsJspActionElement;
import org.opencms.util.CmsHtmlConverter;
import org.opencms.util.CmsStringUtil;
import org.opencms.workplace.editors.CmsSimplePageEditor;

public class CmsTinyMCE extends CmsSimplePageEditor{
	
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
     * @see org.opencms.workplace.editors.CmsEditor#getEditorResourceUri()
     */
    public String getEditorResourceUri() {

        return getSkinUri() + "editors/" + EDITOR_TYPE + "/jscripts/tiny_mce/";
    }
    
    /**
     * @see org.opencms.workplace.editors.CmsSimplePageEditor#prepareContent(boolean)
     */
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
