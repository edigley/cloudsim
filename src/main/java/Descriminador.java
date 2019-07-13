import java.io.File;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.Scanner;

import org.apache.commons.io.IOUtils;

public class Descriminador {

	public static void main(String[] args) throws IOException {

		File dir = new File("/home/edigley/local/traces/spot_instances/resultados/resultado_500");

		String[] files = dir.list(new FilenameFilter() {

			public boolean accept(File dir, String name) {
				return name.contains("resumo");
			}

		});

		StringBuilder sb = new StringBuilder();

		sb.append("scenario bid region so type ");
		sb.append("submitted finished preempted notStarted success finishedCost preemptedCost totalCost ");
		sb.append("\n");

		for (String fileName : files) {

			File file = new File(dir, fileName);

			String machineDescription = fileName.substring(fileName.indexOf("_trace-") + 7, fileName.indexOf("_workload"));
			String cenario = fileName.substring(fileName.indexOf("workload-") + 9, fileName.indexOf(".txt_resumo"));
			String bid = fileName.substring(4, 7);
			String resto = machineDescription;
			String region = resto.substring(0, resto.indexOf("."));
			resto = resto.substring(resto.indexOf(".") + 1);
			String so = resto.substring(0, resto.indexOf("."));
			resto = resto.substring(resto.indexOf(".") + 1);
			String type = resto.substring(0, resto.lastIndexOf("."));

			sb.append(cenario).append(" ");
			sb.append(bid).append(" ");
			sb.append(region).append(" ");
			sb.append(so).append(" ");
			sb.append(type).append(" ");

			Scanner sc = new Scanner(file);

			sb.append(sc.nextLine()).append("\n");

			sc.close();

		}
		IOUtils.write(sb.toString(), new FileOutputStream("/local/edigley/traces/dataset-all.txt"));

	}
}
