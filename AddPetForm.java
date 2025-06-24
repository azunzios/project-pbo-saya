import javax.swing.*;
import java.awt.*;
import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.Objects;

public class AddPetForm extends JFrame {
    private JTextField nameField;
    private JComboBox<String> typeComboBox;
    private JTextField birthDateField;
    private MainMenu mainMenu; // To refresh the main menu after adding

    public AddPetForm(MainMenu mainMenu) {
        this.mainMenu = mainMenu;
        initializeUI();
    }

    private void initializeUI() {
        setTitle("Add New Pet");
        setSize(400, 300);
        setLocationRelativeTo(mainMenu); // Center relative to main menu
        setLayout(new BorderLayout(10, 10));

        JPanel formPanel = new JPanel(new GridLayout(0, 2, 5, 5));
        formPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Form fields
        formPanel.add(new JLabel("Name:"));
        nameField = new JTextField();
        formPanel.add(nameField);

        formPanel.add(new JLabel("Type:"));
        String[] petTypes = {"Dog", "Cat", "Bird", "Fish", "Other..."};
        typeComboBox = new JComboBox<>(petTypes);
        formPanel.add(typeComboBox);

        formPanel.add(new JLabel("Birth Date (yyyy-MM-dd):"));
        birthDateField = new JTextField();
        formPanel.add(birthDateField);

        // Listener for "Other..." option
        typeComboBox.addActionListener(e -> {
            if (Objects.equals(typeComboBox.getSelectedItem(), "Other...")) {
                String newType = JOptionPane.showInputDialog(this, "Enter new pet type:");
                if (newType != null && !newType.trim().isEmpty()) {
                    // Add the new type and select it
                    typeComboBox.insertItemAt(newType, 0);
                    typeComboBox.setSelectedItem(newType);
                } else {
                    typeComboBox.setSelectedIndex(0); // Revert if cancelled
                }
            }
        });

        add(formPanel, BorderLayout.CENTER);

        // Buttons
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton saveButton = new JButton("Save");
        JButton cancelButton = new JButton("Cancel");
        buttonPanel.add(saveButton);
        buttonPanel.add(cancelButton);
        add(buttonPanel, BorderLayout.SOUTH);

        saveButton.addActionListener(e -> savePet());
        cancelButton.addActionListener(e -> dispose());
    }

    private void savePet() {
        try {
            Pet pet = new Pet();
            pet.setName(nameField.getText());
            pet.setType((String) typeComboBox.getSelectedItem());

            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            java.util.Date parsedDate = sdf.parse(birthDateField.getText());
            pet.setBirthDate(new Date(parsedDate.getTime()));

            new PetDAO().addPet(pet);
            JOptionPane.showMessageDialog(this, "Pet saved successfully!");

            // Optional: Refresh the pet list in other forms if needed
            mainMenu.loadSchedules(); // Reload schedules to show new pet's schedules if any

            dispose();
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}