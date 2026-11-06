/*
 * HomeSerializationFilter.java
 *
 * Cosy Home 3D, Copyright (c) 2026 Stéphane BILLOIS
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
package com.eteks.sweethome3d.io;

import java.io.InvalidClassException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Allow list of the classes that may be deserialized from the <code>Home</code> entry
 * of a .sh3d file.
 * Java deserialization can instantiate any class available in the classpath, and some
 * classes can be chained to run code ("gadgets"). Restricting the stream to the model
 * classes and a few JDK value classes makes a forged home file harmless.
 * Plugins storing their own objects in homes can extend the list with the
 * <code>com.eteks.sweethome3d.allowedSerializedClasses</code> system property,
 * a comma separated list of class names or package prefixes ending with a dot.
 * @author Stéphane BILLOIS
 */
public final class HomeSerializationFilter {
  public static final String ALLOWED_CLASSES_PROPERTY = "com.eteks.sweethome3d.allowedSerializedClasses";

  private static final String [] ALLOWED_PREFIXES = {
      "com.eteks.sweethome3d.model."};

  private static final Set<String> ALLOWED_CLASSES = new HashSet<String>(Arrays.asList(
      // Cosy Home 3D classes outside of model package
      "com.eteks.sweethome3d.io.DefaultPatternTexture",
      "com.eteks.sweethome3d.io.HomeURLContent",
      "com.eteks.sweethome3d.tools.URLContent",
      "com.eteks.sweethome3d.tools.TemporaryURLContent",
      "com.eteks.sweethome3d.tools.ResourceURLContent",
      "com.eteks.sweethome3d.tools.SimpleURLContent",
      // Values
      "java.lang.Boolean",
      "java.lang.Byte",
      "java.lang.Character",
      "java.lang.Double",
      "java.lang.Enum",
      "java.lang.Float",
      "java.lang.Integer",
      "java.lang.Long",
      "java.lang.Number",
      "java.lang.Short",
      "java.lang.String",
      "java.math.BigDecimal",
      "java.math.BigInteger",
      "java.net.URL",
      "java.util.Date",
      "java.util.Locale",
      "java.util.TimeZone",
      "java.util.SimpleTimeZone",
      "sun.util.calendar.ZoneInfo",
      // Collections
      "java.util.ArrayList",
      "java.util.Arrays$ArrayList",
      "java.util.LinkedList",
      "java.util.HashMap",
      "java.util.LinkedHashMap",
      "java.util.TreeMap",
      "java.util.HashSet",
      "java.util.LinkedHashSet",
      "java.util.TreeSet",
      "java.util.Collections$EmptyList",
      "java.util.Collections$EmptyMap",
      "java.util.Collections$EmptySet",
      "java.util.Collections$SingletonList",
      "java.util.Collections$SingletonMap",
      "java.util.Collections$SingletonSet",
      "java.util.Collections$UnmodifiableCollection",
      "java.util.Collections$UnmodifiableList",
      "java.util.Collections$UnmodifiableRandomAccessList",
      "java.util.Collections$UnmodifiableMap",
      "java.util.Collections$UnmodifiableSet",
      "java.util.Collections$UnmodifiableSortedMap",
      "java.util.Collections$UnmodifiableSortedSet"));

  private static List<String> extraAllowedClasses;

  private HomeSerializationFilter() {
  }

  /**
   * Throws an <code>InvalidClassException</code> if the class called <code>className</code>
   * isn't allowed to be read from a home stream.
   */
  public static void checkClass(String className) throws InvalidClassException {
    if (!isAllowed(className)) {
      throw new InvalidClassException(className, "Class not allowed in a home file");
    }
  }

  /**
   * Returns <code>true</code> if the class called <code>className</code>
   * may be read from a home stream. Arrays are accepted if their component type is.
   */
  public static boolean isAllowed(String className) {
    if (className.startsWith("[")) {
      String componentName = className.replaceFirst("^\\[+", "");
      if (componentName.length() == 1) {
        // Array of primitive type
        return "ZBCSIJFD".indexOf(componentName.charAt(0)) >= 0;
      } else if (componentName.startsWith("L") && componentName.endsWith(";")) {
        className = componentName.substring(1, componentName.length() - 1);
      } else {
        return false;
      }
    }
    if (ALLOWED_CLASSES.contains(className)) {
      return true;
    }
    for (String prefix : ALLOWED_PREFIXES) {
      if (className.startsWith(prefix)) {
        return true;
      }
    }
    for (String allowed : getExtraAllowedClasses()) {
      if (allowed.endsWith(".")
            ? className.startsWith(allowed)
            : className.equals(allowed)) {
        return true;
      }
    }
    return false;
  }

  private static synchronized List<String> getExtraAllowedClasses() {
    if (extraAllowedClasses == null) {
      extraAllowedClasses = new ArrayList<String>();
      try {
        String property = System.getProperty(ALLOWED_CLASSES_PROPERTY, "");
        for (String name : property.split("\\s*,\\s*")) {
          if (name.length() > 0) {
            extraAllowedClasses.add(name);
          }
        }
      } catch (SecurityException ex) {
        // Keep default list
      }
    }
    return extraAllowedClasses;
  }
}
