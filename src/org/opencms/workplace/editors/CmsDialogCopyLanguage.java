/*
 * This library is part of OpenCms -
 * the Open Source Content Mananagement System
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
import org.opencms.file.CmsResourceFilter;
import org.opencms.i18n.CmsLocaleManager;
import org.opencms.jsp.CmsJspActionElement;
import org.opencms.main.CmsException;
import org.opencms.main.CmsLog;
import org.opencms.main.OpenCms;
import org.opencms.util.CmsUUID;
import org.opencms.workplace.CmsDialog;
import org.opencms.workplace.CmsWorkplaceSettings;
import org.opencms.xml.content.CmsXmlContent;
import org.opencms.xml.content.CmsXmlContentFactory;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.PageContext;

import org.apache.commons.logging.Log;

/**
 * Provides methods for the editor copy language dialog.<p>
 *
 * The following files use this class:
 * <ul>
 * <li>/jsp/editors/dialogs/copylanguage.html
 * </ul>
 * <p>
 *
 * @since 6.5.6
 */
public class CmsDialogCopyLanguage extends CmsDialog {

    /** Value for the action: update languages of the page. */
    public static final int ACTION_UPDATE_LANGUAGES = 310;

    /** The dialog type. */
    public static final String DIALOG_TYPE = "copylanguages";

    /** Request parameter value for the action: update the elements of the page. */
    public static final String DIALOG_UPDATE_LANGUAGES = "updatelanguages";

    /** Param name for the html checkbox field for the language. */
    public static final String PARAM_LANGUAGE = "language";

    /** The log object for this class. */
    private static final Log LOG = CmsLog.getLog(CmsDialogCopyLanguage.class);

    /** The element locale. */
    private Locale m_elementLocale;

    /** Element language parameter. */
    private String m_paramElementlanguage;

    /** The selected languages. */
    private Set<String> m_paramSelectedLanguages;

    /** Temporary file parameter. */
    private String m_paramTempFile;

    /**
     * Public constructor.<p>
     *
     * @param jsp an initialized JSP action element
     */
    public CmsDialogCopyLanguage(CmsJspActionElement jsp) {

        super(jsp);
    }

    /**
     * Public constructor with JSP variables.<p>
     *
     * @param context the JSP page context
     * @param req the JSP request
     * @param res the JSP response
     */
    public CmsDialogCopyLanguage(PageContext context, HttpServletRequest req, HttpServletResponse res) {

        this(new CmsJspActionElement(context, req, res));
    }

    /**
     * Updates the languages of the current xmlcontent by copying from the current language.<p>
     *
     * @throws JspException if there is an error including the error page
     */
    public void actionUpdateLanguages() throws JspException {

        if ((m_paramSelectedLanguages != null) && !m_paramSelectedLanguages.isEmpty()) {
            try {
                CmsFile file = getCms().readFile(getParamTempfile(), CmsResourceFilter.IGNORE_EXPIRATION);
                CmsXmlContent content = CmsXmlContentFactory.unmarshal(getCms(), file);

                List<Locale> toLocales = new ArrayList<Locale>();
                for (Iterator<String> i = m_paramSelectedLanguages.iterator(); i.hasNext();) {
                    String language = i.next();
                    toLocales.add(CmsLocaleManager.getLocale(language));
                }

                // now transfer the contents
                transferContents(content, getElementLocale(), toLocales);

                // write the temporary file
                String decodedContent = content.toString();
                try {
                    file.setContents(decodedContent.getBytes(content.getEncoding()));
                } catch (UnsupportedEncodingException e) {
                    throw new CmsException(
                        Messages.get().container(Messages.ERR_INVALID_CONTENT_ENC_1, getParamResource()),
                        e);
                }
                // the file content might have been modified during the write operation
                CmsObject cloneCms = OpenCms.initCmsObject(getCms());
                CmsUUID tempProjectId = OpenCms.getWorkplaceManager().getTempFileProjectId();
                cloneCms.getRequestContext().setCurrentProject(getCms().readProject(tempProjectId));
                cloneCms.writeFile(file);

            } catch (Throwable e) {
                // show error dialog
                setParamMessage(Messages.get().getBundle(getLocale()).key(Messages.ERR_UPDATE_LANGUAGES_0));
                includeErrorpage(this, e);
            }
        }
    }

    /**
     * Builds the html String for a form list of all possible page elements.<p>
     *
     * @return the html String for a form list
     */
    public String buildLanguageList() {

        try {
            StringBuffer retValue = new StringBuffer(512);
            retValue.append("<table border=\"0\">\n");

            // get locale for current element
            Locale elLocale = getElementLocale();

            // get locale names based on properties and global settings
            List<Locale> localeList = OpenCms.getLocaleManager().getAvailableLocales(getCms(), getParamTempfile());

            // read xml content for checking locale availability
            CmsFile file = getCms().readFile(getParamTempfile(), CmsResourceFilter.IGNORE_EXPIRATION);
            CmsXmlContent content = CmsXmlContentFactory.unmarshal(getCms(), file);

            // show all possible elements
            Iterator<Locale> i = localeList.iterator();
            while (i.hasNext()) {
                // get the current list element
                Locale curLocale = i.next();

                // skip locale of current element
                if (elLocale.equals(curLocale)) {
                    continue;
                }

                // build an element row
                retValue.append("<tr>\n");
                retValue.append("\t<td class=\"textcenter\" unselectable=\"on\"><input type=\"checkbox\" name=\"");
                retValue.append(PARAM_LANGUAGE);
                retValue.append("\" value=\"");
                retValue.append(curLocale.toString());
                retValue.append("\">");
                retValue.append("</td>\n");
                retValue.append("\t<td style=\"white-space: nowrap;\" unselectable=\"on\">");
                retValue.append(curLocale.getDisplayName(getLocale()));
                retValue.append(!content.hasLocale(curLocale) ? " [-]" : "");
                retValue.append("</td>\n");
                retValue.append("\t<td style=\"white-space: nowrap;\" unselectable=\"on\">");
                retValue.append(
                    !content.hasLocale(curLocale)
                    ? Messages.get().getBundle(getLocale()).key(Messages.GUI_EDITOR_DIALOG_COPYLANGUAGE_NEW_0)
                    : "");
                retValue.append("</td>\n");

                retValue.append("</tr>\n");
            }

            retValue.append("</table>\n");
            return retValue.toString();

        } catch (Throwable e) {
            // should usually never happen
            if (LOG.isInfoEnabled()) {
                LOG.info(e);
            }
            return "";
        }
    }

    /**
     * Returns the current element locale.<p>
     *
     * @return the current element locale
     */
    public Locale getElementLocale() {

        if (m_elementLocale == null) {
            m_elementLocale = CmsLocaleManager.getLocale(getParamElementLanguage());
        }
        return m_elementLocale;
    }

    /**
     * Returns the current element language.<p>
     *
     * @return the current element language
     */
    public String getParamElementLanguage() {

        return m_paramElementlanguage;
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
     * Sets the current language.<p>
     *
     * @param elementLanguage the current element language
     */
    public void setParamElementLanguage(String elementLanguage) {

        m_paramElementlanguage = elementLanguage;
    }

    /**
     * Sets the list of selected languages.<p>
     *
     * @param language a selected language
     */
    public void setParamLanguage(String language) {

        if (language != null) {
            if (m_paramSelectedLanguages == null) {
                m_paramSelectedLanguages = new HashSet<String>();
            }
            // add all available values here
            String[] values = getParameterMap().get(PARAM_LANGUAGE);
            for (int i = 0; i < values.length; i++) {
                m_paramSelectedLanguages.add(decodeParamValue(PARAM_LANGUAGE, values[i]));
            }
        }
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
        if (DIALOG_UPDATE_LANGUAGES.equals(getParamAction())) {
            setAction(ACTION_UPDATE_LANGUAGES);
        } else {
            setAction(ACTION_DEFAULT);
            // build title for delete dialog
            setParamTitle(key(Messages.GUI_EDITOR_DIALOG_COPYLANGUAGE_TITLE_0));
        }
    }

    /**
     * Copies the contents from a source locale to a number of destination locales by overwriting them.<p>
     *
     * @param content the xml content
     * @param sourceLocale the source locale
     * @param destLocales a list of destination locales
     * @throws CmsException if something goes wrong
     */
    protected void transferContents(CmsXmlContent content, Locale sourceLocale, List<Locale> destLocales)
    throws CmsException {

        for (Iterator<Locale> i = destLocales.iterator(); i.hasNext();) {
            Locale to = i.next();
            if (content.hasLocale(to)) {
                content.removeLocale(to);
            }
            content.copyLocale(sourceLocale, to);
        }
    }
}