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
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JCheckBox;
import javax.swing.ListSelectionModel;

public class GameTypeManagerDialog extends JDialog {

    private static final long serialVersionUID = 1L;

    private GameTypeDAO gameTypeDAO;
    private GameTypeVariantDAO variantDAO;

    // ── Left panel — Game Types ──
    private DefaultListModel<GameType> gameTypeModel;
    private JList<GameType> gameTypeList;
    private JButton addGameTypeButton;
    private JButton deleteGameTypeButton;

    // ── Right panel — Variants ──
    private DefaultListModel<GameTypeVariant> variantModel;
    private JList<GameTypeVariant> variantList;
    private JButton addVariantButton;
    private JButton editVariantButton;
    private JButton deleteVariantButton;
    private JButton setActiveButton;
    private JButton setInvestigatingButton;
    private JButton setInactiveButton;
    private JLabel variantPanelTitle;

    // ─────────────────────────────────────────────
    // CONSTRUCTOR
    // ─────────────────────────────────────────────

    public GameTypeManagerDialog(JFrame parent, GameTypeDAO gameTypeDAO,
            GameTypeVariantDAO variantDAO) {
        super(parent, "Manage Game Types & Variants", true);
        this.gameTypeDAO = gameTypeDAO;
        this.variantDAO = variantDAO;

        setSize(1100, 750);
        setLocationRelativeTo(parent);
        setLayout(new BorderLayout(10, 10));

        add(buildLeftPanel(), BorderLayout.WEST);
        add(buildRightPanel(), BorderLayout.CENTER);
        add(buildBottomPanel(), BorderLayout.SOUTH);

        loadGameTypes();
    }

    // ─────────────────────────────────────────────
    // LEFT PANEL — Game Types
    // ─────────────────────────────────────────────

    private JPanel buildLeftPanel() {
        JPanel panel = new JPanel(new BorderLayout(5, 5));
        panel.setPreferredSize(new Dimension(280, 0));
        panel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(Color.GRAY),
                "Game Types",
                javax.swing.border.TitledBorder.LEFT,
                javax.swing.border.TitledBorder.TOP,
                new Font("Arial", Font.BOLD, 14)
        ));

        gameTypeModel = new DefaultListModel<>();
        gameTypeList = new JList<>(gameTypeModel);
        gameTypeList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        gameTypeList.setFont(new Font("Arial", Font.PLAIN, 14));
        gameTypeList.setFixedCellHeight(30);

        JScrollPane scroll = new JScrollPane(gameTypeList);
        panel.add(scroll, BorderLayout.CENTER);

        // Buttons
        JPanel buttonPanel = new JPanel();
        addGameTypeButton = new JButton("+ Add");
        addGameTypeButton.setBackground(new Color(0, 160, 80));
        addGameTypeButton.setForeground(Color.WHITE);
        addGameTypeButton.setOpaque(true);
        deleteGameTypeButton = new JButton("Delete");
        deleteGameTypeButton.setEnabled(false);

        buttonPanel.add(addGameTypeButton);
        buttonPanel.add(deleteGameTypeButton);
        panel.add(buttonPanel, BorderLayout.SOUTH);

        // Wire up
        gameTypeList.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                GameType selected = gameTypeList.getSelectedValue();
                if (selected != null) {
                    loadVariants(selected);
                    deleteGameTypeButton.setEnabled(true);
                }
            }
        });

        addGameTypeButton.addActionListener(e -> addGameType());
        deleteGameTypeButton.addActionListener(e -> deleteGameType());

        return panel;
    }

    // ─────────────────────────────────────────────
    // RIGHT PANEL — Variants
    // ─────────────────────────────────────────────

    private JPanel buildRightPanel() {
        JPanel panel = new JPanel(new BorderLayout(5, 5));
        panel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(Color.GRAY),
                "Variants",
                javax.swing.border.TitledBorder.LEFT,
                javax.swing.border.TitledBorder.TOP,
                new Font("Arial", Font.BOLD, 14)
        ));

        // Title showing which gametype is selected
        variantPanelTitle = new JLabel("Select a Game Type to view its variants");
        variantPanelTitle.setFont(new Font("Arial", Font.ITALIC, 13));
        variantPanelTitle.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        panel.add(variantPanelTitle, BorderLayout.NORTH);

        // Variant list
        variantModel = new DefaultListModel<>();
        variantList = new JList<>(variantModel);
        variantList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        variantList.setFont(new Font("Arial", Font.PLAIN, 13));
        variantList.setFixedCellHeight(28);

        JScrollPane scroll = new JScrollPane(variantList);
        panel.add(scroll, BorderLayout.CENTER);

        // Buttons
        JPanel buttonPanel = new JPanel();

        addVariantButton = new JButton("+ Add Variant");
        addVariantButton.setBackground(new Color(0, 160, 80));
        addVariantButton.setForeground(Color.WHITE);
        addVariantButton.setOpaque(true);
        addVariantButton.setEnabled(false);

        editVariantButton = new JButton("Edit");
        editVariantButton.setEnabled(false);

        deleteVariantButton = new JButton("Delete");
        deleteVariantButton.setEnabled(false);

        setActiveButton = new JButton("→ ACTIVE");
        setActiveButton.setBackground(new Color(0, 120, 215));
        setActiveButton.setForeground(Color.WHITE);
        setActiveButton.setOpaque(true);
        setActiveButton.setEnabled(false);

        setInvestigatingButton = new JButton("→ INVESTIGATING");
        setInvestigatingButton.setEnabled(false);

        setInactiveButton = new JButton("→ INACTIVE");
        setInactiveButton.setBackground(new Color(180, 50, 50));
        setInactiveButton.setForeground(Color.WHITE);
        setInactiveButton.setOpaque(true);
        setInactiveButton.setEnabled(false);

        buttonPanel.add(addVariantButton);
        buttonPanel.add(editVariantButton);
        buttonPanel.add(deleteVariantButton);
        buttonPanel.add(new JLabel("  |  "));
        buttonPanel.add(setActiveButton);
        buttonPanel.add(setInvestigatingButton);
        buttonPanel.add(setInactiveButton);

        panel.add(buttonPanel, BorderLayout.SOUTH);

        // Wire up variant list selection
        variantList.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                boolean selected = variantList.getSelectedValue() != null;
                editVariantButton.setEnabled(selected);
                deleteVariantButton.setEnabled(selected);
                setActiveButton.setEnabled(selected);
                setInvestigatingButton.setEnabled(selected);
                setInactiveButton.setEnabled(selected);
            }
        });

        addVariantButton.addActionListener(e -> addVariant());
        editVariantButton.addActionListener(e -> editVariant());
        deleteVariantButton.addActionListener(e -> deleteVariant());
        setActiveButton.addActionListener(e -> setVariantStatus("ACTIVE"));
        setInvestigatingButton.addActionListener(e -> setVariantStatus("INVESTIGATING"));
        setInactiveButton.addActionListener(e -> setVariantStatus("INACTIVE"));

        return panel;
    }

    // ─────────────────────────────────────────────
    // BOTTOM PANEL
    // ─────────────────────────────────────────────

    private JPanel buildBottomPanel() {
        JPanel panel = new JPanel();
        panel.setBackground(new Color(45, 45, 45));
        panel.setPreferredSize(new Dimension(0, 50));

        JButton closeButton = new JButton("Close");
        closeButton.setFont(new Font("Arial", Font.BOLD, 13));
        closeButton.setPreferredSize(new Dimension(120, 35));
        closeButton.addActionListener(e -> dispose());
        panel.add(closeButton);

        return panel;
    }

    // ─────────────────────────────────────────────
    // LOAD GAME TYPES
    // ─────────────────────────────────────────────

    private void loadGameTypes() {
        gameTypeModel.clear();
        List<GameType> types = gameTypeDAO.getAllGameTypes();
        for (GameType gt : types) {
            gameTypeModel.addElement(gt);
        }
    }

    // ─────────────────────────────────────────────
    // LOAD VARIANTS FOR SELECTED GAME TYPE
    // ─────────────────────────────────────────────

    private void loadVariants(GameType gameType) {
        variantModel.clear();
        variantPanelTitle.setText("Variants for: " + gameType.getName() +
                "   (" + gameType.getPlayerParity() + ")");
        addVariantButton.setEnabled(true);

        List<GameTypeVariant> variants = variantDAO.getVariantsForGameType(gameType.getId());
        for (GameTypeVariant v : variants) {
            variantModel.addElement(v);
        }
    }

    // ─────────────────────────────────────────────
    // ADD GAME TYPE
    // ─────────────────────────────────────────────

    private void addGameType() {
        JPanel form = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JTextField nameField = new JTextField(20);
        JComboBox<String> parityCombo = new JComboBox<>(
                new String[]{"EVEN_ONLY", "ODD_ONLY", "BOTH"});
        JCheckBox pvpCheck = new JCheckBox("PvP (uncheck for PvE like Firefight)");
        pvpCheck.setSelected(true);

        gbc.gridx = 0; gbc.gridy = 0;
        form.add(new JLabel("Game Type Name:"), gbc);
        gbc.gridx = 1;
        form.add(nameField, gbc);

        gbc.gridx = 0; gbc.gridy = 1;
        form.add(new JLabel("Player Parity:"), gbc);
        gbc.gridx = 1;
        form.add(parityCombo, gbc);

        gbc.gridx = 0; gbc.gridy = 2; gbc.gridwidth = 2;
        form.add(pvpCheck, gbc);

        int result = JOptionPane.showConfirmDialog(this, form,
                "Add New Game Type", JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE);

        if (result == JOptionPane.OK_OPTION) {
            String name = nameField.getText().trim();
            if (!name.isEmpty()) {
                GameType newType = new GameType(name, pvpCheck.isSelected(),
                        (String) parityCombo.getSelectedItem());
                gameTypeDAO.insertGameType(newType);
                loadGameTypes();
            }
        }
    }

    // ─────────────────────────────────────────────
    // DELETE GAME TYPE
    // ─────────────────────────────────────────────

    private void deleteGameType() {
        GameType selected = gameTypeList.getSelectedValue();
        if (selected == null) return;

        int confirm = JOptionPane.showConfirmDialog(this,
                "Delete \"" + selected.getName() + "\" and ALL its variants?\n" +
                "This cannot be undone.",
                "Confirm Delete", JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE);

        if (confirm == JOptionPane.YES_OPTION) {
            // We need a delete method in GameTypeDAO
            String sql = "DELETE FROM game_type WHERE id = ?";
            try (java.sql.Connection conn = DatabaseManager.getConnection();
                 java.sql.PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setInt(1, selected.getId());
                pstmt.executeUpdate();
            } catch (java.sql.SQLException e) {
                System.err.println("Error deleting game type: " + e.getMessage());
            }
            variantModel.clear();
            variantPanelTitle.setText("Select a Game Type to view its variants");
            loadGameTypes();
        }
    }

    // ─────────────────────────────────────────────
    // ADD VARIANT
    // ─────────────────────────────────────────────

    private void addVariant() {
        GameType selectedType = gameTypeList.getSelectedValue();
        if (selectedType == null) return;

        GameTypeVariant newVariant = showVariantEditForm(null, selectedType);
        if (newVariant != null) {
            variantDAO.insertVariant(newVariant);
            loadVariants(selectedType);
        }
    }

    // ─────────────────────────────────────────────
    // EDIT VARIANT
    // ─────────────────────────────────────────────

    private void editVariant() {
        GameTypeVariant selected = variantList.getSelectedValue();
        GameType selectedType = gameTypeList.getSelectedValue();
        if (selected == null || selectedType == null) return;

        GameTypeVariant updated = showVariantEditForm(selected, selectedType);
        if (updated != null) {
            variantDAO.updateVariant(updated);
            loadVariants(selectedType);
        }
    }

    // ─────────────────────────────────────────────
    // DELETE VARIANT
    // ─────────────────────────────────────────────

    private void deleteVariant() {
        GameTypeVariant selected = variantList.getSelectedValue();
        GameType selectedType = gameTypeList.getSelectedValue();
        if (selected == null) return;

        int confirm = JOptionPane.showConfirmDialog(this,
                "Delete variant \"" + selected.getEffectiveName() + "\"?\n" +
                "This cannot be undone.",
                "Confirm Delete", JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE);

        if (confirm == JOptionPane.YES_OPTION) {
            variantDAO.deleteVariant(selected.getId());
            loadVariants(selectedType);
        }
    }

    // ─────────────────────────────────────────────
    // SET VARIANT STATUS (quick buttons)
    // ─────────────────────────────────────────────

    private void setVariantStatus(String status) {
        GameTypeVariant selected = variantList.getSelectedValue();
        GameType selectedType = gameTypeList.getSelectedValue();
        if (selected == null) return;

        selected.setStatus(status);
        variantDAO.updateVariant(selected);
        loadVariants(selectedType);
    }

    // ─────────────────────────────────────────────
    // VARIANT EDIT FORM (shared by add and edit)
    // ─────────────────────────────────────────────

    private GameTypeVariant showVariantEditForm(GameTypeVariant existing,
            GameType parentType) {
        JPanel form = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JTextField officialNameField = new JTextField(30);
        JTextField displayNameField = new JTextField(30);
        JTextArea descriptionArea = new JTextArea(3, 30);
        descriptionArea.setLineWrap(true);
        descriptionArea.setWrapStyleWord(true);
        JComboBox<String> statusCombo = new JComboBox<>(
                new String[]{"INVESTIGATING", "ACTIVE", "INACTIVE"});
        JCheckBox slayerAdjacentCheck = new JCheckBox(
                "Slayer Adjacent (include in Slayer randomizer pool)");
        JTextArea notesArea = new JTextArea(3, 30);
        notesArea.setLineWrap(true);
        notesArea.setWrapStyleWord(true);

        // Pre-fill if editing
        if (existing != null) {
            officialNameField.setText(existing.getOfficialName());
            displayNameField.setText(existing.getDisplayName() != null ?
                    existing.getDisplayName() : "");
            descriptionArea.setText(existing.getDescription() != null ?
                    existing.getDescription() : "");
            statusCombo.setSelectedItem(existing.getStatus());
            slayerAdjacentCheck.setSelected(existing.isSlayerAdjacent());
            notesArea.setText(existing.getNotes() != null ? existing.getNotes() : "");
        }

        int row = 0;

        gbc.gridx = 0; gbc.gridy = row; gbc.weightx = 0;
        form.add(new JLabel("Official Name (exact in-game):"), gbc);
        gbc.gridx = 1; gbc.weightx = 1.0;
        form.add(officialNameField, gbc);
        row++;

        gbc.gridx = 0; gbc.gridy = row; gbc.weightx = 0;
        form.add(new JLabel("Display Name (optional):"), gbc);
        gbc.gridx = 1; gbc.weightx = 1.0;
        form.add(displayNameField, gbc);
        row++;

        gbc.gridx = 0; gbc.gridy = row; gbc.weightx = 0;
        gbc.anchor = GridBagConstraints.NORTHWEST;
        form.add(new JLabel("Description / Rules:"), gbc);
        gbc.gridx = 1; gbc.weightx = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        form.add(new JScrollPane(descriptionArea), gbc);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.WEST;
        row++;

        gbc.gridx = 0; gbc.gridy = row; gbc.weightx = 0;
        form.add(new JLabel("Status:"), gbc);
        gbc.gridx = 1; gbc.weightx = 1.0;
        form.add(statusCombo, gbc);
        row++;

        gbc.gridx = 0; gbc.gridy = row; gbc.gridwidth = 2;
        form.add(slayerAdjacentCheck, gbc);
        gbc.gridwidth = 1;
        row++;

        gbc.gridx = 0; gbc.gridy = row; gbc.weightx = 0;
        gbc.anchor = GridBagConstraints.NORTHWEST;
        form.add(new JLabel("Notes:"), gbc);
        gbc.gridx = 1; gbc.weightx = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        form.add(new JScrollPane(notesArea), gbc);

        String title = existing != null ? "Edit Variant" : "Add Variant — " +
                parentType.getName();
        int result = JOptionPane.showConfirmDialog(this, form, title,
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

        if (result == JOptionPane.OK_OPTION) {
            String officialName = officialNameField.getText().trim();
            if (officialName.isEmpty()) return null;

            String displayName = displayNameField.getText().trim();
            String description = descriptionArea.getText().trim();
            String status = (String) statusCombo.getSelectedItem();
            boolean slayerAdjacent = slayerAdjacentCheck.isSelected();
            String notes = notesArea.getText().trim();

            if (existing != null) {
                existing.setOfficialName(officialName);
                existing.setDisplayName(displayName.isEmpty() ? null : displayName);
                existing.setDescription(description.isEmpty() ? null : description);
                existing.setStatus(status);
                existing.setSlayerAdjacent(slayerAdjacent);
                existing.setNotes(notes.isEmpty() ? null : notes);
                return existing;
            } else {
                return new GameTypeVariant(
                        parentType.getId(),
                        officialName,
                        displayName.isEmpty() ? null : displayName,
                        description.isEmpty() ? null : description,
                        status,
                        slayerAdjacent,
                        notes.isEmpty() ? null : notes
                );
            }
        }
        return null;
    }
}