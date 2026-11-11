import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.ArrayList;

public class ClientGUI {
    private JFrame frame;
    private JTextField ipField;
    private JTextField portField;
    private JButton connectButton;
    private DefaultListModel<String> fileListModel;

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new ClientGUI().createAndShowGUI());
    }

    private void createAndShowGUI() {
        frame = new JFrame("Cliente GUI");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(300, 150);
        frame.setLocationRelativeTo(null);

        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

        ipField = new JTextField(20);
        setDefaultText(ipField, "Ingrese la IP...");

        portField = new JTextField(20);
        setDefaultText(portField, "Ingrese el puerto...");

        connectButton = new JButton("Conectar");

        connectButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String ip = ipField.getText();
                String port = portField.getText();

                // Verifica los valores proporcionados para la dirección IP y el puerto del servidor.
                // Si los valores proporcionados son invalidos se intentará la conexión usando los
                // valores predeterminados.
                if (!isValidIPAddress(ip)) {
                    ip = "127.0.0.1";
                }

                port = ValidPort(port);

                try {
                    // Establece la variable de entorno del sistema
                    String serverPortValue = ip + ":" + port;
                    System.out.println("Setting SERVER_PORT to: " + serverPortValue);
                    System.setProperty("SERVER_PORT", serverPortValue);
        
                    // Verifica la variable del sistema
                    String serverPortPropertyValue = System.getProperty("SERVER_PORT");
                    if (serverPortPropertyValue != null && serverPortPropertyValue.equals(serverPortValue)) {
                        System.out.println("Variable del sistema establecida con éxito.");
                        Client.setHostIP(ip);
                        Client.setPort(Integer.parseInt(port));
                        openFileListWindow();
                    } else {
                        System.out.println("Error al establecer la Variable del sistema.");
                    }
                } catch (Exception err) {
                    err.printStackTrace();
                }
            }
        });

        panel.add(new JLabel("IP del Servidor:"));
        panel.add(ipField);
        panel.add(new JLabel("Puerto del Servidor:"));
        panel.add(portField);
        panel.add(connectButton);

        // Agrega un margen al panel
        int marginSize = 10;
        panel.setBorder(BorderFactory.createEmptyBorder(marginSize, marginSize, marginSize, marginSize));
    
        frame.getContentPane().add(panel);
        frame.setVisible(true);
    }

    private void setDefaultText(JTextField textField, String defaultText) {
        textField.setText(defaultText);
        textField.setForeground(UIManager.getColor("TextField.inactiveForeground"));

        textField.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                if (textField.getText().equals(defaultText)) {
                    textField.setText("");
                    textField.setForeground(UIManager.getColor("TextField.foreground"));
                }
            }

            public void focusLost(java.awt.event.FocusEvent evt) {
                if (textField.getText().isEmpty()) {
                    textField.setText(defaultText);
                    textField.setForeground(UIManager.getColor("TextField.inactiveForeground"));
                }
            }
        });
    }

    // Método para obtener y actualizar la lista de archivos
    private void updateFileList(String dir) {
        fileListModel.clear(); // Limpiar el modelo actual
        Client.main(new String[]{"ls", dir});
        ArrayList<String> fileNames = Client.getListFiles();
        for (String fileName : fileNames) {
            fileListModel.addElement(fileName);
        }
    }

    private void openFileListWindow() {
        JFrame fileListFrame = new JFrame("Lista de Archivos");
        fileListFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        fileListFrame.setSize(600, 400); // Ajusta el tamaño según tus necesidades
        fileListFrame.setLocationRelativeTo(frame);
    
        JPanel mainPanel = new JPanel(new BorderLayout());
    
        // Panel para los botones
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
    
        JButton shutdownButton = new JButton("Apagar");
        JButton refreshButton = new JButton("Actualizar");
        JButton createFolderButton = new JButton("Crear Carpeta");
        JButton addFileButton = new JButton("Añadir Archivo");
        JButton deleteButton = new JButton("Borrar");
    
        buttonPanel.add(shutdownButton);
        buttonPanel.add(Box.createHorizontalGlue()); // Espaciado
        buttonPanel.add(refreshButton);
        buttonPanel.add(createFolderButton);
        buttonPanel.add(addFileButton);
        buttonPanel.add(deleteButton);
    
        // Ejemplo de nombres de archivo
        String dir = Client.getServerPath();

        // Inicializar el modelo de la lista de archivos
        fileListModel = new DefaultListModel<>();
        updateFileList(dir);

        JList<String> fileList = new JList<>(fileListModel);
        fileList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
    
        JScrollPane scrollPane = new JScrollPane(fileList);
    
        mainPanel.add(buttonPanel, BorderLayout.NORTH);
        mainPanel.add(scrollPane, BorderLayout.CENTER);
    
        JButton downloadButton = new JButton("Descargar");
        mainPanel.add(downloadButton, BorderLayout.SOUTH);

        // Acción para el botón de "Apagar"
        shutdownButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                Client.main(new String[]{"shutdown"});
            }
        });

        // Acción para el botón de "Actualizar"
        refreshButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // Realiza la acción correspondiente para actualizar
                // Puedes llamar al método que actualiza la lista de archivos
                updateFileList(dir);
            }
        });

        // Acción para el botón de "Crear Carpeta"
        createFolderButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // Realiza la acción correspondiente para crear una carpeta
                // Puedes abrir un cuadro de diálogo para obtener el nombre de la carpeta
                String newFolderName = JOptionPane.showInputDialog("Ingrese el nombre de la carpeta:");
                if (newFolderName != null) {
                    Client.main(new String[]{"mkdir", dir + newFolderName});
                    updateFileList(dir);
                }
            }
        });

        // Acción para el botón de "Añadir Archivo"
        addFileButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // Realiza la acción correspondiente para añadir un archivo
                // Puedes abrir un cuadro de diálogo para seleccionar el archivo
                // y obtener la ruta del archivo seleccionado
                JFileChooser fileChooser = new JFileChooser();
                int result = fileChooser.showOpenDialog(null);
                if (result == JFileChooser.APPROVE_OPTION) {
                    File selectedFile = fileChooser.getSelectedFile();
                    Client.main(new String[]{"upload", selectedFile.getAbsolutePath(), dir});
                    updateFileList(dir);
                }
            }
        });

        // Acción para el botón de "Borrar"
        deleteButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // Realiza la acción correspondiente para borrar
                // Puedes abrir un cuadro de diálogo para obtener el nombre del archivo o carpeta a borrar
                String itemName = JOptionPane.showInputDialog("Ingrese el nombre del archivo/carpeta a borrar:");
                if (itemName != null) {
                    Client.main(new String[]{"rm", dir + itemName});
                    updateFileList(dir);
                }
            }
        });

        // Acción para el botón de "Descargar"
        downloadButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // Aquí puedes obtener los elementos seleccionados
                int[] selectedIndices = fileList.getSelectedIndices();
                for (int selectedIndex : selectedIndices) {
                    String selectedFile = fileListModel.getElementAt(selectedIndex);
                    Client.main(new String[]{"download", dir + selectedFile, "ClientStorage/" + selectedFile});
                    System.out.println("Archivo seleccionado: " + dir + selectedFile);
                }
            }
        });
    
        // Agrega un margen al panel principal
        int marginSize = 10;
        mainPanel.setBorder(BorderFactory.createEmptyBorder(marginSize, marginSize, marginSize, marginSize));
    
        fileListFrame.getContentPane().add(mainPanel);
        fileListFrame.setVisible(true);
    }
    
    // Valida la cadena de IP proporcionada:
	private static boolean isValidIPAddress(String ip) {
		String ipAddressPattern = "^([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\." +
															"([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\." +
															"([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\." +
															"([01]?\\d\\d?|2[0-4]\\d|25[0-5])$";
		String localhostPattern = "^localhost$";
		return ip.matches(ipAddressPattern) || ip.matches(localhostPattern);
	}

    private static String ValidPort(String port) {
        if (Integer.parseInt(port) == 0) {
            // Si no se especifica un puerto se utiliza el predeterminado:
            return "1099";
        } else if (Integer.parseInt(port) <= 1024) {
            // Si se utiliza un puerto reservado:
            return "1099";
        }
        return port;
    }
}
