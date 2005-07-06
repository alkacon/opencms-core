/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/workplace/editors/CmsDefaultPageEditor.java,v $
 * Date   : $Date: 2005/07/06 12:45:07 $
 * Version: $Revision: 1.21 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Mananagement System
 *
 * Copyright (c) 2005 Alkacon Software GmbH (http://www.alkacon.com)
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
 * For further information about Alkacon Software GmbH, please see the
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
import org.opencms.file.CmsPropertyDefinition;
import org.opencms.file.CmsRequestContext;
import org.opencms.file.CmsResourceFilter;
import org.opencms.i18n.CmsEncoder;
import org.opencms.i18n.CmsLocaleManager;
import org.opencms.jsp.CmsJspActionElement;
import org.opencms.main.CmsException;
import org.opencms.main.CmsLog;
import org.opencms.main.OpenCms;
import org.opencms.util.CmsHtmlConverter;
import org.opencms.util.CmsStringUtil;
import org.opencms.xml.page.CmsXmlPage;
import org.opencms.xml.page.CmsXmlPageFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Properties;

import javax.servlet.ServletException;
import javax.servlet.jsp.JspException;

import org.apache.commons.logging.Log;

/**
 * Provides methods for building editors for the CmsDefaultPage page type.<p> 
 * 
 * Extend this class for all editors that work with the CmsDefaultPage.<p>
 *
 * @author  Andreas Zahner 
 * 
 * @version $Revision: 1.21 $ 
 * 
 * @since 6.0.0 
 */
public abstract class CmsDefaultPageEditor extends CmsEditor {

    /** Parameter name for the request parameter "element name". */
    public static final String PARAM_ELEMENTNAME = "elementname";

    /** Parameter name for the request parameter "old element name". */
    public static final String PARAM_OLDELEMENTNAME = "oldelementname";

    /** Name of the special body element from an XMLTemplate. */
    public static final String XML_BODY_ELEMENT = "body";

    /** The log object for this class. */
    private static final Log LOG = CmsLog.getLog(CmsDefaultPageEditor.class);

    /** File object used to read and write contents. */
    protected CmsFile m_file;

    /** Page object used from the action and init methods, be sure to initialize this e.g. in the initWorkplaceRequestValues method. */
    protected CmsXmlPage m_page;

    private List m_elementList;

    /** The element locale. */
    private Locale m_elementLocale;

    private String m_paramElementname;
    private String m_paramOldelementname;

    /** option values for font select boxes. */
    public static final String[] SELECTBOX_FONTS = {
        "Arial",
        "Arial Narrow",
        "System",
        "Times New Roman",
        "Verdana",
        "Monospace",
        "SansSerif"};

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
            Locale oldLocale = CmsLocaleManager.getLocale(getParamOldelementlanguage());
            performSaveContent(getParamOldelementname(), oldLocale);
        } catch (CmsException e) {
            // show error page
            try {
                showErrorPage(this, e);
            } catch (JspException exc) {
                // should usually never happen
                if (LOG.isInfoEnabled()) {
                    LOG.info(exc);
                }
            }
        }
        // re-initialize the element name if the language has changed
        if (!getParamElementlanguage().equals(getParamOldelementlanguage())) {
            initBodyElementName(getParamOldelementname());
        }
        // get the new editor content 
        initContent();
    }

    /**
     * Performs the cleanup body action of the editor.<p>
     */
    public void actionCleanupBodyElement() {

        try {
            // save eventually changed content of the editor to the temporary file
            Locale oldLocale = CmsLocaleManager.getLocale(getParamOldelementlanguage());
            performSaveContent(getParamOldelementname(), oldLocale);
        } catch (CmsException e) {
            // show error page
            try {
                showErrorPage(this, e);
            } catch (JspException exc) {
                // should usually never happen
                if (LOG.isInfoEnabled()) {
                    LOG.info(exc);
                }
            }
        }
    }

    /**
     * @see org.opencms.workplace.editors.CmsEditor#actionClear(boolean)
     */
    public void actionClear(boolean forceUnlock) {

        // delete the temporary file        
        deleteTempFile();
        boolean directEditMode = Boolean.valueOf(getParamDirectedit()).booleanValue();
        boolean modified = Boolean.valueOf(getParamModified()).booleanValue();
        if (directEditMode || forceUnlock || !modified) {
            // unlock the resource when in direct edit mode, force unlock is true or resource was not modified
            try {
                getCms().unlockResource(getParamResource());
            } catch (CmsException e) {
                // should usually never happen
                if (LOG.isInfoEnabled()) {
                    LOG.info(e);
                }
            }
        }
    }

    /**
     * Performs a configurable action performed by the editor.<p>
     * 
     * The default action is: save resource, clear temporary files and publish the resource directly.<p>
     * 
     * @throws IOException if a forward fails
     * @throws JspException if including a JSP fails
     * @throws ServletException if a forward fails
     */
    public void actionDirectEdit() throws IOException, JspException, ServletException {

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
     * @see org.opencms.workplace.editors.CmsEditor#actionExit()
     */
    public void actionExit() throws IOException, JspException, ServletException {

        if (getAction() == ACTION_CANCEL) {
            // save and exit was canceled
            return;
        }
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

        try {
            // save content of the editor to the temporary file
            performSaveContent(getParamElementname(), getElementLocale());
        } catch (CmsException e) {
            // show error page
            showErrorPage(this, e);
        }

        // redirect to the temporary file with current active element language
        String param = "?" + org.opencms.i18n.CmsLocaleManager.PARAMETER_LOCALE + "=" + getParamElementlanguage();
        sendCmsRedirect(getParamTempfile() + param);
    }

    /**
     * @see org.opencms.workplace.editors.CmsEditor#actionSave()
     */
    public void actionSave() throws JspException {

        try {

            // save content to temporary file
            performSaveContent(getParamElementname(), getElementLocale());
            // copy the temporary file content back to the original file
            commitTempFile();
            // set the modified parameter
            setParamModified(Boolean.TRUE.toString());
        } catch (CmsException e) {
            showErrorPage(e);
        }

        if (getAction() != ACTION_CANCEL) {
            // save successful, set save action         
            setAction(ACTION_SAVE);
        }
    }

    /**
     * Builds the html String for the gallery buttons.<p>
     * 
     * @param options the display configuration for the editor
     * @param buttonStyle the style for the buttons
     * @param displayOptions the display options for the editor
     * @return the html String for the gallery buttons
     */
    public abstract String buildGalleryButtons(
        CmsEditorDisplayOptions options,
        int buttonStyle,
        Properties displayOptions);

    /**
     * Builds the html String for the element language selector.<p>
     *  
     * @param attributes optional attributes for the &lt;select&gt; tag
     * @return the html for the element language selectbox
     */
    public String buildSelectElementLanguage(String attributes) {

        return buildSelectElementLanguage(attributes, getParamTempfile(), getElementLocale());
    }

    /**
     * Builds the html String for the element name selector.<p>
     *  
     * @param attributes optional attributes for the &lt;select&gt; tag
     * @return the html for the element name selectbox
     */
    public String buildSelectElementName(String attributes) {

        // get the active page elements
        List elementList = getElementList();

        int counter = 0;
        int currentIndex = -1;
        List options = new ArrayList(elementList.size());
        List values = new ArrayList(elementList.size());
        String elementName = getParamElementname();
        if (CmsStringUtil.isEmpty(elementName)) {
            elementName = getParamOldelementname();
        }
        for (int i = 0; i < elementList.size(); i++) {
            // get the current list element
            CmsDialogElement element = (CmsDialogElement)elementList.get(i);

            if (CmsStringUtil.isNotEmpty(elementName) && elementName.equals(element.getName())) {
                // current element is the displayed one, mark it as selected
                currentIndex = counter;
            }
            if ((!m_page.hasValue(element.getName(), getElementLocale()) && element.isMandantory())
                || m_page.isEnabled(element.getName(), getElementLocale())) {
                // add element if it is not available or if it is enabled
                options.add(element.getNiceName());
                values.add(element.getName());
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
        for (int i = 0; i < CmsDefaultPageEditor.SELECTBOX_FONTS.length; i++) {
            String value = CmsDefaultPageEditor.SELECTBOX_FONTS[i];
            names.add(value);
        }
        return buildSelect(attributes, names, names, -1, false);
    }

    /**
     * Escapes the content and title parameters to display them in the editor form.<p>
     * 
     * This method has to be called on the JSP right before the form display html is created.<p>     *
     */
    public void escapeParams() {

        // escape the content
        setParamContent(CmsEncoder.escapeWBlanks(getParamContent(), CmsEncoder.ENCODING_UTF_8));
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
     * Returns the current element name.<p>
     * 
     * @return the current element name
     */
    public String getParamElementname() {

        return m_paramElementname;
    }

    /**
     * Returns the old element name.<p>
     * 
     * @return the old element name
     */
    public String getParamOldelementname() {

        return m_paramOldelementname;
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
            if (!"".equals(currentTemplate)) {
                // read the stylesheet from the template file
                result = getCms().readPropertyObject(currentTemplate, CmsPropertyDefinition.PROPERTY_TEMPLATE, false).getValue(
                    "");
            }
        } catch (CmsException e) {
            LOG.warn(Messages.get().key(Messages.LOG_READ_TEMPLATE_PROP_STYLESHEET_FAILED_0), e);
        }
        return result;
    }

    /**
     * Returns the OpenCms VFS uri of the template of the current page.<p>
     * 
     * @return the OpenCms VFS uri of the template of the current page
     */
    public String getUriTemplate() {

        String result = "";
        try {
            result = getCms().readPropertyObject(getParamTempfile(), CmsPropertyDefinition.PROPERTY_TEMPLATE, true).getValue(
                "");
        } catch (CmsException e) {
            LOG.warn(Messages.get().key(Messages.LOG_READ_TEMPLATE_PROP_FAILED_0), e);
        }
        return result;
    }

    /**
     * Sets the current element name.<p>
     * 
     * @param elementName the current element name
     */
    public void setParamElementname(String elementName) {

        m_paramElementname = elementName;
    }

    /**
     * Sets the old element name.<p>
     * 
     * @param oldElementName the old element name
     */
    public void setParamOldelementname(String oldElementName) {

        m_paramOldelementname = oldElementName;
    }

    /**
     * Returns the list of active elements of the page.<p>
     * 
     * @return the list of active elements of the page
     */
    protected List getElementList() {

        if (m_elementList == null) {
            m_elementList = CmsDialogElements.computeElements(getCms(), m_page, getParamTempfile(), getElementLocale());
        }
        return m_elementList;
    }

    /**
     * Initializes the body element language for the first call of the editor.<p>
     */
    protected void initBodyElementLanguage() {

        List locales = m_page.getLocales();
        Locale defaultLocale = (Locale)OpenCms.getLocaleManager().getDefaultLocales(
            getCms(),
            getCms().getSitePath(m_file)).get(0);

        if (locales.size() == 0) {
            // no body present, create default body
            if (!m_page.hasValue(CmsDefaultPageEditor.XML_BODY_ELEMENT, defaultLocale)) {
                m_page.addValue(CmsDefaultPageEditor.XML_BODY_ELEMENT, defaultLocale);
            }
            try {
                m_file.setContents(m_page.marshal());
                getCms().writeFile(m_file);
            } catch (CmsException e) {
                // show error page
                try {
                    showErrorPage(this, e);
                } catch (JspException exc) {
                    // should usually never happen
                    if (LOG.isInfoEnabled()) {
                        LOG.info(exc);
                    }
                }
            }
            setParamElementlanguage(defaultLocale.toString());
        } else {
            // body present, get the language
            if (locales.contains(defaultLocale)) {
                // get the body for the default language
                setParamElementlanguage(defaultLocale.toString());
            } else {
                // get the first body that can be found
                setParamElementlanguage(locales.get(0).toString());
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

        if (elementName == null
            || (m_page.hasValue(elementName, getElementLocale()) && !m_page.isEnabled(elementName, getElementLocale()))) {
            // elementName not specified or given element is disabled, determine default element
            List allElements = m_page.getNames(getElementLocale());
            int elementCount = allElements.size();
            List elements = new ArrayList(elementCount);
            for (int i = 0; i < elementCount; i++) {
                // filter disabled elements
                if (m_page.isEnabled((String)allElements.get(i), getElementLocale())) {
                    elements.add(allElements.get(i));
                }
            }

            // get the active page elements
            List elementList = getElementList();
            for (int i = 0; i < elementList.size(); i++) {
                CmsDialogElement checkElement = (CmsDialogElement)elementList.get(i);
                if (elements.contains(checkElement.getName())) {
                    // get the first active element from the element list
                    setParamElementname(checkElement.getName());
                    return;
                }
            }

            // no matching active element found
            if (elements.contains(CmsDefaultPageEditor.XML_BODY_ELEMENT)) {
                // default legacy element present, use it
                setParamElementname(CmsDefaultPageEditor.XML_BODY_ELEMENT);
            } else {
                // use the first element from the element list 
                setParamElementname((String)elements.get(0));
            }
        } else {
            // elementName specified and element is enabled or not present, set to elementName
            setParamElementname(elementName);
        }
    }

    /** 
     * This method has to be called after initializing the body element name and language.<p>
     * 
     * @see org.opencms.workplace.editors.CmsEditor#initContent()
     */
    protected void initContent() {

        if (CmsStringUtil.isNotEmpty(getParamContent())) {
            if (CmsStringUtil.isNotEmpty(getParamElementname())
                && getParamElementname().equals(getParamOldelementname())) {
                if (CmsStringUtil.isNotEmpty(getParamElementlanguage())
                    && getParamElementlanguage().equals(getParamOldelementlanguage())) {
                    return;
                }
            }
        }
        // get the content from the temporary file        
        try {
            CmsXmlPage page = CmsXmlPageFactory.unmarshal(getCms(), getCms().readFile(
                getParamTempfile(),
                CmsResourceFilter.ALL));
            getCms().getRequestContext().setAttribute(CmsRequestContext.ATTRIBUTE_EDITOR, new Boolean(true));
            String elementData = page.getStringValue(getCms(), getParamElementname(), getElementLocale());
            if (elementData != null) {
                setParamContent(elementData);
            } else {
                setParamContent("");
            }
        } catch (CmsException e) {
            // reading of file contents failed, show error page
            try {
                showErrorPage(this, e);
            } catch (JspException exc) {
                // should usually never happen
                if (LOG.isInfoEnabled()) {
                    LOG.info(exc);
                }
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

        String contentConversion = m_page.getConversion();
        // check if cleanup was selected in the editor, we have to add the cleanup parameter
        if (EDITOR_CLEANUP.equals(getParamAction())) {
            if ((contentConversion == null) || (contentConversion.equals(CmsHtmlConverter.PARAM_DISABLED))) {
                // if the current conversion mode is "false" only, we have to remove the "false" value and set it to "cleanup", as "false" will be stronger than all other values
                contentConversion = CmsHtmlConverter.PARAM_WORD;
            } else {
                // add "cleanup" to the already existing values
                contentConversion += ";" + CmsHtmlConverter.PARAM_WORD;
            }
        }
        m_page.setConversion(contentConversion);

        // create the element if necessary and if content is present
        if (!m_page.hasValue(body, locale) && !"".equals(content)) {
            m_page.addValue(body, locale);
        }

        // get the enabled state of the element
        boolean enabled = m_page.isEnabled(body, locale);

        // set the element data
        if (m_page.hasValue(body, locale)) {
            m_page.setStringValue(getCms(), body, locale, content);
        }

        // write the file
        m_file.setContents(m_page.marshal());
        m_file = getCms().writeFile(m_file);

        // content might have been modified during write operation
        m_page = CmsXmlPageFactory.unmarshal(getCms(), m_file);
        if (m_page.hasValue(body, locale)) {
            getCms().getRequestContext().setAttribute(CmsRequestContext.ATTRIBUTE_EDITOR, new Boolean(true));
            content = m_page.getStringValue(getCms(), body, locale);
            if (content == null) {
                content = "";
            }
            setParamContent(content);
            prepareContent(false);
            m_page.setEnabled(body, locale, enabled);
        }
    }

    /**
     * Manipulates the content String for different editor views and the save operation.<p>
     * 
     * @param save if set to true, the result String is not escaped and the content parameter is not updated
     * @return the prepared content String
     */
    protected abstract String prepareContent(boolean save);

}