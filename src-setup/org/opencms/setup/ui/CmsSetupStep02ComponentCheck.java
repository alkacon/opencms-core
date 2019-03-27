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

import org.opencms.setup.comptest.CmsSetupTestResult;
import org.opencms.setup.comptest.CmsSetupTests;
import org.opencms.ui.CmsVaadinUtils;

import java.util.List;

import com.vaadin.ui.Button;
import com.vaadin.ui.CheckBox;
import com.vaadin.ui.Component;
import com.vaadin.ui.Label;
import com.vaadin.ui.VerticalLayout;

/**
 * Setup step: component check.
 */
public class CmsSetupStep02ComponentCheck extends A_CmsSetupStep {

    /** Test status enum. */
    enum TestColor {
        /** Green. */
        green,

        /** Red. */
        red,

        /** Yellow. */
        yellow;
    }

    /** Message for green status. */
    public static final String STATUS_GREEN = "Your system uses components which have been tested to work properly with Alkacon OpenCms.";

    /** Message for red status. */
    public static final String STATUS_RED = "Your system does not have the necessary components to use Alkacon OpenCms. It is assumed that OpenCms will not run on your system.";

    /** Message for yellow status. */
    public static final String STATUS_YELLOW = "Your system uses components which have not been tested to work with Alkacon OpenCms. It is possible that OpenCms will not run on your system.";

    /** Back button. */
    private Button m_backButton;

    /** Confirmation checkbox. */
    private CheckBox m_confirmCheckbox;

    /** Container for test failure notes. */
    private VerticalLayout m_failures;

    /** Forward button. */
    private Button m_forwardButton;

    /** Main layout. */
    private VerticalLayout m_mainLayout;

    /** Status label.*/
    private Label m_status;

    /** Container for test results. */
    private VerticalLayout m_testContainer;

    /**
     * Creates a new instance.
     *
     * @param context the context
     */
    public CmsSetupStep02ComponentCheck(I_SetupUiContext context) {

        super(context);

        CmsVaadinUtils.readAndLocalizeDesign(this, null, null);
        //m_icon.setContentMode(ContentMode.HTML);
        CmsSetupTests tests = new CmsSetupTests();
        tests.runTests(context.getSetupBean());
        TestColor color = null;
        color = TestColor.green;
        if (tests.isRed()) {
            color = TestColor.red;
        } else if (tests.isYellow()) {
            color = TestColor.yellow;
        } else if (tests.isRed()) {
            color = TestColor.red;
        }
        m_confirmCheckbox.addValueChangeListener(evt -> m_forwardButton.setEnabled(evt.getValue().booleanValue()));
        updateColor(color);
        showTestResults(tests.getTestResults());
        m_forwardButton.addClickListener(evt -> m_context.stepForward());
        m_backButton.addClickListener(evt -> m_context.stepBack());
    }

    /**
     * Sets test status.
     */
    public void updateColor(TestColor color) {

        switch (color) {
            case green:
                m_forwardButton.setEnabled(true);
                m_confirmCheckbox.setVisible(false);
                m_status.setValue(STATUS_GREEN);
                break;
            case yellow:
                m_forwardButton.setEnabled(false);
                m_confirmCheckbox.setVisible(true);
                m_status.setValue(STATUS_YELLOW);
                break;
            case red:
                m_forwardButton.setEnabled(false);
                m_confirmCheckbox.setVisible(true);
                m_status.setValue(STATUS_RED);
                break;
            default:
                break;
        }
    }

    /**
     * Displays setup test results.
     *
     * @param testResults the test results
     */
    private void showTestResults(List<CmsSetupTestResult> testResults) {

        m_testContainer.removeAllComponents();
        VerticalLayout layout = new VerticalLayout();
        for (CmsSetupTestResult result : testResults) {
            Component resultWidget = new CmsSetupTestResultWidget(result);
            m_testContainer.addComponent(resultWidget);
            if (!result.isGreen()) {
                Label label = new Label(result.getInfo());
                label.setWidth("100%");
                m_failures.addComponent(label);
            }
        }
    }

}
