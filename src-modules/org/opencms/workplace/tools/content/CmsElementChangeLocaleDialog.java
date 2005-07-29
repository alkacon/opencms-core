/*
 * File   : $Source: /alkacon/cvs/opencms/src-modules/org/opencms/workplace/tools/content/CmsElementChangeLocaleDialog.java,v $
 * Date   : $Date: 2005/07/29 15:38:42 $
 * Version: $Revision: 1.1 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Mananagement System
 *
 * Copyright (c) 2005 Alkacon Software GmbH (http://www.alkacon.com)
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

package org.opencms.workplace.tools.content;

import org.opencms.importexport.CmsVfsImportExportHandler;
import org.opencms.jsp.CmsJspActionElement;
import org.opencms.main.CmsException;
import org.opencms.main.CmsIllegalArgumentException;
import org.opencms.main.CmsLog;
import org.opencms.main.OpenCms;
import org.opencms.util.CmsStringUtil;
import org.opencms.widgets.CmsCheckboxWidget;
import org.opencms.widgets.CmsSelectWidget;
import org.opencms.widgets.CmsSelectWidgetOption;
import org.opencms.widgets.CmsVfsFileWidget;
import org.opencms.workplace.CmsWidgetDialog;
import org.opencms.workplace.CmsWidgetDialogParameter;
import org.opencms.workplace.CmsWorkplaceSettings;
import org.opencms.workplace.explorer.CmsNewResourceXmlPage;
import org.opencms.workplace.tools.CmsToolManager;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.jsp.PageContext;

import org.apache.commons.logging.Log;

/**
 * Widget dialog that sets the settings to move page elements to another Locale.<p>
 * 
 * @author  Andreas Zahner 
 * 
 * @version $Revision: 1.1 $ 
 * 
 * @since 6.0.1 
 */
public class CmsElementChangeLocaleDialog extends CmsWidgetDialog {
    
    /** Localized message keys prefix. */
    public static final String KEY_PREFIX = "changelocale";

    /** Defines which pages are valid for this dialog. */
    public static final String[] PAGES = {"page1"};

    /** The import JSP report workplace URI. */
    protected static final String CHANGELOCALE_ACTION_REPORT = PATH_WORKPLACE + "admin/contenttools/reports/changelocale.html";
    
    /** The log object for this class. */
    private static final Log LOG = CmsLog.getLog(CmsElementChangeLocaleDialog.class);

    /** The settings object that is edited on this dialog. */
    private CmsElementChangeLocaleSettings m_settings;

    /**
     * Public constructor with JSP action element.<p>
     * 
     * @param jsp an initialized JSP action element
     */
    public CmsElementChangeLocaleDialog(CmsJspActionElement jsp) {

        super(jsp);
    }

    /**
     * Public constructor with JSP variables.<p>
     * 
     * @param context the JSP page context
     * @param req the JSP request
     * @param res the JSP response
     */
    public CmsElementChangeLocaleDialog(PageContext context, HttpServletRequest req, HttpServletResponse res) {

        this(new CmsJspActionElement(context, req, res));
    }

    /**
     * @see org.opencms.workplace.CmsWidgetDialog#actionCommit()
     */
    public void actionCommit() throws IOException, ServletException {

        List errors = new ArrayList();
        setDialogObject(m_settings);
        
        try {
            
            if (m_settings.getOldLocale().equals(m_settings.getNewLocale())) {
                // old Locale is equals to new one, show error
                throw new CmsIllegalArgumentException(Messages.get().container(Messages.ERR_CHANGEELEMENTLOCALE_LOCALE_EQUAL_0));
            }

            Map params = new HashMap();
            // set the name of this class to get dialog object in report
            params.put(CmsElementChangeLocaleReport.PARAM_CLASSNAME, this.getClass().getName());
            // set style to display report in correct layout
            params.put(PARAM_STYLE, "new");
            // set close link to get back to overview after finishing the import
            params.put(PARAM_CLOSELINK, CmsToolManager.linkForToolPath(getJsp(), "/contenttools"));
            // redirect to the report output JSP
            getToolManager().jspForwardPage(this, CHANGELOCALE_ACTION_REPORT, params);
        
        } catch (CmsIllegalArgumentException e) {
            errors.add(e);    
        }
        // set the list of errors to display when saving failed
        setCommitErrors(errors);
    }
    
    /**
     * Returns the selector widget options to build a Locale selector widget.<p>
     * 
     * @return the selector widget options to build a Locale selector widget
     */
    public List getLocaleConfigOptions() {
        
        List result = new ArrayList();
        
        List locales = OpenCms.getLocaleManager().getAvailableLocales();
        Iterator i = locales.iterator();
        while (i.hasNext()) {
            Locale locale = (Locale)i.next();
            String language = locale.toString();
            String displayLanguage = locale.getDisplayLanguage(getLocale());
            
            result.add(new CmsSelectWidgetOption(language, false, displayLanguage));
        }

        return result;
    }
    
    /**
     * returns the selector widget options to build a template selector widget.<p>
     * 
     * @return the selector widget options to build a template selector widget
     */
    public List getTemplateConfigOptions() {

        List result = new ArrayList();
        result.add(new CmsSelectWidgetOption("", true, key(Messages.GUI_CHANGEELEMENTLOCALE_DIALOG_TEMPLATE_ALL_0)));
        
        TreeMap templates = null;
        try {
            // get all available templates
            templates = CmsNewResourceXmlPage.getTemplates(getCms());
        } catch (CmsException e) {
            // can usually be ignored
            if (LOG.isInfoEnabled()) {
                LOG.info(e);
            }
        }
        if (templates != null) {
            // templates found, create option and value lists
            Iterator i = templates.keySet().iterator();
            while (i.hasNext()) {
                String key = (String)i.next();
                String path = (String)templates.get(key);
                result.add(new CmsSelectWidgetOption(path, false, key));
            }
        }
        return result;
    }

    /**
     * @see org.opencms.workplace.CmsWidgetDialog#createDialogHtml(java.lang.String)
     */
    protected String createDialogHtml(String dialog) {

        StringBuffer result = new StringBuffer(1024);

        // create table
        result.append(createWidgetTableStart());

        // show error header once if there were validation errors
        result.append(createWidgetErrorHeader());

        // create export file name block
        result.append(createWidgetBlockStart(Messages.get().key(Messages.GUI_CHANGEELEMENTLOCALE_DIALOG_BLOCK_SETTINGS_0)));
        result.append(createDialogRowsHtml(0, 4));
        result.append(createWidgetBlockEnd());

        // close table
        result.append(createWidgetTableEnd());

        return result.toString();
    }

    /**
     * @see org.opencms.workplace.CmsWidgetDialog#defineWidgets()
     */
    protected void defineWidgets() {

        // initialize the export object to use for the dialog
        initSettingsObject();
        
        // set localized key prefix
        setKeyPrefix(KEY_PREFIX);

        addWidget(new CmsWidgetDialogParameter(m_settings, "vfsFolder", "/", PAGES[0], new CmsVfsFileWidget(
            false,
            getCms().getRequestContext().getSiteRoot()), 1, 1));

        addWidget(new CmsWidgetDialogParameter(m_settings, "includeSubFolders", PAGES[0], new CmsCheckboxWidget()));
        addWidget(new CmsWidgetDialogParameter(m_settings, "template", PAGES[0], new CmsSelectWidget(getTemplateConfigOptions())));
        
        List localeSelections = getLocaleConfigOptions();
        addWidget(new CmsWidgetDialogParameter(m_settings, "oldLocale", PAGES[0], new CmsSelectWidget(localeSelections)));
        addWidget(new CmsWidgetDialogParameter(m_settings, "newLocale", PAGES[0], new CmsSelectWidget(localeSelections)));
    }

    /**
     * @see org.opencms.workplace.CmsWidgetDialog#getPageArray()
     */
    protected String[] getPageArray() {

        return PAGES;
    }

    /**
     * @see org.opencms.workplace.CmsWorkplace#initMessages()
     */
    protected void initMessages() {

        // add specific dialog resource bundle
        addMessages(Messages.get().getBundleName());
        // add workplace messages
        addMessages("org.opencms.workplace.workplace");
        // add default resource bundles
        super.initMessages();
    }

    /**
     * Initializes the settings object to work with depending on the dialog state and request parameters.<p>
     */
    protected void initSettingsObject() {

        Object o;

        if (CmsStringUtil.isEmpty(getParamAction())) {
            o = new CmsVfsImportExportHandler();
        } else {
            // this is not the initial call, get the job object from session
            o = getDialogObject();
        }

        if (!(o instanceof CmsElementChangeLocaleSettings)) {
            // create a new export handler object
            m_settings = new CmsElementChangeLocaleSettings();
        } else {
            // reuse export handler object stored in session
            m_settings = (CmsElementChangeLocaleSettings)o;
        }

    }

    /**
     * @see org.opencms.workplace.CmsWorkplace#initWorkplaceRequestValues(org.opencms.workplace.CmsWorkplaceSettings, javax.servlet.http.HttpServletRequest)
     */
    protected void initWorkplaceRequestValues(CmsWorkplaceSettings settings, HttpServletRequest request) {

        // initialize parameters and dialog actions in super implementation
        super.initWorkplaceRequestValues(settings, request);

        // save the current state of the export handler (may be changed because of the widget values)
        setDialogObject(m_settings);
    }
}