/*
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (c) Alkacon Software GmbH & Co. KG (https://www.alkacon.com)
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
 * company website: https://www.alkacon.com
 *
 * For further information about OpenCms, please see the
 * project website: https://www.opencms.org
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package org.opencms.ade.contenteditor;

import org.opencms.ade.configuration.CmsADEConfigData;
import org.opencms.configuration.I_CmsConfigurationParameterHandler;
import org.opencms.file.CmsFile;
import org.opencms.file.CmsObject;
import org.opencms.xml.content.I_CmsXmlContentAugmentation;

/**
 * Interface for the content translator.
 *
 * <p>This is used by the translation button in the form based content editor.
 *
 * <p>The global content translator is available via CmsWorkplaceManager and is configured in opencms-workplace.xml.
 */
public interface I_CmsContentTranslator extends I_CmsConfigurationParameterHandler {

    /**
     * Returns a fresh content augmentation object for doing the actual translation work.
     *
     * @return the content augmentation object
     */
    public I_CmsXmlContentAugmentation getContentAugmentation();

    /**
     * Checks if the translation should be enabled for the current context.
     *
     * @param cms the CMS context
     * @param config the currently active ADE configuration
     * @param file the edited file
     * @return true if the translation should be enabled
     */
    public boolean isEnabled(CmsObject cms, CmsADEConfigData config, CmsFile file);

}
