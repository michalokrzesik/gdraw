package gdraw.graph.util.action;

public abstract class Action {
    protected ActionHelper from;
    protected ActionHelper to;

    public abstract void action();

    protected void changeStacks(){
        ActionHelper tmp = from;
        to.push(this);
        from = to;
        to = tmp;
        to.forceDraw();
    }

}
