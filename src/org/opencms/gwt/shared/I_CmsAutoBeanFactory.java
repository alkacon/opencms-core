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

package org.opencms.gwt.shared;

import com.google.web.bindery.autobean.shared.AutoBean;
import com.google.web.bindery.autobean.shared.AutoBeanFactory;

/**
 * General AutoBean factory interface.
 */
public interface I_CmsAutoBeanFactory extends AutoBeanFactory {

    /**
     * Creates a new AutoBean.<p>
     *
     * @return the created AutoBean
     */
    AutoBean<I_CmsContentLoadCollectorInfo> createCollectorInfo();

    /**
     * Creates a new configuration AutoBean.
     *
     * @return the new configuration bean
     */
    AutoBean<I_CmsCodeMirrorClientConfiguration> createConfiguration();

    /**
     * Creates a new dialog info AutoBean.
     *
     * @return the new bean
     */
    AutoBean<I_CmsEmbeddedDialogInfo> createDialogInfo();

    /**
     * Creates a new instance.
     *
     * @return the new instance
     */
    AutoBean<I_CmsEditableDataExtensions> createExtensions();

    /**
     * Creates an I_CmsListAddMetadat AutoBean.
     *
     * @return the bean
     */
    AutoBean<I_CmsListAddMetadata> createListAddMetadata();

    /**
     * Creates a new unlock data instance.
     *
     * @return the unlock data
     */
    AutoBean<I_CmsUnlockData> unlockData();

    /**
     * Creates an AutoBean wrapping an object.<p>
     *
     * @param info the object to wrap
     *
     * @return the AutoBean wrapper
     */
    AutoBean<I_CmsContentLoadCollectorInfo> wrapCollectorInfo(I_CmsContentLoadCollectorInfo info);

    /**
     * Wraps an instance.
     *
     * @param data the bean to wrap
     *
     * @return the new wrapper
     */
    AutoBean<I_CmsEditableDataExtensions> wrapExtensions(I_CmsEditableDataExtensions data);

}
