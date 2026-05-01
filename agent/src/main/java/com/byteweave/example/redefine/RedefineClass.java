package com.byteweave.example.redefine;

import net.bytebuddy.agent.ByteBuddyAgent;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.lang.instrument.ClassDefinition;
import java.lang.instrument.Instrumentation;
import java.lang.instrument.UnmodifiableClassException;
import java.util.Arrays;

/**
 * 热加载class
 */
public class RedefineClass {

    public static void main(String[] args) throws ClassNotFoundException, InstantiationException, IllegalAccessException, IOException, UnmodifiableClassException {

        Print print = new Print();
        print.say();
        Instrumentation inst = ByteBuddyAgent.install();
        byte[] newBytecode = readClassFile("/Users/xxx/java-code/ByteWeave/agent/src/main/redefine/Print.class");
        Class printClazz = Arrays.stream(inst.getAllLoadedClasses()).filter(x -> x.getName().equals("com.byteweave.example.redefine.Print") && inst.isModifiableClass(x)).findFirst().get();
        ClassDefinition definition = new ClassDefinition(printClazz, newBytecode);
        inst.redefineClasses(definition);
        System.out.println("重定义成功");

        print.say();
    }

    private static byte[] readClassFile(String path) throws IOException {
        File file = new File(path);
        byte[] buffer = new byte[(int) file.length()];
        try (FileInputStream fis = new FileInputStream(file)) {
            fis.read(buffer);
        }
        return buffer;
    }
}
