package Misc;

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
    private static int connections = 0;

    private final String accounts = "accounts";
    // Database credentials are provided via environment variables (DB_USER, DB_PASS)
    // -- connect to the world database
    // -- this is the connector to the database, default port is 3306
    //    ApplicationData and CSC335 are schemas (databases) I created using the MySQL workbench
    private String userdatabaseURL = "jdbc:mysql://localhost:3306/csc335?useSSL=false";
    private String first = "jdbc:mysql://";
    private String second = ":3306/csc335?useSSL=false";
    
    // -- this is the username/password, created during installation and in MySQL Workbench
    //    When you add a user make sure you give them the appropriate Administrative Roles
    //    (DBA sets all which works fine)

    private String user = System.getenv().getOrDefault("DB_USER", "root");
    private String password = System.getenv().getOrDefault("DB_PASS", "");


	public DBaseConnectionP() {
		String sqlcmd; 
		
		// -- connect to the database
		try {
            // -- make the connection to the database
			//    performs functionality of SQL: use CSC335;
			// userConnection = DriverManager.getConnection(userdatabaseURL, user, password);
			userConnection = DriverManager.getConnection(userdatabaseURL, user, password);
            
			// -- These will be used to send queries to the database
            userStatement = userConnection.createStatement();
            
            // -- simple SQL strings as they would be typed into the workbench
            sqlcmd = "SELECT VERSION()";
            resultset = userStatement.executeQuery(sqlcmd);

            if (resultset.next()) {
                System.out.println("MySQL Version: " + resultset.getString(1));
            }
            
            updatelists();

		} catch (SQLException ex) {
			// handle any errors
			System.out.println("SQLException: " + ex.getMessage());
			System.out.println("SQLState: " + ex.getSQLState());
			System.out.println("VendorError: " + ex.getErrorCode());
		}

		
	}

    
    public DBaseConnectionP(String ip) {
		String sqlcmd; 
		
		// -- connect to the database
		try {
            // -- make the connection to the database
			//    performs functionality of SQL: use CSC335;
			userConnection = DriverManager.getConnection(first + ip + second, user, password);
            
			// -- These will be used to send queries to the database
            userStatement = userConnection.createStatement();
            
            // -- simple SQL strings as they would be typed into the workbench
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
			// handle any errors
			System.out.println("SQLException: " + ex.getMessage());
			System.out.println("SQLState: " + ex.getSQLState());
			System.out.println("VendorError: " + ex.getErrorCode());
		}

		
	}

    public int getConnections(){
        return connections;
    }

    public void close() {
        try {
            if (resultset != null && !resultset.isClosed()) {
                resultset.close();
            }
            if (userStatement != null && !userStatement.isClosed()) {
                userStatement.close();
            }
            if (userConnection != null && !userConnection.isClosed()) {
                userConnection.close();
                connections = Math.max(0, connections - 1);
            }
            // remove this client's row from the connections table
            endConnectionToServer();
        } catch (SQLException ex) {
            System.out.println("SQLException during endConnectionToServer: " + ex.getMessage());
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
                        //throw new Exception("Username already exists.");
                    }
                }
            }
            for(String existingUsername : usernames) {
                System.out.println("Existing username: " + existingUsername);
            }
            if (!(PasswordAuthernticator.validPassword(password))) {
                return false;
                //throw new Exception("Password is too weak. It must be at least 8 characters long and include uppercase letters, lowercase letters, digits, and special characters.");
            }

            if (!(AuthenticationService.validateEmailAddress(email))){
                return false;
                //throw new Exception("Email is invalid");
            }
            sqlcmd = "INSERT INTO " + accounts + " (username, password, email, active, lockedout, attempts) VALUES ('" + username + "', '" + password + "', '" + email + "', '0', '0', '0');";

            userStatement.executeUpdate(sqlcmd);
            System.out.println("User registered successfully.");
            updatelists();
            return true;
        } catch (SQLException ex) {
            // handle any errors
            System.out.println("SQLException: " + ex.getMessage());
            System.out.println("SQLState: " + ex.getSQLState());
            System.out.println("VendorError: " + ex.getErrorCode());
            return false;
            //throw new Exception("Registration failed: " + ex.getMessage());
        }
    }

    
    public String[] getUsernames() {
        List<String> usernamesA = new ArrayList<>();
        try {
            userStatement = userConnection.prepareStatement("SELECT username FROM " + accounts + ";");
            resultset = userStatement.executeQuery("SELECT username FROM " + accounts + ";");
            while (resultset.next()) {
                usernamesA.add(resultset.getString("username"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return usernamesA.toArray(new String[0]);
    }
    
    public int getconnections(){
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
            userStatement = userConnection.prepareStatement("SELECT email FROM " + accounts + ";");
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
            userStatement = userConnection.prepareStatement("SELECT password FROM " + accounts + ";");
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
            userStatement = userConnection.prepareStatement("SELECT active FROM " + accounts + ";");
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
            userStatement = userConnection.prepareStatement("SELECT lockedout FROM " + accounts + ";");
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
            userStatement = userConnection.prepareStatement("SELECT attempts FROM " + accounts + ";");
            resultset = userStatement.executeQuery("SELECT attempts FROM " + accounts + ";");
            while (resultset.next()) {
                loginAttemptsL.add(resultset.getInt("attempts"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return loginAttemptsL.stream().mapToInt(i -> i).toArray();
    }

    public String[] getLockedOutUsers(){
        updatelists();
        List<String> lockedUsers = new ArrayList<>();
        if (usernames == null || lockedOutStatus == null) {
            return new String[0];
        }
        int len = Math.min(usernames.length, lockedOutStatus.length);
        for (int i = 0; i < len; i++) {
            if (lockedOutStatus[i] == 1) {
                lockedUsers.add(usernames[i]);
            }
        }
        return lockedUsers.toArray(new String[0]);
    }
    
    public void updatelists() {
        // This method can be used to refresh any cached lists of usernames, passwords, etc.
        // For now, it does nothing as we fetch data directly from the database when needed.
        usernames = getUsernames();
        passwords = getPasswords();
        emails = getEmails();
        activeStatus = getActiveStatus();
        lockedOutStatus = getLockedOutStatus();
        loginAttempts = getLoginAttempts();
    }

    private int getUserIndex(String username) {
        for (int i = 0; i < usernames.length; i++) {
            if (usernames[i].equals(username)) {
                return i;
            }
        }
        return -1; // User not found
    }

    public void sendEmail(String username){
        String userEmail = null;
        int index = getUserIndex(username);
        // Find the email associated with the username
        userEmail = emails[index];
        String pass = passwords[index];
        try {
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
            // updatelists();
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
            // handle any errors
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
            //updatelists();
        } catch (SQLException ex) {
            // handle any errors
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
            //updatelists();
        } catch (SQLException ex) {
            // handle any errors
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
            // updatelists();
        } catch (SQLException ex) {
            // handle any errors
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
            // Reset login attempts on successful login
            resetLoginAttempts(username);
            activateAccount(username);
            updatelists();
            return true;
        } else {
            // Increment login attempts on failed login
            String sqlcmd = "UPDATE " + accounts + " SET attempts = attempts + 1 WHERE username = '" + username + "';";
            try {
                userStatement.executeUpdate(sqlcmd);
                // Check if account should be locked
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
            // updatelists();
        } catch (SQLException ex) {
            // handle any errors
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
            // handle any errors
            System.out.println("SQLException: " + ex.getMessage());
            System.out.println("SQLState: " + ex.getSQLState());
            System.out.println("VendorError: " + ex.getErrorCode());
            return false;
        }
    }

    public String[] getActiveUsers() {
        List<String> activeUsers = new ArrayList<>();
        for (int i = 0; i < usernames.length; i++) {
            if (activeStatus[i] == 1) {
                activeUsers.add(usernames[i]);
            }
        }
        return activeUsers.toArray(new String[0]);
    }

    public boolean isLocked(String username) {
        return AuthenticationService.isLockedOut(username, usernames, lockedOutStatus);
    }

	public void processUserDatabase ()
	{
		String sqlcmd; 
		
		try {
             // -- a query will return a ResultSet
            sqlcmd = "SELECT * FROM " + accounts + ";";
            resultset = userStatement.executeQuery(sqlcmd);
            
            // -- the metadata tells us how many columns in the data
            System.out.println("Table Columns:");
            ResultSetMetaData rsmd = resultset.getMetaData();
            int numberOfColumns = rsmd.getColumnCount();
            for (int i = 1; i <= numberOfColumns; ++i) {
            	System.out.print(rsmd.getColumnLabel(i) + "\t");
            }
            System.out.println();
            
            // -- loop through the ResultSet one row at a time
            //    Note that the ResultSet starts at index 1
            while (resultset.next()) {
            	// -- loop through the columns of the ResultSet
            	for (int i = 1; i <= numberOfColumns; ++i) {
            		System.out.print(resultset.getString(i) + "\t\t");
            	}
            	System.out.println();
            }

            String value = "";
            int primarykey = 1;
            
            // -- insert some records (no resultset is returned on updates)
        	primarykey = 0;
        	value = "zero";
        	sqlcmd = "INSERT INTO " + accounts + " VALUES('" + primarykey + "', '" + value + "', )";

            sqlcmd = "INSERT INTO " + accounts + "VALUES ('', 'test', 'abcd1234', 'test@gmail.com', '0')";
        	userStatement.executeUpdate(sqlcmd);
        	
        	primarykey = 1;
        	value = "one";
        	sqlcmd = "INSERT INTO " + accounts + " VALUES('" + primarykey + "', '" + value + "')";
        	userStatement.executeUpdate(sqlcmd);

        	primarykey = 2;
        	value = "too";
        	sqlcmd = "INSERT INTO " + accounts + " VALUES('" + primarykey + "', '" + value + "')";
        	userStatement.executeUpdate(sqlcmd);

          
            // -- select a specific record
            value = "one";     
            sqlcmd = "SELECT * FROM " + accounts + " WHERE value = '" + value + "'";
            resultset = userStatement.executeQuery(sqlcmd);
            // -- loop through the ResultSet one row at a time
            //    Note that the ResultSet starts at index 1
            while (resultset.next()) {
            	// -- loop through the columns of the ResultSet
            	for (int i = 1; i <= numberOfColumns; ++i) {
            		System.out.print(resultset.getString(i) + "\t\t");
            	}
            	System.out.println();
            }
           
            System.out.println("=====================");
            sqlcmd = "SELECT * FROM " + accounts + ";";
            resultset = userStatement.executeQuery(sqlcmd);
            // -- loop through the ResultSet one row at a time
            //    Note that the ResultSet starts at index 1
            while (resultset.next()) {
            	// -- loop through the columns of the ResultSet
            	for (int i = 1; i < numberOfColumns; ++i) {
            		System.out.print(resultset.getString(i) + "\t\t");
            	}
            	System.out.println(resultset.getString(numberOfColumns));
            }
            
            System.out.println("=====================");

            // -- delete a record
            sqlcmd = "DELETE FROM " + accounts + " WHERE primarykey = " + 0 + ";";
            userStatement.executeUpdate(sqlcmd);

            // -- update a record
            int two = 2;
            String stwo = "two";
            sqlcmd = "UPDATE accounts SET value = '" + stwo +  "' WHERE (PrimaryKey = '" + two + "')";
            System.out.println(sqlcmd);
            userStatement.executeUpdate(sqlcmd);
            
            sqlcmd = "SELECT * FROM " + accounts + ";";
            resultset = userStatement.executeQuery(sqlcmd);
            // -- loop through the ResultSet one row at a time
            //    Note that the ResultSet starts at index 1
            while (resultset.next()) {
            	// -- loop through the columns of the ResultSet
            	for (int i = 1; i < numberOfColumns; ++i) {
            		System.out.print(resultset.getString(i) + "\t\t");
            	}
            	System.out.println(resultset.getString(numberOfColumns));
            }
            
		} catch (SQLException ex) {
			// handle any errors
			System.out.println("SQLException: " + ex.getMessage());
			System.out.println("SQLState: " + ex.getSQLState());
			System.out.println("VendorError: " + ex.getErrorCode());
		}
		

	}
	
	// public static void main(String[] args) {

	// 	DBaseConnectionP dbc = new DBaseConnectionP();
		
	// 	System.out.println("\nTestSchema");
	// 	dbc.processUserDatabase();
	// }

    public void endConnectionToServer() {
        try {
            if (resultset != null && !resultset.isClosed()) {
                resultset.close();
            }
            if (userStatement != null && !userStatement.isClosed()) {
                userStatement.close();
            }
            if (userConnection != null && !userConnection.isClosed()) {
                userConnection.close();
                connections = Math.max(0, connections - 1);
            }
            //try {
                // reopen a short-lived connection to remove this client's row from the connections table
                // Connection tmpConn = DriverManager.getConnection(userdatabaseURL, user, password);
                // Statement tmpStmt = tmpConn.createStatement();
                
                String deleteSql = "DELETE FROM connections WHERE `user` = '" + user + "';";
                // int removed = tmpStmt.executeUpdate(deleteSql);
                // System.out.println("Removed " + removed + " row(s) from connections table for user '" + user + "'.");
                // try { if (tmpStmt != null && !tmpStmt.isClosed()) tmpStmt.close(); } catch (SQLException ignore) {}
                // try { if (tmpConn != null && !tmpConn.isClosed()) tmpConn.close(); } catch (SQLException ignore) {}

        } catch (SQLException ex) {
            System.out.println("SQLException during endConnectionToServer: " + ex.getMessage());
        } finally {
            resultset = null;
            userStatement = null;
            userConnection = null;
        }
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

    public void removeConnection(){
        Connection tmpConn = null;
                Statement tmpStmt = null;
                ResultSet tmpRs = null;
                try {
                    tmpConn = DriverManager.getConnection(userdatabaseURL, user, password);
                    tmpStmt = tmpConn.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
                    tmpRs = tmpStmt.executeQuery("SELECT * FROM connections");
                    if (tmpRs.last()) {
                        int lastRow = tmpRs.getRow();
                        //String deleteSql = "DELETE FROM connections WHERE `user` = '" + user + "';";
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
                        System.out.println("Last row index in connections table: " + lastRow);
                        // Example: read the first column of the last row (adjust column label/index as needed)
                        try {
                            System.out.println("Last row first column: " + tmpRs.getString(1));
                        } catch (SQLException ignore) {}
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
