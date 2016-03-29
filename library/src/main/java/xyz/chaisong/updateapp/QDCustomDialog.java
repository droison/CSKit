package xyz.chaisong.updateapp;

import android.app.AlertDialog;
import android.content.Context;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

import xyz.chaisong.cskit.R;

/**
 * Created by song on 15/6/7.
 */
public class QDCustomDialog extends AlertDialog {

    private String title;
    private String content;
    private String cancelText;
    private String commitText;
    private View.OnClickListener cancelOnClick;
    private View.OnClickListener commitOnClick;

    public QDCustomDialog(Context context) {
        super(context);
    }

    public QDCustomDialog buildTitle(String title)
    {
        this.title = title;
        return this;
    }

    public QDCustomDialog buildMessage(String content)
    {
        this.content = content;
        return this;
    }

    public QDCustomDialog buildCancelButton(String cancelText, View.OnClickListener cancelOnClick)
    {
        this.cancelText = cancelText;
        this.cancelOnClick = cancelOnClick;
        return this;
    }

    public QDCustomDialog buildCommitButton(String commitText, View.OnClickListener commitOnClick)
    {
        this.commitText = commitText;
        this.commitOnClick = commitOnClick;
        return this;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.custom_dialog);
        Window dialogWindow = this.getWindow();
        WindowManager.LayoutParams lp = dialogWindow.getAttributes();
        dialogWindow.setGravity(Gravity.CENTER);

        DisplayMetrics metrics = new DisplayMetrics();
        WindowManager windowManager = (WindowManager) getContext().getSystemService( Context.WINDOW_SERVICE );
        windowManager.getDefaultDisplay().getMetrics(metrics);

        lp.width = (int)(Math.min(metrics.widthPixels, metrics.heightPixels) * 0.75); // 宽度
        dialogWindow.setAttributes(lp);

        TextView titleView = (TextView)this.findViewById(R.id.custom_dialog_title);
        titleView.setText(title);

        TextView contentView = (TextView)this.findViewById(R.id.custom_dialog_content);
        contentView.setText(content);

        TextView cancelView = (TextView)this.findViewById(R.id.custom_dialog_cancel);
        if (cancelText != null)
        {
            cancelView.setText(cancelText);
        }
        if (cancelOnClick != null)
        {
            cancelView.setOnClickListener(cancelOnClick);
        }

        TextView commitView = (TextView)this.findViewById(R.id.custom_dialog_commit);
        if (commitText != null)
        {
            commitView.setText(commitText);
        }
        if (commitOnClick != null)
        {
            commitView.setOnClickListener(commitOnClick);
        }
    }

}