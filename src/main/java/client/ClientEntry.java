package client;

import com.fasterxml.jackson.databind.ObjectMapper;
import common.Request;
import common.Response;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;

public class ClientEntry {
    public static void main(String[] args) {
        String HOST = "localhost"; // Mettre l'IP de la VM si distant
        int PORT = 5000;
        ObjectMapper mapper = new ObjectMapper();

        System.out.println("=== J-DOCKER CLIENT CONSOLE ===");
        System.out.println("Commandes disponibles: LIST_IMAGES, LIST_CONTAINERS, PULL <image>, RUN <image> <name>, STOP <id>, RM <id>, EXIT");

        try (Socket socket = new Socket(HOST, PORT);
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
             BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
             Scanner scanner = new Scanner(System.in)) {

            while (true) {
                System.out.print("\nJDocker> ");
                String line = scanner.nextLine();
                if (line.trim().isEmpty()) continue;

                String[] parts = line.split(" ", 2);
                String cmd = parts[0].toUpperCase();
                String arg = parts.length > 1 ? parts[1] : "";

                if ("EXIT".equals(cmd)) {
                    System.out.println("Fermeture...");
                    break;
                }

                // 1. Création de la requête
                Request req = new Request(cmd, arg);
                String jsonReq = mapper.writeValueAsString(req);

                // 2. Envoi au serveur
                out.println(jsonReq);

                // 3. Réception de la réponse
                String jsonResp = in.readLine();
                if (jsonResp == null) {
                    System.err.println("Erreur: Serveur déconnecté.");
                    break;
                }

                Response response = mapper.readValue(jsonResp, Response.class);

                // 4. Affichage
                if (response.isSuccess()) {
                    System.out.println("[OK] " + (response.getMessage() != null ? response.getMessage() : ""));
                    if (response.getData() != null) {
                        // Affiche les données (liste d'images ou conteneurs) en joli JSON
                        System.out.println(mapper.writerWithDefaultPrettyPrinter().writeValueAsString(response.getData()));
                    }
                } else {
                    System.err.println("[ERREUR] " + response.getMessage());
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Impossible de se connecter au serveur.");
        }
    }
}