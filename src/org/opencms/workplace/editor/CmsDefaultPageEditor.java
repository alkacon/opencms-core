/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/workplace/editor/Attic/CmsDefaultPageEditor.java,v $
 * Date   : $Date: 2004/01/16 16:52:00 $
 * Version: $Revision: 1.19 $
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
import com.opencms.file.CmsFile;
import com.opencms.flex.jsp.CmsJspActionElement;
import com.opencms.util.Encoder;
import com.opencms.workplace.I_CmsWpConstants;

import org.opencms.main.OpenCms;
import org.opencms.page.CmsXmlPage;
import org.opencms.workplace.CmsWorkplaceAction;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import javax.servlet.jsp.JspException;

/**
 * Provides methods for building editors for the CmsDefaultPage page type.<p> 
 * 
 * Extend this class for all editors that work with the CmsDefaultPage.<p>
 *
 * @author  Andreas Zahner (a.zahner@alkacon.com)
 * @version $Revision: 1.19 $
 * 
 * @since 5.1.12
 */
public abstract class CmsDefaultPageEditor extends CmsEditor {
    
    /** Constant for the customizable action button */
    public static final String EDITOR_SAVEACTION = "saveaction";
    
    /** Constant value for the customizable action button */
    public static final int ACTION_SAVEACTION = 200;
    
    private String m_paramBodylanguage;
    private String m_paramBodyname;
    private String m_paramOldbodylanguage;
    private String m_paramOldbodyname; 
    private String m_paramBackLink;

    /** Page object used from the action and init methods, be sure to initialize this e.g. in the initWorkplaceRequestValues method */
    protected CmsXmlPage m_page;
    
    /** File object used to read and write contents */
    protected CmsFile m_file;
      
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
    public final String getParamBodylanguage() {
        return m_paramBodylanguage;
    }

    /**
     * Sets the current body element language.<p>
     * 
     * @param bodyLanguage the current body element language
     */
    public final void setParamBodylanguage(String bodyLanguage) {
        m_paramBodylanguage = bodyLanguage;
    }

    /**
     * Returns the current body element name.<p>
     * 
     * @return the current body element name
     */
    public final String getParamBodyname() {
        return m_paramBodyname;
    }

    /**
     * Sets the current body element name.<p>
     * 
     * @param bodyName the current body element name
     */
    public final void setParamBodyname(String bodyName) {
        m_paramBodyname = bodyName;
    }

    /**
    * Returns the old body element language.<p>
    * 
    * @return the old body element language
    */
   public final String getParamOldbodylanguage() {
       return m_paramOldbodylanguage;
   }

   /**
    * Sets the old body element language.<p>
    * 
    * @param oldBodyLanguage the old body element language
    */
   public final void setParamOldbodylanguage(String oldBodyLanguage) {
       m_paramOldbodylanguage = oldBodyLanguage;
   }

   /**
    * Returns the old body element name.<p>
    * 
    * @return the old body element name
    */
   public final String getParamOldbodyname() {
       return m_paramOldbodyname;
   }

   /**
    * Sets the old body element name.<p>
    * 
    * @param oldBodyName the old body element name
    */
   public final void setParamOldbodyname(String oldBodyName) {
       m_paramOldbodyname = oldBodyName;
   }
    
    /**
     * Returns the back link when closing the editor.<p>
     * 
     * @return the back link
     */
    public final String getParamBacklink() {
        if (m_paramBackLink == null) {
            m_paramBackLink = "";
        }
        return m_paramBackLink;
    }
    
    /**
     * Sets the back link when closing the editor.<p>
     * 
     * @param backLink the back link
     */
    public final void setParamBacklink(String backLink) {
        m_paramBackLink = backLink;
    }  
    
    /**
     * Escapes the content and title parameters to display them in the editor form.<p>
     * 
     * This method has to be called on the JSP right before the form display html is created.<p>     *
     */
    public void escapeParams() {
        // escape the content
        setParamContent(Encoder.escapeWBlanks(getParamContent(), Encoder.C_UTF8_ENCODING));
    }
    
    /**
     * Initializes the body element language for the first call of the editor.<p>
     */
    protected void initBodyElementLanguage() {
        Set languages = m_page.getLanguages();
        String defaultLanguage = OpenCms.getUserDefaultLanguage();
        if (languages.size() == 0) {
            // no body present, create default body
            if (!m_page.hasElement(I_CmsConstants.C_XML_BODY_ELEMENT, defaultLanguage)) {
                m_page.addElement(I_CmsConstants.C_XML_BODY_ELEMENT, defaultLanguage);
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
     */
    protected void initBodyElementName() {
        // set the initial body element name
       List bodies = m_page.getNames(getParamBodylanguage());
       if (bodies.size() == 0) {
           // no body present, so create an empty default body
            m_page.addElement(I_CmsConstants.C_XML_BODY_ELEMENT, getParamBodylanguage());
            try {
            getCms().writeFile(m_page.write(m_file));
            } catch (CmsException e) {
                // writing file failed, show error page
                try {
                showErrorPage(this, e, "save");
                } catch (JspException exc) {
                    // ignore this exception
                }
            }
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
            CmsXmlPage page = CmsXmlPage.read(getCms(), getCms().readFile(this.getParamTempfile()));
            String elementData = page.getContent(getCms(), getParamBodyname(), getParamBodylanguage(), true);
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
     */
    public String buildSelectBodyLanguage(String attributes) {
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
     */
    public String buildSelectBodyName(String attributes) {
        List elementList = null;
        try { 
            elementList = CmsDialogElements.computeElements(getCms(), getParamTempfile());
        } catch (CmsException e) {
            // no property found, display all present elements instead
            List bodies = m_page.getNames(getParamBodylanguage());
            Collections.sort(bodies);
            int currentIndex = bodies.indexOf(getParamBodyname());   
            if (currentIndex == -1) {
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
            if (m_page.isEnabled(elementName, getParamBodylanguage())) {
                // add element only if it is enabled
                options.add(elementNice);
                values.add(elementName);
                counter++;
            }
        } 
        return buildSelect(attributes, options, values, currentIndex, false);
    }
    
    /**
     * Builds the html to display the special action button for the direct edit mode of the editor.<p>
     * 
     * @param jsFunction the JavaScript function which will be executed on the mouseup event 
     * @return the html to display the special action button
     */
    public String buttonActionDirectEdit(String jsFunction) {
        //StringBuffer retValue = new StringBuffer(256);
        // get the action class from the OpenCms runtime property
        I_CmsEditorActionHandler actionClass = (I_CmsEditorActionHandler)OpenCms.getRuntimeProperty(I_CmsEditorActionHandler.EDITOR_ACTION);
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
            return button("javascript:" + jsFunction, null, image, name, 0, url.substring(0, url.lastIndexOf("/") + 1));
            //retValue.append("<a href=\"#\" onMouseOver=\"" + nameValue + ".className='over';\" ");
            //retValue.append("onMouseOut=\"" + nameValue + ".className='norm';\" onMouseDown=\"" + nameValue + ".className='push';\" ");
            //retValue.append("onMouseUp=\"" + nameValue + ".className='over';" + jsFunction + ";\">");
        } else {
            return button(null, null, image, name + "_in", 0, url.substring(0, url.lastIndexOf("/") + 1));
        }
        
//        // build the button html
//        retValue.append("<img src=\"" + url + "\" ");
//        retValue.append("width=\"20\" height=\"20\" ");
//        if (nameValue != null) {
//            retValue.append("name=\"" + nameValue + "\" ");
//        }
//        retValue.append("alt=\"" + name + "\" ");
//        retValue.append("title=\"" + name + "\">");
//        
//        if (active) {
//            // close the link
//            retValue.append("</a>");
//        }
//        
//        return retValue.toString();
    }
    
    /**
     * Performs the change body action of the editor.<p>
     */
    public void actionChangeBodyElement() {
        try {
            // save eventually changed content of the editor to the temporary file
            performSaveContent(getParamOldbodyname(), getParamOldbodylanguage());
        } catch (CmsException e) {
            // show error page
            try {
            showErrorPage(this, e, "save");
            } catch (JspException exc) {
                // ignore this exception
            }
        }
        // re-initialize the body element name if the language has changed
        if (!getParamBodylanguage().equals(getParamOldbodylanguage())) {
            initBodyElementName();
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
     * Performs a configurable action performed by the editor.<p>
     * 
     * The default action is: save resource, clear temporary files and publish the resource directly.<p>
     * 
     * @throws IOException if a redirection fails
     * @throws JspException if including a JSP fails
     */
    public void actionDirectEdit() throws IOException, JspException {
        // get the action class from the OpenCms runtime property
        I_CmsEditorActionHandler actionClass = (I_CmsEditorActionHandler)OpenCms.getRuntimeProperty(I_CmsEditorActionHandler.EDITOR_ACTION);
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
    public void actionExit() throws IOException {
        // clear temporary file and unlock resource, if in directedit mode
        actionClear(false);
        if ("true".equals(getParamDirectedit())) {
            // editor is in direct edit mode
            if (!"".equals(getParamBacklink())) {
                // redirect to the specified back link target
                getJsp().getResponse().sendRedirect(getJsp().link(getParamBacklink()));
            } else {
                // redirect to the edited resource
                getJsp().getResponse().sendRedirect(getJsp().link(getParamResource()));
            }
        } else {
            // redirect to the workplace explorer view 
            getJsp().getResponse().sendRedirect(getJsp().link(CmsWorkplaceAction.C_JSP_WORKPLACE_URI));
        }
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
            performSaveContent(getParamBodyname(), getParamBodylanguage());
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
            performSaveContent(getParamBodyname(), getParamBodylanguage());
            // copy the temporary file content back to the original file
            commitTempFile();
        } catch (CmsException e) {
            // error during saving, show error dialog
            showErrorPage(this, e, "save");
        }
    }
    
    /**
     * Saves the editor content to the temporary file.<p>
     * 
     * @param body the body name to write
     * @param language the body language to write
     * @throws CmsException if writing the file fails
     */
    protected void performSaveContent(String body, String language) throws CmsException {
        // prepare the content for saving
        String content = prepareContent(true);
        boolean enabled = m_page.isEnabled(body, language);

        // create the element if necessary
        if (!m_page.hasElement(body, language)) {
            m_page.addElement(body, language);
        }
        // set the element data
        m_page.setContent(getCms(), body, language, content);
        m_page.setEnabled(body, language, enabled);
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

}