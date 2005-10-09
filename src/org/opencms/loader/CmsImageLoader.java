/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/loader/CmsImageLoader.java,v $
 * Date   : $Date: 2005/10/09 07:15:20 $
 * Version: $Revision: 1.1.2.1 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Mananagement System
 *
 * Copyright (C) 2002 - 2005 Alkacon Software (http://www.alkacon.com)
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

import org.opencms.cache.CmsVfsDiskCache;
import org.opencms.file.CmsFile;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsResource;
import org.opencms.main.CmsException;
import org.opencms.main.CmsLog;
import org.opencms.main.OpenCms;
import org.opencms.util.CmsStringUtil;

import java.io.IOException;
import java.util.Map;
import java.util.TreeMap;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;

/**
 * Loader for images from the OpenCms VSF with integrated image scaling and processing capabilites.<p>
 * 
 * To scale or process an image, the parameter <code>{@link org.opencms.loader.CmsImageScaler#PARAM_SCALE}</code>
 * has to be appended to the image URI. The value for the parameter needs to be composed from the <code>SCALE_PARAM</code>
 * options provided by the constants in the <code>{@link org.opencms.file.types.CmsResourceTypeImage}</code> class.<p>
 * 
 * For example, to scale an image to exact 800x600 pixel with center fitting and a background color of grey, 
 * the following parameter String can be used: <code>w:800,h:600,t:0,c:c0c0c0</code>.<p> 
 * 
 * @author  Alexander Kandzior 
 * 
 * @version $Revision: 1.1.2.1 $ 
 * 
 * @since 6.2.0 
 */
public class CmsImageLoader extends CmsDumpLoader {

    /** The configuration parameter for the OpenCms XML configuration to set the image cache respository. */
    public static final String CONFIGURATION_IMAGE_FOLDER = "image.folder";

    /** The configuration parameter for the OpenCms XML configuration to enable the image scaling. */
    public static final String CONFIGURATION_SCALING_ENABLED = "image.scaling.enabled";

    /** Default name for the image cache repository. */
    public static final String IMAGE_REPOSITORY_DEFAULT = "/WEB-INF/imagecache/";

    /** The id of this loader. */
    public static final int RESOURCE_LOADER_ID_IMAGE_LOADER = 2;

    /** The log object for this class. */
    protected static final Log LOG = CmsLog.getLog(CmsImageLoader.class);

    /** Indicates if image scaling is active. */
    private static boolean m_enabled;

    /** The name of the configured image cache repository. */
    private static String m_imageRepositoryFolder;

    /** The disk cache to use for saving scaled image versions. */
    private CmsVfsDiskCache m_vfsDiskCache;

    /**
     * Creates a new image loader.<p>
     */
    public CmsImageLoader() {

        super();
    }

    /**
     * Returns <code>true</code> if the image scaling and processing capablities for the 
     * OpenCms VFS images have been enabled, <code>false</code> if not.<p>
     * 
     * Image scaling is enabled by setting the loader parameter <code>image.scaling.enabled</code>
     * to the value <code>true</code> in the configuration file <code>opencms-vfs.xml</code>.<p>
     * 
     * Enabling image processing in OpenCms may require several additional configuration steps
     * on the server running OpenCms, especially in UNIX systems. Here it is often required to have an X window server
     * configured and accessible so that the required Java ImageIO operations work.
     * Therefore the image scaling capablities in OpenCms are disabled by default.<p>
     * 
     * @return <code>true</code> if the image scaling and processing capablities for the 
     *      OpenCms VFS images have been enabled
     */
    public static boolean isEnabled() {

        return m_enabled;
    }

    /**
     * @see org.opencms.configuration.I_CmsConfigurationParameterHandler#addConfigurationParameter(java.lang.String, java.lang.String)
     */
    public void addConfigurationParameter(String paramName, String paramValue) {

        if (CmsStringUtil.isNotEmpty(paramName) && CmsStringUtil.isNotEmpty(paramValue)) {
            if (CONFIGURATION_SCALING_ENABLED.equals(paramName)) {
                m_enabled = Boolean.valueOf(paramValue).booleanValue();
            }
            if (CONFIGURATION_IMAGE_FOLDER.equals(paramName)) {
                m_imageRepositoryFolder = paramValue.trim();
            }
        }
        super.addConfigurationParameter(paramName, paramValue);
    }

    /**
     * @see org.opencms.loader.I_CmsResourceLoader#destroy()
     */
    public void destroy() {

        m_enabled = false;
        m_imageRepositoryFolder = null;
    }

    /**
     * @see org.opencms.configuration.I_CmsConfigurationParameterHandler#getConfiguration()
     */
    public Map getConfiguration() {

        Map config = super.getConfiguration();
        TreeMap result = new TreeMap();
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
    public int getLoaderId() {

        return RESOURCE_LOADER_ID_IMAGE_LOADER;
    }

    /**
     * @see org.opencms.configuration.I_CmsConfigurationParameterHandler#initConfiguration()
     */
    public void initConfiguration() {

        if (CmsStringUtil.isEmpty(m_imageRepositoryFolder)) {
            m_imageRepositoryFolder = IMAGE_REPOSITORY_DEFAULT;
        }
        // initialize the image cache
        m_vfsDiskCache = new CmsVfsDiskCache(
            OpenCms.getSystemInfo().getWebApplicationRfsPath(),
            m_imageRepositoryFolder);
        // output setup information
        if (CmsLog.INIT.isInfoEnabled()) {
            CmsLog.INIT.info(Messages.get().key(
                Messages.INIT_IMAGE_REPOSITORY_PATH_1,
                m_vfsDiskCache.getRepositoryPath()));
            CmsLog.INIT.info(Messages.get().key(Messages.INIT_IMAGE_SCALING_ENABLED_1, new Boolean(m_enabled)));
        }
    }

    /**
     * @see org.opencms.loader.I_CmsResourceLoader#load(org.opencms.file.CmsObject, org.opencms.file.CmsResource, javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
     */
    public void load(CmsObject cms, CmsResource resource, HttpServletRequest req, HttpServletResponse res)
    throws IOException, CmsException {

        if (m_enabled) {
            // get the scale information from the request
            CmsImageScaler scaler = new CmsImageScaler(req.getParameter(CmsImageScaler.PARAM_SCALE));
            // load the file from the cache
            CmsFile file = getScaledImage(cms, resource, scaler);
            // now perform standarad load operation inherited from dump loader
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
     * @throws IOException in case of errors acessing the disk based cache
     * @throws CmsException in case of errors accessing the OpenCms VFS
     */
    protected CmsFile getScaledImage(CmsObject cms, CmsResource resource, CmsImageScaler scaler)
    throws IOException, CmsException {

        String cacheParam = scaler.isValid() ? scaler.toString() : null;
        String cacheName = m_vfsDiskCache.getCacheName(
            cms.getRequestContext().currentProject().isOnlineProject(),
            resource.getRootPath(),
            cacheParam);
        byte[] content = m_vfsDiskCache.getCacheContent(cacheName, resource.getDateLastModified());

        CmsFile file;
        if (content != null) {
            // save the content in the file
            file = new CmsFile(resource);
            file.setContents(content);
        } else {
            // upgrade the file (load the content)
            file = CmsFile.upgrade(resource, cms);
            if (scaler.isValid()) {
                // valid scaling parameters found, scale the content
                content = scaler.scaleImage(file);
                // exchange the content of the file with the scaled version
                file.setContents(content);
            }
            // save the file content in the cache
            m_vfsDiskCache.saveCacheFile(cacheName, file.getContents(), file.getDateLastModified());
        }
        return file;
    }
}