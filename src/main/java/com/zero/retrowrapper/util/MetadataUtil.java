package com.zero.retrowrapper.util;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.IOUtils;

public class MetadataUtil {
    public static final String version = getVersion();
    public static final List<String> installerSplashes = getSplashes();

    private static final String getVersion() {
        try {
            return IOUtils.toString(ClassLoader.getSystemResourceAsStream("com/zero/retrowrapper/retrowrapperVersion.txt"), Charset.defaultCharset());
        } catch (final IOException e) {
            return "v0.0.0+Missingno";
        }
    }

    private static final List<String> getSplashes() {
        try {
            return IOUtils.readLines(ClassLoader.getSystemResourceAsStream("com/zero/retrowrapper/retrowrapperInstallerSplashes.txt"), Charset.defaultCharset());
        } catch (final IOException e) {
            final ArrayList<String> missingno = new ArrayList<>();
            missingno.add("missingno");
            return missingno;
        }
    }

}
