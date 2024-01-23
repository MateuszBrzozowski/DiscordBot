package pl.mbrzozowski.ranger.giveaway;

import org.springframework.stereotype.Service;
import pl.mbrzozowski.ranger.repository.main.GiveawayRepository;
import pl.mbrzozowski.ranger.repository.main.GiveawayUsersRepository;
import pl.mbrzozowski.ranger.repository.main.PrizeRepository;

@Service
public class GiveawayService {

    private final GiveawayRepository giveawayRepository;
    private final GiveawayUsersRepository giveawayUsersRepository;
    private final PrizeRepository prizeRepository;

    public GiveawayService(GiveawayRepository giveawayRepository,
                           GiveawayUsersRepository giveawayUsersRepository,
                           PrizeRepository prizeRepository) {
        this.giveawayRepository = giveawayRepository;
        this.giveawayUsersRepository = giveawayUsersRepository;
        this.prizeRepository = prizeRepository;
    }
}
