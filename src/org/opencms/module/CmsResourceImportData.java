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

package org.opencms.module;

import org.opencms.file.CmsObject;
import org.opencms.file.CmsProperty;
import org.opencms.file.CmsResource;
import org.opencms.importexport.CmsImportVersion10.RelationData;
import org.opencms.security.CmsAccessControlEntry;
import org.opencms.util.CmsFileUtil;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Import data for a single resource.<p>
 */
public class CmsResourceImportData {

    /** The access control entries. */
    private List<CmsAccessControlEntry> m_aces;

    /** Flag indicating whether resource id checks should be skipped for this import resource. */
    private boolean m_skipResourceIdCheck;

    /** The temp file with the content (may be null). */
    private File m_contentFile;

    /** True if there is a modification date in the import. */
    private boolean m_hasDateLastModified;

    /** True if this had a structure id in the import. */
    private boolean m_hasStructureId;

    /** The import resource. */
    private CmsResource m_importResource;

    /** The path. */
    private String m_path;

    /** The properties. */
    private List<CmsProperty> m_properties;

    /** The relations. */
    private List<RelationData> m_relationData;

    /** The CmsResource object containing the attributes for the resource. */
    private CmsResource m_resource;

    /** The original type name from the manifest. */
    private String m_typeName;

    /**
     * Creats a new instance.<p>
     *
     * @param resource the resource
     * @param path the path
     * @param content the content
     * @param properties the properties
     * @param aces the acccess control entries
     * @param relationData the relation data
     * @param hasStructureId true if has a structure id
     * @param hasDateLastModified true if has a modification date
     * @param typeName the type name from the manifest
     */
    public CmsResourceImportData(
        CmsResource resource,
        String path,
        byte[] content,
        List<CmsProperty> properties,
        List<CmsAccessControlEntry> aces,
        List<RelationData> relationData,
        boolean hasStructureId,
        boolean hasDateLastModified,
        String typeName) {

        m_typeName = typeName;
        m_resource = resource;
        m_path = path;
        if (content != null) {
            m_contentFile = createTempFile(content);
        }

        if (properties == null) {
            properties = new ArrayList<>();
        }
        m_properties = properties;

        if (aces == null) {
            aces = new ArrayList<>();
        }
        m_aces = aces;

        if (relationData == null) {
            relationData = new ArrayList<>();
        }
        m_relationData = relationData;
        m_hasStructureId = hasStructureId;
        m_hasDateLastModified = hasDateLastModified;
    }

    /**
     * Cleans up temp files.<p>
     */
    public void cleanUp() {

        if (m_contentFile != null) {
            m_contentFile.delete();
        }
    }

    /**
     * Computes the root path.<p>
     *
     * @param cms the CMS context
     * @return the root path
     */
    public Object computeRootPath(CmsObject cms) {

        return cms.getRequestContext().addSiteRoot(m_path);

    }

    /**
     * Gets the access control entries.<p>
     *
     * @return the access control entries
     */
    public List<CmsAccessControlEntry> getAccessControlEntries() {

        return m_aces;
    }

    /**
     * Gets the content.<p>
     *
     * @return the content, or null if there is no content
     */
    public byte[] getContent() {

        if (m_contentFile == null) {
            return null;
        }
        try {
            return CmsFileUtil.readFile(m_contentFile);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Gets the import resource.<p>
     *
     * This is set by the module updater if the resource has actually been  imported.
     *
     * @return the import resource
     */
    public CmsResource getImportResource() {

        return m_importResource;
    }

    /**
     * Gets the path.<p>
     *
     * @return the path
     */
    public String getPath() {

        return m_path;
    }

    /**
     * Gets the map of properties, with property names as keys.<p>
     *
     * @return the map of properties
     */
    public Map<String, CmsProperty> getProperties() {

        return CmsProperty.getPropertyMap(m_properties);

    }

    /**
     * Gets the relations.<p>
     *
     * @return the relations
     */
    public List<RelationData> getRelations() {

        return m_relationData;
    }

    /**
     * Gets the resource.<p>
     *
     * @return the resource
     */
    public CmsResource getResource() {

        return m_resource;

    }

    /**
     * Gets the original type name from the manifest.
     *
     * @return the type name
     */
    public String getTypeName() {

        return m_typeName;
    }

    /**
     * Checks if there is content.<p>
     *
     * @return true if there is content
     */
    public boolean hasContent() {

        return m_contentFile != null;
    }

    /**
     * Returns true if this had a modification date in the import.<p>
     *
     * @return true if this had a modification date in the import
     */
    public boolean hasDateLastModified() {

        return m_hasDateLastModified;
    }

    /**
     * Returns true if this had a structure id in the import.<p>
     *
     * @return true if this had a structure id in the import
     */
    public boolean hasStructureId() {

        return m_hasStructureId;
    }

    /**
     * Returns true if resource id checks should be disabled for this import resource.
     *
     * @return true if resource id checks should be disabled
     */
    public boolean isSkipResourceIdCheck() {

        return m_skipResourceIdCheck;
    }

    /**
     * Sets the import resource.<p>
     *
     * @param importRes the import resource
     */
    public void setImportResource(CmsResource importRes) {

        m_importResource = importRes;
    }

    /**
     * Sets the 'skip resource id check' flag.
     *
     * @param skipResourceIdCheck the new value
     */
    public void setSkipResourceIdCheck(boolean skipResourceIdCheck) {

        m_skipResourceIdCheck = skipResourceIdCheck;
    }

    /**
     * Creates a temp file to store the given content.<p>
     *
     * @param content the content to store in the temp file
     *
     * @return the created temp file
     */
    private File createTempFile(byte[] content) {

        try {
            File file = File.createTempFile("ocms-moduleresource-", ".dat");
            file.deleteOnExit();
            try (FileOutputStream output = new FileOutputStream(file)) {
                output.write(content);
            }
            return file;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}
