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

package org.opencms.loader;

import org.opencms.ade.galleries.CmsPreviewService;
import org.opencms.cache.CmsVfsNameBasedDiskCache;
import org.opencms.configuration.CmsConfigurationException;
import org.opencms.configuration.CmsParameterConfiguration;
import org.opencms.configuration.I_CmsConfigurationParameterHandler;
import org.opencms.db.storage.CmsFsStorage;
import org.opencms.db.storage.CmsS3Storage;
import org.opencms.db.storage.CmsStorageManager;
import org.opencms.file.CmsFile;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsResource;
import org.opencms.main.CmsEvent;
import org.opencms.main.CmsException;
import org.opencms.main.CmsLog;
import org.opencms.main.I_CmsEventListener;
import org.opencms.main.OpenCms;
import org.opencms.scheduler.jobs.CmsImageCacheCleanupJob;
import org.opencms.security.CmsPermissionSet;
import org.opencms.staticexport.CmsImageCacheConfiguration;
import org.opencms.staticexport.CmsStaticExportManager;
import org.opencms.util.CmsFileUtil;
import org.opencms.util.CmsRequestUtil;
import org.opencms.util.CmsStringUtil;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.logging.Log;

/**
 * Loader for images from the OpenCms VSF with integrated image scaling and processing capabilities.<p>
 *
 * To scale or process an image, the parameter <code>{@link org.opencms.loader.CmsImageScaler#PARAM_SCALE}</code>
 * has to be appended to the image URI. The value for the parameter needs to be composed from the <code>SCALE_PARAM</code>
 * options provided by the constants in the <code>{@link org.opencms.file.types.CmsResourceTypeImage}</code> class.<p>
 *
 * For example, to scale an image to exact 800x600 pixel with center fitting and a background color of grey,
 * the following parameter String can be used: <code>w:800,h:600,t:0,c:c0c0c0</code>.<p>
 *
 * @since 6.2.0
 */
public class CmsImageLoader extends CmsDumpLoader implements I_CmsEventListener, I_CmsStaticExportDirectResponseLoader {

    /** Controls max number of threads that are allowed to scale images concurrently. */
    public static final String CONFIGURATION_CONCURRENCY = "image.scaling.concurrency";

    /** The configuration parameter for the OpenCms XML configuration to set the image down scale operation. */
    public static final String CONFIGURATION_DOWNSCALE = "image.scaling.downscale";

    /** The configuration parameter for the OpenCms XML configuration to set the image cache repository. */
    public static final String CONFIGURATION_IMAGE_FOLDER = "image.folder";

    /** The configuration parameter for the OpenCms XML configuration to set the maximum image blur size. */
    public static final String CONFIGURATION_MAX_BLUR_SIZE = "image.scaling.maxblursize";

    /** The configuration parameter for the OpenCms XML configuration to set the maximum image scale size. */
    public static final String CONFIGURATION_MAX_SCALE_SIZE = "image.scaling.maxsize";

    /** The configuration parameter for the OpenCms XML configuration to enable the image scaling. */
    public static final String CONFIGURATION_SCALING_ENABLED = "image.scaling.enabled";

    /** Weak ETag prefix. */
    private static final String WEAK_ETAG_PREFIX = "W/";

    /** Default name for the image cache repository. */
    public static final String IMAGE_REPOSITORY_DEFAULT = "/WEB-INF/imagecache/";

    /** Clear event parameter. */
    public static final String PARAM_CLEAR_IMAGES_CACHE = "_IMAGES_CACHE_";

    /** The id of this loader. */
    public static final int RESOURCE_LOADER_ID_IMAGE_LOADER = 2;

    /** The log object for this class. */
    protected static final Log LOG = CmsLog.getLog(CmsImageLoader.class);

    /** The (optional) image down scale parameters for image write operations. */
    protected static String m_downScaleParams;

    /** Indicates if image scaling is active. */
    protected static boolean m_enabled;

    /** The maximum image size (width * height) to apply image blurring when down scaling (setting this to high may cause "out of memory" errors). */
    protected static int m_maxBlurSize = CmsImageScaler.SCALE_DEFAULT_MAX_BLUR_SIZE;

    /** The disk cache to use for saving scaled image versions. */
    protected static CmsVfsNameBasedDiskCache m_vfsDiskCache;

    /** The name of the configured image cache repository. */
    protected String m_imageRepositoryFolder;

    /** The maximum image size (width or height) to allow when up scaling an image using request parameters. */
    protected int m_maxScaleSize = CmsImageScaler.SCALE_DEFAULT_MAX_SIZE;

    /** The optional image cache. */
    protected I_CmsImageCache m_imageCache;

    /** If the image cache configuration has been applied after startup configuration was available. */
    protected boolean m_imageCacheConfigurationApplied;

    /**
     * Creates a new image loader.<p>
     */
    public CmsImageLoader() {

        super();
    }

    /**
     * Returns the image down scale parameters,
     * which is set with the {@link #CONFIGURATION_DOWNSCALE} configuration option.<p>
     *
     * If no down scale parameters have been set in the configuration, this will return <code>null</code>.
     *
     * @return the image down scale parameters
     */
    public static String getDownScaleParams() {

        return m_downScaleParams;
    }

    /**
     * Returns the path of the image cache repository folder in the RFS,
     * which is set with the {@link #CONFIGURATION_IMAGE_FOLDER} configuration option.<p>
     *
     * @return the path of the image cache repository folder in the RFS
     */
    public static String getImageRepositoryPath() {

        return m_vfsDiskCache.getRepositoryPath();
    }

    /**
     * The maximum blur size for image re-scale operations,
     * which is set with the {@link #CONFIGURATION_MAX_BLUR_SIZE} configuration option.<p>
     *
     * The default is 2500 * 2500 pixel.<p>
     *
     * @return the maximum blur size for image re-scale operations
     */
    public static int getMaxBlurSize() {

        return m_maxBlurSize;
    }

    /**
     * Returns <code>true</code> if the image scaling and processing capabilities for the
     * OpenCms VFS images have been enabled, <code>false</code> if not.<p>
     *
     * Image scaling is enabled by setting the loader parameter <code>image.scaling.enabled</code>
     * to the value <code>true</code> in the configuration file <code>opencms-vfs.xml</code>.<p>
     *
     * Enabling image processing in OpenCms may require several additional configuration steps
     * on the server running OpenCms, especially in UNIX systems. Here it is often required to have an X window server
     * configured and accessible so that the required Java ImageIO operations work.
     * Therefore the image scaling capabilities in OpenCms are disabled by default.<p>
     *
     * @return <code>true</code> if the image scaling and processing capabilities for the
     *      OpenCms VFS images have been enabled
     */
    public static boolean isEnabled() {

        return m_enabled;
    }

    /**
     * @see org.opencms.configuration.I_CmsConfigurationParameterHandler#addConfigurationParameter(java.lang.String, java.lang.String)
     */
    @Override
    public void addConfigurationParameter(String paramName, String paramValue) {

        if (CmsStringUtil.isNotEmpty(paramName) && CmsStringUtil.isNotEmpty(paramValue)) {
            if (CONFIGURATION_SCALING_ENABLED.equals(paramName)) {
                m_enabled = Boolean.valueOf(paramValue).booleanValue();
            }
            if (CONFIGURATION_IMAGE_FOLDER.equals(paramName)) {
                m_imageRepositoryFolder = paramValue.trim();
            }
            if (CONFIGURATION_MAX_SCALE_SIZE.equals(paramName)) {
                m_maxScaleSize = CmsStringUtil.getIntValue(
                    paramValue,
                    CmsImageScaler.SCALE_DEFAULT_MAX_SIZE,
                    paramName);
            }
            if (CONFIGURATION_MAX_BLUR_SIZE.equals(paramName)) {
                m_maxBlurSize = CmsStringUtil.getIntValue(
                    paramValue,
                    CmsImageScaler.SCALE_DEFAULT_MAX_BLUR_SIZE,
                    paramName);
            }
            if (CONFIGURATION_DOWNSCALE.equals(paramName)) {
                m_downScaleParams = paramValue.trim();
            }

            if (CONFIGURATION_CONCURRENCY.equals(paramName)) {
                int concurrency = CmsStringUtil.getIntValue(paramValue, CmsImageScaler.DEFAULT_CONCURRENCY, paramName);
                if (concurrency > 0) {
                    CmsImageScaler.setConcurrency(concurrency);
                }
            }
        }
        super.addConfigurationParameter(paramName, paramValue);
    }

    /**
     * @see org.opencms.main.I_CmsEventListener#cmsEvent(org.opencms.main.CmsEvent)
     */
    public void cmsEvent(CmsEvent event) {

        if (event == null) {
            return;
        }
        // only react on the clear caches event
        int type = event.getType();
        if (type != I_CmsEventListener.EVENT_CLEAR_CACHES) {
            return;
        }
        // only react if the clear images cache parameter is set
        Map<String, ?> data = event.getData();
        if (data == null) {
            return;
        }
        Object param = data.get(PARAM_CLEAR_IMAGES_CACHE);
        if (param == null) {
            return;
        }
        float age = -1;
        if (param instanceof String) {
            age = Float.valueOf((String)param).floatValue();
        } else if (param instanceof Number) {
            age = ((Number)param).floatValue();
        }
        CmsImageCacheCleanupJob.cleanImageCache(age);
    }

    /**
     * @see org.opencms.loader.I_CmsResourceLoader#destroy()
     */
    @Override
    public void destroy() {

        m_enabled = false;
        m_vfsDiskCache = null;
        m_imageCache = null;
        m_imageCacheConfigurationApplied = false;
        m_imageRepositoryFolder = null;
    }

    /**
     * @see org.opencms.loader.I_CmsResourceLoader#export(org.opencms.file.CmsObject, org.opencms.file.CmsResource, javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
     */
    @Override
    public byte[] export(CmsObject cms, CmsResource resource, HttpServletRequest req, HttpServletResponse res)
    throws IOException, CmsException {

        if (m_enabled && (req != null)) {
            ensureImageCacheConfiguration();
            CmsImageScaler scaler = new CmsImageScaler(req, m_maxScaleSize, m_maxBlurSize);
            if (scaler.isValid()) {
                if (m_imageCache != null) {
                    String key = ensureImageCacheEntry(cms, resource, scaler);
                    ByteArrayOutputStream out = new ByteArrayOutputStream();
                    writeImageCacheEntryTo(key, out);
                    CmsFile file = (resource instanceof CmsFile) ? (CmsFile)resource : new CmsFile(resource);
                    file.setContents(out.toByteArray());
                    if (res != null) {
                        loadImageCacheEntry(resource, key, req, res);
                    }
                    return file.getContents();
                }
                CmsFile file = getScaledImage(cms, resource, scaler);
                if (res != null) {
                    super.load(cms, file, req, res);
                }
                return file.getContents();
            }
        }
        return super.export(cms, resource, req, res);
    }

    /**
     * @see org.opencms.loader.I_CmsStaticExportStreamLoader#exportTo(org.opencms.file.CmsObject, org.opencms.file.CmsResource, javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse, java.io.OutputStream)
     */
    @Override
    public void exportTo(
        CmsObject cms,
        CmsResource resource,
        HttpServletRequest req,
        HttpServletResponse res,
        OutputStream exportOut)
    throws IOException, CmsException {

        if (m_enabled && (req != null)) {
            ensureImageCacheConfiguration();
            CmsImageScaler scaler = new CmsImageScaler(req, m_maxScaleSize, m_maxBlurSize);
            if (scaler.isValid()) {
                if (m_imageCache != null) {
                    String key = ensureImageCacheEntry(cms, resource, scaler);
                    writeImageCacheEntryTo(key, exportOut);
                    if (res != null) {
                        loadImageCacheEntry(resource, key, req, res);
                    }
                    return;
                }
                File imageCacheFile = getScaledImageFile(cms, resource, scaler);
                try (FileInputStream in = new FileInputStream(imageCacheFile)) {
                    CmsFileUtil.copy(in, exportOut);
                }
                if (res != null) {
                    loadCacheFile(resource, imageCacheFile, req, res);
                }
                return;
            }
        }
        super.exportTo(cms, resource, req, res, exportOut);
    }

    /**
     * @see org.opencms.configuration.I_CmsConfigurationParameterHandler#getConfiguration()
     */
    @Override
    public CmsParameterConfiguration getConfiguration() {

        CmsParameterConfiguration result = new CmsParameterConfiguration();
        CmsParameterConfiguration config = super.getConfiguration();
        if (config != null) {
            result.putAll(config);
        }
        return result;
    }

    /**
     * @see org.opencms.loader.I_CmsResourceLoader#getLoaderId()
     */
    @Override
    public int getLoaderId() {

        return RESOURCE_LOADER_ID_IMAGE_LOADER;
    }

    /**
     * Returns the RFS cache file for the image scaler parameters from the request.<p>
     *
     * @param cms the current users OpenCms context
     * @param resource the base VFS resource for the image
     * @param req the current request
     *
     * @return the scaled image cache file, or <code>null</code> if the request contains no valid scaling parameters
     *
     * @throws IOException in case of errors accessing the disk based cache
     * @throws CmsException in case of errors accessing the OpenCms VFS
     */
    public File getScaledImageFile(CmsObject cms, CmsResource resource, HttpServletRequest req)
    throws IOException, CmsException {

        CmsImageScaler scaler = new CmsImageScaler(req, m_maxScaleSize, m_maxBlurSize);
        if (scaler.isValid()) {
            return getScaledImageFile(cms, resource, scaler);
        }
        return null;
    }

    /**
     * @see org.opencms.configuration.I_CmsConfigurationParameterHandler#initConfiguration()
     */
    @Override
    public void initConfiguration() throws CmsConfigurationException {

        initDefaultImageCache();
        m_imageCacheConfigurationApplied = false;
        OpenCms.addCmsEventListener(this);
        // output setup information
        if (CmsLog.INIT.isInfoEnabled()) {
            CmsLog.INIT.info(
                Messages.get().getBundle().key(
                    Messages.INIT_IMAGE_REPOSITORY_PATH_1,
                    m_vfsDiskCache.getRepositoryPath()));
            CmsLog.INIT.info(
                Messages.get().getBundle().key(Messages.INIT_IMAGE_SCALING_ENABLED_1, Boolean.valueOf(m_enabled)));
        }
    }

    /**
     * @see org.opencms.loader.I_CmsStoredContentDirectDeliveryLoader#isStoredContentDirectDeliveryEnabled(org.opencms.file.CmsObject, org.opencms.file.CmsResource, javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
     */
    @Override
    public boolean isStoredContentDirectDeliveryEnabled(
        CmsObject cms,
        CmsResource resource,
        HttpServletRequest req,
        HttpServletResponse res)
    throws CmsException {

        if ((req == null) || !m_enabled) {
            return true;
        }
        CmsImageScaler scaler = new CmsImageScaler(req, m_maxScaleSize, m_maxBlurSize);
        return !scaler.isValid();
    }

    /**
     * @see org.opencms.loader.I_CmsResourceLoader#load(org.opencms.file.CmsObject, org.opencms.file.CmsResource, javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
     */
    @Override
    public void load(CmsObject cms, CmsResource resource, HttpServletRequest req, HttpServletResponse res)
    throws IOException, CmsException {

        if (m_enabled) {
            ensureImageCacheConfiguration();
            if (canSendLastModifiedHeader(resource, req, res)) {
                // no image processing required at all
                return;
            }
            // get the scale information from the request
            CmsImageScaler scaler = new CmsImageScaler(req, m_maxScaleSize, m_maxBlurSize);
            if (scaler.isValid()) {
                if (m_imageCache != null) {
                    String key = ensureImageCacheEntry(cms, resource, scaler);
                    loadImageCacheEntry(resource, key, req, res);
                    return;
                }
                File file = getScaledImageFile(cms, resource, scaler);
                loadCacheFile(resource, file, req, res);
            } else {
                super.load(cms, resource, req, res);
            }
        } else {
            // scaling is disabled
            super.load(cms, resource, req, res);
        }
    }

    /**
     * Loads an image cache file to the response.<p>
     *
     * @param resource the original resource
     * @param file the image cache file
     * @param req the current request
     * @param res the current response
     *
     * @throws IOException in case writing fails
     */
    public void loadCacheFile(CmsResource resource, File file, HttpServletRequest req, HttpServletResponse res)
    throws IOException {

        res.setStatus(HttpServletResponse.SC_OK);
        res.setContentLength((int)file.length());
        setDateHeaders(resource, req, res);
        try (FileInputStream in = new FileInputStream(file)) {
            CmsFileUtil.copy(in, res.getOutputStream());
        }
    }

    /**
     * @see org.opencms.loader.I_CmsStaticExportDirectResponseLoader#tryExportDirectResponse(org.opencms.file.CmsObject, org.opencms.file.CmsResource, javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
     */
    public boolean tryExportDirectResponse(
        CmsObject cms,
        CmsResource resource,
        HttpServletRequest req,
        HttpServletResponse res)
    throws IOException, CmsException {

        ensureImageCacheConfiguration();
        if ((m_imageCache == null) || !m_enabled || (req == null)) {
            return false;
        }
        if (!cms.hasPermissions(resource, CmsPermissionSet.ACCESS_READ)) {
            return false;
        }
        CmsImageScaler scaler = new CmsImageScaler(req, m_maxScaleSize, m_maxBlurSize);
        if (!scaler.isValid()) {
            return false;
        }
        String key = ensureImageCacheEntry(cms, resource, scaler);
        if (res != null) {
            loadImageCacheEntry(resource, key, req, res);
        }
        return true;
    }

    /**
     * Creates a configured image cache instance.<p>
     *
     * @param className the image cache class name
     * @param parameters the image cache parameters
     * @return the image cache instance
     * @throws Exception if the instance can not be created
     */
    protected I_CmsImageCache createConfiguredImageCache(String className, CmsParameterConfiguration parameters)
    throws Exception {

        Class<?> clazz = Class.forName(className);
        Object instance = clazz.newInstance();
        if (!(instance instanceof I_CmsImageCache)) {
            throw new CmsConfigurationException(Messages.get().container(Messages.ERR_IMAGE_CACHE_INIT_1, className));
        }
        if (instance instanceof I_CmsConfigurationParameterHandler) {
            I_CmsConfigurationParameterHandler configurable = (I_CmsConfigurationParameterHandler)instance;
            if (CmsRfsImageCache.class.getName().equals(className)
                && CmsStringUtil.isEmptyOrWhitespaceOnly(parameters.get(CmsRfsImageCache.PARAM_FOLDER))) {
                String folder = m_imageRepositoryFolder;
                if (CmsStringUtil.isEmptyOrWhitespaceOnly(folder)) {
                    folder = IMAGE_REPOSITORY_DEFAULT;
                }
                parameters.add(CmsRfsImageCache.PARAM_FOLDER, folder);
            }
            for (String key : parameters.keySet()) {
                configurable.addConfigurationParameter(key, parameters.get(key));
            }
            configurable.initConfiguration();
        }
        return (I_CmsImageCache)instance;
    }

    /**
     * Applies the configured image cache once the full OpenCms configuration is available.<p>
     *
     * @throws CmsConfigurationException if the configured image cache is invalid
     */
    protected synchronized void ensureImageCacheConfiguration() throws CmsConfigurationException {

        if (m_imageCacheConfigurationApplied || (m_imageCache != null)) {
            return;
        }
        if (OpenCms.getStaticExportManager() == null) {
            initDefaultImageCache();
            return;
        }
        initImageCache();
        m_imageCacheConfigurationApplied = true;
    }

    /**
     * Ensures that an image cache entry exists in the configured image cache.<p>
     *
     * @param cms the CMS context
     * @param resource the image resource
     * @param scaler the image scaler
     *
     * @return the image cache key
     *
     * @throws IOException in case scaling fails
     * @throws CmsException in case VFS access fails
     */
    protected String ensureImageCacheEntry(CmsObject cms, CmsResource resource, CmsImageScaler scaler)
    throws IOException, CmsException {

        String key = getImageCacheKey(resource, scaler);
        try {
            if (!m_imageCache.exists(key)) {
                if (scaler.getType() == 8) {
                    // only need the focal point for mode 8
                    scaler.setFocalPoint(CmsPreviewService.readFocalPoint(cms, resource));
                }
                CmsFile file = cms.readFile(resource);
                ByteArrayOutputStream out = new ByteArrayOutputStream();
                scaler.scaleImageTo(file.getContents(), null, resource.getRootPath(), out);
                m_imageCache.write(key, out.toByteArray());
            }
            return key;
        } catch (IOException | CmsException e) {
            throw e;
        } catch (Exception e) {
            throw new CmsException(
                Messages.get().container(Messages.ERR_STORED_CONTENT_DELIVERY_2, "image-cache", key),
                e);
        }
    }

    /**
     * Returns the image cache key for an image/scaler pair.<p>
     *
     * @param resource the image resource
     * @param scaler the image scaler
     *
     * @return the image cache key
     */
    protected String getImageCacheKey(CmsResource resource, CmsImageScaler scaler) {

        String cacheName = m_vfsDiskCache.getCacheName(resource, scaler.toString());
        String repositoryPath = m_vfsDiskCache.getRepositoryPath();
        if (cacheName.startsWith(repositoryPath)) {
            return cacheName.substring(repositoryPath.length());
        }
        return cacheName;
    }

    /**
     * Returns a scaled version of the given OpenCms VFS image resource.<p>
     *
     * All results are cached in disk.
     * If the scaled version does not exist in the cache, it is created.
     * Unscaled versions of the images are also stored in the cache.<p>
     *
     * @param cms the current users OpenCms context
     * @param resource the base VFS resource for the image
     * @param scaler the configured image scaler
     *
     * @return a scaled version of the given OpenCms VFS image resource
     *
     * @throws IOException in case of errors accessing the disk based cache
     * @throws CmsException in case of errors accessing the OpenCms VFS
     */
    protected CmsFile getScaledImage(CmsObject cms, CmsResource resource, CmsImageScaler scaler)
    throws IOException, CmsException {

        File cacheFile = getScaledImageFile(cms, resource, scaler);
        CmsFile file;
        if (resource instanceof CmsFile) {
            // the original file content must be modified (required e.g. for static export)
            file = (CmsFile)resource;
        } else {
            // this is no file, but we don't want to use "upgrade" since we don't need to read the content from the VFS
            file = new CmsFile(resource);
        }
        file.setContents(org.opencms.util.CmsFileUtil.readFile(cacheFile));
        return file;
    }

    /**
     * Returns the RFS cache file for a scaled version of the given OpenCms VFS image resource.<p>
     *
     * @param cms the current users OpenCms context
     * @param resource the base VFS resource for the image
     * @param scaler the configured image scaler
     *
     * @return the scaled image cache file
     *
     * @throws IOException in case of errors accessing the disk based cache
     * @throws CmsException in case of errors accessing the OpenCms VFS
     */
    protected File getScaledImageFile(CmsObject cms, CmsResource resource, CmsImageScaler scaler)
    throws IOException, CmsException {

        String cacheParam = scaler.isValid() ? scaler.toString() : null;
        String cacheName = m_vfsDiskCache.getCacheName(resource, cacheParam);
        File cacheFile = m_vfsDiskCache.getCacheFile(cacheName);
        if (cacheFile == null) {
            try (FileOutputStream out = new FileOutputStream(m_vfsDiskCache.createCacheFile(cacheName))) {
                if (scaler.isValid()) {
                    if (scaler.getType() == 8) {
                        // only need the focal point for mode 8
                        scaler.setFocalPoint(CmsPreviewService.readFocalPoint(cms, resource));
                    }
                    CmsFile file = cms.readFile(resource);
                    scaler.scaleImageTo(file.getContents(), null, resource.getRootPath(), out);
                } else {
                    cms.readFileContentTo(resource, out);
                }
            }
            cacheFile = m_vfsDiskCache.getCacheFile(cacheName);
        }
        return cacheFile;
    }

    /**
     * Initializes the classic RFS image cache used when direct image cache delivery is disabled.<p>
     */
    protected void initDefaultImageCache() {

        if (CmsStringUtil.isEmpty(m_imageRepositoryFolder)) {
            m_imageRepositoryFolder = IMAGE_REPOSITORY_DEFAULT;
        }
        m_imageCache = null;
        m_vfsDiskCache = new CmsVfsNameBasedDiskCache(
            OpenCms.getSystemInfo().getWebApplicationRfsPath(),
            m_imageRepositoryFolder);
    }

    /**
     * Initializes the optional image cache.<p>
     */
    protected void initImageCache() throws CmsConfigurationException {

        CmsStaticExportManager staticExportManager = OpenCms.getStaticExportManager();
        if (staticExportManager == null) {
            initDefaultImageCache();
            return;
        }
        CmsImageCacheConfiguration imageCacheConfiguration = staticExportManager.getImageCacheConfiguration();
        if ((imageCacheConfiguration == null) || !imageCacheConfiguration.isConfigured()) {
            initDefaultImageCache();
            return;
        }
        String className = imageCacheConfiguration.getClassName();
        if (CmsStringUtil.isEmptyOrWhitespaceOnly(className)) {
            throw new CmsConfigurationException(Messages.get().container(Messages.ERR_IMAGE_CACHE_INIT_1, "class"));
        }
        CmsParameterConfiguration propertyConfiguration;
        try {
            propertyConfiguration = new CmsParameterConfiguration(
                OpenCms.getSystemInfo().getConfigurationFileRfsPath());
        } catch (Exception e) {
            m_imageCache = null;
            throw new CmsConfigurationException(
                Messages.get().container(Messages.ERR_IMAGE_CACHE_INIT_1, "opencms.properties"),
                e);
        }
        String activeType = CmsStorageManager.getActiveStorageType(propertyConfiguration);
        validateImageCacheConfiguration(activeType, className);
        CmsParameterConfiguration parameters = new CmsParameterConfiguration(
            imageCacheConfiguration.getConfiguration());
        try {
            I_CmsImageCache imageCache = createConfiguredImageCache(className, parameters);
            if (imageCache instanceof CmsS3ImageCache) {
                m_vfsDiskCache = new CmsVfsNameBasedDiskCache("", "");
            } else if (imageCache instanceof CmsFsImageCache) {
                String path = parameters.get(CmsFsImageCache.PARAM_PATH);
                m_vfsDiskCache = new CmsVfsNameBasedDiskCache("", path);
            } else if (imageCache instanceof CmsRfsImageCache) {
                String folder = parameters.get(CmsRfsImageCache.PARAM_FOLDER);
                if (CmsStringUtil.isEmptyOrWhitespaceOnly(folder)) {
                    folder = m_imageRepositoryFolder;
                }
                if (CmsStringUtil.isEmptyOrWhitespaceOnly(folder)) {
                    folder = IMAGE_REPOSITORY_DEFAULT;
                }
                m_vfsDiskCache = new CmsVfsNameBasedDiskCache(
                    OpenCms.getSystemInfo().getWebApplicationRfsPath(),
                    folder);
            } else {
                m_vfsDiskCache = new CmsVfsNameBasedDiskCache("", "");
            }
            m_imageCache = imageCache;
        } catch (CmsConfigurationException e) {
            m_imageCache = null;
            throw e;
        } catch (Exception e) {
            m_imageCache = null;
            throw new CmsConfigurationException(
                Messages.get().container(Messages.ERR_IMAGE_CACHE_INIT_1, className),
                e);
        }
    }

    /**
     * Loads an image cache entry from the configured storage to the response.<p>
     *
     * @param resource the original resource
     * @param key the image cache key
     * @param req the request
     * @param res the response
     *
     * @throws IOException in case writing fails
     * @throws CmsException in case store access fails
     */
    protected void loadImageCacheEntry(
        CmsResource resource,
        String key,
        HttpServletRequest req,
        HttpServletResponse res)
    throws IOException, CmsException {

        try {
            long length = m_imageCache.getLength(key);
            String etag = getImageCacheETag(key);
            if (m_imageCache.supportsRangeDelivery()) {
                res.setHeader(CmsRequestUtil.HEADER_ACCEPT_RANGES, CmsByteRange.RANGE_UNIT_BYTES);
            }
            String rangeHeader = req.getHeader(CmsRequestUtil.HEADER_RANGE);
            boolean rangeRequest = (rangeHeader != null)
                && m_imageCache.supportsRangeDelivery()
                && matchesImageCacheIfRange(req, resource, etag);
            if (!rangeRequest && isImageCacheNotModified(resource, req, res, etag)) {
                return;
            }
            if (rangeRequest) {
                CmsByteRange range = CmsByteRange.parse(rangeHeader, length);
                if (range.isInvalid()) {
                    res.setStatus(HttpServletResponse.SC_REQUESTED_RANGE_NOT_SATISFIABLE);
                    res.setHeader(CmsRequestUtil.HEADER_CONTENT_RANGE, CmsByteRange.RANGE_UNIT_BYTES + " */" + length);
                    res.setHeader(CmsRequestUtil.HEADER_ETAG, etag);
                    return;
                }
                if (!range.isIgnored()) {
                    res.setStatus(HttpServletResponse.SC_PARTIAL_CONTENT);
                    setContentLength(res, range.m_length);
                    res.setHeader(
                        CmsRequestUtil.HEADER_CONTENT_RANGE,
                        CmsByteRange.RANGE_UNIT_BYTES
                            + " "
                            + range.m_start
                            + "-"
                            + (range.m_start + range.m_length - 1)
                            + "/"
                            + length);
                    setImageCacheHeaders(resource, req, res, etag);
                    if (!"HEAD".equalsIgnoreCase(req.getMethod())) {
                        m_imageCache.writeRangeTo(key, range.m_start, range.m_length, res.getOutputStream());
                    }
                    return;
                }
            }
            res.setStatus(HttpServletResponse.SC_OK);
            setContentLength(res, length);
            setImageCacheHeaders(resource, req, res, etag);
            if (!"HEAD".equalsIgnoreCase(req.getMethod())) {
                writeImageCacheEntryTo(key, res.getOutputStream());
            }
        } catch (IOException | CmsException e) {
            throw e;
        } catch (Exception e) {
            throw new CmsException(
                Messages.get().container(Messages.ERR_STORED_CONTENT_DELIVERY_2, "image-cache", key),
                e);
        }
    }

    /**
     * Validates the image cache configuration for the active storage type.<p>
     *
     * @param activeType the active storage backend type
     * @param className the configured image cache class name
     * @throws CmsConfigurationException if the image cache configuration is invalid
     */
    protected void validateImageCacheConfiguration(String activeType, String className)
    throws CmsConfigurationException {

        if (CmsS3ImageCache.class.getName().equals(className) && !CmsS3Storage.STORAGE_TYPE.equals(activeType)) {
            throw new CmsConfigurationException(
                Messages.get().container(Messages.ERR_IMAGE_CACHE_CONFIG_TYPE_1, activeType));
        }
        if (CmsFsImageCache.class.getName().equals(className) && !CmsFsStorage.STORAGE_TYPE.equals(activeType)) {
            throw new CmsConfigurationException(
                Messages.get().container(Messages.ERR_IMAGE_CACHE_CONFIG_TYPE_1, activeType));
        }
    }

    /**
     * Returns the ETag for an image cache entry.<p>
     *
     * @param key the image cache key
     * @return the ETag
     */
    private String getImageCacheETag(String key) {

        return "\"" + DigestUtils.sha256Hex(key) + "\"";
    }

    /**
     * Returns if the given value is an entity tag.<p>
     *
     * @param value the value
     * @return <code>true</code> if the value is an entity tag
     */
    private boolean isEntityTag(String value) {

        String tag = value.trim();
        return tag.startsWith("\"") || tag.startsWith(WEAK_ETAG_PREFIX + "\"");
    }

    /**
     * Returns if the image cache entry was not modified since the client request.<p>
     *
     * @param resource the resource
     * @param req the request
     * @param res the response
     * @param etag the image cache entry ETag
     * @return if the image cache entry was not modified
     */
    private boolean isImageCacheNotModified(
        CmsResource resource,
        HttpServletRequest req,
        HttpServletResponse res,
        String etag) {

        boolean hasIfNoneMatch = req.getHeader(CmsRequestUtil.HEADER_IF_NONE_MATCH) != null;
        boolean notModified = hasIfNoneMatch
        ? matchesImageCacheIfNoneMatch(req, etag)
        : org.opencms.flex.CmsFlexController.isNotModifiedSince(req, resource.getDateLastModified());
        if (resource.getState().isUnchanged()
            && !org.opencms.workplace.CmsWorkplaceManager.isWorkplaceUser(req)
            && notModified) {
            long now = System.currentTimeMillis();
            if ((resource.getDateReleased() < now) && (resource.getDateExpired() > now)) {
                setImageCacheHeaders(resource, req, res, etag);
                res.setStatus(HttpServletResponse.SC_NOT_MODIFIED);
                return true;
            }
        }
        return false;
    }

    /**
     * Returns if the If-None-Match header matches the image cache entry ETag.<p>
     *
     * @param req the request
     * @param etag the image cache entry ETag
     * @return if the entity tag matches
     */
    private boolean matchesImageCacheIfNoneMatch(HttpServletRequest req, String etag) {

        String ifNoneMatch = req.getHeader(CmsRequestUtil.HEADER_IF_NONE_MATCH);
        if (ifNoneMatch == null) {
            return false;
        }
        String imageCacheETag = stripWeakPrefix(etag);
        String[] tags = ifNoneMatch.split(",");
        for (int i = 0; i < tags.length; i++) {
            String tag = stripWeakPrefix(tags[i].trim());
            if ("*".equals(tag) || imageCacheETag.equals(tag)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Checks the If-Range header for image cache delivery.<p>
     *
     * @param req the request
     * @param resource the resource
     * @param etag the image cache entry ETag
     * @return if a range request should be processed
     */
    private boolean matchesImageCacheIfRange(HttpServletRequest req, CmsResource resource, String etag) {

        String ifRange = req.getHeader(CmsRequestUtil.HEADER_IF_RANGE);
        if (ifRange == null) {
            return true;
        }
        if (isEntityTag(ifRange)) {
            return etag.equals(ifRange.trim());
        }
        try {
            long ifRangeDate = req.getDateHeader(CmsRequestUtil.HEADER_IF_RANGE);
            return (ifRangeDate >= 0) && ((resource.getDateLastModified() / 1000) * 1000 <= ifRangeDate);
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    /**
     * Sets the content length header.<p>
     *
     * @param res the response
     * @param length the content length
     */
    private void setContentLength(HttpServletResponse res, long length) {

        if (length <= Integer.MAX_VALUE) {
            res.setContentLength((int)length);
        } else {
            res.setHeader(CmsRequestUtil.HEADER_CONTENT_LENGTH, String.valueOf(length));
        }
    }

    /**
     * Sets date and cache headers for a loaded image.<p>
     *
     * @param resource the original resource
     * @param req the current request
     * @param res the current response
     */
    private void setDateHeaders(CmsResource resource, HttpServletRequest req, HttpServletResponse res) {

        if (org.opencms.workplace.CmsWorkplaceManager.isWorkplaceUser(req)) {
            res.setDateHeader(org.opencms.util.CmsRequestUtil.HEADER_LAST_MODIFIED, System.currentTimeMillis());
            org.opencms.util.CmsRequestUtil.setNoCacheHeaders(res);
        } else {
            res.setDateHeader(org.opencms.util.CmsRequestUtil.HEADER_LAST_MODIFIED, resource.getDateLastModified());
            if (!res.containsHeader(org.opencms.util.CmsRequestUtil.HEADER_CACHE_CONTROL)) {
                long expireTime = resource.getDateExpired();
                if (expireTime == CmsResource.DATE_EXPIRED_DEFAULT) {
                    expireTime--;
                }
                org.opencms.flex.CmsFlexController.setDateExpiresHeader(res, expireTime, getClientCacheMaxAge());
            }
        }
    }

    /**
     * Sets HTTP headers for an image cache entry.<p>
     *
     * @param resource the original resource
     * @param req the current request
     * @param res the current response
     * @param etag the image cache entry ETag
     */
    private void setImageCacheHeaders(
        CmsResource resource,
        HttpServletRequest req,
        HttpServletResponse res,
        String etag) {

        res.setHeader(CmsRequestUtil.HEADER_ETAG, etag);
        setDateHeaders(resource, req, res);
    }

    /**
     * Removes a weak entity tag prefix from the given value.<p>
     *
     * @param value the value
     * @return the value without weak prefix
     */
    private String stripWeakPrefix(String value) {

        return value.startsWith(WEAK_ETAG_PREFIX) ? value.substring(WEAK_ETAG_PREFIX.length()) : value;
    }

    /**
     * Writes an image cache entry to an output stream.<p>
     *
     * @param key the image cache key
     * @param out the output stream
     *
     * @throws IOException in case writing fails
     * @throws CmsException in case store access fails
     */
    private void writeImageCacheEntryTo(String key, OutputStream out) throws IOException, CmsException {

        try {
            m_imageCache.writeTo(key, out);
        } catch (IOException | CmsException e) {
            throw e;
        } catch (Exception e) {
            throw new CmsException(
                Messages.get().container(Messages.ERR_STORED_CONTENT_DELIVERY_2, "image-cache", key),
                e);
        }
    }
}
