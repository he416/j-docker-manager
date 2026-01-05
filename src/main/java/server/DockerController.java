package server;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.command.PullImageResultCallback;
import com.github.dockerjava.api.model.Container;
import com.github.dockerjava.api.model.Image;
import com.github.dockerjava.core.DefaultDockerClientConfig;
import com.github.dockerjava.core.DockerClientImpl;
import com.github.dockerjava.httpclient5.ApacheDockerHttpClient;
import com.github.dockerjava.transport.DockerHttpClient;

import java.util.List;
import java.util.concurrent.TimeUnit;

public class DockerController {
    private DockerClient dockerClient;

    public DockerController() {
        // Configuration par défaut
        // Sur Windows, assurez-vous que Docker Desktop expose le daemon sur TCP 2375
        DefaultDockerClientConfig config = DefaultDockerClientConfig.createDefaultConfigBuilder().build();

        DockerHttpClient httpClient = new ApacheDockerHttpClient.Builder()
                .dockerHost(config.getDockerHost())
                .sslConfig(config.getSSLConfig())
                .build();

        this.dockerClient = DockerClientImpl.getInstance(config, httpClient);
    }

    // 1. Lister les images
    public List<Image> listImages() {
        return dockerClient.listImagesCmd().exec();
    }

    // 2. Lister les conteneurs (tous, même les arrêtés)
    public List<Container> listContainers() {
        return dockerClient.listContainersCmd().withShowAll(true).exec();
    }

    // 3. Télécharger une image
    public String pullImage(String imageName) {
        try {
            dockerClient.pullImageCmd(imageName)
                    .exec(new PullImageResultCallback())
                    .awaitCompletion(60, TimeUnit.SECONDS);
            return "Image " + imageName + " téléchargée avec succès.";
        } catch (InterruptedException e) {
            return "Erreur: Téléchargement interrompu ou Timeout.";
        }
    }

    // 4. Créer et Démarrer un conteneur
    public String createAndStartContainer(String imageName, String containerName) {
        // Création
        String id = dockerClient.createContainerCmd(imageName)
                .withName(containerName)
                .exec()
                .getId();
        // Démarrage
        dockerClient.startContainerCmd(id).exec();
        return "Conteneur créé et démarré. ID: " + id;
    }

    // 5. Arrêter un conteneur
    public String stopContainer(String containerId) {
        dockerClient.stopContainerCmd(containerId).exec();
        return "Conteneur " + containerId + " arrêté.";
    }

    // 6. Supprimer un conteneur
    public String removeContainer(String containerId) {
        dockerClient.removeContainerCmd(containerId).exec();
        return "Conteneur " + containerId + " supprimé.";
    }
}