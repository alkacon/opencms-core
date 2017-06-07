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

package org.opencms.loader;

import org.opencms.cache.CmsVfsNameBasedDiskCache;
import org.opencms.configuration.CmsParameterConfiguration;
import org.opencms.file.CmsFile;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsResource;
import org.opencms.main.CmsEvent;
import org.opencms.main.CmsException;
import org.opencms.main.CmsLog;
import org.opencms.main.I_CmsEventListener;
import org.opencms.main.OpenCms;
import org.opencms.scheduler.jobs.CmsImageCacheCleanupJob;
import org.opencms.util.CmsStringUtil;

import java.io.IOException;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

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
public class CmsImageLoader extends CmsDumpLoader implements I_CmsEventListener {

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
        m_imageRepositoryFolder = null;
        m_vfsDiskCache = null;
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
        result.put(CONFIGURATION_SCALING_ENABLED, String.valueOf(m_enabled));
        result.put(CONFIGURATION_IMAGE_FOLDER, m_imageRepositoryFolder);
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
     * @see org.opencms.configuration.I_CmsConfigurationParameterHandler#initConfiguration()
     */
    @Override
    public void initConfiguration() {

        if (CmsStringUtil.isEmpty(m_imageRepositoryFolder)) {
            m_imageRepositoryFolder = IMAGE_REPOSITORY_DEFAULT;
        }
        // initialize the image cache
        if (m_vfsDiskCache == null) {
            m_vfsDiskCache = new CmsVfsNameBasedDiskCache(
                OpenCms.getSystemInfo().getWebApplicationRfsPath(),
                m_imageRepositoryFolder);
        }
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
     * @see org.opencms.loader.I_CmsResourceLoader#load(org.opencms.file.CmsObject, org.opencms.file.CmsResource, javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
     */
    @Override
    public void load(CmsObject cms, CmsResource resource, HttpServletRequest req, HttpServletResponse res)
    throws IOException, CmsException {

        if (m_enabled) {
            if (canSendLastModifiedHeader(resource, req, res)) {
                // no image processing required at all
                return;
            }
            // get the scale information from the request
            CmsImageScaler scaler = new CmsImageScaler(req, m_maxScaleSize, m_maxBlurSize);
            // load the file from the cache
            CmsFile file = getScaledImage(cms, resource, scaler);
            // now perform standard load operation inherited from dump loader
            super.load(cms, file, req, res);
        } else {
            // scaling is disabled
            super.load(cms, resource, req, res);
        }
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

        String cacheParam = scaler.isValid() ? scaler.toString() : null;
        String cacheName = m_vfsDiskCache.getCacheName(resource, cacheParam);
        byte[] content = m_vfsDiskCache.getCacheContent(cacheName);

        CmsFile file;
        if (content != null) {
            if (resource instanceof CmsFile) {
                // the original file content must be modified (required e.g. for static export)
                file = (CmsFile)resource;
            } else {
                // this is no file, but we don't want to use "upgrade" since we don't need to read the content from the VFS
                file = new CmsFile(resource);
            }
            // save the content in the file
            file.setContents(content);
        } else {
            // we must read the content from the VFS (if this has not been done yet)
            file = cms.readFile(resource);
            // upgrade the file (load the content)
            if (scaler.isValid()) {
                // valid scaling parameters found, scale the content
                content = scaler.scaleImage(file);
                // exchange the content of the file with the scaled version
                file.setContents(content);
            }
            // save the file content in the cache
            m_vfsDiskCache.saveCacheFile(cacheName, file.getContents());
        }
        return file;
    }
}