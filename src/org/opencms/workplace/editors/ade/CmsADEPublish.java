/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/workplace/editors/ade/Attic/CmsADEPublish.java,v $
 * Date   : $Date: 2009/10/27 11:44:43 $
 * Version: $Revision: 1.1.2.4 $
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

import org.opencms.db.CmsPublishList;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsResource;
import org.opencms.file.CmsResourceFilter;
import org.opencms.file.CmsUser;
import org.opencms.json.JSONArray;
import org.opencms.json.JSONException;
import org.opencms.json.JSONObject;
import org.opencms.jsp.CmsJspActionElement;
import org.opencms.lock.CmsLockFilter;
import org.opencms.main.CmsException;
import org.opencms.main.CmsLog;
import org.opencms.main.OpenCms;
import org.opencms.relations.CmsRelation;
import org.opencms.relations.CmsRelationFilter;
import org.opencms.relations.CmsRelationPublishValidator;
import org.opencms.relations.CmsRelationValidatorInfoEntry;
import org.opencms.report.CmsShellReport;
import org.opencms.report.I_CmsReport;
import org.opencms.security.CmsPermissionSet;
import org.opencms.util.CmsStringUtil;
import org.opencms.util.CmsUUID;
import org.opencms.workplace.CmsWorkplace;
import org.opencms.workplace.explorer.CmsResourceUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.logging.Log;

/**
 * ADE publishing features.<p>
 * 
 * @author Michael Moossen 
 * 
 * @version $Revision: 1.1.2.4 $
 * 
 * @since 7.9.3
 */
public class CmsADEPublish {

    /** Reason value constants, when resources can not be published. */
    protected enum BlockingReason {

        /** Resource is locked by another user. */
        LOCKED("locked"),
        /** User does not have enough permissions. */
        PERMISSIONS("permissions"),
        /** Resource has been already published. */
        PUBLISHED("published");

        /** Property name. */
        private String m_name;

        /** Constructor.<p> */
        private BlockingReason(String name) {

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

    /** Dialog value constants for responses. */
    protected enum Dialog {

        /** Dialog with blocked resources. */
        BLOCKED("blocked"),
        /** Publishing has been successfully started. */
        FINISHED("finished"),
        /** Dialog with broken links. */
        LINKCHECK("linkcheck"),
        /** Dialog with resources to publish. */
        PUBLISH("publish");

        /** Property name. */
        private String m_name;

        /** Constructor.<p> */
        private Dialog(String name) {

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

    /** Json property name constants for resources. */
    protected enum JsonResource {

        /** The resource type icon path. */
        ICON("icon"),
        /** The structure id. */
        ID("id"),
        /** The additional information name. */
        INFO_NAME("infoName"),
        /** The additional information value. */
        INFO_VALUE("infoValue"),
        /** The name of the user that has locked the resource. */
        LOCKED_BY("lockedBy"),
        /** The reason a resource can not be published. */
        REASON("reason"),
        /** The resource state. */
        STATE("state"),
        /** Resource title. */
        TITLE("title"),
        /** Resource uri. */
        URI("uri");

        /** Property name. */
        private String m_name;

        /** Constructor.<p> */
        private JsonResource(String name) {

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

    /** Json property name constants for responses. */
    protected enum JsonResponse {

        /** The response state. */
        DIALOG("dialog"),
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

    /**
     * Just for passing around resources and their related together but not mixed up.<p>
     */
    private class ResourcesAndRelated {

        /** The related resources. */
        private Set<CmsResource> m_relatedResources = new HashSet<CmsResource>();

        /** The resources. */
        private Set<CmsResource> m_resources = new HashSet<CmsResource>();

        /**
         * Constructor.<p>
         */
        public ResourcesAndRelated() {

            // empty
        }

        /**
         * Checks if the given resource is present in at least one of the sets.<p>
         * 
         * @param resource the resource to test
         * 
         * @return <code>true</code> if the given resource is present in at least one of the sets
         */
        public boolean contains(CmsResource resource) {

            return m_resources.contains(resource) || m_relatedResources.contains(resource);
        }

        /**
         * Returns the related resources.<p>
         *
         * @return the related resources
         */
        public Set<CmsResource> getRelatedResources() {

            return m_relatedResources;
        }

        /**
         * Returns the resources.<p>
         *
         * @return the resources
         */
        public Set<CmsResource> getResources() {

            return m_resources;
        }
    }

    /** Formatted path length. */
    protected static final int PATH_LENGTH = 50;

    /** The log object for this class. */
    private static final Log LOG = CmsLog.getLog(CmsADEPublish.class);

    /** Flag to include related resources. */
    private boolean m_includeRelated = true;

    /** Flag to include siblings. */
    private boolean m_includeSiblings;

    /** The current JSP context. */
    private CmsJspActionElement m_jsp;

    /** The user's resource publish list. */
    private ResourcesAndRelated m_resourceList;

    /**
     * Constructor.<p>
     * 
     * @param jsp the current JSP context
     */
    public CmsADEPublish(CmsJspActionElement jsp) {

        m_jsp = jsp;
    }

    /**
     * Returns a list of resources that should be published but can not.<p>
     * This can be due to several reasons:<br>
     * <ul>
     *  <li>Locks</li>
     *  <li>Permissions</li>
     *  <li>Already published</li>
     * </ul>
     * 
     * @return a list of resources that should be published but can not
     * 
     * @throws JSONException if something goes wrong 
     * 
     * @see org.opencms.workplace.commons.CmsLock#getBlockingLockedResources()
     * @see JsonResource#REASON
     * @see BlockingReason
     */
    public JSONArray getJsonBlockedResources() throws JSONException {

        CmsObject cms = getCmsObject();
        Locale locale = cms.getRequestContext().getLocale();

        JSONArray resources = new JSONArray();

        // first look for already published resources
        Set<CmsResource> published = getAlreadyPublishedResources();
        for (CmsResource resource : published) {
            JSONObject jsonRes = resourceToJson(resource);
            jsonRes.put(JsonResource.REASON.getName(), BlockingReason.PUBLISHED.getName());
            resources.put(jsonRes);
        }

        // then for resources without permission
        Set<CmsResource> exclude = new HashSet<CmsResource>(published);

        ResourcesAndRelated permissions = getResourcesWithoutPermissions(exclude);
        for (CmsResource resource : permissions.getResources()) {
            JSONObject jsonRes = resourceToJson(resource);
            jsonRes.put(JsonResource.REASON.getName(), BlockingReason.PERMISSIONS.getName());
            resources.put(jsonRes);
        }

        // and finally for locked resources
        exclude.addAll(permissions.getResources());
        exclude.addAll(permissions.getRelatedResources());

        ResourcesAndRelated locked = getBlockingLockedResources(exclude);
        for (CmsResource resource : locked.getResources()) {
            JSONObject jsonRes = resourceToJson(resource);
            jsonRes.put(JsonResource.REASON.getName(), BlockingReason.LOCKED.getName());
            resources.put(jsonRes);
        }

        // update the publish list
        exclude.clear();
        exclude.addAll(published);
        exclude.addAll(permissions.getResources());
        exclude.addAll(locked.getResources());

        m_resourceList.getResources().removeAll(exclude);
        m_resourceList.getRelatedResources().removeAll(permissions.getRelatedResources());
        m_resourceList.getRelatedResources().removeAll(locked.getRelatedResources());

        // at the end process related resources
        String relatedMsg = org.opencms.workplace.commons.Messages.get().getBundle(locale).key(
            org.opencms.workplace.commons.Messages.GUI_PUBLISH_DETAIL_REVERSE_REFERENCE_0);
        for (CmsResource resource : permissions.getRelatedResources()) {
            if (exclude.contains(resource)) {
                // skip related, because it is already in the 'normal' list
                continue;
            }
            JSONArray infoForRelated = getInfoForRelated(cms, locale, resource);
            if (infoForRelated == null) {
                // skip related, if source is already blocking
                continue;
            }
            JSONObject jsonRes = resourceToJson(resource);
            jsonRes.put(JsonResource.REASON.getName(), BlockingReason.PERMISSIONS.getName());
            jsonRes.put(JsonResource.INFO_NAME.getName(), relatedMsg);
            jsonRes.put(JsonResource.INFO_VALUE.getName(), infoForRelated);
            resources.put(jsonRes);
        }
        for (CmsResource resource : locked.getRelatedResources()) {
            if (exclude.contains(resource)) {
                // skip related, because it is already in the 'normal' list
                continue;
            }
            JSONArray infoForRelated = getInfoForRelated(cms, locale, resource);
            if (infoForRelated == null) {
                // skip related, if source is already blocking
                continue;
            }
            JSONObject jsonRes = resourceToJson(resource);
            jsonRes.put(JsonResource.REASON.getName(), BlockingReason.LOCKED.getName());
            jsonRes.put(JsonResource.INFO_NAME.getName(), relatedMsg);
            jsonRes.put(JsonResource.INFO_VALUE.getName(), infoForRelated);
            resources.put(jsonRes);
        }

        return resources;
    }

    /**
     * Checks for possible broken links when the given list of resources would be published.<p>
     * 
     * @param ids list of structure ids identifying the resources to be published
     * 
     * @return a sublist of JSON resources that would produce broken links when published 
     * 
     * @throws JSONException if something goes wrong 
     * @throws CmsException if something goes wrong 
     */
    public JSONArray getJsonBrokenResources(JSONArray ids) throws JSONException, CmsException {

        JSONArray resources = new JSONArray();

        CmsObject cms = getCmsObject();
        Locale locale = cms.getRequestContext().getLocale();

        String sourceMsg = org.opencms.workplace.commons.Messages.get().getBundle(locale).key(
            org.opencms.workplace.commons.Messages.GUI_PUBLISH_BROKENRELATIONS_DETAIL_RELATION_SOURCES_0);
        String targetMsg = org.opencms.workplace.commons.Messages.get().getBundle(locale).key(
            org.opencms.workplace.commons.Messages.GUI_PUBLISH_BROKENRELATIONS_DETAIL_RELATION_TARGETS_0);
        CmsPublishList publishList = new CmsPublishList(resourcesFromJson(ids), m_includeSiblings, false);
        CmsRelationPublishValidator validator = new CmsRelationPublishValidator(cms, publishList);
        List<String> resourceList = new ArrayList<String>(validator.keySet());
        Collections.sort(resourceList);
        for (String resourceName : validator.keySet()) {
            CmsRelationValidatorInfoEntry infoEntry = validator.getInfoEntry(resourceName);
            CmsResource resource = cms.readResource(resourceName);
            JSONArray info = getInfoForLinkCheck(cms, resource, infoEntry);
            JSONObject jsonRes = resourceToJson(resource);
            if (info != null) {
                if (resource.getState().isDeleted()) {
                    jsonRes.put(JsonResource.INFO_NAME.getName(), sourceMsg);
                } else {
                    jsonRes.put(JsonResource.INFO_NAME.getName(), targetMsg);
                }
                jsonRes.put(JsonResource.INFO_VALUE.getName(), info);
            }
            resources.put(jsonRes);
        }

        return resources;
    }

    /**
     * Returns the list of resources that can be published.<p>
     * 
     * @return a list of resources that can be published
     * 
     * @throws JSONException if something goes wrong 
     */
    public JSONArray getJsonPublishResources() throws JSONException {

        JSONArray resources = new JSONArray();

        CmsObject cms = getCmsObject();
        Locale locale = cms.getRequestContext().getLocale();

        ResourcesAndRelated pubResources = getPublishResources();

        String sourceMsg = org.opencms.workplace.commons.Messages.get().getBundle(locale).key(
            org.opencms.workplace.commons.Messages.GUI_PUBLISH_DETAIL_RELATED_RESOURCE_0);
        for (CmsResource resource : pubResources.getResources()) {
            JSONArray infoForSource = getInfoForSource(cms, resource);
            JSONObject jsonRes = resourceToJson(resource);
            if (infoForSource != null) {
                jsonRes.put(JsonResource.INFO_NAME.getName(), sourceMsg);
                jsonRes.put(JsonResource.INFO_VALUE.getName(), infoForSource);
            }
            resources.put(jsonRes);
        }

        String relatedMsg = org.opencms.workplace.commons.Messages.get().getBundle(locale).key(
            org.opencms.workplace.commons.Messages.GUI_PUBLISH_DETAIL_REVERSE_REFERENCE_0);
        for (CmsResource resource : pubResources.getRelatedResources()) {
            JSONArray infoForRelated = getInfoForRelated(cms, locale, resource);
            if (infoForRelated == null) {
                // skip related, if source is not going to be published
                continue;
            }
            JSONObject jsonRes = resourceToJson(resource);
            jsonRes.put(JsonResource.INFO_NAME.getName(), relatedMsg);
            jsonRes.put(JsonResource.INFO_VALUE.getName(), infoForRelated);
            resources.put(jsonRes);
        }

        return resources;
    }

    /**
      * Handles all publish related requests.<p>
      * 
      * @param action the action to carry out
      * @param result the JSON object for results
      * 
      * @return JSON object
      * 
      * @throws CmsException if something goes wrong
      * @throws JSONException if something goes wrong
      */
    public JSONObject handleRequest(CmsADEServer.Action action, JSONObject result) throws JSONException, CmsException {

        HttpServletRequest request = m_jsp.getRequest();
        if (action.equals(CmsADEServer.Action.PUBLISH_LIST)) {
            if (checkParameters(request, null, ParamPublish.REMOVE_RESOURCES)) {
                // remove the resources from the user's publish list
                String remResParam = request.getParameter(ParamPublish.REMOVE_RESOURCES.getName());
                JSONArray resourcesToRemove = new JSONArray(remResParam);
                removeResourcesFromPublishList(resourcesToRemove);
                // we continue to execute the main action
            }
            if (checkParameters(request, null, ParamPublish.RELATED, ParamPublish.SIBLINGS)) {
                // get list of resources to publish
                String relatedParam = request.getParameter(ParamPublish.RELATED.getName());
                String siblingsParam = request.getParameter(ParamPublish.SIBLINGS.getName());
                m_includeRelated = Boolean.parseBoolean(relatedParam);
                m_includeSiblings = Boolean.parseBoolean(siblingsParam);
                JSONArray resourcesToPublish = getJsonPublishResources();
                result.put(JsonResponse.RESOURCES.getName(), resourcesToPublish);
                result.put(JsonResponse.DIALOG.getName(), Dialog.PUBLISH.getName());
            } else {
                JSONArray resources = getJsonBlockedResources();
                if (resources.length() == 0) {
                    // if no problems just get the list of resources to publish
                    resources = getJsonPublishResources();
                    result.put(JsonResponse.DIALOG.getName(), Dialog.PUBLISH.getName());
                } else {
                    result.put(JsonResponse.DIALOG.getName(), Dialog.BLOCKED.getName());
                }
                result.put(JsonResponse.RESOURCES.getName(), resources);
            }
        } else if (action.equals(CmsADEServer.Action.PUBLISH)) {
            if (!checkParameters(request, result, ParamPublish.RESOURCES)) {
                return result;
            }
            // resources to publish
            String resourcesParam = request.getParameter(ParamPublish.RESOURCES.getName());
            JSONArray resourcesToPublish = new JSONArray(resourcesParam);
            // get the resources with link check problems
            JSONArray resources = getJsonBrokenResources(resourcesToPublish);
            if (resources.length() == 0) {
                // publish resources
                publishResources(resourcesToPublish);
                result.put(JsonResponse.DIALOG.getName(), Dialog.FINISHED.getName());
            } else {
                // return resources with problems
                result.put(JsonResponse.RESOURCES.getName(), resources);
                result.put(JsonResponse.DIALOG.getName(), Dialog.LINKCHECK.getName());
            }
        }
        return result;
    }

    /**
     * Publishes the given list of resources.<p>
     * 
     * @param ids list of structure ids identifying the resources to publish
     * 
     * @throws CmsException if something goes wrong
     */
    public void publishResources(JSONArray ids) throws CmsException {

        List<CmsResource> resources = resourcesFromJson(ids);
        I_CmsReport report = new CmsShellReport(getCmsObject().getRequestContext().getLocale());
        CmsPublishList publishList = new CmsPublishList(resources, m_includeSiblings, false);
        OpenCms.getPublishManager().publishProject(getCmsObject(), report, publishList);
        removeResourcesFromPublishList(ids);
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
     * Returns already published resources.<p>
     * 
     * @return already published resources
     */
    protected Set<CmsResource> getAlreadyPublishedResources() {

        Set<CmsResource> resources = new HashSet<CmsResource>();
        for (CmsResource resource : getPublishResources().getResources()) {
            // we are interested just in changed resources
            if (!resource.getState().isUnchanged()) {
                continue;
            }
            resources.add(resource);
        }
        return resources;
    }

    /**
     * Returns locked resources that do not belong to the current user.<p>
     * 
     * @param exclude the resources to exclude
     * 
     * @return the locked and related resources
     * 
     * @see org.opencms.workplace.commons.CmsLock#getBlockingLockedResources
     */
    protected ResourcesAndRelated getBlockingLockedResources(Set<CmsResource> exclude) {

        CmsObject cms = getCmsObject();

        CmsUser user = cms.getRequestContext().currentUser();
        CmsLockFilter blockingFilter = CmsLockFilter.FILTER_ALL;
        blockingFilter = blockingFilter.filterNotLockableByUser(user);

        ResourcesAndRelated result = new ResourcesAndRelated();
        for (CmsResource resource : getPublishResources().getResources()) {
            // skip already blocking resources
            if (exclude.contains(resource)) {
                continue;
            }
            try {
                result.getResources().addAll(cms.getLockedResources(resource, blockingFilter));
            } catch (Exception e) {
                // error reading the resource list, should usually never happen
                if (LOG.isErrorEnabled()) {
                    LOG.error(e.getLocalizedMessage(), e);
                }
            }
        }
        for (CmsResource resource : getPublishResources().getRelatedResources()) {
            // skip already blocking resources
            if (exclude.contains(resource)) {
                continue;
            }
            try {
                result.getRelatedResources().addAll(cms.getLockedResources(resource, blockingFilter));
            } catch (Exception e) {
                // error reading the resource list, should usually never happen
                if (LOG.isErrorEnabled()) {
                    LOG.error(e.getLocalizedMessage(), e);
                }
            }
        }
        return result;
    }

    /**
     * Returns the current cms context.<p>
     * 
     * @return the current cms context
     */
    protected CmsObject getCmsObject() {

        return m_jsp.getCmsObject();
    }

    /**
     * Returns the links that would get broken when publishing the given resource.<p>
     * 
     * @param cms the cms context
     * @param resource the resource to check
     * @param infoEntry the link validation information
     * 
     * @return the links that would get broken when publishing the given resource
     */
    protected JSONArray getInfoForLinkCheck(CmsObject cms, CmsResource resource, CmsRelationValidatorInfoEntry infoEntry) {

        // show all links that will get broken
        JSONArray infoValue = new JSONArray();
        for (CmsRelation relation : infoEntry.getRelations()) {
            String relationName;
            if (resource.getState().isDeleted()) {
                relationName = relation.getSourcePath();
            } else {
                relationName = relation.getTargetPath();
            }
            String siteRoot = infoEntry.getSiteRoot();
            infoValue.put(getResourceName(cms, relationName, siteRoot));
        }
        return infoValue.length() > 0 ? infoValue : null;
    }

    /**
     * Returns a string with a list of resources in the publish list, this resource is related to.<p>
     * 
     * @param cms the current cms context
     * @param locale the current locale
     * @param resource the related resource to use
     * 
     * @return a string with a list of resources in the publish list, this resource is related to, or <code>null</code> if none
     */
    protected JSONArray getInfoForRelated(CmsObject cms, Locale locale, CmsResource resource) {

        JSONArray infoValue = new JSONArray();
        try {
            String siteRoot = cms.getRequestContext().getSiteRoot();
            Set<CmsResource> resources = getPublishResources().getResources();
            // get and iterate over all related resources
            for (CmsRelation relation : cms.getRelationsForResource(
                resource,
                CmsRelationFilter.SOURCES.filterStrong().filterIncludeChildren())) {

                CmsResource source = null;
                try {
                    source = relation.getSource(cms, CmsResourceFilter.ALL);
                } catch (CmsException e) {
                    // error reading a resource, should usually never happen
                    if (LOG.isErrorEnabled()) {
                        LOG.error(e.getLocalizedMessage(), e);
                    }
                    continue;
                }
                // see if the source is a resource to be published
                if (resources.contains(source)) {
                    // HACK: writing HTML code :( 
                    infoValue.put(getResourceName(cms, source.getRootPath(), siteRoot)
                        + "&nbsp;<span style='color: #666666;'>("
                        + relation.getType().getLocalizedName(locale)
                        + ")</span>");
                }
            }
        } catch (CmsException e) {
            // error reading a resource relations, should usually never happen
            if (LOG.isErrorEnabled()) {
                LOG.error(e.getLocalizedMessage(), e);
            }
        }
        return infoValue.length() > 0 ? infoValue : null;
    }

    /**
     * Returns a string with a list of related resources in the publish list.<p>
     * 
     * @param cms the current cms context
     * @param resource the resource to use
     * 
     * @return a string with a list of related resources in the publish list, or <code>null</code> if none
     */
    protected JSONArray getInfoForSource(CmsObject cms, CmsResource resource) {

        JSONArray infoValue = new JSONArray();
        try {
            String siteRoot = cms.getRequestContext().getSiteRoot();
            Set<CmsResource> resources = getPublishResources().getRelatedResources();
            // get and iterate over all related resources
            for (CmsRelation relation : cms.getRelationsForResource(
                resource,
                CmsRelationFilter.TARGETS.filterStrong().filterIncludeChildren())) {

                CmsResource target = null;
                try {
                    target = relation.getTarget(cms, CmsResourceFilter.ALL);
                } catch (CmsException e) {
                    // error reading a resource, should usually never happen
                    if (LOG.isErrorEnabled()) {
                        LOG.error(e.getLocalizedMessage(), e);
                    }
                    continue;
                }
                // see if the source is a resource to be published
                if (resources.contains(target)) {
                    infoValue.put(getResourceName(cms, target.getRootPath(), siteRoot));
                } else if (!target.getState().isUnchanged()) {
                    // a modified related resource wont be published
                    // HACK: writing HTML code :( 
                    infoValue.put("<span style='color: red;'>"
                        + getResourceName(cms, target.getRootPath(), siteRoot)
                        + "</span>");
                }
            }
        } catch (CmsException e) {
            // error reading a resource relations, should usually never happen
            if (LOG.isErrorEnabled()) {
                LOG.error(e.getLocalizedMessage(), e);
            }
        }
        return infoValue.length() > 0 ? infoValue : null;
    }

    /**
     * Returns the resources stored in the user's publish list.<p>
     * 
     * @return the resources stored in the user's publish list
     */
    protected ResourcesAndRelated getPublishResources() {

        CmsObject cms = getCmsObject();
        if (m_resourceList != null) {
            return m_resourceList;
        }
        m_resourceList = new ResourcesAndRelated();
        try {
            m_resourceList.getResources().addAll(OpenCms.getPublishManager().getUsersPubList(cms));
        } catch (CmsException e) {
            // error reading the publish list, should usually never happen
            if (LOG.isErrorEnabled()) {
                LOG.error(e.getLocalizedMessage(), e);
            }
            return m_resourceList;
        }
        if (m_includeSiblings) {
            for (CmsResource resource : new HashSet<CmsResource>(m_resourceList.getResources())) {
                // we are interested just in changed resources
                if (resource.getState().isUnchanged()) {
                    continue;
                }
                try {
                    m_resourceList.getResources().addAll(
                        cms.readSiblings(cms.getSitePath(resource), CmsResourceFilter.ALL_MODIFIED));
                } catch (CmsException e) {
                    // error reading resource siblings, should usually never happen
                    if (LOG.isErrorEnabled()) {
                        LOG.error(e.getLocalizedMessage(), e);
                    }
                    continue;
                }
            }
        }
        if (m_includeRelated) {
            for (CmsResource resource : m_resourceList.getResources()) {
                // we are interested just in changed resources
                if (resource.getState().isUnchanged()) {
                    continue;
                }
                try {
                    // get and iterate over all related resources
                    for (CmsRelation relation : cms.getRelationsForResource(
                        resource,
                        CmsRelationFilter.TARGETS.filterStrong().filterIncludeChildren())) {

                        CmsResource target = null;
                        try {
                            target = relation.getTarget(cms, CmsResourceFilter.ALL);
                        } catch (CmsException e) {
                            // error reading a resource, should usually never happen
                            if (LOG.isErrorEnabled()) {
                                LOG.error(e.getLocalizedMessage(), e);
                            }
                            continue;
                        }
                        // we are interested just in changed resources
                        if (target.getState().isUnchanged()) {
                            continue;
                        }
                        // if already selected
                        if (m_resourceList.contains(target)) {
                            continue;
                        }
                        m_resourceList.getRelatedResources().add(target);
                    }
                } catch (CmsException e) {
                    // error reading a resource relations, should usually never happen
                    if (LOG.isErrorEnabled()) {
                        LOG.error(e.getLocalizedMessage(), e);
                    }
                    continue;
                }
            }
        }
        return m_resourceList;
    }

    /**
     * Formats the given resource path depending on the site root.<p>
     * 
     * @param cms the cms context
     * @param rootPath the resource path to format
     * @param siteRoot the site root
     * 
     * @return the formatted resource path
     */
    protected String getResourceName(CmsObject cms, String rootPath, String siteRoot) {

        Locale locale = cms.getRequestContext().getLocale();
        if (rootPath.startsWith(siteRoot)) {
            // same site
            rootPath = rootPath.substring(siteRoot.length());
            rootPath = CmsStringUtil.formatResourceName(rootPath, PATH_LENGTH);
        } else {
            // other site
            String site = OpenCms.getSiteManager().getSiteRoot(rootPath);
            String siteName = site;
            if (site != null) {
                rootPath = rootPath.substring(site.length());
                siteName = OpenCms.getSiteManager().getSiteForSiteRoot(site).getTitle();
            } else {
                siteName = "/";
            }
            rootPath = CmsStringUtil.formatResourceName(rootPath, PATH_LENGTH);
            rootPath = org.opencms.workplace.commons.Messages.get().getBundle(locale).key(
                org.opencms.workplace.commons.Messages.GUI_PUBLISH_SITE_RELATION_2,
                new Object[] {siteName, rootPath});
        }
        return rootPath;
    }

    /**
     * Returns the sublist of the publish list with resources without publish permissions.<p>
     * 
     * @param exclude the resources to exclude
     * 
     * @return the list with resources without publish permissions
     */
    protected ResourcesAndRelated getResourcesWithoutPermissions(Set<CmsResource> exclude) {

        CmsObject cms = getCmsObject();
        ResourcesAndRelated result = new ResourcesAndRelated();
        for (CmsResource resource : getPublishResources().getResources()) {
            // skip already blocking resources
            if (exclude.contains(resource)) {
                continue;
            }
            try {
                if (!cms.hasPermissions(resource, CmsPermissionSet.ACCESS_DIRECT_PUBLISH)) {
                    result.getResources().add(resource);
                }
            } catch (Exception e) {
                // error reading the permissions, should usually never happen
                if (LOG.isErrorEnabled()) {
                    LOG.error(e.getLocalizedMessage(), e);
                }
            }
        }
        for (CmsResource resource : getPublishResources().getRelatedResources()) {
            // skip already blocking resources
            if (exclude.contains(resource)) {
                continue;
            }
            try {
                if (!cms.hasPermissions(resource, CmsPermissionSet.ACCESS_DIRECT_PUBLISH)) {
                    result.getRelatedResources().add(resource);
                }
            } catch (Exception e) {
                // error reading the resource list, should usually never happen
                if (LOG.isErrorEnabled()) {
                    LOG.error(e.getLocalizedMessage(), e);
                }
            }
        }
        return result;
    }

    /**
     * Removes the given resources from the user's publish list.<p>
     * 
     * @param ids list of structure ids identifying the resources to be removed
     * 
     * @throws CmsException if something goes wrong
     */
    protected void removeResourcesFromPublishList(JSONArray ids) throws CmsException {

        CmsObject cms = getCmsObject();

        Set<CmsUUID> idsToRemove = new HashSet<CmsUUID>();
        for (int i = 0; i < ids.length(); i++) {
            String id = ids.optString(i);
            idsToRemove.add(new CmsUUID(id));
        }

        OpenCms.getPublishManager().removeResourceFromUsersPubList(cms, idsToRemove);
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
        CmsObject cms = getCmsObject();
        for (int i = 0; i < ids.length(); i++) {
            resources.add(cms.readResource(new CmsUUID(ids.optString(i)), CmsResourceFilter.ALL));
        }
        return resources;
    }

    /**
     * Converts a resource to a JSON object.<p>
     * 
     * @param resource the resource to convert
     * 
     * @return a JSON object representing the resource
     * 
     * @throws JSONException if something goes wrong
     */
    protected JSONObject resourceToJson(CmsResource resource) throws JSONException {

        CmsObject cms = getCmsObject();
        JSONObject jsonRes = new JSONObject();
        CmsResourceUtil resUtil = new CmsResourceUtil(cms, resource);
        jsonRes.put(JsonResource.ID.getName(), resource.getStructureId());
        jsonRes.put(JsonResource.URI.getName(), CmsStringUtil.formatResourceName(resUtil.getFullPath(), PATH_LENGTH));
        jsonRes.put(JsonResource.TITLE.getName(), resUtil.getTitle());
        jsonRes.put(JsonResource.ICON.getName(), CmsWorkplace.getResourceUri(resUtil.getIconPathExplorer()));
        jsonRes.put(JsonResource.STATE.getName(), "" + resUtil.getStateAbbreviation());
        jsonRes.put(JsonResource.LOCKED_BY.getName(), resUtil.getLockedByName());
        return jsonRes;
    }
}
