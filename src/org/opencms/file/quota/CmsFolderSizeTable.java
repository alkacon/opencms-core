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
import org.opencms.util.CmsStringUtil;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.collections4.trie.PatriciaTrie;
import org.apache.commons.logging.Log;

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

    /** Trie in which the folder size information is organized by path. */
    private PatriciaTrie<CmsFolderSizeEntry> m_folders = new PatriciaTrie<>();

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
        this.m_folders = new PatriciaTrie<>(other.m_folders);
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

        Set<String> folderSet = new HashSet<>();
        List<String> sortedFolders = new ArrayList<>();

        Map<String, CmsFolderReportEntry> result = new HashMap<>();

        for (String folder : folders) {
            folder = normalize(folder);
            sortedFolders.add(folder);
            result.put(folder, new CmsFolderReportEntry(0, 0));
        }
        Collections.sort(sortedFolders);
        String prev = null;

        // All subfolders of a folder must come in a single block after it in a sorted list,
        // and by skipping them, we end up with just the (mutually disjoint) parent folders.
        Set<String> prefixes = new HashSet<>(folderSet);
        for (String folder : sortedFolders) {
            if ((prev == null) || !CmsStringUtil.isPrefixPath(prev, folder)) {
                prefixes.add(folder);
                prev = folder;
            }
        }
        for (String prefix : prefixes) {
            for (Map.Entry<String, CmsFolderSizeEntry> entry : m_folders.prefixMap(prefix).entrySet()) {
                String currentPath = entry.getKey();
                boolean firstMatch = true;
                while (currentPath != null) {
                    CmsFolderReportEntry resultEntry = result.get(currentPath);
                    if (resultEntry != null) {
                        resultEntry.m_treeSize += entry.getValue().getSize();
                        if (firstMatch) {
                            resultEntry.m_treeSizeExclusive += entry.getValue().getSize();
                        }
                        firstMatch = false;
                    }
                    currentPath = CmsResource.getParentFolder(currentPath);
                }
            }
        }
        return Collections.unmodifiableMap(result);
    }

    /**
     * Gets the total folder size for the complete subtree at the given root path.
     *
     * @param rootPath the root path for which to compute the size
     * @return the total size
     */
    public long getTotalFolderSize(String rootPath) {

        long result = 0;

        for (Map.Entry<String, CmsFolderSizeEntry> entry : m_folders.prefixMap(normalize(rootPath)).entrySet()) {
            result += entry.getValue().getSize();
        }
        return result;
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

        long result = 0;

        PatriciaTrie<CmsFolderSizeEntry> subtrie = new PatriciaTrie<>(m_folders.prefixMap(normalize(rootPath)));
        for (String otherPath : otherPaths) {
            if (!CmsStringUtil.isPrefixPath(otherPath, rootPath)) {
                subtrie.prefixMap(normalize(otherPath)).clear();
            }
        }
        for (Map.Entry<String, CmsFolderSizeEntry> entry : subtrie.entrySet()) {
            result += entry.getValue().getSize();
        }
        return result;

    }

    /**
     * Loads all folder size data.
     *
     * @throws CmsException if something goes wrong
     */
    public void loadAll() throws CmsException {

        PatriciaTrie<CmsFolderSizeEntry> folders = new PatriciaTrie<>();
        List<CmsFolderSizeEntry> stats = m_cms.readFolderSizeStats(new CmsFolderSizeOptions("/", true));
        for (CmsFolderSizeEntry entry : stats) {
            folders.put(normalize(entry.getRootPath()), entry);
        }
        m_folders = folders;
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
            m_folders.put(normalize(entries.get(0).getRootPath()), entries.get(0));
        }
    }

    /**
     * Updates the information for a complete subtree.
     *
     * @param rootPath the root path for which to update the information
     * @throws CmsException if something goes wrong
     */
    public void updateTree(String rootPath) throws CmsException {

        List<CmsFolderSizeEntry> entries = m_cms.readFolderSizeStats(new CmsFolderSizeOptions(rootPath, true));
        m_folders.prefixMap(normalize(rootPath)).clear();
        for (CmsFolderSizeEntry entry : entries) {
            m_folders.put(normalize(entry.getRootPath()), entry);
        }
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

}
