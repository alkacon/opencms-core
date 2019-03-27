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

import org.opencms.module.CmsModule;
import org.opencms.setup.CmsSetupBean;
import org.opencms.setup.CmsSetupComponent;
import org.opencms.ui.CmsVaadinUtils;
import org.opencms.util.CmsStringUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.vaadin.ui.Button;
import com.vaadin.ui.CheckBox;
import com.vaadin.ui.FormLayout;

/**
 * Setup step: Selecting components (= module groups).
 */
public class CmsSetupStep04Modules extends A_CmsSetupStep {

    /** Serial version id. */
    private static final long serialVersionUID = 1L;

    /** Back button. */
    private Button m_backButton;

    /** The list of check boxes for the components. */
    private List<CheckBox> m_componentCheckboxes = new ArrayList<>();

    /** The map of components, with their ids as keys. */
    private Map<String, CmsSetupComponent> m_componentMap = new HashMap<>();

    /** Panel for components. */
    private FormLayout m_components;

    /** The forward button. */
    private Button m_forwardButton;

    /**
     * Creates a new instance.
     *
     * @param context the setup context
     */
    public CmsSetupStep04Modules(I_SetupUiContext context) {

        super(context);

        CmsVaadinUtils.readAndLocalizeDesign(this, null, null);
        CmsSetupBean bean = context.getSetupBean();
        bean.getAvailableModules();
        initComponents(bean.getComponents().elementList());
        m_forwardButton.addClickListener(evt -> forward());
        m_backButton.addClickListener(evt -> m_context.stepBack());
    }

    /**
     * Moves to the next step.
     */
    private void forward() {

        Set<String> selected = new HashSet<>();

        for (CheckBox checkbox : m_componentCheckboxes) {
            CmsSetupComponent component = (CmsSetupComponent)(checkbox.getData());
            if (checkbox.getValue().booleanValue()) {
                selected.add(component.getId());
            }
        }
        String error = null;
        for (String compId : selected) {
            CmsSetupComponent component = m_componentMap.get(compId);
            for (String dep : component.getDependencies()) {
                if (!selected.contains(dep)) {
                    error = "Unfulfilled dependency: The component "
                        + component.getName()
                        + " can not be installed because its dependency "
                        + m_componentMap.get(dep).getName()
                        + " is not selected";
                    break;
                }
            }
        }
        if (error == null) {
            Set<String> modules = new HashSet<>();

            for (CmsSetupComponent component : m_componentMap.values()) {

                if (selected.contains(component.getId())) {

                    for (CmsModule module : m_context.getSetupBean().getAvailableModules().values()) {
                        if (component.match(module.getName())) {
                            modules.add(module.getName());
                        }
                    }
                }
            }
            List<String> moduleList = new ArrayList<>(modules);
            m_context.getSetupBean().setInstallModules(CmsStringUtil.listAsString(moduleList, "|"));
            m_context.stepForward();
        } else {
            CmsSetupErrorDialog.showErrorDialog(error, error);
        }
    }

    /**
     * Initializes the components.
     *
     * @param components the components
     */
    private void initComponents(List<CmsSetupComponent> components) {

        for (CmsSetupComponent component : components) {
            CheckBox checkbox = new CheckBox();
            checkbox.setValue(component.isChecked());
            checkbox.setCaption(component.getName() + " - " + component.getDescription());
            checkbox.setDescription(component.getDescription());
            checkbox.setData(component);
            checkbox.setWidth("100%");
            m_components.addComponent(checkbox);
            m_componentCheckboxes.add(checkbox);
            m_componentMap.put(component.getId(), component);

        }
    }

}
