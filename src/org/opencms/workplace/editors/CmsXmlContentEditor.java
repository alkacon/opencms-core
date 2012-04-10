/*
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (c) Alkacon Software GmbH (http://www.alkacon.com)
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
import org.opencms.file.CmsRequestContext;
import org.opencms.file.CmsResource;
import org.opencms.file.CmsResourceFilter;
import org.opencms.file.collectors.A_CmsResourceCollector;
import org.opencms.file.collectors.I_CmsResourceCollector;
import org.opencms.i18n.CmsEncoder;
import org.opencms.i18n.CmsLocaleManager;
import org.opencms.json.JSONArray;
import org.opencms.json.JSONException;
import org.opencms.json.JSONObject;
import org.opencms.jsp.CmsJspActionElement;
import org.opencms.lock.CmsLockType;
import org.opencms.main.CmsException;
import org.opencms.main.CmsLog;
import org.opencms.main.OpenCms;
import org.opencms.util.CmsRequestUtil;
import org.opencms.util.CmsStringUtil;
import org.opencms.widgets.A_CmsWidget;
import org.opencms.widgets.I_CmsWidget;
import org.opencms.widgets.I_CmsWidgetDialog;
import org.opencms.widgets.I_CmsWidgetParameter;
import org.opencms.workplace.CmsWorkplace;
import org.opencms.workplace.CmsWorkplaceSettings;
import org.opencms.workplace.editors.directedit.CmsDirectEditButtonSelection;
import org.opencms.xml.CmsXmlContentDefinition;
import org.opencms.xml.CmsXmlEntityResolver;
import org.opencms.xml.CmsXmlException;
import org.opencms.xml.CmsXmlUtils;
import org.opencms.xml.content.CmsXmlContent;
import org.opencms.xml.content.CmsXmlContentErrorHandler;
import org.opencms.xml.content.CmsXmlContentFactory;
import org.opencms.xml.content.CmsXmlContentTab;
import org.opencms.xml.content.CmsXmlContentValueSequence;
import org.opencms.xml.types.CmsXmlNestedContentDefinition;
import org.opencms.xml.types.I_CmsXmlContentValue;
import org.opencms.xml.types.I_CmsXmlSchemaType;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.jsp.JspException;

import org.apache.commons.logging.Log;

/**
 * Creates the editor for XML content definitions.<p> 
 * 
 * @since 6.0.0 
 */
public class CmsXmlContentEditor extends CmsEditor implements I_CmsWidgetDialog {

    /** Action for checking content before executing the direct edit action. */
    public static final int ACTION_CHECK = 151;

    /** Action for confirming the XML content structure correction. */
    public static final int ACTION_CONFIRMCORRECTION = 152;

    /** Value for the action: copy the current locale. */
    public static final int ACTION_COPYLOCALE = 141;

    /** Action for correction of the XML content structure confirmed. */
    public static final int ACTION_CORRECTIONCONFIRMED = 153;

    /** Action for optional element creation. */
    public static final int ACTION_ELEMENT_ADD = 154;

    /** Action for element move down operation. */
    public static final int ACTION_ELEMENT_MOVE_DOWN = 155;

    /** Action for element move up operation. */
    public static final int ACTION_ELEMENT_MOVE_UP = 156;

    /** Action for optional element removal. */
    public static final int ACTION_ELEMENT_REMOVE = 157;

    /** Action for new file creation. */
    public static final int ACTION_NEW = 158;

    /** Action that sub choices should be determined. */
    public static final int ACTION_SUBCHOICES = 159;

    /** Indicates that the content should be checked before executing the direct edit action. */
    public static final String EDITOR_ACTION_CHECK = "check";

    /** Indicates that the correction of the XML content structure should be confirmed. */
    public static final String EDITOR_ACTION_CONFIRMCORRECTION = "confirmcorrect";

    /** Indicates an optional element should be created. */
    public static final String EDITOR_ACTION_ELEMENT_ADD = "addelement";

    /** Indicates an element should be moved down. */
    public static final String EDITOR_ACTION_ELEMENT_MOVE_DOWN = "elementdown";

    /** Indicates an element should be moved up. */
    public static final String EDITOR_ACTION_ELEMENT_MOVE_UP = "elementup";

    /** Indicates an optional element should be removed. */
    public static final String EDITOR_ACTION_ELEMENT_REMOVE = "removeelement";

    /** Indicates a new file should be created. */
    public static final String EDITOR_ACTION_NEW = CmsDirectEditButtonSelection.VALUE_NEW;

    /** Indicates that sub choices should be determined. */
    public static final String EDITOR_ACTION_SUBCHOICES = "subchoices";

    /** Indicates that the contents of the current locale should be copied to other locales. */
    public static final String EDITOR_COPYLOCALE = "copylocale";

    /** Indicates that the correction of the XML content structure was confirmed by the user. */
    public static final String EDITOR_CORRECTIONCONFIRMED = "correctconfirmed";

    /** Parameter name for the request parameter "choiceelement". */
    public static final String PARAM_CHOICEELEMENT = "choiceelement";

    /** Parameter name for the request parameter "choicetype". */
    public static final String PARAM_CHOICETYPE = "choicetype";

    /** Parameter name for the request parameter "elementindex". */
    public static final String PARAM_ELEMENTINDEX = "elementindex";

    /** Parameter name for the request parameter "elementname". */
    public static final String PARAM_ELEMENTNAME = "elementname";

    /** Parameter name for the request parameter "newlink". */
    public static final String PARAM_NEWLINK = "newlink";

    /** Constant for the editor type, must be the same as the editors subfolder name in the VFS. */
    private static final String EDITOR_TYPE = "xmlcontent";

    /** The log object for this class. */
    private static final Log LOG = CmsLog.getLog(CmsXmlContentEditor.class);

    /** The content object to edit. */
    private CmsXmlContent m_content;

    /** The currently active tab during form generation. */
    private CmsXmlContentTab m_currentTab;

    /** The currently active tab index during form generation. */
    private int m_currentTabIndex;

    /** The element locale. */
    private Locale m_elementLocale;

    /** The list of tabs that have an element with an error. */
    private List<CmsXmlContentTab> m_errorTabs;

    /** File object used to read and write contents. */
    private CmsFile m_file;

    /** The set of help message IDs that have already been used. */
    private Set<String> m_helpMessageIds;

    /** Indicates if an optional element is included in the form. */
    private boolean m_optionalElementPresent;

    /** Parameter stores the name of the choice element to add. */
    private String m_paramChoiceElement;

    /** Parameter stores the flag if the element to add is a choice type. */
    private String m_paramChoiceType;

    /** Parameter stores the index of the element to add or remove. */
    private String m_paramElementIndex;

    /** Parameter stores the name of the element to add or remove. */
    private String m_paramElementName;

    /** The selected model file for the new resource. */
    private String m_paramModelFile;

    /** Parameter to indicate if a new XML content resource should be created. */
    private String m_paramNewLink;

    /** The error handler for the xml content. */
    private CmsXmlContentErrorHandler m_validationHandler;

    /** The list of tabs that have an element with a warning. */
    private List<CmsXmlContentTab> m_warningTabs;

    /** Visitor implementation that stored the widgets for the content.  */
    private CmsXmlContentWidgetVisitor m_widgetCollector;

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

        // save eventually changed content of the editor
        Locale oldLocale = CmsLocaleManager.getLocale(getParamOldelementlanguage());
        Locale newLocale = getElementLocale();
        try {
            setEditorValues(oldLocale);
            if (!m_content.validate(getCms()).hasErrors(oldLocale)) {
                // no errors found in content
                if (!m_content.hasLocale(newLocale)) {
                    // check if we should copy the content from a default locale
                    boolean addNew = true;
                    List<Locale> locales = OpenCms.getLocaleManager().getDefaultLocales(getCms(), getParamResource());
                    if (locales.size() > 1) {
                        // default locales have been set, try to find a match
                        try {
                            m_content.copyLocale(locales, newLocale);
                            addNew = false;
                        } catch (CmsXmlException e) {
                            // no matching default locale was available, we will create a new one later
                        }
                    }
                    if (addNew) {
                        // create new element if selected language element is not present
                        try {
                            m_content.addLocale(getCms(), newLocale);
                        } catch (CmsXmlException e) {
                            if (LOG.isErrorEnabled()) {
                                LOG.error(e.getLocalizedMessage(), e);
                            }
                        }
                    }
                }
                //save to temporary file              
                writeContent();
                // set default action to suppress error messages
                setAction(ACTION_DEFAULT);
            } else {
                // errors found, switch back to old language to show errors
                setParamElementlanguage(getParamOldelementlanguage());
                // set stored locale to null to reinitialize it
                m_elementLocale = null;
            }
        } catch (Exception e) {
            // should usually never happen
            if (LOG.isInfoEnabled()) {
                LOG.info(e.getLocalizedMessage(), e);
            }
        }
    }

    /**
     * Deletes the temporary file and unlocks the edited resource when in direct edit mode.<p>
     * 
     * @param forceUnlock if true, the resource will be unlocked anyway
     */
    @Override
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
                    LOG.info(e.getLocalizedMessage(), e);
                }
            }
        }
    }

    /**
     * Performs the copy locale action.<p>
     * 
     * @throws JspException if something goes wrong
     */
    public void actionCopyElementLocale() throws JspException {

        try {
            setEditorValues(getElementLocale());
            if (!hasValidationErrors()) {
                // save content of the editor only to the temporary file
                writeContent();
                // remove eventual release & expiration date from temporary file to make preview work
                getCms().setDateReleased(getParamTempfile(), CmsResource.DATE_RELEASED_DEFAULT, false);
                getCms().setDateExpired(getParamTempfile(), CmsResource.DATE_EXPIRED_DEFAULT, false);
            }
        } catch (CmsException e) {
            // show error page
            showErrorPage(this, e);
        }
    }

    /**
     * Performs the delete locale action.<p>
     * 
     * @throws JspException if something goes wrong
     */
    public void actionDeleteElementLocale() throws JspException {

        try {
            Locale loc = getElementLocale();
            m_content.removeLocale(loc);
            //write the modified xml content
            writeContent();
            List<Locale> locales = m_content.getLocales();
            if (locales.size() > 0) {
                // set first locale as new display locale
                Locale newLoc = locales.get(0);
                setParamElementlanguage(newLoc.toString());
                m_elementLocale = newLoc;
            } else {
                if (LOG.isErrorEnabled()) {
                    LOG.error(Messages.get().getBundle().key(Messages.LOG_GET_LOCALES_1, getParamResource()));
                }
            }

        } catch (CmsXmlException e) {
            // an error occurred while trying to delete the locale, stop action
            showErrorPage(e);
        } catch (CmsException e) {
            // should usually never happen
            if (LOG.isInfoEnabled()) {
                LOG.info(e.getLocalizedMessage(), e);
            }
        }
    }

    /**
     * Performs a configurable action performed by the editor.<p>
     * 
     * The default action is: save resource, clear temporary files and publish the resource directly.<p>
     * 
     * @throws IOException if a forward fails
     * @throws ServletException of a forward fails
     * @throws JspException if including a JSP fails
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
     * Performs the exit editor action.<p>
     * 
     * @see org.opencms.workplace.editors.CmsEditor#actionExit()
     */
    @Override
    public void actionExit() throws IOException, JspException, ServletException {

        if (getAction() == ACTION_CANCEL) {
            // save and exit was canceled
            return;
        }
        // unlock resource if we are in direct edit mode
        actionClear(false);
        // close the editor
        actionClose();
    }

    /**
     * Moves an element in the xml content either up or down.<p>
     * 
     * Depends on the given action value.<p>
     * 
     * @throws JspException if including the error page fails
     */
    public void actionMoveElement() throws JspException {

        // set editor values from request
        try {
            setEditorValues(getElementLocale());
        } catch (CmsXmlException e) {
            // an error occurred while trying to set the values, stop action
            showErrorPage(e);
            return;
        }
        // get the necessary parameters to move the element
        int index = 0;
        try {
            index = Integer.parseInt(getParamElementIndex());
        } catch (Exception e) {
            // ignore, should not happen
        }

        // get the value to move
        I_CmsXmlContentValue value = m_content.getValue(getParamElementName(), getElementLocale(), index);

        if (getAction() == ACTION_ELEMENT_MOVE_DOWN) {
            // move down the value
            value.moveDown();
        } else {
            // move up the value
            value.moveUp();
        }
        if (getValidationHandler().hasWarnings(getElementLocale())) {
            // there were warnings for the edited content, reset validation handler to avoid display issues
            resetErrorHandler();
        }
        try {
            // write the modified content to the temporary file
            writeContent();
        } catch (CmsException e) {
            // an error occurred while trying to save
            showErrorPage(e);
        }
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
        String collectorParams = m_paramNewLink.substring(pos + 1);

        String param;
        String templateFileName;

        pos = collectorParams.indexOf(A_CmsResourceCollector.SEPARATOR_TEMPLATEFILE);
        if (pos != -1) {
            // found an explicit template file name to use for the new resource, use it
            param = collectorParams.substring(0, pos);
            templateFileName = collectorParams.substring(pos + A_CmsResourceCollector.SEPARATOR_TEMPLATEFILE.length());
        } else {
            // no template file name was specified, use given resource name as template file
            param = collectorParams;
            templateFileName = getParamResource();
        }

        // get the collector used for calculating the next file name
        I_CmsResourceCollector collector = OpenCms.getResourceManager().getContentCollector(collectorName);
        String newFileName = "";
        try {

            // one resource serves as a "template" for the new resource
            CmsFile templateFile = getCms().readFile(templateFileName, CmsResourceFilter.IGNORE_EXPIRATION);

            CmsXmlContent template = CmsXmlContentFactory.unmarshal(getCloneCms(), templateFile);

            // set the required content locale
            Locale locale = getElementLocale();

            // now create a new XML content based on the templates content definition            
            CmsXmlContent newContent = CmsXmlContentFactory.createDocument(
                getCms(),
                locale,
                template.getEncoding(),
                template.getContentDefinition());

            // IMPORTANT: calculation of the name MUST be done here so the file name is ensured to be valid
            newFileName = collector.getCreateLink(getCms(), collectorName, param);

            boolean useModelFile = false;
            if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(getParamModelFile())) {
                getCms().getRequestContext().setAttribute(CmsRequestContext.ATTRIBUTE_MODEL, getParamModelFile());
                useModelFile = true;
            }
            // now create the resource, fill it with the marshalled XML and write it back to the VFS
            getCms().createResource(newFileName, templateFile.getTypeId());
            // re-read the created resource
            CmsFile newFile = getCms().readFile(newFileName, CmsResourceFilter.ALL);
            if (!useModelFile) {
                newFile.setContents(newContent.marshal());

                // write the file with the updated content
                getCloneCms().writeFile(newFile);
            }
            // wipe out parameters for the editor to ensure proper operation
            setParamNewLink(null);
            setParamAction(null);
            setParamResource(newFileName);
            setAction(ACTION_DEFAULT);

            // create the temporary file to work with
            setParamTempfile(createTempFile());

            // set the member variables for the content 
            m_file = getCms().readFile(getParamTempfile(), CmsResourceFilter.ALL);
            if (!useModelFile) {
                m_content = newContent;
            } else {
                m_content = CmsXmlContentFactory.unmarshal(getCms(), m_file);
            }

        } catch (CmsException e) {
            if (LOG.isErrorEnabled()) {
                LOG.error(Messages.get().getBundle().key(Messages.LOG_CREATE_XML_CONTENT_ITEM_1, m_paramNewLink), e);
            }
            throw new JspException(e);
        } finally {
            try {
                // delete the new file
                getCms().deleteResource(newFileName, CmsResource.DELETE_REMOVE_SIBLINGS);
            } catch (CmsException e) {
                // ignore
            }
        }
    }

    /**
     * Performs the preview XML content action in a new browser window.<p>
     * 
     * @throws IOException if redirect fails
     * @throws JspException if inclusion of error page fails
     */
    public void actionPreview() throws IOException, JspException {

        try {
            // save content of the editor only to the temporary file
            setEditorValues(getElementLocale());
            writeContent();
            // remove eventual release & expiration date from temporary file to make preview work
            getCms().setDateReleased(getParamTempfile(), CmsResource.DATE_RELEASED_DEFAULT, false);
            getCms().setDateExpired(getParamTempfile(), CmsResource.DATE_EXPIRED_DEFAULT, false);
        } catch (CmsException e) {
            // show error page
            showErrorPage(this, e);
        }

        // get preview uri from content handler
        String previewUri = m_content.getHandler().getPreview(getCms(), m_content, getParamTempfile());

        // create locale request parameter
        StringBuffer param = new StringBuffer(8);
        if (previewUri.indexOf('?') != -1) {
            param.append("&");
        } else {
            param.append("?");
        }
        param.append(CmsLocaleManager.PARAMETER_LOCALE);
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
    @Override
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
            if (!hasValidationErrors()) {
                // no errors found, write content and copy temp file contents
                writeContent();
                commitTempFile();
                // set the modified parameter
                setParamModified(Boolean.TRUE.toString());
            }
        } catch (CmsException e) {
            showErrorPage(e);
        }

    }

    /**
     * Adds an optional element to the XML content or removes an optional element from the XML content.<p>
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
            // an error occurred while trying to set the values, stop action
            showErrorPage(e);
            return;
        }

        // get the necessary parameters to add/remove the element
        int index = 0;
        try {
            index = Integer.parseInt(getParamElementIndex());
        } catch (Exception e) {
            // ignore, should not happen
        }

        if (getAction() == ACTION_ELEMENT_REMOVE) {
            // remove the value , first get the value to remove
            I_CmsXmlContentValue value = m_content.getValue(getParamElementName(), getElementLocale(), index);
            m_content.removeValue(getParamElementName(), getElementLocale(), index);
            // check if the value was a choice option and the last one
            if (value.isChoiceOption()
                && (m_content.getSubValues(
                    CmsXmlUtils.removeLastXpathElement(getParamElementName()),
                    getElementLocale()).size() == 0)) {
                // also remove the parent choice type value
                String xpath = CmsXmlUtils.removeLastXpathElement(getParamElementName());
                m_content.removeValue(xpath, getElementLocale(), CmsXmlUtils.getXpathIndexInt(xpath) - 1);
            }
        } else {
            // add the new value after the clicked element
            if (m_content.hasValue(getParamElementName(), getElementLocale())) {
                // when other values are present, increase index to use right position
                index += 1;
            }
            String elementPath = getParamElementName();
            if (CmsStringUtil.isNotEmpty(getParamChoiceElement())) {
                // we have to add a choice element, first check if the element to add itself is part of a choice or not
                boolean choiceType = Boolean.valueOf(getParamChoiceType()).booleanValue();
                I_CmsXmlSchemaType elemType = m_content.getContentDefinition().getSchemaType(elementPath);
                if (!choiceType || (elemType.isChoiceOption() && elemType.isChoiceType())) {
                    // this is a choice option or a nested choice type to add, remove last element name from xpath
                    elementPath = CmsXmlUtils.removeLastXpathElement(elementPath);
                } else {
                    // this is a choice type to add, first create type element
                    m_content.addValue(getCms(), elementPath, getElementLocale(), index);
                    elementPath = CmsXmlUtils.createXpathElement(elementPath, index + 1);
                    // all eventual following elements to create have to be at first position
                    index = 0;
                }
                // check if there are nested choice elements to add
                if (CmsXmlUtils.isDeepXpath(getParamChoiceElement())) {
                    // create all missing elements except the last one
                    String pathToChoice = CmsXmlUtils.removeLastXpathElement(getParamChoiceElement());
                    String newPath = elementPath;
                    while (CmsStringUtil.isNotEmpty(pathToChoice)) {
                        String createElement = CmsXmlUtils.getFirstXpathElement(pathToChoice);
                        newPath = CmsXmlUtils.concatXpath(newPath, createElement);
                        pathToChoice = CmsXmlUtils.isDeepXpath(pathToChoice)
                        ? CmsXmlUtils.removeFirstXpathElement(pathToChoice)
                        : null;
                        I_CmsXmlContentValue newVal = m_content.addValue(getCms(), newPath, getElementLocale(), index);
                        newPath = newVal.getPath();
                        // all eventual following elements to create have to be at first position
                        index = 0;
                    }
                    // create the path to the last choice element
                    elementPath = CmsXmlUtils.concatXpath(
                        newPath,
                        CmsXmlUtils.getLastXpathElement(getParamChoiceElement()));
                } else {
                    // create the path to the choice element
                    elementPath += "/" + getParamChoiceElement();
                }
            }
            // add the value
            m_content.addValue(getCms(), elementPath, getElementLocale(), index);
        }

        if (getValidationHandler().hasWarnings(getElementLocale())) {
            // there were warnings for the edited content, reset validation handler to avoid display issues
            resetErrorHandler();
        }

        try {
            // write the modified content to the temporary file
            writeContent();
        } catch (CmsException e) {
            // an error occurred while trying to save
            showErrorPage(e);
        }

    }

    /**
     * Returns the JSON array with information about the choices of a given element.<p>
     * 
     * The returned array is only filled if the given element has choice options, otherwise an empty array is returned.<br/>
     * Note: the first array element is an object containing information if the element itself is a choice type,
     * the following elements are the choice option items.<p>
     * 
     * @param elementName the element name to check (complete xpath)
     * @param choiceType flag indicating if the given element name represents a choice type or not
     * @param checkChoice flag indicating if the element name should be checked if it is a choice option and choice type
     * 
     * @return the JSON array with information about the choices of a given element
     */
    public JSONArray buildElementChoices(String elementName, boolean choiceType, boolean checkChoice) {

        JSONArray choiceElements = new JSONArray();
        String choiceName = elementName;
        I_CmsXmlSchemaType elemType = m_content.getContentDefinition().getSchemaType(elementName);
        if (checkChoice && elemType.isChoiceOption() && elemType.isChoiceType()) {
            // the element itself is a choice option and again a choice type, remove the last element to get correct choices
            choiceName = CmsXmlUtils.removeLastXpathElement(elementName);
        }
        // use xpath to get choice information        
        if (m_content.hasChoiceOptions(choiceName, getElementLocale())) {
            // we have choice options, first add information about type to create
            JSONObject info = new JSONObject();
            try {
                // put information if element is a choice type
                info.put("choicetype", choiceType);
                choiceElements.put(info);
                // get the available choice options for the choice element
                List<I_CmsXmlSchemaType> options = m_content.getChoiceOptions(choiceName, getElementLocale());
                for (Iterator<I_CmsXmlSchemaType> i = options.iterator(); i.hasNext();) {
                    // add the available element options
                    I_CmsXmlSchemaType type = i.next();
                    JSONObject option = new JSONObject();
                    String key = A_CmsWidget.LABEL_PREFIX
                        + type.getContentDefinition().getInnerName()
                        + "."
                        + type.getName();
                    // add element name, label and help info
                    option.put("name", type.getName());
                    option.put("label", keyDefault(key, type.getName()));
                    option.put("help", keyDefault(key + A_CmsWidget.HELP_POSTFIX, ""));
                    // add info if the choice itself is a (sub) choice type
                    option.put("subchoice", type.isChoiceType());
                    choiceElements.put(option);
                }
            } catch (JSONException e) {
                // ignore, should not happen
            }
        }
        return choiceElements;
    }

    /**
     * Builds the HTML String for the element language selector.<p>
     * 
     * This method has to use the resource request parameter because the temporary file is
     * not available in the upper button frame.<p>
     *  
     * @param attributes optional attributes for the &lt;select&gt; tag
     * @return the HTML for the element language select box
     */
    public String buildSelectElementLanguage(String attributes) {

        return buildSelectElementLanguage(attributes, getParamResource(), getElementLocale());
    }

    /**
     * Returns the available sub choices for a nested choice element.<p>
     * 
     * @return the available sub choices for a nested choice element as JSON array string
     */
    public String buildSubChoices() {

        String elementPath = getParamElementName();

        // we have to add a choice element, first check if the element to add itself is part of a choice or not
        boolean choiceType = Boolean.valueOf(getParamChoiceType()).booleanValue();
        I_CmsXmlSchemaType elemType = m_content.getContentDefinition().getSchemaType(elementPath);
        if (!choiceType || (elemType.isChoiceOption() && elemType.isChoiceType())) {
            // this is a choice option or a nested choice type to add, remove last element name from xpath
            elementPath = CmsXmlUtils.removeLastXpathElement(elementPath);
        }
        elementPath = CmsXmlUtils.concatXpath(elementPath, getParamChoiceElement());
        return buildElementChoices(elementPath, choiceType, false).toString();
    }

    /**
     * @see org.opencms.widgets.I_CmsWidgetDialog#getButtonStyle()
     */
    public int getButtonStyle() {

        return getSettings().getUserSettings().getEditorButtonStyle();
    }

    /**
     * @see org.opencms.workplace.editors.CmsEditor#getEditorResourceUri()
     */
    @Override
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
            if (CmsStringUtil.isNotEmpty(getParamElementlanguage()) && !"null".equals(getParamElementlanguage())) {
                m_elementLocale = CmsLocaleManager.getLocale(getParamElementlanguage());
            } else {
                initElementLanguage();
                m_elementLocale = CmsLocaleManager.getLocale(getParamElementlanguage());
            }
        }
        return m_elementLocale;
    }

    /**
     * @see org.opencms.widgets.I_CmsWidgetDialog#getHelpMessageIds()
     */
    public Set<String> getHelpMessageIds() {

        if (m_helpMessageIds == null) {
            m_helpMessageIds = new HashSet<String>();
        }
        return m_helpMessageIds;
    }

    /**
     * Returns the name of the choice element to add.<p>
     *
     * @return the name of the choice element to add
     */
    public String getParamChoiceElement() {

        return m_paramChoiceElement;
    }

    /**
     * Returns the flag if the element to add is a choice type.<p>
     * 
     * @return the flag if the element to add is a choice type
     */
    public String getParamChoiceType() {

        return m_paramChoiceType;
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
     * Returns the parameter that specifies the model file name.<p>
     * 
     * @return the parameter that specifies the model file name
     */
    public String getParamModelFile() {

        return m_paramModelFile;
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
     * @see org.opencms.widgets.I_CmsWidgetDialog#getUserAgent()
     */
    public String getUserAgent() {

        return getJsp().getRequest().getHeader(CmsRequestUtil.HEADER_USER_AGENT);
    }

    /**
     * Returns the different xml editor widgets used in the form to display.<p>
     * 
     * @return the different xml editor widgets used in the form to display
     */
    public CmsXmlContentWidgetVisitor getWidgetCollector() {

        if (m_widgetCollector == null) {
            // create an instance of the widget collector
            m_widgetCollector = new CmsXmlContentWidgetVisitor(getElementLocale());
            m_content.visitAllValuesWith(m_widgetCollector);
        }
        return m_widgetCollector;
    }

    /**
     * Generates the HTML form for the XML content editor.<p> 
     * 
     * @return the HTML that generates the form for the XML editor
     */
    public String getXmlEditorForm() {

        // set "editor mode" attribute (required for link replacement in the root site) 
        getCms().getRequestContext().setAttribute(CmsRequestContext.ATTRIBUTE_EDITOR, Boolean.TRUE);

        // add customized message bundle eventually specified in XSD of XML content
        addMessages(m_content.getHandler().getMessages(getLocale()));

        // initialize tab lists for error handling before generating the editor form
        m_errorTabs = new ArrayList<CmsXmlContentTab>();
        m_warningTabs = new ArrayList<CmsXmlContentTab>();

        return getXmlEditorForm(m_content.getContentDefinition(), "", true, false).toString();
    }

    /**
     * Generates the HTML for the end of the HTML editor form page.<p>
     * 
     * @return the HTML for the end of the HTML editor form page
     * @throws JspException if including the error page fails
     */
    public String getXmlEditorHtmlEnd() throws JspException {

        StringBuffer result = new StringBuffer(16384);
        if (m_optionalElementPresent) {
            // disabled optional element(s) present, reset widgets to show help bubbles on optional form entries
            resetWidgetCollector();
        }
        try {
            // get all widgets from collector
            Iterator<String> i = getWidgetCollector().getWidgets().keySet().iterator();
            while (i.hasNext()) {
                // get the value of the widget
                String key = i.next();
                I_CmsXmlContentValue value = getWidgetCollector().getValues().get(key);
                I_CmsWidget widget = getWidgetCollector().getWidgets().get(key);
                result.append(widget.getDialogHtmlEnd(getCms(), this, (I_CmsWidgetParameter)value));

            }

            // add empty help text layer
            result.append("<div class=\"help\" id=\"helpText\" ");
            result.append("onmouseover=\"showHelpText();\" onmouseout=\"hideHelpText();\"></div>\n");

            // add empty element button layer
            result.append("<div class=\"xmlButtons\" id=\"xmlElementButtons\" ");
            result.append("onmouseover=\"checkElementButtons(true);\" onmouseout=\"checkElementButtons(false);\"></div>\n");

            // return the HTML
            return result.toString();
        } catch (Exception e) {
            showErrorPage(e);
            return "";
        }
    }

    /**
     * Generates the JavaScript includes for the used widgets in the editor form.<p>
     * 
     * @return the JavaScript includes for the used widgets
     * @throws JspException if including the error page fails
     */
    public String getXmlEditorIncludes() throws JspException {

        StringBuffer result = new StringBuffer(1024);

        // first include general JQuery JS and UI components
        result.append("<script type=\"text/javascript\" src=\"");
        result.append(CmsWorkplace.getSkinUri()).append("jquery/packed/jquery.js");
        result.append("\"></script>\n");
        result.append("<script type=\"text/javascript\" src=\"");
        result.append(CmsWorkplace.getSkinUri()).append("jquery/packed/jquery.ui.js");
        result.append("\"></script>\n");

        // including dialog-helper.js to be used by ADE gallery widgets
        result.append("<script type=\"text/javascript\" src=\"");
        result.append(CmsWorkplace.getSkinUri()).append("components/widgets/dialog-helper.js");
        result.append("\"></script>\n");

        // import the JavaScript for JSON helper functions
        result.append("<script type=\"text/javascript\" src=\"");
        result.append(CmsWorkplace.getSkinUri()).append("commons/json2.js");
        result.append("\"></script>\n");
        result.append("<link rel=\"stylesheet\" type=\"text/css\" href=\"");
        result.append(CmsWorkplace.getSkinUri()).append("jquery/css/ui-ocms/jquery.ui.css");
        result.append("\">\n");
        result.append("<link rel=\"stylesheet\" type=\"text/css\" href=\"");
        result.append(CmsWorkplace.getSkinUri()).append("jquery/css/ui-ocms/jquery.ui.ocms.css");
        result.append("\">\n");

        try {
            // iterate over unique widgets from collector
            Iterator<I_CmsWidget> i = getWidgetCollector().getUniqueWidgets().iterator();
            while (i.hasNext()) {
                I_CmsWidget widget = i.next();
                result.append(widget.getDialogIncludes(getCms(), this));
                result.append("\n");
            }
        } catch (Exception e) {
            showErrorPage(e);
        }
        return result.toString();
    }

    /**
     * Generates the JavaScript initialization calls for the used widgets in the editor form.<p>
     * 
     * @return the JavaScript initialization calls for the used widgets
     * @throws JspException if including the error page fails
     */
    public String getXmlEditorInitCalls() throws JspException {

        StringBuffer result = new StringBuffer(512);
        try {
            // iterate over unique widgets from collector
            Iterator<I_CmsWidget> i = getWidgetCollector().getUniqueWidgets().iterator();
            while (i.hasNext()) {
                I_CmsWidget widget = i.next();
                result.append(widget.getDialogInitCall(getCms(), this));
            }
        } catch (Exception e) {
            showErrorPage(e);
        }
        return result.toString();
    }

    /**
     * Generates the JavaScript initialization methods for the used widgets.<p>
     * 
     * @return the JavaScript initialization methods for the used widgets
     * 
     * @throws JspException if an error occurs during JavaScript generation
     */
    public String getXmlEditorInitMethods() throws JspException {

        StringBuffer result = new StringBuffer(1024);

        // first create JS for eventual tabs
        StringBuffer tabs = new StringBuffer(512);
        if (m_content.getHandler().getTabs().size() > 0) {
            // we have some tabs defined, initialize them using JQuery
            result.append("var xmlSelectedTab = 0;\n");
            result.append("var xmlErrorTabs = new Array();\n");
            result.append("var xmlWarningTabs = new Array();\n");
            tabs.append("\t$xmltabs = $(\"#xmltabs\").tabs({});\n");
            tabs.append("\t$xmltabs.tabs(\"select\", xmlSelectedTab);\n");
            tabs.append("\tfor (var i=0; i<xmlErrorTabs.length; i++) {\n");
            tabs.append("\t\t$(\"#OcmsTabTab\" + xmlErrorTabs[i]).addClass(\"ui-state-error\");\n");
            tabs.append("\t}\n");
            tabs.append("\tfor (var i=0; i<xmlWarningTabs.length; i++) {\n");
            tabs.append("\t\t$(\"#OcmsTabTab\" + xmlWarningTabs[i]).addClass(\"ui-state-warning\");\n");
            tabs.append("\t}\n");
        }

        // create JS for UI dialog
        result.append("var dialogTitleAddChoice = \"");
        result.append(key(Messages.GUI_EDITOR_XMLCONTENT_CHOICE_ADD_HL_0));
        result.append("\";\n");
        result.append("var dialogTitleAddSubChoice = \"");
        result.append(key(Messages.GUI_EDITOR_XMLCONTENT_CHOICE_SUB_ADD_HL_0));
        result.append("\";\n");
        result.append("var vfsPathEditorForm = \"");
        result.append(getJsp().link(CmsEditor.PATH_EDITORS + "xmlcontent/editor_form.jsp"));
        result.append("\";\n");
        result.append("if (jQuery) {\n");
        result.append("$(document).ready(function(){\n");
        result.append(tabs);
        result.append("\t$(\"#xmladdelementdialog\").dialog({\n");
        result.append("\t\ttitle: \"");
        result.append(key(Messages.GUI_EDITOR_XMLCONTENT_CHOICE_ADD_HL_0));
        result.append("\",\n");
        result.append("\t\tautoOpen: false,\n");
        result.append("\t\tbgiframe: true,\n");
        result.append("\t\tminHeight: 150,\n");
        result.append("\t\tminWidth: 300,\n");
        result.append("\t\twidth: 360,\n");
        result.append("\t\tmodal: true,\n");
        result.append("\t\tbuttons: {  \"");
        result.append(key(org.opencms.workplace.Messages.GUI_DIALOG_BUTTON_CANCEL_0));
        result.append("\": function() { $(this).dialog(\"close\"); } }\n");
        result.append("\t});\n");
        result.append("});\n");
        result.append("}\n");

        try {
            // iterate over unique widgets from collector
            Iterator<I_CmsWidget> i = getWidgetCollector().getUniqueWidgets().iterator();
            while (i.hasNext()) {
                I_CmsWidget widget = i.next();
                result.append(widget.getDialogInitMethod(getCms(), this));
                result.append("\n");
            }
        } catch (Exception e) {
            showErrorPage(e);
        }
        return result.toString();
    }

    /**
     * Returns true if the edited content contains validation errors, otherwise false.<p>
     * 
     * @return true if the edited content contains validation errors, otherwise false
     */
    public boolean hasValidationErrors() {

        return getValidationHandler().hasErrors();
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
            CmsXmlContent content = CmsXmlContentFactory.unmarshal(getCloneCms(), file);
            return content.getHandler().getPreview(getCms(), m_content, getParamResource()) != null;
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

        List<String> names = m_content.getNames(locale);
        Iterator<String> i = names.iterator();
        while (i.hasNext()) {
            String path = i.next();
            I_CmsXmlContentValue value = m_content.getValue(path, locale);
            if (value.isSimpleType()) {
                I_CmsWidget widget = value.getContentDefinition().getContentHandler().getWidget(value);
                widget.setEditorValue(
                    getCms(),
                    getJsp().getRequest().getParameterMap(),
                    this,
                    (I_CmsWidgetParameter)value);
            }
        }
    }

    /**
     * Sets the name of the choice element to add.<p>
     *
     * @param choiceElement the name of the choice element to add
     */
    public void setParamChoiceElement(String choiceElement) {

        m_paramChoiceElement = choiceElement;
    }

    /**
     * Sets the flag if the element to add is a choice type.<p>
     * 
     * @param paramChoiceType the flag if the element to add is a choice type
     */
    public void setParamChoiceType(String paramChoiceType) {

        m_paramChoiceType = paramChoiceType;
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
     * Sets the parameter that specifies the model file name.<p>
     * 
     * @param paramMasterFile the parameter that specifies the model file name
     */
    public void setParamModelFile(String paramMasterFile) {

        m_paramModelFile = paramMasterFile;
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

        List<Locale> locales = OpenCms.getLocaleManager().getAvailableLocales(getCms(), getParamResource());
        if ((locales == null) || (locales.size() < 2)) {
            // for less than two available locales, do not create language selector
            return false;
        }
        return true;
    }

    /**
     * @see org.opencms.workplace.tools.CmsToolDialog#useNewStyle()
     */
    @Override
    public boolean useNewStyle() {

        return false;
    }

    /**
     * @see org.opencms.workplace.editors.CmsEditor#commitTempFile()
     */
    @Override
    protected void commitTempFile() throws CmsException {

        super.commitTempFile();
        m_file = getCloneCms().readFile(getParamResource());
        m_content = CmsXmlContentFactory.unmarshal(getCloneCms(), m_file);
    }

    /**
     * Makes sure the requested locale node is present in the content document
     * by either copying an existing locale node or creating an empty one.<p>
     * 
     * @param locale the requested locale
     * 
     * @return the locale
     */
    protected Locale ensureLocale(Locale locale) {

        // get the default locale for the resource
        List<Locale> locales = OpenCms.getLocaleManager().getDefaultLocales(getCms(), getParamResource());
        if (m_content != null) {
            if (!m_content.hasLocale(locale)) {
                try {

                    // to copy anything we need at least one locale
                    if ((m_content.getLocales().size() > 0)) {
                        // required locale not available, check if an existing default locale should be copied as "template"
                        try {
                            // a list of possible default locales has been set as property, try to find a match                    
                            m_content.copyLocale(locales, locale);

                        } catch (CmsException e) {
                            m_content.addLocale(getCms(), locale);
                        }

                    } else {
                        m_content.addLocale(getCms(), locale);
                    }

                    writeContent();
                } catch (CmsException e) {
                    LOG.error(e.getMessageContainer(), e);
                }
            }
            if (!m_content.hasLocale(locale)) {
                // value may have changed because of the copy operation
                locale = m_content.getLocales().get(0);
            }
        }
        return locale;
    }

    /**
     * Initializes the editor content when opening the editor for the first time.<p>
     * 
     * Not necessary for the xmlcontent editor.<p>
     */
    @Override
    protected void initContent() {

        // nothing to be done for the xmlcontent editor form
    }

    /**
     * Initializes the element language for the first call of the editor.<p>
     */
    protected void initElementLanguage() {

        // get the default locale for the resource
        List<Locale> locales = OpenCms.getLocaleManager().getDefaultLocales(getCms(), getParamResource());
        Locale locale = locales.get(0);
        locale = ensureLocale(locale);
        setParamElementlanguage(locale.toString());
    }

    /**
     * @see org.opencms.workplace.CmsWorkplace#initWorkplaceRequestValues(org.opencms.workplace.CmsWorkplaceSettings, javax.servlet.http.HttpServletRequest)
     */
    @Override
    protected void initWorkplaceRequestValues(CmsWorkplaceSettings settings, HttpServletRequest request) {

        // fill the parameter values in the get/set methods
        fillParamValues(request);
        // set the dialog type
        setParamDialogtype(EDITOR_TYPE);

        if (getParamNewLink() != null) {
            setParamAction(EDITOR_ACTION_NEW);
        } else {
            // initialize a content object from the temporary file
            if ((getParamTempfile() != null) && !"null".equals(getParamTempfile())) {
                try {
                    m_file = getCms().readFile(this.getParamTempfile(), CmsResourceFilter.ALL);
                    m_content = CmsXmlContentFactory.unmarshal(getCloneCms(), m_file);
                } catch (CmsException e) {
                    // error during initialization, show error page
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
        }

        // set the action for the JSP switch 
        if (EDITOR_SAVE.equals(getParamAction())) {
            setAction(ACTION_SAVE);
        } else if (EDITOR_SAVEEXIT.equals(getParamAction())) {
            setAction(ACTION_SAVEEXIT);
        } else if (EDITOR_EXIT.equals(getParamAction())) {
            setAction(ACTION_EXIT);
        } else if (EDITOR_ACTION_SUBCHOICES.equals(getParamAction())) {
            setAction(ACTION_SUBCHOICES);
        } else if (EDITOR_CLOSEBROWSER.equals(getParamAction())) {
            // closed browser window accidentally, unlock resource and delete temporary file
            actionClear(true);
            return;
        } else if (EDITOR_ACTION_CHECK.equals(getParamAction())) {
            setAction(ACTION_CHECK);
        } else if (EDITOR_SAVEACTION.equals(getParamAction())) {
            setAction(ACTION_SAVEACTION);
            try {
                actionDirectEdit();
            } catch (Exception e) {
                // should usually never happen
                if (LOG.isInfoEnabled()) {
                    LOG.info(e.getLocalizedMessage(), e);
                }
            }
            setAction(ACTION_EXIT);
        } else if (EDITOR_COPYLOCALE.equals(getParamAction())) {
            setAction(ACTION_COPYLOCALE);
        } else if (EDITOR_DELETELOCALE.equals(getParamAction())) {
            setAction(ACTION_DELETELOCALE);
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
                if (LOG.isErrorEnabled()) {
                    LOG.error(org.opencms.workplace.Messages.get().getBundle().key(
                        org.opencms.workplace.Messages.LOG_INCLUDE_ERRORPAGE_FAILED_0));
                }
            }
        } else if (EDITOR_ACTION_ELEMENT_REMOVE.equals(getParamAction())) {
            setAction(ACTION_ELEMENT_REMOVE);
            try {
                actionToggleElement();
            } catch (JspException e) {
                if (LOG.isErrorEnabled()) {
                    LOG.error(org.opencms.workplace.Messages.get().getBundle().key(
                        org.opencms.workplace.Messages.LOG_INCLUDE_ERRORPAGE_FAILED_0));
                }
            }
        } else if (EDITOR_ACTION_ELEMENT_MOVE_DOWN.equals(getParamAction())) {
            setAction(ACTION_ELEMENT_MOVE_DOWN);
            try {
                actionMoveElement();
            } catch (JspException e) {
                if (LOG.isErrorEnabled()) {
                    LOG.error(org.opencms.workplace.Messages.get().getBundle().key(
                        org.opencms.workplace.Messages.LOG_INCLUDE_ERRORPAGE_FAILED_0));
                }
            }
        } else if (EDITOR_ACTION_ELEMENT_MOVE_UP.equals(getParamAction())) {
            setAction(ACTION_ELEMENT_MOVE_UP);
            try {
                actionMoveElement();
            } catch (JspException e) {
                if (LOG.isErrorEnabled()) {
                    LOG.error(org.opencms.workplace.Messages.get().getBundle().key(
                        org.opencms.workplace.Messages.LOG_INCLUDE_ERRORPAGE_FAILED_0));
                }
            }
        } else if (EDITOR_ACTION_NEW.equals(getParamAction())) {
            setAction(ACTION_NEW);
            return;
        } else if (EDITOR_PREVIEW.equals(getParamAction())) {
            setAction(ACTION_PREVIEW);
        } else if (EDITOR_CORRECTIONCONFIRMED.equals(getParamAction())) {
            setAction(ACTION_SHOW);
            try {
                // correct the XML structure before showing the form
                correctXmlStructure();
            } catch (CmsException e) {
                // error during correction
                try {
                    showErrorPage(this, e);
                } catch (JspException exc) {
                    // should usually never happen
                    if (LOG.isInfoEnabled()) {
                        LOG.info(exc);
                    }
                }
            }
        } else {
            // initial call of editor
            setAction(ACTION_DEFAULT);
            try {
                // lock resource if autolock is enabled in configuration
                if (Boolean.valueOf(getParamDirectedit()).booleanValue()) {
                    // set a temporary lock in direct edit mode
                    checkLock(getParamResource(), CmsLockType.TEMPORARY);
                } else {
                    // set common lock
                    checkLock(getParamResource());
                }
                // create the temporary file
                setParamTempfile(createTempFile());
                // initialize a content object from the created temporary file
                m_file = getCms().readFile(this.getParamTempfile(), CmsResourceFilter.ALL);
                m_content = CmsXmlContentFactory.unmarshal(getCloneCms(), m_file);
                // check the XML content against the given XSD
                try {
                    m_content.validateXmlStructure(new CmsXmlEntityResolver(getCms()));
                } catch (CmsXmlException eXml) {
                    // validation failed, check the settings for handling the correction
                    if (OpenCms.getWorkplaceManager().isXmlContentAutoCorrect()) {
                        // correct the XML structure automatically according to the XSD
                        correctXmlStructure();
                    } else {
                        // show correction confirmation dialog
                        setAction(ACTION_CONFIRMCORRECTION);
                    }
                }

            } catch (CmsException e) {
                // error during initialization
                try {
                    showErrorPage(this, e);
                } catch (JspException exc) {
                    // should usually never happen
                    if (LOG.isInfoEnabled()) {
                        LOG.info(exc);
                    }
                }
            }
            // set the initial element language if not given in request parameters
            if (getParamElementlanguage() == null) {
                initElementLanguage();
            } else {
                Locale locale = CmsLocaleManager.getLocale(getParamElementlanguage());
                ensureLocale(locale);
            }
        }
    }

    /**
     * Returns the HTML for the element operation buttons add, move, remove.<p>
     * 
     * @param value the value for which the buttons are generated
     * @param index the index of the element
     * @param addElement if true, the button to add an element is shown
     * @param removeElement if true, the button to remove an element is shown
     * @return the HTML for the element operation buttons
     */
    private String buildElementButtons(I_CmsXmlContentValue value, boolean addElement, boolean removeElement) {

        StringBuffer jsCall = new StringBuffer(512);
        String elementName = CmsXmlUtils.removeXpathIndex(value.getPath());

        // indicates if at least one button is active
        boolean buttonPresent = false;

        int index = value.getIndex();

        jsCall.append("showElementButtons('");
        jsCall.append(elementName);
        jsCall.append("', ");
        jsCall.append(index);
        jsCall.append(", ");

        // build the remove element button if required
        if (removeElement) {
            jsCall.append(Boolean.TRUE);
            buttonPresent = true;
        } else {
            jsCall.append(Boolean.FALSE);
        }
        jsCall.append(", ");

        // build the move down button (move down in API is move up for content editor)
        if (index > 0) {
            // build active move down button
            jsCall.append(Boolean.TRUE);
            buttonPresent = true;
        } else {
            jsCall.append(Boolean.FALSE);
        }
        jsCall.append(", ");

        // build the move up button (move up in API is move down for content editor)
        int indexCount = m_content.getIndexCount(elementName, getElementLocale());
        if (index < (indexCount - 1)) {
            // build active move up button
            jsCall.append(Boolean.TRUE);
            buttonPresent = true;
        } else {
            jsCall.append(Boolean.FALSE);
        }
        jsCall.append(", ");

        // build the add element button if required
        if (addElement) {
            jsCall.append(Boolean.TRUE);
            buttonPresent = true;
        } else {
            jsCall.append(Boolean.FALSE);
        }
        jsCall.append(", ");

        JSONArray newElements = buildElementChoices(elementName, value.isChoiceType(), true);
        jsCall.append("'").append(CmsEncoder.escape(newElements.toString(), CmsEncoder.ENCODING_UTF_8)).append("'");
        jsCall.append(");");

        String result;
        if (buttonPresent) {
            // at least one button active, create mouseover button
            String btIcon = "xmledit.png";
            String btAction = jsCall.toString();
            // determine icon to use and if a direct click action is possible
            if (addElement && removeElement) {
                btIcon = "xmledit_del_add.png";
            } else if (addElement) {
                btIcon = "xmledit_add.png";
                // create button action to add element on button click
                StringBuffer action = new StringBuffer(128);
                action.append("addElement('");
                action.append(elementName);
                action.append("', ");
                action.append(index);
                action.append(", '").append(CmsEncoder.escape(newElements.toString(), CmsEncoder.ENCODING_UTF_8)).append(
                    "'");
                action.append(");");
                btAction = action.toString();
            } else if (removeElement) {
                btIcon = "xmledit_del.png";
                // create button action to remove element on button click
                StringBuffer action = new StringBuffer(128);
                action.append("removeElement('");
                action.append(elementName);
                action.append("', ");
                action.append(index);
                action.append(");");
                btAction = action.toString();
            }
            StringBuffer href = new StringBuffer(512);
            href.append("javascript:");
            href.append(btAction);
            href.append("\" onmouseover=\"");
            href.append(jsCall);
            href.append("checkElementButtons(true);\" onmouseout=\"checkElementButtons(false);\" id=\"btimg.");
            href.append(elementName).append(".").append(index);
            result = button(href.toString(), null, btIcon, Messages.GUI_EDITOR_XMLCONTENT_ELEMENT_BUTTONS_0, 0);
        } else {
            // no active button, create a spacer
            result = buttonBarSpacer(1);
        }

        return result;
    }

    /**
     * Corrects the XML structure of the edited content according to the XSD.<p>
     * 
     * @throws CmsException if the correction fails
     */
    private void correctXmlStructure() throws CmsException {

        m_content.setAutoCorrectionEnabled(true);
        m_content.correctXmlStructure(getCms());
        // write the corrected temporary file
        writeContent();
    }

    /**
     * Returns the error handler for error handling of the edited xml content.<p>
     * 
     * @return the error handler
     */
    private CmsXmlContentErrorHandler getValidationHandler() {

        if (m_validationHandler == null) {
            // errors were not yet checked, do this now and store result in member
            m_validationHandler = m_content.validate(getCms());
        }
        return m_validationHandler;
    }

    /**
     * Generates the HTML form for the XML content editor.<p>
     * 
     * This is a recursive method because nested schemas are possible,
     * do not call this method directly.<p>
     * 
     * @param contentDefinition the content definition to start with
     * @param pathPrefix for nested xml content
     * @param showHelpBubble if the code for a help bubble should be generated
     * 
     * @return the HTML that generates the form for the XML editor
     */
    private StringBuffer getXmlEditorForm(
        CmsXmlContentDefinition contentDefinition,
        String pathPrefix,
        boolean showHelpBubble,
        boolean superTabOpened) {

        StringBuffer result = new StringBuffer(1024);
        // only show errors if editor is not opened initially
        boolean showErrors = (getAction() != ACTION_NEW)
            && (getAction() != ACTION_DEFAULT)
            && (getAction() != ACTION_ELEMENT_ADD)
            && (getAction() != ACTION_ELEMENT_REMOVE)
            && (getAction() != ACTION_ELEMENT_MOVE_DOWN)
            && (getAction() != ACTION_ELEMENT_MOVE_UP);

        try {
            // check if we are in a nested content definition
            boolean nested = CmsStringUtil.isNotEmpty(pathPrefix);
            boolean useTabs = false;
            boolean tabOpened = false;
            StringBuffer selectedTabScript = new StringBuffer(64);

            boolean collapseLabel = false;
            boolean firstElement = true;

            // show error header once if there were validation errors
            if (!nested && showErrors && (getValidationHandler().hasErrors())) {

                result.append("<div class=\"ui-widget\">");
                result.append("<div class=\"ui-state-error ui-corner-all\" style=\"padding: 0pt 0.7em;\"><div style=\"padding: 3px 0;\">");
                result.append("<span class=\"ui-icon ui-icon-alert\" style=\"float: left; margin-right: 0.3em;\"></span>");
                boolean differentLocaleErrors = false;
                if ((getValidationHandler().getErrors(getElementLocale()) == null)
                    || (getValidationHandler().getErrors().size() > getValidationHandler().getErrors(getElementLocale()).size())) {

                    differentLocaleErrors = true;
                    result.append("<span id=\"xmlerrordialogbutton\" class=\"ui-icon ui-icon-newwin\" style=\"float: left; margin-right: 0.3em;\"></span>");
                }
                result.append(key(Messages.ERR_EDITOR_XMLCONTENT_VALIDATION_ERROR_TITLE_0));
                result.append("</div>");

                // show errors in different locales
                if (differentLocaleErrors) {

                    result.append("<div id=\"xmlerrordialog\" style=\"display: none;\">");
                    // iterate through all found errors
                    Map<Locale, Map<String, String>> locErrors = getValidationHandler().getErrors();
                    Iterator<Map.Entry<Locale, Map<String, String>>> locErrorsIter = locErrors.entrySet().iterator();
                    while (locErrorsIter.hasNext()) {
                        Map.Entry<Locale, Map<String, String>> locEntry = locErrorsIter.next();
                        Locale locale = locEntry.getKey();

                        // skip errors in the actual locale
                        if (getElementLocale().equals(locale)) {
                            continue;
                        }

                        result.append("<div style=\"padding: 3px;\"><strong>");
                        result.append(key(
                            Messages.ERR_EDITOR_XMLCONTENT_VALIDATION_ERROR_LANG_1,
                            new Object[] {locale.getLanguage()}));
                        result.append("</strong></div>\n");
                        result.append("<ul>");

                        // iterate through the found errors in a different locale
                        Map<String, String> elErrors = locEntry.getValue();
                        Iterator<Map.Entry<String, String>> elErrorsIter = elErrors.entrySet().iterator();
                        while (elErrorsIter.hasNext()) {
                            Map.Entry<String, String> elEntry = elErrorsIter.next();
                            String nodeName = elEntry.getKey();
                            String errorMsg = elEntry.getValue();
                            // output the error message
                            result.append("<li>");
                            result.append(nodeName);
                            result.append(": ");
                            result.append(errorMsg);
                            result.append("</li>\n");
                        }

                        result.append("</ul>");
                    }

                    result.append("</div>\n");
                    result.append("<script type=\"text/javascript\">\n");
                    result.append("$(\"#xmlerrordialog\").dialog({\n");
                    result.append("\tautoOpen: true,\n");
                    result.append("\tbgiframe: true,\n");
                    result.append("\twidth: 500,\n");
                    result.append("\tposition: 'center',\n");
                    result.append("\tdialogClass: 'ui-state-error',\n");
                    result.append("\ttitle: '").append(key(Messages.ERR_EDITOR_XMLCONTENT_VALIDATION_ERROR_TITLE_0)).append(
                        "',\n");
                    result.append("\tmaxHeight: 600\n");
                    result.append("});\n");

                    result.append("$(\"#xmlerrordialogbutton\").bind(\"click\", function(e) {$(\"#xmlerrordialog\").dialog(\"open\");});\n");
                    result.append("</script>");
                }
                result.append("</div></div>");
            }

            if (!nested) {
                // check if tabs should be shown
                useTabs = contentDefinition.getContentHandler().getTabs().size() > 0;
                if (useTabs) {
                    // we have some tabs available, generate them on first level
                    result.append("<div id=\"xmltabs\" class=\"ui-tabs\">\n<ul>\n");
                    for (Iterator<CmsXmlContentTab> i = contentDefinition.getContentHandler().getTabs().iterator(); i.hasNext();) {
                        CmsXmlContentTab tab = i.next();
                        result.append("\t<li id=\"OcmsTabTab").append(tab.getIdName()).append("\"><a href=\"#OcmsTab");
                        result.append(tab.getIdName());
                        result.append("\"><span>");
                        result.append(keyDefault(A_CmsWidget.LABEL_PREFIX
                            + contentDefinition.getInnerName()
                            + "."
                            + tab.getTabName(), tab.getTabName()));
                        result.append("</span></a></li>\n");
                    }
                    result.append("</ul>\n");
                }
            }

            // if xsd:choice then we just need to get one element of the sequence
            Iterator<I_CmsXmlSchemaType> it = contentDefinition.getChoiceMaxOccurs() > 0
            ? contentDefinition.getTypeSequence().subList(0, 1).iterator()
            : contentDefinition.getTypeSequence().iterator();

            // iterate the type sequence        
            while (it.hasNext()) {
                // get the type
                I_CmsXmlSchemaType type = it.next();

                boolean tabCurrentlyOpened = false;

                if (useTabs) {
                    // check if a tab is starting with this element
                    for (int tabIndex = 0; tabIndex < contentDefinition.getContentHandler().getTabs().size(); tabIndex++) {
                        CmsXmlContentTab checkTab = contentDefinition.getContentHandler().getTabs().get(tabIndex);
                        if (checkTab.getStartName().equals(type.getName())) {
                            // a tab is starting, add block element
                            if (tabOpened) {
                                // close a previously opened tab
                                result.append("</table>\n</div>\n");
                            }
                            result.append("<div id=\"OcmsTab");
                            result.append(checkTab.getIdName());
                            result.append("\" class=\"ui-tabs-hide\">\n");
                            // set necessary values
                            tabOpened = true;
                            tabCurrentlyOpened = true;
                            collapseLabel = checkTab.isCollapsed();
                            m_currentTab = checkTab;
                            m_currentTabIndex = tabIndex;
                            // leave loop
                            break;
                        }
                    }
                }

                if (firstElement || tabCurrentlyOpened) {
                    // create table before first element or if a tab has been opened before
                    result.append("<table class=\"xmlTable");
                    if (nested && !superTabOpened) {
                        // use other style for nested content definition table if tab was not opened on upper level
                        result.append("Nested");
                    }
                    result.append("\">\n");
                    firstElement = false;
                }

                // get the element sequence of the current type
                CmsXmlContentValueSequence elementSequence = m_content.getValueSequence(
                    pathPrefix + type.getName(),
                    getElementLocale());
                int elementCount = elementSequence.getElementCount();

                // check if value is optional or multiple
                boolean addValue = elementCount < elementSequence.getMaxOccurs();
                boolean removeValue = elementCount > elementSequence.getMinOccurs();

                // assure that at least one element is present in sequence
                boolean disabledElement = false;

                if ((contentDefinition.getChoiceMaxOccurs() == 0) && (elementCount < 1)) {
                    // current element is disabled, create dummy element if it is a nested type or no choice option
                    elementCount = 1;
                    elementSequence.addValue(getCms(), 0);
                    disabledElement = true;
                    m_optionalElementPresent = true;
                }

                // loop through multiple elements
                for (int j = 0; j < elementCount; j++) {
                    // get value and corresponding widget
                    I_CmsXmlContentValue value = elementSequence.getValue(j);

                    String key = value.getPath();

                    // check if a tab should be preselected for an added, removed or moved up/down element
                    if ((m_currentTab != null) && CmsStringUtil.isNotEmpty(getParamElementName())) {
                        // an element was modified, add JS to preselect tab
                        if (key.startsWith(getParamElementName()) && (selectedTabScript.length() == 0)) {
                            selectedTabScript.append("<script type=\"text/javascript\">\n\txmlSelectedTab = ").append(
                                m_currentTabIndex).append(";\n</script>\n");
                        }
                    }

                    // show errors and/or warnings
                    if (showErrors
                        && getValidationHandler().hasErrors(getElementLocale())
                        && getValidationHandler().getErrors(getElementLocale()).containsKey(key)) {
                        // show error message
                        if (collapseLabel) {
                            result.append("<tr><td class=\"xmlTdError\"><img src=\"");
                            result.append(getEditorResourceUri());
                            result.append("error.png\" border=\"0\" alt=\"\" align=\"left\" hspace=\"5\">");
                            result.append(resolveMacros(getValidationHandler().getErrors(getElementLocale()).get(key)));
                            result.append("</td><td></td></tr>\n");
                        } else {
                            result.append("<tr><td></td><td><img src=\"");
                            result.append(getEditorResourceUri());
                            result.append("error.png");
                            result.append("\" border=\"0\" alt=\"\"></td><td class=\"xmlTdError\">");
                            result.append(resolveMacros(getValidationHandler().getErrors(getElementLocale()).get(key)));
                            result.append("</td><td></td></tr>\n");
                        }

                        // mark tab as error tab if tab is present
                        String elemName = CmsXmlUtils.getFirstXpathElement(value.getPath());
                        if (((m_currentTab != null) && !m_errorTabs.contains(m_currentTab))
                            && (elemName.equals(m_currentTab.getStartName()) || (!CmsXmlUtils.isDeepXpath(value.getPath()) && value.getName().equals(
                                elemName)))) {
                            m_errorTabs.add(m_currentTab);
                        }

                    }
                    // warnings can be additional to errors
                    if (showErrors
                        && getValidationHandler().hasWarnings(getElementLocale())
                        && getValidationHandler().getWarnings(getElementLocale()).containsKey(key)) {
                        // show warning message
                        if (collapseLabel) {
                            result.append("<tr><td class=\"xmlTdError\"><img src=\"");
                            result.append(getEditorResourceUri());
                            result.append("warning.png\" border=\"0\" alt=\"\" align=\"left\" hspace=\"5\">");
                            result.append(resolveMacros(getValidationHandler().getWarnings(getElementLocale()).get(key)));
                            result.append("</td><td></td></tr>\n");
                        } else {
                            result.append("<tr><td></td><td><img src=\"");
                            result.append(getEditorResourceUri());
                            result.append("warning.png");
                            result.append("\" border=\"0\" alt=\"\"></td><td class=\"xmlTdWarning\">");
                            result.append(resolveMacros(getValidationHandler().getWarnings(getElementLocale()).get(key)));
                            result.append("</td><td></td></tr>\n");
                        }

                        // mark tab as warning tab if tab is present
                        String elemName = CmsXmlUtils.getFirstXpathElement(value.getPath());
                        if (((m_currentTab != null) && !m_warningTabs.contains(m_currentTab))
                            && (elemName.equals(m_currentTab.getStartName()) || (!CmsXmlUtils.isDeepXpath(value.getPath()) && value.getName().equals(
                                elemName)))) {
                            m_warningTabs.add(m_currentTab);
                        }
                    }

                    I_CmsWidget widget = value.isSimpleType()
                    ? contentDefinition.getContentHandler().getWidget(value)
                    : null;

                    int index = value.getIndex();
                    // create label and help bubble cells
                    result.append("<tr>");
                    if (!collapseLabel) {
                        result.append("<td class=\"xmlLabel");
                        if (disabledElement) {
                            // element is disabled, mark it with CSS
                            result.append("Disabled");
                        }
                        result.append("\">");
                        result.append(keyDefault(A_CmsWidget.getLabelKey((I_CmsWidgetParameter)value), value.getName()));
                        if (elementCount > 1) {
                            result.append(" [").append(index + 1).append("]");
                        }
                        result.append(": </td>");
                        if (showHelpBubble && (widget != null) && (CmsXmlUtils.getXpathIndexInt(value.getPath()) == 1)) {
                            // show help bubble only on first element of each content definition 
                            result.append(widget.getHelpBubble(getCms(), this, (I_CmsWidgetParameter)value));
                        } else {
                            // create empty cell for all following elements 
                            result.append(buttonBarSpacer(16));
                        }
                    }

                    // append individual widget HTML cell if element is enabled
                    if (disabledElement) {
                        // disabled element, show message for optional element
                        result.append("<td class=\"xmlTdDisabled maxwidth\">");
                        result.append(key(Messages.GUI_EDITOR_XMLCONTENT_OPTIONALELEMENT_0));
                        result.append("</td>");

                    } else {
                        // element is enabled, show it
                        if (value.isSimpleType()) {
                            // this is a simple type, display widget
                            result.append(widget.getDialogWidget(getCms(), this, (I_CmsWidgetParameter)value));
                        } else {
                            // recurse into nested type sequence
                            result.append("<td class=\"maxwidth\">");
                            boolean showHelp = (j == 0);
                            superTabOpened = !nested && tabOpened && collapseLabel;
                            result.append(getXmlEditorForm(
                                ((CmsXmlNestedContentDefinition)value).getNestedContentDefinition(),
                                value.getPath() + "/",
                                showHelp,
                                superTabOpened));
                            result.append("</td>");
                        }
                    }

                    // append element operation (add, remove, move) buttons if required
                    result.append(buildElementButtons(value, addValue, removeValue));

                    // close row
                    result.append("</tr>\n");

                    // remove disabled element to avoid eventual side effects, e.g. in widget configurations
                    if (disabledElement) {
                        elementSequence.removeValue(0);
                    }

                }
            }
            // close table
            result.append("</table>\n");
            if (tabOpened) {
                // close last open tab
                result.append("</div>\n");
            }
            if (!nested && useTabs) {
                // close block element around tabs
                result.append("</div>\n");
                // mark eventual warning and error tabs
                result.append("<script type=\"text/javascript\">\n");
                for (Iterator<CmsXmlContentTab> i = m_warningTabs.iterator(); i.hasNext();) {
                    CmsXmlContentTab checkTab = i.next();
                    if (!m_errorTabs.contains(checkTab)) {
                        result.append("\txmlWarningTabs[xmlWarningTabs.length] = \"").append(checkTab.getIdName()).append(
                            "\";\n");
                    }
                }
                for (Iterator<CmsXmlContentTab> i = m_errorTabs.iterator(); i.hasNext();) {
                    CmsXmlContentTab checkTab = i.next();
                    result.append("\txmlErrorTabs[xmlErrorTabs.length] = \"").append(checkTab.getIdName()).append(
                        "\";\n");
                }
                result.append("</script>\n");
            }
            if (selectedTabScript.length() > 0) {
                result.append(selectedTabScript);
            }
        } catch (Throwable t) {
            LOG.error(Messages.get().getBundle().key(Messages.ERR_XML_EDITOR_0), t);
        }
        return result;
    }

    /**
     * Resets the error handler member variable to reinitialize the error messages.<p>
     */
    private void resetErrorHandler() {

        m_validationHandler = null;
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
            throw new CmsException(Messages.get().container(Messages.ERR_INVALID_CONTENT_ENC_1, getParamResource()), e);
        }
        // the file content might have been modified during the write operation    
        m_file = getCloneCms().writeFile(m_file);
        m_content = CmsXmlContentFactory.unmarshal(getCloneCms(), m_file);
    }
}