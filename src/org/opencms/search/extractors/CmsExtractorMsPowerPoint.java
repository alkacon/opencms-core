/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/search/extractors/CmsExtractorMsPowerPoint.java,v $
 * Date   : $Date: 2005/07/29 10:35:06 $
 * Version: $Revision: 1.7 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Mananagement System
 *
 * Copyright (c) 2005 Alkacon Software GmbH (http://www.alkacon.com)
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

import java.io.InputStream;
import java.util.Map;

import org.apache.poi.poifs.eventfilesystem.POIFSReader;
import org.apache.poi.poifs.eventfilesystem.POIFSReaderEvent;
import org.apache.poi.poifs.eventfilesystem.POIFSReaderListener;
import org.apache.poi.poifs.filesystem.DocumentInputStream;
import org.apache.poi.util.LittleEndian;

/**
 * Extracts the text form an MS PowerPoint document.<p>
 * 
 * @author Alexander Kandzior 
 * 
 * @version $Revision: 1.7 $ 
 * 
 * @since 6.0.0 
 */
public final class CmsExtractorMsPowerPoint extends A_CmsTextExtractorMsOfficeBase implements POIFSReaderListener {

    /** The buffer that is written with the content of the PPT. */
    private StringBuffer m_buffer;

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
    public I_CmsExtractionResult extractText(InputStream in, String encoding) throws Exception {

        POIFSReader reader = new POIFSReader();
        reader.registerListener(this);
        reader.read(in);
        
        // extract all information
        Map metaInfo = extractMetaInformation();
        String result = removeControlChars(m_buffer.toString());

        // free some memory
        m_buffer = null;
        cleanup();

        // return the final result
        return new CmsExtractionResult(result, metaInfo);
    }

    /**
     * @see org.apache.poi.poifs.eventfilesystem.POIFSReaderListener#processPOIFSReaderEvent(org.apache.poi.poifs.eventfilesystem.POIFSReaderEvent)
     */
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

                String encoding = null;
                switch (type) {
                    case PPT_TEXTBYTE_ATOM:
                        // this pice is single-byte encoded, let's assume Cp1252 since this is most likley
                        // anyone who knows how to find out the "right" encoding - please email me
                        encoding = ENCODING_CP1252;
                    case PPT_TEXTCHAR_ATOM:
                        if (encoding == null) {
                            // this piece is double-byte encoded, use UTF-16
                            encoding = ENCODING_UTF16;
                        }
                        int start = i + 4 + 1;
                        int end = start + size;

                        byte[] buf = new byte[size];
                        System.arraycopy(buffer, start, buf, 0, buf.length);

                        m_buffer.append(CmsEncoder.createString(buf, encoding));
                        i = end;
                    default:
                // noop                                           
                }
            }
        } catch (Exception e) {
            // ignore
        }
    }
}