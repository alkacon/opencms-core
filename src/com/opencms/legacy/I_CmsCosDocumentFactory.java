/*
 * File   : $Source: /alkacon/cvs/opencms/src/com/opencms/legacy/Attic/I_CmsCosDocumentFactory.java,v $
 * Date   : $Date: 2005/02/18 15:18:52 $
 * Version: $Revision: 1.4 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Mananagement System
 *
 * Copyright (C) 2002  Alkacon Software (http://www.alkacon.com)
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
 
package com.opencms.legacy;

import org.opencms.search.documents.I_CmsDocumentFactory;

/**
 * Interface for lucene document factories used for OpenCms COS documents.<p>
 * 
 * @author Thomas Weckert (t.weckert@alkacon.com)
 * @version $Revision: 1.4 $
 * @since 5.3.6
 * 
 * @deprecated Will not be supported past the OpenCms 6 release.
 */
public interface I_CmsCosDocumentFactory extends I_CmsDocumentFactory {

    /** Channel of cos document. */
    String DOC_CHANNEL = "channel";
    
    /** Content id of cos document. */
    String DOC_CONTENT_ID = "contentid";

    /** Content definition of cos document. */
    String DOC_CONTENT_DEFINITION = "contentdefinition";

}
