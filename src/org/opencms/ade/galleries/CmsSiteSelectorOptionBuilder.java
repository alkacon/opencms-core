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

import org.opencms.ade.galleries.shared.CmsSiteSelectorOption;
import org.opencms.ade.galleries.shared.CmsSiteSelectorOption.Type;
import org.opencms.file.CmsObject;
import org.opencms.main.OpenCms;
import org.opencms.site.CmsSite;
import org.opencms.site.CmsSiteManagerImpl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Helper class for building the options for the site selector in the gallery dialog.<p>
 */
public class CmsSiteSelectorOptionBuilder {

    /**
     * Comparator used for ordering the options in the site selector.<p>
     */
    private static Comparator<CmsSiteSelectorOption> optionOrder = new Comparator<CmsSiteSelectorOption>() {

        public int compare(CmsSiteSelectorOption o1, CmsSiteSelectorOption o2) {

            if (o1.isCurrentSite()) {
                return -1;
            } else if (o2.isCurrentSite()) {
                return 1;
            } else if (o1.getType() == Type.shared) {
                return -1;
            } else if (o2.getType() == Type.shared) {
                return 1;
            } else {
                return o1.getSiteRoot().compareTo(o2.getSiteRoot());
            }
        }
    };

    /** The CMS context used by this object. */
    private CmsObject m_cms;

    /** The option list. */
    private List<CmsSiteSelectorOption> m_options = new ArrayList<CmsSiteSelectorOption>();

    /** The current site root. */
    private String m_siteRoot;

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
     * Adds 'normal' sites.<p>
     */
    public void addNormalSites() {

        CmsSiteManagerImpl siteManager = OpenCms.getSiteManager();
        List<CmsSite> sites = siteManager.getAvailableSites(m_cms, true, false, m_cms.getRequestContext().getOuFqn());
        for (CmsSite site : sites) {
            String siteRoot = site.getSiteRoot();
            Type type = Type.site;
            String message = siteRoot;
            if (siteRoot.equals("")) {
                type = Type.root;
                message = "/";
            }
            addOption(type, siteRoot, message);
        }
    }

    /**
     * Adds the shared folder.<p>
     */
    public void addSharedSite() {

        String shared = OpenCms.getSiteManager().getSharedFolder();
        if (shared != null) {
            addOption(
                Type.shared,
                shared,
                org.opencms.workplace.Messages.get().getBundle(OpenCms.getWorkplaceManager().getWorkplaceLocale(m_cms)).key(
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

        CmsSiteSelectorOption option = new CmsSiteSelectorOption(type, siteRoot, m_siteRoot.equals(siteRoot), message);
        m_options.add(option);
    }
}
