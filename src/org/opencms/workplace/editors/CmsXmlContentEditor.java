/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/workplace/editors/CmsXmlContentEditor.java,v $
 * Date   : $Date: 2004/12/16 11:00:49 $
 * Version: $Revision: 1.30 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Mananagement System
 *
 * Copyright (C) 2002 - 2003 Alkacon Software (http://www.alkacon.com)
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
package org.opencms.workplace.editors;

import org.opencms.file.CmsFile;
import org.opencms.file.CmsRequestContext;
import org.opencms.file.CmsResourceFilter;
import org.opencms.file.I_CmsResourceCollector;
import org.opencms.i18n.CmsEncoder;
import org.opencms.i18n.CmsLocaleManager;
import org.opencms.jsp.CmsJspActionElement;
import org.opencms.lock.CmsLock;
import org.opencms.main.CmsException;
import org.opencms.main.I_CmsConstants;
import org.opencms.main.OpenCms;
import org.opencms.util.CmsStringUtil;
import org.opencms.workplace.CmsWorkplaceAction;
import org.opencms.workplace.CmsWorkplaceSettings;
import org.opencms.workplace.xmlwidgets.A_CmsXmlWidget;
import org.opencms.workplace.xmlwidgets.CmsXmlWidgetCollector;
import org.opencms.workplace.xmlwidgets.I_CmsWidgetDialog;
import org.opencms.workplace.xmlwidgets.I_CmsXmlWidget;
import org.opencms.xml.CmsXmlContentDefinition;
import org.opencms.xml.CmsXmlException;
import org.opencms.xml.CmsXmlUtils;
import org.opencms.xml.content.CmsXmlContent;
import org.opencms.xml.content.CmsXmlContentErrorHandler;
import org.opencms.xml.content.CmsXmlContentFactory;
import org.opencms.xml.content.CmsXmlContentValueSequence;
import org.opencms.xml.types.CmsXmlNestedContentDefinition;
import org.opencms.xml.types.I_CmsXmlContentValue;
import org.opencms.xml.types.I_CmsXmlSchemaType;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.jsp.JspException;

/**
 * Creates the editor for XML content definitions.<p> 
 *
 * @author Alexander Kandzior (a.kandzior@alkacon.com)
 * @author Andreas Zahner (a.zahner@alkacon.com)
 * 
 * @version $Revision: 1.30 $
 * @since 5.5.0
 */
public class CmsXmlContentEditor extends CmsEditor implements I_CmsWidgetDialog {
    
    /** Action for checking content before executing the direct edit action. */
    public static final int ACTION_CHECK = 151;
    
    /** Action for optional element creation. */
    public static final int ACTION_ELEMENT_ADD = 152;
    
    /** Action for optional element removal. */
    public static final int ACTION_ELEMENT_REMOVE = 153;
    
    /** Action for new file creation. */
    public static final int ACTION_NEW = 154;
    
    /** Indicates that the content should be checked before executing the direct edit action. */
    public static final String EDITOR_ACTION_CHECK = "check";
    
    /** Indicates an optional element should be created. */
    public static final String EDITOR_ACTION_ELEMENT_ADD = "addelement";
    
    /** Indicates an optional element should be removed. */
    public static final String EDITOR_ACTION_ELEMENT_REMOVE = "removeelement";
    
    /** Indicates a new file should be created. */
    public static final String EDITOR_ACTION_NEW = I_CmsEditorActionHandler.C_DIRECT_EDIT_OPTION_NEW;
    
    /** Parameter name for the request parameter "elementindex". */
    public static final String PARAM_ELEMENTINDEX = "elementindex";
    
    /** Parameter name for the request parameter "elementname". */
    public static final String PARAM_ELEMENTNAME = "elementname";
    
    /** Constant for the editor type, must be the same as the editors subfolder name in the VFS. */
    private static final String EDITOR_TYPE = "xmlcontent";
    
    /** The content object to edit. */
    private CmsXmlContent m_content;
    
    /** The element locale. */
    private Locale m_elementLocale;
    
    /** The error handler for the xml content. */
    private CmsXmlContentErrorHandler m_errorHandler;
    
    /** File object used to read and write contents. */
    private CmsFile m_file;
    
    /** Indicates if an optional element is included in the form. */
    private boolean m_optionalElementPresent;
    
    /** Parameter stores the index of the element to add or remove. */
    private String m_paramElementIndex;
    
    /** Parameter stores the name of the element to add or remove. */
    private String m_paramElementName;    
    
    /** Parameter to indicate if a new XML content resource should be created. */
    private String m_paramNewLink;
    
    /** Visitor implementation that stored the widgets for the content.  */
    private CmsXmlWidgetCollector m_widgetCollector;

    /**
     * Public constructor.<p>
     * 
     * @param jsp an initialized JSP action element
     */
    public CmsXmlContentEditor(CmsJspActionElement jsp) {
        
        super(jsp);
    }
    
    /**
     * Performs the change element language action of the editor.<p>
     */
    public void actionChangeElementLanguage() {
        
        if (! m_content.hasLocale(getElementLocale())) {
            // create new element if selected language element is not present
            try {
                m_content.addLocale(getCms(), getElementLocale());
            } catch (CmsXmlException e) {
                if (OpenCms.getLog(this).isErrorEnabled()) {
                    OpenCms.getLog(this).error(e);
                }
            }
        }
        // save eventually changed content of the editor
        Locale oldLocale = CmsLocaleManager.getLocale(getParamOldelementlanguage());
        try {
            setEditorValues(oldLocale);
            if (! getErrorHandler().hasErrors()) {
                // no errors found in content, save to temporary file              
                writeContent();
            } else {
                // errors found, switch back to old language to show errors
                setParamElementlanguage(getParamOldelementlanguage());
                // set stored locale to null to reinitialize it
                m_elementLocale = null;
            }
        } catch (Exception e) {
            // should usually never happen
            if (OpenCms.getLog(this).isInfoEnabled()) {
                OpenCms.getLog(this).info(e);
            }
        }
    }
    
    /**
     * Deletes the temporary file and unlocks the edited resource when in direct edit mode.<p>
     * 
     * @param forceUnlock if true, the resource will be unlocked anyway
     */
    public void actionClear(boolean forceUnlock) {
        
        // delete the temporary file        
        deleteTempFile();
        if ("true".equals(getParamDirectedit()) || forceUnlock) {
            // unlock the resource when in direct edit mode or force unlock is true
            try {
                getCms().unlockResource(getParamResource());
            } catch (CmsException e) {
                // should usually never happen
                if (OpenCms.getLog(this).isInfoEnabled()) {
                    OpenCms.getLog(this).info(e);
                }       
            }
        }
    }
    
    /**
     * Performs a configurable action performed by the editor.<p>
     * 
     * The default action is: save resource, clear temporary files and publish the resource directly.<p>
     * 
     * @throws IOException if a redirection fails
     * @throws JspException if including a JSP fails
     */
    public void actionDirectEdit() throws IOException, JspException {
        
        // get the action class from the OpenCms runtime property
        I_CmsEditorActionHandler actionClass = OpenCms.getWorkplaceManager().getEditorActionHandler();
        if (actionClass == null) {
            // error getting the action class, save content and exit the editor
            actionSave();
            actionExit();
        } else {
            actionClass.editorAction(this, getJsp());
        }
    }
    
    /**
     * Performs the exit editor action.<p>
     * 
     * @see org.opencms.workplace.editors.CmsEditor#actionExit()
     */
    public void actionExit() throws IOException, JspException {    
        
        if (getAction() == ACTION_CANCEL) {
            // save and exit was cancelled
            return;
        }
        // unlock resource if we are in direct edit mode
        actionClear(false);
        // close the editor
        actionClose();
    }

    /**
     * Creates a new XML content item for editing.<p>
     * 
     * @throws JspException in case something goes wrong
     */
    public void actionNew() throws JspException {
        
        // get the collector used to create the new content
        int pos = m_paramNewLink.indexOf('|');
        String collectorName = m_paramNewLink.substring(0, pos);
        String param = m_paramNewLink.substring(pos + 1);
        
        // get the collector used for calculating the next file name
        I_CmsResourceCollector collector = OpenCms.getResourceManager().getContentCollector(collectorName);    
        
        try {
            
            // one resource serves as a "template" for the new resource
            CmsFile templateFile = getCms().readFile(getParamResource(), CmsResourceFilter.IGNORE_EXPIRATION);
            CmsXmlContent template = CmsXmlContentFactory.unmarshal(getCms(), templateFile);
            Locale locale = (Locale)OpenCms.getLocaleManager().getDefaultLocales(getCms(), getParamResource()).get(0);

            // now create a new XML content based on the templates content definition            
            CmsXmlContent newContent = CmsXmlContentFactory.createDocument(
                getCms(),
                locale,
                template.getEncoding(),
                template.getContentDefinition());

            // IMPORTANT: calculation of the name MUST be done here so the file name is ensured to be valid
            String newFileName = collector.getCreateLink(getCms(), collectorName, param);            
            
            // now create the resource, fill it with the marshalled XML and write it back to the VFS
            getCms().createResource(newFileName, templateFile.getTypeId());
            // re-read the created resource
            CmsFile newFile = getCms().readFile(newFileName, CmsResourceFilter.ALL);
            newFile.setContents(newContent.marshal());
            // write the file with the updated content
            getCms().writeFile(newFile);
            
            // wipe out parameters for the editor to ensure proper operation
            setParamNewLink(null);
            setParamAction(null);
            setParamResource(newFileName);
            setAction(ACTION_DEFAULT);
            
            // create the temporary file to work with
            setParamTempfile(createTempFile());
            
            // set the member variables for the content 
            m_file = newFile;
            m_content = newContent;                         
                        
        } catch (CmsException e) {
            if (OpenCms.getLog(this).isErrorEnabled()) {
                OpenCms.getLog(this).error("Error creating new XML content item '" + m_paramNewLink + "'" , e);
            }                
            throw new JspException(e);
        }
    }
    
    /**
     * Performs the preview xml content action in a new browser window.<p>
     * 
     * @throws IOException if redirect fails
     * @throws JspException if inclusion of error page fails
     */
    public void actionPreview() throws IOException, JspException {
        
        try {
            // save content of the editor only to the temporary file
            setEditorValues(getElementLocale());
            writeContent();
        } catch (CmsException e) {
            // show error page
            showErrorPage(this, e, "save");
        }
        
        // get preview uri from content handler
        String previewUri = m_content.getContentDefinition().getContentHandler().getPreview(getCms(), m_content, getParamTempfile());     
        
        // create locale request parameter
        StringBuffer param = new StringBuffer(8);
        if (previewUri.indexOf('?') != -1) {
            param.append("&");    
        } else {
            param.append("?");
        }
        param.append(I_CmsConstants.C_PARAMETER_LOCALE);
        param.append("=");
        param.append(getParamElementlanguage());
        
        // redirect to the temporary file with currently active element language or to the specified preview uri
        sendCmsRedirect(previewUri + param);
    }
    
    /**
     * Performs the save content action.<p>
     * 
     * @see org.opencms.workplace.editors.CmsEditor#actionSave()
     */
    public void actionSave() throws JspException {
    
        actionSave(getElementLocale());
        if (getAction() != ACTION_CANCEL) {
            // save successful, set save action         
           setAction(ACTION_SAVE);
        }
    }

    /**
     * Performs the save content action.<p>
     * 
     * This is also used when changing the element language.<p>
     * 
     * @param locale the locale to save the content
     * @throws JspException if including the error page fails
     */
    public void actionSave(Locale locale) throws JspException {
        
        try {                        
            setEditorValues(locale);
            // check if content has errors
            if (! getErrorHandler().hasErrors()) {
                // no errors found, write content and copy temp file contents
                writeContent();  
                commitTempFile();
            }
        } catch (CmsXmlException e) {
            showErrorPage(e, "xml");
        } catch (CmsException e) {
            showErrorPage(e, "save");
        }
    
    }
    
    /**
     * Adds an optional element to the xml content or removes an optional element from the xml content.<p>
     * 
     * Depends on the given action value.<p>
     * 
     * @throws JspException if including the error page fails
     */
    public void actionToggleElement() throws JspException {
        
        // set editor values from request
        try { 
            setEditorValues(getElementLocale());
        } catch (CmsXmlException e) {
            // an error occured while trying to set the values, stop action
            showErrorPage(e, "xml");
            return;
        }
        
        // validate the content values
        if (! getErrorHandler().hasErrors()) {
            // get the necessary parameters to add/remove the element
            int index = 0;
            try {
                index= Integer.parseInt(getParamElementIndex());
            } catch (Exception e) {
                // ignore, should not happen
            }
            
            if (getAction() == ACTION_ELEMENT_REMOVE) {
                // remove the value
                m_content.removeValue(getParamElementName(), getElementLocale(), index);
            } else {
                // add the new value after the clicked element
                if (m_content.hasValue(getParamElementName(), getElementLocale())) {
                    // when other values are present, increase index to use right position
                    index += 1;    
                }
                m_content.addValue(getCms(), getParamElementName(), getElementLocale(), index);  
            }
            
            if (getErrorHandler().hasWarnings(getElementLocale())) {
                // there were warnings for the edited content, reset error handler to avoid display issues
                resetErrorHandler();
            }
            
            try {
                // write the modified content to the temporary file
                writeContent();
            } catch (CmsException e) {
                // an error occured while trying to save
                showErrorPage(e, "save");
            }
        }    
    }
    
    /**
     * Returns the html for a button to add an optional element.<p>
     * 
     * @param elementName name of the element
     * @param insertAfter the index of the element after which the new element should be created
     * @param enabled if true, the button to add an element is shown, otherwise a spacer is returned
     * @return the html for a button to add an optional element
     */
    public String buildAddElement(String elementName, int insertAfter, boolean enabled) {
        
        if (enabled) {
            StringBuffer href = new StringBuffer(4);
            href.append("javascript:addElement('");
            href.append(elementName);
            href.append("', ");
            href.append(insertAfter);
            href.append(");");
            return button(href.toString(), null, "new", "button.addnew", 0);    
        } else {
            return buttonBarSpacer(20);
        }
    }
    
    /**
     * Returns the html for a button to remove an optional element.<p>
     * 
     * @param elementName name of the element
     * @param index the element index of the element to remove
     * @param enabled if true, the button to remove an element is shown, otherwise a spacer is returned
     * @return the html for a button to remove an optional element
     */
    public String buildRemoveElement(String elementName, int index, boolean enabled) {
        
        if (enabled) {
            StringBuffer href = new StringBuffer(4);
            href.append("javascript:removeElement('");
            href.append(elementName);
            href.append("', ");
            href.append(index);
            href.append(");");
            return button(href.toString(), null, "deletecontent", "button.delete", 0);    
        } else {
            return buttonBarSpacer(20);
        }
    }
    
    /**
     * Builds the html String for the element language selector.<p>
     * 
     * This method has to use the resource request parameter because the temporary file is
     * not available in the upper button frame.<p>
     *  
     * @param attributes optional attributes for the &lt;select&gt; tag
     * @return the html for the element language selectbox
     */
    public String buildSelectElementLanguage(String attributes) {
        
        return buildSelectElementLanguage(attributes, getParamResource(), getElementLocale());      
    }
    
    /**
     * @see org.opencms.workplace.editors.CmsEditor#getEditorResourceUri()
     */
    public String getEditorResourceUri() {
        
        return getSkinUri() + "editors/" + EDITOR_TYPE + "/";   
    }
    
    /**
     * Returns the current element locale.<p>
     * 
     * @return the current element locale
     */
    public Locale getElementLocale() {
               
        if (m_elementLocale == null) {
            m_elementLocale = CmsLocaleManager.getLocale(getParamElementlanguage());
        } 
        return m_elementLocale;
    }    
    
    /**
     * Returns the index of the element to add or remove.<p>
     *
     * @return the index of the element to add or remove
     */
    public String getParamElementIndex() {

        return m_paramElementIndex;
    }
    
    /**
     * Returns the name of the element to add or remove.<p>
     *
     * @return the name of the element to add or remove
     */
    public String getParamElementName() {

        return m_paramElementName;
    }
    
    /**
     * Returns the "new link" parameter.<p>
     *
     * @return the "new link" parameter
     */
    public String getParamNewLink() {

        return m_paramNewLink;
    }
    
    /**
     * Generates the HTML form for the XML content editor.<p> 
     * 
     * @return the HTML that generates the form for the XML editor
     */
    public String getXmlEditorForm() {
        
        // set "editor mode" attribute (required for link replacement in the root site) 
        getCms().getRequestContext().setAttribute(CmsRequestContext.ATTRIBUTE_EDITOR, new Boolean(true));               
        return getXmlEditorForm(m_content.getContentDefinition(), "", true).toString();
    }
    
    /**
     * Generates the HTML for the end of the html editor form page.<p>
     * 
     * @return the HTML for the end of the html editor form page
     * @throws JspException if including the error page fails
     */
    public String getXmlEditorHtmlEnd() throws JspException {
        
        StringBuffer result = new StringBuffer(32);
        if (m_optionalElementPresent) {
            // disabled optional element(s) present, reset widgets to show help bubbles on optional form entries
            resetWidgetCollector();    
        }
        try {
            // get all widgets from collector
            Iterator i = getWidgetCollector().getWidgets().keySet().iterator();
            while (i.hasNext()) {
                // get the value of the widget
                I_CmsXmlContentValue value = (I_CmsXmlContentValue)i.next();
                I_CmsXmlWidget widget = (I_CmsXmlWidget)getWidgetCollector().getWidgets().get(value);
                result.append(widget.getDialogHtmlEnd(getCms(), this, value));               
            }
            return result.toString();
        } catch (CmsXmlException e) {
            showErrorPage(e, "xml");
            return "";
        }       
    }
    
    /**
     * Generates the javascript includes for the used xml schema types in the editor form.<p>
     * 
     * @return the javascript includes for the used xml schema types
     * @throws JspException if including the error page fails
     */
    public String getXmlEditorIncludes() throws JspException {
        
        StringBuffer result = new StringBuffer(32);
        try {
            // iterate over unique widgets from collector
            Iterator i = getWidgetCollector().getUniqueWidgets().iterator();
            while (i.hasNext()) {
                I_CmsXmlWidget widget = (I_CmsXmlWidget)i.next();
                result.append(widget.getDialogIncludes(getCms(), this, m_content.getContentDefinition()));
                result.append("\n");
            }
        } catch (CmsXmlException e) {
            showErrorPage(e, "xml");
        }
        return result.toString();
    }
    
    /**
     * Generates the javascript initialization calls for the used xml schema types in the editor form.<p>
     * 
     * @return the javascript initialization calls for the used xml schema types
     * @throws JspException if including the error page fails
     */
    public String getXmlEditorInitCalls() throws JspException {
        
        StringBuffer result = new StringBuffer(32);
        try {
            // iterate over unique widgets from collector
            Iterator i = getWidgetCollector().getUniqueWidgets().iterator();
            while (i.hasNext()) {
                I_CmsXmlWidget widget = (I_CmsXmlWidget)i.next();
                result.append(widget.getDialogInitCall(getCms(), this));
            }
        } catch (CmsXmlException e) {
            showErrorPage(e, "xml");
        }
        return result.toString();
    }
    
    /**
     * Generates the javascript initialization methods for the used xml contents.<p>
     * 
     * @return the javascript initialization methods for the used xml contents
     * @throws JspException if including the error page fails
     */
    public String getXmlEditorInitMethods() throws JspException {
        
        StringBuffer result = new StringBuffer(32);
        try {
            // iterate over unique widgets from collector
            Iterator i = getWidgetCollector().getUniqueWidgets().iterator();
            while (i.hasNext()) {
                I_CmsXmlWidget widget = (I_CmsXmlWidget)i.next();
                result.append(widget.getDialogInitMethod(getCms(), this, m_content));
                result.append("\n");
            }
        } catch (CmsXmlException e) {
            showErrorPage(e, "xml");
        }
        return result.toString();
    }
    
    /**
     * Returns true if the edited content contains validation errors, otherwise false.<p>
     * 
     * @return true if the edited content contains validation errors, otherwise false
     */
    public boolean hasValidationErrors() {
        
        return getErrorHandler().hasErrors();    
    }
    
    /**
     * Returns true if the preview is available for the edited xml content.<p>
     * 
     * This method has to use the resource request parameter and read the file from vfs because the temporary file is
     * not available in the upper button frame.<p>
     * 
     * @return true if the preview is enabled, otherwise false
     */
    public boolean isPreviewEnabled() {
        
        try {
            // read the original file because temporary file is not created when opening button frame
            CmsFile file = getCms().readFile(getParamResource(), CmsResourceFilter.ALL);
            CmsXmlContent content = CmsXmlContentFactory.unmarshal(getCms(), file);
            return content.getContentDefinition().getContentHandler().getPreview(getCms(), m_content, getParamResource()) != null;
        } catch (Exception e) {
            // error reading or unmarshalling, no preview available
            return false;
        }
    }
    
    /**
     * Sets the editor values for the locale with the parameters from the request.<p>
     * 
     * Called before saving the xml content, redisplaying the input form, 
     * changing the language and adding or removing elements.<p>
     * 
     * @param locale the locale of the content to save
     * @throws CmsXmlException if something goes wrong
     */
    public void setEditorValues(Locale locale) throws CmsXmlException {
        
        List valueNames = getSimpleValueNames(m_content.getContentDefinition(), "", locale);
        Iterator i = valueNames.iterator();
        while (i.hasNext()) {
            String valueName = (String)i.next();
            I_CmsXmlContentValue value = m_content.getValue(valueName, locale);
            I_CmsXmlWidget widget = value.getContentDefinition().getContentHandler().getWidget(value);
            widget.setEditorValue(getCms(), getJsp().getRequest().getParameterMap(), this, value);
        }
    }
    
    /**
     * Sets the index of the element to add or remove.<p>
     *
     * @param elementIndex the index of the element to add or remove
     */
    public void setParamElementIndex(String elementIndex) {

        m_paramElementIndex = elementIndex;
    }
    
    /**
     * Sets the name of the element to add or remove.<p>
     *
     * @param elementName the name of the element to add or remove
     */
    public void setParamElementName(String elementName) {

        m_paramElementName = elementName;
    }

    /**
     * Sets the "new link" parameter.<p>
     *
     * @param paramNewLink the "new link" parameter to set
     */
    public void setParamNewLink(String paramNewLink) {

        m_paramNewLink = CmsEncoder.decode(paramNewLink);
    } 
    
    /**
     * Determines if the element language selector is shown dependent on the available Locales.<p>
     * 
     * @return true, if more than one Locale is available, otherwise false
     */
    public boolean showElementLanguageSelector() {
        
        List locales = OpenCms.getLocaleManager().getAvailableLocales(getCms(), getParamResource());
        if (locales == null || locales.size() < 2) {
            // for less than two available locales, do not create language selector
            return false;    
        }
        return true;
    }
    
    /**
     * Closes the editor and redirects to the workplace or the resource depending on the editor mode.<p>
     * 
     * @throws IOException if a redirection fails
     * @throws JspException if including a JSP fails
     */
    protected void actionClose() throws IOException, JspException {
        
        if ("true".equals(getParamDirectedit())) {
            // editor is in direct edit mode
            if (!"".equals(getParamBacklink())) {
                // set link to the specified back link target
                setParamCloseLink(getJsp().link(getParamBacklink()));
            } else {
                // set link to the edited resource
                setParamCloseLink(getJsp().link(getParamResource()));
            }
            // save initialized instance of this class in request attribute for included sub-elements
            getJsp().getRequest().setAttribute(C_SESSION_WORKPLACE_CLASS, this);
            // load the common JSP close dialog
            getJsp().include(C_FILE_DIALOG_CLOSE);
        } else {
            // redirect to the workplace explorer view 
            sendCmsRedirect(CmsWorkplaceAction.C_JSP_WORKPLACE_URI);
        }
    }
    
    /**
     * Initializes the editor content when opening the editor for the first time.<p>
     * 
     * Not necessary for the xmlcontent editor.<p>
     */
    protected void initContent() {
        
        // nothing to be done for the xmlcontent editor form
    }
    
    /**
     * Initializes the element language for the first call of the editor.<p>
     */
    protected void initElementLanguage() {
        
        // get all locales of the content
        List locales = m_content.getLocales();
        Locale defaultLocale = (Locale)OpenCms.getLocaleManager().getDefaultLocales(getCms(), getCms().getSitePath(m_file)).get(0);
        
        if (locales.size() > 0) {
            // locale element present, get the language
            if (locales.contains(defaultLocale)) {
                // get the element for the default locale
                setParamElementlanguage(defaultLocale.toString());
            } else {
                // get the first element that can be found
                setParamElementlanguage(locales.get(0).toString());
            }
        }
    }
    
    /**
     * @see org.opencms.workplace.CmsWorkplace#initWorkplaceRequestValues(org.opencms.workplace.CmsWorkplaceSettings, javax.servlet.http.HttpServletRequest)
     */
    protected void initWorkplaceRequestValues(CmsWorkplaceSettings settings, HttpServletRequest request) {
        
        // fill the parameter values in the get/set methods
        fillParamValues(request);        
        // set the dialog type
        setParamDialogtype(EDITOR_TYPE);
        
        if (getParamNewLink() != null) {
            setParamAction(EDITOR_ACTION_NEW);
        } else {
            // initialize a content object from the temporary file
            if (getParamTempfile() != null && !"null".equals(getParamTempfile())) {
                try {
                    m_file = getCms().readFile(this.getParamTempfile(), CmsResourceFilter.ALL);
                    m_content = CmsXmlContentFactory.unmarshal(getCms(), m_file);
                } catch (CmsException e) {
                    // error during initialization, show error page
                    try {
                        showErrorPage(this, e, "read");
                    } catch (JspException exc) {
                        // should usually never happen
                        if (OpenCms.getLog(this).isInfoEnabled()) {
                            OpenCms.getLog(this).info(exc);
                        }
                    }
                }
            }
        }   

        // set the action for the JSP switch 
        if (EDITOR_SAVE.equals(getParamAction())) {
            setAction(ACTION_SAVE);
        } else if (EDITOR_SAVEEXIT.equals(getParamAction())) {
            setAction(ACTION_SAVEEXIT);         
        } else if (EDITOR_EXIT.equals(getParamAction())) { 
            setAction(ACTION_EXIT);
        } else if (EDITOR_ACTION_CHECK.equals(getParamAction())) {
            setAction(ACTION_CHECK);
        } else if (EDITOR_SAVEACTION.equals(getParamAction())) {
            setAction(ACTION_SAVEACTION);
            try {
                actionDirectEdit();
            } catch (Exception e) {
                // should usually never happen
                if (OpenCms.getLog(this).isInfoEnabled()) {
                    OpenCms.getLog(this).info(e);
                }
            }
            setAction(ACTION_EXIT);
        } else if (EDITOR_SHOW.equals(getParamAction())) {
            setAction(ACTION_SHOW);
        } else if (EDITOR_SHOW_ERRORMESSAGE.equals(getParamAction())) {
            setAction(ACTION_SHOW_ERRORMESSAGE);
        } else if (EDITOR_CHANGE_ELEMENT.equals(getParamAction())) {
            setAction(ACTION_SHOW);
            actionChangeElementLanguage();
        } else if (EDITOR_ACTION_ELEMENT_ADD.equals(getParamAction())) {
            setAction(ACTION_ELEMENT_ADD);
            try {
                actionToggleElement();
            } catch (JspException e) {
                if (OpenCms.getLog(this).isErrorEnabled()) {
                    OpenCms.getLog(this).error("Failed to include the common error page");    
                }    
            }
            if (getAction() != ACTION_CANCEL && getAction() != ACTION_SHOW_ERRORMESSAGE) {
                // no error ocurred, redisplay the input form
                setAction(ACTION_SHOW);
            }
        } else if (EDITOR_ACTION_ELEMENT_REMOVE.equals(getParamAction())) {
            setAction(ACTION_ELEMENT_REMOVE);
            try {
                actionToggleElement();
            } catch (JspException e) {
                if (OpenCms.getLog(this).isErrorEnabled()) {
                    OpenCms.getLog(this).error("Failed to include the common error page");    
                }    
            }
            if (getAction() != ACTION_CANCEL && getAction() != ACTION_SHOW_ERRORMESSAGE) {
                // no error ocurred, redisplay the input form
                setAction(ACTION_SHOW);
            }
        } else if (EDITOR_ACTION_NEW.equals(getParamAction())) {
            setAction(ACTION_NEW);
            return;
        } else if (EDITOR_PREVIEW.equals(getParamAction())) {
            setAction(ACTION_PREVIEW);
        } else {
            // initial call of editor
            setAction(ACTION_DEFAULT);
            try {
                // lock resource if autolock is enabled in configuration
                if ("true".equals(getParamDirectedit())) {
                    // set a temporary lock in direct edit mode
                    checkLock(getParamResource(), CmsLock.C_MODE_TEMP);
                } else {
                    // set common lock
                    checkLock(getParamResource());
                }
                // create the temporary file
                setParamTempfile(createTempFile());
                // initialize a content object from the created temporary file
                m_file =  getCms().readFile(this.getParamTempfile(), CmsResourceFilter.ALL);
                m_content = CmsXmlContentFactory.unmarshal(getCms(), m_file);
            } catch (CmsException e) {
                // error during initialization
                try {
                    showErrorPage(this, e, "read");
                } catch (JspException exc) {
                    // should usually never happen
                    if (OpenCms.getLog(this).isInfoEnabled()) {
                        OpenCms.getLog(this).info(exc);
                    }       
                }
            }
            // set the initial element language if not given in request parameters
            if (getParamElementlanguage() == null) {
                initElementLanguage();
            }
        }      
    }   
    
    /**
     * Returns the error handler for error handling of the edited xml content.<p>
     * 
     * @return the error handler
     */
    private CmsXmlContentErrorHandler getErrorHandler() {
        
        if (m_errorHandler == null) {
            // errors were not yet checked, do this now and store result in member
            m_errorHandler = m_content.validate(getCms());             
        }    
        return m_errorHandler;
    }
    
    /**
     * Returns a list of value names containing the complete xpath of each value as String.<p>
     * 
     * @param contentDefinition the content definition to use
     * @param pathPrefix the xpath prefix
     * @param locale the locale to use
     * @return list of value names (containing the xpath of each value)
     */
    private List getSimpleValueNames(CmsXmlContentDefinition contentDefinition, String pathPrefix, Locale locale) {
        
        List valueNames = new ArrayList();
        Iterator i = contentDefinition.getTypeSequence().iterator();
        while (i.hasNext()) {
     
            I_CmsXmlSchemaType type = (I_CmsXmlSchemaType)i.next();
            
            String name = pathPrefix + type.getElementName();
            
            // get the element sequence of the type
            CmsXmlContentValueSequence elementSequence = m_content.getValueSequence(name, locale);
            int elementCount = elementSequence.getElementCount();
            // loop through elements
            for (int j=0; j<elementCount; j++) {
                I_CmsXmlContentValue value = elementSequence.getValue(j);
                
                StringBuffer xPath = new StringBuffer(pathPrefix.length() + 16);
                xPath.append(pathPrefix);
                xPath.append(CmsXmlUtils.createXpathElement(type.getElementName(), value.getIndex() + 1));
  
                if (! type.isSimpleType()) {
                    // recurse into nested type sequence
                    CmsXmlNestedContentDefinition nestedSchema = (CmsXmlNestedContentDefinition)type;
                    xPath.append("/");
                    valueNames.addAll(getSimpleValueNames(nestedSchema.getNestedContentDefinition(), xPath.toString(), locale));
                } else {
                    // this is a simple type, get widget to display
                    valueNames.add(xPath.toString());
                }
            }
        }
        return valueNames;
    }
    
    /**
     * Returns the different xml editor widgets used in the form to display.<p>
     * 
     * @return the different xml editor widgets used in the form to display
     */
    private CmsXmlWidgetCollector getWidgetCollector() {
        
        if (m_widgetCollector == null) {
            // create an instance of the widget collector
            m_widgetCollector = new CmsXmlWidgetCollector(getElementLocale());
            m_content.visitAllValuesWith(m_widgetCollector);
        }
        return m_widgetCollector;
    }
    
    /**
     * Recursivion that generates the HTML form for the XML content editor.<p>
     * 
     * This is a recursive method because nested schemas are possible,
     * do not call this method directly.<p>
     * 
     * @param contentDefinition the content definition to start with
     * @return the HTML that generates the form for the XML editor
     */
    private StringBuffer getXmlEditorForm(CmsXmlContentDefinition contentDefinition, String pathPrefix, boolean showHelpBubble) {
        
        StringBuffer result = new StringBuffer(64);

        try {
            // check if we are in a nested content definition
            boolean nested = CmsStringUtil.isNotEmpty(pathPrefix);
            
            // create table
            result.append("<table class=\"xmlTable");
            if (nested) {
                // use other style for nested content definition table
                result.append("Nested");    
            }
            result.append("\">\n");
            
            // show error header once if there were validation errors
            if (! nested &&  getErrorHandler().hasErrors(getElementLocale())) {
                result.append("<tr><td colspan=\"4\">&nbsp;</td></tr>\n");
                result.append("<tr><td colspan=\"2\">&nbsp;</td>");
                result.append("<td class=\"xmlTdErrorHeader\">");
                result.append(key("editor.xmlcontent.validation.error.title"));
                result.append("</td><td>&nbsp;");
                result.append("</td></tr>\n");
            }
                      
            // iterate the type sequence        
            Iterator i = contentDefinition.getTypeSequence().iterator();
            while (i.hasNext()) {
                // get the type
                I_CmsXmlSchemaType type = (I_CmsXmlSchemaType)i.next();
                CmsXmlContentDefinition nestedContentDefinition = contentDefinition;
                if (! type.isSimpleType()) {
                    // get nested content definition for nested types
                    CmsXmlNestedContentDefinition nestedSchema = (CmsXmlNestedContentDefinition)type;
                    nestedContentDefinition = nestedSchema.getNestedContentDefinition();
                }
                // create xpath to the current element
                String name = pathPrefix + type.getElementName();
                
                // get the element sequence of the current type
                CmsXmlContentValueSequence elementSequence = m_content.getValueSequence(name, getElementLocale());
                int elementCount = elementSequence.getElementCount();
                
                // check if value is optional or multiple
                boolean addValue = false;        
                if (elementCount < type.getMaxOccurs()) {           
                    addValue = true;    
                }
                boolean removeValue = false;
                if (elementCount > type.getMinOccurs()) {
                    removeValue = true;
                }
                
                // assure that at least one element is present in sequence
                boolean disabledElement = false;
                if (elementCount < 1) {
                    // current element is disabled, create dummy element
                    elementCount = 1;
                    elementSequence.addValue(getCms(), 0);
                    disabledElement = true;
                    m_optionalElementPresent = true;
                }
                
                // loop through multiple elements
                for (int j=0; j<elementCount; j++) {
                    // get value and corresponding widget
                    I_CmsXmlContentValue value = elementSequence.getValue(j);
                    I_CmsXmlWidget widget = contentDefinition.getContentHandler().getWidget(value);    
                    
                    // create label and help bubble cells
                    result.append("<tr>");
                    result.append("<td class=\"xmlLabel");
                    if (disabledElement) {
                        // element is disabled, mark it with css
                        result.append("Disabled");
                    }                    
                    result.append("\">");
                    result.append(A_CmsXmlWidget.getMessage(this, nestedContentDefinition, value.getElementName()));
                    result.append(": </td>");                    
                    if (showHelpBubble && value.getIndex() == 0) {                       
                        // show help bubble only on first element of each content definition 
                        result.append(A_CmsXmlWidget.getHelpBubble(getCms(), this, nestedContentDefinition, value.getElementName()));
                    } else {
                        // create empty cell for all following elements 
                        result.append(buttonBarSpacer(16));    
                    }
                    
                    // append individual widget html cell if element is enabled
                    if (! disabledElement) {
                        if (! type.isSimpleType()) {
                            // recurse into nested type sequence
                            String newPath = CmsXmlUtils.createXpathElement(value.getElementName(), value.getIndex() + 1);
                            result.append("<td class=\"maxwidth\">");
                            boolean showHelp = (j == 0);
                            result.append(getXmlEditorForm(nestedContentDefinition, pathPrefix + newPath + "/", showHelp));
                            result.append("</td>");
                        } else {
                            // this is a simple type, display widget
                            result.append(widget.getDialogWidget(getCms(), this, value));
                        }
                    } else {
                        // disabled element, show message for optional element
                        result.append("<td class=\"xmlTdDisabled\">");                        
                        result.append(key("editor.xmlcontent.optionalelement"));
                        result.append("</td>");    
                    }
                    
                    // append add and remove element buttons if required
                    result.append("<td style=\"vertical-align: top;\"><table border=\"0\" cellpadding=\"0\" cellspacing=\"0\"><tr>");
                    result.append(buildAddElement(name, value.getIndex(), addValue));
                    result.append(buildRemoveElement(name, value.getIndex(), removeValue));                    
                    result.append("</tr></table></td>");                   
                    // close row
                    result.append("</tr>\n");
                    
                    // show errors and/or warnings               
                    String key = value.getPath();
                    if (getErrorHandler().hasErrors(getElementLocale()) && getErrorHandler().getErrors(getElementLocale()).containsKey(key)) {
                        // show error message
                        result.append("<tr><td></td><td><img src=\"");
                        result.append(getEditorResourceUri());
                        result.append("error.gif");
                        result.append("\" border=\"0\" alt=\"\"></td><td class=\"xmlTdError\">");
                        result.append(resolveMacros((String)getErrorHandler().getErrors(getElementLocale()).get(key)));
                        result.append("</td><td></td></tr>\n");
                    }
                    // warnings can be additional to errors
                    if (getErrorHandler().hasWarnings(getElementLocale()) && getErrorHandler().getWarnings(getElementLocale()).containsKey(key)) {
                        // show warning message
                        result.append("<tr><td></td><td><img src=\"");
                        result.append(getEditorResourceUri());
                        result.append("warning.gif");
                        result.append("\" border=\"0\" alt=\"\"></td><td class=\"xmlTdWarning\">");
                        result.append(resolveMacros((String)getErrorHandler().getWarnings(getElementLocale()).get(key)));
                        result.append("</td><td></td></tr>\n");
                    }
                } 
            }
            // close table
            result.append("</table>\n");                     
        } catch (Throwable t) {
            OpenCms.getLog(this).error("Error in XML editor", t);
        }               
        return result;
    }
    
    /**
     * Resets the error handler member variable to reinitialize the error messages.<p>
     */
    private void resetErrorHandler() {
        
        m_errorHandler = null;
    }
    
    /**
     * Resets the widget collector member variable to reinitialize the widgets.<p>
     * 
     * This is needed to display the help messages of optional elements before building the html end of the form.<p>
     */
    private void resetWidgetCollector() {
    
        m_widgetCollector = null;
    }
    
    /**
     * Writes the xml content to the vfs and re-initializes the member variables.<p>
     * 
     * @throws CmsException if writing the file fails
     */
    private void writeContent() throws CmsException {
        
        String decodedContent = m_content.toString();
        try {
            m_file.setContents(decodedContent.getBytes(getFileEncoding()));
        } catch (UnsupportedEncodingException e) {
            throw new CmsException("Invalid content encoding encountered while editing file '" + getParamResource() + "'");
        }        
        // the file content might have been modified during the write operation    
        m_file = getCms().writeFile(m_file);
        m_content = CmsXmlContentFactory.unmarshal(getCms(), m_file);
    }
}