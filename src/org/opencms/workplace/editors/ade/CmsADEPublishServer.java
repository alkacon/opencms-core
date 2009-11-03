/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/workplace/editors/ade/Attic/CmsADEPublishServer.java,v $
 * Date   : $Date: 2009/11/03 09:28:38 $
 * Version: $Revision: 1.6 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (c) 2002 - 2009 Alkacon Software GmbH (http://www.alkacon.com)
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

package org.opencms.workplace.editors.ade;

import org.opencms.file.CmsObject;
import org.opencms.file.CmsResource;
import org.opencms.file.CmsResourceFilter;
import org.opencms.file.I_CmsResource;
import org.opencms.json.I_CmsJsonifable;
import org.opencms.json.JSONArray;
import org.opencms.json.JSONException;
import org.opencms.json.JSONObject;
import org.opencms.jsp.CmsJspActionElement;
import org.opencms.main.CmsException;
import org.opencms.main.CmsLog;
import org.opencms.main.OpenCms;
import org.opencms.security.CmsRole;
import org.opencms.util.CmsUUID;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.logging.Log;

/**
 * ADE publishing features.<p>
 * 
 * @author Michael Moossen 
 * 
 * @version $Revision: 1.6 $
 * 
 * @since 7.9.3
 */
public class CmsADEPublishServer {

    /** Json property name constants for responses. */
    protected enum JsonResponse {

        /** Flag to indicate if the current user can publish. */
        CANPUBLISH("canPublish"),
        /** The list of groups. */
        GROUPS("groups"),
        /** The list of projects. */
        PROJECTS("projects"),
        /** A list of resources. */
        RESOURCES("resources");

        /** Property name. */
        private String m_name;

        /** Constructor.<p> */
        private JsonResponse(String name) {

            m_name = name;
        }

        /** 
         * Returns the name.<p>
         * 
         * @return the name
         */
        public String getName() {

            return m_name;
        }
    }

    /** Request parameter name constants for publishing. */
    protected enum ParamPublish {

        /** Flag to force publishing with broken links. */
        FORCE("force"),
        /** The project to publish. */
        PROJECT("project"),
        /** Flag to indicate if to publish with related resources. */
        RELATED("related"),
        /** The resources to remove from the publish list. */
        REMOVE_RESOURCES("remove-resources"),
        /** The resources to publish. */
        RESOURCES("resources"),
        /** Flag to indicate if to publish with siblings. */
        SIBLINGS("siblings");

        /** Parameter name. */
        private String m_name;

        /** Constructor.<p> */
        private ParamPublish(String name) {

            m_name = name;
        }

        /** 
         * Returns the name.<p>
         * 
         * @return the name
         */
        public String getName() {

            return m_name;
        }
    }

    /** The log object for this class. */
    private static final Log LOG = CmsLog.getLog(CmsADEPublishServer.class);

    /** The current cms context. */
    private CmsObject m_cms;

    /** The current JSP context. */
    private CmsJspActionElement m_jsp;

    /**
     * Constructor.<p>
     * 
     * @param jsp the current JSP context
     */
    public CmsADEPublishServer(CmsJspActionElement jsp) {

        m_jsp = jsp;
        m_cms = m_jsp.getCmsObject();
    }

    /**
     * Handles all publish related requests.<p>
     * 
     * @param action the action to carry out
     * @param result the JSON object for results
     * @param data the request data
     * 
     * @return JSON object
     * 
     * @throws CmsException if something goes wrong
     * @throws JSONException if something goes wrong
     */
    public JSONObject handleRequest(CmsADEServer.Action action, JSONObject result, JSONObject data)
    throws JSONException, CmsException {

        HttpServletRequest request = m_jsp.getRequest();

        // get the cached publish options
        CmsADESessionCache sessionCache = (CmsADESessionCache)request.getSession().getAttribute(
            CmsADESessionCache.SESSION_ATTR_ADE_CACHE);
        CmsPublishOptions options = sessionCache.getADEPublishOptions();
        if (action.equals(CmsADEServer.Action.PUBLISH_OPTIONS)) {
            result.merge(options.toJson(), true, false);
            return result;
        }

        // check possible option parameters
        if (checkParameters(data, null, ParamPublish.RELATED)) {
            options.setIncludeRelated(data.optBoolean(ParamPublish.RELATED.getName()));
        }
        if (checkParameters(data, null, ParamPublish.SIBLINGS)) {
            options.setIncludeSiblings(data.optBoolean(ParamPublish.SIBLINGS.getName()));
        }
        if (checkParameters(data, null, ParamPublish.PROJECT)) {
            String projectParam = data.optString(ParamPublish.PROJECT.getName());
            try {
                options.setProjectId(new CmsUUID(projectParam));
            } catch (NumberFormatException e) {
                LOG.warn(e.getLocalizedMessage(), e);
            }
        }

        CmsADEPublish publish = new CmsADEPublish(m_cms);
        // set options
        publish.getOptions().setIncludeRelated(options.isIncludeRelated());
        publish.getOptions().setIncludeSiblings(options.isIncludeSiblings());
        publish.getOptions().setProjectId(options.getProjectId());

        if (action.equals(CmsADEServer.Action.PUBLISH_LIST)) {
            if (data.has(ParamPublish.REMOVE_RESOURCES.getName())) {
                removeFromPublishList(publish, data.optJSONArray(ParamPublish.REMOVE_RESOURCES.getName()));
                // we continue to execute the main action
            }
            // get list of resources to publish
            JSONArray groupsToPublish = toJsonArray(publish.getPublishGroups());
            result.put(JsonResponse.GROUPS.getName(), groupsToPublish);
        } else if (action.equals(CmsADEServer.Action.PROJECTS)) {
            JSONArray manageableProjects = toJsonArray(publish.getManageableProjects());
            result.put(JsonResponse.PROJECTS.getName(), manageableProjects);
        } else if (action.equals(CmsADEServer.Action.PUBLISH)) {
            if (data.has(ParamPublish.REMOVE_RESOURCES.getName())) {
                removeFromPublishList(publish, data.optJSONArray(ParamPublish.REMOVE_RESOURCES.getName()));
                // we continue to execute the main action
            }
            if (!checkParameters(data, result, ParamPublish.RESOURCES)) {
                return result;
            }
            // save options
            sessionCache.setCacheADEPublishOptions(publish.getOptions());
            // resources to publish
            JSONArray idsToPublish = data.optJSONArray(ParamPublish.RESOURCES.getName());
            List<CmsResource> pubResources;
            try {
                pubResources = resourcesFromJson(idsToPublish);
            } catch (CmsException e) {
                // should never happen
                LOG.error(e.getLocalizedMessage(), e);
                result.put(CmsADEServer.JsonResponse.ERROR.getName(), e.getLocalizedMessage());
                return result;
            }
            Collections.sort(pubResources, I_CmsResource.COMPARE_DATE_LAST_MODIFIED);
            JSONArray resources = new JSONArray();
            if (!data.has(ParamPublish.FORCE.getName())
                || !data.optBoolean(ParamPublish.FORCE.getName())
                || !isCanPublish()) {
                // get the resources with link check problems
                resources = toJsonArray(publish.getBrokenResources(pubResources));
            }
            if (resources.length() == 0) {
                // publish resources
                publish.publishResources(pubResources);
            } else {
                // return resources with problems
                result.put(JsonResponse.RESOURCES.getName(), resources);
                // indicate if the user if allowed to publish anyhow
                result.put(JsonResponse.CANPUBLISH.getName(), isCanPublish());
            }
        }
        return result;
    }

    /**
     * Returns <code>true</code> if the current user is allowed 
     * to publish the selected resources.<p>
     * 
     * @return <code>true</code> if the current user is allowed 
     *          to publish the selected resources
     */
    public boolean isCanPublish() {

        return OpenCms.getWorkplaceManager().getDefaultUserSettings().isAllowBrokenRelations()
            || OpenCms.getRoleManager().hasRole(m_cms, CmsRole.VFS_MANAGER);
    }

    /**
     * Removes the resources from the user's publish list.<p>
     * 
     * @param publish the publish helper
     * @param resourcesToRemove the list of IDs of resources to remove
     * 
     * @throws CmsException if something goes wrong
     */
    public void removeFromPublishList(CmsADEPublish publish, JSONArray resourcesToRemove) throws CmsException {

        // remove the resources from the user's publish list
        Set<CmsUUID> idsToRemove = new HashSet<CmsUUID>();
        for (int i = 0; i < resourcesToRemove.length(); i++) {
            String id = resourcesToRemove.optString(i);
            idsToRemove.add(new CmsUUID(id));
        }
        publish.removeResourcesFromPublishList(idsToRemove);
    }

    /**
     * Removes the given resources from the user's publish list.<p>
     * 
     * @param ids list of structure ids identifying the resources to be removed
     * 
     * @throws CmsException if something goes wrong
     */
    public void removeResourcesFromPublishList(JSONArray ids) throws CmsException {

        Set<CmsUUID> idsToRemove = new HashSet<CmsUUID>();
        for (int i = 0; i < ids.length(); i++) {
            String id = ids.optString(i);
            idsToRemove.add(new CmsUUID(id));
        }

        OpenCms.getPublishManager().removeResourceFromUsersPubList(m_cms, idsToRemove);
    }

    /**
     * Checks whether a list of parameters are present as attributes of a request.<p>
     * 
     * If this isn't the case, an error message is written to the JSON result object.
     * 
     * @param request the request which contains the parameters
     * @param result the JSON object which the error message should be written into, can be <code>null</code>
     * @param params the array of parameters which should be checked
     * 
     * @return true if and only if all parameters are present in the request
     * 
     * @throws JSONException if something goes wrong with JSON
     */
    protected boolean checkParameters(HttpServletRequest request, JSONObject result, ParamPublish... params)
    throws JSONException {

        for (ParamPublish param : params) {
            String value = request.getParameter(param.getName());
            if (value == null) {
                if (result != null) {
                    result.put(CmsADEServer.JsonResponse.ERROR.getName(), Messages.get().getBundle().key(
                        Messages.ERR_JSON_MISSING_PARAMETER_1,
                        param.getName()));
                }
                return false;
            }
        }
        return true;
    }

    /**
     * Checks whether a list of parameters are present as attributes of a request.<p>
     * 
     * If this isn't the case, an error message is written to the JSON result object.
     * 
     * @param data the JSONObject data which contains the parameters
     * @param result the JSON object which the error message should be written into, can be <code>null</code>
     * @param params the array of parameters which should be checked
     * 
     * @return <code>true</code> if and only if all parameters are present in the given data
     * 
     * @throws JSONException if something goes wrong with JSON
     */
    protected boolean checkParameters(JSONObject data, JSONObject result, ParamPublish... params) throws JSONException {

        for (ParamPublish param : params) {
            if (!data.has(param.getName())) {
                if (result != null) {
                    result.put(CmsADEServer.JsonResponse.ERROR.getName(), Messages.get().getBundle().key(
                        Messages.ERR_JSON_MISSING_PARAMETER_1,
                        param.getName()));
                }
                return false;
            }
        }
        return true;
    }

    /**
     * Converts a JSON array of structure ids into a list of resources.<p>
     * 
     * @param ids the JSON array of structure ids
     * 
     * @return a list of resources
     * 
     * @throws CmsException if something goes wrong
     */
    protected List<CmsResource> resourcesFromJson(JSONArray ids) throws CmsException {

        List<CmsResource> resources = new ArrayList<CmsResource>(ids.length());
        for (int i = 0; i < ids.length(); i++) {
            resources.add(m_cms.readResource(new CmsUUID(ids.optString(i)), CmsResourceFilter.ALL));
        }
        return resources;
    }

    /**
     * Converts a list of objects into an JSON array.<p>
     * 
     * @param objects list to convert
     * 
     * @return the JSON array
     * 
     * @throws JSONException if something goes wrong
     */
    protected JSONArray toJsonArray(List<? extends I_CmsJsonifable> objects) throws JSONException {

        JSONArray json = new JSONArray();
        for (I_CmsJsonifable object : objects) {
            json.put(object.toJson());
        }
        return json;
    }
}
