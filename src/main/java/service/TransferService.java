package service;

import connection.ConnectionManager;
import domain.TransferLog;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Savepoint;
import java.sql.Statement;

public class TransferService {

    public TransferLog transferWithOffAutoCommit(int from, int to, int amount) {

        try(var conn = ConnectionManager.getConnection()) {

            try{
                conn.setAutoCommit(false);

                // get amount from account
                var fromAccount = AccountService.getAccount(conn, from);

                // get amount from to account
                var toAccount = AccountService.getAccount(conn, to);

                createTransferLog(conn, from, to, amount, fromAccount, toAccount, "START");

                // check if amount is enough
                if (fromAccount < amount) {
                    createTransferLog(conn, from, to, amount, fromAccount, toAccount, "FAIL");
                    throw new RuntimeException("Not enough money");
                }

                // update from account
                AccountService.updateAccount(conn, from, fromAccount - amount);

                // update to account
                AccountService.updateAccount(conn, to, toAccount + amount);

                // insert transfer log
                var logId = createTransferLog(conn, from, to, amount, fromAccount, toAccount, "SUCCESS");
                conn.commit();

                // select transfer log
                return getTransferLog(conn, logId);
            } catch (SQLException e) {
                conn.rollback();
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null;
    }

    public TransferLog transferWithSavePoint(int from, int to, int amount) {

        Savepoint savepoint = null;

        try(var conn = ConnectionManager.getConnection()) {

            try{
                conn.setAutoCommit(false);
                // get amount from account
                var fromAccount = AccountService.getAccount(conn, from);

                // get amount from to account
                var toAccount = AccountService.getAccount(conn, to);

                createTransferLog(conn, from, to, amount, fromAccount, toAccount, "START");
                savepoint = conn.setSavepoint("Start");

                // check if amount is enough
                if (fromAccount < amount) {
                    createTransferLog(conn, from, to, amount, fromAccount, toAccount, "FAIL");
                    savepoint = conn.setSavepoint("Fail");
                    throw new RuntimeException("Not enough money");
                }

                // update from account
                AccountService.updateAccount(conn, from, fromAccount - amount);

                // update to account
                AccountService.updateAccount(conn, to, toAccount + amount);

                // insert transfer log
                var logId = createTransferLog(conn, from, to, amount, fromAccount, toAccount, "SUCCESS");
                savepoint = conn.setSavepoint("Success");

                conn.commit();

                // select transfer log
                return getTransferLog(conn, logId);
            } catch (Exception e) {
                if (savepoint != null) {
                    conn.commit();
                    conn.rollback(savepoint);
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null;
    }

    private TransferLog getTransferLog(Connection conn, int logId) throws SQLException {
        var stmt = conn.prepareStatement("""
                select * from transfer_log t 
                join account fa on t.from_account = fa.id 
                join account ta on t.to_account = ta.id 
                where t.id = ?
                """);
        stmt.setInt(1, logId);

        var rs = stmt.executeQuery();

        while (rs.next()) {
            return new TransferLog(
                    rs.getInt("id"),
                    rs.getString("fa.name"),
                    rs.getString("ta.name"),
                    rs.getInt("amount"),
                    rs.getTimestamp("transfer_time").toLocalDateTime().toLocalDate(),
                    rs.getInt("from_amount"),
                    rs.getInt("to_amount"),
                    rs.getString("status")
            );
        }
        return null;
    }

    private int createTransferLog(Connection conn, int from, int to, int amount, int fromAccount, int toAccount, String status) throws SQLException {
        var stmt = conn.prepareStatement(
                "insert into transfer_log (from_account, to_account, amount, from_amount, to_amount, status) values (?, ?, ?, ?, ?, ?)"
                    , Statement.RETURN_GENERATED_KEYS);
        stmt.setInt(1, from);
        stmt.setInt(2, to);
        stmt.setInt(3, amount);
        stmt.setInt(4, fromAccount);
        stmt.setInt(5, toAccount);
        stmt.setString(6, status);

        stmt.executeUpdate();

        var rs = stmt.getGeneratedKeys();

        if (rs.next()) {
            return rs.getInt(1);
        }

        return 0;
    }
}
