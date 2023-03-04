package com.xlab13.playhacker.adapters.clan;

import static com.xlab13.playhacker.activities.InitializationActivity.mTools;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.apollographql.apollo.ApolloCall;
import com.apollographql.apollo.api.Response;
import com.apollographql.apollo.exception.ApolloException;
import com.example.GetClansQuery;
import com.example.RequestClanMembershipMutation;
import com.xlab13.playhacker.R;
import com.xlab13.playhacker.network.NetworkService;

import java.util.List;

public class ClansAdapter extends RecyclerView.Adapter<ClansAdapter.ClansViewHolder> {
    Context context;

    private List<GetClansQuery.GetClan> items;

    public ClansAdapter(Context context, List<GetClansQuery.GetClan> items){
        this.context = context;
        this.items = items;
    }


    @NonNull
    @Override
    public ClansViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_clan, parent, false);

        v.setOnClickListener((v1 -> {
            mTools.closeKeyboard(context);
        }));
        return new ClansViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ClansViewHolder holder, int position) {
        GetClansQuery.GetClan item = items.get(position);

        holder.tvName.setText(item.title());
        holder.tvRating.setText("Rating: " + item.rating());

        holder.tvMembers.setText("Members: " + item.members_qty());

        holder.ivAvatar.setImageResource(R.drawable.icon_avatar_clan);


        if (item.is_member_or_in_requests()){
            holder.btnInvite.setVisibility(View.INVISIBLE);
        } else {
            holder.btnInvite.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    mTools.playSound();
                    RequestClanMembershipMutation requestClanMembershipMutation = RequestClanMembershipMutation.builder()
                            .id(item.id()).build();

                    NetworkService.getInstance().getGameClientWithToken()
                            .mutate(requestClanMembershipMutation).enqueue(new ApolloCall.Callback<RequestClanMembershipMutation.Data>() {
                        @Override
                        public void onResponse(@NonNull Response<RequestClanMembershipMutation.Data> response) {
                            if (!response.hasErrors()){
                                ((Activity) context).runOnUiThread(() -> {
                                    holder.btnInvite.setVisibility(View.INVISIBLE);
                                });

                            } else {
                                ((Activity) context).runOnUiThread(() -> {
                                    String error = response.getErrors().get(0).getMessage();

                                    mTools.showErrorDialog(context, error);
                                });
                            }
                        }

                        @Override
                        public void onFailure(@NonNull ApolloException e) {
                            mTools.debug(e.getMessage());
                            mTools.startErrorConnectionActivity(context);
                        }
                    });
                }
            });
        }
    }


    @Override
    public int getItemCount() {
        return items.size();
    }

    public class ClansViewHolder extends RecyclerView.ViewHolder{

        TextView tvName, tvRating, tvMembers;
        ImageView ivAvatar, btnInvite;

        public ClansViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvClanName);
            tvRating = itemView.findViewById(R.id.tvClanRating);
            tvMembers = itemView.findViewById(R.id.tvClanMembers);

            btnInvite = itemView.findViewById(R.id.btnClanInvite);
            ivAvatar = itemView.findViewById(R.id.ivClanAvatar);
        }
    }
}