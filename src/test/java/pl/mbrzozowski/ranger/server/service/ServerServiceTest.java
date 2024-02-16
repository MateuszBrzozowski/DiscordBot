package pl.mbrzozowski.ranger.server.service;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import pl.mbrzozowski.ranger.repository.main.ClientRepository;
import pl.mbrzozowski.ranger.server.service.transcription.TranscriptionService;

import java.util.ArrayList;

class ServerServiceTest {

    private ServerService serverService;
    private ClientRepository clientRepository;
    private TranscriptionService transcriptionService;

    @BeforeEach
    void beforeEach() {
        clientRepository = Mockito.mock(ClientRepository.class);
        transcriptionService = Mockito.mock(TranscriptionService.class);
        serverService = new ServerService(transcriptionService, clientRepository);
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
        clients.add(new Client(null, "123", null, null, true, true, null));
        Mockito.when(clientRepository.findByUserId("123")).thenReturn(clients);
        Assertions.assertFalse(serverService.userHasActiveReport("123"));
    }

    @Test
    void userHasActiveReport_UserHasOpenChannel_ReturnTrue() {
        ArrayList<Client> clients = new ArrayList<>();
        clients.add(new Client(null, "123", null, null, false, true, null));
        Mockito.when(clientRepository.findByUserId("123")).thenReturn(clients);
        Assertions.assertTrue(serverService.userHasActiveReport("123"));
    }

    @Test
    void userHasActiveReport_UserHasOpenAndCloseChannel_ReturnTrue() {
        ArrayList<Client> clients = new ArrayList<>();
        clients.add(new Client(null, "123", null, null, false, true, null));
        clients.add(new Client(null, "123", null, null, true, true, null));
        Mockito.when(clientRepository.findByUserId("123")).thenReturn(clients);
        Assertions.assertTrue(serverService.userHasActiveReport("123"));
    }
}