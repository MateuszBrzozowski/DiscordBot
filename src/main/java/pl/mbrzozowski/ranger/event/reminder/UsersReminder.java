package pl.mbrzozowski.ranger.event.reminder;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Entity;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
public class UsersReminder {
    private Long id;
    private String userId;
}
