/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/search/documents/Attic/CmsWordTextPiece.java,v $
 * Date   : $Date: 2004/02/11 15:58:55 $
 * Version: $Revision: 1.1 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Mananagement System
 *
 * Copyright (C) 2002 - 2004 Alkacon Software (http://www.alkacon.com)
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the m_terms of the GNU Lesser General Public
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
package org.opencms.search.documents;

/**
 * This class stores info about the data structure describing a run of text.
 * 
 * @author
 */

class CmsWordTextPiece {
    
    private int m_fcStart;
    private int m_length;
    private boolean m_usesUnicode;

    /**
     * @param start start
     * @param length length
     * @param unicode true if uses unicode
     */
    public CmsWordTextPiece(int start, int length, boolean unicode) {
        m_usesUnicode = unicode;
        m_length = length;
        m_fcStart = start;
    }
    
    /**
     * @return the length
     */
    public int getLength() {
        return m_length;
    }

    /**
     * @return the start
     */
    public int getStart() {
        return m_fcStart;
    }
    
    /**
     * @return true if uses unicode
     */
    public boolean usesUnicode() {
        return m_usesUnicode;
    }

}