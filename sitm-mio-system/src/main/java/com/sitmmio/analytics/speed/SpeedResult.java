package com.sitmmio.analytics.speed;

public class SpeedResult {

    private final String routeId;
    private final String month;
    private double speedSum;
    private long count;

    public SpeedResult(String routeId, String month) {
        this.routeId = routeId;
        this.month = month;
    }

    public synchronized void add(double speed) {
        this.speedSum += speed;
        this.count++;
    }

    public synchronized void merge(SpeedResult other) {
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

    public String toCsvLine() {
    return routeId + "," + month + "," +
            String.format(java.util.Locale.US, "%.2f", getAverageSpeed()) +
            "," + count;
}
}