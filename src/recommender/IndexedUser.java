package recommender;

/**
 *
 * @author Fasgort
 */
public class IndexedUser implements Comparable<IndexedUser> {

    private static int idNext = 0;
    private int idUser;
    private String name;
    private short age;
    private boolean male;
    private String occupation;
    private String zipCode;

    protected IndexedUser(String name, short age, boolean male, String occupation, String zipCode) {
        idUser = idNext++;
        this.name = name;
        this.age = age;
        this.male = male;
        this.occupation = occupation;
        this.zipCode = zipCode;
    }

    protected static void setNextID(int _idNext) {
        idNext = _idNext;
    }

    protected int getID() {
        return idUser;
    }

    protected void setID(int idUser) {
        this.idUser = idUser;
    }

    protected String getName() {
        return name;
    }

    protected void setName(String name) {
        this.name = name;
    }

    protected short getAge() {
        return age;
    }

    protected void setAge(short age) {
        this.age = age;
    }

    protected boolean isMale() {
        return male;
    }

    protected void setMale(boolean male) {
        this.male = male;
    }

    protected String getOccupation() {
        return occupation;
    }

    protected void setOccupation(String occupation) {
        this.occupation = occupation;
    }

    protected String getZipCode() {
        return zipCode;
    }

    protected void setZipCode(String zipCode) {
        this.zipCode = zipCode;
    }

    @Override
    public int compareTo(IndexedUser w) {
        return idUser - w.idUser;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final IndexedUser other = (IndexedUser) obj;
        return idUser == other.idUser;
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 41 * hash + this.idUser;
        return hash;
    }

}
