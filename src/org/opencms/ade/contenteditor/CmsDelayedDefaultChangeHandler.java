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
import org.opencms.search.galleries.CmsGalleryNameMacroResolver;
import org.opencms.util.CmsStringUtil;
import org.opencms.xml.content.CmsXmlContent;
import org.opencms.xml.types.I_CmsXmlContentValue;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;

/**
 * A change handler that reacts to changes in a field by setting another empty field to a default value.
 *
 * <p>This is useful because the default value used may contain macros, which are expanded every time the
 * change handler is called, rather than only during the initial editor load.
 *
 * <p>The change handler's configuration consists of two strings separated by a pipe symbol:
 *
 * <p>path|default
 *
 * <p>path is the path of the field relative to the changed field which should be filled, while default is
 * the default value to write to the field (possibly containing macros).
 */
public class CmsDelayedDefaultChangeHandler extends A_CmsXmlContentEditorChangeHandler {

    /** The relative path of the target field. */
    private String m_relPath;

    /** The default value to set. */
    private String m_default;

    /**
     * @see org.opencms.xml.content.I_CmsXmlContentEditorChangeHandler#handleChange(org.opencms.file.CmsObject, org.opencms.xml.content.CmsXmlContent, java.util.Locale, java.util.Collection)
     */
    public CmsXmlContent handleChange(
        CmsObject cms,
        CmsXmlContent content,
        Locale locale,
        Collection<String> changedPaths) {

        List<String> paths = new ArrayList<String>(changedPaths);
        for (String changedPath : paths) {
            String fieldPath = resolveRelativePath(changedPath, m_relPath);
            I_CmsXmlContentValue value = content.getValue(fieldPath, locale);
            if ((value != null) && CmsStringUtil.isEmptyOrWhitespaceOnly(value.getStringValue(cms))) {
                CmsGalleryNameMacroResolver resolver = new CmsGalleryNameMacroResolver(cms, content, locale);
                String newVal = resolver.resolveMacros(m_default);
                value.setStringValue(cms, newVal);
            }
        }
        return content;
    }

    /**
     * @see org.opencms.ade.contenteditor.A_CmsXmlContentEditorChangeHandler#setConfiguration(java.lang.String)
     */
    @Override
    public void setConfiguration(String configuration) {

        super.setConfiguration(configuration);
        int pipePos = configuration.indexOf("|");
        if ((pipePos < 0) || (pipePos == (configuration.length() - 1))) {
            throw new RuntimeException("Invalid configuration for " + getClass().getName());
        }
        m_relPath = configuration.substring(0, pipePos);
        m_default = configuration.substring(pipePos + 1, configuration.length());
    }

}
