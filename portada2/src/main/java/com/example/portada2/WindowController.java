package com.example.portada2;
import javafx.application.Platform;
import javafx.fxml.FXML;

import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Label;
import com.fazecast.jSerialComm.SerialPort;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TableView;  // Para usar setVisible() en TableView
import javafx.scene.control.TableColumn;
import java.util.ArrayList;
import java.util.List;
import javafx.beans.property.SimpleDoubleProperty;

public class WindowController {

    @FXML
    private Label welcomeText;
    private SerialPort comPort; //  <-- Aquí se especifica el tipo de variable
    @FXML
    private LineChart<Number, Number> lineChart; // <-- Declaración de lineChart
    @FXML
    private NumberAxis xAxis; // <-- Declaración de xAxis
    @FXML
    private Button runButton; // Botón "Run"
    @FXML
    private Button stopButton; // Botón "Stop"
    @FXML
    private TextField tiempoLimiteTextField;
    private double tiempoLimite = 5.0;

    private int contador = 0; //
    private boolean running = false; // <-- Declaración e inicialización de running
    @FXML
    private Label coordenadasLabel; // Label para mostrar las coordenadas
    private double inicioSeleccionX;
    private Rectangle zoomRect;
    @FXML
    private ComboBox<String> puertoCOMComboBox;
    @FXML
    private ComboBox<Integer> baudRateComboBox;
    @FXML
    private TextField valorXTextField;
    @FXML
    private TextField valorYTextField;
    @FXML
    private NumberAxis yAxis; // Declarar la variable yAxis
    @FXML
    private ImageView logoImageView;
    @FXML
    private Button resetZoomButton; // Botón para reiniciar el zoom
    @FXML
    private Button tablaButton;
    @FXML
    private TableView<Punto> tablaPuntos;
    @FXML
    private TableColumn<Punto, Double> velocidadAngularColumn;
    @FXML
    private TableColumn<Punto, Double> tiempoColumn;
    private List<Punto> puntosSeleccionados = new ArrayList<>();
    private boolean seleccionandoPuntos = false;
    private boolean zoomActivado = true;

    @FXML
    private void onTablaButtonClick() {
        seleccionandoPuntos = !seleccionandoPuntos;
        tablaPuntos.setVisible(seleccionandoPuntos);
        // Desactivar/activar zoom según el estado de seleccionandoPuntos
        zoomActivado = !seleccionandoPuntos;

        // Limpiar la tabla y la lista de puntos
        puntosSeleccionados.clear();
        tablaPuntos.getItems().clear();
    }
    @FXML
    private void onMouseClicked(MouseEvent event) {
        if (seleccionandoPuntos && puntosSeleccionados.size() < 8) {
            // Obtener el nodo del gráfico
            LineChart<Number, Number> chart = (LineChart<Number, Number>) event.getSource();

            // Calcular el margen izquierdo del gráfico
            double margenIzquierdo = chart.getXAxis().getLayoutX();

            // Ajustar la posición del mouse para considerar el margen
            double posicionMouseX = event.getX() - margenIzquierdo;

            // Obtener coordenadas del punto
            double x = xAxis.getValueForDisplay(posicionMouseX).doubleValue();
            double y = lineChart.getData().get(0).getNode().sceneToLocal(event.getSceneX(), event.getSceneY()).getY();
            y = ((NumberAxis) lineChart.getYAxis()).getValueForDisplay(y).doubleValue();

            // Agregar el punto a la lista (y como velocidad angular, x como tiempo)
            puntosSeleccionados.add(new Punto(y, x));

            // Actualizar la tabla
            tablaPuntos.getItems().setAll(puntosSeleccionados);

        }
    }
    // Clase para representar un punto en la tabla
    public static class Punto {
        private final SimpleDoubleProperty velocidadAngular = new SimpleDoubleProperty();
        private final SimpleDoubleProperty tiempo = new SimpleDoubleProperty();

        public Punto(double velocidadAngular, double tiempo) {
            this.velocidadAngular.set(velocidadAngular);
            this.tiempo.set(tiempo);
        }

        // Getters para las propiedades
        public SimpleDoubleProperty velocidadAngularProperty() {
            return velocidadAngular;
        }

        public SimpleDoubleProperty tiempoProperty() {
            return tiempo;
        }

        // Getters para los valores
        public double getVelocidadAngular() {
            return velocidadAngular.get();
        }

        public double getTiempo() {
            return tiempo.get();
        }
    }

    @FXML
    private void onResetZoomButtonClick() {
        xAxis.setAutoRanging(true); // Reactivar el ajuste automático del eje X
        yAxis.setAutoRanging(true); // Reactivar el ajuste automático del eje Y

        // Forzar la actualización de la gráfica
        lineChart.requestLayout();
    }

    @FXML
    private void onAplicarOpcionesButtonClick() {
        // Obtener los valores de los controles
        String puertoCOM = puertoCOMComboBox.getValue();
        int baudRate = baudRateComboBox.getValue();
        double valorX = Double.parseDouble(valorXTextField.getText());
        double valorY = Double.parseDouble(valorYTextField.getText());

        // Aplicar los cambios de configuración
        if (comPort != null && comPort.isOpen()) {
            comPort.closePort(); // Cerrar el puerto actual
        }

        // Abrir el nuevo puerto COM
        comPort = SerialPort.getCommPort(puertoCOM);
        if (comPort.openPort()) {
            System.out.println("Puerto serial abierto: " + comPort.getSystemPortName());
            comPort.setComPortParameters(baudRate, 8, 1, SerialPort.NO_PARITY);
            comPort.setComPortTimeouts(SerialPort.TIMEOUT_READ_SEMI_BLOCKING, 100, 0);

            // Reiniciar la gráfica si estaba en ejecución
            if (running) {
                detenerGrafica();
                onRunButtonClick(); // Reiniciar la gráfica con la nueva configuración
            }
        } else {
            System.err.println("Error al abrir el puerto serial: " + comPort.getSystemPortName());
            // Puedes mostrar un mensaje de error al usuario
        }
        // Obtener el nuevo tiempo límite del TextField
        try {
            tiempoLimite = Double.parseDouble(tiempoLimiteTextField.getText());
        } catch (NumberFormatException e) {
            System.err.println("Error al convertir el tiempo límite a número: " + e.getMessage());
            // Puedes mostrar un mensaje de error al usuario o usar un valor predeterminado
        }

        // Actualizar los límites de los ejes (centrar en el valor ingresado)
        xAxis.setLowerBound(valorX );
        xAxis.setUpperBound(valorX );
        yAxis.setLowerBound(-valorY );
        yAxis.setUpperBound( valorY );
    }


    @FXML
    private void onMouseMoved(MouseEvent event) {
        if (!running) {
            // Obtener el nodo del gráfico
            LineChart<Number, Number> chart = (LineChart<Number, Number>) event.getSource();

            // Calcular el margen izquierdo del gráfico
            double margenIzquierdo = chart.getXAxis().getLayoutX();

            // Ajustar la posición del mouse para considerar el margen
            double posicionMouseX = event.getX() - margenIzquierdo;

            // Calcular la coordenada X usando la posición ajustada
            double x = xAxis.getValueForDisplay(posicionMouseX).doubleValue();

            // Calcular la coordenada Y
            double y = lineChart.getData().get(0).getNode().sceneToLocal(event.getSceneX(), event.getSceneY()).getY();
            y = ((NumberAxis) lineChart.getYAxis()).getValueForDisplay(y).doubleValue();

            // Mostrar las coordenadas en el Label
            coordenadasLabel.setText(String.format("X: %.2f, Y: %.2f", x, y));
            coordenadasLabel.setVisible(true);

            // Actualizar la posición del Label
            coordenadasLabel.setLayoutX(event.getSceneX() + 10);
            coordenadasLabel.setLayoutY(event.getSceneY() + 10);
        } else {
            coordenadasLabel.setVisible(false);
        }
    }
    @FXML
    private void onRunButtonClick() {
        running = true;
        runButton.setDisable(true);
        stopButton.setDisable(false);

        contador = 0;
        lineChart.getData().get(0).getData().clear();

        // Iniciar el hilo para leer datos de Arduino
        Thread thread = new Thread(this::leerDatosArduino);
        thread.setDaemon(true);
        thread.start();

        // Iniciar el hilo para controlar el tiempo límite
        Thread tiempoLimiteThread = new Thread(this::controlarTiempoLimite);
        tiempoLimiteThread.setDaemon(true);
        tiempoLimiteThread.start();
    }

    @FXML
    private void onStopButtonClick() {
        detenerGrafica();
    }
    private void detenerGrafica() {
        running = false;
        runButton.setDisable(false);
        stopButton.setDisable(true);
    }

    private void controlarTiempoLimite() {
        try {
            Thread.sleep((long) (tiempoLimite * 1000)); // Esperar el tiempo límite
            if (running) { // Verificar si la gráfica aún se está ejecutando
                Platform.runLater(this::detenerGrafica); // Detener la gráfica en el hilo de la UI
            }
        } catch (InterruptedException e) {
            System.err.println("Error en el hilo de tiempo límite: " + e.getMessage());
        }
    }
    @FXML
    private void onMousePressed(MouseEvent event) {
        if (!running && zoomActivado) {
            inicioSeleccionX = event.getX();

            // Crear un rectángulo para mostrar la selección
            zoomRect = new Rectangle(); // <-- Mover esta línea antes de configurar el color y la opacidad
            zoomRect.setX(inicioSeleccionX);
            zoomRect.setY(0);
            zoomRect.setFill(Color.BLACK); // Color azul claro
            zoomRect.setOpacity(0); // Opacidad semi-transparente
            lineChart.getChildrenUnmodifiable().add(zoomRect);
        }
    }

    @FXML
    private void onMouseDragged(MouseEvent event) {
        if (!running && zoomRect != null && zoomActivado) {
            double x = event.getX();
            zoomRect.setWidth(Math.abs(x - inicioSeleccionX));
        }
    }

    @FXML
    private void onMouseReleased(MouseEvent event) {
        if (!running && zoomRect != null && zoomActivado) {
            double finSeleccionX = event.getX();
            lineChart.getChildrenUnmodifiable().remove(zoomRect);

            // Calcular los valores del eje X para el zoom
            double xMin = xAxis.getValueForDisplay(Math.min(inicioSeleccionX, finSeleccionX)).doubleValue();
            double xMax = xAxis.getValueForDisplay(Math.max(inicioSeleccionX, finSeleccionX)).doubleValue();

            // Aplicar el zoom al eje X
            xAxis.setAutoRanging(false);
            xAxis.setLowerBound(xMin);
            xAxis.setUpperBound(xMax);

            zoomRect = null;
        }
    }

    @FXML
    public void initialize() {

        System.out.println(System.getProperty("javafx.runtime.version"));
        // Configurar la comunicación serial
        comPort = SerialPort.getCommPort("COM4");
        if (comPort.openPort()) {
            System.out.println("Puerto serial abierto: " + comPort.getSystemPortName());
            comPort.setComPortParameters(115200, 8, 1, SerialPort.NO_PARITY);
            comPort.setComPortTimeouts(SerialPort.TIMEOUT_READ_SEMI_BLOCKING, 100, 0);

            // No iniciar el hilo aquí, se iniciará en onRunButtonClick()
            // thread = new Thread(this::leerDatosArduino);
            // thread.start();
            Thread thread = new Thread(this::leerDatosArduino);
            thread.setDaemon(true); // Configurar como demonio para que no bloquee la salida de la aplicación
            thread.start();
        } else {
            System.err.println("Error al abrir el puerto serial: " + comPort.getSystemPortName());
        }
        // Cargar la imagen del logo
        Image logo = new Image(getClass().getResourceAsStream("/com/example/portada2/logo.png"));

        logoImageView.setImage(logo);
        // Modificar el tamaño de la imagen
        logoImageView.setFitWidth(1000); // Ajustar el ancho a 200 píxeles
        logoImageView.setPreserveRatio(true); // Mantener la proporción

        XYChart.Series<Number, Number> series = new XYChart.Series<>();
        series.setName("Velocidad Angular");
        lineChart.getData().add(series);
        // Inicializar los controles de la pestaña de opciones
        puertoCOMComboBox.getItems().addAll("COM1", "COM2", "COM3", "COM4"); // Agregar opciones al ComboBox
        puertoCOMComboBox.setValue("COM4"); // Valor predeterminado

        baudRateComboBox.getItems().addAll(9600, 115200, 128000); // Agregar opciones al ComboBox
        baudRateComboBox.setValue(115200); // Valor predeterminado

        valorXTextField.setText("0"); // Valor predeterminado
        valorYTextField.setText("0"); // Valor predeterminado


        // Configurar las columnas de la tabla
        velocidadAngularColumn.setCellValueFactory(cellData ->
                cellData.getValue().velocidadAngularProperty().asObject());
        tiempoColumn.setCellValueFactory(cellData ->
                cellData.getValue().tiempoProperty().asObject());

        // Desactivar el ajuste automático de los ejes
        xAxis.setAutoRanging(false);
        yAxis.setAutoRanging(false);
    }
    private void leerDatosArduino() {
        while (comPort.isOpen() && running) {
            try {
                if (comPort.bytesAvailable() > 0) {
                    byte[] readBuffer = new byte[comPort.bytesAvailable()];
                    int numRead = comPort.readBytes(readBuffer, readBuffer.length);

                    String data = new String(readBuffer, 0, numRead);

                    try {
                        // Dividir la cadena por saltos de línea
                        String[] valores = data.split("\n");

                        // Procesar cada valor en la cadena
                        for (String valorStr : valores) {
                            double velocidadAngular = Double.parseDouble(valorStr.trim());
                            Platform.runLater(() -> actualizarGrafica(velocidadAngular));
                        }
                    } catch (NumberFormatException e) {
                        System.err.println("Error al convertir datos a número: " + e.getMessage());
                        System.err.println("Datos recibidos: " + data);
                    }
                }
                Thread.sleep(100);

            } catch (Exception e) {
                System.err.println("Error al leer datos de Arduino: " + e.getMessage());
            }
        }
    }
    private void actualizarGrafica(double velocidadAngular) {
        XYChart.Series<Number, Number> series = lineChart.getData().get(0);
        series.getData().add(new XYChart.Data<>(contador, velocidadAngular));
        contador++;

        xAxis.setLowerBound(Math.max(0, contador - 300));
        xAxis.setUpperBound(contador);

        if (series.getData().size() > 1000) {
            series.getData().remove(0);
        }
        /*Number tiempo = null;
        series.getData().add(new XYChart.Data<>(tiempo, velocidadAngular));*/
        if (contador >= tiempoLimite * 100) { // <-- Corregir la condición
            running = false;
            runButton.setDisable(false);
            stopButton.setDisable(true);
        }
    }






























}
