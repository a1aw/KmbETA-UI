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

import com.mob41.kmbeta.api.ArrivalManager;

import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Color;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;
import java.util.Arrays;
import java.awt.event.ActionEvent;

public class AddMonitorPanel extends JPanel {
	private JComboBox busBox;
	private JComboBox boundBox;
	private JComboBox stopBox;
	private JLabel lblImg;

	/**
	 * Create the panel.
	 */
	public AddMonitorPanel() {
		
		if (ArrivalManager.getBusStopPair() == null){
			boolean loaded = ArrivalManager.loadDatabase(this, true);
			if (!loaded){
				JOptionPane.showMessageDialog(this, "Could not load database. Please fix this problem before using.\nApplication will now close.", "Error", JOptionPane.ERROR_MESSAGE);
				System.exit(-1);
			}
		}
		
		JLabel lblBusNo = new JLabel("Bus No:");
		lblBusNo.setHorizontalAlignment(SwingConstants.RIGHT);
		
		busBox = new JComboBox(ArrivalManager.BUS_NO);
		busBox.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				if (busBox.getSelectedIndex() == -1 || busBox.getSelectedItem() == null){
					return;
				}
				int bounds = ArrivalManager.getBusStopPair().get(busBox.getSelectedIndex()).size();
				System.out.println(bounds);
				boundBox.removeAllItems();
				for (int i = 0; i < bounds; i++){
					System.out.println(" == GEN == ");
					int stopsize = ArrivalManager.getBusStopPair().get(busBox.getSelectedIndex()).get(i).size();
					String firststop = ArrivalManager.getBusStopPair().get(busBox.getSelectedIndex()).get(i).get(0)[4];
					String laststop = ArrivalManager.getBusStopPair().get(busBox.getSelectedIndex()).get(i).get(stopsize - 1)[4];
					boundBox.addItem((i + 1) + ": " + firststop + " --> " + laststop);
					System.out.println((i + 1) + ": " + firststop + " --> " + laststop);
					System.out.println(" == END GEN == ");
				}
			}
		});
		
		JLabel lblBound = new JLabel("Bound:");
		lblBound.setHorizontalAlignment(SwingConstants.RIGHT);
		
		boundBox = new JComboBox();
		boundBox.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				if (boundBox.getSelectedIndex() == -1 || boundBox.getSelectedItem() == null){
					return;
				}
				int stops = ArrivalManager.getBusStopPair().get(busBox.getSelectedIndex()).get(boundBox.getSelectedIndex()).size();
				System.out.println("======= SELECTED BOUND: " + boundBox.getSelectedIndex());
				stopBox.removeAllItems();
				System.out.println("START GEN----------------");
				for (int i = 0; i < stops; i++){
					System.out.println("== GEN stop ==");
					String[] data = ArrivalManager.getBusStopPair().get(busBox.getSelectedIndex()).get(boundBox.getSelectedIndex()).get(i);
					System.out.println(Arrays.deepToString(data));
					System.out.println("Seq: " + data[3]);
					String stopname = data[4];
					System.out.println("Name: " + stopname);
					String stopcode = data[2];
					System.out.println("Code: " + stopcode);
					String bound = data[1];
					System.out.println("Bound: " + bound);
					String text = data[3] + ": " + stopname + " (" + stopcode + ")";
					System.out.println(text);
					stopBox.addItem(text);
					System.out.println("== END GEN stop ==");
				}
			}
		});
		
		JLabel lblStop = new JLabel("Stop:");
		lblStop.setHorizontalAlignment(SwingConstants.RIGHT);
		
		stopBox = new JComboBox();
		stopBox.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				if (stopBox.getSelectedIndex() == -1 || stopBox.getSelectedItem() == null){
					return;
				}
				String stopcode = ArrivalManager.getBusStopPair().get(busBox.getSelectedIndex()).get(boundBox.getSelectedIndex()).get(stopBox.getSelectedIndex())[2];
				lblImg.setText("");
				BufferedImage image = grantImage(stopcode);
				lblImg.setIcon(new ImageIcon(resize(image, 340, 255)));
			}
		});
		
		lblImg = new JLabel("KmbETA");
		lblImg.setForeground(Color.RED);
		lblImg.setFont(new Font("Tahoma", Font.PLAIN, 68));
		lblImg.setHorizontalAlignment(SwingConstants.CENTER);
		GroupLayout groupLayout = new GroupLayout(this);
		groupLayout.setHorizontalGroup(
			groupLayout.createParallelGroup(Alignment.LEADING)
				.addGroup(groupLayout.createSequentialGroup()
					.addContainerGap()
					.addGroup(groupLayout.createParallelGroup(Alignment.TRAILING)
						.addComponent(lblImg, GroupLayout.DEFAULT_SIZE, 458, Short.MAX_VALUE)
						.addGroup(groupLayout.createSequentialGroup()
							.addGroup(groupLayout.createParallelGroup(Alignment.LEADING)
								.addComponent(lblBusNo, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
								.addComponent(lblBound, GroupLayout.DEFAULT_SIZE, 37, Short.MAX_VALUE)
								.addComponent(lblStop, GroupLayout.DEFAULT_SIZE, 37, Short.MAX_VALUE))
							.addPreferredGap(ComponentPlacement.RELATED)
							.addGroup(groupLayout.createParallelGroup(Alignment.LEADING)
								.addComponent(stopBox, 0, 417, Short.MAX_VALUE)
								.addComponent(busBox, Alignment.TRAILING, 0, 417, Short.MAX_VALUE)
								.addComponent(boundBox, 0, 417, Short.MAX_VALUE))))
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
					.addComponent(lblImg, GroupLayout.DEFAULT_SIZE, 326, Short.MAX_VALUE)
					.addContainerGap())
		);
		setLayout(groupLayout);
		
		busBox.setSelectedIndex(0);
		
	}
	
	public int getSelectedBusIndex(){
		return busBox.getSelectedIndex();
	}
	
	public int getSelectedBoundIndex(){
		return boundBox.getSelectedIndex();
	}
	
	public int getSelectedStopIndex(){
		return stopBox.getSelectedIndex();
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
}
