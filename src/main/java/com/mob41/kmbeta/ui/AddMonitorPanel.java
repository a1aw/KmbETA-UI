package com.mob41.kmbeta.ui;

import javax.swing.JPanel;
import javax.imageio.ImageIO;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JComboBox;
import javax.swing.LayoutStyle.ComponentPlacement;
import javax.swing.SwingConstants;

import com.github.mob41.kmbeta.api.ArrivalManager;
import com.github.mob41.kmbeta.api.BusDatabase;
import com.github.mob41.kmbeta.api.BusStop;
import com.github.mob41.kmbeta.api.Route;
import com.github.mob41.kmbeta.api.RouteBound;

import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Color;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;
import java.util.Arrays;
import java.util.List;
import java.awt.event.ActionEvent;
import java.awt.FlowLayout;
import javax.swing.JProgressBar;

public class AddMonitorPanel extends JPanel {
	
	private static final String SELECT_NONE = "--- Select ---";
	private JComboBox busBox;
	private JComboBox boundBox;
	private JComboBox stopBox;
	private JLabel lblImg;
	private BusDatabase busDb;
	private JLabel lblStatus;
	private JProgressBar progressBar;
	private JPanel statusPanel;
	
	private int language;

	/**
	 * Create the panel.
	 */
	public AddMonitorPanel(int lang) {
		this.language = lang;
		busDb = ArrivalManager.getBusDatabase();
		if (busDb.getRoutes() == null){
			boolean loaded = busDb.loadWebDB();
			if (!loaded){
				JOptionPane.showMessageDialog(this, "Could not download database from \"db.kmbeta.ml\". Check your network connection.\nIf this problem still exists, \nApplication will now close.", "Error", JOptionPane.ERROR_MESSAGE);
				System.exit(-1);
			}
		}
		
		JLabel lblBusNo = new JLabel("Bus No:");
		lblBusNo.setHorizontalAlignment(SwingConstants.RIGHT);
		
		busBox = new JComboBox(arrayAppend(busDb.getRoutesNames(), SELECT_NONE, true));
		busBox.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				stopBox.removeAllItems();
				boundBox.removeAllItems();
				if (busBox.getSelectedIndex() == -1 || busBox.getSelectedItem() == null || busBox.getSelectedItem().equals(SELECT_NONE)){
					return;
				}
				
				List<RouteBound> bl = busDb
						.getRoutes()
						.get(busBox.getSelectedIndex() - 1)
						.getList();
				
				int bounds = bl.size();
				
				stopBox.removeAllItems();
				boundBox.removeAllItems();
				boundBox.addItem(SELECT_NONE);
				List<BusStop> stops;
				for (int i = 0; i < bounds; i++){
					stops = bl.get(i).getList();
					int stopsize = stops.size();
					String firststop = language == 0 ? stops.get(0).getStopNameInEnglish() : stops.get(0).getAddressInChinese();
					String laststop = stops.get(stops.size() - 1).getAddressInEnglish();
					boundBox.addItem((i + 1) + ": " + firststop + " --> " + laststop);
				}
			}
		});
		
		JLabel lblBound = new JLabel("Bound:");
		lblBound.setHorizontalAlignment(SwingConstants.RIGHT);
		
		boundBox = new JComboBox();
		boundBox.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				stopBox.removeAllItems();
				if (boundBox.getSelectedIndex() == -1 || boundBox.getSelectedItem() == null || boundBox.getSelectedItem().equals(SELECT_NONE)){
					return;
				}
				
				List<BusStop> sl = busDb
						.getRoutes()
						.get(busBox.getSelectedIndex() - 1)
						.getList()
						.get(boundBox.getSelectedIndex() - 1)
						.getList();
				
				int stops = sl.size();
				stopBox.addItem(SELECT_NONE);
				
				BusStop stop;
				for (int i = 0; i < stops; i++){
					stop = sl.get(i);
					String text = stop.getStopSeq() + ": " + (language == 0 ? stop.getStopNameInEnglish() : stop.getAddressInChinese()) + " (" + stop.getStopCode() + ")";
					stopBox.addItem(text);
				}
			}
		});
		
		JLabel lblStop = new JLabel("Stop:");
		lblStop.setHorizontalAlignment(SwingConstants.RIGHT);
		
		stopBox = new JComboBox();
		stopBox.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				lblImg.setIcon(null);
				lblImg.setText("KmbETA");
				if (stopBox.getSelectedIndex() == -1 || stopBox.getSelectedItem() == null || stopBox.getSelectedItem().equals(SELECT_NONE)){
					return;
				}
				
				BusStop stop = busDb
						.getRoutes()
						.get(busBox.getSelectedIndex() - 1)
						.getList()
						.get(boundBox.getSelectedIndex() - 1)
						.getList()
						.get(stopBox.getSelectedIndex() - 1);
				
				final String stopcode = stop.getStopCode();
				lblImg.setText("");
				
				busBox.setEnabled(false);
				boundBox.setEnabled(false);
				stopBox.setEnabled(false);
				
				new Thread(){
					public void run(){
						lblStatus.setText("Status: Downloading image...");
						progressBar.setIndeterminate(true);
						statusPanel.setVisible(true);
						
						lblImg.setIcon(null);
						lblImg.setText("...");
						
						BufferedImage image = grantImage(stopcode);
						if (image == null){
							lblImg.setText("No image");
						} else {
							lblImg.setText("");
							lblImg.setIcon(new ImageIcon(resize(image, 340, 255)));
						}
						
						lblStatus.setText("Status: Ready");
						progressBar.setIndeterminate(false);
						
						busBox.setEnabled(true);
						boundBox.setEnabled(true);
						stopBox.setEnabled(true);
					}
				}.start();
			}
		});
		
		lblImg = new JLabel("KmbETA");
		lblImg.setForeground(Color.RED);
		lblImg.setFont(new Font("Tahoma", Font.PLAIN, 68));
		lblImg.setHorizontalAlignment(SwingConstants.CENTER);
		
		statusPanel = new JPanel();
		statusPanel.setVisible(false);
		GroupLayout groupLayout = new GroupLayout(this);
		groupLayout.setHorizontalGroup(
			groupLayout.createParallelGroup(Alignment.LEADING)
				.addGroup(groupLayout.createSequentialGroup()
					.addContainerGap()
					.addGroup(groupLayout.createParallelGroup(Alignment.LEADING)
						.addComponent(lblImg, Alignment.TRAILING, GroupLayout.DEFAULT_SIZE, 430, Short.MAX_VALUE)
						.addGroup(groupLayout.createSequentialGroup()
							.addGroup(groupLayout.createParallelGroup(Alignment.LEADING)
								.addComponent(lblBusNo, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
								.addComponent(lblBound, GroupLayout.DEFAULT_SIZE, 38, Short.MAX_VALUE)
								.addComponent(lblStop, GroupLayout.DEFAULT_SIZE, 38, Short.MAX_VALUE))
							.addPreferredGap(ComponentPlacement.RELATED)
							.addGroup(groupLayout.createParallelGroup(Alignment.LEADING)
								.addComponent(stopBox, 0, 388, Short.MAX_VALUE)
								.addComponent(busBox, Alignment.TRAILING, 0, 388, Short.MAX_VALUE)
								.addComponent(boundBox, 0, 388, Short.MAX_VALUE)))
						.addComponent(statusPanel, GroupLayout.DEFAULT_SIZE, 430, Short.MAX_VALUE))
					.addContainerGap())
		);
		groupLayout.setVerticalGroup(
			groupLayout.createParallelGroup(Alignment.LEADING)
				.addGroup(groupLayout.createSequentialGroup()
					.addContainerGap()
					.addGroup(groupLayout.createParallelGroup(Alignment.BASELINE)
						.addComponent(lblBusNo)
						.addComponent(busBox, GroupLayout.DEFAULT_SIZE, 29, Short.MAX_VALUE))
					.addPreferredGap(ComponentPlacement.RELATED)
					.addGroup(groupLayout.createParallelGroup(Alignment.BASELINE)
						.addComponent(lblBound)
						.addComponent(boundBox, GroupLayout.PREFERRED_SIZE, 29, GroupLayout.PREFERRED_SIZE))
					.addPreferredGap(ComponentPlacement.RELATED)
					.addGroup(groupLayout.createParallelGroup(Alignment.BASELINE)
						.addComponent(lblStop)
						.addComponent(stopBox, GroupLayout.DEFAULT_SIZE, 29, Short.MAX_VALUE))
					.addPreferredGap(ComponentPlacement.RELATED)
					.addComponent(lblImg, GroupLayout.DEFAULT_SIZE, 327, Short.MAX_VALUE)
					.addPreferredGap(ComponentPlacement.RELATED)
					.addComponent(statusPanel, GroupLayout.DEFAULT_SIZE, 25, Short.MAX_VALUE)
					.addContainerGap())
		);
		statusPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 5, 5));
		
		lblStatus = new JLabel("Status: None");
		statusPanel.add(lblStatus);
		
		progressBar = new JProgressBar();
		statusPanel.add(progressBar);
		setLayout(groupLayout);
		
	}
	
	public int getSelectedBusIndex(){
		return busBox.getSelectedIndex() != -1 ? busBox.getSelectedIndex() - 1 : -1;
	}
	
	public int getSelectedBoundIndex(){
		return boundBox.getSelectedIndex() != -1 ? boundBox.getSelectedIndex() - 1 : -1;
	}
	
	public int getSelectedStopIndex(){
		return stopBox.getSelectedIndex() != -1 ? stopBox.getSelectedIndex() - 1 : -1;
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
	
	public static BufferedImage resize(BufferedImage img, int newW, int newH) { 
	    Image tmp = img.getScaledInstance(newW, newH, Image.SCALE_SMOOTH);
	    BufferedImage dimg = new BufferedImage(newW, newH, BufferedImage.TYPE_INT_ARGB);

	    Graphics2D g2d = dimg.createGraphics();
	    g2d.drawImage(tmp, 0, 0, null);
	    g2d.dispose();

	    return dimg;
	} 
	
	public static String[] arrayAppend(String[] original, String appendStr, boolean appendAtFront){
		if (original == null){
			return new String[]{appendStr};
		} else if (appendStr == null){
			return original;
		}
		
		String[] out = new String[original.length + 1];
		
		if (appendAtFront){
			out[0] = appendStr;
			for (int i = 0; i < original.length; i++){
				out[i + 1] = original[i];
			}
		} else {
			for (int i = 0; i < original.length; i++){
				out[i] = original[i];
			}
			out[out.length - 1] = appendStr;
		}
		
		return out;
	}
}
