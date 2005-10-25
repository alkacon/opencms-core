/*
 * File   : $Source: /alkacon/cvs/opencms/src-modules/org/opencms/workplace/tools/content/check/CmsContentCheckDialog.java,v $
 * Date   : $Date: 2005/10/25 15:14:32 $
 * Version: $Revision: 1.1.2.2 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Mananagement System
 *
 * Copyright (C) 2005 Alkacon Software GmbH (http://www.alkacon.com)
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

package org.opencms.workplace.tools.content.check;

import org.opencms.jsp.CmsJspActionElement;
import org.opencms.main.CmsIllegalArgumentException;
import org.opencms.util.CmsStringUtil;
import org.opencms.widgets.CmsCheckboxWidget;
import org.opencms.widgets.CmsVfsFileWidget;
import org.opencms.workplace.CmsDialog;
import org.opencms.workplace.CmsWidgetDialog;
import org.opencms.workplace.CmsWidgetDialogParameter;
import org.opencms.workplace.CmsWorkplaceSettings;
import org.opencms.workplace.tools.CmsToolDialog;
import org.opencms.workplace.tools.database.CmsDatabaseExportReport;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.jsp.PageContext;

/**
 * Dialog for selecting the content checks.<p> 
 *
 * @author  Michael Emmerich
 * 
 * @version $Revision: 1.1.2.2 $ 
 * 
 * @since 6.1.2 
 */
public class CmsContentCheckDialog extends CmsWidgetDialog {

    /** The dialog type. */
    public static final String DIALOG_TYPE = "contentcheck";

    /** Defines which pages are valid for this dialog. */
    public static final String[] PAGES = {"page1"};

    /** The content check JSP report workplace URI. */
    protected static final String CONTENT_CHECK_REPORT = PATH_WORKPLACE + "admin/contenttools/check/report.html";

    /** The content check JSP result workplace URI. */
    protected static final String CONTENT_CHECK_RESULT = PATH_WORKPLACE + "admin/contenttools/check/result-fs.html";

    /** The Content Check object. */
    private CmsContentCheck m_contentCheck;

    /**
     * Public constructor with JSP action element.<p>
     * 
     * @param jsp an initialized JSP action element
     */
    public CmsContentCheckDialog(CmsJspActionElement jsp) {

        super(jsp);

    }

    /**
     * Public constructor with JSP variables.<p>
     * 
     * @param context the JSP page context
     * @param req the JSP request
     * @param res the JSP response
     */
    public CmsContentCheckDialog(PageContext context, HttpServletRequest req, HttpServletResponse res) {

        this(new CmsJspActionElement(context, req, res));
    }

    /**
     * @see org.opencms.workplace.CmsWidgetDialog#actionCommit()
     */
    public void actionCommit() {

        List errors = new ArrayList();
        try {
            // check if there are any vfs paths entered
            List paths = m_contentCheck.getPaths();
            if (paths.size() == 0) {
                throw new CmsIllegalArgumentException(Messages.get().container(Messages.ERR_NO_VFSPATH_0));
            } else {
                Iterator i = paths.iterator();
                while (i.hasNext()) {
                    String path = (String)i.next();
                    if (!getCms().existsResource(path)) {
                        throw new CmsIllegalArgumentException(Messages.get().container(Messages.ERR_NO_VFSPATH_0));
                    }
                }
            }

            // check if there is at least one test activated
            boolean isActive = false;
            List plugins = m_contentCheck.getPlugins();
            Iterator i = plugins.iterator();
            while (i.hasNext()) {
                I_CmsContentCheck plugin = (I_CmsContentCheck)i.next();
                if (plugin.isActive()) {
                    isActive = true;
                }
            }
            if (!isActive) {
                throw new CmsIllegalArgumentException(Messages.get().container(Messages.ERR_NO_TEST_0));
            }

            // if there was no error, store the content check object in the session and forward it to the
            // check thread
            setDialogObject(m_contentCheck);
            Map params = new HashMap();
            // set the name of this class to get dialog object in report
            params.put(CmsDatabaseExportReport.PARAM_CLASSNAME, this.getClass().getName());
            // set style to display report in correct layout
            params.put(PARAM_STYLE, CmsToolDialog.STYLE_NEW);
            // set close link to get back to overview after finishing the import
            params.put(PARAM_CLOSELINK, getJsp().link(CONTENT_CHECK_RESULT));
            //params.put(PARAM_CLOSELINK, CmsToolManager.linkForToolPath(getJsp(), "/contenttools"));
            getToolManager().jspForwardPage(this, CONTENT_CHECK_REPORT, params);

        } catch (Throwable t) {
            errors.add(t);
        }
        // set the list of errors to display when saving failed
        setCommitErrors(errors);
    }

    /**
     * Creates the dialog HTML for all defined widgets of the named dialog (page).<p>  
     * 
     * @param dialog the dialog (page) to get the HTML for
     * @return the dialog HTML for all defined widgets of the named dialog (page)
     */
    protected String createDialogHtml(String dialog) {

        StringBuffer result = new StringBuffer(1024);

        // create table
        result.append(createWidgetTableStart());

        // show error header once if there were validation errors
        result.append(createWidgetErrorHeader());

        if (dialog.equals(PAGES[0])) {
            result.append(dialogBlockStart(key("label.vfsresources")));
            result.append(createWidgetTableStart());
            result.append(createDialogRowsHtml(0, 0));
            result.append(createWidgetTableEnd());
            result.append(dialogBlockEnd());
            result.append(dialogBlockStart(key("label.contentcheck")));
            result.append(createWidgetTableStart());
            result.append(createDialogRowsHtml(1, m_contentCheck.getPluginsCount()));
            result.append(createWidgetTableEnd());
            result.append(dialogBlockEnd());
        }

        // close table
        result.append(createWidgetTableEnd());

        return result.toString();
    }

    /**
     * @see org.opencms.workplace.CmsWidgetDialog#defineWidgets()
     */
    protected void defineWidgets() {

        initContentCheck();
        addWidget(new CmsWidgetDialogParameter(m_contentCheck, "paths", PAGES[0], new CmsVfsFileWidget()));
        // get a list of all plugins and build a widget for each plugin
        List plugins = m_contentCheck.getPlugins();
        for (int i = 0; i < plugins.size(); i++) {
            I_CmsContentCheck plugin = (I_CmsContentCheck)plugins.get(i);
            addWidget(new CmsWidgetDialogParameter(
                plugin,
                I_CmsContentCheck.PARAMETER,
                plugin.getDialogParameterName(),
                PAGES[0],
                new CmsCheckboxWidget()));
        }

    }

    /**
     * @see org.opencms.workplace.CmsWidgetDialog#getPageArray()
     */
    protected String[] getPageArray() {

        return new String[] {"page1"};
    }

    /**
     * Initializes the content check object or takes an exiting one which is stored in the sesstion.<p>
     */
    protected void initContentCheck() {

        Object o;
        if (CmsStringUtil.isEmpty(getParamAction()) || CmsDialog.DIALOG_INITIAL.equals(getParamAction())) {
            // this is the initial dialog call
            o = null;
        } else {
            // this is not the initial call, get module from session
            o = getDialogObject();
        }

        if (!(o instanceof CmsContentCheck)) {
            // create a new content check object
            m_contentCheck = new CmsContentCheck(getCms());

        } else {
            // reuse content check object stored in session
            m_contentCheck = (CmsContentCheck)o;
        }
    }

    /**
     * @see org.opencms.workplace.CmsWorkplace#initMessages()
     */
    protected void initMessages() {

        // add specific dialog resource bundle
        addMessages(org.opencms.workplace.tools.content.Messages.get().getBundleName());
        addMessages("org.opencms.workplace.workplace");
        addMessages(Messages.get().getBundleName());

        // now add the additional message bundles for each plugin
        CmsContentCheck dummy = new CmsContentCheck(getCms());
        List plugins = dummy.getPlugins();
        Iterator i = plugins.iterator();
        while (i.hasNext()) {
            I_CmsContentCheck plugin = (I_CmsContentCheck)i.next();
            List bundles = plugin.getMessageBundles();
            Iterator j = bundles.iterator();
            while (j.hasNext()) {
                String bundle = (String)j.next();
                addMessages(bundle);
            }
        }

        // add default resource bundles
        super.initMessages();
    }

    /**
     * @see org.opencms.workplace.CmsWorkplace#initWorkplaceRequestValues(org.opencms.workplace.CmsWorkplaceSettings, javax.servlet.http.HttpServletRequest)
     */
    protected void initWorkplaceRequestValues(CmsWorkplaceSettings settings, HttpServletRequest request) {

        // set the dialog type
        setParamDialogtype(DIALOG_TYPE);

        super.initWorkplaceRequestValues(settings, request);

        // save the current state of the content check object (may be changed because of the widget values)
        setDialogObject(m_contentCheck);
    }

}
