/**
 * The contents of this file are subject to the OpenXML Public
 * License Version 1.0; you may not use this file except in
 * compliance with the License. You may obtain a copy of the
 * License at http://www.openxml.org/license/
 *
 * THIS SOFTWARE AND DOCUMENTATION IS PROVIDED ON AN "AS IS" BASIS
 * WITHOUT WARRANTY OF ANY KIND EITHER EXPRESSED OR IMPLIED,
 * INCLUDING AND WITHOUT LIMITATION, WARRANTIES THAT THE SOFTWARE
 * AND DOCUMENTATION IS FREE OF DEFECTS, MERCHANTABLE, FIT FOR A
 * PARTICULAR PURPOSE OR NON-INFRINGING. SEE THE LICENSE FOR THE
 * SPECIFIC LANGUAGE GOVERNING RIGHTS AND LIMITATIONS UNDER THE
 * LICENSE.
 *
 * The Initial Developer of this code under the License is
 * OpenXML.org. Portions created by OpenXML.org and/or Assaf Arkin
 * are Copyright (C) 1998, 1999 OpenXML.org. All Rights Reserved.
 *
 * $Id: ElementState.java,v 1.2 2000/02/20 16:11:19 a.lucas Exp $
 */


package source.org.openxml.printer;


/**
 * Holds the state of the currently printing element.
 *
 *
 * @version $Revision: 1.2 $ $Date: 2000/02/20 16:11:19 $
 * @author <a href="mailto:arkin@openxml.org">Assaf Arkin</a>
 * @see BasePrinter
 */
class ElementState
{


    /**
     * The element's tag name.
     */
    String tagName;


    /**
     * True if element is space preserving.
     */
    boolean preserveSpace;


    /**
     * True if element is empty. Turns false immediately
     * after printing the first contents of the element.
     */
    boolean empty;


    /**
     * True if the last printed node was an element node.
     */
    boolean afterElement;


}
