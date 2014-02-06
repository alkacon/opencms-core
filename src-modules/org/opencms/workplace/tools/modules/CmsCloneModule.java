/*
 * File   : $Source$
 * Date   : $Date$
 * Version: $Revision$
 *
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (C) 2002 - 2009 Alkacon Software (http://www.alkacon.com)
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

import org.opencms.main.CmsException;
import org.opencms.main.CmsLog;
import org.opencms.main.OpenCms;
import org.opencms.util.CmsStringUtil;
import org.opencms.widgets.CmsCheckboxWidget;
import org.opencms.widgets.CmsInputWidget;
import org.opencms.workplace.CmsDialog;
import org.opencms.workplace.CmsWidgetDialog;
import org.opencms.workplace.CmsWidgetDialogParameter;
import org.opencms.workplace.tools.CmsToolDialog;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.jsp.PageContext;

import org.apache.commons.logging.Log;

/**
 * Clones a module.<p>
 */
public class CmsCloneModule extends CmsWidgetDialog {

    /** The clone module information bean. */
    public static final String ATTR_CLONE_MODULE_INFO = "cloneModuleInfo";

    /** The message key prefix. */
    public static final String KEY_PREFIX = "modules";

    /** Path to the clone module report JSP. */
    private static final String CLONE_MODULE_REPORT = "/system/workplace/admin/modules/module-clone-report.jsp";

    /** The log object for this class. */
    private static final Log LOG = CmsLog.getLog(CmsCloneModule.class);

    /** The dialog pages. */
    private static final String[] PAGES = {"page0"};

    /** Module name. */
    protected String m_paramModule;

    /** The clone module information. */
    private CmsCloneModuleInfo m_cloneInfo;

    /**
     * Constructor, with parameters.<p>
     * 
     * @param context the JSP page context object
     * @param req the JSP request 
     * @param res the JSP response 
     */
    public CmsCloneModule(PageContext context, HttpServletRequest req, HttpServletResponse res) {

        super(context, req, res);
    }

    /**
     * @see org.opencms.workplace.CmsWidgetDialog#actionCommit()
     */
    @Override
    public void actionCommit() {

        try {
            if (CmsStringUtil.isEmptyOrWhitespaceOnly(m_cloneInfo.getName())) {
                throw new CmsException(Messages.get().container(Messages.ERR_CLONEMODULE_EMPTY_PACKAGE_NAME_0));
            }
            if (CmsStringUtil.isEmptyOrWhitespaceOnly(m_cloneInfo.getNiceName())) {
                throw new CmsException(Messages.get().container(Messages.ERR_CLONEMODULE_EMPTY_MODULE_NAME_0));
            }
            if (OpenCms.getModuleManager().hasModule(m_cloneInfo.getName())) {
                throw new CmsException(Messages.get().container(
                    Messages.ERR_CLONEMODULE_MODULE_ALREADY_EXISTS_1,
                    m_cloneInfo.getName()));
            }
            getJsp().getRequest().setAttribute(ATTR_CLONE_MODULE_INFO, m_cloneInfo);
            Map<String, String[]> params = new HashMap<String, String[]>();
            params.put(PARAM_CLOSELINK, new String[] {getParamCloseLink()});
            params.put(PARAM_STYLE, new String[] {CmsToolDialog.STYLE_NEW});
            getToolManager().jspForwardPage(this, CLONE_MODULE_REPORT, params);
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

            result.append(createWidgetBlockStart(getMessages().key(Messages.GUI_CLONEMODULE_MODULE_INFORMATION_0)));
            result.append(createDialogRowsHtml(0, 4));
            result.append(createWidgetBlockEnd());
            result.append(createWidgetBlockStart(getMessages().key(Messages.GUI_CLONEMODULE_AUTHOR_INFORMATION_0)));
            result.append(createDialogRowsHtml(5, 6));
            result.append(createWidgetBlockEnd());
            result.append(createWidgetBlockStart(getMessages().key(Messages.GUI_CLONEMODULE_TRANSLATION_OPTIONS_0)));
            result.append(createDialogRowsHtml(7, 13));
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

        initCloneInfo();
        setKeyPrefix(KEY_PREFIX);
        // new module info
        addWidget(new CmsWidgetDialogParameter(m_cloneInfo, "name", new CmsInputWidget()));
        addWidget(new CmsWidgetDialogParameter(m_cloneInfo, "niceName", new CmsInputWidget()));
        addWidget(new CmsWidgetDialogParameter(m_cloneInfo, "description", new CmsInputWidget()));
        addWidget(new CmsWidgetDialogParameter(m_cloneInfo, "group", new CmsInputWidget()));
        addWidget(new CmsWidgetDialogParameter(m_cloneInfo, "actionClass", new CmsInputWidget()));
        // author info
        addWidget(new CmsWidgetDialogParameter(m_cloneInfo, "authorName", new CmsInputWidget()));
        addWidget(new CmsWidgetDialogParameter(m_cloneInfo, "authorEmail", new CmsInputWidget()));
        // translation options
        addWidget(new CmsWidgetDialogParameter(m_cloneInfo, "sourceNamePrefix", new CmsInputWidget()));
        addWidget(new CmsWidgetDialogParameter(m_cloneInfo, "targetNamePrefix", new CmsInputWidget()));
        addWidget(new CmsWidgetDialogParameter(m_cloneInfo, "formatterSourceModule", new CmsInputWidget()));
        addWidget(new CmsWidgetDialogParameter(m_cloneInfo, "formatterTargetModule", new CmsInputWidget()));
        addWidget(new CmsWidgetDialogParameter(m_cloneInfo, "changeResourceTypes", new CmsCheckboxWidget()));
        addWidget(new CmsWidgetDialogParameter(m_cloneInfo, "rewriteContainerPages", new CmsCheckboxWidget()));
        addWidget(new CmsWidgetDialogParameter(m_cloneInfo, "applyChangesEverywhere", new CmsCheckboxWidget()));
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
     * Initializes the resource type info bean.<p>
     */
    private void initCloneInfo() {

        Object dialogObject = getDialogObject();
        if ((dialogObject == null) || CmsDialog.DIALOG_INITIAL.equals(getParamAction())) {
            m_cloneInfo = new CmsCloneModuleInfo();
            m_cloneInfo.setSourceModuleName(getParamModule());
        } else {
            m_cloneInfo = (CmsCloneModuleInfo)dialogObject;
        }
    }
}
