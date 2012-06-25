/*
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (C) Alkacon Software (http://www.alkacon.com)
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

package org.opencms.db;

import static org.opencms.ade.sitemap.shared.I_CmsAliasConstants.JSON_LINE;
import static org.opencms.ade.sitemap.shared.I_CmsAliasConstants.JSON_MESSAGE;
import static org.opencms.ade.sitemap.shared.I_CmsAliasConstants.JSON_STATUS;

import org.opencms.gwt.shared.alias.CmsAliasImportStatus;
import org.opencms.json.JSONException;
import org.opencms.json.JSONObject;

/**
 * A bean representing the result of trying to import a single alias.<p>
 */
public class CmsAliasImportResult {

    /** The message from importing the alias. */
    private String m_message;

    /** The line containing the data for the alias. */
    private String m_line;

    /** The import status. */
    private CmsAliasImportStatus m_status;

    /**
     * Creates a new instance.<p>
     * 
     * @param status the import status 
     * @param message the import message
     */
    public CmsAliasImportResult(CmsAliasImportStatus status, String message) {

        m_message = message;
        m_status = status;
    }

    /**
     * Creates a new instance.<p>
     * 
     * @param line the line containing the alias data 
     * @param status the import status 
     * @param message the import message 
     */
    public CmsAliasImportResult(String line, CmsAliasImportStatus status, String message) {

        m_line = line;
        m_status = status;
        m_message = message;
    }

    /**
     * Gets the import message.<p>
     *  
     * @return the import message 
     */
    public String getMessage() {

        return m_message;
    }

    /** 
     * Gets the line containing the alias data.<p>
     * 
     * @return the line containing the alias data 
     */
    public String getLine() {

        return m_line;
    }

    /**
     * Sets the line containing the alias data.<p>
     * 
     * @param line the line containing the alias data 
     */
    public void setLine(String line) {

        m_line = line;
    }

    /**
     * Converts the bean to a JSON object.<p>
     * 
     * @return a JSON object containing the data from the bean 
     */
    public JSONObject toJson() {

        try {
            JSONObject obj = new JSONObject();
            if (m_line != null) {
                obj.put(JSON_LINE, m_line);
            }
            if (m_message != null) {
                obj.put(JSON_MESSAGE, m_message);
            }
            if (m_status != null) {
                obj.put(JSON_STATUS, m_status.toString());
            }

            return obj;
        } catch (JSONException e) {
            // should never happen
            return null;

        }
    }

    /**
     * Converts the bean to a JSON string.<p>
     * 
     * @return a JSON string containing the data from the bean 
     */
    public String toJsonString() {

        return toJson().toString();
    }

}
