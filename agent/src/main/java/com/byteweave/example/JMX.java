package com.byteweave.example;

import java.lang.management.*;
import java.util.List;

/**
 *  监控jvm信息,从ManagementFactory获取内存，gc，线程，os相关信息
 */
public class JMX {

    public static void main(String[] args) {

        MemoryMXBean memoryMXBean = ManagementFactory.getMemoryMXBean();
        List<GarbageCollectorMXBean> gcBeans = ManagementFactory.getGarbageCollectorMXBeans();
        ThreadMXBean threadMXBean = ManagementFactory.getThreadMXBean();
        OperatingSystemMXBean osBean = ManagementFactory.getOperatingSystemMXBean();
    }
}
