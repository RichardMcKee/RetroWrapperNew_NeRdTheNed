package com.zero.retrowrapper.emulator.registry.handlers;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.URL;
import java.util.HashMap;

import javax.imageio.ImageIO;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.IOUtils;

import com.eclipsesource.json.Json;
import com.eclipsesource.json.JsonArray;
import com.eclipsesource.json.JsonObject;
import com.eclipsesource.json.JsonValue;
import com.zero.retrowrapper.emulator.RetroEmulator;
import com.zero.retrowrapper.emulator.registry.EmulatorHandler;

public final class SkinHandler extends EmulatorHandler {
    private final HashMap<String, byte[]> skinsCache = new HashMap<>();

    public SkinHandler(String url) {
        super(url);
    }

    @Override
    public void handle(OutputStream os, String get, byte[] data) throws IOException {
        final String username = get.replace(url, "").replace(".png", "");

        if (skinsCache.containsKey(username)) {
            os.write(skinsCache.get(username));
        } else {
            final byte[] bytes3 = downloadSkin(username);

            if (bytes3 != null) {
                final BufferedImage imgSkinRaw = ImageIO.read(new ByteArrayInputStream(bytes3));
                final BufferedImage imgSkinFixed = new BufferedImage(64, 32, BufferedImage.TYPE_INT_ARGB);
                imgSkinFixed.getGraphics().drawImage(imgSkinRaw, 0, 0, null);
                final ByteArrayOutputStream osSkin = new ByteArrayOutputStream();
                ImageIO.write(imgSkinFixed, "png", osSkin);
                osSkin.flush();
                final byte[] bytes = osSkin.toByteArray();
                os.write(bytes);
                skinsCache.put(username, bytes);
            }
        }
    }

    // TODO @Nullable?
    private static byte[] downloadSkin(String username) throws IOException {
        final File skinCache = new File(RetroEmulator.getInstance().getCacheDirectory(), username + ".png");

        try
            (InputStreamReader reader = new InputStreamReader(new URL("https://api.mojang.com/users/profiles/minecraft/" + username + "?at=" + System.currentTimeMillis()).openStream())) {
            final JsonObject profile1 = (JsonObject) Json.parse(reader);
            final String uuid = profile1.get("id").asString();
            System.out.println(uuid);

            try
                (InputStreamReader reader2 = new InputStreamReader(new URL("https://sessionserver.mojang.com/session/minecraft/profile/" + uuid).openStream())) {
                final JsonObject profile2 = (JsonObject) Json.parse(reader2);
                final JsonArray properties = (JsonArray) profile2.get("properties");
                String base64 = "";

                for (final JsonValue property : properties) {
                    final JsonObject propertyj = property.asObject();

                    if ("textures".equalsIgnoreCase(propertyj.get("name").asString())) {
                        base64 = propertyj.get("value").asString();
                    }
                }

                final JsonObject textures1 = (JsonObject) Json.parse(new String(Base64.decodeBase64(base64)));
                final JsonObject textures = (JsonObject) textures1.get("textures");
                final JsonObject skin = (JsonObject) textures.get("SKIN");
                final String skinURL = skin.get("url").asString();
                System.out.println(skinURL);
                final InputStream is = new URL(skinURL).openStream();
                final byte[] skinBytes = IOUtils.toByteArray(is);

                try
                    (FileOutputStream fos = new FileOutputStream(skinCache)) {
                    fos.write(skinBytes);
                }

                return skinBytes;
            }
        } catch (final Exception e) {
            e.printStackTrace();

            if (skinCache.exists()) {
                try
                    (FileInputStream fis = new FileInputStream(skinCache)) {
                    return IOUtils.toByteArray(fis);
                }
            }

            return null;
        }
    }
}
