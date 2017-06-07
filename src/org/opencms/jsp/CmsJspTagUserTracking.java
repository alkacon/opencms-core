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

package org.opencms.jsp;

import org.opencms.db.CmsSubscriptionFilter;
import org.opencms.db.CmsSubscriptionReadMode;
import org.opencms.db.CmsVisitedByFilter;
import org.opencms.file.CmsGroup;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsResource;
import org.opencms.file.CmsUser;
import org.opencms.flex.CmsFlexController;
import org.opencms.main.CmsException;
import org.opencms.main.CmsLog;
import org.opencms.main.OpenCms;
import org.opencms.util.CmsStringUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;

import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.TagSupport;

import org.apache.commons.logging.Log;

/**
 * Implementation of the <code>&lt;cms:usertracking/&gt;</code> tag.<p>
 *
 * This tag can be used to mark OpenCms files as visited or subscribe/unsubscribe them to/from users or groups.<p>
 *
 * It is also possible to check if single resources are visited/subscribed by the current user.<p>
 *
 * See also the {@link org.opencms.db.CmsSubscriptionManager} for more information about subscription or visitation.<p>
 *
 * @since 8.0
 */
public class CmsJspTagUserTracking extends TagSupport {

    /** Prefix for the visited session attributes. */
    public static final String SESSION_PREFIX_SUBSCRIBED = "__ocmssubscribed_";

    /** Prefix for the visited session attributes. */
    public static final String SESSION_PREFIX_VISITED = "__ocmsvisited_";

    /** The log object for this class. */
    private static final Log LOG = CmsLog.getLog(CmsJspTagUserTracking.class);

    /** Serial version UID required for safe serialization. */
    private static final long serialVersionUID = 4253583631739670341L;

    /** Static array with allowed track action values. */
    private static final String[] TAG_ACTIONS = {
        "visit", // 0, default action
        "subscribe", // 1
        "unsubscribe", // 2
        "checkvisited", // 3
        "checksubscribed" // 4
    };

    /** List of allowed track action values for more convenient lookup. */
    private static final List<String> TRACK_ACTIONS_LIST = Arrays.asList(TAG_ACTIONS);

    /** The value of the <code>action</code> attribute. */
    private String m_action;

    /** The value of the <code>currentuser</code> attribute. */
    private boolean m_currentuser;

    /** The value of the <code>file</code> attribute. */
    private String m_file;

    /** The value of the <code>group</code> attribute. */
    private String m_group;

    /** The value of the <code>includegroups</code> attribute. */
    private boolean m_includegroups;

    /** The value of the <code>online</code> attribute. */
    private boolean m_online;

    /** The value of the <code>subfolder</code> attribute. */
    private boolean m_subfolder;

    /** The value of the <code>user</code> attribute. */
    private String m_user;

    /**
     * Tracks an OpenCms file according to the parameters.<p>
     *
     * @param action the action that should be performed
     * @param fileName the file name to track
     * @param subFolder flag indicating if sub folders should be included
     * @param currentUser flag indicating if the current user should be used for the tracking action
     * @param userName the user name that should be used for the action
     * @param includeGroups flag indicating if the given users groups should be included
     * @param groupName the group name that should be used for the action
     * @param req the current request
     *
     * @return the result of the action, usually empty except for the check actions
     *
     * @throws JspException in case something goes wrong
     */
    public static String userTrackingTagAction(
        String action,
        String fileName,
        boolean subFolder,
        boolean currentUser,
        String userName,
        boolean includeGroups,
        String groupName,
        HttpServletRequest req) throws JspException {

        String result = "";

        CmsFlexController controller = CmsFlexController.getController(req);
        CmsObject cms = controller.getCmsObject();

        CmsUser user = null;
        CmsGroup group = null;

        int actionIndex = TRACK_ACTIONS_LIST.indexOf(action);

        try {
            // determine the group for the action
            if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(groupName)) {
                group = cms.readGroup(groupName);
            }
            // determine the user for the action
            if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(userName)) {
                user = cms.readUser(userName);
            } else if (currentUser) {
                user = cms.getRequestContext().getCurrentUser();
            }
            if ((group == null) && (user == null) && (actionIndex != 4)) {
                // set current user for the action except for check subscriptions
                user = cms.getRequestContext().getCurrentUser();
            }
            // determine the file to track
            if (CmsStringUtil.isEmptyOrWhitespaceOnly(fileName)) {
                fileName = cms.getRequestContext().getUri();
            }

            switch (actionIndex) {
                case 1: // subscribe
                    if (group != null) {
                        OpenCms.getSubscriptionManager().subscribeResourceFor(cms, group, fileName);
                    }
                    if (user != null) {
                        OpenCms.getSubscriptionManager().subscribeResourceFor(cms, user, fileName);
                    }
                    removeSessionAttributes(new String[] {SESSION_PREFIX_SUBSCRIBED}, req);
                    break;
                case 2: // unsubscribe
                    if (group != null) {
                        OpenCms.getSubscriptionManager().unsubscribeResourceFor(cms, group, fileName);
                    }
                    if (user != null) {
                        OpenCms.getSubscriptionManager().unsubscribeResourceFor(cms, user, fileName);
                    }
                    removeSessionAttributes(new String[] {SESSION_PREFIX_SUBSCRIBED}, req);
                    break;
                case 3: // checkvisited
                    result = String.valueOf(isResourceVisited(cms, fileName, subFolder, user, req));
                    break;
                case 4: // checksubscribed
                    List<CmsGroup> groups = new ArrayList<CmsGroup>();
                    if ((group == null) && includeGroups) {
                        if (user != null) {
                            groups.addAll(cms.getGroupsOfUser(user.getName(), false));
                        } else {
                            groups.addAll(
                                cms.getGroupsOfUser(cms.getRequestContext().getCurrentUser().getName(), false));
                        }
                    } else if (group != null) {
                        groups.add(group);
                    }
                    result = String.valueOf(isResourceSubscribed(cms, fileName, subFolder, user, groups, req));
                    break;
                case 0: // visit
                default: // default action is visit
                    OpenCms.getSubscriptionManager().markResourceAsVisitedBy(cms, fileName, user);
                    removeSessionAttributes(new String[] {SESSION_PREFIX_SUBSCRIBED, SESSION_PREFIX_VISITED}, req);
            }
        } catch (CmsException e) {
            // store original Exception in controller in order to display it later
            Throwable t = controller.setThrowable(e, cms.getRequestContext().getUri());
            throw new JspException(t);
        }

        return result;
    }

    /**
     * Returns a unique session key depending on the values of the given parameters.<p>
     *
     * @param prefix the key prefix to use
     * @param fileName the file name to track
     * @param subFolder flag indicating if sub folders should be included
     * @param user the user that should be used
     * @param groups the groups that should be used
     *
     * @return a unique session key
     */
    protected static String generateSessionKey(
        String prefix,
        String fileName,
        boolean subFolder,
        CmsUser user,
        List<CmsGroup> groups) {

        StringBuffer result = new StringBuffer(256);
        result.append(prefix);
        result.append(CmsResource.getFolderPath(fileName).hashCode()).append("_");
        result.append(subFolder);
        if (user != null) {
            // add user to session key
            result.append("_").append(user.getName().hashCode());
        }
        if ((groups != null) && !groups.isEmpty()) {
            // add group(s) to session key
            StringBuffer groupNames = new StringBuffer(128);
            for (Iterator<CmsGroup> i = groups.iterator(); i.hasNext();) {
                groupNames.append(i.next().getName());
            }
            result.append("_").append(groupNames.toString().hashCode());
        }
        return result.toString();
    }

    /**
     * Returns if the given resource is subscribed to the user or groups.<p>
     *
     * @param cms the current users context
     * @param fileName the file name to track
     * @param subFolder flag indicating if sub folders should be included
     * @param user the user that should be used for the check
     * @param groups the groups that should be used for the check
     * @param req the current request
     *
     * @return <code>true</code> if the given resource is subscribed to the user or groups, otherwise <code>false</code>
     *
     * @throws CmsException if something goes wrong
     */
    protected static boolean isResourceSubscribed(
        CmsObject cms,
        String fileName,
        boolean subFolder,
        CmsUser user,
        List<CmsGroup> groups,
        HttpServletRequest req) throws CmsException {

        CmsResource checkResource = cms.readResource(fileName);

        HttpSession session = req.getSession(true);
        String sessionKey = generateSessionKey(SESSION_PREFIX_SUBSCRIBED, fileName, subFolder, user, groups);
        // try to get the subscribed resources from a session attribute
        @SuppressWarnings("unchecked")
        List<CmsResource> subscribedResources = (List<CmsResource>)session.getAttribute(sessionKey);
        if (subscribedResources == null) {
            // first call, read subscribed resources and store them to session attribute
            CmsSubscriptionFilter filter = new CmsSubscriptionFilter();
            filter.setParentPath(CmsResource.getFolderPath(checkResource.getRootPath()));
            filter.setIncludeSubfolders(subFolder);
            filter.setUser(user);
            filter.setGroups(groups);
            filter.setMode(CmsSubscriptionReadMode.ALL);
            subscribedResources = OpenCms.getSubscriptionManager().readSubscribedResources(cms, filter);
            session.setAttribute(sessionKey, subscribedResources);
        }
        return subscribedResources.contains(checkResource);
    }

    /**
     * Returns if the given resource was visited by the user.<p>
     *
     * @param cms the current users context
     * @param fileName the file name to track
     * @param subFolder flag indicating if sub folders should be included
     * @param user the user that should be used for the check
     * @param req the current request
     *
     * @return <code>true</code> if the given resource was visited by the user, otherwise <code>false</code>
     *
     * @throws CmsException if something goes wrong
     */
    protected static boolean isResourceVisited(
        CmsObject cms,
        String fileName,
        boolean subFolder,
        CmsUser user,
        HttpServletRequest req) throws CmsException {

        CmsResource checkResource = cms.readResource(fileName);

        HttpSession session = req.getSession(true);
        String sessionKey = generateSessionKey(SESSION_PREFIX_VISITED, fileName, subFolder, user, null);
        // try to get the visited resources from a session attribute
        @SuppressWarnings("unchecked")
        List<CmsResource> visitedResources = (List<CmsResource>)req.getSession(true).getAttribute(sessionKey);
        if (visitedResources == null) {
            // first call, read visited resources and store them to session attribute
            CmsVisitedByFilter filter = new CmsVisitedByFilter();
            filter.setUser(user);
            filter.setParentPath(CmsResource.getFolderPath(checkResource.getRootPath()));
            filter.setIncludeSubfolders(subFolder);
            visitedResources = OpenCms.getSubscriptionManager().readResourcesVisitedBy(cms, filter);
            session.setAttribute(sessionKey, visitedResources);
        }

        return visitedResources.contains(checkResource);
    }

    /**
     * Removes all session attributes starting with the given prefixes.<p>
     *
     * @param prefixes the prefixes of the session attributes to remove
     * @param req the current request
     */
    protected static void removeSessionAttributes(String[] prefixes, HttpServletRequest req) {

        HttpSession session = req.getSession(true);
        @SuppressWarnings("unchecked")
        Enumeration<String> en = session.getAttributeNames();
        while (en.hasMoreElements()) {
            String attrKey = en.nextElement();
            for (int i = 0; i < prefixes.length; i++) {
                if (attrKey.startsWith(prefixes[i])) {
                    session.removeAttribute(attrKey);
                }
            }
        }
    }

    /**
     * @see javax.servlet.jsp.tagext.Tag#doStartTag()
     */
    @Override
    public int doStartTag() throws JspException {

        ServletRequest req = pageContext.getRequest();

        // this will always be true if the page is called through OpenCms
        if (CmsFlexController.isCmsRequest(req)) {

            CmsObject cms = CmsFlexController.getCmsObject(req);

            if (m_online && !cms.getRequestContext().getCurrentProject().isOnlineProject()) {
                // the online flag is set and we are in an offline project, do not track file
                return SKIP_BODY;
            }
            try {
                String result = userTrackingTagAction(
                    m_action,
                    m_file,
                    m_subfolder,
                    m_currentuser,
                    m_user,
                    m_includegroups,
                    m_group,
                    (HttpServletRequest)req);
                pageContext.getOut().print(result);
            } catch (Exception ex) {
                if (LOG.isErrorEnabled()) {
                    LOG.error(Messages.get().getBundle().key(Messages.ERR_PROCESS_TAG_1, "usertracking"), ex);
                }
                throw new JspException(ex);
            }
        }
        return SKIP_BODY;
    }

    /**
     * Returns the action that should be performed, i.e. mark as visited or subscribe/unsubscribe.<p>
     *
     * @return the action that should be performed
     */
    public String getAction() {

        return m_action != null ? m_action : "";
    }

    /**
     * Returns the current user flag.<p>
     *
     * @return the current user flag
     */
    public String getCurrentuser() {

        return String.valueOf(m_currentuser);
    }

    /**
     * Returns the file name to track.<p>
     *
     * @return the file name to track
     */
    public String getFile() {

        return m_file != null ? m_file : "";
    }

    /**
     * Returns the group name that is used for the tracking.<p>
     *
     * @return the group name that is used for the tracking
     */
    public String getGroup() {

        return m_group != null ? m_group : "";
    }

    /**
     * Returns the include groups flag.<p>
     *
     * @return the include groups flag
     */
    public String getIncludegroups() {

        return String.valueOf(m_includegroups);
    }

    /**
     * Returns the online flag.<p>
     *
     * @return the online flag
     */
    public String getOnline() {

        return String.valueOf(m_online);
    }

    /**
     * Returns the subfolder flag.<p>
     *
     * @return the subfolder flag
     */
    public String getSubfolder() {

        return String.valueOf(m_subfolder);
    }

    /**
     * Returns the user name that is used for the tracking.<p>
     *
     * @return the user name that is used for the tracking
     */
    public String getUser() {

        return m_user != null ? m_user : "";
    }

    /**
     * @see javax.servlet.jsp.tagext.Tag#release()
     */
    @Override
    public void release() {

        super.release();
        m_action = null;
        m_file = null;
        m_group = null;
        m_includegroups = false;
        m_online = false;
        m_subfolder = false;
        m_user = null;
    }

    /**
     * Sets the action that should be performed, i.e. mark as visited or subscribe/unsubscribe.<p>
     *
     * @param action the action that should be performed
     */
    public void setAction(String action) {

        if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(action)) {
            m_action = action;
        }
    }

    /**
     * Sets the current user flag.<p>
     *
     * Current user is <code>false</code> by default.<p>
     *
     * @param currentUser the flag to set
     */
    public void setCurrentuser(String currentUser) {

        if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(currentUser)) {
            m_currentuser = Boolean.valueOf(currentUser).booleanValue();
        }
    }

    /**
     * Sets the file name to track.<p>
     *
     * @param file the file name to track
     */
    public void setFile(String file) {

        if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(file)) {
            m_file = file;
        }
    }

    /**
     * Sets the group name that is used for the tracking.<p>
     *
     * @param group the group name that is used for the tracking
     */
    public void setGroup(String group) {

        if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(group)) {
            m_group = group;
        }
    }

    /**
     * Sets the include groups flag.<p>
     *
     * Include groups is <code>false</code> by default.<p>
     *
     * @param includeGroups the flag to set
     */
    public void setIncludegroups(String includeGroups) {

        if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(includeGroups)) {
            m_includegroups = Boolean.valueOf(includeGroups).booleanValue();
        }
    }

    /**
     * Sets the online flag.<p>
     *
     * Online is <code>false</code> by default.<p>
     *
     * @param online the flag to set
     */
    public void setOnline(String online) {

        if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(online)) {
            m_online = Boolean.valueOf(online).booleanValue();
        }
    }

    /**
     * Sets the subfolder flag.<p>
     *
     * Online is <code>false</code> by default.<p>
     *
     * @param subfolder the flag to set
     */
    public void setSubfolder(String subfolder) {

        if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(subfolder)) {
            m_subfolder = Boolean.valueOf(subfolder).booleanValue();
        }
    }

    /**
     * Sets the user name that is used for the tracking.<p>
     *
     * @param user the user name that is used for the tracking
     */
    public void setUser(String user) {

        if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(user)) {
            m_user = user;
        }
    }

}
