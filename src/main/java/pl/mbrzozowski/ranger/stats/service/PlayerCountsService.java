package pl.mbrzozowski.ranger.stats.service;

import lombok.RequiredArgsConstructor;
import pl.mbrzozowski.ranger.repository.stats.PlayerCountsRepository;

@RequiredArgsConstructor
public class PlayerCountsService {

    private final PlayerCountsRepository playerCountsRepository;
}
