/*
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (c) Alkacon Software GmbH & Co. KG (https://www.alkacon.com)
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
 * company website: https://www.alkacon.com
 *
 * For further information about OpenCms, please see the
 * project website: https://www.opencms.org
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package org.opencms.workplace.tools.accounts;

import org.opencms.i18n.CmsEncoder;
import org.opencms.jsp.CmsJspActionElement;
import org.opencms.main.CmsIllegalArgumentException;
import org.opencms.main.CmsRuntimeException;
import org.opencms.util.CmsRequestUtil;
import org.opencms.util.CmsStringUtil;
import org.opencms.util.CmsXsltUtil;
import org.opencms.widgets.CmsGroupWidget;
import org.opencms.widgets.CmsHttpUploadWidget;
import org.opencms.widgets.CmsInputWidget;
import org.opencms.widgets.CmsSelectWidget;
import org.opencms.workplace.CmsDialog;
import org.opencms.workplace.CmsWidgetDialogParameter;
import org.opencms.workplace.CmsWorkplaceSettings;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.jsp.PageContext;

import org.apache.commons.fileupload.FileItem;

/**
 * Dialog to import user data.<p>
 *
 * @since 6.5.6
 */
public class CmsUserDataImportDialog extends A_CmsUserDataImexportDialog {

    /** localized messages Keys prefix. */
    public static final String KEY_PREFIX = "userdata.import";

    /** The path to the file to import. */
    private String m_importFile;

    /** The password to use in the import. */
    private String m_password;

    /**
     * Public constructor with JSP action element.<p>
     *
     * @param jsp an initialized JSP action element
     */
    public CmsUserDataImportDialog(CmsJspActionElement jsp) {

        super(jsp);
    }

    /**
     * Public constructor with JSP variables.<p>
     *
     * @param context the JSP page context
     * @param req the JSP request
     * @param res the JSP response
     */
    public CmsUserDataImportDialog(PageContext context, HttpServletRequest req, HttpServletResponse res) {

        this(new CmsJspActionElement(context, req, res));
    }

    /**
     * @see org.opencms.workplace.tools.accounts.A_CmsUserDataImexportDialog#actionCommit()
     */
    @Override
    public void actionCommit() throws IOException, ServletException {

        List errors = new ArrayList();

        // get the file item from the multipart request
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
            byte[] content = fi.get();
            File importFile = File.createTempFile("import_users", ".csv");
            m_importFile = importFile.getAbsolutePath();

            FileOutputStream fileOutput = new FileOutputStream(importFile);
            fileOutput.write(content);
            fileOutput.close();
            fi.delete();

            FileReader fileReader = new FileReader(importFile);
            BufferedReader bufferedReader = new BufferedReader(fileReader);
            String line = bufferedReader.readLine();

            if (line != null) {
                List colDefs = CmsStringUtil.splitAsList(line, CmsXsltUtil.getPreferredDelimiter(line));
                if (!colDefs.contains("name")) {
                    errors.add(
                        new CmsRuntimeException(
                            Messages.get().container(Messages.ERR_USERDATA_IMPORT_CSV_MISSING_NAME_0)));
                }
                if ((line.indexOf("password") == -1) && CmsStringUtil.isEmptyOrWhitespaceOnly(m_password)) {
                    errors.add(
                        new CmsRuntimeException(
                            Messages.get().container(Messages.ERR_USERDATA_IMPORT_CSV_MISSING_PASSWORD_0)));
                }
            }
            bufferedReader.close();
        } else {
            errors.add(
                new CmsIllegalArgumentException(Messages.get().container(Messages.ERR_USERDATA_IMPORT_NO_CONTENT_0)));
        }

        if (errors.isEmpty()) {
            Map params = new HashMap();
            params.put("groups", CmsStringUtil.collectionAsString(getGroups(), ","));
            params.put("roles", CmsStringUtil.collectionAsString(getRoles(), ","));
            params.put("importfile", m_importFile);
            params.put("password", m_password);
            params.put(A_CmsOrgUnitDialog.PARAM_OUFQN, getParamOufqn());
            // set action parameter to initial dialog call
            params.put(CmsDialog.PARAM_ACTION, CmsDialog.DIALOG_INITIAL);

            getToolManager().jspForwardTool(this, getCurrentToolPath() + "/list", params);
        }
        // set the list of errors to display when something goes wrong
        setCommitErrors(errors);
    }

    /**
     * @see org.opencms.workplace.CmsWidgetDialog#dialogButtonsCustom()
     */
    @Override
    public String dialogButtonsCustom() {

        StringBuffer result = new StringBuffer(256);
        result.append(dialogButtonRow(HTML_START));
        result.append("<input name=\"ok\" value=\"");
        result.append(key(org.opencms.workplace.Messages.GUI_DIALOG_BUTTON_CONTINUE_0) + "\"");
        result.append(" type=\"submit\"");
        result.append(" class=\"dialogbutton\"");
        result.append(">\n");
        dialogButtonsHtml(result, BUTTON_CANCEL, "");
        result.append(dialogButtonRow(HTML_END));
        return result.toString();
    }

    /**
     * Returns the path of the file to import.<p>
     *
     * @return the path of the file to import
     */
    public String getImportFile() {

        return m_importFile;
    }

    /**
     * Returns the password to set during import.<p>
     *
     * @return the password to set during import
     */
    public String getPassword() {

        return m_password;
    }

    /**
     * Sets the path of the import file.<p>
     *
     * @param importFile the import file path
     */
    public void setImportFile(String importFile) {

        m_importFile = importFile;
    }

    /**
     * Sets the password to use during import.<p>
     *
     * @param password the password to use during import
     */
    public void setPassword(String password) {

        m_password = password;
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
            result.append(dialogBlockStart(key(Messages.GUI_USERDATA_IMPORT_LABEL_HINT_BLOCK_0)));
            result.append(key(Messages.GUI_USERDATA_IMPORT_LABEL_HINT_TEXT_0));
            result.append(dialogBlockEnd());
            result.append(dialogBlockStart(key(Messages.GUI_USERDATA_IMPORT_LABEL_DATA_BLOCK_0)));
            result.append(createWidgetTableStart());
            result.append(createDialogRowsHtml(0, 3));
            result.append(createWidgetTableEnd());
            result.append(dialogBlockEnd());
        }

        result.append(createWidgetTableEnd());
        return result.toString();
    }

    /**
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
     * @see org.opencms.workplace.tools.accounts.A_CmsUserDataImexportDialog#defineWidgets()
     */
    @Override
    protected void defineWidgets() {

        initImportObject();
        setKeyPrefix(KEY_PREFIX);

        addWidget(
            new CmsWidgetDialogParameter(this, "groups", PAGES[0], new CmsGroupWidget(null, null, getParamOufqn())));
        addWidget(new CmsWidgetDialogParameter(this, "roles", PAGES[0], new CmsSelectWidget(getSelectRoles())));
        addWidget(new CmsWidgetDialogParameter(this, "password", PAGES[0], new CmsInputWidget()));
        addWidget(new CmsWidgetDialogParameter(this, "importFile", PAGES[0], new CmsHttpUploadWidget()));
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
     * Initializes the message info object to work with depending on the dialog state and request parameters.<p>
     */
    protected void initImportObject() {

        try {
            if (CmsStringUtil.isEmpty(getParamAction()) || CmsDialog.DIALOG_INITIAL.equals(getParamAction())) {
                // create a new list
                setGroups(new ArrayList());
                setRoles(new ArrayList());
            } else {
                // this is not the initial call, get the message info object from session
                setGroups((List)((Map)getDialogObject()).get("groups"));
                setRoles((List)((Map)getDialogObject()).get("roles"));
                m_importFile = (String)((Map)getDialogObject()).get("importfile");
                m_password = (String)((Map)getDialogObject()).get("password");
            }
        } catch (Exception e) {
            // create a new list
            setGroups(new ArrayList());
            setRoles(new ArrayList());
        }
    }

    /**
     * @see org.opencms.workplace.CmsWorkplace#initWorkplaceRequestValues(org.opencms.workplace.CmsWorkplaceSettings, javax.servlet.http.HttpServletRequest)
     */
    @Override
    protected void initWorkplaceRequestValues(CmsWorkplaceSettings settings, HttpServletRequest request) {

        // initialize parameters and dialog actions in super implementation
        super.initWorkplaceRequestValues(settings, request);

        HashMap objectsMap = new HashMap();
        objectsMap.put("groups", getGroups());
        objectsMap.put("roles", getRoles());
        objectsMap.put("importfile", m_importFile);
        objectsMap.put("password", m_password);

        // save the current state of the message (may be changed because of the widget values)
        setDialogObject(objectsMap);
    }
}
