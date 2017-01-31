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

package org.opencms.ui.client;

import org.opencms.ade.galleries.client.CmsGalleryConfigurationJSO;
import org.opencms.ade.galleries.client.ui.CmsGalleryPopup;
import org.opencms.ade.publish.client.CmsPublishDialog;
import org.opencms.ade.publish.shared.CmsPublishData;
import org.opencms.ade.publish.shared.rpc.I_CmsPublishService;
import org.opencms.ade.publish.shared.rpc.I_CmsPublishServiceAsync;
import org.opencms.gwt.client.CmsCoreProvider;
import org.opencms.gwt.client.rpc.CmsRpcAction;
import org.opencms.gwt.client.rpc.CmsRpcPrefetcher;
import org.opencms.gwt.client.ui.CmsLockReportDialog;
import org.opencms.gwt.client.ui.CmsPreviewDialog;
import org.opencms.gwt.client.ui.CmsPreviewDialog.I_PreviewInfoProvider;
import org.opencms.gwt.client.ui.contenteditor.I_CmsContentEditorHandler;
import org.opencms.gwt.client.ui.contextmenu.CmsAbout;
import org.opencms.gwt.client.ui.contextmenu.CmsEditProperties;
import org.opencms.gwt.client.ui.contextmenu.I_CmsContextMenuCommand;
import org.opencms.gwt.client.ui.contextmenu.I_CmsContextMenuHandler;
import org.opencms.gwt.client.ui.externallink.CmsEditExternalLinkDialog;
import org.opencms.gwt.client.ui.input.category.CmsCategoryDialog;
import org.opencms.gwt.client.ui.input.upload.CmsFileInput;
import org.opencms.gwt.client.ui.preferences.CmsUserSettingsDialog;
import org.opencms.gwt.client.ui.replace.CmsReplaceHandler;
import org.opencms.gwt.client.ui.resourceinfo.CmsResourceInfoDialog;
import org.opencms.gwt.shared.CmsGwtConstants;
import org.opencms.gwt.shared.CmsHistoryVersion;
import org.opencms.gwt.shared.CmsPreviewInfo;
import org.opencms.ui.components.extensions.CmsGwtDialogExtension;
import org.opencms.ui.shared.components.I_CmsGwtDialogClientRpc;
import org.opencms.ui.shared.components.I_CmsGwtDialogServerRpc;
import org.opencms.util.CmsUUID;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import com.google.common.collect.Lists;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.logical.shared.CloseEvent;
import com.google.gwt.event.logical.shared.CloseHandler;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.rpc.SerializationException;
import com.google.gwt.user.client.ui.PopupPanel;
import com.vaadin.client.ServerConnector;
import com.vaadin.client.extensions.AbstractExtensionConnector;
import com.vaadin.shared.ui.Connect;

/**
 * Client side part of CmsGwtDialogExtension.<p>
 */
@Connect(CmsGwtDialogExtension.class)
public class CmsGwtDialogExtensionConnector extends AbstractExtensionConnector implements I_CmsGwtDialogClientRpc {

    /**
     * Context menu handler.<p>
     */
    public class ContextMenuHandler implements I_CmsContextMenuHandler {

        /**
         * @see org.opencms.gwt.client.ui.contextmenu.I_CmsContextMenuHandler#ensureLockOnResource(org.opencms.util.CmsUUID)
         */
        public boolean ensureLockOnResource(CmsUUID structureId) {

            notImplemented();
            return false;
        }

        /**
         * @see org.opencms.gwt.client.ui.contextmenu.I_CmsContextMenuHandler#getContextMenuCommands()
         */
        public Map<String, I_CmsContextMenuCommand> getContextMenuCommands() {

            notImplemented();
            return null;
        }

        /**
         * @see org.opencms.gwt.client.ui.contextmenu.I_CmsContextMenuHandler#getContextType()
         */
        public String getContextType() {

            return CmsGwtConstants.CONTEXT_TYPE_FILE_TABLE;
        }

        /**
         * @see org.opencms.gwt.client.ui.contextmenu.I_CmsContextMenuHandler#getEditorHandler()
         */
        public I_CmsContentEditorHandler getEditorHandler() {

            notImplemented();
            return null;
        }

        /**
         * @see org.opencms.gwt.client.ui.contextmenu.I_CmsContextMenuHandler#leavePage(java.lang.String)
         */
        public void leavePage(String targetUri) {

            notImplemented();
        }

        /**
         * @see org.opencms.gwt.client.ui.contextmenu.I_CmsContextMenuHandler#onSiteOrProjectChange(java.lang.String, java.lang.String)
         */
        public void onSiteOrProjectChange(String sitePath, String serverLink) {

            notImplemented();
        }

        /**
         * @see org.opencms.gwt.client.ui.contextmenu.I_CmsContextMenuHandler#refreshResource(org.opencms.util.CmsUUID)
         */
        public void refreshResource(CmsUUID structureId) {

            List<String> changed = Lists.newArrayList();
            changed.add("" + structureId);
            m_changed = changed;
            close(0);
        }

        /**
         * @see org.opencms.gwt.client.ui.contextmenu.I_CmsContextMenuHandler#unlockResource(org.opencms.util.CmsUUID)
         */
        public void unlockResource(CmsUUID structureId) {

            notImplemented();
        }

        /**
         * Throws an illegal state exception for not implemented methods.<p>
         */
        private void notImplemented() {

            throw new IllegalStateException("Not implemented");
        }
    }

    /** Serial version id. */
    private static final long serialVersionUID = 1L;

    /** List of structure ids of changed resources. */
    protected List<String> m_changed = Lists.newArrayList();

    /**
     * @see org.opencms.ui.shared.components.I_CmsGwtDialogClientRpc#editPointer(java.lang.String)
     */
    public void editPointer(String pointerStructureId) {

        CmsEditExternalLinkDialog dialog = CmsEditExternalLinkDialog.loadAndShowDialog(new CmsUUID(pointerStructureId));
        dialog.setContextMenuHandler(new I_CmsContextMenuHandler() {

            public boolean ensureLockOnResource(CmsUUID lockStructureId) {

                return false;
            }

            public Map<String, I_CmsContextMenuCommand> getContextMenuCommands() {

                return null;
            }

            public String getContextType() {

                return CmsGwtConstants.CONTEXT_TYPE_FILE_TABLE;
            }

            public I_CmsContentEditorHandler getEditorHandler() {

                return null;
            }

            public void leavePage(String targetUri) {

                // nothing to do
            }

            public void onSiteOrProjectChange(String sitePath, String serverLink) {

                // nothing to do
            }

            public void refreshResource(CmsUUID structureId) {

                m_changed.add("" + structureId);
            }

            public void unlockResource(CmsUUID structureId) {

                // nothing to do
            }
        });
        dialog.addCloseHandler(new CloseHandler<PopupPanel>() {

            public void onClose(CloseEvent<PopupPanel> event) {

                CmsGwtDialogExtensionConnector.this.close(0);
            }
        });
    }

    /**
     * @see org.opencms.ui.shared.components.I_CmsGwtDialogClientRpc#editProperties(java.lang.String, boolean)
     */
    public void editProperties(String editStructureId, boolean editName) {

        CmsEditProperties.editProperties(
            new CmsUUID(editStructureId),
            new ContextMenuHandler(),
            editName,
            new Runnable() {

                public void run() {

                    close(100);
                }
            },
            false,
            new CmsEditProperties.PropertyEditingContext());
    }

    /**
     * @see org.opencms.ui.shared.components.I_CmsGwtDialogClientRpc#openCategoriesDialog(java.lang.String)
     */
    public void openCategoriesDialog(final String structureId) {

        CmsCategoryDialog dialog = new CmsCategoryDialog(new CmsUUID(structureId), new Command() {

            public void execute() {

                m_changed.add(structureId);
                close(0);
            }
        });
        dialog.center();
    }

    /**
     * @see org.opencms.ui.shared.components.I_CmsGwtDialogClientRpc#openGalleryDialog(java.lang.String)
     */
    public void openGalleryDialog(String galleryConfiguration) {

        CmsGalleryPopup popup = new CmsGalleryPopup(
            null,
            CmsGalleryConfigurationJSO.parseConfiguration(galleryConfiguration));
        popup.addCloseHandler(new CloseHandler<PopupPanel>() {

            public void onClose(CloseEvent<PopupPanel> event) {

                close(0);
            }
        });
        popup.center();
    }

    /**
     * @see org.opencms.ui.shared.components.I_CmsGwtDialogClientRpc#openInfoDialog(java.lang.String)
     */
    public void openInfoDialog(String string) {

        CmsResourceInfoDialog.load(new CmsUUID(string), true, null, new CloseHandler<PopupPanel>() {

            public void onClose(CloseEvent<PopupPanel> event) {

                close(0);
            }
        });

    }

    /**
     * @see org.opencms.ui.shared.components.I_CmsGwtDialogClientRpc#openLockReport(java.lang.String, java.lang.String)
     */
    public void openLockReport(String dialogTitle, final String structureId) {

        CmsLockReportDialog.openDialogForResource(dialogTitle, new CmsUUID(structureId), new Command() {

            public void execute() {

                m_changed.add(structureId);
            }
        }, new Command() {

            public void execute() {

                close(0);
            }
        });

    }

    /**
     * @see org.opencms.ui.shared.components.I_CmsGwtDialogClientRpc#openPublishDialog(java.lang.String)
     */
    public void openPublishDialog(String serializedPublishData) {

        try {
            I_CmsPublishServiceAsync publishService = GWT.create(I_CmsPublishService.class);
            CmsPublishData initData = (CmsPublishData)CmsRpcPrefetcher.getSerializedObjectFromString(
                publishService,
                serializedPublishData);
            Runnable menuRefreshAction = new Runnable() {

                public void run() {

                    Window.Location.reload();
                }
            };

            CloseHandler<PopupPanel> closeHandler = new CloseHandler<PopupPanel>() {

                public void onClose(CloseEvent<PopupPanel> event) {

                    CmsPublishDialog dialog = (CmsPublishDialog)(event.getTarget());
                    long delay = 0;
                    if (dialog.hasFailed() || dialog.hasSucceeded()) {
                        m_changed = Arrays.asList("" + CmsUUID.getNullUUID());
                        delay = 700;
                    } else {
                        m_changed = Lists.newArrayList();
                    }
                    close(delay);
                }
            };

            CmsPublishDialog.showPublishDialog(initData, closeHandler, menuRefreshAction, null);
        } catch (SerializationException e) {
            throw new RuntimeException(e);

        }
    }

    /**
     * @see org.opencms.ui.shared.components.I_CmsGwtDialogClientRpc#openReplaceDialog(java.lang.String)
     */
    public void openReplaceDialog(String structureId) {

        CmsReplaceHandler handler = new CmsReplaceHandler(new CmsUUID(structureId));
        handler.setCloseHandler(new CloseHandler<PopupPanel>() {

            public void onClose(CloseEvent<PopupPanel> event) {

                close(0);
            }
        });
        handler.onChange(new CmsFileInput());

    }

    /**
     * @see org.opencms.ui.shared.components.I_CmsGwtDialogClientRpc#showAbout()
     */
    public void showAbout() {

        CmsAbout.showAbout();
    }

    /**
     * @see org.opencms.ui.shared.components.I_CmsGwtDialogClientRpc#showPreview(java.lang.String, java.lang.String)
     */
    public void showPreview(String uuid, String versionAsString) {

        final CmsUUID id = new CmsUUID(uuid);
        final CmsHistoryVersion version = CmsHistoryVersion.fromString(versionAsString);

        CmsRpcAction<CmsPreviewInfo> previewAction = new CmsRpcAction<CmsPreviewInfo>() {

            @Override
            public void execute() {

                start(0, true);
                CmsCoreProvider.getVfsService().getHistoryPreviewInfo(
                    id,
                    CmsCoreProvider.get().getLocale(),
                    version,
                    this);
            }

            @Override
            protected void onResponse(CmsPreviewInfo result) {

                stop(false);
                CmsPreviewDialog dialog = CmsPreviewDialog.createPreviewDialog(result);
                dialog.setPreviewInfoProvider(new I_PreviewInfoProvider() {

                    public void loadPreviewForLocale(String locale, AsyncCallback<CmsPreviewInfo> resultCallback) {

                        CmsCoreProvider.getVfsService().getHistoryPreviewInfo(id, locale, version, resultCallback);
                    }
                });

                dialog.center();
            }
        };
        previewAction.execute();
    }

    /**
     * @see org.opencms.ui.shared.components.I_CmsGwtDialogClientRpc#showUserPreferences()
     */
    public void showUserPreferences() {

        CmsUserSettingsDialog.loadAndShow(new Runnable() {

            public void run() {

                close(true);
            }
        });
    }

    /**
     *  Disposes of the extension and tells the server whether to re-init the UI.<p>
     *
     * @param reinitUI <code>true</code> to reinit the UI
     */
    protected void close(boolean reinitUI) {

        getRpcProxy(I_CmsGwtDialogServerRpc.class).onClose(reinitUI);
    }

    /**
     * Disposes of the extension on the server side and notifies the server of which resources have been changed.<p>
     *
     * @param delayMillis the time to wait on the server before refreshing the view
     */
    protected void close(long delayMillis) {

        getRpcProxy(I_CmsGwtDialogServerRpc.class).onClose(m_changed, delayMillis);
    }

    /**
     * @see com.vaadin.client.extensions.AbstractExtensionConnector#extend(com.vaadin.client.ServerConnector)
     */
    @Override
    protected void extend(ServerConnector target) {

        registerRpc(I_CmsGwtDialogClientRpc.class, this);
    }

}
