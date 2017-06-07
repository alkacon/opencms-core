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

package org.opencms.workplace.tools.modules;

import org.opencms.file.types.I_CmsResourceType;
import org.opencms.jsp.CmsJspActionElement;
import org.opencms.main.CmsException;
import org.opencms.main.CmsLog;
import org.opencms.main.OpenCms;
import org.opencms.widgets.CmsInputWidget;
import org.opencms.workplace.CmsDialog;
import org.opencms.workplace.CmsWidgetDialog;
import org.opencms.workplace.CmsWidgetDialogParameter;
import org.opencms.workplace.CmsWorkplaceSettings;
import org.opencms.workplace.tools.CmsToolDialog;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.jsp.PageContext;

import org.apache.commons.logging.Log;

/**
 * A work place administration dialog to add a xml content based resource type to a module.<p>
 */
public class CmsModuleAddResourceTypes extends CmsWidgetDialog {

    /** The request attribute name used to store the new resource type information. */
    public static final String ATTR_RESOURCE_TYPE_INFO = "resourceTypeInfo";

    /** The message key prefix. */
    public static final String KEY_PREFIX = "addtype";

    /** The add type report JSP. */
    private static final String ADD_TYPE_REPORT = "/system/workplace/admin/modules/add_type_report.jsp";

    /** The log object for this class. */
    private static final Log LOG = CmsLog.getLog(CmsModuleAddResourceTypes.class);

    /** The dialog pages. */
    private static final String[] PAGES = {"page0"};

    /** Module name. */
    protected String m_paramModule;

    /** The collected resource type information. */
    private CmsResourceTypeInfoBean m_resInfo;

    /**
     * Constructor.<p>
     *
     * @param jsp an initialized JSP action element
     */
    public CmsModuleAddResourceTypes(CmsJspActionElement jsp) {

        super(jsp);

    }

    /**
     * Constructor.<p>
     *
     * @param context the JSP page context
     * @param req the JSP request
     * @param res the JSP response
     */
    public CmsModuleAddResourceTypes(PageContext context, HttpServletRequest req, HttpServletResponse res) {

        this(new CmsJspActionElement(context, req, res));
    }

    /**
     * @see org.opencms.workplace.CmsWidgetDialog#actionCommit()
     */
    @Override
    public void actionCommit() {

        try {
            // validate the resource type info
            if (OpenCms.getResourceManager().hasResourceType(m_resInfo.getName())) {
                throw new CmsException(
                    Messages.get().container(Messages.ERR_RESOURCE_TYPE_ALREADY_EXISTS_1, m_resInfo.getName()));
            }
            if (OpenCms.getResourceManager().hasResourceType(m_resInfo.getId())) {
                throw new CmsException(
                    Messages.get().container(
                        Messages.ERR_RESOURCE_TYPE_ID_ALREADY_IN_USE_1,
                        String.valueOf(m_resInfo.getId())));
            }

            getJsp().getRequest().setAttribute(ATTR_RESOURCE_TYPE_INFO, m_resInfo);
            Map<String, String[]> params = new HashMap<String, String[]>();
            params.put(PARAM_CLOSELINK, new String[] {getParamCloseLink()});
            params.put(PARAM_STYLE, new String[] {CmsToolDialog.STYLE_NEW});
            getToolManager().jspForwardPage(this, ADD_TYPE_REPORT, params);
        } catch (Exception e) {
            LOG.error(e.getLocalizedMessage(), e);
            setCommitErrors(Collections.<Throwable> singletonList(e));
        }

    }

    /**
     * Gets the module parameter.<p>
     *
     * @return the module parameter
     */
    public String getParamModule() {

        return m_paramModule;
    }

    /**
     * Sets the module parameter.<p>
     * @param paramModule the module parameter
     */
    public void setParamModule(String paramModule) {

        m_paramModule = paramModule;
    }

    /**
     * @see org.opencms.workplace.CmsWidgetDialog#createDialogHtml(java.lang.String)
     */
    @Override
    protected String createDialogHtml(String page) {

        StringBuffer result = new StringBuffer(1024);
        if (PAGES[0].equals(page)) {

            // create widget table
            result.append(createWidgetTableStart());

            // show error header once if there were validation errors
            result.append(createWidgetErrorHeader());

            result.append(createWidgetBlockStart(getMessages().key(Messages.GUI_ADD_TYPES_LABEL_0)));
            result.append(createDialogRowsHtml(0, 5));
            result.append(createWidgetBlockEnd());

            // close widget table
            result.append(createWidgetTableEnd());
        } else {
            result.append(page);

        }

        return result.toString();
    }

    /**
     * @see org.opencms.workplace.CmsWidgetDialog#defineWidgets()
     */
    @Override
    protected void defineWidgets() {

        initResInfo();
        setKeyPrefix(KEY_PREFIX);
        addWidget(new CmsWidgetDialogParameter(m_resInfo, "name", new CmsInputWidget()));
        addWidget(new CmsWidgetDialogParameter(m_resInfo, "id", new CmsInputWidget()));
        addWidget(new CmsWidgetDialogParameter(m_resInfo, "niceName", new CmsInputWidget()));
        addWidget(new CmsWidgetDialogParameter(m_resInfo, "title", new CmsInputWidget()));
        addWidget(new CmsWidgetDialogParameter(m_resInfo, "description", new CmsInputWidget()));
        addWidget(new CmsWidgetDialogParameter(m_resInfo, "schemaTypeName", new CmsInputWidget()));
    }

    /**
     * @see org.opencms.workplace.CmsWidgetDialog#getPageArray()
     */
    @Override
    protected String[] getPageArray() {

        return PAGES;
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
     * @see org.opencms.workplace.CmsWorkplace#initWorkplaceRequestValues(org.opencms.workplace.CmsWorkplaceSettings, javax.servlet.http.HttpServletRequest)
     */
    @Override
    protected void initWorkplaceRequestValues(CmsWorkplaceSettings settings, HttpServletRequest request) {

        // initialize parameters and dialog actions in super implementation
        super.initWorkplaceRequestValues(settings, request);

        // save the current state of the job (may be changed because of the widget values)
        setDialogObject(m_resInfo);
    }

    /**
     * @see org.opencms.workplace.CmsWidgetDialog#validateParamaters()
     */
    @Override
    protected void validateParamaters() throws Exception {

        // nothing to do
    }

    /**
     * Returns the next available resource type id.<p>
     *
     * @return the resource type id
     */
    private int findNextTypeId() {

        int result = (int)(20000 + Math.round(Math.random() * 1000));
        for (I_CmsResourceType type : OpenCms.getResourceManager().getResourceTypes()) {
            if (type.getTypeId() >= result) {
                result = type.getTypeId() + 1;
            }
        }
        return result;
    }

    /**
     * Initializes the resource type info bean.<p>
     */
    private void initResInfo() {

        Object dialogObject = getDialogObject();
        if ((dialogObject == null) || CmsDialog.DIALOG_INITIAL.equals(getParamAction())) {
            m_resInfo = new CmsResourceTypeInfoBean();
            int id = findNextTypeId();
            m_resInfo.setId(id);
            m_resInfo.setModuleName(getParamModule());
        } else {
            m_resInfo = (CmsResourceTypeInfoBean)dialogObject;
        }
    }

}
