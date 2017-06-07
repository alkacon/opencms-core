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

package org.opencms.widgets;

import org.opencms.ade.configuration.CmsADEConfigData;
import org.opencms.file.CmsObject;
import org.opencms.main.OpenCms;
import org.opencms.workplace.CmsWorkplaceMessages;
import org.opencms.xml.containerpage.I_CmsFormatterBean;
import org.opencms.xml.types.CmsXmlDisplayFormatterValue;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

/**
 * Widget to select a type and formatter combination.<p>
 */
public class CmsDisplayTypeSelectWidget extends CmsSelectWidget {

    /**
     * Formatter option comparator, sorting by type name and rank.<p>
     */
    class FormatterComparator implements Comparator<FormatterOption> {

        /**
         * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
         */
        public int compare(FormatterOption o1, FormatterOption o2) {

            return o1.m_typeName.equals(o2.m_typeName) ? o2.m_rank - o1.m_rank : o1.m_typeName.compareTo(o2.m_typeName);
        }
    }

    /**
     * Formatter select option.<p>
     */
    class FormatterOption {

        /** The option key. */
        String m_key;

        /** The option label. */
        String m_label;

        /** The formatter rank. */
        int m_rank;

        /** The type name. */
        String m_typeName;

        /**
         * Constructor.<p>
         *
         * @param key the option key
         * @param typeName the type name
         * @param label the option label
         * @param rank the formatter rank
         */
        FormatterOption(String key, String typeName, String label, int rank) {
            m_key = key;
            m_typeName = typeName;
            m_label = label;
            m_rank = rank;
        }
    }

    /**
     * @see org.opencms.widgets.CmsSelectWidget#newInstance()
     */
    @Override
    public I_CmsWidget newInstance() {

        return new CmsDisplayTypeSelectWidget();
    }

    /**
     * @see org.opencms.widgets.A_CmsSelectWidget#parseSelectOptions(org.opencms.file.CmsObject, org.opencms.widgets.I_CmsWidgetDialog, org.opencms.widgets.I_CmsWidgetParameter)
     */
    @Override
    protected List<CmsSelectWidgetOption> parseSelectOptions(
        CmsObject cms,
        I_CmsWidgetDialog widgetDialog,
        I_CmsWidgetParameter param) {

        Locale wpLocale = OpenCms.getWorkplaceManager().getWorkplaceLocale(cms);
        List<CmsSelectWidgetOption> result = new ArrayList<CmsSelectWidgetOption>();
        List<FormatterOption> options = new ArrayList<FormatterOption>();
        CmsADEConfigData config = OpenCms.getADEManager().lookupConfiguration(cms, getResourcePath(cms, widgetDialog));
        if (config != null) {
            for (I_CmsFormatterBean formatter : config.getDisplayFormatters(cms)) {
                for (String typeName : formatter.getResourceTypeNames()) {
                    String label = formatter.getNiceName(wpLocale)
                        + " ("
                        + CmsWorkplaceMessages.getResourceTypeName(wpLocale, typeName)
                        + ")";
                    options.add(
                        new FormatterOption(
                            typeName + CmsXmlDisplayFormatterValue.SEPARATOR + formatter.getId(),
                            typeName,
                            label,
                            formatter.getRank()));
                }
            }
        }
        Collections.sort(options, new FormatterComparator());
        for (FormatterOption option : options) {
            result.add(new CmsSelectWidgetOption(option.m_key, false, option.m_label));
        }
        return result;
    }
}
