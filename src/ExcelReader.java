import org.apache.poi.EncryptedDocumentException;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellReference;

import java.io.FileInputStream;
import java.io.IOException;

public class ExcelReader {
    public static void main(String[] args) throws EncryptedDocumentException, InvalidFormatException {
        String filePath = "C:/rahul/files/a.xls";

        try (Workbook workbook = WorkbookFactory.create(new FileInputStream(filePath))) {
            Sheet sheet = workbook.getSheetAt(0); // Assuming you want to read the first sheet

            // Iterate over rows
            for (Row row : sheet) {
                // Iterate over cells in the row
                for (Cell cell : row) {
                    CellReference cellRef = new CellReference(row.getRowNum(), cell.getColumnIndex());
                    System.out.print(cellRef.formatAsString() + " - ");

                    // Check if the cell is part of a group
                    if (cell.isPartOfArrayFormulaGroup()) {
                        System.out.println("Grouped cell: " + cell.toString());
                    } else {
                        System.out.println(cell.toString());
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
