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

package org.opencms.ui.apps.linkvalidation;

import org.opencms.file.CmsObject;
import org.opencms.file.CmsResource;
import org.opencms.ui.A_CmsUI;
import org.opencms.ui.I_CmsDialogContext;
import org.opencms.ui.I_CmsDialogContext.ContextType;
import org.opencms.ui.apps.CmsFileExplorer;
import org.opencms.ui.apps.I_CmsContextProvider;
import org.opencms.ui.apps.projects.CmsProjectManagerConfiguration;
import org.opencms.ui.components.CmsFileTable;
import org.opencms.ui.components.CmsFileTableDialogContext;
import org.opencms.ui.components.OpenCmsTheme;
import org.opencms.ui.contextmenu.I_CmsSimpleContextMenuEntry;

import java.util.List;
import java.util.Set;

import com.vaadin.ui.Component;
import com.vaadin.v7.ui.Table;
import com.vaadin.v7.ui.Table.CellStyleGenerator;

/**
 * Result table for broken internal relations.<p>
 */
public class CmsLinkValidationInternalTable extends CmsFileTable implements I_CmsUpdatableComponent {

    /**vaadin serial id.*/
    private static final long serialVersionUID = -5023815553518761192L;

    /**CmsObject. */
    private CmsObject m_cms;

    /**Intro Component. */
    private Component m_introComponent;

    /**Empty result component. */
    private Component m_nullComponent;

    /** The available menu entries. */
    private List<I_CmsSimpleContextMenuEntry<Set<String>>> m_menuEntries;

    /**Link Validator to provide validation logic. */
    private A_CmsLinkValidator m_linkValidator;

    /** Resources to check.*/
    private List<String> m_resourcesToCheck;

    /**
     * Constructor for table.<p>
     * @param introComponent Intro Component
     * @param nullComponent  null component
     * @param linkValidator provider for validation methods
     */
    public CmsLinkValidationInternalTable(
        Component introComponent,
        Component nullComponent,
        A_CmsLinkValidator linkValidator) {

        super(null, linkValidator.getTableProperties());
        applyWorkplaceAppSettings();
        setContextProvider(new I_CmsContextProvider() {

            /**
             * @see org.opencms.ui.apps.I_CmsContextProvider#getDialogContext()
             */
            public I_CmsDialogContext getDialogContext() {

                CmsFileTableDialogContext context = new CmsFileTableDialogContext(
                    CmsProjectManagerConfiguration.APP_ID,
                    ContextType.fileTable,
                    CmsLinkValidationInternalTable.this,
                    CmsLinkValidationInternalTable.this.getSelectedResources());
                context.setEditableProperties(CmsFileExplorer.INLINE_EDIT_PROPERTIES);
                return context;
            }
        });
        setSizeFull();
        addPropertyProvider(linkValidator);
        if (linkValidator.getClickListener() != null) {
            CmsLinkValidationInternalTable.this.m_fileTable.addItemClickListener(linkValidator.getClickListener());
            addAdditionalStyleGenerator(new CellStyleGenerator() {

                public String getStyle(Table source, Object itemId, Object propertyId) {

                    if (linkValidator.getTableProperty().equals(propertyId)) {
                        return " " + OpenCmsTheme.HOVER_COLUMN;
                    }
                    return "";
                }
            });
        }
        m_linkValidator = linkValidator;
        m_introComponent = introComponent;
        m_nullComponent = nullComponent;

    }

    /**
     * @see org.opencms.ui.apps.linkvalidation.I_CmsUpdatableComponent#update(java.util.List)
     */
    public void update(List<String> resourcePaths) {

        m_resourcesToCheck = resourcePaths;
        reload();

    }

    /**
     * Reloads the table.<p>
     */
    void reload() {

        List<CmsResource> broken = m_linkValidator.failedResources(m_resourcesToCheck);

        if (broken.size() > 0) {
            setVisible(true);
            m_introComponent.setVisible(false);
            m_nullComponent.setVisible(false);
        } else {
            setVisible(false);
            m_introComponent.setVisible(false);
            m_nullComponent.setVisible(true);
        }
        fillTable(getRootCms(), broken);
    }

    /**
     * Creates a root- cms object.<p>
     *
     * @return CmsObject
     */
    private CmsObject getRootCms() {

        if (m_cms == null) {
            m_cms = A_CmsUI.getCmsObject();
            m_cms.getRequestContext().setSiteRoot("");
        }
        return m_cms;
    }

}
