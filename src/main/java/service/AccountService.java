package service;

import java.sql.Connection;
import java.sql.SQLException;

public class AccountService {
    public static int getAccount(Connection conn, int from) throws SQLException {

        var stmt = conn.prepareStatement("select amount from account where id = ?");
        stmt.setInt(1, from);

        var rs = stmt.executeQuery();

        if (rs.next()) {  // check there is a result
            return rs.getInt("amount");  // can user column index or column name
        }
        return 0;
    }

    public static void updateAccount(Connection conn, int from, int amount) throws SQLException {

        var stmt = conn.prepareStatement("update account set amount = ? where id = ?");
        stmt.setInt(1, amount);
        stmt.setInt(2, from);

        stmt.executeUpdate();
    }
}
