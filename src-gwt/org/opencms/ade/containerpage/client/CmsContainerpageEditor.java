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

package org.opencms.ade.containerpage.client;

import org.opencms.ade.containerpage.client.ui.CmsAddToFavoritesButton;
import org.opencms.ade.containerpage.client.ui.CmsContainerPageElementPanel;
import org.opencms.ade.containerpage.client.ui.CmsToolbarClipboardMenu;
import org.opencms.ade.containerpage.client.ui.CmsToolbarEditButton;
import org.opencms.ade.containerpage.client.ui.CmsToolbarGalleryMenu;
import org.opencms.ade.containerpage.client.ui.CmsToolbarMoveButton;
import org.opencms.ade.containerpage.client.ui.CmsToolbarPublishButton;
import org.opencms.ade.containerpage.client.ui.CmsToolbarRemoveButton;
import org.opencms.ade.containerpage.client.ui.CmsToolbarResetButton;
import org.opencms.ade.containerpage.client.ui.CmsToolbarSaveButton;
import org.opencms.ade.containerpage.client.ui.CmsToolbarSelectionButton;
import org.opencms.ade.containerpage.client.ui.CmsToolbarSettingsButton;
import org.opencms.ade.containerpage.client.ui.CmsToolbarSitemapButton;
import org.opencms.ade.containerpage.client.ui.css.I_CmsLayoutBundle;
import org.opencms.gwt.client.A_CmsEntryPoint;
import org.opencms.gwt.client.CmsCoreProvider;
import org.opencms.gwt.client.CmsPingTimer;
import org.opencms.gwt.client.dnd.CmsDNDHandler;
import org.opencms.gwt.client.ui.CmsPopup;
import org.opencms.gwt.client.ui.CmsPushButton;
import org.opencms.gwt.client.ui.CmsToolbar;
import org.opencms.gwt.client.ui.CmsToolbarContextButton;
import org.opencms.gwt.client.ui.I_CmsButton.ButtonStyle;
import org.opencms.gwt.client.ui.I_CmsButton.Size;
import org.opencms.gwt.client.ui.I_CmsToolbarButton;
import org.opencms.gwt.client.ui.contextmenu.I_CmsContextMenuCommand;
import org.opencms.gwt.client.ui.contextmenu.I_CmsContextMenuCommandInitializer;
import org.opencms.gwt.client.ui.css.I_CmsImageBundle;
import org.opencms.gwt.client.util.CmsDebugLog;
import org.opencms.gwt.client.util.CmsDomUtil;
import org.opencms.gwt.client.util.CmsStyleVariable;
import org.opencms.util.CmsStringUtil;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.RepeatingCommand;
import com.google.gwt.dom.client.Style.Overflow;
import com.google.gwt.dom.client.Style.Position;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.Widget;

/**
 * The container page editor.<p>
 * 
 * @since 8.0.0
 */
public class CmsContainerpageEditor extends A_CmsEntryPoint {

    /** Margin-top added to the document body element when the tool-bar is shown. */
    //    private int m_bodyMarginTop;

    /** The Z index manager. */
    private static final I_CmsContainerZIndexManager Z_INDEX_MANAGER = GWT.create(I_CmsContainerZIndexManager.class);

    /** Style to toggle toolbar visibility. */
    protected CmsStyleVariable m_toolbarVisibility;

    /** Add menu. */
    private CmsToolbarGalleryMenu m_add;

    /** Add to favorites button. */
    private CmsAddToFavoritesButton m_addToFavorites;

    /** Clip-board menu. */
    private CmsToolbarClipboardMenu m_clipboard;

    /** The Button for the context menu. */
    private CmsToolbarContextButton m_context;

    /** The available context menu commands. */
    private Map<String, I_CmsContextMenuCommand> m_contextMenuCommands;

    /** Edit button. */
    private CmsToolbarEditButton m_edit;

    /** Move button. */
    private CmsToolbarMoveButton m_move;

    /** Properties button. */
    private CmsToolbarSettingsButton m_properties;

    /** Publish button. */
    private CmsToolbarPublishButton m_publish;

    /** Remove button. */
    private CmsToolbarRemoveButton m_remove;

    /** Reset button. */
    private CmsToolbarResetButton m_reset;

    /** Save button. */
    private CmsToolbarSaveButton m_save;

    /** Selection button. */
    private CmsToolbarSelectionButton m_selection;

    /** Sitemap button. */
    private CmsToolbarSitemapButton m_sitemap;

    /** The tool-bar. */
    private CmsToolbar m_toolbar;

    /** 
     * Returns the Z index manager for the container page editor.<p> 
     * 
     * @return the Z index manager
     **/
    public static I_CmsContainerZIndexManager getZIndexManager() {

        return Z_INDEX_MANAGER;
    }

    private static void openMessageDialog(String title, String displayHtmlContent) {

        HTMLPanel content = new HTMLPanel(displayHtmlContent);
        content.getElement().getStyle().setOverflow(Overflow.AUTO);
        content.getElement().getStyle().setPosition(Position.RELATIVE);
        CmsPopup dialog = new CmsPopup(title, content);
        content.getElement().getStyle().setProperty("maxHeight", dialog.getAvailableHeight(100), Unit.PX);
        dialog.setWidth(-1);
        dialog.addDialogClose(null);
        dialog.centerHorizontally(100);
    }

    /**
     * Disables the edit functionality.<p>
     * 
     * @param reason the text stating the reason why the edit functionality was disabled 
     */
    public void disableEditing(String reason) {

        reinitializeButtons();
        m_save.disable(reason);
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
     * Returns the reset button.<p>
     *
     * @return the reset button
     */
    public CmsToolbarResetButton getReset() {

        return m_reset;
    }

    /**
     * Returns the save button.<p>
     *
     * @return the save button
     */
    public CmsToolbarSaveButton getSave() {

        return m_save;
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
     * Returns if the tool-bar is visible.<p>
     * 
     * @return <code>true</code> if the tool-bar is visible
     */
    public boolean isToolbarVisible() {

        return !org.opencms.gwt.client.ui.css.I_CmsLayoutBundle.INSTANCE.toolbarCss().toolbarHide().equals(
            m_toolbarVisibility.getValue());
    }

    /**
     * @see com.google.gwt.core.client.EntryPoint#onModuleLoad()
     */
    @Override
    public void onModuleLoad() {

        super.onModuleLoad();
        if (!checkBuildId("org.opencms.ade.containerpage")) {
            return;
        }
        CmsPingTimer.start();
        JavaScriptObject window = CmsDomUtil.getWindow();
        CmsDomUtil.setAttribute(window, "__hideEditorCloseButton", "true");
        I_CmsLayoutBundle.INSTANCE.containerpageCss().ensureInjected();
        I_CmsLayoutBundle.INSTANCE.dragdropCss().ensureInjected();
        I_CmsLayoutBundle.INSTANCE.groupcontainerCss().ensureInjected();

        final CmsContainerpageController controller = new CmsContainerpageController();
        final CmsContainerpageHandler containerpageHandler = new CmsContainerpageHandler(controller, this);
        CmsContentEditorHandler contentEditorHandler = new CmsContentEditorHandler(containerpageHandler);
        CmsContainerpageDNDController dndController = new CmsContainerpageDNDController(controller);
        CmsDNDHandler dndHandler = new CmsDNDHandler(dndController);

        ClickHandler clickHandler = new ClickHandler() {

            /**
             * @see com.google.gwt.event.dom.client.ClickHandler#onClick(com.google.gwt.event.dom.client.ClickEvent)
             */
            public void onClick(ClickEvent event) {

                I_CmsToolbarButton source = (I_CmsToolbarButton)event.getSource();
                source.onToolbarClick();
            }
        };

        //        m_bodyMarginTop = CmsDomUtil.getCurrentStyleInt(Document.get().getBody(), Style.marginTop);
        m_toolbar = new CmsToolbar();
        RootPanel root = RootPanel.get();
        root.add(m_toolbar);
        CmsPushButton toggleToolbarButton = new CmsPushButton();
        toggleToolbarButton.setButtonStyle(ButtonStyle.TEXT, null);
        toggleToolbarButton.setSize(Size.small);
        toggleToolbarButton.setImageClass(I_CmsImageBundle.INSTANCE.style().opencmsSymbol());
        toggleToolbarButton.removeStyleName(org.opencms.gwt.client.ui.css.I_CmsLayoutBundle.INSTANCE.generalCss().buttonCornerAll());
        toggleToolbarButton.addStyleName(org.opencms.gwt.client.ui.css.I_CmsLayoutBundle.INSTANCE.generalCss().cornerAll());
        root.add(toggleToolbarButton);
        toggleToolbarButton.addClickHandler(new ClickHandler() {

            /**
             * @see com.google.gwt.event.dom.client.ClickHandler#onClick(com.google.gwt.event.dom.client.ClickEvent)
             */
            public void onClick(ClickEvent event) {

                containerpageHandler.toggleToolbar();
            }

        });
        toggleToolbarButton.addStyleName(I_CmsLayoutBundle.INSTANCE.containerpageCss().toolbarToggle());

        m_save = new CmsToolbarSaveButton(containerpageHandler);
        m_save.addClickHandler(clickHandler);
        m_toolbar.addLeft(m_save);

        m_publish = new CmsToolbarPublishButton(containerpageHandler);
        m_publish.addClickHandler(clickHandler);
        m_toolbar.addLeft(m_publish);

        m_move = new CmsToolbarMoveButton(containerpageHandler, dndHandler);

        m_edit = new CmsToolbarEditButton(containerpageHandler);

        m_addToFavorites = new CmsAddToFavoritesButton(containerpageHandler);

        m_remove = new CmsToolbarRemoveButton(containerpageHandler);

        m_properties = new CmsToolbarSettingsButton(containerpageHandler);

        m_clipboard = new CmsToolbarClipboardMenu(containerpageHandler);
        m_clipboard.addClickHandler(clickHandler);
        m_toolbar.addLeft(m_clipboard);

        m_add = new CmsToolbarGalleryMenu(containerpageHandler, dndHandler);
        m_add.addClickHandler(clickHandler);
        m_toolbar.addLeft(m_add);

        m_selection = new CmsToolbarSelectionButton(containerpageHandler);
        m_selection.addClickHandler(clickHandler);
        m_toolbar.addLeft(m_selection);

        m_context = new CmsToolbarContextButton(containerpageHandler);
        m_context.addClickHandler(clickHandler);
        m_toolbar.addRight(m_context);

        m_sitemap = new CmsToolbarSitemapButton(containerpageHandler);
        m_sitemap.addClickHandler(clickHandler);
        m_toolbar.addRight(m_sitemap);

        if (CmsStringUtil.isEmptyOrWhitespaceOnly(controller.getData().getSitemapUri())) {
            m_sitemap.setEnabled(false);
        }

        m_reset = new CmsToolbarResetButton(containerpageHandler);
        m_reset.addClickHandler(clickHandler);
        m_toolbar.addRight(m_reset);
        containerpageHandler.enableSaveReset(false);
        m_toolbarVisibility = new CmsStyleVariable(root);
        m_toolbarVisibility.setValue(org.opencms.gwt.client.ui.css.I_CmsLayoutBundle.INSTANCE.toolbarCss().toolbarHide());
        if (CmsCoreProvider.get().isToolbarVisible()) {
            showToolbar(true);
            containerpageHandler.activateSelection();
        }

        CmsContainerpageUtil containerpageUtil = new CmsContainerpageUtil(
            controller,
            m_selection,
            m_move,
            m_edit,
            m_remove,
            m_properties,
            m_addToFavorites);
        controller.init(containerpageHandler, dndHandler, contentEditorHandler, containerpageUtil);

        // export open stack trace dialog function
        exportStacktraceDialogMethod();
        Scheduler.get().scheduleFixedDelay(new RepeatingCommand() {

            public boolean execute() {

                updateAllElements();
                return true;
            }
        }, 1000);
    }

    /**
     * Reinitializes the buttons in the container element menus.<p>
     */
    public void reinitializeButtons() {

        List<CmsContainerPageElementPanel> elemWidgets = getAllContainerPageElements();
        for (CmsContainerPageElementPanel elemWidget : elemWidgets) {
            CmsContainerpageController.get().getContainerpageUtil().addOptionBar(elemWidget);
        }
    }

    /**
     * Shows the tool-bar.<p>
     * 
     * @param show if <code>true</code> the tool-bar will be shown
     */
    public void showToolbar(boolean show) {

        CmsToolbar.showToolbar(m_toolbar, show, m_toolbarVisibility);
    }

    /**
     * Perform layout corrections for the current container elements.<p>
     */
    public void updateAllElements() {

        List<CmsContainerPageElementPanel> pageElements = getAllContainerPageElements();
        for (CmsContainerPageElementPanel pageElement : pageElements) {
            pageElement.update();
        }
    }

    /**
     * Helper method to get all current container page elements.<p>
     * 
     * @return the list of current container page elements 
     */
    protected List<CmsContainerPageElementPanel> getAllContainerPageElements() {

        List<CmsContainerPageElementPanel> elemWidgets = new ArrayList<CmsContainerPageElementPanel>();
        for (Entry<String, org.opencms.ade.containerpage.client.ui.CmsContainerPageContainer> entry : CmsContainerpageController.get().getContainerTargets().entrySet()) {
            Iterator<Widget> elIt = entry.getValue().iterator();
            while (elIt.hasNext()) {
                try {
                    org.opencms.ade.containerpage.client.ui.CmsContainerPageElementPanel elementWidget = (org.opencms.ade.containerpage.client.ui.CmsContainerPageElementPanel)elIt.next();
                    elemWidgets.add(elementWidget);
                } catch (ClassCastException e) {
                    // no proper container element, skip it (this should never happen!)
                    CmsDebugLog.getInstance().printLine("WARNING: there is an inappropriate element within a container");
                }
            }
        }
        return elemWidgets;
    }

    private native void exportStacktraceDialogMethod() /*-{
        $wnd.__openStacktraceDialog = function(event) {
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
        }
    }-*/;

}
