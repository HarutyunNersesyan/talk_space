package com.talk_space.modules.discovery.internal.matching;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;
import jakarta.validation.constraints.*;
@Data
@Component
@Validated
@ConfigurationProperties(prefix = "app.matching")
public class MatchingProperties {
    @Min(1) @Max(10000) private int candidateLimit = 500;
    @Min(1) private int historyDays = 7;
    @DecimalMin("0.0") @DecimalMax("1.0") private double exactWeight = 0.7;
    @Min(1) @Max(50) private int diversityPool = 10;
    @DecimalMin("0.0") @DecimalMax("0.25") private double diversityWindow = 0.08;
    @Min(1) private int skipHours = 24;
    @Min(1) private int pendingHours = 24;
    @Min(1) private int rejectedDays = 7;
    @Min(1) private int cancelledHours = 48;
    @DecimalMin("0.0") @DecimalMax("1.0") private double primaryWeight = 0.8;
    @DecimalMin("0.0") @DecimalMax("1.0") private double parentWeight = 0.35;
}
