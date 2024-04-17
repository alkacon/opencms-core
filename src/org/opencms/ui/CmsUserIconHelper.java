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

package org.opencms.ui;

import com.alkacon.simapi.IdentIcon;
import com.alkacon.simapi.Simapi;

import org.opencms.cache.CmsVfsNameBasedDiskCache;
import org.opencms.file.CmsFile;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsProject;
import org.opencms.file.CmsProperty;
import org.opencms.file.CmsPropertyDefinition;
import org.opencms.file.CmsResource;
import org.opencms.file.CmsResourceFilter;
import org.opencms.file.CmsUser;
import org.opencms.file.types.CmsResourceTypeImage;
import org.opencms.loader.CmsImageScaler;
import org.opencms.main.CmsException;
import org.opencms.main.CmsLog;
import org.opencms.main.OpenCms;
import org.opencms.security.CmsRole;
import org.opencms.util.CmsStringUtil;
import org.opencms.workplace.CmsWorkplace;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Collections;
import java.util.List;

import org.apache.commons.logging.Log;

/**
 * Generates user ident-icons.<p>
 */
public class CmsUserIconHelper {

    /**
     * Available icon sizes.<p>
     */
    private enum IconSize {

        /**The big icon size. */
        Big(192, BIG_ICON_SUFFIX),
        /**The small icon size.*/
        Small(64, SMALL_ICON_SUFFIX),
        /**The tiny icon size. */
        Tiny(46, TINY_ICON_SUFFIX);

        /**Size in pixel.*/
        private int m_size;

        /**Suffix to append to filename.*/
        private String m_suffix;

        /**
         * constructor.<p>
         *
         * @param size in pixel
         * @param suffix for filename
         */
        private IconSize(int size, String suffix) {

            m_size = size;
            m_suffix = suffix;
        }

        /**
         * Gets size in pixel.<p>
         *
         * @return icon size in pixel
         */
        public int getSize() {

            return m_size;
        }

        /**
         * Gets the suffix.<p>
         *
         * @return string
         */
        public String getSuffix() {

            return m_suffix;
        }

    }

    /** The color reserved for admin users. */
    public static final Color ADMIN_COLOR = new Color(0x00, 0x30, 0x82);

    /** The big icon suffix. */
    public static final String BIG_ICON_SUFFIX = "_big_icon.png";

    /** The target folder name. */
    public static final String ICON_FOLDER = "user_icons";

    /** The small icon suffix. */
    public static final String SMALL_ICON_SUFFIX = "_small_icon.png";

    /** The temp folder name. */
    public static final String TEMP_FOLDER = "temp/";

    /** The tiny icon suffix. */
    public static final String TINY_ICON_SUFFIX = "_tiny_icon.png";

    /** The user image folder. */
    public static final String USER_IMAGE_FOLDER = "/system/userimages/";

    /** The user image additional info key. */
    public static final String USER_IMAGE_INFO = "USER_IMAGE";

    /** Logger instance for this class. */
    private static final Log LOG = CmsLog.getLog(CmsUserIconHelper.class);

    /** The admin cms context. */
    private CmsObject m_adminCms;

    /** The image cache. */
    private CmsVfsNameBasedDiskCache m_cache;

    /** The icon renderer. */
    private IdentIcon m_renderer;

    /**
     * Constructor.<p>
     *
     * @param adminCms the admin cms context
     */
    public CmsUserIconHelper(CmsObject adminCms) {

        m_adminCms = adminCms;
        m_renderer = new IdentIcon();
        m_renderer.setReservedColor(ADMIN_COLOR);

        m_cache = new CmsVfsNameBasedDiskCache(
            OpenCms.getSystemInfo().getWebApplicationRfsPath() + "/" + CmsWorkplace.RFS_PATH_RESOURCES,
            ICON_FOLDER);
    }

    /**
     * Checks whether the given user has an individual user image.<p>
     *
     * @param user the user
     *
     * @return <code>true</code> if the given user has an individual user image
     */
    public static boolean hasUserImage(CmsUser user) {

        return CmsStringUtil.isNotEmptyOrWhitespaceOnly((String)user.getAdditionalInfo(USER_IMAGE_INFO));
    }

    /**
     * Deletes the user image of the current user.<p>
     *
     * @param cms the cms context
     */
    public void deleteUserImage(CmsObject cms) {

        CmsUser user = cms.getRequestContext().getCurrentUser();
        String userIconPath = (String)user.getAdditionalInfo(USER_IMAGE_INFO);
        if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(userIconPath)) {
            try {
                CmsObject adminCms = OpenCms.initCmsObject(m_adminCms);
                if (adminCms.existsResource(userIconPath)) {

                    CmsProject tempProject = adminCms.createTempfileProject();
                    adminCms.getRequestContext().setCurrentProject(tempProject);
                    adminCms.lockResource(userIconPath);
                    adminCms.deleteResource(userIconPath, CmsResource.DELETE_REMOVE_SIBLINGS);
                }
                user.deleteAdditionalInfo(CmsUserIconHelper.USER_IMAGE_INFO);
                adminCms.writeUser(user);

                try {
                    OpenCms.getPublishManager().publishProject(adminCms);
                } catch (Exception e) {
                    LOG.error("Error publishing user image resources.", e);
                }
            } catch (CmsException e) {
                LOG.error("Error deleting previous user image.", e);
            }
        }
    }

    /**
     * Returns the big ident-icon path for the given user.<p>
     *
     * @param cms the cms context
     * @param user the user
     *
     * @return the icon path
     */
    public String getBigIconPath(CmsObject cms, CmsUser user) {

        return getIconPath(cms, user, IconSize.Big);
    }

    /**
     * Returns the small ident-icon path for the given user.<p>
     *
     * @param cms the cms context
     * @param user the user
     *
     * @return the icon path
     */
    public String getSmallIconPath(CmsObject cms, CmsUser user) {

        return getIconPath(cms, user, IconSize.Small);
    }

    /**
     * Returns the tiny ident-icon path for the given user.<p>
     *
     * @param cms the cms context
     * @param user the user
     *
     * @return the icon path
     */
    public String getTinyIconPath(CmsObject cms, CmsUser user) {

        return getIconPath(cms, user, IconSize.Tiny);
    }

    /**
     * Handles a user image upload.
     * The uploaded file will be scaled and save as a new file beneath /system/userimages/, the original file will be deleted.<p>
     *
     * @param cms the cms context
     * @param user the user
     * @param uploadedFile the uploaded file
     *
     * @return <code>true</code> in case the image was set successfully
     */
    public boolean handleImageUpload(CmsObject cms, CmsUser user, String uploadedFile) {

        boolean result = false;
        try {
            setUserImage(cms, user, uploadedFile);
            result = true;
        } catch (CmsException e) {
            LOG.error("Error setting user image.", e);
        }
        try {
            cms.lockResource(uploadedFile);
            cms.deleteResource(uploadedFile, CmsResource.DELETE_REMOVE_SIBLINGS);
        } catch (CmsException e) {
            LOG.error("Error deleting user image temp file.", e);
        }
        return result;
    }

    /**
     * Handles a user image upload.
     * The uploaded file will be scaled and save as a new file beneath /system/userimages/, the original file will be deleted.<p>
     *
     * @param cms the cms context
     * @param uploadedFiles the uploaded file paths
     *
     * @return <code>true</code> in case the image was set successfully
     */
    public boolean handleImageUpload(CmsObject cms, List<String> uploadedFiles) {

        boolean result = false;
        if (uploadedFiles.size() == 1) {
            String tempFile = CmsStringUtil.joinPaths(USER_IMAGE_FOLDER, TEMP_FOLDER, uploadedFiles.get(0));
            result = handleImageUpload(cms, cms.getRequestContext().getCurrentUser(), tempFile);
        }
        return result;
    }

    /**
     * Sets the user image for the given user.<p>
     *
     * @param cms the cms context
     * @param user the user
     * @param rootPath the image root path
     *
     * @throws CmsException in case anything goes wrong
     */
    public void setUserImage(CmsObject cms, CmsUser user, String rootPath) throws CmsException {

        CmsFile tempFile = cms.readFile(cms.getRequestContext().removeSiteRoot(rootPath));
        CmsImageScaler scaler = new CmsImageScaler(tempFile.getContents(), tempFile.getRootPath());

        if (scaler.isValid()) {
            scaler.setType(2);
            scaler.setHeight(192);
            scaler.setWidth(192);
            byte[] content = scaler.scaleImage(tempFile);
            String previousImage = (String)user.getAdditionalInfo(CmsUserIconHelper.USER_IMAGE_INFO);
            String newFileName = USER_IMAGE_FOLDER
                + user.getId().toString()
                + "_"
                + System.currentTimeMillis()
                + getSuffix(tempFile.getName());
            CmsObject adminCms = OpenCms.initCmsObject(m_adminCms);
            CmsProject tempProject = adminCms.createTempfileProject();
            adminCms.getRequestContext().setCurrentProject(tempProject);
            if (adminCms.existsResource(newFileName)) {
                // a user image of the given name already exists, just write the new content
                CmsFile imageFile = adminCms.readFile(newFileName);
                adminCms.lockResource(imageFile);
                imageFile.setContents(content);
                adminCms.writeFile(imageFile);
                adminCms.writePropertyObject(
                    newFileName,
                    new CmsProperty(CmsPropertyDefinition.PROPERTY_IMAGE_SIZE, null, "w:192,h:192"));

            } else {
                // create a new user image file
                adminCms.createResource(
                    newFileName,
                    OpenCms.getResourceManager().getResourceType(CmsResourceTypeImage.getStaticTypeName()),
                    content,
                    Collections.singletonList(
                        new CmsProperty(CmsPropertyDefinition.PROPERTY_IMAGE_SIZE, null, "w:192,h:192")));
            }
            if (newFileName.equals(previousImage)) {
                previousImage = null;
            }

            if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(previousImage)) {
                previousImage = (String)user.getAdditionalInfo(CmsUserIconHelper.USER_IMAGE_INFO);
                if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(previousImage)
                    && cms.existsResource(newFileName, CmsResourceFilter.ONLY_VISIBLE_NO_DELETED)) {
                    try {
                        adminCms.lockResource(previousImage);
                        adminCms.deleteResource(previousImage, CmsResource.DELETE_REMOVE_SIBLINGS);
                    } catch (CmsException e) {
                        LOG.error("Error deleting previous user image.", e);
                    }
                }
            }
            user.setAdditionalInfo(CmsUserIconHelper.USER_IMAGE_INFO, newFileName);
            adminCms.writeUser(user);

            try {
                OpenCms.getPublishManager().publishProject(adminCms);
            } catch (Exception e) {
                LOG.error("Error publishing user image resources.", e);
            }

        }
    }

    /**
     * Returns the ident-icon path for the given user.<p>
     *
     * @param cms the cms context
     * @param user the user
     * @param size IconSize to get icon for
     *
     * @return the icon path
     */
    private String getIconPath(CmsObject cms, CmsUser user, IconSize size) {

        String userIconPath = (String)user.getAdditionalInfo(USER_IMAGE_INFO);
        if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(userIconPath)) {
            userIconPath += size.equals(IconSize.Big)
            ? ""
            : "?__scale=h:" + size.getSize() + ",w:" + size.getSize() + ",t:2";
            return OpenCms.getLinkManager().substituteLinkForRootPath(cms, userIconPath);
        }

        boolean isAdmin = OpenCms.getRoleManager().hasRole(cms, user.getName(), CmsRole.ADMINISTRATOR);
        String name = user.getName() + Boolean.toString(isAdmin);
        String rfsName = toRfsName(name, size);
        String path = toPath(name, size);
        if (!m_cache.hasCacheContent(rfsName)) {

            BufferedImage icon = m_renderer.render(name, isAdmin, size.getSize());
            try {
                m_cache.saveCacheFile(rfsName, getImageBytes(icon));
            } catch (Exception e) {
                LOG.error(e.getLocalizedMessage(), e);
            }
        }
        return path;
    }

    /**
     * Returns the image data.
     * @param image the image
     *
     * @return the data
     *
     * @throws IOException in case writing to the output stream failed
     */
    private byte[] getImageBytes(BufferedImage image) throws IOException {

        return Simapi.getImageBytes(image, Simapi.TYPE_PNG);
    }

    /**
     * Returns the file suffix.<p>
     *
     * @param fileName the file name
     *
     * @return the suffix
     */
    private String getSuffix(String fileName) {

        int index = fileName.lastIndexOf(".");
        if (index > 0) {
            return fileName.substring(index);
        } else {
            return fileName;
        }
    }

    /**
     * Transforms user name and icon size into the image path.
     *
     * @param name the user name
     * @param size IconSize to get icon for
     *
     * @return the path
     */
    private String toPath(String name, IconSize size) {

        return CmsStringUtil.joinPaths(CmsWorkplace.getSkinUri(), ICON_FOLDER, "" + name.hashCode()) + size.getSuffix();
    }

    /**
     * Transforms user name and icon size into the rfs image path.
     *
     * @param name the user name
     * @param size IconSize to get icon for
     *
     * @return the path
     */
    private String toRfsName(String name, IconSize size) {

        return CmsStringUtil.joinPaths(m_cache.getRepositoryPath(), "" + name.hashCode()) + size.getSuffix();
    }
}
