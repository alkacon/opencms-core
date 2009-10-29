/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/workplace/editors/ade/Attic/CmsPublishGroupBean.java,v $
 * Date   : $Date: 2009/10/29 10:40:28 $
 * Version: $Revision: 1.2 $
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

import java.util.Collections;
import java.util.List;

/**
 * A publish group.<p>
 * 
 * @author Michael Moossen
 * 
 * @version $Revision: 1.2 $ 
 * 
 * @since 7.6 
 */
public class CmsPublishGroupBean implements I_CmsJsonifable {

    /** Json property name constants. */
    public enum JsonProperty {

        /** The group name. */
        NAME,
        /** The resources. */
        RESOURCES;
    }

    /** The group name.*/
    private final String m_name;

    /** The group resources.*/
    private final List<CmsPublishResourceBean> m_resources;

    /** 
     * Creates a new publish group bean.<p> 
     * 
     * @param name the group name
     * @param resources the resources
     **/
    public CmsPublishGroupBean(String name, List<CmsPublishResourceBean> resources) {

        m_name = name;
        m_resources = ((resources == null)
        ? Collections.<CmsPublishResourceBean> emptyList()
        : Collections.unmodifiableList(resources));
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
     * Returns the resources.<p>
     *
     * @return the resources
     */
    public List<CmsPublishResourceBean> getResources() {

        return m_resources;
    }

    /**
     * @see org.opencms.json.I_CmsJsonifable#toJson()
     */
    public JSONObject toJson() throws JSONException {

        JSONObject json = new JSONObject();
        json.put(JsonProperty.NAME.name().toLowerCase(), getName());
        JSONArray resources = new JSONArray();
        for (CmsPublishResourceBean resource : m_resources) {
            resources.put(resource.toJson());
        }
        json.put(JsonProperty.RESOURCES.name().toLowerCase(), resources);
        return json;
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
