package com.opencms.modules.search.lucene;

/*
 *  $RCSfile: IndexDirectory.java,v $
 *  $Author: g.huhn $
 *  $Date: 2002/02/13 14:42:10 $
 *  $Revision: 1.1 $
 *
 *  Copyright (c) 2002 FRAMFAB Deutschland AG. All Rights Reserved.
 *
 *  THIS SOFTWARE IS NEITHER FREEWARE NOR PUBLIC DOMAIN!
 *
 *  To use this software you must purchease a licencse from Framfab.
 *  In order to use this source code, you need written permission from
 *  Framfab. Redistribution of this source code, in modified or
 *  unmodified form, is not allowed.
 *
 *  FRAMFAB MAKES NO REPRESENTATIONS OR WARRANTIES ABOUT THE SUITABILITY
 *  OF THIS SOFTWARE, EITHER EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED
 *  TO THE IMPLIED WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR
 *  PURPOSE, OR NON-INFRINGEMENT. FRAMFAB SHALL NOT BE LIABLE FOR ANY
 *  DAMAGES SUFFERED BY LICENSEE AS A RESULT OF USING, MODIFYING OR
 *  DISTRIBUTING THIS SOFTWARE OR ITS DERIVATIVES.
 */
import org.apache.lucene.index.IndexWriter;

/**
 *  Description of the Class
 *
 *@author     grehuh
 *@created    13. Februar 2002
 */
public class IndexDirectory {
    // the debug flag
    private final static boolean debug = false;


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
    public static void createIndexDirectory(String indexPath) throws Exception {
        IndexWriter writer = null;
        try {
            writer = new IndexWriter(indexPath, null, true);
            if (debug) {
                System.out.println("Index " + indexPath + " created");
            }
        }
        catch (Exception e) {
            throw e;
        }
        finally {
            writer.close();
        }
    }
}
