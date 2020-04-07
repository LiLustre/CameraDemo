package com.zmy.camerademo.util;

import android.content.Context;
import android.content.res.Configuration;
import android.os.Build;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.WindowManager;
import android.widget.GridView;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;

/**
 * Created by DFHJ on 2016/8/1.
 */
public class DisplayUtil {

    /**
     * 设置嵌套listView高度，解决嵌套listview高度显示不全问题
     *
     * @param listView
     */
    public static void setListViewHeightBasedOnChildren(ListView listView, int defaultItemHeight) {
        //获取ListView对应的Adapter
        ListAdapter listAdapter = listView.getAdapter();
        if (listAdapter == null) {
            return;
        }

        int totalHeight = 0;
        for (int i = 0, len = listAdapter.getCount(); i < len; i++) {   //listAdapter.getCount()返回数据项的数目
            //自动测量高度在测试机 华为G750-T01 measure 异常，改为给定高度
//            View listItem = listAdapter.getView(i, null, listView);
//            listItem.measure(0, 0);  //计算子项View 的宽高
//            totalHeight += listItem.getMeasuredHeight();  //统计所有子项的总高度
            totalHeight += defaultItemHeight;
        }

        ViewGroup.LayoutParams params = listView.getLayoutParams();
        //listView.getDividerHeight()获取子项间分隔符占用的高度
        //params.height最后得到整个ListView完整显示需要的高度
        params.height = totalHeight + (listView.getDividerHeight() * (listAdapter.getCount() - 1)) + listView.getPaddingBottom() + listView.getPaddingTop();
        listView.setLayoutParams(params);
    }

    /**
     * 设置嵌套gridView高度，解决嵌套gridView高度显示不全问题
     *
     * @param gridView          gridView
     * @param defaultItemHeight 行高
     * @param listSize          gridview 共多少数据，用来算gridview有几行
     * @param lineSize          一行有几项
     */
    public static void setGridViewHeightBasedOnChildren(GridView gridView, int listSize, int lineSize, int defaultItemHeight) {
        int remainder = listSize % lineSize;
        int gridViewLines = listSize / lineSize;
        if (remainder > 0) {
            gridViewLines = gridViewLines + 1;
        }
        int totalHeight = gridViewLines * defaultItemHeight;
        ViewGroup.LayoutParams params = gridView.getLayoutParams();
        params.height = totalHeight + gridView.getPaddingBottom() + gridView.getPaddingTop();
        gridView.setLayoutParams(params);
    }

    /**
     * 设置内容为列表的对话框的最大高度(如果对话框列表项太多，通过此方法设置其最大高度，竖屏距离顶部和底部50dip，横屏距离顶部和底部20dip)
     */
    public static void setDialogMaxHeight(final Context context, final ListView listView, final int edgeHeight) {
        ViewTreeObserver vto2 = listView.getViewTreeObserver();
        vto2.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                listView.getViewTreeObserver().removeGlobalOnLayoutListener(this);
                int height = listView.getHeight();
                WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
                Display display = wm.getDefaultDisplay();
                LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) listView.getLayoutParams();
                int maxHeight;
                if (context.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
                    maxHeight = display.getHeight() - getStatusBarHeight(context) - dip2px(context, 40) - edgeHeight;
                } else {
                    maxHeight = display.getHeight() - getStatusBarHeight(context) - dip2px(context, 100) - edgeHeight;
                }
                if (height > maxHeight) {
                    params.height = maxHeight;
                    listView.setLayoutParams(params);
                }
            }
        });
    }



    /**
     * 得到状态栏高度的方法
     *
     * @param context
     * @return 画页状态栏高度
     */
    public static int getStatusBarHeight(Context context) {
        int statusBarHeight = 0;
        Class<?> localClass;
        try {
            localClass = Class.forName("com.android.internal.R$dimen");
            Object localObject = localClass.newInstance();
            int x = Integer.parseInt(localClass.getField("status_bar_height").get(localObject).toString());
            statusBarHeight = context.getResources().getDimensionPixelSize(x);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return statusBarHeight;
    }

    /**
     * dip转换成px
     *
     * @param context
     * @param dipValue
     * @return px
     */
    public static int dip2px(Context context, float dipValue) {
        final float scale = context.getResources().getDisplayMetrics().density; // 设备的密度
        return (int) (dipValue * scale + 0.5f);
    }

    /**
     * 将sp值转换为px值，保证文字大小不变
     *
     * @param spValue
     * @return
     */
    public static int sp2px(Context context, float spValue) {
        final float scale = context.getResources().getDisplayMetrics().scaledDensity;
        return (int) (spValue * scale + 0.5f);
    }

    /**
     * 手机号安全显示
     *
     * @param phoneNumber
     */
    public static String phoneNumberConversion(String phoneNumber) {
        String showPhoneNumber = "";
        String startString = phoneNumber.substring(0, 3);
        String convertString = phoneNumber.substring(3, phoneNumber.length() - 2);
        String endString = phoneNumber.substring(phoneNumber.length() - 2);
        showPhoneNumber = startString + convertString.replace(convertString, "******") + endString;
        return showPhoneNumber;
    }


    /**
     * 获取屏幕宽
     *
     * @param context
     * @return
     */
    public static int getScreenWidth(Context context) {
        DisplayMetrics displayMetrics = null;
        displayMetrics = new DisplayMetrics();
        WindowManager windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            windowManager.getDefaultDisplay().getRealMetrics(displayMetrics);
            return displayMetrics.widthPixels;
        } else {
            return context.getResources().getDisplayMetrics().widthPixels;
        }
    }

    public static int getScreenHeight(Context context) {
        DisplayMetrics displayMetrics = null;
        displayMetrics = new DisplayMetrics();
        WindowManager windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            windowManager.getDefaultDisplay().getRealMetrics(displayMetrics);
            return displayMetrics.heightPixels;
        } else {
            return context.getResources().getDisplayMetrics().heightPixels;
        }
    }
}
