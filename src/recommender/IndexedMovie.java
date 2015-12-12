package recommender;

/**
 *
 * @author Fasgort
 */
public class IndexedMovie implements Comparable<IndexedMovie> {

    private static int idNext = 0;
    private int idMovie;
    private String name;
    private boolean action;
    private boolean adventure;
    private boolean animation;
    private boolean children;
    private boolean comedy;
    private boolean crime;
    private boolean documentary;
    private boolean drama;
    private boolean fantasy;
    private boolean noir;
    private boolean horror;
    private boolean musical;
    private boolean mystery;
    private boolean romance;
    private boolean scifi;
    private boolean thriller;
    private boolean unknown;
    private boolean war;
    private boolean western;

    protected IndexedMovie(String name, boolean action, boolean adventure,
            boolean animation, boolean children, boolean comedy, boolean crime,
            boolean documentary, boolean drama, boolean fantasy, boolean noir,
            boolean horror, boolean musical, boolean mystery, boolean romance,
            boolean scifi, boolean thriller, boolean unknown, boolean war,
            boolean western) {
        idMovie = idNext++;
        this.name = name;
        this.action = action;
        this.adventure = adventure;
        this.animation = animation;
        this.children = children;
        this.comedy = comedy;
        this.crime = crime;
        this.documentary = documentary;
        this.drama = drama;
        this.fantasy = fantasy;
        this.noir = noir;
        this.horror = horror;
        this.musical = musical;
        this.mystery = mystery;
        this.romance = romance;
        this.scifi = scifi;
        this.thriller = thriller;
        this.unknown = unknown;
        this.war = war;
        this.western = western;
    }

    protected static void setNextID(int _idNext) {
        idNext = _idNext;
    }

    protected int getID() {
        return idMovie;
    }

    protected void setID(int idMovie) {
        this.idMovie = idMovie;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isAction() {
        return action;
    }

    public void setAction(boolean action) {
        this.action = action;
    }

    public boolean isAdventure() {
        return adventure;
    }

    public void setAdventure(boolean adventure) {
        this.adventure = adventure;
    }

    public boolean isAnimation() {
        return animation;
    }

    public void setAnimation(boolean animation) {
        this.animation = animation;
    }

    public boolean isChildren() {
        return children;
    }

    public void setChildren(boolean children) {
        this.children = children;
    }

    public boolean isComedy() {
        return comedy;
    }

    public void setComedy(boolean comedy) {
        this.comedy = comedy;
    }

    public boolean isCrime() {
        return crime;
    }

    public void setCrime(boolean crime) {
        this.crime = crime;
    }

    public boolean isDocumentary() {
        return documentary;
    }

    public void setDocumentary(boolean documentary) {
        this.documentary = documentary;
    }

    public boolean isDrama() {
        return drama;
    }

    public void setDrama(boolean drama) {
        this.drama = drama;
    }

    public boolean isFantasy() {
        return fantasy;
    }

    public void setFantasy(boolean fantasy) {
        this.fantasy = fantasy;
    }

    public boolean isNoir() {
        return noir;
    }

    public void setNoir(boolean noir) {
        this.noir = noir;
    }

    public boolean isHorror() {
        return horror;
    }

    public void setHorror(boolean horror) {
        this.horror = horror;
    }

    public boolean isMusical() {
        return musical;
    }

    public void setMusical(boolean musical) {
        this.musical = musical;
    }

    public boolean isMystery() {
        return mystery;
    }

    public void setMystery(boolean mystery) {
        this.mystery = mystery;
    }

    public boolean isRomance() {
        return romance;
    }

    public void setRomance(boolean romance) {
        this.romance = romance;
    }

    public boolean isScifi() {
        return scifi;
    }

    public void setScifi(boolean scifi) {
        this.scifi = scifi;
    }

    public boolean isThriller() {
        return thriller;
    }

    public void setThriller(boolean thriller) {
        this.thriller = thriller;
    }

    public boolean isUnknown() {
        return unknown;
    }

    public void setUnknown(boolean unknown) {
        this.unknown = unknown;
    }

    public boolean isWar() {
        return war;
    }

    public void setWar(boolean war) {
        this.war = war;
    }

    public boolean isWestern() {
        return western;
    }

    public void setWestern(boolean western) {
        this.western = western;
    }

    @Override
    public int compareTo(IndexedMovie w) {
        return idMovie - w.idMovie;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final IndexedMovie other = (IndexedMovie) obj;
        return idMovie == other.idMovie;
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 41 * hash + this.idMovie;
        return hash;
    }

}
