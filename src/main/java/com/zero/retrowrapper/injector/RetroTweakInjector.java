package com.zero.retrowrapper.injector;

import static org.objectweb.asm.Opcodes.ASM4;
import static org.objectweb.asm.Opcodes.ASTORE;
import static org.objectweb.asm.Opcodes.GOTO;
import static org.objectweb.asm.Opcodes.INVOKESTATIC;
import static org.objectweb.asm.Opcodes.TABLESWITCH;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.List;
import java.util.ListIterator;

import javax.imageio.ImageIO;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.RetroTweakClassWriter;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.JumpInsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.TableSwitchInsnNode;
import org.objectweb.asm.tree.VarInsnNode;

import net.minecraft.launchwrapper.IClassTransformer;
import net.minecraft.launchwrapper.Launch;

public class RetroTweakInjector implements IClassTransformer {
    /**
     *
     * THIS IS MODIFIED VERSION OF INDEVVANILLATWEAKINJECTOR
     *   ALL RIGHTS TO MOJANG
     *
     */

    @SuppressWarnings({ "deprecation", "unchecked" })
    @Override
    public byte[] transform(final String name, final String transformedName, final byte[] bytesOld) {
        try {
            if (bytesOld == null) {
                return null;
            }

            final ClassReader cr = new ClassReader(bytesOld);
            final ClassNode classNodeOld = new ClassNode();
            cr.accept(classNodeOld, ClassReader.EXPAND_FRAMES);
            final RetroTweakClassWriter cw = new RetroTweakClassWriter(0, classNodeOld.name.replace('/', '.'));
            final ClassVisitor s = new ClassVisitor(ASM4, cw) {};
            cr.accept(s, 0);
            final byte[] bytes = cw.toByteArray();
            final ClassReader classReader = new ClassReader(bytes);
            final ClassNode classNode = new ClassNode();
            classReader.accept(classNode, ClassReader.EXPAND_FRAMES);

            if (!classNode.interfaces.contains("java/lang/Runnable")) {
                return bytes;
            }

            MethodNode runMethod = null;

            for (final Object methodNode : classNode.methods) {
                final MethodNode m = (MethodNode) methodNode;

                if ("run".equals(m.name)) {
                    runMethod = m;
                    break;
                }
            }

            if (runMethod == null) {
                return bytes;
            }

            System.out.println("Probably the Minecraft class (it has run && is applet!): " + name);
            final ListIterator<AbstractInsnNode> iterator = runMethod.instructions.iterator();
            int firstSwitchJump = -1;

            while (iterator.hasNext()) {
                AbstractInsnNode instruction = iterator.next();

                if (instruction.getOpcode() == TABLESWITCH) {
                    final TableSwitchInsnNode tableSwitchInsnNode = (TableSwitchInsnNode) instruction;
                    firstSwitchJump = runMethod.instructions.indexOf((AbstractInsnNode) tableSwitchInsnNode.labels.get(0));
                } else if ((firstSwitchJump >= 0) && (runMethod.instructions.indexOf(instruction) == firstSwitchJump)) {
                    int endOfSwitch = -1;

                    while (iterator.hasNext()) {
                        instruction = iterator.next();

                        if (instruction.getOpcode() == GOTO) {
                            endOfSwitch = runMethod.instructions.indexOf(((JumpInsnNode) instruction).label);
                            break;
                        }
                    }

                    if (endOfSwitch >= 0) {
                        while ((runMethod.instructions.indexOf(instruction) != endOfSwitch) && iterator.hasNext()) {
                            instruction = iterator.next();
                        }

                        instruction = iterator.next();
                        runMethod.instructions.insertBefore(instruction, new MethodInsnNode(INVOKESTATIC, "com/zero/retrowrapper/injector/RetroTweakInjector", "inject", "()Ljava/io/File;"));
                        runMethod.instructions.insertBefore(instruction, new VarInsnNode(ASTORE, 2));
                    }
                }
            }

            final ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_MAXS | ClassWriter.COMPUTE_FRAMES);
            classNode.accept(writer);
            return writer.toByteArray();
        } catch (final Exception e) {
            return bytesOld;
        }
    }

    public static File inject() throws IllegalArgumentException, IllegalAccessException, ClassNotFoundException {
        System.out.println("Turning off ImageIO disk-caching");
        ImageIO.setUseCache(false);
        RetroTweakInjector.loadIconsOnFrames();
        System.out.println("Setting gameDir to: " + Launch.minecraftHome);
        return Launch.minecraftHome;
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    public static void loadIconsOnFrames() {
        try {
            final File e = new File(Launch.assetsDir, "icons/icon_16x16.png");
            final File bigIcon = new File(Launch.assetsDir, "icons/icon_32x32.png");
            System.out.println("Loading current icons for window from: " + e + " and " + bigIcon);
//          Display.setIcon(new ByteBuffer[]{loadIcon(e), loadIcon(bigIcon)});
            final java.awt.Frame[] frames = java.awt.Frame.getFrames();

            if (frames != null) {
                final List icons = Arrays.asList(ImageIO.read(e), ImageIO.read(bigIcon));
                final java.awt.Frame[] arg3 = frames;
                final int arg4 = frames.length;

                for (int arg5 = 0; arg5 < arg4; ++arg5) {
                    final java.awt.Frame frame = arg3[arg5];

                    try {
                        frame.setIconImages(icons);
                    } catch (final Throwable arg8) {
                        arg8.printStackTrace();
                    }
                }
            }
        } catch (final IOException arg9) {
            arg9.printStackTrace();
        }
    }

    @SuppressWarnings("unused") //I'm pretty sure this is used...
    private static ByteBuffer loadIcon(File iconFile) throws IOException {
        final BufferedImage icon = ImageIO.read(iconFile);
        final int[] rgb = icon.getRGB(0, 0, icon.getWidth(), icon.getHeight(), (int[]) null, 0, icon.getWidth());
        final ByteBuffer buffer = ByteBuffer.allocate(4 * rgb.length);
        final int[] arg3 = rgb;
        final int arg4 = rgb.length;

        for (int arg5 = 0; arg5 < arg4; ++arg5) {
            final int color = arg3[arg5];
            buffer.putInt((color << 8) | ((color >> 24) & 255));
        }

        buffer.flip();
        return buffer;
    }
}
