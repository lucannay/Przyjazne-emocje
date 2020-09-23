package pg.autyzm.przyjazneemocje.View;

public class CheckboxGridBean {

    private String name;
    private boolean checked;

    public CheckboxGridBean(String name, boolean checked) {
        this.setName(name);
        this.setChecked(checked);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isChecked() {
        return checked;
    }

    public void setChecked(boolean checked) {
        this.checked = checked;
    }
}
