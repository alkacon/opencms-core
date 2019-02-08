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

package org.opencms.workplace;

import java.util.Arrays;
import java.util.Collection;

/**
 * Message bundle names for legacy tools which don't correspond to an existing module anymore.
 */
public class CmsLegacyWorkplaceMessageBundleProvider implements I_CmsWorkplaceMessageBundleProvider {

    public Collection<String> getMessageBundleNames() {

        return Arrays.asList(
            "org.opencms.editors.codemirror",
            "org.opencms.editors.tinymce",
            "org.opencms.ugc",
            "org.opencms.workplace",
            "org.opencms.workplace.administration",
            "org.opencms.workplace.explorer",
            "org.opencms.workplace.spellcheck",
            "org.opencms.workplace.tools.accounts",
            "org.opencms.workplace.tools.cache",
            "org.opencms.workplace.tools.content",
            "org.opencms.workplace.tools.database",
            "org.opencms.workplace.tools.galleryoverview",
            "org.opencms.workplace.tools.history",
            "org.opencms.workplace.tools.link",
            "org.opencms.workplace.tools.modules",
            "org.opencms.workplace.tools.projects",
            "org.opencms.workplace.tools.scheduler",
            "org.opencms.workplace.tools.searchindex",
            "org.opencms.workplace.tools.sites",
            "org.opencms.workplace.tools.workplace",
            "org.opencms.workplace.traditional");
    }

}
