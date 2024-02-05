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

package org.opencms.workplace.commons;

import org.opencms.configuration.CmsDefaultUserSettings;
import org.opencms.configuration.preferences.CmsBuiltinPreference.SelectOptions;
import org.opencms.configuration.preferences.CmsStartViewPreference;
import org.opencms.db.CmsUserSettings;
import org.opencms.db.CmsUserSettings.CmsSearchResultStyle;
import org.opencms.db.CmsUserSettings.UploadVariant;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsProject;
import org.opencms.file.CmsPropertyDefinition;
import org.opencms.file.CmsResource;
import org.opencms.file.CmsResource.CmsResourceCopyMode;
import org.opencms.file.CmsResource.CmsResourceDeleteMode;
import org.opencms.file.CmsResourceFilter;
import org.opencms.file.CmsUser;
import org.opencms.i18n.CmsEncoder;
import org.opencms.i18n.CmsLocaleManager;
import org.opencms.jsp.CmsJspActionElement;
import org.opencms.loader.CmsLoaderException;
import org.opencms.main.CmsContextInfo;
import org.opencms.main.CmsException;
import org.opencms.main.CmsLog;
import org.opencms.main.OpenCms;
import org.opencms.report.I_CmsReport;
import org.opencms.security.CmsPasswordInfo;
import org.opencms.site.CmsSite;
import org.opencms.synchronize.CmsSynchronizeSettings;
import org.opencms.util.CmsStringUtil;
import org.opencms.widgets.CmsCalendarWidget;
import org.opencms.workplace.CmsTabDialog;
import org.opencms.workplace.CmsWorkplace;
import org.opencms.workplace.CmsWorkplaceManager;
import org.opencms.workplace.CmsWorkplaceMessages;
import org.opencms.workplace.CmsWorkplaceSettings;
import org.opencms.workplace.editors.CmsWorkplaceEditorConfiguration;
import org.opencms.workplace.explorer.CmsExplorerTypeSettings;
import org.opencms.workplace.galleries.A_CmsAjaxGallery;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.PageContext;

import org.apache.commons.logging.Log;

/**
 * Provides methods for the user preferences dialog. <p>
 *
 * The following files use this class:
 * <ul>
 * <li>/commons/preferences.jsp
 * </ul>
 * <p>
 *
 * @since 6.0.0
 */
public class CmsPreferences extends CmsTabDialog {

    /** Value for the action: change the password. */
    public static final int ACTION_CHPWD = 202;

    /** Value for the action: show error screen. */
    public static final int ACTION_ERROR = 203;

    /** Value for the action: reload the workplace. */
    public static final int ACTION_RELOAD = 201;

    /** Request parameter value for the action: change the password. */
    public static final String DIALOG_CHPWD = "chpwd";

    /** Request parameter value for the action: reload the workplace. */
    public static final String DIALOG_RELOAD = "reload";

    /** The dialog type. */
    public static final String DIALOG_TYPE = "preferences";

    /** Request parameter name for global gallery settings. */
    public static final String INPUT_PRESELECT = "preselect";

    /** Request parameter name for the dialog copy file siblings default setting. */
    public static final String PARAM_DIALOGS_COPYFILEMODE = "tabdicopyfilemode";

    /** Request parameter name for the dialog copy folder siblings default setting. */
    public static final String PARAM_DIALOGS_COPYFOLDERMODE = "tabdicopyfoldermode";

    /** Request parameter name for the dialog delete file siblings default setting. */
    public static final String PARAM_DIALOGS_DELETEFILEMODE = "tabdideletefilemode";

    /** Request parameter name for the dialog permissions show inherited permissions. */
    public static final String PARAM_DIALOGS_PERMISSIONS_EXPANDINHERITED = "tabdipermissionsexpandinherited";

    /** Request parameter name for the dialog permissions show current users permissions. */
    public static final String PARAM_DIALOGS_PERMISSIONS_EXPANDUSER = "tabdipermissionsexpanduser";

    /** Request parameter name for the dialog permissions default inheritance behavior on folders. */
    public static final String PARAM_DIALOGS_PERMISSIONS_INHERITONFOLDER = "tabdipermissionsinheritonfolder";

    /** Request parameter name for the dialog publish file siblings default setting. */
    public static final String PARAM_DIALOGS_PUBLISHFILEMODE = "tabdipublishfilemode";

    /** Request parameter name for the dialog show lock. */
    public static final String PARAM_DIALOGS_SHOWLOCK = "tabdishowlock";

    /** Request parameter name for the direct edit button style. */
    public static final String PARAM_DIRECTEDIT_BUTTONSTYLE = "tabeddirecteditbuttonstyle";

    /** Request parameter name for the editor button style. */
    public static final String PARAM_EDITOR_BUTTONSTYLE = "tabedbuttonstyle";

    /** Request parameter name for the explorer button style. */
    public static final String PARAM_EXPLORER_BUTTONSTYLE = "tabexbuttonstyle";

    /** Request parameter name for the explorer file date created. */
    public static final String PARAM_EXPLORER_FILEDATECREATED = "tabexfiledatecreated";

    /** Request parameter name for the explorer file date expired. */
    public static final String PARAM_EXPLORER_FILEDATEEXPIRED = "tabexfiledateexpired";

    /** Request parameter name for the explorer file date last modified. */
    public static final String PARAM_EXPLORER_FILEDATELASTMODIFIED = "tabexfiledatelastmodified";

    /** Request parameter name for the explorer file date released. */
    public static final String PARAM_EXPLORER_FILEDATERELEASED = "tabexfiledatereleased";

    /** Request parameter name for the explorer file entry number. */
    public static final String PARAM_EXPLORER_FILEENTRIES = "tabexfileentries";

    /** Request parameter name for the explorer file locked by. */
    public static final String PARAM_EXPLORER_FILELOCKEDBY = "tabexfilelockedby";

    /** Request parameter name for the explorer file navtext. */
    public static final String PARAM_EXPLORER_FILENAVTEXT = "tabexfilenavtext";

    /** Request parameter name for the explorer file permissions. */
    public static final String PARAM_EXPLORER_FILEPERMISSIONS = "tabexfilepermissions";

    /** Request parameter name for the explorer file size. */
    public static final String PARAM_EXPLORER_FILESIZE = "tabexfilesize";

    /** Request parameter name for the explorer file state. */
    public static final String PARAM_EXPLORER_FILESTATE = "tabexfilestate";

    /** Request parameter name for the explorer file title. */
    public static final String PARAM_EXPLORER_FILETITLE = "tabexfiletitle";

    /** Request parameter name for the explorer file type. */
    public static final String PARAM_EXPLORER_FILETYPE = "tabexfiletype";

    /** Request parameter name for the explorer file user created. */
    public static final String PARAM_EXPLORER_FILEUSERCREATED = "tabexfileusercreated";

    /** Request parameter name for the explorer file user last modified. */
    public static final String PARAM_EXPLORER_FILEUSERLASTMODIFIED = "tabexfileuserlastmodified";

    /** Request parameter name for the workplace search result list style. */
    public static final String PARAM_EXPLORER_SEARCH_RESULT = "tabexworkplacesearchresult";

    /** Request parameter name for the new password. */
    public static final String PARAM_NEWPASSWORD = "newpassword";

    /** Request parameter name for the old password. */
    public static final String PARAM_OLDPASSWORD = "oldpassword";

    /** Request parameter name prefix for the preferred editors. */
    public static final String PARAM_PREFERREDEDITOR_PREFIX = "tabedprefed_";

    /** Request parameter name prefix for the preferred editors. */
    public static final String PARAM_STARTGALLERY_PREFIX = "tabgastartgallery_";

    /** Request parameter name for the workplace button style. */
    public static final String PARAM_WORKPLACE_BUTTONSTYLE = "tabwpbuttonstyle";

    /** Request parameter name for the workplace start folder. */
    public static final String PARAM_WORKPLACE_FOLDER = "tabwpfolder";

    /** Request parameter name for the workplace language. */
    public static final String PARAM_WORKPLACE_LANGUAGE = "tabwplanguage";

    /** Request parameter name for the user language. */
    public static final String PARAM_WORKPLACE_LISTALLPROJECTS = "tabwplistallprojects";

    /** Request parameter name for the workplace project. */
    public static final String PARAM_WORKPLACE_PROJECT = "tabwpproject";

    /** Request parameter name for the workplace report type. */
    public static final String PARAM_WORKPLACE_REPORTTYPE = "tabwpreporttype";

    /** Request parameter name for the workplace explorer view restriction. */
    public static final String PARAM_WORKPLACE_RESTRICTEXPLORERVIEW = "tabwprestrictexplorerview";

    /** Request parameter name for the workplace show publish notification. */
    public static final String PARAM_WORKPLACE_SHOWPUBLISHNOTIFICATION = "tabwpshowpublishnotification";

    /** Request parameter name for the workplace start site. */
    public static final String PARAM_WORKPLACE_SITE = "tabwpsite";

    /** Request parameter name for the user language. */
    public static final String PARAM_WORKPLACE_TIMEWARP = "tabwptimewarp";

    /** Request parameter name for the workplace to choose the upload variant. */
    public static final String PARAM_WORKPLACE_UPLOADVARIANT = "tabwpuploadvariant";

    /** Request parameter name for the workplace view. */
    public static final String PARAM_WORKPLACE_VIEW = "tabwpview";

    /** The log object for this class. */
    private static final Log LOG = CmsLog.getLog(CmsPreferences.class);

    /** The old password. */
    private String m_paramNewPassword;

    /** The new password. */
    private String m_paramOldPassword;

    /** User settings object used to store the dialog field values. */
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
     * Gets the options for the language selector.<p>
     *
     * @param setLocale the locale for the select options
     * @param prevLocale the locale currently set
     *
     * @return the options for the language selector
     */
    public static SelectOptions getOptionsForLanguageStatic(Locale setLocale, Locale prevLocale) {

        // get available locales from the workplace manager
        List<Locale> locales = OpenCms.getWorkplaceManager().getLocales();
        List<String> options = new ArrayList<String>(locales.size());
        List<String> values = new ArrayList<String>(locales.size());
        int checkedIndex = 0;
        int counter = 0;
        Iterator<Locale> i = locales.iterator();
        while (i.hasNext()) {
            Locale currentLocale = i.next();
            // add all locales to the select box
            String language = currentLocale.getDisplayLanguage(setLocale);
            if (CmsStringUtil.isNotEmpty(currentLocale.getCountry())) {
                language = language + " (" + currentLocale.getDisplayCountry(setLocale) + ")";
            }
            if (CmsStringUtil.isNotEmpty(currentLocale.getVariant())) {
                language = language + " (" + currentLocale.getDisplayVariant(setLocale) + ")";
            }
            options.add(language);
            values.add(currentLocale.toString());
            if (prevLocale.toString().equals(currentLocale.toString())) {
                // mark the currently active locale
                checkedIndex = counter;
            }
            counter++;
        }
        SelectOptions selectOptions = new SelectOptions(options, values, checkedIndex);
        return selectOptions;
    }

    /**
     * Gets the options for the project selector.<p>
     *
     * @param cms  the CMS context
     * @param startProject the start project
     * @param  locale the locale
     *
     * @return the options for the project selector
     */
    public static SelectOptions getProjectSelectOptionsStatic(CmsObject cms, String startProject, Locale locale) {

        List<CmsProject> allProjects;
        try {
            String ouFqn = "";
            CmsUserSettings settings = new CmsUserSettings(cms);
            if (!settings.getListAllProjects()) {
                ouFqn = cms.getRequestContext().getCurrentUser().getOuFqn();
            }
            allProjects = OpenCms.getOrgUnitManager().getAllAccessibleProjects(
                cms,
                ouFqn,
                settings.getListAllProjects());
        } catch (CmsException e) {
            // should usually never happen
            if (LOG.isErrorEnabled()) {
                LOG.error(e.getLocalizedMessage(), e);
            }
            allProjects = Collections.emptyList();
        }

        boolean singleOu = true;
        String ouFqn = null;
        Iterator<CmsProject> itProjects = allProjects.iterator();
        while (itProjects.hasNext()) {
            CmsProject prj = itProjects.next();
            if (prj.isOnlineProject()) {
                // skip the online project
                continue;
            }
            if (ouFqn == null) {
                // set the first ou
                ouFqn = prj.getOuFqn();
            }
            if (!ouFqn.equals(prj.getOuFqn())) {
                // break if one different ou is found
                singleOu = false;
                break;
            }
        }

        List<String> options = new ArrayList<String>(allProjects.size());
        List<String> values = new ArrayList<String>(allProjects.size());
        int checkedIndex = 0;

        for (int i = 0, n = allProjects.size(); i < n; i++) {
            CmsProject project = allProjects.get(i);
            String projectName = project.getSimpleName();
            if (!singleOu && !project.isOnlineProject()) {
                try {
                    projectName = projectName
                        + " - "
                        + OpenCms.getOrgUnitManager().readOrganizationalUnit(cms, project.getOuFqn()).getDisplayName(
                            locale);
                } catch (CmsException e) {
                    projectName = projectName + " - " + project.getOuFqn();
                }
            }
            options.add(projectName);
            values.add(project.getName());
            if (startProject.equals(project.getName())) {
                checkedIndex = i;
            }
        }
        SelectOptions selectOptions = new SelectOptions(options, values, checkedIndex);
        return selectOptions;
    }

    /**
     * Gets the options for the site selector.<p>
     *
     * @param cms the CMS context
     * @param wpSite the selected site
     * @param locale the locale for the select options
     *
     * @return the options for the site selector
     */
    public static SelectOptions getSiteSelectOptionsStatic(CmsObject cms, String wpSite, Locale locale) {

        List<String> options = new ArrayList<String>();
        List<String> values = new ArrayList<String>();
        int selectedIndex = 0;

        List<CmsSite> sites = OpenCms.getSiteManager().getAvailableSites(
            cms,
            true,
            false,
            cms.getRequestContext().getOuFqn());
        if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(wpSite) && !wpSite.endsWith("/")) {
            wpSite += "/";
        }

        Iterator<CmsSite> i = sites.iterator();
        int pos = 0;
        while (i.hasNext()) {
            CmsSite site = i.next();
            String siteRoot = site.getSiteRoot();
            if (!siteRoot.endsWith("/")) {
                siteRoot += "/";
            }
            values.add(siteRoot);
            options.add(CmsWorkplace.substituteSiteTitleStatic(site.getTitle(), locale));
            if (siteRoot.equals(wpSite)) {
                // this is the user's currently chosen site
                selectedIndex = pos;
            }
            pos++;
        }

        if (sites.size() < 1) {
            // no site found, assure that at least the current site is shown in the selector
            String siteRoot = cms.getRequestContext().getSiteRoot();
            CmsSite site = OpenCms.getSiteManager().getSiteForSiteRoot(siteRoot);
            if (!siteRoot.endsWith("/")) {
                siteRoot += "/";
            }
            String title = "";
            if (site != null) {
                title = site.getTitle();
            }
            values.add(siteRoot);
            options.add(title);
        }
        SelectOptions selectOptions = new SelectOptions(options, values, selectedIndex);
        return selectOptions;
    }

    /**
     * Performs the change password action.<p>
     *
     * @throws JspException if inclusion of error element fails
     */
    public void actionChangePassword() throws JspException {

        // save initialized instance of this class in request attribute for included sub-elements
        getJsp().getRequest().setAttribute(SESSION_WORKPLACE_CLASS, this);
        String newPwd = getParamNewPassword();
        String oldPwd = getParamOldPassword();
        // set the action parameter, reset the password parameters
        setAction(ACTION_DEFAULT);
        setParamOldPassword(null);
        setParamNewPassword(null);

        try {
            CmsPasswordInfo pwdInfo = new CmsPasswordInfo(getCms());
            pwdInfo.setCurrentPwd(oldPwd);
            pwdInfo.setNewPwd(newPwd);
            pwdInfo.setConfirmation(newPwd);
            pwdInfo.applyChanges();
        } catch (Throwable e) {
            // failed setting the new password, show error dialog
            setAction(ACTION_ERROR);
            includeErrorpage(this, e);
        }
    }

    /**
     * Performs the save operation of the modified user settings.<p>
     */
    public void actionSave() {

        HttpServletRequest request = getJsp().getRequest();
        // save initialized instance of this class in request attribute for included sub-elements
        request.setAttribute(SESSION_WORKPLACE_CLASS, this);

        // special case: set the preferred editor settings in the user settings object
        CmsUserSettings userSettings = new CmsUserSettings(getSettings().getUser());
        // first set the old preferred editors
        m_userSettings.setEditorSettings(userSettings.getEditorSettings());
        // also set the old start gallery settings
        m_userSettings.setStartGalleriesSetting(userSettings.getStartGalleriesSettings());
        // then set the old synchronization settings
        m_userSettings.setSynchronizeSettings(userSettings.getSynchronizeSettings());
        Enumeration<String> en = request.getParameterNames();
        while (en.hasMoreElements()) {
            // search all request parameters for the presence of the preferred editor parameters
            String paramName = en.nextElement();
            if (paramName.startsWith(PARAM_PREFERREDEDITOR_PREFIX)) {
                String paramValue = request.getParameter(paramName);
                if ((paramValue != null) && !INPUT_DEFAULT.equals(paramValue.trim())) {
                    // set selected editor for this resource type
                    m_userSettings.setPreferredEditor(
                        paramName.substring(PARAM_PREFERREDEDITOR_PREFIX.length()),
                        CmsEncoder.decode(paramValue));
                } else {
                    // reset preferred editor for this resource type
                    m_userSettings.setPreferredEditor(paramName.substring(PARAM_PREFERREDEDITOR_PREFIX.length()), null);
                }
            } else if (paramName.startsWith(PARAM_STARTGALLERY_PREFIX)) {
                String paramValue = request.getParameter(paramName);
                if (paramValue != null) {
                    // set the selected start gallery for the gallery type
                    m_userSettings.setStartGallery(
                        paramName.substring(PARAM_STARTGALLERY_PREFIX.length()),
                        CmsEncoder.decode(paramValue));
                }
            }
        }

        // set the current user in the settings object
        m_userSettings.setUser(getSettings().getUser());
        m_userSettings.setAdditionalPreferencesFrom(userSettings);

        try {
            // write the user settings to the db
            m_userSettings.save(getCms());
        } catch (CmsException e) {
            // should usually never happen
            if (LOG.isInfoEnabled()) {
                LOG.info(e.getLocalizedMessage());
            }
        }

        // update the preferences and project after saving
        updatePreferences(getCms(), getJsp().getRequest());

        try {
            String projectName = m_userSettings.getStartProject();
            CmsProject project = getCms().readProject(projectName);
            getCms().getRequestContext().setCurrentProject(project);
            getSettings().setProject(project.getUuid());
        } catch (Exception e) {
            // should usually never happen
            LOG.error(e.getLocalizedMessage());
        }

        // now determine if the dialog has to be closed or not
        try {
            if (DIALOG_SET.equals(getParamAction())) {
                // after "set" action, leave dialog open
                Map<String, String[]> params = new HashMap<String, String[]>();
                params.put(PARAM_TAB, new String[] {String.valueOf(getActiveTab())});
                params.put(PARAM_SETPRESSED, new String[] {Boolean.TRUE.toString()});
                sendForward(getJsp().getRequestContext().getUri(), params);
            } else {
                // forward to dialog with action set to reload the workplace
                Map<String, String[]> params = new HashMap<String, String[]>();
                params.put(PARAM_ACTION, new String[] {DIALOG_RELOAD});
                sendForward(getJsp().getRequestContext().getUri(), params);
            }
        } catch (IOException e) {
            // error during forward, do nothing
            if (LOG.isInfoEnabled()) {
                LOG.info(e.getLocalizedMessage());
            }
        } catch (ServletException e) {
            // error during forward, do nothing
            if (LOG.isInfoEnabled()) {
                LOG.info(e.getLocalizedMessage());
            }
        }
    }

    /**
     * Builds the html for the default copy file mode select box.<p>
     *
     * @param htmlAttributes optional html attributes for the &lgt;select&gt; tag
     * @return the html for the default copy file mode select box
     */
    public String buildSelectCopyFileMode(String htmlAttributes) {

        List<String> options = new ArrayList<String>(2);
        options.add(key(Messages.GUI_PREF_COPY_AS_SIBLING_0));
        options.add(key(Messages.GUI_COPY_AS_NEW_0));
        List<String> values = new ArrayList<String>(2);
        values.add(CmsResource.COPY_AS_SIBLING.toString());
        values.add(CmsResource.COPY_AS_NEW.toString());
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

        List<String> options = new ArrayList<String>(3);
        options.add(key(Messages.GUI_PREF_COPY_AS_SIBLINGS_0));
        options.add(key(Messages.GUI_PREF_PRESERVE_SIBLINGS_RESOURCES_0));
        options.add(key(Messages.GUI_PREF_COPY_AS_NEW_0));
        List<String> values = new ArrayList<String>(3);
        values.add(CmsResource.COPY_AS_SIBLING.toString());
        values.add(CmsResource.COPY_PRESERVE_SIBLING.toString());
        values.add(CmsResource.COPY_AS_NEW.toString());
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

        List<String> options = new ArrayList<String>(2);
        options.add(key(Messages.GUI_PREF_PRESERVE_SIBLINGS_0));
        options.add(key(Messages.GUI_PREF_DELETE_SIBLINGS_0));
        List<String> values = new ArrayList<String>(2);
        values.add(String.valueOf(CmsResource.DELETE_PRESERVE_SIBLINGS));
        values.add(String.valueOf(CmsResource.DELETE_REMOVE_SIBLINGS));
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

        String emptyOption = OpenCms.getWorkplaceManager().getDefaultUserSettings().getExplorerFileEntryOptions();
        if (CmsStringUtil.isEmptyOrWhitespaceOnly(emptyOption)) {
            emptyOption = "50,100,200,300,400,500";
        }
        // remove all non digits without ','
        emptyOption = emptyOption.replaceAll("[^0-9|^,]", "");
        // remove all empty entries
        emptyOption = emptyOption.replaceAll(",,", ",");
        List<String> opts = CmsStringUtil.splitAsList(emptyOption, ",", true);
        opts.add(key(Messages.GUI_LABEL_UNLIMITED_0));
        opts.remove("0");
        List<String> vals = CmsStringUtil.splitAsList(emptyOption, ",", true);
        vals.add("" + Integer.MAX_VALUE);
        vals.remove("0");
        int selectedIndex = 2;
        for (int i = 0; i < vals.size(); i++) {
            if (vals.get(i).equals(getParamTabExFileEntries())) {
                selectedIndex = i;
                break;
            }
        }
        return buildSelect(htmlAttributes, opts, vals, selectedIndex);
    }

    /**
     * Builds the html for the language select box of the start settings.<p>
     *
     * @param htmlAttributes optional html attributes for the &lgt;select&gt; tag
     * @return the html for the language select box
     */
    public String buildSelectLanguage(String htmlAttributes) {

        SelectOptions selectOptions = getOptionsForLanguage();
        return buildSelect(htmlAttributes, selectOptions);
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
        Map<String, SortedMap<Float, CmsWorkplaceEditorConfiguration>> resourceEditors = OpenCms.getWorkplaceManager().getWorkplaceEditorManager().getConfigurableEditors();
        if (resourceEditors != null) {
            // first: iterate over the resource types and consider order from configuration
            Iterator<String> i = resourceEditors.keySet().iterator();

            SortedMap<Float, String> rankResources = new TreeMap<Float, String>();
            while (i.hasNext()) {
                String currentResourceType = i.next();
                CmsExplorerTypeSettings settings = OpenCms.getWorkplaceManager().getExplorerTypeSetting(
                    currentResourceType);
                rankResources.put(Float.valueOf(settings.getNewResourceOrder()), currentResourceType);
            }

            while (rankResources.size() > 0) {
                // get editor configuration with lowest order
                Float keyVal = rankResources.firstKey();
                String currentResourceType = rankResources.get(keyVal);

                SortedMap<Float, CmsWorkplaceEditorConfiguration> availableEditors = resourceEditors.get(
                    currentResourceType);
                if ((availableEditors != null) && (availableEditors.size() > 0)) {
                    String preSelection = computeEditorPreselection(request, currentResourceType);
                    List<String> options = new ArrayList<String>(availableEditors.size() + 1);
                    List<String> values = new ArrayList<String>(availableEditors.size() + 1);
                    options.add(key(Messages.GUI_PREF_EDITOR_BEST_0));
                    values.add(INPUT_DEFAULT);
                    // second: iteration over the available editors for the resource type
                    int selectedIndex = 0;
                    int counter = 1;
                    while (availableEditors.size() > 0) {
                        Float key = availableEditors.lastKey();
                        CmsWorkplaceEditorConfiguration conf = availableEditors.get(key);
                        options.add(keyDefault(conf.getEditorLabel(), conf.getEditorLabel()));
                        values.add(conf.getEditorUri());
                        if (conf.getEditorUri().equals(preSelection)) {
                            selectedIndex = counter;
                        }
                        counter++;
                        availableEditors.remove(key);
                    }

                    // create the table row for the current resource type
                    result.append("<tr>\n\t<td style=\"white-space: nowrap;\">");
                    String localizedName = keyDefault("label.editor.preferred." + currentResourceType, "");
                    if (CmsStringUtil.isEmpty(localizedName)) {
                        localizedName = CmsWorkplaceMessages.getResourceTypeName(this, currentResourceType);
                    }
                    result.append(localizedName);
                    result.append("</td>\n\t<td>");
                    result.append(
                        buildSelect(htmlAttributes + currentResourceType + "\"", options, values, selectedIndex));
                    result.append("</td>\n</tr>\n");
                }
                rankResources.remove(keyVal);
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

        SelectOptions selectOptions = getProjectSelectOptions();
        return buildSelect(htmlAttributes, selectOptions);

    }

    /**
     * Builds the html for the default publish siblings mode select box.<p>
     *
     * @param htmlAttributes optional html attributes for the &lgt;select&gt; tag
     * @return the html for the default publish siblings mode select box
     */
    public String buildSelectPublishSiblings(String htmlAttributes) {

        List<String> options = new ArrayList<String>(2);
        options.add(key(Messages.GUI_PREF_PUBLISH_SIBLINGS_0));
        options.add(key(Messages.GUI_PREF_PUBLISH_ONLY_SELECTED_0));
        List<String> values = new ArrayList<String>(2);
        values.add(CmsStringUtil.TRUE);
        values.add(CmsStringUtil.FALSE);
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

        List<String> options = new ArrayList<String>(2);
        options.add(key(Messages.GUI_LABEL_SIMPLE_0));
        options.add(key(Messages.GUI_LABEL_EXTENDED_0));
        String[] vals = new String[] {I_CmsReport.REPORT_TYPE_SIMPLE, I_CmsReport.REPORT_TYPE_EXTENDED};
        List<String> values = new ArrayList<String>(java.util.Arrays.asList(vals));
        int selectedIndex = 0;
        if (I_CmsReport.REPORT_TYPE_EXTENDED.equals(getParamTabWpReportType())) {
            selectedIndex = 1;
        }
        return buildSelect(htmlAttributes, options, values, selectedIndex);
    }

    /**
     * Builds the html for the workplace start site select box.<p>
     *
     * @param htmlAttributes optional html attributes for the &lgt;select&gt; tag
     * @return the html for the workplace start site select box
     */
    public String buildSelectSite(String htmlAttributes) {

        SelectOptions selectOptions = getSiteSelectOptions();

        return buildSelect(htmlAttributes, selectOptions);
    }

    /**
     * Builds the HTML for the start galleries settings as select boxes.<p>
     *
     * @param htmlAttributes optional HTML attributes for the &lgt;select&gt; tag
     * @return the HTML for start galleries select boxes
     */
    public String buildSelectStartGalleries(String htmlAttributes) {

        StringBuffer result = new StringBuffer(1024);
        HttpServletRequest request = getJsp().getRequest();
        // set the attributes for the select tag
        if (htmlAttributes != null) {
            htmlAttributes += " name=\"" + PARAM_STARTGALLERY_PREFIX;
        }
        Map<String, A_CmsAjaxGallery> galleriesTypes = OpenCms.getWorkplaceManager().getGalleries();
        if (galleriesTypes != null) {

            // sort the galleries by localized name
            Map<String, String> localizedGalleries = new TreeMap<String, String>();
            for (Iterator<String> i = galleriesTypes.keySet().iterator(); i.hasNext();) {
                String currentGalleryType = i.next();
                String localizedName = CmsWorkplaceMessages.getResourceTypeName(this, currentGalleryType);
                localizedGalleries.put(localizedName, currentGalleryType);
            }

            for (Iterator<Map.Entry<String, String>> i = localizedGalleries.entrySet().iterator(); i.hasNext();) {
                Map.Entry<String, String> entry = i.next();
                // first: retrieve the gallery type
                String currentGalleryType = entry.getValue();
                // second: retrieve the gallery type id
                int currentGalleryTypeId = 0;
                try {
                    currentGalleryTypeId = OpenCms.getResourceManager().getResourceType(currentGalleryType).getTypeId();
                } catch (CmsLoaderException e) {
                    // resource type not found, log error
                    if (LOG.isErrorEnabled()) {
                        LOG.error(e.getLocalizedMessage(), e);
                    }
                    continue;
                }
                // third: get the available galleries for this gallery type id
                List<CmsResource> availableGalleries = A_CmsAjaxGallery.getGalleries(currentGalleryTypeId, getCms());

                // forth: fill the select box
                List<String> options = new ArrayList<String>(availableGalleries.size() + 2);
                List<String> values = new ArrayList<String>(availableGalleries.size() + 2);
                options.add(key(Messages.GUI_PREF_STARTGALLERY_PRESELECT_0));
                values.add(INPUT_DEFAULT);
                options.add(key(Messages.GUI_PREF_STARTGALLERY_NONE_0));
                values.add(INPUT_NONE);

                String savedValue = computeStartGalleryPreselection(request, currentGalleryType);
                int counter = 2;
                int selectedIndex = 0;
                Iterator<CmsResource> iGalleries = availableGalleries.iterator();
                while (iGalleries.hasNext()) {
                    CmsResource res = iGalleries.next();
                    String rootPath = res.getRootPath();
                    String sitePath = getCms().getSitePath(res);
                    // select the value
                    if ((savedValue != null) && (savedValue.equals(rootPath))) {
                        selectedIndex = counter;
                    }
                    counter++;
                    // gallery title
                    String title = "";
                    try {
                        // read the gallery title
                        title = getCms().readPropertyObject(
                            sitePath,
                            CmsPropertyDefinition.PROPERTY_TITLE,
                            false).getValue("");
                    } catch (CmsException e) {
                        // error reading title property
                        if (LOG.isErrorEnabled()) {
                            LOG.error(e.getLocalizedMessage(), e);
                        }
                    }
                    options.add(title.concat(" (").concat(sitePath).concat(")"));
                    values.add(rootPath);

                }
                // select the value
                if ((savedValue != null) && savedValue.equals(INPUT_NONE)) {
                    selectedIndex = 1;
                }

                // create the table row for the current resource type
                result.append("<tr>\n\t<td style=\"white-space: nowrap;\">");

                result.append(entry.getKey());
                result.append("</td>\n\t<td>");
                result.append(buildSelect(htmlAttributes + currentGalleryType + "\"", options, values, selectedIndex));
                result.append("</td>\n</tr>\n");

            }

        }
        return result.toString();
    }

    /**
     * Builds the html for the workplace start site select box.<p>
     *
     * @param htmlAttributes optional html attributes for the &lgt;select&gt; tag
     * @return the html for the workplace start site select box
     */
    public String buildSelectUpload(String htmlAttributes) {

        List<String> options = new ArrayList<String>();
        List<String> values = new ArrayList<String>();
        int selectedIndex = 0;
        int pos = 0;

        UploadVariant currentVariant = getParamTabWpUploadVariant();
        for (UploadVariant variant : UploadVariant.values()) {

            values.add(variant.toString());
            options.add(getUploadVariantMessage(variant));

            if (variant.equals(currentVariant)) {
                selectedIndex = pos;
            }
            pos++;
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

        SelectOptions optionBean = CmsStartViewPreference.getViewSelectOptions(getCms(), getParamTabWpView());
        return buildSelect(htmlAttributes, optionBean);
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
     * Builds the html for the workplace search result list type select box.<p>
     *
     * @param htmlAttributes optional html attributes for the &lgt;select&gt; tag
     * @return the html for the workplace search result list type select box
     */
    public String buildSelectWorkplaceSearchResult(String htmlAttributes) {

        List<String> options = new ArrayList<String>(3);
        List<String> values = new ArrayList<String>(3);
        int checkedIndex = 0;

        // add all styles to the select box
        options.add(key(CmsSearchResultStyle.STYLE_EXPLORER.getKey()));
        values.add(CmsSearchResultStyle.STYLE_EXPLORER.getMode());
        if (getParamTabExWorkplaceSearchResult().equals(CmsSearchResultStyle.STYLE_EXPLORER.toString())) {
            // mark the currently active locale
            checkedIndex = 0;
        }
        options.add(key(CmsSearchResultStyle.STYLE_LIST_WITH_EXCERPTS.getKey()));
        values.add(CmsSearchResultStyle.STYLE_LIST_WITH_EXCERPTS.getMode());
        if (getParamTabExWorkplaceSearchResult().equals(CmsSearchResultStyle.STYLE_LIST_WITH_EXCERPTS.toString())) {
            // mark the currently active locale
            checkedIndex = 1;
        }
        options.add(key(CmsSearchResultStyle.STYLE_LIST_WITHOUT_EXCERPTS.getKey()));
        values.add(CmsSearchResultStyle.STYLE_LIST_WITHOUT_EXCERPTS.getMode());
        if (getParamTabExWorkplaceSearchResult().equals(CmsSearchResultStyle.STYLE_LIST_WITHOUT_EXCERPTS.toString())) {
            // mark the currently active locale
            checkedIndex = 2;
        }

        return buildSelect(htmlAttributes, options, values, checkedIndex);
    }

    /**
     * Builds the html code for the static user information table (tab 4).<p>
     *
     * @return the html code for the static user information table
     */
    public String buildUserInformation() {

        StringBuffer result = new StringBuffer(512);
        CmsUser user = getSettings().getUser();

        result.append("<table border=\"0\" cellspacing=\"0\" cellpadding=\"4\">\n");
        result.append("<tr>\n");
        result.append("\t<td style=\"width: 25%;\">");
        result.append(key(Messages.GUI_LABEL_USER_0));
        result.append("</td>\n");
        result.append("\t<td class=\"textbold\" style=\"width: 25%;\">");
        result.append(user.getName());
        result.append("</td>\n");
        result.append("\t<td style=\"width: 25%;\">");
        result.append(key(Messages.GUI_LABEL_EMAIL_0));
        result.append("</td>\n");
        result.append("\t<td class=\"textbold\" style=\"width: 25%;\">");
        result.append(user.getEmail());
        result.append("</td>\n");
        result.append("</tr>\n");

        result.append("<tr>\n");
        result.append("\t<td>");
        result.append(key(Messages.GUI_LABEL_LASTNAME_0));
        result.append("</td>\n");
        result.append("\t<td class=\"textbold\">");
        result.append(user.getLastname());
        result.append("</td>\n");
        result.append("\t<td rowspan=\"3\" style=\"vertical-align: top;\">");
        result.append(key(Messages.GUI_INPUT_ADRESS_0));
        result.append("</td>\n");

        String address = user.getAddress();

        result.append("\t<td rowspan=\"3\" class=\"textbold\" style=\"vertical-align: top;\">");
        result.append(address);
        result.append("</td>\n");
        result.append("</tr>\n");

        result.append("<tr>\n");
        result.append("\t<td>");
        result.append(key(Messages.GUI_LABEL_FIRSTNAME_0));
        result.append("</td>\n");
        result.append("\t<td class=\"textbold\">");
        result.append(user.getFirstname());
        result.append("</td>\n");
        result.append("</tr>\n");

        result.append("<tr>\n");
        result.append("\t<td>");
        result.append(key(Messages.GUI_LABEL_DESCRIPTION_0));
        result.append("</td>\n");
        result.append("\t<td class=\"textbold\">");
        result.append(user.getDescription(getLocale()));
        result.append("</td>\n");
        result.append("</tr>\n");
        result.append("</table>\n");

        return result.toString();
    }

    /**
     * Creates the HTML JavaScript and stylesheet includes required by the calendar for the head of the page.<p>
     *
     * @return the necessary HTML code for the js and stylesheet includes
     *
     * @deprecated use {@link CmsCalendarWidget#calendarIncludes(java.util.Locale)}, this is just here so that old JSP still work
     */
    @Deprecated
    public String calendarIncludes() {

        return CmsCalendarWidget.calendarIncludes(getLocale());
    }

    /**
     * Generates the HTML to initialize the JavaScript calendar element on the end of a page.<p>
     *
     * @param inputFieldId the ID of the input field where the date is pasted to
     * @param triggerButtonId the ID of the button which triggers the calendar
     * @param align initial position of the calendar popup element
     * @param singleClick if true, a single click selects a date and closes the calendar, otherwise calendar is closed by doubleclick
     * @param weekNumbers show the week numbers in the calendar or not
     * @param mondayFirst show monday as first day of week
     * @param dateStatusFunc name of the function which determines if/how a date should be disabled
     * @param showTime true if the time selector should be shown, otherwise false
     * @return the HTML code to initialize a calendar poup element
     *
     * @deprecated use {@link CmsCalendarWidget#calendarInit(org.opencms.i18n.CmsMessages, String, String, String, boolean, boolean, boolean, String, boolean)}, this is just here so that old JSP still work
     */
    @Deprecated
    public String calendarInit(
        String inputFieldId,
        String triggerButtonId,
        String align,
        boolean singleClick,
        boolean weekNumbers,
        boolean mondayFirst,
        String dateStatusFunc,
        boolean showTime) {

        return CmsCalendarWidget.calendarInit(
            getMessages(),
            inputFieldId,
            triggerButtonId,
            align,
            singleClick,
            weekNumbers,
            mondayFirst,
            dateStatusFunc,
            showTime);
    }

    /**
     * Gets the select options for the language selector.<p>
     *
     * @return the select options
     */
    public SelectOptions getOptionsForLanguage() {

        return getOptionsForLanguageStatic(getSettings().getUserSettings().getLocale(), m_userSettings.getLocale());
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
     * Returns the old password value. <p>
     *
     * @return the old password value
     */
    public String getParamOldPassword() {

        return m_paramOldPassword;
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
     * Returns the "expand inherited permissions" default setting.<p>
     *
     * @return the "expand inherited permissions" default setting
     */
    public String getParamTabDiPermissionsExpandInherited() {

        return isParamEnabled(m_userSettings.getDialogExpandInheritedPermissions());
    }

    /**
     * Returns the "expand current users permissions" default setting.<p>
     *
     * @return the "expand current users permissions" default setting
     */
    public String getParamTabDiPermissionsExpandUser() {

        return isParamEnabled(m_userSettings.getDialogExpandUserPermissions());
    }

    /**
     * Returns the "inherit permissions on folders" default setting.<p>
     *
     * @return the "inherit permissions on folders" default setting
     */
    public String getParamTabDiPermissionsInheritOnFolder() {

        return isParamEnabled(m_userSettings.getDialogPermissionsInheritOnFolder());
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
     * Returns the "display lock dialog" setting.<p>
     *
     * @return <code>"true"</code> if the "display lock dialog" input field is checked, otherwise ""
     */
    public String getParamTabDiShowLock() {

        return isParamEnabled(m_userSettings.getDialogShowLock());
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
     * @return <code>"true"</code> if the file creation date input field is checked, otherwise ""
     */
    public String getParamTabExFileDateCreated() {

        return isParamEnabled(m_userSettings.showExplorerFileDateCreated());
    }

    /**
     * Returns the "display file date expired" setting.<p>
     *
     * @return <code>"true"</code> if the file date expired input field is checked, otherwise ""
     */
    public String getParamTabExFileDateExpired() {

        return isParamEnabled(m_userSettings.showExplorerFileDateExpired());
    }

    /**
     * Returns the "display file last modification date" setting.<p>
     *
     * @return <code>"true"</code> if the file last modification date input field is checked, otherwise ""
     */
    public String getParamTabExFileDateLastModified() {

        return isParamEnabled(m_userSettings.showExplorerFileDateLastModified());
    }

    /**
     * Returns the "display file date released" setting.<p>
     *
     * @return <code>"true"</code> if the file date released input field is checked, otherwise ""
     */
    public String getParamTabExFileDateReleased() {

        return isParamEnabled(m_userSettings.showExplorerFileDateReleased());
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
     * @return <code>"true"</code> if the file locked by input field is checked, otherwise ""
     */
    public String getParamTabExFileLockedBy() {

        return isParamEnabled(m_userSettings.showExplorerFileLockedBy());
    }

    /**
     * Returns the "display navtext" setting.<p>
     *
     * @return <code>"true"</code> if the file navtext input field is checked, otherwise ""
     */
    public String getParamTabExFileNavText() {

        return isParamEnabled(m_userSettings.showExplorerFileNavText());
    }

    /**
     * Returns the "display file permissions" setting.<p>
     *
     * @return <code>"true"</code> if the file permissions input field is checked, otherwise ""
     */
    public String getParamTabExFilePermissions() {

        return isParamEnabled(m_userSettings.showExplorerFilePermissions());
    }

    /**
     * Returns the "display file size" setting.<p>
     *
     * @return <code>"true"</code> if the file size input field is checked, otherwise ""
     */
    public String getParamTabExFileSize() {

        return isParamEnabled(m_userSettings.showExplorerFileSize());
    }

    /**
     * Returns the "display file state" setting.<p>
     *
     * @return <code>"true"</code> if the file state input field is checked, otherwise ""
     */
    public String getParamTabExFileState() {

        return isParamEnabled(m_userSettings.showExplorerFileState());
    }

    /**
     * Returns the "display file title" setting.<p>
     *
     * @return <code>"true"</code> if the file title input field is checked, otherwise ""
     */
    public String getParamTabExFileTitle() {

        return isParamEnabled(m_userSettings.showExplorerFileTitle());
    }

    /**
     * Returns the "display file type" setting.<p>
     *
     * @return <code>"true"</code> if the file type input field is checked, otherwise ""
     */
    public String getParamTabExFileType() {

        return isParamEnabled(m_userSettings.showExplorerFileType());
    }

    /**
     * Returns the "display file created by" setting.<p>
     *
     * @return <code>"true"</code> if the file created by input field is checked, otherwise ""
     */
    public String getParamTabExFileUserCreated() {

        return isParamEnabled(m_userSettings.showExplorerFileUserCreated());
    }

    /**
     * Returns the "display file last modified by" setting.<p>
     *
     * @return <code>"true"</code> if the file last modified by input field is checked, otherwise ""
     */
    public String getParamTabExFileUserLastModified() {

        return isParamEnabled(m_userSettings.showExplorerFileUserLastModified());
    }

    /**
     * Returns the "workplace search result style" setting.<p>
     *
     * @return the "workplace search result style" setting
     */
    public String getParamTabExWorkplaceSearchResult() {

        return m_userSettings.getWorkplaceSearchViewStyle().toString();
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
     * Returns the "start folder" setting.<p>
     *
     * @return the "start folder" setting
     */
    public String getParamTabWpFolder() {

        return m_userSettings.getStartFolder();
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
     * Returns the "list all projects" setting.<p>
     *
     * @return <code>"true"</code> if the "list all projects" input is checked, otherwise ""
     */
    public String getParamTabWpListAllProjects() {

        return isParamEnabled(m_userSettings.getListAllProjects());
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
     * Returns the "workplace report type" setting.<p>
     *
     * @return the "workplace report type" setting
     */
    public String getParamTabWpReportType() {

        return m_userSettings.getWorkplaceReportType();
    }

    /**
     * Returns the "workplace restrict explorer view" setting.<p>
     *
     * @return the "workplace restrict explorer view" setting
     */
    public String getParamTabWpRestrictExplorerView() {

        return "" + m_userSettings.getRestrictExplorerView();
    }

    /**
     * Returns the "show publish notification" setting.<p>
     *
     * @return <code>"true"</code> if the "show publish notification" input is checked, otherwise ""
     */
    public String getParamTabWpShowPublishNotification() {

        return isParamEnabled(m_userSettings.getShowPublishNotification());
    }

    /**
     * Returns the "start site" setting.<p>
     *
     * @return the "start site" setting
     */
    public String getParamTabWpSite() {

        return m_userSettings.getStartSite();
    }

    /**
     * Get the "user timewparp" setting in form of a formatted date string.<p>
     *
     * If no timewarp has been chosen, a value "-" will be returned.<p>
     *
     * @return the "user timewarp" setting in form of a formatted date string
     */
    public String getParamTabWpTimeWarp() {

        String result;
        if (m_userSettings.getTimeWarp() == CmsContextInfo.CURRENT_TIME) {
            result = "-";
        } else {
            result = CmsCalendarWidget.getCalendarLocalizedTime(
                getLocale(),
                getMessages(),
                m_userSettings.getTimeWarp());
        }
        return result;
    }

    /**
     * Returns the upload variant setting.<p>
     *
     * @return <code>"applet"</code>, <code>"gwt"</code> or <code>"basic"</code>
     */
    public UploadVariant getParamTabWpUploadVariant() {

        return m_userSettings.getUploadVariant();
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
     * Gets the project select options.<p>
     *
     * @return the project select options
     */
    public SelectOptions getProjectSelectOptions() {

        return getProjectSelectOptionsStatic(getCms(), m_userSettings.getStartProject(), getLocale());
    }

    /**
     * Gets the site select options.<p>
     *
     * @return the site select options
     */
    public SelectOptions getSiteSelectOptions() {

        return getSiteSelectOptionsStatic(
            getCms(),
            CmsWorkplace.getStartSiteRoot(getCms(), m_userSettings),
            getSettings().getUserSettings().getLocale());
    }

    /**
     * @see org.opencms.workplace.CmsTabDialog#getTabParameterOrder()
     */
    @Override
    public List<String> getTabParameterOrder() {

        ArrayList<String> orderList = new ArrayList<String>(5);
        orderList.add("tabwp");
        orderList.add("tabex");
        orderList.add("tabdi");
        orderList.add("tabed");
        orderList.add("tabga");
        orderList.add("tabup");
        return orderList;
    }

    /**
     * @see org.opencms.workplace.CmsTabDialog#getTabs()
     */
    @Override
    public List<String> getTabs() {

        ArrayList<String> tabList = new ArrayList<String>(6);
        tabList.add(key(Messages.GUI_PREF_PANEL_WORKPLACE_0));
        tabList.add(key(Messages.GUI_PREF_PANEL_EXPLORER_0));
        tabList.add(key(Messages.GUI_PREF_PANEL_DIALOGS_0));
        tabList.add(key(Messages.GUI_PREF_PANEL_EDITORS_0));
        tabList.add(key(Messages.GUI_PREF_PANEL_GALLERIES_0));
        tabList.add(key(Messages.GUI_PREF_PANEL_USER_0));
        return tabList;
    }

    /**
     * Gets the timewarp parameter as a simple numeric string.<p>
     *
     * @return the timewarp parameter as a simple numeric string
     */
    public String getTimeWarpInt() {

        return "" + m_userSettings.getTimeWarp();
    }

    /**
     * Gets the internal user settings object.<p>
     *
     * @return the user settings object
     */
    public CmsUserSettings getUserSettings() {

        return m_userSettings;
    }

    /**
     * Helper method to add the "checked" attribute to an input field.<p>
     *
     * @param paramValue the parameter value, if <code>"true"</code>, the "checked" attribute will be returned
     * @return the "checked" attribute or an empty String
     */
    public String isChecked(String paramValue) {

        if (Boolean.valueOf(paramValue).booleanValue()) {
            return " checked=\"checked\"";
        }
        return "";
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
     * Sets the "copy file default" setting.<p>
     *
     * @param value the "copy file default" setting
     */
    public void setParamTabDiCopyFileMode(String value) {

        try {
            m_userSettings.setDialogCopyFileMode(CmsResourceCopyMode.valueOf(Integer.parseInt(value)));
        } catch (Throwable t) {
            // should usually never happen
        }
    }

    /**
     * Sets the "copy folder default" setting.<p>
     *
     * @param value the "copy folder default" setting
     */
    public void setParamTabDiCopyFolderMode(String value) {

        try {
            m_userSettings.setDialogCopyFolderMode(CmsResourceCopyMode.valueOf(Integer.parseInt(value)));
        } catch (Throwable t) {
            // should usually never happen
        }
    }

    /**
     * Sets the "delete file siblings default" setting.<p>
     *
     * @param value the "delete file siblings default" setting
     */
    public void setParamTabDiDeleteFileMode(String value) {

        try {
            m_userSettings.setDialogDeleteFileMode(CmsResourceDeleteMode.valueOf(Integer.parseInt(value)));
        } catch (Throwable t) {
            // should usually never happen
        }
    }

    /**
     * Sets the "expand inherited permissions" default setting.<p>
     *
     * @param value the "expand inherited permissions" default setting
     */
    public void setParamTabDiPermissionsExpandInherited(String value) {

        m_userSettings.setDialogExpandInheritedPermissions(Boolean.valueOf(value).booleanValue());
    }

    /**
     * Sets the "expand current users permissions" default setting.<p>
     *
     * @param value the "expand current users permissions" default setting
     */
    public void setParamTabDiPermissionsExpandUser(String value) {

        m_userSettings.setDialogExpandUserPermissions(Boolean.valueOf(value).booleanValue());
    }

    /**
     * Sets the "inherit permissions on folders" default setting.<p>
     *
     * @param value the "inherit permissions on folders" default setting
     */
    public void setParamTabDiPermissionsInheritOnFolder(String value) {

        m_userSettings.setDialogPermissionsInheritOnFolder(Boolean.valueOf(value).booleanValue());
    }

    /**
     * Sets the "publish file siblings default" setting.<p>
     *
     * @param value the "publish file siblings default" setting
     */
    public void setParamTabDiPublishFileMode(String value) {

        m_userSettings.setDialogPublishSiblings(Boolean.valueOf(value).booleanValue());
    }

    /**
     * Sets the "display lock dialog" setting.<p>
     *
     * @param value <code>"true"</code> to enable the "display lock dialog" setting, all others to disable
     */
    public void setParamTabDiShowLock(String value) {

        m_userSettings.setDialogShowLock(Boolean.valueOf(value).booleanValue());
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
            // should usually never happen
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
            // should usually never happen
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
            // should usually never happen
        }
    }

    /**
     * Sets the "display file creation date" setting.<p>
     *
     * @param value <code>"true"</code> to enable the "display file creation date" setting, all others to disable
     */
    public void setParamTabExFileDateCreated(String value) {

        m_userSettings.setShowExplorerFileDateCreated(Boolean.valueOf(value).booleanValue());
    }

    /**
     * Sets the "display file expired date" setting.<p>
     *
     * @param value <code>"true"</code> to enable the "display file expired date" setting, all others to disable
     */
    public void setParamTabExFileDateExpired(String value) {

        m_userSettings.setShowExplorerFileDateExpired(Boolean.valueOf(value).booleanValue());
    }

    /**
     * Sets the "display file last modification date" setting.<p>
     *
     * @param value <code>"true"</code> to enable the "display file last modification date" setting, all others to disable
     */
    public void setParamTabExFileDateLastModified(String value) {

        m_userSettings.setShowExplorerFileDateLastModified(Boolean.valueOf(value).booleanValue());
    }

    /**
     * Sets the "display file released date" setting.<p>
     *
     * @param value <code>"true"</code> to enable the "display file released date" setting, all others to disable
     */
    public void setParamTabExFileDateReleased(String value) {

        m_userSettings.setShowExplorerFileDateReleased(Boolean.valueOf(value).booleanValue());
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
            // should usually never happen
        }
    }

    /**
     * Sets the "display file locked by" setting.<p>
     *
     * @param value <code>"true"</code> to enable the "display file locked by" setting, all others to disable
     */
    public void setParamTabExFileLockedBy(String value) {

        m_userSettings.setShowExplorerFileLockedBy(Boolean.valueOf(value).booleanValue());
    }

    /**
     * Sets the "display file navtext" setting.<p>
     *
     * @param value <code>"true"</code> to enable the "display file navtext" setting, all others to disable
     */
    public void setParamTabExFileNavText(String value) {

        m_userSettings.setShowExplorerFileNavText(Boolean.valueOf(value).booleanValue());
    }

    /**
     * Sets the "display file permissions" setting.<p>
     *
     * @param value <code>"true"</code> to enable the "display file permissions" setting, all others to disable
     */
    public void setParamTabExFilePermissions(String value) {

        m_userSettings.setShowExplorerFilePermissions(Boolean.valueOf(value).booleanValue());
    }

    /**
     * Sets the "display file size" setting.<p>
     *
     * @param value <code>"true"</code> to enable the "display file size" setting, all others to disable
     */
    public void setParamTabExFileSize(String value) {

        m_userSettings.setShowExplorerFileSize(Boolean.valueOf(value).booleanValue());
    }

    /**
     * Sets the "display file state" setting.<p>
     *
     * @param value <code>"true"</code> to enable the "display file state" setting, all others to disable
     */
    public void setParamTabExFileState(String value) {

        m_userSettings.setShowExplorerFileState(Boolean.valueOf(value).booleanValue());
    }

    /**
     * Sets the "display file title" setting.<p>
     *
     * @param value <code>"true"</code> to enable the "display file title" setting, all others to disable
     */
    public void setParamTabExFileTitle(String value) {

        m_userSettings.setShowExplorerFileTitle(Boolean.valueOf(value).booleanValue());
    }

    /**
     * Sets the "display file type" setting.<p>
     *
     * @param value <code>"true"</code> to enable the "display file type" setting, all others to disable
     */
    public void setParamTabExFileType(String value) {

        m_userSettings.setShowExplorerFileType(Boolean.valueOf(value).booleanValue());
    }

    /**
     * Sets the "display file created by" setting.<p>
     *
     * @param value <code>"true"</code> to enable the "display file created by" setting, all others to disable
     */
    public void setParamTabExFileUserCreated(String value) {

        m_userSettings.setShowExplorerFileUserCreated(Boolean.valueOf(value).booleanValue());
    }

    /**
     * Sets the "display file last modified by" setting.<p>
     *
     * @param value <code>"true"</code> to enable the "display file last modified by" setting, all others to disable
     */
    public void setParamTabExFileUserLastModified(String value) {

        m_userSettings.setShowExplorerFileUserLastModified(Boolean.valueOf(value).booleanValue());
    }

    /**
     * Sets the "workplace search result style".<p>
     *
     * @param style the "workplace search result style" to set
     */
    public void setParamTabExWorkplaceSearchResult(String style) {

        if (style == null) {
            style = OpenCms.getWorkplaceManager().getDefaultUserSettings().getWorkplaceSearchViewStyle().getMode();
        }
        m_userSettings.setWorkplaceSearchViewStyle(CmsSearchResultStyle.valueOf(style));
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
            // should usually never happen
        }
    }

    /**
     * Sets the "start folder" setting.<p>
     *
     * @param value the start folder to show in the explorer view
     */
    public void setParamTabWpFolder(String value) {

        // perform self - healing
        if (!getCms().existsResource(value, CmsResourceFilter.IGNORE_EXPIRATION)) {
            value = "/";
        }
        m_userSettings.setStartFolder(value);
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
     * Sets the "list all projects" flag.<p>
     *
     * @param value <code>"true"</code> to enable the "list all project" feature, all others to
     *        disable
     */
    public void setParamTabWpListAllProjects(String value) {

        m_userSettings.setListAllProjects(Boolean.valueOf(value).booleanValue());
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
     * Sets the "workplace report type" setting.<p>
     *
     * @param value the "workplace report type" setting
     */
    public void setParamTabWpReportType(String value) {

        if (I_CmsReport.REPORT_TYPE_SIMPLE.equals(value) || I_CmsReport.REPORT_TYPE_EXTENDED.equals(value)) {
            // set only if valid parameter value is found
            m_userSettings.setWorkplaceReportType(value);
        }
    }

    /**
     * Sets the "workplace restrict explorer view" setting.<p>
     *
     * @param value the "workplace restrict explorer view" setting
     */
    public void setParamTabWpRestrictExplorerView(String value) {

        m_userSettings.setRestrictExplorerView(Boolean.valueOf(value).booleanValue());
    }

    /**
     * Sets the "show publish notification" setting.<p>
     *
     * @param value <code>"true"</code> to enable the "show publish notification" setting, all others to
     *        disable
     */
    public void setParamTabWpShowPublishNotification(String value) {

        m_userSettings.setShowPublishNotification(Boolean.valueOf(value).booleanValue());
    }

    /**
     * Sets the "start site" setting.<p>
     *
     * @param value the start site to show in the explorer view
     */
    public void setParamTabWpSite(String value) {

        m_userSettings.setStartSite(value);
    }

    /**
     * Sets the "user timewparp" setting.<p>
     *
     * To delete a timewarp setting for the current user, provide <code>"-"</code> as value.<p>
     *
     * @param value a String representation of an date in the formate as required by
     *      {@link CmsCalendarWidget#getCalendarDate(org.opencms.i18n.CmsMessages, String, boolean)}
     */
    public void setParamTabWpTimeWarp(String value) {

        long datetimestamp = CmsContextInfo.CURRENT_TIME;
        // check for "delete value"
        if (CmsStringUtil.isNotEmpty(value) && !"-".equals(value)) {
            try {
                datetimestamp = CmsCalendarWidget.getCalendarDate(getMessages(), value, true);
            } catch (Exception e) {
                // reset timewarp setting in case of exception
            }
        }
        m_userSettings.setTimeWarp(datetimestamp);
    }

    /**
     * Sets the upload variant setting.<p>
     *
     * @param value <code>"applet"</code>, <code>"basic"</code>,
     * <code>"gwt"</code>, <code>"true"</code> or <code>"false"</code>
     */
    public void setParamTabWpUploadVariant(String value) {

        m_userSettings.setUploadVariant(value);
    }

    /**
     * Sets the start view setting.<p>
     *
     * @param value the start view setting
     */
    public void setParamTabWpView(String value) {

        m_userSettings.setStartView(value);
    }

    /**
     * Sets the timewarp setting from a numeric string
     *
     * @param timewarp a numeric string containing the number of milliseconds since the epoch
     */
    public void setTimewarpInt(String timewarp) {

        try {
            m_userSettings.setTimeWarp(Long.valueOf(timewarp).longValue());
        } catch (Exception e) {
            m_userSettings.setTimeWarp(-1);
        }
    }

    /**
     * Sets the  user settings.<p>
     *
     * @param userSettings the user settings
     */
    public void setUserSettings(CmsDefaultUserSettings userSettings) {

        m_userSettings = userSettings;
    }

    /**
     * Updates the user preferences after changes have been made.<p>
     *
     * @param cms the current cms context
     * @param req the current http request
     */
    public void updatePreferences(CmsObject cms, HttpServletRequest req) {

        HttpSession session = req.getSession(false);
        if (session == null) {
            return;
        }
        CmsWorkplaceSettings settings = (CmsWorkplaceSettings)session.getAttribute(
            CmsWorkplaceManager.SESSION_WORKPLACE_SETTINGS);
        if (settings == null) {
            return;
        }
        // keep old synchronize settings
        CmsSynchronizeSettings synchronizeSettings = settings.getUserSettings().getSynchronizeSettings();
        settings = CmsWorkplace.initWorkplaceSettings(cms, settings, true);
        settings.getUserSettings().setSynchronizeSettings(synchronizeSettings);
    }

    /**
     * @see org.opencms.workplace.CmsWorkplace#checkRole()
     */
    @Override
    protected void checkRole() {

        // this class is used internally for the new preferences dialog, which can also be used by non-workplace users.
    }

    /**
     * @see org.opencms.workplace.CmsWorkplace#initWorkplaceRequestValues(org.opencms.workplace.CmsWorkplaceSettings, javax.servlet.http.HttpServletRequest)
     */
    @Override
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
            setParamTitle(key(Messages.GUI_PREF_0));
        }

    }

    /**
     * Returns the values of all parameter methods of this workplace class instance.<p>
     *
     * This overwrites the super method because of the possible dynamic editor selection entries.<p>
     *
     * @return the values of all parameter methods of this workplace class instance
     *
     * @see org.opencms.workplace.CmsWorkplace#paramValues()
     */
    @Override
    protected Map<String, Object> paramValues() {

        Map<String, Object> map = super.paramValues();
        HttpServletRequest request = getJsp().getRequest();
        Enumeration<?> en = request.getParameterNames();
        while (en.hasMoreElements()) {
            String paramName = (String)en.nextElement();
            if (paramName.startsWith(PARAM_PREFERREDEDITOR_PREFIX) || paramName.startsWith(PARAM_STARTGALLERY_PREFIX)) {
                String paramValue = request.getParameter(paramName);
                if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(paramValue)) {
                    map.put(paramName, CmsEncoder.decode(paramValue));
                }
            }
        }
        return map;
    }

    /**
     * Builds the HTML code for a select widget given a bean containing the select options
     *
     * @param htmlAttributes html attributes for the select widget
     * @param options the bean containing the select options
     *
     * @return the HTML for the select box
     */
    String buildSelect(String htmlAttributes, SelectOptions options) {

        return buildSelect(htmlAttributes, options.getOptions(), options.getValues(), options.getSelectedIndex());
    }

    /**
     * Builds the html for a common button style select box.<p>
     *
     * @param htmlAttributes optional html attributes for the &lgt;select&gt; tag
     * @param selectedIndex the index of the selected option
     * @return the html for the common button style select box
     */
    private String buildSelectButtonStyle(String htmlAttributes, int selectedIndex) {

        List<String> options = new ArrayList<String>(3);
        options.add(key(Messages.GUI_PREF_BUTTONSTYLE_IMG_0));
        options.add(key(Messages.GUI_PREF_BUTTONSTYLE_IMGTXT_0));
        options.add(key(Messages.GUI_PREF_BUTTONSTYLE_TXT_0));
        String[] vals = new String[] {"0", "1", "2"};
        List<String> values = new ArrayList<String>(java.util.Arrays.asList(vals));
        return buildSelect(htmlAttributes, options, values, selectedIndex);
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
        if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(preSelection)) {
            return CmsEncoder.decode(preSelection);
        } else {
            // no value found in request, check current user settings (not the member!)
            CmsUserSettings userSettings = new CmsUserSettings(getSettings().getUser());
            return userSettings.getPreferredEditor(resourceType);

        }
    }

    /**
     * Returns the preferred editor preselection value either from the request, if not present, from the user settings.<p>
     *
     * @param request the current http servlet request
     * @param galleryType the preferred gallery type
     * @return the preferred editor preselection value or null, if none found
     */
    private String computeStartGalleryPreselection(HttpServletRequest request, String galleryType) {

        // first check presence of the setting in request parameter
        String preSelection = request.getParameter(PARAM_STARTGALLERY_PREFIX + galleryType);
        if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(preSelection)) {
            return CmsEncoder.decode(preSelection);
        } else {
            // no value found in request, check current user settings (not the member!)
            CmsUserSettings userSettings = new CmsUserSettings(getSettings().getUser());
            return userSettings.getStartGallery(galleryType);

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
     * Returns the message for a given upload variant.<p>
     *
     * @param variant the variant to get the message for
     *
     * @return the message
     */
    private String getUploadVariantMessage(UploadVariant variant) {

        String message = null;
        switch (variant) {
            case basic:
                message = key(Messages.GUI_PREF_USE_UPLOAD_BASIC_0);
                break;
            case gwt:
                message = key(Messages.GUI_PREF_USE_UPLOAD_GWT_0);
                break;
            default:
                message = key(Messages.ERR_PREF_UPLOAD_VARIANT_NOT_FOUND_0);
                break;
        }
        return message;
    }

    /**
     * Helper method for the request parameter methods to return a String depending on the boolean parameter.<p>
     *
     * @param isEnabled the boolean variable to check
     * @return <code>"true"</code> if isEnabled is true, otherwise ""
     */
    private String isParamEnabled(boolean isEnabled) {

        if (isEnabled) {
            return CmsStringUtil.TRUE;
        }
        return "";
    }
}
