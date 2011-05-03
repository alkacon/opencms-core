/*
 * File   : $Source: /alkacon/cvs/opencms/src-modules/org/opencms/ade/sitemap/Attic/CmsUrlNameValidationService.java,v $
 * Date   : $Date: 2011/05/03 10:49:13 $
 * Version: $Revision: 1.3 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (C) 2002 - 2011 Alkacon Software (http://www.alkacon.com)
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

/**
 * Validation class which both translates a sitemap URL name and checks whether it already exists in a '|'-separated 
 * list of URL names which is passed as a configuration parameter.<p>
 * 
 * @author Georg Westenberger
 * 
 * @version $Revision: 1.3 $
 * 
 * @since 8.0.0
 */
public class CmsUrlNameValidationService implements I_CmsValidationService {

    /**
     * @see org.opencms.gwt.I_CmsValidationService#validate(org.opencms.file.CmsObject, java.lang.String, java.lang.String)
     */
    public CmsValidationResult validate(CmsObject cms, String value, String config) {

        //TODO: implement this correctly

        //
        //        CmsValidationResult result;
        //        CmsUUID id = new CmsUUID(config);
        //        CmsResource res = cms.readResource(id);
        //        String parentPath = CmsResource.getParentFolder(cms.getSitePath(res));
        //        
        //        if (res.getName().equals(value)) {
        //            return new CmsValidationREsult 
        //        }
        //        
        //        
        //        
        //        
        //        
        //        
        //        
        //        
        //         
        //        
        //        
        //        Set<String> otherUrlNames = new HashSet<String>(CmsStringUtil.splitAsList(config, "|"));
        //        String name = cms.getRequestContext().getFileTranslator().translateResource(value);
        //        name = name.replace('/', '_');
        //        if (otherUrlNames.contains(name)) {
        //            result = new CmsValidationResult(Messages.get().getBundle().key(
        //                Messages.ERR_URL_NAME_ALREADY_EXISTS_1,
        //                name));
        //        } else {
        //            result = new CmsValidationResult(null, name);
        //        }
        //        return result;
        String name = cms.getRequestContext().getFileTranslator().translateResource(value);
        name = name.replace('/', '_');
        return new CmsValidationResult(null, name);

    }
}
