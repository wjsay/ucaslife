import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.pdf.PdfContentByte;
import com.itextpdf.text.pdf.PdfPage;
import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.PdfWriter;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

public class PdfTools {
    public static void main(String[] args) {
        if (args.length == 0) {
            System.out.println("请输入PDF文件名");
        }
        for (String filename : args) {
            System.out.println(splitPdf(filename) + "\t" + filename);
        }
    }


    /**
     * 将filename文件切分成多个4页大小的文件
     * @param filename
     * @return 将filename文件划分成的子文件数目
     */
    public static int splitPdf(String filename) {
        // String filename = "1904.08394.pdf";
        PdfReader reader = null;
        try {
            reader = new PdfReader(filename);
        } catch (IOException e) {
            return -1;
        }
        int numberOfPages = reader.getNumberOfPages();
        int splitSize = 4;
        int numberOfNewFiles = 0, pageNumber = 1;
        while (pageNumber <= numberOfPages) {
            Document doc = new Document();
            String outputFilename = String.format(filename.substring(0, filename.length()-4) + "_%02d" + ".pdf", numberOfNewFiles);
            PdfWriter writer = null;
            try {
                writer = PdfWriter.getInstance(doc, new FileOutputStream(outputFilename));
            } catch (FileNotFoundException e) {
                return -2 - numberOfNewFiles * 10;
            } catch (DocumentException e) {
                return -3 - numberOfNewFiles * 10;
            }
            doc.open();
            PdfContentByte cb = writer.getDirectContent();
            // 这里判断加到了循环里不好，有优化空间
            for (int j = 1; pageNumber <= numberOfPages && j <= splitSize; ++j, pageNumber++) {
                doc.newPage();
                // 查看源码得知pageNumber是从1开始计数的
                cb.addTemplate(writer.getImportedPage(reader, pageNumber), 0, 0);
            }
            doc.close();
            numberOfNewFiles++;
            writer.close();
        }
        return numberOfNewFiles;
    }
}
