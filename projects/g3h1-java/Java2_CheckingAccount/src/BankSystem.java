import java.math.BigDecimal;
import java.nio.charset.Charset;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.ArrayList;
import java.util.Queue;
import java.util.LinkedList;
import java.util.Date;


/**
 * 一个银行系统类
 *
 * @author 罗阳豪 16130120191
 * @author keyboard-w@outlook.com
 */
public class BankSystem {
    /**
     * 程序入口
     * @param args
     */
    public static void main(String[] args) {
        // 初始化银行系统
        BankSystem sys = new BankSystem();

        // 添加账户
        sys.create_account("罗阳豪", false, 0, "159284637asd", new BigDecimal("23.33"));
        sys.create_account("罗阳豪1", true, 10000, "137asd", new BigDecimal("23.33"));
        sys.create_account("罗阳豪2", false, 0, "159287asd", new BigDecimal("0.00"));
        sys.create_account("罗阳豪3", false, 100, "159284637asd", new BigDecimal("0.00"));
        sys.create_account("罗阳豪4", true, 100, "1284637asd", new BigDecimal("23.33"));
        sys.create_account("罗阳豪5", false, 0, "1592637d", new BigDecimal("23.33"));
        // 非法输入
        sys.create_account("罗阳豪6", false, 0, "1592637d", new BigDecimal("-23.33"));
        // 显示账户
        sys.show_accounts();

        // 存款
        sys.deposit(sys.get_account_by_id("CREDIT-000000001"), new BigDecimal("100.23"), "137asd");
        sys.deposit(sys.get_account_by_id("CASH-00000000004"), new BigDecimal("23300.56"), "1592637d");
        // 非法输入
        sys.deposit(sys.get_account_by_id("CASH-00000000003"), new BigDecimal("-233.56"), "159284637asd");
        sys.show_accounts();

        // 取钱
        sys.withdraw(sys.get_account_by_id("CREDIT-000000001"), new BigDecimal("2000.00"), "137asd");
        sys.withdraw(sys.get_account_by_id("CREDIT-000000002"), new BigDecimal("99.00"), "1284637asd");
        sys.withdraw(sys.get_account_by_id("CASH-00000000004"), new BigDecimal("12.23"), "1592637d");
        // 非法输入
        sys.withdraw(sys.get_account_by_id("CASH-00000000004"), new BigDecimal("1200000.23"), "1592637d");
        sys.show_accounts();
    }


    // 内部类定义
    /**
     * 银行账号类，是CashAccount和CreditAccount的父类
     * 实现了银行账号的注册、更名、设改验密码、存取钱等操作
     * 依赖Transactions_List类记录交易流水
     */
    private class BankAccount {
        protected String account_id;        // 卡号
        protected String name;              // 开户人姓名
        protected BigDecimal balance;       // 使用精度为2的定点数存储账户余额，避免浮点运算误差。
        public Transactions_List recorder;  // 交易流水记录器
        private String password;

        protected BankAccount(String account_id, BigDecimal balance) {
            this.account_id = account_id;
            this.name = "Undefined";
            this.balance = balance;
            this.recorder = new Transactions_List();
            this.password = "123456";
            if (this.balance.compareTo(BigDecimal.ZERO) < 0) {
                this.balance = new BigDecimal("0.00");
                throw new IllegalArgumentException("初始余额不能为负数，已将余额置为0.00");
            }
            else if (this.balance.scale() != 2) {
                this.balance = new BigDecimal("0.00");
                throw new IllegalArgumentException("初始余额精度不符合要求，应精确到小数点后两位，已将余额置为0.00");
            }
        }
        protected BankAccount(String account_id, String name, BigDecimal balance) {
            this.account_id = account_id;
            this.name = name;
            this.balance = balance;
            this.recorder = new Transactions_List();
            this.password = "123456";
            if (this.balance.compareTo(BigDecimal.ZERO) < 0) {
                this.balance = new BigDecimal("0.00");
                throw new IllegalArgumentException("初始余额不能为负数，已将余额置为0.00");
            }
            else if (this.balance.scale() != 2) {
                this.balance = new BigDecimal("0.00");
                throw new IllegalArgumentException("初始余额精度不符合要求，应精确到小数点后两位，已将余额置为0.00");
            }
        }

        public String get_id() {
            return this.account_id;
        }
        public String get_name() {
            return this.name;
        }
        public String set_name(String new_name) {
            this.name = new_name;
            return this.name;
        }
        public BigDecimal get_balance() {
            return this.balance;
        }
        public boolean check_password(String password) {
            return this.password.equals(password);
        }
        public boolean reset_password(String old_passwd, String new_passwd) {
            if (check_password(old_passwd)) {
                this.password = new_passwd;
                return true;
            }
            return false;
        }

        // 改变账户余额，要求参数为精度为2的定点数。

        public BigDecimal deposit(BigDecimal amount) {
            if (amount.scale() != 2) {
                throw new IllegalArgumentException("金额精度不符合要求，应精确到小数点后两位");
            }
            else if (amount.compareTo(BigDecimal.ZERO) <= 0) {
                throw new IllegalArgumentException("不能存入负数或0.00元");
            }
            else {
                return this.change_balance(amount);
            }
        }
        public BigDecimal withdraw(BigDecimal amount) {
            if (amount.scale() != 2) {
                throw new IllegalArgumentException("金额精度不符合要求，应精确到小数点后两位");
            }
            else if (amount.compareTo(BigDecimal.ZERO) <= 0) {
                throw new IllegalArgumentException("不能取出负数或0.00元");
            }
            else {
                return this.change_balance(amount.negate());
            }
        }
        protected BigDecimal change_balance(BigDecimal difference) {
            if (difference.compareTo(this.balance.negate()) >= 0) {
                this.balance = this.balance.add(difference);
            }
            else {
                throw new IllegalArgumentException("账户余额不能为负，操作不能进行");
            }
            return this.balance;
        }
    }

    /**
     * 现金账户类，继承BankAccount类
     * 并且，对比BankAccount，除了换了个名字什么也没做，仅为了形式上统一而设立
     */
    private class CashAccount extends BankAccount {
        public CashAccount(String account_id, BigDecimal balance) {
            super(account_id, balance);
        }
        public CashAccount(String account_id, String name, BigDecimal balance) {
            super(account_id, name, balance);
        }
    }

    /**
     * 信用卡账户类，继承BankAccount类
     * 对比BankAccount类，增加overdraft属性（可透支额度），并修改了change_balance方法的部分逻辑
     */
    private class CreditAccount extends BankAccount {
        private int overdraft;
        public CreditAccount(String account_id, BigDecimal balance, int overdraft) {
            super(account_id, balance);
            if (overdraft < 0) {
                overdraft = 0;
            }
            this.overdraft = overdraft;
        }
        public CreditAccount(String account_id, String name, BigDecimal balance, int overdraft) {
            super(account_id, name, balance);
            this.overdraft = overdraft;
            if (this.overdraft < 0) {
                this.overdraft = 0;
                throw new IllegalArgumentException("可透支额不能是负数，已置为0元");
            }
        }

        public int get_overdraft() {
            return this.overdraft;
        }
        protected BigDecimal change_balance(BigDecimal difference) {
            if (difference.compareTo(this.balance.add(new BigDecimal(this.overdraft)).negate()) >= 0) {
                this.balance = this.balance.add(difference);
            }
            else {
                throw new IllegalArgumentException("账户透支金额不能大于可透支额度，操作不能进行");
            }
            return this.balance;
        }
    }


    /**
     * 对BankAccount的账户创建、存取款操作以及异常处理做一个简单的封装
     */
    private class CheckingAccount {
        private BankAccount target;
        CheckingAccount() {
            this.target = null;
        }
        CheckingAccount(BankAccount account) {
            this.target = account;
        }
        public boolean change_target(BankAccount new_account) {
            this.target = new_account;
            return true;
        }
        public BankAccount get_account() {
            return target;
        }


        // 创建CashAccount
        public BankAccount create_account(String account_id, BigDecimal balance) {
            try {
                this.target = new CashAccount(account_id, balance);
            }
            catch (IllegalArgumentException err) {
                err.printStackTrace(System.out);
                throw new IllegalArgumentException("创建账户失败");
            }
            return this.target;
        }
        public BankAccount create_account(String account_id, String name, BigDecimal balance) {
            try {
                this.target = new CashAccount(account_id, name, balance);
            }
            catch (IllegalArgumentException err) {
                err.printStackTrace(System.out);
                throw new IllegalArgumentException("创建账户失败");
            }
            return this.target;
        }

        // 创建CreditAccount
        public BankAccount create_account(String account_id, BigDecimal balance, int overdraft) {
            try {
                this.target = new CreditAccount(account_id, balance, overdraft);
            }
            catch (IllegalArgumentException err) {
                err.printStackTrace(System.out);
                throw new IllegalArgumentException("创建账户失败");
            }
            return this.target;
        }
        public BankAccount create_account(String account_id, String name, BigDecimal balance, int overdraft) {
            try {
                this.target = new CreditAccount(account_id, name, balance, overdraft);
            }
            catch (IllegalArgumentException err) {
                err.printStackTrace(System.out);
                throw new IllegalArgumentException("创建账户失败");
            }
            return this.target;
        }


        // 存钱取钱
        public BigDecimal deposit(BigDecimal amount) {
            if (this.target == null) {
                throw new IllegalStateException("没有绑定账户，不能进行操作");
            }
            try {
                return target.deposit(amount);
            }
            catch (IllegalArgumentException err) {
                err.printStackTrace(System.out);
            }
            return target.get_balance();
        }
        public BigDecimal withdraw(BigDecimal amount) {
            if (this.target == null) {
                throw new IllegalStateException("没有绑定账户，不能进行操作");
            }
            try {
                return target.withdraw(amount);
            }
            catch (IllegalArgumentException err) {
                err.printStackTrace(System.out);
            }
            return target.get_balance();
        }

    }


    /**
     * 交易流水队列类，保存近期一定数量的交易流水
     * 依赖Transaction类
     */
    private class Transactions_List {
        private Queue<Transaction> transactions_list = new LinkedList<Transaction>();
        private String date_format = "yyyy-MM-dd HH:mm:ss";
        private int max_size = 6;
        public Transactions_List() {}
        public Transactions_List(Integer max_size, String date_format) {
            if (max_size != null) {
                this.max_size = max_size;
            }
            if (date_format != null) {
                this.date_format = date_format;
            }
        }

        public Transaction record(Transaction transaction) {
            this.transactions_list.offer(transaction);
            if (transactions_list.size() > max_size) {
                return transactions_list.poll();
            }
            return null;
        }

        public void print() {
            int table_height = this.transactions_list.size() + 1;
            int table_width = 5;
            String[][] table = new String[table_height][table_width];
            table[0][0] = "交易日期时间";
            table[0][1] = "摘要";
            table[0][2] = "支出";
            table[0][3] = "收入";
            table[0][4] = "余额";
            int row_num = 1;
            for (Transaction transaction: transactions_list) {
                table[row_num][0] = transaction.get_date(date_format);
                table[row_num][1] = transaction.memo;
                table[row_num][2] = transaction.withdrawal.toString();
                table[row_num][3] = transaction.deposit.toString();
                table[row_num++][4] = transaction.balance.toString();
            }
            print_table(table);
        }
    }

    /**
     * 交易流水类，记录单条交易
     * 包括 日期时间、摘要、支出、收入、结余 五个字段
     */
    private class Transaction {
        public Date date;              // 日期时间
        public String memo;            // 摘要
        public BigDecimal withdrawal;  // 支出
        public BigDecimal deposit;     // 收入
        public BigDecimal balance;     // 结余
        public Transaction(Date date, String memo, BigDecimal withdrawal, BigDecimal deposit, BigDecimal balance) {
            this.date = date;
            this.memo = memo;
            if (withdrawal.scale() == 2 && deposit.scale() == 2 && balance.scale() == 2) {
                this.withdrawal = withdrawal;
                this.deposit = deposit;
                this.balance = balance;
            }
            else {
                throw new IllegalArgumentException();
            }
        }
        public String get_date(String format) {
            return (new SimpleDateFormat(format)).format(this.date);
        }
        public void print() {
            System.out.printf(
                    "| %s | %s | %s | %s | %s |\n",
                    get_date("yyyy-MM-dd HH:mm:ss"), this.memo, this.withdrawal, this.deposit, this.balance
            );
        }
    }


    // 常用工具方法定义
    private static void print_repeat(char ch, int n) {
        for (int i = 0; i < n; i++) {
            System.out.print(ch);
        }
    }
    private static void print_line(int[] fields_max_width) {
        System.out.print('-');
        for (int i: fields_max_width) {
            print_repeat('-', i + 2);
            System.out.print('-');
        }
        System.out.println();
    }
    private static void print_fields(String[] fields, int[] fields_max_width) {
        System.out.print("| ");
        int i = 0;
        for (String field: fields) {
            System.out.print(field);
            print_repeat(' ', fields_max_width[i++] - get_str_width(field));
            System.out.print(" | ");
        }
        System.out.println();
    }
    private static void print_table(String[][] table) {
        int table_height = table.length;
        int table_width = table[0].length;

        // 初始化字段最长宽度数组
        int[] fields_max_width = new int[table_width];
        for (int i = 0; i < fields_max_width.length; i++) {
            fields_max_width[i] = 0;
        }
        // 求各字段最大长度
        for (String[] row: table) {
            for (int col_num = 0; col_num < table_width; col_num++) {
                fields_max_width[col_num] = get_str_width(row[col_num]) > fields_max_width[col_num] ?
                        get_str_width(row[col_num]) : fields_max_width[col_num];
            }
        }

        // 打印
        print_line(fields_max_width);
        print_fields(table[0], fields_max_width);
        print_line(fields_max_width);
        for (int row_num = 1; row_num < table_height; row_num++) {
            print_fields(table[row_num], fields_max_width);
        }
        print_line(fields_max_width);
    }
    private static int get_str_width(String str) {
        return str.getBytes(Charset.forName("gbk")).length;
    }


    // ...
    private List<BankAccount> accounts_list = new ArrayList<BankAccount>();
    private int cash_account_num = 0;
    private int credit_account_num = 0;

    public boolean create_account(String name, boolean is_credit, int overdraft, String password, BigDecimal balance) {
        if (password.equals("")) {
            return false;
        }
        CheckingAccount checking_account = new CheckingAccount();
        try {
            if (is_credit) {
                checking_account.create_account(String.format("CREDIT-%09d", (credit_account_num++) + 1), name, balance, overdraft);
            }
            else {
                checking_account.create_account(String.format("CASH-%011d", (cash_account_num++) + 1), name, balance);
            }
        }
        catch (Throwable err) {
            err.printStackTrace(System.out);
            return false;
        }
        checking_account.get_account().reset_password("123456", password);
        return this.accounts_list.add(checking_account.get_account());
    }
    public boolean delete_account(String account_id) {
        BankAccount account = get_account_by_id(account_id);
        if (account != null) {
            accounts_list.remove(account);
            return true;
        }
        return false;
    }
    public BankAccount get_account_by_id(String account_id) {
        for (BankAccount account: accounts_list) {
            if (account.account_id.equals(account_id)) {
                return account;
            }
        }
        return null;
    }
    public BankAccount[] get_accounts_by_name(String name) {
        List<BankAccount> res = new ArrayList<BankAccount>();
        for (BankAccount account: accounts_list) {
            if (account.get_name().equals(name)) {
                res.add(account);
            }
        }
        return (BankAccount[]) res.toArray(new BankAccount[res.size()]);
    }
    public void show_accounts() {
        int table_height = accounts_list.size() + 1;
        int table_width = 5;
        String[][] table = new String[table_height][table_width];
        table[0][0] = "卡号";
        table[0][1] = "开户人姓名";
        table[0][2] = "账户类型";
        table[0][3] = "余额";
        table[0][4] = "可透支额度";

        int row_num = 1;
        for (BankAccount account: accounts_list) {
            table[row_num][0] = account.get_id();
            table[row_num][1] = account.get_name();
            table[row_num][2] = account instanceof CreditAccount ? "信用卡" : "储蓄卡";
            table[row_num][3] = account.get_balance().toString();
            table[row_num++][4] = account instanceof CreditAccount ?
                    String.valueOf(((CreditAccount) account).get_overdraft()) + ".00" : "----";
        }
        print_table(table);
        System.out.printf("账户总数：%d\n", table_height - 1);
    }

    // 显示银行资产
    public void show_capital() {
        String[][] table = new String[3][2];
        table[0][0] = "项目";
        table[0][1] = "值";
        table[1][0] = "现金总量";
        table[2][0] = "用户贷款总量";
        BigDecimal cash = new BigDecimal("0.00");
        BigDecimal loan = new BigDecimal("0.00");
        for (BankAccount account: accounts_list) {
            cash = cash.add(account.balance);
            loan = account.get_balance().compareTo(BigDecimal.ZERO) <= 0 ? loan.add(account.balance.negate()) : loan;
        }
        table[1][1] = cash.toString();
        table[2][1] = loan.toString();
        print_table(table);
    }

    // 存钱
    public boolean deposit(BankAccount account, BigDecimal amount, String password) {
        if (!this.accounts_list.contains(account)) {
            return false;
        }
        if (account.check_password(password)) {
            CheckingAccount checking_account = new CheckingAccount(account);
            checking_account.deposit(amount);
            Transaction transaction = new Transaction(
                    new Date(),
                    "现金存入",
                    new BigDecimal("0.00"),
                    new BigDecimal("0.00"),
                    account.get_balance()
            );
            account.recorder.record(transaction);
            return true;
        }
        return false;
    }

    // 取钱
    public boolean withdraw(BankAccount account, BigDecimal amount, String password) {
        if (!this.accounts_list.contains(account)) {
            return false;
        }
        if (account.check_password(password)) {
            CheckingAccount checking_account = new CheckingAccount(account);
            checking_account.withdraw(amount);
            Transaction transaction = new Transaction(
                    new Date(),
                    "现金支取",
                    amount,
                    new BigDecimal("0.00"),
                    account.get_balance()
            );
            account.recorder.record(transaction);
            return true;
        }
        return false;
    }
}
