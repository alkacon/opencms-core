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

package org.opencms.workplace.tools.searchindex;

import org.opencms.configuration.CmsSearchConfiguration;
import org.opencms.jsp.CmsJspActionElement;
import org.opencms.main.CmsIllegalStateException;
import org.opencms.main.OpenCms;
import org.opencms.search.CmsSearchManager;
import org.opencms.search.fields.CmsLuceneFieldConfiguration;
import org.opencms.search.fields.CmsSearchField;
import org.opencms.search.fields.CmsSearchFieldConfiguration;
import org.opencms.workplace.CmsWidgetDialog;
import org.opencms.workplace.CmsWorkplaceSettings;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.jsp.PageContext;

/**
 *
 * Abstract widget dialog for all dialogs working with <code>{@link CmsLuceneFieldConfiguration}</code>.<p>
 *
 * @since 6.5.5
 */
public class A_CmsFieldConfigurationDialog extends CmsWidgetDialog {

    /** localized messages Keys prefix. */
    public static final String KEY_PREFIX = "fieldconfiguration";

    /** Defines which pages are valid for this dialog. */
    public static final String[] PAGES = {"page1"};

    /** The request parameter for the fieldconfiguration to work with when contacting
     * this dialog from another. */
    public static final String PARAM_FIELDCONFIGURATION = "fieldconfiguration";

    /** The user object that is edited on this dialog. */
    protected CmsSearchFieldConfiguration m_fieldconfiguration;

    /** The search manager singleton for convenient access. **/
    protected CmsSearchManager m_searchManager;

    /** Stores the value of the request parameter for the search index Name. */
    private String m_paramFieldConfiguration;

    /**
     * Public constructor with JSP action element.<p>
     *
     * @param jsp an initialized JSP action element
     */
    public A_CmsFieldConfigurationDialog(CmsJspActionElement jsp) {

        super(jsp);
    }

    /**
     * Public constructor with JSP variables.<p>
     *
     * @param context the JSP page context
     * @param req the JSP request
     * @param res the JSP response
     */
    public A_CmsFieldConfigurationDialog(PageContext context, HttpServletRequest req, HttpServletResponse res) {

        this(new CmsJspActionElement(context, req, res));
    }

    /**
     * Writes the updated search configuration back to the XML
     * configuration file and refreshes the complete list.<p>
     */
    protected static void writeConfiguration() {

        // update the XML configuration
        OpenCms.writeConfiguration(CmsSearchConfiguration.class);
    }

    /**
     * @see org.opencms.workplace.CmsWidgetDialog#actionCommit()
     */
    @Override
    public void actionCommit() {

        List<Throwable> errors = new ArrayList<Throwable>();

        try {
            // if new create it first
            if (m_searchManager.getFieldConfiguration(m_fieldconfiguration.getName()) == null) {
                m_searchManager.addFieldConfiguration(m_fieldconfiguration);
            }
            if (checkWriteConfiguration()) {
                writeConfiguration();
            }
        } catch (Throwable t) {
            errors.add(t);
        }

        // set the list of errors to display when saving failed
        setCommitErrors(errors);

    }

    /**
     * Returns the request parameter value for parameter fieldconfiguration. <p>
     *
     * @return the request parameter value for parameter fieldconfiguration
     */
    public String getParamFieldconfiguration() {

        return m_paramFieldConfiguration;
    }

    /**
     * Sets the request parameter value for parameter fieldconfiguration. <p>
     *
     * @param fieldconfiguration the request parameter value for parameter fieldconfiguration
     */
    public void setParamFieldconfiguration(String fieldconfiguration) {

        m_paramFieldConfiguration = fieldconfiguration;
    }

    /**
     * Creates the dialog HTML for all defined widgets of the named dialog (page).<p>
     *
     * This overwrites the method from the super class to create a layout variation for the widgets.<p>
     *
     * @param dialog the dialog (page) to get the HTML for
     * @return the dialog HTML for all defined widgets of the named dialog (page)
     */
    @Override
    protected String createDialogHtml(String dialog) {

        StringBuffer result = new StringBuffer(1024);

        result.append(createWidgetTableStart());
        // show error header once if there were validation errors
        result.append(createWidgetErrorHeader());

        if (dialog.equals(PAGES[0])) {
            // create the widgets for the first dialog page
            result.append(dialogBlockStart(key(Messages.GUI_LABEL_FIELDCONFIGURATION_BLOCK_SETTINGS_0)));
            result.append(createWidgetTableStart());
            result.append(createDialogRowsHtml(0, 0));
            result.append(createWidgetTableEnd());
            result.append(dialogBlockEnd());
        }

        result.append(createWidgetTableEnd());

        // Output the list with document types:
        return result.toString();
    }

    /**
     * @see org.opencms.workplace.CmsWidgetDialog#defaultActionHtmlEnd()
     */
    @Override
    protected String defaultActionHtmlEnd() {

        return "";
    }

    /**
     * @see org.opencms.workplace.CmsWidgetDialog#defineWidgets()
     */
    @Override
    protected void defineWidgets() {

        initUserObject();
        setKeyPrefix(KEY_PREFIX);
    }

    /**
     * @see org.opencms.workplace.CmsWidgetDialog#getPageArray()
     */
    @Override
    protected String[] getPageArray() {

        return PAGES;
    }

    /**
     * Returns the root path of this dialog (path relative to "/system/workplace/admin").<p>
     *
     * @return the root path of this dialog (path relative to "/system/workplace/admin")
     */
    protected String getToolPath() {

        return "/searchindex/fieldconfigurations/fieldconfiguration";
    }

    /**
     * @see org.opencms.workplace.CmsWorkplace#initMessages()
     */
    @Override
    protected void initMessages() {

        // add specific dialog resource bundle
        addMessages(Messages.get().getBundleName());
        // add default resource bundles
        super.initMessages();
    }

    /**
     * Initializes the user object to work with depending on the dialog state and request parameters.<p>
     *
     */
    protected void initUserObject() {

        if (m_fieldconfiguration == null) {
            try {
                m_fieldconfiguration = m_searchManager.getFieldConfiguration(getParamFieldconfiguration());
                if (m_fieldconfiguration == null) {
                    m_fieldconfiguration = new CmsLuceneFieldConfiguration();
                }
            } catch (Exception e) {
                m_fieldconfiguration = new CmsLuceneFieldConfiguration();
            }
        }
    }

    /**
     * Overridden to initialize the internal <code>CmsSearchManager</code> before initWorkplaceRequestValues ->
     * defineWidgets ->  will access it (NPE). <p>
     *
     * @see org.opencms.workplace.CmsWorkplace#initWorkplaceMembers(org.opencms.jsp.CmsJspActionElement)
     */
    @Override
    protected void initWorkplaceMembers(CmsJspActionElement jsp) {

        m_searchManager = OpenCms.getSearchManager();
        super.initWorkplaceMembers(jsp);
    }

    /**
     * @see org.opencms.workplace.CmsWorkplace#initWorkplaceRequestValues(org.opencms.workplace.CmsWorkplaceSettings, javax.servlet.http.HttpServletRequest)
     */
    @Override
    @SuppressWarnings({"unchecked", "rawtypes"})
    protected void initWorkplaceRequestValues(CmsWorkplaceSettings settings, HttpServletRequest request) {

        // initialize parameters and dialog actions in super implementation
        super.initWorkplaceRequestValues(settings, request);

        // save the current search index source
        Map dialogObject = (Map)getDialogObject();
        if (dialogObject == null) {
            dialogObject = new HashMap();
            dialogObject.put(PARAM_FIELDCONFIGURATION, m_fieldconfiguration);
            setDialogObject(dialogObject);
        }

    }

    /**
     * Checks if the new search index dialog has to be displayed.<p>
     *
     * @return <code>true</code> if the new search index dialog has to be displayed
     */
    protected boolean isNewFieldConfiguration() {

        return DIALOG_INITIAL.equals(getParamAction());
    }

    /**
     * @see org.opencms.workplace.CmsWidgetDialog#validateParamaters()
     */
    @Override
    protected void validateParamaters() throws Exception {

        if (!isNewFieldConfiguration()) {
            // test the needed parameters
            if ((getParamFieldconfiguration() == null) && (getJsp().getRequest().getParameter("name.0") == null)) {
                throw new CmsIllegalStateException(
                    Messages.get().container(Messages.ERR_SEARCHINDEX_EDIT_MISSING_PARAM_1, PARAM_FIELDCONFIGURATION));
            }
        }
    }

    /**
     * Checks the configuration to write.<p>
     *
     * @return true if configuration is valid, otherwise false
     */
    private boolean checkWriteConfiguration() {

        if ((m_fieldconfiguration != null) || m_fieldconfiguration.getFields().isEmpty()) {
            for (CmsSearchField field : m_fieldconfiguration.getFields()) {
                if (field.getMappings().isEmpty()) {
                    return false;
                }
            }
            return true;
        }
        return false;
    }
}
