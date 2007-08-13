/*
 * File   : $Source: /alkacon/cvs/opencms/src-modules/org/opencms/workplace/tools/database/CmsHtmlImportDialog.java,v $
 * Date   : $Date: 2007/08/13 16:30:15 $
 * Version: $Revision: 1.3 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (c) 2002 - 2007 Alkacon Software GmbH (http://www.alkacon.com)
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

package org.opencms.workplace.tools.database;

import org.opencms.jsp.CmsJspActionElement;
import org.opencms.main.CmsException;
import org.opencms.main.OpenCms;
import org.opencms.util.CmsStringUtil;
import org.opencms.widgets.CmsCheckboxWidget;
import org.opencms.widgets.CmsInputWidget;
import org.opencms.widgets.CmsSelectWidget;
import org.opencms.widgets.CmsSelectWidgetOption;
import org.opencms.widgets.CmsVfsFileWidget;
import org.opencms.workplace.CmsWidgetDialog;
import org.opencms.workplace.CmsWidgetDialogParameter;
import org.opencms.workplace.explorer.CmsNewResourceXmlPage;
import org.opencms.workplace.tools.CmsToolDialog;
import org.opencms.workplace.tools.CmsToolManager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.jsp.PageContext;

/**
 * Dialog to define an extended html import in the administration view.<p>
 * 
 * @author Peter Bonrad
 * 
 * @version $Revision: 1.3 $ 
 * 
 */
public class CmsHtmlImportDialog extends CmsWidgetDialog {

    /** localized messages Keys prefix. */
    public static final String KEY_PREFIX = "htmlimport";

    /** Defines which pages are valid for this dialog. */
    public static final String[] PAGES = {"page1"};

    /** The import JSP report workplace URI. */
    protected static final String IMPORT_ACTION_REPORT = PATH_WORKPLACE + "admin/database/reports/htmlimport.jsp";

    /** The html import object that is edited on this dialog. */
    protected CmsHtmlImport m_htmlimport;

    /**
     * Public constructor with JSP action element.<p>
     * 
     * @param jsp an initialized JSP action element
     */
    public CmsHtmlImportDialog(CmsJspActionElement jsp) {

        super(jsp);
    }

    /**
     * Public constructor with JSP variables.<p>
     * 
     * @param context the JSP page context
     * @param req the JSP request
     * @param res the JSP response
     */
    public CmsHtmlImportDialog(PageContext context, HttpServletRequest req, HttpServletResponse res) {

        this(new CmsJspActionElement(context, req, res));
    }

    /**
     * @see org.opencms.workplace.CmsWidgetDialog#actionCommit()
     */
    public void actionCommit() {

        List errors = new ArrayList();
        setDialogObject(m_htmlimport);

        try {

            m_htmlimport.validate();

            Map params = new HashMap();

            // set the name of this class to get dialog object in report
            params.put(CmsHtmlImportReport.PARAM_CLASSNAME, this.getClass().getName());

            // set style to display report in correct layout
            params.put(PARAM_STYLE, CmsToolDialog.STYLE_NEW);

            // set close link to get back to overview after finishing the import
            params.put(PARAM_CLOSELINK, CmsToolManager.linkForToolPath(getJsp(), "/database"));

            // redirect to the report output JSP
            getToolManager().jspForwardPage(this, IMPORT_ACTION_REPORT, params);

        } catch (Throwable t) {
            errors.add(t);
        }

        // set the list of errors to display when saving failed
        setCommitErrors(errors);
    }

    /**
     * @see org.opencms.workplace.CmsWidgetDialog#createDialogHtml(java.lang.String)
     */
    protected String createDialogHtml(String dialog) {

        StringBuffer result = new StringBuffer(1024);

        result.append(createWidgetTableStart());
        // show error header once if there were validation errors
        result.append(createWidgetErrorHeader());

        if (dialog.equals(PAGES[0])) {

            // create the widgets for the first dialog page
            result.append(createWidgetBlockStart(key(Messages.GUI_HTMLIMPORT_BLOCK_LABEL_FOLDER_0)));
            result.append(createDialogRowsHtml(0, 1));
            result.append(createWidgetBlockEnd());

            result.append(createWidgetBlockStart(key(Messages.GUI_HTMLIMPORT_BLOCK_LABEL_GALLERY_0)));
            result.append(createDialogRowsHtml(2, 7));
            result.append(createWidgetBlockEnd());

            result.append(createWidgetBlockStart(key(Messages.GUI_HTMLIMPORT_BLOCK_LABEL_SETTINGS_0)));
            result.append(createDialogRowsHtml(8, 15));
            result.append(createWidgetBlockEnd());
        }

        result.append(createWidgetTableEnd());
        return result.toString();
    }

    /**
     * @see org.opencms.workplace.CmsWidgetDialog#defineWidgets()
     */
    protected void defineWidgets() {

        initHtmlImportObject();
        setKeyPrefix(KEY_PREFIX);

        addWidget(new CmsWidgetDialogParameter(m_htmlimport, "inputDir", PAGES[0], new CmsInputWidget()));
        addWidget(new CmsWidgetDialogParameter(m_htmlimport, "destinationDir", PAGES[0], new CmsVfsFileWidget(
            false,
            getCms().getRequestContext().getSiteRoot())));
        addWidget(new CmsWidgetDialogParameter(m_htmlimport, "leaveImages", PAGES[0], new CmsCheckboxWidget()));
        addWidget(new CmsWidgetDialogParameter(m_htmlimport, "imageGallery", PAGES[0], new CmsVfsFileWidget(
            false,
            getCms().getRequestContext().getSiteRoot())));
        addWidget(new CmsWidgetDialogParameter(m_htmlimport, "leaveDownloads", PAGES[0], new CmsCheckboxWidget()));
        addWidget(new CmsWidgetDialogParameter(m_htmlimport, "downloadGallery", PAGES[0], new CmsVfsFileWidget(
            false,
            getCms().getRequestContext().getSiteRoot())));
        addWidget(new CmsWidgetDialogParameter(m_htmlimport, "leaveExternalLinks", PAGES[0], new CmsCheckboxWidget()));
        addWidget(new CmsWidgetDialogParameter(m_htmlimport, "linkGallery", PAGES[0], new CmsVfsFileWidget(
            false,
            getCms().getRequestContext().getSiteRoot())));
        addWidget(new CmsWidgetDialogParameter(m_htmlimport, "template", PAGES[0], new CmsSelectWidget(getTemplates())));
        addWidget(new CmsWidgetDialogParameter(m_htmlimport, "element", "body", PAGES[0], new CmsInputWidget(), 1, 1));
        addWidget(new CmsWidgetDialogParameter(m_htmlimport, "locale", PAGES[0], new CmsSelectWidget(getLocales())));
        addWidget(new CmsWidgetDialogParameter(
            m_htmlimport,
            "inputEncoding",
            "ISO-8859-1",
            PAGES[0],
            new CmsInputWidget(),
            1,
            1));
        addWidget(new CmsWidgetDialogParameter(m_htmlimport, "startPattern", PAGES[0], new CmsInputWidget()));
        addWidget(new CmsWidgetDialogParameter(m_htmlimport, "endPattern", PAGES[0], new CmsInputWidget()));
        addWidget(new CmsWidgetDialogParameter(m_htmlimport, "overwrite", PAGES[0], new CmsCheckboxWidget()));
        addWidget(new CmsWidgetDialogParameter(m_htmlimport, "keepBrokenLinks", PAGES[0], new CmsCheckboxWidget()));
    }

    /**
     * @see org.opencms.workplace.CmsWidgetDialog#getPageArray()
     */
    protected String[] getPageArray() {

        return PAGES;
    }

    /**
     * Initializes this widget dialog's object.<p>
     */
    protected void initHtmlImportObject() {

        Object o;

        if (CmsStringUtil.isEmpty(getParamAction())) {
            o = new CmsHtmlImport(getJsp().getCmsObject());
        } else {
            // this is not the initial call, get the job object from session
            o = getDialogObject();
        }

        if (!(o instanceof CmsHtmlImport)) {
            // create a new html import handler object
            m_htmlimport = new CmsHtmlImport(getJsp().getCmsObject());
        } else {
            // reuse html import handler object stored in session
            m_htmlimport = (CmsHtmlImport)o;
        }
    }

    /**
     * @see org.opencms.workplace.CmsWorkplace#initMessages()
     */
    protected void initMessages() {

        // add specific dialog resource bundle
        addMessages(Messages.get().getBundleName());
        // add default resource bundles
        super.initMessages();
    }

    /**
     * Returns a list with all available locales.<p>
     * 
     * @return a list with all available locales
     */
    private List getLocales() {

        ArrayList ret = new ArrayList();

        try {
            Iterator i = OpenCms.getLocaleManager().getAvailableLocales().iterator();

            // loop through all locales and build the entries
            while (i.hasNext()) {
                Locale locale = (Locale)i.next();
                String language = locale.getLanguage();
                String displayLanguage = locale.getDisplayLanguage();

                ret.add(new CmsSelectWidgetOption(language, false, displayLanguage));
            }
        } catch (Exception e) {
            // TODO
        }
        return ret;
    }

    /**
     * Returns a list with all available templates.<p>
     * 
     * @return a list with all available templates
     */
    private List getTemplates() {

        ArrayList ret = new ArrayList();
        TreeMap templates = null;

        try {
            templates = CmsNewResourceXmlPage.getTemplates(getJsp().getCmsObject(), null);

            // loop through all templates and build the entries
            Iterator i = templates.entrySet().iterator();
            while (i.hasNext()) {
                Map.Entry entry = (Map.Entry)i.next();
                String title = (String)entry.getKey();
                String path = (String)entry.getValue();

                ret.add(new CmsSelectWidgetOption(path, false, title));
            }
        } catch (CmsException e) {
            // TODO
        }

        return ret;
    }
}
