package db;

import java.sql.*;

import db.OperationState.State;

public class UserManager {
    public enum UserPermission {
        admin,
        visitor
    };

    Connection myConnection;

    //TODO: Prove User Table
    UserManager(Connection dbConnection) {
        myConnection = dbConnection;
    }
    OperationState register(String userName, String passwd, UserPermission permission) {
        int tmp = 0;
        if (permission == UserPermission.admin) {
            tmp = 1;
        }
        String query = String.format("INSERT INTO User (UserName, Passwd, Permission) VALUES (?, ?, ?)");
        Long userKey = -1L;

        try {
            PreparedStatement stmt = myConnection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS);
            stmt.setString(1, userName);
            stmt.setString(2, passwd);
            stmt.setInt(3, tmp);
            stmt.executeUpdate(query);
            
            ResultSet userKeys = stmt.getGeneratedKeys();
            while (userKeys.next()) {
                userKey = userKeys.getLong(1);
            }

            stmt.close();
        } catch (Exception e) {
            System.err.println( e.getClass().getName() + ": " + e.getMessage() );
            System.exit(0);
        }

        if (userKey == -1L) {
            return new OperationState(State.error, "DB can't add user", "DB can't add user");
        }
        return new OperationState(State.normal, String.format("Success create user %s with ID %d", userName, userKey), "Success");
    }

    OperationState login(String userName, String passwd) {
        Boolean flag = false;
        String query = String.format("SELECT * FROM User WHERE UserName = ? and Passwd = ?;");

        try {
            PreparedStatement stmt = myConnection.prepareStatement(query);
            stmt.setString(1, userName);
            stmt.setString(2, passwd);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                flag = true;
            }

            stmt.close();
        } catch (Exception e) {
            System.err.println( e.getClass().getName() + ": " + e.getMessage() );
            System.exit(0);
        }

        if (flag) {
            return new OperationState(State.normal, String.format("User %s login success", userName), "Success");
        } else {
            return new OperationState(State.normal, String.format("User %s login failed", userName), "Login Failed");
        }
    }

    OperationState changeName(String oldName, String newName) {
        Integer affectLine = 0;
        String query = "UPDATE User SET UserName = ? WHERE UserName = ?";
        
        try {
            PreparedStatement stmt = myConnection.prepareStatement(query);
            stmt.setString(1, newName);
            stmt.setString(2, oldName);
            affectLine = stmt.executeUpdate();
            stmt.close();
        } catch (Exception e) {
            System.err.println( e.getClass().getName() + ": " + e.getMessage() );
            System.exit(0);
        }

        if (affectLine > 0) {
            return new OperationState(State.normal, String.format("User %s rename to %s success, affecting %d User", oldName, newName, affectLine), "Success");
        } else {
            return new OperationState(State.normal, String.format("User %s rename failed", oldName), "Rename Failed");
        }
    }

    OperationState changePasswd(String userName, String newPasswd) {
        Integer affectLine = 0;
        String query = "UPDATE User SET Passwd = ? WHERE UserName = ?";
        
        try {
            PreparedStatement stmt = myConnection.prepareStatement(query);
            stmt.setString(1, newPasswd);
            stmt.setString(2, userName);
            affectLine = stmt.executeUpdate();
            stmt.close();
        } catch (Exception e) {
            System.err.println( e.getClass().getName() + ": " + e.getMessage() );
            System.exit(0);
        }

        if (affectLine > 0) {
            return new OperationState(State.normal, String.format("User %s's passwd change success, affecting %d User", userName, affectLine), "Success");
        } else {
            return new OperationState(State.normal, String.format("User %s rename failed", userName), "Change Passwd Failed");
        }
    }
}
