import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.SQLException;
import java.sql.Time;
import java.sql.Timestamp;

public class MainMenu extends JFrame {
    private JLabel countdownLabel;
    private JList<Schedule> scheduleList;
    private DefaultListModel<Schedule> listModel;

    public MainMenu() {
        initializeUI();
    }

    private void initializeUI() {
        setTitle("Pet Care Scheduler");
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        // Create components
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        countdownLabel = new JLabel("Next schedule in: --:--:--");
        topPanel.add(countdownLabel);

        // Schedule list
        listModel = new DefaultListModel<>();
        scheduleList = new JList<>(listModel);
        scheduleList.setCellRenderer(new ScheduleListRenderer());
        JScrollPane scrollPane = new JScrollPane(scheduleList);

        // Buttons
        JButton addScheduleButton = new JButton("Add Schedule");
        JButton addPetButton = new JButton("Add Pet");
        JButton markDoneButton = new JButton("Mark as Done");
        JButton viewHistoryButton = new JButton("View History");

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        buttonPanel.add(addScheduleButton);
        buttonPanel.add(addPetButton);
        buttonPanel.add(markDoneButton);
        buttonPanel.add(viewHistoryButton);

        // Add components to frame
        add(topPanel, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);

        // Load schedules
        loadSchedules();

        // Add button listeners
        addScheduleButton.addActionListener(e -> {
            new AddScheduleForm(this).setVisible(true);
        });

        addPetButton.addActionListener(e -> {
            new AddPetForm(this).setVisible(true);
        });

        markDoneButton.addActionListener(e -> {
            markScheduleDone();
        });

        viewHistoryButton.addActionListener(e -> {
            new ViewHistory(this).setVisible(true);
        });
    }

    internal void loadSchedules() {
        // Placeholder: Load schedules from database
        // For now, we'll add dummy data
        listModel.clear();
        listModel.addElement(new Schedule(1, "Feeding", Time.valueOf("08:00:00"), "Mon,Tue,Wed,Thu,Fri"));
        listModel.addElement(new Schedule(2, "Grooming", Time.valueOf("10:00:00"), "Sat"));
    }

    private void markScheduleDone() {
        Schedule selected = scheduleList.getSelectedValue();
        if (selected != null) {
            // Open a dialog to add notes and done by
            JTextField notesField = new JTextField(20);
            JTextField doneByField = new JTextField(20);

            JPanel panel = new JPanel(new GridLayout(0, 1));
            panel.add(new JLabel("Notes:"));
            panel.add(notesField);
            panel.add(new JLabel("Done by:"));
            panel.add(doneByField);

            int result = JOptionPane.showConfirmDialog(this, panel, "Mark as Done", 
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
            if (result == JOptionPane.OK_OPTION) {
                // Create a CareLog entry
                CareLog log = new CareLog(selected.getPetId(), selected.getCareType(), new Timestamp(System.currentTimeMillis()));
                log.setScheduleId(selected.getId());
                log.setNotes(notesField.getText());
                log.setDoneBy(doneByField.getText());
                
                // Save to database
                try {
                    new CareLogDAO().addCareLog(log);
                    JOptionPane.showMessageDialog(this, "Logged successfully!");
                } catch (SQLException ex) {
                    ex.printStackTrace();
                    JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        } else {
            JOptionPane.showMessageDialog(this, "Please select a schedule!");
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new MainMenu().setVisible(true);
        });
    }
}

class ScheduleListRenderer extends DefaultListCellRenderer {
    @Override
    public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
        if (value instanceof Schedule) {
            Schedule schedule = (Schedule) value;
            value = schedule.getCareType() + " at " + schedule.getScheduleTime() + " for pet " + schedule.getPetId();
        }
        return super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
    }
}
