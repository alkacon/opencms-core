package com.opencms.modules.search.lucene;

/*
 *  $RCSfile: I_ContentParser.java,v $
 *  $Author: g.huhn $
 *  $Date: 2002/02/26 14:02:46 $
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

 /**
 * <p><code>I_ContentParser</code>
 * Interface for Content Handler/Parsers.
 * </p>
 *
 * @author
 * @version 1.0
 */
import java.io.*;

public interface I_ContentParser {
    String getDescription();
    String getKeywords();
    String getContents();
    String getTitle();
    void parse(InputStream is);
}
