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
 * For further information about Alkacon Software GmbH & Co. KG, please see the
 * company website: http://www.alkacon.com
 *
 * For further information about OpenCms, please see the
 * project website: http://www.opencms.org
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package org.opencms.staticexport;

import org.opencms.i18n.CmsLocaleManager;
import org.opencms.main.OpenCms;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

/**
 * Extended after publish static export handler, supporting multi-language exports.<p>
 *
 * @since 7.0.3
 *
 * @see CmsAfterPublishStaticExportHandler
 * @see I_CmsStaticExportHandler
 */
public class CmsAfterPublishMultiLanguageStaticExportHandler extends CmsAfterPublishStaticExportHandler {

    /** Cached locale matching rules. */
    private static List<CmsStaticExportRfsRule> m_rules;

    /**
     * @see org.opencms.staticexport.CmsAfterPublishStaticExportHandler#getRelatedFilesToPurge(java.lang.String, java.lang.String)
     */
    @Override
    protected List<File> getRelatedFilesToPurge(String exportFileName, String vfsName) {

        CmsStaticExportManager manager = OpenCms.getStaticExportManager();
        List<File> result = new ArrayList<File>();
        if (m_rules == null) {
            // get the locale matching rules
            CmsLocaleManager locManager = OpenCms.getLocaleManager();
            m_rules = new ArrayList<CmsStaticExportRfsRule>();
            Iterator<CmsStaticExportRfsRule> itRules = manager.getRfsRules().iterator();
            while (itRules.hasNext()) {
                CmsStaticExportRfsRule rule = itRules.next();
                Locale locale = CmsLocaleManager.getLocale(rule.getName());
                if (locManager.getDefaultLocales().contains(locale)) {
                    m_rules.add(rule);
                }
            }
        }
        // add paths for all possible locales
        Iterator<CmsStaticExportRfsRule> it = m_rules.iterator();
        while (it.hasNext()) {
            CmsStaticExportRfsRule rule = it.next();
            result.add(new File(rule.getLocalizedRfsName(exportFileName, File.separator)));
        }
        return result;
    }
}
