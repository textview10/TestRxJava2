package com.zonkey.mobileteach_lib.view.drawpaint.controll;

import android.text.TextUtils;

import com.zonkey.mobileteach_lib.MobileTeach;
import com.zonkey.mobileteach_lib.util.LogUtil;
import com.zonkey.mobileteach_lib.view.drawpaint.bean.ActionLineInfo;
import com.zonkey.mobileteach_lib.view.drawpaint.bean.LineInfo;
import com.zonkey.mobileteach_lib.view.drawpaint.interf.IActionController;
import com.zonkey.mobileteach_lib.view.drawpaint.util.PaintInfo2Pc;

import java.util.ArrayList;
import java.util.Stack;

/**
 * Created by xu.wang
 * Date on 2017/9/1 17:32
 * 动作的操作, 如undo,redo
 */

public class ActionController implements IActionController {
    private String TAG = "ActionController";
    private Stack<ActionLineInfo> mUndoStacks = new Stack();  //保存已撤销线条,用于恢复;
    private Stack<ActionLineInfo> mRedoStacks = new Stack();  //保存         ,用于记录撤销逻辑;

    public static final int ADD_SHAPE = 1;  //增加一条新直线
    public static final int DELETE_SHAPE = 2;       //删除线条
    public static final int ADD_MULTI_SHAPE = 3;    //增加多条新直线,本地绘制暂时没有一次增加多个线条的逻辑
    public static final int DELETE_MULTI_SHAPE = 4; //删除多个线条..
    private PaintInfo2Pc mPaintInfo2Pc;

    public ActionController(PaintInfo2Pc paintInfo2Pc) {
        this.mPaintInfo2Pc = paintInfo2Pc;
    }

    /**
     * 撤销
     *
     * @param lists
     */
    @Override
    public boolean undo(ArrayList<LineInfo> lists) {
        if (mUndoStacks.isEmpty()) {
            return false;
        }
        ActionLineInfo pop = mUndoStacks.pop();
        switch (pop.getCmdType()) {
            case ADD_SHAPE:     //如果是新增图形,则撤销哪一笔线条
                if (pop.getObj() instanceof LineInfo) {
                    LineInfo newShapeLine = (LineInfo) pop.getObj();
                    String send = deleteLineInfo(lists, newShapeLine);
                    if (TextUtils.isEmpty(send)) break;
                    if (MobileTeach.CurrentApp == MobileTeach.App_Teachmaster) { //智课助手的undo是撤销上一笔的逻辑...
                        mPaintInfo2Pc.sendUndoInfo(send);
                    } else {
                        mPaintInfo2Pc.sendUndoInfo(DELETE_SHAPE + "|" + send);
                    }
                    mRedoStacks.push(new ActionLineInfo(ADD_SHAPE, newShapeLine));
                }
                break;
            case DELETE_SHAPE:      //如果删除图形,则恢复线条..
                if (pop.getObj() instanceof LineInfo) {
                    LineInfo newShapeLine = (LineInfo) pop.getObj();
                    String send = resumeLineInfo(lists, newShapeLine);
                    if (TextUtils.isEmpty(send)) break;
                    mPaintInfo2Pc.sendUndoInfo(ADD_SHAPE + "|" + send);
                    mRedoStacks.add(new ActionLineInfo(DELETE_SHAPE, newShapeLine));
                }
                break;
            case DELETE_MULTI_SHAPE:        //如果删除,恢复多笔
                if (pop.getObj() instanceof ArrayList) {
                    ArrayList<LineInfo> tempLists = (ArrayList<LineInfo>) pop.getObj();
                    String send = resumeLineInfo(lists, tempLists);
                    if (TextUtils.isEmpty(send)) break;
                    mPaintInfo2Pc.sendUndoInfo(ADD_MULTI_SHAPE + "|" + send);
                    mRedoStacks.add(new ActionLineInfo(DELETE_MULTI_SHAPE, tempLists));
                }
                break;
        }
        if (mUndoStacks.isEmpty()) {
            return false;
        } else {
            return true;
        }
    }

    @Override
    public void pushData(LineInfo lineInfo, int type) {
        if (MobileTeach.CurrentApp == MobileTeach.App_Teachmaster ) {
            if (type ==1) {
                mUndoStacks.push(new ActionLineInfo(type, lineInfo));
            }
        } else {
            mUndoStacks.push(new ActionLineInfo(type, lineInfo));
            mRedoStacks.clear();
        }
    }

    @Override
    public void pushData(ArrayList<LineInfo> lists, int type) {
        if (MobileTeach.CurrentApp == MobileTeach.App_Teachmaster) {
            //智课助手没有多笔操作和删除的逻辑...
            if (type == DELETE_MULTI_SHAPE) {
                mUndoStacks.clear();
                mRedoStacks.clear();
            }
            return;
        }
        mUndoStacks.push(new ActionLineInfo(type, lists));
        mRedoStacks.clear();
    }

    @Override
    public ArrayList<ActionLineInfo> getUndoList() {
        ArrayList<ActionLineInfo> list = new ArrayList<>();
        for (ActionLineInfo action : mUndoStacks) {
            list.add(action);
        }
        return list;
    }

    @Override
    public ArrayList<ActionLineInfo> getRedoList() {
        ArrayList<ActionLineInfo> list = new ArrayList<>();
        for (ActionLineInfo action : mRedoStacks) {
            list.add(action);
        }
        return list;
    }

    @Override
    public void setUndoList(ArrayList<ActionLineInfo> undoList) {
        mUndoStacks.clear();
        for (ActionLineInfo action : undoList) {
            mUndoStacks.push(action);
        }
    }

    @Override
    public void setRedoList(ArrayList<ActionLineInfo> redoList) {
        mRedoStacks.clear();
        for (ActionLineInfo action : redoList) {
            mRedoStacks.push(action);
        }
    }


    /**
     * 恢复
     *
     * @param lists
     */
    @Override
    public boolean redo(ArrayList<LineInfo> lists) {
        if (mRedoStacks.isEmpty()) return false;
        ActionLineInfo actionLineInfo = mRedoStacks.pop();
        switch (actionLineInfo.getCmdType()) {
            case ADD_SHAPE:         //恢复线条,把线条加回去
                if (actionLineInfo.getObj() instanceof LineInfo) {
                    LineInfo temp = (LineInfo) actionLineInfo.getObj();
                    String send = resumeLineInfo(lists, temp);
                    if (TextUtils.isEmpty(send)) break;
                    if (MobileTeach.CurrentApp == MobileTeach.App_Teachmaster) { //智课助手的恢复是恢复上一笔...
                        mPaintInfo2Pc.sendRedoInfo(send);
                    } else {
                        mPaintInfo2Pc.sendRedoInfo(ADD_SHAPE + "|" + send);
                    }
                    mUndoStacks.push(actionLineInfo);
                }
                break;
            case DELETE_SHAPE:      //删除线条,取消掉线条
                if (actionLineInfo.getObj() instanceof LineInfo) {
                    LineInfo lineInfo = (LineInfo) actionLineInfo.getObj();
                    String send = deleteLineInfo(lists, lineInfo);
                    if (TextUtils.isEmpty(send)) break;
                    mPaintInfo2Pc.sendRedoInfo(DELETE_SHAPE + "|" + send);
                    mUndoStacks.push(actionLineInfo);
                }
                break;
            case DELETE_MULTI_SHAPE:    //删除多个线条,取消掉线条
                if (actionLineInfo.getObj() instanceof ArrayList) {
                    ArrayList<LineInfo> temps = (ArrayList<LineInfo>) actionLineInfo.getObj();
                    String send = deleteLineInfo(temps, lists);
                    if (TextUtils.isEmpty(send)) break;
                    mPaintInfo2Pc.sendRedoInfo(DELETE_MULTI_SHAPE + "|" + send);
                    mUndoStacks.push(actionLineInfo);
                }
                break;
        }
        if (mRedoStacks.isEmpty()) {
            return false;
        } else {
            return true;
        }
    }

    /**
     * 根据约定,远程接收到的数据不加入undo,redo栈内
     *
     * @param lists
     * @param receiveBody
     */
    @Override
    public void receiveUndoRedo(ArrayList<LineInfo> lists, String receiveBody) {
        LogUtil.e("ActionController", "" + receiveBody);
        if (TextUtils.isEmpty(receiveBody)) return;
        String[] split = receiveBody.split("\\|");
        if (split == null || split.length != 2) return;
        int action = Integer.parseInt(split[0]);

        if (action == 1) {   //增加一笔
            for (LineInfo lineInfo : lists) {
                if (TextUtils.equals(lineInfo.getLineId(), split[1])) {
                    lineInfo.setIsDelete(0);
                }
            }
        } else if (action == 2) {    //擦除一笔
            for (LineInfo lineInfo : lists) {
                if (TextUtils.equals(lineInfo.getLineId(), split[1])) {
                    lineInfo.setIsDelete(1);
                }
            }
        } else if (action == 3) {    //增加多笔
            String[] ids = split[1].split(",");
            if (ids == null || ids.length == 0) return;
            for (String id : ids) {
                for (LineInfo lineInfo : lists) {
                    if (TextUtils.equals(id, lineInfo.getLineId())) {
                        lineInfo.setIsDelete(0);
                        break;
                    }
                }
            }
        } else if (action == 4) {    //删除多笔
            String[] ids = receiveBody.split(",");
            if (ids == null || ids.length == 0) return;
            for (String id : ids) {
                for (LineInfo lineInfo : lists) {
                    if (TextUtils.equals(id, lineInfo.getLineId())) {
                        lineInfo.setIsDelete(1);
                    }
                }
            }
        }
    }


    //恢复线条
    private String resumeLineInfo(ArrayList<LineInfo> totalLists, LineInfo temp) {
        for (LineInfo lineInfo : totalLists) {
            if (TextUtils.equals(lineInfo.getLineId(), temp.getLineId())) {
                lineInfo.setIsDelete(0);
                return lineInfo.getLineId();
            }
        }
        return null;
    }

    //恢复线条
    private String resumeLineInfo(ArrayList<LineInfo> totalLists, ArrayList<LineInfo> tempLists) {
        StringBuffer sb = new StringBuffer();
        for (LineInfo temp : tempLists) {
            String id = resumeLineInfo(totalLists, temp);
            if (!TextUtils.isEmpty(id)) {
                sb.append(id).append(",");
            }
        }
        return sb.toString();
    }


    //删除线条 ,返回id
    private String deleteLineInfo(ArrayList<LineInfo> totalLists, LineInfo lineInfo) {
        for (int i = 0; i < totalLists.size(); i++) {
            if (TextUtils.equals(lineInfo.getLineId(), totalLists.get(i).getLineId())) {
                totalLists.get(i).setIsDelete(1);
                return totalLists.get(i).getLineId();
            }
        }
        return null;
    }

    private String deleteLineInfo(ArrayList<LineInfo> totalLists, ArrayList<LineInfo> tempLists) {
        StringBuffer sb = new StringBuffer();
        for (LineInfo temp : tempLists) {
            String id = deleteLineInfo(totalLists, temp);
            if (!TextUtils.isEmpty(id)) {
                sb.append(id).append(",");
            }
        }
        return sb.toString();
    }
}
