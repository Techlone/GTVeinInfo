package org.prank;

import static org.prank.ConfigReader.readWorldGenConfig;

public class Main {
    public static void main(String[] args) {
        readWorldGenConfig();
        new MainFrame().setVisible(true);
    }
}
