package com.zero.retrowrapper.installer;

import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.net.URISyntaxException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.Scanner;

import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.WindowConstants;

import com.eclipsesource.json.Json;
import com.eclipsesource.json.JsonObject;
import com.eclipsesource.json.JsonValue;
import com.zero.retrowrapper.util.MetadataUtil;

public class Installer {

    private final static Random rand = new Random();

    //TODO: F1X TH1S. Everything must go!
    static int versionCount;
    static int wrappedVersionCount;
    static String workingDirectory;
    static File directory;
    static File[] directories;
    static File versions;
    static JButton install;
    static JButton uninstall;

    static DefaultListModel<String> model = new DefaultListModel<>();
    static JList<String> list = new JList<>(model);

    public String defaultWorkingDirectory() {
        final String OS = System.getProperty("os.name").toUpperCase();

        if (OS.contains("WIN")) { // windows uses the %appdata%/.minecraft structure
            return System.getenv("AppData") + File.separator + ".minecraft";
        }

        if (OS.contains("MAC")) { // mac os uses %user%/Library/Library/Application Support/minecraft
            return System.getProperty("user.home") + File.separator + "Library" + File.separator + "Application Support" + File.separator + "minecraft";
        }

        return System.getProperty("user.home") + File.separator + ".minecraft";
    }

    boolean refreshList(String givenDirectory) {
        versionCount = 0;
        wrappedVersionCount = 0;
        model.removeAllElements();

        if (!givenDirectory.isEmpty()) {
            directory = new File(givenDirectory);
            directory.mkdirs();
            directories = null;
            versions = new File(directory, "versions");

            if (versions.exists()) {
                directories = versions.listFiles();
            }

            if ((directories != null) && (directories.length != 0)) {
                Arrays.sort(directories);

                // add items

                for (final File f : directories) {
                    if (f.isDirectory()) {
                        final File json = new File(f, f.getName() + ".json");
                        final File jar = new File(f, f.getName() + ".jar");

                        if (json.exists() && jar.exists() && !f.getName().contains("-wrapped")) {
                            try
                                (Scanner s = new Scanner(json).useDelimiter("\\A")) {
                                final String content = s.next();

                                if (content.contains("old_") && !content.contains("retrowrapper")) {
                                    if (new File(versions, f.getName() + "-wrapped").exists()) {
                                        wrappedVersionCount++;
                                        model.addElement(f.getName() + " - already wrapped");
                                    } else {
                                        versionCount++;
                                        model.addElement(f.getName());
                                    }
                                }
                            } catch (final FileNotFoundException e) {
                                // TODO Auto-generated catch block
                                e.printStackTrace();
                            }
                        }
                    }
                }
            }
        }

        // button visibility

        if (givenDirectory.isEmpty()) {
            install.setEnabled(false);
            uninstall.setEnabled(false);
            JOptionPane.showMessageDialog(null, "No directory / minecraft directory detected!\n", "Error", JOptionPane.INFORMATION_MESSAGE);
            return false;
        }

        if (!versions.exists()) {
            install.setEnabled(false);
            uninstall.setEnabled(false);
            JOptionPane.showMessageDialog(null, "No Minecraft versions folder found!", "Error", JOptionPane.INFORMATION_MESSAGE);
            return false;
        }

        if ((versionCount == 0) && (wrappedVersionCount == 0)) {
            install.setEnabled(false);
            uninstall.setEnabled(true);
            JOptionPane.showMessageDialog(null, "No wrappable versions found!", "Error", JOptionPane.INFORMATION_MESSAGE);
            return false;
        }

        if (versionCount == 0) {
            install.setEnabled(true);
            uninstall.setEnabled(true);
            JOptionPane.showMessageDialog(null, "All detected versions have already been wrapped!", "Info", JOptionPane.INFORMATION_MESSAGE);
        } else {
            install.setEnabled(true);
            uninstall.setEnabled(true);
        }

        return true;
    }

    public Installer() throws Exception {
        workingDirectory = defaultWorkingDirectory();
        //final File[] directoriesFinal = directories;
        UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        final JFrame frame = new JFrame("Retrowrapper - NeRd Fork");
        frame.setPreferredSize(new Dimension(654, 420));
        frame.setLayout(null);
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.setResizable(false); //add resizing later
        final JLabel label = new JLabel("Retrowrapper Installer");
        label.setFont(label.getFont().deriveFont(20f).deriveFont(Font.BOLD));
        label.setHorizontalAlignment(SwingConstants.CENTER);
        label.setBounds(0, 10, 654, 40);
        frame.add(label);
        final JLabel label3 = new JLabel(MetadataUtil.version + " - " + MetadataUtil.installerSplashes.get(rand.nextInt(MetadataUtil.installerSplashes.size())));
        label3.setFont(label.getFont().deriveFont(12f));
        label3.setHorizontalAlignment(SwingConstants.CENTER);
        label3.setBounds(0, 30, 654, 40);
        frame.add(label3);
        final JLabel label2 = new JLabel("\u00a92018 000");
        label2.setFont(label2.getFont().deriveFont(12f));
        label2.setHorizontalAlignment(SwingConstants.CENTER);
        label2.setBounds(0, 360, 654, 20);
        frame.add(label2);
        final JTextField workDir = new JTextField(workingDirectory);
        workDir.setHorizontalAlignment(SwingConstants.CENTER);
        workDir.setBounds((654 / 2) - 150, 65, 300, 20);
        workDir.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                final String workDirPath = ((JTextField)e.getSource()).getText();
                final File minecraftDir = new File(workDirPath);

                if (minecraftDir.exists() && refreshList(workDirPath)) {
                    workingDirectory = workDirPath;
                } else {
                    if (minecraftDir.exists()) {
                    } else {
                        JOptionPane.showMessageDialog(null, "No directory / minecraft directory detected!\n", "Error", JOptionPane.INFORMATION_MESSAGE);
                    }

                    ((JTextField)e.getSource()).setText(workingDirectory);
                    refreshList(workingDirectory);
                }
            }
        });
        frame.add(workDir);
        final JScrollPane scrollList = new JScrollPane(list);
        scrollList.setBounds((654 / 2) - 150, 100, 300, 140);
        frame.add(scrollList);
        uninstall = new JButton("Uninstall ALL versions"); //uninstaller code
        uninstall.setBounds((654 / 2) - 100, 312, 200, 30);
        uninstall.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                for (final File f : directories) {
                    if (f.isDirectory() && f.getName().contains("-wrapped")) {
                        deleteDirectory(f);
                    }
                }

                final File libDir = new File(directory, "libraries" + File.separator + "com" + File.separator + "zero");

                if (libDir.exists()) {
                    deleteDirectory(libDir);
                }

                JOptionPane.showMessageDialog(null, "Successfully uninstalled wrapper", "Success", JOptionPane.INFORMATION_MESSAGE);
                refreshList(workingDirectory);
                //System.exit(0);
            }
        });
        frame.add(uninstall);
        install = new JButton("Install"); //installation code
        install.setBounds((654 / 2) - 100, 270, 200, 40);
        install.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                final List<String> versionList = list.getSelectedValuesList();
                final StringBuilder finalVersions = new StringBuilder();
                final File libsDir = new File(directory, "libraries" + File.separator + "com" + File.separator + "zero");

                if (libsDir.exists()) {
                    deleteDirectory(libsDir); // Makes sure that the library gets reinstalled
                }

                for (String version : versionList) {
                    if (version.contains("- already wrapped")) {
                        version = version.replace(" - already wrapped", "");
                        deleteDirectory(new File(directory, "versions" + File.separator + version + "-wrapped"));
                    }

                    try
                        (Reader s = new FileReader(new File(versions, version + File.separator + version + ".json"))) {
                        finalVersions.append(version).append("\n");
                        final JsonObject versionJson = Json.parse(s).asObject();
                        final String versionWrapped = version + "-wrapped";
                        // Add the RetroWrapper library to the list of libraries. A library is a JSON object, and libraries are stored in an array of JSON objects.
                        final JsonObject retrowrapperLibraryJson = Json.object().add("name", "com.zero:retrowrapper:installer");
                        final JsonValue newLibraries = versionJson.get("libraries");
                        newLibraries.asArray().add(retrowrapperLibraryJson);
                        versionJson.set("libraries", newLibraries);

                        // Replace version ID with the wrapped version ID (e.g "c0.30-3" with "c0.30-3-wrapped")
                        if (!versionJson.getString("id", "null").equals(version)) {
                            JOptionPane.showMessageDialog(null, "The version ID " + versionJson.getString("id", "null") + " found in the JSON file " + version + File.separator + version + ".json" + "did not match the expected version ID " + version + ". Things will not go as planned!", "Error", JOptionPane.ERROR_MESSAGE);
                        }

                        versionJson.set("id",  versionWrapped);
                        // Replace any of Mojangs tweakers with RetroWrapper tweakers
                        String modifiedLaunchArgs = versionJson.getString("minecraftArguments", "null");

                        if (modifiedLaunchArgs.contains("VanillaTweaker")) {
                            modifiedLaunchArgs = modifiedLaunchArgs.replace("net.minecraft.launchwrapper.AlphaVanillaTweaker", "com.zero.retrowrapper.RetroTweaker");
                            modifiedLaunchArgs = modifiedLaunchArgs.replace("net.minecraft.launchwrapper.IndevVanillaTweaker", "com.zero.retrowrapper.RetroTweaker");
                        } else {
                            modifiedLaunchArgs = modifiedLaunchArgs.replace("--assetsDir ${game_assets}", "--assetsDir ${game_assets} --tweakClass com.zero.retrowrapper.RetroTweaker");
                        }

                        versionJson.set("minecraftArguments", modifiedLaunchArgs);
                        final File wrapDir = new File(versions, versionWrapped + File.separator);
                        wrapDir.mkdirs();
                        final File libDir = new File(directory, "libraries" + File.separator + "com" + File.separator + "zero" + File.separator + "retrowrapper" + File.separator + "installer");
                        libDir.mkdirs();

                        try
                            (FileOutputStream fos = new FileOutputStream(new File(wrapDir, versionWrapped + ".json"))) {
                            Files.copy(new File(versions, version + File.separator + version + ".jar").toPath(), new File(wrapDir, versionWrapped + ".jar").toPath(), StandardCopyOption.REPLACE_EXISTING);
                            fos.write(versionJson.toString().getBytes());
                            fos.close();
                            final File jar = new File(Installer.class.getProtectionDomain().getCodeSource().getLocation().toURI());
                            Files.copy(jar.toPath(), new File(libDir, "retrowrapper-installer.jar").toPath(), StandardCopyOption.REPLACE_EXISTING);
                        } catch (IOException | URISyntaxException ee) {
                            ee.printStackTrace();
                        }
                    } catch (final IOException ee) {
                        ee.printStackTrace();
                        // TODO Better error handling
                    }
                }

                JOptionPane.showMessageDialog(null, "Successfully wrapped version\n" + finalVersions.toString(), "Success", JOptionPane.INFORMATION_MESSAGE);
                refreshList(workingDirectory);
                //System.exit(0);
            }
        });
        frame.add(install);
        frame.pack();
        frame.setVisible(true);
        frame.setLocationRelativeTo(null);
        refreshList(workingDirectory);
    }

    public static void main(String[] args) {
        try {
            new Installer();
        } catch (final Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "Exception occured!\n" + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private static void deleteDirectory(File f) {
        try {
            Files.walkFileTree(f.toPath(), new SimpleFileVisitor<Path>() {
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                    Files.delete(file);
                    return FileVisitResult.CONTINUE;
                }
                @Override
                public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                    Files.delete(dir);
                    return FileVisitResult.CONTINUE;
                }
            });
        } catch (final IOException ee) {
            ee.printStackTrace();
        }
    }
}
