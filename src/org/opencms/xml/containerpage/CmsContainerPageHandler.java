/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/xml/containerpage/Attic/CmsContainerPageHandler.java,v $
 * Date   : $Date: 2009/10/13 11:59:40 $
 * Version: $Revision: 1.1.2.1 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (c) 2002 - 2009 Alkacon Software GmbH (http://www.alkacon.com)
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
 * For further information about Alkacon Software GmbH, please see the
 * company website: http://www.alkacon.com
 *
 * For further information about OpenCms, please see the
 * project website: http://www.opencms.org
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package org.opencms.xml.containerpage;

import org.opencms.file.CmsObject;
import org.opencms.xml.CmsXmlException;
import org.opencms.xml.CmsXmlUtils;
import org.opencms.xml.content.CmsDefaultXmlContentHandler;
import org.opencms.xml.content.CmsXmlContent;
import org.opencms.xml.content.CmsXmlContentErrorHandler;
import org.opencms.xml.types.I_CmsXmlContentValue;

import java.util.Iterator;
import java.util.Locale;

/**
 * Container page handler to validate consistency.<p>
 * 
 * @author Michael Moossen 
 * 
 * @version $Revision: 1.1.2.1 $
 * 
 * @since 7.6
 */
public class CmsContainerPageHandler extends CmsDefaultXmlContentHandler {

    /**
     * Creates a new instance.<p>
     */
    public CmsContainerPageHandler() {

        super();
    }

    /**
     * Validates container names, so that they are unique in the page.<p>
     * 
     * @param cms the cms context
     * @param value the value to validate
     * @param content the container page to validate
     * 
     * @throws CmsXmlException if there are duplicated names
     */
    protected void validateNames(CmsObject cms, I_CmsXmlContentValue value, CmsXmlContent content)
    throws CmsXmlException {

        // get the current name
        Locale locale = value.getLocale();
        String namePath = CmsXmlUtils.concatXpath(value.getPath(), CmsXmlContainerPage.N_NAME);
        String name = content.getValue(namePath, locale).getStringValue(cms);
        // iterate over all containers
        Iterator<I_CmsXmlContentValue> itValues = content.getValues(CmsXmlContainerPage.N_CONTAINER, locale).iterator();
        while (itValues.hasNext()) {
            I_CmsXmlContentValue itValue = itValues.next();
            if (itValue.getPath().equals(value.getPath())) {
                // skip current container
                continue;
            }
            // get container name
            namePath = CmsXmlUtils.concatXpath(itValue.getPath(), CmsXmlContainerPage.N_NAME);
            String itName = content.getValue(namePath, locale).getStringValue(cms);
            // validate
            if (name.equals(itName)) {
                throw new CmsXmlException(Messages.get().container(Messages.ERR_DUPLICATE_NAME_1, name));
            }
        }
    }

    /**
     * @see org.opencms.xml.content.I_CmsXmlContentHandler#resolveValidation(org.opencms.file.CmsObject, org.opencms.xml.types.I_CmsXmlContentValue, org.opencms.xml.content.CmsXmlContentErrorHandler)
     */
    @Override
    public CmsXmlContentErrorHandler resolveValidation(
        CmsObject cms,
        I_CmsXmlContentValue value,
        CmsXmlContentErrorHandler errorHandler) {

        if (errorHandler == null) {
            // init a new error handler if required
            errorHandler = new CmsXmlContentErrorHandler();
        }

        // we only have to validate containers
        if ((value != null) && CmsXmlUtils.removeXpath(value.getPath()).equals(CmsXmlContainerPage.N_CONTAINER)) {
            CmsXmlContent content = (CmsXmlContent)value.getDocument();
            try {
                validateNames(cms, value, content);
            } catch (CmsXmlException e) {
                errorHandler.addError(value, e.getLocalizedMessage());
            }
        }

        return errorHandler;
    }
}
