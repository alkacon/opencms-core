/*
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (C) Alkacon Software (http://www.alkacon.com)
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

package org.opencms.ade.galleries;

import org.opencms.ade.configuration.CmsADEConfigData;
import org.opencms.ade.galleries.shared.CmsSiteSelectorOption;
import org.opencms.ade.galleries.shared.CmsSiteSelectorOption.Type;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsResourceFilter;
import org.opencms.main.CmsException;
import org.opencms.main.CmsLog;
import org.opencms.main.OpenCms;
import org.opencms.site.CmsSite;
import org.opencms.site.CmsSiteManagerImpl;
import org.opencms.util.CmsStringUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.logging.Log;

/**
 * Helper class for building the options for the site selector in the gallery dialog.<p>
 */
public class CmsSiteSelectorOptionBuilder {

    /** The logger instance for this class. */
    private static final Log LOG = CmsLog.getLog(CmsSiteSelectorOptionBuilder.class.getName());

    /**
     * Comparator used for ordering the options in the site selector.<p>
     */
    private static Comparator<CmsSiteSelectorOption> optionOrder = new Comparator<CmsSiteSelectorOption>() {

        public int compare(CmsSiteSelectorOption o1, CmsSiteSelectorOption o2) {

            return getSortKey(o1).compareTo(getSortKey(o2));
        }

        public String getSortKey(CmsSiteSelectorOption o) {

            String prefix = "";
            if (o.isCurrentSite()) {
                prefix = "0_";
            } else if (o.getType() == Type.shared) {
                prefix = "1_";
            } else if (o.getType() == Type.currentSubsite) {
                prefix = "2_";
            } else {
                prefix = "3_";
            }
            return prefix + o.getSiteRoot();
        }
    };

    /** The CMS context used by this object. */
    private CmsObject m_cms;

    /** The option list. */
    private List<CmsSiteSelectorOption> m_options = new ArrayList<CmsSiteSelectorOption>();

    /** The current site root. */
    private String m_siteRoot;

    /** Site roots of sites which have already been added. */
    private Set<String> m_usedSiteRoots = new HashSet<String>();

    /**
     * Creates a new builder instance.<p>
     *
     * @param cms the CMS context to use
     */
    public CmsSiteSelectorOptionBuilder(CmsObject cms) {

        m_cms = cms;
        m_siteRoot = m_cms.getRequestContext().getSiteRoot();
    }

    /**
     * Adds the current subsite.<p>
     *
     * @param referencePath the reference path
     */
    public void addCurrentSubsite(String referencePath) {

        CmsADEConfigData configData = OpenCms.getADEManager().lookupConfiguration(m_cms, referencePath);
        String basePath = configData.getBasePath();
        if (basePath != null) {
            addOption(
                Type.currentSubsite,
                basePath,
                Messages.get().getBundle(OpenCms.getWorkplaceManager().getWorkplaceLocale(m_cms)).key(
                    Messages.GUI_SITESELECTOR_CURRENT_SUBSITE_0));
        }
    }

    /**
     * Adds 'normal' sites.<p>
     *
     * @param includeRoot if true, also adds the root site
     * @param startFolder the users configured start folder
     */
    public void addNormalSites(boolean includeRoot, String startFolder) {

        CmsSiteManagerImpl siteManager = OpenCms.getSiteManager();
        List<CmsSite> sites = siteManager.getAvailableSites(m_cms, true, false, m_cms.getRequestContext().getOuFqn());
        try {
            CmsObject rootCms = OpenCms.initCmsObject(m_cms);
            rootCms.getRequestContext().setSiteRoot("/");
            if (sites.isEmpty()) {
                String siteRoot = m_cms.getRequestContext().getSiteRoot();
                if (!rootCms.existsResource(siteRoot, CmsResourceFilter.ONLY_VISIBLE_NO_DELETED)) {
                    if (startFolder != null) {
                        siteRoot = CmsStringUtil.joinPaths(siteRoot, startFolder);
                    }
                    if ((startFolder == null)
                        || !rootCms.existsResource(siteRoot, CmsResourceFilter.ONLY_VISIBLE_NO_DELETED)) {
                        siteRoot = null;
                    }
                }
                if (siteRoot != null) {
                    Type type = Type.site;
                    String message = siteRoot;
                    addOption(type, siteRoot, message);
                }
            }
            for (CmsSite site : sites) {
                String siteRoot = site.getSiteRoot();
                if (!rootCms.existsResource(siteRoot, CmsResourceFilter.ONLY_VISIBLE_NO_DELETED)) {
                    if (startFolder != null) {
                        siteRoot = CmsStringUtil.joinPaths(siteRoot, startFolder);
                    }
                    if ((startFolder == null)
                        || !rootCms.existsResource(siteRoot, CmsResourceFilter.ONLY_VISIBLE_NO_DELETED)) {
                        siteRoot = null;
                    }
                }
                if (siteRoot != null) {
                    Type type = Type.site;
                    String message = null;
                    String title = site.getTitle();
                    if (!CmsStringUtil.isEmptyOrWhitespaceOnly(title)) {
                        message = title;
                    } else {
                        message = siteRoot;
                    }
                    if (siteRoot.equals("")) {
                        type = Type.root;
                        message = "/";
                        if (!includeRoot) {
                            continue;
                        }
                    }
                    addOption(type, siteRoot, message);
                }
            }
        } catch (CmsException e) {
            LOG.error(e);
        }
    }

    /**
     * Adds the shared folder.<p>
     */
    public void addSharedSite() {

        String shared = OpenCms.getSiteManager().getSharedFolder();
        if ((shared != null) && m_cms.existsResource(shared, CmsResourceFilter.ONLY_VISIBLE_NO_DELETED)) {
            addOption(
                Type.shared,
                shared,
                org.opencms.workplace.Messages.get().getBundle(
                    OpenCms.getWorkplaceManager().getWorkplaceLocale(m_cms)).key(
                        org.opencms.workplace.Messages.GUI_SHARED_TITLE_0));
        }
    }

    /**
     * Gets the site selector options.<p>
     *
     * @return the site selector options
     */
    public List<CmsSiteSelectorOption> getOptions() {

        Collections.sort(m_options, optionOrder);
        return m_options;
    }

    /**
     * Internal helper method for adding an option.<p>
     *
     * @param type the option type
     * @param siteRoot the site root
     * @param message the message for the option
     */
    private void addOption(Type type, String siteRoot, String message) {

        if (m_usedSiteRoots.contains(CmsStringUtil.joinPaths(siteRoot, "/"))) {
            return;
        }
        CmsSiteSelectorOption option = new CmsSiteSelectorOption(type, siteRoot, m_siteRoot.equals(siteRoot), message);
        m_options.add(option);
        m_usedSiteRoots.add(CmsStringUtil.joinPaths(siteRoot, "/"));
    }
}
