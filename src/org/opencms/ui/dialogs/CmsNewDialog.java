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

package org.opencms.ui.dialogs;

import org.opencms.ade.configuration.CmsADEConfigData;
import org.opencms.ade.configuration.CmsResourceTypeConfig;
import org.opencms.ade.galleries.shared.CmsResourceTypeBean;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsResource;
import org.opencms.main.OpenCms;
import org.opencms.ui.A_CmsUI;
import org.opencms.ui.I_CmsDialogContext;
import org.opencms.ui.apps.CmsAppWorkplaceUi;
import org.opencms.ui.components.extensions.CmsPropertyDialogExtension;
import org.opencms.ui.util.CmsNewResourceBuilder;
import org.opencms.ui.util.CmsNewResourceBuilder.I_Callback;
import org.opencms.util.CmsStringUtil;
import org.opencms.util.CmsUUID;

import java.util.ArrayList;

import com.google.common.collect.Lists;
import com.vaadin.annotations.DesignRoot;
import com.vaadin.ui.Button;
import com.vaadin.v7.data.Property.ValueChangeEvent;
import com.vaadin.v7.data.Property.ValueChangeListener;
import com.vaadin.v7.ui.CheckBox;
import com.vaadin.v7.ui.ComboBox;
import com.vaadin.v7.ui.VerticalLayout;

/**
 * Dialog for creating new resources.<p>
 */
@DesignRoot
public class CmsNewDialog extends A_CmsSelectResourceTypeDialog {

    /** Element view selector. */
    protected ComboBox m_viewSelector;

    /** Container for the type list. */
    protected VerticalLayout m_typeContainer;

    /** Check box for enabling / disabling default creation folders. */
    protected CheckBox m_defaultLocationCheckbox;

    /** The cancel button. */
    protected Button m_cancelButton;

    protected Button m_modeToggle;

    /**
     * Creates a new instance.<p>
     *
     * @param folderResource the folder resource
     * @param context the context
     */
    public CmsNewDialog(CmsResource folderResource, I_CmsDialogContext context) {

        super(folderResource, context);

        m_defaultLocationCheckbox.setValue(getInitialValueForUseDefaultLocationOption(folderResource));
        m_defaultLocationCheckbox.addValueChangeListener(new ValueChangeListener() {

            private static final long serialVersionUID = 1L;

            public void valueChange(ValueChangeEvent event) {

                try {
                    init(m_currentView, ((Boolean)event.getProperty().getValue()).booleanValue(), m_filterString);
                } catch (Exception e) {
                    m_dialogContext.error(e);
                }

            }
        });

    }

    @Override
    public Button getCancelButton() {

        return m_cancelButton;
    }

    @Override
    public Button getModeToggle() {

        return m_modeToggle;
    }

    @Override
    public VerticalLayout getVerticalLayout() {

        return m_typeContainer;
    }

    @Override
    public ComboBox getViewSelector() {

        return m_viewSelector;
    }

    /**
     * Handles selection of a type.<p>
     *
     * @param selectedType the selected type
     */
    @Override
    public void handleSelection(final CmsResourceTypeBean selectedType) {

        CmsObject cms = A_CmsUI.getCmsObject();
        m_selectedType = selectedType;
        try {

            CmsNewResourceBuilder builder = new CmsNewResourceBuilder(cms);
            builder.addCallback(new I_Callback() {

                public void onError(Exception e) {

                    m_dialogContext.error(e);
                }

                public void onResourceCreated(CmsNewResourceBuilder builderParam) {

                    finish(Lists.newArrayList(builderParam.getCreatedResource().getStructureId()));
                }
            });

            m_selectedType = selectedType;

            Boolean useDefaultLocation = m_defaultLocationCheckbox.getValue();
            if (useDefaultLocation.booleanValue() && (m_selectedType.getCreatePath() != null)) {
                try {
                    CmsADEConfigData configData = OpenCms.getADEManager().lookupConfiguration(
                        cms,
                        m_folderResource.getRootPath());

                    CmsResourceTypeConfig typeConfig = configData.getResourceType(m_selectedType.getType());
                    if (typeConfig != null) {
                        typeConfig.configureCreateNewElement(cms, m_folderResource.getRootPath(), builder);
                    }
                } catch (Exception e) {
                    m_dialogContext.error(e);
                }

            } else {
                boolean explorerNameGenerationMode = false;
                String sitePath = cms.getRequestContext().removeSiteRoot(m_folderResource.getRootPath());
                String namePattern = m_selectedType.getNamePattern();
                if (CmsStringUtil.isEmptyOrWhitespaceOnly(namePattern)) {
                    namePattern = OpenCms.getWorkplaceManager().getDefaultNamePattern(m_selectedType.getType());
                    explorerNameGenerationMode = true;
                }
                String fileName = CmsStringUtil.joinPaths(sitePath, namePattern);
                builder.setPatternPath(fileName);
                builder.setType(m_selectedType.getType());
                builder.setExplorerNameGeneration(explorerNameGenerationMode);

            }
            CmsPropertyDialogExtension ext = new CmsPropertyDialogExtension(A_CmsUI.get(), null);
            CmsAppWorkplaceUi.get().disableGlobalShortcuts();
            ext.editPropertiesForNewResource(builder);
            finish(new ArrayList<CmsUUID>());

        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }

    @Override
    public boolean useDefault() {

        return m_defaultLocationCheckbox.getValue().booleanValue();
    }

    /**
     * Gets the initial value for the 'default location' option.<p>
     *
     * @param folderResource the current folder
     *
     * @return the initial value for the option
     */
    private Boolean getInitialValueForUseDefaultLocationOption(CmsResource folderResource) {

        String rootPath = folderResource.getRootPath();
        return Boolean.valueOf(OpenCms.getSiteManager().getSiteForRootPath(rootPath) != null);
    }

}
