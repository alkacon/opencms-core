/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/workplace/Attic/CmsPreferences.java,v $
 * Date   : $Date: 2004/02/26 11:35:35 $
 * Version: $Revision: 1.15 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Mananagement System
 *
 * Copyright (C) 2002 - 2003 Alkacon Software (http://www.alkacon.com)
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
package org.opencms.workplace;

import org.opencms.db.CmsUserSettings;
import org.opencms.file.CmsProject;
import org.opencms.file.CmsUser;
import org.opencms.i18n.CmsEncoder;
import org.opencms.i18n.CmsLocaleManager;
import org.opencms.i18n.CmsMessages;
import org.opencms.jsp.CmsJspActionElement;
import org.opencms.main.CmsException;
import org.opencms.main.I_CmsConstants;
import org.opencms.main.OpenCms;
import org.opencms.security.CmsSecurityException;
import org.opencms.workplace.editor.CmsWorkplaceEditorConfiguration;

import java.io.IOException;
import java.util.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.PageContext;

/**
 * Provides methods for the user preferences dialog.<p> 
 * 
 * The following files use this class:
 * <ul>
 * <li>/jsp/dialogs/preferences_html
 * </ul>
 *
 * @author  Andreas Zahner (a.zahner@alkacon.com)
 * @version $Revision: 1.15 $
 * 
 * @since 5.1.12
 */
public class CmsPreferences extends CmsTabDialog {
    
    /** Value for the action: cancel button */
    public static final int ACTION_CANCEL = 200;
    
    /** Value for the action: change the password */
    public static final int ACTION_CHPWD = 202;
    
    /** Value for the action: show error screen */
    public static final int ACTION_ERROR = 203;
    
    /** Value for the action: reload the workplace */
    public static final int ACTION_RELOAD = 201;
    
    /** Constant for filter */
    private static final String C_SPACER = "------------------------------------------------";
    
    /** Request parameter value for the action: cancel button */
    public static final String DIALOG_CANCEL = "cancel";
    
    /** Request parameter value for the action: change the password */
    public static final String DIALOG_CHPWD = "chpwd";

    /** Request parameter value for the action: reload the workplace */
    public static final String DIALOG_RELOAD = "reload";

    /** The dialog type */
    public static final String DIALOG_TYPE = "preferences";
    
    /** Request parameter name prefix for the preferred editors */
    public static final String INPUT_DEFAULT = "default";
    
    /** Request parameter name for the direct edit button style */
    public static final String PARAM_DIRECTEDIT_BUTTONSTYLE = "tabeddirecteditbuttonstyle";
    
    /** Request parameter name for the editor button style */
    public static final String PARAM_EDITOR_BUTTONSTYLE = "tabedbuttonstyle";
    
    /** Request parameter name for the explorer button style */
    public static final String PARAM_EXPLORER_BUTTONSTYLE = "tabexbuttonstyle";
    
    /** Request parameter name for the explorer file date created */
    public static final String PARAM_EXPLORER_FILEDATECREATED = "tabexfiledatecreated";
    
    /** Request parameter name for the explorer file date last modified */
    public static final String PARAM_EXPLORER_FILEDATELASTMODIFIED = "tabexfiledatelastmodified";
    
    /** Request parameter name for the explorer file entry number */
    public static final String PARAM_EXPLORER_FILEENTRIES = "tabexfileentries";
    
    /** Request parameter name for the explorer file locked by */
    public static final String PARAM_EXPLORER_FILELOCKEDBY = "tabexfilelockedby";
    
    /** Request parameter name for the explorer file permissions */
    public static final String PARAM_EXPLORER_FILEPERMISSIONS = "tabexfilepermissions";
    
    /** Request parameter name for the explorer file size */
    public static final String PARAM_EXPLORER_FILESIZE = "tabexfilesize";
    
    /** Request parameter name for the explorer file state */
    public static final String PARAM_EXPLORER_FILESTATE = "tabexfilestate";
    
    /** Request parameter name for the explorer file title */
    public static final String PARAM_EXPLORER_FILETITLE = "tabexfiletitle";
    
    /** Request parameter name for the explorer file type */
    public static final String PARAM_EXPLORER_FILETYPE = "tabexfiletype";
    
    /** Request parameter name for the explorer file user created */
    public static final String PARAM_EXPLORER_FILEUSERCREATED = "tabexfileusercreated";
    
    /** Request parameter name for the explorer file user last modified */
    public static final String PARAM_EXPLORER_FILEUSERLASTMODIFIED = "tabexfileuserlastmodified";
    
    /** Request parameter name for the new password */
    public static final String PARAM_NEWPASSWORD = "newpassword";
    
    /** Request parameter name for the old password */
    public static final String PARAM_OLDPASSWORD = "oldpassword";
    
    /** Request parameter name prefix for the preferred editors */
    public static final String PARAM_PREFERREDEDITOR_PREFIX = "tabedprefed_";
    
    /** Request parameter name for the workflow filter */
    public static final String PARAM_WORKFLOW_FILTER = "tabwffilter";
    
    /** Request parameter name for the workflow message accepted */
    public static final String PARAM_WORKFLOW_MESSAGEACCEPTED = "tabwfmessageaccepted";
    
    /** Request parameter name for the workflow message completed */
    public static final String PARAM_WORKFLOW_MESSAGECOMPLETED = "tabwfmessagecompleted";
    
    /** Request parameter name for the workflow message forwarded */
    public static final String PARAM_WORKFLOW_MESSAGEFORWARDED = "tabwfmessageforwarded";
    
    /** Request parameter name for the workflow message members */
    public static final String PARAM_WORKFLOW_MESSAGEMEMBERS = "tabwfmessagemembers";
    
    /** Request parameter name for the workflow show all projects */
    public static final String PARAM_WORKFLOW_SHOWALLPROJECTS = "tabwfshowallprojects";
    
    /** Request parameter name for the workplace button style */
    public static final String PARAM_WORKPLACE_BUTTONSTYLE = "tabwpbuttonstyle";
    
    /** Request parameter name for the dialog copy file siblings default setting */
    public static final String PARAM_DIALOGS_COPYFILEMODE = "tabdicopyfilemode";
    
    /** Request parameter name for the dialog copy folder siblings default setting */
    public static final String PARAM_DIALOGS_COPYFOLDERMODE = "tabdicopyfoldermode";
    
    /** Request parameter name for the dialog delete file siblings default setting */
    public static final String PARAM_DIALOGS_DELETEFILEMODE = "tabdideletefilemode";
    
    /** Request parameter name for the workplace language */
    public static final String PARAM_WORKPLACE_LANGUAGE = "tabwplanguage";
    
    /** Request parameter name for the workplace project */
    public static final String PARAM_WORKPLACE_PROJECT = "tabwpproject";
    
    /** Request parameter name for the dialog publish file siblings default setting */
    public static final String PARAM_DIALOGS_PUBLISHFILEMODE = "tabdipublishfilemode";
    
    /** Request parameter name for the workplace report type */
    public static final String PARAM_WORKPLACE_REPORTTYPE = "tabwpreporttype";
    
    /** Request parameter name for the dialog show lock */
    public static final String PARAM_DIALOGS_SHOWLOCK = "tabdishowlock";
    
    /** Request parameter name for the workplace use upload applet */
    public static final String PARAM_WORKPLACE_USEUPLOADAPPLET = "tabwpuseuploadapplet";
    
    /** Request parameter name for the workplace view */
    public static final String PARAM_WORKPLACE_VIEW = "tabwpview";
    
    private String m_paramNewPassword;
    private String m_paramOldPassword;

    /** User settings object used to store the dialog field values **/
    private CmsUserSettings m_userSettings;
    
    /**
     * Public constructor with JSP action element.<p>
     * 
     * @param jsp an initialized JSP action element
     */
    public CmsPreferences(CmsJspActionElement jsp) {
        super(jsp);
    }
    
    /**
     * Public constructor with JSP variables.<p>
     * 
     * @param context the JSP page context
     * @param req the JSP request
     * @param res the JSP response
     */
    public CmsPreferences(PageContext context, HttpServletRequest req, HttpServletResponse res) {
        this(new CmsJspActionElement(context, req, res));
    }  
    
    /**
     * Performs the cancel operation of the settings dialog, i.e. redirecting to the right workplace view.<p>
     */
    public void actionCancel() {
        try {
            if (getSettings().isViewAdministration()) {                  
                String adminLink = I_CmsWpConstants.C_VFS_PATH_WORKPLACE + "action/administration.html";
                sendCmsRedirect(adminLink);
            } else {
                sendCmsRedirect(C_PATH_WORKPLACE + "/explorer_fs.html");
            }
        } catch (IOException e) {
            // do nothing
        }
    }
    
    /**
     * Performs the change password action.<p>
     * 
     * @throws JspException if inclusion of error element fails
     */
    public void actionChangePassword() throws JspException {
        // save initialized instance of this class in request attribute for included sub-elements
        getJsp().getRequest().setAttribute(C_SESSION_WORKPLACE_CLASS, this);
        String oldPwd = getParamOldPassword();
        String newPwd = getParamNewPassword();
        // set the action parameter, reset the password parameters
        setAction(ACTION_DEFAULT);
        setParamOldPassword(null);
        setParamNewPassword(null);
        if (oldPwd != null && !"".equals(oldPwd.trim()) && newPwd != null && !"".equals(newPwd.trim())) {
            try {
                getCms().setPassword(getSettings().getUser().getName(), oldPwd, newPwd);
            } catch (CmsSecurityException e) {
                // the specified username + old password do not match
                setAction(ACTION_ERROR);
                setParamErrorstack(e.getStackTraceAsString());
                setParamMessage(key("error.message.chpwd"));
                setParamReasonSuggestion(key("error.reason.chpwd4") + "<br>\n" + key("error.suggestion.chpwd3") + "\n");
                getJsp().include(C_FILE_DIALOG_SCREEN_ERROR);                
            } catch (CmsException e) {
                // failed setting the new password, show error dialog
                setAction(ACTION_ERROR);
                setParamErrorstack(e.getStackTraceAsString());
                setParamMessage(key("error.message.chpwd"));
                setParamReasonSuggestion(key("error.reason.chpwd2") + "<br>\n" + key("error.suggestion.chpwd2") + "\n");
                getJsp().include(C_FILE_DIALOG_SCREEN_ERROR);
            }
        } else {
            // form wasn't filled out correctly, show error dialog
            CmsException e = new CmsException("The password values you entered are not valid.");
            setAction(ACTION_ERROR);
            setParamErrorstack(e.getStackTraceAsString());
            setParamMessage(key("error.message.chpwd"));
            setParamReasonSuggestion(key("error.reason.chpwd2") + "<br>\n" + key("error.suggestion.chpwd2") + "\n");
            getJsp().include(C_FILE_DIALOG_SCREEN_ERROR);
        }
    }
    
    /**
     * Performs the save operation of the modified user settings.<p>
     */
    public void actionSave() {
        HttpServletRequest request = getJsp().getRequest();
        // save initialized instance of this class in request attribute for included sub-elements
        request.setAttribute(C_SESSION_WORKPLACE_CLASS, this);                 
        
        // special case: set the preferred editor settings the user settings object      
        Enumeration enum = request.getParameterNames();
        while (enum.hasMoreElements()) {
            // search all request parameters for the presence of the preferred editor parameters
            String paramName = (String)enum.nextElement();
            if (paramName.startsWith(PARAM_PREFERREDEDITOR_PREFIX)) {
                String paramValue = request.getParameter(paramName);
                if (paramValue != null && !INPUT_DEFAULT.equals(paramValue.trim())) {
                    m_userSettings.setPreferredEditor(paramName.substring(PARAM_PREFERREDEDITOR_PREFIX.length()), paramValue);
                } else {
                    m_userSettings.setPreferredEditor(paramName.substring(PARAM_PREFERREDEDITOR_PREFIX.length()), null);
                }
            }
        }
        
        // set the current user in the settings object
        m_userSettings.setUser(getSettings().getUser());
        try {
            // write the user settings to the db
            m_userSettings.save(getCms());
        } catch (CmsException e) {
            // ignore this exception
        }
        
        // update the preferences and project after saving
        CmsWorkplaceAction.updatePreferences(getCms(), getJsp().getRequest());

        try {
            int projectId = Integer.parseInt(m_userSettings.getStartProject());
            getCms().getRequestContext().setCurrentProject(getCms().readProject(projectId));
            getSettings().setProject(projectId);            
        } catch (Exception e) {
            // ignore this exception
        }
        
        // now determine if the dialog has to be closed or not
        try {
            if (DIALOG_SET.equals(getParamAction())) {
                // after "set" action, leave dialog open 
                sendCmsRedirect(C_PATH_DIALOGS + "/preferences.html?" + PARAM_TAB + "=" + getActiveTab());
            } else {
                // after "ok" action, close dialog and reload the workplace view
                setParamOkFunctions("window.top.location.reload(true);");
                try {
                    closeDialog();
                } catch (JspException e) {
                    // closing dialog failed, redirect to dialog with action set to reload the workplace
                    sendCmsRedirect(C_PATH_DIALOGS + "/preferences.html?" + PARAM_ACTION + "=" + DIALOG_RELOAD);
                }    
            }
        } catch (IOException e) {
            // error during redirect, do nothing 
        }
    }
    
    /**
     * Builds the html for a common button style select box.<p>
     * 
     * @param htmlAttributes optional html attributes for the &lgt;select&gt; tag
     * @param selectedIndex the index of the selected option
     * @return the html for the common button style select box
     */
    private String buildSelectButtonStyle(String htmlAttributes, int selectedIndex) {
        List options = new ArrayList(3);      
        options.add(key("preferences.buttonstyle.img"));
        options.add(key("preferences.buttonstyle.imgtxt"));
        options.add(key("preferences.buttonstyle.txt"));
        String [] vals = new String[] {"0", "1", "2"};
        List values = new ArrayList(java.util.Arrays.asList(vals));
        return buildSelect(htmlAttributes, options, values, selectedIndex);
    }
    
    /**
     * Builds the html for the default copy file mode select box.<p>
     * 
     * @param htmlAttributes optional html attributes for the &lgt;select&gt; tag
     * @return the html for the default copy file mode select box
     */
    public String buildSelectCopyFileMode(String htmlAttributes) {
        List options = new ArrayList(2);
        options.add(key("preferences.workplace.default.copy.file.sibling"));
        options.add(key("preferences.workplace.default.copy.file.asnew"));
        List values = new ArrayList(2);
        values.add("" + I_CmsConstants.C_COPY_AS_SIBLING);
        values.add("" + I_CmsConstants.C_COPY_AS_NEW);
        int selectedIndex = values.indexOf(getParamTabDiCopyFileMode());
        return buildSelect(htmlAttributes, options, values, selectedIndex);
    }
    
    /**
     * Builds the html for the default copy folder mode select box.<p>
     * 
     * @param htmlAttributes optional html attributes for the &lgt;select&gt; tag
     * @return the html for the default copy folder mode select box
     */
    public String buildSelectCopyFolderMode(String htmlAttributes) {
        List options = new ArrayList(3);
        options.add(key("preferences.workplace.default.copy.folder.sibling"));
        options.add(key("preferences.workplace.default.copy.folder.preserve"));
        options.add(key("preferences.workplace.default.copy.folder.asnew"));
        List values = new ArrayList(3);
        values.add("" + I_CmsConstants.C_COPY_AS_SIBLING);
        values.add("" + I_CmsConstants.C_COPY_PRESERVE_SIBLING);
        values.add("" + I_CmsConstants.C_COPY_AS_NEW);
        int selectedIndex = values.indexOf(getParamTabDiCopyFolderMode());
        return buildSelect(htmlAttributes, options, values, selectedIndex);
    }
    
    /**
     * Builds the html for the default delete file mode select box.<p>
     * 
     * @param htmlAttributes optional html attributes for the &lgt;select&gt; tag
     * @return the html for the default delete file mode select box
     */
    public String buildSelectDeleteFileMode(String htmlAttributes) {
        List options = new ArrayList(2);
        options.add(key("preferences.workplace.default.delete.deletesibling"));
        options.add(key("preferences.workplace.default.delete.preservesibling"));
        List values = new ArrayList(2);
        values.add("" + I_CmsConstants.C_DELETE_OPTION_DELETE_SIBLINGS);
        values.add("" + I_CmsConstants.C_DELETE_OPTION_PRESERVE_SIBLINGS);
        int selectedIndex = values.indexOf(getParamTabDiDeleteFileMode());
        return buildSelect(htmlAttributes, options, values, selectedIndex);
    }
    
    /**
     * Builds the html for the direct edit button style select box.<p>
     * 
     * @param htmlAttributes optional html attributes for the &lgt;select&gt; tag
     * @return the html for the direct edit button style select box
     */
    public String buildSelectDirectEditButtonStyle(String htmlAttributes) {
        int selectedIndex = Integer.parseInt(getParamTabEdDirectEditButtonStyle());
        return buildSelectButtonStyle(htmlAttributes, selectedIndex);
    }
    
    /**
     * Builds the html for the editor button style select box.<p>
     * 
     * @param htmlAttributes optional html attributes for the &lgt;select&gt; tag
     * @return the html for the editor button style select box
     */
    public String buildSelectEditorButtonStyle(String htmlAttributes) {
        int selectedIndex = Integer.parseInt(getParamTabEdButtonStyle());
        return buildSelectButtonStyle(htmlAttributes, selectedIndex);
    }
    
    /**
     * Builds the html for the explorer button style select box.<p>
     * 
     * @param htmlAttributes optional html attributes for the &lgt;select&gt; tag
     * @return the html for the explorer button style select box
     */
    public String buildSelectExplorerButtonStyle(String htmlAttributes) {
        int selectedIndex = Integer.parseInt(getParamTabExButtonStyle());
        return buildSelectButtonStyle(htmlAttributes, selectedIndex);
    }
    
    /**
     * Builds the html for the explorer number of entries per page select box.<p>
     * 
     * @param htmlAttributes optional html attributes for the &lgt;select&gt; tag
     * @return the html for the explorer number of entries per page select box
     */
    public String buildSelectExplorerFileEntries(String htmlAttributes) {
        String [] opts = new String[] {"10", "25", "50", "100", key("preferences.fileentries.unlimited")};
        List options = new ArrayList(java.util.Arrays.asList(opts));
        String [] vals = new String[] {"10", "25", "50", "100", "" + Integer.MAX_VALUE};
        int selectedIndex = 2;
        for (int i=0; i<vals.length; i++) {
            if (vals[i].equals(getParamTabExFileEntries())) {
                selectedIndex = i;
            }
        }
        List values = new ArrayList(java.util.Arrays.asList(vals));
        return buildSelect(htmlAttributes, options, values, selectedIndex);
    }
    
    /**
     * Builds the html for the task startup filter select box.<p>
     * 
     * @param htmlAttributes optional html attributes for the &lgt;select&gt; tag
     * @return the html for the task startup filter select box
     */
    public String buildSelectFilter(String htmlAttributes) {
        List options = new ArrayList(16);
        List values = new ArrayList(16);
        int selectedIndex = -1;
        int counter = 0;
        
        for (int i=1; i<4; i++) {
            for (char k='a'; k<'d'; k++) {
                options.add(key(I_CmsConstants.C_TASK_FILTER + k + i));
                values.add("" + k + i);
                if (("" + k + i).equals(getParamTabWfFilter())) {
                    selectedIndex = counter;
                }
                counter++;
            }
            options.add(C_SPACER);
            values.add("");
            counter++;
        }
        for (int i=1; i<4; i++) {
            options.add(key(I_CmsConstants.C_TASK_FILTER + "d" + i));
            values.add("d" + i);
            if (("d" + i).equals(getParamTabWfFilter())) {
                selectedIndex = counter;
            }
            counter++;
        }
        return buildSelect(htmlAttributes, options, values, selectedIndex);  
    }
    
    /**
     * Builds the html for the language select box of the start settings.<p>
     *  
     * @param htmlAttributes optional html attributes for the &lgt;select&gt; tag
     * @return the html for the language select box
     */
    public String buildSelectLanguage(String htmlAttributes) {
        // get available locales from the workplace manager
        Set locales = OpenCms.getWorkplaceManager().getLocales();
        List options = new ArrayList(locales.size());
        List values = new ArrayList(locales.size());
        int checkedIndex = 0;
        int counter = 0;
        Iterator i = locales.iterator();
        while (i.hasNext()) {
            Locale currentLocale = (Locale)i.next();
            // add all locales to the select box
            options.add(currentLocale.getDisplayLanguage(getSettings().getUserSettings().getLocale()));
            values.add(currentLocale.getLanguage());
            if (getParamTabWpLanguage().equals(currentLocale.getLanguage())) {
                // mark the currently active locale
                checkedIndex = counter;
            }
            counter++;
        }
        return buildSelect(htmlAttributes, options, values, checkedIndex);
    }
    
    /**
     * Builds the html for the preferred editors select boxes of the editor settings.<p>
     *  
     * @param htmlAttributes optional html attributes for the &lgt;select&gt; tag
     * @return the html for the preferred editors select boxes
     */
    public String buildSelectPreferredEditors(String htmlAttributes) {
        StringBuffer result = new StringBuffer(1024);
        HttpServletRequest request = getJsp().getRequest();
        if (htmlAttributes != null) {
            htmlAttributes += " name=\"" + PARAM_PREFERREDEDITOR_PREFIX;
        }
        Map resourceEditors = OpenCms.getWorkplaceManager().getWorkplaceEditorManager().getConfigurableEditors();
        if (resourceEditors != null) {
            // first: iterate over the resource types
            Iterator i = resourceEditors.keySet().iterator();
            while (i.hasNext()) {
                String currentResourceType = (String)i.next();
                SortedMap availableEditors = (TreeMap)resourceEditors.get(currentResourceType);
                if (availableEditors != null && availableEditors.size() > 0) {
                    String preSelection = computeEditorPreselection(request, currentResourceType);
                    List options = new ArrayList(availableEditors.size() + 1);
                    List values = new ArrayList(availableEditors.size() + 1);
                    options.add(key("preferences.editor.best"));
                    values.add(INPUT_DEFAULT);
                    // second: iteration over the available editors for the resource type                   
                    int selectedIndex = 0;
                    int counter = 1;
                    while (availableEditors.size() > 0) {
                        Float key = (Float)availableEditors.lastKey();                      
                        CmsWorkplaceEditorConfiguration conf = (CmsWorkplaceEditorConfiguration)availableEditors.get(key);
                        options.add(key(conf.getEditorLabel()));
                        values.add(conf.getEditorUri());
                        if (conf.getEditorUri().equals(preSelection)) {
                            selectedIndex = counter;
                        }
                        counter++;
                        availableEditors.remove(key);
                    }
                    
                    // create the table row for the current resource type
                    result.append("<tr>\n\t<td style=\"white-space: nowrap;\">");
                    String localizedName = key("fileicon." + currentResourceType);
                    if (localizedName.startsWith(CmsMessages.C_UNKNOWN_KEY_EXTENSION)) {
                        localizedName = currentResourceType;
                    }
                    result.append(localizedName);
                    result.append("</td>\n\t<td>");
                    result.append(buildSelect(htmlAttributes + currentResourceType + "\"", options, values, selectedIndex));
                    result.append("</td>\n</tr>\n");
                }
            }
        }
        return result.toString();
    }
    
    /**
     * Builds the html for the project select box of the start settings.<p>
     * 
     * @param htmlAttributes optional html attributes for the &lgt;select&gt; tag 
     * @return the html for the project select box
     */
    public String buildSelectProject(String htmlAttributes) {
        try {
            Vector allProjects = getCms().getAllAccessibleProjects();
            List options = new ArrayList(allProjects.size());
            List values = new ArrayList(allProjects.size());
            int checkedIndex = 0;
            int startProjectId = -1;
            try {
                startProjectId = Integer.parseInt(getParamTabWpProject());
            } catch (NumberFormatException e) {
                // ignore this exception
            }
            
            for (int i=0; i<allProjects.size(); i++) {
                CmsProject project = (CmsProject)allProjects.get(i);
                options.add(project.getName());
                values.add("" + project.getId());
                if (startProjectId == project.getId()) {
                    checkedIndex = i;
                }
            }
            return buildSelect(htmlAttributes, options, values, checkedIndex);
        } catch (CmsException e) {
            return getSettings().getProject() + "";
        }
    }
    
    /**
     * Builds the html for the default publish siblings mode select box.<p>
     * 
     * @param htmlAttributes optional html attributes for the &lgt;select&gt; tag
     * @return the html for the default publish siblings mode select box
     */
    public String buildSelectPublishSiblings(String htmlAttributes) {
        List options = new ArrayList(2);
        options.add(key("preferences.workplace.default.publish.sibling"));
        options.add(key("preferences.workplace.default.publish.nosibling"));
        List values = new ArrayList(2);
        values.add("true");
        values.add("false");
        int selectedIndex = values.indexOf(getParamTabDiPublishFileMode());
        return buildSelect(htmlAttributes, options, values, selectedIndex);
    }
    
    /**
     * Builds the html for the workplace report type select box.<p>
     * 
     * @param htmlAttributes optional html attributes for the &lgt;select&gt; tag
     * @return the html for the workplace report type select box
     */
    public String buildSelectReportType(String htmlAttributes) {
        List options = new ArrayList(2);      
        options.add(key("preferences.report.simple"));
        options.add(key("preferences.report.extended"));
        String [] vals = new String[] {CmsReport.REPORT_TYPE_SIMPLE, CmsReport.REPORT_TYPE_EXTENDED};
        List values = new ArrayList(java.util.Arrays.asList(vals));
        int selectedIndex = 0;
        if (CmsReport.REPORT_TYPE_EXTENDED.equals(getParamTabWpReportType())) {
            selectedIndex = 1;
        }
        return buildSelect(htmlAttributes, options, values, selectedIndex);
    }
    
    /**
     * Returns a html select box filled with the views accessible by the current user.<p>
     * 
     * @param htmlAttributes attributes that will be inserted into the generated html 
     * @return a html select box filled with the views accessible by the current user
     */    
    public String buildSelectView(String htmlAttributes) {
        
        List options = new ArrayList();
        List values = new ArrayList();
        int selectedIndex = 0;        
        
        // get the Vector of available views from the registry      
        Vector viewNames = new Vector();
        Vector viewLinks = new Vector();
        getCms().getRegistry().getViews(viewNames, viewLinks);                       

        // loop through the vectors and fill the result vectors
        int numViews = viewNames.size();
        for (int i = 0; i<numViews; i++) {
            String loopName = (String)viewNames.get(i);
            String loopLink = (String)viewLinks.get(i);
            
            boolean visible = true;
            try {
                getCms().readFileHeader(loopLink);
            } catch (CmsException e) {
                visible = false;
            }
            if (visible) {
                //loopLink = getJsp().link(loopLink);
                options.add(key(loopName));
                values.add(loopLink);

                if (loopLink.equals(getParamTabWpView())) {
                    selectedIndex = i;
                }
            }
        }        
  
        return buildSelect(htmlAttributes, options, values, selectedIndex);        
    }
    
    /**
     * Builds the html for the workplace button style select box.<p>
     * 
     * @param htmlAttributes optional html attributes for the &lgt;select&gt; tag
     * @return the html for the workplace button style select box
     */
    public String buildSelectWorkplaceButtonStyle(String htmlAttributes) {
        int selectedIndex = Integer.parseInt(getParamTabWpButtonStyle());
        return buildSelectButtonStyle(htmlAttributes, selectedIndex);
    }
    
    /**
     * Builds the html code for the static user information table (tab 4).<p>
     * 
     * @return the html code for the static user information table
     */
    public String buildUserInformation() {
        StringBuffer result = new StringBuffer(512);
        CmsUser user = getSettings().getUser();
        CmsUserSettings settings = new CmsUserSettings(user);
        
        result.append("<table border=\"0\" cellspacing=\"0\" cellpadding=\"4\">\n");
        result.append("<tr>\n");
        result.append("\t<td style=\"width: 25%;\">" + key("input.user") + "</td>\n");
        result.append("\t<td class=\"textbold\" style=\"width: 25%;\">" + user.getName() + "</td>\n");
        result.append("\t<td style=\"width: 25%;\">" + key("input.email") + "</td>\n");
        result.append("\t<td class=\"textbold\" style=\"width: 25%;\">" + user.getEmail() + "</td>\n");        
        result.append("</tr>\n");
        
        result.append("<tr>\n");
        result.append("\t<td>" + key("input.lastname") + "</td>\n");
        result.append("\t<td class=\"textbold\">" + user.getLastname() + "</td>\n");
        result.append("\t<td rowspan=\"3\" style=\"vertical-align: top;\">" + key("input.adress") + "</td>\n");
        
        String address = user.getAddress();
        if ((settings.getAddressZip() != null && !"".equals(settings.getAddressZip())) 
                || (settings.getAddressTown() != null && !"".equals(settings.getAddressTown()))) {
            address += "<br>" + settings.getAddressZip();
            address += " " + settings.getAddressTown();
        }        
        result.append("\t<td rowspan=\"3\" class=\"textbold\" style=\"vertical-align: top;\">" + address + "</td>\n");        
        result.append("</tr>\n");
        
        result.append("<tr>\n");
        result.append("\t<td>" + key("input.firstname") + "</td>\n");
        result.append("\t<td class=\"textbold\">" + user.getFirstname() + "</td>\n");       
        result.append("</tr>\n");
        
        result.append("<tr>\n");
        result.append("\t<td>" + key("input.description") + "</td>\n");
        result.append("\t<td class=\"textbold\">" + user.getDescription() + "</td>\n");       
        result.append("</tr>\n");       
        result.append("</table>\n");
        
        return result.toString();
    }
    
    /**
     * Returns the action for the "cancel" button of the error dialog.<p>
     * 
     * This overwrites the cancel method of the CmsDialog class.<p>
     * 
     * Always use this value, do not write anything directly in the html page.<p>
     * 
     * @return the default action for a "cancel" button
     */
    public String buttonActionCancel() {
        String target = OpenCms.getLinkManager().substituteLink(getCms(), CmsWorkplaceAction.C_JSP_WORKPLACE_URI);
        return "onClick=\"top.location.href='" + target + "';\"";
    }
    
    /**
     * Returns the preferred editor preselection value either from the request, if not present, from the user settings.<p>
     * 
     * @param request the current http servlet request
     * @param resourceType the preferred editors resource type 
     * @return the preferred editor preselection value or null, if none found
     */
    private String computeEditorPreselection(HttpServletRequest request, String resourceType) {
        // first check presence of the setting in request parameter
        String preSelection = request.getParameter(PARAM_PREFERREDEDITOR_PREFIX + resourceType);
        if (preSelection != null && !"".equals(preSelection.trim())) {
            return CmsEncoder.decode(preSelection);
        } else {
            // no value found in request, check current user settings (not the member!)
            CmsUserSettings userSettings = new CmsUserSettings(getSettings().getUser());
            return userSettings.getPreferredEditor(resourceType);
            
        }
    }
    
    /**
     * Fills the parameter values according to the settings of the current user.<p>
     * 
     * This method is called once when first displaying the preferences dialog.<p>
     */
    private void fillUserSettings() {
        m_userSettings = new CmsUserSettings(getSettings().getUser());
    }
    
    /**
     * Returns the new password value.<p>
     * 
     * @return the new password value
     */
    public String getParamNewPassword() {
        return m_paramNewPassword;
    }
    
    /**
     * Returns the old password value.<p>
     * 
     * @return the old password value
     */
    public String getParamOldPassword() {
        return m_paramOldPassword;
    }
    
    /**
     * Returns the "editor button style" setting.<p>
     * 
     * @return the "editor button style" setting
     */
    public String getParamTabEdButtonStyle() {
        return "" + m_userSettings.getEditorButtonStyle();
    }
    
    /**
     * Returns the "direct edit button style" setting.<p>
     * 
     * @return the "direct edit button style" setting
     */
    public String getParamTabEdDirectEditButtonStyle() {
        return "" + m_userSettings.getDirectEditButtonStyle();
    }
    
    
    
    /**
     * Returns the "explorer button style" setting.<p>
     * 
     * @return the "explorer button style" setting
     */
    public String getParamTabExButtonStyle() {
        return "" + m_userSettings.getExplorerButtonStyle();
    }
    
    /**
     * Returns the "display file creation date" setting.<p>
     * 
     * @return "true" if the file creation date input field is checked, otherwise ""
     */
    public String getParamTabExFileDateCreated() {
        return isParamEnabled(m_userSettings.showExplorerFileDateCreated());
    }
    
    /**
     * Returns the "display file last modification date" setting.<p>
     * 
     * @return "true" if the file last modification date input field is checked, otherwise ""
     */
    public String getParamTabExFileDateLastModified() {
        return isParamEnabled(m_userSettings.showExplorerFileDateLastModified());
    }
    
    /**
     * Returns the "explorer number of entries per page" setting.<p>
     * 
     * @return the "explorer number of entries per page" setting
     */
    public String getParamTabExFileEntries() {
        return "" + m_userSettings.getExplorerFileEntries();
    }
    
    /**
     * Returns the "display file locked by" setting.<p>
     * 
     * @return "true" if the file locked by input field is checked, otherwise ""
     */
    public String getParamTabExFileLockedBy() {
        return isParamEnabled(m_userSettings.showExplorerFileLockedBy());
    }
    
    /**
     * Returns the "display file permissions" setting.<p>
     * 
     * @return "true" if the file permissions input field is checked, otherwise ""
     */
    public String getParamTabExFilePermissions() {
        return isParamEnabled(m_userSettings.showExplorerFilePermissions());
    }
    
    /**
     * Returns the "display file size" setting.<p>
     * 
     * @return "true" if the file size input field is checked, otherwise ""
     */
    public String getParamTabExFileSize() {
        return isParamEnabled(m_userSettings.showExplorerFileSize());
    }
    
    /**
     * Returns the "display file state" setting.<p>
     * 
     * @return "true" if the file state input field is checked, otherwise ""
     */
    public String getParamTabExFileState() {
        return isParamEnabled(m_userSettings.showExplorerFileState());
    }
    
    /**
     * Returns the "display file title" setting.<p>
     * 
     * @return "true" if the file title input field is checked, otherwise ""
     */
    public String getParamTabExFileTitle() {
        return isParamEnabled(m_userSettings.showExplorerFileTitle());
    }
    
    /**
     * Returns the "display file type" setting.<p>
     * 
     * @return "true" if the file type input field is checked, otherwise ""
     */
    public String getParamTabExFileType() {
        return isParamEnabled(m_userSettings.showExplorerFileType());
    }
    
    /**
     * Returns the "display file created by" setting.<p>
     * 
     * @return "true" if the file created by input field is checked, otherwise ""
     */
    public String getParamTabExFileUserCreated() {
        return isParamEnabled(m_userSettings.showExplorerFileUserCreated());
    }
    
    /**
     * Returns the "display file last modified by" setting.<p>
     * 
     * @return "true" if the file last modified by input field is checked, otherwise ""
     */
    public String getParamTabExFileUserLastModified() {
        return isParamEnabled(m_userSettings.showExplorerFileUserLastModified());
    }
    
    /**
     * Returns the "task startup filter" setting.<p>
     * 
     * @return the "task startup filter" setting
     */
    public String getParamTabWfFilter() {
        return m_userSettings.getTaskStartupFilter();
    }
    
    /**
     * Returns the "message when accepted" setting.<p>
     * 
     * @return the "message when accepted" setting
     */
    public String getParamTabWfMessageAccepted() {
        return isParamEnabled(m_userSettings.taskMessageAccepted());
    }
    
    /**
     * Returns the "message when completed" setting.<p>
     * 
     * @return the "message when completed" setting
     */
    public String getParamTabWfMessageCompleted() {
        return isParamEnabled(m_userSettings.taskMessageCompleted());
    }
    
    /**
     * Returns the "message when forwarded" setting.<p>
     * 
     * @return the "message when forwarded" setting
     */
    public String getParamTabWfMessageForwarded() {
        return isParamEnabled(m_userSettings.taskMessageForwarded());
    }
    
    /**
     * Returns the "inform all role members" setting.<p>
     * 
     * @return "true" if the "inform all role members" input field is checked, otherwise ""
     */
    public String getParamTabWfMessageMembers() {
        return isParamEnabled(m_userSettings.taskMessageMembers());
    }
    
    /**
     * returns the "show all projects" setting.<p>
     * 
     * @return the "show all projects" setting
     */
    public String getParamTabWfShowAllProjects() {
        return isParamEnabled(m_userSettings.taskShowAllProjects());
    }
    
    /**
     * Returns the "workplace button style" setting.<p>
     * 
     * @return the "workplace button style" setting
     */
    public String getParamTabWpButtonStyle() {
        return "" + m_userSettings.getWorkplaceButtonStyle();
    }
    
    /**
     * Returns the "copy file default" setting.<p>
     * 
     * @return the "copy file default" setting
     */
    public String getParamTabDiCopyFileMode() {
        return "" + m_userSettings.getDialogCopyFileMode();
    }
    
    /**
     * Returns the "copy folder default" setting.<p>
     * 
     * @return the "copy folder default" setting
     */
    public String getParamTabDiCopyFolderMode() {
        return "" + m_userSettings.getDialogCopyFolderMode();
    }
    
    /**
     * Returns the "delete file default" setting.<p>
     * 
     * @return the "delete file default" setting
     */
    public String getParamTabDiDeleteFileMode() {
        return "" + m_userSettings.getDialogDeleteFileMode();
    }
    
    /**
     * Returns the start language setting.<p>
     * 
     * @return the start language setting
     */
    public String getParamTabWpLanguage() {
        return m_userSettings.getLocale().toString();
    }
    
    /**
     * Returns the start project setting.<p>
     * 
     * @return the start project setting
     */
    public String getParamTabWpProject() {
        return m_userSettings.getStartProject();
    }
    
    /**
     * Returns the "publish file siblings default" setting.<p>
     * 
     * @return the "publish file siblings default" setting
     */
    public String getParamTabDiPublishFileMode() {
        return "" + m_userSettings.getDialogPublishSiblings();
    }
    
    /**
     * Returns the "workplace report type" setting.<p>
     * 
     * @return the "workplace report type" setting
     */
    public String getParamTabWpReportType() {
        return m_userSettings.getWorkplaceReportType();
    }
    
    /**
     * Returns the "display lock dialog" setting.<p>
     * 
     * @return "true" if the "display lock dialog" input field is checked, otherwise ""
     */
    public String getParamTabDiShowLock() {
        return isParamEnabled(m_userSettings.showLockDialog());
    }
    
    /**
     * Returns the "use upload applet" setting.<p>
     * 
     * @return "true" if the "use upload applet" input is checked, otherwise ""
     */
    public String getParamTabWpUseUploadApplet() {
        return isParamEnabled(m_userSettings.useUploadApplet());
    }
    
    /**
     * Returns the start view setting.<p>
     * 
     * @return the start view setting
     */
    public String getParamTabWpView() {
        return m_userSettings.getStartView();
    }
    
    /**
     * @see org.opencms.workplace.CmsTabDialog#getTabParameterOrder()
     */
    public List getTabParameterOrder() {
        ArrayList orderList = new ArrayList(5);
        orderList.add("tabwp");
        orderList.add("tabex");
        orderList.add("tabdi");
        orderList.add("tabed");
        orderList.add("tabwf");
        orderList.add("tabup");
        return orderList;
    }
    
    /**
     * @see org.opencms.workplace.CmsTabDialog#getTabs()
     */
    public List getTabs() {
        ArrayList tabList = new ArrayList(6);
        tabList.add(key("panel.workplace"));
        tabList.add(key("panel.explorer"));
        tabList.add(key("panel.dialogs"));
        tabList.add(key("panel.editors"));
        tabList.add(key("panel.task"));
        tabList.add(key("panel.user"));
        return tabList;
    }

    /**
     * @see org.opencms.workplace.CmsWorkplace#initWorkplaceRequestValues(org.opencms.workplace.CmsWorkplaceSettings, javax.servlet.http.HttpServletRequest)
     */
    protected void initWorkplaceRequestValues(CmsWorkplaceSettings settings, HttpServletRequest request) {
        // create an empty user settings object
        m_userSettings = new CmsUserSettings(); 
        // fill the parameter values in the get/set methods
        fillParamValues(request);
             
        // get the active tab from request parameter or display first tab
        getActiveTab();
        
        // set the dialog type
        setParamDialogtype(DIALOG_TYPE);
        // set the action for the JSP switch 
        if (DIALOG_SET.equals(getParamAction())) {
            setAction(ACTION_SET);
        } else if (DIALOG_OK.equals(getParamAction())) {
            setAction(ACTION_OK); 
        } else if (DIALOG_RELOAD.equals(getParamAction())) {
            setAction(ACTION_RELOAD); 
        } else if (DIALOG_CANCEL.equals(getParamAction())) {
            setAction(ACTION_CANCEL); 
        } else if (DIALOG_CHPWD.equals(getParamAction())) {
            setAction(ACTION_CHPWD); 
        } else {
            if (!DIALOG_SWITCHTAB.equals(getParamAction())) {
                // first call of preferences dialog, fill param values with current settings
                fillUserSettings();
            }
            
            setAction(ACTION_DEFAULT);
            // build title for preferences dialog     
            setParamTitle(key("title.preferences"));
        }      
     
    } 
    
    /**
     * Helper method to add the "checked" attribute to an input field.<p>
     * 
     * @param paramValue the parameter value, if "true", the "checked" attribute will be returned 
     * @return the "checked" attribute or an empty String 
     */
    public String isChecked(String paramValue) {
        if ("true".equals(paramValue)) {
            return " checked=\"checked\"";
        }
        return "";
    }
    
    /**
     * Helper method for the request parameter methods to return a String depending on the boolean parameter.<p>
     * 
     * @param isEnabled the boolean variable to check
     * @return "true" if isEnabled is true, otherwise ""
     */
    private String isParamEnabled(boolean isEnabled) {
        if (isEnabled) {
            return "true";
        }
        return "";
    }
    
    /**
     * Returns the values of all parameter methods of this workplace class instance.<p>
     * 
     * This overwrites the super method because of the possible dynamic editor selection entries.<p> 
     * 
     * @return the values of all parameter methods of this workplace class instance
     */
    protected Map paramValues() {
        Map map = super.paramValues();
        HttpServletRequest request = getJsp().getRequest();
        Enumeration enum = request.getParameterNames();
        while (enum.hasMoreElements()) {
            String paramName = (String)enum.nextElement();
            if (paramName.startsWith(PARAM_PREFERREDEDITOR_PREFIX)) {
                String paramValue = request.getParameter(paramName);
                if (paramValue != null && !"".equals(paramValue.trim())) {
                    map.put(paramName, CmsEncoder.decode(paramValue));
                }
            }
        }         
        return map;
    }
    
    /**
     * Sets the new password value.<p>
     * 
     * @param newPwd the new password value
     */
    public void setParamNewPassword(String newPwd) {
        m_paramNewPassword = newPwd;
    }
    
    /**
     * Sets the old password value.<p>
     * 
     * @param oldPwd the old password value
     */
    public void setParamOldPassword(String oldPwd) {
        m_paramOldPassword = oldPwd;
    }
    
    /**
     * Sets the "editor button style" setting.<p>
     * 
     * @param value a String representation of an int value to set the "editor button style" setting
     */
    public void setParamTabEdButtonStyle(String value) {
        try {
            m_userSettings.setEditorButtonStyle(Integer.parseInt(value));
        } catch (Throwable t) {
            // ignore this exception
        }
    }
    
    /**
     * Sets the "direct edit button style" setting.<p>
     * 
     * @param value a String representation of an int value to set the "direct edit button style" setting
     */
    public void setParamTabEdDirectEditButtonStyle(String value) {
        try {
            m_userSettings.setDirectEditButtonStyle(Integer.parseInt(value));
        } catch (Throwable t) {
            // ignore this exception
        }
    }
    
    /**
     * Sets the "explorer button style" setting.<p>
     * 
     * @param value a String representation of an int value to set the "explorer button style" setting
     */
    public void setParamTabExButtonStyle(String value) {
        try {
            m_userSettings.setExplorerButtonStyle(Integer.parseInt(value));
        } catch (Throwable t) {
            // ignore this exception
        }
    }

    /**
     * Sets the "display file creation date" setting.<p>
     * 
     * @param value "true" to enable the "display file creation date" setting, all others to disable
     */
    public void setParamTabExFileDateCreated(String value) {
        m_userSettings.setShowExplorerFileDateCreated("true".equals(value));
    }

    /**
     * Sets the "display file last modification date" setting.<p>
     * 
     * @param value "true" to enable the "display file last modification date" setting, all others to disable
     */
    public void setParamTabExFileDateLastModified(String value) {
        m_userSettings.setShowExplorerFileDateLastModified("true".equals(value));
    }
    
    /**
     * Sets the "explorer number of entries per page" setting.<p>
     * 
     * @param value a String representation of an int value to set the "number of entries per page" setting
     */
    public void setParamTabExFileEntries(String value) {
        try {
            m_userSettings.setExplorerFileEntries(Integer.parseInt(value));
        } catch (Throwable t) {
            // ignore this exception
        }
    }

    /**
     * Sets the "display file locked by" setting.<p>
     * 
     * @param value "true" to enable the "display file locked by" setting, all others to disable
     */
    public void setParamTabExFileLockedBy(String value) {
        m_userSettings.setShowExplorerFileLockedBy("true".equals(value));
    }

    /**
     * Sets the "display file permissions" setting.<p>
     * 
     * @param value "true" to enable the "display file permissions" setting, all others to disable
     */
    public void setParamTabExFilePermissions(String value) {
        m_userSettings.setShowExplorerFilePermissions("true".equals(value));
    }
    
    /**
     * Sets the "display file size" setting.<p>
     * 
     * @param value "true" to enable the "display file size" setting, all others to disable
     */
    public void setParamTabExFileSize(String value) {
        m_userSettings.setShowExplorerFileSize("true".equals(value));
    }

    /**
     * Sets the "display file state" setting.<p>
     * 
     * @param value "true" to enable the "display file state" setting, all others to disable
     */
    public void setParamTabExFileState(String value) {
        m_userSettings.setShowExplorerFileState("true".equals(value));
    }

    /**
     * Sets the "display file title" setting.<p>
     * 
     * @param value "true" to enable the "display file title" setting, all others to disable
     */
    public void setParamTabExFileTitle(String value) {
        m_userSettings.setShowExplorerFileTitle("true".equals(value));
    }

    /**
     * Sets the "display file type" setting.<p>
     * 
     * @param value "true" to enable the "display file type" setting, all others to disable
     */
    public void setParamTabExFileType(String value) {
        m_userSettings.setShowExplorerFileType("true".equals(value));
    }

    /**
     * Sets the "display file created by" setting.<p>
     * 
     * @param value "true" to enable the "display file created by" setting, all others to disable
     */
    public void setParamTabExFileUserCreated(String value) {
        m_userSettings.setShowExplorerFileUserCreated("true".equals(value));
    }

    /**
     * Sets the "display file last modified by" setting.<p>
     * 
     * @param value "true" to enable the "display file last modified by" setting, all others to disable
     */
    public void setParamTabExFileUserLastModified(String value) {
        m_userSettings.setShowExplorerFileUserLastModified("true".equals(value));
    }
    
    /**
     * Sets the "task startup filter" setting.<p>
     * 
     * @param filter the "task startup filter" setting
     */
    public void setParamTabWfFilter(String filter) {
        if ("".equals(filter)) {
            filter = "a1";
        }
        m_userSettings.setTaskStartupFilter(filter);
    }
    
    /**
     * Sets the "message when accepted" setting.<p>
     * 
     * @param value the "message when accepted" setting
     */
    public void setParamTabWfMessageAccepted(String value) {
        m_userSettings.setTaskMessageAccepted("true".equals(value));
    }
    
    /**
     * Sets the "message when completed" setting.<p>
     * 
     * @param value the "message when completed" setting
     */
    public void setParamTabWfMessageCompleted(String value) {
        m_userSettings.setTaskMessageCompleted("true".equals(value));
    }
    
    /**
     * Sets the "message when forwarded" setting.<p>
     * 
     * @param value the "message when forwarded" setting
     */
    public void setParamTabWfMessageForwarded(String value) {
        m_userSettings.setTaskMessageForwarded("true".equals(value));
    }
    
    /**
     * Sets the "inform all role members" setting.<p>
     * 
     * @param value "true" to enable the "inform all role members" setting, all others to disable
     */
    public void setParamTabWfMessageMembers(String value) {
        m_userSettings.setTaskMessageMembers("true".equals(value));
    }
    
    /**
     * Sets the "show all projects" setting.<p>
     * 
     * @param value the "show all projects" setting
     */
    public void setParamTabWfShowAllProjects(String value) {
        m_userSettings.setTaskShowAllProjects("true".equals(value));
    }
    
    /**
     * Sets the "workplace button style" setting.<p>
     * 
     * @param value a String representation of an int value to set the "workplace button style" setting
     */
    public void setParamTabWpButtonStyle(String value) {
        try {
            m_userSettings.setWorkplaceButtonStyle(Integer.parseInt(value));
        } catch (Throwable t) {
            // ignore this exception
        }
    }
    
    /**
     * Sets the "copy file default" setting.<p>
     * 
     * @param value the "copy file default" setting
     */
    public void setParamTabDiCopyFileMode(String value) {
        try {
            m_userSettings.setDialogCopyFileMode(Integer.parseInt(value));
        } catch (Throwable t) {
            // ignore this exception
        }
    }
    
    /**
     * Sets the "copy folder default" setting.<p>
     * 
     * @param value the "copy folder default" setting
     */
    public void setParamTabDiCopyFolderMode(String value) {
        try {
            m_userSettings.setDialogCopyFolderMode(Integer.parseInt(value));
        } catch (Throwable t) {
            // ignore this exception
        }
    }
    
    /**
     * Sets the "delete file siblings default" setting.<p>
     * 
     * @param value the "delete file siblings default" setting
     */
    public void setParamTabDiDeleteFileMode(String value) {
        try {
            m_userSettings.setDialogDeleteFileMode(Integer.parseInt(value));
        } catch (Throwable t) {
            // ignore this exception
        }
    }

    /**
     * Sets the start language setting.<p>
     * 
     * @param value the start language setting
     */
    public void setParamTabWpLanguage(String value) {
        m_userSettings.setLocale(CmsLocaleManager.getLocale(value));
    }

    /**
     * Sets the start project setting.<p>
     * 
     * @param value the start project setting
     */
    public void setParamTabWpProject(String value) {
        m_userSettings.setStartProject(value);
    }
    
    /**
     * Sets the "publish file siblings default" setting.<p>
     * 
     * @param value the "publish file siblings default" setting
     */
    public void setParamTabDiPublishFileMode(String value) {
        m_userSettings.setDialogPublishSiblings("true".equals(value));
    }

    /**
     * Sets the "workplace report type" setting.<p>
     * 
     * @param value the "workplace report type" setting
     */
    public void setParamTabWpReportType(String value) {
        if (CmsReport.REPORT_TYPE_SIMPLE.equals(value) || CmsReport.REPORT_TYPE_EXTENDED.equals(value)) {
            // set only if valid parameter value is found
            m_userSettings.setWorkplaceReportType(value);
        }
    }

    /**
     * Sets the "display lock dialog" setting.<p>
     * 
     * @param value "true" to enable the "display lock dialog" setting, all others to disable
     */
    public void setParamTabDiShowLock(String value) {
        m_userSettings.setShowLockDialog("true".equals(value));
    }

    /**
     * Sets the "use upload applet" setting.<p>
     * 
     * @param value "true" to enable the "use upload applet" setting, all others to disable
     */
    public void setParamTabWpUseUploadApplet(String value) {
        m_userSettings.setUseUploadApplet("true".equals(value));
    }

    /**
     * Sets the start view setting.<p>
     * 
     * @param value the start view setting
     */
    public void setParamTabWpView(String value) {
        m_userSettings.setStartView(value);
    }
    
}
