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

import static org.opencms.ade.sitemap.shared.I_CmsAliasConstants.JSON_LINE;
import static org.opencms.ade.sitemap.shared.I_CmsAliasConstants.JSON_MESSAGE;
import static org.opencms.ade.sitemap.shared.I_CmsAliasConstants.JSON_STATUS;

import org.opencms.gwt.shared.alias.CmsAliasImportStatus;

import java.util.ArrayList;
import java.util.List;

import com.google.gwt.json.client.JSONArray;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.json.client.JSONString;
import com.google.gwt.json.client.JSONValue;

/**
 * A client-side bean for representing the results of an alias import.<p>
 */
public class CmsClientAliasImportResult {

    /** The file line from which the alias to import has been read. */
    private String m_line;

    /** The alias import message. */
    private String m_message;

    /** The alias import status. */
    private CmsAliasImportStatus m_status;

    /**
     * Creates a new instance.<p>
     *
     * @param line the CSV line containing the alias
     * @param status the import status
     * @param message the import message
     */
    public CmsClientAliasImportResult(String line, CmsAliasImportStatus status, String message) {

        m_status = status;
        m_line = line;
        m_message = message;
    }

    /**
     * Parses an alias import result from a JSON object.<p>
     *
     * @param json the JSON object containing the alias import result data
     *
     * @return the alias import result bean
     */
    public static CmsClientAliasImportResult parse(JSONObject json) {

        String line = getString(json, JSON_LINE);
        String message = getString(json, JSON_MESSAGE);
        String statusStr = getString(json, JSON_STATUS);
        CmsAliasImportStatus status = CmsAliasImportStatus.valueOf(statusStr);
        return new CmsClientAliasImportResult(line, status, message);
    }

    /**
     * Extracts alias import results from a JSON array.<p>
     *
     * @param array the JSON array
     *
     * @return the alias import results from the array
     */
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

    /**
     * Helper method to get a string value from a JSON object.<p>
     *
     * @param json the JSON object
     * @param key the key whose value should be extracted as a string
     *
     * @return the string value for the given key
     */
    protected static String getString(JSONObject json, String key) {

        JSONValue value = json.get(key);
        JSONString str = value.isString();
        if (str == null) {
            return null;
        }
        return str.stringValue();
    }

    /**
     * Gets the CSV line containing the alias.<p>
     *
     * @return the CSV line
     */
    public String getLine() {

        return m_line;
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
     * Gets the import status.<p>
     *
     * @return the import status
     */
    public CmsAliasImportStatus getStatus() {

        return m_status;
    }
}
