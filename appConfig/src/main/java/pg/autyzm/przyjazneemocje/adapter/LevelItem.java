package pg.autyzm.przyjazneemocje.adapter;

public class LevelItem {
    private int levelId;
    private String name;
    private boolean isActive;
    private boolean isLearnMode;
    private boolean isTestMode;
    private boolean canEdit;
    private boolean canRemove;

    private boolean is_default;

    public LevelItem(int levelId, String name, boolean isLearnMode, boolean isTestMode, boolean is_default) {
        this.levelId = levelId;
        this.name = name;
        this.isLearnMode = isLearnMode;
        this.isTestMode = isTestMode;
        this.canEdit = !is_default;
        this.canRemove = !is_default;
        this.is_default = is_default;
    }

    public String getName() {
        return name;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean isActive) {
        this.isActive = isActive;
    }

    public boolean isCanEdit() {
        return canEdit;
    }

    public boolean isCanRemove() {
        return canRemove;
    }

    public int getLevelId() {
        return levelId;
    }

    public boolean isLearnMode() {
        return isLearnMode;
    }

    public void setLearnMode(boolean learnMode) {
        isLearnMode = learnMode;
    }

    public boolean isTestMode() {
        return isTestMode;
    }

    public void setTestMode(boolean testMode) {
        isTestMode = testMode;
    }
}
