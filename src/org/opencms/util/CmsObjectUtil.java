/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/util/Attic/CmsObjectUtil.java,v $
 * Date   : $Date: 2010/10/07 13:49:12 $
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

package org.opencms.util;

/**
 * Generic utility functions for dealing with object comparison, hash codes etc.<p>
 * 
 * @author Georg Westenberger
 * 
 * @version $Revision: 1.1 $
 * 
 * @since 8.0.0
 */
public final class CmsObjectUtil {

    /**
     * Hidden no-args constructor.<p>
     */
    private CmsObjectUtil() {

        // do nothing 
    }

    /**
     * Utility method for combining two hash codes into another hash code.<p>
     * 
     * @param hash1 the first hash code 
     * @param hash2 the second hash code
     * @return the resulting hash code 
     */
    public static int combineHashCodes(int hash1, int hash2) {

        return 50000429 * hash1 + hash2;
    }

    /**
     * Computes a hash code for a variable number of objects by combining their individual hash codes.<p>
     * 
     * @param objs the objects from which to compute the hash code 
     * 
     * @return the combined hash code for the objects 
     */
    public static int computeHashCode(Object... objs) {

        int result = 0;
        for (Object obj : objs) {
            int objHash = obj != null ? obj.hashCode() : 12345;
            result = combineHashCodes(result, objHash);
        }
        return result;
    }

    /**
     * Combines the hash codes of two objects.<p>
     * 
     * This also works if one or both of the arguments are null.<p>
     * 
     * @param o1 the first object 
     * @param o2 the second object
     *  
     * @return the combined hash code for the objects 
     */
    public static int computeHashCode(Object o1, Object o2) {

        int hash1 = o1 != null ? o1.hashCode() : 12345;
        int hash2 = o2 != null ? o2.hashCode() : 12345;
        return combineHashCodes(hash1, hash2);
    }

    /**
     * Checks whether two objects are equal.<p>
     * 
     * This will also return true if both arguments are null.<p>
     * 
     * @param a  the first object 
     * @param b the second object
     *  
     * @return true if either both a and b are null, or if a equals b
     */
    public static boolean equals(Object a, Object b) {

        return a == null ? b == null : a.equals(b);
    }
}
