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

package org.opencms.setup;

import org.opencms.setup.ui.A_CmsSetupStep;
import org.opencms.setup.ui.CmsSetupErrorDialog;
import org.opencms.setup.ui.CmsSetupStep01License;
import org.opencms.setup.ui.CmsSetupStep02ComponentCheck;
import org.opencms.setup.ui.CmsSetupStep03Database;
import org.opencms.setup.ui.CmsSetupStep04Modules;
import org.opencms.setup.ui.CmsSetupStep05ServerSettings;
import org.opencms.setup.ui.CmsSetupStep06ImportReport;
import org.opencms.setup.ui.CmsSetupStep07ConfigNotes;
import org.opencms.setup.ui.I_SetupUiContext;
import org.opencms.ui.A_CmsUI;
import org.opencms.ui.components.CmsBasicDialog;
import org.opencms.ui.components.CmsBasicDialog.DialogWidth;
import org.opencms.util.CmsStringUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.vaadin.annotations.Theme;
import com.vaadin.server.ExternalResource;
import com.vaadin.server.Resource;
import com.vaadin.server.VaadinRequest;
import com.vaadin.ui.Window;

/**
 * UI class for the setup wizard.
 */
@Theme("opencms")
public class CmsSetupUI extends A_CmsUI implements I_SetupUiContext {

    /** Serial version id. */
    private static final long serialVersionUID = 1L;

    /** List of setup step classes. */
    private List<Class<? extends A_CmsSetupStep>> m_steps = new ArrayList<>();

    /** Currently active window. */
    private Window m_window;

    /** Current step number. */
    private int m_stepNo = 0;

    /** The setup bean. */
    private CmsSetupBean m_setupBean;

    /**
     * Gets external resource for an HTML page in the setup-resources folder.
     *
     * @param context the context
     * @param name the file name
     *
     * @return the resource for the HTML page
     */
    public static Resource getSetupPage(I_SetupUiContext context, String name) {

        String path = CmsStringUtil.joinPaths(context.getSetupBean().getContextPath(), CmsSetupBean.FOLDER_SETUP, name);
        Resource resource = new ExternalResource(path);
        return resource;
    }

    /**
     * @see org.opencms.setup.ui.I_SetupUiContext#getSetupBean()
     */
    public CmsSetupBean getSetupBean() {

        return m_setupBean;
    }

    public void stepBack() {

        updateStep(m_stepNo - 1);
    }

    /**
     * @see org.opencms.setup.ui.I_SetupUiContext#stepForward()
     */
    @Override
    public void stepForward() {

        updateStep(m_stepNo + 1);
    }

    @Override
    protected void init(VaadinRequest request) {

        try {
            getPage().setTitle("OpenCms Setup");
            this.addStyleName("opencms");
            m_steps = Arrays.asList(
                CmsSetupStep01License.class,
                CmsSetupStep02ComponentCheck.class,
                CmsSetupStep03Database.class,
                CmsSetupStep04Modules.class,
                CmsSetupStep05ServerSettings.class,
                CmsSetupStep06ImportReport.class,
                CmsSetupStep07ConfigNotes.class);

            m_setupBean = new CmsSetupBean();
            CmsSetupServlet servlet = CmsSetupServlet.getInstance();
            m_setupBean.init(servlet.getServletContext(), servlet.getServletConfig());
            if (!m_setupBean.getWizardEnabled()) {
                throw new Exception(
                    "The OpenCms setup wizard is not enabled! Please enable it in your opencms.properties.");
            }

            m_stepNo = 0;
            updateStep(0);
        } catch (Exception e) {
            e.printStackTrace();
            CmsSetupErrorDialog.showErrorDialog(e);

        }
    }

    /**
     * Shows the given step.
     *
     * @param step the step
     */
    protected void showStep(A_CmsSetupStep step) {

        Window window = newWindow();
        window.setContent(step);
        window.setCaption(step.getTitle());
        A_CmsUI.get().addWindow(window);
        window.center();
    }

    /**
     * Moves to the step with the given number.
     *
     * <p>The step number is only updated if no exceptions are thrown when instantiating/displaying the given step
     *
     * @param stepNo the step number to move to
     */
    protected void updateStep(int stepNo) {

        if ((0 <= stepNo) && (stepNo < m_steps.size())) {
            Class<? extends A_CmsSetupStep> cls = m_steps.get(stepNo);
            A_CmsSetupStep step;
            try {
                step = cls.getConstructor(I_SetupUiContext.class).newInstance(this);
                showStep(step);
                m_stepNo = stepNo; // Only update step number if no exceptions
            } catch (Exception e) {
                CmsSetupErrorDialog.showErrorDialog(e);
            }

        }
    }

    /**
     * Replaces active window with a new one and returns it.
     *
     * @return the new window
     */
    private Window newWindow() {

        if (m_window != null) {
            m_window.close();
        }
        Window window = CmsBasicDialog.prepareWindow(DialogWidth.wide);
        m_window = window;
        window.setDraggable(false);
        window.setResizable(false);
        window.setClosable(false);
        return m_window;
    }

}
