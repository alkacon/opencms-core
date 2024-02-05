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

package org.opencms.main;

import org.opencms.configuration.CmsSystemConfiguration.UserSessionMode;
import org.opencms.db.CmsUserSettings;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsProject;
import org.opencms.file.CmsRequestContext;
import org.opencms.file.CmsUser;
import org.opencms.main.CmsBroadcast.ContentMode;
import org.opencms.security.CmsCustomLoginException;
import org.opencms.security.CmsRole;
import org.opencms.security.CmsSecurityException;
import org.opencms.security.CmsUserLog;
import org.opencms.ui.login.CmsLoginHelper;
import org.opencms.util.CmsRequestUtil;
import org.opencms.util.CmsStringUtil;
import org.opencms.util.CmsUUID;
import org.opencms.workplace.CmsWorkplace;
import org.opencms.workplace.CmsWorkplaceManager;
import org.opencms.workplace.tools.CmsToolManager;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpSessionEvent;

import org.apache.commons.collections.Buffer;
import org.apache.commons.collections.BufferUtils;
import org.apache.commons.collections.buffer.CircularFifoBuffer;
import org.apache.commons.logging.Log;

/**
 * Keeps track of the sessions running on the OpenCms server and
 * provides a session info storage which is used to get an overview
 * about currently authenticated OpenCms users, as well as sending broadcasts between users.<p>
 *
 * For each authenticated OpenCms user, a {@link org.opencms.main.CmsSessionInfo} object
 * holds the information about the users status.<p>
 *
 * When a user session is invalidated, the user info will be removed.
 * This happens when a user log out, or when his session times out.<p>
 *
 * <b>Please Note:</b> The current implementation does not provide any permission checking,
 * so all users can access the methods of this manager. Permission checking
 * based on the current users OpenCms context may be added in a future OpenCms release.<p>
 *
 * @since 6.0.0
 */
public class CmsSessionManager {

    /** Header key 'true-client-ip' used by akamai proxies. */
    public static final String HEADER_TRUE_CLIENT_IP = "true-client-ip";

    /** Header key 'user-agent'. */
    public static final String HEADER_USER_AGENT = "user-agent";

    /** Request header containing the real client IP address. */
    public static final String HEADER_X_FORWARDED_FOR = "x-forwarded-for";

    /** Name of the logger for logging user switches. */
    public static final String NAME_USERSWITCH = "userswitch";

    /** Session attribute key for client token. */
    private static final String CLIENT_TOKEN = "client-token";

    /** The log object for this class. */
    private static final Log LOG = CmsLog.getLog(CmsSessionManager.class);

    /** Special logger for logging user switches. */
    private static final Log USERSWITCH = CmsLog.getLog(NAME_USERSWITCH);

    static {
        CmsLog.makeChannelNonManageable(NAME_USERSWITCH);
    }

    /** Lock object for synchronized session count updates. */
    private Object m_lockSessionCount;

    /** Counter for the currently active sessions. */
    private int m_sessionCountCurrent;

    /** Counter for all sessions created so far. */
    private int m_sessionCountTotal;

    /** Session storage provider instance. */
    private I_CmsSessionStorageProvider m_sessionStorageProvider;

    /** The user session mode. */
    private UserSessionMode m_userSessionMode;

    /** Admin CmsObject. */
    private CmsObject m_adminCms;

    /**
     * Creates a new instance of the OpenCms session manager.<p>
     */
    protected CmsSessionManager() {

        // create a lock object for the session counter
        m_lockSessionCount = new Object();
    }

    /**
     * Checks whether a new session can be created for the user, and throws an exception if not.<p>
     *
     * @param user the user to check
     * @throws CmsException if no new session for the user can't be created
     */
    public void checkCreateSessionForUser(CmsUser user) throws CmsException {

        if (getUserSessionMode() == UserSessionMode.single) {
            List<CmsSessionInfo> infos = getSessionInfos(user.getId());
            if (!infos.isEmpty()) {
                throw new CmsCustomLoginException(
                    org.opencms.security.Messages.get().container(
                        org.opencms.security.Messages.ERR_ALREADY_LOGGED_IN_0));
            }
        }

    }

    /**
     * Returns the broadcast queue for the given OpenCms session id.<p>
     *
     * @param sessionId the OpenCms session id to get the broadcast queue for
     *
     * @return the broadcast queue for the given OpenCms session id
     */
    public Buffer getBroadcastQueue(String sessionId) {

        CmsSessionInfo sessionInfo = getSessionInfo(getSessionUUID(sessionId));
        if (sessionInfo == null) {
            // return empty message buffer if the session is gone or not available
            return BufferUtils.synchronizedBuffer(new CircularFifoBuffer(CmsSessionInfo.QUEUE_SIZE));
        }
        return sessionInfo.getBroadcastQueue();
    }

    /**
     * Returns the number of sessions currently authenticated in the OpenCms security system.<p>
     *
     * @return the number of sessions currently authenticated in the OpenCms security system
     */
    public int getSessionCountAuthenticated() {

        // since this method could be called from another thread
        // we have to prevent access before initialization
        if (m_sessionStorageProvider == null) {
            return 0;
        }
        return m_sessionStorageProvider.getSize();
    }

    /**
     * Returns the number of current sessions, including the sessions of not authenticated guest users.<p>
     *
     * @return the number of current sessions, including the sessions of not authenticated guest users
     */
    public int getSessionCountCurrent() {

        return m_sessionCountCurrent;
    }

    /**
     * Returns the number of total sessions generated so far, including already destroyed sessions.<p>
     *
     * @return the number of total sessions generated so far, including already destroyed sessions
     */
    public int getSessionCountTotal() {

        return m_sessionCountTotal;
    }

    /**
     * Returns the complete user session info of a user from the session storage,
     * or <code>null</code> if this session id has no session info attached.<p>
     *
     * @param sessionId the OpenCms session id to return the session info for
     *
     * @return the complete user session info of a user from the session storage
     */
    public CmsSessionInfo getSessionInfo(CmsUUID sessionId) {

        // since this method could be called from another thread
        // we have to prevent access before initialization
        if (m_sessionStorageProvider == null) {
            return null;
        }
        return m_sessionStorageProvider.get(sessionId);
    }

    /**
     * Returns the OpenCms user session info for the given request,
     * or <code>null</code> if no user session is available.<p>
     *
     * @param req the current request
     *
     * @return the OpenCms user session info for the given request, or <code>null</code> if no user session is available
     */
    public CmsSessionInfo getSessionInfo(HttpServletRequest req) {

        HttpSession session = req.getSession(false);
        if (session == null) {
            // special case for accessing a session from "outside" requests (e.g. upload applet)
            String sessionId = req.getHeader(CmsRequestUtil.HEADER_JSESSIONID);
            return sessionId == null ? null : getSessionInfo(sessionId);
        }
        return getSessionInfo(session);
    }

    /**
     * Returns the OpenCms user session info for the given http session,
     * or <code>null</code> if no user session is available.<p>
     *
     * @param session the current http session
     *
     * @return the OpenCms user session info for the given http session, or <code>null</code> if no user session is available
     */
    public CmsSessionInfo getSessionInfo(HttpSession session) {

        if (session == null) {
            return null;
        }
        CmsUUID sessionId = (CmsUUID)session.getAttribute(CmsSessionInfo.ATTRIBUTE_SESSION_ID);
        return (sessionId == null) ? null : getSessionInfo(sessionId);
    }

    /**
     * Returns the complete user session info of a user from the session storage,
     * or <code>null</code> if this session id has no session info attached.<p>
     *
     * @param sessionId the OpenCms session id to return the session info for,
     * this must be a String representation of a {@link CmsUUID}
     *
     * @return the complete user session info of a user from the session storage
     *
     * @see #getSessionInfo(CmsUUID)
     */
    public CmsSessionInfo getSessionInfo(String sessionId) {

        return getSessionInfo(getSessionUUID(sessionId));
    }

    /**
     * Returns all current session info objects.<p>
     *
     * @return all current session info objects
     */
    public List<CmsSessionInfo> getSessionInfos() {

        // since this method could be called from another thread
        // we have to prevent access before initialization
        if (m_sessionStorageProvider == null) {
            return Collections.emptyList();
        }
        return m_sessionStorageProvider.getAll();
    }

    /**
     * Returns a list of all active session info objects for the specified user.<p>
     *
     * An OpenCms user can have many active sessions.
     * This is e.g. possible when two people have logged in to the system using the
     * same username. Even one person can have multiple sessions if he
     * is logged in to OpenCms with several browser windows at the same time.<p>
     *
     * @param userId the id of the user
     *
     * @return a list of all active session info objects for the specified user
     */
    public List<CmsSessionInfo> getSessionInfos(CmsUUID userId) {

        // since this method could be called from another thread
        // we have to prevent access before initialization
        if (m_sessionStorageProvider == null) {
            return Collections.emptyList();
        }
        return m_sessionStorageProvider.getAllOfUser(userId);
    }

    /**
     * Gets the user session mode.<p>
     *
     * @return the user session mode
     */
    public UserSessionMode getUserSessionMode() {

        return m_userSessionMode;
    }

    /**
     * Returns whether the current request has a valid client token.<p>
     * Used to prevent session hijacking.<p>
     *
     * @param req the current request
     *
     * @return <code>true</code> in case the request has a valid token
     */
    public boolean hasValidClientToken(HttpServletRequest req) {

        String requestToken = generateClientToken(req);
        String sessionToken = null;
        HttpSession session = req.getSession(false);
        if (session != null) {
            sessionToken = (String)session.getAttribute(CLIENT_TOKEN);
        }
        return requestToken.equals(sessionToken);
    }

    /**
     * Kills all sessions for the given user.<p>
     *
     * @param cms the current CMS context
     * @param user the user for whom the sessions should be killed
     *
     * @throws CmsException if something goes wrong
     */
    public void killSession(CmsObject cms, CmsUser user) throws CmsException {

        OpenCms.getRoleManager().checkRole(cms, CmsRole.ACCOUNT_MANAGER);
        List<CmsSessionInfo> infos = getSessionInfos(user.getId());
        for (CmsSessionInfo info : infos) {
            m_sessionStorageProvider.remove(info.getSessionId());
        }
    }

    /**
     * Destroys a session given the session id. Only allowed for users which have the "account manager" role.<p>
     *
     * @param cms the current CMS context
     * @param sessionid the session id
     *
     * @throws CmsException if something goes wrong
     */
    public void killSession(CmsObject cms, CmsUUID sessionid) throws CmsException {

        OpenCms.getRoleManager().checkRole(cms, CmsRole.ACCOUNT_MANAGER);
        m_sessionStorageProvider.remove(sessionid);

    }

    /**
     * Sends a broadcast to all sessions of all currently authenticated users.<p>
     *
     * @param cms the OpenCms user context of the user sending the broadcast
     *
     * @param message the message to broadcast
     */
    @Deprecated
    public void sendBroadcast(CmsObject cms, String message) {

        sendBroadcast(cms, message, ContentMode.plain);
    }

    /**
     * Sends a broadcast to all sessions of all currently authenticated users.<p>
     *
     * @param cms the OpenCms user context of the user sending the broadcast
     *
     * @param message the message to broadcast
     * @param repeat repeat this message
     */
    @Deprecated
    public void sendBroadcast(CmsObject cms, String message, boolean repeat) {

        sendBroadcast(cms, message, repeat, ContentMode.plain);

    }

    /**
     * Sends a broadcast to all sessions of all currently authenticated users.<p>
     *
     * @param cms the OpenCms user context of the user sending the broadcast
     * @param message the message to broadcast
     * @param repeat repeat this message
     * @param mode the content mode to use
     */
    public void sendBroadcast(CmsObject cms, String message, boolean repeat, ContentMode mode) {

        if (CmsStringUtil.isEmptyOrWhitespaceOnly(message)) {
            // don't broadcast empty messages
            return;
        }
        // create the broadcast
        CmsBroadcast broadcast = new CmsBroadcast(cms.getRequestContext().getCurrentUser(), message, repeat, mode);
        // send the broadcast to all authenticated sessions
        Iterator<CmsSessionInfo> i = m_sessionStorageProvider.getAll().iterator();
        while (i.hasNext()) {
            CmsSessionInfo sessionInfo = i.next();
            if (m_sessionStorageProvider.get(sessionInfo.getSessionId()) != null) {
                // double check for concurrent modification
                sessionInfo.getBroadcastQueue().add(broadcast);
            }
        }

    }

    /**
     * Sends a broadcast to all sessions of all currently authenticated users.<p>
     *
     * @param cms the OpenCms user context of the user sending the broadcast
     * @param message the message to broadcast
     * @param mode the content mode
     */
    public void sendBroadcast(CmsObject cms, String message, ContentMode mode) {

        sendBroadcast(cms, message, false, mode);
    }

    /**
     * Sends a broadcast to the specified user session.<p>
     *
     * @param cms the OpenCms user context of the user sending the broadcast
     *
     * @param message the message to broadcast
     * @param sessionId the OpenCms session uuid target (receiver) of the broadcast
     */
    @Deprecated
    public void sendBroadcast(CmsObject cms, String message, String sessionId) {

        sendBroadcast(cms, message, sessionId, false);

    }

    /**
     * Sends a broadcast to the specified user session.<p>
     *
     * @param cms the OpenCms user context of the user sending the broadcast
     *
     * @param message the message to broadcast
     * @param sessionId the OpenCms session uuid target (receiver) of the broadcast
     * @param repeat repeat this message
     */
    @Deprecated
    public void sendBroadcast(CmsObject cms, String message, String sessionId, boolean repeat) {

        sendBroadcast(cms, message, sessionId, repeat, ContentMode.plain);

    }

    /**
     * Sends a broadcast to the specified user session.<p>
     *
     * @param cms the OpenCms user context of the user sending the broadcast
     *
     * @param message the message to broadcast
     * @param sessionId the OpenCms session uuid target (receiver) of the broadcast
     * @param repeat repeat this message
     * @param mode the content mode to use
     */
    public void sendBroadcast(CmsObject cms, String message, String sessionId, boolean repeat, ContentMode mode) {

        if (CmsStringUtil.isEmptyOrWhitespaceOnly(message)) {
            // don't broadcast empty messages
            return;
        }
        // send the broadcast only to the selected session
        CmsSessionInfo sessionInfo = m_sessionStorageProvider.get(new CmsUUID(sessionId));
        if (sessionInfo != null) {
            // double check for concurrent modification
            sessionInfo.getBroadcastQueue().add(
                new CmsBroadcast(cms.getRequestContext().getCurrentUser(), message, repeat, mode));
        }
    }

    /**
     * Sends a broadcast to the specified user session.<p>
     *
     * @param cms the OpenCms user context of the user sending the broadcast
     *
     * @param message the message to broadcast
     * @param sessionId the OpenCms session uuid target (receiver) of the broadcast
     * @param mode the content mode to use
     */
    public void sendBroadcast(CmsObject cms, String message, String sessionId, ContentMode mode) {

        sendBroadcast(cms, message, sessionId, false, mode);
    }

    /**
     * Sends a broadcast to all sessions of a given user.<p>
     *
     * The user sending the message may be a real user like
     * <code>cms.getRequestContext().currentUser()</code> or
     * <code>null</code> for a system message.<p>
     *
     * @param fromUser the user sending the broadcast
     * @param message the message to broadcast
     * @param toUser the target (receiver) of the broadcast
     */
    @Deprecated
    public void sendBroadcast(CmsUser fromUser, String message, CmsUser toUser) {

        sendBroadcast(fromUser, message, toUser, ContentMode.plain);

    }

    /**
     * Sends a broadcast to all sessions of a given user.<p>
     *
     * The user sending the message may be a real user like
     * <code>cms.getRequestContext().currentUser()</code> or
     * <code>null</code> for a system message.<p>
     *
     * @param fromUser the user sending the broadcast
     * @param message the message to broadcast
     * @param toUser the target (receiver) of the broadcast
     * @param mode the content mode to use
     */
    public void sendBroadcast(CmsUser fromUser, String message, CmsUser toUser, CmsBroadcast.ContentMode mode) {

        if (CmsStringUtil.isEmptyOrWhitespaceOnly(message)) {
            // don't broadcast empty messages
            return;
        }
        // create the broadcast
        CmsBroadcast broadcast = new CmsBroadcast(fromUser, message, mode);
        List<CmsSessionInfo> userSessions = getSessionInfos(toUser.getId());
        Iterator<CmsSessionInfo> i = userSessions.iterator();
        // send the broadcast to all sessions of the selected user
        while (i.hasNext()) {
            CmsSessionInfo sessionInfo = i.next();
            if (m_sessionStorageProvider.get(sessionInfo.getSessionId()) != null) {
                // double check for concurrent modification
                sessionInfo.getBroadcastQueue().add(broadcast);
            }
        }

    }

    /**
     * Switches the current user to the given user. The session info is rebuild as if the given user
     * performs a login at the workplace.
     *
     * @param cms the current CmsObject
     * @param req the current request
     * @param user the user to switch to
     *
     * @return the direct edit target if available
     *
     * @throws CmsException if something goes wrong
     */
    public String switchUser(CmsObject cms, HttpServletRequest req, CmsUser user) throws CmsException {

        return switchUserFromSession(cms, req, user, null);
    }

    /**
     * Switches the current user to the given user. The session info is rebuild as if the given user
     * performs a login at the workplace.
     *
     * @param cms the current CmsObject
     * @param req the current request
     * @param user the user to switch to
     * @param sessionInfo to switch to a currently logged in user using the same session state
     *
     * @return the direct edit target if available
     *
     * @throws CmsException if something goes wrong
     */
    public String switchUserFromSession(CmsObject cms, HttpServletRequest req, CmsUser user, CmsSessionInfo sessionInfo)
    throws CmsException {

        // only user with root administrator role are allowed to switch the user
        OpenCms.getRoleManager().checkRole(cms, CmsRole.ADMINISTRATOR.forOrgUnit(user.getOuFqn()));
        CmsSessionInfo info = getSessionInfo(req);
        HttpSession session = req.getSession(false);
        if ((info == null) || (session == null)) {
            throw new CmsException(Messages.get().container(Messages.ERR_NO_SESSIONINFO_SESSION_0));
        }

        if (!OpenCms.getRoleManager().hasRole(cms, user.getName(), CmsRole.ELEMENT_AUTHOR)) {
            throw new CmsSecurityException(Messages.get().container(Messages.ERR_NO_WORKPLACE_PERMISSIONS_0));
        }
        String oldUser = cms.getRequestContext().getCurrentUser().getName();

        // get the user settings for the given user and set the start project and the site root
        CmsUserSettings settings = new CmsUserSettings(user);
        String ouFqn = user.getOuFqn();

        CmsProject userProject;
        String userSiteRoot;

        if (sessionInfo == null) {
            userProject = cms.readProject(
                ouFqn + OpenCms.getWorkplaceManager().getDefaultUserSettings().getStartProject());
            try {
                userProject = cms.readProject(settings.getStartProject());
            } catch (Exception e) {
                // ignore, use default
            }
            CmsObject userCms = OpenCms.initCmsObject(m_adminCms, new CmsContextInfo(user.getName()));

            userSiteRoot = CmsWorkplace.getStartSiteRoot(userCms, settings);
        } else {
            userProject = cms.readProject(sessionInfo.getProject());
            userSiteRoot = sessionInfo.getSiteRoot();
        }

        CmsRequestContext context = new CmsRequestContext(
            user,
            userProject,
            null,
            cms.getRequestContext().getRequestMatcher(),
            userSiteRoot,
            cms.getRequestContext().isSecureRequest(),
            null,
            null,
            null,
            0,
            null,
            null,
            ouFqn,
            false);
        // delete the stored workplace settings, so the session has to receive them again
        session.removeAttribute(CmsWorkplaceManager.SESSION_WORKPLACE_SETTINGS);

        // create a new CmsSessionInfo and store it inside the session map
        CmsSessionInfo newInfo = new CmsSessionInfo(context, info.getSessionId(), info.getMaxInactiveInterval());
        addSessionInfo(newInfo);
        // set the site root, project and ou fqn to current cms context
        cms.getRequestContext().setSiteRoot(userSiteRoot);
        cms.getRequestContext().setCurrentProject(userProject);
        cms.getRequestContext().setOuFqn(user.getOuFqn());
        USERSWITCH.info("User '" + oldUser + "' switched to user '" + user.getName() + "'");
        CmsUserLog.logSwitchUser(cms, user.getName());
        String directEditTarget = CmsLoginHelper.getDirectEditPath(cms, new CmsUserSettings(user), false);
        return directEditTarget != null
        ? OpenCms.getLinkManager().substituteLink(cms, directEditTarget, userSiteRoot)
        : null;
    }

    /**
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {

        StringBuffer output = new StringBuffer();
        Iterator<CmsSessionInfo> i = m_sessionStorageProvider.getAll().iterator();
        output.append("[CmsSessions]:\n");
        while (i.hasNext()) {
            CmsSessionInfo sessionInfo = i.next();
            output.append(sessionInfo.getSessionId().toString());
            output.append(" : ");
            output.append(sessionInfo.getUserId().toString());
            output.append('\n');
        }
        return output.toString();
    }

    /**
     * Updates the the OpenCms session data used for quick authentication of users.<p>
     *
     * This is required if the user data (current group or project) was changed in
     * the requested document.<p>
     *
     * The user data is only updated if the user was authenticated to the system.
     *
     * @param cms the current OpenCms user context
     * @param req the current request
     */
    public void updateSessionInfo(CmsObject cms, HttpServletRequest req) {

        updateSessionInfo(cms, req, false);
    }

    /**
     * Updates the the OpenCms session data used for quick authentication of users.<p>
     *
     * This is required if the user data (current group or project) was changed in
     * the requested document.<p>
     *
     * The user data is only updated if the user was authenticated to the system.
     *
     * @param cms the current OpenCms user context
     * @param req the current request
     * @param isHeartBeatRequest in case of heart beat requests
     */
    public void updateSessionInfo(CmsObject cms, HttpServletRequest req, boolean isHeartBeatRequest) {

        if (!cms.getRequestContext().isUpdateSessionEnabled()) {
            // this request must not update the user session info
            // this is true for long running "thread" requests, e.g. during project publish
            return;
        }

        if (cms.getRequestContext().getUri().equals(CmsToolManager.VIEW_JSPPAGE_LOCATION)) {
            // this request must not update the user session info
            // if not the switch user feature would not work
            return;
        }

        if (!cms.getRequestContext().getCurrentUser().isGuestUser()) {
            // Guest user requests don't need to update the OpenCms user session information

            // get the session info object for the user
            CmsSessionInfo sessionInfo = getSessionInfo(req);
            if (sessionInfo != null) {
                // update the users session information
                sessionInfo.update(cms.getRequestContext(), isHeartBeatRequest);
                addSessionInfo(sessionInfo);
            } else {
                HttpSession session = req.getSession(false);
                // only create session info if a session is already available
                if (session != null) {
                    // create a new session info for the user
                    sessionInfo = new CmsSessionInfo(
                        cms.getRequestContext(),
                        new CmsUUID(),
                        session.getMaxInactiveInterval());
                    // append the session info to the http session
                    session.setAttribute(CmsSessionInfo.ATTRIBUTE_SESSION_ID, sessionInfo.getSessionId().clone());
                    // store a client token to prevent session hijacking
                    session.setAttribute(CLIENT_TOKEN, generateClientToken(req));
                    // update the session info user data
                    addSessionInfo(sessionInfo);
                }
            }
        }
    }

    /**
     * Updates the the OpenCms session data used for quick authentication of users.<p>
     *
     * This is required if the user data (current group or project) was changed in
     * the requested document.<p>
     *
     * The user data is only updated if the user was authenticated to the system.
     *
     * @param cms the current OpenCms user context
     * @param session the current session
     */
    public void updateSessionInfo(CmsObject cms, HttpSession session) {

        if (session == null) {
            return;
        }

        if (!cms.getRequestContext().isUpdateSessionEnabled()) {
            // this request must not update the user session info
            // this is true for long running "thread" requests, e.g. during project publish
            return;
        }

        if (cms.getRequestContext().getUri().equals(CmsToolManager.VIEW_JSPPAGE_LOCATION)) {
            // this request must not update the user session info
            // if not the switch user feature would not work
            return;
        }

        if (!cms.getRequestContext().getCurrentUser().isGuestUser()) {
            // Guest user requests don't need to update the OpenCms user session information

            // get the session info object for the user
            CmsSessionInfo sessionInfo = getSessionInfo(session);
            if (sessionInfo != null) {
                // update the users session information
                sessionInfo.update(cms.getRequestContext());
                addSessionInfo(sessionInfo);
            } else {
                sessionInfo = new CmsSessionInfo(
                    cms.getRequestContext(),
                    new CmsUUID(),
                    session.getMaxInactiveInterval());
                // append the session info to the http session
                session.setAttribute(CmsSessionInfo.ATTRIBUTE_SESSION_ID, sessionInfo.getSessionId().clone());
                // update the session info user data
                addSessionInfo(sessionInfo);
            }
        }
    }

    /**
     * Updates all session info objects, so that invalid projects
     * are replaced by the Online project.<p>
     *
     * @param cms the cms context
     */
    public void updateSessionInfos(CmsObject cms) {

        // get all sessions
        List<CmsSessionInfo> userSessions = getSessionInfos();
        Iterator<CmsSessionInfo> i = userSessions.iterator();
        while (i.hasNext()) {
            CmsSessionInfo sessionInfo = i.next();
            // check is the project stored in this session is not existing anymore
            // if so, set it to the online project
            CmsUUID projectId = sessionInfo.getProject();
            try {
                cms.readProject(projectId);
            } catch (CmsException e) {
                // the project does not longer exist, update the project information with the online project
                sessionInfo.setProject(CmsProject.ONLINE_PROJECT_ID);
                addSessionInfo(sessionInfo);
            }
        }
    }

    /**
     * Adds a new session info into the session storage.<p>
     *
     * @param sessionInfo the session info to store for the id
     */
    protected void addSessionInfo(CmsSessionInfo sessionInfo) {

        if (getUserSessionMode() == UserSessionMode.standard) {
            m_sessionStorageProvider.put(sessionInfo);
        } else if (getUserSessionMode() == UserSessionMode.single) {
            CmsUUID userId = sessionInfo.getUserId();
            List<CmsSessionInfo> infos = getSessionInfos(userId);
            if (infos.isEmpty()
                || ((infos.size() == 1) && infos.get(0).getSessionId().equals(sessionInfo.getSessionId()))) {
                m_sessionStorageProvider.put(sessionInfo);
            } else {
                throw new RuntimeException("Can't create another session for the same user.");
            }
        }
    }

    /**
     * Returns the UUID representation for the given session id String.<p>
     *
     * @param sessionId the session id String to return the  UUID representation for
     *
     * @return the UUID representation for the given session id String
     */
    protected CmsUUID getSessionUUID(String sessionId) {

        return new CmsUUID(sessionId);
    }

    /**
     * Sets the storage provider.<p>
     *
     * @param sessionStorageProvider the storage provider implementation
     * @param adminCms
     */
    protected void initialize(I_CmsSessionStorageProvider sessionStorageProvider, CmsObject adminCms) {

        m_sessionStorageProvider = sessionStorageProvider;
        m_sessionStorageProvider.initialize();
        m_adminCms = adminCms;
    }

    /**
     * Called by the {@link OpenCmsListener} when a http session is created.<p>
     *
     * @param event the http session event
     *
     * @see javax.servlet.http.HttpSessionListener#sessionCreated(javax.servlet.http.HttpSessionEvent)
     * @see OpenCmsListener#sessionCreated(HttpSessionEvent)
     */
    protected void sessionCreated(HttpSessionEvent event) {

        HttpServletRequest request = OpenCmsServlet.currentRequestStack.top();
        String tid = "[" + Thread.currentThread().getId() + "] ";
        synchronized (m_lockSessionCount) {
            m_sessionCountCurrent = (m_sessionCountCurrent <= 0) ? 1 : (m_sessionCountCurrent + 1);
            m_sessionCountTotal++;
            if (LOG.isInfoEnabled()) {
                LOG.info(
                    tid
                        + Messages.get().getBundle().key(
                            Messages.LOG_SESSION_CREATED_2,
                            Integer.valueOf(m_sessionCountTotal),
                            Integer.valueOf(m_sessionCountCurrent)));
            }
        }

        if (LOG.isDebugEnabled()) {
            LOG.debug(tid + Messages.get().getBundle().key(Messages.LOG_SESSION_CREATED_1, event.getSession().getId()));
            if (request != null) {
                LOG.debug(tid + "Session created in request: " + request.getRequestURL());
            }
            StringWriter sw = new StringWriter();
            new Throwable("").printStackTrace(new PrintWriter(sw));
            String stackTrace = sw.toString();
            LOG.debug(tid + "Stack = \n" + stackTrace);
        }
    }

    /**
     * Called by the {@link OpenCmsListener} when a http session is destroyed.<p>
     *
     * @param event the http session event
     *
     * @see javax.servlet.http.HttpSessionListener#sessionDestroyed(javax.servlet.http.HttpSessionEvent)
     * @see OpenCmsListener#sessionDestroyed(HttpSessionEvent)
     */
    protected void sessionDestroyed(HttpSessionEvent event) {

        synchronized (m_lockSessionCount) {
            m_sessionCountCurrent = (m_sessionCountCurrent <= 0) ? 0 : (m_sessionCountCurrent - 1);
            if (LOG.isInfoEnabled()) {
                LOG.info(
                    Messages.get().getBundle().key(
                        Messages.LOG_SESSION_DESTROYED_2,
                        Integer.valueOf(m_sessionCountTotal),
                        Integer.valueOf(m_sessionCountCurrent)));
            }
        }

        CmsSessionInfo sessionInfo = getSessionInfo(event.getSession());
        CmsUUID userId = null;
        if (sessionInfo != null) {
            userId = sessionInfo.getUserId();
            m_sessionStorageProvider.remove(sessionInfo.getSessionId());
        }

        if ((userId != null) && (getSessionInfos(userId).size() == 0)) {
            // remove the temporary locks of this user from memory
            OpenCmsCore.getInstance().getLockManager().removeTempLocks(userId);
        }

        HttpSession session = event.getSession();
        Enumeration<?> attrNames = session.getAttributeNames();
        while (attrNames.hasMoreElements()) {
            String attrName = (String)attrNames.nextElement();
            Object attribute = session.getAttribute(attrName);
            if (attribute instanceof I_CmsSessionDestroyHandler) {
                try {
                    ((I_CmsSessionDestroyHandler)attribute).onSessionDestroyed();
                } catch (Exception e) {
                    LOG.error(e.getLocalizedMessage(), e);
                }
            }
        }

        if (LOG.isDebugEnabled()) {
            LOG.debug(Messages.get().getBundle().key(Messages.LOG_SESSION_DESTROYED_1, event.getSession().getId()));
        }
    }

    /**
     * Sets the user session mode.<p>
     *
     * @param userSessionMode the user session mode
     */
    protected void setUserSessionMode(UserSessionMode userSessionMode) {

        m_userSessionMode = userSessionMode;
    }

    /**
     * Removes all stored session info objects.<p>
     *
     * @throws Exception if something goes wrong
     */
    protected void shutdown() throws Exception {

        if (m_sessionStorageProvider != null) {
            m_sessionStorageProvider.shutdown();
        }
    }

    /**
     * Validates the sessions stored in this manager and removes
     * any sessions that have become invalidated.<p>
     */
    protected void validateSessionInfos() {

        // since this method could be called from another thread
        // we have to prevent access before initialization
        if (m_sessionStorageProvider == null) {
            return;
        }
        m_sessionStorageProvider.validate();
    }

    /**
     * Generates a token based on hashed client ip and user agent.<p>
     * Used to prevent session hijacking.<p>
     *
     * @param request the current request
     *
     * @return the client token
     */
    private String generateClientToken(HttpServletRequest request) {

        String ip = request.getHeader(HEADER_TRUE_CLIENT_IP);
        if (CmsStringUtil.isEmptyOrWhitespaceOnly(ip)) {
            ip = request.getHeader(HEADER_X_FORWARDED_FOR);
            if ((ip != null) && ip.contains(",")) {
                ip = ip.split(",")[0];
            }
        }
        if ((ip == null) || CmsStringUtil.isEmptyOrWhitespaceOnly(ip)) {
            ip = request.getRemoteAddr();
        }
        return String.valueOf(ip.hashCode());
    }
}
