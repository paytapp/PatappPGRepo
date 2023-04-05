package com.paymentgateway.commons.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class ExcelReaderUtils {

	private static Logger logger = LoggerFactory.getLogger(ExcelReaderUtils.class.getName());

	public List<String> readCsvFile(File file) throws IOException {
		List<String> csvData = new ArrayList<>();
		BufferedReader br = null;
		try {
			String line = "";
			br = new BufferedReader(new FileReader(file));
			while ((line = br.readLine()) != null) {
				csvData.add(line);
			}
			return csvData;

		} catch (Exception e) {
			logger.error("exception in readCsvFile() ", e);
			return csvData;
		} finally {
			if (br != null)
				br.close();
		}

	}

	public List<String> readXlsxFile(File file) {
		List<String> data = new ArrayList<String>();
		int totalColCount = 0;
		try {

			FileInputStream fis = new FileInputStream(file);
			XSSFWorkbook myWorkBook = new XSSFWorkbook(fis);
			XSSFSheet mySheet = myWorkBook.getSheetAt(0);

			Iterator<Row> rowIterator = mySheet.iterator();
			while (rowIterator.hasNext()) {

				StringBuilder rowString = new StringBuilder();
				Row row = rowIterator.next();
				int j = 0;

				if (row.getRowNum() == 0) {
					totalColCount = row.getLastCellNum();
				}

				/* Iterator<Cell> cellIterator = row.iterator(); */
				while (j < totalColCount) {
					/* while (cellIterator.hasNext()) { */

					rowString.append(",");

					Cell cell = row.getCell(j);
					// Cell cell = cellIterator.next();
					if (cell == null) {
						rowString.append("");
						j++;
						continue;
					}
					switch (cell.getCellType()) {
					case Cell.CELL_TYPE_STRING:
						rowString.append(cell.getStringCellValue());
						break;
					case Cell.CELL_TYPE_NUMERIC:
						if (DateUtil.isCellDateFormatted(cell)) {
							SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy");
							rowString.append(dateFormat.format(cell.getDateCellValue()));
						} else {
							Double doubleValue = cell.getNumericCellValue();
							BigDecimal bd = new BigDecimal(doubleValue.toString());
							long lonVal = bd.longValue();
							String number = Long.toString(lonVal).trim();
							rowString.append(number);
						}
						break;
					case Cell.CELL_TYPE_BOOLEAN:
						rowString.append(String.valueOf(cell.getBooleanCellValue()));
						break;
					case Cell.CELL_TYPE_BLANK:
						rowString.append("");
						break;
					default:
						rowString.append(",");
						break;
					}

					j++;
				}
				rowString.deleteCharAt(0);
				data.add(rowString.toString());

			}
		} catch (Exception e) {

			logger.error("exception ", e);
			return data;
		}

		return data;

	}
}
