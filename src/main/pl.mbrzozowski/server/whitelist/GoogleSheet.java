package server.whitelist;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.SheetsScopes;
import com.google.api.services.sheets.v4.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class GoogleSheet {

    protected static final Logger logger = LoggerFactory.getLogger(GoogleSheet.class.getName());
    private static Sheets sheetsService;
    private static String APPLICATION_NAME = "rangers-polska-whitelist";
    private final List<Player> players;
    private List<List<Object>> playersDataToSheet = new ArrayList<>();

    public GoogleSheet(List<Player> players) {
        this.players = players;
    }

    private static Credential authorize() throws IOException, GeneralSecurityException {
        InputStream in = GoogleSheet.class.getResourceAsStream("/credentials.json");
        GoogleClientSecrets clientSecrets = GoogleClientSecrets.load(
                GsonFactory.getDefaultInstance(), new InputStreamReader(in)
        );

        List<String> scopes = Arrays.asList(SheetsScopes.SPREADSHEETS);

        GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(
                GoogleNetHttpTransport.newTrustedTransport(), GsonFactory.getDefaultInstance(),
                clientSecrets, scopes)
                .setDataStoreFactory(new FileDataStoreFactory(new java.io.File("tokens")))
                .setAccessType("offline")
                .build();
        Credential credential = new AuthorizationCodeInstalledApp(
                flow, new LocalServerReceiver())
                .authorize("user");
        return credential;
    }

    private static Sheets getSheetsService() throws IOException, GeneralSecurityException {
        Credential credential = authorize();
        return new Sheets.Builder(GoogleNetHttpTransport.newTrustedTransport(),
                GsonFactory.getDefaultInstance(), credential)
                .setApplicationName(APPLICATION_NAME)
                .build();
    }

    private void deleteAllRows() {
        try {
            DeleteDimensionRequest deleteDimensionRequest = new DeleteDimensionRequest()
                    .setRange(
                            new DimensionRange()
                                    .setSheetId(0)
                                    .setDimension("ROWS")
                                    .setStartIndex(1)
                    );

            List<Request> requests = new ArrayList<>();
            requests.add(new Request().setDeleteDimension(deleteDimensionRequest));
            BatchUpdateSpreadsheetRequest body = new BatchUpdateSpreadsheetRequest().setRequests(requests);
            sheetsService.spreadsheets().batchUpdate(Auth.SPREADSHEET_ID, body).execute();
        } catch (IOException e) {
            logger.info("Brak wierszy do usunięcia");
        }

    }

    private void addPlayersToSheet() {
        try {
            ValueRange appendBody = new ValueRange()
                    .setValues(playersDataToSheet);
            AppendValuesResponse appendValuesResponse = sheetsService.spreadsheets().values()
                    .append(Auth.SPREADSHEET_ID, "Arkusz1", appendBody)
                    .setValueInputOption("USER_ENTERED")
                    .setInsertDataOption("INSERT_ROWS")
                    .setIncludeValuesInResponse(true)
                    .execute();
        } catch (IOException e) {
            logger.error("Nie można wpisać użytkowników");
        }

    }

    private void prepareValues() {
        for (int i = 0; i < players.size(); i++) {
            List<Object> values = new ArrayList<>();
            values.add("ActivePlayer");
            values.add(players.get(i).getName());
            values.add("Whitelist");
            values.add(players.get(i).getSteamID());
            playersDataToSheet.add(values);
        }
    }

    public void updatePlayers() {
        try {
            sheetsService = getSheetsService();
            prepareValues();
            deleteAllRows();
            addPlayersToSheet();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (GeneralSecurityException e) {
            e.printStackTrace();
        }
    }
}
