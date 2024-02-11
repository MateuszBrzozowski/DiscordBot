package pl.mbrzozowski.ranger.server.service;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.jetbrains.annotations.Nullable;

import javax.persistence.*;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity(name = "server_service_client")
public class Client {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String userId;
    private String channelId;
    @Column(length = 1000)
    private String userName;
    @Column(columnDefinition = "bit(1) default FALSE")
    private Boolean isClose = false;
    @Column(columnDefinition = "bit(1) default TRUE")
    private Boolean autoClose = true;
    @Nullable
    private LocalDateTime closeTimestamp;
}
