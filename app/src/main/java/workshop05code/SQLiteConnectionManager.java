package workshop05code;

import java.io.FileInputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;

public class SQLiteConnectionManager {

    private static final Logger logger = Logger.getLogger(SQLiteConnectionManager.class.getName());

    static {
        try {
            LogManager.getLogManager().readConfiguration(new FileInputStream("resources/logging.properties"));
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Error in loading logging configuration", e);
        }
    }

    private String databaseURL = "";

    private static final String WORDLE_DROP_TABLE_STRING = "DROP TABLE IF EXISTS wordlist;";
    private static final String WORDLE_CREATE_STRING = "CREATE TABLE wordlist (\n"
            + " id integer PRIMARY KEY,\n"
            + " word text NOT NULL\n"
            + ");";

    private static final String VALID_WORDS_DROP_TABLE_STRING = "DROP TABLE IF EXISTS validWords;";
    private static final String VALID_WORDS_CREATE_STRING = "CREATE TABLE validWords (\n"
            + " id integer PRIMARY KEY,\n"
            + " word text NOT NULL\n"
            + ");";

    public SQLiteConnectionManager(String filename) {
        databaseURL = "jdbc:sqlite:sqlite/" + filename;
    }

    public void createNewDatabase(String fileName) {
        try (Connection conn = DriverManager.getConnection(databaseURL)) {
            if (conn != null) {
                DatabaseMetaData meta = conn.getMetaData();
                logger.info("The driver name is " + meta.getDriverName());
                logger.info("A new database has been created.");
            }
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error in creating new database", e);
        }
    }

    public boolean checkIfConnectionDefined() {
        if (databaseURL.equals("")) {
            return false;
        } else {
            try (Connection conn = DriverManager.getConnection(databaseURL)) {
                return conn != null;
            } catch (SQLException e) {
                logger.log(Level.SEVERE, "Error in checking connection definition", e);
                return false;
            }
        }
    }

    public boolean createWordleTables() {
        if (databaseURL.equals("")) {
            return false;
        } else {
            try (Connection conn = DriverManager.getConnection(databaseURL);
                    Statement stmt = conn.createStatement()) {
                stmt.execute(WORDLE_DROP_TABLE_STRING);
                stmt.execute(WORDLE_CREATE_STRING);
                stmt.execute(VALID_WORDS_DROP_TABLE_STRING);
                stmt.execute(VALID_WORDS_CREATE_STRING);
                return true;
            } catch (SQLException e) {
                logger.log(Level.SEVERE, "Error in creating Wordle tables", e);
                return false;
            }
        }
    }

    public void addValidWord(int id, String word) {
        String sql = "INSERT INTO validWords(id,word) VALUES(?,?)";
        try (Connection conn = DriverManager.getConnection(databaseURL);
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            pstmt.setString(2, word);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error in adding valid word", e);
        }
    }

    public boolean isValidWord(String guess) {
        String sql = "SELECT count(id) as total FROM validWords WHERE word like ?";
        try (Connection conn = DriverManager.getConnection(databaseURL);
                PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, guess);
            ResultSet resultRows = stmt.executeQuery();
            if (resultRows.next()) {
                int result = resultRows.getInt("total");
                return result >= 1;
            }
            return false;
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error in checking valid word", e);
            return false;
        }
    }
}
