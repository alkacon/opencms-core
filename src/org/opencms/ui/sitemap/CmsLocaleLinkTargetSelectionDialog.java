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

package org.opencms.ui.sitemap;

import org.opencms.file.CmsResource;
import org.opencms.file.CmsResourceFilter;
import org.opencms.i18n.CmsLocaleGroup;
import org.opencms.i18n.CmsLocaleGroupService;
import org.opencms.main.CmsException;
import org.opencms.main.CmsLog;
import org.opencms.main.OpenCms;
import org.opencms.site.CmsSite;
import org.opencms.ui.A_CmsUI;
import org.opencms.ui.CmsVaadinUtils;
import org.opencms.ui.I_CmsDialogContext;
import org.opencms.ui.Messages;
import org.opencms.ui.components.fileselect.CmsResourceSelectDialog;
import org.opencms.ui.components.fileselect.CmsResourceTreeContainer;
import org.opencms.ui.components.fileselect.I_CmsSelectionHandler;
import org.opencms.util.CmsUUID;

import java.util.Arrays;
import java.util.Collections;
import java.util.Locale;
import java.util.Map;

import org.apache.commons.logging.Log;

import com.google.common.base.Predicate;
import com.vaadin.data.Item;
import com.vaadin.data.util.IndexedContainer;

/**
 * Dialog used to select a resource which should be linked to a locale group.<p>
 */
public class CmsLocaleLinkTargetSelectionDialog extends CmsResourceSelectDialog {

    /** Logger instance for this class. */
    private static final Log LOG = CmsLog.getLog(CmsLocaleLinkTargetSelectionDialog.class);

    /** Serial version id. */
    private static final long serialVersionUID = 1L;

    /** The dialog context. */
    I_CmsDialogContext m_context;

    /** The locale compare context. */
    private I_CmsLocaleCompareContext m_localeContext;

    /**
     * Creates a new instance.<p>
     *
     * @param context the dialog context
     * @param localeContext the locale compare context
     *
     * @throws CmsException if something goes wrong
     */
    public CmsLocaleLinkTargetSelectionDialog(I_CmsDialogContext context, I_CmsLocaleCompareContext localeContext)
    throws CmsException {
        super(CmsResourceFilter.ONLY_VISIBLE_NO_DELETED.addRequireFolder());

        m_localeContext = localeContext;
        CmsResource contextResource = context.getResources().get(0);
        CmsResource realFile = contextResource;
        if (realFile.isFolder()) {
            CmsResource defaultFile = context.getCms().readDefaultFile(realFile, CmsResourceFilter.IGNORE_EXPIRATION);
            if (defaultFile != null) {
                realFile = defaultFile;
            }
        }
        getContents().displayResourceInfo(Collections.singletonList(realFile));

        IndexedContainer siteData = (IndexedContainer)getContents().getSiteSelector().getContainerDataSource();

        m_context = context;

        CmsLocaleGroup localeGroup = localeContext.getLocaleGroup();
        Map<Locale, CmsResource> resourcesByLocale = localeGroup.getResourcesByLocale();
        int index = 0;
        for (Map.Entry<Locale, CmsResource> entry : resourcesByLocale.entrySet()) {
            Locale localeKey = entry.getKey();
            CmsResource resourceValue = entry.getValue();
            String folderPath = null;
            if (resourceValue.isFile()) {
                folderPath = CmsResource.getParentFolder(resourceValue.getRootPath());
            } else {
                folderPath = resourceValue.getRootPath();
            }

            Item item = siteData.addItemAt(index, folderPath);
            index++;
            item.getItemProperty(getContents().getSiteSelector().getItemCaptionPropertyId()).setValue(
                CmsVaadinUtils.getMessageText(
                    Messages.GUI_LOCALECOMPARE_LOCALE_LABEL_1,
                    localeKey.getDisplayLanguage()));
        }

        addSelectionHandler(new I_CmsSelectionHandler<CmsResource>() {

            public void onSelection(CmsResource selected) {

                onClickOk(selected);
            }
        });
        getFileTree().setSelectionFilter(new Predicate<Item>() {

            public boolean apply(Item item) {

                CmsResource resource = (CmsResource)(item.getItemProperty(
                    CmsResourceTreeContainer.PROPERTY_RESOURCE).getValue());
                CmsResource srcResource = m_context.getResources().get(0);
                switch (A_CmsUI.getCmsObject().getLocaleGroupService().checkLinkable(srcResource, resource)) {
                    case linkable:
                        return true;
                    default:
                        return false;

                }
            }
        });

        Locale secondaryLocale = m_localeContext.getComparisonLocale();
        CmsLocaleGroup group = m_localeContext.getLocaleGroup();
        CmsSite site = OpenCms.getSiteManager().getSiteForRootPath(m_localeContext.getRoot().getRootPath());
        if (group.hasLocale(secondaryLocale)) {

            CmsResource res = group.getResourcesByLocale().get(secondaryLocale);
            String folder = res.getRootPath();
            if (res.isFile()) {
                folder = CmsResource.getParentFolder(folder);
            }
            getContents().getSiteSelector().setValue(folder);
        } else if (site != null) {
            getContents().getSiteSelector().setValue(site.getSiteRoot());
        }

    }

    /**
     * Executed when the 'Cancel' button is clicked.<p>
     */
    public void onClickCancel() {

        m_context.finish(Arrays.<CmsUUID> asList());
    }

    /**
     * Executed when the 'OK' button is clicked.<p>
     *
     * @param selected the selected resource
     */
    public void onClickOk(CmsResource selected) {

        try {
            CmsResource target = selected;
            CmsResource source = m_context.getResources().get(0);
            CmsLocaleGroupService service = A_CmsUI.getCmsObject().getLocaleGroupService();
            service.attachLocaleGroupIndirect(source, target);
            m_context.finish(Arrays.asList(source.getStructureId()));
        } catch (CmsException e) {
            LOG.error(e.getLocalizedMessage(), e);
            m_context.error(e);
        }
    }
}