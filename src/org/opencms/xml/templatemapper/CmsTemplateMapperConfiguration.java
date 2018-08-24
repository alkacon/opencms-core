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

package org.opencms.xml.templatemapper;

import org.opencms.file.CmsObject;
import org.opencms.file.CmsResource;
import org.opencms.file.CmsResourceFilter;
import org.opencms.main.CmsException;
import org.opencms.main.CmsLog;
import org.opencms.main.OpenCms;
import org.opencms.util.CmsStringUtil;
import org.opencms.util.CmsUUID;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.compress.utils.Lists;
import org.apache.commons.logging.Log;

import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.Node;

/**
 * Configuration for the template mapper.<p>
 */
public class CmsTemplateMapperConfiguration {

    /** XML attribute name. */
    public static final String A_ENABLED = "enabled";

    /** XML attribute name. */
    public static final String A_NEW = "new";

    /** XML attribute name. */
    public static final String A_OLD = "old";

    /** Empty configuratin. */
    public static final CmsTemplateMapperConfiguration EMPTY_CONFIG = new CmsTemplateMapperConfiguration();

    /** XML element name. */
    public static final String N_FORMATTER_CONFIG = "formatter-config";
    /** XML element name. */
    public static final String N_FORMATTER_JSP = "formatter-jsp";

    /** XML element name. */
    public static final String N_ELEMENT_GROUP_TYPE = "element-group-type";

    /** XML element name. */
    public static final String N_PATH = "path";

    /** The logger instance for this class. */
    private static final Log LOG = CmsLog.getLog(CmsTemplateMapperConfiguration.class);

    /** The formatter configuration mapping (structure id to structure id). */
    private Map<CmsUUID, CmsUUID> m_formatterConfigMap = new HashMap<>();

    /** The formatter JSP mapping (structure id to structure id). */
    private Map<CmsUUID, CmsUUID> m_formatterJspMap = new HashMap<>();

    /** Mapping for element group types. */
    private Map<String, String> m_groupTypeMap = new HashMap<>();

    /** List of root paths for which the configuration is valid. */
    private List<String> m_paths = Lists.newArrayList();

    /**
     * Creates an empty mapper configuration which is not active for any path.<p>
     */
    public CmsTemplateMapperConfiguration() {
        // do nothing
    }

    /**
     * Parses a template mapper configuration from an XML document.<p>
     *
     * @param cms the current CMS context
     * @param doc the XML document containing the configuration
     *
     * @throws CmsException if something goes wrong
     */
    public CmsTemplateMapperConfiguration(CmsObject cms, Document doc)
    throws CmsException {

        cms = OpenCms.initCmsObject(cms);
        cms.getRequestContext().setSiteRoot("");
        Element root = doc.getRootElement();
        String enabledStr = root.attributeValue(A_ENABLED);
        boolean enabled = Boolean.parseBoolean(enabledStr);

        if (enabled) {
            for (Node node : root.selectNodes("//" + N_FORMATTER_CONFIG)) {
                Element formatterElem = (Element)node;
                String oldPath = formatterElem.attributeValue(A_OLD);
                String newPath = formatterElem.attributeValue(A_NEW);
                try {
                    CmsResource oldFormatter = cms.readResource(oldPath, CmsResourceFilter.IGNORE_EXPIRATION);
                    CmsResource newFormatter = cms.readResource(newPath, CmsResourceFilter.IGNORE_EXPIRATION);
                    m_formatterConfigMap.put(oldFormatter.getStructureId(), newFormatter.getStructureId());
                } catch (CmsException e) {
                    LOG.error(e.getLocalizedMessage(), e);
                }
            }
            for (Node node : root.selectNodes("//" + N_FORMATTER_JSP)) {
                Element formatterElem = (Element)node;
                String oldPath = formatterElem.attributeValue(A_OLD);
                String newPath = formatterElem.attributeValue(A_NEW);
                try {
                    CmsResource oldFormatter = cms.readResource(oldPath, CmsResourceFilter.IGNORE_EXPIRATION);
                    CmsResource newFormatter = cms.readResource(newPath, CmsResourceFilter.IGNORE_EXPIRATION);
                    m_formatterJspMap.put(oldFormatter.getStructureId(), newFormatter.getStructureId());
                } catch (CmsException e) {
                    LOG.error(e.getLocalizedMessage(), e);
                }
            }

            for (Node node : root.selectNodes("//" + N_ELEMENT_GROUP_TYPE)) {
                Element groupElem = (Element)node;
                String oldType = groupElem.attributeValue(A_OLD);
                String newType = groupElem.attributeValue(A_NEW);
                m_groupTypeMap.put(oldType, newType);
            }

            for (Node node : root.selectNodes("//" + N_PATH)) {
                Element pathElem = (Element)node;
                m_paths.add(pathElem.getText());
            }
        }
    }

    /**
     * Gets the mapped type for a given element group type, or null if there is no mapped type.<p>
     *
     * @param type the original element group type
     *
     * @return the mapped element group type
     */
    public String getMappedElementGroupType(String type) {

        return m_groupTypeMap.get(type);

    }

    /**
     * Gets the mapped formatter configuration structure id string for another formatter configuration structure id string.<p>
     *
     * @param id the structure id of a formatter configuration as a string
     * @return the mapped formatter configuration structure id of a string
     */
    public String getMappedFormatterConfiguration(String id) {

        CmsUUID resultId = m_formatterConfigMap.get(new CmsUUID(id));
        if (resultId == null) {
            return null;
        }
        return resultId.toString();
    }

    /**
     * Gets the mapped formatter JSP structure id for another formatter JSP structure id.<p>
     *
     * @param formatterId the input formatter JSP structure id
     * @return the mapped formatter JSP structure id
     */
    public CmsUUID getMappedFormatterJspId(CmsUUID formatterId) {

        return m_formatterJspMap.get(formatterId);
    }

    /**
     * Checks if the mapping is enabled for the given root path.<p>
     *
     * @param rootPath a VFS root path
     * @return true if the configuration is enabled for the given root path
     */
    public boolean isEnabledForPath(String rootPath) {

        for (String path : m_paths) {
            if (CmsStringUtil.isPrefixPath(path, rootPath)) {
                return true;
            }
        }
        return false;
    }

}
