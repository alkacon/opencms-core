/*
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (c) Alkacon Software GmbH (http://www.alkacon.com)
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

package org.opencms.editors.usergenerated;

import org.opencms.file.CmsFile;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsProject;
import org.opencms.file.CmsResource;
import org.opencms.file.types.I_CmsResourceType;
import org.opencms.lock.CmsLock;
import org.opencms.main.CmsException;
import org.opencms.main.CmsIllegalArgumentException;
import org.opencms.main.OpenCms;
import org.opencms.util.CmsStringUtil;
import org.opencms.xml.CmsXmlUtils;
import org.opencms.xml.content.CmsXmlContent;
import org.opencms.xml.content.CmsXmlContentErrorHandler;
import org.opencms.xml.content.CmsXmlContentFactory;
import org.opencms.xml.types.I_CmsXmlContentValue;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * A form editing session is required to create and edit contents from the web front-end.<p>
 */
public class CmsFormSession {

    /**
     * Compares XPaths.<p>
     */
    protected static class PathComparator implements Comparator<String> {

        /**
         * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
         */
        public int compare(String o1, String o2) {

            int result = -1;
            if (o1 == null) {
                result = 1;
            } else if (o2 == null) {
                result = -1;
            } else {
                String[] o1Elements = o1.split("/");
                String[] o2Elements = o2.split("/");
                // compare the path elements
                for (int i = 0; i < o1Elements.length; i++) {
                    String o1Name = CmsXmlUtils.removeXpathIndex(o1Elements[i]);
                    String o2Name = CmsXmlUtils.removeXpathIndex(o2Elements[i]);
                    if (o1Name.equals(o2Name)) {
                        // in case of the same path name, check the indices
                        int o1Index = CmsXmlUtils.getXpathIndexInt(o1Elements[i]);
                        int o2Index = CmsXmlUtils.getXpathIndexInt(o2Elements[i]);
                        if (o1Index != o2Index) {
                            result = o2Index - o1Index;
                            break;
                        } else if (i == (o2Elements.length - 1)) {
                            // in case this is the last element of o2's path
                            result = 1;
                            break;
                        }
                    } else {
                        result = o1Name.compareTo(o2Name);
                        break;
                    }
                }
            }

            return result;
        }
    }

    /** The edit context. */
    private CmsObject m_cms;

    /** The form configuration. */
    private CmsFormConfiguration m_configuration;

    /** The resource being edited. */
    private CmsResource m_editResource;

    /** 
     * Constructor.<p>
     * 
     * @param cms the cms context
     * @param configuration the form configuration
     * 
     * @throws CmsException if creating the session project fails
     */
    public CmsFormSession(CmsObject cms, CmsFormConfiguration configuration)
    throws CmsException {

        m_configuration = configuration;
        if (cms.getRequestContext().getCurrentUser().isGuestUser() && m_configuration.getUserForGuests().isPresent()) {
            m_cms = OpenCms.initCmsObject(m_configuration.getUserForGuests().get().getName());
            m_cms.getRequestContext().setSiteRoot(cms.getRequestContext().getSiteRoot());
        } else {
            m_cms = OpenCms.initCmsObject(cms);
        }
        CmsProject project = m_cms.createProject(
            generateProjectName(),
            "User generated content project",
            m_configuration.getProjectGroup().getName(),
            m_configuration.getProjectGroup().getName());
        m_cms.getRequestContext().setCurrentProject(project);

    }

    /**
     * 
     * @param fileName
     * @param content
     * @return
     * @throws CmsIllegalArgumentException
     * @throws CmsException
     */
    public CmsResource createUploadResource(String fileName, byte[] content)
    throws CmsIllegalArgumentException, CmsException {

        CmsResource result = null;
        if (m_configuration.getUploadParentFolder().isPresent()) {
            String sitePath = OpenCms.getResourceManager().getNameGenerator().getUniqueFileName(
                m_cms,
                m_configuration.getUploadParentFolder().get().getRootPath(),
                fileName);
            int resTypeId = OpenCms.getResourceManager().getDefaultTypeForName(sitePath).getTypeId();
            result = m_cms.createResource(fileName, resTypeId, content, null);
        }
        return null;
    }

    public CmsResource createXmlContent() throws CmsIllegalArgumentException, CmsException {

        I_CmsResourceType type = OpenCms.getResourceManager().getResourceType(m_configuration.getResourceType());
        m_editResource = m_cms.createResource(getNewResourceName(), type.getTypeId());
        return m_editResource;
    }

    public CmsResource getResource() {

        return m_editResource;

    }

    /**
     * Returns the content values.<p>
     * 
     * @return the content values
     * 
     * @throws CmsException if reading the content fails
     */
    public Map<String, String> getValues() throws CmsException {

        CmsFile file = m_cms.readFile(m_editResource);
        CmsXmlContent content = CmsXmlContentFactory.unmarshal(m_cms, file);
        Locale locale = m_cms.getRequestContext().getLocale();
        if (!content.hasLocale(locale)) {
            content.addLocale(m_cms, locale);
        }
        Map<String, String> result = new HashMap<String, String>();
        List<I_CmsXmlContentValue> values = content.getValues(locale);
        for (I_CmsXmlContentValue value : values) {
            result.put(value.getPath(), value.getStringValue(m_cms));
        }
        return null;
    }

    public CmsResource loadXmlContent(String fileName) throws CmsException {

        m_editResource = m_cms.readResource(fileName);
        CmsLock lock = m_cms.getLock(m_editResource);
        if (!lock.isOwnedBy(m_cms.getRequestContext().getCurrentUser())) {
            m_cms.lockResourceTemporary(m_editResource);
        }
        return m_editResource;

    }

    public CmsXmlContentErrorHandler saveContent(Map<String, String> contentValues) throws CmsException {

        CmsFile file = m_cms.readFile(m_editResource);
        CmsXmlContent content = addContentValues(file, contentValues);
        CmsXmlContentErrorHandler errorHandler = content.validate(m_cms);

        file.setContents(content.marshal());

        // the file content might have been modified during the write operation
        file = m_cms.writeFile(file);

        return errorHandler;

    }

    public CmsXmlContentErrorHandler validateContent(Map<String, String> contentValues) throws CmsException {

        CmsFile file = m_cms.readFile(m_editResource);
        CmsXmlContent content = addContentValues(file, contentValues);
        return content.validate(m_cms);
    }

    CmsProject getProject() {

        return m_cms.getRequestContext().getCurrentProject();
    }

    private void addContentValue(CmsXmlContent content, Locale locale, String path, String value) {

        if (!content.hasValue(path, locale)) {
            String[] pathElements = path.split("/");
            String currentPath = pathElements[0];
            for (int i = 0; i < pathElements.length; i++) {
                if (i > 0) {
                    currentPath = CmsStringUtil.joinPaths(currentPath, pathElements[i]);
                }
                while (!content.hasValue(currentPath, locale)) {
                    content.addValue(m_cms, currentPath, locale, CmsXmlUtils.getXpathIndexInt(currentPath));
                }
            }
        }
        content.getValue(path, locale).setStringValue(m_cms, value);
    }

    private CmsXmlContent addContentValues(CmsFile file, Map<String, String> contentValues) throws CmsException {

        CmsXmlContent content = CmsXmlContentFactory.unmarshal(m_cms, file);
        Locale locale = m_cms.getRequestContext().getLocale();

        if (!content.hasLocale(locale)) {
            content.addLocale(m_cms, locale);
        }
        List<String> paths = new ArrayList<String>(contentValues.keySet());
        Collections.sort(paths, new PathComparator());
        for (String path : paths) {
            addContentValue(content, locale, path, contentValues.get(path));
        }
        return content;
    }

    private String generateProjectName() {

        return "Edit project";
    }

    private String getNewResourceName() {

        return CmsStringUtil.joinPaths(m_cms.getSitePath(m_configuration.getContentParentFolder()), "newResource");
    }
}
