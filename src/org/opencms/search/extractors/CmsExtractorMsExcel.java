/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/search/extractors/CmsExtractorMsExcel.java,v $
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

import org.opencms.util.CmsStringUtil;

import java.io.InputStream;
import java.util.Iterator;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;

/**
 * Extracts the text form an MS Excel document.<p>
 * 
 * @author Alexander Kandzior (a.kandzior@alkacon.com)
 * 
 * @since 5.7.2
 */
public final class CmsExtractorMsExcel extends A_CmsTextExtractor {

    /** Static member instance of the extractor. */
    private static final CmsExtractorMsExcel m_instance = new CmsExtractorMsExcel();

    /**
     * Hide the public constructor.<p> 
     */
    private CmsExtractorMsExcel() {

        // noop
    }

    /**
     * Returns an instance of this text extractor.<p> 
     * 
     * @return an instance of this text extractor
     */
    public static I_CmsTextExtractor getExtractor() {

        return m_instance;
    }

    /**
     * @see org.opencms.search.extractors.I_CmsTextExtractor#extractText(java.io.InputStream, java.lang.String)
     */
    public I_CmsExtractionResult extractText(InputStream in, String encoding) throws Exception {

        HSSFWorkbook excelWb = new HSSFWorkbook(in);
        StringBuffer result = new StringBuffer(4096);
        
        int numberOfSheets = excelWb.getNumberOfSheets();
                
        for (int i = 0; i < numberOfSheets; i++) {
            HSSFSheet sheet = excelWb.getSheetAt(i);
            int numberOfRows = sheet.getPhysicalNumberOfRows();
            if (numberOfRows > 0) {
                
                if (CmsStringUtil.isNotEmpty(excelWb.getSheetName(i))) {
                    // append sheet name to content
                    if (i > 0) {
                        result.append("\n\n");
                    }
                    result.append(excelWb.getSheetName(i).trim());
                    result.append(":\n\n");                    
                }
                
                Iterator rowIt = sheet.rowIterator();
                while (rowIt.hasNext()) {
                    HSSFRow row = (HSSFRow)rowIt.next();
                    if (row != null) {
                        boolean hasContent = false;
                        Iterator it = row.cellIterator();
                        while (it.hasNext()) {
                            HSSFCell cell = (HSSFCell)it.next();
                            String text = null;
                            switch (cell.getCellType()) {
                                case HSSFCell.CELL_TYPE_BLANK:
                                case HSSFCell.CELL_TYPE_ERROR:
                                    // ignore all blank or error cells
                                    break;
                                case HSSFCell.CELL_TYPE_NUMERIC:
                                    try {
                                        text = Double.toString(cell.getNumericCellValue());
                                    } catch (Exception e) {
                                        // ignore this cell
                                    }
                                    break;
                                case HSSFCell.CELL_TYPE_STRING:
                                default:
                                    try {
                                        text = cell.getStringCellValue();
                                    } catch (Exception e) {
                                        // ignore this cell
                                    }
                                    break;
                            }
                            if (CmsStringUtil.isNotEmpty(text)) {
                                result.append(text.trim());
                                result.append(' ');
                                hasContent = true;
                            }
                        }
                        if (hasContent) {
                            // append a newline at the end of each row that has content                            
                            result.append('\n');
                        }
                    }
                }
            }
        }

        return new CmsExtractionResult(result.toString());
    }

}