/*
 * File   : $Source: /alkacon/cvs/opencms/src-modules/org/opencms/ade/sitemap/Attic/CmsTitleValidationService.java,v $
 * Date   : $Date: 2010/09/09 15:02:20 $
 * Version: $Revision: 1.1 $
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

package org.opencms.ade.sitemap;

import org.opencms.file.CmsObject;
import org.opencms.gwt.I_CmsValidationService;
import org.opencms.gwt.shared.CmsValidationResult;
import org.opencms.util.CmsStringUtil;

/**
 * A dummy validation service for the title of a sitemap entry.
 * 
 * @author Georg Westenberger
 * 
 * @version $Revision: 1.1 $
 * 
 * @since 8.0.0
 */
public class CmsTitleValidationService implements I_CmsValidationService {

    /**
     * @see org.opencms.gwt.I_CmsValidationService#validate(org.opencms.file.CmsObject, java.lang.String, java.lang.String)
     */
    public CmsValidationResult validate(CmsObject cms, String value, String config) {

        if (CmsStringUtil.isEmptyOrWhitespaceOnly(value)) {
            String errorMessage = Messages.get().getBundle().key(Messages.ERR_TITLE_MUST_NOT_BE_EMPTY_0);
            return new CmsValidationResult(errorMessage, value);
        }
        // TODO: strip whitespace 
        return new CmsValidationResult(null, value);
    }

}
