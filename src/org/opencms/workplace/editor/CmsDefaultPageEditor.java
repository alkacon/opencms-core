/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/workplace/editor/Attic/CmsDefaultPageEditor.java,v $
 * Date   : $Date: 2004/02/13 13:41:45 $
 * Version: $Revision: 1.39 $
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
package org.opencms.workplace.editor;

import org.opencms.i18n.CmsEncoder;
import org.opencms.i18n.CmsLocaleManager;
import org.opencms.jsp.CmsJspActionElement;
import org.opencms.main.CmsException;
import org.opencms.main.I_CmsConstants;
import org.opencms.main.OpenCms;
import org.opencms.page.CmsXmlPage;
import org.opencms.workplace.CmsWorkplaceAction;

import org.opencms.file.CmsFile;
import com.opencms.workplace.I_CmsWpConstants;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

import javax.servlet.jsp.JspException;

/**
 * Provides methods for building editors for the CmsDefaultPage page type.<p> 
 * 
 * Extend this class for all editors that work with the CmsDefaultPage.<p>
 *
 * @author  Andreas Zahner (a.zahner@alkacon.com)
 * @version $Revision: 1.39 $
 * 
 * @since 5.1.12
 */
public abstract class CmsDefaultPageEditor extends CmsEditor {
    
    /** Constant value for the customizable action button */
    public static final int ACTION_SAVEACTION = 200;
    
    /** Constant for the customizable action button */
    public static final String EDITOR_SAVEACTION = "saveaction";
    
    /** The element locale */
    private Locale m_elementLocale;
    
    /** File object used to read and write contents */
    protected CmsFile m_file;

    /** Page object used from the action and init methods, be sure to initialize this e.g. in the initWorkplaceRequestValues method */
    protected CmsXmlPage m_page;
    
    private String m_paramBodylanguage;
    private String m_paramBodyname;
    private String m_paramOldbodylanguage;
    private String m_paramOldbodyname; 
      
    /**
     * Public constructor.<p>
     * 
     * @param jsp an initialized JSP action element
     */
    public CmsDefaultPageEditor(CmsJspActionElement jsp) {
        super(jsp);
    }
    
    /**
     * Performs the change body action of the editor.<p>
     */
    public void actionChangeBodyElement() {
        try {
            // save eventually changed content of the editor to the temporary file
            Locale oldLocale = CmsLocaleManager.getLocale(getParamOldbodylanguage());
            performSaveContent(getParamOldbodyname(), oldLocale);
        } catch (CmsException e) {
            // show error page
            try {
            showErrorPage(this, e, "save");
            } catch (JspException exc) {
                // ignore this exception
            }
        }
        // re-initialize the element name if the language has changed
        if (!getParamBodylanguage().equals(getParamOldbodylanguage())) {
            initBodyElementName(getParamOldbodyname());
        }
        // get the new editor content 
        initContent();
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
                getCms().unlockResource(getParamResource(), false);
            } catch (CmsException e) {
                // ignore this exception
            }
        }
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
                setParamOkLink(getParamBacklink());
            } else {
                // set link to the edited resource
                setParamOkLink(getParamResource());
            }
            // set the okfunctions parameter to load the common close dialog jsp (to disable history back jump)
            setParamOkFunctions("var x=null;");
            // save initialized instance of this class in request attribute for included sub-elements
            getJsp().getRequest().setAttribute(C_SESSION_WORKPLACE_CLASS, this);
            closeDialog();
        } else {
            // redirect to the workplace explorer view 
            getJsp().getResponse().sendRedirect(getJsp().link(CmsWorkplaceAction.C_JSP_WORKPLACE_URI));
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
     * Performs the exit editor action and deletes the temporary file.<p>
     * 
     * @see org.opencms.workplace.editor.CmsEditor#actionExit()
     */
    public void actionExit() throws IOException, JspException {
        // clear temporary file and unlock resource, if in directedit mode
        actionClear(false);
        // close the editor
        actionClose();
    }
    
    /**
     * Performs the preview page action in a new browser window.<p>
     * 
     * @throws IOException if redirect fails
     * @throws JspException if inclusion of error page fails
     */
    public void actionPreview() throws IOException, JspException {
        // save content of the editor to the temporary file
        try {
            performSaveContent(getParamBodyname(), getElementLocale());
        } catch (CmsException e) {
            // show error page
            showErrorPage(this, e, "save");
        }
        
        // redirect to the temporary file
        getCms().getRequestContext().getResponse().sendCmsRedirect(getParamTempfile());
        
    }

    /**
     * @see org.opencms.workplace.editor.CmsEditor#actionSave()
     */
    public void actionSave() throws JspException { 
        try {
            // save content to temporary file
            performSaveContent(getParamBodyname(), getElementLocale());
            // copy the temporary file content back to the original file
            commitTempFile();
        } catch (CmsException e) {
            // error during saving, show error dialog
            showErrorPage(this, e, "save");
        }
    }
    
    /**
     * Builds the html String for the body language selector.<p>
     *  
     * @param attributes optional attributes for the &lt;select&gt; tag
     * @return the html for the body language selectbox
     */
    public String buildSelectBodyLanguage(String attributes) {
        // get locale names based on properties and global settings
        List locales = OpenCms.getLocaleManager().getAvailableLocales(getCms(), getParamTempfile());
        List options = new ArrayList(locales.size());
        List selectList = new ArrayList(locales.size());        
        int currentIndex = -1;
        for (int counter = 0; counter < locales.size(); counter++) {
            // create the list of options and values
            Locale curLocale = (Locale)locales.get(counter);
            selectList.add(curLocale.toString());
            options.add(curLocale.getDisplayName(getLocale()));
            if (curLocale.equals(getElementLocale())) {
                // set the selected index of the selector
                currentIndex = counter;
            }
        }
       
        if (currentIndex == -1) {
            // no matching body language found, use first body language in list
            if (selectList != null && selectList.size() > 0) {
                currentIndex = 0;
                setParamBodylanguage((String)selectList.get(0));
            }
        }
    
        return buildSelect(attributes, options, selectList, currentIndex, false);      
    }
    
    /**
     * Builds the html String for the body name selector.<p>
     *  
     * @param attributes optional attributes for the &lt;select&gt; tag
     * @return the html for the body name selectbox
     */
    public String buildSelectBodyName(String attributes) {
        List elementList = null;
        try { 
            elementList = CmsDialogElements.computeElements(getCms(), getParamTempfile());
        } catch (CmsException e) {
            // no property found, display all present elements instead
            List bodies = m_page.getNames(getElementLocale());
            Collections.sort(bodies);
            int currentIndex = bodies.indexOf(getParamBodyname());   
            if (currentIndex == -1) {
                // mark the default body as selected
                currentIndex = bodies.indexOf(I_CmsConstants.C_XML_BODY_ELEMENT);
                setParamBodyname(I_CmsConstants.C_XML_BODY_ELEMENT);
            }
            return buildSelect(attributes, bodies, bodies, currentIndex, false);
        }
        
        int counter = 0;
        int currentIndex = -1; 
        Iterator i = elementList.iterator();
        List options = new ArrayList(elementList.size());
        List values = new ArrayList(elementList.size());
        while (i.hasNext()) {
            // get the current list element
            String[] currentElement = (String[])i.next();               
            String elementName = currentElement[0];
            String elementNice = currentElement[1];
            if (getParamBodyname().equals(elementName)) {
                // current body is the displayed one, mark it as selected
                currentIndex = counter;
            }
            if (!m_page.hasElement(elementName, getElementLocale()) || m_page.isEnabled(elementName, getElementLocale())) {
                // add element if it is not available or if it is enabled
                options.add(elementNice);
                values.add(elementName);
                counter++;
            }
        } 
        return buildSelect(attributes, options, values, currentIndex, false);
    }
    
    /**
     * Builds the html for the font face select box of a WYSIWYG editor.<p>
     * 
     * @param attributes optional attributes for the &lt;select&gt; tag
     * @return the html for the font face select box
     */
    public String buildSelectFonts(String attributes) {
        List names = new ArrayList();
        for (int i=0; i<I_CmsWpConstants.C_SELECTBOX_FONTS.length; i++) {
            String value = I_CmsWpConstants.C_SELECTBOX_FONTS[i];
            names.add(value);
        }        
        return buildSelect(attributes, names, names, -1, false);
    }
    
    /**
     * Returns the editor action for a "cancel" button.<p>
     * 
     * This overwrites the cancel method of the CmsDialog class.<p>
     * 
     * Always use this value, do not write anything directly in the html page.<p>
     * 
     * @return the default action for a "cancel" button
     */
    public String buttonActionCancel() {
        String target = null;
        if ("true".equals(getParamDirectedit())) {
            // editor is in direct edit mode
            if (!"".equals(getParamBacklink())) {
                // set link to the specified back link target
                target = getParamBacklink();
            } else {
                // set link to the edited resource
                target = getParamResource();
            }
        } else {
            // in workplace mode, show explorer view
            target = CmsWorkplaceAction.C_JSP_WORKPLACE_URI;
        }
        return "onclick=\"top.location.href='" + getJsp().link(target) + "';\"";
    }
    
    /**
     * Builds the html to display the special action button for the direct edit mode of the editor.<p>
     * 
     * @param jsFunction the JavaScript function which will be executed on the mouseup event 
     * @param type 0: image only (default), 1: image and text, 2: text only
     * @return the html to display the special action button
     */
    public String buttonActionDirectEdit(String jsFunction, int type) {
        // get the action class from the OpenCms runtime property
        I_CmsEditorActionHandler actionClass = OpenCms.getWorkplaceManager().getEditorActionHandler();
        String url;
        String name;
        boolean active = false; 
        if (actionClass != null) {
            // get button parameters and state from action class
            url = actionClass.getButtonUrl(getJsp(), getParamResource());
            name = actionClass.getButtonName();
            active = actionClass.isButtonActive(getJsp(), getParamResource());
        } else {
            // action class not defined, display inactive button
            url = getSkinUri() + "buttons/publish_in";
            name = "explorer.context.publish";
        }
        String image = url.substring(url.lastIndexOf("/") + 1);
        if (url.endsWith(".gif")) {
            image = image.substring(0, image.length() - 4);
        }
        
        if (active) {
            // create the link for the button
            return button("javascript:" + jsFunction, null, image, name, type, url.substring(0, url.lastIndexOf("/") + 1));
        } else {
            // create the inactive button
            return button(null, null, image, name + "_in", type, url.substring(0, url.lastIndexOf("/") + 1));
        }
    }
    
    /**
     * Escapes the content and title parameters to display them in the editor form.<p>
     * 
     * This method has to be called on the JSP right before the form display html is created.<p>     *
     */
    public void escapeParams() {
        // escape the content
        setParamContent(CmsEncoder.escapeWBlanks(getParamContent(), CmsEncoder.C_UTF8_ENCODING));
    }
    
    /**
     * Returns the current element locale.<p>
     * 
     * @return the current element locale
     */
    public Locale getElementLocale() {
        if (m_elementLocale == null) {
            m_elementLocale = CmsLocaleManager.getLocale(getParamBodylanguage());
        } 
        return m_elementLocale;
    }    
    
    /**
     * Returns the current body element language.<p>
     * 
     * @return the current body element language
     */
    public String getParamBodylanguage() {
        return m_paramBodylanguage;
    }

    /**
     * Returns the current body element name.<p>
     * 
     * @return the current body element name
     */
    public String getParamBodyname() {
        return m_paramBodyname;
    }

    /**
    * Returns the old body element language.<p>
    * 
    * @return the old body element language
    */
   public String getParamOldbodylanguage() {
       return m_paramOldbodylanguage;
   }

   /**
    * Returns the old body element name.<p>
    * 
    * @return the old body element name
    */
   public String getParamOldbodyname() {
       return m_paramOldbodyname;
   }
    
    /**
     * Initializes the body element language for the first call of the editor.<p>
     */
    protected void initBodyElementLanguage() {
        List locales = m_page.getLocales();
        Locale defaultLocale = (Locale)OpenCms.getLocaleManager().getDefaultLocales(getCms(), getCms().readAbsolutePath(m_file)).get(0);
        
        if (locales.size() == 0) {
            // no body present, create default body
            if (!m_page.hasElement(I_CmsConstants.C_XML_BODY_ELEMENT, defaultLocale)) {
                m_page.addElement(I_CmsConstants.C_XML_BODY_ELEMENT, defaultLocale);
            }
            try {
                getCms().writeFile(m_page.write(m_file));
            } catch (CmsException e) {
                // show error page
                try {
                showErrorPage(this, e, "save");
                } catch (JspException exc) {
                    // ignore this exception
                }
            }
            setParamBodylanguage(defaultLocale.toString());
        } else {
            // body present, get the language
            if (locales.contains(defaultLocale)) {
                // get the body for the default language
                setParamBodylanguage(defaultLocale.toString());
            } else {
                // get the first body that can be found
                setParamBodylanguage(locales.get(0).toString());
            }

        }
    }

    /**
     * Initializes the body element name of the editor.<p>
     * 
     * This has to be called after the element language has been set with setParamBodylanguage().<p>
     * 
     * @param elementName the name of the element to initialize or null, if default element should be used
     */
    protected void initBodyElementName(String elementName) { 
        if (elementName == null || (m_page.hasElement(elementName, getElementLocale()) && !m_page.isEnabled(elementName, getElementLocale()))) {                  
            // elementName not specified or given element is disabled, set to default element
            List elements = m_page.getNames(getElementLocale());
            if (elements.contains(I_CmsConstants.C_XML_BODY_ELEMENT)) {
                // default element present
                setParamBodyname(I_CmsConstants.C_XML_BODY_ELEMENT);
            } else {
                // get first element from element list
                setParamBodyname((String)elements.get(0));
            }
        } else {
            // elementName specified and element is enabled or not present, set to elementName
            setParamBodyname(elementName);
        }    
    }
    
    /** 
     * This method has to be called after initializing the body element name and language.<p>
     * 
     * @see org.opencms.workplace.editor.CmsEditor#initContent()
     */
    protected void initContent() {
        // get the content from the temporary file     
        try {
            CmsXmlPage page = CmsXmlPage.read(getCms(), getCms().readFile(this.getParamTempfile()));
            String elementData = page.getContent(getCms(), getParamBodyname(), getElementLocale(), true);
            if (elementData != null) {
                setParamContent(elementData);
            } else {
                setParamContent("");
            }
        } catch (CmsException e) {
            // reading of file contents failed, show error page
            try {
                showErrorPage(this, e, "read");
            } catch (JspException exc) {
                // inclusion of error page failed, ignore
            }
        }
    }
    
    /**
     * Saves the editor content to the temporary file.<p>
     * 
     * @param body the body name to write
     * @param locale the body locale to write
     * @throws CmsException if writing the file fails
     */
    protected void performSaveContent(String body, Locale locale) throws CmsException {
        // prepare the content for saving
        String content = prepareContent(true);

        // create the element if necessary and if content is present
        if (!m_page.hasElement(body, locale) && !"".equals(content)) {
            m_page.addElement(body, locale);
        }
        
        // get the enabled state of the element
        boolean enabled = m_page.isEnabled(body, locale);
        
        // set the element data
        if (m_page.hasElement(body, locale)) {
            m_page.setContent(getCms(), body, locale, content);
            m_page.setEnabled(body, locale, enabled);
        }

        // write the file
        getCms().writeFile(m_page.write(m_file));
    }
    
    /**
     * Manipulates the content String for different editor views and the save operation.<p>
     * 
     * @param save if set to true, the result String is not escaped and the content parameter is not updated
     * @return the prepared content String
     */
    protected abstract String prepareContent(boolean save);

    /**
     * Sets the current body element language.<p>
     * 
     * @param bodyLanguage the current body element language
     */
    public void setParamBodylanguage(String bodyLanguage) {
        m_paramBodylanguage = bodyLanguage;
    }

    /**
     * Sets the current body element name.<p>
     * 
     * @param bodyName the current body element name
     */
    public void setParamBodyname(String bodyName) {
        m_paramBodyname = bodyName;
    }

   /**
    * Sets the old body element language.<p>
    * 
    * @param oldBodyLanguage the old body element language
    */
   public void setParamOldbodylanguage(String oldBodyLanguage) {
       m_paramOldbodylanguage = oldBodyLanguage;
   }

   /**
    * Sets the old body element name.<p>
    * 
    * @param oldBodyName the old body element name
    */
   public void setParamOldbodyname(String oldBodyName) {
       m_paramOldbodyname = oldBodyName;
   } 
   
   
   /**
    * Returns the OpenCms VFS uri of the template of the current page.<p>
    * 
    * @return the OpenCms VFS uri of the template of the current page
    */
   public String getUriTemplate() {
       String result = "";
       try {
           result = getCms().readProperty(getParamResource(), I_CmsConstants.C_PROPERTY_TEMPLATE, true, "");
       } catch (CmsException e) {
           OpenCms.getLog(this).warn("Template property could not be read", e);
       }
       return result;
   }
   
   /**
    * Returns the OpenCms VFS uri of the style sheet of the current page.<p>
    * 
    * @return the OpenCms VFS uri of the style sheet of the current page
    */
   public String getUriStyleSheet() {
        String result = "";
        try {
            String currentTemplate = getUriTemplate();
            if (! "".equals(currentTemplate)) {
                // read the stylesheet from the template file
                result = getCms().readProperty(currentTemplate, I_CmsConstants.C_PROPERTY_TEMPLATE, false, "");
            }
        } catch (CmsException e) {
            OpenCms.getLog(this).warn("Template property for style sheet could not be read");
        }
        return result;
   }

}