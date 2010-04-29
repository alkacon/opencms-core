/*
 * File   : $Source: /alkacon/cvs/opencms/src-modules/org/opencms/gwt/client/ui/css/Attic/I_CmsLayoutBundle.java,v $
 * Date   : $Date: 2010/04/29 07:13:40 $
 * Version: $Revision: 1.24 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (C) 2002 - 2009 Alkacon Software (http://www.alkacon.com)
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
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.resources.client.CssResource.Shared;
import com.google.gwt.resources.client.ImageResource.ImageOptions;
import com.google.gwt.resources.client.ImageResource.RepeatStyle;

/**
 * Resource bundle to access CSS and image resources.
 * 
 * @author Tobias Herrmann
 * 
 * @version $Revision: 1.24 $
 * 
 * @since 8.0.0
 */
public interface I_CmsLayoutBundle extends ClientBundle {

    /** Button CSS. */
    interface I_CmsButtonCss extends CssResource, I_CmsStateCss {

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
         * Access method.<p>
         * 
         * @return the CSS class name
         */
        String cmsImageButtonTransparent();

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
        String cmsTextButton();
    }

    /** Constants CSS. */
    interface I_CmsConstantsCss extends CssResource {

        /** 
         * Access method.<p>
         * 
         * @return the CSS class name
         */
        String backgroundColorBar();

        /** 
         * Access method.<p>
         * 
         * @return the CSS class name
         */
        String backgroundColorLight();

        /** 
         * Access method.<p>
         * 
         * @return the CSS class name
         */
        String borderColor();

        /** 
         * Access method.<p>
         * 
         * @return the CSS class name
         */
        String borderColorActive();

        /** 
         * Access method.<p>
         * 
         * @return the CSS class name
         */
        String borderColorInactive();

        /** 
         * Access method.<p>
         * 
         * @return the CSS class name
         */
        String borderRadius();

        /** 
         * Access method.<p>
         * 
         * @return the CSS class name
         */
        String color();

        /** 
         * Access method.<p>
         * 
         * @return the CSS class name
         */
        String colorDisabled();

        /** 
         * Access method.<p>
         * 
         * @return the CSS class name
         */
        String fontFamily();

        /** 
         * Access method.<p>
         * 
         * @return the CSS class name
         */
        String fontSize();

        /** 
         * Access method.<p>
         * 
         * @return the CSS class name
         */
        String fontSizeBig();

        /** 
         * Access method.<p>
         * 
         * @return the CSS class name
         */
        String fontSizeSmall();

        /** 
         * Access method.<p>
         * 
         * @return the CSS class name
         */
        String pageWidth();

        /** 
         * Access method.<p>
         * 
         * @return the CSS class name
         */
        String toolbarHeight();
    }

    /** Dialog CSS. */
    @Shared
    interface I_CmsDialogCss extends CssResource {

        /** 
         * Access method.<p>
         * 
         * @return the CSS class name
         */
        String menuPopup();

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
        String popupButtonPanel();

        /** 
         * Access method.<p>
         * 
         * @return the CSS class name
         */
        String popupContent();

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

        /** 
         * Access method.<p>
         * 
         * @return the CSS class name
         */
        String popupShadow();
    }

    /** General CSS, used for general re-occurring styles. */
    @Shared
    interface I_CmsGeneralCss extends I_CmsStateCss {

        /** 
         * Access method.<p>
         * 
         * @return the CSS class name
         */
        String background();

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

    /** Icons CSS, making available a fixed set of icons. */
    interface I_CmsIconsCss extends CssResource, I_CmsStateCss {

        /** 
         * Access method.<p>
         * 
         * @return the CSS class name
         */
        @ClassName("ui_icon")
        String uiIcon();

    }

    /** List item CSS. */
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
        String itemTitle();

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
        String listTreeItemHandler();

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
        String tabLeftMargin();

    }

    /** Toolbar CSS. */
    interface I_CmsToolbarCss extends CssResource {

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
        String notificationWarning();

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
        String toolbarPlaceholder();

        /** 
         * Access method.<p>
         * 
         * @return the CSS class name
         */
        String toolbarShow();
    }

    /** The bundle instance. */
    I_CmsLayoutBundle INSTANCE = GWT.create(I_CmsLayoutBundle.class);

    /**
     * Access method.<p>
     * 
     * @return the image resource
     */
    @Source("images/ui-bg_glass_65_ffffff_1x400.png")
    @ImageOptions(repeatStyle = RepeatStyle.Horizontal)
    ImageResource backgroundActive();

    /**
     * Access method.<p>
     * 
     * @return the image resource
     */
    @Source("images/ui-bg_glass_75_e6e6e6_1x400.png")
    @ImageOptions(repeatStyle = RepeatStyle.Horizontal)
    ImageResource backgroundDefault();

    /**
     * Access method.<p>
     * 
     * @return the image resource
     */
    @Source("images/ui-bg_glass_75_cccccc_1x400.png")
    @ImageOptions(repeatStyle = RepeatStyle.Horizontal)
    ImageResource backgroundHover();

    /**
     * Access method.<p>
     * 
     * @return the image resource
     */
    @Source("images/ui-bg_highlight-soft_75_aaaaaa_1x100.png")
    @ImageOptions(repeatStyle = RepeatStyle.Horizontal)
    ImageResource backgroundSoft();

    /**
     * Access method.<p>
     * 
     * @return the button CSS
     */
    @Source("button.css")
    I_CmsButtonCss buttonCss();

    /**
     * Access method.<p>
     * 
     * @return the constants CSS
     */
    @Source("constants.css")
    I_CmsConstantsCss constantsCss();

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
     * @return the header CSS
     */
    @Source("header.css")
    I_CmsHeaderCss headerCss();

    /** 
     * Access method.<p>
     * 
     * @return the image resource
     */
    @Source("images/ocms_de_b_blue.gif")
    @ImageOptions(repeatStyle = RepeatStyle.Horizontal)
    ImageResource highlightBorderBottomBlue();

    /**
     * Access method.<p>
     * 
     * @return the image resource
     */
    @Source("images/ocms_de_b.gif")
    @ImageOptions(repeatStyle = RepeatStyle.Horizontal)
    ImageResource highlightBorderBottomRed();

    /**
     * Access method.<p>
     * 
     * @return the image resource
     */
    @Source("images/ocms_de_l_blue.gif")
    @ImageOptions(repeatStyle = RepeatStyle.Vertical)
    ImageResource highlightBorderLeftBlue();

    /**
     * Access method.<p>
     * 
     * @return the image resource
     */
    @Source("images/ocms_de_l.gif")
    @ImageOptions(repeatStyle = RepeatStyle.Vertical)
    ImageResource highlightBorderLeftRed();

    /**
     * Access method.<p>
     * 
     * @return the image resource
     */
    @Source("images/ocms_de_r_blue.gif")
    @ImageOptions(repeatStyle = RepeatStyle.Vertical)
    ImageResource highlightBorderRightBlue();

    /**
     * Access method.<p>
     * 
     * @return the image resource
     */
    @Source("images/ocms_de_r.gif")
    @ImageOptions(repeatStyle = RepeatStyle.Vertical)
    ImageResource highlightBorderRightRed();

    /**
     * Access method.<p>
     * 
     * @return the image resource
     */
    @ImageOptions(repeatStyle = RepeatStyle.Horizontal)
    @Source("images/ocms_de_t_blue.gif")
    ImageResource highlightBorderTopBlue();

    /**
     * Access method.<p>
     * 
     * @return the image resource
     */
    @Source("images/ocms_de_t.gif")
    @ImageOptions(repeatStyle = RepeatStyle.Horizontal)
    ImageResource highlightBorderTopRed();

    /**
     * Access method.<p>
     * 
     * @return the highlight CSS
     */
    @Source("highlight.css")
    I_CmsHighlightCss highlightCss();

    /** 
     * Access method.<p>
     * 
     * @return the image resource
     */
    @Source("images/ui-icons_222222_256x240.png")
    ImageResource iconActive();

    /**
     * Access method.<p>
     * 
     * @return the image resource
     */
    @Source("images/ui-icons_444444_256x240.png")
    ImageResource iconDefault();

    /**
     * Access method.<p>
     * 
     * @return the icons CSS
     */
    @Source("buttonIcons.css")
    @CssResource.NotStrict
    I_CmsIconsCss iconsCss();

    /**
     * Access method.<p>
     * 
     * @return the list item CSS
     */
    @Source("listItemWidget.css")
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
     * @return the image resource
     */
    @Source("images/placeholderOverlay_trans.png")
    @ImageOptions(repeatStyle = RepeatStyle.Both)
    ImageResource overlayImage();

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
