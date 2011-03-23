/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/search/extractors/CmsExtractorMsPowerPoint.java,v $
 * Date   : $Date: 2011/03/23 14:51:16 $
 * Version: $Revision: 1.18 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (c) 2002 - 2011 Alkacon Software GmbH (http://www.alkacon.com)
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

package org.opencms.search.extractors;

import org.opencms.i18n.CmsEncoder;
import org.opencms.main.CmsLog;

import java.io.InputStream;

import org.apache.commons.logging.Log;
import org.apache.poi.poifs.eventfilesystem.POIFSReader;
import org.apache.poi.poifs.eventfilesystem.POIFSReaderEvent;
import org.apache.poi.poifs.filesystem.DocumentInputStream;
import org.apache.poi.util.LittleEndian;

/**
 * Extracts the text from an MS PowerPoint document.<p>
 * 
 * @author Alexander Kandzior 
 * 
 * @version $Revision: 1.18 $ 
 * 
 * @since 6.0.0 
 */
public final class CmsExtractorMsPowerPoint extends A_CmsTextExtractorMsOfficeBase {

    /** The buffer that is written with the content of the PPT. */
    private StringBuffer m_buffer;

    /** The log object for this class. */
    private static final Log LOG = CmsLog.getLog(CmsExtractorMsPowerPoint.class);

    /**
     * Hide the public constructor.<p> 
     */
    private CmsExtractorMsPowerPoint() {

        m_buffer = new StringBuffer(4096);
    }

    /**
     * Returns an instance of this text extractor.<p> 
     * 
     * @return an instance of this text extractor
     */
    public static I_CmsTextExtractor getExtractor() {

        // since this extractor requires a member variable we have no static instance
        return new CmsExtractorMsPowerPoint();
    }

    /** 
     * @see org.opencms.search.extractors.I_CmsTextExtractor#extractText(java.io.InputStream, java.lang.String)
     */
    @Override
    public I_CmsExtractionResult extractText(InputStream in, String encoding) throws Exception {

        String rawContent = "";
        try {
            POIFSReader reader = new POIFSReader();
            reader.registerListener(this);
            reader.read(in);

            // extract all information
            rawContent = removeControlChars(m_buffer.toString());
            // free buffer memory
            m_buffer = new StringBuffer(4096);
        } catch (Exception e) {
            if (LOG.isErrorEnabled()) {
                LOG.error(Messages.get().container(Messages.LOG_EXTRACT_TEXT_ERROR_0), e);
            }
        }
        // combine the meta information with the content and create the result
        return createExtractionResult(rawContent);
    }

    /**
     * @see org.apache.poi.poifs.eventfilesystem.POIFSReaderListener#processPOIFSReaderEvent(org.apache.poi.poifs.eventfilesystem.POIFSReaderEvent)
     */
    @Override
    public void processPOIFSReaderEvent(POIFSReaderEvent event) {

        try {

            // super implementation handles document summary
            super.processPOIFSReaderEvent(event);

            // make sue this is a PPT document
            if (!event.getName().startsWith(POWERPOINT_EVENT_NAME)) {
                return;
            }

            DocumentInputStream input = event.getStream();
            byte[] buffer = new byte[input.available()];
            input.read(buffer, 0, input.available());

            for (int i = 0; i < buffer.length - 20; i++) {
                int type = LittleEndian.getUShort(buffer, i + 2);
                int size = (int)LittleEndian.getUInt(buffer, i + 4) + 3;

                switch (type) {
                    case PPT_TEXTBYTE_ATOM:
                        // this pice is single-byte encoded, let's assume Cp1252 since this is most likley
                        // anyone who knows how to find out the "right" encoding - please email me
                        i = appendTextChars(ENCODING_CP1252, i, size, buffer);
                        break;
                    case PPT_TEXTCHAR_ATOM:
                        i = appendTextChars(ENCODING_UTF16, i, size, buffer);
                        break;
                    default:
                        // noop                                           
                }
            }
        } catch (RuntimeException e) {
            // ignore
        } catch (Exception e) {
            // ignore
        }
    }

    /**
     * Append the next char to the result buffer.<p>
     */
    private int appendTextChars(String encoding, int i, int size, byte[] buffer) {

        if (encoding == null) {
            // this piece is double-byte encoded, use UTF-16
            encoding = ENCODING_UTF16;
        }
        int start = i + 4 + 1;
        int end = start + size;

        byte[] buf = new byte[size];
        System.arraycopy(buffer, start, buf, 0, buf.length);

        m_buffer.append(CmsEncoder.createString(buf, encoding));
        return end;
    }

}