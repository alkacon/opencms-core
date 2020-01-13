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

package org.opencms.setup.ui;

import org.opencms.setup.CmsSetupBean;
import org.opencms.ui.CmsVaadinUtils;

import com.vaadin.ui.Button;
import com.vaadin.ui.CheckBox;
import com.vaadin.ui.Label;
import com.vaadin.ui.VerticalLayout;

public class CmsSetupStep01License extends A_CmsSetupStep {

    /** Forward button. */
    private Button m_forwardButton;

    /** License container. */
    private VerticalLayout m_licenseContainer;

    /** Main layout. */
    private VerticalLayout m_mainLayout;

    /**
     * Creates a new instance.
     *
     * @param context the setup context
     *
     * @throws Exception if something goes wrong
     */
    public CmsSetupStep01License(I_SetupUiContext context)
    throws Exception {

        super(context);
        CmsVaadinUtils.readAndLocalizeDesign(this, null, null);
        CmsSetupBean setupBean = context.getSetupBean();
        m_forwardButton.setEnabled(false);
        if (setupBean == null) {
            m_mainLayout.addComponent(htmlLabel(readSnippet("notInitialized.html")));
        } else if (!setupBean.getWizardEnabled()) {
            m_mainLayout.addComponent(htmlLabel(readSnippet("wizardDisabled.html")));
        } else {
            Label label = htmlLabel(readSnippet("license.html"));
            label.setWidth("100%");
            m_licenseContainer.addComponent(label);

            CheckBox confirmation = new CheckBox();
            confirmation.setCaption("I accept all the terms of the preceding license agreement");
            m_mainLayout.addComponent(confirmation);
            confirmation.addValueChangeListener(evt -> {
                m_forwardButton.setEnabled(evt.getValue().booleanValue());
            });
            m_forwardButton.addClickListener(evt -> m_context.stepForward());
        }

    }

    @Override
    protected boolean isEnableMaxHeight() {

        return false;
    }

}
