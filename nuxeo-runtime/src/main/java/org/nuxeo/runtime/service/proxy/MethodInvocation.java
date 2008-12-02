/*
 * (C) Copyright 2006-2008 Nuxeo SAS (http://nuxeo.com/) and contributors.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser General Public License
 * (LGPL) version 2.1 which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/lgpl.html
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * Contributors:
 *     bstefanescu
 *
 * $Id$
 */

package org.nuxeo.runtime.service.proxy;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.lang.reflect.Method;

/**
 * @author <a href="mailto:bs@nuxeo.com">Bogdan Stefanescu</a>
 *
 */
public class MethodInvocation implements Serializable {

    private static final long serialVersionUID = -7847013619148245793L;
    
    protected transient Method method;
    
    public MethodInvocation(Method method) {
        this.method = method;        
    }
    
    
    public Method getMethod() {
        return method;
    }

    public Object invoke(Object proxy, Object ... args)  throws Throwable {
        return method.invoke(proxy, args);
    }

    
    protected Class<?> loadClass(String name) throws ClassNotFoundException {
        return Class.forName(name);
    }
    
    protected Class<?> getPrimitiveType(String name) {
        if (name.equals("byte")) return Byte.TYPE;
        if (name.equals("short")) return Short.TYPE;
        if (name.equals("int")) return Integer.TYPE;
        if (name.equals("long")) return Long.TYPE;
        if (name.equals("char")) return Character.TYPE;
        if (name.equals("float")) return Float.TYPE;
        if (name.equals("double")) return Double.TYPE;
        if (name.equals("boolean")) return Boolean.TYPE;
        if (name.equals("void")) return Void.TYPE;
        return null;
    }

    protected Class<?> getType(String name) throws ClassNotFoundException {
        Class<?> p = getPrimitiveType(name);
        if (p == null) {
            p = loadClass(name);
        }
        return p;
    }    

    protected void writeChars(ObjectOutputStream out, String chars) throws IOException {
        out.writeInt(chars.length());
        out.writeChars(chars);
    }

    protected String readChars(ObjectInputStream in) throws IOException {
        int len = in.readInt();
        if (len <= 0) {
            return "";
        }
        char[] chars = new char[len];
        for (int i=0; i<len; i++) {
            chars[i] = in.readChar();
        }
        return new String(chars);        
    }
    

    protected Method readMethod(ObjectInputStream in) throws ClassNotFoundException, IOException {
        Method method = null;
        Class<?> klass = loadClass(readChars(in));
        String meth = readChars(in);
            
        try {
        int len = in.readInt();
        if (len > 0) {
            Class<?>[] params = new Class<?>[len]; 
            for (int i=0; i<params.length; i++) {
                params[i] = getType(readChars(in));
            }            
            method = klass.getMethod(meth, params);
        } else {
            method = klass.getMethod(meth);
        }       
        } catch (NoSuchMethodException e) {
            IOException ee = new IOException("No such method: "+meth+" for class "+klass.getName());
            ee.initCause(e);
            throw ee;
        }
        return method;
    }
    
    protected void writeMethod(ObjectOutputStream out, Method method) throws IOException {
        writeChars(out, method.getDeclaringClass().getName());
        writeChars(out, method.getName());                
        Class<?>[] params = method.getParameterTypes();
        if (params.length > 0) {
            out.writeInt(params.length);            
            for (int i=0; i<params.length; i++) {
                writeChars(out, params[i].getName());
            }
        } else {
            out.writeInt(0);
        }
    }
    
    private void writeObject(ObjectOutputStream out) throws IOException {
        out.defaultWriteObject();
        writeMethod(out, method);
    }

    private void readObject(ObjectInputStream in)
            throws ClassNotFoundException, IOException {
        in.defaultReadObject();        
        this.method = readMethod(in);        
    }

    
}
