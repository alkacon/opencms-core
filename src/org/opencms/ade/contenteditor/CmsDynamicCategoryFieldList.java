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

package org.opencms.ade.contenteditor;

import org.opencms.file.CmsObject;
import org.opencms.main.CmsLog;
import org.opencms.xml.content.CmsXmlContent;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.apache.commons.logging.Log;

/**
 * Class used to keep track of optional dynamic category fields for a content.
 */
public class CmsDynamicCategoryFieldList {

    /** The logger instance for this class. */
    private static final Log LOG = CmsLog.getLog(CmsDynamicCategoryFieldList.class);

    /** The list of field paths. */
    private List<String> m_paths = new ArrayList<>();

    /**
     * Creates a new instance.
     */
    public CmsDynamicCategoryFieldList() {

        // do nothing
    }

    /**
     * Adds a path.
     *
     * @param path the path to add
     */
    public void add(String path) {

        m_paths.add(path);
    }

    /**
     * Tries to add the collected fields to all locales of the given content.
     *
     * @param cms the CMS context
     * @param content the content which the fields should be added to
     */
    public void ensureFields(CmsObject cms, CmsXmlContent content) {
        for (Locale locale: content.getLocales()) {
            ensureFields(cms, content, locale);
        }
    }

    /**
     * Tries to add the collected fields to one locale of the given content.
     *
     * @param cms the CMS context
     * @param content the content to add the fields to
     * @param locale the locale
     */
    public void ensureFields(CmsObject cms, CmsXmlContent content, Locale locale) {

        for (String path : m_paths) {
            if (!content.hasValue(path, locale)) {
                try {
                    content.addValue(cms, path, locale, 0);
                } catch (Exception e) {
                    LOG.error(e.getLocalizedMessage(), e);
                }
            }
        }
    }

}
