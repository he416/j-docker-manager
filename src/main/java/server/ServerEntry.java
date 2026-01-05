package server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class ServerEntry {
    public static void main(String[] args) {
        int PORT = 5000;
        DockerController controller = new DockerController();

        System.out.println("=== J-DOCKER SERVER ===");
        System.out.println("Initialisation du moteur Docker...");

        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("Serveur en écoute sur le port " + PORT);

            while (true) {
                Socket clientSocket = serverSocket.accept(); // Bloque jusqu'à connexion
                System.out.println("Nouveau client connecté : " + clientSocket.getInetAddress());

                // Multithreading : On lance un thread pour gérer ce client
                Thread t = new Thread(new ClientHandler(clientSocket, controller));
                t.start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
