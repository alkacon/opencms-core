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
 * For further information about Alkacon Software GmbH & Co. KG, please see the
 * company website: https://www.alkacon.com
 *
 * For further information about OpenCms, please see the
 * project website: https://www.opencms.org
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package org.opencms.xml.containerpage;

import org.opencms.file.CmsFile;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsResourceFilter;
import org.opencms.main.CmsException;
import org.opencms.main.CmsLog;
import org.opencms.relations.CmsLink;
import org.opencms.xml.CmsXmlException;
import org.opencms.xml.CmsXmlUtils;
import org.opencms.xml.content.CmsDefaultXmlContentHandler;
import org.opencms.xml.content.CmsXmlContent;
import org.opencms.xml.content.CmsXmlContentErrorHandler;
import org.opencms.xml.types.CmsXmlVarLinkValue;
import org.opencms.xml.types.CmsXmlVfsFileValue;
import org.opencms.xml.types.I_CmsXmlContentValue;

import java.util.Iterator;
import java.util.Locale;

import org.apache.commons.logging.Log;

/**
 * Container page handler to validate consistency.<p>
 *
 * @since 7.6
 */
public class CmsXmlContainerPageHandler extends CmsDefaultXmlContentHandler {

    /** Logger instance for this class. */
    private static final Log LOG = CmsLog.getLog(CmsXmlContainerPageHandler.class);

    /**
     * Creates a new instance.<p>
     */
    public CmsXmlContainerPageHandler() {

        super();
    }

    /**
     * @see org.opencms.xml.content.CmsDefaultXmlContentHandler#hasModifiableFormatters()
     */
    @Override
    public boolean hasModifiableFormatters() {

        return false;
    }

    /**
     * @see org.opencms.xml.content.CmsDefaultXmlContentHandler#prepareForWrite(org.opencms.file.CmsObject, org.opencms.xml.content.CmsXmlContent, org.opencms.file.CmsFile)
     */
    @Override
    public CmsFile prepareForWrite(CmsObject cms, CmsXmlContent content, CmsFile file) throws CmsException {

        Object attribute = cms.getRequestContext().getAttribute(CmsXmlContent.AUTO_CORRECTION_ATTRIBUTE);
        boolean autoCorrectionEnabled = (attribute != null) && ((Boolean)attribute).booleanValue();
        if (autoCorrectionEnabled) { // this is to ensure that 'touch' converts pages to the V12 format.
            CmsXmlContainerPage page = (CmsXmlContainerPage)content;
            try {
                page.writeContainerPage(cms, page.getContainerPage(cms));
            } catch (Exception e) {
                LOG.error(e.getLocalizedMessage(), e);
            }
        }
        return super.prepareForWrite(cms, content, file);
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
        if ((value != null)
            && CmsXmlUtils.removeXpath(value.getPath()).equals(CmsXmlContainerPage.XmlNode.Containers.name())) {
            CmsXmlContent content = (CmsXmlContent)value.getDocument();
            try {
                validateNames(cms, value, content);
            } catch (CmsXmlException e) {
                errorHandler.addError(value, e.getLocalizedMessage());
            }
        }

        return errorHandler;
    }

    /**
     * @see org.opencms.xml.content.CmsDefaultXmlContentHandler#validateLink(org.opencms.file.CmsObject, org.opencms.xml.types.I_CmsXmlContentValue, org.opencms.xml.content.CmsXmlContentErrorHandler)
     */
    @Override
    protected boolean validateLink(CmsObject cms, I_CmsXmlContentValue value, CmsXmlContentErrorHandler errorHandler) {

        // if there is a value of type file reference
        if ((value == null) || (!(value instanceof CmsXmlVfsFileValue) && !(value instanceof CmsXmlVarLinkValue))) {
            return false;
        }
        // if the value has a link (this will automatically fix, for instance, the path of moved resources)
        CmsLink link = null;
        if (value instanceof CmsXmlVfsFileValue) {
            link = ((CmsXmlVfsFileValue)value).getLink(cms);
        } else if (value instanceof CmsXmlVarLinkValue) {
            link = ((CmsXmlVarLinkValue)value).getLink(cms);
        }
        if ((link == null) || !link.isInternal()) {
            return false;
        }
        try {
            String sitePath = cms.getRequestContext().removeSiteRoot(link.getTarget());
            // validate the link for error
            cms.readResource(sitePath, CmsResourceFilter.IGNORE_EXPIRATION);

            // we handle expiration in the cms:container tag, so don't validate it here

        } catch (CmsException e) {
            if (errorHandler != null) {
                // generate error message
                errorHandler.addError(
                    value,
                    org.opencms.xml.content.Messages.get().getBundle(value.getLocale()).key(
                        org.opencms.xml.content.Messages.GUI_XMLCONTENT_CHECK_ERROR_0));
            }
            return true;
        }
        return false;
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
        String namePath = CmsXmlUtils.concatXpath(value.getPath(), CmsXmlContainerPage.XmlNode.Name.name());
        String name = content.getValue(namePath, locale).getStringValue(cms);
        // iterate over all containers
        Iterator<I_CmsXmlContentValue> itValues = content.getValues(
            CmsXmlContainerPage.XmlNode.Containers.name(),
            locale).iterator();
        while (itValues.hasNext()) {
            I_CmsXmlContentValue itValue = itValues.next();
            if (itValue.getPath().equals(value.getPath())) {
                // skip current container
                continue;
            }
            // get container name
            namePath = CmsXmlUtils.concatXpath(itValue.getPath(), CmsXmlContainerPage.XmlNode.Name.name());
            String itName = content.getValue(namePath, locale).getStringValue(cms);
            // validate
            if (name.equals(itName)) {
                throw new CmsXmlException(Messages.get().container(Messages.ERR_DUPLICATE_NAME_1, name));
            }
        }
    }
}
