package com.example.eric.wishare;

import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class WiMyInvitationsButton {
    TextView tvInvitationBadge;
    Button btnMyInvitations;

    public WiMyInvitationsButton(RelativeLayout layout){
        tvInvitationBadge = layout.findViewById(R.id.tv_number_of_invites);
        btnMyInvitations = layout.findViewById(R.id.btn_my_invitations);
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
