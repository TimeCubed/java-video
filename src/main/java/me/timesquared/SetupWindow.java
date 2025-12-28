package me.timesquared;

import org.apache.commons.io.FilenameUtils;

import javax.swing.*;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class SetupWindow extends JFrame {
	private File inputDirectory, outputFile;
	private final List<File> inputFiles = new ArrayList<>();
	
	public SetupWindow(String title) {
		super(title);
		
		JPanel dialogPanel = new JPanel(new BorderLayout(5, 5));
		
		FlowLayout contentPanelLayout = new FlowLayout(FlowLayout.LEFT);
		contentPanelLayout.setVgap(5);
		contentPanelLayout.setHgap(5);
		JPanel contentPanel = new JPanel(contentPanelLayout);
		
		JPanel configurationPanel = new JPanel(new GridBagLayout());
		
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.weightx = 1.0;
		gbc.weighty = 1.0;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		
		gbc.gridx = 0;
		gbc.gridy = 0;
		
		JLabel frameRateLabel = new JLabel("Set frame-rate (in fps):");
		configurationPanel.add(frameRateLabel, gbc);
		
		gbc.gridx = 0;
		gbc.gridy = 1;
		
		SpinnerNumberModel frameRateModel = new SpinnerNumberModel(
				60,
				1,
				null,
				1
		);
		JSpinner frameRateSpinner = new JSpinner(frameRateModel);
		configurationPanel.add(frameRateSpinner, gbc);
		
		gbc.gridx = 0;
		gbc.gridy = 2;
		
		configurationPanel.add(new EmptyPanel(), gbc);
		
		gbc.gridx = 0;
		gbc.gridy = 3;
		
		JLabel videoWidthLabel = new JLabel("Set video width (in px):");
		configurationPanel.add(videoWidthLabel, gbc);
		
		gbc.gridx = 0;
		gbc.gridy = 4;
		
		SpinnerNumberModel widthModel = new SpinnerNumberModel(
				600,
				1,
				null,
				1
		);
		JSpinner videoWidthSpinner = new JSpinner(widthModel);
		configurationPanel.add(videoWidthSpinner, gbc);
		
		gbc.gridx = 0;
		gbc.gridy = 5;
		configurationPanel.add(new EmptyPanel(), gbc);
		
		gbc.gridx = 0;
		gbc.gridy = 6;
		
		JLabel videoHeightLabel = new JLabel("Set video height (in px):");
		configurationPanel.add(videoHeightLabel, gbc);
		
		gbc.gridx = 0;
		gbc.gridy = 7;
		
		SpinnerNumberModel heightModel = new SpinnerNumberModel(
				400,
				1,
				null,
				1
		);
		JSpinner videoHeightSpinner = new JSpinner(heightModel);
		configurationPanel.add(videoHeightSpinner, gbc);
		
		gbc.gridx = 1;
		gbc.gridy = 0;
		
		configurationPanel.add(new EmptyPanel(10, 5), gbc);
		
		gbc.gridx = 2;
		gbc.gridy = 0;
		
		JLabel inputDirectoryLabel = new JLabel("Set input directory (directory should contain only images of the same type):");
		configurationPanel.add(inputDirectoryLabel, gbc);
		
		JPanel inputDirectorySelectorPanel = new JPanel(new GridBagLayout());
		
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.gridwidth = 2;
		gbc.weightx = 2;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		
		JTextField inputDirectoryPathField = new JTextField();
		inputDirectoryPathField.setEnabled(false);
		inputDirectorySelectorPanel.add(inputDirectoryPathField, gbc);
		
		gbc.gridx = 3;
		gbc.gridy = 0;
		gbc.gridwidth = 1;
		gbc.weightx = 0;
		
		JButton browseButton = createBrowseButton(e -> {
			JFileChooser directoryChooser = new JFileChooser();
			
			directoryChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
			
			// don't need to worry about HeadlessExceptions because we shouldn't
			// even be here if we're in a headless environment
			int returnVal = directoryChooser.showOpenDialog(this.getParent());
			
			if (returnVal == JFileChooser.APPROVE_OPTION) {
				this.inputDirectory = directoryChooser.getSelectedFile();
				
				inputDirectoryPathField.setText(this.inputDirectory.getAbsolutePath());
			}
		});
		inputDirectorySelectorPanel.add(browseButton, gbc);
		
		gbc.gridx = 2;
		gbc.gridy = 1;
		gbc.weightx = 1;
		
		configurationPanel.add(inputDirectorySelectorPanel, gbc);
		
		gbc.gridx = 2;
		gbc.gridy = 2;
		
		configurationPanel.add(new EmptyPanel(), gbc);
		
		gbc.gridx = 2;
		gbc.gridy = 3;
		
		JLabel outputFileLabel = new JLabel("Set output file name:");
		configurationPanel.add(outputFileLabel, gbc);
		
		JPanel outputFileSelectorPanel = new JPanel(new GridBagLayout());
		
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.gridwidth = 2;
		gbc.weightx = 2;
		
		JTextField outputFilePathField = new JTextField();
		outputFilePathField.setEnabled(false);
		outputFileSelectorPanel.add(outputFilePathField, gbc);
		
		gbc.gridx = 2;
		gbc.gridy = 0;
		gbc.gridwidth = 1;
		gbc.weightx = 0;
		
		JButton outputFileBrowseButton = createBrowseButton(e -> {
			JFileChooser directoryChooser = new JFileChooser();
			
			if (!(outputFilePathField.getText() == null) && !outputFilePathField.getText().isEmpty()) {
				directoryChooser.setCurrentDirectory(new File(outputFilePathField.getText()));
			}
			
			directoryChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
			directoryChooser.setAcceptAllFileFilterUsed(false);
			
			FileFilter fileFilter = new FileNameExtensionFilter("Movie Files (*.mov)", ".mov");
			
			directoryChooser.setFileFilter(fileFilter);
			
			// don't need to worry about HeadlessExceptions because we shouldn't
			// even be here if we're in a headless environment
			int returnVal = directoryChooser.showSaveDialog(this.getParent());
			
			if (returnVal == JFileChooser.APPROVE_OPTION) {
				this.outputFile = directoryChooser.getSelectedFile();
				
				outputFilePathField.setText(this.outputFile.getAbsolutePath());
			}
		});
		outputFileSelectorPanel.add(outputFileBrowseButton, gbc);
		
		gbc.gridx = 2;
		gbc.gridy = 4;
		gbc.weightx = 1;
		
		configurationPanel.add(outputFileSelectorPanel, gbc);
		
		contentPanel.add(configurationPanel);
		
		JPanel buttonBar = new JPanel(new FlowLayout(FlowLayout.RIGHT));
		
		JButton createButton = new JButton("Create");
		createButton.addActionListener(e -> {
			// framerate, video width and video height don't need to get validated,
			// since they already have restrictions on them from the SpinnerNumberModels.
			
			if (!validateInputDirectory()) {
				return;
			}
			
			if (!validateOutputFile()) {
				return;
			}
			
			this.setVisible(false);
			
			Main.createVideo((int) frameRateSpinner.getValue(), (int) videoWidthSpinner.getValue(), (int) videoHeightSpinner.getValue(), inputFiles, outputFile);
		});
		buttonBar.add(createButton);
		
		JButton cancelButton = new JButton("Cancel");
		cancelButton.addActionListener(e -> System.exit(0));
		buttonBar.add(cancelButton);
		
		dialogPanel.add(buttonBar, BorderLayout.SOUTH);
		dialogPanel.add(contentPanel, BorderLayout.WEST);
		
		this.add(dialogPanel, BorderLayout.CENTER);
		
		this.pack();
	}
	
	private boolean validateOutputFile() {
		if (outputFile == null) {
			JOptionPane.showMessageDialog(
					null,
					"""
							The output path can't be empty.
							""",
					"Invalid path",
					JOptionPane.ERROR_MESSAGE
			);
			
			return false;
		}
		
		if (outputFile.exists() && outputFile.isDirectory()) {
			JOptionPane.showMessageDialog(
					null,
					"""
							The output path points to a directory, not a file.
							""",
					"Invalid path",
					JOptionPane.ERROR_MESSAGE
			);
			
			return false;
		}
		
		File parentDirectory = outputFile.getParentFile();
		
		if (parentDirectory == null) {
			JOptionPane.showMessageDialog(
					null,
					"""
							The chosen output file location is invalid.
							""",
					"Invalid path",
					JOptionPane.ERROR_MESSAGE
			);
			
			return false;
		}
		
		if (!parentDirectory.canWrite()) {
			JOptionPane.showMessageDialog(
					null,
					"""
							The chosen output file location is placed in a directory without
							write permissions. Please choose a writable directory.
							""",
					"Directory not writable",
					JOptionPane.ERROR_MESSAGE
			);
			
			return false;
		}
		
		return true;
	}
	
	private JButton createBrowseButton(ActionListener actionListener) {
		JButton browseButton = new JButton("Browse");
		browseButton.addActionListener(actionListener);
		return browseButton;
	}
	
	private boolean isImageFile(String extension) {
		return extension.equals("jpg") || extension.equals("jpeg") || extension.equals("png") || extension.equals("bmp");
	}
	
	private boolean validateInputDirectory() {
		if (inputDirectory == null || !inputDirectory.exists()) {
			JOptionPane.showMessageDialog(
					null,
					"""
							Please choose a directory that has image files of the same type inside.
							""",
					"No directory selected",
					JOptionPane.ERROR_MESSAGE
			);
			
			return false;
		}
		
		File[] files = inputDirectory.listFiles();
		
		if (files == null || files.length == 0) {
			JOptionPane.showMessageDialog(
					null,
					"""
							Please choose a directory that has image files of the same type inside.
							""",
					"Directory empty",
					JOptionPane.ERROR_MESSAGE
			);
			
			return false;
		}
		
		boolean showMultipleExtensionsDialog = true;
		boolean showUnsupportedExtensionDialog = true;
		String firstFileExtension = "";
		
		for (File file : files) {
			if (file.isDirectory()) {
				continue;
			}
			
			String fileExtension = FilenameUtils.getExtension(file.getName());
			
			if (!isImageFile(fileExtension)) {
				// unsupported extension found
				if (!showUnsupportedExtensionDialog) {
					continue;
				}
				
				int returnVal = JOptionPane.showConfirmDialog(
						null,
						"""
								A file with an unsupported type was found. Only .jpg, .jpeg, .png and .bmp
								files are supported. Unsupported file types will be skipped.
								Continue?
								""",
						"Unsupported file found",
						JOptionPane.OK_CANCEL_OPTION,
						JOptionPane.WARNING_MESSAGE
				);
				
				if (returnVal == JOptionPane.OK_OPTION) {
					showUnsupportedExtensionDialog = false;
					
					continue;
				}
				
				return false;
			}
			
			if (firstFileExtension.isEmpty()) {
				firstFileExtension = fileExtension;
				
				inputFiles.add(file);
				
				continue;
			}
			
			if (showMultipleExtensionsDialog && !firstFileExtension.equals(fileExtension)) {
				// multiple file extensions found, warn user
				int returnVal = JOptionPane.showConfirmDialog(
						null,
						"""
								Files with multiple types were found. Would you like to continue regardless?
								""",
						"Multiple file extensions found",
						JOptionPane.OK_CANCEL_OPTION,
						JOptionPane.WARNING_MESSAGE
				);
				
				if (returnVal == JOptionPane.OK_OPTION) {
					showMultipleExtensionsDialog = false;
				} else {
					return false;
				}
			}
			
			inputFiles.add(file);
		}
		
		return true;
	}
}
