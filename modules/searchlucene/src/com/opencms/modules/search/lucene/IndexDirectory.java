package com.opencms.modules.search.lucene;

/*
    $RCSfile: IndexDirectory.java,v $
    $Date: 2003/03/25 14:48:28 $
    $Revision: 1.5 $
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
import org.apache.lucene.index.IndexWriter;
import java.io.*;

/**
 *  Class to create a new index directory.
 *
 *@author     grehuh
 *@created    13. Februar 2002
 */
public class IndexDirectory {
    // the debug flag
    private final static boolean debug = true;


    /**
     *  Constructor for the IndexDirectory object
     */
    public IndexDirectory() { }


    // usage: CreateIndex <index-directory>

    /**
     *  Description of the Method
     *
     *@param  indexPath      Description of the Parameter
     *@exception  Exception  Description of the Exception
     */
    protected static void createIndexDirectory(String indexPath)  {
        IndexWriter writer = null;
        if(debug) {
            System.err.println("Index " + indexPath + " created");
        }
        try {
            writer = new IndexWriter(indexPath, null, true);

        } catch(IOException e) {
            e.printStackTrace();
        }
        try {
            writer.close();
        }catch (IOException ex) {
            ex.printStackTrace();
        }
    }
}
