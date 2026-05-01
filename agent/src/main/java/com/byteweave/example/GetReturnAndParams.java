package com.byteweave.example;

import net.bytebuddy.agent.ByteBuddyAgent;
import net.bytebuddy.agent.builder.AgentBuilder;
import net.bytebuddy.asm.Advice;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.dynamic.DynamicType;
import net.bytebuddy.matcher.ElementMatchers;
import net.bytebuddy.utility.JavaModule;

import java.lang.instrument.Instrumentation;
import java.lang.reflect.Method;

import static net.bytebuddy.agent.builder.AgentBuilder.RedefinitionStrategy.RETRANSFORMATION;
import static net.bytebuddy.implementation.bytecode.assign.Assigner.Typing.DYNAMIC;

/**
 * 获取方法参数与返回
 */
public class GetReturnAndParams {

    public static void a(){
        System.out.println("no return");
    }

    public static String b(int b,String b1){
        return "b";
    }

    public static int c(int c){
        return c;
    }

    private static class GetReturnAndParamsAdvice {
        @Advice.OnMethodEnter
        public static void enter( @Advice.AllArguments Object[] args,
                                  @Advice.Origin("#m") String method) {
            System.out.println("args = " + java.util.Arrays.toString(args));
        }

        @Advice.OnMethodExit(onThrowable = Throwable.class, suppress = Throwable.class)
        public static void exit(@Advice.Origin("#t") String clazz,
                                @Advice.Origin Method method,
                                @Advice.AllArguments Object[] args,
                                @Advice.Thrown Throwable throwable,
                                @Advice.Return(readOnly = false, typing = DYNAMIC) Object object) {
            System.out.println("return = " + object);
        }
    }


    public static void main(String[] args) {
        Instrumentation inst = ByteBuddyAgent.install();
        AgentBuilder agentBuilder = new AgentBuilder.Default()
                .disableClassFormatChanges()
                .with(RETRANSFORMATION);
        agentBuilder = agentBuilder.type(ElementMatchers.named("com.byteweave.example.GetReturnAndParams"))
                .transform(((builder, typeDescription, classLoader, javaModule) -> builder.visit(Advice.to(GetReturnAndParamsAdvice.class).on(ElementMatchers.isMethod()))));
        agentBuilder.installOn(inst);

        a();
        b(1,"1");
        c(2);

    }
}
