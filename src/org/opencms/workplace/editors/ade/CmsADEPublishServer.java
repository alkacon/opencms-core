/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/workplace/editors/ade/Attic/CmsADEPublishServer.java,v $
 * Date   : $Date: 2010/01/26 11:00:56 $
 * Version: $Revision: 1.14 $
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
import org.opencms.main.CmsException;
import org.opencms.main.CmsLog;
import org.opencms.main.OpenCms;
import org.opencms.security.CmsRole;
import org.opencms.util.CmsUUID;
import org.opencms.workplace.A_CmsAjaxServer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.jsp.PageContext;

import org.apache.commons.logging.Log;

/**
 * Publish server.<p>
 * 
 * @author Michael Moossen 
 * 
 * @version $Revision: 1.14 $
 * 
 * @since 7.9.3
 */
public class CmsADEPublishServer extends A_CmsAjaxServer {

    /** Request parameter action value constants. */
    protected enum Action {

        /** To retrieve the manageable projects. */
        PROJECTS,
        /** To publish. */
        PUBLISH,
        /** To retrieve the publish list. */
        PUBLISH_LIST,
        /** To retrieve the stored publish options. */
        PUBLISH_OPTIONS;
    }

    /** Json property name constants for responses. */
    protected enum JsonResponse {

        /** Flag to indicate if the current user can publish. */
        canPublish,
        /** The list of groups. */
        groups,
        /** The list of projects. */
        projects,
        /** A list of resources. */
        resources;
    }

    /** Request parameter name constants for publishing. */
    protected enum ParamPublish {

        /** Flag to force publishing with broken links. */
        force,
        /** The project to publish. */
        project,
        /** Flag to indicate if to publish with related resources. */
        related,
        /** The resources to remove from the publish list. */
        removeResources,
        /** The resources to publish. */
        resources,
        /** Flag to indicate if to publish with siblings. */
        siblings;
    }

    /** Request parameter name constants. */
    protected enum ReqParam {

        /** The action of execute. */
        action,
        /** Generic data parameter. */
        data;
    }

    /** The log object for this class. */
    private static final Log LOG = CmsLog.getLog(CmsADEPublishServer.class);

    /**
     * Constructor, with parameters.
     * 
     * @param context the JSP page context object
     * @param req the JSP request 
     * @param res the JSP response 
     */
    public CmsADEPublishServer(PageContext context, HttpServletRequest req, HttpServletResponse res) {

        super(context, req, res);
    }

    /**
     * Handles all publish related requests.<p>
     * 
     * @return the result
     * 
     * @throws JSONException if there is any problem with JSON
     * @throws CmsException if there is a problem with the cms context
     */
    @Override
    public JSONObject executeAction() throws CmsException, JSONException {

        JSONObject result = new JSONObject();

        HttpServletRequest request = getRequest();

        if (!checkParameters(request, result, ReqParam.action.name())) {
            // every request needs to have at least these parameters 
            return result;
        }
        String actionParam = request.getParameter(ReqParam.action.name());
        Action action = Action.valueOf(actionParam.toUpperCase());
        JSONObject data = new JSONObject();
        if (checkParameters(request, null, ReqParam.data.name())) {
            String dataParam = request.getParameter(ReqParam.data.name());
            data = new JSONObject(dataParam);
        }

        // get the cached publish options
        CmsADESessionCache sessionCache = (CmsADESessionCache)request.getSession().getAttribute(
            CmsADESessionCache.SESSION_ATTR_ADE_CACHE);
        if (sessionCache == null) {
            sessionCache = new CmsADESessionCache(getCmsObject());
            request.getSession().setAttribute(CmsADESessionCache.SESSION_ATTR_ADE_CACHE, sessionCache);
        }
        CmsPublishOptions options = sessionCache.getPublishOptions();
        if (action.equals(Action.PUBLISH_OPTIONS)) {
            result.merge(options.toJson(), true, false);
            return result;
        }

        // check possible option parameters
        if (checkParameters(data, null, ParamPublish.related.name())) {
            options.setIncludeRelated(data.optBoolean(ParamPublish.related.name()));
        }
        if (checkParameters(data, null, ParamPublish.siblings.name())) {
            options.setIncludeSiblings(data.optBoolean(ParamPublish.siblings.name()));
        }
        if (checkParameters(data, null, ParamPublish.project.name())) {
            String projectParam = data.optString(ParamPublish.project.name());
            try {
                options.setProjectId(new CmsUUID(projectParam));
            } catch (NumberFormatException e) {
                options.setProjectId(null);
                LOG.warn(e.getLocalizedMessage(), e);
            }
        } else {
            options.setProjectId(null);
        }

        CmsObject cms = getCmsObject();
        cms.getRequestContext().setLocale(getWorkplaceLocale());
        CmsADEPublish publish = new CmsADEPublish(cms, request, getResponse());
        // set options
        publish.getOptions().setIncludeRelated(options.isIncludeRelated());
        publish.getOptions().setIncludeSiblings(options.isIncludeSiblings());
        publish.getOptions().setProjectId(options.getProjectId());

        if (action.equals(Action.PUBLISH_LIST)) {
            if (data.has(ParamPublish.removeResources.name())) {
                removeFromPublishList(publish, data.optJSONArray(ParamPublish.removeResources.name()));
                // we continue to execute the main action
            }
            // get list of resources to publish
            JSONArray groupsToPublish = toJsonArray(publish.getPublishGroups());
            result.put(JsonResponse.groups.name(), groupsToPublish);
        } else if (action.equals(Action.PROJECTS)) {
            JSONArray manageableProjects = toJsonArray(publish.getManageableProjects());
            result.put(JsonResponse.projects.name(), manageableProjects);
        } else if (action.equals(Action.PUBLISH)) {
            if (data.has(ParamPublish.removeResources.name())) {
                removeFromPublishList(publish, data.optJSONArray(ParamPublish.removeResources.name()));
                // we continue to execute the main action
            }
            if (!checkParameters(data, result, ParamPublish.resources.name())) {
                return result;
            }
            // save options
            sessionCache.setCachePublishOptions(new CmsPublishOptions(
                publish.getOptions().isIncludeRelated(),
                publish.getOptions().isIncludeSiblings(),
                publish.getOptions().getProjectId()));
            // resources to publish
            JSONArray idsToPublish = data.optJSONArray(ParamPublish.resources.name());
            List<CmsResource> pubResources;
            try {
                pubResources = resourcesFromJson(idsToPublish);
            } catch (CmsException e) {
                // should never happen
                error(result, e.getLocalizedMessage());
                return result;
            }
            Collections.sort(pubResources, I_CmsResource.COMPARE_DATE_LAST_MODIFIED);
            JSONArray resources = new JSONArray();
            if (!data.has(ParamPublish.force.name()) || !data.optBoolean(ParamPublish.force.name()) || !isCanPublish()) {
                // get the resources with link check problems
                resources = toJsonArray(publish.getBrokenResources(pubResources));
            }
            if (resources.length() == 0) {
                // publish resources
                publish.publishResources(pubResources);
            } else {
                // return resources with problems
                result.put(JsonResponse.resources.name(), resources);
                // indicate if the user if allowed to publish anyhow
                result.put(JsonResponse.canPublish.name(), isCanPublish());
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
            || OpenCms.getRoleManager().hasRole(getCmsObject(), CmsRole.VFS_MANAGER);
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

        OpenCms.getPublishManager().removeResourceFromUsersPubList(getCmsObject(), idsToRemove);
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
            resources.add(getCmsObject().readResource(new CmsUUID(ids.optString(i)), CmsResourceFilter.ALL));
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
