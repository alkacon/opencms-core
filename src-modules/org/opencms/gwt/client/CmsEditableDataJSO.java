/*
 * File   : $Source: /alkacon/cvs/opencms/src-modules/org/opencms/gwt/client/Attic/CmsEditableDataJSO.java,v $
 * Date   : $Date: 2011/04/21 11:50:17 $
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

package org.opencms.gwt.client;

import com.google.gwt.core.client.JavaScriptObject;

/**
 * Javascript overlay object holding data needed to edit a content collector list element.<p>
 * 
 * @author Tobias Herrmann
 * 
 * @version $Revision: 1.1 $
 * 
 * @since 8.0.0
 */
public class CmsEditableDataJSO extends JavaScriptObject {

    /**
     * Constructor.<p>
     */
    protected CmsEditableDataJSO() {

        // nothing to do
    }

    /**
     * Parses the given JSON text and returns the editable data object.<p>
     * 
     * @param jsonText the JSON text to parse
     * 
     * @return the data object
     */
    public static final native CmsEditableDataJSO parseEditableData(String jsonText) /*-{
        return eval('('+jsonText+')');
    }-*/;

    /**
     * Returns the edit id.<p>
     * 
     * @return the edit id
     */
    public final native String getEditId() /*-{
        return this.editId;
    }-*/;

    /** 
     * Returns the element language.<p>
     * 
     * @return the element language
     */
    public final native String getElementLanguage() /*-{
        return this.elementlanguage;
    }-*/;

    /**
     * Returns the element name.<p>
     * 
     * @return the element name
     */
    public final native String getElementName() /*-{
        return this.elementname;
    }-*/;

    /**
     * Returns the new link url.<p>
     * 
     * @return the new link url
     */
    public final native String getNewLink() /*-{
        return this.newlink;
    }-*/;

    /**
     * Returns the site path.<p>
     * 
     * @return the site path
     */
    public final native String getSitePath() /*-{
        return this.sitePath;
    }-*/;

    /**
     * Returns the structure id.<p>
     * 
     * @return the structure id
     */
    public final native String getStructureId() /*-{
        return this.structureId;
    }-*/;

    /**
     * Returns if the delete button should be present.<p>
     * 
     * @return <code>true</code> if the delete button should be present
     */
    public final native boolean hasDelete() /*-{
        return this.hasDelete;
    }-*/;

    /**
     * Returns if the edit button should be present.<p>
     * 
     * @return <code>true</code> if the edit button should be present
     */
    public final native boolean hasEdit() /*-{
        return this.hasEdit;
    }-*/;

    /**
     * Returns if the new button should be present.<p>
     * 
     * @return <code>true</code> if the new button should be present
     */
    public final native boolean hasNew() /*-{
        return this.hasNew;
    }-*/;

}
