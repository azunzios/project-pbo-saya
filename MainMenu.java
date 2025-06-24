import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.List;
import java.util.Vector;

public class MainMenu extends JFrame {
    private JTable scheduleTable;
    private DefaultTableModel tableModel;
    private ScheduleDAO scheduleDAO;
    private PetDAO petDAO;

    public MainMenu() {
        this.scheduleDAO = new ScheduleDAO();
        this.petDAO = new PetDAO();
        initializeUI();
        loadSchedules();
    }

    private void initializeUI() {
        setTitle("Pet Care Scheduler");
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout(10, 10)); // Add gaps between components

        // Main panel with padding
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
        add(mainPanel);

        JLabel titleLabel = new JLabel("Today's Schedules", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        mainPanel.add(titleLabel, BorderLayout.NORTH);

        // Schedule table
        String[] columnNames = {"ID", "Pet Name", "Care Type", "Time", "Days"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // Make table cells non-editable
            }
        };
        scheduleTable = new JTable(tableModel);
        scheduleTable.setFillsViewportHeight(true);
        scheduleTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        JScrollPane scrollPane = new JScrollPane(scheduleTable);
        mainPanel.add(scrollPane, BorderLayout.CENTER);

        // Button panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        JButton addScheduleButton = new JButton("Add Schedule");
        JButton addPetButton = new JButton("Add Pet");
        JButton viewPetsButton = new JButton("View Pets");
        JButton markDoneButton = new JButton("Mark as Done");
        JButton exportButton = new JButton("Export to CSV");

        buttonPanel.add(addPetButton);
        buttonPanel.add(addScheduleButton);
        buttonPanel.add(viewPetsButton);
        buttonPanel.add(markDoneButton);
        buttonPanel.add(exportButton);
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);

        // Add button listeners
        addPetButton.addActionListener(e -> new AddPetForm(this).setVisible(true));
        addScheduleButton.addActionListener(e -> new AddScheduleForm(this).setVisible(true));
        viewPetsButton.addActionListener(e -> new ViewPetsFrame().setVisible(true));
        markDoneButton.addActionListener(e -> markScheduleDone());
        exportButton.addActionListener(e -> exportSchedulesToCSV());
    }

    public void loadSchedules() {
        try {
            List<Schedule> schedules = scheduleDAO.getAllSchedules(); // Assumes this method exists
            tableModel.setRowCount(0); // Clear existing data
            for (Schedule schedule : schedules) {
                Pet pet = petDAO.getPet(schedule.getPetId());
                String petName = (pet != null) ? pet.getName() : "Unknown Pet";
                Vector<Object> row = new Vector<>();
                row.add(schedule.getId());
                row.add(petName);
                row.add(schedule.getCareType());
                row.add(schedule.getScheduleTime());
                row.add(schedule.getDays());
                tableModel.addRow(row);
            }
        } catch (SQLException e) {
            handleError("Failed to load schedules", e);
        }
    }

    private void markScheduleDone() {
        int selectedRow = scheduleTable.getSelectedRow();
        if (selectedRow != -1) {
            int scheduleId = (int) tableModel.getValueAt(selectedRow, 0);

            try {
                Schedule selected = scheduleDAO.getSchedule(scheduleId);
                if (selected == null) {
                    JOptionPane.showMessageDialog(this, "Schedule not found.", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                // Create a dialog for notes
                JPanel panel = new JPanel(new GridLayout(0, 1));
                JTextField notesField = new JTextField(20);
                panel.add(new JLabel("Notes:"));
                panel.add(notesField);

                int result = JOptionPane.showConfirmDialog(this, panel, "Mark as Done",
                        JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
                if (result == JOptionPane.OK_OPTION) {
                    CareLog log = new CareLog(selected.getPetId(), selected.getCareType(), new Timestamp(System.currentTimeMillis()));
                    log.setScheduleId(selected.getId());
                    log.setNotes(notesField.getText());

                    new CareLogDAO().addCareLog(log);
                    JOptionPane.showMessageDialog(this, "Care logged successfully!");
                }
            } catch (SQLException ex) {
                handleError("Failed to mark schedule as done", ex);
            }

        } else {
            JOptionPane.showMessageDialog(this, "Please select a schedule to mark as done.");
        }
    }

    private void exportSchedulesToCSV() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Save as CSV");
        fileChooser.setSelectedFile(new File("schedules.csv"));

        int userSelection = fileChooser.showSaveDialog(this);
        if (userSelection == JFileChooser.APPROVE_OPTION) {
            File fileToSave = fileChooser.getSelectedFile();
            try (FileWriter writer = new FileWriter(fileToSave)) {
                // Write header
                for (int i = 0; i < tableModel.getColumnCount(); i++) {
                    writer.append(tableModel.getColumnName(i));
                    if (i < tableModel.getColumnCount() - 1) {
                        writer.append(',');
                    }
                }
                writer.append('\n');

                // Write data
                for (int i = 0; i < tableModel.getRowCount(); i++) {
                    for (int j = 0; j < tableModel.getColumnCount(); j++) {
                        writer.append(tableModel.getValueAt(i, j).toString());
                        if (j < tableModel.getColumnCount() - 1) {
                            writer.append(',');
                        }
                    }
                    writer.append('\n');
                }
                JOptionPane.showMessageDialog(this, "Data exported successfully to " + fileToSave.getAbsolutePath());
            } catch (IOException e) {
                handleError("Error exporting data", e);
            }
        }
    }

    private void handleError(String message, Exception e) {
        e.printStackTrace();
        JOptionPane.showMessageDialog(this, message + ": " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new MainMenu().setVisible(true));
    }
}