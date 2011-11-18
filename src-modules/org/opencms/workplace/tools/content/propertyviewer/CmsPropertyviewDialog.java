/*
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (c) Alkacon Software GmbH (http://www.alkacon.com)
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

package org.opencms.workplace.tools.content.propertyviewer;

import org.opencms.file.CmsObject;
import org.opencms.file.CmsPropertyDefinition;
import org.opencms.jsp.CmsJspActionElement;
import org.opencms.main.CmsException;
import org.opencms.main.CmsLog;
import org.opencms.widgets.CmsCheckboxWidget;
import org.opencms.widgets.CmsDisplayWidget;
import org.opencms.widgets.CmsSelectWidget;
import org.opencms.widgets.CmsVfsFileWidget;
import org.opencms.workplace.CmsWidgetDialog;
import org.opencms.workplace.CmsWidgetDialogParameter;
import org.opencms.workplace.CmsWorkplace;
import org.opencms.workplace.CmsWorkplaceSettings;
import org.opencms.workplace.tools.CmsToolDialog;
import org.opencms.workplace.tools.CmsToolManager;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.jsp.PageContext;

import org.apache.commons.logging.Log;

/**
 * Widget dialog that collects the options for the property view.
 * <p>
 * 
 * @since 7.5.1
 */
public class CmsPropertyviewDialog extends CmsWidgetDialog {

    /**
     * The settings bean for this dialog.
     * <p>
     * 
     */
    public class CmsPropertyviewDialogSettings {

        /** Display message. */
        private String m_message;

        /** The paths to collect resources. */
        private List<String> m_paths = new LinkedList<String>();

        /** The properties to show. */
        private List<String> m_properties = new LinkedList<String>();

        /** If true siblings will also be inspected. */
        private boolean m_showSiblings;

        /**
         * The default constructor.
         * <p>
         */
        public CmsPropertyviewDialogSettings() {

            super();
            m_paths.add("/");
            m_properties.add("Title");
        }

        /**
         * @return the message
         */
        public String getMessage() {

            return m_message;
        }

        /**
         * @return the paths
         */
        public List<String> getPaths() {

            return m_paths;
        }

        /**
         * @return the properties
         */
        public List<String> getProperties() {

            return m_properties;
        }

        /**
         * @return the showSiblings
         */
        public boolean isShowSiblings() {

            return m_showSiblings;
        }

        /**
         * @param message the message to set
         */
        public void setMessage(final String message) {

            m_message = message;
        }

        /**
         * @param paths the paths to set
         */
        public void setPaths(final List<String> paths) {

            m_paths = paths;
        }

        /**
         * @param properties the properties to set
         */
        public void setProperties(final List<String> properties) {

            m_properties = properties;
        }

        /**
         * @param showSiblings the showSiblings to set
         */
        public void setShowSiblings(final boolean showSiblings) {

            m_showSiblings = showSiblings;
        }

    }

    /** localized messages Keys prefix. */
    public static final String KEY_PREFIX = "propertyviewer";

    /** Defines which pages are valid for this dialog. */
    public static final String[] PAGES = {"page1"};

    /** The log object for this class. */
    private static final Log LOG = CmsLog.getLog(CmsPropertyviewDialog.class);

    /** The widget mapped data container. */
    private CmsPropertyviewDialogSettings m_settings = new CmsPropertyviewDialogSettings();

    /**
     * Public constructor with JSP action element.
     * <p>
     * @param jsp an initialized JSP action element
     */
    public CmsPropertyviewDialog(final CmsJspActionElement jsp) {

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
    public CmsPropertyviewDialog(final PageContext context, final HttpServletRequest req, final HttpServletResponse res) {

        this(new CmsJspActionElement(context, req, res));

    }

    /**
     * @see org.opencms.workplace.CmsWidgetDialog#actionCommit()
     */
    @Override
    public void actionCommit() throws IOException, ServletException {

        initDialogObject();
        List<Throwable> errors = new ArrayList<Throwable>();
        Map<String, String[]> params = new HashMap<String, String[]>();
        List<String> paths = m_settings.getPaths();
        params.put(CmsPropertyviewList.PARAM_RESOURCES, paths.toArray(new String[paths.size()]));
        List<String> props = m_settings.getProperties();
        params.put(CmsPropertyviewList.PARAM_PROPERTIES, props.toArray(new String[props.size()]));
        params.put(CmsPropertyviewList.PARAM_SIBLINGS, new String[] {String.valueOf(m_settings.isShowSiblings())});
        // set style to display report in correct layout
        params.put(PARAM_STYLE, new String[] {CmsToolDialog.STYLE_NEW});
        // set close link to get back to overview after finishing the import
        params.put(PARAM_CLOSELINK, new String[] {CmsToolManager.linkForToolPath(getJsp(), "propertyviewer")});

        // redirect to the report output JSP
        getToolManager().jspForwardPage(
            this,
            CmsWorkplace.PATH_WORKPLACE + "admin/contenttools/propertyviewer-list.jsp",
            params);

        // set the list of errors to display when saving failed
        setCommitErrors(errors);
    }

    /**
     * @see org.opencms.workplace.CmsWidgetDialog#createDialogHtml(java.lang.String)
     */
    @Override
    protected String createDialogHtml(final String dialog) {

        StringBuffer result = new StringBuffer(1024);

        // create table
        result.append(createWidgetTableStart());

        // show error header once if there were validation errors
        result.append(createWidgetErrorHeader());

        // create export file name block
        result.append(createWidgetBlockStart(key(Messages.GUI_PROPERTYVIEW_ADMIN_TOOL_BLOCK_0)));
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

        setKeyPrefix(KEY_PREFIX);

        addWidget(new CmsWidgetDialogParameter(
            m_settings,
            "message",
            key(Messages.GUI_MODULES_DETAIL_DIALOG_MESSAGE_0),
            PAGES[0],
            new CmsDisplayWidget(),
            1,
            1));
        addWidget(new CmsWidgetDialogParameter(m_settings, "paths", "/", PAGES[0], new CmsVfsFileWidget(
            false,
            getCms().getRequestContext().getSiteRoot()), 1, CmsWidgetDialogParameter.MAX_OCCURENCES));
        addWidget(new CmsWidgetDialogParameter(m_settings, "properties", "/", PAGES[0], new CmsSelectWidget(
            getPropertySelectWidgetConfiguration()), 1, CmsWidgetDialogParameter.MAX_OCCURENCES));
        addWidget(new CmsWidgetDialogParameter(
            m_settings,
            "showSiblings",
            String.valueOf(false),
            PAGES[0],
            new CmsCheckboxWidget(),
            1,
            1));
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
     * @see org.opencms.workplace.CmsWorkplace#initWorkplaceRequestValues(org.opencms.workplace.CmsWorkplaceSettings,
     *      javax.servlet.http.HttpServletRequest)
     */
    @Override
    protected void initWorkplaceRequestValues(final CmsWorkplaceSettings settings, final HttpServletRequest request) {

        initDialogObject();
        // initialize parameters and dialog actions in super implementation
        super.initWorkplaceRequestValues(settings, request);
    }

    /**
     * Reads all available properties in the system and returns a config string for a select widget.
     * <p>
     * 
     * @return a select widget configuration String for available properties in the system.
     */
    private String getPropertySelectWidgetConfiguration() {

        String result = "";
        CmsObject cms = getCms();
        try {
            List<CmsPropertyDefinition> props = cms.readAllPropertyDefinitions();
            StringBuffer buffer = new StringBuffer();
            Iterator<CmsPropertyDefinition> it = props.iterator();
            CmsPropertyDefinition prop;
            while (it.hasNext()) {
                prop = it.next();
                buffer.append(prop.getName());
                if (it.hasNext()) {
                    buffer.append('|');
                }
            }
            result = buffer.toString();

        } catch (CmsException e) {
            if (LOG.isErrorEnabled()) {
                LOG.error(Messages.get().getBundle().key(Messages.LOG_ERR_PROPERTYVIEWER_READALLPROPS_0), e);
            }
        }
        return result;
    }

    /**
     * Initializes the dialog object.
     * <p>
     */
    private void initDialogObject() {

        Object o = getDialogObject();
        if (o != null) {
            m_settings = (CmsPropertyviewDialogSettings)o;
        } else {
            m_settings = new CmsPropertyviewDialogSettings();
            setDialogObject(m_settings);
        }
    }
}
