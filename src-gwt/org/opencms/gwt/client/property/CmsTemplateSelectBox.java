/*
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (c) Alkacon Software GmbH (http://www.alkacon.com)
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

package org.opencms.gwt.client.property;

import org.opencms.gwt.client.ui.I_CmsAutoHider;
import org.opencms.gwt.client.ui.input.A_CmsSelectBox;
import org.opencms.gwt.client.ui.input.I_CmsHasGhostValue;
import org.opencms.gwt.shared.property.CmsClientTemplateBean;
import org.opencms.util.CmsStringUtil;

import java.util.HashMap;
import java.util.Map;

/**
 * A widget class for selecting a template for a sitemap entry in the sitemap editor.<p>
 * 
 * @since 8.0.0
 * 
 */
public class CmsTemplateSelectBox extends A_CmsSelectBox<CmsTemplateSelectCell> implements I_CmsHasGhostValue {

    /** Width to which the title label should be truncated. */
    private static final int TRUNCATION_WIDTH = 250;

    /** The select cell which is used as the widget contained in the opener.<p> */
    CmsTemplateSelectCell m_openerWidget;

    /** The possible templates which can be selected. */
    private Map<String, CmsClientTemplateBean> m_possibleTemplates = new HashMap<String, CmsClientTemplateBean>();

    /**
     * Default constructor.<p>
     * 
     * @param possibleTemplates the templates which should be available for selection
     */
    public CmsTemplateSelectBox(Map<String, CmsClientTemplateBean> possibleTemplates) {

        super();
        m_possibleTemplates = possibleTemplates;
        CmsTemplateSelectCell cell = new CmsTemplateSelectCell();
        cell.setTemplate(CmsClientTemplateBean.getNullTemplate());
        addOption(cell);
    }

    /**
     * @see org.opencms.gwt.client.ui.input.I_CmsFormWidget#getApparentValue()
     */
    public String getApparentValue() {

        return getFormValueAsString();
    }

    /**
     * Returns the templates which are available for selection.<p>
     * 
     * @return the templates which are available for selection 
     */
    public Map<String, CmsClientTemplateBean> getPossibleTemplates() {

        return m_possibleTemplates;
    }

    /**
     * @see com.google.gwt.user.client.ui.Widget#onLoad()
     */
    @Override
    public void onLoad() {

        truncate("TEMPLATE_SELECT", TRUNCATION_WIDTH);
    }

    /**
     * @see org.opencms.gwt.client.ui.input.I_CmsFormWidget#setAutoHideParent(org.opencms.gwt.client.ui.I_CmsAutoHider)
     */
    public void setAutoHideParent(I_CmsAutoHider autoHideParent) {

        // do nothing
    }

    /**
     * @see org.opencms.gwt.client.ui.input.I_CmsHasGhostValue#setGhostMode(boolean)
     */
    public void setGhostMode(boolean enable) {

        // do nothing for now 
    }

    /**
     * @see org.opencms.gwt.client.ui.input.I_CmsHasGhostValue#setGhostValue(java.lang.String, boolean)
     */
    public void setGhostValue(String value, boolean isGhostMode) {

        Map<String, CmsClientTemplateBean> templates = m_possibleTemplates;
        CmsClientTemplateBean template = templates.get(value);
        template = getDefaultTemplate(template);
        m_selectCells.get("").setTemplate(template);
        if (isGhostMode) {
            if (CmsStringUtil.isEmpty(m_selectedValue)) {
                updateOpener("");
            }
        }

    }

    /**
     * @see org.opencms.gwt.client.ui.input.A_CmsSelectBox#truncateOpener(java.lang.String, int)
     */
    @Override
    public void truncateOpener(String prefix, int width) {

        m_openerWidget.truncate(prefix + "_OPENER", TRUNCATION_WIDTH);
    }

    /**
     * @see org.opencms.gwt.client.ui.input.A_CmsSelectBox#createUnknownOption(java.lang.String)
     */
    @Override
    protected CmsTemplateSelectCell createUnknownOption(String value) {

        return null;
    }

    /**
     * @see org.opencms.gwt.client.ui.input.A_CmsSelectBox#initOpener()
     */
    @Override
    protected void initOpener() {

        m_openerWidget = new CmsTemplateSelectCell();
        m_opener.add(m_openerWidget);
    }

    /**
     * @see org.opencms.gwt.client.ui.input.A_CmsSelectBox#updateOpener(java.lang.String)
     */
    @Override
    protected void updateOpener(String newValue) {

        CmsTemplateSelectCell selectCell = m_selectCells.get(newValue);
        CmsTemplateSelectCell opener = m_openerWidget;
        opener.setTemplate(selectCell.getTemplate());

    }

    /**
     * Returns the template which should be used as the "use default" option in the template selector.<p>
     * 
     * @param template the template whose data should be used to fill the default template fields 
     * 
     * @return the default template 
     */
    private CmsClientTemplateBean getDefaultTemplate(CmsClientTemplateBean template) {

        if (template != null) {
            // replace site path with empty string and title with "default"
            CmsClientTemplateBean result = new CmsClientTemplateBean(
                template.getTitle(),
                template.getDescription(),
                "",
                template.getImgPath());
            result.setShowWeakText(true);
            return result;
        } else {
            return CmsClientTemplateBean.getNullTemplate();
        }
    }
}
