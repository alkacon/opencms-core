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

package org.opencms.workplace;

import org.opencms.configuration.CmsDefaultUserSettings;
import org.opencms.db.CmsUserSettings;
import org.opencms.file.CmsProject;
import org.opencms.file.CmsResourceFilter;
import org.opencms.i18n.CmsEncoder;
import org.opencms.jsp.CmsJspActionElement;
import org.opencms.main.CmsException;
import org.opencms.main.CmsLog;
import org.opencms.main.OpenCms;
import org.opencms.security.CmsOrganizationalUnit;
import org.opencms.site.CmsSite;
import org.opencms.synchronize.CmsSynchronizeSettings;
import org.opencms.util.CmsFileUtil;
import org.opencms.util.CmsRequestUtil;
import org.opencms.util.CmsStringUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.commons.logging.Log;

/**
 * Provides methods for building the main framesets of the OpenCms Workplace.<p>
 *
 * The following files use this class:
 * <ul>
 * <li>/views/top.html
 * <li>/views/top_foot.html
 * <li>/views/top_head.html
 * </ul>
 * <p>
 *
 * @since 6.0.0
 */
public class CmsFrameset extends CmsWorkplace {

    /** Path to the JSP workplace frame loader file. */
    public static final String JSP_WORKPLACE_URI = CmsWorkplace.JSP_WORKPLACE_URI;

    /** The request parameter for the selection of the frame. */
    public static final String PARAM_WP_FRAME = "wpFrame";

    /** The request parameter for the workplace start selection. */
    public static final String PARAM_WP_START = "wpStart";

    /** The request parameter for the workplace view selection. */
    public static final String PARAM_WP_VIEW = "wpView";

    /** The names of the supported frames. */
    private static final String[] FRAMES = {"top", "head", "body", "foot"};

    /** The names of the supported frames in a list. */
    public static final List<String> FRAMES_LIST = Collections.unmodifiableList(Arrays.asList(FRAMES));

    /** The log object for this class. */
    private static final Log LOG = CmsLog.getLog(CmsFrameset.class);

    /** Indicates if a reload of the main body frame is required. */
    private boolean m_reloadRequired;

    /**
     * Public constructor.<p>
     *
     * @param jsp an initialized JSP action element
     */
    public CmsFrameset(CmsJspActionElement jsp) {

        super(jsp);
    }

    /**
     * Performs additional filtering on the list of projects for the project selector.<p>
     *
     * @param projects the original project list
     *
     * @return the filtered project list
     */
    public List<CmsProject> filterProjectsForSelector(List<CmsProject> projects) {

        List<CmsProject> result = new ArrayList<CmsProject>();
        for (CmsProject project : projects) {
            if (!project.isHiddenFromSelector()) {
                result.add(project);
            }
        }
        return result;
    }

    /**
     * Returns the javascript code for the broadcast message alert in the foot of the workplace.<p>
     *
     * @return javascript code showing an alert box when the foot load
     */
    public String getBroadcastMessage() {

        StringBuffer result = new StringBuffer(512);
        String message = getBroadcastMessageString();

        if (CmsStringUtil.isNotEmpty(message)) {
            // create a javascript alert for the message
            result.append("\n<script type=\"text/javascript\">\n<!--\n");
            // the timeout gives the frameset enough time to load before the alert is shown
            result.append("function showMessage() {\n");
            result.append("\talert(decodeURIComponent(\"");
            // the user has pending messages, display them all
            result.append(CmsEncoder.escapeWBlanks(message, CmsEncoder.ENCODING_UTF_8));
            result.append("\"));\n}\n");
            result.append("setTimeout('showMessage();', 2000);");
            result.append("\n//-->\n</script>");
        }
        return result.toString();
    }

    /**
     * Returns the remote ip address of the current user.<p>
     *
     * @return the remote ip address of the current user
     */
    public String getLoginAddress() {

        return getCms().getRequestContext().getRemoteAddress();
    }

    /**
     * Returns the last login time of the current user in localized format.<p>
     *
     * @return the last login time of the current user in localized format
     */
    public String getLoginTime() {

        return getMessages().getDateTime(getSettings().getUser().getLastlogin());
    }

    /**
     * Returns the html for the "preferences" button depending on the current users permissions and
     * the default workplace settings.<p>
     *
     * @return the html for the "preferences" button
     */
    public String getPreferencesButton() {

        int buttonStyle = getSettings().getUserSettings().getWorkplaceButtonStyle();
        if (!getCms().getRequestContext().getCurrentUser().isManaged()) {
            return button(
                "../commons/preferences.jsp",
                "body",
                "preferences.png",
                Messages.GUI_BUTTON_PREFERENCES_0,
                buttonStyle);
        } else {
            return button(null, null, "preferences_in.png", Messages.GUI_BUTTON_PREFERENCES_0, buttonStyle);
        }
    }

    /**
     * Returns a html select box filled with the current users accessible projects.<p>
     *
     * @param htmlAttributes attributes that will be inserted into the generated html
     * @param htmlWidth additional style attributes containing width information
     * @return a html select box filled with the current users accessible projects
     */
    public String getProjectSelect(String htmlAttributes, String htmlWidth) {

        // get all project information
        List<CmsProject> allProjects;
        try {
            String ouFqn = "";
            CmsUserSettings settings = new CmsUserSettings(getCms());
            if (!settings.getListAllProjects()) {
                ouFqn = getCms().getRequestContext().getCurrentUser().getOuFqn();
            }
            allProjects = OpenCms.getOrgUnitManager().getAllAccessibleProjects(
                getCms(),
                ouFqn,
                settings.getListAllProjects());
        } catch (CmsException e) {
            // should usually never happen
            if (LOG.isErrorEnabled()) {
                LOG.error(e.getLocalizedMessage(), e);
            }
            allProjects = Collections.emptyList();
        }
        allProjects = filterProjectsForSelector(allProjects);

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

        List<String> options = new ArrayList<String>();
        List<String> values = new ArrayList<String>();
        int selectedIndex = -1;
        int ouDefaultProjIndex = -1;

        CmsOrganizationalUnit ou = null;
        try {
            ou = OpenCms.getOrgUnitManager().readOrganizationalUnit(getCms(), getCms().getRequestContext().getOuFqn());
        } catch (CmsException e) {
            // should never happen, ignore
        }

        // now loop through all projects and fill the result vectors
        for (int i = 0, n = allProjects.size(); i < n; i++) {
            CmsProject project = allProjects.get(i);
            String projectId = project.getUuid().toString();
            String projectName = project.getSimpleName();
            if (!singleOu && !project.isOnlineProject()) {
                try {
                    projectName = projectName
                        + " - "
                        + OpenCms.getOrgUnitManager().readOrganizationalUnit(
                            getCms(),
                            project.getOuFqn()).getDisplayName(getLocale());
                } catch (CmsException e) {
                    projectName = projectName + " - " + project.getOuFqn();
                }
            }

            values.add(projectId);
            options.add(projectName);

            if (project.getUuid().equals(getSettings().getProject())) {
                // this is the user's current project
                selectedIndex = i;
            }
            if ((ou != null) && project.getUuid().equals(ou.getProjectId())) {
                ouDefaultProjIndex = i;
            }
        }
        if (selectedIndex == -1) {
            if (ouDefaultProjIndex == -1) {
                selectedIndex = 0;
            } else {
                selectedIndex = ouDefaultProjIndex;
            }
        }
        if (CmsStringUtil.isNotEmpty(htmlWidth)) {
            StringBuffer buf = new StringBuffer(htmlAttributes.length() + htmlWidth.length() + 2);
            buf.append(htmlAttributes);
            buf.append(" ");
            buf.append(htmlWidth);
            htmlAttributes = buf.toString();
        }

        return buildSelect(htmlAttributes, options, values, selectedIndex);
    }

    /**
     * Returns the html for the "publish project" button depending on the current users permissions and the default
     * workplace settings.<p>
     *
     * @return the html for the "publish project" button
     */
    public String getPublishButton() {

        String publishButton = OpenCms.getWorkplaceManager().getDefaultUserSettings().getPublishButtonAppearance();
        if (CmsDefaultUserSettings.PUBLISHBUTTON_SHOW_NEVER.equals(publishButton)) {
            return "";
        }

        int buttonStyle = getSettings().getUserSettings().getWorkplaceButtonStyle();

        if (CmsDefaultUserSettings.PUBLISHBUTTON_SHOW_AUTO.equals(publishButton)) {
            if (getCms().isManagerOfProject()) {
                return button(
                    "../../workplace/commons/publish_project.jsp",
                    "body",
                    "publish.png",
                    Messages.GUI_BUTTON_PUBLISH_0,
                    buttonStyle);
            } else {
                return "";
            }
        }

        if (getCms().isManagerOfProject()) {
            return (button(
                "../../workplace/commons/publish_project.jsp",
                "body",
                "publish.png",
                Messages.GUI_BUTTON_PUBLISH_0,
                buttonStyle));
        } else {
            return (button(null, null, "publish_in.png", Messages.GUI_BUTTON_PUBLISH_0, buttonStyle));
        }
    }

    /**
     * Returns the html for the "publish queue" button.<p>
     *
     * @return the html for the "publish queue" button
     */
    public String getPublishQueueButton() {

        int buttonStyle = getSettings().getUserSettings().getWorkplaceButtonStyle();
        StringBuffer js = new StringBuffer(128);
        js.append("javascript:if (parent.body.admin_content && parent.body.admin_menu) {");
        js.append("parent.body.location.href = '");
        js.append(getJsp().link("/system/workplace/views/admin/admin-fs.jsp?root=admin&path=/publishqueue"));
        js.append("';");
        js.append("} else {");
        js.append("parent.body.explorer_body.explorer_files.location.href = '");
        js.append(getJsp().link("/system/workplace/views/admin/admin-fs.jsp?root=explorer&path=/publishqueue&menu=no"));
        js.append("';");
        js.append("};");
        return button(js.toString(), null, "publish_queue.png", Messages.GUI_BUTTON_PUBLISHQUEUE_0, buttonStyle);
    }

    /**
     * Returns a html select box filled with the current users accessible sites.<p>
     *
     * @param htmlAttributes attributes that will be inserted into the generated html
     * @return a html select box filled with the current users accessible sites
     */
    public String getSiteSelect(String htmlAttributes) {

        List<String> options = new ArrayList<String>();
        List<String> values = new ArrayList<String>();
        int selectedIndex = 0;

        List<CmsSite> sites = OpenCms.getSiteManager().getAvailableSites(getCms(), true);

        Iterator<CmsSite> i = sites.iterator();
        int pos = 0;
        while (i.hasNext()) {
            CmsSite site = i.next();
            values.add(site.getSiteRoot());
            options.add(substituteSiteTitle(site.getTitle()));
            String siteRoot = CmsFileUtil.addTrailingSeparator(site.getSiteRoot());
            String settingsSiteRoot = getSettings().getSite();
            if (settingsSiteRoot != null) {
                settingsSiteRoot = CmsFileUtil.addTrailingSeparator(settingsSiteRoot);
            }
            if (siteRoot.equals(settingsSiteRoot)) {
                // this is the user's current site
                selectedIndex = pos;
            }
            pos++;
        }

        return buildSelect(htmlAttributes, options, values, selectedIndex);
    }

    /**
     * Returns the startup URI for display in the main body frame, this can
     * either be the user default view, or (if set) a specific startup resource.<p>
     *
     * @return the startup URI for display in the main body frame
     */
    public String getStartupUri() {

        String result = getSettings().getViewStartup();
        if (result == null) {
            // no specific startup URI is set, use view from user settings
            result = getSettings().getViewUri();
        } else {
            // reset the startup URI, so that it is not displayed again on reload of the frameset
            getSettings().setViewStartup(null);
        }
        // add eventual request parameters to startup uri
        if (getJsp().getRequest().getParameterMap().size() > 0) {
            @SuppressWarnings("unchecked")
            Set<Entry<String, String[]>> params = getJsp().getRequest().getParameterMap().entrySet();
            Iterator<Entry<String, String[]>> i = params.iterator();
            while (i.hasNext()) {
                Entry<?, ?> entry = i.next();
                result = CmsRequestUtil.appendParameter(
                    result,
                    (String)entry.getKey(),
                    ((String[])entry.getValue())[0]);
            }
        }
        // append the frame name to the startup uri
        return CmsRequestUtil.appendParameter(result, CmsFrameset.PARAM_WP_FRAME, FRAMES[2]);
    }

    /**
     * Returns a html select box filled with the views accessible by the current user.<p>
     *
     * @param htmlAttributes attributes that will be inserted into the generated html
     * @return a html select box filled with the views accessible by the current user
     */
    public String getViewSelect(String htmlAttributes) {

        List<String> options = new ArrayList<String>();
        List<String> values = new ArrayList<String>();
        int selectedIndex = 0;

        // loop through the vectors and fill the result vectors
        Iterator<CmsWorkplaceView> i = OpenCms.getWorkplaceManager().getViews().iterator();
        int count = -1;
        String currentView = getSettings().getViewUri();
        if (CmsStringUtil.isNotEmpty(currentView)) {
            // remove possible parameters from current view
            int pos = currentView.indexOf('?');
            if (pos >= 0) {
                currentView = currentView.substring(0, pos);
            }
        }
        while (i.hasNext()) {
            CmsWorkplaceView view = i.next();
            if (getCms().existsResource(view.getUri(), CmsResourceFilter.ONLY_VISIBLE_NO_DELETED)) {
                count++;
                // ensure the current user has +v+r permissions on the view
                String loopLink = getJsp().link(view.getUri());
                String localizedKey = resolveMacros(view.getKey());
                options.add(localizedKey);
                values.add(loopLink);

                if (loopLink.equals(currentView)) {
                    selectedIndex = count;
                }
            }
        }

        return buildSelect(htmlAttributes, options, values, selectedIndex);
    }

    /**
     * Returns the reload URI for the OpenCms workplace.<p>
     *
     * @return the reload URI for the OpenCms workplace
     */
    public String getWorkplaceReloadUri() {

        return getJsp().link(CmsFrameset.JSP_WORKPLACE_URI);
    }

    /**
     * Returns <code>true</code> if a reload of the main body frame is required.<p>
     *
     * This value is modified with the select options (project, site or view) in the head frame of
     * the Workplace. If a user changes one of these select values, the head frame is posted
     * "against itself". The posted values will be processed by this class, causing
     * the internal Workplace settings to change. After these settings have been changed,
     * a reload of the main body frame is required in order to update it with the new values.
     * A JavaScript in the Workplace head frame will be executed in this case.<p>
     *
     * @return <code>true</code> if a reload of the main body frame is required
     */
    public boolean isReloadRequired() {

        return m_reloadRequired;
    }

    /**
     * Returns true if the user has enabled synchronization.<p>
     *
     * @return true if the user has enabled synchronization
     */
    public boolean isSyncEnabled() {

        CmsSynchronizeSettings syncSettings = getSettings().getUserSettings().getSynchronizeSettings();
        return (syncSettings != null) && syncSettings.isSyncEnabled();
    }

    /**
     * Indicates if the site selector should be shown in the top frame depending on the count of accessible sites.<p>
     *
     * @return true if site selector should be shown, otherwise false
     */
    public boolean showSiteSelector() {

        if (getSettings().getUserSettings().getRestrictExplorerView()) {
            // restricted explorer view to site and folder, do not show site selector
            return false;
        }
        // count available sites
        int siteCount = OpenCms.getSiteManager().getAvailableSites(getCms(), true).size();
        return (siteCount > 1);
    }

    /**
     * @see org.opencms.workplace.CmsWorkplace#initTimeWarp(org.opencms.db.CmsUserSettings, javax.servlet.http.HttpSession)
     */
    @Override
    protected void initTimeWarp(CmsUserSettings settings, HttpSession session) {

        // overriden to avoid deletion of the configured time warp:
        // this is triggered by editors and in auto time warping a direct edit
        // must not delete a potential auto warped request time
    }

    /**
     * @see org.opencms.workplace.CmsWorkplace#initWorkplaceRequestValues(org.opencms.workplace.CmsWorkplaceSettings, javax.servlet.http.HttpServletRequest)
     */
    @Override
    protected void initWorkplaceRequestValues(CmsWorkplaceSettings settings, HttpServletRequest request) {

        // check if a startup page has been set
        String frame = CmsRequestUtil.getNotEmptyDecodedParameter(request, CmsFrameset.PARAM_WP_FRAME);
        if ((frame == null) || (FRAMES_LIST.indexOf(frame) < 0)) {
            // illegal or no frame selected, assume the "top" frame
            frame = FRAMES[0];
        }

        if (FRAMES[0].equals(frame)) {
            // top frame requested - execute special reload actions
            topFrameReload(settings);
        }

        // check if a startup page has been set
        String startup = CmsRequestUtil.getNotEmptyDecodedParameter(request, CmsFrameset.PARAM_WP_START);
        if (startup != null) {
            m_reloadRequired = true;
            settings.setViewStartup(startup);
        }

        // check if the user requested a view change
        String view = request.getParameter(CmsFrameset.PARAM_WP_VIEW);
        if (view != null) {
            m_reloadRequired = true;
            settings.setViewUri(view);
            settings.getFrameUris().put("body", view);
        }

        m_reloadRequired = initSettings(settings, request) || m_reloadRequired;
    }

    /**
     * Performs certain clear cache actions if the top frame is reloaded.<p>
     *
     * @param settings the current users workplace settings
     */
    protected void topFrameReload(CmsWorkplaceSettings settings) {

        // ensure to read the settings from the database
        initUserSettings(getCms(), settings, true);

        // reset the HTML list in order to force a full reload
        settings.setListObject(null);
    }
}
