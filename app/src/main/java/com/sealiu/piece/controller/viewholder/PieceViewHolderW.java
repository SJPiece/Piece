package com.sealiu.piece.controller.viewholder;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import com.sealiu.piece.R;
import com.sealiu.piece.model.Piece;

/**
 * Created by liuyang
 * on 2016/8/4.
 */

public class PieceViewHolderW extends RecyclerView.ViewHolder {
    public TextView authorView;
    public TextView likeView;
    public TextView bodyView;
    public TextView dateView;

    public PieceViewHolderW(View itemView) {
        super(itemView);

        authorView = (TextView) itemView.findViewById(R.id.piece_author);
        likeView = (TextView) itemView.findViewById(R.id.piece_like);
        bodyView = (TextView) itemView.findViewById(R.id.piece_body);
        dateView = (TextView) itemView.findViewById(R.id.piece_date);
    }

    public void bindToPiece(Piece piece, View.OnClickListener likeClickListener) {
        authorView.setText(piece.author);
        likeView.setText(String.valueOf(piece.likeCount));
        bodyView.setText(piece.content);
        dateView.setText(piece.date);

        likeView.setOnClickListener(likeClickListener);
    }
}