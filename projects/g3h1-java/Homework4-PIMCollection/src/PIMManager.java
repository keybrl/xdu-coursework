import java.io.*;
import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;


/**
 * PIM系统管理类
 * 为用户提供基于文本的交互界面，管理信息
 *
 * @author 罗阳豪 16130120191
 */
public class PIMManager {
    public static void main(String[] args) {
        (new PIMManager()).startSession();
    }


    private PIMCollection<PIMEntity> entities;
    private static final String defaultSavePath = "entities.txt";

    PIMManager() {
        this.entities = new PIMCollection<PIMEntity>();
    }
    void startSession() {
        System.out.println("\nWelcome to PIM.");
        Scanner sc = new Scanner(System.in);
        boolean isQuit = false;
        while (!isQuit) {
            System.out.println("\n---Enter a command (Supported commands are List Create Save Load Quit)---");
            label:
            while (true) {
                System.out.print(">>> ");
                String comm = sc.nextLine();
                switch (comm) {
                    case "List":
                        list();
                        break label;
                    case "Create":
                        create();
                        break label;
                    case "Save":
                        save();
                        break label;
                    case "Load":
                        load();
                        break label;
                    case "Quit":
                        isQuit = true;
                        break label;
                    default:
                        System.out.printf("Command '%s' not found.\n", comm);
                        break;
                }
            }

        }
    }
    private void list() {
        Scanner sc = new Scanner(System.in);
        System.out.println("Which type of information you want to list (Supported todo note appointment contact, or you can enter a date like '04/23/2018' to list this day's information, or you can just press ENTER to list all.):");
        Collection<PIMEntity> resCollection = null;
        while (true) {
            System.out.print("List> ");
            String comm = sc.nextLine();
            switch (comm) {
                case "todo":
                    resCollection = entities.getTodos();
                    break;
                case "note":
                    resCollection = entities.getNotes();
                    break;
                case "contact":
                    resCollection = entities.getContacts();
                    break;
                case "appointment":
                    resCollection = entities.getAppointments();
                    break;
                case "":
                    resCollection = entities;
                    break;
                default:
                    try {
                        resCollection = entities.getItemsForDate(PIMEntity.parseDate(comm));
                    }
                    catch (Exception ex) {
                        System.out.printf("Command '%s' not found.\n", comm);
                    }
                    break;
            }
            if (resCollection != null) {
                break;
            }
        }
        System.out.printf("There are %d items.\n\n", resCollection.size());

        int i = 0;
        for (PIMEntity entity: resCollection) {
            System.out.printf("Item %d: %s\n\n", i + 1, entityToPrintStr(entity));
            i += 1;
        }
    }
    private void create() {
        Scanner sc = new Scanner(System.in);
        System.out.println("Enter an item type (todo, note, contact or appointment) (Enter 'cancel' to cancel.)");
        while (true) {
            System.out.print("Create> ");
            String comm = sc.nextLine();
            switch (comm) {
                case "todo":
                    createTodo();
                    return;
                case "note":
                    createNote();
                    return;
                case "contact":
                    createContact();
                    return;
                case "appointment":
                    createAppointment();
                    return;
                case "cancel":
                    System.out.println("Operation canceled.");
                    return;
                default:
                    System.out.printf("Command '%s' not found.\n", comm);
                    break;
            }
        }
    }
    private void createTodo() {
        Scanner sc = new Scanner(System.in);

        // 获取日期输入
        Date date = null;
        System.out.println("Enter date for todo item (Please follow the format 'MM/dd/yyyy'):");
        while (true) {
            System.out.print("Date> ");
            String comm = sc.nextLine();
            if (comm.equals("cancel")) {
                System.out.println("Operation canceled.");
                return;
            }
            try {
                date = (new SimpleDateFormat("MM/dd/yyyy")).parse(comm);
                break;
            }
            catch (ParseException ex) {
                System.out.println("Unknown date format, please follow the format 'MM/dd/yyyy'.");
            }
        }

        // 获取TODO描述
        System.out.println("Enter todo text (Press ENTER to skip):");
        System.out.print("text> ");
        String text = sc.nextLine();

        // 获取优先级输入
        System.out.println("Enter todo priority (Press ENTER to skip, the priority 'normal' by default):");
        System.out.print("priority> ");
        String comm = sc.nextLine();
        String priority = comm.length() == 0 ? "normal" : comm;

        entities.add(new PIMTodo(priority, date, text));
        System.out.println("Created successfully.");
    }
    private void createNote() {
        Scanner sc = new Scanner(System.in);

        // 获取NOTE内容
        System.out.println("Enter note text (Enter in one line, but you can warp by typing '\\n'):");
        System.out.print("text> ");
        String text = sc.nextLine();

        // 获取优先级输入
        System.out.println("Enter note priority (Press ENTER to skip, the priority 'normal' by default):");
        System.out.print("priority> ");
        String comm = sc.nextLine();
        String priority = comm.length() == 0 ? "normal" : comm;

        entities.add(new PIMNote(priority, text));
        System.out.println("Created successfully.");
    }
    private void createContact() {
        Scanner sc = new Scanner(System.in);

        // 获取First Name输入
        System.out.println("Enter first name (Press ENTER to skip):");
        System.out.print("First Name> ");
        String firstName = sc.nextLine();

        // 获取Last Name输入
        System.out.println("Enter last name (Press ENTER to skip):");
        System.out.print("Last Name> ");
        String lastName = sc.nextLine();

        // 获取Email输入
        System.out.println("Enter email (Press ENTER to skip):");
        System.out.print("Email> ");
        String email = sc.nextLine();

        // 获取优先级输入
        System.out.println("Enter contact priority (Press ENTER to skip, the priority 'normal' by default):");
        System.out.print("priority> ");
        String comm = sc.nextLine();
        String priority = comm.length() == 0 ? "normal" : comm;

        entities.add(new PIMContact(priority, firstName, lastName, email));
        System.out.println("Created successfully.");
    }
    private void createAppointment() {
        Scanner sc = new Scanner(System.in);

        // 获取日期输入
        Date date = null;
        System.out.println("Enter date for appointment item (Please follow the format 'MM/dd/yyyy'):");
        while (true) {
            System.out.print("Date> ");
            String comm = sc.nextLine();
            if (comm.equals("cancel")) {
                System.out.println("Operation canceled.");
                return;
            }
            try {
                date = (new SimpleDateFormat("MM/dd/yyyy")).parse(comm);
                break;
            }
            catch (ParseException ex) {
                System.out.println("Unknown date format, please follow the format 'MM/dd/yyyy'.");
            }
        }

        // 获取Appointment描述
        System.out.println("Enter appointment description (Press ENTER to skip):");
        System.out.print("Description> ");
        String desc = sc.nextLine();

        // 获取优先级输入
        System.out.println("Enter appointment priority (Press ENTER to skip, the priority 'normal' by default):");
        System.out.print("priority> ");
        String comm = sc.nextLine();
        String priority = comm.length() == 0 ? "normal" : comm;

        entities.add(new PIMAppointment(priority, date, desc));
        System.out.println("Created successfully.");
    }
    private void save() {
        Scanner sc = new Scanner(System.in);
        System.out.printf("Enter save path (Press ENTER to skip, the path '%s' by default):\n", defaultSavePath);
        System.out.print("Path> ");
        String filePath = sc.nextLine();
        filePath = filePath.length() == 0 ? defaultSavePath : filePath;

        FileOutputStream file = null;
        try {
            file = new FileOutputStream(filePath);
            OutputStreamWriter writer = new OutputStreamWriter(file, StandardCharsets.UTF_8);
            try {
                for (PIMEntity entity: entities) {
                    writer.write(entity.toString() + "\n");
                }
                writer.flush();
            }
            catch (IOException ex) {
                System.out.printf("Failed to write to file '%s'\n", filePath);
            }
            file.close();
        }
        catch (FileNotFoundException ex) {
            System.out.printf("'%s': No such file or directory.\n", filePath);
            return;
        }
        catch (IOException ex) {
            System.out.printf("Failed to close file '%s'\n", filePath);
            return;
        }
        System.out.println("Saved successfully!");

    }
    private void load() {
        Scanner sc = new Scanner(System.in);
        System.out.printf("Load from where? Enter file path (Press ENTER to skip, the path '%s' by default):\n", defaultSavePath);
        System.out.print("Path> ");
        String filePath = sc.nextLine();
        filePath = filePath.length() == 0 ? defaultSavePath : filePath;

        FileInputStream file = null;
        try {
            file = new FileInputStream(filePath);
            InputStreamReader reader = new InputStreamReader(file, StandardCharsets.UTF_8);
            Scanner fileSc = new Scanner(reader);
            List<String> entitiesStr = new ArrayList<String>();
            while (fileSc.hasNext()) {
                entitiesStr.add(fileSc.nextLine());
            }
            loadFromStr(entitiesStr.toArray(new String[0]));
            file.close();
        }
        catch (FileNotFoundException ex) {
            System.out.printf("'%s': No such file or directory.\n", filePath);
        }
        catch (IOException ex) {
            System.out.printf("Failed to close file '%s'\n", filePath);
        }
    }
    private void loadFromStr(String[] entitiesStr) {
        int count = 0;
        for (String entityStr: entitiesStr) {
            if (entityStr.length() < 4) {
                System.out.printf("String '%s' did not match any entity.\n", entityStr);
                continue;
            }
            try {
                switch (entityStr.substring(0, 4)) {
                    case "TODO":
                        this.entities.add(new PIMTodo(entityStr));
                        count += 1;
                        break;
                    case "NOTE":
                        this.entities.add(new PIMNote(entityStr));
                        count += 1;
                        break;
                    case "APPO":
                        this.entities.add(new PIMAppointment(entityStr));
                        count += 1;
                        break;
                    case "CONT":
                        this.entities.add(new PIMContact(entityStr));
                        count += 1;
                        break;
                    default:
                        System.out.printf("String '%s' did not match any entity.\n", entityStr);
                }
            }
            catch (IllegalArgumentException ex) {
                System.out.printf("String '%s' did not match any entity.\n", entityStr);
                System.out.println(ex.getMessage());
            }
        }
        System.out.printf("Loaded %d items.\n", count);
    }

    private static String entityToPrintStr(PIMEntity pimEntity) {
        if (pimEntity instanceof PIMNote) {
            String res = "NOTE ";
            res += pimEntity.getPriority() + " " + PIMEntity.formatDate(((PIMNote) pimEntity).getDate()) + "\n";
            res += ((PIMNote)pimEntity).getText().replace("\\n", "\n");
            return res;
        }
        else if (pimEntity instanceof PIMContact) {
            String res = "CONTACT ";
            res += pimEntity.getPriority() + " " + PIMEntity.formatDate(((PIMContact) pimEntity).getDate()) + "\n";
            res += "First Name: " + ((PIMContact)pimEntity).getFirstName() + '\n';
            res += "Last Name: " + ((PIMContact)pimEntity).getLastName() + "\n";
            res += "Email: " + ((PIMContact)pimEntity).getEmail();
            return res;
        }
        return pimEntity.toString();
    }
}

