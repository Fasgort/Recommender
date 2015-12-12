package recommender;

import java.util.ArrayList;

/**
 *
 * @author Fasgort
 */
public class UserDictionary extends Dictionary<IndexedUser> {

    private static volatile UserDictionary instance = null;

    private UserDictionary() {
        ArrayList<IndexedUser> userIDs = null;

        if (userIDs == null) {
            entryIDs = new ArrayList(1000);
        } else {
            entryIDs = userIDs;
        }

    }

    protected static UserDictionary getInstance() {
        if (instance == null) {
            instance = new UserDictionary();
        }
        return instance;
    }

    @Override
    protected int add(IndexedUser newUser) {
        int idUser = newUser.getID();
        entryIDs.add(idUser, newUser);
        return idUser;
    }

}
