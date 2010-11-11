/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/xml/content/Attic/CmsDefaultUrlNameSequenceGenerator.java,v $
 * Date   : $Date: 2010/11/11 13:08:17 $
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

package org.opencms.xml.content;

import org.opencms.file.CmsObject;
import org.opencms.file.CmsResource;
import org.opencms.main.OpenCms;
import org.opencms.xml.types.I_CmsXmlContentValue;

import java.util.Iterator;

/**
 * The default class used for generating sequences of URL name candidates from an XML content.<p>
 * 
 * @author Georg Westenberger
 * 
 * @version $Revision: 1.1 $
 * 
 * @since 8.0.0
 */
public class CmsDefaultUrlNameSequenceGenerator implements I_CmsUrlNameSequenceGenerator {

    /**
     * @see org.opencms.xml.content.I_CmsUrlNameSequenceGenerator#getUrlNameSequence(org.opencms.file.CmsObject, org.opencms.xml.content.CmsXmlContent, org.opencms.xml.types.I_CmsXmlContentValue, org.opencms.file.CmsResource)
     */
    public Iterator<String> getUrlNameSequence(
        CmsObject cms,
        CmsXmlContent content,
        I_CmsXmlContentValue value,
        CmsResource sibling) {

        String translatedTitle = OpenCms.getResourceManager().getFileTranslator().translateResource(
            value.getStringValue(cms)).replace("/", "_");
        return new CmsNumberSuffixNameSequence(translatedTitle);
    }

}
