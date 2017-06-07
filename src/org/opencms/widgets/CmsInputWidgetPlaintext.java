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
 * For further information about Alkacon Software GmbH & Co. KG, please see the
 * company website: http://www.alkacon.com
 *
 * For further information about OpenCms, please see the
 * project website: http://www.opencms.org
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package org.opencms.widgets;

import org.opencms.file.CmsObject;
import org.opencms.i18n.CmsEncoder;
import org.opencms.main.CmsLog;
import org.opencms.util.CmsHtmlExtractor;
import org.opencms.util.CmsStringUtil;

import java.io.UnsupportedEncodingException;
import java.util.Map;

import org.apache.commons.logging.Log;

import org.htmlparser.util.ParserException;

/**
 * {@link org.opencms.widgets.CmsInputWidget} that strips HTML Tags from the input before storing values.<p>
 *
 * @since 6.3.0
 *
 */
public final class CmsInputWidgetPlaintext extends CmsInputWidget {

    /** The log object for this class. */
    private static final Log LOG = CmsLog.getLog(CmsInputWidgetPlaintext.class);

    /**
     * Defcon.<p>
     */
    public CmsInputWidgetPlaintext() {

        super();
    }

    /**
     * @see org.opencms.widgets.CmsInputWidget#newInstance()
     */
    @Override
    public I_CmsWidget newInstance() {

        return new CmsInputWidgetPlaintext();
    }

    /**
     * @see org.opencms.widgets.A_CmsWidget#setEditorValue(org.opencms.file.CmsObject, java.util.Map, org.opencms.widgets.I_CmsWidgetDialog, org.opencms.widgets.I_CmsWidgetParameter)
     */
    @Override
    public void setEditorValue(
        CmsObject cms,
        Map<String, String[]> formParameters,
        I_CmsWidgetDialog widgetDialog,
        I_CmsWidgetParameter param) {

        String[] values = formParameters.get(param.getId());
        if ((values != null) && (values.length > 0)) {
            String value = values[0];
            if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(value)) {
                try {
                    value = CmsHtmlExtractor.extractText(value, CmsEncoder.ENCODING_UTF_8);
                } catch (ParserException e) {
                    if (LOG.isErrorEnabled()) {
                        LOG.error(
                            Messages.get().getBundle().key(Messages.LOG_ERR_WIDGET_PLAINTEXT_EXTRACT_HTML_1, value));
                    }
                } catch (UnsupportedEncodingException e) {
                    if (LOG.isErrorEnabled()) {
                        LOG.error(
                            Messages.get().getBundle().key(Messages.LOG_ERR_WIDGET_PLAINTEXT_EXTRACT_HTML_1, value));
                    }
                }
            } else {
                value = "";
            }
            param.setStringValue(cms, value);
        }
    }
}
