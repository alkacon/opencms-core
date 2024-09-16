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

package org.opencms.ade.contenteditor;

import org.opencms.ade.contenteditor.CmsWidgetUtil.WidgetInfo;
import org.opencms.file.CmsGroup;
import org.opencms.file.CmsObject;
import org.opencms.json.JSONObject;
import org.opencms.main.CmsLog;
import org.opencms.main.OpenCms;
import org.opencms.security.CmsRole;
import org.opencms.util.CmsFileUtil;
import org.opencms.util.CmsStringUtil;
import org.opencms.widgets.CmsAccessRestrictionWidget;
import org.opencms.xml.CmsXmlContentDefinition;
import org.opencms.xml.types.CmsXmlAccessRestrictionValue;
import org.opencms.xml.types.CmsXmlNestedContentDefinition;
import org.opencms.xml.types.I_CmsXmlSchemaType;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;

/**
 * Class for representing information about a 'restriction' field defined in a schema.
 */
public class CmsAccessRestrictionInfo {

    /** The role that can ignore group membership for manipulating the 'restricted' status. */
    public static final CmsRole ROLE_CAN_IGNORE_GROUP = CmsRole.ROOT_ADMIN;

    /** Log instance for this class. */
    private static final Log LOG = CmsLog.getLog(CmsAccessRestrictionInfo.class);

    /** The restriction group. */
    private CmsGroup m_group;

    /** The xpath for the restriction field in the schema. */
    private String m_path;

    /**
     * Creates a new instance.
     *
     * @param path the xpath for the restriction field in the schema
     * @param group the restriction group
     */
    public CmsAccessRestrictionInfo(String path, CmsGroup group) {

        super();
        m_path = path;
        m_group = group;
    }

    /**
     * Helper method for collecting all nested schema types of a content definition in a map.
     *
     * <p>The map keys in the resulting map will be the xpaths corresponding to the schema types.
     *
     * @param definition a content definition
     * @param path the path to start with
     * @param typesByPath the map in which the schema types should be stored
     */
    public static void collectTypesByPath(
        CmsXmlContentDefinition definition,
        String path,
        Map<String, I_CmsXmlSchemaType> typesByPath) {

        for (I_CmsXmlSchemaType schemaType : definition.getTypeSequence()) {
            String name = schemaType.getName();
            String subPath = path + "/" + name;
            typesByPath.put(CmsFileUtil.removeLeadingSeparator(subPath), schemaType);
            if (schemaType instanceof CmsXmlNestedContentDefinition) {
                CmsXmlContentDefinition nestedDef = ((CmsXmlNestedContentDefinition)schemaType).getNestedContentDefinition();
                if (nestedDef != null) {
                    collectTypesByPath(nestedDef, subPath, typesByPath);
                }
            }
        }
    }

    /**
     * Gets the restriction info for the current user and content definition.
     *
     * <p>This will only return a non-null value if the is restriction field defined in the content definition <em>and</em> the current user is in the group configured as the restriction group for that field.
     *
     * @param cms the current CMS context
     * @param contentDef the content definition
     *
     * @return the restriction information
     */
    public static CmsAccessRestrictionInfo getRestrictionInfo(CmsObject cms, CmsXmlContentDefinition contentDef) {

        Map<String, I_CmsXmlSchemaType> typesByPath = new HashMap<>();
        collectTypesByPath(contentDef, "", typesByPath);
        for (Map.Entry<String, I_CmsXmlSchemaType> entry : typesByPath.entrySet()) {
            I_CmsXmlSchemaType type = entry.getValue();
            try {
                if (type instanceof CmsXmlAccessRestrictionValue) {
                    WidgetInfo widgetInfo = CmsWidgetUtil.collectWidgetInfo(cms, contentDef, entry.getKey(), null, null);
                    String widgetConfig = widgetInfo.getWidget().getConfiguration();
                    if (widgetConfig != null) {
                        JSONObject json = new JSONObject(widgetConfig);
                        String groupName = json.optString(CmsAccessRestrictionWidget.ATTR_GROUP);
                        if (!CmsStringUtil.isEmptyOrWhitespaceOnly(groupName)) {
                            groupName = groupName.trim();
                            if (OpenCms.getRoleManager().hasRole(cms, ROLE_CAN_IGNORE_GROUP)
                                || cms.userInGroup(cms.getRequestContext().getCurrentUser().getName(), groupName)) {
                                return new CmsAccessRestrictionInfo(entry.getKey(), cms.readGroup(groupName));
                            }
                        }
                    }
                    break;
                }
            } catch (Exception e) {
                LOG.error(e.getLocalizedMessage(), e);
            }
        }
        return null;

    }

    /**
     * Gets the restriction group.
     *
     * @return the restriction group
     */
    public CmsGroup getGroup() {

        return m_group;
    }

    /**
     * Gets the xpath of the restriction field.
     *
     * @return the xpath of the restriction field
     */
    public String getPath() {

        return m_path;
    }

}
