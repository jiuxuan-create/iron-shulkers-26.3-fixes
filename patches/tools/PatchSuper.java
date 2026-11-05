import org.objectweb.asm.*;
import org.objectweb.asm.tree.*;

import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.zip.*;

/**
 * Rewrites AbstractIronShulkerBoxBlock so that it extends vanilla ShulkerBoxBlock
 * instead of BaseEntityBlock, and forwards its constructor to the vanilla
 * (DyeColor, Properties) constructor.
 *
 * Needed because third party code (QuickShulker's ShulkerUtils.isShulkerItem)
 * decides "is this a shulker box" with "instanceof ShulkerBoxBlock".
 *
 * Usage: java -cp asm-9.11.jar;asm-tree-9.11.jar PatchSuper.java path/to/mod.jar
 */
public final class PatchSuper {

    private static final String CLS = "com/astryxion/ironshulkerbox/common/block/AbstractIronShulkerBoxBlock.class";
    private static final String BASE = "net/minecraft/world/level/block/BaseEntityBlock";
    private static final String SHULKER = "net/minecraft/world/level/block/ShulkerBoxBlock";
    private static final String CTOR_DESC =
            "(Lnet/minecraft/world/item/DyeColor;Lnet/minecraft/world/level/block/state/BlockBehaviour$Properties;)V";

    public static void main(String[] args) throws Exception {
        Path jar = Paths.get(args[0]);

        Map<String, byte[]> entries = new LinkedHashMap<>();
        try (ZipFile zip = new ZipFile(jar.toFile())) {
            Enumeration<? extends ZipEntry> it = zip.entries();
            while (it.hasMoreElements()) {
                ZipEntry e = it.nextElement();
                byte[] data;
                try (InputStream in = zip.getInputStream(e)) {
                    data = readAll(in);
                }
                entries.put(e.getName(), data);
            }
        }
        byte[] original = entries.get(CLS);
        if (original == null) {
            throw new IllegalStateException("class not found: " + CLS);
        }

        ClassReader reader = new ClassReader(original);
        ClassWriter writer = new ClassWriter(reader, ClassWriter.COMPUTE_MAXS);
        ClassVisitor visitor = new ClassVisitor(Opcodes.ASM9, writer) {
            @Override
            public void visit(int version, int access, String name, String signature,
                              String superName, String[] interfaces) {
                super.visit(version, access, name, signature, SHULKER, interfaces);
            }

            @Override
            public MethodVisitor visitMethod(int access, String name, String descriptor,
                                             String signature, String[] exceptions) {
                MethodVisitor target = super.visitMethod(access, name, descriptor, signature, exceptions);
                if (!"<init>".equals(name)) {
                    return target;
                }
                MethodNode node = new MethodNode(Opcodes.ASM9, access, name, descriptor, signature, exceptions);
                return new MethodVisitor(Opcodes.ASM9, node) {
                    @Override
                    public void visitEnd() {
                        InsnList instructions = node.instructions;
                        for (AbstractInsnNode insn : instructions.toArray()) {
                            if (!(insn instanceof MethodInsnNode)) {
                                continue;
                            }
                            MethodInsnNode call = (MethodInsnNode) insn;
                            if (call.getOpcode() != Opcodes.INVOKESPECIAL
                                    || !BASE.equals(call.owner) || !"<init>".equals(call.name)) {
                                continue;
                            }
                            call.owner = SHULKER;
                            call.desc = CTOR_DESC;
                            AbstractInsnNode previous = call.getPrevious();
                            if (previous instanceof VarInsnNode && previous.getOpcode() == Opcodes.ALOAD
                                    && ((VarInsnNode) previous).var == 1) {
                                instructions.insertBefore(previous, new VarInsnNode(Opcodes.ALOAD, 2));
                            } else {
                                throw new IllegalStateException("unexpected constructor shape before super call");
                            }
                        }
                        node.accept(target);
                    }
                };
            }
        };
        reader.accept(visitor, 0);
        byte[] patched = writer.toByteArray();

        ClassReader check = new ClassReader(patched);
        if (!SHULKER.equals(check.getSuperName())) {
            throw new IllegalStateException("patch did not take effect, super = " + check.getSuperName());
        }
        entries.put(CLS, patched);

        Path tmp = jar.resolveSibling(jar.getFileName() + ".tmp");
        try (ZipOutputStream out = new ZipOutputStream(Files.newOutputStream(tmp))) {
            for (Map.Entry<String, byte[]> entry : entries.entrySet()) {
                ZipEntry ze = new ZipEntry(entry.getKey());
                out.putNextEntry(ze);
                out.write(entry.getValue());
                out.closeEntry();
            }
        }
        Files.move(tmp, jar, StandardCopyOption.REPLACE_EXISTING);
        System.out.println("PATCHED super=" + check.getSuperName() + " size=" + patched.length);
    }

    private static byte[] readAll(InputStream in) throws IOException {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        byte[] chunk = new byte[8192];
        int read;
        while ((read = in.read(chunk)) > 0) {
            buffer.write(chunk, 0, read);
        }
        return buffer.toByteArray();
    }
}
