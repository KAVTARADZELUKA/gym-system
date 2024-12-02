package com.gym.gymsystem.feign;

import com.gym.gymsystem.dto.workload.WorkloadRequest;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

@FeignClient(name = "WORKLOAD-SERVICE", path = "/api/workloads")
public interface WorkloadInterface {
    @PostMapping
    HttpStatus updateWorkload(@RequestBody WorkloadRequest request, @RequestHeader("X-Transaction-Id") String transactionId);
}
