package pl.mbrzozowski.ranger.server.seed.call;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import pl.mbrzozowski.ranger.repository.main.MessageRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
public class MessageService {

    private final MessageRepository messageRepository;

    public void save(Message message) {
        messageRepository.save(message);
    }

    public List<Message> findByLevel(Levels level) {
        return messageRepository.findByLevel(level);
    }

    public void deleteByLevel(Levels level) {
        messageRepository.deleteByLevel(level);
    }

    public void deleteById(long id) {
        messageRepository.deleteById(id);
    }
}
