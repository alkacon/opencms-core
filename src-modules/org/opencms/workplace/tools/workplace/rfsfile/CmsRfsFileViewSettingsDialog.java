/*
 * File   : $Source: /alkacon/cvs/opencms/src-modules/org/opencms/workplace/tools/workplace/rfsfile/CmsRfsFileViewSettingsDialog.java,v $
 * Date   : $Date: 2005/06/23 09:05:01 $
 * Version: $Revision: 1.4 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Mananagement System
 *
 * Copyright (C) 2002 - 2004 Alkacon Software (http://www.alkacon.com)
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

package org.opencms.workplace.tools.workplace.rfsfile;

import org.opencms.configuration.CmsWorkplaceConfiguration;
import org.opencms.jsp.CmsJspActionElement;
import org.opencms.main.CmsSystemInfo;
import org.opencms.main.OpenCms;
import org.opencms.widgets.CmsCheckboxWidget;
import org.opencms.widgets.CmsComboWidget;
import org.opencms.widgets.CmsSelectWidgetOption;
import org.opencms.workplace.CmsWidgetDialogParameter;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.SortedMap;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.jsp.PageContext;

/**
 * A <code>{@link org.opencms.workplace.CmsWidgetDialog}</code> that allows 
 * modification of the properties of the
 * <code>{@link org.opencms.util.CmsRfsFileViewer}</code> bean.<p>
 * 
 * @author  Achim Westermann 
 * 
 * @version $Revision: 1.4 $ 
 * 
 * @since 6.0.0 
 */
public class CmsRfsFileViewSettingsDialog extends A_CmsRfsFileWidgetDialog {

    /**
     * @param jsp the CmsJspActionElement.
     */
    public CmsRfsFileViewSettingsDialog(CmsJspActionElement jsp) {

        super(jsp);
    }

    /**
     * Public constructor with JSP variables.<p>
     * 
     * @param context the JSP page context
     * @param req the JSP request
     * @param res the JSP response
     */
    public CmsRfsFileViewSettingsDialog(PageContext context, HttpServletRequest req, HttpServletResponse res) {

        this(new CmsJspActionElement(context, req, res));
    }

    /**
     * Commits the <code>{@link org.opencms.util.CmsRfsFileViewer}</code> to the 
     * <code>{@link org.opencms.workplace.CmsWorkplaceManager}</code>. <p>
     * 
     * @see org.opencms.workplace.CmsWidgetDialog#actionCommit()
     */
    public void actionCommit() {

        List errors = new ArrayList();

        try {
            // set the edited settings
            OpenCms.getWorkplaceManager().setFileViewSettings(getCms(), m_logView);
            // write the configuration
            OpenCms.writeConfiguration(CmsWorkplaceConfiguration.class);
            setDialogObject(null);
        } catch (Throwable t) {
            errors.add(t);
        }

        // set the list of errors to display when saving failed
        setCommitErrors(errors);
    }

    /**
     * Creates the dialog HTML for all defined widgets of the named dialog (page).<p>
     * 
     * This overwrites the method from the super class to create a layout variation for the widgets.<p>
     * 
     * @param dialog the dialog (page) to get the HTML for
     * @return the dialog HTML for all defined widgets of the named dialog (page)
     */
    protected String createDialogHtml(String dialog) {

        StringBuffer result = new StringBuffer(1024);

        // create widget table
        result.append(createWidgetTableStart());

        // show error header once if there were validation errors
        result.append(createWidgetErrorHeader());

        // create the widgets for the settings page
        result.append(dialogBlockStart(key(Messages.GUI_WORKPLACE_LOGVIEW_SETTINGS_NAME_0)));
        result.append(createWidgetTableStart());
        result.append(createDialogRowsHtml(0, 4));
        result.append(createWidgetTableEnd());
        result.append(dialogBlockEnd());

        result.append(createWidgetTableEnd());

        return result.toString();

    }

    /**
     * @see org.opencms.workplace.CmsWidgetDialog#defineWidgets()
     */
    protected void defineWidgets() {

        super.defineWidgets();
        addWidget(new CmsWidgetDialogParameter(m_logView, "isLogfile", "page1", new CmsCheckboxWidget("true")));
        addWidget(new CmsWidgetDialogParameter(m_logView, "filePath", "page1", new CmsComboWidget(
            createComboConfigurationFileChoice())));

        // options for windowsize combowidget:
        List comboOptions = new LinkedList();
        comboOptions.add(new CmsSelectWidgetOption("100"));
        comboOptions.add(new CmsSelectWidgetOption("200"));
        comboOptions.add(new CmsSelectWidgetOption("400"));
        comboOptions.add(new CmsSelectWidgetOption("600"));
        comboOptions.add(new CmsSelectWidgetOption("800"));
        addWidget(new CmsWidgetDialogParameter(m_logView, "windowSize", "page1", new CmsComboWidget(comboOptions)));

        // file encoding combowidget;
        addWidget(new CmsWidgetDialogParameter(m_logView, "fileEncoding", "page1", new CmsComboWidget(
            createComboConfigurationEncodingChoice())));

        addWidget(new CmsWidgetDialogParameter(m_logView, "enabled", "page1", new CmsCheckboxWidget()));

    }

    /**
     * Returns a list of <code>{@link org.opencms.widgets.CmsSelectWidgetOption}</code> instances for the  
     * <code>{@link CmsComboWidget}</code> with the supported encodings of the 
     * current system and the default encoding set as default combo option.<p> 
     * 
     * @return a list of <code>{@link org.opencms.widgets.CmsSelectWidgetOption}</code> instances for the  
     *         <code>{@link CmsComboWidget}</code> with the supported encodings of the 
     *         current system and the default encoding set as default combo option.<p>  
     */
    private List createComboConfigurationEncodingChoice() {

        List result = new LinkedList();
        SortedMap csMap = Charset.availableCharsets();
        // default charset: see http://java.sun.com/j2se/corejava/intl/reference/faqs/index.html#default-encoding
        // before java 1.5 there is no other way (System property "file.encoding" is implementation detail not in vmspec.
        Charset defaultCs = Charset.forName(new OutputStreamWriter(new ByteArrayOutputStream()).getEncoding());
        Charset cs;
        Iterator it = csMap.values().iterator();
        while (it.hasNext()) {
            cs = (Charset)it.next();
            // default? no equals required: safety by design!
            if (cs == defaultCs) {
                result.add(new CmsSelectWidgetOption(cs.name(), true, null, Messages.get().key(
                    getLocale(),
                    Messages.GUI_WORKPLACE_LOGVIEW_FILE_CHARSET_DEF_HELP_0,
                    null)));
            } else {
                if (!cs.name().startsWith("x")) {
                    result.add(new CmsSelectWidgetOption(cs.name(), false, null, Messages.get().key(
                        getLocale(),
                        Messages.GUI_WORKPLACE_LOGVIEW_FILE_CHARSET_HELP_0,
                        null)));
                }
            }
        }

        return result;

    }

    /**
     * Returns a list of <code>{@link org.opencms.widgets.CmsSelectWidgetOption}</code> instances for the 
     *         <code>{@link CmsComboWidget}</code> with default file locations of OpenCms.<p>
     * 
     * @return a list of <code>{@link org.opencms.widgets.CmsSelectWidgetOption}</code> instances for the 
     *         <code>{@link CmsComboWidget}</code> with default file locations of OpenCms
     * 
     */
    private List createComboConfigurationFileChoice() {

        List result = new LinkedList();
        CmsSystemInfo sysInfo = OpenCms.getSystemInfo();
        // log file, default
        result.add(new CmsSelectWidgetOption(sysInfo.getLogFileRfsPath(), true, null, Messages.get().key(
            getLocale(),
            Messages.GUI_WORKPLACE_LOGVIEW_FILE_LOG_HELP_0,
            null)));
        // opencms.properties
        result.add(new CmsSelectWidgetOption(sysInfo.getConfigurationFileRfsPath(), false, null, Messages.get().key(
            getLocale(),
            Messages.GUI_WORKPLACE_LOGVIEW_FILE_CONF_HELP_0,
            null)));
        // config xml 
        String configPath = sysInfo.getAbsoluteRfsPathRelativeToWebInf("/config");
        File configFolder = new File(configPath);
        File[] configFiles = configFolder.listFiles();
        File configFile;
        for (int i = 0; i < configFiles.length; i++) {
            configFile = configFiles[i];
            if (configFile.isFile()) {
                if (configFile.getName().endsWith(".xml")) {
                    result.add(new CmsSelectWidgetOption(configFile.getAbsolutePath(), false, null, Messages.get().key(
                        getLocale(),
                        Messages.GUI_WORKPLACE_LOGVIEW_FILE_XMLCONF_HELP_0,
                        null)));
                }
            }
        }
        return result;
    }
}