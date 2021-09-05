package com.zero.retrowrapper.emulator.registry.handlers;


import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;

import org.apache.commons.io.IOUtils;

import com.zero.retrowrapper.emulator.registry.EmulatorHandler;

public final class ResourcesHandlerBeta extends EmulatorHandler {
    public ResourcesHandlerBeta() {
        super("/MinecraftResources/");
    }

    @Override
    public void handle(OutputStream os, String get, byte[] data) throws IOException {
        final URL resourceURL = new URL("http://s3.amazonaws.com" + get);
        final InputStream is = resourceURL.openStream();
        os.write(IOUtils.toByteArray(is));
    }
}
