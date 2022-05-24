package org.prank;

import javax.swing.*;
import java.io.PrintWriter;
import java.io.StringWriter;

public class Main {
    public static void main(String[] args) {
        try {
            ConfigReader.readWorldGenConfig();
            ConfigReader.readGregTechConfig();
            new MainFrame();
        } catch (Exception e) {
            StringWriter stringWriter = new StringWriter();
            e.printStackTrace(new PrintWriter(stringWriter));
            JOptionPane.showMessageDialog(null, stringWriter.toString(), "Some shit happened", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }
}