/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/workplace/editors/ade/Attic/CmsPublishResourceBean.java,v $
 * Date   : $Date: 2009/10/28 15:38:11 $
 * Version: $Revision: 1.1 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (C) 2002 - 2009 Alkacon Software (http://www.alkacon.com)
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

package org.opencms.workplace.editors.ade;

import org.opencms.json.I_CmsJsonifable;
import org.opencms.json.JSONArray;
import org.opencms.json.JSONException;
import org.opencms.json.JSONObject;
import org.opencms.util.CmsUUID;

import java.util.Collections;
import java.util.List;

/**
 * A publish resource.<p>
 * 
 * @author Michael Moossen
 * 
 * @version $Revision: 1.1 $ 
 * 
 * @since 7.6 
 */
public class CmsPublishResourceBean implements I_CmsJsonifable {

    /** JSON property name constants. */
    public enum JsonProperty {

        /** The resource type icon path. */
        ICON,
        /** The structure id. */
        ID,
        /** The information tool tip. */
        INFO,
        /** The reason a resource can not be published. */
        INFOTYPE,
        /** The list of related resources. */
        RELATED,
        /** Flag to indicate if the resource can be removed from the publish list. */
        REMOVABLE,
        /** The resource state. */
        STATE,
        /** Resource title. */
        TITLE,
        /** Resource uri. */
        URI;
    }

    /** The resource icon.*/
    private final String m_icon;

    /** The resource id.*/
    private final CmsUUID m_id;

    /** The additional information, if any. */
    private final CmsPublishResourceInfoBean m_info;

    /** The resource name.*/
    private final String m_name;

    /** The related resources.*/
    private final List<CmsPublishResourceBean> m_related;

    /** Flag to indicate if the resource can be removed from the user's publish list.*/
    private final boolean m_removable;

    /** The resource state.*/
    private final String m_state;

    /** The resource title.*/
    private final String m_title;

    /** 
     * Creates a new publish group bean.<p> 
     * 
     * @param id the resource id
     * @param name the resource name
     * @param title the resource title
     * @param icon the resource icon
     * @param state the resource state
     * @param removable to indicate if the resource can be removed from the user's publish list
     * @param info the additional information, if any
     * @param related the related resources
     **/
    public CmsPublishResourceBean(
        CmsUUID id,
        String name,
        String title,
        String icon,
        String state,
        boolean removable,
        CmsPublishResourceInfoBean info,
        List<CmsPublishResourceBean> related) {

        super();
        m_icon = icon;
        m_id = id;
        m_name = name;
        m_related = Collections.unmodifiableList(related);
        m_state = state;
        m_title = title;
        m_removable = removable;
        m_info = info;
    }

    /**
     * Returns the icon.<p>
     *
     * @return the icon
     */
    public String getIcon() {

        return m_icon;
    }

    /**
     * Returns the id.<p>
     *
     * @return the id
     */
    public CmsUUID getId() {

        return m_id;
    }

    /**
     * Returns the additional info.<p>
     *
     * @return the additional info
     */
    public CmsPublishResourceInfoBean getInfo() {

        return m_info;
    }

    /**
     * Returns the name.<p>
     *
     * @return the name
     */
    public String getName() {

        return m_name;
    }

    /**
     * Returns the related resources.<p>
     *
     * @return the related resources
     */
    public List<CmsPublishResourceBean> getRelated() {

        return m_related;
    }

    /**
     * Returns the state.<p>
     *
     * @return the state
     */
    public String getState() {

        return m_state;
    }

    /**
     * Returns the title.<p>
     *
     * @return the title
     */
    public String getTitle() {

        return m_title;
    }

    /**
     * Returns the removable flag.<p>
     *
     * @return the removable flag
     */
    public boolean isRemovable() {

        return m_removable;
    }

    /**
     * @see org.opencms.json.I_CmsJsonifable#toJson()
     */
    public JSONObject toJson() throws JSONException {

        JSONObject jsonRes = new JSONObject();
        jsonRes.put(JsonProperty.ID.name().toLowerCase(), m_id.toString());
        jsonRes.put(JsonProperty.URI.name().toLowerCase(), m_name);
        jsonRes.put(JsonProperty.TITLE.name().toLowerCase(), m_title);
        jsonRes.put(JsonProperty.ICON.name().toLowerCase(), m_icon);
        jsonRes.put(JsonProperty.STATE.name().toLowerCase(), m_state);
        jsonRes.put(JsonProperty.REMOVABLE.name().toLowerCase(), m_removable);
        jsonRes.put(JsonProperty.INFO.name().toLowerCase(), m_info.getValue());
        jsonRes.put(JsonProperty.INFOTYPE.name().toLowerCase(), m_info.getType().toString().toLowerCase());
        JSONArray resources = new JSONArray();
        for (CmsPublishResourceBean related : m_related) {
            resources.put(related.toJson());
        }
        jsonRes.put(JsonProperty.RELATED.name().toLowerCase(), resources);
        return jsonRes;
    }

    /**
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {

        try {
            return toJson().toString();
        } catch (JSONException e) {
            // should never happen
            return m_name;
        }
    }
}
