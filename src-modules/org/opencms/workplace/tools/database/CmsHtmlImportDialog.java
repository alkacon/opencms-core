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

package org.opencms.workplace.tools.database;

import org.opencms.configuration.CmsImportExportConfiguration;
import org.opencms.i18n.CmsEncoder;
import org.opencms.importexport.CmsExtendedHtmlImportDefault;
import org.opencms.jsp.CmsJspActionElement;
import org.opencms.main.CmsException;
import org.opencms.main.OpenCms;
import org.opencms.util.CmsRequestUtil;
import org.opencms.util.CmsStringUtil;
import org.opencms.widgets.CmsCheckboxWidget;
import org.opencms.widgets.CmsHttpUploadWidget;
import org.opencms.widgets.CmsInputWidget;
import org.opencms.widgets.CmsSelectWidget;
import org.opencms.widgets.CmsSelectWidgetOption;
import org.opencms.widgets.CmsVfsFileWidget;
import org.opencms.widgets.I_CmsWidget;
import org.opencms.workplace.CmsWidgetDialog;
import org.opencms.workplace.CmsWidgetDialogParameter;
import org.opencms.workplace.explorer.CmsNewResourceXmlPage;
import org.opencms.workplace.tools.CmsToolDialog;
import org.opencms.workplace.tools.CmsToolManager;

import java.io.File;
import java.io.FileOutputStream;
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

import org.apache.commons.fileupload.FileItem;

/**
 * Dialog to define an extended HTML import in the administration view.<p>
 *
 * WARNING: If the zip file is to great to upload, then only a log entry
 * is created from the following method and this dialog is only refreshed:<p>
 * {@link org.opencms.util.CmsRequestUtil#readMultipartFileItems(HttpServletRequest)}. <p>
 *
 * There are three modes to show the dialog:<p>
 *
 * <ul>
 *  <li>{@link #MODE_DEFAULT}
 *      <ul>
 *          <li>HTTP-Upload is not shown.</li>
 *          <li>default values are saved by action commit.</li>
 *      </ul>
 *  </li>
 *  <li>{@link #MODE_STANDARD}
 *      <ul>
 *          <li>HTTP-Upload is shown.</li>
 *          <li>the HTML files would be imported by action commit.</li>
 *      </ul>
 *  </li>
 *  <li>{@link #MODE_ADVANCED}
 *      <ul>
 *          <li>This dialog is needed for the advanced button in the new Dialog for the user.</li>
 *          <li>HTTP-Upload is shown.</li>
 *          <li>DestinationDir is not shown.</li>
 *          <li>InputDir is not shown.</li>
 *          <li>the HTML files would be imported by action commit.</li>
 *      </ul>
 *  </li>
 * </ul>
 *
 */
public class CmsHtmlImportDialog extends CmsWidgetDialog {

    /** the JSP path, which requested the default mode. */
    public static final String IMPORT_DEFAULT_PATH = "htmldefault.jsp";

    /** the JSP path, which requested the standard mode. */
    public static final String IMPORT_STANDARD_PATH = "htmlimport.jsp";

    /** localized messages Keys prefix. */
    public static final String KEY_PREFIX = "htmlimport";

    /** shows this dialog in the advanced mode.*/
    public static final String MODE_ADVANCED = "advanced";

    /** shows this dialog in the default mode.*/
    public static final String MODE_DEFAULT = "default";

    /** shows this dialog in the standard mode.*/
    public static final String MODE_STANDARD = "standard";

    /** Defines which pages are valid for this dialog. */
    public static final String[] PAGES = {"page1"};

    /** The import JSP report workplace URI. */
    protected static final String IMPORT_ACTION_REPORT = PATH_WORKPLACE + "admin/database/reports/htmlimport.jsp";

    /** The HTML import object that is edited on this dialog. */
    protected CmsHtmlImport m_htmlimport;

    /**the current mode of the dialog. */
    private String m_dialogMode;

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
    @Override
    public void actionCommit() {

        List errors = new ArrayList();
        setDialogObject(m_htmlimport);

        try {

            if (isDisplayMode(MODE_DEFAULT)) {
                // default mode the default values are saved in the configuration file

                m_htmlimport.validate(null, true);

                // fill the extended
                fillExtendedHtmlImportDefault();

                // save the default values in the file
                OpenCms.writeConfiguration(CmsImportExportConfiguration.class);

            } else {
                // advanced and standard mode the importing is starting
                FileItem fi = getHttpImportFileItem();

                m_htmlimport.validate(fi, false);

                // write the file in the temporary directory
                writeHttpImportDir(fi);

                Map params = new HashMap();

                // set the name of this class to get dialog object in report
                params.put(CmsHtmlImportReport.PARAM_CLASSNAME, this.getClass().getName());

                // set style to display report in correct layout
                params.put(PARAM_STYLE, CmsToolDialog.STYLE_NEW);

                // set close link to get back to overview after finishing the import
                params.put(PARAM_CLOSELINK, CmsToolManager.linkForToolPath(getJsp(), "/database"));

                // redirect to the report output JSP
                getToolManager().jspForwardPage(this, IMPORT_ACTION_REPORT, params);
            }
        } catch (Throwable t) {
            errors.add(t);
        }

        // set the list of errors to display when saving failed
        setCommitErrors(errors);
    }

    /**
     * @see org.opencms.workplace.CmsWidgetDialog#createDialogHtml(java.lang.String)
     */
    @Override
    protected String createDialogHtml(String dialog) {

        StringBuffer result = new StringBuffer(1024);

        result.append(createWidgetTableStart());
        // show error header once if there were validation errors
        result.append(createWidgetErrorHeader());

        if (dialog.equals(PAGES[0])) {

            // create the widgets for the first dialog page
            int row = (isDisplayMode(MODE_DEFAULT) ? 1 : (isDisplayMode(MODE_ADVANCED) ? 0 : 2));
            result.append(createWidgetBlockStart(key(Messages.GUI_HTMLIMPORT_BLOCK_LABEL_FOLDER_0)));
            result.append(createDialogRowsHtml(0, row));
            result.append(createWidgetBlockEnd());
            row++;

            result.append(createWidgetBlockStart(key(Messages.GUI_HTMLIMPORT_BLOCK_LABEL_GALLERY_0)));
            result.append(createDialogRowsHtml(row, row + 2));
            result.append(createWidgetBlockEnd());

            result.append(createWidgetBlockStart(key(Messages.GUI_HTMLIMPORT_BLOCK_LABEL_SETTINGS_0)));

            result.append(createDialogRowsHtml(row + 3, row + 10));
            result.append(createWidgetBlockEnd());
        }

        result.append(createWidgetTableEnd());
        return result.toString();
    }

    /**
     * This must be overwrite, because we need additional the 'enctype' parameter.<p>
     *
     * @see org.opencms.workplace.CmsWidgetDialog#defaultActionHtmlContent()
     */
    @Override
    protected String defaultActionHtmlContent() {

        StringBuffer result = new StringBuffer(2048);
        result.append("<form name=\"EDITOR\" id=\"EDITOR\" method=\"post\" action=\"").append(getDialogRealUri());
        result.append("\" class=\"nomargin\" onsubmit=\"return submitAction('").append(DIALOG_OK).append(
            "', null, 'EDITOR');\" enctype=\"multipart/form-data\">\n");
        result.append(dialogContentStart(getDialogTitle()));
        result.append(buildDialogForm());
        result.append(dialogContentEnd());
        result.append(dialogButtonsCustom());
        result.append(paramsAsHidden());
        if (getParamFramename() == null) {
            result.append("\n<input type=\"hidden\" name=\"").append(PARAM_FRAMENAME).append("\" value=\"\">\n");
        }
        result.append("</form>\n");
        result.append(getWidgetHtmlEnd());
        return result.toString();
    }

    /**
     * @see org.opencms.workplace.CmsWidgetDialog#defineWidgets()
     */
    @Override
    protected void defineWidgets() {

        initHtmlImportObject();
        setKeyPrefix(KEY_PREFIX);

        if (!isDisplayMode(MODE_ADVANCED)) {
            addWidget(getDialogParameter("inputDir", new CmsInputWidget()));
        }
        if (!isDisplayMode(MODE_DEFAULT)) {
            addWidget(getDialogParameter("httpDir", new CmsHttpUploadWidget()));
        }
        if (!isDisplayMode(MODE_ADVANCED)) {
            addWidget(getDialogParameter(
                "destinationDir",
                new CmsVfsFileWidget(false, getCms().getRequestContext().getSiteRoot())));
        }

        addWidget(
            getDialogParameter(
                "imageGallery",
                new CmsVfsFileWidget(false, getCms().getRequestContext().getSiteRoot())));
        addWidget(
            getDialogParameter(
                "downloadGallery",
                new CmsVfsFileWidget(false, getCms().getRequestContext().getSiteRoot())));
        addWidget(
            getDialogParameter("linkGallery", new CmsVfsFileWidget(false, getCms().getRequestContext().getSiteRoot())));

        addWidget(getDialogParameter("template", new CmsSelectWidget(getTemplates())));
        addWidget(getDialogParameter("element", new CmsInputWidget()));
        addWidget(getDialogParameter("locale", new CmsSelectWidget(getLocales())));
        addWidget(getDialogParameter("inputEncoding", new CmsInputWidget()));
        addWidget(getDialogParameter("startPattern", new CmsInputWidget()));
        addWidget(getDialogParameter("endPattern", new CmsInputWidget()));

        addWidget(getDialogParameter("overwrite", new CmsCheckboxWidget()));
        addWidget(getDialogParameter("keepBrokenLinks", new CmsCheckboxWidget()));
    }

    /**
     * This function fills the <code> {@link CmsHtmlImport} </code> Object based on
     * the values in the import/export configuration file. <p>
     */
    protected void fillHtmlImport() {

        CmsExtendedHtmlImportDefault extimport = OpenCms.getImportExportManager().getExtendedHtmlImportDefault();
        m_htmlimport.setDestinationDir(extimport.getDestinationDir());
        m_htmlimport.setInputDir(extimport.getInputDir());
        m_htmlimport.setDownloadGallery(extimport.getDownloadGallery());
        m_htmlimport.setImageGallery(extimport.getImageGallery());
        m_htmlimport.setLinkGallery(extimport.getLinkGallery());
        m_htmlimport.setTemplate(extimport.getTemplate());
        m_htmlimport.setElement(extimport.getElement());
        m_htmlimport.setLocale(extimport.getLocale());
        m_htmlimport.setInputEncoding(extimport.getEncoding());
        m_htmlimport.setStartPattern(extimport.getStartPattern());
        m_htmlimport.setEndPattern(extimport.getEndPattern());
        m_htmlimport.setOverwrite(Boolean.valueOf(extimport.getOverwrite()).booleanValue());
        m_htmlimport.setKeepBrokenLinks(Boolean.valueOf(extimport.getKeepBrokenLinks()).booleanValue());
    }

    /**
     * @see org.opencms.workplace.CmsWidgetDialog#fillWidgetValues(javax.servlet.http.HttpServletRequest)
     */
    @Override
    protected void fillWidgetValues(HttpServletRequest request) {

        Map parameters;
        if (getMultiPartFileItems() != null) {
            parameters = CmsRequestUtil.readParameterMapFromMultiPart(
                getCms().getRequestContext().getEncoding(),
                getMultiPartFileItems());
        } else {
            parameters = request.getParameterMap();
        }
        Map processedParameters = new HashMap();
        Iterator p = parameters.entrySet().iterator();
        // make sure all "hidden" widget parameters are decoded
        while (p.hasNext()) {
            Map.Entry entry = (Map.Entry)p.next();
            String key = (String)entry.getKey();
            String[] values = (String[])entry.getValue();
            if (key.startsWith(HIDDEN_PARAM_PREFIX)) {
                // this is an encoded hidden parameter
                key = key.substring(HIDDEN_PARAM_PREFIX.length());
                String[] newValues = new String[values.length];
                for (int l = 0; l < values.length; l++) {
                    newValues[l] = CmsEncoder.decode(values[l], getCms().getRequestContext().getEncoding());
                }
                values = newValues;
            }
            processedParameters.put(key, values);
        }

        // now process the parameters
        m_widgetParamValues = new HashMap();
        Iterator i = getWidgets().iterator();

        while (i.hasNext()) {
            // check for all widget base parameters
            CmsWidgetDialogParameter base = (CmsWidgetDialogParameter)i.next();

            List params = new ArrayList();
            int maxOccurs = base.getMaxOccurs();

            boolean onPage = false;
            if (base.isCollectionBase()) {
                // for a collection base, check if we are on the page where the collection base is shown
                if (CmsStringUtil.isNotEmpty(getParamAction()) && !DIALOG_INITIAL.equals(getParamAction())) {
                    // if no action set (usually for first display of dialog) make sure all values are shown
                    // DIALOG_INITIAL is a special value for the first display and must be handled the same way
                    String page = getParamPage();
                    // keep in mind that since the paramPage will be set AFTER the widget values are filled,
                    // so the first time this page is called from another page the following will result to "false",
                    // but for every "submit" on the page this will be "true"
                    onPage = CmsStringUtil.isEmpty(page)
                        || CmsStringUtil.isEmpty(base.getDialogPage())
                        || base.getDialogPage().equals(page);
                }
            }

            for (int j = 0; j < maxOccurs; j++) {
                // check for all possible values in the request parameters
                String id = CmsWidgetDialogParameter.createId(base.getName(), j);

                boolean required = (params.size() < base.getMinOccurs())
                    || (processedParameters.get(id) != null)
                    || (!onPage && base.hasValue(j));

                if (required) {
                    CmsWidgetDialogParameter param = new CmsWidgetDialogParameter(base, params.size(), j);
                    param.setKeyPrefix(KEY_PREFIX);
                    base.getWidget().setEditorValue(getCms(), processedParameters, this, param);
                    params.add(param);
                }
            }
            m_widgetParamValues.put(base.getName(), params);
        }
    }

    /**
     * This function creates a <code> {@link CmsWidgetDialogParameter} </code> Object based
     * on the given properties.<p>
     *
     * @param property the base object property to map the parameter to / from
     * @param widget the widget used for this dialog-parameter
     *
     * @return a <code> {@link CmsWidgetDialogParameter} </code> Object
     */
    protected CmsWidgetDialogParameter getDialogParameter(String property, I_CmsWidget widget) {

        return new CmsWidgetDialogParameter(m_htmlimport, property, PAGES[0], widget);
    }

    /**
     * @see org.opencms.workplace.CmsWidgetDialog#getPageArray()
     */
    @Override
    protected String[] getPageArray() {

        return PAGES;
    }

    /**
     * Initializes this widget dialog's object.<p>
     */
    protected void initHtmlImportObject() {

        Object o;
        String uri = getJsp().getRequestContext().getUri();
        if ((uri == null) || uri.endsWith(IMPORT_STANDARD_PATH)) {
            m_dialogMode = MODE_STANDARD;
        } else if (uri.endsWith(IMPORT_DEFAULT_PATH)) {
            m_dialogMode = MODE_DEFAULT;
        } else {
            m_dialogMode = MODE_ADVANCED;
        }
        if (CmsStringUtil.isEmpty(getParamAction())) {
            o = new CmsHtmlImport(getJsp().getCmsObject());
        } else {
            // this is not the initial call, get the job object from session
            o = getDialogObject();
        }

        if (!(o instanceof CmsHtmlImport)) {
            // create a new HTML import handler object
            m_htmlimport = new CmsHtmlImport(getJsp().getCmsObject());
        } else {
            // reuse HTML import handler object stored in session
            m_htmlimport = (CmsHtmlImport)o;
            // this is needed, because the user can switch between the sites, now get the current
            m_htmlimport.setCmsObject(getJsp().getCmsObject());
        }

        // gets the data from the configuration file
        fillHtmlImport();
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
     * This function fills the <code> {@link CmsExtendedHtmlImportDefault} </code> Object based on
     * the current values in the dialog. <p>
     */
    private void fillExtendedHtmlImportDefault() {

        CmsExtendedHtmlImportDefault extimport = OpenCms.getImportExportManager().getExtendedHtmlImportDefault();
        extimport.setDestinationDir(m_htmlimport.getDestinationDir());
        extimport.setInputDir(m_htmlimport.getInputDir());
        extimport.setDownloadGallery(m_htmlimport.getDownloadGallery());
        extimport.setImageGallery(m_htmlimport.getImageGallery());
        extimport.setLinkGallery(m_htmlimport.getLinkGallery());
        extimport.setTemplate(m_htmlimport.getTemplate());
        extimport.setElement(m_htmlimport.getElement());
        extimport.setLocale(m_htmlimport.getLocale());
        extimport.setEncoding(m_htmlimport.getInputEncoding());
        extimport.setStartPattern(m_htmlimport.getStartPattern());
        extimport.setEndPattern(m_htmlimport.getEndPattern());
        extimport.setOverwrite(Boolean.toString(m_htmlimport.isOverwrite()));
        extimport.setKeepBrokenLinks(Boolean.toString(m_htmlimport.isKeepBrokenLinks()));
        OpenCms.getImportExportManager().setExtendedHtmlImportDefault(extimport);
    }

    /**
     * Returns a list with all available local's.<p>
     *
     * @return a list with all available local's
     */
    private List getLocales() {

        ArrayList ret = new ArrayList();

        try {
            Iterator i = OpenCms.getLocaleManager().getAvailableLocales().iterator();

            // loop through all local's and build the entries
            while (i.hasNext()) {
                Locale locale = (Locale)i.next();
                String language = locale.getLanguage();
                String displayLanguage = locale.getDisplayLanguage();

                ret.add(new CmsSelectWidgetOption(language, false, displayLanguage));
            }
        } catch (Exception e) {
            // not necessary
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
            // not necessary
        }

        return ret;
    }

    /**
     * Checks if given mode is to show.<p>
     *
     * @param mode [ {@link #MODE_DEFAULT} | {@link #MODE_STANDARD} | {@link #MODE_ADVANCED} ]
     *
     * @return <code>true</code> if the given display mode is to shown
     */
    private boolean isDisplayMode(String mode) {

        return m_dialogMode.equals(mode);
    }

    /**
     * This function reads the file item and if its exits then the
     * file is saved in the temporary directory of the system.<p>
     *
     * @param fi the file item from the multipart-request
     *
     * @throws CmsException if something goes wrong.
     */
    private void writeHttpImportDir(FileItem fi) throws CmsException {

        try {

            if ((fi != null) && CmsStringUtil.isNotEmptyOrWhitespaceOnly(fi.getName())) {
                //write the file in the tmp-directory of the system
                byte[] content = fi.get();
                File importFile = File.createTempFile("import_html", ".zip");
                //write the content in the tmp file
                FileOutputStream fileOutput = new FileOutputStream(importFile.getAbsolutePath());
                fileOutput.write(content);
                fileOutput.close();
                fi.delete();
                m_htmlimport.setHttpDir(importFile.getAbsolutePath());
            }
        } catch (Exception e) {
            throw new CmsException(Messages.get().container(Messages.ERR_ACTION_ZIPFILE_UPLOAD_0));
        }
    }

    /**
     * Checks if a multipart-request file item exists and returns it.<p>
     *
     * @return <code>true</code> if a multipart-request file exists
     */
    private FileItem getHttpImportFileItem() {

        FileItem result = null;
        m_htmlimport.setHttpDir("");
        // get the file item from the multipart-request
        Iterator it = getMultiPartFileItems().iterator();
        FileItem fi = null;
        while (it.hasNext()) {
            fi = (FileItem)it.next();
            if (fi.getName() != null) {
                // found the file object, leave iteration
                break;
            } else {
                // this is no file object, check next item
                continue;
            }
        }

        if ((fi != null) && CmsStringUtil.isNotEmptyOrWhitespaceOnly(fi.getName())) {
            result = fi;
        }
        return result;
    }
}
