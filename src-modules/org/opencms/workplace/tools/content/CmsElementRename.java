/*
 * File   : $Source: /alkacon/cvs/opencms/src-modules/org/opencms/workplace/tools/content/CmsElementRename.java,v $
 * Date   : $Date: 2005/06/22 10:38:16 $
 * Version: $Revision: 1.8 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Mananagement System
 *
 * Copyright (C) 2002 - 2005 Alkacon Software (http://www.alkacon.com)
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

package org.opencms.workplace.tools.content;

import org.opencms.file.CmsFile;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsProperty;
import org.opencms.file.CmsPropertyDefinition;
import org.opencms.file.CmsResource;
import org.opencms.file.CmsResourceFilter;
import org.opencms.file.types.CmsResourceTypeXmlPage;
import org.opencms.i18n.CmsLocaleManager;
import org.opencms.jsp.CmsJspActionElement;
import org.opencms.lock.CmsLock;
import org.opencms.main.CmsException;
import org.opencms.main.CmsLog;
import org.opencms.main.OpenCms;
import org.opencms.report.I_CmsReport;
import org.opencms.util.CmsStringUtil;
import org.opencms.workplace.CmsReport;
import org.opencms.workplace.CmsWorkplace;
import org.opencms.workplace.CmsWorkplaceSettings;
import org.opencms.workplace.editors.CmsDialogElement;
import org.opencms.workplace.explorer.CmsNewResourceXmlPage;
import org.opencms.xml.CmsXmlException;
import org.opencms.xml.page.CmsXmlPage;
import org.opencms.xml.page.CmsXmlPageFactory;
import org.opencms.xml.types.CmsXmlHtmlValue;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.TreeMap;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.PageContext;

import org.apache.commons.logging.Log;

/**
 * Provides methods for the change page element name dialog.<p> 
 * 
 * The following files use this class:
 * <ul>
 * <li>/administration/properties/renameelement/index.html
 * </ul>
 *
 * @author Armen Markarian 
 * @version $Revision: 1.8 $
 * 
 * @since 5.5.3
 */
public class CmsElementRename extends CmsReport {

    /** A constant representing the select option all templates. */
    public static final String C_ALL = "ALL";
    /** The dialog type. */
    public static final String DIALOG_TYPE = "renameelement";
    /** Request parameter name for the locale. */
    public static final String PARAM_LOCALE = "locale";
    /** Request parameter name for the new element name. */
    public static final String PARAM_NEW_ELEMENT = "newelement";
    /** Request parameter name for the old element name. */
    public static final String PARAM_OLD_ELEMENT = "oldelement";
    /** Request parameter name for the recursive search. */
    public static final String PARAM_RECURSIVE = "recursive";
    /** Request parameter name for the remove empty elements. */
    public static final String PARAM_REMOVE_EMPTYELEMENTS = "removeemptyelements";
    /** Request parameter name for the template. */
    public static final String PARAM_TEMPLATE = "template";
    /** Request parameter name for the validate new element. */
    public static final String PARAM_VALIDATE_NEW_ELEMENT = "validatenewelement";

    /** The log object for this class. */
    private static final Log LOG = CmsLog.getLog(CmsElementRename.class);

    /** the cms object. */
    private CmsObject m_cms;
    /** the error message. */
    private String m_errorMessage;
    /** the locale use for content definition. */
    private String m_paramLocale;
    /** The new page element name. */
    private String m_paramNewElement;
    /** The old page element name. */
    private String m_paramOldElement;
    /** The recursive parameter. */
    private String m_paramRecursive;
    /** the flag indicating to remove empty elements. */
    private String m_paramRemoveEmptyElements;
    /** the template use for all pages (optional). */
    private String m_paramTemplate;
    /** the flag indicating to remove empty elements. */
    private String m_paramValidateNewElement;
    /** the report for the output. */
    private I_CmsReport m_report;

    /**
     * Public constructor with JSP action element.<p>
     * 
     * @param jsp an initialized JSP action element
     */
    public CmsElementRename(CmsJspActionElement jsp) {

        super(jsp);
    }

    /**
     * Public constructor for testcase using.<p>
     * 
     * @param jsp an initialized JSP action element
     * @param cms the cms object
     * @param resource the resource path
     * @param recursive if true then do read recursive from the folder 
     * @param template the template
     * @param locale the locale 
     * @param oldElement the old element name
     * @param newElement the new element name
     * @param removeEmptyElements if true then remove all invalid elements with no content
     * @param validateNewElement if true then validate the new element before renaming
     */
    public CmsElementRename(
        CmsJspActionElement jsp,
        CmsObject cms,
        String resource,
        String recursive,
        String template,
        String locale,
        String oldElement,
        String newElement,
        String removeEmptyElements,
        String validateNewElement) {

        super(jsp);
        m_cms = cms;
        setParamResource(resource);
        setParamRecursive(recursive);
        setParamTemplate(template);
        setParamLocale(locale);
        setParamOldElement(oldElement);
        setParamNewElement(newElement);
        setParamRemoveEmptyElements(removeEmptyElements);
        setParamValidateNewElement(validateNewElement);
    }

    /**
     * Public constructor with JSP variables.<p>
     * 
     * @param context the JSP page context
     * @param req the JSP request
     * @param res the JSP response
     */
    public CmsElementRename(PageContext context, HttpServletRequest req, HttpServletResponse res) {

        this(new CmsJspActionElement(context, req, res));
    }

    /**
     * Renames the element name on the specified resources.<p>
     * 
     * @param report the cms report
     */
    public void actionRename(I_CmsReport report) {

        m_report = report;
        List locales = OpenCms.getLocaleManager().getAvailableLocales();
        List xmlPages = getXmlPages();
        if (C_ALL.equals(getParamLocale())) {
            Iterator i = locales.iterator();
            while (i.hasNext()) {
                Locale locale = (Locale)i.next();
                performRenameOperation(xmlPages, locale);
            }
        } else {
            performRenameOperation(xmlPages, CmsLocaleManager.getLocale(getParamLocale()));
        }
    }

    /**
     * Performs the move report, will be called by the JSP page.<p>
     * 
     * @throws JspException if problems including sub-elements occur
     */
    public void actionReport() throws JspException {

        // save initialized instance of this class in request attribute for included sub-elements
        getJsp().getRequest().setAttribute(C_SESSION_WORKPLACE_CLASS, this);
        switch (getAction()) {
            case ACTION_REPORT_END:
                actionCloseDialog();
                break;
            case ACTION_REPORT_UPDATE:
                setParamAction(REPORT_UPDATE);
                getJsp().include(C_FILE_REPORT_OUTPUT);
                break;
            case ACTION_REPORT_BEGIN:
            case ACTION_CONFIRMED:
            default:
                CmsElementRenameThread thread = new CmsElementRenameThread(getCms(), this);
                setParamAction(REPORT_BEGIN);
                setParamThread(thread.getUUID().toString());
                getJsp().include(C_FILE_REPORT_OUTPUT);
                break;
        }
    }

    /**
     * Builds the html for the available locales select box.<p>
     * 
     * @param attributes optional attributes for the &lt;select&gt; tag
     *      
     * @return the html for the available locales select box
     */
    public String buildSelectLocales(String attributes) {

        List options = new ArrayList();
        List values = new ArrayList();
        List locales = OpenCms.getLocaleManager().getAvailableLocales();
        int selectedIndex = -1;
        if (locales == null) {
            // no locales found, return empty String
            return "";
        } else {
            // locales found, create option and value lists            
            options.add(key("please.select"));
            values.add("");
            options.add(key("button.all"));
            values.add(C_ALL);
            if (C_ALL.equals(getParamLocale())) {
                selectedIndex = 1;
            }
            Iterator i = locales.iterator();
            int counter = 2;
            while (i.hasNext()) {
                Locale locale = (Locale)i.next();
                String language = locale.getLanguage();
                String displayLanguage = locale.getDisplayLanguage();
                if (language.equals(getParamLocale())) {
                    selectedIndex = counter;
                }
                options.add(displayLanguage);
                values.add(language);
                counter++;
            }
        }

        return CmsWorkplace.buildSelect(attributes, options, values, selectedIndex, false);
    }

    /**
     * Builds the html for the template select box.<p>
     * 
     * @param attributes optional attributes for the &lt;select&gt; tag
     * @return the html for the template select box
     */
    public String buildSelectTemplates(String attributes) {

        List options = new ArrayList();
        List values = new ArrayList();
        TreeMap templates = null;
        int selectedIndex = -1;
        try {
            // get all available templates
            templates = CmsNewResourceXmlPage.getTemplates(getCms());
        } catch (CmsException e) {
            // can usually be ignored
            if (LOG.isInfoEnabled()) {
                LOG.info(e);
            }
        }
        if (templates == null) {
            // no templates found, return empty String
            return "";
        } else {
            // templates found, create option and value lists
            options.add(key("please.select"));
            values.add("");
            options.add(key("button.all"));
            values.add(C_ALL);
            if (C_ALL.equals(getParamTemplate())) {
                selectedIndex = 1;
            }
            Iterator i = templates.keySet().iterator();
            int counter = 2;
            while (i.hasNext()) {
                String key = (String)i.next();
                String path = (String)templates.get(key);
                if (path.equals(getParamTemplate())) {
                    selectedIndex = counter;
                }
                options.add(key);
                values.add(path);
                counter++;
            }
        }
        return buildSelect(attributes, options, values, selectedIndex, false);
    }

    /**
     * @see org.opencms.workplace.CmsWorkplace#getCms()
     */
    public CmsObject getCms() {

        if (m_cms == null) {
            return super.getCms();
        }

        return m_cms;
    }

    /**
     * Returns the errorMessage.<p>
     *
     * @return the errorMessage
     */
    public String getErrorMessage() {

        if (CmsStringUtil.isEmpty(m_errorMessage)) {
            return "";
        }

        return m_errorMessage;
    }

    /**
     * Returns the paramLocale.<p>
     *
     * @return the paramLocale
     */
    public String getParamLocale() {

        return m_paramLocale;
    }

    /**
     * Returns the value of the newvalue parameter.<p>
     *
     * @return the value of the newvalue parameter
     */
    public String getParamNewElement() {

        return m_paramNewElement;
    }

    /**
     * Returns the value of the oldvalue parametere.<p>
     *
     * @return the value of the oldvalue parameter
     */
    public String getParamOldElement() {

        return m_paramOldElement;
    }

    /**
     * Returns the value of the recursive parameter.<p>
     *
     * @return the value of the recursive parameter
     */
    public String getParamRecursive() {

        return m_paramRecursive;
    }

    /**
     * Returns true if the user has set remove empty elements parameter; otherwise false.<p>
     *
     * @return true if the user has set remove empty elements parameter; otherwise false
     */
    public String getParamRemoveEmptyElements() {

        return m_paramRemoveEmptyElements;
    }

    /**
     * Returns the template.<p>
     *
     * @return the template
     */
    public String getParamTemplate() {

        return m_paramTemplate;
    }

    /**
     * Returns true if the user has set validate new element parameter; otherwise false.<p>.<p>
     *
     * @return true if the user has set validate new element parameter; otherwise false
     */
    public String getParamValidateNewElement() {

        return m_paramValidateNewElement;
    }

    /**
     * Sets the errorMessage.<p>
     *
     * @param errorMessage the errorMessage to set
     */
    public void setErrorMessage(String errorMessage) {

        m_errorMessage = errorMessage;
    }

    /**
     * Sets the locale.<p>
     *
     * @param paramLocale the locale to set
     */
    public void setParamLocale(String paramLocale) {

        m_paramLocale = paramLocale;
    }

    /**
     * Sets the value of the newvalue parameter.<p>
     *
     * @param paramNewValue the value of the newvalue parameter
     */
    public void setParamNewElement(String paramNewValue) {

        m_paramNewElement = paramNewValue;
    }

    /**
     * Sets the value of the oldvalue parameter.<p>
     *
     * @param paramOldValue the value of the oldvalue parameter
     */
    public void setParamOldElement(String paramOldValue) {

        m_paramOldElement = paramOldValue;
    }

    /**
     * Sets the value of the recursive parameter.<p>
     *
     * @param paramRecursive the value of the recursive parameter
     */
    public void setParamRecursive(String paramRecursive) {

        m_paramRecursive = paramRecursive;
    }

    /**
     * Sets the remove empty elements parameter to true or false.<p>
     *
     * @param paramRemoveEmptyElements the remove empty elements parameter to set
     */
    public void setParamRemoveEmptyElements(String paramRemoveEmptyElements) {

        m_paramRemoveEmptyElements = paramRemoveEmptyElements;
    }

    /**
     * Sets the param Template.<p>
     *
     * @param paramTemplate the template name to set
     */
    public void setParamTemplate(String paramTemplate) {

        m_paramTemplate = paramTemplate;
    }

    /**
     * Sets the paramValidateNewElement.<p>
     *
     * @param paramValidateNewElement the validate new element parameter to set
     */
    public void setParamValidateNewElement(String paramValidateNewElement) {

        m_paramValidateNewElement = paramValidateNewElement;
    }

    /**
     * Does validate the request parameters and returns a buffer with error messages.<p>
     * 
     * If there were no error messages, the buffer is empty.<p>
     */
    public void validateParameters() {

        // localisation  
        Locale locale = getLocale();

        StringBuffer validationErrors = new StringBuffer();
        if (CmsStringUtil.isEmpty(getParamResource())) {
            validationErrors.append(
                Messages.get().key(locale, Messages.GUI_ELEM_RENAME_VALIDATE_RESOURCE_FOLDER_0, null)).append("<br>");
        }
        if (CmsStringUtil.isEmpty(getParamTemplate())) {
            validationErrors.append(
                Messages.get().key(locale, Messages.GUI_ELEM_RENAME_VALIDATE_SELECT_TEMPLATE_0, null)).append("<br>");
        }
        if (CmsStringUtil.isEmpty(getParamLocale())) {
            validationErrors.append(
                Messages.get().key(locale, Messages.GUI_ELEM_RENAME_VALIDATE_SELECT_LANGUAGE_0, null)).append("<br>");
        }
        if (CmsStringUtil.isEmpty(getParamOldElement())) {
            validationErrors.append(
                Messages.get().key(locale, Messages.GUI_ELEM_RENAME_VALIDATE_ENTER_OLD_ELEM_0, null)).append("<br>");
        }
        if (CmsStringUtil.isEmpty(getParamNewElement())) {
            validationErrors.append(
                Messages.get().key(locale, Messages.GUI_ELEM_RENAME_VALIDATE_ENTER_NEW_ELEM_0, null)).append("<br>");
        }
        if (!isValidElement(getParamNewElement())) {
            validationErrors.append(
                Messages.get().key(
                    locale,
                    Messages.GUI_ELEM_RENAME_VALIDATE_INVALID_NEW_ELEM_2,
                    new Object[] {getParamNewElement(), getParamTemplate()})).append("<br>");
        }

        setErrorMessage(validationErrors.toString());
    }

    /**
     * @see org.opencms.workplace.CmsWorkplace#initWorkplaceRequestValues(org.opencms.workplace.CmsWorkplaceSettings, javax.servlet.http.HttpServletRequest)
     */
    protected void initWorkplaceRequestValues(CmsWorkplaceSettings settings, HttpServletRequest request) {

        // fill the parameter values in the get/set methods
        fillParamValues(request);
        // set the dialog type
        setParamDialogtype(DIALOG_TYPE);
        // set the action for the JSP switch 
        // set the action for the JSP switch 
        if (DIALOG_CONFIRMED.equals(getParamAction())) {
            setAction(ACTION_CONFIRMED);
        } else if (DIALOG_OK.equals(getParamAction())) {
            setAction(ACTION_OK);
        } else if (DIALOG_CANCEL.equals(getParamAction())) {
            setAction(ACTION_CANCEL);
        } else if (REPORT_UPDATE.equals(getParamAction())) {
            setAction(ACTION_REPORT_UPDATE);
        } else if (REPORT_BEGIN.equals(getParamAction())) {
            setAction(ACTION_REPORT_BEGIN);
        } else if (REPORT_END.equals(getParamAction())) {
            setAction(ACTION_REPORT_END);
        } else {
            setAction(ACTION_DEFAULT);
            // add the title for the dialog 
            setParamTitle(key("title.renameelement"));
        }
    }

    /**
     * Returns a retained list of xml pages that belongs to the specified template.<p>
     * 
     * @param xmlPages a list of all xml pages 
     * @return a retained list of xml pages that belongs to the specified template
     */
    private List getRetainedPagesWithTemplate(List xmlPages) {

        // list of resources belongs to the selected template
        List resourcesWithTemplate = new ArrayList();
        TreeMap templates = null;
        try {
            templates = CmsNewResourceXmlPage.getTemplates(getCms());
        } catch (CmsException e) {
            if (LOG.isErrorEnabled()) {
                LOG.error(e);
            }
        }
        // check if the users selected template is valid. 
        if (templates != null && templates.containsValue(getParamTemplate())) {
            // iterate the xmlPages list and add all resources with the specified template to the resourcesWithTemplate list
            Iterator i = xmlPages.iterator();
            while (i.hasNext()) {
                CmsResource currentPage = (CmsResource)i.next();
                // read the template property
                CmsProperty templateProperty;
                try {
                    templateProperty = getCms().readPropertyObject(
                        getCms().getSitePath(currentPage),
                        CmsPropertyDefinition.PROPERTY_TEMPLATE,
                        false);
                } catch (CmsException e2) {
                    if (LOG.isErrorEnabled()) {
                        LOG.error(e2);
                    }
                    continue;
                }
                // add currentResource if the template property value is the same as the given template
                if (getParamTemplate().equals(templateProperty.getValue())) {
                    resourcesWithTemplate.add(currentPage);
                }
            }
            // retain the list of pages against the list with template 
            xmlPages.retainAll(resourcesWithTemplate);
        }

        return xmlPages;
    }

    /**
     * Returns a set of elements stored in the given template property.<p>
     * 
     * The elements are stored in the property I_CmsConstants.C_PROPERTY_TEMPLATE_ELEMENTS.<p>
     * 
     * @param currentTemplate the path of the template to look in
     * @return a set of elements stored in the given template path
     */
    private Set getTemplateElements(String currentTemplate) {

        Set templateElements = new HashSet();

        if (currentTemplate != null && currentTemplate.length() > 0) {
            // template found, check template-elements property
            String elements = null;
            try {
                // read the property from the template file
                elements = getCms().readPropertyObject(
                    currentTemplate,
                    CmsPropertyDefinition.PROPERTY_TEMPLATE_ELEMENTS,
                    false).getValue(null);
            } catch (CmsException e) {
                if (LOG.isWarnEnabled()) {
                    LOG.warn(e.getLocalizedMessage());
                }
            }
            if (elements != null) {
                // elements are defined on template file, merge with available elements
                StringTokenizer T = new StringTokenizer(elements, ",");
                while (T.hasMoreTokens()) {
                    String currentElement = T.nextToken();
                    String niceName = null;
                    boolean mandatory = false;
                    int sepIndex = currentElement.indexOf("|");
                    if (sepIndex != -1) {
                        // nice name found for current element, extract it
                        niceName = currentElement.substring(sepIndex + 1);
                        currentElement = currentElement.substring(0, sepIndex);
                    }
                    if (currentElement.endsWith("*")) {
                        // element is mandatory
                        mandatory = true;
                        currentElement = currentElement.substring(0, currentElement.length() - 1);
                    }

                    CmsDialogElement element = new CmsDialogElement(currentElement, niceName, mandatory, true, false);
                    templateElements.add(element);
                }
            }
        }

        return templateElements;
    }

    /**
     * Returns a list of xml pages from the specified folder.<p>
     * 
     * @return a list of xml pages from the specified folder
     */
    private List getXmlPages() {

        boolean isRecursive = Boolean.valueOf(getParamRecursive()).booleanValue();
        // filterdefinition to read only the required resources 
        CmsResourceFilter filter = CmsResourceFilter.IGNORE_EXPIRATION.addRequireType(CmsResourceTypeXmlPage.getStaticTypeId());
        // trying to read the resources
        List xmlPages = null;

        try {
            xmlPages = getCms().readResources(getParamResource(), filter, isRecursive);
        } catch (CmsException e) {
            if (LOG.isErrorEnabled()) {
                LOG.error(e);
            }
        }

        return xmlPages;
    }

    /**
     * Checks if the specified element/locale of the given page has a content.<p>
     * 
     * @param page the xml page
     * @param element the element name
     * @param locale the locale
     * @return false if the specified element/locale of the given page has a content; otherwise true
     */
    private boolean isEmptyElement(CmsXmlPage page, String element, Locale locale) {

        CmsXmlHtmlValue xmlHtmlValue = (CmsXmlHtmlValue)page.getValue(element, locale);
        if (CmsStringUtil.isNotEmpty(xmlHtmlValue.getPlainText(getCms()))) {
            return false;
        }

        return true;
    }

    /** 
     * Checks if the selected new element is valid for the selected template.<p>
     * 
     * @param page the xml page
     * @param element the element name
     * 
     * @return true if ALL_TEMPLATES selected or the element is valid for the selected template; otherwise false
     */
    private boolean isValidElement(CmsXmlPage page, String element) {

        CmsFile file = page.getFile();
        String template;
        try {
            template = getCms().readPropertyObject(
                getCms().getSitePath(file),
                CmsPropertyDefinition.PROPERTY_TEMPLATE,
                true).getValue(null);
        } catch (CmsException e) {
            return false;
        }

        return isValidTemplateElement(template, element);
    }

    /** 
     * Checks if the selected new element is valid for the selected template.<p>
     * 
     * @param element the element name
     * 
     * @return true if ALL_TEMPLATES selected or the element is valid for the selected template; otherwise false
     */
    private boolean isValidElement(String element) {

        boolean validateNewElement = Boolean.valueOf(getParamValidateNewElement()).booleanValue();
        if (C_ALL.equals(getParamTemplate()) || !validateNewElement) {
            return true;
        }

        return isValidTemplateElement(getParamTemplate(), element);
    }

    /**
     * Check if the given template includes the specified element.<p>
     *  
     * @param template the template
     * @param element the element name
     * @return true if the template includes the given element
     */
    private boolean isValidTemplateElement(String template, String element) {

        List elements = new ArrayList(getTemplateElements(template));
        if (elements != null) {
            Iterator i = elements.iterator();
            while (i.hasNext()) {
                CmsDialogElement currElement = (CmsDialogElement)i.next();
                if (element.equals(currElement.getName())) {
                    return true;
                }
            }
        }

        return false;
    }

    /**
     * Performs the main element rename operation on the filtered resources.<p>
     * 
     * @param xmlPages the list of xml pages
     * @param locale the locale specifying the xmlpage node to perform the operation on
     */
    private void performRenameOperation(List xmlPages, Locale locale) {

        // partial localized (stopped due to low prio).
        boolean removeEmptyElements = Boolean.valueOf(getParamRemoveEmptyElements()).booleanValue();
        boolean validateNewElement = Boolean.valueOf(getParamValidateNewElement()).booleanValue();
        // the list including at least one resource
        if (xmlPages != null && xmlPages.size() > 0) {
            m_report.println(
                Messages.get().container(Messages.RPT_RENAME_LANG_1, locale.getLanguage()),
                I_CmsReport.C_FORMAT_HEADLINE);
            // if user has not selected ALL templates, then retain pages with specified template
            if (!C_ALL.equals(getParamTemplate())) {
                xmlPages = getRetainedPagesWithTemplate(xmlPages);
            }
            int m = 0;
            int n = xmlPages.size();
            // loop over remained pages
            Iterator i = xmlPages.iterator();
            while (i.hasNext()) {
                m++;
                CmsXmlPage page = null;
                try {
                    // next file from the list
                    CmsResource res = (CmsResource)i.next();
                    CmsFile file;

                    m_report.print(org.opencms.report.Messages.get().container(
                        org.opencms.report.Messages.RPT_SUCCESSION_2,
                        String.valueOf(m),
                        String.valueOf(n)), I_CmsReport.C_FORMAT_NOTE);
                    m_report.print(Messages.get().container(Messages.RPT_PROCESSING_PAGE_0), I_CmsReport.C_FORMAT_NOTE);
                    m_report.print(org.opencms.report.Messages.get().container(
                        org.opencms.report.Messages.RPT_ARGUMENT_1,
                        getCms().getSitePath(res)));
                    m_report.println(org.opencms.report.Messages.get().container(org.opencms.report.Messages.RPT_DOTS_0));

                    try {
                        file = getCms().readFile(getCms().getSitePath(res), CmsResourceFilter.IGNORE_EXPIRATION);
                    } catch (CmsException e2) {
                        if (LOG.isErrorEnabled()) {
                            LOG.error(e2);
                        }
                        m_report.println(e2);
                        continue;
                    }
                    // try unmarshaling to xml page
                    try {
                        page = CmsXmlPageFactory.unmarshal(getCms(), file);
                    } catch (CmsXmlException e) {
                        m_report.println(e);
                        continue;
                    }

                    // check if the source element exists in the page
                    if (!page.hasValue(getParamOldElement(), locale)) {
                        m_report.println(
                            Messages.get().container(Messages.RPT_NONEXISTANT_ELEM_1, getParamOldElement()),
                            I_CmsReport.C_FORMAT_NOTE);
                        continue;
                    }

                    // check if the target element already exists in the page
                    if (page.hasValue(getParamNewElement(), locale)) {
                        // the page contains already the new element with speicific content. 
                        // renaming the old will invalid the xml page
                        m_report.println(
                            Messages.get().container(Messages.RPT_NEW_ELEM_EXISTS_0),
                            I_CmsReport.C_FORMAT_NOTE);
                        continue;
                    }

                    if (validateNewElement) {
                        // check if the target element is valid for the template
                        if (!isValidElement(page, getParamNewElement())) {
                            m_report.println(Messages.get().container(
                                Messages.RPT_INVALID_ARGUMENT_1,
                                getParamNewElement()), I_CmsReport.C_FORMAT_NOTE);
                            continue;
                        }
                    }

                    try {
                        // rename the element from the old value to the new
                        page.renameValue(getParamOldElement(), getParamNewElement(), locale);
                        // write the page with the new content
                        writePageAndReport(page, true);
                    } catch (Throwable t) {
                        LOG.error(t);
                        m_report.println(t);
                        continue;
                    }

                } catch (Throwable t) {
                    LOG.error(t);
                    m_report.println(t);
                } finally {
                    // finally do remove empty elements of the page
                    // the remove operation is executed if the user has checked the specified checkbox and selected a template (NOT ALL)
                    if (removeEmptyElements) {
                        removeInValidElements(page, locale);
                    }
                }
            }
        }
    }

    /**
     * Analyzes xml page and removes any element if this is not valid for the specified template and has no content.<p>
     * 
     * @param resources a list of xml pages
     * @param locale the locale
     */
    private void removeInValidElements(CmsXmlPage page, Locale locale) {

        if (page == null) {
            return;
        }

        if (C_ALL.equals(getParamTemplate())) {
            return;
        }

        // get all elements of this page
        List pageElements = page.getNames(locale);
        if (pageElements != null) {
            Iterator i = pageElements.iterator();
            while (i.hasNext()) {
                String currElement = (String)i.next();
                // remove current element only is invalid and has no content 
                if (!isValidElement(currElement) && isEmptyElement(page, currElement, locale)) {
                    page.removeValue(currElement, locale);
                    try {
                        writePageAndReport(page, false);
                        m_report.println(
                            Messages.get().container(Messages.RPT_REMOVE_INVALID_EMPTY_ELEM_1, currElement),
                            I_CmsReport.C_FORMAT_NOTE);
                    } catch (CmsException e) {
                        // ignore
                    }
                }
            }
        }
    }

    /**
     * Writes the given xml page by reporting the result.<p>
     * 
     * @param page the xml page
     * @param report if true then some output will be written to the report
     * @throws CmsException if operation failed
     */
    private void writePageAndReport(CmsXmlPage page, boolean report) throws CmsException {

        CmsFile file = page.getFile();
        byte[] content = page.marshal();
        file.setContents(content);
        // check lock            
        CmsLock lock = getCms().getLock(file);
        if (lock.isNullLock() || lock.getUserId().equals(getCms().getRequestContext().currentUser().getId())) {
            // lock the page
            checkLock(getCms().getSitePath(file));
            // write the file with the new content
            getCms().writeFile(file);
            // unlock the page
            getCms().unlockResource(getCms().getSitePath(file));
            if (report) {
                m_report.println(Messages.get().container(
                    Messages.RPT_ELEM_RENAME_2,
                    getParamOldElement(),
                    getParamNewElement()), I_CmsReport.C_FORMAT_OK);
            }
        }
    }
}