package com.gym.gymsystem.health;

import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

import java.io.File;

@Component
public class DiskSpaceHealthIndicator implements HealthIndicator {
    private static final long THRESHOLD = 1024 * 1024 * 500;

    @Override
    public Health health() {
        File diskRoot = new File("/");
        long freeSpace = diskRoot.getFreeSpace();

        if (freeSpace >= THRESHOLD) {
            return Health.up().withDetail("Disk Space", "Sufficient").withDetail("Free Space", freeSpace).build();
        } else {
            return Health.down().withDetail("Disk Space", "Insufficient").withDetail("Free Space", freeSpace).build();
        }
    }
}
