package com.project.soc.service;

import com.project.soc.dto.network.NetworkTrafficDto;
import com.project.soc.dto.network.SystemHealthDto;
import org.springframework.stereotype.Service;

import java.lang.management.ManagementFactory;
import java.lang.management.OperatingSystemMXBean;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@Service
public class NetworkService {

    private static final DateTimeFormatter HH_MM = DateTimeFormatter.ofPattern("HH:mm");

    public List<NetworkTrafficDto> getTraffic() {
        Random rng = new Random(Instant.now().getEpochSecond() / 60);
        Instant now = Instant.now();
        List<NetworkTrafficDto> result = new ArrayList<>();
        for (int i = 29; i >= 0; i--) {
            Instant t = now.minusSeconds(i * 60L);
            String label = t.atZone(ZoneOffset.UTC).format(HH_MM);
            result.add(NetworkTrafficDto.builder()
                    .time(label)
                    .inboundMbps(50 + rng.nextDouble() * 200)
                    .outboundMbps(30 + rng.nextDouble() * 150)
                    .build());
        }
        return result;
    }

    public SystemHealthDto getHealth() {
        OperatingSystemMXBean os = ManagementFactory.getOperatingSystemMXBean();
        double cpuLoad = os.getSystemLoadAverage();
        if (cpuLoad < 0) cpuLoad = 15 + new Random().nextDouble() * 30;

        Runtime rt = Runtime.getRuntime();
        double totalMem = rt.maxMemory();
        double usedMem = totalMem - rt.freeMemory();
        double ramPercent = (usedMem / totalMem) * 100;

        double diskPercent = 35 + new Random().nextDouble() * 25;
        int sessions = 2 + new Random().nextInt(18);

        String status = cpuLoad > 80 || ramPercent > 90 ? "WARNING" : "HEALTHY";

        return SystemHealthDto.builder()
                .cpuPercent(Math.round(cpuLoad * 10.0) / 10.0)
                .ramPercent(Math.round(ramPercent * 10.0) / 10.0)
                .diskPercent(Math.round(diskPercent * 10.0) / 10.0)
                .activeSessions(sessions)
                .status(status)
                .build();
    }
}
