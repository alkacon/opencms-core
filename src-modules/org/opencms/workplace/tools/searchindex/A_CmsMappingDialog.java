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
import org.opencms.search.fields.CmsSearchFieldMapping;
import org.opencms.search.fields.I_CmsSearchFieldMapping;
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
 * Abstract widget dialog for all dialogs working with <code>{@link CmsSearchFieldMapping}</code>.<p>
 *
 * @since 6.5.5
 */
public class A_CmsMappingDialog extends CmsWidgetDialog {

    /** localized messages Keys prefix. */
    public static final String KEY_PREFIX = "fieldconfiguration.field.mapping";

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

    /**
     * The request parameter for the mapping type to work with when contacting
     * this dialog from another. <p>
     *
     */
    public static final String PARAM_PARAM = "param";

    /**
     * The request parameter for the mapping type to work with when contacting
     * this dialog from another. <p>
     *
     */
    public static final String PARAM_TYPE = "type";

    /** The user object that is edited on this dialog. */
    protected CmsSearchField m_field;

    /** The user object that is edited on this dialog. */
    protected CmsSearchFieldConfiguration m_fieldconfiguration;

    /** The user object that is edited on this dialog. */
    protected I_CmsSearchFieldMapping m_mapping;

    /** The search manager singleton for convenient access. **/
    protected CmsSearchManager m_searchManager;

    /** Stores the value of the request parameter for the search index Name. */
    private String m_paramField;

    /** Stores the value of the request parameter for the search index Name. */
    private String m_paramFieldConfiguration;

    /** Stores the value of the request parameter for the mapping param. */
    private String m_paramParam;

    /** Stores the value of the request parameter for the mapping type. */
    private String m_paramType;

    /**
     * Public constructor with JSP action element.<p>
     *
     * @param jsp an initialized JSP action element
     */
    public A_CmsMappingDialog(CmsJspActionElement jsp) {

        super(jsp);
    }

    /**
     * Public constructor with JSP variables.<p>
     *
     * @param context the JSP page context
     * @param req the JSP request
     * @param res the JSP response
     */
    public A_CmsMappingDialog(PageContext context, HttpServletRequest req, HttpServletResponse res) {

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
            Iterator<I_CmsSearchFieldMapping> itMappings = m_field.getMappings().iterator();
            while (itMappings.hasNext()) {
                I_CmsSearchFieldMapping curMapping = itMappings.next();
                if (curMapping.getType().toString().equals(m_mapping.getType().toString())
                    && (((curMapping.getParam() == null) && (m_mapping.getParam() == null))
                        || (curMapping.getParam().equals(m_mapping.getParam())))) {
                    found = true;
                }
            }
            if (!found) {
                m_field.addMapping(m_mapping);
            }
            writeConfiguration();

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
     * Returns the request parameter value for parameter mapping param. <p>
     *
     * @return the request parameter value for parameter mapping param
     */
    public String getParamParam() {

        return m_paramParam;
    }

    /**
     * Returns the request parameter value for parameter mapping type. <p>
     *
     * @return the request parameter value for parameter mapping type
     */
    public String getParamType() {

        return m_paramType;
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
     * Sets the request parameter value for parameter mapping param. <p>
     *
     * @param param the request parameter value for parameter mapping param
     */
    public void setParamParam(String param) {

        m_paramParam = param;
    }

    /**
     * Sets the request parameter value for parameter mapping type. <p>
     *
     * @param type the request parameter value for parameter mapping type
     */
    public void setParamType(String type) {

        m_paramType = type;
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
                    CmsSearchField curField = itFields.next();
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
        if (m_mapping == null) {
            try {
                Iterator<I_CmsSearchFieldMapping> itMappings = m_field.getMappings().iterator();
                while (itMappings.hasNext()) {
                    I_CmsSearchFieldMapping curMapping = itMappings.next();
                    if (curMapping.getType().toString().equals(getParamType())
                        && (((curMapping.getParam() == null) && getParamParam().equals("-"))
                            || (curMapping.getParam().equals(getParamParam())))) {
                        m_mapping = curMapping;
                        break;
                    }
                }
                if (m_mapping == null) {
                    m_mapping = new CmsSearchFieldMapping(true);
                }
            } catch (Exception e) {
                m_mapping = new CmsSearchFieldMapping(true);
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
            dialogObject.put(PARAM_PARAM, m_mapping.getParam());
            if (m_mapping.getType() != null) {
                dialogObject.put(PARAM_TYPE, m_mapping.getType().toString());
            } else {
                dialogObject.put(PARAM_TYPE, m_mapping.getType());
            }
            setDialogObject(dialogObject);
        }

    }

    /**
     * Checks if the new search index dialog has to be displayed.<p>
     *
     * @return <code>true</code> if the new search index dialog has to be displayed
     */
    protected boolean isNewMapping() {

        return DIALOG_INITIAL.equals(getParamAction());
    }

    /**
     * @see org.opencms.workplace.CmsWidgetDialog#validateParamaters()
     */
    @Override
    protected void validateParamaters() throws Exception {

        if (!isNewMapping()) {
            // test the needed parameters
            if ((getParamField() == null) && (getJsp().getRequest().getParameter("name.0") == null)) {
                throw new CmsIllegalStateException(
                    Messages.get().container(Messages.ERR_SEARCHINDEX_EDIT_MISSING_PARAM_1, PARAM_FIELD));
            }
        }
    }
}
