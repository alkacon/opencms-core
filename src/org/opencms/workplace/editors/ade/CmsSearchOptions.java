/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/workplace/editors/ade/Attic/CmsSearchOptions.java,v $
 * Date   : $Date: 2009/09/03 11:17:23 $
 * Version: $Revision: 1.1.2.5 $
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

import org.opencms.json.JSONArray;
import org.opencms.json.JSONException;
import org.opencms.util.CmsStringUtil;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

/**
 * Bean encapsulating all ADE search options.<p>
 * 
 * @author Michael Moossen
 * 
 * @version $Revision: 1.1.2.5 $ 
 * 
 * @since 7.6 
 */
public class CmsSearchOptions {

    /** The vfs location to search. */
    private String m_location;

    /** The page number to use, zero based. */
    private int m_page;

    /** The search query. */
    private String m_text;

    /** The type of resources to search. */
    private String m_type;

    /** 
     * Creates a new search options bean.<p> 
     * 
     * @param request the current request
     **/
    public CmsSearchOptions(HttpServletRequest request) {

        String location = request.getParameter(CmsADEServer.PARAMETER_LOCATION);
        String text = request.getParameter(CmsADEServer.PARAMETER_TEXT);
        String type = request.getParameter(CmsADEServer.PARAMETER_TYPE);
        int page = 0;
        try {
            page = Integer.parseInt(request.getParameter(CmsADEServer.PARAMETER_PAGE));
        } catch (Throwable e) {
            // ignore
        }
        init(location, text, type, page);
    }

    /**
     * Creates a new search options bean.<p> 
     * 
     * @param location the vfs location to search
     * @param text the search query
     * @param type the type of resources to search
     * @param page the page number to use, zero based
     */
    public CmsSearchOptions(String location, String text, String type, int page) {

        init(location, text, type, page);
    }

    /**
     * Returns the vfs location to search.<p>
     *
     * @return the vfs location to search
     */
    public String getLocation() {

        return m_location;
    }

    /**
     * Returns the page number to use, zero based.<p>
     *
     * @return the page number to use, zero based
     */
    public int getPage() {

        return m_page;
    }

    /**
     * Returns the search query.<p>
     *
     * @return the search query
     */
    public String getText() {

        return m_text;
    }

    /**
     * Returns the type of resources to search.<p>
     * 
     * @return the type of resources to search
     */
    public String getType() {

        return m_type;
    }

    /**
     * Returns the types as an {@link JSONArray}.<p>
     * 
     * @return the types as an {@link JSONArray}
     */
    public JSONArray getTypes() {

        try {
            return new JSONArray(getType());
        } catch (JSONException e) {
            return new JSONArray();
        }
    }

    /**
     * Returns the types as a {@link List}.<p>
     * 
     * @return the types as a {@link List}
     */
    public List<String> getTypesAsList() {

        List<String> types = new ArrayList<String>();
        JSONArray jsonTypes = getTypes();
        for (int i = 0; i < jsonTypes.length(); i++) {
            types.add(jsonTypes.optString(i));
        }
        return types;
    }

    /**
     * Checks if the search options are valid.<p>
     * 
     * Valid means, at least one type and text
     * 
     * @return <code>true</code> if the search options are valid
     */
    public boolean isValid() {

        return (getTypes().length() > 0) && CmsStringUtil.isNotEmptyOrWhitespaceOnly(getText());
    }

    /**
     * Returns a clone of this object, but with <code>page = 0</code>.<p>
     * 
     * @return a clone of this object, but with <code>page = 0</code>
     */
    public CmsSearchOptions resetPage() {

        return new CmsSearchOptions(getLocation(), getText(), getType(), 0);
    }

    /**
     * Validates all parameters.<p>
     * 
     * @param location the vfs location to search
     * @param text the search query
     * @param type the type of resources to search
     * @param page the page number to use, zero based
     */
    protected void init(String location, String text, String type, int page) {

        m_text = text;
        if (CmsStringUtil.isEmptyOrWhitespaceOnly(m_text)) {
            m_text = "";
        }
        m_location = location;
        if (CmsStringUtil.isEmptyOrWhitespaceOnly(m_location)) {
            m_location = "/";
        }
        m_type = type;
        if (CmsStringUtil.isEmptyOrWhitespaceOnly(m_type)) {
            m_type = "[]";
        }
        m_page = page;
        if (m_page < 0) {
            m_page = 0;
        }
    }
}
