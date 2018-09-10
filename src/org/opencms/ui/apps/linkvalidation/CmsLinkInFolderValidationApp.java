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
import org.opencms.main.CmsException;
import org.opencms.main.OpenCms;
import org.opencms.ui.A_CmsUI;
import org.opencms.ui.CmsVaadinUtils;
import org.opencms.ui.apps.A_CmsWorkplaceApp;
import org.opencms.ui.apps.CmsFileExplorer;
import org.opencms.ui.apps.Messages;
import org.opencms.ui.dialogs.CmsDeleteDialog;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

import com.google.common.collect.Multimap;
import com.vaadin.server.Sizeable.Unit;
import com.vaadin.ui.Component;
import com.vaadin.ui.HorizontalSplitPanel;
import com.vaadin.v7.ui.VerticalLayout;

/**
 * App to check relations of resources in folder to other folder.<p>
 */
public class CmsLinkInFolderValidationApp extends A_CmsWorkplaceApp {

    /**
     * Validator.<p>
     */
    public class InFolderValidator extends A_CmsLinkValidator {

        /**Resource which would be broken, if considered resoures would be deleted. */
        Multimap<CmsResource, CmsResource> brokenResources;

        /**
         * @see org.opencms.ui.apps.linkvalidation.A_CmsLinkValidator#failedResources(java.util.List)
         */
        @Override
        public List<CmsResource> failedResources(List<String> resources) {

            try {
                CmsObject cms = OpenCms.initCmsObject(A_CmsUI.getCmsObject());
                cms.getRequestContext().setSiteRoot("");
                List<CmsResource> res = new ArrayList<CmsResource>();
                for (String resource : resources) {
                    if (cms.existsResource(resource)) {
                        res.add(cms.readResource(resource));
                    }
                }

                brokenResources = CmsDeleteDialog.getBrokenLinks(cms, res, false);
                return new ArrayList<CmsResource>(brokenResources.keySet());
            } catch (CmsException e) {
                return new ArrayList<CmsResource>();
            }
        }

        /**
         * @see org.opencms.ui.apps.linkvalidation.A_CmsLinkValidator#failMessage(org.opencms.file.CmsResource)
         */
        @Override
        public String failMessage(CmsResource resource) {

            return brokenResources.get(resource).iterator().next().getRootPath();
        }

        /**
         * @see org.opencms.ui.apps.linkvalidation.A_CmsLinkValidator#getValidationName()
         */
        @Override
        public String getValidationName() {

            return CmsVaadinUtils.getMessageText(Messages.GUI_LINKVALIDATION_CHECK_FOLDER_RELATIONS_COLUMN_HEADER_0);
        }

    }

    /**
     * @see org.opencms.ui.apps.A_CmsWorkplaceApp#getBreadCrumbForState(java.lang.String)
     */
    @Override
    protected LinkedHashMap<String, String> getBreadCrumbForState(String state) {

        // TODO Auto-generated method stub
        return null;
    }

    /**
     * @see org.opencms.ui.apps.A_CmsWorkplaceApp#getComponentForState(java.lang.String)
     */
    @Override
    protected Component getComponentForState(String state) {

        m_rootLayout.setMainHeightFull(true);
        HorizontalSplitPanel panel = new HorizontalSplitPanel();
        VerticalLayout result = new VerticalLayout();
        result.setSizeFull();
        VerticalLayout intro = CmsVaadinUtils.getInfoLayout(Messages.GUI_LINKVALIDATION_CHECK_FOLDER_RELATIONS_INTRO_0);
        VerticalLayout nullResult = CmsVaadinUtils.getInfoLayout(
            Messages.GUI_LINKVALIDATION_CHECK_FOLDER_RELATIONS_NO_RESULT_0);

        nullResult.setVisible(false);
        CmsLinkValidationInternalTable table = new CmsLinkValidationInternalTable(
            intro,
            nullResult,
            new InFolderValidator());
        table.setVisible(false);
        table.setSizeFull();
        table.setWidth("100%");

        result.addComponent(table);
        result.addComponent(intro);
        result.addComponent(nullResult);

        table.setVisible(false);
        table.setSizeFull();
        table.setWidth("100%");
        panel.setFirstComponent(new CmsInternalResources(table));
        panel.setSecondComponent(result);

        panel.setSplitPosition(CmsFileExplorer.LAYOUT_SPLIT_POSITION, Unit.PIXELS);
        return panel;
    }

    /**
     * @see org.opencms.ui.apps.A_CmsWorkplaceApp#getSubNavEntries(java.lang.String)
     */
    @Override
    protected List<NavEntry> getSubNavEntries(String state) {

        return null;
    }

}
