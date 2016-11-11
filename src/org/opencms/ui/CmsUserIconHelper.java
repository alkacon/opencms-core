/*
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (c) Alkacon Software GmbH (http://www.alkacon.com)
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

import org.apache.commons.logging.Log;

/**
 * Generates user ident-icons.<p>
 */
public class CmsUserIconHelper {

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

        return getIconPath(cms, user, true);
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

        return getIconPath(cms, user, false);
    }

    /**
     * Handles a user image upload.
     * The uploaded file will be scaled and save as a new file beneath /system/userimages/, the original file will be deleted.<p>
     *
     * @param cms the cms context
     * @param user the user
     * @param uploadedFile the uploaded file
     */
    public void handleImageUpload(CmsObject cms, CmsUser user, String uploadedFile) {

        try {
            setUserImage(cms, user, uploadedFile);

        } catch (CmsException e) {
            LOG.error("Error setting user image.", e);
        }
        try {
            cms.lockResource(uploadedFile);
            cms.deleteResource(uploadedFile, CmsResource.DELETE_REMOVE_SIBLINGS);
        } catch (CmsException e) {
            LOG.error("Error deleting user image temp file.", e);
        }
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
     * @param big <code>true</code> to retrieve the big icon
     *
     * @return the icon path
     */
    private String getIconPath(CmsObject cms, CmsUser user, boolean big) {

        String userIconPath = (String)user.getAdditionalInfo(USER_IMAGE_INFO);
        if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(userIconPath)) {
            userIconPath += big ? "" : "?__scale=h:32,w:32,t:2";
            return OpenCms.getLinkManager().substituteLinkForRootPath(cms, userIconPath);
        }

        boolean isAdmin = OpenCms.getRoleManager().hasRole(cms, user.getName(), CmsRole.ADMINISTRATOR);
        String name = user.getName() + Boolean.toString(isAdmin);
        String rfsName = toRfsName(name, big);
        String path = toPath(name, big);
        if (!m_cache.hasCacheContent(rfsName)) {

            BufferedImage icon = m_renderer.render(name, isAdmin, big ? 96 : 32);
            try {
                m_cache.saveCacheFile(rfsName, getImageBytes(icon));
            } catch (Exception e) {
                LOG.error(e.getLocalizedMessage(), e);
            }
        }
        return path;
    }

    /**
     * Returns the image data
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
     * @param big <code>true</code> in case of big icons
     *
     * @return the path
     */
    private String toPath(String name, boolean big) {

        String result = CmsStringUtil.joinPaths(CmsWorkplace.getSkinUri(), ICON_FOLDER, "" + name.hashCode());
        if (big) {
            result += BIG_ICON_SUFFIX;
        } else {
            result += SMALL_ICON_SUFFIX;
        }
        return result;
    }

    /**
     * Transforms user name and icon size into the rfs image path.
     *
     * @param name the user name
     * @param big <code>true</code> in case of big icons
     *
     * @return the path
     */
    private String toRfsName(String name, boolean big) {

        String result = CmsStringUtil.joinPaths(m_cache.getRepositoryPath(), "" + name.hashCode());
        if (big) {
            result += BIG_ICON_SUFFIX;
        } else {
            result += SMALL_ICON_SUFFIX;
        }
        return result;
    }
}
