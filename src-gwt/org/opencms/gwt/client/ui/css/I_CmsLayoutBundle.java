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

package org.opencms.gwt.client.ui.css;

import com.google.gwt.core.client.GWT;
import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.CssResource;
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
    interface I_CmsButtonCss extends I_CmsStateCss {

        /**
         *  Access method.<p>
         * 
         * @return the CSS class name
         */
        String black();

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
        String cmsImageButton();

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
        String red();

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

        /**
         *  Access method.<p>
         * 
         * @return the CSS class name
         */
        String yellow();
    }

    /** The content editor dialog CSS. */
    interface I_CmsContentEditorCss extends CssResource {

        /** Access method.<p>
         * 
         * @return the CSS class name
         */
        String contentEditor();
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
        String itemIcon();

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
        String image();

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
        String shadow();

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
        String colorRed();

        /** 
         * Access method.<p>
         * 
         * @return the CSS class name
         */
        String highlightBox();
    }

    /** Link warning panel CSS. */
    interface I_CmsLinkWarningCss extends CssResource {

        /** 
         * Access method.<p>
         * 
         * @return the CSS class name
         */
        String brokenLink();

        /** 
         * Access method.<p>
         * 
         * @return the CSS class name
         */
        String deletedEntryLabel();
    }

    /** List item CSS. */
    interface I_CmsListItemCss extends CssResource {

        /** 
         * Access method.<p>
         * 
         * @return the CSS class name
         */
        String listItemCheckbox();

    }

    /** List item CSS. */
    @Shared
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
        String disabledItem();

        /** 
         * Access method.<p>
         * 
         * @return the CSS class name
         */
        String dragging();

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
        String hidden();

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
        String itemRed();

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
         * CSS class accessor.<p>
         * 
         * @return a CSS class
         **/
        String normal();

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
        String permaVisible();

        /**
         * CSS class accessor.<p>
         * 
         * @return a CSS class
         **/
        String redirect();

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
        String titleInput();

        /** 
         * Access method.<p>
         * 
         * @return the CSS class name
         */
        String titleRow();

    }

    /** ListTree CSS. */
    interface I_CmsListTreeCss extends CssResource {

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

        /** 
         * Access method.<p>
         * 
         * @return the CSS class name
         */
        String minus();

        /** 
         * Access method.<p>
         * 
         * @return the CSS class name
         */
        String plus();
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
        String loadingAnimation();

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
        String simpleToolbarShow();

        /** 
         * Access method.<p>
         * 
         * @return the CSS class name
         */
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
        String toolbarContent();

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
        String toolbarShow();

        /** 
         * Access method.<p>
         * 
         * @return the CSS class name
         */
        String toolbarToggle();
    }

    /** The bundle instance. */
    I_CmsLayoutBundle INSTANCE = GWT.create(I_CmsLayoutBundle.class);

    /**
     * Access method.<p>
     * 
     * @return the constants CSS
     */
    @Source("availability.css")
    I_CmsAvailabilityCss availabilityCss();

    /**
     * Access method.<p>
     * 
     * @return the image bundle for the backgrounds
     */
    I_CmsBackgroundImageBundle backgrounds();

    /**
     * Access method.<p>
     * 
     * @return the image bundle for the borders
     */
    I_CmsBorderImageBundle borders();

    /**
     * Access method.<p>
     * 
     * @return the button CSS
     */
    @Source("button.css")
    I_CmsButtonCss buttonCss();

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
    @Source("contentEditor.css")
    @CssResource.NotStrict
    I_CmsContentEditorCss contentEditorCss();

    /**
     * Access method.<p>
     * 
     * @return the constants CSS
     */
    @Source("contextmenu.css")
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
    @Source("datebox.css")
    I_CmsDateBoxCss dateBoxCss();

    /**
     * Access method.<p>
     * 
     * @return the dialog CSS
     */
    @Source("dialog.css")
    I_CmsDialogCss dialogCss();

    /**
     * Access method.<p>
     * 
     * @return the dialog CSS
     */
    @Source("directedit.css")
    I_CmsDirectEditCss directEditCss();

    /**
     * Access method.<p>
     * 
     * @return the drag and drop CSS
     */
    @Source("dragdrop.css")
    I_CmsDragCss dragdropCss();

    /**
     * Access method.<p>
     * 
     * @return the error dialog CSS
     */
    @Source("errorDialog.css")
    I_CmsErrorDialogCss errorDialogCss();

    /**
     * Access method.<p>
     * 
     * @return the constants CSS
     */
    @Source("fieldset.css")
    I_CmsFieldsetCss fieldsetCss();

    /**
     * Access method.<p>
     * 
     * @return the toolbar CSS
     */
    @Source("floatDecoratedPanel.css")
    I_CmsFloatDecoratedPanelCss floatDecoratedPanelCss();

    /**
     * Access method.<p>
     * 
     * @return the general CSS
     */
    @Source("general.css")
    I_CmsGeneralCss generalCss();

    /**
     * Access method.<p>
     * 
     * @return the image bundle for the gwt module
     */
    I_CmsImageBundle gwtImages();

    /**
     * Access method.<p>
     * 
     * @return the header CSS
     */
    @Source("header.css")
    I_CmsHeaderCss headerCss();

    /**
     * Access method.<p>
     * 
     * @return the highlight CSS
     */
    @Source("highlight.css")
    I_CmsHighlightCss highlightCss();

    /**
     * Returns the image bundle for the input package.<p>
     * 
     * @return an image bundle
     */
    I_CmsInputImageBundle inputImages();

    /**
     * Access method.<p>
     * 
     * @return the list item CSS 
     */
    @Source("linkWarning.css")
    I_CmsLinkWarningCss linkWarningCss();

    /**
     * Access method.<p>
     * 
     * @return the list item CSS 
     */
    @Source("listitem.css")
    I_CmsListItemCss listItemCss();

    /**
     * Access method.<p>
     * 
     * @return the list item CSS
     */
    @Source("listItemWidget.css")
    @CssResource.NotStrict
    I_CmsListItemWidgetCss listItemWidgetCss();

    /**
     * Access method.<p>
     * 
     * @return the list tree CSS
     */
    @Source("listtree.css")
    I_CmsListTreeCss listTreeCss();

    /**
     * Access method.<p>
     * 
     * @return the menu button CSS
     */
    @Source("menuButton.css")
    I_CmsMenuButton menuButtonCss();

    /**
     * Access method.<p>
     * 
     * @return the list item CSS
     */
    @Source("notification.css")
    I_CmsNotificationCss notificationCss();

    /**
     * Access method.<p>
     * 
     * @return the constants CSS
     */
    @Source("progressbar.css")
    I_CmsProgressBarCss progressBarCss();

    /**
     * Access method.<p>
     * 
     * @return the page CSS
     */
    @Source("properties.css")
    I_CmsPropertiesCss propertiesCss();

    /**
     * Access method.<p>
     * 
     * @return the list item CSS
     */
    @Source("resourceState.css")
    I_CmsResourceStateCss resourceStateCss();

    /**
     * Access method.<p>
     * 
     * @return the select area CSS
     */
    @Source("selectArea.css")
    I_CmsSelectArea selectAreaCss();

    /**
     * Access method.<p>
     * 
     * @return the single line list item CSS
     */
    @Source("singleLineItem.css")
    I_CmsSingleLineItem singleLineItemCss();

    /**
     * Access method. These CSS classes are used to indicate the state of ui items, use them within a dedicated CSS resources.
     * Do not inject this CSS, as it contains no style information.<p>
     * 
     * @return the state CSS
     */
    @Source("state.css")
    I_CmsStateCss stateCss();

    /**
     * Access method.<p>
     *  
     * @return tabbed panel CSS
     */
    @Source("tabbedPanel.css")
    I_CmsTabbedPanelCss tabbedPanelCss();

    /**
     * Access method.<p>
     * 
     * @return the toolbar CSS
     */
    @Source("toolbar.css")
    I_CmsToolbarCss toolbarCss();
}
