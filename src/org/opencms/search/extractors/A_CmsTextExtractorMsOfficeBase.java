/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/search/extractors/A_CmsTextExtractorMsOfficeBase.java,v $
 * Date   : $Date: 2011/03/23 14:51:16 $
 * Version: $Revision: 1.14 $
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

import org.opencms.util.CmsStringUtil;

import java.util.HashMap;
import java.util.Map;

import org.apache.poi.hpsf.DocumentSummaryInformation;
import org.apache.poi.hpsf.PropertySetFactory;
import org.apache.poi.hpsf.Section;
import org.apache.poi.hpsf.SummaryInformation;
import org.apache.poi.hpsf.wellknown.PropertyIDMap;
import org.apache.poi.poifs.eventfilesystem.POIFSReaderEvent;
import org.apache.poi.poifs.eventfilesystem.POIFSReaderListener;

/**
 * Base class to extract summary information from MS office documents.<p>
 * 
 * @author Alexander Kandzior 
 * 
 * @version $Revision: 1.14 $ 
 * 
 * @since 6.0.0 
 */
public abstract class A_CmsTextExtractorMsOfficeBase extends A_CmsTextExtractor implements POIFSReaderListener {

    /** Windows Cp1252 endocing (western europe) is used as default for single byte fields. */
    protected static final String ENCODING_CP1252 = "Cp1252";

    /** UTF-16 encoding is used for double byte fields. */
    protected static final String ENCODING_UTF16 = "UTF-16";

    /** Event event name for a MS PowerPoint document. */
    protected static final String POWERPOINT_EVENT_NAME = "PowerPoint Document";

    /** PPT text byte atom. */
    protected static final int PPT_TEXTBYTE_ATOM = 4008;

    /** PPT text char atom. */
    protected static final int PPT_TEXTCHAR_ATOM = 4000;

    /** The summary of the POI document. */
    private DocumentSummaryInformation m_documentSummary;

    /** The summary of the POI document. */
    private SummaryInformation m_summary;

    /**
     * @see org.apache.poi.poifs.eventfilesystem.POIFSReaderListener#processPOIFSReaderEvent(org.apache.poi.poifs.eventfilesystem.POIFSReaderEvent)
     */
    public void processPOIFSReaderEvent(POIFSReaderEvent event) {

        try {
            if ((m_summary == null) && event.getName().startsWith(SummaryInformation.DEFAULT_STREAM_NAME)) {
                m_summary = (SummaryInformation)PropertySetFactory.create(event.getStream());
                return;
            }
            if ((m_documentSummary == null)
                && event.getName().startsWith(DocumentSummaryInformation.DEFAULT_STREAM_NAME)) {
                m_documentSummary = (DocumentSummaryInformation)PropertySetFactory.create(event.getStream());
                return;
            }
        } catch (RuntimeException e) {
            // ignore            
        } catch (Exception e) {
            // ignore
        }
    }

    /**
     * Cleans up some internal memory.<p> 
     */
    protected void cleanup() {

        m_summary = null;
        m_documentSummary = null;
    }

    /**
     * Creates the extraction result for this MS Office document.<p>
     * 
     * The extraction result contains the raw content, plus additional meta information 
     * as content items read from the MS Office document properties.<p>
     * 
     * @param rawContent the raw content extracted from the document
     * 
     * @return the extraction result for this MS Office document
     */
    protected I_CmsExtractionResult createExtractionResult(String rawContent) {

        Map<String, String> contentItems = new HashMap<String, String>();
        if (CmsStringUtil.isNotEmpty(rawContent)) {
            contentItems.put(I_CmsExtractionResult.ITEM_RAW, rawContent);
        }

        StringBuffer content = new StringBuffer(rawContent);

        if (m_summary != null) {
            // can't use convenience methods on summary since they can't deal with multiple sections
            Section section = (Section)m_summary.getSections().get(0);
            combineContentItem(
                (String)section.getProperty(PropertyIDMap.PID_TITLE),
                I_CmsExtractionResult.ITEM_TITLE,
                content,
                contentItems);
            combineContentItem(
                (String)section.getProperty(PropertyIDMap.PID_KEYWORDS),
                I_CmsExtractionResult.ITEM_KEYWORDS,
                content,
                contentItems);
            combineContentItem(
                (String)section.getProperty(PropertyIDMap.PID_SUBJECT),
                I_CmsExtractionResult.ITEM_SUBJECT,
                content,
                contentItems);
            combineContentItem(
                (String)section.getProperty(PropertyIDMap.PID_COMMENTS),
                I_CmsExtractionResult.ITEM_COMMENTS,
                content,
                contentItems);
            combineContentItem(
                (String)section.getProperty(PropertyIDMap.PID_AUTHOR),
                I_CmsExtractionResult.ITEM_AUTHOR,
                content,
                contentItems);
        }
        if (m_documentSummary != null) {
            // can't use convenience methods on document since they can't deal with multiple sections
            Section section = (Section)m_documentSummary.getSections().get(0);
            // extract available meta information from document summary
            combineContentItem(
                (String)section.getProperty(PropertyIDMap.PID_COMPANY),
                I_CmsExtractionResult.ITEM_COMPANY,
                content,
                contentItems);
            combineContentItem(
                (String)section.getProperty(PropertyIDMap.PID_MANAGER),
                I_CmsExtractionResult.ITEM_MANAGER,
                content,
                contentItems);
            combineContentItem(
                (String)section.getProperty(PropertyIDMap.PID_CATEGORY),
                I_CmsExtractionResult.ITEM_CATEGORY,
                content,
                contentItems);
        }

        // free some memory
        cleanup();

        return new CmsExtractionResult(content.toString(), contentItems);
    }
}