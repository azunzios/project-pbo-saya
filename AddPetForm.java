import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.Date;
import java.text.ParseException;
import java.text.SimpleDateFormat;

public class AddPetForm extends JFrame {
    private JTextField nameField;
    private JComboBox<String> typeComboBox;
    private JTextField birthDateField;
    private JTextField weightField;
    private JComboBox<String> genderComboBox;
    private JTextArea notesArea;
    private MainMenu mainMenu;

    public AddPetForm(MainMenu mainMenu) {
        this.mainMenu = mainMenu;
        initializeUI();
    }

    private void initializeUI() {
        setTitle("Add New Pet");
        setSize(400, 500);
        setLayout(new GridLayout(0, 2, 5, 5));

        // Form fields
        add(new JLabel("Name:"));
        nameField = new JTextField();
        add(nameField);

        add(new JLabel("Type:"));
        typeComboBox = new JComboBox<>(new String[]{"Dog", "Cat", "Bird", "Fish", "Other"});
        add(typeComboBox);

        add(new JLabel("Birth Date (yyyy-MM-dd):"));
        birthDateField = new JTextField();
        add(birthDateField);

        add(new JLabel("Weight (kg):"));
        weightField = new JTextField();
        add(weightField);

        add(new JLabel("Gender:"));
        genderComboBox = new JComboBox<>(new String[]{"Male", "Female", "Other"});
        add(genderComboBox);

        add(new JLabel("Notes:"));
        notesArea = new JTextArea();
        JScrollPane scrollPane = new JScrollPane(notesArea);
        add(scrollPane);

        // Buttons
        JButton saveButton = new JButton("Save");
        saveButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                savePet();
            }
        });
        add(saveButton);

        JButton cancelButton = new JButton("Cancel");
        cancelButton.addActionListener(e -> dispose());
        add(cancelButton);
    }

    private void savePet() {
        try {
            // Create a new Pet object
            Pet pet = new Pet();
            pet.setName(nameField.getText());
            pet.setType((String) typeComboBox.getSelectedItem());
            
            // Parse birth date
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            java.util.Date parsedDate = sdf.parse(birthDateField.getText());
            pet.setBirthDate(new Date(parsedDate.getTime()));
            
            pet.setWeight(Double.parseDouble(weightField.getText()));
            pet.setGender((String) genderComboBox.getSelectedItem());
            pet.setNotes(notesArea.getText());

            // Save to database
            new PetDAO().addPet(pet);
            JOptionPane.showMessageDialog(this, "Pet saved successfully!");
            dispose();
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}
