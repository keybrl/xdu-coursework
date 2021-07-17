import java.util.ArrayList;
import java.util.List;
import java.util.Arrays;

/**
 * Coutse-Selecting System 选课系统
 */
public class CSS {
    public static void main(String[] args) {
        CSS css = new CSS();
        css.create_data();
        String[] select_courses = new String[10];
        css.select_courses(args[0], Arrays.asList(args).subList(1, args.length));
    }


//    private String data_file_path;
    private List<Student> all_students;
    private List<Course> all_courses;

    public CSS() {
        this.all_students = new ArrayList<Student>();
        this.all_courses = new ArrayList<Course>();
    }
    private void create_data() {
        this.all_courses.add(new Course("Java", new String[]{"Thinking in Java", "Java 8"}));
        this.all_courses.add(new Course("Web Engineering", new String[]{"Web Engineering"}));
        this.all_courses.add(new Course("嵌入式系统基础", new String[]{"ARM Cortex A9 多核嵌入式系统开发教程"}));
        this.all_courses.add(new Course("数据管理技术"));

        this.all_students.add(new Student("16130120191"));
        this.all_students.add(new Student("16130120181"));
        this.all_students.add(new Student("16130120201"));
    }
    public void select_courses(String stu_id, List<String> courses_name) {
        Student target = get_student_by_id(stu_id);
        if (target == null) {
            throw new IllegalArgumentException("学生不存在");
        }

        for (String course_name: courses_name) {
            Course new_course = get_course_by_name(course_name);
            if (new_course == null) {
                throw new IllegalArgumentException("课程不存在");
            }
            try {
                target.add_course(new_course);
            }
            catch (IllegalArgumentException err) {
                err.printStackTrace(System.out);
            }
        }
        target.print_all_courses();
    }
    public Student get_student_by_id(String stu_id) {
        for (Student stu: this.all_students) {
            if (stu.get_id().equals(stu_id)) {
                return stu;
            }
        }
        return null;
    }
    public Course get_course_by_name(String course_name) {
        for (Course course: this.all_courses) {
            if (course.get_name().equals(course_name)) {
                return course;
            }
        }
        return null;
    }
}


class Book {
    private String name;
    public Book(String name) {
        this.name = name;
    }
    public String get_name() {
        return this.name;
    }
}


class Course {
    private String name;
    private List<Book> used_books;
    public Course(String name) {
        this.name = name;
        this.used_books = new ArrayList<Book>();
    }
    public Course(String name, String[] used_books) {
        this.name = name;
        this.used_books = new ArrayList<Book>();
        for (String book_name: used_books) {
            add_book(book_name);
        }
    }
    public void add_book(String book_name) {
        for (Book book: this.used_books) {
            if (book.get_name().equals(book_name)) {
                throw new IllegalArgumentException("该课本已存在，一个课程不能要求学生买两本一样的教材");
            }
        }
        Book new_book = new Book(book_name);
        this.used_books.add(new_book);
    }
    public String get_name() {
        return this.name;
    }
    public List<Book> get_books() {
        return this.used_books;
    }
}


class Student {
    private String id;
    private String name;
    private List<Course> selected_courses;
    public Student(String id) {
        this.id = id;
        this.name = "undefined";
        this.selected_courses = new ArrayList<Course>();
    }
    public Student(String id, String name) {
        this.id = id;
        this.name = name;
        this.selected_courses = new ArrayList<Course>();
    }
    public Student(String id, Course[] courses) {
        this.id = id;
        this.name = "undefined";
        this.selected_courses = Arrays.asList(courses);
    }
    public Student(String id, String name, Course[] courses) {
        this.id = id;
        this.name = name;
        this.selected_courses = Arrays.asList(courses);
    }
    public void add_course(Course new_course) {
        for (Course course: this.selected_courses) {
            if (course == new_course) {
                throw new IllegalArgumentException("课程已选，不能重复选课");
            }
        }
        selected_courses.add(new_course);
    }
    public String get_id() {
        return this.id;
    }
    public String get_name() {
        return this.name;
    }
    public void print_all_courses() {
        if (this.selected_courses.size() == 0) {
            System.out.printf("%s selected no course\n", this.id);
            return;
        }
        for (Course course: this.selected_courses) {
            // 打印第一句
            if (this.selected_courses.get(0) == course) {
                System.out.printf("%s select ", this.id);
            }
            // 打印倒数第一句
            else if (this.selected_courses.get(this.selected_courses.size() - 1) == course){
                System.out.print("; and ");
            }
            // 打印中间其他
            else {
                System.out.print("; ");
            }

            System.out.printf("%s with ", course.get_name());
            if (course.get_books().size() == 0) {
                System.out.print("no book");
            }
            else if (course.get_books().size() == 1) {
                System.out.printf("book %s", course.get_books().get(0).get_name());
            }
            else {
                System.out.print("books ");
                for (Book book: course.get_books()) {
                    if (course.get_books().get(0) != book) {
                        System.out.print(", ");
                    }
                    System.out.print(book.get_name());
                }
            }
        }
        System.out.println();
    }
}
