/*
* File   : $Source: /alkacon/cvs/opencms/src/com/opencms/defaults/Attic/A_CmsNavBase.java,v $
* Date   : $Date: 2004/02/13 13:41:45 $
* Version: $Revision: 1.18 $
*
* This library is part of OpenCms -
* the Open Source Content Mananagement System
*
* Copyright (C) 2001  The OpenCms Group
*
* This library is free software; you can redistribute it and/or
* modify it under the terms of the GNU Lesser General Public
* License as published by the Free Software Foundation; either
* version 2.1 of the License, or (at your option) any later version.
*
* This library is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
* Lesser General Public License for more details.
*
* For further information about OpenCms, please see the
* OpenCms Website: http://www.opencms.org 
*
* You should have received a copy of the GNU Lesser General Public
* License along with this library; if not, write to the Free Software
* Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
*/

package com.opencms.defaults;

import org.opencms.file.*;
import org.opencms.main.*;

import com.opencms.core.*;
import com.opencms.template.*;
import java.util.*;

/**
 * This abstract class builds the default Navigation.
 *
 * @author Alexander Kandzior
 * @author Waruschan Babachan
 * @version $Revision: 1.18 $ $Date: 2004/02/13 13:41:45 $
 */
public abstract class A_CmsNavBase extends CmsXmlTemplate {
    protected static final String C_PROPERTY_NAVINDEX = "NavIndex";
    protected static final String C_NAVINDEX = "index.html";

    /**
     * Indicates if the results of this class are cacheable.
     *
     * @param cms CmsObject Object for accessing system resources
     * @param templateFile Filename of the template file
     * @param elementName Element name of this template in our parent template.
     * @param parameters Hashtable with all template class parameters.
     * @param templateSelector template section that should be processed.
     * @return <EM>true</EM> if cacheable, <EM>false</EM> otherwise.
     */
    public boolean isCacheable(CmsObject cms, String templateFile, String elementName, Hashtable parameters, String templateSelector) {
        return true;
    }

    /**
     * gets the current folder.
     *
     * @param cms CmsObject Object for accessing system resources.
     * @param tagcontent Unused in this special case of a user method. Can be ignored.
     * @param doc Reference to the A_CmsXmlContent object of the initiating XLM document.
     * @param userObject Hashtable with parameters.
     * @return byte[] with the content of this subelement.
     * @throws CmsException if something goes wrong
     */
    protected abstract Object getFolderCurrent(CmsObject cms, String tagcontent, A_CmsXmlContent doc, Object userObject) throws CmsException;

    /**
     * gets the parent folder.
     *
     * @param cms CmsObject Object for accessing system resources.
     * @param tagcontent Unused in this special case of a user method. Can be ignored.
     * @param doc Reference to the A_CmsXmlContent object of the initiating XLM document.
     * @param userObject Hashtable with parameters.
     * @return byte[] with the content of this subelement.
     * @throws CmsException if something goes wrong
     */
    protected abstract Object getFolderParent(CmsObject cms, String tagcontent, A_CmsXmlContent doc, Object userObject) throws CmsException;

    /**
     * gets the root folder.
     *
     * @param cms CmsObject Object for accessing system resources.
     * @param tagcontent Unused in this special case of a user method. Can be ignored.
     * @param doc Reference to the A_CmsXmlContent object of the initiating XLM document.
     * @param userObject Hashtable with parameters.
     * @return byte[] with the content of this subelement.
     * @throws CmsException if something goes wrong
     */
    protected abstract Object getFolderRoot(CmsObject cms, String tagcontent, A_CmsXmlContent doc, Object userObject) throws CmsException;

    /**
     * gets the navigation of current folder.
     *
     * @param cms CmsObject Object for accessing system resources.
     * @param tagcontent Unused in this special case of a user method. Can be ignored.
     * @param doc Reference to the A_CmsXmlContent object of the initiating XLM document.
     * @param userObject Hashtable with parameters.
     * @return byte[] with the content of this subelement.
     * @throws CmsException if something goes wrong
     */
    protected abstract Object getNavCurrent(CmsObject cms, String tagcontent, A_CmsXmlContent doc, Object userObject) throws CmsException;

    /**
     * gets the navigation of files and folders,
     * by folders it is showed closed, if the folder is clicked then it is opened.
     *
     * @param cms CmsObject Object for accessing system resources.
     * @param tagcontent Unused in this special case of a user method. Can be ignored.
     * @param doc Reference to the A_CmsXmlContent object of the initiating XLM document.
     * @param userObject Hashtable with parameters.
     * @return byte[] with the content of this subelement.
     * @throws CmsException if something goes wrong
     */
    protected abstract Object getNavFold(CmsObject cms, String tagcontent, A_CmsXmlContent doc, Object userObject) throws CmsException;

    /**
     * gets the navigation of specified level of parent folder.
     *
     * @param cms CmsObject Object for accessing system resources.
     * @param tagcontent Unused in this special case of a user method. Can be ignored.
     * @param doc Reference to the A_CmsXmlContent object of the initiating XLM document.
     * @param userObject Hashtable with parameters.
     * @return byte[] with the content of this subelement.
     * @throws CmsException if something goes wrong
     */
    protected abstract Object getNavParent(CmsObject cms, String tagcontent, A_CmsXmlContent doc, Object userObject) throws CmsException;

    /**
     * gets the navigation of root folder or parent folder starting from root folder.
     *
     * @param cms CmsObject Object for accessing system resources.
     * @param tagcontent Unused in this special case of a user method. Can be ignored.
     * @param doc Reference to the A_CmsXmlContent object of the initiating XLM document.
     * @param userObject Hashtable with parameters.
     * @return byte[] with the content of this subelement.
     * @throws CmsException if something goes wrong
     */
    protected abstract Object getNavRoot(CmsObject cms, String tagcontent, A_CmsXmlContent doc, Object userObject) throws CmsException;

    /**
     * gets the navigation of folders recursive.
     *
     * @param cms CmsObject Object for accessing system resources.
     * @param tagcontent Unused in this special case of a user method. Can be ignored.
     * @param doc Reference to the A_CmsXmlContent object of the initiating XLM document.
     * @param userObject Hashtable with parameters.
     * @return byte[] with the content of this subelement.
     * @throws CmsException if something goes wrong
     */
    protected abstract Object getNavTree(CmsObject cms, String tagcontent, A_CmsXmlContent doc, Object userObject) throws CmsException;

    /**
     * gets a specified property of current folder.
     *
     * @param cms A_CmsObject Object for accessing system resources.
     * @param tagcontent Unused in this special case of a user method. Can be ignored.
     * @param doc Reference to the A_CmsXmlContent object of the initiating XLM document.
     * @param userObject Hashtable with parameters.
     * @return byte[] with the content of this subelement.
     * @throws CmsException if something goes wrong
     */
    protected abstract Object getPropertyCurrent(CmsObject cms, String tagcontent, A_CmsXmlContent doc, Object userObject)
            throws CmsException;

    /**
     * gets a specified property of specified folder starting from current folder.
     *
     * @param cms A_CmsObject Object for accessing system resources.
     * @param tagcontent Unused in this special case of a user method. Can be ignored.
     * @param doc Reference to the A_CmsXmlContent object of the initiating XLM document.
     * @param userObject Hashtable with parameters.
     * @return byte[] with the content of this subelement.
     * @throws CmsException if something goes wrong
     */
    protected abstract Object getPropertyParent(CmsObject cms, String tagcontent, A_CmsXmlContent doc, Object userObject)
            throws CmsException;

    /**
     * gets a specified property of specified folder starting from root.
     *
     * @param cms A_CmsObject Object for accessing system resources.
     * @param tagcontent Unused in this special case of a user method. Can be ignored.
     * @param doc Reference to the A_CmsXmlContent object of the initiating XLM document.
     * @param userObject Hashtable with parameters.
     * @return byte[] with the content of this subelement.
     * @throws CmsException if something goes wrong
     */
    protected abstract Object getPropertyRoot(CmsObject cms, String tagcontent, A_CmsXmlContent doc, Object userObject)
            throws CmsException;


    /**
     * gets a specified property of uri.
     *
     * @param cms A_CmsObject Object for accessing system resources.
     * @param tagcontent Unused in this special case of a user method. Can be ignored.
     * @param doc Reference to the A_CmsXmlContent object of the initiating XLM document.
     * @param userObject Hashtable with parameters.
     * @return byte[] with the content of this subelement.
     * @throws CmsException if something goes wrong
     */
    protected abstract Object getPropertyUri(CmsObject cms, String tagcontent, A_CmsXmlContent doc, Object userObject)
            throws CmsException;

    /**
     * This method sorts the left and right side of unsorted partition.<p>
     *
     * @param left the left side of boundery in an array
     * @param right the right side of boundery in an array
     * @param pivot the pivot element
     * @param navLink Array of link path
     * @param navText Array of navigation text
     * @param navPos Array of navigation position
     * @return the next pivot
     */
    private int partitionIt(int left, int right, float pivot, String[] navLink, String[] navText, float[] navPos) {

        // left (after ++)
        int leftPtr = left - 1;

        // right-1 (after --)
        int rightPtr = right;
        while (true) {

            // find bigger item
            // nop
            while (navPos[++leftPtr] < pivot) {
            }

            // find smaller item
            // nop
            while (rightPtr > 0 && navPos[--rightPtr] > pivot) {
            }

            // if pointers cross, partition done
            // if pointers are not crossed, swap elements
            if (leftPtr >= rightPtr) {
                break;
            } else {
                swap(leftPtr, rightPtr, navLink, navText, navPos);
            }
        }

        // restore pivot
        swap(leftPtr, right, navLink, navText, navPos);

        // return pivot location
        return leftPtr;
    }

    /**
     * Sorts the navigation according to Quicksort method.
     *
     * @param left the left side of boundery in an array.
     * @param right the right side of boundery in an array.
     * @param navLink Array of link path.
     * @param navText Array of navigation text.
     * @param navPos Array of navigation position.
     */
    private void quickSort(int left, int right, String[] navLink, String[] navText, float[] navPos) {
        if (right - left <= 0) {
            return;
        } else {

            // rightmost item partition range
            float pivot = navPos[right];
            int partition = partitionIt(left, right, pivot, navLink, navText, navPos);

            // sort left side
            quickSort(left, partition - 1, navLink, navText, navPos);

            // sort right side
            quickSort(partition + 1, right, navLink, navText, navPos);
        }
    }

    /**
     * Sorts the navigation.
     *
     * @param size the size of array to be sorted.
     * @param navLink Array of link path.
     * @param navText Array of navigation text.
     * @param navPos Array of navigation position.
     */
    protected void sortNav(int size, String[] navLink, String[] navText, float[] navPos) {
        quickSort(0, size - 1, navLink, navText, navPos);
    }

    /**
     * Swapping the elements.<p>
     * 
     * @param dex1 first index
     * @param dex2 second index 
     * @param navLink array of nav links
     * @param navText array of nav texts
     * @param navPos array of nav positions
     */
    private void swap(int dex1, int dex2, String[] navLink, String[] navText, float[] navPos) {

        // swap positions
        float navPosTemp = navPos[dex1];
        navPos[dex1] = navPos[dex2];
        navPos[dex2] = navPosTemp;

        // swap titles
        String navTextTemp = navText[dex1];
        navText[dex1] = navText[dex2];
        navText[dex2] = navTextTemp;

        // swap foldername
        String navLinkTemp = navLink[dex1];
        navLink[dex1] = navLink[dex2];
        navLink[dex2] = navLinkTemp;
    }
}
