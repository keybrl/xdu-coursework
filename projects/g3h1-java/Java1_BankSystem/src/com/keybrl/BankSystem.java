/**
 * 一个银行系统类
 *
 * @author 罗阳豪 16130120191
 * @author keyboard-w@outlook.com
 */

package com.keybrl;


import java.math.BigDecimal;
import java.nio.charset.Charset;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.ArrayList;
import java.util.Queue;
import java.util.LinkedList;
import java.util.Date;



public class BankSystem {
    // 入口
    public static void main(String[] args) {
        BigDecimal a = new BigDecimal("0.00");
        BigDecimal b = new BigDecimal("100.23");
        System.out.println(b.compareTo(a.negate()));
        // 初始化银行系统
        BankSystem sys = new BankSystem();

        // 添加账户
        sys.create_account("罗阳豪", false, 0, "159284637asd");
        sys.create_account("罗阳豪1", true, 10000, "137asd");
        sys.create_account("罗阳豪2", false, 0, "159287asd");
        sys.create_account("罗阳豪3", false, 100, "159284637asd");
        sys.create_account("罗阳豪4", true, 100, "1284637asd");
        sys.create_account("罗阳豪5", false, 0, "1592637d");
        // 显示账户
        sys.show_accounts();

        // 删除账户
        sys.delete_account(sys.get_accounts_by_name("罗阳豪3")[0].get_id());
        sys.show_accounts();

        // 存款
        sys.deposit(sys.get_account_by_id("CREDIT-000000001"), new BigDecimal("100.23"), "137asd");
        sys.deposit(sys.get_account_by_id("CASH-00000000004"), new BigDecimal("23300.56"), "1592637d");
        sys.show_accounts();
        // 查看银行资产统计
        sys.show_capital();

        // 取钱
        sys.withdraw(sys.get_account_by_id("CREDIT-000000001"), new BigDecimal("2000.00"), "137asd");
        sys.withdraw(sys.get_account_by_id("CREDIT-000000002"), new BigDecimal("99.00"), "1284637asd");
        sys.withdraw(sys.get_account_by_id("CASH-00000000004"), new BigDecimal("12.23"), "1592637d");
        sys.show_accounts();
        sys.show_capital();

        // 更名
        sys.get_account_by_id("CASH-00000000001").set_name("16130120191");
        sys.show_accounts();
    }


    // 内部类定义
    private class BankAccount {
        protected String account_id;        // 卡号
        protected String name;              // 开户人姓名
        protected BigDecimal balance;       // 使用精度为2的定点数存储账户余额，避免浮点运算误差。
        public Transactions_List recorder;  // 交易流水记录器
        private String password;

        protected BankAccount(String account_id) {
            this.account_id = account_id;
            this.name = "Undefined";
            this.balance = new BigDecimal("0.00");
            this.recorder = new Transactions_List();
            this.password = "123456";
        }
        protected BankAccount(String account_id, String name) {
            this.account_id = account_id;
            this.name = name;
            this.balance = new BigDecimal("0.00");
            this.recorder = new Transactions_List();
            this.password = "123456";
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

        // 改变账户余额，要求参数为精度为2的定点数，正数则加，负数若不小于余额的相反数则减。
        // 若参数不符合要求，则不做运算。返回最终余额，可根据返回值与初始值比较检查是否更改成功。
        public BigDecimal change_balance(BigDecimal difference) {
            if (difference.scale() == 2) {
                if (difference.compareTo(this.balance.negate()) >= 0) {
                    this.balance = this.balance.add(difference);
                }
            }
            return this.balance;
        }
    }
    private class CashAccount extends BankAccount {
        public CashAccount(String account_id) {
            super(account_id);
        }
        public CashAccount(String account_id, String name) {
            super(account_id, name);
        }
    }
    private class CreditAccount extends BankAccount {
        private int overdraft;
        public CreditAccount(String account_id, int overdraft) {
            super(account_id);
            if (overdraft < 0) {
                overdraft = 0;
            }
            this.overdraft = overdraft;
        }
        public CreditAccount(String account_id, String name, int overdraft) {
            super(account_id, name);
            if (overdraft < 0) {
                overdraft = 0;
            }
            this.overdraft = overdraft;
        }

        public int get_overdraft() {
            return this.overdraft;
        }

        // 改变账户余额，要求参数为精度为2的定点数，正数则加，负数若不小于余额的相反数则减。
        // 若参数不符合要求，则不做运算。返回最终余额，可根据返回值与初始值比较检查是否更改成功。
        public BigDecimal change_balance(BigDecimal difference) {
            if (difference.scale() == 2) {
                if (difference.compareTo(this.balance.add(new BigDecimal(this.overdraft)).negate()) >= 0) {
                    this.balance = this.balance.add(difference);
                }
            }
            return this.balance;
        }
    }

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

    public boolean create_account(String name, boolean is_credit, int overdraft, String password) {
        if (password.equals("")) {
            return false;
        }
        BankAccount account;
        if (is_credit) {
            overdraft = overdraft >= 0 ? overdraft : 0;
            account = new CreditAccount(String.format("CREDIT-%09d", (credit_account_num++) + 1), name, overdraft);
        }
        else {
            account = new CashAccount(String.format("CASH-%011d", (cash_account_num++) + 1), name);
        }
        account.reset_password("123456", password);
        return this.accounts_list.add(account);
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
        if (amount.scale() != 2 || amount.compareTo(BigDecimal.ZERO) <= 0) {
            return false;
        }
        if (account.check_password(password)) {
            account.change_balance(amount);
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
        if (amount.scale() != 2 || amount.compareTo(BigDecimal.ZERO) <= 0) {
            return false;
        }
        if (account.check_password(password)) {
            account.change_balance(amount.negate());
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
