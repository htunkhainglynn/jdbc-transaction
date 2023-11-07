import connection.ConnectionManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import service.TransferService;

import java.sql.SQLException;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;

public class TransferTest {

    TransferService transferService;

    @BeforeEach
    void setUp() {
        transferService = new TransferService();

        // truncate table
        try (var conn = ConnectionManager.getConnection()) {
            conn.prepareStatement("set foreign_key_checks = 0").execute();

            conn.prepareStatement("truncate table transfer_log").execute();
            conn.prepareStatement("truncate table account").execute();

            conn.prepareStatement("insert into account (name, amount) values ('Aung Aung', 200000)").execute();
            conn.prepareStatement("insert into account (name, amount) values ('Thida', 200000)").execute();

            conn.prepareStatement("set foreign_key_checks = 1").execute();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Test
    void test_transfer_with_off_auto_commit() {
        var transferLog = transferService.transferWithOffAutoCommit(1, 2, 50000);
        assertNotNull(transferLog);

        assertEquals(1, transferLog.id());
        assertEquals("Aung Aung", transferLog.fromAccount());
        assertEquals("Thida", transferLog.toAccount());
        assertEquals(50000, transferLog.amount());
        assertEquals(200000, transferLog.fromAmount());
        assertEquals(200000, transferLog.toAmount());
    }

    @Test
    void test_transfer_with_save_point() {
        var transferLog = transferService.transferWithSavePoint(1, 2, 50000);
        assertNotNull(transferLog);

//        assertEquals(1, transferLog.id());
        assertEquals("Aung Aung", transferLog.fromAccount());
        assertEquals("Thida", transferLog.toAccount());
        assertEquals(50000, transferLog.amount());
        assertEquals(200000, transferLog.fromAmount());
        assertEquals(200000, transferLog.toAmount());
    }
}
