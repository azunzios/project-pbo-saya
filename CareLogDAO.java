import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CareLogDAO {
    public void addCareLog(CareLog careLog) throws SQLException {
        String sql = "INSERT INTO care_logs (pet_id, schedule_id, care_type, completed_at, notes, done_by) VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setInt(1, careLog.getPetId());
            if (careLog.getScheduleId() == null) {
                stmt.setNull(2, Types.INTEGER);
            } else {
                stmt.setInt(2, careLog.getScheduleId());
            }
            stmt.setString(3, careLog.getCareType());
            stmt.setTimestamp(4, careLog.getCompletedAt());
            stmt.setString(5, careLog.getNotes());
            stmt.setString(6, careLog.getDoneBy());
            stmt.executeUpdate();

            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    careLog.setId(generatedKeys.getInt(1));
                }
            }
        }
    }

    public List<CareLog> getCareLogsByPetId(int petId) throws SQLException {
        List<CareLog> logs = new ArrayList<>();
        String sql = "SELECT * FROM care_logs WHERE pet_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, petId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    CareLog log = new CareLog(
                        rs.getInt("pet_id"),
                        rs.getString("care_type"),
                        rs.getTimestamp("completed_at")
                    );
                    log.setId(rs.getInt("id"));
                    int scheduleId = rs.getInt("schedule_id");
                    if (!rs.wasNull()) {
                        log.setScheduleId(scheduleId);
                    }
                    log.setNotes(rs.getString("notes"));
                    log.setDoneBy(rs.getString("done_by"));
                    logs.add(log);
                }
            }
        }
        return logs;
    }
}
