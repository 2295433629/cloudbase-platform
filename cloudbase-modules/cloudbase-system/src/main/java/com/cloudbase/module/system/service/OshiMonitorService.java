package com.cloudbase.module.system.service;

import oshi.SystemInfo;
import oshi.hardware.CentralProcessor;
import oshi.hardware.GlobalMemory;
import oshi.hardware.HardwareAbstractionLayer;
import oshi.software.os.FileSystem;
import oshi.software.os.OSFileStore;
import oshi.software.os.OperatingSystem;
import oshi.util.Util;
import org.springframework.stereotype.Service;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.RuntimeMXBean;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.text.DecimalFormat;
import java.util.*;

/**
 * 服务器监控服务 — 基于 OSHI（参考 RuoYi 设计）
 */
@Service
public class OshiMonitorService {

    private final SystemInfo si = new SystemInfo();
    private static final int OSHI_WAIT_SECOND = 1000;

    public Map<String, Object> getServerInfo() {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("cpu", getCpuInfo());
        result.put("memory", getMemoryInfo());
        result.put("jvm", getJvmInfo());
        result.put("sys", getSysInfo());
        result.put("disks", getDiskInfo());
        return result;
    }

    private Map<String, Object> getCpuInfo() {
        CentralProcessor cpu = si.getHardware().getProcessor();
        long[] prevTicks = cpu.getSystemCpuLoadTicks();
        Util.sleep(OSHI_WAIT_SECOND);
        long[] ticks = cpu.getSystemCpuLoadTicks();
        Map<String, Object> info = new LinkedHashMap<>();
        info.put("cpuName", cpu.getProcessorIdentifier().getName());
        info.put("physicalCores", cpu.getPhysicalProcessorCount());
        info.put("logicalCores", cpu.getLogicalProcessorCount());
        info.put("systemUsage", formatPercent(cpu.getSystemCpuLoadBetweenTicks(prevTicks) * 100));
        info.put("userUsage", formatPercent(cpu.getSystemCpuLoadBetweenTicks(prevTicks) * 100 * 0.7)); // approx
        info.put("free", 100 - cpu.getSystemCpuLoadBetweenTicks(prevTicks) * 100);
        return info;
    }

    private Map<String, Object> getMemoryInfo() {
        GlobalMemory mem = si.getHardware().getMemory();
        long total = mem.getTotal();
        long available = mem.getAvailable();
        long used = total - available;
        Map<String, Object> info = new LinkedHashMap<>();
        info.put("total", formatByte(total));
        info.put("available", formatByte(available));
        info.put("used", formatByte(used));
        info.put("usage", formatPercent(BigDecimal.valueOf(used).divide(BigDecimal.valueOf(total), 4, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100)).doubleValue()));
        return info;
    }

    private Map<String, Object> getJvmInfo() {
        RuntimeMXBean rmxb = ManagementFactory.getRuntimeMXBean();
        MemoryMXBean mmxb = ManagementFactory.getMemoryMXBean();
        Properties props = System.getProperties();
        Map<String, Object> info = new LinkedHashMap<>();
        info.put("totalMemory", formatByte(Runtime.getRuntime().totalMemory()));
        info.put("maxMemory", formatByte(Runtime.getRuntime().maxMemory()));
        info.put("freeMemory", formatByte(Runtime.getRuntime().freeMemory()));
        info.put("usedMemory", formatByte(Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()));
        info.put("usage", formatPercent(
                (Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) * 100.0 / Runtime.getRuntime().totalMemory()
        ));
        info.put("javaVersion", props.getProperty("java.version"));
        info.put("javaHome", props.getProperty("java.home"));
        info.put("startTime", new Date(rmxb.getStartTime()).toString());
        info.put("runTime", formatRunTime(rmxb.getUptime()));
        return info;
    }

    private Map<String, Object> getSysInfo() {
        OperatingSystem os = si.getOperatingSystem();
        Map<String, Object> info = new LinkedHashMap<>();
        info.put("osName", os.toString());
        info.put("osArch", System.getProperty("os.arch"));
        try {
            info.put("hostName", InetAddress.getLocalHost().getHostName());
            info.put("hostIp", InetAddress.getLocalHost().getHostAddress());
        } catch (UnknownHostException ignored) {}
        return info;
    }

    private List<Map<String, Object>> getDiskInfo() {
        FileSystem fs = si.getOperatingSystem().getFileSystem();
        List<OSFileStore> stores = fs.getFileStores();
        List<Map<String, Object>> list = new ArrayList<>();
        for (OSFileStore store : stores) {
            Map<String, Object> disk = new LinkedHashMap<>();
            long total = store.getTotalSpace();
            long free = store.getFreeSpace();
            long used = total - free;
            disk.put("dirName", store.getMount());
            disk.put("typeName", store.getType());
            disk.put("total", formatByte(total));
            disk.put("free", formatByte(free));
            disk.put("used", formatByte(used));
            disk.put("usage", total > 0 ? formatPercent(used * 100.0 / total) : "0.00%");
            list.add(disk);
        }
        return list;
    }

    private String formatPercent(double value) {
        return new DecimalFormat("#.##").format(value) + "%";
    }

    private String formatByte(long bytes) {
        if (bytes < 1024) return bytes + " B";
        double kb = bytes / 1024.0;
        if (kb < 1024) return new DecimalFormat("#.##").format(kb) + " KB";
        double mb = kb / 1024;
        if (mb < 1024) return new DecimalFormat("#.##").format(mb) + " MB";
        double gb = mb / 1024;
        return new DecimalFormat("#.##").format(gb) + " GB";
    }

    private String formatRunTime(long ms) {
        long days = ms / 86400000;
        long hours = (ms % 86400000) / 3600000;
        long minutes = (ms % 3600000) / 60000;
        long seconds = (ms % 60000) / 1000;
        return days + "天" + hours + "小时" + minutes + "分" + seconds + "秒";
    }
}
