package source.org.apache.java.io;

/*
 * Copyright (c) 1997-1999 The Java Apache Project.  All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. All advertising materials mentioning features or use of this
 *    software must display the following acknowledgment:
 *    "This product includes software developed by the Java Apache 
 *    Project for use in the Apache JServ servlet engine project
 *    <http://java.apache.org/>."
 *
 * 4. The names "Apache JServ", "Apache JServ Servlet Engine" and 
 *    "Java Apache Project" must not be used to endorse or promote products 
 *    derived from this software without prior written permission.
 *
 * 5. Products derived from this software may not be called "Apache JServ"
 *    nor may "Apache" nor "Apache JServ" appear in their names without 
 *    prior written permission of the Java Apache Project.
 *
 * 6. Redistributions of any form whatsoever must retain the following
 *    acknowledgment:
 *    "This product includes software developed by the Java Apache 
 *    Project for use in the Apache JServ servlet engine project
 *    <http://java.apache.org/>."
 *    
 * THIS SOFTWARE IS PROVIDED BY THE JAVA APACHE PROJECT "AS IS" AND ANY
 * EXPRESSED OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 * PURPOSE ARE DISCLAIMED.  IN NO EVENT SHALL THE JAVA APACHE PROJECT OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
 * NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
 * STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
 * OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Java Apache Group. For more information
 * on the Java Apache Project and the Apache JServ Servlet Engine project,
 * please see <http://java.apache.org/>.
 *
 */

/**
 * Class to store the logging information until it gets processed by the
 * logger thread.
 * <p>
 * Possible enhancements:
 * <ul>
 * <li>Name of the thread which issued the log message.
 * <li>Name of the object on whose behalf the message was issued.
 * </ul>
 * @version $Revision: 1.4 $ $Date: 2003/06/13 10:56:35 $
 * @author <a href="mailto:vt@freehold.crocodile.org">Vadim Tkachenko</a>
 */

public class LogRecord {

	/**
	 * Creation time.
	 */
	public long m_date;

	/**
	 * Log channel.
	 * To be used in the log filters.
	 */
	public String m_channel;

	/**
	 * Message to log, if any.
	 */
	 public String m_message;

	/**
	 * Exception to log, if any.
	 */
	public Throwable m_t;

	/**
	 * Constructor.
	 * Performs inexpensive operations:
	 * <ul>
	 * <li>Records the system time;
	 * <li>Stores the message channel;
	 * <li>Stores the message itself, if any;
	 * <li>Stores the exception, if any.
	 * </ul>
	 * @param t Owner thread
     * @param channel the channel to put the message on
     * @param message the message to log
	 */
	public LogRecord(String channel, String message, Throwable t) {

		//  Note, no new Date() here - it's EXPENSIVE!
		//  You can do whatever you want with it later, in the logging
		//  thread.

		m_date = System.currentTimeMillis();
		this.m_channel = channel;
		this.m_message = message;
		this.m_t = t;
	}
}
