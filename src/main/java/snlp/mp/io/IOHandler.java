package snlp.mp.io;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
/**
 * Class to handle the input and output operations to files in project
 * @author Nikit
 *
 */
public class IOHandler {
	private String inputFilePath;
	private String outputFilePath;
	private String[] titleArr;
	// Buffered Reader for the file
	private BufferedReader reader;
	private BufferedWriter writer;

	public IOHandler(String inputFilePath, String outputFilePath) throws IOException {
		super();
		this.inputFilePath = inputFilePath;
		this.outputFilePath = outputFilePath;
		reader = new BufferedReader(new InputStreamReader(new FileInputStream(inputFilePath), "utf-8"));
		writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(outputFilePath)));
		this.titleArr = reader.readLine().split("\t");
	}

	// Method to get the next document
	/**
	 * Method to retrieve the next line from the input file
	 * @return next line
	 * @throws IOException
	 */
	public String[] getNextDoc() throws IOException {
		String line = reader.readLine();
		return line==null?null:line.split("\t");
	}
	// Method to write to output file
	/**
	 * Method to write an Id and Result to output file
	 * @param id
	 * @param result
	 * @throws IOException
	 */
	public void writeToFile(String id, boolean result) throws IOException {
		writer.write(id+"\t"+result);
		writer.newLine();
	}
	//Close Methods
	public void closeIO() throws IOException {
		reader.close();
		writer.close();
	}
	// Getter and Setter
	public String getInputFilePath() {
		return inputFilePath;
	}

	public void setInputFilePath(String inputFilePath) {
		this.inputFilePath = inputFilePath;
	}

	public String getOutputFilePath() {
		return outputFilePath;
	}

	public void setOutputFilePath(String outputFilePath) {
		this.outputFilePath = outputFilePath;
	}

	public String[] getTitleArr() {
		return titleArr;
	}

	public void setTitleArr(String[] titleArr) {
		this.titleArr = titleArr;
	}

	public BufferedReader getReader() {
		return reader;
	}

	public void setReader(BufferedReader reader) {
		this.reader = reader;
	}

	public BufferedWriter getWriter() {
		return writer;
	}

	public void setWriter(BufferedWriter writer) {
		this.writer = writer;
	}

}
