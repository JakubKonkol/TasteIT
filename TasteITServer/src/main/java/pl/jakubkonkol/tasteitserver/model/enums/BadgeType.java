package pl.jakubkonkol.tasteitserver.model.enums;

public enum BadgeType {
    FIRST_RECIPE(1), MASTER_CHEF(4);

    private int id;

    BadgeType(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }
}
