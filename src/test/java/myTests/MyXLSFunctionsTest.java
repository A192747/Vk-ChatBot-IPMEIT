package myTests;

import junit.framework.TestCase;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Workbook;

import java.io.*;

public class MyXLSFunctionsTest extends TestCase {
    /*MyXLSFunctions xlsFunc = new MyXLSFunctions();
    @Override
    protected void setUp() throws Exception {
        super.setUp();
    }
    public void testGetSubsCount() {
        int actualValue = 0;
        int expected = 3;
        try {
            actualValue = xlsFunc.getSubsCount();
        } catch (IOException e) {
            e.printStackTrace();
        }
        assertEquals(expected, actualValue);
    }

    public void testDoesUserHaveSigned() {
        boolean actual0 = false;
        boolean actual1 = false;
        boolean actual2 = false;
        boolean actual3 = false;
        try{
            actual0 = xlsFunc.doesUserHaveSigned(321529641); //Петя
            actual1 = xlsFunc.doesUserHaveSigned(363563438); //Егор
            actual2 = xlsFunc.doesUserHaveSigned(365108180); //Леша
            actual3 = xlsFunc.doesUserHaveSigned(446121748); //Вика
        } catch (IOException e) {
            e.printStackTrace();
        }
        assertEquals(true, actual0);
        assertEquals(true, actual1);
        assertEquals(false, actual2);
        assertEquals(true, actual3);
    }

    public void testAddUserId() {
        boolean actual = false;
        int actualValue = 0;

        try{
            xlsFunc.addUserId(1111);
            actualValue = xlsFunc.getSubsCount();
            if(actualValue == 4)
                actual = true;
            removeLastAddedUser(1111);
        } catch (IOException e) {
            e.printStackTrace();
        }

        assertEquals(actual, true);
    }

    public void testGiveSign(){
        boolean actual0 = true;
        boolean actual1 = false;
        int actualValue = 0;

        try{
            xlsFunc.giveSign(321529641, 0);
            actual0 = xlsFunc.doesUserHaveSigned(321529641);
            xlsFunc.giveSign(321529641, 1);
            actual1 = xlsFunc.doesUserHaveSigned(321529641);
        } catch (IOException e) {
            e.printStackTrace();
        }

        assertEquals(actual0, false);
        assertEquals(actual1, true);
    }

    public void testRememberGroup(){
        boolean actual0 = true;
        boolean actual1 = false;
        int actualValue = 0;

        try{
            xlsFunc.rememberGroup(321529641, "123123123");
            actual0 = xlsFunc.hasGroupNumber(321529641);
            actual1 = xlsFunc.getGroupNumder(321529641).equals("123123123");

        } catch (IOException e) {
            e.printStackTrace();
        }

        assertEquals(actual0, true);
        assertEquals(actual1, true);
    }

    public void removeLastAddedUser(int userId) throws IOException {
        int i = 0;
        FileInputStream fis = new FileInputStream("workFiles/users.xls"); //читаем из файла
        Workbook wb = new HSSFWorkbook(fis);
        while(true) { //доходим до пустой строки
            if(wb.getSheetAt(0).getRow(i) != null) {
                if (wb.getSheetAt(0).getRow(i).getCell(0).getCellTypeEnum() != CellType.STRING) {
                    if (wb.getSheetAt(0).getRow(i).getCell(0).getNumericCellValue() == userId) {
                        wb.getSheetAt(0).removeRow(wb.getSheetAt(0).getRow(i));
                        FileOutputStream out = new FileOutputStream("workFiles/users.xls");
                        wb.write(out);
                        fis.close();
                        wb.close();
                        break;
                    }
                }
                i++;
            }
            else {
                fis.close();
                wb.close();
                break;
            }
        }
    }

    //testSendNewsToEveryone отсутствует


    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
    }*/
}
