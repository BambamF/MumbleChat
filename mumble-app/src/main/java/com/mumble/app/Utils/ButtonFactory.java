package com.mumble.app.Utils;

import java.awt.Cursor;

import javax.swing.*;

public class ButtonFactory {
    
    public static JButton createTransparentButton(String iconPath){
        JButton button = new JButton(new ImageIcon(iconPath));
        button.setOpaque(false);
        button.setContentAreaFilled(false);
        button.setBorderPainted(false);
        button.setFocusPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));

        return button;
    }

}
