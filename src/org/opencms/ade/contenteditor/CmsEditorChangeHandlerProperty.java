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
import org.opencms.file.CmsProperty;
import org.opencms.main.CmsException;
import org.opencms.main.CmsLog;
import org.opencms.util.CmsStringUtil;
import org.opencms.xml.content.CmsXmlContent;
import org.opencms.xml.types.I_CmsXmlContentValue;

import java.util.Collection;
import java.util.Locale;

import org.apache.commons.logging.Log;

/**
 * Handles editor content changes to read OpenCms resource properties and insert their values into the edited content.<p>
 */
public class CmsEditorChangeHandlerProperty extends A_CmsXmlContentEditorChangeHandler {

    /** The logger instance. */
    protected static final Log LOG = CmsLog.getLog(CmsEditorChangeHandlerProperty.class);

    /** The property to read. */
    private String m_propertyName;

    /** The content field to manipulate. */
    private String m_targetField;

    /**
     * @see org.opencms.xml.content.I_CmsXmlContentEditorChangeHandler#handleChange(org.opencms.file.CmsObject, org.opencms.xml.content.CmsXmlContent, java.util.Locale, java.util.Collection)
     */
    public CmsXmlContent handleChange(
        CmsObject cms,
        CmsXmlContent content,
        Locale locale,
        Collection<String> changedPaths) {

        if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(m_propertyName)
            && CmsStringUtil.isNotEmptyOrWhitespaceOnly(m_targetField)) {
            for (String path : changedPaths) {
                I_CmsXmlContentValue value = content.getValue(path, locale);
                if (value != null) {
                    String val = value.getStringValue(cms);
                    if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(val)) {
                        try {
                            CmsProperty prop = cms.readPropertyObject(val, m_propertyName, false);

                            if (!prop.isNullProperty()) {
                                String target = resolveRelativePath(path, m_targetField);
                                if (content.hasValue(target, locale)) {
                                    content.getValue(target, locale).setStringValue(cms, prop.getValue());
                                } else {
                                    content.addValue(cms, target, locale, 0).setStringValue(cms, prop.getValue());
                                }
                            }
                        } catch (CmsException e) {
                            LOG.error(e.getLocalizedMessage(), e);
                        }
                    }
                }
            }
        }
        return content;
    }

    /**
     * @see org.opencms.xml.content.I_CmsXmlContentEditorChangeHandler#setConfiguration(java.lang.String)
     */
    @Override
    public void setConfiguration(String configuration) {

        super.setConfiguration(configuration);
        String[] temp = m_configuration.split("\\|");
        if (temp.length == 2) {
            m_propertyName = temp[0];
            m_targetField = temp[1];
        }
    }
}
