package com.edigley.cloudsim.util;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class BufferedWriterUtils {

	public static BufferedWriter createBufferedWriter(File utilizationFile) {
		try {
			if (utilizationFile != null) {
				return new BufferedWriter(new FileWriter(utilizationFile));
			}
			return null;
		} catch (IOException e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}
	}

	public static void closeBufferedWriter(BufferedWriter bw) {
		try {
			if (bw != null) {
				bw.close();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static void closeFileWriter(FileWriter bw) {
		try {
			if (bw != null) {
				bw.close();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
}
