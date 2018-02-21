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

package org.opencms.ui.apps.modules.edit;

import org.opencms.ade.galleries.CmsSiteSelectorOptionBuilder;
import org.opencms.ade.galleries.shared.CmsSiteSelectorOption;
import org.opencms.db.CmsUserSettings;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsResourceFilter;
import org.opencms.main.CmsException;
import org.opencms.main.CmsLog;
import org.opencms.main.OpenCms;
import org.opencms.ui.CmsVaadinUtils;
import org.opencms.ui.apps.Messages;
import org.opencms.ui.components.fileselect.CmsPathSelectField;
import org.opencms.ui.components.fileselect.CmsResourceSelectDialog;
import org.opencms.ui.components.fileselect.CmsResourceSelectDialog.Options;
import org.opencms.util.CmsStringUtil;

import java.util.Arrays;

import org.apache.commons.logging.Log;

import com.vaadin.server.AbstractErrorMessage.ContentMode;
import com.vaadin.server.ErrorMessage;
import com.vaadin.server.UserError;
import com.vaadin.shared.ui.ErrorLevel;
import com.vaadin.ui.Component;
import com.vaadin.v7.data.Item;
import com.vaadin.v7.data.Property;
import com.vaadin.v7.data.util.IndexedContainer;

/**
 * A widget for selecting a module resource.<p>
 */
public class CmsModuleResourceSelectField extends CmsPathSelectField {

    /** The logger instance for this class. */
    private static final Log LOG = CmsLog.getLog(CmsModuleResourceSelectField.class);

    /** The serial version id. */
    private static final long serialVersionUID = 1L;

    /**
     * Creates a new instance.<p>
     */
    public CmsModuleResourceSelectField() {

        addValueChangeListener(new ValueChangeListener() {

            /** Serial version id. */
            private static final long serialVersionUID = 1L;

            @SuppressWarnings("synthetic-access")
            public void valueChange(Property.ValueChangeEvent event) {

                updateValidation();
            }

        });
    }

    /**
     * @see org.opencms.ui.components.fileselect.A_CmsFileSelectField#setCmsObject(org.opencms.file.CmsObject)
     */
    @Override
    public void setCmsObject(CmsObject cms) {

        m_cms = cms;
    }

    /**
     * Updates the site root.<p>
     *
     * @param siteRoot the site root
     */
    public void updateSite(String siteRoot) {

        try {
            CmsObject cloneCms = OpenCms.initCmsObject(m_cms);
            if (siteRoot == null) {
                siteRoot = "/system";
            }
            cloneCms.getRequestContext().setSiteRoot(siteRoot);
            m_cms = cloneCms;
        } catch (CmsException e1) {
            LOG.error(e1.getLocalizedMessage(), e1);
        }
        updateValidation();
    }

    /**
     * @see org.opencms.ui.components.fileselect.A_CmsFileSelectField#getOptions()
     */
    @Override
    protected Options getOptions() {

        Options options = new Options();

        CmsSiteSelectorOptionBuilder optBuilder = new CmsSiteSelectorOptionBuilder(m_cms);
        optBuilder.addNormalSites(true, (new CmsUserSettings(m_cms)).getStartFolder());
        optBuilder.addSharedSite();
        optBuilder.addSystemFolder();
        IndexedContainer availableSites = new IndexedContainer();
        availableSites.addContainerProperty(CmsResourceSelectDialog.PROPERTY_SITE_CAPTION, String.class, null);
        for (CmsSiteSelectorOption option : optBuilder.getOptions()) {
            String siteRoot = option.getSiteRoot();
            boolean matches = false;
            for (String candidate : Arrays.asList(
                m_cms.getRequestContext().getSiteRoot(),
                "/system",
                OpenCms.getSiteManager().getSharedFolder())) {
                if (CmsStringUtil.comparePaths(candidate, siteRoot)) {
                    matches = true;
                    break;
                }
            }
            if (matches) {
                Item siteItem = availableSites.addItem(option.getSiteRoot());
                siteItem.getItemProperty(CmsResourceSelectDialog.PROPERTY_SITE_CAPTION).setValue(option.getMessage());
            }
        }
        options.setSiteSelectionContainer(availableSites);
        return options;

    }

    /**
     * Updates the validation status.<p>
     */
    private void updateValidation() {

        boolean changed = false;
        changed |= CmsVaadinUtils.updateComponentError(m_textField, null);
        String path = getValue();
        if (!CmsStringUtil.isEmptyOrWhitespaceOnly(path)) {
            if (!m_cms.existsResource(path, CmsResourceFilter.IGNORE_EXPIRATION)) {
                ErrorMessage error = new UserError(
                    CmsVaadinUtils.getMessageText(Messages.GUI_MODULES_MODULE_RESOURCE_NOT_FOUND_0),
                    ContentMode.TEXT,
                    ErrorLevel.WARNING);
                changed |= CmsVaadinUtils.updateComponentError(m_textField, error);
            }
        }
        if (changed) {
            fireEvent(new Component.ErrorEvent(null, this));
        }
    }

}
