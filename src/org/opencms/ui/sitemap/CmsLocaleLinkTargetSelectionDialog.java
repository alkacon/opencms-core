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
import org.opencms.ui.components.OpenCmsTheme;
import org.opencms.ui.components.fileselect.CmsSitemapFolderSelectDialog;
import org.opencms.ui.components.fileselect.I_CmsSelectionHandler;
import org.opencms.util.CmsUUID;

import java.util.Arrays;
import java.util.Collections;
import java.util.Locale;
import java.util.Map;

import org.apache.commons.logging.Log;

import com.google.common.base.Predicate;
import com.google.common.collect.Maps;
import com.vaadin.data.Container;
import com.vaadin.data.Item;
import com.vaadin.data.util.IndexedContainer;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Tree;
import com.vaadin.ui.Tree.ItemStyleGenerator;

/**
 * Dialog used to select a resource which should be linked to a locale group.<p>
 */
public class CmsLocaleLinkTargetSelectionDialog extends CmsSitemapFolderSelectDialog {

    /** The current mode. */
    public enum Mode {
        /** Select between roots for different locales. */
        selectLocale,

        /** Select between actual site roots. */
        selectSite;
    }

    /**
     * Style generator for the tree used to select the resource to link.<p>
     */
    protected class TargetSelectionStyleGenerator implements ItemStyleGenerator {

        /** Serial version id. */
        private static final long serialVersionUID = 1L;

        /** The locale group service instance. */
        private CmsLocaleGroupService m_groupService;

        /** Creates a new instance. */
        public TargetSelectionStyleGenerator() {
            m_groupService = A_CmsUI.getCmsObject().getLocaleGroupService();
        }

        /**
         * @see com.vaadin.ui.Tree.ItemStyleGenerator#getStyle(com.vaadin.ui.Tree, java.lang.Object)
         */
        @SuppressWarnings("synthetic-access")
        public String getStyle(Tree source, Object itemId) {

            Item item = source.getContainerDataSource().getItem(itemId);
            CmsResource resource = (CmsResource)(item.getItemProperty("resource").getValue());
            CmsResource srcResource = m_context.getResources().get(0);
            switch (m_groupService.checkLinkable(m_currentCms, srcResource, resource)) {
                case linkable:
                    return "";
                case alreadyLinked:
                    return "o-locale-link-target-alredy-linked";
                case notranslation:
                    return "o-locale-link-notranslation";
                case other:
                default:
                    return "o-locale-link-target-other";
            }
        }

    }

    /** Logger instance for this class. */
    private static final Log LOG = CmsLog.getLog(CmsLocaleLinkTargetSelectionDialog.class);

    /** Serial version id. */
    private static final long serialVersionUID = 1L;

    /** The dialog context. */
    private I_CmsDialogContext m_context;

    /** Last site/locale selection choices by mode. */
    private Map<Mode, String> m_lastValue = Maps.newHashMap();

    /** The locale compare context. */
    private I_CmsLocaleCompareContext m_localeContext;

    /** Site selector data for 'locales' mode. */
    private Container m_localeData = new IndexedContainer();

    /** Button to switch to 'locales' mode. */
    private Button m_localeModeButton = new Button(
        CmsVaadinUtils.getMessageText(Messages.GUI_LOCALECOMPARE_MODE_LOCALES_0));

    /** The current mode. */
    private Mode m_mode;

    /** Site selector data for 'sites' mode. */
    private Container m_siteData;

    /** Button to switch to 'sites' mode. */
    private Button m_siteModeButton = new Button(
        CmsVaadinUtils.getMessageText(Messages.GUI_LOCALECOMPARE_MODE_SITES_0));

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
        super();

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
        m_localeData.addContainerProperty(
            getContents().getSiteSelector().getItemCaptionPropertyId(),
            String.class,
            null);
        m_siteData = getContents().getSiteSelector().getContainerDataSource();
        HorizontalLayout modeChangeButtons = new HorizontalLayout(m_localeModeButton, m_siteModeButton);
        modeChangeButtons.addStyleName("o-locale-linkmode-bar");

        getContents().getContainer().addComponent(modeChangeButtons, 0);
        String buttonWidth = "100px";
        m_siteModeButton.setWidth(buttonWidth);
        m_localeModeButton.setWidth(buttonWidth);
        m_siteModeButton.addClickListener(new ClickListener() {

            private static final long serialVersionUID = 1L;

            public void buttonClick(ClickEvent event) {

                setMode(Mode.selectSite);
            }
        });
        m_localeModeButton.addClickListener(new ClickListener() {

            private static final long serialVersionUID = 1L;

            public void buttonClick(ClickEvent event) {

                setMode(Mode.selectLocale);
            }
        });

        m_context = context;

        CmsLocaleGroup localeGroup = localeContext.getLocaleGroup();
        Map<Locale, CmsResource> resourcesByLocale = localeGroup.getResourcesByLocale();
        for (Map.Entry<Locale, CmsResource> entry : resourcesByLocale.entrySet()) {
            Locale localeKey = entry.getKey();
            CmsResource resourceValue = entry.getValue();
            String folderPath = null;
            if (resourceValue.isFile()) {
                folderPath = CmsResource.getParentFolder(resourceValue.getRootPath());
            } else {
                folderPath = resourceValue.getRootPath();
            }
            Item item = m_localeData.addItem(folderPath);
            item.getItemProperty(getContents().getSiteSelector().getItemCaptionPropertyId()).setValue(
                localeKey.getDisplayLanguage());
        }

        addSelectionHandler(new I_CmsSelectionHandler<CmsResource>() {

            public void onSelection(CmsResource selected) {

                onClickOk(selected);
            }
        });
        getFileTree().setItemStyleGenerator(new TargetSelectionStyleGenerator());
        getFileTree().setSelectionFilter(new Predicate<Item>() {

            @SuppressWarnings("synthetic-access")
            public boolean apply(Item item) {

                CmsResource resource = (CmsResource)(item.getItemProperty("resource").getValue());
                CmsResource srcResource = m_context.getResources().get(0);
                switch (A_CmsUI.getCmsObject().getLocaleGroupService().checkLinkable(
                    m_currentCms,
                    srcResource,
                    resource)) {
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
        if (site != null) {
            m_lastValue.put(Mode.selectSite, site.getSiteRoot());
        }
        if (group.hasLocale(secondaryLocale)) {

            CmsResource res = group.getResourcesByLocale().get(secondaryLocale);
            String folder = res.getRootPath();
            if (res.isFile()) {
                folder = CmsResource.getParentFolder(folder);
            }
            m_lastValue.put(Mode.selectLocale, folder);
            setMode(Mode.selectLocale);
        } else {
            setMode(Mode.selectSite);
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

    /**
     * @see org.opencms.ui.components.fileselect.CmsResourceSelectDialog#onSiteChange(java.lang.String)
     */
    @Override
    protected void onSiteChange(String site) {

        if ((m_mode != null) && (site != null)) {
            m_lastValue.put(m_mode, site);
        }
        super.onSiteChange(site);

    }

    /**
     * Switches the mode to a different value.<p>
     *
     * @param mode the new mode
     */
    protected void setMode(Mode mode) {

        m_mode = mode;
        switch (mode) {
            case selectLocale:
                setStyleSelected(m_siteModeButton, false);
                setStyleSelected(m_localeModeButton, true);
                getContents().getSiteSelector().setContainerDataSource(m_localeData);
                break;
            case selectSite:
            default:
                setStyleSelected(m_localeModeButton, false);
                setStyleSelected(m_siteModeButton, true);
                getContents().getSiteSelector().setContainerDataSource(m_siteData);
                break;
        }
        if (m_lastValue.get(mode) != null) {
            getContents().getSiteSelector().setValue(m_lastValue.get(mode));

        }
    }

    /**
     * Changes the style of the mode selection buttons between selected/unselected.<p>
     *
     * @param button the button to change
     * @param selected true if the button should be shown in 'selected' style
     */
    protected void setStyleSelected(Button button, boolean selected) {

        String styleName = OpenCmsTheme.BUTTON_BLUE;
        if (selected) {
            button.addStyleName(styleName);
        } else {
            button.removeStyleName(styleName);
        }
    }

}