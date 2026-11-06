/*
 * FSH3DDarkLaf.java
 *
 * FSH3D, Copyright (c) 2026 Stéphane BILLOIS
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
package com.eteks.sweethome3d.swing;

import com.formdev.flatlaf.FlatDarkLaf;

/**
 * Dark theme of FSH3D, based on the blue of its logo.
 * Colors are defined in FSH3DDarkLaf.properties.
 * @author Stéphane BILLOIS
 */
public class FSH3DDarkLaf extends FlatDarkLaf {
  public static final String NAME = "FSH3D Dark";

  @Override
  public String getName() {
    return NAME;
  }

  @Override
  public String getDescription() {
    return "Dark theme of FSH3D, based on the blue of its logo.";
  }

  @Override
  public boolean isDark() {
    return true;
  }
}
