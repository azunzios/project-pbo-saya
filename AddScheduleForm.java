import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.Time;
import java.util.ArrayList;
import java.util.List;

public class AddScheduleForm extends JFrame {
    private JComboBox<Pet> petComboBox;
    private JComboBox<String> careTypeComboBox;
    private JTextField timeField;
    private JTextField daysField;
    private MainMenu mainMenu;

    public AddScheduleForm(MainMenu mainMenu) {
        this.mainMenu = mainMenu;
        initializeUI();
    }

    private void initializeUI() {
        setTitle("Add New Schedule");
        setSize(400, 300);
        setLayout(new GridLayout(0, 2, 5, 5));

        // Load pets
        List<Pet> pets = new ArrayList<>(); // TODO: Load pets from database
        pets.add(new Pet("Buddy", "Dog", null));
        pets.add(new Pet("Whiskers", "Cat", null));

        // Form fields
        add(new JLabel("Pet:"));
        petComboBox = new JComboBox<>(pets.toArray(new Pet[0]));
        petComboBox.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                if (value instanceof Pet) {
                    value = ((Pet) value).getName();
                }
                return super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            }
        });
        add(petComboBox);

        add(new JLabel("Care Type:"));
        careTypeComboBox = new JComboBox<>(new String[]{"Feeding", "Grooming", "Walking", "Vet Visit", "Play Time"});
        add(careTypeComboBox);

        add(new JLabel("Time (HH:MM):"));
        timeField = new JTextField("08:00");
        add(timeField);

        add(new JLabel("Days (e.g. Mon,Wed,Fri):"));
        daysField = new JTextField();
        add(daysField);

        // Buttons
        JButton saveButton = new JButton("Save");
        saveButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                saveSchedule();
            }
        });
        add(saveButton);

        JButton cancelButton = new JButton("Cancel");
        cancelButton.addActionListener(e -> dispose());
        add(cancelButton);
    }

    private void saveSchedule() {
        try {
            Pet selectedPet = (Pet) petComboBox.getSelectedItem();
            String careType = (String) careTypeComboBox.getSelectedItem();
            String[] timeParts = timeField.getText().split(":");
            Time time = new Time(Integer.parseInt(timeParts[0]), Integer.parseInt(timeParts[1]), 0);
            String days = daysField.getText();

            Schedule schedule = new Schedule(selectedPet.getId(), careType, time, days);
            
            // Save to database
            new ScheduleDAO().addSchedule(schedule);
            JOptionPane.showMessageDialog(this, "Schedule saved successfully!");
            mainMenu.loadSchedules(); // Refresh the schedule list
            dispose();
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}
