package server;

import com.fasterxml.jackson.databind.ObjectMapper;
import common.Request;
import common.Response;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class ClientHandler implements Runnable {
    private Socket socket;
    private DockerController controller;
    private ObjectMapper jsonMapper;

    public ClientHandler(Socket socket, DockerController controller) {
        this.socket = socket;
        this.controller = controller;
        this.jsonMapper = new ObjectMapper();
    }

    @Override
    public void run() {
        try (
                BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                PrintWriter out = new PrintWriter(socket.getOutputStream(), true)
        ) {
            String inputLine;
            while ((inputLine = in.readLine()) != null) {
                // Réception JSON -> Objet Request
                Request req = jsonMapper.readValue(inputLine, Request.class);
                Response resp = new Response();

                System.out.println("Reçu commande: " + req.getCommand());

                try {
                    switch (req.getCommand().toUpperCase()) {
                        case "LIST_IMAGES":
                            resp.setSuccess(true);
                            resp.setData(controller.listImages());
                            break;
                        case "LIST_CONTAINERS":
                            resp.setSuccess(true);
                            resp.setData(controller.listContainers());
                            break;
                        case "PULL":
                            // Cette opération peut être longue (bloquante ici)
                            String pullResult = controller.pullImage(req.getArgument());
                            resp.setSuccess(true);
                            resp.setMessage(pullResult);
                            break;
                        case "RUN":
                            // Format argument attendu: "image:tag nom_conteneur"
                            String[] parts = req.getArgument().split(" ");
                            if(parts.length < 2) throw new IllegalArgumentException("Usage: RUN image nom");
                            String result = controller.createAndStartContainer(parts[0], parts[1]);
                            resp.setSuccess(true);
                            resp.setMessage(result);
                            break;
                        case "STOP":
                            resp.setSuccess(true);
                            resp.setMessage(controller.stopContainer(req.getArgument()));
                            break;
                        case "RM":
                            resp.setSuccess(true);
                            resp.setMessage(controller.removeContainer(req.getArgument()));
                            break;
                        default:
                            resp.setSuccess(false);
                            resp.setMessage("Commande inconnue.");
                    }
                } catch (Exception e) {
                    resp.setSuccess(false);
                    resp.setMessage("Erreur Docker: " + e.getMessage());
                }

                // Envoi Réponse -> JSON
                out.println(jsonMapper.writeValueAsString(resp));
            }
        } catch (IOException e) {
            System.out.println("Client déconnecté: " + socket.getInetAddress());
        }
    }
}