/*
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (c) Alkacon Software GmbH & Co. KG (https://www.alkacon.com)
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
 * company website: https://www.alkacon.com
 *
 * For further information about OpenCms, please see the
 * project website: https://www.opencms.org
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
import org.opencms.loader.I_CmsImageCache;
import org.opencms.loader.imagecache.CmsImageCacheMaintenanceService;
import org.opencms.main.CmsException;
import org.opencms.main.CmsLog;
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

import org.apache.commons.logging.Log;

/**
 * Helper class for getting information about cached images.<p>
 */
public class CmsImageCacheHolder {

    /** The logger for this class. */
    private static final Log LOG = CmsLog.getLog(CmsImageCacheHolder.class);

    /** Maps cache file names and their intermediate names to VFS names for this listing operation. */
    private Map<String, String> m_pathToVfsName = new HashMap<String, String>();

    /**File path map. */
    private Map<String, List<CmsVariationBean>> m_filePaths = new HashMap<>();

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
        I_CmsImageCache imageCache = CmsImageLoader.getImageCache();
        if (imageCache != null) {
            readExternalImageCache(imageCache, search);
            freezeMaps();
            return;
        }
        if (CmsImageLoader.getImageRepositoryPath() == null) {
            freezeMaps();
            return;
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
     * Gets a cache key prefix which can safely be applied before resolving cache keys to VFS paths.<p>
     *
     * The file name is deliberately excluded because image cache keys contain hashes before the extension.
     *
     * @param search the search expression
     * @return the cache key prefix
     */
    static String getCacheKeyPrefix(String search) {

        if (CmsStringUtil.isEmptyOrWhitespaceOnly(search) || search.startsWith("*") || !search.startsWith("/")) {
            return "";
        }
        int firstWildcard = search.indexOf('*');
        String fixedPrefix = firstWildcard < 0 ? search : search.substring(0, firstWildcard);
        int lastSlash = fixedPrefix.lastIndexOf('/');
        if (lastSlash <= 0) {
            return "";
        }
        return fixedPrefix.substring(1, lastSlash + 1);
    }

    /**
     * Gets the original VFS path encoded in a current external image cache key.<p>
     *
     * @param key the image cache key
     * @return the original VFS path, or an empty string if the key does not use the expected format
     */
    static String getVfsNameFromExternalCacheKey(String key) {

        if (CmsStringUtil.isEmptyOrWhitespaceOnly(key)) {
            return "";
        }
        String normalizedKey = CmsStringUtil.substitute(key, "\\", "/");
        if (!normalizedKey.startsWith("/")) {
            normalizedKey = "/" + normalizedKey;
        }
        String extension = CmsFileUtil.getExtension(normalizedKey);
        String nameWithoutExtension = normalizedKey.substring(0, normalizedKey.length() - extension.length());
        int pathEnd = nameWithoutExtension.lastIndexOf('/');
        int parameterSeparator = nameWithoutExtension.lastIndexOf('_');
        if (parameterSeparator <= pathEnd) {
            return "";
        }
        String parameterHash = nameWithoutExtension.substring(parameterSeparator + 1);
        if (!isLowerCaseHexHash(parameterHash)) {
            return "";
        }
        int resourceSeparator = nameWithoutExtension.lastIndexOf('_', parameterSeparator - 1);
        if (resourceSeparator <= pathEnd) {
            return "";
        }
        String resourceHash = nameWithoutExtension.substring(resourceSeparator + 1, parameterSeparator);
        if (!isInteger(resourceHash)) {
            return "";
        }
        return nameWithoutExtension.substring(0, resourceSeparator) + extension;
    }

    /** Checks whether a value represents an integer. */
    private static boolean isInteger(String value) {

        if (CmsStringUtil.isEmpty(value)) {
            return false;
        }
        int start = value.charAt(0) == '-' ? 1 : 0;
        if (start == value.length()) {
            return false;
        }
        for (int i = start; i < value.length(); i++) {
            char character = value.charAt(i);
            if ((character < '0') || (character > '9')) {
                return false;
            }
        }
        return true;
    }

    /** Checks whether a value is a 32 character lower-case hexadecimal hash. */
    private static boolean isLowerCaseHexHash(String value) {

        if (value.length() != 32) {
            return false;
        }
        for (int i = 0; i < value.length(); i++) {
            char character = value.charAt(i);
            if (!(((character >= '0') && (character <= '9')) || ((character >= 'a') && (character <= 'f')))) {
                return false;
            }
        }
        return true;
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

        List<CmsVariationBean> ret = m_filePaths.get(resource);
        if (ret == null) {
            return new ArrayList<CmsVariationBean>();
        }
        return new ArrayList<CmsVariationBean>(ret);
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

        if (m_pathToVfsName.containsKey(oName)) {
            return m_pathToVfsName.get(oName);
        }

        String imgName = oName;
        List<String> attemptedNames = new ArrayList<String>();
        while (true) {
            String path = CmsResource.getParentFolder(imgName);
            String name = imgName.substring(path.length());
            String ext = CmsFileUtil.getExtension(imgName);
            String nameWoExt = name.substring(0, name.length() - ext.length());
            int pos = nameWoExt.lastIndexOf("_");
            String newName = path;
            if (pos >= 0) {
                newName += nameWoExt.substring(0, pos);
            } else {
                newName += nameWoExt;
            }
            newName += ext;
            attemptedNames.add(newName);
            if (m_pathToVfsName.containsKey(newName)) {
                String result = m_pathToVfsName.get(newName);
                cacheVfsName(attemptedNames, oName, result);
                return result;
            }
            try {
                CmsResource res = cms.readResource(newName, CmsResourceFilter.ALL);
                String result = res.getRootPath();
                cacheVfsName(attemptedNames, oName, result);
                return result;
            } catch (Exception e) {
                // it could be a variation
            }
            if (pos < 0) {
                cacheVfsName(attemptedNames, oName, "");
                return "";
            }
            imgName = newName;
        }
    }

    /**
     * Adds a variation description.<p>
     *
     * @param vfsName the VFS resource name
     * @param length the cache entry length
     */
    private void addVariation(String vfsName, long length) {

        List variations = (List)m_variations.get(vfsName);
        if (variations == null) {
            variations = new ArrayList();
            m_variations.put(vfsName, variations);

        }
        variations.add(vfsName + " (" + length + " Bytes)");
    }

    /** Caches a resolved VFS name for the cache key and all names inspected while resolving it. */
    private void cacheVfsName(List<String> attemptedNames, String cacheKey, String vfsName) {

        m_pathToVfsName.put(cacheKey, vfsName);
        for (String attemptedName : attemptedNames) {
            m_pathToVfsName.put(attemptedName, vfsName);
        }
    }

    /** Freezes the result maps. */
    private void freezeMaps() {

        m_variations = Collections.unmodifiableMap(m_variations);
        m_sizes = Collections.unmodifiableMap(m_sizes);
        m_lengths = Collections.unmodifiableMap(m_lengths);
        m_filePaths = Collections.unmodifiableMap(m_filePaths);
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

        String repositoryPath = CmsImageLoader.getImageRepositoryPath();
        if (repositoryPath == null) {
            return;
        }
        File basedir = new File(repositoryPath + root);
        visitImages(m_clonedCms, basedir);
        freezeMaps();

    }

    /**
     * Reads all entries from an external image cache.<p>
     *
     * @param imageCache the external image cache
     * @param search the search expression
     */
    private void readExternalImageCache(I_CmsImageCache imageCache, String search) {

        String pattern = search.replace("*", "");
        String cacheKeyPrefix = getCacheKeyPrefix(search);
        try {
            CmsImageCacheMaintenanceService maintenance = CmsImageCacheMaintenanceService.create(
                imageCache,
                OpenCms.getImageCacheConfiguration());
            imageCache.visitEntries(cacheKeyPrefix, (key, length) -> {
                String vfsName = getVfsNameFromExternalCacheKey(key);
                if (CmsStringUtil.isEmptyOrWhitespaceOnly(vfsName)) {
                    vfsName = getVFSName(m_clonedCms, key);
                }
                if (CmsStringUtil.isEmptyOrWhitespaceOnly(vfsName)) {
                    vfsName = key.startsWith("/") ? key : "/" + key;
                }
                if (vfsName.contains(pattern)) {
                    visitImage(vfsName, new CmsVariationBean(imageCache, maintenance, key, length), length);
                }
            });
        } catch (Exception e) {
            LOG.warn("Unable to list the configured image cache.", e);
        }
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

        List<CmsVariationBean> files = m_filePaths.get(oName);
        if (files == null) {
            files = new ArrayList<CmsVariationBean>();
            m_filePaths.put(oName, files);
        }
        files.add(new CmsVariationBean(f.getAbsolutePath()));

        addVariation(oName, f.length());
    }

    /**
     * Adds an external image cache entry.<p>
     *
     * @param vfsName the VFS resource name
     * @param variation the variation bean
     * @param length the cache entry length
     */
    private void visitImage(String vfsName, CmsVariationBean variation, long length) {

        List<CmsVariationBean> files = m_filePaths.get(vfsName);
        if (files == null) {
            files = new ArrayList<CmsVariationBean>();
            m_filePaths.put(vfsName, files);
        }
        files.add(variation);
        addVariation(vfsName, length);
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
