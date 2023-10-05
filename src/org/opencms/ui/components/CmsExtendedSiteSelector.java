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

package org.opencms.ui.components;

import org.opencms.file.CmsObject;
import org.opencms.file.CmsResource;
import org.opencms.file.CmsResourceFilter;
import org.opencms.file.CmsVfsResourceNotFoundException;
import org.opencms.main.CmsException;
import org.opencms.main.CmsLog;
import org.opencms.main.OpenCms;
import org.opencms.security.CmsPermissionViolationException;
import org.opencms.site.CmsSite;
import org.opencms.ui.CmsVaadinUtils;
import org.opencms.ui.components.CmsExtendedSiteSelector.SiteSelectorOption;
import org.opencms.util.CmsFileUtil;
import org.opencms.util.CmsPath;
import org.opencms.util.CmsStringUtil;
import org.opencms.workplace.explorer.CmsResourceUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.apache.commons.logging.Log;

import com.google.common.base.Objects;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import com.vaadin.data.provider.ListDataProvider;
import com.vaadin.ui.ComboBox;

/**
 * Site selector widget which also optionally offers subsite options.
 **/
public class CmsExtendedSiteSelector extends ComboBox<SiteSelectorOption> {

    /**
     * Class representing a single option.
     */
    public static class SiteSelectorOption {

        /** The label for the option. */
        private String m_label;

        /** The path in the site (may be null). */
        private String m_path;

        /** The site root. */
        private String m_site;

        /**
         * Creates a new instance.
         *
         * @param site the site root
         * @param path the path in the site (may be null)
         * @param label the option label
         */
        public SiteSelectorOption(String site, String path, String label) {

            m_site = normalizePath(site);
            m_path = normalizePath(path);
            m_label = label;
        }

        /**
         * @see java.lang.Object#equals(java.lang.Object)
         */
        @Override
        public boolean equals(Object obj) {

            if (this == obj) {
                return true;
            }
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            SiteSelectorOption other = (SiteSelectorOption)obj;
            return Objects.equal(m_site, other.m_site) && Objects.equal(m_path, other.m_path);
        }

        /**
         * Gets the option label.
         *
         * @return the option label
         */
        public String getLabel() {

            return m_label;
        }

        /**
         * Gets the path to jump to as a site path (may be null).
         *
         * @return the path to jump to
         */
        public String getPath() {

            return m_path;
        }

        /**
         * Gets the site root
         *
         * @return the site root
         */
        public String getSite() {

            return m_site;
        }

        /**
         * @see java.lang.Object#hashCode()
         */
        @Override
        public int hashCode() {

            final int prime = 31;
            int result = 1;
            result = (prime * result) + ((m_path == null) ? 0 : m_path.hashCode());
            result = (prime * result) + ((m_site == null) ? 0 : m_site.hashCode());
            return result;
        }

        /**
         * Cuts off trailing slashes if the path is not null.
         *
         * @param path a path
         * @return the normalized path
         */
        private String normalizePath(String path) {

            if (path == null) {
                return null;
            }
            return CmsFileUtil.removeTrailingSeparator(path);
        }

    }

    /** Logger instance for this class. */
    private static final Log LOG = CmsLog.getLog(CmsExtendedSiteSelector.class);

    /** Serial version id. */
    private static final long serialVersionUID = 1L;

    /** Longer default page length for site selectors. */
    public static final int LONG_PAGE_LENGTH = 20;

    /** True if the options have been initialized.  */
    private boolean m_initialized;

    /** The options. */
    private List<SiteSelectorOption> m_options = new ArrayList<>();

    /**
     * Creates a new instance.<p>
     *
     * To actually use the widget, the initOptions method has to be called first.
     */
    public CmsExtendedSiteSelector() {

        super();

        setDataProvider(new ListDataProvider<SiteSelectorOption>(m_options));
        setItemCaptionGenerator(item -> item.getLabel());
        setTextInputAllowed(true);
        setEmptySelectionAllowed(false);
        addAttachListener(evt -> {
            if (!m_initialized) {
                throw new RuntimeException(getClass().getName() + " must be initialized with initOptions");
            }

        });
    }

    /**
     * Builds a list of site selector option that also includes subsites with the 'include in site selector' option enabled in their configuration.
     *
     * @param cms the current CMS context
     * @param addSubsites true if subsite options should be added
     * @return the the extended site selecctor options
     */
    public static List<CmsExtendedSiteSelector.SiteSelectorOption> getExplorerSiteSelectorOptions(
        CmsObject cms,
        boolean addSubsites) {

        LinkedHashMap<String, String> siteOptions = CmsVaadinUtils.getAvailableSitesMap(cms);
        List<String> subsites = new ArrayList<>(
            OpenCms.getADEManager().getSubsitesForSiteSelector(
                cms.getRequestContext().getCurrentProject().isOnlineProject()));
        Collections.sort(subsites);
        Multimap<CmsPath, CmsExtendedSiteSelector.SiteSelectorOption> subsitesForSite = ArrayListMultimap.create();
        if (addSubsites) {
            try {
                CmsObject titleCms = OpenCms.initCmsObject(cms);
                titleCms.getRequestContext().setSiteRoot("");
                for (String subsite : subsites) {
                    CmsSite site = OpenCms.getSiteManager().getSiteForRootPath(subsite);
                    if ((site != null) && site.isSubsiteSelectionEnabled()) { // only use subsites that are in an actual site; also, subsite selection must be enabled on the site
                        CmsPath siteRootPath = new CmsPath(site.getSiteRoot());
                        if (!siteRootPath.equals(new CmsPath(subsite))) { // Don't allow the site itself as a subsite
                            Optional<String> remainingPath = CmsStringUtil.removePrefixPath(
                                site.getSiteRoot(),
                                subsite);
                            if (remainingPath.isPresent()) {
                                try {
                                    CmsResource subsiteRes = titleCms.readResource(
                                        subsite,
                                        CmsResourceFilter.ONLY_VISIBLE_NO_DELETED);
                                    CmsResourceUtil resUtil = new CmsResourceUtil(titleCms, subsiteRes);
                                    String title = resUtil.getTitle();
                                    if (CmsStringUtil.isEmptyOrWhitespaceOnly(title)) {
                                        title = subsiteRes.getName();
                                    }
                                    SiteSelectorOption option = new SiteSelectorOption(
                                        site.getSiteRoot(),
                                        remainingPath.get(),
                                        "\u2013 " + title);
                                    subsitesForSite.put(siteRootPath, option);
                                } catch (CmsPermissionViolationException | CmsVfsResourceNotFoundException e) {
                                    LOG.info(e.getLocalizedMessage(), e);
                                } catch (CmsException e) {
                                    LOG.warn(e.getLocalizedMessage(), e);
                                }
                            }
                        }
                    }
                }
            } catch (Exception e) {
                LOG.error(e.getLocalizedMessage(), e);
            }
        }
        List<SiteSelectorOption> result = new ArrayList<>();
        for (Map.Entry<String, String> entry : siteOptions.entrySet()) {
            result.add(new SiteSelectorOption(entry.getKey(), null, entry.getValue()));
            result.addAll(subsitesForSite.get(new CmsPath(entry.getKey())));
        }
        return result;

    }

    /**
     * Gets the option for the specific site root (without any subsite path).
     *
     * <p>If no option for the site is found, returns null.
     *
     * @param siteRoot the site root
     * @return the option for the site root
     */
    public SiteSelectorOption getOptionForSiteRoot(String siteRoot) {

        for (SiteSelectorOption option : m_options) {
            if (Objects.equal(option.getSite(), siteRoot) && (option.getPath() == null)) {
                return option;
            }
        }
        return null;

    }

    /**
     * Initializes the select options.
     *
     * @param cms the CMS context
     * @param addSubsites true if subsites should be shown
     */
    public void initOptions(CmsObject cms, boolean addSubsites) {

        List<SiteSelectorOption> options = CmsExtendedSiteSelector.getExplorerSiteSelectorOptions(cms, addSubsites);
        m_options.clear();
        m_options.addAll(options);
        m_initialized = true;
    }

    /**
     * Selects a specific site.
     *
     * @param siteRoot the site root
     */
    public void selectSite(String siteRoot) {

        SiteSelectorOption option = getOptionForSiteRoot(siteRoot);
        if (option != null) {
            setValue(option);
        }

    }

}
