
import comunicacionserial.ArduinoExcepcion;
import comunicacionserial.ComunicacionSerial_Arduino;
import java.awt.BasicStroke;
import java.awt.Color;
import java.util.logging.Level;
import java.util.logging.Logger;
import jssc.SerialPortEvent;
import jssc.SerialPortEventListener;
import jssc.SerialPortException;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

public class Principal extends javax.swing.JFrame {

    static JFreeChart chart;
    static ChartPanel panel;
    int cont = 0;
    Thread hilo;
    boolean On = true;
    public static boolean statusHilo = false;
    boolean Graficar = true;
    //Se crea una instancia de la librería PanamaHitek_Arduino
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
                Logger.getLogger(Principal.class.getName()).log(Level.SEVERE, null, ex);
            } catch (ArduinoExcepcion ex) {
                Logger.getLogger(Principal.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

    };

    public Principal() {
        initComponents();
        try {
            ino.arduinoRXTX("COM6", 9600, listener);
        } catch (ArduinoExcepcion ex) {
            Logger.getLogger(Principal.class.getName()).log(Level.SEVERE, null, ex);
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
                        crearGrafica();
                        Thread.sleep(2000);
                    } catch (InterruptedException ex) {
                        Logger.getLogger(Principal.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            }

        };
    }
    XYSeries dataset = new XYSeries("Registros Distancias EcoWave");

    private void crearGrafica() {
        //https://www.tutorialspoint.com/jfreechart/jfreechart_xy_chart.htm
        String[] lecturas = consola.getText().split("\n");
        if (lecturas.length > 0) {
            if (!lecturas[lecturas.length - 1].trim().equals("")) {
                if (Double.parseDouble(lecturas[lecturas.length - 1]) < 50 && Double.parseDouble(lecturas[lecturas.length - 1]) > -10) {
                    dataset.add(cont, Double.parseDouble(lecturas[lecturas.length - 1]));
                    cont++;
                }
            }

        }

        XYSeriesCollection collection = new XYSeriesCollection();
        collection.addSeries(dataset);

        chart = ChartFactory.createXYLineChart(
                "X vs Tiempo", // chart title
                "Tiempo", // X axis label
                "Distancia", // Y axis label
                collection, // data
                PlotOrientation.VERTICAL,
                true, // include legend
                true, // tooltips
                false // urls
        );

        panel = new ChartPanel(chart, true, true, true, false, true);
        panel.setPreferredSize(new java.awt.Dimension(560, 367));
        XYPlot plot = chart.getXYPlot();
        XYLineAndShapeRenderer renderer = new XYLineAndShapeRenderer();
        renderer.setSeriesPaint(0, Color.RED);
        renderer.setSeriesStroke(0, new BasicStroke(2.0f));
        plot.setRenderer(renderer);
        jPanel1.setLayout(null);
        panel.setBounds(0, 0, jPanel1.getWidth(), jPanel1.getHeight());
        jPanel1.removeAll();
        jPanel1.add(panel);
        jPanel1.repaint();

    }

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        panelRound1 = new custom.PanelRound();
        btnGraficar = new javax.swing.JButton();
        jPanel1 = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        consola = new javax.swing.JTextArea();
        btnReset = new javax.swing.JButton();
        btnReanudar = new javax.swing.JButton();
        btnPausa = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        panelRound1.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        btnGraficar.setText("Graficar");
        btnGraficar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnGraficarActionPerformed(evt);
            }
        });
        panelRound1.add(btnGraficar, new org.netbeans.lib.awtextra.AbsoluteConstraints(220, 500, -1, -1));

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 812, Short.MAX_VALUE)
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 422, Short.MAX_VALUE)
        );

        panelRound1.add(jPanel1, new org.netbeans.lib.awtextra.AbsoluteConstraints(14, 48, -1, -1));

        consola.setColumns(20);
        consola.setRows(5);
        jScrollPane1.setViewportView(consola);

        panelRound1.add(jScrollPane1, new org.netbeans.lib.awtextra.AbsoluteConstraints(868, 48, 264, 502));

        btnReset.setText("Reset");
        btnReset.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnResetActionPerformed(evt);
            }
        });
        panelRound1.add(btnReset, new org.netbeans.lib.awtextra.AbsoluteConstraints(560, 500, -1, -1));

        btnReanudar.setText("Reanudar");
        btnReanudar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnReanudarActionPerformed(evt);
            }
        });
        panelRound1.add(btnReanudar, new org.netbeans.lib.awtextra.AbsoluteConstraints(460, 500, -1, -1));

        btnPausa.setText("Pausa");
        btnPausa.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnPausaActionPerformed(evt);
            }
        });
        panelRound1.add(btnPausa, new org.netbeans.lib.awtextra.AbsoluteConstraints(380, 500, -1, -1));

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(panelRound1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(panelRound1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 34, Short.MAX_VALUE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void btnGraficarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnGraficarActionPerformed
        btnGraficar.setEnabled(false);
        btnPausa.setEnabled(true);
        btnReset.setEnabled(true);
        On = true;
        ReiniciarHilo();
        hilo.start();
        
    }//GEN-LAST:event_btnGraficarActionPerformed

    private void btnResetActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnResetActionPerformed
       
            btnGraficar.setEnabled(true);
            btnPausa.setEnabled(false);
            btnReset.setEnabled(false);
            consola.setEnabled(false);
            On = false;
            hilo.stop();
            statusHilo = false;
            
    }//GEN-LAST:event_btnResetActionPerformed

    private void btnPausaActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnPausaActionPerformed

            btnReanudar.setEnabled(true);
            btnPausa.setEnabled(false);
            consola.setEnabled(false);
            On = false;
            hilo.stop();
            statusHilo = false;
            
        
    }//GEN-LAST:event_btnPausaActionPerformed

    private void btnReanudarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnReanudarActionPerformed
        On = true;
        ReiniciarHilo();
            hilo.start();
        btnReanudar.setEnabled(false);
        btnPausa.setEnabled(true);    }//GEN-LAST:event_btnReanudarActionPerformed

    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(Principal.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(Principal.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(Principal.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(Principal.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new Principal().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnGraficar;
    private javax.swing.JButton btnPausa;
    private javax.swing.JButton btnReanudar;
    private javax.swing.JButton btnReset;
    private static javax.swing.JTextArea consola;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JScrollPane jScrollPane1;
    private custom.PanelRound panelRound1;
    // End of variables declaration//GEN-END:variables
}
