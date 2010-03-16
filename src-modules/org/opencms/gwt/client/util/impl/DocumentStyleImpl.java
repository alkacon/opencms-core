/*
 * File   : $Source: /alkacon/cvs/opencms/src-modules/org/opencms/gwt/client/util/impl/Attic/DocumentStyleImpl.java,v $
 * Date   : $Date: 2010/03/16 07:55:34 $
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

package org.opencms.gwt.client.util.impl;

import com.google.gwt.dom.client.Element;

public class DocumentStyleImpl {

    public String getCurrentStyle(Element elem, String name) {

        name = hyphenize(name);
        String propVal = getComputedStyle(elem, name, null);
        if ("opacity".equals(name) && ((propVal == null) || (propVal.trim().length() == 0))) {
            propVal = "1";
        }
        return propVal;
    }

    public String getPropertyName(String name) {

        if ("float".equals(name)) {
            return "cssFloat";
        } else if ("class".equals(name)) {
            return "className";
        } else if ("for".equals(name)) {
            return "htmlFor";
        }
        return camelize(name);
    }

    protected native String hyphenize(String name) /*-{
        return name.replace( /([A-Z])/g, "-$1" ).toLowerCase();
    }-*/;

    private native String getComputedStyle(Element elem, String name, String pseudo) /*-{
        var cStyle = $doc.defaultView.getComputedStyle( elem, pseudo );
        return cStyle ? cStyle.getPropertyValue( name ) : null;
    }-*/;

    protected static native String camelize(String s)/*-{
        return s.replace(/\-(\w)/g, function(all, letter){
        return letter.toUpperCase();
        });
    }-*/;

}
