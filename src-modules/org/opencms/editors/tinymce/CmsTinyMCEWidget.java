package org.opencms.editors.tinymce;

import org.opencms.file.CmsObject;
import org.opencms.widgets.A_CmsHtmlWidget;
import org.opencms.widgets.CmsHtmlWidgetOption;
import org.opencms.widgets.I_CmsWidget;
import org.opencms.widgets.I_CmsWidgetDialog;
import org.opencms.widgets.I_CmsWidgetParameter;

public class CmsTinyMCEWidget extends A_CmsHtmlWidget{

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
	
	public String getDialogWidget(CmsObject cms, I_CmsWidgetDialog widgetDialog, I_CmsWidgetParameter param) {
		// TODO Auto-generated method stub
		return null;
	}

	/**
     * @see org.opencms.widgets.I_CmsWidget#newInstance()
     */
	public I_CmsWidget newInstance() {
		return new CmsTinyMCEWidget(getHtmlWidgetOption());
	}

	
	
}
