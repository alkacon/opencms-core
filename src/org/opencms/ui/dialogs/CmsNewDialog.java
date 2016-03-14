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

package org.opencms.ui.dialogs;

import org.opencms.ade.configuration.CmsADEConfigData;
import org.opencms.ade.configuration.CmsElementView;
import org.opencms.ade.configuration.CmsResourceTypeConfig;
import org.opencms.ade.containerpage.CmsAddDialogTypeHelper;
import org.opencms.ade.galleries.shared.CmsResourceTypeBean;
import org.opencms.ade.galleries.shared.CmsResourceTypeBean.Origin;
import org.opencms.configuration.preferences.CmsElementViewPreference;
import org.opencms.db.CmsUserSettings;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsProperty;
import org.opencms.file.CmsResource;
import org.opencms.file.CmsResourceFilter;
import org.opencms.lock.CmsLock;
import org.opencms.main.CmsException;
import org.opencms.main.CmsLog;
import org.opencms.main.OpenCms;
import org.opencms.ui.A_CmsUI;
import org.opencms.ui.CmsVaadinUtils;
import org.opencms.ui.I_CmsDialogContext;
import org.opencms.ui.I_CmsUpdateListener;
import org.opencms.ui.Messages;
import org.opencms.ui.apps.CmsAppWorkplaceUi;
import org.opencms.ui.components.CmsBasicDialog;
import org.opencms.ui.components.CmsErrorDialog;
import org.opencms.ui.components.CmsResourceInfo;
import org.opencms.ui.components.OpenCmsTheme;
import org.opencms.ui.components.extensions.CmsGwtDialogExtension;
import org.opencms.util.CmsStringUtil;
import org.opencms.util.CmsUUID;
import org.opencms.workplace.CmsWorkplace;
import org.opencms.workplace.explorer.CmsExplorerTypeSettings;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.apache.commons.lang.RandomStringUtils;
import org.apache.commons.logging.Log;

import com.google.common.base.Predicate;
import com.google.common.collect.ComparisonChain;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.vaadin.annotations.DesignRoot;
import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.Property.ValueChangeListener;
import com.vaadin.event.LayoutEvents.LayoutClickEvent;
import com.vaadin.event.LayoutEvents.LayoutClickListener;
import com.vaadin.server.FontAwesome;
import com.vaadin.server.VaadinService;
import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.ui.AbstractComponent;
import com.vaadin.ui.AbstractSelect.ItemCaptionMode;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.CheckBox;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.Component;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.declarative.Design;

/**
 * Dialog for creating new resources.<p>
 */
@DesignRoot
public class CmsNewDialog extends CmsBasicDialog {

    /** Default value for the 'default location' check box. */
    public static final Boolean DEFAULT_LOCATION_DEFAULT = Boolean.TRUE;

    /** Id for the 'All' pseudo-view. */
    public static final CmsUUID ID_VIEW_ALL = CmsUUID.getConstantUUID("view-all");

    /** Setting name for the standard view. */
    public static final String SETTING_STANDARD_VIEW = "newDialogStandardView";

    /** The 'All' pseudo-view. */
    public static final CmsElementView VIEW_ALL = new CmsElementView(ID_VIEW_ALL);

    /** Logger instance for this class. */
    private static final Log LOG = CmsLog.getLog(CmsNewDialog.class);

    /** Serial version id. */
    private static final long serialVersionUID = 1L;

    /** The cancel button. */
    protected Button m_cancelButton;

    /** The created resource. */
    protected CmsResource m_createdResource;

    /** The current view id. */
    protected CmsElementView m_currentView;

    /** Check box for enabling / disabling default creation folders. */
    protected CheckBox m_defaultLocationCheckbox;

    /** The dialog context. */
    protected I_CmsDialogContext m_dialogContext;

    /** The current folder. */
    protected CmsResource m_folderResource;

    /** The selected type. */
    protected CmsResourceTypeBean m_selectedType;

    /** Container for the type list. */
    protected VerticalLayout m_typeContainer;

    /** The type helper. */
    protected CmsAddDialogTypeHelper m_typeHelper;

    /** Element view selector. */
    protected ComboBox m_viewSelector;

    /** List of all types. */
    private List<CmsResourceTypeBean> m_allTypes;

    /**
     * Creates a new instance.<p>
     *
     * @param folderResource the folder resource
     * @param context the context
     */
    public CmsNewDialog(CmsResource folderResource, I_CmsDialogContext context) {
        m_folderResource = folderResource;
        m_dialogContext = context;

        Design.read(this);
        CmsVaadinUtils.visitDescendants(this, new Predicate<Component>() {

            public boolean apply(Component component) {

                component.setCaption(CmsVaadinUtils.localizeString(component.getCaption()));
                return true;
            }
        });
        CmsUUID initViewId = (CmsUUID)VaadinService.getCurrentRequest().getWrappedSession().getAttribute(
            SETTING_STANDARD_VIEW);
        if (initViewId == null) {
            try {
                CmsUserSettings settings = new CmsUserSettings(A_CmsUI.getCmsObject());
                String viewSettingStr = settings.getAdditionalPreference(
                    CmsElementViewPreference.EXPLORER_PREFERENCE_NAME,
                    true);
                if ((viewSettingStr != null) && CmsUUID.isValidUUID(viewSettingStr)) {
                    initViewId = new CmsUUID(viewSettingStr);
                }
            } catch (Exception e) {
                LOG.error(e.getLocalizedMessage(), e);
            }
        }
        if (initViewId == null) {
            initViewId = CmsUUID.getNullUUID();
        }
        CmsElementView initView = initViews(initViewId);

        m_cancelButton.addClickListener(new ClickListener() {

            private static final long serialVersionUID = 1L;

            public void buttonClick(ClickEvent event) {

                finish(new ArrayList<CmsUUID>());
            }
        });

        m_defaultLocationCheckbox.setValue(DEFAULT_LOCATION_DEFAULT);
        m_defaultLocationCheckbox.addValueChangeListener(new ValueChangeListener() {

            private static final long serialVersionUID = 1L;

            public void valueChange(ValueChangeEvent event) {

                try {
                    init(m_currentView, ((Boolean)event.getProperty().getValue()).booleanValue());
                } catch (Exception e) {
                    m_dialogContext.error(e);
                }

            }
        });
        m_viewSelector.setNullSelectionAllowed(false);
        m_viewSelector.setTextInputAllowed(false);
        m_typeContainer.addLayoutClickListener(new LayoutClickListener() {

            private static final long serialVersionUID = 1L;

            public void layoutClick(LayoutClickEvent event) {

                CmsResourceTypeBean clickedType = (CmsResourceTypeBean)(((AbstractComponent)(event.getChildComponent())).getData());
                handleSelection(clickedType);
            }
        });

        init(initView, true);
    }

    /**
     * Notifies the context that the given ids have changed.<p>
     *
     * @param ids the ids
     */
    public void finish(List<CmsUUID> ids) {

        m_dialogContext.finish(ids);
        if (ids.size() == 1) {
            m_dialogContext.focus(ids.get(0));

        }
    }

    /**
     * Initializes and displays the type list for the given view.<p>
     *
     * @param view the element view
     * @param useDefault true if we should use the default location for resource creation
     */
    public void init(CmsElementView view, boolean useDefault) {

        m_currentView = view;
        if (!view.getId().equals(m_viewSelector.getValue())) {
            m_viewSelector.setValue(view.getId());
        }
        m_typeContainer.removeAllComponents();

        List<CmsResourceTypeBean> typeBeans = m_typeHelper.getPrecomputedTypes(view);
        if (view.getId().equals(ID_VIEW_ALL)) {
            typeBeans = m_allTypes;
        }

        if (typeBeans == null) {

            LOG.warn("precomputed type list is null: " + view.getTitle(A_CmsUI.getCmsObject(), Locale.ENGLISH));
            return;
        }
        for (CmsResourceTypeBean type : typeBeans) {
            final String typeName = type.getType();
            String title = typeName;
            String subtitle = getSubtitle(type, useDefault);
            CmsExplorerTypeSettings explorerType = OpenCms.getWorkplaceManager().getExplorerTypeSetting(typeName);
            String iconUri = explorerType.getBigIconIfAvailable();
            title = CmsVaadinUtils.getMessageText(explorerType.getKey());
            CmsResourceInfo info = new CmsResourceInfo(
                title,
                subtitle,
                CmsWorkplace.getResourceUri("filetypes/" + iconUri));
            info.setData(type);
            m_typeContainer.addComponent(info);
            info.getButtonLabel().setContentMode(ContentMode.HTML);
            String labelClass = getLabelClass();
            info.getButtonLabel().setValue("<span class='" + labelClass + "'>");
            info.getButtonLabel().addStyleName(OpenCmsTheme.RESINFO_HIDDEN_ICON);
        }
    }

    /**
     * Creates type helper which is responsible for generating the type list.<p>
     *
     * @return the type helper
     */
    protected CmsAddDialogTypeHelper createTypeHelper() {

        return new CmsAddDialogTypeHelper() {

            @Override
            protected boolean exclude(CmsResourceTypeBean type) {

                String typeName = type.getType();
                CmsExplorerTypeSettings explorerType = OpenCms.getWorkplaceManager().getExplorerTypeSetting(typeName);
                boolean noCreate = !(type.isCreatableType() && !type.isDeactivated());
                return noCreate || (explorerType == null) || CmsStringUtil.isEmpty(explorerType.getNewResourceUri());
            }
        };

    }

    /**
     * Gets the HTML for the action buttons the type info boxes.<p>
     *
     * @return the HTML for the buttons
     */
    protected String getActionIconHtml() {

        return FontAwesome.PLUS.getHtml();
    }

    /**
     * Returns the class for the button label.<p>
     *
     * @return the CSS class for the button label
     */
    protected String getLabelClass() {

        return "o-addIcon";
    }

    /**
     * Gets the subtitle for the type info widget.<p>
     *
     * @param type the type
     * @param useDefault true if we are in 'use default' mode
     *
     * @return the subtitle
     */
    protected String getSubtitle(CmsResourceTypeBean type, boolean useDefault) {

        String subtitle = "";
        CmsExplorerTypeSettings explorerType = OpenCms.getWorkplaceManager().getExplorerTypeSetting(type.getType());
        if ((explorerType != null) && (explorerType.getInfo() != null)) {
            subtitle = CmsVaadinUtils.getMessageText(explorerType.getInfo());
        }
        if (useDefault && (type.getOrigin() == Origin.config) && (type.getCreatePath() != null)) {
            String path = type.getCreatePath();
            CmsObject cms = A_CmsUI.getCmsObject();
            path = cms.getRequestContext().removeSiteRoot(path);
            subtitle = CmsVaadinUtils.getMessageText(Messages.GUI_NEW_CREATE_IN_PATH_1, path);
        }
        return subtitle;
    }

    /**
     * Handles selection of a type.<p>
     *
     * @param typeFinal the selected type
     */
    protected void handleSelection(final CmsResourceTypeBean typeFinal) {

        CmsObject cms = A_CmsUI.getCmsObject();
        m_selectedType = typeFinal;

        Boolean useDefaultLocation = m_defaultLocationCheckbox.getValue();
        if (useDefaultLocation.booleanValue() && (m_selectedType.getCreatePath() != null)) {
            try {
                CmsADEConfigData configData = OpenCms.getADEManager().lookupConfiguration(
                    cms,
                    m_folderResource.getRootPath());

                CmsResourceTypeConfig typeConfig = configData.getResourceType(m_selectedType.getType());
                CmsResource createdResource = null;
                if (typeConfig != null) {
                    createdResource = typeConfig.createNewElement(cms, m_folderResource.getRootPath());
                }
                m_createdResource = createdResource;
                CmsGwtDialogExtension gwtDialogExt = new CmsGwtDialogExtension(
                    UI.getCurrent(),
                    new I_CmsUpdateListener<String>() {

                        @SuppressWarnings("synthetic-access")
                        public void onUpdate(List<String> updatedItems) {

                            List<CmsUUID> ids = Lists.newArrayList();
                            if (updatedItems.isEmpty()) {
                                removeResource();
                            } else {
                                for (String item : updatedItems) {
                                    CmsUUID id = new CmsUUID(item);
                                    ids.add(id);
                                }
                            }
                            finish(ids);

                        }

                    });

                m_dialogContext.finish(new ArrayList<CmsUUID>());
                if (createdResource != null) {
                    CmsAppWorkplaceUi.get().disableGlobalShortcuts();
                    gwtDialogExt.editProperties(createdResource.getStructureId(), true);
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
            String realCreatePath;
            try {
                realCreatePath = OpenCms.getResourceManager().getNameGenerator().getNewFileName(
                    cms,
                    fileName,
                    6,
                    explorerNameGenerationMode);
            } catch (@SuppressWarnings("unused") CmsException e1) {
                realCreatePath = CmsStringUtil.joinPaths(sitePath, RandomStringUtils.randomAlphabetic(8));
            }

            try {
                CmsResource createdResource = cms.createResource(
                    realCreatePath,
                    OpenCms.getResourceManager().getResourceType(m_selectedType.getType()),
                    null,
                    Lists.<CmsProperty> newArrayList());
                if (!cms.getLock(createdResource).isInherited()) {
                    cms.unlockResource(createdResource);
                }
                m_createdResource = createdResource;
                CmsGwtDialogExtension gwtDialogExt = new CmsGwtDialogExtension(
                    UI.getCurrent(),
                    new I_CmsUpdateListener<String>() {

                        @SuppressWarnings("synthetic-access")
                        public void onUpdate(List<String> updatedItems) {

                            List<CmsUUID> ids = Lists.newArrayList();
                            if (updatedItems.isEmpty()) {
                                removeResource();
                            } else {
                                for (String item : updatedItems) {
                                    CmsUUID id = new CmsUUID(item);
                                    ids.add(id);
                                }
                            }
                            finish(ids);
                        }
                    });
                m_dialogContext.finish(new ArrayList<CmsUUID>());
                CmsAppWorkplaceUi.get().disableGlobalShortcuts();
                gwtDialogExt.editProperties(createdResource.getStructureId(), true);
            } catch (Exception e) {
                m_dialogContext.error(e);
            }

        }
    } // end

    /**
     * Initializes the view selector, using the given view id as an initial value.<p>
     *
     * @param startId the start view
     *
     * @return the start view
     */
    private CmsElementView initViews(CmsUUID startId) {

        Map<CmsUUID, CmsElementView> viewMap = OpenCms.getADEManager().getElementViews(A_CmsUI.getCmsObject());
        List<CmsElementView> viewList = new ArrayList<CmsElementView>(viewMap.values());
        Collections.sort(viewList, new Comparator<CmsElementView>() {

            public int compare(CmsElementView arg0, CmsElementView arg1) {

                return ComparisonChain.start().compare(arg0.getOrder(), arg1.getOrder()).result();
            }

        });
        m_viewSelector.setItemCaptionMode(ItemCaptionMode.EXPLICIT);
        m_typeHelper = createTypeHelper();
        m_typeHelper.precomputeTypeLists(
            A_CmsUI.getCmsObject(),
            m_folderResource.getRootPath(),
            A_CmsUI.getCmsObject().getRequestContext().removeSiteRoot(m_folderResource.getRootPath()),
            viewList,
            null);

        // also collect types in LinkedHashMap to preserve order and ensure uniqueness
        LinkedHashMap<String, CmsResourceTypeBean> allTypes = Maps.newLinkedHashMap();

        for (CmsElementView view : viewList) {

            if (view.hasPermission(A_CmsUI.getCmsObject(), m_folderResource)) {

                List<CmsResourceTypeBean> typeBeans = m_typeHelper.getPrecomputedTypes(view);

                if (typeBeans.isEmpty()) {
                    continue;
                }
                for (CmsResourceTypeBean typeBean : typeBeans) {
                    allTypes.put(typeBean.getType(), typeBean);
                }
                m_viewSelector.addItem(view.getId());
                m_viewSelector.setItemCaption(
                    view.getId(),
                    view.getTitle(A_CmsUI.getCmsObject(), A_CmsUI.get().getLocale()));
            }
        }
        m_viewSelector.addItem(VIEW_ALL.getId());
        m_viewSelector.setItemCaption(VIEW_ALL.getId(), CmsVaadinUtils.getMessageText(Messages.GUI_VIEW_ALL_0));
        m_allTypes = Lists.newArrayList(allTypes.values());
        if (allTypes.size() <= 8) {
            startId = ID_VIEW_ALL;
        }
        if (m_viewSelector.getItem(startId) == null) {
            startId = (CmsUUID)(m_viewSelector.getItemIds().iterator().next());
        }

        m_viewSelector.addValueChangeListener(new ValueChangeListener() {

            private static final long serialVersionUID = 1L;

            public void valueChange(ValueChangeEvent event) {

                CmsUUID viewId = (CmsUUID)(event.getProperty().getValue());
                CmsElementView selectedView;
                if (viewId.equals(ID_VIEW_ALL)) {
                    selectedView = VIEW_ALL;
                } else {
                    selectedView = OpenCms.getADEManager().getElementViews(A_CmsUI.getCmsObject()).get(
                        event.getProperty().getValue());
                }
                init(selectedView, m_defaultLocationCheckbox.getValue().booleanValue());
                if (selectedView != VIEW_ALL) {
                    VaadinService.getCurrentRequest().getWrappedSession().setAttribute(
                        SETTING_STANDARD_VIEW,
                        (event.getProperty().getValue()));
                }
            }
        });
        if (startId.equals(ID_VIEW_ALL)) {
            return VIEW_ALL;
        } else {
            return OpenCms.getADEManager().getElementViews(A_CmsUI.getCmsObject()).get(startId);
        }
    }

    /**
     * Deletes the resource when the user cancels the creation.<p>
     */
    private void removeResource() {

        try {
            CmsObject cms = A_CmsUI.getCmsObject();
            if (m_createdResource != null) {
                CmsResource res = cms.readResource(m_createdResource.getStructureId(), CmsResourceFilter.ALL);
                if (res.getState().isNew()) {
                    CmsLock lock = cms.getLock(res);
                    if (lock.isUnlocked()) {
                        cms.lockResource(res);
                    }
                    cms.deleteResource(res, CmsResource.DELETE_PRESERVE_SIBLINGS);
                }

            }
        } catch (CmsException e) {
            CmsErrorDialog.showErrorDialog(e);
            return;
        }
    }
}
