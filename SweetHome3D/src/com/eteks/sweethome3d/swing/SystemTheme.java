/*
 * SystemTheme.java
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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.concurrent.TimeUnit;

import javax.swing.LookAndFeel;
import javax.swing.UIManager;

import com.eteks.sweethome3d.model.UserPreferences;
import com.eteks.sweethome3d.tools.OperatingSystem;

/**
 * Detection of the light or dark mode of the operating system
 * and installation of the look and feel matching a theme.
 * @author Stéphane BILLOIS
 */
public final class SystemTheme {
  private static final long COMMAND_TIMEOUT_MS = 2000;

  private SystemTheme() {
  }

  /**
   * Installs the look and feel matching the given <code>theme</code>.
   * The <code>swing.defaultlaf</code> system property, when set, has priority.
   */
  public static void installLookAndFeel(UserPreferences.Theme theme) throws Exception {
    String defaultLaf = System.getProperty("swing.defaultlaf");
    if (defaultLaf != null) {
      UIManager.setLookAndFeel(defaultLaf);
      return;
    }
    // Allows to force a theme from command line with -Dcom.eteks.sweethome3d.theme=DARK
    String forcedTheme = System.getProperty("com.eteks.sweethome3d.theme");
    if (forcedTheme != null) {
      try {
        theme = UserPreferences.Theme.valueOf(forcedTheme.toUpperCase());
      } catch (IllegalArgumentException ex) {
        // Keep theme in preferences
      }
    }
    if (theme == null) {
      theme = UserPreferences.Theme.SYSTEM;
    }
    if (theme == UserPreferences.Theme.SYSTEM) {
      theme = isSystemDark()
          ? UserPreferences.Theme.DARK
          : UserPreferences.Theme.LIGHT;
    }
    switch (theme) {
      case CLASSIC :
        UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        break;
      default :
        LookAndFeel lookAndFeel = theme == UserPreferences.Theme.DARK
            ? new FSH3DDarkLaf()
            : new FSH3DLightLaf();
        UIManager.setLookAndFeel(lookAndFeel);
        // FlatLaf classes are loaded by FSH3D class loader, not by the system one
        UIManager.put("ClassLoader", lookAndFeel.getClass().getClassLoader());
        break;
    }
  }

  /**
   * Returns <code>true</code> if the operating system is set in dark mode.
   * Returns <code>false</code> if it's in light mode or if the mode can't be found.
   */
  public static boolean isSystemDark() {
    try {
      if (OperatingSystem.isMacOSX()) {
        String style = runCommand("defaults", "read", "-g", "AppleInterfaceStyle");
        return style != null && style.trim().equalsIgnoreCase("Dark");
      } else if (OperatingSystem.isWindows()) {
        String value = runCommand("reg", "query",
            "HKCU\\Software\\Microsoft\\Windows\\CurrentVersion\\Themes\\Personalize", "/v", "AppsUseLightTheme");
        return value != null && value.matches("(?s).*AppsUseLightTheme\\s+REG_DWORD\\s+0x0+\\b.*");
      } else {
        return isLinuxDark();
      }
    } catch (SecurityException ex) {
      return false;
    }
  }

  private static boolean isLinuxDark() {
    // 1. XDG desktop portal, supported by GNOME, KDE and others, also inside Flatpak sandbox
    //    color-scheme: 0 = no preference, 1 = prefer dark, 2 = prefer light
    String [] portalCommand = {"gdbus", "call", "--session",
        "--dest", "org.freedesktop.portal.Desktop",
        "--object-path", "/org/freedesktop/portal/desktop",
        "--method", "org.freedesktop.portal.Settings.ReadOne",
        "org.freedesktop.appearance", "color-scheme"};
    String portal = runCommand(portalCommand);
    if (portal == null) {
      // Older portals only implement Read method
      portalCommand [8] = "org.freedesktop.portal.Settings.Read";
      portal = runCommand(portalCommand);
    }
    if (portal != null) {
      if (portal.matches("(?s).*uint32 1\\b.*")) {
        return true;
      } else if (portal.matches("(?s).*uint32 2\\b.*")) {
        return false;
      }
    }
    // 2. GNOME settings
    String colorScheme = runCommand("gsettings", "get", "org.gnome.desktop.interface", "color-scheme");
    if (colorScheme != null && colorScheme.contains("prefer-dark")) {
      return true;
    }
    // 3. GTK theme name
    String gtkTheme = System.getenv("GTK_THEME");
    if (gtkTheme == null) {
      gtkTheme = runCommand("gsettings", "get", "org.gnome.desktop.interface", "gtk-theme");
    }
    if (gtkTheme != null && gtkTheme.toLowerCase().contains("dark")) {
      return true;
    }
    // 4. KDE color scheme
    String home = System.getProperty("user.home");
    if (home != null) {
      String kdeColorScheme = readIniValue(new File(home, ".config/kdeglobals"), "General", "ColorScheme");
      if (kdeColorScheme != null && kdeColorScheme.toLowerCase().contains("dark")) {
        return true;
      }
    }
    return false;
  }

  /**
   * Returns the standard output of the given command,
   * or <code>null</code> if it failed or didn't end quickly.
   */
  private static String runCommand(String ... command) {
    Process process = null;
    try {
      ProcessBuilder builder = new ProcessBuilder(command);
      builder.redirectErrorStream(true);
      process = builder.start();
      process.getOutputStream().close();
      InputStream in = process.getInputStream();
      StringBuilder output = new StringBuilder();
      long end = System.currentTimeMillis() + COMMAND_TIMEOUT_MS;
      byte [] buffer = new byte [1024];
      while (System.currentTimeMillis() < end) {
        if (in.available() > 0) {
          int length = in.read(buffer);
          if (length == -1) {
            break;
          }
          output.append(new String(buffer, 0, length, "UTF-8"));
        } else if (process.waitFor(20, TimeUnit.MILLISECONDS)) {
          // Read remaining output
          for (int length; (length = in.read(buffer)) != -1; ) {
            output.append(new String(buffer, 0, length, "UTF-8"));
          }
          return process.exitValue() == 0
              ? output.toString()
              : null;
        }
      }
      return null;
    } catch (IOException ex) {
      return null;
    } catch (InterruptedException ex) {
      Thread.currentThread().interrupt();
      return null;
    } finally {
      if (process != null) {
        process.destroy();
      }
    }
  }

  /**
   * Returns the value of the given key in a section of an INI file, or <code>null</code>.
   */
  private static String readIniValue(File file, String section, String key) {
    if (!file.isFile()) {
      return null;
    }
    BufferedReader reader = null;
    try {
      reader = new BufferedReader(new InputStreamReader(new FileInputStream(file), "UTF-8"));
      boolean inSection = false;
      for (String line; (line = reader.readLine()) != null; ) {
        line = line.trim();
        if (line.startsWith("[")) {
          inSection = line.equals("[" + section + "]");
        } else if (inSection && line.startsWith(key + "=")) {
          return line.substring(key.length() + 1).trim();
        }
      }
      return null;
    } catch (IOException ex) {
      return null;
    } finally {
      if (reader != null) {
        try {
          reader.close();
        } catch (IOException ex) {
        }
      }
    }
  }
}
