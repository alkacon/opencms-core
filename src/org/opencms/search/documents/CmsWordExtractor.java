/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/search/documents/Attic/CmsWordExtractor.java,v $
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


import org.apache.poi.poifs.filesystem.*;
import org.apache.poi.util.LittleEndian;

import java.util.*;
import java.io.*;

/**
 * This class extracts the text from a Word 97/2000/XP word doc
 *
 * @author Ryan Ackley
 */
public class CmsWordExtractor {

  /**
   * Constructor
   */
  public CmsWordExtractor() {
      // empty
  }

  /**
   * Goes through the piece table and parses out the info regarding the text
   * blocks. For Word 97 and greater all text is stored in the "complex" way
   * because of unicode.
   *
   * @param tableStream buffer containing the main table stream.
   * @param complexOffset of the complex data.
   * @param text text
   * @return the text
   * @throws IOException if something goes wrong
   */
  private static int findText(byte[] tableStream, int complexOffset, ArrayList text) throws IOException
  {
    //actual text
    int pos = complexOffset;
    int multiple = 2;
    //skips through the prms before we reach the piece table. These contain data
    //for actual fast saved files
    while (tableStream[pos] == 1) {
        pos++;
        int skip = LittleEndian.getShort(tableStream, pos);
        pos += 2 + skip;
    }
    if (tableStream[pos] != 2) {
        throw new IOException("corrupted Word file");
    } else {
        //parse out the text pieces
        int pieceTableSize = LittleEndian.getInt(tableStream, ++pos);
        pos += 4;
        int pieces = (pieceTableSize - 4) / 12;
        for (int x = 0; x < pieces; x++) {
            int filePos = LittleEndian.getInt(tableStream, pos + ((pieces + 1) * 4) + (x * 8) + 2);
            boolean unicode = false;
            if ((filePos & 0x40000000) == 0) {
                unicode = true;
            } else {
                unicode = false;
                multiple = 1;
                filePos &= ~(0x40000000); //gives me FC in doc stream
                filePos /= 2;
            }
            int totLength = LittleEndian.getInt(tableStream, pos + (x + 1) * 4)
                            - LittleEndian.getInt(tableStream, pos + (x * 4));

            CmsWordTextPiece piece = new CmsWordTextPiece(filePos, totLength, unicode);
            text.add(piece);

        }

    }
    return multiple;
  }

  /**
   * Gets the text from a Word document.
   *
   * @param in The InputStream representing the Word file.
   * @return the text from a Word document
   * @throws Exception if something goes wrong
   */
  public String extractText(InputStream in) throws Exception {
    ArrayList text = new ArrayList();
    POIFSFileSystem fsys = new POIFSFileSystem(in);

    DocumentEntry headerProps =
        (DocumentEntry)fsys.getRoot().getEntry("WordDocument");
    DocumentInputStream din = fsys.createDocumentInputStream("WordDocument");
    byte[] header = new byte[headerProps.getSize()];

    din.read(header);
    din.close();
    //Get the information we need from the header
    int info = LittleEndian.getShort(header, 0xa);

    boolean useTable1 = (info & 0x200) != 0;

    //get the location of the piece table
    int complexOffset = LittleEndian.getInt(header, 0x1a2);


    String tableName = null;
    if (useTable1) {
      tableName = "1Table";
    } else {
      tableName = "0Table";
    }

    DocumentEntry table = (DocumentEntry)fsys.getRoot().getEntry(tableName);
    byte[] tableStream = new byte[table.getSize()];

    din = fsys.createDocumentInputStream(tableName);

    din.read(tableStream);
    din.close();

    din = null;
    fsys = null;
    table = null;
    headerProps = null;

    int multiple = findText(tableStream, complexOffset, text);

    StringBuffer sb = new StringBuffer();
    int size = text.size();
    tableStream = null;

    for (int x = 0; x < size; x++) {
      CmsWordTextPiece nextPiece = (CmsWordTextPiece)text.get(x);
      int start = nextPiece.getStart();
      int length = nextPiece.getLength();

      boolean unicode = nextPiece.usesUnicode();
      String toStr = null;
      if (unicode) {
        toStr = new String(header, start, length * multiple, "UTF-16LE");
      } else {
        toStr = new String(header, start, length , "ISO-8859-1");
      }
      sb.append(toStr).append(" ");

    }
    return sb.toString();
  }

}