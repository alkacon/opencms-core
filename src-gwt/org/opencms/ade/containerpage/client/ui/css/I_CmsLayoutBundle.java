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

package org.opencms.ade.containerpage.client.ui.css;

import org.opencms.gwt.client.ui.css.I_CmsConstantsBundle;
import org.opencms.gwt.client.ui.css.I_CmsDirectEditCss;
import org.opencms.gwt.client.ui.css.I_CmsLayoutBundle.I_CmsButtonCss;

import com.google.gwt.core.client.GWT;
import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.resources.client.CssResource.Import;
import com.google.gwt.resources.client.CssResource.Shared;

/**
 * Resource bundle to access CSS and image resources.
 *
 * @since 8.0.0
 */
public interface I_CmsLayoutBundle extends ClientBundle {

    /** Container-page CSS. */
    public interface I_CmsContainerpageCss extends I_CmsDirectEditCss, I_CmsDragDropCss {

        /** Access method.<p>
         *
         * @return the CSS class name
         */
        String clipboardList();

        /**
         * Access method.<p>
         *
         * @return the CSS class name
         */
        String emptyGroupContainer();

        /** Access method.<p>
         *
         * @return the CSS class name
         */
        @ClassName("oc-enlarge-small-elements")
        String enlargeSmallElements();

        /**
         * Access method.<p>
         *
         * @return the CSS class name
         */
        String expired();

        /**
         * Access method.<p>
         *
         * @return the CSS class name
         */
        String expiredOverlay();

        /**
         * Access method.<p>
         *
         * @return the CSS class name
         */
        String functionElement();

        /** Access method.<p>
         *
         * @return the CSS class name
         */
        @ClassName("oc-groupcontainer")
        String groupContainer();

        /** Access method.<p>
         *
         * @return the CSS class name
         */
        String groupcontainerEditing();

        /** Access method.<p>
         *
         * @return the CSS class name
         */
        String groupcontainerEditor();

        /** Access method.<p>
         *
         * @return the CSS class name
         */
        String groupcontainerOverlay();

        /** Access method.<p>
         *
         * @return the CSS class name
         */
        String groupcontainerPlaceholder();

        /** Access method.<p>
         *
         * @return the CSS class name
         */
        String hiddenElement();

        /** Access method.<p>
         *
         * @return the CSS class name
         */
        String hiddenElementOverlay();

        /** Access method.<p>
         *
         * @return the CSS class name
         */
        String hideElements();

        /** Access method.<p>
         *
         * @return the CSS class name
         */
        @ClassName("oc-ignore-small-elements")
        String ignoreSmallElements();

        /** Access method.<p>
        *
        * @return the CSS class name
        */
        String lockedElement();

        /** Access method.<p>
         *
         * @return the CSS class name
         */
        String menuTabContainer();

        /** Access method.<p>
        *
        * @return the CSS class name
        */
        @ClassName("oc-nondefault-view")
        String nonDefaultView();

        /** Access method.<p>
        *
        * @return the CSS class name
        */
        @ClassName("oc-reused-element")
        String reusedElement();

        /** Access method.<p>
         *
         * @return the CSS class name
         */
        @ClassName("oc-small-element")
        String smallElement();

        /** Access method.<p>
         *
         * @return the CSS class name
         */
        String toolbarToggle();
    }

    /** The drag and drop CSS classes used also within the container-page CSS. */
    @Shared
    public interface I_CmsDragDropCss extends org.opencms.gwt.client.ui.css.I_CmsLayoutBundle.I_CmsDragCss {

        /**
         * Access method.<p>
         *
         * @return the CSS class name
         */
        @ClassName("oc-drag-element")
        String dragElement();

        /**
         * Access method.<p>
         *
         * @return the CSS class name
         */
        String dragging();

    }

    /** The drag and drop CSS classes. */
    public interface I_CmsDragDropExtendedCss extends I_CmsDragDropCss {

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
        String currentTarget();

        /**
         * Access method.<p>
         *
         * @return the CSS class name
         */
        String dragElementBackground();

        /**
         * Access method.<p>
         *
         * @return the CSS class name
         */
        String dragElementBorder();

        /**
         * Access method.<p>
         *
         * @return the CSS class name
         */
        String dragGroupContainer();

        /**
         * Access method.<p>
         *
         * @return the CSS class name
         */
        String dragHandle();

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
        @ClassName("oc-drag-target")
        String dragTarget();

        /**
         * Access method.<p>
         *
         * @return the CSS class name
         */
        String overlayShow();

        /**
         * Access method.<p>
         *
         * @return the CSS class name
         */
        String placeholderOverlay();

    }

    /** Group container editor CSS. */
    public interface I_CmsGroupContainer extends CssResource {

        /** Access method.<p>
         *
         * @return the CSS class name
         */
        String containerMarker();

        /** Access method.<p>
         *
         * @return the CSS class name
         */
        String inputBox();

        /** Access method.<p>
         *
         * @return the CSS class name
         */
        String inputLabel();

        /** Access method.<p>
         *
         * @return the CSS class name
         */
        String inputRow();
    }

    /** The bundle instance. */
    I_CmsLayoutBundle INSTANCE = GWT.create(I_CmsLayoutBundle.class);

    /**
     * The accessor for the CSS constants bundle.<p>
     *
     * @return the constants bundle
     */
    I_CmsConstantsBundle constants();

    /**
     * Access method.<p>
     *
     * @return the container-page CSS
     */
    @Source("containerpage.gss")
    @Import(I_CmsButtonCss.class)
    I_CmsContainerpageCss containerpageCss();

    /**
     * Access method.<p>
     *
     * @return the drag and drop CSS
     */
    @Source("dragdrop.gss")
    I_CmsDragDropExtendedCss dragdropCss();

    /**
     * Access method.<p>
     *
     * @return the container-page CSS
     */
    @Source("groupcontainer.gss")
    I_CmsGroupContainer groupcontainerCss();
}
