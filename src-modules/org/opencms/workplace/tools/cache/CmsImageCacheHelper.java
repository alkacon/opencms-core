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

package org.opencms.workplace.tools.cache;

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
            CmsObject clonedCms = OpenCms.initCmsObject(cms);
            // only online images get caches
            clonedCms.getRequestContext().setCurrentProject(clonedCms.readProject(CmsProject.ONLINE_PROJECT_ID));
            // paths are always root path
            clonedCms.getRequestContext().setSiteRoot("");
            // get the images
            visitImages(clonedCms, basedir, withVariations, showSize, statsOnly);
        } catch (CmsException e) {
            // should never happen
        }
        m_variations = Collections.unmodifiableMap(m_variations);
        m_sizes = Collections.unmodifiableMap(m_sizes);
        m_lengths = Collections.unmodifiableMap(m_lengths);
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
            List variations = (List)m_variations.get(oName);
            if (variations == null) {
                variations = new ArrayList();
                m_variations.put(oName, variations);
                if (statsOnly) {
                    continue;
                }
                if (res != null) {
                    m_lengths.put(oName, "" + res.getLength() + " Bytes");
                    if (showSize) {
                        try {
                            BufferedImage img = Simapi.read(cms.readFile(res).getContents());
                            m_sizes.put(oName, "" + img.getWidth() + " x " + img.getHeight() + "px");
                        } catch (Throwable e) {
                            // ignore
                        }
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
                continue;
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
    }
}
