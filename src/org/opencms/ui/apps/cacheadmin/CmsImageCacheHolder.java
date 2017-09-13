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

package org.opencms.ui.apps.cacheadmin;

import org.opencms.file.CmsObject;
import org.opencms.file.CmsProject;
import org.opencms.file.CmsResource;
import org.opencms.file.CmsResourceFilter;
import org.opencms.loader.CmsImageLoader;
import org.opencms.main.CmsException;
import org.opencms.main.OpenCms;
import org.opencms.ui.A_CmsUI;
import org.opencms.util.CmsFileUtil;
import org.opencms.util.CmsStringUtil;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Helper class for getting information about cached images.<p>
 */
public class CmsImageCacheHolder {

    /**Maps Cache-file name to VFS name. */
    private static Map<String, String> PATH_TO_VFS_NAME = new HashMap<String, String>();

    /**File path map. */
    private Map m_filePaths = new HashMap();

    /** Lengths map. */
    private Map m_lengths = new HashMap();

    /** Sizes map. */
    private Map m_sizes = new HashMap();

    /** Variations map. */
    private Map m_variations = new HashMap();

    /**Filter for files (and dictionaries). */
    private FilenameFilter m_filter;

    /**Cms Object. */
    protected CmsObject m_clonedCms;

    /**
     * public constructor.<p>
     * @param search
     */
    public CmsImageCacheHolder(final String search) {

        try {
            m_clonedCms = getClonedCmsObject(A_CmsUI.getCmsObject());
        } catch (CmsException e) {
            m_clonedCms = A_CmsUI.getCmsObject();
        }
        m_filter = new FilenameFilter() {

            public boolean accept(File dir, String name) {

                String spatt = search.replace("*", "");
                if (new File(dir, name).isDirectory()) {
                    return true;
                }

                String fullPath = dir.getAbsolutePath() + "/" + name;

                fullPath = fullPath.substring(CmsImageLoader.getImageRepositoryPath().length() - 1);
                return getVFSName(m_clonedCms, fullPath).contains(spatt);

            }
        };
        if (!search.startsWith("*") & search.startsWith("/")) {
            String root = getRootFromPattern(search);
            if (root.length() > 1) {
                readAllImagesAndVariations(root.substring(1).replace("*", ""));
            } else {
                readAllImagesAndVariations("");
            }
        } else {
            readAllImagesAndVariations("");
        }

    }

    /**
     * Returns all cached images.<p>
     *
     * @return a list of root paths
     */
    public List<String> getAllCachedImages() {

        List<String> ret = new ArrayList<String>(m_variations.keySet());
        Collections.sort(ret);
        return ret;
    }

    /**
     * Get variations of resource.<p>
     *
     * @param resource to get variations for
     * @return list of CmsVariationBean
     */
    public List<CmsVariationBean> getVariations(String resource) {

        List<String> ret = (List<String>)(m_filePaths.get(resource));
        List<CmsVariationBean> res = new ArrayList<CmsVariationBean>();
        if (ret == null) {
            return new ArrayList<CmsVariationBean>();
        }
        for (String r : ret) {
            res.add(new CmsVariationBean(r));
        }
        return res;
    }

    /**
     * Get the amount of variations for resource.<p>
     *
     * @param resource to get variations for
     * @return amount of variations
     */
    public int getVariationsCount(String resource) {

        return ((List<String>)m_variations.get(resource)).size();
    }

    /**
     * Get name of image in the VFS.<p>
     *
     * @param cms CmsObject
     * @param oName Name of cached image file
     * @return vfs resource name (root path)
     */
    String getVFSName(CmsObject cms, String oName) {

        oName = CmsStringUtil.substitute(oName, "\\", "/");
        if (!oName.startsWith("/")) {
            oName = "/" + oName;
        }

        if (PATH_TO_VFS_NAME.containsKey(oName)) {
            return PATH_TO_VFS_NAME.get(oName);
        }

        String imgName = oName;
        CmsResource res = null;
        boolean found = false;
        while (!found) {
            String path = CmsResource.getParentFolder(imgName);
            String name = imgName.substring(path.length());
            String ext = CmsFileUtil.getExtension(imgName);
            String nameWoExt = name.substring(0, name.length() - ext.length());
            int pos = nameWoExt.lastIndexOf("_");
            String newName = path;
            found = (pos < 0);
            if (!found) {
                newName += nameWoExt.substring(0, pos);
            } else {
                newName += nameWoExt;
            }
            newName += ext;
            try {
                res = cms.readResource(newName, CmsResourceFilter.ALL);
                found = true;
            } catch (Exception e) {
                // it could be a variation
            }
            imgName = newName;
        }

        if (res != null) {
            PATH_TO_VFS_NAME.put(oName, res.getRootPath());
            return res.getRootPath();
        }
        return "";
    }

    /**
     * Clones a CmsObject.<p>
     *
     * @param cms the CmsObject to be cloned.
     * @return a clones CmsObject
     * @throws CmsException if something goes wrong
     */
    private CmsObject getClonedCmsObject(CmsObject cms) throws CmsException {

        CmsObject clonedCms = OpenCms.initCmsObject(cms);
        // only online images get caches
        clonedCms.getRequestContext().setCurrentProject(clonedCms.readProject(CmsProject.ONLINE_PROJECT_ID));
        // paths are always root path
        clonedCms.getRequestContext().setSiteRoot("");

        return clonedCms;
    }

    private String getRootFromPattern(String pattern) {

        String res = pattern.substring(0, pattern.lastIndexOf("/"));

        return res;
    }

    /**
     * Fille the list m_variations and m_filePaths.<p>
     */
    private void readAllImagesAndVariations(String root) {

        File basedir = new File(CmsImageLoader.getImageRepositoryPath() + root);
        visitImages(m_clonedCms, basedir);
        m_variations = Collections.unmodifiableMap(m_variations);
        m_sizes = Collections.unmodifiableMap(m_sizes);
        m_lengths = Collections.unmodifiableMap(m_lengths);

    }

    /**
     * Visits a single image.<p>
     *
     * @param cms CmsObject
     * @param f a File to be read out
     */
    private void visitImage(CmsObject cms, File f) {

        f.length();
        String oName = f.getAbsolutePath().substring(CmsImageLoader.getImageRepositoryPath().length());
        oName = getVFSName(cms, oName);

        List files = (List)m_filePaths.get(oName);
        if (files == null) {
            files = new ArrayList();
            m_filePaths.put(oName, files);
        }
        files.add(f.getAbsolutePath());

        List variations = (List)m_variations.get(oName);
        if (variations == null) {
            variations = new ArrayList();
            m_variations.put(oName, variations);

        }
        oName += " (";
        oName += f.length() + " Bytes)";
        variations.add(oName);
    }

    /**
    * Visits all cached images in the given directory.<p>
    *
    * @param cms the cms context
    * @param directory the directory to visit
    */
    private void visitImages(CmsObject cms, File directory) {

        if (!directory.canRead() || !directory.isDirectory()) {
            return;
        }
        File[] files = directory.listFiles(m_filter);
        for (int i = 0; i < files.length; i++) {
            File f = files[i];
            if (f.isDirectory()) {
                visitImages(cms, f);
                continue;
            }
            visitImage(cms, f);
        }
    }
}
