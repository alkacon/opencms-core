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

package org.opencms.file.quota;

import org.opencms.file.CmsObject;
import org.opencms.file.CmsResource;
import org.opencms.main.CmsException;
import org.opencms.main.CmsLog;
import org.opencms.main.OpenCms;
import org.opencms.util.CmsFileUtil;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.apache.commons.logging.Log;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;

/**
 * Object for storing folder size information for all folders in the VFS, and for efficiently updating and retrieving it.
 *
 * <p>Mutable, not threadsafe by itself.
 */
public class CmsFolderSizeTable {

    /** Logger instance for this class. */
    private static final Log LOG = CmsLog.getLog(CmsFolderSizeTable.class);

    /** A CMS context. */
    private CmsObject m_cms;

    private TreeMap<String, Long> m_folders = new TreeMap<>();

    private Map<String, Long> m_subtreeCache;

    /**
     * Creates a deep copy of an existing instance.
     *
     * @param other
     */
    public CmsFolderSizeTable(CmsFolderSizeTable other) {

        try {
            this.m_cms = OpenCms.initCmsObject(other.m_cms);
        } catch (Exception e) {
            // shouldn't happen
            LOG.error(e.getLocalizedMessage(), e);
            this.m_cms = other.m_cms;
        }
        this.m_folders = new TreeMap<>(other.m_folders);
        updateSubtreeCache();
    }

    /**
     * Create a new instance.
     *
     * @param cms the CMS context
     */
    public CmsFolderSizeTable(CmsObject cms) {

        m_cms = cms;
    }

    /**
     * Prepares a folder report consisting of subtree sizes for a bunch of folders.
     *
     * <p>This is more efficient than querying for folder sizes individually.
     *
     * @param folders the folders
     * @return the folder report
     */
    public Map<String, CmsFolderReportEntry> getFolderReport(Collection<String> folders) {

        Map<String, Long> subtreeCache = getSubtreeCache();
        Set<String> folderSet = new HashSet<>();
        for (String folder : folders) {
            folderSet.add(normalize(folder));
        }
        // to compute the exclusive folder sizes, we need to take the total subtree size for the given folder and then subtract
        // the subtree sizes of its immediate descendants.
        // For example, if our paths are /a/, /a/b/, and /a/b/d/, to get the exclusive size for /a/, we must *not* subtract the size of /a/b/d/.
        // So we first need to determine the direct descendants for each folder.
        Multimap<String, String> directDescendants = ArrayListMultimap.create();
        for (String folder : folderSet) {
            String currentAncestor = CmsResource.getParentFolder(folder);
            String directAncestor = null;
            while (currentAncestor != null) {
                if (folderSet.contains(currentAncestor)) {
                    directAncestor = currentAncestor;
                    break;
                }
                currentAncestor = CmsResource.getParentFolder(currentAncestor);
            }
            if (directAncestor != null) {
                directDescendants.put(directAncestor, folder);
            }
        }
        Map<String, CmsFolderReportEntry> result = new HashMap<>();
        for (String folder : folderSet) {
            long size = subtreeCache.getOrDefault(folder, Long.valueOf(0));
            long exclusiveSize = size;
            for (String child : directDescendants.get(folder)) {
                exclusiveSize -= subtreeCache.getOrDefault(child, Long.valueOf(0));
            }
            result.put(folder, new CmsFolderReportEntry(size, exclusiveSize));
        }
        return result;
    }

    /**
     * Gets the total folder size for the complete subtree at the given root path.
     *
     * @param rootPath the root path for which to compute the size
     * @return the total size
     */
    public long getTotalFolderSize(String rootPath) {

        Long size = getSubtreeCache().get(normalize(rootPath));
        if (size != null) {
            return size.longValue();
        } else {
            return 0;
        }
    }

    /**
     * Gets the folder size for the subtree at the given root path, but without including any folder sizes
     * of subtrees at any paths from 'otherPaths' of which rootPath is a proper prefix.
     *
     * @param rootPath the root path for which to calculate the size
     * @param otherPaths the other paths to exclude from the size
     *
     * @return the total size
     */
    public long getTotalFolderSizeExclusive(String rootPath, Collection<String> otherPaths) {

        Set<String> paths = new HashSet<>(otherPaths);
        rootPath = normalize(rootPath);
        paths.add(rootPath);
        return getFolderReport(paths).get(rootPath).getTreeSizeExclusive();

    }

    /**
     * Loads all folder size data.
     *
     * @throws CmsException if something goes wrong
     */
    public void loadAll() throws CmsException {

        TreeMap<String, Long> folders = new TreeMap<>();
        List<CmsFolderSizeEntry> stats = m_cms.readFolderSizeStats(new CmsFolderSizeOptions("/", true));
        for (CmsFolderSizeEntry entry : stats) {
            folders.put(normalize(entry.getRootPath()), Long.valueOf(entry.getSize()));
        }
        m_folders = folders;
        m_subtreeCache = null;
    }

    /**
     * Updates the folder size for a single folder, not including subfolders.
     *
     * @param rootPath the root path of the folder for which to update the information
     * @throws CmsException if something goes wrong
     */
    public void updateSingle(String rootPath) throws CmsException {

        List<CmsFolderSizeEntry> entries = m_cms.readFolderSizeStats(new CmsFolderSizeOptions(rootPath, false));
        m_folders.remove(normalize(rootPath));
        if (entries.size() > 0) {
            m_folders.put(normalize(entries.get(0).getRootPath()), Long.valueOf(entries.get(0).getSize()));
        }
        m_subtreeCache = null;
    }

    /**
     * Updates the information for a complete subtree.
     *
     * @param rootPath the root path for which to update the information
     * @throws CmsException if something goes wrong
     */
    public void updateTree(String rootPath) throws CmsException {

        List<CmsFolderSizeEntry> entries = m_cms.readFolderSizeStats(new CmsFolderSizeOptions(rootPath, true));
        rootPath = normalize(rootPath);
        m_folders.subMap(rootPath, rootPath + Character.MAX_VALUE).clear();
        for (CmsFolderSizeEntry entry : entries) {
            m_folders.put(normalize(entry.getRootPath()), Long.valueOf(entry.getSize()));
        }
        m_subtreeCache = null;
    }

    /**
     * Gets the subtree cache.
     *
     * @return the subtree cache
     */
    private Map<String, Long> getSubtreeCache() {

        if (m_subtreeCache == null) {
            updateSubtreeCache();
        }
        return m_subtreeCache;
    }

    /**
     * Normalizes a path to include a trailing separator.
     *
     * @param path the path to normalize
     * @return the normalized path
     */
    private String normalize(String path) {

        if ("".equals(path)) {
            return "/";
        }
        if ("/".equals(path)) {
            return path;
        }
        return CmsFileUtil.addTrailingSeparator(path);
    }

    /**
     * Updates the subtree cache.
     */
    private void updateSubtreeCache() {

        Map<String, Long> subtreeCache = new HashMap<>();
        for (Map.Entry<String, Long> entry : m_folders.entrySet()) {
            String currentPath = entry.getKey();
            while (currentPath != null) {
                subtreeCache.put(
                    currentPath,
                    Long.valueOf(subtreeCache.computeIfAbsent(currentPath, key -> 0l) + entry.getValue()));
                currentPath = CmsResource.getParentFolder(currentPath);
            }
        }
        m_subtreeCache = subtreeCache;
    }

}
