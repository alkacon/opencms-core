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
import org.opencms.search.fields.CmsLuceneField;
import org.opencms.search.fields.CmsLuceneFieldConfiguration;
import org.opencms.search.fields.CmsSearchField;
import org.opencms.search.fields.CmsSearchFieldConfiguration;
import org.opencms.workplace.CmsWidgetDialog;
import org.opencms.workplace.CmsWorkplaceSettings;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.jsp.PageContext;

/**
 *
 * Abstract widget dialog for all dialogs working with <code>{@link CmsLuceneField}</code>.<p>
 *
 * @since 6.5.5
 */
public class A_CmsFieldDialog extends CmsWidgetDialog {

    /** localized messages Keys prefix. */
    public static final String KEY_PREFIX = "fieldconfiguration.field";

    /** Defines which pages are valid for this dialog. */
    public static final String[] PAGES = {"page1"};

    /**
     * The request parameter for the field to work with when contacting
     * this dialog from another. <p>
     *
     */
    public static final String PARAM_FIELD = "field";

    /**
     * The request parameter for the fieldconfiguration to work with when contacting
     * this dialog from another. <p>
     *
     */
    public static final String PARAM_FIELDCONFIGURATION = "fieldconfiguration";

    /** The user object that is edited on this dialog. */
    protected CmsLuceneField m_field;

    /** The user object that is edited on this dialog. */
    protected CmsSearchFieldConfiguration m_fieldconfiguration;

    /** The search manager singleton for convenient access. **/
    protected CmsSearchManager m_searchManager;

    /** Stores the value of the request parameter for the search index Name. */
    private String m_paramField;

    /** Stores the value of the request parameter for the search index Name. */
    private String m_paramFieldConfiguration;

    /**
     * Public constructor with JSP action element.<p>
     *
     * @param jsp an initialized JSP action element
     */
    public A_CmsFieldDialog(CmsJspActionElement jsp) {

        super(jsp);
    }

    /**
     * Public constructor with JSP variables.<p>
     *
     * @param context the JSP page context
     * @param req the JSP request
     * @param res the JSP response
     */
    public A_CmsFieldDialog(PageContext context, HttpServletRequest req, HttpServletResponse res) {

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
            boolean found = false;
            for (CmsSearchField field : m_fieldconfiguration.getFields()) {
                if (field.getName().equals(m_field.getName())) {
                    found = true;
                }
            }
            if (!found) {
                m_fieldconfiguration.addField(m_field);
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
     * Returns the request parameter value for parameter field. <p>
     *
     * @return the request parameter value for parameter field
     */
    public String getParamField() {

        return m_paramField;
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
     * Sets the request parameter value for parameter field. <p>
     *
     * @param field the request parameter value for parameter field
     */
    public void setParamField(String field) {

        m_paramField = field;
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

        if (m_field == null) {
            try {
                Iterator<CmsSearchField> itFields = m_fieldconfiguration.getFields().iterator();
                while (itFields.hasNext()) {
                    CmsLuceneField curField = (CmsLuceneField)itFields.next();
                    if (curField.getName().equals(getParamField())) {
                        m_field = curField;
                        break;
                    }
                }
                if (m_field == null) {
                    m_field = new CmsLuceneField();
                }
            } catch (Exception e) {
                m_field = new CmsLuceneField();
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
    @SuppressWarnings({"unchecked", "rawtypes"})
    @Override
    protected void initWorkplaceRequestValues(CmsWorkplaceSettings settings, HttpServletRequest request) {

        // initialize parameters and dialog actions in super implementation
        super.initWorkplaceRequestValues(settings, request);

        // save the current search index source
        Map dialogObject = (Map)getDialogObject();
        if (dialogObject == null) {
            dialogObject = new HashMap();
            dialogObject.put(PARAM_FIELDCONFIGURATION, m_fieldconfiguration);
            dialogObject.put(PARAM_FIELD, m_field);
            setDialogObject(dialogObject);
        }

    }

    /**
     * Checks if the new search index dialog has to be displayed.<p>
     *
     * @return <code>true</code> if the new search index dialog has to be displayed
     */
    protected boolean isNewField() {

        return DIALOG_INITIAL.equals(getParamAction());
    }

    /**
     * @see org.opencms.workplace.CmsWidgetDialog#validateParamaters()
     */
    @Override
    protected void validateParamaters() throws Exception {

        if (!isNewField()) {
            // test the needed parameters
            if ((getParamField() == null) && (getJsp().getRequest().getParameter("name.0") == null)) {
                throw new CmsIllegalStateException(
                    Messages.get().container(Messages.ERR_SEARCHINDEX_EDIT_MISSING_PARAM_1, PARAM_FIELD));
            }
        }
    }

    /**
     * Checks the configuration to write.<p>
     *
     * @return true if configuration is valid, otherwise false
     */
    private boolean checkWriteConfiguration() {

        if (!m_field.getMappings().isEmpty()) {
            return true;
        }
        return false;
    }
}
