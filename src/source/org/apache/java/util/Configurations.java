package source.org.apache.java.util;

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

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.NoSuchElementException;
import java.util.Properties;
import java.util.Vector;

/**
 * This class is used to encapsulate properties and addresses
 * the need for a flexible, portable and fast configurations
 * repository.
 *
 * <p>While properties are just considered as strings, configurations
 * have methods to return different types such as <b>int</b> and
 * <b>long</b>.
 *
 * <p>Performance is needed to avoid the use of properties only at
 * startup to fill variables: configurations encapsulate properties
 * in the sense that objects retrieved by parsing property strings
 * are stored for faster reuse. This allows a program to use
 * configurations instead of global variables in a central repository,
 * that, if updated, will reflect instantly the changes throughout
 * the whole application.
 *
 * <p>The behavior of this class is syntax indipendent because
 * it's only an encapsulator.
 * This allows greater flexibility throught the use of syntax abstraction
 * and faster transition between different configuration syntax.
 *
 * @author <a href="mailto:stefano@apache.org">Stefano Mazzocchi</a>
 * @version $Revision: 1.5 $ $Date: 2003/06/13 10:56:35 $
 */
public class Configurations {

	/**
	 * Default configurations repository.
	 */
	private Configurations m_defaults;

	/**
	 * Configuration repository.
	 */
	private ConfigurationsRepository m_repository;

	/**
	 * Creates an empty configuration repository
	 * with no default values.
	 */
	public Configurations() {
		this(null, null);
	}
    
	/**
	 * Creates an empty configuration repository with
	 * the specified defaults.
	 *
	 * @param defaults the default values repository.
	 */
	public Configurations(Configurations defaults) {
		this(null, defaults);
	}
    
	/**
	 * Creates a configuration repository encapsulating
	 * the given properties with no default values.
	 *
	 * @param properties the properties to encapsulate.
	 */
	public Configurations(ConfigurationsRepository properties) {
		this(properties, null);
	}
	/**
	 * Merge the given properties object as configurations.
	 *
	 * @param properties the properties file to merge
     * @param defaults the defaults
	 */
	public Configurations(ConfigurationsRepository properties,
			Configurations defaults) {
		this.m_repository = properties;
		this.m_defaults = defaults;
	}
	/**
	 * Get a boolean associated with the given configuration key.
	 *
	 * @param key the configuration key.
	 * @return the associated boolean.
	 * @throws NoSuchElementException is thrown if the key doesn't
	 * map to an existing object.
	 * @throws ClassCastException is thrown if the key maps to an
	 * object that is not a Boolean.
	 */
	public boolean getBoolean(String key) {
		Boolean b = this.getBoolean(key, (Boolean) null);
		if (b != null) {
			return b.booleanValue();
		} else {
			throw new NoSuchElementException(key
				+ " doesn't map to an existing object");
		}
	}
	/**
	 * Get a boolean associated with the given configuration key.
	 *
	 * @param key the configuration key.
	 * @param defaultValue the defaul value.
	 * @return the associated boolean if key is found and has valid format,
	 * default value otherwise.
	 * @throws ClassCastException is thrown if the key maps to an
	 * object that is not a Boolean.
	 */
	public Boolean getBoolean(String key, Boolean defaultValue) {
		Object value = m_repository.get(key);

		if (value instanceof Boolean) {
			return (Boolean) value;
		} else if (value instanceof String) {
			Boolean b = new Boolean((String) value);
			m_repository.put(key, b);
			return b;
		} else if (value == null) {
			if (m_defaults != null) {
				return m_defaults.getBoolean(key, defaultValue);
			} else {
				return defaultValue;
			}
		} else {
			throw new ClassCastException(key
				+ " doesn't map to a Boolean object");
		}
	}
	/**
	 * Get a boolean associated with the given configuration key.
	 *
	 * @param key the configuration key.
	 * @param defaultValue the defaul value.
	 * @return the associated boolean.
	 * @throws ClassCastException is thrown if the key maps to an
	 * object that is not a Boolean.
	 */
	public boolean getBoolean(String key, boolean defaultValue) {
		return this.getBoolean(key, new Boolean(defaultValue)).booleanValue();
	}
	/**
	 * Get a byte associated with the given configuration key.
	 *
	 * @param key the configuration key.
	 * @return the associated byte.
	 * @throws NoSuchElementException is thrown if the key doesn't
	 * map to an existing object.
	 * @throws ClassCastException is thrown if the key maps to an
	 * object that is not a Byte.
	 * @throws NumberFormatException is thrown if the value
	 * mapped by the key has not a valid number format.
	 */
	public byte getByte(String key) {
		Byte b = this.getByte(key, null);
		if (b != null) {
			return b.byteValue();
		} else {
			throw new NoSuchElementException(key
				+ " doesn't map to an existing object");
		}
	}
	/**
	 * Get a byte associated with the given configuration key.
	 *
	 * @param key the configuration key.
	 * @param defaultValue the defaul value.
	 * @return the associated byte.
	 * @throws ClassCastException is thrown if the key maps to an
	 * object that is not a Byte.
	 * @throws NumberFormatException is thrown if the value
	 * mapped by the key has not a valid number format.
	 */
	public byte getByte(String key, byte defaultValue) {
		return this.getByte(key, new Byte(defaultValue)).byteValue();
	}
	/**
	 * Get a byte associated with the given configuration key.
	 *
	 * @param key the configuration key.
	 * @param defaultValue the defaul value.
	 * @return the associated byte if key is found and has valid format,
	 * default value otherwise.
	 * @throws ClassCastException is thrown if the key maps to an
	 * object that is not a Byte.
	 * @throws NumberFormatException is thrown if the value
	 * mapped by the key has not a valid number format.
	 */
	public Byte getByte(String key, Byte defaultValue) {
		Object value = m_repository.get(key);

		if (value instanceof Byte) {
			return (Byte) value;
		} else if (value instanceof String) {
			Byte b = new Byte((String) value);
			m_repository.put(key, b);
			return b;
		} else if (value == null) {
			if (m_defaults != null) {
				return m_defaults.getByte(key, defaultValue);
			} else {
				return defaultValue;
			}
		} else {
			throw new ClassCastException(key
				+ " doesn't map to a Byte object");
		}
	}
	/**
	 * Get a double associated with the given configuration key.
	 *
	 * @param key the configuration key.
	 * @return the associated double.
	 * @throws NoSuchElementException is thrown if the key doesn't
	 * map to an existing object.
	 * @throws ClassCastException is thrown if the key maps to an
	 * object that is not a Double.
	 * @throws NumberFormatException is thrown if the value
	 * mapped by the key has not a valid number format.
	 */
	public double getDouble(String key) {
		Double d = this.getDouble(key, null);
		if (d != null) {
			return d.doubleValue();
		} else {
			throw new NoSuchElementException(key
				+ " doesn't map to an existing object");
		}
	}
	/**
	 * Get a double associated with the given configuration key.
	 *
	 * @param key the configuration key.
	 * @param defaultValue the defaul value.
	 * @return the associated double.
	 * @throws ClassCastException is thrown if the key maps to an
	 * object that is not a Double.
	 * @throws NumberFormatException is thrown if the value
	 * mapped by the key has not a valid number format.
	 */
	public double getDouble(String key, double defaultValue) {
		return this.getDouble(key, new Double(defaultValue)).doubleValue();
	}
	/**
	 * Get a double associated with the given configuration key.
	 *
	 * @param key the configuration key.
	 * @param defaultValue the defaul value.
	 * @return the associated double if key is found and has valid format,
	 * default value otherwise.
	 * @throws ClassCastException is thrown if the key maps to an
	 * object that is not a Double.
	 * @throws NumberFormatException is thrown if the value
	 * mapped by the key has not a valid number format.
	 */
	public Double getDouble(String key, Double defaultValue) {
		Object value = m_repository.get(key);

		if (value instanceof Double) {
			return (Double) value;
		} else if (value instanceof String) {
			Double d = new Double((String) value);
			m_repository.put(key, d);
			return d;
		} else if (value == null) {
			if (m_defaults != null) {
				return m_defaults.getDouble(key, defaultValue);
			} else {
				return defaultValue;
			}
		} else {
			throw new ClassCastException(key
				+ " doesn't map to a Double object");
		}
	}
	/**
	 * Get a float associated with the given configuration key.
	 *
	 * @param key the configuration key.
	 * @return the associated float.
	 * @throws NoSuchElementException is thrown if the key doesn't
	 * map to an existing object.
	 * @throws ClassCastException is thrown if the key maps to an
	 * object that is not a Float.
	 * @throws NumberFormatException is thrown if the value
	 * mapped by the key has not a valid number format.
	 */
	public float getFloat(String key) {
		Float f = this.getFloat(key, null);
		if (f != null) {
			return f.floatValue();
		} else {
			throw new NoSuchElementException(key
				+ " doesn't map to an existing object");
		}
	}
	/**
	 * Get a float associated with the given configuration key.
	 *
	 * @param key the configuration key.
	 * @param defaultValue the defaul value.
	 * @return the associated float.
	 * @throws ClassCastException is thrown if the key maps to an
	 * object that is not a Float.
	 * @throws NumberFormatException is thrown if the value
	 * mapped by the key has not a valid number format.
	 */
	public float getFloat(String key, float defaultValue) {
		return this.getFloat(key, new Float(defaultValue)).floatValue();
	}
	/**
	 * Get a float associated with the given configuration key.
	 *
	 * @param key the configuration key.
	 * @param defaultValue the defaul value.
	 * @return the associated float if key is found and has valid format,
	 * default value otherwise.
	 * @throws ClassCastException is thrown if the key maps to an
	 * object that is not a Float.
	 * @throws NumberFormatException is thrown if the value
	 * mapped by the key has not a valid number format.
	 */
	public Float getFloat(String key, Float defaultValue) {
		Object value = m_repository.get(key);

		if (value instanceof Float) {
			return (Float) value;
		} else if (value instanceof String) {
			Float f = new Float((String) value);
			m_repository.put(key, f);
			return f;
		} else if (value == null) {
			if (m_defaults != null) {
				return m_defaults.getFloat(key, defaultValue);
			} else {
				return defaultValue;
			}
		} else {
			throw new ClassCastException(key
				+ " doesn't map to a Float object");
		}
	}
	/**
	 * Get a int associated with the given configuration key.
	 *
	 * @param key the configuration key.
	 * @return the associated int.
	 * @throws NoSuchElementException is thrown if the key doesn't
	 * map to an existing object.
	 * @throws ClassCastException is thrown if the key maps to an
	 * object that is not a Integer.
	 * @throws NumberFormatException is thrown if the value
	 * mapped by the key has not a valid number format.
	 */
	public int getInteger(String key) {
		Integer i = this.getInteger(key, null);
		if (i != null) {
			return i.intValue();
		} else {
			throw new NoSuchElementException(key
				+ " doesn't map to an existing object");
		}
	}
	/**
	 * Get a int associated with the given configuration key.
	 *
	 * @param key the configuration key.
	 * @param defaultValue the defaul value.
	 * @return the associated int.
	 * @throws ClassCastException is thrown if the key maps to an
	 * object that is not a Integer.
	 * @throws NumberFormatException is thrown if the value
	 * mapped by the key has not a valid number format.
	 */
	public int getInteger(String key, int defaultValue) {
		return this.getInteger(key, new Integer(defaultValue)).intValue();
	}
	/**
	 * Get a int associated with the given configuration key.
	 *
	 * @param key the configuration key.
	 * @param defaultValue the defaul value.
	 * @return the associated int if key is found and has valid format,
	 * default value otherwise.
	 * @throws ClassCastException is thrown if the key maps to an
	 * object that is not a Integer.
	 * @throws NumberFormatException is thrown if the value
	 * mapped by the key has not a valid number format.
	 */
	public Integer getInteger(String key, Integer defaultValue) {
		Object value = m_repository.get(key);

		if (value instanceof Integer) {
			return (Integer) value;
		} else if (value instanceof String) {
			Integer i = new Integer((String) value);
			m_repository.put(key, i);
			return i;
		} else if (value == null) {
			if (m_defaults != null) {
				return m_defaults.getInteger(key, defaultValue);
			} else {
				return defaultValue;
			}
		} else {
			throw new ClassCastException(key
				+ " doesn't map to a Integer object");
		}
	}
    
	/**
	 * Get the list of the keys contained in the
	 * configuration repository.
     * @return the list of the keys 
	 */
	public Enumeration getKeys() {
		return this.m_repository.keys();
	}
    
	/**
	 * Get a list of strings associated with the
	 * given configuration key.
	 *
	 * @param key the configuration key.
	 * @return the associated list.
	 * @throws ClassCastException is thrown if the key maps to an
	 * object that is not a Vector.
	 */
	public Enumeration getList(String key) {
		return this.getVector(key, null).elements();
	}
    
	/**
	 * Get a long associated with the given configuration key.
	 *
	 * @param key the configuration key.
	 * @return the associated long.
	 * @throws NoSuchElementException is thrown if the key doesn't
	 * map to an existing object.
	 * @throws ClassCastException is thrown if the key maps to an
	 * object that is not a Long.
	 * @throws NumberFormatException is thrown if the value
	 * mapped by the key has not a valid number format.
	 */
	public long getLong(String key) {
		Long l = this.getLong(key, null);
		if (l != null) {
			return l.longValue();
		} else {
			throw new NoSuchElementException(key
				+ " doesn't map to an existing object");
		}
	}
	/**
	 * Get a long associated with the given configuration key.
	 *
	 * @param key the configuration key.
	 * @param defaultValue the defaul value.
	 * @return the associated long.
	 * @throws ClassCastException is thrown if the key maps to an
	 * object that is not a Long.
	 * @throws NumberFormatException is thrown if the value
	 * mapped by the key has not a valid number format.
	 */
	public long getLong(String key, long defaultValue) {
		return this.getLong(key, new Long(defaultValue)).longValue();
	}
	/**
	 * Get a long associated with the given configuration key.
	 *
	 * @param key the configuration key.
	 * @param defaultValue the defaul value.
	 * @return the associated long if key is found and has valid format,
	 * default value otherwise.
	 * @throws ClassCastException is thrown if the key maps to an
	 * object that is not a Long.
	 * @throws NumberFormatException is thrown if the value
	 * mapped by the key has not a valid number format.
	 */
	public Long getLong(String key, Long defaultValue) {
		Object value = m_repository.get(key);

		if (value instanceof Long) {
			return (Long) value;
		} else if (value instanceof String) {
			Long l = new Long((String) value);
			m_repository.put(key, l);
			return l;
		} else if (value == null) {
			if (m_defaults != null) {
				return m_defaults.getLong(key, defaultValue);
			} else {
				return defaultValue;
			}
		} else {
			throw new ClassCastException(key
				+ " doesn't map to a Long object");
		}
	}
	/**
	 * Get a list of properties associated with the given configuration key.
	 *
	 * @param key the configuration key.
	 * @return the associated properties if key is found
	 * @throws ClassCastException is thrown if the key maps to an
	 * object that is not a String/Vector
	 * @throws IllegalArgumentException if one of the tokens is
	 * malformed (does not contain an equals sign)
	 */
	public Properties getProperties(String key) {
	  return getProperties(key, new Properties());
	}
	/**
	 * Get a list of properties associated with the given configuration key.
	 *
	 * @param key the configuration key.
     * @param defaults the defaults
	 * @return the associated properties if key is found
	 * @throws ClassCastException is thrown if the key maps to an
	 * object that is not a String/Vector
	 * @throws IllegalArgumentException if one of the tokens is
	 * malformed (does not contain an equals sign)
	 */
	public Properties getProperties(String key, Properties defaults) {
		// Grab an array of the tokens for this key
		String[] tokens = getStringArray(key);

	// Each token is of the form 'key=value'.
	Properties props = new Properties(defaults);
	for (int i = 0; i < tokens.length; i++) {
	  String token = tokens[i];
	  int equalSign = token.indexOf('=');
	  if (equalSign > 0) {
		String pkey = token.substring(0, equalSign).trim();
		String pvalue = token.substring(equalSign + 1).trim();
		props.put(pkey, pvalue);
	  } else {
		throw new IllegalArgumentException("'" + token + "' does not contain an equals sign");
	  }
	}

	return props;
	}
    
	/**
	 * Get encapsulated configuration repository.
     * 
     * @return the encapsulated configuration repository
	 */
	public Hashtable getRepository() {
		return this.m_repository;
	}
    
	/**
	 * Get a short associated with the given configuration key.
	 *
	 * @param key the configuration key.
	 * @return the associated short.
	 * @throws NoSuchElementException is thrown if the key doesn't
	 * map to an existing object.
	 * @throws ClassCastException is thrown if the key maps to an
	 * object that is not a Short.
	 * @throws NumberFormatException is thrown if the value
	 * mapped by the key has not a valid number format.
	 */
	public short getShort(String key) {
		Short s = this.getShort(key, null);
		if (s != null) {
			return s.shortValue();
		} else {
			throw new NoSuchElementException(key
				+ " doesn't map to an existing object");
		}
	}
    
	/**
	 * Get a short associated with the given configuration key.
	 *
	 * @param key the configuration key.
	 * @param defaultValue the defaul value.
	 * @return the associated short if key is found and has valid format,
	 * default value otherwise.
	 * @throws ClassCastException is thrown if the key maps to an
	 * object that is not a Short.
	 * @throws NumberFormatException is thrown if the value
	 * mapped by the key has not a valid number format.
	 */
	public Short getShort(String key, Short defaultValue) {
		Object value = m_repository.get(key);

		if (value instanceof Short) {
			return (Short) value;
		} else if (value instanceof String) {
			Short s = new Short((String) value);
			m_repository.put(key, s);
			return s;
		} else if (value == null) {
			if (m_defaults != null) {
				return m_defaults.getShort(key, defaultValue);
			} else {
				return defaultValue;
			}
		} else {
			throw new ClassCastException(key
				+ " doesn't map to a Short object");
		}
	}
    
	/**
	 * Get a short associated with the given configuration key.
	 *
	 * @param key the configuration key.
	 * @param defaultValue the defaul value.
	 * @return the associated short.
	 * @throws ClassCastException is thrown if the key maps to an
	 * object that is not a Short.
	 * @throws NumberFormatException is thrown if the value
	 * mapped by the key has not a valid number format.
	 */
	public short getShort(String key, short defaultValue) {
		return this.getShort(key, new Short(defaultValue)).shortValue();
	}
    
	/**
	 * Get a string associated with the given configuration key.
	 *
	 * @param key the configuration key.
	 * @return the associated string.
	 * @throws ClassCastException is thrown if the key maps to an
	 * object that is not a String.
	 */
	public String getString(String key) {
		return this.getString(key, null);
	}
    
	/**
	 * Get a string associated with the given configuration key.
	 *
	 * @param key the configuration key.
	 * @param defaultValue the default value.
	 * @return the associated string if key is found,
	 * default value otherwise.
	 * @throws ClassCastException is thrown if the key maps to an
	 * object that is not a String.
	 */
	public String getString(String key, String defaultValue) {
		Object value = m_repository.get(key);

		if (value instanceof String) {
			return (String) value;
		} else if (value == null) {
			if (m_defaults != null) {
				return m_defaults.getString(key, defaultValue);
			} else {
				return defaultValue;
			}
		} else {
			throw new ClassCastException(key
				+ " doesn't map to a String object");
		}
	}
    
	/**
	 * Get an array of strings associated with the given configuration key.
	 *
	 * @param key the configuration key.
	 * @return the associated string array if key is found,
	 * @throws ClassCastException is thrown if the key maps to an
	 * object that is not a String/Vector
	 */
	public String[] getStringArray(String key) {
		Object value = m_repository.get(key);

	// What's your vector, Victor?
	Vector vector;
		if (value instanceof String) {
	  vector = new Vector(1);
	  vector.addElement(value);
		} else if (value instanceof Vector) {
	  vector = (Vector)value;
	} else if (value == null) {
	  if (m_defaults != null) {
		return m_defaults.getStringArray(key);
	  } else {
		return new String[0];
	  }
	} else {
	  throw new ClassCastException(key + " doesn't map to a String/Vector object");
		}

	String[] tokens = new String[vector.size()];
	for (int i = 0; i < tokens.length; i++)
	  tokens[i] = (String)vector.elementAt(i);

	return tokens;
	}
    
	/**
	 * Get a Vector of strings associated with the
	 * given configuration key.
	 *
	 * @param key the configuration key.
	 * @return the associated Vector.
	 * @throws ClassCastException is thrown if the key maps to an
	 * object that is not a Vector.
	 */
	public Vector getVector(String key) {
		return this.getVector(key, null);
	}
    
	/**
	 * Get a Vector of strings associated with the
	 * given configuration key.
	 *
	 * @param key the configuration key.
	 * @param defaultValue the defaul value.
	 * @return the associated Vector.
	 * @throws ClassCastException is thrown if the key maps to an
	 * object that is not a Vector.
	 */
	public Vector getVector(String key, Vector defaultValue) {
		Object value = m_repository.get(key);

		if (value instanceof Vector) {
			return (Vector) value;
		} else if (value instanceof String) {
			Vector v = new Vector(1);
			v.addElement((String) value);
			m_repository.put(key, v);
			return v;
		} else if (value == null) {
			if (m_defaults != null) {
				return m_defaults.getVector(key, defaultValue);
			} else {
				return ((defaultValue == null)
					? new Vector() : defaultValue);
			}
		} else {
			throw new ClassCastException(key
				+ " doesn't map to a Vector object");
		}
	}
}
