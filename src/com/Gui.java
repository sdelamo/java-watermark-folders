package com;

import javax.swing.SwingUtilities;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.DecimalFormat;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

public class Gui extends JFrame {
	DecimalFormat formatter = new DecimalFormat("0.00");
	JFrame frame = new JFrame("Añadir Marcas de Agua a todas las imágenes de un directorio");
	public static final JButton button = new JButton("Añadir Marca de Agua a las imágenes");
	public static final JLabel  inputLabel  = new JLabel("Directorio donde estan las imagenes");
	public static final JTextField  inputField  = new JTextField(15);
	public static final JLabel  outputLabel  = new JLabel("Directorio vacio donde guardar las copias con marca de agua");
	public static final JTextField  outputField  = new JTextField(15);
	public static final JLabel  watermarkLabel  = new JLabel("Archivo marca de agua (gif transparente)");
	public static final JTextField  watermarkField  = new JTextField(15);
	public static final JLabel FEEDBACK  = new JLabel("Número de Imágenes: ");

	public int numberOfImages = 0;
	private int imagesProcessed = 0;
	public String input_folder = "";
	public String output_folder = "";
	public String watermark = "";

	public static void main(String[] args) {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				Gui app = new Gui();				
			}
		});
	}

	public Gui()  {
		go();
	}

	public void go() {
		JPanel panel = new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));		
		button.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				Thread queryThread = new Thread() {
					public void run() {
						String input = inputField.getText();
						String output = outputField.getText();
						String watermark = watermarkField.getText();
						if(!input.equals("") && !output.equals("") && !watermark.equals("")) {
							File file = new File(watermark);
							if (!new File(input).isDirectory()) {
								Runnable task = new UpdateJob("El primer campo debe ser un directorio",Color.red);
								SwingUtilities.invokeLater(task);
							} else if (!new File(output).isDirectory()) {
									Runnable task = new UpdateJob("El segundo campo debe ser un directorio",Color.red);
									SwingUtilities.invokeLater(task);
							} else if (new File(output).isDirectory() && new File(output).list().length != 0) {
								Runnable task = new UpdateJob("El segundo campo debe ser un directorio vacio",Color.red);
								SwingUtilities.invokeLater(task);
							} else if(!file.exists()) {
								Runnable task = new UpdateJob("El archivo marca de agua no existe",Color.red);
								SwingUtilities.invokeLater(task);												
							} else if(!file.getName().endsWith(".gif")) {
								Runnable task = new UpdateJob("El archivo marca de agua no es un gif",Color.red);
								SwingUtilities.invokeLater(task);
							} else {							
								watermark(input,output,watermark);
							} 						
						} else if(output.contains(input)) {
							Runnable task = new UpdateJob("El directorio de salida no puede estar dentro del directorio de origen",Color.red);
							SwingUtilities.invokeLater(task);					
						} else {
							Runnable task = new UpdateJob("Todos los parametros son obligatorios",Color.red);
							SwingUtilities.invokeLater(task);					
						}
					}
				};
				queryThread.start();
			}
		});
		panel.add(inputLabel);
		panel.add(inputField);		
		panel.add(outputLabel);
		panel.add(outputField);
		panel.add(watermarkLabel);
		panel.add(watermarkField);		
		panel.add(button);
		panel.add(FEEDBACK);
		frame.getContentPane().add(BorderLayout.WEST,panel);
		frame.setVisible(true);
		frame.pack(); 
	}

	public void watermark(String input,String output,String watermark) {
		this.input_folder = input ;
		this.output_folder =  output;
		this.watermark = watermark;

		File file = new File(output_folder);
		if(!file.exists()) {
			file.mkdir();				
		}
		numberOfImages = countImages(input_folder);
		Runnable task = new UpdateJob(" " + numberOfImages + " ",Color.black);
		SwingUtilities.invokeLater(task);		
		watermarkFolder(input_folder, output_folder,watermark);
	}

	public int getNumberOfImages() {
		if(numberOfImages == 0 && !input_folder.equals("")) {
			numberOfImages = countImages(input_folder);
		}
		return numberOfImages;
	}

	public int countImages(String input) {
		int count = 0; 
		File[] files = new File(input).listFiles();
		for(int x=0;x<files.length;x++) {
			if(files[x].isFile()) {
				String name = files[x].getName();
				if(name.endsWith(".jpg") || name.endsWith(".jpeg") 
						|| name.endsWith(".JPG") || name.endsWith(".JPEG") ) {
					count++;
				}				
			} else if (files[x].isDirectory()) {
				count += countImages(files[x].getAbsolutePath());				
			}
		}
		return count;
	}

	public void watermarkFolder(String input,String output,String watermark_image) {
		//System.out.println("i:" + input + "o:" + output + "w:" +watermark_image);
		File[] files = new File(input).listFiles();
		for(int x=0;x<files.length;x++) {
			if(files[x].isFile()) {
				String name = files[x].getName();
				if(name.endsWith(".jpg") || name.endsWith(".jpeg") 
						|| name.endsWith(".JPG") || name.endsWith(".JPEG") ) {
					watermarkFile(files[x].getAbsolutePath(),output + "/" + name,watermark_image);
				}				
			} else if (files[x].isDirectory()) {
				String output_dir = output + "/" + files[x].getName();
				File file = new File(output_dir);
				file.mkdir();
				watermarkFolder(files[x].getAbsolutePath(), output_dir,watermark_image);
			}
		}
	}

	private void watermarkFile(String srFile, String dtFile, String watermark_file) {
		copyfile(srFile,dtFile);
		File file = new File(watermark_file);
		if(file.exists()) {
			float alpha = 0.5f;			
			ImageWatermark.markImage( dtFile, watermark_file, alpha, ImageWatermark.MARK_RIGHT_BOTTOM);
			//feedback("Se ha añadido la marca de agua a:" + srFile);
			imagesProcessed++;
			double processed = (imagesProcessed / (double) numberOfImages) * 100;
			feedback( formatter.format(processed) + "%");
		} else {
			Runnable task = new UpdateJob("Imagen Marca de Agua no existe",Color.red);
			SwingUtilities.invokeLater(task);			
		}		
	}

	private void copyfile(String srFile, String dtFile){
		try{
			File f1 = new File(srFile);
			File f2 = new File(dtFile);
			InputStream in = new FileInputStream(f1);

			//For Append the file.
			//		      OutputStream out = new FileOutputStream(f2,true);

			//For Overwrite the file.
			OutputStream out = new FileOutputStream(f2);

			byte[] buf = new byte[1024];
			int len;
			while ((len = in.read(buf)) > 0){
				out.write(buf, 0, len);
			}
			in.close();
			out.close();
			//feedback("Se ha copiado la imagen:" + srFile);
		}
		catch(FileNotFoundException ex){
			feedback(ex.getMessage() + " in the specified directory.");
			System.exit(0);
		}
		catch(IOException e){
			feedback(e.getMessage());      
		}
	}

	public void feedback(final String message) {
		String text = "Imágenes:" +  numberOfImages +" Procesado:" + message;
		Runnable task = new UpdateJob(text,Color.black);
		SwingUtilities.invokeLater(task);			

	}

	class UpdateJob implements Runnable {
		private final Color fg;
		private final String msg;
		UpdateJob(String message,Color fg) {
			this.fg = fg;
			this.msg = message;
		}
		public void run() {
			FEEDBACK.setText(msg);
			FEEDBACK.setForeground(fg);
		}
	}

}