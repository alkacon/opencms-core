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

import org.opencms.gwt.client.CmsCoreProvider;
import org.opencms.gwt.client.ui.I_CmsTruncable;
import org.opencms.gwt.client.ui.css.I_CmsInputLayoutBundle;
import org.opencms.gwt.client.ui.input.A_CmsSelectCell;
import org.opencms.gwt.client.ui.input.CmsLabel;
import org.opencms.gwt.shared.property.CmsClientTemplateBean;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;

/**
 * A select option for the {@link CmsTemplateSelectBox} class.<p>
 * 
 * @since 8.0.0
 */
public class CmsTemplateSelectCell extends A_CmsSelectCell implements I_CmsTruncable {

    /**
     * The UiBinder interface used for this widget.<p>
     */
    protected interface I_CmsTemplateSelectCellUiBinder extends UiBinder<Widget, CmsTemplateSelectCell> {
        // empty
    }

    /** The parameter for the OpenCms image scaler. */
    private static final String SCALE_PARAMS = "?__scale=t:0,c:transparent,w:64,h:64";

    /** The UiBinder instance used for this widget.<p>*/
    private static I_CmsTemplateSelectCellUiBinder uiBinder = GWT.create(I_CmsTemplateSelectCellUiBinder.class);

    /** The bottom label containing the description. */
    @UiField
    protected Label m_bottomLabel;

    /** The container for the template icon. */
    @UiField
    protected FlowPanel m_imageBox;

    /** The top label containing the title. */
    @UiField
    protected CmsLabel m_topLabel;

    /** The bean containing the data for the select option. */
    private CmsClientTemplateBean m_template;

    /**
     * Default constructor.<p>
     */
    public CmsTemplateSelectCell() {

        initWidget(uiBinder.createAndBindUi(this));
    }

    /**
     * Returns the bean containing this widget's data.<p>
     * 
     * @return the template bean for this select cell 
     */
    public CmsClientTemplateBean getTemplate() {

        return m_template;
    }

    /**
     * @see org.opencms.gwt.client.ui.input.A_CmsSelectCell#getValue()
     */
    @Override
    public String getValue() {

        String result = m_template.getSitePath();
        return result;
    }

    /**
     * Updates the data displayed in the select cell from a template bean.<p>
     * 
     * @param template the template bean containing the new data for the widget 
     */
    public void setTemplate(CmsClientTemplateBean template) {

        m_template = template;
        m_topLabel.setText(template.getTitle());
        m_bottomLabel.setText(template.getDescription());
        m_imageBox.clear();
        m_imageBox.add(new Image(CmsCoreProvider.get().link(template.getImgPath() + SCALE_PARAMS)));
        String stylename = I_CmsInputLayoutBundle.INSTANCE.inputCss().weakText();
        if (template.isShowWeakText()) {
            m_topLabel.addStyleName(stylename);
            m_bottomLabel.addStyleName(stylename);
        } else {
            m_topLabel.removeStyleName(stylename);
            m_bottomLabel.removeStyleName(stylename);
        }
    }

    /**
     * @see org.opencms.gwt.client.ui.I_CmsTruncable#truncate(java.lang.String, int)
     */
    public void truncate(String textMetricsKey, int labelWidth) {

        m_topLabel.truncate(textMetricsKey + "_TOP_LABEL", labelWidth);
    }

}
