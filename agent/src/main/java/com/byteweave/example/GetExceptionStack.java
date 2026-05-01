package com.byteweave.example;

import net.bytebuddy.agent.ByteBuddyAgent;
import net.bytebuddy.agent.builder.AgentBuilder;
import net.bytebuddy.asm.Advice;
import net.bytebuddy.matcher.ElementMatchers;
import java.lang.instrument.Instrumentation;

import static net.bytebuddy.agent.builder.AgentBuilder.RedefinitionStrategy.RETRANSFORMATION;

/**
 * 获取方法抛出异常信息
 */
public class GetExceptionStack {

    public static void a(){
        System.out.println("ok");
    }

    public static void b(){
        System.out.println(1/0);
    }


    private static class GetExceptionStackAdvice {

        @Advice.OnMethodExit(onThrowable = Throwable.class, suppress = Throwable.class)
        public static void exit(@Advice.Thrown Throwable throwable) {
           if(throwable!=null){
               throwable.printStackTrace();
           }
        }
    }


    public static void main(String[] args) {
        Instrumentation inst = ByteBuddyAgent.install();
        AgentBuilder agentBuilder = new AgentBuilder.Default()
                .disableClassFormatChanges()
                .with(RETRANSFORMATION);
        agentBuilder = agentBuilder.type(ElementMatchers.nameStartsWith("com.byteweave.example"))
                .transform(((builder, typeDescription, classLoader, javaModule) -> builder.visit(Advice.to(GetExceptionStackAdvice.class).on(ElementMatchers.isMethod()))));
        agentBuilder.installOn(inst);

        a();
        b();

    }
}
