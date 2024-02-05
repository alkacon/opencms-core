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

package org.opencms.scheduler.jobs;

import org.opencms.file.CmsObject;
import org.opencms.loader.CmsImageLoader;
import org.opencms.main.CmsLog;
import org.opencms.scheduler.I_CmsScheduledJob;

import java.io.File;
import java.util.Map;

import org.apache.commons.logging.Log;

/**
 * A schedulable OpenCms job that clear the image cache for the scaled images created by the <code>{@link org.opencms.loader.CmsImageLoader}</code>.<p>
 *
 * Job parameters:<p>
 * <dl>
 * <dt><code>maxage={time in hours}</code></dt>
 * <dd>Specifies the maximum age (in hours) images can be unused before they are removed from the cache.
 * Any image in the image cache folder that has a RFS date of last modification older than this time is considered
 * expired and is therefore deleted.</dd>
 * </dl>
 *
 * @since 6.2.0
 */
public class CmsImageCacheCleanupJob implements I_CmsScheduledJob {

    /** Unlock parameter. */
    public static final String PARAM_MAXAGE = "maxage";

    /** The log object for this class. */
    private static final Log LOG = CmsLog.getLog(CmsImageCacheCleanupJob.class);

    /**
     * Removes all expired image cache entries from the RFS cache.<p>
     *
     * Empty directories are removed as well.<p>
     *
     * @param maxAge the maximum age of the image cache files in hours (or fractions of hours)
     *
     * @return the total number of deleted resources
     */
    public static int cleanImageCache(float maxAge) {

        // calculate oldest possible date for the cache files
        long expireDate = System.currentTimeMillis() - (long)(maxAge * 60f * 60f * 1000f);
        File basedir = new File(CmsImageLoader.getImageRepositoryPath());
        // perform the cache cleanup
        return cleanImageCache(expireDate, basedir);
    }

    /**
     * Removes all expired image cache entries from the given RFS directory, including recursion to subdirectories.<p>
     *
     * @param maxAge the maximum age of the image cache files
     * @param directory the directory to remove the cache files in
     *
     * @return the total number of deleted resources
     */
    private static int cleanImageCache(long maxAge, File directory) {

        int count = 0;
        if (directory.canRead() && directory.isDirectory()) {
            File[] files = directory.listFiles();
            for (int i = 0; i < files.length; i++) {
                File f = files[i];
                if (f.isDirectory()) {
                    count += cleanImageCache(maxAge, f);
                }
                if (f.canWrite()) {
                    if (f.lastModified() < maxAge) {
                        try {
                            f.delete();
                            count++;
                        } catch (Exception e) {
                            LOG.error(
                                Messages.get().getBundle().key(
                                    Messages.LOG_IMAGE_CACHE_UNABLE_TO_DELETE_1,
                                    f.getAbsolutePath()));
                        }
                    }
                }
            }
            if (directory.listFiles().length <= 0) {
                try {
                    directory.delete();
                    count++;
                } catch (Exception e) {
                    LOG.error(
                        Messages.get().getBundle().key(
                            Messages.LOG_IMAGE_CACHE_UNABLE_TO_DELETE_1,
                            directory.getAbsolutePath()));
                }
            }
        }
        return count;
    }

    /**
     * @see org.opencms.scheduler.I_CmsScheduledJob#launch(CmsObject, Map)
     */
    public String launch(CmsObject cms, Map<String, String> parameters) throws Exception {

        if (!CmsImageLoader.isEnabled() || (CmsImageLoader.getImageRepositoryPath() == null)) {
            // scaling functions are not available
            return Messages.get().getBundle().key(Messages.LOG_IMAGE_SCALING_DISABLED_0);
        }

        String maxAgeStr = parameters.get(PARAM_MAXAGE);
        float maxAge;
        try {
            maxAge = Float.parseFloat(maxAgeStr);
        } catch (NumberFormatException e) {
            // in case of an error, use maxage of one week
            maxAge = 24f * 7f;
            LOG.error(
                Messages.get().getBundle().key(Messages.LOG_IMAGE_CACHE_BAD_MAXAGE_2, maxAgeStr, Float.valueOf(maxAge)));
        }

        // now perform the image cache cleanup
        int count = cleanImageCache(maxAge);

        return Messages.get().getBundle().key(Messages.LOG_IMAGE_CACHE_CLEANUP_COUNT_1, Integer.valueOf(count));
    }
}