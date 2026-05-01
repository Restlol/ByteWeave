package com.byteweave.example;

import net.bytebuddy.agent.ByteBuddyAgent;
import net.bytebuddy.agent.builder.AgentBuilder;
import net.bytebuddy.agent.builder.ResettableClassFileTransformer;
import net.bytebuddy.asm.Advice;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.dynamic.DynamicType;
import net.bytebuddy.matcher.ElementMatchers;
import net.bytebuddy.utility.JavaModule;

import java.io.File;
import java.io.IOException;
import java.lang.instrument.ClassDefinition;
import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.Instrumentation;
import java.lang.instrument.UnmodifiableClassException;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.ProtectionDomain;
import java.util.Arrays;
import java.util.Date;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

import static net.bytebuddy.agent.builder.AgentBuilder.RedefinitionStrategy.REDEFINITION;
import static net.bytebuddy.agent.builder.AgentBuilder.RedefinitionStrategy.RETRANSFORMATION;
import static net.bytebuddy.implementation.bytecode.assign.Assigner.Typing.DYNAMIC;

/**
 * 重置被增强的类，redefine热加载的类不能重置
 */
public class Reset {

    public void haha(){
        System.out.println("haha");
    }

    private static class RestAdvice {

        @Advice.OnMethodEnter
        public static void enter( @Advice.Origin("#t") String clazz,
                                  @Advice.Origin("#m") String method) {

            System.out.println( "before");

        }

        @Advice.OnMethodExit(onThrowable = Throwable.class, suppress = Throwable.class)
        public static void exit(@Advice.Origin("#t") String clazz,
                                @Advice.Origin Method method,
                                @Advice.AllArguments Object[] args,
                                @Advice.Thrown Throwable throwable,
                                @Advice.Return(readOnly = false, typing = DYNAMIC) Object object) {
            System.out.println( "after");
        }
    }

    public static final Map<String, Boolean> DISABLED = new ConcurrentHashMap<>();

    public static void main(String[] args) throws ClassNotFoundException, InstantiationException, IllegalAccessException, UnmodifiableClassException {
        Instrumentation inst = ByteBuddyAgent.install();

        Reset reset = new Reset();
        reset.haha();
        System.out.println("----------------");

        AgentBuilder agentBuilder = new AgentBuilder.Default()
                .disableClassFormatChanges()
                .with(RETRANSFORMATION);

        agentBuilder = agentBuilder.type(typeDesc -> {
            String name = typeDesc.getName();
            return "com.byteweave.example.Reset".equals(name) && !DISABLED.getOrDefault(name, false);
            }).transform((builder, typeDescription, classLoader, javaModule) -> builder.visit(Advice.to(RestAdvice.class).on(ElementMatchers.isMethod())));
        ResettableClassFileTransformer resettableClassFileTransformer = agentBuilder.installOn(inst);

        reset.haha();
        System.out.println("-------reset---------");
        Class resetClazz = Arrays.stream(inst.getAllLoadedClasses()).filter(x -> x.getName().equals("com.byteweave.example.Reset") && inst.isModifiableClass(x)).findFirst().get();
        DISABLED.put("com.byteweave.example.Reset",true);
        inst.retransformClasses(resetClazz);
        reset.haha();

        //全体reset
//        resettableClassFileTransformer.reset(inst,RETRANSFORMATION);
    }

}
