package StationSim;

public class State {

    private static final long serialVersionUID = 1;

    public long step;
    public String person;
    public double x;
    public double y;
    public double speed;
    public String exit;

    public State(){}

    public State(long step, String person, double x, double y, double speed, String exit) {
        this.step = step;
        this.person = person;
        this.x = x;
        this.y = y;
        this.speed = speed;
        this.exit = exit;
    }

    @Override
    public String toString() {
        return(Long.toString(step) + ", "
                + person + ", "
                + x + ", "
                + y + ", "
                + speed + ", "
                + exit);
    }
}

