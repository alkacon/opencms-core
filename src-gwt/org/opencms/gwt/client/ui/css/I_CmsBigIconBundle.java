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

import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.ImageResource;

/**
 * Image bundle for big icons.
 *
 * @since 8.0.0
 */
public interface I_CmsBigIconBundle extends ClientBundle {

    /**
     * Access method.<p>
     * @return an image resource
     */
    @Source("images/icons/big/brokenLinkBigIcon.png")
    ImageResource brokenLinkBigIcon();

    /**
     * Access method.<p>
     * @return an image resource
     */
    @Source("images/icons/big/checkmark.png")
    ImageResource checkmark();

    /**
     * Access method.<p>
     * @return an image resource
     */
    @Source("images/icons/big/filetypeNavlevel.png")
    ImageResource fileTypeNavLevel();

    /**
     * Access method.<p>
     * @return an image resource
     */
    @Source("images/icons/big/invPropertyBigIconActive.png")
    ImageResource invPropertyBigIconActive();

    /**
     * Access method.<p>
     * @return an image resource
     */
    @Source("images/icons/big/invPropertyBigIconDeactivated.png")
    ImageResource invPropertyBigIconDeactivated();

    /**
     * Access method.<p>
     *
     * @return an image resource
     */
    @Source("images/icons/big/stateExportBigIcon.png")
    ImageResource stateExportBigIcon();

    /**
     * Access method.<p>
     *
     * @return an image resource
     */
    @Source("images/icons/big/stateHiddenBigIcon.png")
    ImageResource stateHiddenBigIcon();

    /**
     * Access method.<p>
     *
     * @return an image resource
     */
    @Source("images/icons/big/stateNormalBigIcon.png")
    ImageResource stateNormalBigIcon();

    /**
     * Access method.<p>
     *
     * @return an image resource
     */
    @Source("images/icons/big/stateRedirectBigIcon.png")
    ImageResource stateRedirectBigIcon();

    /**
     * Access method.<p>
     *
     * @return an image resource
     */
    @Source("images/icons/big/stateSecureBigIcon.png")
    ImageResource stateSecureBigIcon();
}