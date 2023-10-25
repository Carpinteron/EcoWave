package com.raven.form;

import com.raven.chart.ModelChart;
import comunicacionserial.ArduinoExcepcion;
import comunicacionserial.ComunicacionSerial_Arduino;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.net.URL;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import jssc.SerialPortEvent;
import jssc.SerialPortEventListener;
import jssc.SerialPortException;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

public class Form1 extends javax.swing.JPanel {
 private Form1 sonidoManager; // Campo para almacenar la referencia

    // Variables
    static JFreeChart chart;
    static ChartPanel panel;
    static JFreeChart chart2;
    static ChartPanel panel2;
    int cont = 0;
    Thread hilo;
    boolean On = true;
    public static boolean statusHilo = false;
    boolean Graficar = true;
    //Se crea una instancia de la librería ComunicacionSerial_Arduino
    private static ComunicacionSerial_Arduino ino = new ComunicacionSerial_Arduino();
    public SerialPortEventListener listener = new SerialPortEventListener() {
        @Override
        public synchronized void serialEvent(SerialPortEvent spe) {
            try {
                if (ino.isMessageAvailable()) {
                    String d = ino.printMessage();
                    System.out.println(d);
                    if (On == true) {
                        consola.setText("\n" + d);//si solo quiero ver uno
                        //consola.setText(consola.getText()+"\n"+d); si quiero ver todas
                    }

                }
            } catch (SerialPortException ex) {
                Logger.getLogger(Form1.class.getName()).log(Level.SEVERE, null, ex);
            } catch (ArduinoExcepcion ex) {
                Logger.getLogger(Form1.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

    };
    
    //SUBRUTINAS PARA APLICAR SONIDO
    public static Clip clip;
    public static boolean sali=false;

    public void reproducirSonido(String cadena) {
        
        try {
            clip = AudioSystem.getClip();
            URL url = getClass().getResource(cadena);
            AudioInputStream audioIn = AudioSystem.getAudioInputStream(url);
            clip.open(audioIn);
            clip.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    
    }

    

    //METODOS
    private void ReiniciarHilo() {
        statusHilo = true;
        hilo = new Thread() {

            @Override
            public void run() {
                while (statusHilo) {

                    try {
                        crearGraficas();
                        Thread.sleep(1000);
                    } catch (InterruptedException ex) {
                        Logger.getLogger(Form1.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            }

        };
    }
    XYSeries dataset = new XYSeries("Registros Distancias EcoWave");
// Nuevos parámetros para la segunda función seno
    double amplitud = 5.0;  // Cambia el valor según tus necesidades
    double frecuenciaAngular = 0.05;  // Cambia el valor según tus necesidades
    double anguloDeFase = 0.0;
    XYSeries dataset2 = new XYSeries("Registros Onda Senoidal ");

    private void crearGraficas() {
        //https://www.tutorialspoint.com/jfreechart/jfreechart_xy_chart.htm
        String[] lecturas = consola.getText().split("\n");
        if (lecturas.length > 0) {
            if (!lecturas[lecturas.length - 1].trim().equals("")) {
                if (Double.parseDouble(lecturas[lecturas.length - 1]) < 50 && Double.parseDouble(lecturas[lecturas.length - 1]) > -10) {
                    double DISTANCIAOK = Double.parseDouble(lecturas[lecturas.length - 1]);
                    dataset.add(cont, DISTANCIAOK);
                    double distancia = amplitud * Math.sin(frecuenciaAngular * cont + anguloDeFase);
                    System.out.println("distncia seno: "+distancia);
                    dataset2.add(cont, distancia);
                    cont++;
                    //porcentaje 1
                    double minimo = 18;
                    double maximo = 20;
                    double DISTANCIAOK2 = DISTANCIAOK;
                    // Asegurémonos de que el valor esté dentro del rango
                    if (DISTANCIAOK2 < minimo) {
                        DISTANCIAOK2 = minimo;
                    } else if (DISTANCIAOK2 > maximo) {
                        DISTANCIAOK2 = maximo;
                    }

                    // Calcula el porcentaje
                    double porcentaje = ((DISTANCIAOK2 - minimo) / (maximo - minimo)) * 100.0;
                    int valorInt = (int) Math.round(porcentaje);
                    gaugeChart1.setValueWithAnimation(valorInt);
                    if (valorInt>90 && valorInt<=100 && sali==false){
                        reproducirSonido("/Sonido/buzzer.wav");
                        consola.setForeground(Color.RED);//AQUI DEBEN IR LAS 8 LEDS
                    }else{
                        consola.setForeground(new Color (80,116,253));
                    }

                }
            }

        }
        XYSeriesCollection collection = new XYSeriesCollection();
        collection.addSeries(dataset);

        chart = ChartFactory.createXYLineChart(
                "X vs Tiempo", // chart title
                "Tiempo (s)", // X axis label
                "Distancia (cm)", // Y axis label
                collection, // data
                PlotOrientation.VERTICAL,
                true, // include legend
                true, // tooltips
                false // urls
        );

        panel = new ChartPanel(chart, true, true, true, false, true);
        panel.setPreferredSize(new java.awt.Dimension(433, 971));
        XYPlot plot = chart.getXYPlot();
        XYLineAndShapeRenderer renderer = new XYLineAndShapeRenderer();
        renderer.setSeriesPaint(0, Color.BLUE);
        renderer.setSeriesStroke(0, new BasicStroke(2.0f));
        plot.setRenderer(renderer);
        jPanel1.setLayout(null);
        panel.setBounds(0, 0, jPanel1.getWidth(), jPanel1.getHeight());
        jPanel1.removeAll();
        jPanel1.add(panel);
        jPanel1.repaint();
        panelShadow4.setPreferredSize(new java.awt.Dimension(1011, 473));
        panelShadow4.add(jPanel1);
        panelShadow4.revalidate();
        panelShadow4.repaint();

        //PARA SENO
        // C
        XYSeriesCollection collection2 = new XYSeriesCollection();
        collection2.addSeries(dataset2);

        chart2 = ChartFactory.createXYLineChart(
                "Función senoidal ",
                "Tiempo (s)",
                "Distancia (cm)",
                collection2,
                PlotOrientation.VERTICAL,
                true,
                true,
                false
        );
        // Obtén el rango del eje Y del gráfico
        ValueAxis rangeAxis = chart2.getXYPlot().getRangeAxis();

// Configura el rango del eje Y de 0 a 10
        rangeAxis.setRange(-6.0, 6.0);
        panel2 = new ChartPanel(chart2, true, true, true, false, true);
        panel2.setPreferredSize(new java.awt.Dimension(433, 971));
        gsen.setLayout(null);
        panel2.setBounds(0, 0, gsen.getWidth(), gsen.getHeight());
        gsen.removeAll();
        gsen.add(panel2);
        gsen.repaint();
        panelShadow5.setPreferredSize(new java.awt.Dimension(1011, 473));
        panelShadow5.add(gsen);
        panelShadow5.revalidate();
        panelShadow5.repaint();

    }

    private void DatosinSensor() {
        // Crear un temporizador para generar datos cada segundo
        Timer timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                // Generar un número aleatorio en el rango de 20 a 22
                double randomValue = 18 + new Random().nextDouble() * 2;

                // Formatear el número como "20.22" usando el formato con punto decimal
                DecimalFormatSymbols decimalFormatSymbols = new DecimalFormatSymbols(Locale.US);
                DecimalFormat df = new DecimalFormat("0.00", decimalFormatSymbols);
                String sensorData = df.format(randomValue);
                System.out.println(sensorData);
                consola.setText("\n" + sensorData);
            }
        }, 0, 1000); // Generar datos cada segundo (1000 milisegundos)
    }

    /**
     * OPCIONAL BOTON GRAFICAR: btnGraficar.setEnabled(false);
     * btnPausa.setEnabled(true); btnReset.setEnabled(true); On = true;
     * ReiniciarHilo(); hilo.start(); BOTON PAUSAR:
     * btnReanudar.setEnabled(true); btnPausa.setEnabled(false);
     * consola.setEnabled(false); On = false; hilo.stop(); statusHilo = false;
     * BOTON RESET: btnGraficar.setEnabled(true); btnPausa.setEnabled(false);
     * btnReset.setEnabled(false); consola.setEnabled(false); On = false;
     * hilo.stop(); statusHilo = false; BOTON REANUDAR: On = true;
     * ReiniciarHilo(); hilo.start(); btnReanudar.setEnabled(false);
     * btnPausa.setEnabled(true);
     */
    public Form1() {
        
        initComponents();
        setOpaque(false);


        init();
        //Activamos el llamado para recibir datos del arduino
        
         try { ino.arduinoRXTX("COM6", 9600, listener); } catch
         (ArduinoExcepcion ex) {
         Logger.getLogger(Form1.class.getName()).log(Level.SEVERE, null, ex);
          }
         
         

    }

    private void init() {
        //DatosinSensor();
        On = true;
        ReiniciarHilo();
        hilo.start();

    }

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        panelShadow1 = new com.raven.swing.PanelShadow();
        gaugeChart1 = new com.raven.chart.GaugeChart();
        jLabel9 = new javax.swing.JLabel();
        jLabel1 = new javax.swing.JLabel();
        consola = new javax.swing.JLabel();
        jLabel7 = new javax.swing.JLabel();
        panelShadow2 = new com.raven.swing.PanelShadow();
        jLabel8 = new javax.swing.JLabel();
        jLabel13 = new javax.swing.JLabel();
        jLabel14 = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();
        panelShadow4 = new com.raven.swing.PanelShadow();
        jPanel1 = new javax.swing.JPanel();
        jLabel22 = new javax.swing.JLabel();
        jLabel23 = new javax.swing.JLabel();
        panelShadow5 = new com.raven.swing.PanelShadow();
        gsen = new javax.swing.JPanel();
        jLabel24 = new javax.swing.JLabel();
        panelShadow7 = new com.raven.swing.PanelShadow();
        jLabel12 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        labelñe = new javax.swing.JLabel();
        jLabel25 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        panelShadow8 = new com.raven.swing.PanelShadow();
        jLabel26 = new javax.swing.JLabel();
        jLabel27 = new javax.swing.JLabel();
        jLabel28 = new javax.swing.JLabel();
        jLabel10 = new javax.swing.JLabel();
        panelShadow10 = new com.raven.swing.PanelShadow();
        jLabel30 = new javax.swing.JLabel();
        jLabel36 = new javax.swing.JLabel();
        jLabel37 = new javax.swing.JLabel();
        jLabel6 = new javax.swing.JLabel();
        panelShadow9 = new com.raven.swing.PanelShadow();
        jLabel29 = new javax.swing.JLabel();
        jLabel34 = new javax.swing.JLabel();
        jLabel35 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();

        panelShadow1.setBorder(javax.swing.BorderFactory.createEmptyBorder(20, 20, 20, 20));

        gaugeChart1.setColor1(new java.awt.Color(255, 255, 255));
        gaugeChart1.setColor2(new java.awt.Color(69, 71, 252));
        gaugeChart1.setFont(new java.awt.Font("sansserif", 0, 14)); // NOI18N

        jLabel9.setFont(new java.awt.Font("sansserif", 1, 10)); // NOI18N
        jLabel9.setForeground(new java.awt.Color(133, 133, 133));
        jLabel9.setText("% con respecto a la distancia máxima");
        jLabel9.setBorder(javax.swing.BorderFactory.createEmptyBorder(3, 3, 3, 3));

        jLabel1.setFont(new java.awt.Font("sansserif", 1, 18)); // NOI18N
        jLabel1.setForeground(new java.awt.Color(97, 97, 97));
        jLabel1.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel1.setText("Distancia");

        consola.setFont(new java.awt.Font("sansserif", 1, 36)); // NOI18N
        consola.setForeground(new java.awt.Color(80, 116, 253));
        consola.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        consola.setBorder(javax.swing.BorderFactory.createEmptyBorder(3, 3, 3, 3));

        jLabel7.setFont(new java.awt.Font("sansserif", 1, 14)); // NOI18N
        jLabel7.setForeground(new java.awt.Color(133, 133, 133));
        jLabel7.setText("                        Cm ");
        jLabel7.setBorder(javax.swing.BorderFactory.createEmptyBorder(3, 3, 3, 3));

        javax.swing.GroupLayout panelShadow1Layout = new javax.swing.GroupLayout(panelShadow1);
        panelShadow1.setLayout(panelShadow1Layout);
        panelShadow1Layout.setHorizontalGroup(
            panelShadow1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelShadow1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(panelShadow1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jLabel1, javax.swing.GroupLayout.DEFAULT_SIZE, 136, Short.MAX_VALUE)
                    .addComponent(gaugeChart1, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(panelShadow1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel7, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(panelShadow1Layout.createSequentialGroup()
                        .addGroup(panelShadow1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(consola)
                            .addComponent(jLabel9))
                        .addGap(0, 0, Short.MAX_VALUE)))
                .addContainerGap())
        );
        panelShadow1Layout.setVerticalGroup(
            panelShadow1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, panelShadow1Layout.createSequentialGroup()
                .addComponent(jLabel1)
                .addGap(7, 7, 7)
                .addGroup(panelShadow1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(panelShadow1Layout.createSequentialGroup()
                        .addGroup(panelShadow1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel7)
                            .addComponent(consola, javax.swing.GroupLayout.PREFERRED_SIZE, 41, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(18, 18, 18)
                        .addComponent(jLabel9)
                        .addGap(6, 6, 6))
                    .addComponent(gaugeChart1, javax.swing.GroupLayout.PREFERRED_SIZE, 111, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap())
        );

        panelShadow2.setBorder(javax.swing.BorderFactory.createEmptyBorder(20, 20, 20, 20));

        jLabel8.setFont(new java.awt.Font("sansserif", 1, 18)); // NOI18N
        jLabel8.setForeground(new java.awt.Color(97, 97, 97));
        jLabel8.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel8.setText(" Frecuencia");

        jLabel13.setFont(new java.awt.Font("sansserif", 1, 14)); // NOI18N
        jLabel13.setForeground(new java.awt.Color(133, 133, 133));
        jLabel13.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        jLabel13.setText("                   Hz           ");
        jLabel13.setBorder(javax.swing.BorderFactory.createEmptyBorder(3, 3, 3, 3));

        jLabel14.setFont(new java.awt.Font("sansserif", 1, 36)); // NOI18N
        jLabel14.setForeground(new java.awt.Color(204, 51, 255));
        jLabel14.setText("0.05");
        jLabel14.setBorder(javax.swing.BorderFactory.createEmptyBorder(3, 3, 3, 3));

        jLabel5.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/raven/icon/frecuencia.png"))); // NOI18N

        javax.swing.GroupLayout panelShadow2Layout = new javax.swing.GroupLayout(panelShadow2);
        panelShadow2.setLayout(panelShadow2Layout);
        panelShadow2Layout.setHorizontalGroup(
            panelShadow2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelShadow2Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(panelShadow2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(panelShadow2Layout.createSequentialGroup()
                        .addComponent(jLabel8, javax.swing.GroupLayout.PREFERRED_SIZE, 136, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(12, 12, 12))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, panelShadow2Layout.createSequentialGroup()
                        .addComponent(jLabel5, javax.swing.GroupLayout.PREFERRED_SIZE, 136, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)))
                .addGroup(panelShadow2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(panelShadow2Layout.createSequentialGroup()
                        .addComponent(jLabel14, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addGap(71, 71, 71))
                    .addGroup(panelShadow2Layout.createSequentialGroup()
                        .addComponent(jLabel13, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addContainerGap())))
        );
        panelShadow2Layout.setVerticalGroup(
            panelShadow2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, panelShadow2Layout.createSequentialGroup()
                .addComponent(jLabel8)
                .addGroup(panelShadow2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(panelShadow2Layout.createSequentialGroup()
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addGroup(panelShadow2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel14, javax.swing.GroupLayout.PREFERRED_SIZE, 35, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel13, javax.swing.GroupLayout.PREFERRED_SIZE, 11, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(42, 42, 42))
                    .addGroup(panelShadow2Layout.createSequentialGroup()
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jLabel5, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addContainerGap())))
        );

        panelShadow4.setBorder(javax.swing.BorderFactory.createEmptyBorder(20, 20, 20, 20));

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 0, Short.MAX_VALUE)
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 433, Short.MAX_VALUE)
        );

        javax.swing.GroupLayout panelShadow4Layout = new javax.swing.GroupLayout(panelShadow4);
        panelShadow4.setLayout(panelShadow4Layout);
        panelShadow4Layout.setHorizontalGroup(
            panelShadow4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        panelShadow4Layout.setVerticalGroup(
            panelShadow4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );

        jLabel22.setFont(new java.awt.Font("sansserif", 1, 15)); // NOI18N
        jLabel22.setForeground(new java.awt.Color(56, 56, 56));
        jLabel22.setText("Gráfica Distancia vs Tiempo");
        jLabel22.setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 10, 1, 1));

        jLabel23.setFont(new java.awt.Font("sansserif", 1, 15)); // NOI18N
        jLabel23.setForeground(new java.awt.Color(241, 241, 241));
        jLabel23.setText("Datos captados en tiempo real");
        jLabel23.setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 10, 1, 1));

        panelShadow5.setBorder(javax.swing.BorderFactory.createEmptyBorder(20, 20, 20, 20));

        javax.swing.GroupLayout gsenLayout = new javax.swing.GroupLayout(gsen);
        gsen.setLayout(gsenLayout);
        gsenLayout.setHorizontalGroup(
            gsenLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 0, Short.MAX_VALUE)
        );
        gsenLayout.setVerticalGroup(
            gsenLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 433, Short.MAX_VALUE)
        );

        javax.swing.GroupLayout panelShadow5Layout = new javax.swing.GroupLayout(panelShadow5);
        panelShadow5.setLayout(panelShadow5Layout);
        panelShadow5Layout.setHorizontalGroup(
            panelShadow5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(gsen, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        panelShadow5Layout.setVerticalGroup(
            panelShadow5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(gsen, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );

        jLabel24.setFont(new java.awt.Font("sansserif", 1, 15)); // NOI18N
        jLabel24.setForeground(new java.awt.Color(56, 56, 56));
        jLabel24.setText("Grafica Senoidal");
        jLabel24.setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 10, 1, 1));

        panelShadow7.setBorder(javax.swing.BorderFactory.createEmptyBorder(20, 20, 20, 20));
        panelShadow7.setPreferredSize(new java.awt.Dimension(329, 188));

        jLabel12.setFont(new java.awt.Font("sansserif", 1, 8)); // NOI18N
        jLabel12.setForeground(new java.awt.Color(133, 133, 133));
        jLabel12.setBorder(javax.swing.BorderFactory.createEmptyBorder(3, 3, 3, 3));

        jLabel3.setFont(new java.awt.Font("sansserif", 1, 18)); // NOI18N
        jLabel3.setForeground(new java.awt.Color(97, 97, 97));
        jLabel3.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel3.setText("Amplitud");

        labelñe.setFont(new java.awt.Font("sansserif", 1, 36)); // NOI18N
        labelñe.setForeground(new java.awt.Color(0, 204, 102));
        labelñe.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        labelñe.setText("  5.0");
        labelñe.setBorder(javax.swing.BorderFactory.createEmptyBorder(3, 3, 3, 3));

        jLabel25.setFont(new java.awt.Font("sansserif", 1, 14)); // NOI18N
        jLabel25.setForeground(new java.awt.Color(133, 133, 133));
        jLabel25.setText("                  Cm ");
        jLabel25.setBorder(javax.swing.BorderFactory.createEmptyBorder(3, 3, 3, 3));

        jLabel2.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/raven/icon/amplitud.png"))); // NOI18N

        javax.swing.GroupLayout panelShadow7Layout = new javax.swing.GroupLayout(panelShadow7);
        panelShadow7.setLayout(panelShadow7Layout);
        panelShadow7Layout.setHorizontalGroup(
            panelShadow7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelShadow7Layout.createSequentialGroup()
                .addGroup(panelShadow7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(panelShadow7Layout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(jLabel3, javax.swing.GroupLayout.PREFERRED_SIZE, 136, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(panelShadow7Layout.createSequentialGroup()
                        .addGap(12, 12, 12)
                        .addComponent(jLabel2, javax.swing.GroupLayout.PREFERRED_SIZE, 130, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(panelShadow7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel25, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(panelShadow7Layout.createSequentialGroup()
                        .addGroup(panelShadow7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(labelñe)
                            .addComponent(jLabel12))
                        .addGap(0, 97, Short.MAX_VALUE))))
        );
        panelShadow7Layout.setVerticalGroup(
            panelShadow7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, panelShadow7Layout.createSequentialGroup()
                .addComponent(jLabel3)
                .addGroup(panelShadow7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(panelShadow7Layout.createSequentialGroup()
                        .addGap(37, 37, 37)
                        .addGroup(panelShadow7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel25)
                            .addComponent(labelñe, javax.swing.GroupLayout.PREFERRED_SIZE, 41, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(18, 18, 18)
                        .addComponent(jLabel12))
                    .addGroup(panelShadow7Layout.createSequentialGroup()
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jLabel2, javax.swing.GroupLayout.PREFERRED_SIZE, 112, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap())
        );

        panelShadow8.setBorder(javax.swing.BorderFactory.createEmptyBorder(20, 20, 20, 20));

        jLabel26.setFont(new java.awt.Font("sansserif", 1, 18)); // NOI18N
        jLabel26.setForeground(new java.awt.Color(97, 97, 97));
        jLabel26.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel26.setText("Frecuencia Angular");

        jLabel27.setFont(new java.awt.Font("sansserif", 1, 14)); // NOI18N
        jLabel27.setForeground(new java.awt.Color(133, 133, 133));
        jLabel27.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        jLabel27.setText("                   Rad/Seg           ");
        jLabel27.setBorder(javax.swing.BorderFactory.createEmptyBorder(3, 3, 3, 3));

        jLabel28.setFont(new java.awt.Font("sansserif", 1, 36)); // NOI18N
        jLabel28.setForeground(new java.awt.Color(102, 0, 102));
        jLabel28.setText("0.05");
        jLabel28.setBorder(javax.swing.BorderFactory.createEmptyBorder(3, 3, 3, 3));

        jLabel10.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/raven/icon/angular.png"))); // NOI18N

        javax.swing.GroupLayout panelShadow8Layout = new javax.swing.GroupLayout(panelShadow8);
        panelShadow8.setLayout(panelShadow8Layout);
        panelShadow8Layout.setHorizontalGroup(
            panelShadow8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelShadow8Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(panelShadow8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(panelShadow8Layout.createSequentialGroup()
                        .addComponent(jLabel10, javax.swing.GroupLayout.PREFERRED_SIZE, 136, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(panelShadow8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(panelShadow8Layout.createSequentialGroup()
                                .addComponent(jLabel28, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addGap(71, 71, 71))
                            .addGroup(panelShadow8Layout.createSequentialGroup()
                                .addComponent(jLabel27, javax.swing.GroupLayout.DEFAULT_SIZE, 191, Short.MAX_VALUE)
                                .addContainerGap())))
                    .addGroup(panelShadow8Layout.createSequentialGroup()
                        .addComponent(jLabel26)
                        .addGap(0, 0, Short.MAX_VALUE))))
        );
        panelShadow8Layout.setVerticalGroup(
            panelShadow8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, panelShadow8Layout.createSequentialGroup()
                .addComponent(jLabel26)
                .addGroup(panelShadow8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(panelShadow8Layout.createSequentialGroup()
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addGroup(panelShadow8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel28, javax.swing.GroupLayout.PREFERRED_SIZE, 35, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel27, javax.swing.GroupLayout.PREFERRED_SIZE, 35, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(34, 34, 34))
                    .addGroup(panelShadow8Layout.createSequentialGroup()
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jLabel10, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addContainerGap())))
        );

        panelShadow10.setBorder(javax.swing.BorderFactory.createEmptyBorder(20, 20, 20, 20));

        jLabel30.setFont(new java.awt.Font("sansserif", 1, 18)); // NOI18N
        jLabel30.setForeground(new java.awt.Color(97, 97, 97));
        jLabel30.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel30.setText("Periodo");

        jLabel36.setFont(new java.awt.Font("sansserif", 1, 14)); // NOI18N
        jLabel36.setForeground(new java.awt.Color(133, 133, 133));
        jLabel36.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        jLabel36.setText("                        Seg");
        jLabel36.setBorder(javax.swing.BorderFactory.createEmptyBorder(3, 3, 3, 3));

        jLabel37.setFont(new java.awt.Font("sansserif", 1, 36)); // NOI18N
        jLabel37.setForeground(new java.awt.Color(255, 51, 204));
        jLabel37.setText("24,56");
        jLabel37.setBorder(javax.swing.BorderFactory.createEmptyBorder(3, 3, 3, 3));

        jLabel6.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/raven/icon/periodo.png"))); // NOI18N

        javax.swing.GroupLayout panelShadow10Layout = new javax.swing.GroupLayout(panelShadow10);
        panelShadow10.setLayout(panelShadow10Layout);
        panelShadow10Layout.setHorizontalGroup(
            panelShadow10Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelShadow10Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(panelShadow10Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jLabel30, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jLabel6, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(panelShadow10Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(panelShadow10Layout.createSequentialGroup()
                        .addComponent(jLabel37)
                        .addGap(0, 0, Short.MAX_VALUE))
                    .addComponent(jLabel36, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        panelShadow10Layout.setVerticalGroup(
            panelShadow10Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, panelShadow10Layout.createSequentialGroup()
                .addComponent(jLabel30)
                .addGroup(panelShadow10Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(panelShadow10Layout.createSequentialGroup()
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addGroup(panelShadow10Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel36)
                            .addComponent(jLabel37, javax.swing.GroupLayout.PREFERRED_SIZE, 35, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(39, 39, 39))
                    .addGroup(panelShadow10Layout.createSequentialGroup()
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jLabel6, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addContainerGap())))
        );

        panelShadow9.setBorder(javax.swing.BorderFactory.createEmptyBorder(20, 20, 20, 20));
        panelShadow9.setPreferredSize(new java.awt.Dimension(323, 187));

        jLabel29.setFont(new java.awt.Font("sansserif", 1, 18)); // NOI18N
        jLabel29.setForeground(new java.awt.Color(97, 97, 97));
        jLabel29.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel29.setText("Angulo de Fase");

        jLabel34.setFont(new java.awt.Font("sansserif", 1, 14)); // NOI18N
        jLabel34.setForeground(new java.awt.Color(133, 133, 133));
        jLabel34.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        jLabel34.setText("                 Rad");
        jLabel34.setBorder(javax.swing.BorderFactory.createEmptyBorder(3, 3, 3, 3));

        jLabel35.setFont(new java.awt.Font("sansserif", 1, 36)); // NOI18N
        jLabel35.setForeground(new java.awt.Color(255, 102, 51));
        jLabel35.setText(" 0.0");
        jLabel35.setBorder(javax.swing.BorderFactory.createEmptyBorder(3, 3, 3, 3));

        jLabel4.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/raven/icon/angulo.png"))); // NOI18N

        javax.swing.GroupLayout panelShadow9Layout = new javax.swing.GroupLayout(panelShadow9);
        panelShadow9.setLayout(panelShadow9Layout);
        panelShadow9Layout.setHorizontalGroup(
            panelShadow9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelShadow9Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(panelShadow9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jLabel29, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jLabel4, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(panelShadow9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(panelShadow9Layout.createSequentialGroup()
                        .addComponent(jLabel35)
                        .addGap(0, 0, Short.MAX_VALUE))
                    .addComponent(jLabel34, javax.swing.GroupLayout.DEFAULT_SIZE, 169, Short.MAX_VALUE)))
        );
        panelShadow9Layout.setVerticalGroup(
            panelShadow9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, panelShadow9Layout.createSequentialGroup()
                .addComponent(jLabel29)
                .addGroup(panelShadow9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(panelShadow9Layout.createSequentialGroup()
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addGroup(panelShadow9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel34)
                            .addComponent(jLabel35, javax.swing.GroupLayout.PREFERRED_SIZE, 35, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(39, 39, 39))
                    .addGroup(panelShadow9Layout.createSequentialGroup()
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jLabel4, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addContainerGap())))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(panelShadow4, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(panelShadow5, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel22)
                    .addComponent(jLabel23)
                    .addGroup(layout.createSequentialGroup()
                        .addGap(6, 6, 6)
                        .addComponent(jLabel24)))
                .addGap(0, 0, Short.MAX_VALUE))
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(panelShadow1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addGap(0, 0, 0)
                        .addComponent(panelShadow2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(panelShadow10, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(panelShadow7, javax.swing.GroupLayout.PREFERRED_SIZE, 361, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(panelShadow8, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(panelShadow9, javax.swing.GroupLayout.PREFERRED_SIZE, 357, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(22, 22, 22)
                .addComponent(jLabel23)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(panelShadow1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(panelShadow2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(panelShadow10, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel22)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(panelShadow4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(panelShadow7, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(panelShadow9, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addComponent(panelShadow8, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel24)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(panelShadow5, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel consola;
    private com.raven.chart.GaugeChart gaugeChart1;
    private javax.swing.JPanel gsen;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel12;
    private javax.swing.JLabel jLabel13;
    private javax.swing.JLabel jLabel14;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel22;
    private javax.swing.JLabel jLabel23;
    private javax.swing.JLabel jLabel24;
    private javax.swing.JLabel jLabel25;
    private javax.swing.JLabel jLabel26;
    private javax.swing.JLabel jLabel27;
    private javax.swing.JLabel jLabel28;
    private javax.swing.JLabel jLabel29;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel30;
    private javax.swing.JLabel jLabel34;
    private javax.swing.JLabel jLabel35;
    private javax.swing.JLabel jLabel36;
    private javax.swing.JLabel jLabel37;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JLabel labelñe;
    private com.raven.swing.PanelShadow panelShadow1;
    private com.raven.swing.PanelShadow panelShadow10;
    private com.raven.swing.PanelShadow panelShadow2;
    private com.raven.swing.PanelShadow panelShadow4;
    private com.raven.swing.PanelShadow panelShadow5;
    private com.raven.swing.PanelShadow panelShadow7;
    private com.raven.swing.PanelShadow panelShadow8;
    private com.raven.swing.PanelShadow panelShadow9;
    // End of variables declaration//GEN-END:variables
}
