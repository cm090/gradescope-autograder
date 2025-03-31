package rhit.presentation;

import javax.swing.JFrame;
import rhit.domain.PropertiesLoader;

/*
  Gradescope Autograder: Builder Tool
  Copyright (C) 2025 Canon Maranda <https://about.canon.click>

  This program is free software: you can redistribute it and/or modify
  it under the terms of the GNU General Public License as published by
  the Free Software Foundation, either version 3 of the License, or
  (at your option) any later version.

  This program is distributed in the hope that it will be useful,
  but WITHOUT ANY WARRANTY; without even the implied warranty of
  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
  GNU General Public License for more details.

  You should have received a copy of the GNU General Public License
  along with this program. If not, see <https://www.gnu.org/licenses/>.
*/

public class AutograderBuilder {
  public static void main(String[] args) {
    new AutograderBuilder().initialize();
  }

  public void initialize() {
    JFrame frame = new JFrame(PropertiesLoader.get("windowTitle"));
    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    InterfaceUtils.setFrame(frame);

    SwingGui.setVisibleFrame(new DirectorySelector());
    SwingGui.showFrame();
  }
}
