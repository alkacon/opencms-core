package org.opencms.editors.tinymce;

import org.opencms.jsp.CmsJspActionElement;
import org.opencms.util.CmsHtmlConverter;
import org.opencms.util.CmsStringUtil;
import org.opencms.workplace.editors.CmsSimplePageEditor;

public class CmsTinyMCE extends CmsSimplePageEditor{
	
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
     * Build toolbar Javascript file for TinyMCE
     * @param buttonString button names and block separators delimited by coma
     * @return Javascript for the toolbar
     */
    public static String buildToolbar(String buttonString){
    	StringBuilder toolbar = new StringBuilder() ;
    	String[] buttons = buttonString.split("\\,") ;
    	
    	String button ;
    	boolean firstGroup = true;
    	int rowNum = 1 ;
    	for(int i=0; i < buttons.length; i++){
    		button = buttons[i] ;
    		if(button.equals(GROUP_SEPARATOR)){
    			if(!firstGroup){
    				toolbar.append("\",\n");
    				rowNum++;
    			}
    			firstGroup = false;
    			toolbar.append("theme_advanced_buttons" + rowNum + " : \""+button) ;
    			
    		} else {
    			toolbar.append(","+button) ;
    		}
    	}
    	
    	// close the last row/block
    	toolbar.append("\",\n");
    	
    	return toolbar.toString() ;
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
