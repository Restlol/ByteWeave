package com.byteweave.example;

import net.bytebuddy.agent.ByteBuddyAgent;
import net.bytebuddy.agent.builder.AgentBuilder;
import net.bytebuddy.asm.Advice;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.dynamic.DynamicType;
import net.bytebuddy.matcher.ElementMatchers;
import net.bytebuddy.utility.JavaModule;

import java.io.File;
import java.io.IOException;
import java.lang.instrument.Instrumentation;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;

import static net.bytebuddy.agent.builder.AgentBuilder.RedefinitionStrategy.RETRANSFORMATION;

/**
 * 打印方法耗时
 */
public class PrintCostTime {

    public  void a() throws InterruptedException {
        TimeUnit.SECONDS.sleep(1);
    }

    public  void b() throws InterruptedException {
        TimeUnit.SECONDS.sleep(2);
    }

    public  void c() throws InterruptedException{
        b();
        a();
    }


    private static class CostTimeAdvice {

        @Advice.OnMethodEnter
        public static void enter(@Advice.Local("start") long start) {
            start = System.nanoTime();
        }


        @Advice.OnMethodExit(onThrowable = Throwable.class)
        public static void exit(@Advice.Origin("#t#m") String method,
                                @Advice.Local("start") long start) {
            long costNs = System.nanoTime() - start;
            double ms = costNs / 1_000_000.0;

            System.out.println(method + " cost=" + ms + " ms");
        }
    }


    public static void main(String[] args) throws InterruptedException {
        Instrumentation inst = ByteBuddyAgent.install();
        AgentBuilder agentBuilder = new AgentBuilder.Default()
                .disableClassFormatChanges()
                .with(RETRANSFORMATION);
        agentBuilder = agentBuilder.type(ElementMatchers.named("com.byteweave.example.PrintCostTime"))
                .transform(((builder, typeDescription, classLoader, javaModule) -> builder.visit(Advice.to(CostTimeAdvice.class).on(ElementMatchers.isMethod()))));
        agentBuilder.installOn(inst);
        Arrays.stream(inst.getAllLoadedClasses()).forEach(System.out::println);

        new PrintCostTime().c();

    }
}
