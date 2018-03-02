package com.main;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.util.store.FileDataStoreFactory;

import com.google.api.services.drive.DriveScopes;
import com.google.api.services.drive.model.*;
import com.google.api.services.drive.Drive;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.Collection;
import com.google.api.client.http.FileContent;
import com.google.api.services.drive.model.File;

import java.awt.Desktop;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.*;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.plaf.basic.BasicOptionPaneUI.ButtonActionListener;

public class Main {
	JFrame frm_main;
	JLabel lbl_path;
	JButton btn_choose;
	JButton btn_upload;
	/** Application name. */
	private static final String APPLICATION_NAME = "Drive API Java Quickstart";

	/** Directory to store user credentials for this application. */
	// Property is name of Users
	private static final java.io.File DATA_STORE_DIR = new java.io.File(System.getProperty("user.home"),
			".credentials/drive-java-quickstart");

	/** Global instance of the {@link FileDataStoreFactory}. */
	private static FileDataStoreFactory DATA_STORE_FACTORY;

	/** Global instance of the JSON factory. */
	private static final JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();

	/** Global instance of the HTTP transport. */
	private static HttpTransport HTTP_TRANSPORT;

	// DriveScopes.ALL: tat ca cac quyen
	private static final Collection<String> SCOPES = DriveScopes.all();
	static {
		try {
			HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
			DATA_STORE_FACTORY = new FileDataStoreFactory(DATA_STORE_DIR);
		} catch (Throwable t) {
			t.printStackTrace();
			System.exit(1);
		}
	}

	/**
	 * Creates an authorized Credential object.
	 * 
	 * @return an authorized Credential object.
	 * @throws IOException
	 */
	public static Credential authorize() throws IOException {
		// Load client secrets (File register API).
		InputStream in = Main.class.getResourceAsStream("/client_secret.json");
		GoogleClientSecrets clientSecrets = GoogleClientSecrets.load(JSON_FACTORY, new InputStreamReader(in));

		// Build flow and trigger user authorization request.
		GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(HTTP_TRANSPORT, JSON_FACTORY,
				clientSecrets, SCOPES).setDataStoreFactory(DATA_STORE_FACTORY).setAccessType("offline").build();
		Credential credential = new AuthorizationCodeInstalledApp(flow, new LocalServerReceiver()).authorize("user");
		System.out.println("Credentials saved to " + DATA_STORE_DIR.getAbsolutePath());
		return credential;
	}

	/**
	 * Build and return an authorized Drive client service.
	 * 
	 * @return an authorized Drive client service
	 * @throws IOException
	 */
	public static Drive getDriveService() throws IOException {
		Credential credential = authorize();
		return new Drive.Builder(HTTP_TRANSPORT, JSON_FACTORY, credential).setApplicationName(APPLICATION_NAME).build();
	}

	//contructor
	public Main() {
		prepareGUI();
	}

	public static void main(String[] args) throws IOException {
		Main swingControlDemo = new Main();

	}

	private void prepareGUI() {

		frm_main = new JFrame("Upload Google Drive");
		frm_main.setSize(300, 300);// w & h
		frm_main.setLayout(null);
		frm_main.setVisible(true);
		frm_main.setResizable(false);
		frm_main.setLocationRelativeTo(null);
		frm_main.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		btn_choose = new JButton("Choose");
		btn_choose.setBounds(87, 32, 120, 47);// truc x, truc y, do rong, chieu cao
		btn_choose.addActionListener(ChooseListener);

		btn_upload = new JButton("Upload");
		btn_upload.setBounds(87, 172, 120, 47);// truc x, truc y, do rong, chieu cao
		btn_upload.addActionListener(UploadListener);

		lbl_path = new JLabel("Hello, please choose file to upload!");
		lbl_path.setSize(300, 50);
		lbl_path.setLocation(0, 109);
		lbl_path.setHorizontalAlignment(JLabel.CENTER);

		frm_main.add(lbl_path);
		frm_main.add(btn_choose);
		frm_main.add(btn_upload);
		frm_main.add(lbl_path);

	}

	
	String path;	//Duong dan file
	String fileName;	//Ten file
	String mime;
	//Su kien xu ly lay duong dan file
	private final ActionListener ChooseListener = new ActionListener() {
		@Override
		public void actionPerformed(ActionEvent e) {
			JFileChooser chooseFile = new JFileChooser("%userprofile%\\pictures");
			//chooseFile.setFileFilter(new FileNameExtensionFilter("Image","jpeg","jpg","png","bmp"));
			int result = chooseFile.showOpenDialog(null);
			if (result == JFileChooser.APPROVE_OPTION) {
				try {
					java.io.File selectedFile = chooseFile.getSelectedFile();
					path = selectedFile.getAbsolutePath();
					fileName = selectedFile.getName();
					lbl_path.setText(path);
					mime = Files.probeContentType(Paths.get(fileName));
				} catch (Exception e1) {
					System.out.println(e1);
				}
			}
		}
	};

	//Su kien upload file
	private final ActionListener UploadListener = new ActionListener() {
		@Override
		public void actionPerformed(ActionEvent e) {
			try {
				Drive service = getDriveService();
				File fileMetadata = new File();
				// Select the path to save the file
				fileMetadata.setName(fileName);
				java.io.File filePath = new java.io.File(path);
				FileContent mediaContent = new FileContent(mime, filePath);
				File file = service.files().create(fileMetadata, mediaContent).setFields("id").execute();
				//MessegerBox
				JOptionPane.showMessageDialog(null, "Upload Successfully!\nFile ID: " + file.getId());
				//Mo tab web googleDrive
				Desktop.getDesktop().browse(new URL("https://drive.google.com").toURI());
			} catch (IOException e1) {
				System.out.println(e1);
			} catch (URISyntaxException e1) {
				e1.printStackTrace();
				
			}
		}
	};
}
