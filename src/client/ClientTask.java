package client;

import Misc.*;
import javax.swing.*;

class ClientTask implements Runnable {
    private final String action;
    private final String username;
    private final String password;
    private final String email;
    private final JFrame parent;
    private final DBaseConnectionP dbc;

    public ClientTask(String action, String username, String password, String email,
                      JFrame parent, DBaseConnectionP dbc) {
        this.action = action;
        this.username = username;
        this.password = password;
        this.email = email;
        this.parent = parent;
        this.dbc = dbc;
    }

    @Override
    public void run() {
        try {
            switch (action) {
                case "login":
                    boolean auth = dbc.login(username, password);
                    SwingUtilities.invokeLater(() -> {
                        if (auth) {
                            //JOptionPane.showMessageDialog(parent, "Login successful!");
                            parent.dispose();
                            new ClientApp(username);
                        } else {
                            if (dbc.isLocked(username)) {
                                JOptionPane.showMessageDialog(parent,
                                        "Account is locked due to multiple failed login attempts.");
                            } else {
                                JOptionPane.showMessageDialog(parent,
                                        "Login failed! Please check your credentials.");
                            }
                        }
                    });
                    //dbc.endConnectionToServer();
                    break;

                case "register":
                    boolean registered = dbc.Register(username, password, email);
                    SwingUtilities.invokeLater(() -> {
                        if (registered) {
                            JOptionPane.showMessageDialog(parent, "Registration successful!");
                        } else {
                            JOptionPane.showMessageDialog(parent, "Registration unsuccessful!");
                        }
                    });
                    //dbc.endConnectionToServer();
                    break;

                case "recover":
                    dbc.sendEmail(username);
                    SwingUtilities.invokeLater(() ->
                            JOptionPane.showMessageDialog(parent, "Recovery email sent!")
                    );
                    //dbc.endConnectionToServer();
                    break;

                case "changePassword":
                    boolean changed = dbc.changePassword(username, password);
                    SwingUtilities.invokeLater(() -> {
                        if (changed) {
                            JOptionPane.showMessageDialog(parent, "Password changed successfully.");
                        } else {
                            JOptionPane.showMessageDialog(parent, "Password change failed.");
                        }
                    });
                    //dbc.endConnectionToServer();
                    break;

                case "logout":
                    dbc.logout(username);
                    SwingUtilities.invokeLater(() -> {
                        //JOptionPane.showMessageDialog(parent, "Logged out.");
                        parent.dispose();
                        new ClientApp(true);
                    });
                    //dbc.endConnectionToServer();
                    break;
                case "shutdown":
                    dbc.logout(username);
                    SwingUtilities.invokeLater(() -> {
                        //JOptionPane.showMessageDialog(parent, "Logged out.");
                        parent.dispose();
                        System.exit(0);
                    });
                    dbc.endConnectionToServer();
                    break;
    
                default:
                    System.out.println("Unknown action: " + action);
            }
        } catch (Exception e) {
            SwingUtilities.invokeLater(() ->
                    JOptionPane.showMessageDialog(parent, "Error: " + e.getMessage())
            );
        }
    }
}