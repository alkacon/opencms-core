/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/search/extractors/CmsExtractorMsPowerPoint.java,v $
 * Date   : $Date: 2005/03/23 19:08:22 $
 * Version: $Revision: 1.1 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Mananagement System
 *
 * Copyright (C) 2002 - 2005 Alkacon Software (http://www.alkacon.com)
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

package org.opencms.search.extractors;

import org.opencms.i18n.CmsEncoder;

import java.io.InputStream;

import org.apache.poi.poifs.eventfilesystem.POIFSReader;
import org.apache.poi.poifs.eventfilesystem.POIFSReaderEvent;
import org.apache.poi.poifs.eventfilesystem.POIFSReaderListener;
import org.apache.poi.poifs.filesystem.DocumentInputStream;
import org.apache.poi.util.LittleEndian;

/**
 * Extracts the text form an MS PowerPoint document.<p>
 * 
 * @author Alexander Kandzior (a.kandzior@alkacon.com)
 * 
 * @since 5.7.2
 */
public final class CmsExtractorMsPowerPoint extends A_CmsTextExtractor implements POIFSReaderListener {
    
    /** PPT text byte atom. */    
    public static final int PPT_TEXTBYTE_ATOM = 4008;    
    
    /** PPT text char atom. */
    public static final int PPT_TEXTCHAR_ATOM = 4000;

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
        
        String result = removeControlChars(m_buffer.toString());
        return new CmsExtractionResult(result);
    }

    /**
     * @see org.apache.poi.poifs.eventfilesystem.POIFSReaderListener#processPOIFSReaderEvent(org.apache.poi.poifs.eventfilesystem.POIFSReaderEvent)
     */
    public void processPOIFSReaderEvent(POIFSReaderEvent event) {

        try {

            DocumentInputStream input = event.getStream();

            // make sue this is a PPT document
            if (!event.getName().startsWith("PowerPoint Document")) {
                return;
            }
            
            byte[] buffer = new byte[input.available()];
            input.read(buffer, 0, input.available());

            for (int i = 0; i < buffer.length - 20; i++) {
                int type = LittleEndian.getUShort(buffer, i + 2);
                int size = (int)LittleEndian.getUInt(buffer, i + 4) + 3;                                
                
                String encoding = null;                
                switch (type) {                    
                    case PPT_TEXTBYTE_ATOM:
                        // this pice is single-byte encoded, let's assume Cp1252 since this is most likley
                        encoding = "Cp1252";
                    case PPT_TEXTCHAR_ATOM:
                        if (encoding == null) {
                            // this piece is double-byte encoded, use UTF-16 (don't know what else to use)
                            encoding = "UTF-16";
                        }
                        int start = i + 4 + 1;
                        int end = start + size;
                        
                        byte[] buf = new byte[size];                    
                        System.arraycopy(buffer, start, buf, 0, buf.length);
                        
                        // TODO: figure out what encoding PPT uses here - UTF-16 seems to be the best guess
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