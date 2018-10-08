package helper;

import java.text.NumberFormat;

public class ResourceInfo {
    private Runtime runtime = Runtime.getRuntime();

    public String ResourceInfo() {
        StringBuilder sb = new StringBuilder();
        sb.append(this.memInfo());
        return sb.toString();
    }

    public static long totalMem() {
        return Runtime.getRuntime().totalMemory();
    }

    public static long usedMem() {
        return Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
    }

    public String memInfo() {
        NumberFormat format = NumberFormat.getInstance();
        StringBuilder sb = new StringBuilder();
        long maxMemory = runtime.maxMemory();
        long allocatedMemory = runtime.totalMemory();
        long freeMemory = runtime.freeMemory();
        sb.append("Free memory: ");
        sb.append(format.format(freeMemory / 1024));
        sb.append("\n");
        sb.append("Allocated memory: ");
        sb.append(format.format(allocatedMemory / 1024));
        sb.append("\n");
        sb.append("Max memory: ");
        sb.append(format.format(maxMemory / 1024));
        sb.append("\n");
        sb.append("Total free memory: ");
        sb.append(format.format((freeMemory + (maxMemory - allocatedMemory)) / 1024));
        sb.append("\n");
        return sb.toString();

    }
}
