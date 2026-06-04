package com.sitmmio.analytics.speed.distributed;

public class PartialSpeedResult {

    private final String routeId;
    private final String month;
    private double speedSum;
    private long count;

    public PartialSpeedResult(String routeId, String month) {
        this.routeId = routeId;
        this.month = month;
    }

    public void add(double speed) {
        this.speedSum += speed;
        this.count++;
    }

    public void merge(PartialSpeedResult other) {
        this.speedSum += other.speedSum;
        this.count += other.count;
    }

    public String getRouteId() {
        return routeId;
    }

    public String getMonth() {
        return month;
    }

    public double getSpeedSum() {
        return speedSum;
    }

    public long getCount() {
        return count;
    }

    public double getAverageSpeed() {
        return count == 0 ? 0.0 : speedSum / count;
    }

    public String toPartialCsvLine() {
        return routeId + "," + month + "," +
                String.format(java.util.Locale.US, "%.5f", speedSum) + "," +
                count;
    }

    public String toFinalCsvLine() {
        return routeId + "," + month + "," +
                String.format(java.util.Locale.US, "%.2f", getAverageSpeed()) + "," +
                count;
    }
}