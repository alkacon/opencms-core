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

package org.opencms.ui.dialogs.history;

import org.opencms.file.CmsObject;
import org.opencms.file.CmsResource;
import org.opencms.gwt.CmsRpcException;
import org.opencms.gwt.CmsVfsService;
import org.opencms.gwt.shared.CmsHistoryResourceBean;
import org.opencms.gwt.shared.CmsHistoryResourceCollection;
import org.opencms.gwt.shared.CmsHistoryVersion.OfflineOnline;
import org.opencms.main.CmsException;
import org.opencms.main.CmsLog;
import org.opencms.main.OpenCms;
import org.opencms.ui.A_CmsUI;
import org.opencms.ui.CmsVaadinUtils;
import org.opencms.ui.I_CmsDialogContext;
import org.opencms.ui.I_CmsUpdateListener;
import org.opencms.ui.Messages;
import org.opencms.ui.components.CmsBasicDialog;
import org.opencms.ui.components.CmsConfirmationDialog;
import org.opencms.ui.components.extensions.CmsGwtDialogExtension;
import org.opencms.ui.dialogs.history.diff.CmsAttributeDiff;
import org.opencms.ui.dialogs.history.diff.CmsImageDiff;
import org.opencms.ui.dialogs.history.diff.CmsPropertyDiff;
import org.opencms.ui.dialogs.history.diff.CmsShowVersionButtons;
import org.opencms.ui.dialogs.history.diff.CmsTextDiff;
import org.opencms.ui.dialogs.history.diff.CmsValueDiff;
import org.opencms.ui.dialogs.history.diff.I_CmsDiffProvider;
import org.opencms.ui.util.CmsComponentField;
import org.opencms.ui.util.CmsLogicalCheckboxGroup;
import org.opencms.ui.util.table.CmsBeanTableBuilder;
import org.opencms.util.CmsUUID;

import java.util.Arrays;
import java.util.List;

import org.apache.commons.logging.Log;

import com.google.common.base.Optional;
import com.google.common.collect.Lists;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.CheckBox;
import com.vaadin.ui.Component;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Notification;
import com.vaadin.ui.Panel;
import com.vaadin.ui.Table;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;
import com.vaadin.ui.themes.ValoTheme;

/**
 * Dialog used to change resource modification times.<p>
 */
public class CmsHistoryDialog extends CmsBasicDialog {

    /** Logger for this class. */
    private static final Log LOG = CmsLog.getLog(CmsHistoryDialog.class);

    /** Serial version id. */
    private static final long serialVersionUID = 1L;

    /** The dialog context. */
    protected I_CmsDialogContext m_context;

    /** Unbound field for the compare button. */
    private CmsComponentField<Button> m_compareButton = CmsComponentField.newInstance();

    /** Objects used to display differences between two versions. */
    private List<I_CmsDiffProvider> m_diffs = Arrays.<I_CmsDiffProvider> asList(
        new CmsShowVersionButtons(),
        new CmsPropertyDiff(),
        new CmsAttributeDiff(),
        new CmsImageDiff(),
        new CmsTextDiff(),
        new CmsValueDiff());

    /** Check box group for column V1.<p> */
    private CmsLogicalCheckboxGroup m_group1 = new CmsLogicalCheckboxGroup();

    /** Check box group for column V2.<p> */
    private CmsLogicalCheckboxGroup m_group2 = new CmsLogicalCheckboxGroup();

    /** Container for the list. */
    private VerticalLayout m_listContainer;

    /** The resource for which the history dialog was opened. */
    private CmsResource m_resource;

    /** Unbound field for the first selected check box. */
    private CmsComponentField<CheckBox> m_selected1 = CmsComponentField.newInstance();

    /** Unbound field for the second selected check box. */
    private CmsComponentField<CheckBox> m_selected2 = CmsComponentField.newInstance();

    /**
     * Creates a new instance.<p>
     *
     * @param context the dialog context
     */
    public CmsHistoryDialog(I_CmsDialogContext context) {
        m_context = context;
        m_resource = context.getResources().get(0);
        setWidth("100%");
        CmsVaadinUtils.readAndLocalizeDesign(
            this,
            OpenCms.getWorkplaceManager().getMessages(A_CmsUI.get().getLocale()),
            null);
        CmsResource resource = context.getResources().get(0);

        CmsVfsService vfsService = new CmsVfsService();
        vfsService.setCms(context.getCms());
        try {
            CmsHistoryResourceCollection historyList = vfsService.getResourceHistoryInternal(resource.getStructureId());

            Table historyTable = buildHistoryTable(historyList);

            historyTable.setWidth("100%");
            m_listContainer.setWidth("100%");
            Button compareButton = new Button(CmsVaadinUtils.getMessageText(Messages.GUI_HISTORY_DIALOG_COMPARE_0));
            m_listContainer.addComponent(compareButton);
            m_listContainer.setComponentAlignment(compareButton, Alignment.MIDDLE_RIGHT);

            compareButton.addClickListener(new ClickListener() {

                private static final long serialVersionUID = 1L;

                @SuppressWarnings("synthetic-access")
                public void buttonClick(ClickEvent event) {

                    try {
                        tryCompare();
                    } catch (Exception e) {
                        LOG.error(e.getLocalizedMessage(), e);
                        m_context.error(e);
                    }
                }
            });
            m_compareButton.set(compareButton);
            m_group1.setChangeListener(new CmsLogicalCheckboxGroup.I_ChangeListener() {

                @SuppressWarnings("synthetic-access")
                public void onSelect(CheckBox box) {

                    m_selected1.set(box);
                    m_compareButton.get().setEnabled(canCompare(m_selected1.get(), m_selected2.get()));

                }
            });

            m_group2.setChangeListener(new CmsLogicalCheckboxGroup.I_ChangeListener() {

                @SuppressWarnings("synthetic-access")
                public void onSelect(CheckBox box) {

                    m_selected2.set(box);
                    m_compareButton.get().setEnabled(canCompare(m_selected1.get(), m_selected2.get()));
                }
            });
            m_compareButton.get().setEnabled(false);
            m_listContainer.addComponent(historyTable);
        } catch (CmsException e) {
            LOG.error(e.getLocalizedMessage(), e);
        }
        addButton(createCloseButton());
        displayResourceInfo(m_context.getResources());
    }

    /**
     * Replaces the contents of the window containing a given component with a basic dialog
     * consisting of a back button to restore the previous window state and another user provided widget.<p>
     *
     * @param currentComponent the component whose parent window's content should be replaced
     * @param newView the user supplied part of the new window content
     * @param newCaption the caption for the child dialog
     */
    public static void openChildDialog(Component currentComponent, Component newView, String newCaption) {

        final Window window = CmsVaadinUtils.getWindow(currentComponent);
        final String oldCaption = window.getCaption();
        CmsBasicDialog dialog = new CmsBasicDialog();

        VerticalLayout vl = new VerticalLayout();
        dialog.setContent(vl);
        Button backButton = new Button(CmsVaadinUtils.getMessageText(Messages.GUI_CHILD_DIALOG_GO_BACK_0));
        HorizontalLayout buttonBar = new HorizontalLayout();
        buttonBar.addComponent(backButton);
        buttonBar.setMargin(true);
        vl.addComponent(buttonBar);
        vl.addComponent(newView);
        final Component oldContent = window.getContent();
        if (oldContent instanceof CmsBasicDialog) {
            List<CmsResource> infoResources = ((CmsBasicDialog)oldContent).getInfoResources();
            dialog.displayResourceInfo(infoResources);
            if (oldContent instanceof CmsHistoryDialog) {
                dialog.addButton(((CmsHistoryDialog)oldContent).createCloseButton());
            }
        }
        backButton.addClickListener(new ClickListener() {

            private static final long serialVersionUID = 1L;

            public void buttonClick(ClickEvent event) {

                window.setContent(oldContent);
                window.setCaption(oldCaption);
                window.center();

            }

        });
        window.setContent(dialog);
        window.setCaption(newCaption);
        window.center();

    }

    /**
     * Restores a resource's state to the given version, but asks the user for confirmation beforehand.<p>
     *
     * @param cms the CMS context
     * @param structureId the structure id of the resource to restore
     * @param version the version to which the resource should be restored
     */
    public void actionRestore(final CmsObject cms, final CmsUUID structureId, final Integer version) {

        String title = CmsVaadinUtils.getMessageText(Messages.GUI_HISTORY_DIALOG_CONFIRM_RESTORE_TITLE_0);
        String message = CmsVaadinUtils.getMessageText(Messages.GUI_HISTORY_DIALOG_CONFIRM_RESTORE_0);

        CmsConfirmationDialog.show(title, message, new Runnable() {

            @SuppressWarnings("synthetic-access")
            public void run() {

                CmsVfsService svc = new CmsVfsService();
                svc.setCms(cms);
                try {
                    svc.restoreResource(structureId, version.intValue());
                    m_context.finish(Arrays.asList(m_resource.getStructureId()));
                } catch (CmsRpcException e) {
                    LOG.error(e.getLocalizedMessage(), e);
                    m_context.error(e);
                }
            }
        });
    }

    /**
     * Creates a close button for child dialogs.<p>
     *
     * @return the close button
     */
    public Button createCloseButton() {

        Button button = new Button(CmsVaadinUtils.getMessageText(org.opencms.ui.Messages.GUI_BUTTON_CLOSE_DIALOG_0));
        button.setWidth("150px");
        button.addClickListener(new ClickListener() {

            private static final long serialVersionUID = 1L;

            public void buttonClick(ClickEvent event) {

                m_context.finish(Lists.newArrayList(m_context.getResources().get(0).getStructureId()));
            }
        });
        return button;
    }

    /**
     * Opens the 'compare' view for the two selected versions of the resource.<p>
     *
     * @throws CmsException if something goes wrong
     */
    public void tryCompare() throws CmsException {

        CmsObject cms = A_CmsUI.getCmsObject();
        CheckBox check1 = m_group1.getSelected();
        CheckBox check2 = m_group2.getSelected();
        if (!canCompare(check1, check2)) {
            Notification.show(
                CmsVaadinUtils.getMessageText(Messages.GUI_HISTORY_DIALOG_SELECT_TWO_DIFFERENT_VERSIONS_0));
        } else {
            CmsHistoryResourceBean bean1 = (CmsHistoryResourceBean)(check1.getData());
            CmsHistoryResourceBean bean2 = (CmsHistoryResourceBean)(check2.getData());
            VerticalLayout diffContainer = new VerticalLayout();
            diffContainer.setSpacing(true);
            for (I_CmsDiffProvider diff : m_diffs) {
                Optional<Component> optionalDiff = diff.diff(cms, bean1, bean2);
                if (optionalDiff.isPresent()) {
                    diffContainer.addComponent(optionalDiff.get());
                }
            }
            Panel panel = new Panel();
            panel.setSizeFull();
            diffContainer.setWidth("100%");
            diffContainer.setMargin(true);
            panel.addStyleName(ValoTheme.PANEL_BORDERLESS);
            panel.setContent(diffContainer);
            openChildDialog(
                CmsHistoryDialog.this,
                panel,
                CmsVaadinUtils.getMessageText(Messages.GUI_HISTORY_DIALOG_COMPARE_0));
        }

    }

    /**
     * Displays the preview for a given resource version.<p>
     *
     * @param bean the resource version to display the preview for
     */
    private void actionPreview(CmsHistoryResourceBean bean) {

        CmsGwtDialogExtension ext = new CmsGwtDialogExtension(A_CmsUI.get(), new I_CmsUpdateListener<String>() {

            public void onUpdate(List<String> updatedItems) {

                // nothing to do
            }
        });
        OfflineOnline offOnline = null;
        if (bean.getVersion().isOffline()) {
            offOnline = OfflineOnline.offline;
        }
        if (bean.getVersion().isOnline()) {
            offOnline = OfflineOnline.online;
        }
        ext.showPreview(bean.getStructureId(), bean.getVersion().getVersionNumber(), offOnline);
    }

    /**
     * Builds a table containing the different versions of a resource.<p>
     *
     * @param historyList the list of history resource beans from which to construct the table
     *
     * @return the table
     */
    private Table buildHistoryTable(CmsHistoryResourceCollection historyList) {

        final CmsObject cms = A_CmsUI.getCmsObject();
        try {
            CmsBeanTableBuilder<CmsHistoryRow> builder = CmsBeanTableBuilder.newInstance(
                CmsHistoryRow.class,
                A_CmsUI.get().getDisplayType().toString());
            List<CmsHistoryRow> rows = Lists.newArrayList();

            for (CmsHistoryResourceBean bean : historyList.getResources()) {
                final CmsHistoryResourceBean beanFinal = bean;
                final CmsUUID structureId = bean.getStructureId();
                CmsHistoryRow row = new CmsHistoryRow(bean);
                rows.add(row);
                m_group1.add(row.getCheckBoxV1());
                m_group2.add(row.getCheckBoxV2());
                final Integer version = bean.getVersion().getVersionNumber();
                if (version != null) {
                    row.getRestoreButton().addClickListener(new ClickListener() {

                        private static final long serialVersionUID = 1L;

                        public void buttonClick(ClickEvent event) {

                            actionRestore(cms, structureId, version);

                        }

                    });
                }
                row.getPreviewButton().addClickListener(new ClickListener() {

                    private static final long serialVersionUID = 1L;

                    @SuppressWarnings("synthetic-access")
                    public void buttonClick(ClickEvent event) {

                        actionPreview(beanFinal);

                    }

                });
                for (CheckBox checkBox : Arrays.asList(row.getCheckBoxV1(), row.getCheckBoxV2())) {
                    checkBox.setData(bean);
                }
            }

            Table result = builder.buildTable(rows);
            result.setPageLength(Math.min(rows.size(), 12));
            result.setSortEnabled(false);
            return result;
        } catch (Exception e) {
            return null;

        }
    }

    /**
     * Checks if two different versions are selected, and so can be compared.<p>
     *
     * @param check1 the first selected check box
     * @param check2 the second selected check box
     *
     * @return true if the check boxes correspond to two different versions
     */
    private boolean canCompare(CheckBox check1, CheckBox check2) {

        return !((check1 == null) || (check2 == null) || (check1.getData() == check2.getData()));
    }

}
