package com.zero.retrowrapper.util;

import java.awt.Component;
import java.awt.Frame;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.imageio.ImageIO;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;

public class SwingUtils {
    public static void addJButtonCentered(JFrame frame, JButton component) {
        component.setHorizontalAlignment(SwingConstants.CENTER);
        component.setVerticalAlignment(SwingConstants.CENTER);
        addJComponentCentered(frame, component);
    }

    public static void addJComponentCentered(JFrame frame, JComponent component) {
        component.setAlignmentX(Component.CENTER_ALIGNMENT);
        component.setAlignmentY(Component.CENTER_ALIGNMENT);
        frame.add(component);
    }

    public static void addJLabelCentered(JFrame frame, JLabel component) {
        component.setHorizontalAlignment(SwingConstants.CENTER);
        component.setVerticalAlignment(SwingConstants.CENTER);
        addJComponentCentered(frame, component);
    }

    public static void addJTextFieldCentered(JFrame frame, JTextField component) {
        component.setHorizontalAlignment(SwingConstants.CENTER);
        addJComponentCentered(frame, component);
    }

    public static void loadIconsOnFrames() {
        final List<File> iconList = new ArrayList<>();
        final File[] files = { FileUtils.tryFindResourceFile("icons/icon_16x16.png"), FileUtils.tryFindResourceFile("icons/icon_32x32.png") };
        CollectionUtils.addNonNullToCollection(iconList, files);

        if (!iconList.isEmpty()) {
            System.out.println("Loading current icons for window from: " + iconList);
            // TODO Re-add?
            //Display.setIcon(new ByteBuffer[]{loadIcon(e), loadIcon(bigIcon)});
            final java.awt.Frame[] frames = java.awt.Frame.getFrames();

            if (frames != null) {
                final List<BufferedImage> bufferedImageList = new ArrayList<>();

                for (final File icon : iconList) {
                    try {
                        final BufferedImage iconImage = ImageIO.read(icon);
                        bufferedImageList.add(iconImage);
                    } catch (final IOException e) {
                        // TODO Better error handling
                        e.printStackTrace();
                    }
                }

                if (!bufferedImageList.isEmpty()) {
                    for (final Frame frame : frames) {
                        frame.setIconImages(bufferedImageList);
                    }
                }
            }
        } else {
            System.out.println("Could not find any icon files!");
        }
    }

    private SwingUtils() {
        // As this is a helper class, there should be no reason to instantiate an instance of it.
    }
}
