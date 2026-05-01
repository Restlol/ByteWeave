package com.byteweave.example;

import net.bytebuddy.agent.ByteBuddyAgent;
import net.bytebuddy.agent.builder.AgentBuilder;
import net.bytebuddy.asm.Advice;
import net.bytebuddy.matcher.ElementMatchers;
import java.lang.instrument.Instrumentation;
import java.lang.reflect.Method;
import static net.bytebuddy.agent.builder.AgentBuilder.RedefinitionStrategy.RETRANSFORMATION;
import static net.bytebuddy.implementation.bytecode.assign.Assigner.Typing.DYNAMIC;

/**
 * 打印方法调用的链路
 */
public class MethodCallRoot {
    public void a(){}
    public void b(){}
    public void c(){
        b();
        a();
    }

    private static class MethodStackAdvice {

        @Advice.OnMethodEnter
        public static void enter( @Advice.Origin("#t") String clazz,
                                  @Advice.Origin("#m") String method) {

            System.out.println( " enter: "+ clazz + "#" + method);

        }

        @Advice.OnMethodExit(onThrowable = Throwable.class, suppress = Throwable.class)
        public static void exit(@Advice.Origin("#t") String clazz,
                                @Advice.Origin Method method,
                                @Advice.AllArguments Object[] args,
                                @Advice.Thrown Throwable throwable,
                                @Advice.Return(readOnly = false, typing = DYNAMIC) Object object) {
            System.out.println( " exist: "+ clazz + "#" + method.getName());
        }
    }

    public static void main(String[] args) {
        Instrumentation inst = ByteBuddyAgent.install();
        AgentBuilder agentBuilder = new AgentBuilder.Default()
                .disableClassFormatChanges()
                .with(RETRANSFORMATION);
        agentBuilder = agentBuilder.type(ElementMatchers.nameStartsWith("com.byteweave.example"))
                .transform(((builder, typeDescription, classLoader, javaModule) -> builder.visit(Advice.to(MethodStackAdvice.class).on(ElementMatchers.isMethod()))));
        agentBuilder.installOn(inst);
        new MethodCallRoot().c();

    }
}
