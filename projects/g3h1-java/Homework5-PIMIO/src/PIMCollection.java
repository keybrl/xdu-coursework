import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;


/**
 * PIM集合类
 * PIMEntity的容器
 *
 * @author 罗阳豪 16130120191
 */
class PIMCollection<T> extends ArrayList<T> implements Collection<T> {
    Collection<T> getTodos() {
        return getSomething(new PIMTodo());
    }

    Collection<T> getNotes() {
        return getSomething(new PIMNote());
    }

    Collection<T> getAppointments() {
        return getSomething(new PIMAppointment());
    }

    Collection<T> getContacts() {
        return getSomething(new PIMContact());
    }
    Collection<T> getItemsForDate(Date d) {
        Collection<T> resCollection = new PIMCollection<T>();
        for (Object entity: super.toArray()) {
            if (PIMEntity.formatDate(((PIMDate)entity).getDate()).equals(PIMEntity.formatDate(d))) {
                resCollection.add((T) entity);
            }
        }
        return resCollection;
    }

    private Collection<T> getSomething(Object o) {
        Collection<T> resCollection = new PIMCollection<T>();
        for (Object entity: super.toArray()) {
            if (o.getClass().isInstance(entity)) {
                resCollection.add((T) entity);
            }
        }
        return resCollection;
    }
}
