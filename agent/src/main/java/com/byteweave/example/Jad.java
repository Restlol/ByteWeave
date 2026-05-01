package com.byteweave.example;

import net.bytebuddy.agent.ByteBuddyAgent;
import net.bytebuddy.agent.builder.AgentBuilder;
import net.bytebuddy.asm.Advice;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.dynamic.DynamicType;
import net.bytebuddy.matcher.ElementMatchers;
import net.bytebuddy.utility.JavaModule;
import org.benf.cfr.reader.Main;
import org.benf.cfr.reader.api.CfrDriver;

import java.io.File;
import java.io.FileOutputStream;
import java.lang.annotation.Annotation;
import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.Instrumentation;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.security.ProtectionDomain;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicReference;

import static net.bytebuddy.agent.builder.AgentBuilder.RedefinitionStrategy.RETRANSFORMATION;
import static net.bytebuddy.implementation.bytecode.assign.Assigner.Typing.DYNAMIC;

/**
 * arthas jad的简单实现
 */
public class Jad {

    public static void a(){
    }

    private static class JADAdvice {
        @Advice.OnMethodEnter
        public static void enter( @Advice.AllArguments Object[] args,
                                  @Advice.Origin("#m") String method) {
            String a = "1";
        }

        @Advice.OnMethodExit(onThrowable = Throwable.class, suppress = Throwable.class)
        public static void exit(@Advice.Origin("#t") String clazz,
                                @Advice.Origin Method method,
                                @Advice.AllArguments Object[] args,
                                @Advice.Thrown Throwable throwable,
                                @Advice.Return(readOnly = false, typing = DYNAMIC) Object object) {
            int b = 2;
        }
    }

    private static class JadTrigger {
        private static final ThreadLocal<byte[]> CLASS_BYTES_HOLDER = new ThreadLocal<>();

        public static void jad(Class<?> target,Instrumentation inst) throws Exception {

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
        AgentBuilder agentBuilder = new AgentBuilder.Default()
                .disableClassFormatChanges()
                .with(RETRANSFORMATION);
        agentBuilder = agentBuilder.type(ElementMatchers.named("com.byteweave.example.Jad"))
                .transform(((builder, typeDescription, classLoader, javaModule) -> builder.visit(Advice.to(JADAdvice.class).on(ElementMatchers.isMethod()))));
        agentBuilder.installOn(inst);

        JadTrigger.jad(Jad.class, inst);
    }
}
