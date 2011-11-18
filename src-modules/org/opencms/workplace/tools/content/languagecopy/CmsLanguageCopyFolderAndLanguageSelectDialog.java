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

package org.opencms.workplace.tools.content.languagecopy;

import org.opencms.jsp.CmsJspActionElement;
import org.opencms.main.OpenCms;
import org.opencms.util.CmsStringUtil;
import org.opencms.widgets.CmsDisplayWidget;
import org.opencms.widgets.CmsRadioSelectWidget;
import org.opencms.widgets.CmsSelectWidgetOption;
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
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.jsp.PageContext;

/**
 * Widget dialog that collects the folders and the languages for XML content language node copy operation.
 * <p>
 * 
 * @since 7.5.1
 */
public class CmsLanguageCopyFolderAndLanguageSelectDialog extends CmsWidgetDialog {

    /**
     * Settings bean for the dialog.
     * <p>
     * 
     */
    public class CmsLanguageCopyFolderAndLanguageSelectDialogSettings {

        /** Display message. */
        private String m_message;

        /** The paths to collect resources. */
        private List<String> m_paths = new LinkedList<String>();

        /** The list of resource paths to process: all should be files. */
        private String[] m_resources;

        /** The source language. */
        private String m_sourcelanguage;

        /** The source language. */
        private String m_targetlanguage;

        /**
         * Constructor from the inner class.
         * <p>
         */
        public CmsLanguageCopyFolderAndLanguageSelectDialogSettings() {

            super();
            m_paths.add("/");
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
         * @return the resources
         */
        public String getResources() {

            return CmsStringUtil.arrayAsString(m_resources, ",");
        }

        /**
         * Returns the resources paths in an array.<p>
         * 
         * @return the resources paths in an array.
         */
        public String[] getResourcesArray() {

            return m_resources;
        }

        /**
         * @return the sourceLanguage
         */
        public String getSourcelanguage() {

            return m_sourcelanguage;
        }

        /**
         * @return the targetLanguage
         */
        public String getTargetlanguage() {

            return m_targetlanguage;
        }

        /**
         * @param message
         *            the message to set
         */
        public void setMessage(final String message) {

            // nop, this is hardcoded... just has to be here for "bean - convention".
        }

        /**
         * @param paths
         *            the paths to set
         */
        public void setPaths(final List<String> paths) {

            m_paths = paths;
        }

        /**
         * @param resources
         *            the resources to set
         */
        public void setResources(final String resources) {

            m_resources = CmsStringUtil.splitAsArray(resources, ",");

        }

        /**
         * @param sourceLanguage
         *            the sourceLanguage to set
         */
        public void setSourcelanguage(final String sourceLanguage) {

            m_sourcelanguage = sourceLanguage;
        }

        /**
         * @param targetLanguage
         *            the targetLanguage to set
         */
        public void setTargetlanguage(final String targetLanguage) {

            m_targetlanguage = targetLanguage;
        }
    }

    /** localized messages Keys prefix. */
    public static final String KEY_PREFIX = "languagecopy";

    /** Defines which pages are valid for this dialog. */
    public static final String[] PAGES = {"page1"};

    /** The request parameter for the resources to process from the previous dialog. */
    public static final String PARAM_COPYRESOURCES = "copyresources";

    /** The widget mapped data container. */
    private CmsLanguageCopyFolderAndLanguageSelectDialogSettings m_dialogSettings = new CmsLanguageCopyFolderAndLanguageSelectDialogSettings();

    /**
     * Public constructor with JSP action element.
     * <p>
     * 
     * @param jsp
     *            an initialized JSP action element
     */
    public CmsLanguageCopyFolderAndLanguageSelectDialog(final CmsJspActionElement jsp) {

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
    public CmsLanguageCopyFolderAndLanguageSelectDialog(
        final PageContext context,
        final HttpServletRequest req,
        final HttpServletResponse res) {

        this(new CmsJspActionElement(context, req, res));

    }

    /**
     * @see org.opencms.workplace.CmsWidgetDialog#actionCommit()
     */
    @Override
    public void actionCommit() throws IOException, ServletException {

        initDialogObject();
        List<Throwable> errors = new ArrayList<Throwable>();
        // create absolute RFS path and store it in dialog object

        Map<String, String[]> params = new HashMap<String, String[]>();
        List<String> paths = m_dialogSettings.getPaths();
        String sourceLanguage = m_dialogSettings.getSourcelanguage();
        String targetLanguage = m_dialogSettings.getTargetlanguage();
        params.put(CmsLanguageCopySelectionList.PARAM_PATHS, paths.toArray(new String[paths.size()]));
        params.put(CmsLanguageCopySelectionList.PARAM_SOURCE_LANGUAGE, new String[] {sourceLanguage});
        params.put(CmsLanguageCopySelectionList.PARAM_TARGET_LANGUAGE, new String[] {targetLanguage});
        // set style to display report in correct layout
        params.put(PARAM_STYLE, new String[] {CmsToolDialog.STYLE_NEW});
        // set close link to get back to overview after finishing the import
        params.put(PARAM_CLOSELINK, new String[] {CmsToolManager.linkForToolPath(getJsp(), "/languagecopy")});
        // redirect to the report output JSP
        getToolManager().jspForwardPage(
            this,
            CmsWorkplace.PATH_WORKPLACE + "admin/contenttools/languagecopy/selectresources.jsp",
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
        result.append(createWidgetBlockStart(null));
        result.append(createDialogRowsHtml(0, 1));
        result.append(createWidgetBlockEnd());

        // create source language block
        result.append(createWidgetBlockStart(key(Messages.GUI_LANGUAGECOPY_SELECTLANGUAGE_SOURCE_BLOCK_0)));
        result.append(createDialogRowsHtml(2, 2));
        result.append(createWidgetBlockEnd());

        // create target language block
        result.append(createWidgetBlockStart(key(Messages.GUI_LANGUAGECOPY_SELECTLANGUAGE_TARGET_BLOCK_0)));
        result.append(createDialogRowsHtml(3, 3));
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
        List<CmsSelectWidgetOption> options = getLanguageSelections();

        addWidget(new CmsWidgetDialogParameter(
            m_dialogSettings,
            "message",
            key(Messages.GUI_LANGUAGECOPY_SELECTLANGUAGE_DIALOG_MESSAGE_0),
            PAGES[0],
            new CmsDisplayWidget(),
            1,
            1));
        addWidget(new CmsWidgetDialogParameter(m_dialogSettings, "paths", "/", PAGES[0], new CmsVfsFileWidget(
            false,
            getCms().getRequestContext().getSiteRoot()), 1, CmsWidgetDialogParameter.MAX_OCCURENCES));
        addWidget(new CmsWidgetDialogParameter(
            m_dialogSettings,
            "sourcelanguage",
            "/",
            PAGES[0],
            new CmsRadioSelectWidget(options),
            1,
            1));
        addWidget(new CmsWidgetDialogParameter(
            m_dialogSettings,
            "targetlanguage",
            "/",
            PAGES[0],
            new CmsRadioSelectWidget(options),
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
     * Returns a list with the possible <code>{@link Locale}</code> selections based on the OpenCms configuration.
     * <p>
     * 
     * @return a list with the possible <code>{@link Locale}</code> selections based on the OpenCms configuration.
     */
    private List<CmsSelectWidgetOption> getLanguageSelections() {

        List<CmsSelectWidgetOption> result = new LinkedList<CmsSelectWidgetOption>();
        List<Locale> sysLocales = OpenCms.getLocaleManager().getAvailableLocales();
        CmsSelectWidgetOption option;
        boolean first = true;
        for (Locale locale : sysLocales) {
            option = new CmsSelectWidgetOption(locale.toString(), first, locale.getDisplayName(getLocale()));
            first = false;
            result.add(option);
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
            m_dialogSettings = (CmsLanguageCopyFolderAndLanguageSelectDialogSettings)o;
        } else {
            m_dialogSettings = new CmsLanguageCopyFolderAndLanguageSelectDialogSettings();
            setDialogObject(m_dialogSettings);
        }
    }
}
