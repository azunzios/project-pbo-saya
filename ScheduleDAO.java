import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ScheduleDAO {
    public void addSchedule(Schedule schedule) throws SQLException {
        String sql = "INSERT INTO schedules (pet_id, care_type, schedule_time, days, is_active) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setInt(1, schedule.getPetId());
            stmt.setString(2, schedule.getCareType());
            stmt.setTime(3, schedule.getScheduleTime());
            stmt.setString(4, schedule.getDays());
            stmt.setBoolean(5, schedule.isActive());
            stmt.executeUpdate();

            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    schedule.setId(generatedKeys.getInt(1));
                }
            }
        }
    }

    public List<Schedule> getSchedulesByPetId(int petId) throws SQLException {
        List<Schedule> schedules = new ArrayList<>();
        String sql = "SELECT * FROM schedules WHERE pet_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, petId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Schedule schedule = new Schedule(
                        rs.getInt("pet_id"),
                        rs.getString("care_type"),
                        rs.getTime("schedule_time"),
                        rs.getString("days")
                    );
                    schedule.setId(rs.getInt("id"));
                    schedule.setActive(rs.getBoolean("is_active"));
                    schedules.add(schedule);
                }
            }
        }
        return schedules;
    }

    public void updateSchedule(Schedule schedule) throws SQLException {
        String sql = "UPDATE schedules SET pet_id=?, care_type=?, schedule_time=?, days=?, is_active=? WHERE id=?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, schedule.getPetId());
            stmt.setString(2, schedule.getCareType());
            stmt.setTime(3, schedule.getScheduleTime());
            stmt.setString(4, schedule.getDays());
            stmt.setBoolean(5, schedule.isActive());
            stmt.setInt(6, schedule.getId());
            stmt.executeUpdate();
        }
    }
}
