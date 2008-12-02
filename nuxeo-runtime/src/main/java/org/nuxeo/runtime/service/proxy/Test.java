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

/**
 * @author <a href="mailto:bs@nuxeo.com">Bogdan Stefanescu</a>
 *
 */
public class Test<T> {

    T obj;
    
    public Test(T obj) {this.obj = obj;}
    
    public static void main(String[] args) {
        Test<String> t = new Test<String>("abc");
        System.out.println(t.obj);
        System.out.println(t.getClass().getTypeParameters()[0].getName());
        System.out.println(t.getClass().getTypeParameters()[0].getBounds().length);
        System.out.println(t.getClass().getTypeParameters()[0].getBounds()[0]);
        System.out.println(t.getClass().getTypeParameters()[0].getGenericDeclaration());
    }
    
    
}
