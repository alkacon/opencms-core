/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/workplace/editors/ade/Attic/CmsProjectBean.java,v $
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
import org.opencms.json.JSONException;
import org.opencms.json.JSONObject;
import org.opencms.util.CmsUUID;

/**
 * A project bean.<p>
 * 
 * @author Michael Moossen
 * 
 * @version $Revision: 1.1 $ 
 * 
 * @since 7.6 
 */
public class CmsProjectBean implements I_CmsJsonifable {

    /** Json property name constants. */
    public enum JsonProperty {

        /** The id. */
        ID("id"),
        /** The project name. */
        NAME("name");

        /** Property name. */
        private String m_name;

        /** Constructor.<p> */
        private JsonProperty(String name) {

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

    /** The project id.*/
    private final CmsUUID m_id;

    /** The project name.*/
    private final String m_name;

    /** 
     * Creates a new project bean.<p> 
     *
     * @param id the project id
     * @param name the project name
     **/
    public CmsProjectBean(CmsUUID id, String name) {

        m_id = id;
        m_name = name;
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
     * Returns the name.<p>
     *
     * @return the name
     */
    public String getName() {

        return m_name;
    }

    /**
     * @see org.opencms.json.I_CmsJsonifable#toJson()
     */
    public JSONObject toJson() throws JSONException {

        JSONObject json = new JSONObject();
        json.put(JsonProperty.ID.getName(), getId());
        json.put(JsonProperty.NAME.getName(), getName());
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
