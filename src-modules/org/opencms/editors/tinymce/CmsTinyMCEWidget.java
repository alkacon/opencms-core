package org.opencms.editors.tinymce;

import org.opencms.file.CmsFile;
import org.opencms.file.CmsObject;
import org.opencms.i18n.CmsEncoder;
import org.opencms.main.CmsException;
import org.opencms.main.CmsLog;
import org.opencms.main.OpenCms;
import org.opencms.util.CmsStringUtil;
import org.opencms.widgets.A_CmsHtmlWidget;
import org.opencms.widgets.CmsHtmlWidgetOption;
import org.opencms.widgets.I_CmsWidget;
import org.opencms.widgets.I_CmsWidgetDialog;
import org.opencms.widgets.I_CmsWidgetParameter;
import org.opencms.workplace.CmsWorkplace;
import org.opencms.workplace.editors.I_CmsEditorCssHandler;
import org.opencms.xml.types.I_CmsXmlContentValue;

import java.io.UnsupportedEncodingException;
import java.util.Iterator;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;

public class CmsTinyMCEWidget extends A_CmsHtmlWidget{

	/** Request parameter name for the tool bar configuration parameter. */
    public static final String PARAM_CONFIGURATION = "config";
	
    /** The translation of the generic widget button names to TinyMCE specific button names. */
    public static final String BUTTON_TRANSLATION =
    		/* Row 1*/
    		"|newdocument:newdocument|bold:bold|italic:italic|underline:underline|strikethrough:strikethrough|alignleft:justifyleft"
    		+"|aligncenter:justifycenter|alignright:justifyright|justify:justifyfull|style:styleselect|formatselect:formatselect"
    		+"|fontselect:fontselect|fontsizeselect:fontsizeselect"
    		/* Row 2*/
    		+"|cut:cut|copy:copy|paste:paste|pastetext:pastetext|pasteword:pasteword|find:search|replace:replace|unorderedlist:bullist"
    		+"|orderedlist:numlist|outdent:outdent|indent:indent|blockquote:blockquote|undo:undo|redo:redo|editorlink:link|unlink:unlink"
    		+"|anchor:anchor|image:image|cleanup:cleanup|source:code|insertdate:insertdate|inserttime:inserttime|forecolor:forecolor|backcolor:backcolor"
    		/* Row 3*/
    		+"|table:tablecontrols|hr:hr|removeformat:removeformat|visualaid:visualaid|subscript:sub|superscript:sup|specialchar:charmap"
    		+"|emotions:emotions|spellcheck:iespell|media:media|advhr:advhr|print:print|ltr:ltr|rtl:rtl|fitwindow:fullscreen"
    		/* Row 4*/
    		+"|insertlayer:insertlayer|moveforward:moveforward|movebackward:movebackward|absolute:absolute|styleprops:styleprops|cite:cite"
    		+"|abbr:abbr|acronym:acronym|del:del|ins:ins|attribs:attribs|visualchars:visualchars|nonbreaking:nonbreaking|template:template"
    		+"|pagebreak:pagebreak|selectall:selectall|fullpage:fullpage|imagegallery:OcmsImageGallery|downloadgallery:OcmsDownloadGallery"
    		+"|linkgallery:OcmsLinkGallery|htmlgallery:OcmsHtmlGallery|tablegallery:OcmsTableGallery|link:oc-link";
    
    /** The map containing the translation of the generic widget button names to TinyMCE specific button names. */
    public static final Map<String, String> BUTTON_TRANSLATION_MAP = CmsStringUtil.splitAsMap(
        BUTTON_TRANSLATION,
        "|",
        ":");
    
    /* Map TinyMCE buttons to particular row in the toolbar. */
    private static final String[][] m_toolbarMap = {
    		/* Row 1*/
    		{"newdocument","bold","italic","underline","strikethrough","justifyleft","justifycenter","justifyright","justifyfull","styleselect"
    			,"formatselect","fontselect","fontsizeselect"},
    		/* Row 2*/
    		{"cut","copy","paste","pastetext","pasteword","search","replace","bullist","numlist","outdent","indent","blockquote","undo","redo"
    			,"link","unlink","anchor","image","cleanup","code","insertdate","inserttime","forecolor","backcolor"},
    		/* Row 3*/
    		{"tablecontrols","hr","removeformat","visualaid","sub","sup","charmap","emotions","iespell","media","advhr","print","ltr","rtl","fullscreen"},
    		/* Row 4*/
    		{"insertlayer","moveforward","movebackward","absolute","styleprops","cite","abbr","acronym","del","ins","attribs","visualchars","nonbreaking"
    			,"template","pagebreak","selectall","OcmsImageGallery","OcmsDownloadGallery","OcmsLinkGallery","OcmsHtmlGallery"
    			,"OcmsTableGallery","oc-link","fullpage"}
    } ;
    
    /** The log object for this class. */
    private static final Log LOG = CmsLog.getLog(org.opencms.editors.tinymce.CmsTinyMCEWidget.class);
    
	/**
     * Creates a new TinyMCE widget.<p>
     */
    public CmsTinyMCEWidget() {

        // empty constructor is required for class registration
        this("");
    }
    
    /**
     * Creates a new TinyMCE widget with the given configuration.<p>
     * 
     * @param configuration the configuration to use
     */
    public CmsTinyMCEWidget(CmsHtmlWidgetOption configuration) {

        super(configuration);
    }

    /**
     * Creates a new TinyMCE widget with the given configuration.<p>
     * 
     * @param configuration the configuration to use
     */
    public CmsTinyMCEWidget(String configuration) {

        super(configuration);
    }
    
    /**
     * @see org.opencms.widgets.I_CmsWidget#getDialogIncludes(org.opencms.file.CmsObject, org.opencms.widgets.I_CmsWidgetDialog)
     */
    @Override
    public String getDialogIncludes(CmsObject cms, I_CmsWidgetDialog widgetDialog) {

        StringBuilder result = new StringBuilder(128);
        // general TinyMCE JS
        result.append(getJSIncludeFile(CmsWorkplace.getSkinUri() + "editors/tinymce/jscripts/tiny_mce/tiny_mce.js"));
        result.append("\n");
        result.append(getJSIncludeFile(OpenCms.getLinkManager().substituteLinkForRootPath(
            cms,
            "/system/workplace/editors/tinymce/opencms_plugin.js")));
        result.append("\n");
        // special TinyMCE widget functions
        result.append(getJSIncludeFile(CmsWorkplace.getSkinUri() + "components/widgets/tinymce.js"));
        return result.toString();
    }
    
    /**
     * @see org.opencms.widgets.I_CmsWidget#getDialogWidget(org.opencms.file.CmsObject, org.opencms.widgets.I_CmsWidgetDialog, org.opencms.widgets.I_CmsWidgetParameter)
     */
	public String getDialogWidget(CmsObject cms, I_CmsWidgetDialog widgetDialog, I_CmsWidgetParameter param) {
		String id = param.getId();
        String value = param.getStringValue(cms);
        StringBuilder result = new StringBuilder();

        result.append("<td class=\"xmlTd\">");

        result.append("<textarea class=\"xmlInput maxwidth\" name=\"ta_");
        result.append(id);
        result.append("\" id=\"ta_");
        result.append(id);
        result.append("\" style=\"height: ");
        result.append(getHtmlWidgetOption().getEditorHeight());
        result.append(";\" rows=\"20\" cols=\"60\">");
        result.append(CmsEncoder.escapeXml(value));
        result.append("</textarea>");
        result.append("<input type=\"hidden\" name=\"");
        result.append(id);
        result.append("\" id=\"");
        result.append(id);
        result.append("\" value=\"");
        result.append(CmsEncoder.encode(value));
        result.append("\">");

        result.append("<script type=\"text/javascript\">\n");
        result.append("tinyMCE.init({\n");
        result.append("	// General options\n");
        result.append("	mode : \"exact\",\n");
        result.append("	elements : \"ta_"+id+"\",\n");
        result.append("	theme : \"advanced\",\n");
        result.append("	plugins : \"autolink,lists,pagebreak,style,layer,table,save,advhr,advimage,advlink,emotions,iespell,inlinepopups,insertdatetime,preview,media,searchreplace,print,contextmenu,paste,directionality,fullscreen,noneditable,visualchars,nonbreaking,xhtmlxtras,template,wordcount,advlist,autosave,-opencms");
        
        //check for fullpage mode
        if(getHtmlWidgetOption().isFullPage()){
        	// add fullpage plugin
        	result.append(",fullpage") ;
        }
        
        result.append("\",\n") ;
        
        result.append("	// Theme options\n");
        result.append(getToolbar()) ;
        
        result.append("	theme_advanced_toolbar_location : \"top\",\n");
        result.append("	theme_advanced_toolbar_align : \"left\",\n");
        result.append("	theme_advanced_statusbar_location : \"bottom\",\n");
        result.append("	theme_advanced_resizing : true,\n");
        
        // set CSS style sheet for current editor widget if configured
        boolean cssConfigured = false;
        String cssPath = "";
        if (getHtmlWidgetOption().useCss()) {
            cssPath = getHtmlWidgetOption().getCssPath();
            // set the CSS path to null (the created configuration String passed to JS will not include this path then)
            getHtmlWidgetOption().setCssPath(null);
            cssConfigured = true;
        } else if (OpenCms.getWorkplaceManager().getEditorCssHandlers().size() > 0) {
            Iterator<I_CmsEditorCssHandler> i = OpenCms.getWorkplaceManager().getEditorCssHandlers().iterator();
            try {
                // cast parameter to I_CmsXmlContentValue
                I_CmsXmlContentValue contentValue = (I_CmsXmlContentValue)param;
                // now extract the absolute path of the edited resource
                String editedResource = cms.getSitePath(contentValue.getDocument().getFile());
                while (i.hasNext()) {
                    I_CmsEditorCssHandler handler = i.next();
                    if (handler.matches(cms, editedResource)) {
                        cssPath = handler.getUriStyleSheet(cms, editedResource);
                        if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(cssPath)) {
                            cssConfigured = true;
                        }
                        break;
                    }
                }
            } catch (Exception e) {
                // ignore, CSS could not be set
            }
        }
        if (cssConfigured) {
            result.append("content_css : \"");
            result.append(OpenCms.getLinkManager().substituteLink(cms, cssPath));
            result.append("\",\n");
        }
        
        if(getHtmlWidgetOption().showStylesFormat()){
        	try {
        		CmsFile file = cms.readFile(getHtmlWidgetOption().getStylesFormatPath()) ;
        		String characterEncoding = OpenCms.getSystemInfo().getDefaultEncoding() ;
        		String formatSelect = "style_formats : " + new String(file.getContents(),characterEncoding) + ",\n";
        		result.append(formatSelect) ;
        	} catch(CmsException cmsException){
        		LOG.error("Can not open file:"+getHtmlWidgetOption().getStylesFormatPath(),cmsException);
        	} catch(UnsupportedEncodingException ex){
        		LOG.error(ex) ;
        	}
        }
        
        result.append("	// Drop lists for link/image/media/template dialogs\n");
        result.append("	template_external_list_url : \"lists/template_list.js\",\n");
        result.append("	external_link_list_url : \"lists/link_list.js\",\n");
        result.append("	external_image_list_url : \"lists/image_list.js\",\n");
        result.append("	media_external_list_url : \"lists/media_list.js\"\n");
        result.append("});\n");
        
        result.append("contentFields[contentFields.length] = document.getElementById(\"").append(id).append("\");\n");
        
        result.append("</script>\n");
        result.append("</td>");

        return result.toString();
	}

	/**
     * @see org.opencms.widgets.I_CmsWidget#newInstance()
     */
	public I_CmsWidget newInstance() {
		return new CmsTinyMCEWidget(getHtmlWidgetOption());
	}
	
	/**
	 * Builds the toolbar.
	 * 
	 * @return Javascript code for toolbar configuration
	 */
	private String getToolbar(){
		String buttonString = getHtmlWidgetOption().getButtonBar(BUTTON_TRANSLATION_MAP, ",", false) ;
		
		buttonString = StringUtils.replace(buttonString, "[", "") ;
		buttonString = StringUtils.replace(buttonString, "]", "") ;
		buttonString = StringUtils.replace(buttonString, ",-,", ",") ;

		if(getHtmlWidgetOption().isFullPage()){
			buttonString = buttonString + ",fullpage";
		}
		
		String[] availableButtons = buttonString.split("\\,") ;
		StringBuilder toolbar = new StringBuilder() ;
		
		int currentRow = 1 ;
		for(int i=0 ; i < m_toolbarMap.length; i++){
			StringBuilder row = new StringBuilder() ;
			for(int j=0 ; j < m_toolbarMap[i].length; j++){
				if(isEnabledButton(m_toolbarMap[i][j], availableButtons)){
					if(row.length() > 0){
						row.append(", ") ;
					}
					row.append(m_toolbarMap[i][j]) ;
				}
			}
			if(row.length() > 0){
				toolbar.append("theme_advanced_buttons"+currentRow+": \"" + row +"\",\n") ;
				currentRow++ ;
			}
		}
		
		// it has to set empty string for rows without buttons
		for(int i=currentRow; i <= m_toolbarMap.length; i++){
			toolbar.append("theme_advanced_buttons"+i+": \"\",\n") ;
		}
		
		return toolbar.toString();
	}

	/**
	 * Check for particular button availability
	 * 
	 * @param buttonName - the button name
	 * @param avalableButtons - array with enabled buttons
	 * @return true if this button is enabled
	 */
	private boolean isEnabledButton(String buttonName, String[] avalableButtons){
		boolean result = false ;
		for(int i=0 ; i < avalableButtons.length; i++){
			if(buttonName.equals(avalableButtons[i])){
				result = true ;
				break ;
			}
		}
		return result ;
	}
	
}
