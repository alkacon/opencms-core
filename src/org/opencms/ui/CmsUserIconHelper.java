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
import org.opencms.file.CmsObject;
import org.opencms.file.CmsUser;
import org.opencms.main.CmsLog;
import org.opencms.main.OpenCms;
import org.opencms.security.CmsRole;
import org.opencms.util.CmsStringUtil;
import org.opencms.workplace.CmsWorkplace;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.IOException;

import org.apache.commons.logging.Log;

/**
 * Generates user ident-icons.<p>
 */
public class CmsUserIconHelper {

    /** The color reserved for admin users. */
    public static final Color ADMIN_COLOR = new Color(0xff, 0xa8, 0x26);

    /** The big icon suffix. */
    public static final String BIG_ICON_SUFFIX = "_big_icon.png";

    /** The target folder name. */
    public static final String ICON_FOLDER = "user_icons";

    /** The small icon suffix. */
    public static final String SMALL_ICON_SUFFIX = "_small_icon.png";

    /** The helper instance. */
    private static CmsUserIconHelper INSTANCE;

    /** Logger instance for this class. */
    private static final Log LOG = CmsLog.getLog(CmsUserIconHelper.class);

    /** The image cache. */
    private CmsVfsNameBasedDiskCache m_cache;

    /** The icon renderer. */
    private IdentIcon m_renderer;

    /**
     * Constructor.<p>
     */
    private CmsUserIconHelper() {
        m_renderer = new IdentIcon();
        m_renderer.setReservedColor(ADMIN_COLOR);

        m_cache = new CmsVfsNameBasedDiskCache(
            OpenCms.getSystemInfo().getWebApplicationRfsPath() + "/" + CmsWorkplace.RFS_PATH_RESOURCES,
            ICON_FOLDER);
    }

    /**
     * Returns the icon helper instance.<p>
     *
     * @return the icon helper instance
     */
    public static CmsUserIconHelper getInstance() {

        if (INSTANCE == null) {
            INSTANCE = new CmsUserIconHelper();
        }
        return INSTANCE;
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
     * Returns the ident-icon path for the given user.<p>
     *
     * @param cms the cms context
     * @param user the user
     * @param big <code>true</code> to retrieve the big icon
     *
     * @return the icon path
     */
    private String getIconPath(CmsObject cms, CmsUser user, boolean big) {

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
