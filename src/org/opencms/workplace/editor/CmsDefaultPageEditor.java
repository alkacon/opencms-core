/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/workplace/editor/Attic/CmsDefaultPageEditor.java,v $
 * Date   : $Date: 2003/11/28 16:13:11 $
 * Version: $Revision: 1.2 $
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

import com.opencms.core.CmsException;
import com.opencms.core.I_CmsConstants;
import com.opencms.flex.jsp.CmsJspActionElement;
import com.opencms.util.Encoder;
import com.opencms.workplace.CmsHelperMastertemplates;
import com.opencms.workplace.I_CmsWpConstants;

import org.opencms.main.OpenCms;
import org.opencms.page.CmsDefaultPage;
import org.opencms.page.CmsXmlPage;
import org.opencms.workplace.CmsWorkplaceAction;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.Vector;

import javax.servlet.jsp.JspException;

/**
 * Provides methods for building editors for the CmsDefaultPage page type.<p> 
 * 
 * Extend this class for all editors that work with the CmsDefaultPage.<p>
 *
 * @author  Andreas Zahner (a.zahner@alkacon.com)
 * @version $Revision: 1.2 $
 * 
 * @since 5.1.12
 */
public abstract class CmsDefaultPageEditor extends CmsEditor {
     
    private String m_paramBodylanguage;
    private String m_paramBodyname;
    private String m_paramNewbodyname;
    private String m_paramOldbodylanguage;
    private String m_paramOldbodyname; 
    private String m_paramPageTemplate;

    protected CmsDefaultPage m_page;

    
    /** Helper variable to store the html content for the template selector.<p> */
    private String m_selectTemplates = null;
      
    /**
     * Public constructor.<p>
     * 
     * @param jsp an initialized JSP action element
     */
    public CmsDefaultPageEditor(CmsJspActionElement jsp) {
        super(jsp);
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
     * Sets the current body element language.<p>
     * 
     * @param bodyLanguage the current body element language
     */
    public void setParamBodylanguage(String bodyLanguage) {
        m_paramBodylanguage = bodyLanguage;
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
     * Sets the current body element name.<p>
     * 
     * @param bodyName the current body element name
     */
    public void setParamBodyname(String bodyName) {
        m_paramBodyname = bodyName;
    }
    
    /**
    * Returns the new body element language.<p>
    * 
    * @return the new body element language
    */
    public String getParamNewbodyname() {
        if (m_paramNewbodyname == null) {
            m_paramNewbodyname = "";
        }
        return m_paramNewbodyname;
    }
    
    /**
     * Sets the new body element name.<p>
     * 
     * @param newBodyName the new body element name
     */
    public void setParamNewbodyname(String newBodyName) {
        m_paramNewbodyname = newBodyName;
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
    * Sets the old body element language.<p>
    * 
    * @param oldBodyLanguage the old body element language
    */
   public void setParamOldbodylanguage(String oldBodyLanguage) {
       m_paramOldbodylanguage = oldBodyLanguage;
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
    * Sets the old body element name.<p>
    * 
    * @param oldBodyName the old body element name
    */
   public void setParamOldbodyname(String oldBodyName) {
       m_paramOldbodyname = oldBodyName;
   }
    
    /**
     * Returns the page template.<p>
     * 
     * @return the page template
     */
    public String getParamPagetemplate() {
        return m_paramPageTemplate;
    }
    
    /**
     * Sets the page template.<p>
     * 
     * @param pageTemplate the page template
     */
    public void setParamPagetemplate(String pageTemplate) {
        m_paramPageTemplate = pageTemplate;
    }  
    
    /**
     * Initializes the body element language for the first call of the editor.<p>
     * 
     * @throws CmsException if something goes wrong
     */
    protected void initBodyElementLanguage() throws CmsException {
        Set languages = m_page.getLanguages();
        String defaultLanguage = OpenCms.getUserDefaultLanguage();
        if (languages.size() == 0) {
            // no body present, create default body
            if (!m_page.hasElement(I_CmsConstants.C_XML_BODY_ELEMENT, defaultLanguage)) {
                m_page.addElement(I_CmsConstants.C_XML_BODY_ELEMENT, defaultLanguage);
            }
            getCms().writeFile(m_page.marshal());
            setParamBodylanguage(defaultLanguage);
        } else {
            // body present, get the language
            if (languages.contains(defaultLanguage)) {
                // get the body for the default language
                setParamBodylanguage(defaultLanguage);
            } else {
                // get the first body that can be found
                setParamBodylanguage((String)languages.toArray()[0]);
            }

        }
    }

    /**
     * Initializes the body element name of the editor.<p>
     * 
     * This has to be called after the element language has been set with setParamBodylanguage().<p>
     * 
     * @throws CmsException if something goes wrong
     */
    protected void initBodyElementName() throws CmsException {
        // set the initial body element name
       List bodies = m_page.getNames(getParamBodylanguage());
       if (bodies.size() == 0) {
           // no body present, so create an empty default body
            m_page.addElement(I_CmsConstants.C_XML_BODY_ELEMENT, getParamBodylanguage());
            getCms().writeFile(m_page.marshal());
            setParamBodyname(I_CmsConstants.C_XML_BODY_ELEMENT);
        } else {
            // body present, set body to default body if possible
            if (bodies.contains(I_CmsConstants.C_XML_BODY_ELEMENT)) {
                setParamBodyname(I_CmsConstants.C_XML_BODY_ELEMENT);
            } else {
                setParamBodyname((String)bodies.get(0));
            }
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
            CmsDefaultPage page = (CmsDefaultPage)CmsXmlPage.newInstance(getCms(), getCms().readFile(this.getParamTempfile()));
            byte[] elementData = page.getElementData(getParamBodyname(), getParamBodylanguage());
            if (elementData != null) {
                setParamContent(new String(elementData).trim());
            } else {
                setParamContent("");
            }
        } catch (CmsException e) {
            // reading of file contents failed, show error dialog
            setAction(ACTION_SHOW_ERRORMESSAGE);
            setParamErrorstack(e.getStackTraceAsString());
            setParamTitle(key("error.title.editorread"));
            setParamMessage(key("error.message.editorread"));
            String reasonSuggestion = key("error.reason.editorread") + "<br>\n" + key("error.suggestion.editorread") + "\n";
            setParamReasonSuggestion(reasonSuggestion);
            // log the error 
            String errorMessage = "Error while reading file " + getParamResource() + ": " + e;
            if (OpenCms.getLog(this).isErrorEnabled()) {
                OpenCms.getLog(this).error(errorMessage, e);
            }
            // save initialized instance of this class in request attribute for included sub-elements
            getJsp().getRequest().setAttribute(C_SESSION_WORKPLACE_CLASS, this);
            try {
                // include the common error dialog
                getJsp().include(C_FILE_DIALOG_SCREEN_ERROR);
            } catch (JspException exc) {
                // inclusion of error page failed, ignore
            }
        }
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
     * Builds the html String for the body language selector.<p>
     *  
     * @param attributes optional attributes for the &lt;select&gt; tag
     * @return the html for the body language selectbox
     * @throws CmsException if something goes wrong
     */
    public String buildSelectBodyLanguage(String attributes) throws CmsException {
        Set languages = m_page.getLanguages();
        List options = new ArrayList(languages.size());
        List selectList = new ArrayList(languages.size());
        Iterator i = languages.iterator();
        int currentIndex = -1;
        int counter = 0;
        while (i.hasNext()) {
            String curLang = (String)i.next();
            selectList.add(curLang);
            Locale curLocale = new Locale(curLang);
            options.add(curLocale.getDisplayLanguage(new Locale(getSettings().getLanguage())));
            if (curLang.equals(getParamBodylanguage())) {
                currentIndex = counter;
            }
            counter++;
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
     * @throws CmsException if something goes wrong
     */
    public String buildSelectBodyName(String attributes) throws CmsException {
        List bodies = m_page.getNames(getParamBodylanguage());
        Collections.sort(bodies);
        int currentIndex = bodies.indexOf(getParamBodyname());   
        if (currentIndex == -1) {
            currentIndex = bodies.indexOf(I_CmsConstants.C_XML_BODY_ELEMENT);
            setParamBodyname(I_CmsConstants.C_XML_BODY_ELEMENT);
        }

        return buildSelect(attributes, bodies, bodies, currentIndex, false);
    }

    /**
     * Builds the html for the page template select box.<p>
     * 
     * @param attributes optional attributes for the &lt;select&gt; tag
     * @return the html for the page template select box
     */
    public String buildSelectTemplates(String attributes) {
        if (m_selectTemplates == null) {
            // member variable is null, so generate the html
            Vector names = new Vector();
            Vector values = new Vector();
            Integer selectedValue = new Integer(-1);
            try {
                selectedValue = CmsHelperMastertemplates.getTemplates(getCms(), names, values, getParamPagetemplate(), -1);
            } catch (CmsException e) {
                // ignore this exception
            }
            if (selectedValue.intValue() == -1) {
                // no template found -> use the given one
                // first clean the vectors
                names.removeAllElements();
                values.removeAllElements();
                // now add the current template
                String name = getParamPagetemplate();
                try { 
                    // read the title of this template
                    name = getCms().readProperty(name, I_CmsConstants.C_PROPERTY_TITLE);
                } catch (CmsException exc) {
                    // ignore this exception - the title for this template was not readable
                }
                names.add(name);
                values.add(getParamPagetemplate());
            }
            m_selectTemplates = buildSelect(attributes, names, values, selectedValue.intValue(), false);
        }
        return m_selectTemplates;
    }
   
    /**
     * Performs a change template action.<p>
     * 
     * @throws CmsException if changing the template fails
     */
    public void actionChangeTemplate() throws CmsException {
        // switch to the temporary file project
        switchToTempProject();       
        // write the changed template property to the temporary file
        getCms().writeProperty(getParamTempfile(), I_CmsConstants.C_PROPERTY_TEMPLATE, getParamPagetemplate());        
        // switch back to the current users project
        switchToCurrentProject();     
    }
    
    /**
     * Performs the change body action of the editor.<p>
     * 
     * @throws CmsException if something goes wrong
     */
    public void actionChangeBodyElement() throws CmsException {
        // save eventually changed content of the editor to the temporary file
        performSaveContent(getParamOldbodyname(), getParamOldbodylanguage());
        // re-initialize the body element name if the language has changed
        if (!getParamBodylanguage().equals(getParamOldbodylanguage())) {
            initBodyElementName();
        }
        // get the new editor content 
        initContent();
    }
    
    /**
     * Performs the exit editor action and deletes the temporary file.<p>
     * 
     * @see org.opencms.workplace.editor.CmsEditor#actionExit()
     */
    public void actionExit() throws CmsException, IOException {
        // delete the temporary file        
        deleteTempFile();
        // now redirect to the workplace explorer view
        getJsp().getResponse().sendRedirect(getJsp().link(CmsWorkplaceAction.C_JSP_WORKPLACE_URI));   
    }
    
    /**
     * Performs the creation of a new body action.<p>
     * 
     * @throws CmsException if something goes wrong
     */
    public void actionNewBody() throws CmsException {
        // save content of the editor to the temporary file
        performSaveContent(getParamBodyname(), getParamBodylanguage());
        String newBody = getParamNewbodyname();
        if (newBody != null && !"".equals(newBody.trim()) && !"null".equals(newBody)) {
            if (!m_page.hasElement(newBody, getParamBodylanguage())) {
                m_page.addElement(newBody, getParamBodylanguage());
                getCms().writeFile(m_page.marshal());
                setParamBodyname(newBody);
                initContent();
            }
        }        
    }
    
    /**
     * Performs the preview page action in a new browser window.<p>
     * 
     * @throws CmsException if something goes wrong
     */
    public void actionPreview() throws CmsException {
        // save content of the editor to the temporary file
        performSaveContent(getParamBodyname(), getParamBodylanguage());
        try {
            getCms().getRequestContext().getResponse().sendCmsRedirect(getParamTempfile());
        } catch (IOException e) {
            // do nothing...
        }
    }

    /**
     * @see org.opencms.workplace.editor.CmsEditor#actionSave()
     */
    public void actionSave() throws JspException { 
        try {
            // write the modified title to the temporary file
            if (getParamPagetitle() != null && !"null".equals(getParamPagetitle())) {
                getCms().writeProperty(getParamTempfile(), I_CmsConstants.C_PROPERTY_TITLE, getParamPagetitle());
            }
            // save content to temporary file
            performSaveContent(getParamBodyname(), getParamBodylanguage());
            // copy the temporary file content back to the original file
            commitTempFile();
        } catch (CmsException e) {
            // error during saving, show error dialog
            setParamErrorstack(e.getStackTraceAsString());
            setParamTitle(key("error.title.editorsave"));
            setParamMessage(key("error.message.editorsave"));
            String reasonSuggestion = key("error.reason.editorsave") + "<br>\n" + key("error.suggestion.editorsave") + "\n";
            setParamReasonSuggestion(reasonSuggestion);
            getJsp().include(C_FILE_DIALOG_SCREEN_ERROR);
        }
        // now escape the title parameter in case the editor is re-displayed
        setParamPagetitle(Encoder.escapeXml(getParamPagetitle()));
    }
    
    /**
     * Saves the editor content to the temporary file.<p>
     * 
     * @param body the body name to write
     * @param language the body language to write
     * @throws CmsException if something goes wrong
     */
    protected void performSaveContent(String body, String language) throws CmsException {
        // prepare the content for saving
        String content = prepareContent(true);
        if (!m_page.hasElement(body, language)) {
            m_page.addElement(body, language);
        }
        m_page.setElementData(body, language, content.getBytes());
        getCms().writeFile(m_page.marshal());
    }
    
    /**
     * Manipulates the content String for the different editor views and the save operation.<p>
     * 
     * @param save if set to true, the result String is not escaped and the content parameter is not updated
     * @return the prepared content String
     */
    protected abstract String prepareContent(boolean save);

}