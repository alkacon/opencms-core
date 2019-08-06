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

package org.opencms.ui.favorites;

import org.opencms.file.CmsObject;
import org.opencms.file.CmsProject;
import org.opencms.file.CmsResource;
import org.opencms.file.CmsResourceFilter;
import org.opencms.json.JSONException;
import org.opencms.json.JSONObject;
import org.opencms.main.CmsException;
import org.opencms.main.OpenCms;
import org.opencms.ui.A_CmsUI;
import org.opencms.ui.CmsVaadinUtils;
import org.opencms.ui.apps.A_CmsWorkplaceApp;
import org.opencms.ui.apps.CmsFileExplorerConfiguration;
import org.opencms.util.CmsStringUtil;
import org.opencms.util.CmsUUID;

/**
 * Represents an entry in the favorite location list.
 */
public class CmsFavoriteEntry {

    /**
     * Represents the type of the favorite.
     */
    public enum Type {
        /** Container page editor favorite. */
        explorerFolder("f"),

        /** Page favorite. */
        page("p");

        /** String representing this type in the JSON format. */
        private String m_jsonId;

        /**
         * Creates new value.
         *
         * @param jsonId the JSON id.
         */
        private Type(String jsonId) {

            m_jsonId = jsonId;

        }

        /**
         * Converts JSON id to correct type.
         *
         * @param id the JSON id
         * @return the corresponding type
         */
        public static Type fromJsonId(String id) {

            for (Type type : Type.values()) {
                if (type.getJsonId().equals(id)) {
                    return type;
                }
            }
            return null;
        }

        /**
         * Gets the JSON id for the type.
         *
         * @return the JSON id
         */
        public String getJsonId() {

            return m_jsonId;
        }
    }

    /** JSON key. */
    public static final String JSON_DETAIL = "d";

    /** JSON key. */
    public static final String JSON_PROJECT = "p";

    /** JSON key. */
    public static final String JSON_SITEROOT = "s";

    /** JSON key. */
    public static final String JSON_STRUCTUREID = "i";

    /** JSON key. */
    public static final String JSON_TITLE = "ti";

    /** JSON key. */
    public static final String JSON_TYPE = "t";

    /** The custom title. */
    private String m_customTitle;

    /** The detail id. */
    private CmsUUID m_detailId;

    /** The project id. */
    private CmsUUID m_projectId;

    /** The site root. */
    private String m_siteRoot;

    /** The structure id. */
    private CmsUUID m_structureId;

    /** The type. */
    private Type m_type;

    /**
     * Creates a new entry.
     */
    public CmsFavoriteEntry() {}

    /**
     * Creates a new entry from a JSON object.
     *
     * @param obj the JSON object
     */
    public CmsFavoriteEntry(JSONObject obj) {

        m_detailId = readId(obj, JSON_DETAIL);
        m_projectId = readId(obj, JSON_PROJECT);
        setSiteRoot(obj.optString(JSON_SITEROOT));
        m_structureId = readId(obj, JSON_STRUCTUREID);
        m_type = Type.fromJsonId(obj.optString(JSON_TYPE));
        m_customTitle = obj.optString(JSON_TITLE);
    }

    /**
     * Reads a UUID from a JSON object.
     *
     * Returns null if the JSON value for the given key is not present or not a valid UUID
     *
     * @param obj the JSON object
     * @param key the JSON key
     *
     * @return the UUID
     */
    public static CmsUUID readId(JSONObject obj, String key) {

        String strValue = obj.optString(key);
        if (!CmsUUID.isValidUUID(strValue)) {
            return null;
        }
        return new CmsUUID(strValue);
    }

    /**
     * Gets the custom title.
     *
     * @return the custom title
     */
    public String getCustomTitle() {

        return m_customTitle;
    }

    /**
     * Gets the detail id.
     *
     * @return the detail id
     */
    public CmsUUID getDetailId() {

        return m_detailId;
    }

    /**
     * Gets the project id.
     *
     * @return the project id
     */
    public CmsUUID getProjectId() {

        return m_projectId;
    }

    /**
     * Gets the site root.
     *
     * @return the site root
     */
    public String getSiteRoot() {

        return m_siteRoot;
    }

    /**
     * Gets the structure id
     *
     * @return the structure id
     */
    public CmsUUID getStructureId() {

        return m_structureId;
    }

    /**
     * Gets the type.
     *
     * @return the type
     */
    public Type getType() {

        return m_type;
    }

    /**
     * Sets the custom title.
     *
     * @param title the custom title
     */
    public void setCustomTitle(String title) {

        if (CmsStringUtil.isEmpty(title)) {
            m_customTitle = null;
        } else {
            m_customTitle = title;
        }
    }

    /**
     * Sets the detail id.
     *
     * @param detailId the detail id
     */
    public void setDetailId(CmsUUID detailId) {

        m_detailId = detailId;
    }

    /**
     * Sets the project id.
     *
     * @param projectId the project id
     */
    public void setProjectId(CmsUUID projectId) {

        m_projectId = projectId;
    }

    /**
     * Sets the site root.
     *
     * @param siteRoot the site root
     */
    public void setSiteRoot(String siteRoot) {

        if (siteRoot != null) {
            siteRoot = siteRoot.replaceFirst("/$", "");
        }
        m_siteRoot = siteRoot;
    }

    /**
     * Sets the structure id.
     * @param structureId the structure id
     */
    public void setStructureId(CmsUUID structureId) {

        m_structureId = structureId;
    }

    /**
     * Sets the type.
     *
     * @param type the type
     */
    public void setType(Type type) {

        m_type = type;
    }

    /**
     * Converts this object to JSON.
     *
     * @return the JSON representation
     *
     * @throws JSONException if JSON operations fail
     */
    public JSONObject toJson() throws JSONException {

        JSONObject result = new JSONObject();
        if (m_detailId != null) {
            result.put(JSON_DETAIL, "" + m_detailId);
        }
        if (m_siteRoot != null) {
            result.put(JSON_SITEROOT, m_siteRoot);
        }
        if (m_structureId != null) {
            result.put(JSON_STRUCTUREID, "" + m_structureId);
        }
        if (m_projectId != null) {
            result.put(JSON_PROJECT, "" + m_projectId);
        }
        if (m_type != null) {
            result.put(JSON_TYPE, "" + m_type.getJsonId());
        }

        if (!CmsStringUtil.isEmptyOrWhitespaceOnly(m_customTitle)) {
            result.put(JSON_TITLE, m_customTitle);
        }
        return result;
    }

    /**
     * Prepares the CmsObject for jumping to this favorite location, and returns the appropriate URL.
     *
     * @param cms the CmsObject to initialize for jumping to the favorite
     * @return the link for the favorite location
     *
     * @throws CmsException if something goes wrong
     */
    public String updateContextAndGetFavoriteUrl(CmsObject cms) throws CmsException {

        CmsResourceFilter filter = CmsResourceFilter.IGNORE_EXPIRATION;
        CmsProject project = null;
        switch (getType()) {
            case explorerFolder:
                CmsResource folder = cms.readResource(getStructureId(), filter);
                project = cms.readProject(getProjectId());
                cms.getRequestContext().setSiteRoot(getSiteRoot());
                A_CmsUI.get().getWorkplaceSettings().setSite(getSiteRoot());
                cms.getRequestContext().setCurrentProject(project);
                String explorerLink = CmsVaadinUtils.getWorkplaceLink(
                    CmsFileExplorerConfiguration.APP_ID,
                    getProjectId()
                        + A_CmsWorkplaceApp.PARAM_SEPARATOR
                        + getSiteRoot()
                        + A_CmsWorkplaceApp.PARAM_SEPARATOR
                        + cms.getSitePath(folder));
                return explorerLink;
            case page:
                project = cms.readProject(getProjectId());
                CmsResource target = cms.readResource(getStructureId(), filter);
                CmsResource detailContent = null;
                String link = null;
                cms.getRequestContext().setCurrentProject(project);
                cms.getRequestContext().setSiteRoot(getSiteRoot());
                A_CmsUI.get().getWorkplaceSettings().setSite(getSiteRoot());
                if (getDetailId() != null) {
                    detailContent = cms.readResource(getDetailId());
                    link = OpenCms.getLinkManager().substituteLinkForUnknownTarget(
                        cms,
                        cms.getSitePath(detailContent),
                        cms.getSitePath(target),
                        false);
                } else {
                    link = OpenCms.getLinkManager().substituteLink(cms, target);
                }
                return link;
            default:
                return null;
        }
    }

}
