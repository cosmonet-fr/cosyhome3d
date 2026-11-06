/*
 * FSH3DLightLaf.java
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

import com.formdev.flatlaf.FlatLightLaf;

/**
 * Light theme of FSH3D.
 * Colors are defined in FSH3DLightLaf.properties.
 * @author Stéphane BILLOIS
 */
public class FSH3DLightLaf extends FlatLightLaf {
  public static final String NAME = "FSH3D Light";

  @Override
  public String getName() {
    return NAME;
  }

  @Override
  public String getDescription() {
    return "Light theme of FSH3D.";
  }

  @Override
  public boolean isDark() {
    return false;
  }
}
