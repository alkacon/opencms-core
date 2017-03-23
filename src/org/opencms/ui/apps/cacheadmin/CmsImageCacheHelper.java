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

package org.opencms.ui.apps.cacheadmin;

import com.alkacon.simapi.Simapi;

import org.opencms.file.CmsObject;
import org.opencms.file.CmsProject;
import org.opencms.file.CmsResource;
import org.opencms.file.CmsResourceFilter;
import org.opencms.loader.CmsImageLoader;
import org.opencms.main.CmsException;
import org.opencms.main.OpenCms;
import org.opencms.util.CmsFileUtil;
import org.opencms.util.CmsStringUtil;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Image Cache helper.<p>
 *
 * @since 7.0.5
 */
public class CmsImageCacheHelper {

    /**File path map. */
    private Map<String, String> m_filePaths = new HashMap<String, String>();

    /** Lengths map. */
    private Map m_lengths = new HashMap();

    /** Sizes map. */
    private Map m_sizes = new HashMap();

    /** Variations map. */
    private Map m_variations = new HashMap();

    /** The total number of variations. */
    private int m_variationsCount;

    /** The total size of all variations. */
    private int m_variationsSize;

    /**
     * Default constructor.<p>
     *
     * @param cms the cms context
     * @param withVariations if also variations should be read
     * @param showSize if it is needed to compute the image size
     * @param statsOnly if only statistical information should be retrieved
     */
    public CmsImageCacheHelper(CmsObject cms, boolean withVariations, boolean showSize, boolean statsOnly) {

        init(cms, withVariations, showSize, statsOnly);
    }

    /**
     * Returns all cached images.<p>
     *
     * @return a list of root paths
     */
    public List getAllCachedImages() {

        List ret = new ArrayList(m_variations.keySet());
        Collections.sort(ret);
        return ret;
    }

    /**
     * Returns the total number of files.<p>
     *
     * @return the total number of files
     */
    public int getFilesCount() {

        return m_variations.keySet().size();
    }

    /**
     * Returns the length of the given image.<p>
     *
     * @param imgName the image name
     *
     * @return the length of the given image
     */
    public String getLength(String imgName) {

        String ret = (String)m_lengths.get(imgName);
        if (ret == null) {
            return "";
        }
        return ret;
    }

    /**
     * Reads the size of a single image.<p>
     *
     * @param cms CmsObejct
     * @param resPath Path of image (uri)
     * @return a String representation of the dimension of the given image
     * @throws CmsException if something goes wrong
     */
    public String getSingleSize(CmsObject cms, String resPath) throws CmsException {

        CmsResource res = getClonedCmsObject(cms).readResource(resPath);
        return getSingleSize(cms, res);
    }

    /**
     * Returns the size of the given image.<p>
     *
     * @param imgName the image name
     *
     * @return the size of the given image
     */
    public String getSize(String imgName) {

        String ret = (String)m_sizes.get(imgName);
        if (ret == null) {
            return "";
        }
        return ret;
    }

    /**
     * Returns the variations for the given image.<p>
     *
     * @param imgName the image name
     *
     * @return the variations for the given image
     */
    public List getVariations(String imgName) {

        List ret = (List)m_variations.get(imgName);
        if (ret == null) {
            return new ArrayList();
        }
        Collections.sort(ret);
        return ret;
    }

    /**
     * Returns the total number of variations.<p>
     *
     * @return the total number of variations
     */
    public int getVariationsCount() {

        return m_variationsCount;
    }

    /**
     * Returns the total size of all variations.<p>
     *
     * @return the total size of all variations
     */
    public int getVariationsSize() {

        return m_variationsSize;
    }

    /**
     * Reads out the path of a given image (by its name).<p>
     *
     * @param resName name of image
     * @return path to it on Server
     */
    String getFilePath(String resName) {

        return m_filePaths.get(resName);
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

    /**
     * Reads the size of a single image.<p>
     *
     * @param cms CmsObejct
     * @param res CmsResource to be read
     * @return a String representation of the dimension of the given image
     */
    private String getSingleSize(CmsObject cms, CmsResource res) {

        try {
            BufferedImage img = Simapi.read(cms.readFile(res).getContents());
            return "" + img.getWidth() + " x " + img.getHeight() + "px";
        } catch (CmsException | IOException e) {
            return "";
        }
    }

    /**
     * Reads all cached images.<p>
     *
     * @param cms the cms context
     * @param withVariations if also variations should be read
     * @param showSize if it is needed to compute the image size
     * @param statsOnly if only statistical information should be retrieved
     */
    private void init(CmsObject cms, boolean withVariations, boolean showSize, boolean statsOnly) {

        File basedir = new File(CmsImageLoader.getImageRepositoryPath());
        try {
            CmsObject clonedCms = getClonedCmsObject(cms);
            visitImages(clonedCms, basedir, withVariations, showSize, statsOnly);
        } catch (CmsException e) {
            // should never happen
        }
        m_variations = Collections.unmodifiableMap(m_variations);
        m_sizes = Collections.unmodifiableMap(m_sizes);
        m_lengths = Collections.unmodifiableMap(m_lengths);
    }

    /**
     * Visits a single image.<p>
     *
     * @param cms CmsObject
     * @param f a File to be read out
     * @param withVariations boolean
     * @param showSize boolean
     * @param statsOnly boolean
     */
    private void visitImage(CmsObject cms, File f, boolean withVariations, boolean showSize, boolean statsOnly) {

        m_variationsCount++;
        m_variationsSize += f.length();
        String oName = f.getAbsolutePath().substring(CmsImageLoader.getImageRepositoryPath().length());
        oName = CmsStringUtil.substitute(oName, "\\", "/");
        if (!oName.startsWith("/")) {
            oName = "/" + oName;
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
            oName = res.getRootPath();
        }
        m_filePaths.put(oName, f.getAbsolutePath());
        List variations = (List)m_variations.get(oName);
        if (variations == null) {
            variations = new ArrayList();
            m_variations.put(oName, variations);
            if (statsOnly) {
                return;
            }
            if (res != null) {
                m_lengths.put(oName, "" + res.getLength() + " Bytes");
                if (showSize) {
                    m_sizes.put(oName, getSingleSize(cms, res));
                }
            } else {
                m_lengths.put(oName, "" + f.length() + " Bytes");
                if (showSize) {
                    try {
                        BufferedImage img = Simapi.read(f);
                        m_sizes.put(oName, "" + img.getWidth() + " x " + img.getHeight() + "px");
                    } catch (Throwable e) {
                        // ignore
                    }
                }
            }
        }
        if (!withVariations) {
            return;
        }
        oName += " (";
        if (showSize) {
            try {
                BufferedImage img = Simapi.read(f);
                oName += "" + img.getWidth() + " x " + img.getHeight() + "px - ";
            } catch (Throwable e) {
                // ignore
            }
        }
        oName += f.length() + " Bytes)";
        variations.add(oName);
    }

    /**
    * Visits all cached images in the given directory.<p>
    *
    * @param cms the cms context
    * @param directory the directory to visit
    * @param withVariations if also variations should be read
    * @param showSize if it is needed to compute the image size
    * @param statsOnly if only statistical information should be retrieved
    */
    private void visitImages(
        CmsObject cms,
        File directory,
        boolean withVariations,
        boolean showSize,
        boolean statsOnly) {

        if (!directory.canRead() || !directory.isDirectory()) {
            return;
        }
        File[] files = directory.listFiles();
        for (int i = 0; i < files.length; i++) {
            File f = files[i];
            if (f.isDirectory()) {
                visitImages(cms, f, withVariations, showSize, statsOnly);
                continue;
            }
            visitImage(cms, f, withVariations, showSize, statsOnly);
        }
    }

}
