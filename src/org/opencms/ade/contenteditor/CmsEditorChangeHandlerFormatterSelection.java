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
import org.opencms.widgets.A_CmsFormatterWidget;
import org.opencms.widgets.CmsAddFormatterWidget;
import org.opencms.xml.content.CmsXmlContent;
import org.opencms.xml.content.CmsXmlContentValueSequence;
import org.opencms.xml.types.CmsXmlBooleanValue;
import org.opencms.xml.types.I_CmsXmlContentValue;

import java.util.Collection;
import java.util.List;
import java.util.Locale;

import org.apache.commons.logging.Log;

/** Editor change handler implementation for the formatter selection in the sitemap config.
 *
 * If "Remove all formatters" is selected/deselected, the set of formatters that can be added/removed changes.
 * That means, formerly explicitly added/removed formatters in the sitemap config can not be added/removed anymore.
 * The respective content nodes must be removed. This is done by that handler.
 *
 */
public class CmsEditorChangeHandlerFormatterSelection extends A_CmsXmlContentEditorChangeHandler {

    /** The logger instance for this class. */
    private static final Log LOG = CmsLog.getLog(A_CmsFormatterWidget.class);

    /** XPath to the main node of the formatters to remove subnodes. */
    private static final String REMOVE_PATH = "/RemoveFormatters";
    /** XPath to the main node of the formatters to add subnodes. */
    private static final String ADD_PATH = "/AddFormatters";
    /** XPath to the node of a formatter to add (without number of the sequence). */
    private static final String ADD_PATH_SINGLE_NODE = ADD_PATH + "/AddFormatter";

    /**
     * Adjusts the added/removed formatters if "Remove all formatters" is checked/unchecked.
     *
     * @see org.opencms.xml.content.I_CmsXmlContentEditorChangeHandler#handleChange(org.opencms.file.CmsObject, org.opencms.xml.content.CmsXmlContent, java.util.Locale, java.util.Collection)
     */
    public CmsXmlContent handleChange(
        final CmsObject cms,
        final CmsXmlContent content,
        final Locale locale,
        final Collection<String> changedPaths) {

        if ((changedPaths.size() != 1)
            && content.getValue(changedPaths.iterator().next(), locale).getTypeName().equals(
                CmsXmlBooleanValue.TYPE_NAME)) {
            LOG.error(
                Messages.get().container(
                    Messages.ERROR_CONFIGURATION_EDITOR_CHANGE_HANDLER_FORMATTER_SELECTION_1,
                    content.getContentDefinition().getSchemaLocation()));
            return content;
        }

        String removeAllPath = changedPaths.iterator().next();
        String removeAllStringValue = content.getStringValue(cms, removeAllPath, locale);
        boolean removeAll = Boolean.valueOf(removeAllStringValue).booleanValue();
        if (removeAll && content.hasValue(REMOVE_PATH, locale)) {
            content.removeValue(REMOVE_PATH, locale, 0);
        } else if (content.hasValue(ADD_PATH, locale)) {
            String rootPath = content.getFile().getRootPath();
            List<String> optionValues = CmsAddFormatterWidget.getSelectOptionValues(cms, rootPath, false);
            CmsXmlContentValueSequence addSequence = content.getValueSequence(ADD_PATH_SINGLE_NODE, locale);
            List<I_CmsXmlContentValue> values = addSequence.getValues();
            boolean removeMainAddNode = true;
            for (int i = values.size() - 1; i >= 0; i--) {
                if (optionValues.contains(values.get(i).getStringValue(cms))) {
                    removeMainAddNode = false;
                } else {
                    content.removeValue(ADD_PATH_SINGLE_NODE, locale, i);
                }
            }
            if (removeMainAddNode) {
                content.removeValue(ADD_PATH, locale, 0);
            }
        }
        return content;
    }

}
