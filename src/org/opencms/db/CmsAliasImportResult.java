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

import org.opencms.gwt.shared.alias.CmsAliasImportStatus;
import static org.opencms.ade.sitemap.shared.I_CmsAliasConstants.*;
import org.opencms.json.JSONException;
import org.opencms.json.JSONObject;

public class CmsAliasImportResult {

    private String m_error;
    private String m_line;
    private CmsAliasImportStatus m_status;

    public CmsAliasImportResult(CmsAliasImportStatus status, String error) {

        m_error = error;
        m_status = status;
    }

    public CmsAliasImportResult(String line, CmsAliasImportStatus status, String error) {

        m_line = line;
        m_status = status;
        m_error = error;
    }

    public String getError() {

        return m_error;
    }

    public String getLine() {

        return m_line;
    }

    public void setLine(String line) {

        m_line = line;
    }

    public JSONObject toJson() {

        try {
            JSONObject obj = new JSONObject();
            if (m_line != null) {
                obj.put(JSON_LINE, m_line);
            }
            if (m_error != null) {
                obj.put(JSON_MESSAGE, m_error);
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

    public String toJsonString() {

        return toJson().toString();
    }

}
