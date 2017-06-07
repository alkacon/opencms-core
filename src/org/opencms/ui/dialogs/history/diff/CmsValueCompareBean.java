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

package org.opencms.ui.dialogs.history.diff;

import org.opencms.file.CmsObject;
import org.opencms.ui.CmsVaadinUtils;
import org.opencms.ui.Messages;
import org.opencms.ui.components.OpenCmsTheme;
import org.opencms.ui.util.table.Column;
import org.opencms.util.CmsDateUtil;
import org.opencms.util.CmsStringUtil;
import org.opencms.workplace.comparison.CmsElementComparison;
import org.opencms.workplace.comparison.CmsResourceComparison;
import org.opencms.workplace.comparison.CmsXmlContentElementComparison;
import org.opencms.xml.types.CmsXmlDateTimeValue;

import java.text.DateFormat;
import java.util.Date;

import com.vaadin.ui.Button;
import com.vaadin.ui.themes.ValoTheme;

/**
 * Represents a row of the XML content value comparison table.<p>
 */
public class CmsValueCompareBean {

    /** The CMS context. */
    private CmsObject m_cms;

    /** The element comparison. */
    private CmsElementComparison m_elemComp;

    /** The button representing the change type. */
    private Button m_getChangeTypeButton;

    /**
     * Creates a new instance.<p>
     *
     * @param cms the CMS context
     * @param elemComp the element comparison bean
     */
    public CmsValueCompareBean(CmsObject cms, CmsElementComparison elemComp) {
        m_cms = cms;
        m_elemComp = elemComp;

        String changeType = m_elemComp.getStatus();
        String key = null;
        String style = null;
        if (CmsResourceComparison.TYPE_ADDED.equals(changeType)) {
            key = org.opencms.workplace.comparison.Messages.GUI_COMPARE_ADDED_0;
            style = OpenCmsTheme.DIFF_TYPE_ADDED;
        } else if (CmsResourceComparison.TYPE_REMOVED.equals(changeType)) {
            key = org.opencms.workplace.comparison.Messages.GUI_COMPARE_REMOVED_0;
            style = OpenCmsTheme.DIFF_TYPE_DELETED;
        } else if (CmsResourceComparison.TYPE_CHANGED.equals(changeType)) {
            key = org.opencms.workplace.comparison.Messages.GUI_COMPARE_CHANGED_0;
            style = OpenCmsTheme.DIFF_TYPE_CHANGED;
        } else {
            key = org.opencms.workplace.comparison.Messages.GUI_COMPARE_UNCHANGED_0;
            style = OpenCmsTheme.DIFF_TYPE_UNCHANGED;
        }

        Button result = new Button();
        result.setCaption(CmsVaadinUtils.getMessageText(key));
        result.addStyleName(ValoTheme.BUTTON_LINK);
        result.addStyleName(style);
        m_getChangeTypeButton = result;

    }

    /**
     * Formats an xml content value string for display in the value comparison table.<p>
     *
     * @param cms the CMS context
     * @param comparison the element comparison
     * @param origValue the XML content value as a string
     *
     * @return the formatted string
     */
    public static String formatContentValueForDiffTable(
        CmsObject cms,
        CmsElementComparison comparison,
        String origValue) {

        String result = CmsStringUtil.substitute(CmsStringUtil.trimToSize(origValue, 60), "\n", "");

        // formatting DateTime
        if (comparison instanceof CmsXmlContentElementComparison) {
            if (((CmsXmlContentElementComparison)comparison).getType().equals(CmsXmlDateTimeValue.TYPE_NAME)) {
                if (CmsStringUtil.isNotEmpty(result)) {

                    result = CmsDateUtil.getDateTime(
                        new Date(Long.parseLong(result)),
                        DateFormat.SHORT,
                        cms.getRequestContext().getLocale());
                }
            }
        }
        return result;
    }

    /**
     * Gets the change type, as a button.<p>
     *
     * @return the change type
     */
    @Column(header = Messages.GUI_HISTORY_DIALOG_COL_CHANGETYPE_0, order = 10)
    public Button getChangeType() {

        return m_getChangeTypeButton;
    }

    /**
     * Gets the locale.<p>
     *
     * @return the locale
     */
    @Column(header = Messages.GUI_HISTORY_DIALOG_COL_LOCALE_0, order = 20)
    public String getLocale() {

        return m_elemComp.getLocale().toString();
    }

    /**
     * Gets the value for the first version.<p>
     *
     * @return the value for the first version
     */
    @Column(header = "V1 (%(v1))", order = 40)
    public String getV1() {

        return CmsValueCompareBean.formatContentValueForDiffTable(m_cms, m_elemComp, m_elemComp.getVersion1());
    }

    /**
     * Gets the value for the second version.<p>
     *
     * @return the value for the second version
     */
    @Column(header = "V2 (%(v2))", order = 50)
    public String getV2() {

        return CmsValueCompareBean.formatContentValueForDiffTable(m_cms, m_elemComp, m_elemComp.getVersion2());
    }

    /**
     * Gets the element name.<p>
     *
     * @return the element name
     */
    @Column(header = Messages.GUI_HISTORY_DIALOG_COL_XPATH_0, order = 30)
    public String getXPath() {

        return m_elemComp.getName();
    }

}
