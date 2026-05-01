package com.byteweave.example;

import net.bytebuddy.agent.ByteBuddyAgent;
import net.bytebuddy.agent.builder.AgentBuilder;
import net.bytebuddy.asm.Advice;
import net.bytebuddy.matcher.ElementMatchers;

import java.io.File;
import java.io.FileOutputStream;
import java.lang.annotation.Annotation;
import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.Instrumentation;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

import static net.bytebuddy.agent.builder.AgentBuilder.RedefinitionStrategy.RETRANSFORMATION;
import static net.bytebuddy.implementation.bytecode.assign.Assigner.Typing.DYNAMIC;

/**
 * arthas sc的简单实现
 */
public class Sc {

    public static void a(){
    }


    private static class ScTrigger {
        private static final ThreadLocal<byte[]> CLASS_BYTES_HOLDER = new ThreadLocal<>();

        public static void sc(Class<?> target,Instrumentation inst) throws Exception {

            File dumpedFile = null;

            ClassFileTransformer transformer = (loader, className, classBeingRedefined, protectionDomain, classfileBuffer) -> {

                if (classBeingRedefined == target) {
                    CLASS_BYTES_HOLDER.set(classfileBuffer);
                }
                return null;
            };

            try {
                inst.addTransformer(transformer, true);
                inst.retransformClasses(target);
                byte[] bytes = CLASS_BYTES_HOLDER.get();

                if (bytes != null) {
                    String internalName = target.getName().replace('.', '/') + ".class";
                    dumpedFile = new File(internalName);

                    if (dumpedFile.getParentFile() != null) {
                        dumpedFile.getParentFile().mkdirs();
                    }
                    try (FileOutputStream fos =
                                 new FileOutputStream(dumpedFile)) {
                        fos.write(bytes);
                    }
                    System.out.println("jad dump success: " + dumpedFile.getAbsolutePath());
                }
            }finally {
                inst.removeTransformer(transformer);
                CLASS_BYTES_HOLDER.remove();
                //在此记得删除写入的文件！
            }
        }
    }

    public static void main(String[] args) throws Exception {
        Instrumentation inst = ByteBuddyAgent.install();

        ScTrigger.sc(Sc.class,inst);

        Class[] allLoadedClasses = inst.getAllLoadedClasses();
        for (Class clazz:allLoadedClasses) {
            if(clazz != null && ("com.byteweave.example.Sc").equals(clazz.getCanonicalName())){
                System.out.println("class-info        " + clazz.getName());

                try {
                    String codeSource = clazz.getProtectionDomain()
                            .getCodeSource()
                            .getLocation()
                            .toString();
                    System.out.println("code-source       " + codeSource);
                } catch (Exception e) {
                    System.out.println("code-source       null");
                }

                System.out.println("name              " + clazz.getName());
                System.out.println("simple-name       " + clazz.getSimpleName());

                System.out.println("isInterface       " + clazz.isInterface());
                System.out.println("isAnnotation      " + clazz.isAnnotation());
                System.out.println("isEnum            " + clazz.isEnum());
                System.out.println("isAnonymousClass  " + clazz.isAnonymousClass());
                System.out.println("isArray           " + clazz.isArray());
                System.out.println("isLocalClass      " + clazz.isLocalClass());
                System.out.println("isMemberClass     " + clazz.isMemberClass());
                System.out.println("isPrimitive       " + clazz.isPrimitive());
                System.out.println("isSynthetic       " + clazz.isSynthetic());

                System.out.println("modifier          " + Modifier.toString(clazz.getModifiers()));

                System.out.println("annotation");
                Annotation[] annotations = clazz.getAnnotations();
                for (Annotation ann : annotations) {
                    System.out.println("  " + ann.annotationType().getName());
                }

                System.out.println("interfaces");
                for (Class<?> i : clazz.getInterfaces()) {
                    System.out.println("  " + i.getName());
                }

                System.out.println("super-class       +-"
                        + (clazz.getSuperclass() != null
                        ? clazz.getSuperclass().getName()
                        : "null"));

                printClassLoader(clazz.getClassLoader(), "class-loader      ");

                if (clazz.getClassLoader() != null) {
                    System.out.println("classLoaderHash   "
                            + Integer.toHexString(clazz.getClassLoader().hashCode()));
                }
            }
        }
    }

    private static void printClassLoader(ClassLoader cl, String prefix) {

        if (cl == null) {
            System.out.println(prefix + "BootstrapClassLoader");
            return;
        }

        System.out.println(prefix + cl.getClass().getName()
                + "@" + Integer.toHexString(cl.hashCode()));

        ClassLoader parent = cl.getParent();

        if (parent != null) {
            printClassLoader(parent, "                    +-");
        }
    }
}
