package com.opencms.modules.search.lucene;

/*
 *  $RCSfile: PdfParser.java,v $
 *  $Author: g.huhn $
 *  $Date: 2002/02/26 16:16:48 $
 *  $Revision: 1.3 $
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
 * <p><code>PdfParser</code>
 *	Content Parser for PDF documents.
 * </p>
 *
 * @author
 * @version 1.0
 */
import java.util.zip.InflaterInputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.io.*;
/**
 * Insert the type's description here.
 * Creation date: (2/21/2001 7:19:20 PM)
 * @author:
 */

import java.util.List;


public class PdfParser implements I_ContentParser{

    private InputStream in;
    private static final boolean debug=false;

    /*
     * Input cache.  This is much faster than calling down to a synchronized
     * method of BufferedReader for each byte.  Measurements done 5/30/97
     * show that there's no point in having a bigger buffer:  Increasing
     * the buffer to 8192 had no measurable impact for a program discarding
     * one character at a time (reading from an http URL to a local machine).
     */
    private byte buf[] = new byte[256];
    private int pos;
    private int len;
    /*
    tracks position relative to the beginning of the
    document.
     */
    private int currentPosition;

    // 1996.07.10 15:08:56 PST
    SimpleDateFormat dateFormatter;

    // Content Data
    private String author;
    private long published;
    private String keywords;
    private String description;
    private String title;
    private StringBuffer contents;

    // Flags
    private boolean streamHit = false;
    private boolean parseNextStream = false;

    // Compression
    private static final int NONE = 0;
    private static final int FLATE = 1;
    private static final int LZW = 2;
    private int compression = NONE;


    // TOKENS
    private static final char[] AUTHOR = "/Author".toCharArray();
    private static final char[] CREATIONDATE = "/CreationDate".toCharArray();
    private static final char[] ENDSTREAM = "endstream".toCharArray();
    private static final char[] KEYWORDS = "/Keywords".toCharArray();
    private static final char[] STREAM = "stream".toCharArray();
    private static final char[] SUBJECT = "/Subject".toCharArray();
    private static final char[] TITLE = "/Title".toCharArray();
    private static final char[] NEWLINE = {'\n'};
    private static final char[] RETURN = {'\r'};
    private static final char[] PARAMSTART = {'<','<'};

    private static final char[][] tokens = {
        AUTHOR, CREATIONDATE, ENDSTREAM, KEYWORDS, STREAM, SUBJECT,
        TITLE, PARAMSTART
    };

    /**
     * PdfParser constructor comment.
     */
    public PdfParser() {
        contents = new StringBuffer();
        published = -1;

        // 19960710150856
        dateFormatter = new SimpleDateFormat("yyyyMMddHHmmss");
    }
    /**
     * Look for tokens.  This is not effiecent.
     * Should use low, hi method with ordered array. NEED TO RECODE
     */
    private char[] findToken() throws IOException {


        // flags if token still matches.
        boolean[] match = new boolean[tokens.length];
        for (int i = 0; i < match.length; i++) {
            match[i] = true;
        }

        // how many tokens still match;
        int matchCount = tokens.length;

        // current position to look for char match in tokens
        int charPosition = 0;

        // look for matching tokens.
        while (true) {
            int b = read();
            if (b == -1 ) break;
            char ch = (char)b;


            // loop through all tokens
            for (int i = 0; i < tokens.length; i++) {
                // check to see if match flag is true for this token

                if (match[i] == true) {
                    // get the token
                    char[] token = tokens[i];
                    // check if char array of token is in bounds
                    if (charPosition >= token.length) {
                        // out of bounds, check to see if other tokens still match
                        if (matchCount >= 2) {
                            // other tokens still match, set this one to false.
                            match[i] = false;
                            matchCount--;
                        } else {
                            // last matching token;
                            return token;
                        }
                        // token is in bounds, check for match on char at charPosition.
                    } else {
                        if (token[charPosition] != ch) {
                            // did not match, set match to false;
                            match[i] = false;
                            matchCount--;
                        }
                    }
                }
            }
            if (matchCount <= 0 ) break;
            charPosition++;
        }

        return null;
    }
    /**
     * Parse Content.
     */
    public String getAuthor() {
        return author;
    }
    /**
     * Return categories (from META tags)
     */
    public String getCategories() {
        return null;
    }
    /**
     * Parse Content.
     */
    public String getContents() {
        return contents.toString();
    }
    /**
     * Parse Content.
     */
    public String getDescription() {
        return description;
    }
    /**
     *	Return META HREF
     */
    public String getHREF() {
        return null;
    }
    /**
     * Parse Content.
     */
    public String getKeywords() {
        return keywords;
    }
    /**
     * Return links
     */
    public List getLinks() {
        return null;
    }
    /**
     * Parse Content.
     */
    public long getPublished() {
        return published;
    }

    /**
     *
     */
    public String getTitle() {
        return title;
    }
    /**
     * Check for new line chars
     */
    private boolean isNewLineChar(char ch) {
        switch (ch) {
            case '\n' :
                return true;
            case '\r' :
                return true;
            default :
                return false;
        }

    }
    /**
     * Insert the method's description here.
     * Creation date: (2/21/2001 7:50:24 PM)
     * @param args java.lang.String[]
     */
    public static void main(String[] args) {

        //System.out.println("test");
        try {
            //String path = "z:/webapps/dev/pdf/HealthPlansOnline.pdf";]
            String path = "D:/ablage/Urlaubsantrag3_02.pdf";
            PdfParser p = new PdfParser();
            p.parse(new FileInputStream(path));
            System.out.println("Title: " + p.getTitle());
            System.out.println("Author: " + p.getAuthor());
            System.out.println("Published " + p.getPublished());
            System.out.println("Keywords: " + p.getKeywords());
            System.out.println("Description: " + p.getDescription());
            System.out.println("Content: " + p.getContents());
        } catch (Exception e) {e.printStackTrace();}
    }
    /**
     * Parse Content. [24] 320:1
     */
    private boolean nextLine() throws IOException {
        //System.out.println("look for new line");
        while (true) {
            int b = read();
            if (b == -1 ) return false;
            if (isNewLineChar((char)b)) return true;
        }

    }
    /**
     * Parse Content.
     */
    public void parse(InputStream in) {


        //System.out.println("mark supported" + in.markSupported());

        try {
            this.in = new BufferedInputStream(in);
            reset();
            parseContent();
            if (debug){
                System.out.println("Title: " + getTitle());
                System.out.println("Author: " + getAuthor());
                System.out.println("Published " + getPublished());
                System.out.println("Keywords: " + getKeywords());
                System.out.println("Description: " + getDescription());
                System.out.println("Content: " + getContents());
            }

            /*int b;
            while ((b = in.read()) != -1) {
                System.out.print((byte)b + ".");
                System.out.print((char)b + "*");
            }*/

        } catch (Exception e) {e.printStackTrace();}
    }
    /**
     * Parse Content. [24] 320:1
     */
    private void parseContent() throws IOException {
        Thread curThread = Thread.currentThread();
        while (true) {
            if (curThread.isInterrupted()) {
                curThread.interrupt(); // resignal the interrupt
                break;
            }
            char[] token;
            while (true) {
                token = findToken();
                if (token != null) {
                    //System.out.println("found a token : " + token);
                    if (token == AUTHOR) {
                        author = parseData();
                    } else if (token == CREATIONDATE) {
                        published = parseDate();
                    } else if (token == KEYWORDS) {
                        keywords = parseData();
                    } else if (token == SUBJECT) {
                        description = parseData();
                    } else if (token == TITLE) {
                        title = parseData();
                    } else if (token == PARAMSTART) {
                        //System.out.println("param set mark");
                        in.mark(10000);
                        //parseDataParams();
                    } else if (token == STREAM) {
                        if (!streamHit) {
                            //System.out.println("new stream hit");
                            // first time this stream has been hit
                            // go back and parseDataParams.
                            in.reset();
                            streamHit = true;
                            parseDataParams();
                        } else {
                            //System.out.println("second stream hit");
                            if (parseNextStream) {
                                //System.out.println("parseDataStream");
                                contents.append(parseDataStream());
                                parseNextStream = false;
                            }
                            streamHit = false;
                        }
                    }
                }
                if (!nextLine()) {
                    //System.out.println("new line");
                    break;
                }
                //System.out.println("new line");
            }

            //System.out.println("hello");
            break;

        }
        replaceSpChars();
    }
    /**
     * Look for tokens.  This is not effiecent.
     * Should use low, hi method with ordered array. NEED TO RECODE
     */
    private String parseData() throws IOException {

        ByteArrayOutputStream temp = new ByteArrayOutputStream();

        // look for start '('
        while (true) {
            int b = read();
            if (b == -1 ) break;
            char ch = (char)b;
            if (ch == '(') break;
        }
        while (true) {
            int b = read();
            if (b == -1 ) break;
            char ch = (char)b;
            if (ch == ')') break;
            temp.write(b);
        }

        return new String(temp.toByteArray());
    }
    /**
     * Look for tokens.  This is not effiecent.
     * Should use low, hi method with ordered array. NEED TO RECODE
     */
    private String parseDataParams() throws IOException {

        ByteArrayOutputStream temp = new ByteArrayOutputStream();

        boolean end = false;
        int b = read();

        while (true) {
            // check to see if new line;
            if ((char)b == '>') {
                b = read();
                if ((char)b == '>') {
                    end = true;
                    break;
                } else {
                    temp.write(b);
                }
            } else {
                temp.write(b);
            }
            if (end) break;
            b = read();
        }
        String params = new String(temp.toByteArray());
        //System.out.println(params.length());
        //System.out.println(params);
        if (params.length() < 38
        /*&& params.indexOf("0 R") != -1*/
        && params.indexOf("/Length ") != -1)  {
            if (params.indexOf("/FlateDecode") != -1) compression = FLATE;
            if (params.indexOf("/LZWDecode") != -1) compression = LZW;
            parseNextStream = true;
            //System.out.println();
            //System.out.println(params);
        }

        return new String(temp.toByteArray());
    }
    /**
     * Look for tokens.  This is not effiecent.
     * Should use low, hi method with ordered array. NEED TO RECODE
     */
    private String parseDataStream() throws IOException {

        ByteArrayOutputStream temp = new ByteArrayOutputStream();
        ByteArrayOutputStream tmp = new ByteArrayOutputStream(ENDSTREAM.length);
        boolean endstream = false;

        int b = read();
        char ch = (char)b;
        while (true) {
            // check to see if new line;
            if (isNewLineChar(ch)) {
                // check to see if it is endstream
                tmp.reset();
                boolean notMatch = false;
                for (int i = 0; i < ENDSTREAM.length; i++) {
                    b = read();
                    tmp.write(b);
                    if ((char)b != ENDSTREAM[i]) {
                        // not endsteam break..
                        notMatch = true;
                        tmp.writeTo(temp);
                        break;
                    }
                }
                if (!notMatch) endstream = true;
            } else {
                // not new line append byte
                temp.write(b);
                b = read();
                ch = (char)b;
            }
            if (endstream) break; // endstream found
        }

        // Uncompress if flateDecode is used
        if (compression == FLATE) {
            //System.out.println("FlateDecode = " +flateDecode);
            ByteArrayInputStream bis = new ByteArrayInputStream(temp.toByteArray());
            InflaterInputStream iin = new InflaterInputStream(bis);
            temp.reset();
            while ((b = iin.read()) != -1) {
                temp.write(b);
            }
        }

        //System.out.println(temp.size());
        //System.out.println(new String(temp.toByteArray()));

        // parse content out from formating data. Content is wrapped in a
        // bunch of ()

        // look for start '('
        ByteArrayInputStream bis = new ByteArrayInputStream(temp.toByteArray());
        tmp.reset();
        boolean end = false;
        while (true) {
            b = bis.read();
            if (b == -1 ) break;
            if ((char)b == '(') {
                while (true) {
                    b = bis.read();
                    if (b == -1 ) {end = true; break;}
                    // look for end ')'
                    if ((char)b == ')') break;
                    tmp.write(b);
                }
            }
            if (end) break;
        }

        // reset flateDecode flag
        compression = NONE;
        //System.out.println(tmp.size());
        //System.out.println(new String(tmp.toByteArray()));
        return new String(tmp.toByteArray());
    }
    /**
     * To replace the german Sonderzeichen.
     */
    private void replaceSpChars(){

        int index=0;
        int length=contents.length();
        boolean weiter;
        while(true){
            weiter=false;
            if (index+4<length) {
                index=contents.toString().indexOf("\\237");
                if (index!=-1) {
                    contents=contents.replace(index,index+4,"ue");
                    weiter=true;
                }
                index=contents.toString().indexOf("\\212");
                if (index!=-1) {
                    contents=contents.replace(index,index+4,"ae");
                    weiter=true;
                }
                index=contents.toString().indexOf("\\232");
                if (index!=-1) {
                    contents=contents.replace(index,index+4,"oe");
                    weiter=true;
                }
                index=contents.toString().indexOf("\\206");
                if (index!=-1) {
                    contents=contents.replace(index,index+4,"Ue");
                    weiter=true;
                }
                index=contents.toString().indexOf("\\312");
                if (index!=-1) {
                    contents=contents.replace(index,index+4," ");
                    weiter=true;
                }
                index=contents.toString().indexOf("\\320");
                if (index!=-1) {
                    contents=contents.replace(index,index+4,"-");
                    weiter=true;
                }/*
                index=contents.toString().indexOf("\\237");
                if (index!=-1) {
                    contents=contents.replace(index,index+4,"Ae");
                    weiter=true;
                }
                index=contents.toString().indexOf("\\237");
                if (index!=-1) {
                    contents=contents.replace(index,index+4,"Oe");
                    weiter=true;
                }*/
                index=contents.toString().indexOf("\\321");
                if (index!=-1) {
                    contents=contents.replace(index,index+4," ");
                    weiter=true;
                }
                index=contents.toString().indexOf("\\247");
                if (index!=-1) {
                    contents=contents.replace(index,index+4,"ss");
                    weiter=true;
                }
                //next codeset
                index=contents.toString().indexOf("\\344");
                if (index!=-1) {
                    contents=contents.replace(index,index+4,"ae");
                    weiter=true;
                }
                index=contents.toString().indexOf("\\304");
                if (index!=-1) {
                    contents=contents.replace(index,index+4,"Ae");
                    weiter=true;
                }
                index=contents.toString().indexOf("\\374");
                if (index!=-1) {
                    contents=contents.replace(index,index+4,"ue");
                    weiter=true;
                }
                index=contents.toString().indexOf("\\334");
                if (index!=-1) {
                    contents=contents.replace(index,index+4,"Ue");
                    weiter=true;
                }
                index=contents.toString().indexOf("\\366");
                if (index!=-1) {
                    contents=contents.replace(index,index+4,"oe");
                    weiter=true;
                }
                index=contents.toString().indexOf("\\326");
                if (index!=-1) {
                    contents=contents.replace(index,index+4,"Oe");
                    weiter=true;
                }
                index=contents.toString().indexOf("\\337");
                if (index!=-1) {
                    contents=contents.replace(index,index+4,"ss");
                    weiter=true;
                }
            }
            if (!weiter) break;
        }
    }

    /**
     * Look for tokens.  This is not effiecent.
     * Should use low, hi method with ordered array. NEED TO RECODE
     */
    private long parseDate() throws IOException {

        try {
            String date = parseData();
            return dateFormatter.parse(date.substring(2, date.length())).getTime();
        } catch(ParseException e) {
            e.printStackTrace();
            return -1;
        }
    }
    private final int read() throws IOException {

        ++currentPosition;
        return in.read();
    }
    private final char readCh() throws IOException {

        ++currentPosition;
        return (char)in.read();
    }
    /**
     *	Return contents
     */
    private void reset() {

        // Content
        title = null;
        description = null;
        keywords = null;
        author = null;

        contents.setLength(0);
        published = -1;


        // Flags
        streamHit = false;
        parseNextStream = false;
        compression = NONE;
    }
}
