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

package org.opencms.ade.containerpage.client;

import org.opencms.ade.containerpage.client.ui.CmsAddToFavoritesButton;
import org.opencms.ade.containerpage.client.ui.CmsToolbarAllGalleriesMenu;
import org.opencms.ade.containerpage.client.ui.CmsToolbarClipboardMenu;
import org.opencms.ade.containerpage.client.ui.CmsToolbarEditButton;
import org.opencms.ade.containerpage.client.ui.CmsToolbarElementInfoButton;
import org.opencms.ade.containerpage.client.ui.CmsToolbarFavLocationButton;
import org.opencms.ade.containerpage.client.ui.CmsToolbarGalleryMenu;
import org.opencms.ade.containerpage.client.ui.CmsToolbarInfoButton;
import org.opencms.ade.containerpage.client.ui.CmsToolbarListAddButton;
import org.opencms.ade.containerpage.client.ui.CmsToolbarListManagerButton;
import org.opencms.ade.containerpage.client.ui.CmsToolbarMoveButton;
import org.opencms.ade.containerpage.client.ui.CmsToolbarPublishButton;
import org.opencms.ade.containerpage.client.ui.CmsToolbarRemoveButton;
import org.opencms.ade.containerpage.client.ui.CmsToolbarSelectionButton;
import org.opencms.ade.containerpage.client.ui.CmsToolbarSettingsButton;
import org.opencms.ade.containerpage.client.ui.css.I_CmsLayoutBundle;
import org.opencms.gwt.client.A_CmsEntryPoint;
import org.opencms.gwt.client.CmsBroadcastTimer;
import org.opencms.gwt.client.CmsCoreProvider;
import org.opencms.gwt.client.CmsRpcContext;
import org.opencms.gwt.client.dnd.CmsCompositeDNDController;
import org.opencms.gwt.client.dnd.CmsDNDHandler;
import org.opencms.gwt.client.dnd.CmsDNDHandler.AnimationType;
import org.opencms.gwt.client.ui.CmsErrorDialog;
import org.opencms.gwt.client.ui.CmsPushButton;
import org.opencms.gwt.client.ui.CmsQuickLauncher.A_QuickLaunchHandler;
import org.opencms.gwt.client.ui.CmsToolbar;
import org.opencms.gwt.client.ui.CmsToolbarContextButton;
import org.opencms.gwt.client.ui.I_CmsToolbarButton;
import org.opencms.gwt.client.ui.contextmenu.I_CmsContextMenuCommand;
import org.opencms.gwt.client.ui.contextmenu.I_CmsContextMenuCommandInitializer;
import org.opencms.gwt.client.util.CmsDomUtil;
import org.opencms.gwt.client.util.CmsEmbeddedDialogFrame;
import org.opencms.gwt.client.util.CmsStyleVariable;
import org.opencms.gwt.shared.CmsGwtConstants;
import org.opencms.gwt.shared.CmsGwtConstants.QuickLaunch;
import org.opencms.gwt.shared.CmsQuickLaunchParams;
import org.opencms.util.CmsStringUtil;

import java.util.Map;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.Widget;

import elemental2.dom.DomGlobal;

/**
 * The container page editor.<p>
 *
 * @since 8.0.0
 */
public class CmsContainerpageEditor extends A_CmsEntryPoint {

    /** Margin-top added to the document body element when the tool-bar is shown. */
    //    private int m_bodyMarginTop;

    /**
     * Quick launch handler for the page editor.
     */
    public static class PageEditorQuickLaunchHandler extends A_QuickLaunchHandler {

        /**
         * @see org.opencms.gwt.client.ui.CmsQuickLauncher.I_QuickLaunchHandler#getParameters()
         */
        public CmsQuickLaunchParams getParameters() {

            return new CmsQuickLaunchParams(
                QuickLaunch.CONTEXT_PAGE,
                CmsCoreProvider.get().getStructureId(),
                CmsContainerpageController.get().getData().getDetailId(),
                null,
                CmsCoreProvider.get().getUri(),
                CmsCoreProvider.get().getLastPageId());
        }

    }

    /** Add menu. */
    private CmsToolbarGalleryMenu m_add;

    /** Add to favorites button. */
    private CmsAddToFavoritesButton m_addToFavorites;

    /** The button for the 'complete galleries' dialog. */
    private CmsToolbarAllGalleriesMenu m_allGalleries;

    /** Clip-board menu. */
    private CmsToolbarClipboardMenu m_clipboard;

    /** The Button for the context menu. */
    private CmsToolbarContextButton m_context;

    /** The available context menu commands. */
    private Map<String, I_CmsContextMenuCommand> m_contextMenuCommands;

    /** Edit button. */
    private CmsToolbarEditButton m_edit;

    /** Button for the elements information. */
    private CmsToolbarElementInfoButton m_elementsInfo;

    /** Info button. */
    private CmsToolbarInfoButton m_info;

    /** Move button. */
    private CmsToolbarMoveButton m_move;

    /** Properties button. */
    private CmsToolbarSettingsButton m_properties;

    /** Publish button. */
    private CmsToolbarPublishButton m_publish;

    /** Remove button. */
    private CmsToolbarRemoveButton m_remove;

    /** Selection button. */
    private CmsToolbarSelectionButton m_selection;

    /** The style variable for the display mode for small elements. */
    private CmsStyleVariable m_smallElementsStyle;

    /** The tool-bar. */
    private CmsToolbar m_toolbar;

    /**
     * Opens a message dialog with the given content.<p>
     *
     * @param title the dialog title
     * @param displayHtmlContent the dialog content
     */
    private static void openMessageDialog(String title, String displayHtmlContent) {

        new CmsErrorDialog(title, displayHtmlContent).center();
    }

    /**
     * Disables the edit functionality.<p>
     *
     * @param reason the text stating the reason why the edit functionality was disabled
     */
    public void disableEditing(String reason) {

        CmsContainerpageController.get().reinitializeButtons();
        m_add.disable(reason);
        m_clipboard.disable(reason);
    }

    /**
     * Deactivates all toolbar buttons.<p>
     */
    public void disableToolbarButtons() {

        for (Widget button : m_toolbar.getAll()) {
            if (button instanceof I_CmsToolbarButton) {
                ((I_CmsToolbarButton)button).setEnabled(false);
            }
        }
        m_toolbar.setVisible(false);
    }

    /**
     * Enables the toolbar buttons.<p>
     *
     * @param hasChanges if the page has changes
     * @param noEditReason the no edit reason
     */
    public void enableToolbarButtons(boolean hasChanges, String noEditReason) {

        for (Widget button : m_toolbar.getAll()) {
            // enable all buttons that are not equal save or reset or the page has changes
            if (button instanceof I_CmsToolbarButton) {
                ((I_CmsToolbarButton)button).setEnabled(true);
            }
        }
        if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(noEditReason)) {
            m_add.disable(noEditReason);
            m_clipboard.disable(noEditReason);
        }

        m_toolbar.setVisible(true);
    }

    /**
     * Returns the add gallery menu.<p>
     *
     * @return the add gallery menu
     */
    public CmsToolbarGalleryMenu getAdd() {

        return m_add;
    }

    /**
     * Returns the clip-board menu.<p>
     *
     * @return the clip-board menu
     */
    public CmsToolbarClipboardMenu getClipboard() {

        return m_clipboard;
    }

    /**
     * Returns the context menu.<p>
     *
     * @return the context menu
     */
    public CmsToolbarContextButton getContext() {

        return m_context;
    }

    /**
     * Returns the available context menu commands as a map by class name.<p>
     *
     * @return the available context menu commands as a map by class name
     */
    public Map<String, I_CmsContextMenuCommand> getContextMenuCommands() {

        if (m_contextMenuCommands == null) {
            I_CmsContextMenuCommandInitializer initializer = GWT.create(I_CmsContextMenuCommandInitializer.class);
            m_contextMenuCommands = initializer.initCommands();
        }
        return m_contextMenuCommands;
    }

    /**
     * Returns the publish.<p>
     *
     * @return the publish
     */
    public CmsToolbarPublishButton getPublish() {

        return m_publish;
    }

    /**
     * Returns the selection button.<p>
     *
     * @return the selection button
     */
    public CmsToolbarSelectionButton getSelection() {

        return m_selection;
    }

    /**
     * Returns the tool-bar widget.<p>
     *
     * @return the tool-bar widget
     */
    public CmsToolbar getToolbar() {

        return m_toolbar;
    }

    /**
     * @see com.google.gwt.core.client.EntryPoint#onModuleLoad()
     */
    @Override
    public void onModuleLoad() {

        super.onModuleLoad();
        CmsRpcContext.get().put(CmsGwtConstants.RpcContext.PAGE_ID, "" + CmsCoreProvider.get().getStructureId());
        CmsBroadcastTimer.start();
        JavaScriptObject window = CmsDomUtil.getWindow();
        CmsDomUtil.setAttribute(window, "__hideEditorCloseButton", "true");

        I_CmsLayoutBundle.INSTANCE.containerpageCss().ensureInjected();
        I_CmsLayoutBundle.INSTANCE.dragdropCss().ensureInjected();

        I_CmsLayoutBundle.INSTANCE.groupcontainerCss().ensureInjected();
        org.opencms.gwt.client.ui.css.I_CmsLayoutBundle.INSTANCE.elementSettingsDialogCss().ensureInjected();
        org.opencms.gwt.client.ui.css.I_CmsLayoutBundle.INSTANCE.listAddCss().ensureInjected();

        final CmsContainerpageController controller = new CmsContainerpageController();
        final CmsContainerpageHandler containerpageHandler = new CmsContainerpageHandler(controller, this);
        CmsContentEditorHandler contentEditorHandler = new CmsContentEditorHandler(containerpageHandler);
        CmsCompositeDNDController dndController = new CmsCompositeDNDController();
        dndController.addController(new CmsContainerpageDNDController(controller));
        //dndController.addController(new CmsImageDndController(controller));
        controller.setDndController(dndController);
        CmsDNDHandler dndHandler = new CmsDNDHandler(dndController);
        dndHandler.setAnimationType(AnimationType.SPECIAL);
        ClickHandler clickHandler = new ClickHandler() {

            /**
             * @see com.google.gwt.event.dom.client.ClickHandler#onClick(com.google.gwt.event.dom.client.ClickEvent)
             */
            public void onClick(ClickEvent event) {

                I_CmsToolbarButton source = (I_CmsToolbarButton)event.getSource();
                source.onToolbarClick();
                if (source instanceof CmsPushButton) {
                    ((CmsPushButton)source).clearHoverState();
                }
            }
        };

        //        m_bodyMarginTop = CmsDomUtil.getCurrentStyleInt(Document.get().getBody(), Style.marginTop);
        m_toolbar = new CmsToolbar();
        m_toolbar.setQuickLaunchHandler(new PageEditorQuickLaunchHandler());
        m_toolbar.getUserInfo().setHandler(containerpageHandler);
        m_toolbar.getQuickLauncher().setHandler(containerpageHandler);
        String title = controller.getData().getAppTitle();
        if (title == null) {
            title = Messages.get().key(Messages.GUI_PAGE_EDITOR_TITLE_0);
        }
        m_toolbar.setAppTitle(title);

        m_publish = new CmsToolbarPublishButton(containerpageHandler);
        m_publish.addClickHandler(clickHandler);
        m_toolbar.addLeft(m_publish);

        m_move = new CmsToolbarMoveButton(containerpageHandler, dndHandler);

        m_edit = new CmsToolbarEditButton(containerpageHandler);

        m_addToFavorites = new CmsAddToFavoritesButton(containerpageHandler);

        m_remove = new CmsToolbarRemoveButton(containerpageHandler);

        m_properties = new CmsToolbarSettingsButton(containerpageHandler);
        m_info = new CmsToolbarInfoButton(containerpageHandler);

        m_clipboard = new CmsToolbarClipboardMenu(containerpageHandler);
        m_clipboard.addClickHandler(clickHandler);
        m_toolbar.addLeft(m_clipboard);

        m_add = new CmsToolbarGalleryMenu(containerpageHandler, dndHandler);
        m_add.addClickHandler(clickHandler);
        m_toolbar.addLeft(m_add);

        m_allGalleries = new CmsToolbarAllGalleriesMenu(containerpageHandler, dndHandler);
        m_allGalleries.addClickHandler(clickHandler);
        m_toolbar.addLeft(m_allGalleries);

        m_elementsInfo = new CmsToolbarElementInfoButton(containerpageHandler, controller);
        m_elementsInfo.addClickHandler(clickHandler);
        m_toolbar.addLeft(m_elementsInfo);

        m_selection = new CmsToolbarSelectionButton(containerpageHandler);
        m_selection.addClickHandler(clickHandler);
        m_toolbar.addLeft(m_selection);

        m_context = new CmsToolbarContextButton(containerpageHandler);
        m_context.addClickHandler(clickHandler);
        m_toolbar.insertRight(m_context, 0);



        CmsToolbarFavLocationButton favLocButton = new CmsToolbarFavLocationButton(containerpageHandler);
        favLocButton.addClickHandler(clickHandler);
        m_toolbar.insertRight(favLocButton, 1);

        DomGlobal.window.addEventListener("pagehide", event -> {
            controller.onWindowClose();
        });

        containerpageHandler.activateSelection();

        RootPanel root = RootPanel.get();
        root.add(m_toolbar);
        CmsContainerpageUtil containerpageUtil = new CmsContainerpageUtil(
            controller,
            m_edit,
            m_move,
            new CmsToolbarListAddButton(containerpageHandler),
            new CmsToolbarListManagerButton(containerpageHandler),
            m_info,
            m_properties,
            m_addToFavorites,
            m_remove);
        CmsEmbeddedDialogFrame.get().preload();
        controller.init(containerpageHandler, dndHandler, contentEditorHandler, containerpageUtil);

        // export open stack trace dialog function
        exportMethods(controller);
    }

    /**
     * Exports the __openMessageDialog and the __reinitializeEditButtons method to the page context.<p>
     *
     * @param controller the controller
     */
    private native void exportMethods(CmsContainerpageController controller) /*-{
		var contr = controller;
		$wnd.opencms = {
			openStacktraceDialog : function(event) {
				event = (event) ? event : ((window.event) ? window.event : "");
				var elem = (event.target) ? event.target : event.srcElement;
				if (elem != null) {
					var children = elem.getElementsByTagName("span");
					if (children.length > 0) {
						var title = children[0].getAttribute("title");
						var content = children[0].innerHTML;
						@org.opencms.ade.containerpage.client.CmsContainerpageEditor::openMessageDialog(Ljava/lang/String;Ljava/lang/String;)(title,content);
					}
				}
			},
			reinitializeEditButtons : function() {
				contr.@org.opencms.ade.containerpage.client.CmsContainerpageController::reinitializeButtons()();
			}
		}
    }-*/;
}
