package libms.model.orm;

import java.util.List;
import java.util.ArrayList;
import java.math.BigDecimal;
import java.util.regex.Pattern;


/**
 * 书本信息类
 * 对应数据库的 `book_info` 类
 *
 * @author keybrl
 */
public class BookInfo extends Model {
    public String isbn;
    public String name;
    public String author;
    public BigDecimal price;
    public String category;
    public int total;
    public int available;


    public BookInfo(String isbn, String name, String author, BigDecimal price, String category, int total, int available) {
        if (null == isbn) {
            throw new NullPrimaryKey("参数 `isbn` 不能是 `null` ！");
        }
        this.isbn = isbn;
        this.name = null != name ? name : "";
        this.author = null != author ? author : "";
        this.price = null != price ? price : new BigDecimal("0.00");
        this.category = null != category ? category : "";
        this.total = total;
        this.available = available;
    }


    public static boolean checkISBN(String isbn) {
        if (isbn == null) {
            return false;
        }

        String isbnReg =
                "^(?=[0-9X]{10}$|(?=(?:[0-9]+[-]){3})[-0-9X]{13}$|97[89][0-9]{10}$|(?=(?:[0-9]+[-]){4})[-0-9]{17}$)" +
                "(?:97[89][-]?)?[0-9]{1,5}[-]?[0-9]+[-]?[0-9]+[-]?[0-9X]$";
        if (Pattern.matches(isbnReg, isbn)) {

            // 计算校验和
            List<Integer> pureNumISBN = new ArrayList<Integer>();
            for (char ch: isbn.toCharArray()) {
                if (ch >= '0' && ch <= '9') {
                    pureNumISBN.add(ch - '0');
                }
                else if ('X' == ch) {
                    pureNumISBN.add(10);
                }
            }
            if (10 == pureNumISBN.size()) {
                int sum = 0;
                for (int i = 0; i < 9; i++) {
                    sum += (i + 1) * pureNumISBN.get(i);
                }
                return pureNumISBN.get(9) == sum % 11;
            }
            else if (13 == pureNumISBN.size()) {
                int sum = 0;
                for (int i = 0; i < 12; i++) {
                    sum += i % 2 == 0 ? pureNumISBN.get(i) : pureNumISBN.get(i) * 3;
                }
                return 10 - (sum % 10) == pureNumISBN.get(12) || (sum % 10 == 0 && pureNumISBN.get(12) == 0);
            }
            return false;
        }
        return false;
    }
    public static boolean checkName(String name) {
        return name != null && name.length() < 256;
    }
    public static boolean checkAuthor(String author) {
        return author != null && author.length() < 256;
    }
    public static boolean checkPrice(BigDecimal price) {
        return price != null && 2 == price.scale() && price.compareTo(new BigDecimal("99999999999999.99")) <= 0 && price.compareTo(BigDecimal.ZERO) >= 0;
    }
    public static boolean checkCategory(String category) {
        return category != null && category.length() < 16;
    }
}
