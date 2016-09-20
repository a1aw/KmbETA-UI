package com.mob41.kmbeta.ui;

import java.awt.EventQueue;

import javax.swing.JFrame;
import javax.imageio.ImageIO;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JOptionPane;

import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Arrays;
import java.util.Calendar;

import javax.swing.LayoutStyle.ComponentPlacement;
import java.awt.Color;
import java.awt.Component;
import java.awt.Desktop;

import javax.swing.SwingConstants;
import javax.swing.Timer;
import javax.swing.JPanel;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableModel;

import com.github.mob41.kmbeta.api.ArrivalManager;
import com.github.mob41.kmbeta.api.BusDatabase;
import com.github.mob41.kmbeta.api.BusStop;
import com.github.mob41.kmbeta.api.MultiArrivalManager;
import com.github.mob41.kmbeta.api.Route;

import javax.swing.JButton;
import javax.swing.JProgressBar;
import java.awt.FlowLayout;
import java.awt.BorderLayout;
import javax.swing.JCheckBox;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import java.awt.CardLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class UI {

	private static final String copyright = "Copyright (c) 2016 Anthony Law";
	
	private static final String API_VERSION = "1.0.0-SNAPSHOT";
	
	private static final String[] colident = {"Icon", "Name", "Location", "Arrival", "ETA"};
	
	private MultiArrivalManager mularr;

	private JFrame frmKmbeta;
	
	private Timer textTimer;
	
	private Timer updateTimer;
	
	private int textIndex = 50;
	
	private String[] slideText = null;
	
	private boolean updating = false;

	private ActionListener textChange = new ActionListener(){

		public void actionPerformed(ActionEvent arg0) {
			if (slideText == null){
				slideText = processSlideText("Check Bus ETA on PC!", 6);
			}
			if (textIndex >= slideText.length){
				if (textIndex >= 60){
					textIndex = 0;
				}
				lblKmbeta.setText("KmbETA");
				textIndex++;
				return;
			}
			lblKmbeta.setText(slideText[textIndex]);
			textIndex++;
		}
		
	};
	
	private int language = 0;
	
	private ActionListener update = new ActionListener(){

		public void actionPerformed(ActionEvent arg0) {
			if (!updating){
				System.out.println("Not updating. Start thread update");
				new Thread(){
					public void run(){
						if (!updating){
							updating = true;
							
							if (monitorTable.getRowCount() <= 0){
								updating = false;
								return;
							}
							
							progressBar.setValue(0);
							progressBar.setIndeterminate(false);
							lblStatus.setText("Status: Updating...");
							int per = 100 / monitorTable.getRowCount();
							statusPanel.setVisible(true);
							
							for (int i = 0; i < mularr.getArrivalManagers().size(); i++){
								mularr.getArrivalManagers().get(i).fetchNewData();
								progressBar.setValue(per * (i + 1));
							}
							
							for (int i = 0; i < monitorTable.getRowCount(); i++){
								ArrivalManager arrman = mularr.getArrivalManagers().get(i);
								try {
									monitorTableModel.setValueAt(arrman.getArrivalTimeText(), i, 3);
									monitorTableModel.setValueAt(arrman.getRemainingArrivalMinuteText(), i, 4);
								} catch (Exception e){
									JOptionPane.showMessageDialog(frmKmbeta, "Error occurred: " + e, "Error", JOptionPane.ERROR_MESSAGE);
									e.printStackTrace();
									return;
								}
							}
							monitorTableModel.fireTableDataChanged();
							updateRowHeights(monitorTable);
							
							lblStatus.setText("Status: Ready");
							
							new Thread(){
								public void run(){
									try {
										sleep(1000);
									} catch (InterruptedException ignore){}
									statusPanel.setVisible(false);
								}
							}.start();
							
							System.out.println("Updating success");
							
							updating = false;
						}
					}
				}.start();
			} else {
				System.out.println("Still updating... skipping thread start");
			}
		}
		
	};
	
	private JLabel lblKmbeta;
	private JPanel statusPanel;
	private JTable monitorTable;
	private DefaultTableModel monitorTableModel;
	private JLabel lblStatus;
	private JProgressBar progressBar;
	private JScrollPane monitorListScroll;
	private JPanel blockingPanel;
	private JButton btnAddNewMonitor;
	private JButton btnRemoveMonitor;
	private JLabel lblVersion;
	
	private static String[] processSlideText(String text, int max){
		for (int i = 0; i < max; i++){
			text = " " + text;
		}
		for (int i = 0; i < max; i++){
			text = text + " ";
		}
		int slides = text.length() - 1;
		String[] arr = new String[slides];
		for (int i = 0; i < slides; i++){
			if ((i + max) < text.length()){
				arr[i] = text.substring(i, i + max - 1);
			} else {
				arr[i] = text.substring(i, text.length());
			}
		}
		return arr;
	}

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					UI window = new UI();
					window.frmKmbeta.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the application.
	 */
	public UI() {
		initialize();
	}

	/**
	 * Initialize the contents of the frame.
	 */
	private void initialize() {
		mularr = new MultiArrivalManager(50);
		frmKmbeta = new JFrame();
		frmKmbeta.setTitle("KmbETA");
		frmKmbeta.setBounds(100, 100, 751, 564);
		frmKmbeta.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		JPanel decPanel1 = new JPanel();
		decPanel1.setBackground(Color.RED);
		
		lblVersion = new JLabel(copyright);
		lblVersion.addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent e) {
				try {
					Desktop.getDesktop().browse(new URI("https://github.com/mob41/KmbETA-API"));
				} catch (Exception e1){
					JOptionPane.showMessageDialog(frmKmbeta, "Error when opening URL: " + e1, "Error", JOptionPane.ERROR_MESSAGE);
				}
			}
		});
		lblVersion.setHorizontalAlignment(SwingConstants.CENTER);
		
		lblKmbeta = new JLabel("KmbETA");
		lblKmbeta.setHorizontalAlignment(SwingConstants.CENTER);
		lblKmbeta.setForeground(Color.RED);
		lblKmbeta.setFont(new Font("Tahoma", Font.PLAIN, 33));
		
		JPanel decPanel2 = new JPanel();
		decPanel2.setBackground(Color.RED);
		
		JPanel monitorListPanel = new JPanel();
		monitorListPanel.setBorder(new TitledBorder(null, "Monitoring", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		
		btnAddNewMonitor = new JButton("Add new monitor");
		btnAddNewMonitor.setEnabled(false);
		btnAddNewMonitor.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				AddMonitorPanel pane = new AddMonitorPanel(language);
				
				BusDatabase busDb = ArrivalManager.getBusDatabase();
				
				int result = JOptionPane.showOptionDialog(frmKmbeta, pane, "Add new monitor", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE, null, new String[]{"OK", "Cancel"}, 1);
				if (result != 0 ||
						pane.getSelectedBusIndex() == -1 ||
						pane.getSelectedBoundIndex() == -1 ||
						pane.getSelectedStopIndex() == -1 
						){
					return;
				}
				
				String busno = busDb.getRoutesNames()[pane.getSelectedBusIndex()];
				
				BusStop busStop = busDb.getRoutes()
						.get(pane.getSelectedBusIndex())
						.getBound(pane.getSelectedBoundIndex())
						.getBusStop(pane.getSelectedStopIndex());
				
				ArrivalManager arrman = null;
				try {
					arrman = new ArrivalManager(busno, busStop.getStopCode(), pane.getSelectedBoundIndex(), ArrivalManager.ENGLISH_LANG, true);
				} catch (Exception e) {
					JOptionPane.showMessageDialog(frmKmbeta, "Error occurred: " + e, "Error", JOptionPane.ERROR_MESSAGE);
					e.printStackTrace();
					return;
				}
				mularr.addArrivalManager(arrman);
				arrman.getServerTime();
				BufferedImage image = grantImage(busStop.getStopCode());
				image = resize(image, 135, 75);
				Object[] data = new Object[5];
				try {
					data[0] = new ImageIcon(image);
					data[1] = busno;
					data[2] = language == 0 ? busStop.getStopNameInEnglish() : busStop.getAddressInChinese();
					data[3] = arrman.getArrivalTimeText();
					data[4] = arrman.getRemainingArrivalMinuteText();
				} catch (Exception e){
					JOptionPane.showMessageDialog(frmKmbeta, "Error occurred: " + e, "Error", JOptionPane.ERROR_MESSAGE);
					e.printStackTrace();
					return;
				}
				monitorTableModel.addRow(data);
				monitorTableModel.fireTableDataChanged();
				updateRowHeights(monitorTable);
			}
		});
		
		btnRemoveMonitor = new JButton("Remove monitor");
		btnRemoveMonitor.setEnabled(false);
		btnRemoveMonitor.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				if (monitorTable.getSelectedRow() != -1){
					mularr.removeArrivalManager(monitorTable.getSelectedRow());
					monitorTableModel.removeRow(monitorTable.getSelectedRow());
					monitorTableModel.fireTableDataChanged();
					updateRowHeights(monitorTable);
				} else {
					JOptionPane.showMessageDialog(frmKmbeta, "Please select a monitor to be deleted.", "Error", JOptionPane.ERROR_MESSAGE);
				}
			}
		});
		
		JCheckBox chckbxSpeak = new JCheckBox("Speak");
		chckbxSpeak.setToolTipText("Not implemented");
		chckbxSpeak.setEnabled(false);
		GroupLayout groupLayout = new GroupLayout(frmKmbeta.getContentPane());
		groupLayout.setHorizontalGroup(
			groupLayout.createParallelGroup(Alignment.LEADING)
				.addGroup(groupLayout.createSequentialGroup()
					.addContainerGap()
					.addGroup(groupLayout.createParallelGroup(Alignment.TRAILING)
						.addGroup(groupLayout.createSequentialGroup()
							.addComponent(lblKmbeta, GroupLayout.DEFAULT_SIZE, 135, Short.MAX_VALUE)
							.addPreferredGap(ComponentPlacement.RELATED)
							.addGroup(groupLayout.createParallelGroup(Alignment.LEADING)
								.addComponent(decPanel1, GroupLayout.DEFAULT_SIZE, 574, Short.MAX_VALUE)
								.addComponent(lblVersion, GroupLayout.DEFAULT_SIZE, 574, Short.MAX_VALUE)))
						.addComponent(decPanel2, GroupLayout.DEFAULT_SIZE, 715, Short.MAX_VALUE)
						.addComponent(monitorListPanel, GroupLayout.DEFAULT_SIZE, 715, Short.MAX_VALUE)
						.addGroup(groupLayout.createSequentialGroup()
							.addComponent(btnAddNewMonitor, GroupLayout.PREFERRED_SIZE, 175, Short.MAX_VALUE)
							.addPreferredGap(ComponentPlacement.RELATED)
							.addComponent(btnRemoveMonitor, GroupLayout.PREFERRED_SIZE, 173, Short.MAX_VALUE)
							.addPreferredGap(ComponentPlacement.UNRELATED)
							.addComponent(chckbxSpeak, GroupLayout.PREFERRED_SIZE, 355, GroupLayout.PREFERRED_SIZE)))
					.addContainerGap())
		);
		groupLayout.setVerticalGroup(
			groupLayout.createParallelGroup(Alignment.LEADING)
				.addGroup(groupLayout.createSequentialGroup()
					.addContainerGap()
					.addGroup(groupLayout.createParallelGroup(Alignment.TRAILING)
						.addGroup(groupLayout.createSequentialGroup()
							.addComponent(lblVersion)
							.addPreferredGap(ComponentPlacement.RELATED)
							.addComponent(decPanel1, GroupLayout.PREFERRED_SIZE, 24, GroupLayout.PREFERRED_SIZE))
						.addComponent(lblKmbeta))
					.addPreferredGap(ComponentPlacement.RELATED)
					.addComponent(decPanel2, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
					.addGap(11)
					.addComponent(monitorListPanel, GroupLayout.DEFAULT_SIZE, 403, Short.MAX_VALUE)
					.addPreferredGap(ComponentPlacement.RELATED)
					.addGroup(groupLayout.createParallelGroup(Alignment.TRAILING, false)
						.addComponent(chckbxSpeak, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
						.addComponent(btnRemoveMonitor, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
						.addComponent(btnAddNewMonitor, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
					.addContainerGap())
		);
		
		monitorListScroll = new JScrollPane();
		
		monitorTableModel = new DefaultTableModel();
		monitorTable = new JTable(monitorTableModel){
			
			public boolean isCellEditable(int row, int column){
				return false;
			}
			
			public Class getColumnClass(int column){
                return getValueAt(0, column).getClass();
            }
		};
		monitorTable.setFont(new Font("Tahoma", Font.PLAIN, 27));
		monitorTableModel.setColumnIdentifiers(colident);
		monitorListPanel.setLayout(new CardLayout(0, 0));
		monitorListScroll.setViewportView(monitorTable);
		monitorListPanel.add(monitorListScroll, "name_168879057220579");
		monitorListScroll.setVisible(false);
		
		blockingPanel = new JPanel();
		monitorListPanel.add(blockingPanel, "name_169100630683037");
		blockingPanel.setLayout(new BorderLayout(0, 0));
		blockingPanel.setVisible(true);
		
		JLabel lblJustAWhile = new JLabel("Just a while...");
		lblJustAWhile.setFont(new Font("PMingLiU", Font.PLAIN, 28));
		lblJustAWhile.setHorizontalAlignment(SwingConstants.CENTER);
		blockingPanel.add(lblJustAWhile, BorderLayout.CENTER);
		decPanel1.setLayout(new BorderLayout(0, 0));
		
		statusPanel = new JPanel();
		statusPanel.setVisible(false);
		statusPanel.setBackground(Color.RED);
		decPanel1.add(statusPanel, BorderLayout.NORTH);
		
		lblStatus = new JLabel("Status: None");
		lblStatus.setFont(new Font("Tahoma", Font.BOLD, 11));
		lblStatus.setForeground(Color.WHITE);
		statusPanel.add(lblStatus);
		
		progressBar = new JProgressBar();
		statusPanel.add(progressBar);
		frmKmbeta.getContentPane().setLayout(groupLayout);
		
		textTimer = new Timer(250, textChange);
		textTimer.start();
		updateTimer = new Timer(10000, update);
		updateTimer.start();
		
		EventQueue.invokeLater(new Runnable(){
			public void run(){
				language = JOptionPane.showOptionDialog(frmKmbeta, "Choose your database language (Not UI):", "Database Language chooser", JOptionPane.OK_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE, null, new String[]{"English", "Traditional Chinese"}, 0);
				if (language != 0 && language != 1){
					System.exit(0);
					return;
				}
				new Thread(){
					public void run(){
						lblStatus.setText("Status: Downloading database...");
						progressBar.setValue(50);
						progressBar.setIndeterminate(true);
						statusPanel.setVisible(true);
						
						String prefix = "";
						boolean fileLoaded = false;
						if (new File("kmbeta_db.json").exists()){
							int option = JOptionPane.showConfirmDialog(frmKmbeta, "A database file \"kmbeta_db.json\" is detected. Do you want to load it instead of the web database?\nA web database is more recommended.", "Load from file?", JOptionPane.YES_NO_CANCEL_OPTION);
							if (option == 0){
								if (!ArrivalManager.getBusDatabase().loadDatabase(null, false)){
									JOptionPane.showMessageDialog(frmKmbeta, "Unable to load from file \"kmbeta_db.json\". \nApplication will now close.", "Error", JOptionPane.ERROR_MESSAGE);
									System.exit(-1);
									return;
								} else {
									prefix += "[Offline Database] ";
									fileLoaded = true;
								}
							} else if (option == 1) {
								fileLoaded = false;
							} else {
								System.exit(-1);
								return;
							}
						}
						
						if (!fileLoaded){
							boolean loaded = ArrivalManager.getBusDatabase().loadWebDB();
							if (!loaded){
								int option = JOptionPane.showConfirmDialog(frmKmbeta, "Could not download database from \"db.kmbeta.ml\".\nIf you want to load from file, press \"Yes\".\nOtherwise, the application will close.", "Load from file?", JOptionPane.YES_NO_OPTION);
								if (option == 0){
									if (!ArrivalManager.getBusDatabase().loadDatabase(null, false)){
										JOptionPane.showMessageDialog(frmKmbeta, "Unable to load from file \"kmbeta_db.json\". \nApplication will now close.", "Error", JOptionPane.ERROR_MESSAGE);
										System.exit(-1);
										return;
									}
									prefix += "[Offline Database] ";
								} else {
									System.exit(-1);
									return;
								}
							}
						}
						progressBar.setIndeterminate(false);
						progressBar.setValue(100);
						lblStatus.setText("Status: Ready");
						
						lblVersion.setText(
								prefix + "DB: " + ArrivalManager.getBusDatabase().getPureJSON().getString("generated_human") +
								" API: " + API_VERSION
						);
						
						btnAddNewMonitor.setEnabled(true);
						btnRemoveMonitor.setEnabled(true);
						blockingPanel.setVisible(false);
						monitorListScroll.setVisible(true);
						try {
							sleep(2000);
						} catch (InterruptedException ignore) {}
						statusPanel.setVisible(false);
						
					}
				}.start();
			}
		});
	}
	
	private static BufferedImage grantImage(String stopcode){
		final String urllink = "http://www.kmb.hk/chi/img.php?file=";
		try {
		    URL url = new URL(urllink + convertToSubArea(stopcode));
		    BufferedImage image = ImageIO.read(url);
		    return image;
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	private static String convertToSubArea(String stopcode){
		String first = stopcode.substring(0, 4);
		String second = stopcode.substring(4, 5);
		String third = stopcode.substring(5, 9);
		String forth = stopcode.substring(9, 10);
		return first + "-" + second + "-" + third + "-" + forth;
	}
	
	private void updateRowHeights(JTable table)
	{
	    for (int row = 0; row < table.getRowCount(); row++)
	    {
	        int rowHeight = table.getRowHeight();

	        for (int column = 0; column < table.getColumnCount(); column++)
	        {
	            Component comp = table.prepareRenderer(table.getCellRenderer(row, column), row, column);
	            rowHeight = Math.max(rowHeight, comp.getPreferredSize().height);
	        }

	        table.setRowHeight(row, rowHeight);
	    }
	}
	
	private static BufferedImage resize(BufferedImage img, int newW, int newH) { 
	    Image tmp = img.getScaledInstance(newW, newH, Image.SCALE_SMOOTH);
	    BufferedImage dimg = new BufferedImage(newW, newH, BufferedImage.TYPE_INT_ARGB);

	    Graphics2D g2d = dimg.createGraphics();
	    g2d.drawImage(tmp, 0, 0, null);
	    g2d.dispose();

	    return dimg;
	}  
}
