import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.Scanner;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;

public class WatermarkApp extends JFrame {

    private CardLayout cardLayout = new CardLayout();
    private JPanel mainPanel = new JPanel(cardLayout);

    private String currentUser = "";
    private File selectedFile;

    private BufferedImage originalImage;

    private JLabel previewLabel;
    private JTextField watermarkText;
    private JComboBox<String> fontSelector;
    private JComboBox<Integer> fontSize;

    private Color selectedColor = Color.WHITE;

    public WatermarkApp() {

        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {}

        setTitle("Secure Watermark System");
        setSize(750,550);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        initLoginPage();
        initUserPage();
        initAdminPage();

        add(mainPanel);
        setVisible(true);
    }

    private void initLoginPage() {

        JPanel loginPanel = new JPanel(new GridLayout(5,1,10,10));
        loginPanel.setBorder(BorderFactory.createEmptyBorder(60,200,60,200));

        JTextField userField = new JTextField();
        JPasswordField passField = new JPasswordField();
        JButton loginBtn = new JButton("Login");

        loginPanel.add(new JLabel("Username"));
        loginPanel.add(userField);
        loginPanel.add(new JLabel("Password"));
        loginPanel.add(passField);
        loginPanel.add(loginBtn);

        loginBtn.addActionListener(e -> {

            String user = userField.getText();
            String pass = new String(passField.getPassword());

            if(pass.equals("123")) {

                currentUser = user;

                if(user.equalsIgnoreCase("admin"))
                    cardLayout.show(mainPanel,"admin");
                else
                    cardLayout.show(mainPanel,"user");

            } else {
                JOptionPane.showMessageDialog(this,"Invalid credentials");
            }

        });

        mainPanel.add(loginPanel,"login");
    }

    private void initUserPage() {

        JPanel userPanel = new JPanel();
        userPanel.setLayout(new BoxLayout(userPanel,BoxLayout.Y_AXIS));
        userPanel.setBorder(BorderFactory.createEmptyBorder(20,20,20,20));

        JButton uploadBtn = new JButton("Select Image");

        watermarkText = new JTextField(15);

        String[] fonts = GraphicsEnvironment
                .getLocalGraphicsEnvironment()
                .getAvailableFontFamilyNames();

        fontSelector = new JComboBox<>(fonts);

        Integer[] sizes = {20,30,40,50,60};
        fontSize = new JComboBox<>(sizes);

        JButton colorBtn = new JButton("Choose Color");

        previewLabel = new JLabel();
        previewLabel.setPreferredSize(new Dimension(500,250));
        previewLabel.setBorder(BorderFactory.createLineBorder(Color.GRAY));

        JButton processBtn = new JButton("Apply & Save");
        JButton logoutBtn = new JButton("Logout");

        userPanel.add(new JLabel("User Dashboard"));
        userPanel.add(uploadBtn);

        userPanel.add(new JLabel("Watermark Text"));
        userPanel.add(watermarkText);

        userPanel.add(new JLabel("Font Style"));
        userPanel.add(fontSelector);

        userPanel.add(new JLabel("Font Size"));
        userPanel.add(fontSize);

        userPanel.add(colorBtn);

        userPanel.add(new JLabel("Live Preview"));
        userPanel.add(previewLabel);

        userPanel.add(processBtn);
        userPanel.add(logoutBtn);

        uploadBtn.addActionListener(e -> {

            JFileChooser jfc = new JFileChooser();

            if(jfc.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {

                selectedFile = jfc.getSelectedFile();

                try {
                    originalImage = ImageIO.read(selectedFile);
                    updatePreview();
                } catch(Exception ex) {
                    ex.printStackTrace();
                }
            }
        });

        colorBtn.addActionListener(e -> {

            Color c = JColorChooser.showDialog(this,"Choose Color",Color.WHITE);

            if(c != null) {
                selectedColor = c;
                updatePreview();
            }

        });

        watermarkText.addKeyListener(new KeyAdapter() {
            public void keyReleased(KeyEvent e) {
                updatePreview();
            }
        });

        fontSelector.addActionListener(e -> updatePreview());
        fontSize.addActionListener(e -> updatePreview());

        processBtn.addActionListener(e -> {

            if(selectedFile == null) {
                JOptionPane.showMessageDialog(this,"Please select an image first");
                return;
            }

            try {

                File output = new File("watermarked_"+selectedFile.getName());

                WatermarkEngine.addTextWatermark(
                        watermarkText.getText(),
                        selectedFile,
                        output,
                        currentUser,
                        (String)fontSelector.getSelectedItem(),
                        (Integer)fontSize.getSelectedItem(),
                        selectedColor
                );

                JOptionPane.showMessageDialog(this,"Saved as "+output.getName());

            } catch(Exception ex) {
                JOptionPane.showMessageDialog(this,ex.getMessage());
            }

        });

        logoutBtn.addActionListener(e -> cardLayout.show(mainPanel,"login"));

        mainPanel.add(userPanel,"user");
    }

    private void updatePreview() {

        if(originalImage == null) return;

        try {

            BufferedImage preview = new BufferedImage(
                    originalImage.getWidth(),
                    originalImage.getHeight(),
                    BufferedImage.TYPE_INT_ARGB);

            Graphics2D g2 = preview.createGraphics();

            g2.drawImage(originalImage,0,0,null);

            g2.setColor(selectedColor);

            g2.setFont(new Font(
                    (String)fontSelector.getSelectedItem(),
                    Font.BOLD,
                    (Integer)fontSize.getSelectedItem()
            ));

            String text = watermarkText.getText();

            int x = preview.getWidth()/5;
            int y = preview.getHeight()/2;

            g2.drawString(text,x,y);

            g2.dispose();

            Image scaled = preview.getScaledInstance(
                    500,
                    250,
                    Image.SCALE_SMOOTH);

            previewLabel.setIcon(new ImageIcon(scaled));

        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    private void initAdminPage() {

        JPanel adminPanel = new JPanel(new BorderLayout());

        String[] columns = {"User","File","Timestamp"};
        DefaultTableModel model = new DefaultTableModel(columns,0);

        JTable table = new JTable(model);

        JButton refreshBtn = new JButton("Refresh Logs");
        JButton logoutBtn = new JButton("Logout");

        refreshBtn.addActionListener(e -> {

            model.setRowCount(0);

            try(Scanner sc = new Scanner(new File("logs.txt"))) {

                while(sc.hasNextLine()) {
                    model.addRow(sc.nextLine().split(","));
                }

            } catch(Exception ex) {
                System.out.println("No logs yet");
            }

        });

        logoutBtn.addActionListener(e -> cardLayout.show(mainPanel,"login"));

        JPanel btnPanel = new JPanel();
        btnPanel.add(refreshBtn);
        btnPanel.add(logoutBtn);

        adminPanel.add(new JLabel("ADMIN PANEL - System Logs",SwingConstants.CENTER),BorderLayout.NORTH);
        adminPanel.add(new JScrollPane(table),BorderLayout.CENTER);
        adminPanel.add(btnPanel,BorderLayout.SOUTH);

        mainPanel.add(adminPanel,"admin");
    }

    public static void main(String[] args) {
        new WatermarkApp();
    }
}
