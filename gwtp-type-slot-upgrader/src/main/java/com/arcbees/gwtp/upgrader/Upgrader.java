/**
 * Copyright 2015 ArcBees Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package com.arcbees.gwtp.upgrader;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.Label;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.logging.Logger;

import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JEditorPane;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

public class Upgrader extends JPanel {

    private static final Logger LOGGER = Logger.getGlobal();

    private static final long serialVersionUID = 1L;

    JTextField fileTextField = new JTextField();

    private JRadioButton v2;

    private JProgressBar progressBar;

    private Label progressLabel;

    public Upgrader() {
        super(new BorderLayout(10, 10));

        setPreferredSize(new Dimension(600, 400));

        addBackupWarning();

        JPanel centerPanel = new JPanel(new BorderLayout(10, 10));

        add(centerPanel, BorderLayout.CENTER);

        addOpenDialog(centerPanel);
        addVersionChoice(centerPanel);
        addRunButton(centerPanel);
        addProgressBar();
    }

    private void addProgressBar() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(new EmptyBorder(15, 15, 35, 15));
        progressLabel = new Label();
        panel.add(progressLabel, BorderLayout.PAGE_START);

        progressBar = new JProgressBar();

        panel.add(progressBar, BorderLayout.PAGE_END);

        add(panel, BorderLayout.PAGE_END);
    }

    private void addRunButton(JPanel container) {
        JPanel panel = new JPanel();
        final JButton runButton = new JButton("Convert Project");
        runButton.setEnabled(false);
        runButton.setSize(new Dimension(100, 20));
        panel.add(runButton);
        container.add(panel, BorderLayout.PAGE_END);

        runButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                File file = new File(fileTextField.getText());
                if (file.exists() && file.isDirectory()) {
                    new SlotCollector(file, v2.isSelected(), progressBar, progressLabel);
                } else {
                    JOptionPane.showMessageDialog(null, "Please select a directory");
                }
            }
        });

        fileTextField.getDocument().addDocumentListener(new DocumentListener() {

            @Override
            public void removeUpdate(DocumentEvent e) {
                runButton.setEnabled(!fileTextField.getText().isEmpty());
            }

            @Override
            public void insertUpdate(DocumentEvent e) {
                runButton.setEnabled(!fileTextField.getText().isEmpty());
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                runButton.setEnabled(!fileTextField.getText().isEmpty());
            }
        });
    }

    private void addVersionChoice(JPanel container) {

        JPanel panel = new JPanel();
        panel.setBorder(new TitledBorder("Choose target version"));

        v2 = new JRadioButton("GWTP 1.5");
        JRadioButton v1 = new JRadioButton("GWTP 1.4 and below");
        ButtonGroup bG = new ButtonGroup();

        bG.add(v2);
        bG.add(v1);

        panel.add(v2);
        panel.add(v1);
        v2.setSelected(true);
        container.add(panel, BorderLayout.CENTER);
    }

    private void addOpenDialog(JPanel container) {
        JPanel outer = new JPanel();
        outer.setBorder(new TitledBorder("Choose Project Directory"));
        outer.setLayout(new BorderLayout(10, 10));

        JPanel panel = new JPanel(new BorderLayout(5, 5));
        panel.setBorder(new EmptyBorder(new Insets(5, 5, 5, 5)));
        JButton openButton = new JButton("Browse");

        openButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                LOGGER.info("Showing file dialog");
                final JFileChooser fileChooser = new JFileChooser();

                fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);

                if (fileChooser.showOpenDialog(Upgrader.this) == JFileChooser.APPROVE_OPTION) {
                    setDirectory(fileChooser.getSelectedFile());
                } else {
                    LOGGER.info("file selection cancelled");
                }
            }
        });

        panel.add(fileTextField, BorderLayout.CENTER);
        panel.add(openButton, BorderLayout.EAST);

        outer.add(panel);
        container.add(outer, BorderLayout.PAGE_START);
    }

    private void addBackupWarning() {
        JEditorPane warning = new JEditorPane();
        warning.setContentType("text/html");

        warning.setText("<h2 align='center'>This program will overwrite files in your project!</h2>"
                + "<h3 align='center'>Backup your project before running it!</h3>");
        add(warning, BorderLayout.PAGE_START);
    }

    private static void createAndShowGui() {
        final JFrame frame = new JFrame("GWTP Upgrader");

        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        JComponent newContentPane = new Upgrader();
        newContentPane.setOpaque(true);
        frame.setContentPane(newContentPane);

        frame.pack();
        frame.setVisible(true);
    }

    private void setDirectory(File selectedFile) {
        LOGGER.info("Selected dir: " + selectedFile.getName());
        fileTextField.setText(selectedFile.getAbsolutePath());
    }

    public static void main(String[] args) {
        javax.swing.SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                createAndShowGui();
            }
        });
    }
}
