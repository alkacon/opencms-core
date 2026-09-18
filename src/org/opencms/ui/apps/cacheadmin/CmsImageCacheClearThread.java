/*
 * This library is part of OpenCms -
 * The Open Source Content Management System
 *
 * Copyright (c) Alkacon Software GmbH & Co. KG (https://www.alkacon.com)
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 */

package org.opencms.ui.apps.cacheadmin;

import org.opencms.file.CmsObject;
import org.opencms.loader.CmsImageLoader;
import org.opencms.loader.imagecache.CmsImageCacheMaintenanceCleaner;
import org.opencms.loader.imagecache.CmsImageCacheMaintenanceService;
import org.opencms.main.CmsLog;
import org.opencms.main.OpenCms;
import org.opencms.report.A_CmsReportThread;
import org.opencms.report.I_CmsReport;
import org.opencms.scheduler.jobs.CmsImageCacheCleanupJob;
import org.opencms.ui.apps.Messages;

import java.time.Instant;

import org.apache.commons.logging.Log;

/**
 * Clears image cache entries in a report thread.<p>
 */
public class CmsImageCacheClearThread extends A_CmsReportThread {

    /** Number of entries passed to one backend delete request. */
    private static final int DELETE_BATCH_SIZE = 1000;

    /** The logger. */
    private static final Log LOG = CmsLog.getLog(CmsImageCacheClearThread.class);

    /** The exclusive last-modified cutoff, or {@code null} for all entries. */
    private final Instant m_cutoff;

    /**
     * Creates the report thread.<p>
     *
     * @param cms the current CMS context
     * @param cutoff the exclusive last-modified cutoff, or {@code null} for all entries
     */
    public CmsImageCacheClearThread(CmsObject cms, Instant cutoff) {

        super(cms, "imageCacheClear");
        m_cutoff = cutoff;
        initHtmlReport(cms.getRequestContext().getLocale());
    }

    /**
     * @see org.opencms.report.A_CmsReportThread#getReportUpdate()
     */
    @Override
    public String getReportUpdate() {

        return getReport().getReportUpdate();
    }

    /**
     * @see java.lang.Thread#run()
     */
    @Override
    public void run() {

        I_CmsReport report = getReport();
        report.println(Messages.get().container(Messages.RPT_IMAGECACHE_CLEAR_BEGIN_0), I_CmsReport.FORMAT_HEADLINE);
        try {
            if (CmsImageLoader.getImageCache() == null) {
                float maxAge = m_cutoff == null
                ? -1f
                : (System.currentTimeMillis() - m_cutoff.toEpochMilli()) / (60f * 60f * 1000f);
                int deleted = CmsImageCacheCleanupJob.cleanImageCache(maxAge);
                report.println(
                    Messages.get().container(Messages.RPT_IMAGECACHE_CLEAR_END_3, 0, deleted, 0),
                    I_CmsReport.FORMAT_OK);
                return;
            }
            CmsImageCacheMaintenanceService service = CmsImageCacheMaintenanceService.createForConfiguredCache();
            int deleteBatchSize = DELETE_BATCH_SIZE;
            if ("s3".equals(service.getBackendId())) {
                long concurrentBatchSize = (long)OpenCms.getImageCacheConfiguration().getS3DeleteBatchSize()
                    * OpenCms.getImageCacheConfiguration().getS3DeleteConcurrency();
                deleteBatchSize = (int)Math.min(Integer.MAX_VALUE, concurrentBatchSize);
            }
            CmsImageCacheMaintenanceCleaner.Result result = CmsImageCacheMaintenanceCleaner.delete(
                service,
                m_cutoff,
                deleteBatchSize,
                (progress, batch) -> {
                    report.println(
                        Messages.get().container(
                            Messages.RPT_IMAGECACHE_CLEAR_PROGRESS_3,
                            Long.valueOf(progress.getScanned()),
                            Long.valueOf(progress.getMatched()),
                            Long.valueOf(progress.getSucceeded())),
                        batch.getFailed() == 0 ? I_CmsReport.FORMAT_DEFAULT : I_CmsReport.FORMAT_WARNING);
                    for (Exception failure : batch.getFailures().values()) {
                        report.println(failure);
                    }
                });
            report.println(
                Messages.get().container(
                    Messages.RPT_IMAGECACHE_CLEAR_END_3,
                    Long.valueOf(result.getScanned()),
                    Long.valueOf(result.getSucceeded()),
                    Long.valueOf(result.getFailed())),
                result.getFailed() == 0 ? I_CmsReport.FORMAT_OK : I_CmsReport.FORMAT_WARNING);
        } catch (Exception e) {
            report.println(e);
            LOG.error("Unable to clear the image cache.", e);
        }
    }
}
