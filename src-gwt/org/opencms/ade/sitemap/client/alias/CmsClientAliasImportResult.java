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

package org.opencms.ade.sitemap.client.alias;

import org.opencms.gwt.shared.alias.CmsAliasImportStatus;

import java.util.ArrayList;
import java.util.List;

import com.google.gwt.json.client.JSONArray;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.json.client.JSONString;
import com.google.gwt.json.client.JSONValue;
import static org.opencms.ade.sitemap.shared.I_CmsAliasConstants.*;

/**
 * A client-side bean for representing the results 
 */
public class CmsClientAliasImportResult {

    private String m_error;
    private String m_line;
    private CmsAliasImportStatus m_status;

    public CmsClientAliasImportResult(String line, CmsAliasImportStatus status, String error) {

        m_status = status;
        m_line = line;
        m_error = error;
    }

    public static CmsClientAliasImportResult parse(JSONObject json) {

        String line = getString(json, JSON_LINE);
        String error = getString(json, JSON_MESSAGE);
        String statusStr = getString(json, JSON_STATUS);
        CmsAliasImportStatus status = CmsAliasImportStatus.valueOf(statusStr);
        return new CmsClientAliasImportResult(line, status, error);
    }

    public static List<CmsClientAliasImportResult> parseArray(JSONArray array) {

        List<CmsClientAliasImportResult> result = new ArrayList<CmsClientAliasImportResult>();
        for (int i = 0; i < array.size(); i++) {
            JSONValue lineVal = array.get(i);
            JSONObject lineObj = (JSONObject)lineVal;
            CmsClientAliasImportResult singleResult = parse(lineObj);
            result.add(singleResult);
        }
        return result;
    }

    protected static String getString(JSONObject json, String key) {

        JSONValue value = json.get(key);
        JSONString str = value.isString();
        if (str == null) {
            return null;
        }
        return str.stringValue();
    }

    public String getError() {

        return m_error;
    }

    public String getLine() {

        return m_line;
    }

    public CmsAliasImportStatus getStatus() {

        return m_status;
    }
}
