package com.halo;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SpinnerNumberModel;

public class MapEditDialog extends JDialog {

    private static final long serialVersionUID = 1L;

    private MapDAO mapDAO;
    private GameTypeDAO gameTypeDAO;
    private Map existingMap;  // null if adding, populated if editing
    private boolean isEditMode;

    // ── Form fields ──
    private JTextField nameField;
    private JComboBox<String> sizeCombo;
    private JSpinner minPlayersSpinner;
    private JSpinner maxPlayersSpinner;
    private JTextField lightingField;
    private JTextField imageFilenameField;
    private JTextField waypointLinkField;
    private JTextArea descriptionArea;
    private JTextArea customModesArea;

    // ── GameType assignment ──
    private JList<GameType> availableGameTypesList;
    private JList<GameType> assignedGameTypesList;
    private DefaultListModel<GameType> availableModel;
    private DefaultListModel<GameType> assignedModel;
    private JButton assignButton;
    private JButton removeButton;

    // ── Bottom buttons ──
    private JButton saveButton;
    private JButton cancelButton;

    // ─────────────────────────────────────────────
    // CONSTRUCTOR
    // ─────────────────────────────────────────────

    public MapEditDialog(JFrame parent, Map map, MapDAO mapDAO, GameTypeDAO gameTypeDAO) {
        super(parent, true);  // modal — blocks main frame while open
        this.mapDAO = mapDAO;
        this.gameTypeDAO = gameTypeDAO;
        this.existingMap = map;
        this.isEditMode = (map != null);

        setTitle(isEditMode ? "Edit Map: " + map.getName() : "Add New Map");
        setSize(1000, 700);
        setLocationRelativeTo(parent);
        setLayout(new BorderLayout(10, 10));

        JScrollPane formScroll = new JScrollPane(buildFormPanel());
        formScroll.getVerticalScrollBar().setUnitIncrement(16);
        add(formScroll, BorderLayout.CENTER);
        add(buildBottomPanel(), BorderLayout.SOUTH);

        if (isEditMode) {
            populateFields();
        }

        loadGameTypeLists();
    }

    // ─────────────────────────────────────────────
    // FORM PANEL
    // ─────────────────────────────────────────────

    private JPanel buildFormPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(5, 20, 10, 20));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 6, 6, 6);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;

        int row = 0;

        // ── Map Name ──
        gbc.gridx = 0; gbc.gridy = row; gbc.weightx = 0;
        panel.add(new JLabel("Map Name:"), gbc);
        gbc.gridx = 1; gbc.weightx = 1.0; gbc.gridwidth = 3;
        nameField = new JTextField();
        nameField.setFont(new Font("Arial", Font.PLAIN, 14));
        panel.add(nameField, gbc);
        gbc.gridwidth = 1;
        row++;

        // ── Size ──
        gbc.gridx = 0; gbc.gridy = row; gbc.weightx = 0;
        panel.add(new JLabel("Size:"), gbc);
        gbc.gridx = 1; gbc.weightx = 0.3;
        sizeCombo = new JComboBox<>(new String[]{"Small", "Medium", "Large", "XL"});
        panel.add(sizeCombo, gbc);

        // ── Min Players ──
        gbc.gridx = 2; gbc.weightx = 0;
        panel.add(new JLabel("Min Players:"), gbc);
        gbc.gridx = 3; gbc.weightx = 0.3;
        minPlayersSpinner = new JSpinner(new SpinnerNumberModel(2, 2, 50, 1));
        panel.add(minPlayersSpinner, gbc);
        row++;

        // ── Max Players ──
        gbc.gridx = 0; gbc.gridy = row; gbc.weightx = 0;
        panel.add(new JLabel("Max Players:"), gbc);
        gbc.gridx = 1; gbc.weightx = 0.3;
        maxPlayersSpinner = new JSpinner(new SpinnerNumberModel(16, 2, 50, 1));
        panel.add(maxPlayersSpinner, gbc);

        // ── Lighting ──
        gbc.gridx = 2; gbc.weightx = 0;
        panel.add(new JLabel("Lighting:"), gbc);
        gbc.gridx = 3; gbc.weightx = 0.3;
        lightingField = new JTextField();
        panel.add(lightingField, gbc);
        row++;

        // ── Image Filename ──
        gbc.gridx = 0; gbc.gridy = row; gbc.weightx = 0;
        panel.add(new JLabel("Image Filename:"), gbc);
        gbc.gridx = 1; gbc.weightx = 1.0; gbc.gridwidth = 3;
        imageFilenameField = new JTextField();
        imageFilenameField.setToolTipText("e.g. my_map.png — file must be in the images/ folder");
        panel.add(imageFilenameField, gbc);
        gbc.gridwidth = 1;
        row++;

        // ── Waypoint Link ──
        gbc.gridx = 0; gbc.gridy = row; gbc.weightx = 0;
        panel.add(new JLabel("Waypoint Link:"), gbc);
        gbc.gridx = 1; gbc.weightx = 1.0; gbc.gridwidth = 3;
        waypointLinkField = new JTextField();
        waypointLinkField.setToolTipText("Halo Waypoint URL for this map");
        panel.add(waypointLinkField, gbc);
        gbc.gridwidth = 1;
        row++;

        // ── Description ──
        gbc.gridx = 0; gbc.gridy = row; gbc.weightx = 0;
        gbc.anchor = GridBagConstraints.NORTHWEST;
        panel.add(new JLabel("Description:"), gbc);
        gbc.gridx = 1; gbc.weightx = 1.0; gbc.gridwidth = 3;
        gbc.fill = GridBagConstraints.BOTH;
        descriptionArea = new JTextArea(4, 20);
        descriptionArea.setLineWrap(true);
        descriptionArea.setWrapStyleWord(true);
        panel.add(new JScrollPane(descriptionArea), gbc);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.gridwidth = 1;
        row++;

        // ── Custom Modes ──
        gbc.gridx = 0; gbc.gridy = row; gbc.weightx = 0;
        gbc.anchor = GridBagConstraints.NORTHWEST;
        panel.add(new JLabel("Custom Modes:"), gbc);
        gbc.gridx = 1; gbc.weightx = 1.0; gbc.gridwidth = 3;
        gbc.fill = GridBagConstraints.BOTH;
        customModesArea = new JTextArea(3, 20);
        customModesArea.setLineWrap(true);
        customModesArea.setWrapStyleWord(true);
        customModesArea.setToolTipText("e.g. Gravemind, Thunderbowl, Famished Falcons");
        panel.add(new JScrollPane(customModesArea), gbc);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.gridwidth = 1;
        row++;

        // ── GameType Assignment ──
        gbc.gridx = 0; gbc.gridy = row; gbc.weightx = 0;
        gbc.gridwidth = 4;
        JLabel gtTitle = new JLabel("Assign Game Types:");
        gtTitle.setFont(new Font("Arial", Font.BOLD, 13));
        panel.add(gtTitle, gbc);
        gbc.gridwidth = 1;
        row++;

        // Available list
        gbc.gridx = 0; gbc.gridy = row; gbc.weightx = 0.45;
        gbc.fill = GridBagConstraints.BOTH;
        availableModel = new DefaultListModel<>();
        availableGameTypesList = new JList<>(availableModel);
        availableGameTypesList.setSelectionMode(
                ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        JScrollPane availScroll = new JScrollPane(availableGameTypesList);
        availScroll.setPreferredSize(new Dimension(300, 180));
        availScroll.setBorder(BorderFactory.createTitledBorder("Available"));
        panel.add(availScroll, gbc);

        // Assign / Remove buttons (center column)
        gbc.gridx = 1; gbc.weightx = 0.1;
        gbc.fill = GridBagConstraints.NONE;
        gbc.anchor = GridBagConstraints.CENTER;
        JPanel arrowPanel = new JPanel(new BorderLayout(0, 10));
        assignButton = new JButton("→ Add");
        removeButton = new JButton("← Remove");
        arrowPanel.add(assignButton, BorderLayout.NORTH);
        arrowPanel.add(removeButton, BorderLayout.SOUTH);
        panel.add(arrowPanel, gbc);

        // Assigned list
        gbc.gridx = 2; gbc.weightx = 0.45; gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.BOTH;
        assignedModel = new DefaultListModel<>();
        assignedGameTypesList = new JList<>(assignedModel);
        assignedGameTypesList.setSelectionMode(
                ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        JScrollPane assignScroll = new JScrollPane(assignedGameTypesList);
        assignScroll.setPreferredSize(new Dimension(300, 180));
        assignScroll.setBorder(BorderFactory.createTitledBorder("Assigned to this Map"));
        panel.add(assignScroll, gbc);
        gbc.gridwidth = 1;

        // Wire up assign/remove buttons
        assignButton.addActionListener(e -> assignSelectedGameTypes());
        removeButton.addActionListener(e -> removeSelectedGameTypes());

        return panel;
    }

    // ─────────────────────────────────────────────
    // BOTTOM PANEL
    // ─────────────────────────────────────────────

    private JPanel buildBottomPanel() {
        JPanel panel = new JPanel();
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        panel.setBackground(new Color(45, 45, 45));

        saveButton = new JButton(isEditMode ? "Save Changes" : "Add Map");
        saveButton.setFont(new Font("Arial", Font.BOLD, 14));
        saveButton.setPreferredSize(new Dimension(160, 40));
        saveButton.setBackground(new Color(0, 160, 80));
        saveButton.setForeground(Color.WHITE);
        saveButton.setOpaque(true);

        cancelButton = new JButton("Cancel");
        cancelButton.setFont(new Font("Arial", Font.BOLD, 14));
        cancelButton.setPreferredSize(new Dimension(120, 40));

        panel.add(saveButton);
        panel.add(cancelButton);

        saveButton.addActionListener(e -> saveMap());
        cancelButton.addActionListener(e -> dispose());

        return panel;
    }

    // ─────────────────────────────────────────────
    // POPULATE FIELDS (edit mode only)
    // ─────────────────────────────────────────────

    private void populateFields() {
        nameField.setText(existingMap.getName());
        sizeCombo.setSelectedItem(existingMap.getSize());
        minPlayersSpinner.setValue(existingMap.getMinPlayers());
        maxPlayersSpinner.setValue(existingMap.getMaxPlayers());
        lightingField.setText(existingMap.getLighting() != null ? existingMap.getLighting() : "");
        imageFilenameField.setText(existingMap.getImageFilename() != null ?
                existingMap.getImageFilename() : "");
        waypointLinkField.setText(existingMap.getWaypointLink() != null ?
                existingMap.getWaypointLink() : "");
        descriptionArea.setText(existingMap.getDescription() != null ?
                existingMap.getDescription() : "");
        customModesArea.setText(existingMap.getCustomModes() != null ?
                existingMap.getCustomModes() : "");
    }

    // ─────────────────────────────────────────────
    // LOAD GAMETYPE LISTS
    // ─────────────────────────────────────────────

    private void loadGameTypeLists() {
        availableModel.clear();
        assignedModel.clear();

        List<GameType> allTypes = gameTypeDAO.getAllGameTypes();

        if (isEditMode) {
            List<GameType> assigned = mapDAO.getGameTypesForMap(existingMap.getId());

            for (GameType gt : allTypes) {
                boolean isAssigned = assigned.stream()
                        .anyMatch(a -> a.getId() == gt.getId());
                if (isAssigned) {
                    assignedModel.addElement(gt);
                } else {
                    availableModel.addElement(gt);
                }
            }
        } 
        
        else {
            // Game types pre-assigned to all maps
            java.util.Set<String> defaultTypes = new java.util.HashSet<>(java.util.Arrays.asList(
                    "Attrition",  "Escalation", "Slayer (FFA)", "Slayer (Team)"
            ));

            for (GameType gt : allTypes) {
                if (defaultTypes.contains(gt.getName())) {
                    assignedModel.addElement(gt);
                } else {
                    availableModel.addElement(gt);
                }
            }
        }
            
    } // end loadGameTypeLists

    // ─────────────────────────────────────────────
    // ASSIGN SELECTED GAME TYPES
    // ─────────────────────────────────────────────

    private void assignSelectedGameTypes() {
        List<GameType> selected = availableGameTypesList.getSelectedValuesList();
        for (GameType gt : selected) {
            availableModel.removeElement(gt);
            assignedModel.addElement(gt);
        }
    }

    // ─────────────────────────────────────────────
    // REMOVE SELECTED GAME TYPES
    // ─────────────────────────────────────────────

    private void removeSelectedGameTypes() {
        List<GameType> selected = assignedGameTypesList.getSelectedValuesList();
        for (GameType gt : selected) {
            assignedModel.removeElement(gt);
            availableModel.addElement(gt);
        }
    }

    // ─────────────────────────────────────────────
    // SAVE MAP
    // ─────────────────────────────────────────────

    private void saveMap() {
        String name = nameField.getText().trim();
        if (name.isEmpty()) {
            nameField.setBackground(new Color(255, 200, 200));
            nameField.setToolTipText("Map name is required");
            return;
        }
        nameField.setBackground(Color.WHITE);

        String size = (String) sizeCombo.getSelectedItem();
        int minPlayers = (int) minPlayersSpinner.getValue();
        int maxPlayers = (int) maxPlayersSpinner.getValue();
        String lighting = lightingField.getText().trim();
        String imageFilename = imageFilenameField.getText().trim();
        String waypointLink = waypointLinkField.getText().trim();
        String description = descriptionArea.getText().trim();
        String customModes = customModesArea.getText().trim();

        if (isEditMode) {
            existingMap.setName(name);
            existingMap.setSize(size);
            existingMap.setMinPlayers(minPlayers);
            existingMap.setMaxPlayers(maxPlayers);
            existingMap.setLighting(lighting.isEmpty() ? null : lighting);
            existingMap.setImageFilename(imageFilename.isEmpty() ? null : imageFilename);
            existingMap.setWaypointLink(waypointLink.isEmpty() ? null : waypointLink);
            existingMap.setDescription(description.isEmpty() ? null : description);
            existingMap.setCustomModes(customModes.isEmpty() ? null : customModes);
            mapDAO.updateMap(existingMap);

            // Sync game types — clear all then re-add assigned
            List<GameType> currentTypes = mapDAO.getGameTypesForMap(existingMap.getId());
            for (GameType gt : currentTypes) {
                mapDAO.removeGameTypeFromMap(existingMap.getId(), gt.getId());
            }
            for (int i = 0; i < assignedModel.size(); i++) {
                mapDAO.addGameTypeToMap(existingMap.getId(), assignedModel.get(i).getId());
            }

        } else {
            Map newMap = new Map(name, size, minPlayers, maxPlayers,
                    lighting.isEmpty() ? null : lighting,
                    imageFilename.isEmpty() ? null : imageFilename,
                    description.isEmpty() ? null : description,
                    waypointLink.isEmpty() ? null : waypointLink,
                    customModes.isEmpty() ? null : customModes);

            int newId = mapDAO.insertMap(newMap);

            if (newId != -1) {
                for (int i = 0; i < assignedModel.size(); i++) {
                    mapDAO.addGameTypeToMap(newId, assignedModel.get(i).getId());
                }
            }
        }

        dispose();
    }
}