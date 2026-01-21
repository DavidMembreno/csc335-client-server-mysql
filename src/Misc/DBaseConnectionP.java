package Misc;
// NOTE:
// This database layer was developed for a course project.
// Passwords are stored in plaintext and email recovery sends the current password.
// This is for demonstration purposes only and not production-safe.

// -- download MySQL from: http://dev.mysql.com/downloads/
//    Community Server version
// -- Installation instructions are here: http://dev.mysql.com/doc/refman/5.7/en/installing.html
// -- open MySQL Workbench to see the contents of the database
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import server.SendEmailUsingGMailSMTP;

// -- MAKE SURE THE JDBC CONNECTOR JAR IS IN THE BUILD PATH
//    workspace -> properties -> Java Build Path -> Libraries -> Add External JARs...

// https://docs.oracle.com/javase/tutorial/jdbc/basics/prepared.html for example SQL statements
public class DBaseConnectionP {

	// -- objects to be used for user database access
    private Connection userConnection = null;
    private Statement userStatement = null;
    private ResultSet resultset = null;

    private String[] usernames;
    private String[] passwords;
    private String[] emails;
    private int[] activeStatus;
    private int[] lockedOutStatus;
    private int[] loginAttempts;

    private final String accounts = "accounts";

    // Database credentials are provided via environment variables (DB_USER, DB_PASS)
    private String userdatabaseURL = "jdbc:mysql://localhost:3306/csc335?useSSL=false";
    private String first = "jdbc:mysql://";
    private String second = ":3306/csc335?useSSL=false";

    private String user = System.getenv().getOrDefault("DB_USER", "root");
    private String password = System.getenv().getOrDefault("DB_PASS", "");

	public DBaseConnectionP() {
		String sqlcmd;

		// -- connect to the database
		try {
            userConnection = DriverManager.getConnection(userdatabaseURL, user, password);
            userStatement = userConnection.createStatement();

            sqlcmd = "SELECT VERSION()";
            resultset = userStatement.executeQuery(sqlcmd);

            if (resultset.next()) {
                System.out.println("MySQL Version: " + resultset.getString(1));
            }

            updatelists();

		} catch (SQLException ex) {
			System.out.println("SQLException: " + ex.getMessage());
			System.out.println("SQLState: " + ex.getSQLState());
			System.out.println("VendorError: " + ex.getErrorCode());
		}
	}

    public DBaseConnectionP(String ip) {
		String sqlcmd;

		// -- connect to the database
		try {
            userConnection = DriverManager.getConnection(first + ip + second, user, password);
            userStatement = userConnection.createStatement();

            sqlcmd = "SELECT VERSION()";
            resultset = userStatement.executeQuery(sqlcmd);

            if (resultset.next()) {
                System.out.println("MySQL Version: " + resultset.getString(1));
            }

            try {
                sqlcmd = "INSERT INTO connections () VALUES ();";
                userStatement.executeUpdate(sqlcmd);
            } catch (SQLException ex) {
                System.out.println("SQLException while altering connections table: " + ex.getMessage());
            }

            updatelists();

		} catch (SQLException ex) {
			System.out.println("SQLException: " + ex.getMessage());
			System.out.println("SQLState: " + ex.getSQLState());
			System.out.println("VendorError: " + ex.getErrorCode());
		}
	}

    public void close() {
        try {
            if (resultset != null && !resultset.isClosed()) resultset.close();
            if (userStatement != null && !userStatement.isClosed()) userStatement.close();
            if (userConnection != null && !userConnection.isClosed()) userConnection.close();

            // remove this client's row from the connections table
            endConnectionToServer();
        } catch (SQLException ex) {
            System.out.println("SQLException during close/endConnectionToServer: " + ex.getMessage());
        } finally {
            resultset = null;
            userStatement = null;
            userConnection = null;
        }
    }

    public boolean Register(String username, String password, String email) throws Exception {
        String sqlcmd;
        try {
            // Check if username already exists
            if (usernames != null) {
                for (String existingUsername : usernames) {
                    if (existingUsername.equals(username)) {
                        return false;
                    }
                }
            }

            if (!(PasswordAuthernticator.validPassword(password))) {
                return false;
            }

            if (!(AuthenticationService.validateEmailAddress(email))) {
                return false;
            }

            sqlcmd = "INSERT INTO " + accounts + " (username, password, email, active, lockedout, attempts) " +
                     "VALUES ('" + username + "', '" + password + "', '" + email + "', '0', '0', '0');";

            userStatement.executeUpdate(sqlcmd);
            System.out.println("User registered successfully.");
            updatelists();
            return true;
        } catch (SQLException ex) {
            System.out.println("SQLException: " + ex.getMessage());
            System.out.println("SQLState: " + ex.getSQLState());
            System.out.println("VendorError: " + ex.getErrorCode());
            return false;
        }
    }

    public String[] getUsernames() {
        List<String> usernamesA = new ArrayList<>();
        try {
            resultset = userStatement.executeQuery("SELECT username FROM " + accounts + ";");
            while (resultset.next()) {
                usernamesA.add(resultset.getString("username"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return usernamesA.toArray(new String[0]);
    }

    public int getconnections() {
        String sql = "SELECT COUNT(*) AS cnt FROM connections;";
        try {
            resultset = userStatement.executeQuery(sql);
            if (resultset.next()) {
                return resultset.getInt("cnt");
            }
        } catch (SQLException ex) {
            System.out.println("SQLException: " + ex.getMessage());
        }
        return 0;
    }

    private String[] getEmails() {
        List<String> emailsL = new ArrayList<>();
        try {
            resultset = userStatement.executeQuery("SELECT email FROM " + accounts + ";");
            while (resultset.next()) {
                emailsL.add(resultset.getString("email"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return emailsL.toArray(new String[0]);
    }

    private String[] getPasswords() {
        List<String> passwordsL = new ArrayList<>();
        try {
            resultset = userStatement.executeQuery("SELECT password FROM " + accounts + ";");
            while (resultset.next()) {
                passwordsL.add(resultset.getString("password"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return passwordsL.toArray(new String[0]);
    }

    private int[] getActiveStatus() {
        List<Integer> activeStatusL = new ArrayList<>();
        try {
            resultset = userStatement.executeQuery("SELECT active FROM " + accounts + ";");
            while (resultset.next()) {
                activeStatusL.add(resultset.getInt("active"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return activeStatusL.stream().mapToInt(i -> i).toArray();
    }

    private int[] getLockedOutStatus() {
        List<Integer> lockedOutStatusL = new ArrayList<>();
        try {
            resultset = userStatement.executeQuery("SELECT lockedout FROM " + accounts + ";");
            while (resultset.next()) {
                lockedOutStatusL.add(resultset.getInt("lockedout"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return lockedOutStatusL.stream().mapToInt(i -> i).toArray();
    }

    private int[] getLoginAttempts() {
        List<Integer> loginAttemptsL = new ArrayList<>();
        try {
            resultset = userStatement.executeQuery("SELECT attempts FROM " + accounts + ";");
            while (resultset.next()) {
                loginAttemptsL.add(resultset.getInt("attempts"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return loginAttemptsL.stream().mapToInt(i -> i).toArray();
    }

    public String[] getLockedOutUsers() {
        updatelists();
        List<String> lockedUsers = new ArrayList<>();
        if (usernames == null || lockedOutStatus == null) return new String[0];

        int len = Math.min(usernames.length, lockedOutStatus.length);
        for (int i = 0; i < len; i++) {
            if (lockedOutStatus[i] == 1) lockedUsers.add(usernames[i]);
        }
        return lockedUsers.toArray(new String[0]);
    }

    public void updatelists() {
        usernames = getUsernames();
        passwords = getPasswords();
        emails = getEmails();
        activeStatus = getActiveStatus();
        lockedOutStatus = getLockedOutStatus();
        loginAttempts = getLoginAttempts();
    }

    private int getUserIndex(String username) {
        if (usernames == null) return -1;
        for (int i = 0; i < usernames.length; i++) {
            if (usernames[i].equals(username)) return i;
        }
        return -1;
    }

    public void sendEmail(String username) {
        try {
            int index = getUserIndex(username);
            if (index < 0) throw new RuntimeException("Username not found!");

            String userEmail = emails[index];
            String pass = passwords[index];

            resetLoginAttempts(username);
            unlockAccount(username);
            updatelists();
            SendEmailUsingGMailSMTP.sendMail(userEmail, pass);
        } catch (Exception e) {
            System.out.println("Username not found!");
        }
    }

    private void resetLoginAttempts(String username) {
        String sqlcmd = "UPDATE " + accounts + " SET attempts = 0 WHERE username = '" + username + "';";
        try {
            userStatement.executeUpdate(sqlcmd);
        } catch (SQLException ex) {
            System.out.println("SQLException: " + ex.getMessage());
        }
    }

    public boolean accountStatus(String username) {
        String sqlcmd;
        try {
            sqlcmd = "SELECT active FROM " + accounts + " WHERE username = '" + username + "';";
            resultset = userStatement.executeQuery(sqlcmd);
            if (resultset.next()) {
                int active = resultset.getInt("active");
                return active == 1;
            }
        } catch (SQLException ex) {
            System.out.println("SQLException: " + ex.getMessage());
            System.out.println("SQLState: " + ex.getSQLState());
            System.out.println("VendorError: " + ex.getErrorCode());
        }
        return false;
    }

    private void activateAccount(String username) {
        String sqlcmd;
        try {
            sqlcmd = "UPDATE " + accounts + " SET active = 1 WHERE username = '" + username + "';";
            userStatement.executeUpdate(sqlcmd);
            System.out.println("Account activated successfully.");
        } catch (SQLException ex) {
            System.out.println("SQLException: " + ex.getMessage());
            System.out.println("SQLState: " + ex.getSQLState());
            System.out.println("VendorError: " + ex.getErrorCode());
        }
    }

    private void deactivateAccount(String username) {
        String sqlcmd;
        try {
            sqlcmd = "UPDATE " + accounts + " SET active = 0 WHERE username = '" + username + "';";
            userStatement.executeUpdate(sqlcmd);
            System.out.println("Account deactivated successfully.");
        } catch (SQLException ex) {
            System.out.println("SQLException: " + ex.getMessage());
            System.out.println("SQLState: " + ex.getSQLState());
            System.out.println("VendorError: " + ex.getErrorCode());
        }
    }

    public void lockAccount(String username) {
        String sqlcmd;
        try {
            sqlcmd = "UPDATE " + accounts + " SET lockedout = 1 WHERE username = '" + username + "';";
            userStatement.executeUpdate(sqlcmd);
            System.out.println("Account locked successfully.");
        } catch (SQLException ex) {
            System.out.println("SQLException: " + ex.getMessage());
            System.out.println("SQLState: " + ex.getSQLState());
            System.out.println("VendorError: " + ex.getErrorCode());
        }
    }

    public boolean login(String username, String password) {
        boolean successfulLogin = AuthenticationService.authenticate(username, password, usernames, passwords);
        if (successfulLogin) {
            if (isLocked(username)) {
                System.out.println("Account is locked out.");
                return false;
            }
            resetLoginAttempts(username);
            activateAccount(username);
            updatelists();
            return true;
        } else {
            String sqlcmd = "UPDATE " + accounts + " SET attempts = attempts + 1 WHERE username = '" + username + "';";
            try {
                userStatement.executeUpdate(sqlcmd);

                int userIndex = getUserIndex(username);
                if (userIndex != -1 && loginAttempts[userIndex] >= 3) {
                    lockAccount(username);
                    System.out.println("Account locked due to multiple failed login attempts.");
                }

                updatelists();
            } catch (SQLException ex) {
                System.out.println("SQLException: " + ex.getMessage());
            }
            return false;
        }
    }

    public void logout(String username) {
        deactivateAccount(username);
        updatelists();
    }

    private void unlockAccount(String username) {
        String sqlcmd;
        try {
            sqlcmd = "UPDATE " + accounts + " SET lockedout = 0 WHERE username = '" + username + "';";
            userStatement.executeUpdate(sqlcmd);
            System.out.println("Account unlocked successfully.");
        } catch (SQLException ex) {
            System.out.println("SQLException: " + ex.getMessage());
            System.out.println("SQLState: " + ex.getSQLState());
            System.out.println("VendorError: " + ex.getErrorCode());
        }
    }

    public boolean changePassword(String username, String newPassword) {
        String sqlcmd;
        try {
            if (!(PasswordAuthernticator.validPassword(newPassword))) {
                System.out.println("Password is too weak. It must be at least 8 characters long and include uppercase letters, lowercase letters, digits, and special characters.");
                return false;
            }
            sqlcmd = "UPDATE " + accounts + " SET password = '" + newPassword + "' WHERE username = '" + username + "';";
            userStatement.executeUpdate(sqlcmd);
            System.out.println("Password changed successfully.");
            updatelists();
            return true;
        } catch (SQLException ex) {
            System.out.println("SQLException: " + ex.getMessage());
            System.out.println("SQLState: " + ex.getSQLState());
            System.out.println("VendorError: " + ex.getErrorCode());
            return false;
        }
    }

    public String[] getActiveUsers() {
        List<String> activeUsers = new ArrayList<>();
        if (usernames == null || activeStatus == null) return new String[0];

        int len = Math.min(usernames.length, activeStatus.length);
        for (int i = 0; i < len; i++) {
            if (activeStatus[i] == 1) activeUsers.add(usernames[i]);
        }
        return activeUsers.toArray(new String[0]);
    }

    public boolean isLocked(String username) {
        return AuthenticationService.isLockedOut(username, usernames, lockedOutStatus);
    }

    public void endConnectionToServer() {
        // Best-effort cleanup for the connections table (if used in your schema)
        removeConnection();
    }

    public int getSchemaConnections() {
        String sql = "SELECT COUNT(*) AS conn_count FROM information_schema.processlist WHERE db = 'csc335';";
        try {
            resultset = userStatement.executeQuery(sql);
            if (resultset.next()) {
                return resultset.getInt("conn_count");
            }
        } catch (SQLException ex) {
            System.out.println("SQLException: " + ex.getMessage());
        }
        return 0;
    }

    public void removeConnection() {
        Connection tmpConn = null;
        Statement tmpStmt = null;
        ResultSet tmpRs = null;

        try {
            tmpConn = DriverManager.getConnection(userdatabaseURL, user, password);
            tmpStmt = tmpConn.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
            tmpRs = tmpStmt.executeQuery("SELECT * FROM connections");

            if (tmpRs.last()) {
                try {
                    ResultSetMetaData md = tmpRs.getMetaData();
                    String colLabel = md.getColumnLabel(1);
                    String lastVal = tmpRs.getString(1);

                    String deleteSql = "DELETE FROM connections WHERE `" + colLabel + "` = ? LIMIT 1";
                    try (java.sql.PreparedStatement delStmt = tmpConn.prepareStatement(deleteSql)) {
                        delStmt.setString(1, lastVal);
                        int removed = delStmt.executeUpdate();
                        System.out.println("Removed " + removed + " row(s) from connections table for " + colLabel + " = " + lastVal);
                    }
                } catch (SQLException e) {
                    System.out.println("SQLException while removing last row: " + e.getMessage());
                }
            } else {
                System.out.println("Connections table is empty.");
            }
        } catch (SQLException e) {
            System.out.println("SQLException in removeConnection: " + e.getMessage());
        } finally {
            try { if (tmpRs != null && !tmpRs.isClosed()) tmpRs.close(); } catch (SQLException ignore) {}
            try { if (tmpStmt != null && !tmpStmt.isClosed()) tmpStmt.close(); } catch (SQLException ignore) {}
            try { if (tmpConn != null && !tmpConn.isClosed()) tmpConn.close(); } catch (SQLException ignore) {}
        }
    }
}
