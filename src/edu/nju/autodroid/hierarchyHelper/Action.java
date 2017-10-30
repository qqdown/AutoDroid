package edu.nju.autodroid.hierarchyHelper;

/**
 * 在Strategy中使用到的动作
 * Created by ysht on 2016/3/7 0007.
 */
public class Action{
    //nomoreaction一定要放在最后一个
    public enum ActionType {
        Click,
        LongClick,
      //  SetText,
        Home,
        Back,
        ScrollBackward,
        ScrollForward,
        SwipeToRight,
        SwipeToLeft,
        NoAction,//未作动作
        NoMoreAction//没有动作可以做，即所有的都已经做过了
    }

    public ActionType actionType;//行为类型
    public LayoutNode actionNode;//触发该行为的node

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Action action = (Action) o;

        if (actionType != action.actionType) return false;
        if(actionNode != null)
            return actionNode.equals(action.actionNode);
        else
            return true;

    }

    @Override
    public int hashCode() {
        int result = actionType.hashCode();
        if(actionNode != null)
            result = 31 * result + actionNode.hashCode();
        return result;
    }
}

