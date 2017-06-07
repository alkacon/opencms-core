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

package org.opencms.workplace.tools.content;

import org.opencms.file.types.CmsResourceTypeXmlContent;
import org.opencms.file.types.I_CmsResourceType;
import org.opencms.jsp.CmsJspActionElement;
import org.opencms.main.CmsIllegalArgumentException;
import org.opencms.main.OpenCms;
import org.opencms.util.CmsStringUtil;
import org.opencms.widgets.CmsCheckboxWidget;
import org.opencms.widgets.CmsSelectWidget;
import org.opencms.widgets.CmsSelectWidgetOption;
import org.opencms.widgets.CmsVfsFileWidget;
import org.opencms.workplace.CmsWidgetDialog;
import org.opencms.workplace.CmsWidgetDialogParameter;
import org.opencms.workplace.CmsWorkplaceSettings;
import org.opencms.workplace.threads.CmsXmlContentRepairSettings;
import org.opencms.workplace.tools.CmsToolDialog;
import org.opencms.workplace.tools.CmsToolManager;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.jsp.PageContext;

/**
 * Widget dialog that sets the settings to repair XML contents where the XSD was changed below a given VFS folder.<p>
 *
 * @since 6.2.0
 */
public class CmsXmlContentRepairDialog extends CmsWidgetDialog {

    /** Localized message keys prefix. */
    public static final String KEY_PREFIX = "xmlcontentrepair";

    /** Defines which pages are valid for this dialog. */
    public static final String[] PAGES = {"page1"};

    /** The name of the generic xmlcontent resource type. */
    protected static final String TYPE_XMLCONTENT = "xmlcontent";

    /** The repair JSP report workplace URI. */
    protected static final String XMLCONTENTREPAIR_ACTION_REPORT = PATH_WORKPLACE
        + "admin/contenttools/reports/xmlcontentrepair.jsp";

    /** The settings object that is edited on this dialog. */
    private CmsXmlContentRepairSettings m_settings;

    /**
     * Public constructor with JSP action element.
     * <p>
     *
     * @param jsp an initialized JSP action element
     */
    public CmsXmlContentRepairDialog(CmsJspActionElement jsp) {

        super(jsp);
    }

    /**
     * Public constructor with JSP variables.
     * <p>
     *
     * @param context the JSP page context
     * @param req the JSP request
     * @param res the JSP response
     */
    public CmsXmlContentRepairDialog(PageContext context, HttpServletRequest req, HttpServletResponse res) {

        this(new CmsJspActionElement(context, req, res));
    }

    /**
     * @see org.opencms.workplace.CmsWidgetDialog#actionCommit()
     */
    @Override
    public void actionCommit() throws IOException, ServletException {

        List errors = new ArrayList();
        setDialogObject(m_settings);

        try {
            Map params = new HashMap();
            // set style to display report in correct layout
            params.put(PARAM_STYLE, CmsToolDialog.STYLE_NEW);
            // set close link to get back to overview after finishing the import
            params.put(PARAM_CLOSELINK, CmsToolManager.linkForToolPath(getJsp(), "/contenttools"));
            // redirect to the report output JSP
            getToolManager().jspForwardPage(this, XMLCONTENTREPAIR_ACTION_REPORT, params);
        } catch (CmsIllegalArgumentException e) {
            errors.add(e);
        }
        // set the list of errors to display when saving failed
        setCommitErrors(errors);
    }

    /**
     * @see org.opencms.workplace.CmsWidgetDialog#createDialogHtml(java.lang.String)
     */
    @Override
    protected String createDialogHtml(String dialog) {

        StringBuffer result = new StringBuffer(2048);

        // create table
        result.append(createWidgetTableStart());

        // show error header once if there were validation errors
        result.append(createWidgetErrorHeader());

        // create export file name block
        result.append(createWidgetBlockStart(
            Messages.get().getBundle(getLocale()).key(Messages.GUI_XMLCONTENTREPAIR_DIALOG_BLOCK_SETTINGS_0)));
        result.append(createDialogRowsHtml(0, 3));
        result.append(createWidgetBlockEnd());

        // close table
        result.append(createWidgetTableEnd());

        return result.toString();
    }

    /**
     * @see org.opencms.workplace.CmsWidgetDialog#defineWidgets()
     */
    @Override
    protected void defineWidgets() {

        // initialize the settings object to use for the dialog
        initSettingsObject();

        // set localized key prefix
        setKeyPrefix(KEY_PREFIX);
        // add the widgets to show
        addWidget(new CmsWidgetDialogParameter(
            m_settings,
            "vfsFolder",
            "/",
            PAGES[0],
            new CmsVfsFileWidget(false, getCms().getRequestContext().getSiteRoot()),
            1,
            1));

        addWidget(new CmsWidgetDialogParameter(m_settings, "includeSubFolders", PAGES[0], new CmsCheckboxWidget("")));

        addWidget(new CmsWidgetDialogParameter(m_settings, "force", PAGES[0], new CmsCheckboxWidget("")));

        addWidget(
            new CmsWidgetDialogParameter(
                m_settings,
                "resourceType",
                PAGES[0],
                new CmsSelectWidget(getXmlContentResourceTypes())));
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
     * Initializes the settings object to work with depending on the dialog state and request parameters.<p>
     */
    protected void initSettingsObject() {

        Object o;
        if (CmsStringUtil.isEmpty(getParamAction())) {
            o = new CmsXmlContentRepairSettings(getCms());
        } else {
            // this is not the initial call, get the settings object from session
            o = getDialogObject();
        }

        if (o == null) {
            // create a new settings object
            m_settings = new CmsXmlContentRepairSettings(getCms());
        } else {
            // reuse settings object stored in session
            m_settings = (CmsXmlContentRepairSettings)o;
        }

    }

    /**
     * @see org.opencms.workplace.CmsWorkplace#initWorkplaceRequestValues(org.opencms.workplace.CmsWorkplaceSettings,
     *      javax.servlet.http.HttpServletRequest)
     */
    @Override
    protected void initWorkplaceRequestValues(CmsWorkplaceSettings settings, HttpServletRequest request) {

        // initialize parameters and dialog actions in super implementation
        super.initWorkplaceRequestValues(settings, request);

        // save the current state of the settings (may be changed because of the widget values)
        setDialogObject(m_settings);
    }

    /**
     * Returns the selector widget options to build a XML content resource type selector widget.<p>
     *
     * @return the selector widget options to build a XML content resource type selector widget
     */
    private List getXmlContentResourceTypes() {

        // get all available resource types and filter XML content resource types
        List resTypes = OpenCms.getResourceManager().getResourceTypes();
        Iterator i = resTypes.iterator();
        List resTypeNames = new ArrayList(resTypes.size());
        while (i.hasNext()) {
            I_CmsResourceType resType = (I_CmsResourceType)i.next();
            if (!(resType instanceof CmsResourceTypeXmlContent)) {
                // this is not XML content resource type, skip it
                continue;
            }
            if (!resType.getTypeName().equals(TYPE_XMLCONTENT)) {
                resTypeNames.add(resType.getTypeName());
            }
        }

        // create the selector options
        List result = new ArrayList(resTypeNames.size() + 2);
        // add empty "please select" option to selector
        result.add(new CmsSelectWidgetOption("", true, key(Messages.GUI_XMLCONTENTREPAIR_DIALOG_RESTYPE_SELECT_0)));

        // sort the resource type names alphabetically
        Collections.sort(resTypeNames);
        i = resTypeNames.iterator();
        while (i.hasNext()) {
            // add all resource type names to the selector
            String resTypeName = (String)i.next();
            result.add(new CmsSelectWidgetOption(resTypeName));
        }

        // add option for generic XML content without "own" resource types at the end
        result.add(
            new CmsSelectWidgetOption(
                TYPE_XMLCONTENT,
                false,
                key(Messages.GUI_XMLCONTENTREPAIR_DIALOG_RESTYPE_GENERIC_0)));

        return result;
    }
}