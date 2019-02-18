package com.example.eric.wishare;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class WiMyInvitationsButton extends RelativeLayout {
    TextView tvInvitationBadge;
    Button btnMyInvitations;

    public WiMyInvitationsButton(Context context){
        super(context);
        init();
    }

    public WiMyInvitationsButton(Context context, AttributeSet attrs){
        super(context, attrs);
        init();
    }

    private void init(){
        inflate(getContext(), R.layout.layout_my_invitations_button, this);

        tvInvitationBadge = findViewById(R.id.tv_number_of_invites);
        btnMyInvitations = findViewById(R.id.btn_my_invitations2);
    }

    public void setInvitationCount(int count){
        tvInvitationBadge.setText(String.valueOf(count));

        // show invitations if there are some. otherwise hide the badge
        tvInvitationBadge.setVisibility(count > 0 ? View.VISIBLE : View.INVISIBLE);
    }

    public void setOnClickListener(View.OnClickListener onClickListener){
        btnMyInvitations.setOnClickListener(onClickListener);
    }
}
