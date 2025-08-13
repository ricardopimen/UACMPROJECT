package com.example.portada2;

import javafx.application.HostServices;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.IOException;

public class HelloController {
    @FXML
    private VBox container1, container2, container3;
    @FXML
    private ImageView imagenUbicacion, imagenGithub, imagenFacebook;
    private HostServices hostServices;

    public void setHostServices(HostServices hostServices) {
        this.hostServices = hostServices;
    }

    @FXML
    private void initialize() {
        configurarEventosContenedor(container1, imagenUbicacion, "https://www.google.com/maps/place/UACM...");
        configurarEventosContenedor(container2, imagenGithub, "https://github.com/");
        configurarEventosContenedor(container3, imagenFacebook, "https://www.facebook.com/UACMCasaLib/...");
    }

    private void configurarEventosContenedor(VBox container, ImageView imageView, String url) {
        container.setOnMouseEntered(event -> container.setStyle("-fx-background-color: #dcdcdc; -fx-border-color: #0073e6;"));
        container.setOnMouseExited(event -> container.setStyle(""));
        imageView.setOnMouseClicked(event -> abrirEnlace(url));
    }

    @FXML
    private void mostrarAcercaDe() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Acerca de");
        alert.setHeaderText(null);
        alert.setContentText("Versión 1.2 - Aplicación de escritorio para realizar las prácticas, proyectos y tareas para el laboratorio de Física en la UACM Plantel Casa Libertad.");
        alert.showAndWait();
    }

    @FXML
    private void mostrarMensajeTareas() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Tareas");
        alert.setHeaderText(null);
        alert.setContentText("Al momento no disponible");
        alert.showAndWait();
    }

    @FXML
    private void mostrarMensajeProyectos() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Proyectos");
        alert.setHeaderText(null);
        alert.setContentText("Al momento no disponible");
        alert.showAndWait();
    }

    @FXML
    private void mostrarMensajeContactos() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Contactos");
        alert.setHeaderText(null);
        alert.setContentText("Contactar a los siguientes correos:\n\nricardo.pimentel@uacm.edu.mx\nigor.pena@uacm.edu.mx\ndavid.estrada.espinosa@uacm.edu.mx\nccyt@uacm.edu.mx");
        alert.showAndWait();
    }

    private void abrirEnlace(String url) {
        if (hostServices != null) {
            hostServices.showDocument(url);
        } else {
            System.err.println("HostServices no está disponible");
        }
    }

    @FXML private void abrirVentanaVolante() throws IOException { Parent root = FXMLLoader.load(getClass().getResource("window.fxml"));
        Stage stage = new Stage(); stage.setTitle("Volante de Inercia");
        stage.setScene(new Scene(root));
        stage.show(); }

    @FXML
    private void mostrarMensajeNoDisponible() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Funcionalidad no disponible");
        alert.setHeaderText(null);
        alert.setContentText("Aún no están disponibles sus funcionalidades.");
        alert.showAndWait();
    }

}
