package com.opencms.modules.search.lucene;

/*
    $RCSfile: I_ContentParser.java,v $
    $Date: 2002/02/28 13:00:11 $
    $Revision: 1.3 $
    Copyright (C) 2000  The OpenCms Group
    This File is part of OpenCms -
    the Open Source Content Mananagement System
    This program is free software; you can redistribute it and/or
    modify it under the terms of the GNU General Public License
    as published by the Free Software Foundation; either version 2
    of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.
    For further information about OpenCms, please see the
    OpenCms Website: http://www.opencms.com
    You should have received a copy of the GNU General Public License
    long with this program; if not, write to the Free Software
    Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
  */
import java.io.*;

/**
 *  Interface to connect different parsers to the searchmodul.
 *
 *@author     grehuh
 *@created    28. Februar 2002
 */
public interface I_ContentParser {
    /**
     *  Gets the description attribute of the I_ContentParser object
     *
     *@return    The description value
     */
    String getDescription();


    /**
     *  Gets the keywords attribute of the I_ContentParser object
     *
     *@return    The keywords value
     */
    String getKeywords();


    /**
     *  Gets the contents attribute of the I_ContentParser object
     *
     *@return    The contents value
     */
    String getContents();


    /**
     *  Gets the title attribute of the I_ContentParser object
     *
     *@return    The title value
     */
    String getTitle();


    /**
     *  Description of the Method
     *
     *@param  is  Description of the Parameter
     */
    void parse(InputStream is);


    /**
     *  Gets the published attribute of the I_ContentParser object
     *
     *@return    The published value
     */
    String getPublished();
}
