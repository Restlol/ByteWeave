package com.byteweave.agent;

import net.bytebuddy.agent.builder.AgentBuilder;
import net.bytebuddy.asm.Advice;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.matcher.ElementMatcher;
import net.bytebuddy.matcher.ElementMatchers;

import java.lang.instrument.Instrumentation;
import java.lang.reflect.Method;
import java.util.Date;

import static net.bytebuddy.agent.builder.AgentBuilder.RedefinitionStrategy.RETRANSFORMATION;
import static net.bytebuddy.implementation.bytecode.assign.Assigner.Typing.DYNAMIC;
import static net.bytebuddy.matcher.ElementMatchers.nameContains;
import static net.bytebuddy.matcher.ElementMatchers.nameStartsWith;

public class EntranceTemplate {

    public static void premain(String args, Instrumentation inst) throws Throwable {
        AgentBuilder agentBuilder = new AgentBuilder.Default()
                .ignore(ignore())
                .disableClassFormatChanges()
                .with(RETRANSFORMATION);
        agentBuilder = agentBuilder.type(ElementMatchers.nameStartsWith("com.byteweave.example"))
                    .transform(((builder, typeDescription, classLoader, javaModule) -> builder.visit(Advice.to(MYAdvice.class).on(ElementMatchers.isMethod()))));
        agentBuilder.installOn(inst);
    }

    public static class MYAdvice {

        @Advice.OnMethodExit(onThrowable = Throwable.class, suppress = Throwable.class)
        public static void exit(@Advice.Origin Method method,
                                @Advice.AllArguments Object[] args,
                                @Advice.Thrown Throwable throwable,
                                @Advice.Return(readOnly = false, typing = DYNAMIC) Object object) {
            System.out.println(new Date());
        }


    }

    public static ElementMatcher.Junction<TypeDescription> ignore() {

        return nameStartsWith("net.bytebuddy.")
                .or(nameStartsWith("org.slf4j."))
                .or(nameStartsWith("org.groovy."))
                .or(nameContains("javassist"))
                .or(nameContains(".asm."))
                .or(nameContains(".reflectasm."))
                .or(nameStartsWith("sun.reflect"))
                .or(nameContains("com.byteweave"))
                .or(ElementMatchers.isSynthetic());
    }
}
