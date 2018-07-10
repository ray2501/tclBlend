/*
 * TclObjectMemory.java
 *
 * Copyright (c) 1997 Cornell University.
 * Copyright (c) 1997 Sun Microsystems, Inc.
 *
 * See the file "license.terms" for information on usage and
 * redistribution of this file, and for a DISCLAIMER OF ALL
 * WARRANTIES.
 * 
 * RCS: @(#) $Id: TclObjectMemory.java,v 1.1 2006/06/07 01:53:51 mdejong Exp $
 *
 */

package tcl.lang;

import java.util.*;
import java.lang.reflect.*;
import java.util.*;

/**
 * This class is used to test memory space requrements for TclObject
 * and related classes.
 */

class TclObjectMemory implements Command {

    public void cmdProc(Interp interp, TclObject[] objv)
	    throws TclException {
	if (objv.length != 1) {
	    throw new TclNumArgsException(interp, 1, objv, "");
	}

        // Report memory sizes for common TclObjects

        StringBuffer sb = new StringBuffer();
        MemoryCounter mc = new MemoryCounter();
        long bytes;
        TclObject tobj;

        tobj = TclString.newInstance("hi");
        bytes = mc.estimate( tobj );
        sb.append("TclObject " + bytes + " bytes");
        sb.append("\n");

        bytes = mc.estimate( tobj.getInternalRep() );
        sb.append("TclString " + bytes + " bytes");
        sb.append("\n");

        tobj = TclInteger.newInstance(1);
        bytes = mc.estimate( tobj.getInternalRep() );
        sb.append("TclInteger " + bytes + " bytes");
        sb.append("\n");

        tobj = TclDouble.newInstance(1.0);
        bytes = mc.estimate( tobj.getInternalRep() );
        sb.append("TclDouble " + bytes + " bytes");
        sb.append("\n");

        tobj = TclList.newInstance();
        bytes = mc.estimate( tobj.getInternalRep() );
        sb.append("TclList " + bytes + " bytes");

        interp.setResult( sb.toString() );
    }
}

class MemorySizes {
  private final Map primitiveSizes = new IdentityHashMap() {
    {
      put(boolean.class, new Integer(1));
      put(byte.class, new Integer(1));
      put(char.class, new Integer(2));
      put(short.class, new Integer(2));
      put(int.class, new Integer(4));
      put(float.class, new Integer(4));
      put(double.class, new Integer(8));
      put(long.class, new Integer(8));
    }
  };
  public int getPrimitiveFieldSize(Class clazz) {
    return ((Integer) primitiveSizes.get(clazz)).intValue();
  }
  public int getPrimitiveArrayElementSize(Class clazz) {
    return getPrimitiveFieldSize(clazz);
  }
  public int getPointerSize() {
    return 4;
  }
  public int getClassSize() {
    return 8;
  }
}

/**
 * This class can estimate how much memory an Object uses.  It is
 * fairly accurate for JDK 1.4.2.  It is based on the newsletter #29.
 */
class MemoryCounter {
  private static final MemorySizes sizes = new MemorySizes();
  private final Map visited = new IdentityHashMap();
  private final Stack stack = new Stack();

  public synchronized long estimate(Object obj) {
    long result = _estimate(obj);
    while (!stack.isEmpty()) {
      result += _estimate(stack.pop());
    }
    visited.clear();
    return result;
  }

  private boolean skipObject(Object obj) {
    if (obj instanceof String) {
      // this will not cause a memory leak since
      // unused interned Strings will be thrown away
      if (obj == ((String) obj).intern()) {
        return true;
      }
    }
    return (obj == null)
        || visited.containsKey(obj);
  }

  private long _estimate(Object obj) {
    if (skipObject(obj)) return 0;
    visited.put(obj, null);
    long result = 0;
    Class clazz = obj.getClass();
    if (clazz.isArray()) {
      return _estimateArray(obj);
    }
    while (clazz != null) {
      Field[] fields = clazz.getDeclaredFields();
      for (int i = 0; i < fields.length; i++) {
        if (!Modifier.isStatic(fields[i].getModifiers())) {
          if (fields[i].getType().isPrimitive()) {
            result += sizes.getPrimitiveFieldSize(
                fields[i].getType());
          } else {
            result += sizes.getPointerSize();
            fields[i].setAccessible(true);
            try {
              Object toBeDone = fields[i].get(obj);
              if (toBeDone != null) {
                stack.add(toBeDone);
              }
            } catch (IllegalAccessException ex) {}
          }
        }
      }
      clazz = clazz.getSuperclass();
    }
    result += sizes.getClassSize();
    return roundUpToNearestEightBytes(result);
  }

  private long roundUpToNearestEightBytes(long result) {
    if ((result % 8) != 0) {
      result += 8 - (result % 8);
    }
    return result;
  }

  protected long _estimateArray(Object obj) {
    long result = 16;
    int length = Array.getLength(obj);
    if (length != 0) {
      Class arrayElementClazz = obj.getClass().getComponentType();
      if (arrayElementClazz.isPrimitive()) {
        result += length *
            sizes.getPrimitiveArrayElementSize(arrayElementClazz);
      } else {
        for (int i = 0; i < length; i++) {
          result += sizes.getPointerSize() +
              _estimate(Array.get(obj, i));
        }
      }
    }
    return result;
  }

  public static void main(String[] args) throws Exception {
      String name1 = args[0];
      String name2 = args[1];

      MemoryCounter mc = new MemoryCounter();

      Class cl = Class.forName(name1); 
      long size = mc.estimate( cl.newInstance() );
      System.out.println("sizeof " + name1 + " is " + size);

      cl = Class.forName(name2); 
      size = mc.estimate( cl.newInstance() );
      System.out.println("sizeof " + name2 + " is " + size);
  }
}



