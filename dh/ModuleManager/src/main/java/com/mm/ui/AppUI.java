/**
 * @author selvakumarv
 *
 * 24-03-2021
 *
 *AppUI.java
 */
package com.mm.ui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;

import com.mm.config.Configuration;
import com.mm.config.MMConfig;
import com.mm.service.IModuleManagerProcess;
import com.mm.service.MonitoringComponentService;
import com.mm.service.StartComponentService;
import com.mm.service.StopComponentService;

/**
 * @author selvakumarv
 *
 */
@SuppressWarnings("serial")
public class AppUI extends JFrame implements ActionListener {

	private JMenu filemenu;
	private JMenuItem startM, aliveM, stopM;
	public  Configuration config = null;
	private  MMConfig mmconf = null;

	public AppUI(Configuration configuration, MMConfig mmConfig) {
		super();
		this.config = configuration;
		this.mmconf = mmConfig;

		initUI();
	}

	private void initUI() {

		filemenu = new JMenu("File");

		startM = new JMenuItem("start");
		aliveM = new JMenuItem("Monitoring");
		stopM = new JMenuItem("Stop");

		filemenu.add(startM);
		filemenu.add(aliveM);
		filemenu.add(stopM);

		JMenuBar mbar = new JMenuBar();
		mbar.add(filemenu);

		setJMenuBar(mbar);

		startM.addActionListener(this);
		aliveM.addActionListener(this);
		stopM.addActionListener(this);
		setSize(550, 400);
		setVisible(true);
	}

	@Override
	public void actionPerformed(ActionEvent event) {
		if (event.getSource() == startM) {
			start();
		} else if (event.getSource() == aliveM) {
			monitor();
		} else if (event.getSource() == stopM) {
			stop();
		}

	}

	private void start() {

		IModuleManagerProcess service = new StartComponentService(mmconf);
		service.getLogger(config);
		mmconf.getLocalComponents(mmconf.localComponentSubNode);

		service.startComponents();

	}

	private void monitor() {

		final ScheduledExecutorService ses = Executors.newSingleThreadScheduledExecutor();
		ses.scheduleWithFixedDelay(new Runnable() {

			@Override
			public void run() {
				IModuleManagerProcess monitor = new MonitoringComponentService(mmconf);
				monitor.getLogger(config);
				monitor.isAliveAllComponents();

			}
		}, 0, mmconf.getMonitoringInterval(), TimeUnit.SECONDS);

	}

	private void stop() {
		IModuleManagerProcess stopService = new StopComponentService(mmconf);
		stopService.getLogger(config);
		stopService.stopComponents();
	}

	public static void main(String arg[]) {
		// new AppUI();
	}
}
