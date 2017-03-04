package org.adaptinet.node.processors;

import java.util.Date;
import java.awt.AWTEvent;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.text.DateFormat;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import org.adaptinet.node.processoragent.Processor;


public class ConsoleFrame extends JFrame {
	private static final long serialVersionUID = 5241615895405362957L;

	Processor processor = null;

	JPanel contentPane;

	BorderLayout borderLayout1 = new BorderLayout();

	JPanel jPanel1 = new JPanel();

	JPanel jPanel2 = new JPanel();

	JLabel jLabel1 = new JLabel();

	JComboBox<String> jComboBoxNodes = new JComboBox<String>();

	JButton jButtonPing = new JButton();

	JPanel jPanel3 = new JPanel();

	JButton jButtonShutdown = new JButton();

	JLabel jLabel2 = new JLabel();

	JScrollPane jScrollPane1 = new JScrollPane();

	GridLayout gridLayout1 = new GridLayout();

	JTextArea jTextAreaStatus = new JTextArea();

	/** Construct the frame */
	public ConsoleFrame() {
		enableEvents(AWTEvent.WINDOW_EVENT_MASK);
		try {
			jbInit();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/** Component initialization */
	private void jbInit() throws Exception {
		// setIconImage(Toolkit.getDefaultToolkit().createImage(ConsoleFrame.class.getResource("[Your
		// Icon]")));
		contentPane = (JPanel) this.getContentPane();
		contentPane.setLayout(borderLayout1);
		this.setSize(new Dimension(487, 247));
		this.setTitle("Run-Time Console");
		jLabel1.setText("Node List:");
		jComboBoxNodes.setPreferredSize(new Dimension(175, 20));
		jComboBoxNodes.setEditable(true);
		jButtonPing.setText("Ping");
		jButtonPing.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(ActionEvent e) {
				jButtonPing_actionPerformed(e);
			}
		});
		jButtonShutdown.setText("Shutdown Server");
		jButtonShutdown.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(ActionEvent e) {
				jButtonShutdown_actionPerformed(e);
			}
		});
		jLabel2.setText("  ");
		jPanel3.setLayout(gridLayout1);
		jTextAreaStatus.setMargin(new Insets(5, 5, 5, 5));
		jTextAreaStatus.setEditable(false);
		contentPane.add(jPanel1, BorderLayout.NORTH);
		jPanel1.add(jLabel1, null);
		jPanel1.add(jComboBoxNodes, null);
		jPanel1.add(jLabel2, null);
		jPanel1.add(jButtonPing, null);
		contentPane.add(jPanel2, BorderLayout.SOUTH);
		jPanel2.add(jButtonShutdown, null);
		contentPane.add(jPanel3, BorderLayout.CENTER);
		jPanel3.add(jScrollPane1, null);
		jScrollPane1.getViewport().add(jTextAreaStatus, null);
		addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				processor.shutdown();
			}
		});
	}

	void jButtonPing_actionPerformed(ActionEvent e) {
		((Console) processor).doPing((String) jComboBoxNodes.getSelectedItem());
	}

	void jButtonShutdown_actionPerformed(ActionEvent e) {
		this.setVisible(false);
		processor.shutdown();
	}

	public void init(String title) {
		this.setTitle("Runtime Console - " + title);
		if (jComboBoxNodes.getItemCount() > 0)
			jComboBoxNodes.setSelectedIndex(0);
	}

	public void setProcessor(Processor processor) {
		this.processor = processor;
	}

	public void insertNode(String address) {
		jComboBoxNodes.addItem(address);
	}

	public void clearNodes() {
		jComboBoxNodes.removeAllItems();
	}

	public void appendStatus(String text) {
		DateFormat timeFormat = DateFormat.getTimeInstance(DateFormat.MEDIUM);

		jTextAreaStatus.append("[" + timeFormat.format(new Date()) + "] "
				+ text + "\n");
		jTextAreaStatus.setCaretPosition(jTextAreaStatus.getDocument()
				.getLength());
	}
}
