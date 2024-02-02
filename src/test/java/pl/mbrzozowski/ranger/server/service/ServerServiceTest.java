package pl.mbrzozowski.ranger.server.service;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import pl.mbrzozowski.ranger.repository.main.ClientRepository;
import pl.mbrzozowski.ranger.settings.SettingsService;

import java.util.ArrayList;

class ServerServiceTest {

    private ServerService serverService;
    private ClientRepository clientRepository;

    @BeforeEach
    void beforeEach() {
        clientRepository = Mockito.mock(ClientRepository.class);
        SettingsService settingsService = Mockito.mock(SettingsService.class);
        serverService = new ServerService(clientRepository, settingsService);
    }

    @Test
    void userHasActiveReport_NoUser_ReturnFalse() {
        ArrayList<Client> clients = new ArrayList<>();
        Mockito.when(clientRepository.findByUserId("123")).thenReturn(clients);
        Assertions.assertFalse(serverService.userHasActiveReport("123"));
    }

    @Test
    void userHasActiveReport_UserHasCloseChannel_ReturnFalse() {
        ArrayList<Client> clients = new ArrayList<>();
        clients.add(new Client(null, "123", null, null, true, null));
        Mockito.when(clientRepository.findByUserId("123")).thenReturn(clients);
        Assertions.assertFalse(serverService.userHasActiveReport("123"));
    }

    @Test
    void userHasActiveReport_UserHasOpenChannel_ReturnTrue() {
        ArrayList<Client> clients = new ArrayList<>();
        clients.add(new Client(null, "123", null, null, false, null));
        Mockito.when(clientRepository.findByUserId("123")).thenReturn(clients);
        Assertions.assertTrue(serverService.userHasActiveReport("123"));
    }

    @Test
    void userHasActiveReport_UserHasOpenAndCloseChannel_ReturnTrue() {
        ArrayList<Client> clients = new ArrayList<>();
        clients.add(new Client(null, "123", null, null, false, null));
        clients.add(new Client(null, "123", null, null, true, null));
        Mockito.when(clientRepository.findByUserId("123")).thenReturn(clients);
        Assertions.assertTrue(serverService.userHasActiveReport("123"));
    }
}