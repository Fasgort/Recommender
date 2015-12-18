package recommender;

import java.util.ArrayList;
import java.util.Iterator;

/**
 *
 * @author Fasgort
 * @param <T1>
 */
public abstract class Dictionary<T1> {

    protected ArrayList<T1> entryIDs; // Entry Dictionary ID -> entry

    protected T1 search(int idEntry) {
        if (idEntry < entryIDs.size()) {
            return entryIDs.get(idEntry);
        } else {
            return null;
        }
    }

    protected abstract int add(T1 newEntry);

    protected int size() {
        return entryIDs.size();
    }

    protected Iterator<T1> iterator() {
        return entryIDs.iterator();
    }

}

