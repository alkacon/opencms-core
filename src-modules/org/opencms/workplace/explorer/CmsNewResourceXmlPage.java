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
 * For further information about Alkacon Software GmbH & Co. KG, please see the
 * company website: http://www.alkacon.com
 *
 * For further information about OpenCms, please see the
 * project website: http://www.opencms.org
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package org.opencms.workplace.explorer;

import org.opencms.file.CmsFile;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsPropertyDefinition;
import org.opencms.file.CmsResource;
import org.opencms.file.CmsResourceFilter;
import org.opencms.main.CmsException;
import org.opencms.main.CmsLog;
import org.opencms.util.CmsStringUtil;
import org.opencms.workplace.CmsWorkplace;

import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;

import org.apache.commons.logging.Log;

/**
 * The new resource page dialog handles the creation of an xml page.<p>
 *
 * The following files use this class:
 * <ul>
 * <li>/commons/newresource_xmlpage.jsp
 * </ul>
 * <p>
 *
 * @since 6.0.0
 */
public class CmsNewResourceXmlPage {

    /** Request parameter name for the selected body. */
    public static final String PARAM_BODYFILE = "bodyfile";

    /** Request parameter name for the suffix check. */
    public static final String PARAM_SUFFIXCHECK = "suffixcheck";

    /** Request parameter name for the selected template. */
    public static final String PARAM_TEMPLATE = "template";

    /** The log object for this class. */
    private static final Log LOG = CmsLog.getLog(CmsNewResourceXmlPage.class);

    /**
     * Returns a sorted Map of all available body files of the OpenCms modules.<p>
     *
     * @param cms the current cms object
     * @param currWpPath the current path in the OpenCms workplace
     *
     * @return a sorted map with the body file title as key and absolute path to the body file as value
     *
     * @throws CmsException if reading a folder or file fails
     */
    public static TreeMap<String, String> getBodies(CmsObject cms, String currWpPath) throws CmsException {

        return getElements(cms, CmsWorkplace.VFS_DIR_DEFAULTBODIES, currWpPath, true);
    }

    /**
     * Returns a sorted Map of all available body files of the OpenCms modules.<p>
     *
     * @param cms the current cms object
     * @param currWpPath the current path in the OpenCms workplace
     * @param emptyMap flag indicating if it is OK to return a filtered empty Map
     *
     * @return a sorted map with the body file title as key and absolute path to the body file as value
     *
     * @throws CmsException if reading a folder or file fails
     */
    public static TreeMap<String, String> getBodies(CmsObject cms, String currWpPath, boolean emptyMap)
    throws CmsException {

        return getElements(cms, CmsWorkplace.VFS_DIR_DEFAULTBODIES, currWpPath, emptyMap);
    }

    /**
     * Returns a sorted Map of all available templates of the OpenCms modules.<p>
     *
     * @param cms the current cms object
     * @param currWpPath the current path in the OpenCms workplace
     *
     * @return a sorted map with the template title as key and absolute path to the template as value
     *
     * @throws CmsException if reading a folder or file fails
     */
    public static TreeMap<String, String> getTemplates(CmsObject cms, String currWpPath) throws CmsException {

        return getElements(cms, CmsWorkplace.VFS_DIR_TEMPLATES, currWpPath, true);
    }

    /**
     * Returns a sorted Map of all available templates of the OpenCms modules.<p>
     *
     * @param cms the current cms object
     * @param currWpPath the current path in the OpenCms workplace
     * @param emptyMap flag indicating if it is OK to return a filtered empty Map
     *
     * @return a sorted map with the template title as key and absolute path to the template as value
     *
     * @throws CmsException if reading a folder or file fails
     */
    public static TreeMap<String, String> getTemplates(CmsObject cms, String currWpPath, boolean emptyMap)
    throws CmsException {

        return getElements(cms, CmsWorkplace.VFS_DIR_TEMPLATES, currWpPath, emptyMap);
    }

    /**
     * Returns a sorted Map of all available elements in the specified subfolder of the OpenCms modules.<p>
     *
     * @param cms the current cms object
     * @param elementFolder the module subfolder to search for elements
     * @param currWpPath the current path in the OpenCms workplace
     * @param emptyMap flag indicating if it is OK to return a filtered empty Map
     *
     * @return a sorted map with the element title as key and absolute path to the element as value
     *
     * @throws CmsException if reading a folder or file fails
     */
    protected static TreeMap<String, String> getElements(
        CmsObject cms,
        String elementFolder,
        String currWpPath,
        boolean emptyMap)
    throws CmsException {

        TreeMap<String, String> elements = new TreeMap<String, String>();
        TreeMap<String, String> allElements = new TreeMap<String, String>();

        if (CmsStringUtil.isNotEmpty(currWpPath)) {
            // add site root to current workplace path
            currWpPath = cms.getRequestContext().addSiteRoot(currWpPath);
        }

        // get all visible template elements in the module folders
        List<CmsResource> modules = cms.getSubFolders(
            CmsWorkplace.VFS_PATH_MODULES,
            CmsResourceFilter.IGNORE_EXPIRATION);
        for (int i = 0; i < modules.size(); i++) {
            List<CmsResource> moduleTemplateFiles = new ArrayList<CmsResource>();
            String folder = cms.getSitePath(modules.get(i));
            try {
                moduleTemplateFiles = cms.getFilesInFolder(
                    folder + elementFolder,
                    CmsResourceFilter.DEFAULT.addRequireVisible());
            } catch (CmsException e) {
                // folder not available, list will be empty
                if (LOG.isDebugEnabled()) {
                    LOG.debug(e.getMessage(), e);
                }
            }
            for (int j = 0; j < moduleTemplateFiles.size(); j++) {
                // get the current template file
                CmsFile templateFile = (CmsFile)moduleTemplateFiles.get(j);
                String title = null;
                String folderProp = null;
                try {
                    title = cms.readPropertyObject(
                        cms.getSitePath(templateFile),
                        CmsPropertyDefinition.PROPERTY_TITLE,
                        false).getValue();
                    folderProp = cms.readPropertyObject(
                        templateFile,
                        CmsPropertyDefinition.PROPERTY_FOLDERS_AVAILABLE,
                        false).getValue();
                } catch (CmsException e) {
                    // property not available, will be null
                    if (LOG.isInfoEnabled()) {
                        LOG.info(e.getLocalizedMessage(), e);
                    }
                }

                boolean isInFolder = false;
                // check template folders property value
                if (CmsStringUtil.isNotEmpty(currWpPath) && CmsStringUtil.isNotEmpty(folderProp)) {
                    // property value set on template, check if current workplace path fits
                    List<String> folders = CmsStringUtil.splitAsList(folderProp, CmsNewResource.DELIM_PROPERTYVALUES);
                    for (int k = 0; k < folders.size(); k++) {
                        String checkFolder = folders.get(k);
                        if (currWpPath.startsWith(checkFolder)) {
                            isInFolder = true;
                            break;
                        }
                    }
                } else {
                    isInFolder = true;
                }

                if (title == null) {
                    // no title property found, display the file name
                    title = templateFile.getName();
                }
                String path = cms.getSitePath(templateFile);
                if (isInFolder) {
                    // element is valid, add it to result
                    elements.put(title, path);
                }
                // also put element to overall result
                allElements.put(title, path);
            }
        }
        if (!emptyMap && (elements.size() < 1)) {
            // empty Map should not be returned, return all collected elements
            return allElements;
        }
        // return the filtered elements sorted by title
        return elements;
    }
}
