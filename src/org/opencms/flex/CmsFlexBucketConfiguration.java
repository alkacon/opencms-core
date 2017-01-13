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

package org.opencms.flex;

import org.opencms.db.CmsPublishedResource;
import org.opencms.file.CmsFile;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsResource;
import org.opencms.main.CmsException;
import org.opencms.main.CmsLog;
import org.opencms.util.CmsFileUtil;
import org.opencms.util.CmsStringUtil;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.BitSet;
import java.util.Collections;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import org.apache.commons.logging.Log;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

/**
 * Represents a Flex bucket configuration.<p>
 *
 * This consists of a list of flex bucket definitions, and a 'clear all' list.<p>
 *
 * Each flex bucket definition consists of a name and a list of paths. If any resources from the given list of paths or their
 * descendants is published, the corresponding Flex bucket's contents should be removed from the Flex cache.
 *
 * If a resource with its path below one of the paths from the 'clear all' list is published, the complete Flex cache should be
 * cleared.
 *
 */
public class CmsFlexBucketConfiguration {

    /**
     * A data structure representing a set of Flex cache buckets.<p>
     */
    public class BucketSet {

        /** A bit set, with each bit index representing a bucket. */
        private BitSet m_bits = new BitSet();

        /**
         * Creates a new instance from a set of bucket names.<p>
         *
         * @param buckets the bucket names
         */
        protected BucketSet(Set<String> buckets) {
            for (String bucket : buckets) {
                int index = getBucketIndex(bucket);
                if (index >= 0) {
                    m_bits.set(index);
                }
            }
        }

        /**
         * Computes the list of bucket names for this instance.<p>
         *
         * @return the list of bucket names
         */
        public List<String> getBucketNames() {

            List<String> result = Lists.newArrayList();
            for (int i = m_bits.nextSetBit(0); i >= 0; i = m_bits.nextSetBit(i + 1)) {
                result.add(getBucketName(i));
            }
            return result;
        }

        /**
         * If this entry is the bucket set created from a publish list, and the argument is the bucket list
         * of a flex cache entry, then the result of this method determines whether the flex cache entry for which
         * the argument bucket set was created should be removed.<p>
         *
         * @param flexEntryBucketSet the bucket set for the flex cache entry
         *
         * @return true if the flex cache entry from which argument flex bucket set was generated should be removed.<p>
         */
        public boolean matchForDeletion(BucketSet flexEntryBucketSet) {

            if (flexEntryBucketSet == null) {
                return true;
            }
            BitSet otherBits = flexEntryBucketSet.m_bits;
            BitSet commonBits = (BitSet)(m_bits.clone());
            commonBits.and(otherBits);
            return !(commonBits.isEmpty());
        }

        /**
         * @see java.lang.Object#toString()
         */
        @Override
        public String toString() {

            return "[BucketSet:" + getBucketNames().toString() + "]";
        }

    }

    /** Special bucket name for everything that doesn't belong in any other bucket. */
    public static final String BUCKET_OTHER = "OTHER";

    /** Configuration key for the list of folders for which the whole flex cache should be purged when a resource in them is published. */
    public static final String KEY_CLEAR_ALL = "clearAll";

    /** The configuration key prefix used to define a bucket. */
    public static final String KEY_PREFIX_BUCKET = "bucket.";

    /** Logger instance for this class. */
    private static final Log LOG = CmsLog.getLog(CmsFlexBucketConfiguration.class);

    /** The list of bucket names. */
    private List<String> m_bucketNames = Lists.newArrayList();

    /** The list of bucket paths for the bucket at the corresponding list in m_bucketNames. */
    private List<List<String>> m_bucketPathLists = Lists.newArrayList();

    /** A list of paths for which the flex cache should be cleared completely if any resources below them are published. */
    private List<String> m_clearAll = Lists.newArrayList("/system/modules");

    /** Flag which, when set, prevents further modification of this configuration object. */
    private boolean m_frozen;

    /**
     * Loads the flex bucket configuration from a java.util.Properties instance.<p>
     *
     * @param properties the properties from which to load the configuration
     * @return the configuration
     */
    public static CmsFlexBucketConfiguration loadFromProperties(Properties properties) {

        ArrayListMultimap<String, String> multimap = ArrayListMultimap.create();
        List<String> clearAll = Lists.newArrayList();
        for (Object keyObj : properties.keySet()) {
            String key = (String)keyObj;
            key = key.trim();
            String value = (String)(properties.get(key));
            value = value.trim();
            if (key.startsWith(KEY_PREFIX_BUCKET)) {
                String bucketName = key.substring(KEY_PREFIX_BUCKET.length());
                multimap.putAll(bucketName, Arrays.asList(value.trim().split(" *, *")));
            } else if (KEY_CLEAR_ALL.equals(key)) {
                clearAll = Arrays.asList(value.trim().split(" *, *"));
            }
        }
        CmsFlexBucketConfiguration result = new CmsFlexBucketConfiguration();
        if (!clearAll.isEmpty()) {
            result.setClearAll(clearAll);
        }
        for (String key : multimap.keySet()) {
            result.add(key, multimap.get(key));
        }
        result.freeze();
        return result;
    }

    /**
     * Loads a flex bucket configuration from the OpenCms VFS.<p>
     *
     * @param cms the CMS context to use for VFS operations
     * @param path the path of the resource
     *
     * @return the flex bucket configuration
     *
     * @throws CmsException if something goes wrong
     */
    public static CmsFlexBucketConfiguration loadFromVfsFile(CmsObject cms, String path) throws CmsException {

        if (!cms.existsResource(path)) {
            return null;
        }
        CmsResource configRes = cms.readResource(path);
        if (configRes.isFolder()) {
            return null;
        }
        CmsFile configFile = cms.readFile(configRes);
        String encoding = CmsFileUtil.getEncoding(cms, configRes);
        Properties props = new Properties();
        try {
            props.load(new InputStreamReader(new ByteArrayInputStream(configFile.getContents()), encoding));
            return loadFromProperties(props);
        } catch (IOException e) {
            LOG.error(e.getLocalizedMessage(), e);
        }
        return null;
    }

    /**
     * Adds a flex bucket definition, consisting of a flex bucket name and a list of paths.<p>
     *
     * @param key the flex bucket name
     * @param values the flex bucket paths
     */
    public void add(String key, List<String> values) {

        if (m_frozen) {
            throw new IllegalStateException("Can not modify frozen CmsFlexBucketConfiguration");
        }
        m_bucketNames.add(key);
        m_bucketPathLists.add(values);
    }

    /**
     * Freetze
     */
    public void freeze() {

        m_frozen = true;
    }

    /**
     * Gets the bucket name for the given bit index.<p>
     *
     * @param bitIndex the bit index for the bucket in the bit set representation.<p>
     *
     * @return the name of the bucket
     */
    public String getBucketName(int bitIndex) {

        if (bitIndex == 0) {
            return BUCKET_OTHER;
        } else {
            String result = null;
            if ((bitIndex - 1) < m_bucketNames.size()) {
                result = m_bucketNames.get(bitIndex - 1);
            }
            if (result == null) {
                return "??? " + bitIndex;
            } else {
                return result;
            }
        }
    }

    /**
     * Computes the bucket set for a set of paths based on this configuration.<p>
     *
     * The resulting bucket set contains all buckets for which one of the given paths is below the
     * configured roots of that bucket.
     *
     * @param paths a list of root paths
     *
     * @return the bucket set for the input paths
     */
    public BucketSet getBucketSet(Iterable<String> paths) {

        Set<String> bucketNames = Sets.newHashSet();
        for (String path : paths) {
            bucketNames.addAll(getBucketsForPath(path));
        }
        if (LOG.isDebugEnabled()) {
            LOG.debug("Determined bucket set " + bucketNames.toString() + " for path set " + paths);
        }
        return new BucketSet(bucketNames);
    }

    /**
     * Sets the 'clear all' list, a list of paths for which the complete Flex cache should be cleared if any resource
     * below them is published.<p>
     *
     * @param clearAll a list of paths
     */
    public void setClearAll(List<String> clearAll) {

        if (m_frozen) {
            throw new IllegalStateException("Can not modify frozen CmsFlexBucketConfiguration");
        }
        m_clearAll = Collections.unmodifiableList(clearAll);
    }

    /**
     * Returns true if for the given publish list, the complete Flex cache should be cleared based on this configuration.<p>
     *
     * @param publishedResources a publish list
     * @return true if the complete Flex cache should be cleared
     */
    public boolean shouldClearAll(List<CmsPublishedResource> publishedResources) {

        for (CmsPublishedResource pubRes : publishedResources) {
            for (String clearPath : m_clearAll) {
                if (CmsStringUtil.isPrefixPath(clearPath, pubRes.getRootPath())) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Gets the bucket bit index for the given bucket name.<p>
     *
     * @param bucketName a bucket name
     * @return the bit index for the bucket
     */
    int getBucketIndex(String bucketName) {

        if (bucketName.equals(BUCKET_OTHER)) {
            return 0;
        }
        for (int i = 0; i < m_bucketNames.size(); i++) {
            if (m_bucketNames.get(i).equals(bucketName)) {
                return 1 + i;
            }
        }
        return -1;
    }

    /**
     * Gets the bucket of which the given path is a part.<p>
     *
     * @param path a root path
     * @return the set of buckets for the given path
     */
    private Set<String> getBucketsForPath(String path) {

        Set<String> result = Sets.newHashSet();
        boolean foundBucket = false;
        for (int i = 0; i < m_bucketNames.size(); i++) {
            for (String bucketPath : m_bucketPathLists.get(i)) {
                if (CmsStringUtil.isPrefixPath(bucketPath, path)) {
                    String bucketName = m_bucketNames.get(i);
                    result.add(bucketName);
                    if (!BUCKET_OTHER.equals(bucketName)) {
                        foundBucket = true;
                    }
                }
            }
        }
        if (!foundBucket) {
            result.add(BUCKET_OTHER);
        }
        return result;
    }
}
