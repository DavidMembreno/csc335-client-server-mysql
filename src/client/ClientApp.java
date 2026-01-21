package client;


import Misc.DBaseConnectionP;
import java.awt.*;
import javax.swing.*;

public class ClientApp extends JFrame {

    private static final String IP = "localhost";
    // UI components
    private JLabel usernameLabel;
    private JLabel passwordLabel;
    private JLabel emailLabel;
    private JTextField usernameField;
    private JPasswordField passwordField;
    private JTextField emailField;
    private JButton loginButton;
    private JButton registerButton;
    private JButton recoveryButton;
    private JButton closeButton;
    private JButton logoutButton;
    private JButton shutdownButton;
    private JButton changePasswordButton;
    private JCheckBox showPasswordBox;
    private JButton queryButton;
    private JLabel IPLabel;
    private JTextField IPField;
    private JButton connectDisconnectButton;

    // State
    private Color backgroundCol = new Color(39, 60, 117);
    private int frameWidth = 780;
    private int frameHeight = 800;
    private boolean registerMode = false;
    private boolean recoveryMode = false;
    private boolean connected = false;
    DBaseConnectionP dbc = null;

    // -------------------- Constructor: Login/Register UI --------------------
    public ClientApp(boolean connect) {
        super();
        connected = connect;
        this.setTitle("Client UI");
        this.setSize(frameWidth, frameHeight);
        this.setLocationRelativeTo(null);
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setLayout(null);
        this.getContentPane().setBackground(backgroundCol);

        // Labels
        usernameLabel = new JLabel("Username:");
        passwordLabel = new JLabel("Password:");
        emailLabel = new JLabel("Email:");

        IPLabel = new JLabel("Server IP:");
        IPLabel.setForeground(Color.WHITE);
        IPField = new JTextField("");
        IPLabel.setBounds(400, 20, 100, 30);
        IPField.setBounds(500, 20, 200, 30);

        connectDisconnectButton = new JButton("Connect");
        
        connectDisconnectButton.setBounds(500, 60, 200, 30);


        showPasswordBox = new JCheckBox("Show Password");

        usernameField = new JTextField();
        passwordField = new JPasswordField();
        emailField = new JTextField();
        Color textCol = Color.WHITE;

        JLabel helloLabel = new JLabel("Hello!", SwingConstants.CENTER);
        helloLabel.setForeground(Color.WHITE);
        helloLabel.setFont(new Font("Arial", Font.BOLD, 84));
        helloLabel.setBounds(0, 200, frameWidth, 80);
        this.add(helloLabel);

        JLabel prompt = new JLabel("Please Login or Register", SwingConstants.CENTER);
        prompt.setForeground(Color.LIGHT_GRAY);
        prompt.setFont(new Font("Arial", Font.PLAIN, 20));
        prompt.setBounds(0, 280, frameWidth, 50);
        this.add(prompt);

        usernameLabel.setForeground(textCol);
        passwordLabel.setForeground(textCol);
        emailLabel.setForeground(textCol);
        emailLabel.setVisible(false);

        showPasswordBox.setBackground(backgroundCol);
        showPasswordBox.setForeground(textCol);

        usernameLabel.setBounds(210, 350, 100, 30);
        passwordLabel.setBounds(210, 400, 100, 30);
        emailLabel.setBounds(210, 450, 100, 30);

        usernameField.setBounds(330, 350, 200, 30);
        passwordField.setBounds(330, 400, 200, 30);
        showPasswordBox.setBounds(540, 400, 150, 30);
        emailField.setBounds(330, 450, 200, 30);
        emailField.setVisible(false);

        // Buttons
        loginButton = new JButton("Login");
        registerButton = new JButton("Register");
        recoveryButton = new JButton("Recover Password");
        closeButton = new JButton("Close");

        loginButton.setBounds(260, 520, 140, 40);
        registerButton.setBounds(420, 520, 140, 40);
        recoveryButton.setBounds(260, 600, 300, 40);
        closeButton.setBounds(260, 680, 300, 40);

        this.add(usernameLabel);
        this.add(usernameField);
        this.add(passwordLabel);
        this.add(passwordField);
        this.add(IPLabel);
        this.add(IPField);
        this.add(connectDisconnectButton);
        this.add(emailLabel);
        this.add(emailField);
        this.add(loginButton);
        this.add(registerButton);
        this.add(recoveryButton);
        this.add(closeButton);
        this.add(showPasswordBox);

        loginButton.setEnabled(false);
        recoveryButton.setEnabled(false);
        registerButton.setEnabled(false);
        //DisconnectedMode();

        if (!connected) {
            connectDisconnectButton.setText("Connect");
            //DisconnectedMode();
        } else {
            dbc = new DBaseConnectionP();
            connectDisconnectButton.setText("Disconnect");
            IPField.setText("localhost");
            IPField.setEditable(false);
            //ConnectedMode();
            loginButton.setEnabled(true);
            recoveryButton.setEnabled(true);
            registerButton.setEnabled(true);
        }

        // Disconnect
        closeButton.addActionListener(e -> {
            if (connected) {
                //connected = false;
                //closeButton.setText("Disconnected");
                dbc.removeConnection();
                dbc.endConnectionToServer();

                
            }
            this.dispose();
        });

        //disconnect with window close
        this.addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent windowEvent) {
                if (connected) {
                    connected = false;
                    dbc.removeConnection();
                    dbc.endConnectionToServer();
                    System.exit(0);
                }
            }
        });

        // Register
        registerButton.addActionListener(e -> {
            if (!registerMode) {
                enterRegisterMode();
                getRootPane().setDefaultButton(registerButton);
                return;
            }
            TaskEx.runTask(new ClientTask("register",
                    usernameField.getText(),
                    new String(passwordField.getPassword()),
                    emailField.getText(),
                    this,
                    dbc));
            getRootPane().setDefaultButton(loginButton);
            exitRegisterMode();
        });

        // Recovery
        recoveryButton.addActionListener(e -> {
            if (!recoveryMode) {
                enterRecoveryMode();
                getRootPane().setDefaultButton(recoveryButton);
                return;
            }
            TaskEx.runTask(new ClientTask("recover",
                    usernameField.getText(),
                    null,
                    null,
                    this,
                    dbc));
                    getRootPane().setDefaultButton(loginButton);
            exitRecoveryMode();
        });

        // Login
        loginButton.addActionListener(e -> {
            TaskEx.runTask(new ClientTask("login",
                    usernameField.getText(),
                    new String(passwordField.getPassword()),
                    null,
                    this,
                    dbc));
        });

        connectDisconnectButton.addActionListener(e -> {
            if (!connected) {
                boolean success = connectToServer();
                if(success){
                    ConnectedMode();
                }
            } else {
                disconnectFromServer();
                DisconnectedMode();
            }
        }); 
        // Enter key listener for login
        
        getRootPane().setDefaultButton(loginButton);
        // Show Password
        showPasswordBox.addActionListener(e -> showPassword());

        this.setVisible(true);
    }

    // -------------------- Constructor: User Dashboard --------------------
    public ClientApp(String username) {
        super();
        this.setTitle("User UI");
        this.setSize(frameWidth + 200, frameHeight);
        this.setLocationRelativeTo(null);
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setLayout(null);
        this.getContentPane().setBackground(backgroundCol);

        dbc = new DBaseConnectionP();
        connected = true;
        JPanel centerCard = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                // rounded white background
                g2.setColor(new Color(230, 235, 240));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 60, 60);

                // stick figure drawing
                g2.setColor(Color.DARK_GRAY);
                g2.setStroke(new BasicStroke(3));
                int centerX = getWidth() / 2;
                g2.drawOval(centerX - 40, 40, 80, 80);
                g2.drawArc(centerX - 25, 70, 50, 40, 0, -180);
                g2.drawArc(centerX - 60, 120, 120, 160, 0, 180);
            }
        };
        int cardWidth = 400;
        int cardX = ((frameWidth + 200) - cardWidth) / 2;
        centerCard.setBounds(cardX, 260, cardWidth, 300);
        centerCard.setOpaque(false);
        this.add(centerCard);

        changePasswordButton = new JButton("Change Password");
        changePasswordButton.setBounds(20, 30, 150, 35);
        this.add(changePasswordButton);
        changePasswordButton.addActionListener(e -> {
            String newPassword = JOptionPane.showInputDialog(this, "Enter new password:");
            if (newPassword != null && !newPassword.isEmpty()) {
                TaskEx.runTask(new ClientTask("changePassword",
                        username,
                        newPassword,
                        null,
                        this,
                        dbc));
            }
        });

        queryButton = new JButton("Query");
        queryButton.setBounds(200, 30, 120, 35);
        this.add(queryButton);
        queryButton.addActionListener(e -> {
            server.ServerApplication serverApp = new server.ServerApplication(username, dbc);
            this.dispose();
        });
        

        logoutButton = new JButton("Logout");
        logoutButton.setBounds((frameWidth + 200) - 300, 30, 120, 35);
        this.add(logoutButton);
        logoutButton.addActionListener(e -> {
            TaskEx.runTask(new ClientTask("logout",
                    username,
                    null,
                    null,
                    this,
                    dbc));
        });

        shutdownButton = new JButton("Shutdown");
        shutdownButton.setBounds((frameWidth + 200) - 160, 30, 120, 35);
        this.add(shutdownButton);
        shutdownButton.addActionListener(e -> {
            connected = false;
            this.dispose();
            
            TaskEx.runTask(new ClientTask("shutdown",
                    username,
                    null,
                    null,
                    this,
                    dbc));
            dbc.removeConnection();
            dbc.endConnectionToServer();
        });

        //if the pannel is closed while connected, logout
        this.addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent windowEvent) {
                connected = false;
                dbc.removeConnection();
                TaskEx.runTask(new ClientTask("shutdown",
                        username,
                        null,
                        null,
                        null,
                        dbc));
                dbc.endConnectionToServer();
            }
        });

        JLabel welcomeLabel = new JLabel("Welcome " + username + "!", SwingConstants.CENTER);
        welcomeLabel.setForeground(Color.WHITE);
        welcomeLabel.setFont(new Font("Arial", Font.BOLD, 42));
        welcomeLabel.setBounds(0, 180, frameWidth + 200, 60);
        this.add(welcomeLabel);
        
        
        this.setVisible(true);
    }

    // -------------------- Mode Helpers --------------------
    private void shutdownClient() {
        this.dispose();
    }
    private void enterRegisterMode() {
        registerMode = true;
        if (recoveryMode) exitRecoveryMode();
        emailLabel.setVisible(true);
        emailField.setVisible(true);
        registerButton.setText("Submit");
        loginButton.setEnabled(false);
    }

    private boolean connectToServer() {
        String ipAddress = IPField.getText();
        if (!ipAddress.isEmpty()) {
            try {
                
                if (ipAddress.equals("localhost")) {
                    dbc = new DBaseConnectionP(ipAddress);
                    connected = true;
                    JOptionPane.showMessageDialog(this, "Connected to server at " + ipAddress);
                    connectDisconnectButton.setText("Disconnect");
                    IPField.setEditable(false);
                    return true;
                }   else {
                    JOptionPane.showMessageDialog(this, "Failed to connect to server at " + ipAddress);
                    return false;
                }
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, "Failed to connect to server at " + ipAddress);
                return false;
            }
            // boolean success = (ipAddress.equals("localhost")); // Placeholder for actual connection logic
            // if (success) {
            //     dbc = new DBaseConnectionP();
            //     connected = true;
            //     JOptionPane.showMessageDialog(this, "Connected to server at " + ipAddress);
            //     connectDisconnectButton.setText("Disconnect");
            //     IPField.setEditable(false);
            // } else {
            //     JOptionPane.showMessageDialog(this, "Failed to connect to server at " + ipAddress);
            // }
        } else {
            JOptionPane.showMessageDialog(this, "Please enter a valid IP address.");
            return false;
        }
    }

    private void disconnectFromServer() {
        connected = false;
        JOptionPane.showMessageDialog(this, "Disconnected from server.");
        connectDisconnectButton.setText("Connect");
        IPField.setText("");
        IPField.setEditable(true);
        dbc.removeConnection();
        dbc.endConnectionToServer();
    }

    private void exitRegisterMode() {
        registerMode = false;
        emailLabel.setVisible(false);
        emailField.setVisible(false);
        registerButton.setText("Register");
        loginButton.setEnabled(true);
        usernameField.setText("");
        passwordField.setText("");
        emailField.setText("");
    }

    private void enterRecoveryMode() {
        recoveryMode = true;
        if (registerMode) exitRegisterMode();
        passwordLabel.setVisible(false);
        passwordField.setVisible(false);
        recoveryButton.setText("Submit");
        registerButton.setEnabled(false);
        loginButton.setEnabled(false);
        showPasswordBox.setVisible(false);
    }

    private void exitRecoveryMode() {
        recoveryMode = false;
        passwordLabel.setVisible(true);
        passwordField.setVisible(true);
        recoveryButton.setText("Recover Password");
        loginButton.setEnabled(true);
        registerButton.setEnabled(true);
        showPasswordBox.setVisible(true);
        usernameField.setText("");
        passwordField.setText("");
        emailField.setText("");
    }


    private void showPassword() {
        if (showPasswordBox.isSelected()) {
            passwordField.setEchoChar((char) 0); // Show password
        } else {
            passwordField.setEchoChar('*'); // Hide password
        }
    }

    private void ConnectedMode() {
        //usernameField.setEditable(true);
        //passwordField.setEditable(true);
        loginButton.setEnabled(true);
        recoveryButton.setEnabled(true);
        registerButton.setEnabled(true);
    }

    private void DisconnectedMode() {
        //usernameField.setEditable(false);
        //passwordField.setEditable(false);
        loginButton.setEnabled(false);
        recoveryButton.setEnabled(false);
        registerButton.setEnabled(false);
    }

    public static void main(String[] args) {
        new ClientApp(false);
        
    }
}