package hlj.wordtopdf;

import fr.opensagres.xdocreport.converter.ConverterTypeTo;
import fr.opensagres.xdocreport.converter.ConverterTypeVia;
import fr.opensagres.xdocreport.converter.Options;
import fr.opensagres.xdocreport.core.XDocReportException;
import fr.opensagres.xdocreport.document.IXDocReport;
import fr.opensagres.xdocreport.document.images.FileImageProvider;
import fr.opensagres.xdocreport.document.images.IImageProvider;
import fr.opensagres.xdocreport.document.registry.XDocReportRegistry;
import fr.opensagres.xdocreport.template.IContext;
import fr.opensagres.xdocreport.template.TemplateEngineKind;
import fr.opensagres.xdocreport.template.formatter.FieldsMetadata;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

/**
 * @author HealerJean
 * @ClassName PdfCreate
 * @date 2019/8/20  10:23.
 * @Description
 */
public class PdfCreate {

    public static final String WORD = "word";
    public static final String PERSON = "person";
    public static final String TABLE = "table";
    public static final String IMG = "img";


    public static void main(String[] args) {
        File template = new File("D:/pdf/template.docx");
        File outputFile = new File("D:/pdf/ok_template.pdf");

        InputStream inputStream = null;
        OutputStream outputStream = null;
        try {
            inputStream = new FileInputStream(template);
            IXDocReport report =  XDocReportRegistry.getRegistry().loadReport(inputStream, TemplateEngineKind.Freemarker);
            IContext context = report.createContext();
            FieldsMetadata fieldsMetadata = report.createFieldsMetadata();

            //普通对象
            fieldsMetadata.load(PERSON, Person.class);
            //是否list
            fieldsMetadata.load(TABLE, Table.class, true);
            //图片
            fieldsMetadata.addFieldAsImage("img");
            report.setFieldsMetadata(fieldsMetadata);

            // 1、普通字符
            context.put(WORD, "这是一句话");

            // 2、对象
            Person person = new Person("HealerJean", "25", "男");
            context.put(PERSON, person);

            // 3、表格
            Table table1 = new Table("11", "12", "13");
            Table table2 = new Table("21", "22", "23");
            List<Table> table = new ArrayList<>();
            table.add(table1);
            table.add(table2);
            context.put(TABLE, table);

            // 4、 图片
            // 图片的插入方式和上面的不太相同，首先我们点击图片(必须有一张图片)，选择插入，选择书签，输入一个任意的变量名如 img
            IImageProvider img = new FileImageProvider(new File("D:/pdf/img.png"), true);
            img.setSize(200f, 100f);

            // IImageProvider img = new FileImageProvider(new File("D:/pdf/img.png"));
            context.put("img", img);

            //生成word
            // outputStream = new FileOutputStream(new File("D:/pdf/word.docx"));
            // report.process(context, outputStream);

            //生成pdf
            outputStream = new FileOutputStream(outputFile);
            Options options = Options.getTo(ConverterTypeTo.PDF).via(ConverterTypeVia.XWPF);
            report.convert(context, options, outputStream);

        } catch (IOException | XDocReportException e) {
            throw new RuntimeException(e.getMessage(), e);
        } finally {
            try {
                if (inputStream != null) {
                    inputStream.close();
                }
                if (outputStream != null) {
                    outputStream.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
    }



}
