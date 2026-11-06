/*
 * XMLTools.java
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
package com.eteks.sweethome3d.tools;

import javax.xml.XMLConstants;
import javax.xml.parsers.SAXParserFactory;

/**
 * XML utilities.
 * @author Stéphane BILLOIS
 */
public final class XMLTools {
  private XMLTools() {
  }

  /**
   * Returns a SAX parser factory protected against XML external entities (XXE)
   * and entity expansion attacks. Documents with a DOCTYPE are still accepted,
   * but no external DTD or entity is ever loaded.
   */
  public static SAXParserFactory newSecureSAXParserFactory() {
    SAXParserFactory factory = SAXParserFactory.newInstance();
    factory.setValidating(false);
    factory.setXIncludeAware(false);
    trySetFeature(factory, XMLConstants.FEATURE_SECURE_PROCESSING, true);
    trySetFeature(factory, "http://xml.org/sax/features/external-general-entities", false);
    trySetFeature(factory, "http://xml.org/sax/features/external-parameter-entities", false);
    trySetFeature(factory, "http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
    return factory;
  }

  private static void trySetFeature(SAXParserFactory factory, String feature, boolean value) {
    try {
      factory.setFeature(feature, value);
    } catch (Exception ex) {
      // Feature not supported by this parser
    }
  }
}
