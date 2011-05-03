/*
 * File   : $Source: /alkacon/cvs/opencms/src-modules/org/opencms/ade/sitemap/Attic/CmsSitemapFormValidator.java,v $
 * Date   : $Date: 2011/05/03 10:49:13 $
 * Version: $Revision: 1.2 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (C) 2002 - 2011 Alkacon Software (http://www.alkacon.com)
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

package org.opencms.ade.sitemap;

import org.opencms.file.CmsObject;
import org.opencms.gwt.CmsDefaultFormValidator;
import org.opencms.gwt.I_CmsFormValidator;
import org.opencms.gwt.shared.CmsValidationQuery;
import org.opencms.gwt.shared.CmsValidationResult;
import org.opencms.util.CmsStringUtil;

import java.util.Map;

/**
 * A form validator for the sitemap entry editor form.<p>
 * 
 * This class is mostly for handling the special case logic of setting the URL name when the user edits
 * the title of a fresh sitemap entry.<p>
 * 
 * @author Georg Westenberger
 * 
 * @version $Revision: 1.2 $
 * 
 * @since 8.0
 */
public class CmsSitemapFormValidator implements I_CmsFormValidator {

    /** A validator for handling the normal case. */
    private I_CmsFormValidator m_defaultValidator = new CmsDefaultFormValidator();

    /**
     * @see org.opencms.gwt.I_CmsFormValidator#validate(org.opencms.file.CmsObject, java.util.Map, java.util.Map, java.lang.String)
     */
    public Map<String, CmsValidationResult> validate(
        CmsObject cms,
        Map<String, CmsValidationQuery> queries,
        Map<String, String> values,
        String config) throws Exception {

        String KEY_TITLE = "field_title";
        String KEY_NAME = "field_urlname";

        Map<String, String> configMap = CmsStringUtil.splitAsMap(config, "|", ":");
        String forbiddenNamesStr = configMap.get("forbidden").replace('#', '|');

        boolean hasTitleQuery = queries.get(KEY_TITLE) != null;
        boolean hasNameQuery = queries.get(KEY_NAME) != null;
        boolean isNew = "true".equals(configMap.get("new"));

        boolean isRoot = false; // TODO: get the real value 
        if (hasTitleQuery
            && !hasNameQuery
            && !isRoot
            && isNew
            && !CmsStringUtil.isEmptyOrWhitespaceOnly(queries.get(KEY_TITLE).getValue())) {

            // This only applies if the user has edited the title, but not the name, of a new sitemap entry,
            // and the title is non-empty. 

            String title = queries.get(KEY_TITLE).getValue();
            CmsValidationQuery nameQuery = new CmsValidationQuery(
                "org.opencms.ade.sitemap.CmsUrlNameValidationService",
                title,
                forbiddenNamesStr);
            queries.put(KEY_NAME, nameQuery);
            return m_defaultValidator.validate(cms, queries, values, null);
        } else {
            Map<String, CmsValidationResult> result = m_defaultValidator.validate(cms, queries, values, config);
            return result;
        }
    }

}
