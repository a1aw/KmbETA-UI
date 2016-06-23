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
import java.io.IOException;
import java.net.URL;
import java.util.Arrays;
import java.util.Calendar;

import javax.swing.LayoutStyle.ComponentPlacement;
import java.awt.Color;
import java.awt.Component;

import javax.swing.SwingConstants;
import javax.swing.Timer;
import javax.swing.JPanel;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableModel;

import com.mob41.kmbeta.api.ArrivalManager;
import com.mob41.kmbeta.api.MultiArrivalManager;
import com.mob41.kmbeta.exception.CouldNotLoadDatabaseException;
import com.mob41.kmbeta.exception.InvalidArrivalTargetException;
import com.mob41.kmbeta.exception.InvalidException;

import javax.swing.JButton;
import javax.swing.JProgressBar;
import java.awt.FlowLayout;
import java.awt.BorderLayout;
import javax.swing.JCheckBox;
import javax.swing.JScrollPane;
import javax.swing.JTable;

public class UI {
	
	private static final String[] colident = {"Icon", "Name", "Location", "Arrival", "ETA"};
	
	private MultiArrivalManager mularr;

	private JFrame frmKmbeta;
	
	private Timer textTimer;
	
	private Timer updateTimer;
	
	private int textIndex = 50;
	
	private String[] slideText = null;

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
	
	private ActionListener update = new ActionListener(){

		public void actionPerformed(ActionEvent arg0) {
			System.out.println("Updating! " + Calendar.getInstance().getTimeInMillis());
			mularr.fetchAllData();
			for (int i = 0; i < monitorTable.getRowCount(); i++){
				ArrivalManager arrman = mularr.getArrivalManagers().get(i);
				try {
					monitorTableModel.setValueAt(arrman.getArrivalTime_Formatted(), i, 3);
					monitorTableModel.setValueAt(arrman.getArrivalTimeRemaining_Formatted(), i, 4);
				} catch (Exception e){
					JOptionPane.showMessageDialog(frmKmbeta, "Error occurred: " + e, "Error", JOptionPane.ERROR_MESSAGE);
					e.printStackTrace();
					return;
				}
			}
			monitorTableModel.fireTableDataChanged();
			updateRowHeights(monitorTable);
		}
		
	};
	
	private JLabel lblKmbeta;
	private JPanel statusPanel;
	private JTable monitorTable;
	private DefaultTableModel monitorTableModel;
	
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
		
		JLabel lblVersionV = new JLabel("App Ver: v2.0  Internal Database Ver: v0.2 API Ver: v0.2");
		lblVersionV.setHorizontalAlignment(SwingConstants.CENTER);
		
		lblKmbeta = new JLabel("KmbETA");
		lblKmbeta.setHorizontalAlignment(SwingConstants.CENTER);
		lblKmbeta.setForeground(Color.RED);
		lblKmbeta.setFont(new Font("Tahoma", Font.PLAIN, 33));
		
		JPanel decPanel2 = new JPanel();
		decPanel2.setBackground(Color.RED);
		
		JPanel monitorListPanel = new JPanel();
		monitorListPanel.setBorder(new TitledBorder(null, "Monitoring", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		
		JButton btnAddNewMonitor = new JButton("Add new monitor");
		btnAddNewMonitor.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				AddMonitorPanel pane = new AddMonitorPanel();
				int result = JOptionPane.showOptionDialog(frmKmbeta, pane, "Add new monitor", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE, null, new String[]{"OK", "Cancel"}, 1);
				if (result != 0){
					return;
				}
				String busno = ArrivalManager.BUS_NO[pane.getSelectedBusIndex()];
				String[] stopdata = ArrivalManager.getBusStopPair().get(pane.getSelectedBusIndex()).get(pane.getSelectedBoundIndex()).get(pane.getSelectedStopIndex());
				ArrivalManager arrman = null;
				try {
					arrman = new ArrivalManager(busno, stopdata[2], pane.getSelectedBoundIndex() + 1, ArrivalManager.ENGLISH_LANG, true);
				} catch (Exception e) {
					JOptionPane.showMessageDialog(frmKmbeta, "Error occurred: " + e, "Error", JOptionPane.ERROR_MESSAGE);
					e.printStackTrace();
					return;
				}
				mularr.addArrivalManager(arrman);
				mularr.fetchAllData();
				arrman.getServerTime();
				BufferedImage image = grantImage(stopdata[2]);
				image = resize(image, 135, 75);
				Object[] data = new Object[5];
				try {
					data[0] = new ImageIcon(image);
					data[1] = busno;
					data[2] = stopdata[4];
					data[3] = arrman.getArrivalTime_Formatted();
					data[4] = arrman.getArrivalTimeRemaining_Formatted();
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
		
		JButton btnRemoveMonitor = new JButton("Remove monitor");
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
								.addComponent(lblVersionV, GroupLayout.DEFAULT_SIZE, 574, Short.MAX_VALUE)))
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
							.addComponent(lblVersionV)
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
		
		JScrollPane monitorListScroll = new JScrollPane();
		GroupLayout gl_monitorListPanel = new GroupLayout(monitorListPanel);
		gl_monitorListPanel.setHorizontalGroup(
			gl_monitorListPanel.createParallelGroup(Alignment.LEADING)
				.addGroup(gl_monitorListPanel.createSequentialGroup()
					.addGap(2)
					.addComponent(monitorListScroll, GroupLayout.DEFAULT_SIZE, 613, Short.MAX_VALUE))
		);
		gl_monitorListPanel.setVerticalGroup(
			gl_monitorListPanel.createParallelGroup(Alignment.LEADING)
				.addGroup(gl_monitorListPanel.createSequentialGroup()
					.addGap(2)
					.addComponent(monitorListScroll, GroupLayout.DEFAULT_SIZE, 241, Short.MAX_VALUE))
		);
		
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
		monitorListScroll.setViewportView(monitorTable);
		monitorListPanel.setLayout(gl_monitorListPanel);
		decPanel1.setLayout(new BorderLayout(0, 0));
		
		statusPanel = new JPanel();
		statusPanel.setBackground(Color.RED);
		decPanel1.add(statusPanel, BorderLayout.NORTH);
		
		JLabel lblStatus = new JLabel("Status: None");
		lblStatus.setFont(new Font("Tahoma", Font.BOLD, 11));
		lblStatus.setForeground(Color.WHITE);
		statusPanel.add(lblStatus);
		
		JProgressBar progressBar = new JProgressBar();
		statusPanel.add(progressBar);
		frmKmbeta.getContentPane().setLayout(groupLayout);
		
		textTimer = new Timer(250, textChange);
		textTimer.start();
		
		statusPanel.setVisible(false);
		updateTimer = new Timer(10000, update);
		updateTimer.start();
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
