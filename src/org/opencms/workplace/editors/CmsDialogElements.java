/*
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (c) Alkacon Software GmbH & Co. KG (http://www.alkacon.com)
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
 * For further information about Alkacon Software GmbH & Co. KG, please see the
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
import org.opencms.file.CmsObject;
import org.opencms.file.CmsPropertyDefinition;
import org.opencms.file.CmsResource;
import org.opencms.file.CmsResourceFilter;
import org.opencms.i18n.CmsLocaleManager;
import org.opencms.jsp.CmsJspActionElement;
import org.opencms.main.CmsException;
import org.opencms.main.CmsLog;
import org.opencms.util.CmsStringUtil;
import org.opencms.workplace.CmsDialog;
import org.opencms.workplace.CmsWorkplaceSettings;
import org.opencms.xml.page.CmsXmlPage;
import org.opencms.xml.page.CmsXmlPageFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.PageContext;

import org.apache.commons.logging.Log;

/**
 * Provides methods for the editor elements dialog.<p>
 *
 * The following files use this class:
 * <ul>
 * <li>/jsp/editors/dialogs/elements.html
 * </ul>
 * <p>
 *
 * @since 6.0.0
 */
public class CmsDialogElements extends CmsDialog {

    /** Value for the action: update the elements of the page. */
    public static final int ACTION_UPDATE_ELEMENTS = 210;

    /** The dialog type. */
    public static final String DIALOG_TYPE = "elementselector";

    /** Request parameter value for the action: update the elements of the page. */
    public static final String DIALOG_UPDATE_ELEMENTS = "updateelements";

    /** Prefix for the html input field for the body. */
    public static final String PREFIX_PARAM_BODY = "element-";

    /** The log object for this class. */
    private static final Log LOG = CmsLog.getLog(CmsDialogElements.class);

    /** Stores the element to change to after an element update operation. */
    private String m_changeElement;

    /** List used to store information of all possible elements of the page. */
    private List<CmsDialogElement> m_elementList;

    /** The element locale. */
    private Locale m_elementLocale;

    // Special parameters used by this dialog
    /** The element language parameter. */
    private String m_paramElementlanguage;

    /** The element name parameter. */
    private String m_paramElementname;

    /** The temporary file parameter. */
    private String m_paramTempFile;

    /**
     * Public constructor.<p>
     *
     * @param jsp an initialized JSP action element
     */
    public CmsDialogElements(CmsJspActionElement jsp) {

        super(jsp);
        m_changeElement = "";
    }

    /**
     * Public constructor with JSP variables.<p>
     *
     * @param context the JSP page context
     * @param req the JSP request
     * @param res the JSP response
     */
    public CmsDialogElements(PageContext context, HttpServletRequest req, HttpServletResponse res) {

        this(new CmsJspActionElement(context, req, res));
        m_changeElement = "";
    }

    /**
     * Creates a list of possible elements of a template from the template property "template-elements"
     * and the elements available in the provided xmlPage.<p>
     *
     * @param cms the CmsObject
     * @param xmlPage the resource to read the elements from
     * @param xmlPageUri the URI of the resource to read the template property from
     * @param locale the current element locale
     * @return the list of elements in a String array with element name, nice name (if present) and mandatory flag
     */
    public static List<CmsDialogElement> computeElements(
        CmsObject cms,
        CmsXmlPage xmlPage,
        String xmlPageUri,
        Locale locale) {

        List<CmsDialogElement> result = new ArrayList<CmsDialogElement>();

        if (xmlPage != null) {
            List<String> elementNames = xmlPage.getNames(locale);

            Iterator<String> i = elementNames.iterator();
            while (i.hasNext()) {
                String name = i.next();
                CmsDialogElement element = new CmsDialogElement(name, null, false, false, true);
                result.add(element);
            }
        }

        String currentTemplate = null;
        try {
            currentTemplate = cms.readPropertyObject(
                xmlPageUri,
                CmsPropertyDefinition.PROPERTY_TEMPLATE,
                true).getValue();
        } catch (CmsException e) {
            if (LOG.isWarnEnabled()) {
                LOG.warn(e.getLocalizedMessage(), e);
            }
        }
        if ((currentTemplate != null) && (currentTemplate.length() > 0)) {
            // template found, check template-elements property
            String elements = null;
            try {
                // read the property from the template file
                elements = cms.readPropertyObject(
                    currentTemplate,
                    CmsPropertyDefinition.PROPERTY_TEMPLATE_ELEMENTS,
                    false).getValue(null);
            } catch (CmsException e) {
                if (LOG.isWarnEnabled()) {
                    LOG.warn(e.getLocalizedMessage(), e);
                }
            }
            if (elements != null) {
                // elements are defined on template file, merge with available elements
                List<String> tokens = CmsStringUtil.splitAsList(elements, ',', true);
                Iterator<String> it = tokens.iterator();
                while (it.hasNext()) {
                    String currentElement = it.next();
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
                    if (result.contains(element)) {
                        element.setExisting(true);
                        result.remove(element);
                    }
                    result.add(element);
                }
            }
        }

        Collections.sort(result);
        return result;
    }

    /**
     * Creates a list of possible elements of a template from the template property "template-elements"
     * and the elements available in the provided resource file.<p>
     *
     * @param cms the CmsObject
     * @param xmlPageUri the resource to read the elements from
     * @param locale the current element locale
     * @return the list of elements in a String array with element name, nice name (if present) and mandatory flag
     */
    public static List<CmsDialogElement> computeElements(CmsObject cms, String xmlPageUri, Locale locale) {

        CmsXmlPage page = null;
        try {
            // read the xmlpage file
            CmsFile pageFile = cms.readFile(xmlPageUri, CmsResourceFilter.IGNORE_EXPIRATION);
            page = CmsXmlPageFactory.unmarshal(cms, pageFile);
        } catch (CmsException e) {
            LOG.warn(Messages.get().getBundle().key(Messages.LOG_READ_XMLPAGE_FAILED_1, xmlPageUri), e);
            // xmlpage will be null, only "template-elements" property on template will be checked
        }
        return computeElements(cms, page, xmlPageUri, locale);
    }

    /**
     * Updates the enabled/diabled status of all elements of the current page.<p>
     *
     * @throws JspException if there is an error including the error page
     */
    public void actionUpdateElements() throws JspException {

        try {
            List<CmsDialogElement> elementList = computeElements();
            CmsFile file = getCms().readFile(getParamTempfile(), CmsResourceFilter.IGNORE_EXPIRATION);
            CmsXmlPage page = CmsXmlPageFactory.unmarshal(getCms(), file);
            boolean foundMandatory = false;
            m_changeElement = "";
            Iterator<CmsDialogElement> i = elementList.iterator();
            while (i.hasNext()) {
                // get the current list element
                CmsDialogElement element = i.next();
                if (element.isMandantory()
                    || element.getName().equals(getParamElementname())
                    || Boolean.valueOf(
                        getJsp().getRequest().getParameter(PREFIX_PARAM_BODY + element.getName())).booleanValue()) {
                    if (!element.isExisting()) {
                        // create element in order to enable it properly
                        page.addValue(element.getName(), getElementLocale());
                    }
                    page.setEnabled(element.getName(), getElementLocale(), true);
                    if (element.isMandantory() && !foundMandatory) {
                        m_changeElement = element.getName();
                        foundMandatory = true;
                    }
                } else {
                    if (element.isExisting()) {
                        // remove element if it is already existing
                        page.removeValue(element.getName(), getElementLocale());
                    }
                }
            }
            // write the temporary file
            file.setContents(page.marshal());
            getCms().writeFile(file);
            // set the javascript functions which should be executed
            if (page.isEnabled(getParamElementname(), getElementLocale())) {
                m_changeElement = getParamElementname();
            } else if (!foundMandatory) {
                if (elementList.size() > 0) {
                    m_changeElement = elementList.get(0).getName();
                }
            }
        } catch (Throwable e) {
            // show error dialog
            setParamMessage(Messages.get().getBundle(getLocale()).key(Messages.ERR_UPDATE_ELEMENTS_0));
            includeErrorpage(this, e);
        }
    }

    /**
     * Builds the html String for a form list of all possible page elements.<p>
     *
     * @return the html String for a form list
     */
    public String buildElementList() {

        StringBuffer retValue = new StringBuffer(512);
        retValue.append("<table border=\"0\">\n");
        retValue.append("<tr>\n");
        retValue.append(
            "\t<td class=\"textbold\" unselectable=\"on\">"
                + key(Messages.GUI_EDITOR_DIALOG_ELEMENTS_PAGEELEMENT_0)
                + "</td>\n");
        retValue.append("\t<td class=\"textbold\" unselectable=\"on\">&nbsp;&nbsp;"
            + key(Messages.GUI_EDITOR_DIALOG_ELEMENTS_ENABLED_0)
            + "&nbsp;&nbsp;</td>\n");
        retValue.append("</tr>\n");
        retValue.append("<tr><td colspan=\"2\"><span style=\"height: 6px;\"></span></td></tr>\n");

        try {

            // get the list of all possible elements
            List<CmsDialogElement> elementList = computeElements();

            // get all present bodies from the temporary file
            CmsFile file = getCms().readFile(getParamTempfile(), CmsResourceFilter.IGNORE_EXPIRATION);
            CmsXmlPage page = CmsXmlPageFactory.unmarshal(getCms(), file);

            // show all possible elements
            Iterator<CmsDialogElement> i = elementList.iterator();
            while (i.hasNext()) {
                // get the current list element
                CmsDialogElement element = i.next();
                // build an element row
                retValue.append("<tr>\n");
                retValue.append("\t<td style=\"white-space: nowrap;\" unselectable=\"on\">" + element.getNiceName());
                retValue.append("</td>\n");
                retValue.append("\t<td class=\"textcenter\" unselectable=\"on\"><input type=\"checkbox\" name=\"");
                retValue.append(PREFIX_PARAM_BODY);
                retValue.append(element.getName());
                retValue.append("\" value=\"true\"");

                if ((!page.hasValue(element.getName(), getElementLocale()) && element.isMandantory())
                    || page.isEnabled(element.getName(), getElementLocale())) {
                    retValue.append(" checked=\"checked\"");
                }
                if (element.isMandantory() || element.getName().equals(getParamElementname())) {
                    retValue.append(" disabled=\"disabled\"");
                }
                retValue.append(">");
                retValue.append("<script type=\"text/javascript\">registerElement(\"");

                retValue.append(element.getName());
                retValue.append("\", ");
                retValue.append(page.isEnabled(element.getName(), getElementLocale()));
                retValue.append(");</script>");

                retValue.append("</td>\n");
                retValue.append("</tr>\n");
            }

        } catch (CmsException e) {
            // should usually never happen
            if (LOG.isInfoEnabled()) {
                LOG.info(e);
            }
        }

        retValue.append("</table>\n");
        return retValue.toString();
    }

    /**
     * Creates a list of possible elements of a template from the template property "template-elements".<p>
     *
     * @return the list of elements in a String array with element name, nice name (if present) and mandatory flag
     */
    public List<CmsDialogElement> computeElements() {

        if (m_elementList == null) {
            m_elementList = computeElements(getCms(), getParamTempfile(), getElementLocale());
        }
        return m_elementList;
    }

    /**
     * Returns the element name that has to be changed.<p>
     *
     * @return the element name that has to be changed
     */
    public String getChangeElement() {

        return m_changeElement;
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
     * Returns the current element language.<p>
     *
     * @return the current element language
     */
    public String getParamElementlanguage() {

        return m_paramElementlanguage;
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
     * Returns the name of the temporary file.<p>
     *
     * @return the name of the temporary file
     */
    public String getParamTempfile() {

        return m_paramTempFile;
    }

    /**
     * Sets the current element language.<p>
     *
     * @param elementLanguage the current element language
     */
    public void setParamElementlanguage(String elementLanguage) {

        m_paramElementlanguage = elementLanguage;
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
     * Sets the name of the temporary file.<p>
     *
     * @param fileName the name of the temporary file
     */
    public void setParamTempfile(String fileName) {

        m_paramTempFile = fileName;
    }

    /**
     * @see org.opencms.workplace.CmsWorkplace#initWorkplaceRequestValues(org.opencms.workplace.CmsWorkplaceSettings, javax.servlet.http.HttpServletRequest)
     */
    @Override
    protected void initWorkplaceRequestValues(CmsWorkplaceSettings settings, HttpServletRequest request) {

        // fill the parameter values in the get/set methods
        fillParamValues(request);
        // set the dialog type
        setParamDialogtype(DIALOG_TYPE);
        // set the action for the JSP switch
        if (DIALOG_UPDATE_ELEMENTS.equals(getParamAction())) {
            setAction(ACTION_UPDATE_ELEMENTS);
        } else {
            setAction(ACTION_DEFAULT);
            // build title for delete dialog
            setParamTitle(key(
                Messages.GUI_EDITOR_DIALOG_ELEMENTS_TITLE_1,
                new Object[] {CmsResource.getName(getParamResource())}));
        }
    }
}