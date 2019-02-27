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

package org.opencms.ui.favorites;

import org.opencms.file.CmsObject;
import org.opencms.file.CmsResourceFilter;
import org.opencms.file.CmsUser;
import org.opencms.json.JSONArray;
import org.opencms.json.JSONException;
import org.opencms.json.JSONObject;
import org.opencms.main.CmsException;
import org.opencms.main.CmsLog;
import org.opencms.main.OpenCms;
import org.opencms.util.CmsStringUtil;
import org.opencms.util.CmsUUID;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.logging.Log;

/**
 * Loads/saves favorites.
 *
 * <p>When loading favorites, the individual entries are validated by trying to read the resources/projects they reference.
 * If this fails, the entries are discareded from the returned list.
 */
public class CmsFavoriteDAO {

    /** The additional info key under which the favorites for a user are saved. */
    public static final String ADDINFO_KEY = "favLocations";

    /** The main key in the JSON object containing the favorites. */
    public static final String BASE_KEY = "f";

    /** The logger instance for this class. */
    private static final Log LOG = CmsLog.getLog(CmsFavoriteDAO.class);

    /** The CMS context. */
    private CmsObject m_cms;

    /** Projects which have been validated. */
    private Set<CmsUUID> m_okProjects = new HashSet<>();

    /** Site roots which have been validated. */
    private Set<String> m_okSiteRoots = new HashSet<>();

    /** Structure ids which have been validated. */
    private Set<CmsUUID> m_okStructureIds = new HashSet<>();

    /** The root CMS context. */
    private CmsObject m_rootCms;

    /** Name of user from which bookmarks should be loaded. */
    private String m_userName;

    /**
     * Creates a new instance.
     *
     * @param cms the CMS Context
     * @throws CmsException if something goes wrong
     */
    public CmsFavoriteDAO(CmsObject cms)
    throws CmsException {

        this(cms, cms.getRequestContext().getCurrentUser().getName());
    }

    /**
     * Creates a new instance.
     *
     * @param cms the CMS Context
     * @param userName the name of the user in whose additional infos the bookmarks are stored
     * @throws CmsException if something goes wrong
     */
    public CmsFavoriteDAO(CmsObject cms, String userName)
    throws CmsException {

        m_cms = cms;
        m_rootCms = OpenCms.initCmsObject(m_cms);
        m_rootCms.getRequestContext().setSiteRoot("");
        m_userName = userName;
    }

    /**
     * Loads the favorite list.
     *
     * @return the list of favorites
     *
     * @throws CmsException if something goes wrong
     */
    public List<CmsFavoriteEntry> loadFavorites() throws CmsException {

        List<CmsFavoriteEntry> result = new ArrayList<>();
        try {
            CmsUser user = readUser();
            String data = (String)user.getAdditionalInfo(ADDINFO_KEY);
            if (CmsStringUtil.isEmptyOrWhitespaceOnly(data)) {
                return new ArrayList<>();
            }
            JSONObject json = new JSONObject(data);
            JSONArray array = json.getJSONArray(BASE_KEY);
            for (int i = 0; i < array.length(); i++) {
                JSONObject fav = array.getJSONObject(i);
                try {
                    CmsFavoriteEntry entry = new CmsFavoriteEntry(fav);
                    if (validate(entry)) {
                        result.add(entry);
                    }
                } catch (Exception e) {
                    LOG.warn(e.getLocalizedMessage(), e);
                }

            }
        } catch (JSONException e) {
            LOG.error(e.getLocalizedMessage(), e);
        }
        return result;
    }

    /**
     * Saves the favorites.
     *
     * @param favorites the list of favorites to save
     * @throws CmsException if something goes wrong
     */
    public void saveFavorites(List<CmsFavoriteEntry> favorites) throws CmsException {

        try {
            JSONObject json = new JSONObject();
            JSONArray array = new JSONArray();
            for (CmsFavoriteEntry entry : favorites) {
                array.put(entry.toJson());
            }
            json.put(BASE_KEY, array);
            String data = json.toString();
            CmsUser user = readUser();
            user.setAdditionalInfo(ADDINFO_KEY, data);
            m_cms.writeUser(user);
        } catch (JSONException e) {
            LOG.error(e.getLocalizedMessage(), e);
        }

    }

    private CmsUser readUser() throws CmsException {

        return m_cms.readUser(m_userName);

    }

    /**
     * Validates a favorite entry.
     *
     * <p>If the favorite entry references a resource or project that can't be read, this will return false.
     *
     * @param entry the favorite entry
     * @return the
     */
    private boolean validate(CmsFavoriteEntry entry) {

        try {
            String siteRoot = entry.getSiteRoot();
            if (!m_okSiteRoots.contains(siteRoot)) {
                m_rootCms.readResource(siteRoot);
                m_okSiteRoots.add(siteRoot);
            }
            CmsUUID project = entry.getProjectId();
            if (!m_okProjects.contains(project)) {
                m_cms.readProject(project);
                m_okProjects.add(project);
            }
            for (CmsUUID id : Arrays.asList(entry.getDetailId(), entry.getStructureId())) {
                if ((id != null) && !m_okStructureIds.contains(id)) {
                    m_cms.readResource(id, CmsResourceFilter.IGNORE_EXPIRATION.addRequireVisible());
                    m_okStructureIds.add(id);
                }
            }
            return true;

        } catch (Exception e) {
            LOG.info("Favorite entry validation failed: " + e.getLocalizedMessage(), e);
            return false;
        }

    }

}
