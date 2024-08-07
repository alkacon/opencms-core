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

package org.opencms.gwt.client.ui.css;

import com.google.gwt.core.client.GWT;
import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.resources.client.CssResource.Import;
import com.google.gwt.resources.client.CssResource.ImportedWithPrefix;
import com.google.gwt.resources.client.CssResource.Shared;

/**
 * Resource bundle to access CSS and image resources.
 *
 * @since 8.0.0
 */
public interface I_CmsLayoutBundle extends ClientBundle {

    /** The context menu CSS classes. */
    public interface I_CmsAvailabilityCss extends CssResource {

        /**
         * Access method.<p>
         *
         * @return the CSS class name
         */
        String checkBox();

        /**
         * Access method.<p>
         *
         * @return the CSS class name
         */
        String dateBox();

        /**
         * Access method.<p>
         *
         * @return the CSS class name
         */
        String fieldsetSpacer();

        /**
         * Access method.<p>
         *
         * @return the CSS class name
         */
        String inlineBlock();

        /**
         * Access method.<p>
         *
         * @return the CSS class name
         */
        String inputCombination();

        /**
         * Access method.<p>
         *
         * @return the CSS class name
         */
        String labelColumn();

        /**
         * Access method.<p>
         *
         * @return the CSS class name
         */
        String principalIcon();

        /**
         * Access method.<p>
         *
         * @return the CSS class name
         */
        String responsabilityLabel();
    }

    /** Button CSS. */
    @Shared
    @ImportedWithPrefix("buttons")
    interface I_CmsButtonCss extends I_CmsStateCss {

        /**
         *  Access method.<p>
         *
         * @return the CSS class name
         */
        String blue();

        /**
         *  Access method.<p>
         *
         * @return the CSS class name
         */
        String cmsButtonBig();

        /**
         *  Access method.<p>
         *
         * @return the CSS class name
         */
        String cmsButtonMedium();

        /**
         *  Access method.<p>
         *
         * @return the CSS class name
         */
        String cmsButtonSmall();

        /**
         *  Access method.<p>
         *
         * @return the CSS class name
         */
        String cmsFontIconButton();

        /**
         *  Access method.<p>
         *
         * @return the CSS class name
         */
        String cmsMenuButton();

        /**
         * Access method.<p>
         *
         * @return the CSS class name
         */
        String cmsMinWidth();

        /**
         * Access method.<p>
         *
         * @return the CSS class name
         */
        String cmsPushButton();

        /**
         *  Access method.<p>
         *
         * @return the CSS class name
         */
        String cmsTextButton();

        /**
         *  Access method.<p>
         *
         * @return the CSS class name
         */
        String cmsTransparentButton();

        /**
         *  Access method.<p>
         *
         * @return the CSS class name
         */
        String gray();

        /**
         *  Access method.<p>
         *
         * @return the CSS class name
         */
        String green();

        /**
         *  Access method.<p>
         *
         * @return the CSS class name
         */
        String helpIcon();

        /**
         *  Access method.<p>
         *
         * @return the CSS class name
         */
        String hoverBlack();

        /**
         *  Access method.<p>
         *
         * @return the CSS class name
         */
        String red();

        /**
         * Access method.<p>
         *
         * @return the CSS class name
         */
        String resizeButton();

        /**
         *  Access method.<p>
         *
         * @return the CSS class name
         */
        String spacerLeft();

        /**
         *  Access method.<p>
         *
         * @return the CSS class name
         */
        String spacerRight();
    }

    /** The category CSS. */
    interface I_CmsCategoryDialogCss extends CssResource {

        /** Access method.<p>
         *
         * @return the CSS class name
         */
        String criteriaList();

        /** Access method.<p>
         *
         * @return the CSS class name
         */
        String infoLabel();

        /** Access method.<p>
         *
         * @return the CSS class name
         */
        String tabOptions();
    }

    /** THe color selector CSS classes.  */
    public interface I_CmsColorSelector extends CssResource {

        /**
         * Css class reader.<p>
         *
         * @return the css class
         */
        String colorSelectorWidget();

        /**
         * Css class reader.<p>
         *
         * @return the css class
         */
        String sliderMap();

        /**
         * Css class reader.<p>
         *
         * @return the css class
         */
        String sliderMapOverlay();

        /**
         * Css class reader.<p>
         *
         * @return the css class
         */
        String sliderMapSlider();

        /**
         * Css class reader.<p>
         *
         * @return the css class
         */
        String sliderMapUnderlay();

        /**
         * Css class reader.<p>
         *
         * @return the css class
         */
        String tableField();

    }

    /** The content editor dialog CSS. */
    interface I_CmsContentEditorCss extends CssResource {

        /** Access method.<p>
         *
         * @return the CSS class name
         */
        String contentEditor();

        /**
         * Access method.<p>
         *
         * @return the CSS class name
         */
        String dataViewItem();
    }

    /** The context menu CSS classes. */
    public interface I_CmsContextmenuCss extends I_CmsPopupCss, I_CmsContextmenuItemCss {

        /**
         * Access method.<p>
         *
         * @return the CSS class name
         */
        String cmsMenuBar();

        /**
         * Access method.<p>
         *
         * @return the CSS class name
         */
        String iconBox();

        /**
         * Access method.<p>
         *
         * @return the CSS class name
         */
        String menuInfoLabel();

        /**
         * Access method.<p>
         *
         * @return the CSS class name
         */
        String menuItemSeparator();

        /**
         * Access method.<p>
         *
         * @return the CSS class name
         */
        String menuPanel();

    }

    /** The context menu item CSS classes. */
    @Shared
    public interface I_CmsContextmenuItemCss extends CssResource {

        /**
         * Access method.<p>
         *
         * @return the CSS class name
         */
        String arrow();

        /**
         * Access method.<p>
         *
         * @return the CSS class name
         */
        String cmsMenuItem();

        /**
         * Access method.<p>
         *
         * @return the CSS class name
         */
        String disabled();

        /**
         * Access method.<p>
         *
         * @return the CSS class name
         */
        String label();

        /**
         * Access method.<p>
         *
         * @return the CSS class name
         */
        String selected();
    }

    /** DateBox css. */
    public interface I_CmsDateBoxCss extends CssResource {

        /**
         * Access method.<p>
         *
         * @return the CSS class name
         */
        String ampm();

        /**
         * Access method.<p>
         *
         * @return the CSS class name
         */
        String dateTime();

        /**
         * Access method.<p>
         *
         * @return the CSS class name
         */
        String time();
    }

    /** Dialog CSS. */
    @Shared
    @ImportedWithPrefix("dialog")
    interface I_CmsDialogCss extends I_CmsPopupCss {

        /**
         * Access method.<p>
         *
         * @return the CSS class name
         */
        String alertBottomContent();

        /**
         * Access method.<p>
         *
         * @return the CSS class name
         */
        String alertMainContent();

        /**
         * Access method.<p>
         *
         * @return the CSS class name
         */
        String alertTopContent();

        /**
         * Access method.<p>
         *
         * @return the CSS class name
         */
        String borderPadding();

        /**
         * Access method.<p>
         *
         * @return the CSS class name
         */
        String caption();

        /**
         * Access method.<p>
         *
         * @return the CSS class name
         */
        String closePopup();

        /**
         * Access method.<p>
         *
         * @return the CSS class name
         */
        String closePopupImage();

        /**
         * Access method.<p>
         *
         * @return the CSS class name
         */
        String contentPadding();

        /**
         * Access method.<p>
         *
         * @return the CSS class name
         */
        String contentSpacer();

        /**
         * Access method.<p>
         *
         * @return the CSS class name
         */
        String contextMenu();

        /**
         * Access method.<p>
         *
         * @return the CSS class name
         */
        String dragging();

        /**
         * Access method.<p>
         *
         * @return the CSS class name
         */
        String dragOverlay();

        /**
         * Access method.<p>
         *
         * @return the CSS class name
         */
        String embeddedDialogFrame();

        /**
         * Access method.<p>
         *
         * @return the CSS class name
         */
        String embeddedDialogFrameHidden();

        /**
         * Access method.<p>
         *
         * @return the CSS class name
         */
        String frameDialog();

        /**
         * Access method.<p>
         *
         * @return the CSS class name
         */
        String hideButtonPanel();

        /**
         * Access method.<p>
         *
         * @return the CSS class name
         */
        String hideCaption();

        /**
         * Access method.<p>
         *
         * @return the CSS class name
         */
        String invertClose();

        /**
         * Access method.<p>
         *
         * @return the CSS class name
         */
        String leftButtonBox();

        /**
         * Access method.<p>
         *
         * @return the CSS class name
         */
        String logReportScrollPanel();

        /**
         * Access method.<p>
         *
         * @return the CSS class name
         */
        String menuArrowBottom();

        /**
         * Access method.<p>
         *
         * @return the CSS class name
         */
        String menuArrowTop();

        /**
         * Access method.<p>
         *
         * @return the CSS class name
         */
        String modelSelectList();

        /**
         * Access method.<p>
         *
         * @return the CSS class name
         */
        String popupButtonPanel();

        /**
         * Access method.<p>
         *
         * @return the CSS class name
         */
        String popupHead();

        /**
         * Access method.<p>
         *
         * @return the CSS class name
         */
        String popupMainContent();

        /**
         * Access method.<p>
         *
         * @return the CSS class name
         */
        String popupOverlay();
    }

    /** The drag and drop CSS used by the base module. */
    @Shared
    interface I_CmsDragCss extends CssResource {

        /**
         * Access method.<p>
         *
         * @return the CSS class name
         */
        String dragPlaceholder();

        /**
         * Access method.<p>
         *
         * @return the CSS class name
         */
        String dragStarted();

        /**
         * Access method.<p>
         *
         * @return the CSS class name
         */
        String fullWindowDrag();
    }

    /** The error dialog CSS classes. */
    public interface I_CmsErrorDialogCss extends CssResource {

        /**
         * Access method.<p>
         *
         * @return the CSS class name
         */
        String details();

        /**
         * Access method.<p>
         *
         * @return the CSS class name
         */
        String errorIcon();

        /**
         * Access method.<p>
         *
         * @return the CSS class name
         */
        String message();
    }

    /** The context menu CSS classes. */
    @ImportedWithPrefix("fieldset")
    public interface I_CmsFieldsetCss extends CssResource {

        /**
         * Access method.<p>
         *
         * @return the CSS class name
         */
        String content();

        /**
         * Access method.<p>
         *
         * @return the CSS class name
         */
        String fieldsetInvisible();

        /**
         * Access method.<p>
         *
         * @return the CSS class name
         */
        String fieldsetVisible();

        /**
         * Access method.<p>
         *
         * @return the CSS class name
         */
        String image();

        /**
         * Access method.<p>
         *
         * @return the CSS class name
         */
        String legend();
    }

    /**
     * CSS for the filter select box.
     */
    interface I_CmsFilterSelectCss extends CssResource {

        /**
         * Access method.<p>
         *
         * @return the CSS class name
         */
        String filterInput();

        /**
         * Access method.<p>
         *
         * @return the CSS class name
         */
        String filterSelect();

    }

    /** General CSS, used for general re-occurring styles. */
    @Shared
    interface I_CmsGeneralCss extends CssResource {

        /**
         * Access method.<p>
         *
         * @return the CSS class name
         */
        String border();

        /**
         * Access method.<p>
         *
         * @return the CSS class name
         */
        String buttonCornerAll();

        /**
         * Access method.<p>
         *
         * @return the CSS class name
         */
        String cellpadding();

        /**
         * Access method.<p>
         *
         * @return the CSS class name
         */
        String clearAll();

        /**
         * Access method.<p>
         *
         * @return the CSS class name
         */
        String clearFix();

        /**
         * Access method.<p>
         *
         * @return the CSS class name
         */
        String clearStyles();

        /**
         * Access method.<p>
         *
         * @return the CSS class name
         */
        String cornerAll();

        /**
         * Access method.<p>
         *
         * @return the CSS class name
         */
        String cornerBottom();

        /**
         * Access method.<p>
         *
         * @return the CSS class name
         */
        String cornerTop();

        /**
         * Access method.<p>
         *
         * @return the CSS class name
         */
        String disablingOverlay();

        /**
         * Access method.<p>
         *
         * @return the CSS class name
         */
        String header();

        /**
         * Access method.<p>
         *
         * @return the CSS class name
         */
        String headerButtons();

        /**
         * Access method.<p>
         *
         * @return the CSS class name
         */
        String hideOverlay();

        /**
         * Access method.<p>
         *
         * @return the CSS class name
         */
        String inlineBlock();

        /**
         * Access method.<p>
         *
         * @return the CSS class name
         */
        String multiLineLabel();

        /**
         * Access method.<p>
         *
         * @return the CSS class name
         */
        String opencms();

        /**
         * Access method.<p>
         *
         * @return the CSS class name
         */
        String shadow();

        /**
         * Access method.<p>
         *
         * @return the CSS class name
         */
        String simpleFormInputBox();

        /**
         * Access method.<p>
         *
         * @return the CSS class name
         */
        String simpleFormLabel();

        /**
         * Access method.<p>
         *
         * @return the CSS class name
         */
        String simpleFormRow();

        /**
         * Access method.<p>
         *
         * @return the CSS class name
         */
        String textBig();

        /**
         * Access method.<p>
         *
         * @return the CSS class name
         */
        String textMedium();

        /**
         * Access method.<p>
         *
         * @return the CSS class name
         */
        String textSmall();

        /**
         * Access method.<p>
         *
         * @return the CSS class name
         */
        String toolTip();

        /**
         * Access method.<p>
         *
         * @return the CSS class name
         */
        String truncatingLabel();
    }

    /** The global widget CSS class. */
    @Shared
    public interface I_CmsGlobalWidgetCss extends I_CmsOpenerHoverCss {

        /**
         * Css class reader.<p>
         *
         * @return the css class
         */
        String selectBoxPopup();

        /**
         * Css class reader.<p>
         *
         * @return the css class
         */
        String selectBoxSelected();

        /**
         * Css class reader.<p>
         *
         * @return the css class
         */
        String textAreaBox();

        /**
         * Css class reader.<p>
         *
         * @return the css class
         */
        String textAreaBoxPanel();
    }

    /** Header CSS. */
    interface I_CmsHeaderCss extends CssResource {

        /**
         * Access method.<p>
         *
         * @return the CSS class name
         */
        String h1();

        /**
         * Access method.<p>
         *
         * @return the CSS class name
         */
        String h2();

        /**
         * Access method.<p>
         *
         * @return the CSS class name
         */
        String h3();

        /**
         * Access method.<p>
         *
         * @return the CSS class name
         */
        String h4();

        /**
         * Access method.<p>
         *
         * @return the CSS class name
         */
        String h5();

        /**
         * Access method.<p>
         *
         * @return the CSS class name
         */
        String h6();
    }

    /** Highlighting CSS, used within the {@link org.opencms.gwt.client.ui.CmsHighlightingBorder} widget. */
    interface I_CmsHighlightCss extends CssResource {

        /**
         * Access method.<p>
         *
         * @return the CSS class name
         */
        String animated();

        /**
         * Access method.<p>
         *
         * @return the CSS class name
         */
        String borderBottom();

        /**
         * Access method.<p>
         *
         * @return the CSS class name
         */
        String borderLeft();

        /**
         * Access method.<p>
         *
         * @return the CSS class name
         */
        String borderRight();

        /**
         * Access method.<p>
         *
         * @return the CSS class name
         */
        String borderTop();

        /**
         * Access method.<p>
         *
         * @return the CSS class name
         */
        String colorBlue();

        /**
         * Access method.<p>
         *
         * @return the CSS class name
         */
        String colorGrey();

        /**
         * Access method.<p>
         *
         * @return the CSS class name
         */
        String colorRed();

        /**
         * Access method.<p>
         *
         * @return the CSS class name
         */
        String colorSolidGrey();

        /**
         * Access method.<p>
         *
         * @return the CSS class name
         */
        String highlightBox();

        /**
         * Access method.<p>
         *
         * @return the CSS class name
         */
        String midpointSeparator();
    }

    /** Link warning panel CSS. */
    interface I_CmsLinkWarningCss extends CssResource {

        /**
         * Access method.<p>
         *
         * @return the CSS class name
         */
        String deletedEntryLabel();
    }

    /**
     * CSS for the list item creation dialog.
     */
    interface I_CmsListAddCss extends CssResource {

        /**
         * Access method.<p>
         *
         * @return the CSS class name
         */
        String labelContainer();

        /**
         * Access method.<p>
         *
         * @return the CSS class name
         */
        String optionContainer();
    }

    /** List item CSS. */
    @Shared
    @ImportedWithPrefix("liw")
    interface I_CmsListItemWidgetCss extends I_CmsStateCss {

        /**
         * Access method.<p>
         *
         * @return the CSS class name
         */
        String buttonPanel();

        /**
         * Access method.<p>
         *
         * @return the CSS class name
         */
        String changed();

        /**
         * Access method.<p>
         *
         * @return the CSS class name
         */
        String copyModel();

        /**
         * Access method.<p>
         *
         * @return the CSS class name
         */
        String disabledItem();

        /**
         * Access method.<p>
         *
         * @return the CSS class name
         */
        String dragging();

        /** Access method.<p>
         *
         * @return the CSS class name
         */
        String expired();

        /**
         * CSS class accessor.<p>
         *
         * @return a CSS class
         **/
        String export();

        /**
         * CSS class accessor.<p>
         *
         * @return a CSS class
         **/
        @ClassName("oc-inline-editable")
        String inlineEditable();

        /**
         * Access method.<p>
         *
         * @return the CSS class name
         */
        String itemActive();

        /**
         * Access method.<p>
         *
         * @return the CSS class name
         */
        String itemAdditional();

        /**
         * Access method.<p>
         *
         * @return the CSS class name
         */
        String itemAdditionalTitle();

        /**
         * Access method.<p>
         *
         * @return the CSS class name
         */
        String itemAdditionalValue();

        /**
         * Access method.<p>
         *
         * @return the CSS class name
         */
        String itemBlue();

        /**
         * Access method.<p>
         *
         * @return the CSS class name
         */
        String itemContainer();

        /**
         * Access method.<p>
         *
         * @return the CSS class name
         */
        String itemContent();

        /**
         * Access method.<p>
         *
         * @return the CSS class name
         */
        String itemIcon();

        /**
         * Access method.<p>
         *
         * @return the CSS class name
         */
        String itemInfoRow();

        /**
         * Access method.<p>
         *
         * @return the CSS class name
         */
        String itemRed();

        /**
         * Access method.<p>
         *
         * @return the CSS class name
         */
        String itemSubtitle();

        /**
         * Access method.<p>
         *
         * @return the CSS class name
         */
        String itemTitle();

        /**
         * Access method.<p>
         *
         * @return the CSS class name
         */
        String itemYellow();

        /**
         * CSS class accessor.<p>
         *
         * @return a CSS class
         **/
        String lockClosed();

        /**
         * CSS class accessor.<p>
         *
         * @return a CSS class
         **/
        String lockIcon();

        /**
         * CSS class accessor.<p>
         *
         * @return a CSS class
         **/
        String lockOpen();

        /**
         * CSS class accessor.<p>
         *
         * @return a CSS class
         **/
        String lockSharedClosed();

        /**
         * CSS class accessor.<p>
         *
         * @return a CSS class
         **/
        String lockSharedOpen();

        /**
         * Access method.<p>
         *
         * @return the CSS class name
         */
        String open();

        /**
         * Access method.<p>
         *
         * @return the CSS class name
         */
        String pageDetailType();

        /**
         * Access method.<p>
         *
         * @return the CSS class name
         */
        String permaVisible();

        /**
         * CSS class accessor.<p>
         *
         * @return a CSS class
         **/
        String secure();

        /**
         * CSS class accessor.<p>
         *
         * @return a CSS class
         **/
        String stateIcon();

        /**
         * CSS class accessor.<p>
         *
         * @return a CSS class
         **/
        String titleDeleted();

        /**
         * CSS class accessor.<p>
         *
         * @return a CSS class
         **/
        String titleInput();

        /**
         * Access method.<p>
         *
         * @return the CSS class name
         */
        String titleRow();

        /**
         * Access method.<p>
         *
         * @return the CSS class name
         */
        String topRightIcon();

        /**
         * Access method.<p>
         *
         * @return the CSS class name
         */
        String unselectable();

    }

    /** ListTree CSS. */
    interface I_CmsListTreeCss extends CssResource {

        /**
         * Access method.<p>
         *
         * @return the CSS class name
         */
        String bigIndentation();

        /**
         * Access method.<p>
         *
         * @return the CSS class name
         */
        String list();

        /**
         * Access method.<p>
         *
         * @return the CSS class name
         */
        String listScrollable();

        /**
         * Access method.<p>
         *
         * @return the CSS class name
         */
        String listTreeItem();

        /**
         * Access method.<p>
         *
         * @return the CSS class name
         */
        String listTreeItemChildren();

        /**
         * Access method.<p>
         *
         * @return the CSS class name
         */
        String listTreeItemClosed();

        /**
         * Access method.<p>
         *
         * @return the CSS class name
         */
        String listTreeItemContent();

        /**
         * Access method.<p>
         *
         * @return the CSS class name
         */
        String listTreeItemInternal();

        /**
         * Access method.<p>
         *
         * @return the CSS class name
         */
        String listTreeItemLeaf();

        /**
         * Access method.<p>
         *
         * @return the CSS class name
         */
        String listTreeItemNoOpeners();

        /**
         * Access method.<p>
         *
         * @return the CSS class name
         */
        String listTreeItemOpen();

        /**
         * Access method.<p>
         *
         * @return the CSS class name
         */
        String listTreeItemOpener();
    }

    /** Location picker CSS. */
    public interface I_CmsLocationPicker extends I_CmsLocationPickerBase {

        /**
         * Access method.<p>
         *
         * @return the CSS class name
         */
        String buttonBar();

        /**
         * Access method.<p>
         *
         * @return the CSS class name
         */
        String fader();

        /**
         * Access method.<p>
         *
         * @return the CSS class name
         */
        String hasPreview();

        /**
         * Access method.<p>
         *
         * @return the CSS class name
         */
        String inlineField();

        /**
         * Access method.<p>
         *
         * @return the CSS class name
         */
        String inputContainer();

        /**
         * Access method.<p>
         *
         * @return the CSS class name
         */
        String mapCanvas();

        /**
         * Access method.<p>
         *
         * @return the CSS class name
         */
        String opener();
    }

    /** Base location picker CSS. */
    @Shared
    public interface I_CmsLocationPickerBase extends CssResource {

        /**
         * Access method.<p>
         *
         * @return the CSS class name
         */
        String displayBox();

        /**
         * Access method.<p>
         *
         * @return the CSS class name
         */
        String locationField();

        /**
         * Access method.<p>
         *
         * @return the CSS class name
         */
        String locationFields();

        /**
         * Access method.<p>
         *
         * @return the CSS class name
         */
        String locationInfo();

        /**
         * Access method.<p>
         *
         * @return the CSS class name
         */
        String locationMainPanel();

        /**
         * Access method.<p>
         *
         * @return the CSS class name
         */
        String mapPreview();
    }

    /** The menu button CSS. */
    public interface I_CmsMenuButton extends CssResource {

        /**
         *  Access method.<p>
         *
         * @return the CSS class name
         */
        String button();

        /**
         *  Access method.<p>
         *
         * @return the CSS class name
         */
        String menu();
    }

    /** Notification CSS. */
    @Shared
    interface I_CmsNotificationCss extends I_CmsPopupCss {

        /**
         * Access method.<p>
         *
         * @return the CSS class name
         */
        String blocking();

        /**
         * Access method.<p>
         *
         * @return the CSS class name
         */
        String busy();

        /**
         * Access method.<p>
         *
         * @return the CSS class name
         */
        String closeButton();

        /**
         * Access method.<p>
         *
         * @return the CSS class name
         */
        String loadingAnimation();

        /**
         * Access method.<p>
         *
         * @return the CSS class name
         */
        String messageContent();

        /**
         * Access method.<p>
         *
         * @return the CSS class name
         */
        String messageHead();

        /**
         * Access method.<p>
         *
         * @return the CSS class name
         */
        String messagesPanel();

        /**
         * Access method.<p>
         *
         * @return the CSS class name
         */
        String messageText();

        /**
         * Access method.<p>
         *
         * @return the CSS class name
         */
        String messageTime();

        /**
         * Access method.<p>
         *
         * @return the CSS class name
         */
        String messageWrap();

        /**
         * Access method.<p>
         *
         * @return the CSS class name
         */
        String notificationContainer();

        /**
         * Access method.<p>
         *
         * @return the CSS class name
         */
        String notificationError();

        /**
         * Access method.<p>
         *
         * @return the CSS class name
         */
        String notificationMessage();

        /**
         * Access method.<p>
         *
         * @return the CSS class name
         */
        String notificationNormal();

        /**
         * Access method.<p>
         *
         * @return the CSS class name
         */
        String notificationOverlay();

        /**
         * Access method.<p>
         *
         * @return the CSS class name
         */
        String notificationWarning();
    }

    /** The opener hover CSS class. */
    @Shared
    public interface I_CmsOpenerHoverCss extends CssResource {

        /**
         * Css class reader.<p>
         *
         * @return the css class
         */
        String openerHover();

        /**
         * Css class reader.<p>
         *
         * @return the css class
         */
        String openerNoHover();
    }

    /** Page CSS. */
    interface I_CmsPageCss extends CssResource {

        /**
         * Access method.<p>
         *
         * @return the CSS class name
         */
        String page();
    }

    /** Basic popup CSS classes. */
    @Shared
    public interface I_CmsPopupCss extends CssResource {

        /**
         * Access method.<p>
         *
         * @return the CSS class name
         */
        String popup();

        /**
         * Access method.<p>
         *
         * @return the CSS class name
         */
        String popupContent();
    }

    /** The context menu CSS classes. */
    public interface I_CmsProgressBarCss extends CssResource {

        /**
         * Access method.<p>
         *
         * @return the CSS class name
         */
        String colorComplete();

        /**
         * Access method.<p>
         *
         * @return the CSS class name
         */
        String colorIncomplete();

        /**
         * Access method.<p>
         *
         * @return the CSS class name
         */
        String meterText();

        /**
         * Access method.<p>
         *
         * @return the CSS class name
         */
        String meterValue();

        /**
         * Access method.<p>
         *
         * @return the CSS class name
         */
        String meterWrap();
    }

    /** The context menu CSS classes. */
    public interface I_CmsResourceStateCss extends CssResource {

        /**
         * Access method.<p>
         *
         * @return the CSS class name
         */
        String noState();

        /**
         * Access method.<p>
         *
         * @return the CSS class name
         */
        String stateChanged();

        /**
         * Access method.<p>
         *
         * @return the CSS class name
         */
        String stateDeleted();

        /**
         * Access method.<p>
         *
         * @return the CSS class name
         */
        String stateNew();
    }

    /** The scroll bar CSS classes. */
    public interface I_CmsScrollBarCss extends I_CmsScrollPanel {

        /**
         * Access method.<p>
         *
         * @return the CSS class name
         */
        String scrollBar();

        /**
         * Access method.<p>
         *
         * @return the CSS class name
         */
        String scrollbarLayer();

        /**
         * Access method.<p>
         *
         * @return the CSS class name
         */
        String scrollKnob();
    }

    /** The scroll bar CSS classes. */
    @Shared
    public interface I_CmsScrollPanel extends CssResource {

        /**
         * Access method.<p>
         *
         * @return the CSS class name
         */
        String allwaysShowBars();

        /**
         * Access method.<p>
         *
         * @return the CSS class name
         */
        String hiddenSize();

        /**
         * Access method.<p>
         *
         * @return the CSS class name
         */
        String scrollable();

        /**
         * Access method.<p>
         *
         * @return the CSS class name
         */
        String scrollContainer();

        /**
         * Access method.<p>
         *
         * @return the CSS class name
         */
        String scrollPanel();

        /**
         * Access method.<p>
         *
         * @return the CSS class name
         */
        String showBars();
    }

    /** The select area CSS. */
    public interface I_CmsSelectArea extends CssResource {

        /**
         *  Access method.<p>
         *
         * @return the CSS class name
         */
        String main();

        /**
         *  Access method.<p>
         *
         * @return the CSS class name
         */
        String marker();

        /**
         *  Access method.<p>
         *
         * @return the CSS class name
         */
        String markerBlackBorder();

        /**
         *  Access method.<p>
         *
         * @return the CSS class name
         */
        String markerBorder();

        /**
         *  Access method.<p>
         *
         * @return the CSS class name
         */
        String markerWhiteBorder();

        /**
         *  Access method.<p>
         *
         * @return the CSS class name
         */
        String overlay();

        /**
         *  Access method.<p>
         *
         * @return the CSS class name
         */
        String showSelect();
    }

    /** The single line list item CSS. */
    public interface I_CmsSingleLineItem extends I_CmsFloatDecoratedPanelCss {

        /**
         * Access method.<p>
         *
         * @return the CSS class name
         */
        String itemFace();

        /**
         * Access method.<p>
         *
         * @return the CSS class name
         */
        String singleLineItem();
    }

    /** General CSS. */
    @Shared
    interface I_CmsStateCss extends CssResource {

        /**
         * Access method.<p>
         *
         * @return the CSS class name
         */
        String cmsHovering();

        /**
         * Access method.<p>
         *
         * @return the CSS class name
         */
        String cmsState();

        /**
         * Access method.<p>
         *
         * @return the CSS class name
         */
        @ClassName("cmsState-down")
        String cmsStateDown();

        /**
         * Access method.<p>
         *
         * @return the CSS class name
         */
        @ClassName("cmsState-down-disabled")
        String cmsStateDownDisabled();

        /**
         * Access method.<p>
         *
         * @return the CSS class name
         */
        @ClassName("cmsState-down-hovering")
        String cmsStateDownHovering();

        /**
         * Access method.<p>
         *
         * @return the CSS class name
         */
        @ClassName("cmsState-up")
        String cmsStateup();

        /**
         * Access method.<p>
         *
         * @return the CSS class name
         */
        @ClassName("cmsState-up-disabled")
        String cmsStateUpDisabled();

        /**
         * Access method.<p>
         *
         * @return the CSS class name
         */
        @ClassName("cmsState-up-hovering")
        String cmsStateUpHovering();

    }

    /** Tabbed panel css. */
    interface I_CmsTabbedPanelCss extends CssResource {

        /**
         * Access method.<p>
         *
         * @return the CSS class name
         */
        String black();

        /**
         * Access method.<p>
         *
         * @return the CSS class name
         */
        String borderAll();

        /**
         * Access method.<p>
         *
         * @return the CSS class name
         */
        String buttonTabs();

        /**
         * Access method.<p>
         *
         * @return the CSS class name
         */
        String classicTabs();

        /**
         * Access method.<p>
         *
         * @return the CSS class name
         */
        @ClassName("internal-tabbed")
        String cmsInternalTab();

        /**
         * Access method.<p>
         *
         * @return the CSS class name
         */
        @ClassName("gwt-TabLayoutPanel")
        String cmsTabLayoutPanel();

        /**
         * Access method.<p>
         *
         * @return the CSS class name
         */
        @ClassName("gwt-TabLayoutPanelContent")
        String cmsTabLayoutPanelContent();

        /**
         * Access method.<p>
         *
         * @return the CSS class name
         */
        @ClassName("gwt-TabLayoutPanelTab")
        String cmsTabLayoutPanelTab();

        /**
         * Access method.<p>
         *
         * @return the CSS class name
         */
        @ClassName("gwt-TabLayoutPanelTabBar")
        String cmsTabLayoutPanelTabBar();

        /**
         * Access method.<p>
         *
         * @return the CSS class name
         */
        @ClassName("gwt-TabLayoutPanelTabs")
        String cmsTabLayoutPanelTabs();

        /**
         * Access method.<p>
         *
         * @return the CSS class name
         */
        @ClassName("gwt-TabLayoutPanelTab-selected")
        String cmsTabLayoutPanelTabSelected();

        /**
         * Access method.<p>
         *
         * @return the CSS class name
         */
        String cornerLeft();

        /**
         * Access method.<p>
         *
         * @return the CSS class name
         */
        String cornerRight();

        /**
         * Access method.<p>
         *
         * @return the CSS class name
         */
        String tabDisabled();

        /**
         * Access method.<p>
         *
         * @return the CSS class name
         */
        String tabLeftMargin();

        /**
         * Access method.<p>
         *
         * @return the CSS class name
         */
        String tabPanel();

        /**
         * Access method.<p>
         *
         * @return the CSS class name
         */
        String wrapTabs();
    }

    /** Toolbar CSS. */
    @Shared
    interface I_CmsToolbarCss extends CssResource {

        /**
         * Access method.<p>
         *
         * @return the CSS class name
         */
        String notification();

        /**
         * Access method.<p>
         *
         * @return the CSS class name
         */
        @ClassName("v-button")
        String quickButton();

        /**
         * Access method.<p>
         *
         * @return the CSS class name
         */
        @ClassName("v-disabled")
        String quickButtonDeactivated();

        /**
         * Access method.<p>
         *
         * @return the CSS class name
         */
        @ClassName("v-button-wrap")
        String quickButtonWrap();

        /**
         * Access method.<p>
         *
         * @return the CSS class name
         */
        String quickLaunchContainer();

        /**
         * Access method.<p>
         *
         * @return the CSS class name
         */
        String simpleToolbarShow();

        /**
         * Access method.<p>
         *
         * @return the CSS class name
         */
        String title();

        /**
         * Access method.<p>
         *
         * @return the CSS class name
         */
        @ClassName("oc-toolbar")
        String toolbar();

        /**
         * Access method.<p>
         *
         * @return the CSS class name
         */
        String toolbarBackground();

        /**
         * Access method.<p>
         *
         * @return the CSS class name
         */
        String toolbarButtonsLeft();

        /**
         * Access method.<p>
         *
         * @return the CSS class name
         */
        String toolbarButtonsRight();

        /**
         * Access method.<p>
         *
         * @return the CSS class name
         */
        String toolbarCenter();

        /**
         * Access method.<p>
         *
         * @return the CSS class name
         */
        String toolbarContent();

        /**
         * Access method.<p>
         *
         * @return the CSS class name
         */
        String toolbarFontButton();

        /**
         * Access method.<p>
         *
         * @return the CSS class name
         */
        String toolbarHide();

        /**
         * Access method.<p>
         *
         * @return the CSS class name
         */
        String toolbarLogo();

        String toolbarPlacementMode();

        /**
         * Access method.<p>
         *
         * @return the CSS class name
         */
        String toolbarShow();

        /**
         * Access method.<p>
         *
         * @return the CSS class name
         */
        String userInfo();

        /**
         * Access method.<p>
         *
         * @return the CSS class name
         */
        String userInfoButtons();

        /**
         * Access method.<p>
         *
         * @return the CSS class name
         */
        String userInfoDialog();
    }

    /** The upload button CSS classes. */
    public interface I_CmsUploadButtonCss extends I_CmsPopupCss {

        /**
         * Access method.<p>
         *
         * @return the CSS class name
         */
        String dialogMessage();

        /**
         * Access method.<p>
         *
         * @return the CSS class name
         */
        String dialogMessageImportant();

        /**
         * Access method.<p>
         *
         * @return the CSS class name
         */
        String fileInfoTable();

        /**
         * Access method.<p>
         *
         * @return the CSS class name
         */
        String loadingAnimation();

        /**
         * Access method.<p>
         *
         * @return the CSS class name
         */
        String loadingPanel();

        /**
         * Access method.<p>
         *
         * @return the CSS class name
         */
        String loadingText();

        /**
         * Access method.<p>
         *
         * @return the CSS class name
         */
        String progressInfo();

        /**
         * Access method.<p>
         *
         * @return the CSS class name
         */
        String uploadButton();

        /**
         * Access method.<p>
         *
         * @return the CSS class name
         */
        String uploadDialogButton();

        /**
         * Access method.<p>
         *
         * @return the CSS class name
         */
        String uploadFileInput();

        /**
         * Access method.<p>
         *
         * @return the CSS class name
         */
        String warningMessage();
    }

    /** The bundle instance. */
    I_CmsLayoutBundle INSTANCE = GWT.create(I_CmsLayoutBundle.class);

    /**
     * Access method.
     *
     * @return the attribute editor CSS
     */
    @Source("attributeEditor.gss")
    @Import(value = {I_CmsInputCss.class, I_CmsDialogCss.class})
    I_CmsAttributeEditorCss attributeEditorCss();

    /**
     * Access method.<p>
     *
     * @return the availability dialog CSS
     */
    @Source("availability.gss")
    I_CmsAvailabilityCss availabilityCss();

    /**
     * Access method.<p>
     *
     * @return the button CSS
     */
    @Source("button.gss")
    I_CmsButtonCss buttonCss();

    /**
     * Access method.<p>
     *
     * @return the gallery dialog CSS
     */
    @Source("categorydialog.gss")
    I_CmsCategoryDialogCss categoryDialogCss();

    /**
     * Access method.<p>
     *
     * @return the gallery dialog CSS
     */
    @Source("colorSelector.gss")
    I_CmsColorSelector colorSelectorCss();

    /**
     * The CSS constants bundle.<p>
     *
     * @return a bundle of CSS constants
     */
    I_CmsConstantsBundle constants();

    /**
     * Access method.<p>
     *
     * @return the content editor dialog CSS
     */
    @Source("contentEditor.gss")
    @CssResource.NotStrict
    I_CmsContentEditorCss contentEditorCss();

    /**
     * Access method.<p>
     *
     * @return the constants CSS
     */
    @Source("contextmenu.gss")
    I_CmsContextmenuCss contextmenuCss();

    /**
     * Access method.<p>
     *
     * @return the CSS class name
     */
    I_CmsImageBundle coreImages();

    /**
     * Access method.<p>
     *
     * @return the constants CSS
     */
    @Source("datebox.gss")
    I_CmsDateBoxCss dateBoxCss();

    /**
     * Access method.<p>
     *
     * @return the dialog CSS
     */
    @Source("dialog.gss")
    @Import(value = {I_CmsInputCss.class})
    I_CmsDialogCss dialogCss();

    /**
     * Access method.<p>
     *
     * @return the dialog CSS
     */
    @Source("directedit.gss")
    I_CmsDirectEditCss directEditCss();

    /**
     * Access method.<p>
     *
     * @return the drag and drop CSS
     */
    @Source("dragdrop.gss")
    I_CmsDragCss dragdropCss();

    /**
     * Access method.<p>
     *
     * @return the error dialog CSS
     */
    @Source("elementSettingsDialog.gss")
    @Import(value = {I_CmsInputCss.class, I_CmsDialogCss.class})
    I_CmsElementSettingsDialogCss elementSettingsDialogCss();

    /**
     * Access method.<p>
     *
     * @return the error dialog CSS
     */
    @Source("errorDialog.gss")
    I_CmsErrorDialogCss errorDialogCss();

    /**
     * Access method.<p>
     *
     * @return the constants CSS
     */
    @Source("fieldset.gss")
    I_CmsFieldsetCss fieldsetCss();

    /**
     * Access method.<p>
     *
     * @return the filter select CSS
     */
    @Source("filterselect.gss")
    @Import(value = {I_CmsInputCss.class})
    I_CmsFilterSelectCss filterSelectCss();

    /**
     * Access method.<p>
     *
     * @return the toolbar CSS
     */
    @Source("floatDecoratedPanel.gss")
    I_CmsFloatDecoratedPanelCss floatDecoratedPanelCss();

    /**
     * Access method.<p>
     *
     * @return the general CSS
     */
    @Source("general.gss")
    I_CmsGeneralCss generalCss();

    /**
     * Access method.<p>
     *
     * @return the upload button CSS
     */
    @Source("globalWidget.gss")
    I_CmsGlobalWidgetCss globalWidgetCss();

    /**
     * Access method.<p>
     *
     * @return the highlight CSS
     */
    @Source("highlight.gss")
    I_CmsHighlightCss highlightCss();

    /**
     * Access method.<p>
     *
     * @return the list item CSS
     */
    @Source("linkWarning.gss")
    I_CmsLinkWarningCss linkWarningCss();

    /**
     * Access method.<p>
     *
     * @return the CSS for the list add dialog
     */
    @Source("listadd.css")
    @Import(value = {I_CmsListItemWidgetCss.class})
    I_CmsListAddCss listAddCss();

    /**
     * Access method.<p>
     *
     * @return the list item CSS
     */
    @Source("listItemWidget.gss")
    @CssResource.NotStrict
    I_CmsListItemWidgetCss listItemWidgetCss();

    /**
     * Access method.<p>
     *
     * @return the list tree CSS
     */
    @Source("listtree.gss")
    I_CmsListTreeCss listTreeCss();

    /**
     * Access method.<p>
     *
     * @return the location picker CSS
     */
    @Source("locationPicker.gss")
    I_CmsLocationPicker locationPickerCss();

    /**
     * Access method.<p>
     *
     * @return the menu button CSS
     */
    @Source("menuButton.gss")
    I_CmsMenuButton menuButtonCss();

    /**
     * Access method.<p>
     *
     * @return the list item CSS
     */
    @Source("notification.gss")
    I_CmsNotificationCss notificationCss();

    /**
     * Access method.<p>
     *
     * @return the upload button CSS
     */
    @Source("openerHoverWidget.gss")
    I_CmsOpenerHoverCss openerHoverCss();

    /**
     * Access method.<p>
     *
     * @return the constants CSS
     */
    @Source("progressbar.gss")
    I_CmsProgressBarCss progressBarCss();

    /**
     * Access method.<p>
     *
     * @return the page CSS
     */
    @Source("properties.gss")
    I_CmsPropertiesCss propertiesCss();

    /**
     * Access method.<p>
     *
     * @return the list item CSS
     */
    @Source("resourceState.gss")
    I_CmsResourceStateCss resourceStateCss();

    /**
     * Access method.<p>
     *
     * @return the constants CSS
     */
    @Source("scrollBar.gss")
    I_CmsScrollBarCss scrollBarCss();

    /**
     * Access method.<p>
     *
     * @return the select area CSS
     */
    @Source("selectArea.gss")
    I_CmsSelectArea selectAreaCss();

    /**
     * Access method.<p>
     *
     * @return the single line list item CSS
     */
    @Source("singleLineItem.gss")
    I_CmsSingleLineItem singleLineItemCss();

    /**
     * Access method. These CSS classes are used to indicate the state of ui items, use them within a dedicated CSS resources.
     * Do not inject this CSS, as it contains no style information.<p>
     *
     * @return the state CSS
     */
    @Source("state.gss")
    I_CmsStateCss stateCss();

    /**
     * Access method.<p>
     *
     * @return tabbed panel CSS
     */
    @Source("tabbedPanel.gss")
    I_CmsTabbedPanelCss tabbedPanelCss();

    /**
     * Access method.<p>
     *
     * @return the toolbar CSS
     */
    @Source("toolbar.gss")
    I_CmsToolbarCss toolbarCss();

    /**
     * Access method.<p>
     *
     * @return the upload button CSS
     */
    @Source("uploadButton.gss")
    I_CmsUploadButtonCss uploadButton();
}
